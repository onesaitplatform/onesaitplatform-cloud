#!/bin/bash

source ./config.properties


echo "#############################################################################################"
echo "#                                                                                           #"
echo "#   _____             _                                                                     #"              
echo "#  |  __ \           | |                                                                    #"            
echo "#  | |  | | ___   ___| | _____ _ __                                                         #"
echo "#  | |  | |/ _ \ / __| |/ / _ \ '__|                                                        #"
echo "#  | |__| | (_) | (__|   <  __/ |                                                           #"
echo "#  |_____/ \___/ \___|_|\_\___|_|                                                           #"                
echo "#                                                                                           #"
echo "# OP Community templates generation                                                         #"
echo "# =================================                                                         #"
echo "#                                                                                           #"
echo "# config.properties:                                                                        #"
echo "#                                                                                           #"
echo "# - WORKER2DEPLOY -> Tag host name to deploy  							                  #"
echo "# -----------------------------------------------------------------                         #"
echo "# - DOMAIN_NAME   -> Domain host name 	      			                                  #"
echo "# ----------------------------------------------------------------------                    #"
echo "# - IMAGE_TAG     -> Image tag version 						                              #"
echo "# --------------------------------------------------------------------                      #"
echo "# - PROJECT_NAME  -> Project name								                              #"
echo "#                                                                                           #"
echo "#############################################################################################"


echo "Node to deploy -> $WORKER2DEPLOY"
echo "Domain -> $DOMAIN_NAME"
echo "Image tag-> $IMAGE_TAG"

TEMPLATES="../templates"
DEPLOY_FOLDER="$PROJECT_NAME-$WORKER2DEPLOY"
mkdir $DEPLOY_FOLDER

cp $TEMPLATES/docker-compose-ce.template  $DEPLOY_FOLDER/docker-compose.yml
echo "#####Environment variables substitution."
sed -i "s/{WORKER2DEPLOY}/$WORKER2DEPLOY/g" $DEPLOY_FOLDER/docker-compose.yml
sed -i "s/{DOMAIN_NAME}/$DOMAIN_NAME/g" $DEPLOY_FOLDER/docker-compose.yml
sed -i "s/{IMAGE_TAG}/$IMAGE_TAG/g" $DEPLOY_FOLDER/docker-compose.yml

cp $TEMPLATES/rancher-compose-ce.yml $DEPLOY_FOLDER/rancher-compose.yml

echo "######## Rancher deployment files generated at-> $DEPLOY_FOLDER."
