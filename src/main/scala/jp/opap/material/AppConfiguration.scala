package jp.opap.material

import io.dropwizard.Configuration
import org.hibernate.validator.constraints.NotEmpty

import scala.beans.BeanProperty

class AppConfiguration extends Configuration {
  @BeanProperty
  @NotEmpty
  var dbHost: String = _

  @BeanProperty
  @NotEmpty
  var dbName: String = _

  @BeanProperty
  @NotEmpty
  var repositories: String = _

  @BeanProperty
  @NotEmpty
  var manifest: String = _

  @BeanProperty
  @NotEmpty
  var repositoryStore: String = _

  @BeanProperty
  @NotEmpty
  var imageMagickHost: String = _

  @BeanProperty
  @NotEmpty
  var metadataFileName: String = "material-explorer.yaml"
}
