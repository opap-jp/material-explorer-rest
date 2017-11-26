package jp.opap.material.dao

import java.util.UUID

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoDatabase
import jp.opap.material.dao.MongoComponentDao.FileAndThumbnail
import jp.opap.material.dao.MongoDao.Documents
import jp.opap.material.model.Components.{Component, FileElement}
import jp.opap.material.model.{ComponentElement, CompositeElement, LeafElement, MetaFile, MetaHead, ThumbnailInfo}
import org.bson.Document
import scala.beans.BeanProperty
import scala.collection.JavaConverters._

class MongoComponentDao(mongo: MongoDatabase) extends MongoDao(mongo) {

  override def collectionName = "components"

  def insert(item: ComponentElement[MetaHead, MetaFile]): Unit = {
    def headDocument(head: MetaHead): Document = new Document()
      .append("_id", head.id.toString)
      .append("name", head.name)
      .append("parentId", head.parentId.map(_.toString).orNull)
      .append("repositoryId", head.repositoryId)
      .append("path", head.path)

    def leafDocument(head: MetaFile): Document = new Document()

    val document = item match {
      case CompositeElement(head) => headDocument(head).append("_constructor", "CompositeElement")
      case LeafElement(head, payload) => (headDocument(head) + leafDocument(payload)).append("_constructor", "LeafElement")
    }

    this.collection.insertOne(document)
  }

  def findById(id: UUID): Option[ComponentElement[MetaHead, MetaFile]] = {
    this.findOneByKey("_id", id)
      .map(fromDocument)
  }

  def findFileById(id: UUID): Option[LeafElement[MetaHead, MetaFile]] = {
    this
      .findById(id)
      .flatMap(item => item match {
        case x@LeafElement(_, _) => Option(x)
        case _ => Option.empty
      })
  }

  def findImages(): Seq[FileAndThumbnail] = {
    def thumb(document: Document): Option[FileAndThumbnail] = {
      fromDocument(document) match {
        case leaf@LeafElement(_, _) => document
          .getFirstDocumentFrom("thumbnail")
          .map(thumbnail => FileAndThumbnail(leaf, MongoThumbnailDao.infoFromDocument(thumbnail)))
        case _ => Option.empty
      }
    }

    val pipeline = List(
      "{ $lookup: { from: 'thumbnails', localField: '_id', foreignField: 'file_id', as: 'thumbnail' } }",
      "{ $match: { thumbnail: { $size: 1 } } }",
      "{ $project: { 'thumbnail.data': false } }"
    ).map(BasicDBObject.parse).asJava

    this.collection.aggregate(pipeline)
      .asScala
      .flatMap(thumb)
      .toSeq
  }

  def fromDocument(document: Document): Component = {
    val id = UUID.fromString(document.getString("_id"))
    val parentId = Option(document.getString("parentId"))
      .map(UUID.fromString)
    val head = MetaHead(id, document.getString("repositoryId"), parentId, document.getString("name"), document.getString("path"))

    document.getString("_constructor") match {
      case "CompositeElement" => CompositeElement(head)
      case "LeafElement" => LeafElement(head, MetaFile())
      case _ => throw new IllegalArgumentException()
    }
  }
}

object MongoComponentDao {
  case class FileAndThumbnail(@BeanProperty file: FileElement, @BeanProperty thumbnail: ThumbnailInfo)
}


