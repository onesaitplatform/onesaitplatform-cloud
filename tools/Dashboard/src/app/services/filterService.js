(function () {
  'use strict';

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