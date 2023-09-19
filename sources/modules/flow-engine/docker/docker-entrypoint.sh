#!/bin/sh

echo "Sustituyendo variables de entorno en ficheros de propiedades de onesaitplatform"

grep -rl '${SERVERNAME}' /opt/nodeRed/Flow-Engine-Manager/node_modules/@node-red/editor-client/public/config/onesait-platform-config.js | xargs sed -i 's/${SERVERNAME}/'"$SERVERNAME"'/g'
grep -rl '${FLOWENGINESERVICE}' /opt/nodeRed/Flow-Engine-Manager/node_modules/@node-red/editor-client/public/config/onesait-platform-config.js | xargs sed -i 's/${FLOWENGINESERVICE}/'"$FLOWENGINESERVICE"'/g'
grep -rl '${SOCKETTIMEOUT}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${SOCKETTIMEOUT}/'"$SOCKETTIMEOUT"'/g'
grep -rl '${HTTPREQUESTTIMEOUT}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${HTTPREQUESTTIMEOUT}/'"$HTTPREQUESTTIMEOUT"'/g'
# NOT in use
#grep -rl '${PROTOCOL}' /opt/nodeRed/Flow-Engine-Manager | xargs sed -i 's/${PROTOCOL}/'"$PROTOCOL"'/g'

grep -rl '${GRAYLOG_HOST}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${GRAYLOG_HOST}/'"$GRAYLOG_HOST"'/g'
grep -rl '${GRAYLOG_HOST}' /opt/nodeRed/Flow-Engine-Manager/app.js | xargs sed -i 's/${GRAYLOG_HOST}/'"$GRAYLOG_HOST"'/g'
grep -rl '${GRAYLOG_HOST}' /opt/nodeRed/Flow-Engine-Manager/proxy-nodered.js | xargs sed -i 's/${GRAYLOG_HOST}/'"$GRAYLOG_HOST"'/g'

grep -rl '${GRAYLOG_PORT}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${GRAYLOG_PORT}/'"$GRAYLOG_PORT"'/g'
grep -rl '${GRAYLOG_PORT}' /opt/nodeRed/Flow-Engine-Manager/app.js | xargs sed -i 's/${GRAYLOG_PORT}/'"$GRAYLOG_PORT"'/g'
grep -rl '${GRAYLOG_PORT}' /opt/nodeRed/Flow-Engine-Manager/proxy-nodered.js | xargs sed -i 's/${GRAYLOG_PORT}/'"$GRAYLOG_PORT"'/g'

grep -rl '${HTTP_PROXY}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${HTTP_PROXY}/'"$HTTP_PROXY"'/g'
grep -rl '${HTTPS_PROXY}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${HTTPS_PROXY}/'"$HTTPS_PROXY"'/g'
grep -rl '${NO_PROXY}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${NO_PROXY}/'"$NO_PROXY"'/g'

grep -rl '${FLOW_SECRET}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${FLOW_SECRET}/'"$FLOW_SECRET"'/g'

export COMMENT_START=''
export COMMENT_END=''

#Enabling Middleware Auth
if [ ! -z ${DISABLE_HTTP_MIDDLEWARE} ] && [ ${DISABLE_HTTP_MIDDLEWARE} = true ]
then
    export COMMENT_START='\/\*'
    export COMMENT_END='\*\/'
fi

grep -rl '${COMMENT_START}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${COMMENT_START}/'"$COMMENT_START"'/g'
grep -rl '${COMMENT_END}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${COMMENT_END}/'"$COMMENT_END"'/g'

#Enabling GELF logging for Graylog
export COMMENT_GRAYLOG_START='\/\*'
export COMMENT_GRAYLOG_END='\*\/'

if [ ! -z ${GRAYLOG_ENABLED} ] && [ ${GRAYLOG_ENABLED} = true ]
then
    export COMMENT_GRAYLOG_START=''
    export COMMENT_GRAYLOG_END=''
fi

grep -rl '${COMMENT_GRAYLOG_START}' /opt/nodeRed/Flow-Engine-Manager/app.js | xargs sed -i 's/${COMMENT_GRAYLOG_START}/'"$COMMENT_GRAYLOG_START"'/g'
grep -rl '${COMMENT_GRAYLOG_END}' /opt/nodeRed/Flow-Engine-Manager/app.js | xargs sed -i 's/${COMMENT_GRAYLOG_END}/'"$COMMENT_GRAYLOG_END"'/g'
grep -rl '${COMMENT_GRAYLOG_START}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${COMMENT_GRAYLOG_START}/'"$COMMENT_GRAYLOG_START"'/g'
grep -rl '${COMMENT_GRAYLOG_END}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${COMMENT_GRAYLOG_END}/'"$COMMENT_GRAYLOG_END"'/g'
grep -rl '${COMMENT_GRAYLOG_START}' /opt/nodeRed/Flow-Engine-Manager/proxy-nodered.js | xargs sed -i 's/${COMMENT_GRAYLOG_START}/'"$COMMENT_GRAYLOG_START"'/g'
grep -rl '${COMMENT_GRAYLOG_END}' /opt/nodeRed/Flow-Engine-Manager/proxy-nodered.js | xargs sed -i 's/${COMMENT_GRAYLOG_END}/'"$COMMENT_GRAYLOG_END"'/g'


#Enabling HTTP PROXY
export COMMENT_PROXY_START='\/\*'
export COMMENT_PROXY_END='\*\/'
if [ ! -z ${HTTP_PROXY} ] || [ ! -z ${HTTPS_PROXY} ]
then

    export COMMENT_PROXY_START=''
    export COMMENT_PROXY_END=''
fi
grep -rl '${COMMENT_PROXY_START}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${COMMENT_PROXY_START}/'"$COMMENT_PROXY_START"'/g'
grep -rl '${COMMENT_PROXY_END}' /opt/nodeRed/Flow-Engine-Manager/child.js | xargs sed -i 's/${COMMENT_PROXY_END}/'"$COMMENT_PROXY_END"'/g'


echo "Arrancando Tomcat..."
java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar

exit 0
