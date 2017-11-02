package jp.opap.material.resource

import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.{GET, Path, Produces}

@Path("")
class RootResource {
  @GET
  @Path("/person")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def person(): Response = {
    val data: Map[String, String] = Map("name" -> "祝園アカネ")

    Response.ok(data).build()
  }
}
