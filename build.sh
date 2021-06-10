#!/usr/bin/env bash
set -eux

docker build --pull --rm -f "Dockerfile" -t feder8/webapi:latest "."
