(function () {
  'use strict';

  angular.module('dashboardFramework')
    .service('socketService', MockSocketService);

  /** @ngInject */
  function MockSocketService($http, $timeout, $log, __env) {
    var vm = this;

    vm.cachedData = null;

    vm.connect = function () {
      $http.get(window.__env.dashboardEngineMockDataPath).then(function (response) {
        vm.cachedData = response.data;
        $log.info("Mock Server Service Started");
      }).catch(function () {
        $log.warn("Mock Server Service Started, but data is empty")
      });
    }

    vm.connectAndSendAndSubscribe = function (reqrespList) {
      for (var reqrest in reqrespList) {
        var request = reqrespList[reqrest];
        request.callback(request.id,datasource.type,{"data":JSON.stringify(vm.getFromCache(datasource))});
      }
    };

    vm.sendAndSubscribe = function (datasource, ignoreQueue) {
      if(!vm.cachedData){
        var futureTry = function (){
          vm.sendAndSubscribe(datasource, ignoreQueue)
        }
        $timeout(futureTry,1000);
      }
      else{
        datasource.callback(datasource.id,datasource.type,{"startTime":new Date(),"data":JSON.stringify(vm.getFromCache(datasource))});
      }
    }


    vm.disconnect = function (reqrespList) {
      $log.info("Mock Server Service Stoped");
    }

    function _objectWithoutProperties(obj, keys) {
      var target = {};
      for (var i in obj) {
        if (keys.indexOf(i) >= 0) continue;
        if (!Object.prototype.hasOwnProperty.call(obj, i)) continue;
        target[i] = obj[i];
      }
      return target;
    }

    //provisional method, could be use hash key
    function generateDatasourceKey(datasource) {
      var keyobj = {
        id: datasource.id,
        msg: _objectWithoutProperties(datasource.msg, ["dashboard"]),
        type: datasource.type
      }
      return JSON.stringify(keyobj);
    }

    vm.getFromCache = function(datasource){//empty if not exist
      if(!__env.usesqltodatasource){
        var key = generateDatasourceKey(datasource);

        if(vm.cachedData.hasOwnProperty(key)){
          return vm.cachedData[key];
        }
        else{
          return [];
        }
      }
      else{
        return vm.executeAlasql(datasource);
      }
    }

    vm.executeAlasql = function(datasource){
      console.log("Mock alasql for: " + JSON.stringify(datasource));
      var alasQuery = vm.generateAlasql(datasource);
      console.log("Mock alasql: " + alasQuery);
      return alasql(alasQuery,[vm.cachedData[datasource.msg.ds]]);
    }

    vm.generateAlasql = function(datasource){
      var plainDs = datasource.msg;
      return buildQuery(" ? ", plainDs.limit,plainDs.filter,plainDs.project,plainDs.group,plainDs.sort,plainDs.offset);
    }



    function buildQuery(query, maxreg, where, project,	group, sort, offset) {
      var sb = "";
      sb+="select ";
      sb+=buildProject(project);
      sb+=" from ";
      sb+=query;
      sb+=buildWhere(where, "", true, "");
      sb+=buildGroup(group);
      sb+=buildHaving(where);
      sb+=buildSort(sort);
      if(maxreg && maxreg != -1){
        sb+=" limit ";
        sb+=maxreg;
      }
      if(offset && offset != -1){
        sb+=" offset ";
        sb+=offset;
      }
      return sb;
    }

	function buildProject(projections) {
		if (projections == null || projections.length == 0) {
			return "* ";
		} else {
			var sb = "";
			for (var p in projections) {
        p = projections[p];
        if(p.op){
          sb += p.op;
          sb+="(";
        }
        sb+=p.field.replace(".","->");
        if(p.op){
          sb+=")";
        }
        if(p.alias){
          sb+=" as " + p.alias;
        }
				sb+=",";
			}
			return sb.substring(0, sb.length - 1);
		}
  }
  
  function isHavingExp(exp) {
		var expaux = exp.toLowerCase().replace(" ", "");
		return (expaux.indexOf("sum(") != -1 || expaux.indexOf("max(") != -1 || expaux.indexOf("min(") != -1
				|| expaux.indexOf("avg(") != -1 || expaux.indexOf("count(") != -1);
	}


	function buildWhere(filters, prefix, includeWhere, realproject) {
		if (filters == null || filters.length == 0) {
			return "";
		} else {
			var sb = "";
			if (includeWhere)
				sb+=" where ";
			for (var f in filters) {
        f=filters[f];
        if(!isHavingExp(f.field)){
          sb+=prefix;
          sb+=f.field.replace(".","->");
          sb+=" ";
          sb+=f.op;
          sb+=" ";
          sb+=f.exp;
          sb+=" and ";
        }
			}
			return sb.substring(0, sb.length - 5);
		}
  }
  
  function buildHaving(filters, prefix, includeWhere) {
		if (filters == null || filters.length == 0) {
			return "";
		} else {
			var sb = "";
			if (includeWhere)
				sb+=" having ";
			for (var f in filters) {
        f=filters[f];
        if(isHavingExp(f.field)){
          sb+=prefix;
          sb+=f.field.replace(".","->");
          sb+=" ";
          sb+=f.op;
          sb+=" ";
          sb+=f.exp;
          sb+=" and ";
        }
			}
			return sb.substring(0, sb.length - 5);
		}
	}

	function buildGroup(groups) {
		if (groups == null || groups.length == 0) {
			return "";
		} else {
			var sb = "";
			sb+=" group by ";
			for (var g in groups) {
        g=groups[g];
				sb+=g.replace(".","->");
				sb+=",";
			}
			return sb.substring(0, sb.length - 1);
		}
  }
  
  function buildSort(sort) {
		if (sort == null || sort.length == 0) {
			return "";
		} else {
			var sb = "";
			sb+=" order by ";
			for (var s in sort) {
        s=sort[s];
        sb+=s.field.replace(".","->");
        if(s.asc){
          sb+=" asc ";
        }
        else{
          sb+=" desc ";
        }
				sb+=",";
			}
			return sb.substring(0, sb.length - 1);
		}
	}



  };
})();
