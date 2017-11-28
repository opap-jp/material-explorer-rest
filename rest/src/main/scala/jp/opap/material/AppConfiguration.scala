package jp.opap.material

import io.dropwizard.Configuration
import org.hibernate.validator.constraints.NotEmpty

import scala.beans.BeanProperty

class AppConfiguration extends Configuration {
  @BeanProperty
  @NotEmpty
  var dbHost: String = ""

  @BeanProperty
  @NotEmpty
  var repositories: String = ""

  @BeanProperty
  @NotEmpty
  var repositoryStore: String = ""

  @BeanProperty
  @NotEmpty
  var imageMagickHost: String = ""
}
