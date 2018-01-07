package jp.opap.material.model

import java.io.File

import jp.opap.material.model.Manifest.{Category, Tag, TagGroup}
import org.scalatest.FunSpec

import scala.collection.SortedMap

class ManifestTest extends FunSpec {
  describe("fromYaml") {
    it("正常なタグ宣言") {
      val file = ManifestTest.getResourceFile("model/manifest/valid.yaml")
      val actual = Manifest.fromYaml(file)
      val expected = (List(), Manifest(
        List(
          TagGroup(Category.Common, "キャラクター", List(
            Tag.create(List("祝園アカネ", "アカネ"), None),
            Tag.create(List("少佐"), None),
            Tag.create(List("山家宏佳", "宏佳"), None),
            Tag.create(List("垂水結菜", "結菜"), None),
          )),
          TagGroup(Category.Author, Category.Author.defaultName.get, List(
            Tag.create(List("Butameron", "豚メロン", "井二かける", "Kakeru IBUTA", "IBUTA Kakeru"), None),
            Tag.create(List("水雪"), Option("藻")),
          )),
        )
      ))

      SortedMap(2 -> "foo", 1 -> "foo")
      assert(actual == expected)
    }

    it("同じタグ名があるとき、そのタグ名はタグ宣言全体から消去される") {
      val file = ManifestTest.getResourceFile("model/manifest/invalid-duplicated.yaml")
      val actual = Manifest.fromYaml(file)
      val expectedManifest = Manifest(
        List(
          TagGroup(Category.Common, "キャラクター", List(
            Tag.create(List("アカネ"), None),
          )),
          TagGroup(Category.Author, Category.Author.defaultName.get, List(
            Tag.create(List("豚メロン", "Kakeru IBUTA", "IBUTA Kakeru"), None),
            Tag.create(List("水雪"), None),
          )),
        )
      )

      assert(actual._1.head.message == "祝園アカネ - このラベルは重複しています。")
      assert(actual._1(1).message == "井二かける - このラベルは重複しています。")
      assert(actual._1(2).message == "藻 - このラベルは重複しています。")
      assert(actual._1(3).message == "Butameron - このラベルは重複しています。")

      assert(actual._2 == expectedManifest)
    }

    it("不正なカテゴリのタググループは、タグ宣言から消去される") {
      val file = ManifestTest.getResourceFile("model/manifest/invalid-category.yaml")
      val actual = Manifest.fromYaml(file)

      assert(actual._1.head.message == "tag_groups[0]: name が必要です。")
      assert(actual._1(1).message == "tag_groups[1]: foo - そのようなカテゴリはありません。")
      assert(actual._2 == Manifest(List()))
    }

    it("タグ名のリストが空のとき、その項目は無視される") {
      val file = ManifestTest.getResourceFile("model/manifest/invalid-empty.yaml")
      val actual = Manifest.fromYaml(file)

      val expected = (List(), Manifest(
        List(
          TagGroup(Category.Common, "キャラクター", List())
        )
      ))

      assert(actual == expected)
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

object ManifestTest {
  def getResourceFile(path: String): File = new File(ClassLoader.getSystemResource(path).toURI)
}
