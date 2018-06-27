FROM frolvlad/alpine-oraclejdk8:slim

# Build system will provide arguments to identify source location/version
ARG GIT_REPO_NAME='NOT SPECIFIED'
ARG GIT_BRANCH_NAME='NOT SPECIFIED'
ARG GIT_COMMIT_HASH='NOT SPECIFIED'
ARG BUILD_TIMESTAMP='NOT SPECIFIED'

# Label image with source location/version
LABEL ~~REV_PROD_DNS_NAME~~.build.repo=${GIT_REPO_NAME} \
      ~~REV_PROD_DNS_NAME~~.build.branch=${GIT_BRANCH_NAME} \
      ~~REV_PROD_DNS_NAME~~.build.timestamp=${BUILD_TIMESTAMP} \
      ~~REV_PROD_DNS_NAME~~.build.commit-hash=${GIT_COMMIT_HASH}

# Add features to the base image
# - bash: allow users to bash into a container for troubleshooting
RUN apk --no-cache add --update bash

ARG JAVA_OPTS=''

COPY target/~~GITHUB_REPO_NAME~~-*.jar /app.jar


# TODO: Jenkins secret: Use this ENTRYPOINT when secrets are configured
# ENTRYPOINT [ "sh", "-c", "java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -Dspring.cloud.kubernetes.secrets.paths=/secrets -jar /app.jar" ]
ENTRYPOINT [ "sh", "-c", "java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]

