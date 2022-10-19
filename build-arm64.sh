#!/usr/bin/env bash
set -eux

VERSION=2.0.3
TAG=2.9.0-$VERSION-arm64

export REGISTRY_USERNAME=admin

export REGISTRY_PASSWORD=
export REGISTRY=harbor.honeur.org
export REPOSITORY=honeur
docker buildx build --rm --platform linux/arm64 --pull --push -f "Dockerfile" -t $REGISTRY/$REPOSITORY/webapi:$TAG .

export REGISTRY_PASSWORD=
export REGISTRY=harbor.athenafederation.org
export REPOSITORY=athena
docker buildx build --rm --platform linux/arm64 --pull --push -f "Dockerfile" -t $REGISTRY/$REPOSITORY/webapi:$TAG .

export REGISTRY_PASSWORD=
export REGISTRY=harbor.lupusnet.org
export REPOSITORY=lupus
docker buildx build --rm --platform linux/arm64 --pull --push -f "Dockerfile" -t $REGISTRY/$REPOSITORY/webapi:$TAG .

export REGISTRY_PASSWORD=
export REGISTRY=harbor.esfurn.org
export REPOSITORY=esfurn
docker buildx build --rm --platform linux/arm64 --pull --push -f "Dockerfile" -t $REGISTRY/$REPOSITORY/webapi:$TAG .

export REGISTRY_PASSWORD=
export REGISTRY=harbor.phederation.org
export REPOSITORY=phederation
docker buildx build --rm --platform linux/arm64 --pull --push -f "Dockerfile" -t $REGISTRY/$REPOSITORY/webapi:$TAG .