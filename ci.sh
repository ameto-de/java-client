#!/usr/bin/env bash
set -e

setup_test_env() {
    docker-compose -f docker-compose.ci.yml up -d kafka
    sleep 10
    docker-compose -f docker-compose.ci.yml up -d
    sleep 10
    local project_name=$(basename $(pwd))
    local network_name=${project_name//-/}_default
    python3.6 -m ametoctl operators package noop-operator.toml | docker run --interactive --network=${network_name} --rm dev.digitalernachschub.de/ameto/ametoctl:0.4.1 --broker kafka:9092 operators add -
    local api_token=$(docker run --interactive --network=${network_name} --rm dev.digitalernachschub.de/ameto/ametoctl:0.4.1 --broker kafka:9092 users add_token)
    export AMETO_API_TOKEN=${api_token}
}

run_tests() {
    local project_name=$(basename $(pwd))
    local network_name=${project_name//-/}_default
    docker run --network=${network_name} --env AMETO_API_URL=http://api:5000 --env AMETO_API_TOKEN=${AMETO_API_TOKEN} \
      --mount type=volume,source=${project_name}_gradle_cache,target=/home/gradle/.gradle --rm ameto/java-client-tests
}

tear_down_test_env() {
    docker-compose -f docker-compose.ci.yml down
}

docker build -f Dockerfile.test -t ameto/java-client-tests .

trap tear_down_test_env EXIT
setup_test_env
run_tests
