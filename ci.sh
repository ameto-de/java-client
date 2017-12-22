#!/usr/bin/env bash
set -e

setup_test_env() {
    docker-compose -f docker-compose.ci.yml up -d kafka
    sleep 10
    docker-compose -f docker-compose.ci.yml up -d
    echo '{"name": "noop", "consumes": []}' > manifest.json
    docker save dev.digitalernachschub.de/ameto/noop-operator --output docker-image.tar
    local operator_bundle_path=operator.tar
    tar --create --file ${operator_bundle_path} docker-image.tar manifest.json
    local project_name=$(basename $(pwd))
    local network_name=${project_name//-/}_default
    cat ${operator_bundle_path} | docker run --interactive --network=${network_name} --rm dev.digitalernachschub.de/ameto/ametoctl python -m ametoctl --broker kafka:9092 add
}

run_tests() {
    local project_name=$(basename $(pwd))
    local network_name=${project_name//-/}_default
    docker run --network=${network_name} --env AMETO_API_URL=http://api:5000 --rm ameto/java-client-tests
}

tear_down_test_env() {
    docker-compose -f docker-compose.ci.yml down
}

docker build -f Dockerfile.test -t ameto/java-client-tests .

trap tear_down_test_env EXIT
setup_test_env
run_tests
