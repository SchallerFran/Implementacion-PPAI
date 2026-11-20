# Backend + frontend en un solo servicio: Spring Boot sirve el build de React desde /static
# y la API en /api, en el mismo dominio (sin CORS).

# 1. Frontend: VITE_API_URL vacía => las llamadas quedan relativas (/api/...)
FROM node:22-alpine AS frontend
WORKDIR /frontend
COPY Frontend/package.json Frontend/package-lock.json ./
RUN npm ci
COPY Frontend/ ./
RUN printf 'VITE_API_URL=\n' > .env.production.local && npm run build

# 2. Backend con el frontend dentro de los recursos estáticos
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /backend
COPY Backend/pom.xml ./
RUN mvn -q dependency:go-offline
COPY Backend/src ./src
COPY --from=frontend /frontend/dist ./src/main/resources/static
RUN mvn -q package -DskipTests

# 3. Imagen final
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /backend/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
