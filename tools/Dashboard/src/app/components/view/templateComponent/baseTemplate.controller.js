(function () {
  'use strict';

  angular.module('dashboardFramework')
    .controller(
      'baseTemplateController',
      function($rootScope, $scope, $element, $mdCompiler, datasourceSolverService, httpService, interactionService, utilsService, urlParamService, filterService, $translate, $window, cacheBoard){

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
    
    
    
      }
    )
  
})();