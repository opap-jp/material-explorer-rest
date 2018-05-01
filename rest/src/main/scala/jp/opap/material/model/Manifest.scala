package jp.opap.material.model

import java.util.UUID

import jp.opap.data.yaml.InternalNode.ListNode
import jp.opap.data.yaml.Leaf.{StringNode, UndefinedNode}
import jp.opap.data.yaml.Node
import jp.opap.data.yaml.YamlException.TypeException
import jp.opap.material.data.Collections.{EitherSeq, Seqs}
import jp.opap.material.model.Manifest.{Selector, TagGroup}
import jp.opap.material.model.Warning.GlobalWarning

import scala.util.matching.Regex

/**
  * メタデータで使用される識別子の宣言の集合です。
  */
case class Manifest(tagGroups: Seq[TagGroup], selectors: Seq[Selector])

object Manifest {
  val WARNING_FAILED_TO_LOAD: String = "タグ定義ファイルの取得に失敗しました。"
  val WARNING_NO_SUCH_KIND_EXISTS: String = "%1$s - そのような種別はありません。"
  val WARNING_GROUP_NAME_REQUIRED: String = "グループ %1$s には name が必要です。"
  val WARNING_DUPLICATED_NAME: String = "%1$s - この名称は重複しています。"
  val WARNING_SELECTOR_MODE_REQUIRED: String = "include または exclude が必要です。"
  val WARNING_NAME_REQUIRED: String = "names または generic_replacement に一つ以上の名称が必要です。"

  def fromYaml(document: Node):  (Seq[GlobalWarning], Manifest) = {
    def extractTagGroup(node: Node): (Seq[GlobalWarning], Option[TagGroup]) = {
      def extractTag(node: Node): Either[GlobalWarning, Tag]  = withWarning {
        val generic = node("generic_replacement").string.option
        val names = node("names") match {
          case StringNode(name, _) => List(name)
          case x: ListNode => x.map(y => y.string.get).toList
          case _: UndefinedNode[_] => List()
          case x => throw TypeException(x)
        }

        val tag = Tag.create(names, generic)
        if (tag.names.nonEmpty || tag.generic.nonEmpty)
          tag
        else
          throw GlobalException(WARNING_NAME_REQUIRED, Some(node))
      }

      withWarnings {
        val kind = node("kind").string.option.map(x => Kind.parse(x) match {
          case Some(y) => y
          case None => throw GlobalException(WARNING_NO_SUCH_KIND_EXISTS.format(x), Option(node))
        }).getOrElse(Kind.Common)

        val name = node("name").string.option.getOrElse(kind.defaultName match {
          case Some(value) => value
          case None => throw GlobalException(WARNING_GROUP_NAME_REQUIRED.format(kind.identifier), Option(node))
        })

        val tags = node("tags").list.map(extractTag).toList

        tags.left -> TagGroup(kind, name, tags.right)
      }
    }

    def extractSelector(node: Node): Either[GlobalWarning, Selector] = {
      def extractExtensions(node: Node): FilePredicate = ExtensionSetPredicate(node("extensions").list.map(node => node.string.get).toList)

      withWarning {
        node.mapping.toMap match {
          case x if x.contains("include") => Selector(Inclusive, extractExtensions(node("include")))
          case x if x.contains("exclude") => Selector(Exclusive, extractExtensions(node("exclusive")))
          case _ => throw GlobalException(WARNING_SELECTOR_MODE_REQUIRED, Option(node))
        }
      }
    }

    def validate(warnings: Seq[GlobalWarning], manifest: Manifest): (Seq[GlobalWarning], Manifest) = {
      val duplications = manifest.tagGroups.flatMap(group => group.tags)
        .flatMap(tag => tag.names ++ tag.generic)
        .groupByOrdered(name => name.normalized)
        .filter(entry => entry._2.size > 1)
      val duplicatedNames = duplications.map(_._1).toSet
      val duplicationDictionary = duplications.toMap

      val m = manifest.copy(tagGroups = manifest.tagGroups.map(group => {
        // 重複した名称が消去されたことでタグが空になっていることがあるので、そのようなタグを消去します。
        val filtered = group.tags
          .map(tag => {
            tag.copy(names = tag.names.filter(n => !duplicatedNames.contains(n.normalized)),
              generic = tag.generic.filter(n => !duplicatedNames.contains(n.normalized)))
          }).filter(tag => tag.names.nonEmpty || tag.generic.nonEmpty)
        group.copy(tags = filtered)
      }))

      val w = duplicatedNames.map(n => new GlobalWarning(UUID.randomUUID(), WARNING_DUPLICATED_NAME.format(duplicationDictionary(n).head.name)))
      (warnings ++ w, m)
    }

    val manifest = withWarnings {
      val tagGroups = document("tag_groups").list.map(extractTagGroup).toSeq
      val selectors = document("selectors").list.map(extractSelector).toSeq
      val warnings = tagGroups.flatMap(item => item._1) ++ selectors.left

      warnings -> Manifest(tagGroups.flatMap(item => item._2), selectors.right)
    }

    validate(manifest._1, manifest._2.getOrElse(Manifest(Seq(), Seq())))
  }

