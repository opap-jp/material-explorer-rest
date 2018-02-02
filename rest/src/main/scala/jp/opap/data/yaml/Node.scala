package jp.opap.data.yaml

import java.math.BigInteger
import java.time.LocalDateTime

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
  def bigInteger: Leaf[BigInteger] = throw TypeException(this)
  def double: Leaf[Double] = throw TypeException(this)
  def date: Leaf[LocalDateTime] = throw TypeException(this)

  def ancestors: List[ConcreteParent] = {
    @tailrec
    def a(parent: Parent, accumulator: List[ConcreteParent]): List[ConcreteParent] = parent match {
      case EmptyParent() => accumulator
      case p: ConcreteParent => a(p.node.parent, p :: accumulator)
    }
    a(this.parent, List())
  }

  lazy val location: String = {
    this.ancestors.map {
      case MappingParent(_, key) => "[\"" + key + "\"]"
      case ListParent(_, index) => s"[$index]"
    }.mkString("")
  }

  override def toString: String = {
    s"${this.location}: ${this.getClass.getName}"
  }
}
