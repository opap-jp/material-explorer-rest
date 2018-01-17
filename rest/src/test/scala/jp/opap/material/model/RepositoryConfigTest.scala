package jp.opap.material.model

import jp.opap.material.Tests
import jp.opap.material.model.RepositoryConfig.GitlabRepositoryInfo
import org.scalatest.FunSpec

class RepositoryConfigTest extends FunSpec {
  describe("fromYaml") {
    it("妥当なリポジトリ設定") {
      val file = Tests.getResourceFile("model/repository-config/valid.yaml")
      val actual = RepositoryConfig.fromYaml(file)
      val expected = (List(), RepositoryConfig(List(
        GitlabRepositoryInfo("kosys-ep01", "こうしす! #1", "https://gitlab.com", "kosys", "kosys-ep01")
      )))

      assert(actual == expected)
    }

    it("IDが不正であるとき、その項目は無視される") {
      val file = Tests.getResourceFile("model/repository-config/invalid-id.yaml")
      val actual = RepositoryConfig.fromYaml(file)
      val expectedConfig = RepositoryConfig(List())

      assert(actual._1.head.message == "repositories[0]: " + RepositoryConfig.WARNING_INVALID_ID.format("kosys\nkosys-ep01"))

      assert(actual._2 == expectedConfig)
    }

    it("同じIDがあるとき、それらの項目は消去される") {
      val file = Tests.getResourceFile("model/repository-config/invalid-duplicated-id.yaml")
      val actual = RepositoryConfig.fromYaml(file)
      val expectedConfig = RepositoryConfig(List(
        GitlabRepositoryInfo("kosys-ep01", "こうしす! #1", "https://gitlab.com", "kosys", "kosys-ep01")
      ))

      assert(actual._1.head.message == RepositoryConfig.WARNING_DUPLICATED_ID.format("kosys-ep03"))
      assert(actual._1(1).message == RepositoryConfig.WARNING_DUPLICATED_ID.format("kosys-ep02"))
      assert(actual._2 == expectedConfig)
    }
  }
}
