FROM amazoncorretto:18-alpine

RUN apk upgrade --update-cache --available && apk add openssl && rm -rf /var/cache/apk/*

ARG JAR_FILE=target/sat-web-service.jar

COPY ${JAR_FILE} sat-web-service.jar

COPY create-p12.sh ./create-pfx.sh

ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=docker", "/sat-web-service.jar"]