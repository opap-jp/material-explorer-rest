package jp.opap.material.model

import java.io.IOException
import java.util.UUID

import jp.opap.material.data.Collections.{EitherList, Seqs}
import jp.opap.material.data.Yaml.{EntryException, ListNode, MapNode}
import jp.opap.material.model.Manifest.{Selector, TagGroup}
import jp.opap.material.model.Warning.GlobalWarning

import scala.util.matching.Regex

/**
  * メタデータで使用される識別子の宣言の集合です。
  */
case class Manifest(tagGroups: Seq[TagGroup], selectors: Seq[Selector])

object Manifest {
  // FIXME: 引き数の型が Any となっていて、事実上の実行時型エラーを生じやすいです。
  def fromYaml(document: Any): (List[GlobalWarning], Manifest) = try {
    document match {
      case root: MapNode => fromRoot(root)
      case _ =>
        val warning = new GlobalWarning(UUID.randomUUID(), "要素の型が不正です。")
        (List(warning), Manifest(List(), List()))
    }
  } catch {
    case e: IOException =>
      val warning = GlobalWarning(UUID.randomUUID(), "タグ定義ファイルの取得に失敗しました。", Option(e.getMessage))
      (List(warning), Manifest(List(), List()))
  }

  def fromRoot(root: MapNode): (List[GlobalWarning], Manifest) = {
    def extractTag(node: Any, groupIndex: Int, tagIndex: Int): Either[GlobalWarning, Tag] =
      try {
        node match {
          case x: MapNode =>
            val names = x("names").get match {
              case ListNode(y) => y.map {
                case z: String => z
                case z => throw EntryException(s"文字列が必要です。（$z でした。）")
              }
              case y: String => List(y)
              case y => throw EntryException(s"文字列が必要です。（$y でした。）")
            }
            Right(Tag.create(names, x("generic_replacement").stringOption))
          case _ => Left(new GlobalWarning(UUID.randomUUID(), s"tag_groups[$groupIndex].tags[$tagIndex]: names が必要です。"))
        }
      } catch {
        case e: EntryException => Left(new GlobalWarning(UUID.randomUUID(), s"tag_groups[$groupIndex].tags[$tagIndex]: ${e.message}"))
      }

    def extractTagGroup(element: (Any, Int)): (List[GlobalWarning], Option[TagGroup]) = {
      val (node, i) = element
      try {
        node match {
          case item: MapNode =>
            val category = item("category").stringOption
              .map(v => Category.parse(v) match {
                case Some(value) => value
                case None => throw EntryException(s"$v - そのようなカテゴリはありません。")
              }).getOrElse(Category.Common)

            val name = item("name").stringOption.getOrElse(category.defaultName match {
              case Some(value) => value
              case None => throw EntryException(s"name が必要です。")
            })

            val tags = item("tags").value match {
              case Some(ListNode(x)) => x.zipWithIndex.map(y => extractTag(y._1, i, y._2))
              case _ => throw EntryException("tags が必要です。")
            }
            (tags.left, Option(TagGroup(category, name, tags.right)))
        }
      } catch {
        case e: EntryException =>
          val warning = new GlobalWarning(UUID.randomUUID(), s"tag_groups[$i]: ${e.message}")
          (List(warning), None)
      }
    }

    def extractTagGroups(root: MapNode): (List[GlobalWarning], Seq[TagGroup]) = {
      root("tag_groups").get match {
        case ListNode(node) =>
          val x = node.zipWithIndex.map(extractTagGroup)
          (x.flatMap(y => y._1), x.flatMap(y => y._2))
        case _ =>
          // tag_groups の型が不正だったときのケース
          List(Left[GlobalWarning, TagGroup](new GlobalWarning(UUID.randomUUID(), "tag_groups が必要です。"))).leftRight
      }
    }

    def extractSelector(element: (Any, Int)): Either[GlobalWarning, Selector] = {
      val (node, i) =  element
      try {
        node match {
          case item: MapNode if item.node.contains("include") =>
            item("include").get match {
              case item: MapNode if item.node.contains("extensions") =>
                val extensions = item("extensions").get match {
                  case ListNode(x) => x.map {
                    case y: String => y
                    case y => throw EntryException(s"文字列が必要です。（$y でした）")
                  }
                }
                Right(Selector(Inclusive, ExtensionSetPredicate(extensions)))
            }
          // TODO: item("exclude") のケースが必要です。
          case _ => Left(new GlobalWarning(UUID.randomUUID(), s"selectors[$i]: include または exclude が必要です。"))
        }
      } catch {
        case e: EntryException => Left(new GlobalWarning(UUID.randomUUID(), s"selectors[$i]: ${e.message}"))
      }
    }

    def extractSelectors(root: MapNode): (List[GlobalWarning], Seq[Selector]) = {
      root("selectors").get match {
        case ListNode(node) => node.zipWithIndex.map(extractSelector).leftRight

        case _ =>
          // selectors の型が不正だったときのケース
          List(Left[GlobalWarning, Selector](new GlobalWarning(UUID.randomUUID(), "selectors が必要です。"))).leftRight
      }
    }

    def validate(warnings: List[GlobalWarning], manifest: Manifest): (List[GlobalWarning], Manifest) = {
      val duplications = manifest.tagGroups.flatMap(group => group.tags)
        .flatMap(tag => tag.names ++ tag.generic)
        .groupByOrdered(name => name.normalized)
        .filter(entry => entry._2.size > 1)
      val duplicatedNames = duplications.map(_._1).toSet
      val duplicationDictionary = duplications.toMap

      val m = manifest.copy(tagGroups = manifest.tagGroups.map(group => {
        group.copy(tags = group.tags
          .map(tag => {
            tag.copy(names = tag.names.filter(n => !duplicatedNames.contains(n.normalized)),
              generic = tag.generic.filter(n => !duplicatedNames.contains(n.normalized)))
          }).filter(tag => tag.names.nonEmpty || tag.generic.nonEmpty)
        )
      }))

      val w = duplicatedNames.map(n => new GlobalWarning(UUID.randomUUID(), s"${duplicationDictionary(n).head.name} - このラベルは重複しています。"))
      (warnings ++ w, m)
    }

    try {
      val (warnings1, tagGroups) = extractTagGroups(root)
      val (warnings2, selectors) = extractSelectors(root)
      val manifest = Manifest(tagGroups, selectors)
      validate(warnings1 ++ warnings2, manifest)
    } catch {
      case e: EntryException =>
        val warnings = List(new GlobalWarning(UUID.randomUUID(), e.message))
        val manifest = Manifest(Seq(), Seq())
        (warnings, manifest)
    }
  }

