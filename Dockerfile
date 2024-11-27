# Stage 1: Build application
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY gradle/ gradle
COPY src/ src
COPY gradlew build.gradle settings.gradle ./
RUN ./gradlew build --no-daemon

# Stage 2: Run application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar ./app.jar
CMD ["java", "-jar", "app.jar"]

