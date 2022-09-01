#!/bin/sh

mlflow server \
    --backend-store-uri $STORE_URI \
    --default-artifact-root $ARTIFACT_STORE \
    --host $SERVER_HOST \
    --port $SERVER_PORT
