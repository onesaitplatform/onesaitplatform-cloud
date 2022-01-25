(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('datadiscoveryFieldPicker', { 
      templateUrl: 'app/components/view/datadiscoveryComponent/datadiscoveryComponents/datadiscoveryFieldPicker.html',
      controller: DatadiscoveryFieldPickerController,
      controllerAs: 'vm',
      bindings:{            
        datasource: "<",
        fields: "=",
        metrics: "=",
        id: "<?"
      }
    });

  /** @ngInject */
  function DatadiscoveryFieldPickerController($log, $scope, $mdDialog, $element, datasourceSolverService, utilsService) {
    var vm = this;

    vm.$onInit = function(){
      if(!vm.id && !vm.fields && !vm.metrics){
        vm.reloadFields();
      }
    }

    vm.addPrefaultMetrics = function(){
      vm.metrics.push({"type":"metric","field":"recordCount","formula":"count(*)"});
      vm.metrics = vm.metrics.concat(vm.getNumericFields().flatMap(
        function(column){
          return [
            {"type":"metric","field":column.field + "Sum","formula":"sum(" + column.field + ")"},
            {"type":"metric","field":column.field + "Avg","formula":"avg(" + column.field + ")"},
            {"type":"metric","field":column.field + "Min","formula":"min(" + column.field + ")"},
            {"type":"metric","field":column.field + "Max","formula":"max(" + column.field + ")"}
          ]
        }
      ));
    }

    vm.getNumericFields = function(){
      return vm.fields.filter(
        function(column){
          return column.type == "number" || column.type == "integer"
        }
      );
    }

    vm.metricDialog = function (index) {
      $mdDialog.show({
        controller: metricDialogController,
        templateUrl: 'app/partials/edit/addEditDataDiscoveryMetrics.html',
        parent: angular.element(document.body),
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          fields: vm.metrics,
          index: index
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog pages closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    function metricDialogController($scope, $mdDialog, fields, index) {
  
      if(index !== undefined){
        $scope.name = fields[index].field;
        $scope.formula = fields[index].formula; 
        $scope.index = index;
      }

      $scope.cancel = function() {
        $mdDialog.cancel();
      };
  
      $scope.createMetric = function(answer) {
        fields.push({"field":$scope.name,"type":"metric","formula":$scope.formula})
        $mdDialog.hide(answer);
      };

      $scope.editMetric = function(answer) {
        fields[index] = {"field":$scope.name,"type":"metric","formula":$scope.formula};
        $mdDialog.hide(answer);
      };
    }

    vm.dragPickerControl = {
      accept: false,
      itemMoved: function (event) {},
      orderChanged: function(event) {},
      clone: true,
      allowDuplicates: false,
      containment: document.querySelector("#gadget_create_form")?".page-wrapper":null
    };

    vm.removeAttr = function(index){
      vm.fields.splice(index,1);
    } 

    vm.removeMetric = function(index){
      vm.metrics.splice(index,1);
    } 

    vm.reloadFields = function(){
      datasourceSolverService.getFields(vm.datasource.identification).then(
        function(data){
          vm.fields = data;
          vm.metrics = [];
          vm.addPrefaultMetrics();
        }
      ).catch(
        function(error){
          console.error("Error geting datasource fields: " + error); 
        }
      );
    }
}
})();
