package jp.opap.material

import java.io.File
import java.util.UUID

import jp.opap.material.RepositoryConfig.RepositoryInfo
import jp.opap.material.data.Yaml
import jp.opap.material.data.Yaml.{EntryException, ListNode, MapNode}
import jp.opap.material.model.Warning.GlobalWarning
import jp.opap.material.data.Collections.EitherList

case class RepositoryConfig(repositories: List[RepositoryInfo])

object RepositoryConfig {

  /**
    * 取得するリポジトリの情報を表現するクラスです。
    */
  sealed trait RepositoryInfo {

    /**
      * システム内での、このリポジトリの識別子です。ファイル名として正しい文字列でなければなりません。
      */
    val id: String

    /**
      * このリポジトリの名称です。ウェブページ上で表示されます。
      */
    val title: String
  }

  /**
    * GitLab で取得可能なリポジトリの情報を表現するクラスです。
    *
    * @param host GitLab をホスティングしているサーバーの URL
    * @param namespace GitLab リポジトリの namespace
    * @param name GitLab リポジトリの name
    */
  case class GitlabRepositoryInfo(id: String, title: String, host: String, namespace: String, name: String) extends RepositoryInfo

  def fromYaml(file: File): (List[GlobalWarning], RepositoryConfig) = {
    def item(element: (Any, Int)): Either[GlobalWarning, RepositoryInfo] = {
      val (node, i) = element
      try {
        node match {
          case item: MapNode => item("protocol").get match {
            case "gitlab" => Right(GitlabRepositoryInfo(item("id").string, item("title").string, item("host").string, item("namespace").string, item("name").string))
            case _ => throw EntryException("protocol が必要です。")
          }
          case _ => throw EntryException("要素の型が不正です。")
        }
      } catch {
        case e: EntryException => Left(new GlobalWarning(UUID.randomUUID(), s"repositories[$i]: ${e.message}"))
      }
    }

    try {
      // TODO: リポジトリ ID のバリデーション。（重複排除とパターン）
      val (warnings, repositories) = (Yaml.parse(file) match {
        case MapNode(root) => root.get("repositories") match {
          case Some(ListNode(items)) => items.zipWithIndex.map(item)
          case _ => List(Left(new GlobalWarning(UUID.randomUUID(), "repositories が必要です。")))
        }
        case _ => List(Left(new GlobalWarning(UUID.randomUUID(), "repositories が必要です。")))
      }).leftRight

      (warnings, RepositoryConfig(repositories))
    } catch {
      case e: Exception =>
        val warning = GlobalWarning(UUID.randomUUID(), "リポジトリ設定の取得に失敗しました。", Option(e.getMessage))
        (List(warning),  RepositoryConfig(List()))
    }
  }
}
