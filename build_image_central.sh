#!/usr/bin/env bash
set -e

./mvnw clean install -s settings.xml -Pcentral -DskipTests
