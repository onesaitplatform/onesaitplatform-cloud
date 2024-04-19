#!/bin/bash

#ZEPPELIN_INSTANCES='{"zepp1": {"name": "zepp1", "host": "zeppelin", "port": 8080, "notes":"Untitled Note 1, Zeppelin Tutorial/R (SparkR)"}}'
#ZEPPELIN_INSTANCES='{"zepp1": {"name": "zepp1", "host": "localhost", "port": 8080, "notes":["Untitled Note 1", "Zeppelin Tutorial/R (SparkR)"]}}'
metrics=$ZEPPELIN_INSTANCES

if [ -z "$metrics" ]; then
	echo "It is mandatory to define almost one zeppelin instance"
	exit 1
fi

instances=$(echo $metrics | jq -r '. | keys[]')

fullMetrics=''

for instance in $instances; do

	selectedNotebooks=$(echo $metrics | jq -r ".$instance.notes")
	host=$(echo $metrics | jq -r ".$instance.host")
	port=$(echo $metrics | jq -r ".$instance.port")
	instanceName=$(echo $metrics | jq -r ".$instance.name")
	notebooks=$(curl -s "http://$host:$port/api/notebook" --connect-timeout 10)
	allNotebooks=$(echo $notebooks | jq -r ".body")

	for (( i=0; i<$(echo $allNotebooks | jq -r "." | jq length); i++  ));do
		status=$(curl -s "http://$host:$port/api/notebook/job/$(echo $allNotebooks | jq -r ".[$i].id")" --connect-timeout 10)
		name=$(echo $allNotebooks | jq -r ".[$i].name" | tr -d '[[:space:]]')
		rawProgress=$(echo $status | jq -r ".body")
		parragraphs=$(echo $rawProgress | jq length)
		progressAgreggate=0

		for (( c=0; c<${parragraphs}; c++ )); do
			progressPerParragraph=$(echo $status | jq -r ".body[$c].progress")
			progressAgreggate=$(($progressAgreggate + $progressPerParragraph))
			if [ $progressPerParragraph != "100" ];then
				break
			fi
		done

		json='{"'${name}'":$i}'

		if [ "$(echo $status | jq ".status")" == '"OK"' ];then
			bresponse='{"title":"'$name'","metrics":{"status":1,"progress":'$(echo $(($progressAgreggate / $parragraphs )))'}}'
		else
			bresponse='{"title":"'$name'","metrics":{"status":0,"progress":'$(echo $(($progressAgreggate / $parragraphs )))'}}'
		fi

		bmetric=$(jq -n -r --argjson i "$bresponse" $json)			
		cmetric=$(echo "$bmetric" | sed 's/"OK"/1/')
		fullMetrics=${fullMetrics}','$(echo $cmetric | sed -e 's/^.//' -e 's/.$//')
	done

	bobject='{"'${instance}'":{"name":"'${instanceName}'","host":"'${host}'","port":"'${port}'","notebooks":{'$(echo $fullMetrics | sed -e 's/^,//')'}}}'
	echo $bobject

done

exit 0
