(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('dashboard', {
      templateUrl: 'app/dashboard.html',
      controller: MainController,
      controllerAs: 'vm',
      bindings:{
        editmode : "=",
        iframe : "=",
        wrapper: "=",
        selectedpage : "&",
        id: "@",
        public: "=",
        synop: "="
      }
    });

  /** @ngInject */
  function MainController($window, $rootScope, $scope,  $mdDialog,$mdPanel, $timeout,$interval,  httpService, interactionService,urlParamService, gadgetManagerService,filterService,utilsService,datasourceSolverService,favoriteGadgetService, $translate, localStorageService, __env, cacheBoard) {
    var vm = this;
    
    $window.onbeforeunload = function(){
      console.log("exit dashboard");     
      datasourceSolverService.disconnect();
    };
    
    vm.$onInit = function () {
      
     $translate.use(utilsService.urlParamLang());
     vm.showSynopticEditor = false;
      if(vm.editmode){
        vm.showSynopticEditor=true;
        //show dialog dashboard is in use
        if(!vm.iframe && typeof __env.resourceinuse!=='undefined' && __env.resourceinuse){
          $mdDialog.show({
            controller: dashboardInUseController,
            templateUrl: 'app/partials/edit/dashboardInUseDialog.html',
            parent: angular.element(document.body),
            clickOutsideToClose:false,
            fullscreen: false, // Only for -xs, -sm breakpoints.
            openFrom: '.sidenav-fab',
            closeTo: angular.element(document.querySelector('.sidenav-fab')),
            locals: {                          
            }
          })
          .then(function(page) {
            $scope.status = 'Dialog pages closed'             
          }, function() {
            $scope.status = 'You cancelled the dialog.';
          }); 
        }
        //call for keep the session alive
        $interval(function (){try{ httpService.isAlive() } catch (error) {}}, 60000);
      }
      vm.selectedpage = 0;
      vm.synopticEdit = {
        zindexEditor:600,
        showEditor:vm.showSynopticEditor,
        showSynoptic: vm.synop
      }
      vm.drawAddGadgets = true;

      if(typeof __env.drawAddGadgets !== 'undefined' && __env.drawAddGadgets !== null){
        vm.drawAddGadgets = __env.drawAddGadgets;
      }else {
        if( __env.dashboardEngineBungleMode ){
          vm.drawAddGadgets = false;
        }       
      }
      vm.initDash = function (dash){
        if(typeof dash !== 'undefined'){        
          vm.dashboard = dash;
        }            
        vm.dashboard.gridOptions.resizable.stop = sendResizeToGadget;
  
        vm.dashboard.gridOptions.enableEmptyCellDrop = true;
        if((!vm.iframe||
          (vm.iframe && typeof vm.dashboard.editButtonsIframe!='undefined' && vm.dashboard.editButtonsIframe.active)) || 
          vm.wrapper){ 
            
           vm.dashboard.gridOptions.emptyCellDropCallback = dropElementEvent;
           vm.dashboard.gridOptions.emptyCellDragCallback = dropElementEvent;
         
        } 
        //If interaction hash then recover connections
        if(vm.dashboard.interactionHash){
          interactionService.setInteractionHash(vm.dashboard.interactionHash);
        }
         //If interaction hash then recover connections
         if(vm.dashboard.parameterHash){
          urlParamService.seturlParamHash(vm.dashboard.parameterHash);
        }
        if(typeof vm.dashboard.gridOptions.displayGrid === "undefined" ||
                  vm.dashboard.gridOptions.displayGrid === null ){
          vm.dashboard.gridOptions.displayGrid = "onDrag&Resize";
        }
       
        if(!vm.editmode){           
          vm.dashboard.gridOptions.draggable.enabled = false;
          vm.dashboard.gridOptions.resizable.enabled = false;
          vm.dashboard.gridOptions.enableEmptyCellDrop = false;
          vm.dashboard.gridOptions.displayGrid = "none";
          vm.dashboard.gridOptions.enableEmptyCellDrag = false;
          var urlParamMandatory = urlParamService.checkParameterMandatory();
          if(urlParamMandatory.length>0){
            showUrlParamDialog(urlParamMandatory);
          }
        }

        //if $gadgetid is present in params we visualize this gadget in full screen
        if("$gadgetid" in __env.urlParameters){
          gadgetManagerService.setDashboardModelAndPage(vm.dashboard,vm.selectedpage,__env.urlParameters["$gadgetid"]);
          generateGadgetDataForView(__env.urlParameters["$gadgetid"]);
        }
        else{
          gadgetManagerService.setDashboardModelAndPage(vm.dashboard,vm.selectedpage);
        }
     }
  


      // pdf configuration
      var margins = {
        top: 70,
        bottom: 40,
        left: 30,
        width: 550
      };

      vm.margins = margins;
      
      $rootScope.dashboard = angular.copy(vm.id);

      function loadDashboard(model) {

        window.postMessage("start loading dashboard", "*");
        if(window.self !== window.top){
          window.parent.postMessage("start loading dashboard", "*");
        }
        console.log("start loading dashboard");
       

        if (__env.dashboardEngineBungleMode) {
          Object.assign(cacheBoard, model);
          model = JSON.parse(model.model);
        }
        localStorageService.isAfterSavedDate(vm.id, model.updatedAt).then(function (isAfterSavedDate) {
          if (!vm.iframe && vm.editmode && __env.codeLocalStorage === null && !isAfterSavedDate) {
            vm.initDash(model);
            $mdDialog.show({
              controller: localStorageController,
              templateUrl: 'app/partials/edit/initLocalStorageDialog.html',
              parent: angular.element(document.body),
              clickOutsideToClose: false,
              fullscreen: false, // Only for -xs, -sm breakpoints.
              openFrom: '.sidenav-fab',
              closeTo: angular.element(document.querySelector('.sidenav-fab')),
              locals: {
              }
            })
              .then(function (page) {
                $scope.status = 'Dialog pages closed'
              }, function () {
                $scope.status = 'You cancelled the dialog.';
              });

            function localStorageController($scope, $mdDialog) {
              $scope.cancel = function () {
                $mdDialog.cancel();
              };

              $scope.hide = function () {
                $mdDialog.hide();
              };
              $scope.ok = function () {
                localStorageService.getLastItemDate(vm.id).then(function (identification) {
                  $window.location.href = __env.endpointControlPanel + '/dashboards/editfull/' + vm.id + '?__hist_dash=' + identification;
                  $mdDialog.hide();
                })


              };
            }
          } else if (!vm.iframe && vm.editmode && __env.codeLocalStorage !== null) {
            localStorageService.getItemByIdAndDate(vm.id, __env.codeLocalStorage).then(function (modByIdAndDate) {
              vm.initDash(modByIdAndDate);
            })

          } else {
            vm.initDash(model);
          }
        })
      }

      if(!utilsService.isEmptyJson(cacheBoard) && cacheBoard.id == vm.id){
        loadDashboard(cacheBoard);
      }
      else{
        /*Rest api call to get dashboard data*/
        httpService.getDashboardModel(vm.id).then(
          function(data){loadDashboard(data.data);}
        ).catch(
          function(error){      
            if(sessionStorage.getItem("dashboardEngineOauthtoken") != null){
              document.getElementsByTagName("dashboard")[0].innerHTML = "<div style='padding:15px;background:#fbecec'><div class='no-data-title'>Dashboard Engine Error " + (error.status?error.status:"") + "</div><div class='no-data-text'>" + (error.config?"Rest Call: " + error.config.url + ". ":"") + "Detail: " + (error.data?JSON.stringify(error.data):error) + "</div></div>";
              window.dispatchEvent(new CustomEvent('errordashboardengine', { detail: {
                "type": "failLoadDashboard",
                "errorCode": (error.status?error.status:"")
              } }));
            } 
            else{
              $window.location.href = "/controlpanel/500";
            }   
          }
        )
      }

      function generateGadgetDataForView(gadgetid){//we  search by gadgetid
        vm.gadgetFullScreen = gadgetManagerService.findGadgetByIdAllPages(gadgetid);
        
        if(!vm.gadgetFullScreen){
          console.error("Gadget ID: " + gadgetid + ", not found in dashboard. Loading complete dashboard");
        }
      }

      function addGadgetHtml5(type,config,layergrid){
        addGadgetGeneric(type,config,layergrid);
      } 
      function addGadgetFilter(type,config,layergrid){
        addGadgetGeneric(type,config,layergrid);
      } 


      function addGadgetGeneric(type,config,layergrid){
        config.type = type;
        layergrid.push(config);      
        window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: config}));
        $timeout(
         function(){
           $scope.$broadcast("$resize", "");
         },100
       );
       } 

       function dashboardInUseController($scope, $mdDialog) {                    
        $scope.cancel = function() { 
          $mdDialog.cancel();              
        };
          
        $scope.hide = function() { 
          $mdDialog.hide();
        };
        $scope.ok = function(){ 
          $window.location.href=__env.endpointControlPanel+'/dashboards/list/'; 
          $mdDialog.hide();
        };
      }

      
      vm.api={};
      //External API
      vm.api.createGadget = function(type,id,name,template,datasource,filters,customMenuOptions,setupLayout) {
            if(typeof template !== "undefined" && template !== null && typeof template !=="string" && template.length>0 ){
              //Gadgetcreate from template
              var newElem = {x: 0, y: 0, cols: 40, rows: 40,};
              //newElem.minItemRows = 10;
              //newElem.minItemCols = 10;            
              newElem.content=template.template;        
              newElem.contentcode = template.templateJS;
              newElem.id = id;             
              newElem.type = 'livehtml';
              newElem.idtemplate =  template.identification;
              newElem.header = {
                enable: true,
                title: {
                  iconColor: "hsl( 206, 54%, 5%)",
                  text: name, 
                  textColor: "hsl( 206, 54%, 5%)"
                },
                backgroundColor: "hsl(0, 0%, 100%)",
                height: 40
              } 
              newElem.backgroundColor ="white";
              newElem.padding = 0;
              newElem.border = {
                color: "hsl(0, 0%, 90%)",
                width: 0,
                radius: 5
              }  
              newElem.datasource = datasource;
              newElem.filters = filters;  
              newElem.filtersInModal=setupLayout.filtersInModal;
              newElem.customMenuOptions = customMenuOptions;
              newElem.hideBadges=setupLayout.hideBadges;
              newElem.hidebuttonclear=setupLayout.hidebuttonclear;
              addGadgetGeneric(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);        

            }else{
              var newElem = {x: 0, y: 0, cols: 40, rows: 40,};
              //newElem.minItemRows = 10;
              //newElem.minItemCols = 10;
              var type = type;
              newElem.id = id;
              newElem.content = type;
              newElem.type = type;
              newElem.idtemplate = type;
              newElem.header = {
                enable: true,
                title: {
                  iconColor: "hsl( 206, 54%, 5%)",
                  text: name, 
                  textColor: "hsl( 206, 54%, 5%)"
                },
                backgroundColor: "hsl(0, 0%, 100%)",
                height: 40
              }
              newElem.backgroundColor ="white";
              newElem.padding = 0;
              newElem.border = {
                color: "hsl(0, 0%, 90%)",
                width: 0,
                radius: 5
              }    
              newElem.filters = filters;    
              newElem.filtersInModal = setupLayout.filtersInModal;
              newElem.hideBadges = setupLayout.hideBadges;
              newElem.hidebuttonclear = setupLayout.hidebuttonclear;      
              addGadgetGeneric(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);        

            }
         
      };

      vm.api.updateFilterGadget = function(id,filters,customMenuOptions,merge) {
        $scope.$apply(function() {    
        var gadgets = vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard;
        for(var i=0;i<gadgets.length;i++){
          var gadget = gadgets[i];       	
          if(typeof gadget.id!=="undefined" && gadget.id === id){
            if(typeof gadget.filters!=="undefined" && gadget.filters!==null ){
              for(var j=0;j<gadget.filters.length;j++){
                var filter = gadget.filters[j];         
                for(var k=0;k<filters.length;k++){
                  if(filter.id === filters[k].id){
                      if(filters[k].type === 'multiselectfilter' || filters[k].type === 'multiselectnumberfilter'                       
                      ){
                        if(merge){
                          filter.data.options = Array.from(new Set(filter.data.options.concat(filters[k].data.options)));
                          filter.data.optionsDescription = Array.from(new Set(filter.data.optionsDescription.concat(filters[k].data.optionsDescription)));
                          filter.data.optionsSelected = Array.from(new Set(filter.data.optionsSelected.concat(filters[k].data.optionsSelected)));
                        }else{
                          filter.data.options = filters[k].data.options;
                          filter.data.optionsDescription = filters[k].data.optionsDescription;
                          filter.data.optionsSelected = filters[k].data.optionsSelected;
                        }
                      } 
                     else if(filters[k].type === 'multiselectdsfilter' || filters[k].type === 'multiselectnumberdsfilter'||
                     filters[k].type === 'simpleselectdsfilter' || filters[k].type === 'simpleselectnumberdsfilter'
                     ){                     
                       filter.data.ds = filters[k].data.ds;
                       filter.data.dsFieldValue = filters[k].data.dsFieldValue;
                       filter.data.dsFieldDes = filters[k].data.dsFieldDes;                    
                   } else if(filters[k].type === 'textfilter'){
                        filter.value = filters[k].value;
                      } else if(filters[k].type === 'numberfilter'){
                        filter.value = filters[k].value;
                      }
                       
                    }
                  }
                }		
              }
              if(merge){
                gadget.customMenuOptions = Array.from(new Set(gadget.customMenuOptions.concat(customMenuOptions)));               
              }else{
                gadget.customMenuOptions = customMenuOptions;
              }
              var idNoSpaces = id;
              idNoSpaces = idNoSpaces.replace(new RegExp(" ", "g"), "\\ ");
              angular.element(document.querySelector('#'+idNoSpaces)).controller("element").config = gadget.filters;
              if(gadget.filters !== undefined && gadget.filters!==null  ){
                filterService.sendFilters(id,gadget.filters);
              }
              angular.element(document.querySelector('#'+idNoSpaces)).controller("element").reloadFilters();
              break;
            }
          }    }) 
        
      }

      vm.api.dropOnElement = function(x,y) {
        if(x !=null && y !=null){
        vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard;
        var elements = document.getElementsByTagName("element");
        if(elements!=null && elements.length>0){
          for (var index = 0; index < elements.length; index++) {
            var element = elements[index];
            console.log(element.firstElementChild.getBoundingClientRect());
            var sl = element.firstElementChild.getBoundingClientRect().x;
            var sr = element.firstElementChild.getBoundingClientRect().x+ element.firstElementChild.getBoundingClientRect().width;
            var st = element.firstElementChild.getBoundingClientRect().y;
            var sb = element.firstElementChild.getBoundingClientRect().y + element.firstElementChild.getBoundingClientRect().height;
            if(x>=sl && x<=sr && y<= sb && y >= st){
              return {"dropOnElement":"TRUE","idGadget":element.id,"type":element.getAttribute('idtemplate')};
            }
          }
        }
      }
        return {"dropOnElement":"FALSE","idGadget":"","type":""};
      }

      vm.api.refreshGadgets = function(){
        var gadgets =document.querySelectorAll( 'gadget' ) ;
        if(gadgets.length>0){
         for (var index = 0; index < gadgets.length; index++) {
           var gad = gadgets[index];
           angular.element(gad).scope().$$childHead.reloadContent();
         }        
       }


      }

      //END External API
      

