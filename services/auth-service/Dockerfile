FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Cache dependencies in their own layer before copying source
COPY services/auth-service/pom.xml .
RUN mvn -B dependency:go-offline

COPY services/auth-service/src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/auth-service.jar app.jar

EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
