package me.lightspeed7.examples.cli

import scala.io.StdIn
import scala.util.Try

object CollectMax extends App {
  import me.lightspeed7.examples.library.Util._

  //
  // params and setup
  // /////////////////////
  val interval: Option[Int] = args.headOption match {
    case Some("--help")                                 => usage()
    case Some(nStr) if Try(nStr.trim().toInt).isSuccess => Some(nStr.trim.toInt)
    case Some(other)                                    => errorOut(s"Unknown argument given '$other'")
    case None                                           => None
  }

  //
  // processor
  // /////////////////////
  var count = 0
  //
  Iterator
    .continually(StdIn.readLine)
    .takeWhile(_.nonEmpty)
    .map(logic)
    .foreach { line: String =>
      count += 1
      interval match {
        case None =>
          println(logic(line))
        case Some(interval) =>
          if (count >= interval) {
            println(logic(line))
            count = 0
          }
      }
    }

  def logic(in: String): String = {
    val words          = Option(in).getOrElse("").split(" ").toSeq.flatMap(s => s.trim.notBlank)
    val maxLength: Int = words.maxBy(_.length).length
    f"${words.length}%d $maxLength%d"
  }

  def errorOut(msg: String): Nothing = {
    Console.err.println(s"Unknown argument given '$msg'")
    sys.exit(1)
  }

  def usage(): Nothing = {
    println("""Usage: collectMax [--help] [n]
              |
              |     where : n = output every nth line""".stripMargin)
    sys.exit(0)
  }

}
