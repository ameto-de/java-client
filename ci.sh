#!/usr/bin/env bash
set -e

setup_test_env() {
    docker-compose -f docker-compose.ci.yml up -d kafka object-store
    sleep 10
    local project_name=$(basename $(pwd))
    local network_name=${project_name}_default
    docker run --interactive --network=${network_name} \
        --env AMETO_DEFAULT_USER_TOPICS=jobs:1:1,pipelines:1:1,uploaded:1:1,api_tokens:1:1,processed:1:1 \
        --rm dev.digitalernachschub.de/ameto/ametoctl:0.8.0 --broker kafka:9092 users add testuser
    local api_token=$(docker run --interactive --network=${network_name} --rm dev.digitalernachschub.de/ameto/ametoctl:0.8.0 --broker kafka:9092 users add_token testuser admin)
    export AMETO_API_TOKEN=${api_token}
    docker-compose -f docker-compose.ci.yml up -d api
    sleep 5
    python3.6 -m ametoctl operators package noop-operator.toml | docker run --interactive --network=${network_name} \
        --rm dev.digitalernachschub.de/ameto/ametoctl:0.8.0 --api-url http://api:5000 \
        --api-token ${api_token} \
        operators add -
    docker-compose -f docker-compose.ci.yml up -d
    sleep 5
}

run_tests() {
    local project_name=$(basename $(pwd))
    local network_name=${project_name}_default
    docker run --network=${network_name} --env AMETO_API_URL=http://api:5000 --env AMETO_API_TOKEN=${AMETO_API_TOKEN} \
      --mount type=volume,source=${project_name}_gradle_cache,target=/home/gradle/.gradle --rm ameto/java-client-tests
}

tear_down_test_env() {
    echo "API logs"
    docker-compose -f docker-compose.ci.yml logs api
    docker-compose -f docker-compose.ci.yml down
}

docker build -f Dockerfile.test -t ameto/java-client-tests .

trap tear_down_test_env EXIT
setup_test_env
run_tests
