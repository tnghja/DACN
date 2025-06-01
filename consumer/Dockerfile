# Use an official Python image as the base image
FROM python:3.9-slim

# Set the working directory
WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements.txt into the container
COPY requirements.txt .

# Install Python dependencies from requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copy the Kafka consumer Python script into the container
COPY consumer.py .

# Expose port for FastAPI (if running in the same container or use Docker Compose for communication)
#EXPOSE 8000

# Run the Kafka consumer
CMD ["python", "consumer.py"]