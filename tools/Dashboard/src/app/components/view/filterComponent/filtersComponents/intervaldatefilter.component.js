(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('intervaldatefilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/intervaldatefilter.html',
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
   
      var startDate = moment().subtract(8,'hour');
      var endDate = moment();
   
       if(typeof vm.config.data.startDate!=='undefined' && vm.config.data.startDate!=null ){
          startDate = moment (vm.config.data.startDate,moment.ISO_8601) ;  
       }
       if(typeof vm.config.data.startDate!=='undefined' && vm.config.data.startDate!=null ){
          endDate = moment (vm.config.data.endDate,moment.ISO_8601) ; 
       } 
   
         vm.vue = new Vue({
           el: '#'+vm.config.htmlId,
           data: function() {
             return {
               dynamicValidateForm: {
                 inputName: vm.config.name,
                 intervalDates: [new Date(startDate.format("YYYY-MM-DDTHH:mm")),new Date(endDate.format("YYYY-MM-DDTHH:mm"))]                 
               }
             }
           },
           methods: {
             dateChange : function(dates) {
               $scope.$apply(function() {
                 var startDate = moment(dates[0]);
                 var endDate = moment(dates[1]);
                 vm.config.data.startDate =startDate.toISOString();
                 vm.config.data.endDate = endDate.toISOString();
               });
             }
           }
         })
       };
   
     
     }
   })();
   