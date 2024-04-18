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
 * Controller for Stage Library Uninstall Modal.
 */

angular
  .module('dataCollectorApp.home')
  .controller('DeleteExtrasModalInstanceController', function ($scope, $modalInstance, stageLibrariesExtras, api) {
    angular.extend($scope, {
      common: {
        errors: []
      },
      stageLibrariesExtras: stageLibrariesExtras,
      operationInProgress: false,
      libraryUninstalled: false,
      isRestartInProgress: false,

      deleteExtras: function() {
        $scope.operationInProgress = true;
        api.pipelineAgent.deleteExtras(stageLibrariesExtras)
          .then(
            function (res) {
              $scope.libraryUninstalled = true;
              $scope.operationInProgress = false;
            },
            function (res) {
              $scope.operationInProgress = false;
              $scope.common.errors = [res.data];
            }
          );
      },

      restart: function() {
        $scope.isRestartInProgress = true;
        api.admin.restartDataCollector();
      },

      cancel: function() {
        $modalInstance.dismiss('cancel');
      }
    });
  });
