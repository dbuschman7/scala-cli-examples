package cdk

import software.amazon.awscdk.{App, Environment, StackProps}

object CdkSynth extends scala.App {

  val account: String = sys.env.getOrElse("AWS_ACCOUNT", "UNKNOWN")
  val region: String = sys.env.getOrElse("AWS_REGION", "UNKNOWN")

  val env = Environment.builder().region(region).account(account).build()
  val props: StackProps = StackProps.builder().env(env).build()

  val app: App = new App()

  val example = ExampleStack(app, "First", props)
  //
  //

  val vpcStack = VpcStack(app, props)
  vpcStack.vpc
  vpcStack.privateSecurityGroup
  //
  //
  val pingLambda = PingLambdaStack(app, props)
  pingLambda.lambda

  val dockerLambda = DockerLambdaStack(app, props)
  dockerLambda.ecrRepository

  //
  app.synth()

}
