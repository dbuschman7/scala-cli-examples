FROM amazoncorretto:17-alpine3.16-jdk as build-jre
RUN java -version 

RUN jlink --compress=2 --no-header-files --no-man-pages \
     --module-path /opt/jdk/jdk-11/jmods \
     --add-modules java.base,jdk.unsupported,java.management,jdk.management \
     --output /linked

FROM alpine:3.16 as runtime
COPY --from=build-jre /linked /opt/jdk/
ENV PATH=$PATH:/opt/jdk/bin

CMD java -version

# Custom JRE base image 
# ######################################
FROM alpine-jvm-jlink:11 as deploy 

COPY LambdaMain.jar /
CMD java -jar       /LambdaMain.jar


# Binary deploy 
# ##########################
FROM alpine:3.16 as binary 
COPY        lambdaBinary / 
ENTRYPOINT               /lambdaBinary

# DONE 
