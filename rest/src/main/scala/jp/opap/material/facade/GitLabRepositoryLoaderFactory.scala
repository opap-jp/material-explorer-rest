package jp.opap.material.facade

import java.io.File
import java.nio.file.Files
import java.util.UUID

import jp.opap.material.RepositoryConfig.{GitlabRepositoryInfo, RepositoryInfo}
import jp.opap.material.facade.RepositoryLoader.{ChangedResult, RepositoryLoaderFactory}
import jp.opap.material.model.Components.IntermediateFile
import jp.opap.material.model.Repository
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.GitLabApi.ApiVersion
import org.gitlab4j.api.models.TreeItem

import scala.collection.JavaConverters._

object GitLabRepositoryLoaderFactory extends RepositoryLoaderFactory {
  override def attemptCreate(info: RepositoryInfo, storage: File): Option[RepositoryLoader] = info match {
    case info: GitlabRepositoryInfo => Option(create(info, storage))
    case _ => Option.empty
  }

  protected def create(repositoryInfo: GitlabRepositoryInfo, repositoryStore: File): RepositoryLoader = new RepositoryLoader {
    override val info: GitlabRepositoryInfo = repositoryInfo

    val gitLab = new GitLabApi(ApiVersion.V4, "https://gitlab.com/", null: String)
    val store: File = repositoryStore
    var projectId: Int = _
    var hashDictionary: Map[String, String] = _

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
      this.hashDictionary = tree.map(item => item.getPath -> item.getId).toMap

      repository.map(r => {
        val diffs = repositoryApi.compare(project.getId, r.headHash, head.getId).getDiffs.asScala
        val files = diffs
          .par
          .map(diff => IntermediateFile(UUID.randomUUID(), diff.getNewPath.split("/").last, diff.getNewPath))
          .seq
        ChangedResult(r.copy(headHash = head.getId), files)
      }).getOrElse({
        val files = tree.map(item => IntermediateFile(UUID.randomUUID(), item.getName, item.getPath))
        ChangedResult(Repository(this.info.id, this.info.id, this.info.title, head.getId), files)
      })
    }

    override def loadFile(path: String, cache: Boolean): File = {
      val file = new File(this.store, path)
      if (file.exists())
        file
      else {
        val hash = this.hashDictionary.get(path)
        val stream = this.gitLab.getRepositoryApi.getRawBlobContent(this.projectId, hash.get)

        file.getParentFile.mkdirs()
        Files.copy(stream, file.toPath)
        file
      }
    }

    override def deleteCache(path: String): Unit = {
      val file = new File(this.store, path)
      file.delete()
    }
  }
}
