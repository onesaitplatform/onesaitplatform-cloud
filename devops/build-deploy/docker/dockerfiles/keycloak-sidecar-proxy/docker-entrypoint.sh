#!/bin/bash

set -e

if [ "$1" = 'keycloak-proxy' ]; then

    if [ -f $KEYSTORE_PASSWORD_FILE ]; then
        KEYSTORE_PASSWORD=`cat $KEYSTORE_PASSWORD_FILE`
    else
        echo $KEYSTORE_PASSWORD > $KEYSTORE_PASSWORD_FILE
    fi

    if [ -f $KEY_PASSWORD_FILE ]; then
        KEY_PASSWORD=`cat $KEY_PASSWORD_FILE`
    else
        echo $KEY_PASSWORD > $KEY_PASSWORD_FILE
    fi

    if [ -f $TRUSTSTORE_PASSWORD_FILE ]; then
        TRUSTSTORE_PASSWORD=`cat $TRUSTSTORE_PASSWORD_FILE`
    else
        echo $TRUSTSTORE_PASSWORD > $TRUSTSTORE_PASSWORD_FILE
    fi

    echo "Starting Keycloak Proxy $KEYCLOAK_VERSION"
    exec java -Xbootclasspath/p:/opt/keycloak-proxy/lib/alpn-boot.jar -jar /opt/keycloak-proxy/bin/launcher.jar /opt/keycloak-proxy/conf/proxy.json
    exit $?
fi

exec "$@"
