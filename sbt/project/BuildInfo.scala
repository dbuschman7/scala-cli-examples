object BuildInfo {
  import sbt._
  import sbtbuildinfo.BuildInfoKey
  import scala.sys.process._
  import java.nio.file._

  def gitRepoOrElse[T](default: T)(gitCmd: java.nio.file.Path => T): T = {
    val pwd = System.getProperty("user.dir")
    val path = java.nio.file.Paths.get(s"$pwd/.git")
    if (path.toFile.exists()) {
      println("Unable to find git repo for git commands, aborting ...")
      gitCmd(path)
    }
    else {
      default
    }
  }

  def branch: String = gitRepoOrElse("Unknown")(_ => ("git rev-parse --abbrev-ref HEAD" !!).trim)
  def commit: String = gitRepoOrElse("Unknown")(_ => ("git rev-parse --short HEAD" !!).trim)
  def hasUnCommitted: Boolean = gitRepoOrElse(false) { _ =>
    ("git diff-index --quiet HEAD --" !) != 0
  }

  def settings(
      name: SettingKey[String],
      version: SettingKey[String],
      scalaVersion: SettingKey[String],
      sbtVersion: SettingKey[String],
    ) = {

    import sbtbuildinfo.BuildInfoKeys.{ buildInfoKeys, buildInfoPackage }

    def generateBuildInfo(
        name: BuildInfoKey,
        version: BuildInfoKey,
        scalaVersion: BuildInfoKey,
        sbtVersion: BuildInfoKey,
      ): Seq[BuildInfoKey] =
      Seq(name, version, scalaVersion, sbtVersion) :+ BuildInfoKey.action("buildTime") {
        System.currentTimeMillis
      } :+ BuildInfoKey.action("commit") {
        commit
      } :+ BuildInfoKey.action("branch") {
        branch
      } :+ BuildInfoKey.action("hasUnCommitted") {
        hasUnCommitted
      }

    Seq(
      buildInfoPackage := "me.lightspeed7",
      buildInfoKeys := generateBuildInfo(
        BuildInfoKey.action("name")(name.value),
        version,
        scalaVersion,
        sbtVersion,
      ),
    )
  }
}
