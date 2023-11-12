package me.lightspeed7.examples.library

import java.nio.file.{Path, Paths}

trait TestHelper {

  var debugLogging: Boolean = false

  def logIt(msg: String): Unit = {
    if (debugLogging) {
      Console.println(msg)
    }
  }

  def getProjectBasePath(project: String): Path = {
    val t = Paths.get(".").toAbsolutePath.toString.replace("/.", "")
    if (t.contains(project)) Paths.get(t) else Paths.get(t, project)
  }

  def getLibraryTestFilePath(project: String, pathParts: String*): Path = {
    val parts: Seq[String] = Seq("src", "test", "resources") ++ pathParts
    Paths.get(getProjectBasePath(project).toString, parts.mkString("/")).toAbsolutePath
  }

}
