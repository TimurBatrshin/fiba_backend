#!/bin/bash

# Устанавливаем переменные окружения
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fiba
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres

# Собираем проект, пропуская тесты
if [ -f mvnw ]; then
  ./mvnw clean package -DskipTests
else
  mvn clean package -DskipTests
fi

# Запускаем приложение
java -jar target/api-1.0.0.jar 