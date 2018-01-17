package jp.opap.material.model

import java.io.{File, IOException}
import java.util.UUID

import jp.opap.material.data.Collections.{EitherList, Seqs}
import jp.opap.material.data.Yaml
import jp.opap.material.data.Yaml.{EntryException, ListNode, MapNode}
import jp.opap.material.model.RepositoryConfig.RepositoryInfo
import jp.opap.material.model.Warning.GlobalWarning

import scala.util.matching.Regex

case class RepositoryConfig(repositories: List[RepositoryInfo])

object RepositoryConfig {

  val PATTERN_ID: Regex = "^[a-z0-9_-]+$".r

  val WARNING_INVALID_ID: String = "%1$s - このIDは不正です。IDは、 /^[a-z0-9_-]+$/ でなければなりません。"
  val WARNING_DUPLICATED_ID: String = "%1$s - このIDは重複しています。"

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
          case item: MapNode =>
            val id = item("id").string.toLowerCase
            if (PATTERN_ID.findFirstIn(id).isEmpty)
              throw EntryException(WARNING_INVALID_ID.format(id))

            item("protocol").get match {
              case "gitlab" => Right(GitlabRepositoryInfo(id, item("title").string, item("host").string, item("namespace").string, item("name").string))
              case _ => throw EntryException("protocol が必要です。")
            }
          case _ => throw EntryException("要素の型が不正です。")
        }
      } catch {
        case e: EntryException => Left(new GlobalWarning(UUID.randomUUID(), s"repositories[$i]: ${e.message}"))
      }
    }

    def validate(warnings: List[GlobalWarning], config: RepositoryConfig): (List[GlobalWarning], RepositoryConfig) = {
      val duplications = config.repositories.groupByOrdered(info => info.id)
        .filter(entry => entry._2.size > 1)
      val duplicationSet = duplications.map(_._1).toSet

      val c = config.copy(repositories = config.repositories.filter(r => !duplicationSet.contains(r.id)))
      val w = duplications.map(entry => new GlobalWarning(UUID.randomUUID(), WARNING_DUPLICATED_ID.format(entry._1)))
      (warnings ++ w, c)
    }

    try {
      val (warnings, repositories) = (Yaml.parse(file) match {
        case MapNode(root) => root.get("repositories") match {
          case Some(ListNode(items)) => items.zipWithIndex.map(item)
          case _ => List(Left(new GlobalWarning(UUID.randomUUID(), "repositories が必要です。")))
        }
        case _ => List(Left(new GlobalWarning(UUID.randomUUID(), "repositories が必要です。")))
      }).leftRight

      validate(warnings, RepositoryConfig(repositories))
    } catch {
      case e: IOException =>
        val warning = GlobalWarning(UUID.randomUUID(), "リポジトリ設定ファイルの取得に失敗しました。", Option(e.getMessage))
        (List(warning),  RepositoryConfig(List()))
    }
  }
}
