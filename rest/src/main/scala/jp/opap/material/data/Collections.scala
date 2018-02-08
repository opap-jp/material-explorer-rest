package jp.opap.material.data

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.immutable.TreeMap

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

  implicit class Seqs[T](self: Seq[T]) {
    def groupByOrdered[P](f: T => P): Seq[(P, Seq[T])] = {
      val groups = self.groupBy(f)
      @tailrec
      def accumulate(a: List[(P, Seq[T])], done: Set[P], items: List[T]): List[(P, Seq[T])] = {
        items match {
          case head :: tail =>
            val key = f(head)
            if (done.contains(key))
              accumulate(a, done, tail)
            else
              accumulate((key, groups(key)) :: a, done + key, tail)
          case Nil => a
        }
      }
      accumulate(List(), Set(), self.toList).reverse
    }
  }

  implicit class EitherSeq[L, R](self: Seq[Either[L, R]]) {
    def leftRight: (Seq[L], Seq[R]) = (this.left, this.right)
    def left: Seq[L] = self.flatMap(element => element.left.toOption)
    def right: Seq[R] = self.flatMap(element => element.right.toOption)
  }
}