  case class TagGroup(category: Category, name: String, tags: Seq[Tag])

  case class Tag(names: List[TagName], generic: Option[TagName])

  object Tag {
    def create(names: List[String], generic: Option[String]): Tag = {
      Tag(names.map(TagName), generic.map(TagName))
    }
  }

  case class TagName(name: String) {
    lazy val normalized: String = normalize(name)

    override def equals(obj: Any): Boolean = obj match {
      case t: TagName => this.normalized == t.normalized
      case _ => false
    }
  }

  /**
    * タグのカテゴリを表現します。
    *
    * @param identifier このカテゴリの識別子。タグ定義ファイルで使用されることがあります。
    * @param defaultName 既定の名称
    * @param unique true の場合、このカテゴリのタググループはタグ定義に1つしか存在できません。
    */
  sealed abstract class Category(val identifier: String, val defaultName: Option[String], val unique: Boolean)

  object Category {
    def parse(value: String): Option[Category] = value match {
      case "common" => Option(Common)
      case "author" => Option(Author)
      case _ => None
    }

    case object Common extends Category("common", None, false)
    case object Author extends Category("author", Option("作成者"), true)
  }

  /**
    * ファイルが検索対象であるかを判定する型です。
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
    */
  case class ExtensionSetPredicate(extensions: Seq[String]) extends FilePredicate

  val SPACES: Regex = "[ 　]".r

  /**
    * 文字列を、タグの内部表現として正規化します。
    *
    * @param target 正規化の対象
    * @return 正規化された文字列
    */
  def normalize(target: String): String = {
    /**
      * 全角英数を半角英数に変換します。
      */
    def singleByte(character: Char): Char = {
      val c = character.toInt
      if ((c >= '０' && c <= '９') || (c >= 'Ａ' && c <= 'Ｚ') || (c >= 'ａ' && c <= 'ｚ'))
        (c  - 0xFEE0).toChar
      else
        character
    }

    /**
      * カタカナをひらがなに変換します。
      */
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

