#!/usr/bin/env bash
set -e

setup_test_env() {
    docker-compose -f docker-compose.ci.yml up -d kafka
    sleep 10
    docker-compose -f docker-compose.ci.yml up -d
    sleep 10
    local project_name=$(basename $(pwd))
    local network_name=${project_name//-/}_default
    python3.6 -m ametoctl package noop-operator.toml | docker run --interactive --network=${network_name} --rm dev.digitalernachschub.de/ameto/ametoctl --broker kafka:9092 add -
}

run_tests() {
    local project_name=$(basename $(pwd))
    local network_name=${project_name//-/}_default
    docker run --network=${network_name} --env AMETO_API_URL=http://api:5000 --env AMETO_DELIVERY_URL=http://delivery:80/ \
      --mount type=volume,source=${project_name}_gradle_cache,target=/home/gradle/.gradle --rm ameto/java-client-tests
}

tear_down_test_env() {
    docker-compose -f docker-compose.ci.yml down
}

docker build -f Dockerfile.test -t ameto/java-client-tests .

trap tear_down_test_env EXIT
setup_test_env
run_tests
