(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('numberfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/numberfilter.html',
      controller: FilterController,
      controllerAs: 'vm',
      bindings:{
        idfilter:"<?",
        datasource: "<?",
        config:"=?"
      }
    });

  /** @ngInject */
  function FilterController( $scope ) {
    var vm = this;
    //structure config =  [{"type":" ", "field":" ","name":" ","op":" ","typeAction":"","initialFilter":""}]
    
    
    vm.$onInit = function () {
      vm.config.value  = parseFloat(vm.config.value);
      vm.vue = new Vue({
        el: '#'+vm.config.htmlId,
        data: function() {
          return {
            dynamicValidateForm: {
              inputValue: parseFloat(vm.config.value),
              inputName: vm.config.name,
              inputId: vm.idfilter
            }
          }
        },
        methods: {
          valueChange : function(inputValue) {
            $scope.$apply(function() {
              vm.config.value = parseFloat(inputValue);
            });
          }, noop: function() {
            // do nothing
          }
        }
      })
    };

  
  }
})();
