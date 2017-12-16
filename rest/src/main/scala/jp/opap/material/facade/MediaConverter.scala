package jp.opap.material.facade

import java.io.{ByteArrayInputStream, File}
import javax.imageio.ImageIO

import com.google.common.io.Files
import jp.opap.material.model.ComponentEntry.FileEntry
import jp.opap.material.model.Thumbnails.Thumbnail
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder

trait MediaConverter {
  def shouldDispatch(file: FileEntry): Boolean
  def convert(original: FileEntry, file: File): Thumbnail
}

object MediaConverter {
  class ImageConverter(val resize: IResize) extends MediaConverter {
    override def shouldDispatch(file: FileEntry): Boolean = {
      List(".ai", ".jpg", ".jpeg", ".pdf", ".png", ".psd")
        .exists(extension => file.name.toLowerCase.endsWith(extension))
    }

    override def convert(original: FileEntry, file: File): Thumbnail = {
      val converted = resize.resize(file, 290, 290)
      val bi = ImageIO.read(new ByteArrayInputStream(converted))
      Thumbnail(original.id, converted, bi.getWidth, bi.getHeight)
    }
  }

  trait IResize {
    def resize(file: File, width: Int, height: Int): Array[Byte]
  }

  class RestResize(val host: String) extends IResize {
    override def resize(file: File, width: Int, height: Int): Array[Byte] = {
      val entity = MultipartEntityBuilder.create()
        .addTextBody("width", width.toString, ContentType.TEXT_PLAIN)
        .addTextBody("height", height.toString, ContentType.TEXT_PLAIN)
        .addBinaryBody("data", Files.toByteArray(file), ContentType.MULTIPART_FORM_DATA, file.getName)
        .build()
      Request.Post(host + "/resize")
        .body(entity)
        .execute()
        .returnContent()
        .asBytes()
    }
  }
}
