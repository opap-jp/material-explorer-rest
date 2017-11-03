package jp.opap.material

import java.util

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.{Bootstrap, Environment}
import jp.opap.material.resource.RootResource
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.eclipse.jetty.servlets.CrossOriginFilter
import javax.servlet.DispatcherType

class AppConfiguration extends Configuration {
}

object MaterialExplorer extends Application[AppConfiguration] {

  def main(args: Array[String]): Unit = {
    this.run(args:_*)
  }

  override def initialize(bootstrap: Bootstrap[AppConfiguration]): Unit = {
  }

  override def run(configuration: AppConfiguration, environment: Environment): Unit = {
    val cors = environment.servlets.addFilter("CORS", classOf[CrossOriginFilter])
    cors.setInitParameter("allowedOrigins", "*")
    cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin")
    cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD")
    cors.addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/*")

    val server = environment.jersey()
    val roots = new RootResource()
    server.register(roots)

    val om = environment.getObjectMapper
    om.registerModule(DefaultScalaModule)
    om.enable(SerializationFeature.INDENT_OUTPUT)
  }
}
