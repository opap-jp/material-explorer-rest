package jp.opap.material.dao

import com.mongodb.client.MongoDatabase
import jp.opap.material.dao.MongoThumbnailDao.infoFromDocument
import jp.opap.material.model.Thumbnails.ThumbnailInfo
import org.bson.Document
import org.bson.types.Binary

class MongoThumbnailDao(mongo: MongoDatabase) extends MongoDao(mongo) {
  override def collectionName = "thumbnails"

  def insert(thumbnail: ThumbnailInfo, data: Array[Byte]): Unit = {
    val document = new Document()
      .append("_id", thumbnail.id)
      .append("width", thumbnail.width)
      .append("height", thumbnail.height)
      .append("data", new Binary(data))
    this.collection.insertOne(document)
  }

  def find(id: String): Option[ThumbnailInfo] = {
    this.findOneByKey("_id", id)
      .map(infoFromDocument)
  }

  def findData(blobId: String): Option[Array[Byte]] = this
    .findOneByKey("_id", blobId)
    .map(document => document.get[Binary]("data", classOf[Binary]).getData)
}

object MongoThumbnailDao {
  def infoFromDocument(document: Document): ThumbnailInfo = {
    val id = document.getString("_id")
    val width = document.getInteger("width")
    val height = document.getInteger("height")
    ThumbnailInfo(id, width, height)
  }
}
