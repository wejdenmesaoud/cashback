FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Set Maven options to handle SSL and network issues
ENV MAVEN_OPTS="-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true"

# Copy Maven configuration files
COPY pom.xml .
COPY settings.xml .
COPY mvnw .
COPY .mvn .mvn

# Make mvnw executable (in case it's not)
RUN chmod +x mvnw

# Download dependencies with custom settings and SSL bypass
RUN mvn dependency:go-offline -B -s settings.xml || \
    mvn dependency:go-offline -B -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true || \
    ./mvnw dependency:go-offline -B

# Copy the project source
COPY src src

# Package the application with custom settings
RUN mvn clean package -DskipTests -B -s settings.xml || \
    mvn clean package -DskipTests -B -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true || \
    ./mvnw clean package -DskipTests -B

# Create the final image
FROM eclipse-temurin:17-jdk-jammy
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
