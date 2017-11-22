package jp.opap.material

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

class ProjectConfig {
  @BeanProperty
  var projects: java.util.List[ProjectInfo] = List().asJava
}

class ProjectInfo {
  @BeanProperty
  var id: String = ""

  @BeanProperty
  var title: String = ""

  @BeanProperty
  var url: String = ""
}
