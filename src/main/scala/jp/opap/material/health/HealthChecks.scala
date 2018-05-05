package jp.opap.material.health

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result
import com.mongodb.client.MongoDatabase
import jp.opap.material.facade.MediaConverter.RestResize

import scala.collection.JavaConverters._

object HealthChecks {
  class MongoHealthCheck(val db: MongoDatabase) extends HealthCheck {
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

  class ImageMagickHealthCheck(val resize: RestResize) extends HealthCheck {
    override def check(): Result = {
      try {
        val pong = resize.ping()
        if (pong == "pong")
          Result.healthy()
        else
          Result.unhealthy(s"response must be pong. ($pong)")
      } catch {
        case e: Exception => Result.unhealthy(e)
      }
    }
  }
}
