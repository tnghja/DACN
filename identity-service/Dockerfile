# Build stage

# Run stage
FROM openjdk:21
WORKDIR /app
COPY  target/identity-service-0.0.1.jar identity-service.jar


# Run the application
ENTRYPOINT ["java", "-jar", "identity-service.jar"]

# To build, tag and push this image, run the following commands:
# docker build -t product-service .
# docker tag product-service tnghja/product-service
# docker push tnghja/product-service