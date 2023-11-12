## Scala cli

## Package 
```shell
scala-cli package --scala 2.13 \
	--main-class bootstrap \
	--native-image  \
        . \
	-v -f -o target/lambda -f \
	-- --enable-url-protocols=http --no-fallback \
    --initialize-at-build-time=org.slf4j \
    --initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils \
	-H:+ReportExceptionStackTraces
    ```