# Etapa 1: compila el proyecto con Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: imagen final, liviana, solo con el jar ya compilado
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/metas-backend-0.0.1.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]