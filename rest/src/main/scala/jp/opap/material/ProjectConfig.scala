package jp.opap.material

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

class ProjectConfig {
  @BeanProperty
  var projects: java.util.List[ProjectInfo] = List().asJava
}

class ProjectInfo {
  @BeanProperty
  var protocol: String = ""

  @BeanProperty
  var name: String = ""

  @BeanProperty
  var title: String = ""

  def id: String = this.protocol + ":" + this.name

  def namespace: String = this.name.split("/")(0)
  def projectName: String = this.name.split("/")(1)
}
