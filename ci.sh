#!/usr/bin/env bash
set -e

setup_test_env() {
    docker-compose -f docker-compose.ci.yml up -d kafka object-store
    sleep 30
    local project_name=$(basename $(pwd))
    local network_name=${project_name}_default
    docker-compose -f docker-compose.ci.yml up -d
    local ametoctl="dev.digitalernachschub.de/ameto/ametoctl:0.10.0"
    sleep 30
    local user_id=$(docker run --interactive --network=${network_name} \
        --rm ${ametoctl} --api-url http://delivery:80 --api-token V4l1dAdm1nT0ken \
        users add testuser)
    sleep 10
    local api_token=$(docker run --interactive --network=${network_name} --rm ${ametoctl} --api-url http://delivery:80 \
        --api-token V4l1dAdm1nT0ken users add_token ${user_id} user)
    export AMETO_API_TOKEN=${api_token}
    ametoctl operators package noop-operator.toml | docker run --interactive --network=${network_name} \
        --rm ${ametoctl} --api-url http://delivery:80 --api-token V4l1dAdm1nT0ken \
        operators add -
}

run_tests() {
    local project_name=$(basename $(pwd))
    local network_name=${project_name}_default
    docker run --network=${network_name} --env AMETO_API_URL=http://delivery:80 --env AMETO_API_TOKEN=${AMETO_API_TOKEN} \
      --mount type=volume,source=${project_name}_gradle_cache,target=/home/gradle/.gradle --rm ameto/java-client-tests
}

deploy_release() {
    docker run --mount type=bind,source=${HOME}/.gradle/gradle.properties,target=/home/gradle/.gradle/gradle.properties,ro \
        --mount type=bind,source=${HOME}/.gnupg/ameto-releng-secring.gpg,target=/home/gradle/.gnupg/ameto-releng-secring.gpg \
        --rm ameto/java-client-tests gradle --no-daemon uploadArchives closeAndReleaseRepository
}

tear_down_test_env() {
    echo "####################"
    echo "# API service logs #"
    echo "####################"
    docker-compose -f docker-compose.ci.yml logs api
    echo "####################"
    echo "# API gateway logs #"
    echo "####################"
    docker-compose -f docker-compose.ci.yml logs delivery
    docker-compose -f docker-compose.ci.yml down
}

docker build -f Dockerfile.test -t ameto/java-client-tests .

trap tear_down_test_env EXIT
setup_test_env
run_tests

version=$(git describe --tags --always --dirty --match client-*)
if [[ $(git tag --list ${version}) ]]; then
    deploy_release
fi
