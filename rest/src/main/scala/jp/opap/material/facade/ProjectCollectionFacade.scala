package jp.opap.material.facade

import java.io.File
import java.util.UUID

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import jp.opap.material.dao.{MongoComponentDao, MongoProjectDao, MongoThumbnailDao}
import jp.opap.material.model.{ComponentElement, GenericComponent, IntermediateHead, IntermediateLeaf, _}
import jp.opap.material.{AppConfiguration, ProjectConfig, ProjectInfo}
import org.eclipse.jgit.api.Git

import scala.collection.JavaConverters._

class ProjectCollectionFacade(val configuration: AppConfiguration,
    val projectDao: MongoProjectDao, val componentDao: MongoComponentDao, val thumbnailDao: MongoThumbnailDao,
    val eventEmitter: ProjectDataEventEmitter) {
  val converters: Seq[MediaConverter] = Seq(PngConverter)

  def updateProjects(configuration: AppConfiguration): Unit = {
    // TODO: 1. マスタデータから、メタデータで使用する識別子（タグ）の定義と、取得対象のリモートリポジトリのURLを取得する。
    // 取得を試みるプロジェクトのリスト
    val remotes = loadProjectConfig(configuration).projects.asScala

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
        case CompositeElement(_) => Option.empty[LeafElement[MetaHead, MetaFile]]
        case leaf@LeafElement(_, _) => Option(leaf)
      }).flatMap(leaf => {
        this.converters.find(converter => converter.shouldDispatch(leaf))
          .map(converter => converter.getThumbnail(leaf, getFile(leaf)))
      }).foreach(thumb => this.thumbnailDao.insert(thumb))
    })
  }

  def loadProjectConfig(configuration: AppConfiguration): ProjectConfig = {
    val mapper = new ObjectMapper(new YAMLFactory())
    try {
      mapper.readValue(new File(configuration.projects), classOf[ProjectConfig])
    } catch {
      case e: Exception => throw e
    }
  }

  def cloneOrPull(project: ProjectInfo): Unit = {
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

  def intermediateTree(project: ProjectInfo): (ProjectInfo, GenericComponent[IntermediateHead, IntermediateLeaf]) = {
    def tree(component: File, parentPath: String): GenericComponent[IntermediateHead, IntermediateLeaf] = {
      val path = parentPath + "/" + component.getName
      if (component.isFile) {
        Leaf(IntermediateHead(component.getName, path), IntermediateLeaf())
      } else {
        val children = component
          .listFiles()
          .filterNot(f => f.isHidden)
          .map(f => tree(f, path))
          .seq
        Composite(IntermediateHead(component.getName, path), children)
      }
    }

    val root = repositoryPath(project.id)
    (project, tree(root, ""))
  }

  def toList(project: ProjectInfo, tree: GenericComponent[IntermediateHead, IntermediateLeaf]): List[ComponentElement[MetaHead, MetaFile]] = {
    def list(tree: GenericComponent[IntermediateHead, IntermediateLeaf], parentId: Option[UUID]):  List[ComponentElement[MetaHead, MetaFile]]  = {
      val createHead = (name: String, path: String) => MetaHead(UUID.randomUUID(), project.id, parentId, name, path)
      tree match {
        case Composite(head, children) => {
          val dir = CompositeElement[MetaHead, MetaFile](createHead(head.name, head.path))
          dir :: children.flatMap(child => list(child, Option(dir.head.id))).toList
        }
        case Leaf(head, payload) => List(LeafElement(createHead(head.name, head.path), MetaFile()))
      }
    }

    list(tree, Option.empty)
  }

  def repositoryPath(repositoryId: String): File = new File(this.configuration.repositoryStore, repositoryId)

  def getFile(file: LeafElement[MetaHead, MetaFile]): File = new File(this.configuration.repositoryStore, file.head.path)
}
