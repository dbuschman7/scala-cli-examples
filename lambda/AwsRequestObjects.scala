// /////////////////////////////////////////
//> using lib "dev.zio::zio-json:0.3.0"
// /////////////////////////////////////////

import zio.json._

import scala.util.Try

case class RequestIdentity(
                            apiKey: Option[String],
                            userArn: Option[String],
                            cognitoAuthenticationType: Option[String],
                            caller: Option[String],
                            userAgent: Option[String],
                            user: Option[String],
                            cognitoIdentityPoolId: Option[String],
                            cognitoAuthenticationProvider: Option[String],
                            sourceIp: Option[String],
                            accountId: Option[String],
                          )

case class RequestContext(
                           resourceId: String,
                           apiId: String,
                           resourcePath: String,
                           httpMethod: String,
                           accountId: String,
                           stage: String,
                           identity: RequestIdentity,
                           extendedRequestId: Option[String],
                           path: String
                         )

// The request returned from the next-event url
case class RequestEvent(
                         httpMethod: String,
                         body: Option[String],
                         resource: String,
                         requestContext: RequestContext,
                         queryStringParameters: Option[Map[String, String]],
                         headers: Option[Map[String, String]],
                         pathParameters: Option[Map[String, String]],
                         stageVariables: Option[Map[String, String]],
                         path: String,
                         isBase64Encoded: Boolean
                       )

object RequestEvent {

  import zio.json._

  private implicit val _decoderRI: JsonDecoder[RequestIdentity] = DeriveJsonDecoder.gen[RequestIdentity]
  private implicit val _decoderRC: JsonDecoder[RequestContext] = DeriveJsonDecoder.gen[RequestContext]
  private implicit val _decoderRE: JsonDecoder[RequestEvent] = DeriveJsonDecoder.gen[RequestEvent]

  def fromJsonSafe(s: String): Option[RequestEvent] = Try(s.fromJson[RequestEvent]) match {
    case util.Success(Right(re)) => Some(re)
    case util.Success(Left(err)) =>
      Console.err.println(s"Failed to parse body into RequestEvent: $err \nbody: $s");
      None
    case util.Failure(ex) =>
      Console.err.println(s"Failed to parse body into RequestEvent: $ex \nbody: $s");
      None
  }
}

// The response written to the response url by the function
case class LambdaResponse(
                           statusCode: String,
                           headers: Map[String, String],
                           body: String,
                           isBase64Encoded: Boolean = false
                         ) {


  def toJsonString: String = {
    import zio.json._
    this.toJsonPretty(LambdaResponse._encoder)
  }
}
object LambdaResponse {
  implicit val _encoder: JsonEncoder[LambdaResponse] = DeriveJsonEncoder.gen[LambdaResponse]
}