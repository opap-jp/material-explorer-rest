package jp.opap.material.model

import java.io.File

import jp.opap.material.model.Manifest.{Category, Tag, TagGroup}
import org.scalatest.FunSuite

class ManifestTest extends FunSuite {
  test("正常なタグ宣言ファイル") {
    val file = ManifestTest.getResourceFile("model/manifest/valid.yaml")
    val actual = Manifest.fromYaml(file)
    val expected = (List(), Manifest(
      List(
        TagGroup(Category.Common, "キャラクター", List(
          Tag(List("祝園アカネ", "アカネ"), None),
          Tag(List("少佐"), None),
          Tag(List("山家宏佳", "宏佳"), None),
          Tag(List("垂水結菜", "結菜"), None),
        )),
        TagGroup(Category.Author, Category.Author.defaultName.get, List(
          Tag(List("Butameron", "豚メロン", "井二かける", "Kakeru IBUTA", "IBUTA Kakeru"), None),
          Tag(List("水雪"), Option("藻")),
        )),
      )
    ))

    assert(actual == expected)
  }
}

object ManifestTest {
  def getResourceFile(path: String): File = new File(ClassLoader.getSystemResource(path).toURI)
}
