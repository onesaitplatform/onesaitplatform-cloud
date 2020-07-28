#!/bin/bash

ROOT_DIRECTORY="/datadrive/onesaitplatform"

DIRS=("platform-logs"
	"nginx/certs"
	"configdb" 
	"realtimedb" 
	"schedulerdb"
	"flowengine"
	"webprojects"
	"export"
	"binaryrepository"
	"zeppelin/notebook"
	"zeppelin/conf"
	"streamsets/data"
	"streamsets/lib"
	"streamsets/certs")

for dir in "${DIRS[@]}";do
	d="$ROOT_DIRECTORY/$dir"
	echo "creating directory $d"
	mkdir -p $d
done;