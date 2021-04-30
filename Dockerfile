FROM tomcat:9-jdk8-openjdk-slim

COPY target-central/webapi.war /usr/local/tomcat/webapps/webapi.war