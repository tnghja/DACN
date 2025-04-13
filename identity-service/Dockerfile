# Build stage

# Run stage
FROM openjdk:21
WORKDIR /app
COPY  target/identity-0.0.1-SNAPSHOT.jar identity-service.jar

# Expose app port
EXPOSE 8083

# Run the application
ENTRYPOINT ["java", "-jar", "identity-service.jar"]

# To build, tag and push this image, run the following commands:
# docker build -t product-service .
# docker tag product-service tnghja/product-service
# docker push tnghja/product-service