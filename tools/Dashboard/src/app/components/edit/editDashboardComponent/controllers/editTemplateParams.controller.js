(function () {
  'use strict';

  angular.module('dashboardFramework')
    .controller(
      'editTemplateParamsController',
      function ($scope,__env, $mdDialog,$mdCompiler, httpService, type, config, layergrid, edit, element, utilsService,create,inline) {
        var agc = this;

        agc.$onInit = function () {
          if (!$scope.config.tconfig) {
            $scope.getPredefinedParameters($scope.config.content);
            $scope.getPredefinedParameters($scope.config.contentcode);
          } else {
            $scope.parameters = JSON.parse(JSON.stringify($scope.config.tconfig)).gform;
          }
        }

        window.setTimeout(
        function() {
          var gform = JSON.parse(JSON.stringify($scope.parameters));
          if ($scope.config.config) {
            var gformvalue = utilsService.legacyToNewParamsWithDatasource(JSON.parse($scope.config.config), $scope.element.datasource);
            if (!__env.dashboardEngineAvoidReassign) {
              gformvalue = utilsService.reassign(gform, gformvalue)
            }
          } else {
            var gformvalue = {}
          }

          $scope.vueapp = new Vue({
            el: '#gform',
            data: {
              list2: gform,
              gformvalue: create?gformvalue:utilsService.fillWithDefaultFormData(gformvalue, gform)
            },
            methods: {
              remove: function(list,index){
                list.splice(index, 1);
              },
              getDefaultTParams: function() {
                function setDefault(elements, localvalue) {
                  for (element in elements) {
                    if (elements[element].elements && elements[element].elements.length > 0) {
                      localvalue[elements[element].name] = {}
                      setDefault(elements[element].elements, localvalue[elements[element].name])
                    } else {
                      localvalue[elements[element].name] = JSON.parse(JSON.stringify(elements[element].default == undefined ? null : elements[element].default))
                    }
                  }
                }
                var defaultTParams = {}
                setDefault(this.list2, defaultTParams);
                return defaultTParams;
              }
            },
            mounted: function () {
              if (this.gformvalue == {}) {
                this.gformvalue['parameters'] = this.getDefaultTParams();
              }
            }
          });


        },500);
       
        $scope.type = type;
        $scope.config = config;
        $scope.element = element;
        $scope.layergrid = layergrid;
        $scope.edit = edit;
        $scope.inline = inline;
        $scope.datasource;
        $scope.datasources = [];
        $scope.datasourceFields = [];
        $scope.parameters = [];
        
        $scope.templates = [];

        $scope.create = create;
        $scope.dat ={};
        $scope.dat.ident ;
        $scope.dat.desc ;

        $scope.hide = function() {
          $mdDialog.hide();
        };

        $scope.cancel = function() {
          $mdDialog.cancel();
        };

       
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
  
        $scope.iterate=  function (obj, stack, fields) {
          for (var property in obj) {
               if (obj.hasOwnProperty(property)) {
                   if (typeof obj[property] == "object") {
                    $scope.iterate(obj[property], stack + (stack==""?'':'.') + property, fields);
            } else {
                       fields.push({field:stack + (stack==""?'':'.') + property, type:typeof obj[property]});
                   }
               }
            }    
            return fields;
         }

        

        /**we look for the parameters in the source code to create the form */
        $scope.getPredefinedParameters = function(str){
          var regexTagHTML =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\-->/g;
          var regexTagJS =  /\/\*[\-\-\s\w\>\=\"\'\,\:\+\_\/]*\*\//g;
          var regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;
          var regexDescription = /description\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;
          var regexOptions = /options\s*=\s*\"[\s\w\>\=\-\'\:\,\+\_\/]*\s*\"/g;
          var found=[];
          found = utilsService.searchTag(regexTagHTML,str).concat(utilsService.searchTag(regexTagJS,str));	
          
          found.unique=function unique (a){
            return function(){return this.filter(a)}}(function(a,b,c){return c.indexOf(a,b+1)<0
           }); 
          found = found.unique(); 
      
          for (var i = 0; i < found.length; i++) {			
            var tag = found[i];
            
            var name = utilsService.searchTagContentName(regexName,tag)
            var param = {
              name: name
            }
            if(tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
              param.type = "input-text"
            }else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
              param.type = "input-number"
            }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
              param.type = "ds-field(ds[0].)"
            }else if(tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
              param.type = "ds-field"
            }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-osp')>=0){
              param.type = "selector";
              var options = utilsService.searchTagContentName(regexOptions,tag);
              if (options && options.length > 0) {
                param.options = options.split(",").map(
                  function(option){
                    return {
                      value: option
                    }
                  }
                )
              }
            }
            if (name) {
              $scope.parameters.push(param);
            }
           } 
          }       
        
        $scope.loadDatasourcesFields = function(){
          
          if($scope.config.datasource!=null && $scope.config.datasource.id!=null && $scope.config.datasource.id!=""){
               return httpService.getsampleDatasources($scope.config.datasource.id).then(
                function(response){
                  $scope.datasourceFields=$scope.iterate(response.data[0],"", []);
                },
                function(e){
                  console.log("Error getting datasourceFields: " +  JSON.stringify(e))
                }
              );
            }
            else 
            {return null;}
      }


        $scope.save = function() { 
          $scope.parameters = $scope.vueapp._data.gformvalue;
          if(!edit){
            if(create){
              //create new gadget with 
              

              var config = $scope.parameters;
              var gadget = {
                "identification": $scope.dat.ident,
                "description": $scope.dat.desc,               
                "config": JSON.stringify(config),
                "gadgetMeasures": [],
                "type": $scope.config.tempId,
                "instance":true
              }
              if( $scope.config.datasource){     
                gadget["datasource"]= {
                  "identification": $scope.config.datasource.name,
                  "query": $scope.config.datasource.query,
                  "refresh": $scope.config.datasource.refresh,
                  "maxValues": $scope.config.datasource.maxValues,
                  "description": $scope.config.datasource.description
                }
              }

              if (!inline) {//Gadget custom
                return httpService.createGadget(gadget).then(
                  function(response){
                    $scope.config.type = $scope.type;                 
                    $scope.config.params = $scope.parameters;
                    $scope.config.tparams = $scope.parameters;
                    $scope.config.gadgetid = response.data.id;
                    $scope.config.datasource = $scope.parameters.datasource;
                    $scope.layergrid.push($scope.config);
                    window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: $scope.config}));
                    $mdDialog.cancel();
                  },
                  function(e){
                    console.log("Error create Custom Gadget: " +  JSON.stringify(e))
                  }
                );
              } else {//Gadget inline
                $scope.config.type = $scope.type;
                $scope.config.params = $scope.parameters;
                $scope.config.tparams = $scope.parameters;
                $scope.config.datasource = $scope.parameters.datasource;
                $scope.layergrid.push($scope.config);
                window.dispatchEvent(new CustomEvent("newgadgetcreated",{detail: $scope.config}));
                $mdDialog.cancel();
              } 
            }else{  
              $scope.config.type = $scope.type;
              if($scope.config.template){// ID mode, save init params (edit only params)
                $scope.config.params = $scope.parameters;
              }
              else{ // edit code mode (no id reference) 
                $scope.config.content=utilsService.parseProperties($scope.config.content,$scope.parameters);         
                $scope.config.contentcode=utilsService.parseProperties($scope.config.contentcode,$scope.parameters,true);
                $scope.config.datasource = $scope.parameters.datasource
              }
              $scope.layergrid.push($scope.config);
              $mdDialog.cancel();
            }
          }
          else{ // only edit params (ID mode)
            if (!inline) {
              var configCustomGadget = $scope.parameters;
              httpService.updateGadgetConf($scope.element.gadgetid,JSON.stringify(configCustomGadget)).then(
                function(response){
                  $scope.element.params = $scope.parameters;
                  $scope.element.datasource = $scope.parameters.datasource;
                  $mdDialog.cancel();
                },
                function(e){
                  console.log("Error create Custom Gadget: " +  JSON.stringify(e));
                  $mdDialog.cancel();
                }
              );
            } else {
              $scope.element.params = $scope.parameters;
              if(typeof $scope.parameters.datasource !== 'undefined'){
                $scope.element.datasource = JSON.parse(JSON.stringify($scope.parameters.datasource));
              }
              $mdDialog.cancel();
            }
          }
        
        };
      
      }
    )
  
})();
