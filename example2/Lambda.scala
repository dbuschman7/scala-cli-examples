// ///////////////////////////////////////////
//
//> using scala "2.13"
//> using platform "jvm"
//> using lib "dev.zio::zio-lambda:1.0.0-RC6"
//> using lib "dev.zio::zio-json:0.3.0-RC11"
//> using lib "com.lihaoyi::requests:0.7.1"
//
// ///////////////////////////////////////////

import zio.lambda.{ Context, ZLambda }
import zio.{ Task, ZIO }

object LambdaMain extends ZLambda[Request, Response] {

  override def apply(event: Request, context: Context): Task[Response] =
    for {
      _ <- ZIO.succeed(Console.println(s"Lambda Executing for ${event.toString}"))
    } yield Library.function(event)

}

import zio.json.{ DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder }

final case class Request(name: String, comment: String)

object Request {
  implicit val _encode: JsonEncoder[Request] = DeriveJsonEncoder.gen[Request]
  implicit val _decode: JsonDecoder[Request] = DeriveJsonDecoder.gen[Request]
}

final case class Response(message: String)

object Response {
  implicit val _encode: JsonEncoder[Response] = DeriveJsonEncoder.gen[Response]
  implicit val _decode: JsonDecoder[Response] = DeriveJsonDecoder.gen[Response]
}

object Library {
  def function(request: Request): Response =
    Response(request.name.toUpperCase() + "!! -> " + request.comment)
}

object Util {
  implicit class StringOps(s: String) {

    def notEmpty: Option[String] =
      s match {
        case "" => None
        case _  => Option(s)
      }

    def notBlank: Option[String] = s.notEmpty.flatMap(_ => s.trim.notEmpty)

  }
}

