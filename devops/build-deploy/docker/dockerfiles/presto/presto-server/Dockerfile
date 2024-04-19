FROM openjdk:8-jre

ENV PRESTO_NODE_ID=coordinator-worker \
	PRESTO_COORDINATOR=true \
	PRESTO_DISCOVERY_SERVER_ENABLED=true \
	PRESTO_SCHEDULER_INCLUDE_COORDINATOR=true \
	PRESTO_QUERY_MAX_MEMORY=50GB \
	PRESTO_QUERY_MAX_MEMORY_PER_NODE=1GB \
	PRESTO_QUERY_MAX_TOTAL_MEMORY_PER_NODE=2GB \
	PRESTO_DISCOVERY_URI=http://presto-coordinator:8080 \
	HIVE_METASTORE_URI=thrift://presto-metastore-server:9083 \
	MINIO_SERVER_ENDPOINT=http://minio:9000 \
	MINIO_ROOT_USER=minio \
	MINIO_ROOT_PASSWORD=minio123

# Presto version will be passed in at build time
ARG PRESTO_VERSION=0.254

# Set the URL to download
ARG PRESTO_BIN=https://repo1.maven.org/maven2/com/facebook/presto/presto-server/${PRESTO_VERSION}/presto-server-${PRESTO_VERSION}.tar.gz

# Update the base image OS and install wget and python. Download Presto and unpack it to /opt/presto. Download the Presto CLI and put it in the image
RUN apt-get update && \
    apt-get install -y wget python less vim curl && \
    wget --quiet ${PRESTO_BIN} && \
    mkdir -p /opt && \
    tar -xf presto-server-${PRESTO_VERSION}.tar.gz -C /opt && \
    rm presto-server-${PRESTO_VERSION}.tar.gz && \
    ln -s /opt/presto-server-${PRESTO_VERSION} /opt/presto && \
	wget --quiet https://repo1.maven.org/maven2/com/facebook/presto/presto-cli/${PRESTO_VERSION}/presto-cli-${PRESTO_VERSION}-executable.jar && \
    mv presto-cli-${PRESTO_VERSION}-executable.jar /usr/local/bin/presto && \
	chmod +x /usr/local/bin/presto
	
# Copy configuration files on the host into the image
COPY etc /opt/presto/etc
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod ugo+x /docker-entrypoint.sh

	
# Specify the entrypoint to start
ENTRYPOINT ["/docker-entrypoint.sh"]