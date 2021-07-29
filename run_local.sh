#!/usr/bin/env bash
set -ex

VERSION=latest
TAG=$VERSION

touch webapi.env

echo "DB_HOST=postgres" >> webapi.env
echo "FEDER8_WEBAPI_SECURE=false" >> webapi.env
echo "FEDER8_WEBAPI_CENTRAL=false" >> webapi.env
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

