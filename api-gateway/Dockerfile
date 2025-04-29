# Build stage

# Run stage
FROM openjdk:21
WORKDIR /app
COPY  target/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar

# Expose app port
EXPOSE 8888

# Run the application
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]

# To build, tag and push this image, run the following commands:
# docker build -t product-service .
# docker tag product-service tnghja/product-service
# docker push tnghja/product-service