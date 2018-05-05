package jp.opap.material.facade

import jp.opap.material.facade.RepositoryLoader.ChangedResult
import jp.opap.material.model.ComponentEntry.FileEntry
import jp.opap.material.model.Components.IntermediateFile
import jp.opap.material.model.Repository
import jp.opap.material.model.RepositoryConfig.RepositoryInfo

trait RepositoryLoader {
  val info: RepositoryInfo

  def loadChangedFiles(repository: Option[Repository]): ChangedResult
  def loadFile(path: FileEntry): Array[Byte]
}

object RepositoryLoader {
  trait RepositoryLoaderFactory {
    def attemptCreate(info: RepositoryInfo): Option[RepositoryLoader]
  }

  case class ChangedResult(repository: Repository, files: Seq[IntermediateFile])
}
