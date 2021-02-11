#!/usr/bin/env bash
set -eux

docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v $(which docker):$(which docker) -v $HOME/.m2:/root/.m2 -v $(pwd):/opt/app openjdk:11-jdk-slim-buster bash -c "set -eux; cd /opt/app; ./mvnw clean install -s settings.xml -Pcentral -DskipTests; chown -R $(id -u) /opt/app; chown -R $(id -u) /root/.m2"

docker build --no-cache -t honeur/webapi .