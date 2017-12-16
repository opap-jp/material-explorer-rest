package jp.opap.material.facade

import java.io.File

import jp.opap.material.RepositoryConfig.RepositoryInfo
import jp.opap.material.facade.RepositoryLoader.ChangedResult
import jp.opap.material.model.Components.IntermediateFile
import jp.opap.material.model.Repository

trait RepositoryLoader {
  val info: RepositoryInfo
  def loadChangedFiles(repository: Option[Repository]): ChangedResult
  def loadFile(path: String, cache: Boolean): File
  def deleteCache(path: String): Unit
}

object RepositoryLoader {
  trait RepositoryLoaderFactory {
    def attemptCreate(info: RepositoryInfo, storage: File): Option[RepositoryLoader]
  }

  case class ChangedResult(repository: Repository, files: Seq[IntermediateFile])
}
