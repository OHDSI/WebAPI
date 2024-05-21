#!/usr/bin/env bash
set -eux

VERSION=2.2.0
TAG=2.14.0-$VERSION

docker buildx build --rm --platform linux/amd64,linux/arm64 --pull --push -f "Dockerfile" -t $THERAPEUTIC_AREA_URL/$THERAPEUTIC_AREA/webapi:$TAG .