  case class TagGroup(kind: Kind, name: String, tags: Seq[Tag])


  /**
    * タグの種別を表現します。
    *
    * @param identifier この種別の識別子。タグ定義ファイルで使用されることがあります。
    * @param defaultName 既定の名称。None の場合、この種別が設定されたグループには名前が必要です。
    * @param unique true の場合、この種別のグループはタグ定義に1つしか存在できません。
    */
  sealed abstract class Kind(val identifier: String, val defaultName: Option[String], val unique: Boolean)

  object Kind {
    def parse(value: String): Option[Kind] = value match {
      case "common" => Option(Common)
      case "author" => Option(Author)
      case _ => None
    }

    case object Common extends Kind("common", None, false)
    case object Author extends Kind("author", Some("作成者"), true)
    case object Undeclared extends Kind("undeclared", Some("未宣言"), true)
  }

  /**
    * ファイルが検索対象であるかを判定する型です。
    *
    * @param mode セレクタのモード。述語が選択したものを対象とするかしないかを表します。
    * @param predicate 述語。ファイルを特定のロジックで選択します。
    */
  case class Selector(mode: SelectorMode, predicate: FilePredicate)

  sealed trait SelectorMode
  case object Inclusive extends SelectorMode
  case object Exclusive extends SelectorMode

  sealed trait FilePredicate

  /**
    * 拡張子の集合で構成された述語です。
    *
    * @param extensions 判定に用いる、拡張子の集合
    */
  case class ExtensionSetPredicate(extensions: Seq[String]) extends FilePredicate

  val SPACES: Regex = "[ 　]".r

  /**
    * 文字列を、タグの内部表現として正規化します。
    * <ul>
    *   <li>半角スペースと全角スペースが削除されます。</li>
    *   <li>全角英数は半角英数に変換されます。</li>
    *   <li>カタカナはひらがなに変換されます。</li>
    *   <li>大文字は小文字に変換されます。</li>
    * </ul>
    *
    * @param target 正規化の対象
    * @return 正規化された文字列
    */
  def normalize(target: String): String = {
    def singleByte(character: Char): Char = {
      val c = character.toInt
      if ((c >= '０' && c <= '９') || (c >= 'Ａ' && c <= 'Ｚ') || (c >= 'ａ' && c <= 'ｚ'))
        // 0xFEE0 は、半角と全角のオフセット
        (c  - 0xFEE0).toChar
      else
        character
    }

    def kana(character: Char): Char = {
      val c = character.toInt
      if (c >= 'ァ' && c <= 'ン')
        (c - 96).toChar
      else
        character
    }

    SPACES.replaceAllIn(target, "")
      .map(singleByte)
      .map(kana)
      .toLowerCase
  }
}