//------------------------------------------------------------------------------------------
      
      function newTemplateDialog(identification,inline,config,layergrid){
        httpService.getUserGadgetTemplateByIdentification(identification).then(
          function(data){     
            var template = data.data      
            config.type = 'livehtml';
            //subtype angularJS, ...
            config.subtype = template.type;
            config.content=template.template        
            config.contentcode=template.templateJS
            config.template = template.identification;
            config.tempId = template.id
            config.tconfig = template.config
            function contextShowAddGadgetTemplateParameterDialog () {
              showAddGadgetTemplateParameterDialog(config.type,config,layergrid,true,inline);
            }
            checkHeaderLibsInDashboard(template.headerlibs, contextShowAddGadgetTemplateParameterDialog)
          }
        ).catch(function (error) {
          console.error('Can not load gadget template: ', error)
        });;
      }




//----------------------------------------------------

      function showAddTemplateDialog(config,layergrid){
        function AddGadgetController($scope,__env, $mdDialog, httpService,  config, layergrid) {
          //$scope.type = type;
          $scope.config = config;
          $scope.layergrid = layergrid;

          $scope.gadgets = [];         
          $scope.templates = []; 
          
          $scope.hide = function() {
            $mdDialog.hide();
          };

          $scope.cancel = function() {
            $mdDialog.cancel();
          };

          $scope.loadTemplates = function() {
          return httpService.getUserGadgetTemplate().then(
              function(templates){
                if(templates!=null && typeof templates.data != 'undefined' && templates.data!=null ){
                  var templateBaseFiltered = templates.data.filter(function(itm){
                    return itm.type!=='base';
                  });
                $scope.templates = templateBaseFiltered;
                }
              }
            );
          };

          $scope.loadGadgets = function() {
            return httpService.getUserGadgetsByType($scope.template.id).then(
              function(gadgets){
                $scope.gadgets = gadgets.data;
              }
            );
          };

          $scope.addGadget = function() {
            $scope.config.type = $scope.template.id;
           

            if(!$scope.template || !$scope.gadget) return;  
            
            var configGadget = JSON.parse($scope.gadget.config)
            $scope.config.type = 'livehtml';  
            $scope.config.subtype = $scope.template.type;             
            $scope.config.params = configGadget.parameters;
            $scope.config.content=$scope.template.template        
            $scope.config.contentcode=$scope.template.templateJS
            $scope.config.template = $scope.template.identification;
            $scope.config.tempId = $scope.template.id
            
            if(typeof configGadget.datasource!= 'undefined'){              
              $scope.config.datasource = {
                          id:configGadget.datasource.id,                       
                          name:configGadget.datasource.name,
                          query:configGadget.datasource.query,
                          refresh:configGadget.datasource.refresh,
                          maxValues:configGadget.datasource.maxValues,
                          type:configGadget.datasource.type,
                          description:configGadget.datasource.description}
            }
            $scope.config.gadgetid = $scope.gadget.id;
            $scope.layergrid.push($scope.config);
            window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: $scope.config}));
            $mdDialog.cancel();
          };

          function formGadget(inline) {
            if(!$scope.template) return;   
            //type htmlive  
            // $scope.config.type = $scope.type;

            $scope.config.type = 'livehtml';
            //subtype angularJS, ...
            $scope.config.subtype = $scope.template.type;
            $scope.config.content=$scope.template.template        
            $scope.config.contentcode=$scope.template.templateJS
            $scope.config.template = $scope.template.identification;
            $scope.config.tempId = $scope.template.id
            $scope.config.tconfig = $scope.template.config
            showAddGadgetTemplateParameterDialog($scope.config.type,$scope.config,$scope.layergrid,true,inline);
            $mdDialog.hide();
          }

          $scope.newGadget = function($event,inline) {
            formGadget(false);
          };

          $scope.newGadgetInline = function($event,inline) {
            formGadget(true);
          };

        }

        $mdDialog.show({
          controller: AddGadgetController,
          templateUrl: 'app/partials/edit/addTemplateDialog.html',
          parent: angular.element(document.body),
          clickOutsideToClose:true,
          fullscreen: false, // Only for -xs, -sm breakpoints.
          openFrom: '.sidenav-fab',
          closeTo: angular.element(document.querySelector('.sidenav-fab')),
          locals: {           
            config: config,
            layergrid: layergrid
          }
        })
        .then(function() {

        }, function() {
          $scope.status = 'You cancelled the dialog.';
        });
      }





      function newGadgetDialog(type,config,layergrid){       
            $scope.type = type;
            $scope.config = config;
            $scope.layergrid = layergrid;
         
            var parentEl = angular.element(document.body);
            $mdDialog.show({
              parent: parentEl,
             
              fullscreen: false,
              template:
                '<md-dialog id="dialogCreateGadget"  aria-label="List dialog">' +
                '  <md-dialog-content >'+
                '<iframe id="iframeCreateGadget" style=" height: 80vh; width: 80vw;" frameborder="0" src="'+__env.endpointControlPanel+'/gadgets/createiframe/'+$scope.type+'"+></iframe>'+                     
                '  </md-dialog-content>' +             
                '</md-dialog>',
              locals: {
                config:  $scope.config, 
                layergrid: $scope.layergrid,
                type: $scope.type
              },
              controller: DialogController
           });
           function DialogController($scope, $mdDialog, config, layergrid, type) {
             $scope.config = config;
             $scope.layergrid = layergrid;
             $scope.closeDialog = function() {               
               $mdDialog.hide();
             }
              $scope.addGadgetFromIframe = function(type,id,identification) {
              $scope.config.type = type;
              $scope.config.id = id;
              $scope.config.header.title.text = identification;
              $scope.layergrid.push($scope.config);
              window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: $scope.config}));
              $mdDialog.cancel();
            };
           }
                };
    




      function showAddGadgetDialog(type,config,layergrid){
        function AddGadgetController($scope,__env, $mdDialog, httpService, type, config, layergrid) {
          $scope.type = type;
          $scope.config = config;
          $scope.layergrid = layergrid;

          $scope.gadgets = [];
         

          $scope.hide = function() {
            $mdDialog.hide();
          };

          $scope.cancel = function() {
            $mdDialog.cancel();
          };


          $scope.loadGadgets = function() {
            return httpService.getUserGadgetsByType($scope.type).then(
              function(gadgets){
                $scope.gadgets = gadgets.data;
              }
            );
          };

          $scope.addGadget = function() {
            $scope.config.type = $scope.type;
            $scope.config.id = $scope.gadget.id;
            $scope.config.header.title.text = $scope.gadget.identification;
            $scope.layergrid.push($scope.config);
            window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: $scope.config}));
            $mdDialog.cancel();
          };

          
          $scope.newGadget = function($event) {
            var parentEl = angular.element(document.body);
            $mdDialog.show({
              parent: parentEl,
              targetEvent: $event,
              fullscreen: false,
              template:
                '<md-dialog id="dialogCreateGadget"  aria-label="List dialog">' +
                '  <md-dialog-content >'+
                '<iframe id="iframeCreateGadget" style=" height: 80vh; width: 80vw;" frameborder="0" src="'+__env.endpointControlPanel+'/gadgets/createiframe/'+$scope.type+'"+></iframe>'+                     
                '  </md-dialog-content>' +             
                '</md-dialog>',
              locals: {
                config:  $scope.config, 
                layergrid: $scope.layergrid,
                type: $scope.type
              },
              controller: DialogController
           });
           function DialogController($scope, $mdDialog, config, layergrid, type) {
             $scope.config = config;
             $scope.layergrid = layergrid;
             $scope.closeDialog = function() {
               
               $mdDialog.hide();
             }

              $scope.addGadgetFromIframe = function(type,id,identification) {
              $scope.config.type = type;
              $scope.config.id = id;
              $scope.config.header.title.text = identification;
              $scope.layergrid.push($scope.config);
              window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: $scope.config}));
              $mdDialog.cancel();
            };
           }
                };
        }

        $mdDialog.show({
          controller: AddGadgetController,
          templateUrl: 'app/partials/edit/addGadgetDialog.html',
          parent: angular.element(document.body),
          clickOutsideToClose:true,
          fullscreen: false, // Only for -xs, -sm breakpoints.
          openFrom: '.sidenav-fab',
          closeTo: angular.element(document.querySelector('.sidenav-fab')),
          locals: {
            type: type,
            config: config,
            layergrid: layergrid
          }
        })
        .then(function() {

        }, function() {
          $scope.status = 'You cancelled the dialog.';
        });
      }

      function addFavoriteGadget(id,config,layergrid){

        favoriteGadgetService.getFavoriteGadgetByIdentification(id).then(
          function(favorite){                
            var data = JSON.parse(favorite.config);
            config.id =  (favorite.type + "_" + (new Date()).getTime());                
            config.type = favorite.type;
    
            config.header = data.header;
            config.backgroundColor =data.backgroundColor;
            config.padding = data.padding;
            config.border = data.border;
           
            if(favorite.gadget){
                if(config.type == 'livehtml'){
                  addCustomGadget(favorite.gadget.id, config, layergrid)
                }else{
                //gadgets line,bars,...
                config.id = favorite.gadget.id; 
                layergrid.push(config);
                window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: config}));
                
                }
            }
            //we differentiate by type
            else if(config.type == 'livehtml'){
              config.subtype = data.subtype;
              config.content=data.content;
              config.contentcode=data.contentcode;
              config.datasource = data.datasource;
              if(favorite.gadgetTemplate){
                config.template = favorite.gadgetTemplate.identification; 
                config.params=data.params;
                layergrid.push(config);
                window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: config}));                
               }else{
                layergrid.push(config); 
                window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: config}));              
               }
              }else  if(config.type == 'gadgetfilter'){
                config.content = data.content;
                config.contentcode = data.contentcode;
                config.filters = data.filters;
                addGadgetFilter(config.type,config,layergrid);              
              }
              else if(config.type == 'html5'){         
                addGadgetHtml5(config.type,layergrid);                
              }                  
              
              //we use config.id like target gadget id because can be changed when is created
              //add urlparams
              if (data.urlparams) {
                if (Object.keys(data.urlparams).length > 0) {
                  for (var keyGadget in data.urlparams) {
                    var destinationList = data.urlparams[keyGadget];
                    for (var keyGDest in destinationList) {
                      var destination = destinationList[keyGDest];
                      for (var targ in destination.targetList) {
                        var target = destination.targetList[targ];                          
                        urlParamService.registerParameter(keyGadget, destination.type, config.id, target.overwriteField, destination.mandatory);
                      }
                    }
                  }
                }                
              }
              //add datalinks
              if(data.datalinks){
                if (Object.keys(data.datalinks).length > 0) {
                  for (var keyGadget in data.datalinks) {
                    var sourceList = data.datalinks[keyGadget];
                    for (var keyGDest in sourceList) {
                      var destination = sourceList[keyGDest];
                      for (var targ in destination.targetList) {
                        var target = destination.targetList[targ];
                        interactionService.registerGadgetInteractionDestination(keyGadget, config.id, destination.emiterField, target.overwriteField,undefined,target.filterChaining,undefined);         
                      }
                    }
                  }
                }
              }


          });


      } 
      
      function createGadgetAndAdd(type, element, config, layergrid) {
        httpService.getGadgetTemplateByIdentification(type).then(
          function (data) {
            if (data.data.type === 'base') {
              console.error('not suported');
            } else {
              utilsService.createCustomGadget(config, type).then(
                function (response) {
                  addCustomGadget(response.data.id, element, layergrid)
                },
                function (e) {
                  console.log("Error create Custom Gadget: " + JSON.stringify(e))
                }
              );
            }
          }
        ).catch(function (error) {
          console.error('Can not load gadget: ', error)
        });
      }


      function addCustomGadget(id,config,layergrid){
        httpService.getGadgetConfigById(
          id
        ).then( 
          function(dataGadget){
            var gadget = dataGadget.data;
            
           
              var template = gadget.type;
              config.type = template.id;
              

              if(!template || !gadget) return;  
              
              var configGadet = JSON.parse(gadget.config)
              config.type = 'livehtml';  
              config.subtype = template.type;             
              config.params = configGadet.parameters;
              config.content=template.template        
              config.contentcode=template.templateJS
              config.template = template.identification;
              config.tempId = template.id
              if(config.id == gadget.id){
                config.id = (config.type + "_" + (new Date()).getTime());    
              }
              if(typeof configGadet.datasource!= 'undefined'){
                config.datasource = {
                            name:configGadet.datasource.identification,
                            query:configGadet.datasource.query,
                            refresh:configGadet.datasource.refresh,
                            maxValues:configGadet.datasource.maxValues,
                            description:configGadet.datasource.description}
              }
              config.gadgetid = gadget.id;

              function contextEndAddCustomGadget () {
                layergrid.push(config);
                window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: config}));
              }
              checkHeaderLibsInDashboard(template.headerlibs, contextEndAddCustomGadget)
          }            
        ,function(e){
          if(e.message==='Gadget was deleted'){
              vm.type='removed'
              console.log('Gadget was deleted');
          }else{
              vm.type = 'nodata'
              console.log('Data no available'); 
          }
        })
      } 

      function showAddFavoriteGadgetDialog(config,layergrid){
        function AddFavoriteGadgetController($scope,__env, urlParamService,interactionService, favoriteGadgetService, $mdDialog, httpService, config, layergrid) {
          
          $scope.config = config;
          $scope.layergrid = layergrid;

          $scope.gadgets = [];

          $scope.hide = function() {
            $mdDialog.hide();
          };

          $scope.cancel = function() {
            $mdDialog.cancel();
          };


          $scope.loadGadgets = function() {
            return favoriteGadgetService.getAllIdentifications().then(
              function(gadgets){                
                $scope.gadgets = gadgets;
              }
            );
          };

          $scope.addGadget = function() {
            addFavoriteGadget($scope.gadget,$scope.config,$scope.layergrid);
            $mdDialog.hide();
          };

          $scope.alert;
        
        }

        $mdDialog.show({
          controller: AddFavoriteGadgetController,
          templateUrl: 'app/partials/edit/addFavoriteGadgetDropDialog.html',
          parent: angular.element(document.body),
          clickOutsideToClose:true,
          fullscreen: false, // Only for -xs, -sm breakpoints.
          openFrom: '.sidenav-fab',
          closeTo: angular.element(document.querySelector('.sidenav-fab')),
          locals: {            
            config: config,
            layergrid: layergrid
          }
        })
        .then(function() {

        }, function() {
          $scope.status = 'You cancelled the dialog.';
        });
      }



      function showAddGadgetTemplateDialog(type,config,layergrid){
        function AddGadgetController($scope,__env, $mdDialog, httpService, type, config, layergrid) {
          $scope.type = type;
          $scope.config = config;
          $scope.layergrid = layergrid;
          $scope.templatetype = 'angularJS'

          $scope.templates = [];

          $scope.hide = function() {
            $mdDialog.hide();
          };

          $scope.cancel = function() {
            $mdDialog.cancel();
          };

          var initCode = {
            "vueJS": {
              "html": "<!--Focus here and F11 to full screen editor-->\n<!-- Write your CSS <style></style> here -->\n<div class=\"gadget-app\">\n<!-- Write your HTML <div></div> here -->\n</div>",
              "js": "//Write your Vue JSON controller code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\n\nvm.vueconfig = {\n\tel: document.getElementById(vm.id).querySelector('vuetemplate .gadget-app'),\n\tdata:{\n\t\tds:[]\n\t},\n\tmethods:{\n\t\tdrawVueComponent: function(newData,oldData){\n\t\t\t//This will be call on new data\n\t\t},\n\t\tresizeEvent: function(){\n\t\t\t//Resize event\n\t\t},\n\t\tdestroyVueComponent: function(){\n\t\t\tvm.vueapp.$destroy();\n\t\t},\n\t\treceiveValue: function(data){\n\t\t\t//data received from datalink\n\t\t},\n\t\tsendValue: vm.sendValue,\n\t\tsendFilter: vm.sendFilter\n\t}\n}\n\n//Init Vue app\nvm.vueapp = new Vue(vm.vueconfig);\n"
            },
            "vueJSODS": {
              "html": "<!--Focus here and F11 to full screen editor-->\n<!-- Write your CSS <style></style> here -->\n<div class=\"gadget-app\">\n<!-- Write your HTML <div></div> here -->\n</div>",
              "js": "//Write your Vue with ODS JSON controller code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\n\nvm.vueconfig = {\n\tel: document.getElementById(vm.id).querySelector('vuetemplate .gadget-app'),\n\tdata:{\n\t\tds:[]\n\t},\n\tmethods:{\n\t\tdrawVueComponent: function(newData,oldData){\n\t\t\t//This will be call on new data\n\t\t},\n\t\tresizeEvent: function(){\n\t\t\t//Resize event\n\t\t},\n\t\tdestroyVueComponent: function(){\n\t\t\tvm.vueapp.$destroy();\n\t\t},\n\t\treceiveValue: function(data){\n\t\t\t//data received from datalink\n\t\t},\n\t\tsendValue: vm.sendValue,\n\t\tsendFilter: vm.sendFilter\n\t}\n}\n\n//Init Vue app\nvm.vueapp = new Vue(vm.vueconfig);\n"
            },
            "reactJS": {
              "html": "<!-- Write your HTML <div></div> and CSS <style></style> here -->\n<!--Focus here and F11 to full screen editor-->",
              "js": "//Write your Vue JSON controller code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\n\nvm.vueconfig = {\n\tel: $element[0],\n\tdata:{\n\t\tds:[]\n\t},\n\tmethods:{\n\t\tdrawVueComponent: function(newData,oldData){\n\t\t\t//This will be call on new data\n\t\t},\n\t\tresizeEvent: function(){\n\t\t\t//Resize event\n\t\t},\n\t\tdestroyVueComponent: function(){\n\n\t\t},\n\t\treceiveValue: function(data){\n\t\t\t//data received from datalink\n\t\t}\n\t}\n}\n\n//Init Vue app\nvm.vueapp = new Vue(vm.vueconfig);\n"
            },
            "angularJS": {
              "html": "<!-- Write your HTML <div></div> and CSS <style></style> here -->\n<!--Focus here and F11 to full screen editor-->",
              "js": "//Write your controller (JS code) code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\nvm.initLiveComponent = function(){\n\n};\n\n//This function will be call when data change. On first execution oldData will be null\nvm.drawLiveComponent = function(newData, oldData){\n\n};\n\n//This function will be call on element resize\nvm.resizeEvent = function(){\n\n}\n\n//This function will be call when element is destroyed\nvm.destroyLiveComponent = function(){\n\n};\n\n//This function will be call when receiving a value from vm.sendValue(idGadgetTarget,data)\nvm.receiveValue = function(data){\n\n};"
            }
          }

          httpService.getTemplateTypes().then(function(data){           
              $scope.templatetypes = data.data;            
          })

          $scope.loadTemplates = function() {
            return httpService.getUserGadgetTemplate().then(
                function(templates){
                  if(templates!=null && typeof templates.data != 'undefined' && templates.data!=null ){
                    var templateBaseFiltered = templates.data.filter(function(itm){
                      
                      return itm.type===$scope.templatetype;
                    });
                  $scope.templates = templateBaseFiltered;
                  }
                }
              );
            };
          $scope.useTemplate = function(byId) {    
            if(!$scope.template) return;     
            $scope.config.type = $scope.type;
            $scope.config.subtype = $scope.templatetype;
            $scope.config.content=$scope.template.template        
            $scope.config.contentcode=$scope.template.templateJS
            if(byId){
              $scope.config.template = $scope.template.identification;
            }
            showAddGadgetTemplateParameterDialog($scope.type,$scope.config,$scope.layergrid,false);
            $mdDialog.hide();
          };
          $scope.noUseTemplate = function() {
            $scope.config.type = $scope.type;
            $scope.config.subtype = $scope.templatetype;
            httpService.getGadgetTemplateType($scope.templatetype).then(function(data){
              $scope.config.content=data.data.template
              $scope.config.contentcode=data.data.templateJS

              $scope.layergrid.push($scope.config);
              window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: $scope.config}));
              $mdDialog.cancel();
            });
          };

        }
        $mdDialog.show({
          controller: AddGadgetController,
          templateUrl: 'app/partials/edit/addGadgetTemplateDialog.html',
          parent: angular.element(document.body),
          clickOutsideToClose:true,
          fullscreen: false, // Only for -xs, -sm breakpoints.
          openFrom: '.sidenav-fab',
          closeTo: angular.element(document.querySelector('.sidenav-fab')),
          locals: {
            type: type,
            config: config,
            layergrid: layergrid
          }
        })
        .then(function() {
       
        }, function() {
          $scope.status = 'You cancelled the dialog.';
        });
      }
       
      function showAddGadgetTemplateParameterDialog(type,config,layergrid,create,inline){
       
        
        if(window.panelRef){
          window.panelRef.close();
        }
        window.panelRef = {};
        var configPanel = {
          attachTo: angular.element(document.getElementById("divrightsidemenubody")),
          controller: 'editTemplateParamsController',
          controllerAs: 'ctrl',
         // position: panelPosition,
          //animation: panelAnimation,
          
          templateUrl: 'app/partials/edit/addGadgetTemplateParameterDialog.html',
          clickOutsideToClose: false,
          escapeToClose: false,
          focusOnOpen: true,
          locals: {
            type: type,
            config: config,
            element: null,
            layergrid: layergrid,
            edit: false,
            create:create,
            inline:inline
          }
        };
        window.dispatchEvent(new CustomEvent('showMenurightsidebardashboard',{}));
        window.removeEventListener('editTemplateParamsclose',function(a){
          window.panelRef.close();
          window.dispatchEvent(new CustomEvent('hideMenurightsidebardashboard',{}));
        });
        window.addEventListener('editTemplateParamsclose',function(a){
          window.panelRef.close();
          window.dispatchEvent(new CustomEvent('hideMenurightsidebardashboard',{}));
        });
      
        $mdPanel.open(configPanel)
        .then(function(result) {
          window.panelRef = result;
        });

      /*  $mdDialog.show({
          controller: 'editTemplateParamsController',
          templateUrl: 'app/partials/edit/addGadgetTemplateParameterDialog.html',
          parent: angular.element(document.body),
          clickOutsideToClose:true,
          fullscreen: false, // Only for -xs, -sm breakpoints.
          openFrom: '.sidenav-fab',
          closeTo: angular.element(document.querySelector('.sidenav-fab')),
          locals: {
            type: type,
            config: config,
            element: null,
            layergrid: layergrid,
            edit: false,
            create:create,
            inline:inline
          }
        })
        .then(function() {
        }, function() {
          $scope.status = 'You cancelled the dialog.';
        });*/
      }

      function AddDashboardHeaderLibsController($scope, httpService, $mdDialog, $window, gadgetlibs, dashboardlibs) {
          $timeout(function(){
            document.querySelector("#headerlibseditor").style.height= window.getComputedStyle(document.querySelector("md-dialog")).height + "px"
            document.querySelector("#headerlibseditor").style.width= "800px"
            $scope.VSheaderlibseditor = monaco.editor.createDiffEditor(document.querySelector("#headerlibseditor"), {
              readOnly: false,
              scrollBeyondLastLine: false,
              theme: "vs-dark",
              automaticLayout: true,
              renderSideBySide: false
            })

          var originalModel = monaco.editor.createModel(
            dashboardlibs,
            "html"
          );

          var newModel = monaco.editor.createModel(
            dashboardlibs + "\n" + gadgetlibs,
            "html"
          );

          $scope.VSheaderlibseditor.setModel({
            original: originalModel,
            modified: newModel
          });

          $scope.VSheaderlibseditor.revealLine(newModel.getLineCount())

        },0);
  
        $scope.hide = function() {
          $mdDialog.hide();
        };
  
        $scope.cancel = function() {
          $mdDialog.cancel();
        };
  
        $scope.saveAndReload = function() {
          httpService.saveHeaderLibsById(vm.id,$scope.VSheaderlibseditor.getModifiedEditor().getValue()).then(
            function(){
              $window.location.reload();
            }
          );
        };
  
      }

      function checkHeaderLibsInDashboard(gadgetlibs, callback) {
        if (__env.dashboardCheckHeaderLibs) {
          httpService.getHeaderLibsById(vm.id).then(
            function (data) {
              if (utilsService.isLibsinHLibs(gadgetlibs, data.data)) {
                callback();
              } else {
                $mdDialog.show({
                  controller: AddDashboardHeaderLibsController,
                  templateUrl: 'app/partials/edit/askAddHeaderLibsToDashboardDialog.html',
                  parent: angular.element(document.body),
                  clickOutsideToClose: true,
                  fullscreen: false, // Only for -xs, -sm breakpoints.
                  openFrom: '.sidenav-fab',
                  closeTo: angular.element(document.querySelector('.sidenav-fab')),
                  locals: {
                    gadgetlibs: gadgetlibs,
                    dashboardlibs: data.data
                  }
                })
                  .then(function () {

                  }, function () {
                    $scope.status = 'You cancelled the dialog.';
                    callback();
                  });
              }
            }
          )
        } else {
          callback();
        }
      }

      function dropElementEvent(e,newElem){         
        var type = (!e.dataTransfer?(vm.dashboard.gridOptions.dragGadgetType?vm.dashboard.gridOptions.dragGadgetType:'livehtml'):e.dataTransfer.getData("type"));
        var id = (!e.dataTransfer?null:e.dataTransfer.getData("gid"));
        var title = (!e.dataTransfer?null:e.dataTransfer.getData("title"));
        var config = (!e.dataTransfer?null:e.dataTransfer.getData("config"));
        var customType = (!e.dataTransfer?null:e.dataTransfer.getData("customType"));
        var inLine = (!e.dataTransfer?null:e.dataTransfer.getData("inLine"));
       
        if(config){
          config = JSON.parse(config);
        }
        if(!type || type === ''){
          return;
        }
        newElem.id = id || (type + "_" + (new Date()).getTime());
        newElem.content = type;
        newElem.type = type;       
        newElem.header = {
          enable: true,
          title: {
            iconColor: "hsl( 206, 54%, 5%)",
            text: title || (type + "_" + (new Date()).getTime()),
            textColor: "hsl(206,54%,5%)"
          },
          backgroundColor: "hsl(0, 0%, 100%)",
          height: 40
        }
        newElem.backgroundColor ="white";
        newElem.padding = 0;
        newElem.border = {

          color: "hsl(0, 0%, 90%)",
          width: 0,
          radius: 5
        }
        if(!id){
          if(type == 'livehtml'){
            if(!config){
              newElem.content = "<!-- Write your HTML <div></div> and CSS <style></style> here -->\n<!--Focus here and F11 to full screen editor-->";
              newElem.contentcode = "//Write your controller (JS code) code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\nvm.initLiveComponent = function(){\n\n};\n\n//This function will be call when data change. On first execution oldData will be null\nvm.drawLiveComponent = function(newData, oldData){\n\n};\n\n//This function will be call on element resize\nvm.resizeEvent = function(){\n\n}\n\n//This function will be call when element is destroyed\nvm.destroyLiveComponent = function(){\n\n};\n\n//This function will be call when receiving a value from vm.sendValue(idGadgetTarget,data)\nvm.receiveValue = function(data){\n\n};";       
              showAddGadgetTemplateDialog(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard); 
            }
            else{//with config we draw direct the gadget
              var fconfig = Object.assign(newElem, config);
              vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard.push(fconfig);
              utilsService.forceRender($scope);
              window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: fconfig}));
            }
          }else  if(type == 'gadgetfilter'){
            newElem.content = "<!-- Write your HTML <div></div> and CSS <style></style> here -->\n<!--Focus here and F11 to full screen editor-->";
            newElem.contentcode = "//Write your controller (JS code) code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\nvm.initLiveComponent = function(){\n\n};\n\n//This function will be call when data change. On first execution oldData will be null\nvm.drawLiveComponent = function(newData, oldData){\n\n};\n\n//This function will be call on element resize\nvm.resizeEvent = function(){\n\n}\n\n//This function will be call when element is destroyed\nvm.destroyLiveComponent = function(){\n\n};\n\n//This function will be call when receiving a value from vm.sendValue(idGadgetTarget,data)\nvm.receiveValue = function(data){\n\n};";       
            addGadgetFilter(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
          }
          else if(type == 'html5'){         
            addGadgetHtml5(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard); 
          }
          else if(type == 'favoritegadget'){         
            showAddFavoriteGadgetDialog(newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
          } 
          else if(type == 'customgadget'){ //New (Inline or not) of Custom Gadget
           // showAddTemplateDialog(newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
            newTemplateDialog(customType,inLine,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
          }          
          else{ 
            if(!config){     
              //showAddGadgetDialog(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
              newGadgetDialog(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
            }
            else {
              //check  type 
             createGadgetAndAdd(type,newElem,config,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
            }
          }
        }
        else{ //Prevous created favorite, gadget custom, base gadget
          if(type=='favoritegadget'){
            addFavoriteGadget(id,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
          } else if(type == 'customgadget'){   
            addCustomGadget(id,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
          }else{
            addGadgetGeneric(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
          }
        }
      };


      function sendResizeToGadget(item, itemComponent) {
        $timeout(
          function(){
            $scope.$broadcast("$resize", "");
          },100
        );
      }
    };

    vm.checkIndex = function(index){
      return vm.selectedpage === index;
    }

    vm.setIndex = function(index){
      vm.selectedpage = index;
    }


    

    function showUrlParamDialog(parameters){
      function showUrlParamController($scope,__env, $mdDialog, httpService,  parameters) {
        $scope.parameters = parameters;
        $scope.hide = function() {
          $mdDialog.hide();
        };

        
        $scope.save = function() {
          var sPageURL = $window.location.pathname;	
          var url = urlParamService.generateUrlWithParam(sPageURL,$scope.parameters);
          $window.location.href = url;          	
        };
       
      }
      $mdDialog.show({
        controller: showUrlParamController,
        templateUrl: 'app/partials/edit/formUrlparamMandatoryDialog.html',
        parent: angular.element(document.body),
        clickOutsideToClose:false,
        fullscreen: true, // Only for -xs, -sm breakpoints.
        openFrom: '.sidenav-fab',
        closeTo: angular.element(document.querySelector('.sidenav-fab')),
        locals: {
          parameters: parameters
        }
      })
      .then(function() {
     
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    }



  }
})();
