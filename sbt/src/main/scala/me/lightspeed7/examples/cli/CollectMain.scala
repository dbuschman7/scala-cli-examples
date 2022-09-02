package me.lightspeed7.examples.cli

import zio.stream.{ ZPipeline, ZSink, ZStream }
import zio.{ Scope, ZIO, ZIOAppArgs, ZIOAppDefault }

import scala.io.StdIn

object CollectMain extends ZIOAppDefault {
  import me.lightspeed7.examples.library.Util._

  Iterator.continually(StdIn.readLine).takeWhile(_.nonEmpty)

  val source: ZStream[Any, Throwable, String] =
    ZStream.fromIterator(Iterator.continually(StdIn.readLine).takeWhile(_.nonEmpty))

  val process: ZPipeline[Any, Nothing, String, String] = ZPipeline
    .map[String, String](logic)

  val output: ZPipeline[Any, Nothing, String, Unit] = ZPipeline
    .map[String, Unit](println)

  val sink: ZSink[Any, Any, Unit, Nothing, Unit] = ZSink.drain

  val program: ZIO[Any, Any, Unit] = source.via(process).via(output).run(sink)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = program.exitCode

  def logic(in: String): String = {
    val words          = Option(in).getOrElse("").split(" ").toSeq.flatMap(s => s.trim.notBlank)
    val maxLength: Int = words.maxBy(_.length).length
    f"${words.length}%-3d ${maxLength}%d"
  }

  def println(in:String): Unit = {
    Console.println(in)
    Console.flush()
  }
}
