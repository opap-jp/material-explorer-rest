package jp.opap.material.facade

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

import jp.opap.material.model.ComponentEntry.FileEntry
import jp.opap.material.model.Thumbnail
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder

trait MediaConverter {
  def shouldDispatch(file: FileEntry): Boolean
  def convert(original: FileEntry, data: Array[Byte]): (Thumbnail, Array[Byte])
}

object MediaConverter {
  class ImageConverter(val resize: IResize) extends MediaConverter {
    override def shouldDispatch(file: FileEntry): Boolean = {
      List(".ai", ".jpg", ".jpeg", ".pdf", ".png", ".psd")
        .exists(extension => file.name.toLowerCase.endsWith(extension))
    }

    override def convert(original: FileEntry, data: Array[Byte]):  (Thumbnail, Array[Byte]) = {
      val converted = resize.resize(original, data, 290, 290)
      val bi = ImageIO.read(new ByteArrayInputStream(converted))
      (Thumbnail(original.blobId, bi.getWidth, bi.getHeight), converted)
    }
  }

  trait IResize {
    def resize(original: FileEntry, data: Array[Byte], width: Int, height: Int): Array[Byte]
  }

  class RestResize(val host: String) extends IResize {
    def ping(): String = {
      Request.Get(host + "/ping")
        .connectTimeout(1000)
        .execute()
        .returnContent()
        .asString()
    }

    override def resize(original: FileEntry, data: Array[Byte], width: Int, height: Int): Array[Byte] = {
      val entity = MultipartEntityBuilder.create()
        .addTextBody("width", width.toString, ContentType.TEXT_PLAIN)
        .addTextBody("height", height.toString, ContentType.TEXT_PLAIN)
        .addBinaryBody("data", data, ContentType.MULTIPART_FORM_DATA, original.name)
        .build()
      Request.Post(host + "/resize")
        .body(entity)
        .execute()
        .returnContent()
        .asBytes()
    }
  }
}
