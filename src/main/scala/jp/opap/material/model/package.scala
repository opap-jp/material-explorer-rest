package jp.opap.material

import java.util.UUID

import jp.opap.data.yaml.Leaf.EmptyNode
import jp.opap.data.yaml.Parent.MappingParent
import jp.opap.data.yaml.YamlException.{TypeException, UnsupportedMappingKeyException}
import jp.opap.data.yaml.{Node, Yaml, YamlException}
import jp.opap.material.model.Warning.{ComponentWarning, GlobalWarning, RepositoryWarning}

package object model {
  val WARNING_NODE_TYPE_INVALID: String = "%1$s - 要素の型が不正です。"
  val WARNING_KEY_REQUIRED: String = "%1$s が必要です。"
  val WARNING_UNSUPPORTED_MAPPING_KEY: String = "対応していない形式のキーを持つマッピングがあります。"

  def withWarning[T](context: ExceptionContext)(supplier: => T): Either[Warning, T] = {
    try {
      Yaml {
        supplier
      }.left.map {
        e: YamlException => messageFromYaml(e, context)
      }
    } catch {
      case e: DeserializationException => Left(messageFromDeserialization(e, context))
    }
  }

  def withWarnings[T](context: ExceptionContext)(supplier: => (Seq[Warning], T)): (Seq[Warning], Option[T]) = {
    try {
      val s = supplier
      s._1 -> Option(s._2)
    } catch {
      case e: YamlException => List(messageFromYaml(e, context)) -> None
      case e: DeserializationException => List(messageFromDeserialization(e, context)) -> None
    }
  }

  private def messageFromYaml(exception: YamlException, context: ExceptionContext): Warning = {
    val nonMapping =
      (node: Node) => s"${node.location}: ${WARNING_NODE_TYPE_INVALID.format(node.getClass.getSimpleName)}"

    val message = exception match {
      case TypeException(node: EmptyNode[_]) =>
        node.parent match {
          case MappingParent(parentNode, key) => s"${parentNode.location}: ${WARNING_KEY_REQUIRED.format(key)}"
          case _ => nonMapping(node)
        }
      case TypeException(node) => nonMapping(node)
      case UnsupportedMappingKeyException(_, _) => WARNING_UNSUPPORTED_MAPPING_KEY
    }
    toWarning(context, message, UUID.randomUUID)
  }

  private def messageFromDeserialization(e: DeserializationException, context: ExceptionContext): Warning = {
    val location = e.focus.map(l => s"${l.location}: ").getOrElse("")
    val message = location + e.message
    toWarning(context, message, UUID.randomUUID)
  }

  private def toWarning(context: ExceptionContext, message: String, idGenerator: () => UUID): Warning = {
    val id = idGenerator()
    context match {
      case GlobalContext => GlobalWarning(id, message, None)
      case RepositoryContext(repositoryId) => RepositoryWarning(id, message, None, repositoryId)
      case ComponentContext(componentId) => ComponentWarning(id, message, None, componentId)
    }
  }

  /**
    * デシリアライズで発生する、特定の要素（グローバル，リポジトリ，コンポーネント）に関連づけられるべき例外です。
    *
    * @param message エラーメッセージ
    * @param focus この例外の原因となったノード（任意）
    */
  case class DeserializationException(message: String, focus: Option[Node]) extends RuntimeException

  sealed trait ExceptionContext
  case object GlobalContext extends ExceptionContext
  case class RepositoryContext(id: String) extends ExceptionContext
  case class ComponentContext(id: UUID) extends ExceptionContext
}
