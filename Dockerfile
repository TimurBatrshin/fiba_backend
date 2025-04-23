FROM openjdk:19-jdk-slim

WORKDIR /app

# Копируем JAR-файл
COPY target/api-1.0.0.jar app.jar

# Лучшая настройка переменных окружения для Railway
ENV PORT=${PORT:-8080}
EXPOSE ${PORT}

# Установка переменных ОЗУ
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Оптимальный запуск с явными параметрами для контейнеризации
CMD java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT} -Dserver.address=0.0.0.0 -jar app.jar