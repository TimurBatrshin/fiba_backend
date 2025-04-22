FROM openjdk:19-jdk-slim

WORKDIR /app

# Копируем JAR-файл
COPY target/api-1.0.0.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]