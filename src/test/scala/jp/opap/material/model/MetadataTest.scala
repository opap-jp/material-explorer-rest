package jp.opap.material.model

import java.util.UUID

import jp.opap.data.yaml.Yaml
import jp.opap.material.Tests
import org.scalatest.FunSpec

class MetadataTest extends FunSpec {
  val id: UUID = UUID.randomUUID()

  describe("fromYaml") {
    ignore("妥当なメタデータ") {
      val data = Tests.getResourceAsStrean("model/metadata/valid.yaml")
      val actual = MetadataBundle.fromYaml(Yaml.parse(data), ComponentContext(UUID.randomUUID()), () => this.id)
    }
  }
}
