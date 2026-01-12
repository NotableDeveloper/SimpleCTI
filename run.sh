#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

CONTAINER_NAME="simple_cti_application"
# Changed image name to ensure a fresh build for the new structure
IMAGE_NAME="simple-cti-local"

# Function to clean up temporary docker config
cleanup() {
    rm -rf .docker_tmp
}
# Register cleanup to run on exit (success or failure)
trap cleanup EXIT

echo "[1/3] Building executable JAR with Gradle..."
./gradlew bootJar

echo ""
if [ -z "$(docker images -q $IMAGE_NAME 2> /dev/null)" ]; then
    echo "[2/3] Building Docker image '$IMAGE_NAME' ભા"
    
    # Create a temporary docker config to bypass macOS keychain issues for public images
    mkdir -p .docker_tmp
    echo "{}" > .docker_tmp/config.json
    
    # Build using the temporary config
    DOCKER_CONFIG=$(pwd)/.docker_tmp docker build -t $IMAGE_NAME .
else
    echo "[2/3] Docker image '$IMAGE_NAME' already exists. Skipping build."
fi

echo ""
echo "[3/3] Handling container '$CONTAINER_NAME' ભા"

# Check if the container exists (running or stopped)
if [ "$(docker ps -aq -f name=^/${CONTAINER_NAME}$)" ]; then
    echo "    Container '$CONTAINER_NAME' exists. Stopping and removing..."
    docker stop $CONTAINER_NAME > /dev/null 2>&1 || true
    docker rm $CONTAINER_NAME > /dev/null 2>&1 || true
    echo "    Container removed."
fi

echo "    Starting new container with bind mount..."
# Added -v to mount the build/libs directory to /app/libs inside the container
docker run --network host --name $CONTAINER_NAME \
  -v "$(pwd)/build/libs:/app/libs" \
  $IMAGE_NAME &