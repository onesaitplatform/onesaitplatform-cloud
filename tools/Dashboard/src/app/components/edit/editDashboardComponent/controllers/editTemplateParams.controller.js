(function () {
  'use strict';

  angular.module('dashboardFramework')
    .controller(
      'editTemplateParamsController',
      function ($scope,__env, $mdDialog,$mdCompiler, httpService, type, config, layergrid, edit, element, utilsService) {
        var agc = this;
        agc.$onInit = function () {
          $scope.loadDatasources();
          $scope.getPredefinedParameters($scope.config.content);
          $scope.getPredefinedParameters($scope.config.contentcode);
          if($scope.edit){
            for(var i=0;i < $scope.element.params.length;i++){
              var founds = $scope.parameters.filter(function(par){
                return par.label ==  $scope.element.params[i].label && par.type ==  $scope.element.params[i].type;
              })
              if(founds.length == 1){
                founds[0].value = $scope.element.params[i].value;
              }
            }
            if($scope.element.datasource){
              config.datasource = $scope.element.datasource;
            }
            $scope.loadDatasourcesFields();
          }
        }
       
        $scope.type = type;
        $scope.config = config;
        $scope.element = element;
        $scope.layergrid = layergrid;
        $scope.edit = edit;
        $scope.datasource;
        $scope.datasources = [];
        $scope.datasourceFields = [];
        $scope.parameters = [];
       
        $scope.templates = [];

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
          var regexOptions = /options\s*=\s*\"[\s\w\>\=\-\'\:\,\+\_\/]*\s*\"/g;
          var found=[];
          found = utilsService.searchTag(regexTagHTML,str).concat(utilsService.searchTag(regexTagJS,str));	
          
          found.unique=function unique (a){
            return function(){return this.filter(a)}}(function(a,b,c){return c.indexOf(a,b+1)<0
           }); 
          found = found.unique(); 
      
          for (var i = 0; i < found.length; i++) {			
            var tag = found[i];
            if(tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){	
              $scope.parameters.push({label:utilsService.searchTagContentName(regexName,tag),value:"parameterTextLabel", type:"labelsText"});
            }else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
              $scope.parameters.push({label:utilsService.searchTagContentName(regexName,tag),value:0, type:"labelsNumber"});              
            }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
              $scope.parameters.push({label:utilsService.searchTagContentName(regexName,tag),value:"parameterDsLabel", type:"labelsds"});               
            }else if(tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
              $scope.parameters.push({label:utilsService.searchTagContentName(regexName,tag),value:"parameterNameDsLabel", type:"labelsdspropertie"});               
            }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-osp')>=0){
              var optionsValue = utilsService.searchTagContentOptions(regexOptions,tag); 
              $scope.parameters.push({label:utilsService.searchTagContentName(regexName,tag),value:"parameterSelectLabel",type:"selects", optionsValue:optionsValue});	              
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
          if(!edit){
            $scope.config.type = $scope.type;
            if($scope.config.template){// ID mode, save init params (edit only params)
              $scope.config.params = $scope.parameters;
            }
            else{ // edit code mode (no id reference) 
              $scope.config.content=utilsService.parseProperties($scope.config.content,$scope.parameters);         
              $scope.config.contentcode=utilsService.parseProperties($scope.config.contentcode,$scope.parameters,true);
            }
            $scope.layergrid.push($scope.config);
          }
          else{ // only edit params (ID mode)
            $scope.element.params = $scope.parameters;
            $scope.element.datasource = $scope.config.datasource;
          }
          $mdDialog.cancel();
        };
      
      }
    )
  
})();
