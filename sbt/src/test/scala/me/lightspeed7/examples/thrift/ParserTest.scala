package me.lightspeed7.examples.thrift

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

import scala.util.matching.Regex

class ParserTest extends AnyFunSuite with Matchers with TestFiles {

  debugLogging = true

  test("full thrift file") {

    loadFile(getLibraryTestFilePath("sbt", "example.thrift")) match {
      case Left(value) => fail(value)
      case Right(file) =>
        file.namespaces.size mustBe 1
        file.consts.size mustBe 1
        file.typeDefs.size mustBe 3
        file.structs.size mustBe 1
        file.services.size mustBe 1
        file.enums.size mustBe 1
        file.includes.size mustBe 1

        logIt("Consts **************************")
        file.consts.foreach(println)

        logIt("Typedefs **************************")
        file.typeDefs.foreach(println)

        logIt("Namespaces **************************")
        file.namespaces.foreach(println)

        logIt("Enums **************************")
        file.enums.foreach(println)

        logIt("Structs **************************")
        file.structs.foreach(println)

        logIt("Includes **************************")
        file.includes.foreach(println)

        logIt("Services **************************")
        file.services.foreach(println)
    }
  }

  test("includes object") {
    parseWith(Parser.includeObj)("""include "types/some_dir/foo.thrift"""") mustBe ThriftInclude("foo.thrift", Seq("types", "some_dir"))
  }

  test("typedef object") {
    parseWith(Parser.typeDefObj)("typedef string S3URL") mustBe ThriftTypedef("S3URL", DataType("string", None, None))
    parseWith(Parser.typeDefObj)("typedef list<S3URL>  urlList") mustBe ThriftTypedef("urlList", DataType("list", Some("S3URL"), None))
    parseWith(Parser.typeDefObj)("typedef map<some_dir.Type, S3URL> someMap") mustBe ThriftTypedef("someMap", DataType("map", Some("some_dir.Type"), Some("S3URL")))
  }


  test("datatype object") {
    parseWith(Parser.dataType)("i32") mustBe DataType("i32", None, None)
    parseWith(Parser.dataType)("set<i32>") mustBe DataType("set", Some("i32"), None)
    parseWith(Parser.dataType)("map<some_dir.Type, S3URL> ") mustBe DataType("map", Some("some_dir.Type"), Some("S3URL"))
    parseWith(Parser.dataType)("optional i32") mustBe DataType("i32", None, None, optional = true)
  }

  test("const object") {
    parseWith(Parser.constObj)("const bool trueForNot") mustBe ThriftConst("trueForNot", DataType("bool", None, None), None)
    parseWith(Parser.constObj)("const i64 LongLargeValue = 23452345234523452345") mustBe ThriftConst("LongLargeValue", DataType("i64", None, None), Some("23452345234523452345"))
  }

  test("service object") {

    val methodCall1 = ThriftMethod("get_last_sale", "TradeReport", //
      Seq(ThriftDateLine("Symbol", DataType("string"), 1), ThriftDateLine("status", DataType("TradeType"), 2)), //
      Seq() //
    )

    val methodCall2 = methodCall1.copy(exception = Seq(ThriftDateLine("ex", DataType("service.ServiceException"), 1)))

    parseWith(Parser.serviceMethod)("TradeReport get_last_sale(1: string Symbol, 2:  TradeType status)") mustBe methodCall1
    parseWith(Parser.serviceMethod)("TradeReport get_last_sale(1: string Symbol, 2:  TradeType status) throws (1: service.ServiceException ex)") mustBe methodCall2

    parseWith(Parser.serviceObj) {
      """service TradeHistory {
        |    TradeReport get_last_sale(1: string Symbol, 2:  TradeType status)
        |}""".stripMargin
    } mustBe ThriftService("TradeHistory", Seq(methodCall1))

    parseWith(Parser.serviceObj) {
      """service TradeHistory extends another_dir.BaseService {
        |    TradeReport get_last_sale(1: string Symbol, 2:  TradeType status) throws (1: service.ServiceException ex)
        |}""".stripMargin
    } mustBe ThriftService("TradeHistory", Seq(methodCall2), Some("another_dir.BaseService"))

  }

  test("struct object") {
    parseWith(Parser.dataLine)("1: string  symbol,") mustBe ThriftDateLine("symbol", DataType("string"), 1)
    parseWith(Parser.dataLine)("4: i32     seq_num") mustBe ThriftDateLine("seq_num", DataType("i32"), 4)
    parseWith(Parser.dataLine)("4: optional i32 optInt") mustBe ThriftDateLine("optInt", DataType("i32", optional = true), 4)
  }

  test("enum tests") {
    parseWith(Parser.enumEntry)("GOOD = 1,") mustBe ThriftEnumEntry("GOOD", 1)
    parseWith(Parser.enumEntry)("BAD = 2") mustBe ThriftEnumEntry("BAD", 2)

    val eunmObj = parseWith(Parser.enumObj)(
      """enum TradeType {
        |    GOOD = 1
        |    BAD = 2
        |    UGLY = 4
        |}
        |""".stripMargin)

    eunmObj.name mustBe "TradeType"
    eunmObj.entries.size mustBe 3
    println(eunmObj)
  }

  test("namespace tests") {
    parseWith(Parser.namespaceObj)("namespace * TradeReporting") mustBe ThriftNamespace("TradeReporting", None)
    parseWith(Parser.namespaceObj)("namespace scala TradeReporting") mustBe ThriftNamespace("TradeReporting", Some("scala"))
  }


  test("simple tokens") {
    Parser.integerRegex.findAllMatchIn("1").headMatch.map(_.toInt) mustBe Some(1)
    Parser.integerRegex.findAllMatchIn("234").headMatch.map(_.toInt) mustBe Some(234)
    Parser.integerRegex.findAllMatchIn("a1").headMatch.map(_.toInt) mustBe None

    Parser.openBraceRegex.findAllMatchIn("{").headMatch mustBe Some("{")
    Parser.openBraceRegex.findAllMatchIn("*").headMatch mustBe None

    Parser.closeBraceRegex.findAllMatchIn("}").headMatch mustBe Some("}")
    Parser.closeBraceRegex.findAllMatchIn("*").headMatch mustBe None

    Parser.openParenRegex.findAllMatchIn("(").headMatch mustBe Some("(")
    Parser.openParenRegex.findAllMatchIn("*").headMatch mustBe None

    Parser.closeParenRegex.findAllMatchIn(")").headMatch mustBe Some(")")
    Parser.closeParenRegex.findAllMatchIn("*").headMatch mustBe None

    Parser.colonRegex.findAllMatchIn(":").headMatch mustBe Some(":")
    Parser.colonRegex.findAllMatchIn("*").headMatch mustBe None

    Parser.equalsRegex.findAllMatchIn("=").headMatch mustBe Some("=")
    Parser.equalsRegex.findAllMatchIn("*").headMatch mustBe None

    Parser.wildcardRegex.findAllMatchIn("*").headMatch mustBe Some("*")
    Parser.wildcardRegex.findAllMatchIn("=").headMatch mustBe None

    Parser.identRegex.findAllMatchIn("TradeReport").headMatch mustBe Some("TradeReport")
    Parser.identRegex.findAllMatchIn("namespace").headMatch mustBe Some("namespace")
    Parser.identRegex.findAllMatchIn("enum").headMatch mustBe Some("enum")
    Parser.identRegex.findAllMatchIn("TradeType").headMatch mustBe Some("TradeType")

    Parser.identRegex.findAllMatchIn("i32").headMatch mustBe Some("i32")
    Parser.identRegex.findAllMatchIn("TradeType").headMatch mustBe Some("TradeType")
    Parser.identRegex.findAllMatchIn("get_last_sale").headMatch mustBe Some("get_last_sale")

    Parser.identRegex.findAllMatchIn("1").headMatch mustBe None
    Parser.identRegex.findAllMatchIn(":").headMatch mustBe None
    Parser.identRegex.findAllMatchIn("{").headMatch mustBe None
    Parser.identRegex.findAllMatchIn("}").headMatch mustBe None


    Parser.namespaceRegex.findAllMatchIn("namespace ").headMatch mustBe Some("namespace")
    Parser.namespaceRegex.findAllMatchIn("service ").headMatch mustBe None

    Parser.serviceRegex.findAllMatchIn("namespace ").headMatch mustBe None
    Parser.serviceRegex.findAllMatchIn("service ").headMatch mustBe Some("service")

    Parser.structRegex.findAllMatchIn("struct ").headMatch mustBe Some("struct")
    Parser.structRegex.findAllMatchIn("enum ").headMatch mustBe None

    Parser.enumKeywordRegex.findAllMatchIn("struct ").headMatch mustBe None
    Parser.enumKeywordRegex.findAllMatchIn("enum ").headMatch mustBe Some("enum")

    Parser.quoteRegex.findAllMatchIn(""""foo"""").headMatch mustBe Some(""""""")
    Parser.quoteRegex.findAllMatchIn("""'foo'""").headMatch mustBe Some("""'""")

    Parser.includeRegex.findAllMatchIn("include foo").headMatch mustBe Some("include")
  }


  def parseWith[T](parser: Parser.Parser[T])(payload: String): T = {
    Parser.parse(parser)(payload) match {
      case Left(value) => fail(value)
      case Right(call) => call
    }
  }

  implicit class Cleanup(matches: Iterator[Regex.Match]) {
    def headMatch: Option[String] = matches.toList.headOption.map(_.group(0))
  }
}
