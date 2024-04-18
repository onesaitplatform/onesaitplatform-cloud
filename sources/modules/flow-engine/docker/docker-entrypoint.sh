#!/bin/sh

echo "Sustituyendo variables de entorno en ficheros de propiedades de onesaitplatform"
		
grep -rl '${SERVERNAME}' /opt/nodeRed/Flow-Engine-Manager | xargs sed -i 's/${SERVERNAME}/'"$SERVERNAME"'/g'
			
echo "Arrancando Tomcat..."	
java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar

exit 0