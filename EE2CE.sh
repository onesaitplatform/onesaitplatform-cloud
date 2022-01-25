
echo "Remove General folders"

rm -R ./sources/examples
rm -R ./sources/plugins
rm -R ./sources/modules/microservices-gateway
#rm -R ./sources/modules/video-broker
rm -R ./sources/modules/bpm-engine
rm -R ./sources/modules/scripting-engine
rm -R ./tools
echo "Remove General folders...............OK"


echo "Remove Devops folders"

#rm -R ./devops/build-deploy/rancher
rm -R ./devops/build-deploy/config-resources
rm -R ./devops/build-deploy/migration-scripts
rm -R ./devops/build-deploy/openshift
rm -R ./devops/build-deploy/version-upgrade
rm -R ./devops/build-deploy/docker/data
rm -R ./devops/build-deploy/docker/HOWTO.md
rm -R ./devops/build-deploy/docker/config.properties
rm -R ./devops/build-deploy/docker/image-generation.sh
rm -R ./devops/build-deploy/docker/dockerfiles/configdb
rm -R ./devops/build-deploy/docker/dockerfiles/dashboardexporter
rm -R ./devops/build-deploy/docker/dockerfiles/elasticsearch
rm -R ./devops/build-deploy/docker/dockerfiles/elasticsearch630
rm -R ./devops/build-deploy/docker/dockerfiles/gravitee
rm -R ./devops/build-deploy/docker/dockerfiles/kafka-cluster
rm -R ./devops/build-deploy/docker/dockerfiles/mongoexpress
rm -R ./devops/build-deploy/docker/dockerfiles/nginx
rm -R ./devops/build-deploy/docker/dockerfiles/nodered
#rm -R ./devops/build-deploy/docker/dockerfiles/platform-base-image
rm -R ./devops/build-deploy/docker/dockerfiles/prometheus-grafana
rm -R ./devops/build-deploy/docker/dockerfiles/quasar
rm -R ./devops/build-deploy/docker/dockerfiles/quasar30
rm -R ./devops/build-deploy/docker/dockerfiles/quasar40
rm -R ./devops/build-deploy/docker/dockerfiles/realtimedb
rm -R ./devops/build-deploy/docker/dockerfiles/realtimedb36
rm -R ./devops/build-deploy/docker/dockerfiles/realtimedb40
rm -R ./devops/build-deploy/docker/dockerfiles/realtimedb40-rs
rm -R ./devops/build-deploy/docker/dockerfiles/registry-ui
rm -R ./devops/build-deploy/docker/dockerfiles/schedulerdb
rm -R ./devops/build-deploy/docker/dockerfiles/staticserver
rm -R ./devops/build-deploy/docker/dockerfiles/streamsets310
rm -R ./devops/build-deploy/docker/dockerfiles/streamsets33
rm -R ./devops/build-deploy/docker/dockerfiles/streamsets38
rm -R ./devops/build-deploy/docker/dockerfiles/zeppelin
rm -R ./devops/build-deploy/docker/modules
rm -R ./devops/build-deploy/docker/README.md
rm -R ./devops/ci-cd
rm -R ./Jenkinsfile
echo "Remove Devops folders...............OK"



echo "Remove Microservice classes"
echo "From (but not all) find . -name *icroser*"
echo "From (but not all) find . -name *MS*"

rm -R ./sources/libraries/config/model/src/integration-test/java/com/minsait/onesait/platform/config/repository/MicroserviceRepositoryTest.java
#rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/model/Microservice.java
rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/repository/MicroserviceRepository.java
#rm -R ./sources/modules/config-init/src/main/resources/configurations/Microservice-compose.yml
rm -R ./sources/modules/control-panel/src/integration-test/java/com/minsait/onesait/platform/microservices/MicroserviceIntegrationTest.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/controller/microservice/MicroserviceController.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/controller/microservice
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/rest/management/microservices/MicroservicesRestController.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/rest/management/microservices/model/MicroserviceDeployment.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/rest/management/microservices
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/rest/microservice/MicroserviceEntity.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/rest/microservice/MicroserviceRestController.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/rest/microservice
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/microservice/MicroserviceBusinessService.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/microservice/MicroserviceBusinessServiceImpl.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/microservice/MicroserviceTemplateUtil.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/microservice
rm -R ./sources/modules/control-panel/src/main/resources/static/js/pages/microservice
rm -R ./sources/modules/control-panel/src/main/resources/static/microservices/microservice-ml.zip
rm -R ./sources/modules/control-panel/src/main/resources/static/microservices/microservice.zip
rm -R ./sources/modules/control-panel/src/main/resources/static/microservices
rm -R ./sources/modules/control-panel/src/main/resources/templates/microservice
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/exceptions/MicroserviceException.java
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/microservice/dto/MicroserviceDTO.java
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/microservice/dto/MicroserviceRestDTO.java
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/microservice/MicroserviceService.java
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/microservice/MicroserviceServiceImpl.java
rm -R ./sources/modules/control-panel/src/integration-test/java/com/minsait/onesait/platform/microservices/MSTestConfiguration.java
rm -R ./sources/modules/control-panel/src/integration-test/java/com/minsait/onesait/platform/microservices/MSTestExecutionListener.java
rm -R ./sources/modules/control-panel/src/integration-test/java/com/minsait/onesait/platform/microservices
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/microservice/dto/MSConfig.java
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/microservice
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/rest/deployment/Deploy.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/rest/deployment/DeploymentRestService.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/rest/deployment
rm -R ./sources/modules/config-init/src/main/resources/configurations/JenkinsXMLTemplateIoT.xml
rm -R ./sources/modules/config-init/src/main/resources/configurations/JenkinsXMLTemplateML.xml
rm -R ./sources/modules/config-init/src/main/resources/configurations/JenkinsXMLTemplateNaaS.xml
rm -R ./sources/modules/config-init/src/main/resources/configurations/Microservice-compose.yml
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/controller/promotiontool/PromotionToolController.java
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/promotiontool/PromotionToolParamsDTO.java
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/promotiontool/PromotionToolService.java
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/promotiontool/PromotionToolServiceImpl.java

