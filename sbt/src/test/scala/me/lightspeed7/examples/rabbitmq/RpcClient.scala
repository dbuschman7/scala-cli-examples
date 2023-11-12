package me.lightspeed7.examples.rabbitmq

import play.api.libs.json.{Json, OFormat}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Using

final case class Request(num: Int)

object Request {
  implicit val _request: OFormat[Request] = Json.format[Request]
}

final case class Response(value: Int)

object Response {
  implicit val _response: OFormat[Response] = Json.format[Response]
}

object RpcClient extends App {

  private val RPC_QUEUE_NAME = "rpc_queue"

  Using(RabbitContext("localhost", 5672, "guest", "guest")) { ctx: RabbitContext =>
    Using(ctx.rpqQueue(RPC_QUEUE_NAME)) { queue: RpcQueue =>

      try {
        for (i <- 0 until 32) {
          System.out.println(" [x] Requesting fib(" + i + ")")
          val req: Request = Request(i)
          val fut: Future[Response] = queue.send[Request, Response](req)
          val response: Response = Await.result(fut, Duration.Inf)
          System.out.println(" [.] Got '" + response.value + "'")
        }
      } catch {
        case e: Throwable => e.printStackTrace()
      }
    }
  }

}
