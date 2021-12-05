#!/bin/bash
set -e

# Build project
mvn package -DskipTests -Pjlink-image

# Build doker image
if [ -z "$1" ]
then # no argument supplied
    docker build -t tanger46/shopping-service -f Dockerfile.jlink .
else
    docker build -t arm64v8/tanger46/shopping-service -f Dockerfile.jlink.arm64v8 .
fi