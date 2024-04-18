(function () {
  'use strict';

  angular.module('dashboardFramework')
    .service('localStorageService', LocalStorageService);

  /** @ngInject */
  function LocalStorageService(__env,$q) {

    var vm = this;
    var DB_NAME = 'onesaitplatform';
    var DB_VERSION = 1; // Use a long long for this value (don't use a float)    
    var DB_STORE_NAME = 'dashboards';
    
   


    vm.maxItems = typeof __env.maxItemsLocalStorage !=='undefined'?__env.maxItemsLocalStorage:5;
    vm.milliIntervalSave = typeof __env.milliIntervalSaveLocalStorage !=='undefined'?__env.milliIntervalSaveLocalStorage:10000;
    vm.saveEnabled = true;




    vm.getLastItemDate = function (id) {  
      var deferred = $q.defer();    
        getItemLocalStorage(id).then(function(modelArray){
          if ( modelArray === null || modelArray.savedByUser === null || modelArray.savedByUser.length === 0) {            
            deferred.resolve( null);
          } else{       
            deferred.resolve( JSON.parse(modelArray.savedByUser[modelArray.savedByUser.length - 1].date));
          }
      });
      return deferred.promise;
    }

    vm.getDateItems = function (id) {
      var deferred = $q.defer(); 
        getItemLocalStorage(id).then(function(modelArray){
          var dates = {autoSave:[],savedByUser:[]};
      
          if ( modelArray !== null && modelArray.autoSave.length > 0) {
            modelArray.autoSave.forEach(function (elem) {
              dates.autoSave.push({milis:elem.date,date:new Date(elem.date).toLocaleString(),model:JSON.parse(elem.model)});
            });
          }
          if ( modelArray !== null && modelArray.savedByUser.length > 0) {
            modelArray.savedByUser.forEach(function (elem) {
              dates.savedByUser.push({milis:elem.date,date:new Date(elem.date).toLocaleString(),model:JSON.parse(elem.model)});
            });
          }        
          deferred.resolve(dates);
        });
        return deferred.promise;
    }

    vm.getItemByIdAndDate = function (id, savedDate) {
     var deferred = $q.defer();
     getItemLocalStorage(id).then(function(modelArray){
           
      if (modelArray !== null && modelArray.savedByUser !== null && modelArray.savedByUser.length > 0) {
        for (var index = 0; index < modelArray.savedByUser.length; index++) {
          var element = modelArray.savedByUser[index];
          if(element.date+'' === savedDate+''){            
            deferred.resolve(JSON.parse(element.model));
          }
        }        
      }

      if (modelArray !== null && modelArray.autoSave !== null && modelArray.autoSave.length > 0) {
        for (var index = 0; index < modelArray.autoSave.length; index++) {
          var element = modelArray.autoSave[index];
          if(element.date+'' === savedDate+''){            
            deferred.resolve(JSON.parse(element.model));
          }
        }        
      }
    })
          return deferred.promise;
    }

    vm.isAfterSavedDate = function (id, savedDate) {
      var deferred = $q.defer(); 
        vm.getLastItemDate(id).then(function(itemDate){
        if ( itemDate !== null) {           
          deferred.resolve(savedDate >= itemDate);
        }else{
          deferred.resolve(true);
        }
      });     
      return deferred.promise;
    }

    vm.modelsAreEqual = function (modelA, modelB) {
      return JSON.stringify(modelA) === JSON.stringify(modelB);       
    }
    //{date:"",model:""}
    vm.setItem = function (id, newModel,origin,timemillis) {  
      
      if(vm.saveEnabled){ 
        getItemLocalStorage(id).then(function(modelArray){           
          if ( modelArray === null) {
            modelArray = {autoSave:[],savedByUser:[]};
          }

          var saveItem = true;
          if (modelArray[origin].length > 0) {
            if (vm.modelsAreEqual(newModel.model, modelArray[origin][modelArray[origin].length - 1].model)) {
              saveItem = false;
            }
          }
          if (saveItem) {
            var savedtime ;
            if(typeof timemillis == 'undefined'){
              savedtime = new Date().getTime();
            } 
            else{
              savedtime = timemillis;
            }
            if (modelArray[origin].length < vm.maxItems) {
              modelArray[origin].push({
                date: savedtime,
                model: newModel.model
              });
            } else {
              modelArray[origin].shift();
              modelArray[origin].push({
                date: savedtime,
                model: newModel.model
              });             
            }
          }
          setItemLocalStorage(id, modelArray);
        })
      }
    }

  
    function setItemLocalStorage(id, modelArray) { 
           
      getObjectStore(DB_STORE_NAME, 'readwrite').then(
        function (store) {
          var request = store.get(id);
          request.onerror = function(event) {
          // Handle errors!
          };
          request.onsuccess = function(event) {
              // Get the old value that we want to update
                var data = event.target.result;      
              if (typeof data=='undefined'){
                var dat = {id:id,value:modelArray};
                store.add(dat);
              }
              else{
                var dat = {id:id,value:modelArray};
                store.put(dat);
              }      
        }
      })
    }
       

      function getItemLocalStorage(id){
      
      var deferred = $q.defer();
       getObjectStore(DB_STORE_NAME, 'readonly').then(
            function (store) {
              var request = store.get(id);
              request.onerror = function(event) {
              deferred.resolve( null);
              };
              request.onsuccess = function(event) {
                if(typeof request.result!='undefined'){              
                  deferred.resolve(request.result.value);
                }else{
                  deferred.resolve(null);
                }
              };
            })
            return deferred.promise;
          }
     
    function getObjectStore(store_name, mode) {
        var deferred = $q.defer();
             openDb().then(
            function (db) {
              var tx = db.transaction(store_name, mode);
              deferred.resolve(tx.objectStore(store_name));
          });
          return deferred.promise;
    }
  
       vm.db=null;
       function openDb () {
        var deferred = $q.defer();
        if(vm.db==null){       
        var req = indexedDB.open(DB_NAME, DB_VERSION);
        req.onsuccess = function (evt) {
          vm.db=this.result
          deferred.resolve(vm.db);         
        };
        req.onerror = function (evt) {
          console.error("openDb:", evt.target.errorCode);
          deferred.reject();
        };
        req.onupgradeneeded = function (evt) {         
          var store = evt.currentTarget.result.createObjectStore(
            DB_STORE_NAME, { keyPath: 'id', autoIncrement: false });
            store.createIndex('id', 'id', { unique: true });  
            store.createIndex('value', 'value', { unique: false }); 
            openDb().then(
              function (db) {
                deferred.resolve(vm.db);  
              })
                         
        };
      }else{
        deferred.resolve(vm.db);
      }
        return deferred.promise;
    }
  };
})();