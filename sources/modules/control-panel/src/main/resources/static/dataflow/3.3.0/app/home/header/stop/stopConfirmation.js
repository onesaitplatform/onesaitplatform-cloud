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
 * Controller for Stop Confirmation Modal.
 */

angular
  .module('dataCollectorApp.home')
  .controller('StopConfirmationModalInstanceController', function ($scope, $modalInstance, pipelineInfo, forceStop, api) {
    angular.extend($scope, {
      common: {
        errors: []
      },
      pipelineInfo: pipelineInfo,
      forceStop: forceStop,
      stopping: false,
      isList: _.isArray(pipelineInfo),

      yes: function() {
        $scope.stopping = true;
        if ($scope.isList) {

          var pipelineNames = _.pluck(pipelineInfo, 'pipelineId');
          api.pipelineAgent.stopPipelines(pipelineNames, forceStop)
            .then(function(response) {
              var res = response.data;
              if (res.errorMessages.length === 0) {
                $modalInstance.close(res);
              } else {
                $scope.stopping = false;
                $scope.common.errors = res.errorMessages;
              }
            })
            .catch(function(res) {
              $scope.stopping = false;
              $scope.common.errors = [res.data];
            });

        } else {
          api.pipelineAgent.stopPipeline(pipelineInfo.pipelineId, 0, forceStop)
            .then(function(res) {
              $modalInstance.close(res.data);
            })
            .catch(function(res) {
              $scope.stopping = false;
              $scope.common.errors = [res.data];
            });
        }
      },
      no: function() {
        $modalInstance.dismiss('cancel');
      }
    });
  });
