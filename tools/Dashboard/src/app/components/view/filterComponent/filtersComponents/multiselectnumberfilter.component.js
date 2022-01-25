(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('multiselectnumberfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/multiselectnumberfilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"      
      }
    });

  /** @ngInject */
  function FilterController($scope) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    
    
    vm.$onInit = function () {
      vm.opt=[];
      if( typeof vm.config.data.optionsDescription === 'undefined' || vm.config.data.optionsDescription === null ){
        for (var index = 0; index < vm.config.data.options.length; index++) {
          vm.opt.push({label:vm.config.data.options[index],value:vm.config.data.options[index]});          
        }
      }else{
        for (var index = 0; index < vm.config.data.options.length; index++) {
          vm.opt.push({label:vm.config.data.optionsDescription[index],value:vm.config.data.options[index]});          
        }
      }
      vm.vue = new Vue({
        el: '#'+vm.config.htmlId,
        data: function() {
          return {
            dynamicValidateForm: {             
              inputName: vm.config.name,
              inputId: vm.idfilter,
              options:vm.opt,
              optionsSelected:vm.config.data.optionsSelected
            }
          }
        },
        methods: {
          valueChange : function(optionsSelected) {
            $scope.$apply(function() {
              vm.config.data.optionsSelected = optionsSelected;
            });
          }
        }
      })
    };

  
  }
})();
