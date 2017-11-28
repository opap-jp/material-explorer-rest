package jp.opap.material.dao

import java.time.LocalDateTime

import com.mongodb.BasicDBObject
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.{MongoCollection, MongoDatabase}
import jp.opap.material.data.Collections.Iterables
import jp.opap.material.model.Repository
import org.bson.Document

class MongoRepositoryDao(val mongo: MongoDatabase) {
  val collection: MongoCollection[Document]  = this.mongo.getCollection("repositories")

  def update(repository: Repository): Unit = {
    val document: Document =  new Document()
      .append("name", repository.name)
      .append("title", repository.title)
      .append("last_modified", repository.getLastActivityAt.toString)
    val key = new BasicDBObject("_id", repository.id)
    val options = new UpdateOptions().upsert(true)

    this.collection.replaceOne(key, document, options)
  }

  def find(): Seq[Repository] = this.collection
    .find()
    .map[Option[Repository]](d => {
      try {
        val record = Repository(d.getString("_id"), d.getString("name"), d.getString("title"), LocalDateTime.parse(d.getString("last_modified")), "")
        Option(record)
      } catch {
        case _: Throwable => Option.empty
      }
    })
    .toSeq
    .flatMap(item => item.iterator)

  def removeById(id: String): Unit = {
    val key = new BasicDBObject("_id", id)
    this.collection.deleteOne(key)
  }
}
