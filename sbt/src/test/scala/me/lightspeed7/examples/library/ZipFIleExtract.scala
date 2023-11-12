package me.lightspeed7.examples.library

import java.io.File
import scala.collection.mutable.ArrayBuffer

final case class ZipFileExtract(file: File, filesMeta: Seq[FileMetadata])

final case class FileMetadata(name: String, size: Long, md5: String, directory: Boolean = false)

//
//
object ZipFileExtract extends App {

  import Util._

  import sys.process._

  def process(file: File, window: Int = 1000): ZipFileExtract = {

    val metaList = ArrayBuffer.empty[FileMetadata]

    def logger(window: Int = 1000): ProcessLogger = new ProcessLogger {
      var index = 0

      override def out(s: => String): Unit = {
        val parts = s.split(" ").flatMap(_.notBlank)
        val size = parts(0).trim.toInt
        val name = parts(3).trim
        val md5 = parts(4).trim
        val meta = FileMetadata(name, size, md5, name.endsWith("/"))
        index += 1
        if (index % window == 0) print(".")
        metaList.append(meta)
      }

      override def err(s: => String): Unit = System.out.println(s)

      override def buffer[T](f: => T): T = f
    }

    println(s"Looking for file '${file.toString}'")
    println(s"Exists - ${file.exists()}")
    print("Processing ")
    s"/Users/dave/temp/unzipChkSums.sh '${file.toString}' ".!(logger(window))
    println(" Done")

    ZipFileExtract(file, metaList.toSeq)
  }


  val extract = process(new File("/Users/dave/dev/dbuschman7/migrate/sk8s/sk8s-core/0.6.7/sk8s-core_2.12-0.6.7.jar"), window = 10)
  println("File parsed")
  extract.filesMeta.foreach(println)
}


