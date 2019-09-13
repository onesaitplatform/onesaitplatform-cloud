FROM openjdk:8-jdk-alpine

# Metadata
LABEL module.name="${dtName}"	


COPY *.jar app.jar

# logs folder
RUN mkdir -p /var/log && \
	mkdir ./target


# create user
RUN addgroup -S user -g 433 && adduser -u 431 -S -g user -h /usr/local -s /sbin/nologin user

RUN  chown -R user:user /var/log && \
	 chmod -R 777 /var/log && \
	 chown user:user app.jar && \
	 chown -R user:user ./target && \    
     chmod -R 777 ./target
	 
	 
VOLUME ["/var/log/platform-logs"]


USER user

EXPOSE 30010


ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G" 

ENTRYPOINT java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar

	