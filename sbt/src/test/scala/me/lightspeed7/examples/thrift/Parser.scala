package me.lightspeed7.examples.thrift

import me.lightspeed7.examples.library.FileIO

import java.lang
import java.nio.file.Path
import scala.annotation.tailrec
import scala.util._
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

object Parser extends RegexParsers {

  override val skipWhitespace = true

  //
  // Simple Regexes
  // ////////////////////////////

  private[thrift] val integerRegex: Regex = "^[0-9]+".r
  private val integer: Parser[Integer] = integerRegex ^^ (_.trim.toInt)

  private[thrift] val slashRegex: Regex = "/".r
  private val slash: Parser[String] = slashRegex ^^ (_.trim)


  private[thrift] val openBraceRegex: Regex = "\\{".r
  private val openBrace: Parser[String] = openBraceRegex ^^ (_.trim)

  private[thrift] val closeBraceRegex: Regex = "}".r
  private val closeBrace: Parser[String] = closeBraceRegex ^^ (_.trim)

  private[thrift] val openArrowRegex: Regex = "<".r
  private val openArrow: Parser[String] = openArrowRegex ^^ (_.trim)

  private[thrift] val closeArrowRegex: Regex = ">".r
  private val closeArrow: Parser[String] = closeArrowRegex ^^ (_.trim)

  private[thrift] val commaRegex: Regex = ",".r
  private val comma: Parser[String] = commaRegex ^^ (_.trim)

  private[thrift] val quoteRegex: Regex = "['\"]".r
  private val quote: Parser[String] = quoteRegex ^^ (_.trim)

  private[thrift] val openParenRegex: Regex = "\\(".r
  private val openParen: Parser[String] = openParenRegex ^^ (_.trim)

  private[thrift] val closeParenRegex: Regex = "\\)".r
  private val closParen: Parser[String] = closeParenRegex ^^ (_.trim)

  private[thrift] val colonRegex: Regex = ":".r
  private val colon: Parser[String] = colonRegex ^^ (_.trim)

  private[thrift] val equalsRegex: Regex = "=".r
  private val equals: Parser[String] = equalsRegex ^^ (_.trim)

  private[thrift] val wildcardRegex: Regex = "\\*".r
  private val wildcard: Parser[String] = wildcardRegex ^^ (_.trim)

  private[thrift] val identRegex: Regex = "^[a-zA-Z_][a-zA-Z0-9_\\-.]+".r // very liberal here
  private val identity: Parser[String] = identRegex ^^ (_.trim)

  private[thrift] val numberStringrRegex: Regex = "^[0-9]+".r
  private val numberString: Parser[String] = numberStringrRegex ^^ (_.trim)

  private[thrift] val namespaceRegex: Regex = "namespace".r
  private val namespace: Parser[String] = namespaceRegex ^^ (_.trim)

  private[thrift] val includeRegex: Regex = "include".r
  private val include: Parser[String] = includeRegex ^^ (_.trim)

  private[thrift] val structRegex: Regex = "struct".r
  private val struct: Parser[String] = structRegex ^^ (_.trim)

  private[thrift] val constRegex: Regex = "const".r
  private val const: Parser[String] = constRegex ^^ (_.trim)

  private[thrift] val typedefRegex: Regex = "typedef".r
  private val typedef: Parser[String] = typedefRegex ^^ (_.trim)

  private[thrift] val serviceRegex: Regex = "service".r
  private val service: Parser[String] = serviceRegex ^^ (_.trim)

  private[thrift] val optionalRegex: Regex = "optional".r
  private val optional: Parser[String] = optionalRegex ^^ (_.trim)


  private[thrift] val extendsRegex: Regex = "extends".r
  private val extendsKeyword: Parser[String] = extendsRegex ^^ (_.trim)

  private[thrift] val throwsRegex: Regex = "throws".r
  private val throws: Parser[String] = throwsRegex ^^ (_.trim)

  private[thrift] val enumKeywordRegex: Regex = "enum".r
  private val enumKeyword: Parser[String] = enumKeywordRegex ^^ (_.trim)

  private[thrift] val pathRegex: Regex = "^[a-zA-Z_][a-zA-Z0-9_\\-./]+".r // very liberal here
  private val relativePath: Parser[String] = pathRegex ^^ (_.trim)


  //
  // Types
  // ////////////////////////////
  private[thrift] val dataType = opt(optional) ~ identity ~ opt(openArrow ~ identity ~ opt(comma ~ identity) ~ closeArrow) ^^ {
    case opt ~ typ ~ None => DataType(typ, None, None, opt.isDefined)
    case opt ~ typ ~ Some(_ ~ subType ~ None ~ _) => DataType(typ, Option(subType), None, opt.isDefined)
    case opt ~ typ ~ Some(_ ~ subType ~ Some(_ ~ valType) ~ _) => DataType(typ, Option(subType), Some(valType), opt.isDefined)
  }

