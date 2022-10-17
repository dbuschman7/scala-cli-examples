#! /bin/sh
set -ex 


export AWS_LAMBDA_RUNTIME_API="http://localhost"
export VERSION="0.0.1"

docker build --target runtime -t alpine-jvm-jlink:11 . 
docker images | grep alpine-jvm-jlink

#
#
scala-cli clean   . 
scala-cli compile Lambda.scala 

rm -f LambdaMain.jar

scala-cli package --assembly Lambda.scala 

docker run -v $(pwd):/build --entrypoint /build/make-binary.sh virtuslab/scala-cli

ls -lh *.jar 
ls -h lambdaBinary

jeps  -s LambdaMain.jar >  modules.list
docker build --target deploy -t docker-lambda-jre:$VERSION .
docker build --target binary -t docker-lambda-bin:$VERSION .
docker images | grep docker-lambda

set +e 
docker run -e AWS_LAMBDA_RUNTIME_API=${AWS_LAMBDA_RUNTIME_API} -ti docker-lambda-jre:0.0.1 
docker run -e AWS_LAMBDA_RUNTIME_API=${AWS_LAMBDA_RUNTIME_API} -ti docker-lambda-bin:0.0.1 

# DONE
