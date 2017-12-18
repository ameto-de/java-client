#!/usr/bin/env bash
docker-compose up -d kafka
sleep 10
docker-compose up

./gradlew test
