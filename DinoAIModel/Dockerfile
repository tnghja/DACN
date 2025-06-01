# Use an official Python runtime as a base image
FROM pytorch/pytorch:latest
# Set environment variables to avoid interactive prompts during package installations
ENV PYTHONUNBUFFERED 1

# Install dependencies
RUN apt-get update && apt-get install -y \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    && rm -rf /var/lib/apt/lists/*

# Install pip requirements
COPY requirements.txt /app/requirements.txt
WORKDIR /app
RUN pip install --no-cache-dir -r requirements.txt

# Download model to cache it during build
RUN python -c "import timm; timm.create_model('vit_base_patch14_reg4_dinov2.lvd142m', pretrained=True, num_classes=0)"

# Copy the application code into the container
COPY . /app

# Expose the port the app runs on
EXPOSE 8000

# Command to run the application
CMD ["uvicorn", "AIModel:app", "--host", "0.0.0.0", "--port", "8000"]
