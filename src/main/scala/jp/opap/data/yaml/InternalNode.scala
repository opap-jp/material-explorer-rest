package jp.opap.data.yaml

import jp.opap.data.yaml.Leaf.UndefinedNode
import jp.opap.data.yaml.Parent.{ListParent, MappingParent}

sealed trait InternalNode extends Node {
}

object InternalNode {
  class MappingNode(private val children: Map[String, Node], val parent: Parent) extends Iterable[(String, Node)] with InternalNode {
    override def apply(key: String): Node = {
      val parent = MappingParent(this, key)
      if (this.children.contains(key))
        this.children(key).withParent(MappingParent(this, key))
      else
        UndefinedNode(parent)
    }

    override def withParent(parent: Parent): Node = new MappingNode(this.children, parent)

    override def iterator: Iterator[(String, Node)] = this.toMap.iterator
    lazy val toMap: Map[String, Node] = this.children.map(x => x._1 -> x._2.withParent(MappingParent(this, x._1)))

    override def mapping: MappingNode = this
    override def mappingOption: Option[MappingNode] = Option(this)
  }

  class ListNode(private val items: List[Node], val parent: Parent) extends Iterable[Node] with InternalNode  {
    override def withParent(parent: Parent): Node = new ListNode(this.items, parent)

    override def iterator: Iterator[Node] = this.toList.iterator

    override lazy val toList: List[Node] = this.items.zipWithIndex.map(x => x._1.withParent(ListParent(this, x._2)))

    override def list: ListNode = this
    override def listOption: Option[ListNode] = Option(this)
  }
}
