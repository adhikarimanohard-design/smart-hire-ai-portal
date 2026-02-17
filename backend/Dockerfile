# Stage 1: Build the application (The Builder Stage)
# Use the Maven image with JDK 17 to compile the Spring Boot application.
FROM maven:3.9.0-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy the pom.xml and source code relative to the 'backend' folder
COPY pom.xml .
COPY src ./src

# Build the project and create the executable JAR file
RUN mvn clean package -DskipTests

# ---
# Stage 2: Create the final, smaller runtime image (The Runner Stage)
# Use a slimmed-down JRE 17 (Java Runtime Environment) image for the final container.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from the 'build' stage into the final image
COPY --from=build /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# The command that runs your Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
