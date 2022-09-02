import Dependencies._
import sbt._
import sbt.Keys._


ThisBuild / organization := "dbuschman7"
ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := Version.dateVersioning

lazy val `examples` =
  project
    .enablePlugins(JavaAppPackaging, BuildInfoPlugin, DockerPlugin)
    .in(file("."))
    .settings(name := "examples")
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Amazon.SDK.all ++ Seq(
        Amazon.cdk,
        com.typesafe.Logging.scalaLogging,
        dev.zio.`zio-lambda`,
        dev.zio.`zio-json`
      ),
      //
      // Test Deps
      //
      libraryDependencies ++= Seq(
        org.scalacheck.scalacheck,
        org.scalatest.scalatest,
        org.scalatestplus.`scalacheck-1-15`,
      ).map(_ % Test)
    )
    .settings(
       Compile / sources := Seq.empty,
       doc / sources := Seq.empty,
       Compile / publishArtifact := false,
       packageDoc / publishArtifact := false,
    )
    .settings(BuildInfo.settings(name, version, ThisBuild / scalaVersion, sbtVersion))

lazy val commonSettings = Seq(
  // addCompilerPlugin(org.typelevel.`kind-projector`),
  update / evictionWarningOptions := EvictionWarningOptions.empty,
  Compile / console / scalacOptions := {
    (Compile / console / scalacOptions)
      .value
      .filterNot(_.contains("wartremover"))
      .filterNot(Scalac.Lint.toSet)
      .filterNot(Scalac.FatalWarnings.toSet) :+ "-Wconf:any:silent"
  },
  Test / console / scalacOptions :=
    (Compile / console / scalacOptions).value,
)


onLoadMessage +=
  s"""|
      |╭─────────────────────────────────────
      |│ App ${name.value}
      |├─────────────────┬───────────────────
      |│ Scala Version   │ ${scalaVersion.value}
      |│ Sbt Version     │ ${sbtVersion.value}
      |│ App Version     │ ${version.value}
      |├─────────────────┼───────────────────
      |│ Git Branch      │ ${BuildInfo.branch}
      |│ Git Commit      │ ${BuildInfo.commit}
      |│ Has Uncommitted │ ${BuildInfo.hasUnCommitted}
      |╰─────────────────┴───────────────────""".stripMargin
