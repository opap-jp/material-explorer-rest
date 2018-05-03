package jp.opap.material.model

import scala.beans.BeanProperty

// TODO: サムネイルの ID は、ハッシュ値とする
object Thumbnails {
  trait IThumbnail {
    val id: String
    val  width: Int
    val height: Int
  }

  case class ThumbnailInfo(@BeanProperty id: String, @BeanProperty width: Int, @BeanProperty height: Int) extends IThumbnail
}
