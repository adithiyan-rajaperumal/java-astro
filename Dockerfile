# Stage 1: Build frontend
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 2: Build backend
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src/ src/
# Create static directory
RUN mkdir -p src/main/resources/static/
COPY --from=frontend-build /app/frontend/dist/ src/main/resources/static/
RUN mvn package -DskipTests -Dfrontend.skip=true

# Stage 3: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-XX:+UseSerialGC", "-XX:ReservedCodeCacheSize=64m", "-XX:MaxMetaspaceSize=128m", "-jar", "app.jar"]
