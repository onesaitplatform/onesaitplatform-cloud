FROM kartoza/geoserver:latest
	
# Metadata
LABEL platform.image.name="onesaitplatform-geoserver-image"

ENV GEOSERVER_USERNAME=admin \
    GEOSERVER_PASSWORD=geoserver \
    GEOSERVER_WORKSPACE=onesaitplatform \
    GEOSERVER_NAMESPACE="http://onesaitplatform.com" \
    GEOSERVER_NAMESPACE_ID=onesaitplatform \
    GEOSERVER_DATASTORE="postgis" \  
    PG_HOSTNAME=postgis \
    PG_PORT=5432 \
    PG_USERNAME=docker \
    PG_PASSWORD=docker \
    PG_DATABASE=gis


RUN mkdir -p /opt/geoserver/data_dir/workspaces/ && \
    mkdir -p /opt/geoserver/data_dir/workspaces/$GEOSERVER_WORKSPACE && \
    mkdir -p /opt/geoserver/data_dir/workspaces/$GEOSERVER_WORKSPACE/$PG_HOSTNAME

COPY data/namespace.xml /opt/geoserver/data_dir/workspaces/$GEOSERVER_WORKSPACE/namespace.xml
COPY data/workspace.xml /opt/geoserver/data_dir/workspaces/$GEOSERVER_WORKSPACE/workspace.xml
COPY data/datastore.xml /opt/geoserver/data_dir/workspaces/$GEOSERVER_WORKSPACE/$PG_HOSTNAME/datastore.xml

COPY docker-entrypoint.sh /usr/local/bin

ENTRYPOINT ["docker-entrypoint.sh"]
