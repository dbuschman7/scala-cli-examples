#!/bin/sh 
set -ex 

SCALA="--scala 2.13"
CDK_BASE="sbt/src/test/scala/cdk"
DEPENDS="--dependency software.amazon.awscdk:aws-cdk-lib:2.40.0"
 
scala-cli clean                                             . 
scala-cli compile $SCALA $DEPENDS                           $CDK_BASE
scala-cli run     $SCALA $DEPENDS --main-class cdk.CdkSynth $CDK_BASE

# DONE 
