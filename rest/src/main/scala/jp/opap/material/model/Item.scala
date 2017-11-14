package jp.opap.material.model

import scala.beans.BeanProperty

case class Item(@BeanProperty projectId: String, @BeanProperty itemType: ItemType, @BeanProperty path: String)

sealed trait ItemType {
  override def toString: String = this match {
    case Commit => "commit"
    case Tree => "tree "
    case Blob => "blob"
  }
}

case object Commit extends ItemType
case object Tree extends ItemType
case object Blob extends ItemType

object ItemType {
  def valueOf(s: String): ItemType = s match {
    case "commit" => Commit
    case "tree" => Tree
    case "blob" => Blob
  }
}
