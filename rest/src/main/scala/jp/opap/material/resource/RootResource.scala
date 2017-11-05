package jp.opap.material.resource

import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.{GET, Path, Produces}

import jp.opap.material.dao.MongoProjectDao

@Path("")
class RootResource(val projectDao: MongoProjectDao) {
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
}
