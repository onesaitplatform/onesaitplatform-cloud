(function () {
  'use strict';

  angular.module('dashboardFramework')
    .service('utilsService', UtilsService);

  /** @ngInject */
  function UtilsService(__env,httpService) {
    var vm = this;

    //force angular render in order to fast refresh view of component. $scope is pass as argument for render only this element
    vm.forceRender = function ($scope) {
      if (!$scope.$$phase) {
        $scope.$applyAsync();
      }
    }

    //Access json by string dot path
    function multiIndex(obj, is, pos) {  // obj,['1','2','3'] -> ((obj['1'])['2'])['3']
      if (is.length && !(is[0] in obj)) {
        return obj[is[is.length - 1]];
      }
      return is.length ? multiIndex(obj[is[0]], is.slice(1), pos) : obj
    }

    function isNormalInteger(str) {
      var n = Math.floor(Number(str));
      return n !== Infinity && String(n) === str && n >= 0;
    }

    vm.replaceBrackets = function (obj) {
      obj = obj.replace(/[\[]/g, ".");
      obj = obj.replace(/[\]]/g, "");
      return obj;
    }

    vm.getJsonValueByJsonPath = function (obj, is, pos) {
      //special case for array access, return key is 0, 1
      var matchArray = is.match(/\[[0-9]\]*$/);
      if (matchArray) {
        //Get de match in is [0] and get return field name
        return obj[pos];
      }
      return multiIndex(obj, is.split('.'))
    }

    //array transform to sorted and unique values
    vm.sort_unique = function (arr) {
      if (arr.length === 0) return arr;
      var sortFn;
      if (typeof arr[0] === "string") {//String sort
        sortFn = function (a, b) {
          if (a < b) return -1;
          if (a > b) return 1;
          return 0;
        }
      }
      else {//Number and date sort
        sortFn = function (a, b) {
          return a * 1 - b * 1;
        }
      }
      arr = arr.sort(sortFn);
      var ret = [arr[0]];
      for (var i = 1; i < arr.length; i++) { //Start loop at 1: arr[0] can never be a duplicate
        if (arr[i - 1] !== arr[i]) {
          ret.push(arr[i]);
        }
      }
      return ret;
    }

    //array transform to sorted and unique values
    vm.sort_jsonarray = function(arr,sortfield) {
      if (arr.length === 0) return arr;
      var sortFn;
      if(typeof arr[0][sortfield] === "string"){//String sort
        sortFn = function (a, b) {
          if(a[sortfield] < b[sortfield]) return -1;
          if(a[sortfield] > b[sortfield]) return 1;
          return 0;
        }
      }
      else{//Number and date sort
        sortFn = function (a, b) {
          return a[sortfield]*1 - b[sortfield]*1;
        }
      }
      return arr.sort(sortFn);
    }

    vm.isSameJsonInArray = function (json, arrayJson) {
      for (var index = 0; index < arrayJson.length; index++) {
        var equals = true;
        for (var key in arrayJson[index]) {
          if (arrayJson[index][key] != json[key]) {
            equals = false;
            break;
          }
        }
        if (equals) {
          return true;
        }
      }
      return false;
    }  

    vm.getJsonFields = function iterate(obj, stack, fields) {
      for (var property in obj) {
        if (obj.hasOwnProperty(property)) {
          if (typeof obj[property] == "object") {
            vm.getJsonFields(obj[property], stack + (stack == "" ? '' : '.') + property, fields);
          } else {
            fields.push({ field: stack + (stack == "" ? '' : '.') + property, type: typeof obj[property] });
          }
        }
      }
      return fields;
    }



    function distinct(value, index, self) {
      return self.indexOf(value) === index;
    }

    vm.uniqueArray = function (arr) {
      if (typeof arr !== undefined) {
        return arr.filter(distinct);
      }
      return arr;
    }



    vm.transformJsonFieldsArrays = function (fields) {
      var transformArrays = [];
      for (var fieldAux in fields) {
        var pathFields = fields[fieldAux].field.split(".");
        var realField = pathFields[0];
        for (var i = 1; i < pathFields.length; i++) {
          if (isNormalInteger(pathFields[i])) {
            pathFields[i] = "[" + pathFields[i] + "]"
            realField += pathFields[i];
          }
          else {
            realField += "." + pathFields[i];
          }
        }
        transformArrays.push({ field: realField, type: fields[fieldAux].type });
      }
      return transformArrays;
    }

    vm.urlParamLang = function () {
      //controlar si ponen minúsculas o mayusculas
      var urlSearch = window.location.search;
      var searchParam = new URLSearchParams(urlSearch);
      var lang = searchParam.get("lang");
      return (lang?lang.toUpperCase():"");
    }

    vm.getMarkerForMap = function (value, jsonMarkers) {

      var result = {
        type: 'vectorMarker',
        icon: 'circle',
        markerColor: 'blue',
        iconColor: "white"
      }
      var found = false;
      for (var index = 0; index < jsonMarkers.length && !found; index++) {
        var limit = jsonMarkers[index];
        var minUndefined = typeof limit.min == "undefined";
        var maxUndefined = typeof limit.max == "undefined";

        if (!minUndefined && !maxUndefined) {
          if (value <= limit.max && value >= limit.min) {
            result.icon = limit.icon;
            result.markerColor = limit.markerColor;
            result.iconColor = limit.iconColor;
            found = true;
          }
        } else if (!minUndefined && maxUndefined) {
          if (value >= limit.min) {
            result.icon = limit.icon;
            result.markerColor = limit.markerColor;
            result.iconColor = limit.iconColor;
            found = true;
          }

        } else if (minUndefined && !maxUndefined) {
          if (value <= limit.max) {
            result.icon = limit.icon;
            result.markerColor = limit.markerColor;
            result.iconColor = limit.iconColor;
            found = true;
          }

        }

      }

      return result;
    }

    vm.isEmptyJson = function (obj) {
      return Object.keys(obj).length === 0 && obj.constructor === Object;
    }

    /**method that finds the tags in the given text*/
    vm.searchTag = function(regex,str){
      var m;
      var found=[];
      while ((m = regex.exec(str)) !== null) {  
          if (m.index === regex.lastIndex) {
              regex.lastIndex++;
          }
          m.forEach(function(item, index, arr){			
          found.push(arr[0]);			
        });  
      }
      return found;
    }


    vm.searchTagContentDescriptionOrName = function(regexDescription,regexName, str){
      var tag = vm.searchTagContentName(regexDescription,str);
      if(typeof tag=='undefined' || tag==null || tag.length==0 ){
        tag = vm.searchTagContentName(regexName,str);
      }
      return tag;
    }
    


    vm.searchTagContentName = function(regex,str){
      var m;
      var content;
      while ((m = regex.exec(str)) !== null) {  
          if (m.index === regex.lastIndex) {
              regex.lastIndex++;
          }
          m.forEach(function(item, index, arr){			
            content = arr[0].match(/"([^"]+)"/)[1];			
        });  
      }
      return content;
    }

    /**method that finds the options attribute and returns its values in the given tag */
    vm.searchTagContentOptions = function(regex,str){
      var m;
      var content=" ";
      while ((m = regex.exec(str)) !== null) {  
          if (m.index === regex.lastIndex) {
              regex.lastIndex++;
          }
          m.forEach(function(item, index, arr){			
            content = arr[0].match(/"([^"]+)"/)[1];			
        });  
      }
    
      return  content.split(',');
    }

    /**find a value for a given parameter */
    function findValueForParameter(parameters,label,jsparam,number){
      for (var index = 0; index <  parameters.length; index++) {
        var element =  parameters[index];
        if(element.label===label){
          if(!jsparam){
            return element.value;
          }
          else{
            if(number){
              return element.value + " || ";
            }
            else{
              return "'" + element.value + "' || ";
            }
          }
        }
      }
    }

    /**Parse the parameter of the data source so that it has array coding*/
    function parseArrayPosition(str){
      var regex = /\.[\d]+/g;
      var m;              
      while ((m = regex.exec(str)) !== null) {                
          if (m.index === regex.lastIndex) {
              regex.lastIndex++;
          } 
          m.forEach( function(item, index, arr){             
            var index = arr[0].substring(1,arr[0].length)
            var result =  "["+index+"]";
            str = str.replace(arr[0],result) ;
          });
      }
      return str;
    }

    vm.flattenObj = function (ob) {
      var toReturn = {};

      for (var i in ob) {
        if (!ob.hasOwnProperty(i)) continue;

        if ((typeof ob[i]) == 'object' && ob[i] !== null) {
          var flatObject = vm.flattenObj(ob[i]);
          for (var x in flatObject) {
            if (!flatObject.hasOwnProperty(x)) continue;

            toReturn[i + '.' + x] = flatObject[x];
          }
        } else {
          toReturn[i] = ob[i];
        }
      }
      return toReturn;
    }

    vm.unflattenObj = function (data) {
      var result = {}
      for (var i in data) {
        var keys = i.split('.')
        keys.reduce(function (r, e, j) {
          return r[e] || (r[e] = isNaN(Number(keys[j + 1])) ? (keys.length - 1 == j ? data[i] : {}) : [])
        }, result)
      }
      return result
    }

    vm.setRecProperty = function (obj, spath, value) {
      var auxobj = obj;
      var paths = spath.split(".");
      for (var p = 0; p < paths.length; p++) {
        var path = paths[p];
        if (!auxobj.hasOwnProperty(path)) {
          auxobj[path] = {}
        }
        if (p === (paths.length-1)) {
          auxobj[path] = value
        } else {
          auxobj = auxobj[path]
        }
      }
    }

    vm.getDefaultParams = function(gform) {
      function getDefault(elements, localvalue) {
        for (var element in elements) {
          if (elements[element].elements && elements[element].elements.length > 0) {
            localvalue[elements[element].name] = {}
            getDefault(elements[element].elements, localvalue[elements[element].name])
          } else {
            localvalue[elements[element].name] = JSON.parse(JSON.stringify(elements[element].default == undefined ? null : elements[element].default))
          }
        }
      }
      var defaultParams = {}
      getDefault(gform, defaultParams);
      return defaultParams;
    }

    vm.reassign = function(gform, parameters) { //reassign parameters to other level of gform. Only for saved parameters not used with 1 level of deep
      var defaultParams = vm.getDefaultParams(gform); //we get default params of gform

      var notUsedParams = [];
      for (var key in parameters.parameters) { // we get the not used params: params in parameters.parameters and not in defaultParams
        if (!defaultParams.hasOwnProperty(key)) {
          notUsedParams.push(key)
        }
      }
      if (notUsedParams.length > 0) {
        var fdparams = Object.keys(vm.flattenObj(defaultParams)).filter(function(key){ //defaultParams with more than 1 level of deep flattened. 
          return key.indexOf(".") != -1;
        });
        for (var i in notUsedParams) {
          var param = notUsedParams[i];
          for (var j in fdparams) {
            var fdpath = fdparams[j];
            if (fdpath.endsWith("." + param)) { //if recursive param in defaultparams ends with .name of not used param, the value will be reassing in this position in paramters.parameters
              vm.setRecProperty(parameters.parameters, fdpath, parameters.parameters[param]);
              delete parameters.parameters[param]
              break;
            }
          }
        }
      }
      return parameters;
    }

    vm.isValidObject = function(obj) { //obj is object and not array or null
      return typeof obj === 'object' && !Array.isArray(obj) && obj !== null
    }

    vm.cleanDashboardTempFields = function (dashboard) {
      var cleanDashboard = dashboard;
      return cleanDashboard
    }

    vm.deepMerge = function () {
      // create a new object
      var target = {};

      // deep merge the object into the target object
      var merger = function(obj) {
        for (var prop in obj) {
          if (obj.hasOwnProperty(prop)) {
            if (Object.prototype.toString.call(obj[prop]) === '[object Object]') {
              // if the property is a nested object
              target[prop] = vm.deepMerge(target[prop], obj[prop]);
            } else {
              // for regular property
              target[prop] = obj[prop];
            }
          }
        }
      };

      // iterate through all objects and 
      // deep merge them with target
      for (var i = 0; i < arguments.length; i++) {
        merger(arguments[i]);
      }

      return target;
    }

    vm.fillWithDefaultFormData = function(paramsori, gform) {
      var params = JSON.parse(JSON.stringify(paramsori));
      if (!window.__env.dashboardEngineAvoidFillDefault) {
        var defaultParams = vm.getDefaultParams(gform);
        if (vm.isValidObject(params) && Array.isArray(gform)) {
          if (params.hasOwnProperty("parameters")) {
            if (vm.isValidObject(params.parameters)) {
              var auxParams = vm.deepMerge(defaultParams, params.parameters);
              params.parameters = auxParams;
            } else {
              console.info("Template can't be fill with default data because params.parameters exists but is not a valid object")
            }
          } else {
            var auxParams = vm.deepMerge(defaultParams, params);
            params.parameters = auxParams;
          }
        } else {
          console.info("Template can't be fill with default data because params or defaultParams are not defined")
        }
      }
      return params;
    }

    vm.legacyToNewParamsWithDatasource = function(parameters, datasource) { //return new params for legacy or parameters with stt {parameters:{...},datasource:{...}}
      var auxparameters = {}
      if (Array.isArray(parameters)) { // from legacy to new params
        auxparameters['parameters'] = vm.legacyToNewParams(parameters);
        auxparameters['datasource'] = datasource;
      } else {
        auxparameters = parameters;
      }
      return auxparameters;
    }

    vm.legacyToNewParams = function(parameters) { // return convertion of legacy params to new params, only for parameters without datasource
      var auxparameters = {}
      if (Array.isArray(parameters)) { // from legacy to new params
        for (var i = 0; i < parameters.length ; i++) {
          var param = parameters[i];
          auxparameters[param.label] = typeof param.value === 'object' && param.value !== null ? param.value.field : param.value;
        }
      } else {
        auxparameters = parameters;
      }
      return auxparameters;
    }

    /** this function Replace parameteres for his selected values*/
    vm.parseProperties = function(str,parameters,jsparam){
      var regexTagHTML =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\-->/g;
      var regexTagJS =  /\/\*[\-\-\s\w\>\=\"\'\,\:\+\_\/]*\*\//g;
      var regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;
      var regexOptions = /options\s*=\s*\"[\s\w\>\=\-\'\:\,\+\_\/]*\s*\"/g;
      var found=[];
      found = vm.searchTag(regexTagHTML,str).concat(vm.searchTag(regexTagJS,str));	
      
      var auxparameters = vm.legacyToNewParams(parameters);

      var parserList=[];
      for (var i = 0; i < found.length; i++) {
        var tag = found[i];	
        
        function getKeyRec(paramMap, key) {
          for (var k in paramMap) {
            if (typeof paramMap[k] === "object") {
              var ret = getKeyRec(paramMap[k], key)
              if (ret) {
                return ret;
              }
            } else if (key in paramMap) {
              return paramMap[key];
            }		
          }
          return null
        }
        var key = vm.searchTagContentName(regexName,tag);
        var value = getKeyRec(auxparameters, key);
        if(tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0 ||
           tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0 ||
           tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-osp')>=0){
          parserList.push({tag:tag,value:(jsparam?("'" + value + "' || "):value)});   
        }else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
          parserList.push({tag:tag,value:value + (jsparam?" || ":"")});   
        }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){                
          var field = value;
          if(!jsparam){                             
            parserList.push({tag:tag,value:"{{ds[0]."+field+"}}"});
          }
          else{
            parserList.push({tag:tag,value:"ds[0]."+field+" || "});
          }
        }
      } 
      //Replace parameteres for his values
      for (var i = 0; i < parserList.length; i++) {
        str = str.replace(parserList[i].tag,parserList[i].value);
      }
      return str;
    }

  //function for create custom gadget
  vm.createCustomGadget = function(config,type){    
   var identification = config.identification;
   var description = config.description; 
   if(!config.identification){
    identification="customgadget"+(new Date()).getTime();
   }
   if(!description){
     description = identification;
   }
   delete config.identification;
   delete config.description;
    var gadget = {
      "identification": identification,
      "description": description,               
      "config": JSON.stringify(config),
      "gadgetMeasures": [],
      "type": type,
      "instance":true
    }
    return httpService.createGadget(gadget);
  }


    vm.icons = [
      "3d_rotation",
      "ac_unit",
      "access_alarm",
      "access_alarms",
      "access_time",
      "accessibility",
      "accessible",
      "account_balance",
      "account_balance_wallet",
      "account_box",
      "account_circle",
      "adb",
      "add",
      "add_a_photo",
      "add_alarm",
      "add_alert",
      "add_box",
      "add_circle",
      "add_circle_outline",
      "add_location",
      "add_shopping_cart",
      "add_to_photos",
      "add_to_queue",
      "adjust",
      "airline_seat_flat",
      "airline_seat_flat_angled",
      "airline_seat_individual_suite",
      "airline_seat_legroom_extra",
      "airline_seat_legroom_normal",
      "airline_seat_legroom_reduced",
      "airline_seat_recline_extra",
      "airline_seat_recline_normal",
      "airplanemode_active",
      "airplanemode_inactive",
      "airplay",
      "airport_shuttle",
      "alarm",
      "alarm_add",
      "alarm_off",
      "alarm_on",
      "album",
      "all_inclusive",
      "all_out",
      "android",
      "announcement",
      "apps",
      "archive",
      "arrow_back",
      "arrow_downward",
      "arrow_drop_down",
      "arrow_drop_down_circle",
      "arrow_drop_up",
      "arrow_forward",
      "arrow_upward",
      "art_track",
      "aspect_ratio",
      "assessment",
      "assignment",
      "assignment_ind",
      "assignment_late",
      "assignment_return",
      "assignment_returned",
      "assignment_turned_in",
      "assistant",
      "assistant_photo",
      "attach_file",
      "attach_money",
      "attachment",
      "audiotrack",
      "autorenew",
      "av_timer",
      "backspace",
      "backup",
      "battery_alert",
      "battery_charging_full",
      "battery_full",
      "battery_std",
      "battery_unknown",
      "beach_access",
      "beenhere",
      "block",
      "bluetooth",
      "bluetooth_audio",
      "bluetooth_connected",
      "bluetooth_disabled",
      "bluetooth_searching",
      "blur_circular",
      "blur_linear",
      "blur_off",
      "blur_on",
      "book",
      "bookmark",
      "bookmark_border",
      "border_all",
      "border_bottom",
      "border_clear",
      "border_color",
      "border_horizontal",
      "border_inner",
      "border_left",
      "border_outer",
      "border_right",
      "border_style",
      "border_top",
      "border_vertical",
      "branding_watermark",
      "brightness_1",
      "brightness_2",
      "brightness_3",
      "brightness_4",
      "brightness_5",
      "brightness_6",
      "brightness_7",
      "brightness_auto",
      "brightness_high",
      "brightness_low",
      "brightness_medium",
      "broken_image",
      "brush",
      "bubble_chart",
      "bug_report",
      "build",
      "burst_mode",
      "business",
      "business_center",
      "cached",
      "cake",
      "call",
      "call_end",
      "call_made",
      "call_merge",
      "call_missed",
      "call_missed_outgoing",
      "call_received",
      "call_split",
      "call_to_action",
      "camera",
      "camera_alt",
      "camera_enhance",
      "camera_front",
      "camera_rear",
      "camera_roll",
      "cancel",
      "card_giftcard",
      "card_membership",
      "card_travel",
      "casino",
      "cast",
      "cast_connected",
      "center_focus_strong",
      "center_focus_weak",
      "change_history",
      "chat",
      "chat_bubble",
      "chat_bubble_outline",
      "check",
      "check_box",
      "check_box_outline_blank",
      "check_circle",
      "chevron_left",
      "chevron_right",
      "child_care",
      "child_friendly",
      "chrome_reader_mode",
      "class",
      "clear",
      "clear_all",
      "close",
      "closed_caption",
      "cloud",
      "cloud_circle",
      "cloud_done",
      "cloud_download",
      "cloud_off",
      "cloud_queue",
      "cloud_upload",
      "code",
      "collections",
      "collections_bookmark",
      "color_lens",
      "colorize",
      "comment",
      "compare",
      "compare_arrows",
      "computer",
      "confirmation_number",
      "contact_mail",
      "contact_phone",
      "contacts",
      "content_copy",
      "content_cut",
      "content_paste",
      "control_point",
      "control_point_duplicate",
      "copyright",
      "create",
      "create_new_folder",
      "credit_card",
      "crop",
      "crop_16_9",
      "crop_3_2",
      "crop_5_4",
      "crop_7_5",
      "crop_din",
      "crop_free",
      "crop_landscape",
      "crop_original",
      "crop_portrait",
      "crop_rotate",
      "crop_square",
      "dashboard",
      "data_usage",
      "date_range",
      "dehaze",
      "delete",
      "delete_forever",
      "delete_sweep",
      "description",
      "desktop_mac",
      "desktop_windows",
      "details",
      "developer_board",
      "developer_mode",
      "device_hub",
      "devices",
      "devices_other",
      "dialer_sip",
      "dialpad",
      "directions",
      "directions_bike",
      "directions_boat",
      "directions_bus",
      "directions_car",
      "directions_railway",
      "directions_run",
      "directions_subway",
      "directions_transit",
      "directions_walk",
      "disc_full",
      "dns",
      "do_not_disturb",
      "do_not_disturb_alt",
      "do_not_disturb_off",
      "do_not_disturb_on",
      "dock",
      "domain",
      "done",
      "done_all",
      "donut_large",
      "donut_small",
      "drafts",
      "drag_handle",
      "drive_eta",
      "dvr",
      "edit",
      "edit_location",
      "eject",
      "email",
      "enhanced_encryption",
      "equalizer",
      "error",
      "error_outline",
      "euro_symbol",
      "ev_station",
      "event",
      "event_available",
      "event_busy",
      "event_note",
      "event_seat",
      "exit_to_app",
      "expand_less",
      "expand_more",
      "explicit",
      "explore",
      "exposure",
      "exposure_neg_1",
      "exposure_neg_2",
      "exposure_plus_1",
      "exposure_plus_2",
      "exposure_zero",
      "extension",
      "face",
      "fast_forward",
      "fast_rewind",
      "favorite",
      "favorite_border",
      "featured_play_list",
      "featured_video",
      "feedback",
      "fiber_dvr",
      "fiber_manual_record",
      "fiber_new",
      "fiber_pin",
      "fiber_smart_record",
      "file_download",
      "file_upload",
      "filter",
      "filter_1",
      "filter_2",
      "filter_3",
      "filter_4",
      "filter_5",
      "filter_6",
      "filter_7",
      "filter_8",
      "filter_9",
      "filter_9_plus",
      "filter_b_and_w",
      "filter_center_focus",
      "filter_drama",
      "filter_frames",
      "filter_hdr",
      "filter_list",
      "filter_none",
      "filter_tilt_shift",
      "filter_vintage",
      "find_in_page",
      "find_replace",
      "fingerprint",
      "first_page",
      "fitness_center",
      "flag",
      "flare",
      "flash_auto",
      "flash_off",
      "flash_on",
      "flight",
      "flight_land",
      "flight_takeoff",
      "flip",
      "flip_to_back",
      "flip_to_front",
      "folder",
      "folder_open",
      "folder_shared",
      "folder_special",
      "font_download",
      "format_align_center",
      "format_align_justify",
      "format_align_left",
      "format_align_right",
      "format_bold",
      "format_clear",
      "format_color_fill",
      "format_color_reset",
      "format_color_text",
      "format_indent_decrease",
      "format_indent_increase",
      "format_italic",
      "format_line_spacing",
      "format_list_bulleted",
      "format_list_numbered",
      "format_paint",
      "format_quote",
      "format_shapes",
      "format_size",
      "format_strikethrough",
      "format_textdirection_l_to_r",
      "format_textdirection_r_to_l",
      "format_underlined",
      "forum",
      "forward",
      "forward_10",
      "forward_30",
      "forward_5",
      "free_breakfast",
      "fullscreen",
      "fullscreen_exit",
      "functions",
      "g_translate",
      "gamepad",
      "games",
      "gavel",
      "gesture",
      "get_app",
      "gif",
      "golf_course",
      "gps_fixed",
      "gps_not_fixed",
      "gps_off",
      "grade",
      "gradient",
      "grain",
      "graphic_eq",
      "grid_off",
      "grid_on",
      "group",
      "group_add",
      "group_work",
      "hd",
      "hdr_off",
      "hdr_on",
      "hdr_strong",
      "hdr_weak",
      "headset",
      "headset_mic",
      "healing",
      "hearing",
      "help",
      "help_outline",
      "high_quality",
      "highlight",
      "highlight_off",
      "history",
      "home",
      "hot_tub",
      "hotel",
      "hourglass_empty",
      "hourglass_full",
      "http",
      "https",
      "image",
      "image_aspect_ratio",
      "import_contacts",
      "import_export",
      "important_devices",
      "inbox",
      "indeterminate_check_box",
      "info",
      "info_outline",
      "input",
      "insert_chart",
      "insert_comment",
      "insert_drive_file",
      "insert_emoticon",
      "insert_invitation",
      "insert_link",
      "insert_photo",
      "invert_colors",
      "invert_colors_off",
      "iso",
      "keyboard",
      "keyboard_arrow_down",
      "keyboard_arrow_left",
      "keyboard_arrow_right",
      "keyboard_arrow_up",
      "keyboard_backspace",
      "keyboard_capslock",
      "keyboard_hide",
      "keyboard_return",
      "keyboard_tab",
      "keyboard_voice",
      "kitchen",
      "label",
      "label_outline",
      "landscape",
      "language",
      "laptop",
      "laptop_chromebook",
      "laptop_mac",
      "laptop_windows",
      "last_page",
      "launch",
      "layers",
      "layers_clear",
      "leak_add",
      "leak_remove",
      "lens",
      "library_add",
      "library_books",
      "library_music",
      "lightbulb_outline",
      "line_style",
      "line_weight",
      "linear_scale",
      "link",
      "linked_camera",
      "list",
      "live_help",
      "live_tv",
      "local_activity",
      "local_airport",
      "local_atm",
      "local_bar",
      "local_cafe",
      "local_car_wash",
      "local_convenience_store",
      "local_dining",
      "local_drink",
      "local_florist",
      "local_gas_station",
      "local_grocery_store",
      "local_hospital",
      "local_hotel",
      "local_laundry_service",
      "local_library",
      "local_mall",
      "local_movies",
      "local_offer",
      "local_parking",
      "local_pharmacy",
      "local_phone",
      "local_pizza",
      "local_play",
      "local_post_office",
      "local_printshop",
      "local_see",
      "local_shipping",
      "local_taxi",
      "location_city",
      "location_disabled",
      "location_off",
      "location_on",
      "location_searching",
      "lock",
      "lock_open",
      "lock_outline",
      "looks",
      "looks_3",
      "looks_4",
      "looks_5",
      "looks_6",
      "looks_one",
      "looks_two",
      "loop",
      "loupe",
      "low_priority",
      "loyalty",
      "mail",
      "mail_outline",
      "map",
      "markunread",
      "markunread_mailbox",
      "memory",
      "menu",
      "merge_type",
      "message",
      "mic",
      "mic_none",
      "mic_off",
      "mms",
      "mode_comment",
      "mode_edit",
      "monetization_on",
      "money_off",
      "monochrome_photos",
      "mood",
      "mood_bad",
      "more",
      "more_horiz",
      "more_vert",
      "motorcycle",
      "mouse",
      "move_to_inbox",
      "movie",
      "movie_creation",
      "movie_filter",
      "multiline_chart",
      "music_note",
      "music_video",
      "my_location",
      "nature",
      "nature_people",
      "navigate_before",
      "navigate_next",
      "navigation",
      "near_me",
      "network_cell",
      "network_check",
      "network_locked",
      "network_wifi",
      "new_releases",
      "next_week",
      "nfc",
      "no_encryption",
      "no_sim",
      "not_interested",
      "note",
      "note_add",
      "notifications",
      "notifications_active",
      "notifications_none",
      "notifications_off",
      "notifications_paused",
      "offline_pin",
      "ondemand_video",
      "opacity",
      "open_in_browser",
      "open_in_new",
      "open_with",
      "pages",
      "pageview",
      "palette",
      "pan_tool",
      "panorama",
      "panorama_fish_eye",
      "panorama_horizontal",
      "panorama_vertical",
      "panorama_wide_angle",
      "party_mode",
      "pause",
      "pause_circle_filled",
      "pause_circle_outline",
      "payment",
      "people",
      "people_outline",
      "perm_camera_mic",
      "perm_contact_calendar",
      "perm_data_setting",
      "perm_device_information",
      "perm_identity",
      "perm_media",
      "perm_phone_msg",
      "perm_scan_wifi",
      "person",
      "person_add",
      "person_outline",
      "person_pin",
      "person_pin_circle",
      "personal_video",
      "pets",
      "phone",
      "phone_android",
      "phone_bluetooth_speaker",
      "phone_forwarded",
      "phone_in_talk",
      "phone_iphone",
      "phone_locked",
      "phone_missed",
      "phone_paused",
      "phonelink",
      "phonelink_erase",
      "phonelink_lock",
      "phonelink_off",
      "phonelink_ring",
      "phonelink_setup",
      "photo",
      "photo_album",
      "photo_camera",
      "photo_filter",
      "photo_library",
      "photo_size_select_actual",
      "photo_size_select_large",
      "photo_size_select_small",
      "picture_as_pdf",
      "picture_in_picture",
      "picture_in_picture_alt",
      "pie_chart",
      "pie_chart_outlined",
      "pin_drop",
      "place",
      "play_arrow",
      "play_circle_filled",
      "play_circle_outline",
      "play_for_work",
      "playlist_add",
      "playlist_add_check",
      "playlist_play",
      "plus_one",
      "poll",
      "polymer",
      "pool",
      "portable_wifi_off",
      "portrait",
      "power",
      "power_input",
      "power_settings_new",
      "pregnant_woman",
      "present_to_all",
      "print",
      "priority_high",
      "public",
      "publish",
      "query_builder",
      "question_answer",
      "queue",
      "queue_music",
      "queue_play_next",
      "radio",
      "radio_button_checked",
      "radio_button_unchecked",
      "rate_review",
      "receipt",
      "recent_actors",
      "record_voice_over",
      "redeem",
      "redo",
      "refresh",
      "remove",
      "remove_circle",
      "remove_circle_outline",
      "remove_from_queue",
      "remove_red_eye",
      "remove_shopping_cart",
      "reorder",
      "repeat",
      "repeat_one",
      "replay",
      "replay_10",
      "replay_30",
      "replay_5",
      "reply",
      "reply_all",
      "report",
      "report_problem",
      "restaurant",
      "restaurant_menu",
      "restore",
      "restore_page",
      "ring_volume",
      "room",
      "room_service",
      "rotate_90_degrees_ccw",
      "rotate_left",
      "rotate_right",
      "rounded_corner",
      "router",
      "rowing",
      "rss_feed",
      "rv_hookup",
      "satellite",
      "save",
      "scanner",
      "schedule",
      "school",
      "screen_lock_landscape",
      "screen_lock_portrait",
      "screen_lock_rotation",
      "screen_rotation",
      "screen_share",
      "sd_card",
      "sd_storage",
      "search",
      "security",
      "select_all",
      "send",
      "sentiment_dissatisfied",
      "sentiment_neutral",
      "sentiment_satisfied",
      "sentiment_very_dissatisfied",
      "sentiment_very_satisfied",
      "settings",
      "settings_applications",
      "settings_backup_restore",
      "settings_bluetooth",
      "settings_brightness",
      "settings_cell",
      "settings_ethernet",
      "settings_input_antenna",
      "settings_input_component",
      "settings_input_composite",
      "settings_input_hdmi",
      "settings_input_svideo",
      "settings_overscan",
      "settings_phone",
      "settings_power",
      "settings_remote",
      "settings_system_daydream",
      "settings_voice",
      "share",
      "shop",
      "shop_two",
      "shopping_basket",
      "shopping_cart",
      "short_text",
      "show_chart",
      "shuffle",
      "signal_cellular_4_bar",
      "signal_cellular_connected_no_internet_4_bar",
      "signal_cellular_no_sim",
      "signal_cellular_null",
      "signal_cellular_off",
      "signal_wifi_4_bar",
      "signal_wifi_4_bar_lock",
      "signal_wifi_off",
      "sim_card",
      "sim_card_alert",
      "skip_next",
      "skip_previous",
      "slideshow",
      "slow_motion_video",
      "smartphone",
      "smoke_free",
      "smoking_rooms",
      "sms",
      "sms_failed",
      "snooze",
      "sort",
      "sort_by_alpha",
      "spa",
      "space_bar",
      "speaker",
      "speaker_group",
      "speaker_notes",
      "speaker_notes_off",
      "speaker_phone",
      "spellcheck",
      "star",
      "star_border",
      "star_half",
      "stars",
      "stay_current_landscape",
      "stay_current_portrait",
      "stay_primary_landscape",
      "stay_primary_portrait",
      "stop",
      "stop_screen_share",
      "storage",
      "store",
      "store_mall_directory",
      "straighten",
      "streetview",
      "strikethrough_s",
      "style",
      "subdirectory_arrow_left",
      "subdirectory_arrow_right",
      "subject",
      "subscriptions",
      "subtitles",
      "subway",
      "supervisor_account",
      "surround_sound",
      "swap_calls",
      "swap_horiz",
      "swap_vert",
      "swap_vertical_circle",
      "switch_camera",
      "switch_video",
      "sync",
      "sync_disabled",
      "sync_problem",
      "system_update",
      "system_update_alt",
      "tab",
      "tab_unselected",
      "tablet",
      "tablet_android",
      "tablet_mac",
      "tag_faces",
      "tap_and_play",
      "terrain",
      "text_fields",
      "text_format",
      "textsms",
      "texture",
      "theaters",
      "thumb_down",
      "thumb_up",
      "thumbs_up_down",
      "time_to_leave",
      "timelapse",
      "timeline",
      "timer",
      "timer_10",
      "timer_3",
      "timer_off",
      "title",
      "toc",
      "today",
      "toll",
      "tonality",
      "touch_app",
      "toys",
      "track_changes",
      "traffic",
      "train",
      "tram",
      "transfer_within_a_station",
      "transform",
      "translate",
      "trending_down",
      "trending_flat",
      "trending_up",
      "tune",
      "turned_in",
      "turned_in_not",
      "tv",
      "unarchive",
      "undo",
      "unfold_less",
      "unfold_more",
      "update",
      "usb",
      "verified_user",
      "vertical_align_bottom",
      "vertical_align_center",
      "vertical_align_top",
      "vibration",
      "video_call",
      "video_label",
      "video_library",
      "videocam",
      "videocam_off",
      "videogame_asset",
      "view_agenda",
      "view_array",
      "view_carousel",
      "view_column",
      "view_comfy",
      "view_compact",
      "view_day",
      "view_headline",
      "view_list",
      "view_module",
      "view_quilt",
      "view_stream",
      "view_week",
      "vignette",
      "visibility",
      "visibility_off",
      "voice_chat",
      "voicemail",
      "volume_down",
      "volume_mute",
      "volume_off",
      "volume_up",
      "vpn_key",
      "vpn_lock",
      "wallpaper",
      "warning",
      "watch",
      "watch_later",
      "wb_auto",
      "wb_cloudy",
      "wb_incandescent",
      "wb_iridescent",
      "wb_sunny",
      "wc",
      "web",
      "web_asset",
      "weekend",
      "whatshot",
      "widgets",
      "wifi",
      "wifi_lock",
      "wifi_tethering",
      "work",
      "wrap_text",
      "youtube_searched_for",
      "zoom_in",
      "zoom_out",
      "zoom_out_map"
    ]

    vm.getInsensitiveProperty = function (elem,label) {
      if (elem == null || typeof elem == 'undefined' || label == null || typeof label == 'undefined' ) {
        return undefined;
      }
      if (label in elem) {
        return elem[label];
      } else if (label.toUpperCase() in elem) {
        return elem[label.toUpperCase()]
      } else if (label.toLowerCase() in elem) {
        return elem[label.toLowerCase()]
      } else {
        return undefined;
      }
    
    }

    vm.cleanHTMLJSComments = function (libs) {
      return libs.slice().replace(/<!--(?!>)[\S\s]*?-->/g, '').replace(/(\r\n|\n|\r| )/gm, "");
    }

    vm.isLibsinHLibs = function (libs, hlibs) {
      return vm.cleanHTMLJSComments(hlibs).indexOf(vm.cleanHTMLJSComments(libs)) != -1;
    }
  };
})();
