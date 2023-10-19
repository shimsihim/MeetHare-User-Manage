FROM openjdk:11

WORKDIR /workspace

COPY build/libs/*.jar service.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "service.jar"]