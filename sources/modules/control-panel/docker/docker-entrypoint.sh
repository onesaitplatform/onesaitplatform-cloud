#!/bin/bash

echo "Finding Plugins"

if [ -z "$PLUGIN_URI" ]
then
      echo "No plugins specified"
else

      echo "Downloading Plugins..."
	  IFS=';' read -ra plugins <<< "$PLUGIN_URI"
	  for plugin in "${plugins[@]}"
	  do
		  echo "Plugin found on '$plugin'"
	      wget -P /application/BOOT-INF/lib/ $plugin

	  done

fi

if [ ${INTERNAL_SSL} = true ]
then
  java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djavax.net.ssl.trustStore=/var/run/secrets/java.io/keystores/truststore.jks -Djavax.net.ssl.trustStorePassword=changeit -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -Dloader.path=file:/usr/local/themes/ -Djava.awt.headless=true org.springframework.boot.loader.JarLauncher
else
	java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -Dloader.path=file:/usr/local/themes/ -Djava.awt.headless=true org.springframework.boot.loader.JarLauncher
fi

exit 0
