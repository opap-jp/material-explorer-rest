package jp.opap.material.model

import java.io.File

import jp.opap.material.model.Manifest.{Category, Tag, TagGroup, TagName}
import org.scalatest.FunSpec

class ManifestTest extends FunSpec {
  describe("fromYaml") {
    it("正常") {
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

      assert(actual == expected)
    }

    ignore("名前に重複がある") {
      ???
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
