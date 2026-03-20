FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y dos2unix && \
    dos2unix mvnw && \
    chmod +x mvnw

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]