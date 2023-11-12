package me.lightspeed7.sk8s.aiq.k8s

/**
  *  Config
  *    .subSystem("realtime")
  *    .runMode("Dev")
  *    .features("query.lambda", "altlas", "dynamo")
  *    .toTypeSafe
  *    .prettyPrint
  *
  */
object SSMConfig {

  def load(subSystem: String)  = SSMConfigReadBuilder(subSystem)
  def write(subSystem: String) = SSMConfigWriteBuilder(subSystem)
}

final case class SSMConfigReadBuilder(subSystem: String, runMode: Option[String] = None, features: Seq[String] = Seq())
    extends Logger {

  def runMode(name: String): SSMConfigReadBuilder    = this.copy(runMode = Option(name))
  def features(names: String*): SSMConfigReadBuilder = this.copy(features = names)

  def load(): Either[String, Unit] = {
    (subSystem, runMode, features) match {
      case (_, Some(_), ft) if ft.isEmpty  => Left("No features given")
      case (_, None, _)                    => Left("No runMode given")
      case (s, Some(r), ft) if ft.nonEmpty => privateLoad(s, r, ft)
    }
  }

  def privateLoad(subSystem: String, runMode: String, features: Seq[String]): Either[String, Unit] = {
    features.foreach { ft =>
      val path = s"/$subSystem/$runMode/$ft/*"
      stdOut("Loading path - " + path)
    }

    Right(Unit)
  }
}

final case class SSMConfigWriteBuilder(
  subSystem: String,
  runMode: Option[String] = None,
  feature: Option[String] = None
) extends Logger {

  def runMode(name: String): SSMConfigWriteBuilder    = this.copy(runMode = Option(name))
  def features(name: String): SSMConfigWriteBuilder = this.copy(feature = Option(name))

  def write(key: String, value: String): Either[String, Unit] = {

    val path = s"/$subSystem/$runMode/$feature/$key"




  }
}
