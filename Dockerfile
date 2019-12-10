FROM maven:3.6-jdk-8 as builder

WORKDIR /code

# Download dependencies
COPY pom.xml /code/
RUN mvn package -Pwebapi-postgresql -DskipTests

# Compile code
COPY src /code/src
RUN mvn package -Pwebapi-postgresql -DskipTests

# OHDSI WebAPI and ATLAS web application running in a Tomcat 8 server on Java JRE 8
FROM tomcat:8-jre8

MAINTAINER Lee Evans - www.ltscomputingllc.com

ENV JAVA_OPTS=-Djava.security.egd=file:///dev/./urandom
ENV TOMCAT_ADMIN_PASSWORD=password

# set working directory to the Tomcat server webapps directory
WORKDIR /usr/local/tomcat/webapps

# install the bash shell deploy script that supervisord will run whenever the container is started
COPY docker/deploy-script.sh /usr/local/tomcat/bin/

# add a Tomcat server management web UI 'admin' user with default 'abc123' password!
COPY docker/tomcat-users.xml /usr/local/tomcat/conf/

COPY docker/entrypoint.sh /entrypoint.sh

RUN chmod +x /usr/local/tomcat/bin/deploy-script.sh /entrypoint.sh

# deploy the latest OHDSI WebAPI war file from the OHDSI CI Nexus repository (built from GitHub OHDSI WebAPI released branch)
COPY --from=builder /code/target/WebAPI.war /usr/local/tomcat/webapps/

# run supervisord to execute the deploy script (which also starts the tomcat server)
CMD ["/usr/local/tomcat/bin/deploy-script.sh"]

ENTRYPOINT ["/entrypoint.sh"]

EXPOSE 8080
