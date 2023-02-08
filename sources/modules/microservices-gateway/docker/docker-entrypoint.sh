#!/bin/sh


echo "Executing keytool..."
keytool -noprompt -import -v -trustcacerts -alias onesait_cer -file ${CER_DIR} -keystore /usr/lib/jvm/java-1.8-openjdk/jre/lib/security/cacerts -keypass changeit -storepass changeit

echo "Arrancando el Gateway..."	
java $JAVA_OPTS -Dspring.profiles.active=docker -Dspring.application.json=$ONESAIT_PROPERTIES -jar /app.jar

exit 0