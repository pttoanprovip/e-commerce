FROM openjdk:23-jdk-slim AS build
WORKDIR /app
COPY . /app
RUN ./mvnw package -DskipTests
FROM openjdk:23-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]