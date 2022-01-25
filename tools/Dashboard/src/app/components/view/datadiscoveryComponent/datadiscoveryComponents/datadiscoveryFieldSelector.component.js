(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('datadiscoveryFieldSelector', { 
      templateUrl: 'app/components/view/datadiscoveryComponent/datadiscoveryComponents/datadiscoveryFieldSelector.html',
      controller: DatadiscoveryFieldSelectorController,
      controllerAs: 'vm',
      bindings:{            
        datasource: "=",
        columns: "="
      }
    });

  /** @ngInject */
  function DatadiscoveryFieldSelectorController($log, $scope, $mdDialog, $element, datasourceSolverService, utilsService) {
    var vm = this;

    vm.from = datasourceSolverService.from;

    vm.$onInit = function(){
      if(vm.columns.subtotalField != undefined && (!vm.columns.subtotalFields || (vm.columns.subtotalFields && vm.columns.subtotalFields.length === 0))){
        vm.columns.subtotalFields = [];
        vm.columns.subtotalFields.push(vm.columns.subtotalField);
        vm.columns.subtotalField = undefined;
      }
    }

    vm.$onChanged = function(){
    }

    vm.refreshModel = function(){//copy model to trigger onChange
      vm.columns = angular.copy(vm.columns);
    }

    vm.subtotalChange =function(index){
      vm.columns.subtotalFields = vm.columns.subtotalField==$index?-1:$index;
      vm.refreshModel()
    }

    vm.removeColumn = function(index){
      vm.columns.list.splice(index,1);
      vm.columns.subtotalFields=[];
      vm.refreshModel();
    }

    vm.onDrop = function(model){
      if(model.type !== 'metric'){
        model.asc = true;
      }
      else{
        model.asc = null;
      }
      model.field = model.field.replace(/\.([0-9])+(\.|$)/,"[$1]");
      vm.columns.subtotalEnable = vm.columns.list.filter(function(f){return f.type !== 'metric'}).length > 1 && vm.columns.list.filter(function(f){return f.type == 'metric'}).length > 0;
      vm.refreshModel();
    }

    vm.dragSelectControl = {
      accept: function (sourceItemHandleScope, destSortableScope) {
        return destSortableScope.modelValue.filter(function(item){return item.field === sourceItemHandleScope.modelValue.field && item.$$hashKey !== sourceItemHandleScope.modelValue.$$hashKey}).length == 0;
      },
      orderChanged: function(event) {
        vm.refreshModel();
      },
      allowDuplicates: false,
      containment: document.querySelector("#gadget_create_form")?".page-wrapper":null
    };

    vm.openColumnStyleDialog = function (index) {
      $mdDialog.show({
        controller: columnStyleDialogController,
        templateUrl: 'app/partials/edit/editDataDiscoveryColumnStyle.html',
        parent: angular.element(document.body),
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          index: index
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog pages closed'
        vm.refreshModel();
      }, function() {
        $scope.status = 'You cancelled the dialog.';
        vm.refreshModel();
      });
    };

    function columnStyleDialogController($scope, $mdDialog, index) {
      $scope.index = index;
      $scope.styles = vm.columns.list[index].condstyles;
      $scope.alias = vm.columns.list[index].alias;

      $scope.create = function() {
        var newCondStyle = {
          cond: $scope.cond,
          val: $scope.val,
          val2: $scope.val2,
          style: $scope.style,
          vfunction: $scope.vfunction
        }
        if(!$scope.styles){
          $scope.styles = [];  
        }
        $scope.styles.push(newCondStyle);
        vm.columns.list[index].condstyles = $scope.styles;
        $scope.$applyAsync();
      };

      $scope.delete = function(index){
        $scope.styles.splice(index, 1);
      }

      $scope.cancel = function() {
        vm.columns.list[index].alias = $scope.alias;
        $mdDialog.cancel();
      };
  
    }

    vm.toggleSubtotalField = function(index){
      var elemindex = vm.columns.subtotalFields.indexOf(index);
      if(elemindex === -1){
        vm.columns.subtotalFields.push(index);
      }
      else{
        vm.columns.subtotalFields.splice(elemindex, 1);
      }
    }

}
})();
