#!/bin/sh

echo "Executing keytool..."
keytool -noprompt -import -v -trustcacerts -alias openshiftserver -file /usr/local/ocpserver.crt -keystore /usr/lib/jvm/java-1.8-openjdk/jre/lib/security/cacerts -keypass changeit -storepass changeit

echo "Arrancando Tomcat..."	
java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar
sleep 2m
exit 0