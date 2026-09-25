# --- build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia solo il pom prima: le dipendenze restano in cache finché non cambia.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# --- run ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/salone-auto-*.jar app.jar

# Render passa la porta reale in $PORT; application.yml la legge già.
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75.0 -jar app.jar"]
