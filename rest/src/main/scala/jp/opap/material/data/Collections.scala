package jp.opap.material.data

import scala.collection.JavaConverters._

object Collections {
  implicit class Iterables[T](self: java.lang.Iterable[T]) {
    def toSeq: Seq[T] = self.asScala.toSeq
  }
}
