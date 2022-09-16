(function () {
  'use strict';

  angular.module('dashboardFramework')
    .service('favoriteGadgetService', FavoriteGadgetService);

  /** @ngInject */
  function FavoriteGadgetService(httpService, $log, __env, $rootScope, $timeout, $q) {

    var vm = this;

    vm.create = function (data) {
      var defered = $q.defer();
      var promise = defered.promise;
      httpService.existFavoriteGadgetByIdentification(data.identification).then(function (resultExist) {
        if (resultExist.data) {
          defered.resolve({
            status: "error",
            message: "Error. There is already a favorite with that identifier"
          });
        } else {
          httpService.createFavoriteGadget(addMetainfFromContext(data)).then(function (resultCreate) {
            if (resultCreate.status == 200) {
              window.postMessage("addNewFavoriteGadget", "*");
              if(window.self !== window.top){
                window.parent.postMessage("addNewFavoriteGadget", "*");
              }
              defered.resolve({
                status: "ok",
                message: "Favorite gadget created"
              });
            } else {
              console.log(resultCreate);
              defered.resolve({
                status: "error",
                message: "Error. There was a problem during creation"
              });
            }
          }).catch(function (error) {
            console.log(error);
            defered.resolve({
              status: "error",
              message: "Error. There was a problem during creation"
            });
          });
        }
      });
      return promise;
    }

    vm.getAllIdentifications = function () { 
      var defered = $q.defer();
      var promise = defered.promise;
      httpService.getFavoriteGadgetGetallidentifications().then(function (response) {
        defered.resolve(response.data);
      }).catch(function (error) {
        defered.resolve([]);
      })
      return promise;
    }

    vm.getFavoriteGadgetByIdentification = function (identification) {
      var defered = $q.defer();
      var promise = defered.promise;
      httpService.getFavoriteGadgetByIdentification(identification).then(function (response) {
        defered.resolve(response.data);
      }).catch(function (error) {
        defered.resolve([]);
      })
      return promise;
    }


    vm.delete = function (identification) {
      return httpService.deleteFavoriteGadget(identification);
    }

    vm.exist = function (identification) {
      var defered = $q.defer();
      var promise = defered.promise;
      httpService.existFavoriteGadgetByIdentification(data.identification).then(function (resultExist) {        
          defered.resolve(resultExist.data);
      })
      return promise;
      }

    function addMetainfFromContext(data) { //add metainf to gadget favorite from window.gfmetainf
      if (window.gfmetainf !== null) {
        data['metainf'] = window.gfmetainf;
      }
      return data;
    }  
  };
})();