#!/usr/bin/env bash

if [ "$1" = 'run-webapi' ]; then
  set -e
  if [ -f "/var/lib/shared/honeur.env" ]; then
    source /var/lib/shared/honeur.env
    export $(cut -d= -f1 /var/lib/shared/honeur.env)
  fi

  if [[ ! -z "$FEDER8_WEBAPI_SECURE" && "$FEDER8_WEBAPI_SECURE" == "true" ]]; then
    JAVA_OPTS="${JAVA_OPTS} -Dsecurity.provider=AtlasRegularSecurity"

    if [[ ! -z "$FEDER8_WEBAPI_CENTRAL" && "$FEDER8_WEBAPI_CENTRAL" == "true" ]]; then
      JAVA_OPTS="${JAVA_OPTS} -Dsecurity.oid.clientId=${FEDER8_WEBAPI_OIDC_CLIENT_ID} -Dsecurity.oid.apiSecret=${FEDER8_WEBAPI_OIDC_SECRET} -Dsecurity.oid.url=${FEDER8_WEBAPI_OIDC_ISSUER_URI} -Dsecurity.oid.redirectUrl=${FEDER8_WEBAPI_OIDC_REDIRECT_URL} -Dsecurity.oauth.callback.api=${FEDER8_WEBAPI_OIDC_REDIRECT_URL_API} -Dsecurity.oauth.callback.ui=${FEDER8_WEBAPI_OIDC_REDIRECT_URL_UI}"
    elif [[ ! -z "$FEDER8_WEBAPI_AUTH_METHOD" && "$FEDER8_WEBAPI_AUTH_METHOD" == "ldap" ]]; then
      JAVA_OPTS="${JAVA_OPTS} -Dsecurity.ldap.dn=${FEDER8_WEBAPI_LDAP_DN} -Dsecurity.ldap.url=${FEDER8_WEBAPI_LDAP_URL} -Dsecurity.ldap.baseDn=${FEDER8_WEBAPI_LDAP_BASEDN} -Dsecurity.ldap.system.username=${FEDER8_WEBAPI_LDAP_SYSTEM_USERNAME} -Dsecurity.ldap.system.password=${FEDER8_WEBAPI_LDAP_SYSTEM_PASSWORD}"
    fi
  fi

  exec java ${DEFAULT_JAVA_OPTS} ${JAVA_OPTS} \
    -cp ".:WebAPI.jar:WEB-INF/lib/*.jar${CLASSPATH}" \
    org.springframework.boot.loader.WarLauncher
fi

exec "$@"
