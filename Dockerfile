FROM maven:3.9.7-eclipse-temurin-17-alpine AS builder
WORKDIR /code
ARG CODEARTIFACT_AUTH_TOKEN
ARG MAVEN_PROFILE=webapi-docker
ARG MAVEN_PARAMS="-DskipUnitTests -DskipITtests -D\"maven.test.skip\"=true" # can use maven options, e.g. -DskipTests=true -DskipUnitTests=true
ARG MAVEN_M2="/code/.m2/settings.xml"
ARG OPENTELEMETRY_JAVA_AGENT_VERSION=2.8.0
ENV CODEARTIFACT_AUTH_TOKEN=${CODEARTIFACT_AUTH_TOKEN}
RUN echo $CODEARTIFACT_AUTH_TOKEN && curl -LSsO https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OPENTELEMETRY_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar

# Copy .m2 folder
COPY .m2 /code/.m2

# Download dependencies
COPY pom.xml /code/
RUN mvn package -q -s ${MAVEN_M2} ${MAVEN_PARAMS} -P${MAVEN_PROFILE}
COPY src /code/src
RUN mvn package -q -s ${MAVEN_M2} ${MAVEN_PARAMS} -P${MAVEN_PROFILE} && \
    mkdir war && \
    mv target/WebAPI.war war && \
    cd war && \
    jar -xf WebAPI.war && \
    rm WebAPI.war

# OHDSI WebAPI and ATLAS web application running as a Spring Boot application with Java 11
FROM 201959883603.dkr.ecr.us-east-2.amazonaws.com/mdaca/base-images/ironbank-alpine-java:3.20.3_jdk17

# Any Java options to pass along, e.g. memory, garbage collection, etc.
ENV JAVA_OPTS=""
# Additional classpath parameters to pass along. If provided, start with colon ":"
ENV CLASSPATH=""
# Default Java options. The first entry is a fix for when java reads secure random numbers:
# in a containerized system using /dev/random may reduce entropy too much, causing slowdowns.
# https://ruleoftech.com/2016/avoiding-jvm-delays-caused-by-random-number-generation
ENV DEFAULT_JAVA_OPTS="-Djava.security.egd=file:///dev/./urandom"

# Create and make working directory to a fixed WebAPI directory
RUN addgroup -S webapi && \
    adduser -S -G webapi webapi && \
    mkdir -p /var/lib/ohdsi/webapi && \
    chown -R webapi:webapi /var/lib/ohdsi/webapi

WORKDIR /var/lib/ohdsi/webapi

COPY --from=builder --chown=101 /code/opentelemetry-javaagent.jar .

# deploy the just built OHDSI WebAPI war file
# copy resources in order of fewest changes to most changes.
# This way, the libraries step is not duplicated if the dependencies
# do not change.
COPY --from=builder --chown=webapi /code/war/WEB-INF/lib*/* WEB-INF/lib/
COPY --from=builder --chown=webapi /code/war/org org
COPY --from=builder --chown=webapi /code/war/WEB-INF/classes WEB-INF/classes
COPY --from=builder --chown=webapi /code/war/META-INF META-INF

ENV WEBAPI_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/OHDSI?currentSchema=webapi"
# ENV WEBAPI_DATASOURCE_URL="jdbc:postgresql://10.0.21.93:32000/OHDSI?currentSchema=webapi"
ENV WEBAPI_DATASOURCE_USERNAME=ohdsi_app_user
ENV WEBAPI_DATASOURCE_PASSWORD=app1
ENV WEBAPI_SCHEMA=webapi
ENV FLYWAY_DATASOURCE_USERNAME=ohdsi_admin_user
ENV FLYWAY_DATASOURCE_PASSWORD=admin1

EXPOSE 8080

USER webapi

# Directly run the code as a WAR.
CMD exec java ${DEFAULT_JAVA_OPTS} ${JAVA_OPTS} \
    -cp ".:WebAPI.jar:WEB-INF/lib/*.jar${CLASSPATH}" \
    org.springframework.boot.loader.launch.WarLauncher