echo "Remove Microservice classes...............OK"


echo "Remove Gitlab classes"
echo "From (but not all) find . -name *gitl*"
echo "From (but not all) find . -name *Git*"

#rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/components/GitlabConfiguration.java
#rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/converters/GitlabConfigurationConverter.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/rest/deployment/GitlabInput.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/exceptions/GitlabException.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/GitlabRestService.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/GitlabRestServiceImpl.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/GitOperations.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/GitOperationsImpl.java

echo "Remove Gitlab classes...............OK"

echo "Remove CaaS classes"
echo "From (but not all) find . -name *CaaS*"
echo "From (but not all) find . -name *ancher*"

rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/rest/deployment/CaasPlatform.java
#rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/components/RancherConfiguration.java
#rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/converters/RancherConfigurationConverter.java
#rm -R ./sources/modules/config-init/src/main/resources/configurations/DockerCompose_Rancher.yml
#rm -R ./sources/modules/config-init/src/main/resources/simulations/RancherConfiguration.yml
rm -R ./sources/modules/control-panel/docker/rancher
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/exceptions/RancherException.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/RancherService.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/RancherServiceImpl.java

echo "Remove CaaS classes...............OK"


echo "Remove Jenkins classes"
echo "From (but not all) find . -name *enkin*.java*"
echo "From (but not all) find . -name *Jenkins*.*"

#rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/components/JenkinsConfiguration.java
#rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/converters/JenkinsConfigurationConverter.java
rm -R ./sources/modules/control-panel/src/integration-test/java/com/minsait/onesait/platform/services/jenkins/JenkinsTest.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/jenkins/JenkinsBuildWatcher.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/jenkins/JenkinsException.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/jenkins/JenkinsService.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/jenkins/JenkinsServiceImpl.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/jenkins/model/JenkinsBuild.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/jenkins/model/JenkinsPipeline.java
#rm -R ./sources/modules/config-init/src/main/resources/configurations/JenkinsXMLTemplateIoT.xml
#rm -R ./sources/modules/config-init/src/main/resources/configurations/JenkinsXMLTemplateML.xml

echo "Remove Jenkins classes...............OK"


echo "Remove Project Services classes"
echo "From (but not all) find . -name *enkin*.java*"
echo "From (but not all) find . -name *Jenkins*.*"

#rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/components/OpenshiftConfiguration.java
#rm -R ./sources/modules/config-init/src/main/resources/configurations/OpenshiftConfiguration.yml
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/exceptions/OpenshiftException.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/OpenshiftService.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/OpenshiftServiceImpl.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/NginxService.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/NginxServiceImpl.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/project/

echo "Remove Project Services classes...............OK"



echo "Remove BPM classes"
echo "From (but not all) find . -name *BPM*"

#rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/components/BPMEngine.java
rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/model/BPMTenant.java
rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/model/BPMTenantAuthorization.java
rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/repository/BPMTenantAuthorizationRepository.java
rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/repository/BPMTenantRepository.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/controller/bpm/BPMAuthorization.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/controller/bpm/BPMController.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/controller/bpm/BPMTenant.java
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/bpm/BPMTenantService.java
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/bpm/BPMTenantServiceImpl.java
rm -R ./sources/modules/control-panel/src/main/resources/static/js/pages/bpm
rm -R ./sources/modules/control-panel/src/main/resources/templates/bpm
rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/components/BPMEngine.java

