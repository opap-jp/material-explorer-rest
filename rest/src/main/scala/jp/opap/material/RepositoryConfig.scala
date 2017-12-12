package jp.opap.material

import java.io.File

import jp.opap.material.RepositoryConfig.RepositoryInfo
import jp.opap.material.data.Yaml
import jp.opap.material.data.Yaml.{ListNode, MapNode}

case class RepositoryConfig(repositories: List[RepositoryInfo])

object RepositoryConfig {
  sealed trait RepositoryInfo {
    val id: String
    val title: String
  }

  case class GitlabRepositoryInfo(id: String, title: String, namespace: String, name: String) extends RepositoryInfo

  def fromYaml(file: File): List[RepositoryInfo] = {
    def item(node: AnyRef): Option[RepositoryInfo] = {
      val e: NoSuchElementException = null
      node match {
        case MapNode(item) => item.get("protocol")
          .flatMap(protocol => protocol match {
            case "gitlab" => Option(GitlabRepositoryInfo(item("id").asInstanceOf[String], item("title").asInstanceOf[String], item("namespace").asInstanceOf[String], item("name").asInstanceOf[String]))
            case _ => Option.empty
          })
        case _ => Option.empty
      }
    }

    // TODO: YAML が妥当でないとき、エラーを返す。
    Yaml.parse(file) match {
      case MapNode(root) => root.get("repositories") match {
        case Some(ListNode(items)) => items.flatMap(item)
        case _ => List()
      }
      case _ => List()
    }
  }
}
