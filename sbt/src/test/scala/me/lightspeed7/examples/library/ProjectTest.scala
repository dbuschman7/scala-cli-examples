package me.lightspeed7.examples.library

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class ProjectTest extends AnyFunSuite with Matchers {

  test("generate project stings") {

    val subProjA = Project.build("sunProjA", "subdir/projA")
    val subProjB = Project.build("sunProjB", "subdir/projB")

    val proj1 = Project
      .build("proj1", "library/proj1")
      .addPlugin("JavaAppPackaging")
      .addPlugin("DockerPlugin")
      .addSetting("name := \"proj1\"")
      .addSetting("scalaSettings")
      .addSetting("artifactoryPublishSettings")
      .addSetting("libraryDependencies ++= Amazon.SDK.all")
      .addDependsOn(subProjA)
      .addAggregate(subProjB)

    val result1 = proj1.toSbtString
    result1 mustBe
      """lazy val proj1 = project.in(file("library/proj1")
        |    .enablePlugins(JavaAppPackaging, DockerPlugin)
        |  .settings(name := "proj1")
        |  .settings(scalaSettings)
        |  .settings(artifactoryPublishSettings)
        |  .settings(libraryDependencies ++= Amazon.SDK.all)
        |  .dependsOn(
        |sunProjA % Cctt
        |)  .aggregates(
        |sunProjB % Cctt
        |)
        |""".stripMargin

  }

  test("settings test") {

    val scalaC = Settings
      .build("scalacSettings")
      .addSetting("""scalacOptions ++= Seq(
                    |  "-language:_",
                    |  "-Ymacro-annotations",
                    |  "-Wunused:imports")""".stripMargin)

    val result1 = scalaC.toSbtString
    result1 mustBe """lazy val scalacSettings = Seq(scalacOptions ++= Seq(
                     |  "-language:_",
                     |  "-Ymacro-annotations",
                     |  "-Wunused:imports")
                     |  )""".stripMargin
  }


}
