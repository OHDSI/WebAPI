#!/bin/sh

set -e

if [ -n "${TOMCAT_ADMIN_PASSWORD}" ]; then
  sed -i s|password=\"abc123\"|password=\""${TOMCAT_ADMIN_PASSWORD}"\"|g /usr/local/tomcat/conf/tomcat-users.xml
fi

exec "$@"