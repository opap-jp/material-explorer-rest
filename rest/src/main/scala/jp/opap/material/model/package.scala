package jp.opap.material

import java.util.UUID

import jp.opap.data.yaml.Leaf.EmptyNode
import jp.opap.data.yaml.Parent.MappingParent
import jp.opap.data.yaml.{Node, Yaml, YamlException}
import jp.opap.data.yaml.YamlException.TypeException
import jp.opap.material.model.Warning.GlobalWarning

package object model {
  val WARNING_NODE_TYPE_INVALID: String = "%1$s - 要素の型が不正です。"
  val WARNING_KEY_REQUIRED: String = "%1$s が必要です。"

  def withWarning[T](supplier: => T): Either[GlobalWarning, T] = {
    try {
      Yaml {
        supplier
      }.left.map {
        e: YamlException => toWarning(e)
      }
    } catch {
      case e: GlobalException => Left(toWarning(e))
    }
  }

  def withWarnings[T](supplier: => (Seq[GlobalWarning], T)): (Seq[GlobalWarning], Option[T]) = {
    try {
      val s = supplier
      s._1 -> Option(s._2)
    } catch {
      case e: YamlException => List(toWarning(e)) -> None
      case e: GlobalException => List(toWarning(e)) -> None
    }
  }

  private def toWarning(exception: YamlException): GlobalWarning = {
    def nonMapping(node: Node) = {
      GlobalWarning(UUID.randomUUID(), s"${node.location}: ${WARNING_NODE_TYPE_INVALID.format(node.getClass.getSimpleName)}", None)
    }

    exception match {
      case TypeException(node: EmptyNode[_]) =>
        node.parent match {
          case MappingParent(parentNode, key) => GlobalWarning(UUID.randomUUID(), s"${parentNode.location}: ${WARNING_KEY_REQUIRED.format(key)}", None)
          case _ => nonMapping(node)
        }
      case TypeException(node) => nonMapping(node)
    }
  }

  private def toWarning(e: GlobalException): GlobalWarning = {
    val location = e.focus.map(l => s"${l.location}: ").getOrElse("")
    GlobalWarning(UUID.randomUUID(), location + e.message, None)
  }

  case class GlobalException(message: String, focus: Option[Node]) extends RuntimeException
}
