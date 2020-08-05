#!/usr/bin/env bash

HOST="127.0.0.1"
PORT="27017"
DB=onesaitplatform_rtdb
USER=${user}
PASSWORD=${password}
AUTHDB=admin

for c in ${ontologies}
do
    echo "Exporting $HOST:$PORT/$DB/$c to $c.json..."
    mongoexport -u $USER -p $PASSWORD --authenticationDatabase $AUTHDB --host $HOST --port $PORT --db $DB --collection $c > $c.json
done

echo "Done."