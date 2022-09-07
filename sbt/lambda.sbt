import Util._
import complete.DefaultParsers._

val region = "us-east-2"
val account = sys.env.getOrElse("AWS_ACCOUNT", "UNKNOWN")
val container = "docker-image"

val ecrRegistry = s"$account.dkr.ecr.$region.amazonaws.com"

lazy val tagRemotes = TaskKey[Unit]("tagRemotes", "Point local image to ECR", rank = KeyRanks.APlusTask)
tagRemotes := {
  val tag = version.value
  s"docker tag $container:$tag $ecrRegistry/$container:$tag".echo.runOrThrow
}

lazy val pushEcr = TaskKey[Unit]("pushEcr", "Transmit to ECR", rank = KeyRanks.APlusTask)
pushEcr := {
  val tag = version.value
  s"docker push $ecrRegistry/$container:$tag".echo.runOrThrow
}

lazy val publishVersion = InputKey[Unit]("publishVersion", "Update a lambda to a specific version", rank = KeyRanks.APlusTask)
publishVersion := {

  val args: Seq[String] = spaceDelimited("<args>").parsed
  if (args.length != 2) {
    println("This task required 2 parameters")
    println("")
    println("Usage: <lambda> <tag>")
    println("")
  } else {
    val lambda: String = args.head
    val tag: String = args.drop(1).head
    println(s"Create new lambda version - $tag")
    s"aws lambda update-function-code --function-name $lambda --publish --image-uri $ecrRegistry/$container:$tag".echo.runOrThrow
  }
}

lazy val updateAlias = InputKey[Unit]("updateAlias", "Update the lambda alias to a specific version", rank = KeyRanks.APlusTask)
updateAlias := {

  val args: Seq[String] = spaceDelimited("<args>").parsed
  if (args.length != 3) {
    println("This task required 3 parameters")
    println("")
    println("Usage: <lambda> <version:int> <alias>")
    println("")
  } else {
    val lambda: String = args.head
    val version: String = args.drop(1).head
    val alias: String = args.drop(2).head

    println(s"Deploying version $version of Lambda $lambda")
    s"aws lambda update-alias --function-name $lambda --function-version $version --name $alias".echo.runOrThrow
  }

}