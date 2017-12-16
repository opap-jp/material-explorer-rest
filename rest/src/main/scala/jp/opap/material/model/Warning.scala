package jp.opap.material.model

import java.util.UUID

/**
  * リポジトリデータの処理の過程で生じる警告を表現する型です。
  */
sealed trait Warning {
  val id: UUID
  val message: String
  val caused: Option[String]
}

object Warning {

  /**
    * コンポーネント（ファイルやフォルダ）に関連して生じる警告を表現するクラスです。
    *
    * @param id ID
    * @param message メッセージ
    * @param caused この警告が、例外が発生したことによるものであるとき、その例外のメッセージ
    * @param repositoryId コンポーネントのリポジトリID
    * @param path コンポーネントのパス
    */
  case class ComponentWarning(id: UUID, message: String, caused: Option[String], repositoryId: String, path: String) extends Warning
}
