package jp.opap.material.model

import java.util.UUID

import jp.opap.data.yaml.Node
import jp.opap.material.model.Tag.TagName
import jp.opap.material.model.Warning.ComponentWarning

/**
  * 特定のフォーマット（YAMLなど）で記述され、リポジトリのいろいろな場所にファイルとして配置されることを前提とする、
  * ファイルやディレクトリに関連づけられるメタデータの集まりです。
  * このファイルの名称は AppConfiguration#metadataFileName で設定されます。
  */
case class MetadataBundle() {
}

object MetadataBundle {
  def fromYaml(root: Node, idGenerator: () => UUID): (Seq[ComponentWarning], MetadataBundle) = {
    val items = root("items")

    Seq() -> MetadataBundle()
  }

  /**
    * 名前（ファイルやディレクトリ）に対して直接的に設定されたメタデータです。メタデータファイルから取得されます。
    */
  case class AttachedMetadata(scope: Scope, tags: Seq[TagName])

  /**
    * メタデータのスコープ。メタデータがどのような範囲で有効かを表します。
    */
  sealed trait Scope

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

  /**
    * メタデータのモード。親ディレクトリで設定されたメタデータをどのように扱うかを表します。
    */
  sealed trait Mode

  /**
    * メタデータを継承し、このメタデータを合成します。
    */
  case object Merging extends Mode

  /**
    * メタデータを継承しません。このコンポーネントに対するメタデータは、このメタデータで完全に設定されます。
    */
  case object Overriding extends Mode
}
