package me.lightspeed7.examples

import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider, ProfileCredentialsProvider}
import software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.{InvocationType, InvokeRequest, LogType}

import java.time.{Duration => JTDuration}
import scala.annotation.tailrec
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success, Try}

case class LambdaLoadTestClient(regionStr: String, alias: String, request: String, testDurationInMin: Int) {
  private val profileKey: Option[String] = sys.env.get("AWS_PROFILE")
  private val awsSecretKey: Option[String] = sys.env.get("AWS_ACCESS_KEY_ID")
  private val region: Region = Region.of(regionStr)

  val account = "fixMe"

  private val jtTimeout: JTDuration = JTDuration.ofMinutes(testDurationInMin + 1)
  private val timeout: FiniteDuration = Duration.fromNanos(jtTimeout.toNanos)

  private val credsProvider: AwsCredentialsProvider = (profileKey, awsSecretKey) match {
    case _ => ProfileCredentialsProvider.create()
  }

  private lazy val httpClientBuilder: ApacheHttpClient.Builder = ApacheHttpClient
    .builder()
    .socketTimeout(jtTimeout)

  private lazy val clientOverride: ClientOverrideConfiguration = ClientOverrideConfiguration
    .builder()
    .apiCallAttemptTimeout(jtTimeout)
    .apiCallTimeout(jtTimeout)
    .build()

  val requestOverride: AwsRequestOverrideConfiguration = AwsRequestOverrideConfiguration
    .builder()
    .apiCallAttemptTimeout(jtTimeout)
    .build()


  private lazy val lambdaClient: LambdaClient = LambdaClient
    .builder()
    .httpClientBuilder(httpClientBuilder)
    .region(region)
    .credentialsProvider(credsProvider)
    .overrideConfiguration(clientOverride)
    .build()

  private def lambdaFullArn(name: String, tag: String): String = s"ard:aws:lambda:us-east-1:$account:function:$name:$tag"

  def invokeFunction(
                      name: String,
                      tag: String,
                      payload: String,
                    ): Either[String, String] = {
    val bytes = SdkBytes.fromUtf8String(payload)

    val request =
      InvokeRequest
        .builder()
        .functionName(lambdaFullArn(name, tag))
        .payload(bytes)
        .logType(LogType.TAIL)
        .invocationType(InvocationType.REQUEST_RESPONSE)
        .overrideConfiguration(requestOverride)
        .build()

    Retry.times(3, timeout) {
      val response = lambdaClient.invoke(request)
      response.statusCode() match {
        case i if i > 299 || i < 200 =>
          throw new IllegalStateException(Option(response.functionError()).getOrElse("Unknown Cause"))
        case _ =>
          Try(response.payload().asUtf8String()) match {
            case Failure(exception) => Left(exception.getMessage)
            case Success(value) => Right(value)
          }
      }
    }
  }

}

object Retry {

  import scala.concurrent.duration._

  @tailrec
  def times[T](n: Int, backoff: scala.concurrent.duration.Duration = 100.microseconds)(fn: => T): T = Try(fn) match {
    case Success(value) => value
    case _ if n > 1 =>
      Thread.sleep(backoff.toMillis)
      times(n - 1, backoff)(fn)

    case Failure(exception) => throw exception
  }

}
