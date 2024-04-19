FROM fnproject/dind

# Metadata
LABEL platform.image.maintainer="Onesait Platform"
LABEL platform.image.vendor="Minsait"
LABEL platform.image.support="support@onesaitplatform.com"
LABEL platform.image.license="Apache Software License 2"

RUN apk update && apk upgrade && \
    apk add --no-cache libc6-compat \
    curl \
    git 
   
RUN apk add --no-cache --repository=http://dl-cdn.alpinelinux.org/alpine/v3.15/community/ --allow-untrusted openjdk17-jre-headless=17.0.7_p7-r0


ENV PATH="$PATH:/usr/lib/jvm/java-17-openjdk/bin" 
    
RUN curl -LSs https://raw.githubusercontent.com/fnproject/cli/master/install | sh
	
ADD *.jar /app.jar

RUN mkdir -p /var/log/platform-logs

EXPOSE 8086

ENV PATH="$PATH:/usr/lib/jvm/java-17-openjdk/bin" \
    FN_API_URL=https://development.onesaitplatform.com/fn \
    FN_REGISTRY=registry.onesaitplatform.com \
    SERVER_NAME=localhost \
    ACCESS_TOKEN_URL=https://${SERVER_NAME}/auth/realms/onesaitplatform/protocol/openid-connect/token \
    AUTHORIZATION_URL=https://${SERVER_NAME}/auth/realms/onesaitplatform/protocol/openid-connect/auth?scope=openid \
    CHECK_TOKEN_URL=https://${SERVER_NAME}/auth/realms/onesaitplatform/protocol/openid-connect/instrospect \
    USERINFO_URL=https://${SERVER_NAME}/auth/realms/onesaitplatform/protocol/openid-connect/userinfo \
    ADMIN_API_KEY=52bd41e41c2c4b919ace6f43560a3d03 \
    CONFIGDBURL=jdbc:mysql://configdb:3306/onesaitplatform_serverless?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true \
    DBADDPROPS= \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
	CONFIGDB_DRIVER=com.mysql.cj.jdbc.Driver \
	CONFIGDB_DIALECT=org.hibernate.dialect.MySQL5InnoDBDialect \
    ROOT_LOG_LEVEL=WARN \
    MINSAIT_LOG_LEVEL=INFO \
    SPRING_LOG_LEVEL=INFO \
    GATEWAY_USER=operations \
    GATEWAY_SECRET=operations \
    HTTP_LOG_LEVEL=INFO
    
    
CMD ["java","-Dspring.profiles.active=docker","-jar","/app.jar"]
