package jp.opap.material.data

import java.time.{ LocalDateTime, ZoneId}
import java.util.Date

object Formats {
  implicit class Dates(self: Date) {
    def toLocal: LocalDateTime = self.toInstant.atZone(ZoneId.systemDefault()).toLocalDateTime
  }
}
