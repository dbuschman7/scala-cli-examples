#! /bin/sh 

export FALLBACK_EXECUTOR_VERBOSE=true
scala-cli clean .
scala-cli package --scala 2.13 \
        --dependency dev.zio::zio-lambda:1.0.0-RC6 \
        --dependency dev.zio::zio-json:0.3.0-RC11 \
        --main-class me.lightspeed7.examples.cli.CollectMain \
        --native-image  \
        sbt/src/main/scala/me/lightspeed7/examples/library/Library.scala \
        sbt/src/main/scala/me/lightspeed7/examples/cli/CollectMain.scala \
        -v -f -o collectMain -f \
        -- --enable-url-protocols=http --no-fallback \
        -H:+ReportExceptionStackTraces
