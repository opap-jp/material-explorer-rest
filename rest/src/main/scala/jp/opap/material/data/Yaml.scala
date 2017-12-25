package jp.opap.material.data

import java.io.File
import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import com.google.common.base.Charsets
import com.google.common.io.Files
import org.yaml.snakeyaml.{Yaml => SnakeYaml}

import scala.collection.JavaConverters._

object Yaml {
  def parse(file: File): Any = {
    val yaml = new SnakeYaml()
    val root = yaml.load[Object](Files.newReader(file, Charsets.UTF_8))
    toNode(root)
  }

  def parse(document: String): Any = {
    val yaml = new SnakeYaml()
    val root = yaml.load[Object](document)
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
    // def option: Option[AnyRef] = this.value

    def string: String = this.value match {
      case Some(x: String) => x
      case None => throw EntryException(s"$key に要素がありません。")
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
