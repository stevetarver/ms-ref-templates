#!/usr/bin/env bash
#
# Package the app - build the docker image
#
MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
(
    # Run from project root
    cd ${MY_DIR}/../..

    gradle --console=plain -Dspring.profiles.active=dev clean compileJava compileGroovy assemble

    if [[ $? -ne 0 ]]; then
        echo "gradle build failed"
        exit 1
    fi

    docker build \
        -f docker/Dockerfile                            \
        --build-arg GIT_REPO_NAME=${GIT_REPO_NAME}      \
        --build-arg GIT_BRANCH_NAME=${GIT_BRANCH_NAME}  \
        --build-arg BUILD_TIMESTAMP=${BUILD_TIMESTAMP}  \
        --build-arg GIT_COMMIT_HASH=${GIT_COMMIT_HASH}  \
        -t ${DOCKER_BUILD_IMAGE_NAMETAG}                \
        -t ${DOCKER_BUILD_IMAGE_NAMETAG_LATEST}         \
        .
)

