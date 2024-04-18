(function () {
  'use strict';
  angular.module('dashboardFramework')
    .component('filter', {
      templateUrl: 'app/components/view/filterComponent/filter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings: {
        id: "<?",
        datasource: "<?",
        config: "=?",
        hidebuttonclear: "<?",
        buttonbig:"<?"
      }
    });

  /** @ngInject */
  function FilterController($mdDialog, $timeout,filterService) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":"",value:""}]
    //"typeAction": {action, value , filter}


    vm.$onInit = function () {
      if(typeof vm.config!=='undefined' && vm.config!==null){
        vm.tempConfig = JSON.parse(JSON.stringify(vm.config)); 
        if(vm.tempConfig.length>0){
          for (var index = 0; index < vm.tempConfig.length; index++) {
            var element = vm.tempConfig[index];
            element.htmlId =  generateID();
          }
        }
      }
    };



    vm.sendFilters = function () {
      filterService.sendFilters(vm.id, vm.tempConfig);
      //filterService.cleanAllFilters(vm.id, vm.tempConfig);      
      //filterService.cleanAllFilters(vm.id, vm.tempConfig,filterService.sendFilters(vm.id, vm.tempConfig));     
      vm.config = JSON.parse(JSON.stringify(vm.tempConfig));
      $mdDialog.hide();
    }

    vm.cleanFilters = function () {
      filterService.cleanAllFilters(vm.id, vm.tempConfig);
      $mdDialog.hide();
    }

    function generateID(){
      return 'id-' + Math.random().toString(36).substr(2, 16);
    }


  }
})();