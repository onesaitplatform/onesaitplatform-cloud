(function () {
  'use strict';

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
               vm.type='removed';
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
