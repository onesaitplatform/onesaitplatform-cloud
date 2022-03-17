(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('element', {
      templateUrl: 'app/components/view/elementComponent/element.html',
      controller: ElementController,
      controllerAs: 'vm',
      bindings:{
        element: "=",
        iframe: "=",
        editmode: "<",
        eventedit: "=",
        editbuttonsiframe:"<?",
        showfavoritesg:"<?"
      }
    });

  /** @ngInject */
  function ElementController($compile,$log, $scope, $mdDialog, $sce, $rootScope, $timeout, interactionService,filterService,$mdSidenav,utilsService, httpService, __env) {
    var vm = this;
    vm.isMaximized = false;
    vm.datastatus;
 
    //Contains the information of the filters
  
  //  vm.config=[{id:"filtro1",type:"numberfilter", field:"Helsinki.year",name:"year",op:">",typeAction:"filter",initialFilter:false,useLastValue:true,filterChaining:false,targetList:[{gadgetId:"livehtml_1550073936906",overwriteField:"Helsinki.year"},{gadgetId:"livehtml_1549895094697",overwriteField:"Helsinki.year"}],value:2000},
   //            {id:"filtro2",type:"textfilter", field:"Helsinki.population",name:"population",op:">",typeAction:"action",initialFilter:false,useLastValue:true,filterChaining:false,targetList:[{gadgetId:"livehtml_1550073936906",overwriteField:"Helsinki.year"}],value:""}];
    //vm.config=[{"type":"textfilter"}];
     
    

    vm.$onInit = function () {
      //Base images urls
      vm.baseimg = __env.endpointControlPanel;
      //Initialice filters      
      vm.config = vm.element.filters;
      
      if(typeof vm.element.hideBadges ==='undefined'){
        vm.element.hideBadges=true;
      }
      if(typeof vm.element.notshowDotsMenu ==='undefined'){
        vm.element.notshowDotsMenu=false;
      }
      if(typeof vm.element.nomargin ==='undefined'){
        vm.element.nomargin=false;
      }
      if(typeof vm.element.customMenuOptions ==='undefined'){
        vm.element.customMenuOptions=[];
      }
      /** Custom menuoptiom structure example*/ 
      /** vm.element.customMenuOptions=[{id:'optionm',description:'Optionm',imagePath:'/controlpanel/static/images/dashboards/style.svg',position:'menu'}, */
      /** {id:'optionh',description:'Optionh',imagePath:'/controlpanel/static/images/dashboards/style.svg',position:'header'}];*/
  
      inicializeIncomingsEvents(); 
      //Added config filters to interactionService hashmap      
      interactionService.registerGadgetFilters(vm.element.id,vm.config);       
      $timeout(
        function(){
          vm.reloadFilters();
        },1
      );  
      
    };

    vm.openMenu = function($mdMenu){
      $mdMenu.open();
    }

    

    vm.elemntbodyclass = function(){     
     var temp =''+vm.element.id+' '+vm.element.type;
      if(vm.element.header.enable === true ) {
        temp +=' '+'headerMargin';
        if(vm.element.hideBadges === true ) {
          temp +=' '+'withoutBadgesAndHeader';
         }else{
          temp +=' '+'withBadgesAndHeader';
         }
     }else{
        temp +=' '+'noheaderMargin';
        if(vm.element.hideBadges === true ) {
          temp +=' '+'withoutBadges';
         }else{
          temp +=' '+'withBadges';
         }

        }
        if(vm.element.type === 'livehtml'){
          if(vm.element.nomargin){
            temp +=' '+'livehtmlfull';
          }else{
            temp +=' '+'livehtmlnotfull';
          }
        }
    
   return temp;
    }


vm.showHideEditButton = function(){
  var result =  vm.editmode && ((!vm.iframe || (vm.iframe &&  vm.editbuttonsiframe.editGadgetButton) ) || vm.eventedit) && (vm.element.type == 'livehtml' ||  vm.element.type == 'vuetemplate' ||  vm.element.type == 'reacttemplate');
return result;
}    

vm.elemntbadgesclass = function(){     
  var temp ='';
  if(vm.element.header.enable === false ) {
    if(vm.editmode === true ) {
      temp +=' '+'badgesMarginRightEditMode';
    }else{
      temp +=' '+'badgesMarginRightNoEditMode';
    }
  }
  return temp;
 }



    function inicializeIncomingsEvents(){
      $scope.$on("global.style",
        function(ev,style){
          angular.merge(vm.element,vm.element,style);
        }
      );   
    }

    vm.sendCustomMenuOption = function (id){
      vm.emitToTargets(vm.element.id,id);
    }

    vm.emitToTargets = function(id,data){
      //pendingDatasources
      $rootScope.$broadcast(id,
        {
          type: "customOptionMenu",
          data: data
        }
      );
    }


    vm.openEditGadgetIframe = function(ev) {
      if(vm.eventedit){
        vm.sendSelectEvent(vm.element);
        return;
      }   
      $mdDialog.show({
        parent: angular.element(document.body),
        targetEvent: ev,
        fullscreen: false,
        template:
          '<md-dialog id="dialogCreateGadget"  aria-label="List dialog">' +
          '  <md-dialog-content >'+
          '<iframe id="iframeCreateGadget" style=" height: 80vh; width: 80vw;" frameborder="0" src="'+__env.endpointControlPanel+'/gadgets/updateiframe/'+vm.element.id+'"+></iframe>'+                     
          '  </md-dialog-content>' +             
          '</md-dialog>',
          locals: {
            element: vm.element
          },
        controller: DialogIframeEditGadgetController
     });
     function DialogIframeEditGadgetController($scope, $mdDialog, element) {
       $scope.element = element;
       $scope.closeDialog = function () {
         var gadgets = document.querySelectorAll('gadget');
         if (gadgets.length > 0) {
           for (var index = 0; index < gadgets.length; index++) {
             var gad = gadgets[index];
             angular.element(gad).scope().$$childHead.reloadContent();
           }
         }
         var datadiscoverys = document.querySelectorAll('datadiscovery');
         if (datadiscoverys.length > 0) {
           for (var index = 0; index < datadiscoverys.length; index++) {
             var gad = datadiscoverys[index];
             angular.element(gad).isolateScope().vm.ds = null;
             angular.element(gad).isolateScope().reloadContent();
           }
         }
         $mdDialog.hide();
       }
      };


     };

     // toggle gadget to fullscreen and back.
     vm.toggleFullScreen = function(){               
      vm.isMaximized = !vm.isMaximized;
      var gridster = document.getElementsByTagName('gridster')[0];
      //change overflow-y gridster 
      if(vm.isMaximized){
        gridster.style.overflowY = 'hidden';
        gridster.style.overflowX = 'hidden';
        gridster.scrollTop = 0;
      }else{
        gridster.style.overflowY = 'auto';
        gridster.style.overflowX = 'auto';
        gridster.scrollTop = 0;
      }
      $timeout(
         function(){
           $scope.$broadcast("$resize", "");
         },300
       );
    };


     vm.reloadFilters = function(){
      var idNoSpaces = vm.element.id;
      idNoSpaces = idNoSpaces.replace(new RegExp(" ", "g"), "\\ ");      
      angular.element( document.querySelector( '#_'+idNoSpaces+'filters' ) ).empty();
      angular.element(document.getElementById('_'+vm.element.id+'filters')).append($compile('<filter id="vm.element.id" datasource="vm.element.datasource" config="vm.config" hidebuttonclear="vm.element.hidebuttonclear" buttonbig="false"></filter> ')($scope));      
      angular.element( document.querySelector( '#__'+idNoSpaces+'filters' ) ).empty();
      angular.element(document.getElementById('__'+vm.element.id+'filters')).append($compile('<filter id="vm.element.id" datasource="vm.element.datasource" config="vm.config" hidebuttonclear="vm.element.hidebuttonclear" buttonbig="false"></filter> ')($scope));      
     }

     vm.showfiltersInModal = function (){
      //if iframe show on menu
      if(vm.element.type==='gadgetfilter'){
        return false
      }
      if( vm.element.filtersInModal === true 
            && vm.config!=null 
            && showFilters(vm.config)){             
        return true;
      }      
      return false;
     }

     vm.showFiltersInBody = function (){
        //hide when is a gadget filter 
      if(vm.element.type==='gadgetfilter'){
         return false
       }
        //if iframe show on menu
        if((typeof vm.element.filtersInModal === 'undefined' || vm.element.filtersInModal === false) 
              && vm.config!=null 
              && showFilters(vm.config)){         
          return true;
        }     
        return false;
     }


     function showFilters(config){ 
      if(config.length>0){
        for (var index = 0; index < config.length; index++) {
          var element = config[index];
            if(typeof element.hide === 'undefined' || element.hide === false){
              return true;
            }          
        }
      }
        return false;      
     }


    vm.openEditContainerDialog = function (ev) {
      $mdDialog.show({
        controller: EditContainerDialog,
        templateUrl: 'app/partials/edit/editContainerDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:false,
        multiple : true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        locals: {
          element: vm.element
        }
      })
      .then(function(answer) {
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    function EditContainerDialog($scope, $mdDialog,utilsService, element) {
      $scope.icons = utilsService.icons;

      $scope.element = element;

      $scope.queryIcon = function (query) {
        return query ? $scope.icons.filter( createFilterFor(query) ) : $scope.icons;
      }

      /**
       * Create filter function for a query string
       */
      function createFilterFor(query) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(icon) {
          return (icon.indexOf(lowercaseQuery) != -1);
        };
      }

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };
    }

    function EditGadgetDialog($scope, $timeout,$mdDialog,  element, contenteditor, httpService) {
      $scope.initMonaco = function(){
        vm.VSHTML = monaco.editor.create(document.querySelector("#htmleditor"), {
          value: contenteditor.html,
          language: 'html',
          readOnly: false,
          scrollBeyondLastLine: false,
          theme: "vs-dark",
          automaticLayout: true
        });
  
        vm.VSJS = monaco.editor.create(document.querySelector("#jseditor"), {
          value: contenteditor.js,
          language: 'javascript',
          readOnly: false,
          scrollBeyondLastLine: false,
          theme: "vs-dark",
          automaticLayout: true
        });

        $scope.initFullScreen("jseditor",vm.VSJS);
        $scope.initFullScreen("htmleditor",vm.VSHTML);

        vm.VSJS.onDidChangeModelContent(function() {
          $scope.contenteditor.js = vm.VSJS.getValue();
          if($scope.livecompilation){
            $scope.compileJS();
          }
          utilsService.forceRender($scope);
        })
  
        vm.VSHTML.onDidChangeModelContent(function() {
          $scope.contenteditor.html = vm.VSHTML.getValue();   
          if($scope.livecompilation){
            $scope.compileHTML();
          }
          utilsService.forceRender($scope);
        })
      }

      vm.fullScreenControl = {"jseditor":false,"htmleditor":false};

      $scope.initFullScreen = function(id,editorObject){

        function toggleEditor() {
        	if(!vm.fullScreenControl[id]){
	        	document.getElementById(id).style.maxWidth = "100%";
	            document.getElementById(id).style.maxHeight = "100%";
	            document.getElementById(id).style.height = "100%";
	            document.getElementById(id).style.left = "0";
	            document.getElementById(id).style.right = "0";
	            document.getElementById(id).style.top = "0";
	            document.getElementById(id).style.bottom = "0";
	            document.getElementById(id).style.position = "fixed";
	            document.getElementById(id).style.zIndex = "1000";
	            vm.fullScreenControl[id] = true;
        	}
        	else{
        		document.getElementById(id).style.maxWidth = "";
	            document.getElementById(id).style.maxHeight = "";
	            document.getElementById(id).style.height = "400px";
	            document.getElementById(id).style.left = "";
	            document.getElementById(id).style.right = "";
	            document.getElementById(id).style.top = "";
	            document.getElementById(id).style.bottom = "";
	            document.getElementById(id).style.position = "";
	            document.getElementById(id).style.zIndex = "";
	            vm.fullScreenControl[id]=false;
          }
          if(vm.fullScreenControl[id]){
            document.getElementsByTagName("md-dialog")[0].style.maxWidth = "100%";
            document.getElementsByTagName("md-dialog")[0].style.maxHeight = "100%";
            document.getElementsByTagName("md-dialog")[0].style.left = "0";
            document.getElementsByTagName("md-dialog")[0].style.right = "0";
            document.getElementsByTagName("md-dialog")[0].style.top = "0";
            document.getElementsByTagName("md-dialog")[0].style.bottom = "0";
            document.getElementsByTagName("md-dialog")[0].style.position = "fixed";
          }
          else{
            document.getElementsByTagName("md-dialog")[0].style.maxWidth = "";
            document.getElementsByTagName("md-dialog")[0].style.maxHeight = "";
            document.getElementsByTagName("md-dialog")[0].style.left = "";
            document.getElementsByTagName("md-dialog")[0].style.right = "";
            document.getElementsByTagName("md-dialog")[0].style.top = "";
            document.getElementsByTagName("md-dialog")[0].style.bottom = "";
            document.getElementsByTagName("md-dialog")[0].style.position = "";
          }
        }

        editorObject.addCommand(monaco.KeyCode.F10, toggleEditor);
        editorObject.addCommand(monaco.KeyCode.F11, toggleEditor);
        
        editorObject.addCommand(monaco.KeyCode.Escape, function() {
        	document.getElementById(id).style.maxWidth = "";
            document.getElementById(id).style.maxHeight = "";
            document.getElementById(id).style.height = "400px";
            document.getElementById(id).style.left = "";
            document.getElementById(id).style.right = "";
            document.getElementById(id).style.top = "";
            document.getElementById(id).style.bottom = "";
            document.getElementById(id).style.position = "";
            document.getElementById(id).style.zIndex = "";
            vm.fullScreenControl[id]=false;
            document.getElementsByTagName("md-dialog")[0].style.maxWidth = "";
            document.getElementsByTagName("md-dialog")[0].style.maxHeight = "";
            document.getElementsByTagName("md-dialog")[0].style.left = "";
            document.getElementsByTagName("md-dialog")[0].style.right = "";
            document.getElementsByTagName("md-dialog")[0].style.top = "";
            document.getElementsByTagName("md-dialog")[0].style.bottom = "";
            document.getElementsByTagName("md-dialog")[0].style.position = "";
        });
      }

      $scope.contenteditor = contenteditor;
      
      $scope.livecompilation = false;

      $scope.element = element;

      $scope.compileHTML = function(){
        if(typeof $scope.contenteditor.html !== 'undefined'){
          $scope.element.content = vm.VSHTML.getValue();
        }else{
          $scope.element.content = "";
        }
      }

      $scope.compileJS = function(){
        if(typeof $scope.contenteditor.js !== 'undefined'){
          $scope.element.contentcode = vm.VSJS.getValue();
        }else{
          $scope.element.contentcode = "";
        }
      }

      $scope.compile = function(){ 
        $scope.contenteditor.html = vm.VSHTML.getValue();   
        $scope.contenteditor.js = vm.VSJS.getValue();
        $scope.compileHTML();
        $scope.compileJS();
      }

      $scope.saveAsTemplate = function(){
        $scope.contenteditor.html = vm.VSHTML.getValue();   
        $scope.contenteditor.js = vm.VSJS.getValue();
      }

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };

      $scope.datasources = [];

      $scope.loadDatasources = function(){
        return httpService.getDatasources().then(
          function(response){
            $scope.datasources=response.data;
          },
          function(e){
            console.log("Error getting datasources: " +  JSON.stringify(e))
          }
        );
      };
      $scope.loadDatasources();
    }

    vm.openEditGadgetDialog = function (ev) {
      if(vm.eventedit){
        vm.sendSelectEvent(vm.element);
        return;
      }
      if(!vm.contenteditor){
        vm.contenteditor = {}
        vm.contenteditor["html"] = vm.element.content.slice();
        vm.contenteditor["js"] = (vm.element.contentcode?vm.element.contentcode.slice():"");
      }
      $mdDialog.show({
        controller: EditGadgetDialog,
        templateUrl: 'app/partials/edit/editGadgetDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        onComplete: function($scope){
          $scope.initMonaco();
        },
        multiple : true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
       
        locals: {
          element: vm.element,
          contenteditor: vm.contenteditor
        }
      })
      .then(function(answer) {
       
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    
    };

    vm.openEditTemplateParamsDialog = function (ev) {
      if(vm.eventedit){
        vm.sendSelectEvent(vm.element);
        return;
      }
      httpService.getGadgetTemplateByIdentification(vm.element.template).then(
        function(data){
          vm.contenteditor = {}
          vm.contenteditor["content"] = data.data.template;
          vm.contenteditor["contentcode"] = data.data.templateJS;

          $mdDialog.show({
            controller: 'editTemplateParamsController',
            templateUrl: 'app/partials/edit/addGadgetTemplateParameterDialog.html',
            parent: angular.element(document.body),
            targetEvent: ev,
            clickOutsideToClose:true,
            multiple : true,
            fullscreen: false, // Only for -xs, -sm breakpoints.
            locals: {
              type: vm.element.type,
              config: vm.contenteditor,
              element: vm.element,
              layergrid: null,
              edit: true
            }
          })
          .then(function(answer) {
           
          }, function() {
            $scope.status = 'You cancelled the dialog.';
          });
        }
      )
    };

    function EditGadgetHTML5Dialog($timeout,$scope, $mdDialog, contenteditor, element) {
      $scope.editor;
      
      $scope.element = element;

      $scope.initMonaco = function(){
        vm.VSHTML = monaco.editor.create(document.querySelector("#htmleditor"), {
          value: contenteditor.html,
          language: 'html',
          readOnly: false,
          scrollBeyondLastLine: false,
          theme: "vs-dark",
          automaticLayout: true
        });

        $scope.initFullScreen("htmleditor",vm.VSHTML);
  
        vm.VSHTML.onDidChangeModelContent(function() {
          $scope.contenteditor.html = vm.VSHTML.getValue();   
          if($scope.livecompilation){
            $scope.compileHTML();
          }
          utilsService.forceRender($scope);
        })
      }

      $scope.initFullScreen = function(id,editorObject){

        function toggleEditor() {
        	if(!vm.fullScreenControl[id]){
	        	document.getElementById(id).style.maxWidth = "100%";
	            document.getElementById(id).style.maxHeight = "100%";
	            document.getElementById(id).style.height = "100%";
	            document.getElementById(id).style.left = "0";
	            document.getElementById(id).style.right = "0";
	            document.getElementById(id).style.top = "0";
	            document.getElementById(id).style.bottom = "0";
	            document.getElementById(id).style.position = "fixed";
	            document.getElementById(id).style.zIndex = "1000";
	            vm.fullScreenControl[id] = true;
        	}
        	else{
        		document.getElementById(id).style.maxWidth = "";
	            document.getElementById(id).style.maxHeight = "";
	            document.getElementById(id).style.height = "400px";
	            document.getElementById(id).style.left = "";
	            document.getElementById(id).style.right = "";
	            document.getElementById(id).style.top = "";
	            document.getElementById(id).style.bottom = "";
	            document.getElementById(id).style.position = "";
	            document.getElementById(id).style.zIndex = "";
	            vm.fullScreenControl[id]=false;
          }
          if(vm.fullScreenControl[id]){
            document.getElementsByTagName("md-dialog")[0].style.maxWidth = "100%";
            document.getElementsByTagName("md-dialog")[0].style.maxHeight = "100%";
            document.getElementsByTagName("md-dialog")[0].style.left = "0";
            document.getElementsByTagName("md-dialog")[0].style.right = "0";
            document.getElementsByTagName("md-dialog")[0].style.top = "0";
            document.getElementsByTagName("md-dialog")[0].style.bottom = "0";
            document.getElementsByTagName("md-dialog")[0].style.position = "fixed";
          }
          else{
            document.getElementsByTagName("md-dialog")[0].style.maxWidth = "";
            document.getElementsByTagName("md-dialog")[0].style.maxHeight = "";
            document.getElementsByTagName("md-dialog")[0].style.left = "";
            document.getElementsByTagName("md-dialog")[0].style.right = "";
            document.getElementsByTagName("md-dialog")[0].style.top = "";
            document.getElementsByTagName("md-dialog")[0].style.bottom = "";
            document.getElementsByTagName("md-dialog")[0].style.position = "";
          }
        }

        editorObject.addCommand(monaco.KeyCode.F10, toggleEditor);
        editorObject.addCommand(monaco.KeyCode.F11, toggleEditor);
        
        editorObject.addCommand(monaco.KeyCode.Escape, function() {
        	document.getElementById(id).style.maxWidth = "";
            document.getElementById(id).style.maxHeight = "";
            document.getElementById(id).style.height = "400px";
            document.getElementById(id).style.left = "";
            document.getElementById(id).style.right = "";
            document.getElementById(id).style.top = "";
            document.getElementById(id).style.bottom = "";
            document.getElementById(id).style.position = "";
            document.getElementById(id).style.zIndex = "";
            vm.fullScreenControl[id]=false;
            document.getElementsByTagName("md-dialog")[0].style.maxWidth = "";
            document.getElementsByTagName("md-dialog")[0].style.maxHeight = "";
            document.getElementsByTagName("md-dialog")[0].style.left = "";
            document.getElementsByTagName("md-dialog")[0].style.right = "";
            document.getElementsByTagName("md-dialog")[0].style.top = "";
            document.getElementsByTagName("md-dialog")[0].style.bottom = "";
            document.getElementsByTagName("md-dialog")[0].style.position = "";
        });
      }

      $scope.compileHTML = function(){
        if(typeof $scope.contenteditor.html !== 'undefined'){
          $scope.element.content = vm.VSHTML.getValue();
        }else{
          $scope.element.content = "";
        }
      }

      $scope.contenteditor = contenteditor;

      $scope.livecompilation = false;

      $scope.compile = function(){ 
        $scope.contenteditor.html = vm.VSHTML.getValue();   
        $scope.compileHTML();
      }

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };

    }


    vm.openEditGadgetHTML5Dialog = function (ev) {
      if(vm.eventedit){
        vm.sendSelectEvent(vm.element);
        return;
      }
      if(!vm.contenteditor){
        vm.contenteditor = {}
        vm.contenteditor["html"] = vm.element.content.slice();
      }
      $mdDialog.show({
        controller: EditGadgetHTML5Dialog,
        templateUrl: 'app/partials/edit/editGadgetHTML5Dialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        onComplete: function($scope){
          $scope.initMonaco();
        },
        multiple : true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
       
        locals: {
          element: vm.element,
          contenteditor: vm.contenteditor
        }
      })
      .then(function(answer) {
       
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    vm.trustHTML = function(html_code) {
      return $sce.trustAsHtml(html_code)
    }

    vm.calcHeight = function(){
      vm.element.header.height = (vm.element.header.height=='inherit'?25:vm.element.header.height);     
     var result = "'calc(100% - 36px)'";


      return result;
    }


    vm.toggleRight =  function(componentId) {
      $mdSidenav(componentId).toggle();
    };
    
    
    
    
    
    vm.deleteElement = function(){
      $rootScope.$broadcast("deleteElement",vm.element);
    }

    vm.generateFilterInfo = function(filter){ 
      return filter.value;
    }

    vm.deleteFilter = function(id, field,op){      
      $rootScope.$broadcast(vm.element.id,{id: id,type:'filter',data:[],field:field,op:op})
    }




    vm.openFilterDialog = function(ev) {     
      $mdDialog.show({
        parent: angular.element(document.body),
        targetEvent: ev,
        scope: $scope,
        preserveScope: true, 
        fullscreen: false,
        template:
          '<md-dialog flex="35"  aria-label="List dialog" style="min-width:440px">' +
          '<form ng-cloak>'+
          '<md-toolbar style="background-color:rgba(255,255,255,0.87);  position: absolute; top: 0;right: 0;">' +
          '<div class="md-toolbar-tools">' +
          '<span flex="" class="flex"></span>'+
          '<button type="button" aria-label="Close" class="ods-dialog__headerbtn" ng-click="closeDialog()"><span class="ods-dialog__close ods-icon ods-icon-close"></span></button>'+           
          '</div>' +
       ' </md-toolbar>' +
          '  <md-dialog-content style="padding: 30px 30px 10px;" >'+
          ' <filter id="vm.element.id" datasource="vm.element.datasource" config="vm.config" hidebuttonclear="vm.element.hidebuttonclear" buttonbig="true"></filter>'+  
          '  </md-dialog-content>' + 
          '</form>'+           
          '</md-dialog>',
         
        controller: function DialogController($scope, $mdDialog) {
      
          $scope.closeDialog = function() {
            $mdDialog.hide();
          }
        }
     });
    

     };


     vm.openEditFilterDialog = function (ev) {
     filterService.cleanAllFilters(vm.element.id,vm.element.filters);
     interactionService.unregisterGadgetFilter(vm.element.id);   
      $mdDialog.show({
        controller: EditFilterDialog,
        templateUrl: 'app/partials/edit/editFilterDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:false,
        multiple : false,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        locals: {
          element: vm.element
        }
      })
      .then(function(answer) {        
        interactionService.registerGadgetFilters(vm.element.id,vm.element.filters);        
        vm.config = vm.element.filters;
        vm.reloadFilters();
      }, function() {
        $scope.status = 'You cancelled the dialog.';             
        interactionService.registerGadgetFilters(vm.element.id,vm.element.filters);        
        vm.config = vm.element.filters;
        vm.reloadFilters();
      
      });
    };

    function EditFilterDialog($scope, $mdDialog,utilsService,httpService, element,gadgetManagerService) {
     

      $scope.element = element; 
     
      $scope.tempFilter = {data:{ds:""},typeAction: "filter"};
      $scope.typeList = [
                          {id:'textfilter',description:'text filter'},
                          {id:'numberfilter',description:'numerical filter'},
                          {id:'livefilter',description:'Date range and real time filter'},
                          {id:'intervaldatefilter',description:'Date range filter'},
                          {id:'intervaldatestringfilter',description:'Date range filter no conversion to timestamp'},                                                    
                          {id:'simpleselectdsfilter',description:'text filter with simple-selection from datasource'},
                          {id:'simpleselectnumberdsfilter',description:'numerical filter with simple-selection from datasource'},
                          {id:'multiselectdsfilter',description:'text filter with multi-selection from datasource'},
                          {id:'multiselectnumberdsfilter',description:'numerical filter with multi-selection from datasource'},
                          {id:'simpleselectfilter',description:'text filter with simple-selection'},
                          {id:'simpleselectnumberfilter',description:'numerical filter with simple-selection'},
                          {id:'multiselectfilter',description:'text filter with multi-selection'},                         
                          {id:'multiselectnumberfilter',description:'numerical filter with multi-selection'}
                        ];
      
                        
      $scope.opList = [
        {id:'=',description:'='},
        {id:'>',description:'>'},
        {id:'<',description:'<'},
        {id:'<=',description:'<='},
        {id:'>=',description:'>='},
        {id:'<>',description:'<>'}
      ];
     
      
     
      $scope.hideLabelName = true;
      $scope.hideOperator = true;
      $scope.hideValue = true;
      $scope.hideOptions = true; 
      $scope.hideDatasource = true; 
     generateGadgetsLists();

     function generateGadgetsLists(){
     
      $scope.gadgetsTargets = getGadgetsInDashboard();
      refreshGadgetTargetFields($scope.element.id);
    }


    $scope.datasources = [];
    $scope.datasourcesSelected ="";
    $scope.loadDatasources = function(){
      return httpService.getDatasources().then(
        function(response){
          $scope.datasources=response.data;
          
        },
        function(e){
          console.log("Error getting datasources: " +  JSON.stringify(e))
        }
      );
    };
    $scope.loadDatasources();

   




    $scope.setdsTargetFields = function (){  
        
      if(typeof $scope.tempFilter!=='undefined'&& $scope.tempFilter !=null 
      && typeof $scope.tempFilter.data!=='undefined'&& $scope.tempFilter.data !=null
      && typeof $scope.tempFilter.data.ds!=='undefined'&& $scope.tempFilter.data.ds !=null ){

        var idDs= findDsId($scope.tempFilter.data.ds);
        httpService.getFieldsFromDatasourceId(idDs).then(
        function(data){
          
          $scope.dsTargetFields = utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", []));
        }
      )
      }
    }

    $scope.dsTargetFields = [];
    $scope.setdsTargetFields();

    function findDsId(identification){
     var elem = $scope.datasources.find( function(element){
        return element.identification === identification;
      });
      if(typeof elem !=='undefined'){
        return elem.id;
      }

    }

    //Generate gadget list of posible Sources of interactions: pie, bar, livehtml
    function getGadgetsInDashboard(){
      return gadgetManagerService.returnGadgets();
    }


    function findGadgetInDashboard(gadgetId){    
        return gadgetManagerService.findGadgetById(gadgetId)
    }

    $scope.generateGadgetInfo = function (gadgetId){
      var gadget = findGadgetInDashboard(gadgetId);
      if(gadget == null){
        return gadgetId;
      }
      else{
        return $scope.prettyGadgetInfo(gadget);
      }
    }


    

    $scope.deleteOption = function (opt){
      var options = $scope.tempFilter.data.options;
      var optionsDescription = $scope.tempFilter.data.optionsDescription;

          for(var i=0;i<options.length;i++){
            if(options[i] === opt ){
              options.splice(i, 1);
              optionsDescription.splice(i, 1);
              break;
            }
          }
    }

    $scope.addOption = function (opt,description){
      if(typeof $scope.tempFilter.data ==='undefined' ){
        $scope.tempFilter.data = {};
      }
      var addedOption = false;

      if( typeof $scope.tempFilter.data.options ==='undefined' || $scope.tempFilter.data.options === null){
        $scope.tempFilter.data.options = [];
        $scope.tempFilter.data.options[0] = opt;
        addedOption=true;
      }else{
        var find = false;
        for(var i=0;i<$scope.tempFilter.data.options.length;i++){
          if($scope.tempFilter.data.options[i] === opt ){            
            find=true;
          }
        }
        if(!find){
          $scope.tempFilter.data.options.push(opt); 
          addedOption = true;
        }
      }
     if(addedOption){
        if( typeof $scope.tempFilter.data.optionsDescription ==='undefined' || $scope.tempFilter.data.optionsDescription === null){
          $scope.tempFilter.data.optionsDescription = []
          $scope.tempFilter.data.optionsDescription[0] = description;
        }else{         
            $scope.tempFilter.data.optionsDescription.push(description); 
          }
        }
      
    }

    

    $scope.deleteFilter = function (id){
      if(typeof $scope.element.filters !=='undefined' && $scope.element.filters!= null  ){
        for (var index = 0; index < $scope.element.filters.length; index++) {         
          if($scope.element.filters[index].id === id){           
            $scope.element.filters.splice(index, 1);            
            return null;
          }          
        }
      }
    }

    

    $scope.editFilter = function (id){
      if(typeof $scope.element.filters !=='undefined' && $scope.element.filters !== null ){
        for (var index = 0; index < $scope.element.filters.length; index++) {         
          if($scope.element.filters[index].id === id){
            
            $scope.tempFilter = makeFilter( $scope.element.filters[index],true);
            //update
            $scope.hideFields($scope.tempFilter.type);        
            return null;
          }          
        }
      }
    }
    


   

    $scope.addFilter = function(){
      //validations
      var tempFilter = $scope.tempFilter;
     
      tempFilter.typeAction = "filter";
      var targetGadgetField = $scope.targetGadgetField;

      if(typeof tempFilter.id ==='undefined' || (typeof tempFilter.id !=='undefined' && tempFilter.id.length===0)){
        //identifier mandatory
        return null;
      }
     
      if(typeof tempFilter.type ==='undefined' || (typeof tempFilter.type !=='undefined' && tempFilter.type.length===0)){
        //type mandatory
        return null;
      }
      if(typeof tempFilter.name ==='undefined' || (typeof tempFilter.name !=='undefined' && tempFilter.name.length===0)){
      
        tempFilter.name="";

      }
      if(tempFilter.typeAction==='filter'&&( tempFilter.type ==='textfilter' || tempFilter.type==='numberfilter' )){
        if( typeof tempFilter.op ==='undefined' || (typeof tempFilter.op !=='undefined' && tempFilter.op.length===0)){
          //   op mandatory 
          return null;
        }
      }    

      if(typeof targetGadgetField ==='undefined' || targetGadgetField===null || targetGadgetField.trim().length === 0){
        //targetList mandatory
        return null;
      }

      tempFilter.targetList=[{
        "gadgetId": $scope.element.id,
        "overwriteField": targetGadgetField
      }];

        //update for id 
      if(typeof $scope.element.filters !=='undefined' &&  $scope.element.filters != null){
        for (var index = 0; index < $scope.element.filters.length; index++) {
          var elem = $scope.element.filters[index];
          if(elem.id === tempFilter.id){           
            $scope.element.filters[index] = makeFilter(tempFilter,false) ;
            return null;
          }          
        }
      }
      if(typeof  $scope.element.filters === 'undefined' ||   $scope.element.filters == null){
        $scope.element.filters = [makeFilter(tempFilter,false)];
      }else{
        $scope.element.filters.push( makeFilter(tempFilter,false) );
      }
    }


function makeFilter(tempFilter,read){
  //load for edit
  if(read){
    var filter = {
      'id':tempFilter.id,
      'type': tempFilter.type,
      'typeAction': tempFilter.typeAction,
      'name':tempFilter.name,
      'op': tempFilter.op,
      'value': tempFilter.value,
      'targetList':tempFilter.targetList,
      'hide':tempFilter.hide,
      'initialFilter':tempFilter.initialFilter,
      'data':tempFilter.data
    };
    $scope.targetGadgetField=tempFilter.targetList[0].overwriteField;
  }
  else{
    //for create new data or update
    for (var index = 0; index < tempFilter.targetList.length; index++) {    
      tempFilter.targetList[index].field = tempFilter.targetList[index].overwriteField;
    }
    if(tempFilter.type === 'multiselectfilter' 
    || tempFilter.type === 'multiselectnumberfilter'
    || tempFilter.type === 'multiselectdsfilter' 
    || tempFilter.type === 'multiselectnumberdsfilter'  ){
      if(typeof tempFilter.data!='undefined' && typeof tempFilter.data.options!='undefined' ){
        tempFilter.data.optionsSelected = tempFilter.data.options.slice();
      }
    }
    if(tempFilter.type === 'simpleselectfilter' 
    || tempFilter.type === 'simpleselectnumberfilter' 
    || tempFilter.type === 'simpleselectdsfilter'
    || tempFilter.type === 'simpleselectnumberdsfilter' ){
      if(typeof tempFilter.data!='undefined' && typeof tempFilter.data.options!='undefined' ){
        tempFilter.data.optionsSelected = tempFilter.value;
      }
    }
    if(tempFilter.type === 'livefilter'){
      tempFilter.data = {
        "options": null,
        "optionsSelected": null,
        "startDate": "NOW(\"yyyy-MM-dd'T'HH:mm:ss'Z'\",\"hour\",-8)",
        "endDate": "NOW(\"yyyy-MM-dd'T'HH:mm:ss'Z'\",\"hour\",0)",
        "selectedPeriod": 8,
        "realtime": "start"
      };
    }
    if(tempFilter.type === 'intervaldatefilter' || tempFilter.type === 'intervaldatestringfilter'){
      tempFilter.data = {
        "options": null,
        "optionsSelected": null,
        "startDate":  moment().subtract(8,'hour').toISOString() ,
        "endDate":  moment().toISOString() 
      };
    }

    var filter = {
      'id':tempFilter.id,
      'typeAction': tempFilter.typeAction,
      'type': tempFilter.type,
      'name':tempFilter.name,
      'op': tempFilter.op,
      'value': tempFilter.value,
      'targetList':tempFilter.targetList,
      'hide':tempFilter.hide,
      'initialFilter':tempFilter.initialFilter,
      'data':tempFilter.data
    };
  }
return filter;
}

    function refreshGadgetTargetFields (gadgetId){
      var gadget = findGadgetInDashboard(gadgetId);
      if(gadget == null){
        $scope.gadgetEmitterFields = [];
      }
      else{
        setGadgetTargetFields(gadget);
      }
    }


 //Destination are all gadget fields
 function setGadgetTargetFields(gadget){        
    $scope.targetDatasource="";
  var gadgetData = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm;
  if(gadget.type === 'livehtml' || gadget.type === 'vuetemplate' || gadget.type === 'reacttemplate'){
    if(typeof gadgetData.datasource!=='undefined'){
      $scope.targetDatasource = gadgetData.datasource.name;
     
    }else{
      $scope.gadgetTargetFields = [];
      return null;
    }
    var dsId = gadgetData.datasource.id;
  } else  if(gadget.type === 'gadgetfilter'){
    if(typeof gadgetData.datasource!=='undefined'){
      $scope.targetDatasource = gadgetData.datasource.name;     
    }else{
      $scope.gadgetTargetFields = [];
      return null;
    }
    var dsId = gadgetData.datasource.id;
  }
  else{
    $scope.targetDatasource = gadgetData.measures[0].datasource.identification;
    var dsId = gadgetData.measures[0].datasource.id;
  }
  httpService.getFieldsFromDatasourceId(dsId).then(
    function(data){
      $scope.gadgetTargetFields = utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", []));
    }
  )
  $scope.gadgetTargetFields = [];
}

 //Get gadget JSON and return string info for UI
 $scope.prettyGadgetInfo = function(gadget){
       
  return gadget.header.title.text + " (" + gadget.type + ")";

}


$scope.queryTargetField = function(query){     
  $scope.targetDatasource="";
var gadgetData = angular.element(document.getElementsByClassName($scope.targetGadget)[0]).scope().$$childHead.vm;
if(gadgetData.type === 'livehtml'){
  if(typeof gadgetData.datasource!=='undefined'){
    $scope.targetDatasource = gadgetData.datasource.name;
   
  }else{
    return [];
   
  }
  var dsId = gadgetData.datasource.id;
} else if(gadgetData.type === 'gadgetfilter'){
  if(typeof gadgetData.datasource!=='undefined'){
    $scope.targetDatasource = gadgetData.datasource.name;
   
  }else{
    return [];
   
  }
  var dsId = gadgetData.datasource.id;
}
else{
  $scope.targetDatasource = gadgetData.measures[0].datasource.identification;
  var dsId = gadgetData.measures[0].datasource.id;
}
httpService.getFieldsFromDatasourceId(dsId).then(
  function(data){
   var gadgetTargetFields = utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", []));
   var result = query ? gadgetTargetFields.filter(createFilterFor(query)) : gadgetTargetFields;
   return result;
  }
)
return  [];
}

    
      /**
       * Create filter function for a query string
       */
      function createFilterFor(query) {
        var lowercaseQuery = query.toLowerCase();  
        return function filterFn(field) {
          return (field.field.toLowerCase().indexOf(lowercaseQuery) === 0); 
        };
  
      }


     
$scope.hideFields = function(type){
  
  if($scope.tempFilter.typeAction==='filter'){

    if(type==='textfilter' ||
      type==='numberfilter'){
      $scope.hideLabelName = false;
      $scope.hideOperator = false;
      $scope.hideValue = false;
      $scope.hideOptions = true; 
      $scope.hideInitialFilter = false;
      $scope.hideHide =false;      
      $scope.hideDatasource = true; 
    }else if(type==='livefilter'){
      $scope.hideLabelName = true;
      $scope.hideOperator = true;
      $scope.hideValue = true;
      $scope.hideOptions = true; 
      $scope.hideInitialFilter = false;
      $scope.hideHide =false;
      $scope.hideDatasource = true; 
    }else if( type==='multiselectfilter' ||
       type==='multiselectnumberfilter'){
      $scope.hideLabelName = false;
      $scope.hideOperator = true;
      $scope.hideValue = true;
      $scope.hideOptions = false; 
      $scope.hideInitialFilter = false;
      $scope.hideHide =false;
      $scope.hideDatasource = true; 
    }else if(type==='simpleselectfilter'
      || type==='simpleselectnumberfilter'){
      $scope.hideLabelName = false;
      $scope.hideOperator = false;
      $scope.hideValue = true;
      $scope.hideOptions = false; 
      $scope.hideInitialFilter = true;
      $scope.hideHide =true;
      $scope.hideDatasource = true; 
    }else if(type==='intervaldatefilter' 
    || type==='intervaldatestringfilter'){
      $scope.hideLabelName = false;
      $scope.hideOperator = true;
      $scope.hideValue = true;
      $scope.hideOptions = true; 
      $scope.hideInitialFilter = false;
      $scope.hideHide =false;
      $scope.hideDatasource = true; 
    }else if(type==='simpleselectdsfilter'||
     type ==='simpleselectnumberdsfilter' ){
      $scope.hideLabelName = false;
      $scope.hideOperator = false;
      $scope.hideValue = true;
      $scope.hideOptions = true; 
      $scope.hideInitialFilter = true;
      $scope.hideHide =true;
      $scope.hideDatasource = false; 
    }else if( type==='multiselectdsfilter' ||
    type==='multiselectnumberdsfilter'){
    $scope.hideLabelName = false;
    $scope.hideOperator = true;
    $scope.hideValue = true;
    $scope.hideOptions = true; 
    $scope.hideInitialFilter = true;
    $scope.hideHide =false;
    $scope.hideDatasource = false;   
    }else{
      
    $scope.hideLabelName = false;
    $scope.hideOperator = true;
    $scope.hideValue = false;
    $scope.hideOptions = true; 
    $scope.hideInitialFilter = true;
    $scope.hideHide =false;
    $scope.hideDatasource = true;   
  }

}


      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };
    }

    $scope.hideFields();

  }
  
    vm.openEditCustomMenuOptionsDialog = function (ev) {
      $mdDialog.show({
        controller: EditCustomMenuOptionsDialog,
        templateUrl: 'app/partials/edit/editCustomMenuOptions.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:false,
        multiple : false,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        locals: {
          element: vm.element
        }
      })
      .then(function(answer) {    
     
      }, function() {
        $scope.status = 'You cancelled the dialog.';             
      
      });
    };


    vm.addFavoriteDialog = function (ev) {
      $mdDialog.show({
        controller: AddFavoriteGadgetDialog,
        templateUrl: 'app/partials/edit/addFavoriteGadgetDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:false,
        multiple : false,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        locals: {
          element: vm.element
        }
      })
      .then(function(answer) {    
     
      }, function() {
        $scope.status = 'You cancelled the dialog.';             
      
      });
    }


    function EditCustomMenuOptionsDialog($scope, $mdDialog, element) {
     

      $scope.element = element;     
      $scope.positionList = [
        {id:'menu',description:'Menu'},
        {id:'header',description:'Header'}
      ];

      $scope.tempMenuOp = {};
     
    $scope.deleteMenuOption = function (id){
      if(typeof $scope.element.customMenuOptions !=='undefined' ){
        for (var index = 0; index < $scope.element.customMenuOptions.length; index++) {         
          if($scope.element.customMenuOptions[index].id === id){           
            $scope.element.customMenuOptions.splice(index, 1);            
            return null;
          }          
        }
      }
    }

    $scope.editMenuOption = function (id){
      if(typeof $scope.element.customMenuOptions !=='undefined' ){
        for (var index = 0; index < $scope.element.customMenuOptions.length; index++) {         
          if($scope.element.customMenuOptions[index].id === id){            
            $scope.tempMenuOp = makeCustomMenuOptions( $scope.element.customMenuOptions[index]);
            return null;
          }          
        }
      }
    }
  
    $scope.addCustomMenuOpt = function(){
      //validations
      var tempMenuOp = $scope.tempMenuOp;     
      if(typeof tempMenuOp.id ==='undefined' || (typeof tempMenuOp.id !=='undefined' && tempMenuOp.id.length===0)){
        //identifier mandatory
        return null;
      }     
      if(typeof tempMenuOp.description ==='undefined' || (typeof tempMenuOp.description !=='undefined' && tempMenuOp.description.length===0)){
        //description mandatory
        return null;
      }     
        //update for id 
      if(typeof $scope.element.customMenuOptions !=='undefined' ){
        for (var index = 0; index < $scope.element.customMenuOptions.length; index++) {
          var elem = $scope.element.customMenuOptions[index];
          if(elem.id === tempMenuOp.id){           
            $scope.element.customMenuOptions[index] = makeCustomMenuOptions(tempMenuOp) ;
            return null;
          }          
        }
      }
      if(typeof  $scope.element.customMenuOptions === 'undefined'){
        $scope.element.customMenuOptions = [makeCustomMenuOptions(tempMenuOp)];
      }else{
        $scope.element.customMenuOptions.push( makeCustomMenuOptions(tempMenuOp) );
      }
    }


function makeCustomMenuOptions(tempCustomMenuOp){
  //load for edit
  
    var customMenuOp = {
      'id':tempCustomMenuOp.id,
      'description': tempCustomMenuOp.description,
      'imagePath': tempCustomMenuOp.imagePath,
      'position':tempCustomMenuOp.position      
    };  
return customMenuOp;
}

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };
      
    }
    //This function is called when push button add favorite gaget 
    function AddFavoriteGadgetDialog($scope, $timeout, $mdDialog, favoriteGadgetService, urlParamService, interactionService, element) {
      $scope.element = element;
      $scope.showAlert = false;
      $scope.isOK = "alertOK";
      $scope.identifier =  element.header.title.text || "" ;
      $scope.saveconnections = true;
      $scope.message = "";
      $scope.addFavoriteGadget = function () {
          var data = {};
          data.identification = $scope.identifier;
          data.idDatasource = null
          data.idGadget = null;
          data.idGadgetTemplate = null
          data.config = null;
          data.type = $scope.element.type;
          var config = {};
          if (data.type == "livehtml" || data.type == "gadgetfilter") {
            if ($scope.element.template) {
              data.idGadgetTemplate = $scope.element.template;
              config.params = $scope.element.params;
            }
            config.subtype = $scope.element.subtype;
            config.content = $scope.element.content;
            config.contentcode = $scope.element.contentcode;
            config.customMenuOptions = $scope.element.customMenuOptions;
            config.backgroundColor = $scope.element.backgroundColor;
            config.header = $scope.element.header;
            config.cols = $scope.element.cols;
            config.rows = $scope.element.rows;
            config.border = $scope.element.border;
            config.hideBadges = $scope.element.hideBadges;
            config.nomargin = $scope.element.nomargin;
            config.notshowDotsMenu = $scope.element.notshowDotsMenu;
            config.padding = $scope.element.padding;
            if ($scope.element.datasource) {
              //map name because need identifier not id
              data.idDatasource = $scope.element.datasource.name;
              config.datasource = $scope.element.datasource;
            }
          } else if (data.type == "html5") {
            config.content = $scope.element.content;
            config.customMenuOptions = $scope.element.customMenuOptions;
            config.backgroundColor = $scope.element.backgroundColor;
            config.header = $scope.element.header;
            config.cols = $scope.element.cols;
            config.rows = $scope.element.rows;
            config.border = $scope.element.border;
            config.hideBadges = $scope.element.hideBadges;
            config.nomargin = $scope.element.nomargin;
            config.notshowDotsMenu = $scope.element.notshowDotsMenu;
            config.padding = $scope.element.padding;
          } else {
            data.idGadget = $scope.element.id;
            config.content = $scope.element.content;
            config.customMenuOptions = $scope.element.customMenuOptions;
            config.backgroundColor = $scope.element.backgroundColor;
            config.header = $scope.element.header;
            config.cols = $scope.element.cols;
            config.rows = $scope.element.rows;
            config.border = $scope.element.border;
            config.hideBadges = $scope.element.hideBadges;
            config.nomargin = $scope.element.nomargin;
            config.notshowDotsMenu = $scope.element.notshowDotsMenu;
            config.padding = $scope.element.padding;
          }
          if ($scope.saveconnections) {
            config.urlparams = urlParamService.geturlParamHashForTargetGadget($scope.element.id);
            config.datalinks = interactionService.getInteractionHashForTargetGadget($scope.element.id);
          }
          data.config = JSON.stringify(config);
          favoriteGadgetService.create(data).then(function (result) {
            console.log(result);
            if (result.status == "ok") {
              $scope.showAlert = true;
              $scope.isOK = "alertOK";
              $scope.message = result.message;
              $timeout(function () {
                $scope.hide()
              }, 1000);
            } else if (result.status == "error") {
              $scope.showAlert = true;
              $scope.isOK = "alertError";
              $scope.message = result.message;
            }
          });
      }
      
      $scope.validateImputIdentifier = function() {
        return !($scope.identifier!=null && $scope.identifier.trim().length>0);
      } 
      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };
      
    }





    vm.sendSelectEvent = function(element){
      window.dispatchEvent(new CustomEvent("gadgetselect",
      {
        detail: element
      }));
    }
  
  }
})();
