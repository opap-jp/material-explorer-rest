package jp.opap.material.model

import jp.opap.material.model.RepositoryConfig.RepositoryInfo

import scala.beans.BeanProperty

/**
  * 取得されたリポジトリを表現する型です。
  *
  * @param headHash このリポジトリの HEAD のコミットハッシュ
  */
// TODO: name は必要か？（GitLab でしか使わない）
case class Repository(@BeanProperty id: String, @BeanProperty name: String, @BeanProperty title: String,
  @BeanProperty headHash: String) extends RepositoryInfo
