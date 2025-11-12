# syntax=docker/dockerfile:1.6

### 1) BUILD STAGE
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Bağımlılık cache için önce yalnızca pom.xml
COPY pom.xml .

# m2 cache ile offline dependency indirme
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests dependency:go-offline

# Kaynak kodları kopyala ve paketle
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests package

### 2) RUNTIME STAGE
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError -XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=25" \
    TZ=Europe/Berlin

# build edilen jar'ı kopyala
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
# application.properties içinde: server.port=${PORT:8080}
ENTRYPOINT ["java","-jar","/app/app.jar"]
