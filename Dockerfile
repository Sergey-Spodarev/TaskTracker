# Стадия 1: сборка
FROM openjdk:21-jdk-slim-bullseye AS builder

WORKDIR /app

# Копируем POM и Maven Wrapper ДО запуска
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Загружаем зависимости (кэшируется, если pom.xml не менялся)
RUN ./mvnw dependency:go-offline -B

# Копируем исходники и собираем JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests -B


# Стадия 2: запуск
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Копируем JAR из сборки
COPY --from=builder /app/target/taskTracker-0.0.1-SNAPSHOT.jar taskTraker.jar

# Запускаем
ENTRYPOINT ["java", "-jar", "taskTraker.jar"]