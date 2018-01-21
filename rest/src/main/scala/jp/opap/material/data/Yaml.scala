package jp.opap.material.data

import java.io.{File, InputStream}
import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import com.google.common.base.Charsets
import com.google.common.io.Files
import org.yaml.snakeyaml.{Yaml => SnakeYaml}

import scala.collection.JavaConverters._

object Yaml {
  @deprecated
  def parse(file: File): Any = _parse(yaml => yaml.load[Object](Files.newReader(file, Charsets.UTF_8)))

  def parse(document: String): Any = _parse(yaml => yaml.load[Object](document))

  def parse(stream: InputStream): Any = _parse(yaml => yaml.load[Object](stream))

  protected def _parse(converter: SnakeYaml => Any): Any = {
    val yaml = new SnakeYaml()
    val root = converter(yaml)
    toNode(root)
  }

  protected def toNode(data: Any): Any = data match {
    case map: java.util.Map[_, _] => MapNode(map.asScala.toMap.map(entry => (entry._1.toString, toNode(entry._2))))
    case list: java.util.List[_] => ListNode(list.asScala.toList.map(element => toNode(element)))
    case null => Unit
    case value: Date => LocalDateTime.ofInstant(value.toInstant, ZoneId.systemDefault())
    case any: Any => any
  }

  sealed trait InternalNode

  case class MapNode(node: Map[String, Any]) extends InternalNode {
    def apply(key: String): Entry = Entry(key, this.node.get(key))

    override def toString: String = s"${this.getClass.getSimpleName} (${this.node.size})"
  }
  case class ListNode(node: List[Any]) extends InternalNode {
    override def toString: String = s"${this.getClass.getSimpleName} (${this.node.size})"
  }

  case class Entry(key: String, value: Option[Any]) {
    def get: Any = this.value match {
      case Some(x) => x
      case None => throw EntryException(s"$key に要素がありません。")
    }

    def option: Option[Entry] = this.value.map(v => this.copy(value = Option(v)))

    def string: String = this.value match {
      case Some(x: String) => x
      case None => throw EntryException(s"$key の値がありません。")
      case Some(x) => throw EntryException(s"$key => $x の値を文字列として取得することはできません。")
    }

    def stringOption: Option[String] = this.value match {
      case Some(x: String) => Option(x)
      case None => None
      case Some(x) => throw EntryException(s"$key => $x の値を文字列として取得することはできません。")
    }
  }

  case class EntryException(message: String) extends RuntimeException(message)
}
