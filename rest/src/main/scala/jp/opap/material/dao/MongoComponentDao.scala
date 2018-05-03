package jp.opap.material.dao

import java.util.UUID

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoDatabase
import jp.opap.material.dao.MongoComponentDao.FileAndThumbnail
import jp.opap.material.dao.MongoDao.Documents
import jp.opap.material.model.ComponentEntry
import jp.opap.material.model.ComponentEntry.{DirectoryEntry, FileEntry}
import jp.opap.material.model.Thumbnail
import org.bson.Document

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

class MongoComponentDao(mongo: MongoDatabase) extends MongoDao(mongo) {
  override def collectionName = "components"

  def insert(item: ComponentEntry): Unit = {
    def componentDocument(component: ComponentEntry): Document = new Document()
      .append("_id", component.id.toString)
      .append("name", component.name)
      .append("parent_id", component.parentId.map(_.toString).orNull)
      .append("repository_id", component.repositoryId)
      .append("path", component.path)

    def fileDocument(file: FileEntry): Document = new Document()
      .append("blob_id", file.blobId)

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

  def findFiles(repositoryId: String): Seq[FileEntry] = {
    val filter = new BasicDBObject()
      .append("repository_id", repositoryId)
    this.collection.find(filter)
      .asScala
      .map(fromDocument)
      .par
      .flatMap {
        case file: FileEntry => Option(file)
        case _ => Option.empty[FileEntry]
      }
      .toSeq
      .seq
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
      "{ $lookup: { from: 'thumbnails', localField: 'blob_id', foreignField: '_id', as: 'thumbnail' } }",
      "{ $match: { thumbnail: { $size: 1 } } }",
      "{ $project: { 'thumbnail.data': false } }"
    ).map(BasicDBObject.parse).asJava

    this.collection.aggregate(pipeline)
      .asScala
      .flatMap(thumb)
      .toSeq
  }

  def deleteByRepositoryId(repositoryId: String): Unit = {
    val filter = new BasicDBObject()
      .append("repository_id", repositoryId)
    this.collection.deleteMany(filter)
  }

  def fromDocument(document: Document): ComponentEntry = {
    val id = UUID.fromString(document.getString("_id"))
    val repositoryId = document.getString("repository_id")
    val parentId = Option(document.getString("parent_id"))
      .map(UUID.fromString)
    val name = document.getString("name")
    val path = document.getString("path")
    val blobId = document.getString("blob_id")

    document.getString("_constructor") match {
      case "CompositeElement" => DirectoryEntry(id, repositoryId, parentId, name, path)
      case "LeafElement" => FileEntry(id, repositoryId, parentId, name, path, blobId)
      case _ => throw new IllegalArgumentException()
    }
  }
}

object MongoComponentDao {
  case class FileAndThumbnail(@BeanProperty file: FileEntry, @BeanProperty thumbnail: Thumbnail)
}


