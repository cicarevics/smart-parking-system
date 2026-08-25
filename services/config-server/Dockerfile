FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY services/config-server/pom.xml .
RUN mvn -B dependency:go-offline

COPY services/config-server/src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/config-server.jar app.jar
COPY infra/config-repo /config-repo

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
