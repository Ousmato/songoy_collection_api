# Build with the Java version required by pom.xml.
FROM maven:3.9.6-eclipse-temurin-22 AS build

WORKDIR /workspace

# The Maven project lives in the nested songoycollection directory.
COPY songoycollection/pom.xml ./pom.xml
COPY songoycollection/src ./src

RUN mvn --batch-mode --no-transfer-progress -DskipTests clean package

# Keep the runtime image separate from the build tooling.
FROM eclipse-temurin:22.0.2_9-jre

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
