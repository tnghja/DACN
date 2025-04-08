
FROM openjdk:21

WORKDIR /app

COPY target/recombee-service-0.0.1.jar recombee-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "recombee-service.jar"]