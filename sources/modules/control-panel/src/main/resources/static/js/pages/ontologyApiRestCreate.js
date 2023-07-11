var authorizationsArr 		= []; // add authorizations
var authorizationUpdateArr  = []; // get authorizations of the ontology
var authorizationsIds 		= []; // get authorizations ids for actions
var authorizationObj 		= {}; // object to receive authorizations responses.
var mountableModel2 = $('#ontology_autthorizations').find('tr.authorization-model')[0].outerHTML;

var wizardStep = 1;

var OntologyCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Ontology Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var validTypes = ["object","string","number","integer","date","timestamp","array","geometry","file","boolean"]; // Valid property types	
	var mountableModel3 = $('#pathsParams').find('tr.mountable-model')[0].outerHTML;
	var mountableModel4 = $('#queriesParams').find('tr.mountable-model')[0].outerHTML;
	var validJsonSchema = false;
	var validMetaInf = false;
	var hasId = false; // instance
	var nextIndex=0;
	var jsonPathParams = [];
	var jsonQueryParams = []
	var operationsNames = [];
	var pathParamNames = [];
	var queryParamNames = [];
	var isExternal = false;
	var emptyBaseId='';
	var headersNames = [];
	var definitions = {};
	var myCodeMirror;

	
	// CONTROLLER PRIVATE FUNCTIONS	--------------------------------------------------------------------------------
	
	 
	$('#pathsParams').mounTable(jsonPathParams,{
		model: '.mountable-model',
		noDebug: false,
		addLine:{				
			button: "#addPathParamOperation",					
			onClick: function (element){
				
				console.log('PathParam added!');
				return true;
			}
		}
			
	});
	
	$('#pathsParams tbody').on("DOMSubtreeModified", function(){
		var inputs = $("input[name='indexes\\[\\]']");
		$.each(inputs, function(i, item){
			inputs[i].value = i;
		});
	});
	
	$('#queriesParams').mounTable(jsonQueryParams,{
		model: '.mountable-model',
		noDebug: false,
		addLine:{				
			button: "#addQueryParamOperation",					
			onClick: function (element){
				console.log('PathParam added!');				
				return true;
			}
		}			
	});
	
	
	$('input[type=radio][name=auth]').change(function() {
        if (this.value == 'apiKey') {
        	$("#authMethod").val('apiKey');
           $("#header").removeAttr("disabled");
           $("#token").removeAttr("disabled");
           $("#oauthUser").attr("disabled","disabled");
           $("#oauthPass").attr("disabled","disabled");
           $("#basicUser").attr("disabled","disabled");
           $("#basicPass").attr("disabled","disabled");
           $("#oauthUser").val("");
           $("#oauthPass").val("");
           $("#basicUser").val("");
           $("#basicPass").val("");
        }else if (this.value == 'oauth') {
        	$("#authMethod").val('oauth');
        	$("#header").attr("disabled","disabled");;
            $("#token").attr("disabled","disabled");
            $("#oauthUser").removeAttr("disabled");
            $("#oauthPass").removeAttr("disabled");
            $("#basicUser").attr("disabled","disabled");
            $("#basicPass").attr("disabled","disabled");
            $("#header").val("");
            $("#token").val("");
            $("#basicUser").val("");
            $("#basicPass").val("");
        }else if (this.value == 'basic') {
        	$("#authMethod").val('basic');
        	$("#header").attr("disabled","disabled");
            $("#token").attr("disabled","disabled");
            $("#oauthUser").attr("disabled","disabled");
            $("#oauthPass").attr("disabled","disabled");
            $("#basicUser").removeAttr("disabled");
            $("#basicPass").removeAttr("disabled");
            $("#header").val("");
            $("#token").val("");
            $("#oauthPass").val("");
            $("#oauthUser").val("");
        }
    });
	
	$("#auth").on("click", function(){
		if($("#auth").is(":checked")){
			$("#authenticationOptions").show();
		}else{
			$("#authenticationOptions").hide();
		}
	});
	
	
	$('#addOperation').on('click', function(){
		event.preventDefault();
		logControl ? console.log('AddPathParamOperation() -> ') : '';
	
		var name = $("#nameOperation").val();
		var path = $("#pathOperation").val();
		var typeOperation = $("#typeOperation").val();
		var defaultOperationType = $("#defaultOperationType").val();
		var description = $("#descriptionOperation").val();
		
		if(name=="" || name==null || name==undefined){
			toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.operation.name);
			return;
		}
		
		if(description=="" || description==null || description==undefined){
			toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.operation.desc);
			return;
		}
		
		//check Unique names for operations
		if (operationsNames.includes(name))  { $.alert({title: 'ERROR!', theme: 'light', type: 'red', content: ontologyCreateReg.validations.duplicates}); return false; }
		
		var origin = ontologyCreateReg.manual;
		
		var indexes = $("input[name='indexes\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();				
		var namesPath = $("input[name='namesPaths\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();		
		var fieldsPath = $("input[name='fieldsPaths\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();	
		
		var pathParamJson = [];
		var notInPathDefinition = [];
		if ( namesPath.length ){	
			$.each(namesPath, function( index, value ) {
				nameIndex = namesPath.indexOf(value);
				pathParamJson.push({'indexes': indexes[nameIndex], 'namesPaths':value, 'fieldsPaths':fieldsPath[index]});
				if(!path.includes('{'+value+'}') ) notInPathDefinition.push(value);
			});			
		}
		//check path params defined in the path
		if(notInPathDefinition.length >0){
			toastr.error(messagesForms.operations.genOpError, ontologyCreateReg.validations.wrong.path.params +' '+notInPathDefinition.toString().replace(/,/g,', '));
			return false; 
		}
		var namesQuery = $("input[name='namesQueries\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();		
		var fieldsQuery = $("input[name='fieldsQueries\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();
		
		var queryParamJson=[];
		var isFirst= true;
		var queryParamsCallExample = "?";
		
		if ( namesQuery.length ){	
			$.each(namesQuery, function( index, value ) {
				nameIndex = namesQuery.indexOf(value);
				queryParamJson.push({'namesQueries':value, 'fieldsQueries':fieldsQuery[index]});
				queryParamsCallExample += value + "=" + fieldsQuery[index] + "&";
			});			
		}
		queryParamsCallExample = path + queryParamsCallExample.substr(0,queryParamsCallExample.length-1);
		
		var numParams = queryParamJson.length + pathParamJson.length;
		
		if ((defaultOperationType == 'GET_BY_ID' || defaultOperationType == 'DELETE_BY_ID' || defaultOperationType == 'UPDATE_BY_ID') && numParams != 1){
			//Operations need one parameter
			toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.operation.one.param);
			return false;
		}
		if ((defaultOperationType == 'GET_ALL' || defaultOperationType == 'INSERT' || defaultOperationType == 'DELETE_ALL' ) && numParams != 0) {
			//Operations must have no params
			toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.operation.no.params);
			return false;
		}		
		
		operationsNames.push(name);
		
		$("#operationsList tbody").append("<tr id='operation_"+name+"'></tr>");
		$("#operation_"+name).append("<td class='' value='" + name + "' id='" + name + "'>" + name +"</td>");
		$("#operation_"+name).append("<td class='hide' value='" + path + "' id='path_" + name + "'>" + path +"</td>");
		$("#operation_"+name).append("<td class='' value='" + queryParamsCallExample + "' id='path_params_" + name + "'>" + queryParamsCallExample +"</td>");
		$("#operation_"+name).append("<td class='' value='" + typeOperation + "' id='type_" + name + "'>" + typeOperation +"</td>");
		$("#operation_"+name).append("<td class='' value='" + defaultOperationType + "' id='default_operation_type_" + name + "'>" + defaultOperationType +"</td>");
		$("#operation_"+name).append("<td class='' value='" + description + "' id='des_" + name + "'>" + description +"</td>");
		$("#operation_"+name).append("<td class='' value='" + origin + "' id='" + origin + "'>" + origin +"</td>");
		
		
		$("#operation_"+name).append("<td class='hide' value=" + JSON.stringify(pathParamJson)  +" id='pathParams_" + name +"'>" + JSON.stringify(pathParamJson)  +"</td>");
		$("#operation_"+name).append("<td class='hide' value=" + JSON.stringify(queryParamJson) +" id='queryParams_" + name +"'>" + JSON.stringify(queryParamJson)  +"</td>");	
		$("#operation_"+name).append("<td class='icon' style='white-space: nowrap'><div class='grupo-iconos'><span  class='btn btn-xs btn-no-border icon-on-table color-blue tooltips' data-container='body' data-placement='bottom' onclick='OntologyCreateController.showOperation(\""+name+"\")'><i class='la la-eye font-hg'></i></span><span  class='btn btn-xs btn-no-border icon-on-table color-blue tooltips' onclick='OntologyCreateController.editOperation(\""+name+"\")'><i class='icon-edit'></i></span> <span class='btn btn-xs btn-no-border icon-on-table color-red tooltips' data-container='body' data-placement='bottom' th:title='#{gen.deleteBtn} ' th:data-original-title='#{gen.deleteBtn} ' onclick='OntologyCreateController.deleteOperation(\""+name+"\")'><i class='icon-delete'></i></span></div></td>");
		$("#operations_div").show();
		
		$("#nameOperation").val("");
		$("#pathOperation").val("");
		$("#typeOperation").val("get");
		$("#descriptionOperation").val("");
		$("#pathsParams tbody tr").remove();
		$("#queriesParams tbody tr").remove();
		
		toastr.success(messagesForms.operations.genOpSuccess,'');
		
		manageWizardStep();
	});
	
	$('#importSwagger').on('click', function(){
		event.preventDefault();
		var url = '/controlpanel/ontologies/swaggerApi?url='+$('#swagger').val();
		$.get(url, function(data, status){
			swaggerApiRestParser(JSON.parse(data),operationsNames);
	    }).fail(function(data) {
	    	toastr.error(messagesForms.operations.genOpError,ontologyCreateJson.validations.apirest.invalidUrl);
	    });
		
	});
	
	var swaggerApiRestParser = function(data,operationsNames){
		if(data) {
			//check swagger expected fields
			if (data.basePath && data.host && data.paths) {
				var baseUrl = data.host+data.basePath;
				if(!baseUrl.toLowerCase().startsWith("http"))
					baseUrl = "http://" + baseUrl;
				$('#urlBase').val(baseUrl);
				var duplicatedOperations = [];
				for (var key in data.paths) {
				    // skip loop if the property is from prototype
				    if (!data.paths.hasOwnProperty(key)) continue;

				    var path = data.paths[key];
				    var operation = null;

				    if(path.get){
				    	var inserted = insertSwaggerOperation(key,"get",path.get,operationsNames);
				    	if(!inserted) duplicatedOperations.push(path.get.operationId); 
				    }
				    if(path.post){
				    	var inserted = insertSwaggerOperation(key,"post",path.post,operationsNames);
				    	if(!inserted) duplicatedOperations.push(path.post.operationId); 
				    }
				    if(path.put){
				    	var inserted = insertSwaggerOperation(key,"put",path.put,operationsNames);
				    	if(!inserted) duplicatedOperations.push(path.put.operationId); 
				    }
				    if(path.delete){
				    	var inserted = insertSwaggerOperation(key,"delete",path.delete,operationsNames);
				    	if(!inserted) duplicatedOperations.push(path.delete.operationId); 
				    }
				}
				if(duplicatedOperations.length > 0){
					//If duplicates detected, inform the user
					toastr.error(messagesForms.operations.genOpError, ontologyCreateJson.validations.apirest.duplicates +' '+duplicatedOperations.toString().replace(/,/g,', '));
				}
				//TODO: Fill modal list with definitions
				$('#definitionsNames').empty();
				if(data.definitions){
					Object.keys(data.definitions).forEach(function(definitionName){
						$('#definitionsNames').append('<option value="'+definitionName+'">'+definitionName+'</option>');
					});
				}
				definitions = data.definitions;
				$('#modal-swagger-definitions').modal('show');
				
				toastr.success(messagesForms.operations.genOpSuccess,'');
			} else {
				toastr.error(messagesForms.operations.genOpError,ontologyCreateJson.validations.apirest.parseError);
			}
		} else {
			toastr.error(messagesForms.operations.genOpError,ontologyCreateJson.validations.apirest.invalidUrl);
		}
		manageWizardStep();
	};
	
	var insertSwaggerOperation = function(path, method, data){
		var name = data.operationId;
		
		//UNIQUE?
		if ( operationsNames.includes(name) )return false;
		
		var origin = 'SWAGGER';
		
		var queryParamsCallExample = "?";

		var pathParamJson = [];
		var queryParamJson = [];
		var pathIndex = 0;
		if(data.parameters){
			data.parameters.forEach(function(param) {
				if(param.in == "path"){
					pathParamJson.push({'indexes': pathIndex, 'namesPaths':param.name, 'fieldsPaths':param.name});
					pathIndex++;
				} else if (param.in =="query"){
					queryParamJson.push({'namesQueries':param.name, 'fieldsQueries':param.name});
					queryParamsCallExample += param.name + "=" + param.name + "&";
				}
			});
		}
		queryParamsCallExample = path + queryParamsCallExample.substr(0,queryParamsCallExample.length-1);
		
		$("#operationsList tbody").append("<tr id='operation_"+name+"'></tr>");
		$("#operation_"+name).append("<td class='' value='" + name + "' id='" + name + "'>" + name +"</td>");
		$("#operation_"+name).append("<td class='hide' value='" + path + "' id='path_" + name + "'>" + path +"</td>");
		$("#operation_"+name).append("<td class='' value='" + queryParamsCallExample + "' id='path_params_" + name + "'>" + queryParamsCallExample +"</td>");
		$("#operation_"+name).append("<td class='' value='" + method + "' id='type_" + name + "'>" + method +"</td>");
		$("#operation_"+name).append("<td class='' value='NONE' id='default_operation_type_" + name + "'>NONE</td>");
		$("#operation_"+name).append("<td class='' value='" + data.summary + "' id='des_" + name + "'>" + data.summary +"</td>");
		$("#operation_"+name).append("<td class='' value='" + origin + "' id='" + origin + "'>" + origin +"</td>");
		
		
		$("#operation_"+name).append("<td class='hide' value=" + JSON.stringify(pathParamJson)  +" id='pathParams_" + name +"'>" + JSON.stringify(pathParamJson)  +"</td>");
		$("#operation_"+name).append("<td class='hide' value=" + JSON.stringify(queryParamJson) +" id='queryParams_" + name +"'>" + JSON.stringify(queryParamJson)  +"</td>");	
		$("#operation_"+name).append("<td class='icon' style='white-space: nowrap'><div class='grupo-iconos'><span  class='btn btn-xs btn-no-border icon-on-table color-blue tooltips' data-container='body' data-placement='bottom' onclick='OntologyCreateController.showOperation(\""+name+"\")'><i class='la la-eye font-hg'></i></span><span  class='btn btn-xs btn-no-border icon-on-table color-blue tooltips' onclick='OntologyCreateController.editOperation(\""+name+"\")'><i class='icon-edit'></i></span> <span class='btn btn-xs btn-no-border icon-on-table color-red tooltips' data-container='body' data-placement='bottom' th:title='#{gen.deleteBtn} ' th:data-original-title='#{gen.deleteBtn} ' onclick='OntologyCreateController.deleteOperation(\""+name+"\")'><i class='icon-delete'></i></span></div></td>");
		$("#operations_div").show();
		operationsNames.push(name);
		return true;
	};
	
	$('#addHeaderBtn').on('click', function(){
		event.preventDefault();
		logControl ? console.log('addHeaderBtn() -> ') : '';
	
		var key = $("#headerKey").val();
		var value = $("#headerValue").val();
		
		if(key=="" || key==null || key==undefined){
			toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.header.key);
			return;
		}
		
		if(value=="" || value==null || value==undefined){
			toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.header.key);
			return;
		}
		
		headersNames.push(key);
		
		checkUnique = headersNames.unique();
		if (headersNames.length !== checkUnique.length)  {
			toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.duplicates);
			return false; 
		} 
		
		$("#headersList tbody").append("<tr id='header_"+key+"'></tr>");
		$("#header_"+key).append("<td class='' value='" + key + "' id='" + key + "'>" + key +"</td>");
		$("#header_"+key).append("<td class='' value='" + value + "' id='value_" + key + "'>" + value +"</td>");
		
		$("#header_"+key).append("<td class='icon' style='white-space: nowrap'><div class='grupo-iconos'><span class='btn btn-xs btn-no-border icon-on-table color-red tooltips' data-container='body' data-placement='bottom' th:title='#{gen.deleteBtn} ' th:data-original-title='#{gen.deleteBtn} ' onclick='OntologyCreateController.deleteHeader(\""+key+"\")'><i class='icon-delete'></i></span></div></td>");
		$("#headers_div").show();
		
		$("#headerKey").val("");
		$("#headerValue").val("");
		
		toastr.success(messagesForms.operations.genOpSuccess,'');
		
	});
	

	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// SELECT SWAGGER OBJECT DEFINITION
	var selectSwaggerDefinitionObject = function(){
		//TODO: select from list the definition of a swagger object
		var definition = definitions[$('#definitionsNames').val()];
		recursiveFieldSearch(definition,definition);
		var objectDefinition = JSON.stringify(definition);
		editorRest.setText(objectDefinition);
	}
	
	var recursiveFieldSearch = function(dataObject, originalParent){
	    for (prop in  dataObject) {
	         if( dataObject.hasOwnProperty(prop)  ) {
	            
	            if (typeof dataObject[prop] === 'object'){
	                //recursive call
	                recursiveFieldSearch(dataObject[prop],originalParent);
	            }else if(prop === '$ref' ){
	                //FOUND IT
	                if(dataObject[prop].includes("definitions/")){
	                	//Change reference according to new schema
	                    dataObject[prop] = dataObject[prop].replace("definitions/","");
	                    var referencedObject = dataObject[prop].substring(2);
	                    //Add referenced object definition to new schema
	                    originalParent[referencedObject] = definitions[referencedObject];
	                    console.log(referencedObject);
	                }
	            }
	        }
	    }
	}
	
	var manageWizardStep = function(){
		if (wizardStepValid()){
			wizardStepForward();
		} else {
			wizardStepReset();
		}
	}
	
	var wizardStepValid = function(){
		
		if (wizardStep == 1){
			return ($('#identification').val().length >= 5 && 
					$('#description').val().length >= 5 && 
					$('#metainf').val().length >= 5 &&
					$('#urlBase').val().length >= 5);
		} else if (wizardStep == 2){
			return (operationsNames.length>0);
		}
	}
	
	var wizardStepForward = function(){
		$('#continueBtn').prop('disabled', false);
		if (wizardStep == 1){
			$('#stepOneCheckbox').prop('checked', true);
			$('#stepOneCheckbox').prop('disabled', false);
			$('#stepOneCheckbox').nextAll('span:first').addClass('wizard-success-step');
		} else if (wizardStep == 2){
			$('#stepTwoCheckbox').prop('checked', true);
			$('#stepTwoCheckbox').prop('disabled', false);
			$('#stepTwoCheckbox').nextAll('span:first').addClass('wizard-success-step');
			$('#createWizardBtn').prop('disabled', false);
		}
	}
	
	var wizardStepReset = function(){
		$('#continueBtn').prop('disabled', true);
		if (wizardStep == 1){
			$('#stepOneCheckbox').prop('checked', false);
			$('#stepOneCheckbox').prop('disabled', true);
			$('#stepOneCheckbox').nextAll('span:first').removeClass('wizard-success-step');
		}  else if (wizardStep == 2){
			$('#stepTwoCheckbox').prop('checked', false);
			$('#stepTwoCheckbox').prop('disabled', true);
			$('#stepTwoCheckbox').nextAll('span:first').removeClass('wizard-success-step');
			$('#createWizardBtn').prop('disabled', true);
		}
	}
	
	var wizardStepContinue = function(){
		if (wizardStep == 1){		 
				$('#tab-restapi-info a').removeClass('disabled');
				$('#tab-restapi-info a').click();
				$('#continueBtn').addClass('hide');
				$('#createWizardBtn').removeClass('hide');
				if ($("#allowsCreateTable") || $("#allowsCreateTable").is(':checked')) {
					$('#saveSqlCode').show();
				} else {
					$('#saveSqlCode').hide();
				}
				wizardStep = 2;			 
		} else if (wizardStep == 2){
			
		}
	}	
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#ontology_create_form');
					
		// set current language
		currentLanguage = ontologyCreateReg.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// custom messages
            messages: {},
			// validation rules
            rules: {
				ontologyId:		{ minlength: 5, required: true },
                identification:	{ minlength: 5, required: true },
				description:	{ minlength: 5, required: true }
            },
            invalidHandler: function(event, validator) { //display error alert on form submit              
            	toastr.error(messagesForms.validation.genFormError,'');
                validateTagsInput();
            },
            errorPlacement: function(error, element) {				
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }				
				else { error.insertAfter(element); }
            },
            highlight: function(element) { // hightlight error inputs
                $(element).closest('.form-group').addClass('has-error'); 
                $(element).nextAll('span:last-child').addClass('hide');
            },
            unhighlight: function(element) { // revert the change done by hightlight
                $(element).closest('.form-group').removeClass('has-error');
                $(element).nextAll('span:last-child').addClass('hide');
            },
            success: function(label) {
                label.closest('.form-group').removeClass('has-error');
            },
			// ALL OK, THEN SUBMIT.
            submitHandler: function(form) {
            	
				// VALIDATE JSON SCHEMA 
                var postOperations = [];
            	var json =[];
            	var operations = $("#operationsList tbody tr");
            	$.each(operations, function(i,item){
            		
            		if(i!=0){
            			var ops = $("#"+item.id + " td");
            			
            			json.push({'name': ops[0].innerHTML , 'path': ops[1].innerHTML,'type': ops[3].innerHTML,'defaultOperationType': ops[4].innerHTML, 'description' : ops[5].innerHTML, 'origin' : ops[6].innerHTML, 'pathParams' : JSON.parse(ops[7].innerHTML) , 'queryParams' : JSON.parse(ops[8].innerHTML)});
            			if(ops[1].innerHTML == "post"){
            				postOperations.push(ops[1].innerHTML);
            			}
            		}
 			       
       		        
       		     });
            	
            	 $("<input type='hidden' value='"+JSON.stringify(json)+"' />")
   		         	.attr("name", "operations")
   		         	.appendTo("#ontology_create_form");
            	 
            	var json =[];
             	var headers = $("#headersList tbody tr");
             	$.each(headers, function(i,item){
             		
             		if(i!=0 && item.id){
             			var header = $("#"+item.id + " td");
             			
             			json.push({'key': header[0].innerHTML , 'value': header[1].innerHTML});
             			
             		}
  			       
       		        
       		     });
             	
             	 $("<input type='hidden' value='"+JSON.stringify(json)+"' />")
    		         	.attr("name", "headers")
    		         	.appendTo("#ontology_create_form");
             	
             	 if(postOperations.length>0 && editorRest.getText() == "{}"){
             		toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.schema);
        			return;
             	 }else{
             		if(IsJsonString(editorRest.getText())){
                 		$("#schema").val(editorRest.getText());
                 	}
             	 }
             	
            	 
            	// VALIDATE TAGSINPUT
				validMetaInf = validateTagsInput();
				if (validMetaInf) {
					// form.submit();
					form1.ajaxSubmit({type: 'post', success : function(data){
						toastr.success(messagesForms.operations.genOpSuccess,'');
						$('#modal-created').modal('show');
						//navigateUrl(data.redirect);
						}, error: function(data){
							toastr.error(messagesForms.operations.genOpError,data.responseJSON.cause);
						}
					})					
				}
			}
        });
    }
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {

		// CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm();
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});

		// CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');

		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		$('.tagsinput').prev().removeClass('tagsinput-has-error');
		$('.tagsinput').nextAll('span:first').addClass('hide');

		// CLEAN ALERT MSG
		$('.alert-danger').hide();

	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() ->  resetForm,  currentLanguage: ' + currentLanguage) : '';
		
		// tagsinput validate fix when handleValidation()
		$('#metainf').on('itemAdded', function(event) {
			
			if ($(this).val() !== ''){ $('#metainferror').addClass('hide');}
		});
		
		// authorization tab control 
		$(".option a[href='#tab_2']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			$.alert({title: 'INFO!', type: 'blue' , theme: 'light', content: ontologyCreateReg.validations.authinsert});
			return false;
		  }
		});
				
		
		// 	INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_]*", greedy: false });
		
		// Wizard container
		
		// general inf tab control
		$(".wizard-option a[href='#tab_1']").on("click", function(e) {
	        $('.wizardContainer').find('.wizard-option').removeClass('active');
	        $('#tab-datos').addClass('active');
		
			$('#continueBtn').removeClass('hide');
			$('#continueBtn').prop('disabled', false);
			$('#createWizardBtn').addClass('hide');			
			
			wizardStep = 1;
	    });
		
		// general template/schema tab control
		$(".wizard-option a[href='#tab_restapi_info']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
				e.preventDefault();
				return false;
		  } else {
	        $('.wizardContainer').find('.wizard-option').removeClass('active');
	        $('#tab-restapi-info').addClass('active');
	        
			$('#continueBtn').removeClass('hide');
			$('#continueBtn').prop('disabled', false);
			$('#createWizardBtn').addClass('hide');			
			
			wizardStep = 2;
		  }
	    });
		
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('ontology_create_form');
		});
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		}).on('keyup', function(){
			if (ontologyCreateJson.actionMode==null){
				manageWizardStep();
			}
		});
		
		$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.getElementsByTagName('select')[0]){
				$('.form').validate().element('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id'));
			}
		}).on('keyup', function(){
			if (ontologyCreateJson.actionMode==null){
				manageWizardStep();
			}
		});
	
		$('.tagsinput').filter('[required]').parent().on('blur', 'input', function(event) {
			if ($(event.target).parent().next().val() == ''){
				$(event.target).parent().next().nextAll('span:first').removeClass('hide');
				$(event.target).parent().next().nextAll('span:last-child').addClass('hide');
				$(event.target).parent().addClass('tagsinput-has-error');
			} else if($(event.target).parent().next().val().length < 5){
				$(event.target).parent().next().nextAll('span:last-child').addClass('font-red');
				$(event.target).parent().next().nextAll('span:last-child').removeClass('hide');
				$(event.target).parent().addClass('tagsinput-has-error');
			} else {
				$(event.target).parent().next().nextAll('span:first').addClass('hide');
				$(event.target).parent().next().nextAll('span:last-child').addClass('hide');
				$(event.target).parent().removeClass('tagsinput-has-error');
			} 
			if (ontologyCreateJson.actionMode==null){
				manageWizardStep();
			}  
		}).on('keyup', function(){
			if (ontologyCreateJson.actionMode==null){
				manageWizardStep();
			}
		}).on('itemAdded', function(event) {
			if (ontologyCreateJson.actionMode==null){
				manageWizardStep();
			}
		});
		

		// UPDATE TITLE AND DESCRIPTION IF CHANGED 
		$('#identification').on('change', function(){
			var jsonFromEditor = {};
			var datamodelLoaded = $('#datamodel_properties').attr('data-loaded');
			if (datamodelLoaded){			
				if (IsJsonString(editor.getText())){				
					jsonFromEditor = editor.get();
					jsonFromEditor["title"] = $(this).val();
					editor.set(jsonFromEditor);
				}			
			}		
		});
	
		$('#description').on('change', function(){
			var jsonFromEditor = {};
			var datamodelLoaded = $('#datamodel_properties').attr('data-loaded');
			if (datamodelLoaded){			
				if (IsJsonString(editor.getText())){				
					jsonFromEditor = editor.get();
					jsonFromEditor["description"] = $(this).val();
					editor.set(jsonFromEditor);
				}			
			}	
			
		});
		
		// INSERT MODE ACTIONS  (ontologyCreateReg.actionMode = NULL ) 
		if ( ontologyCreateReg.actionMode === null){
			logControl ? console.log('|---> Action-mode: INSERT') : '';
			
			// Set active 
			$('#active').trigger('click');
			
			// Set Public 
			$('#public').trigger('click');
		}
		// EDIT MODE ACTION 
		else {	
			logControl ? console.log('|---> Action-mode: UPDATE') : '';
			
			// if ontology has authorizations we load it!.
			authorizationsJson = ontologyCreateReg.authorizations;			
			if (authorizationsJson.length > 0 ){
				
				// MOUNTING AUTHORIZATIONS ARRAY
				var authid_update, accesstype_update , userid_update , authorizationUpdate , authorizationIdUpdate = '';
				$.each( authorizationsJson, function (key, object){			
					
					authid_update 		= object.id; 
					accesstype_update 	= object.typeName; 
					userid_update 		= object.userId;					
					
					logControl ? console.log('      |----- authorizations object on Update, ID: ' +  authid_update + ' TYPE: ' +  accesstype_update + ' USER: ' +  userid_update  ) : '';
					
					// AUTHs-table {"users":user,"accesstypes":accesstype,"id": response.id}
					authorizationUpdate = {"users": userid_update, "accesstypes": accesstype_update, "id": authid_update};					
					authorizationsArr.push(authorizationUpdate);
					
					// AUTH-Ids {[user_id]:auth_id}
					authorizationIdUpdate = {[userid_update]:authid_update};
					authorizationsIds.push(authorizationIdUpdate);
					
					// disable this users on users select
					$("#users option[value=" + userid_update + "]").prop('disabled', true);
					$("#users").selectpicker('refresh');
					
				});

				// TO-HTML
				if ($('#authorizations').attr('data-loaded') === 'true'){
					$('#ontology_autthorizations > tbody').html("");
					$('#ontology_autthorizations > tbody').append(mountableModel2);
				}
				logControl ? console.log('authorizationsArr on UPDATE: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr)) : '';
				$('#ontology_autthorizations').mounTable(authorizationsArr,{
					model: '.authorization-model',
					noDebug: false							
				});
				
				// hide info , disable user and show table
				$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));					
				$('#authorizations').removeClass('hide');
				$('#authorizations').attr('data-loaded',true);// TO-HTML
				$("#users").selectpicker('deselectAll');
				
				showHideImageTableOntology();
			}
			
			//OntologyRest
			var ontologyRest = ontologyCreateReg.ontologyRest;
			if(ontologyRest.baseUrl!=null || ontologyRest.baseUrl!=undefined || ontologyRest.baseUrl!=''){
				$("#urlBase").val(ontologyRest.baseUrl);
				var securityType = ontologyCreateReg.ontologyRest.securityType;
				if(securityType!='none'){
					$("#auth").trigger("click");
					var security = ontologyCreateReg.ontologyRest.security;
					if(securityType=='ApiKey'){
						var json = JSON.parse(security);
						$("#header").val(json.header);
						$("#token").val(json.token);
						$("#apiKey").trigger("click");
					}else if(securityType=='Basic'){
						var json = JSON.parse(security);
						$("#basicUser").val(json.user);
						$("#basicPass").val(json.password);
						$("#basic").trigger("click");
					}else if(securityType=='OAuth'){
						var json = JSON.parse(security);
						$("#oauthUser").val(json.user);
						$("#oauthPass").val(json.password);
						$("#oauth").trigger("click");
					}
				}
				
				var headers = ontologyCreateReg.ontologyRest.headers;
				var jsonHeaders = JSON.parse(headers);
				
				for(var i=0; i<jsonHeaders.length; i++){
					var json = jsonHeaders[i];
					
					$("#headersList tbody").append("<tr id='header_"+json.key+"'></tr>");
					$("#header_"+json.key).append("<td class='' value='" + json.key + "' id='" + json.key + "'>" + json.key +"</td>");
					$("#header_"+json.key).append("<td class='' value='" + json.value + "' id='value_" + json.key + "'>" + json.value +"</td>");
					
					$("#header_"+json.key).append("<td class='icon' style='white-space: nowrap'><div class='grupo-iconos'><span class='btn btn-xs btn-no-border icon-on-table color-red tooltips' data-container='body' data-placement='bottom' th:title='#{gen.deleteBtn} ' th:data-original-title='#{gen.deleteBtn} ' onclick='OntologyCreateController.deleteHeader(\""+json.key+"\")'><i class='icon-delete'></i></span></div></td>");
					$("#headers_div").show();
					
					headersNames.push(json.key);
				}
				
				if(ontologyCreateReg.ontologyRest.infer){
					$("#infer").trigger("click");
				}
				
				var operations = ontologyCreateReg.ontologyRest.loperations;
				for(var i=0; i<operations.length;i++){
					var operation = operations[i];
					
					
					var params = operation.lparams;
					var queryParamsCallExample = "?";
					
					var jsonPath = [];
					var jsonQuery = [];
					for(var x=0; x<params.length;x++){
						var param = params[x];
						if(param.type=='PATH'){
							jsonPath.push({'indexes': param.index, 'namesPaths':param.name, 'fieldsPaths': param.field});
						}else if(param.type=='QUERY'){
							jsonQuery.push({'namesQueries':param.name,  'fieldsQueries': param.field});
							queryParamsCallExample += param.name + "=" + param.field + "&";
						}
					}
					queryParamsCallExample = operation.path + queryParamsCallExample.substr(0,queryParamsCallExample.length-1);
					
					$("#operationsList tbody").append("<tr id='operation_"+operation.name+"'></tr>");
					$("#operation_"+operation.name).append("<td class='' value='" + operation.name + "' id='" + operation.name + "'>" + operation.name +"</td>");
					$("#operation_"+operation.name).append("<td class='hide' value='" + operation.path + "' id='path_" + operation.name + "'>" + operation.path +"</td>");
					$("#operation_"+operation.name).append("<td class='' value='" + queryParamsCallExample + "' id='path_params_" + operation.name + "'>" + queryParamsCallExample +"</td>");
					$("#operation_"+operation.name).append("<td class='' value='" + operation.type.toLowerCase() + "' id='type_" + operation.name + "'>" + operation.type.toLowerCase() +"</td>");
					$("#operation_"+operation.name).append("<td class='' value='" + operation.defaultOperationType + "' id='default_operation_type_" + operation.name + "'>" + operation.defaultOperationType +"</td>");
					$("#operation_"+operation.name).append("<td class='' value='" + operation.description + "' id='des_" + operation.name + "'>" + operation.description +"</td>");
					$("#operation_"+operation.name).append("<td class='' value='" + operation.origin + "' id='" + operation.origin + "'>" + operation.origin +"</td>");
					
					
					$("#operation_"+operation.name).append("<td class='hide' value=" + JSON.stringify(jsonPath)  +" id='pathParams_" + operation.name +"'>" + JSON.stringify(jsonPath)  +"</td>");
					
					$("#operation_"+operation.name).append("<td class='hide' value=" + JSON.stringify(jsonQuery) +" id='queryParams_" + operation.name +"'>" + JSON.stringify(jsonQuery)  +"</td>");	
					$("#operation_"+operation.name).append("<td class='icon' style='white-space: nowrap'><div class='grupo-iconos'><span  class='btn btn-xs btn-no-border icon-on-table color-blue tooltips' data-container='body' data-placement='bottom' onclick='OntologyCreateController.showOperation(\""+operation.name+"\")'><i class='la la-eye font-hg'></i></span><span  class='btn btn-xs btn-no-border icon-on-table color-blue tooltips' onclick='OntologyCreateController.editOperation(\""+operation.name+"\")'><i class='icon-edit'></i></span> <span class='btn btn-xs btn-no-border icon-on-table color-red tooltips' data-container='body' data-placement='bottom' th:title='#{gen.deleteBtn} ' th:data-original-title='#{gen.deleteBtn} ' onclick='OntologyCreateController.deleteOperation(\""+operation.name+"\")'><i class='icon-delete'></i></span></div></td>");
					//$("#operation_"+operation.name).append("<td class='icon' style='white-space: nowrap'><div class='grupo-iconos'><span  class='btn btn-xs btn-no-border btn-circle btn-outline blue tooltips' data-container='body' data-placement='bottom' onclick='OntologyCreateController.showOperation(\""+name+"\")'><i class='la la-eye font-hg'></i></span><span  class='btn btn-xs btn-no-border btn-circle btn-outline blue tooltips' onclick='OntologyCreateController.editOperation(\""+name+"\")'><i class='la la-edit font-hg'></i></span> <span class='btn btn-xs btn-no-border btn-circle btn-outline blue tooltips' data-container='body' data-placement='bottom' th:title='#{gen.deleteBtn} ' th:data-original-title='#{gen.deleteBtn} ' onclick='OntologyCreateController.deleteOperation(\""+name+"\")'><i class='fa fa-trash font-hg'></i></span></div></td>");
					$("#operations_div").show();
					
					operationsNames.push(operation.name);
					
				}
			}
			
			// take schema from ontology and load it
			schema = ontologyCreateReg.ontologyRest.jsonSchema;	
			
			editorRest.set(JSON.parse(schema));
			
			// overwrite datamodel schema with loaded ontology schema generated with this datamodel  template.
			var theSelectedModel = $("h3[data-model='"+ ontologyCreateReg.dataModelEditMode +"']");
			var theSelectedModelType = theSelectedModel.closest('div .panel-collapse').parent().find("a").trigger('click');			
			theSelectedModel.attr('data-schema',schema).trigger('click');
			
		}		
	}	
	
	
	
	// DELETE ONTOLOGY
	var deleteOntologyConfirmation = function(ontologyId){
		console.log('deleteOntologyConfirmation() -> formId: '+ ontologyId);
		
		// no Id no fun!
		if ( !ontologyId ) {$.alert({title: 'ERROR!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.validform}); return false; }
		
		logControl ? console.log('deleteOntologyConfirmation() -> formAction: ' + $('.delete-ontology').attr('action') + ' ID: ' + $('#delete-ontologyId').attr('ontologyId')) : '';
		
		// call ontology Confirm at header. 
		HeaderController.showConfirmDialogOntologia('delete_ontology_form',ontologyId);	
	}

	
	// CREATE EDITOR FOR JSON SCHEMA 
	var createEditor = function(){		
		logControl ? console.log('|--->   createEditor()') : '';
		var containerRest = document.getElementById('jsoneditorRest');	
		var options = {
			mode: 'text',
			theme: 'bootstrap3',
			required_by_default: true,
			modes: ['text', 'tree', 'view'], // allowed modes
			error: function (err) {
				toastr.error(messagesForms.operations.genOpError, err.toString());
				return false;
			},
			onChange: function(){
				
				console.log('se modifica el editor en modo:' + editor.mode + ' contenido: ' + editor.getText());
			}
		};		
		editorRest = new jsoneditor.JSONEditor(containerRest, options, {});	
	}
	
	
	// CHECK IF JSON STRING WHEN JSON PARSE IS OK OR NOT, IF THE JSON IS MALFORMED THE JSON.parse() is broken.
	var IsJsonString = function(str) {
		try {
			JSON.parse(str);
		}
		catch (e) {	return false; }
		
		return true;
	}
	
	
	// JSON SCHEMA VALIDATION PROCESS
	var validateJsonSchema = function(){
        logControl ? console.log('|--->   validateJsonSchema()') : ''; 
		
		if(IsJsonString(editor.getText())){
			
			var isValid = true;
		 
			// obtener esquemaOntologiaJson
			var ontologia = JSON.parse(editor.getText());
			
			if((ontologia.properties == undefined && ontologia.required == undefined)){
			
				toastr.error(messagesForms.operations.genOpError, ontologyCreateReg.validations.schemaprop);
				isValid = false;
				return isValid;
				
			}else if( ontologia.properties == undefined && (ontologia.additionalProperties == null || ontologia.additionalProperties == false)){
			
				toastr.error(messagesForms.operations.genOpError, ontologyCreateReg.validations.schemaprop);
				isValid = false;
				return isValid;
					
			}else{  
			
				// Situarse en elemento raiz  ontologia.properties (ontologia) o ontologia.datos.properties (datos)
				var nodo;
				
				if(jQuery.isEmptyObject(ontologia.properties)){
					 //esquema sin raiz
					 nodo=ontologia;
				}else{
					for (var property in ontologia.properties){
						
						var data = "";
						//Se comprueba si dispone de un elemento raiz
						if (ontologia.properties[property] && ontologia.properties[property].$ref){
						
							// Se accede al elemento raiz que referencia el obj
							var ref = ontologia.properties[property].$ref;
							ref = ref.substring(ref.indexOf("/")+1, ref.length);
							nodo = ontologia[ref];
							
						} else {
							//esquema sin raiz
							nodo = ontologia;
						}
					}
				}				
				// Plantilla EmptyBase: se permite crear/modificar si se cumple alguna de estas condiciones:
				//a.     Hay al menos un campo (requerido o no requerido)
				//b.     No hay ningún campo (requerido o no requerido) pero tenemos el AditionalProperties = true
				// Resto de casos: Con que haya al menos un campo (da igual que sea requerido o no requerido) o el AditionalProperties = true, se permite crear/actualizar el esquema de la ontología.
				
				// Nodo no tiene valor
				if( (nodo == undefined)){
					toastr.error(messagesForms.operations.genOpError, 'NO NODE!');
					isValid = false;
					return isValid;
					  
				// Propiedades no definida y additionarProperteis no esta informado a true     
				}else  if(  (nodo.properties ==undefined || jQuery.isEmptyObject(nodo.properties))  && (nodo.additionalProperties == null || nodo.additionalProperties == false)){
					
					toastr.error(messagesForms.operations.genOpError, ontologyCreateReg.validations.noproperties);
					isValid = false;
					return isValid;
				}				
				//Validaciones sobre propiedas y requeridos
				else if(nodo.required!=undefined && (nodo.additionalProperties == null || nodo.additionalProperties == false)) {

					var requiredData = nodo.required.length;
					
					// Si tiene elementos requeridos
					if (requiredData!=null && requiredData>0){
					
						if(nodo.properties!=null){
							 var propertiesNumber=0;
							 for(var propertyName in nodo.properties) {
								 propertiesNumber++;
							 }
							 if(propertiesNumber==0){
								toastr.error(messagesForms.operations.genOpSuccess,ontologyCreateReg.validations.schemanoprop);
								isValid = true;
							 }
						}
						else{
							toastr.error(messagesForms.operations.genOpSuccess,ontologyCreateReg.validations.noproperties);
							isValid = false;
							return isValid;
						}			
					}           
				}             
			}
		}
		else {
			// no schema no fun!
			isValid = false;
			toastr.error(messagesForms.operations.genOpSuccess,ontologyCreateReg.validations.noschema);
			return isValid;
			
		}	
		console.log('JSON SCHEMA VALIDATION: ' + isValid);
		return isValid;
	}	
	
	
	// VALIDATE TAGSINPUT
	var validateTagsInput = function(){		
		if ($('#metainf').val() === ''){
			$('#metainf').prev().addClass('tagsinput-has-error');
			$('#metainf').nextAll('span:first').removeClass('hide');
			return false;
		} else {
			return true;
		}
	}

	var showHideImageTableOntology = function (){
		if(typeof $('#ontology_autthorizations > tbody > tr').length =='undefined' || $('#ontology_autthorizations > tbody > tr').length == 0 || $('#ontology_autthorizations > tbody > tr > td > input')[0].value==''){
			$('#imageNoElementsOnTable').show();
		}else{
			$('#imageNoElementsOnTable').hide();
		}
	}
	

	// AJAX AUTHORIZATION FUNCTIONS
	var authorization = function(action,ontology,user,accesstype,authorization,btn){
		logControl ? console.log('|---> authorization()') : '';	
		var insertURL = '/controlpanel/ontologies/authorization';
		var updateURL = '/controlpanel/ontologies/authorization/update';
		var deleteURL = '/controlpanel/ontologies/authorization/delete';
		var response = {};
		
		if (action === 'insert'){
			console.log('    |---> Inserting... ' + insertURL);
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");			
			$.ajax({
				url:insertURL,
				headers: {
					[csrf_header]: csrf_value
			    },
				type:"POST",
				async: true,
				data: {"accesstype": accesstype, "ontology": ontology,"user": user},			 
				dataType:"json",
				success: function(response,status){							
					
					var propAuth = {"users":user,"accesstypes":accesstype,"id": response.id};
					authorizationsArr.push(propAuth);
					console.log('     |---> JSONtoTable: ' + authorizationsArr.length + ' data: ' + JSON.stringify(authorizationsArr));
					// store ids for after actions.	inside callback 				
					var user_id = user;
					var auth_id = response.id;
					var AuthId = {[user_id]:auth_id};
					authorizationsIds.push(AuthId);
					console.log('     |---> Auths: ' + authorizationsIds.length + ' data: ' + JSON.stringify(authorizationsIds));
										
					// TO-HTML
					if ($('#authorizations').attr('data-loaded') === 'true'){
						$('#ontology_autthorizations > tbody').html("");
						$('#ontology_autthorizations > tbody').append(mountableModel2);
					}
					console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
					$('#ontology_autthorizations').mounTable(authorizationsArr,{
						model: '.authorization-model',
						noDebug: false							
					});
					
					// hide info , disable user and show table
					$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));			
					$("#users").selectpicker('deselectAll');
					$("#users option[value=" + $('#users').val() + "]").prop('disabled', true);
					$("#users").selectpicker('refresh');
					$('#authorizations').removeClass('hide');
					$('#authorizations').attr('data-loaded',true);
					
					showHideImageTableOntology();
					
					toastr.success(messagesForms.operations.genOpSuccess,'');
				}
			});

	
		}
		if (action === 'update'){
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			$.ajax({url:updateURL, type:"POST", async: true, 
				headers: {
					[csrf_header]: csrf_value
			    },
				data: {"id": authorization, "accesstype": accesstype},			 
				dataType:"json",
				success: function(response,status){
							
					var updateIndex = foundIndex(user,'users',authorizationsArr);			
					authorizationsArr[updateIndex]["accesstypes"] = accesstype;
					console.log('ACTUALIZADO: ' + authorizationsArr[updateIndex]["accesstypes"]);
					
					// UPDATING STATUS...
					$(btn).find("i").removeClass('fa fa-spin fa-refresh').addClass('fa fa-edit');
					$(btn).find("span").text('Update');
					
					toastr.success(messagesForms.operations.genOpSuccess,'');
				}
			});
			
			
		}
		if (action  === 'delete'){
			console.log('    |---> Deleting... ' + user + ' with authId:' + authorization );
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			$.ajax({url:deleteURL, type:"POST", async: true, 
				headers: {
					[csrf_header]: csrf_value
			    },
				data: {"id": authorization},			 
				dataType:"json",
				success: function(response,status){									
					
					// remove object
					var removeIndex = authorizationsIds.map(function(item) { return item[user]; }).indexOf(authorization);			
					authorizationsIds.splice(removeIndex, 1);
					authorizationsArr.splice(removeIndex, 1);
					
					console.log('AuthorizationsIDs: ' + JSON.stringify(authorizationsIds));
					// refresh interface. TO-DO: EL this este fallará					
					if ( response  ){ 
						$(btn).closest('tr').remove();
						$("#users option[value=" + user + "]").prop('disabled', false);						
						$("#users").selectpicker('deselectAll');
						$("#users").selectpicker('refresh');
						if (authorizationsArr.length == 0){
							$('#alert-authorizations').toggle(!$('#alert-authorizations').is(':visible'));					
							$('#authorizations').addClass('hide');
							
						}
						showHideImageTableOntology();
						
						toastr.success(messagesForms.operations.genOpSuccess,'');
					}
					else{ 
						toastr.error(messagesForms.operations.genOpError,'NO RESPONSE!');
					}
				}
			});			
		}	
	};
	
	
	// return position to find authId.
	var foundIndex = function(what,item,arr){
		var found = '';
		arr.forEach(function(element, index, array) {
			if ( what === element[item]){ found = index;  console.log("a[" + index + "] = " + element[item] + ' Founded in position: ' + found ); } 
			
		});		
		return found;
	}
	
	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/ontologies/freeResource/" + id).done(
				function(data){
					console.log('freeResource() -> ok');
					navigateUrl(url); 
				}
			).fail(
				function(e){
					console.error("Error freeResource", e);
					navigateUrl(url); 
				}
			)		
	}
	
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{

		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return ontologyCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';				
			handleValidation();
		 
			createEditor();
			initTemplateElements();
			showHideImageTableOntology();
			$('#jsonschema').val(ontologyCreateJson.dataModels[0].jsonSchema);
			$('#datamodelid').val(ontologyCreateJson.dataModels[0].id);
			$("#rtdb").val("API_REST");
			
			
			// PROTOTYPEs
			// ARRAY PROTOTYPE FOR CHECK UNIQUE PROPERTIES.
			Array.prototype.unique = function() {
				return this.filter(function (value, index, self) { 
					return self.indexOf(value) === index;
				});
			};
			
			// ARRAY PROTROTYPE FOR REMOVE ELEMENT (not object) BY VALUE
			Array.prototype.remove = function() {
				var what, a = arguments, L = a.length, ax;				
				while (L && this.length) {
					what = a[--L];				
					while ((ax = this.indexOf(what)) !== -1) {
						console.log('AX: ' + ax);
						this.splice(ax, 1);
					}
				}
				return this;
			};
			
			
		},
		// UPDATE HIDDEN INPUT--> IF USER WANTS TO CHANGE ONTOLOGY TITLE FOR
		// EXAMPLE
		updateJsonschemaInput: function(){
			$('#jsonschema').val(editorRest.getText());
		},
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		cancel: function(id,url){
			logControl ? console.log(LIB_TITLE + ': cancel()') : '';
			
			freeResource(id,url);
		},
		
		// DELETE ONTOLOGY 
		deleteOntology: function(ontologyId){
			logControl ? console.log(LIB_TITLE + ': deleteOntology()') : '';	
			deleteOntologyConfirmation(ontologyId);			
		},
			
		// JSON SCHEMA VALIDATION
		validateJson: function(){	
			validateJsonSchema();			
		},
		
		// WIZARD SEQUENCING
		wizardContinue: function(){
			wizardStepContinue();
		},
		
		selectSwaggerDefinitionObject: function(){
			selectSwaggerDefinitionObject();
		},
		
		// INSERT AUTHORIZATION
		insertAuthorization: function(){
			logControl ? console.log(LIB_TITLE + ': insertAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){	
				// UPDATE MODE ONLY AND VALUES on user and accesstype
				if (($('#users').val() !== '') && ($("#users option:selected").attr('disabled') !== 'disabled') && ($('#accesstypes').val() !== '')){
					
					// AJAX INSERT (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns object with data.
					authorization('insert',ontologyCreateReg.ontologyId,$('#users').val(),$('#accesstypes').val(),'');
								
				} else {
					toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.authuser);
				}
			}
		},
		
		// REMOVE authorization
		removeAuthorization: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){
				
				// AJAX REMOVE (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns object with data.
				var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
				var selAccessType = $(obj).closest('tr').find("select[name='accesstypes\\[\\]']").val();				
				
				var removeIndex = foundIndex(selUser,'users',authorizationsArr);				
				var selAuthorizationId = authorizationsIds[removeIndex][selUser];
				
				console.log('removeAuthorization:' + selAuthorizationId);
				
				authorization('delete',ontologyCreateReg.ontologyId, selUser, selAccessType, selAuthorizationId, obj );				
			}
		},		
		// UPDATE authorization
		updateAuthorization: function(obj){
			logControl ? console.log(LIB_TITLE + ': updateAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){
				
				// AJAX UPDATE (ACTION,ONTOLOGYID,USER,ACCESSTYPE,ID) returns object with data.
				var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
				var selAccessType = $(obj).closest('tr').find("select[name='accesstypes\\[\\]']").val();
								
				var updateIndex = foundIndex(selUser,'users',authorizationsArr);				
				var selAuthorizationId = authorizationsIds[updateIndex][selUser];				
				
				console.log('updateAuthorization:' + selAuthorizationId);
				
				if (selAccessType !== authorizationsArr[updateIndex]["accesstypes"]){
					
					// UPDATING STATUS...
					$(obj).find("i").removeClass('fa fa-edit').addClass('fa fa-spin fa-refresh');
					$(obj).find("span").text('Updating...');
					
					authorization('update',ontologyCreateReg.ontologyId, selUser, selAccessType, selAuthorizationId, obj);
				} 
				else { console.log('no hay cambios');}
			}
		},
		
		removePathParam: function(obj){
			logControl ? console.log(LIB_TITLE + ': removePathParam()') : '';
			
			var rempath = $(obj).closest('tr').find("input[name='namesPath\\[\\]']").val();		
			$(obj).closest('tr').remove();
			var inputs = $("input[name='indexes\\[\\]']");
			$.each(inputs, function(i, item){
				inputs[i].value = i;
			});
		},
		removeQueryParam: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeQueryParam()') : '';
			
			var rempath = $(obj).closest('tr').find("input[name='namesQueries\\[\\]']").val();		
			$(obj).closest('tr').remove();
		},
		// CHECK FOR NON DUPLICATE Path Params name
		checkNamePaths: function(obj){
			logControl ? console.log(LIB_TITLE + ': checkNamePaths()') : '';
			var allProperties = $("input[name='namesPaths\\[\\]']").map(function(){return $(this).val();}).get();		
			areUnique = allProperties.unique();
			if (allProperties.length !== areUnique.length)  { 
				$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: ontologyCreateReg.validations.duplicates});
				$(obj).val(''); return false;
			} 
			else {
				$(obj).closest('tr').find('.btn-mountable-remove').attr('data-property', $(obj).val() );   
			}
		},
		// CHECK FOR NON DUPLICATE Query Params name
		checkNameQueries: function(obj){
			logControl ? console.log(LIB_TITLE + ': checkNameQueries()') : '';
			var allProperties = $("input[name='namesQueries\\[\\]']").map(function(){return $(this).val();}).get();		
			areUnique = allProperties.unique();
			if (allProperties.length !== areUnique.length)  { 
				toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.duplicates);
				$(obj).val(''); return false;
			} 
			else {
				$(obj).closest('tr').find('.btn-mountable-remove').attr('data-property', $(obj).val() );   
			}
		},
		showOperation: function(operation){
			
			$("#updateoperationBtn").attr("disabled", "disabled");
			$("#nameOperation").removeAttr("disabled");
			
			var pathParams = $.parseJSON($("#pathParams_" + operation).text());
			var queryParams = $.parseJSON($("#queryParams_" + operation).text());
			$("#nameOperation").val($("#" + operation).text());
			$("#pathOperation").val($("#path_" + operation).text());
			$("#descriptionOperation").val($("#des_" + operation).text());
			$("#typeOperation").val($("#type_" + operation).text());
			$("#defaultOperationType").val($("#default_operation_type_" + operation).text());
			
			$('#pathsParams > tbody').html("");
			$('#pathsParams > tbody').append(mountableModel3);
			
			$('#queriesParams > tbody').html("");
			$('#queriesParams > tbody').append(mountableModel4);
			
			$('#pathsParams').mounTable(pathParams,{
				model: '.mountable-model',
				noDebug: false,
				addLine:{				
					button: "#addPathParamOperation",					
					onClick: function (element){
						
						console.log('PathParam added!');
						return true;
					}
				}
					
			});
			
			$('#queriesParams').mounTable(queryParams,{
				model: '.mountable-model',
				noDebug: false,
				addLine:{				
					button: "#addQueryParamOperation",					
					onClick: function (element){
						
						console.log('PathParam added!');
						return true;
					}
				}
					
			});
		},
		editOperation: function(operation){
			
			OntologyCreateController.showOperation(operation);
			$("#nameOperation").attr("disabled", "disabled");
			$("#updateoperationBtn").removeAttr("disabled");
		},
		updateOperation : function(){
			
			var nameOperation = $("#nameOperation").val();
			//VALIDATIONS
			if(nameOperation=="" || nameOperation==null || nameOperation==undefined){
				toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.operation.name);
				return;
			}
			
			if($("#descriptionOperation").val()=="" || $("#descriptionOperation").val()==null || $("#descriptionOperation").val()==undefined){
				toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.operation.desc);
				return;
			}
			
			var indexes = $("input[name='indexes\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();				
			var namesPath = $("input[name='namesPaths\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();
			var fieldsPath = $("input[name='namesPaths\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();		
			
			var jsonPathParams = [];
			var notInPathDefinition = [];
			var path = $("#pathOperation").val();
			if ( namesPath.length ){	
				$.each(namesPath, function( index, value ) {
					nameIndex = namesPath.indexOf(value);
					jsonPathParams.push({'indexes': indexes[nameIndex], 'namesPaths':value, 'fieldsPaths':fieldsPath[nameIndex]});
					if(!path.includes('{'+value+'}') ) notInPathDefinition.push(value);
				});			
			}
			//check path params defined in the path
	        if(notInPathDefinition.length >0){
	        	toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.wrong.path.params +' '+notInPathDefinition.toString().replace(/,/g,', '));
	        	return false; 
	        }
			
			var namesQuery = $("input[name='namesQueries\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();
			var fieldsQuery = $("input[name='fieldsQueries\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();			
			
			var jsonQueryParams=[];
			var isFirst= true;
			var queryParamsCallExample = "?";
			if ( namesQuery.length ){	
				$.each(namesQuery, function( index, value ) {
					nameIndex = namesQuery.indexOf(value);
					jsonQueryParams.push({'namesQueries':value, 'fieldsQueries':fieldsQuery[index]});
					queryParamsCallExample += value + "=" + fieldsQuery[index] + "&";
				});			
			}
			queryParamsCallExample = path + queryParamsCallExample.substr(0,queryParamsCallExample.length-1);
			
			var numParams = jsonQueryParams.length + jsonPathParams.length;
			var defaultOperationType = $("#defaultOperationType").val();
			if ((defaultOperationType == 'GET_BY_ID' || defaultOperationType == 'DELETE_BY_ID' || defaultOperationType == 'UPDATE_BY_ID') && numParams != 1){
				//Operations need one parameter
				toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.operation.one.param);
				return false;
			}
			if ((defaultOperationType == 'GET_ALL' || defaultOperationType == 'INSERT' || defaultOperationType == 'DELETE_ALL') && numParams != 0) {
				//Operations must have no params
				toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.operation.no.params);
				return false;
			}
			
			//PERSIST CHANGES TO FORM
			
			$("#path_" + nameOperation).val($("#pathOperation").val());
			$("#path_params_" + nameOperation).val(queryParamsCallExample);
			$("#type_" + nameOperation).val($("#typeOperation").val());
			$("#default_operation_type_" + nameOperation).val($("#defaultOperationType").val());
			$("#des_" + nameOperation).val($("#descriptionOperation").val());
			$("#des_" + nameOperation).val($("#descriptionOperation").val());
			$("#path_" + nameOperation).text($("#pathOperation").val());
			$("#path_params_" + nameOperation).text(queryParamsCallExample);
			$("#type_" + nameOperation).text($("#typeOperation").val());
			$("#default_operation_type_" + nameOperation).text($("#defaultOperationType").val());
			$("#des_" + nameOperation).text($("#descriptionOperation").val());
			$("#des_" + nameOperation).text($("#descriptionOperation").val());
			
			$("#pathParams_" + nameOperation).val(JSON.stringify(jsonPathParams))
			$("#pathParams_" + nameOperation).text(JSON.stringify(jsonPathParams))
			
			$("#queryParams_" + nameOperation).val(JSON.stringify(jsonQueryParams))
			$("#queryParams_" + nameOperation).text(JSON.stringify(jsonQueryParams))
			
			$("#nameOperation").val("");
			$("#pathOperation").val("");
			$("#typeOperation").val("get");
			$("#defaultOperationType").val("NONE");
			$("#descriptionOperation").val("");
			$("#pathsParams tbody tr").remove();
			$("#queriesParams tbody tr").remove();
			
			$("#updateoperationBtn").attr("disabled", "disabled");
			$("#nameOperation").removeAttr("disabled");
		},
		deleteOperation: function(operation){
			
			$("#operation_"+operation).remove();
			var indexToRemove = -1;
			operationsNames.forEach(function(element, index){
				if(element == operation) indexToRemove = index;
			});
			if(indexToRemove >= 0)	operationsNames.splice(indexToRemove, 1);
			
			manageWizardStep();
		},
		editHeader: function(header){
			
			$("#headerKey").val(header);
			$("#headerValue").val($("#value_" + header).text());
			
		},
		deleteHeader: function(header){
			
			$("#header_"+header).remove();
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// GLOBAL JSON AND CODE EDITOR INSTANCES
	var editorRest;
	var aceEditor;
	var schema = ''; // current schema json string var
	
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	OntologyCreateController.load(ontologyCreateJson);	
		
	// AUTO INIT CONTROLLER.
	OntologyCreateController.init();
	
	handleTabsChange();
	
});
