#!/bin/bash
set -e
export DOCKER_BUILDKIT=1

RUN_TESTS=${RUN_TESTS:-true}

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --skip-tests) RUN_TESTS=false ;;
        *) echo "Unknown parameter passed: $1"; exit 1 ;;
    esac
    shift
done

if [ "$RUN_TESTS" = true ]; then
    SKIP_TESTS=false
    echo "Building all projects cleanly WITH tests..."
else
    SKIP_TESTS=true
    echo "Building all projects cleanly WITHOUT tests..."
fi

VERSION=$(awk -F'[<>]' '/<version>/{print $3; exit}' pom.xml)
echo "Detected Maven project version: $VERSION"

MODULES=("eureka" "gateway" "attachment" "storage" "worker")

for MODULE in "${MODULES[@]}"; do
    IMAGE_NAME="furrify-$MODULE:$VERSION"
    LATEST_NAME="furrify-$MODULE:latest"
    
    echo "========================================"
    echo "Building Docker image for: $MODULE"
    echo "Tags: $IMAGE_NAME, $LATEST_NAME"
    echo "========================================"
    
    docker build \
        -f "$MODULE/Dockerfile" \
        --build-arg SKIP_TESTS=$SKIP_TESTS \
        -t "$IMAGE_NAME" \
        -t "$LATEST_NAME" \
        .
done

echo "Build complete! All images for version $VERSION have been created locally."
