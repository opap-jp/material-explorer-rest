package jp.opap.material.facade

import jp.opap.material.ProjectInfo
import jp.opap.material.model.{Item, Project}
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.GitLabApi.ApiVersion
import jp.opap.material.data.Collections.Iterables
import jp.opap.material.data.Formats.Dates
import org.gitlab4j.api.models.{Commit => GitLabCommit, TreeItem}


import scala.collection.JavaConverters._
import scala.collection.parallel.ParIterable

trait ProjectLoader {
  // def loadByProjectInfo(info: ProjectInfo): (Project, Iterable[Item])
  def shouldDispatch(protocol: String): Boolean

  def loadByProjectInfo(info: ProjectInfo, storedProjects: Map[String, Project]): DeferredProject
}

trait DeferredProject {
  def project: Project

  def isModified: Boolean

  def items: ItemIterable
}

trait ItemIterable {
  def count: Int
  def iterator: ParIterable[Item]
}

class GitLabProjectLoader extends ProjectLoader {
  override def shouldDispatch(protocol: String): Boolean = protocol.toLowerCase == "gitlab"

  override def loadByProjectInfo(info: ProjectInfo, storedProjects: Map[String, Project]): DeferredProject = {
    val gitLab = new GitLabApi(ApiVersion.V4, "https://gitlab.com/", null: String)
    val projectApi = gitLab.getProjectApi
    val repositoryApi = gitLab.getRepositoryApi
    val response = projectApi.getProject(info.namespace, info.projectName)

    new DeferredProject() {
      lazy val head: GitLabCommit = gitLab.getCommitsApi.getCommit(response.getId, response.getDefaultBranch)

      override def project: Project = {
        // val head = gitLab.getCommitsApi.getCommit(response.getId, response.getDefaultBranch)
        Project(info.id, info.name, info.title, response.getLastActivityAt.toLocal, this.head.getId)
      }

      override def isModified: Boolean = {
        val oldData = storedProjects.get(info.id)
        oldData.forall(p => p.lastActivityAt != response.getLastActivityAt.toLocal)
      }

      override def items: ItemIterable = {
        def itemsIfStored(project: Project): ItemIterable = {
          val commitsApi = gitLab.getCommitsApi
          val head = commitsApi.getCommit(response.getId, response.getDefaultBranch)
          val diffs = repositoryApi.compare(response.getId, project.headHash, this.head.getId)
            .getDiffs

          new ItemIterable {
            override def count: Int = diffs.size()

            override def iterator: ParIterable[Item] = diffs.toIterable
              .view
              .par
              .map(diff => Item(info.id, diff.getNewPath))
          }
        }

        def itemsOfNew: ItemIterable = {
          val pager = repositoryApi.getTree(response.getId, null, null, true, 100)

          new ItemIterable {
            override def count: Int = pager.getTotalItems

            override def iterator: ParIterable[Item] = {
              def itemOf(treeItem: TreeItem): Item = Item(info.id, treeItem.getPath)

              Iterable
                .range(1, pager.getTotalPages + 1)
                .view
                .par
                .flatMap(page => pager.page(page).asScala)
                .map(itemOf)
            }
          }
        }

        storedProjects.get(info.id)
          .map[ItemIterable](itemsIfStored)
          .getOrElse[ItemIterable](itemsOfNew)
      }
    }
  }
}
