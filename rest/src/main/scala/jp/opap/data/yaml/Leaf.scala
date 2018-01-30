package jp.opap.data.yaml

import java.math.BigInteger
import java.time.LocalDateTime

import jp.opap.data.yaml.YamlException.TypeException

sealed trait Leaf[T] extends Node {
  lazy val get: T = throw TypeException(this)
  lazy val option: Option[T] = throw TypeException(this)
}

object Leaf {
  trait EmptyNode[T] extends Leaf[T] {
    override lazy val option: Option[T] = None
  }

  case class UndefinedNode[T](parent: Parent) extends EmptyNode[T] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class NullNode[T](parent: Parent) extends EmptyNode[T] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class StringNode(content: String, parent: Parent) extends Leaf[String] {
    override lazy val get: String = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class BooleanNode(content: Boolean, parent: Parent) extends Leaf[Boolean] {
    override lazy val get: Boolean = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class IntNode(content: Int, parent: Parent) extends Leaf[Int] {
    override lazy val get: Int = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class LongNode(content: Long, parent: Parent) extends Leaf[Long] {
    override lazy val get: Long = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class BigIntegerNode(content: BigInteger, parent: Parent) extends Leaf[BigInteger] {
    override lazy val get: BigInteger = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class DoubleNode(content: Double, parent: Parent) extends Leaf[Double] {
    override lazy val get: Double = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class DateNode(content: LocalDateTime, parent: Parent) extends Leaf[LocalDateTime] {
    override lazy val get: LocalDateTime = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }
}
