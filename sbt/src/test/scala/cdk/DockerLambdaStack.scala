
package cdk

import software.amazon.awscdk.services.ec2._
import software.amazon.awscdk.services.ecr.Repository
import software.amazon.awscdk.services.iam._
import software.amazon.awscdk.services.lambda.{Alias, EcrImageCode, Runtime, Function => LambdaFunction}
import software.amazon.awscdk.{Duration, RemovalPolicy, Stack, StackProps}
import software.constructs.Construct

case class DockerLambdaStack(scope: Construct, props: StackProps) extends Stack(scope, "DockerLambda", props) with Common {

  import scala.jdk.CollectionConverters._

  lazy val ecrRepository: Repository = Repository
    .Builder
    .create(this, "EcrRepoDockerLambda")
    .repositoryName("docker-lambda")
    .imageScanOnPush(true)
    .removalPolicy(RemovalPolicy.DESTROY)
    .build()

  lazy val ecrImageCode: EcrImageCode = EcrImageCode
    .Builder
    .create(ecrRepository)
    .entrypoint(Seq("./lambda_entrypoint.sh").asJava)
    .tagOrDigest("1.0.0")
    .cmd(Seq("me.lightspeed7.lambda.LambdaMain::handleRequest").asJava)
    .build()

  val fullName: String = "docker-lambda"

  val vpc: IVpc = Vpc
    .fromLookup(
      this,
      "VPC",
      VpcLookupOptions
        .builder()
        .vpcName(vpcName)
        .build(),
    )

  val sgs: Seq[ISecurityGroup] = Seq(SecurityGroup.fromLookupByName(this, "SG", privateSG, vpc))

  val subnets: SubnetSelection = SubnetSelection
    .builder()
    .subnetType(SubnetType.PRIVATE_ISOLATED)
    .build()

  lazy val lambdaRole: Role = Role
    .Builder
    .create(this, "LambdaRole")
    .assumedBy(ServicePrincipal.Builder.create("lambda").build())
    .inlinePolicies(policies.asJava)
    .managedPolicies(managed.asJava)
    .maxSessionDuration(Duration.hours(1))
    .build()

  lazy val lambda: LambdaFunction = LambdaFunction
    .Builder
    .create(this, fullName)
    .functionName(fullName)
    .description(fullName)
    .runtime(Runtime.FROM_IMAGE)
    .handler("FROM_IMAGE")
    .securityGroups(sgs.asJava)
    .vpc(vpc)
    .timeout(Duration.millis(500))
    .vpcSubnets(subnets)
    .role(lambdaRole)
    .memorySize(100) // in MB
    .code(ecrImageCode)
    .build()

  lazy val lambdaAliasDev: Alias = Alias.Builder
    .create(this, "DevAlias")
    .aliasName("Dev")
    .description("Lambda alias for run mode Dev and lambda: " + fullName)
    .version(lambda.getLatestVersion)
    .build()

  lazy val lambdaAliasProd: Alias = Alias.Builder
    .create(this, "ProdAlias")
    .aliasName("Prod")
    .description("Lambda alias for run mode Prod and lambda: " + fullName)
    .version(lambda.getLatestVersion)
    .build()

  lazy val vpcAccess: IManagedPolicy = ManagedPolicy
    .fromManagedPolicyArn(this, "VpcAccess", "arn:aws:iam::aws:policy/AWSLambdaVPCAccessExecutionRole")

  lazy val policies: Map[String, PolicyDocument] = Map()

  lazy val managed: Seq[IManagedPolicy] = Seq(vpcAccess)
}

