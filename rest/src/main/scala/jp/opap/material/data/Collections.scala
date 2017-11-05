package jp.opap.material.data

import com.fasterxml.jackson.annotation.JsonProperty

import scala.collection.JavaConverters._

object Collections {
  implicit class Iterables[T](self: java.lang.Iterable[T]) {
    def toSeq: Seq[T] = self.asScala.toSeq
  }
}
