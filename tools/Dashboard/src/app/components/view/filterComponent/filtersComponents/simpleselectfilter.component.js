(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('simpleselectfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/simpleselectfilter.html',
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
      vm.vue = new Vue({
        el: '#'+vm.config.htmlId,
        data: function() {
          return {
            dynamicValidateForm: {             
              inputName: vm.config.name,
              inputId: vm.idfilter,
              options:vm.config.data.options,
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
