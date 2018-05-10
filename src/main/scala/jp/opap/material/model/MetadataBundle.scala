package jp.opap.material.model

import java.util.UUID

import jp.opap.data.yaml.Node
import jp.opap.material.data.Collections.{EitherSeq, Seqs}
import jp.opap.material.model.MetadataBundle.{AttachedMetadata, Scope}
import jp.opap.material.model.Tag.TagName
import jp.opap.material.model.Warning.ComponentWarning

/**
  * 特定のフォーマット（YAMLなど）で記述され、リポジトリのいろいろな場所にファイルとして配置されることを前提とする、
  * ファイルやディレクトリに関連づけられるメタデータの集まりです。
  * このファイルの名称は AppConfiguration#metadataFileName で設定されます。
  * メタデータファイルとして存在する一つのファイルから、一つの MetadataBundle が生成されます。
  */
case class MetadataBundle(items: Map[Scope, AttachedMetadata]) {
}

object MetadataBundle {
  val WARNING_FAILED_TO_LOAD: String = "メタデータファイルの取得に失敗しました。"
  val WARNING_NO_SUCH_MODE_EXISTS: String = "%1$s - そのようなモードはありません。"
  val WARNING_INVALID_KEY_NAME: String = "%1$s - メタデータに対するキーとして不正な文字列です。"

  def fromYaml(root: Node, idGenerator: () => UUID): (Seq[ComponentWarning], MetadataBundle) = {
    // TODO: 正しい Context を指定する必要があります。
    def extractMetadata(key: String, node: Node): (Seq[Warning], Option[AttachedMetadata]) = withWarnings(GlobalContext) {
      val scope = Scope.parse(key) match {
        case Some(x) => x
        case None => throw DeserializationException(WARNING_INVALID_KEY_NAME.format(key), Option(node))
      }
      val mode = node("mode").string.option.map(x => Mode.parse(x) match {
        case Some(y) => x
        case None => throw DeserializationException(WARNING_NO_SUCH_MODE_EXISTS.format(x), Option(node))
      })

      val tags = node("tags").list
      Seq[Warning]() -> AttachedMetadata(scope, Seq())
    }

    val items = root("items").mapping.toMap.map(entry => entry._1 -> extractMetadata(entry._1, entry ._2))
    val metadataDictionary = items.flatMap(entry => entry._2._2.map(item => entry._1 -> item))
    val warnings = items.flatMap(entry => entry._2._1)

    // Seq() -> MetadataBundle(metadataDictionary)
    ???
  }

  /**
    * 名前（ファイルやディレクトリ）に対して直接的に設定されたメタデータです。メタデータファイルから取得されます。
    */
  case class AttachedMetadata(scope: Scope, tags: Seq[TagName])

  /**
    * メタデータのスコープ。メタデータがどのような範囲で有効かを表します。
    */
  sealed trait Scope

  object Scope {
    def parse(value: String): Option[Scope] = ???

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
    def parse(value: String): Option[Mode] = ???

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
