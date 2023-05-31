#!/usr/bin/env bash
#set -eux

VERSION=2.1.0
TAG=2.9.0-$VERSION

REGISTRY=harbor.honeur.org
REPOSITORY=honeur
docker manifest rm $REGISTRY/$REPOSITORY/webapi:$TAG
docker manifest create $REGISTRY/$REPOSITORY/webapi:$TAG $REGISTRY/$REPOSITORY/webapi:$TAG-amd64 $REGISTRY/$REPOSITORY/webapi:$TAG-arm64
docker manifest push $REGISTRY/$REPOSITORY/webapi:$TAG

export REGISTRY=harbor.athenafederation.org
export REPOSITORY=athena
docker manifest rm $REGISTRY/$REPOSITORY/webapi:$TAG
docker manifest create $REGISTRY/$REPOSITORY/webapi:$TAG $REGISTRY/$REPOSITORY/webapi:$TAG-amd64 $REGISTRY/$REPOSITORY/webapi:$TAG-arm64
docker manifest push $REGISTRY/$REPOSITORY/webapi:$TAG

export REGISTRY=harbor.lupusnet.org
export REPOSITORY=lupus
docker manifest rm $REGISTRY/$REPOSITORY/webapi:$TAG
docker manifest create $REGISTRY/$REPOSITORY/webapi:$TAG $REGISTRY/$REPOSITORY/webapi:$TAG-amd64 $REGISTRY/$REPOSITORY/webapi:$TAG-arm64
docker manifest push $REGISTRY/$REPOSITORY/webapi:$TAG

export REGISTRY=harbor.esfurn.org
export REPOSITORY=esfurn
docker manifest rm $REGISTRY/$REPOSITORY/webapi:$TAG
docker manifest create $REGISTRY/$REPOSITORY/webapi:$TAG $REGISTRY/$REPOSITORY/webapi:$TAG-amd64 $REGISTRY/$REPOSITORY/webapi:$TAG-arm64
docker manifest push $REGISTRY/$REPOSITORY/webapi:$TAG

export REGISTRY=harbor.phederation.org
export REPOSITORY=phederation
docker manifest rm $REGISTRY/$REPOSITORY/webapi:$TAG
docker manifest create $REGISTRY/$REPOSITORY/webapi:$TAG $REGISTRY/$REPOSITORY/webapi:$TAG-amd64 $REGISTRY/$REPOSITORY/webapi:$TAG-arm64
docker manifest push $REGISTRY/$REPOSITORY/webapi:$TAG
