# FROM maven:3.9.7-eclipse-temurin-17-alpine AS builder
FROM mip-sf-harbor.med.osd.ds/mip-sf/jdk17-alpine-images-main:latest AS builder

WORKDIR /code

ARG MAVEN_PROFILE=webapi-docker
ARG MAVEN_PARAMS="-DskipUnitTests -DskipITtests -D\"maven.test.skip\"=true" # can use maven options, e.g. -DskipTests=true -DskipUnitTests=true

# Install curl
RUN apk add --no-cache curl

ARG OPENTELEMETRY_JAVA_AGENT_VERSION=1.17.0
RUN curl -LSsO https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OPENTELEMETRY_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar

RUN mkdir war
COPY WebAPI.war war/WebAPI.war 
RUN cd war \
&& jar -xf WebAPI.war \
   && rm WebAPI.war

# OHDSI WebAPI and ATLAS web application running as a Spring Boot application with Java 11
# FROM openjdk:17-jdk-slim
# FROM eclipse-temurin:17-jre-alpine
FROM mip-sf-harbor.med.osd.ds/mip-sf/jdk17-alpine-images-main:latest

# Any Java options to pass along, e.g. memory, garbage collection, etc.
ENV JAVA_OPTS=""
# Additional classpath parameters to pass along. If provided, start with colon ":"
ENV CLASSPATH=""
# Default Java options. The first entry is a fix for when java reads secure random numbers:
# in a containerized system using /dev/random may reduce entropy too much, causing slowdowns.
# https://ruleoftech.com/2016/avoiding-jvm-delays-caused-by-random-number-generation
ENV DEFAULT_JAVA_OPTS="-Djava.security.egd=file:///dev/./urandom"

# set working directory to a fixed WebAPI directory
WORKDIR /var/lib/ohdsi/webapi

COPY --from=builder /code/opentelemetry-javaagent.jar .

# deploy the just built OHDSI WebAPI war file
# copy resources in order of fewest changes to most changes.
# This way, the libraries step is not duplicated if the dependencies
# do not change.
COPY --from=builder /code/war/WEB-INF/lib*/* WEB-INF/lib/
COPY --from=builder /code/war/org org
COPY --from=builder /code/war/WEB-INF/classes WEB-INF/classes
COPY --from=builder /code/war/META-INF META-INF

ENV WEBAPI_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/OHDSI?currentSchema=webapi"
# ENV WEBAPI_DATASOURCE_URL="jdbc:postgresql://10.0.21.93:32000/OHDSI?currentSchema=webapi"
ENV WEBAPI_DATASOURCE_USERNAME=ohdsi_app_user
ENV WEBAPI_DATASOURCE_PASSWORD=app1
ENV WEBAPI_SCHEMA=webapi
ENV FLYWAY_DATASOURCE_USERNAME=ohdsi_admin_user
ENV FLYWAY_DATASOURCE_PASSWORD=admin1

EXPOSE 8080

USER 101

# Directly run the code as a WAR.
CMD exec java ${DEFAULT_JAVA_OPTS} ${JAVA_OPTS} \
    -cp ".:WebAPI.jar:WEB-INF/lib/*.jar${CLASSPATH}" \
    org.springframework.boot.loader.launch.WarLauncher