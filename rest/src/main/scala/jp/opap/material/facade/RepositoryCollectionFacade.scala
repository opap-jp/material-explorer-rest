package jp.opap.material.facade

import java.io.File
import java.util.UUID

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import jp.opap.material.dao.{MongoComponentDao, MongoRepositoryDao, MongoThumbnailDao}
import jp.opap.material.model.{ComponentEntry, DirectoryEntry, FileEntry, IntermediateComponent, IntermediateDirectory, IntermediateFile}
import jp.opap.material.{AppConfiguration, RepositoryConfig, RepositoryInfo}
import org.eclipse.jgit.api.Git

import scala.collection.JavaConverters._

class RepositoryCollectionFacade(val configuration: AppConfiguration,
    val repositoryDao: MongoRepositoryDao, val componentDao: MongoComponentDao, val thumbnailDao: MongoThumbnailDao,
    val eventEmitter: RepositoryDataEventEmitter) {
  val converters: Seq[MediaConverter] = Seq(
      new ImageConverter(new RestResize(configuration.imageMagickHost))
    )

  def updateRepositories(configuration: AppConfiguration): Unit = {
    // TODO: 1. マスタデータから、メタデータで使用する識別子（タグ）の定義と、取得対象のリモートリポジトリのURLを取得する。
    // 取得を試みるプロジェクトのリスト
    val remotes = loadRepositoryConfig(configuration).repositories.asScala

    // 2. すべてのリモートリポジトリをクローンまたはプルする。
    remotes.foreach(r => cloneOrPull(r))

    // 3. リポジトリから、仮想的なファイルの木を構成する。
    val intermediateTrees = remotes.par.map(intermediateTree).toMap

    // TODO: 4. すべてのファイルやディレクトリについて、対応するメタデータ（*.yaml, *.md）があればそれを関連づける。
    // このとき、マスタデータで宣言されていないタグについては警告データを登録する。

    // TODO: 6. マージやスコープなどを考慮して、メタデータとファイルの関連づけを行なう。

    // 木をリストにする。
    val records = intermediateTrees.flatMap(tree => toList(tree._1, tree._2))

    // 7. データベースを空にする。
    this.componentDao.drop()
    this.thumbnailDao.drop()

    // TODO: 8. ファイル名とメタデータの組をデータベースに登録する。
    records.par.foreach(component => {
      this.componentDao.insert(component)

      (component match {
        case _: DirectoryEntry =>  Option.empty
        case file: FileEntry => Option(file)
      }).flatMap(file => {
        this.converters.find(converter => converter.shouldDispatch(file))
          .map(converter => converter.convert(file, getFile(file)))
      }).foreach(thumb => this.thumbnailDao.insert(thumb))
    })
  }

  def loadRepositoryConfig(configuration: AppConfiguration): RepositoryConfig = {
    val mapper = new ObjectMapper(new YAMLFactory())
    try {
      mapper.readValue(new File(configuration.repositories), classOf[RepositoryConfig])
    } catch {
      case e: Exception => throw e
    }
  }

  def cloneOrPull(project: RepositoryInfo): Unit = {
    val dir = new File(this.configuration.repositoryStore, project.id)
    if (dir.exists()) {
      Git.open(dir)
        .pull()
        .call()
    } else {
      val result = Git.cloneRepository()
        .setURI(project.url)
        .setProgressMonitor(new AppProgressMonitor())
        .setDirectory(dir)
        .call()
    }
  }

  def intermediateTree(project: RepositoryInfo): (RepositoryInfo, IntermediateComponent) = {
    def tree(component: File, parentPath: String): IntermediateComponent = {
      val path = parentPath + "/" + component.getName
      if (component.isFile) {
        IntermediateFile(component.getName, path)
      } else {
        val children = component
          .listFiles()
          .filterNot(f => f.isHidden)
          .map(f => tree(f, path))
          .seq
        IntermediateDirectory(component.getName, path, children)
      }
    }

    val root = repositoryPath(project.id)
    (project, tree(root, ""))
  }

  def toList(project: RepositoryInfo, tree: IntermediateComponent): List[ComponentEntry] = {
    def list(tree: IntermediateComponent, parentId: Option[UUID]):  List[ComponentEntry]  = {
      tree match {
        case IntermediateDirectory(name, path, children) => {
          val dir = DirectoryEntry(UUID.randomUUID(), project.id, parentId, name, path)
          dir :: children.flatMap(child => list(child, Option(dir.id))).toList
        }
        case IntermediateFile(name, path) => List(FileEntry(UUID.randomUUID(), project.id, parentId, name, path))
      }
    }

    list(tree, Option.empty)
  }

  def repositoryPath(repositoryId: String): File = new File(this.configuration.repositoryStore, repositoryId)

  def getFile(file: FileEntry): File = new File(this.configuration.repositoryStore, file.path)
}
