(function () { 
  'use strict';

  angular.module('dashboardFramework')
    .component('html5', {
      templateUrl: 'app/components/view/html5Component/html5.html',
      controller: HTML5Controller,
      controllerAs: 'vm',
      bindings:{
        id:"=?",
        livecontent:"<",
        datasource:"<"
      }
    });

  /** @ngInject */
  function HTML5Controller($timeout,$log, $scope, $element, $mdCompiler, $compile, datasourceSolverService,httpService,interactionService,utilsService,urlParamService) {
    var vm = this;
    
    vm.status = "initial";

    vm.$onInit = function(){
      compileContent();
      if(!vm.loadSended){
        window.dispatchEvent(new CustomEvent('gadgetloaded', { detail: vm.id }));
        vm.loadSended = true;
      }
    }

    vm.$onChanges = function(changes,c,d,e) {
        compileContent();
    };


    vm.$onDestroy = function(){
     
    }
    function compileContent(){
     
        
        $timeout(
          function(){
            try {
                var ifrm = document.getElementById(vm.id + "_html5");
                ifrm = (ifrm.contentWindow) ? ifrm.contentWindow : (ifrm.contentDocument.document) ? ifrm.contentDocument.document : ifrm.contentDocument; 
                ifrm.document.open(); 
                ifrm.document.write(vm.livecontent); 
                ifrm.document.close();
                console.log("Compiled html5")
          } catch (error) {        
          }
          },0);
       

       
    
    }
  
  }
})();
