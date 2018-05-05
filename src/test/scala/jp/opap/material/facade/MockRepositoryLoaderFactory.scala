package jp.opap.material.facade

import jp.opap.material.facade.RepositoryLoader.RepositoryLoaderFactory
import jp.opap.material.model.RepositoryConfig.RepositoryInfo
import jp.opap.material.model.{ComponentEntry, Repository, RepositoryConfig}

object MockRepositoryLoaderFactory extends RepositoryLoaderFactory {
  override def attemptCreate(info: RepositoryConfig.RepositoryInfo): Option[RepositoryLoader] = Option(create(info))

  protected def create(repositoryInfo: RepositoryInfo): RepositoryLoader = new RepositoryLoader {
    override val info: RepositoryConfig.RepositoryInfo = repositoryInfo

    override def loadChangedFiles(repository: Option[Repository]): RepositoryLoader.ChangedResult = ???

    override def loadFile(path: ComponentEntry.FileEntry): Array[Byte] = ???
  }
}
