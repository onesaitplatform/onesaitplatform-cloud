(function () {
  'use strict';

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
        vm.configtype = vm.config.type;
        if(typeof vm.config.type.id !== 'undefined' && vm.config.type.id !== null){
          vm.configtype =vm.config.type.id;
        }

        vm.projects = [];
        for(var index=0; index < vm.measures.length; index++){
          var jsonConfig = (typeof vm.measures[index].config == "string"? JSON.parse(vm.measures[index].config):vm.measures[index].config);;
          for(var indexF = 0 ; indexF < jsonConfig.fields.length; indexF++){
            if(!utilsService.isSameJsonInArray( { op:"", field:jsonConfig.fields[indexF] },vm.projects)){
              vm.projects.push({op:"",field:jsonConfig.fields[indexF]});
            }
          }
           //add attribute for filter style marker to recover from datasource.
         if(vm.configtype=="map" && typeof vm.config.config.jsonMarkers!=undefined && vm.config.config.jsonMarkers!=null && vm.config.config.jsonMarkers.length>0){
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
          if(vm.configtype=="map" && typeof vm.config.config.jsonMarkers!=undefined && vm.config.config.jsonMarkers!=null && vm.config.config.jsonMarkers.length>0){
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
      //for bunglemode
      vm.configtype = vm.config.type;
      if(typeof vm.config.type.id !== 'undefined' && vm.config.type.id !== null){
        vm.configtype =vm.config.type.id;
      }

      switch(vm.configtype){
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

          if(vm.configtype == "pie"){
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
        if(vm.configtype == "pie"){

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
         

          if(vm.configtype==="line"||vm.configtype==="bar"){   
            
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
            if(m.config.config.type.id==='line'){
              return m.config.config;
            }else if(m.config.config.type.id==='bar'){
              return m.config.config;
            }else if(m.config.config.type.id==='points'){
              m.config.config.type.id= 'line';
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
      vm.type = vm.configtype;//Activate gadget
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
      var maxWordSize = width * 0.04;
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
      vm.configtype = vm.config.type;
      if(typeof vm.config.type.id !== 'undefined' && vm.config.type.id !== null){
        vm.configtype =vm.config.type.id;
      }

      if(typeof points[0]!=='undefined'){
        switch(vm.configtype){          
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
      var originValue = event.currentScope.vm.markers[args.modelName].id;
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
