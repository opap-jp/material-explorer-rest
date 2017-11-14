package jp.opap.material.model

import scala.beans.BeanProperty

case class Item(@BeanProperty  projectId: String, @BeanProperty path: String)
