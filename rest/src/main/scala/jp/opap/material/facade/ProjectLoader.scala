package jp.opap.material.facade

import java.util.stream.IntStream

import jp.opap.material.ProjectInfo
import jp.opap.material.data.Formats.Dates
import jp.opap.material.model.{Blob, Item, ItemType, Project}
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.GitLabApi.ApiVersion
import org.gitlab4j.api.models.{TreeItem, Commit => GitLabCommit}

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
  def iterator: java.util.stream.Stream[Item]
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

            override def iterator: java.util.stream.Stream[Item] = diffs.parallelStream()
              .map(diff => Item(info.id, Blob, diff.getNewPath))
          }
        }

        def itemsOfNew: ItemIterable = {
          val pager = repositoryApi.getTree(response.getId, null, null, true, 100)

          new ItemIterable {
            override def count: Int = pager.getTotalItems

            override def iterator: java.util.stream.Stream[Item] = {
              def itemOf(treeItem: TreeItem): Item = Item(info.id, ItemType.valueOf(treeItem.getType.toString), treeItem.getPath)

              IntStream.range(1, pager.getTotalPages + 1)
                .parallel()
                .mapToObj(i => Int.box(i))
                .flatMap(page => pager.page(page).stream())
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
