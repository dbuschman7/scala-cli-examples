package cdk

import software.amazon.awscdk.{Stack, StackProps}
import software.amazon.awscdk.services.s3.Bucket
import software.constructs.Construct;

final case class ExampleStack(val scope: Construct, val id: String, val props: StackProps = null) extends Stack(scope, id, props) {

  lazy val first: Bucket = Bucket
    .Builder
    .create(this, "MyFirstBucket")
    .versioned(true)
    .build();

}

