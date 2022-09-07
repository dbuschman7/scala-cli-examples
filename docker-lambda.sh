#! /bin/sh 
set -ex 

source version.sh

# ############################
# Setup and pre-clean
# ############################
mkdir -p target/bin
mkdir -p target/opt
mkdir -p target/var/task 

rm -fv   target/bin/*
rm -fv   target/opt/*
rm -fv   target/var/task/*
rm -f    target/upload.zip 

# ############################
# Make the artifact
# ############################

export FALLBACK_EXECUTOR_VERBOSE=true 
scala-cli clean . 
scala-cli package --scala 2.13 \
	--dependency dev.zio::zio-lambda:1.0.0-RC6 \
	--dependency dev.zio::zio-json:0.3.0-RC11 \
	--main-class me.lightspeed7.examples.lambda.LambdaMain \
	--docker  \
        sbt/src/main/scala/me/lightspeed7/examples/library/Library.scala \
        sbt/src/main/scala/me/lightspeed7/examples/lambda/LambdaMain.scala \
	--docker-image-repository ${AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com/docker-image:${VERSION}


# ############################
# Create the lambda zip 
# ############################


# ############################
# AWS Upload 
# ############################
#aws lambda update-function-code \
#	--function-name scala-cli-lambda \
#	--region ${AWS_REGION} \
#	--zip-file fileb://$(pwd)/upload.zip \\
#	--publish \
#	--cli-connect-timeout 6000
#


# ############################
# DONE 
# ############################
