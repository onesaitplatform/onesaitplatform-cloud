#!/bin/bash
export SIDECAR_NAME="$STACK-$SERVICE_NAME-$SERVICE_NAME-sidecar-1"
export RANCHER_SERVICE_NAME="$STACK-$SERVICE_NAME-1"
echo sidecar listening on address: $RANCHER_SERVICE_NAME
echo sidecar for service: $SERVICE_NAME


if [ -z "$UPSTREAMS" ]
then
      service_upstreams=[]
else
     echo "Found configured upstreams"
     IFS=';' read -ra upstreams <<< "$UPSTREAMS"
     export service_upstreams=[]
     for upstream in "${upstreams[@]}"
     do
       echo adding upstream $upstream
       service=$(cut -d':' -f1 <<< "$upstream")
       port=$(cut -d':' -f2 <<< "$upstream")
       service_upstreams=$(echo $service_upstreams | jq '.+=[ {"destination_name":"'"$service"'","local_bind_port":'"$(($port + 0))"', "local_bind_address":"0.0.0.0"} ]')
       echo $service_upstreams
     done
     echo $service_upstreams
fi

echo '{
  "Name": "'"$SERVICE_NAME"'",
  "Tags": [
    "'"$SERVICE_NAME"'"
  ],
  "Port": '"$(($PORT + 0))"',
  "Address": "'"$RANCHER_SERVICE_NAME"'",
   "connect": {
      "sidecar_service": {
      	"address":"'"$RANCHER_SERVICE_NAME"'",
      	"port":21255,
      	"check":{
      		"name":"$SERVICE_NAME-sidecar",
        	"tcp": "'"$RANCHER_SERVICE_NAME:21255"'",
        	"interval" :"10s"
      	},
      	"proxy":{
      		"local_service_address":"'"$RANCHER_SERVICE_NAME"'",
          "upstreams": '"$service_upstreams"'
      	}
      }
    }
}'

curl --location --request PUT "http://$CONSUL_AGENT:8500/v1/agent/service/register" \
--header 'Content-Type: application/json' \
--data-raw '{
  "Name": "'"$SERVICE_NAME"'",
  "Tags": [
    "'"$SERVICE_NAME"'"
  ],
  "Port": '"$(($PORT + 0))"',
  "Address": "'"$RANCHER_SERVICE_NAME"'",
   "connect": {
      "sidecar_service": {
      	"address":"'"$RANCHER_SERVICE_NAME"'",
      	"port":21255,
      	"check":{
      		"name":"$SERVICE_NAME-sidecar",
        	"tcp": "'"$RANCHER_SERVICE_NAME:21255"'",
        	"interval" :"10s"
      	},
      	"proxy":{
      		"local_service_address":"'"$RANCHER_SERVICE_NAME"'",
          "upstreams": '"$service_upstreams"'
      	}
      }
    }
}'


consul connect proxy -sidecar-for "$SERVICE_NAME" -http-addr="$CONSUL_AGENT":8500 -log-level=DEBUG
