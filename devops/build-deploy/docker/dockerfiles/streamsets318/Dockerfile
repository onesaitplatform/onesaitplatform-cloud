FROM streamsets/datacollector:3.18.1

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
      module.name="streamsets" \
      module.streamsets.version="3.18.1"

USER root

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

EXPOSE 18630

ADD sdc /etc/sdc
ADD onesaitplatform-streamsets /opt/streamsets-datacollector-user-libs/onesaitplatform-streamsets
ADD resources /opt/streamsets-datacollector-3.18.1/resources
ADD streamsets-orchestrator /opt/streamsets-datacollector-3.18.1/streamsets-libs/streamsets-datacollector-orchestrator-lib

RUN ln -s /opt/streamsets-datacollector-3.18.1/ /opt/streamsets-datacollector

RUN chown -R onesait:onesait /etc/sdc && \
    chmod -R 777 /etc/sdc && \
	chown -R onesait:onesait /logs && \
    chmod -R 777 /logs && \
	chown -R onesait:onesait /data && \
    chmod -R 777 /data && \
	chown -R onesait:onesait /opt/streamsets-datacollector-user-libs/onesaitplatform-streamsets && \
    chmod -R 777 /opt/streamsets-datacollector-user-libs/onesaitplatform-streamsets && \
	chown -R onesait:onesait /opt/streamsets-datacollector-3.18.1/streamsets-libs && \
    chmod -R 777 /opt/streamsets-datacollector-3.18.1 && \
	chown -R onesait:onesait /opt/streamsets-datacollector-3.18.1/resources

# Install Python 3
RUN apk add --no-cache python3 && \
    python3 -m ensurepip && \
    rm -r /usr/lib/python*/ensurepip && \
    pip3 install --upgrade pip setuptools && \
    if [ ! -e /usr/bin/pip ]; then ln -s pip3 /usr/bin/pip ; fi && \
    if [[ ! -e /usr/bin/python ]]; then ln -sf /usr/bin/python3 /usr/bin/python; fi && \
    rm -r /root/.cache

# Install gcc compiler
RUN apk add --no-cache build-base gfortran python python-dev py-pip build-base wget freetype-dev libpng-dev openblas-dev net-snmp net-snmp-tools

ENV OP_STREAMSETS_XMX=2048m \
    OP_STREAMSETS_XMS=1024m \
    OP_STREAMSETS_POOL_SIZE=50 \
    OP_STREAMSETS_SAMPLING_SAMPLE_SIZE=1 \
    OP_STREAMSETS_SAMPLING_POPULATION_SIZE=10000 \
    OP_STREAMSETS_UI_REFRESH_INTERVAL_MS=10000 \
    OP_STREAMSETS_UI_JVMMETRICS_REFRESH_INTERVAL_MS=10000 \
    OP_STREAMSETS_HTTP_SESSION_MAX_INACTIVE_INTERVAL=300 \
    OP_STREAMSETS_MAX_BATCH_SIZE=5000\
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    GRAYLOG_APP_NAME=Streamsets-server-1

# Copy default libraries to temp folder
RUN cp -r /opt/streamsets-datacollector-3.18.1/streamsets-libs /tmp && \
    curl -L -X GET  https://search.maven.org/classic/remotecontent?filepath=biz/paluch/logging/logstash-gelf/1.14.1/logstash-gelf-1.14.1.jar --output /opt/streamsets-datacollector-3.18.1/container-lib/logstash-gelf-1.14.1.jar

#USER onesait
USER root

#VOLUME ["sdc-stagelibs", "sdc-data"]

COPY docker-entrypoint.sh /
RUN chmod ugo+x /docker-entrypoint.sh

USER sdc

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["dc", "-exec"]

#RUN chmod -R 555 /opt/streamsets-datacollector-3.18.1/streamsets-libs/streamsets-datacollector-orchestrator-lib/lib/.
