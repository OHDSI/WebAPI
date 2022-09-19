#!/usr/bin/env bash
set -eux

VERSION=2.0.2
TAG=2.9.0-$VERSION-amd64

export REGISTRY_USERNAME=admin

export REGISTRY_PASSWORD=
export REGISTRY=harbor.honeur.org
export REPOSITORY=honeur
docker build --pull --rm -f "Dockerfile" -t $REGISTRY/$REPOSITORY/webapi:$TAG "."
docker push $REGISTRY/$REPOSITORY/webapi:$TAG

export REGISTRY_PASSWORD=
export REGISTRY=harbor.athenafederation.org
export REPOSITORY=athena
docker build --pull --rm -f "Dockerfile" -t $REGISTRY/$REPOSITORY/webapi:$TAG "."
docker push $REGISTRY/$REPOSITORY/webapi:$TAG

export REGISTRY_PASSWORD=
export REGISTRY=harbor.lupusnet.org
export REPOSITORY=lupus
docker build --pull --rm -f "Dockerfile" -t $REGISTRY/$REPOSITORY/webapi:$TAG "."
docker push $REGISTRY/$REPOSITORY/webapi:$TAG

export REGISTRY_PASSWORD=
export REGISTRY=harbor.esfurn.org
export REPOSITORY=esfurn
docker build --pull --rm -f "Dockerfile" -t $REGISTRY/$REPOSITORY/webapi:$TAG "."
docker push $REGISTRY/$REPOSITORY/webapi:$TAG

export REGISTRY_PASSWORD=
export REGISTRY=harbor.phederation.org
export REPOSITORY=phederation
docker build --pull --rm -f "Dockerfile" -t $REGISTRY/$REPOSITORY/webapi:$TAG "."
docker push $REGISTRY/$REPOSITORY/webapi:$TAG
