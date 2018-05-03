package jp.opap.material.model

import scala.beans.BeanProperty

case class Thumbnail(@BeanProperty id: String, @BeanProperty width: Int, @BeanProperty height: Int)
