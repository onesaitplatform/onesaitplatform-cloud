FROM maven:3.6.0-jdk-8-alpine

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="data-cleaner"
	  
ENV SERVER_NAME=localhost  

ADD OpenRefine34 /usr/local/OpenRefine34

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

RUN mkdir -p /var/log/platform-logs && \
	mkdir /mnt/refine && \
    chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait /mnt/refine && \
    apk add --no-cache bash && \  
    chmod 755 /usr/local/OpenRefine34/refine && \
    /usr/local/OpenRefine34/refine build
       
USER onesait    

EXPOSE 3333

VOLUME ["/tmp","/var/log/platform-logs","/mnt/refine"]

CMD /usr/local/OpenRefine34/refine -i 0.0.0.0 -d /mnt/refine
