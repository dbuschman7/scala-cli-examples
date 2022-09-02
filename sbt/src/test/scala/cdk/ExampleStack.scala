package cdk

import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.s3.Bucket
import software.constructs.Construct;

class ExampleStack(val scope: Construct, val id: String, val props: StackProps = null) extends Stack(scope, id, props) {

    Bucket
        .Builder
        .create(this, "MyFirstBucket")
        .versioned(true)
        .build();

}

