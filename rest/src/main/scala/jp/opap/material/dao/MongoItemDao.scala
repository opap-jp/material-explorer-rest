package jp.opap.material.dao

import com.mongodb.BasicDBObject
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.{MongoCollection, MongoDatabase}
import jp.opap.material.model._
import org.bson.Document
import jp.opap.material.data.Collections.Iterables

class MongoItemDao(val mongo: MongoDatabase) {
  val collection: MongoCollection[Document]  = this.mongo.getCollection("items")

  def update(item: Item): Unit = {
    val key = new BasicDBObject()
      .append("project_id", item.projectId)
      .append("path", item.path)
    val document: Document = new Document()
      .append("project_id", item.projectId)
      .append("path", item.path)
      .append("itemType", item.itemType.toString)
    val options = new UpdateOptions().upsert(true)
    this.collection.replaceOne(key, document, options)
  }

  def findAll(): Seq[Item] = this.collection
    .find()
    .map[Option[Item]](d => {
      try {
        val record = Item(d.getString("project_id"), ItemType.valueOf(d.getString("itemType")), d.getString("path"))
        Option(record)
      } catch {
        case _: Throwable => Option.empty
      }
    })
    .toSeq
    .flatMap(item => item.iterator)

}
