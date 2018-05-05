package jp.opap.material

import java.io.File
import java.util
import java.util.UUID
import javax.servlet.DispatcherType

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import io.dropwizard.Application
import io.dropwizard.jackson.Jackson
import io.dropwizard.setup.{Bootstrap, Environment}
import jp.opap.data.yaml.Yaml
import jp.opap.material.dao.{CacheDao, GridFsCacheDao, MongoComponentDao, MongoRepositoryDao, MongoThumbnailDao}
import jp.opap.material.data.JavaScriptPrettyPrinter.PrettyPrintFilter
import jp.opap.material.data.JsonSerializers.AppSerializerModule
import jp.opap.material.facade.MediaConverter.{ImageConverter, RestResize}
import jp.opap.material.facade.{GitLabRepositoryLoaderFactory, RepositoryCollectionFacade, RepositoryDataEventEmitter}
import jp.opap.material.health.HealthChecks.{ImageMagickHealthCheck, MongoHealthCheck}
import jp.opap.material.model.{Manifest, RepositoryConfig}
import jp.opap.material.resource.RootResource
import org.eclipse.jetty.servlets.CrossOriginFilter

object MaterialExplorer extends Application[AppConfiguration] {
  def main(args: Array[String]): Unit = {
    run(args:_*)
  }

  override def initialize(bootstrap: Bootstrap[AppConfiguration]): Unit = {
    val om = Jackson.newMinimalObjectMapper()
      .registerModule(AppSerializerModule)
    bootstrap.setObjectMapper(om)
  }

  override def run(configuration: AppConfiguration, environment: Environment): Unit = {
    def getServiceBundle: ServiceBundle = {
      val dbClient = new MongoClient(configuration.dbHost)
      val db = dbClient.getDatabase(configuration.dbName)

      val repositoryDao = new MongoRepositoryDao(db)
      val componentDao = new MongoComponentDao(db)
      val thumbnailDao = new MongoThumbnailDao(db)
      val cacheDao = new GridFsCacheDao(db)
      val resize = new RestResize(configuration.imageMagickHost)

      new ServiceBundle(db, repositoryDao, componentDao, thumbnailDao, cacheDao, resize)
    }

    val serviceBundle = getServiceBundle
    val repositoryEventEmitter = new RepositoryDataEventEmitter()

    val rootResource = new RootResource(serviceBundle, repositoryEventEmitter)

    val server = environment.jersey()
    server.register(rootResource)

    val servlets = environment.servlets()

    val healthChecks = environment.healthChecks()
    healthChecks.register("mongodb", new MongoHealthCheck(serviceBundle.db))
    healthChecks.register("imagemagick", new ImageMagickHealthCheck(serviceBundle.resize))

    val cors = servlets.addFilter("CORS", classOf[CrossOriginFilter])
    cors.setInitParameter("allowedOrigins", "*")
    cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin")
    cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD")
    cors.addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/*")

    // JSON Pretty Print
    servlets.addFilter(classOf[PrettyPrintFilter].getSimpleName, PrettyPrintFilter.SINGLETON)
      .addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/*")

    updateRepositoryData(configuration, serviceBundle, repositoryEventEmitter)
      .start()
  }

  def updateRepositoryData(configuration: AppConfiguration, services: ServiceBundle, eventEmitter: RepositoryDataEventEmitter): Thread = {
    val manifest = Manifest.fromYaml(Yaml.parse(new File(configuration.manifest)), UUID.randomUUID)
    val repositories = RepositoryConfig.fromYaml(Yaml.parse(new File(configuration.repositories)))

    val context = RepositoryCollectionFacade.Context(manifest, repositories)
    val converters =Seq(new ImageConverter(services.resize))
    val loaders = Seq(new GitLabRepositoryLoaderFactory(configuration))

    val facade = new RepositoryCollectionFacade(context, services, converters, loaders, configuration, eventEmitter)

    new Thread(()  => facade.updateRepositories())
  }

  class ServiceBundle(
    val db: MongoDatabase,
    val repositoryDao: MongoRepositoryDao,
    val componentDao: MongoComponentDao,
    val thumbnailDao: MongoThumbnailDao,
    val cacheDao: CacheDao,
    val resize: RestResize
  )
}
