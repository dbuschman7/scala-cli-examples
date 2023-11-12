package me.lightspeed7.examples

import zio._

import java.io.IOException

object ZioCliApp extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = for {
    args <- getArgs
    _ <- Parser.parse(args)

  } yield ()
}

trait Command {
  def usage: IO[IOException, Unit]

  def execute(args: List[String]): IO[IOException, Unit]
}

object Parser {

  def parse(args: Chunk[String]): IO[IOException, Unit] = {
    args.toList match {
      case "run" :: duration :: Nil => Console.printLine(s"Running for ${duration.toInt} seconds")
      case "build" :: rest => Build.execute(rest)
      case "help" :: Nil => Console.printLine("I am sorry Dave, I am afraid I cannot do that.")
      case _ => usage
    }
  }

  def usage: IO[IOException, Unit] = {
    Console.printLine("CLI All-in-One app for Profile Api") *>
      Console.printLine("  Build:") *>
      Build.usage *>
      Console.printLine("  Run:") *>
      Run.usage
  }
}


object Build extends Command {
  def execute(args: List[String]): IO[IOException, Unit] = {
    args match {
      case "lambda" :: "query" :: "native" :: Nil => Console.printLine("Building native query lambda")
      case "lambda" :: "query" :: "java" :: Nil => Console.printLine("Building java query lambda")
      case "cli" :: Nil => Console.printLine("Building CLI app")
      case _ => Parser.usage

    }
  }

  def usage: IO[IOException, Unit] =
    Console.printLine(
      """    scala-cli build lambda query native - GraalVM Binary Image
        |    scala-cli build lambda query java   - Assembly Jar
        |    scala-cli build cli                 - Local native image
        |""".stripMargin)
}

object Run {
  def execute(args: List[String]): Unit = {
    args match {
      case duration :: Nil => Console.printLine(s"Running lambda with duration $duration")
      case _ => Parser.usage
    }
  }

  def usage: IO[IOException, Unit] =
    Console.printLine(
      """    scala-cli run <duration>            - Run the lambda for <duration> seconds
        |""".stripMargin)
}