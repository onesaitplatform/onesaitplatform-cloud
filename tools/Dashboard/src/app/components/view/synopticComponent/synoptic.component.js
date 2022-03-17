(function () {
    'use strict';

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