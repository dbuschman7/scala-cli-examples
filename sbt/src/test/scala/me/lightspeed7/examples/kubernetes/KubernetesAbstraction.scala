package me.lightspeed7.examples.kubernetes

import io.kubernetes.client.custom.{IntOrString, Quantity}
import io.kubernetes.client.openapi.models.{V1ObjectMeta, _}

import scala.collection.JavaConverters._
import scala.collection.mutable

trait KubernetesAbstraction {

  sealed trait Arch {
    def label: String
  }

  case object Arm64 extends Arch {
    override def label: String = "arm64"
  }

  case object Amd64 extends Arch {
    override def label: String = "amd64"
  }

  object Service {
    def fromDeploymentWithLabelSelector(deploy: V1Deployment, portName: String): V1Service = {

      val svc = new V1Service()
        .apiVersion("v1")
        .kind("Service")
        .withMetadata(_.name(deploy.getMetadata.getName)) // same as deployment
        .withMetadata(_.namespace(deploy.getMetadata.getNamespace))


      if (deploy.getMetadata == null ||
        deploy.getMetadata.getLabels == null ||
        deploy.getMetadata.getLabels.isEmpty
      ) {
        throw new IllegalStateException("Deployment must have labels defined on the root metadata")
      }

      val spec = new V1ServiceSpec()
      svc.spec(spec)

      svc.withMetadata { svcMeta =>
        deploy.withMetadata { meta =>
          meta.getLabels.asScala.map {
            case (k, v) =>
              spec.putSelectorItem(k, v)
              svcMeta.putLabelsItem(k, v)
          }
        }
      }

      deploy.withSpec { dapSpec: V1DeploymentSpec =>
        dapSpec.withTemplate { tempSpec: V1PodTemplateSpec =>
          tempSpec.withPodSpec { pod =>
            val portList = pod.getContainers.asScala.flatMap { p => p.getPorts.asScala }.toSeq
            portList
              .find(_.getName == portName)
              .map { port: V1ContainerPort =>
                spec.addPortsItem(new V1ServicePort()
                  .name(port.getName)
                  .protocol(port.getProtocol)
                  .port(port.getContainerPort)
                  .targetPort(new IntOrString(port.getContainerPort)))
              }
          }
        }
      }

      svc
    }
  }

  object Deployment {

    def withName(environment: String, namespace: String, appName: String, forcePodAntiAffinity: Boolean = false): V1Deployment = {
      val deploy = new V1Deployment()
      deploy.kind("Deployment").apiVersion("apps/v1")
      deploy.setAppName(namespace, appName)
      if (forcePodAntiAffinity) {
        deploy.withMetadata { meta =>
          deploy.forcePodAntiAffinity(environment, appName)
        }
      }
      deploy
    }

  }

  implicit class BuilderV1Service(service: V1Service) {

    def withMetadata(block: V1ObjectMeta => Unit): V1Service = {
      if (service.getMetadata == null) {
        service.setMetadata(new V1ObjectMeta)
      }
      block(service.getMetadata)
      service
    }
  }

  implicit class BuilderV1Deployment(deploy: V1Deployment) {

    def setAppName(namespace: String, appName: String): V1Deployment = {
      withMetadata { meta =>
        meta.setName(appName)
        meta.setNamespace(namespace)
        meta.getLabelsSafe.put("namespace", namespace)
      }
      deploy
    }

    def withMetadata(block: V1ObjectMeta => Unit): V1Deployment = {
      if (deploy.getMetadata == null) {
        deploy.setMetadata(new V1ObjectMeta)
      }
      block(deploy.getMetadata)
      deploy
    }

    def forcePodAntiAffinity(environment: String, appName: String): V1Deployment = {
      withMetadata { meta: V1ObjectMeta =>
        val safe = meta.getLabelsSafe
        safe.put("application", appName)
        safe.put("environment", environment)

        deploy.withSpec { spec: V1DeploymentSpec =>
          spec.withTemplate { tempSpec: V1PodTemplateSpec =>
            tempSpec.withPodSpec { pod =>
              pod.withPodAntiAffinity(meta)
            }
          }
        }
      }
      //
      deploy
    }

    def setContainerArch(arch: Arch): V1Deployment = {
      deploy.withSpec { spec: V1DeploymentSpec =>
        spec.withTemplate { tempSpec: V1PodTemplateSpec =>
          tempSpec.withPodSpec { pod: V1PodSpec =>
            pod.addNodeAffinityForArch(arch)
          }
        }
      }
      //
      deploy
    }

    def addPodContainer(block: V1Container => Unit): V1Deployment = {
      deploy.withSpec { spec: V1DeploymentSpec =>
        spec.withTemplate { tempSpec: V1PodTemplateSpec =>
          tempSpec.withPodSpec { pod: V1PodSpec =>
            pod.addContainer(block)
          }
        }
      }
      //
      deploy
    }

    def withSpec(block: V1DeploymentSpec => Unit): V1Deployment = {
      if (deploy.getSpec == null) {
        deploy.setSpec(new V1DeploymentSpec)
      }
      block(deploy.getSpec)
      deploy
    }
  }

