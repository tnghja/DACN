#!/bin/bash

# Exit on error
set -e

# Configuration
IMAGE_NAME="product-service"
DOCKER_HUB_USERNAME="tnghja"
TAGGED_IMAGE="${DOCKER_HUB_USERNAME}/${IMAGE_NAME}"

echo "Building Docker image for ${IMAGE_NAME}..."
docker build -t ${IMAGE_NAME} .

echo "Tagging image as ${TAGGED_IMAGE}..."
docker tag ${IMAGE_NAME} ${TAGGED_IMAGE}

echo "Pushing image to Docker Hub..."
docker push ${TAGGED_IMAGE}

echo "Process completed successfully!"
echo "Image is available at: ${TAGGED_IMAGE}" 