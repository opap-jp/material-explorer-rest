package jp.opap.material.facade

import java.io.File
import java.util.UUID

import jp.opap.material.RepositoryConfig.RepositoryInfo
import jp.opap.material.facade.RepositoryLoader.{ChangedResult, RepositoryLoaderFactory}
import jp.opap.material.model.{IntermediateFile, Repository}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.TextProgressMonitor

@deprecated
object GitRepositoryLoaderFactory extends RepositoryLoaderFactory {
  override def attemptCreate(info: RepositoryInfo, storage: File): Option[RepositoryLoader] = Option.empty

  protected def create(repositoryInfo: RepositoryInfo, repositoryStore: File): RepositoryLoader = new RepositoryLoader {
    override val info: RepositoryInfo = repositoryInfo

    val store: File = repositoryStore

    override def loadChangedFiles(repository: Option[Repository]): ChangedResult = {
      repository.map(r => {
        val git = Git.open(this.store)
        git.pull()
          .setProgressMonitor(new TextProgressMonitor())
          .call()
      }).getOrElse({
        Git.cloneRepository()
          .setURI(this.info.id)
          .setDirectory(this.store)
          .setProgressMonitor(new TextProgressMonitor())
          .call()
      })
      throw new UnsupportedOperationException()
    }

    override def loadFile(path: String, cache: Boolean): File = {
      new File(this.store, "." + path)
    }

    override def deleteCache(path: String): Unit = Unit

    def toList(repository: File): Seq[IntermediateFile] = {
      def list(component: File, parentPath: String): Seq[IntermediateFile] = {
        val path = parentPath + "/" + component.getName
        if (component.isFile) {
          Seq(IntermediateFile(UUID.randomUUID(), component.getName, path))
        } else {
          component
            .listFiles()
            .filterNot(f => f.isHidden)
            .flatMap(f => list(f, path))
        }
      }

      repository
        .listFiles()
        .filterNot(f => f.isHidden)
        .flatMap(f => list(f, ""))
    }
  }
}
