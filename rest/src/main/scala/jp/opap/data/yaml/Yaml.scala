package jp.opap.data.yaml

import java.io.{File, InputStream}
import java.math.BigInteger
import java.util.Date

import com.google.common.base.Charsets
import com.google.common.io.Files
import jp.opap.data.yaml.InternalNode.{ListNode, MappingNode}
import jp.opap.data.yaml.Leaf.{BigIntegerNode, BooleanNode, DateNode, DoubleNode, IntNode, LongNode, NullNode, StringNode}
import jp.opap.data.yaml.Parent.EmptyParent
import jp.opap.data.yaml.YamlException.UnsupportedMappingKeyException
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
    case null => NullNode(EmptyParent())
    case x: String => StringNode(x, EmptyParent())
    case x: Boolean => BooleanNode(x, EmptyParent())
    case x: Int => IntNode(x, EmptyParent())
    case x: Long => LongNode(x, EmptyParent())
    case x: BigInteger => BigIntegerNode(x, EmptyParent())
    case x: Double => DoubleNode(x, EmptyParent())
    case x: Date => DateNode(x.toInstant, EmptyParent())
    case x: java.util.List[_] => new ListNode(x.asScala.toList.map(constructNode), EmptyParent())
    case x: java.util.Map[_, _] =>
      val body = x.asScala.toMap.map(x => x._1 match {
        case key: String => key -> constructNode(x._2)
        case _ => throw UnsupportedMappingKeyException(x._1, x._2)
      })
      new MappingNode(body , EmptyParent())
  }

  def apply[T](supplier: => T): Either[YamlException, T] = {
    try {
      Right(supplier)
    } catch {
      case e: YamlException => Left(e)
    }
  }
}
