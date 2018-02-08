package jp.opap.material.facade

import java.io.{File, IOException, InputStream}
import java.util.UUID

import jp.opap.data.yaml.Yaml
import jp.opap.material.AppConfiguration
import jp.opap.material.dao.{MongoComponentDao, MongoRepositoryDao, MongoThumbnailDao}
import jp.opap.material.facade.MediaConverter.{ImageConverter, RestResize}
import jp.opap.material.facade.RepositoryLoader.RepositoryLoaderFactory
import jp.opap.material.model.ComponentEntry.{DirectoryEntry, FileEntry}
import jp.opap.material.model.Components.{IntermediateComponent, IntermediateDirectory, IntermediateFile}
import jp.opap.material.model.RepositoryConfig.RepositoryInfo
import jp.opap.material.model.Warning.ComponentWarning
import jp.opap.material.model.{ComponentEntry, Manifest, RepositoryConfig}
import org.slf4j.{Logger, LoggerFactory}

class RepositoryCollectionFacade(val configuration: AppConfiguration,
    val repositoryDao: MongoRepositoryDao, val componentDao: MongoComponentDao, val thumbnailDao: MongoThumbnailDao,
    val eventEmitter: RepositoryDataEventEmitter) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[RepositoryCollectionFacade])

  val loadersFactories: Seq[RepositoryLoaderFactory] = Seq(GitLabRepositoryLoaderFactory)
  val converters: Seq[MediaConverter] = Seq(
      new ImageConverter(new RestResize(configuration.imageMagickHost))
    )

  def updateRepositories(configuration: AppConfiguration): Unit = {
    LOG.info("リポジトリ データの更新を開始しました。")

    // TODO: 1. マスタデータから、メタデータで使用する識別子（タグ）の定義と、取得対象のリモートリポジトリ情報のリストを取得する。
    val manifest = Manifest.fromYaml(Yaml.parse(new File(configuration.manifest)))

    val config = RepositoryConfig.fromYaml(jp.opap.material.data.Yaml.parse(new File(configuration.repositories)))

    val repositories = config._2.repositories
      .flatMap(info => {
        // TODO: リポジトリ用ストレージの名称のサニタイズ（idにハッシュ値をつける）
        val repositoryStore = new File(this.configuration.repositoryStore, info.id)
        this.loadersFactories.view.map(factory => factory.attemptCreate(info, repositoryStore))
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
  }

  def updateRepository(loader: RepositoryLoader): Unit = {
    def toMap(files: Seq[IntermediateFile]) = files.map(file => (file.path, file)).toMap

    LOG.info(s"リポジトリ ${loader.info.id}（${loader.info.title}）の更新を開始しました。")

    val info = loader.info
    val savedRepository = this.repositoryDao.findById(info.id)

    // データベースに保存されている、ファイルのリストを取得します。
    val storedFiles = this.componentDao.findFiles(info.id).map(_.toIntermediate)

    // リモートリポジトリから、変更されたファイルのリストを取得します。
    val changedResult = loader.loadChangedFiles(savedRepository)

    // 変更されたファイルのキャッシュを削除します。
    changedResult.files.foreach(file => loader.deleteCache(file.path))

    // 保存されているファイルのリストに、変更されたファイルをマージします。変更されたファイルのみ、Id が変化します。
    val mergedFiles = (toMap(storedFiles) ++ toMap(changedResult.files)).values.toList.sortBy(file => file.path)

    // ファイルのリストからファイルツリーを作ります。
    val fileTree = intermediateTree(mergedFiles)

    // TODO: すべてのファイルやディレクトリについて、対応するメタデータ（*.yaml, *.md）があればそれを関連づけます。
    // このとき、マスタデータで宣言されていないタグについては警告データを登録します。

    // TODO: マージやスコープなどを考慮して、メタデータとファイルの関連づけを行ないます。

    // ファイルやディレクトリの古い情報をデータベースから削除します。
    this.componentDao.deleteByRepositoryId(info.id)

    // リポジトリの情報をデータベースに書き込みます。
    this.repositoryDao.update(changedResult.repository)

    // ファイルやディレクトリの情報をデータベースに書き込みます。
    LOG.info(s"リポジトリ ${loader.info.id}（${loader.info.title}）のサムネイル生成を開始します。")
    toList(info, fileTree)
      .par
      .foreach(component => {
        this.componentDao.insert(component)

        (component match {
          case _: DirectoryEntry =>  Option.empty
          case file: FileEntry => Option(file)
        })
        .foreach(file => this.updateThumbnaleIfChanged(file, loader))
      })
    LOG.info(s"リポジトリ ${loader.info.id}（${loader.info.title}）のサムネイル生成が終了しました。")
    LOG.info(s"リポジトリ ${loader.info.id}（${loader.info.title}）の更新が終了しました。")
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

  def toList(info: RepositoryInfo, tree: IntermediateComponent): List[ComponentEntry] = {
    def list(tree: IntermediateComponent, parentId: Option[UUID]):  List[ComponentEntry]  = {
      tree match {
        case IntermediateDirectory(id, name, Some(path), children) =>
          val dir = DirectoryEntry(id, info.id, parentId, name, path)
          dir :: children.values.flatMap(child => list(child, Option(dir.id))).toList
        case IntermediateDirectory(id, _, None, children) => children.values.flatMap(child => list(child, Option(id))).toList
        case IntermediateFile(id, name, path) => List(FileEntry(id, info.id, parentId, name, path))
      }
    }

    list(tree, Option.empty)
  }

  def updateThumbnaleIfChanged(file: FileEntry, loader: RepositoryLoader): Unit = {
    // UUID でサムネイルの存在を確認します。
    // あったら、なにもしません。なければ、該当するパスのドキュメントを削除し、ダウンロードし、サムネイル生成、書き込み。
    this.converters.find(converter => converter.shouldDispatch(file))
      .foreach(converter => {
        if (this.thumbnailDao.findById(file.id).isEmpty) {
          try {
            LOG.debug(s"${loader.info.id} - ${file.path} のサムネイルを生成します。")
            val thumb = converter.convert(file, loader.loadFile(file.path, cache = true))
            this.thumbnailDao.deleteByFile(file)
            this.thumbnailDao.insert(thumb, file)
            LOG.debug(s"${loader.info.id} - ${file.path} のサムネイルを生成しました。")
          } catch {
            case e: IOException =>
              val warning = ComponentWarning(UUID.randomUUID(), s"${loader.info.id} - ${file.path} のサムネイルの生成に失敗しました。",
                Option(e.getMessage), file.repositoryId, file.path)
              // TODO: 警告の登録（現在はログ出力）
              LOG.info(warning.message)
          }
        }
      })
  }

  def repositoryPath(repositoryId: String): File = new File(this.configuration.repositoryStore, repositoryId)
}
