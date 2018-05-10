package jp.opap.material.facade

import java.io.IOException
import java.util.UUID

import jp.opap.material.AppConfiguration
import jp.opap.material.MaterialExplorer.ServiceBundle
import jp.opap.material.facade.RepositoryCollectionFacade.Context
import jp.opap.material.facade.RepositoryLoader.RepositoryLoaderFactory
import jp.opap.material.model.ComponentEntry.{DirectoryEntry, FileEntry}
import jp.opap.material.model.Components.{IntermediateComponent, IntermediateDirectory, IntermediateFile}
import jp.opap.material.model.MetaComponent.{MetaDirectory, MetaFile}
import jp.opap.material.model.RepositoryConfig.RepositoryInfo
import jp.opap.material.model.Warning.{ComponentWarning, GlobalWarning}
import jp.opap.material.model.{ComponentEntry, Manifest, MetaComponent, Metadata, RepositoryConfig, Warning}
import org.slf4j.{Logger, LoggerFactory}

class RepositoryCollectionFacade(
  val context: Context,
  val services: ServiceBundle,
  val converters: Seq[MediaConverter],
  val loaderFactories: Seq[RepositoryLoaderFactory],
  val configuration: AppConfiguration,
  val eventEmitter: RepositoryDataEventEmitter
) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[RepositoryCollectionFacade])

  def updateRepositories(): Unit = {
    LOG.info("リポジトリ データの更新を開始しました。")

    val manifest = context.manifest
    val config = context.repositoryConfig

    val repositories = config._2.repositories
      .flatMap(info => {
        this.loaderFactories.view
          .map(factory => factory.attemptCreate(info))
          .find(loader => loader.isDefined)
          .flatten
          .seq
      })

    // TODO: 警告の登録
    manifest._1.++(config._1).foreach(w => {
      LOG.info(w.message)
      w.caused.foreach(LOG.info)
    })

    LOG.info("リポジトリ ローダーを生成しました。")
    repositories.foreach(updateRepository)
    LOG.info("リポジトリ データの更新が終了しました。")
    // TODO: 取得リポジトリ設定ファイルで除外されたリポジトリを削除する
  }

  def updateRepository(loader: RepositoryLoader): Unit = {
    def toMap(files: Seq[IntermediateFile]): Map[String, IntermediateFile] = files.map(file => (file.path, file)).toMap

    LOG.info(s"リポジトリ ${loader.info.id}（${loader.info.title}）の更新を開始しました。")

    val info = loader.info
    val savedRepository = this.services.repositoryDao.findById(info.id)

    // データベースに保存されている、ファイルのリストを取得します。
    val storedFiles = this.services.componentDao.findFiles(info.id).map(_.toIntermediate)

    // リモートリポジトリから、変更されたファイルのリストを取得します。
    val changedResult = loader.loadChangedFiles(savedRepository)

    // 最新のファイルのリストについて、保存されているファイルのリストとパスが一致するものがあれば、古い ID で書き換えます
    val files = merge(storedFiles, changedResult.files)

    // ファイルのリストからファイルツリーを作ります。
    val fileTree = intermediateTree(files.sortBy(file => file.path))

    // TODO: すべてのファイルやディレクトリについて、対応するメタデータ（*.yaml, *.md）があればそれを関連づけます。
    // このとき、マスタデータで宣言されていないタグについては警告データを登録します。

    // TODO: マージやスコープなどを考慮して、メタデータとファイルの関連づけを行ないます。

    // ファイルやディレクトリの古い情報をデータベースから削除します。
    this.services.componentDao.deleteByRepositoryId(info.id)

    // リポジトリの情報をデータベースに書き込みます。
    this.services.repositoryDao.update(changedResult.repository)

    // ファイルやディレクトリの情報をデータベースに書き込みます。
    LOG.info(s"リポジトリ ${loader.info.id}（${loader.info.title}）のサムネイル生成を開始します。")
    toList(info, fileTree)
      .par
      .foreach(component => {
        this.services.componentDao.insert(component)

        (component match {
          case _: DirectoryEntry =>  Option.empty
          case file: FileEntry => Option(file)
        })
        .foreach(file => this.updateThumbnaleIfChanged(file, loader))
      })
    LOG.info(s"リポジトリ ${loader.info.id}（${loader.info.title}）のサムネイル生成が終了しました。")
    LOG.info(s"リポジトリ ${loader.info.id}（${loader.info.title}）の更新が終了しました。")
  }

  def merge(stored: Seq[IntermediateFile], recent: Seq[IntermediateFile]): Seq[IntermediateFile] = {
    val dictionary = stored.map(file => file.path -> file).toMap
    recent.map(file =>
      dictionary.get(file.path ) match {
        case Some(old) => file.copy(id = old.id)
        case None => file
      }
    )
  }

  def intermediateTree(files: Seq[IntermediateFile]): IntermediateComponent = {
    def updateDirectory(file: IntermediateFile, parent: IntermediateDirectory, portions: List[String]): IntermediateDirectory = {
      portions match {
        case _ :: Nil => parent.copy(children = parent.children + (file.name -> file))
        case head :: followings =>
          val dir = parent.children.get(head)
            .flatMap {
              case dir: IntermediateDirectory => Option(dir)
              case _ => throw new IllegalArgumentException(file.toString)
            }
            .getOrElse(IntermediateDirectory(UUID.randomUUID(), head, parent.path.map(p => s"$p/$head").orElse(Option(head)) , Map()))

          val updated = updateDirectory(file, dir, followings)
          parent.copy(children = parent.children + (updated.name -> updated))
        case _ => throw new IllegalArgumentException(file.toString)
      }
    }

    def accumulate(acc: IntermediateDirectory, file: IntermediateFile): IntermediateDirectory = {
      val portions = file.path.split("/").toList
      updateDirectory(file, acc, portions)
    }

    val root = IntermediateDirectory(UUID.randomUUID(), "", Option.empty, Map())
    files.seq.foldLeft(root)(accumulate)
  }

  def metaComponent(component: IntermediateComponent): MetaComponent = {
    // TODO: 作成中です
    component match {
      case IntermediateDirectory(id, name, path, children) => {
        val metaChildren = children.map(x => x._1 -> metaComponent(x._2))
        MetaDirectory(id, name, Metadata(Seq()), metaChildren)
      }
      case IntermediateFile(id, name, path, blobId) =>
        // TODO: ここで、メタデータを参照するために兄弟ファイル（name.yaml）を参照できないといけない。
        MetaFile(id, name, Metadata(Seq()))
    }
  }

  /**
    * コンポーネントの木を、親への参照を保持したコンポーネントのリストに変換します。
    *
    * @param info リポジトリ情報
    * @param tree コンポーネントの木。変換の対象です。
    * @return 親への参照を保持したコンポーネントのリスト。これらのコンポーネントは子への参照を持ちません。
    */
  def toList(info: RepositoryInfo, tree: IntermediateComponent): List[ComponentEntry] = {
    /**
      * コンポーネントの木をリストに変換する関数です。
      * 再帰的に呼びだされます。再帰の最初の呼び出しは、木のルート（ルートディレクトリ）に対してのみ行なわれます。
      * <ol>
      *   <li>再帰の最初として、木のルートに対して呼びだされたときのみマッチします。
      *   自身のすべての子に対して再帰的な変換を行ない、それぞれの結果（リスト）を連結したリストを返します。</li>
      *   <li>ディレクトリに対して呼び出されたとき、
      *   自身のすべての子に対して再帰的な変換を行ない、自身と、それぞれの結果を連結したリストを連結したリストを返します。</li>
      *   <li>ファイルに対して呼びだされたとき、再帰の底につき、このファイルからなる、要素が1個のリストを返します。</li>
      * </ol>
      *
      * @param tree コンポーネントの木。変換の対象です。
      * @param parentId 変換の対象のコンポーネントの親コンポーネントの ID。木のルートに関する呼び出しのときのみ None です。
      * @return コンポーネントのリスト。ファイルとディレクトリです。
      */
    def list(tree: IntermediateComponent, parentId: Option[UUID]):  List[ComponentEntry]  = {
      tree match {
        case IntermediateDirectory(id, _, None, children) =>
          // TODO: ルートディレクトリもコンポーネントとして返したほうがよいのではないか
          children.values.flatMap(child => list(child, Option(id))).toList
        case IntermediateDirectory(id, name, Some(path), children) =>
          val dir = DirectoryEntry(id, info.id, parentId, name, path)
          dir :: children.values.flatMap(child => list(child, Option(dir.id))).toList
        // ファイルに対するリスト化は、要素が1個のリストを返します。
        case IntermediateFile(id, name, path, blobId) =>
          // ファイルは、必ず親ディレクトリを持つ。
          List(FileEntry(id, info.id, Some(parentId.get), name, path, blobId))
      }
    }

    // 再帰の最初の呼び出し。
    // parentId として empty が与えられる唯一の呼び出しです。
    list(tree, Option.empty)
  }

  def updateThumbnaleIfChanged(file: FileEntry, loader: RepositoryLoader): Unit = {
    // UUID でサムネイルの存在を確認します。
    // あったら、なにもしません。なければ、該当するパスのドキュメントを削除し、ダウンロードし、サムネイル生成、書き込み。
    this.converters.find(converter => converter.shouldDispatch(file))
      .foreach(converter => {
        if (this.services.thumbnailDao.find(file.blobId).isEmpty) {
          try {
            LOG.debug(s"${loader.info.id} - ${file.path} のサムネイルを生成します。")
            val data = this.services.cacheDao.loadIfAbsent(file.blobId, _ => loader.loadFile(file))
            val (thumb, converted) = converter.convert(file, data)
            this.services.thumbnailDao.insert(thumb, converted)
            LOG.debug(s"${loader.info.id} - ${file.path} のサムネイルを生成しました。")
          } catch {
            case e: IOException =>
              val warning = ComponentWarning(UUID.randomUUID(), s"${loader.info.id} - ${file.path} のサムネイルの生成に失敗しました。",
                Option(e.getMessage), file.id)
              // TODO: 警告の登録（現在はログ出力）
              LOG.info(warning.message)
              e.printStackTrace()
          }
        } else {
          LOG.debug(s"${loader.info.id} - ${file.path} のサムネイルの存在を確認しました。")
        }
      })
  }
}

object RepositoryCollectionFacade {
  val WARNING_NO_SUCH_LOADER: String = "%1$s - このリポジトリの取得方式に対応していません。"

  case class Context(manifest: (Seq[Warning], Manifest), repositoryConfig: (Seq[Warning], RepositoryConfig))
}
