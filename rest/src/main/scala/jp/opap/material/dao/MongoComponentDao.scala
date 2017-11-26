package jp.opap.material.dao

import java.util.UUID

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoDatabase
import jp.opap.material.dao.MongoComponentDao.FileAndThumbnail
import jp.opap.material.dao.MongoDao.Documents
import jp.opap.material.model.{ComponentEntry, DirectoryEntry, FileEntry, ThumbnailInfo}
import org.bson.Document

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

class MongoComponentDao(mongo: MongoDatabase) extends MongoDao(mongo) {

  override def collectionName = "components"

  def insert(item: ComponentEntry): Unit = {
    def componentDocument(component: ComponentEntry): Document = new Document()
      .append("_id", component.id.toString)
      .append("name", component.name)
      .append("parentId", component.parentId.map(_.toString).orNull)
      .append("repositoryId", component.repositoryId)
      .append("path", component.path)

    def fileDocument(file: FileEntry): Document = new Document()

    val document = item match {
      case component: DirectoryEntry => componentDocument(component).append("_constructor", "CompositeElement")
      case component: FileEntry => (componentDocument(component) + fileDocument(component)).append("_constructor", "LeafElement")
    }

    this.collection.insertOne(document)
  }

  def findById(id: UUID): Option[ComponentEntry] = {
    this.findOneByKey("_id", id)
      .map(fromDocument)
  }

  def findFileById(id: UUID): Option[FileEntry] = {
    this
      .findById(id)
      .flatMap(item => item match {
        case file: FileEntry  => Option(file)
        case _ => Option.empty
      })
  }

  def findImages(): Seq[FileAndThumbnail] = {
    def thumb(document: Document): Option[FileAndThumbnail] = {
      fromDocument(document) match {
        case file: FileEntry => document
          .getFirstDocumentFrom("thumbnail")
          .map(thumbnail => FileAndThumbnail(file, MongoThumbnailDao.infoFromDocument(thumbnail)))
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

  def fromDocument(document: Document): ComponentEntry = {
    val id = UUID.fromString(document.getString("_id"))
    val repositoryId = document.getString("repositoryId")
    val parentId = Option(document.getString("parentId"))
      .map(UUID.fromString)
    val name = document.getString("name")
    val path = document.getString("path")

    document.getString("_constructor") match {
      case "CompositeElement" => DirectoryEntry(id, repositoryId, parentId, name, path)
      case "LeafElement" => FileEntry(id, repositoryId, parentId, name, path)
      case _ => throw new IllegalArgumentException()
    }
  }
}

object MongoComponentDao {
  case class FileAndThumbnail(@BeanProperty file: FileEntry, @BeanProperty thumbnail: ThumbnailInfo)
}


