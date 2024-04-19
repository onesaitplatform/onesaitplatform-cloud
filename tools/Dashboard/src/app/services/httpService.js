(function () {
  'use strict';

  angular.module('dashboardFramework')
    .service('httpService', HttpService);

  /** @ngInject */
  function HttpService($http, $log, __env, $rootScope,localStorageService) {
      var vm = this;
      $http.defaults.headers.common['Authorization'] = 'Bearer '+__env.dashboardEngineOauthtoken;

      vm.modelurl = __env.dashboardEngineBungleMode?'/dashboards/bunglemodel/':'/dashboards/model/';

      vm.getDatasources = function(){
        return $http.get(__env.endpointControlPanel + '/datasources/getUserGadgetDatasources',{'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }

      vm.getsampleDatasources = function(ds){
        return $http.get(__env.endpointControlPanel + '/datasources/getSampleDatasource/'+ds,{'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }

      vm.getDatasourceById = function(datasourceId){
        return $http.get(__env.endpointControlPanel + '/datasources/getDatasourceById/' + datasourceId,{'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }
      vm.getDatasourceByIdentification = function(datasourceIdentification){
        return $http.get(__env.endpointControlPanel + '/datasources/getDatasourceByIdentification/' + datasourceIdentification,{'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }

      vm.getDatasourceByIdentification = function(datasourceIdentification){
        return $http.get(__env.endpointControlPanel + '/datasources/getDatasourceByIdentification/' + datasourceIdentification,{'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }


      vm.getFieldsFromDatasourceId = function(datasourceId){
        return $http.get(__env.endpointControlPanel + '/datasources/getSampleDatasource/' + datasourceId,{'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }

      vm.getGadgetConfigById = function(gadgetId){
        return $http.get(__env.endpointControlPanel + '/gadgets/getGadgetConfigById/' + gadgetId,{'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }

      vm.getUserGadgetsByType = function(type){
        return $http.get(__env.endpointControlPanel + '/gadgets/getUserGadgetsByType/' + type,{'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }

      vm.getUserGadgetTemplate = function(type){
        return $http.get(__env.endpointControlPanel + '/gadgettemplates/getUserGadgetTemplate/' + type,{'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }
      vm.getGadgetTemplateByIdentification = function(identification){
        return $http.get(__env.endpointControlPanel + '/gadgettemplates/getGadgetTemplateByIdentification/'+ identification,{'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }
      vm.getGadgetMeasuresByGadgetId = function(gadgetId){
        return $http.get(__env.endpointControlPanel + '/gadgets/getGadgetMeasuresByGadgetId/' + gadgetId, {'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }

      vm.saveDashboard = function(id, dashboard){
        var model = JSON.parse(dashboard.data.model);
        model.updatedAt = new Date().getTime();         
        localStorageService.setItem(id,{"model":JSON.stringify(model)},"savedByUser",model.updatedAt);       
        return $http.put(__env.endpointControlPanel + '/dashboards/savemodel/' + id, {"model":JSON.stringify(model)},{'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }
      vm.saveDashboardToken = function(id, dashboard, token){
        var model = JSON.parse(dashboard.data.model);
        model.updatedAt = new Date().getTime();                      
        localStorageService.setItem(id,{"model":JSON.stringify(model)},"savedByUser",model.updatedAt);  
        return $http.put(__env.endpointControlPanel + '/dashboardapi/savemodel/' + id, JSON.parse(dashboard.data.model) ,{'headers': { 'Authorization':token }});
      }
      vm.deleteDashboard = function(id){
        return $http.put(__env.endpointControlPanel + '/dashboards/delete/' + id,{},{'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }
      vm.freeResource = function(id){
        return $http.get(__env.endpointControlPanel + '/dashboards/freeResource/'+ id, {'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }

      vm.getHeaderLibsById = function(id){
        return $http.get(__env.endpointControlPanel + '/dashboards/headerlibs/' + id, {'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }

      vm.saveHeaderLibsById = function(id,headerlibs){
        return $http.put(__env.endpointControlPanel + '/dashboards/saveheaderlibs/' + id,headerlibs, {'headers': { 'Authorization':__env.dashboardEngineOauthtoken, 'content-type': 'text/html' }});
      }

      vm.getHeaderLibsById = function(id){
        return $http.get(__env.endpointControlPanel + '/dashboards/headerlibs/' + id, {'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }

      vm.saveHeaderLibsById = function(id,headerlibs){
        return $http.put(__env.endpointControlPanel + '/dashboards/saveheaderlibs/' + id,headerlibs, {'headers': { 'Authorization':__env.dashboardEngineOauthtoken, 'content-type': 'text/html' }});
      }

      vm.getTemplateTypes = function(){
        return $http.get(__env.endpointControlPanel + '/gadgettemplates/getTemplateTypes' , {'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }

      vm.getGadgetTemplateType = function(id){
        return $http.get(__env.endpointControlPanel + '/gadgettemplates/getTemplateTypeById/' + id, {'headers': { 'Authorization':__env.dashboardEngineOauthtoken }});
      }

      //favorite gadgets
      vm.getFavoriteGadgetGetallidentifications = function(){
        return $http.get(__env.endpointControlPanel + '/api/favoritegadget/getallidentifications' );
      }
      vm.getFavoriteGadgetGetall = function(){
        return $http.get(__env.endpointControlPanel + '/api/favoritegadget/getall' );
      }
      vm.getFavoriteGadgetByIdentification = function(identification){
        return $http.get(__env.endpointControlPanel + '/api/favoritegadget/' + identification);
      }
      vm.existFavoriteGadgetByIdentification = function(identification){
        return $http.get(__env.endpointControlPanel + '/api/favoritegadget/existwithidentification/' + identification);
      }
      vm.createFavoriteGadget = function (favoriteGadget){
        return $http.post(__env.endpointControlPanel + '/api/favoritegadget/' ,JSON.stringify(favoriteGadget));
      }
      vm.updateFavoriteGadget = function (favoriteGadget){
        return $http.put(__env.endpointControlPanel + '/api/favoritegadget/' + identification,JSON.stringify(favoriteGadget));
      }
      vm.deleteFavoriteGadget = function (identification){
        return $http.delete(__env.endpointControlPanel + '/api/favoritegadget/' + identification);
      }
      //end favorite gadgets

      vm.setDashboardEngineCredentials = function () {
        if(__env.dashboardEngineOauthtoken === '' || !__env.dashboardEngineOauthtoken){//No oauth token, trying login user/pass
          if(__env.dashboardEngineUsername != '' && __env.dashboardEngineUsername){
            var authdata = 'Basic ' + btoa(__env.dashboardEngineUsername + ':' + __env.dashboardEnginePassword);
            $rootScope.globals = {
              currentUser: {
                  username: __env.dashboardEngineUsername,
                  authdata: __env.dashboardEnginePassword
              }
            };
          }
          else{//anonymous login
            var authdata = 'anonymous';
          }
        }
        else{//oauth2 login
          var authdata = "Bearer " + __env.dashboardEngineOauthtoken;
          $rootScope.globals = {
            currentUser: {
                oauthtoken: __env.dashboardEngineOauthtoken
            }
          };
        }
      
      };

      vm.setDashboardEngineCredentialsAndLogin = function () {
        vm.setDashboardEngineCredentials();
      
        return $http.get(__env.endpointDashboardEngine + __env.dashboardEngineLoginRest, {headers: {Authorization: authdata}, timeout : __env.dashboardEngineLoginRestTimeout});
      };

      vm.getDashboardModel = function(id){
        return $http.get(__env.endpointControlPanel + vm.modelurl + id);
      }

      vm.insertHttp = function(token, clientPlatform, clientPlatformId, ontology, data){
        return $http.get(__env.restUrl + "/client/join?token=" + token + "&clientPlatform=" + clientPlatform + "&clientPlatformId=" + clientPlatformId).then(
          function(e){
            $http.defaults.headers.common['Authorization'] = e.data.sessionKey;
            return $http.post(__env.restUrl + "/ontology/" + ontology,data);
          }
        )
      }
  };
})();
