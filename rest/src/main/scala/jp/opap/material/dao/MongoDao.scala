package jp.opap.material.dao

import com.mongodb.BasicDBObject
import com.mongodb.client.{MongoCollection, MongoDatabase}
import jp.opap.material.data.Collections
import org.bson.Document

abstract class MongoDao(val mongo: MongoDatabase) {
  def collectionName: String

  val collection: MongoCollection[Document] = this.mongo.getCollection(this.collectionName)

  protected def findOneByKey(key: String, value: Any): Option[Document] = {
    val filter = new BasicDBObject()
      .append(key, value.toString)
    val documents = this.collection.find(filter)
    Option(documents.first())
  }

  def drop(): Unit = {
    this.collection.drop()
  }
}

object MongoDao {
  implicit class Documents(val self: Document) {
    def +(another: Document): Document = {
      val document = new Document()
      self.forEach((k, v) => document.put(k, v))
      another.forEach((k, v) => document.put(k, v))
      document
    }

    def getDocuments(key: String): Option[List[Document]] = {
      Option(self.get(key)).flatMap(doc => {
        doc match {
          case list: java.util.List[_] => Collections.parameterized[Any, Document](list.asInstanceOf[java.util.List[Any]])
          case _ => Option.empty
        }
      })
    }

    def getFirstDocumentFrom(key: String): Option[Document] = {
      this
        .getDocuments(key)
        .flatMap(documents => documents match {
          case x :: xs => Option(x)
          case _ => Option.empty
        })
    }
  }
}
