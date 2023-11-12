FROM  ghcr.io/graalvm/native-image:22.2.0 AS native-build

RUN curl -fLo scala-cli.rpm https://github.com/Virtuslab/scala-cli/releases/latest/download/scala-cli-x86_64-pc-linux.rpm \
 && rpm -i scala-cli.rpm

RUN echo "//> using scala \"2.13\"    "  > /root/Build.scala \
 && echo "//> using platform \"jdk\"  " >> /root/Build.scala \
 && echo ""                             >> /root/Build.scala \
 && echo "object App extends App {"     >> /root/Build.scala \
 && echo "  println(\"Tag your it\")"   >> /root/Build.scala \
 && echo "}"                            >> /root/Build.scala \
 && cat                                    /root/Build.scala

RUN scala-cli package --scala 2.13 \
	--main-class bootstrap \
	--native-image  \
        /root/Build.scala \
	-v -f -o /root/tag-your-it -f \
	-- --enable-url-protocols=http --no-fallback \
    --initialize-at-build-time=org.slf4j \
    --initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils \
	-H:+ReportExceptionStackTraces


# Copy in the app to compile 
RUN mkdir /build 
WORKDIR /build 

COPY *.scala /build 
COPY resources /build

RUN scala-cli package --scala 2.13 \
	--main-class bootstrap \
	--native-image  \
        . \
	-v -f -o target/lambda -f \
	-- --enable-url-protocols=http --no-fallback \
    --initialize-at-build-time=org.slf4j \
    --initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils \
	-H:+ReportExceptionStackTraces

 RUN ls -l /build/target/lambda
    
