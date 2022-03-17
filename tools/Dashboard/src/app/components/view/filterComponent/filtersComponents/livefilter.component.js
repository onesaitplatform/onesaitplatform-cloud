(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('livefilter', {
      templateUrl: 'app/components/view/filterComponent/filtersComponents/livefilter.html',
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

   //   startDate:moment().subtract(8,'hour'),
   //   endDate:moment(),   
   //if realtime use now function name(params)
   //NOW();
   //NOW(“format“,'unitTime', amount)
   //NOW("yyyy-MM-dd'T'HH:mm:ss'Z'","hour",-intervalDates)

   var startDate = moment().subtract(vm.config.data.selectedPeriod,'hour');
   var endDate = moment();

   if(vm.config.data.realtime == "start"){
      vm.config.data.startDate = 'NOW("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'","hour",-'+vm.config.data.selectedPeriod+')';
      vm.config.data.endDate = 'NOW("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'","hour",0)';
   }else{
    startDate = moment (vm.config.data.startDate,moment.ISO_8601) ;  
    endDate = moment (vm.config.data.endDate,moment.ISO_8601) ; 
   }   

      vm.vue = new Vue({
        el: '#'+vm.config.htmlId,
        data: function() {
          return {
            dynamicValidateForm: {
              intervalDates: [new Date(startDate.format("YYYY-MM-DDTHH:mm")),new Date(endDate.format("YYYY-MM-DDTHH:mm"))],              
              options: [{
                value: 8,
                label: '8 h'
              }, {
                value: 16,
                label: '16 h'
              }, {
                value: 24,
                label: '24 h'
              }],
              selectedPeriod:vm.config.data.selectedPeriod,
              realtime :vm.config.data.realtime            
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
          },
          periodChange : function(selectedPeriod) {
            $scope.$apply(function() {
              vm.config.data.startDate = 'NOW("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'","hour",-'+selectedPeriod+')';
              vm.config.data.endDate = 'NOW("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'","hour",0)';
              vm.config.data.selectedPeriod = selectedPeriod;
            });
          },
          realTimeChange : function(realtime) {
            var selectedPeriod,intervalDates;
           
            if(realtime == "stop"){
              
              var startDate = moment().subtract(this.dynamicValidateForm.selectedPeriod,'hour');
              var endDate = moment();
              this.dynamicValidateForm.intervalDates = [new Date(startDate.format("YYYY-MM-DDTHH:mm")),new Date(endDate.format("YYYY-MM-DDTHH:mm"))];              
            }        

            selectedPeriod = this.dynamicValidateForm.selectedPeriod;
            intervalDates = this.dynamicValidateForm.intervalDates;            
            $scope.$apply(function() {
              vm.config.data.realtime = realtime;
              if(realtime == "stop"){
                vm.config.data.startDate =intervalDates[0].toISOString();
                vm.config.data.endDate =  intervalDates[1].toISOString();
              }else{
                vm.config.data.realtime = realtime;
                vm.config.data.startDate = 'NOW("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'","hour",-'+selectedPeriod+')';
                vm.config.data.endDate = 'NOW("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'","hour",0)';
              }          
            });
          }
        }
      })
    };

  
  }
})();