  private[thrift] val typeDefObj: Parser[ThriftTypedef] = typedef ~ dataType ~ identity ^^ {
    case _ ~ dataType ~ name => ThriftTypedef(name, dataType)
  }

  //
  // Const
  // ////////////////////////
  private[thrift] val constObj: Parser[ThriftConst] = const ~ dataType ~ identity ~ opt(equalsRegex ~ (identity | numberString)) ^^ {
    case _ ~ dataType ~ name ~ None => ThriftConst(name, dataType, None)
    case _ ~ dataType ~ name ~ Some(_ ~ defaultValue) => ThriftConst(name, dataType, Option(defaultValue))
  }
  //
  // Language Parsers
  // ////////////////////////////
  private val language: Parser[String] = wildcard | identity

  private[thrift] val namespaceObj: Parser[ThriftNamespace] = namespace ~ language ~ identity ^^ {
    case _ ~ "*" ~ name => ThriftNamespace(name, None)
    case _ ~ lang ~ name => ThriftNamespace(name, Some(lang))
  }
  // /////////////////////////////
  private[thrift] val includeObj: Parser[ThriftInclude] = include ~ opt(quote) ~ relativePath ~ opt(quote) ^^ {
    case _ ~ _ ~ path ~ _ =>
      val parts = path.split("/")
      ThriftInclude(parts.last, parts.init)
  }

  // /////////////////////////////
  private[thrift] val enumEntry: Parser[ThriftEnumEntry] = identity ~ equals ~ integer ~ opt(comma) ^^ {
    case name ~ _ ~ position ~ _ => ThriftEnumEntry(name, position)
  }

  private[thrift] val enumObj = enumKeyword ~ identity ~ openBrace ~ rep(enumEntry) ~ closeBrace ^^ {
    case _ ~ name ~ _ ~ entries ~ _ => ThriftEnum(name, entries)
  }

  // /////////////////////////////
  private[thrift] val dataLine = integer ~ colon ~ dataType ~ identity ~ opt(comma) ^^ {
    case position ~ _ ~ typ ~ name ~ _ => ThriftDateLine(name, typ, position)
  }

  private[thrift] val structObj = struct ~ identity ~ openBrace ~ rep(dataLine) ~ closeBrace ^^ {
    case _ ~ name ~ _ ~ lines ~ _ => ThriftStruct(name, lines)
  }

  // /////////////////////////////
  private[thrift] val throwsClause: Parser[List[ThriftDateLine]] = throws ~ openParen ~ rep(dataLine) ~ closParen ^^ {
    case _ ~ _ ~ params ~ _ => params
  }

  private[thrift] val extendsClause: Parser[String] = extendsKeyword ~ identity ^^ {
    case _ ~ baseClass => baseClass
  }

  private[thrift] val serviceMethod = identity ~ identity ~ openParen ~ rep(dataLine) ~ closParen ~ opt(throwsClause) ^^ {
    case returnType ~ methodName ~ _ ~ parameters ~ _ ~ None => ThriftMethod(methodName, returnType, parameters, Seq())
    case returnType ~ methodName ~ _ ~ parameters ~ _ ~ Some(lines) => ThriftMethod(methodName, returnType, parameters, lines)
  }

  private[thrift] val serviceObj = service ~ identity ~ opt(extendsClause) ~ openBrace ~ rep(serviceMethod) ~ closeBrace ^^ {
    case _ ~ name ~ baseClass ~ _ ~ methods ~ _ => ThriftService(name, methods, baseClass)
  }

  // /////////////////////////////
  private[thrift] val thriftTypes: Parser[ThriftObj] = {
    namespaceObj | includeObj | enumObj | structObj | typeDefObj | serviceObj | constObj
  }

  private[thrift] val thriftFile: Parser[ThriftFile] = rep(thriftTypes) ^^ {
    types: Seq[ThriftObj] =>
      types.foldLeft(ThriftFile()) { case (file, obj) =>
        obj match {
          case ns@ThriftNamespace(_, _) => file.copy(namespaces = file.namespaces :+ ns)
          case in@ThriftInclude(_, _) => file.copy(includes = file.includes :+ in)
          case en@ThriftEnum(_, _) => file.copy(enums = file.enums :+ en)
          case cs@ThriftConst(_, _, _) => file.copy(consts = file.consts :+ cs)
          case td@ThriftTypedef(_, _) => file.copy(typeDefs = file.typeDefs :+ td)
          case st@ThriftStruct(_, _) => file.copy(structs = file.structs :+ st)
          case sv@ThriftService(_, _, _) => file.copy(services = file.services :+ sv)
        }
      }
  }

