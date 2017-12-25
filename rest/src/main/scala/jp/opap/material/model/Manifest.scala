package jp.opap.material.model

import java.util.UUID

import jp.opap.material.data.Yaml.{EntryException, ListNode, MapNode}
import jp.opap.material.model.Manifest.TagGroup
import jp.opap.material.model.Warning.GlobalWarning
import jp.opap.material.data.Collections.EitherList

/**
  * メタデータで使用される識別子の宣言の集合です。
  */
case class Manifest(tagGroups: Seq[TagGroup])

object Manifest {
  def fromYaml(root: MapNode): (List[GlobalWarning], Manifest) = {
    def tag(node: Any): Either[GlobalWarning, Tag] =
      try {
        node match {
          case x: MapNode =>
            val names = x("names").get match {
              case ListNode(y) => y.map {
                case z: String => z
                case _ => throw EntryException("文字列が必要です。")
              }
              case y: String => List(y)
              case _ => throw EntryException("文字列が必要です。")
            }
            Right(Tag(names, x("generic_replacement").stringOption))
          case _ => Left(new GlobalWarning(UUID.randomUUID(), "names が必要です。"))
        }
      } catch {
        case e: EntryException => Left(new GlobalWarning(UUID.randomUUID(), e.message))
      }
    def tagGroup(element: (Any, Int)): (List[GlobalWarning], Option[TagGroup]) = {
      val (node, i) = element
      try {
        node match {
          case item: MapNode =>
            val category = item("category").stringOption
              .map(v => Category.parse(v) match {
                case Some(value) => value
                case None => throw EntryException(s"$v: そのようなカテゴリはありません。")
              }).getOrElse(Category.Common)

            val name = item("name").stringOption.getOrElse(category.defaultName match {
              case Some(value) => value
              case None => throw EntryException(s"name が必要です。")
            })

            val tags = item("tags").value match {
              case Some(ListNode(x)) => x.map(tag)
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

  case class Tag(names: Seq[String], generic: Option[String])

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
    case object Author extends Category("author", Option("author"), true)
  }
}

