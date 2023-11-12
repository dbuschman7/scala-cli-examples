package me.lightspeed7.examples.kubernetes

import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Container
import io.kubernetes.client.openapi.{ApiClient, Configuration}
import io.kubernetes.client.util.{Config, Yaml}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import scala.collection.JavaConverters._

class KubernetesGeneratorTest extends AnyFunSuite with Matchers with KubernetesAbstraction {

  val client: ApiClient = Config.defaultClient
  Configuration.setDefaultApiClient(client)

  val api: CoreV1Api = new CoreV1Api

  test("full deployment with service") {
    val publicPortName = "api"

    def apiKeys(container: V1Container): Unit = {
      container
        .envFromConfigMap("ENVIRONMENT", "environment-config", "environment")
        .envFromFieldRef("MY_NODE_IP", "status.hostIP")
    }

    def kafkaVars(container: V1Container) = {}

    def rabbitVars(container: V1Container) = {}

    def resources(env: String)(cont: V1Container): Unit = {
      env match {
        case "dev" =>
          cont
            .setResource("cpu", "1", "1.2")
            .setResource("memory", "1Gi", "2Gi")
        case "stage" | "prod" =>
          cont
            .setResource("cpu", "2", "2.5")
            .setResource("memory", "2Gi", "4Gi")
      }
    }

    // make deployment
    val env = "dev"

    val deploy = Deployment
      .withName(env, "ns", "app-name", forcePodAntiAffinity = true)
      .setContainerArch(Arm64)
      .addPodContainer { container: V1Container =>
        container
          .name("www")
          .image("the-best-app-ever")
          .imagePullPolicy("IfNotPresent")
          .addStandardHealthProbes(8999)
          .addPublicPort(9000, publicPortName)
          .apply(resources(env))
          .apply(apiKeys)
          .apply(kafkaVars)
          .apply(rabbitVars)
          .envFromSecret("API_KEY", "api-secrets", "service.api.key")
          .envWithValue("FOO", "BAR")

      }
      .addPodContainer { container =>
        container
          .name("side-car")
          .image("big-brother")
          .imagePullPolicy("IfNotPresent")
          .addStandardHealthProbes(8999)
          .apply(resources(env))
          .envWithValue("BAR", "BAZ")
      }

    // attach service
    val service = Service.fromDeploymentWithLabelSelector(deploy, publicPortName)
    service.getSpec.clusterIP("None")

    println(Yaml.dumpAll(Seq(deploy, service).iterator.asJava))


  }

  //  test("make parts and lenses for generating k8s manifests") {
  //
  //    val pod = new V1Pod()
  //      .setAppName("app-name", forcePodAntiAffinity = true)
  //      .withSpec { spec: V1PodSpec =>
  //        spec
  //          .addNodeAffinityArm64()
  //          .addContainer { container: V1Container =>
  //            container
  //              .name("www")
  //              .image("nginx")
  //              .addStandardHealthProbes(8999)
  //          }
  //      }
  //
  //    println(Yaml.dump(pod))
  //  }


}
