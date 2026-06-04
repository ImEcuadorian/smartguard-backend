# =========================
# Build stage
# =========================
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x gradlew

# Descarga dependencias primero para aprovechar cache
RUN ./gradlew dependencies --no-daemon || true

COPY src src

RUN ./gradlew clean bootJar --no-daemon


# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN addgroup -S smartguard && adduser -S smartguard -G smartguard

COPY --from=build /app/build/libs/*.jar app.jar

USER smartguard

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]