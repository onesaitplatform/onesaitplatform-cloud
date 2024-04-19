FROM openjdk:8-jre-alpine

# Metadata
LABEL platform.image.maintainer="Onesait Platform" \
 platform.image.vendor="Minsait" \
 platform.image.support="support@onesaitplatform.com" \
 platform.image.license="Apache Software License 2"

# create onesait platform user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait && \
	mkdir -p /opt/avatica && \
	mkdir -p /my-database-jars && \
	mkdir -p /var/log/platform-logs && \
	chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait /my-database-jars && \
    chmod -R 777 /var/log && \
    chmod -R 777 /home && \
    chmod -R 777 /my-database-jars

# Dependencies
ADD https://repo1.maven.org/maven2/org/apache/calcite/avatica/avatica-standalone-server/1.16.0/avatica-standalone-server-1.16.0.jar /opt/avatica
COPY log4j.properties /opt/avatica

# Add owner to this file
RUN chown -R onesait:onesait /opt/avatica
	
ENV JVM_INITIAL_JAVA_HEAP=100m \
    JVM_MAX_JAVA_HEAP=2g \
	JDBC_PORT=8765
	
ENV JAVA_OPTS="$JAVA_OPTS -Xms${JVM_INITIAL_JAVA_HEAP} -Xmx${JVM_MAX_JAVA_HEAP}"

USER onesait
						  
EXPOSE 8765
						  
ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Dlog4j.configuration=file:/opt/avatica/log4j.properties -cp "/opt/avatica/*:/my-database-jars/*" org.apache.calcite.avatica.standalone.StandaloneServer -p $JDBC_PORT -u "$JDBC_URL"