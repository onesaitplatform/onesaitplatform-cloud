#!/bin/sh

echo "Sustituyendo variables de entorno en ficheros de propiedades de onesaitplatform"

grep -rl '${GEOSERVER_WORKSPACE}' /opt/geoserver/data_dir/workspaces | xargs sed -i 's/${GEOSERVER_WORKSPACE}/'"$GEOSERVER_WORKSPACE"'/g'
grep -rl '${GEOSERVER_NAMESPACE}' /opt/geoserver/data_dir/workspaces | xargs sed -i 's,${GEOSERVER_NAMESPACE},'"$GEOSERVER_NAMESPACE"',g'
grep -rl '${GEOSERVER_NAMESPACE_ID}' /opt/geoserver/data_dir/workspaces | xargs sed -i 's/${GEOSERVER_NAMESPACE_ID}/'"$GEOSERVER_NAMESPACE_ID"'/g'
grep -rl '${GEOSERVER_DATASTORE}' /opt/geoserver/data_dir/workspaces | xargs sed -i 's/${GEOSERVER_DATASTORE}/'"$GEOSERVER_DATASTORE"'/g'
grep -rl '${GEOSERVER_STYLENAME}' /opt/geoserver/data_dir/workspaces | xargs sed -i 's/${GEOSERVER_STYLENAME}/'"$GEOSERVER_STYLENAME"'/g'
grep -rl '${GEOSERVER_USERNAME}' /opt/geoserver/data_dir/workspaces | xargs sed -i 's/${GEOSERVER_USERNAME}/'"$GEOSERVER_USERNAME"'/g'
grep -rl '${GEOSERVER_PASSWORD}' /opt/geoserver/data_dir/workspaces | xargs sed -i 's/${GEOSERVER_PASSWORD}/'"$GEOSERVER_PASSWORD"'/g'
grep -rl '${PG_DATABASE}' /opt/geoserver/data_dir/workspaces | xargs sed -i 's/${PG_DATABASE}/'"$PG_DATABASE"'/g'
grep -rl '${PG_HOSTNAME}' /opt/geoserver/data_dir/workspaces | xargs sed -i 's/${PG_HOSTNAME}/'"$PG_HOSTNAME"'/g'
grep -rl '${PG_PORT}' /opt/geoserver/data_dir/workspaces | xargs sed -i 's/${PG_PORT}/'"$PG_PORT"'/g'
grep -rl '${PG_PASSWORD}' /opt/geoserver/data_dir/workspaces | xargs sed -i 's/${PG_PASSWORD}/'"$PG_PASSWORD"'/g'
grep -rl '${PG_USERNAME}' /opt/geoserver/data_dir/workspaces | xargs sed -i 's/${PG_USERNAME}/'"$PG_USERNAME"'/g'

sh /scripts/entrypoint.sh