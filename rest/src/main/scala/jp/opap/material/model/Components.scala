package jp.opap.material.model

import java.util.UUID

import scala.beans.BeanProperty

sealed trait IntermediateComponent {
  val name: String
  val path: String
}

case class IntermediateDirectory(name: String, path: String, children: Seq[IntermediateComponent]) extends IntermediateComponent {
}

case class IntermediateFile(name: String, path: String) extends IntermediateComponent {
}

sealed trait ComponentEntry {
  val id: UUID
  val repositoryId: String
  val parentId: Option[UUID]
  val name: String
  val path: String
}

case class DirectoryEntry(@BeanProperty id: UUID, @BeanProperty repositoryId: String,
  @BeanProperty parentId: Option[UUID], @BeanProperty name: String, @BeanProperty path: String) extends ComponentEntry {
}

case class FileEntry(@BeanProperty id: UUID, @BeanProperty repositoryId: String, @BeanProperty parentId: Option[UUID],
  @BeanProperty name: String, @BeanProperty path: String) extends ComponentEntry {
}

trait IThumbnail {
  val fileId: UUID
  val  width: Int
  val height: Int
}

case class ThumbnailInfo(@BeanProperty fileId: UUID, @BeanProperty width: Int, @BeanProperty height: Int) extends IThumbnail

case class Thumbnail(@BeanProperty fileId: UUID, @BeanProperty data: Array[Byte], @BeanProperty width: Int, @BeanProperty height: Int) extends IThumbnail
