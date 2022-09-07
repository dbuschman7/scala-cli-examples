#! /bin/sh 
set -x 

echo "PWD -> $PWD" 
source $(PWD)/version.sh 

echo "Version $VERSION"

scala-cli -S 2.13 package --library \
	sbt/src/main/scala/me/lightspeed7/examples/library/AWS.scala \
	sbt/src/main/scala/me/lightspeed7/examples/library/Library.scala 


scala-cli -S 2.13 publish local \
	--organization me.lightspeed7 --name scala-cli-library --version $VERSION  \
	sbt/src/main/scala/me/lightspeed7/examples/library/AWS.scala \
	sbt/src/main/scala/me/lightspeed7/examples/library/Library.scala 

