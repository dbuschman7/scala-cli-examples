package me.lightspeed7.examples.lambda

import me.lightspeed7.examples.library.{ Library, Request, Response }
import zio.lambda.{ Context, ZLambda }
import zio.{ Task, ZIO }

object LambdaMain extends ZLambda[Request, Response] {

  override def apply(event: Request, context: Context): Task[Response] =
    for {
      _ <- ZIO.succeed(Console.println(s"Lambda Executing for ${event.toString}"))
    } yield Library.function(event)

}
