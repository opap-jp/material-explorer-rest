package jp.opap.material.facade

import jp.opap.material.dao.MongoProjectDao
import jp.opap.material.data.Formats.Dates
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.GitLabApi.ApiVersion
import org.gitlab4j.api.models.{Group, Project => GitLabProject}

import scala.collection.JavaConverters._

class ProjectCollectionFacade(val projectDao: MongoProjectDao) {
  def updateProjects(targets: Seq[(String, String)]): Unit = {
    def loadGroup(url: String, groupName: String): Seq[GitLabProject] = {
      val gitLab = new GitLabApi(ApiVersion.V4, url, null: String)
      val groupApi = gitLab.getGroupApi
      val group: Group = groupApi.getGroup(groupName)
      group.getProjects.asScala
    }

    val storedProjects = this.projectDao.findProjects()
      .map(p => p.id -> p)
      .toMap
    val isChanged = (project: GitLabProject) => !storedProjects
      .get(project.getHttpUrlToRepo)
      .exists(stored => project.getLastActivityAt.toLocal.isEqual(stored.lastActivityAt))

    targets.par.flatMap(target => loadGroup(target._1, target._2))
      .filter(isChanged)
      .seq
      .foreach(projectDao.updateProject)
  }
}
