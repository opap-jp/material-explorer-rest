package jp.opap.data.yaml

import java.time.Instant

import jp.opap.data.yaml.NodeTest.getNode
import jp.opap.data.yaml.YamlException.TypeException
import org.scalatest.FunSpec

class LeafTest extends FunSpec {
  describe("get") {
    it("should return an appropriate value if the node is ValueNode.") {
      assert(getNode("string").string.get == "foo")
      assert(getNode("boolean").boolean.get)
      assert(getNode("int").int.get == 42)
      assert(getNode("long").long.get == 12345678900L)
      assert(getNode("big_integer").bigInteger.get == BigInt("12345678901234567890"))
      assert(getNode("double").double.get == 3.14d)
      assert(getNode("date").date.get == Instant.parse("2018-01-01T12:34:56Z"))
    }

    it("should throw TypeException if the node is UndefinedNode.") {
      assertThrows[TypeException] { getNode("blahblah").string.get }
      assertThrows[TypeException] { getNode("blahblah").boolean.get }
      assertThrows[TypeException] { getNode("blahblah").int.get }
      assertThrows[TypeException] { getNode("blahblah").long.get }
      assertThrows[TypeException] { getNode("blahblah").bigInteger.get }
      assertThrows[TypeException] { getNode("blahblah").double.get }
      assertThrows[TypeException] { getNode("blahblah").date.get }
    }

    it("should throw TypeException if the node is NullNode.") {
      assertThrows[TypeException] { getNode("null").string.get }
      assertThrows[TypeException] { getNode("null").boolean.get }
      assertThrows[TypeException] { getNode("null").int.get }
      assertThrows[TypeException] { getNode("null").long.get }
      assertThrows[TypeException] { getNode("null").bigInteger.get }
      assertThrows[TypeException] { getNode("null").double.get }
      assertThrows[TypeException] { getNode("null").date.get }
    }
  }

  describe("option") {
    it("should return Option which contains an appropriate value if the node is ValueNode.") {
      assert(getNode("string").string.option.get == "foo")
      assert(getNode("boolean").boolean.option.get)
      assert(getNode("int").int.option.get == 42)
      assert(getNode("long").long.option.get == 12345678900L)
      assert(getNode("big_integer").bigInteger.option.get == BigInt("12345678901234567890"))
      assert(getNode("double").double.option.get == 3.14d)
      assert(getNode("date").date.option.get == Instant.parse("2018-01-01T12:34:56Z"))
    }

    it("should return an empty Option if the node is UndefinedNode.") {
      assert(getNode("blahblah").string.option.isEmpty)
      assert(getNode("blahblah").boolean.option.isEmpty)
      assert(getNode("blahblah").int.option.isEmpty)
      assert(getNode("blahblah").long.option.isEmpty)
      assert(getNode("blahblah").bigInteger.option.isEmpty)
      assert(getNode("blahblah").double.option.isEmpty)
      assert(getNode("blahblah").date.option.isEmpty)
    }

    it("should return an empty Option if the node is NullNode.") {
      assert(getNode("null").string.option.isEmpty)
      assert(getNode("null").boolean.option.isEmpty)
      assert(getNode("null").int.option.isEmpty)
      assert(getNode("null").long.option.isEmpty)
      assert(getNode("null").bigInteger.option.isEmpty)
      assert(getNode("null").double.option.isEmpty)
      assert(getNode("null").date.option.isEmpty)
    }
  }
}

object LeafTest {
  def getLeaf(key: String): Leaf[Any] = getNode(key).asInstanceOf[Leaf[Any]]
}
