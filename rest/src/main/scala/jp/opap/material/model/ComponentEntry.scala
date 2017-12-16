package jp.opap.material.model

import java.util.UUID

import jp.opap.material.model.Components.IntermediateFile

import scala.beans.BeanProperty


sealed trait ComponentEntry {
  val id: UUID
  val repositoryId: String
  val parentId: Option[UUID]
  val name: String
  val path: String
}

object ComponentEntry {
  case class DirectoryEntry(@BeanProperty id: UUID, @BeanProperty repositoryId: String,
    @BeanProperty parentId: Option[UUID], @BeanProperty name: String, @BeanProperty path: String) extends ComponentEntry {
  }

  case class FileEntry(@BeanProperty id: UUID, @BeanProperty repositoryId: String, @BeanProperty parentId: Option[UUID],
    @BeanProperty name: String, @BeanProperty path: String) extends ComponentEntry {

    def toIntermediate: IntermediateFile = IntermediateFile(this.id, this.name, this.path)
  }
}
