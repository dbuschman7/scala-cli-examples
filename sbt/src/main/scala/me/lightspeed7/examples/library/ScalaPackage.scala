package me.lightspeed7.examples.library

case class ScalaPackage(path: Seq[String], files: Seq[ScalaFile], children: Seq[ScalaPackage]) {

  def addfile(file: ScalaFile): ScalaPackage = this.copy(files = this.files :+ file)

  def addfiles(files: Seq[ScalaFile]): ScalaPackage = this.copy(files = this.files ++ files)

  def addChild(child: ScalaPackage): ScalaPackage = this.copy(children = this.children :+ child)

  def addChildren(children: Seq[ScalaPackage]): ScalaPackage = this.copy(children = this.children ++ children)
}

object ScalaPackage {

  import Util._

  def make(path: String): ScalaPackage = {
    val parts = path
      .replaceAll("/", ".")
      .split("\\.")
      .flatMap(_.notBlank)
      .toSeq
    ScalaPackage(parts, Seq(), Seq())
  }
}

case class ScalaFile(fileName: String, packageList: Seq[String], assigned: Option[Project]) {
  def assignTo(project: Project): ScalaFile = this.copy(assigned = Option(project))
}

object ScalaFile {

  import Util._

  def make(path: String): ScalaFile = {

    val parts = path
      .replaceAll("/", ".")
      .split("\\.")
      .flatMap(_.notBlank)
      .toSeq
      .removeTail("scala")

    val name = parts.last
    val packageList = parts.init
    ScalaFile(name, packageList, None)
  }
}