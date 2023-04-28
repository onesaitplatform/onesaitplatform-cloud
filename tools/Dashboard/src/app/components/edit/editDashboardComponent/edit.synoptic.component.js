(function () {
    'use strict';
    angular.module('dashboardFramework')
      .component('editSynoptic', {
        templateUrl: 'app/components/edit/editDashboardComponent/edit.synoptic.html',
        controller: EditSynopticController,
        controllerAs: 'ed',
        bindings: {
          "dashboard":"=",       
          "public":"&",
          "id":"&",
          "selectedpage" : "&",
          "synopticedit": "=?"
        }
      });
  
    /** @ngInject */
    function EditSynopticController( __env, $scope, $mdDialog,  httpService, interactionService, urlParamService,localStorageService) {
      var ed = this;
      
     
     
      ed.$onInit = function () {    
          localStorageService.saveEnabled=false;   
       
          ed.changeZindexEditor = function (ev) {    
            if(ed.synopticedit.zindexEditor===600){
              ed.synopticedit.zindexEditor=0;
            }else{
              ed.synopticedit.zindexEditor=600;
            }
            $scope.$applyAsync();
          }
      
          ed.hideShowSynopticEditor = function (ev) {    
            ed.synopticedit.showEditor = !ed.synopticedit.showEditor;
            if( document.getElementById("synoptic_editor")!=='undefined' && document.getElementById("synoptic_editor")!=null){
              ed.dashboard.synoptic =
              {
                svgImage:document.getElementById("synoptic_editor").contentWindow.svgEditor.canvas.getSvgString(),
                conditions:Array.from(document.getElementById("synoptic_editor").contentWindow.svgEditor.getConditions())
              };
            }    
            $scope.$applyAsync();
            return ed.synopticedit.showEditor;
          }
       
           ed.saveSynopticAndDashboard = function (token) {        
            if(typeof document.getElementById("synoptic_editor")!=='undefined' && document.getElementById("synoptic_editor")!=null){
              ed.dashboard.synoptic =
              {
                svgImage:document.getElementById("synoptic_editor").contentWindow.svgEditor.canvas.getSvgString(),
                conditions:Array.from(document.getElementById("synoptic_editor").contentWindow.svgEditor.getConditions())
              };
            }
             ed.dashboard.interactionHash = interactionService.getInteractionHashWithoutGadgetFilters();
             ed.dashboard.parameterHash = urlParamService.geturlParamHash();
             //console.log("synoptic saved");    
             return httpService.saveDashboardToken(ed.id(), {"data":{"model":JSON.stringify(ed.dashboard),"id":"","identification":"a","customcss":"","customjs":"","jsoni18n":"","description":"a","public":ed.public}},token); 
           }
           
      
      
         
      
      
      
      
          
      
      
      
          
      }
  
    
  
  
     
    
  
    }
  })();
  
  
