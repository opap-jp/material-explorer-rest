package jp.opap.data.yaml

import jp.opap.data.yaml.InternalNode.{ListNode, MappingNode}
import jp.opap.data.yaml.Leaf.{BigIntegerNode, BooleanNode, DateNode, DoubleNode, IntNode, LongNode, NullNode, StringNode, UndefinedNode}
import jp.opap.data.yaml.Parent.{ListParent, MappingParent}
import jp.opap.data.yaml.YamlException.{TypeException, UnsupportedMappingKeyException}
import org.scalatest.FunSpec
import jp.opap.material.Tests

class NodeTest extends FunSpec {
  describe("apply(String)") {
    it("throws TypeException when the node is not MappingNode") {
      assertThrows[TypeException] {
        getNode("string")("foo")
      }
    }
  }

  describe("mapping") {
    it("should return MappingNode when the node is MappingNode.") {
      assert(getNode("mapping").mapping.isInstanceOf[MappingNode])
    }

    it("should throw TypeException when the node is undefined.") {
      assertThrows[TypeException] {
        getNode("blahblah").mapping
      }
    }

    it("should throw TypeException when the node is null.") {
      assertThrows[TypeException] {
        getNode("null").mapping
      }
    }
  }

  describe("list") {
    it("should return ListNode when the node is ListNode.") {
      assert(getNode("list").list.isInstanceOf[ListNode])
    }

    it("should throw TypeException when the node is undefined.") {
      assertThrows[TypeException] {
        getNode("blahblah").list
      }
    }

    it("should throw TypeException when the node is null.") {
      assertThrows[TypeException] {
        getNode("null").list
      }
    }
  }

  describe("string") {
    it("should return StringNode when the node contains string.") {
      assert(getNode("string").string.isInstanceOf[StringNode])
    }

    it("should return UndefinedNode when the node is undefined.") {
      assert(getNode("blahblah").string.isInstanceOf[UndefinedNode[_]])
    }

    it("should return NullNode when the node contains is null.") {
      assert(getNode("null").string.isInstanceOf[NullNode[_]])
    }

    it("should throw TypeException when the node is ValueLeaf and its type is not appropriate.") {
      assertThrows[TypeException] {
        getNode("boolean").string
      }
    }
  }

  describe("boolean") {
    it("should return BooleanNode when the node contains boolean.") {
      assert(getNode("boolean").boolean.isInstanceOf[BooleanNode])
    }

    it("should return UndefinedNode when the node is undefined.") {
      assert(getNode("blahblah").boolean.isInstanceOf[UndefinedNode[_]])
    }

    it("should return NullNode when the node contains is null.") {
      assert(getNode("null").boolean.isInstanceOf[NullNode[_]])
    }

    it("should throw TypeException when the node is ValueLeaf and its type is not appropriate.") {
      assertThrows[TypeException] {
        getNode("int").boolean
      }
    }
  }

  describe("int") {
    it("should return IntNode when the node contains int.") {
      assert(getNode("int").int.isInstanceOf[IntNode])
    }

    it("should return UndefinedNode when the node is undefined.") {
      assert(getNode("blahblah").int.isInstanceOf[UndefinedNode[_]])
    }

    it("should return NullNode when the node contains is null.") {
      assert(getNode("null").int.isInstanceOf[NullNode[_]])
    }

    it("should throw TypeException when the node is ValueLeaf and its type is not appropriate.") {
      assertThrows[TypeException] {
        getNode("string").int
      }
    }
  }

  describe("long") {
    it("should return LongNode when the node contains long.") {
      assert(getNode("long").isInstanceOf[LongNode])
    }

    it("should return UndefinedNode when the node is undefined.") {
      assert(getNode("blahblah").long.isInstanceOf[UndefinedNode[_]])
    }

    it("should return NullNode when the node contains is null.") {
      assert(getNode("null").long.isInstanceOf[NullNode[_]])
    }

    it("should throw TypeException when the node is ValueLeaf and its type is not appropriate.") {
      assertThrows[TypeException] {
        getNode("string").long
      }
    }
  }

  describe("bigInteger") {
    it("should return BigIntegerNode when the node contains bigInteger.") {
      assert(getNode("big_integer").bigInteger.isInstanceOf[BigIntegerNode])
    }

    it("should return UndefinedNode when the node is undefined.") {
      assert(getNode("blahblah").bigInteger.isInstanceOf[UndefinedNode[_]])
    }

    it("should return NullNode when the node contains is null.") {
      assert(getNode("null").bigInteger.isInstanceOf[NullNode[_]])
    }

    it("should throw TypeException when the node is ValueLeaf and its type is not appropriate.") {
      assertThrows[TypeException] {
        getNode("string").bigInteger
      }
    }
  }

  describe("double") {
    it("should return DoubleNode when the node contains double.") {
      assert(getNode("double").double.isInstanceOf[DoubleNode])
    }

    it("should return UndefinedNode when the node is undefined.") {
      assert(getNode("blahblah").double.isInstanceOf[UndefinedNode[_]])
    }

    it("should return NullNode when the node contains is null.") {
      assert(getNode("null").double.isInstanceOf[NullNode[_]])
    }

    it("should throw TypeException when the node is ValueLeaf and its type is not appropriate.") {
      assertThrows[TypeException] {
        getNode("string").double
      }
    }
  }

  describe("date") {
    it("should return DateNode when the node contains date.") {
      assert(getNode("date").date.isInstanceOf[DateNode])
    }

    it("should return UndefinedNode when the node is undefined.") {
      assert(getNode("blahblah").date.isInstanceOf[UndefinedNode[_]])
    }

    it("should return NullNode when the node contains is null.") {
      assert(getNode("null").date.isInstanceOf[NullNode[_]])
    }

    it("should throw TypeException when the node is ValueLeaf and its type is not appropriate.") {
      assertThrows[TypeException] {
        getNode("string").date
      }
    }
  }

  describe("ancestors") {
    it("should returns ancestors.") {
      val sut = getNode("list").list.head.ancestors
      assert(sut.size == 3)

      assert(sut.head.asInstanceOf[MappingParent].key == "types")
      assert(sut(1).asInstanceOf[MappingParent].key == "list")
      assert(sut(2).asInstanceOf[ListParent].index == 0)
    }
  }

  describe("location") {
    it("should returns location.") {
      val actual = getNode("list").list.head.location
      assert(actual == """["types"]["list"][0]""")
    }
  }


  def getNode(key: String): Node = {
    val data = Tests.getResourceAsStrean("data/yaml/types.yaml")
    Yaml.parse(data)("types")(key)
  }
}
