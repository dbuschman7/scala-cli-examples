package me.lightspeed7.examples.library

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class ScalaPackageTest extends AnyFunSuite with Matchers {

  test("scala file test") {
    val file = ScalaFile.make("/foo/bar/baz/Name.scala")
    file.fileName mustBe "Name"
    file.packageList mustBe Seq("foo", "bar", "baz")
  }

  test("scala package test") {
    val file = ScalaFile.make("/foo/bar/baz/Name.scala")

    val blah = ScalaPackage
      .make("/foo/bar/baz/blah")

    val pkg = ScalaPackage
      .make("/foo/bar/baz")
      .addfile(file)
      .addChild(blah)

    pkg.path mustBe Seq("foo", "bar", "baz")
    pkg.files.size mustBe 1
    pkg.files.map(_.fileName) mustBe Seq("Name")
    pkg.children.size mustBe 1
    pkg.children.flatMap(_.path).mkString("|") mustBe "foo|bar|baz|blah"
  }
}
