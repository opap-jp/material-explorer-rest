package jp.opap.material

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import org.hibernate.validator.constraints.NotEmpty

class AppConfiguration extends Configuration {
  @NotEmpty
  var dbHost: String = ""

  @JsonProperty
  def getDbHost: String = this.dbHost

  @JsonProperty
  def setDbHost(value: String): Unit = this.dbHost = value
}
