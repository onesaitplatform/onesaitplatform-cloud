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
	      wget -P /application/plugins/ $plugin
	    	
	  done
      
fi		

java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Dspring.profiles.active=docker -jar /app.jar

exit 0
