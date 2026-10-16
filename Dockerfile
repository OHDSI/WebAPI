FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /code

ARG MAVEN_PARAMS="" # can use maven options, e.g. -DskipTests=true -DskipUnitTests=true

ARG OPENTELEMETRY_JAVA_AGENT_VERSION=1.17.0
RUN curl -LSsO https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OPENTELEMETRY_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar

ARG GIT_BRANCH=unknown
ARG GIT_COMMIT_ID_ABBREV=unknown

# Compile code and repackage it
COPY pom.xml /code/
COPY src /code/src
RUN mvn package ${MAVEN_PARAMS} \
    -Dpackaging.type=jar \
    -Dgit.branch=${GIT_BRANCH} \
    -Dgit.commit.id.abbrev=${GIT_COMMIT_ID_ABBREV}

# OHDSI WebAPI running as a Spring Boot executable JAR with Java 21
FROM index.docker.io/library/eclipse-temurin:21-jre

LABEL maintainer="Lee Evans - www.ltscomputingllc.com"

# Any Java options to pass along, e.g. memory, garbage collection, etc.
ENV JAVA_OPTS=""
# Default Java options. The first entry is a fix for when java reads secure random numbers:
# in a containerized system using /dev/random may reduce entropy too much, causing slowdowns.
# https://ruleoftech.com/2016/avoiding-jvm-delays-caused-by-random-number-generation
ENV DEFAULT_JAVA_OPTS="-Djava.security.egd=file:///dev/./urandom"

# set working directory to a fixed WebAPI directory
WORKDIR /var/lib/ohdsi/webapi

RUN apt-get update && apt-get install -y unzip && rm -rf /var/lib/apt/lists/*

COPY --from=builder /code/opentelemetry-javaagent.jar .
COPY --from=builder /code/target/WebAPI.jar .

# Plugin setup: download trexsql plugin JAR
ARG TREXSQL_VERSION=0.2.0
RUN mkdir -p /opt/webapi/plugins && \
    if curl -fL -o /opt/webapi/plugins/trexsql.jar \
      "https://github.com/OHDSI/trex/releases/download/v${TREXSQL_VERSION}/trexsql-${TREXSQL_VERSION}.jar"; then \
      echo "Downloaded trexsql plugin v${TREXSQL_VERSION}"; \
    else \
      echo "WARNING: Failed to download trexsql plugin v${TREXSQL_VERSION}, trexsql will be unavailable"; \
    fi

# Download native libtrexsql.so (JNA resolves it from linux-x86-64/ on the plugin classpath)
ARG LIBTREXSQL_VERSION=v1.4.4-trex
RUN mkdir -p /opt/webapi/plugins/linux-x86-64 && \
    curl -fL -o /tmp/libtrexsql.zip \
      "https://github.com/p-hoffmann/trexsql-rs/releases/download/${LIBTREXSQL_VERSION}/libtrexsql-linux-amd64.zip" && \
    unzip -j /tmp/libtrexsql.zip 'libtrexsql.so' -d /opt/webapi/plugins/linux-x86-64/ && \
    rm /tmp/libtrexsql.zip

# Create logs directory for logback before switching to non-root user
RUN mkdir -p logs && chown 101:101 logs

EXPOSE 8080

USER 101

CMD ["sh", "-c", "exec java ${DEFAULT_JAVA_OPTS} ${JAVA_OPTS} -Dloader.path=/opt/webapi/plugins --add-opens java.naming/com.sun.jndi.ldap=ALL-UNNAMED -jar WebAPI.jar"]
