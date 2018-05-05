package jp.opap.material.model

import java.util.UUID

/**
  * リポジトリデータの処理の過程で生じる警告を表現する型です。
  */
sealed trait Warning {
  val id: UUID
  val message: String

  /**
    * この警告が、例外が発生したことによるものであるとき、その例外のメッセージ
    */
  val caused: Option[String]
}

object Warning {
  /**
    * グローバルレベルで生じる警告を表現するクラスです。
    */
  case class GlobalWarning(id: UUID, message: String, caused: Option[String]) extends Warning {
    def this(id: UUID, message: String) = this(id, message, None)
  }

  /**
    * リポジトリに関連して生じる警告を表現するクラスです。
    * @param repositoryId リポジトリID
    */
  case class RepositoryWarning(id: UUID, message: String, caused: Option[String], repositoryId: String) extends Warning

  /**
    * コンポーネント（ファイルやディレクトリ）に関連して生じる警告を表現するクラスです。
    *
    * @param caused この警告が、例外が発生したことによるものであるとき、その例外のメッセージ
    * @param componentId コンポーネントID
    */
  case class ComponentWarning(id: UUID, message: String, caused: Option[String], componentId: UUID) extends Warning
}
