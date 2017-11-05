package jp.opap.material.model

import java.time.LocalDateTime

import scala.beans.BeanProperty

case class Project(@BeanProperty id: String, @BeanProperty name: String, @BeanProperty lastActivityAt: LocalDateTime)
