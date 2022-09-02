package me.lightspeed7.examples.library

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
