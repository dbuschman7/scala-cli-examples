import sbt.Keys.{baseDirectory, streams}
import sbt._

import scala.sys.process._

object DepListPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val helloGreeting = settingKey[String]("greeting")
    val hello = taskKey[Unit]("say hello")
  }

  import autoImport._

  override lazy val globalSettings: Seq[Setting[_]] = Seq(
    helloGreeting := "hi",
  )

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    hello := {
      println("MAIN")
      //val file = ValidateDeps.generateFile(baseDirectory.value)
      //println(s"Exists(${file.exists()}) - ${file.toString}")
       this.projectConfigurations.map(_.toString()).foreach(println)

      val s = streams.value
      val g = helloGreeting.value
      s.log.info(g)
    }
  )
}

object ValidateDeps {

  def generateFile(projectDir: File): File = {
    val filePath = "depList.txt"
    val file = projectDir / "target" / filePath
    println(s"Generating Deps File : ${file.toString}")
    val out: String = Process(Seq("sbt", s"dependencyList/toFile target/$filePath"), projectDir).!!
    println("Process Out - " + out)
    file
  }


}
