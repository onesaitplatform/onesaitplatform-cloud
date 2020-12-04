var ApiCustomOpsController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Menu Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.
	var currentFormat = '' // date format depends on currentLanguage.
	var internalFormat = 'yyyy/mm/dd';
	var internalLanguage = 'en';
	
	var name_op_edit_customsql=''
	
	// CONTROLLER PRIVATE FUNCTIONS	
	
    function selectEditOp(field) {
		name_op_edit_customsql = field;
		loadCustomSql (name_op_edit_customsql);
    }

    function viewEditOp(field) {
		name_op_edit_customsql = field;
		viewCustomSql (name_op_edit_customsql);
	}
	
    function validateNameOp(field) {
        var RegExPattern = /^[a-zA-Z0-9._-]*$/;
        if ((field.value.match(RegExPattern))) {

        } else {
        	ApiCreateController.showErrorDialog('Error', apiCustomOpsReg.apimanager_customsql_error_name_format);
        }
    }
    
    function loadParamQuerySQLType() {
   	 var query = codeEditor.getValue();
   	 monacoFormat($('#id_customsql_querytype').val(), $("#ontology option:selected").text());
    }
    
    function monacoFormat (queryType, defaultOntology=null) {
    	if (codeEditor){
    		switch (queryType){
    		case 'sql':
    			monaco.editor.setModelLanguage(codeEditor.getModel(), 'sql');
    			if (defaultOntology) {
        			codeEditor.setValue("Select * from "+defaultOntology+" as c limit 3");
    			}
    			break;
    		case 'native':
    			monaco.editor.setModelLanguage(codeEditor.getModel(), 'javascript');
    			if (defaultOntology) {
    				codeEditor.setValue("db."+defaultOntology+".find().limit(3)");
    			}
    			break;
    		default:
    			break;
    		}
    	}
    }

    function loadParamsFromQuery(field, op_name) {
        clearParams();
        if (op_name==null || op_name==""){
            var error = "";
            if (field != null && field !=""){
                error = isValidQuery(field);
            }
            if (error==""){
            	showParams(field);
            } else {
            	ApiCreateController.showErrorDialog('Error', error);
            }
        } else {
        	showParams(field);
        }
    }
	var rtdb;
	function getRtdbFromOntology(){
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		$.ajax({ url: "/controlpanel/querytool/rtdb/" +  $("#ontology option:selected").text(),  
            headers: {
				[csrf_header]: csrf_value
		    },
			type: 'GET', 
			contentType: 'text/html',
			async : false,
			success: function (result) {	
				rtdb=result;
			},
			error: function(error){
				//alert(error);
			}
		});
	}
    
    function isValidQuery(field){
    	getRtdbFromOntology();
    	if (field != null && field !=""){
    		if(rtdb=="Virtual"){
    			$('#id_customsql_querytype').val("native");
    			$('#id_customsql_querytype').attr("disabled", "disabled");
    			return "";
    		} else if (rtdb=="ELASTIC_SEARCH"){
    			return "";
    		} else {
    			$('#id_customsql_querytype').removeAttr("disabled");
    			if (((field.toUpperCase().indexOf("SELECT")>=0)&&($('#id_customsql_querytype').val()=="sql"))|| 
    					((field.toUpperCase().indexOf("DELETE")>=0)&&($('#id_customsql_querytype').val()=="sql")) ||
    					((field.toUpperCase().indexOf("UPDATE")>=0)&&($('#id_customsql_querytype').val()=="sql")) ||
    	    			((field.toUpperCase().indexOf("DB.")>=0)&&($('#id_customsql_querytype').val()=="native"))){
    	    			if ($("#ontology option:selected").text()!= "" && field.indexOf($("#ontology option:selected").text())>=0){
    	    				if (((field.split("{$").length - 1)==(field.split("}").length - 1) || ($('#id_customsql_querytype').val()=="native"))){
    	    					return "";
    	    				} else {
    	    					return (apiCustomOpsReg.apimanager_customsql_error_query_params);
    	    				}
    	    			} else {
    	    				return (apiCustomOpsReg.apimanager_customsql_error_query_ontology);
    	    			}
    	    		} else {
    	    			return (apiCustomOpsReg.apimanager_customsql_error_query);
    	    		}
    		}
    		
    	} else {
    		return (apiCustomOpsReg.apimanager_customsql_error_required);
    	}
    }

    function clearParams() {
    	$("#customsql_paramsquery").html("");
    	$("#customsql_params_div").css({ 'display': "none" });
    	$("#customsql_noparam_div").css({ 'display': "block" });
    }

    function showParams(query) {
		 var param = "";
		 customsql_queryparam = new Array();
		 while (query.indexOf("{$")>0 && query.indexOf("}")!=-1){
			 var param = query.substring(query.indexOf("{$") + 2, query.indexOf("}", query.indexOf("{$")));
		
			 if (param.indexOf(":")==-1){
				 if (!isParameterAdded(customsql_queryparam, param)){
				 	loadParamQuery(param);
			 	 }
				 query = query.substring(query.indexOf("}", query.indexOf("{$")) + 1);
			 } else {
			    query = query.substring(query.indexOf("{$") + 2, query.length);
			 }
		 }
		 
		 if (customsql_queryparam.length>0){
			  
			 $("#alertExecuteQuery").removeClass('hide');
			$("#executeQuery").attr("disabled", true);
			$("#customsql_noparam_div").css({ 'display': "none" });
			$("#customsql_params_div").css({ 'display': "block" });
		 } else {
			$("#executeQuery").attr("disabled", false);
			$("#alertExecuteQuery").addClass('hide');
		 	$("#customsql_params_div").css({ 'display': "none" });
		 	$("#customsql_noparam_div").css({ 'display': "block" });
		 }
    }
    
    function isParameterAdded(customsql_queryparam, param) {
        for(var i=0; i<customsql_queryparam.length; i+=1){
            var parameter = customsql_queryparam [i];
            if (parameter.name == param){
                return true;
            }
        }
        return false;  	
    }

    function loadParamQuery(param) {
        var customsqlParamaDiv=document.getElementById("customsql_paramsquery");

        var newCustomsqlParamDiv = document.createElement('div');
        newCustomsqlParamDiv.id= "customsql_param_" + param;

        var newCustomsqlParamFieldSet = document.createElement('fieldset');
        newCustomsqlParamFieldSet.id = "customsql_param_fieldset" + param;

        newCustomsqlParamFieldSet.style.margin="10px";
        newCustomsqlParamFieldSet.style.marginTop="10px";
        newCustomsqlParamFieldSet.style.padding="10px";
        newCustomsqlParamFieldSet.style.border="1px #d0d2d9 dotted";
        newCustomsqlParamFieldSet.style.display="inline";

        var newLabelCustomsqlParam = document.createElement('label');
        newLabelCustomsqlParam.id = param;
        newLabelCustomsqlParam.className="description";
        newLabelCustomsqlParam.style.marginRight="20px";
        newLabelCustomsqlParam.innerHTML=param;

        newCustomsqlParamFieldSet.appendChild(newLabelCustomsqlParam);

        var newInputCustomsqlParam = document.createElement('select');
        newInputCustomsqlParam.name="customsqlParamType_" + param;
        newInputCustomsqlParam.style.cssFloat="right";

        var optionObject = document.createElement( 'option' );
        optionObject.value = "OBJECT"; 
        optionObject.text = "OBJECT";
        newInputCustomsqlParam.add(optionObject);
        var optionString = document.createElement( 'option' );
        optionString.value = "STRING"; 
        optionString.text = "STRING";
        newInputCustomsqlParam.add(optionString);
        var optionNumber = document.createElement( 'option' );
        optionNumber.value = "NUMBER"; 
        optionNumber.text = "NUMBER";
        newInputCustomsqlParam.add(optionNumber);
        var optionDate = document.createElement( 'option' );
        optionDate.value = "DATE"; 
        optionDate.text = "DATE";
        newInputCustomsqlParam.add(optionDate);
        newCustomsqlParamFieldSet.appendChild(newInputCustomsqlParam);

        customsqlParamaDiv.appendChild(newCustomsqlParamFieldSet);

        var parameter = {name: param, condition: "REQUIRED", dataType: $('[name="customsqlParamType_' + param+"\"").val(), description: ""};
        customsql_queryparam.push(parameter);
    }

    function saveCustomsqlOperation(){
    	
        var id_type_op_customsql = $('#id_type_op_customsql').val();
        var id_name_op_customsql = $('#id_name_op_customsql').val();
        var errorQuery = isValidQuery(codeEditor.getValue());
        var postProcess = myCodeMirrorJs.getValue();

        var desc_op_customsql = $('#id_desc_op_customsql').val();
        if (id_type_op_customsql!=null && id_type_op_customsql!="" && id_name_op_customsql!=null && id_name_op_customsql!="" && desc_op_customsql!=null && desc_op_customsql!=""){
       	 if (errorQuery!=null && errorQuery==""){
	             if (name_op_edit_customsql==null || name_op_edit_customsql==""){
	                 if (!ApiCreateController.existOperation(id_name_op_customsql)){
	                     var querystrings = new Array();
	                     var headers = new Array();
	                     var operation = {identification: id_name_op_customsql, description: desc_op_customsql , operation: id_type_op_customsql, path: "", postprocess: postProcess, querystrings: querystrings, headers: headers};

	                     saveParamQueryCustomsql(operation);

	                     addOperationCustomsql(operation);

	                     operations.push(operation);

	                     $('#dialog-customsql').modal('toggle');
	                 } else {
	                	 ApiCreateController.showErrorDialog('Error', apiCustomOpsReg.apimanager_customsql_error_operation_exists);
	                 }
	             } else {
	                 for(var i=0; i<operations.length; i+=1){
	                     if (operations [i].identification == name_op_edit_customsql){
	                    	 operations [i].description=desc_op_customsql;
	                    	 operations [i].operation=id_type_op_customsql;
	                    	 operations [i].postprocess=postProcess;

	                    	 operations [i].querystrings = new Array();

	                         saveParamQueryCustomsql(operations [i]);
	                         break;
	                     }
	                 }
	                 updateCustomSqlOperation(operations[i]);
	                 $('#dialog-customsql').modal('toggle');
	             }
	         } else {
	        	 ApiCreateController.showErrorDialog('Error', errorQuery);
	         }
        } else {
        	ApiCreateController.showErrorDialog('Error', apiCustomOpsReg.apimanager_customsql_error_fields);
        }
    }

    function saveParamQueryCustomsql(operation){
   	 	var queryParameter = {name: "query", condition: "CONSTANT", dataType: "STRING", value: codeEditor.getValue(), description: "", headerType: "QUERY"};
   	 	operation.querystrings.push(queryParameter);
        var targetBDParameter = {name: "targetdb", condition: "CONSTANT", dataType: "STRING", value: $('#id_customsql_targetBD').val() , description: "", headerType: "QUERY"};
        operation.querystrings.push(targetBDParameter);
        var querytypeBDParameter = {name: "queryType", condition: "CONSTANT", dataType: "STRING", value: $('#id_customsql_querytype').val() , description: "", headerType: "QUERY"};
        operation.querystrings.push(querytypeBDParameter);
        var path =  operation.identification;
//        if (customsql_queryparam.length>0){
//       	 	path=path + "?";
//        }
        for (var i = 0; i < customsql_queryparam.length; i++) {
	       	customsql_queryparam [i].dataType = $('[name="customsqlParamType_' + customsql_queryparam [i].name +"\"").val();
	       	customsql_queryparam [i].headerType = "PATH";
	       	operation.querystrings.push(customsql_queryparam [i]);
	       	path = path + "/{" + customsql_queryparam [i].name +"}";
//	       	if (i < customsql_queryparam.length-1){
//	       		path = path + "";
//	       	}
        }
        operation.path = path;
    }


    function addOperationCustomsql(operation){
   	 var customsqlOpsDiv=document.getElementById("divCUSTOMSQLS");

        var newCustomsqlParamDiv = document.createElement('div');
        newCustomsqlParamDiv.id= operation.identification;
        newCustomsqlParamDiv.className= "op_div_selected";

        var newInputCustomsqlOperationDiv = document.createElement('div');
        newInputCustomsqlOperationDiv.className= "op_button_div";
        
		
		// div description get all the data inside
		var OperationDivDesc = document.createElement('div');
		OperationDivDesc.className = "op_desc_div";
		
		
        var newInputCustomsqlOperation = document.createElement('input');
			newInputCustomsqlOperation.id=operation.identification + "_OPERATION";
			newInputCustomsqlOperation.className="op_button_selected";        
			newInputCustomsqlOperation.type="reset";
			newInputCustomsqlOperation.value=apiCustomOpsReg.apimanager_customBtn;
			newInputCustomsqlOperation.name="CUSTOM_SQL";
			newInputCustomsqlOperation.disabled="disabled";
			newInputCustomsqlOperationDiv.appendChild(newInputCustomsqlOperation);
			newCustomsqlParamDiv.appendChild(newInputCustomsqlOperationDiv);
		
		// CONTENTS, ALL INSIDE DESC , THEN INSIDE customsqlOpsDiv
        var newLabelCustomsqlOperation = document.createElement('label');
			newLabelCustomsqlOperation.id=operation.identification + "_LABEL";
			newLabelCustomsqlOperation.className="description bold";        
			newLabelCustomsqlOperation.style = "font-size: 14px; color: rgb(34, 48, 77); padding-right:15px; min-width: 200px; display: inline-block";
			newLabelCustomsqlOperation.innerHTML=operation.identification;

        //newCustomsqlParamDiv.appendChild(newLabelCustomsqlOperation);
		OperationDivDesc.appendChild(newLabelCustomsqlOperation);
		

        var newInputEditCustomsqlOperation = document.createElement('input');
			newInputEditCustomsqlOperation.id=operation.identification + "_Edit";
			newInputEditCustomsqlOperation.className="btn btn-sm blue-hoki";
			newInputEditCustomsqlOperation.style = "float: right; position: relative;top: -40px";
			newInputEditCustomsqlOperation.type="button";      
			newInputEditCustomsqlOperation.value=apiCustomOpsReg.apimanager_editBtn;
			newInputEditCustomsqlOperation.name=operation.identification + "_Edit";
			newInputEditCustomsqlOperation.onclick = function() {
				ApiCustomOpsController.selectEditCustomOp(operation.identification);
			};
			//newCustomsqlParamDiv.appendChild(newInputEditCustomsqlOperation);
			OperationDivDesc.appendChild(newInputEditCustomsqlOperation);
				

        var newInputEliminarCustomsqlOperation = document.createElement('input');
			newInputEliminarCustomsqlOperation.id=operation.identification + "_Eliminar";
			newInputEliminarCustomsqlOperation.className="btn btn-sm red-sunglo";
			newInputEliminarCustomsqlOperation.style = "float: right;  margin-right: 4px;position: relative;top: -40px";
			newInputEliminarCustomsqlOperation.type="button";			
			newInputEliminarCustomsqlOperation.value=apiCustomOpsReg.apimanager_deleteBtn;
			newInputEliminarCustomsqlOperation.name=operation.identification + "_Eliminar";
			newInputEliminarCustomsqlOperation.onclick = function() {
				ApiCustomOpsController.removeCustomSqlOp(operation.identification);
			};
			//newCustomsqlParamDiv.appendChild(newInputEliminarCustomsqlOperation);
			OperationDivDesc.appendChild(newInputEliminarCustomsqlOperation);
		
       
        var newInputPathOperationCustomsql = document.createElement('span');
			newInputPathOperationCustomsql.id=operation.identification + "_PATH";			
			newInputPathOperationCustomsql.style = "padding-right:15px; min-width: 250px;display: inline-block";		
			newInputPathOperationCustomsql.innerHTML= '<span class="label label-success"><small>ENDPOINT</small></span> <span class="bold">' + operation.path + '</span>';
			newInputPathOperationCustomsql.name=operation.path + "_PATH";
        
		//newCustomsqlParamDiv.appendChild(newInputPathOperationCustomsql);
		OperationDivDesc.appendChild(newInputPathOperationCustomsql);
		
       
        for (var i = 0; i < operation.querystrings.length; i++) {
            if (operation.querystrings[i].name=="query"){
                var newInputQueryOperationCustomsql = document.createElement('span');
                newInputQueryOperationCustomsql.id=operation.identification + "_QUERY";
                newInputQueryOperationCustomsql.style = "padding-right: 30px; min-width: 150px; display: inline-block";
                newInputQueryOperationCustomsql.innerHTML='<span class="label label-info "><small>QUERY</small></span> <span class="bold">' + operation.querystrings[i].value + "</span>";
                newInputQueryOperationCustomsql.name=operation.identification + "_QUERY";

                //newCustomsqlParamDiv.appendChild(newInputQueryOperationCustomsql);
				OperationDivDesc.appendChild(newInputQueryOperationCustomsql);
				
            }
        }

        var newInputDescOperationCustomsql = document.createElement('span');
        newInputDescOperationCustomsql.id=operation.identification + "_DESC";       
        newInputDescOperationCustomsql.style = "padding-left: 20px; display: inline-block";		
        newInputDescOperationCustomsql.innerHTML = '<span class="label label-info "><small>DESC.</small></span> <span class="text-truncate-lg">' +operation.description + '</span>';
        newInputDescOperationCustomsql.name=operation.identification + "_DESC";

        //newCustomsqlParamDiv.appendChild(newInputDescOperationCustomsql);
		OperationDivDesc.appendChild(newInputDescOperationCustomsql);
		newCustomsqlParamDiv.appendChild(OperationDivDesc);

        customsqlOpsDiv.appendChild(newCustomsqlParamDiv);

        document.getElementById("divCUSTOMSQLS").style.display="block";
    }
    function loadParamsCustomValues(querystrings){
    	var labels = $('#customsql_paramsquery fieldset label');
    	for(var i=0; i<labels.length; i++){
    		var label = labels.get(i);
    		var param = label.attributes.id.value;
    		
    		for(var j=0; j< querystrings.length; j++){
    			var name = querystrings[j].name;
    			if(name == param){
    				
    				var id = $('[name= "customsqlParamType_'+param+"\"");
    				id.val(querystrings[j].dataType);
    				
    			}
    			
    		}
    		
    		
    		
    		
    	}
    }

    

    function viewCustomSql (op_name){
        if (op_name!=null && op_name!=""){
            var operation;
            for(var i=0; i<operations.length; i+=1){
                var op = operations [i];
                if (op.identification == op_name){
                	operation=op;
                }
            }
            $('#id_name_op_customsql').val(operation.identification);
            $('#id_desc_op_customsql').val(operation.description);
            
            for (var i = 0; i < operation.querystrings.length; i++) {
                if (operation.querystrings [i].name == "query" ){
                	codeEditor.setValue(operation.querystrings [i].value);
                	loadParamsFromQuery(operation.querystrings [i].value, op_name);
                }
            }

            loadParamsQueryValues(operation.querystrings);
            
            if (operation.postprocess!=null && operation.postprocess!=""){
            	//myCodeMirrorJs.setValue(operation.postprocess);
            	
            	
            	$('#jsImportTextArea').val(operation.postprocess);	
            	$('#postProcessCheckbox').prop('checked', true);
            	$('#portletBody').css('display') == "none" ? $('#portletToolPostProcess').click():null;
            	$('#id_postprocess_op_customsql').show();
            } else {
            	$('#id_postprocess_op_customsql').hide();
            	$('#portletBody').css('display') == "block" ? $('#portletToolPostProcess').click():null;
            }
            
            $('#id_name_op_customsql').prop('disabled', true);
            loadParamsCustomValues(operation.querystrings);
        } else {
        	$('#id_name_op_customsql').val("");
        	codeEditor.setValue("");
        	$('#id_desc_op_customsql').val("");
        			
        	loadParamsFromQuery("", "");
        	
        	myCodeMirrorJs.setValue("");
        	$('#jsImportTextArea').val("");	
    		$('#postProcessCheckbox').prop('checked', false);
    		$('#portletBody').css('display') == "block" ? $('#portletToolPostProcess').click():null;
            $('#id_name_op_customsql').prop('disabled', false);

        }
        
        $('#dialog-customsql').modal('toggle');
      //  setTimeout(function() {
      //  	myCodeMirrorJs.refresh();
      //},600);

    }   
    
    function loadCustomSql (op_name){
        if (op_name!=null && op_name!=""){
            var operation;
            for(var i=0; i<operations.length; i+=1){
                var op = operations [i];
                if (op.identification == op_name){
                	operation=op;
                }
            }
            $('#id_name_op_customsql').val(operation.identification);
            $('#id_desc_op_customsql').val(operation.description);

            for (var i = 0; i < operation.querystrings.length; i++) {
                if (operation.querystrings [i].name == "query" ){
                	codeEditor.setValue(operation.querystrings [i].value);
                	loadParamsFromQuery(operation.querystrings [i].value, op_name);
                }
            }

            loadParamsQueryValues(operation.querystrings);
            
            if (operation.postprocess!=null && operation.postprocess!=""){
            	myCodeMirrorJs.setValue(operation.postprocess);
            	$('#postProcessCheckbox').prop('checked', true);
            	$('#portletBody').css('display') == "none" ? $('#portletToolPostProcess').click():null;
            	$('#id_postprocess_op_customsql').show();
            } else {
            	$('#id_postprocess_op_customsql').hide();
            	$('#portletBody').css('display') == "block" ? $('#portletToolPostProcess').click():null;
            }
            
            $('#id_name_op_customsql').prop('disabled', true);
            loadParamsCustomValues(operation.querystrings);
        } else {
        	$('#id_name_op_customsql').val("");
        	$('#id_desc_op_customsql').val("");
        			
        	loadParamsFromQuery("", "");
        	monacoFormat('sql', $("#ontology option:selected").text());	
        	
        	myCodeMirrorJs.setValue("");
    		$('#postProcessCheckbox').prop('checked', false);
    		$('#portletBody').css('display') == "block" ? $('#portletToolPostProcess').click():null;
    		

            $('#id_name_op_customsql').prop('disabled', false);

        }
        
        $('#dialog-customsql').modal('toggle');
        setTimeout(function() {
        	myCodeMirrorJs.refresh();
    	},600);

    }


    function loadParamsQueryValues(querystrings){
        for (var i = 0; i < querystrings.length; i++) {
	       	 if (querystrings [i].name == "query" ){
            } else if (querystrings [i].name == "targetdb" ){
            	$('#id_customsql_targetBD').val(querystrings [i].value);
       	 	} else if (querystrings [i].name == "formatResult" ){
       		 	$('#"id_customsql_formatresult').val(querystrings [i].value);
       	 	} else if (querystrings [i].name == "queryType" ){
       		 	$('#id_customsql_querytype').val(querystrings [i].value);
       		 	monacoFormat(querystrings [i].value);
       	 	} else {
       		 $('[name = "customsqlParamType_' + querystrings [i].name +"\"").val(querystrings [i].value);
        	}
         }
    }

    function updateCustomSqlOperation(operation){
    	$('#' + operation.identification + "_PATH").html("<b>" + operation.path+ "</b>");

        for (var i = 0; i < operation.querystrings.length; i++) {
            if (operation.querystrings [i].name == "query" ){
            	$('#' + operation.identification + '_QUERY').html(operation.querystrings [i].value);
            }
        }

        $('#' + operation.identification + "_DESC").html(operation.description);
    }

    function removeCustomOp(op_name){
        for(var i=0; i<operations.length; i+=1){
            var operation = operations [i];
            if (operation.identification == op_name){
            	operations.splice(i, 1);
            }
        }
        var operationsCustomSqlDiv=document.getElementById("divCUSTOMSQLS");
        var operationCustomSqlRemoveDiv = document.getElementById(op_name);
        operationsCustomSqlDiv.removeChild(operationCustomSqlRemoveDiv);

    }  
    
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return apiCustomOpsReg = Data;
		},

		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';	
			
		},
		
		// SELECTS EDIT OPERATION
		selectEditCustomOp: function(field) {
			logControl ? console.log(LIB_TITLE + ': selectEditCustomOp(field)') : '';
			selectEditOp(field);
		},
		// SELECTS EDIT OPERATION
		viewEditCustomOp: function(field) {
			logControl ? console.log(LIB_TITLE + ': viewEditCustomOp(field)') : '';
			viewEditOp(field);
		},
		// REMOVES EDIT OPERATION
		removeCustomSqlOp: function(field) {
			logControl ? console.log(LIB_TITLE + ': removeCustomOp(field)') : '';
			removeCustomOp(field);
		},
		
		// VALIDATE OP NAME
		validateName: function(field) {
			logControl ? console.log(LIB_TITLE + ': validateNameOp()') : '';
			validateNameOp(field);
		},
		
		// EXTRACT PARAMS
		loadParamsQuery: function(field, op_name){
			logControl ? console.log(LIB_TITLE + ': loadParamsFromQuery()') : '';
			loadParamsFromQuery(field, op_name);
		},
		
		// EXTRACT PARAMS
		loadParamsQueryType: function(){
			logControl ? console.log(LIB_TITLE + ': loadParamQuerySQLType()') : '';
			loadParamQuerySQLType();
		},		
		
		// SAVE CHANGES
		saveCustom: function(){
			logControl ? console.log(LIB_TITLE + ': saveCustom()') : '';
			saveCustomsqlOperation();
		}
	};
}();
// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	ApiCustomOpsController.load(apiCustomOpsJson);
	// AUTO INIT CONTROLLER.
	ApiCustomOpsController.init();
});	

