FROM onesaitplatform/baseimage:latest

# Metadata
LABEL platform.image.name="oauthserver"	

ENV SERVER_NAME=localhost \    
    REALTIMEDBSERVERS=realtimedb:27017 \  
    CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    SCHEDULERDBSERVERS=schedulerdb:3306 \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \        
    AUDITGLOBALNOTIFY=false \      
    QUASARHOST=quasar \
    QUASARPORT=10800 \ 
    REALTIMEDBNAME=onesaitplatform_rtdb \
    AUTH_PROVIDER=configdb \
    LDAP_URL="" \
    LDAP_BASE_DN="" \
    LDAP_ADMIN_DN="" \
    LDAP_ADMIN_PASSWORD="" \
    LDAP_DEFAULT_ROLE=ROLE_USER \
    LDAP_USERID_ATT=sAMAccountName \
    LDAP_MAIL_ATT=mail \
    LDAP_CN_ATT=cn \
    CONFIGDB_ACL_ENABLED=false \
    CONFIGDB_ACL_LIST=administrator,analytics,dataviewer,demo_developer,demo_user,developer,operations,partner,sysadmin,user

# Timezone 
RUN apk add --no-cache tzdata

RUN mkdir -p /var/log/platform-logs && \
	mkdir ./target	

ADD *-exec.jar app.jar

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait 

RUN chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log
    
VOLUME ["/tmp", "/var/log/platform-logs"]
    
USER onesait

EXPOSE 21000

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Dspring.profiles.active=docker -jar /app.jar