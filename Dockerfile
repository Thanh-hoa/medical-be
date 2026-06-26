# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
    && mkdir -p /app/uploads/photos \
    && chown -R appuser:appgroup /app/uploads

COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
