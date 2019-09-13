#!/bin/bash

#
# Copyright Indra Sistemas, S.A.
# 2013-2018 SPAIN
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#      http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ------------------------------------------------------------------------

buildImage()
{
	echo "Docker image generation for onesaitplatform module: "$2
	cd $1/docker
	cp $1/target/*-exec.jar $1/docker/
	docker build -t $USERNAME/$2:$3 .
	rm $1/docker/*.jar
}

buildImageSB2()
{
	echo "Docker image generation for onesaitplatform module: "$2
	cd $1/docker
	cp $1/target/*.jar $1/docker/
	docker build -t $USERNAME/$2:$3 .
	rm $1/docker/*.jar
}

buildDashboardExporter()
{
	echo "Dashboard Exporter image generation with Docker CLI: "
	docker build -t $USERNAME/dashboardexporter:$1 .
}

prepareConfigInitExamples()
{
	echo "Compressing and Copying realtimedb file examples: "
	cp -r $1/src/main/resources/examples/  $1/docker/examples
	if [ $? -eq 0 ]; then
		echo "examples folder compress & copied...................... [OK]"
	else
		echo "examples folder compress & copied..................... [KO]"
	fi	
}

removeConfigInitExamples()
{
	echo "Deleting realtimedb file examples"
	rm -rf $1/docker/examples
	if [ $? -eq 0 ]; then
		echo "examples folder deleted...................... [OK]"
	else
		echo "examples folder deleted..................... [KO]"
	fi		
}

prepareNodeRED()
{
	cp $homepath/../../../../tools/Flow-Engine-Manager/*.zip $homepath/../../../../sources/modules/flow-engine/docker/nodered.zip
	cd $homepath/../../../../sources/modules/flow-engine/docker
	unzip nodered.zip
	echo "Copying onesait platform custom node files"		
	# FIXME: custom file not necessary
	# cp -f $homepath/../dockerfiles/nodered/proxy-nodered.js $homepath/../../../../sources/modules/flow-engine/docker/Flow-Engine-Manager/
	
	# if [ $? -eq 0 ]; then
	#	echo "proxy-nodered.js file copied...................... [OK]"
	# else
	#	echo "proxy-nodered.js file copied..................... [KO]"
	# fi	
	# 
	
	cp -f $homepath/../dockerfiles/nodered/onesait-platform-config.js $homepath/../../../../sources/modules/flow-engine/docker/Flow-Engine-Manager/node_modules/node-red-onesait-platform/nodes/config/onesait-platform-config.js
	
	if [ $? -eq 0 ]; then
		echo "onesait-platform-config.js file copied...................... [OK]"
	else
		echo "onesait-platform-config.js file copied..................... [KO]"
	fi
		
	cp -f $homepath/../dockerfiles/nodered/onesait-platform-public-config.js $homepath/../../../../sources/modules/flow-engine/docker/Flow-Engine-Manager/node_modules/node-red-onesait-platform/public/config/onesait-platform-config.js	
	
	if [ $? -eq 0 ]; then
		echo "onesait-platform-config.js (public) file copied...................... [OK]"
	else
		echo "onesait-platform-config.js (public) file copied..................... [KO]"
	fi	
}

removeNodeRED()
{
	cd $homepath/../../../../sources/modules/flow-engine/docker
	rm -rf Flow-Engine-Manager
	rm nodered.zip		
}

pushImage2Registry()
{
	if [ "$NO_PROMT" = false ]; then
		echo "¿Deploy "$1 " image to registry y/n: "
		read confirmation
	fi
	
	if [[ "$confirmation" == "y" || "$NO_PROMT" = true ]]; then
		if [ "$(docker images -q $USERNAME/$1:$2)" ]; then
			docker tag $USERNAME/$1:$2 $3$USERNAME/$1:$2
			docker push $3$USERNAME/$1:$2
		fi	
	fi	
}

clear
echo "#############################################################################################"
echo "#                                                                                           #"
echo "#   _____             _                                                                     #"              
echo "#  |  __ \           | |                                                                    #"            
echo "#  | |  | | ___   ___| | _____ _ __                                                         #"
echo "#  | |  | |/ _ \ / __| |/ / _ \ '__|                                                        #"
echo "#  | |__| | (_) | (__|   <  __/ |                                                           #"
echo "#  |_____/ \___/ \___|_|\_\___|_|                                                           #"                
echo "#                                                                                           #"
echo "# Docker Image generation                                                                   #"
echo "# =======================                                                                   #"
echo "#                                                                                           #"
echo "# config.properties:                                                                        #"
echo "#                                                                                           #"
echo "# - PUSH2DOCKERHUBREGISTRY -> true -> deploy images to Docker Hub registry                  #"
echo "# - USERNAME -> image name convention <username>/<repository>:<tag>                         #"
echo "# -----------------------------------------------------------------                         #"
echo "# - MODULE_<module_name> -> true -> generate image module                                   #"
echo "# ----------------------------------------------------------------------                    #"
echo "# - MODULE_TAG=<module_tag> -> tag to push image to registry                                #"
echo "# --------------------------------------------------------------------                      #"
echo "# Proxy Configuration:                                                                      #"
echo "# ====================                                                                      #"
echo "# - PROXY_ON -> true -> proxy enabled                                                       #"
echo "# - PROXY_HOST -> <host>:<port>                                                             #"
echo "# - PROXY_USER                                                                              #"
echo "# - PROXY_PASS                                                                              #"
echo "#                                                                                           #"
echo "#############################################################################################"

# Load configuration file
source config.properties

if [[ -z "$1" && "$NO_PROMT" = false ]]; then
	echo "Continue? y/n: "
	
	read confirmation
	
	if [ "$confirmation" != "y" ]; then
		exit 1
	fi
fi

if [ "$PROXY_ON" = true ]; then
	echo "Setting corporate proxy configuration"
	export https_proxy=https://$PROXY_USER:$PROXY_PASS@$PROXY_HOST/
	export http_proxy=http://$PROXY_USER:$PROXY_PASS@$PROXY_HOST/
fi

homepath=$PWD

#####################################################
# Open Platform Module image generation
#####################################################
	
if [[ "$MODULE_CONTROLPANEL" = true && "$(docker images -q $USERNAME/controlpanel 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/control-panel controlpanel $MODULE_TAG
fi	

if [[ "$MODULE_IOTBROKER" = true && "$(docker images -q $USERNAME/iotbroker 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/iot-broker	iotbroker $MODULE_TAG
fi

if [[ "$MODULE_APIMANAGER" = true && "$(docker images -q $USERNAME/apimanager 2> /dev/null)" == "" ]]; then		
	buildImage $homepath/../../../../sources/modules/api-manager apimanager $MODULE_TAG
fi

if [[ "$MODULE_DIGITALTWIN" = true && "$(docker images -q $USERNAME/digitaltwin 2> /dev/null)" == "" ]]; then	
	buildImage $homepath/../../../../sources/modules/digitaltwin-broker	 digitaltwin $MODULE_TAG
fi	

if [[ "$MODULE_DASHBOARDENGINE" = true && "$(docker images -q $USERNAME/dashboard 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/dashboard-engine dashboard $MODULE_TAG
fi

if [[ "$MODULE_DEVICESIMULATOR" = true && "$(docker images -q $USERNAME/devicesimulator 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/device-simulator devicesimulator $MODULE_TAG
fi	

if [[ "$MODULE_MONITORINGUI" = true && "$(docker images -q $USERNAME/monitoringui 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/monitoring-ui monitoringui $MODULE_TAG
fi

if [[ "$MODULE_CACHESERVER" = true && "$(docker images -q $USERNAME/cacheservice 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/cache-server cacheservice $MODULE_TAG
fi

if [[ "$MODULE_ROUTER" = true && "$(docker images -q $USERNAME/router 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/semantic-inf-broker router $MODULE_TAG
fi	

if [[ "$MODULE_OAUTHSERVER" = true && "$(docker images -q $USERNAME/oauthserver 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/oauth-server oauthserver $MODULE_TAG
fi	

if [[ "$MODULE_RTDBMAINTAINER" = true && "$(docker images -q $USERNAME/rtdbmaintainer 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/rtdb-maintainer rtdbmaintainer $MODULE_TAG
fi	

#if [[ "$MODULE_FLOWENGINE" = true && "$(docker images -q $USERNAME/flowengine 2> /dev/null)" == "" ]]; then		
#	prepareNodeRED		
#	
#	buildImage $homepath/../../../../sources/modules/flow-engine flowengine $MODULE_TAG
#	
#	removeNodeRED
#fi

if [[ "$MODULE_CONFIGINIT" = true && "$(docker images -q $USERNAME/configinit 2> /dev/null)" == "" ]]; then
	prepareConfigInitExamples $homepath/../../../../sources/modules/config-init
	
	buildImage $homepath/../../../../sources/modules/config-init configinit $MODULE_TAG
	
	removeConfigInitExamples $homepath/../../../../sources/modules/config-init
fi

if [[ "$MODULE_RULESENGINE" = true && "$(docker images -q $USERNAME/rules-engine 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/rules-engine rules-engine $MODULE_TAG
fi		

echo "Docker images successfully generated!"

if [ "$PUSH2DOCKERHUBREGISTRY" = true ]; then
    echo "Pushing images to Docker Hub registry"
		
	pushImage2Registry controlpanel $MODULE_TAG 
	pushImage2Registry iotbroker $MODULE_TAG 
	pushImage2Registry apimanager $MODULE_TAG 
	pushImage2Registry flowengine $MODULE_TAG 
	pushImage2Registry devicesimulator $MODULE_TAG 
	pushImage2Registry digitaltwin $MODULE_TAG
	pushImage2Registry dashboard $MODULE_TAG 
	pushImage2Registry monitoringui $MODULE_TAG 
	pushImage2Registry configinit $MODULE_TAG 
	pushImage2Registry cacheservice $MODULE_TAG
	pushImage2Registry router $MODULE_TAG	
	pushImage2Registry oauthserver $MODULE_TAG
	pushImage2Registry rtdbmaintainer $MODULE_TAG	
	pushImage2Registry rules-engine $MODULE_TAG

fi

exit 0
