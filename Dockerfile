# Stage 1: Build frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci || echo "No frontend yet"
COPY frontend/ ./
RUN npm run build || echo "No frontend yet"

# Stage 2: Build backend
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src/ src/
# Create static directory in case frontend build failed/skipped
RUN mkdir -p src/main/resources/static/
COPY --from=frontend-build /app/frontend/dist/ src/main/resources/static/ || true
RUN mvn package -DskipTests -Dfrontend.skip=true

# Stage 3: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-jar", "app.jar"]
