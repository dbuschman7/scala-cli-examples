package me.lightspeed7.examples.library

import com.typesafe.scalalogging.LazyLogging

import java.io.{File, InputStream, OutputStream}
import java.nio.file.{Path, Paths, StandardOpenOption}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.io.Source
import scala.util.{Failure, Success, Try}

object FileIO extends LazyLogging {

  import scala.language.postfixOps

  def getContents(basePath: Path, filename: String): Option[String] =
    getContents(Paths.get(basePath.toString, filename))

  def getContents(fullPath: Path): Option[String] = {
    logger.debug(s"getContents - path = $fullPath")
    // use new Java nio parts to get a read-only access files read correctly.
    val stream = java.nio.file.Files.newInputStream(fullPath, StandardOpenOption.READ)
    getContents(stream) match {
      case Left(msg) =>
        logger.warn(s"Unable to fetch $fullPath - $msg")
        None
      case Right(value) =>
        Some(value)
    }
  }

  def getContents(stream: InputStream): Either[String, String] = {
    Try(Source.fromInputStream(stream, "UTF-8").mkString) match {
      case Success(value) => Right(value)
      case Failure(ex) => Left(ex.getMessage)
    }
  }

  def exec(command: String): String = {
    import sys.process._
    command !!
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit): Unit = {
    val p = new java.io.PrintWriter(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }

  def writeContents(fullPath: Path)(data: String*): Unit =
    printToFile(fullPath.toFile) { p =>
      data.foreach(p.println)
    }

  def writeContentTo(outStream: OutputStream)(body: => Array[Byte]) = {
    try {
      outStream.write(body)
    } finally {
      outStream.close()
    }
  }

  //
  def await[T](f: Future[T])(implicit timeout: Duration): T = Await.result(f, timeout)

  //
  def findTreeFiles(baseDir: File, keyWords: String*): Array[File] = {
    val these = baseDir.listFiles
    val good = these.filter { f =>
      val fStr = f.toString
      keyWords.forall(kw => fStr.contains(kw))
    }
    good ++ these.filter(_.isDirectory).flatMap(findTreeFiles(_, keyWords: _*))
  }

}
