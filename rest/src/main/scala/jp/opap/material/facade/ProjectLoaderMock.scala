package jp.opap.material.facade

import java.io.File
import java.time.LocalDateTime
import java.util

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jp.opap.material.ProjectInfo
import jp.opap.material.model.{Item, ItemType, Project}

import scala.collection.JavaConverters._

class ProjectLoaderMock extends ProjectLoader {
  override def shouldDispatch(protocol: String): Boolean = true

  override def loadByProjectInfo(info: ProjectInfo, storedProjects: Map[String, Project]): DeferredProject = {
    val content = new File(getClass.getClassLoader.getResource("fixture/items.json").getFile)
    val mapLists: util.List[util.List[util.Map[String, String]]] = new ObjectMapper().readValue(content, new TypeReference[util.List[util.List[util.Map[String, String]]]]() {})

    new DeferredProject() {
      override def project: Project = {
        Project("gitlab:kosys-ep02", "kosys-ep02", "こうしす！#2", LocalDateTime.parse("2017-11-05T21:42:29"), "7047be879d57ed12c58a9908b916f46a5f46dee2")
      }

      override def isModified: Boolean = true

      override def items: ItemIterable = {
        new ItemIterable {
          override def count: Int = mapLists.asScala.map(chunk => chunk.asScala.size).sum

          override def iterator: java.util.stream.Stream[Item] = mapLists
            .parallelStream()
            .map[java.util.stream.Stream[Item]](chunk => {
              val latency = (500 + (Math.random() * 1500)).longValue()
              Thread.sleep(latency)
              chunk.stream()
                .map(entry => Item("gitlab:kosys-ep02", ItemType.valueOf(entry.get("itemType")), entry.get("path")))
            }).flatMap(s => s)
        }
      }
    }
  }
}
