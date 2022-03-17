FROM maven:3.5.3-jdk-8-alpine

# metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="controlpanel"

ADD *-exec.jar app.jar

# Timezone
RUN apk add --no-cache tzdata

# web projects folder & logs folder
RUN mkdir -p /usr/local/webprojects && \
	mkdir -p /usr/local/themes && \
    mkdir -p /usr/local/files && \
	mkdir -p /var/log/platform-logs && \
	mkdir /application


WORKDIR /application
RUN unzip /app.jar &&\
	rm /app.jar

# create sofia user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

# Install git
RUN apk update && apk upgrade && \
    apk add --no-cache bash git openssh

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /usr/share/maven && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait /application && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local &&\
    chmod -R 777 /application &&\
    chmod -R 777 /etc/ssl/certs/java

# libc6-compat package for GNU libc compatibility
# needed for execute ocp binary
ENV BOWTIE2_VERSION 2.2.8
ENV ASPECTJ_VERSION 1.8.9

RUN apk add --no-cache \
        bash \
        perl \
        wget \
        openssl \
        ca-certificates \
        libc6-compat \
        libstdc++ \
        nss \
        fontconfig \
        ttf-dejavu \
    && wget https://downloads.sourceforge.net/project/bowtie-bio/bowtie2/$BOWTIE2_VERSION/bowtie2-$BOWTIE2_VERSION-linux-x86_64.zip \
    && unzip -d /usr/local bowtie2-$BOWTIE2_VERSION-linux-x86_64.zip \
    && rm bowtie2-$BOWTIE2_VERSION-linux-x86_64.zip

