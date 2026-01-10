# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy necessary files for dependency resolution first to leverage caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Fix Windows line endings (CRLF) -> Linux (LF) and grant execution rights
RUN sed -i 's/\r$//' gradlew
RUN chmod +x gradlew

# Copy source code
COPY src src

# Build the application (skipping tests to speed up build)
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built artifact from the builder stage
COPY --from=builder /app/build/libs/simple_cti.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Command to run the application (Disabled for debugging)
ENTRYPOINT ["java", "-jar", "app.jar"]

