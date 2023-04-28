/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Orange angular-swagger-ui - v0.2.3
 *
 * (C) 2015 Orange, all right reserved
 * MIT Licensed
 */

angular
  .module('swaggerUi')
  .service('swaggerUiExternalReferences', ['$http', '$q', 'swaggerModules', function($http, $q, swaggerModules) {

    var url,
      deferred,
      swagger;

    /**
     * Module entry point
     */
    this.execute = function(swaggerUrl, swaggerData) {
      url = swaggerUrl;
      swagger = swaggerData;
      deferred = $q.defer();
      loadExternalReferences();
      return deferred.promise;
    };

    function onError(error) {
      deferred.reject(error);
    }

    /**
     * Load external definition
     */
    function get(externalUrl, callback, prefix) {
      var options = {
        method: 'GET',
        url: externalUrl,
        transformResponse: function(json) {
          if (prefix) {
            // rewrite references
            json = json.replace(/"\$ref": ?"#\/(.*)"/g, '"$ref": "#/definitions/' + prefix + '#/$1"');
          }
          var obj;
          try {
            obj = angular.fromJson(json);
          } catch (e) {
            onError({
              code: 500,
              message: 'failed to parse JSON'
            });
          }
          return obj;
        }
      };
      swaggerModules
        .execute(swaggerModules.BEFORE_LOAD, options)
        .then(function() {
          $http(options)
            .then(callback)
            .catch(function(res) {
              onError({
                message: res.data,
                code: res.status
              });
            });
        })
        .catch(onError);
    }

    /**
     * Generate external URL
     */
    function getExternalUrl($ref) {
      var parts = $ref.split('#/'),
        externalUrl = parts[0];

      if (externalUrl.indexOf('http') !== 0 && externalUrl.indexOf('https') !== 0) {
        // relative url
        if (externalUrl.indexOf('/') === 0) {
          var swaggerUrlParts = URL.parse(url);
          externalUrl = swaggerUrlParts.protocol + '//' + swaggerUrlParts.host + externalUrl;
        } else {
          var pos = url.lastIndexOf('/');
          externalUrl = url.substring(0, pos) + '/' + externalUrl;
        }
      }
      return externalUrl;
    }

    /**
     * Find and resolve external definitions
     */
    function loadExternalReferences() {

      var loading = 0;

      function loadOperations(path) {
        loading++;
        get(getExternalUrl(path.$ref), function(json) {
          loading--;
          delete path.$ref;
          for (var key in json) {
            path[key] = json[key];
          }
          if (loading === 0) {
            loadExternalDefinitions();
          }
        });
      }

      for (var path in swagger.paths) {
        path = swagger.paths[path];
        if (isExternal(path)) {
          loadOperations(path);
        }
      }
      if (loading === 0) {
        // may have no external paths
        loadExternalDefinitions();
      }
    }

    function isExternal(item) {
      return item && item.$ref && item.$ref.indexOf('#/') !== 0;
    }

    function loadExternalDefinitions() {
      var loading = 0,
        loadingUrls = {};

      function loadDefinitions(item) {
        var parts = item.$ref.split('#/'),
          externalUrl = getExternalUrl(item.$ref);

        // rewrite reference
        item.$ref = '#/definitions/' + item.$ref;
        // load external if needed
        if (!loadingUrls[externalUrl]) {
          loading++;
          loadingUrls[externalUrl] = true;
          get(externalUrl, function(json) {
            for (var key in json) {
              swagger.definitions[parts[0] + '#/' + key] = json[key];
            }
            loading--;
            if (loading === 0) {
              deferred.resolve(true);
            }
          }, parts[0]);
        }
      }

      function checkDefinitions(item) {
        // check if an item has an external reference
        if (isExternal(item)) {
          loadDefinitions(item);
        } else if (isExternal(item.items)) {
          loadDefinitions(item.items);
        }
      }

      function checkOperationDefinitions(operation) {
        // check if operation params or responses have external references
        for (var j = 0, params = operation.parameters || [], k = params.length; j < k; j++) {
          if (params[j].schema) {
            checkDefinitions(params[j].schema);
          }
        }
        for (var code in (operation.responses || {})) {
          if (operation.responses[code].schema) {
            checkDefinitions(operation.responses[code].schema);
          }
        }
      }
      for (var path in swagger.paths) {
        var operations = swagger.paths[path];
        for (var httpMethod in operations) {
          checkOperationDefinitions(operations[httpMethod]);
        }
      }
      if (loading === 0) {
        // may have no external definitions
        deferred.resolve(true);
      }
    }

  }]);
