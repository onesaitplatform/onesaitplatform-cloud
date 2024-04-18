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

buildBaseImage()
{
	echo "Platform Base JRE image generation with Docker CLI: "
	docker build -t $USERNAME/baseimage:$1 .
}

buildImageSB2()
{
	echo "Docker image generation for onesaitplatform module: "$2
	cd $1/docker
	cp $1/target/*.jar $1/docker/
	docker build -t $USERNAME/$2:$3 .
	rm $1/docker/*.jar
}

buildGravitee(){
	echo "Build Gravitee managament API"
	docker build -t $USERNAME/gravitee-management-api:$1 management/

	echo "Build Gravitee gateway"
	docker build -t $USERNAME/gravitee-gateway:$1 gateway/

	echo "Build Gravitee managament UI"
	docker build -t $USERNAME/gravitee-management-ui:$1 ui/
}

buildEnvoySidecar(){
	echo "Building consul-proxy-sidecar"
	docker build -t $USERNAME/consul-proxy-sidecar:$1 .
}

buildCas(){
	cd $1
	$1/build.sh packageDocker
	cd $1/docker
	cp $1/target/cas.war $1/docker/
	mkdir $1/docker/etc
	cp -rfv $1/etc/* $1/docker/etc
	docker build -t $USERNAME/cas-server:$2 .
	rm $1/docker/*.war
	rm -R $1/docker/etc
}

buildConfigDB()
{
	echo "ConfigDB image generation with Docker CLI: "
	if [ "$PUSH2OCPREGISTRY" = true ]; then
		docker build -t $USERNAME/configdb:$1 -f Dockerfile.ocp .
	else
		docker build -t $USERNAME/configdb:$1 .
	fi
}

buildSchedulerDB()
{
	echo "SchedulerDB image generation with Docker CLI: "
	if [ "$PUSH2OCPREGISTRY" = true ]; then
		docker build -t $USERNAME/schedulerdb:$1 -f Dockerfile.ocp .
	else
		docker build -t $USERNAME/schedulerdb:$1 .
	fi
}

buildRealTimeDB()
{
	echo "RealTimeDB image generation with Docker CLI: "
	docker build -t $USERNAME/realtimedb:$1 .

	echo "RealTimeDB image generation with Docker CLI: - No Auth"
	docker build -t $USERNAME/realtimedb:$1-noauth -f Dockerfile.noauth .
}

buildRealTimeDB40()
{
	echo "RealTimeDB image generation with Docker CLI: "
	docker build -t $USERNAME/realtimedb:$1 .

	echo "RealTimeDB image generation with Docker CLI: - No Auth"
	docker build -t $USERNAME/realtimedb:$1-noauth -f Dockerfile.noauth .
}

buildRealTimeDB36()
{
	echo "RealTimeDB image generation with Docker CLI: "
	docker build -t $USERNAME/realtimedb:$1 .

	echo "RealTimeDB image generation with Docker CLI: - No Auth"
	docker build -t $USERNAME/realtimedb:$1-noauth -f Dockerfile.noauth .
}

buildMongoExpress()
{
	echo "MongoExpress image generation with Docker CLI: "
	docker build -t $USERNAME/mongoexpress:$1 .
}

buildElasticSearchDB()
{
	echo "ElasticSearchDB image generation with Docker CLI: "
	docker build --squash -t $USERNAME/elasticdb:$1 .
}

buildKafka()
{
	echo "KAFKA image generation with Docker CLI: "
	cp $homepath/../../../../sources/libraries/security/kafka-login/target/*.jar .
	docker build --squash -t $USERNAME/kafka-secured:$1 .
	rm onesaitplatform-kafka-login*.jar
}

buildZookeeper()
{
	echo "ZOOKEEPER image generation with Docker CLI: "
	cp $homepath/../../../../sources/libraries/security/kafka-login/target/*.jar .
	docker build --squash -t $USERNAME/zookeeper-secured:$1 .
	rm onesaitplatform-kafka-login*.jar
}

buildBurrow()
{
	echo "Burrow Kafka monitoring image generation with Docker CLI: "
	docker build -t $USERNAME/burrow:$1 .
}

buildKsql()
{
	echo "Kafka server image generation with Docker CLI: "
	docker build -t $USERNAME/ksql-server:$1 .
}

buildZeppelin()
{
	echo "Apache Zeppelin image generation with Docker CLI: "
	docker build --squash -t $USERNAME/notebook:$1 .
}

buildStreamsets()
{
	echo "Streamsets image generation with Docker CLI: "
	docker build --squash -t $USERNAME/streamsets:$1 .
}

buildDashboardExporter()
{
	echo "Dashboard Exporter image generation with Docker CLI: "
	docker build -t $USERNAME/dashboardexporter:$1 .
}

buildChatbot()
{
	echo "Chatbot module example image generation with Docker CLI: "
	cd $1/docker
	cp $1/target/*-exec.jar $1/docker/
	docker build -t $USERNAME/$2:$3 .
	rm $1/docker/*.jar
}

buildRegistryUI()
{
	echo "RegistryUI image generation with Docker CLI: "
	docker build -t $USERNAME/registryui:$1 .
}

buildNginx()
{
	echo "NGINX image generation with Docker CLI: "
	if [ "$PUSH2OCPREGISTRY" = true ]; then
		docker build -t $USERNAME/nginx:$1 -f Dockerfile.ocp .
	else
		docker build -t $USERNAME/nginx:$1 .
	fi
}

buildDynamicLB()
{
	echo "Dynamic Load Balancer image generation with Docker CLI: "
	docker build -t $USERNAME/dynamiclb:$1 .
}
buildLoadBalancer()
{
	echo "Load Balancer image generation with Docker CLI: "
	docker build -t $USERNAME/loadbalancer:$1 .

}

buildInstaller()
{
	echo "Installer image generation with Docker CLI: "
	docker build -t $USERNAME/installer:$1 .
}

buildQuasar()
{
	echo "Quasar image generation with Docker CLI: "
	echo "Step 1: download quasar binary file"
	wget https://github.com/quasar-analytics/quasar/releases/download/v14.2.6-quasar-web/quasar-web-assembly-14.2.6.jar

	echo "Step 2: build quasar image"
	docker build -t $USERNAME/quasar:$1 .

	rm quasar-web-assembly*.jar
}

buildQuasar40()
{
	echo "Quasar 40 image generation with Docker CLI: "

	echo "Step 1: download quasar binary file"
	wget https://github.com/slamdata/quasar/releases/download/v40.0.0/quasar-web-assembly-40.0.0.jar

	echo "Step 2: Downloading quasar Mongo plugin"
    wget https://github.com/slamdata/quasar/releases/download/v40.0.0/quasar-mongodb-internal-assembly-40.0.0.jar

	echo "Step 3: build quasar image"
	docker build -t $USERNAME/quasar:$1 .

	rm quasar*.jar
}

buildQuasar30()
{
	echo "Quasar 30 image generation with Docker CLI: "

	echo "Step 1: download quasar binary file"
	wget https://github.com/slamdata/quasar/releases/download/v30.0.0/quasar-web-assembly-30.0.0.jar

	echo "Step 2: Downloading quasar Mongo plugin"
    wget https://github.com/slamdata/quasar/releases/download/v30.0.0/quasar-mongodb-internal-assembly-30.0.0.jar

	echo "Step 3: build quasar image"
	docker build -t $USERNAME/quasar:$1 .

	rm quasar*.jar
}

buildDataHubBase()
{
	echo "DataHub Base image generation with Docker CLI: "
	docker build -t $USERNAME/jdbc4datahub-baseimage:$1 .
}

buildDataHub()
{
	echo "DataHub image generation with Docker CLI: "
	docker build -t $USERNAME/jdbc4datahub:$1 .
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

	cp -f $homepath/../dockerfiles/nodered/onesait-platform-config.js $homepath/../../../../sources/modules/flow-engine/docker/Flow-Engine-Manager/onesait-platform/nodes/config/onesait-platform-config.js

	if [ $? -eq 0 ]; then
		echo "onesait-platform-config.js file copied...................... [OK]"
	else
		echo "onesait-platform-config.js file copied..................... [KO]"
	fi

	cp -f $homepath/../dockerfiles/nodered/onesait-platform-public-config.js $homepath/../../../../sources/modules/flow-engine/docker/Flow-Engine-Manager/node_modules/@node-red/editor-client/public/config/onesait-platform-config.js

	if [ $? -eq 0 ]; then
		echo "onesait-platform-config.js (public) file copied...................... [OK]"
	else
		echo "onesait-platform-config.js (public) file copied..................... [KO]"
	fi

	cp -f $homepath/../dockerfiles/nodered/child.js $homepath/../../../../sources/modules/flow-engine/docker/Flow-Engine-Manager/child.js

	if [ $? -eq 0 ]; then
		echo "child.js file copied...................... [OK]"
	else
		echo "child.js file copied..................... [KO]"
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

pushImage2ACRRegistry()
{
	if [ "$NO_PROMT" = false ]; then
		echo "¿Deploy "$1 " image to OCP registry y/n: "
		read confirmation
	fi

	if [[ "$confirmation" == "y" || "$NO_PROMT" = true ]]; then
	    if [ "$(docker images -q $USERNAME/$1:$2)" ]; then
			docker tag $USERNAME/$1:$2 solucionesregistry.azurecr.io/$USERNAME/$1:$2
			docker push solucionesregistry.azurecr.io/$USERNAME/$1:$2
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
echo "# - PUSH2ACRPREGISTRY  -> true -> deploy images to ACR registry                              #"
echo "# - PUSH2PRIVREGISTRY -> true -> deploy images to private registry                          #"
echo "# - USERNAME -> image name convention <username>/<repository>:<tag>                         #"
echo "# -----------------------------------------------------------------                         #"
echo "# - MODULE_<module_name> -> true -> generate image module                                   #"
echo "# - PERSISTENCE_<persistence_name> -> true -> generate persistence image                    #"
echo "# - INFRA_<infra_module_name> -> true -> generate infra image                               #"
echo "# ----------------------------------------------------------------------                    #"
echo "# - MODULE_TAG=<module_tag> -> tag to push image to registry                                #"
echo "# - PERSISTENCE_TAG=<persistence_tag> -> tag to push image to registry                      #"
echo "# - INFRA_TAG=<infra_tag> -> tag to push image to registry                                  #"
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

if [[ "$PLATFORM_BASE_IMAGE" = true && "$(docker images -q $USERNAME/baseimage 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/platform-base-image
	buildBaseImage $BASEIMAGE_TAG
fi

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

if [[ "$MODULE_FLOWENGINE" = true && "$(docker images -q $USERNAME/flowengine 2> /dev/null)" == "" ]]; then
	prepareNodeRED

	buildImage $homepath/../../../../sources/modules/flow-engine flowengine $MODULE_TAG

	removeNodeRED
fi

if [[ "$MODULE_CONFIGINIT" = true && "$(docker images -q $USERNAME/configinit 2> /dev/null)" == "" ]]; then
	prepareConfigInitExamples $homepath/../../../../sources/modules/config-init

	buildImage $homepath/../../../../sources/modules/config-init configinit $MODULE_TAG

	removeConfigInitExamples $homepath/../../../../sources/modules/config-init
fi

if [[ "$MODULE_CHATBOT" = true && "$(docker images -q $USERNAME/chatbot 2> /dev/null)" == "" ]]; then
    buildChatbot $homepath/../../../../sources/examples/chatbot chatbot $MODULE_TAG
fi

if [[ "$MODULE_VIDEOBROKER" = true && "$(docker images -q $USERNAME/videobroker 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/video-broker videobroker $MODULE_TAG
fi

if [[ "$MODULE_INSTALLER" = true && "$(docker images -q $USERNAME/installer 2> /dev/null)" == "" ]]; then
    cd $homepath/../../instalation-resources/installer
	buildInstaller $MODULE_TAG
fi

if [[ "$MODULE_GATEWAY" = true && "$(docker images -q $USERNAME/microservices-gateway 2> /dev/null)" == "" ]]; then
	buildImageSB2 $homepath/../../../../sources/modules/microservices-gateway microservices-gateway $MODULE_TAG
fi

if [[ "$MODULE_RULESENGINE" = true && "$(docker images -q $USERNAME/rules-engine 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/rules-engine rules-engine $MODULE_TAG
fi

if [[ "$MODULE_BPM_ENGINE" = true && "$(docker images -q $USERNAME/bpm-engine 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/bpm-engine bpm-engine $MODULE_TAG
fi

if [[ "$MODULE_REST_PLANNER" = true && "$(docker images -q $USERNAME/rest-planner 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/rest-planner rest-planner $MODULE_TAG
fi

if [[ "$MODULE_REPORT_ENGINE" = true && "$(docker images -q $USERNAME/report-engine 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/report-engine report-engine $MODULE_TAG
fi

#####################################################
# Persistence image generation
#####################################################

if [[ "$PERSISTENCE_CONFIGDB" = true && "$(docker images -q $USERNAME/configdb 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/configdb
	buildConfigDB $PERSISTENCE_TAG
fi

if [[ "$PERSISTENCE_SCHEDULERDB" = true && "$(docker images -q $USERNAME/schedulerdb 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/schedulerdb
	buildSchedulerDB $PERSISTENCE_TAG
fi

if [[ "$PERSISTENCE_REALTIMEDB" = true && "$(docker images -q $USERNAME/realtimedb 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/realtimedb
	buildRealTimeDB $PERSISTENCE_TAG
fi

if [[ "$PERSISTENCE_REALTIMEDB_40" = true && "$(docker images -q $USERNAME/realtimedb:40 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/realtimedb40
	buildRealTimeDB40 $PERSISTENCE_TAG
fi

if [[ "$PERSISTENCE_REALTIMEDB_36" = true && "$(docker images -q $USERNAME/realtimedb:36 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/realtimedb36
	buildRealTimeDB36 $PERSISTENCE_TAG
fi

if [[ "$PERSISTENCE_ELASTICDB" = true && "$(docker images -q $USERNAME/elasticdb 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/elasticsearch
	buildElasticSearchDB $PERSISTENCE_TAG
fi

if [[ "$PERSISTENCE_KAFKA" = true && "$(docker images -q $USERNAME/kafka-secured 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/kafka-cluster/kafka
	buildKafka $PERSISTENCE_TAG
fi

if [[ "$PERSISTENCE_ZOOKEEPER" = true && "$(docker images -q $USERNAME/zookeeper-secured 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/kafka-cluster/zookeeper
	buildZookeeper $PERSISTENCE_TAG
fi

if [[ "$PERSISTENCE_KSQL" = true && "$(docker images -q $USERNAME/ksql-server 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/kafka-cluster/ksql-server
	buildKsql $PERSISTENCE_TAG
fi

#####################################################
# Infrastructure image generation
#####################################################

if [[ "$INFRA_NGINX" = true && "$(docker images -q $USERNAME/nginx 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/nginx
	buildNginx $INFRA_TAG
fi
if [[ "$INFRA_LB" = true && "$(docker images -q $USERNAME/loadbalancer 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/loadbalancer
	buildLoadBalancer $INFRA_TAG
fi


if [[ "$INFRA_DYNAMIC_LB" = true && "$(docker images -q $USERNAME/dynamiclb 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/dynamic-lb
	buildDynamicLB $INFRA_TAG
fi

if [[ "$INFRA_QUASAR" = true && "$(docker images -q $USERNAME/quasar 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/quasar
	buildQuasar $INFRA_TAG
fi

if [[ "$INFRA_QUASAR_40" = true && "$(docker images -q $USERNAME/quasar:40 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/quasar40
	buildQuasar40 40
fi

if [[ "$INFRA_QUASAR_30" = true && "$(docker images -q $USERNAME/quasar:30 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/quasar30
	buildQuasar30 30
fi

if [[ "$INFRA_MONGOEXPRESS" = true && "$(docker images -q $USERNAME/mongoexpress 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/mongoexpress
	buildMongoExpress $INFRA_TAG
fi

if [[ "$INFRA_ZEPPELIN" = true && "$(docker images -q $USERNAME/notebook 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/zeppelin
	buildZeppelin $INFRA_TAG
fi

if [[ "$INFRA_ZEPPELINPY3" = true && "$(docker images -q $USERNAME/notebook 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/zeppelin-py3
	buildZeppelin $INFRA_TAG
fi

if [[ "$INFRA_STREAMSETS33" = true && "$(docker images -q $USERNAME/streamsets 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/streamsets33
	buildStreamsets $INFRA_TAG
fi

if [[ "$INFRA_STREAMSETS38" = true && "$(docker images -q $USERNAME/streamsets 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/streamsets38
	buildStreamsets $INFRA_TAG
fi

if [[ "$INFRA_STREAMSETS310" = true && "$(docker images -q $USERNAME/streamsets 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/streamsets310
	buildStreamsets $INFRA_TAG
fi

if [[ "$INFRA_STREAMSETS313" = true && "$(docker images -q $USERNAME/streamsets 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/streamsets313
	buildStreamsets $INFRA_TAG
fi

if [[ "$INFRA_DASHBOARDEXPORTER" = true && "$(docker images -q $USERNAME/dashboardexporter 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/dashboardexporter
	buildDashboardExporter $INFRA_TAG
fi

if [[ "$INFRA_REGISTRYUI" = true && "$(docker images -q $USERNAME/registryui 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/registry-ui
	buildRegistryUI $INFRA_TAG
fi

if [[ "$INFRA_BURROW" = true && "$(docker images -q $USERNAME/burrow 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/kafka-cluster/burrow
	buildBurrow $INFRA_TAG
fi

if [[ "$INFRA_GRAVITEE" = true && "$(docker images -q $USERNAME/gravitee-* 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/gravitee
	buildGravitee $INFRA_TAG
fi

if [[ "$INFRA_SIDECAR" = true && "$(docker images -q $USERNAME/consul-proxy* 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/consul
	buildEnvoySidecar $INFRA_TAG
fi

if [[ "$INFRA_CAS" = true  ]]; then
	buildCas $homepath/../../../../tools/cas-overlay-template-5.2 $INFRA_TAG
fi

if [[ "$INFRA_JDBC4DATAHUBBASE" = true && "$(docker images -q $USERNAME/jdbc4datahub-baseimage 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/jdbc4datahub-base-image
	buildDataHubBase $INFRA_TAG
fi

if [[ "$INFRA_JDBC4DATAHUBMYSQL8" = true && "$(docker images -q $USERNAME/jdbc4datahub 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/jdbc4datahub-mysql8
	buildDataHub $INFRA_TAG
fi

if [[ "$INFRA_JDBC4DATAHUBBIGQUERY" = true && "$(docker images -q $USERNAME/jdbc4datahub 2> /dev/null)" == "" ]]; then
	cd $homepath/../dockerfiles/jdbc4datahub-bigquery
	buildDataHub $INFRA_TAG
fi

echo "Docker images successfully generated!"

if [ "$PUSH2ACRREGISTRY" = true ]; then
	echo "Pushing images to ACR registry..."

	pushImage2ACRRegistry configdb $PERSISTENCE_TAG
	pushImage2ACRRegistry schedulerdb $PERSISTENCE_TAG
	pushImage2ACRRegistry realtimedb $PERSISTENCE_TAG
	pushImage2ACRRegistry realtimedb $PERSISTENCE_TAG-noauth
	pushImage2ACRRegistry elasticdb $PERSISTENCE_TAG
	pushImage2ACRRegistry zookeeper-secured $PERSISTENCE_TAG
	pushImage2ACRRegistry kafka-secured $PERSISTENCE_TAG
	pushImage2ACRRegistry ksql-server $PERSISTENCE_TAG

	pushImage2ACRRegistry controlpanel $MODULE_TAG
	pushImage2ACRRegistry iotbroker $MODULE_TAG
	pushImage2ACRRegistry apimanager $MODULE_TAG
	pushImage2ACRRegistry flowengine $MODULE_TAG
	pushImage2ACRRegistry devicesimulator $MODULE_TAG
	pushImage2ACRRegistry digitaltwin $MODULE_TAG
	pushImage2ACRRegistry dashboard $MODULE_TAG
	pushImage2ACRRegistry monitoringui $MODULE_TAG
	pushImage2ACRRegistry configinit $MODULE_TAG
	pushImage2ACRRegistry scalability $MODULE_TAG
	pushImage2ACRRegistry chatbot $MODULE_TAG
	pushImage2ACRRegistry cacheservice $MODULE_TAG
	pushImage2ACRRegistry router $MODULE_TAG
	pushImage2ACRRegistry oauthserver $MODULE_TAG
	pushImage2ACRRegistry rtdbmaintainer $MODULE_TAG
	pushImage2ACRRegistry videobroker $MODULE_TAG
	pushImage2ACRRegistry installer $MODULE_TAG
	pushImage2ACRRegistry microservices-gateway $MODULE_TAG
	pushImage2ACRRegistry rules-engine $MODULE_TAG
	pushImage2ACRRegistry bpm-engine $MODULE_TAG
	pushImage2ACRRegistry rest-planner $MODULE_TAG
	pushImage2ACRRegistry report-engine $MODULE_TAG
	pushImage2ACRRegistry nginx $INFRA_TAG
	pushImage2ACRRegistry quasar $INFRA_TAG
	pushImage2ACRRegistry quasar 40
	pushImage2ACRRegistry quasar 30
	pushImage2ACRRegistry notebook $INFRA_TAG
	pushImage2ACRRegistry mongoexpress $INFRA_TAG
	pushImage2ACRRegistry streamsets $INFRA_TAG
	pushImage2ACRRegistry dashboardexporter $INFRA_TAG
	pushImage2ACRRegistry registryui $INFRA_TAG
	pushImage2ACRRegistry burrow $INFRA_TAG
	pushImage2ACRRegistry gravitee-management-api $INFRA_TAG
	pushImage2ACRRegistry gravitee-management-ui $INFRA_TAG
	pushImage2ACRRegistry gravitee-gateway $INFRA_TAG
	pushImage2ACRRegistry consul-proxy-sidecar $INFRA_TAG
fi

if [ "$PUSH2DOCKERHUBREGISTRY" = true ]; then
    echo "Pushing images to private registry"

	pushImage2Registry configdb $PERSISTENCE_TAG
	pushImage2Registry schedulerdb $PERSISTENCE_TAG
	pushImage2Registry realtimedb $PERSISTENCE_TAG
	pushImage2Registry realtimedb $PERSISTENCE_TAG-noauth
	pushImage2Registry elasticdb $PERSISTENCE_TAG
	pushImage2Registry zookeeper-secured $PERSISTENCE_TAG
	pushImage2Registry kafka-secured $PERSISTENCE_TAG
	pushImage2Registry ksql-server $PERSISTENCE_TAG

	pushImage2Registry controlpanel $MODULE_TAG
	pushImage2Registry iotbroker $MODULE_TAG
	pushImage2Registry apimanager $MODULE_TAG
	pushImage2Registry flowengine $MODULE_TAG
	pushImage2Registry devicesimulator $MODULE_TAG
	pushImage2Registry digitaltwin $MODULE_TAG
	pushImage2Registry dashboard $MODULE_TAG
	pushImage2Registry monitoringui $MODULE_TAG
	pushImage2Registry configinit $MODULE_TAG
	pushImage2Registry scalability $MODULE_TAG
	pushImage2Registry chatbot $MODULE_TAG
	pushImage2Registry cacheservice $MODULE_TAG
	pushImage2Registry router $MODULE_TAG
	pushImage2Registry oauthserver $MODULE_TAG
	pushImage2Registry rtdbmaintainer $MODULE_TAG
	pushImage2Registry videobroker $MODULE_TAG
	pushImage2Registry installer $MODULE_TAG
	pushImage2Registry microservices-gateway $MODULE_TAG
	pushImage2Registry rules-engine $MODULE_TAG
	pushImage2Registry bpm-engine $MODULE_TAG
	pushImage2Registry rest-planner $MODULE_TAG
	pushImage2Registry report-engine $MODULE_TAG

	pushImage2Registry baseimage $BASEIMAGE_TAG

	pushImage2Registry nginx $INFRA_TAG
	pushImage2Registry dynamiclb $INFRA_TAG
	pushImage2Registry quasar $INFRA_TAG
	pushImage2Registry quasar 40
	pushImage2Registry quasar 30
	pushImage2Registry notebook $INFRA_TAG
	pushImage2Registry mongoexpress $INFRA_TAG
	pushImage2Registry streamsets $INFRA_TAG
	pushImage2Registry dashboardexporter $INFRA_TAG
	pushImage2Registry registryui $INFRA_TAG
	pushImage2Registry burrow $INFRA_TAG
	pushImage2Registry gravitee-management-api $INFRA_TAG
	pushImage2Registry gravitee-management-ui $INFRA_TAG
	pushImage2Registry gravitee-gateway $INFRA_TAG
	pushImage2Registry cas-server $INFRA_TAG
	pushImage2Registry consul-proxy-sidecar $INFRA_TAG
	pushImage2Registry jdbc4datahub-baseimage $INFRA_TAG
	pushImage2Registry jdbc4datahub $INFRA_TAG
fi

if [ "$PUSH2PRIVREGISTRY" = true ]; then
    echo "Pushing images to private registry"

	pushImage2Registry configdb $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry schedulerdb $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry realtimedb $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry realtimedb $PERSISTENCE_TAG-noauth $PRIVATE_REGISTRY/
	pushImage2Registry elasticdb $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry zookeeper-secured $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry kafka-secured $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry ksql-server $PERSISTENCE_TAG $PRIVATE_REGISTRY/

	pushImage2Registry controlpanel $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry iotbroker $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry apimanager $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry flowengine $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry devicesimulator $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry digitaltwin $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry dashboard $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry monitoringui $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry configinit $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry scalability $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry chatbot $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry cacheservice $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry router $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry oauthserver $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry rtdbmaintainer $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry videobroker $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry installer $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry microservices-gateway $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry rules-engine $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry bpm-engine $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry rest-planner $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry report-engine $MODULE_TAG $PRIVATE_REGISTRY/

	pushImage2Registry baseimage $BASEIMAGE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry loadbalancer $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry nginx $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry dynamiclb $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry quasar $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry quasar 40 $PRIVATE_REGISTRY/
	pushImage2Registry quasar 30 $PRIVATE_REGISTRY/
	pushImage2Registry notebook $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry mongoexpress $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry streamsets $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry dashboardexporter $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry registryui $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry burrow $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry gravitee-management-api $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry gravitee-management-ui $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry gravitee-gateway $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry cas-server $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry consul-proxy-sidecar $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry jdbc4datahub-baseimage $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry jdbc4datahub $INFRA_TAG $PRIVATE_REGISTRY/
fi

exit 0
