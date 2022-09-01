(function () {
  'use strict';

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
    //vm.dashboard = $rootScope.dashboard ? $rootScope.dashboard : "";

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
      vm.dashboard = $rootScope.dashboard ? $rootScope.dashboard : "";
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
