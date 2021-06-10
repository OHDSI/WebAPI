#!/usr/bin/env bash
set -ex

VERSION=2.0.0
TAG=2.9.0-$VERSION

touch webapi.env

echo "DB_HOST=postgres" >> webapi.env
echo "FEDER8_WEBAPI_SECURE=true" >> webapi.env
echo "FEDER8_WEBAPI_CENTRAL=true" >> webapi.env
echo "FEDER8_WEBAPI_OIDC_CLIENT_ID=default" >> webapi.env
echo "FEDER8_WEBAPI_OIDC_SECRET=default-secret" >> webapi.env
echo "FEDER8_WEBAPI_OIDC_ISSUER_URI=https://cas-dev.honeur.org/oidc/.well-known/openid-configuration" >> webapi.env
echo "FEDER8_WEBAPI_OIDC_REDIRECT_URL=http://localhost/index.html#/welcome/" >> webapi.env

docker run \
--rm \
--name webapi \
-p 8080:8080 \
-v shared:/var/lib/shared \
--env-file webapi.env \
--network honeur-net \
feder8/webapi:$TAG

rm -rf webapi.env

