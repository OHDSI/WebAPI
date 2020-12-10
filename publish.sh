#!/usr/bin/env bash
set -e

docker tag honeur/webapi:latest 973455288590.dkr.ecr.eu-west-1.amazonaws.com/honeur/webapi:1.7-UAT
export AWS_DEFAULT_REGION='eu-west-1'
aws ecr get-login --no-include-email | bash
docker push 973455288590.dkr.ecr.eu-west-1.amazonaws.com/honeur/webapi:1.7-UAT