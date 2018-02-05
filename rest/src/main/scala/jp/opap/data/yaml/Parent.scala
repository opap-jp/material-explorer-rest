package jp.opap.data.yaml

import jp.opap.data.yaml.InternalNode.{ListNode, MappingNode}

sealed trait Parent

object Parent {
  trait ConcreteParent extends Parent {
    val node: Node
  }

  case class EmptyParent() extends Parent
  case class MappingParent(node: MappingNode, key: String) extends ConcreteParent
  case class ListParent(node: ListNode, index: Int) extends ConcreteParent
}
