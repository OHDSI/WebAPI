FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /code

ARG MAVEN_PROFILE=trexsql
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
    -Dgit.commit.id.abbrev=${GIT_COMMIT_ID_ABBREV} \
    -P${MAVEN_PROFILE}

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

RUN mkdir -p /tmp/trexsql && \
    (unzip -j WebAPI.jar 'BOOT-INF/lib/trexsql-ext-*.jar' -d /tmp 2>/dev/null && \
    unzip -j /tmp/trexsql-ext-*.jar 'libtrexsql_java.so_linux_amd64' -d /tmp/trexsql 2>/dev/null && \
    mv /tmp/trexsql/libtrexsql_java.so_linux_amd64 /tmp/trexsql/libtrexsql_java.so 2>/dev/null && \
    rm -f /tmp/trexsql-ext-*.jar || true)

EXPOSE 8080

USER 101

# Run the executable JAR with TrexSQL native library path
CMD ["sh", "-c", "exec java ${DEFAULT_JAVA_OPTS} ${JAVA_OPTS} -Dorg.duckdb.lib_path=/tmp/trexsql/libtrexsql_java.so --add-opens java.naming/com.sun.jndi.ldap=ALL-UNNAMED -jar WebAPI.jar"]
