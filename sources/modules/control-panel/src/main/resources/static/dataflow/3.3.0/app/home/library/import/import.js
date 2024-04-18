/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Controller for Import Modal Dialog.
 */

angular
  .module('dataCollectorApp.home')
  .controller('ImportModalInstanceController', ["$scope", "$modalInstance", "api", "pipelineInfo", "$translate", function ($scope, $modalInstance, api, pipelineInfo, $translate) {
    var errorMsg = 'Not a valid Pipeline Configuration file.';

    angular.extend($scope, {
      common: {
        errors: []
      },
      showLoading: true,
      uploadFile: {},
      createNewPipeline: (!pipelineInfo),
      pipelineInfo: pipelineInfo,
      newConfig : {
        title: '',
        description: ''
      },

      /**
       * Import button callback function.
       */
      import: function () {
        var reader = new FileReader();

        if ($scope.createNewPipeline && !$scope.newConfig.title) {
          $translate('home.library.nameRequiredValidation').then(function(translation) {
            $scope.common.errors = [translation];
          });

          return;
        }

        reader.onload = function (loadEvent) {
          try {
            var parsedObj = JSON.parse(loadEvent.target.result);
            var jsonConfigObj;
            var jsonRulesObj;

            if (parsedObj.pipelineConfig) {
              //It is an config and rules envelope
              jsonConfigObj = parsedObj.pipelineConfig;
              jsonRulesObj = parsedObj.pipelineRules;
            } else {
              jsonConfigObj = parsedObj;
            }

            if (!jsonConfigObj.version) {
              jsonConfigObj.version = 1;
            }

            if (jsonConfigObj.uuid) {
              if (pipelineInfo && !$scope.createNewPipeline) { // If pipeline config already exists
                jsonConfigObj.uuid = pipelineInfo.uuid;
                jsonConfigObj.metadata = pipelineInfo.metadata;
                jsonConfigObj.title = pipelineInfo.title;

                api.pipelineAgent.savePipelineConfig(pipelineInfo.pipelineId, jsonConfigObj).
                then(function(res) {
                  if (jsonRulesObj && jsonRulesObj.uuid) {
                    api.pipelineAgent.getPipelineRules(pipelineInfo.pipelineId).
                    then(function(res) {
                      var rulesObj = res.data;
                      rulesObj.schemaVersion = jsonRulesObj.schemaVersion;
                      rulesObj.version = jsonRulesObj.version;
                      rulesObj.metricsRuleDefinitions = jsonRulesObj.metricsRuleDefinitions;
                      rulesObj.dataRuleDefinitions = jsonRulesObj.dataRuleDefinitions;
                      rulesObj.driftRuleDefinitions = jsonRulesObj.driftRuleDefinitions;
                      rulesObj.emailIds = jsonRulesObj.emailIds;
                      rulesObj.configuration = jsonRulesObj.configuration;

                      api.pipelineAgent.savePipelineRules(pipelineInfo.pipelineId, rulesObj).
                      then(function() {
                        $modalInstance.close();
                      });

                    });

                  } else {
                    $modalInstance.close();
                  }
                },function(res) {
                  $scope.common.errors = [res.data];
                });
              } else { // If no pipeline exist or create pipeline option selected
                var newPipelineObject,
                  label,
                  name,
                  description;

                if ($scope.createNewPipeline) {
                  label = $scope.newConfig.title;
                  description = $scope.newConfig.description;
                } else {
                  label = jsonConfigObj.info.label || jsonConfigObj.info.pipelineId;
                  description = jsonConfigObj.info.description;
                }

                api.pipelineAgent.createNewPipelineConfig(label, description)
                  .then(function(res) {
                    newPipelineObject = res.data;
                    name = newPipelineObject.info.pipelineId;
                    newPipelineObject.configuration = jsonConfigObj.configuration;
                    newPipelineObject.errorStage = jsonConfigObj.errorStage;
                    newPipelineObject.statsAggregatorStage = jsonConfigObj.statsAggregatorStage;
                    newPipelineObject.uiInfo = jsonConfigObj.uiInfo;
                    newPipelineObject.stages = jsonConfigObj.stages;
                    newPipelineObject.version = jsonConfigObj.version;
                    newPipelineObject.schemaVersion = jsonConfigObj.schemaVersion;
                    newPipelineObject.metadata = jsonConfigObj.metadata;
                    newPipelineObject.startEventStages = jsonConfigObj.startEventStages;
                    newPipelineObject.stopEventStages = jsonConfigObj.stopEventStages;
                    return api.pipelineAgent.savePipelineConfig(name, newPipelineObject);
                  })
                  .then(function(res) {
                    if (jsonRulesObj && jsonRulesObj.uuid) {
                      api.pipelineAgent.getPipelineRules(name).
                      then(function(res) {
                        var rulesObj = res.data;
                        rulesObj.schemaVersion = jsonRulesObj.schemaVersion;
                        rulesObj.version = jsonRulesObj.version;
                        rulesObj.metricsRuleDefinitions = jsonRulesObj.metricsRuleDefinitions;
                        rulesObj.dataRuleDefinitions = jsonRulesObj.dataRuleDefinitions;
                        rulesObj.driftRuleDefinitions = jsonRulesObj.driftRuleDefinitions;
                        rulesObj.emailIds = jsonRulesObj.emailIds;
                        rulesObj.configuration = jsonRulesObj.configuration;

                        api.pipelineAgent.savePipelineRules(name, rulesObj).
                        then(function() {
                          $modalInstance.close(newPipelineObject);
                        });

                      });

                    } else {
                      $modalInstance.close(newPipelineObject);
                    }
                  },function(res) {
                    $scope.common.errors = [res.data];

                    //Failed to import pipeline. If new pipeline is created during import revert it back.
                    if (res.data && res.data.RemoteException &&
                      res.data.RemoteException.errorCode === 'CONTAINER_0201') {
                      // For CONTAINER_0201 - Pipeline 'Sample123' already exists - don't delete existing one
                      return;
                    }

                    api.pipelineAgent.deletePipelineConfig(name);
                  });
              }

            } else {
              $scope.$apply(function() {
                $scope.common.errors = [errorMsg];
              });
            }
          } catch(e) {
            $scope.$apply(function() {
              $scope.common.errors = [errorMsg];
            });
          }
        };
        reader.readAsText($scope.uploadFile);
      },

      /**
       * Cancel button callback.
       */
      cancel: function () {
        $modalInstance.dismiss('cancel');
      }
    });



    $scope.$watch('uploadFile', function (newValue) {
      if (newValue && newValue.name && !$scope.newConfig.title) {
        $scope.newConfig.title = newValue.name.replace('.json', '');
      }
    });


  }]);
