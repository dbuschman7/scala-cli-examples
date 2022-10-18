package me.lightspeed7.examples.library

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class CollectionToIteratorTest extends AnyFunSuite with Matchers {

  test("iterate a list 3 times") {
    val list = Seq("a", "bb", "ccc", "ddddd")

    val iter: Iterator[String] = CollectionToIterator.toEndlessStream(list)

    val results: Seq[String] = (1 to 12).map(_ => iter.next())
    results.foreach(println)
    results.sorted.groupBy(in => in).map(_._2.size) mustBe Seq(3, 3, 3, 3)
  }

}
