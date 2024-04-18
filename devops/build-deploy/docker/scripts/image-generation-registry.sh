#!/bin/bash

#
# Copyright Indra Sistemas, S.A.
# 2013-2022 SPAIN
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
	docker build --network host -t $USERNAME/$2:$3 .
	rm $1/docker/*.jar
}

buildImage2()
{
   cd $1
   docker build --network host -t $USERNAME/$2:$3 -f $4 .
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

	cp -f $homepath/../dockerfiles/nodered/proxy-nodered.js $homepath/../../../../sources/modules/flow-engine/docker/Flow-Engine-Manager/proxy-nodered.js

	 if [ $? -eq 0 ]; then
		echo "proxy-nodered.js file copied...................... [OK]"
	 else
		echo "proxy-nodered.js file copied..................... [KO]"
	fi

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

	cp -f $homepath/../dockerfiles/nodered/app.js $homepath/../../../../sources/modules/flow-engine/docker/Flow-Engine-Manager/app.js

	if [ $? -eq 0 ]; then
		echo "app.js file copied...................... [OK]"
	else
		echo "app.js file copied..................... [KO]"
	fi
}

removeNodeRED()
{
	cd $homepath/../../../../sources/modules/flow-engine/docker
	rm -rf Flow-Engine-Manager
	rm nodered.zip
}

prepareAnalyticsEngineSparkFiles(){

	echo "Copying Spark files "
	cd $homepath/../../../../sources/modules/analytics-engine-launcher-manager/docker
   cp -r /tmp/platformdata/jars .
}

removeAnalyticsEngineSparkFiles()
{
	cd $homepath/../../../../sources/modules/analytics-engine-launcher-manager/docker
	rm -rf jars
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

pushImage2GCPRegistry()
{
	if [ "$NO_PROMT" = false ]; then
		echo "¿Deploy "$1 " image to GCP registry y/n: "
		read confirmation
	fi

	if [[ "$confirmation" == "y" || "$NO_PROMT" = true ]]; then
	    if [ "$(docker images -q $USERNAME/$1:$2)" ]; then
			docker tag $USERNAME/$1:$2 europe-west1-docker.pkg.dev/dcme-npro-onst-snbx-osp-dev-00/platformregistry/$USERNAME/$1:$2
			docker push europe-west1-docker.pkg.dev/dcme-npro-onst-snbx-osp-dev-00/platformregistry/$USERNAME/$1:$2
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
   echo "Platform Base JRE image generation with Docker CLI: "   
   buildImage2 $homepath/../dockerfiles/platform-base-image baseimage $BASEIMAGE_TAG Dockerfile
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

if [[ "$MODULE_AUDIT_ROUTER" = true && "$(docker images -q $USERNAME/audit-router 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/audit-router audit-router $MODULE_TAG
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

if [[ "$MODULE_GATEWAY" = true && "$(docker images -q $USERNAME/microservices-gateway 2> /dev/null)" == "" ]]; then
   echo "Docker image generation for onesaitplatform module: microservices-gateway"
	cd $homepath/../../../../sources/modules/microservices-gateway/docker
	cp $homepath/../../../../sources/modules/microservices-gateway/target/*.jar $homepath/../../../../sources/modules/microservices-gateway/docker/
   buildImage2 $homepath/../../../../sources/modules/microservices-gateway microservices-gateway $MODULE_TAG Dockerfile
	rm $homepath/../../../../sources/modules/microservices-gateway/docker/*.jar

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

if [[ "$MODULE_KEYCLOAK_MANAGER" = true && "$(docker images -q $USERNAME/microservices-gateway 2> /dev/null)" == "" ]]; then
   echo "Docker image generation for onesaitplatform module: keycloak-manager"
	cd $homepath/../../../../tools/keycloak/onesaitplatform-keycloak-manager/docker
	cp $homepath/../../../../tools/keycloak/onesaitplatform-keycloak-manager/target/*.jar $homepath/../../../../tools/keycloak/onesaitplatform-keycloak-manager/docker/
   buildImage2 $homepath/../../../../tools/keycloak/onesaitplatform-keycloak-manager keycloak-manager $MODULE_TAG Dockerfile
	rm $homepath/../../../../tools/keycloak/onesaitplatform-keycloak-manager/docker/*.ja
fi

if [[ "$MODULE_SERVERLESS_MANAGER" = true && "$(docker images -q $USERNAME/serverless-manager 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/serverless-manager serverless-manager $MODULE_TAG
fi

if [[ "$MODULE_ANALYTICS_ENGINE_LAUNCHER_MANAGER" = true && "$(docker images -q $USERNAME/analytics-engine-launcher-manager 2> /dev/null)" == "" ]]; then
   prepareAnalyticsEngineSparkFiles
   buildImage $homepath/../../../../sources/modules/analytics-engine-launcher-manager analytics-engine-launcher-manager $MODULE_TAG
   removeAnalyticsEngineSparkFiles
fi

if [[ "$MODULE_PLUGIN_MANAGER" = true && "$(docker images -q $USERNAME/plugin-manager 2> /dev/null)" == "" ]]; then
	buildImage $homepath/../../../../sources/modules/plugin-manager plugin-manager $MODULE_TAG
fi

#####################################################
# Persistence image generation
#####################################################

if [[ "$PERSISTENCE_CONFIGDB" = true && "$(docker images -q $USERNAME/configdb 2> /dev/null)" == "" ]]; then
   echo "ConfigDB image generation with Docker CLI: "
   if [ "$PUSH2OCPREGISTRY" = true ]; then
      buildImage2 $homepath/../dockerfiles/configdb configdb $PERSISTENCE_TAG Dockerfile.ocp
	else
      buildImage2 $homepath/../dockerfiles/configdb configdb $PERSISTENCE_TAG Dockerfile
	fi
fi

if [[ "$PERSISTENCE_CONFIGDB_MYSQL" = true && "$(docker images -q $USERNAME/configdb 2> /dev/null)" == "" ]]; then
   echo "ConfigDB image generation with Docker CLI: "
	if [ "$PERSISTENCE_CONFIGDB_MYSQL" = true ]; then
      buildImage2 $homepath/../dockerfiles/configdb configdb $PERSISTENCE_TAG Dockerfile.mysql
	fi
fi

if [[ "$PERSISTENCE_CONFIGDB_POSTGRESQL" = true && "$(docker images -q $USERNAME/configdb 2> /dev/null)" == "" ]]; then
   echo "ConfigDB image generation with Docker CLI: "
   if [ "$PERSISTENCE_CONFIGDB_POSTGRESQL" = true ]; then
      buildImage2 $homepath/../dockerfiles/configdb configdb $PERSISTENCE_TAG Dockerfile.postgres
	fi
fi

if [[ "$PERSISTENCE_REALTIMEDB" = true && "$(docker images -q $USERNAME/realtimedb 2> /dev/null)" == "" ]]; then
   echo "RealTimeDB image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/realtimedb realtimedb $PERSISTENCE_TAG Dockerfile
   echo "RealTimeDB image generation with Docker CLI: - No Auth"
   buildImage2 $homepath/../dockerfiles/realtimedb realtimedb $PERSISTENCE_TAG-noauth Dockerfile.noauth
fi

if [[ "$PERSISTENCE_REALTIMEDB_40" = true && "$(docker images -q $USERNAME/realtimedb:40 2> /dev/null)" == "" ]]; then
   echo "RealTimeDB image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/realtimedb40 realtimedb $PERSISTENCE_TAG Dockerfile
   echo "RealTimeDB image generation with Docker CLI: - No Auth"
   buildImage2 $homepath/../dockerfiles/realtimedb40 realtimedb $PERSISTENCE_TAG-noauth Dockerfile.noauth
fi

if [[ "$PERSISTENCE_REALTIMEDB_44" = true && "$(docker images -q $USERNAME/realtimedb:4.4 2> /dev/null)" == "" ]]; then
   echo "RealTimeDB image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/realtimedb44 realtimedb $PERSISTENCE_TAG Dockerfile
   echo "RealTimeDB image generation with Docker CLI: - No Auth"
   buildImage2 $homepath/../dockerfiles/realtimedb44 realtimedb $PERSISTENCE_TAG-noauth Dockerfile.noauth
fi

if [[ "$PERSISTENCE_REALTIMEDB_50" = true && "$(docker images -q $USERNAME/realtimedb:5.0 2> /dev/null)" == "" ]]; then
   echo "RealTimeDB image generation with Docker CLI: "
	buildImage2 $homepath/../dockerfiles/realtimedb50 realtimedb $PERSISTENCE_TAG Dockerfile
   echo "RealTimeDB image generation with Docker CLI: - No Auth"
   buildImage2 $homepath/../dockerfiles/realtimedb50 realtimedb $PERSISTENCE_TAG-noauth Dockerfile.noauth
fi

if [[ "$PERSISTENCE_REALTIMEDB_60" = true && "$(docker images -q $USERNAME/realtimedb:6.0 2> /dev/null)" == "" ]]; then
   echo "RealTimeDB image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/realtimedb60 realtimedb $PERSISTENCE_TAG Dockerfile
   echo "RealTimeDB image generation with Docker CLI: - No Auth"
   buildImage2 $homepath/../dockerfiles/realtimedb60 realtimedb $PERSISTENCE_TAG-noauth Dockerfile.noauth
fi

if [[ "$PERSISTENCE_ELASTICDB" = true && "$(docker images -q $USERNAME/elasticdb 2> /dev/null)" == "" ]]; then
   echo "ElasticSearchDB image generation with Docker CLI: "
	buildImage2 $homepath/../dockerfiles/elasticsearch elasticdb $PERSISTENCE_TAG Dockerfile
fi

if [[ "$PERSISTENCE_AUDITOPDISTRO" = true && "$(docker images -q $USERNAME/auditdb 2> /dev/null)" == "" ]]; then
   echo "Audit database image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/opendistro auditdb $PERSISTENCE_TAG Dockerfile
fi

if [[ "$PERSISTENCE_OPENSEARCH" = true && "$(docker images -q $USERNAME/auditdb 2> /dev/null)" == "" ]]; then
   echo "Open Search database image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/opensearch auditdb $PERSISTENCE_TAG Dockerfile
fi

if [[ "$PERSISTENCE_KAFKA" = true && "$(docker images -q $USERNAME/kafka-secured 2> /dev/null)" == "" ]]; then
	echo "KAFKA image generation with Docker CLI: "
   cd $homepath/../dockerfiles/kafka-cluster/kafka
   cp $homepath/../../../../sources/libraries/security/kafka-login/target/*.jar .
   buildImage2 $homepath/../dockerfiles/kafka-cluster/kafka kafka-secured $PERSISTENCE_TAG Dockerfile
   rm onesaitplatform-kafka-login*.jar
fi

if [[ "$PERSISTENCE_ZOOKEEPER" = true && "$(docker images -q $USERNAME/zookeeper-secured 2> /dev/null)" == "" ]]; then
   echo "ZOOKEEPER image generation with Docker CLI: "
	cd $homepath/../dockerfiles/kafka-cluster/zookeeper
   cp $homepath/../../../../sources/libraries/security/kafka-login/target/*.jar .
   buildImage2 $homepath/../dockerfiles/kafka-cluster/zookeeper zookeeper-secured $PERSISTENCE_TAG Dockerfile
fi

if [[ "$PERSISTENCE_TIMESCALEDB" = true ]]; then
   echo "TimescaleDB image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/timescaledb timescaledb $PERSISTENCE_TAG Dockerfile
fi

if [[ "$PERSISTENCE_TIMESCALEDB_PG14" = true ]]; then
   echo "TimescaleDB image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/timescaledb-pg14 timescaledb $PERSISTENCE_TAG Dockerfile
fi

if [[ "$PERSISTENCE_ANALYTICS_ENGINE" = true  ]]; then
   echo "Spark image generation with Docker CLI: "
	cd $homepath/../dockerfiles/analytics-engine
   cp -r /tmp/platformdata/jars .
   buildImage2 $homepath/../dockerfiles/analytics-engine analytics-engine $PERSISTENCE_TAG Dockerfile
   rm -fr ./jars
fi

if [[ "$PERSISTENCE_NEBULADB" = true  ]]; then
   buildImage2 $homepath/../dockerfiles/nebula-graph nebula-graph $PERSISTENCE_TAG Dockerfile-graphd
   buildImage2 $homepath/../dockerfiles/nebula-meta nebula-meta $PERSISTENCE_TAG Dockerfile-metad
   buildImage2 $homepath/../dockerfiles/nebula-storage nebula-storage $PERSISTENCE_TAG Dockerfile-storaged
   buildImage2 $homepath/../dockerfiles/nebula-studio nebula-studio $PERSISTENCE_TAG Dockerfile-studio
fi

#####################################################
# Infrastructure image generation
#####################################################

if [[ "$INFRA_NGINX" = true && "$(docker images -q $USERNAME/nginx 2> /dev/null)" == "" ]]; then
   echo "NGINX image generation with Docker CLI: "
	if [ "$PUSH2OCPREGISTRY" = true ]; then
      buildImage2 $homepath/../dockerfiles/nginx nginx $INFRA_TAG Dockerfile.ocp
	else
      buildImage2 $homepath/../dockerfiles/nginx nginx $INFRA_TAG Dockerfile
	fi
fi

if [[ "$INFRA_LB" = true && "$(docker images -q $USERNAME/loadbalancer 2> /dev/null)" == "" ]]; then
   echo "Load Balancer image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/loadbalancer loadbalancer $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_DYNAMIC_LB" = true && "$(docker images -q $USERNAME/dynamiclb 2> /dev/null)" == "" ]]; then
   echo "Dynamic Load Balancer image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/dynamic-lb dynamic-lb $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_ZEPPELIN" = true && "$(docker images -q $USERNAME/notebook 2> /dev/null)" == "" ]]; then
   echo "Apache Zeppelin image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/zeppelin zeppelin $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_ZEPPELINPY3" = true && "$(docker images -q $USERNAME/notebook 2> /dev/null)" == "" ]]; then
   echo "Apache Zeppelin image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/zeppelin-py3 zeppelin $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_STREAMSETS33" = true && "$(docker images -q $USERNAME/streamsets 2> /dev/null)" == "" ]]; then
   echo "Streamsets image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/streamsets33 streamsets $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_STREAMSETS38" = true && "$(docker images -q $USERNAME/streamsets 2> /dev/null)" == "" ]]; then
   echo "Streamsets image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/streamsets38 streamsets $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_STREAMSETS310" = true && "$(docker images -q $USERNAME/streamsets 2> /dev/null)" == "" ]]; then
   echo "Streamsets image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/streamsets310 streamsets $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_STREAMSETS313" = true && "$(docker images -q $USERNAME/streamsets 2> /dev/null)" == "" ]]; then
   echo "Streamsets image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/streamsets313 streamsets $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_STREAMSETS318" = true && "$(docker images -q $USERNAME/streamsets 2> /dev/null)" == "" ]]; then
   echo "Streamsets image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/streamsets318 streamsets $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_STREAMSETS323" = true && "$(docker images -q $USERNAME/streamsets 2> /dev/null)" == "" ]]; then
   echo "Streamsets image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/streamsets323 streamsets $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_STREAMSETS3231" = true && "$(docker images -q $USERNAME/streamsets 2> /dev/null)" == "" ]]; then
   echo "Streamsets image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/streamsets3231 streamsets $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_DASHBOARDEXPORTER" = true && "$(docker images -q $USERNAME/dashboardexporter 2> /dev/null)" == "" ]]; then
   echo "Dashboard Exporter image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/dashboardexporter dashboardexporter $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_BURROW" = true && "$(docker images -q $USERNAME/burrow 2> /dev/null)" == "" ]]; then
   echo "Burrow Kafka monitoring image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/kafka-cluster/burrow burrow $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_GRAVITEE" = true && "$(docker images -q $USERNAME/gravitee-* 2> /dev/null)" == "" ]]; then
   echo "Build Gravitee managament API"
   buildImage2 $homepath/../dockerfiles/gravitee gravitee-management-api $INFRA_TAG managament/Dockerfile
	echo "Build Gravitee gateway"
   buildImage2 $homepath/../dockerfiles/gravitee gravitee-gateway $INFRA_TAG gateway/Dockerfile
	echo "Build Gravitee managament UI"
   buildImage2 $homepath/../dockerfiles/gravitee gravitee-management-ui $INFRA_TAG ui/Dockerfile
fi

if [[ "$INFRA_SIDECAR" = true && "$(docker images -q $USERNAME/consul-proxy* 2> /dev/null)" == "" ]]; then
   echo "Building consul-proxy-sidecar"
   buildImage2 $homepath/../dockerfiles/consul consul-proxy-sidecar $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_CAS" = true  ]]; then
	buildCas $homepath/../../../../tools/cas-overlay-template-5.2 $INFRA_TAG
   
   cd $homepath/../../../../tools/cas-overlay-template-5.2
	$homepath/../../../../tools/cas-overlay-template-5.2/build.sh packageDocker
	cd $homepath/../../../../tools/cas-overlay-template-5.2/docker
	cp $homepath/../../../../tools/cas-overlay-template-5.2/target/cas.war $1/docker/
	mkdir $homepath/../../../../tools/cas-overlay-template-5.2/docker/etc
	cp -rfv $homepath/../../../../tools/cas-overlay-template-5.2/etc/* $homepath/../../../../tools/cas-overlay-template-5.2/docker/etc
   buildImage2 $homepath/../../../../tools/cas-overlay-template-5.2 cas-server $INFRA_TAG Dockerfile
	rm $homepath/../../../../tools/cas-overlay-template-5.2/docker/*.war
	rm -R $homepath/../../../../tools/cas-overlay-template-5.2/docker/etc
fi

if [[ "$INFRA_JDBC4DATAHUBBASE" = true && "$(docker images -q $USERNAME/jdbc4datahub-baseimage 2> /dev/null)" == "" ]]; then
   echo "DataHub Base image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/jdbc4datahub-base-image jdbc4datahub-baseimage $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_JDBC4DATAHUBMYSQL8" = true && "$(docker images -q $USERNAME/jdbc4datahub 2> /dev/null)" == "" ]]; then
   echo "DataHub image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/jdbc4datahub-mysql8 jdbc4datahub $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_JDBC4DATAHUBBIGQUERY" = true && "$(docker images -q $USERNAME/jdbc4datahub 2> /dev/null)" == "" ]]; then
   echo "DataHub image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/jdbc4datahub-bigquery jdbc4datahub $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_DATACLEANER" = true && "$(docker images -q $USERNAME/data-cleaner 2> /dev/null)" == "" ]]; then
   echo "Copying OpenRefine sources to target directory"
   cp -r $homepath/../../../../tools/OpenRefine/3.4 $homepath/../../../../devops/build-deploy/docker/dockerfiles/data-cleaner/OpenRefine34
	echo "DataCleaner image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/data-cleaner data-cleaner $INFRA_TAG Dockerfile
	echo "Cleaning OpenRefine sources"
	rm -rf $homepath/../../../../devops/build-deploy/docker/dockerfiles/data-cleaner/OpenRefine34
fi

if [[ "$INFRA_DATACLEANER372" = true && "$(docker images -q $USERNAME/data-cleaner 2> /dev/null)" == "" ]]; then
   echo "Copying OpenRefine sources to target directory"
   cp -r $homepath/../../../../tools/OpenRefine/3.7.2 $homepath/../../../../devops/build-deploy/docker/dockerfiles/data-cleaner-372/OpenRefine372
	echo "DataCleaner image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/data-cleaner-372 data-cleaner $INFRA_TAG Dockerfile
	echo "Cleaning OpenRefine sources"
	rm -rf $homepath/../../../../devops/build-deploy/docker/dockerfiles/data-cleaner-372/OpenRefine372
fi

if [[ "$INFRA_LOGCENTRALIZER" = true && "$(docker images -q $USERNAME/data-cleaner 2> /dev/null)" == "" ]]; then
   echo "Log Centralizer image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/log-centralizer log-centralizer $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_LOGCENTRALIZER_OCP" = true && "$(docker images -q $USERNAME/data-cleaner 2> /dev/null)" == "" ]]; then
   echo "Log Centralizer OCP image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/log-centralizer log-centralizer $INFRA_TAG Dockerfile.ocp
fi

if [[ "$INFRA_TELEGRAF" = true && "$(docker images -q $USERNAME/agent-metric-collector 2> /dev/null)" == "" ]]; then
   echo "Docker image generation for telegraf"
   buildImage2 $homepath/../dockerfiles/telegraf-streamsets agent-metric-collector $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_KEYCLOAK" = true  ]]; then
   echo "Building default MariaDB image"
	cd $homepath/../../../../tools/keycloak/onesaitplatform-keycloak-storage-provider
	cp onesaitplatform-keycloak-storage-provider.jar $homepath/../../../../tools/keycloak/server/
	cd $homepath/../../../../tools/keycloak/server
	docker build -t $USERNAME/keycloak:$INFRA_TAG .
	rm onesaitplatform-keycloak-storage-provider.jar
	cd $homepath/../../../../tools/keycloak/onesaitplatform-keycloak-storage-provider
	rm onesaitplatform-keycloak-storage-provider.jar
	echo "Building PostgreSQL image"
	cp onesaitplatform-keycloak-storage-provider-psql.jar $homepath/../../../../tools/keycloak/server/
	cd $$homepath/../../../../tools/keycloak/server
   buildImage2 $homepath/../../../../tools/keycloak keycloak $INFRA_TAG-postgres Dockerfile
	rm onesaitplatform-keycloak-storage-provider-psql.jar
	cd $homepath/../../../../tools/keycloak/onesaitplatform-keycloak-storage-provider
	rm onesaitplatform-keycloak-storage-provider-psql.jar
fi

if [[ "$INFRA_PRESTO" = true  ]]; then
   cd $homepath/../dockerfiles/presto/presto-server
   buildImage2 $homepath/../dockerfiles/presto presto-server $INFRA_TAG Dockerfile
	cd $homepath/../dockerfiles/presto/presto-metastore-server
   buildImage2 $homepath/../dockerfiles/presto presto-metastore-server $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_MLFLOW" = true && "$(docker images -q $USERNAME/modelsmanager 2> /dev/null)" == "" ]]; then
   echo "MLFlow image generation with Docker CLI: "
   buildImage2 $homepath/../dockerfiles/mlflow modelsmanager $INFRA_TAG Dockerfile
fi

if [[ "$INFRA_DATALABELING" = true && "$(docker images -q $USERNAME/modelsmanager 2> /dev/null)" == "" ]]; then
   echo "Copying DataLabeling sources to target directory"
   cp -r $homepath/../../../../tools/label-studio $homepath/../../../../devops/build-deploy/docker/dockerfiles/datalabeling/label-studio
   echo "Copying Dokerfile into label-studio directory"
   cp  $homepath/../../../../devops/build-deploy/docker/dockerfiles/datalabeling/Dockerfile $homepath/../../../../devops/build-deploy/docker/dockerfiles/datalabeling/label-studio/
   echo "DataLabeling image generation with Docker CLI: "
   cd $homepath/../dockerfiles/datalabeling/label-studio/
   buildImage2 $homepath/../dockerfiles/datalabeling datalabeling $INFRA_TAG Dockerfile
   cd $homepath/../dockerfiles/datalabeling
   echo "Cleaning DataLabeling sources"
   rm -rf $homepath/../../../../devops/build-deploy/docker/dockerfiles/datalabeling/label-studio
fi

echo "Docker images successfully generated!"

if [ "$PUSH2GCPREGISTRY" = true ]; then
	echo "Pushing images to GCP registry..."

	pushImage2GCPRegistry configdb $PERSISTENCE_TAG
	pushImage2GCPRegistry realtimedb $PERSISTENCE_TAG
	pushImage2GCPRegistry realtimedb $PERSISTENCE_TAG-noauth
	pushImage2GCPRegistry elasticdb $PERSISTENCE_TAG
	pushImage2GCPRegistry auditdb $PERSISTENCE_TAG
	pushImage2GCPRegistry opensearch $PERSISTENCE_TAG
	pushImage2GCPRegistry zookeeper-secured $PERSISTENCE_TAG
	pushImage2GCPRegistry kafka-secured $PERSISTENCE_TAG
	pushImage2GCPRegistry timescaledb $PERSISTENCE_TAG
	pushImage2GCPRegistry analytics-engine $PERSISTENCE_TAG
	pushImage2GCPRegistry nebula-graph $PERSISTENCE_TAG
	pushImage2GCPRegistry nebula-meta $PERSISTENCE_TAG
	pushImage2GCPRegistry nebula-storage $PERSISTENCE_TAG
	pushImage2GCPRegistry nebula-studio $PERSISTENCE_TAG

	pushImage2GCPRegistry controlpanel $MODULE_TAG
	pushImage2GCPRegistry iotbroker $MODULE_TAG
	pushImage2GCPRegistry apimanager $MODULE_TAG
	pushImage2GCPRegistry flowengine $MODULE_TAG
	pushImage2GCPRegistry devicesimulator $MODULE_TAG
	pushImage2GCPRegistry digitaltwin $MODULE_TAG
	pushImage2GCPRegistry dashboard $MODULE_TAG
	pushImage2GCPRegistry monitoringui $MODULE_TAG
	pushImage2GCPRegistry configinit $MODULE_TAG
	pushImage2GCPRegistry scalability $MODULE_TAG
	pushImage2GCPRegistry cacheservice $MODULE_TAG
	pushImage2GCPRegistry router $MODULE_TAG
	pushImage2GCPRegistry audit-router $MODULE_TAG
	pushImage2GCPRegistry oauthserver $MODULE_TAG
	pushImage2GCPRegistry rtdbmaintainer $MODULE_TAG
	pushImage2GCPRegistry microservices-gateway $MODULE_TAG
	pushImage2GCPRegistry rules-engine $MODULE_TAG
	pushImage2GCPRegistry bpm-engine $MODULE_TAG
	pushImage2GCPRegistry rest-planner $MODULE_TAG
	pushImage2GCPRegistry report-engine $MODULE_TAG
	pushImage2GCPRegistry keycloak-manager $MODULE_TAG
	pushImage2GCPRegistry serverless-manager $MODULE_TAG
	pushImage2GCPRegistry analytics-engine-launcher-manager $MODULE_TAG
	pushImage2GCPRegistry plugin-manager $MODULE_TAG

	pushImage2GCPRegistry baseimage $BASEIMAGE_TAG

	pushImage2GCPRegistry loadbalancer $INFRA_TAG
	pushImage2GCPRegistry nginx $INFRA_TAG
	pushImage2GCPRegistry notebook $INFRA_TAG
	pushImage2GCPRegistry streamsets $INFRA_TAG
	pushImage2GCPRegistry dashboardexporter $INFRA_TAG
	pushImage2GCPRegistry burrow $INFRA_TAG
	pushImage2GCPRegistry gravitee-management-api $INFRA_TAG
	pushImage2GCPRegistry gravitee-management-ui $INFRA_TAG
	pushImage2GCPRegistry gravitee-gateway $INFRA_TAG
	pushImage2GCPRegistry consul-proxy-sidecar $INFRA_TAG
	pushImage2GCPRegistry data-cleaner $INFRA_TAG
	pushImage2GCPRegistry log-centralizer $INFRA_TAG
	pushImage2GCPRegistry keycloak $INFRA_TAG
	pushImage2GCPRegistry keycloak $INFRA_TAG-postgres
	pushImage2GCPRegistry presto-server $INFRA_TAG
	pushImage2GCPRegistry presto-metastore-server $INFRA_TAG
	pushImage2GCPRegistry modelsmanager $INFRA_TAG
	pushImage2GCPRegistry datalabeling $INFRA_TAG
fi

if [ "$PUSH2DOCKERHUBREGISTRY" = true ]; then
    echo "Pushing images to private registry"

	pushImage2Registry configdb $PERSISTENCE_TAG
	pushImage2Registry realtimedb $PERSISTENCE_TAG
	pushImage2Registry realtimedb $PERSISTENCE_TAG-noauth
	pushImage2Registry elasticdb $PERSISTENCE_TAG
	pushImage2Registry auditdb $PERSISTENCE_TAG
	pushImage2Registry opensearch $PERSISTENCE_TAG
	pushImage2Registry zookeeper-secured $PERSISTENCE_TAG
	pushImage2Registry kafka-secured $PERSISTENCE_TAG
	pushImage2Registry timescaledb $PERSISTENCE_TAG
	pushImage2Registry analytics-engine $PERSISTENCE_TAG
	pushImage2Registry nebula-graph $PERSISTENCE_TAG
	pushImage2Registry nebula-meta $PERSISTENCE_TAG
	pushImage2Registry nebula-storage $PERSISTENCE_TAG
	pushImage2Registry nebula-studio $PERSISTENCE_TAG

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
	pushImage2Registry cacheservice $MODULE_TAG
	pushImage2Registry router $MODULE_TAG
	pushImage2Registry audit-router $MODULE_TAG
	pushImage2Registry oauthserver $MODULE_TAG
	pushImage2Registry rtdbmaintainer $MODULE_TAG
	pushImage2Registry microservices-gateway $MODULE_TAG
	pushImage2Registry rules-engine $MODULE_TAG
	pushImage2Registry bpm-engine $MODULE_TAG
	pushImage2Registry rest-planner $MODULE_TAG
	pushImage2Registry report-engine $MODULE_TAG
	pushImage2Registry keycloak-manager $MODULE_TAG
	pushImage2Registry serverless-manager $MODULE_TAG
	pushImage2Registry analytics-engine-launcher-manager $MODULE_TAG
	pushImage2Registry plugin-manager $MODULE_TAG

	pushImage2Registry baseimage $BASEIMAGE_TAG

	pushImage2Registry nginx $INFRA_TAG
	pushImage2Registry dynamiclb $INFRA_TAG
	pushImage2Registry notebook $INFRA_TAG
	pushImage2Registry streamsets $INFRA_TAG
	pushImage2Registry dashboardexporter $INFRA_TAG
	pushImage2Registry burrow $INFRA_TAG
	pushImage2Registry gravitee-management-api $INFRA_TAG
	pushImage2Registry gravitee-management-ui $INFRA_TAG
	pushImage2Registry gravitee-gateway $INFRA_TAG
	pushImage2Registry cas-server $INFRA_TAG
	pushImage2Registry consul-proxy-sidecar $INFRA_TAG
	pushImage2Registry jdbc4datahub-baseimage $INFRA_TAG
	pushImage2Registry jdbc4datahub $INFRA_TAG
	pushImage2Registry data-cleaner $INFRA_TAG
	pushImage2Registry keycloak $INFRA_TAG
	pushImage2Registry keycloak $INFRA_TAG-postgres
	pushImage2Registry presto-server $INFRA_TAG
	pushImage2Registry presto-metastore-server $INFRA_TAG
	pushImage2Registry modelsmanager $INFRA_TAG
	pushImage2Registry datalabeling $INFRA_TAG
fi

if [ "$PUSH2PRIVREGISTRY" = true ]; then
    echo "Pushing images to private registry"

	pushImage2Registry configdb $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry realtimedb $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry realtimedb $PERSISTENCE_TAG-noauth $PRIVATE_REGISTRY/
	pushImage2Registry elasticdb $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry auditdb $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry opensearch $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry zookeeper-secured $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry kafka-secured $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry timescaledb $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry analytics-engine $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry nebula-graph $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry nebula-meta $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry nebula-storage $PERSISTENCE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry nebula-studio $PERSISTENCE_TAG $PRIVATE_REGISTRY/

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
	pushImage2Registry cacheservice $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry router $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry audit-router $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry oauthserver $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry rtdbmaintainer $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry microservices-gateway $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry rules-engine $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry bpm-engine $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry rest-planner $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry report-engine $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry keycloak-manager $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry serverless-manager $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry analytics-engine-launcher-manager $MODULE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry plugin-manager $MODULE_TAG $PRIVATE_REGISTRY/

	pushImage2Registry baseimage $BASEIMAGE_TAG $PRIVATE_REGISTRY/
	pushImage2Registry loadbalancer $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry nginx $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry dynamiclb $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry notebook $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry streamsets $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry dashboardexporter $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry burrow $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry gravitee-management-api $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry gravitee-management-ui $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry gravitee-gateway $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry cas-server $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry consul-proxy-sidecar $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry jdbc4datahub-baseimage $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry jdbc4datahub $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry data-cleaner $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry log-centralizer $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry agent-metric-collector $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry keycloak $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry keycloak $INFRA_TAG-postgres $PRIVATE_REGISTRY/
	pushImage2Registry presto-server $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry presto-metastore-server $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry modelsmanager $INFRA_TAG $PRIVATE_REGISTRY/
	pushImage2Registry datalabeling $INFRA_TAG $PRIVATE_REGISTRY/
fi

exit 0