package me.lightspeed7.examples.thrift

import me.lightspeed7.examples.library.{FileIO, TestHelper}

import java.nio.file.Path

trait TestFiles extends TestHelper {

  def loadFile(source: Path): Either[String, ThriftFile] = {
    logIt("Path - " + source.toString)
    FileIO.getContents(source) match {
      case None => Left("source file could not be read")
      case Some(content) =>
        val removed: String = Parser.removeComments(content)
        // println(removed)
        Parser.parse(Parser.thriftFile)(removed) match {
          case Left(value) => Left(value)
          case Right(call) => Right(call)
        }
    }

  }

}