
FROM openjdk:21

# Đặt thư mục làm việc
WORKDIR /app

# Sao chép file JAR vào container
COPY target/product-service-0.0.1-SNAPSHOT.jar /app/product-service.jar

# Mở cổng 8080
EXPOSE 8088

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "/app/product-service.jar"]