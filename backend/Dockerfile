# Stage 1: Build
FROM eclipse-temurin:21-jdk AS build
LABEL authors="Filipe Andrade"
WORKDIR /app

# 1. Copy the wrapper files specifically
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# 2. Fix line endings (crucial if you edited on Windows) and permissions
RUN apt-get update && apt-get install -y dos2unix && \
    dos2unix mvnw && \
    chmod +x mvnw

# 3. Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]