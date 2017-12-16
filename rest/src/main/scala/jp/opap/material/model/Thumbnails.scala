package jp.opap.material.model

import java.util.UUID

import scala.beans.BeanProperty

object Thumbnails {
  trait IThumbnail {
    val fileId: UUID
    val  width: Int
    val height: Int
  }

  case class ThumbnailInfo(@BeanProperty fileId: UUID, @BeanProperty width: Int, @BeanProperty height: Int) extends IThumbnail

  case class Thumbnail(@BeanProperty fileId: UUID, @BeanProperty data: Array[Byte], @BeanProperty width: Int, @BeanProperty height: Int) extends IThumbnail
}
