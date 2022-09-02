package me.lightspeed7
package examples

import zio.Console._
import zio.{Scope, ZIO, ZIOAppArgs}

import java.io.IOException

object Main extends zio.ZIOAppDefault {

  val myAppLogic: ZIO[Any, IOException, Unit] =
    for {
      _ <- printLine("─" * 100)
      _ <- printLine("hello world")
      _ <- printLine("─" * 100)
    } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =  myAppLogic.exitCode
}


