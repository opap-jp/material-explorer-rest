package jp.opap.data.yaml

import jp.opap.data.yaml.InternalNode.{ListNode, MappingNode}
import jp.opap.data.yaml.YamlException.TypeException

trait Node {
  val parent: Parent
  protected[yaml] def withParent(parent: Parent): Node

  def apply(key: String): Node = throw TypeException(this)
  def apply(i: Int): Node = throw TypeException(this)

  def asMap[T](mapper: MappingNode => T): T = throw TypeException(this)
  def asList[T](mapper: ListNode => T): T = throw TypeException(this)
  def asSingleOrList[T](converter: Node => T): List[T] = throw TypeException(this)

  def mapping: MappingNode = throw TypeException(this)
  def list: ListNode = throw TypeException(this)

  def string: Leaf[String] = throw TypeException(this)
}
