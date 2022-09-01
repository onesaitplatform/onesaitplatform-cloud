#!/bin/sh
echo "Substituting environment variables in Presto properties"	

grep -rl '${PRESTO_NODE_ID}' /opt/presto/etc/node.properties | xargs sed -i 's/${PRESTO_NODE_ID}/'"$PRESTO_NODE_ID"'/g'

grep -rl '${PRESTO_COORDINATOR}' /opt/presto/etc/config.properties | xargs sed -i 's/${PRESTO_COORDINATOR}/'"$PRESTO_COORDINATOR"'/g'
grep -rl '${PRESTO_SCHEDULER_INCLUDE_COORDINATOR}' /opt/presto/etc/config.properties | xargs sed -i 's/${PRESTO_SCHEDULER_INCLUDE_COORDINATOR}/'"$PRESTO_SCHEDULER_INCLUDE_COORDINATOR"'/g'
grep -rl '${PRESTO_QUERY_MAX_MEMORY}' /opt/presto/etc/config.properties | xargs sed -i 's/${PRESTO_QUERY_MAX_MEMORY}/'"$PRESTO_QUERY_MAX_MEMORY"'/g'
grep -rl '${PRESTO_QUERY_MAX_MEMORY_PER_NODE}' /opt/presto/etc/config.properties | xargs sed -i 's/${PRESTO_QUERY_MAX_MEMORY_PER_NODE}/'"$PRESTO_QUERY_MAX_MEMORY_PER_NODE"'/g'
grep -rl '${PRESTO_QUERY_MAX_TOTAL_MEMORY_PER_NODE}' /opt/presto/etc/config.properties | xargs sed -i 's/${PRESTO_QUERY_MAX_TOTAL_MEMORY_PER_NODE}/'"$PRESTO_QUERY_MAX_TOTAL_MEMORY_PER_NODE"'/g'
grep -rl '${PRESTO_DISCOVERY_URI}' /opt/presto/etc/config.properties | xargs sed -i 's!${PRESTO_DISCOVERY_URI}!'"$PRESTO_DISCOVERY_URI"'!g'

if [ "$PRESTO_DISCOVERY_SERVER_ENABLED" = "true" ]; then
 echo 'discovery-server.enabled=true' >> /opt/presto/etc/config.properties 
fi

grep -rl '${HIVE_METASTORE_URI}' /opt/presto/etc/catalog/minio.properties | xargs sed -i 's!${HIVE_METASTORE_URI}!'"$HIVE_METASTORE_URI"'!g'
grep -rl '${MINIO_SERVER_ENDPOINT}' /opt/presto/etc/catalog/minio.properties | xargs sed -i 's!${MINIO_SERVER_ENDPOINT}!'"$MINIO_SERVER_ENDPOINT"'!g'
grep -rl '${MINIO_ROOT_USER}' /opt/presto/etc/catalog/minio.properties | xargs sed -i 's/${MINIO_ROOT_USER}/'"$MINIO_ROOT_USER"'/g'
grep -rl '${MINIO_ROOT_PASSWORD}' /opt/presto/etc/catalog/minio.properties | xargs sed -i 's/${MINIO_ROOT_PASSWORD}/'"$MINIO_ROOT_PASSWORD"'/g'


cat /opt/presto/etc/config.properties

echo "Executing Presto..." 
/opt/presto/bin/launcher run