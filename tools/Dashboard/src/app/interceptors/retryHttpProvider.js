(function () {
    'use strict';

    angular.module('dashboardFramework').config(RetryHttpProviderConfig);

    /** @ngInject */
    function RetryHttpProviderConfig($httpProvider) {  
        $httpProvider.interceptors.push(function ($q, $injector) {
            var incrementalTimeout = 1000;
        
            function retryRequest (httpConfig) {
                var $timeout = $injector.get('$timeout');
                var thisTimeout = incrementalTimeout;
                incrementalTimeout *= 2;
                return $timeout(function() {
                    var $http = $injector.get('$http');
                    return $http(httpConfig);
                }, thisTimeout);
            };
        
            return {
                responseError: function (response) {
                    console.error("Error " + response.status + " in RestCall " + response.config.url + ", detail: " + JSON.stringify(response.data));
                    if (response.status === 500) {
                        if (incrementalTimeout < 5000) {
                            return retryRequest(response.config);
                        }
                        else {
                            console.error('The remote server seems to be busy at the moment. Please try again in later');
                        }
                    }
                    else {
                        incrementalTimeout = 1000;
                    }
                    return $q.reject(response);
                }
            };
        }); 
    }

})();