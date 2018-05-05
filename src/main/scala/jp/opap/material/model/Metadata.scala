package jp.opap.material.model

import java.util.UUID

/**
  * コンポーネントに対して設定される追加的な情報です。
  */
case class Metadata(tags: Seq[UUID]) {

}

case object Metadata {
  /**
    * コンポーネントとタグの関連です。
    */
  case class ComponentTag(id: UUID, tagId: UUID)
}
