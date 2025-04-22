#!/bin/bash

# Пропускаем тесты для ускорения сборки
./mvnw clean package -DskipTests

# Если Maven wrapper отсутствует, используем команду ниже
# mvn clean package -DskipTests

echo "Сборка завершена. JAR-файл создан в директории target/" 