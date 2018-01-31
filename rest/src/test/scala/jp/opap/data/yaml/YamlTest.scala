package jp.opap.data.yaml

import jp.opap.data.yaml.Leaf.{BigIntegerNode, BooleanNode, DateNode, DoubleNode, IntNode, LongNode, NullNode, StringNode}
import jp.opap.data.yaml.YamlException.UnsupportedMappingKeyException
import org.scalatest.FunSpec
import jp.opap.material.Tests

class YamlTest extends FunSpec {

  describe("constructNode") {
    it("should return a node which corresponds to each object type.") {
      val data = Tests.getResourceAsStrean("data/yaml/types.yaml")
      val sut = Yaml.parse(data)("types")

      assert(sut("null").isInstanceOf[NullNode[_]])
      assert(sut("string").isInstanceOf[StringNode])
      assert(sut("boolean").isInstanceOf[BooleanNode])
      assert(sut("int").isInstanceOf[IntNode])
      assert(sut("long").isInstanceOf[LongNode])
      assert(sut("big_integer").isInstanceOf[BigIntegerNode])
      assert(sut("double").isInstanceOf[DoubleNode])
      assert(sut("date").isInstanceOf[DateNode])
    }

    it("should throw UnsupportedMappingKeyException when document contains a mapping which has non-string key.") {
      val data = Tests.getResourceAsStrean("data/yaml/non-string-key-mapping.yaml")
      assertThrows[UnsupportedMappingKeyException] {
        Yaml.parse(data)
      }
    }
  }
}
