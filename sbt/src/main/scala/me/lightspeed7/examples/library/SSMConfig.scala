package me.lightspeed7.examples.library

/**
 * SSMConfig
 * .read("realtime")
 * .runMode("Dev")
 * .features("query.lambda", "atlas", "dynamo")
 * .toTypeSafe
 * .prettyPrint
 *
 * SMMConfig
 * .write("realtime")
 * .runMode("Dev")
 * .feature("atlas")
 * .save("password", "do not look at me", secure = true)
 */
object SSMConfig {

  def read(subSystem: String): SSMConfigReadBuilder = SSMConfigReadBuilder(subSystem)

  def write(subSystem: String): SSMConfigWriteBuilder = SSMConfigWriteBuilder(subSystem)
}

final case class SSMConfigReadBuilder(subSystem: String, runMode: Option[String] = None, features: Seq[String] = Seq())
  extends Logger {

  def runMode(name: String): SSMConfigReadBuilder = this.copy(runMode = Option(name))

  def features(names: String*): SSMConfigReadBuilder = this.copy(features = names)

  def load(): Either[String, Environment] = {
    (subSystem, runMode, features) match {
      case (_, Some(_), ft) if ft.isEmpty => Left("No features given")
      case (_, None, _) => Left("No runMode given")
      case (s, Some(r), ft) if ft.nonEmpty => privateLoad(s, r, ft)
    }
  }

  def privateLoad(subSystem: String, runMode: String, features: Seq[String]): Either[String, Environment] = {
    import scala.collection.JavaConverters._
    val env = Environment()
    features.foreach { ft =>
      val path = s"/$subSystem/$runMode][$ft/*"
      stdOut("Loading path - " + path)
      val props = AWS.SSM.getFromSSM(s"/$subSystem/$runMode", Seq(ft, "*")).asScala
      props
        .keySet
        .map { key: String =>
          props.get(key).map { value =>
            sys.props.put(key, value)
            val envKey = key.toUpperCase.replaceAll("\\.", "_")
            env.overrideValue(envKey, value)
          }
        }
    }

    Right(env)
  }
}

final case class SSMConfigWriteBuilder(
                                        subSystem: String,
                                        runMode: Option[String] = None,
                                        feature: Option[String] = None
                                      ) extends Logger {

  def runMode(name: String): SSMConfigWriteBuilder = this.copy(runMode = Option(name))

  def features(name: String): SSMConfigWriteBuilder = this.copy(feature = Option(name))

  def save(key: String, value: String, secure: Boolean = false): Either[String, Unit] = {
    val path = s"/$subSystem/$runMode/$feature"
    stdOut(s"Put -> $path/$key")
    if (secure) {
      AWS.SSM.putSecureStringValue(path)(Seq(key), value).map(_ => ())
    } else {
      AWS.SSM.putStringValue(path)(Seq(key), value).map(_ => ())
    }
  }
}
