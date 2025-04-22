FROM openjdk:19-jdk-slim

WORKDIR /app

COPY target/api-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 