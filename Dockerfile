FROM amazoncorretto:17-alpine

ARG JAR_FILE=target/sat-web-service.jar

COPY ${JAR_FILE} sat-web-service.jar

ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=docker", "/sat-web-service.jar"]