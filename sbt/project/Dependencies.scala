import sbt._

object Dependencies {
  case object Amazon {
    val cdk = "software.amazon.awscdk" % "aws-cdk-lib" % "2.38.0"
    object SDK {
      val all = Seq("auth", "sso", "lambda", "ecr", "cloudwatchlogs", "ssm")
        .map(lib => (stringToOrganization("software.amazon.awssdk") % lib % "2.17.157"))
    }
  }

  case object com {
    case object github {

      case object liancheng {
        val `organize-imports` =
          "com.github.liancheng" %% "organize-imports" % "0.6.0"
      }
    }

    case object lihaoyi {
      val `ammonite-ops` = "com.lihaoyi" %% "ammonite-ops" % "2.4.1"
    }

    case object softwaremill {
      val quicklens = "com.softwaremill.quicklens" %% "quicklens" % "1.8.8"
    }

    case object typesafe {
      case object Akka {
        val version = "2.6.19"
        val akka    = "com.typesafe.akka" %% "akka"        % version
        val streams = "com.typesafe.akka" %% "akka-stream" % version
        val http    = "com.typesafe.akka" %% "akka-http"   % "10.2.9"
      }

      case object Logging {
        val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
      }
    }
  }

  case object dev {
    case object zio {
      val zio =
        "dev.zio" %% "zio" % "2.0.1"

      val `zio-config` =
        "dev.zio" %% "zio-config" % "3.0.2"

      val `zio-interop-cats` =
        "dev.zio" %% "zio-interop-cats" % "22.0.0.0"

      val `zio-streams` =
        "dev.zio" %% "zio-streams" % "2.0.1"

      val `zio-lambda`: ModuleID = "dev.zio" %% "zio-lambda" % "1.0.0-RC6"

      val `zio-json`: ModuleID = "dev.zio" %% "zio-json" % "0.3.0-RC11"

    }
  }

  case object io {
    case object scalaland {
      val chimney = "io.scalaland" %% "chimney" % "0.6.2"
    }
  }

  case object org {

    case object scalacheck {
      val scalacheck =
        "org.scalacheck" %% "scalacheck" % "1.16.0"
    }

    case object scalatest {
      val scalatest =
        "org.scalatest" %% "scalatest" % "3.2.12"
    }

    case object scalatestplus {
      val `scalacheck-1-15` =
        "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0"
    }

  }

}
