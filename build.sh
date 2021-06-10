#!/usr/bin/env bash
set -eux

VERSION=2.0.0
TAG=2.9.0-$VERSION

docker build --pull --rm -f "Dockerfile" -t feder8/webapi:$TAG "."