  //
  // Public Parse Methods
  // /////////////////////////////
  def parse[T](parser: Parser[T])(in: String): Either[String, T] =
    Try(parse(parser, in))
      .map {
        case Success(obj, _) => Right(obj)
        case Failure(msg, _) => Left(msg)
        case Error(msg, _) => Left(msg)
      }.getOrElse(Left("Unknown Error"))


  //
  // Filter Comments
  // ////////////////////////////////

  def removeComments(content: String): String = {

    @tailrec
    def removeMultiLine(content: String): String = {
      val start = content.indexOf("/**")
      val end = start + 6 + content.substring(start + 3).indexOf("**/")
      // println(s"COMMENT[${content.substring(start, end)}]")
      if (start == -1) {
        content
      } else {
        // println(s"Removing $start - $end")
        val alpha = content.substring(0, start)
        val omega = content.substring(end + 3)
        removeMultiLine(alpha + omega)
      }
    }

    def removeSingleLine(content: Seq[String]): Seq[String] = content.map { line =>
      val startPound = line.indexOf('#')
      val startSlashes = line.indexOf("//")
      if (startPound == -1 && startSlashes == -1) {
        line
      } else {
        line.substring(0, startSlashes.max(startPound)) // remove comment
      }
    }

    val temp: Array[String] = removeMultiLine(content).split("\n")
    //    println("*" * 100)
    //    temp.foreach(println)
    //    println("*" * 100)
    removeSingleLine(temp).mkString("\n")
  }


  //
  // File loader methods
  // ////////////////////////////
  def parseThriftFilePath(source: Path): Try[ThriftFile] = Try {
    FileIO.getContents(source) match {
      case None => throw new IllegalArgumentException("source file could not be read")
      case Some(content) =>
        val removed: String = removeComments(content)
        // println(removed)
        parse(thriftFile)(removed) match {
          case Left(value) => throw new IllegalStateException(value)
          case Right(file) => file
        }
    }
  }

}


sealed trait ThriftObj

final case class ThriftTypedef(name: String, dataType: DataType) extends ThriftObj

final case class ThriftInclude(fileName: String, dirs: Seq[String]) extends ThriftObj

final case class ThriftException(params: Seq[ThriftDateLine]) extends ThriftObj

final case class ThriftNamespace(name: String, language: Option[String]) extends ThriftObj

final case class ThriftConst(name: String, dataType: DataType, defaultValue: Option[String]) extends ThriftObj

final case class ThriftEnumEntry(name: String, position: Int)

final case class ThriftEnum(name: String, entries: Seq[ThriftEnumEntry]) extends ThriftObj

final case class ThriftDateLine(name: String, dataType: DataType, position: Int)

final case class ThriftStruct(name: String, lines: Seq[ThriftDateLine]) extends ThriftObj

final case class ThriftMethod(name: String, returnType: String, parameters: Seq[ThriftDateLine], exception: Seq[ThriftDateLine])

final case class ThriftService(name: String, methods: Seq[ThriftMethod], baseClass: Option[String] = None) extends ThriftObj

final case class ThriftFile(
                             namespaces: Seq[ThriftNamespace] = Seq(),
                             includes: Seq[ThriftInclude] = Seq(),
                             typeDefs: Seq[ThriftTypedef] = Seq(),
                             consts: Seq[ThriftConst] = Seq(),
                             enums: Seq[ThriftEnum] = Seq(),
                             structs: Seq[ThriftStruct] = Seq(),
                             services: Seq[ThriftService] = Seq() //
                           )

// Call, Reply, Exception and Oneway.
sealed trait MessageType

case object Call extends MessageType

case object Reply extends MessageType

case object Exception extends MessageType

case object Oneway extends MessageType


//
// Data Types
// //////////////////////////////
final case class DataType(typ: String, subType: Option[String] = None, valueType: Option[String] = None, optional: Boolean = false) {
  def thrift: String = DataType.renderType(this)

  def toScala: String = DataType.renderType(this, toScalaType)

  protected def toScalaType(thrift: String) = {
    thrift match {
      case "string" => "String"
      case "map" => "Map"
      case "list" => "Seq"
      case "double" => "Double"
      case "i32" => "Int"
      case "i64" => "Long"
      case other => other
    }
  }
}

object DataType {
  def renderType(dt: DataType, mapper: String => String = in => in) = {
    (dt.subType, dt.valueType) match {
      case (None, None) => mapper(dt.typ)
      case (Some(sub), None) => s"${mapper(dt.typ)}<${mapper(sub)}>"
      case (Some(sub), Some(vType)) => s"${mapper(dt.typ)}<${mapper(sub)}, ${mapper(vType)}>"
      case (None, Some(_)) => throw new IllegalStateException(("Unsupported datatype"))
    }
  }
}