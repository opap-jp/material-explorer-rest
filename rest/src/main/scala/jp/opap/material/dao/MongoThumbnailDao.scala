package jp.opap.material.dao

import java.util.UUID

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoDatabase
import jp.opap.material.dao.MongoThumbnailDao.fromDocument
import jp.opap.material.model.{FileEntry, Thumbnail, ThumbnailInfo}
import org.bson.Document
import org.bson.types.Binary

class MongoThumbnailDao(mongo: MongoDatabase) extends MongoDao(mongo) {
  override def collectionName = "thumbnails"

  def insert(item: Thumbnail, file: FileEntry): Unit = {
    val document = new Document()
      .append("file_id", item.fileId.toString)
      .append("data", new Binary(item.data))
      .append("width", item.width)
      .append("height", item.height)
      .append("repository_id", file.repositoryId)
      .append("path", file.path)
    this.collection.insertOne(document)
  }

  def findById(id: UUID): Option[Thumbnail] = this
    .findOneByKey("file_id", id)
    .map(fromDocument)

  def deleteByFile(file: FileEntry): Unit = {
    val filter = new BasicDBObject()
      .append("repository_id", file.repositoryId)
      .append("path", file.path)

    this.collection.deleteOne(filter)
  }
}

object MongoThumbnailDao {
  def fromDocument(document: Document): Thumbnail = {
    val id = UUID.fromString(document.getString("file_id"))
    Thumbnail(id, document.get[Binary]("data", classOf[Binary]).getData, document.getInteger("width"), document.getInteger("height"))
  }

  def infoFromDocument(document: Document): ThumbnailInfo = {
    val id = UUID.fromString(document.getString("file_id"))
    ThumbnailInfo(id, document.getInteger("width"), document.getInteger("height"))
  }
}
