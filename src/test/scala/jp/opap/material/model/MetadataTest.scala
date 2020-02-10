package jp.opap.material.model

import java.util.UUID

import jp.opap.data.yaml.Yaml
import jp.opap.material.Tests
import jp.opap.material.model.MetadataBundle.{AttachedMetadata, Mode}
import org.scalatest.FunSpec

class MetadataTest extends FunSpec {
  val id: UUID = UUID.randomUUID()

  describe("fromYaml") {
    it("妥当なメタデータ") {
      val data = Tests.getResourceAsStrean("model/metadata/valid.yaml")
      val actual = MetadataBundle.fromYaml(Yaml.parse(data), ComponentContext(UUID.randomUUID()), () => this.id)
      val expected = {
        val descendants = AttachedMetadata(Mode.Merging, Seq())
        val directory = AttachedMetadata(Mode.Merging, Seq("動画"))
        val items = Map(
          "bg_light.psd" -> AttachedMetadata(Mode.Overriding, Seq("背景")),
          "grid.ai" -> MetadataBundle.DEFAULT_METADATA,
          "kosys-logo.ai" -> AttachedMetadata(Mode.Merging, Seq("ロゴ")),
          "logo-anime.aep" -> AttachedMetadata(Mode.Merging, Seq("ロゴ")),
          "OP_C01.aep" -> MetadataBundle.DEFAULT_METADATA,
        )
        MetadataBundle(descendants, directory, items)
      }

      assert(actual._2 == expected)
    }
  }
}