echo "Remove BPM classes...............OK"


echo "Remove Cognitive files"
echo "From (but not all) find . -name *AzureCog*"

#rm -R ./sources/modules/config-init/src/main/resources/market/details/NoderedAzureCognitiveServiceLanguage.json
#rm -R ./sources/modules/config-init/src/main/resources/market/details/NoderedAzureCognitiveServiceSpeech.json
#rm -R ./sources/modules/config-init/src/main/resources/market/details/NoderedAzureCognitiveServiceTranslation.json
#rm -R ./sources/modules/config-init/src/main/resources/market/details/NoderedAzureCognitiveServiceVision.json
#rm -R ./sources/modules/config-init/src/main/resources/market/docs/AzureCognitiveServiceLanguage.json
#rm -R ./sources/modules/config-init/src/main/resources/market/docs/AzureCognitiveServiceSpeech.json
#rm -R ./sources/modules/config-init/src/main/resources/market/docs/AzureCognitiveServiceTranslation.json
#rm -R ./sources/modules/config-init/src/main/resources/market/docs/AzureCognitiveServiceVision.json

echo "Remove Cognitive files...............OK"


echo "Remove Gravitee files"
echo "From (but not all) find . -name *ravit*"

#rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/components/Gravitee.java
rm -R ./sources/modules/api-manager/src/main/java/com/minsait/onesait/platform/api/rule/rules/GraviteeRequestRule.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/gravitee/controller/GraviteeController.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/gravitee/dto/GraviteeApi.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/gravitee/dto/GraviteeException.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/gravitee/GraviteeService.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/gravitee/GraviteeServiceImpl.java
rm -R ./sources/modules/control-panel/src/main/resources/templates/apimanager/gravitee.html
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/gravitee
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/gravitee
rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/components/Gravitee.java

echo "Remove Gravitee files...............OK"



echo "Remove CloudGateway files"
echo "From (but not all) find . -name *loudGate*"

rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/gateway/CloudGatewayService.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/gateway/CloudGatewayServiceImpl.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/services/gateway

echo "Remove CloudGateway files...............OK"

echo "Remove OpenData files"

rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/controller/opendata/DatasetController.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/controller/opendata/OpenDataController.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/controller/opendata/OrganizationController.java
rm -R ./sources/modules/control-panel/src/main/java/com/minsait/onesait/platform/controlpanel/controller/opendata/ResourceController.java
rm -R ./sources/services/config-services/src/main/java/com/minsait/onesait/platform/config/services/opendata
rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/repository/DatasetResourceRepository.java
rm -R ./sources/libraries/config/model/src/main/java/com/minsait/onesait/platform/config/components/GISViewer.java
rm -R ./sources/modules/control-panel/src/main/resources/templates/opendata
rm -R ./sources/modules/control-panel/src/main/resources/static/js/pages/datasetsCreate.js
rm -R ./sources/modules/control-panel/src/main/resources/static/js/pages/resourcesCreateExternal.js
rm -R ./sources/modules/control-panel/src/main/resources/static/js/pages/resourcesCreate.js
rm -R ./sources/modules/control-panel/src/main/resources/static/js/pages/organizationsCreate.js
rm -R ./sources/modules/control-panel/src/main/resources/static/js/pages/datasetsCreate.js
rm -R ./sources/services/business-services/src/main/java/com/minsait/onesait/platform/business/services/opendata
rm -R ./sources/services/business-services/src/main/java/com/minsait/onesait/platform/business/services/dataset

echo "Remove OpenData files...............OK"

echo "Remove Static files"
echo "Files which not change as they are static"
echo "Review this files before delete in case there are changes to be commited to CE version"

#rm -R ./devops/build-deploy/docker/scripts/image-generation-registry.sh
#rm -R ./devops/build-deploy/docker/scripts/config.properties

echo "Remove Static files...............OK"


echo "Replace Too Weight files for other files"
echo "Files which are not possible to upload to Github"

echo " - Replace Yolo weights for dummy weights (they can be found on internet)"
rm -R ./sources/modules/video-broker/src/main/resources/processor/yolo/yolov3.weights
touch ./sources/modules/video-broker/src/main/resources/processor/yolo/yolov3.weights

echo " - Replace Example QA_DETAIL-dataset.json for smaller one"
head -500 ./sources/modules/config-init/src/main/resources/examples/QA_DETAIL-dataset.json > ./sources/modules/config-init/src/main/resources/examples/QA_DETAIL-dataset-tmp.json
mv ./sources/modules/config-init/src/main/resources/examples/QA_DETAIL-dataset-tmp.json ./sources/modules/config-init/src/main/resources/examples/QA_DETAIL-dataset.json

echo "Replace Too Weight files...............OK"