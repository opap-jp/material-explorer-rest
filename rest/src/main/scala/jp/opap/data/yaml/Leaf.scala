package jp.opap.data.yaml

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
}
