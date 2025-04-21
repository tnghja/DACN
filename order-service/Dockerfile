# Build stage

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY /target/order-0.0.1-SNAPSHOT.jar /app/order-service.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/order-service.jar"]

# To build, tag and push this image, run the following commands:
# docker build -t product-service .
# docker tag product-service tnghja/product-service
# docker push tnghja/product-service