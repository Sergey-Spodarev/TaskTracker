FROM openjdk:21-jdk-slim-bullseye
LABEL authors="sergejspodarev"
WORKDIR /app
COPY target/calendar-0.0.1-SNAPSHOT.jar /app/taskTraker.jar

ENTRYPOINT ["java", "-jar", "taskTraker.jar"]