RUN cd /tmp/ \
    && wget 'http://download.oracle.com/otn-pub/java/jce/8/jce_policy-8.zip' --header "Cookie: oraclelicense=accept-securebackup-cookie" \
    && unzip jce_policy-8.zip \
    && yes |cp -v /tmp/UnlimitedJCEPolicyJDK8/*.jar /usr/lib/jvm/java-1.8-openjdk/jre/lib/security \
    && rm jce_policy-8.zip \
    && cd /usr/local \
    && wget https://repo1.maven.org/maven2/org/aspectj/aspectjweaver/$ASPECTJ_VERSION/aspectjweaver-$ASPECTJ_VERSION.jar


VOLUME ["/tmp", "/usr/local/webprojects", "/var/log/platform-logs", "/usr/local/themes"]


USER onesait

EXPOSE 18000 5701

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G -javaagent:/usr/local/aspectjweaver-$ASPECTJ_VERSION.jar" \
    SERVER_NAME=localhost \
    KAFKAENABLED=false \
    KAFKABROKERS=none \
    KAFKAHOST=kafka \
    KAFKAPORT=9095 \
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \
	REALTIMEDBUSEAUTH=true \
  	REALTIMEDBUSER=platformadmin \
	REALTIMEDBPASS=ENC(GGpZ1sLYnXwK+vz2QLkI/VK7geKcdM4pZaTL6hv6QTk=) \
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \
	CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
	CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
	SCHEDULERDBBURL="jdbc:mysql://configdb:3306/onesaitplatform_scheduler?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
	CONFIGDBUSER=root \
	CONFIGDBPASS=changeIt! \
    CONFIGDB_MAX_ACTIVE=2 \
    CONFIGDB_MAX_IDLE=2 \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    AUDITGLOBALNOTIFY=true \
    QUASARHOST=quasar \
    QUASARPORT=10800 \
    BURROWHOST=burrow \
    BURROWPORT=18400 \
    FLOW_DOMAIN_MIN_PORT=28000 \
    FLOW_DOMAIN_MAX_PORT=28500 \
    KSQLSERVER=http://streamingflowengineservice:20200 \
    KSQLENABLE=false \
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
	CAPTCHA_ON=false \
	CAPTCHA_TOKEN=6Lc40JkUAAAAANyTpMrn9JNwKsiNRCY0bZ32cWIh \
	CSRF_ON=true \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181 \
    CLOUD_GATEWAY=https://development.onesaitplatform.com \
    REALTIMEDBNAME=onesaitplatform_rtdb \
    AUTH_PROVIDER=configdb \
    CAS_ATT_MAIL=mail \
    CAS_ATT_NAME=name \
    CAS_MAIL_SUFFIX=onesaitplatform.com \
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
    LDAP_ADMIN_WHITELIST=administrator,admin \
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
    SAML_INCLUDE_PORT=false \
    SAML_UNAUTHORIZED_URL=https://unauthorized.axpoiberia.es/ \
    STREAMSETS_SDC_VERSION=3.10.0 \
    TWOFA_ENABLED=false \
    CONFIGDB_ACL_ENABLED=false \
    CONFIGDB_ACL_LIST=administrator,analytics,dataviewer,demo_developer,demo_user,developer,operations,partner,sysadmin,user \
    DS_TIME_BETWEEN_EVICTION_RUNS_MILLIS=10000 \
    DS_MIN_EVICTABLE_IDLE_TIME_MILLIS=180000 \
    DS_MAX_WAIT_MILLIS=10000 \
    DS_MAX_WAIT=10000 \
    DS_INITIAL_SIZE=10 \
    DS_MAX_ACTIVE=30 \
    DS_MAX_IDLE=5 \
    DS_MIN_IDLE=5 \
    DS_REMOVE_ABANDONED=true \
    DS_REMOVE_ABANDONED_TIMEOUT=60 \
    ENABLE_METRICS=true \
    HTTP_MAX_FILE_SIZE=200Mb \
    HTTP_MAX_REQUEST_SIZE=200Mb \
    DYNAMIC_LOADBALANCER_ENABLED=false \
    DYNAMIC_LOADBALANCER_ENDPOINT=http://localhost:8000/nginx \
    QUERY_TOOL_ALLOWED_OPERATIONS=false \
    MAIL_PROXY_HOST=localhost \
    MAIL_PROXY_PORT=8080 \
    MULTITENANCY_ENABLED=false \
    MAIL_PROXY_HOST=localhost \
    MAIL_PROXY_PORT=8080 \
    RESTPLANNER_CHECKSSL=false \
    MAIL_PROXY_HOST=localhost \
    MAIL_PROXY_PORT=8080 \
    SPLASH_ON=false \
    SPLASH_EVERYXHOURS=24 \
    ELASTICSEARCH_ENABLED=false \
    NOTEBOOKPLATFORMAUTH=false \
    X509_ID_ATT=SERIALNUMBER \
    X509_ID_REGEX=(.*?)(?:,|$) \
    X509_CN_REGEX=CN=\"(.*?)(?:\"|$) \
    X509_ADMIN_ID=51503283G \
    X509_HASH_ID=false \
    SECURE_XFRAMEOPTIONS=true \
    ENCRYPTION_KEY=ENC(VYVseIUh5xiRd8ws0prbEAg6bGq7vmbfi3gkM65HECy+YPbjz4f49w==) \
    ENCRYPTION_ITERATIONS=5 \
    JWT_KEY=ENC(BXYVNNRDiawMsH4sY79FUPig6FWPGbcoMusSVrXXb6ZLtZ4qhIEUazqv6XrcRm39s8zEu2d1G8yTowyE/sEv7YF0vrLNdb1m) \
    JWT_CLIENT_SECRET=ENC(siZm4DUe6Y1FkxoUH7y0oUCiGDIq/vRX) \
    OAUTH_SSO_ENABLED=false \
    USE_KEYCLOAK=false \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    CORS_ALLOWED_URLS="*" \
	INTERNAL_SSL=false \
	EDGE_SERVER_NAME=iothub02.onesaitplatform.com \
	SBA_USERNAME=operations \
	SBA_SECRET=ENC(2Xd2AOD2NrMaWs915/st8C4ILo3qlHXv)

COPY docker-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["docker-entrypoint.sh"]

#ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -Dloader.path=file:/usr/local/themes/ -Djava.awt.headless=true -jar /app.jar
