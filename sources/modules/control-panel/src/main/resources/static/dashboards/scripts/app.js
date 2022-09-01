(function () {
  'use strict';

  angular.module('dashboardFramework', ['angular-gridster2', 'ngSanitize', 'ngMaterial', 'lfNgMdFileInput', 'color.picker', 'ngStomp', 'ngAnimate', 'angular-d3-word-cloud', 'chart.js', 'ui-leaflet', 'nemLogging', 'md.data.table', '720kb.tooltips','nvd3','moment-picker','as.sortable', 'pascalprecht.translate'])
})();

(function () {
  'use strict';

  VueController.$inject = ["$controller", "$rootScope", "$scope", "$element", "$mdCompiler", "datasourceSolverService", "httpService", "interactionService", "utilsService", "urlParamService", "filterService", "$translate", "$window"];
  angular.module('dashboardFramework')
    .component('vuetemplate', {
      templateUrl: 'app/components/view/templateComponent/vueTemplateComponent/vuetemplate.html',
      controller: VueController,
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
        params: "<?"
      }
    });

  /** @ngInject */
  function VueController($controller, $rootScope, $scope, $element, $mdCompiler, datasourceSolverService, httpService, interactionService, utilsService, urlParamService, filterService, $translate, $window) {
    /* Extend from base controller */
    var $ctrl = this;
    var $base = $controller('baseTemplateController', {$scope: $scope, $rootScope: $rootScope, $element: $element, $mdCompiler: $mdCompiler, datasourceSolverService: datasourceSolverService, httpService: httpService, interactionService: interactionService, utilsService: utilsService, urlParamService: urlParamService, filterService: filterService, $translate: $translate, $window: $window});

    angular.extend($ctrl, $base);

    /* Own code */
    var vm = this;

    vm.$onDestroy = function () {
      
      
      if (vm.unsubscribeHandler) {
        vm.unsubscribeHandler();
        vm.unsubscribeHandler = null;
        if(typeof vm.datasource !== 'undefined'){
          datasourceSolverService.unregisterDatasourceTrigger(vm.datasource.name, vm.id);
        }
      }
      if (vm.vueapp && vm.vueapp.destroyVueComponent) {
        vm.vueapp.destroyVueComponent();
      }
    }

    vm.compileContent = function(){
      if (vm.vueapp && vm.vueapp.destroyVueComponent) {
        vm.vueapp.destroyVueComponent();
      }

      if(!vm.id){//not enabled
        return;
      }

      document.querySelector("#" + vm.id + " vuetemplate").innerHTML="";
      document.querySelector("#" + vm.id + " vuetemplate").innerHTML=vm.livecontent;

      eval(vm.addSourceFile(vm.livecontentcode?vm.livecontentcode:""));

      if (vm.initLiveComponent) {
        vm.initLiveComponent();
      }

    }

    vm.eventLProcessor = function(event, dataEvent) {
      if (dataEvent.type === "data" && dataEvent.data.length === 0) {
        vm.type = "nodata";
        $scope.ds = "";
        if (vm.drawLiveComponent) {
          vm.drawLiveComponent($scope.ds, null);
        }
        if (vm.vueapp.drawVueComponent) {
          vm.vueapp.drawVueComponent($scope.ds, null);
        }
      } else {
        switch (dataEvent.type) {
          case "data":
            switch (dataEvent.name) {
              case "refresh":
                if (vm.status === "initial" || vm.status === "ready") {
                  $scope.ds = dataEvent.data;
                  vm.vueapp.ds = dataEvent.data;
                  if (vm.drawLiveComponent) {
                    vm.drawLiveComponent($scope.ds, $scope.ds_old);
                  }
                  if (vm.vueapp.drawVueComponent) {
                    vm.vueapp.drawVueComponent($scope.ds, $scope.ds_old);
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
                  vm.vueapp.ds_old = $scope.ds_old;
                  vm.vueapp.ds = dataEvent.data;
                  vm.status = "ready";
                  if (vm.vueapp.drawVueComponent) {
                    vm.vueapp.drawVueComponent($scope.ds, $scope.ds_old);
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
(function () {
  'use strict';

  ReactController.$inject = ["$controller", "$rootScope", "$scope", "$element", "$mdCompiler", "datasourceSolverService", "httpService", "interactionService", "utilsService", "urlParamService", "filterService", "$translate", "$window"];
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
        params: "<?"
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
      }

    }

    vm.eventLProcessor = function(event, dataEvent) {
      if (dataEvent.type === "data" && dataEvent.data.length === 0) {
        vm.type = "nodata";
        $scope.ds = "";
        vm.renderReactGadget ($scope.ds, null);
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
        params: "<?"
      }
    }).controller('LiveHTMLController',
      ["$controller", "$rootScope", "$scope", "$element", "$mdCompiler", "datasourceSolverService", "httpService", "interactionService", "utilsService", "urlParamService", "filterService", "$translate", "$window", function LiveHTMLController($controller, $rootScope, $scope, $element, $mdCompiler, datasourceSolverService, httpService, interactionService, utilsService, urlParamService, filterService, $translate, $window) {
        /* Extend from base controller */
        var $ctrl = this;
        var $base = $controller('baseTemplateController', {$scope: $scope, $rootScope: $rootScope, $element: $element, $mdCompiler: $mdCompiler, datasourceSolverService: datasourceSolverService, httpService: httpService, interactionService: interactionService, utilsService: utilsService, urlParamService: urlParamService, filterService: filterService, $translate: $translate, $window: $window});

        angular.extend($ctrl, $base);

        /* Own code */
        var vm = this;
    
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
            vm.drawLiveComponent($scope.ds, null);
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
    
        
    
      }]
    );
  
})();
(function () {
  'use strict';

  FilterController.$inject = ["$scope"];
  angular.module('dashboardFramework')
    .component('textfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/textfilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"      
      }
    });

  /** @ngInject */
  function FilterController($scope) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    
    
    vm.$onInit = function () {
      vm.vue = new Vue({
        el: '#'+vm.config.htmlId,
        data: function() {
          return {
            dynamicValidateForm: {
              inputValue: vm.config.value,
              inputName: vm.config.name,
              inputId: vm.idfilter
            }
          }
        },
        methods: {
          valueChange : function(inputValue) {
            $scope.$apply(function() {
              vm.config.value = inputValue;
            });
          }, noop: function() {
            // do nothing
          }
        }
      })
    };

  
  }
})();

(function () {
  'use strict';

  FilterController.$inject = ["$scope"];
  angular.module('dashboardFramework')
    .component('simpleselectnumberfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/simpleselectnumberfilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"      
      }
    });

  /** @ngInject */
  function FilterController($scope) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
   
    vm.$onInit = function () {

    vm.options = parseOptions(vm.config.data.options);
    if(typeof vm.config.data.optionsSelected ==='undefined' ){
      vm.optionsSelected=null;
    }else{
      vm.optionsSelected = parseFloat(vm.config.data.optionsSelected);
    }
      vm.vue = new Vue({
        el: '#'+vm.config.htmlId,
        data: function() {

          return {
            dynamicValidateForm: {             
              inputName: vm.config.name,
              inputId: vm.idfilter,
              options:vm.options,
              optionsSelected:vm.optionsSelected
            }
          }
        },
        methods: {
          valueChange : function(optionsSelected) {
            $scope.$apply(function() {
                if(optionsSelected===""){
                  vm.config.data.optionsSelected = null;
                }else{
                  vm.config.data.optionsSelected = optionsSelected;
                }
            });
          }
        }
      })
    };

    function parseOptions(opts){
      var temp = [];
      if(typeof opts!= 'undefined'){
        for (var index = 0; index < opts.length; index++) {
          temp.push(parseFloat(opts[index]));          
        }
      }
      return temp;
    }
  
  }
})();

(function () {
  'use strict';

  FilterController.$inject = ["$scope", "datasourceSolverService"];
  angular.module('dashboardFramework')
    .component('simpleselectnumberdsfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/simpleselectnumberdsfilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"      
      }
    });

  /** @ngInject */
  function FilterController($scope,datasourceSolverService) {
    var vm = this;
    
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    vm.options=[];
     
    $scope.getDataFromDataSource = function (datasource, callbackF, filters, group, project, sort, limit, offset, param, debug) {
      if (typeof filters === 'undefined' || filters === null) {
        filters = [];
      }
      var id, name, dat;
      return datasourceSolverService.getDataFromDataSourceForFilter({
        type: 'query',
        name: datasource,
        refresh: 0,
        triggers: [{ params: { filter: filters, group: (group ? group : []), project: (project ? project : []), sort: (sort ? sort : []), limit: (limit ? limit : -1), offset: (offset ? offset : -1), param: (param ? param : []), debug: (debug ? debug : false) }, emitTo: vm.id }]
      }, function (id, name, dat) {
        if (dat.error) {
          console.error("Error in response datasource: " + dat.data);
          dat.data = JSON.stringify({ "error": dat.data });
        }
        callbackF(dat.data, dat.error);
      }, callbackF);
    };

    vm.$onInit = function () {     
      var projects = []; 
      if(typeof vm.config.data !=='undefined'){
        delete vm.config.data.optionsDescription;
        delete vm.config.data.startDate;
        delete vm.config.data.endDate;
        delete vm.config.data.selectedPeriod;
        delete vm.config.data.realtime;
        delete vm.config.data.optionsSelected;
      }
      if(typeof vm.config.data !=='undefined' && typeof vm.config.data.dsFieldValue !=='undefined' &&  vm.config.data.dsFieldValue.length>0 ){
        projects.push({op:"",field: vm.config.data.dsFieldValue, alias:'filterCode'});
      }
      if(typeof vm.config.data !=='undefined' && typeof vm.config.data.dsFieldDes !=='undefined' &&  vm.config.data.dsFieldDes.length>0){
        projects.push({op:"",field: vm.config.data.dsFieldDes, alias: 'filterDes'});
      }
      $scope.getDataFromDataSource(vm.config.data.ds,function(data){
       try {
         var dat = JSON.parse(data);
         var keys = Object.keys(dat[0]); 
         var opt = [];
        
         if( keys.length === 1){
          for (var index = 0; index < dat.length; index++) { 
           var tmpv=Object.values(dat[index])[0]; 
           //filter distinct values for list
           if (opt.filter(function(e) { return e.value === tmpv; }).length === 0) {
            opt.push({label:tmpv,value:parseFloat(tmpv)});
            }     
               
          }
         }else{
          for (var index = 0; index < dat.length; index++) { 
            var tmpv=dat[index]['filterCode']; 
            var tmpd=dat[index]['filterDes']; 
           //filter distinct values for list
             if (opt.filter(function(e) { return e.value === tmpv; }).length === 0) {
              opt.push({label:tmpd,value:parseFloat(tmpv)});
              }          
           }
         }
        
        

       } catch (error) {
         
       }
        vm.options = opt;
        if(typeof vm.config.data.optionsSelected ==='undefined' ){
          vm.optionsSelected = null;
        }else{
          vm.optionsSelected = parseFloat(vm.config.data.optionsSelected);
        }
        
        vm.vue = new Vue({
          el: '#'+vm.config.htmlId,
          data: function() {
            return {
              dynamicValidateForm: {             
                inputName: vm.config.name,
                inputId: vm.idfilter,
                options:vm.options,
                optionsSelected:vm.optionsSelected
              }
            }
          },
          methods: {
            valueChange : function(optionsSelected) {
              $scope.$apply(function() {
                if(optionsSelected===""){
                  vm.config.data.optionsSelected = null;
                }else{
                   vm.config.data.optionsSelected = optionsSelected;
                }

              });
            }
          }
        })  
},null,null,projects
)

      



     
    };

  
  }
})();

(function () {
  'use strict';

  FilterController.$inject = ["$scope"];
  angular.module('dashboardFramework')
    .component('simpleselectfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/simpleselectfilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"      
      }
    });

  /** @ngInject */
  function FilterController($scope) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
   
    vm.$onInit = function () {
      vm.vue = new Vue({
        el: '#'+vm.config.htmlId,
        data: function() {
          return {
            dynamicValidateForm: {             
              inputName: vm.config.name,
              inputId: vm.idfilter,
              options:vm.config.data.options,
              optionsSelected:vm.config.data.optionsSelected
            }
          }
        },
        methods: {
          valueChange : function(optionsSelected) {
            $scope.$apply(function() {
              vm.config.data.optionsSelected = optionsSelected;
            });
          }
        }
      })
    };

  
  }
})();

(function () {
  'use strict';

  FilterController.$inject = ["$scope", "datasourceSolverService"];
  angular.module('dashboardFramework')
    .component('simpleselectdsfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/simpleselectdsfilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"      
      }
    });

  /** @ngInject */
  function FilterController($scope,datasourceSolverService) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    vm.options=[];
    $scope.getDataFromDataSource = function (datasource, callbackF, filters, group, project, sort, limit, offset, param, debug) {
      if (typeof filters === 'undefined' || filters === null) {
        filters = [];
      }
      var id, name, dat;
      return datasourceSolverService.getDataFromDataSourceForFilter({ 
        type: 'query',
        name: datasource,
        refresh: 0,
        triggers: [{ params: { filter: filters, group: (group ? group : []), project: (project ? project : []), sort: (sort ? sort : []), limit: (limit ? limit : -1), offset: (offset ? offset : -1), param: (param ? param : []), debug: (debug ? debug : false) }, emitTo: vm.id }]
      }, function (id, name, dat) {
        if (dat.error) {
          console.error("Error in response datasource: " + dat.data);
          dat.data = JSON.stringify({ "error": dat.data });
        }
        callbackF(dat.data, dat.error);
      }, callbackF);
    };

    vm.$onInit = function () {     
      var projects = []; 
      if(typeof vm.config.data !=='undefined'){
        delete vm.config.data.optionsDescription;
        delete vm.config.data.startDate;
        delete vm.config.data.endDate;
        delete vm.config.data.selectedPeriod;
        delete vm.config.data.realtime;
        delete vm.config.data.optionsSelected;
      }
      
      if(typeof vm.config.data !=='undefined' && typeof vm.config.data.dsFieldValue !=='undefined' &&  vm.config.data.dsFieldValue.length>0){
        projects.push({op:"",field: vm.config.data.dsFieldValue , alias:'filterCode'});
      }
      if(typeof vm.config.data !=='undefined' && typeof vm.config.data.dsFieldDes !=='undefined' &&  vm.config.data.dsFieldDes.length>0){
        projects.push({op:"",field: vm.config.data.dsFieldDes, alias: 'filterDes'});
      }
      $scope.getDataFromDataSource(vm.config.data.ds,function(data){
       try {
         var dat = JSON.parse(data);
         var keys = Object.keys(dat[0]); 
         var opt = [];
        
         if( keys.length === 1){
          for (var index = 0; index < dat.length; index++) { 
           var tmpv=Object.values(dat[index])[0]; 
           //filter distinct values for list
           if (opt.filter(function(e) { return e.value === tmpv; }).length === 0) {
            opt.push({label:tmpv,value:tmpv});
            }     
               
          }
         }else{
          for (var index = 0; index < dat.length; index++) { 
            var tmpv=dat[index]['filterCode']; 
            var tmpd=dat[index]['filterDes']; 
           //filter distinct values for list
             if (opt.filter(function(e) { return e.value === tmpv; }).length === 0) {
              opt.push({label:tmpd,value:tmpv});
              }          
           }
         }
        
        

       } catch (error) {
         
       }
        vm.options = opt;
        vm.vue = new Vue({
          el: '#'+vm.config.htmlId,
          data: function() {
            return {
              dynamicValidateForm: {             
                inputName: vm.config.name,
                inputId: vm.idfilter,
                options:vm.options,
                optionsSelected:vm.config.data.optionsSelected
              }
            }
          },
          methods: {
            valueChange : function(optionsSelected) {
              $scope.$apply(function() {
                vm.config.data.optionsSelected = optionsSelected;
              });
            }
          }
        })  
},null,null,projects
)

      



     
    };

  
  }
})();

(function () {
  'use strict';

  FilterController.$inject = ["$scope"];
  angular.module('dashboardFramework')
    .component('numberfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/numberfilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"
      }
    });

  /** @ngInject */
  function FilterController( $scope ) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    
    
    vm.$onInit = function () {
      vm.config.value  = parseFloat(vm.config.value);
      vm.vue = new Vue({
        el: '#'+vm.config.htmlId,
        data: function() {
          return {
            dynamicValidateForm: {
              inputValue: parseFloat(vm.config.value),
              inputName: vm.config.name,
              inputId: vm.idfilter
            }
          }
        },
        methods: {
          valueChange : function(inputValue) {
            $scope.$apply(function() {
              vm.config.value = parseFloat(inputValue);
            });
          }, noop: function() {
            // do nothing
          }
        }
      })
    };

  
  }
})();

(function () {
  'use strict';

  FilterController.$inject = ["$scope"];
  angular.module('dashboardFramework')
    .component('multiselectnumberfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/multiselectnumberfilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"      
      }
    });

  /** @ngInject */
  function FilterController($scope) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    
    
    vm.$onInit = function () {
      vm.opt=[];
      if( typeof vm.config.data.optionsDescription === 'undefined' || vm.config.data.optionsDescription === null ){
        for (var index = 0; index < vm.config.data.options.length; index++) {
          vm.opt.push({label:vm.config.data.options[index],value:vm.config.data.options[index]});          
        }
      }else{
        for (var index = 0; index < vm.config.data.options.length; index++) {
          vm.opt.push({label:vm.config.data.optionsDescription[index],value:vm.config.data.options[index]});          
        }
      }
      vm.vue = new Vue({
        el: '#'+vm.config.htmlId,
        data: function() {
          return {
            dynamicValidateForm: {             
              inputName: vm.config.name,
              inputId: vm.idfilter,
              options:vm.opt,
              optionsSelected:vm.config.data.optionsSelected
            }
          }
        },
        methods: {
          valueChange : function(optionsSelected) {
            $scope.$apply(function() {
              vm.config.data.optionsSelected = optionsSelected;
            });
          }
        }
      })
    };

  
  }
})();

(function () {
  'use strict';

  FilterController.$inject = ["$scope", "datasourceSolverService"];
  angular.module('dashboardFramework')
    .component('multiselectnumberdsfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/multiselectnumberdsfilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"      
      }
    });
 
  /** @ngInject */
  function FilterController($scope,datasourceSolverService) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    vm.options=[];
    $scope.getDataFromDataSource = function (datasource, callbackF, filters, group, project, sort, limit, offset, param, debug) {
      if (typeof filters === 'undefined' || filters === null) {
        filters = [];
      }
      var id, name, dat;
      return datasourceSolverService.getDataFromDataSourceForFilter({
        type: 'query',
        name: datasource,
        refresh: 0,
        triggers: [{ params: { filter: filters, group: (group ? group : []), project: (project ? project : []), sort: (sort ? sort : []), limit: (limit ? limit : -1), offset: (offset ? offset : -1), param: (param ? param : []), debug: (debug ? debug : false) }, emitTo: vm.id }]
      }, function (id, name, dat) {
        if (dat.error) {
          console.error("Error in response datasource: " + dat.data);
          dat.data = JSON.stringify({ "error": dat.data });
        }
        callbackF(dat.data, dat.error);
      }, callbackF);
    };

    vm.$onInit = function () {     
      var projects = []; 
      if(typeof vm.config.data !=='undefined'){
        delete vm.config.data.optionsDescription;
        delete vm.config.data.startDate;
        delete vm.config.data.endDate;
        delete vm.config.data.selectedPeriod;
        delete vm.config.data.realtime;
        delete vm.config.data.optionsSelected;
      }


      if(typeof vm.config.data !=='undefined' && typeof vm.config.data.dsFieldValue !=='undefined' &&  vm.config.data.dsFieldValue.length>0){
        projects.push({op:"",field: vm.config.data.dsFieldValue, alias:'filterCode'});
      }
      if(typeof vm.config.data !=='undefined' && typeof vm.config.data.dsFieldDes !=='undefined' && vm.config.data.dsFieldDes.length>0){
        projects.push({op:"",field: vm.config.data.dsFieldDes, alias: 'filterDes'});
      }
      $scope.getDataFromDataSource(vm.config.data.ds,function(data){
       try {
         var dat = JSON.parse(data);
         var keys = Object.keys(dat[0]); 
         var opt = [];
        
         if( keys.length === 1){
          for (var index = 0; index < dat.length; index++) { 
           var tmpv=Object.values(dat[index])[0]; 
           //filter distinct values for list
           if (opt.filter(function(e) { return e.value === tmpv; }).length === 0) {
            opt.push({label:tmpv,value:tmpv});
            }     
               
          }
         }else{
          for (var index = 0; index < dat.length; index++) { 
            var tmpv=dat[index]['filterCode']; 
            var tmpd=dat[index]['filterDes']; 
           //filter distinct values for list
             if (opt.filter(function(e) { return e.value === tmpv; }).length === 0) {
              opt.push({label:tmpd,value:tmpv});
              }          
           }
         }
       } catch (error) {         
       }
        vm.options = opt;
        vm.vue = new Vue({
          el: '#'+vm.config.htmlId,
          data: function() {
            return {
              dynamicValidateForm: {             
                inputName: vm.config.name,
                inputId: vm.idfilter,
                options:vm.options,
                optionsSelected:vm.config.data.optionsSelected
              }
            }
          },
          methods: {
            valueChange : function(optionsSelected) {
              $scope.$apply(function() {
                vm.config.data.optionsSelected = optionsSelected;
              });
            }
          }
        })  
},null,null,projects
)
    };
  }
})();

(function () {
  'use strict';

  FilterController.$inject = ["$scope"];
  angular.module('dashboardFramework')
    .component('multiselectfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/multiselectfilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"      
      }
    });

  /** @ngInject */
  function FilterController($scope) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    
    
    vm.$onInit = function () {
      vm.opt=[];
      if( typeof vm.config.data.optionsDescription === 'undefined' || vm.config.data.optionsDescription === null ){
        for (var index = 0; index < vm.config.data.options.length; index++) {
          vm.opt.push({label:vm.config.data.options[index],value:vm.config.data.options[index]});          
        }
      }else{
        for (var index = 0; index < vm.config.data.options.length; index++) {
          vm.opt.push({label:vm.config.data.optionsDescription[index],value:vm.config.data.options[index]});          
        }
      }
      vm.vue = new Vue({
        el: '#'+vm.config.htmlId,
        data: function() {
          return {
            dynamicValidateForm: {             
              inputName: vm.config.name,
              inputId: vm.idfilter,
              options:vm.opt,
              optionsSelected:vm.config.data.optionsSelected
            }
          }
        },
        methods: {
          valueChange : function(optionsSelected) {
            $scope.$apply(function() {
              vm.config.data.optionsSelected = optionsSelected;
            });
          }
        }
      })
    };

  
  }
})();

(function () {
  'use strict';

  FilterController.$inject = ["$scope", "datasourceSolverService"];
  angular.module('dashboardFramework')
    .component('multiselectdsfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/multiselectdsfilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"      
      }
    });
  
  /** @ngInject */
  function FilterController($scope,datasourceSolverService) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    vm.options=[];   
    
    $scope.getDataFromDataSource = function (datasource, callbackF, filters, group, project, sort, limit, offset, param, debug) {
      if (typeof filters === 'undefined' || filters === null) {
        filters = [];
      }
      var id, name, dat;
      return datasourceSolverService.getDataFromDataSourceForFilter({
        type: 'query',
        name: datasource,
        refresh: 0,
        triggers: [{ params: { filter: filters, group: (group ? group : []), project: (project ? project : []), sort: (sort ? sort : []), limit: (limit ? limit : -1), offset: (offset ? offset : -1), param: (param ? param : []), debug: (debug ? debug : false) }, emitTo: vm.id }]
      }, function (id, name, dat) {
        if (dat.error) {
          console.error("Error in response datasource: " + dat.data);
          dat.data = JSON.stringify({ "error": dat.data });
        }
        callbackF(dat.data, dat.error);
      }, callbackF);
    };

    vm.$onInit = function () {     
      var projects = []; 
      
      if(typeof vm.config.data !=='undefined'){
        delete vm.config.data.optionsDescription;
        delete vm.config.data.startDate;
        delete vm.config.data.endDate;
        delete vm.config.data.selectedPeriod;
        delete vm.config.data.realtime;
        delete vm.config.data.optionsSelected;
      }
      if(typeof vm.config.data !=='undefined' && typeof vm.config.data.dsFieldValue !=='undefined' &&  vm.config.data.dsFieldValue.length>0){
        projects.push({op:"",field: vm.config.data.dsFieldValue,  alias:'filterCode'});
      }
      if(typeof vm.config.data !=='undefined' && typeof vm.config.data.dsFieldDes !=='undefined' &&  vm.config.data.dsFieldDes.length>0){
        projects.push({op:"",field: vm.config.data.dsFieldDes, alias: 'filterDes'});
      }
      $scope.getDataFromDataSource(vm.config.data.ds,function(data){
       try {
         var dat = JSON.parse(data);
         var keys = Object.keys(dat[0]); 
         var opt = [];
        
         if( keys.length === 1){
          for (var index = 0; index < dat.length; index++) { 
           var tmpv=Object.values(dat[index])[0]; 
           //filter distinct values for list
           if (opt.filter(function(e) { return e.value === tmpv; }).length === 0) {
            opt.push({label:tmpv,value:tmpv});
            }     
               
          }
         }else{
          for (var index = 0; index < dat.length; index++) { 
            var tmpv=dat[index]['filterCode']; 
            var tmpd=dat[index]['filterDes']; 
           //filter distinct values for list
             if (opt.filter(function(e) { return e.value === tmpv; }).length === 0) {
              opt.push({label:tmpd,value:tmpv});
              }          
           }
         }
       } catch (error) {         
       }
        vm.options = opt;
        vm.vue = new Vue({
          el: '#'+vm.config.htmlId,
          data: function() {
            return {
              dynamicValidateForm: {             
                inputName: vm.config.name,
                inputId: vm.idfilter,
                options:vm.options,
                optionsSelected:vm.config.data.optionsSelected
              }
            }
          },
          methods: {
            valueChange : function(optionsSelected) {
              $scope.$apply(function() {
                vm.config.data.optionsSelected = optionsSelected;
              });
            }
          }
        })  
},null,null,projects
)
    };
  }
})();

(function () {
  'use strict';

  FilterController.$inject = ["$scope"];
  angular.module('dashboardFramework')
    .component('livefilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/livefilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"
      }
    });

  /** @ngInject */
  function FilterController( $scope ) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    
    
    vm.$onInit = function () {

   //   startDate:moment().subtract(8,'hour'),
   //   endDate:moment(),   
   //if realtime use now function name(params)
   //NOW();
   //NOW(“format“,'unitTime', amount)
   //NOW("yyyy-MM-dd'T'HH:mm:ss'Z'","hour",-intervalDates)

   var startDate = moment().subtract(vm.config.data.selectedPeriod,'hour');
   var endDate = moment();

   if(vm.config.data.realtime == "start"){
      vm.config.data.startDate = 'NOW("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'","hour",-'+vm.config.data.selectedPeriod+')';
      vm.config.data.endDate = 'NOW("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'","hour",0)';
   }else{
    startDate = moment (vm.config.data.startDate,moment.ISO_8601) ;  
    endDate = moment (vm.config.data.endDate,moment.ISO_8601) ; 
   }   

      vm.vue = new Vue({
        el: '#'+vm.config.htmlId,
        data: function() {
          return {
            dynamicValidateForm: {
              intervalDates: [new Date(startDate.format("YYYY-MM-DDTHH:mm")),new Date(endDate.format("YYYY-MM-DDTHH:mm"))],              
              options: [{
                value: 8,
                label: '8 h'
              }, {
                value: 16,
                label: '16 h'
              }, {
                value: 24,
                label: '24 h'
              }],
              selectedPeriod:vm.config.data.selectedPeriod,
              realtime :vm.config.data.realtime            
            }
          }
        },
        methods: {
          dateChange : function(dates) {
            $scope.$apply(function() {
              var startDate = moment(dates[0]);
              var endDate = moment(dates[1]);
              vm.config.data.startDate =startDate.toISOString();
              vm.config.data.endDate = endDate.toISOString();
            });
          },
          periodChange : function(selectedPeriod) {
            $scope.$apply(function() {
              vm.config.data.startDate = 'NOW("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'","hour",-'+selectedPeriod+')';
              vm.config.data.endDate = 'NOW("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'","hour",0)';
              vm.config.data.selectedPeriod = selectedPeriod;
            });
          },
          realTimeChange : function(realtime) {
            var selectedPeriod,intervalDates;
           
            if(realtime == "stop"){
              
              var startDate = moment().subtract(this.dynamicValidateForm.selectedPeriod,'hour');
              var endDate = moment();
              this.dynamicValidateForm.intervalDates = [new Date(startDate.format("YYYY-MM-DDTHH:mm")),new Date(endDate.format("YYYY-MM-DDTHH:mm"))];              
            }        

            selectedPeriod = this.dynamicValidateForm.selectedPeriod;
            intervalDates = this.dynamicValidateForm.intervalDates;            
            $scope.$apply(function() {
              vm.config.data.realtime = realtime;
              if(realtime == "stop"){
                vm.config.data.startDate =intervalDates[0].toISOString();
                vm.config.data.endDate =  intervalDates[1].toISOString();
              }else{
                vm.config.data.realtime = realtime;
                vm.config.data.startDate = 'NOW("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'","hour",-'+selectedPeriod+')';
                vm.config.data.endDate = 'NOW("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'","hour",0)';
              }          
            });
          }
        }
      })
    };

  
  }
})();

(function () {
    'use strict';
  
       FilterController.$inject = ["$scope"];
    angular.module('dashboardFramework')
      .component('intervaldatestringfilter', {
        templateUrl: 'app/components/view/filterComponent/filtersComponents/intervaldatestringfilter.html',
        controller: FilterController,
        controllerAs: 'vm',
        bindings:{
          idfilter:"<?",
          datasource: "<?",
          config:"=?"
        }
      });
  
    /** @ngInject */
    function FilterController( $scope ) {
      var vm = this;
      //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
      
      vm.$onInit = function () {
     
        var startDate = moment().subtract(8,'hour');
        var endDate = moment();
     
         if(typeof vm.config.data.startDate!=='undefined' && vm.config.data.startDate!=null ){
            startDate = moment (vm.config.data.startDate,moment.ISO_8601) ;  
         }
         if(typeof vm.config.data.startDate!=='undefined' && vm.config.data.startDate!=null ){
            endDate = moment (vm.config.data.endDate,moment.ISO_8601) ; 
         } 
     
           vm.vue = new Vue({
             el: '#'+vm.config.htmlId,
             data: function() {
               return {
                 dynamicValidateForm: {
                   inputName: vm.config.name,
                   intervalDates: [new Date(startDate.format("YYYY-MM-DDTHH:mm")),new Date(endDate.format("YYYY-MM-DDTHH:mm"))]                 
                 }
               }
             },
             methods: {
               dateChange : function(dates) {
                 $scope.$apply(function() {
                   var startDate = moment(dates[0]);
                   var endDate = moment(dates[1]);
                   vm.config.data.startDate =startDate.toISOString();
                   vm.config.data.endDate = endDate.toISOString();
                 });
               }
             }
           })
         };
     
       
       }
     })();
     
(function () {
  'use strict';

     FilterController.$inject = ["$scope"];
  angular.module('dashboardFramework')
    .component('intervaldatefilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/intervaldatefilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"
      }
    });

  /** @ngInject */
  function FilterController( $scope ) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    
    vm.$onInit = function () {
   
      var startDate = moment().subtract(8,'hour');
      var endDate = moment();
   
       if(typeof vm.config.data.startDate!=='undefined' && vm.config.data.startDate!=null ){
          startDate = moment (vm.config.data.startDate,moment.ISO_8601) ;  
       }
       if(typeof vm.config.data.startDate!=='undefined' && vm.config.data.startDate!=null ){
          endDate = moment (vm.config.data.endDate,moment.ISO_8601) ; 
       } 
   
         vm.vue = new Vue({
           el: '#'+vm.config.htmlId,
           data: function() {
             return {
               dynamicValidateForm: {
                 inputName: vm.config.name,
                 intervalDates: [new Date(startDate.format("YYYY-MM-DDTHH:mm")),new Date(endDate.format("YYYY-MM-DDTHH:mm"))]                 
               }
             }
           },
           methods: {
             dateChange : function(dates) {
               $scope.$apply(function() {
                 var startDate = moment(dates[0]);
                 var endDate = moment(dates[1]);
                 vm.config.data.startDate =startDate.toISOString();
                 vm.config.data.endDate = endDate.toISOString();
               });
             }
           }
         })
       };
   
     
     }
   })();
   
(function () {
  'use strict';

  ActionController.$inject = ["$scope"];
  angular.module('dashboardFramework')
    .component('activaterefreshaction', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/activaterefreshaction.html',
      controller: ActionController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"      
      }
    });

  /** @ngInject */
  function ActionController($scope) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    
    
    vm.$onInit = function () {     
      vm.vue = new Vue({
        el: '#'+vm.config.htmlId,
        data: function() {
          return {
            dynamicValidateForm: {
              inputValue: vm.config.value,
              inputName: vm.config.name,
              inputId: vm.idfilter
            }
          }
        },
        methods: {
          valueChange : function(inputValue) {
            $scope.$apply(function() {             
                vm.config.value=inputValue;                      
            });
          }
        }
      })
    };

  
  }
})();

(function () {
  'use strict';

DatadiscoveryFieldSelectorController.$inject = ["$log", "$scope", "$mdDialog", "$element", "datasourceSolverService", "utilsService"];
  angular.module('dashboardFramework')
    .component('datadiscoveryFieldSelector', { 
      templateUrl: 'app/components/view/datadiscoveryComponent/datadiscoveryComponents/datadiscoveryFieldSelector.html',
      controller: DatadiscoveryFieldSelectorController,
      controllerAs: 'vm',
      bindings:{            
        datasource: "=",
        columns: "="
      }
    });

  /** @ngInject */
  function DatadiscoveryFieldSelectorController($log, $scope, $mdDialog, $element, datasourceSolverService, utilsService) {
    columnStyleDialogController.$inject = ["$scope", "$mdDialog", "index"];
    var vm = this;

    vm.from = datasourceSolverService.from;

    vm.$onInit = function(){
      if(vm.columns.subtotalField != undefined && (!vm.columns.subtotalFields || (vm.columns.subtotalFields && vm.columns.subtotalFields.length === 0))){
        vm.columns.subtotalFields = [];
        vm.columns.subtotalFields.push(vm.columns.subtotalField);
        vm.columns.subtotalField = undefined;
      }
    }

    vm.$onChanged = function(){
    }

    vm.refreshModel = function(){//copy model to trigger onChange
      vm.columns = angular.copy(vm.columns);
    }

    vm.subtotalChange =function(index){
      vm.columns.subtotalFields = vm.columns.subtotalField==$index?-1:$index;
      vm.refreshModel()
    }

    vm.removeColumn = function(index){
      vm.columns.list.splice(index,1);
      vm.columns.subtotalFields=[];
      vm.refreshModel();
    }

    vm.onDrop = function(model){
      if(model.type !== 'metric'){
        model.asc = true;
      }
      else{
        model.asc = null;
      }
      model.field = model.field.replace(/\.([0-9])+(\.|$)/,"[$1]");
      vm.columns.subtotalEnable = vm.columns.list.filter(function(f){return f.type !== 'metric'}).length > 1 && vm.columns.list.filter(function(f){return f.type == 'metric'}).length > 0;
      vm.refreshModel();
    }

    vm.dragSelectControl = {
      accept: function (sourceItemHandleScope, destSortableScope) {
        return destSortableScope.modelValue.filter(function(item){return item.field === sourceItemHandleScope.modelValue.field && item.$$hashKey !== sourceItemHandleScope.modelValue.$$hashKey}).length == 0;
      },
      orderChanged: function(event) {
        vm.refreshModel();
      },
      allowDuplicates: false,
      containment: document.querySelector("#gadget_create_form")?".page-wrapper":null
    };

    vm.openColumnStyleDialog = function (index) {
      $mdDialog.show({
        controller: columnStyleDialogController,
        templateUrl: 'app/partials/edit/editDataDiscoveryColumnStyle.html',
        parent: angular.element(document.body),
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          index: index
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog pages closed'
        vm.refreshModel();
      }, function() {
        $scope.status = 'You cancelled the dialog.';
        vm.refreshModel();
      });
    };

    function columnStyleDialogController($scope, $mdDialog, index) {
      $scope.index = index;
      $scope.styles = vm.columns.list[index].condstyles;
      $scope.alias = vm.columns.list[index].alias;

      $scope.create = function() {
        var newCondStyle = {
          cond: $scope.cond,
          val: $scope.val,
          val2: $scope.val2,
          style: $scope.style,
          vfunction: $scope.vfunction
        }
        if(!$scope.styles){
          $scope.styles = [];  
        }
        $scope.styles.push(newCondStyle);
        vm.columns.list[index].condstyles = $scope.styles;
        $scope.$applyAsync();
      };

      $scope.delete = function(index){
        $scope.styles.splice(index, 1);
      }

      $scope.cancel = function() {
        vm.columns.list[index].alias = $scope.alias;
        $mdDialog.cancel();
      };
  
    }

    vm.toggleSubtotalField = function(index){
      var elemindex = vm.columns.subtotalFields.indexOf(index);
      if(elemindex === -1){
        vm.columns.subtotalFields.push(index);
      }
      else{
        vm.columns.subtotalFields.splice(elemindex, 1);
      }
    }

}
})();

(function () {
  'use strict';

DatadiscoveryFieldPickerController.$inject = ["$log", "$scope", "$mdDialog", "$element", "datasourceSolverService", "utilsService"];
  angular.module('dashboardFramework')
    .component('datadiscoveryFieldPicker', { 
      templateUrl: 'app/components/view/datadiscoveryComponent/datadiscoveryComponents/datadiscoveryFieldPicker.html',
      controller: DatadiscoveryFieldPickerController,
      controllerAs: 'vm',
      bindings:{            
        datasource: "<",
        fields: "=",
        metrics: "=",
        id: "<?"
      }
    });

  /** @ngInject */
  function DatadiscoveryFieldPickerController($log, $scope, $mdDialog, $element, datasourceSolverService, utilsService) {
    metricDialogController.$inject = ["$scope", "$mdDialog", "fields", "index"];
    var vm = this;

    vm.$onInit = function(){
      if(!vm.id && !vm.fields && !vm.metrics){
        vm.reloadFields();
      }
    }

    vm.addPrefaultMetrics = function(){
      vm.metrics.push({"type":"metric","field":"recordCount","formula":"count(*)"});
      vm.metrics = vm.metrics.concat(vm.getNumericFields().flatMap(
        function(column){
          return [
            {"type":"metric","field":column.field + "Sum","formula":"sum(" + column.field + ")"},
            {"type":"metric","field":column.field + "Avg","formula":"avg(" + column.field + ")"},
            {"type":"metric","field":column.field + "Min","formula":"min(" + column.field + ")"},
            {"type":"metric","field":column.field + "Max","formula":"max(" + column.field + ")"}
          ]
        }
      ));
    }

    vm.getNumericFields = function(){
      return vm.fields.filter(
        function(column){
          return column.type == "number" || column.type == "integer"
        }
      );
    }

    vm.metricDialog = function (index) {
      $mdDialog.show({
        controller: metricDialogController,
        templateUrl: 'app/partials/edit/addEditDataDiscoveryMetrics.html',
        parent: angular.element(document.body),
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          fields: vm.metrics,
          index: index
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog pages closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    function metricDialogController($scope, $mdDialog, fields, index) {
  
      if(index !== undefined){
        $scope.name = fields[index].field;
        $scope.formula = fields[index].formula; 
        $scope.index = index;
      }

      $scope.cancel = function() {
        $mdDialog.cancel();
      };
  
      $scope.createMetric = function(answer) {
        fields.push({"field":$scope.name,"type":"metric","formula":$scope.formula})
        $mdDialog.hide(answer);
      };

      $scope.editMetric = function(answer) {
        fields[index] = {"field":$scope.name,"type":"metric","formula":$scope.formula};
        $mdDialog.hide(answer);
      };
    }

    vm.dragPickerControl = {
      accept: false,
      itemMoved: function (event) {},
      orderChanged: function(event) {},
      clone: true,
      allowDuplicates: false,
      containment: document.querySelector("#gadget_create_form")?".page-wrapper":null
    };

    vm.removeAttr = function(index){
      vm.fields.splice(index,1);
    } 

    vm.removeMetric = function(index){
      vm.metrics.splice(index,1);
    } 

    vm.reloadFields = function(){
      datasourceSolverService.getFields(vm.datasource.identification).then(
        function(data){
          vm.fields = data;
          vm.metrics = [];
          vm.addPrefaultMetrics();
        }
      ).catch(
        function(error){
          console.error("Error geting datasource fields: " + error); 
        }
      );
    }
}
})();

(function () {
  'use strict';

  DatadiscoveryDataDrawController.$inject = ["$log", "$scope", "$element", "$timeout", "datasourceSolverService", "utilsService", "$q", "$window", "urlParamService", "filterService", "interactionService"];
  angular.module('dashboardFramework')
    .component('datadiscoveryDataDraw', {
      templateUrl: 'app/components/view/datadiscoveryComponent/datadiscoveryComponents/datadiscoveryDataDraw.html',
      controller: DatadiscoveryDataDrawController,
      controllerAs: 'vm',
      bindings: {
        id: "<?",
        datastatus: "=",
        datasource: "=",
        columns: "<",
        config: "<",
        filters: "<",
        reloadDataLink: "&",
        getDataAndStyle: "&"
      }
    });

  /** @ngInject */
  function DatadiscoveryDataDrawController($log, $scope, $element, $timeout, datasourceSolverService, utilsService, $q, $window, urlParamService, filterService,interactionService) {
    var vm = this;

    vm.from = datasourceSolverService.from;

    vm.$onInit = function () {      
     
      if(!vm.columns.dataAccess){
        vm.columns.dataAccess = {
          total: {
        //    index: totalindex
          },
          subTotals: {
            indexes: [],
            keyToIndex: {
    
            }
        //    key1: index1
        //    key2: index2
          }
        }
      }
      
      //Retrocompatibility
      if(vm.columns.subtotalField != undefined && (!vm.columns.subtotalFields || (vm.columns.subtotalFields && vm.columns.subtotalFields.length === 0))){
        vm.columns.subtotalFields = [];
        if(vm.columns.subtotalField != -1){
          vm.columns.subtotalFields.push(vm.columns.subtotalField);
          vm.columns.subtotalField = undefined;
        }
      }

      vm.ready = true;
      $scope.$on("$resize",vm.resizeJExcel);
      angular.element($window).on("resize",vm.resizeJExcel);
      
      vm.unsubscribeHandler = $scope.$on(vm.id,eventDProcessor);

      vm.reloadDataLink({"reloadchild":vm.redrawView});
      vm.getDataAndStyle({"getDataAndStyleChild":function(){return {data:vm.nonMergedData?vm.nonMergedData:vm.jexcel.getData(),style:vm.jexcel.getStyle(),headers:vm.getHeaders()}}});

      if(vm.config && vm.config.discovery && vm.config.discovery.matrix && vm.config.discovery.matrix.data){
        $timeout(
          function(){

            if(vm.filters && vm.filters.length>0){
              if(vm.filters){
                filterService.getInitialFilters(vm.id,vm.filters);
              }
              else{
                vm.redrawView();
              }
            }
            else{//No initial filter, keep saved data
              vm.blockUpdateTable = true;
              vm.drawRedrawJExcel(vm.config.discovery.matrix.data);
              vm.blockUpdateTable = false;
              vm.triggerUpdateTable();
              vm.jexcel.setStyle(vm.config.discovery.matrix.style);
              vm.mergeByColumnAttr();
            }
            vm.status = "ready"
            if(!vm.loadSended){
              window.dispatchEvent(new CustomEvent('gadgetloaded', { detail: vm.id }));
              vm.loadSended = true;
            }
          },0
        )
      }
      else{
        vm.status = "ready"
      }
    }

    vm.$onChanges = function (changes) {
      if (vm.status == "ready")
      {
        vm.deleteJExcel();
        vm.redrawView();
      }
    }

    vm.reloadDataF = function(){
      vm.redrawView();
    }

    vm.getHeaders = function(){
      var cols = vm.jexcel.getWidth();
      return vm.jexcel.getHeaders().split(",").map(function(h, index){
        return {
          name: h,
          width: cols[index]
        }
      })
    }

    vm.redrawView = function () {
     
      if(!vm.columns.dataAccess){
        vm.columns.dataAccess = {
          total: {
        //    index: totalindex
          },
          subTotals: {
            indexes: [],
            keyToIndex: {
    
            }
        //    key1: index1
        //    key2: index2
          }
        }
      }


      $scope.pindex = $scope.pindex ? $scope.pindex + 1 : 1;
      var actindex = angular.copy($scope.pindex);
      vm.blockUpdateTable = true;      
      vm.columns.dataAccess.subTotals.indexes=[];
      vm.columns.dataAccess.total.index=null;

      if (vm.columns && vm.columns.list && vm.columns.list.length) {
        vm.status = 'pending';
      }
      else{//No column data
        return;
      }
      vm.sendQuery().then(
        vm.drawRedrawJExcel
      ).then(
        function (data) {
          if (vm.config.enableTotal && vm.hasMetrics() && $scope.pindex === actindex) {
            return vm.sendQueryTotals().then(
              function (totalData) {
                return vm.setTotalsJExcel(totalData, data);
              }
            );
          }
          else {
            var deferred = $q.defer();
            deferred.resolve(data);
            return deferred.promise;
          }
        }
      ).then(
        function (data) {
          if (typeof data !='undefined' && data.length>0 && vm.columns.subtotalEnable && ((vm.columns.subtotalField != undefined && vm.columns.subtotalField != -1) || (vm.columns.subtotalFields && vm.columns.subtotalFields.length > 0))  && $scope.pindex === actindex) {
            return vm.sendQuerySubTotals().then(
              function (subTotalData) {
                return vm.setSubTotalsJExcel(subTotalData, data);
              }
            );
          }
          else {
            var deferred = $q.defer();
            deferred.resolve(data);
            return deferred.promise;
          }
        }
      ).then(function () {
        if($scope.pindex === actindex){
          vm.status = "ready";
          vm.blockUpdateTable = false;
          vm.triggerUpdateTable();
          vm.mergeByColumnAttr();
        }
      }).catch(function (e) {
        vm.status = "error";
        vm.error = e;
      });
    }

    vm.triggerUpdateTable = function(){
      vm.jexcel.setValue("A1",vm.jexcel.getValue("A1"));
    }

    vm.getGroupFields = function () {
      return vm.columns.list.filter(function (select) { return select.type !== 'metric' }).map(function (select) { return select.field });
    }

    vm.getSelectFields = function () {
      return vm.columns.list.map(function (select) {
        return { "field": (select.type != "metric" ? select.field : select.formula), "alias": vm.normalizeField(select.field) }
      });
    }

    vm.getSelectMetrics = function () {
      return vm.columns.list.filter(function (select) {
        return select.type === "metric";
      }).map(function (select) {
        return { "field": (select.type != "metric" ? select.field : select.formula), "alias": vm.normalizeField(select.field) }
      });
    }

    vm.getSortFields = function () {
      return vm.columns.list.filter(
        function (sort) {
          return sort.asc !== null
        }
      ).map(
        function (sort) {
          if (sort.type === "metric") {
            return { "field": sort.formula, "asc": sort.asc }
          }
          else {
            return { "field": sort.field, "asc": sort.asc }
          }
        }
      );
    }

    vm.getColumnIndex = function (name) {
      var vm = angular.element(document.querySelector('datadiscovery-data-draw')).isolateScope().vm;
      var index = vm.jexcel.options.columns.map(function (col) { return col.title }).indexOf(name);
      if (index == -1) {
        return vm.jexcel.options.columns.map(function (col) { return col.name }).indexOf(name);
      }
      return index;
    }

    vm.getSubTotalFields = function () {
      var returnArray = [];
      for(var i = 0; i < vm.columns.subtotalFields.length; i++){
        returnArray.push(vm.columns.list[vm.columns.subtotalFields[i]].field);
      }
      return returnArray;
    }

    vm.getFirstSubTotalIndex = function(){
      return vm.columns.subtotalFields.sort()[0];
    }

    vm.formatSubTotal = function(dataHash){
      var subtotalLabel = vm.config.prefixSubtotal;
      var defaultKey = "";
      for(var key in dataHash){
        var regex = new RegExp("\{" + key + "\}", "g");
        subtotalLabel = subtotalLabel.replace(regex,dataHash[key]);
        defaultKey += (defaultKey == ""?dataHash[key]:"-" + dataHash[key]);
      }
      var regex = new RegExp("\{\}", "g");
      subtotalLabel = subtotalLabel.replace(regex,defaultKey);
      
      return subtotalLabel;
    }

    vm.getSubtotalKey = function(row){
      var returnStringKey = "";
      for(var i = 0; i < vm.columns.subtotalFields.length; i++){
        returnStringKey += "-" + row[vm.normalizeField(vm.columns.list[vm.columns.subtotalFields[i]].field)];
      }
      return returnStringKey;
    }

    vm.getColumnKey = function(row,testColumns){
      var returnStringKey = "";
      for(var i = 0; i < testColumns.length; i++){
        returnStringKey += "-" + row[testColumns[i]];
      }
      return returnStringKey;
    }

    vm.fromJsonToRowTotalArray = function (data) {
      var rowArray = [];
      var lowindex = vm.jexcel.getHeaders(true).length, maxindex = 0;
      for (var d in data[0]) {
        var index = vm.getColumnIndex(d);
        rowArray[index] = ""+data[0][d];
        lowindex = Math.min(index, lowindex);
        maxindex = Math.max(index, maxindex);
      }
      //Add label Total
      if (lowindex > 0) {
        rowArray[lowindex - 1] = vm.config.prefixTotal;
      }
      else {
        rowArray[maxindex + 1] = vm.config.prefixTotal;
      }
      return rowArray;
    }

    vm.fromJsonToRowSubTotalArray = function (data) {
      var rowArray = [];
      var subtotalData = {};
      for (var d in data) {
        var index = vm.getColumnIndex(d);
        //Check column is data or subtotal
        if (vm.getSubTotalFields().map(function(f){return vm.normalizeField(f)}).indexOf(d)===-1) {
          rowArray[index] = ""+data[d];
        }
        else {
          subtotalData[d] = data[d];
        }
      }
      rowArray[vm.getFirstSubTotalIndex()] = vm.formatSubTotal(subtotalData);
      return rowArray;
    }

    vm.getJExcelColumnName = function (columnNumber) {
      var dividend = columnNumber;
      var columnName = "";
      var modulo;

      while (dividend > 0) {
        modulo = (dividend - 1) % 26;
        columnName = String.fromCharCode(65 + modulo) + columnName;
        dividend = parseInt((dividend - modulo) / 26);
      }

      return columnName;
    }

    vm.setTotalsJExcel = function (data, allData) {
      var index = allData.length - 1;
      var dataToInsert = vm.fromJsonToRowTotalArray(data);
      vm.jexcel.insertRow(dataToInsert, index);
      for (var i in dataToInsert) {
        vm.jexcel.setStyle(vm.getJExcelColumnName(parseInt(i) + 1) + (index + 2), 'font-weight', 'bold');
      }
      var deferred = $q.defer();
      deferred.resolve(allData);
      vm.columns.dataAccess.total.index = index;
      return deferred.promise;
    }

    vm.setSubTotalsJExcel = function (subTotalData, data) {
      //var refInstance = data[0][vm.getSubTotalField().replace(/\./g, "_").replace(/\[/g, "_").replace(/\]/g, "")];
      
      var refInstance = vm.getSubtotalKey(data[0]);
      var indexSubTotalData = 0;
      var indexToInsert = 0;
      vm.subTotalData = [];
      for (var inst = 0; inst < data.length; inst++) {
        var actRefInstance = vm.getSubtotalKey(data[inst]);
        //if (data[inst][vm.getSubTotalField().replace(/\./g, "_").replace(/\[/g, "_").replace(/\]/g, "")] !== refInstance) {
        if(actRefInstance !== refInstance){
          var dataToInsert = vm.fromJsonToRowSubTotalArray(subTotalData[indexSubTotalData], refInstance);
          var rowIndexSubtotal = indexToInsert - 1 + indexSubTotalData;
          vm.jexcel.insertRow(dataToInsert, rowIndexSubtotal);
          for (var i in dataToInsert) {
            vm.jexcel.setStyle(vm.getJExcelColumnName(parseInt(i) + 1) + (indexToInsert + 1 + indexSubTotalData), 'font-weight', 'bold');
          }
          indexSubTotalData++;
          refInstance = actRefInstance;
          vm.columns.dataAccess.subTotals.keyToIndex[refInstance] = {index: rowIndexSubtotal+1};
          vm.columns.dataAccess.subTotals.indexes.push(rowIndexSubtotal+1);
          vm.columns.dataAccess.total.index++;
        }
        indexToInsert++
      }
      var dataToInsert = vm.fromJsonToRowSubTotalArray(subTotalData[indexSubTotalData], refInstance);
      var rowIndexSubtotal = indexToInsert - 1 + indexSubTotalData;
      vm.jexcel.insertRow(dataToInsert, rowIndexSubtotal);
      for (var i in dataToInsert) {
        vm.jexcel.setStyle(vm.getJExcelColumnName(parseInt(i) + 1) + (indexToInsert + 1 + indexSubTotalData), 'font-weight', 'bold');
      }
      var deferred = $q.defer();
      deferred.resolve(data);
      vm.columns.dataAccess.subTotals.keyToIndex[refInstance] = {index: rowIndexSubtotal+1};
      vm.columns.dataAccess.subTotals.indexes.push(rowIndexSubtotal+1);
      vm.columns.dataAccess.total.index++;
      return deferred.promise;
    }

    vm.hasMetrics = function () {
      return vm.columns.list.filter(function (column) {
        return column.type === "metric";
      }).length > 0
    }

    vm.deleteJExcel = function () {
      if (vm.jexcel) {
        $element.find("div")[1].innerHTML = "";
        $element.find("div")[1].style.height = "0px";
      }
    }

    vm.mergeByColumnAttr = function () {
      if(vm.config.enableMergeCols){
        var data = vm.jexcel.getData();
        vm.nonMergedData = angular.copy(data);
        var inspectColumns = {}
        var notMetricColumns = []
        for (var ind in vm.columns.list) {
          if (vm.columns.list[ind].type !== 'metric') {
            notMetricColumns.push(ind);
            inspectColumns[ind] = { index: 0, value: data[0][ind] };
          }
        }
        var refInstance = vm.getColumnKey(data[0], notMetricColumns);
        for (var inst = 0; inst < data.length && ((vm.config.enableTotal&&vm.columns.dataAccess.total.index)?(vm.columns.dataAccess.subTotals.indexes.length>0?(vm.columns.dataAccess.total.index+1) > inst:(vm.columns.dataAccess.total.index+1) >= inst):true); inst++) {
          var actRefInstance = vm.getColumnKey(data[inst], notMetricColumns);
          var isSubtotalRow = vm.columns.dataAccess.subTotals.indexes.indexOf(inst) != -1;
          if (actRefInstance !== refInstance || isSubtotalRow) {
            for (var c in notMetricColumns) {
              var ncol = notMetricColumns[c];
              var newval = data[inst][ncol];
              if (isSubtotalRow) {
                for (var clast = 0; clast < notMetricColumns.length; clast++) {
                  var cell = vm.getJExcelColumnName(parseInt(notMetricColumns[clast]) + 1) + (inspectColumns[clast].index + 1);
                  vm.jexcel.setStyle(cell, 'border-right', '1px solid #CCC');
                  if (cell.indexOf("A") == 0) {
                    vm.jexcel.setStyle(cell, 'border-left', '1px solid #CCC');
                  }
                  if ((inst - inspectColumns[clast].index) > 1) {
                    vm.jexcel.setMerge(cell, 1, inst - (inspectColumns[clast].index));
                  }
                  if (data.length > inst + 2) {
                    inspectColumns[clast] = { index: inst + 1, value: data[inst + 1][notMetricColumns[clast]] };
                  }
                }
                refInstance = vm.getColumnKey(data[inst], notMetricColumns);
                break;
              }
              else if (inspectColumns[ncol].value !== newval) {
                for (var clast = c; clast < notMetricColumns.length; clast++) {
                  var cell = vm.getJExcelColumnName(parseInt(notMetricColumns[clast])+1)+ (inspectColumns[clast].index+1);
                  vm.jexcel.setStyle(cell, 'border-right', '1px solid #CCC');
                  if (cell.indexOf("A") == 0) {
                    vm.jexcel.setStyle(cell, 'border-left', '1px solid #CCC');
                  }
                  if ((inst - inspectColumns[clast].index) > 1) {
                    vm.jexcel.setMerge(cell, 1, inst - (inspectColumns[clast].index));
                  }
                  if (data.length > inst + 1) {
                    inspectColumns[clast] = { index: inst, value: data[inst][notMetricColumns[clast]] };
                  }
                }
                refInstance = actRefInstance;
                break;
              }
            }
          }
        }
      }
      else{
        vm.nonMergedData = null;
      }
    }

    vm.drawRedrawJExcel = function (data) {
      var deferred = $q.defer();

      var headers;
      if(data.length){//With data
        if(!Array.isArray(data[0])){//form array of json (from datasource)
          headers = vm.columns.list.map(function (column) {
            return { title: (!column.alias || column.alias == "" ? vm.normalizeField(column.field) : column.alias), name: vm.normalizeField(column.field), type: ((typeof data[0][column.field] == 'number' || typeof data[0][column.field] == 'integer') ? 'numeric' : 'text') }
          })
        }
        else{//from array of array (from saved data)
          headers = vm.columns.list.map(function (column,index ) {
            if(!vm.config.adjustColumnToView){
              return { title: vm.config.discovery.matrix.headers[index].name, width: vm.config.discovery.matrix.headers[index].width ,type: ((typeof data[0][column.field] == 'number' || typeof data[0][column.field] == 'integer') ? 'numeric' : 'text') }
            }
            else{//no set width, adjust to view
              return { title: vm.config.discovery.matrix.headers[index].name ,type: ((typeof data[0][column.field] == 'number' || typeof data[0][column.field] == 'integer') ? 'numeric' : 'text') }
            }
          })
        }
      }
      else{//No data, all default text or alias
        var headers = vm.columns.list.map(function (column) {
          return { title: (!column.alias || column.alias == "" ? vm.normalizeField(column.field) : column.alias), name: vm.normalizeField(column.field), type: 'text' }
        })
      }

      vm.deleteJExcel();

      var jexcelOptions = {
        data: data,
        columns: headers,
        columnDrag: vm.config.editGrid,
        columnResize: vm.config.editGrid,
        rowResize: vm.config.editGrid,
        editable: vm.config.editGrid,
        lazyLoading: false,
        search:vm.config.showSearch,
        defaultColWidth: (vm.config.adjustColumnToView?($element[0].offsetWidth-40-(vm.config.showRowNum?50:0))/(Math.max(headers.length,vm.config.baseCols)):100),
        tableOverflow: true,
        columnSorting:false,
        minDimensions: [vm.config.baseCols, vm.config.baseRows],
        tableHeight: $element[0].offsetHeight - (vm.config.showSearch?40:0) - (vm.config.editGrid?50:0) - 37 + "px",
        toolbar: (vm.config.editGrid?[
          { type: 'i', content: 'undo', onclick: function () { vm.jexcel.undo(); } },
          { type: 'i', content: 'redo', onclick: function () { vm.jexcel.redo(); } },
          { type: 'i', content: 'save', onclick: function () { vm.jexcel.download(); } },
          { type: 'select', k: 'font-family', v: ['Arial', 'Verdana'] },
          { type: 'select', k: 'font-size', v: ['9px', '10px', '11px', '12px', '13px', '14px', '15px', '16px', '17px', '18px', '19px', '20px'] },
          { type: 'i', content: 'format_align_left', k: 'text-align', v: 'left' },
          { type: 'i', content: 'format_align_center', k: 'text-align', v: 'center' },
          { type: 'i', content: 'format_align_right', k: 'text-align', v: 'right' },
          { type: 'i', content: 'format_bold', k: 'font-weight', v: 'bold' },
          { type: 'color', content: 'format_color_text', k: 'color' },
          { type: 'color', content: 'format_color_fill', k: 'background-color' }
        ]:[]),
        updateTable: customUpdateTable,
        onsort: function(a,b,c,d,e){
          
        }
      }

      if(!vm.config.editGrid){
        jexcelOptions["contextMenu"] = false
      }

      vm.jexcel = jexcel($element.find("div")[1], jexcelOptions);

      if(vm.config.showRowNum){
        document.querySelector("table.jexcel tr td:first-child").style.opacity=1;
        document.querySelector("table.jexcel").style.marginLeft="0px";
      }
      else{
        document.querySelector("table.jexcel tr td:first-child").style.opacity=0;
        document.querySelector("table.jexcel").style.marginLeft="-50px";
      }

      //Set context menú parent to dashbaord in order to set right position
      var contextmenu = document.querySelector(".jexcel_contextmenu");
      document.getElementsByTagName("body")[0].appendChild(contextmenu);

      document.querySelectorAll(".jexcel thead tr td").forEach(
        function(elem){
          elem.ondblclick = function(){
            vm.avoidReload = true;
            vm.columns.list[this.getAttribute("data-x")].asc = !vm.columns.list[this.getAttribute("data-x")].asc;
            vm.redrawView();
          }
        }
      )

      //Solve promise
      deferred.resolve(data);
      return deferred.promise;
    }

    function customUpdateTable(instance, cell, col, row, val, label, cellName){
      if(vm.blockUpdateTable){
        return false;
      }
      var conds;
      if(vm.columns.list[col]){
        var conds = vm.columns.list[col].condstyles;
      }
      var i = 0;          
      var found = false;
      while(conds && i < conds.length){              
        switch(conds[i].cond){
          case "all":
            found=true;
            break;
          case "data":
            if(row-1 < vm.columns.dataAccess.total.index && vm.columns.dataAccess.subTotals.indexes.indexOf(row) === -1){
              found=true;
            }
            break;
          case "total":
            if(row-1 === vm.columns.dataAccess.total.index){
              found=true;
            }
            break;
          case "subtotals":
            if(vm.columns.dataAccess.subTotals.indexes.indexOf(row) !== -1){
              found=true;
            }
            break;
          case "equal":
            if(val == conds[i].val){
              found=true;
            }
            break;
          case "mayorequal":
            if(val >= conds[i].val){
              found=true;
            }
            break;
          case "minorequal":
            if(val <= conds[i].val){
              found=true;
            }
            break;
          case "mayor":
            if(val > conds[i].val){
              found=true;
            }
            break;
          case "minor":
            if(val < conds[i].val){
              found=true;
            }
            break;
          case "between":
            if(val >= conds[i].val && val <= conds[i].val2){
              found=true;
            }
            break;
          case "in":
            if(conds[i].val.indexOf(val) != -1){
              found=true;
            }
            break;
        }
        if(found){
          cell.style.cssText = cell.style.cssText + ";" + conds[i].style;
          if(conds[i].vfunction){
            var f = eval("(" + vm.preprocessFunction(conds[i].vfunction, instance, cell, col, row, val, label, cellName) + ")");
            cell.innerHTML = f(val, col, row, instance, cell, label, cellName);
          }
          found = false;
        }        
        i++;
      }      
    }

    vm.preprocessFunction =function(f, instance, cell, col, row, val, label, cellName){
      //replace $total
      if(vm.config.enableTotal){
        
        //replace $total(col)
        var regexp = new RegExp("\\$total\\((.+?)\\)","g");
        do
        {
          var match = regexp.exec(f);
          if(match){
            var innerRegexp = new RegExp("\\$total\\(" + match[1] + "\\)","g")
            f = f.replace(innerRegexp, instance.jexcel.getColumnData(vm.getColumnIndex(match[1]))[vm.columns.dataAccess.total.index+1]);
          }
        }
        while(match)
        
        var regexp = new RegExp("\\$total","g");
        f = f.replace(regexp, instance.jexcel.getColumnData(col)[vm.columns.dataAccess.total.index+1]);
      }

      //replace $subtotal(col)
      var regexp = new RegExp("\\$subtotal\\((.+?)\\)","g");
      do
      {
        var match = regexp.exec(f);
        if(match){
          var innerRegexp = new RegExp("\\$subtotal\\(" + match[1] + "\\)","g")
          f = f.replace(innerRegexp, instance.jexcel.getColumnData(vm.getColumnIndex(match[1]))[vm.columns.dataAccess.subTotals.indexes.filter(function(e){return e > row})[0]]);
        }
      }
      while(match)

      //replace $subtotal
      var regexp = new RegExp("\\$subtotal","g");
      f = f.replace(regexp, instance.jexcel.getColumnData(col)[vm.columns.dataAccess.subTotals.indexes.filter(function(e){return e > row})[0]]);

      return f;
    }

    vm.isSubTotalRow = function(row){
      return vm.columns.dataAccess.subTotals.indexes.indexOf(row) != -1;
    }

    vm.isSubTotalRowKey = function(row, key){
      return vm.columns.dataAccess.subTotals.indexes.indexOf(row) != -1;
    }

    vm.isTotalRow = function(row){
      return vm.columns.dataAccess.total.index === row;
    }

    vm.resizeJExcel = function(){
      if(vm.columns.list && vm.columns.list.length && vm.status === "ready"){
        $timeout(
          function(){
            vm.drawRedrawJExcel(vm.nonMergedData?vm.nonMergedData:vm.jexcel.getData());
            vm.mergeByColumnAttr();
          },300
        )
      }
    }

    vm.normalizeField = function(f){
      return f.replace(/\./g, "_").replace(/\[/g, "_").replace(/\]/g, "")
    }

    vm.generateSortHighPreferenceSubtotalsFields = function(){
      var sortSubtotalHighPreference = [];
      var notSubtotalFields = [];
      var userSort = vm.getSortFields();
      var subtotals = vm.getSubTotalFields();
      for(var i=0;i<userSort.length;i++){
        if(subtotals.indexOf(userSort[i].field) != -1){
          sortSubtotalHighPreference.push(userSort[i]);
        }
        else{
          notSubtotalFields.push(userSort[i]);
        }
      }
      return sortSubtotalHighPreference.concat(notSubtotalFields);
    }

    vm.sendQueryTotals = function () {
      return vm.from(vm.datasource.identification).filter(getDataStatusFilters()).select(vm.getSelectMetrics()).exec()
    }

    vm.sendQuerySubTotals = function () {
      return vm.from(vm.datasource.identification).filter(getDataStatusFilters()).group(vm.getSubTotalFields()).select(vm.getSelectMetrics().concat(vm.getSubTotalFields().map(function(f){return {"field":f,"alias":vm.normalizeField(f)}}))).sort(vm.getSortFields().filter(function(sfield){return vm.getSubTotalFields().indexOf(sfield.field) != -1})).exec()
    }

    vm.sendQuery = function () {
      return vm.from(vm.datasource.identification).filter(getDataStatusFilters()).group(vm.getGroupFields()).select(vm.getSelectFields()).sort(vm.generateSortHighPreferenceSubtotalsFields()).exec();
    }

    function getDataStatusFilters(){
      var filters = (vm.datastatus && vm.datastatus.length)?datasourceSolverService.buildFilterStt({id:vm.id,data:vm.datastatus})["filter"]["data"]:[];
      filters.concat(urlParamService.generateFiltersForGadgetId(vm.id))
      //Add initial datalink
      filters = interactionService.generateFiltersForGadgetIdWithDatastatus(vm.id, addDatastatus, filters);
      return filters;      
    }

    function eventDProcessor(event, dataEvent) {
      if (dataEvent.type === "data" && dataEvent.data.length === 0) {
        //Do nothing
      }
      else {
        switch (dataEvent.type) {
          case "data":
            //Do nothing datadiscovery solve it own datasource
            break;
          case "filter":
            if (!vm.datastatus) {
              vm.datastatus = [];
            }
            if (dataEvent.data.length) {
              for (var index in dataEvent.data) {
                addDatastatus(dataEvent, index);
              }

            }
            else {
              deleteDatastatus(dataEvent);
            }

            if (typeof vm.datastatus === 'undefined') {
              dataEvent.data = [];
            } else {
              dataEvent.data = vm.datastatus.filter(function (elem) { return elem.id === dataEvent.id });
            }

            vm.redrawView();
            break;
          case "action":
            //Do nothing
            break;
          case "value":
            //Do nothing
            break
          default:
            console.error("Not allowed event: " + dataEvent.type);
            break;
        }
      }
      utilsService.forceRender($scope);
    }

    function addDatastatus(dataEvent, index) {

      if (!vm.datastatus) {
        vm.datastatus = [];

        vm.datastatus.push({
          field: dataEvent.data[index].field,
          value: angular.copy(dataEvent.data[index].value),
          id: angular.copy(dataEvent.id),
          op: angular.copy(dataEvent.data[index].op),
          idFilter: angular.copy(dataEvent.data[index].idFilter),
          name: angular.copy(dataEvent.data[index].name)
        });
      } else {
        var exist = false;
        for (var i = 0; i < vm.datastatus.length; i++) {
          var element = vm.datastatus[i];
          if (vm.datastatus[i].idFilter === dataEvent.data[index].idFilter
            && vm.datastatus[i].op === dataEvent.data[index].op) {
            vm.datastatus[i] = {
              field: dataEvent.data[index].field,
              value: angular.copy(dataEvent.data[index].value),
              id: angular.copy(dataEvent.id),
              op: angular.copy(dataEvent.data[index].op),
              idFilter: angular.copy(dataEvent.data[index].idFilter),
              name: angular.copy(dataEvent.data[index].name)
            }
            exist = true;
            break
          }

        }
        if (!exist) {
          vm.datastatus.push({
            field: dataEvent.data[index].field,
            value: angular.copy(dataEvent.data[index].value),
            id: angular.copy(dataEvent.id),
            op: angular.copy(dataEvent.data[index].op),
            idFilter: angular.copy(dataEvent.data[index].idFilter),
            name: angular.copy(dataEvent.data[index].name)
          })
        }
      }
      vm.datastatus = vm.datastatus.filter(function(s){return s.value!==null;});
      
    }

    function deleteDatastatus(dataEvent) {
      if (typeof vm.datastatus !== 'undefined') {
        for (var index = 0; index < vm.datastatus.length; index++) {
          var element = vm.datastatus[index];
          if (typeof dataEvent.op !== 'undefined' && dataEvent.op !== null && typeof element.op !== 'undefined' && element.op !== null) {
            if (element.field === dataEvent.field && element.id === dataEvent.id && element.op === dataEvent.op) {
              vm.datastatus.splice(index, 1);
            }
          } else {
            if (element.field === dataEvent.field && element.id === dataEvent.id) {
              vm.datastatus.splice(index, 1);
            }
          }

        }
        if (vm.datastatus.length === 0) {
          vm.datastatus = undefined;
        }
      }
    }
  }

})();
(function () {
  'use strict';

  angular.module('dashboardFramework')
    .controller(
      'editTemplateParamsController',
      ["$scope", "__env", "$mdDialog", "$mdCompiler", "httpService", "type", "config", "layergrid", "edit", "element", "utilsService", function ($scope,__env, $mdDialog,$mdCompiler, httpService, type, config, layergrid, edit, element, utilsService) {
        var agc = this;
        agc.$onInit = function () {
          $scope.loadDatasources();
          $scope.getPredefinedParameters($scope.config.content);
          $scope.getPredefinedParameters($scope.config.contentcode);
          if($scope.edit){
            for(var i=0;i < $scope.element.params.length;i++){
              var founds = $scope.parameters.filter(function(par){
                return par.label ==  $scope.element.params[i].label && par.type ==  $scope.element.params[i].type;
              })
              if(founds.length == 1){
                founds[0].value = $scope.element.params[i].value;
              }
            }
            if($scope.element.datasource){
              config.datasource = $scope.element.datasource;
            }
            $scope.loadDatasourcesFields();
          }
        }
       
        $scope.type = type;
        $scope.config = config;
        $scope.element = element;
        $scope.layergrid = layergrid;
        $scope.edit = edit;
        $scope.datasource;
        $scope.datasources = [];
        $scope.datasourceFields = [];
        $scope.parameters = [];
       
        $scope.templates = [];

        $scope.hide = function() {
          $mdDialog.hide();
        };

        $scope.cancel = function() {
          $mdDialog.cancel();
        };

       
        $scope.loadDatasources = function(){
          return httpService.getDatasources().then(
            function(response){
              $scope.datasources=response.data;
              
            },
            function(e){
              console.log("Error getting datasources: " +  JSON.stringify(e))
            }
          );
        };
  
        $scope.iterate=  function (obj, stack, fields) {
          for (var property in obj) {
               if (obj.hasOwnProperty(property)) {
                   if (typeof obj[property] == "object") {
                    $scope.iterate(obj[property], stack + (stack==""?'':'.') + property, fields);
            } else {
                       fields.push({field:stack + (stack==""?'':'.') + property, type:typeof obj[property]});
                   }
               }
            }    
            return fields;
         }

        

        /**we look for the parameters in the source code to create the form */
        $scope.getPredefinedParameters = function(str){
          var regexTagHTML =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\-->/g;
          var regexTagJS =  /\/\*[\-\-\s\w\>\=\"\'\,\:\+\_\/]*\*\//g;
          var regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;
          var regexOptions = /options\s*=\s*\"[\s\w\>\=\-\'\:\,\+\_\/]*\s*\"/g;
          var found=[];
          found = utilsService.searchTag(regexTagHTML,str).concat(utilsService.searchTag(regexTagJS,str));	
          
          found.unique=function unique (a){
            return function(){return this.filter(a)}}(function(a,b,c){return c.indexOf(a,b+1)<0
           }); 
          found = found.unique(); 
      
          for (var i = 0; i < found.length; i++) {			
            var tag = found[i];
            if(tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){	
              $scope.parameters.push({label:utilsService.searchTagContentName(regexName,tag),value:"parameterTextLabel", type:"labelsText"});
            }else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
              $scope.parameters.push({label:utilsService.searchTagContentName(regexName,tag),value:0, type:"labelsNumber"});              
            }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
              $scope.parameters.push({label:utilsService.searchTagContentName(regexName,tag),value:"parameterDsLabel", type:"labelsds"});               
            }else if(tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
              $scope.parameters.push({label:utilsService.searchTagContentName(regexName,tag),value:"parameterNameDsLabel", type:"labelsdspropertie"});               
            }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-osp')>=0){
              var optionsValue = utilsService.searchTagContentOptions(regexOptions,tag); 
              $scope.parameters.push({label:utilsService.searchTagContentName(regexName,tag),value:"parameterSelectLabel",type:"selects", optionsValue:optionsValue});	              
            }
           } 
          }       
        
        $scope.loadDatasourcesFields = function(){
          
          if($scope.config.datasource!=null && $scope.config.datasource.id!=null && $scope.config.datasource.id!=""){
               return httpService.getsampleDatasources($scope.config.datasource.id).then(
                function(response){
                  $scope.datasourceFields=$scope.iterate(response.data[0],"", []);
                },
                function(e){
                  console.log("Error getting datasourceFields: " +  JSON.stringify(e))
                }
              );
            }
            else 
            {return null;}
      }


        $scope.save = function() {
          if(!edit){
            $scope.config.type = $scope.type;
            if($scope.config.template){// ID mode, save init params (edit only params)
              $scope.config.params = $scope.parameters;
            }
            else{ // edit code mode (no id reference) 
              $scope.config.content=utilsService.parseProperties($scope.config.content,$scope.parameters);         
              $scope.config.contentcode=utilsService.parseProperties($scope.config.contentcode,$scope.parameters,true);
            }
            $scope.layergrid.push($scope.config);
          }
          else{ // only edit params (ID mode)
            $scope.element.params = $scope.parameters;
            $scope.element.datasource = $scope.config.datasource;
          }
          $mdDialog.cancel();
        };
      
      }]
    )
  
})();
(function () {
  'use strict';

  angular.module('dashboardFramework')
    .controller(
      'baseTemplateController',
      ["$rootScope", "$scope", "$element", "$mdCompiler", "datasourceSolverService", "httpService", "interactionService", "utilsService", "urlParamService", "filterService", "$translate", "$window", "cacheBoard", function($rootScope, $scope, $element, $mdCompiler, datasourceSolverService, httpService, interactionService, utilsService, urlParamService, filterService, $translate, $window, cacheBoard){

        /*
        From subcontrollers in vm

        id: "=?",
        livecontent: "<",
        livecontentcode: "<",
        datasource: "<",
        datastatus: "=?",
        filters: "=",
        custommenuoptions: "=?",
        showonlyfiltered: "=?"

        */

        var vm = $scope.vm;
        $scope.ds = [];
        $scope.ds_old = [];
        vm.status = "initial";
        vm.init = false;
    
        vm.$onInit = function () {
          //register Gadget in interaction service when gadget has id
          if (vm.id) {
            interactionService.registerGadget(vm.id);
          }
          //Activate incoming events
          vm.unsubscribeHandler = $scope.$on(vm.id, vm.eventLProcessor);
    
          if(typeof vm.datasource !== 'undefined' && vm.datasource.length>0){
            httpService.getDatasourceById(vm.datasource.id).then(
              function(datasource){            
                vm.datasource.refresh = datasource.data.refresh;
                refreshSubscriptionDatasource(vm.datasource);
                if(!vm.loadSended){
                  window.dispatchEvent(new CustomEvent('gadgetloaded', { detail: vm.id }));
                  vm.loadSended = true;
                }
              });
           
          
          }else{
            refreshSubscriptionDatasource(vm.datasource);
            if(!vm.loadSended){
              window.dispatchEvent(new CustomEvent('gadgetloaded', { detail: vm.id }));
              vm.loadSended = true;
            }
          }
         
          vm.init = true;

          if(vm.template){
            var templatedata = (cacheBoard.gadgetTemplates?cacheBoard.gadgetTemplates.filter(function(g){return g.identification == vm.template})[0]:null);
            if(templatedata){
              vm.livecontent=utilsService.parseProperties(templatedata.template,vm.params);         
              vm.livecontentcode=utilsService.parseProperties(templatedata.templateJS,vm.params,true);
              vm.compileContent();
            }
            else{
              httpService.getGadgetTemplateByIdentification(vm.template).then(
                function(data){
                  if(!cacheBoard.gadgetTemplates){
                    cacheBoard.gadgetTemplates=[]
                  }
                  cacheBoard.gadgetTemplates.push(data.data);
                  vm.livecontent=utilsService.parseProperties(data.data.template,vm.params);         
                  vm.livecontentcode=utilsService.parseProperties(data.data.templateJS,vm.params,true);
                  vm.compileContent();
                }
              );
            }
          }
          else{
            vm.compileContent();
          }
          
          $scope.$on("$resize", vm.resizeEvent);
          setTimeout(function () {
            try {
              $("gridster").animate({ scrollTop: 0 }, 0);
            } catch (error) {
            }
          }, 1);
        }
    
    
    
    
        //Only with livehtml code, for resize custom library purposes
        vm.resizeEvent = function () {
    
        }
    
    
        $scope.parseDSArray = function (name) {
          var result = [];
          var properties = [];
          if (typeof name != "undefined" && name != null) {
            try {
              for (var propertyName in $scope.ds[0]) {
                properties.push(propertyName);
              }
              if (properties.indexOf(name) > -1) {
                for (var index = 0; index < $scope.ds.length; index++) {
                  result.push($scope.ds[index][name]);
                }
              }
    
            } catch (error) {
    
            }
          }
          return result;
        }
    
        vm.loadScriptAsynchronized = function (script, callback) {
          var filetype = script.substring(script.lastIndexOf(".") + 1).toLowerCase()
          if (filetype == "js") { //if filename is a external JavaScript file
            var fileref = document.createElement('script')
            fileref.setAttribute("type", "text/javascript")
            fileref.onload = callback;
            fileref.setAttribute("src", script)
          } else if (filetype == "css") { //if filename is an external CSS file
            var fileref = document.createElement("link")
            fileref.setAttribute("rel", "stylesheet")
            fileref.setAttribute("type", "text/css")
            fileref.onload = callback;
            fileref.setAttribute("href", script)
          }
          if (typeof fileref != "undefined") {
            document.getElementsByTagName("head")[0].appendChild(fileref)
          }
        }
    
        vm.refreshcontentFromCache = function(){
          if(vm.template){//only for id reference template
            var templatedata = (cacheBoard.gadgetTemplates?cacheBoard.gadgetTemplates.filter(function(g){return g.identification == vm.template})[0]:null);
            vm.livecontent=utilsService.parseProperties(templatedata.template,vm.params);         
            vm.livecontentcode=utilsService.parseProperties(templatedata.templateJS,vm.params,true);
          }
        }
    
        vm.$onChanges = function (changes, c, d, e) {
          if ("datasource" in changes && changes["datasource"].currentValue && vm.init) {
            refreshSubscriptionDatasource(changes.datasource.currentValue, changes.datasource.previousValue);
          }
          if (
              (changes === "FORCE_COMPILE") ||
              (typeof changes != "undefined" && typeof changes.livecontentcode != "undefined" && !changes.livecontentcode.isFirstChange()) ||
              (typeof changes != "undefined" && typeof changes.livecontent != "undefined" && !changes.livecontent.isFirstChange()) ||
              (typeof changes != "undefined" && typeof changes.params != "undefined" && !changes.params.isFirstChange()) 
          ) {
            vm.refreshcontentFromCache();
            vm.compileContent();
          }
        };
    
    
    
    
        $scope.getTime = function () {
          var date = new Date();
          return date.getTime();
        }
    
    
        $scope.sendFilter = function (field, value, op) {
          var filterStt = {};
          if (typeof op === 'undefined') {
            op = "="
          }
          filterStt[field] = {
            value: value,
            id: vm.id,
            op: op
          };
          interactionService.sendBroadcastFilter(vm.id, filterStt);
        }
    
        vm.sendFilter = $scope.sendFilter;
    
        vm.sendFilters = function () {
          filterService.sendFilters(vm.id, vm.filters);
        }
    
        //Function to send a value, paramters target gadget and value, value can be a json for example 
        $scope.sendValue = function (topic, value) {
          var filterStt = {};
          filterStt[topic] = {
            "typeAction": "value",
            "id": topic,
            "value": value
          };
    
          interactionService.sendBroadcastFilter(vm.id, filterStt);
        }
        vm.sendValue = $scope.sendValue;
        //Function to receive values over write function to add the desired functionality when receiving a value
        $scope.receiveValue = function (data) {
    
        }
    
        vm.receiveValue = $scope.receiveValue;
    
        vm.insertHttp = function (token, clientPlatform, clientPlatformId, ontology, data) {
          httpService.insertHttp(token, clientPlatform, clientPlatformId, ontology, data).then(
            function (e) {
              console.log("OK Rest: " + JSON.stringify(e));
            }).catch(function (e) {
              console.log("Fail Rest: " + JSON.stringify(e));
            });
        }
    
        $scope.getDataFromDataSource = function (datasource, callbackF, filters, group, project, sort, limit, offset, param, debug) {
          if (typeof filters === 'undefined' || filters === null) {
            filters = [];
          }
          var id, name, dat;
          return datasourceSolverService.getDataFromDataSource({
            type: 'query',
            name: datasource,
            refresh: 0,
            triggers: [{ params: { filter: filters, group: (group ? group : []), project: (project ? project : []), sort: (sort ? sort : []), limit: (limit ? limit : -1), offset: (offset ? offset : -1), param: (param ? param : []), debug: (debug ? debug : false) }, emitTo: vm.id }]
          }, function (id, name, dat) {
            if (dat.error) {
              console.error("Error in response datasource: " + dat.data);
              dat.data = JSON.stringify({ "error": dat.data });
            }
            callbackF(dat.data, dat.error);
          }, callbackF);
        };
    
        vm.getDataFromDataSource = $scope.getDataFromDataSource;
    
        vm.get = datasourceSolverService.get;
        vm.getOne = datasourceSolverService.getOne;
        vm.from = datasourceSolverService.from;
    
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
    
        /*vm.compileContent = function (){
          if (vm.destroyLiveComponent) {
            vm.destroyLiveComponent();
          }
          eval(addSourceFile(vm.livecontentcode?vm.livecontentcode:""));
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
        }*/
    
        vm.addSourceFile = function(contentcode){
          return contentcode + "\n//# sourceURL=" + $window.location.protocol + "//" + $window.location.host + window.location.pathname + (window.location.pathname.endsWith("/")?"":"/") +  "templates/" + vm.id + ".js";
        }
    
        function refreshSubscriptionDatasource(newDatasource, oldDatasource) {      
          if (vm.unsubscribeHandler) {
            vm.unsubscribeHandler();
            vm.unsubscribeHandler = null;
            vm.unsubscribeHandler = $scope.$on(vm.id, vm.eventLProcessor);
            if(typeof oldDatasource !== 'undefined'){
              datasourceSolverService.unregisterDatasourceTrigger(oldDatasource.name, vm.id);
            }    
          }
          var filter = urlParamService.generateFiltersForGadgetId(vm.id);
          if (typeof newDatasource !== "undefined" && newDatasource !== null) {
            filterService.getInitialFilters(vm.id, vm.filters).then(function(filterResult){          
              var firtshot = true;
              if(typeof vm.showonlyfiltered !== 'undefined' && vm.showonlyfiltered){
                firtshot = false;
              }else{
                if(typeof filterResult!== 'undefined'){
                  var initials =  Object.values(filterResult).find(function(element){ return element.initialFilter ===true}) ;
                  if(typeof initials !== 'undefined'){
                    firtshot = false;
                  }
                }
              }
              //Add initial datalink
              filter = interactionService.generateFiltersForGadgetIdWithDatastatus(vm.id, vm.addDatastatus, filter);
             datasourceSolverService.registerSingleDatasourceAndFirstShot( //Raw datasource no group, filter or projections
              {
                type: newDatasource.type,
                name: newDatasource.name,
                refresh: newDatasource.refresh,
                triggers: [{
                  params: {
                    filter: filter,
                    group: [],
                    project: []
                  },
                  emitTo: vm.id
                }]
              }, firtshot , function(){ datasourceSolverService.refreshIntervalData(vm.id);})});
          }
        };
    
    
    
        vm.eventLProcessor = function(event, dataEvent) {
          if (dataEvent.type === "data" && dataEvent.data.length === 0) {
            vm.type = "nodata";
            $scope.ds = "";
            vm.drawLiveComponent($scope.ds, null);
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
                    addDatastatus(dataEvent, index);
                  }
                  dataEvent.data = dataEvent.data.filter(function(s){return s.value!==null;});
                } else {
                  deleteDatastatus(dataEvent);
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
    
        vm.addDatastatus = function(dataEvent, index) {
    
          if (!vm.datastatus) {
            vm.datastatus = [];
    
            vm.datastatus.push({
              field: dataEvent.data[index].idFilter,
              value: angular.copy(dataEvent.data[index].value),
              id: angular.copy(dataEvent.id),
              op: angular.copy(dataEvent.data[index].op),
              idFilter: angular.copy(dataEvent.data[index].idFilter),
              name: angular.copy(dataEvent.data[index].name)
            });
          } else {
            var exist = false;
            for (var i = 0; i < vm.datastatus.length; i++) {
              var element = vm.datastatus[i];
              if (vm.datastatus[i].field === dataEvent.data[index].idFilter
                && vm.datastatus[i].op === dataEvent.data[index].op) {
                vm.datastatus[i] = {
                  field: dataEvent.data[index].idFilter,
                  ownfield: dataEvent.data[index].field,
                  value: angular.copy(dataEvent.data[index].value),
                  id: angular.copy(dataEvent.id),
                  op: angular.copy(dataEvent.data[index].op),
                  idFilter: angular.copy(dataEvent.data[index].idFilter),
                  name: angular.copy(dataEvent.data[index].name)
                }
                exist = true;
                break
              }
    
            }
            if (!exist) {
              vm.datastatus.push({
                field: dataEvent.data[index].idFilter,
                ownfield: dataEvent.data[index].field,
                value: angular.copy(dataEvent.data[index].value),
                id: angular.copy(dataEvent.id),
                op: angular.copy(dataEvent.data[index].op),
                idFilter: angular.copy(dataEvent.data[index].idFilter),
                name: angular.copy(dataEvent.data[index].name)
              })
            }
          }
          vm.datastatus = vm.datastatus.filter(function(s){return s.value!==null;});     
        }
    
        vm.deleteDatastatus = function(dataEvent) { 
    
          if (typeof vm.datastatus !== 'undefined') {
            var index = vm.datastatus.length;
            while (index--) {
              var element = vm.datastatus[index];
              if ((element.ownfield === dataEvent.field || element.field === dataEvent.field) && element.id === dataEvent.id) {
                vm.datastatus.splice(index, 1);
              }
    
            }
            if (vm.datastatus.length === 0) {
              vm.datastatus = undefined;
            }
          }
        }
    
    
    
      }]
    )
  
})();
(function () {
    'use strict';

      SynopticEditorController.$inject = ["$rootScope", "$scope", "$element", "$compile", "datasourceSolverService", "httpService", "interactionService", "utilsService", "urlParamService", "filterService"];
    angular.module('dashboardFramework')
      .component('synopticeditor', {
        templateUrl: 'app/components/view/synopticEditorComponent/synopticEditor.html',
        controller: SynopticEditorController,
        controllerAs: 'vm',
        bindings: {          
          synoptic: "=?",
          config: "<?",
          dashboardheader: "<?",
          synopticinit: "<?",
          iframe: "=?",
          imagelib: "<?"
        }
      }); 

    /** @ngInject */
    function SynopticEditorController($rootScope, $scope, $element, $compile, datasourceSolverService, httpService, interactionService, utilsService, urlParamService, filterService) {
      var vm = this;

  

      vm.datasources = new Map();


      
      vm.$onInit = function () {
        //Charge datasources with fields
        if(!vm.iframe){
            httpService.getDatasources().then(
              function(response){
                for(var i=0;i<response.data.length;i++){
                  loadFields(response.data[i].identification,response.data[i].id);
                }
              },
              function(e){
                console.log("Error getting datasources: " +  JSON.stringify(e))
              }
            );
          }
          //$('gridster').hide();
        }

        function loadFields(identification,id){
          httpService.getFieldsFromDatasourceId(id).then(
            function(data){
              vm.datasources.set(identification,utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", [])))  ;
            }
          )
        }

       vm.initsvgImage = function () {
       
          
        
/**  Conditions example
          vm.conditions = new Map();

          vm.conditions.set('svg_1', {
            identification: 'rectangle',
            datasource: 'helsinki',
            field: 'Helsinki.year',
            class: 'indicator',
            elementAttr: 'fill',
            color: {
              colorOn: '#aaff00',
              colorOff: '#ff0000',
              cutValue: '2'
            }
          });
  
          vm.conditions.set('svg_2', {
            identification: 'circle',
            datasource: 'helsinki',
            field: 'Helsinki.population_women',
            class: 'indicator',
            elementAttr: 'fill',
            color: {
              colorOn: '#aaff00',
              colorOff: '#ff0000',
              cutValue: '113710'
            }
          });
*/  
          //TODO catch window size and put on svg initial image
          //initialize synoptic 
          if(typeof vm.synoptic === 'undefined'){
           
            var width = 1280;
            var height = 960;
            if(typeof vm.synopticinit !== 'undefined' ){ 
               width = vm.synopticinit.width;
               height = vm.synopticinit.height;
            }

            vm.synoptic =  {
              svgImage:             
              '<svg width="'+width+'" height="'+height+'" xmlns="http://www.w3.org/2000/svg" xmlns:svg="http://www.w3.org/2000/svg">'+            
              ' <g class="layer">'+
              ' <title>Layer 1</title>'+
              ' </g>'+
              '</svg>'
              ,
              conditions:[]
             };
          }
          
            /*
          "imagelib":[{"name":"group1","description":"group1","content":[{"title":"imagen a","link":"http://a...."},{"title":"imagen b","link":"http://b...."}]},
           {"name":"group2","description":"group2","content":[{"title":"imagen c","link":"http://a...."},{"title":"imagen d","link":"http://b...."}]} ]
          */

          vm.editor = $("#synoptic_editor")[0];
          vm.editor.contentWindow.svgEditor.canvas.setSvgString(vm.synoptic.svgImage);
          vm.editor.contentWindow.svgEditor.setConditions(new Map(vm.synoptic.conditions));
          vm.editor.contentWindow.svgEditor.setImageLibrary(vm.imagelib);
          vm.editor.contentWindow.svgEditor.setIsIframe(vm.iframe);
          if(!vm.iframe){
            vm.editor.contentWindow.svgEditor.setDatasources(vm.datasources);
          }
       };
       window.initsvgImage = function(){
        vm.initsvgImage();
      }
       
      }
    })();
(function () {
    'use strict';

      SynopticController.$inject = ["$rootScope", "$scope", "$element", "$compile", "datasourceSolverService", "httpService", "interactionService", "utilsService", "urlParamService", "filterService"];
    angular.module('dashboardFramework')
      .component('synoptic', {
        templateUrl: 'app/components/view/synopticComponent/synoptic.html',
        controller: SynopticController,
        controllerAs: 'vm',
        bindings: {
          synoptic: "=?",
          backgroundcolorstyle:"<?"
        }
      });

    /** @ngInject */
    function SynopticController($rootScope, $scope, $element, $compile, datasourceSolverService, httpService, interactionService, utilsService, urlParamService, filterService) {
      var vm = this;

      vm.config = new Map();
      var SYNOPTIC_ = 'synoptic_';
      var SYNOPTIC = 'synoptic_';
      vm.synopticFunctions = {};
      vm.$onInit = function () {  
        //Init background color
        if(typeof vm.backgroundcolorstyle === 'undefined'){
          $('html').css("background-color", "hsl(0, 0%, 100%)");
          $('body').css("background-color", "hsl(0, 0%, 100%)");
        }else{
          $('html').css("background-color",  vm.backgroundcolorstyle);
          $('body').css("background-color",  vm.backgroundcolorstyle);
        }
       

          if(typeof vm.synoptic!=='undefined'){
              vm.config = new Map(vm.synoptic.conditions);
              angular.element( document.querySelector( '#synopticbody' ) ).empty();

             var  parsesvgImage = vm.synoptic.svgImage.split("xlink:").join(" target=\"_blank\" ");

              
              document.getElementById('synopticbody').innerHTML = parsesvgImage;  
             
              //$('gridster').show();
              
              $('svg g title')[0].innerHTML='';    
                //connect to datasources
              createDatasourceHash();
              createClickEvents();
              interactionService.registerGadget(SYNOPTIC);
              $scope.$on(SYNOPTIC, eventSyMessageProcessor);
          }

          $('#synopticbody > svg   title').each(function() {
            $(this)[0].innerHTML='';
          });
        }



        function createDatasourceHash() {
          vm.datasources = [];
          if (vm.config.size > 0) {
            vm.config.forEach(function (value, key) {
              if (!vm.datasources.includes(value.datasource)) {
                vm.datasources.push(value.datasource);
              }
            });
            establishConnectionsDatasources();
          }
        }

        function establishConnectionsDatasources() {

          for (var i = 0; i < vm.datasources.length; i++) {
            if(typeof vm.datasources[i]!== 'undefined' && vm.datasources[i].length>0){
            $scope.$on(SYNOPTIC_ + vm.datasources[i], eventSyProcessor);
              datasourceSolverService.registerSingleDatasourceAndFirstShot( //Raw datasource no group, filter or projections
                {
                  type: 'query',
                  name: vm.datasources[i],
                  refresh: 5,
                  triggers: [{
                    params: {
                      filter: [],
                      group: [],
                      project: []
                    },
                    emitTo: SYNOPTIC_ + vm.datasources[i]
                  }]
                }, true)
            }
          }
        }

       function parseLabel(dataVal){
        var result=dataVal;
        if(typeof dataVal !== 'undefined' && dataVal!== null){
          if(!isNaN(dataVal)){
            var tempNum = parseFloat(dataVal);
            tempNum=+tempNum.toFixed(2);
            result = tempNum;
          }
        }
        return result;
       }

        function eventSyProcessor(event, dataEvent) {
          console.log('eventSyProcessor in');
          console.log(event);
          console.log(dataEvent);

          var datasource = event.name.substring(9, event.name.length);
          console.log(datasource);
          if (typeof vm.config != 'undefined' && vm.config != null) {
            vm.config.forEach(function (value, key) {
              if (value.datasource === datasource) {
                console.log(key)
                switch (value.class) {
                  case 'label':
                      var dataVal = utilsService.getJsonValueByJsonPath(dataEvent.data[0], utilsService.replaceBrackets(value.fieldAtt), 0);
                    if (typeof dataVal !== 'undefined' && dataVal != null) {
                      
                      var resulWithUnitsOfMeasure = parseLabel(dataVal);
                      if(typeof value.unitsOfMeasure !== 'undefined' && value.unitsOfMeasure !== null && value.unitsOfMeasure.length>0){
                        resulWithUnitsOfMeasure = resulWithUnitsOfMeasure+' '+value.unitsOfMeasure;
                      }
                      $("#" + key).text(resulWithUnitsOfMeasure);
                    }
                    break;
                  case 'indicator':

                    break;
                  case 'progress_bar':
                      var dataVal = utilsService.getJsonValueByJsonPath(dataEvent.data[0], utilsService.replaceBrackets(value.fieldAtt), 0);
                    if (typeof dataVal !== 'undefined' && dataVal != null) {
                      var size;

                      if (dataVal > value.condition.maxValue){
                        size = value.condition.orgSize}
                      else if (dataVal < value.condition.minValue){
                        size = 0;
                      }
                      else{
                        size = dataVal * value.condition.orgSize / (value.condition.maxValue - value.condition.minValue);
                      }
                      $("#" + key).attr(value.elementAttr, size);
                      console.log("progress_bar ","key ",key,' ',size);
                    }
                    break;
                  default:
                    break;
                }
               

                //if color defined
                if (value.color !== 'undefined' && value.color !== '') {
                  var dataVal = utilsService.getJsonValueByJsonPath(dataEvent.data[0], utilsService.replaceBrackets(value.field), 0);
                  console.log(dataVal);
                  if (typeof dataVal !== 'undefined' && dataVal != null) {
                    var color;
                    if (value.color.cutValue == "") {
                      color = (parseFloat(dataVal)) ? value.color.colorOn : value.color.colorOff;
                    } else {
                      color = (parseFloat(dataVal) > parseFloat(value.color.cutValue)) ? value.color.colorOn : value.color.colorOff;
                    }
                    $("#" + key).attr("fill", color);
                  }
                }

              }
            });
          }
        }

        function createClickEvents(){
          vm.config.forEach(function (value, key) {
            if (value.class === 'button'){
           /* var ins="{'"+conditions[key].ontologyAssetId+"':'"+conditions[key].value+"'}";
              var d = new Date();
              var ins = '{"Feed":{ "tagId":"'+conditions[key].ontologyAssetId+'","timestamp":{"$date": "'+d.toISOString()+'"},"measure":{"measure":"'+conditions[key].value+'"}}}'
              $("#"+conditions[key].elementId).click(ins,function(ins){
                insertInstance(ins.data)
              });*/
            }else if(value.class === 'switch'){
             /* var ins="{'"+conditions[key].ontologyAssetId+"':'"+conditions[key].value+"'}";
              var d = new Date();
              var ins = '{"Feed":{ "tagId":"'+conditions[key].ontologyAssetId+'","timestamp":{"$date": "'+d.toISOString()+'"},"measure":{"measure":"<measure>"}}}'
              conditions[key].value= (conditions[key].value=='1')?0:1;
              data={ins:ins,val:conditions[key].value}
              $("#"+conditions[key].elementId).click(data, function(data){
                var ins = data.data.ins.replace('<measure>',data.data.val%2);
                data.data.val++;
                insertInstance(ins)
              });*/
            }
        
            if(value.events){
              for(var e in value.events){
                (function(index,valu,ke){
                  try{                  
                   $("#"+ke).on(index,function(){eval(valu[index])});                 
                  }catch(err){console.log(err)}
                })(e,value.events,key);
              }
            }
          });
        }


        $scope.sendFilter = function (field, value, op) {
          var filterStt = {};
          if (typeof op === 'undefined') {
            op = "="
          }
          filterStt[field] = {
            value: value,
            id: 'synoptic',
            op: op
          };
          interactionService.sendBroadcastFilter('synoptic', filterStt);
        }
    
        vm.sendFilter = $scope.sendFilter;
     
       vm.sendFilters =  function () {
        filterService.sendFilters('synoptic', vm.filters);
      }
    
    //Function to send a value, parameters target gadget and value, value can be a json for example 
        $scope.sendValue = function (topic, value) {
          var filterStt ={};
            filterStt[topic] = {
            "typeAction": "value", 
            "id": topic, 
            "value": value
          };
    
          interactionService.sendBroadcastFilter('synoptic', filterStt);     
        }
        vm.sendValue = $scope.sendValue;
      //Function to receive values over write function to add the desired functionality when receiving a value
        $scope.receiveValue = function (data) {
        
        }
     
        vm.receiveValue = $scope.receiveValue;
    
        function eventSyMessageProcessor(event, dataEvent) {
          console.log('eventSyMessageProcessor in');
          console.log(event);
          console.log(dataEvent);
        }


      

      }
    })();
(function () { 
  'use strict';

  HTML5Controller.$inject = ["$timeout", "$log", "$scope", "$element", "$mdCompiler", "$compile", "datasourceSolverService", "httpService", "interactionService", "utilsService", "urlParamService"];
  angular.module('dashboardFramework')
    .component('html5', {
      templateUrl: 'app/components/view/html5Component/html5.html',
      controller: HTML5Controller,
      controllerAs: 'vm',
      bindings:{
        id:"=?",
        livecontent:"<",
        datasource:"<"
      }
    });

  /** @ngInject */
  function HTML5Controller($timeout,$log, $scope, $element, $mdCompiler, $compile, datasourceSolverService,httpService,interactionService,utilsService,urlParamService) {
    var vm = this;
    
    vm.status = "initial";

    vm.$onInit = function(){
      compileContent();
      if(!vm.loadSended){
        window.dispatchEvent(new CustomEvent('gadgetloaded', { detail: vm.id }));
        vm.loadSended = true;
      }
    }

    vm.$onChanges = function(changes,c,d,e) {
        compileContent();
    };


    vm.$onDestroy = function(){
     
    }
    function compileContent(){
     
        
        $timeout(
          function(){
            try {
                var ifrm = document.getElementById(vm.id + "_html5");
                ifrm = (ifrm.contentWindow) ? ifrm.contentWindow : (ifrm.contentDocument.document) ? ifrm.contentDocument.document : ifrm.contentDocument; 
                ifrm.document.open(); 
                ifrm.document.write(vm.livecontent); 
                ifrm.document.close();
                console.log("Compiled html5")
          } catch (error) {        
          }
          },0);
       

       
    
    }
  
  }
})();

(function () {
  'use strict';

  PageController.$inject = ["$log", "$scope", "$mdSidenav", "$mdDialog", "datasourceSolverService"];
  angular.module('dashboardFramework')
    .component('page', {
      templateUrl: 'app/components/view/pageComponent/page.html',
      controller: PageController,
      controllerAs: 'vm',
      bindings:{
        page:"=",
        iframe:"=",
        editmode:"<",
        gridoptions:"<",
        dashboardheader:"<",
        synoptic: "=?",
        synopticedit: "<?",
        tabson:"<?",
        editbuttonsiframe:"<?",
        showfavoritesg:"<?"
      }
    });

  /** @ngInject */
  function PageController($log, $scope, $mdSidenav, $mdDialog, datasourceSolverService) {
    var vm = this;
    vm.$onInit = function () {

     var countgadgets = vm.page.layers[0].gridboard.filter(function(gadget){return typeof gadget.id != "undefined"});
      if(countgadgets.length === 0){
        window.dispatchEvent(new Event('resize'));
        window.postMessage("dashboardloaded", "*");
        if(window.self !== window.top){
          window.parent.postMessage("dashboardloaded", "*");
        }
      }
    };

    vm.$postLink = function(){

    }

    vm.$onDestroy = function(){   
    }
   
    function eventStop(item, itemComponent, event) {
      $log.info('eventStop', item, itemComponent, event);
    }

    function itemChange(item, itemComponent) {
      $log.info('itemChanged', item, itemComponent);
    }
 
    function itemResize(item, itemComponent) {
    
      $log.info('itemResized', item, itemComponent);
    }

    function itemInit(item, itemComponent) {
      $log.info('itemInitialized', item, itemComponent);
    }

    function itemRemoved(item, itemComponent) {
      $log.info('itemRemoved', item, itemComponent);
    }

    function gridInit(grid) {
      $log.info('gridInit', grid);
    }

    function gridDestroy(grid) {
      $log.info('gridDestroy', grid);
    }

    vm.prevent = function (event) {
      event.stopPropagation();
      event.preventDefault();
    };

    vm.pageStyle = function(){
      var  temp ;
      try {
        if(typeof  $('#synopticbody > svg')[0].height.baseVal.value !=='undefined' && 
        typeof  $('#synopticbody > svg')[0].width.baseVal.value !=='undefined'){
          return   {'height': $('#synopticbody > svg')[0].height.baseVal.value+'px' , 'width': $('#synopticbody > svg')[0].width.baseVal.value+'px'};
        }       
      } catch (error) {
        
      }
      
      if(vm.dashboardheader.enable){
        if(vm.tabson){
          temp = {'height': 'calc(100% - '+(vm.dashboardheader.height+50)+'px'+')'};
        }
        else{
          temp= {'height': 'calc(100% - '+vm.dashboardheader.height+'px'+')'};
        }
      }else{
        if(vm.tabson){
          temp = {'height': 'calc(100% - 50px)'};
        }
        else{
          temp= {'height': '100%'};
        }
      }
      return temp;
     }
 


  }
})();

(function () {
  'use strict';

GadgetController.$inject = ["$log", "$scope", "$element", "$interval", "$window", "$mdCompiler", "$compile", "datasourceSolverService", "httpService", "interactionService", "utilsService", "leafletMarkerEvents", "leafletData", "urlParamService", "filterService", "cacheBoard"];
  angular.module('dashboardFramework')
    .component('gadget', { 
      templateUrl: 'app/components/view/gadgetComponent/gadget.html',
      controller: GadgetController,
      controllerAs: 'vm',
      bindings:{
        id:"<?",             
        datastatus: "=?",
        filters: "="
      }
    });

  /** @ngInject */
  function GadgetController($log, $scope, $element,$interval, $window, $mdCompiler, $compile, datasourceSolverService, httpService, interactionService, utilsService, leafletMarkerEvents, leafletData, urlParamService, filterService, cacheBoard) {
    var vm = this;
    vm.ds = [];
    vm.type = "loading";
    vm.config = {};//Gadget database config
    vm.measures = [];
    vm.status = "initial";
    vm.selected = [];
    vm.notSmall=true;
    vm.showCheck = [];
    // color swatches >>> vm.swatches.global, vm.swatches.blues, vm.swatches.neutral
    vm.swatches = {};
    vm.swatches.global  = ['#FFEA7F','#FFF8D2','#F7AC6F','#FCE2CC','#E88AA2','#79C6B4','#CFEBE5','#639FCB','#C8DEED','#F7D6DF','#FDE3D4','#FEF6F0','#7874B4','#CFCEE5'];
    vm.swatches.neutral = ['#060E14','#F5F5F5','#6E767D','#A2ACB3','#D5DCE0','#F9F9FB'];
    vm.swatches.blues   = ['#2E6C99','#C0D3E0','#87BEE6','#E3EBF1','#639FCB'];
    vm.showNoData = false;
    vm.startTime = 0;




    //Chaining filters, used to propagate own filters to child elements
    vm.filterChaining=true;

    vm.$onInit = function(){
      
      //register Gadget in interaction service when gadget has id
      if(vm.id){
        interactionService.registerGadget(vm.id);
      }   
      //Activate incoming events
      vm.unsubscribeHandler = $scope.$on(vm.id,eventGProcessor);     
      $scope.reloadContent();  
    }



    $scope.reloadContent = function(){  

      function loadGadget(config){
        if(config==="" ){
          throw new Error('Gadget was deleted');
        }
        vm.config=config;            
        if(typeof vm.config.config == "string"){
          vm.config.config = JSON.parse(vm.config.config);
        }
      }

      function loadMeasures(measures){
        vm.measures = measures;

        vm.projects = [];
        for(var index=0; index < vm.measures.length; index++){
          var jsonConfig = (typeof vm.measures[index].config == "string"? JSON.parse(vm.measures[index].config):vm.measures[index].config);;
          for(var indexF = 0 ; indexF < jsonConfig.fields.length; indexF++){
            if(!utilsService.isSameJsonInArray( { op:"", field:jsonConfig.fields[indexF] },vm.projects)){
              vm.projects.push({op:"",field:jsonConfig.fields[indexF]});
            }
          }
           //add attribute for filter style marker to recover from datasource.
         if(vm.config.type=="map" && typeof vm.config.config.jsonMarkers!=undefined && vm.config.config.jsonMarkers!=null && vm.config.config.jsonMarkers.length>0){
          vm.projects.push({op:"",field:vm.config.config.markersFilter});
         }
          vm.measures[index].config = jsonConfig;
        }
        if(!utilsService.isEmptyJson(cacheBoard)){
          subscriptionDatasource(vm.measures[0].datasource, [], vm.projects, []);
        }
        else{
          httpService.getDatasourceById(vm.measures[0].datasource.id).then(
            function(datasource){
              subscriptionDatasource(datasource.data, [], vm.projects, []);
            }
          )
        }
      }

          
      /*Gadget Editor Mode*/
      if(!vm.id){
       
        if(!vm.config.config){
          return;//Init editor triggered
        }
        if(typeof vm.config.config == "string"){
          vm.config.config = JSON.parse(vm.config.config);
        }
        //vm.measures = vm.gmeasures;//gadget config
        var projects = [];
        for(var index=0; index < vm.measures.length; index++){
          var jsonConfig = (typeof vm.measures[index].config == "string"? JSON.parse(vm.measures[index].config):vm.measures[index].config);
          for(var indexF = 0 ; indexF < jsonConfig.fields.length; indexF++){
            if(!utilsService.isSameJsonInArray( { op:"", field:jsonConfig.fields[indexF] },projects)){
              projects.push({op:"",field:jsonConfig.fields[indexF]});
            }
          }
          //add attribute for filter style marker to recover from datasource.
          if(vm.config.type=="map" && typeof vm.config.config.jsonMarkers!=undefined && vm.config.config.jsonMarkers!=null && vm.config.config.jsonMarkers.length>0){
            projects.push({op:"",field:vm.config.config.markersFilter});
          }
          vm.measures[index].config = jsonConfig;
        }
        httpService.getDatasourceById(vm.ds).then(
          function(datasource){
            subscriptionDatasource(datasource.data, [], projects, []);
          }
        )
        
      }
      else{
      /*View Mode*/
        var gadgetdata = null; 
        var measuresdata = null;
        if(!utilsService.isEmptyJson(cacheBoard) && cacheBoard.gadgets && cacheBoard.gadgetMeasures){
          gadgetdata = cacheBoard.gadgets.filter(function(g){return g.id == vm.id})[0];
          measuresdata = cacheBoard.gadgetMeasures.filter(function(g){return g.gadget.id == vm.id});
        }
        if(gadgetdata && measuresdata){
          loadGadget(gadgetdata);
          loadMeasures(measuresdata);
        }
        else{
          httpService.getGadgetConfigById(
            vm.id
          ).then( 
            function(config){
              loadGadget(config.data);
              return httpService.getGadgetMeasuresByGadgetId(vm.id);
            }
          ).then(
            function(config){
              return loadMeasures(config.data);
            }
          ,function(e){
            if(e.message==='Gadget was deleted'){
                vm.type='removed'
                console.log('Gadget was deleted');
            }else{
                vm.type = 'nodata'
                console.log('Data no available'); 
            }
          })
        }
      }
    }

    vm.$onChanges = function(changes) {

    };

    vm.$onDestroy = function(){
      if(vm.unsubscribeHandler){
        vm.unsubscribeHandler();
        vm.unsubscribeHandler=null;
        datasourceSolverService.unregisterDatasourceTrigger(vm.measures[0].datasource.identification,vm.id);
      }
      
    }

    vm.toggleDecapite = function(){
      vm.config.config.tablePagination.options.decapitate = !vm.config.config.tablePagination.options.decapitate; 
    }

    vm.getValueOrder =  function (path) {
      return function (item) {
        var index="";
        var value="";
        if(typeof item !== "undefined" && Object.keys(item).length>0){
          if(typeof vm.config.config.tablePagination.order !== "undefined" && vm.config.config.tablePagination.order.charAt(0) === '-'){
            index=vm.config.config.tablePagination.order.substring(1,vm.config.config.tablePagination.order.length);
          }else if(typeof vm.config.config.tablePagination.order !== "undefined" && vm.config.config.tablePagination.order.charAt(0) !== '-'){
            index=vm.config.config.tablePagination.order.substring(0,vm.config.config.tablePagination.order.length)
          }else{
            index = Object.keys(item)[0];
          }
          value = item[index];
        }
        return value;
      }
    };

    function subscriptionDatasource(datasource, filter, project, group) {
      
      //Add parameters filters
      filter = urlParamService.generateFiltersForGadgetId(vm.id);
      //Add initial datalink
      filter = interactionService.generateFiltersForGadgetIdWithDatastatus(vm.id, addDatastatus, filter);
      
      filterService.getInitialFilters(vm.id, vm.filters, datasourceSolverService.registerSingleDatasourceAndFirstShot( //Raw datasource no group, filter or projections
        {
          type: datasource.mode,
          name: datasource.identification,
          refresh: datasource.refresh,
          triggers: [{
            params: {
              filter: filter,
              group: [],
              project: []
            },
            emitTo: vm.id
          }]
        },true));
         
    
    };

    function processDataToGadget(data){ //With dynamic loading this will change
      
      switch(vm.config.type){
        case "line":
        case "bar":
        case "radar":
        case "pie":
          //Group X axis values
          var allLabelsField = [];
          for(var index=0; index < vm.measures.length; index++){
            allLabelsField = allLabelsField.concat(data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[index].config.fields[0]),ind)}));
          }
          if((typeof vm.config.config.scales === "undefined")||(typeof vm.config.config.scales["xAxes"][0].sort === "undefined")|| 
           (typeof vm.config.config.scales["xAxes"][0].sort) !== "undefined" && vm.config.config.scales["xAxes"][0].sort){
              allLabelsField = utilsService.sort_unique(allLabelsField);
           }else{
              allLabelsField = utilsService.uniqueArray(allLabelsField);
           }
          //Match Y values
          var allDataField = [];//Data values sort by labels
          for(var index=0; index < vm.measures.length; index++){
            var dataRawSerie = data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[index].config.fields[1]),ind)});
            var labelRawSerie = data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[index].config.fields[0]),ind)});
            var sortedArray = [];
            for(var indexf = 0; indexf < dataRawSerie.length; indexf++){
              sortedArray[allLabelsField.indexOf(labelRawSerie[indexf])] = dataRawSerie[indexf];
            }
            allDataField.push(sortedArray);
          }

          vm.labels = allLabelsField;
          vm.series = vm.measures.map (function(m){return m.config.name});

          if(vm.config.type == "pie"){
            vm.data = allDataField[0];
          }
          else{
            vm.data = allDataField;
          }
        
          
          var baseOptionsChart = {           
            legend: {
                display: true, 
                fullWidth: false,
                position: 'top',      
                labels: {
                  padding: 10, 
                  fontSize: 11,
                  usePointStyle: false,
                  boxWidth:1
                }
              },
            elements: {
                arc: {
                    borderWidth: 1,
                    borderColor: '#fff'
                }
            },          
            maintainAspectRatio: false, 
            responsive: true, 
            responsiveAnimationDuration:500,
            circumference:  Math.PI,
            rotation: Math.PI,
            charType: 'pie'            
          };
          
          vm.datasetOverride = vm.measures.map (function(m){return m.config.config});
          vm.optionsChart = angular.merge({},vm.config.config,baseOptionsChart);
        

        // CONFIG FOR PIE/DOUGHNUT CHARTS
        if(vm.config.type == "pie"){

            try {
              // update legend display
              if( vm.config.config.legend.display !== undefined){ vm.optionsChart.legend.display = vm.config.config.legend.display;  } 

              // update data position 
              if( vm.config.config.legend.position !== undefined){ vm.optionsChart.legend.position = vm.config.config.legend.position;  } 

              // update data circunference 
              if( vm.config.config.circumference !== undefined){ vm.optionsChart.circumference = Number(vm.config.config.circumference);  } 
              
              // update data rotation 
              if( vm.config.config.rotation !== undefined){ vm.optionsChart.rotation = Number(vm.config.config.rotation);  } 

            } catch (error) {    } 

            
            // MERGE TOOLTIP CALLBACK ONLY FOR PIE/DOUGHNUT CHARTS
            var tooltips =  {              
              callbacks: {
                label: function(tooltipItem, data) {
                  var total = 0;
                  data.datasets[tooltipItem.datasetIndex].data.forEach(function(element /*, index, array*/ ) {
                    total += element;
                  });
                  var value = data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
                  var percentTxt = Math.round(value / total * 100);
                  return data.labels[tooltipItem.index] + ': ' + data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index] + ' (' + percentTxt + '%)';
                }
              },
              xPadding: 10,
              yPadding: 16,
              backgroundColor: '#FFF',
              bodyFontFamily: 'Soho',
              bodyFontColor: '#555',
              displayColors: true,
              bodyFontSize: 11,
              borderWidth: 1,
              borderColor: '#CCC'              
            };
            // add tooltip to pie/doughtnut conf.
            vm.optionsChart.tooltips = tooltips;
          
        }   
         

          if(vm.config.type==="line"||vm.config.type==="bar"){   
            
            try {
              // update legend display
              if( vm.config.config.legend.display !== undefined){ vm.optionsChart.legend.display = vm.config.config.legend.display;  } 

              // update data position 
              if( vm.config.config.legend.position !== undefined){ vm.optionsChart.legend.position = vm.config.config.legend.position;  } 
             
            } catch (error) {    } 


              //Ticks options
              vm.optionsChart.scales.xAxes[0].ticks={
                callback: function(dataLabel, index) {									
                  if(typeof vm.optionsChart.scales.xAxes[0].hideLabel ==="undefined"){return index % 1 === 0 ? dataLabel : '';}
                  else{
                    return index % vm.optionsChart.scales.xAxes[0].hideLabel === 0 ? dataLabel : '';
                  }
                }
              }
              
              var linebarTooltips = {
                bodySpacing : 15,
                xPadding: 10,
                yPadding: 16,
                titleFontColor: '#6E767D',
                backgroundColor: '#F9F9FB',
                bodyFontFamily: 'Soho',
                bodyFontColor: '#555',
                displayColors: true,
                bodyFontSize: 11,
                titleMarginBottom: 8,                
                callbacks: { 
                  label: function(tooltipItem, chart){ 
                   var datasetLabel = chart.datasets[tooltipItem.datasetIndex].label || ''; 
                   return datasetLabel + ': ' + formatNumber(tooltipItem.yLabel, 0,'','');
                  } 
                 } 
              };
              
              // add tooltip to line/bar 
              vm.optionsChart.tooltips = linebarTooltips;     

            }
          break;
          case "mixed":
          //Group X axis values
          var allLabelsField = [];
          for(var index=0; index < vm.measures.length; index++){
            allLabelsField = allLabelsField.concat(data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[index].config.fields[0]),ind)}));
          }
          if((typeof vm.config.config.scales["xAxes"][0].sort === "undefined")|| 
          (typeof vm.config.config.scales["xAxes"][0].sort) !== "undefined" && vm.config.config.scales["xAxes"][0].sort){
            allLabelsField = utilsService.sort_unique(allLabelsField);
          }
          //Match Y values
          var allDataField = [];//Data values sort by labels
          for(var index=0; index < vm.measures.length; index++){
            var dataRawSerie = data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[index].config.fields[1]),ind)});
            var labelRawSerie = data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[index].config.fields[0]),ind)});
            var sortedArray = [];
            for(var ind=0; ind < vm.measures.length; ind++){
              sortedArray[ind]=null;
            }
            for(var indexf = 0; indexf < dataRawSerie.length; indexf++){
              sortedArray[allLabelsField.indexOf(labelRawSerie[indexf])] = dataRawSerie[indexf];
            }
            allDataField.push(sortedArray);
          }

          vm.labels = allLabelsField;
          vm.series = vm.measures.map (function(m){return m.config.name});

        
            vm.data = allDataField;
        

          var baseOptionsChart = {
            legend: {
              display: true, 
              labels: {
                boxWidth: 11
              }
            }, 
            maintainAspectRatio: false, 
            responsive: true, 
            responsiveAnimationDuration:500
          };

          vm.datasetOverride = vm.measures.map (function(m){
            if(m.config.config.type==='line'){
              return m.config.config;
            }else if(m.config.config.type==='bar'){
              return m.config.config;
            }else if(m.config.config.type==='points'){
              m.config.config.type= 'line';
              m.config.config.borderWidth= 0;
              if(typeof m.config.config.pointRadius ==="undefined" ||m.config.config.pointRadius<1 ){
                m.config.config.pointRadius=4;
              }
              m.config.config.showLine=false;              
              return m.config.config;
            }
            return m.config.config});
          vm.optionsChart = angular.merge({},vm.config.config,baseOptionsChart); 
         //Ticks options
          vm.optionsChart.scales.xAxes[0].ticks={
            callback: function(dataLabel, index) {									
              if(typeof vm.optionsChart.scales.xAxes[0].hideLabel ==="undefined"){return index % 1 === 0 ? dataLabel : '';}
              else{
                return index % vm.optionsChart.scales.xAxes[0].hideLabel === 0 ? dataLabel : '';
              }
            }
          } 
          //tooltips options
          vm.optionsChart.tooltips= {
            callbacks: {
                label: function(tooltipItem, data) {               
                    var label = data.datasets[tooltipItem.datasetIndex].label || '';
                    if (label) {
                        label += ': ';
                    }
                    if(!isNaN(tooltipItem.yLabel)){
                      label += tooltipItem.yLabel;
                    }else{
                      label ='';
                    }
                    return label;
                
              }
            }
        }   
          break;
        case 'wordcloud':
          //Get data in an array
          var arrayWordSplited = data.reduce(function(a,b){return a.concat(( utilsService.getJsonValueByJsonPath(b,utilsService.replaceBrackets(vm.measures[0].config.fields[0]),0)))},[])//data.flatMap(function(d){return getJsonValueByJsonPath(d,vm.measures[index].config.fields[0]).split(" ")})
          var hashWords = {};
          var counterArray = []
          for(var index = 0; index < arrayWordSplited.length; index++){
            var word = arrayWordSplited[index];
            if(word in hashWords){
              counterArray[hashWords[word]].count++;
            }
            else{
              hashWords[word]=counterArray.length;
              counterArray.push({text:word,count:1});
            }
          }

          vm.counterArray = counterArray.sort(function(a, b){
            return b.count - a.count;
          })
          redrawWordCloud();
          $scope.$on("$resize",redrawWordCloud);
          break;
        case "map":
        

          vm.center = vm.center || vm.config.config.center;
          //IF defined intervals for marker 
          if(typeof vm.config.config.jsonMarkers!=undefined && vm.config.config.jsonMarkers!=null && vm.config.config.jsonMarkers.length>0){
            var jsonMarkers = JSON.parse(vm.config.config.jsonMarkers);
            
            vm.markers = data.map(
              function(d){
                return {
                  lat: parseFloat(utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[0].config.fields[0]),0)),
                  lng: parseFloat(utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[0].config.fields[1]),1)),
  
                  message: vm.measures[0].config.fields.slice(3).reduce(
                    function(a, b){
                      return a + "<b>" + b + ":</b>&nbsp;" + utilsService.getJsonValueByJsonPath(d,b) + "<br/>";
                    }
                    ,""
                  ),
                  id: utilsService.getJsonValueByJsonPath(d,vm.measures[0].config.fields[2],2),
                  icon: utilsService.getMarkerForMap(utilsService.getJsonValueByJsonPath(d,vm.config.config.markersFilter,2),jsonMarkers),
                }
              }
            )
          
          }else{
          vm.markers = data.map(
            function(d){
              return {
                lat: parseFloat(utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[0].config.fields[0]),0)),
                lng: parseFloat(utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[0].config.fields[1]),1)),

                message: vm.measures[0].config.fields.slice(3).reduce(
                  function(a, b){
                    return a + "<b>" + b + ":</b>&nbsp;" + utilsService.getJsonValueByJsonPath(d,b) + "<br/>";
                  }
                  ,""
                ),
                id: utilsService.getJsonValueByJsonPath(d,vm.measures[0].config.fields[2],2)
               
              }
            }
          )
        }

          $scope.events = {
            markers: {
                enable: leafletMarkerEvents.getAvailableEvents(),
            }
          };
          
          //Init map events
          var eventName = 'leafletDirectiveMarker.lmap' + vm.id + '.click';
          $scope.$on(eventName, vm.clickMarkerMapEventProcessorEmitter);
          
          redrawLeafletMap();
          $scope.$on("$resize",redrawLeafletMap);
          break;
          case "table":
          vm.data=data;
          if(data.length>0){
            var listMeasuresFields=[];
            var measures = orderTable(vm.measures);
            for (var index = 0; index < measures.length; index++) {
              measures[index].config.order=  measures[index].config.fields[0];

              var tokenizer = measures[index].config.fields[0].split(".");
              var last = tokenizer[tokenizer.length-1];
              if(last.indexOf('[') > -1){
                last = last.substring(
                  last.lastIndexOf("[") + 1, 
                  last.lastIndexOf("]"));
              }
              var proyected = {order: measures[index].config.fields[0],value:last};
              listMeasuresFields.push(proyected);
              measures[index].config.last=  last;
              if(typeof measures[index].config.name === "undefined" || measures[index].config.name.trim() === "" ){
                measures[index].config.name = last;
              }
            }
            vm.data = data.map(function (data, index, array) {
              var obj={};
                for (var i = 0; i < listMeasuresFields.length; i++) {
                  obj[listMeasuresFields[i].order]=utilsService.getJsonValueByJsonPath(data,utilsService.replaceBrackets(listMeasuresFields[i].order),index);
                }
              
              return obj;           
          });   
          }          
          vm.config.config.tablePagination.limitOptions = vm.config.config.tablePagination.options.limitSelect ? [5, 10, 20, 50 ,100]  : undefined;
          redrawTable();
          $scope.$on("$resize",redrawTable);
          break;   
  }
      vm.type = vm.config.type;//Activate gadget
      utilsService.forceRender($scope);

      if(!vm.loadSended){
        window.dispatchEvent(new CustomEvent('gadgetloaded', { detail: vm.id }));
        vm.loadSended = true;
      }
    }


    function orderTable(measures){
		
      var neworder = measures.sort(function (a,b){
        var a = Number(a.config.config.position);			
        var b = Number(b.config.config.position);		
        return a-b;
      });
      return neworder;
      
    }

    function redrawWordCloud(){
      var element = $element[0];
      var height = element.offsetHeight;
      var width = element.offsetWidth;
      var maxCount = vm.counterArray[0].count;
      var minCount = vm.counterArray[vm.counterArray.length - 1].count;
      var maxWordSize = width * 0.15;
      var minWordSize = maxWordSize / 5;
      var spread = maxCount - minCount;
      if (spread <= 0) spread = 1;
      var step = (maxWordSize - minWordSize) / spread;
      vm.words = vm.counterArray.map(function(word) {
          return {
              text: word.text,
              size: Math.round(maxWordSize - ((maxCount - word.count) * step)),
              tooltipText: word.count + ' ocurrences'
          }
      })
      vm.width = width;
      vm.height = height;
    }

   

    function redrawTable(){
     var element = $element[0];   
      var width = element.offsetWidth;
      
      if(width<600){
        vm.notSmall=false;
      }else{
        vm.notSmall=true;
      }
    }


    function redrawLeafletMap(){
      
      var element = $element[0];
      var height = element.offsetHeight;
      var width = element.offsetWidth;
      vm.width = width;
      vm.height = height;
      
    }

    function eventGProcessor(event,dataEvent){            
      if(dataEvent.type === "data" && dataEvent.data.length===0 ){
        vm.type="nodata";
        vm.status = "ready";
      }
      else{
        switch(dataEvent.type){
          case "data":
            switch(dataEvent.name){ 
              case "refresh":
                if(vm.status === "initial" || vm.status === "ready"){
                  if(vm.startTime<=dataEvent.startTime){
                      vm.startTime = dataEvent.startTime;
                      processDataToGadget(dataEvent.data);
                  }
                }
                else{
                  console.log("Ignoring refresh event, status " + vm.status);
                }
                break;
              case "add":
                //processDataToGadget(data);
                break;
              case "filter":
                if(vm.status === "pending"){                
                  if(vm.startTime <= dataEvent.startTime){
                     vm.startTime = dataEvent.startTime;
                    processDataToGadget(dataEvent.data);
                }
                  vm.status = "ready";
                }
                break;
              case "drillup":
                //processDataToGadget(data);
                break;
              case "drilldown":
                //processDataToGadget(data);
                break;
              default:
                console.error("Not allowed data event: " + dataEvent.name);
                break;
            } 
            break;
          case "filter":
            vm.status = "pending";
            //vm.type = "loading";
            if(!vm.datastatus){
              vm.datastatus = [];
            }
            if(dataEvent.data.length){
              for(var index in dataEvent.data){
                addDatastatus(dataEvent,index);
              }
              dataEvent.data = dataEvent.data.filter(function(s){return s.value!==null;});
            }
            else{
              deleteDatastatus(dataEvent); 
           
            }           
           //NEW filters
           if(typeof vm.datastatus === 'undefined'){
            dataEvent.data = [];
           }else{
            dataEvent.data = vm.datastatus.filter(function (elem){return elem.id === dataEvent.id});
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
            //do nothing
            break
          default:
            console.error("Not allowed event: " + dataEvent.type);
            break;
        }
      }
      utilsService.forceRender($scope);
    }

    
    //Chartjs click event
    vm.clickChartEventProcessorEmitter = function(points, evt){
      var originField;
      var originValue;
      if(typeof points[0]!=='undefined'){
        switch(vm.config.type){          
          case "bar":
            //find serie x field if there are diferent x field in measures
            for(var index in vm.data){
              if(vm.data[index][points[0]._index]){
                originField = vm.measures[index].config.fields[0];
                break;
              }
            }
            originValue = points[0]._model.label;
            break;
            case "line":
            case "mixed":
            //find serie x field if there are diferent x field in measures
            for(var index in vm.data){
              if(vm.data[index][points[0]._index]){
                originField = vm.measures[index].config.fields[0];
                break;
              }
            }
            originValue = points[0]._chart.config.data.labels[points[0]._index];
            break;
            case "radar":
            //find serie x field if there are diferent x field in measures
            for(var index in vm.data){
              if(vm.data[index][points[0]._index]){
                originField = vm.measures[index].config.fields[0];
                break;
              }
            }
            originValue = points[0]._chart.config.data.labels[points[0]._index];
            break;
          case "pie":
            originField = vm.measures[0].config.fields[0];
            originValue = points[0]._model.label;
            break;
        }
        sendEmitterEvent(originField,originValue);
      }
    }


 //word-cloud click event
 vm.clickWordCloudEventProcessorEmitter = function(word){
  var originField = vm.measures[0].config.fields[0];
  var originValue = word.text;
  
  sendEmitterEvent(originField,originValue);
}



    //leafletjs click marker event, by Point Id
    vm.clickMarkerMapEventProcessorEmitter = function(event, args){
      var originField = vm.measures[0].config.fields[2];
      var originValue = args.model.id;
      sendEmitterEvent(originField,originValue);
    }

    vm.selectItemTable = function (item) {
      
      console.log(item, 'was selected');
      for (var index = 0; index < vm.measures.length; index++) {
        var element = vm.measures[index];
        var originField = element.config.fields[0];
        var originValue = item[element.config.order];
        sendEmitterEvent(originField,originValue);
      }      
    };
  

    function sendEmitterEvent(originField,originValue){
      var filterStt = angular.copy(vm.datastatus)||{};     
      filterStt[originField]={value: originValue, id: vm.id};
      interactionService.sendBroadcastFilter(vm.id,filterStt);
    };

    vm.classPie = function () {
      if (vm.config.config.charType === undefined){ return true; } else {
          if (vm.config.config.charType === 'pie'){ return true; } else { return false; }
      }
    };
    
    
    function formatNumber(number, decimals, dec_point, thousands_sep) { 
      // *  example: formatNumber(1234.56, 2, ',', '.'); 
      // *  return: '1.234,56' 
          number = (number + '').replace(',', '').replace(' ', ''); 
          var n = !isFinite(+number) ? 0 : +number, 
            prec = !isFinite(+decimals) ? 0 : Math.abs(decimals), 
            sep = (typeof thousands_sep === 'undefined') ? '.' : thousands_sep, 
            dec = (typeof dec_point === 'undefined') ? ',' : dec_point, 
            s = '', 
            toFixedFix = function (n, prec) { 
             var k = Math.pow(10, prec); 
             return '' + Math.round(n * k)/k; 
            }; 
          // Fix for IE parseFloat(0.55).toFixed(0) = 0; 
          s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.'); 
          if (s[0].length > 3) { 
           s[0] = s[0].replace(/\B(?=(?:\d{3})+(?!\d))/g, sep); 
          } 
          if ((s[1] || '').length < prec) { 
           s[1] = s[1] || ''; 
           s[1] += new Array(prec - s[1].length + 1).join('0'); 
          } 
          return s.join(dec); 
      } 


      function addDatastatus(dataEvent,index){

        if(!vm.datastatus){
          vm.datastatus = [];
  
          vm.datastatus.push({
            field:dataEvent.data[index].field,
            value: angular.copy(dataEvent.data[index].value),
            id: angular.copy(dataEvent.id),
            op: angular.copy(dataEvent.data[index].op),
            idFilter: angular.copy(dataEvent.data[index].idFilter),
            name:angular.copy(dataEvent.data[index].name)                 
          }) ;
        }else{
          var exist = false;
          for (var i = 0; i < vm.datastatus.length; i++) {
            var element = vm.datastatus[i];
            if(vm.datastatus[i].idFilter===dataEvent.data[index].idFilter 
              && vm.datastatus[i].op === dataEvent.data[index].op){
                vm.datastatus[i]={
                  field:dataEvent.data[index].field,
                  value: angular.copy(dataEvent.data[index].value),
                  id: angular.copy(dataEvent.id),
                  op: angular.copy(dataEvent.data[index].op),
                  idFilter: angular.copy(dataEvent.data[index].idFilter),
                  name:angular.copy(dataEvent.data[index].name)                 
                }
                exist=true;
                break
              }
            
            }
          if(!exist){
            vm.datastatus.push({
              field:dataEvent.data[index].field,
              value: angular.copy(dataEvent.data[index].value),
              id: angular.copy(dataEvent.id),
              op: angular.copy(dataEvent.data[index].op),
              idFilter: angular.copy(dataEvent.data[index].idFilter),
              name:angular.copy(dataEvent.data[index].name)                 
            }) 
          }
        }
        vm.datastatus = vm.datastatus.filter(function(s){return s.value!==null;});

    }
  
    function deleteDatastatus(dataEvent){
  
    if(typeof vm.datastatus !== 'undefined'){
      var index = vm.datastatus.length;
      while (index--) {
        var element =  vm.datastatus[index];
        if(typeof dataEvent.op !== 'undefined' && dataEvent.op !== null && typeof element.op !== 'undefined' && element.op !== null){
          if(element.field === dataEvent.field && element.id === dataEvent.id && element.op === dataEvent.op  ){
            vm.datastatus.splice(index, 1); 
          }
        }else{
          if(element.field === dataEvent.field && element.id=== dataEvent.id ){
            vm.datastatus.splice(index, 1); 
          }
        } 
        
      }    
      if (vm.datastatus.length === 0) {
        vm.datastatus = undefined;
      }
    }
    }
      

}
})();

(function () {
  'use strict';
  FilterController.$inject = ["$mdDialog", "$timeout", "filterService"];
  angular.module('dashboardFramework')
    .component('filter', {
      templateUrl: 'app/components/view/filterComponent/filter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings: {
        id: "<?",
        datasource: "<?",
        config: "=?",
        hidebuttonclear: "<?",
        buttonbig:"<?"
      }
    });

  /** @ngInject */
  function FilterController($mdDialog, $timeout,filterService) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":"",value:""}]
    //"typeAction": {action, value , filter}


    vm.$onInit = function () {
      if(typeof vm.config!=='undefined' && vm.config!==null){
        vm.tempConfig = JSON.parse(JSON.stringify(vm.config)); 
        if(vm.tempConfig.length>0){
          for (var index = 0; index < vm.tempConfig.length; index++) {
            var element = vm.tempConfig[index];
            element.htmlId =  generateID();
          }
        }
      }
    };



    vm.sendFilters = function () {
      filterService.sendFilters(vm.id, vm.tempConfig);
      //filterService.cleanAllFilters(vm.id, vm.tempConfig);      
      //filterService.cleanAllFilters(vm.id, vm.tempConfig,filterService.sendFilters(vm.id, vm.tempConfig));     
      vm.config = JSON.parse(JSON.stringify(vm.tempConfig));
      $mdDialog.hide();
    }

    vm.cleanFilters = function () {
      filterService.cleanAllFilters(vm.id, vm.tempConfig);
      $mdDialog.hide();
    }

    function generateID(){
      return 'id-' + Math.random().toString(36).substr(2, 16);
    }


  }
})();
(function () {
  'use strict';

  ElementController.$inject = ["$compile", "$log", "$scope", "$mdDialog", "$sce", "$rootScope", "$timeout", "interactionService", "filterService", "$mdSidenav", "utilsService", "httpService", "__env"];
  angular.module('dashboardFramework')
    .component('element', {
      templateUrl: 'app/components/view/elementComponent/element.html',
      controller: ElementController,
      controllerAs: 'vm',
      bindings:{
        element: "=",
        iframe: "=",
        editmode: "<",
        eventedit: "=",
        editbuttonsiframe:"<?",
        showfavoritesg:"<?"
      }
    });

  /** @ngInject */
  function ElementController($compile,$log, $scope, $mdDialog, $sce, $rootScope, $timeout, interactionService,filterService,$mdSidenav,utilsService, httpService, __env) {
    EditContainerDialog.$inject = ["$scope", "$mdDialog", "utilsService", "element"];
    EditGadgetDialog.$inject = ["$scope", "$timeout", "$mdDialog", "element", "contenteditor", "httpService"];
    EditGadgetHTML5Dialog.$inject = ["$timeout", "$scope", "$mdDialog", "contenteditor", "element"];
  EditFilterDialog.$inject = ["$scope", "$mdDialog", "utilsService", "httpService", "element", "gadgetManagerService"];
    EditCustomMenuOptionsDialog.$inject = ["$scope", "$mdDialog", "element"];
    AddFavoriteGadgetDialog.$inject = ["$scope", "$timeout", "$mdDialog", "favoriteGadgetService", "urlParamService", "interactionService", "element"];
    var vm = this;
    vm.isMaximized = false;
    vm.datastatus;
 
    //Contains the information of the filters
  
  //  vm.config=[{id:"filtro1",type:"numberfilter", field:"Helsinki.year",name:"year",op:">",typeAction:"filter",initialFilter:false,useLastValue:true,filterChaining:false,targetList:[{gadgetId:"livehtml_1550073936906",overwriteField:"Helsinki.year"},{gadgetId:"livehtml_1549895094697",overwriteField:"Helsinki.year"}],value:2000},
   //            {id:"filtro2",type:"textfilter", field:"Helsinki.population",name:"population",op:">",typeAction:"action",initialFilter:false,useLastValue:true,filterChaining:false,targetList:[{gadgetId:"livehtml_1550073936906",overwriteField:"Helsinki.year"}],value:""}];
    //vm.config=[{"type":"textfilter"}];
     
    

    vm.$onInit = function () {
      //Base images urls
      vm.baseimg = __env.endpointControlPanel;
      //Initialice filters      
      vm.config = vm.element.filters;
      
      if(typeof vm.element.hideBadges ==='undefined'){
        vm.element.hideBadges=true;
      }
      if(typeof vm.element.notshowDotsMenu ==='undefined'){
        vm.element.notshowDotsMenu=false;
      }
      if(typeof vm.element.nomargin ==='undefined'){
        vm.element.nomargin=false;
      }
      if(typeof vm.element.customMenuOptions ==='undefined'){
        vm.element.customMenuOptions=[];
      }
      /** Custom menuoptiom structure example*/ 
      /** vm.element.customMenuOptions=[{id:'optionm',description:'Optionm',imagePath:'/controlpanel/static/images/dashboards/style.svg',position:'menu'}, */
      /** {id:'optionh',description:'Optionh',imagePath:'/controlpanel/static/images/dashboards/style.svg',position:'header'}];*/
  
      inicializeIncomingsEvents(); 
      //Added config filters to interactionService hashmap      
      interactionService.registerGadgetFilters(vm.element.id,vm.config);      
    };

    vm.openMenu = function($mdMenu){
      $mdMenu.open();
    }

    

    vm.elemntbodyclass = function(){     
     var temp =''+vm.element.id+' '+vm.element.type;
      if(vm.element.header.enable === true ) {
        temp +=' '+'headerMargin';
        if(vm.element.hideBadges === true ) {
          temp +=' '+'withoutBadgesAndHeader';
         }else{
          temp +=' '+'withBadgesAndHeader';
         }
     }else{
        temp +=' '+'noheaderMargin';
        if(vm.element.hideBadges === true ) {
          temp +=' '+'withoutBadges';
         }else{
          temp +=' '+'withBadges';
         }

        }
        if(vm.element.type === 'livehtml'){
          if(vm.element.nomargin){
            temp +=' '+'livehtmlfull';
          }else{
            temp +=' '+'livehtmlnotfull';
          }
        }
    
   return temp;
    }


vm.showHideEditButton = function(){
  var result =  vm.editmode && ((!vm.iframe || (vm.iframe &&  vm.editbuttonsiframe.editGadgetButton) ) || vm.eventedit) && (vm.element.type == 'livehtml' ||  vm.element.type == 'vuetemplate' ||  vm.element.type == 'reacttemplate');
return result;
}    

vm.elemntbadgesclass = function(){     
  var temp ='';
  if(vm.element.header.enable === false ) {
    if(vm.editmode === true ) {
      temp +=' '+'badgesMarginRightEditMode';
    }else{
      temp +=' '+'badgesMarginRightNoEditMode';
    }
  }
  return temp;
 }



    function inicializeIncomingsEvents(){
      $scope.$on("global.style",
        function(ev,style){
          angular.merge(vm.element,vm.element,style);
        }
      );   
    }

    vm.sendCustomMenuOption = function (id){
      vm.emitToTargets(vm.element.id,id);
    }

    vm.emitToTargets = function(id,data){
      //pendingDatasources
      $rootScope.$broadcast(id,
        {
          type: "customOptionMenu",
          data: data
        }
      );
    }


    vm.openEditGadgetIframe = function(ev) {
      DialogIframeEditGadgetController.$inject = ["$scope", "$mdDialog", "element"];
      if(vm.eventedit){
        vm.sendSelectEvent(vm.element);
        return;
      }   
      $mdDialog.show({
        parent: angular.element(document.body),
        targetEvent: ev,
        fullscreen: false,
        template:
          '<md-dialog id="dialogCreateGadget"  aria-label="List dialog">' +
          '  <md-dialog-content >'+
          '<iframe id="iframeCreateGadget" style=" height: 80vh; width: 80vw;" frameborder="0" src="'+__env.endpointControlPanel+'/gadgets/updateiframe/'+vm.element.id+'"+></iframe>'+                     
          '  </md-dialog-content>' +             
          '</md-dialog>',
          locals: {
            element: vm.element
          },
        controller: DialogIframeEditGadgetController
     });
     function DialogIframeEditGadgetController($scope, $mdDialog, element) {
       $scope.element = element;
       $scope.closeDialog = function () {
         var gadgets = document.querySelectorAll('gadget');
         if (gadgets.length > 0) {
           for (var index = 0; index < gadgets.length; index++) {
             var gad = gadgets[index];
             angular.element(gad).scope().$$childHead.reloadContent();
           }
         }
         var datadiscoverys = document.querySelectorAll('datadiscovery');
         if (datadiscoverys.length > 0) {
           for (var index = 0; index < datadiscoverys.length; index++) {
             var gad = datadiscoverys[index];
             angular.element(gad).isolateScope().vm.ds = null;
             angular.element(gad).isolateScope().reloadContent();
           }
         }
         $mdDialog.hide();
       }
      };


     };

     // toggle gadget to fullscreen and back.
     vm.toggleFullScreen = function(){               
      vm.isMaximized = !vm.isMaximized;
      var gridster = document.getElementsByTagName('gridster')[0];
      //change overflow-y gridster 
      if(vm.isMaximized){
        gridster.style.overflowY = 'hidden';
        gridster.style.overflowX = 'hidden';
        gridster.scrollTop = 0;
      }else{
        gridster.style.overflowY = 'auto';
        gridster.style.overflowX = 'auto';
        gridster.scrollTop = 0;
      }
      $timeout(
         function(){
           $scope.$broadcast("$resize", "");
         },300
       );
    };


     vm.reloadFilters = function(){
      var idNoSpaces = vm.element.id;
      idNoSpaces = idNoSpaces.replace(new RegExp(" ", "g"), "\\ ");      
      angular.element( document.querySelector( '#_'+idNoSpaces+'filters' ) ).empty();
      angular.element(document.getElementById('_'+vm.element.id+'filters')).append($compile('<filter id="vm.element.id" datasource="vm.element.datasource" config="vm.config" hidebuttonclear="vm.element.hidebuttonclear" buttonbig="false"></filter> ')($scope));      
      angular.element( document.querySelector( '#__'+idNoSpaces+'filters' ) ).empty();
      angular.element(document.getElementById('__'+vm.element.id+'filters')).append($compile('<filter id="vm.element.id" datasource="vm.element.datasource" config="vm.config" hidebuttonclear="vm.element.hidebuttonclear" buttonbig="false"></filter> ')($scope));      
     }

     vm.showfiltersInModal = function (){
      //if iframe show on menu
      if(vm.element.type==='gadgetfilter'){
        return false
      }
      if( vm.element.filtersInModal === true 
            && vm.config!=null 
            && showFilters(vm.config)){             
        return true;
      }      
      return false;
     }

     vm.showFiltersInBody = function (){
        //hide when is a gadget filter 
      if(vm.element.type==='gadgetfilter'){
         return false
       }
        //if iframe show on menu
        if((typeof vm.element.filtersInModal === 'undefined' || vm.element.filtersInModal === false) 
              && vm.config!=null 
              && showFilters(vm.config)){         
          return true;
        }     
        return false;
     }


     function showFilters(config){
      if(config.length>0){
        for (var index = 0; index < config.length; index++) {
          var element = config[index];
            if(typeof element.hide === 'undefined' || element.hide === false){
              return true;
            }          
        }
      }
        return false;      
     }


    vm.openEditContainerDialog = function (ev) {
      $mdDialog.show({
        controller: EditContainerDialog,
        templateUrl: 'app/partials/edit/editContainerDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:false,
        multiple : true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        locals: {
          element: vm.element
        }
      })
      .then(function(answer) {
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    function EditContainerDialog($scope, $mdDialog,utilsService, element) {
      $scope.icons = utilsService.icons;

      $scope.element = element;

      $scope.queryIcon = function (query) {
        return query ? $scope.icons.filter( createFilterFor(query) ) : $scope.icons;
      }

      /**
       * Create filter function for a query string
       */
      function createFilterFor(query) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(icon) {
          return (icon.indexOf(lowercaseQuery) != -1);
        };
      }

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };
    }

    function EditGadgetDialog($scope, $timeout,$mdDialog,  element, contenteditor, httpService) {
      $scope.initMonaco = function(){
        vm.VSHTML = monaco.editor.create(document.querySelector("#htmleditor"), {
          value: contenteditor.html,
          language: 'html',
          readOnly: false,
          scrollBeyondLastLine: false,
          theme: "vs-dark",
          automaticLayout: true
        });
  
        vm.VSJS = monaco.editor.create(document.querySelector("#jseditor"), {
          value: contenteditor.js,
          language: 'javascript',
          readOnly: false,
          scrollBeyondLastLine: false,
          theme: "vs-dark",
          automaticLayout: true
        });

        $scope.initFullScreen("jseditor",vm.VSJS);
        $scope.initFullScreen("htmleditor",vm.VSHTML);

        vm.VSJS.onDidChangeModelContent(function() {
          $scope.contenteditor.js = vm.VSJS.getValue();
          if($scope.livecompilation){
            $scope.compileJS();
          }
          utilsService.forceRender($scope);
        })
  
        vm.VSHTML.onDidChangeModelContent(function() {
          $scope.contenteditor.html = vm.VSHTML.getValue();   
          if($scope.livecompilation){
            $scope.compileHTML();
          }
          utilsService.forceRender($scope);
        })
      }

      vm.fullScreenControl = {"jseditor":false,"htmleditor":false};

      $scope.initFullScreen = function(id,editorObject){

        function toggleEditor() {
        	if(!vm.fullScreenControl[id]){
	        	document.getElementById(id).style.maxWidth = "100%";
	            document.getElementById(id).style.maxHeight = "100%";
	            document.getElementById(id).style.height = "100%";
	            document.getElementById(id).style.left = "0";
	            document.getElementById(id).style.right = "0";
	            document.getElementById(id).style.top = "0";
	            document.getElementById(id).style.bottom = "0";
	            document.getElementById(id).style.position = "fixed";
	            document.getElementById(id).style.zIndex = "1000";
	            vm.fullScreenControl[id] = true;
        	}
        	else{
        		document.getElementById(id).style.maxWidth = "";
	            document.getElementById(id).style.maxHeight = "";
	            document.getElementById(id).style.height = "400px";
	            document.getElementById(id).style.left = "";
	            document.getElementById(id).style.right = "";
	            document.getElementById(id).style.top = "";
	            document.getElementById(id).style.bottom = "";
	            document.getElementById(id).style.position = "";
	            document.getElementById(id).style.zIndex = "";
	            vm.fullScreenControl[id]=false;
          }
          if(vm.fullScreenControl[id]){
            document.getElementsByTagName("md-dialog")[0].style.maxWidth = "100%";
            document.getElementsByTagName("md-dialog")[0].style.maxHeight = "100%";
            document.getElementsByTagName("md-dialog")[0].style.left = "0";
            document.getElementsByTagName("md-dialog")[0].style.right = "0";
            document.getElementsByTagName("md-dialog")[0].style.top = "0";
            document.getElementsByTagName("md-dialog")[0].style.bottom = "0";
            document.getElementsByTagName("md-dialog")[0].style.position = "fixed";
          }
          else{
            document.getElementsByTagName("md-dialog")[0].style.maxWidth = "";
            document.getElementsByTagName("md-dialog")[0].style.maxHeight = "";
            document.getElementsByTagName("md-dialog")[0].style.left = "";
            document.getElementsByTagName("md-dialog")[0].style.right = "";
            document.getElementsByTagName("md-dialog")[0].style.top = "";
            document.getElementsByTagName("md-dialog")[0].style.bottom = "";
            document.getElementsByTagName("md-dialog")[0].style.position = "";
          }
        }

        editorObject.addCommand(monaco.KeyCode.F10, toggleEditor);
        editorObject.addCommand(monaco.KeyCode.F11, toggleEditor);
        
        editorObject.addCommand(monaco.KeyCode.Escape, function() {
        	document.getElementById(id).style.maxWidth = "";
            document.getElementById(id).style.maxHeight = "";
            document.getElementById(id).style.height = "400px";
            document.getElementById(id).style.left = "";
            document.getElementById(id).style.right = "";
            document.getElementById(id).style.top = "";
            document.getElementById(id).style.bottom = "";
            document.getElementById(id).style.position = "";
            document.getElementById(id).style.zIndex = "";
            vm.fullScreenControl[id]=false;
            document.getElementsByTagName("md-dialog")[0].style.maxWidth = "";
            document.getElementsByTagName("md-dialog")[0].style.maxHeight = "";
            document.getElementsByTagName("md-dialog")[0].style.left = "";
            document.getElementsByTagName("md-dialog")[0].style.right = "";
            document.getElementsByTagName("md-dialog")[0].style.top = "";
            document.getElementsByTagName("md-dialog")[0].style.bottom = "";
            document.getElementsByTagName("md-dialog")[0].style.position = "";
        });
      }

      $scope.contenteditor = contenteditor;
      
      $scope.livecompilation = false;

      $scope.element = element;

      $scope.compileHTML = function(){
        if(typeof $scope.contenteditor.html !== 'undefined'){
          $scope.element.content = vm.VSHTML.getValue();
        }else{
          $scope.element.content = "";
        }
      }

      $scope.compileJS = function(){
        if(typeof $scope.contenteditor.js !== 'undefined'){
          $scope.element.contentcode = vm.VSJS.getValue();
        }else{
          $scope.element.contentcode = "";
        }
      }

      $scope.compile = function(){ 
        $scope.contenteditor.html = vm.VSHTML.getValue();   
        $scope.contenteditor.js = vm.VSJS.getValue();
        $scope.compileHTML();
        $scope.compileJS();
      }

      $scope.saveAsTemplate = function(){
        $scope.contenteditor.html = vm.VSHTML.getValue();   
        $scope.contenteditor.js = vm.VSJS.getValue();
      }

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };

      $scope.datasources = [];

      $scope.loadDatasources = function(){
        return httpService.getDatasources().then(
          function(response){
            $scope.datasources=response.data;
          },
          function(e){
            console.log("Error getting datasources: " +  JSON.stringify(e))
          }
        );
      };
      $scope.loadDatasources();
    }

    vm.openEditGadgetDialog = function (ev) {
      if(vm.eventedit){
        vm.sendSelectEvent(vm.element);
        return;
      }
      if(!vm.contenteditor){
        vm.contenteditor = {}
        vm.contenteditor["html"] = vm.element.content.slice();
        vm.contenteditor["js"] = (vm.element.contentcode?vm.element.contentcode.slice():"");
      }
      $mdDialog.show({
        controller: EditGadgetDialog,
        templateUrl: 'app/partials/edit/editGadgetDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        onComplete: function($scope){
          $scope.initMonaco();
        },
        multiple : true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
       
        locals: {
          element: vm.element,
          contenteditor: vm.contenteditor
        }
      })
      .then(function(answer) {
       
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    
    };

    vm.openEditTemplateParamsDialog = function (ev) {
      if(vm.eventedit){
        vm.sendSelectEvent(vm.element);
        return;
      }
      httpService.getGadgetTemplateByIdentification(vm.element.template).then(
        function(data){
          vm.contenteditor = {}
          vm.contenteditor["content"] = data.data.template;
          vm.contenteditor["contentcode"] = data.data.templateJS;

          $mdDialog.show({
            controller: 'editTemplateParamsController',
            templateUrl: 'app/partials/edit/addGadgetTemplateParameterDialog.html',
            parent: angular.element(document.body),
            targetEvent: ev,
            clickOutsideToClose:true,
            multiple : true,
            fullscreen: false, // Only for -xs, -sm breakpoints.
            locals: {
              type: vm.element.type,
              config: vm.contenteditor,
              element: vm.element,
              layergrid: null,
              edit: true
            }
          })
          .then(function(answer) {
           
          }, function() {
            $scope.status = 'You cancelled the dialog.';
          });
        }
      )
    };

    function EditGadgetHTML5Dialog($timeout,$scope, $mdDialog, contenteditor, element) {
      $scope.editor;
      
      $scope.element = element;

      $scope.initMonaco = function(){
        vm.VSHTML = monaco.editor.create(document.querySelector("#htmleditor"), {
          value: contenteditor.html,
          language: 'html',
          readOnly: false,
          scrollBeyondLastLine: false,
          theme: "vs-dark",
          automaticLayout: true
        });

        $scope.initFullScreen("htmleditor",vm.VSHTML);
  
        vm.VSHTML.onDidChangeModelContent(function() {
          $scope.contenteditor.html = vm.VSHTML.getValue();   
          if($scope.livecompilation){
            $scope.compileHTML();
          }
          utilsService.forceRender($scope);
        })
      }

      $scope.initFullScreen = function(id,editorObject){

        function toggleEditor() {
        	if(!vm.fullScreenControl[id]){
	        	document.getElementById(id).style.maxWidth = "100%";
	            document.getElementById(id).style.maxHeight = "100%";
	            document.getElementById(id).style.height = "100%";
	            document.getElementById(id).style.left = "0";
	            document.getElementById(id).style.right = "0";
	            document.getElementById(id).style.top = "0";
	            document.getElementById(id).style.bottom = "0";
	            document.getElementById(id).style.position = "fixed";
	            document.getElementById(id).style.zIndex = "1000";
	            vm.fullScreenControl[id] = true;
        	}
        	else{
        		document.getElementById(id).style.maxWidth = "";
	            document.getElementById(id).style.maxHeight = "";
	            document.getElementById(id).style.height = "400px";
	            document.getElementById(id).style.left = "";
	            document.getElementById(id).style.right = "";
	            document.getElementById(id).style.top = "";
	            document.getElementById(id).style.bottom = "";
	            document.getElementById(id).style.position = "";
	            document.getElementById(id).style.zIndex = "";
	            vm.fullScreenControl[id]=false;
          }
          if(vm.fullScreenControl[id]){
            document.getElementsByTagName("md-dialog")[0].style.maxWidth = "100%";
            document.getElementsByTagName("md-dialog")[0].style.maxHeight = "100%";
            document.getElementsByTagName("md-dialog")[0].style.left = "0";
            document.getElementsByTagName("md-dialog")[0].style.right = "0";
            document.getElementsByTagName("md-dialog")[0].style.top = "0";
            document.getElementsByTagName("md-dialog")[0].style.bottom = "0";
            document.getElementsByTagName("md-dialog")[0].style.position = "fixed";
          }
          else{
            document.getElementsByTagName("md-dialog")[0].style.maxWidth = "";
            document.getElementsByTagName("md-dialog")[0].style.maxHeight = "";
            document.getElementsByTagName("md-dialog")[0].style.left = "";
            document.getElementsByTagName("md-dialog")[0].style.right = "";
            document.getElementsByTagName("md-dialog")[0].style.top = "";
            document.getElementsByTagName("md-dialog")[0].style.bottom = "";
            document.getElementsByTagName("md-dialog")[0].style.position = "";
          }
        }

        editorObject.addCommand(monaco.KeyCode.F10, toggleEditor);
        editorObject.addCommand(monaco.KeyCode.F11, toggleEditor);
        
        editorObject.addCommand(monaco.KeyCode.Escape, function() {
        	document.getElementById(id).style.maxWidth = "";
            document.getElementById(id).style.maxHeight = "";
            document.getElementById(id).style.height = "400px";
            document.getElementById(id).style.left = "";
            document.getElementById(id).style.right = "";
            document.getElementById(id).style.top = "";
            document.getElementById(id).style.bottom = "";
            document.getElementById(id).style.position = "";
            document.getElementById(id).style.zIndex = "";
            vm.fullScreenControl[id]=false;
            document.getElementsByTagName("md-dialog")[0].style.maxWidth = "";
            document.getElementsByTagName("md-dialog")[0].style.maxHeight = "";
            document.getElementsByTagName("md-dialog")[0].style.left = "";
            document.getElementsByTagName("md-dialog")[0].style.right = "";
            document.getElementsByTagName("md-dialog")[0].style.top = "";
            document.getElementsByTagName("md-dialog")[0].style.bottom = "";
            document.getElementsByTagName("md-dialog")[0].style.position = "";
        });
      }

      $scope.compileHTML = function(){
        if(typeof $scope.contenteditor.html !== 'undefined'){
          $scope.element.content = vm.VSHTML.getValue();
        }else{
          $scope.element.content = "";
        }
      }

      $scope.contenteditor = contenteditor;

      $scope.livecompilation = false;

      $scope.compile = function(){ 
        $scope.contenteditor.html = vm.VSHTML.getValue();   
        $scope.compileHTML();
      }

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };

    }


    vm.openEditGadgetHTML5Dialog = function (ev) {
      if(vm.eventedit){
        vm.sendSelectEvent(vm.element);
        return;
      }
      if(!vm.contenteditor){
        vm.contenteditor = {}
        vm.contenteditor["html"] = vm.element.content.slice();
      }
      $mdDialog.show({
        controller: EditGadgetHTML5Dialog,
        templateUrl: 'app/partials/edit/editGadgetHTML5Dialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        onComplete: function($scope){
          $scope.initMonaco();
        },
        multiple : true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
       
        locals: {
          element: vm.element,
          contenteditor: vm.contenteditor
        }
      })
      .then(function(answer) {
       
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    vm.trustHTML = function(html_code) {
      return $sce.trustAsHtml(html_code)
    }

    vm.calcHeight = function(){
      vm.element.header.height = (vm.element.header.height=='inherit'?25:vm.element.header.height);     
     var result = "'calc(100% - 36px)'";


      return result;
    }


    vm.toggleRight =  function(componentId) {
      $mdSidenav(componentId).toggle();
    };
    
    
    
    
    
    vm.deleteElement = function(){
      $rootScope.$broadcast("deleteElement",vm.element);
    }

    vm.generateFilterInfo = function(filter){ 
      return filter.value;
    }

    vm.deleteFilter = function(id, field,op){      
      $rootScope.$broadcast(vm.element.id,{id: id,type:'filter',data:[],field:field,op:op})
    }




    vm.openFilterDialog = function(ev) {     
      $mdDialog.show({
        parent: angular.element(document.body),
        targetEvent: ev,
        scope: $scope,
        preserveScope: true, 
        fullscreen: false,
        template:
          '<md-dialog flex="35"  aria-label="List dialog" style="min-width:440px">' +
          '<form ng-cloak>'+
          '<md-toolbar style="background-color:rgba(255,255,255,0.87);  position: absolute; top: 0;right: 0;">' +
          '<div class="md-toolbar-tools">' +
          '<span flex="" class="flex"></span>'+
          '<button type="button" aria-label="Close" class="ods-dialog__headerbtn" ng-click="closeDialog()"><span class="ods-dialog__close ods-icon ods-icon-close"></span></button>'+           
          '</div>' +
       ' </md-toolbar>' +
          '  <md-dialog-content style="padding: 30px 30px 10px;" >'+
          ' <filter id="vm.element.id" datasource="vm.element.datasource" config="vm.config" hidebuttonclear="vm.element.hidebuttonclear" buttonbig="true"></filter>'+  
          '  </md-dialog-content>' + 
          '</form>'+           
          '</md-dialog>',
         
        controller: ["$scope", "$mdDialog", function DialogController($scope, $mdDialog) {
      
          $scope.closeDialog = function() {
            $mdDialog.hide();
          }
        }]
     });
    

     };


     vm.openEditFilterDialog = function (ev) {
     filterService.cleanAllFilters(vm.element.id,vm.element.filters);
     interactionService.unregisterGadgetFilter(vm.element.id);   
      $mdDialog.show({
        controller: EditFilterDialog,
        templateUrl: 'app/partials/edit/editFilterDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:false,
        multiple : false,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        locals: {
          element: vm.element
        }
      })
      .then(function(answer) {        
        interactionService.registerGadgetFilters(vm.element.id,vm.element.filters);        
        vm.config = vm.element.filters;
        vm.reloadFilters();
      }, function() {
        $scope.status = 'You cancelled the dialog.';             
        interactionService.registerGadgetFilters(vm.element.id,vm.element.filters);        
        vm.config = vm.element.filters;
        vm.reloadFilters();
      
      });
    };

    function EditFilterDialog($scope, $mdDialog,utilsService,httpService, element,gadgetManagerService) {
     

      $scope.element = element; 
     
      $scope.tempFilter = {data:{ds:""},typeAction: "filter"};
      $scope.typeList = [
                          {id:'textfilter',description:'text filter'},
                          {id:'numberfilter',description:'numerical filter'},
                          {id:'livefilter',description:'Date range and real time filter'},
                          {id:'intervaldatefilter',description:'Date range filter'},
                          {id:'intervaldatestringfilter',description:'Date range filter no conversion to timestamp'},                                                    
                          {id:'simpleselectdsfilter',description:'text filter with simple-selection from datasource'},
                          {id:'simpleselectnumberdsfilter',description:'numerical filter with simple-selection from datasource'},
                          {id:'multiselectdsfilter',description:'text filter with multi-selection from datasource'},
                          {id:'multiselectnumberdsfilter',description:'numerical filter with multi-selection from datasource'},
                          {id:'simpleselectfilter',description:'text filter with simple-selection'},
                          {id:'simpleselectnumberfilter',description:'numerical filter with simple-selection'},
                          {id:'multiselectfilter',description:'text filter with multi-selection'},                         
                          {id:'multiselectnumberfilter',description:'numerical filter with multi-selection'}
                        ];
      
                        
      $scope.opList = [
        {id:'=',description:'='},
        {id:'>',description:'>'},
        {id:'<',description:'<'},
        {id:'<=',description:'<='},
        {id:'>=',description:'>='},
        {id:'<>',description:'<>'}
      ];
     
      
     
      $scope.hideLabelName = true;
      $scope.hideOperator = true;
      $scope.hideValue = true;
      $scope.hideOptions = true; 
      $scope.hideDatasource = true; 
     generateGadgetsLists();

     function generateGadgetsLists(){
     
      $scope.gadgetsTargets = getGadgetsInDashboard();
      refreshGadgetTargetFields($scope.element.id);
    }


    $scope.datasources = [];
    $scope.datasourcesSelected ="";
    $scope.loadDatasources = function(){
      return httpService.getDatasources().then(
        function(response){
          $scope.datasources=response.data;
          
        },
        function(e){
          console.log("Error getting datasources: " +  JSON.stringify(e))
        }
      );
    };
    $scope.loadDatasources();

   




    $scope.setdsTargetFields = function (){  
        
      if(typeof $scope.tempFilter!=='undefined'&& $scope.tempFilter !=null 
      && typeof $scope.tempFilter.data!=='undefined'&& $scope.tempFilter.data !=null
      && typeof $scope.tempFilter.data.ds!=='undefined'&& $scope.tempFilter.data.ds !=null ){

        var idDs= findDsId($scope.tempFilter.data.ds);
        httpService.getFieldsFromDatasourceId(idDs).then(
        function(data){
          
          $scope.dsTargetFields = utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", []));
        }
      )
      }
    }

    $scope.dsTargetFields = [];
    $scope.setdsTargetFields();

    function findDsId(identification){
     var elem = $scope.datasources.find( function(element){
        return element.identification === identification;
      });
      if(typeof elem !=='undefined'){
        return elem.id;
      }

    }

    //Generate gadget list of posible Sources of interactions: pie, bar, livehtml
    function getGadgetsInDashboard(){
      return gadgetManagerService.returnGadgets();
    }


    function findGadgetInDashboard(gadgetId){    
        return gadgetManagerService.findGadgetById(gadgetId)
    }

    $scope.generateGadgetInfo = function (gadgetId){
      var gadget = findGadgetInDashboard(gadgetId);
      if(gadget == null){
        return gadgetId;
      }
      else{
        return $scope.prettyGadgetInfo(gadget);
      }
    }


    

    $scope.deleteOption = function (opt){
      var options = $scope.tempFilter.data.options;
      var optionsDescription = $scope.tempFilter.data.optionsDescription;

          for(var i=0;i<options.length;i++){
            if(options[i] === opt ){
              options.splice(i, 1);
              optionsDescription.splice(i, 1);
              break;
            }
          }
    }

    $scope.addOption = function (opt,description){
      if(typeof $scope.tempFilter.data ==='undefined' ){
        $scope.tempFilter.data = {};
      }
      var addedOption = false;

      if( typeof $scope.tempFilter.data.options ==='undefined' || $scope.tempFilter.data.options === null){
        $scope.tempFilter.data.options = [];
        $scope.tempFilter.data.options[0] = opt;
        addedOption=true;
      }else{
        var find = false;
        for(var i=0;i<$scope.tempFilter.data.options.length;i++){
          if($scope.tempFilter.data.options[i] === opt ){            
            find=true;
          }
        }
        if(!find){
          $scope.tempFilter.data.options.push(opt); 
          addedOption = true;
        }
      }
     if(addedOption){
        if( typeof $scope.tempFilter.data.optionsDescription ==='undefined' || $scope.tempFilter.data.optionsDescription === null){
          $scope.tempFilter.data.optionsDescription = []
          $scope.tempFilter.data.optionsDescription[0] = description;
        }else{         
            $scope.tempFilter.data.optionsDescription.push(description); 
          }
        }
      
    }

    

    $scope.deleteFilter = function (id){
      if(typeof $scope.element.filters !=='undefined' && $scope.element.filters!= null  ){
        for (var index = 0; index < $scope.element.filters.length; index++) {         
          if($scope.element.filters[index].id === id){           
            $scope.element.filters.splice(index, 1);            
            return null;
          }          
        }
      }
    }

    

    $scope.editFilter = function (id){
      if(typeof $scope.element.filters !=='undefined' && $scope.element.filters !== null ){
        for (var index = 0; index < $scope.element.filters.length; index++) {         
          if($scope.element.filters[index].id === id){
            
            $scope.tempFilter = makeFilter( $scope.element.filters[index],true);
            //update
            $scope.hideFields($scope.tempFilter.type);        
            return null;
          }          
        }
      }
    }
    


   

    $scope.addFilter = function(){
      //validations
      var tempFilter = $scope.tempFilter;
     
      tempFilter.typeAction = "filter";
      var targetGadgetField = $scope.targetGadgetField;

      if(typeof tempFilter.id ==='undefined' || (typeof tempFilter.id !=='undefined' && tempFilter.id.length===0)){
        //identifier mandatory
        return null;
      }
     
      if(typeof tempFilter.type ==='undefined' || (typeof tempFilter.type !=='undefined' && tempFilter.type.length===0)){
        //type mandatory
        return null;
      }
      if(typeof tempFilter.name ==='undefined' || (typeof tempFilter.name !=='undefined' && tempFilter.name.length===0)){
      
        tempFilter.name="";

      }
      if(tempFilter.typeAction==='filter'&&( tempFilter.type ==='textfilter' || tempFilter.type==='numberfilter' )){
        if( typeof tempFilter.op ==='undefined' || (typeof tempFilter.op !=='undefined' && tempFilter.op.length===0)){
          //   op mandatory 
          return null;
        }
      }    

      if(typeof targetGadgetField ==='undefined' || targetGadgetField===null || targetGadgetField.trim().length === 0){
        //targetList mandatory
        return null;
      }

      tempFilter.targetList=[{
        "gadgetId": $scope.element.id,
        "overwriteField": targetGadgetField
      }];

        //update for id 
      if(typeof $scope.element.filters !=='undefined' &&  $scope.element.filters != null){
        for (var index = 0; index < $scope.element.filters.length; index++) {
          var elem = $scope.element.filters[index];
          if(elem.id === tempFilter.id){           
            $scope.element.filters[index] = makeFilter(tempFilter,false) ;
            return null;
          }          
        }
      }
      if(typeof  $scope.element.filters === 'undefined' ||   $scope.element.filters == null){
        $scope.element.filters = [makeFilter(tempFilter,false)];
      }else{
        $scope.element.filters.push( makeFilter(tempFilter,false) );
      }
    }


function makeFilter(tempFilter,read){
  //load for edit
  if(read){
    var filter = {
      'id':tempFilter.id,
      'type': tempFilter.type,
      'typeAction': tempFilter.typeAction,
      'name':tempFilter.name,
      'op': tempFilter.op,
      'value': tempFilter.value,
      'targetList':tempFilter.targetList,
      'hide':tempFilter.hide,
      'initialFilter':tempFilter.initialFilter,
      'data':tempFilter.data
    };
    $scope.targetGadgetField=tempFilter.targetList[0].overwriteField;
  }
  else{
    //for create new data or update
    for (var index = 0; index < tempFilter.targetList.length; index++) {    
      tempFilter.targetList[index].field = tempFilter.targetList[index].overwriteField;
    }
    if(tempFilter.type === 'multiselectfilter' 
    || tempFilter.type === 'multiselectnumberfilter'
    || tempFilter.type === 'multiselectdsfilter' 
    || tempFilter.type === 'multiselectnumberdsfilter'  ){
      if(typeof tempFilter.data!='undefined' && typeof tempFilter.data.options!='undefined' ){
        tempFilter.data.optionsSelected = tempFilter.data.options.slice();
      }
    }
    if(tempFilter.type === 'simpleselectfilter' 
    || tempFilter.type === 'simpleselectnumberfilter' 
    || tempFilter.type === 'simpleselectdsfilter'
    || tempFilter.type === 'simpleselectnumberdsfilter' ){
      if(typeof tempFilter.data!='undefined' && typeof tempFilter.data.options!='undefined' ){
        tempFilter.data.optionsSelected = tempFilter.value;
      }
    }
    if(tempFilter.type === 'livefilter'){
      tempFilter.data = {
        "options": null,
        "optionsSelected": null,
        "startDate": "NOW(\"yyyy-MM-dd'T'HH:mm:ss'Z'\",\"hour\",-8)",
        "endDate": "NOW(\"yyyy-MM-dd'T'HH:mm:ss'Z'\",\"hour\",0)",
        "selectedPeriod": 8,
        "realtime": "start"
      };
    }
    if(tempFilter.type === 'intervaldatefilter' || tempFilter.type === 'intervaldatestringfilter'){
      tempFilter.data = {
        "options": null,
        "optionsSelected": null,
        "startDate":  moment().subtract(8,'hour').toISOString() ,
        "endDate":  moment().toISOString() 
      };
    }

    var filter = {
      'id':tempFilter.id,
      'typeAction': tempFilter.typeAction,
      'type': tempFilter.type,
      'name':tempFilter.name,
      'op': tempFilter.op,
      'value': tempFilter.value,
      'targetList':tempFilter.targetList,
      'hide':tempFilter.hide,
      'initialFilter':tempFilter.initialFilter,
      'data':tempFilter.data
    };
  }
return filter;
}

    function refreshGadgetTargetFields (gadgetId){
      var gadget = findGadgetInDashboard(gadgetId);
      if(gadget == null){
        $scope.gadgetEmitterFields = [];
      }
      else{
        setGadgetTargetFields(gadget);
      }
    }


 //Destination are all gadget fields
 function setGadgetTargetFields(gadget){        
    $scope.targetDatasource="";
  var gadgetData = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm;
  if(gadget.type === 'livehtml' || gadget.type === 'vuetemplate' || gadget.type === 'reacttemplate'){
    if(typeof gadgetData.datasource!=='undefined'){
      $scope.targetDatasource = gadgetData.datasource.name;
     
    }else{
      $scope.gadgetTargetFields = [];
      return null;
    }
    var dsId = gadgetData.datasource.id;
  } else  if(gadget.type === 'gadgetfilter'){
    if(typeof gadgetData.datasource!=='undefined'){
      $scope.targetDatasource = gadgetData.datasource.name;     
    }else{
      $scope.gadgetTargetFields = [];
      return null;
    }
    var dsId = gadgetData.datasource.id;
  }
  else{
    $scope.targetDatasource = gadgetData.measures[0].datasource.identification;
    var dsId = gadgetData.measures[0].datasource.id;
  }
  httpService.getFieldsFromDatasourceId(dsId).then(
    function(data){
      $scope.gadgetTargetFields = utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", []));
    }
  )
  $scope.gadgetTargetFields = [];
}

 //Get gadget JSON and return string info for UI
 $scope.prettyGadgetInfo = function(gadget){
       
  return gadget.header.title.text + " (" + gadget.type + ")";

}


$scope.queryTargetField = function(query){     
  $scope.targetDatasource="";
var gadgetData = angular.element(document.getElementsByClassName($scope.targetGadget)[0]).scope().$$childHead.vm;
if(gadgetData.type === 'livehtml'){
  if(typeof gadgetData.datasource!=='undefined'){
    $scope.targetDatasource = gadgetData.datasource.name;
   
  }else{
    return [];
   
  }
  var dsId = gadgetData.datasource.id;
} else if(gadgetData.type === 'gadgetfilter'){
  if(typeof gadgetData.datasource!=='undefined'){
    $scope.targetDatasource = gadgetData.datasource.name;
   
  }else{
    return [];
   
  }
  var dsId = gadgetData.datasource.id;
}
else{
  $scope.targetDatasource = gadgetData.measures[0].datasource.identification;
  var dsId = gadgetData.measures[0].datasource.id;
}
httpService.getFieldsFromDatasourceId(dsId).then(
  function(data){
   var gadgetTargetFields = utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", []));
   var result = query ? gadgetTargetFields.filter(createFilterFor(query)) : gadgetTargetFields;
   return result;
  }
)
return  [];
}

    
      /**
       * Create filter function for a query string
       */
      function createFilterFor(query) {
        var lowercaseQuery = query.toLowerCase();  
        return function filterFn(field) {
          return (field.field.toLowerCase().indexOf(lowercaseQuery) === 0); 
        };
  
      }


     
$scope.hideFields = function(type){
  
  if($scope.tempFilter.typeAction==='filter'){

    if(type==='textfilter' ||
      type==='numberfilter'){
      $scope.hideLabelName = false;
      $scope.hideOperator = false;
      $scope.hideValue = false;
      $scope.hideOptions = true; 
      $scope.hideInitialFilter = false;
      $scope.hideHide =false;      
      $scope.hideDatasource = true; 
    }else if(type==='livefilter'){
      $scope.hideLabelName = true;
      $scope.hideOperator = true;
      $scope.hideValue = true;
      $scope.hideOptions = true; 
      $scope.hideInitialFilter = false;
      $scope.hideHide =false;
      $scope.hideDatasource = true; 
    }else if( type==='multiselectfilter' ||
       type==='multiselectnumberfilter'){
      $scope.hideLabelName = false;
      $scope.hideOperator = true;
      $scope.hideValue = true;
      $scope.hideOptions = false; 
      $scope.hideInitialFilter = false;
      $scope.hideHide =false;
      $scope.hideDatasource = true; 
    }else if(type==='simpleselectfilter'
      || type==='simpleselectnumberfilter'){
      $scope.hideLabelName = false;
      $scope.hideOperator = false;
      $scope.hideValue = true;
      $scope.hideOptions = false; 
      $scope.hideInitialFilter = true;
      $scope.hideHide =true;
      $scope.hideDatasource = true; 
    }else if(type==='intervaldatefilter' 
    || type==='intervaldatestringfilter'){
      $scope.hideLabelName = false;
      $scope.hideOperator = true;
      $scope.hideValue = true;
      $scope.hideOptions = true; 
      $scope.hideInitialFilter = false;
      $scope.hideHide =false;
      $scope.hideDatasource = true; 
    }else if(type==='simpleselectdsfilter'||
     type ==='simpleselectnumberdsfilter' ){
      $scope.hideLabelName = false;
      $scope.hideOperator = false;
      $scope.hideValue = true;
      $scope.hideOptions = true; 
      $scope.hideInitialFilter = true;
      $scope.hideHide =true;
      $scope.hideDatasource = false; 
    }else if( type==='multiselectdsfilter' ||
    type==='multiselectnumberdsfilter'){
    $scope.hideLabelName = false;
    $scope.hideOperator = true;
    $scope.hideValue = true;
    $scope.hideOptions = true; 
    $scope.hideInitialFilter = true;
    $scope.hideHide =false;
    $scope.hideDatasource = false;   
    }else{
      
    $scope.hideLabelName = false;
    $scope.hideOperator = true;
    $scope.hideValue = false;
    $scope.hideOptions = true; 
    $scope.hideInitialFilter = true;
    $scope.hideHide =false;
    $scope.hideDatasource = true;   
  }

}


      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };
    }

    $scope.hideFields();

  }
  
    vm.openEditCustomMenuOptionsDialog = function (ev) {
      $mdDialog.show({
        controller: EditCustomMenuOptionsDialog,
        templateUrl: 'app/partials/edit/editCustomMenuOptions.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:false,
        multiple : false,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        locals: {
          element: vm.element
        }
      })
      .then(function(answer) {    
     
      }, function() {
        $scope.status = 'You cancelled the dialog.';             
      
      });
    };


    vm.addFavoriteDialog = function (ev) {
      $mdDialog.show({
        controller: AddFavoriteGadgetDialog,
        templateUrl: 'app/partials/edit/addFavoriteGadgetDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:false,
        multiple : false,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        locals: {
          element: vm.element
        }
      })
      .then(function(answer) {    
     
      }, function() {
        $scope.status = 'You cancelled the dialog.';             
      
      });
    }


    function EditCustomMenuOptionsDialog($scope, $mdDialog, element) {
     

      $scope.element = element;     
      $scope.positionList = [
        {id:'menu',description:'Menu'},
        {id:'header',description:'Header'}
      ];

      $scope.tempMenuOp = {};
     
    $scope.deleteMenuOption = function (id){
      if(typeof $scope.element.customMenuOptions !=='undefined' ){
        for (var index = 0; index < $scope.element.customMenuOptions.length; index++) {         
          if($scope.element.customMenuOptions[index].id === id){           
            $scope.element.customMenuOptions.splice(index, 1);            
            return null;
          }          
        }
      }
    }

    $scope.editMenuOption = function (id){
      if(typeof $scope.element.customMenuOptions !=='undefined' ){
        for (var index = 0; index < $scope.element.customMenuOptions.length; index++) {         
          if($scope.element.customMenuOptions[index].id === id){            
            $scope.tempMenuOp = makeCustomMenuOptions( $scope.element.customMenuOptions[index]);
            return null;
          }          
        }
      }
    }
  
    $scope.addCustomMenuOpt = function(){
      //validations
      var tempMenuOp = $scope.tempMenuOp;     
      if(typeof tempMenuOp.id ==='undefined' || (typeof tempMenuOp.id !=='undefined' && tempMenuOp.id.length===0)){
        //identifier mandatory
        return null;
      }     
      if(typeof tempMenuOp.description ==='undefined' || (typeof tempMenuOp.description !=='undefined' && tempMenuOp.description.length===0)){
        //description mandatory
        return null;
      }     
        //update for id 
      if(typeof $scope.element.customMenuOptions !=='undefined' ){
        for (var index = 0; index < $scope.element.customMenuOptions.length; index++) {
          var elem = $scope.element.customMenuOptions[index];
          if(elem.id === tempMenuOp.id){           
            $scope.element.customMenuOptions[index] = makeCustomMenuOptions(tempMenuOp) ;
            return null;
          }          
        }
      }
      if(typeof  $scope.element.customMenuOptions === 'undefined'){
        $scope.element.customMenuOptions = [makeCustomMenuOptions(tempMenuOp)];
      }else{
        $scope.element.customMenuOptions.push( makeCustomMenuOptions(tempMenuOp) );
      }
    }


function makeCustomMenuOptions(tempCustomMenuOp){
  //load for edit
  
    var customMenuOp = {
      'id':tempCustomMenuOp.id,
      'description': tempCustomMenuOp.description,
      'imagePath': tempCustomMenuOp.imagePath,
      'position':tempCustomMenuOp.position      
    };  
return customMenuOp;
}

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };
      
    }
    //This function is called when push button add favorite gaget 
    function AddFavoriteGadgetDialog($scope, $timeout, $mdDialog, favoriteGadgetService, urlParamService, interactionService, element) {
      $scope.element = element;
      $scope.showAlert = false;
      $scope.isOK = "alertOK";
      $scope.identifier =  element.header.title.text || "" ;
      $scope.saveconnections = true;
      $scope.message = "";
      $scope.addFavoriteGadget = function () {
          var data = {};
          data.identification = $scope.identifier;
          data.idDatasource = null
          data.idGadget = null;
          data.idGadgetTemplate = null
          data.config = null;
          data.type = $scope.element.type;
          var config = {};
          if (data.type == "livehtml" || data.type == "gadgetfilter") {
            if ($scope.element.template) {
              data.idGadgetTemplate = $scope.element.template;
              config.params = $scope.element.params;
            }
            config.subtype = $scope.element.subtype;
            config.content = $scope.element.content;
            config.contentcode = $scope.element.contentcode;
            config.customMenuOptions = $scope.element.customMenuOptions;
            config.backgroundColor = $scope.element.backgroundColor;
            config.header = $scope.element.header;
            config.cols = $scope.element.cols;
            config.rows = $scope.element.rows;
            config.border = $scope.element.border;
            config.hideBadges = $scope.element.hideBadges;
            config.nomargin = $scope.element.nomargin;
            config.notshowDotsMenu = $scope.element.notshowDotsMenu;
            config.padding = $scope.element.padding;
            if ($scope.element.datasource) {
              //map name because need identifier not id
              data.idDatasource = $scope.element.datasource.name;
              config.datasource = $scope.element.datasource;
            }
          } else if (data.type == "html5") {
            config.content = $scope.element.content;
            config.customMenuOptions = $scope.element.customMenuOptions;
            config.backgroundColor = $scope.element.backgroundColor;
            config.header = $scope.element.header;
            config.cols = $scope.element.cols;
            config.rows = $scope.element.rows;
            config.border = $scope.element.border;
            config.hideBadges = $scope.element.hideBadges;
            config.nomargin = $scope.element.nomargin;
            config.notshowDotsMenu = $scope.element.notshowDotsMenu;
            config.padding = $scope.element.padding;
          } else {
            data.idGadget = $scope.element.id;
            config.content = $scope.element.content;
            config.customMenuOptions = $scope.element.customMenuOptions;
            config.backgroundColor = $scope.element.backgroundColor;
            config.header = $scope.element.header;
            config.cols = $scope.element.cols;
            config.rows = $scope.element.rows;
            config.border = $scope.element.border;
            config.hideBadges = $scope.element.hideBadges;
            config.nomargin = $scope.element.nomargin;
            config.notshowDotsMenu = $scope.element.notshowDotsMenu;
            config.padding = $scope.element.padding;
          }
          if ($scope.saveconnections) {
            config.urlparams = urlParamService.geturlParamHashForTargetGadget($scope.element.id);
            config.datalinks = interactionService.getInteractionHashForTargetGadget($scope.element.id);
          }
          data.config = JSON.stringify(config);
          favoriteGadgetService.create(data).then(function (result) {
            console.log(result);
            if (result.status == "ok") {
              $scope.showAlert = true;
              $scope.isOK = "alertOK";
              $scope.message = result.message;
              $timeout(function () {
                $scope.hide()
              }, 1000);
            } else if (result.status == "error") {
              $scope.showAlert = true;
              $scope.isOK = "alertError";
              $scope.message = result.message;
            }
          });
      }
      
      $scope.validateImputIdentifier = function() {
        return !($scope.identifier!=null && $scope.identifier.trim().length>0);
      } 
      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };
      
    }





    vm.sendSelectEvent = function(element){
      window.dispatchEvent(new CustomEvent("gadgetselect",
      {
        detail: element
      }));
    }
  
  }
})();

(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('elementFullScreen', {
      templateUrl: 'app/components/view/elementFullScreenComponent/elementFullScreen.html',
      controller: ElementFullScreenController,
      controllerAs: 'vm',
      bindings:{
        element: "<",
        iframe: "=",
        editmode: "<",
        gridoptions: "="
      }
    });

  /** @ngInject */
  function ElementFullScreenController() {
    var vm = this;

    vm.$onInit = function () {
      vm.gridoptions = angular.copy(vm.gridoptions);
      vm.gridoptions.minCols = 1;
      vm.gridoptions.maxCols = 1;
      vm.gridoptions.minRows = 1;
      vm.gridoptions.maxRows = 1;
      vm.element.cols = 1;
      vm.element.rows = 1;
      vm.element.notshowDotsMenu = true;
    }
  }
})();

(function () {
  'use strict';

DatadiscoveryController.$inject = ["$log", "$scope", "datasourceSolverService", "httpService", "urlParamService", "utilsService"];
  angular.module('dashboardFramework')
    .component('datadiscovery', { 
      templateUrl: 'app/components/view/datadiscoveryComponent/datadiscovery.html',
      controller: DatadiscoveryController,
      controllerAs: 'vm',
      bindings:{
        id:"<?",             
        datastatus: "=?",
        filters: "="
      }
    });

  /** @ngInject */
  function DatadiscoveryController($log, $scope, datasourceSolverService, httpService, urlParamService, utilsService) {
    var vm = this;
    vm.ds;
    vm.reloadDataLink = function(reloadchild){//link function child
      vm.reloadDataF = reloadchild;
    };
    vm.getDataAndStyle = function(getDataAndStyleChild){//link function child
      vm.getDataAndStyleF = getDataAndStyleChild;
    };
    vm.type = "loading";
    vm.config = {};//Gadget database config
    vm.measures = [];
    vm.status = "initial";
    vm.selected = [];
    vm.notSmall=true;
    vm.showCheck = [];
    vm.showNoData = false;
    vm.startTime = 0;

    //Chaining filters, used to propagate own filters to child elements
    vm.filterChaining=true;

    vm.$onInit = function(){
      $scope.reloadContent();
    }

    $scope.reloadContent = function(){      
      /*Datadiscovery Editor Mode*/
      if(!vm.id){
       
        if(!vm.config.config){
          return;//Init editor triggered
        }
        if(typeof vm.config.config == "string"){
          vm.config.config = JSON.parse(vm.config.config);
        }
      }
      else{
      /*View Mode*/
        httpService.getGadgetConfigById(
          vm.id
        ).then( 
          function(config){
            if(config.data==="" ){
               throw new Error('Gadget was deleted');
            }
            vm.config=config.data;
            vm.config.config = JSON.parse(vm.config.config);
            vm.config.config.discovery = vm.config.config.discovery||{metrics:{list:[]},fields:{list:[]},columns:{list:[],subtotalField:-1}}
            return httpService.getGadgetMeasuresByGadgetId(vm.id);
          }
        ).then(
          function(measures){
            vm.measures = measures.data;
            vm.ds = measures.data[0].datasource;
          }
        ,function(e){
          if(e.message==='Gadget was deleted'){
              vm.type='removed'
              console.log('Gadget was deleted');
          }else{
              vm.type = 'nodata'
              console.log('Data no available'); 
          }
        })
      }

      utilsService.forceRender($scope);
      
      if(vm.reloadDataF){//call child function
        vm.reloadDataF();
      }
      
    }

    vm.$onChanges = function(changes) {

    };

    vm.$onDestroy = function(){
      
    }
}
})();

(function () {
  'use strict';

  EditDashboardController.$inject = ["$log", "$window", "__env", "$scope", "$mdSidenav", "$mdDialog", "$mdBottomSheet", "httpService", "interactionService", "urlParamService", "utilsService", "$translate", "localStorageService"];
  angular.module('dashboardFramework')
    .component('editDashboard', {
      templateUrl: 'app/components/edit/editDashboardComponent/edit.dashboard.html',
      controller: EditDashboardController,
      controllerAs: 'ed',
      bindings: {
        "dashboard":"=",
        "iframe":"=",
        "public":"&",
        "id":"&",
        "selectedpage" : "&",
        "synopticedit": "=?"
      }
    });

  /** @ngInject */
  function EditDashboardController($log, $window,__env, $scope, $mdSidenav, $mdDialog, $mdBottomSheet, httpService, interactionService, urlParamService,utilsService, $translate, localStorageService) {
    PagesController.$inject = ["$scope", "$mdDialog", "dashboard", "icons", "$timeout"];
    LayersController.$inject = ["$scope", "$mdDialog", "dashboard", "selectedpage", "selectedlayer"];
    DatasourcesController.$inject = ["$scope", "$mdDialog", "httpService", "dashboard", "selectedpage"];
    EditDashboardController.$inject = ["$scope", "$mdDialog", "dashboard", "$timeout"];
    EditDashboardStyleController.$inject = ["$scope", "$rootScope", "$mdDialog", "style", "$timeout"];
    EditDashboardHeaderLibsController.$inject = ["$scope", "httpService", "$mdDialog", "$window", "id"];
    DatalinkController.$inject = ["$scope", "$rootScope", "$mdDialog", "interactionService", "utilsService", "httpService", "dashboard", "selectedpage", "synopticedit"];
    UrlParamController.$inject = ["$scope", "$rootScope", "$mdDialog", "urlParamService", "utilsService", "httpService", "dashboard", "selectedpage"];
    EditDashboardHistoricalController.$inject = ["$scope", "__env", "$mdDialog", "localStorageService", "$window"];
    EditFavoriteGadgetListController.$inject = ["$scope", "__env", "$mdDialog", "favoriteGadgetService", "$window"];
    DialogController.$inject = ["$scope", "$mdDialog"];
    AddWidgetBottomSheetController.$inject = ["$scope", "$mdBottomSheet", "dashboard"];
    var ed = this;
    
    //Gadget source connection type list
    var typeGadgetList = ["pie","bar","map","livehtml","radar","table","mixed","line","wordcloud","gadgetfilter"];
   
    //ed.showButtons = true;
    ed.autoSaveActivated = false;
    ed.$onInit = function () {
      ed.selectedlayer = 0;

      //translate with url param
      $translate.use(utilsService.urlParamLang());

      //ed.selectedpage = ed.selectedpage;
      ed.icons = utilsService.icons;
      /*When global style change, that changes are broadcasted to all elements*/
      ed.global = {
        style: {
          header:{
            height: 25,
            enable: "initial",
            backgroundColor: "initial",
            title: {
              textColor: "initial",
              iconColor: "initial"
            }
          },
          border: {},
          backgroundColor: "initial",
          nomargin: false
        }
      };
      ; 
      setTimeout(function(){ ed.dragElement(document.getElementById("toolbarButtonsEdition"))}, 1000);
      if( ed.iframe == null || !ed.iframe){     
        
       if( ed.synopticedit == null || ed.synopticedit.showSynoptic == false){
           ed.autoSave();
           
       }
      }else{
        localStorageService.saveEnabled=false;
      }

    }

    //Control if show or Hide Edit Buttons when is  iframe
ed.showHideButtons = function () {
  if (ed.iframe) {
    if (typeof ed.dashboard != 'undefined' && typeof ed.dashboard.editButtonsIframe != 'undefined') {
      return ed.dashboard.editButtonsIframe.active;
    } else {
      return false;
    }
  } else {
    return true;
  }
}

ed.showHideTrashButton = function () {
  if (ed.iframe) {
    if (typeof ed.dashboard != 'undefined' && typeof ed.dashboard.editButtonsIframe != 'undefined') {
      return ed.dashboard.editButtonsIframe.trashButton;
    } else {
      return false;
    }
  } else {
    return true;
  }
}

ed.showHideCloseButton = function () {
  if (ed.iframe) {
    if (typeof ed.dashboard != 'undefined' && typeof ed.dashboard.editButtonsIframe != 'undefined') {
      return ed.dashboard.editButtonsIframe.closeButton;
    } else {
      return false;
    }
  } else {
    return true;
  }
}

ed.showHideConfigButton = function () {
  if (ed.iframe) {
    if (typeof ed.dashboard != 'undefined' && typeof ed.dashboard.editButtonsIframe != 'undefined') {
      return ed.dashboard.editButtonsIframe.configButton;
    } else {
      return false;
    }
  } else {
    return true;
  }
}

ed.showHideUrlParameterButton = function () {
  if (ed.iframe) {
    if (typeof ed.dashboard != 'undefined' && typeof ed.dashboard.editButtonsIframe != 'undefined') {
      return ed.dashboard.editButtonsIframe.urlParameterButton;
    } else {
      return false;
    }
  } else {
    return true;
  }
}


ed.showHideDataLinkButton = function () {
  if (ed.iframe) {
    if (typeof ed.dashboard != 'undefined' && typeof ed.dashboard.editButtonsIframe != 'undefined') {
      return ed.dashboard.editButtonsIframe.dataLinkButton;
    } else {
      return false;
    }
  } else {
    return true;
  }
}

ed.showHideAddElementButton = function () {
  if (ed.iframe) {
    if (typeof ed.dashboard != 'undefined' && typeof ed.dashboard.editButtonsIframe != 'undefined') {
      return ed.dashboard.editButtonsIframe.addElementButton;
    } else {
      return false;
    }
  } else {
    return true;
  }
}
ed.showHideMoveToolBarButton = function () {
  if (ed.iframe) {
    if (typeof ed.dashboard != 'undefined' && typeof ed.dashboard.editButtonsIframe != 'undefined') {
      return ed.dashboard.editButtonsIframe.moveToolBarButton;
    } else {
      return false;
    }
  } else {
    return true;
  }
}




     ed.dragElement = function(elmnt){
      var pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;
      if (document.getElementById("toolbarButtonsEditionMove")) {
        // if present, the header is where you move the DIV from:
        document.getElementById("toolbarButtonsEditionMove").onmousedown = dragMouseDown;
      } else {
        // otherwise, move the DIV from anywhere inside the DIV:
        if(elmnt!=null){
          elmnt.onmousedown = dragMouseDown;
        }
      }
    
      function dragMouseDown(e) {
        e = e || window.event;
        e.preventDefault();
        // get the mouse cursor position at startup:
        pos3 = e.clientX;
        pos4 = e.clientY;
        document.onmouseup = closeDragElement;
        // call a function whenever the cursor moves:
        document.onmousemove = elementDrag;
      }
    
      function elementDrag(e) {
        e = e || window.event;
        e.preventDefault();
        // calculate the new cursor position:      
        pos1 = pos3 - e.clientX;
        pos2 = pos4 - e.clientY;
        pos3 = e.clientX;
        pos4 = e.clientY;
        
        // set the element's new position:
        //elmnt.style.top = (elmnt.offsetTop - pos2) + "px";
        elmnt.style.left = (elmnt.offsetLeft - pos1) + "px";
      
      }
    
      function closeDragElement() {
        // stop moving when mouse button is released:
        document.onmouseup = null;
        document.onmousemove = null;
      }
    }

   


    ed.removePage = function (event, index) {
      event.stopPropagation();
      event.preventDefault();
      vm.dashboard.pages.splice(index, 1);
      $scope.$applyAsync();
    };

    ed.pagesEdit = function (ev) {
      $mdDialog.show({
        controller: PagesController,
        templateUrl: 'app/partials/edit/pagesDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          icons: ed.icons
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog pages closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.layersEdit = function (ev) {
      $mdDialog.show({
        controller: LayersController,
        templateUrl: 'app/partials/edit/layersDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          selectedpage: ed.selectedpage(),
          selectedlayer: ed.selectedlayer
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog layers closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.datasourcesEdit = function (ev) {
      $mdDialog.show({
        controller: DatasourcesController,
        templateUrl: 'app/partials/edit/datasourcesDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          selectedpage: ed.selectedpage()
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog datasources closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.dashboardEdit = function (ev) {
      $mdDialog.show({
        controller: EditDashboardController,
        templateUrl: 'app/partials/edit/editDashboardDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard
        }
      })
      .then(function(page) {
        $scope.status = 'Dashboard Edit closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.dashboardStyleEdit = function (ev) {
      $mdDialog.show({
        controller: EditDashboardStyleController,
        templateUrl: 'app/partials/edit/editDashboardStyleDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          style: ed.global.style
        }
      })
      .then(function(page) {
        $scope.status = 'Dashboard Edit closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.dashboardHeaderLibs = function (ev) {
      $mdDialog.show({
        controller: EditDashboardHeaderLibsController,
        templateUrl: 'app/partials/edit/editDashboardHeaderLibsDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          id: ed.id()
        }
      })
      .then(function(page) {
        $scope.status = 'Dashboard Edit Header Libs closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.showDatalink = function (ev) {
      $mdDialog.show({
        controller: DatalinkController,
        templateUrl: 'app/partials/edit/datalinkDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          selectedpage: ed.selectedpage(),
          synopticedit: ed.synopticedit
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog datalink closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.showUrlParam = function (ev) {
      $mdDialog.show({
        controller: UrlParamController,
        templateUrl: 'app/partials/edit/urlParamDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          selectedpage: ed.selectedpage()
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog url Param closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.dashboardHistoricalEdit = function (ev) {
      $mdDialog.show({
        controller: EditDashboardHistoricalController,
        templateUrl: 'app/partials/edit/editDashboardHistoricalDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard 
        }
      })
      .then(function(page) {
        $scope.status = 'Dashboard Historical closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.favoriteGadgetsList = function (ev) {
      $mdDialog.show({
        controller: EditFavoriteGadgetListController,
        templateUrl: 'app/partials/edit/editFavoriteGadgetListDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard 
        }
      })
      .then(function(page) {
        $scope.status = 'Dashboard Favorite Gadgets list closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };


    ed.changeZindexEditor = function (ev) {    
      if(ed.synopticedit.zindexEditor===600){
        ed.synopticedit.zindexEditor=0;
      }else{
        ed.synopticedit.zindexEditor=600;
      }
      $scope.$applyAsync();
    }
    ed.hideShowSynopticEditor = function (ev) {    
      ed.synopticedit.showEditor = !ed.synopticedit.showEditor;
      if(typeof $("#synoptic_editor")[0]!=='undefined'){
        ed.dashboard.synoptic =
        {
          svgImage:$("#synoptic_editor")[0].contentWindow.svgEditor.canvas.getSvgString(),
          conditions:Array.from($("#synoptic_editor")[0].contentWindow.svgEditor.getConditions())
        };
      }
     if( ed.iframe == null || !ed.iframe){
      if( ed.synopticedit.showEditor){
        ed.stopAutosave();
      }else{
        ed.startAutosave();
      }
    }
      $scope.$applyAsync();
      return ed.synopticedit.showEditor;
    }
 
     ed.saveSynopticAndDashboard = function (token) {        
      if(typeof $("#synoptic_editor")[0]!=='undefined'){
        ed.dashboard.synoptic =
        {
          svgImage:$("#synoptic_editor")[0].contentWindow.svgEditor.canvas.getSvgString(),
          conditions:Array.from($("#synoptic_editor")[0].contentWindow.svgEditor.getConditions())
        };
      }
       ed.dashboard.interactionHash = interactionService.getInteractionHashWithoutGadgetFilters();
       ed.dashboard.parameterHash = urlParamService.geturlParamHash();
       //console.log("synoptic saved");    
       return httpService.saveDashboardToken(ed.id(), {"data":{"model":JSON.stringify(ed.dashboard),"id":"","identification":"a","customcss":"","customjs":"","jsoni18n":"","description":"a","public":ed.public}},token); 
     }
     ed.autosavetimeout;
     ed.autoSave = function () {
       try {         
        if(typeof $("#synoptic_editor")[0]!=='undefined'){
          ed.dashboard.synoptic =
          {
            svgImage:$("#synoptic_editor")[0].contentWindow.svgEditor.canvas.getSvgString(),
            conditions:Array.from($("#synoptic_editor")[0].contentWindow.svgEditor.getConditions())
          };
        }
         
        ed.dashboard.interactionHash = interactionService.getInteractionHashWithoutGadgetFilters();
        ed.dashboard.parameterHash = urlParamService.geturlParamHash();      
        localStorageService.setItem(ed.id(),{"model":JSON.stringify(ed.dashboard)},"autoSave");       
        
        ed.autosavetimeout =  setTimeout(function(){ed.autoSave()},localStorageService.milliIntervalSave);  
        } catch (error) {          
          if (ed.autosavetimeout) {
            clearTimeout(ed.autosavetimeout);
          }
          ed.autosavetimeout = setTimeout(function(){ed.autoSave()},localStorageService.milliIntervalSave);  
        }
        ed.autoSaveActivated = true;    
     }

     ed.stopAutosave = function () {
      if (ed.autosavetimeout) {
        clearTimeout(ed.autosavetimeout);
        ed.autoSaveActivated = false;   
      }
    }

    ed.startAutosave = function () {        
      ed.autoSave();
    }







     ed.saveLocalByUser = function () {
        try {          
          if(typeof $("#synoptic_editor")[0]!=='undefined'){
            ed.dashboard.synoptic =
            {
              svgImage:$("#synoptic_editor")[0].contentWindow.svgEditor.canvas.getSvgString(),
              conditions:Array.from($("#synoptic_editor")[0].contentWindow.svgEditor.getConditions())
            };
          }
          ed.dashboard.interactionHash = interactionService.getInteractionHashWithoutGadgetFilters();
          ed.dashboard.parameterHash = urlParamService.geturlParamHash();                
          localStorageService.setItem(ed.id(),{"model":JSON.stringify(ed.dashboard)},"savedByUser");  
        } catch (error) {          
        }  
      }
    
    ed.savePage = function (ev) {      
      if(typeof $("#synoptic_editor")[0]!=='undefined'){
        ed.dashboard.synoptic =
        {
          svgImage:$("#synoptic_editor")[0].contentWindow.svgEditor.canvas.getSvgString(),
          conditions:Array.from($("#synoptic_editor")[0].contentWindow.svgEditor.getConditions())
        };
      }
      ed.dashboard.interactionHash = interactionService.getInteractionHashWithoutGadgetFilters();
      ed.dashboard.parameterHash = urlParamService.geturlParamHash();
      httpService.saveDashboard(ed.id(), {"data":{"model":JSON.stringify(ed.dashboard),"id":"","identification":"a","customcss":"","customjs":"","jsoni18n":"","description":"a","public":ed.public}}).then(
        function(d){
          if(d){
            $mdDialog.show({
              controller: DialogController,
              templateUrl: 'app/partials/edit/saveDialog.html',
              parent: angular.element(document.body),
              targetEvent: ev,
              clickOutsideToClose:true,
              fullscreen: false // Only for -xs, -sm breakpoints.
            })
            .then(function(answer) {
              $scope.status = 'You said the information was "' + answer + '".';
            }, function() {
              $scope.status = 'You cancelled the dialog.';
            });

          }
        }
      ).catch(
        function(d){
          if(d){           
            $mdDialog.show({
              controller: DialogController,
              templateUrl: 'app/partials/edit/saveErrorDialog.html',
              parent: angular.element(document.body),
              targetEvent: ev,
              clickOutsideToClose:true,
              fullscreen: false // Only for -xs, -sm breakpoints.
            })
            .then(function(answer) {
              $scope.status = 'You said the information was "' + answer + '".';
            }, function() {
              $scope.status = 'You cancelled the dialog.';
            });
          }
        }
      );
      //alert(JSON.stringify(ed.dashboard));
    };



    ed.getDataToSavePage = function (token) {    
      ed.dashboard.interactionHash = interactionService.getInteractionHashWithoutGadgetFilters();
      ed.dashboard.parameterHash = urlParamService.geturlParamHash();
    return httpService.saveDashboardToken(ed.id(), {"data":{"model":JSON.stringify(ed.dashboard),"id":"","identification":"a","customcss":"","customjs":"","jsoni18n":"","description":"a","public":ed.public}},token);

    };




    ed.showSaveOK = function (ev) {
      $mdDialog.show({
        controller: DialogController,
        templateUrl: 'app/partials/edit/saveDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false // Only for -xs, -sm breakpoints.
      })
    }



    function DialogController($scope, $mdDialog) {
      $scope.hide = function() {
        $mdDialog.hide();
      };
  
      $scope.cancel = function() {
        $mdDialog.cancel();
      };
  
      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };
    }

    ed.deleteDashboard = function (ev) {

      $mdDialog.show({
        controller: DialogController,
        templateUrl: 'app/partials/edit/askDeleteDashboardDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false // Only for -xs, -sm breakpoints.
      })
      .then(function(answer) {
       if(answer==="DELETE"){
        httpService.deleteDashboard(ed.id()).then(
          function(d){
            if(d){
              $mdDialog.show({
                controller: DialogController,
                templateUrl: 'app/partials/edit/deleteOKDialog.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:true,
                fullscreen: false // Only for -xs, -sm breakpoints.
              })
              .then(function(answer) {
                $window.location.href=__env.endpointControlPanel+'/dashboards/list';
              }, function() {
                $window.location.href=__env.endpointControlPanel+'/dashboards/list';
              });
            }
          }
        ).catch(
          function(d){
            if(d){
              $mdDialog.show({
                controller: DialogController,
                templateUrl: 'app/partials/edit/deleteErrorDialog.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:true,
                fullscreen: false // Only for -xs, -sm breakpoints.
              })
              .then(function(answer) {
                $scope.status = 'You said the information was "' + answer + '".';
              }, function() {
                $scope.status = 'You cancelled the dialog.';
              });
            }
          }
        );
        }
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });


     
    }

    
    ed.closeDashboard = function (ev) {

 
      $mdDialog.show({
        controller: DialogController,
        templateUrl: 'app/partials/edit/askCloseDashboardDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:false,
        fullscreen: false // Only for -xs, -sm breakpoints.
      })
      .then(function(answer) {
        if(answer==="SAVE"){
          ed.dashboard.interactionHash = interactionService.getInteractionHashWithoutGadgetFilters();
          ed.dashboard.parameterHash = urlParamService.geturlParamHash();
          httpService.saveDashboard(ed.id(), {"data":{"model":JSON.stringify(ed.dashboard),"id":"","identification":"a","customcss":"","customjs":"","jsoni18n":"","description":"a","public":ed.public}}).then(
            function(d){
              if(d){
                $mdDialog.show({
                  controller: DialogController,
                  templateUrl: 'app/partials/edit/saveDialog.html',
                  parent: angular.element(document.body),
                  targetEvent: ev,
                  clickOutsideToClose:true,
                  fullscreen: false // Only for -xs, -sm breakpoints.
                })
                .then(function(answer) {
                  httpService.freeResource(ed.id()).then(
                    function(t){ $window.location.href=__env.endpointControlPanel+'/dashboards/list';}
                    ).catch(
                      function(t){ $window.location.href=__env.endpointControlPanel+'/dashboards/list';}
                    );
                 
                }, function() {
                  $scope.status = 'You cancelled the dialog.';
                });
    
              }
            }
          ).catch(
            function(d){
              if(d){           
                $mdDialog.show({
                  controller: DialogController,
                  templateUrl: 'app/partials/edit/saveErrorDialog.html',
                  parent: angular.element(document.body),
                  targetEvent: ev,
                  clickOutsideToClose:true,
                  fullscreen: false // Only for -xs, -sm breakpoints.
                })
                .then(function(answer) {
                  $scope.status = 'You said the information was "' + answer + '".';
                }, function() {
                  $scope.status = 'You cancelled the dialog.';
                });
              }
            }
          );
        }
        else{         
          httpService.freeResource(ed.id()).then(
            function(t){ $window.location.href=__env.endpointControlPanel+'/dashboards/list';}
            ).catch(
              function(t){ $window.location.href=__env.endpointControlPanel+'/dashboards/list';}
            );         
        }
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });



    }


    ed.changedOptions = function changedOptions() {
      //main.options.api.optionsChanged();
    };

    function PagesController($scope, $mdDialog, dashboard, icons, $timeout) {
      $scope.dashboard = dashboard;
      $scope.icons = icons;
      $scope.auxUpload = [];
      $scope.apiUpload = [];

      function auxReloadUploads(){
        /*Load previous images*/
        $timeout(function(){
          for(var page = 0; page < $scope.dashboard.pages.length; page ++){
            if($scope.dashboard.pages[page].background.file.length > 0){
              $scope.apiUpload[page].addRemoteFile($scope.dashboard.pages[page].background.file[0].filedata,$scope.dashboard.pages[page].background.file[0].lfFileName,$scope.dashboard.pages[page].background.file[0].lfTagType);
            }
          }
        });
      }

      auxReloadUploads();

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.create = function() {
        var newPage = {};
        var newLayer = {};
        //newLayer.options = JSON.parse(JSON.stringify(ed.dashboard.pages[0].layers[0].options));
        newLayer.gridboard = [
        ];
        newLayer.title = "baseLayer";
        newPage.title = angular.copy($scope.title);
        newPage.icon = angular.copy($scope.selectedIconItem);
        newPage.layers = [newLayer];
        newPage.background = {}
        newPage.background.file = angular.copy($scope.file);
        newPage.background.color="hsl(0, 0%, 100%)";
        newPage.selectedlayer= 0;
        dashboard.pages.push(newPage);
        $scope.title = "";
        $scope.icon = "";
        $scope.file = [];
        auxReloadUploads();
        $scope.$applyAsync();
      };

      $scope.onFilesChange = function(index){
        dashboard.pages[index].background.file = $scope.auxUpload[index].file;
        if(dashboard.pages[index].background.file.length > 0){
          var FR = new FileReader();
          FR.onload = function(e) {
            dashboard.pages[index].background.filedata = e.target.result;
          };
          FR.readAsDataURL( dashboard.pages[index].background.file[0].lfFile );
        }
        else{
          dashboard.pages[index].background.filedata="";
        }
      }

      $scope.delete = function(index){
        dashboard.pages.splice(index, 1);
      }

      $scope.queryIcon = function (query) {
        return query ? $scope.icons.filter( createFilterFor(query) ) : $scope.icons;
      }

      /**
       * Create filter function for a query string
       */
      function createFilterFor(query) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(icon) {
          return (icon.indexOf(lowercaseQuery) != -1);
        };
      }

      $scope.moveUpPage = function(index){
        var temp = dashboard.pages[index];
        dashboard.pages[index] = dashboard.pages[index-1];
        dashboard.pages[index-1] = temp;
      }

      $scope.moveDownPage = function(index){
        var temp = dashboard.pages[index];
        dashboard.pages[index] = dashboard.pages[index+1];
        dashboard.pages[index+1] = temp;
      }
    }

    function LayersController($scope, $mdDialog, dashboard, selectedpage, selectedlayer) {
      $scope.dashboard = dashboard;
      $scope.selectedpage = selectedpage;
      $scope.selectedlayer = selectedlayer;
      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.create = function() {
        var newLayer = {}
        newLayer.gridboard = [
          {cols: 5, rows: 5, y: 0, x: 0, type:"mixed"},
          {cols: 2, rows: 2, y: 0, x: 2, hasContent: true},
          {cols: 1, rows: 1, y: 0, x: 4, type:"polar"},
          {cols: 1, rows: 1, y: 2, x: 5, type:"map"},
          {cols: 2, rows: 2, y: 1, x: 0}
        ];
        newLayer.title = angular.copy($scope.title);
        dashboard.pages[$scope.selectedpage].layers.push(newLayer);
        $scope.selectedlayer = dashboard.pages[$scope.selectedpage].layers.length-1;
        $scope.title = "";
        $scope.$applyAsync();
      };

      $scope.delete = function(index){
        dashboard.pages[$scope.selectedpage].layers.splice(index, 1);
      }

      $scope.queryIcon = function (query) {
        return query ? $scope.icons.filter( createFilterFor(query) ) : $scope.icons;
      }

      /**
       * Create filter function for a query string
       */
      function createFilterFor(query) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(icon) {
          return (icon.indexOf(lowercaseQuery) != -1);
        };
      }

      $scope.moveUpLayer = function(index){
        var temp = dashboard.pages[$scope.selectedpage].layers[index];
        dashboard.pages[$scope.selectedpage].layers[index] = dashboard.pages[$scope.selectedpage].layers[index-1];
        dashboard.pages[$scope.selectedpage].layers[index-1] = temp;
      }

      $scope.moveDownLayer = function(index){
        var temp = dashboard.pages[$scope.selectedpage].layers[index];
        dashboard.pages[$scope.selectedpage].layers[index] = dashboard.pages[$scope.selectedpage].layers[index+1];
        dashboard.pages[$scope.selectedpage].layers[index+1] = temp;
      }
    }

    function DatasourcesController($scope, $mdDialog, httpService, dashboard, selectedpage) {
      $scope.dashboard = dashboard;
      $scope.selectedpage = selectedpage;
      $scope.datasources = [];
      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.create = function() {
        var datasource = angular.copy($scope.datasource);
        dashboard.pages[$scope.selectedpage].datasources[datasource.identification]={triggers:[],type:datasource.type,interval:datasources.refresh};
        $scope.name = "";
        $scope.$applyAsync();
      };

      $scope.delete = function(key){
        delete dashboard.pages[$scope.selectedpage].datasources[key];
      }

      $scope.loadDatasources = function(){
        return httpService.getDatasources().then(
          function(response){
            $scope.datasources=response.data;
          },
          function(e){
            console.log("Error getting datasources: " +  JSON.stringify(e))
          }
        );
      }
    }

    function EditDashboardController($scope, $mdDialog, dashboard, $timeout) {
      $scope.dashboard = dashboard;

      function auxReloadUploads(){
        /*Load previous images*/
        $timeout(function(){
          if($scope.dashboard.header.logo.file && $scope.dashboard.header.logo.file.length > 0){
            $scope.apiUpload.addRemoteFile($scope.dashboard.header.logo.filedata,$scope.dashboard.header.logo.file[0].lfFileName,$scope.dashboard.header.logo.file[0].lfTagType);
          }
        });
      }

      $scope.onFilesChange = function(){
        $scope.dashboard.header.logo.file = $scope.auxUpload.file;
        if($scope.dashboard.header.logo.file.length > 0){
          var FR = new FileReader();
          FR.onload = function(e) {
            $scope.dashboard.header.logo.filedata = e.target.result;
          };
          FR.readAsDataURL( $scope.dashboard.header.logo.file[0].lfFile );
        }
        else{
          $scope.dashboard.header.logo.filedata="";
        }
      }

      auxReloadUploads();

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.changedOptions = function changedOptions() {
        $scope.dashboard.gridOptions.api.optionsChanged();
      };

    }

    function compareJSON(obj1, obj2) {
      var result = {};
      for(var key in obj1) {
          if(obj2[key] != obj1[key]) result[key] = obj2[key];
          if(typeof obj2[key] == 'array' && typeof obj1[key] == 'array')
              result[key] = compareJSON(obj1[key], obj2[key]);
          if(typeof obj2[key] == 'object' && typeof obj1[key] == 'object')
              result[key] = compareJSON(obj1[key], obj2[key]);
      }
      return result;
    }

    function EditDashboardStyleController($scope,$rootScope, $mdDialog, style, $timeout) {
      $scope.style = style;

      $scope.$watch('style',function(newValue, oldValue){
        if (newValue===oldValue) {
          return;
        }
        var diffs = compareJSON(oldValue, newValue);
        $rootScope.$broadcast('global.style', diffs);
      }, true)

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

    }

    function EditDashboardHeaderLibsController($scope, httpService, $mdDialog, $window, id) {
      $scope.id = id;

      httpService.getHeaderLibsById(id).then(
        function(data){
          document.querySelector("#headerlibseditor").style.height= window.getComputedStyle(document.querySelector("md-dialog")).height + "px"
          $scope.VSheaderlibseditor = monaco.editor.create(document.querySelector("#headerlibseditor"), {
            value: data.data,
            language: 'html',
            readOnly: false,
            scrollBeyondLastLine: false,
            theme: "vs-dark",
            automaticLayout: true
          });
        }
      )

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.saveAndReload = function() {
        httpService.saveHeaderLibsById(id,$scope.VSheaderlibseditor.getValue()).then(
          function(){
            $window.location.reload();
          }
        );
      };

    }

    function DatalinkController($scope,$rootScope, $mdDialog, interactionService, utilsService, httpService, dashboard, selectedpage,synopticedit) {
      $scope.dashboard = dashboard;
      $scope.selectedpage = selectedpage;
      $scope.synopticedit = synopticedit;
      

      initConnectionsList();
      generateGadgetsLists();

      function initConnectionsList(){
        $scope.connections = [];
        var rawInteractions = interactionService.getInteractionHashWithoutGadgetFilters()
        for(var source in rawInteractions){
          for(var indexFieldTargets in rawInteractions[source]){
            for(var indexTargets in rawInteractions[source][indexFieldTargets].targetList){
              var rowInteraction = {
                source:source,
                sourceField:rawInteractions[source][indexFieldTargets].emiterField,
                target:rawInteractions[source][indexFieldTargets].targetList[indexTargets].gadgetId,
                targetField:rawInteractions[source][indexFieldTargets].targetList[indexTargets].overwriteField,
                filterChaining:rawInteractions[source][indexFieldTargets].filterChaining
              }
              $scope.connections.push(rowInteraction);
            }
          }
        }
      }

      $scope.findEmitterGadgetID = function (prettyTitle){
        if( $scope.gadgetsSources!=null &&  $scope.gadgetsSources.length>0){
          for(var gad in $scope.gadgetsSources){
            if(prettyTitle == $scope.gadgetsSources[gad].prettyTitle){
              return $scope.gadgetsSources[gad].id;
            }
          }
        }
        return prettyTitle;
      }

      $scope.refreshGadgetEmitterFields = function(prettyTitle){
        var gadgetId = null;
        //find gadgetid 
        gadgetId = $scope.findEmitterGadgetID(prettyTitle);
       
        var gadget = findGadgetInDashboard(gadgetId);
        if(gadget == null){
          $scope.gadgetEmitterFields = [];
        }
        else{
          setGadgetEmitterFields(gadget);
        }
      }

      $scope.refreshGadgetTargetFields = function(gadgetId){
        var gadget = findGadgetInDashboard(gadgetId);
        if(gadget == null){
          $scope.gadgetEmitterFields = [];
        }
        else{
          setGadgetTargetFields(gadget);
        }
      }

      function setGadgetEmitterFields(gadget){
        switch(gadget.type){
          case "pie":
          case "bar":
          case "line":
          case "wordcloud":
          case "mixed":
            var gadgetMeasures = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.measures;
            $scope.emitterDatasource = gadgetMeasures[0].datasource.identification;
            $scope.gadgetEmitterFields = utilsService.sort_unique(gadgetMeasures.map(function(m){return m.config.fields[0]})).map(function(m){return {field:m}});
            break;
          case "radar":
            var gadgetMeasures = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.measures;
            $scope.emitterDatasource = gadgetMeasures[0].datasource.identification;
            $scope.gadgetEmitterFields = utilsService.sort_unique(gadgetMeasures.map(function(m){return m.config.fields[0]})).map(function(m){return {field:m}});
            break;
          case "map":
            var gadgetMeasures = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.measures;
            $scope.emitterDatasource = gadgetMeasures[0].datasource.identification;
            $scope.gadgetEmitterFields = utilsService.sort_unique(gadgetMeasures.map(function(m){return m.config.fields[2]})).map(function(m){return {field:m}});
            break;
          case "livehtml":
            var gadgetData = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm;
            if(typeof gadgetData.datasource !== 'undefined'){
                 $scope.emitterDatasource = gadgetData.datasource.name;
            }           
            if(typeof gadgetData.datasource !== 'undefined' && typeof gadgetData.datasource.id !== 'undefined'  ){
              httpService.getFieldsFromDatasourceId(gadgetData.datasource.id).then(
                function(data){
                  $scope.gadgetEmitterFields = utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", []));
                }
              )
            }
            $scope.gadgetEmitterFields = [];
            break;
          case "gadgetfilter":
            $scope.gadgetEmitterFields = [];
            var gadgetFilters = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.filters;
            if(typeof gadgetFilters!='undefined' && gadgetFilters!=null && gadgetFilters.length>0){
              for (var index = 0; index < gadgetFilters.length; index++) {
                var filter = gadgetFilters[index];
                $scope.gadgetEmitterFields.push({field:filter.id})
              }
            } 
            break;

          case "table":
            
            var gadgetMeasures = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.measures;
            $scope.emitterDatasource = gadgetMeasures[0].datasource.identification;
            $scope.gadgetEmitterFields = utilsService.sort_unique(gadgetMeasures.map(function(m){return m.config.fields[0]})).map(function(m){return {field:m}});
            break;
        }
      }

      //Destination are all gadget fields
      function setGadgetTargetFields(gadget){        
        $scope.targetDatasource="";
        var gadgetData = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm;
        if(gadget.type === 'livehtml'){
          if(typeof gadgetData.datasource !== 'undefined'){
            $scope.targetDatasource = gadgetData.datasource.name;
            var dsId = gadgetData.datasource.id;
          }
        }else if(gadget.type === 'gadgetfilter'){
          $scope.targetDatasource = gadgetData.datasource.name;
          var dsId = gadgetData.datasource.id;
        }
        else{
          $scope.targetDatasource = gadgetData.measures[0].datasource.identification;
          var dsId = gadgetData.measures[0].datasource.id;
        }
        if(typeof dsId !== 'undefined'){
          httpService.getFieldsFromDatasourceId(dsId).then(
            function(data){
              $scope.gadgetTargetFields = utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", []));
            }
          )
        }
        $scope.gadgetTargetFields = [];
      }

      //Get gadget JSON and return string info for UI
      $scope.prettyGadgetInfo = function (gadget) {
        return prettyGadgetInfo(gadget);
      }

      function prettyGadgetInfo(gadget) {
        if (gadget.type === 'synoptic') {
          return gadget.header.title.text;
        } else {
          return gadget.header.title.text + " (" + gadget.type + ")";
        }
      }


      $scope.generateGadgetInfo = function (gadgetId) {
        return generateGadgetInfo(gadgetId);
      }

      function generateGadgetInfo(gadgetId) {
        var gadget = findGadgetInDashboard(gadgetId);
        if (gadget == null) {
          return gadgetId;
        } else {
          return $scope.prettyGadgetInfo(gadget)
        }
      }

      function generateGadgetsLists(){
        $scope.gadgetsSources = getGadgetsSourcesInDashboard();       
        $scope.gadgetsTargets = getGadgetsInDashboard();
        if(typeof $scope.synopticedit !=='undefined' && typeof $scope.synopticedit.showSynoptic !=='undefined' && $scope.synopticedit.showSynoptic ){
         var synop = {id:'synoptic',header:{title:{text:'synoptic'}},type:'synoptic'};
         $scope.gadgetsSources = $scope.gadgetsSources.concat(synop);
         $scope.gadgetsTargets = $scope.gadgetsTargets.concat(synop);
        }
      }

      //Generate gadget list of posible Sources of interactions: pie, bar, livehtml
      function getGadgetsSourcesInDashboard(){        
        var gadgets = [];
        var page = $scope.dashboard.pages[$scope.selectedpage];
        for (var i = 0; i < page.layers.length; i++) {
          var layer = page.layers[i];
          var gadgetsAux = layer.gridboard.filter(function(gadget){return typeGadgetList.indexOf(gadget.type) != -1});
          if(gadgetsAux.length){
            gadgets = gadgets.concat(gadgetsAux);
          }
        }
        if(gadgets!=null && gadgets.length>0){
          for(var gad in gadgets){
            gadgets[gad].prettyTitle = prettyGadgetInfo(gadgets[gad]);
          }
        }
        return gadgets;
      }

      //Generate gadget list of posible Sources of interactions: pie, bar, livehtml
      function getGadgetsInDashboard(){
        var gadgets = [];
        var page = $scope.dashboard.pages[$scope.selectedpage];
        for (var i = 0; i < page.layers.length; i++) {
          var layer = page.layers[i];
          var gadgetsAux = layer.gridboard.filter(function(gadget){return typeof gadget.id != "undefined"});
          if(gadgetsAux.length){
            gadgets = gadgets.concat(gadgetsAux);
          }
        }
        return gadgets;
      }

      function findGadgetInDashboard(gadgetId){
        for(var p=0;p<$scope.dashboard.pages.length;p++){
          var page = $scope.dashboard.pages[p];       
          for (var i = 0; i < page.layers.length; i++) {
            var layer = page.layers[i];
            var gadgets = layer.gridboard.filter(function(gadget){return gadget.id === gadgetId});
            if(gadgets.length){
              return gadgets[0];
            }
          }
        }
        return null;
      }

      $scope.create = function(sourceGadgetId, originField , targetGadgetId, destinationField,filterChaining) {
        if(sourceGadgetId && originField && targetGadgetId && destinationField){
          interactionService.registerGadgetInteractionDestination(sourceGadgetId, targetGadgetId, originField, destinationField,undefined,filterChaining,undefined);
         
          initConnectionsList();
        }
      };

      $scope.delete = function(sourceGadgetId, targetGadgetId, originField, destinationField,filterChaining){
        interactionService.unregisterGadgetInteractionDestination(sourceGadgetId, originField, targetGadgetId, destinationField,filterChaining);
        initConnectionsList();
      }

      $scope.edit = function(sourceGadgetId, originField,targetGadgetId,  destinationField,filterChaining){
        $scope.refreshGadgetEmitterFields(sourceGadgetId);
        $scope.refreshGadgetTargetFields(targetGadgetId)
        $scope.emitterGadget = generateGadgetInfo(sourceGadgetId);
        $scope.emitterGadgetField = originField;
        $scope.targetGadget = targetGadgetId;        
        $scope.targetGadgetField = destinationField;
        $scope.filterChaining = filterChaining;
       
       }
 

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

    }



    function UrlParamController($scope,$rootScope, $mdDialog,urlParamService , utilsService, httpService, dashboard, selectedpage) {
      $scope.dashboard = dashboard;
      $scope.selectedpage = selectedpage;
      $scope.types=["string","number","string array","numbers array"];
      var rawParameters = urlParamService.geturlParamHash();

      initUrlParamList();
      generateGadgetsLists();

      function initUrlParamList(){
        $scope.parameters = [];
        for(var paramName in rawParameters){
          for(var indexFieldTargets in rawParameters[paramName]){
            for(var indexTargets in rawParameters[paramName][indexFieldTargets].targetList){
              var rowInteraction = {
                paramName:paramName,
                type:rawParameters[paramName][indexFieldTargets].type,
                mandatory:rawParameters[paramName][indexFieldTargets].mandatory,
                target:rawParameters[paramName][indexFieldTargets].targetList[indexTargets].gadgetId,
                targetField:rawParameters[paramName][indexFieldTargets].targetList[indexTargets].overwriteField
              }
              $scope.parameters.push(rowInteraction);
            }
          }
        }
      }

      $scope.refreshGadgetTargetFields = function(gadgetId){
        var gadget = findGadgetInDashboard(gadgetId);
        if(gadget != null){
          setGadgetTargetFields(gadget);
        }
      }

      //Destination are all gadget fields
      function setGadgetTargetFields(gadget){
        
        $scope.targetDatasource="";
        var gadgetData = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm;
        if(gadget.type === 'livehtml'){
          if(typeof gadgetData.datasource !== 'undefined'){
            $scope.targetDatasource = gadgetData.datasource.name;
            var dsId = gadgetData.datasource.id;
          }
        }else if(gadget.type === 'gadgetfilter'){
          if(typeof gadgetData.datasource !== 'undefined'){
            $scope.targetDatasource = gadgetData.datasource.name;
            var dsId = gadgetData.datasource.id;
          }
        }else {
          $scope.targetDatasource = gadgetData.measures[0].datasource.identification;
          var dsId = gadgetData.measures[0].datasource.id;
        }
        if(typeof dsId !== 'undefined'){
          httpService.getFieldsFromDatasourceId(dsId).then(
            function(data){
              $scope.gadgetTargetFields = utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", []));
            }
          )
         }
        $scope.gadgetTargetFields = [];
      }

      //Get gadget JSON and return string info for UI
      $scope.prettyGadgetInfo = function(gadget){
       
          return gadget.header.title.text + " (" + gadget.type + ")";
        
      }

      $scope.generateGadgetInfo = function (gadgetId){
        var gadget = findGadgetInDashboard(gadgetId);
        if(gadget == null){
          return gadgetId;
        }
        else{
          return $scope.prettyGadgetInfo(gadget)
        }
      }

      function generateGadgetsLists(){
     
        $scope.gadgetsTargets = getGadgetsInDashboard();
      }

      //Generate gadget list of posible Sources of interactions: pie, bar, livehtml
      function getGadgetsInDashboard(){
        var gadgets = [];
        var page = $scope.dashboard.pages[$scope.selectedpage];
        for (var i = 0; i < page.layers.length; i++) {
          var layer = page.layers[i];
          var gadgetsAux = layer.gridboard.filter(function(gadget){return typeof gadget.id != "undefined"});
          if(gadgetsAux.length){
            gadgets = gadgets.concat(gadgetsAux);
          }
        }
        return gadgets;
      }

      function findGadgetInDashboard(gadgetId){
        for(var p=0;p<$scope.dashboard.pages.length;p++){
          var page = $scope.dashboard.pages[p];       
          for (var i = 0; i < page.layers.length; i++) {
            var layer = page.layers[i];
            var gadgets = layer.gridboard.filter(function(gadget){return gadget.id === gadgetId});
            if(gadgets.length){
              return gadgets[0];
            }
          }
        }
        return null;
      }

      $scope.create = function(parameterName, parameterType , targetGadgetId, destinationField, mandatory) {
        if(parameterName && parameterType && targetGadgetId && destinationField){
          urlParamService.registerParameter(parameterName, parameterType, targetGadgetId, destinationField, mandatory);
          initUrlParamList();
        }
      };

      $scope.delete = function(parameterName, parameterType , targetGadgetId, destinationField, mandatory){
        urlParamService.unregisterParameter(parameterName, parameterType , targetGadgetId, destinationField, mandatory);
        initUrlParamList();
      }
      $scope.edit = function(parameterName, parameterType , targetGadgetId, destinationField, mandatory){
       $scope.refreshGadgetTargetFields(targetGadgetId);
       $scope.paramName=parameterName;
       $scope.type=parameterType;
       $scope.targetGadget=targetGadgetId;
       
       $scope.targetGadgetField=destinationField;
       $scope.mandatory=mandatory;
      }

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

    }

    function EditDashboardHistoricalController($scope, __env, $mdDialog, localStorageService,$window) {
      function init(){        
        localStorageService.getDateItems(ed.id()).then(function(dates){
          $scope.dates = dates;
        });
      }
      init();
      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.selectDate = function(milis){   
    
       $window.location.href=__env.endpointControlPanel+'/dashboards/editfull/'+ed.id()+'?__hist_dash='+milis; 
     
       $mdDialog.hide();
      }

      
      $scope.refreshServerVersion = function(){
        $window.location.href=__env.endpointControlPanel+'/dashboards/editfull/'+ed.id();
        $mdDialog.hide();
      }  
      $scope.pushsaveLocalByUser = function(){
        ed.saveLocalByUser();
        $mdDialog.hide();
      }
      $scope.cancel = function() {
        $mdDialog.cancel();
      };


    }

    function EditFavoriteGadgetListController($scope, __env, $mdDialog, favoriteGadgetService, $window) {
      function init(){        
        favoriteGadgetService.getAllIdentifications().then(function(identifications){
          $scope.identifications = identifications;
        });
      }
      init();

      $scope.delete = function(identification){
        favoriteGadgetService.delete(identification).then(function(){
          favoriteGadgetService.getAllIdentifications().then(function(identifications){
            $scope.identifications = identifications;
          });
        });
      }

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };


    }


    ed.showListBottomSheet = function() {
      $window.dispatchEvent(new Event("resize"));      
      $mdBottomSheet.show({
        templateUrl: 'app/partials/edit/addWidgetBottomSheet.html',
        controller: AddWidgetBottomSheetController,
        disableParentScroll: false,
        disableBackdrop: true,
        clickOutsideToClose: true,
        locals: {
          dashboard: ed.dashboard
        }
      }).then(function(clickedItem) {
        $scope.alert = clickedItem['name'] + ' clicked!';
        
      }).catch(function(error) {
        // User clicked outside or hit escape
      });
      
    };

    ed.toolbarButtonsAssignclass  = function() {
    
      if(ed.synopticedit.showSynoptic === true && ed.synopticedit.showEditor === true){        
        return 'toolbarButtonsSynopticEditor';
      }else if(ed.synopticedit.showSynoptic === true && ed.synopticedit.showEditor === false){        
        return 'toolbarButtonsSynoptic';
      }else{        
        return 'toolbarButtons';
      }
    }


    function AddWidgetBottomSheetController($scope, $mdBottomSheet, dashboard){
      $scope.dashboard = dashboard;

      $scope.checkGadgetType = function(type){
        if( $scope.dashboard.gridOptions.enableEmptyCellDrag){
          $scope.dashboard.gridOptions.dragGadgetType = type;
          var elems = document.getElementsByClassName("dragg-button-gad");
          for (var i = 0; i < elems.length; i++) {
            if(elems[i].id != type){
              elems[i].style.borderColor = "#E6EDF3";
            }
            else{
              elems[i].style.borderColor = "#FF5522";
            }
          }
        }
      }

      $scope.closeBottomSheet = function() {         
        $mdBottomSheet.hide();
      }
    }

    $scope.$on('deleteElement',function (event, item) {
      var dashboard = $scope.ed.dashboard;
      var page = dashboard.pages[$scope.ed.selectedpage()];
      var layer = page.layers[page.selectedlayer];
      layer.gridboard.splice(layer.gridboard.indexOf(item), 1);
      $scope.$applyAsync();
    });
  }
})();

angular.module('dashboardFramework').value('cacheBoard', {});

(function () {
  'use strict';

  UtilsService.$inject = ["__env"];
  angular.module('dashboardFramework')
    .service('utilsService', UtilsService);

  /** @ngInject */
  function UtilsService(__env) {
    var vm = this;

    //force angular render in order to fast refresh view of component. $scope is pass as argument for render only this element
    vm.forceRender = function ($scope) {
      if (!$scope.$$phase) {
        $scope.$applyAsync();
      }
    }

    //Access json by string dot path
    function multiIndex(obj, is, pos) {  // obj,['1','2','3'] -> ((obj['1'])['2'])['3']
      if (is.length && !(is[0] in obj)) {
        return obj[is[is.length - 1]];
      }
      return is.length ? multiIndex(obj[is[0]], is.slice(1), pos) : obj
    }

    function isNormalInteger(str) {
      var n = Math.floor(Number(str));
      return n !== Infinity && String(n) === str && n >= 0;
    }

    vm.replaceBrackets = function (obj) {
      obj = obj.replace(/[\[]/g, ".");
      obj = obj.replace(/[\]]/g, "");
      return obj;
    }

    vm.getJsonValueByJsonPath = function (obj, is, pos) {
      //special case for array access, return key is 0, 1
      var matchArray = is.match(/\[[0-9]\]*$/);
      if (matchArray) {
        //Get de match in is [0] and get return field name
        return obj[pos];
      }
      return multiIndex(obj, is.split('.'))
    }

    //array transform to sorted and unique values
    vm.sort_unique = function (arr) {
      if (arr.length === 0) return arr;
      var sortFn;
      if (typeof arr[0] === "string") {//String sort
        sortFn = function (a, b) {
          if (a < b) return -1;
          if (a > b) return 1;
          return 0;
        }
      }
      else {//Number and date sort
        sortFn = function (a, b) {
          return a * 1 - b * 1;
        }
      }
      arr = arr.sort(sortFn);
      var ret = [arr[0]];
      for (var i = 1; i < arr.length; i++) { //Start loop at 1: arr[0] can never be a duplicate
        if (arr[i - 1] !== arr[i]) {
          ret.push(arr[i]);
        }
      }
      return ret;
    }

    //array transform to sorted and unique values
    vm.sort_jsonarray = function(arr,sortfield) {
      if (arr.length === 0) return arr;
      var sortFn;
      if(typeof arr[0][sortfield] === "string"){//String sort
        sortFn = function (a, b) {
          if(a[sortfield] < b[sortfield]) return -1;
          if(a[sortfield] > b[sortfield]) return 1;
          return 0;
        }
      }
      else{//Number and date sort
        sortFn = function (a, b) {
          return a[sortfield]*1 - b[sortfield]*1;
        }
      }
      return arr.sort(sortFn);
    }

    vm.isSameJsonInArray = function (json, arrayJson) {
      for (var index = 0; index < arrayJson.length; index++) {
        var equals = true;
        for (var key in arrayJson[index]) {
          if (arrayJson[index][key] != json[key]) {
            equals = false;
            break;
          }
        }
        if (equals) {
          return true;
        }
      }
      return false;
    }  

    vm.getJsonFields = function iterate(obj, stack, fields) {
      for (var property in obj) {
        if (obj.hasOwnProperty(property)) {
          if (typeof obj[property] == "object") {
            vm.getJsonFields(obj[property], stack + (stack == "" ? '' : '.') + property, fields);
          } else {
            fields.push({ field: stack + (stack == "" ? '' : '.') + property, type: typeof obj[property] });
          }
        }
      }
      return fields;
    }



    function distinct(value, index, self) {
      return self.indexOf(value) === index;
    }

    vm.uniqueArray = function (arr) {
      if (typeof arr !== undefined) {
        return arr.filter(distinct);
      }
      return arr;
    }



    vm.transformJsonFieldsArrays = function (fields) {
      var transformArrays = [];
      for (var fieldAux in fields) {
        var pathFields = fields[fieldAux].field.split(".");
        var realField = pathFields[0];
        for (var i = 1; i < pathFields.length; i++) {
          if (isNormalInteger(pathFields[i])) {
            pathFields[i] = "[" + pathFields[i] + "]"
            realField += pathFields[i];
          }
          else {
            realField += "." + pathFields[i];
          }
        }
        transformArrays.push({ field: realField, type: fields[fieldAux].type });
      }
      return transformArrays;
    }

    vm.urlParamLang = function () {
      //controlar si ponen minúsculas o mayusculas
      var urlSearch = window.location.search;
      var searchParam = new URLSearchParams(urlSearch);
      var lang = searchParam.get("lang");
      return (lang?lang.toUpperCase():"");
    }

    vm.getMarkerForMap = function (value, jsonMarkers) {

      var result = {
        type: 'vectorMarker',
        icon: 'circle',
        markerColor: 'blue',
        iconColor: "white"
      }
      var found = false;
      for (var index = 0; index < jsonMarkers.length && !found; index++) {
        var limit = jsonMarkers[index];
        var minUndefined = typeof limit.min == "undefined";
        var maxUndefined = typeof limit.max == "undefined";

        if (!minUndefined && !maxUndefined) {
          if (value <= limit.max && value >= limit.min) {
            result.icon = limit.icon;
            result.markerColor = limit.markerColor;
            result.iconColor = limit.iconColor;
            found = true;
          }
        } else if (!minUndefined && maxUndefined) {
          if (value >= limit.min) {
            result.icon = limit.icon;
            result.markerColor = limit.markerColor;
            result.iconColor = limit.iconColor;
            found = true;
          }

        } else if (minUndefined && !maxUndefined) {
          if (value <= limit.max) {
            result.icon = limit.icon;
            result.markerColor = limit.markerColor;
            result.iconColor = limit.iconColor;
            found = true;
          }

        }

      }

      return result;
    }

    vm.isEmptyJson = function (obj) {
      return Object.keys(obj).length === 0 && obj.constructor === Object;
    }

    /**method that finds the tags in the given text*/
    vm.searchTag = function(regex,str){
      var m;
      var found=[];
      while ((m = regex.exec(str)) !== null) {  
          if (m.index === regex.lastIndex) {
              regex.lastIndex++;
          }
          m.forEach(function(item, index, arr){			
          found.push(arr[0]);			
        });  
      }
      return found;
    }

    vm.searchTagContentName = function(regex,str){
      var m;
      var content;
      while ((m = regex.exec(str)) !== null) {  
          if (m.index === regex.lastIndex) {
              regex.lastIndex++;
          }
          m.forEach(function(item, index, arr){			
            content = arr[0].match(/"([^"]+)"/)[1];			
        });  
      }
      return content;
    }

    /**method that finds the options attribute and returns its values in the given tag */
    vm.searchTagContentOptions = function(regex,str){
      var m;
      var content=" ";
      while ((m = regex.exec(str)) !== null) {  
          if (m.index === regex.lastIndex) {
              regex.lastIndex++;
          }
          m.forEach(function(item, index, arr){			
            content = arr[0].match(/"([^"]+)"/)[1];			
        });  
      }
    
      return  content.split(',');
    }

    /**find a value for a given parameter */
    function findValueForParameter(parameters,label,jsparam,number){
      for (var index = 0; index <  parameters.length; index++) {
        var element =  parameters[index];
        if(element.label===label){
          if(!jsparam){
            return element.value;
          }
          else{
            if(number){
              return element.value + " || ";
            }
            else{
              return "'" + element.value + "' || ";
            }
          }
        }
      }
    }

    /**Parse the parameter of the data source so that it has array coding*/
    function parseArrayPosition(str){
      var regex = /\.[\d]+/g;
      var m;              
      while ((m = regex.exec(str)) !== null) {                
          if (m.index === regex.lastIndex) {
              regex.lastIndex++;
          } 
          m.forEach( function(item, index, arr){             
            var index = arr[0].substring(1,arr[0].length)
            var result =  "["+index+"]";
            str = str.replace(arr[0],result) ;
          });
      }
      return str;
    }

    /** this function Replace parameteres for his selected values*/
    vm.parseProperties = function(str,parameters,jsparam){
      var regexTagHTML =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\-->/g;
      var regexTagJS =  /\/\*[\-\-\s\w\>\=\"\'\,\:\+\_\/]*\*\//g;
      var regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;
      var regexOptions = /options\s*=\s*\"[\s\w\>\=\-\'\:\,\+\_\/]*\s*\"/g;
      var found=[];
      found = vm.searchTag(regexTagHTML,str).concat(vm.searchTag(regexTagJS,str));	
  
      var parserList=[];
      for (var i = 0; i < found.length; i++) {
        var tag = found[i];			
       
        if(tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){                 
          parserList.push({tag:tag,value:findValueForParameter(parameters,vm.searchTagContentName(regexName,tag),jsparam)});   
        }else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
          parserList.push({tag:tag,value:findValueForParameter(parameters,vm.searchTagContentName(regexName,tag),jsparam,true)});   
        }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){                
          var field = parseArrayPosition(findValueForParameter(parameters,vm.searchTagContentName(regexName,tag)).field);
          if(!jsparam){                             
            parserList.push({tag:tag,value:"{{ds[0]."+field+"}}"});
          }
          else{
            parserList.push({tag:tag,value:"ds[0]."+field+" || "});
          }
        }else if(tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){                
          var field = parseArrayPosition(findValueForParameter(parameters,vm.searchTagContentName(regexName,tag)).field);
          if(!jsparam){                             
            parserList.push({tag:tag,value:field});
          }
          else{
            parserList.push({tag:tag,value:"'" + field + "' || "});
          }                            
        }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-osp')>=0){                
          parserList.push({tag:tag,value:findValueForParameter(parameters,vm.searchTagContentName(regexName,tag),jsparam)});  
        }
      } 
      //Replace parameteres for his values
      for (var i = 0; i < parserList.length; i++) {
        str = str.replace(parserList[i].tag,parserList[i].value);
      }
      return str;
    }

    vm.icons = [
      "3d_rotation",
      "ac_unit",
      "access_alarm",
      "access_alarms",
      "access_time",
      "accessibility",
      "accessible",
      "account_balance",
      "account_balance_wallet",
      "account_box",
      "account_circle",
      "adb",
      "add",
      "add_a_photo",
      "add_alarm",
      "add_alert",
      "add_box",
      "add_circle",
      "add_circle_outline",
      "add_location",
      "add_shopping_cart",
      "add_to_photos",
      "add_to_queue",
      "adjust",
      "airline_seat_flat",
      "airline_seat_flat_angled",
      "airline_seat_individual_suite",
      "airline_seat_legroom_extra",
      "airline_seat_legroom_normal",
      "airline_seat_legroom_reduced",
      "airline_seat_recline_extra",
      "airline_seat_recline_normal",
      "airplanemode_active",
      "airplanemode_inactive",
      "airplay",
      "airport_shuttle",
      "alarm",
      "alarm_add",
      "alarm_off",
      "alarm_on",
      "album",
      "all_inclusive",
      "all_out",
      "android",
      "announcement",
      "apps",
      "archive",
      "arrow_back",
      "arrow_downward",
      "arrow_drop_down",
      "arrow_drop_down_circle",
      "arrow_drop_up",
      "arrow_forward",
      "arrow_upward",
      "art_track",
      "aspect_ratio",
      "assessment",
      "assignment",
      "assignment_ind",
      "assignment_late",
      "assignment_return",
      "assignment_returned",
      "assignment_turned_in",
      "assistant",
      "assistant_photo",
      "attach_file",
      "attach_money",
      "attachment",
      "audiotrack",
      "autorenew",
      "av_timer",
      "backspace",
      "backup",
      "battery_alert",
      "battery_charging_full",
      "battery_full",
      "battery_std",
      "battery_unknown",
      "beach_access",
      "beenhere",
      "block",
      "bluetooth",
      "bluetooth_audio",
      "bluetooth_connected",
      "bluetooth_disabled",
      "bluetooth_searching",
      "blur_circular",
      "blur_linear",
      "blur_off",
      "blur_on",
      "book",
      "bookmark",
      "bookmark_border",
      "border_all",
      "border_bottom",
      "border_clear",
      "border_color",
      "border_horizontal",
      "border_inner",
      "border_left",
      "border_outer",
      "border_right",
      "border_style",
      "border_top",
      "border_vertical",
      "branding_watermark",
      "brightness_1",
      "brightness_2",
      "brightness_3",
      "brightness_4",
      "brightness_5",
      "brightness_6",
      "brightness_7",
      "brightness_auto",
      "brightness_high",
      "brightness_low",
      "brightness_medium",
      "broken_image",
      "brush",
      "bubble_chart",
      "bug_report",
      "build",
      "burst_mode",
      "business",
      "business_center",
      "cached",
      "cake",
      "call",
      "call_end",
      "call_made",
      "call_merge",
      "call_missed",
      "call_missed_outgoing",
      "call_received",
      "call_split",
      "call_to_action",
      "camera",
      "camera_alt",
      "camera_enhance",
      "camera_front",
      "camera_rear",
      "camera_roll",
      "cancel",
      "card_giftcard",
      "card_membership",
      "card_travel",
      "casino",
      "cast",
      "cast_connected",
      "center_focus_strong",
      "center_focus_weak",
      "change_history",
      "chat",
      "chat_bubble",
      "chat_bubble_outline",
      "check",
      "check_box",
      "check_box_outline_blank",
      "check_circle",
      "chevron_left",
      "chevron_right",
      "child_care",
      "child_friendly",
      "chrome_reader_mode",
      "class",
      "clear",
      "clear_all",
      "close",
      "closed_caption",
      "cloud",
      "cloud_circle",
      "cloud_done",
      "cloud_download",
      "cloud_off",
      "cloud_queue",
      "cloud_upload",
      "code",
      "collections",
      "collections_bookmark",
      "color_lens",
      "colorize",
      "comment",
      "compare",
      "compare_arrows",
      "computer",
      "confirmation_number",
      "contact_mail",
      "contact_phone",
      "contacts",
      "content_copy",
      "content_cut",
      "content_paste",
      "control_point",
      "control_point_duplicate",
      "copyright",
      "create",
      "create_new_folder",
      "credit_card",
      "crop",
      "crop_16_9",
      "crop_3_2",
      "crop_5_4",
      "crop_7_5",
      "crop_din",
      "crop_free",
      "crop_landscape",
      "crop_original",
      "crop_portrait",
      "crop_rotate",
      "crop_square",
      "dashboard",
      "data_usage",
      "date_range",
      "dehaze",
      "delete",
      "delete_forever",
      "delete_sweep",
      "description",
      "desktop_mac",
      "desktop_windows",
      "details",
      "developer_board",
      "developer_mode",
      "device_hub",
      "devices",
      "devices_other",
      "dialer_sip",
      "dialpad",
      "directions",
      "directions_bike",
      "directions_boat",
      "directions_bus",
      "directions_car",
      "directions_railway",
      "directions_run",
      "directions_subway",
      "directions_transit",
      "directions_walk",
      "disc_full",
      "dns",
      "do_not_disturb",
      "do_not_disturb_alt",
      "do_not_disturb_off",
      "do_not_disturb_on",
      "dock",
      "domain",
      "done",
      "done_all",
      "donut_large",
      "donut_small",
      "drafts",
      "drag_handle",
      "drive_eta",
      "dvr",
      "edit",
      "edit_location",
      "eject",
      "email",
      "enhanced_encryption",
      "equalizer",
      "error",
      "error_outline",
      "euro_symbol",
      "ev_station",
      "event",
      "event_available",
      "event_busy",
      "event_note",
      "event_seat",
      "exit_to_app",
      "expand_less",
      "expand_more",
      "explicit",
      "explore",
      "exposure",
      "exposure_neg_1",
      "exposure_neg_2",
      "exposure_plus_1",
      "exposure_plus_2",
      "exposure_zero",
      "extension",
      "face",
      "fast_forward",
      "fast_rewind",
      "favorite",
      "favorite_border",
      "featured_play_list",
      "featured_video",
      "feedback",
      "fiber_dvr",
      "fiber_manual_record",
      "fiber_new",
      "fiber_pin",
      "fiber_smart_record",
      "file_download",
      "file_upload",
      "filter",
      "filter_1",
      "filter_2",
      "filter_3",
      "filter_4",
      "filter_5",
      "filter_6",
      "filter_7",
      "filter_8",
      "filter_9",
      "filter_9_plus",
      "filter_b_and_w",
      "filter_center_focus",
      "filter_drama",
      "filter_frames",
      "filter_hdr",
      "filter_list",
      "filter_none",
      "filter_tilt_shift",
      "filter_vintage",
      "find_in_page",
      "find_replace",
      "fingerprint",
      "first_page",
      "fitness_center",
      "flag",
      "flare",
      "flash_auto",
      "flash_off",
      "flash_on",
      "flight",
      "flight_land",
      "flight_takeoff",
      "flip",
      "flip_to_back",
      "flip_to_front",
      "folder",
      "folder_open",
      "folder_shared",
      "folder_special",
      "font_download",
      "format_align_center",
      "format_align_justify",
      "format_align_left",
      "format_align_right",
      "format_bold",
      "format_clear",
      "format_color_fill",
      "format_color_reset",
      "format_color_text",
      "format_indent_decrease",
      "format_indent_increase",
      "format_italic",
      "format_line_spacing",
      "format_list_bulleted",
      "format_list_numbered",
      "format_paint",
      "format_quote",
      "format_shapes",
      "format_size",
      "format_strikethrough",
      "format_textdirection_l_to_r",
      "format_textdirection_r_to_l",
      "format_underlined",
      "forum",
      "forward",
      "forward_10",
      "forward_30",
      "forward_5",
      "free_breakfast",
      "fullscreen",
      "fullscreen_exit",
      "functions",
      "g_translate",
      "gamepad",
      "games",
      "gavel",
      "gesture",
      "get_app",
      "gif",
      "golf_course",
      "gps_fixed",
      "gps_not_fixed",
      "gps_off",
      "grade",
      "gradient",
      "grain",
      "graphic_eq",
      "grid_off",
      "grid_on",
      "group",
      "group_add",
      "group_work",
      "hd",
      "hdr_off",
      "hdr_on",
      "hdr_strong",
      "hdr_weak",
      "headset",
      "headset_mic",
      "healing",
      "hearing",
      "help",
      "help_outline",
      "high_quality",
      "highlight",
      "highlight_off",
      "history",
      "home",
      "hot_tub",
      "hotel",
      "hourglass_empty",
      "hourglass_full",
      "http",
      "https",
      "image",
      "image_aspect_ratio",
      "import_contacts",
      "import_export",
      "important_devices",
      "inbox",
      "indeterminate_check_box",
      "info",
      "info_outline",
      "input",
      "insert_chart",
      "insert_comment",
      "insert_drive_file",
      "insert_emoticon",
      "insert_invitation",
      "insert_link",
      "insert_photo",
      "invert_colors",
      "invert_colors_off",
      "iso",
      "keyboard",
      "keyboard_arrow_down",
      "keyboard_arrow_left",
      "keyboard_arrow_right",
      "keyboard_arrow_up",
      "keyboard_backspace",
      "keyboard_capslock",
      "keyboard_hide",
      "keyboard_return",
      "keyboard_tab",
      "keyboard_voice",
      "kitchen",
      "label",
      "label_outline",
      "landscape",
      "language",
      "laptop",
      "laptop_chromebook",
      "laptop_mac",
      "laptop_windows",
      "last_page",
      "launch",
      "layers",
      "layers_clear",
      "leak_add",
      "leak_remove",
      "lens",
      "library_add",
      "library_books",
      "library_music",
      "lightbulb_outline",
      "line_style",
      "line_weight",
      "linear_scale",
      "link",
      "linked_camera",
      "list",
      "live_help",
      "live_tv",
      "local_activity",
      "local_airport",
      "local_atm",
      "local_bar",
      "local_cafe",
      "local_car_wash",
      "local_convenience_store",
      "local_dining",
      "local_drink",
      "local_florist",
      "local_gas_station",
      "local_grocery_store",
      "local_hospital",
      "local_hotel",
      "local_laundry_service",
      "local_library",
      "local_mall",
      "local_movies",
      "local_offer",
      "local_parking",
      "local_pharmacy",
      "local_phone",
      "local_pizza",
      "local_play",
      "local_post_office",
      "local_printshop",
      "local_see",
      "local_shipping",
      "local_taxi",
      "location_city",
      "location_disabled",
      "location_off",
      "location_on",
      "location_searching",
      "lock",
      "lock_open",
      "lock_outline",
      "looks",
      "looks_3",
      "looks_4",
      "looks_5",
      "looks_6",
      "looks_one",
      "looks_two",
      "loop",
      "loupe",
      "low_priority",
      "loyalty",
      "mail",
      "mail_outline",
      "map",
      "markunread",
      "markunread_mailbox",
      "memory",
      "menu",
      "merge_type",
      "message",
      "mic",
      "mic_none",
      "mic_off",
      "mms",
      "mode_comment",
      "mode_edit",
      "monetization_on",
      "money_off",
      "monochrome_photos",
      "mood",
      "mood_bad",
      "more",
      "more_horiz",
      "more_vert",
      "motorcycle",
      "mouse",
      "move_to_inbox",
      "movie",
      "movie_creation",
      "movie_filter",
      "multiline_chart",
      "music_note",
      "music_video",
      "my_location",
      "nature",
      "nature_people",
      "navigate_before",
      "navigate_next",
      "navigation",
      "near_me",
      "network_cell",
      "network_check",
      "network_locked",
      "network_wifi",
      "new_releases",
      "next_week",
      "nfc",
      "no_encryption",
      "no_sim",
      "not_interested",
      "note",
      "note_add",
      "notifications",
      "notifications_active",
      "notifications_none",
      "notifications_off",
      "notifications_paused",
      "offline_pin",
      "ondemand_video",
      "opacity",
      "open_in_browser",
      "open_in_new",
      "open_with",
      "pages",
      "pageview",
      "palette",
      "pan_tool",
      "panorama",
      "panorama_fish_eye",
      "panorama_horizontal",
      "panorama_vertical",
      "panorama_wide_angle",
      "party_mode",
      "pause",
      "pause_circle_filled",
      "pause_circle_outline",
      "payment",
      "people",
      "people_outline",
      "perm_camera_mic",
      "perm_contact_calendar",
      "perm_data_setting",
      "perm_device_information",
      "perm_identity",
      "perm_media",
      "perm_phone_msg",
      "perm_scan_wifi",
      "person",
      "person_add",
      "person_outline",
      "person_pin",
      "person_pin_circle",
      "personal_video",
      "pets",
      "phone",
      "phone_android",
      "phone_bluetooth_speaker",
      "phone_forwarded",
      "phone_in_talk",
      "phone_iphone",
      "phone_locked",
      "phone_missed",
      "phone_paused",
      "phonelink",
      "phonelink_erase",
      "phonelink_lock",
      "phonelink_off",
      "phonelink_ring",
      "phonelink_setup",
      "photo",
      "photo_album",
      "photo_camera",
      "photo_filter",
      "photo_library",
      "photo_size_select_actual",
      "photo_size_select_large",
      "photo_size_select_small",
      "picture_as_pdf",
      "picture_in_picture",
      "picture_in_picture_alt",
      "pie_chart",
      "pie_chart_outlined",
      "pin_drop",
      "place",
      "play_arrow",
      "play_circle_filled",
      "play_circle_outline",
      "play_for_work",
      "playlist_add",
      "playlist_add_check",
      "playlist_play",
      "plus_one",
      "poll",
      "polymer",
      "pool",
      "portable_wifi_off",
      "portrait",
      "power",
      "power_input",
      "power_settings_new",
      "pregnant_woman",
      "present_to_all",
      "print",
      "priority_high",
      "public",
      "publish",
      "query_builder",
      "question_answer",
      "queue",
      "queue_music",
      "queue_play_next",
      "radio",
      "radio_button_checked",
      "radio_button_unchecked",
      "rate_review",
      "receipt",
      "recent_actors",
      "record_voice_over",
      "redeem",
      "redo",
      "refresh",
      "remove",
      "remove_circle",
      "remove_circle_outline",
      "remove_from_queue",
      "remove_red_eye",
      "remove_shopping_cart",
      "reorder",
      "repeat",
      "repeat_one",
      "replay",
      "replay_10",
      "replay_30",
      "replay_5",
      "reply",
      "reply_all",
      "report",
      "report_problem",
      "restaurant",
      "restaurant_menu",
      "restore",
      "restore_page",
      "ring_volume",
      "room",
      "room_service",
      "rotate_90_degrees_ccw",
      "rotate_left",
      "rotate_right",
      "rounded_corner",
      "router",
      "rowing",
      "rss_feed",
      "rv_hookup",
      "satellite",
      "save",
      "scanner",
      "schedule",
      "school",
      "screen_lock_landscape",
      "screen_lock_portrait",
      "screen_lock_rotation",
      "screen_rotation",
      "screen_share",
      "sd_card",
      "sd_storage",
      "search",
      "security",
      "select_all",
      "send",
      "sentiment_dissatisfied",
      "sentiment_neutral",
      "sentiment_satisfied",
      "sentiment_very_dissatisfied",
      "sentiment_very_satisfied",
      "settings",
      "settings_applications",
      "settings_backup_restore",
      "settings_bluetooth",
      "settings_brightness",
      "settings_cell",
      "settings_ethernet",
      "settings_input_antenna",
      "settings_input_component",
      "settings_input_composite",
      "settings_input_hdmi",
      "settings_input_svideo",
      "settings_overscan",
      "settings_phone",
      "settings_power",
      "settings_remote",
      "settings_system_daydream",
      "settings_voice",
      "share",
      "shop",
      "shop_two",
      "shopping_basket",
      "shopping_cart",
      "short_text",
      "show_chart",
      "shuffle",
      "signal_cellular_4_bar",
      "signal_cellular_connected_no_internet_4_bar",
      "signal_cellular_no_sim",
      "signal_cellular_null",
      "signal_cellular_off",
      "signal_wifi_4_bar",
      "signal_wifi_4_bar_lock",
      "signal_wifi_off",
      "sim_card",
      "sim_card_alert",
      "skip_next",
      "skip_previous",
      "slideshow",
      "slow_motion_video",
      "smartphone",
      "smoke_free",
      "smoking_rooms",
      "sms",
      "sms_failed",
      "snooze",
      "sort",
      "sort_by_alpha",
      "spa",
      "space_bar",
      "speaker",
      "speaker_group",
      "speaker_notes",
      "speaker_notes_off",
      "speaker_phone",
      "spellcheck",
      "star",
      "star_border",
      "star_half",
      "stars",
      "stay_current_landscape",
      "stay_current_portrait",
      "stay_primary_landscape",
      "stay_primary_portrait",
      "stop",
      "stop_screen_share",
      "storage",
      "store",
      "store_mall_directory",
      "straighten",
      "streetview",
      "strikethrough_s",
      "style",
      "subdirectory_arrow_left",
      "subdirectory_arrow_right",
      "subject",
      "subscriptions",
      "subtitles",
      "subway",
      "supervisor_account",
      "surround_sound",
      "swap_calls",
      "swap_horiz",
      "swap_vert",
      "swap_vertical_circle",
      "switch_camera",
      "switch_video",
      "sync",
      "sync_disabled",
      "sync_problem",
      "system_update",
      "system_update_alt",
      "tab",
      "tab_unselected",
      "tablet",
      "tablet_android",
      "tablet_mac",
      "tag_faces",
      "tap_and_play",
      "terrain",
      "text_fields",
      "text_format",
      "textsms",
      "texture",
      "theaters",
      "thumb_down",
      "thumb_up",
      "thumbs_up_down",
      "time_to_leave",
      "timelapse",
      "timeline",
      "timer",
      "timer_10",
      "timer_3",
      "timer_off",
      "title",
      "toc",
      "today",
      "toll",
      "tonality",
      "touch_app",
      "toys",
      "track_changes",
      "traffic",
      "train",
      "tram",
      "transfer_within_a_station",
      "transform",
      "translate",
      "trending_down",
      "trending_flat",
      "trending_up",
      "tune",
      "turned_in",
      "turned_in_not",
      "tv",
      "unarchive",
      "undo",
      "unfold_less",
      "unfold_more",
      "update",
      "usb",
      "verified_user",
      "vertical_align_bottom",
      "vertical_align_center",
      "vertical_align_top",
      "vibration",
      "video_call",
      "video_label",
      "video_library",
      "videocam",
      "videocam_off",
      "videogame_asset",
      "view_agenda",
      "view_array",
      "view_carousel",
      "view_column",
      "view_comfy",
      "view_compact",
      "view_day",
      "view_headline",
      "view_list",
      "view_module",
      "view_quilt",
      "view_stream",
      "view_week",
      "vignette",
      "visibility",
      "visibility_off",
      "voice_chat",
      "voicemail",
      "volume_down",
      "volume_mute",
      "volume_off",
      "volume_up",
      "vpn_key",
      "vpn_lock",
      "wallpaper",
      "warning",
      "watch",
      "watch_later",
      "wb_auto",
      "wb_cloudy",
      "wb_incandescent",
      "wb_iridescent",
      "wb_sunny",
      "wc",
      "web",
      "web_asset",
      "weekend",
      "whatshot",
      "widgets",
      "wifi",
      "wifi_lock",
      "wifi_tethering",
      "work",
      "wrap_text",
      "youtube_searched_for",
      "zoom_in",
      "zoom_out",
      "zoom_out_map"
    ]


  };
})();

(function () {
  'use strict';

  urlParamService.$inject = ["$log", "__env", "$rootScope"];
  angular.module('dashboardFramework')
    .service('urlParamService', urlParamService);

  /** @ngInject */
  function urlParamService($log, __env, $rootScope) {
    
    var vm = this;
    //Gadget interaction hash table, {gadgetsource:{emiterField:"field1", targetList: [{gadgetId,overwriteField}]}}
    vm.urlParamHash = {

    };

    vm.seturlParamHash = function(urlParamHash){
      vm.urlParamHash = urlParamHash;
    };

    vm.geturlParamHash = function(){
      return vm.urlParamHash;
    };

    vm.geturlParamHashForTargetGadget = function (targetGadgetId) {
        var resultHash = JSON.parse(JSON.stringify(vm.urlParamHash));        
        if (Object.keys(resultHash).length > 0) {
          for (var keyGadget in resultHash) {
            var destinationList = resultHash[keyGadget];
            for (var keyGDest in destinationList) {
              var destination = destinationList[keyGDest];
              destination.targetList = destination.targetList.filter(function(targ){
               return targ.gadgetId == targetGadgetId;
              }) ;             
            }
            //clean if targetList is empty 
            resultHash[keyGadget] = destinationList.filter(function(dest){
              return dest.targetList.length>0;
             }) ;             
          }
          //clean empty resultHash property
          for (var keyGadget in resultHash) {            
            if(resultHash[keyGadget]==null || resultHash[keyGadget].length==0){
            delete resultHash[keyGadget];
            }
          }
        }
        return resultHash;
        }

    vm.registerParameter = function (parameterName, parameterType, targetGadgetId, destinationField, mandatory) {
      //Auto generated
      
      if(!(parameterName in vm.urlParamHash) ){        
        vm.urlParamHash[parameterName] = [];
        vm.urlParamHash[parameterName].push(
          {
            targetList: [],
            type: parameterType,
            mandatory:mandatory
          }
        )
      }
      var parameter = vm.urlParamHash[parameterName];
        parameter[0].mandatory = mandatory;
        parameter[0].type = parameterType;
        var found = -1;
        for (var keyGDest in parameter[0].targetList) {
          var destination = parameter[0].targetList[keyGDest];
          if (destination.gadgetId == targetGadgetId) {
            found = keyGDest;
            destination.gadgetId = targetGadgetId;
            destination.overwriteField = destinationField;
          }
        }
        if (found == -1) {
          parameter[0].targetList.push({
          gadgetId: targetGadgetId,
          overwriteField: destinationField        
          })
        }
    };

    

    vm.unregisterParameter = function (parameterName, parameterType, targetGadgetId, destinationField, mandatory) {      
      var parameter = vm.urlParamHash[parameterName].filter(
        function (elem) {
          return elem.type == parameterType && elem.mandatory == mandatory;
        }
      );
      var found = -1;
      parameter[0].targetList.map(
        function (dest, index) {
          if (dest.overwriteField == destinationField && dest.gadgetId == targetGadgetId) {
            found = index;
          }
        }
      );
      if (found != -1) {
        parameter[0].targetList.splice(found, 1);
      }
      if(parameter[0].targetList.length == 0){
       delete vm.urlParamHash[parameterName];
      }
    };

    vm.unregisterGadget = function (gadgetId) {    
      //Delete from destination list
      for (var keyGadget in vm.urlParamHash) {
        var destinationList = vm.urlParamHash[keyGadget];
        for (var keyDest in destinationList) {
          var destinationFieldBundle = destinationList[keyDest];
          var found = -1; //-1 not found other remove that position in targetList array
          for (var keyGDest in destinationFieldBundle.targetList) {
            var destination = destinationFieldBundle.targetList[keyGDest];
            if (destination.gadgetId == gadgetId) {
              found = keyGDest;
              break;
            }
          }
          //delete targetList entry if diferent -1
          if (found != -1) {
            destinationBundle.targetList.splice(found, 1);
          }
        }
      }
    };


  vm.generateFiltersForGadgetId = function (gadgetId){    
  var filterList=[];
  for (var keyParameter in vm.urlParamHash) {
    var destinationList = vm.urlParamHash[keyParameter];
    for (var keyDest in destinationList) {
      var destinationFieldBundle = destinationList[keyDest];
      var found = -1; //-1 not found other remove that position in targetList array
      for (var keyGDest in destinationFieldBundle.targetList) {
        var destination = destinationFieldBundle.targetList[keyGDest];
        if (destination.gadgetId == gadgetId) {
          if(__env.urlParameters.hasOwnProperty(keyParameter)){
            filterList.push(buildFilter(keyParameter,keyDest,keyGDest))
          }
          break;
        }
      }
     
    }
  }
  return filterList;
}

    function buildFilter(keyParameter,keyDest,keyGDest){      
      var value = __env.urlParameters[keyParameter];
      var field = vm.urlParamHash[keyParameter][keyDest].targetList[keyGDest].overwriteField;
      var op ="=";
      if(vm.urlParamHash[keyParameter][keyDest].type === "string"){ 
        value = "\"" + value + "\"";
      }else if(vm.urlParamHash[keyParameter][keyDest].type === "number"){
        value = Number(value);
      }else if(vm.urlParamHash[keyParameter][keyDest].type === "string array"){
        value =  "(" +createIn(value,true) +")";
        op = 'IN';
      }else if(vm.urlParamHash[keyParameter][keyDest].type === "numbers array"){
        value =  "(" +createIn(value,false) +")";
        op = 'IN';
      }
     
     var filter = {
      field: field,
      op: op,
      exp: value
    };
    
      return filter     
    }

    function createIn(values,quotes){
      var signals =[];
      var optionsSelected = values.replace(/%20/g, " ").split(',');
      for(var index = 0; index < optionsSelected.length; index++){
        if(quotes){         
          signals.push("'"+optionsSelected[index]+"'");
        }else{
          signals.push(optionsSelected[index]);
        }
      }
     return signals.join(",");
    }
    

    vm.checkParameterMandatory = function (){
      var result = [];
      for (var keyParameter in vm.urlParamHash) {
        var param = vm.urlParamHash[keyParameter];
        for (var keyDest in param) {
          if(param[keyDest].mandatory){
            if(!__env.urlParameters.hasOwnProperty(keyParameter)){
              result.push({name:keyParameter,val:""});
            }            
          }
        }
      }
      return result;
    }

    vm.generateUrlWithParam=function(url,parameters){
      var result = "";
      if((Object.getOwnPropertyNames(__env.urlParameters).length + parameters.length)>0){
        result = "?"; 
        
        for (var name in __env.urlParameters) {
          if(result.length==1){
            result=result+name+"="+__env.urlParameters[name];
          }else{
            result=result+"&"+name+"="+__env.urlParameters[name];
          }
        }
        for (var index in parameters) {
          if(result.length==1){
            result=result+parameters[index].name+"="+parameters[index].val;
          }else{
            result=result+"&"+parameters[index].name+"="+parameters[index].val;
          }
        }

      }
      return result;
    }

    //{param, value, op}
    vm.sendBroadcastParams = function (params, originid) {
      var destlist = {};
      for(var iparam in params){
        var param = params[iparam].param;
        var value = params[iparam].value;
        var op = params[iparam].op;
        if(param in vm.urlParamHash){
          for(var itgadget in vm.urlParamHash[param][0].targetList){
            var deststt = vm.urlParamHash[param][0].targetList[itgadget];
            if(deststt.gadgetId in destlist){
              destlist[deststt.gadgetId].data.push(
                {
                  "field": deststt.overwriteField, 
                  "value": value,
                  "op": op || "="
                }
              )
            }
            else{
              destlist[deststt.gadgetId] = {
                "type": "filter",
                "id": originid,
                "data": [
                  {
                    "field": deststt.overwriteField, 
                    "value": value,
                    "op": op || "="
                  }
                ]
              }
            }
          }
        }
      }

      for(var g in destlist){
        emitToTargets(g, destlist[g]);
      }
    }

    vm.sendBroadcastParam = function (param, value, op, originid) {

      if(param in vm.urlParamHash){
        vm.urlParamHash[param][0].targetList.map(
          function(destg){
            emitToTargets(destg.gadgetId, { 
                "type": "filter",
                "id": originid,
                "data": [
                  {
                    "field": destg.overwriteField, 
                    "value": value,
                    "op": op || "="
                  }
                ]
              }
            )
          }
        )
      }
    }

    function emitToTargets(id, data) {
      $rootScope.$broadcast(id, data);
    }
  };
})();

(function () {
  'use strict';

  SocketService.$inject = ["$stomp", "$log", "__env", "$timeout", "$q"];
  angular.module('dashboardFramework')
    .service('socketService', SocketService);

  /** @ngInject */
  function SocketService($stomp, $log, __env, $timeout, $q) {
      var vm = this;

      vm.stompClient = {};
      vm.hashRequestResponse = {};
      vm.connected = false;
      vm.queue = {};
      vm.resetHeartBeatCallback;//call on connection and when some data is received for reset scheduling

      $stomp.setDebug(function (args) {
        $log.debug(args)
      });

      $stomp.setDebug(false);

      var errorfn = function(error){
        console.log("Error websockets: " + error + " , reconnecting...");
        $timeout(vm.connect,2000);
      }

      vm.connect = function(heartBeatCallback,enableListenerCallback){
        $stomp.connect(__env.socketEndpointConnect+ "?" + (__env.dashboardEngineOauthtoken?"oauthtoken=" +__env.dashboardEngineOauthtoken:'anonymous'), [], errorfn, __env.dashboardEngineProtocol === 'websocket'?{ transports: ['websocket']}:{}).then(
          function(frame){
            if(frame.command == "CONNECTED"){
              console.log('%c DSEngine Websocket Connected    ' + '%c ' + new Date(), 'color: #1e8fff; font-weight:bold; font-size:13px', 'color: #bbb; font-weight:bold; font-size:13px');
              vm.connected=true;
              Object.keys(vm.queue).map(
                function(dskey){
                  vm.sendAndSubscribe(vm.queue[dskey], true);
                }
              )
              if(heartBeatCallback){
                vm.resetHeartBeatCallback = heartBeatCallback;
                vm.resetHeartBeatCallback();
              }
              if(enableListenerCallback){
                enableListenerCallback();
              }
            }
            else{
              console.log("Error websockets, reconnecting... " + new Date())
              $timeout(vm.connect,2000);
            }
          }
        ).catch(
          errorfn
        );
      }

      vm.connectAndSendAndSubscribe = function(reqrespList){
        $stomp
          .connect(__env.socketEndpointConnect, [])
          .then(function (frame) {
            for(var reqrest in reqrespList){
              var UUID = generateConnectionUUID();
              vm.hashRequestResponse[UUID] = reqrespList[reqrest];
              vm.hashRequestResponse[UUID].subscription = $stomp.subscribe(__env.socketEndpointSubscribe + "/" + UUID, function (payload, headers, res) {
                var answerId = headers.destination.split("/").pop();
                vm.hashRequestResponse[answerId].callback(vm.hashRequestResponse[answerId].id,payload);
                // Unsubscribe
                vm.hashRequestResponse[answerId].subscription.unsubscribe();//Unsubscribe
              })
              // Send message
              $stomp.send(__env.socketEndpointSend + "/" + UUID, reqrespList[reqrest].msg)
            }
          })
        };

      vm.sendAndSubscribe = function(datasource,ignoreQueue){
        if(vm.connected && (ignoreQueue || notInQueue(datasource))){
          var UUID = generateConnectionUUID();
          if (!ignoreQueue) {
            addToQueue(datasource);
          }
          vm.hashRequestResponse[UUID] = getFromQueue(datasource);//get array callback from queue
          vm.hashRequestResponse[UUID].subscription = $stomp.subscribe(__env.socketEndpointSubscribe + "/" + UUID, function (payload, headers, res) {
            var answerId = headers.destination.split("/").pop();
            for (var i=0;i < vm.hashRequestResponse[answerId].callbacks.length;i++) {
              vm.hashRequestResponse[answerId].callbacks[i](vm.hashRequestResponse[answerId].id,vm.hashRequestResponse[answerId].type,payload);
            }
            // Unsubscribe
            vm.hashRequestResponse[UUID].subscription.unsubscribe();//Unsubscribe
            removeFromQueue(vm.hashRequestResponse[UUID]);//datasource remove
            function deleteHash(UUID){
              $timeout(
                function(){
                  delete vm.hashRequestResponse[UUID];
                },
                0
              );
            }
            deleteHash(UUID);
            vm.resetHeartBeatCallback();
          })

          // Send message
          var datasourcefinal;
          if (datasource.msg.filter && datasource.msg.filter.length > 0 && datasource.msg.filter[0].id) {
            datasourcefinal = JSON.parse(JSON.stringify(datasource));
            datasourcefinal.msg.filter = datasourcefinal.msg.filter.map(function (d) {
              return d.data[0]
            })
          } else {
            datasourcefinal = datasource
          }
          $stomp.send(__env.socketEndpointSend + "/" + UUID, datasourcefinal.msg)
        }
        else{
          addToQueue(datasource);
        }
      }

      vm.cleanqueue = function(reqrespList){
        vm.queue = {};
      }
      vm.disconnect = function(reqrespList){
        var deferred = $q.defer();
       
        $stomp.disconnect().then(function () {
          $log.info('disconnected');
          vm.connected = false;
          deferred.resolve();
        })
        return deferred.promise;
      }

      //provisional method, could be use hash key
      function generateDatasourceKey(datasource){
        var keyobj = {
          id: datasource.id,
          msg: datasource.msg,
          type: datasource.type          
        }
        return JSON.stringify(keyobj);
      }

      function notInQueue(datasource){
        return !vm.queue.hasOwnProperty(generateDatasourceKey(datasource));
      }

      function addToQueue(datasource){
        var key = generateDatasourceKey(datasource);
        if (datasource.id !== 1 || !(key in vm.queue)) {// 1 is from vm.from
          vm.queue[key] = {};
          vm.queue[key].id = datasource.id;
          vm.queue[key].msg = datasource.msg;
          vm.queue[key].type = datasource.type;
          vm.queue[key].callbacks = [datasource.callback];
        } else {
          vm.queue[key].callbacks.push(datasource.callback);
        }
      }

      function getFromQueue(datasource){
        return vm.queue[generateDatasourceKey(datasource)];
      }

      function removeFromQueue(datasource){
        delete vm.queue[generateDatasourceKey(datasource)];
      }

      vm.addListenerForHeartbeat = function(callback){
        $stomp.sock.addEventListener("heartbeat",callback);
      }

      vm.isConnected = function(){
        return vm.connected;
      }

      function generateConnectionUUID(){
        var newUUID = (new Date()).getTime() + Math.floor(((Math.random()*1000000)));
        while(newUUID in vm.hashRequestResponse){
          newUUID = generateConnectionUUID();
        }
        return newUUID;
      }
  };
})();

(function () {
  'use strict';

  LocalStorageService.$inject = ["__env", "$q"];
  angular.module('dashboardFramework')
    .service('localStorageService', LocalStorageService);

  /** @ngInject */
  function LocalStorageService(__env,$q) {

    var vm = this;
    var DB_NAME = 'onesaitplatform';
    var DB_VERSION = 1; // Use a long long for this value (don't use a float)    
    var DB_STORE_NAME = 'dashboards';
    
   


    vm.maxItems = typeof __env.maxItemsLocalStorage !=='undefined'?__env.maxItemsLocalStorage:5;
    vm.milliIntervalSave = typeof __env.milliIntervalSaveLocalStorage !=='undefined'?__env.milliIntervalSaveLocalStorage:10000;
    vm.saveEnabled = true;




    vm.getLastItemDate = function (id) {  
      var deferred = $q.defer();    
        getItemLocalStorage(id).then(function(modelArray){
          if ( modelArray === null || modelArray.savedByUser === null || modelArray.savedByUser.length === 0) {            
            deferred.resolve( null);
          } else{       
            deferred.resolve( JSON.parse(modelArray.savedByUser[modelArray.savedByUser.length - 1].date));
          }
      });
      return deferred.promise;
    }

    vm.getDateItems = function (id) {
      var deferred = $q.defer(); 
        getItemLocalStorage(id).then(function(modelArray){
          var dates = {autoSave:[],savedByUser:[]};
      
          if ( modelArray !== null && modelArray.autoSave.length > 0) {
            modelArray.autoSave.forEach(function (elem) {
              dates.autoSave.push({milis:elem.date,date:new Date(elem.date).toLocaleString(),model:JSON.parse(elem.model)});
            });
          }
          if ( modelArray !== null && modelArray.savedByUser.length > 0) {
            modelArray.savedByUser.forEach(function (elem) {
              dates.savedByUser.push({milis:elem.date,date:new Date(elem.date).toLocaleString(),model:JSON.parse(elem.model)});
            });
          }        
          deferred.resolve(dates);
        });
        return deferred.promise;
    }

    vm.getItemByIdAndDate = function (id, savedDate) {
     var deferred = $q.defer();
     getItemLocalStorage(id).then(function(modelArray){
           
      if (modelArray !== null && modelArray.savedByUser !== null && modelArray.savedByUser.length > 0) {
        for (var index = 0; index < modelArray.savedByUser.length; index++) {
          var element = modelArray.savedByUser[index];
          if(element.date+'' === savedDate+''){            
            deferred.resolve(JSON.parse(element.model));
          }
        }        
      }

      if (modelArray !== null && modelArray.autoSave !== null && modelArray.autoSave.length > 0) {
        for (var index = 0; index < modelArray.autoSave.length; index++) {
          var element = modelArray.autoSave[index];
          if(element.date+'' === savedDate+''){            
            deferred.resolve(JSON.parse(element.model));
          }
        }        
      }
    })
          return deferred.promise;
    }

    vm.isAfterSavedDate = function (id, savedDate) {
      var deferred = $q.defer(); 
        vm.getLastItemDate(id).then(function(itemDate){
        if ( itemDate !== null) {           
          deferred.resolve(savedDate >= itemDate);
        }else{
          deferred.resolve(true);
        }
      });     
      return deferred.promise;
    }

    vm.modelsAreEqual = function (modelA, modelB) {
      return JSON.stringify(modelA) === JSON.stringify(modelB);       
    }
    //{date:"",model:""}
    vm.setItem = function (id, newModel,origin,timemillis) {  
      
      if(vm.saveEnabled){ 
        getItemLocalStorage(id).then(function(modelArray){           
          if ( modelArray === null) {
            modelArray = {autoSave:[],savedByUser:[]};
          }

          var saveItem = true;
          if (modelArray[origin].length > 0) {
            if (vm.modelsAreEqual(newModel.model, modelArray[origin][modelArray[origin].length - 1].model)) {
              saveItem = false;
            }
          }
          if (saveItem) {
            var savedtime ;
            if(typeof timemillis == 'undefined'){
              savedtime = new Date().getTime();
            } 
            else{
              savedtime = timemillis;
            }
            if (modelArray[origin].length < vm.maxItems) {
              modelArray[origin].push({
                date: savedtime,
                model: newModel.model
              });
            } else {
              modelArray[origin].shift();
              modelArray[origin].push({
                date: savedtime,
                model: newModel.model
              });             
            }
          }
          setItemLocalStorage(id, modelArray);
        })
      }
    }

  
    function setItemLocalStorage(id, modelArray) { 
           
      getObjectStore(DB_STORE_NAME, 'readwrite').then(
        function (store) {
          var request = store.get(id);
          request.onerror = function(event) {
          // Handle errors!
          };
          request.onsuccess = function(event) {
              // Get the old value that we want to update
                var data = event.target.result;      
              if (typeof data=='undefined'){
                var dat = {id:id,value:modelArray};
                store.add(dat);
              }
              else{
                var dat = {id:id,value:modelArray};
                store.put(dat);
              }      
        }
      })
    }
       

      function getItemLocalStorage(id){
      
      var deferred = $q.defer();
       getObjectStore(DB_STORE_NAME, 'readonly').then(
            function (store) {
              var request = store.get(id);
              request.onerror = function(event) {
              deferred.resolve( null);
              };
              request.onsuccess = function(event) {
                if(typeof request.result!='undefined'){              
                  deferred.resolve(request.result.value);
                }else{
                  deferred.resolve(null);
                }
              };
            })
            return deferred.promise;
          }
     
    function getObjectStore(store_name, mode) {
        var deferred = $q.defer();
             openDb().then(
            function (db) {
              var tx = db.transaction(store_name, mode);
              deferred.resolve(tx.objectStore(store_name));
          });
          return deferred.promise;
    }
  
       vm.db=null;
       function openDb () {
        var deferred = $q.defer();
        if(vm.db==null){       
        var req = indexedDB.open(DB_NAME, DB_VERSION);
        req.onsuccess = function (evt) {
          vm.db=this.result
          deferred.resolve(vm.db);         
        };
        req.onerror = function (evt) {
          console.error("openDb:", evt.target.errorCode);
          deferred.reject();
        };
        req.onupgradeneeded = function (evt) {         
          var store = evt.currentTarget.result.createObjectStore(
            DB_STORE_NAME, { keyPath: 'id', autoIncrement: false });
            store.createIndex('id', 'id', { unique: true });  
            store.createIndex('value', 'value', { unique: false }); 
            openDb().then(
              function (db) {
                deferred.resolve(vm.db);  
              })
                         
        };
      }else{
        deferred.resolve(vm.db);
      }
        return deferred.promise;
    }
  };
})();
(function () {
  'use strict';

  InteractionService.$inject = ["$log", "__env", "$rootScope", "datasourceSolverService"];
  angular.module('dashboardFramework')
    .service('interactionService', InteractionService);

  /** @ngInject */
  function InteractionService($log, __env, $rootScope, datasourceSolverService) {
    
    var vm = this;
    //Gadget interaction hash table, {gadgetsource:{emiterField:"field1", targetList: [{gadgetId,overwriteField}]}}
    vm.interactionHash = {

    };

    vm.setInteractionHash = function(interactionHash){   
         
      vm.interactionHash = cleanInteractionHash(interactionHash);
    };

    vm.getGadgetInteractions = function(gadgetId){
      return vm.interactionHash[gadgetId] ;
    }

function cleanInteractionHash(interactionHash){
  
  for(var key in interactionHash) {
   
    interactionHash[key] = interactionHash[key].filter(function(f){
      if(typeof f.targetList === 'undefined') {
        return false;
      } else{
        return f.targetList.length > 0;
      }    
      
    });
    if(interactionHash[key].length===0){
      delete interactionHash[key];
    }
}
return interactionHash;
}

    vm.getInteractionHash = function(){      
      return vm.interactionHash;
    };

    vm.registerGadget = function (gadgetId) {
      
      if(!(gadgetId in vm.interactionHash)){
        vm.interactionHash[gadgetId] = [];
      }
    };

    vm.unregisterGadget = function (gadgetId) {
      
      //Delete from sources list
      delete vm.interactionHash[gadgetId];
      //Delete from destination list
      for (var keyGadget in vm.interactionHash) {
        var destinationList = vm.interactionHash[keyGadget];
        for (var keyDest in destinationList) {
          var destinationFieldBundle = destinationList[keyDest];
          var found = -1; //-1 not found other remove that position in targetList array
          for (var keyGDest in destinationFieldBundle.targetList) {
            var destination = destinationFieldBundle.targetList[keyGDest];
            if (destination.gadgetId == gadgetId) {
              found = keyGDest;
              break;
            }
          }
          //delete targetList entry if diferent -1
          if (found != -1) {
            destinationBundle.targetList.splice(found, 1);
          }
        }
      }
    };

    vm.registerGadgetFieldEmitter = function (gadgetId, fieldEmitter) {
      
      if(!(gadgetId in vm.interactionHash)){
        vm.interactionHash[gadgetId] = [];
      }
      if(vm.interactionHash[gadgetId].filter(function(f){
        if(typeof f.emiterField === 'undefined') {
          return false;
        } else{
          return f.emiterField === fieldEmitter
        }    
        
      }).length===0){
        vm.interactionHash[gadgetId].push(
          {
            targetList: [],
            emiterField: fieldEmitter
            
          }
        )
      }
    };

    vm.unregisterGadgetFieldEmitter = function (gadgetId, fieldEmitter) {
      
      var indexEmitter;
      vm.interactionHash[gadgetId].map(function (elem, index) {
        if (elem.fieldEmitter === fieldEmitter) {
          indexEmitter = index;
        }
      })
      vm.interactionHash[gadgetId].splice(found, 1);
    };

    vm.registerGadgetInteractionDestination = function (sourceGadgetId, targetGadgetId, originField, destinationField,dsField,filterChaining, idFilter) {
      //Auto generated
      
      if(!(sourceGadgetId in vm.interactionHash) || (vm.interactionHash[sourceGadgetId].filter(function(f){
        if(typeof f.emiterField === 'undefined') {
          return false;
        } else{
          return f.emiterField === originField
        }
      }).length===0)){
        vm.registerGadgetFieldEmitter(sourceGadgetId, originField);
      }
      var destinationFieldBundle = vm.interactionHash[sourceGadgetId].filter(
        function (elem) {
          return elem.emiterField == originField;
        }
      );
      destinationFieldBundle[0].filterChaining = filterChaining;
      destinationFieldBundle[0].targetList.push({
        gadgetId: targetGadgetId,
        idFilter:idFilter,
        dsField:dsField,
        overwriteField: destinationField
      })
    };



    vm.registerGadgetFilter = function (sourceGadgetId, filter){
      
      if(typeof sourceGadgetId != "undefined" && typeof filter !="undefined"){    
         
            for (var indexTarget = 0; indexTarget < filter.targetList.length; indexTarget++) {
              //if it is of the livefilter type we create three records in the hash
              if(filter.type === "livefilter"){
                vm.registerGadgetInteractionDestination(sourceGadgetId, filter.targetList[indexTarget].gadgetId,
                  filter.id+'realtime', filter.targetList[indexTarget].overwriteField,filter.targetList[indexTarget].field, filter.filterChaining, filter.id+'realtime');
                vm.registerGadgetInteractionDestination(sourceGadgetId, filter.targetList[indexTarget].gadgetId,
                    filter.id+'startDate', filter.targetList[indexTarget].overwriteField,filter.targetList[indexTarget].field, filter.filterChaining, filter.id+'startDate');
                vm.registerGadgetInteractionDestination(sourceGadgetId, filter.targetList[indexTarget].gadgetId,
                      filter.id+'endDate', filter.targetList[indexTarget].overwriteField, filter.targetList[indexTarget].field, filter.filterChaining, filter.id+'endDate');
              
                }else  if(filter.type === "intervaldatefilter" || filter.type === "intervaldatestringfilter"){                          
                vm.registerGadgetInteractionDestination(sourceGadgetId, filter.targetList[indexTarget].gadgetId,
                    filter.id+'startDate', filter.targetList[indexTarget].overwriteField,filter.targetList[indexTarget].field, filter.filterChaining, filter.id+'startDate');
                vm.registerGadgetInteractionDestination(sourceGadgetId, filter.targetList[indexTarget].gadgetId,
                      filter.id+'endDate', filter.targetList[indexTarget].overwriteField, filter.targetList[indexTarget].field, filter.filterChaining, filter.id+'endDate');
              }
              else{
                vm.registerGadgetInteractionDestination(sourceGadgetId, filter.targetList[indexTarget].gadgetId,
                  filter.id, filter.targetList[indexTarget].overwriteField,filter.targetList[indexTarget].field, filter.filterChaining, filter.id);
              }
          }
            
      }
    }

  //When delete filter from gadget
  vm.unregisterGadgetFilter = function (sourceGadgetId){    
    var interactionHash = vm.interactionHash[sourceGadgetId];    
    if(typeof interactionHash!='undefined' &&  interactionHash.length>0){
      for (var i = 0; i < interactionHash.length; i++) {
        var interaction = interactionHash[i];
          var resultTargetList = [];
          for (var indexTarget = 0; indexTarget < interaction.targetList.length; indexTarget++) {
            if( typeof interaction.targetList[indexTarget].idFilter === "undefined"){
              resultTargetList.push(interaction.targetList[indexTarget]);
            }
          }
          //If exist general filters 
          if(resultTargetList.length>0){
            interaction.targetList = resultTargetList.slice(0);
          }else{    
            delete vm.interactionHash[sourceGadgetId][i];          
          }
    }
      vm.interactionHash[sourceGadgetId] = vm.interactionHash[sourceGadgetId].filter(function (el) { 
      return el != null; 
     }); 
    if(vm.interactionHash[sourceGadgetId].length==0){
      delete vm.interactionHash[sourceGadgetId];
    }
  }
  }

    vm.registerGadgetFilters = function (sourceGadgetId, config){
      
      if(typeof sourceGadgetId != "undefined" && typeof config !="undefined" && config!=null && config.length>0){
        for (var index = 0; index < config.length; index++) {
          var filter = config[index];
          vm.registerGadgetFilter(sourceGadgetId,filter);
        }
      }
    }

  

    vm.getInteractionHashWithoutGadgetFilters = function(){ 
      
      var tempInteractionHash=angular.copy(vm.interactionHash);
      for (var sourceGadgetId in tempInteractionHash) {
        cleanInteractionHashGadgetFilters(sourceGadgetId,tempInteractionHash)
      }      
      return tempInteractionHash;
    };


    function cleanInteractionHashGadgetFilters(sourceGadgetId,tempInteractionHash){ 
           
      //filter interactions without idFilter
      var interactions = tempInteractionHash[sourceGadgetId];
      for (var i = 0; i < interactions.length; i++) {        
        var interaction = interactions[i];
        if(typeof interaction.targetList !== "undefined"){
          interaction.targetList = interaction.targetList.filter(function(f){
            return typeof f.idFilter === 'undefined';
          })
        }
      }
      //clean interactions empty
      interactions = interactions.filter(function(f){
                return (typeof f.targetList !== 'undefined' &&  f.targetList.length>0);       
      });
    }
    



    vm.unregisterGadgetInteractionDestination = function (sourceGadgetId, targetGadgetId, originField, destinationField,filterChaining) {
      
      var destinationFieldBundle = vm.interactionHash[sourceGadgetId].filter(
        function (elem) {
          return elem.emiterField == originField;
        }
      );
      var found = -1;
      destinationFieldBundle[0].targetList.map(
        function (dest, index) {
          if (dest.overwriteField == destinationField && dest.gadgetId == targetGadgetId) {
            found = index;
          }
        }
      );
      if (found != -1) {
        destinationFieldBundle[0].targetList.splice(found, 1);
      }
    };


    vm.getInteractionHashForTargetGadget=function(targetGadgetId){      
      var interactions = vm.getInteractionHashWithoutGadgetFilters();
      if (Object.keys(interactions).length > 0) {
        for (var keyGadget in interactions) {
          var destinationList = interactions[keyGadget];
          for (var keyGDest in destinationList) {
            var destination = destinationList[keyGDest];
            destination.targetList = destination.targetList.filter(function(targ){
             return targ.gadgetId == targetGadgetId;
            }) ;             
          }
          //clean if targetList is empty 
          interactions[keyGadget] = destinationList.filter(function(dest){
            return dest.targetList.length>0;
           }) ;             
        }
        //clean empty interactions property
        for (var keyGadget in interactions) {            
          if(interactions[keyGadget]==null || interactions[keyGadget].length==0){
          delete interactions[keyGadget];
          }
        }
      }
      return interactions;
   }




    //SourceFilterData: {"field1":{"value":" ","op":" ","typeAction":""}}},"field2":"value2","field3":"value3"}
    vm.sendBroadcastFilter = function (gadgetId, sourceFilterData) {             
      
      var destinationList = vm.interactionHash[gadgetId];
      var filterSourceFilterData = [];
      var listFilters = [];
      var listActions = [];
      var listValues = [];
      try {        
        if(typeof destinationList[0].filterChaining != "undefined" &&
            destinationList[0].filterChaining){
           for (var keySource in sourceFilterData){
            if(sourceFilterData[keySource].id === gadgetId){
              filterSourceFilterData[keySource] = sourceFilterData[keySource];
            }
          }
        }else{
          filterSourceFilterData=sourceFilterData;
        }
      } catch (error) {
        filterSourceFilterData=sourceFilterData; 
      }      
      for (var keyDest in destinationList) {
        var destinationFieldBundle = destinationList[keyDest];
        //Target list is not empty and field came from triggered gadget data
        if (destinationFieldBundle.targetList.length > 0 && destinationFieldBundle.emiterField in filterSourceFilterData) {
          for (var keyGDest in destinationFieldBundle.targetList) {
            var destination = destinationFieldBundle.targetList[keyGDest];         
            if(typeof filterSourceFilterData[destinationFieldBundle.emiterField].typeAction == "undefined"){
              buildFilterEvent(destination,  filterSourceFilterData[destinationFieldBundle.emiterField], gadgetId,listFilters);            
            }else  if(filterSourceFilterData[destinationFieldBundle.emiterField].typeAction == "filter"){
              buildFilterEvent(destination,  filterSourceFilterData[destinationFieldBundle.emiterField], gadgetId,listFilters);           
            }else  if(filterSourceFilterData[destinationFieldBundle.emiterField].typeAction == "value"){             
              buildValueEvent(destination,  filterSourceFilterData[destinationFieldBundle.emiterField], gadgetId,listValues);
            }else  if(filterSourceFilterData[destinationFieldBundle.emiterField].typeAction == "action"){
              buildActionEvent(destination,  filterSourceFilterData[destinationFieldBundle.emiterField], gadgetId,listActions);             
            }           
          }
        }
      } 
      //Send filters joined for destiny gadget
        for (var idGadgetDest in listFilters) {
          emitToTargets(idGadgetDest, listFilters[idGadgetDest]);
        }
        for (var idGadgetDest in listActions) {
          emitToTargets(idGadgetDest, listActions[idGadgetDest]);
        }
        for (var idGadgetDest in listValues) {
          emitToTargets(idGadgetDest, listValues[idGadgetDest]);
        }
    };

    function buildFilterEvent(destination, sourceFilterData, gadgetEmitterId,listFilters) {
            
      var sourceFilterDataAux = angular.copy(sourceFilterData);
      if(typeof listFilters[destination.gadgetId] ==="undefined"){
        listFilters[destination.gadgetId]={ "type": "filter",  "id": gadgetEmitterId,  "data": []};
      }    
        if(typeof sourceFilterDataAux.typeAction === 'undefined' || 
          sourceFilterDataAux.typeAction === 'filter'){
            var op ="=";
            if(typeof sourceFilterDataAux.op!="undefined" && sourceFilterDataAux.op.length>0 ){
              op = sourceFilterDataAux.op;
            }
            var idFilter = destination.overwriteField;
            if(typeof destination.idFilter !== "undefined"){
              idFilter = destination.idFilter;
            }

            var field = destination.overwriteField;
            if(typeof destination.dsField !== "undefined"){
              field = destination.dsField;
            }
            var name = destination.overwriteField;
            if(typeof sourceFilterDataAux.name!="undefined" ){
              name = sourceFilterDataAux.name;
            }
            var result = [];
            if(listFilters[destination.gadgetId].data.length>0){
               result = listFilters[destination.gadgetId].data.find(function(data){
                if(data.field === field && 
                  data.value === sourceFilterDataAux.value && 
                  data.op === op &&
                  data.idFilter === idFilter &&
                  data.name === name){
                    return true;
                  }else{
                    return false;
                  }
              })
             
            }
            if( typeof result == 'undefined' || result.length === 0 ){
            listFilters[destination.gadgetId].data.push({
              "field": field, 
              "value": sourceFilterDataAux.value,
              "op":op,
              "idFilter":idFilter,
              "name":name
            })
          }
        }
    }

    function buildActionEvent(destination,  sourceFilterData, gadgetEmitterId,listActions) {
      
      var sourceFilterDataAux = angular.copy(sourceFilterData);
      //we add first de filter event by the parent filter and then we add the chaining filter with the same filterId in order to propagate filters
      if(typeof listActions[destination.gadgetId] ==="undefined"){
        listActions[destination.gadgetId]={ "type": "action",  "id": gadgetEmitterId,  "data": []};
      }
      
        if(typeof sourceFilterDataAux.typeAction === 'undefined' || 
        sourceFilterDataAux.typeAction === 'action'){
          listActions[destination.gadgetId].data.push({         
            "value": sourceFilterDataAux.value
          })
      }
      
     
    }

function buildValueEvent(destination,  sourceFilterData, gadgetEmitterId,listValues) {
      
      var sourceFilterDataAux = angular.copy(sourceFilterData);
      //we add first de filter event by the parent filter and then we add the chaining filter with the same filterId in order to propagate filters
      if(typeof listValues[destination.gadgetId] ==="undefined"){
        listValues[destination.gadgetId]={ "type": "value",  "id": gadgetEmitterId,  "data":{}};
      }      
        if(typeof sourceFilterDataAux.typeAction === 'undefined' || 
        sourceFilterDataAux.typeAction === 'value'){
          listValues[destination.gadgetId].data = {"topic":destination.overwriteField,"value":sourceFilterDataAux.value};
      }
      
     
    }


    //Gadget interaction hash table, {gadgetsource:{emiterField:"field1", targetList: [{gadgetId,overwriteField}]}}
    vm.interactionHash

    vm.generateInitialDatalinkFiltersForGadgetId = function (gadgetId){
      var filterList=[];

      if (__env.initialDatalink) {
        for (var idParameter in __env.initialDatalink) { //objects in initial datalink origin parameters
          if(vm.interactionHash.hasOwnProperty(idParameter)) { //is object in interactionhash
            for (var parameter in __env.initialDatalink[idParameter]) { //fields in object of initial datalink
              for (var dtForParameterIndex in vm.interactionHash[idParameter]) { //datalinks for parameters
                var dtForParameter = vm.interactionHash[idParameter][dtForParameterIndex];
                if (dtForParameter.emiterField == __env.initialDatalink[idParameter][parameter].field) { //if emiter field is the field in initial object
                  for (var indexTargetList in dtForParameter.targetList) { 
                    var targetElem = dtForParameter.targetList[indexTargetList];
                    if (targetElem.gadgetId === gadgetId) { //if gadgetId (fn param) is in the target list of datalinks
                      //ADD to filter list
                      filterList.push({
                        origin: idParameter,
                        data: [
                          {
                            value: __env.initialDatalink[idParameter][parameter].value,
                            op: __env.initialDatalink[idParameter][parameter].op,
                            field: targetElem.overwriteField, 
                            idFilter: targetElem.overwriteField,
                            name: targetElem.overwriteField
                          }]
                      });
                    }
                  }
                }
              }
              
            }
          }
        }
      }
      return filterList;
    }

    vm.generateFiltersForGadgetIdWithDatastatus = function(gadgetid, addDatastatusFn, filters) {
      var initialDatalinks = this.generateInitialDatalinkFiltersForGadgetId(gadgetid);
      if (initialDatalinks.length > 0) { //{origin:{"{"id":"origin","data":[{"field":"countrydest","value":"American Samoa","op":"=","idFilter":"countrydest","name":"countrydest"}]}"}}
        for (var index in initialDatalinks) {
          var initialDatalink = initialDatalinks[index];
          var dataEvent = {
            type: "filter",
            id: initialDatalink.origin,
            data: initialDatalink.data
          }
          for(var index in dataEvent.data){
            addDatastatusFn(dataEvent,index);
          }
          //filters with id for changing in external filter. Diference sendAndSubscribe (not working id, data) and updatedatasourceAndtrigger (with id)
          var fi = datasourceSolverService.buildFilterStt(dataEvent).filter.data;
          filters = filters.concat({"id":dataEvent.id,"data":fi});
        }
      }
      return filters;
    }

    function emitToTargets(id, data) {
      $rootScope.$broadcast(id, data);
    }

    vm.emitForClean = function (id,data){
        $rootScope.$broadcast(id, data);
    }

    function copyObject (src) {
      return Object.assign({}, src);
    }      


  };
})();

(function () {
  'use strict';

  HttpService.$inject = ["$http", "$log", "__env", "$rootScope", "localStorageService"];
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

(function () {
  'use strict';

  angular.module('dashboardFramework')
    .service('gadgetManagerService', GadgetManagerService);

  /** @ngInject */
  function GadgetManagerService() {
      var vm = this;
      vm.dashboardModel = {};
      vm.selectedpage = 0;
      vm.gadgetFullScreen = null;
      vm.loadedList = {}

      vm.setDashboardModelAndPage = function(dashboard,selectedpage,gadgetFullScreen){
        vm.dashboardModel = dashboard;
        vm.selectedpage = selectedpage;
        vm.gadgetFullScreen = gadgetFullScreen;

        initLoadGadgetMonitor();
      }

      function initLoadGadgetMonitor(){
        //loaded list
        if(vm.gadgetFullScreen){
          vm.loadedList[vm.gadgetFullScreen] = 1;
        }
        else{
          var pagegadgets = vm.returnGadgets();
          for(var ig in pagegadgets){
            if(pagegadgets[ig].type !== 'gadgetfilter'){
              if(pagegadgets[ig].id in vm.loadedList){
                vm.loadedList[pagegadgets[ig].id]++;
              }
              else{
                vm.loadedList[pagegadgets[ig].id] = 1;
              }
            }
          }
        }

        //receive gadget finish
        window.addEventListener('gadgetloaded', function (e) {
          var gid = e.detail;
          vm.loadedList[gid]--;
          if(checkAllLoaded()){
            window.postMessage("dashboardloaded", "*");
            if(window.self !== window.top){
              window.parent.postMessage("dashboardloaded", "*");
            }
          }
          
        }, false);
      }

      function checkAllLoaded(){
        for(var g in vm.loadedList){
          if(vm.loadedList[g] != 0){
            return false;
          }
        }
        return true;
      }

      vm.findGadgetById = function(gadgetId,page){
        var page = vm.dashboardModel.pages[page || vm.selectedpage];
        for(var layerIndex in page.layers){
          var layer = page.layers[layerIndex];
          var gadgets = layer.gridboard.filter(function(gadget){return gadget.id === gadgetId});
          if(gadgets.length){
            return gadgets[0];
          }
        }
        return null;
      }

      vm.findGadgetByIdAllPages = function(gadgetId){
        for(var pageindex in vm.dashboardModel.pages){
          var g = vm.findGadgetById(gadgetId,pageindex);
          if(g != null){
            return g;
          }
        }
        return null;
      }

      vm.returnGadgets = function(page){
        var gadgets = [];     
        var page = vm.dashboardModel.pages[page || vm.selectedpage];
        for(var layerIndex in page.layers){
          var layer = page.layers[layerIndex];
          var gadgetsAux = layer.gridboard.filter(function(gadget){return typeof gadget.id != "undefined"});
          if(gadgetsAux.length){
            gadgets = gadgets.concat(gadgetsAux);
          }
        }
        return gadgets;
      }
      
  }
})();

(function () {
  'use strict';

  FilterService.$inject = ["$log", "__env", "$rootScope", "$timeout", "interactionService", "$q"];
  angular.module('dashboardFramework')
    .service('filterService', FilterService);

  /** @ngInject */
  function FilterService($log, __env, $rootScope,$timeout, interactionService,$q) {

    var vm = this;

    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":"",value:""}]


    vm.sendFilters = function (id, tempConfig) {
      var config = JSON.parse(JSON.stringify(tempConfig));
      //send to broadcastfilter all filters with op and typeAction:{action, value, data}
      var filterStt = {};
      for (var index = 0; index < config.length; index++) {
        if (config[index].typeAction === "action") {
          //value can contain the values start, stop or refresh 
          filterStt[config[index].id] = {
            value: config[index].value,
            typeAction: config[index].typeAction
          };
        } else if (config[index].typeAction === "filter" || config[index].typeAction === "value") {
          //type lifefilter special behavior
          if (config[index].type === "livefilter") {

            //send start/stop timeinterval
            filterStt[config[index].id + 'realtime'] = {
              value: config[index].data.realtime,
              typeAction: "action"
            };
            //send dates
            filterStt[config[index].id + 'startDate'] = {
              value: config[index].data.startDate,
              op: '>=',
              typeAction: "filter",
              name: "startDate",
              initialFilter: config[index].initialFilter
            };
            filterStt[config[index].id + 'endDate'] = {
              value: config[index].data.endDate,
              op: '<=',
              typeAction: "filter",
              name: "endDate",
              initialFilter: config[index].initialFilter
            };
          } else if (config[index].type === "multiselectfilter" 
            || config[index].type === "multiselectdsfilter") {
          
            if(typeof config[index].data.optionsSelected !== 'undefined' 
              && config[index].data.optionsSelected.length>0 ){
                filterStt[config[index].id] = {
                  value: "(" +createIn(config[index].data.optionsSelected,true) +")",
                  op: 'IN',
                  name: config[index].name,
                  typeAction: "filter",
                  initialFilter: config[index].initialFilter
                };
          }else{
            filterStt[config[index].id] = {
              value: null,
              op: 'IN',
              name: config[index].name,
              typeAction: "filter",
              initialFilter: config[index].initialFilter
            };
          }
          }else if (config[index].type === "multiselectnumberfilter"
            || config[index].type === "multiselectnumberdsfilter") {
            if(typeof config[index].data.optionsSelected !== 'undefined' 
              && config[index].data.optionsSelected.length>0 ){
                filterStt[config[index].id] = {
                  value: "(" +createIn(config[index].data.optionsSelected,false) +")",
                  op: 'IN',
                  name: config[index].name,
                  typeAction: "filter",
                  initialFilter: config[index].initialFilter
                };
             }else{
              filterStt[config[index].id] = {
                value: null,
                op: 'IN',
                name: config[index].name,
                typeAction: "filter",
                initialFilter: config[index].initialFilter
              };
             }
           }
           else if (config[index].type === "simpleselectfilter" || 
              config[index].type === "simpleselectdsfilter") {
                if(typeof config[index].data.optionsSelected !== 'undefined'
                  && config[index].data.optionsSelected!=null 
                  && config[index].data.optionsSelected.length>0      ){
                    var quote = config[index].data.optionsSelected.split("'").length -1;
                    var quotes = config[index].data.optionsSelected.split('"').length -1;
                    var value=  config[index].data.optionsSelected;
                    if( quote!==2 && quotes!==2  ){
                      value="'"+value+"'";
                    }
                    filterStt[config[index].id] = {
                      value:value,
                      op:  config[index].op,
                      name: config[index].name,
                      typeAction: "filter",
                      initialFilter: config[index].initialFilter
                    };
                }else{
                  filterStt[config[index].id] = {
                    value:null,
                    op:  config[index].op,
                    name: config[index].name,
                    typeAction: "filter",
                    initialFilter: config[index].initialFilter
                  };
                }
           } else if (config[index].type === "simpleselectnumberfilter" || 
              config[index].type === "simpleselectnumberdsfilter") {
                if(typeof config[index].data.optionsSelected !== 'undefined'
                  && config[index].data.optionsSelected!=null 
                    ){              
                    filterStt[config[index].id] = {
                      value: Number(config[index].data.optionsSelected),
                      op: config[index].op,
                      name: config[index].name,
                      typeAction: "filter",
                      initialFilter: config[index].initialFilter
                    };
                }else{
                  filterStt[config[index].id] = {
                    value: null,
                    op: config[index].op,
                    name: config[index].name,
                    typeAction: "filter",
                    initialFilter: config[index].initialFilter
                  };
                }
           }else  if (config[index].type === "intervaldatefilter") {            
            //send dates
            var starDate = config[index].data.startDate;
            var endDate = config[index].data.endDate;
            if(typeof starDate !== 'undefined' && starDate!=null ){
              starDate= "TIMESTAMP('"+starDate+"')";
              filterStt[config[index].id + 'startDate'] = {
                  value: starDate,
                  op: '>=',
                  typeAction: "filter",
                  name: "startDate",
                  initialFilter: config[index].initialFilter
                };
           }
           if(typeof endDate !== 'undefined' && endDate!=null ){
            endDate= "TIMESTAMP('"+endDate+"')";
            filterStt[config[index].id + 'endDate'] = {
              value: endDate,
              op: '<=',
              typeAction: "filter",
              name: "endDate",
              initialFilter: config[index].initialFilter
            };
          }
          } else  if (config[index].type === "intervaldatestringfilter") {                      
            //send dates
            var starDate = config[index].data.startDate;
            var endDate = config[index].data.endDate;
            if(typeof starDate !== 'undefined' && starDate!=null ){
              starDate= "'"+starDate+"'";
              filterStt[config[index].id + 'startDate'] = {
                  value: starDate,
                  op: '>=',
                  typeAction: "filter",
                  name: "startDate",
                  initialFilter: config[index].initialFilter
                };
           }
           if(typeof endDate !== 'undefined' && endDate!=null ){
            endDate= "'"+endDate+"'";
            filterStt[config[index].id + 'endDate'] = {
              value: endDate,
              op: '<=',
              typeAction: "filter",
              name: "endDate",
              initialFilter: config[index].initialFilter
            };
          }
          } else if (config[index].type === "textfilter" ){
            
              if( config[index].value != null && typeof config[index].value != "undefined" && (config[index].value + "").length > 0) {          
                  var quote = config[index].value.split("'").length -1;
                  var quotes = config[index].value.split('"').length -1;
                  var value= config[index].value
                  if( quote!==2 && quotes!==2  ){
                    value="'"+value+"'";
                  }
                filterStt[config[index].id] = {
                  value: value,
                  op: config[index].op,
                  name: config[index].name,
                  typeAction: config[index].typeAction,
                  initialFilter: config[index].initialFilter
                };
              }else{
                filterStt[config[index].id] = {
                  value: null,
                  op: config[index].op,
                  name: config[index].name,
                  typeAction: config[index].typeAction,
                  initialFilter: config[index].initialFilter
                };
              }
          }  
          else if (config[index].type === "numberfilter" && typeof config[index] != "undefined") {
            if (  config[index].value != null && typeof config[index].value != "undefined" && (config[index].value + "").length > 0) {
             filterStt[config[index].id] = {
              value: Number(config[index].value),
              op: config[index].op,
              name: config[index].name,
              typeAction: config[index].typeAction,
              initialFilter: config[index].initialFilter
            };
          }else{
            filterStt[config[index].id] = {
              value: null,
              op: config[index].op,
              name: config[index].name,
              typeAction: config[index].typeAction,
              initialFilter: config[index].initialFilter
            };
          }
          }
        }
      }
      interactionService.sendBroadcastFilter(id, filterStt);
    }


    vm.getInitialFilters = function (id,tempConfig) {
        var defered = $q.defer();
        var promise = defered.promise;
      if(typeof tempConfig !== "undefined" && tempConfig != null){
        var config = JSON.parse(JSON.stringify(tempConfig));

        var filterStt = {};
        for (var index = 0; index < config.length; index++) {
          if (config[index].initialFilter) {
        
            if (config[index].typeAction === "filter" || config[index].typeAction === "value") {
              //type lifefilter special behavior
              if (config[index].type === "livefilter") {
                //send dates
                filterStt[config[index].id + 'startDate'] = {
                  value: config[index].data.startDate,
                  op: '>=',
                  name: "startDate",
                  typeAction: "filter",
                  initialFilter: config[index].initialFilter
                };
                filterStt[config[index].id + 'endDate'] = {
                  value: config[index].data.endDate,
                  op: '<=',
                  name: "endDate",
                  typeAction: "filter",
                  initialFilter: config[index].initialFilter
                };
              } else if (config[index].type === "multiselectfilter" 
                || config[index].type === "multiselectdsfilter") {
                if(typeof config[index].data.optionsSelected !== 'undefined' 
                  && config[index].data.optionsSelected.length>0 ){
                    filterStt[config[index].id] = {
                      value: "(" +createIn(config[index].data.optionsSelected,true) +")",
                      op: 'IN',
                      name: config[index].name,
                      typeAction: "filter",
                      initialFilter: config[index].initialFilter
                    };
                }else{
                  filterStt[config[index].id] = {
                    value: null,
                    op: 'IN',
                    name: config[index].name,
                    typeAction: "filter",
                    initialFilter: config[index].initialFilter
                  };
                }
              } else if (config[index].type === "multiselectnumberfilter" 
               || config[index].type === "multiselectnumberdsfilter") {
                if(typeof config[index].data.optionsSelected !== 'undefined' 
                && config[index].data.optionsSelected.length>0 ){
                filterStt[config[index].id] = {
                  value: "(" +createIn(config[index].data.optionsSelected,false) +")",
                  op: 'IN',
                  name: config[index].name,
                  typeAction: "filter",
                  initialFilter: config[index].initialFilter
                };
                }else{
                  filterStt[config[index].id] = {
                    value: null,
                    op: 'IN',
                    name: config[index].name,
                    typeAction: "filter",
                    initialFilter: config[index].initialFilter
                  };
                }
              } else if (config[index].type === "simpleselectfilter" || config[index].type === "simpleselectdsfilter") {
                if(typeof config[index].data.optionsSelected !== 'undefined'
                   && config[index].data.optionsSelected!=null   
                   && config[index].data.optionsSelected.length>0    ){
                    var quote = config[index].data.optionsSelected.split("'").length -1;
                    var quotes = config[index].data.optionsSelected.split('"').length -1;
                    var value= config[index].data.optionsSelected;
                    if( quote!==2 && quotes!==2  ){
                      value="'"+value+"'";
                    }
                    filterStt[config[index].id] = {
                      value:value,
                      op:  config[index].op,
                      name: config[index].name,
                      typeAction: "filter",
                      initialFilter: config[index].initialFilter
                    };
                 }else{
                  filterStt[config[index].id] = {
                    value:null,
                    op:  config[index].op,
                    name: config[index].name,
                    typeAction: "filter",
                    initialFilter: config[index].initialFilter
                  };
                 }
               } else if (config[index].type === "simpleselectnumberfilter" || config[index].type === "simpleselectnumberdsfilter") {
                if(typeof config[index].data.optionsSelected !== 'undefined'
                   && config[index].data.optionsSelected!=null
                   && config[index].data.optionsSelected.length>0  ){              
                    filterStt[config[index].id] = {
                      value: Number(config[index].data.optionsSelected),
                      op:  config[index].op,
                      name: config[index].name,
                      typeAction: "filter",
                      initialFilter: config[index].initialFilter
                    };
                 }else{
                  filterStt[config[index].id] = {
                    value: null,
                    op:  config[index].op,
                    name: config[index].name,
                    typeAction: "filter",
                    initialFilter: config[index].initialFilter
                  };
                 }
               } 
               else if (config[index].type === "intervaldatefilter") {                        
                        //send dates
                    var starDate = config[index].data.startDate;
                    var endDate = config[index].data.endDate;
                    if(typeof starDate !== 'undefined' && starDate!=null ){
                      starDate= "TIMESTAMP('"+starDate+"')";
                      filterStt[config[index].id + 'startDate'] = {
                          value: starDate,
                          op: '>=',
                          typeAction: "filter",
                          name: "startDate",
                          initialFilter: config[index].initialFilter
                        };
                  }
                  if(typeof endDate !== 'undefined' && endDate!=null ){
                    endDate= "TIMESTAMP('"+endDate+"')";
                    filterStt[config[index].id + 'endDate'] = {
                      value: endDate,
                      op: '<=',
                      typeAction: "filter",
                      name: "endDate",
                      initialFilter: config[index].initialFilter
                    };
                  }  
                }else if (config[index].type === "intervaldatestringfilter") {                                             
                      //send dates
                  var starDate = config[index].data.startDate;
                  var endDate = config[index].data.endDate;
                  if(typeof starDate !== 'undefined' && starDate!=null ){
                    starDate= "'"+starDate+"'";
                    filterStt[config[index].id + 'startDate'] = {
                        value: starDate,
                        op: '>=',
                        typeAction: "filter",
                        name: "startDate",
                        initialFilter: config[index].initialFilter
                      };
                }
                if(typeof endDate !== 'undefined' && endDate!=null ){
                  endDate= "'"+endDate+"'";
                  filterStt[config[index].id + 'endDate'] = {
                    value: endDate,
                    op: '<=',
                    typeAction: "filter",
                    name: "endDate",
                    initialFilter: config[index].initialFilter
                  };
                }  
                }else if (config[index].type === "textfilter" ) {
                if(config[index].value != null && typeof config[index].value != "undefined" && (config[index].value + "").length > 0) {
                var quote = config[index].value.split("'").length -1;
                  var quotes = config[index].value.split('"').length -1;
                  var value = config[index].value ;
                  if(quote!==2 && quotes!==2){
                    value  ="'"+value+"'";
                  }                
                  filterStt[config[index].id] = {
                    value: value,
                    op: config[index].op,
                    name: config[index].name,
                    typeAction: config[index].typeAction,
                    initialFilter: config[index].initialFilter
                  };
                }else{
                  filterStt[config[index].id] = {
                    value: null,
                    op: config[index].op,
                    name: config[index].name,
                    typeAction: config[index].typeAction,
                    initialFilter: config[index].initialFilter
                  };
                }
              }else if (config[index].type === "numberfilter" && typeof config[index] != "undefined") {
               if (  config[index].value != null && typeof config[index].value != "undefined" && (config[index].value + "").length > 0) {
                filterStt[config[index].id] = {
                  value: Number(config[index].value),
                  op: config[index].op,
                  name: config[index].name,
                  typeAction: config[index].typeAction,
                  initialFilter: config[index].initialFilter
                };
              }else{
                filterStt[config[index].id] = {
                  value: null,
                  op: config[index].op,
                  name: config[index].name,
                  typeAction: config[index].typeAction,
                  initialFilter: config[index].initialFilter
                };
              }
            }
          }
          }
        }
      interactionService.sendBroadcastFilter(id, filterStt);
      defered.resolve(filterStt);
      }else{
        defered.resolve(); 
      }
      
      

      return promise;

    }


    vm.cleanAllFilters = function (id,tempConfig) {
      if(typeof tempConfig!=='undefined'){
          var config = JSON.parse(JSON.stringify(tempConfig));
          //First clean gadget filters     
          for (var index = 0; index < config.length; index++) {
            for (var i = 0; i < config[index].targetList.length; i++) {
              if (config[index].typeAction === "filter" || config[index].typeAction === "value") {
                //type lifefilter special behavior
                if (config[index].type === "livefilter") {
                //DO NOTHING 
                } else if (typeof config[index] != "undefined") {
                
                  sendclean(config[index].targetList[i].gadgetId,id,config[index].targetList[i].field);
            
                }
              }
            }
          }

          //second clean filters on gadgets targets 
          var interactions = interactionService.getGadgetInteractions(id);
          for (var index = 0; index < interactions.length; index++) {
            for (var i = 0; i < interactions[index].targetList.length; i++) {
              if(interactions[index].targetList[i].gadgetId!==id){
                sendclean(interactions[index].targetList[i].gadgetId,id,interactions[index].targetList[i].overwriteField);
              }
            }
          }

      }
    }
    function sendclean(id,gadgetId,field){
      $timeout(function() { interactionService.emitForClean(id,{id: gadgetId,type:'filter',data:[],field:field})},100);
    }
    function createIn(optionsSelected,quotes){
      var signals =[];
      for(var index = 0; index < optionsSelected.length; index++){
        if(quotes){
          signals.push("'"+optionsSelected[index]+"'");
        }else{
          signals.push(optionsSelected[index]);
        }
      }
     return signals.join(",");
    }
    

  };
})();
(function () {
  'use strict';

  FavoriteGadgetService.$inject = ["httpService", "$log", "__env", "$rootScope", "$timeout", "$q"];
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
(function () {
  'use strict';

  DatasourceSolverService.$inject = ["socketService", "httpService", "$mdDialog", "$interval", "$rootScope", "urlParamService", "$q", "utilsService", "$timeout"];
  angular.module('dashboardFramework')
    .service('datasourceSolverService', DatasourceSolverService);

  /** @ngInject */
  function DatasourceSolverService(socketService, httpService, $mdDialog, $interval, $rootScope, urlParamService, $q, utilsService,$timeout) {
    var vm = this;
    vm.gadgetToDatasource = {};

    vm.pendingDatasources = {};
    vm.poolingDatasources = {};
    vm.streamingDatasources = {};

    vm.heartbeatTimeoutObj;

    vm.arrayintervals=[];
    //Adding dashboard for security comprobations
    vm.dashboard = $rootScope.dashboard ? $rootScope.dashboard : "";

    vm.addListenerForHeartbeat = function(){
      socketService.addListenerForHeartbeat(
        function(){
          vm.reactivateHeartbeatTimeout();
        }
      )
    }

    vm.reactivateHeartbeatTimeout = function (){
      if(vm.heartbeatTimeoutObj){
        $timeout.cancel(vm.heartbeatTimeoutObj);
      }
      vm.heartbeatTimeoutObj = $timeout(
        function(){
          console.log("Error timeout heartbeat while reconnecting")
          reconnect();
        },
        __env.globalSockMaxWaitTimeout || 50000
      );
    }

    function reconnect(){
      if(socketService.isConnected()){
        console.log("Closing connection after " + (__env.globalSockMaxWaitTimeout || 5000) + " ms");
        vm.disconnect().then(
          function(){
            console.log("Opening new connection after " + (__env.globalSockMaxWaitTimeout || 5000) + " ms");
            initConnection();
        });
      }
      else{
        initConnection();
      }
    }

    function initConnection(){
      httpService.setDashboardEngineCredentials();
      socketService.connect(vm.reactivateHeartbeatTimeout,vm.addListenerForHeartbeat);
    }

    initConnection();

    //datasource {"name":"name","type":"query","refresh":"refresh",triggers:[{params:{where:[],project:[],filter:[]},emiter:""}]}


    function connectRegisterSingleDatasourceAndFirstShot(datasource) {

      if (datasource.type == "query") {//Query datasource. We don't need RT conection only request-response
        if (datasource.refresh == 0) {//One shot datasource, we don't need to save it, only execute it once

          for (var i = 0; i < datasource.triggers.length; i++) {
            socketService.connectAndSendAndSubscribe([{ "msg": fromTriggerToMessage(datasource.triggers[i], datasource.name), id: datasource.triggers[i].emitTo, callback: vm.emitToTargets }]);
          }
        }
        else {//Interval query datasource, we need to register this datasource in order to pooling results
          vm.poolingDatasources[datasource.name] = datasource;
          var intervalId = $interval(/*Datasource passed as parameter in order to call every refresh time*/
            function (datasource) {
              for (var i = 0; i < datasource.triggers.length; i++) {
                if (typeof datasource.triggers[i].isActivated === "undefined" || datasource.triggers[i].isActivated) {
                  socketService.connectAndSendAndSubscribe([{ "msg": fromTriggerToMessage(datasource.triggers[i], datasource.name), id: datasource.triggers[i].emitTo, callback: vm.emitToTargets }]);
                }
              }
            }, datasource.refresh * 1000, 0, true, datasource
          );
          vm.poolingDatasources[datasource.name].intervalId = intervalId;
        }
      }
      else {//Streaming datasource

      }
    }

    //Method from gadget to drill up and down the datasource
    vm.drillDown = function (gadgetId) { }
    vm.drillUp = function (gadgetId) { }

    vm.updateDatasourceTriggerAndShot = function (gadgetID, updateInfo,intents ) {

      var accessInfo = vm.gadgetToDatasource[gadgetID];
      if (typeof accessInfo !== 'undefined') {
        
        var dsSolver = vm.poolingDatasources[accessInfo.ds].triggers[accessInfo.index];
        if (updateInfo != null && updateInfo.constructor === Array) {
          for (var index in updateInfo) {
            updateQueryParams(dsSolver, updateInfo[index]);
          }
        } else {
          updateQueryParams(dsSolver, updateInfo);
        }
        var solverCopy = angular.copy(dsSolver);
        solverCopy.params.filter = urlParamService.generateFiltersForGadgetId(gadgetID);
        for (var index in dsSolver.params.filter) {
          var bundleFilters = dsSolver.params.filter[index].data;
          for (var indexB in bundleFilters) {
            solverCopy.params.filter.push(bundleFilters[indexB]);
          }
        }
        socketService.sendAndSubscribe({ "msg": fromTriggerToMessage(solverCopy, accessInfo.ds), id: angular.copy(gadgetID), type: "filter", callback: vm.emitToTargets });
      }else{
        if(typeof intents ==='undefined'){
          intents = 10;
        }
        if(intents > 0){
          $timeout(function() {vm.updateDatasourceTriggerAndShot(gadgetID, updateInfo,intents-1)}, 100); 
        }
      }

    }

    vm.updateDatasourceTriggerAndRefresh = function (gadgetID, updateInfo) {
      var accessInfo = vm.gadgetToDatasource[gadgetID];
      var dsSolver = vm.poolingDatasources[accessInfo.ds].triggers[accessInfo.index];
      if (updateInfo != null && updateInfo.constructor === Array) {
        for (var index in updateInfo) {
          updateQueryParams(dsSolver, updateInfo[index]);
        }
      } else {
        updateQueryParams(dsSolver, updateInfo);
      }
      var solverCopy = angular.copy(dsSolver);
      solverCopy.params.filter = urlParamService.generateFiltersForGadgetId(gadgetID);
      for (var index in dsSolver.params.filter) {
        var bundleFilters = dsSolver.params.filter[index].data;
        for (var indexB in bundleFilters) {
          solverCopy.params.filter.push(bundleFilters[indexB]);
        }
      }
      socketService.sendAndSubscribe({ "msg": fromTriggerToMessage(solverCopy, accessInfo.ds), id: angular.copy(gadgetID), type: "refresh", callback: vm.emitToTargets });
    }

    vm.startRefreshIntervalData = function (gadgetID) {
      try {      
        var accessInfo = vm.gadgetToDatasource[gadgetID];
        var dsSolver = vm.poolingDatasources[accessInfo.ds].triggers[accessInfo.index];
        dsSolver.isActivated = true;
        var solverCopy = angular.copy(dsSolver);
        solverCopy.params.filter = urlParamService.generateFiltersForGadgetId(gadgetID);
        for (var index in dsSolver.params.filter) {
          var bundleFilters = dsSolver.params.filter[index].data;
          for (var indexB in bundleFilters) {
            solverCopy.params.filter.push(bundleFilters[indexB]);
          }
        }
        socketService.sendAndSubscribe({ "msg": fromTriggerToMessage(solverCopy, accessInfo.ds), id: angular.copy(gadgetID), type: "refresh", callback: vm.emitToTargets });
    } catch (error) {
        
    }
    }

    vm.stopRefreshIntervalData = function (gadgetID) {
      try {
        var accessInfo = vm.gadgetToDatasource[gadgetID];
        var dsSolver = vm.poolingDatasources[accessInfo.ds].triggers[accessInfo.index];
        dsSolver.isActivated = false;
    } catch (error) {        
    }
    }

    vm.refreshIntervalData = function (gadgetID) {
      try {
        var accessInfo = vm.gadgetToDatasource[gadgetID];
        var dsSolver = vm.poolingDatasources[accessInfo.ds].triggers[accessInfo.index];
        var solverCopy = angular.copy(dsSolver);
        solverCopy.params.filter = urlParamService.generateFiltersForGadgetId(gadgetID);
        for (var index in dsSolver.params.filter) {
          var bundleFilters = dsSolver.params.filter[index].data;
          for (var indexB in bundleFilters) {
            solverCopy.params.filter.push(bundleFilters[indexB]);
          }
        }
        socketService.sendAndSubscribe({ "msg": fromTriggerToMessage(solverCopy, accessInfo.ds), id: angular.copy(gadgetID), type: "refresh", callback: vm.emitToTargets });
    } catch (error) {        
    }
    }


    //update info has the filter, group, project id to allow override filters from same gadget and combining with others
    function updateQueryParams(trigger, updateInfo) {
      var index = 0;//index filter
      var overwriteFilter = trigger.params.filter.filter(function (sfilter, i) {
        if (sfilter.id == updateInfo.filter.id) {
          index = i;
        }
        return sfilter.id == updateInfo.filter.id;
      });
      if (overwriteFilter.length > 0) {//filter founded, we need to override it
        if (updateInfo.filter.data.length == 0) {//with empty array we delete it, remove filter action
          trigger.params.filter.splice(index, 1);
        }
        else { //override filter, for example change filter data and no adding
          overwriteFilter[0].data = updateInfo.filter.data;
        }
      }
      else {
        trigger.params.filter.push(updateInfo.filter);
      }

      if (updateInfo.group) {//For group that only change in drill options, we need to override all elements
        trigger.params.group = updateInfo.group;
      }

      if (updateInfo.project) {//For project that only change in drill options, we need to override all elements
        trigger.params.project = updateInfo.project;
      }
    }

    vm.registerSingleDatasourceAndFirstShot = function (datasource, firstShot) {
      
      if (datasource.type == "query") {//Query datasource. We don't need RT conection only request-response
        if (!(datasource.name in vm.poolingDatasources)) {
          vm.poolingDatasources[datasource.name] = datasource;
          vm.poolingDatasources[datasource.name].triggers[0].listeners = 1;
          vm.gadgetToDatasource[datasource.triggers[0].emitTo] = { "ds": datasource.name, "index": 0 };
        }
        else if (!(datasource.triggers[0].emitTo in vm.gadgetToDatasource)) {
          vm.poolingDatasources[datasource.name].triggers.push(datasource.triggers[0]);
          var newposition = vm.poolingDatasources[datasource.name].triggers.length - 1
          vm.poolingDatasources[datasource.name].triggers[newposition].listeners = 1;
          vm.gadgetToDatasource[datasource.triggers[0].emitTo] = { "ds": datasource.name, "index": newposition };
        }
        else {
          var gpos = vm.gadgetToDatasource[datasource.triggers[0].emitTo];
          vm.poolingDatasources[datasource.name].triggers[gpos.index].listeners++;
        }
        //One shot datasource, for pooling and          
        if (firstShot != null && firstShot) {
          for (var i = 0; i < datasource.triggers.length; i++) {
            console.log("firstShot", datasource.triggers);
            socketService.sendAndSubscribe({ "msg": fromTriggerToMessage(datasource.triggers[i], datasource.name), id: angular.copy(datasource.triggers[i].emitTo), type: "refresh", callback: vm.emitToTargets });
          }
        }
        if (datasource.refresh != 0) {//Interval query datasource, we need to register this datasource in order to pooling results
          var i;


          if(typeof vm.poolingDatasources[datasource.name].intervalId!='undefined'){
            $interval.cancel(vm.poolingDatasources[datasource.name].intervalId);
          }
          vm.poolingDatasources[datasource.name].intervalId = $interval(/*Datasource passed as parameter in order to call every refresh time*/
            function (datasource) {
              for (var i = 0; i < vm.poolingDatasources[datasource.name].triggers.length; i++) {
 
                var solverCopy = angular.copy(vm.poolingDatasources[datasource.name].triggers[i]);
                solverCopy.params.filter = urlParamService.generateFiltersForGadgetId(vm.poolingDatasources[datasource.name].triggers[i].emitTo);
                for (var index in vm.poolingDatasources[datasource.name].triggers[i].params.filter) {
                  var bundleFilters = vm.poolingDatasources[datasource.name].triggers[i].params.filter[index].data;
                  for (var indexB in bundleFilters) {
                    solverCopy.params.filter.push(bundleFilters[indexB]);
                  }
                }
                if (typeof vm.poolingDatasources[datasource.name].triggers[i].isActivated === "undefined" || vm.poolingDatasources[datasource.name].triggers[i].isActivated) {
                  console.log("sendAndSubscribe", solverCopy);
                  socketService.sendAndSubscribe({ "msg": fromTriggerToMessage(solverCopy, datasource.name), id: angular.copy(vm.poolingDatasources[datasource.name].triggers[i].emitTo), type: "refresh", callback: vm.emitToTargets });
                }
              }
            }, datasource.refresh * 1000, 0, true, datasource
          );
          
          //vm.poolingDatasources[datasource.name].intervalId = intervalId;
        }
      }
      else {//Streaming datasource

      }
    }


    vm.getDataFromDataSource = function (datasource, callback) {
      socketService.sendAndSubscribe({ "msg": fromTriggerToMessage(datasource.triggers[0], datasource.name), id: angular.copy(datasource.triggers[0].emitTo), type: "refresh", callback: callback });
    } 
    vm.getDataFromDataSourceForFilter = function (datasource, callback) {
      socketService.sendAndSubscribe({ "msg": fromTriggerToMessage(datasource.triggers[0], datasource.name), id: generateUUID(), type: "refresh", callback: callback });
    } 

    function generateUUID(){
      return (new Date()).getTime() + Math.floor(((Math.random()*1000000)));     
    }

   

    vm.get = function (datasourcename, triggers) {
      var deferred = $q.defer();
      socketService.sendAndSubscribe({ 
        "msg": fromTriggerToMessage({"params":triggers?triggers:{}}, datasourcename), 
        id: 1, 
        type: "refresh", 
        callback: function(id,name,data){
          if(data.error){
            console.error("Error in response datasource: " + data.data);
            deferred.reject(data.data);
          }
          else{
            deferred.resolve(JSON.parse(data.data));

          }
        }
      });
      return deferred.promise;
    }

    vm.getOne = function (datasourcename) {
      return vm.get(datasourcename,{"limit":1});
    }

    vm.from = function(datasource){
      var datasourceCallBuilder = {
        datasource: datasource,
        params: {},
        filter: function(field_filters,value,op){
          var filterList;
          if(Array.isArray(field_filters)){
            filterList = field_filters.map(function(filter){filter.op = (filter.op?filter.op:"="); return filter});
          }
          else{
            filterList = [{"field":field_filters,"op":op?op:"=","exp":value}]
          }
          this.params.filter =  (this.params.filter || []).concat(filterList)
          return this;
        },
        skip: function(skip){
          this.params.offset = skip;
          return this;
        },
        limit: function(limit){
          this.params.limit = limit;
          return this;
        },
        group: function(group_groups){
          var groupList;
          if(Array.isArray(group_groups)){
            groupList = group_groups;
          }
          else{
            groupList = [group_groups]
          }
          this.params.group = (this.params.group || []).concat(groupList)
          return this;
        },
        project: function(field_projects,alias,op){
          var projectList;
          if(Array.isArray(field_projects)){
            projectList = field_projects;
          }
          else{
            var project = {"field":field_projects};
            if(alias){
              project.alias = alias
            }
            else{
              project.alias = field_projects
            }
            if(op){
              project.op = op
            }
            projectList = [project]
          }
          this.params.project = (this.params.project || []).concat(projectList)
          return this;
        },
        sort: function(field_sorts,asc){
          var sortList;
          if(Array.isArray(field_sorts)){
            sortList = field_sorts;
          }
          else{
            var sort = {"field":field_sorts};
            if(asc){
              sort.asc = asc;
            }
            else{
              sort.asc = true;
            }
            sortList = [sort]
          }
          this.params.sort = (this.params.sort || []).concat(sortList)
          return this;
        },
        param: function(field_params,value){
          var paramList;
          if(Array.isArray(field_params)){
            paramList = field_params;
          }
          else{
            var param = {"field":field_params,"param":param};
            paramList = [param]
          }
          this.params.param = (this.params.param || []).concat(paramList)
          return this;
        },
        debug: function(debug){
          if(typeof debug !== 'undefined'){
            this.params.debug = debug;
          }
          else{
            this.params.debug = true;
          }
          return this;
        },
        execute: function(){
          return vm.get(this.datasource,this.params);
        }
      }

      //aliases
      datasourceCallBuilder.where = datasourceCallBuilder.filter;
      datasourceCallBuilder.offset = datasourceCallBuilder.skip;
      datasourceCallBuilder.max = datasourceCallBuilder.limit;
      datasourceCallBuilder.select = datasourceCallBuilder.project;
      datasourceCallBuilder.exec = datasourceCallBuilder.execute;

      return datasourceCallBuilder;
    }

    vm.getFields = function(datasource){
      return vm.getOne(datasource).then(        
        function(data){
          var deferred = $q.defer();
          if(data.length){
            deferred.resolve(utilsService.sort_jsonarray(utilsService.getJsonFields(data[0],"",[]),"field"));
          }
          else{
            deferred.reject("No data found");
          }
          return deferred.promise;
        }
      );
    }

    function fromTriggerToMessage(trigger, dsname) {
      var baseMsg = trigger.params;
      baseMsg.ds = dsname;
      baseMsg.dashboard = vm.dashboard;
      return baseMsg;
    }

    

    vm.emitToTargets = function (id, name, data) {
      //pendingDatasources
     var parseData = [];
      try {
        parseData = JSON.parse(data.data);
      } catch (error) {
        parseData = [];
      }

      $rootScope.$broadcast(id,
        {
          type: "data",
          name: name,
          data: parseData,
          startTime: data.startTime
        }
      );
    }

    vm.registerDatasource = function (datasource) {
      vm.poolingDatasources[datasource.name] = datasource;
    }

    vm.registerDatasourceTrigger = function (datasource, trigger) {//add streaming too
      if (!(datasource.name in vm.poolingDatasources)) {
        vm.poolingDatasources[datasource.name] = datasource;
      }
      vm.poolingDatasources[name].triggers.push(trigger);
      //trigger one shot
    }

    vm.unregisterDatasourceTrigger = function (name, emiter) {
      
      if (name in vm.pendingDatasources && vm.pendingDatasources[name].triggers.length == 0) {
        vm.pendingDatasources[name].triggers = vm.pendingDatasources[name].triggers.filter(function (trigger) { return trigger.emitTo != emiter });

        if (vm.pendingDatasources[name].triggers.length == 0) {
          delete vm.pendingDatasources[name];
        }
      }
      if (name in vm.poolingDatasources ) {
        var trigger = vm.poolingDatasources[name].triggers.filter(function (trigger) { return trigger.emitTo == emiter });
        trigger[0].listeners--;
        delete vm.gadgetToDatasource[emiter];

        if (trigger[0].listeners == 0 || isNaN(trigger[0].listeners) ) {
          vm.poolingDatasources[name].triggers = vm.poolingDatasources[name].triggers.filter(function (trigger) { return trigger.emitTo != emiter });
        }

        if (vm.poolingDatasources[name].triggers.length == 0) {
          $interval.cancel(vm.poolingDatasources[name].intervalId);
          delete vm.poolingDatasources[name];
          socketService.cleanqueue();
        }
      }
      if (name in vm.streamingDatasources && vm.streamingDatasources[name].triggers.length == 0) {
        vm.streamingDatasources[name].triggers = vm.streamingDatasources[name].triggers.filter(function (trigger) { return trigger.emitTo != emiter });

        if (vm.streamingDatasources[name].triggers.length == 0) {
          delete vm.streamingDatasources[name];
        }
      }
    }

    vm.disconnect = function () {
      return socketService.disconnect();
    }



    //Create filter 
    vm.buildFilterStt = function (dataEvent) {
      return {
        filter: {
          id: dataEvent.id,
          data: dataEvent.data.map(
            function (f) {
              //quotes for string identification
              if (typeof f.value === "string") {
                // var re = /^([\+-]?\d{4}(?!\d{2}\b))((-?)((0[1-9]|1[0-2])(\3([12]\d|0[1-9]|3[01]))?|W([0-4]\d|5[0-2])(-?[1-7])?|(00[1-9]|0[1-9]\d|[12]\d{2}|3([0-5]\d|6[1-6])))([T\s]((([01]\d|2[0-3])((:?)[0-5]\d)?|24\:?00)([\.,]\d+(?!:))?)?(\17[0-5]\d([\.,]\d+)?)?([zZ]|([\+-])([01]\d|2[0-3]):?([0-5]\d)?)?)?)?$/;

                /*  if(f.value.length>4 && re.test(f.value)){
                    f.value = "\'" + f.value + "\'"
                  }  */
                if ((typeof f.op != 'undefined' && f.op != null && f.op.toUpperCase() !== "IN" && f.op.toUpperCase() !== "BETWEEN") && f.value.indexOf("'") < 0) {
                  f.value = "\'" + f.value + "\'"
                }
              }
              return {
                field: f.field,
                op: f.op,
                exp: f.value
              }
            }
          )
        },
        group: [],
        project: []
      }
    }
  }
})(); 

!function(e,i,n){"use strict";var t=function(){return"lfobjyxxxxxxxx".replace(/[xy]/g,function(e){var i=16*Math.random()|0,n="x"==e?i:3&i|8;return n.toString(16)})},l=function(e){var i=e.type,n=e.name;return o(i,n)?"image":r(i,n)?"video":s(i,n)?"audio":"object"},o=function(e,i){return!(!e.match("image.*")&&!i.match(/\.(gif|png|jpe?g)$/i))},r=function(e,i){return!(!e.match("video.*")&&!i.match(/\.(og?|mp4|webm|3gp)$/i))},s=function(e,i){return!(!e.match("audio.*")&&!i.match(/\.(ogg|mp3|wav)$/i))},a=function(i){var n={key:t(),lfFile:i,lfFileName:i.name,lfFileType:i.type,lfTagType:l(i),lfDataUrl:e.URL.createObjectURL(i),isRemote:!1};return n},f=function(e,i,n){var o={name:i,type:n},r={key:t(),lfFile:void 0,lfFileName:i,lfFileType:n,lfTagType:l(o),lfDataUrl:e,isRemote:!0};return r},c=i.module("lfNgMdFileInput",["ngMaterial"]);c.directive("lfFile",function(){return{restrict:"E",scope:{lfFileObj:"=",lfUnknowClass:"="},link:function(e,i,n){var t=e.lfFileObj.lfDataUrl,l=e.lfFileObj.lfFileType,o=e.lfFileObj.lfTagType,r=e.lfUnknowClass;switch(o){case"image":i.replaceWith('<img src="'+t+'" />');break;case"video":i.replaceWith('<video controls><source src="'+t+'""></video>');break;case"audio":i.replaceWith('<audio controls><source src="'+t+'""></audio>');break;default:void 0==e.lfFileObj.lfFile&&(l="unknown/unknown"),i.replaceWith('<object type="'+l+'" data="'+t+'"><div class="lf-ng-md-file-input-preview-default"><md-icon class="lf-ng-md-file-input-preview-icon '+r+'"></md-icon></div></object>')}}}}),c.run(["$templateCache",function(e){e.put("lfNgMdFileinput.html",['<div layout="column" class="lf-ng-md-file-input" ng-model="'+t()+'">','<div layout="column" class="lf-ng-md-file-input-preview-container" ng-class="{\'disabled\':isDisabled}" ng-show="isDrag || (isPreview && lfFiles.length)">','<md-button aria-label="remove all files" class="close lf-ng-md-file-input-x" ng-click="removeAllFiles($event)" ng-hide="!lfFiles.length || !isPreview" >&times;</md-button>','<div class="lf-ng-md-file-input-drag">','<div layout="row" layout-align="center center" class="lf-ng-md-file-input-drag-text-container" ng-show="(!lfFiles.length || !isPreview) && isDrag">','<div class="lf-ng-md-file-input-drag-text">{{strCaptionDragAndDrop}}</div>',"</div>",'<div class="lf-ng-md-file-input-thumbnails" ng-if="isPreview == true">','<div class="lf-ng-md-file-input-frame" ng-repeat="lffile in lfFiles" ng-click="onFileClick(lffile)">','<div class="lf-ng-md-file-input-x" aria-label="remove {{lffile.lFfileName}}" ng-click="removeFile(lffile,$event)">&times;</div>','<lf-file lf-file-obj="lffile" lf-unknow-class="strUnknowIconCls"/>','<div class="lf-ng-md-file-input-frame-footer">','<div class="lf-ng-md-file-input-frame-caption">{{lffile.lfFileName}}</div>',"</div>","</div>","</div>",'<div class="clearfix" style="clear:both"></div>',"</div>","</div>",'<div layout="row" class="lf-ng-md-file-input-container" >','<div class="lf-ng-md-file-input-caption" layout="row" layout-align="start center" flex ng-class="{\'disabled\':isDisabled}" >','<md-icon class="lf-icon" ng-class="strCaptionIconCls"></md-icon>','<div flex class="lf-ng-md-file-input-caption-text-default" ng-show="!lfFiles.length">',"{{strCaptionPlaceholder}}","</div>",'<div flex class="lf-ng-md-file-input-caption-text" ng-hide="!lfFiles.length">','<span ng-if="isCustomCaption">{{strCaption}}</span>','<span ng-if="!isCustomCaption">','{{ lfFiles.length == 1 ? lfFiles[0].lfFileName : lfFiles.length+" files selected" }}',"</span>","</div>",'<md-progress-linear md-mode="determinate" value="{{floatProgress}}" ng-show="intLoading && isProgress"></md-progress-linear>',"</div>",'<md-button aria-label="remove all files" ng-disabled="isDisabled" ng-click="removeAllFiles()" ng-hide="!lfFiles.length || intLoading" class="md-raised lf-ng-md-file-input-button lf-ng-md-file-input-button-remove" ng-class="strRemoveButtonCls">','<md-icon class="lf-icon" ng-class="strRemoveIconCls"></md-icon> ',"{{strCaptionRemove}}","</md-button>",'<md-button aria-label="submit" ng-disabled="isDisabled" ng-click="onSubmitClick()" class="md-raised md-warn lf-ng-md-file-input-button lf-ng-md-file-input-button-submit" ng-class="strSubmitButtonCls" ng-show="lfFiles.length && !intLoading && isSubmit">','<md-icon class="lf-icon" ng-class="strSubmitIconCls"></md-icon> ',"{{strCaptionSubmit}}","</md-button>",'<md-button aria-label="browse" ng-disabled="isDisabled" ng-click="openDialog($event, this)" class="md-raised lf-ng-md-file-input-button lf-ng-md-file-input-button-brower" ng-class="strBrowseButtonCls">','<md-icon class="lf-icon" ng-class="strBrowseIconCls"></md-icon> ',"{{strCaptionBrowse}}",'<input type="file" aria-label="{{strAriaLabel}}" accept="{{accept}}" ng-disabled="isDisabled" class="lf-ng-md-file-input-tag" />',"</md-button>","</div>","</div>"].join(""))}]),c.filter("lfTrusted",["$sce",function(e){return function(i){return e.trustAsResourceUrl(i)}}]),c.directive("lfRequired",function(){return{restrict:"A",require:"ngModel",link:function(e,i,n,t){t&&(t.$validators.required=function(e,i){return e?e.length>0:!1})}}}),c.directive("lfMaxcount",function(){return{restrict:"A",require:"ngModel",link:function(e,i,n,t){if(t){var l=-1;n.$observe("lfMaxcount",function(e){var i=parseInt(e,10);l=isNaN(i)?-1:i,t.$validate()}),t.$validators.maxcount=function(e,i){return e?e.length<=l:!1}}}}}),c.directive("lfFilesize",function(){return{restrict:"A",require:"ngModel",link:function(e,i,n,t){if(t){var l=-1;n.$observe("lfFilesize",function(e){var i=/^[1-9][0-9]*(Byte|KB|MB)$/;if(i.test(e)){var n=["Byte","KB","MB"],o=e.match(i)[1],r=e.substring(0,e.indexOf(o));n.every(function(e,i){return o===e?(l=parseInt(r)*Math.pow(1024,i),!1):!0})}else l=-1;t.$validate()}),t.$validators.filesize=function(e,i){if(!e)return!1;var n=!0;return e.every(function(e,i){return e.lfFile.size>l?(n=!1,!1):!0}),n}}}}}),c.directive("lfTotalsize",function(){return{restrict:"A",require:"ngModel",link:function(e,n,t,l){if(l){var o=-1;t.$observe("lfTotalsize",function(e){var i=/^[1-9][0-9]*(Byte|KB|MB)$/;if(i.test(e)){var n=["Byte","KB","MB"],t=e.match(i)[1],r=e.substring(0,e.indexOf(t));n.every(function(e,i){return t===e?(o=parseInt(r)*Math.pow(1024,i),!1):!0})}else o=-1;l.$validate()}),l.$validators.totalsize=function(e,n){if(!e)return!1;var t=0;return i.forEach(e,function(e,i){t+=e.lfFile.size}),o>t}}}}}),c.directive("lfMimetype",function(){return{restrict:"A",require:"ngModel",link:function(e,i,t,l){if(l){var o;t.$observe("lfMimetype",function(e){var i=e.replace(/,/g,"|");o=new RegExp(i,"i"),l.$validate()}),l.$validators.mimetype=function(e,i){if(!e)return!1;var t=!0;return e.every(function(e,i){return e.lfFile!==n&&e.lfFile.type.match(o)?!0:(t=!1,!1)}),t}}}}}),c.directive("lfNgMdFileInput",["$q","$compile","$timeout",function(e,t,l){return{restrict:"E",templateUrl:"lfNgMdFileinput.html",replace:!0,require:"ngModel",scope:{lfFiles:"=?",lfApi:"=?",lfOption:"=?",lfCaption:"@?",lfPlaceholder:"@?",lfDragAndDropLabel:"@?",lfBrowseLabel:"@?",lfRemoveLabel:"@?",lfSubmitLabel:"@?",lfOnFileClick:"=?",lfOnSubmitClick:"=?",lfOnFileRemove:"=?",accept:"@?",ngDisabled:"=?",ngChange:"&?"},link:function(t,o,r,s){var c=i.element(o[0].querySelector(".lf-ng-md-file-input-tag")),u=i.element(o[0].querySelector(".lf-ng-md-file-input-drag")),d=i.element(o[0].querySelector(".lf-ng-md-file-input-thumbnails")),m=0;t.intLoading=0,t.floatProgress=0,t.isPreview=!1,t.isDrag=!1,t.isMutiple=!1,t.isProgress=!1,t.isCustomCaption=!1,t.isSubmit=!1,i.isDefined(r.preview)&&(t.isPreview=!0),i.isDefined(r.drag)&&(t.isDrag=!0),i.isDefined(r.multiple)?(c.attr("multiple","multiple"),t.isMutiple=!0):c.removeAttr("multiple"),i.isDefined(r.progress)&&(t.isProgress=!0),i.isDefined(r.submit)&&(t.isSubmit=!0),t.isDisabled=!1,i.isDefined(r.ngDisabled)&&t.$watch("ngDisabled",function(e){t.isDisabled=e}),t.strBrowseIconCls="lf-browse",t.strRemoveIconCls="lf-remove",t.strCaptionIconCls="lf-caption",t.strSubmitIconCls="lf-submit",t.strUnknowIconCls="lf-unknow",t.strBrowseButtonCls="md-primary",t.strRemoveButtonCls="",t.strSubmitButtonCls="md-accent",i.isDefined(r.lfOption)&&i.isObject(t.lfOption)&&(t.lfOption.hasOwnProperty("browseIconCls")&&(t.strBrowseIconCls=t.lfOption.browseIconCls),t.lfOption.hasOwnProperty("removeIconCls")&&(t.strRemoveIconCls=t.lfOption.removeIconCls),t.lfOption.hasOwnProperty("captionIconCls")&&(t.strCaptionIconCls=t.lfOption.captionIconCls),t.lfOption.hasOwnProperty("unknowIconCls")&&(t.strUnknowIconCls=t.lfOption.unknowIconCls),t.lfOption.hasOwnProperty("submitIconCls")&&(t.strSubmitIconCls=t.lfOption.submitIconCls),t.lfOption.hasOwnProperty("strBrowseButtonCls")&&(t.strBrowseButtonCls=t.lfOption.strBrowseButtonCls),t.lfOption.hasOwnProperty("strRemoveButtonCls")&&(t.strRemoveButtonCls=t.lfOption.strRemoveButtonCls),t.lfOption.hasOwnProperty("strSubmitButtonCls")&&(t.strSubmitButtonCls=t.lfOption.strSubmitButtonCls)),t.accept=t.accept||"",t.lfFiles=[],t[r.ngModel]=t.lfFiles,t.lfApi=new function(){var e=this;e.removeAll=function(){t.removeAllFiles()},e.removeByName=function(e){t.removeFileByName(e)},e.addRemoteFile=function(e,i,n){var l=f(e,i,n);t.lfFiles.push(l)}},t.strCaption="",t.strCaptionPlaceholder="Select file",t.strCaptionDragAndDrop="Drag & drop files here...",t.strCaptionBrowse="Browse",t.strCaptionRemove="Remove",t.strCaptionSubmit="Submit",t.strAriaLabel="",i.isDefined(r.ariaLabel)&&(t.strAriaLabel=r.ariaLabel),i.isDefined(r.lfPlaceholder)&&t.$watch("lfPlaceholder",function(e){t.strCaptionPlaceholder=e}),i.isDefined(r.lfCaption)&&(t.isCustomCaption=!0,t.$watch("lfCaption",function(e){t.strCaption=e})),t.lfDragAndDropLabel&&(t.strCaptionDragAndDrop=t.lfDragAndDropLabel),t.lfBrowseLabel&&(t.strCaptionBrowse=t.lfBrowseLabel),t.lfRemoveLabel&&(t.strCaptionRemove=t.lfRemoveLabel),t.lfSubmitLabel&&(t.strCaptionSubmit=t.lfSubmitLabel),t.openDialog=function(e,i){e&&l(function(){e.preventDefault(),e.stopPropagation();var i=e.target.children[2];i!==n&&c[0].click()},0)},t.removeAllFilesWithoutVaildate=function(){t.isDisabled||(t.lfFiles.length=0,d.empty())},t.removeAllFiles=function(e){t.removeAllFilesWithoutVaildate(),g()},t.removeFileByName=function(e,i){t.isDisabled||(t.lfFiles.every(function(i,n){return i.lfFileName==e?(t.lfFiles.splice(n,1),!1):!0}),g())},t.removeFile=function(e){t.lfFiles.every(function(n,l){return n.key==e.key?(i.isFunction(t.lfOnFileRemove)&&t.lfOnFileRemove(n,l),t.lfFiles.splice(l,1),!1):!0}),g()},t.onFileClick=function(e){i.isFunction(t.lfOnFileClick)&&t.lfFiles.every(function(i,n){return i.key==e.key?(t.lfOnFileClick(i,n),!1):!0})},t.onSubmitClick=function(){i.isFunction(t.lfOnSubmitClick)&&t.lfOnSubmitClick(t.lfFiles)},u.bind("dragover",function(e){e.stopPropagation(),e.preventDefault(),!t.isDisabled&&t.isDrag&&u.addClass("lf-ng-md-file-input-drag-hover")}),u.bind("dragleave",function(e){e.stopPropagation(),e.preventDefault(),!t.isDisabled&&t.isDrag&&u.removeClass("lf-ng-md-file-input-drag-hover")}),u.bind("drop",function(e){if(e.stopPropagation(),e.preventDefault(),!t.isDisabled&&t.isDrag){u.removeClass("lf-ng-md-file-input-drag-hover"),i.isObject(e.originalEvent)&&(e=e.originalEvent);var n=e.target.files||e.dataTransfer.files,l=t.accept.replace(/,/g,"|"),o=new RegExp(l,"i"),r=[];i.forEach(n,function(e,i){e.type.match(o)&&r.push(e)}),p(r)}}),c.bind("change",function(e){var i=e.files||e.target.files;p(i)});var p=function(e){if(!(e.length<=0)){t.lfFiles.map(function(e){return e.lfFileName});if(t.floatProgress=0,t.isMutiple){m=e.length,t.intLoading=m;for(var i=0;i<e.length;i++){var n=e[i];setTimeout(v(n),100*i)}}else{m=1,t.intLoading=m;for(var i=0;i<e.length;i++){var n=e[i];t.removeAllFilesWithoutVaildate(),v(n);break}}c.val("")}},g=function(){i.isFunction(t.ngChange)&&t.ngChange(),s.$validate()},v=function(e){b(e).then(function(i){var l=!1;if(t.lfFiles.every(function(i,t){var o=i.lfFile;return i.isRemote?!0:o.name!==n&&o.name==e.name?(o.size==e.size&&o.lastModified==e.lastModified&&(l=!0),!1):!0}),!l){var o=a(e);t.lfFiles.push(o)}0==t.intLoading&&g()},function(e){},function(e){})},b=function(i,n){var l=e.defer(),o=new FileReader;return o.onloadstart=function(){l.notify(0)},o.onload=function(e){},o.onloadend=function(e){l.resolve({index:n,result:o.result}),t.intLoading--,t.floatProgress=(m-t.intLoading)/m*100},o.onerror=function(e){l.reject(o.result),t.intLoading--,t.floatProgress=(m-t.intLoading)/m*100},o.onprogress=function(e){l.notify(e.loaded/e.total)},o.readAsArrayBuffer(i),l.promise}}}}])}(window,window.angular);
(function () {
    'use strict';

    RetryHttpProviderConfig.$inject = ["$httpProvider"];
    angular.module('dashboardFramework').config(RetryHttpProviderConfig);

    /** @ngInject */
    function RetryHttpProviderConfig($httpProvider) {  
        $httpProvider.interceptors.push(["$q", "$injector", function ($q, $injector) {
            var incrementalTimeout = 1000;
        
            function retryRequest (httpConfig) {
                var $timeout = $injector.get('$timeout');
                var thisTimeout = incrementalTimeout;
                incrementalTimeout *= 2;
                return $timeout(function() {
                    var $http = $injector.get('$http');
                    return $http(httpConfig);
                }, thisTimeout);
            };
        
            return {
                responseError: function (response) {
                    console.error("Error " + response.status + " in RestCall " + response.config.url + ", detail: " + JSON.stringify(response.data));
                    if (response.status === 500) {
                        if (incrementalTimeout < 5000) {
                            return retryRequest(response.config);
                        }
                        else {
                            console.error('The remote server seems to be busy at the moment. Please try again in later');
                        }
                    }
                    else {
                        incrementalTimeout = 1000;
                    }
                    return $q.reject(response);
                }
            };
        }]); 
    }

})();
(function () {
  'use strict';

  angular.module('dashboardFramework').directive('draggable', function() {
    return function(scope, element) {
      // this gives us the native JS object
      var el = element[0];

      el.draggable = true;

      el.addEventListener(
        'dragstart',
        function(e) {
          e.dataTransfer.effectAllowed = 'move';
          e.dataTransfer.setData('type', this.id);
          this.classList.add('drag');
          if($('synoptic')){
            $('synoptic').css("z-index", "0");
          }
          return false;
        },
        false
      );

      el.addEventListener(
        'dragend',
        function(e) {
          if($('synoptic')){
            $('synoptic').css("z-index", "1");
          }
          this.classList.remove('drag');
          return false;
        },
        false
      );
    }
  });
})();

var env = {};

// Import variables if present (from env.js)
if(window && window.__env){
  Object.assign(env, window.__env);
  angular.module('dashboardFramework').constant('__env', env);
}
else{//Default config
  console.info("__env properties not defined globally, manual definition is required")
}

(function () {
    'use strict';
    angular.module('dashboardFramework').config(['$translateProvider', configTranslate]);
    function configTranslate($translateProvider) {
        if(__env.i18njson && Object.keys(__env.i18njson).length > 0 && __env.i18njson.constructor === Object){
            var jsonlangs = __env.i18njson.languages;
            var langs = Object.keys(jsonlangs);
            for(var i=0; i<langs.length; i++){
                $translateProvider.translations(langs[i], jsonlangs[langs[i]]);
            }
            
            $translateProvider.preferredLanguage(__env.i18njson.default);
        } else { //Default translate when no language is defined
            $translateProvider.translations('EN', {});
        }
        
    };

})();
(function () {
  'use strict';

  config.$inject = ["$logProvider", "$compileProvider"];
  angular.module('dashboardFramework').config(config);

  /** @ngInject */
  function config($logProvider, $compileProvider) {
    // Disable debug
    $logProvider.debugEnabled(true);
    $compileProvider.debugInfoEnabled(true);

  }

})();

(function () {
  'use strict';

  MainController.$inject = ["$window", "$rootScope", "$scope", "$mdDialog", "$timeout", "httpService", "interactionService", "urlParamService", "gadgetManagerService", "filterService", "utilsService", "datasourceSolverService", "favoriteGadgetService", "$translate", "localStorageService", "__env", "cacheBoard"];
  angular.module('dashboardFramework')
    .component('dashboard', {
      templateUrl: 'app/dashboard.html',
      controller: MainController,
      controllerAs: 'vm',
      bindings:{
        editmode : "=",
        iframe : "=",
        wrapper: "=",
        selectedpage : "&",
        id: "@",
        public: "=",
        synop: "="
      }
    });

  /** @ngInject */
  function MainController($window, $rootScope, $scope,  $mdDialog, $timeout,  httpService, interactionService,urlParamService, gadgetManagerService,filterService,utilsService,datasourceSolverService,favoriteGadgetService, $translate, localStorageService, __env, cacheBoard) {
    var vm = this;
    $window.onbeforeunload = function(){
      console.log("exit dashboard");     
      datasourceSolverService.disconnect();
    };
    vm.$onInit = function () {
      
      dashboardInUseController.$inject = ["$scope", "$mdDialog"];
     $translate.use(utilsService.urlParamLang());
     vm.showSynopticEditor = false;
      if(vm.editmode){
        vm.showSynopticEditor=true;
        //show dialog dashboard is in use
        if(!vm.iframe && typeof __env.resourceinuse!=='undefined' && __env.resourceinuse){
          $mdDialog.show({
            controller: dashboardInUseController,
            templateUrl: 'app/partials/edit/dashboardInUseDialog.html',
            parent: angular.element(document.body),
            clickOutsideToClose:false,
            fullscreen: false, // Only for -xs, -sm breakpoints.
            openFrom: '.sidenav-fab',
            closeTo: angular.element(document.querySelector('.sidenav-fab')),
            locals: {                          
            }
          })
          .then(function(page) {
            $scope.status = 'Dialog pages closed'             
          }, function() {
            $scope.status = 'You cancelled the dialog.';
          }); 
        }
      }
      vm.selectedpage = 0;
      vm.synopticEdit = {
        zindexEditor:600,
        showEditor:vm.showSynopticEditor,
        showSynoptic: vm.synop
      }
      
     

      vm.initDash = function (dash){
        if(typeof dash !== 'undefined'){        
          vm.dashboard = dash;
        }            
        vm.dashboard.gridOptions.resizable.stop = sendResizeToGadget;
  
        vm.dashboard.gridOptions.enableEmptyCellDrop = true;
        if((!vm.iframe||
          (vm.iframe && typeof vm.dashboard.editButtonsIframe!='undefined' && vm.dashboard.editButtonsIframe.active)) || 
          vm.wrapper){ 
            
           vm.dashboard.gridOptions.emptyCellDropCallback = dropElementEvent;
           vm.dashboard.gridOptions.emptyCellDragCallback = dropElementEvent;
         
        } 
        //If interaction hash then recover connections
        if(vm.dashboard.interactionHash){
          interactionService.setInteractionHash(vm.dashboard.interactionHash);
        }
         //If interaction hash then recover connections
         if(vm.dashboard.parameterHash){
          urlParamService.seturlParamHash(vm.dashboard.parameterHash);
        }
        if(typeof vm.dashboard.gridOptions.displayGrid === "undefined" ||
                  vm.dashboard.gridOptions.displayGrid === null ){
          vm.dashboard.gridOptions.displayGrid = "onDrag&Resize";
        }
       
        if(!vm.editmode){           
          vm.dashboard.gridOptions.draggable.enabled = false;
          vm.dashboard.gridOptions.resizable.enabled = false;
          vm.dashboard.gridOptions.enableEmptyCellDrop = false;
          vm.dashboard.gridOptions.displayGrid = "none";
          vm.dashboard.gridOptions.enableEmptyCellDrag = false;
          var urlParamMandatory = urlParamService.checkParameterMandatory();
          if(urlParamMandatory.length>0){
            showUrlParamDialog(urlParamMandatory);
          }
        }

        //if $gadgetid is present in params we visualize this gadget in full screen
        if("$gadgetid" in __env.urlParameters){
          gadgetManagerService.setDashboardModelAndPage(vm.dashboard,vm.selectedpage,__env.urlParameters["$gadgetid"]);
          generateGadgetDataForView(__env.urlParameters["$gadgetid"]);
        }
        else{
          gadgetManagerService.setDashboardModelAndPage(vm.dashboard,vm.selectedpage);
        }
     }
  


      // pdf configuration
      var margins = {
        top: 70,
        bottom: 40,
        left: 30,
        width: 550
      };

      vm.margins = margins;
      
      $rootScope.dashboard = angular.copy(vm.id);

      function loadDashboard(model) {

        window.postMessage("start loading dashboard", "*");
        if(window.self !== window.top){
          window.parent.postMessage("start loading dashboard", "*");
        }
        console.log("start loading dashboard");
       

        if (__env.dashboardEngineBungleMode) {
          Object.assign(cacheBoard, model);
          model = JSON.parse(model.model);
        }
        localStorageService.isAfterSavedDate(vm.id, model.updatedAt).then(function (isAfterSavedDate) {
          if (!vm.iframe && vm.editmode && __env.codeLocalStorage === null && !isAfterSavedDate) {
            localStorageController.$inject = ["$scope", "$mdDialog"];
            vm.initDash(model);
            $mdDialog.show({
              controller: localStorageController,
              templateUrl: 'app/partials/edit/initLocalStorageDialog.html',
              parent: angular.element(document.body),
              clickOutsideToClose: false,
              fullscreen: false, // Only for -xs, -sm breakpoints.
              openFrom: '.sidenav-fab',
              closeTo: angular.element(document.querySelector('.sidenav-fab')),
              locals: {
              }
            })
              .then(function (page) {
                $scope.status = 'Dialog pages closed'
              }, function () {
                $scope.status = 'You cancelled the dialog.';
              });

            function localStorageController($scope, $mdDialog) {
              $scope.cancel = function () {
                $mdDialog.cancel();
              };

              $scope.hide = function () {
                $mdDialog.hide();
              };
              $scope.ok = function () {
                localStorageService.getLastItemDate(vm.id).then(function (identification) {
                  $window.location.href = __env.endpointControlPanel + '/dashboards/editfull/' + vm.id + '?__hist_dash=' + identification;
                  $mdDialog.hide();
                })


              };
            }
          } else if (!vm.iframe && vm.editmode && __env.codeLocalStorage !== null) {
            localStorageService.getItemByIdAndDate(vm.id, __env.codeLocalStorage).then(function (modByIdAndDate) {
              vm.initDash(modByIdAndDate);
            })

          } else {
            vm.initDash(model);
          }
        })
      }

      if(!utilsService.isEmptyJson(cacheBoard) && cacheBoard.id == vm.id){
        loadDashboard(cacheBoard);
      }
      else{
        /*Rest api call to get dashboard data*/
        httpService.getDashboardModel(vm.id).then(
          function(data){loadDashboard(data.data);}
        ).catch(
          function(error){      
            if(__env.dashboardEngineOauthtoken != null){
              document.getElementsByTagName("dashboard")[0].innerHTML = "<div style='padding:15px;background:#fbecec'><div class='no-data-title'>Dashboard Engine Error " + (error.status?error.status:"") + "</div><div class='no-data-text'>" + (error.config?"Rest Call: " + error.config.url + ". ":"") + "Detail: " + (error.data?JSON.stringify(error.data):error) + "</div></div>";
              window.dispatchEvent(new CustomEvent('errordashboardengine', { detail: {
                "type": "failLoadDashboard",
                "errorCode": (error.status?error.status:"")
              } }));
            } 
            else{
              $window.location.href = "/controlpanel/500";
            }   
          }
        )
      }

      function generateGadgetDataForView(gadgetid){//we  search by gadgetid
        vm.gadgetFullScreen = gadgetManagerService.findGadgetByIdAllPages(gadgetid);
        
        if(!vm.gadgetFullScreen){
          console.error("Gadget ID: " + gadgetid + ", not found in dashboard. Loading complete dashboard");
        }
      }

      function addGadgetHtml5(type,config,layergrid){
        addGadgetGeneric(type,config,layergrid);
      } 
      function addGadgetFilter(type,config,layergrid){
        addGadgetGeneric(type,config,layergrid);
      } 


      function addGadgetGeneric(type,config,layergrid){
        config.type = type;
        layergrid.push(config);
        $timeout(
         function(){
           $scope.$broadcast("$resize", "");
         },100
       );
       } 

       function dashboardInUseController($scope, $mdDialog) {                    
        $scope.cancel = function() { 
          $mdDialog.cancel();              
        };
          
        $scope.hide = function() { 
          $mdDialog.hide();
        };
        $scope.ok = function(){ 
          $window.location.href=__env.endpointControlPanel+'/dashboards/list/'; 
          $mdDialog.hide();
        };
      }

      
      vm.api={};
      //External API
      vm.api.createGadget = function(type,id,name,template,datasource,filters,customMenuOptions,setupLayout) {
            if(typeof template !== "undefined" && template !== null && typeof template !=="string"  ){
              //Gadgetcreate from template
              var newElem = {x: 0, y: 0, cols: 6, rows: 6,};
              //newElem.minItemRows = 10;
              //newElem.minItemCols = 10;            
              newElem.content=template.template;        
              newElem.contentcode = template.templateJS;
              newElem.id = id;             
              newElem.type = 'livehtml';
              newElem.idtemplate =  template.identification;
              newElem.header = {
                enable: true,
                title: {
                  iconColor: "hsl( 206, 54%, 5%)",
                  text: name, 
                  textColor: "hsl( 206, 54%, 5%)"
                },
                backgroundColor: "hsl(0, 0%, 100%)",
                height: 40
              } 
              newElem.backgroundColor ="white";
              newElem.padding = 0;
              newElem.border = {
                color: "hsl(0, 0%, 90%)",
                width: 0,
                radius: 5
              }  
              newElem.datasource = datasource;
              newElem.filters = filters;  
              newElem.filtersInModal=setupLayout.filtersInModal;
              newElem.customMenuOptions = customMenuOptions;
              newElem.hideBadges=setupLayout.hideBadges;
              newElem.hidebuttonclear=setupLayout.hidebuttonclear;
              addGadgetGeneric(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);        

            }else{
              var newElem = {x: 0, y: 0, cols: 6, rows: 6,};
              //newElem.minItemRows = 10;
              //newElem.minItemCols = 10;
              var type = type;
              newElem.id = id;
              newElem.content = type;
              newElem.type = type;
              newElem.idtemplate = type;
              newElem.header = {
                enable: true,
                title: {
                  iconColor: "hsl( 206, 54%, 5%)",
                  text: name, 
                  textColor: "hsl( 206, 54%, 5%)"
                },
                backgroundColor: "hsl(0, 0%, 100%)",
                height: 40
              }
              newElem.backgroundColor ="white";
              newElem.padding = 0;
              newElem.border = {
                color: "hsl(0, 0%, 90%)",
                width: 0,
                radius: 5
              }    
              newElem.filters = filters;    
              newElem.filtersInModal = setupLayout.filtersInModal;
              newElem.hideBadges = setupLayout.hideBadges;
              newElem.hidebuttonclear = setupLayout.hidebuttonclear;      
              addGadgetGeneric(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);        

            }
         
      };

      vm.api.updateFilterGadget = function(id,filters,customMenuOptions,merge) {
        $scope.$apply(function() {    
        var gadgets = vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard;
        for(var i=0;i<gadgets.length;i++){
          var gadget = gadgets[i];       	
          if(typeof gadget.id!=="undefined" && gadget.id === id){
            if(typeof gadget.filters!=="undefined" && gadget.filters!==null ){
              for(var j=0;j<gadget.filters.length;j++){
                var filter = gadget.filters[j];         
                for(var k=0;k<filters.length;k++){
                  if(filter.id === filters[k].id){
                      if(filters[k].type === 'multiselectfilter' || filters[k].type === 'multiselectnumberfilter'                       
                      ){
                        if(merge){
                          filter.data.options = Array.from(new Set(filter.data.options.concat(filters[k].data.options)));
                          filter.data.optionsDescription = Array.from(new Set(filter.data.optionsDescription.concat(filters[k].data.optionsDescription)));
                          filter.data.optionsSelected = Array.from(new Set(filter.data.optionsSelected.concat(filters[k].data.optionsSelected)));
                        }else{
                          filter.data.options = filters[k].data.options;
                          filter.data.optionsDescription = filters[k].data.optionsDescription;
                          filter.data.optionsSelected = filters[k].data.optionsSelected;
                        }
                      } 
                     else if(filters[k].type === 'multiselectdsfilter' || filters[k].type === 'multiselectnumberdsfilter'||
                     filters[k].type === 'simpleselectdsfilter' || filters[k].type === 'simpleselectnumberdsfilter'
                     ){                     
                       filter.data.ds = filters[k].data.ds;
                       filter.data.dsFieldValue = filters[k].data.dsFieldValue;
                       filter.data.dsFieldDes = filters[k].data.dsFieldDes;                    
                   } else if(filters[k].type === 'textfilter'){
                        filter.value = filters[k].value;
                      } else if(filters[k].type === 'numberfilter'){
                        filter.value = filters[k].value;
                      }
                       
                    }
                  }
                }		
              }
              if(merge){
                gadget.customMenuOptions = Array.from(new Set(gadget.customMenuOptions.concat(customMenuOptions)));               
              }else{
                gadget.customMenuOptions = customMenuOptions;
              }
              var idNoSpaces = id;
              idNoSpaces = idNoSpaces.replace(new RegExp(" ", "g"), "\\ ");
              angular.element(document.querySelector('#'+idNoSpaces)).controller("element").config = gadget.filters;
              if(gadget.filters !== undefined && gadget.filters!==null  ){
                filterService.sendFilters(id,gadget.filters);
              }
              angular.element(document.querySelector('#'+idNoSpaces)).controller("element").reloadFilters();
              break;
            }
          }    }) 
        
      }

      vm.api.dropOnElement = function(x,y) {
        if(x !=null && y !=null){
        vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard;
        var elements = document.getElementsByTagName("element");
        if(elements!=null && elements.length>0){
          for (var index = 0; index < elements.length; index++) {
            var element = elements[index];
            console.log(element.firstElementChild.getBoundingClientRect());
            var sl = element.firstElementChild.getBoundingClientRect().x;
            var sr = element.firstElementChild.getBoundingClientRect().x+ element.firstElementChild.getBoundingClientRect().width;
            var st = element.firstElementChild.getBoundingClientRect().y;
            var sb = element.firstElementChild.getBoundingClientRect().y + element.firstElementChild.getBoundingClientRect().height;
            if(x>=sl && x<=sr && y<= sb && y >= st){
              return {"dropOnElement":"TRUE","idGadget":element.id,"type":element.getAttribute('idtemplate')};
            }
          }
        }
      }
        return {"dropOnElement":"FALSE","idGadget":"","type":""};
      }

      vm.api.refreshGadgets = function(){
        var gadgets =document.querySelectorAll( 'gadget' ) ;
        if(gadgets.length>0){
         for (var index = 0; index < gadgets.length; index++) {
           var gad = gadgets[index];
           angular.element(gad).scope().$$childHead.reloadContent();
         }        
       }


      }

      //END External API

      function showAddGadgetDialog(type,config,layergrid){
        AddGadgetController.$inject = ["$scope", "__env", "$mdDialog", "httpService", "type", "config", "layergrid"];
        function AddGadgetController($scope,__env, $mdDialog, httpService, type, config, layergrid) {
          $scope.type = type;
          $scope.config = config;
          $scope.layergrid = layergrid;

          $scope.gadgets = [];
         

          $scope.hide = function() {
            $mdDialog.hide();
          };

          $scope.cancel = function() {
            $mdDialog.cancel();
          };


          $scope.loadGadgets = function() {
            return httpService.getUserGadgetsByType($scope.type).then(
              function(gadgets){
                $scope.gadgets = gadgets.data;
              }
            );
          };

          $scope.addGadget = function() {
            $scope.config.type = $scope.type;
            $scope.config.id = $scope.gadget.id;
            $scope.config.header.title.text = $scope.gadget.identification;
            $scope.layergrid.push($scope.config);
            $mdDialog.cancel();
          };

          $scope.alert;
          $scope.newGadget = function($event) {
           DialogController.$inject = ["$scope", "$mdDialog", "config", "layergrid", "type"];
            var parentEl = angular.element(document.body);
            $mdDialog.show({
              parent: parentEl,
              targetEvent: $event,
              fullscreen: false,
              template:
                '<md-dialog id="dialogCreateGadget"  aria-label="List dialog">' +
                '  <md-dialog-content >'+
                '<iframe id="iframeCreateGadget" style=" height: 80vh; width: 80vw;" frameborder="0" src="'+__env.endpointControlPanel+'/gadgets/createiframe/'+$scope.type+'"+></iframe>'+                     
                '  </md-dialog-content>' +             
                '</md-dialog>',
              locals: {
                config:  $scope.config, 
                layergrid: $scope.layergrid,
                type: $scope.type
              },
              controller: DialogController
           });
           function DialogController($scope, $mdDialog, config, layergrid, type) {
             $scope.config = config;
             $scope.layergrid = layergrid;
             $scope.closeDialog = function() {
               
               $mdDialog.hide();
             }

              $scope.addGadgetFromIframe = function(type,id,identification) {
              $scope.config.type = type;
              $scope.config.id = id;
              $scope.config.header.title.text = identification;
              $scope.layergrid.push($scope.config);
              $mdDialog.cancel();
            };
           }
                };
        }

        $mdDialog.show({
          controller: AddGadgetController,
          templateUrl: 'app/partials/edit/addGadgetDialog.html',
          parent: angular.element(document.body),
          clickOutsideToClose:true,
          fullscreen: false, // Only for -xs, -sm breakpoints.
          openFrom: '.sidenav-fab',
          closeTo: angular.element(document.querySelector('.sidenav-fab')),
          locals: {
            type: type,
            config: config,
            layergrid: layergrid
          }
        })
        .then(function() {

        }, function() {
          $scope.status = 'You cancelled the dialog.';
        });
      }

      function addFavoriteGadget(id,config,layergrid){

        favoriteGadgetService.getFavoriteGadgetByIdentification(id).then(
          function(favorite){                
            var data = JSON.parse(favorite.config);
            config.id =  (favorite.type + "_" + (new Date()).getTime());                
            config.type = favorite.type;
    
            config.header = data.header;
            config.backgroundColor =data.backgroundColor;
            config.padding = data.padding;
            config.border = data.border;
           
          
            //we differentiate by type
            if(config.type == 'livehtml'){
              config.subtype = data.subtype;
              config.content=data.content;
              config.contentcode=data.contentcode;
              config.datasource = data.datasource;
              if(favorite.gadgetTemplate){
                config.template = favorite.gadgetTemplate.identification; 
                config.params=data.params;
                layergrid.push(config);                
               }else{
                layergrid.push(config);               
               }
              }else  if(config.type == 'gadgetfilter'){
                newElem.content = data.content;
                newElem.contentcode = data.contentcode;
                addGadgetFilter(config.type,layergrid);              
              }
              else if(config.type == 'html5'){         
                addGadgetHtml5(config.type,layergrid);                
              }                  
              else{  
                //gadgets line,bars,...
                config.id = favorite.gadget.id; 
                layergrid.push(config);               
              }
              //we use config.id like target gadget id because can be changed when is created
              //add urlparams
              if (data.urlparams) {
                if (Object.keys(data.urlparams).length > 0) {
                  for (var keyGadget in data.urlparams) {
                    var destinationList = data.urlparams[keyGadget];
                    for (var keyGDest in destinationList) {
                      var destination = destinationList[keyGDest];
                      for (var targ in destination.targetList) {
                        var target = destination.targetList[targ];                          
                        urlParamService.registerParameter(keyGadget, destination.type, config.id, target.overwriteField, destination.mandatory);
                      }
                    }
                  }
                }                
              }
              //add datalinks
              if(data.datalinks){
                if (Object.keys(data.datalinks).length > 0) {
                  for (var keyGadget in data.datalinks) {
                    var sourceList = data.datalinks[keyGadget];
                    for (var keyGDest in sourceList) {
                      var destination = sourceList[keyGDest];
                      for (var targ in destination.targetList) {
                        var target = destination.targetList[targ];
                        interactionService.registerGadgetInteractionDestination(keyGadget, config.id, destination.emiterField, target.overwriteField,undefined,target.filterChaining,undefined);         
                      }
                    }
                  }
                }
              }


          });


      } 


      function showAddFavoriteGadgetDialog(config,layergrid){
        AddFavoriteGadgetController.$inject = ["$scope", "__env", "urlParamService", "interactionService", "favoriteGadgetService", "$mdDialog", "httpService", "config", "layergrid"];
        function AddFavoriteGadgetController($scope,__env, urlParamService,interactionService, favoriteGadgetService, $mdDialog, httpService, config, layergrid) {
          
          $scope.config = config;
          $scope.layergrid = layergrid;

          $scope.gadgets = [];

          $scope.hide = function() {
            $mdDialog.hide();
          };

          $scope.cancel = function() {
            $mdDialog.cancel();
          };


          $scope.loadGadgets = function() {
            return favoriteGadgetService.getAllIdentifications().then(
              function(gadgets){                
                $scope.gadgets = gadgets;
              }
            );
          };

          $scope.addGadget = function() {
            addFavoriteGadget($scope.gadget,$scope.config,$scope.layergrid);
            $mdDialog.hide();
          };

          $scope.alert;
        
        }

        $mdDialog.show({
          controller: AddFavoriteGadgetController,
          templateUrl: 'app/partials/edit/addFavoriteGadgetDropDialog.html',
          parent: angular.element(document.body),
          clickOutsideToClose:true,
          fullscreen: false, // Only for -xs, -sm breakpoints.
          openFrom: '.sidenav-fab',
          closeTo: angular.element(document.querySelector('.sidenav-fab')),
          locals: {            
            config: config,
            layergrid: layergrid
          }
        })
        .then(function() {

        }, function() {
          $scope.status = 'You cancelled the dialog.';
        });
      }



      function showAddGadgetTemplateDialog(type,config,layergrid){
        AddGadgetController.$inject = ["$scope", "__env", "$mdDialog", "httpService", "type", "config", "layergrid"];
        function AddGadgetController($scope,__env, $mdDialog, httpService, type, config, layergrid) {
          $scope.type = type;
          $scope.config = config;
          $scope.layergrid = layergrid;
          $scope.templatetype = 'angularJS'

          $scope.templates = [];

          $scope.hide = function() {
            $mdDialog.hide();
          };

          $scope.cancel = function() {
            $mdDialog.cancel();
          };

          var initCode = {
            "vueJS": {
              "html": "<!-- Write your HTML <div></div> and CSS <style></style> here -->\n<!--Focus here and F11 to full screen editor-->",
              "js": "//Write your Vue JSON controller code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\n\nvm.vueconfig = {\n\tel: document.querySelector('#' + vm.id + ' vuetemplate'),\n\tdata:{\n\t\tds:[]\n\t},\n\tmethods:{\n\t\tdrawVueComponent: function(newData,oldData){\n\t\t\t//This will be call on new data\n\t\t},\n\t\tresizeEvent: function(){\n\t\t\t//Resize event\n\t\t},\n\t\tdestroyVueComponent: function(){\n\t\t\tvm.vueapp.$destroy();\n\t\t},\n\t\treceiveValue: function(data){\n\t\t\t//data received from datalink\n\t\t},\n\t\tsendValue: vm.sendValue,\n\t\tsendFilter: vm.sendFilter\n\t}\n}\n\n//Init Vue app\nvm.vueapp = new Vue(vm.vueconfig);\n"
            },
            "vueJSODS": {
              "html": "<!-- Write your HTML <div></div> and CSS <style></style> here -->\n<!--Focus here and F11 to full screen editor-->",
              "js": "//Write your Vue with ODS JSON controller code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\n\nvm.vueconfig = {\n\tel: document.querySelector('#' + vm.id + ' vuetemplate'),\n\tdata:{\n\t\tds:[]\n\t},\n\tmethods:{\n\t\tdrawVueComponent: function(newData,oldData){\n\t\t\t//This will be call on new data\n\t\t},\n\t\tresizeEvent: function(){\n\t\t\t//Resize event\n\t\t},\n\t\tdestroyVueComponent: function(){\n\t\t\tvm.vueapp.$destroy();\n\t\t},\n\t\treceiveValue: function(data){\n\t\t\t//data received from datalink\n\t\t},\n\t\tsendValue: vm.sendValue,\n\t\tsendFilter: vm.sendFilter\n\t}\n}\n\n//Init Vue app\nvm.vueapp = new Vue(vm.vueconfig);\n"
            },
            "reactJS": {
              "html": "<!-- Write your HTML <div></div> and CSS <style></style> here -->\n<!--Focus here and F11 to full screen editor-->",
              "js": "//Write your Vue JSON controller code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\n\nvm.vueconfig = {\n\tel: $element[0],\n\tdata:{\n\t\tds:[]\n\t},\n\tmethods:{\n\t\tdrawVueComponent: function(newData,oldData){\n\t\t\t//This will be call on new data\n\t\t},\n\t\tresizeEvent: function(){\n\t\t\t//Resize event\n\t\t},\n\t\tdestroyVueComponent: function(){\n\n\t\t},\n\t\treceiveValue: function(data){\n\t\t\t//data received from datalink\n\t\t}\n\t}\n}\n\n//Init Vue app\nvm.vueapp = new Vue(vm.vueconfig);\n"
            },
            "angularJS": {
              "html": "<!-- Write your HTML <div></div> and CSS <style></style> here -->\n<!--Focus here and F11 to full screen editor-->",
              "js": "//Write your controller (JS code) code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\nvm.initLiveComponent = function(){\n\n};\n\n//This function will be call when data change. On first execution oldData will be null\nvm.drawLiveComponent = function(newData, oldData){\n\n};\n\n//This function will be call on element resize\nvm.resizeEvent = function(){\n\n}\n\n//This function will be call when element is destroyed\nvm.destroyLiveComponent = function(){\n\n};\n\n//This function will be call when receiving a value from vm.sendValue(idGadgetTarget,data)\nvm.receiveValue = function(data){\n\n};"
            }
          }

          httpService.getTemplateTypes().then(function(data){
            $scope.templatetypes = data.data;
          })

         
          $scope.loadTemplates = function(type) {
            return httpService.getUserGadgetTemplate(type).then(
              function(templates){
                $scope.templates = templates.data;
              }
            );
          };

          $scope.useTemplate = function(byId) {    
            if(!$scope.template) return;     
            $scope.config.type = $scope.type;
            $scope.config.subtype = $scope.templatetype;
            $scope.config.content=$scope.template.template        
            $scope.config.contentcode=$scope.template.templateJS
            if(byId){
              $scope.config.template = $scope.template.identification;
            }
            showAddGadgetTemplateParameterDialog($scope.type,$scope.config,$scope.layergrid);
            $mdDialog.hide();
          };
          $scope.noUseTemplate = function() {
            $scope.config.type = $scope.type;
            $scope.config.subtype = $scope.templatetype;
            httpService.getGadgetTemplateType($scope.templatetype).then(function(data){
              $scope.config.content=data.data.template
              $scope.config.contentcode=data.data.templateJS

              $scope.layergrid.push($scope.config);
              $mdDialog.cancel();
            });
          };

        }
        $mdDialog.show({
          controller: AddGadgetController,
          templateUrl: 'app/partials/edit/addGadgetTemplateDialog.html',
          parent: angular.element(document.body),
          clickOutsideToClose:true,
          fullscreen: false, // Only for -xs, -sm breakpoints.
          openFrom: '.sidenav-fab',
          closeTo: angular.element(document.querySelector('.sidenav-fab')),
          locals: {
            type: type,
            config: config,
            layergrid: layergrid
          }
        })
        .then(function() {
       
        }, function() {
          $scope.status = 'You cancelled the dialog.';
        });
      }

      function showAddGadgetTemplateParameterDialog(type,config,layergrid){
        
        $mdDialog.show({
          controller: 'editTemplateParamsController',
          templateUrl: 'app/partials/edit/addGadgetTemplateParameterDialog.html',
          parent: angular.element(document.body),
          clickOutsideToClose:true,
          fullscreen: false, // Only for -xs, -sm breakpoints.
          openFrom: '.sidenav-fab',
          closeTo: angular.element(document.querySelector('.sidenav-fab')),
          locals: {
            type: type,
            config: config,
            element: null,
            layergrid: layergrid,
            edit: false
          }
        })
        .then(function() {

        }, function() {
          $scope.status = 'You cancelled the dialog.';
        });
      }

      function dropElementEvent(e,newElem){
        var type = (!e.dataTransfer?(vm.dashboard.gridOptions.dragGadgetType?vm.dashboard.gridOptions.dragGadgetType:'livehtml'):e.dataTransfer.getData("type"));
        var id = (!e.dataTransfer?null:e.dataTransfer.getData("gid"));
        var title = (!e.dataTransfer?null:e.dataTransfer.getData("title"));
        var config = (!e.dataTransfer?null:e.dataTransfer.getData("config"));
        if(!type || type === ''){
          return;
        }
        newElem.id = id || (type + "_" + (new Date()).getTime());
        newElem.content = type;
        newElem.type = type;

        newElem.header = {
          enable: true,
          title: {

           
            iconColor: "hsl( 206, 54%, 5%)",
            text: title || (type + "_" + (new Date()).getTime()),
            textColor: "hsl(206,54%,5%)"
          },
          backgroundColor: "hsl(0, 0%, 100%)",
          height: 40
        }
        newElem.backgroundColor ="white";
        newElem.padding = 0;
        newElem.border = {

          color: "hsl(0, 0%, 90%)",
          width: 0,
          radius: 5
        }
        if(!id){
          if(type == 'livehtml'){
            if(!config){
              newElem.content = "<!-- Write your HTML <div></div> and CSS <style></style> here -->\n<!--Focus here and F11 to full screen editor-->";
              newElem.contentcode = "//Write your controller (JS code) code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\nvm.initLiveComponent = function(){\n\n};\n\n//This function will be call when data change. On first execution oldData will be null\nvm.drawLiveComponent = function(newData, oldData){\n\n};\n\n//This function will be call on element resize\nvm.resizeEvent = function(){\n\n}\n\n//This function will be call when element is destroyed\nvm.destroyLiveComponent = function(){\n\n};\n\n//This function will be call when receiving a value from vm.sendValue(idGadgetTarget,data)\nvm.receiveValue = function(data){\n\n};";       
              showAddGadgetTemplateDialog(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard); 
            }
            else{//with config we draw direct the gadget
              var fconfig = Object.assign(newElem, JSON.parse(config));
              vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard.push(fconfig);
              utilsService.forceRender($scope);
            }
          }else  if(type == 'gadgetfilter'){
            newElem.content = "<!-- Write your HTML <div></div> and CSS <style></style> here -->\n<!--Focus here and F11 to full screen editor-->";
            newElem.contentcode = "//Write your controller (JS code) code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\nvm.initLiveComponent = function(){\n\n};\n\n//This function will be call when data change. On first execution oldData will be null\nvm.drawLiveComponent = function(newData, oldData){\n\n};\n\n//This function will be call on element resize\nvm.resizeEvent = function(){\n\n}\n\n//This function will be call when element is destroyed\nvm.destroyLiveComponent = function(){\n\n};\n\n//This function will be call when receiving a value from vm.sendValue(idGadgetTarget,data)\nvm.receiveValue = function(data){\n\n};";       
            addGadgetFilter(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
          }
          else if(type == 'html5'){         
            addGadgetHtml5(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard); 
          }
          else if(type == 'favoritegadget'){         
            showAddFavoriteGadgetDialog(newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
          }
          else{         
            showAddGadgetDialog(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
          }
        }
        else{
          if(type=='favoritegadget'){
            addFavoriteGadget(id,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
          }else{
            addGadgetGeneric(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
          }
        }
      };


      function sendResizeToGadget(item, itemComponent) {
        $timeout(
          function(){
            $scope.$broadcast("$resize", "");
          },100
        );
      }
    };

    vm.checkIndex = function(index){
      return vm.selectedpage === index;
    }

    vm.setIndex = function(index){
      vm.selectedpage = index;
    }


    

    function showUrlParamDialog(parameters){
      showUrlParamController.$inject = ["$scope", "__env", "$mdDialog", "httpService", "parameters"];
      function showUrlParamController($scope,__env, $mdDialog, httpService,  parameters) {
        $scope.parameters = parameters;
        $scope.hide = function() {
          $mdDialog.hide();
        };

        
        $scope.save = function() {
          var sPageURL = $window.location.pathname;	
          var url = urlParamService.generateUrlWithParam(sPageURL,$scope.parameters);
          $window.location.href = url;          	
        };
       
      }
      $mdDialog.show({
        controller: showUrlParamController,
        templateUrl: 'app/partials/edit/formUrlparamMandatoryDialog.html',
        parent: angular.element(document.body),
        clickOutsideToClose:false,
        fullscreen: true, // Only for -xs, -sm breakpoints.
        openFrom: '.sidenav-fab',
        closeTo: angular.element(document.querySelector('.sidenav-fab')),
        locals: {
          parameters: parameters
        }
      })
      .then(function() {
     
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    }



  }
})();

angular.module('dashboardFramework').run(['$templateCache', function($templateCache) {$templateCache.put('app/dashboard.html','<edit-dashboard ng-if=vm.editmode iframe=vm.iframe id=vm.id public=vm.public dashboard=vm.dashboard selectedpage=vm.selectedpage synopticedit=vm.synopticEdit></edit-dashboard><ng-include src="\'app/partials/view/header.html\'"></ng-include><ng-include src="\'app/partials/view/tabsnav.html\'"></ng-include><span><div id=printing ng-repeat="page in vm.dashboard.pages" ng-if="vm.checkIndex($index) && !vm.gadgetFullScreen"><synopticeditor ng-if="vm.synopticEdit.showSynoptic && vm.synopticEdit.showEditor" config=vm.synopticEdit synoptic=vm.dashboard.synoptic dashboardheader=vm.dashboard.header synopticinit=vm.dashboard.synopticInit imagelib=vm.dashboard.imagelib iframe=vm.iframe></synopticeditor><page page=page synopticedit=vm.synopticEdit iframe=vm.iframe editbuttonsiframe=vm.dashboard.editButtonsIframe tabson="vm.dashboard.pages.length > 1" gridoptions=vm.dashboard.gridOptions dashboardheader=vm.dashboard.header editmode=vm.editmode selectedlayer=vm.selectedlayer showfavoritesg=vm.dashboard.showfavoritesg synoptic=vm.dashboard.synoptic class=flex ng-if=vm.checkIndex($index)></page></div><element-full-screen ng-if=vm.gadgetFullScreen id={{vm.gadgetFullScreen.id}} idtemplate={{vm.gadgetFullScreen.idtemplate}} iframe=vm.iframe element=vm.gadgetFullScreen editmode=vm.editmode gridoptions=vm.dashboard.gridOptions></element-full-screen></span>');
$templateCache.put('app/partials/edit/addEditDataDiscoveryMetrics.html','<md-dialog aria-label="Add Metrics"><md-toolbar><div class=md-toolbar-tools><h2>Metric</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content><md-subheader ng-if="index === undefined" class="md-primary form-header">Add new metric:</md-subheader><md-subheader ng-if="index !== undefined" class="md-primary form-header">Edit metric:</md-subheader><md-input-container flex=25><label>Metric Name</label><input type=text class=flex ng-model=name></md-input-container><md-input-container flex=75><label>Metric Formula</label><input type=text class=flex ng-model=formula></md-input-container></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-if="index === undefined" ng-click=createMetric() class="md-raised md-primary">Create</md-button><md-button ng-if="index !== undefined" ng-click=editMetric() class="md-raised md-primary">Edit</md-button><md-button ng-click=cancel() class="md-raised md-primary">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/addFavoriteGadgetDialog.html','<md-dialog aria-label=Container><md-toolbar><div class=md-toolbar-tools><h2>Add to Favorites</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content layout-padding><div layout=row ng-if=showAlert ng-class=isOK layout-margin layout-align="left center"><label>{{message}}</label></div><div layout=row layout-margin layout-align="left center"><md-input-container class=md-block flex=60><label>Identifier</label><input class=flex ng-model=identifier required md-autofocus></md-input-container><md-checkbox flex=50 ng-model=saveconnections class=checkbox-adjust placeholder="Save connections" md-autofocus><md-tooltip md-direction=top>Enable/Disable Save connections</md-tooltip>Save connections</md-checkbox></div></md-dialog-content><md-dialog-actions layout=row><md-button ng-disabled=validateImputIdentifier() ng-click=addFavoriteGadget() class="md-raised md-primary">CREATE</md-button><md-button ng-click=hide() class="md-raised md-primary">CLOSE</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/addFavoriteGadgetDropDialog.html','<md-dialog aria-label="Add Gadget"><md-toolbar><div class=md-toolbar-tools><h2>Select Favorite Gadget to add</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-input-container><label>Select</label><md-select ng-model=gadget md-on-open=loadGadgets()><md-option ng-value=gadget ng-repeat="gadget in gadgets"><em>{{gadget}}</em></md-option></md-select></md-input-container><md-dialog-actions layout=row><span flex></span><md-button class=md-warm ng-click=cancel()>Cancel</md-button><md-button class="md-raised md-primary" ng-click=addGadget()>Add</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/addGadgetDialog.html','<md-dialog aria-label="Add Gadget"><md-toolbar><div class=md-toolbar-tools><h2>Select Gadget to add</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-input-container><label>Select gadget type</label><md-select ng-model=gadget md-on-open=loadGadgets()><md-option ng-value=gadget ng-repeat="gadget in gadgets"><em>{{gadget.identification}}</em></md-option></md-select></md-input-container><md-dialog-actions layout=row><span flex></span><md-button class=md-warm ng-click=cancel()>Cancel</md-button><md-button class="md-raised md-primary" ng-click=addGadget()>Add Gadget</md-button><md-button class="md-raised md-primary" ng-click=newGadget()>New Gadget</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/addGadgetTemplateDialog.html','<md-dialog aria-label="Add Gadget"><md-toolbar><div class=md-toolbar-tools><h2>Create using template?</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-input-container><label>Select Template Type</label><md-select ng-model=templatetype ng-change="template == null"><md-option ng-value=ttype.id ng-repeat="ttype in templatetypes">{{ttype.identification}}</md-option></md-select></md-input-container><md-input-container><label>Select Template</label><md-select ng-model=template md-on-open=loadTemplates(templatetype)><md-option ng-value=template ng-repeat="template in templates"><span><strong>{{template.identification}} </strong></span><span>{{template.description}}</span></md-option></md-select></md-input-container><md-dialog-actions layout=row><span flex></span><md-button class=md-warn ng-click=noUseTemplate()>No, start with empty {{templatetype}} template</md-button><md-button class="md-raised md-primary" ng-click=useTemplate(true)>Yes, use {{template.identification}}</md-button><md-button class="md-raised md-primary" ng-click=useTemplate()>Yes, copy {{template.identification}} for edit</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/addGadgetTemplateParameterDialog.html','<md-dialog aria-label="Add Gadget"><md-toolbar><div class=md-toolbar-tools><h2>Select a content for the parameters</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-input-container class=md-dialog-content><label>Datasource</label><md-select md-autofocus placeholder="Select new template datasource" ng-model=config.datasource ng-model-options="{trackBy: \'$value.name\'}" md-on-open=loadDatasources() ng-change=loadDatasourcesFields()><md-option ng-value={name:datasource.identification,refresh:datasource.refresh,type:datasource.mode,id:datasource.id} ng-repeat="datasource in datasources">{{datasource.identification}}</md-option></md-select></md-input-container><div flex=""><md-content><md-list class=md-dense flex=""><md-list-item class=md-3-line ng-repeat="item in parameters"><div class=md-list-item-text layout=column><span>{{ item.label }}</span><md-input-container ng-if="item.type==\'labelsText\'" class=md-dialog-content><p>string value :</p><input type=text ng-model=item.value></md-input-container><md-input-container ng-if="item.type==\'labelsNumber\'" class=md-dialog-content><p>number value :</p><input type=number ng-model=item.value></md-input-container><md-input-container ng-if="item.type==\'labelsds\'" class=md-dialog-content><p>value :</p><md-select required md-autofocus placeholder="Select parameter from datasource" ng-model-options="{trackBy: \'$value.field\'}" ng-model=item.value><md-option ng-value={field:datasourceField.field,type:datasourceField.type} ng-repeat="datasourceField in datasourceFields">{{datasourceField.field}}</md-option></md-select></md-input-container><md-input-container ng-if="item.type==\'labelsdspropertie\'" class=md-dialog-content><p>value :</p><md-select required md-autofocus placeholder="Select parameter from datasource" ng-model-options="{trackBy: \'$value.field\'}" ng-model=item.value><md-option ng-value={field:datasourceField.field,type:datasourceField.type} ng-repeat="datasourceField in datasourceFields">{{datasourceField.field}}</md-option></md-select></md-input-container><md-input-container ng-if="item.type==\'selects\'" class=md-dialog-content><p>value :</p><md-select required md-autofocus placeholder="Select parameter value" ng-model=item.value><md-option ng-value=optionsValue ng-repeat="optionsValue in item.optionsValue">{{optionsValue}}</md-option></md-select></md-input-container></div></md-list-item></md-list></md-content></div><md-dialog-actions layout=row><span flex></span><md-button class="md-raised md-primary" ng-click=save()>Ok</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/addWidgetBottomSheet.html','<md-bottom-sheet class="md-grid addGadget-sheet" layout=column><div layout=row style="margin-top: 4px;" layout-align=left ng-cloak><span class=addGadget-title>Drag and drop your gadget</span><md-button class="md-mini md-icon-button cross-close" aria-label="Open Menu" ng-click=closeBottomSheet()><img src=/controlpanel/static/images/dashboards/icon_button_cross_black.svg></md-button></div><div ng-cloak class=row-button-gad><div layout=row layout-align="center center" layout-wrap style="margin-bottom: 15px;  margin-top: -10px;"><div flex=9><div class=dragg-button-gad id=line draggable=true ng-click="checkGadgetType(\'line\')"><img src=/controlpanel/static/images/dashboards/icon_line_chart.svg><div class=gadget-text>Line chart</div></div></div><div flex=9><div class=dragg-button-gad id=bar draggable=true ng-click="checkGadgetType(\'bar\')"><img src=/controlpanel/static/images/dashboards/icon_bar_chart.svg><div class=gadget-text>Bar chart</div></div></div><div flex=9><div class=dragg-button-gad id=mixed draggable=true ng-click="checkGadgetType(\'mixed\')"><img src=/controlpanel/static/images/dashboards/icon_mixed_chart.svg><div class=gadget-text>Mixed chart</div></div></div><div flex=9><div class=dragg-button-gad id=pie draggable=true ng-click="checkGadgetType(\'pie\')"><img src=/controlpanel/static/images/dashboards/icon_piechart.svg><div class=gadget-text>Pie chart</div></div></div><div flex=9><div class=dragg-button-gad id=wordcloud draggable=true ng-click="checkGadgetType(\'wordcloud\')"><img src=/controlpanel/static/images/dashboards/icon_wordcloud.svg><div class=gadget-text>Word cloud</div></div></div><div flex=9><div class=dragg-button-gad id=map draggable=true ng-click="checkGadgetType(\'map\')"><img src=/controlpanel/static/images/dashboards/icon_map.svg><div class=gadget-text>Map</div></div></div><div flex=9><div class=dragg-button-gad id=radar draggable=true ng-click="checkGadgetType(\'radar\')"><img src=/controlpanel/static/images/dashboards/icon_radar.svg><div class=gadget-text>Radar</div></div></div><div flex=9><div class=dragg-button-gad id=table draggable=true ng-click="checkGadgetType(\'table\')"><img src=/controlpanel/static/images/dashboards/icon_table.svg><div class=gadget-text>Table</div></div></div><div flex=9><div class=dragg-button-gad id=datadiscovery draggable=true ng-click="checkGadgetType(\'datadiscovery\')"><img src=/controlpanel/static/images/dashboards/widgets.svg><div class=gadget-text>Datadiscovery</div></div></div><div flex=9><div class=dragg-button-gad id=livehtml draggable=true ng-click="checkGadgetType(\'livehtml\')"><img src=/controlpanel/static/images/dashboards/icon_template.svg><div class=gadget-text>Template</div></div></div><div flex=9><div class=dragg-button-gad id=html5 draggable=true ng-click="checkGadgetType(\'html5\')"><img src=/controlpanel/static/images/dashboards/icon_live_html.svg><div class=gadget-text>HTML 5</div></div></div><div flex=9><div class=dragg-button-gad id=gadgetfilter draggable=true ng-click="checkGadgetType(\'gadgetfilter\')"><img src=/controlpanel/static/images/dashboards/icon_filter_gadget.svg><div class=gadget-text>Filter</div></div></div><div flex=9 ng-if=dashboard.showfavoritesg><div class=dragg-button-gad id=favoritegadget draggable=true ng-click="checkGadgetType(\'favoritegadget\')"><img src=/controlpanel/static/images/dashboards/icon_star.svg><div class=gadget-text>Favorite</div></div></div></div></div></md-bottom-sheet>');
$templateCache.put('app/partials/edit/askCloseDashboardDialog.html','<md-dialog><form ng-cloak><md-dialog-content><div class=md-dialog-content layout=column layout-align="center center"><div class=md-dialog-alert-title>Dashboard Editor</div><div class=md-dialog-alert-text>Would you like to save Dashboard before Close?. If you close without saving the changes will be lost</div></div></md-dialog-content><md-dialog-actions layout=row layout-align="center center"><md-button class=cancel-button ng-click="answer(\'CLOSE\')">CLOSE</md-button><md-button class=ok-button ng-click="answer(\'SAVE\')">SAVE</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/askDeleteDashboardDialog.html','<md-dialog><form ng-cloak><md-dialog-content><div class=md-dialog-content layout=column layout-align="center center"><div class=md-dialog-alert-title>Dashboard Editor</div><div class=md-dialog-alert-text>Your dashboard was successfully saved!</div></div></md-dialog-content><md-dialog-actions layout=row layout-align="center center"><md-button class=cancel-button ng-click="answer(\'CLOSE\')">CLOSE</md-button><md-button class=ok-button ng-click="answer(\'DELETE\')">DELETE</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/dashboardInUseDialog.html','<md-dialog><form ng-cloak><md-dialog-content><div class=md-dialog-content layout=column layout-align="center center"><div class=md-dialog-alert-title>Dashboard Editor</div><div class=md-dialog-alert-text>This dashboard is being edited by another user. Do you want to continue or back to the list?</div></div></md-dialog-content><md-dialog-actions layout=row layout-align="center center"><md-button class=cancel-button ng-click=hide()>CONTINUE</md-button><md-button class=ok-button ng-click=ok()>BACK</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/datalinkDialog.html','<md-dialog aria-label=Pages><md-toolbar><div class=md-toolbar-tools><h2>Datalink</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content><md-subheader class="md-primary form-header">Add new connection:</md-subheader><md-list><md-list-item class=md-no-proxy><md-input-container flex=25><label>Source</label><input class=flex list=gadgetsSourceslist ng-model=emitterGadget ng-change=refreshGadgetEmitterFields(emitterGadget)><datalist id=gadgetsSourceslist><option ng-repeat="gadget in gadgetsSources" ng-value=gadget.prettyTitle></option></datalist></md-input-container><md-input-container flex=25><label>Source Field</label><input class=flex list=emitterGadgetFieldlist ng-model=emitterGadgetField><datalist id=emitterGadgetFieldlist><option ng-repeat="field in gadgetEmitterFields" ng-value=field.field>{{field.field}}</option></datalist></md-input-container><md-input-container flex=25 style="padding-bottom: 25px!important;"><label>Target Gadget</label><md-select ng-model=targetGadget aria-label="Target Gadget" placeholder="Target Gadget" class=flex ng-change=refreshGadgetTargetFields(targetGadget)><md-option ng-repeat="gadget in gadgetsTargets" ng-value=gadget.id>{{prettyGadgetInfo(gadget)}}</md-option></md-select></md-input-container><md-input-container flex=25><label>{{targetDatasource?\'Target Field\' + \'(\' + targetDatasource + \')\':\'Target Field\'}}</label><input class=flex list=targetGadgetFieldlist ng-model=targetGadgetField><datalist id=targetGadgetFieldlist><option ng-repeat="field in gadgetTargetFields" ng-value=field.field>{{field.field}}</option></datalist></md-input-container><md-input-container class=hide flex=25><md-checkbox ng-model=filterChaining class=flex>Unchained filter</md-checkbox></md-input-container><md-input-container flex=5><md-button class="md-icon-button md-primary" aria-label="Add Connection" ng-click=create(findEmitterGadgetID(emitterGadget),emitterGadgetField,targetGadget,targetGadgetField,filterChaining)><md-icon>add</md-icon></md-button></md-input-container></md-list-item></md-list><md-subheader class="md-primary form-header">Connections:</md-subheader><md-table-container style="margin-bottom: 12px;"><table md-table ng-model=connections md-progress=promise><thead md-head><tr md-row><th md-column><span>Source Gadget</span></th><th md-column><span>Source Field</span></th><th md-column><span>Target Gadget</span></th><th md-column><span>Target Field</span></th><th md-column><span>Options</span></th></tr></thead><tbody md-body><tr md-row md-select=c md-select-id=name md-auto-select ng-repeat="c in connections"><td md-cell>{{ generateGadgetInfo(c.source) }}</td><td md-cell>{{c.sourceField}}</td><td md-cell>{{ generateGadgetInfo(c.target) }}</td><td md-cell>{{c.targetField}}</td><td md-cell><md-button class="md-icon-button md-primary" aria-label="Edit Connection" ng-click=edit(c.source,c.sourceField,c.target,c.targetField,c.filterChaining)><md-icon>create</md-icon></md-button><md-button class="md-icon-button md-warn" aria-label="Delete connection" ng-click=delete(c.source,c.sourceField,c.target,c.targetField,c.filterChaining)><md-icon>clear</md-icon></md-button></td></tr></tbody></table></md-table-container></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class="md-raised md-primary">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/datasourcesDialog.html','<md-dialog aria-label=Layers><md-toolbar><div class=md-toolbar-tools><h2>Page Datasources</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content><md-subheader class="md-primary form-header">Datasources:</md-subheader><md-list><md-list-item ng-repeat="(nameDatasource, data) in dashboard.pages[selectedpage].datasources"><md-input-container flex=60><label>Datasource name</label><input ng-model=nameDatasource md-autofocus disabled></md-input-container><md-input-container flex=40><md-button ng-if="data.triggers.length == 0" class="md-icon-button md-warn" aria-label="Delete Datasource" ng-click=delete(nameDatasource)><md-icon>clear</md-icon></md-button></md-input-container></md-list-item></md-list><md-subheader>Add New Datasource</md-subheader><md-list><md-list-item><md-input-container flex=80><md-select required md-autofocus placeholder="Select new page datasource" ng-model=datasource md-on-open=loadDatasources()><md-option ng-if=!dashboard.pages[selectedpage].datasources[datasource.identification] ng-value=datasource ng-repeat="datasource in datasources">{{datasource.identification}}</md-option></md-select></md-input-container><md-input-container flex=30><md-button class="md-icon-button md-primary" aria-label="Add Datasource" ng-click=create()><md-icon>add</md-icon></md-button></md-input-container></md-list-item></md-list></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class="md-raised md-primary">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/deleteErrorDialog.html','<md-dialog><form ng-cloak><md-dialog-content><div class=md-dialog-content layout=column layout-align="center center"><div class=md-dialog-alert-title>Dashboard Editor</div><div class=md-dialog-alert-text>There was an error deleting your dashboard!</div></div></md-dialog-content><md-dialog-actions layout=row layout-align="center center"><md-button class=ok-button ng-click="answer(\'OK\')">OK</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/deleteOKDialog.html','<md-dialog><form ng-cloak><md-dialog-content><div class=md-dialog-content layout=column layout-align="center center"><div class=md-dialog-alert-title>Dashboard Editor</div><div class=md-dialog-alert-text>Your dashboard was successfully Deleted!</div></div></md-dialog-content><md-dialog-actions layout=row layout-align="center center"><md-button class=ok-button ng-click="answer(\'OK\')">OK</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editContainerDialog.html','<md-dialog aria-label=Container><md-toolbar><div class=md-toolbar-tools><h2>Edit Gadget ({{element.id}})</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content><md-subheader class="md-primary form-header">Gadget Header:</md-subheader><div layout=row layout-margin layout-align="left center"><md-checkbox flex=5 ng-model=element.header.enable class=checkbox-adjust placeholder="Enable Header"><md-tooltip md-direction=top>Enable/Disable Header</md-tooltip></md-checkbox><md-input-container flex=45><input ng-model=element.header.height type=number ng-disabled="element.header.enable==false" placeholder="Header Height"></md-input-container><md-input-container flex=50><label>Background Color</label><color-picker ng-model=element.header.backgroundColor></color-picker></md-input-container><md-input-container flex=25><label>Gadget Title</label><input ng-model=element.header.title.text required md-autofocus></md-input-container><md-input-container flex=75><label>Text Color</label><color-picker flex=50 ng-model=element.header.title.textColor></color-picker></md-input-container></div><div layout=row layout-margin layout-align="left center"><md-autocomplete flex=25 ng-disabled=false md-no-cache=false md-selected-item=ctrl.icons[$index] md-search-text-change=ctrl.searchTextChange(ctrl.searchText) md-search-text=element.header.title.icon md-selected-item-change=ctrl.selectedItemChange(item) md-items="icon in queryIcon(element.header.title.icon)" md-item-text=icon md-min-length=0 md-menu-class=autocomplete-custom-template md-floating-label="Select icon of gadget"><md-item-template style="background-color: red"><span class=item-title><md-icon>{{icon}}</md-icon><span>{{icon}}</span></span></md-item-template></md-autocomplete><md-input-container flex=75><label>Icon Color</label><color-picker flex=50 ng-model=element.header.title.iconColor></color-picker></md-input-container></div><md-subheader class="md-primary form-header">Gadget Content:</md-subheader><div layout=row layout-margin layout-align="left center"><md-input-container flex=25><input ng-model=element.padding type=number placeholder="Content Padding"></md-input-container><md-input-container flex=75><label>Body Background</label><color-picker flex=100 ng-model=element.backgroundColor></color-picker></md-input-container></div><div layout=row layout-margin layout-align="left center"><md-input-container flex=33><input ng-model=element.border.width type=number placeholder="Border width"></md-input-container><md-input-container flex=33><input ng-model=element.border.radius type=number placeholder="Corner Radius"></md-input-container><md-input-container flex=30><label>Border Color</label><color-picker flex=33 ng-model=element.border.color></color-picker></md-input-container></div><md-subheader class="md-primary form-header">Gadget Config:</md-subheader><div layout=row layout-margin layout-align="left center"><md-checkbox flex=50 ng-model=element.showOnlyFiltered class=checkbox-adjust placeholder="Show widget only filtered">Show Gadget only when it is filtered</md-checkbox><md-checkbox flex=50 ng-model=element.notshowDotsMenu class=checkbox-adjust placeholder="Do not show menu">Do not show menu</md-checkbox></div></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class="md-raised md-primary">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editCustomMenuOptions.html','<md-dialog aria-label=Container><md-toolbar><div class=md-toolbar-tools><h2>Edit Custom Menu Options</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content layout-padding><md-subheader class="md-primary form-header">Custom Options Menu:</md-subheader><div layout=row layout-margin layout-align="left center"><md-table-container style="margin-bottom: 12px;"><table md-table ng-model=element.customMenuOptions md-progress=promise><thead md-head><tr md-row><th md-column><span>Description</span></th><th md-column><span>ID</span></th><th md-column><span>Image Path</span></th><th md-column><span>Position</span></th><th md-column><span>Options</span></th></tr></thead><tbody md-body><tr md-row md-select=c md-select-id=name md-auto-select ng-repeat="c in element.customMenuOptions"><td md-cell>{{c.description}}</td><td md-cell>{{c.id}}</td><td md-cell>{{c.imagePath}}</td><td md-cell>{{c.position}}</td><td md-cell><md-button class="md-icon-button md-primary" aria-label="Edit Menu Option" ng-click=editMenuOption(c.id)><md-icon>create</md-icon></md-button><md-button class="md-icon-button md-warn" aria-label="Delete Menu Option" ng-click=deleteMenuOption(c.id)><md-icon>clear</md-icon></md-button></td></tr></tbody></table></md-table-container></div><md-subheader class="md-primary form-header">Add New Custom Menu Option:</md-subheader><div layout=row layout-margin layout-align="left center"><md-input-container class=md-block flex=15><label>Identifier</label><input class=flex ng-model=tempMenuOp.id required md-autofocus></md-input-container><md-input-container class=md-block flex=15><label>Description</label><input class=flex ng-model=tempMenuOp.description required md-autofocus></md-input-container><md-input-container class=md-block flex=40><label>Image Path</label><input class=flex ng-model=tempMenuOp.imagePath md-autofocus></md-input-container><md-input-container class=md-block flex=15 style="padding-bottom: 25px!important;"><label>Position</label><md-select ng-model=tempMenuOp.position aria-label=position placeholder=Position class=flex><md-option ng-repeat="positionElem in positionList" ng-value=positionElem.id>{{positionElem.description}}</md-option></md-select></md-input-container></div></md-dialog-content><md-dialog-actions layout=row><md-input-container class=md-block><md-button ng-click=addCustomMenuOpt() class="md-raised md-primary">CREATE</md-button></md-input-container><md-input-container class=md-block><md-button ng-click=hide() class="md-raised md-primary">Close</md-button></md-input-container></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editDashboardButtons.html','<div ng-class=ed.toolbarButtonsAssignclass() id=toolbarButtonsEdition layout=row layout-align="right right" style="z-index:9000; margin-top: 12px;"><md-button ng-if=ed.showHideMoveToolBarButton() style="min-width:35px !important" id=toolbarButtonsEditionMove class="md-fab md-primary md-mini md-hue-2 transparent-color" aria-label="Move toolbar buttons"><md-tooltip md-direction=bottom>Move toolbar buttons</md-tooltip><img src=/controlpanel/static/images/dashboards/Icon_move_horizontal.svg></md-button><span ng-if=ed.synopticedit.showSynoptic><md-switch class=md-primary md-no-ink aria-label="Hide editor" ng-click=ed.hideShowSynopticEditor()></md-switch><md-tooltip ng-if=ed.synopticedit.showEditor md-direction=bottom>Hide editor</md-tooltip><md-tooltip ng-if=!ed.synopticedit.showEditor md-direction=bottom>Show editor</md-tooltip></span><md-button ng-if=ed.showHideAddElementButton() ng-disabled="ed.synopticedit.showSynoptic && ed.synopticedit.showEditor" style="min-width:35px !important" class="md-fab md-mini md-warn transparent-color" ng-click=ed.showListBottomSheet() aria-label="Add Element"><md-tooltip md-direction=bottom>Add Element</md-tooltip><img src=/controlpanel/static/images/dashboards/icon_button_plus.svg></md-button><md-button ng-if=ed.showHideDataLinkButton() style="min-width:35px !important" class="md-fab md-mini md-warn transparent-color" ng-click=ed.showDatalink() aria-label="Show datalink"><md-tooltip md-direction=bottom>Datalink</md-tooltip><img src=/controlpanel/static/images/dashboards/icon_button_arrows.svg></md-button><md-button ng-if=ed.showHideUrlParameterButton() style="min-width:35px !important" class="md-fab md-mini md-warn transparent-color" ng-click=ed.showUrlParam() aria-label="Show URL Parameters"><md-tooltip md-direction=bottom>URL Parameters</md-tooltip><img src=/controlpanel/static/images/dashboards/icon_button_triangle.svg></md-button><md-menu ng-if=ed.showHideConfigButton() md-offset="0 60"><md-button aria-label="Open menu with custom trigger" class="md-fab md-warn md-mini transparent-color" ng-click=$mdMenu.open()><img src=/controlpanel/static/images/dashboards/icon_button_boxes.svg></md-button><md-menu-content width=2><md-menu-item><md-button aria-label=Pages ng-click=ed.pagesEdit()><img src=/controlpanel/static/images/dashboards/icon_menu_pages.svg> <span>Pages</span></md-button></md-menu-item><md-menu-item><md-button aria-label="Configure Dashboard" ng-click=ed.dashboardEdit()><img src=/controlpanel/static/images/dashboards/icon_menu_preferences.svg> <span>Configuration</span></md-button></md-menu-item><md-menu-item><md-button aria-label="Dashboard Style" ng-click=ed.dashboardStyleEdit()><img src=/controlpanel/static/images/dashboards/icon_menu_style.svg> <span>Styled</span></md-button></md-menu-item><md-menu-item><md-button aria-label="Dashboard Style" ng-click=ed.dashboardHistoricalEdit()><img src=/controlpanel/static/images/dashboards/edit.svg> <span>Historical</span></md-button></md-menu-item><md-menu-item ng-if=ed.dashboard.showfavoritesg><md-button aria-label="Dashboard Style" ng-click=ed.favoriteGadgetsList()><img src=/controlpanel/static/images/dashboards/star-default.svg> <span>Favorite Gadgets</span></md-button></md-menu-item><md-menu-item><md-button aria-label="Header Libs" ng-click=ed.dashboardHeaderLibs()><img height=18 src=/controlpanel/static/images/dashboards/icon_live_html.svg> <span>Header Libs</span></md-button></md-menu-item></md-menu-content></md-menu><md-button style="min-width:35px !important" class="md-fab md-primary md-mini md-hue-2 transparent-color" ng-click=ed.savePage() aria-label="Save Dashboard"><md-tooltip md-direction=bottom>Save Dashboard</md-tooltip><img src=/controlpanel/static/images/dashboards/icon_button_save.svg></md-button><md-button ng-if=ed.showHideTrashButton() style="min-width:35px !important" class="md-fab md-primary md-mini md-hue-2 transparent-color" ng-click=ed.deleteDashboard() aria-label="Delete Dashboard"><md-tooltip md-direction=bottom>Delete Dashboard</md-tooltip><img src=/controlpanel/static/images/dashboards/icon_button_bin.svg></md-button><md-button ng-if=ed.showHideCloseButton() style="min-width:35px !important" class="md-fab md-primary md-mini md-hue-2 transparent-color" ng-click=ed.closeDashboard() aria-label="Close Dashboard Editor"><md-tooltip md-direction=bottom>Close Dashboard Editor</md-tooltip><img src=/controlpanel/static/images/dashboards/icon_button_cross.svg></md-button></div>');
$templateCache.put('app/partials/edit/editDashboardDialog.html','<md-dialog aria-label=Layers><md-toolbar><div class=md-toolbar-tools><h2>Dashboard configuration</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content><md-subheader class="md-primary form-header">Dashboard header:</md-subheader><div layout=row layout-margin layout-align="left center" style=margin-top:25px;><md-checkbox flex=5 ng-model=dashboard.header.enable class=checkbox-adjust placeholder="Enable Header" md-autofocus><md-tooltip md-direction=top>Enable/Disable Header</md-tooltip></md-checkbox><md-input-container flex=50><label>Title</label><input ng-model=dashboard.header.title md-autofocus></md-input-container><md-input-container flex=25><input ng-model=dashboard.header.height min=20 max=200 step=1 type=number placeholder="Header Height"></md-input-container><md-input-container flex=25><input ng-model=dashboard.header.logo.height min=0 max=200 step=1 type=number placeholder="Logo Height"></md-input-container></div><div layout=row layout-margin layout-align="left center"><md-input-container flex=30><label>Header Color</label><color-picker options="{restrictToFormat:false, preserveInputFormat:false}" ng-model=dashboard.header.backgroundColor></color-picker></md-input-container><md-input-container flex=30><label>Title Color</label><color-picker ng-model=dashboard.header.textColor></color-picker></md-input-container><md-input-container flex=30><label>Icon Color</label><color-picker ng-model=dashboard.header.iconColor></color-picker></md-input-container><md-input-container flex=30><label>Page Color</label><color-picker ng-model=dashboard.header.pageColor></color-picker></md-input-container></div><lf-ng-md-file-input style="margin-top:25px; margin-left: 15px;" flex=70 ng-change=onFilesChange() lf-api=apiUpload lf-files=auxUpload.file lf-placeholder="" lf-browse-label="Change Logo Img" accept=image/* progress lf-filesize=1MB lf-remove-label=""></lf-ng-md-file-input><md-subheader class="md-primary form-header" style="margin-top: 20px;">Visibility and Navigation properties:</md-subheader><md-checkbox ng-model=dashboard.navigation.showBreadcrumb class=flex>Show Breadcrumbs</md-checkbox><md-checkbox ng-model=dashboard.navigation.showBreadcrumbIcon class=flex>Show Breadcrumbs Icon</md-checkbox><md-checkbox ng-model=dashboard.showfavoritesg class=flex>Show favorite gadgets</md-checkbox><md-subheader class="md-primary form-header" style="margin-top: 10px; margin-bottom: 10px;">Grid Settings:</md-subheader><div class="layout-row layout-align-start-center flex" style="margin: 10px 15px;"><md-input-container class=flex><label>Grid Type</label><md-select aria-label="Grid type" ng-model=dashboard.gridOptions.gridType ng-change=changedOptions() placeholder="Grid Type" class=flex><md-option value=fit>Fit to screen</md-option><md-option value=scrollVertical>Scroll Vertical</md-option><md-option value=scrollHorizontal>Scroll Horizontal</md-option><md-option value=fixed>Fixed</md-option><md-option value=verticalFixed>Vertical Fixed</md-option><md-option value=horizontalFixed>Horizontal Fixed</md-option></md-select></md-input-container><md-input-container class=flex><label>Compact Type</label><md-select aria-label="Compact type" ng-model=dashboard.gridOptions.compactType ng-change=changedOptions() placeholder="Compact Type" class=flex><md-option value=none>None</md-option><md-option value=compactUp>Compact Up</md-option><md-option value=compactLeft>Compact Left</md-option><md-option value=compactLeft&Up>Compact Left & Up</md-option><md-option value=compactUp&Left>Compact Up & Left</md-option></md-select></md-input-container></div><div class="layout-row layout-align-start-center flex" style="margin: 10px 15px;"><md-input-container class=flex><input ng-model=dashboard.gridOptions.minCols type=number placeholder="Min Grid Cols" ng-change=changedOptions()></md-input-container><md-input-container class=flex><input ng-model=dashboard.gridOptions.maxCols type=number placeholder="Max Grid Cols" ng-change=changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex" style="margin: 10px 15px;"><md-input-container class=flex><input ng-model=dashboard.gridOptions.minRows type=number placeholder="Min Grid Rows" ng-change=changedOptions()></md-input-container><md-input-container class=flex><input ng-model=dashboard.gridOptions.maxRows type=number placeholder="Max Grid Rows" ng-change=changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex" style="margin: 10px 15px;"><md-input-container class=flex><input ng-model=dashboard.gridOptions.margin min=0 max=100 step=1 type=number placeholder=Margin ng-change=changedOptions()></md-input-container><md-checkbox ng-model=dashboard.gridOptions.outerMargin ng-change=changedOptions() class=flex>Outer Margin</md-checkbox></div><div class="layout-row layout-align-start-center flex" style="margin: 10px 15px;"><md-input-container class=flex><input ng-model=dashboard.gridOptions.mobileBreakpoint type=number placeholder="Mobile Breakpoint" ng-change=changedOptions()></md-input-container><md-checkbox ng-model=dashboard.gridOptions.disableWindowResize ng-change=changedOptions() class=flex>Disable window resize</md-checkbox></div><md-subheader>Item Settings</md-subheader><div class="layout-row layout-align-start-center flex" style="margin: 10px 15px;"><md-input-container class=flex><input ng-model=dashboard.gridOptions.defaultItemRows type=number placeholder="Default Item Rows" ng-change=changedOptions()></md-input-container><md-input-container class=flex><input ng-model=dashboard.gridOptions.defaultItemCols type=number placeholder="Default Item Cols" ng-change=changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex" style="margin: 10px 15px;"><md-input-container class=flex><input ng-model=dashboard.gridOptions.fixedColWidth type=number placeholder="Fixed Col Width" ng-change=changedOptions()></md-input-container><md-input-container class=flex><input ng-model=dashboard.gridOptions.fixedRowHeight type=number placeholder="Fixed layout-row layout-align-start-center Height" ng-change=changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex" style="margin: 10px 15px;"><md-checkbox ng-model=dashboard.gridOptions.keepFixedHeightInMobile ng-change=changedOptions() class=flex>Keep Fixed Height In Mobile</md-checkbox><md-checkbox ng-model=dashboard.gridOptions.keepFixedWidthInMobile ng-change=changedOptions() class=flex>Keep Fixed Width In Mobile</md-checkbox></div></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class="md-primary md-raised">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editDashboardHeaderLibsDialog.html','<md-dialog aria-label=Layers style=width:80%><md-toolbar><div class=md-toolbar-tools><h2>Dashboard Header Libs</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content><md-subheader class="md-primary form-header">Header Libs Content:</md-subheader><div flex><div id=headerlibseditor style=height:500px></div></div></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=saveAndReload() class="md-primary md-raised">Save And Reload Page</md-button><md-button ng-click=hide() class="md-primary md-raised">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editDashboardHistoricalDialog.html','<md-dialog aria-label=Pages><md-toolbar><div class=md-toolbar-tools><h2>Historical</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content><md-subheader class="md-primary form-header">Select the element of the history to load</md-subheader><md-subheader class="md-primary form-header">Saved by the user</md-subheader><md-table-container style="margin-bottom: 12px;"><table md-table ng-model=dates.savedByUser md-progress=promise><thead md-head><tr md-row><th md-column><span>Date</span></th><th md-column><span>Select</span></th></tr></thead><tbody md-body><tr md-row md-select=c md-select-id=name md-auto-select ng-repeat="c in dates.savedByUser"><td md-cell>{{c.date}}</td><td md-cell><md-button class="md-icon-button md-primary" aria-label=Select ng-click=selectDate(c.milis)><md-icon>play_arrow</md-icon></md-button></td></tr></tbody></table></md-table-container><md-subheader class="md-primary form-header">Auto saved</md-subheader><md-table-container style="margin-bottom: 12px;"><table md-table ng-model=dates.autoSave md-progress=promise><thead md-head><tr md-row><th md-column><span>Date</span></th><th md-column><span>Select</span></th></tr></thead><tbody md-body><tr md-row md-select=c md-select-id=name md-auto-select ng-repeat="c in dates.autoSave"><td md-cell>{{c.date}}</td><td md-cell><md-button class="md-icon-button md-primary" aria-label=Select ng-click=selectDate(c.milis)><md-icon>play_arrow</md-icon></md-button></td></tr></tbody></table></md-table-container></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=pushsaveLocalByUser() class="md-raised md-primary">Save only locally</md-button><md-button ng-click=refreshServerVersion() class="md-raised md-primary">Refresh to server version</md-button><md-button ng-click=hide() class="md-raised md-primary">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editDashboardSidenav.html','<md-sidenav class="site-sidenav md-sidenav-left md-whiteframe-4dp layout-padding" md-component-id=left md-is-locked-open=false><label class=md-headline>Grid Settings</label><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><md-input-container class=md-block flex-gt-sm><label>Dashboard Title</label><input ng-model=title></md-input-container></md-input-container><md-input-container class=flex><label>Grid Type</label><md-select aria-label="Grid type" ng-model=main.options.gridType ng-change=main.changedOptions() placeholder="Grid Type" class=flex><md-option value=fit>Fit to screen</md-option><md-option value=scrollVertical>Scroll Vertical</md-option><md-option value=scrollHorizontal>Scroll Horizontal</md-option><md-option value=fixed>Fixed</md-option><md-option value=verticalFixed>Vertical Fixed</md-option><md-option value=horizontalFixed>Horizontal Fixed</md-option></md-select></md-input-container><md-input-container class=flex><label>Compact Type</label><md-select aria-label="Compact type" ng-model=main.options.compactType ng-change=main.changedOptions() placeholder="Compact Type" class=flex><md-option value=none>None</md-option><md-option value=compactUp>Compact Up</md-option><md-option value=compactLeft>Compact Left</md-option><md-option value=compactLeft&Up>Compact Left & Up</md-option><md-option value=compactUp&Left>Compact Up & Left</md-option></md-select></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.swap ng-change=main.changedOptions() class=flex>Swap Items</md-checkbox><md-checkbox ng-model=main.options.pushItems ng-change=main.changedOptions() class=flex>Push Items</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.disablePushOnDrag ng-change=main.changedOptions() class=flex>Disable Push On Drag</md-checkbox><md-checkbox ng-model=main.options.disablePushOnResize ng-change=main.changedOptions() class=flex>Disable Push On Resize</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.pushDirections.north ng-change=main.changedOptions() class=flex>Push North</md-checkbox><md-checkbox ng-model=main.options.pushDirections.east ng-change=main.changedOptions() class=flex>Push East</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.pushDirections.south ng-change=main.changedOptions() class=flex>Push South</md-checkbox><md-checkbox ng-model=main.options.pushDirections.west ng-change=main.changedOptions() class=flex>Push West</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.draggable.enabled ng-change=main.changedOptions() class=flex>Drag Items</md-checkbox><md-checkbox ng-model=main.options.resizable.enabled ng-change=main.changedOptions() class=flex>Resize Items</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.pushResizeItems ng-change=main.changedOptions() class=flex>Push Resize Items</md-checkbox><md-input-container class=flex><label>Display grid lines</label><md-select aria-label="Display grid lines" ng-model=main.options.displayGrid placeholder="Display grid lines" ng-change=main.changedOptions()><md-option value=always>Always</md-option><md-option value=onDrag&Resize>On Drag & Resize</md-option><md-option value=none>None</md-option></md-select></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.minCols type=number placeholder="Min Grid Cols" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.maxCols type=number placeholder="Max Grid Cols" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.minRows type=number placeholder="Min Grid Rows" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.maxRows type=number placeholder="Max Grid Rows" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.margin min=0 max=30 step=1 type=number placeholder=Margin ng-change=main.changedOptions()></md-input-container><md-checkbox ng-model=main.options.outerMargin ng-change=main.changedOptions() class=flex>Outer Margin</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.mobileBreakpoint type=number placeholder="Mobile Breakpoint" ng-change=main.changedOptions()></md-input-container><md-checkbox ng-model=main.options.disableWindowResize ng-change=main.changedOptions() class=flex>Disable window resize</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.scrollToNewItems ng-change=main.changedOptions() class=flex>Scroll to new items</md-checkbox><md-checkbox ng-model=main.options.disableWarnings ng-change=main.changedOptions() class=flex>Disable console warnings</md-checkbox></div><label class=md-headline>Item Settings</label><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.maxItemCols type=number placeholder="Max Item Cols" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.minItemCols type=number placeholder="Min Item Cols" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.maxItemRows type=number placeholder="Max Item Rows" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.minItemRows type=number placeholder="Min Item Rows" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.maxItemArea type=number placeholder="Max Item Area" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.minItemArea type=number placeholder="Min Item Area" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.defaultItemRows type=number placeholder="Default Item Rows" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.defaultItemCols type=number placeholder="Default Item Cols" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.fixedColWidth type=number placeholder="Fixed Col Width" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.fixedRowHeight type=number placeholder="Fixed layout-row layout-align-start-center Height" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.keepFixedHeightInMobile ng-change=main.changedOptions() class=flex>Keep Fixed Height In Mobile</md-checkbox><md-checkbox ng-model=main.options.keepFixedWidthInMobile ng-change=main.changedOptions() class=flex>Keep Fixed Width In Mobile</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.enableEmptyCellClick ng-change=main.changedOptions() class=flex>Enable click to add</md-checkbox><md-checkbox ng-model=main.options.enableEmptyCellContextMenu ng-change=main.changedOptions() class=flex>Enable right click to add</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.enableEmptyCellDrop ng-change=main.changedOptions() class=flex>Enable drop to add</md-checkbox><md-checkbox ng-model=main.options.enableEmptyCellDrag ng-change=main.changedOptions() class=flex>Enable drag to add</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.emptyCellDragMaxCols type=number placeholder="Drag Max Cols" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.emptyCellDragMaxRows type=number placeholder="Drag Max Rows" ng-change=main.changedOptions()></md-input-container></div></md-sidenav>');
$templateCache.put('app/partials/edit/editDashboardStyleDialog.html','<md-dialog aria-label=Layers><md-toolbar><div class=md-toolbar-tools><h2>Dashboard configuration</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content><md-subheader class="md-primary form-header">Gadgets Header:</md-subheader><div layout=row layout-margin layout-align="left center" style="margin: 10px 15px;"><md-checkbox flex=5 ng-model=style.header.enable class=checkbox-adjust placeholder="Enable Header"><md-tooltip md-direction=top>Enable/Disable Header</md-tooltip></md-checkbox><md-input-container flex=20><input ng-model=style.header.height type=number placeholder="Header Height"></md-input-container><md-input-container flex=30><label>Header Background</label><color-picker flex=40 ng-model=style.header.backgroundColor></color-picker></md-input-container></div><md-subheader class="md-primary form-header">Gadgets title:</md-subheader><div layout=row layout-margin layout-align="left center" style="margin: 10px 15px;"><md-input-container flex=30><label>Header Text Color</label><color-picker flex=50 ng-model=style.header.title.textColor></color-picker></md-input-container><md-input-container flex=30><label>Header Icon Color</label><color-picker flex=50 ng-model=style.header.title.iconColor></color-picker></md-input-container></div><md-subheader class="md-primary form-header">Gadgets body:</md-subheader><div layout=row layout-margin layout-align="left center" style="margin: 10px 15px;"><md-input-container flex=30><label>Body Background</label><color-picker flex=100 ng-model=style.backgroundColor></color-picker></md-input-container><md-input-container flex=50><input ng-model=style.padding type=number placeholder="Content Padding"></md-input-container></div><div layout=row layout-margin layout-align="left center" style="margin: 10px 15px;"><md-input-container flex=33><input ng-model=style.border.width type=number placeholder="Border width"></md-input-container><md-input-container flex=33><input ng-model=style.border.radius type=number placeholder="Corner Radius"></md-input-container><md-input-container flex=30><label>Border Color</label><color-picker flex=33 ng-model=style.border.color></color-picker></md-input-container></div><md-subheader class="md-primary form-header">Gadgets Template:</md-subheader><div layout=row layout-margin layout-align="left center" style="margin: 10px 15px;"><md-checkbox flex=50 ng-model=style.nomargin class=checkbox-adjust placeholder="Hide button clean filters">No margin</md-checkbox></div></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class="md-primary md-raised">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editDataDiscoveryColumnStyle.html','<md-dialog aria-label="Add Metrics"><md-toolbar><div class=md-toolbar-tools><h2>Column Style</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content><md-subheader class="md-primary form-header">Column Alias:</md-subheader><md-input-container flex=50><label>Alias</label><input ng-model=alias md-autofocus></md-input-container><md-subheader class="md-primary form-header">Conditional Style:</md-subheader><md-list><md-list-item ng-repeat="style in styles"><md-input-container flex=10><label>Condition</label><md-select ng-model=style.cond placeholder="Select a operation"><md-option value=all>all</md-option><md-option value=data>data</md-option><md-option value=total>total</md-option><md-option value=subtotals>subtotals</md-option><md-option value=equal>=</md-option><md-option value=mayorequal>>=</md-option><md-option value=minorequal>&#60;=</md-option><md-option value=mayor>></md-option><md-option value=minor>&#60;</md-option><md-option value=between>between</md-option><md-option value=in>in</md-option></md-select></md-input-container><md-input-container flex=30 style="height: 35px;"><label>Value</label><input ng-model=style.val></md-input-container><md-input-container ng-if="style.cond == \'between\'" flex=30 style="height: 35px;"><label>Value</label><input type=text class=flex ng-model=val2></md-input-container><md-input-container flex=30 style="height: 35px;"><label>CSS Style</label><input ng-model=style.style></md-input-container><md-input-container flex=30 style="height: 35px;"><label>Transform value function</label><input type=text class=flex ng-model=style.vfunction></md-input-container><md-input-container flex=30><md-button class="md-icon-button md-warn" aria-label="Delete layer" ng-click=delete($index)><md-icon>clear</md-icon></md-button></md-input-container></md-list-item></md-list><md-subheader>Add New Conditional Style</md-subheader><md-list><md-list-item class=md-no-proxy><md-input-container flex=15><label>Condition</label><md-select ng-model=cond aria-label=condition placeholder=Condition class=flex><md-option value=all>all</md-option><md-option value=data>data</md-option><md-option value=total>total</md-option><md-option value=subtotals>subtotals</md-option><md-option value=equal>=</md-option><md-option value=mayorequal>>=</md-option><md-option value=minorequal>&#60;=</md-option><md-option value=mayor>></md-option><md-option value=minor>&#60;</md-option><md-option value=between>between</md-option><md-option value=in>in</md-option></md-select></md-input-container><md-input-container flex=30 style="height: 35px;"><label>Value</label><input type=text class=flex ng-model=val></md-input-container><md-input-container ng-if="cond == \'between\'" flex=30 style="height: 35px;"><label>Value</label><input type=text class=flex ng-model=val2></md-input-container><md-input-container flex=30 style="height: 35px;"><label>CSS Style</label><input type=text class=flex ng-model=style></md-input-container><md-input-container flex=30 style="height: 35px;"><label>Transform value function</label><input type=text class=flex ng-model=vfunction></md-input-container><md-input-container flex=10><md-button class="md-icon-button md-primary" aria-label="Add layer" ng-click=create()><md-icon>add</md-icon></md-button></md-input-container></md-list-item></md-list></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=cancel() class="md-raised md-primary">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editFavoriteGadgetListDialog.html','<md-dialog aria-label=Pages><md-toolbar><div class=md-toolbar-tools><h2>Favorite Gadgets</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content><md-table-container style="margin-bottom: 12px;"><table md-table ng-model=identifications md-progress=promise><thead md-head><tr md-row><th md-column><span>identification</span></th><th md-column><span>Delete</span></th></tr></thead><tbody md-body><tr md-row md-select=c md-select-id=name md-auto-select ng-repeat="c in identifications"><td md-cell>{{c}}</td><td md-cell><md-button class="md-icon-button md-warn md-button ng-scope md-ink-ripple" aria-label=Select ng-click=delete(c)><md-icon>clear</md-icon></md-button></td></tr></tbody></table></md-table-container></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class="md-raised md-primary">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editFilterDialog.html','<md-dialog aria-label=Container><md-toolbar><div class=md-toolbar-tools><h2>Edit Filters</h2><span flex></span><md-button class=md-icon-button ng-mousedown=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content layout-padding><md-subheader class="md-primary form-header">Filters:</md-subheader><div layout=row layout-margin layout-align="left center"><md-table-container style="margin-bottom: 12px;"><table md-table ng-model=element.filters md-progress=promise><thead md-head><tr md-row><th md-column><span>Identifier</span></th><th md-column><span>Type Filter</span></th><th md-column><span>Label Name</span></th><th md-column><span>Initially Filtered</span></th><th md-column><span>Hidden</span></th><th md-column><span>Options</span></th></tr></thead><tbody md-body><tr md-row md-select=c md-select-id=name md-auto-select ng-repeat="c in element.filters"><td md-cell>{{c.id}}</td><td md-cell>{{c.type}}</td><td md-cell>{{c.name}}</td><td md-cell><md-checkbox ng-disabled=true ng-model=c.initialFilter class=flex></md-checkbox></td><td md-cell><md-checkbox ng-disabled=true ng-model=c.hide class=flex></md-checkbox></td><td md-cell><md-button class="md-icon-button md-primary" aria-label="Edit Connection" ng-click=editFilter(c.id)><md-icon>create</md-icon></md-button><md-button class="md-icon-button md-warn" aria-label="Delete connection" ng-click=deleteFilter(c.id)><md-icon>clear</md-icon></md-button></td></tr></tbody></table></md-table-container></div><md-subheader class="md-primary form-header">Add new filter:</md-subheader><div layout=row layout-margin layout-align="left center"><md-input-container class=md-block flex=15><label>Identifier</label><input class=flex ng-model=tempFilter.id required md-autofocus></md-input-container><md-input-container class=md-block flex=15 style="padding-bottom: 25px!important;"><label>Type Filter</label><md-select ng-model=tempFilter.type aria-label="type filter" placeholder="Type Filter" class=flex md-on-open=hideFields() ng-change=hideFields(tempFilter.type)><md-option ng-repeat="typeElement in typeList" ng-value=typeElement.id>{{typeElement.description}}</md-option></md-select></md-input-container><md-input-container class=md-block flex=15><label>Target Field</label><input class=flex list=targetGadgetFieldlist ng-model=targetGadgetField><datalist id=targetGadgetFieldlist><option ng-repeat="field in gadgetTargetFields" ng-value=field.field>{{field.field}}</option></datalist></md-input-container><md-checkbox flex=15 ng-model=tempFilter.initialFilter ng-hide=hideInitialFilter class=checkbox-adjust placeholder="Initially filtered">Initially filtered</md-checkbox><md-checkbox flex=15 ng-model=tempFilter.hide class=checkbox-adjust ng-hide=hideHide placeholder="Hidden filter">Hide</md-checkbox></div><div layout=row layout-margin layout-align="left center"><md-input-container ng-hide=hideLabelName class=md-block flex-gt-sm><label>label Name</label><input ng-model=tempFilter.name md-autofocus></md-input-container><md-input-container ng-hide=hideDatasource class=md-block flex=15 style="margin-bottom: 32px;"><label>Datasource</label><md-select required md-autofocus placeholder="Select datasource" ng-model=tempFilter.data.ds ng-model-options="{trackBy: \'$value\'}" md-on-open=loadDatasources() ng-change=setdsTargetFields()><md-option value=""></md-option><md-option ng-value=datasource.identification ng-repeat="datasource in datasources">{{datasource.identification}}</md-option></md-select></md-input-container><md-input-container ng-hide=hideDatasource class=md-block flex=15><label>Options Values</label><input class=flex list=targetfieldlistopt ng-model=tempFilter.data.dsFieldValue><datalist id=targetfieldlistopt><option ng-repeat="field in dsTargetFields" ng-value=field.field>{{field.field}}</option></datalist></md-input-container><md-input-container ng-hide=hideDatasource class=md-block flex=15><label>Options Descriptions</label><input class=flex list=targetfieldlistdesc ng-model=tempFilter.data.dsFieldDes><datalist id=targetfieldlistdesc><option ng-repeat="field in dsTargetFields" ng-value=field.field>{{field.field}}</option></datalist></md-input-container><md-input-container ng-hide=hideOperator class=md-block style="padding-bottom: 25px!important;" flex-gt-sm><label>Operator</label><md-select ng-model=tempFilter.op aria-label=Operator placeholder=Operator class=flex><md-option ng-repeat="opElement in opList" ng-value=opElement.id>{{opElement.description}}</md-option></md-select></md-input-container><md-input-container class=md-block flex-gt-sm ng-hide=hideValue><label>Value</label><input ng-model=tempFilter.value md-autofocus></md-input-container><md-list ng-hide=hideOptions><md-list-item class=md-no-proxy><md-input-container class=md-block flex-gt-sm><label>Value</label><input ng-model=option md-autofocus></md-input-container><md-input-container class=md-block flex-gt-sm><label>Description</label><input ng-model=description md-autofocus></md-input-container><md-input-container class=md-block flex-gt-sm><md-button class="md-icon-button md-primary" aria-label="Add Option" ng-click=addOption(option,description)><md-icon>add</md-icon></md-button></md-input-container></md-list-item></md-list><md-table-container style="margin-bottom: 12px;" ng-hide=hideOptions><table md-table ng-model=tempFilter.data.options md-progress=promise><thead md-head><tr md-row><th md-column><span>Value</span></th><th md-column><span>Description</span></th><th md-column><span></span></th></tr></thead><tbody md-body><tr md-row md-select=opts md-select-id=name md-auto-select ng-repeat="opts in tempFilter.data.options"><td md-cell>{{opts}}</td><td md-cell>{{tempFilter.data.optionsDescription[$index]}}</td><td md-cell><md-button class="md-icon-button md-warn" aria-label="Delete target" ng-click=deleteOption(opts)><md-icon>clear</md-icon></md-button></td></tr></tbody></table></md-table-container></div><md-subheader ng-if="element.type!=\'gadgetfilter\'" class="md-primary form-header">Filter layout:</md-subheader><div layout=row ng-if="element.type!=\'gadgetfilter\'" layout-margin layout-align="left center"><md-checkbox flex=50 ng-model=element.filtersInModal class=checkbox-adjust placeholder="Show filters in modal">Show filters in modal</md-checkbox><md-checkbox flex=50 ng-model=element.hideBadges class=checkbox-adjust placeholder="Hide active filters">Hide active filters</md-checkbox><md-checkbox flex=50 ng-model=element.hidebuttonclear class=checkbox-adjust placeholder="Hide button clean filters">Hide button clean filters</md-checkbox></div></md-dialog-content></form><md-dialog-actions layout=row><md-input-container class=md-block><md-button ng-mousedown=addFilter() class="md-raised md-primary">CREATE</md-button></md-input-container><md-input-container class=md-block><md-button ng-mousedown=hide() class="md-raised md-primary">Close</md-button></md-input-container></md-dialog-actions></md-dialog>');
$templateCache.put('app/partials/edit/editGadgetDialog.html','<md-dialog class=modal-lg aria-label=GadgetEditor><md-toolbar><div class=md-toolbar-tools><h2>Edit Gadget</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak style=min-width:800px><md-subheader class="md-primary form-header">Edit Content:</md-subheader><md-input-container><label>Datasource</label><md-select required md-autofocus placeholder="Select new template datasource" ng-model=element.datasource ng-model-options="{trackBy: \'$value.name\'}" md-on-open=loadDatasources()><md-option value=""></md-option><md-option ng-value={name:datasource.identification,id:datasource.id,refresh:datasource.refresh,type:datasource.mode} ng-repeat="datasource in datasources">{{datasource.identification}}</md-option></md-select></md-input-container><div style=margin-top:25px;><md-input-container layout=horizontal layout-padding class=md-dialog-content style="margin-top: -32px;margin-bottom: -28px;"><md-toolbar flex ng-if="contenteditor.html === element.content" class="small-toolbar green-toolbar"><h5 class=no-margin><span>HTML Compiled & Synchronized</span></h5></md-toolbar><md-toolbar flex ng-if="contenteditor.html !== element.content" class="small-toolbar md-accent"><h5 class=no-margin><span>HTML NO compiled & NO Synchronized</span></h5></md-toolbar><div flex=0></div><md-toolbar flex ng-if="contenteditor.js === element.contentcode" class="small-toolbar green-toolbar"><h5 class=no-margin><span>JS Compiled & Synchronized</span></h5></md-toolbar><md-toolbar flex ng-if="contenteditor.js !== element.contentcode" class="small-toolbar md-accent"><h5 class=no-margin><span>JS NO compiled & NO Synchronized</span></h5></md-toolbar></md-input-container><md-input-container layout=horizontal layout-padding><div flex><div id=htmleditor style=height:350px></div></div><div flex><div id=jseditor style=height:350px></div></div></md-input-container></div><div layout=row layout-align="end center" style=margin-top:25px;><md-dialog-actions layout=row><span flex></span><md-button ng-click=compile() ng-show=!livecompilation class="md-raised md-secondary">Compile & Synchronize</md-button><md-button ng-click="livecompilation = !livecompilation" class="md-raised md-secondary">{{livecompilation?\'Disable Live Compilation\':\'Enable Live Compilation\'}}</md-button><md-button ng-click=hide() class="md-raised md-primary">Close</md-button></md-dialog-actions></div></form></md-dialog>');
$templateCache.put('app/partials/edit/editGadgetHTML5Dialog.html','<md-dialog class=modal-lg aria-label=GadgetEditor><md-toolbar><div class=md-toolbar-tools><h2>Edit Gadget</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-subheader class="md-primary form-header">Edit Content:</md-subheader><div style=margin-top:25px;><md-input-container layout=horizontal layout-padding class=md-dialog-content style="margin-top: -32px;margin-bottom: -28px;"><md-toolbar ng-if="contenteditor.html === element.content" flex class="small-toolbar green-toolbar"><h5 class=no-margin><span>HTML 5 Compiled & Synchronized</span></h5></md-toolbar><md-toolbar ng-if="contenteditor.html !== element.content" flex class="small-toolbar md-accent"><h5 class=no-margin><span>HTML 5 NO compiled & NO Synchronized</span></h5></md-toolbar></md-input-container></div><md-input-container class=md-dialog-content><div flex><div id=htmleditor style=height:350px></div></div></md-input-container><md-dialog-actions layout=row><span flex></span><md-button ng-click=compile() ng-show=!livecompilation class="md-raised md-secondary">Compile & Synchronize</md-button><md-button ng-click="livecompilation = !livecompilation" class="md-raised md-secondary">{{livecompilation?\'Disable Live Compilation\':\'Enable Live Compilation\'}}</md-button><md-button ng-click=hide() class="md-raised md-primary">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editPageButtons.html','<div class=sidenav-fab layout=row layout-align="center end"><md-button class="md-fab md-mini md-primary" ng-click=main.sidenav.toggle()><md-icon>settings</md-icon></md-button><md-button class="md-fab md-mini md-danger" ng-click=main.addItem()><md-icon>add</md-icon><md-tooltip>Add widget</md-tooltip></md-button></div>');
$templateCache.put('app/partials/edit/editPageSidenav.html','<md-sidenav class="site-sidenav md-sidenav-left md-whiteframe-4dp layout-padding" md-component-id=left md-is-locked-open=true><label class=md-headline>Grid Settings</label><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><md-input-container class=md-block flex-gt-sm><label>Dashboard Title</label><input ng-model=title></md-input-container></md-input-container><md-input-container class=flex><label>Grid Type</label><md-select aria-label="Grid type" ng-model=main.options.gridType ng-change=main.changedOptions() placeholder="Grid Type" class=flex><md-option value=fit>Fit to screen</md-option><md-option value=scrollVertical>Scroll Vertical</md-option><md-option value=scrollHorizontal>Scroll Horizontal</md-option><md-option value=fixed>Fixed</md-option><md-option value=verticalFixed>Vertical Fixed</md-option><md-option value=horizontalFixed>Horizontal Fixed</md-option></md-select></md-input-container><md-input-container class=flex><label>Compact Type</label><md-select aria-label="Compact type" ng-model=main.options.compactType ng-change=main.changedOptions() placeholder="Compact Type" class=flex><md-option value=none>None</md-option><md-option value=compactUp>Compact Up</md-option><md-option value=compactLeft>Compact Left</md-option><md-option value=compactLeft&Up>Compact Left & Up</md-option><md-option value=compactUp&Left>Compact Up & Left</md-option></md-select></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.swap ng-change=main.changedOptions() class=flex>Swap Items</md-checkbox><md-checkbox ng-model=main.options.pushItems ng-change=main.changedOptions() class=flex>Push Items</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.disablePushOnDrag ng-change=main.changedOptions() class=flex>Disable Push On Drag</md-checkbox><md-checkbox ng-model=main.options.disablePushOnResize ng-change=main.changedOptions() class=flex>Disable Push On Resize</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.pushDirections.north ng-change=main.changedOptions() class=flex>Push North</md-checkbox><md-checkbox ng-model=main.options.pushDirections.east ng-change=main.changedOptions() class=flex>Push East</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.pushDirections.south ng-change=main.changedOptions() class=flex>Push South</md-checkbox><md-checkbox ng-model=main.options.pushDirections.west ng-change=main.changedOptions() class=flex>Push West</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.draggable.enabled ng-change=main.changedOptions() class=flex>Drag Items</md-checkbox><md-checkbox ng-model=main.options.resizable.enabled ng-change=main.changedOptions() class=flex>Resize Items</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.pushResizeItems ng-change=main.changedOptions() class=flex>Push Resize Items</md-checkbox><md-input-container class=flex><label>Display grid lines</label><md-select aria-label="Display grid lines" ng-model=main.options.displayGrid placeholder="Display grid lines" ng-change=main.changedOptions()><md-option value=always>Always</md-option><md-option value=onDrag&Resize>On Drag & Resize</md-option><md-option value=none>None</md-option></md-select></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.minCols type=number placeholder="Min Grid Cols" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.maxCols type=number placeholder="Max Grid Cols" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.minRows type=number placeholder="Min Grid Rows" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.maxRows type=number placeholder="Max Grid Rows" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.margin min=0 max=30 step=1 type=number placeholder=Margin ng-change=main.changedOptions()></md-input-container><md-checkbox ng-model=main.options.outerMargin ng-change=main.changedOptions() class=flex>Outer Margin</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.mobileBreakpoint type=number placeholder="Mobile Breakpoint" ng-change=main.changedOptions()></md-input-container><md-checkbox ng-model=main.options.disableWindowResize ng-change=main.changedOptions() class=flex>Disable window resize</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.scrollToNewItems ng-change=main.changedOptions() class=flex>Scroll to new items</md-checkbox><md-checkbox ng-model=main.options.disableWarnings ng-change=main.changedOptions() class=flex>Disable console warnings</md-checkbox></div><label class=md-headline>Item Settings</label><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.maxItemCols type=number placeholder="Max Item Cols" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.minItemCols type=number placeholder="Min Item Cols" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.maxItemRows type=number placeholder="Max Item Rows" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.minItemRows type=number placeholder="Min Item Rows" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.maxItemArea type=number placeholder="Max Item Area" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.minItemArea type=number placeholder="Min Item Area" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.defaultItemRows type=number placeholder="Default Item Rows" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.defaultItemCols type=number placeholder="Default Item Cols" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.fixedColWidth type=number placeholder="Fixed Col Width" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.fixedRowHeight type=number placeholder="Fixed layout-row layout-align-start-center Height" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.keepFixedHeightInMobile ng-change=main.changedOptions() class=flex>Keep Fixed Height In Mobile</md-checkbox><md-checkbox ng-model=main.options.keepFixedWidthInMobile ng-change=main.changedOptions() class=flex>Keep Fixed Width In Mobile</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.enableEmptyCellClick ng-change=main.changedOptions() class=flex>Enable click to add</md-checkbox><md-checkbox ng-model=main.options.enableEmptyCellContextMenu ng-change=main.changedOptions() class=flex>Enable right click to add</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.enableEmptyCellDrop ng-change=main.changedOptions() class=flex>Enable drop to add</md-checkbox><md-checkbox ng-model=main.options.enableEmptyCellDrag ng-change=main.changedOptions() class=flex>Enable drag to add</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.emptyCellDragMaxCols type=number placeholder="Drag Max Cols" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.emptyCellDragMaxRows type=number placeholder="Drag Max Rows" ng-change=main.changedOptions()></md-input-container></div></md-sidenav>');
$templateCache.put('app/partials/edit/formUrlparamMandatoryDialog.html','<md-dialog aria-label="Mandatory Parameters"><md-toolbar><div class=md-toolbar-tools><h2>Select a content for mandatory parameters</h2></div></md-toolbar><form ng-cloak><div flex=""><md-content><md-list class=md-dense flex=""><md-list-item class=md-3-line ng-repeat="item in parameters"><div class=md-list-item-text layout=column><md-input-container class=md-dialog-content><p>{{ item.name }}</p><input type=text ng-model=item.val></md-input-container></div></md-list-item></md-list></md-content></div><md-dialog-actions layout=row><span flex></span><md-button class="md-raised md-primary" ng-click=save()>Ok</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/initLocalStorageDialog.html','<md-dialog><form ng-cloak><md-dialog-content><div class=md-dialog-content layout=column layout-align="center center"><div class=md-dialog-alert-title>Dashboard Editor</div><div class=md-dialog-alert-text>Do you have a local version later than the version recovered from the server, do you want to load the local version?</div></div></md-dialog-content><md-dialog-actions layout=row layout-align="center center"><md-button class=cancel-button ng-click=hide()>CANCEL</md-button><md-button class=ok-button ng-click=ok()>OK</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/layersDialog.html','<md-dialog aria-label=Layers><form ng-cloak><md-toolbar><div class=md-toolbar-tools><h2>Page Layers</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><md-dialog-content><md-subheader>Layers</md-subheader><md-list><md-list-item ng-repeat="layer in dashboard.pages[selectedpage].layers"><md-input-container flex=70><label>Layer name</label><input ng-model=layer.title required md-autofocus></md-input-container><md-input-container flex=30><md-button ng-if="!$first && dashboard.pages.length > 1" class="md-icon-button md-primary" aria-label=up ng-click=moveUpLayer($index)><md-icon>arrow_upward</md-icon></md-button><md-button ng-if="!$last && dashboard.pages.length > 1" class="md-icon-button md-primary" aria-label=down ng-click=moveDownLayer($index)><md-icon>arrow_downward</md-icon></md-button><md-button ng-if="dashboard.pages[selectedpage].layers.length > 1" class="md-icon-button md-warn" aria-label="Delete layer" ng-click=delete($index)><md-icon>clear</md-icon></md-button></md-input-container></md-list-item></md-list><md-subheader>Add New Layer</md-subheader><md-list><md-list-item><md-input-container flex=70><label>Layer name</label><input ng-model=title required md-autofocus></md-input-container><md-input-container flex=30><md-button class="md-icon-button md-primary" aria-label="Add layer" ng-click=create()><md-icon>add</md-icon></md-button></md-input-container></md-list-item></md-list></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class=md-primary>Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/pagesDialog.html','<md-dialog class=dialog-lg aria-label=Pages><md-toolbar><div class=md-toolbar-tools><h2>Dashboard Pages</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content><md-subheader class="md-primary form-header">Add New Page:</md-subheader><md-list><md-list-item><md-input-container flex=40><label>Page name</label><input ng-model=title required md-autofocus></md-input-container><md-autocomplete style="margin-right: 6px;" flex=30 ng-disabled=false md-no-cache=false md-selected-item=selectedIconItem md-search-text-change=ctrl.searchTextChange(ctrl.searchText) md-search-text=searchIconText md-selected-item-change=ctrl.selectedItemChange(item) md-items="icon in queryIcon(searchIconText)" md-item-text=icon md-min-length=0 md-menu-class=autocomplete-custom-template md-floating-label="Select icon of page"><md-item-template><span class=item-title><md-icon>{{icon}}</md-icon><span>{{icon}}</span></span></md-item-template></md-autocomplete><lf-ng-md-file-input flex=30 lf-files=file lf-placeholder="" lf-browse-label="Change Background Img" accept=image/* progress lf-filesize=5MB lf-remove-label=""></lf-ng-md-file-input><md-input-container class=btn-add-page><md-button class="md-icon-button md-primary" aria-label="Add page" ng-click=create()><md-icon>add</md-icon></md-button></md-input-container></md-list-item></md-list><md-subheader class="md-primary form-header">Dashboard Pages:</md-subheader><md-list><md-list-item ng-repeat="page in dashboard.pages"><md-input-container flex=40><label>Page name</label><input ng-model=page.title required md-autofocus></md-input-container><md-autocomplete flex=30 ng-disabled=false md-no-cache=false md-selected-item=ctrl.icons[$index] md-search-text-change=ctrl.searchTextChange(ctrl.searchText) md-search-text=page.icon md-selected-item-change=ctrl.selectedItemChange(item) md-items="icon in queryIcon(page.icon)" md-item-text=icon md-min-length=0 md-menu-class=autocomplete-custom-template md-floating-label="Select icon of page"><md-item-template><span class=item-title><md-icon>{{icon}}</md-icon><span>{{icon}}</span></span></md-item-template></md-autocomplete><md-input-container flex=30><label>Background Color</label><color-picker options="{restrictToFormat:false, preserveInputFormat:false}" ng-model=page.background.color></color-picker></md-input-container><lf-ng-md-file-input ng-change=onFilesChange($index) lf-api=apiUpload[$index] lf-files=auxUpload[$index].file lf-placeholder="" lf-browse-label="Change Background Img" accept=image/* progress lf-filesize=5MB lf-remove-label=""></lf-ng-md-file-input><md-input-container flex=30 class=btn-add-page><md-button ng-if="!$first && dashboard.pages.length > 1" class="md-icon-button md-primary" aria-label=up ng-click=moveUpPage($index)><md-icon>arrow_upward</md-icon></md-button><md-button ng-if="!$last && dashboard.pages.length > 1" class="md-icon-button md-primary" aria-label=down ng-click=moveDownPage($index)><md-icon>arrow_downward</md-icon></md-button><md-button ng-if="dashboard.pages.length > 1" class="md-icon-button md-warn" aria-label="Delete page" ng-click=delete($index)><md-icon>clear</md-icon></md-button></md-input-container></md-list-item></md-list></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class="md-primary md-raised">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/saveDialog.html','<md-dialog><form ng-cloak><md-dialog-content><div class=md-dialog-content layout=column layout-align="center center"><div class=md-dialog-alert-title>Dashboard Editor</div><div class=md-dialog-alert-text>Your dashboard was successfully saved!</div></div></md-dialog-content><md-dialog-actions layout=row layout-align="center center"><md-button class=ok-button ng-click="answer(\'OK\')">OK</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/saveErrorDialog.html','<md-dialog><form ng-cloak><md-dialog-content><div class=md-dialog-content layout=column layout-align="center center"><div class=md-dialog-alert-title>Dashboard Editor</div><div class=md-dialog-alert-text>There was an error saving your dashboard!</div></div></md-dialog-content><md-dialog-actions layout=row layout-align="center center"><md-button class=ok-button ng-click="answer(\'OK\')">OK</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/saveSynopticDialog.html','<md-dialog><form ng-cloak><md-dialog-content><div class=md-dialog-content layout=column layout-align="center center"><div class=md-dialog-alert-title>Synoptic Editor</div><div class=md-dialog-alert-text>Your synoptic has been temporarily stored, save the dashboard to save it permanently.</div></div></md-dialog-content><md-dialog-actions layout=row layout-align="center center"><md-button class=ok-button ng-click="answer(\'OK\')">OK</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/urlParamDialog.html','<md-dialog aria-label=Pages><md-toolbar><div class=md-toolbar-tools><h2>URL Parameters</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><form ng-cloak><md-dialog-content><md-subheader class="md-primary form-header">Add new parameter:</md-subheader><md-list><md-list-item class=md-no-proxy><md-input-container flex=25><label>Parameter Name</label><input type=text class=flex ng-model=paramName></md-input-container><md-input-container flex=25 style="padding-bottom: 25px!important;"><label>Parameter Type</label><md-select ng-model=type aria-label="Source Field" placeholder="Parameter Type" class=flex><md-option ng-repeat="type in types" ng-value=type>{{type}}</md-option></md-select></md-input-container><md-input-container flex=25 style="padding-bottom: 25px!important;"><label>Target Gadget</label><md-select ng-model=targetGadget aria-label="Target Gadget" placeholder="Target Gadget" class=flex ng-change=refreshGadgetTargetFields(targetGadget)><md-option ng-repeat="gadget in gadgetsTargets" ng-value=gadget.id>{{prettyGadgetInfo(gadget)}}</md-option></md-select></md-input-container><md-input-container flex=25><label>{{targetDatasource?\'Target Field\' + \'(\' + targetDatasource + \')\':\'Target Field\'}}</label><input class=flex list=targetGadgetFieldlist ng-model=targetGadgetField><datalist id=targetGadgetFieldlist><option ng-repeat="field in gadgetTargetFields" ng-value=field.field>{{field.field}}</option></datalist></md-input-container><md-input-container flex=25><md-checkbox ng-model=mandatory class=flex>Mandatory</md-checkbox></md-input-container><md-input-container flex=5><md-button class="md-icon-button md-primary" aria-label="Add Connection" ng-click=create(paramName,type,targetGadget,targetGadgetField,mandatory)><md-icon>add</md-icon></md-button></md-input-container></md-list-item></md-list><md-subheader class="md-primary form-header">Parameters:</md-subheader><md-table-container style="margin-bottom: 12px;"><table md-table ng-model=parameters md-progress=promise><thead md-head><tr md-row><th md-column><span>Parameter Name</span></th><th md-column><span>Parameter Type</span></th><th md-column><span>Target Gadget</span></th><th md-column><span>Target Field</span></th><th md-column><span>Mandatory</span></th><th md-column><span>Options</span></th></tr></thead><tbody md-body><tr md-row md-select=c md-select-id=name md-auto-select ng-repeat="c in parameters"><td md-cell>{{c.paramName | translate}}</td><td md-cell>{{c.type}}</td><td md-cell>{{ generateGadgetInfo(c.target) }}</td><td md-cell>{{c.targetField}}</td><td md-cell><md-checkbox ng-model=c.mandatory ng-disabled=true class=flex></md-checkbox></td><td md-cell><md-button class="md-icon-button md-primary" aria-label="Edit Connection" ng-click=edit(c.paramName,c.type,c.target,c.targetField,c.mandatory)><md-icon>create</md-icon></md-button><md-button class="md-icon-button md-warn" aria-label="Delete connection" ng-click=delete(c.paramName,c.type,c.target,c.targetField,c.mandatory)><md-icon>clear</md-icon></md-button></td></tr></tbody></table></md-table-container></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class="md-raised md-primary">Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/view/header.html','<md-toolbar ng-if=vm.dashboard.header.enable layout=row class=md-hue-2 layout-align="space-between center" ng-style="{\'height\': + vm.dashboard.header.height + \'px\', \'background\': vm.dashboard.header.backgroundColor}"><md-headline layout=row layout-align="start center" class=left-margin-10><img ng-if=vm.dashboard.header.logo.filedata ng-src={{vm.dashboard.header.logo.filedata}} ng-style="{\'height\': + vm.dashboard.header.logo.height + \'px\'}" style="padding-left: 12px; padding-right: 12px"><span class=header-title ng-style="{\'color\': vm.dashboard.header.textColor}">{{\'&nbsp;\' + vm.dashboard.header.title | translate}} </span><span class=header-title ng-style="{\'color\': vm.dashboard.header.iconColor}" ng-if=vm.dashboard.navigation.showBreadcrumbIcon>></span> <span class=header-page-title ng-style="{\'color\': vm.dashboard.header.pageColor}" ng-if=vm.dashboard.navigation.showBreadcrumb>{{vm.dashboard.pages[vm.selectedpage].title | translate}}</span></md-headline></md-toolbar>');
$templateCache.put('app/partials/view/sidenav.html','<md-sidenav class="md-sidenav-right md-whiteframe-4dp" md-component-id=right><header class=nav-header></header><md-content flex="" role=navigation class="_md flex"><md-subheader class="md-no-sticky sidenav-subheader">Dashboard Pages</md-subheader><md-list class=md-hue-2><span ng-repeat="page in vm.dashboard.pages"><md-list-item md-colors="{background: ($index===vm.selectedpage ? \'primary\' : \'grey-A100\')}" ng-click=vm.setIndex($index) flex><md-icon ng-class="{{page.icon}} === \'\' ? \'ng-hide\' : \'sidenav-page-icon\'" md-colors="{color: ($index===vm.selectedpage ? \'grey-A100\' : \'primary\')}">{{page.icon}}</md-icon><p class=sidenav-page-title>{{page.title | translate}}</p></md-list-item></span></md-list></md-content></md-sidenav>');
$templateCache.put('app/partials/view/tabsnav.html','<md-nav-bar ng-if="vm.dashboard.pages.length > 1" md-dynamic-height md-border-bottom><span ng-repeat="page in vm.dashboard.pages"><md-nav-item label=one md-nav-click=vm.setIndex($index)>{{page.title | translate}}</md-nav-item></span></md-nav-bar>');
$templateCache.put('app/components/edit/editDashboardComponent/edit.dashboard.html','<ng-include ng-if=" ed.showHideButtons()" src="\'app/partials/edit/editDashboardButtons.html\'"></ng-include><ng-include src="\'app/partials/edit/editDashboardSidenav.html\'"></ng-include>');
$templateCache.put('app/components/view/datadiscoveryComponent/datadiscovery.html','<div style=height:100% layout=row flex><div flex layout=column><datadiscovery-field-selector flex ng-if="vm.ds && vm.config.config.editFields" columns=vm.config.config.discovery.columns config=vm.config.config></datadiscovery-field-selector><datadiscovery-data-draw flex ng-if=vm.ds reload-data-link=vm.reloadDataLink(reloadchild) get-data-and-style=vm.getDataAndStyle(getDataAndStyleChild) columns=vm.config.config.discovery.columns id=vm.id datastatus=vm.datastatus datasource=vm.ds config=vm.config.config filters=vm.filters></datadiscovery-data-draw></div><datadiscovery-field-picker flex=30 ng-if="vm.ds && vm.config.config.editFields" datasource=vm.ds id=vm.id fields=vm.config.config.discovery.fields.list metrics=vm.config.config.discovery.metrics.list></datadiscovery-field-picker></div>');
$templateCache.put('app/components/view/elementComponent/element.html','<gridster-item ng-hide="!vm.editmode && !vm.datastatus && vm.element.showOnlyFiltered" item=vm.element ng-style="{\'background-color\':vm.element.backgroundColor, \'border-width\': vm.element.border.width + \'px\', \'border-color\': vm.element.border.color, \'border-radius\': vm.element.border.radius + \'px\', \'border-style\': \'solid\'}" ng-class="vm.isMaximized ? \'animate-show-hide widget-maximize\': \'animate-show-hide\'"><div class="element-container fullcontainer"><div class="md-toolbar-tools widget-header md-hue-2" flex ng-if=vm.element.header.enable ng-style="{\'background\':vm.element.header.backgroundColor, \'height\': vm.element.header.height + \'px\'}"><md-icon ng-if=vm.element.header.title.icon ng-style="{\'color\':vm.element.header.title.iconColor,\'font-size\' : \'24px\'}">{{vm.element.header.title.icon}}</md-icon><h5 ng-if=vm.element.header.enable class=gadget-title flex ng-style="{\'color\':vm.element.header.title.textColor}" md-truncate>{{vm.element.header.title.text | translate}}</h5><div ng-repeat="menuOption in vm.element.customMenuOptions"><md-button ng-if="menuOption.position == \'header\'" ng-click=vm.sendCustomMenuOption(menuOption.id) style="margin-right: 10px;" class="cursor-hand md-icon-button"><img draggable=false ng-src="{{menuOption.imagePath ? menuOption.imagePath : \'/controlpanel/static/images/dashboards/icon_button_controls.svg\'}}"><md-tooltip>{{menuOption.description}}</md-tooltip></md-button></div><md-button ng-if=vm.showFiltersInBody() ng-click="vm.toggleRight(vm.element.id+\'rightSidenav\')" style="margin-right: 10px;" class="cursor-hand md-icon-button"><img draggable=false ng-src={{vm.baseimg}}/static/images/dashboards/icon_filter.svg><md-tooltip>Filter</md-tooltip></md-button><md-button ng-if="vm.showfiltersInModal() " ng-click=vm.openFilterDialog() style="margin-right: 10px;" class="cursor-hand md-icon-button"><img draggable=false ng-src={{vm.baseimg}}/static/images/dashboards/icon_filter.svg><md-tooltip>Filter</md-tooltip></md-button><md-button ng-if="vm.editmode && vm.element.header.enable" style="margin-right: 10px;" class="drag-handler md-icon-button"><img draggable=false ng-src={{vm.baseimg}}/static/images/dashboards/Icon_move.svg><md-tooltip>Move</md-tooltip></md-button><div id="{{vm.element.id + \'toolbarheader\'}}"></div><div flex=nogrow layout-align="center right" ng-if="vm.editmode || !vm.element.notshowDotsMenu"><md-menu-bar><md-menu md-position-mode="target-right bottom" md-offset="-4 0"><button ng-click=$mdMenu.open() style="padding: 0px"><img ng-src={{vm.baseimg}}/static/images/dashboards/more.svg><md-tooltip>Options</md-tooltip></button><md-menu-content width=5><md-menu-item><md-button ng-click=vm.toggleFullScreen() aria-label=Fullscreen><img ng-src={{vm.baseimg}}/static/images/dashboards/Icon_full.svg> <span>Fullscreen</span></md-button></md-menu-item><md-menu-item ng-if="(!vm.iframe || vm.iframe && vm.editbuttonsiframe.filterGadgetMenu) && vm.editmode && vm.element.type != \'html5\'"><md-button ng-click=vm.openEditFilterDialog() aria-label="Edit Filter"><img ng-src={{vm.baseimg}}/static/images/dashboards/icon_menu_filter.svg> <span>Edit Filters</span></md-button></md-menu-item><md-menu-item ng-if="!vm.iframe && vm.editmode && vm.element.type === \'livehtml\'"><md-button ng-click=vm.openEditCustomMenuOptionsDialog() aria-label="Custom Menu Options"><img ng-src={{vm.baseimg}}/static/images/dashboards/icon_button_menu.svg style=height:20px;> <span>Custom Menu Options</span></md-button></md-menu-item><md-menu-item ng-if=vm.showfavoritesg><md-button ng-click=vm.addFavoriteDialog() aria-label="Add to Favorites"><img ng-src={{vm.baseimg}}/static/images/dashboards/star-default.svg style="height:20px;color: #060E14;"> <span>Add to Favorites</span></md-button></md-menu-item><md-menu-item ng-if=vm.editmode><md-button ng-click=vm.openEditContainerDialog() aria-label="Edit Container"><img ng-src={{vm.baseimg}}/static/images/dashboards/style.svg> <span>Styling</span></md-button></md-menu-item><md-menu-item ng-if="vm.editmode && ((!vm.iframe || (vm.iframe &&  vm.editbuttonsiframe.editGadgetMenu) ) || vm.eventedit) && (vm.element.type == \'livehtml\' ||  vm.element.type == \'vuetemplate\' ||  vm.element.type == \'reacttemplate\')"><md-button ng-if="vm.element.template == null" ng-click=vm.openEditGadgetDialog() aria-label="Gadget Editor"><img ng-src={{vm.baseimg}}/static/images/dashboards/edit.svg> <span>Edit</span></md-button><md-button ng-if="vm.element.template != null" ng-click=vm.openEditTemplateParamsDialog() aria-label="Gadget Editor"><img ng-src={{vm.baseimg}}/static/images/dashboards/edit.svg> <span>Edit</span></md-button></md-menu-item><md-menu-item ng-if="vm.editmode  && ((!vm.iframe || (vm.iframe &&  vm.editbuttonsiframe.editGadgetMenu) ) || vm.eventedit) &&  (vm.element.type == \'html5\' )"><md-button ng-click=vm.openEditGadgetHTML5Dialog() aria-label="Gadget Editor"><img ng-src={{vm.baseimg}}/static/images/dashboards/edit.svg> <span>Edit</span></md-button></md-menu-item><md-menu-item ng-if="vm.editmode  && ((!vm.iframe || (vm.iframe &&  vm.editbuttonsiframe.editGadgetMenu) ) || vm.eventedit) && (vm.element.type != \'livehtml\' && vm.element.type != \'html5\'&& vm.element.type != \'gadgetfilter\'   && vm.element.type != \'vuetemplate\'  && vm.element.type != \'reacttemplate\')"><md-button ng-click=vm.openEditGadgetIframe() aria-label="Edit Container"><img ng-src={{vm.baseimg}}/static/images/dashboards/edit.svg> <span>Edit</span></md-button></md-menu-item><md-menu-item ng-if=vm.editmode><md-button ng-click=vm.deleteElement()><img ng-src={{vm.baseimg}}/static/images/dashboards/delete.svg> <span>Remove</span></md-button></md-menu-item><div ng-repeat="menuOption in vm.element.customMenuOptions"><md-menu-item ng-if="menuOption.position == \'menu\'"><md-button ng-click=vm.sendCustomMenuOption(menuOption.id)><img ng-src="{{menuOption.imagePath ? menuOption.imagePath : \'/controlpanel/static/images/dashboards/icon_button_controls.svg\'}}" style=height:20px;> <span>{{menuOption.description}}</span></md-button></md-menu-item></div></md-menu-content></md-menu></md-menu-bar></div></div><div flex ng-if=!vm.element.header.enable class=item-buttons><div ng-repeat="menuOption in vm.element.customMenuOptions"><md-button ng-if="menuOption.position == \'header\'" ng-click=vm.sendCustomMenuOption(menuOption.id) style="margin-right: 10px;" class="cursor-hand md-icon-button"><img draggable=false ng-src="{{menuOption.imagePath ? menuOption.imagePath : \'/controlpanel/static/images/dashboards/icon_button_controls.svg\'}}"><md-tooltip>{{menuOption.description}}</md-tooltip></md-button></div><md-button ng-if=vm.showFiltersInBody() ng-click="vm.toggleRight(vm.element.id+\'rightSidenav\')" style="margin-right: 10px;" class="cursor-hand md-icon-button"><img draggable=false ng-src={{vm.baseimg}}/static/images/dashboards/icon_filter.svg><md-tooltip>Filter</md-tooltip></md-button><md-button ng-if=vm.showfiltersInModal() ng-click=vm.openFilterDialog() style="margin-right: 10px;" class="cursor-hand md-icon-button"><img draggable=false ng-src={{vm.baseimg}}/static/images/dashboards/icon_filter.svg><md-tooltip>Filter</md-tooltip></md-button><md-button ng-if=vm.editmode style="margin: 0px 10px 0px 0px;" class="drag-handler md-icon-button"><img draggable=false ng-src={{vm.baseimg}}/static/images/dashboards/Icon_move.svg><md-tooltip>Move</md-tooltip></md-button><div flex=nogrow layout-align="center right" ng-if="vm.editmode || !vm.element.notshowDotsMenu"><md-menu-bar><md-menu md-position-mode="target-right bottom" md-offset="-4 0"><button ng-click=$mdMenu.open() style="padding: 0px"><img ng-src={{vm.baseimg}}/static/images/dashboards/more.svg><md-tooltip>Options</md-tooltip></button><md-menu-content width=5><md-menu-item><md-button ng-click=vm.toggleFullScreen() aria-label=Fullscreen><img ng-src={{vm.baseimg}}/static/images/dashboards/Icon_full.svg> <span>Fullscreen</span></md-button></md-menu-item><md-menu-item ng-if="(!vm.iframe || vm.iframe && vm.editbuttonsiframe.filterGadgetMenu) && vm.editmode && vm.element.type != \'html5\'"><md-button ng-click=vm.openEditFilterDialog() aria-label="Edit Filter"><img ng-src={{vm.baseimg}}/static/images/dashboards/icon_menu_filter.svg> <span>Edit Filters</span></md-button></md-menu-item><md-menu-item ng-if="!vm.iframe && vm.editmode && vm.element.type === \'livehtml\'"><md-button ng-click=vm.openEditCustomMenuOptionsDialog() aria-label="Custom Menu Options"><img ng-src={{vm.baseimg}}/static/images/dashboards/icon_button_menu.svg style=height:20px;> <span>Custom Menu Options</span></md-button></md-menu-item><md-menu-item ng-if=vm.showfavoritesg><md-button ng-click=vm.addFavoriteDialog() aria-label="Add to Favorites"><img ng-src={{vm.baseimg}}/static/images/dashboards/star-default.svg style="height:20px;color: #060E14;"> <span>Add to Favorites</span></md-button></md-menu-item><md-menu-item ng-if=vm.editmode><md-button ng-click=vm.openEditContainerDialog() aria-label="Edit Container"><img ng-src={{vm.baseimg}}/static/images/dashboards/style.svg> <span>Styling</span></md-button></md-menu-item><md-menu-item ng-if="vm.editmode && ((!vm.iframe || (vm.iframe &&  vm.editbuttonsiframe.editGadgetMenu) )|| vm.eventedit) && (vm.element.type == \'livehtml\' ||  vm.element.type == \'vuetemplate\' ||  vm.element.type == \'reacttemplate\')"><md-button ng-if="vm.element.template == null" ng-click=vm.openEditGadgetDialog() aria-label="Gadget Editor"><img ng-src={{vm.baseimg}}/static/images/dashboards/edit.svg> <span>Edit</span></md-button><md-button ng-if="vm.element.template != null" ng-click=vm.openEditTemplateParamsDialog() aria-label="Gadget Editor"><img ng-src={{vm.baseimg}}/static/images/dashboards/edit.svg> <span>Edit</span></md-button></md-menu-item><md-menu-item ng-if="vm.editmode && ((!vm.iframe || (vm.iframe &&  vm.editbuttonsiframe.editGadgetMenu) ) || vm.eventedit) &&  (vm.element.type == \'html5\' )"><md-button ng-click=vm.openEditGadgetHTML5Dialog() aria-label="Gadget Editor"><img ng-src={{vm.baseimg}}/static/images/dashboards/edit.svg> <span>Edit</span></md-button></md-menu-item><md-menu-item ng-if="vm.editmode && ((!vm.iframe || (vm.iframe &&  vm.editbuttonsiframe.editGadgetMenu) ) || vm.eventedit) && (vm.element.type != \'livehtml\' && vm.element.type != \'html5\'  && vm.element.type != \'gadgetfilter\'  && vm.element.type != \'vuetemplate\'  && vm.element.type != \'reacttemplate\')"><md-button ng-click=vm.openEditGadgetIframe() aria-label="Edit Container"><img ng-src={{vm.baseimg}}/static/images/dashboards/edit.svg> <span>Edit</span></md-button></md-menu-item><md-menu-item ng-if=vm.editmode><md-button ng-click=vm.deleteElement()><img ng-src={{vm.baseimg}}/static/images/dashboards/delete.svg> <span>Remove</span></md-button></md-menu-item><div ng-repeat="menuOption in vm.element.customMenuOptions"><md-menu-item ng-if="menuOption.position == \'menu\'"><md-button ng-click=vm.sendCustomMenuOption(menuOption.id)><img ng-src="{{menuOption.imagePath ? menuOption.imagePath : \'/controlpanel/static/images/dashboards/icon_button_controls.svg\'}}" style=height:20px;> <span>{{menuOption.description}}</span></md-button></md-menu-item></div></md-menu-content></md-menu></md-menu-bar></div></div><div layout=row layout-wrap layout-align="end start" ng-if="(vm.element.hideBadges === undefined || vm.element.hideBadges === false) && vm.element.type != \'gadgetfilter\'"><div ng-class=vm.elemntbadgesclass() ng-repeat=" data in vm.datastatus" style="margin-top: 5px; text-align: left; z-index:1"><div class=filter flex=20><span class=badges-filters title="{{data.name}} {{data.op}} {{data.value}}">{{data.name}} <span style="margin-left: 10px;margin-right: 2px" ng-click=vm.deleteFilter(data.id,data.field,data.op)>X</span></span></div></div></div><md-sidenav style="min-width: 50px !important;    width: 100% !important;    max-width: 257px !important;" ng-if="(vm.element.filtersInModal === undefined || vm.element.filtersInModal === false) && vm.element.type != \'gadgetfilter\' " class=md-sidenav-right md-component-id={{vm.element.id}}rightSidenav md-disable-backdrop="" md-whiteframe=4><md-content style="padding: 24px"><div layout=row layout-align="end start"><button type=button aria-label=Close style="background: 0 0;border: none; outline: 0; cursor: pointer;" ng-click="vm.toggleRight(vm.element.id+\'rightSidenav\')"><span style="font-size: 16px !important;" class="ods-dialog__close ods-icon ods-icon-close"></span></button></div><div id=_{{vm.element.id}}filters><filter id=vm.element.id datasource=vm.element.datasource config=vm.config hidebuttonclear=vm.element.hidebuttonclear buttonbig=false></filter></div></md-content></md-sidenav><livehtml ng-style="{\'background-color\':vm.element.backgroundColor, \'padding\': vm.element.padding + \'px\'}" ng-if="(vm.element.type == \'livehtml\' && (!vm.element.subtype || vm.element.subtype.startsWith(\'angularJS\'))) || vm.element.type == \'gadgetfilter\'" livecontent=vm.element.content filters=vm.config livecontentcode=vm.element.contentcode datasource=vm.element.datasource custommenuoptions=vm.element.customMenuOptions ng-class=vm.elemntbodyclass() id=vm.element.id datastatus=vm.datastatus showonlyfiltered=vm.element.showOnlyFiltered template=vm.element.template params=vm.element.params></livehtml><vuetemplate ng-style="{\'background-color\':vm.element.backgroundColor, \'padding\': vm.element.padding + \'px\'}" ng-if="vm.element.type == \'livehtml\' && vm.element.subtype.startsWith(\'vueJS\')" livecontent=vm.element.content filters=vm.config livecontentcode=vm.element.contentcode datasource=vm.element.datasource custommenuoptions=vm.element.customMenuOptions ng-class=vm.elemntbodyclass() id=vm.element.id datastatus=vm.datastatus showonlyfiltered=vm.element.showOnlyFiltered template=vm.element.template params=vm.element.params></vuetemplate><reacttemplate ng-style="{\'background-color\':vm.element.backgroundColor, \'padding\': vm.element.padding + \'px\'}" ng-if="vm.element.type == \'livehtml\' && vm.element.subtype.startsWith(\'reactJS\')" livecontent=vm.element.content filters=vm.config livecontentcode=vm.element.contentcode datasource=vm.element.datasource custommenuoptions=vm.element.customMenuOptions ng-class=vm.elemntbodyclass() id=vm.element.id datastatus=vm.datastatus showonlyfiltered=vm.element.showOnlyFiltered template=vm.element.template params=vm.element.params></reacttemplate><gadget ng-style="{\'background-color\':vm.element.backgroundColor, \'padding\': vm.element.padding + \'px\', \'display\': \'inline-block\', \'width\': \'calc(100% - 40px)\', \'position\': \'absolute\',\'top\': \'50%\',\'left\': \'50%\',\'transform\': \'translate(-50%, -50%)\'}" ng-if="vm.element.type != \'livehtml\'&& vm.element.type != \'html5\' && vm.element.type != \'gadgetfilter\' && vm.element.type != \'datadiscovery\'" ng-class=vm.elemntbodyclass() id=vm.element.id datastatus=vm.datastatus filters=vm.config></gadget><html5 ng-style="{\'background-color\':vm.element.backgroundColor, \'padding\': vm.element.padding + \'px\'}" ng-if="vm.element.type == \'html5\'" livecontent=vm.element.content datasource=vm.element.datasource ng-class=vm.elemntbodyclass() id=vm.element.id></html5><datadiscovery ng-style="{\'background-color\':vm.element.backgroundColor, \'padding\': vm.element.padding + \'px\', \'display\': \'inline-block\', \'width\': \'calc(100% - 40px)\', \'position\': \'absolute\',\'top\': \'50%\',\'left\': \'50%\',\'transform\': \'translate(-50%, -50%)\'}" ng-if="vm.element.type === \'datadiscovery\'" ng-class=vm.elemntbodyclass() id=vm.element.id datastatus=vm.datastatus filters=vm.config></datadiscovery><md-content ng-style="{\'background-color\':vm.element.backgroundColor, \'padding\':\'0px 22px 22px 22px\', \'height\': \'calc(100% - \'+ (vm.element.header.height+22) + \'px)\'}" ng-if="vm.element.type == \'gadgetfilter\' && vm.element.header.enable"><div id=__{{vm.element.id}}filters class=ovfl><filter id=vm.element.id datasource=vm.element.datasource config=vm.config hidebuttonclear=vm.element.hidebuttonclear buttonbig=false></filter></div></md-content><md-content ng-style="{\'background-color\':vm.element.backgroundColor, \'padding\':\'0px 22px 22px 22px\', \'height\': \'calc(100% - 22px)\'}" ng-if="vm.element.type == \'gadgetfilter\' && !vm.element.header.enable"><div id=__{{vm.element.id}}filters class=ovfl><filter id=vm.element.id datasource=vm.element.datasource config=vm.config hidebuttonclear=vm.element.hidebuttonclear buttonbig=false></filter></div></md-content></div></gridster-item>');
$templateCache.put('app/components/view/gadgetComponent/gadget.html','<div class=spinner-margin-top ng-if="vm.type == \'loading\'" layout=row layout-sm=column layout-align=space-around><div class=sk-chase><div class=sk-chase-dot></div><div class=sk-chase-dot></div><div class=sk-chase-dot></div><div class=sk-chase-dot></div><div class=sk-chase-dot></div><div class=sk-chase-dot></div></div></div><div class=spinner-overlay ng-if="vm.status == \'pending\'" layout=row layout-sm=column layout-align=space-around><md-progress-linear md-mode=indeterminate></md-progress-linear></div><div ng-if="vm.type == \'nodata\' || vm.showNoData  " class=no-data-gadget layout=column><div class=no-data-title>NO DATA</div><div class=no-data-text>Sorry, we couldn\xB4t load the visual information for this gadget. Try again.</div></div><div ng-if="vm.type == \'removed\' || vm.showNoData  " class=no-data-gadget layout=column><div class=no-data-title>NO DATA</div><div class=no-data-text>Sorry, we couldn\xB4t load the visual information for this gadget. This gadget was removed.</div></div><canvas ng-if="vm.type == \'line\'" chart-dataset-override=vm.datasetOverride chart-click=vm.clickChartEventProcessorEmitter class="chart chart-line" chart-data=vm.data chart-labels=vm.labels chart-series=vm.series chart-options=vm.optionsChart></canvas><canvas ng-if="vm.type == \'mixed\'" chart-dataset-override=vm.datasetOverride chart-click=vm.clickChartEventProcessorEmitter class=chart-bar chart-data=vm.data chart-labels=vm.labels chart-series=vm.series chart-options=vm.optionsChart></canvas><canvas ng-if="vm.type == \'bar\'" chart-dataset-override=vm.datasetOverride chart-click=vm.clickChartEventProcessorEmitter class="chart chart-bar" chart-data=vm.data chart-labels=vm.labels chart-series=vm.series chart-options=vm.optionsChart></canvas><canvas ng-if="vm.type == \'pie\' && vm.classPie()" chart-click=vm.clickChartEventProcessorEmitter class="chart chart-pie" chart-data=vm.data chart-labels=vm.labels chart-options=vm.optionsChart chart-colors=vm.swatches.global></canvas><canvas ng-if="vm.type == \'pie\' && !vm.classPie()" chart-click=vm.clickChartEventProcessorEmitter class="chart chart-doughnut" chart-data=vm.data chart-labels=vm.labels chart-options=vm.optionsChart chart-colors=vm.swatches.global></canvas><canvas ng-if="vm.type == \'radar\'" chart-dataset-override=vm.datasetOverride chart-click=vm.clickChartEventProcessorEmitter class="chart chart-radar" chart-data=vm.data chart-labels=vm.labels chart-series=vm.series chart-options=vm.optionsChart></canvas><word-cloud ng-if="vm.type == \'wordcloud\'" words=vm.words on-click=vm.clickWordCloudEventProcessorEmitter width=vm.width height=vm.height padding=0 use-tooltip=false use-transition=true></word-cloud><leaflet id="{{\'lmap\' + vm.id}}" ng-if="vm.type == \'map\'" lf-center=vm.center markers=vm.markers height={{vm.height}} width=100%></leaflet><md-table-container ng-style="{\'height\': \'calc(100% - \'+{{vm.config.config.tablePagination.style.trHeightFooter}}+\'px\'+\')\'}" ng-if="vm.type == \'table\'"><table md-table md-progress=promise md-row-select=vm.config.config.tablePagination.options.rowSelection ng-model=vm.selected class="table-light table-hover"><thead md-head ng-if=!vm.config.config.tablePagination.options.decapitate ng-style="{\'background-color\':vm.config.config.tablePagination.style.backGroundTHead}" md-order=vm.config.config.tablePagination.order><tr md-row ng-style="{\'height\':vm.config.config.tablePagination.style.trHeightHead}"><th ng-if=vm.showCheck[$index] ng-style="{\'color\':vm.config.config.tablePagination.style.textColorTHead}" md-column ng-repeat="measure in vm.measures" md-order-by={{measure.config.order}}><span>{{measure.config.name | translate}}</span></th></tr></thead><tbody md-body><tr md-row md-auto-select=true md-on-select=vm.selectItemTable md-select=dat ng-style="{\'height\':vm.config.config.tablePagination.style.trHeightBody}" ng-repeat="dat in vm.data | orderBy: vm.getValueOrder(vm.config.config.tablePagination.order) : vm.config.config.tablePagination.order.charAt(0) === \'-\' |  limitTo: vm.config.config.tablePagination.limit : (vm.config.config.tablePagination.page -1) * vm.config.config.tablePagination.limit"><td ng-if=vm.showCheck[$index] ng-style="{\'color\':vm.config.config.tablePagination.style.textColorBody}" md-cell ng-repeat="value in dat">{{value}}</td></tr></tbody></table></md-table-container><div ng-if="vm.type == \'table\'" class="md-table-toolbar md-default" style="min-height: 30px;height: 30px; position: absolute;"><div class=md-toolbar-tools><md-button class=md-icon-button ng-click=vm.toggleDecapite()><md-icon style="color: #ACACAC;  font-size: 18px;">calendar_view_day</md-icon></md-button><md-menu md-position-mode="target-left bottom"><md-button class=md-icon-button ng-click=$mdMenu.open() style="margin-right: 12px;"><md-icon style="color: #ACACAC;  font-size: 18px; margin-right: 8px">visibility</md-icon></md-button><md-menu-content width=2><md-menu-item ng-repeat="measure in vm.measures"><md-checkbox class=blue ng-model=vm.showCheck[$index] ng-checked=true>{{measure.config.name | translate}}</md-checkbox></md-menu-item></md-menu-content></md-menu></div></div><md-table-pagination ng-if="vm.type == \'table\'" md-limit=vm.config.config.tablePagination.limit md-limit-options="vm.notSmall ? vm.config.config.tablePagination.limitOptions : undefined" md-page=vm.config.config.tablePagination.page md-total={{vm.data.length}} md-page-select="vm.config.config.tablePagination.options.pageSelect && vm.notSmall" md-boundary-links="vm.config.config.tablePagination.options.boundaryLinks && vm.notSmall" ng-style="{\'background-color\':vm.config.config.tablePagination.style.backGroundTFooter,\'height\':vm.config.config.tablePagination.style.trHeightFooter, \'color\':vm.config.config.tablePagination.style.textColorFooter}"></md-table-pagination>');
$templateCache.put('app/components/view/filterComponent/filter.html','<div ng-repeat="(index,item) in vm.tempConfig" id={{vm.tempConfig[index].htmlId}}><div ng-class="{\'ng-hide\': vm.tempConfig[index].hide}"><textfilter ng-if="item.type == \'textfilter\'  " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></textfilter><numberfilter ng-if="item.type == \'numberfilter\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></numberfilter><intervaldatefilter ng-if="item.type == \'intervaldatefilter\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></intervaldatefilter><intervaldatestringfilter ng-if="item.type == \'intervaldatestringfilter\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></intervaldatestringfilter><activaterefreshaction ng-if="item.type == \'activaterefreshaction\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></activaterefreshaction><livefilter ng-if="item.type == \'livefilter\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></livefilter><simpleselectfilter ng-if="item.type == \'simpleselectfilter\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></simpleselectfilter><simpleselectnumberfilter ng-if="item.type == \'simpleselectnumberfilter\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></simpleselectnumberfilter><simpleselectdsfilter ng-if="item.type == \'simpleselectdsfilter\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></simpleselectdsfilter><simpleselectnumberdsfilter ng-if="item.type == \'simpleselectnumberdsfilter\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></simpleselectnumberdsfilter><multiselectfilter ng-if="item.type == \'multiselectfilter\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></multiselectfilter><multiselectnumberfilter ng-if="item.type == \'multiselectnumberfilter\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></multiselectnumberfilter><multiselectdsfilter ng-if="item.type == \'multiselectdsfilter\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></multiselectdsfilter><multiselectnumberdsfilter ng-if="item.type == \'multiselectnumberdsfilter\' " idfilter=vm.tempConfig[index].id resultfilter=vm.resultFilters[index] datasource=vm.datasource config=vm.tempConfig[index]></multiselectnumberdsfilter></div></div><md-button ng-class="vm.buttonbig ? \'ok-button\' : \'ok-button-small\'" ng-click=vm.sendFilters()>OK</md-button><md-button ng-class="vm.buttonbig ? \'ok-button\' : \'ok-button-small\'" ng-if="vm.hidebuttonclear === undefined || vm.hidebuttonclear === false" ng-click=vm.cleanFilters()>CLEAN FILTERS</md-button>');
$templateCache.put('app/components/view/pageComponent/page.html','<div class=page-dashboard-container ng-style="{\'background-image\':\'url(\' + vm.page.background.filedata + \')\',\'background-color\': vm.page.background.color }"><synoptic ng-if="vm.synopticedit.showSynoptic && !vm.synopticedit.showEditor" style="position: absolute; z-index:1;" synoptic=vm.synoptic backgroundcolorstyle=vm.page.background.color></synoptic><span ng-repeat="layer in vm.page.layers"><gridster ng-style=vm.pageStyle() ng-if="(vm.synopticedit.showSynoptic && !vm.synopticedit.showEditor && (vm.page.combinelayers || vm.page.selectedlayer == $index))||(!vm.synopticedit.showSynoptic && (vm.page.combinelayers || vm.page.selectedlayer == $index)) " options=vm.gridoptions class=flex><element ng-style="{\'z-index\':$parent.$index*500+1}" ng-if=item.id id={{item.id}} idtemplate={{item.idtemplate}} iframe=vm.iframe editbuttonsiframe=vm.editbuttonsiframe element=item editmode=vm.editmode showfavoritesg=vm.showfavoritesg eventedit=vm.gridoptions.eventedit ng-repeat="item in layer.gridboard"></element></gridster></span></div>');
$templateCache.put('app/components/view/html5Component/html5.html','<iframe ng-attr-id="{{vm.id + \'_html5\'}}" style="height: 100%; width: 100%; padding: 0; margin: 0;" frameborder=0></iframe>');
$templateCache.put('app/components/view/synopticComponent/synoptic.html','<div id=synopticbody></div>');
$templateCache.put('app/components/view/synopticEditorComponent/synopticEditor.html','<iframe id=synoptic_editor ng-style="vm.dashboardheader.enable && {\'height\': \'calc(100% - \'+{{vm.dashboardheader.height }}+\'px\'+\')\',\'position\': \'absolute\',\'z-index\':vm.config.zindexEditor,\'border-style\': \'none\'} || {\'height\': \'100%\',\'position\': \'absolute\',\'z-index\':vm.config.zindexEditor,\'border-style\': \'none\'}" src=/controlpanel/static/svg/editor/svg-editor.html width=100% onload=initsvgImage();></iframe>');
$templateCache.put('app/components/view/elementFullScreenComponent/elementFullScreen.html','<gridster options=vm.gridoptions class=flex><element id={{vm.element.id}} idtemplate={{vm.element.idtemplate}} iframe=vm.iframe element=vm.element editmode=vm.editmode></element></gridster>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/activaterefreshaction.html','<ods-form v-model=dynamicValidateForm><ods-form-item :label=dynamicValidateForm.inputName><ods-switch v-model=dynamicValidateForm.inputValue active-value=start inactive-value=stop @change=valueChange></ods-switch></ods-form-item></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/intervaldatefilter.html','<ods-form v-model=dynamicValidateForm><label class=ods-form-item__label style="display: block;   width: 100%;  text-align: left;">{{dynamicValidateForm.inputName}}</label><ods-date-picker v-model=dynamicValidateForm.intervalDates size=deci type=datetimerange range-separator=To start-placeholder="Start date" @change=dateChange end-placeholder="End date"></ods-date-picker></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/intervaldatestringfilter.html','<ods-form v-model=dynamicValidateForm><label class=ods-form-item__label style="display: block;   width: 100%;  text-align: left;">{{dynamicValidateForm.inputName}}</label><ods-date-picker v-model=dynamicValidateForm.intervalDates size=deci type=datetimerange range-separator=To start-placeholder="Start date" @change=dateChange end-placeholder="End date"></ods-date-picker></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/livefilter.html','<ods-form v-model=dynamicValidateForm><div class=default-form><ods-row><ods-col><span class=ods-form-item__label>REALTIME</span><ods-switch v-model=dynamicValidateForm.realtime active-value=start inactive-value=stop style="padding-top: 10px;\n          padding-left: 10px;" @change=realTimeChange></ods-switch></ods-col></ods-row><ods-row style="height: 42px;"><ods-col v-if="dynamicValidateForm.realtime==\'stop\'"><ods-form-item><span class=ods-form-item__label>FROM</span><ods-date-picker v-model=dynamicValidateForm.intervalDates type=datetimerange range-separator=To start-placeholder="Start date" @change=dateChange end-placeholder="End date"></ods-date-picker></ods-form-item></ods-col><ods-col v-else><ods-form-item><ods-select v-model=dynamicValidateForm.selectedPeriod placeholder=Select @change=periodChange><ods-option v-for="item in dynamicValidateForm.options" :key=item.value :label=item.label :value=item.value></ods-option></ods-select></ods-form-item></ods-col></ods-row></div></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/multiselectdsfilter.html','<ods-form v-model=dynamicValidateForm><form v-on:submit.prevent=noop><ods-form-item :label=dynamicValidateForm.inputName><ods-select v-model=dynamicValidateForm.optionsSelected multiple collapse-tags placeholder=Select @change=valueChange><ods-option v-for="item in dynamicValidateForm.options" :key=item.value :label=item.label :value=item.value></ods-option></ods-select></ods-form-item></form></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/multiselectfilter.html','<ods-form v-model=dynamicValidateForm><form v-on:submit.prevent=noop><ods-form-item :label=dynamicValidateForm.inputName><ods-select v-model=dynamicValidateForm.optionsSelected multiple collapse-tags placeholder=Select @change=valueChange><ods-option v-for="item in dynamicValidateForm.options" :key=item.value :label=item.label :value=item.value></ods-option></ods-select></ods-form-item></form></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/multiselectnumberdsfilter.html','<ods-form v-model=dynamicValidateForm><form v-on:submit.prevent=noop><ods-form-item :label=dynamicValidateForm.inputName><ods-select v-model=dynamicValidateForm.optionsSelected multiple collapse-tags placeholder=Select @change=valueChange><ods-option v-for="item in dynamicValidateForm.options" :key=item.value :label=item.label :value=item.value></ods-option></ods-select></ods-form-item></form></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/multiselectnumberfilter.html','<ods-form v-model=dynamicValidateForm><form v-on:submit.prevent=noop><ods-form-item :label=dynamicValidateForm.inputName><ods-select v-model=dynamicValidateForm.optionsSelected multiple collapse-tags placeholder=Select @change=valueChange><ods-option v-for="item in dynamicValidateForm.options" :key=item.value :label=item.label :value=item.value></ods-option></ods-select></ods-form-item></form></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/numberfilter.html','<ods-form v-model=dynamicValidateForm v-on:submit.prevent=noop><form v-on:submit.prevent=noop><ods-form-item :label=dynamicValidateForm.inputName><ods-input type=number placeholder="Please input" v-model=dynamicValidateForm.inputValue @change=valueChange></ods-input></ods-form-item></form></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/simpleselectdsfilter.html','<ods-form v-model=dynamicValidateForm><form v-on:submit.prevent=noop><ods-form-item :label=dynamicValidateForm.inputName><ods-select v-model=dynamicValidateForm.optionsSelected collapse-tags clearable placeholder=Select @change=valueChange><ods-option v-for="item in dynamicValidateForm.options" :key=item.value :label=item.label :value=item.value></ods-option></ods-select></ods-form-item></form></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/simpleselectfilter.html','<ods-form v-model=dynamicValidateForm><form v-on:submit.prevent=noop><ods-form-item :label=dynamicValidateForm.inputName><ods-select v-model=dynamicValidateForm.optionsSelected collapse-tags clearable placeholder=Select @change=valueChange><ods-option v-for="item in dynamicValidateForm.options" :key=item :label=item :value=item></ods-option></ods-select></ods-form-item></form></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/simpleselectnumberdsfilter.html','<ods-form v-model=dynamicValidateForm><form v-on:submit.prevent=noop><ods-form-item :label=dynamicValidateForm.inputName><ods-select v-model=dynamicValidateForm.optionsSelected collapse-tags clearable placeholder=Select @change=valueChange><ods-option v-for="item in dynamicValidateForm.options" :key=item.value :label=item.label :value=item.value></ods-option></ods-select></ods-form-item></form></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/simpleselectnumberfilter.html','<ods-form v-model=dynamicValidateForm><form v-on:submit.prevent=noop><ods-form-item :label=dynamicValidateForm.inputName><ods-select v-model=dynamicValidateForm.optionsSelected collapse-tags clearable placeholder=Select @change=valueChange><ods-option v-for="item in dynamicValidateForm.options" :key=item :label=item :value=item></ods-option></ods-select></ods-form-item></form></ods-form>');
$templateCache.put('app/components/view/filterComponent/filtersComponents/textfilter.html','<ods-form v-model=dynamicValidateForm v-on:submit.prevent=noop><form v-on:submit.prevent=noop><ods-form-item :label=dynamicValidateForm.inputName><ods-input type=text placeholder="Please input" v-model=dynamicValidateForm.inputValue @change=valueChange></ods-input></ods-form-item></form></ods-form>');
$templateCache.put('app/components/view/templateComponent/reactTemplateComponent/reacttemplate.html','<div class=rootapp></div><div class=styles></div>');
$templateCache.put('app/components/view/templateComponent/liveHTMLComponent/livehtml.html','<div id=testhtml></div>');
$templateCache.put('app/components/view/templateComponent/vueTemplateComponent/vuetemplate.html','<div id=testhtml></div>');
$templateCache.put('app/components/view/datadiscoveryComponent/datadiscoveryComponents/datadiscoveryDataDraw.html','<div style=overflow:auto;height:100%;width:100%><div ng-hide="vm.status != \'ready\'" class=container></div><div ng-hide="vm.status != \'error\'" class=container>{{vm.error}}</div><div class=spinner-overlay ng-if="vm.status == \'pending\'" layout=row layout-sm=column layout-align=space-around style=overflow:hidden;position:relative><md-progress-linear md-mode=indeterminate></md-progress-linear></div></div>');
$templateCache.put('app/components/view/datadiscoveryComponent/datadiscoveryComponents/datadiscoveryFieldPicker.html','<md-content style=height:100% layout=column><md-subheader flex=5 layout=row><md-button aria-label="Reload Fields" ng-click=vm.reloadFields() class="md-icon-button md-primary"><md-icon>replay</md-icon></md-button><label>{{vm.datasource.identification}}</label></md-subheader><hr><md-subheader class=md-secondary flex=5><md-icon class=md-secondary>view_list</md-icon><span>Attributes</span></md-subheader><ul flex class="fieldPicker attrPicker" data-as-sortable=vm.dragPickerControl data-ng-model=vm.fields style=padding-left:0px;overflow-y:auto;overflow-x:hidden><li data-as-sortable-item data-as-sortable-item-handle data-ng-repeat="field in vm.fields" ng-click=null class="datacolumn pickercolumn"><md-tooltip>{{field.field}}</md-tooltip><label data-as-sortable-item-handle><md-icon ng-if="field.type==\'string\'">line_weight</md-icon><md-icon ng-if="field.type==\'integer\' || field.type==\'number\'">score</md-icon><md-icon ng-if="field.type==\'boolean\'">exposure</md-icon>{{ field.field }}</label><md-button aria-label="Clear Metric" ng-click=vm.removeAttr($index) class="md-icon-button md-accent pull-right"><md-icon>clear</md-icon></md-button></li></ul><md-subheader class=md-primary flex=5><md-icon class=md-primary>insert_chart_outlined</md-icon><span>Metrics</span></md-subheader><ul flex class="fieldPicker metricPicker" data-as-sortable=vm.dragPickerControl data-ng-model=vm.metrics style=padding-left:0px;overflow-y:auto;overflow-x:hidden><li data-as-sortable-item data-as-sortable-item-handle data-ng-repeat="field in vm.metrics" ng-click=null class="datacolumn pickercolumn"><md-tooltip>{{field.field + \': \' + field.formula}}</md-tooltip><label titledata-as-sortable-item-handle><md-icon>functions</md-icon>{{ field.field }}</label><md-button aria-label="Clear Metric" ng-click=vm.removeMetric($index) class="md-icon-button md-accent pull-right"><md-icon>clear</md-icon></md-button><md-button aria-label="Edit Metric" ng-click=vm.metricDialog($index) class="md-icon-button md-primary pull-right"><md-icon>edit</md-icon></md-button></li></ul><md-button ng-click=vm.metricDialog() flex=5 style=padding-left:0px class="addmetric md-raised"><md-icon>add</md-icon></md-button></md-content>');
$templateCache.put('app/components/view/datadiscoveryComponent/datadiscoveryComponents/datadiscoveryFieldSelector.html','<md-content><div ng-on-drop=vm.onDrop() class="sortable-row columnSelector" as-sortable=vm.dragSelectControl data-ng-model=vm.columns.list><div ng-repeat="field in vm.columns.list" as-sortable-item><div as-sortable-item-handle><span ng-if="field.asc === undefined" ng-init=vm.onDrop(field)></span><md-tooltip ng-if="field.type==\'metric\'">{{field.formula}}</md-tooltip><label data-as-sortable-item-handle><md-icon ng-if="field.type==\'string\'">line_weight</md-icon><md-icon ng-if="field.type==\'integer\' || field.type==\'number\'">score</md-icon><md-icon ng-if="field.type==\'boolean\'">exposure</md-icon><md-icon ng-if="field.type==\'metric\'">functions</md-icon>{{ field.field }}</label><md-button ng-if="field.type !== \'metric\' && vm.columns.subtotalEnable" aria-label="Enable subtotals" class=md-icon-button ng-click=vm.toggleSubtotalField($index);vm.refreshModel() class=pull-right><md-icon ng-class="{\'md-primary\': vm.columns.subtotalFields.indexOf($index) !== -1}">notes</md-icon></md-button><md-button aria-label="Edit Dynamic Style" class=md-icon-button ng-click=vm.openColumnStyleDialog($index) class=pull-right><md-icon>brush</md-icon></md-button><md-button aria-label="Change sort" class=md-icon-button ng-click="(field.asc == true?field.asc = false:(field.asc == null?field.asc = true:field.asc = null));vm.columns.subtotalEnable=field.asc!=null;vm.refreshModel()" class=pull-right><md-icon ng-if="field.asc === null">block</md-icon><md-icon ng-if="field.asc == true " ng-click="field.asc = false">arrow_downward</md-icon><md-icon ng-if="field.asc == false" ng-click="field.asc = null">arrow_upward</md-icon></md-button><md-button aria-label=Clear ng-click=vm.removeColumn($index) class="md-icon-button md-accent"><md-icon>clear</md-icon></md-button></div></div></div></md-content>');}]);