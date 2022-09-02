package me.lightspeed7.examples.library

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient
import software.amazon.awssdk.services.cloudwatchlogs.model.{
  DescribeLogStreamsRequest,
  DescribeLogStreamsResponse,
  GetLogEventsRequest,
  LogStream,
}
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.{ InvocationType, InvokeRequest, LogType }
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.{ GetParametersByPathRequest, GetParametersByPathResponse, Parameter }

import java.time.{ Duration => JTDuration }
import java.util.Properties
import scala.annotation.tailrec
import scala.concurrent.duration.{ Duration, FiniteDuration }
import scala.util.{ Failure, Success, Try }

object AWS {
  import scala.jdk.CollectionConverters._

  val account: String = sys.env.getOrElse("AWS_ACCOUNT", "UNKNOWN")
  val profile: String = sys.env.getOrElse("AWS_PROFILE", "UNKNOWN")
  val region: Region  = Region.US_EAST_2

  val jtTimeout: JTDuration   = JTDuration.ofSeconds(60)
  val timeout: FiniteDuration = Duration.fromNanos(jtTimeout.toNanos)

  private lazy val credentials: ProfileCredentialsProvider = ProfileCredentialsProvider
    .builder()
    .profileName(profile)
    .build()

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

  // ///////////////////////////////////////////
  // ///////////////////////////////////////////
  // ///////////////////////////////////////////
  // ///////////////////////////////////////////

  object Lambda {

    private lazy val lambdaClient: LambdaClient = LambdaClient
      .builder()
      .httpClientBuilder(httpClientBuilder)
      .region(region)
      .credentialsProvider(credentials)
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
              case Success(value)     => Right(value)
            }
        }
      }
    }

  }

  // ///////////////////////////////////////////
  // ///////////////////////////////////////////
  // ///////////////////////////////////////////
  // ///////////////////////////////////////////

  object Logs {

    private val logsClient: CloudWatchLogsClient = CloudWatchLogsClient
      .builder()
      .region(region)
      .httpClientBuilder(httpClientBuilder)
      .credentialsProvider(credentials)
      .overrideConfiguration(clientOverride)
      .build()

    def fetchMostRecentLogs(groupName: String, streamPrefix: String): Unit = {

      val request: DescribeLogStreamsRequest = DescribeLogStreamsRequest
        .builder()
        .overrideConfiguration(requestOverride)
        .logGroupName(groupName)
        .logStreamNamePrefix(streamPrefix)
        .build()

      val response: DescribeLogStreamsResponse = logsClient.describeLogStreams(request)
      val stream: LogStream                    = response.logStreams().asScala.maxBy(_.creationTime())

      val sReq = GetLogEventsRequest
        .builder()
        .logGroupName(groupName)
        .logStreamName(stream.logStreamName())
        .startFromHead(true)
        .build()

      val sResp = logsClient.getLogEvents(sReq)
      sResp.events().asScala.foreach(e => print(e.message()))
    }

  }

  // ///////////////////////////////////////////
  // ///////////////////////////////////////////
  // ///////////////////////////////////////////
  // ///////////////////////////////////////////

  object SSM {

    lazy val client: SsmClient = SsmClient
      .builder()
      .httpClientBuilder(httpClientBuilder)
      .credentialsProvider(credentials)
      .build()

    def getFromSSM(pathPrefix: String): Properties = {

      @tailrec
      def recurse(props: Properties, nextToken: Option[String]): Properties = {
        val result: GetParametersByPathResponse =
          client
            .getParametersByPath(
              GetParametersByPathRequest
                .builder()
                .path(pathPrefix)
                .withDecryption(true)
                .nextToken(nextToken.orNull)
                .build()
            )

        val prefixTerms = pathPrefix.split("/").length
        result.parameters().asScala.foreach { p: Parameter =>
          props.put(p.name().split("/").drop(prefixTerms).mkString("."), p.value())
        }

        Option(result.nextToken()) match {
          case None       => props
          case Some(next) => recurse(props, Some(next))
        }
      }

      recurse(new Properties(), None)
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
