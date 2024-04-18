(function () {
  'use strict';

  angular.module('dashboardFramework')
    .service('socketHttpService', SocketHttpService);

  /** @ngInject */
  function SocketHttpService($log, __env, $timeout, $q, httpService) {
      var vm = this;

      vm.stompClient = {};
      vm.hashRequestResponse = {};
      vm.connected = false;
      vm.queue = {};
      vm.resetHeartBeatCallback;//call on connection and when some data is received for reset scheduling
      vm.retrying = false;

      var errorfn = function(error){
        if (!vm.retrying) {
          if(error.status) {
            console.log("Error Rest Connect: " + "code: " + error.status + " - " + error.statusText + " , reconnecting...");
          } else {
            console.log("Error Rest Connect: " + error);
          }
          vm.retrying = true;
          window.dispatchEvent(new CustomEvent("ErrorConnect",{detail: error}));
          $timeout(vm.connect,5000);
        } else {
          //if retrying we ignore error
        }
      }

      vm.connect = function(){
        httpService.restConnect().then(
          function (response) {
            if (response.status === 200) {
              console.log('%c DSEngine Rest Connected    ' + '%c ' + new Date(), 'color: #1e8fff; font-weight:bold; font-size:13px', 'color: #bbb; font-weight:bold; font-size:13px');
              vm.connected=true;
              Object.keys(vm.queue).map(
                function(dskey){
                  vm.sendAndSubscribe(vm.queue[dskey], true);
                }
              )
            } else {
              errorfn    
            }
          }
        ).catch(
          errorfn
        );
        vm.retrying=false;
      }

      /*vm.connectAndSendAndSubscribe = function(reqrespList){
        httpService.restConnect()
          .then(function (frame) {
            for(var reqrest in reqrespList){
              httpService.solveDatasource(reqrespList[reqrest].msg).then(
                function (payload) {
                  reqrespList[reqrest].callback(reqrespList[reqrest].id, payload)
                }
              ).catch(
                function(error) {
                  console.log("Error datasource " + reqrespList[reqrest].ds + " : " + error);
                  $timeout(function() {
                    $timeout(vm.connect,5000);
                  },5000);
                }
              )
            }
          })
        };*/

      vm.sendAndSubscribe = function(datasource,ignoreQueue){
        if(vm.connected && (ignoreQueue || notInQueue(datasource))){
          // Send message
          var datasourcefinal;
          if (datasource.msg.filter && datasource.msg.filter.length > 0 && datasource.msg.filter[0].id) {
            datasourcefinal = JSON.parse(JSON.stringify(datasource));
            datasourcefinal.msg.filter = datasourcefinal.msg.filter.map(function (d) {
              return d.data[0]
            })
            datasourcefinal.callback = datasource.callback;
            datasourcefinal.callbacks = datasource.callbacks;
          } else {
            datasourcefinal = datasource
          }

          var curriedCallback = function(datasourcefinal) {
            return function (payload) {
              if (datasourcefinal.callback) {
                datasourcefinal.callback(datasourcefinal.id, datasourcefinal.type, payload.data);
              } else if (datasourcefinal.callbacks) {
                for(var calli in datasourcefinal.callbacks) {
                  datasourcefinal.callbacks[calli](datasourcefinal.id, datasourcefinal.type, payload.data);
                }
              }
              removeFromQueue(datasourcefinal);
            }
          };

          httpService.solveDatasource(datasourcefinal.msg).then(
            curriedCallback(datasourcefinal)
          ).catch(
            errorfn
          )
        }
        else{
          addToQueue(datasource);
        }
      }

      vm.cleanqueue = function(reqrespList){
        vm.queue = {};
      }
      vm.disconnect = function(){
        var deferred = $q.defer();
        $log.info('disconnected');
        vm.connected = false;
        deferred.resolve();
        return deferred.promise;
      }

      //provisional method, could be use hash key
      function generateDatasourceKey(datasource){
        var keyobj = {
          id: datasource.id,
          msg: datasource.msg,
          type: datasource.type          
        }
        return JSON.stringify(keyobj);
      }

      function notInQueue(datasource){
        return !vm.queue.hasOwnProperty(generateDatasourceKey(datasource));
      }

      function addToQueue(datasource){
        var key = generateDatasourceKey(datasource);
        if (datasource.id !== 1 || !(key in vm.queue)) {// 1 is from vm.from
          vm.queue[key] = {};
          vm.queue[key].id = datasource.id;
          vm.queue[key].msg = datasource.msg;
          vm.queue[key].type = datasource.type;
          vm.queue[key].callbacks = [datasource.callback];
        } else {
          vm.queue[key].callbacks.push(datasource.callback);
        }
      }

      function getFromQueue(datasource){
        return vm.queue[generateDatasourceKey(datasource)];
      }

      function removeFromQueue(datasource){
        delete vm.queue[generateDatasourceKey(datasource)];
      }

      vm.addListenerForHeartbeat = function(callback){
      }

      vm.isConnected = function(){
        return vm.connected;
      }
  };
})();
