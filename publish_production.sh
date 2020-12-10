#!/usr/bin/env bash
set -e

export AWS_DEFAULT_REGION='eu-west-1';
aws ecr get-login --no-include-email | bash;
docker pull 973455288590.dkr.ecr.eu-west-1.amazonaws.com/honeur/webapi:1.7-UAT;
docker tag 973455288590.dkr.ecr.eu-west-1.amazonaws.com/honeur/webapi:1.7-UAT 973455288590.dkr.ecr.eu-west-1.amazonaws.com/honeur/webapi:1.7
docker push 973455288590.dkr.ecr.eu-west-1.amazonaws.com/honeur/webapi:1.7