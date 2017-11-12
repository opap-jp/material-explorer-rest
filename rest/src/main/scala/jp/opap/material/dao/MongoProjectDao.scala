package jp.opap.material.dao

import java.time.LocalDateTime

import com.mongodb.BasicDBObject
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.{MongoCollection, MongoDatabase}
import jp.opap.material.data.Collections.Iterables
import jp.opap.material.model.Project
import org.bson.Document

class MongoProjectDao(val mongo: MongoDatabase) {
  val projects: MongoCollection[Document]  = this.mongo.getCollection("projects")

  def updateProject(project: Project): Unit = {
    val document: Document =  new Document()
      .append("name", project.name)
      .append("title", project.title)
      .append("last_modified", project.getLastActivityAt.toString)
    val key = new BasicDBObject("_id", project.id)
    val options = new UpdateOptions().upsert(true)

    this.projects.replaceOne(key, document, options)
  }

  def findProjects(): Seq[Project] = this.projects.find()
    .map[Option[Project]](d => {
      try {
        val record = Project(d.getString("_id"), d.getString("name"), d.getString("title"), LocalDateTime.parse(d.getString("last_modified")))
        Option(record)
      } catch {
        case _: Throwable => Option.empty
      }
    })
    .toSeq
    .flatMap(item => item.iterator)

  def removeProjectById(id: String): Unit = {
    val key = new BasicDBObject("_id", id)
    this.projects.deleteOne(key)
  }
}
