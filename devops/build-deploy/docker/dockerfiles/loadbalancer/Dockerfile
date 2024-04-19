FROM bitnami/nginx

# Metadata
LABEL module.maintainer "onesaitplatform@indra.es" \
	    module.name="loadbalancer"

VOLUME /usr/local/webprojects

# create onesait user/group
USER 0
RUN groupadd -g 10001 onesait && useradd -u 10001 -g 10001 onesait --no-create-home

## Install 'vim' and give write permissions to custom config folder
RUN install_packages vim
RUN chmod ugo+w /opt/bitnami/nginx/conf/server_blocks

# Copy nginx config file
COPY my_server_ssl_block.conf /opt/bitnami/nginx/conf/server_blocks/my_server_block.conf

RUN mkdir -p /opt/bitnami/nginx/conf/server_blocks/conf.d/api && \
    mkdir -p /opt/bitnami/nginx/conf/server_blocks/conf.d/engine && \
		mkdir -p /opt/bitnami/nginx/conf/server_blocks/conf.d/things && \
		mkdir -p /opt/bitnami/nginx/conf/server_blocks/conf.d/tools && \
		mkdir -p /opt/bitnami/nginx/conf/server_blocks/conf.d/intelligence && \
		mkdir -p /opt/bitnami/nginx/conf/server_blocks/conf.d/opendata && \
		mkdir -p /opt/bitnami/nginx/conf/server_blocks/conf.d/engine && \
		mkdir -p /opt/bitnami/nginx/conf/server_blocks/conf.d/log

RUN chmod ugo+w /opt/bitnami/nginx/conf/server_blocks/my_server_block.conf
RUN chmod ugo+w /opt/bitnami/nginx/tmp/
RUN chmod -R ugo+w /opt/bitnami/nginx/conf/server_blocks/conf.d

USER onesait

EXPOSE 8080
EXPOSE 8443
