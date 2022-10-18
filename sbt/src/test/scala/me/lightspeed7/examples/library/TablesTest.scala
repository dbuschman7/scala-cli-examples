package me.lightspeed7.examples.library

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

import scala.util.{Failure, Success, Try}

class TablesTest extends AnyFunSuite with Matchers {

  test("foo") {

    val line1 = Map("1001" -> "123456", "1003" -> "234", "name" -> "us-east-1", "p90" -> "123")
    val line2 = Map("1002" -> "123456", "1003" -> "123", "name" -> "us-east-2", "p90" -> "234")

    def fmt(in: String): String = Try(in.toLong) match {
      case Failure(_) => in
      case Success(num) => PrettyPrint.number(num)
    }


    val tableDef = PreFormattedBuild[Map[String, String]]("table")
      .column("name") { line => fmt(line.getOrElse("name", "")) }
      .column("1001") { line => fmt(line.getOrElse("1001", "")) }
      .column("1002") { line => fmt(line.getOrElse("1002", "")) }
      .column("1003") { line => fmt(line.getOrElse("1003", "")) }
      .column("p90") { line => fmt(line.getOrElse("p90", "")) }

    val str = tableDef.generate(Seq(line1, line2))
    println(str)
  }

}
