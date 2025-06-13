FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# Copy the Maven configuration files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download all required dependencies
RUN mvn dependency:go-offline -B

# Copy the project source
COPY src src

# Package the application
RUN mvn package -DskipTests

# Create the final image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build /app/target/spring-boot-security-jwt-0.0.1-SNAPSHOT.jar app.jar

# Set environment variables
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/cashcase
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=admin

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
