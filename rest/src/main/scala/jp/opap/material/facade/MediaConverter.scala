package jp.opap.material.facade

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import javax.imageio.ImageIO

import jp.opap.material.model._
import net.coobird.thumbnailator.Thumbnails

trait MediaConverter {
  def shouldDispatch(file: LeafElement[MetaHead, MetaFile]): Boolean
  def getThumbnail(original: LeafElement[MetaHead, MetaFile], file: File): Thumbnail
}

object PngConverter extends MediaConverter {
  override def shouldDispatch(file: LeafElement[MetaHead, MetaFile]): Boolean = file.head.name.toLowerCase.endsWith(".png")

  override def getThumbnail(original: LeafElement[MetaHead, MetaFile], file: File): Thumbnail = {
    val os = new ByteArrayOutputStream()
    Thumbnails.of(file)
      .size(290, 290)
      .toOutputStream(os)
    val bi = ImageIO.read(new ByteArrayInputStream(os.toByteArray))
    Thumbnail(original.head.id, os.toByteArray, bi.getWidth, bi.getHeight)
  }
}
