package jp.opap.material.model

import java.util.UUID

import jp.opap.data.yaml.Node
import jp.opap.material.data.Collections.{EitherSeq, Seqs}
import jp.opap.material.model.RepositoryConfig.RepositoryInfo
import jp.opap.material.model.Warning.GlobalWarning

import scala.util.matching.Regex

case class RepositoryConfig(repositories: List[RepositoryInfo])

object RepositoryConfig {

  val PATTERN_ID: Regex = "^[a-z0-9_-]+$".r

  val WARNING_INVALID_ID: String = "%1$s - このIDは不正です。IDは、 /^[a-z0-9_-]+$/ でなければなりません。"
  val WARNING_NO_SUCH_PROTOCOL: String = "%1$s - そのような取得方式はありません。"
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

  def fromYaml(document: Node): (List[GlobalWarning], RepositoryConfig) = {
    def extractItem(node: Node): Either[GlobalWarning, RepositoryInfo] = {
      withWarning {
        val id = node("id").string.get.toLowerCase
        if (PATTERN_ID.findFirstIn(id).isEmpty)
          throw GlobalException(WARNING_INVALID_ID.format(id), Option(node))
        node("protocol").string.get match {
          case "gitlab" =>
            GitlabRepositoryInfo(id, node("title").string.get, node("host").string.get, node("namespace").string.get, node("name").string.get)
          case protocol => throw GlobalException(WARNING_NO_SUCH_PROTOCOL.format(protocol), Option(node))
        }
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

    val repositories = withWarnings {
      val items = document("repositories").list.map(extractItem).toList
      items.left -> RepositoryConfig(items.right.toList)
    }

    validate(repositories._1.toList, repositories._2.getOrElse(RepositoryConfig(List())))
  }

}
