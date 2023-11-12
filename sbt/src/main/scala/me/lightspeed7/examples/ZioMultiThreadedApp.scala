package me.lightspeed7.examples

import zio._

/*
object ZioMultiThreadedApp extends zio.ZIOAppDefault {

val requestTimeout: zio.Duration = 5.seconds
val clientErrorCode = 499
val outcomes = Seq("success", "timeout", "failure")

val url = "http://localhost:8090"


val program = for {
body <- Task{ Body.}
token    <- Client.request(s"${url}/login/username/emanresu").flatMap(_.body.asString)
// Once the jwt token is procured, adding it as a Barer token in Authorization header while accessing a protected route.
response <- Client.request(s"${url}/user/userName/greet", headers = Headers.bearerAuthorizationHeader(token))
body     <- response.body.asString
_        <- Console.printLine(body)
} yield ()


override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = myAppLogic.exitCode

def requestWithBackoff(retries) = for {
req <- Random
  .nextIntBetween(0, 3)
  .map(outcomes(_))
count <- authRequest(req)
  .retry {
    zio.Schedule.exponential(1.second) && zio.Schedule.recurs(5)
  }

} yield ()


def authRequest(params: String): Task[Count] = Task {
  params match {
    case "fail" => ZIO.attempt {
      Thread.sleep(10);
      throw new IllegalStateException("foo")
    }.catchAll { t =>
      ZIO.succeed(Count(clientErrorCode, t.getMessage))
    }
    case "success" => ZIO.attempt {
      Thread.sleep(100)
      Count(200, "Payload goes here")
    }
    case "timeout" => ZIO.attempt {
      Thread.sleep(100 * 100 * 100)
    }
      .disconnect.timeout(requestTimeout)
      .catchAll { t =>
        ZIO.succeed(Count(408, "ZIO"))
      }
    case unk => Count(clientErrorCode, s"Unknown code - $unk")
  }
}

def log(msg: String): Unit = println(msg)
}
*/
final case class Count(code: Int, message: String)
