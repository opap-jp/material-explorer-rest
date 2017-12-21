package jp.opap.material.model

import jp.opap.material.model.Manifest.TagGroup

/**
  * メタデータで使用される識別子の宣言の集合です。
  */
case class Manifest(tagGroups: Seq[TagGroup])

object Manifest {
  case class TagGroup(category: Category, name: String, tags: Seq[Tag])

  case class Tag(names: Seq[String], generic: Option[String])

  sealed abstract class Category(val code: String)

  object Category {
    case object Common extends Category("common")
    case object Author extends Category("author")
  }
}

