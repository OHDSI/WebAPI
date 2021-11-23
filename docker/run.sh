#!/bin/bash

service rsyslog start
service logrotate start
service cron start
echo "WEBAPI WILL START"
/usr/local/tomcat/bin/startup.sh
tail -f /usr/local/tomcat/logs/catalina.out
exit 0