(function () {
  'use strict';

  angular.module('dashboardFramework')
    .service('httpService', MockHttpService);

  /** @ngInject */
  function MockHttpService($http, $window, $log, __env, $rootScope,$q) {
      var vm = this;

      vm.getDatasources = function(){
        $log.info("Mock empty answer");
        return [];
      }

      vm.getsampleDatasources = function(ds){
        $log.info("Mock empty answer");
        return [];
      }

      vm.getDatasourceById = function(datasourceId){
        $log.info("From local path");
        return $http.get(window.__env.dashboardEngineMockModelPath + $window.location.pathname.slice(1,-1) + ".json").then(
          function(data){
            var deferred = $q.defer();
            deferred.resolve({"data":data.data.gadgetDatasources.filter(function(d){return d.id === datasourceId})[0]});
            return deferred.promise;
          }
        );
      }
      vm.getDatasourceByIdentification = function(datasourceIdentification){
        $log.info("Mock empty answer");
        return [];
      }

      vm.getDatasourceByIdentification = function(datasourceIdentification){
        $log.info("Mock empty answer");
        return [];
      }


      vm.getFieldsFromDatasourceId = function(datasourceId){
        $log.info("Mock empty answer");
        return [];
      }

      vm.getGadgetConfigById = function(gadgetId){
        $log.info("From local path");
        return $http.get(window.__env.dashboardEngineMockModelPath + $window.location.pathname.slice(1,-1) + ".json").then(
          function(data){
            var deferred = $q.defer();
            deferred.resolve({"data":data.data.gadgets.filter(function(g){return g.id === gadgetId})[0]});
            return deferred.promise;
          }
        );
      }

      vm.getUserGadgetsByType = function(type){
        $log.info("Mock empty answer");
        return [];
      }

      vm.getUserGadgetTemplate = function(){
        $log.info("Mock empty answer");
        return [];
      }
      vm.getGadgetTemplateByIdentification = function(identification){
        $log.info("Mock empty answer");
        return {};
      }
      vm.getGadgetMeasuresByGadgetId = function(gadgetId){
        $log.info("From local path");
        return $http.get(window.__env.dashboardEngineMockModelPath + $window.location.pathname.slice(1,-1) + ".json").then(
          function(data){
            var deferred = $q.defer();
            deferred.resolve({"data":data.data.gadgetMeasures.filter(function(gm){return gm.gadget.id === gadgetId})});
            return deferred.promise;
          }
        );
      }

      vm.saveDashboard = function(id, dashboard){        
        $log.error("Not implemented");
      }
      vm.saveDashboardToken = function(id, dashboard, token){                
        $log.error("Not implemented");
      }
      vm.deleteDashboard = function(id){
        $log.error("Not implemented");
      }

      vm.setDashboardEngineCredentialsAndLogin = function () {
        $log.info("Mock credential setted");
        var deferred = $q.defer();
        deferred.resolve();
        return deferred.promise;
      };

      vm.getDashboardModel = function(id){
        $log.info("From local path");
        return $http.get(window.__env.dashboardEngineMockModelPath + id).then(
          function(data){
            var deferred = $q.defer();
            deferred.resolve({"data":JSON.parse(data.data.model)});
            return deferred.promise;
          }
        );
      }

      vm.insertHttp = function(token, clientPlatform, clientPlatformId, ontology, data){
        $log.error("Not implemented");
      }
  };
})();
