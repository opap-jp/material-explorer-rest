package jp.opap.data.yaml

import java.io.{File, InputStream}
import java.util.Date

import com.google.common.base.Charsets
import com.google.common.io.Files
import jp.opap.data.yaml.InternalNode.{ListNode, MappingNode}
import jp.opap.data.yaml.Leaf.{NullNode, UndefinedNode}
import jp.opap.data.yaml.Parent.EmptyParent
import jp.opap.data.yaml.YamlException.MalformedContentException
import org.yaml.snakeyaml.{Yaml => SnakeYaml}

import scala.collection.JavaConverters._

object Yaml {
  def parse(file: File): Node = _parse(yaml => yaml.load[Object](Files.newReader(file, Charsets.UTF_8)))

  def parse(stream: InputStream): Node = _parse(yaml => yaml.load[Object](stream))

  private def _parse(mapper: SnakeYaml => Any): Node = {
    val yaml = new SnakeYaml()
    val root = mapper(yaml)
    constructNode(root)
  }

  private def constructNode(data: Any): Node = data match {
    case x: java.util.Map[_, _] => new MappingNode(x.asScala.toMap.map(x => x._1.toString -> constructNode(x._2)), EmptyParent())
    case x: java.util.List[_] => new ListNode(x.asScala.toList.map(constructNode), EmptyParent())
    case null => NullNode(EmptyParent())
    case x: Date => UndefinedNode(EmptyParent()) // TODO: LocalDateTime.ofInstant(value.toInstant, ZoneId.systemDefault())
    case x: Any => throw MalformedContentException(x)
  }

  def either[T](supplier: => T): Either[YamlException, T] = {
    try {
      Right(supplier)
    } catch {
      case e: YamlException => Left(e)
    }
  }
}
