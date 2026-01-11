#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "[1/2] Building Docker image 'simple-cti'..."
docker build -t simple-cti .

echo ""
echo "[2/2] Running container in host network mode"

docker run --network host simple-cti &
