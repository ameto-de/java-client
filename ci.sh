#!/usr/bin/env bash
docker build -f Dockerfile.test -t ameto/java-client-tests .

docker-compose -f docker-compose.ci.yml up -d kafka
sleep 10
docker-compose -f docker-compose.ci.yml up -d

docker run --network=javaclient_default --env AMETO_API_URL=http://api:5000 --rm ameto/java-client-tests