  implicit class BuilderV1PodTemplateSpec(tempSpec: V1PodTemplateSpec) {

    def withPodSpec(block: V1PodSpec => Unit): V1PodTemplateSpec = {
      if (tempSpec.getSpec == null) {
        tempSpec.setSpec(new V1PodSpec)
      }
      block(tempSpec.getSpec)
      tempSpec
    }

  }

  implicit class BuilderV1DeploymentSpec(spec: V1DeploymentSpec) {
    def withTemplate(block: V1PodTemplateSpec => Unit): V1DeploymentSpec = {
      if (spec.getTemplate == null) {
        spec.setTemplate(new V1PodTemplateSpec)
      }
      block(spec.getTemplate)
      spec
    }
  }

  implicit class BuilderV1Pod(pod: V1Pod) {
    def withMetadata(block: V1ObjectMeta => Unit): V1Pod = {
      if (pod.getMetadata == null) {
        pod.setMetadata(new V1ObjectMeta)
      }
      block(pod.getMetadata)
      pod
    }

    def withSpec(block: V1PodSpec => Unit): V1Pod = {
      if (pod.getSpec == null) {
        pod.setSpec(new V1PodSpec)
      }
      block(pod.getSpec)
      pod
    }

    def setAppName(appName: String, forcePodAntiAffinity: Boolean = false, meta: V1ObjectMeta): V1Pod = {
      withMetadata { meta =>
        meta.setName(appName)
      }
      if (forcePodAntiAffinity) {
        withSpec(_.withPodAntiAffinity(meta))
      }
      pod
    }
  }

  implicit class BuilderV1ObjectMeta(meta: V1ObjectMeta) {
    def getLabelsSafe: java.util.Map[String, String] = {
      if (meta.getLabels == null) {
        meta.setLabels(new mutable.HashMap[String, String].asJava)
      }
      meta.getLabels
    }
  }

  implicit class BuilderV1PodSpec(spec: V1PodSpec) {
    def addContainer(block: V1Container => Unit): V1PodSpec = {
      val c = new V1Container
      spec.addContainersItem(c)
      block(c)
      spec
    }

    def addNodeAffinityArm64(): V1PodSpec = addNodeAffinityForArch(Arm64)

    def addNodeAffinityAmd64(): V1PodSpec = addNodeAffinityForArch(Amd64)

    def addNodeAffinityForArch(arch: Arch): V1PodSpec = {
      val matchReq = new V1NodeSelectorRequirement()
        .key("kubernetes.io/arch").operator("In").values(List(arch.label).asJava)
      val matchTerm = new V1NodeSelectorTerm().addMatchExpressionsItem(matchReq)
      val sel = new V1NodeSelector().addNodeSelectorTermsItem(matchTerm)

      val node = new V1NodeAffinity()
      node.setRequiredDuringSchedulingIgnoredDuringExecution(sel)
      getTopAffinity.nodeAffinity(node)
      spec
    }

    def withPodAntiAffinity(meta: V1ObjectMeta): V1PodSpec = {

      val reqs: Seq[V1LabelSelectorRequirement] = meta.getLabels.asScala.map { case (k, v) =>
        new V1LabelSelectorRequirement().key(k).operator("In").values(List(v).asJava)
      }.toSeq

      val sel = reqs.foldLeft(new V1LabelSelector()) { case (sel, req) => sel.addMatchExpressionsItem(req) }
      val matchTerm = new V1PodAffinityTerm().labelSelector(sel)
      val pod = new V1PodAntiAffinity()
      pod.setRequiredDuringSchedulingIgnoredDuringExecution(List(matchTerm).asJava)
      getTopAffinity.podAntiAffinity(pod)
      spec
    }

    private def getTopAffinity: V1Affinity = {
      if (spec.getAffinity == null) {
        spec.affinity(new V1Affinity())
      }
      spec.getAffinity
    }
  }

