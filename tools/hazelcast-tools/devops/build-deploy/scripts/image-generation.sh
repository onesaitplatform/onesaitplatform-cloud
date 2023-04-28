#!/bin/bash

#
# Copyright Indra Sistemas, S.A.
# 2019-2020 SPAIN
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
	echo "Docker image generation for Hazelcast modules: "$2
	echo $1
	cp $1/target/*.jar $1/docker/
	docker build -t $GIT_GROUP/$2:$3 .
	rm $1/docker/*.jar
}

pushImage()
{
	docker tag $GIT_GROUP/$1:$2 mps.labs.devops:8443/$GIT_GROUP/$1:$2
	docker push mps.labs.devops:8443/$GIT_GROUP/$1:$2	
}

echo "##########################################################################################"
echo "#                                                                                        #"
echo "#   _____             _                                                                  #"              
echo "#  |  __ \           | |                                                                 #"            
echo "#  | |  | | ___   ___| | _____ _ __                                                      #"
echo "#  | |  | |/ _ \ / __| |/ / _ \ '__|                                                     #"
echo "#  | |__| | (_) | (__|   <  __/ |                                                        #"
echo "#  |_____/ \___/ \___|_|\_\___|_|                                                        #"                
echo "#                                                                                        #"
echo "# Docker Image generation                                                                #"
echo "# arg1 (opt) --> -1 if only want to create images for modules layer (skip persistence)   #"
echo "#                                                                                        #"
echo "##########################################################################################"

# Load configuration file
source config.properties

homepath=$PWD

# Generates images only if they are not present in local docker registry
if [[ "$(docker images -q $GIT_GROUP/datagrid-server 2> /dev/null)" == "" ]]; then
	cd $homepath/../../../sources/datagrid-server/docker
	buildImage $homepath/../../../sources/datagrid-server datagrid-server $TAG
	pushImage datagrid-server $TAG
fi

if [[ "$(docker images -q $GIT_GROUP/datagrid-manager 2> /dev/null)" == "" ]]; then
	cd $homepath/../../../sources/datagrid-manager/docker
	buildImage $homepath/../../../sources/datagrid-manager datagrid-manager $TAG
	pushImage datagrid-manager $TAG
fi



echo "Docker images successfully generated!"

exit 0
