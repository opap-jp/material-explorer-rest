package jp.opap.material.dao

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.UpdateOptions
import jp.opap.material.dao.MongoRepositoryDao.fromDocument
import jp.opap.material.data.Collections.Iterables
import jp.opap.material.model.Repository
import org.bson.Document

class MongoRepositoryDao(mongo: MongoDatabase) extends MongoDao(mongo) {
  override def collectionName = "repositories"

  def update(repository: Repository): Unit = {
    val document: Document =  new Document()
      .append("name", repository.name)
      .append("title", repository.title)
      .append("head_hash", repository.headHash)
    val key = new BasicDBObject("_id", repository.id)
    val options = new UpdateOptions().upsert(true)

    this.collection.replaceOne(key, document, options)
  }

  def find(): Seq[Repository] = this.collection
    .find()
    .map[Option[Repository]](d => {
      try {
        Option(MongoRepositoryDao.fromDocument(d))
      } catch {
        case _: Throwable => Option.empty
      }
    })
    .toSeq
    .flatMap(item => item.iterator)

  def findById(id: String): Option[Repository] = {
    this.findOneByKey("_id", id)
      .map(fromDocument)
  }

  def removeById(id: String): Unit = {
    val key = new BasicDBObject("_id", id)
    this.collection.deleteOne(key)
  }
}

object MongoRepositoryDao {
  def fromDocument(document: Document): Repository = {
    val id = document.getString("_id")
    Repository(id, document.getString("name"), document.getString("title"), document.getString("head_hash"))
  }
}
