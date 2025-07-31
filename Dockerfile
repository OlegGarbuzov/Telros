FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Копируем файлы pom.xml и src
COPY pom.xml .
COPY src ./src

# Собираем приложение и пропускаем тесты
RUN mvn clean package -DskipTests

# Используем образ JRE для запуска приложения
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем JAR-файл из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar

# Определяем переменные окружения для подключения к базе данных
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/telros
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=password

# Открываем порт 8080
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]