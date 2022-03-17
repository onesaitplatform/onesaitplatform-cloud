(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('datadiscoveryDataDraw', {
      templateUrl: 'app/components/view/datadiscoveryComponent/datadiscoveryComponents/datadiscoveryDataDraw.html',
      controller: DatadiscoveryDataDrawController,
      controllerAs: 'vm',
      bindings: {
        id: "<?",
        datastatus: "=",
        datasource: "=",
        columns: "<",
        config: "<",
        filters: "<",
        reloadDataLink: "&",
        getDataAndStyle: "&"
      }
    });

  /** @ngInject */
  function DatadiscoveryDataDrawController($log, $scope, $element, $timeout, datasourceSolverService, utilsService, $q, $window, urlParamService, filterService) {
    var vm = this;

    vm.from = datasourceSolverService.from;

    vm.$onInit = function () {      
     
      if(!vm.columns.dataAccess){
        vm.columns.dataAccess = {
          total: {
        //    index: totalindex
          },
          subTotals: {
            indexes: [],
            keyToIndex: {
    
            }
        //    key1: index1
        //    key2: index2
          }
        }
      }
      
      //Retrocompatibility
      if(vm.columns.subtotalField != undefined && (!vm.columns.subtotalFields || (vm.columns.subtotalFields && vm.columns.subtotalFields.length === 0))){
        vm.columns.subtotalFields = [];
        if(vm.columns.subtotalField != -1){
          vm.columns.subtotalFields.push(vm.columns.subtotalField);
          vm.columns.subtotalField = undefined;
        }
      }

      vm.ready = true;
      $scope.$on("$resize",vm.resizeJExcel);
      angular.element($window).on("resize",vm.resizeJExcel);
      
      vm.unsubscribeHandler = $scope.$on(vm.id,eventDProcessor);

      vm.reloadDataLink({"reloadchild":vm.redrawView});
      vm.getDataAndStyle({"getDataAndStyleChild":function(){return {data:vm.nonMergedData?vm.nonMergedData:vm.jexcel.getData(),style:vm.jexcel.getStyle(),headers:vm.getHeaders()}}});

      if(vm.config && vm.config.discovery && vm.config.discovery.matrix && vm.config.discovery.matrix.data){
        $timeout(
          function(){

            if(vm.filters && vm.filters.length>0){
              if(vm.filters){
                filterService.getInitialFilters(vm.id,vm.filters);
              }
              else{
                vm.redrawView();
              }
            }
            else{//No initial filter, keep saved data
              vm.blockUpdateTable = true;
              vm.drawRedrawJExcel(vm.config.discovery.matrix.data);
              vm.blockUpdateTable = false;
              vm.triggerUpdateTable();
              vm.jexcel.setStyle(vm.config.discovery.matrix.style);
              vm.mergeByColumnAttr();
            }
            vm.status = "ready"
            if(!vm.loadSended){
              window.dispatchEvent(new CustomEvent('gadgetloaded', { detail: vm.id }));
              vm.loadSended = true;
            }
          },0
        )
      }
      else{
        vm.status = "ready"
      }
    }

    vm.$onChanges = function (changes) {
      if (vm.status == "ready")
      {
        vm.deleteJExcel();
        vm.redrawView();
      }
    }

    vm.reloadDataF = function(){
      vm.redrawView();
    }

    vm.getHeaders = function(){
      var cols = vm.jexcel.getWidth();
      return vm.jexcel.getHeaders().split(",").map(function(h, index){
        return {
          name: h,
          width: cols[index]
        }
      })
    }

    vm.redrawView = function () {
     
      if(!vm.columns.dataAccess){
        vm.columns.dataAccess = {
          total: {
        //    index: totalindex
          },
          subTotals: {
            indexes: [],
            keyToIndex: {
    
            }
        //    key1: index1
        //    key2: index2
          }
        }
      }


      $scope.pindex = $scope.pindex ? $scope.pindex + 1 : 1;
      var actindex = angular.copy($scope.pindex);
      vm.blockUpdateTable = true;      
      vm.columns.dataAccess.subTotals.indexes=[];
      vm.columns.dataAccess.total.index=null;

      if (vm.columns && vm.columns.list && vm.columns.list.length) {
        vm.status = 'pending';
      }
      else{//No column data
        return;
      }
      vm.sendQuery().then(
        vm.drawRedrawJExcel
      ).then(
        function (data) {
          if (vm.config.enableTotal && vm.hasMetrics() && $scope.pindex === actindex) {
            return vm.sendQueryTotals().then(
              function (totalData) {
                return vm.setTotalsJExcel(totalData, data);
              }
            );
          }
          else {
            var deferred = $q.defer();
            deferred.resolve(data);
            return deferred.promise;
          }
        }
      ).then(
        function (data) {
          if (typeof data !='undefined' && data.length>0 && vm.columns.subtotalEnable && ((vm.columns.subtotalField != undefined && vm.columns.subtotalField != -1) || (vm.columns.subtotalFields && vm.columns.subtotalFields.length > 0))  && $scope.pindex === actindex) {
            return vm.sendQuerySubTotals().then(
              function (subTotalData) {
                return vm.setSubTotalsJExcel(subTotalData, data);
              }
            );
          }
          else {
            var deferred = $q.defer();
            deferred.resolve(data);
            return deferred.promise;
          }
        }
      ).then(function () {
        if($scope.pindex === actindex){
          vm.status = "ready";
          vm.blockUpdateTable = false;
          vm.triggerUpdateTable();
          vm.mergeByColumnAttr();
        }
      }).catch(function (e) {
        vm.status = "error";
        vm.error = e;
      });
    }

    vm.triggerUpdateTable = function(){
      vm.jexcel.setValue("A1",vm.jexcel.getValue("A1"));
    }

    vm.getGroupFields = function () {
      return vm.columns.list.filter(function (select) { return select.type !== 'metric' }).map(function (select) { return select.field });
    }

    vm.getSelectFields = function () {
      return vm.columns.list.map(function (select) {
        return { "field": (select.type != "metric" ? select.field : select.formula), "alias": vm.normalizeField(select.field) }
      });
    }

    vm.getSelectMetrics = function () {
      return vm.columns.list.filter(function (select) {
        return select.type === "metric";
      }).map(function (select) {
        return { "field": (select.type != "metric" ? select.field : select.formula), "alias": vm.normalizeField(select.field) }
      });
    }

    vm.getSortFields = function () {
      return vm.columns.list.filter(
        function (sort) {
          return sort.asc !== null
        }
      ).map(
        function (sort) {
          if (sort.type === "metric") {
            return { "field": sort.formula, "asc": sort.asc }
          }
          else {
            return { "field": sort.field, "asc": sort.asc }
          }
        }
      );
    }

    vm.getColumnIndex = function (name) {
      var vm = angular.element(document.querySelector('datadiscovery-data-draw')).isolateScope().vm;
      var index = vm.jexcel.options.columns.map(function (col) { return col.title }).indexOf(name);
      if (index == -1) {
        return vm.jexcel.options.columns.map(function (col) { return col.name }).indexOf(name);
      }
      return index;
    }

    vm.getSubTotalFields = function () {
      var returnArray = [];
      for(var i = 0; i < vm.columns.subtotalFields.length; i++){
        returnArray.push(vm.columns.list[vm.columns.subtotalFields[i]].field);
      }
      return returnArray;
    }

    vm.getFirstSubTotalIndex = function(){
      return vm.columns.subtotalFields.sort()[0];
    }

    vm.formatSubTotal = function(dataHash){
      var subtotalLabel = vm.config.prefixSubtotal;
      var defaultKey = "";
      for(var key in dataHash){
        var regex = new RegExp("\{" + key + "\}", "g");
        subtotalLabel = subtotalLabel.replace(regex,dataHash[key]);
        defaultKey += (defaultKey == ""?dataHash[key]:"-" + dataHash[key]);
      }
      var regex = new RegExp("\{\}", "g");
      subtotalLabel = subtotalLabel.replace(regex,defaultKey);
      
      return subtotalLabel;
    }

    vm.getSubtotalKey = function(row){
      var returnStringKey = "";
      for(var i = 0; i < vm.columns.subtotalFields.length; i++){
        returnStringKey += "-" + row[vm.normalizeField(vm.columns.list[vm.columns.subtotalFields[i]].field)];
      }
      return returnStringKey;
    }

    vm.getColumnKey = function(row,testColumns){
      var returnStringKey = "";
      for(var i = 0; i < testColumns.length; i++){
        returnStringKey += "-" + row[testColumns[i]];
      }
      return returnStringKey;
    }

    vm.fromJsonToRowTotalArray = function (data) {
      var rowArray = [];
      var lowindex = vm.jexcel.getHeaders(true).length, maxindex = 0;
      for (var d in data[0]) {
        var index = vm.getColumnIndex(d);
        rowArray[index] = ""+data[0][d];
        lowindex = Math.min(index, lowindex);
        maxindex = Math.max(index, maxindex);
      }
      //Add label Total
      if (lowindex > 0) {
        rowArray[lowindex - 1] = vm.config.prefixTotal;
      }
      else {
        rowArray[maxindex + 1] = vm.config.prefixTotal;
      }
      return rowArray;
    }

    vm.fromJsonToRowSubTotalArray = function (data) {
      var rowArray = [];
      var subtotalData = {};
      for (var d in data) {
        var index = vm.getColumnIndex(d);
        //Check column is data or subtotal
        if (vm.getSubTotalFields().map(function(f){return vm.normalizeField(f)}).indexOf(d)===-1) {
          rowArray[index] = ""+data[d];
        }
        else {
          subtotalData[d] = data[d];
        }
      }
      rowArray[vm.getFirstSubTotalIndex()] = vm.formatSubTotal(subtotalData);
      return rowArray;
    }

    vm.getJExcelColumnName = function (columnNumber) {
      var dividend = columnNumber;
      var columnName = "";
      var modulo;

      while (dividend > 0) {
        modulo = (dividend - 1) % 26;
        columnName = String.fromCharCode(65 + modulo) + columnName;
        dividend = parseInt((dividend - modulo) / 26);
      }

      return columnName;
    }

    vm.setTotalsJExcel = function (data, allData) {
      var index = allData.length - 1;
      var dataToInsert = vm.fromJsonToRowTotalArray(data);
      vm.jexcel.insertRow(dataToInsert, index);
      for (var i in dataToInsert) {
        vm.jexcel.setStyle(vm.getJExcelColumnName(parseInt(i) + 1) + (index + 2), 'font-weight', 'bold');
      }
      var deferred = $q.defer();
      deferred.resolve(allData);
      vm.columns.dataAccess.total.index = index;
      return deferred.promise;
    }

    vm.setSubTotalsJExcel = function (subTotalData, data) {
      //var refInstance = data[0][vm.getSubTotalField().replace(/\./g, "_").replace(/\[/g, "_").replace(/\]/g, "")];
      
      var refInstance = vm.getSubtotalKey(data[0]);
      var indexSubTotalData = 0;
      var indexToInsert = 0;
      vm.subTotalData = [];
      for (var inst = 0; inst < data.length; inst++) {
        var actRefInstance = vm.getSubtotalKey(data[inst]);
        //if (data[inst][vm.getSubTotalField().replace(/\./g, "_").replace(/\[/g, "_").replace(/\]/g, "")] !== refInstance) {
        if(actRefInstance !== refInstance){
          var dataToInsert = vm.fromJsonToRowSubTotalArray(subTotalData[indexSubTotalData], refInstance);
          var rowIndexSubtotal = indexToInsert - 1 + indexSubTotalData;
          vm.jexcel.insertRow(dataToInsert, rowIndexSubtotal);
          for (var i in dataToInsert) {
            vm.jexcel.setStyle(vm.getJExcelColumnName(parseInt(i) + 1) + (indexToInsert + 1 + indexSubTotalData), 'font-weight', 'bold');
          }
          indexSubTotalData++;
          refInstance = actRefInstance;
          vm.columns.dataAccess.subTotals.keyToIndex[refInstance] = {index: rowIndexSubtotal+1};
          vm.columns.dataAccess.subTotals.indexes.push(rowIndexSubtotal+1);
          vm.columns.dataAccess.total.index++;
        }
        indexToInsert++
      }
      var dataToInsert = vm.fromJsonToRowSubTotalArray(subTotalData[indexSubTotalData], refInstance);
      var rowIndexSubtotal = indexToInsert - 1 + indexSubTotalData;
      vm.jexcel.insertRow(dataToInsert, rowIndexSubtotal);
      for (var i in dataToInsert) {
        vm.jexcel.setStyle(vm.getJExcelColumnName(parseInt(i) + 1) + (indexToInsert + 1 + indexSubTotalData), 'font-weight', 'bold');
      }
      var deferred = $q.defer();
      deferred.resolve(data);
      vm.columns.dataAccess.subTotals.keyToIndex[refInstance] = {index: rowIndexSubtotal+1};
      vm.columns.dataAccess.subTotals.indexes.push(rowIndexSubtotal+1);
      vm.columns.dataAccess.total.index++;
      return deferred.promise;
    }

    vm.hasMetrics = function () {
      return vm.columns.list.filter(function (column) {
        return column.type === "metric";
      }).length > 0
    }

    vm.deleteJExcel = function () {
      if (vm.jexcel) {
        $element.find("div")[1].innerHTML = "";
        $element.find("div")[1].style.height = "0px";
      }
    }

    vm.mergeByColumnAttr = function () {
      if(vm.config.enableMergeCols){
        var data = vm.jexcel.getData();
        vm.nonMergedData = angular.copy(data);
        var inspectColumns = {}
        var notMetricColumns = []
        for (var ind in vm.columns.list) {
          if (vm.columns.list[ind].type !== 'metric') {
            notMetricColumns.push(ind);
            inspectColumns[ind] = { index: 0, value: data[0][ind] };
          }
        }
        var refInstance = vm.getColumnKey(data[0], notMetricColumns);
        for (var inst = 0; inst < data.length && ((vm.config.enableTotal&&vm.columns.dataAccess.total.index)?(vm.columns.dataAccess.subTotals.indexes.length>0?(vm.columns.dataAccess.total.index+1) > inst:(vm.columns.dataAccess.total.index+1) >= inst):true); inst++) {
          var actRefInstance = vm.getColumnKey(data[inst], notMetricColumns);
          var isSubtotalRow = vm.columns.dataAccess.subTotals.indexes.indexOf(inst) != -1;
          if (actRefInstance !== refInstance || isSubtotalRow) {
            for (var c in notMetricColumns) {
              var ncol = notMetricColumns[c];
              var newval = data[inst][ncol];
              if (isSubtotalRow) {
                for (var clast = 0; clast < notMetricColumns.length; clast++) {
                  var cell = vm.getJExcelColumnName(parseInt(notMetricColumns[clast]) + 1) + (inspectColumns[clast].index + 1);
                  vm.jexcel.setStyle(cell, 'border-right', '1px solid #CCC');
                  if (cell.indexOf("A") == 0) {
                    vm.jexcel.setStyle(cell, 'border-left', '1px solid #CCC');
                  }
                  if ((inst - inspectColumns[clast].index) > 1) {
                    vm.jexcel.setMerge(cell, 1, inst - (inspectColumns[clast].index));
                  }
                  if (data.length > inst + 2) {
                    inspectColumns[clast] = { index: inst + 1, value: data[inst + 1][notMetricColumns[clast]] };
                  }
                }
                refInstance = vm.getColumnKey(data[inst], notMetricColumns);
                break;
              }
              else if (inspectColumns[ncol].value !== newval) {
                for (var clast = c; clast < notMetricColumns.length; clast++) {
                  var cell = vm.getJExcelColumnName(parseInt(notMetricColumns[clast])+1)+ (inspectColumns[clast].index+1);
                  vm.jexcel.setStyle(cell, 'border-right', '1px solid #CCC');
                  if (cell.indexOf("A") == 0) {
                    vm.jexcel.setStyle(cell, 'border-left', '1px solid #CCC');
                  }
                  if ((inst - inspectColumns[clast].index) > 1) {
                    vm.jexcel.setMerge(cell, 1, inst - (inspectColumns[clast].index));
                  }
                  if (data.length > inst + 1) {
                    inspectColumns[clast] = { index: inst, value: data[inst][notMetricColumns[clast]] };
                  }
                }
                refInstance = actRefInstance;
                break;
              }
            }
          }
        }
      }
      else{
        vm.nonMergedData = null;
      }
    }

    vm.drawRedrawJExcel = function (data) {
      var deferred = $q.defer();

      var headers;
      if(data.length){//With data
        if(!Array.isArray(data[0])){//form array of json (from datasource)
          headers = vm.columns.list.map(function (column) {
            return { title: (!column.alias || column.alias == "" ? vm.normalizeField(column.field) : column.alias), name: vm.normalizeField(column.field), type: ((typeof data[0][column.field] == 'number' || typeof data[0][column.field] == 'integer') ? 'numeric' : 'text') }
          })
        }
        else{//from array of array (from saved data)
          headers = vm.columns.list.map(function (column,index ) {
            if(!vm.config.adjustColumnToView){
              return { title: vm.config.discovery.matrix.headers[index].name, width: vm.config.discovery.matrix.headers[index].width ,type: ((typeof data[0][column.field] == 'number' || typeof data[0][column.field] == 'integer') ? 'numeric' : 'text') }
            }
            else{//no set width, adjust to view
              return { title: vm.config.discovery.matrix.headers[index].name ,type: ((typeof data[0][column.field] == 'number' || typeof data[0][column.field] == 'integer') ? 'numeric' : 'text') }
            }
          })
        }
      }
      else{//No data, all default text or alias
        var headers = vm.columns.list.map(function (column) {
          return { title: (!column.alias || column.alias == "" ? vm.normalizeField(column.field) : column.alias), name: vm.normalizeField(column.field), type: 'text' }
        })
      }

      vm.deleteJExcel();

      var jexcelOptions = {
        data: data,
        columns: headers,
        columnDrag: vm.config.editGrid,
        columnResize: vm.config.editGrid,
        rowResize: vm.config.editGrid,
        editable: vm.config.editGrid,
        lazyLoading: false,
        search:vm.config.showSearch,
        defaultColWidth: (vm.config.adjustColumnToView?($element[0].offsetWidth-40-(vm.config.showRowNum?50:0))/(Math.max(headers.length,vm.config.baseCols)):100),
        tableOverflow: true,
        columnSorting:false,
        minDimensions: [vm.config.baseCols, vm.config.baseRows],
        tableHeight: $element[0].offsetHeight - (vm.config.showSearch?40:0) - (vm.config.editGrid?50:0) - 37 + "px",
        toolbar: (vm.config.editGrid?[
          { type: 'i', content: 'undo', onclick: function () { vm.jexcel.undo(); } },
          { type: 'i', content: 'redo', onclick: function () { vm.jexcel.redo(); } },
          { type: 'i', content: 'save', onclick: function () { vm.jexcel.download(); } },
          { type: 'select', k: 'font-family', v: ['Arial', 'Verdana'] },
          { type: 'select', k: 'font-size', v: ['9px', '10px', '11px', '12px', '13px', '14px', '15px', '16px', '17px', '18px', '19px', '20px'] },
          { type: 'i', content: 'format_align_left', k: 'text-align', v: 'left' },
          { type: 'i', content: 'format_align_center', k: 'text-align', v: 'center' },
          { type: 'i', content: 'format_align_right', k: 'text-align', v: 'right' },
          { type: 'i', content: 'format_bold', k: 'font-weight', v: 'bold' },
          { type: 'color', content: 'format_color_text', k: 'color' },
          { type: 'color', content: 'format_color_fill', k: 'background-color' }
        ]:[]),
        updateTable: customUpdateTable,
        onsort: function(a,b,c,d,e){
          
        }
      }

      if(!vm.config.editGrid){
        jexcelOptions["contextMenu"] = false
      }

      vm.jexcel = jexcel($element.find("div")[1], jexcelOptions);

      if(vm.config.showRowNum){
        document.querySelector("table.jexcel tr td:first-child").style.opacity=1;
        document.querySelector("table.jexcel").style.marginLeft="0px";
      }
      else{
        document.querySelector("table.jexcel tr td:first-child").style.opacity=0;
        document.querySelector("table.jexcel").style.marginLeft="-50px";
      }

      //Set context menú parent to dashbaord in order to set right position
      var contextmenu = document.querySelector(".jexcel_contextmenu");
      document.getElementsByTagName("body")[0].appendChild(contextmenu);

      document.querySelectorAll(".jexcel thead tr td").forEach(
        function(elem){
          elem.ondblclick = function(){
            vm.avoidReload = true;
            vm.columns.list[this.getAttribute("data-x")].asc = !vm.columns.list[this.getAttribute("data-x")].asc;
            vm.redrawView();
          }
        }
      )

      //Solve promise
      deferred.resolve(data);
      return deferred.promise;
    }

    function customUpdateTable(instance, cell, col, row, val, label, cellName){
      if(vm.blockUpdateTable){
        return false;
      }
      var conds;
      if(vm.columns.list[col]){
        var conds = vm.columns.list[col].condstyles;
      }
      var i = 0;          
      var found = false;
      while(conds && i < conds.length){              
        switch(conds[i].cond){
          case "all":
            found=true;
            break;
          case "data":
            if(row-1 < vm.columns.dataAccess.total.index && vm.columns.dataAccess.subTotals.indexes.indexOf(row) === -1){
              found=true;
            }
            break;
          case "total":
            if(row-1 === vm.columns.dataAccess.total.index){
              found=true;
            }
            break;
          case "subtotals":
            if(vm.columns.dataAccess.subTotals.indexes.indexOf(row) !== -1){
              found=true;
            }
            break;
          case "equal":
            if(val == conds[i].val){
              found=true;
            }
            break;
          case "mayorequal":
            if(val >= conds[i].val){
              found=true;
            }
            break;
          case "minorequal":
            if(val <= conds[i].val){
              found=true;
            }
            break;
          case "mayor":
            if(val > conds[i].val){
              found=true;
            }
            break;
          case "minor":
            if(val < conds[i].val){
              found=true;
            }
            break;
          case "between":
            if(val >= conds[i].val && val <= conds[i].val2){
              found=true;
            }
            break;
          case "in":
            if(conds[i].val.indexOf(val) != -1){
              found=true;
            }
            break;
        }
        if(found){
          cell.style.cssText = cell.style.cssText + ";" + conds[i].style;
          if(conds[i].vfunction){
            var f = eval("(" + vm.preprocessFunction(conds[i].vfunction, instance, cell, col, row, val, label, cellName) + ")");
            cell.innerHTML = f(val, col, row, instance, cell, label, cellName);
          }
          found = false;
        }        
        i++;
      }      
    }

    vm.preprocessFunction =function(f, instance, cell, col, row, val, label, cellName){
      //replace $total
      if(vm.config.enableTotal){
        
        //replace $total(col)
        var regexp = new RegExp("\\$total\\((.+?)\\)","g");
        do
        {
          var match = regexp.exec(f);
          if(match){
            var innerRegexp = new RegExp("\\$total\\(" + match[1] + "\\)","g")
            f = f.replace(innerRegexp, instance.jexcel.getColumnData(vm.getColumnIndex(match[1]))[vm.columns.dataAccess.total.index+1]);
          }
        }
        while(match)
        
        var regexp = new RegExp("\\$total","g");
        f = f.replace(regexp, instance.jexcel.getColumnData(col)[vm.columns.dataAccess.total.index+1]);
      }

      //replace $subtotal(col)
      var regexp = new RegExp("\\$subtotal\\((.+?)\\)","g");
      do
      {
        var match = regexp.exec(f);
        if(match){
          var innerRegexp = new RegExp("\\$subtotal\\(" + match[1] + "\\)","g")
          f = f.replace(innerRegexp, instance.jexcel.getColumnData(vm.getColumnIndex(match[1]))[vm.columns.dataAccess.subTotals.indexes.filter(function(e){return e > row})[0]]);
        }
      }
      while(match)

      //replace $subtotal
      var regexp = new RegExp("\\$subtotal","g");
      f = f.replace(regexp, instance.jexcel.getColumnData(col)[vm.columns.dataAccess.subTotals.indexes.filter(function(e){return e > row})[0]]);

      return f;
    }

    vm.isSubTotalRow = function(row){
      return vm.columns.dataAccess.subTotals.indexes.indexOf(row) != -1;
    }

    vm.isSubTotalRowKey = function(row, key){
      return vm.columns.dataAccess.subTotals.indexes.indexOf(row) != -1;
    }

    vm.isTotalRow = function(row){
      return vm.columns.dataAccess.total.index === row;
    }

    vm.resizeJExcel = function(){
      if(vm.columns.list && vm.columns.list.length && vm.status === "ready"){
        $timeout(
          function(){
            vm.drawRedrawJExcel(vm.nonMergedData?vm.nonMergedData:vm.jexcel.getData());
            vm.mergeByColumnAttr();
          },300
        )
      }
    }

    vm.normalizeField = function(f){
      return f.replace(/\./g, "_").replace(/\[/g, "_").replace(/\]/g, "")
    }

    vm.generateSortHighPreferenceSubtotalsFields = function(){
      var sortSubtotalHighPreference = [];
      var notSubtotalFields = [];
      var userSort = vm.getSortFields();
      var subtotals = vm.getSubTotalFields();
      for(var i=0;i<userSort.length;i++){
        if(subtotals.indexOf(userSort[i].field) != -1){
          sortSubtotalHighPreference.push(userSort[i]);
        }
        else{
          notSubtotalFields.push(userSort[i]);
        }
      }
      return sortSubtotalHighPreference.concat(notSubtotalFields);
    }

    vm.sendQueryTotals = function () {
      return vm.from(vm.datasource.identification).filter(getDataStatusFilters()).select(vm.getSelectMetrics()).exec()
    }

    vm.sendQuerySubTotals = function () {
      return vm.from(vm.datasource.identification).filter(getDataStatusFilters()).group(vm.getSubTotalFields()).select(vm.getSelectMetrics().concat(vm.getSubTotalFields().map(function(f){return {"field":f,"alias":vm.normalizeField(f)}}))).sort(vm.getSortFields().filter(function(sfield){return vm.getSubTotalFields().indexOf(sfield.field) != -1})).exec()
    }

    vm.sendQuery = function () {
      return vm.from(vm.datasource.identification).filter(getDataStatusFilters()).group(vm.getGroupFields()).select(vm.getSelectFields()).sort(vm.generateSortHighPreferenceSubtotalsFields()).exec();
    }

    function getDataStatusFilters(){
      var filters = (vm.datastatus && vm.datastatus.length)?datasourceSolverService.buildFilterStt({id:vm.id,data:vm.datastatus})["filter"]["data"]:[];
      filters.concat(urlParamService.generateFiltersForGadgetId(vm.id))
      //Add initial datalink
      filters = interactionService.generateFiltersForGadgetIdWithDatastatus(vm.id, addDatastatus, filters);
      return filters;      
    }

    function eventDProcessor(event, dataEvent) {
      if (dataEvent.type === "data" && dataEvent.data.length === 0) {
        //Do nothing
      }
      else {
        switch (dataEvent.type) {
          case "data":
            //Do nothing datadiscovery solve it own datasource
            break;
          case "filter":
            if (!vm.datastatus) {
              vm.datastatus = [];
            }
            if (dataEvent.data.length) {
              for (var index in dataEvent.data) {
                addDatastatus(dataEvent, index);
              }

            }
            else {
              deleteDatastatus(dataEvent);
            }

            if (typeof vm.datastatus === 'undefined') {
              dataEvent.data = [];
            } else {
              dataEvent.data = vm.datastatus.filter(function (elem) { return elem.id === dataEvent.id });
            }

            vm.redrawView();
            break;
          case "action":
            //Do nothing
            break;
          case "value":
            //Do nothing
            break
          default:
            console.error("Not allowed event: " + dataEvent.type);
            break;
        }
      }
      utilsService.forceRender($scope);
    }

    function addDatastatus(dataEvent, index) {

      if (!vm.datastatus) {
        vm.datastatus = [];

        vm.datastatus.push({
          field: dataEvent.data[index].field,
          value: angular.copy(dataEvent.data[index].value),
          id: angular.copy(dataEvent.id),
          op: angular.copy(dataEvent.data[index].op),
          idFilter: angular.copy(dataEvent.data[index].idFilter),
          name: angular.copy(dataEvent.data[index].name)
        });
      } else {
        var exist = false;
        for (var i = 0; i < vm.datastatus.length; i++) {
          var element = vm.datastatus[i];
          if (vm.datastatus[i].idFilter === dataEvent.data[index].idFilter
            && vm.datastatus[i].op === dataEvent.data[index].op) {
            vm.datastatus[i] = {
              field: dataEvent.data[index].field,
              value: angular.copy(dataEvent.data[index].value),
              id: angular.copy(dataEvent.id),
              op: angular.copy(dataEvent.data[index].op),
              idFilter: angular.copy(dataEvent.data[index].idFilter),
              name: angular.copy(dataEvent.data[index].name)
            }
            exist = true;
            break
          }

        }
        if (!exist) {
          vm.datastatus.push({
            field: dataEvent.data[index].field,
            value: angular.copy(dataEvent.data[index].value),
            id: angular.copy(dataEvent.id),
            op: angular.copy(dataEvent.data[index].op),
            idFilter: angular.copy(dataEvent.data[index].idFilter),
            name: angular.copy(dataEvent.data[index].name)
          })
        }
      }
      vm.datastatus = vm.datastatus.filter(function(s){return s.value!==null;});
      
    }

    function deleteDatastatus(dataEvent) {
      if (typeof vm.datastatus !== 'undefined') {
        for (var index = 0; index < vm.datastatus.length; index++) {
          var element = vm.datastatus[index];
          if (typeof dataEvent.op !== 'undefined' && dataEvent.op !== null && typeof element.op !== 'undefined' && element.op !== null) {
            if (element.field === dataEvent.field && element.id === dataEvent.id && element.op === dataEvent.op) {
              vm.datastatus.splice(index, 1);
            }
          } else {
            if (element.field === dataEvent.field && element.id === dataEvent.id) {
              vm.datastatus.splice(index, 1);
            }
          }

        }
        if (vm.datastatus.length === 0) {
          vm.datastatus = undefined;
        }
      }
    }
  }

})();