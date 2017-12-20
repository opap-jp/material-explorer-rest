package jp.opap.material.data

import scala.annotation.tailrec
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

  def parameterized[A <: Any, T](items: java.util.List[A])(implicit t: scala.reflect.ClassTag[T]): Option[List[T]] = {
    @tailrec
    def p(items: List[Any], acc: List[T]): Option[List[T]] = items match {
      case x :: xs => if (t.runtimeClass.isInstance(x)) p(xs, x.asInstanceOf[T] :: acc) else Option.empty
      case List() => Option(acc)
      case _ => Option.empty
    }

    p(items.asScala.toList, List()).map(_.reverse)
  }

  implicit class Iterables[T](self: java.lang.Iterable[T]) {
    def toSeq: Seq[T] = self.asScala.toSeq
    def toIterable: Iterable[T] = self.asScala
  }

  implicit class EitherList[L, R](self: List[Either[L, R]]) {
    def leftRight: (List[L], List[R]) = (this.left, this.right)
    def left: List[L] = self.flatMap(element => element.left.toOption)
    def right: List[R] = self.flatMap(element => element.right.toOption)
  }
}
