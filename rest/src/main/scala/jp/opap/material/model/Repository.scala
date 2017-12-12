package jp.opap.material.model

import scala.beans.BeanProperty

case class Repository(@BeanProperty id: String, @BeanProperty name: String, @BeanProperty title: String, @BeanProperty headHash: String)
