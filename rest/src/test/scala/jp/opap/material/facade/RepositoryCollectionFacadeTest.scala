package jp.opap.material.facade

import java.nio.file.Files

import jp.opap.material.AppConfiguration
import jp.opap.material.MaterialExplorer.ServiceBundle
import jp.opap.material.dao.{MongoComponentDao, MongoRepositoryDao, MongoThumbnailDao}
import jp.opap.material.facade.MediaConverter.ImageConverter
import jp.opap.material.model.RepositoryConfig
import org.mockito.Mockito.mock
import org.scalatest.FunSpec
import jp.opap.material.model.Manifest

class RepositoryCollectionFacadeTest extends FunSpec {
  describe("updateRepositories") {
    it("should ...") {
      val dir = Files.createTempDirectory("material")

      val repositories = mock(classOf[MongoRepositoryDao])
      val components = mock(classOf[MongoComponentDao])
      val thumbs =  mock(classOf[MongoThumbnailDao])
      val services = new ServiceBundle(repositories, components, thumbs)
      val converters = Seq(mock(classOf[ImageConverter]))
      val factories = Seq(MockRepositoryLoaderFactory)
      val emitter = new RepositoryDataEventEmitter()

      val context = RepositoryCollectionFacade.Context((Seq(), Manifest(Seq(), Seq())), (Seq(), RepositoryConfig(List())))

      val sut = new RepositoryCollectionFacade(context, services, converters, factories, emitter)
      sut.updateRepositories()
    }
  }
}
