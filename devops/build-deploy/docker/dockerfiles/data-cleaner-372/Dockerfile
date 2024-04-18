FROM ubuntu:20.04

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="data-cleaner"
	  
ENV SERVER_NAME=localhost  

ADD OpenRefine372 /usr/local/OpenRefine372

RUN apt-get update
RUN apt-get upgrade -y
RUN apt install openjdk-17-jdk openjdk-17-jre -y

# create onesait user/group
# RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait
RUN groupadd -r onesait -g 433 && useradd -u 431 -r -g onesait -d /usr/local -s /sbin/nologin -c "Data user" onesait
  
RUN apt install wget -y
RUN mkdir -p /var/log/platform-logs
RUN mkdir /mnt/refine
RUN chown -R onesait:onesait /usr/local
RUN chown -R onesait:onesait /var/log/platform-logs
RUN chown -R onesait:onesait /mnt/refine
  
RUN chmod 777 /usr/local/OpenRefine372/refine 
  
       
USER onesait    

EXPOSE 3333

VOLUME ["/mnt/refine"]

CMD /usr/local/OpenRefine372/refine -i 0.0.0.0 -d /mnt/refine
