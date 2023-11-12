package cdk

import software.amazon.awscdk.services.ec2._
import software.amazon.awscdk.services.iam._
import software.amazon.awscdk.services.lambda.{Alias, AssetCode, Code, Runtime, Function => LambdaFunction}
import software.amazon.awscdk.services.logs.RetentionDays
import software.amazon.awscdk.{Duration, Stack, StackProps}
import software.constructs.Construct

case class PingLambdaStack(scope: Construct, props: StackProps) extends Stack(scope, "PingLambda", props) with Common {

  import scala.jdk.CollectionConverters._

  val fullName: String = "ping-lambda"

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

  lazy val code: AssetCode = Code.fromAsset("/Users/dave/dev/dbuschman7/scala-cli-examples/target/upload.zip")

  lazy val lambda: LambdaFunction = LambdaFunction
    .Builder
    .create(this, fullName)
    .functionName(fullName)
    .description(fullName)
    .runtime(Runtime.PROVIDED_AL2)
    .handler("not.required")
    .securityGroups(sgs.asJava)
    .vpc(vpc)
    .timeout(Duration.seconds(3))
    .vpcSubnets(subnets)
    .logRetention(RetentionDays.ONE_MONTH)
    .role(lambdaRole)
    .memorySize(100) // in MB
    .code(code)
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
