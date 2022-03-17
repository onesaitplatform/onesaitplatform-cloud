(function () {
  'use strict';

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
