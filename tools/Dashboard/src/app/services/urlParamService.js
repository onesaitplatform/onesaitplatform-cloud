(function () {
  'use strict';

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
