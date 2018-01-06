package jp.opap.material.model

import java.io.{File, IOException}
import java.util.UUID

import jp.opap.material.data.Yaml.{EntryException, ListNode, MapNode}
import jp.opap.material.model.Manifest.TagGroup
import jp.opap.material.model.Warning.GlobalWarning
import jp.opap.material.data.Collections.EitherList
import jp.opap.material.data.Yaml

import scala.util.matching.Regex

/**
  * メタデータで使用される識別子の宣言の集合です。
  */
case class Manifest(tagGroups: Seq[TagGroup])

object Manifest {
  def fromYaml(file: File): (List[GlobalWarning], Manifest) = try {
    Yaml.parse(file) match {
      case root: MapNode => fromRoot(root)
      case _ =>
        val warning = new GlobalWarning(UUID.randomUUID(), "要素の型が不正です。")
        (List(warning), Manifest(List()))
    }
  } catch {
    case e: IOException =>
      val warning = GlobalWarning(UUID.randomUUID(), "タグ定義ファイルの取得に失敗しました。", Option(e.getMessage))
      (List(warning), Manifest(List()))
  }

  def fromRoot(root: MapNode): (List[GlobalWarning], Manifest) = {
    def tag(node: Any, groupIndex: Int, tagIndex: Int): Either[GlobalWarning, Tag] =
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
    def tagGroup(element: (Any, Int)): (List[GlobalWarning], Option[TagGroup]) = {
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
              case Some(ListNode(x)) => x.zipWithIndex.map(y => tag(y._1, i, y._2))
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

    try {
      val (warnings, groups) = root("tag_groups").get match {
        case ListNode(node) =>
          val x = node.zipWithIndex.map(tagGroup)
          (x.flatMap(y => y._1), x.flatMap(y => y._2))
        case _ => List(Left[GlobalWarning, TagGroup](new GlobalWarning(UUID.randomUUID(), "tag_groups が必要です。"))).leftRight
      }
      (warnings, Manifest(groups))
    } catch {
      case e: EntryException =>
        val warnings = List(new GlobalWarning(UUID.randomUUID(), e.message))
        val groups = Manifest(Seq())
        (warnings, groups)
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

