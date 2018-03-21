FROM hub.arachnenetwork.com/atlasenv:1.0.4
MAINTAINER vitaly.koulakov@odysseusinc.com

COPY run.sh /bin/run.sh
RUN dos2unix /bin/run.sh

ADD WebAPI.war /opt/tomcat/webapps/WebAPI.war

COPY atlasui.zip atlasui.zip
RUN unzip atlasui.zip -d /opt/tomcat/webapps/ROOT && rm atlasui.zip

VOLUME /opt/tomcat/webapps/ROOT/config

RUN chmod +x /bin/run.sh

EXPOSE 8080

CMD ["/bin/run.sh"]