FROM opensearchproject/opensearch:1.2.2

# Metadata
LABEL module.maintainer "onesaitplatform@indra.es" \
      module.name="audit-opendistro-based"

ENV discovery.type=single-node \
    cluster.name=onesaitplatform \
    bootstrap.memory_lock=true \
    opendistro_security.disabled=false \
	  ES_JAVA_OPTS="-Xms2048m -Xmx2048m"


# Elastic Search custom configuration
COPY --chown=opensearch:opensearch opensearch.yml /usr/share/opensearch/config/

# OpenDistro Internal users
COPY --chown=opensearch:opensearch internal_users.yml /usr/share/opensearch/plugins/opensearch-security/securityconfig/internal_users.yml

# Self signed certificates
COPY --chown=opensearch:opensearch root-ca.pem /usr/share/opensearch/config/
COPY --chown=opensearch:opensearch esnode.pem /usr/share/opensearch/config/
COPY --chown=opensearch:opensearch esnode-key.pem /usr/share/opensearch/config/

USER opensearch

EXPOSE 9200 9300 9600

# Elastic Search data directory
VOLUME ["/usr/share/opensearch/data"]
