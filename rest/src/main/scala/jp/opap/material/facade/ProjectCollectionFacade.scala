package jp.opap.material.facade

import java.io.File

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import jp.opap.material.dao.MongoProjectDao
import jp.opap.material.data.Formats.Dates
import jp.opap.material.model.Project
import jp.opap.material.{AppConfiguration, ProjectConfig, ProjectInfo}
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.GitLabApi.ApiVersion
import org.gitlab4j.api.models.{Project => GitLabProject}

import scala.collection.JavaConverters._

class ProjectCollectionFacade(val projectDao: MongoProjectDao) {
  def updateProjects(targets1: Seq[(String, String)], configuration: AppConfiguration): Unit = {
    def loadProjectConfig(): ProjectConfig = {
      val mapper = new ObjectMapper(new YAMLFactory())
      try {
        mapper.readValue(new File(configuration.projects), classOf[ProjectConfig])
      } catch {
        case e: Exception => throw e
      }
    }

    def loadProject(info: ProjectInfo, store: Map[String, Project]): Option[Project] = {
      if (info.protocol.toLowerCase() == "gitlab") {
        val gitLab = new GitLabApi(ApiVersion.V4, "https://gitlab.com/", null: String)
        val projectApi = gitLab.getProjectApi
        val response = projectApi.getProject(info.namespace, info.projectName)
        val isModified = store.get(info.id)
          .forall(p => p.lastActivityAt != response.getLastActivityAt.toLocal)
        if (isModified) {
          Option(Projects.fromGitLab(info, response))
        } else {
          Option.empty
        }
      } else {
        // TODO: エラーを出力する
        Option.empty
      }
    }

    // 取得を試みるプロジェクトのリスト
    val targets = loadProjectConfig().projects.asScala

    // プロジェクト ID をインデックスとする、データベースに保存されているプロジェクトの辞書
    val storedProjects = this.projectDao.findProjects()
      .map(p => p.id -> p)
      .toMap

    // 変更されたプロジェクトのみ取得し、更新します
    targets.par
      .map(info => loadProject(info, storedProjects))
      .flatMap(p => p.iterator)
      .foreach(projectDao.updateProject)

    // 参照されなくなったプロジェクトを削除します
    storedProjects.keySet.diff(targets.map(info => info.id).toSet)
        .foreach(projectDao.removeProjectById)
  }

  object Projects {
    def fromGitLab(info: ProjectInfo, item: GitLabProject): Project = {
      Project(info.id, info.name, info.title, item.getLastActivityAt.toLocal)
    }
  }
}
