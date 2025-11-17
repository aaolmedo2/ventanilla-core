FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY target/ventanilla-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 80

ENTRYPOINT ["java", "-jar", "app.jar"] 