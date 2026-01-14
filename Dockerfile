FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "libs/simple_cti.jar"]
