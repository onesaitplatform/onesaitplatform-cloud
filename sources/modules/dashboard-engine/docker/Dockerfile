FROM onesaitplatform/baseimage:latest

# Metadata
LABEL platform.image.name="dashboard"

ADD *-exec.jar app.jar

# Timezone
RUN apk add --no-cache tzdata \
    bash

# logs folder
RUN mkdir -p /var/log/platform-logs && \
    mkdir /application

WORKDIR /application    
RUN unzip /app.jar &&\
	rm /app.jar

# create onesait platform user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \   
    chown -R onesait:onesait /application && \ 
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local &&\
    chmod -R 777 /application

VOLUME ["/tmp", "/var/log/platform-logs"]

EXPOSE 18300

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV SERVER_NAME=localhost \
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \
    REALTIMEDBUSEAUTH=true \
	REALTIMEDBUSER=platformadmin \
	REALTIMEDBPASS=ENC(GGpZ1sLYnXwK+vz2QLkI/VK7geKcdM4pZaTL6hv6QTk=) \
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \
	CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
	CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    CONFIGDB_MAX_ACTIVE=2 \
    CONFIGDB_MAX_IDLE=2 \
    SCHEDULERDBSERVERS=schedulerdb:3306 \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \
    AUDITGLOBALNOTIFY=true \
    QUASARHOST=quasar \
    QUASARPORT=10800 \
    ENABLEHADOOP=false \
	HDFSURL=hdfs://127.0.0.1:8020 \
	HDFSPATH=/user/hdfs \
	HIVEURL=jdbc:hive2://localhost:10000/default \
	HIVEUSERNAME=cloudera-scm \
	HIVEPASSWORD=cloudera-scm \
	HIVEDRIVERCLASS=org.apache.hive.jdbc.HiveDriver \
	IMPALAURL=jdbc:hive2://localhost:21050/default;auth=noSasl \
	KUDUNUMREPLICAS=1 \
	KUDUURL=localhost:7051 \
	INCLUDEKUDUTABLENAME=false \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181 \
    REALTIMEDBNAME=onesaitplatform_rtdb \
    AUTH_PROVIDER=configdb \
    CAS_ATT_MAIL=mail \
    CAS_ATT_NAME=name \
    JKS_URI=saml.jks \
    JKS_PASS=pass \
    KEY_ALIAS=pass \
    KEY_PASS=keypass \
    AD_ADMIN_USERID=administrator \
    IDP_METADATA=url.xml \
    ENTITY_ID=entityid \
    ENTITY_URL=https://${SERVER_NAME} \
    SAML_SCHEME=https \
    SAML_SERVER_NAME=localhost \
    SAML_INCLUDE_PORT=18300 \
    CONFIGDB_ACL_ENABLED=false \
    CONFIGDB_ACL_LIST=administrator,analytics,dataviewer,demo_developer,demo_user,developer,operations,partner,sysadmin,user \
    ELASTICSEARCH_ENABLED=false \
    WSINBOUNDCHANNELCOREPOOL=40 \
    WSINBOUNDCHANNELMAXPOOL=40 \
    WSINBOUNDCHANNELQUEUESIZE= \
    WSOUTBOUNDCHANNELCOREPOOL=20 \
    WSOUTBOUNDCHANNELMAXPOOL=20 \
    WSOUTBOUNDCHANNELQUEUESIZE= \
    WSBROKERCHANNELCOREPOOL=40 \
    WSBROKERCHANNELMAXPOOL=40 \
    WSBROKERCHANNELQUEUESIZE= \
    WSCACHELIMIT=1024 \
    WSLOGGINGPERIOD=30000 \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    USE_KEYCLOAK=false \
    SBA_USERNAME=operations \
	SBA_SECRET=ENC(2Xd2AOD2NrMaWs915/st8C4ILo3qlHXv)

COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod ugo+x /usr/local/bin/docker-entrypoint.sh

USER onesait

ENTRYPOINT ["docker-entrypoint.sh"]
