#!/bin/sh
export SIDECAR_NAME="localhost"
echo $SIDECAR_NAME

curl --location --request PUT "http://$CONSUL_AGENT:8500/v1/agent/service/register" \
--header 'Content-Type: application/json' \
--data-raw '{
  "Name": "'"$SERVICE_NAME"'",
  "Tags": [
    "'"$SERVICE_NAME"'"
  ],
  "Port": '"$(($PORT + 0))"',
  "Address": "'"$SERVICE_NAME"'",
   "connect": {
      "sidecar_service": {
      	"address":"'"$SIDECAR_NAME"'",
      	"port":20000,
      	"check":{
      		"name":"$SERVICE_NAME-sidecar",
        	"tcp": "'"$SIDECAR_NAME:20000"'",
        	"interval" :"10s"
      	},
      	"proxy":{
      		"local_service_address":"'"$SERVICE_NAME"'"
      	}
      }
    }
}'


consul connect proxy -sidecar-for "$SERVICE_NAME" -http-addr="$CONSUL_AGENT":8500 -log-level=DEBUG