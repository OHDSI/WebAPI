#!/usr/bin/env bash
set -eux

VERSION=2.0.3
TAG=2.9.0-$VERSION
export REGISTRY=harbor-uat.honeur.org
export REPOSITORY=honeur
#export REGISTRY=harbor-uat.athenafederation.org
#export REPOSITORY=athena
#export REGISTRY=harbor-uat.lupusnet.org
#export REPOSITORY=lupus
export REGISTRY_USERNAME=admin
export REGISTRY_PASSWORD=harbor_password

docker buildx build --rm --platform linux/amd64,linux/arm64 --pull --push -f "Dockerfile" -t $REGISTRY/$REPOSITORY/webapi:$TAG .
