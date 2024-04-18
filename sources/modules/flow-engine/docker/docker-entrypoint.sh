#!/bin/sh

echo "Sustituyendo variables de entorno en ficheros de propiedades de onesaitplatform"
		
grep -rl '${SERVERNAME}' /opt/nodeRed/Flow-Engine-Manager | xargs sed -i 's/${SERVERNAME}/'"$SERVERNAME"'/g'
grep -rl '${SOCKETTIMEOUT}' /opt/nodeRed/Flow-Engine-Manager | xargs sed -i 's/${SOCKETTIMEOUT}/'"$SOCKETTIMEOUT"'/g'
grep -rl '${HTTPREQUESTTIMEOUT}' /opt/nodeRed/Flow-Engine-Manager | xargs sed -i 's/${HTTPREQUESTTIMEOUT}/'"$HTTPREQUESTTIMEOUT"'/g'
grep -rl '${PROTOCOL}' /opt/nodeRed/Flow-Engine-Manager | xargs sed -i 's/${PROTOCOL}/'"$PROTOCOL"'/g'

export COMMENT_START=''
export COMMENT_END=''

if [ ! -z ${DISABLE_HTTP_MIDDLEWARE} ] && [ ${DISABLE_HTTP_MIDDLEWARE} = true ]
then
    export COMMENT_START='\/\*'
    export COMMENT_END='\*\/'
fi

grep -rl '${COMMENT_START}' /opt/nodeRed/Flow-Engine-Manager | xargs sed -i 's/${COMMENT_START}/'"$COMMENT_START"'/g'
grep -rl '${COMMENT_END}' /opt/nodeRed/Flow-Engine-Manager | xargs sed -i 's/${COMMENT_END}/'"$COMMENT_END"'/g'
			
echo "Arrancando Tomcat..."	
java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar

exit 0
