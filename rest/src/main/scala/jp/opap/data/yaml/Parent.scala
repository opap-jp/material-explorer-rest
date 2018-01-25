package jp.opap.data.yaml

sealed trait Parent

object Parent {
  case class MapParent(node: Node, key: String) extends Parent
  case class ListParent(node: Node, index: Int) extends Parent
  case class EmptyParent() extends Parent
}
