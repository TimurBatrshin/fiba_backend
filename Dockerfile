FROM openjdk:19-jdk-slim

WORKDIR /app

# Копируем JAR-файл
COPY target/api-1.0.0.jar app.jar

# Явное указание сетевых настроек
ENV PORT=8080
EXPOSE ${PORT}

# Запуск с явным указанием сетевых параметров
CMD java -Dserver.port=${PORT} -Dserver.address=0.0.0.0 -jar app.jar