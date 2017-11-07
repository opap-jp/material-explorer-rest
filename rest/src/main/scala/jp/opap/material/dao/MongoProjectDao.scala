package jp.opap.material.dao

import java.util.UUID

import com.mongodb.BasicDBObject
import com.mongodb.client.model.{InsertOneOptions, UpdateOptions}
import org.gitlab4j.api.models.{Project => GitLabProject}
import com.mongodb.client.{MongoCollection, MongoDatabase}
import jp.opap.material.data.Collections.Iterables
import jp.opap.material.data.Formats.Dates
import jp.opap.material.model.Project
import org.bson.Document

class MongoProjectDao(val mongo: MongoDatabase) {
  val projects: MongoCollection[Document]  = this.mongo.getCollection("projects")

  def updateProject(project: GitLabProject): Unit = {
    val document: Document =  new Document()
      .append("name", project.getName)
      .append("last_modified", project.getLastActivityAt)
    val key = new BasicDBObject("_id", project.getHttpUrlToRepo)
    val options = new UpdateOptions().upsert(true)

    this.projects.replaceOne(key, document, options)
  }

  def findProjects(): Seq[Project] = this.projects.find()
    .map(d => Project(d.getString("_id"), d.getString("name"), d.getDate("last_modified").toLocal))
    .toSeq
}
