#!/bin/sh 
set -ex 

ls -l / 
ls -l /build 
cd /build 

scala-cli  clean .
scala-cli package --native-image  \
        Lambda.scala \
        -v -f -o lambdaBinary -f \
        -- --enable-url-protocols=http --no-fallback \
        -H:+ReportExceptionStackTraces

scala clean .
# DONE 
