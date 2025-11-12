# syntax=docker/dockerfile:1.6

### 1) BUILD STAGE
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Bağımlılık cache'i için önce pom ve wrapper dosyaları
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Maven repo'yu cache'le
RUN --mount=type=cache,target=/root/.m2 \
    (./mvnw -q -B -DskipTests dependency:go-offline || mvn -q -B -DskipTests dependency:go-offline)

# Asıl kaynak kod
COPY src ./src

# Paketle (testleri atla)
RUN --mount=type=cache,target=/root/.m2 \
    (./mvnw -q -B -DskipTests package || mvn -q -B -DskipTests package)

### 2) RUNTIME STAGE
FROM eclipse-temurin:21-jre
WORKDIR /app

# Konteyner bellek ayarı (OOM'a karşı güvenli)
ENV JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError -XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=25" \
    TZ=Europe/Berlin

# Build edilen jar'ı kopyala
COPY --from=build /app/target/*.jar app.jar

# Lokal test için 8080; Render'da PORT env verilecek
EXPOSE 8080

# Spring Boot'ta server.port=${PORT:8080} ayarlı olmalı (application.properties)
ENTRYPOINT ["java","-jar","/app/app.jar"]
