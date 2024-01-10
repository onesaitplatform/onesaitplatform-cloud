(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('reacttemplate', {
      templateUrl: 'app/components/view/templateComponent/reactTemplateComponent/reacttemplate.html',
      controller: ReactController,
      controllerAs: 'vm',
      bindings: {
        id: "=?",
        livecontent: "<",
        livecontentcode: "<",
        datasource: "<",
        datastatus: "=?",
        filters: "=",
        custommenuoptions: "=?",
        showonlyfiltered: "=?",
        template: "<?",
        params: "<?",
        gadgetid: "<?",
        toolsopts: "="
      }
    });

  /** @ngInject */
  function ReactController($controller, $rootScope, $scope, $element, $mdCompiler, datasourceSolverService, httpService, interactionService, utilsService, urlParamService, filterService, $translate, $window) {
    /* Extend from base controller */
    var $ctrl = this;
    var $base = $controller('baseTemplateController', {$scope: $scope, $rootScope: $rootScope, $element: $element, $mdCompiler: $mdCompiler, datasourceSolverService: datasourceSolverService, httpService: httpService, interactionService: interactionService, utilsService: utilsService, urlParamService: urlParamService, filterService: filterService, $translate: $translate, $window: $window});

    angular.extend($ctrl, $base);

    /* Own code */
    var vm = this;
    vm.tparams = vm.params;

    vm.reactapp = {}

    vm.$onDestroy = function () {
      
      
      if (vm.unsubscribeHandler) {
        vm.unsubscribeHandler();
        vm.unsubscribeHandler = null;
        if(typeof vm.datasource !== 'undefined'){
          datasourceSolverService.unregisterDatasourceTrigger(vm.datasource.name, vm.id);
        }
      }
      if (vm.reactapp && vm.destroyReactGadget) {
        vm.destroyReactGadget();
      }
    }

    vm.compileContent = function(){
      if (vm.reactapp && vm.destroyReactGadget) {
        vm.destroyReactGadget();
      }

      if(!vm.id){//not enabled
        return;
      }

      document.querySelector("#" + vm.id + " reacttemplate .styles").innerHTML="";
      document.querySelector("#" + vm.id + " reacttemplate .styles").innerHTML=vm.livecontent;

      eval(vm.addSourceFile(vm.livecontentcode?vm.livecontentcode:""));

      if (vm.renderReactGadget) {
        vm.renderReactGadget($scope.ds);
        if ($scope.ds) {
          if (vm.drawLiveComponent) {
            vm.drawLiveComponent($scope.ds, null);
          }
        }
      }

    }

    vm.eventLProcessor = function(event, dataEvent) {
      if (dataEvent.type === "data" && dataEvent.data.length === 0) {
        vm.type = "nodata";
        $scope.ds = "";
        if (vm.renderReactGadget ) {
          vm.renderReactGadget ($scope.ds, null);
        }
      } else {
        switch (dataEvent.type) {
          case "data":
            switch (dataEvent.name) {
              case "refresh":
                if (vm.status === "initial" || vm.status === "ready") {
                  $scope.ds = dataEvent.data;
                  vm.reactapp.ds = dataEvent.data;
                  if (vm.renderReactGadget ) {
                    vm.renderReactGadget($scope.ds, $scope.ds_old);
                  }
                } else {
                  console.log("Ignoring refresh event, status " + vm.status);
                }
                break;
              case "add":
                $scope.ds.concat(data);
                break;
              case "filter":
                if (vm.status === "pending") {
                  $scope.ds_old = JSON.parse(JSON.stringify($scope.ds));
                  $scope.ds = dataEvent.data;
                  vm.reactapp.ds_old = $scope.ds_old;
                  vm.reactapp.ds = dataEvent.data;
                  vm.status = "ready";
                  if (vm.renderReactGadget) {
                    vm.renderReactGadget($scope.ds, $scope.ds_old);
                  }
                }
                break;
              default:
                console.error("Not allowed data event: " + dataEvent.name);
                break;
            }
            break;
          case "filter":
            vm.status = "pending";
            vm.type = "loading";
            if (!vm.datastatus) {
              vm.datastatus = [];
            }
            if (dataEvent.data.length) {
              for (var index in dataEvent.data) {
                vm.addDatastatus(dataEvent, index);
              }
              dataEvent.data = dataEvent.data.filter(function(s){return s.value!==null;});
            } else {
              vm.deleteDatastatus(dataEvent);
            }
            datasourceSolverService.updateDatasourceTriggerAndShot(vm.id, datasourceSolverService.buildFilterStt(dataEvent));
            break;
          case "action":
            if (dataEvent.data.length) {
              for (var index in dataEvent.data) {
                if (dataEvent.data[index].value === "start") {
                  datasourceSolverService.startRefreshIntervalData(vm.id);
                } else if (dataEvent.data[index].value === "stop") {
                  datasourceSolverService.stopRefreshIntervalData(vm.id);
                } else if (dataEvent.data[index].value === "refresh") {
                  datasourceSolverService.refreshIntervalData(vm.id);
                }
              }
            }
            break;
          case "value":
            vm.receiveValue(dataEvent.data);
            break
          case "customOptionMenu":
            vm.receiveValue(dataEvent.data);
            break;
          default:
            console.error("Not allowed event: " + dataEvent.type);
            break;
        }
      }
      utilsService.forceRender($scope);
    }



  }
})();