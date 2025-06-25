# etapa 1: build da JAR
        FROM maven:3.8.7-eclipse-temurin-17 AS build
        WORKDIR /app
        COPY pom.xml .
        COPY src ./src
        RUN mvn clean package -DskipTests

        # etapa 2: runtime
        FROM eclipse-temurin:17-jre
        WORKDIR /app
        COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
