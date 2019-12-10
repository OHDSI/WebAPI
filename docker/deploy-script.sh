# load any jdbc drivers in the docker host volume mapped directory into the tomcat library
if [ -d /var/lib/ohdsi/webapi/drivers ]; then
  cp /var/lib/ohdsi/webapi/drivers/*.jar /usr/local/tomcat/lib
fi

# start tomcat
/usr/local/tomcat/bin/catalina.sh run
