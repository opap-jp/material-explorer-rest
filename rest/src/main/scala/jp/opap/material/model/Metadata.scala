package jp.opap.material.model

import java.util.UUID

/**
  * コンポーネントに対して設定される追加的な情報です。
  */
case class Metadata(tags: Seq[UUID]) {

}

case object Metadata {
  /**
    * コンポーネントに設定されるタグです。
    */
  sealed trait ComponentTag {
    val id: UUID
    val tagId: UUID
  }

  /**
    * コンポーネントと宣言済みタグの関連です。
    */
  case class DeclaredComponentTag(id: UUID, tagId: UUID) extends ComponentTag

  /**
    * コンポーネントと未宣言タグの関連です。固有の名称を持ちます。
    *
    * @param name 未宣言タグの固有の名称
    */
  case class UndeclaredComponentTag(id: UUID, tagId: UUID, name: String) extends ComponentTag
}

