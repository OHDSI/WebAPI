#!/usr/bin/env bash
set -eux

VERSION=2.2.0
TAG=2.14.0-$VERSION

docker tag feder8/webapi:latest $THERAPEUTIC_AREA_URL/$THERAPEUTIC_AREA/webapi:$TAG
docker push $THERAPEUTIC_AREA_URL/$THERAPEUTIC_AREA/webapi:$TAG
