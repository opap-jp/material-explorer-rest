package jp.opap.material.model

import java.time.LocalDateTime

import scala.beans.BeanProperty

case class Repository(@BeanProperty id: String, @BeanProperty name: String, @BeanProperty title: String,
  @BeanProperty lastActivityAt: LocalDateTime, @BeanProperty headHash: String)