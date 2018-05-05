package jp.opap.material.health

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result
import com.mongodb.client.MongoDatabase
import jp.opap.material.AppConfiguration

import scala.collection.JavaConverters._

object HealthChecks {
  class MongoHealthCheck(val db: MongoDatabase, val configuration: AppConfiguration) extends HealthCheck {
    override def check(): Result = {
      try {
        if (db.listCollectionNames().asScala.exists(_ == "repositories"))
          Result.healthy()
        else
          Result.unhealthy("unable to connect to MongoDB.")
      } catch {
        case e: Exception => Result.unhealthy(e)
      }
    }
  }
}
