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
	   	 clearParams();
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
    		} else if (rtdb=="NEBULA_GRAPH"){
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

        var divLabel1 = document.createElement('div');
        divLabel1.className = "col-md-12";
        
        var newLabelCustomsqlParam = document.createElement('label');
        newLabelCustomsqlParam.id = param;
        newLabelCustomsqlParam.className="description";
        newLabelCustomsqlParam.style.marginRight="20px";
        newLabelCustomsqlParam.style.fontSize="1em";
        newLabelCustomsqlParam.innerHTML=param;
        divLabel1.appendChild(newLabelCustomsqlParam)
        
        newCustomsqlParamFieldSet.appendChild(divLabel1);
        
        var divLabel2 = document.createElement('div');
        divLabel2.className = "col-md-6";
        
        var labelType = document.createElement('label');
        labelType.className="description";
        labelType.style.marginRight="2px";
        labelType.id="location"+param;
        labelType.innerHTML="type";
        divLabel2.appendChild(labelType)

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
        divLabel2.appendChild(newInputCustomsqlParam);
        newCustomsqlParamFieldSet.appendChild(divLabel2);
        
        
        var divLabel3 = document.createElement('div');
        divLabel3.className = "col-md-6";
        
        var labelType2 = document.createElement('label');
        labelType2.className="description";
        labelType2.style.marginRight="2px";
        labelType2.id="location"+param;
        labelType2.innerHTML="location";
        divLabel3.appendChild(labelType2)
       
        //BODY, FORMDATA, HEADER, PATH, QUERY;
        var newInputCustomsqlParam2 = document.createElement('select');
        newInputCustomsqlParam2.name="customsqlParamType_2" + param;
        newInputCustomsqlParam2.style.cssFloat="right";

        var optionBody = document.createElement( 'option' );
        optionBody.value = "BODY"; 
        optionBody.text = "BODY";
        newInputCustomsqlParam2.add(optionBody);
        
        var optionFormData = document.createElement( 'option' );
        optionFormData.value = "FORMDATA"; 
        optionFormData.text = "FORMDATA";
        newInputCustomsqlParam2.add(optionFormData);
        var optionHeader = document.createElement( 'option' );
        optionHeader.value = "HEADER"; 
        optionHeader.text = "HEADER";
        newInputCustomsqlParam2.add(optionHeader);
        var optionPath = document.createElement( 'option' );
        optionPath.value = "PATH"; 
        optionPath.text = "PATH";
        newInputCustomsqlParam2.add(optionPath);
        var optionQuery = document.createElement( 'option' );
        optionQuery.value = "QUERY"; 
        optionQuery.text = "QUERY";
        newInputCustomsqlParam2.add(optionQuery);
        
        divLabel3.appendChild(newInputCustomsqlParam2);
        newCustomsqlParamFieldSet.appendChild(divLabel3);
	  //newCustomsqlParamFieldSet.appendChild(newInputCustomsqlParam);
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
	                 if (!ApiCreateController.existOperation(id_name_op_customsql, id_type_op_customsql)){
	                     var querystrings = new Array();
	                     var headers = new Array();
	                     var operation = {identification: id_name_op_customsql, description: desc_op_customsql , operation: id_type_op_customsql, path: "", postprocess: postProcess, querystrings: querystrings, headers: headers};

	                     saveParamQueryCustomsql(operation);

	                     addOperationCustomsql(operation);

	                     operations.push(operation);

	                     $('#dialog-customsql').modal('toggle');
	                     
	                     toastr.success(messagesForms.operations.genOpSuccess,'');
	                 } else {
	                	 toastr.error(messagesForms.operations.genOpError,apiCustomOpsReg.apimanager_customsql_error_operation_exists);
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
	                 
	                 toastr.success(messagesForms.operations.genOpSuccess,'');
	             }
	         } else {
	        	 toastr.error(messagesForms.operations.genOpError,errorQuery);
	         }
        } else {
        	toastr.error(messagesForms.operations.genOpError,apiCustomOpsReg.apimanager_customsql_error_fields);
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
	       	customsql_queryparam [i].headerType = $('[name="customsqlParamType_2' + customsql_queryparam [i].name +"\"").val();
	       	operation.querystrings.push(customsql_queryparam [i]);
	       	if(customsql_queryparam [i].headerType === 'PATH'){
	       		path = path + "/{" + customsql_queryparam [i].name +"}";
	       	}
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
        newInputCustomsqlOperationDiv.className= "op_button_div row main";
	
		var newOpHeaderCol1Div = document.createElement('div');
			newOpHeaderCol1Div.className= "col-md-7";
		
	        var newInputCustomsqlOperation = document.createElement('input');
				newInputCustomsqlOperation.id=operation.identification + "_OPERATION";
				newInputCustomsqlOperation.className="op_button_selected";        
				newInputCustomsqlOperation.type="reset";
				newInputCustomsqlOperation.value=operation.operation;
				newInputCustomsqlOperation.name="CUSTOM_SQL";
				newInputCustomsqlOperation.disabled="disabled";
				if(operation.operation === 'PUT'){
					newInputCustomsqlOperation.style = "background-color: #fca130;"
				}else if(operation.operation === 'POST'){
					newInputCustomsqlOperation.style = "background-color: #49cc90;"
				}else if(operation.operation === 'DELETE'){
					newInputCustomsqlOperation.style = "background-color: #f93e3e;"
				}else if(operation.operation === 'GET'){
					newInputCustomsqlOperation.style = "background-color: #61affe;"
				}
				newOpHeaderCol1Div.appendChild(newInputCustomsqlOperation);
				
			var newLabelCustomsqlOperation = document.createElement('label');
				newLabelCustomsqlOperation.id=operation.identification + "_LABEL";
				newLabelCustomsqlOperation.className="description";        
				newLabelCustomsqlOperation.style = "padding-right:15px; display: inline-block; font-weight:bold";
				newLabelCustomsqlOperation.innerHTML=operation.identification;
				newOpHeaderCol1Div.appendChild(newLabelCustomsqlOperation);				

				
			newInputCustomsqlOperationDiv.appendChild(newOpHeaderCol1Div);
			
		var newOpHeaderCol2Div = document.createElement('div');
			newOpHeaderCol2Div.className= "col-md-5";
			
			var newOpHeaderCol2DivButtons= document.createElement('div');
			newOpHeaderCol2DivButtons.className= "pull-right";
			
		        var newInputEliminarCustomsqlOperation = document.createElement('i');
					newInputEliminarCustomsqlOperation.id=operation.identification + "_Eliminar";
					newInputEliminarCustomsqlOperation.className="icon-delete";
					newInputEliminarCustomsqlOperation.style = "margin-right: 10px;color:#A73535;";
					newInputEliminarCustomsqlOperation.type="button";
					newInputEliminarCustomsqlOperation.value=apiCustomOpsReg.apimanager_deleteBtn;
					newInputEliminarCustomsqlOperation.name=operation.identification + "_Eliminar";
					newInputEliminarCustomsqlOperation.onclick = function() {
						ApiCustomOpsController.removeCustomSqlOp(operation.identification);
					};
				newOpHeaderCol2DivButtons.appendChild(newInputEliminarCustomsqlOperation);
				
				var newInputEditCustomsqlOperation = document.createElement('i');
					newInputEditCustomsqlOperation.id=operation.identification + "_Edit";
					newInputEditCustomsqlOperation.className="icon-edit";
					newInputEditCustomsqlOperation.style = "color:#1168A6";
					newInputEditCustomsqlOperation.type="button";      
					newInputEditCustomsqlOperation.value=apiCustomOpsReg.apimanager_editBtn;
					newInputEditCustomsqlOperation.name=operation.identification + "_Edit";
					newInputEditCustomsqlOperation.onclick = function() {
						ApiCustomOpsController.selectEditCustomOp(operation.identification);
					};
				newOpHeaderCol2DivButtons.appendChild(newInputEditCustomsqlOperation);
	
			newOpHeaderCol2Div.appendChild(newOpHeaderCol2DivButtons);
					
			newInputCustomsqlOperationDiv.appendChild(newOpHeaderCol2Div);
		
		newCustomsqlParamDiv.appendChild(newInputCustomsqlOperationDiv);

		// CONTENTS, ALL INSIDE DESC , THEN INSIDE customsqlOpsDiv

		// div description get all the data inside
		var OperationDivEndpoint = document.createElement('div');
		OperationDivEndpoint.className = "op_desc_div row main";		

		var endpointCol1Div = document.createElement('div');
			endpointCol1Div.className= "col-md-3";
	
	        var newInputPathOperationCustomsql = document.createElement('input');
			newInputPathOperationCustomsql.style = "text-align: center;";
			newInputPathOperationCustomsql.className="op_desc_label_tittle"
			newInputPathOperationCustomsql.disabled="disabled"
			newInputPathOperationCustomsql.placeholder="endpoint"
			endpointCol1Div.appendChild(newInputPathOperationCustomsql);

		OperationDivEndpoint.appendChild(endpointCol1Div);
		var endpointCol2Div = document.createElement('div');
			endpointCol2Div.className= "col-md-7";			
			
	        var newLabelPathOperationCustomsql = document.createElement('label');
			newLabelPathOperationCustomsql.id=operation.identification + "_PATH";			
			newLabelPathOperationCustomsql.className="op_desc_label"
			newLabelPathOperationCustomsql.textContent = operation.path;
			newLabelPathOperationCustomsql.name=operation.path + "_PATH";
			endpointCol2Div.appendChild(newLabelPathOperationCustomsql);
			
		OperationDivEndpoint.appendChild(endpointCol2Div);
		
		// div description get all the data inside
		var OperationDivDesc = document.createElement('div');
		OperationDivDesc.className = "row main";
		
		var descriptionCol1Div = document.createElement('div');
			descriptionCol1Div.className= "col-md-3";		
		
	        var newInputDescriptionOperationCustomsql = document.createElement('input');
	        newInputDescriptionOperationCustomsql.style = "text-align: center;";
	        newInputDescriptionOperationCustomsql.className="op_desc_label_tittle"
	        newInputDescriptionOperationCustomsql.disabled="disabled"
	        newInputDescriptionOperationCustomsql.placeholder="desc."
	        descriptionCol1Div.appendChild(newInputDescriptionOperationCustomsql);		

		OperationDivDesc.appendChild(descriptionCol1Div);
		var descriptionCol2Div = document.createElement('div');
			descriptionCol2Div.className= "col-md-7";	
	        
	        var newLabelDescriptionOperationCustomsql = document.createElement('label');
			newLabelDescriptionOperationCustomsql.id=operation.identification + "_DESC";			
			newLabelDescriptionOperationCustomsql.className="op_desc_label"
			newLabelDescriptionOperationCustomsql.textContent = operation.description;
			newLabelDescriptionOperationCustomsql.name=operation.path + "_DESC";
			descriptionCol2Div.appendChild(newLabelDescriptionOperationCustomsql);
		
		OperationDivDesc.appendChild(descriptionCol2Div);	
		
		// div description get all the data inside
		var OperationDivMethod = document.createElement('div');
		OperationDivMethod.className = "row main";
		
//		var methodCol1Div = document.createElement('div');
//			methodCol1Div.className= "col-md-3";		
//		
//	        var newInputMethodOperationCustomsql = document.createElement('input');
//	        newInputMethodOperationCustomsql.style = "text-align: center;";
//	        newInputMethodOperationCustomsql.className="op_desc_label_tittle"
//	        newInputMethodOperationCustomsql.disabled="disabled"
//	        newInputMethodOperationCustomsql.placeholder="method"
//	        methodCol1Div.appendChild(newInputMethodOperationCustomsql);		
//
//		OperationDivMethod.appendChild(methodCol1Div);
//		var methodCol2Div = document.createElement('div');
//			methodCol2Div.className= "col-md-7";	
	        
//	        var newLabelMethodOperationCustomsql = document.createElement('label');
//	        newLabelMethodOperationCustomsql.id=operation.identification + "_METHOD";			
//	        newLabelMethodOperationCustomsql.className="op_desc_label"
//	        newLabelMethodOperationCustomsql.textContent = operation.operation;
//	        newLabelMethodOperationCustomsql.name=operation.path + "_METHOD";
//	        methodCol2Div.appendChild(newLabelMethodOperationCustomsql);
		
//	    OperationDivMethod.appendChild(methodCol2Div);	
		
		newCustomsqlParamDiv.appendChild(newInputCustomsqlOperationDiv);
		
		newCustomsqlParamDiv.appendChild(OperationDivEndpoint);
		
		newCustomsqlParamDiv.appendChild(OperationDivDesc);
		
		newCustomsqlParamDiv.appendChild(OperationDivMethod);
		
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
    				id = $('[name= "customsqlParamType_2'+param+"\"");
    				id.val(querystrings[j].headerType);
    				
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
            $('#id_type_op_customsql').selectpicker('val', operation.operation);
            
            for (var i = 0; i < operation.querystrings.length; i++) {
                if (operation.querystrings [i].name == "queryType" ){
                	if (operation.querystrings [i].value =="native"){
                		monacoFormat('native', null);
                		$('#id_customsql_querytype').val("native");
                		$('#id_customsql_querytype').change();
                	} else {
                		monacoFormat('sql', null);
                		$('#id_customsql_querytype').val("sql");
                		$('#id_customsql_querytype').change();
                	}
                }
            }
            
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
            	$('#postProcessOp').removeClass('hide');
            	$('#id_postprocess_op_customsql').show();
            	refreshCodeMirror();
            } else {
            	myCodeMirrorJs.setValue("");
            	$('#postProcessOp').addClass('hide');
            	$('#postProcessCheckbox').prop("checked",false)
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
            $('#id_type_op_customsql').selectpicker('val', operation.operation);

            for (var i = 0; i < operation.querystrings.length; i++) {
                if (operation.querystrings [i].name == "queryType" ){
                	if (operation.querystrings [i].value =="native"){
                		monacoFormat('native', null);
                		$('#id_customsql_querytype').val("native");
                		$('#id_customsql_querytype').change();
                	} else {
                		monacoFormat('sql', null);
                		$('#id_customsql_querytype').val("sql");
                		$('#id_customsql_querytype').change();
                	}
                }
            }
            
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
            	$('#postProcessOp').removeClass('hide');
            	$('#id_postprocess_op_customsql').show();
            } else {
            	myCodeMirrorJs.setValue("");
            	$('#postProcessOp').addClass('hide');
            	$('#postProcessCheckbox').prop("checked",false)
            }
            
            $('#id_name_op_customsql').prop('disabled', true);
            loadParamsCustomValues(operation.querystrings);
        } else {
        	$('#id_name_op_customsql').val("");
        	$('#id_desc_op_customsql').val("");
        			
        	loadParamsFromQuery("", "");
        	monacoFormat('sql', $("#ontology option:selected").text());	
        	
        	myCodeMirrorJs.setValue("");
        	$('#postProcessCheckbox').prop("checked",false)
        	$('#postProcessOp').addClass('hide');
        	
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
    	$('#' + operation.identification + "_PATH").html(operation.path);
        $('#' + operation.identification + "_DESC").html(operation.description);
        $('#' + operation.identification + "CUSTOMSQL").val(operation.operation);
        changeHTTPMethodColors();
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
        
        toastr.success(messagesForms.operations.genOpSuccess,'');

    }  
    
    function changeHTTPMethodColors(){
    	let els = document.getElementsByClassName("op_button_selected");
		Array.prototype.forEach.call(els, function(el) {
			if(el.value === 'PUT'){
				el.style = "background-color: #fca130;"
			}else if(el.value === 'POST'){
				el.style = "background-color: #49cc90;"
			}else if(el.value === 'DELETE'){
				el.style = "background-color: #f93e3e;"
			}else if(el.value === 'GET'){
				el.style = "background-color: #61affe;"
			}
		});
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
			changeHTTPMethodColors();
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

