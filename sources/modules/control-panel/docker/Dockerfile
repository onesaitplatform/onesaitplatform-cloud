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
	mkdir ./target

# create sofia user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait 

# Install git
RUN apk update && apk upgrade && \
    apk add --no-cache bash git openssh	

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /usr/share/maven && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \    
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local
    
# libc6-compat package for GNU libc compatibility
# needed for execute ocp binary
ENV BOWTIE2_VERSION 2.2.8
ENV ASPECTJ_VERSION 1.8.9

RUN apk add --no-cache \
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

# Rancher CLI    
COPY rancher /bin
	
USER onesait

EXPOSE 18000 5701

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G -javaagent:/usr/local/aspectjweaver-$ASPECTJ_VERSION.jar" \
    SERVER_NAME=localhost \
    KAFKAENABLED=false \
    KAFKAHOST=kafka \
    KAFKAPORT=9095 \    
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \
	REALTIMEDBUSEAUTH=true \
	REALTIMEDBUSER=platformadmin \
	REALTIMEDBPASS=ENC(GGpZ1sLYnXwK+vz2QLkI/VK7geKcdM4pZaTL6hv6QTk=) \ 
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \		    
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
    GRAVITEE_ON=false \
    GRAVITEE_CLIENTID=onesait \
    GRAVITEE_CLIENTSECRET=admin \
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
    LDAP_URL="" \
    LDAP_BASE_DN=OU="" \
    LDAP_ADMIN_DN="" \
    LDAP_ADMIN_PASSWORD="" \
    LDAP_DEFAULT_ROLE=ROLE_USER \
    LDAP_USERID_ATT=sAMAccountName \
    LDAP_MAIL_ATT=mail \
    LDAP_CN_ATT=cn \
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
    ENABLE_METRICS=true

    
ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -Dloader.path=file:/usr/local/themes/ -Djava.awt.headless=true -jar /app.jar
