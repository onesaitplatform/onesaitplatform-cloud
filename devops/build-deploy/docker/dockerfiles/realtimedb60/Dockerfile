FROM mongo:6.0.13

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="realtimedb MongoDB 6.0.13 based"	

# MongoDB environment
ENV MONGO_INITDB_DATABASE=onesaitplatform_rtdb \
	MONGO_INITDB_ROOT_USERNAME=platformadmin \
	MONGO_INITDB_ROOT_PASSWORD=0pen-platf0rm-2018!


RUN chmod -R 777 /data/db

EXPOSE 27017

USER mongodb
