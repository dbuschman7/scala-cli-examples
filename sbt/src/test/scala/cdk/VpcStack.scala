package cdk

import software.amazon.awscdk.services.ec2.{SecurityGroup, SubnetConfiguration, SubnetType, Vpc}
import software.amazon.awscdk.{Stack, StackProps}
import software.constructs.Construct

final case class VpcStack(scope: Construct, props: StackProps) 
  extends Stack(scope, "ScalaCliVpc", props) 
  with Common {

  import scala.jdk.CollectionConverters._

  lazy val subnets: Seq[SubnetConfiguration] = Seq(
    SubnetConfiguration
      .builder()
      .subnetType(SubnetType.PRIVATE_ISOLATED)
      .cidrMask(24)
      .name(privateSubnet)
      .build()
  )

  lazy val vpc: Vpc = Vpc
    .Builder
    .create(this, "VPC")
    .vpcName(vpcName)
    .maxAzs(2)
    .cidr("10.0.0.0/16")
    .subnetConfiguration(subnets.asJava)
    .build()

  lazy val privateSecurityGroup: SecurityGroup = SecurityGroup.Builder
    .create(this, "PrivateSG")
    .securityGroupName(privateSG)
    .vpc(vpc)
    .allowAllOutbound(true)
    .build()


}
