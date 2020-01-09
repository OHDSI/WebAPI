FROM maven:3.6-jdk-8 as builder

WORKDIR /code

ARG MAVEN_PROFILE=webapi-postgresql

# Download dependencies
COPY pom.xml /code/
RUN mvn package -P${MAVEN_PROFILE} -DskipTests

# Compile code and repackage it
COPY src /code/src
RUN mvn package -P${MAVEN_PROFILE} -DskipTests \
    && mkdir war \
    && mv target/WebAPI.war war \
    && cd war \
    && unzip WebAPI.war \
    && rm WebAPI.war

# OHDSI WebAPI and ATLAS web application running as a Spring Boot application with Java 11
FROM openjdk:11

MAINTAINER Lee Evans - www.ltscomputingllc.com

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

# deploy the just built OHDSI WebAPI war file
# copy resources in order of fewest changes to most changes.
# This way, the libraries step is not duplicated if the dependencies
# do not change.
COPY --from=builder /code/war/WEB-INF/lib*/* WEB-INF/lib/
COPY --from=builder /code/war/org org
COPY --from=builder /code/war/WEB-INF/classes WEB-INF/classes
COPY --from=builder /code/war/META-INF META-INF

EXPOSE 8080

# Directly run the code as a WAR.
CMD exec java ${DEFAULT_JAVA_OPTS} ${JAVA_OPTS} \
    -cp ".:WebAPI.jar:WEB-INF/lib/*.jar${CLASSPATH}" \
    org.springframework.boot.loader.WarLauncher
