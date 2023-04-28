FROM onesaitplatform/baseimage:latest

# metadata
LABEL platform.image.name="spark-launcher-manager"

ADD *-exec.jar app.jar

# Timezone
RUN apk add --no-cache tzdata bash

# logs folder
RUN mkdir -p /var/log/platform-logs && \
	mkdir ./target

# create sofia user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local

ENV ASPECTJ_VERSION=1.9.7 \
	SPARK_VERSION=3.2.1 \
	HADOOP_VERSION=3.2 \
	SPARK_HOME=/opt/spark \
	PYTHONHASHSEED=1 \
	JAVA_HOME=/usr/local/openjdk-11

RUN cd /usr/local \
    && wget https://repo1.maven.org/maven2/org/aspectj/aspectjweaver/${ASPECTJ_VERSION}/aspectjweaver-$ASPECTJ_VERSION.jar \
    && wget --no-verbose -O apache-spark.tgz "https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz" \
	&& mkdir -p /opt/spark \
	&& tar -xf apache-spark.tgz -C /opt/spark --strip-components=1 \
	&& rm apache-spark.tgz \
	&& rm -f /opt/spark/jars/* \
	&& ln -s /usr/lib/jvm/java-1.8-openjdk /usr/local/openjdk-11

ADD ./jars/ /opt/spark/jars/

VOLUME ["/tmp", "/var/log/platform-logs"]

USER onesait

EXPOSE 21320

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G -javaagent:/usr/local/aspectjweaver-$ASPECTJ_VERSION.jar" \
    SERVER_NAME=localhost \
	CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
	CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    CONFIGDB_MAX_ACTIVE=2 \
    CONFIGDB_MAX_IDLE=2 \
	CONFIGDB_DRIVER=com.mysql.cj.jdbc.Driver \
	CONFIGDB_DIALECT=org.hibernate.dialect.MySQL5InnoDBDialect \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181 \
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \
	REALTIMEDBUSEAUTH=true \
  	REALTIMEDBUSER=platformadmin \
	REALTIMEDBPASS=ENC(GGpZ1sLYnXwK+vz2QLkI/VK7geKcdM4pZaTL6hv6QTk=) \
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \
    REALTIMEDBNAME=onesaitplatform_rtdb \    
    QUASARHOST=quasar \
    QUASARPORT=10800 \
    MAXCONN=100 \
    MAXCONNROUTE=100 \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    AUDITGLOBALNOTIFY=true \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
	MINIO_ROOT_USER=ENC(d6bDYa5aGMyXndGA0edIGWFtcw3UygXi) \
	MINIO_ROOT_PASSWORD=ENC(kV79KowPOv0J8QGeQ6iSQcFUqoZalNVj) \
    OP_LOG_LEVEL=INFO \
    TIMESCALE_ENABLED=false \
  	ELASTICSEARCH_ENABLED=false \
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \
    SBA_USERNAME=operations \
	SBA_SECRET=changeIt2019! \
    ENABLEHADOOP=false \
    KAFKAENABLED=false

COPY docker-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["docker-entrypoint.sh"]
