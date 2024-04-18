#!/usr/bin/env bash

HOST="127.0.0.1"
PORT="27017"
DB=onesaitplatform_rtdb
USER=${user}
PASSWORD=${password}
AUTHDB=admin
oid=\$oid

for id in ${files}
do
    echo "Exporting $HOST:$PORT/$DB/$id ..."
	echo "Executing mongofiles -u $USER -p $PASSWORD --authenticationDatabase $AUTHDB --host $HOST --port $PORT --db $DB get_id '{\"$oid\": \"$id\"}'"
    mongofiles -u $USER -p $PASSWORD --authenticationDatabase $AUTHDB --host $HOST --port $PORT --db $DB get_id '{"$oid": "'$id'"}'
done

echo "Done."