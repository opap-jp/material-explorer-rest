package jp.opap.material.dao

import com.mongodb.client.MongoDatabase
import jp.opap.material.dao.MongoThumbnailDao.infoFromDocument
import jp.opap.material.model.Thumbnail
import org.bson.Document
import org.bson.types.Binary

class MongoThumbnailDao(mongo: MongoDatabase) extends MongoDao(mongo) {
  override def collectionName = "thumbnails"

  def insert(thumbnail: Thumbnail, data: Array[Byte]): Unit = {
    val document = new Document()
      .append("_id", thumbnail.id)
      .append("width", thumbnail.width)
      .append("height", thumbnail.height)
      .append("data", new Binary(data))
    this.collection.insertOne(document)
  }

  def find(id: String): Option[Thumbnail] = {
    this.findOneByKey("_id", id)
      .map(infoFromDocument)
  }

  def findData(blobId: String): Option[Array[Byte]] = this
    .findOneByKey("_id", blobId)
    .map(document => document.get[Binary]("data", classOf[Binary]).getData)
}

object MongoThumbnailDao {
  def infoFromDocument(document: Document): Thumbnail = {
    val id = document.getString("_id")
    val width = document.getInteger("width")
    val height = document.getInteger("height")
    Thumbnail(id, width, height)
  }
}
