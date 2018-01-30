package jp.opap.data.yaml

import java.math.BigInteger
import java.time.LocalDateTime

import jp.opap.data.yaml.YamlException.TypeException

sealed trait Leaf[T] extends Node {
  lazy val get: T = throw TypeException(this)
  lazy val option: Option[T] = throw TypeException(this)
}

object Leaf {
  case class StringNode(content: String, parent: Parent) extends Leaf[String] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class UndefinedNode(parent: Parent) extends Leaf[Nothing] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class NullNode(parent: Parent) extends Leaf[Unit] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class BooleanNode(content: Boolean, parent: Parent) extends Leaf[Boolean] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class IntNode(content: Int, parent: Parent) extends Leaf[Int] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class LongNode(content: Long, parent: Parent) extends Leaf[Long] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class BigIntegerNode(content: BigInteger, parent: Parent) extends Leaf[BigInteger] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class DoubleNode(content: Double, parent: Parent) extends Leaf[Double] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }

  case class DateNode(content: LocalDateTime, parent: Parent) extends Leaf[LocalDateTime] {
    override def withParent(parent: Parent): Node = this.copy(parent = parent)
  }
}
