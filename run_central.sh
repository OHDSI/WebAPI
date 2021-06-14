#!/usr/bin/env bash
set -ex

VERSION=latest
TAG=$VERSION

touch webapi.env

echo "DB_HOST=postgres" >> webapi.env
echo "FEDER8_WEBAPI_SECURE=true" >> webapi.env
echo "FEDER8_WEBAPI_CENTRAL=true" >> webapi.env
echo "FEDER8_WEBAPI_OIDC_CLIENT_ID=default" >> webapi.env
echo "FEDER8_WEBAPI_OIDC_SECRET=default-secret" >> webapi.env
echo "FEDER8_WEBAPI_OIDC_ISSUER_URI=https://cas-dev.honeur.org/oidc/.well-known/openid-configuration" >> webapi.env
echo "FEDER8_WEBAPI_OIDC_REDIRECT_URL_API=http://localhost:8080/user/oauth/callback" >> webapi.env
echo "FEDER8_WEBAPI_OIDC_REDIRECT_URL_UI=http://localhost:8081/index.html#/welcome/" >> webapi.env
echo "JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=54322" >> webapi.env

docker run \
--rm \
--name webapi \
-p 8080:8080 \
-p 54322:54322 \
-v shared:/var/lib/shared \
--env-file webapi.env \
--network honeur-net \
feder8/webapi:$TAG

rm -rf webapi.env

