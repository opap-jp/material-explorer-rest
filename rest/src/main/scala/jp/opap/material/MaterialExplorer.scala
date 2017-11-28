package jp.opap.material

import java.util
import javax.servlet.DispatcherType

import com.mongodb.MongoClient
import io.dropwizard.Application
import io.dropwizard.jackson.Jackson
import io.dropwizard.setup.{Bootstrap, Environment}
import jp.opap.material.dao.{MongoComponentDao, MongoItemDao, MongoRepositoryDao, MongoThumbnailDao}
import jp.opap.material.data.JavaScriptPrettyPrinter.PrettyPrintFilter
import jp.opap.material.data.JsonSerializers.AppSerializerModule
import jp.opap.material.facade.{RepositoryCollectionFacade, RepositoryDataEventEmitter}
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
    val dbClient = new MongoClient(configuration.dbHost)
    val db = dbClient.getDatabase("material_explorer")

    val repositoryEventEmitter = new RepositoryDataEventEmitter()

    val repositoryDao = new MongoRepositoryDao(db)
    val itemDao = new MongoItemDao(db)
    val componentDao = new MongoComponentDao(db)
    val thumbnailDao = new MongoThumbnailDao(db)
    val repositoryCollectionFacade = new RepositoryCollectionFacade(configuration, repositoryDao, componentDao, thumbnailDao, repositoryEventEmitter)

    val rootResource = new RootResource(repositoryDao, itemDao, componentDao, thumbnailDao, repositoryEventEmitter)

    val pattern = "\\.([a-zA-Z0-9]+)$".r
    val server = environment.jersey()
    server.register(rootResource)

    val servlets = environment.servlets()

    val cors = servlets.addFilter("CORS", classOf[CrossOriginFilter])
    cors.setInitParameter("allowedOrigins", "*")
    cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin")
    cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD")
    cors.addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/*")

    // JSON Pretty Print
    servlets.addFilter(classOf[PrettyPrintFilter].getSimpleName, PrettyPrintFilter.SINGLETON)
      .addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/*")

    new Thread() {
      override def run(): Unit = {
        MaterialExplorer.this.updateRepositoryData(repositoryCollectionFacade, configuration)
      }
    }.start()
  }

  def updateRepositoryData(facade: RepositoryCollectionFacade, configuration: AppConfiguration): Unit = {
    facade.updateRepositories(configuration)
  }
}
