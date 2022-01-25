(function () {
  'use strict';

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
  function FilterController($scope,datasourceSolverService, utilsService) {
    var vm = this;
    
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    vm.options=[];
     
    $scope.getDataFromDataSource = function (datasource, callbackF, filters, group, project, sort, limit, offset, param, debug ) {
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
            var tmpv=utilsService.getInsensitiveProperty(dat[index],'filterCode'); 
            var tmpd=utilsService.getInsensitiveProperty(dat[index],'filterDes'); 
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
