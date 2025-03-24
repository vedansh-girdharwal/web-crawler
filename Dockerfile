# ================== STAGE 1: Build JAR ==================
FROM openjdk:17-jdk-slim AS builder

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy Maven files first for caching dependencies
COPY mvnw pom.xml ./

# Download dependencies to speed up builds
#RUN mvn dependency:go-offline

# Copy the actual source code
COPY src ./src

# Build the application (skipping tests for faster build)
RUN mvn clean install -DskipTests

# ================== STAGE 2: Run in Selenium Image ==================
FROM selenium/standalone-chrome:latest

# Set working directory
WORKDIR /app

# Copy the built JAR file from Stage 1
COPY --from=builder /app/target/web-crawler-0.0.1-SNAPSHOT.jar /app/webcrawler.jar

# Expose port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "/app/webcrawler.jar"]
