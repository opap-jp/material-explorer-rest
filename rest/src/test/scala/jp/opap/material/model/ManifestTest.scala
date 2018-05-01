package jp.opap.material.model

import java.util.UUID

import jp.opap.data.yaml.Yaml
import jp.opap.material.Tests
import jp.opap.material.model.Manifest.{Kind, ExtensionSetPredicate, Inclusive, Selector, TagGroup}
import org.scalatest.FunSpec

class ManifestTest extends FunSpec {
  val id: UUID = UUID.randomUUID()

  describe("fromYaml") {
    it("妥当なタグ宣言") {
      val data = Tests.getResourceAsStrean("model/manifest/valid.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data), () => this.id)
      val expected = (List(), Manifest(
        List(
          TagGroup(Kind.Author, Kind.Author.defaultName.get, List(
            new Tag(List("Butameron", "豚メロン", "井二かける", "Kakeru IBUTA", "IBUTA Kakeru"), None, () => this.id),
            new Tag(List("水雪"), Some("藻"), () => this.id),
          )),
          TagGroup(Kind.Common, "キャラクター", List(
            new Tag(List("祝園アカネ", "アカネ"), None, () => this.id),
            new Tag(List(), Some("少佐"), () => this.id),
            new Tag(List("山家宏佳", "宏佳"), None, () => this.id),
            new Tag(List("垂水結菜", "結菜"), None, () => this.id),
          )),
        ), List(Selector(Inclusive, ExtensionSetPredicate("psd,ai,png,jpg,jpeg,pdf,wav,flac,mp3,blender".split(","))))
      ))

      assert(actual == expected)
    }

    it("同じタグ名があるとき、そのタグ名はタグ宣言全体から消去される") {
      val data = Tests.getResourceAsStrean("model/manifest/invalid-duplicated.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data), () => this.id)
      val expectedManifest = Manifest(
        List(
          TagGroup(Kind.Author, Kind.Author.defaultName.get, List(
            new Tag(List("豚メロン", "Kakeru IBUTA", "IBUTA Kakeru"), None, () => this.id),
            new Tag(List("水雪"), None, () => this.id),
          )),
          TagGroup(Kind.Common, "キャラクター", List(
            new Tag(List("アカネ"), None, () => this.id),
          )),
        ), List()
      )

      assert(actual._1.head.message == Manifest.WARNING_DUPLICATED_NAME.format("Butameron"))
      assert(actual._1(1).message == Manifest.WARNING_DUPLICATED_NAME.format("井二かける"))
      assert(actual._1(2).message == Manifest.WARNING_DUPLICATED_NAME.format("藻"))
      assert(actual._1(3).message == Manifest.WARNING_DUPLICATED_NAME.format("祝園アカネ"))

      assert(actual._2 == expectedManifest)
    }

    it("不正なカテゴリのタググループは、タグ宣言から消去される") {
      val data = Tests.getResourceAsStrean("model/manifest/invalid-category.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data), () => this.id)

      assert(actual._1.head.message == "/tag_groups[0]: " + Manifest.WARNING_GROUP_NAME_REQUIRED.format("common"))
      assert(actual._1(1).message == "/tag_groups[1]: " + Manifest.WARNING_NO_SUCH_KIND_EXISTS.format("foo"))
      assert(actual._2 == Manifest(List(), List()))
    }

    it("タグ名のリストが空のとき、その項目は無視され、警告が出力される") {
      val data = Tests.getResourceAsStrean("model/manifest/invalid-empty.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data), () => this.id)

      val expectedManifest = Manifest(List(TagGroup(Kind.Common, "キャラクター", List())), List())

      assert(actual._1.head.message == "/tag_groups[0]/tags[0]: " + Manifest.WARNING_NAME_REQUIRED)
      assert(actual._2 == expectedManifest)
    }

    it("tag_groups がないとき、警告が出力される") {
      val data = Tests.getResourceAsStrean("model/manifest/invalid-missing-tag-groups.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data), () => this.id)

      assert(actual._1.head.message == ": " + WARNING_KEY_REQUIRED.format("tag_groups"))
      assert(actual._2 == Manifest(List(), List()))
    }

    it("selectors がないとき、警告が出力される") {
      val data = Tests.getResourceAsStrean("model/manifest/invalid-missing-selectors.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data), () => this.id)

      assert(actual._1.head.message == ": " + WARNING_KEY_REQUIRED.format("selectors"))
      assert(actual._2 == Manifest(List(), List()))
    }

    it("ファイルセレクタに include も exclude もないとき、警告が出力される") {
      val data = Tests.getResourceAsStrean("model/manifest/invalid-selector.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data), () => this.id)

      assert(actual._1.head.message == "/selectors[0]: " + Manifest.WARNING_SELECTOR_MODE_REQUIRED)
      assert(actual._2 == Manifest(List(), List()))
    }
  }

  describe("normalize") {
    it ("全角英数は半角英数に変換される") {
      val actual = Manifest.normalize("ｆｏｏbar４２10")
      assert(actual == "foobar4210")
    }

    it ("大文字は小文字に変換され、半角スペースは削除される") {
      val actual = Manifest.normalize("IBUTA Kakeru")
      assert(actual == "ibutakakeru")
    }

    it ("カタカナはひらがなに変換され、全角スペースは削除される") {
      val actual = Manifest.normalize("祝園　アカネ")
      assert(actual == "祝園あかね")
    }
  }
}
