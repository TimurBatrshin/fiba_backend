# FIBA Basketball Tournament Management API

Бэкенд-API для управления баскетбольными турнирами FIBA.

## Требования

- Java 19
- PostgreSQL 12+

## Локальная разработка

1. Клонируйте репозиторий:
   ```
   git clone <repository-url>
   cd fiba_backend
   ```

2. Создайте базу данных PostgreSQL:
   ```
   createdb fiba
   ```

3. Настройте переменные окружения (скопируйте из .env.example):
   ```
   cp .env.example .env
   ```
   
   Отредактируйте файл .env, установив корректные значения для вашей среды.

4. Запустите приложение:
   ```
   ./mvnw spring-boot:run
   ```
   
   Или с использованием скрипта:
   ```
   ./local-run.sh
   ```

5. Приложение будет доступно по адресу: http://localhost:8080

## Сборка и деплой

### Сборка

```
./mvnw clean package -DskipTests
```

Или с использованием скрипта:
```
./build.sh
```

### Docker

```
docker build -t fiba-api .
docker run -p 8080:8080 fiba-api
```

### Heroku

1. Создайте приложение Heroku:
   ```
   heroku create
   ```

2. Настройте переменные окружения:
   ```
   heroku config:set SPRING_DATASOURCE_URL=your-db-url
   heroku config:set SPRING_DATASOURCE_USERNAME=your-db-username
   heroku config:set SPRING_DATASOURCE_PASSWORD=your-db-password
   heroku config:set JWT_SECRET=your-jwt-secret
   ```

3. Деплой через Git:
   ```
   git push heroku main
   ```

## Переменные окружения

Все необходимые переменные окружения перечислены в файле `.env.example`.

## Документация API

После запуска приложения документация API будет доступна по адресу:
- Swagger UI: http://localhost:8080/swagger-ui
- OpenAPI спецификация: http://localhost:8080/api-docs 