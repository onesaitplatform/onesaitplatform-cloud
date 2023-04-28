FROM openjdk:8-jdk-alpine

# Metadata
LABEL module.maintainer="payments@indra.es" \
	  module.name="datagrid-client"	

ADD *.jar app.jar

ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=docker","-jar","/app.jar"]
