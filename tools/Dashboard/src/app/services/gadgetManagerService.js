(function () {
  'use strict';

  angular.module('dashboardFramework')
    .service('gadgetManagerService', GadgetManagerService);

  /** @ngInject */
  function GadgetManagerService() {
      var vm = this;
      vm.dashboardModel = {};
      vm.selectedpage = 0;
      vm.gadgetFullScreen = null;
      vm.loadedList = {}

      vm.setDashboardModelAndPage = function(dashboard,selectedpage,gadgetFullScreen){
        vm.dashboardModel = dashboard;
        vm.selectedpage = selectedpage;
        vm.gadgetFullScreen = gadgetFullScreen;

        initLoadGadgetMonitor();
      }

      function initLoadGadgetMonitor(){
        //loaded list
        if(vm.gadgetFullScreen){
          vm.loadedList[vm.gadgetFullScreen] = 1;
        }
        else{
          var pagegadgets = vm.returnGadgets();
          for(var ig in pagegadgets){
            if(pagegadgets[ig].type !== 'gadgetfilter'){
              if(pagegadgets[ig].id in vm.loadedList){
                vm.loadedList[pagegadgets[ig].id]++;
              }
              else{
                vm.loadedList[pagegadgets[ig].id] = 1;
              }
            }
          }
        }

        //receive gadget finish
        window.addEventListener('gadgetloaded', function (e) {
          var gid = e.detail;
          vm.loadedList[gid]--;
          if(checkAllLoaded()){
            window.postMessage("dashboardloaded", "*");
            if(window.self !== window.top){
              window.parent.postMessage("dashboardloaded", "*");
            }
          }
          
        }, false);
      }

      function checkAllLoaded(){
        for(var g in vm.loadedList){
          if(vm.loadedList[g] != 0){
            return false;
          }
        }
        return true;
      }

      vm.findGadgetById = function(gadgetId,page){
        var page = vm.dashboardModel.pages[page || vm.selectedpage];
        for(var layerIndex in page.layers){
          var layer = page.layers[layerIndex];
          var gadgets = layer.gridboard.filter(function(gadget){return gadget.id === gadgetId});
          if(gadgets.length){
            return gadgets[0];
          }
        }
        return null;
      }

      vm.findGadgetByIdAllPages = function(gadgetId){
        for(var pageindex in vm.dashboardModel.pages){
          var g = vm.findGadgetById(gadgetId,pageindex);
          if(g != null){
            return g;
          }
        }
        return null;
      }

      vm.returnGadgets = function(page){
        var gadgets = [];     
        var page = vm.dashboardModel.pages[page || vm.selectedpage];
        for(var layerIndex in page.layers){
          var layer = page.layers[layerIndex];
          var gadgetsAux = layer.gridboard.filter(function(gadget){return typeof gadget.id != "undefined"});
          if(gadgetsAux.length){
            gadgets = gadgets.concat(gadgetsAux);
          }
        }
        return gadgets;
      }
      
  }
})();
