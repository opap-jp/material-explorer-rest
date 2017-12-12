package jp.opap.material.data

import java.io.File
import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import com.google.common.base.Charsets
import com.google.common.io.Files
import org.yaml.snakeyaml.{Yaml => SnakeYaml}

import scala.collection.JavaConverters._

object Yaml {
  def parse(file: File): AnyRef = {
    val yaml = new SnakeYaml()
    val root = yaml.load[Object](Files.newReader(file, Charsets.UTF_8))
    toNode(root)
  }

  protected def toNode(data: Any): AnyRef = data match {
    case map: java.util.Map[_, _] => MapNode(map.asScala.toMap.map(entry => (entry._1.toString, toNode(entry._2))))
    case list: java.util.List[_] => ListNode(list.asScala.toList.map(element => toNode(element)))
    case null => Unit
    case value: Date => LocalDateTime.ofInstant(value.toInstant, ZoneId.systemDefault())
    case any: AnyRef => any
  }

  case class MapNode(node: Map[String, AnyRef])
  case class ListNode(node: List[AnyRef])
}