  implicit class BuilderV1PodContainer(cont: V1Container) {
    def addPort(block: V1ContainerPort => Unit): V1Container = {
      val p = new V1ContainerPort
      cont.addPortsItem(p)
      block(p)
      cont
    }

    def addLivenessProbe(block: V1Probe => Unit): V1Container = {
      val p = new V1Probe
      cont.setLivenessProbe(p)
      block(p)
      cont
    }

    def addReadinessProbe(block: V1Probe => Unit): V1Container = {
      val p = new V1Probe
      cont.setReadinessProbe(p)
      block(p)
      cont
    }

    def addStandardHealthProbes(port: Int = 8999,
                                initialDelaySecs: Int = 3,
                                livenessDelaySecs: Int = 10,
                                failureThreshold: Int = 3,
                                timeoutSecs: Int = 10,
                                periodSecs: Int = 10
                               ): V1Container = {
      val name = "health"
      cont
        .addPort(_.name(name).containerPort(port).protocol("TCP"))
        .addLivenessProbe { probe =>
          probe
            .initialDelaySeconds(initialDelaySecs)
            .failureThreshold(failureThreshold)
            .timeoutSeconds(timeoutSecs)
            .periodSeconds(periodSecs)
            .addHttp(_.path("/health").scheme("TCP").port(new IntOrString(name)))
        }
        .addReadinessProbe { probe =>
          probe
            .initialDelaySeconds(livenessDelaySecs)
            .failureThreshold(failureThreshold)
            .timeoutSeconds(timeoutSecs)
            .periodSeconds(periodSecs)
            .addHttp(_.path("/health").scheme("TCP").port(new IntOrString(name)))
        }
    }

    def addPublicPort(port: Int, name: String, protocol: String = "TCP"): V1Container = {
      cont.addPort(_.name(name).containerPort(port).protocol(protocol))
    }

    def setResource(name: String, request: String, limit: String): V1Container = {
      withResourceRequirements { req =>

        if (req.getRequests == null) {
          req.setRequests(new mutable.HashMap[String, Quantity].asJava)
        }
        req.getRequests.put(name, new Quantity(request))

        if (req.getLimits == null) {
          req.setLimits(new mutable.HashMap[String, Quantity].asJava)
        }
        req.getLimits.put(name, new Quantity(limit))
      }
    }

    def withResourceRequirements(block: V1ResourceRequirements => Unit): V1Container = {
      if (cont.getResources == null) {
        cont.resources(new V1ResourceRequirements())
      }
      block(cont.getResources)
      cont
    }

    def envWithValue(name: String, value: String): V1Container = {
      cont.addEnvItem(new V1EnvVar().name(name).value(value))
    }

    def envFromConfigMap(name: String, mapName: String, mapKey: String): V1Container = {
      val sel = new V1ConfigMapKeySelector().name(mapName).key(mapKey)
      val varSrc = new V1EnvVarSource().configMapKeyRef(sel)
      cont.addEnvItem(new V1EnvVar().name(name).valueFrom(varSrc))
    }

    def envFromSecret(name: String, secretName: String, secretKey: String): V1Container = {
      val sel = new V1SecretKeySelector().name(secretName).key(secretKey)
      val varSrc = new V1EnvVarSource().secretKeyRef(sel)
      cont.addEnvItem(new V1EnvVar().name(name).valueFrom(varSrc))
    }

    def envFromFieldRef(name: String, fieldPath: String): V1Container = {
      val sel = new V1ObjectFieldSelector().fieldPath(fieldPath)
      val varSrc = new V1EnvVarSource().fieldRef(sel)
      cont.addEnvItem(new V1EnvVar().name(name).valueFrom(varSrc))
    }

    def apply(block: V1Container => Unit): V1Container = {
      block(cont)
      cont
    }

    implicit class BuilderV1Probe(probe: V1Probe) {

      def addExec(block: V1ExecAction => Unit): V1Probe = {
        val p = new V1ExecAction
        probe.setExec(p)
        block(p)
        probe
      }

      def addHttp(block: V1HTTPGetAction => Unit): V1Probe = {
        val h = new V1HTTPGetAction
        probe.setHttpGet(h)
        block(h)
        probe
      }
    }
  }
}
