package jp.opap.material.model

import java.util.UUID

import jp.opap.data.yaml.Node
import jp.opap.material.model.MetadataBundle.AttachedMetadata

/**
  * 特定のフォーマット（YAMLなど）で記述され、リポジトリのいろいろな場所にファイルとして配置されることを前提とする、
  * ファイルやディレクトリに関連づけられるメタデータの集まりです。
  * このファイルの名称は AppConfiguration#metadataFileName で設定されます。
  * メタデータファイルとして存在する一つのファイルから、一つの MetadataBundle が生成されます。
  */
case class MetadataBundle(
  descendants: Option[AttachedMetadata],
  directory: Option[AttachedMetadata],
  items: Map[String, AttachedMetadata],
)

object MetadataBundle {
  val WARNING_FAILED_TO_LOAD: String = "メタデータファイルの取得に失敗しました。"
  val WARNING_NO_SUCH_MODE_EXISTS: String = "%1$s - そのようなモードはありません。"
  val WARNING_INVALID_KEY_NAME: String = "%1$s - メタデータに対するキーとして不正な文字列です。"

  /**
    * @param root パースされた YAML データのルート要素
    * @param context YAML ファイルを指すコンテキスト
    * @param idGenerator ID ジェネレータ―
    * @return
    */
  def fromYaml(root: Node, context: ComponentContext, idGenerator: () => UUID): (Seq[Warning], MetadataBundle) = {
    def extractMetadata(key: String, node: Node): (Seq[Warning], Option[AttachedMetadata]) = withWarnings(context) {
      ???
    }

    ???
  }

  /**
    * 名前（ファイルやディレクトリ）に対して直接的に設定されたメタデータです。メタデータファイルから取得されます。
    */
  case class AttachedMetadata(mode: Mode, tags: Seq[String])

  /**
    * メタデータのスコープ。メタデータがどのような範囲で有効かを表します。
    * @deprecated
    */
  sealed trait Scope

  /**
    * @deprecated
    */
  object Scope {

    def parse(value: String): Scope = value match {
      case "." => DirectoryScope
      case "*" => RecursiveScope
      case x => FileScope(x)
    }

    /**
      * 単一のファイルに対して有効です。
      * @param name ファイル名
      */
    case class FileScope(name: String) extends Scope

    /**
      * ディレクトリにあるファイルに対して有効です。
      * サブディレクトリのファイルには有効ではありません。
      */
    case object DirectoryScope extends Scope

    /**
      * このディレクトリ以下で、再帰的にすべてのファイルに対して有効です。
      */
    case object RecursiveScope extends Scope
  }

  /**
    * メタデータのモード。親ディレクトリで設定されたメタデータをどのように扱うかを表します。
    */
  sealed trait Mode

  object Mode {
    def parse(value: String): Option[Mode] = value match {
      case "merge" => Some(Merging)
      case "override" => Some(Overriding)
      case _ => None
    }

    /**
      * メタデータを継承し、このメタデータを合成します。
      */
    case object Merging extends Mode

    /**
      * メタデータを継承しません。このコンポーネントに対するメタデータは、このメタデータで完全に設定されます。
      */
    case object Overriding extends Mode
  }
}
