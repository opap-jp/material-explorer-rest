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
    protected def parameterize[U]: Leaf[U]

    override def string: Leaf[String] = this.parameterize
    override def boolean: Leaf[Boolean] = this.parameterize
    override def int: Leaf[Int] = this.parameterize
    override def long: Leaf[Long] = this.parameterize
    override def bigInteger: Leaf[BigInteger] = this.parameterize
    override def double: Leaf[Double] = this.parameterize
    override def date: Leaf[LocalDateTime] = this.parameterize
  }

  trait ValueLeaf[T] extends Leaf[T] {
    val content: T

    override def toString: String = this.content.toString
  }

  case class UndefinedNode[T](parent: Parent) extends EmptyNode[T] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    protected def parameterize[U]: EmptyNode[U] = this.copy(this.parent)
  }

  case class NullNode[T](parent: Parent) extends EmptyNode[T] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    protected def parameterize[U]: EmptyNode[U] = this.copy(this.parent)
  }

  case class StringNode(content: String, parent: Parent) extends ValueLeaf[String] {
    override lazy val get: String = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def string: Leaf[String] = this

    override def toString: String = "\"" + this.content + "\""
  }

  case class BooleanNode(content: Boolean, parent: Parent) extends ValueLeaf[Boolean] {
    override lazy val get: Boolean = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def boolean: Leaf[Boolean] = this
  }

  case class IntNode(content: Int, parent: Parent) extends ValueLeaf[Int] {
    override lazy val get: Int = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def int: Leaf[Int] = this
  }

  case class LongNode(content: Long, parent: Parent) extends ValueLeaf[Long] {
    override lazy val get: Long = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def long: Leaf[Long] = super.long
  }

  case class BigIntegerNode(content: BigInteger, parent: Parent) extends ValueLeaf[BigInteger] {
    override lazy val get: BigInteger = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def bigInteger: Leaf[BigInteger] = this
  }

  case class DoubleNode(content: Double, parent: Parent) extends ValueLeaf[Double] {
    override lazy val get: Double = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def double: Leaf[Double] = this
  }

  case class DateNode(content: LocalDateTime, parent: Parent) extends ValueLeaf[LocalDateTime] {
    override lazy val get: LocalDateTime = this.content
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def date: Leaf[LocalDateTime] = this
  }
}
