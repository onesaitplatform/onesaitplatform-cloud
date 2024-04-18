#!/usr/bin/env bash

HOST="127.0.0.1"
PORT="27017"
DB=onesaitplatform_rtdb
USER=${user}
PASSWORD=${password}
AUTHDB=admin

for c in ${ontologies}
do
    echo "Importing $HOST:$PORT/$DB/$c from $c.json..."
    mongoimport -u $USER -p $PASSWORD --authenticationDatabase $AUTHDB --host $HOST --port $PORT --db $DB --collection $c --file=$c.json
	
done

echo "Done."