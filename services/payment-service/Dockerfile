FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY services/payment-service/pom.xml .
RUN mvn -B dependency:go-offline

COPY services/payment-service/src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/payment-service.jar app.jar

EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
