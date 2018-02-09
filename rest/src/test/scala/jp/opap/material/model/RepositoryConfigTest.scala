package jp.opap.material.model

import jp.opap.data.yaml.Yaml
import jp.opap.material.Tests
import jp.opap.material.model.RepositoryConfig.GitlabRepositoryInfo
import org.scalatest.FunSpec

class RepositoryConfigTest extends FunSpec {
  describe("fromYaml") {
    it("妥当なリポジトリ設定") {
      val data = Tests.getResourceAsStrean("model/repository-config/valid.yaml")
      val actual = RepositoryConfig.fromYaml(Yaml.parse(data))
      val expected = (List(), RepositoryConfig(List(
        GitlabRepositoryInfo("kosys-ep01", "こうしす! #1", "https://gitlab.com", "kosys", "kosys-ep01")
      )))

      assert(actual == expected)
    }

    it("IDが不正であるとき、その項目は無視される") {
      val data = Tests.getResourceAsStrean("model/repository-config/invalid-id.yaml")
      val actual = RepositoryConfig.fromYaml(Yaml.parse(data))
      val expectedConfig = RepositoryConfig(List())

      assert(actual._1.head.message == "/repositories[0]: " + RepositoryConfig.WARNING_INVALID_ID.format("kosys\nkosys-ep01"))

      assert(actual._2 == expectedConfig)
    }

    it("同じIDがあるとき、それらの項目は消去される") {
      val data = Tests.getResourceAsStrean("model/repository-config/invalid-duplicated-id.yaml")
      val actual = RepositoryConfig.fromYaml(Yaml.parse(data))
      val expectedConfig = RepositoryConfig(List(
        GitlabRepositoryInfo("kosys-ep01", "こうしす! #1", "https://gitlab.com", "kosys", "kosys-ep01")
      ))

      assert(actual._1.head.message == RepositoryConfig.WARNING_DUPLICATED_ID.format("kosys-ep03"))
      assert(actual._1(1).message == RepositoryConfig.WARNING_DUPLICATED_ID.format("kosys-ep02"))
      assert(actual._2 == expectedConfig)
    }

    it("不正なプロトコルが指定されたとき、その項目は無視される") {
      val data = Tests.getResourceAsStrean("model/repository-config/invalid-protocol.yaml")
      val actual = RepositoryConfig.fromYaml(Yaml.parse(data))
      val expectedConfig = RepositoryConfig(List())

      assert(actual._1.head.message == "/repositories[0]: " + RepositoryConfig.WARNING_NO_SUCH_PROTOCOL.format("foo"))
      assert(actual._2 == expectedConfig)
    }
  }
}
