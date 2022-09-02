package cdk

import software.amazon.awscdk.services.ec2.{SecurityGroup, SubnetConfiguration, SubnetType, Vpc}
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

case class VpcStack(scope: Construct, props: StackProps) extends Stack(scope, "ScalaCliVpc", props) with Common {

  import scala.jdk.CollectionConverters._

  val subnets: Seq[SubnetConfiguration] = Seq(
    SubnetConfiguration
      .builder()
      .subnetType(SubnetType.PRIVATE_ISOLATED)
      .cidrMask(24)
      .name(privateSubnet)
      .build()
  )

  val vpc: Vpc = Vpc
    .Builder
    .create(this, "VPC")
    .vpcName(vpcName)
    .maxAzs(2)
    .cidr("10.0.0.0/16")
    .subnetConfiguration(subnets.asJava)
    .build()

  val privateSecurityGroup: SecurityGroup = SecurityGroup.Builder
    .create(this, "PrivateSG")
    .securityGroupName(privateSG)
    .vpc(vpc)
    .allowAllOutbound(true)
    .build()


}
