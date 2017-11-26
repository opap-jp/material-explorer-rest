package jp.opap.material.facade

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import javax.imageio.ImageIO

import jp.opap.material.model.{FileEntry, Thumbnail}

import net.coobird.thumbnailator.Thumbnails

trait MediaConverter {
  def shouldDispatch(file: FileEntry): Boolean
  def getThumbnail(original: FileEntry, file: File): Thumbnail
}

object PngConverter extends MediaConverter {
  override def shouldDispatch(file: FileEntry): Boolean = file.name.toLowerCase.endsWith(".png")

  override def getThumbnail(original: FileEntry, file: File): Thumbnail = {
    val os = new ByteArrayOutputStream()
    Thumbnails.of(file)
      .size(290, 290)
      .toOutputStream(os)
    val bi = ImageIO.read(new ByteArrayInputStream(os.toByteArray))
    Thumbnail(original.id, os.toByteArray, bi.getWidth, bi.getHeight)
  }
}
