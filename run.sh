#!/usr/bin/env bash
set -ex

VERSION=2.0.0
TAG=2.9.0-$VERSION

touch webapi.env

#echo "DB_HOST=postgres" >>  omopcdm-add-base-indexes.env

docker run \
--rm \
--name webapi \
-it \
-p 8080:8080 \
-v shared:/var/lib/shared \
--env-file webapi.env \
--network honeur-net \
feder8/webapi:$TAG bash

rm -rf webapi.env

