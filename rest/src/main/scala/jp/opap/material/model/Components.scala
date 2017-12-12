package jp.opap.material.model

import java.util.UUID

import scala.beans.BeanProperty

sealed trait IntermediateComponent {
  val id: UUID
  val name: String

  final override def toString: String = {
    this match {
      case dir: IntermediateDirectory => s"${dir.path} (${dir.children.size})"
      case file: IntermediateFile => s"${file.path}"
    }
  }
}

/**
  *
  * @param id
  * @param name
  * @param path empty のとき、ルート要素です。
  * @param children
  */
case class IntermediateDirectory(id: UUID, name: String, path: Option[String], children: Map[String, IntermediateComponent]) extends IntermediateComponent {
}

case class IntermediateFile(id: UUID, name: String, path: String) extends IntermediateComponent {
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

  def toIntermediate: IntermediateFile = IntermediateFile(this.id, this.name, this.path)
}

trait IThumbnail {
  val fileId: UUID
  val  width: Int
  val height: Int
}

case class ThumbnailInfo(@BeanProperty fileId: UUID, @BeanProperty width: Int, @BeanProperty height: Int) extends IThumbnail

case class Thumbnail(@BeanProperty fileId: UUID, @BeanProperty data: Array[Byte], @BeanProperty width: Int, @BeanProperty height: Int) extends IThumbnail
