(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('page', {
      templateUrl: 'app/components/view/pageComponent/page.html',
      controller: PageController,
      controllerAs: 'vm',
      bindings:{
        page:"=",
        iframe:"=",
        editmode:"<",
        gridoptions:"<",
        dashboardheader:"<",
        synoptic: "=?",
        synopticedit: "<?",
        tabson:"<?",
        editbuttonsiframe:"<?",
        showfavoritesg:"<?"
      }
    });

  /** @ngInject */
  function PageController($log, $scope, $mdSidenav, $mdDialog, datasourceSolverService) {
    var vm = this;
    vm.$onInit = function () {

     var countgadgets = vm.page.layers[0].gridboard.filter(function(gadget){return typeof gadget.id != "undefined"});
      if(countgadgets.length === 0){
        window.dispatchEvent(new Event('resize'));
        window.postMessage("dashboardloaded", "*");
        if(window.self !== window.top){
          window.parent.postMessage("dashboardloaded", "*");
        }
      }
    };

    vm.$postLink = function(){

    }

    vm.$onDestroy = function(){   
    }
   
    function eventStop(item, itemComponent, event) {
      $log.info('eventStop', item, itemComponent, event);
    }

    function itemChange(item, itemComponent) {
      $log.info('itemChanged', item, itemComponent);
    }
 
    function itemResize(item, itemComponent) {
    
      $log.info('itemResized', item, itemComponent);
    }

    function itemInit(item, itemComponent) {
      $log.info('itemInitialized', item, itemComponent);
    }

    function itemRemoved(item, itemComponent) {
      $log.info('itemRemoved', item, itemComponent);
    }

    function gridInit(grid) {
      $log.info('gridInit', grid);
    }

    function gridDestroy(grid) {
      $log.info('gridDestroy', grid);
    }

    vm.prevent = function (event) {
      event.stopPropagation();
      event.preventDefault();
    };

    vm.pageStyle = function(){
      var  temp ;
      try {
        if(typeof  $('#synopticbody > svg')[0].height.baseVal.value !=='undefined' && 
        typeof  $('#synopticbody > svg')[0].width.baseVal.value !=='undefined'){
          return   {'height': $('#synopticbody > svg')[0].height.baseVal.value+'px' , 'width': $('#synopticbody > svg')[0].width.baseVal.value+'px'};
        }       
      } catch (error) {
        
      }
      
      if(vm.dashboardheader.enable){
        if(vm.tabson){
          temp = {'height': 'calc(100% - '+(vm.dashboardheader.height+50)+'px'+')'};
        }
        else{
          temp= {'height': 'calc(100% - '+vm.dashboardheader.height+'px'+')'};
        }
      }else{
        if(vm.tabson){
          temp = {'height': 'calc(100% - 50px)'};
        }
        else{
          temp= {'height': '100%'};
        }
      }
      return temp;
     }
 


  }
})();
