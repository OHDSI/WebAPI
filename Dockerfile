FROM maven:3.6-jdk-11 as builder

WORKDIR /code

ARG MAVEN_PROFILE=webapi-docker
ARG MAVEN_PARAMS="" # can use maven options, e.g. -DskipTests=true -DskipUnitTests=true

ARG OPENTELEMETRY_JAVA_AGENT_VERSION=1.17.0
RUN curl -LSsO https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OPENTELEMETRY_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar

# Download dependencies
COPY pom.xml /code/
RUN mkdir .git \
    && mvn package \
    -P${MAVEN_PROFILE}

ARG GIT_BRANCH=unknown
ARG GIT_COMMIT_ID_ABBREV=unknown

# Compile code and repackage it
COPY src /code/src
RUN mvn package ${MAVEN_PARAMS} \
    -Dgit.branch=${GIT_BRANCH} \
    -Dgit.commit.id.abbrev=${GIT_COMMIT_ID_ABBREV} \
    -P${MAVEN_PROFILE} \
    && mkdir war \
    && mv target/WebAPI.war war \
    && cd war \
    && jar -xf WebAPI.war \
    && rm WebAPI.war

# OHDSI WebAPI and ATLAS web application running as a Spring Boot application with Java 8
FROM amazoncorretto:8u402-al2023

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

EXPOSE 8080

USER 0

RUN mkdir /usr/local/share/aws-certs \
    && curl https://truststore.pki.rds.amazonaws.com/us-east-1/us-east-1-bundle.pem -o /usr/local/share/aws-certs/us-east-1-bundle.pem \
    && cd $JAVA_HOME/jre/lib/security \
    && keytool -import -trustcacerts -storepass changeit -noprompt -alias aws -file /usr/local/share/aws-certs/us-east-1-bundle.pem 

USER 101

# Directly run the code as a WAR.
CMD exec java ${DEFAULT_JAVA_OPTS} ${JAVA_OPTS} \
    -cp ".:WebAPI.jar:WEB-INF/lib/*.jar${CLASSPATH}" \
    org.springframework.boot.loader.WarLauncher