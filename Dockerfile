FROM openjdk:19-jdk-slim

WORKDIR /app

# Копируем JAR-файл
COPY target/api-1.0.0.jar app.jar

# Explicitly expose port from environment or default to 8080
ENV PORT=8080
EXPOSE ${PORT}

# Use the PORT environment variable when running the jar
CMD java -Dserver.port=${PORT} -jar app.jar