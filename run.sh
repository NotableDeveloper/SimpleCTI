#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "[1/2] Building Docker image 'simple-cti'..."
docker build -t simple-cti .

echo ""
echo "[2/2] Running container on http://localhost:8080"

docker run -p 8080:8080 --add-host=host.docker.internal:host-gateway -e ASTERISK_HOST=host.docker.internal simple-cti
