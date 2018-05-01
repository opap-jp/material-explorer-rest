package jp.opap.material.model

import jp.opap.data.yaml.Yaml
import jp.opap.material.Tests
import jp.opap.material.model.Manifest.{Category, ExtensionSetPredicate, Inclusive, Selector, TagGroup}
import org.scalatest.FunSpec

class ManifestTest extends FunSpec {
  describe("fromYaml") {
    it("妥当なタグ宣言") {
      val data = Tests.getResourceAsStrean("model/manifest/valid.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data))
      val expected = (List(), Manifest(
        List(
          TagGroup(Category.Author, Category.Author.defaultName.get, List(
            Tag.create(List("Butameron", "豚メロン", "井二かける", "Kakeru IBUTA", "IBUTA Kakeru"), None),
            Tag.create(List("水雪"), Some("藻")),
          )),
          TagGroup(Category.Common, "キャラクター", List(
            Tag.create(List("祝園アカネ", "アカネ"), None),
            Tag.create(List(), Some("少佐")),
            Tag.create(List("山家宏佳", "宏佳"), None),
            Tag.create(List("垂水結菜", "結菜"), None),
          )),
        ), List(Selector(Inclusive, ExtensionSetPredicate("psd,ai,png,jpg,jpeg,pdf,wav,flac,mp3,blender".split(","))))
      ))

      assert(actual == expected)
    }

    it("同じタグ名があるとき、そのタグ名はタグ宣言全体から消去される") {
      val data = Tests.getResourceAsStrean("model/manifest/invalid-duplicated.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data))
      val expectedManifest = Manifest(
        List(
          TagGroup(Category.Common, "キャラクター", List(
            Tag.create(List("アカネ"), None),
          )),
          TagGroup(Category.Author, Category.Author.defaultName.get, List(
            Tag.create(List("豚メロン", "Kakeru IBUTA", "IBUTA Kakeru"), None),
            Tag.create(List("水雪"), None),
          )),
        ), List()
      )

      assert(actual._1.head.message == Manifest.WARNING_DUPLICATED_NAME.format("祝園アカネ"))
      assert(actual._1(1).message == Manifest.WARNING_DUPLICATED_NAME.format("井二かける"))
      assert(actual._1(2).message == Manifest.WARNING_DUPLICATED_NAME.format("藻"))
      assert(actual._1(3).message == Manifest.WARNING_DUPLICATED_NAME.format("Butameron"))

      assert(actual._2 == expectedManifest)
    }

    it("不正なカテゴリのタググループは、タグ宣言から消去される") {
      val data = Tests.getResourceAsStrean("model/manifest/invalid-category.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data))

      assert(actual._1.head.message == "/tag_groups[0]: " + Manifest.WARNING_CATEGORY_NAME_REQUIRED.format("common"))
      assert(actual._1(1).message == "/tag_groups[1]: " + Manifest.WARNING_NO_SUCH_CATEGORY_EXISTS.format("foo"))
      assert(actual._2 == Manifest(List(), List()))
    }

    it("タグ名のリストが空のとき、その項目は無視され、警告が出力される") {
      val data = Tests.getResourceAsStrean("model/manifest/invalid-empty.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data))

      val expectedManifest = Manifest(List(TagGroup(Category.Common, "キャラクター", List())), List())

      assert(actual._1.head.message == "/tag_groups[0]/tags[0]: " + Manifest.WARNING_EMPTY_NAME)
      assert(actual._2 == expectedManifest)
    }

    it("tag_groups がないとき、警告が出力される") {
      val data = Tests.getResourceAsStrean("model/manifest/invalid-missing-tag-groups.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data))

      assert(actual._1.head.message == ": " + WARNING_KEY_REQUIRED.format("tag_groups"))
      assert(actual._2 == Manifest(List(), List()))
    }

    it("selectors がないとき、警告が出力される") {
      val data = Tests.getResourceAsStrean("model/manifest/invalid-missing-selectors.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data))

      assert(actual._1.head.message == ": " + WARNING_KEY_REQUIRED.format("selectors"))
      assert(actual._2 == Manifest(List(), List()))
    }

    it("ファイルセレクタに include も exclude もないとき、警告が出力される") {
      val data = Tests.getResourceAsStrean("model/manifest/invalid-selector.yaml")
      val actual = Manifest.fromYaml(Yaml.parse(data))

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
