(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('livehtml', {
      templateUrl: 'app/components/view/templateComponent/liveHTMLComponent/livehtml.html',
      controller: 'LiveHTMLController',
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
      }
    }).controller('LiveHTMLController',
      function LiveHTMLController($controller, $rootScope, $scope, $element, $mdCompiler, datasourceSolverService, httpService, interactionService, utilsService, urlParamService, filterService, $translate, $window) {
        /* Extend from base controller */
        var $ctrl = this;
        var $base = $controller('baseTemplateController', {$scope: $scope, $rootScope: $rootScope, $element: $element, $mdCompiler: $mdCompiler, datasourceSolverService: datasourceSolverService, httpService: httpService, interactionService: interactionService, utilsService: utilsService, urlParamService: urlParamService, filterService: filterService, $translate: $translate, $window: $window});

        angular.extend($ctrl, $base);

        /* Own code */
        var vm = this;
        vm.tparams = vm.params;

        vm.$onDestroy = function () {
          
          
          if (vm.unsubscribeHandler) {
            vm.unsubscribeHandler();
            vm.unsubscribeHandler = null;
            if(typeof vm.datasource !== 'undefined'){
              datasourceSolverService.unregisterDatasourceTrigger(vm.datasource.name, vm.id);
            }
          }
          if (vm.destroyLiveComponent) {
            vm.destroyLiveComponent();
          }
        }
    
        vm.compileContent = function (){
          if (vm.destroyLiveComponent) {
            vm.destroyLiveComponent();
          }
          eval(vm.addSourceFile(vm.livecontentcode?vm.livecontentcode:""));
          $mdCompiler.compile({
            template: vm.livecontent,
            controller: vm.livecontroller
          }).then(function (compileData) {
            compileData.link($scope);
            $element.empty();
            $element.prepend(compileData.element);
            if (vm.initLiveComponent) {
              vm.initLiveComponent();
            }
    
    
          });
        }
    
    
        vm.eventLProcessor = function(event, dataEvent) {
          if (dataEvent.type === "data" && dataEvent.data.length === 0) {
            vm.type = "nodata";
            $scope.ds = "";
            if (vm.drawLiveComponent) {
              vm.drawLiveComponent($scope.ds, null);
            }
          } else {
            switch (dataEvent.type) {
              case "data":
                switch (dataEvent.name) {
                  case "refresh":
                    if (vm.status === "initial" || vm.status === "ready") {
                      $scope.ds = dataEvent.data;
                      if (vm.drawLiveComponent) {
                        vm.drawLiveComponent($scope.ds, null);
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
                      vm.status = "ready";
                      if (vm.drawLiveComponent) {
                        vm.drawLiveComponent($scope.ds, $scope.ds_old);
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
    );
  
})();
