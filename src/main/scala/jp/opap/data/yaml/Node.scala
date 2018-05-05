package jp.opap.data.yaml

import java.time.Instant

import jp.opap.data.yaml.InternalNode.{ListNode, MappingNode}
import jp.opap.data.yaml.Parent.{ConcreteParent, EmptyParent, ListParent, MappingParent}
import jp.opap.data.yaml.YamlException.TypeException

import scala.annotation.tailrec

trait Node {
  val parent: Parent
  protected[yaml] def withParent(parent: Parent): Node

  def apply(key: String): Node = throw TypeException(this)

  def mapping: MappingNode = throw TypeException(this)
  def list: ListNode = throw TypeException(this)

  def mappingOption: Option[MappingNode] = throw TypeException(this)
  def listOption: Option[ListNode] = throw TypeException(this)

  def string: Leaf[String] = throw TypeException(this)
  def boolean: Leaf[Boolean] = throw TypeException(this)
  def int: Leaf[Int] = throw TypeException(this)
  def long: Leaf[Long] = throw TypeException(this)
  def bigInteger: Leaf[BigInt] = throw TypeException(this)
  def double: Leaf[Double] = throw TypeException(this)
  def date: Leaf[Instant] = throw TypeException(this)

  def ancestors: List[ConcreteParent] = {
    @tailrec
    def a(parent: Parent, accumulator: List[ConcreteParent]): List[ConcreteParent] = parent match {
      case EmptyParent() => accumulator
      case p: ConcreteParent => a(p.node.parent, p :: accumulator)
    }
    a(this.parent, List())
  }

  lazy val location: String = {
    @tailrec
    def l(parent: Parent, previous: Option[Parent], portions: String): String = {
      parent match {
        case EmptyParent() =>
          previous match {
            case Some(_: ListParent) => "/" + portions
            case _ => portions
          }
        case MappingParent(node, key) => l(node.parent, Option(parent), "/" + key + portions)
        case ListParent(node, index) => l(node.parent, Option(parent), "[" + index + "]" + portions)
      }
    }
    l(this.parent, None, "")
  }

  override def toString: String = {
    s"${this.location}: ${this.getClass.getSimpleName}"
  }
}
