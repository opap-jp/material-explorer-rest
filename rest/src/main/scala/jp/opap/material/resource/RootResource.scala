package jp.opap.material.resource

import java.io.IOException
import java.util.UUID
import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.{GET, Path, PathParam, Produces}

import com.fasterxml.jackson.databind.ObjectMapper
import jp.opap.material.MaterialExplorer.ServiceBundle
import jp.opap.material.dao.{MongoComponentDao, MongoRepositoryDao, MongoThumbnailDao}
import jp.opap.material.facade.RepositoryDataEventEmitter
import jp.opap.material.facade.RepositoryDataEventEmitter.{Progress, ProgressListener}
import org.glassfish.jersey.media.sse.{EventOutput, OutboundEvent, SseFeature}

@Path("")
class RootResource(val services: ServiceBundle, val eventEmitter: RepositoryDataEventEmitter) {
  val projectDao: MongoRepositoryDao = services.repositoryDao
  val componentDao: MongoComponentDao = services.componentDao
  val thumbnailDao: MongoThumbnailDao = services.thumbnailDao

  @GET
  @Path("/repositories")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def repositories(): Response = {
    val items = this.projectDao.find()
    val data = Map("items" -> items)
    Response.ok(data).build()
  }

  @GET
  @Path("/images")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def images(): Response = {
    val items = this.componentDao.findImages()
    val data = Map("items" -> items)
    Response.ok(data).build()
  }

  @GET
  @Path("/thumbnail/{file_id}")
  @Produces(Array("image/png"))
  def thumbnail(@PathParam("file_id") fileId: String): Response = {
    val id = UUID.fromString(fileId)
    try {
      val blobId = this.componentDao.findFileById(id).get.blobId
      val thumbnail = this.thumbnailDao.findData(blobId).get
      Response.ok(thumbnail)
        .build()
    } catch {
      case _: NoSuchElementException => Response.status(404).build()
    }
  }

  @GET
  @Path("/count")
  @Produces(Array(SseFeature.SERVER_SENT_EVENTS))
  def count(): EventOutput = {
    val eventOutput = new EventOutput()
    new Thread() {
      override def run(): Unit = {
        Iterable.range(0, 1000)
          .foreach(i => {
            val event = new OutboundEvent.Builder()
              .data(i)
              .build()
            Thread.sleep(1)
            eventOutput.write(event)
          })
        eventOutput.close()
      }
    }.start()
    eventOutput
  }

  @GET
  @Path("/progress")
  @Produces(Array(SseFeature.SERVER_SENT_EVENTS))
  def progress(): EventOutput = {
    if (eventEmitter.getRunning) {
      val serialize = (() => {
        val m = new ObjectMapper()
        (o: Object) => m.writeValueAsString(o)
      })()
      val eventOutput = new EventOutput()
      var lastEvent: Long = 0

      eventEmitter.subscribe(new ProgressListener {
        override def onUpdate(progress: Progress): Unit = {
          val event = new OutboundEvent.Builder()
            .data(serialize(progress))
            .build()
          try {
            val current = System.currentTimeMillis()
            val difference = current - lastEvent
            if (difference < 15 || eventOutput.isClosed) {

            } else {
              lastEvent = current
              eventOutput.write(event)
            }
          } catch {
            case e: NullPointerException => System.out.println(e)
            case e: IOException => System.out.println(e)
          }
        }

        override def onFinish(): Unit = {
          val event = new OutboundEvent.Builder()
            .name("close")
            .data("close")
            .build()

          eventOutput.write(event)
          eventOutput.close()
        }
      })
      eventOutput
    } else {
      val event = new OutboundEvent.Builder()
          .name("negative")
          .data("negative")
          .build()
      val eventOutput = new EventOutput()
      eventOutput.write(event)
      eventOutput.close()
      eventOutput
    }
  }
}
