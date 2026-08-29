# =========================
# Build stage
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy root Maven configuration
COPY pom.xml .

# Copy module Maven configurations
COPY common-events/pom.xml common-events/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY post-service/pom.xml post-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml

# Copy source code
COPY common-events/src common-events/src
COPY user-service/src user-service/src

# Build common-events + user-service
RUN mvn clean package -pl user-service -am -DskipTests


# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the User Service JAR
COPY --from=build /app/user-service/target/*.jar app.jar

# User Service port
EXPOSE 8082

# Start User Service
ENTRYPOINT ["java", "-jar", "app.jar"]