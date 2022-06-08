FROM golang:1.16-buster as golang-build

WORKDIR /go/src/app
COPY cmd cmd

RUN go env -w GO111MODULE=auto; \
    go install -v ./...

FROM maven:3.6.0-jdk-11 as builder

WORKDIR /code

ARG MAVEN_PROFILE=webapi-docker

# Download dependencies
COPY pom.xml /code/
RUN mkdir .git \
    && mvn package \
     -P${MAVEN_PROFILE}

ARG GIT_BRANCH=unknown
ARG GIT_COMMIT_ID_ABBREV=unknown

# Compile code and repackage it
COPY src /code/src
RUN mvn package \
    -Dgit.branch=${GIT_BRANCH} \
    -Dgit.commit.id.abbrev=${GIT_COMMIT_ID_ABBREV} \
    -P${MAVEN_PROFILE} \
    && mkdir war \
    && mv target/WebAPI.war war \
    && cd war \
    && jar -xf WebAPI.war \
    && rm WebAPI.war

# OHDSI WebAPI and ATLAS web application running as a Spring Boot application with Java 11
FROM openjdk:11-jre-slim

MAINTAINER Lee Evans - www.ltscomputingllc.com

COPY --from=golang-build /go/bin/healthcheck /app/healthcheck
HEALTHCHECK --start-period=1m --interval=1m --timeout=10s --retries=10 CMD ["/app/healthcheck"]

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

COPY docker-entrypoint.sh .

# deploy the just built OHDSI WebAPI war file
# copy resources in order of fewest changes to most changes.
# This way, the libraries step is not duplicated if the dependencies
# do not change.
COPY --from=builder /code/war/WEB-INF/lib*/* WEB-INF/lib/
COPY --from=builder /code/war/org org
COPY --from=builder /code/war/WEB-INF/classes WEB-INF/classes
COPY --from=builder /code/war/META-INF META-INF

EXPOSE 8080

USER 101

# Directly run the code as a WAR.
ENTRYPOINT ["./docker-entrypoint.sh"]
CMD ["run-webapi"]
