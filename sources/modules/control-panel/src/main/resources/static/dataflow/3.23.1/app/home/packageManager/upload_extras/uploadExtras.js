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
 * Controller for Upload Extras Modal.
 */

angular
  .module('dataCollectorApp.home')
  .controller('UploadExtrasModalInstanceController',
    ["$scope", "$rootScope", "$modalInstance", "installedLibraries", "api", function ($scope, $rootScope, $modalInstance, installedLibraries, api) {
      angular.extend($scope, {
        common: {
          errors: []
        },
        operationStatus: 'incomplete',
        installedLibraries: installedLibraries,
        libraryInfo: {
          library: '',
          uploadFile: {}
        },

        uploadExtras: function() {
          if (!$scope.libraryInfo.library) {
            $scope.common.errors = ['Please Select Library'];
            return;
          }

          if (!$scope.libraryInfo.uploadFile.name) {
            $scope.common.errors = ['Please Select File to Upload'];
            return;
          }

          $scope.operationStatus = 'uploading';
          api.pipelineAgent.installExtras($scope.libraryInfo.library.stageLibraryManifest.stageLibId, $scope.libraryInfo.uploadFile)
            .then(
              function(res) {
                $scope.operationStatus = 'complete';
              },
              function(res) {
                $scope.common.errors = [res.data];
              }
            );
        },

        restart: function() {
          $scope.operationStatus = 'restarting';
          api.admin.restartDataCollector();
        },

        cancel: function() {
          $modalInstance.dismiss('cancel');
        }
      });

      $scope.libraryInfo.library = _.find(installedLibraries, function (library) {
        return library.stageLibraryManifest.stageLibId === 'streamsets-datacollector-jdbc-lib';
      });

      if (!$scope.libraryInfo.library && installedLibraries) {
        $scope.libraryInfo.library = installedLibraries[0];
      }

      $scope.installedLibraries = _.chain(installedLibraries)
        .filter(function(stageLibrary) {
          return stageLibrary.stageLibraryManifest.stageLibId !== 'streamsets-datacollector-stats-lib';
        })
        .sortBy(function(stageLibrary) {
          return stageLibrary.stageLibraryManifest.stageLibLabel;
        })
        .value();
    }]);
