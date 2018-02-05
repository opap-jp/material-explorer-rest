package jp.opap.data.yaml

import java.time.Instant

import jp.opap.data.yaml.YamlException.TypeException

sealed trait Leaf[T] extends Node {
  lazy val get: T = throw TypeException(this)
  lazy val option: Option[T] = throw TypeException(this)
}

object Leaf {
  trait EmptyNode[T] extends Leaf[T] {
    override lazy val option: Option[T] = None
    protected def adjust[U]: Leaf[U]

    override def string: Leaf[String] = this.adjust
    override def boolean: Leaf[Boolean] = this.adjust
    override def int: Leaf[Int] = this.adjust
    override def long: Leaf[Long] = this.adjust
    override def bigInteger: Leaf[BigInt] = this.adjust
    override def double: Leaf[Double] = this.adjust
    override def date: Leaf[Instant] = this.adjust

    override def mappingOption: Option[InternalNode.MappingNode] = None

    override def listOption: Option[InternalNode.ListNode] = None
  }

  trait ValueLeaf[T] extends Leaf[T] {
    val content: T

    override def toString: String = s"${this.location}: ${this.getClass.getSimpleName}(${this.content.toString})"
  }

  case class UndefinedNode[T](parent: Parent) extends EmptyNode[T] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    protected def adjust[U]: EmptyNode[U] = this.copy(this.parent)
  }

  case class NullNode[T](parent: Parent) extends EmptyNode[T] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    protected def adjust[U]: EmptyNode[U] = this.copy()
  }

  case class StringNode(content: String, parent: Parent) extends ValueLeaf[String] {
    override lazy val get: String = this.content
    override lazy val option: Option[String] = Option(this.content)
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def string: Leaf[String] = this
    override def toString: String = this.location + ": " + this.getClass.getSimpleName + "(\"" + this.content.toString + "\")"
  }

  case class BooleanNode(content: Boolean, parent: Parent) extends ValueLeaf[Boolean] {
    override lazy val get: Boolean = this.content
    override lazy val option: Option[Boolean] = Option(this.content)
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def boolean: Leaf[Boolean] = this
  }

  case class IntNode(content: Int, parent: Parent) extends ValueLeaf[Int] {
    override lazy val get: Int = this.content
    override lazy val option: Option[Int] = Option(this.content)
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def int: Leaf[Int] = this
  }

  case class LongNode(content: Long, parent: Parent) extends ValueLeaf[Long] {
    override lazy val get: Long = this.content
    override lazy val option: Option[Long] = Option(this.content)
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def long: Leaf[Long] = this
  }

  case class BigIntegerNode(content: BigInt, parent: Parent) extends ValueLeaf[BigInt] {
    override lazy val get: BigInt = this.content
    override lazy val option: Option[BigInt] = Option(this.content)
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def bigInteger: Leaf[BigInt] = this
  }

  case class DoubleNode(content: Double, parent: Parent) extends ValueLeaf[Double] {
    override lazy val get: Double = this.content
    override lazy val option: Option[Double] = Option(this.content)
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def double: Leaf[Double] = this
  }

  case class DateNode(content: Instant, parent: Parent) extends ValueLeaf[Instant] {
    override lazy val get: Instant = this.content
    override lazy val option: Option[Instant] = Option(this.content)
    override def withParent(parent: Parent): Node = this.copy(parent = parent)

    override def date: Leaf[Instant] = this
  }
}
