#!/bin/bash

# Запуск приложения с параметрами JVM для оптимизации в контейнере
java -Xms512m -Xmx1024m -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom \
     -jar target/api-1.0.0.jar 