package jp.opap.material.resource

import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.{GET, Path, Produces}

import jp.opap.material.dao.{MongoItemDao, MongoProjectDao}

@Path("")
class RootResource(val projectDao: MongoProjectDao, val itemDao: MongoItemDao) {
  @GET
  @Path("/person")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def person(): Response = {
    val data: Map[String, _] = Map("name" -> "祝園アカネ")
    Response.ok(data).build()
  }

  @GET
  @Path("/repositories")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def repositories(): Response = {
    val items = this.projectDao.findProjects()
    val data = Map("items" -> items)
    Response.ok(data).build()
  }

  @GET
  @Path("/items")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def items(): Response = {
    val items = this.itemDao.findAll()
      .sortBy(item => item.path)
    val data = Map("items" -> items)
    Response.ok(data).build()
  }
}
