package jp.opap.material

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

class RepositoryConfig {
  @BeanProperty
  var repositories: java.util.List[RepositoryInfo] = List().asJava
}

class RepositoryInfo {
  @BeanProperty
  var id: String = ""

  @BeanProperty
  var title: String = ""

  @BeanProperty
  var url: String = ""
}
