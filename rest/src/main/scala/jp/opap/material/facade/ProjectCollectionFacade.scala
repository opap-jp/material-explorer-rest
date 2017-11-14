package jp.opap.material.facade

import java.io.File

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import jp.opap.material.dao.{MongoItemDao, MongoProjectDao}
import jp.opap.material.model.Project
import jp.opap.material.{AppConfiguration, ProjectConfig, ProjectInfo}

import scala.collection.JavaConverters._

class ProjectCollectionFacade(val projectDao: MongoProjectDao, val itemDao: MongoItemDao) {
  val loaders: Seq[ProjectLoader] = Seq(new GitLabProjectLoader())

  def updateProjects(targets1: Seq[(String, String)], configuration: AppConfiguration): Unit = {
    def loadProjectConfig(): ProjectConfig = {
      val mapper = new ObjectMapper(new YAMLFactory())
      try {
        mapper.readValue(new File(configuration.projects), classOf[ProjectConfig])
      } catch {
        case e: Exception => throw e
      }
    }

    def loadProject(info: ProjectInfo, store: Map[String, Project]): Option[DeferredProject] = {
      this.loaders.find(loader => loader.shouldDispatch(info.protocol))
        .map(loader => loader.loadByProjectInfo(info, store))
        .filter(project => project.isModified)
    }

    // 取得を試みるプロジェクトのリスト
    val targets = loadProjectConfig().projects.asScala

    // プロジェクト ID をインデックスとする、データベースに保存されているプロジェクトの辞書
    val storedProjects = this.projectDao.findProjects()
      .map(p => p.id -> p)
      .toMap

    // 変更されたプロジェクトのみ取得します
    val deferredProjects = targets.view.par
      .map(info => loadProject(info, storedProjects))
      .flatMap(p => p.iterator)

    // 変更されたプロジェクトを更新します
    deferredProjects
      .map(_.project)
      .foreach(projectDao.updateProject)

    // 変更されたプロジェクトのファイルを取得して保存します
    deferredProjects
      .map(_.items)
      .flatMap(_.iterator)
      .foreach(itemDao.update)

    // 参照されなくなったプロジェクトを削除します
    storedProjects.keySet
      .par
      .diff(targets.map(info => info.id).toSet)
      .foreach(projectDao.removeProjectById)
  }
}
