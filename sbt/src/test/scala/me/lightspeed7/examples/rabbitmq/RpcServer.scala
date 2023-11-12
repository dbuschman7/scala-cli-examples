package me.lightspeed7.examples.rabbitmq

import scala.util.Using

object RpcServer extends App {

  private val RPC_QUEUE_NAME = "rpc_queue"

  Using(RabbitContext("localhost", 5672, "guest", "guest")) { ctx: RabbitContext =>
    println("In ctx")
    Using(ctx.rpqQueue(RPC_QUEUE_NAME)) { queue: RpcQueue =>
      println("In queue")
      queue.consume[Request, Response] { request =>
        Response(fib(request.num))
      }

      println("Server started, running ...")
      ctx.runUntilStopped()
      println("Queue shutdown")
    }
    println("Ctx shutdown")
  }


  private def fib(n: Int): Int = {
    if (n == 0) return 0
    if (n == 1) return 1
    fib(n - 1) + fib(n - 2)
  }


}
