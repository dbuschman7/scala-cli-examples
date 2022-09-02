package cdk

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import org.scalatest.funsuite.AnyFunSuite

class CdkSynth extends AnyFunSuite {

  test("main") {
    val app = new App()

    new ExampleStack(app, "ExampleStack", StackProps.builder()
//            .env(Environment.builder()
//                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
//                        .region(System.getenv("CDK_DEFAULT_REGION"))
//                        .build()
//                )
            .build())

    app.synth()

  }
}
