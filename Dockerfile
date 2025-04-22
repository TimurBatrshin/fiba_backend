FROM openjdk:19-jdk-slim

WORKDIR /app

# Копируем JAR-файл
COPY target/api-1.0.0.jar app.jar

# Настройки для оптимизации JVM в контейнере
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Создаем директорию для загруженных файлов
RUN mkdir -p /app/uploads && chmod 777 /app/uploads

# Настройки для отладки
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS

EXPOSE 8080

CMD ["java", "-jar", "app.jar"] 