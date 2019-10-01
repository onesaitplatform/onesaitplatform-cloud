FROM openjdk:8-jre-alpine

# metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="scripting-engine"	

VOLUME /tmp

ARG JAR_FILE

ADD *-exec.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar