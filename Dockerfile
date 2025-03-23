# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Install necessary dependencies and Chrome
RUN apt-get update && \
    apt-get install -y wget gnupg2 && \
    wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get install -y ./google-chrome-stable_current_amd64.deb && \
    apt-get install -y libxss1 libappindicator1 libindicator7 && \
    apt-get install -y fonts-liberation libasound2 libatk-bridge2.0-0 libgtk-3-0 libnspr4 libnss3 libx11-xcb1 libxcb1 libxcomposite1 libxcursor1 libxdamage1 libxext6 libxfixes3 libxi6 libxrandr2 libxrender1 libxtst6 && \
    rm -f google-chrome-stable_current_amd64.deb && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV ENV=production

# Copy the Maven wrapper and pom.xml to the container
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Install Maven dependencies
RUN ./mvnw dependency:go-offline

# Copy the project source code to the container
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port the application runs on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "target/webcrawler-0.0.1-SNAPSHOT.jar"]