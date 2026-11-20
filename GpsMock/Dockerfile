# API mock del GPS Tracker (repositorio de la cátedra, con los bolsines 122-126 agregados por el grupo)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /mock
COPY pom.xml ./
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /mock/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
