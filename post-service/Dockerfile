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

# Source code needed to build Post Service
COPY common-events/src common-events/src
COPY post-service/src post-service/src

# Build common-events + post-service
RUN mvn clean package -pl post-service -am -DskipTests


# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy Post Service JAR
COPY --from=build /app/post-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]