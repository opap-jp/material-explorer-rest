package jp.opap.data.yaml

import jp.opap.data.yaml.Parent.{ListParent, MappingParent}

sealed trait InternalNode extends Node {
}

object InternalNode {
  class MappingNode(private val children: Map[String, Node], val parent: Parent) extends InternalNode with Iterable[(String, Node)] {
    override def apply(key: String): Node = {
      this.children(key).withParent(MappingParent(this, key))
    }

    override def withParent(parent: Parent): Node = new MappingNode(this.children, parent)

    override def iterator: Iterator[(String, Node)] = this.toMap.iterator

    lazy val toMap: Map[String, Node] = this.children.map(x => x._1 -> x._2.withParent(MappingParent(this, x._1)))
  }

  class ListNode(private val items: List[Node], val parent: Parent) extends InternalNode with Iterable[Node] {
    override def withParent(parent: Parent): Node = new ListNode(this.items, parent)

    override def iterator: Iterator[Node] = this.toList.iterator

    override lazy val toList: List[Node] = this.items.zipWithIndex.map(x => x._1.withParent(ListParent(this, x._2)))
  }
}
