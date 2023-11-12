package me.lightspeed7.examples.library

case class Project(name: String,
                   relPath: String,
                   plugins: Seq[String] = Seq(),
                   settings: Seq[String] = Seq(),
                   dependsOn: Seq[Project] = Seq(),
                   aggregates: Seq[Project] = Seq()
                  ) {
  def addDependsOn(project: Project): Project = this.copy(dependsOn = this.dependsOn :+ project)

  def addAggregate(project: Project): Project = this.copy(aggregates = this.aggregates :+ project)

  def addSetting(aggregates: String): Project = this.copy(settings = this.settings :+ aggregates)

  def addPlugin(plugin: String): Project = this.copy(plugins = this.plugins :+ plugin)

  def toSbtString: String = {
    import Util._
    val pluginsStr = this.plugins.emptyOption.map(_.mkString(".enablePlugins(", ", ", ")\n")).getOrElse("")
    val settings = this.settings.emptyOption.map(_.mkString("  .settings(", ")\n  .settings(", ")\n")).getOrElse("")
    val dependsStr = this.dependsOn.emptyOption.map(_.map(_.name).mkString("  .dependsOn(\n", " % Cctt,\n", " % Cctt\n)")).getOrElse("")
    val aggregatesStr = this.aggregates.emptyOption.map(_.map(_.name).mkString("  .aggregates(\n", " % Cctt,\n", " % Cctt\n)")).getOrElse("")

    s"""lazy val $name = project.in(file("$relPath")
       |    $pluginsStr$settings$dependsStr$aggregatesStr
       |""".stripMargin

  }

}

object Project {
  def build(name: String, relPpath: String): Project = Project(name, relPpath)
}


case class Settings(name: String, lines: Seq[String]) {
  def addSetting(line: String): Settings = this.copy(lines = this.lines :+ line)

  def toSbtString: String = {
    val fmt = lines.mkString("Seq(", ",\n  ", "\n  )")
    s"lazy val $name = $fmt"

  }
}

object Settings {
  def build(name: String): Settings = Settings(name, Seq())
}