FROM telegraf:latest

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
      module.name="telegraf-streamsets" \
      module.streamsets.version="3.10"

USER root

RUN apt -y update && \
    apt install -y jq 

ADD telegraf /etc/telegraf

RUN mkdir -p /tmp/sdc_telegraf_tmpFiles

RUN chown -R telegraf:telegraf /etc/telegraf && \
    chown -R telegraf:telegraf /tmp/sdc_telegraf_tmpFiles

ENV INTERVAL=60s \
    FLUSH_INTERVAL=15s \
    HOSTNAME=telegraf \
    INFLUXDB_URLS="http://influxdb:8086" \
    INFLUXDB_DATABASE=telegraf \
    INFLUXDB_TIMEOUT=5s \
    INFLUXDB_USERNAME=telegraf \
    INFLUXDB_PASSWORD=Welcome1 \
    LABEL=monitored \
    STREAMSETS_INSTANCES='{"sdc1": {"name": "sdc1", "host": "streamsets", "port": 18630}}'  \
    SDC_USER="" \
    SDC_PASS=""

USER telegraf

ENTRYPOINT ["/entrypoint.sh"]
CMD ["telegraf"]
