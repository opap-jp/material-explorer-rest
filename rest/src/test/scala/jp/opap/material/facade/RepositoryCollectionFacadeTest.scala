package jp.opap.material.facade

import java.nio.file.Files

import com.mongodb.client.MongoDatabase
import jp.opap.material.AppConfiguration
import jp.opap.material.MaterialExplorer.ServiceBundle
import jp.opap.material.dao.{GridFsCacheDao, MongoComponentDao, MongoRepositoryDao, MongoThumbnailDao}
import jp.opap.material.facade.MediaConverter.ImageConverter
import jp.opap.material.model.RepositoryConfig
import org.mockito.Mockito.mock
import org.scalatest.FunSpec
import jp.opap.material.model.Manifest

class RepositoryCollectionFacadeTest extends FunSpec {
  describe("updateRepositories") {
    ignore("should ...") {
      val dir = Files.createTempDirectory("material")

      val db = mock(classOf[MongoDatabase])
      val repositories = mock(classOf[MongoRepositoryDao])
      val components = mock(classOf[MongoComponentDao])
      val thumbs =  mock(classOf[MongoThumbnailDao])
      val caches = mock(classOf[GridFsCacheDao])

      val services = new ServiceBundle(db, repositories, components, thumbs, caches)
      val converters = Seq(mock(classOf[ImageConverter]))
      val factories = Seq(MockRepositoryLoaderFactory)
      val emitter = new RepositoryDataEventEmitter()

      val context = RepositoryCollectionFacade.Context((Seq(), Manifest(Seq(), Seq())), (Seq(), RepositoryConfig(List())))

      val sut = new RepositoryCollectionFacade(context, services, converters, factories, new AppConfiguration(), emitter)
      sut.updateRepositories()
    }
  }
}
