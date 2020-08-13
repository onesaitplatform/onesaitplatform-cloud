FROM onesaitplatform/baseimage:latest

# Metadata
LABEL platform.image.name="oauthserver"

ENV SERVER_NAME=localhost \
    REALTIMEDBSERVERS=realtimedb:27017 \
    CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
	CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
	SCHEDULERDBBURL="jdbc:mysql://configdb:3306/onesaitplatform_scheduler?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \	
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    CONFIGDB_MAX_ACTIVE=2 \
    CONFIGDB_MAX_IDLE=2 \
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
    LDAP_BASE_DN=OU="" \
    LDAP_ADMIN_DN="" \
    LDAP_ADMIN_PASSWORD="" \
    LDAP_DEFAULT_ROLE=ROLE_USER \
    LDAP_USERID_ATT=sAMAccountName \
    LDAP_MAIL_ATT=mail \
    LDAP_CN_ATT=cn \
    LDAP_IGNORE_PARTIAL_RESULT=false \
    LDAP_ADMINISTRATOR_DN="" \ 
    LDAP_ANALYTICS_DN="" \
    LDAP_DEVELOPER_DN="" \
    LDAP_DATAVIEWER_DN="" \
    LDAP_DEVOPS_DN="" \
    LDAP_OPERATIONS_DN="" \
    LDAP_PARTNER_DN="" \
    LDAP_PLATFORM_ADMIN_DN="" \
    LDAP_SYS_ADMIN_DN="" \
    LDAP_USER_DN="" \
    LDAP_GROUP_OF_NAMES_ATT="" \
    LDAP_ROLES_MEMBER_ATT="" \
    CONFIGDB_ACL_ENABLED=false \
    CONFIGDB_ACL_LIST=administrator,analytics,dataviewer,demo_developer,demo_user,developer,operations,partner,sysadmin,user \
    MULTITENANCY_ENABLED=false \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181   

# Timezone
RUN apk add --no-cache tzdata \
    bash

RUN mkdir -p /var/log/platform-logs && \
	mkdir ./target && \
	mkdir /application


ADD *-exec.jar app.jar

WORKDIR /application
RUN unzip /app.jar &&\
	rm /app.jar

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

RUN chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait /application && \
    chmod -R 777 /var/log && \
    chmod -R 777 /application
    



VOLUME ["/tmp", "/var/log/platform-logs"]

EXPOSE 21000

COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod 775 /usr/local/bin/docker-entrypoint.sh

USER onesait

ENTRYPOINT ["docker-entrypoint.sh"]

