
FROM openjdk:21

WORKDIR /app

COPY target/search-service-0.0.1.jar search-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "search-service.jar"]