FROM graylog/graylog:4.0

# Metadata
LABEL module.maintainer "onesaitplatform@indra.es" \
	    module.name="log-centralizer"

# CHANGE ME (must be at least 16 characters)!
ENV GRAYLOG_PASSWORD_SECRET=somepasswordpepper \
    # Password: Erui374h_@eK4
    GRAYLOG_ROOT_PASSWORD_SHA2=2370259bc872582d69a63691596e54a825d3b3170aa929da18296ba58bdbbee5 \
    GRAYLOG_HTTP_EXTERNAL_URI=http://graylog:9000/ \
    GRAYLOG_MONGODB_URI=mongodb://realtimedb:27017/graylog \
    GRAYLOG_ELASTICSEARCH_HOSTS=http://elasticdb:9200

USER root

RUN chmod -R ugo+rw /usr/share/graylog/plugins-merged

USER graylog

EXPOSE 9000 12201
