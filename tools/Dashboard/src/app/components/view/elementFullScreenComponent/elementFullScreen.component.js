(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('elementFullScreen', {
      templateUrl: 'app/components/view/elementFullScreenComponent/elementFullScreen.html',
      controller: ElementFullScreenController,
      controllerAs: 'vm',
      bindings:{
        element: "<",
        iframe: "=",
        editmode: "<",
        gridoptions: "="
      }
    });

  /** @ngInject */
  function ElementFullScreenController() {
    var vm = this;

    vm.$onInit = function () {
      vm.gridoptions = angular.copy(vm.gridoptions);
      vm.gridoptions.minCols = 1;
      vm.gridoptions.maxCols = 1;
      vm.gridoptions.minRows = 1;
      vm.gridoptions.maxRows = 1;
      vm.element.cols = 1;
      vm.element.rows = 1;
      vm.element.notshowDotsMenu = true;
    }
  }
})();
