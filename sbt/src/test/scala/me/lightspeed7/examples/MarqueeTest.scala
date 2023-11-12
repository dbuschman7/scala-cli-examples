package me.lightspeed7.examples

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

import scala.collection.mutable

final case class Term(term: String, level: Int, children: mutable.HashMap[String, Term] = mutable.HashMap.empty) {
  def toShortString: String = s"$term - $level - Children - ${children.size}"

  def nodeCount: Int = 1 + children.map(_._2.nodeCount).sum
}

object TermBuilder {
  @scala.annotation.tailrec
  final def placePath(position: Int,
                      path: Seq[String],
                      lookup: Term): Unit = {
    println(s"placeWord - $position - $path - ${lookup.toShortString}")
    if (path.nonEmpty) {
      val added = lookup.children.getOrElseUpdate(path.head, Term(path.head, position))
      if (path.length > 1) {
        placePath(position + 1, path.tail, added)
      }
    }
  }
}

class TermTest extends AnyFunSuite with Matchers {

  test("marquee runs successfully") {

    val paths = Seq("foo/bar/baz", "foo/bar/foo", "bar/foo/baz")
    val root = Term("root", 0)

    paths.foreach(path => TermBuilder.placePath(0, path.split("/"), root))

    root.nodeCount mustBe 7 + 1 // Plus root
    root.children.size mustBe 2

    root.children("foo").children.size mustBe 1
    root.children("foo").children("bar").children.size mustBe 2
    root.children("bar").children("foo").children.size mustBe 1
    root.children("foo").children("bar").children("baz").children.size mustBe 0
    root.children("foo").children("bar").children("foo").children.size mustBe 0

    root.children("bar").children.size mustBe 1
    root.children("bar").children("foo").children.size mustBe 1
    root.children("bar").children("foo").children("baz").children.size mustBe 0
  }
}
