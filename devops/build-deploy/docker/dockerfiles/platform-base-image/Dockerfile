FROM openjdk:8-jre-alpine

# Metadata
LABEL platform.image.maintainer="Onesait Platform"
LABEL platform.image.vendor="Minsait"
LABEL platform.image.support="support@onesaitplatform.com"
LABEL platform.image.license="Apache Software License 2"

# HEAP tuning parameters
ENV JVM_INITIAL_JAVA_HEAP=1g
ENV JVM_MAX_JAVA_HEAP=3g

# Young generation represents 
# all the objects which have a short life of time
# Oracle recommends that you keep the 
# size for the young generation between 
# a half and a quarter of the overall heap size
ENV JVM_YOUNG_GENERATION=1g

# JVM Logging
# ENV JVM_LOGGING="-XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/tmp/gc.log"

# Datasource pool configuration. These variables are used by modules that use configdb.
ENV DS_TIME_BETWEEN_EVICTION_RUNS_MILLIS=10000 \
    DS_MIN_EVICTABLE_IDLE_TIME_MILLIS=180000 \
    DS_MAX_WAIT_MILLIS=10000 \
    DS_MAX_WAIT=10000 \
    DS_INITIAL_SIZE=10 \
    DS_MAX_ACTIVE=30 \
    DS_MAX_IDLE=5 \
    DS_MIN_IDLE=5 \
    DS_REMOVE_ABANDONED=true \
    DS_REMOVE_ABANDONED_TIMEOUT=60

ENV JAVA_OPTS="$JAVA_OPTS -Xms${JVM_INITIAL_JAVA_HEAP} \
                          -Xmx${JVM_MAX_JAVA_HEAP} \
                          -Xmn${JVM_YOUNG_GENERATION} \
                          -XX:-TieredCompilation \ 
                          -XX:ReservedCodeCacheSize=240m \                         
                          -XX:+UnlockExperimentalVMOptions \
                          -XX:+UseCGroupMemoryLimitForHeap \
                          -Djava.security.egd=file:/dev/./urandom \
                          -Dfile.encoding=UTF-8"                                              
