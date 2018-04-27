package jp.opap.material.model

import java.util.UUID

sealed trait MetaComponent {
  val id: UUID
  val name: String
  val metaData: MetaData
}

object MetaComponent {
  case class MetaDirectory(id: UUID, name: String, metaData: MetaData, children: Map[String, MetaComponent]) extends MetaComponent {

  }

  case class MetaFile(id: UUID, name: String, metaData: MetaData) extends MetaComponent {

  }
}
