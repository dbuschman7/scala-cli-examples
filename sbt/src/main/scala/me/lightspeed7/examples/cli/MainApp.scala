package me.lightspeed7.examples.cli

import me.lightspeed7.examples.library.{AWS, Library, Request, Response}
import zio.json._

import scala.util.Try

object MainApp extends App {

  val command = args.headOption.getOrElse("help")
  command match {
    case "help" =>
      println("Usage: [logs | deploy ] ")

    case "logs" => AWS.Logs.fetchMostRecentLogs(s"/aws/lambda/ping-lambda", "")

    case "deploy"  => Try(args(2)).toOption.foreach { name: String => Lambda.invoke(name, "Current") }
  }

}

object Lambda {

  def invoke(name: String, alias: String): Unit =
    AWS.Lambda.invokeFunction("ping-lambda", alias, Request(name, "").toJson) match {
      case Left(msg) => println("Unable to call Lambda - " + msg)
      case Right(respStr) =>
        respStr.fromJson[Response] match {
          case Left(msg)       => println("Could not parse response to json - " + msg)
          case Right(response) => println("RESPONSE -> " + response.message)
        }
    }


}
