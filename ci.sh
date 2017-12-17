#!/usr/bin/env bash

docker pull digitalernachschub.de

docker-compose up -d kafka
sleep 10
docker-compose up

gradle test
