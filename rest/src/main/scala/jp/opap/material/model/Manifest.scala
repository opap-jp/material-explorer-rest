package jp.opap.material.model

/**
  * メタデータで使用される識別子の宣言の集合です。
  */
case class Manifest(tagGroups: Seq[TagGroup])

case class TagGroup(category: TagCategory, tags: Seq[Tag])

case class TagCategory(name: String)

case class Tag(names: Seq[String], generic: Option[String])
