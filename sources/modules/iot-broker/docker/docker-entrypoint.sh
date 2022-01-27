#!/bin/sh

if [ ${INTERNAL_SSL} = true ]
then
  java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djavax.net.ssl.trustStore=/var/run/secrets/java.io/keystores/truststore.jks -Djavax.net.ssl.trustStorePassword=changeit -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar
else
	java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar
fi

exit 0
