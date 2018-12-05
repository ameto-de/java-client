#!/usr/bin/env bash
set -e

setup_test_env() {
    docker-compose -f docker-compose.ci.yml up -d kafka object-store
    sleep 30
    local project_name=$(basename $(pwd))
    local network_name=${project_name}_default
    docker-compose -f docker-compose.ci.yml up -d
    sleep 30
    local ametoctl="dev.digitalernachschub.de/ameto/ametoctl:0.15.0"
    local tenant_id=$(docker run --interactive --network=${network_name} \
        --rm ${ametoctl} --api-url http://delivery:80 --login admin --password V4l1dAdm1nT0ken \
        tenants add testtenant)
    sleep 5
    local user_id=$(docker run --interactive --network=${network_name} \
        --rm ${ametoctl} --api-url http://delivery:80 --login admin --password V4l1dAdm1nT0ken \
        users add ${tenant_id} user)
    sleep 5
    local api_token=$(docker run --interactive --network=${network_name} \
        --rm ${ametoctl} --api-url http://delivery:80 --login admin --password V4l1dAdm1nT0ken \
        users list --token ${tenant_id} ${user_id})
    export AMETO_API_TOKEN=${api_token}
    docker run --interactive --network=${network_name} \
        --rm ${ametoctl} --api-url http://delivery:80 --login admin --password V4l1dAdm1nT0ken \
        operators add https://operators.ameto.de/shrink-1.1.0.tar.xz
    docker run --interactive --network=${network_name} \
        --rm ${ametoctl} --api-url http://delivery:80 --login admin --password V4l1dAdm1nT0ken \
        operators add https://operators.ameto.de/resize-1.0.0.tar.xz
    docker run --interactive --network=${network_name} \
        --rm ${ametoctl} --api-url http://delivery:80 --login admin --password V4l1dAdm1nT0ken \
        operators add https://operators.ameto.de/normalize-1.0.0.tar.xz
    docker run --interactive --network=${network_name} \
        --rm ${ametoctl} --api-url http://delivery:80 --login admin --password V4l1dAdm1nT0ken \
        operators add https://operators.ameto.de/read_exif-1.0.0.tar.xz
    docker run --interactive --network=${network_name} \
        --rm ${ametoctl} --api-url http://delivery:80 --login admin --password V4l1dAdm1nT0ken \
        operators add https://operators.ameto.de/auto_orient-1.0.0.tar.xz
    sleep 10
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

if [[ ${AMETO_DEPLOY} ]]; then
    deploy_release
fi
