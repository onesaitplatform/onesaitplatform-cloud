FROM openjdk:8-jdk-alpine

LABEL maintainer "plataformasofia2@indra.es"

VOLUME /tmp

ARG JAR_FILE

ADD *-exec.jar app.jar

EXPOSE 8080

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar