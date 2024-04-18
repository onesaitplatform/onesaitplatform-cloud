FROM kartoza/geoserver:2.21.0

# Create directories
RUN mkdir -p /opt/geoserver/data_dir && \
    mkdir -p /etc/certs && \
    mkdir -p /opt/footprints_dir && \
    mkdir -p /opt/fonts && \
    mkdir -p /opt/geoserver/data_dir/gwc && \
    mkdir -p /opt/geoserver/data_dir/user_projections && \
    mkdir -p /opt/geoserver/data_dir/logs && \
    mkdir -p /geoserver && \
    mkdir -p /usr/local/tomcat/webapps/manager/META-INF && \
    mkdir -p /settings

#Set environment variables
ENV GEOSERVER_USERNAME=admin \
    GEOSERVER_PASSWORD=geoserver \
    GEOSERVER_WORKSPACE=onesaitplatform \
    GEOSERVER_NAMESPACE="http://development.onesaitplatform.com" \
    GEOSERVER_NAMESPACE_ID=onesaitplatform \
    GEOSERVER_DATASTORE="postgis" \  
    PG_HOSTNAME=postgis \
    PG_PORT=5432 \
    PG_USERNAME=docker \
    PG_PASSWORD=docker \
    PG_DATABASE=gis

USER root

# Give permissions
RUN chown -R geoserveruser:geoserverusers /usr/local/tomcat && \
    chown -R geoserveruser:geoserverusers /opt/footprints_dir && \
    chown -R geoserveruser:geoserverusers /opt/geoserver/data_dir && \
    chown -R geoserveruser:geoserverusers /etc/certs && \
    chown -R geoserveruser:geoserverusers /opt/fonts && \
    chown -R geoserveruser:geoserverusers /home/geoserveruser && \
    chown -R geoserveruser:geoserverusers /community_plugins && \
    chown -R geoserveruser:geoserverusers /stable_plugins && \
    chown -R geoserveruser:geoserverusers /geoserver && \
    chown -R geoserveruser:geoserverusers /opt/geoserver/data_dir/logs && \
    chown -R geoserveruser:geoserverusers /settings && \
    chown -R geoserveruser:geoserverusers /usr/share/fonts && \
    chown -R geoserveruser:geoserverusers /scripts && \
    chown -R geoserveruser:geoserverusers /tomcat_apps.zip && \
    chown -R geoserveruser:geoserverusers /tmp && \
    chown -R geoserveruser:geoserverusers /opt/geoserver/data_dir/gwc && \
    chown -R geoserveruser:geoserverusers /build_data && \
    chown -R geoserveruser:geoserverusers /opt/geoserver/data_dir/user_projections

RUN touch /usr/local/tomcat/log4j.properties

RUN chmod o+rw /etc/certs && \
    chmod -R 777 /usr/local/tomcat/conf && \
    chmod -R 777 /build_data && \
    chmod -R 777 /usr/local/tomcat/webapps/geoserver/WEB-INF/lib && \
    chmod -R 777 /opt/geoserver/data_dir/user_projections && \
    chmod -R 777 /usr/local/tomcat/data/user_projections && \
    chmod -R 777 /opt/geoserver/data_dir && \
    chmod -R 777 /usr/local/tomcat/webapps && \
    chmod -R 777 /usr/local/tomcat/webapps/manager/META-INF && \
    chmod -R 777 /settings && \
    chmod 777 /usr/local/tomcat/log4j.properties && \
    chmod -R 777 /opt/geoserver/data_dir/logs
    

USER geoserveruser
