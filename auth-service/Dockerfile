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
COPY api-gateway/pom.xml api-gateway/pom.xml

# Copy source code
COPY common-events/src common-events/src
COPY auth-service/src auth-service/src

# Build common-events + auth-service
RUN mvn clean package -pl auth-service -am -DskipTests


# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/auth-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]