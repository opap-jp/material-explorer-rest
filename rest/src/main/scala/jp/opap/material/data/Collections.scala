package jp.opap.material.data

import scala.collection.JavaConverters._

object Collections {
  /**
    * Java Iterator を取得する関数 から、Iterable を取得します。
    *
    * @param iteratorSupplier Java Iterator を取得する関数
    * @tparam T Iterable の要素の型
    * @return Iterable
    */
  def iterableOf[T](iteratorSupplier: () => java.util.Iterator[T]): Iterable[T] = {
    val javaIterable: java.lang.Iterable[T] = () => iteratorSupplier()
    javaIterable.toIterable
  }

  implicit class Iterables[T](self: java.lang.Iterable[T]) {
    def toSeq: Seq[T] = self.asScala.toSeq
    def toIterable: Iterable[T] = self.asScala
  }
}
