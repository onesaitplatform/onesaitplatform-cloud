(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('textfilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/textfilter.html',
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
              inputValue: vm.config.value,
              inputName: vm.config.name,
              inputId: vm.idfilter
            }
          }
        },
        methods: {
          valueChange : function(inputValue) {
            $scope.$apply(function() {
              vm.config.value = inputValue;
            });
          }, noop: function() {
            // do nothing
          }
        }
      })
    };

  
  }
})();
