FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY services/gateway/pom.xml .
RUN mvn -B dependency:go-offline

COPY services/gateway/src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/gateway.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
