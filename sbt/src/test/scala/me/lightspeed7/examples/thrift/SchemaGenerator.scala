package me.lightspeed7.examples.thrift

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

object SchemaGenerator {

  // case class Picture(width: Int, height: Int, url: Option[String])
  // import sangria.macros.derive._
  //
  //implicit val PictureType =
  //  deriveObjectType[Unit, Picture](
  //    ObjectTypeDescription("The product picture"),
  //    DocumentField("url", "Picture CDN URL"))
  //
  //trait Identifiable {
  //  def id: String
  //}
  //
  // val IdentifiableType = InterfaceType(
  //  "Identifiable",
  //  "Entity that can be identified",
  //  fields[Unit, Identifiable](
  //    Field("id", StringType, resolve = _.value.id)))
  //
  // case class Product(id: String, name: String, description: String) extends Identifiable {
  //  def picture(size: Int): Picture =
  //    Picture(width = size, height = size, url = Some(s"//cdn.com/$size/$id.jpg"))
  //}
  //
  //val ProductType =
  //  deriveObjectType[Unit, Product](
  //    Interfaces(IdentifiableType),
  //    IncludeMethods("picture"))


  def generateForPrefix(prefix: String, thriftFile: ThriftFile): String = {

    val buf = new StringBuilder()

    thriftFile.structs.map { st =>
      buf.append {
        st
          .lines
          .sortBy(_.position)
          .map { line =>
            s"${line.name} : ${line.dataType.toScala}"
          }
          .mkString(s"final case class ${st.name}(", ", ", ")")
      }
    }

    buf.toString()
  }

}
class ScalaGeneratorTest extends AnyFlatSpec with Matchers with TestFiles {

  "scala generation" should "make good scala code " in {

    loadFile(getLibraryTestFilePath("sbt", "example.thrift")) match {
      case Left(value) => fail(value)
      case Right(file) =>
        Console.println(SchemaGenerator.generateForPrefix("dave", file))
    }
  }
}