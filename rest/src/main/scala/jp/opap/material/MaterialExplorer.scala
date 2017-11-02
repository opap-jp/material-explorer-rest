package jp.opap.material

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.{Bootstrap, Environment}
import jp.opap.material.resource.RootResource
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.scala.DefaultScalaModule

class AppConfiguration extends Configuration {
}

object MaterialExplorer extends Application[AppConfiguration] {

  def main(args: Array[String]): Unit = {
    this.run(args:_*)
  }

  override def initialize(bootstrap: Bootstrap[AppConfiguration]): Unit = {
  }

  override def run(configuration: AppConfiguration, environment: Environment): Unit = {
    val server = environment.jersey()
    val roots = new RootResource()
    server.register(roots)

    val om = environment.getObjectMapper
    om.registerModule(DefaultScalaModule)
    om.enable(SerializationFeature.INDENT_OUTPUT)
  }
}
