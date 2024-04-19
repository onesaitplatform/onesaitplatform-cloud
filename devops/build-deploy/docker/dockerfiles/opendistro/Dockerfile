FROM amazon/opendistro-for-elasticsearch:1.11.0

# Metadata
LABEL module.maintainer "onesaitplatform@indra.es" \
      module.name="audit-opendistro-based"

ENV discovery.type=single-node \
    cluster.name=onesaitplatform \
    bootstrap.memory_lock=true \
    opendistro_security.disabled=false \
	  ES_JAVA_OPTS="-Xms2048m -Xmx2048m"


# Elastic Search custom configuration
COPY --chown=elasticsearch:elasticsearch elasticsearch.yml /usr/share/elasticsearch/config/

# OpenDistro Internal users
COPY --chown=elasticsearch:elasticsearch internal_users.yml /usr/share/elasticsearch/plugins/opendistro_security/securityconfig/internal_users.yml

# Self signed certificates
COPY --chown=elasticsearch:elasticsearch root-ca.pem /usr/share/elasticsearch/config/
COPY --chown=elasticsearch:elasticsearch esnode.pem /usr/share/elasticsearch/config/
COPY --chown=elasticsearch:elasticsearch esnode-key.pem /usr/share/elasticsearch/config/

USER elasticsearch

EXPOSE 9200 9300 9600

# Elastic Search data directory
VOLUME ["/usr/share/elasticsearch/data"]
