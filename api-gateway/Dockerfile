# =========================
# Build stage
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Root Maven project
COPY pom.xml .

# Module POMs
COPY common-events/pom.xml common-events/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY post-service/pom.xml post-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml

# Gateway source code
COPY api-gateway/src api-gateway/src

# Build API Gateway
RUN mvn clean package -pl api-gateway -am -DskipTests


# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy API Gateway JAR
COPY --from=build /app/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]