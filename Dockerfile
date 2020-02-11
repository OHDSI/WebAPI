FROM tomcat:9.0.30-jdk8-openjdk-slim

COPY target-central/webapi.war /usr/local/tomcat/webapps/ROOT.war