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
/**
 * Controller for Batch Timer Chart.
 */

angular
  .module('dataCollectorApp.home')
  .controller('AllStageBatchTimerChartController', ["$rootScope", "$scope", "api", "pipelineConstant", function($rootScope, $scope, api, pipelineConstant) {
    var baseQuery = "select mean,metric from timers where (pipeline='" + $scope.pipelineConfig.info.pipelineId + "') and ";

    angular.extend($scope, {
      allDataZero: true,
      totalValue: 0,
      chartOptions: {
        chart: {
          type: 'pieChart',
          height: 500,
          x: function(d) {
            return d.key;
          },
          y: function(d){
            return d.value;
          },
          showLabels: false,
          showLegend: true,
          donut: true,
          //donutRatio: '.45',
          labelsOutside: true,
          transitionDuration: 500,
          labelThreshold: 0.01,
          legend: {
            margin: {
              left:10,
              top:10,
              bottom:10,
              right:10
            }
          }
        }
      },
      chartData: [],

      timeSeriesChartOptions: {
        chart: {
          type: 'lineChart',
          height: 500,
          showLabels: true,
          duration: 0,
          x:function(d){
            return (new Date(d[0])).getTime();
          },
          y: function(d) {
            return d[1];
          },
          showLegend: true,
          xAxis: {
            tickFormat: $scope.dateFormat()
          },
          yAxis: {
            tickFormat: $scope.formatValue()
          },
          margin: {
            left: 50,
            top: 20,
            bottom: 30,
            right: 20
          },
          useInteractiveGuideline: true
        }
      },

      timeSeriesChartData: [],

      getLabel: function(){
        return function(d) {
          return d.key;
        };
      },

      getValue: function() {
        return function(d){
          if(d.value > 0) {
            return d.value.toFixed(2);
          } else {
            return 0;
          }
        };
      },

      getTooltipContent: function() {
        return function(key, x, y, e, graph) {
          return '<p>' + key + '</p><p>' + y.value +  ' seconds</p>';
        };
      },

      xValue: function(){
        return function(d){
          return (new Date(d[0])).getTime();
        };
      },

      yValue: function(){
        return function(d){
          if(d[1] > 0) {
            return d[1].toFixed(2);
          } else {
            return 0;
          }
        };
      }
    });

    var stages = $scope.stageInstances;

    if (stages.length > 10) {
      $scope.chartOptions.chart.showLegend = false;
    }

    angular.forEach(stages, function(stage) {
      $scope.chartData.push({
        instanceName: stage.instanceName,
        key: stage.uiInfo.label,
        value: 0
      });
    });

    $scope.$on('summaryDataUpdated', function() {
      var pipelineMetrics = $rootScope.common.pipelineMetrics,
        values = [];

      $scope.allDataZero = true;

      if(!pipelineMetrics.timers) {
        return;
      }

      angular.forEach($scope.chartData, function(data) {
        var stageTimer = pipelineMetrics.timers['stage.' + data.instanceName + '.batchProcessing.timer'];
        if(stageTimer) {
          data.value = stageTimer.mean;

          if(data.value > 0) {
            $scope.allDataZero = false;
          }
        }
        values.push(data);
      });

      $scope.chartData = values;

      $scope.totalValue = pipelineMetrics.timers['pipeline.batchProcessing.timer'].mean;

      if($scope.totalValue > 0) {
        $scope.totalValue = $scope.totalValue.toFixed(2);
      }
    });

    var refreshTimeSeriesData = function() {
      var query = baseQuery + '(';
      var timeRangeCondition = $scope.getTimeRangeWhereCondition();
      var labelMap = {};

      angular.forEach(stages, function(stage, index) {
        var stageTimer = 'stage.' + stage.instanceName + '.batchProcessing.timer';

        if(index !== 0) {
          query += ' or ';
        }

        query += "metric = '" + stageTimer + "'";
        labelMap[stageTimer] = stage.uiInfo.label;
      });

      query += ") and " + timeRangeCondition;

      api.timeSeries.getTimeSeriesData(query).then(
        function(res) {
          if(res && res.data) {
            var chartData = $scope.timeSeriesChartData;
            chartData.splice(0, chartData.length);
            angular.forEach(res.data.results[0].series, function(d, index) {
              chartData.push({
                key: labelMap[d.tags.metric],
                columns: d.columns,
                values: d.values
              });
            });
          }
        },
        function(res) {
          $rootScope.common.errors = [res.data];
        }
      );
    };

    $scope.$watch('timeRange', function() {
      if($scope.timeRange !== 'latest') {
        refreshTimeSeriesData();
      }
    });

    $scope.$on('onSelectionChange', function(event, options) {
      if($scope.isPipelineRunning && $scope.timeRange !== 'latest' &&
        options.type !== pipelineConstant.LINK) {
        refreshTimeSeriesData();
      }
    });

    if($scope.timeRange !== 'latest') {
      refreshTimeSeriesData();
    }

  }]);
