FROM openjdk:19-jdk-slim

WORKDIR /app

# Создаем директории для загрузки файлов
RUN mkdir -p /app/uploads/profiles \
    /app/uploads/avatars \
    /app/uploads/tournaments \
    /app/uploads/sponsors \
    /app/uploads/ads \
    /app/uploads/teams \
    /app/uploads/temp

# Устанавливаем права доступа
RUN chmod -R 777 /app/uploads

# Копируем JAR-файл
COPY target/api-1.0.0.jar app.jar

# Настройка переменных окружения
ENV PORT=8080 \
    JAVA_OPTS="-Xms512m -Xmx1024m" \
    SPRING_PROFILES_ACTIVE=prod

# Открываем порт
EXPOSE ${PORT}

# Точка входа с оптимизированными настройками JVM
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS \
    -XX:+UseContainerSupport \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom \
    -Dserver.port=${PORT} \
    -Dserver.address=0.0.0.0 \
    -jar app.jar" ]