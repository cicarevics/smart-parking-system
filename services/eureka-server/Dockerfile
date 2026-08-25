FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY services/eureka-server/pom.xml .
RUN mvn -B dependency:go-offline

COPY services/eureka-server/src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/eureka-server.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
