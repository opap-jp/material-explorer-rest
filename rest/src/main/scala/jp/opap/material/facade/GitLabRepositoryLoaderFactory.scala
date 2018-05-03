package jp.opap.material.facade

import java.util.UUID

import com.google.common.io.ByteStreams
import jp.opap.material.AppConfiguration
import jp.opap.material.facade.GitLabRepositoryLoaderFactory.GitlabRepositoryInfo
import jp.opap.material.facade.RepositoryLoader.{ChangedResult, RepositoryLoaderFactory}
import jp.opap.material.model.ComponentEntry.FileEntry
import jp.opap.material.model.Components.IntermediateFile
import jp.opap.material.model.Repository
import jp.opap.material.model.RepositoryConfig.RepositoryInfo
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.GitLabApi.ApiVersion
import org.gitlab4j.api.models.TreeItem

import scala.collection.JavaConverters._

class GitLabRepositoryLoaderFactory(val config: AppConfiguration) extends RepositoryLoaderFactory {
  override def attemptCreate(info: RepositoryInfo): Option[RepositoryLoader] = info match {
    case info: GitlabRepositoryInfo => Option(create(info))
    case _ => Option.empty
  }

  protected def create(repositoryInfo: GitlabRepositoryInfo): RepositoryLoader = new RepositoryLoader {
    override val info: GitlabRepositoryInfo = repositoryInfo

    val gitLab = new GitLabApi(ApiVersion.V4, repositoryInfo.host, null: String)
    var projectId: Int = _

    override def loadChangedFiles(repository: Option[Repository]): ChangedResult = {
      val projectApi = this.gitLab.getProjectApi
      val repositoryApi = this.gitLab.getRepositoryApi
      val commitsApi = this.gitLab.getCommitsApi

      val project = projectApi.getProject(info.namespace, info.name)
      this.projectId = project.getId
      val head = commitsApi.getCommit(project.getId, project.getDefaultBranch)

      val pager = repositoryApi.getTree(project.getId, null, null, true, 100)
      val tree = Iterable.range(1, pager.getTotalPages + 1)
        .par
        .flatMap(i => pager.page(i).asScala)
        .filter(item => item.getType == TreeItem.Type.BLOB)
        .seq
        .toSeq
      val files = tree.map(item => IntermediateFile(UUID.randomUUID(), item.getName, item.getPath, item.getId))
      repository.map(r => {
        ChangedResult(r.copy(headHash = head.getId), files)
      }).getOrElse({
        ChangedResult(Repository(this.info.id, this.info.id, this.info.title, head.getId), files)
      })
    }

    override def loadFile(file: FileEntry): Array[Byte] = {
      val stream = this.gitLab.getRepositoryApi.getRawBlobContent(this.projectId, file.blobId)
      ByteStreams.toByteArray(stream)
    }
  }
}

object GitLabRepositoryLoaderFactory {
  /**
    * GitLab で取得可能なリポジトリの情報を表現するクラスです。
    *
    * @param host GitLab をホスティングしているサーバーの URL
    * @param namespace GitLab リポジトリの namespace
    * @param name GitLab リポジトリの name
    */
  case class GitlabRepositoryInfo(id: String, title: String, host: String, namespace: String, name: String) extends RepositoryInfo
}
