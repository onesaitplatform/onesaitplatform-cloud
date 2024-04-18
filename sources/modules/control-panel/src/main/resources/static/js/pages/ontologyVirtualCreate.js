var authorizationsArr 		= []; // add authorizations
var authorizationUpdateArr  = []; // get authorizations of the ontology
var authorizationsIds 		= []; // get authorizations ids for actions
var authorizationObj 		= {}; // object to receive authorizations responses.
var createKeyColumns 		= []; // object to receive key fields to create table
var createColumns 			= []; // object to receive fields to create table
var createKeyConstrains 	= []; // object to receive key table constarins to create table
var createConstrains 		= []; // object to receive table constarins to create table
var creationConfig          = {}; // object to save request params corresponding to OntologyConfiguration class
var selectedDatasource      = undefined // object to save selected datasource
	
var OntologyCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Ontology Controller';	
    var logControl = 1;
	var LANGUAGE = ['es']
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var validTypes = ["object","string","number","integer","date","timestamp","array","geometry","file","boolean"]; // Valid property types	
	var validIdName = false;
	var validJsonSchema = false;
	var validMetaInf = false;
	var hasId = false; // instance

	
	// CONTROLLER PRIVATE FUNCTIONS	--------------------------------------------------------------------------------
	var getDataSourceFromServer = function() {
		// execute call to back for SQL code

		if ($("#datasources").val() === "" || $("#datasources").val() == undefined) {
			$.alert({title: 'ERROR '+ status + ': Not possible to get data surce from server!', theme: 'dark', type: 'red', content: "Datasource not defined"});
		}
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		$.ajax({
			url : "/controlpanel/ontologies/virtual/datasource/" + $("#datasources").val(),
			headers: {
				[csrf_header]: csrf_value
			},
			type : 'GET',
			dataType: 'text', 
			contentType: 'text/plain',
			mimeType: 'text/plain',
			success : function(data) {
				selectedDatasource = JSON.parse(data);
			},
			error : function(data, status, err) {
				$.alert({title: 'ERROR '+ status + ': '+err+'!', theme: 'dark', type: 'red', content: data.responseText});

			}, 
			complete: function (data, status){
			}
		});
	}
	
	// GENERARATE PROPERTY TYPES [GEOMETRY, OBJECT, ARRAY OR BASIC]
	var generateBasicType = function(propType, obj, parent, thevalue){
		logControl ? console.log('        |--->   generateBasicType()') : '';

    	// if enum, return enum value
    	if (thevalue != null && thevalue != ""){ return "\"" + thevalue + "\""; }
    	// string
        if (propType == "string") {
        	if 		(parent == "media" && obj == "storageArea")	  { return "\"SERIALIZED\""; }
			else if (parent == "media" && obj == "binaryEncoding"){ return "\"Base64\""; }
			else if (parent == "media" && obj == "name")		  {	return "\"fichero.pdf\""; }
			else if (parent == "media" && obj == "mime")		  {	return "\"application/pdf\""; }
			else{ return "\"string\""; }
        }
		// integer,boolean, object, number and geometry
		else if ( propType == "integer" ){ return "1" }
		else if ( propType == "boolean" ){ return "true"; }
		else if ( propType == "object" ){ return "{}"; }
		else if ( propType == "number"   || propType == "numeric" ){	return "28.6" }
		// Not supported for virtual ontologies
		//else if ( propType == "geometry-point" || obj =="geometry-point"){ return "{\"type\":\"Point\", \"coordinates\":[9,19.3]}"; }
		//else if ( propType == "geometry-linestring" || obj =="geometry-linestring"){ return "{\"type\":\"LineString\", \"coordinates\":[[9,19.3],[19,9.3]]}"; }
		//else if ( propType == "geometry-polygon" || obj =="geometry-polygon"){ return "{\"type\":\"Polygon\", \"coordinates\":[ [[9,-19.3],[-19,-9.3],[-9,19.3],[19,-9.3]] ,[[9,19.3],[19,9.3],[-9,19.3],[-19,9.3]] ]}"; }
		//else if ( propType == "geometry-multipoint" || obj =="geometry-multipoint"){ return "{\"type\":\"MultiPoint\", \"coordinates\":[[9,19.3],[19,19.3]]}"; }
		//else if ( propType == "geometry-multilinestring" || obj =="geometry-multilinestring"){ return "{\"type\":\"MultiLineString\", \"coordinates\":[ [[9,19.3],[19,9.3]], [[9,19.3],[19,9.3]] ]}"; }
		//else if ( propType == "geometry-multipolygon" || obj =="geometry-multipolygon"){ return "{\"type\":\"MultiPolygon\", \"coordinates\":[[[[9,2.5],[9,2],[-9,3],[9,-3],[-9,2]]],[[[9,0],[9,-0.5],[9,-1.5],[9,1],[9,0.4]],[[9,0.2],[-9,0.2],[9,0.8],[-9,0.8],[9,3.2]]]]}"; }
		
		//else if ( propType == "array") {return ""; }
    }
	
	var createJsonProperties = function (jsonData, constr){
		logControl ? console.log('|---  createJsonProperties()') : '';
		
		var properties 		= [];
		
		// Properties
		if ( jsonData.hasOwnProperty('datos') ){ properties = jsonData.datos.properties; } else { properties = jsonData.properties;  }
				
		// KEY and VALUE (value or object, or array...)
		var options = [];
		$.each( properties, function (key, object){			
			if (object){
				console.log('|--- Key: '+ key );
					
				var src = [{id: key, txt: key}];
				src.forEach(function (item) {
					var option = "<option value='"+item.txt+"'>" + item.txt + "</option>"
					options.push(option);
				});
			}
		});	
		
		if (constr == 1) {
			$('#constraintReferencedColumns').html(options);
			$('#constraintReferencedColumns').selectpicker('refresh');
			$("#constraintReferencedColumns").show();
		} else {
			$('#fields').html(options);
			$('#fields').selectpicker('refresh');
			$("#fields").show();
		}
		
		
	}
	
	var createJsonPropertiesGeometry = function (jsonData, constr){
		logControl ? console.log('|---  createJsonPropertiesGeometry()') : '';
		
		var properties 		= [];
		
		// Properties
		if ( jsonData.hasOwnProperty('datos') ){ properties = jsonData.datos.properties; } else { properties = jsonData.properties;  }
				
		// KEY and VALUE (value or object, or array...)
		var options = [];
		$.each( properties, function (key, object){			
			if (object && (object.type=="string" || object.type=="json")){
				console.log('|--- Key: '+ key );
					
				var src = [{id: key, txt: key}];
				src.forEach(function (item) {
					var option = "<option value='"+item.txt+"'>" + item.txt + "</option>"
					options.push(option);
				});
			}
		});	
		
		if (constr == 1) {
			$('#constraintReferencedColumns').html(options);
			$('#constraintReferencedColumns').selectpicker('refresh');
			$("#constraintReferencedColumns").show();
		} else {
			$('#fieldsGeometry').html(options);
			$('#fieldsGeometry').selectpicker('refresh');
			$("#fieldsGeometry").show();
		}
		
		
	}
	
	$("#datasources").on("change", function (){
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		
		$('#collections').prop('disabled', true);
		$('#collections').empty();
		$('#loading-collection').show();
		$('#collections').selectpicker('refresh');
		
		$('#identification').val('');
		editor.setText("{}");
		
		$("#id").prop('checked',false);
		$("#fields").attr('disabled','disabled');
		$("#fields").empty();
		$("#fields").selectpicker('deselectAll').selectpicker('refresh');
		
		$.ajax({
			url : "/controlpanel/ontologies/getTables/"+ $("#datasources").val(),
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'GET',
			dataType: 'text', 
			contentType: 'text/plain',
			mimeType: 'text/plain',
			success : function(data, _textStatus, xhr) {
				$('#collections').empty();
				if (xhr.status === 204) {
					var tables = [];
				} else {
					var tables = JSON.parse(data);
				}
				
				var count = tables.length;
				if(count > 0)
					$.each( tables, function (key, object){
						$('#collections').append("<option value='"+object+"' text='"+object+"' >"+object+"</option>");
						$('#tablesConstraint').append("<option value='"+object+"' text='"+object+"' >"+object+"</option>");
						if (!--count) {
							$('#collections').prop('disabled', false);
							$('#collections').selectpicker('refresh');
							$('#tablesConstraint').selectpicker('refresh');
						}
					});
				else {
					$.alert({title: 'INFO!', type: 'blue' , theme: 'dark', content: 'Database has not tables'});
					$('#collections').prop('disabled', true);
					$('#collections').selectpicker('refresh');
				}
				
				$("#datasource").val($("#datasources").val());
				
			},
			error : function(data, status, err) {
				$.alert({title: 'ERROR '+ status + ': '+err+'!', theme: 'dark', type: 'red', content: data.responseText});
				$('#collections').empty();
				$('#collections').prop('disabled', true);
				$('#collections').selectpicker('refresh');
			}, 
			complete: function (data, status){
				$('#loading-collection').hide();
			}
		});
		getDataSourceFromServer();
	});

	
	
	$("#generateSchema").on("click", function(){
		OntologyCreateController.generateSchema();
	});
	
//	$("#tab-esquema").on("click", function(){
//		if(JsonSchema.INPUT_VALUE==undefined){
//			$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: ontologyCreateReg.validations.schema}); 
//		}
//	});
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		logControl ? console.log('cleanFields() -> ') : '';
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		//CLEANING SELECTs
		$(".selectpicker").not('.no-remove').each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		//CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
		
		// CLEAN DATAMODEL TABLE
		$('#datamodel_properties').attr('data-loaded',false);
		$('#datamodel_properties > tbody').html("");
		$('#datamodel_properties > tbody').append(mountableModel);
		editor.setMode("text");
		editor.setText('{}');
		editor.setMode("tree");
		$('li.mt-list-item.datamodel-template').removeClass('bg-success done');
		$('.list-toggle-container').not('.collapsed').trigger('click');
		$('#template_schema').addClass('hide');
		
		// CLEAN SCHEMA DINAMIC TITLE FROM DATAMODEL SEL.
		$('#schema_title').empty();	
	}
	
	// VALIDATE TAGSINPUT
	var validateIdName = function(){
		var error1 = $('.alert-danger');
		error1.show();
		if ($('#identification').val().match(/^[0-9]/)) { $('#identificationerror').removeClass('hide').addClass('help-block-error font-red'); App.scrollTo(error1, -200);return false;  } else { return true;}
	}

	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#ontology_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');
		
					
		// set current language
		currentLanguage = ontologyCreateReg.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// custom messages
            messages: {	
				
			},
			// validation rules
            rules: {
				ontologyId:		{ minlength: 5, required: true },
                identification:	{ required: true },
				description:	{ minlength: 5, required: true }
            },
            invalidHandler: function(event, validator) { //display error alert on form submit              
                success1.hide();
                error1.show();
                App.scrollTo(error1, -200);
            },
            errorPlacement: function(error, element) {				
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }				
				else { error.insertAfter(element); }
            },
            highlight: function(element) { // hightlight error inputs
                $(element).closest('.form-group').addClass('has-error'); 
            },
            unhighlight: function(element) { // revert the change done by hightlight
                $(element).closest('.form-group').removeClass('has-error');
            },
            success: function(label) {
                label.closest('.form-group').removeClass('has-error');
            },
			// ALL OK, THEN SUBMIT.
            submitHandler: function(form) {
                error1.hide();
                // VALIDATE IDENTIFICATION
				validIdName = validateIdName();
				if (validIdName){
					// VALIDATE JSON SCHEMA 
					validJsonSchema = validateJsonSchema();				
					if (validJsonSchema){
						if($("#id").is(":checked")){
							$("#objectId").val($("#fields").val());
						}
						if($("#geometrycheck").is(":checked")){
							$("#objectGeometry").val($("#fieldsGeometry").val());
						}
						
						$.each(createColumns, function(k,v){
							if(v.type == "geometry"){
								$("#objectGeometry").val(v.name);
							}
						})
						
						if ($("#allowsCreateTable").is(':checked')) {
							$("#datasourceTableName").val($("#identification").val());
						}
							
						// VALIDATE TAGSINPUT
						validMetaInf = validateTagsInput();
						if (validMetaInf) {
							//form.submit();
							form1.ajaxSubmit({type: 'post', success : function(data){
									navigateUrl(data.redirect);
								}, error: function(data){
									HeaderController.showErrorDialog(data.responseJSON.cause)
								}
							})
						}
					}else {
						success1.hide();
						error1.show();										
					}
				}
				else {
					success1.hide();
					error1.show();										
				}
				
			}
        });
    }
	
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() ->  resetForm,  currentLanguage: ' + currentLanguage) : '';
		
		// INPUT MASK FOR ontology identification allow only letters, numbers
		// and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_]*", greedy: false });

		
		// tagsinput validate fix when handleValidation()
		$('#metainf').on('itemAdded', function(event) {
			
			if ($(this).val() !== ''){ $('#metainferror').addClass('hide');}
		});
		
		// authorization tab control 
		$(".nav-tabs a[href='#tab_2']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			$.alert({title: 'INFO!', type: 'blue' , theme: 'dark', content: ontologyCreateReg.validations.authinsert});
			return false;
		  }
		});
				
		
		// 	INPUT MASK FOR ontology identification allow only letters, numbers and -_
//		$("#identification").inputmask({ regex: "[a-zA-Z0-9_]*", greedy: false });
		
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('ontology_create_form');
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
			
		}
		// EDIT MODE ACTION 
		else {	
			logControl ? console.log('|---> Action-mode: UPDATE') : '';
			
			$("#datasources").val(ontologyCreateReg.datasource);
			$("#collections").val(ontologyCreateReg.collection);
			
			OntologyCreateController.changeCollection(0);
			OntologyCreateController.generateSchema();
			
			if(ontologyCreateReg.objectId != null && ontologyCreateReg.objectId != undefined && ontologyCreateReg.objectId != ""){
				
				$("#id").attr("checked", "checked");
				OntologyCreateController.schemaToTable();
				$("#fields").selectpicker('val', ontologyCreateReg.objectId);
			}
			
			if(ontologyCreateReg.objectGeometry != null && ontologyCreateReg.objectGeometry != undefined && ontologyCreateReg.objectGeometry != ""){
				
				$("#geometrycheck").attr("checked", "checked");
				OntologyCreateController.schemaToTableGeometry();
				$("#fieldsGeometry").selectpicker('val', ontologyCreateReg.objectGeometry);
			}
			
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
				
			}		
			
			// take schema from ontology and load it
			schema = ontologyCreateReg.schemaEditMode;			
			
		}		
	}	

	
	// DELETE ONTOLOGY
	var deleteOntologyConfirmation = function(ontologyId){
		console.log('deleteOntologyConfirmation() -> formId: '+ ontologyId);
		
		// no Id no fun!
		if ( !ontologyId ) {$.alert({title: 'ERROR!', type: 'red' , theme: 'dark', content: ontologyCreateReg.validations.validform}); return false; }
		
		logControl ? console.log('deleteOntologyConfirmation() -> formAction: ' + $('.delete-ontology').attr('action') + ' ID: ' + $('#delete-ontologyId').attr('ontologyId')) : '';
		
		// call ontology Confirm at header. 
		HeaderController.showConfirmDialogOntologia('delete_ontology_form');	
	}

	
	// CREATE EDITOR FOR JSON SCHEMA 
	var createEditor = function(){		
		logControl ? console.log('|--->   createEditor()') : '';
		var container = document.getElementById('jsoneditor');
//		var containerInstance = document.getElementById('jsoneditorInstance');
		var options = {
			mode: 'code',
			theme: 'bootstrap3',
			required_by_default: true,
			modes: ['tree', 'view'], // allowed modes
			error: function (err) {
				$.alert({title: 'ERROR!', theme: 'dark', style: 'red', content: err.toString()});
				return false;
			},
			onChange: function(){
				
				console.log('se modifica el editor en modo:' + editor.mode + ' contenido: ' + editor.getText());
			}
		};		
		editor = new jsoneditor.JSONEditor(container, options, {});		
//		editorInstance = new jsoneditor.JSONEditor(containerInstance, options, {});
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
			
				$.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'dark', content: ontologyCreateReg.validations.schemaprop});
				isValid = false;
				return isValid;
				
			}else if( ontologia.properties == undefined && (ontologia.additionalProperties == null || ontologia.additionalProperties == false)){
			
				$.alert({title: 'ERROR JSON SCHEMA!', type: 'red' , theme: 'dark', content: ontologyCreateReg.validations.schemanoprop});
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
					   
					 $.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'dark', content: 'NO NODE!'});
					  isValid = false;
					  return isValid;
					  
				// Propiedades no definida y additionarProperteis no esta informado a true     
				}else  if(  (nodo.properties ==undefined || jQuery.isEmptyObject(nodo.properties))  && (nodo.additionalProperties == null || nodo.additionalProperties == false)){
					
					$.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'dark', content: ontologyCreateReg.validations.noproperties});
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
									$.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'dark', content: ontologyCreateReg.validations.schemanoprop});
									isValid = true;
								 }
						}
						else{
							$.alert({title: 'JSON SCHEMA !', type: 'red' , theme: 'dark', content: ontologyCreateReg.validations.noproperties});
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
			$.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'dark', content: ontologyCreateReg.validations.noschema});			
			return isValid;
			
		}	
		console.log('JSON SCHEMA VALIDATION: ' + isValid);
		return isValid;
	}	
	
	
	// VALIDATE TAGSINPUT
	var validateTagsInput = function(){		
		if ($('#metainf').val() === '') { $('#metainferror').removeClass('hide').addClass('help-block-error font-red'); return false;  } else { return true;} 
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
						
					}
					else{ 
						$.alert({title: 'ALERT!', theme: 'dark', type: 'orange', content: 'NO RESPONSE!'}); 
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
			this.loadCodeMirror();
			
			$('#jsonschema').val(ontologyCreateJson.dataModels[0].jsonSchema);
			$('#datamodelid').val(ontologyCreateJson.dataModels[0].id);
			$("#rtdb").val("VIRTUAL");
			
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
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		// DELETE ONTOLOGY 
		deleteOntology: function(ontologyId){
			logControl ? console.log(LIB_TITLE + ': deleteOntology()') : '';	
			deleteOntologyConfirmation(ontologyId);			
		},
		
		
		// UPDATE HIDDEN INPUT--> IF USER WANTS TO CHANGE ONTOLOGY TITLE FOR EXAMPLE
		updateJsonschemaInput: function(){	
			$('#jsonschema').val(editor.getText());
		},		
		
		// JSON SCHEMA VALIDATION
		validateJson: function(){	
			validateJsonSchema();			
		},
		
		// INSERT AUTHORIZATION
		insertAuthorization: function(){
			logControl ? console.log(LIB_TITLE + ': insertAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){	
				// UPDATE MODE ONLY AND VALUES on user and accesstype
				if (($('#users').val() !== '') && ($("#users option:selected").attr('disabled') !== 'disabled') && ($('#accesstypes').val() !== '')){
					
					// AJAX INSERT (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns object with data.
					authorization('insert',ontologyCreateReg.ontologyId,$('#users').val(),$('#accesstypes').val(),'');
								
				} else {  $.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: ontologyCreateReg.validations.authuser}); }
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
		// GENERATE DUMMY ONTOLOGY INSTANCES
		setRtdbDatasource: function(){
			$('#rtdb').val($('#rtdbInstance').val());
		},
		// GENERATE DUMMY ONTOLOGY INSTANCES
		setRtdbCleanLapse: function(){
			var optSelected = $('#rtdbCleanLapseOpt').val();
			$('#rtdbCleanLapse').val(optSelected);
			if(optSelected != 'Never'){
				$('#rtdbClean').val(true);
			}else{
				$('#rtdbClean').val(false);
			}
				
		},
		
	    changeCollection: function(n, constr) {
			
			if (constr == 1) {
				var collection = $("#tablesConstraint").val();
			} else {
				var collection = $("#collections").val();
			}

			if (/\s/.test(collection)) {
	            $.alert({title: 'Table not valid!', type: 'red' , theme: 'dark', content: "Whitespaces not allowed in ontology name"});			
	            $("#collections").val(undefined);
	            return;
	        }
	        
	        var datasource = $("#datasources").val();
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			
			ontologyCreateJson.datasources.forEach(function logArrayElements(virtualdatasource) {
				 if (virtualdatasource.identification==datasource){
					 if (virtualdatasource.domain!=null && virtualdatasource.domain!=""){
				        $("#identification").val(virtualdatasource.domain + "_" + collection);				       
				        $("#identification").text(virtualdatasource.domain + "_" +collection);
					 } else {
					    $("#identification").val(collection);				       
					    $("#identification").text(collection);
					 }
				 }
			});
			
			$("#datasourceTableName").val(collection);
			

			if(n==1)
			{
				$("#id").prop('checked',false);
				$("#fields").attr('disabled','disabled');
				$("#fields").empty();
				$("#fields").selectpicker('deselectAll').selectpicker('refresh');
			}
			
	        $.ajax({
				url : "/controlpanel/ontologies/getRelationalSchema/"+ datasource +"/"+ collection,
				headers: {
					[csrf_header]: csrf_value
			    },
				type : 'GET',
				dataType: 'text', 
				contentType: 'text/plain',
				mimeType: 'text/plain',
				async: false,
				success : function(data) {
					JsonSchema.INPUT_VALUE = JSON.stringify(JSON.parse(data));
					genSchema(constr);
				},
				error : function(data, status, er) {
					$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: er}); 
				}
			});
	        
	       	function genSchema(constr) {
		    	event.preventDefault();
		    	if(JsonSchema.INPUT_VALUE==undefined){
					$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: ontologyCreateReg.validations.schema}); 
					return;
				}
		    	if (document.getElementById("instancia").value==""){
			        JsonSchema.RESOLVE_REFS = false;
			        JsonSchema.MERGE_EXTS = false;
			        JsonSchema.INCLUDE_DEFS = false;
			        JsonSchema.INPUT_MODE = false;
			        JsonSchema.VERBOSE = false;
			
			        var rootSchema = JsonSchema.GenerateSchema();
			
			        this.rootSchemaPair = new SchemaPair({
			          schema: rootSchema,
			          root: true
			        });
			
			        this.rootSchemaPair.constructId();
			       
			        if (!this.rootSchemaPair) {
			           return;
			        }
			
			        var spStaticV = new SchemaPairStrV({
			           model: this.rootSchemaPair
			        });
			        
			        if (!constr == 1) {
						editor.setText("{}");
						editor.setMode("view");
						editor.setText(spStaticV.render());
			        
						$("#jsonschema").val(spStaticV.render());
					}
			        
			     // CHANGE TO SCHEMA TAB.
//					 $('.nav-tabs li a[href="#tab_4"]').tab('show');
		    	} else {
		    		$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Error!"}); 
		    	}
			}
			
			if (constr == 1) {
				OntologyCreateController.schemaToTable(constr);
			}
	        
	        
		},
		
		setRequiredFields: function() {
			var sch = JSON.parse(editor.getText());
			var reloadEditor = false;
			if (!$("#allowsCreateTable").is(':checked')) {
				return;
			}
			
			if (createColumns.length > 0 && sch.hasOwnProperty("properties")) {
				var index = 0;
				while (index < createColumns.length) {
					var name = createColumns[index]['name'];
					var notNull = createColumns[index]['notNull'];
					if (notNull === "true" && sch['properties'].hasOwnProperty(name)) {
                        sch['properties'][name]["required"] = true;
                        reloadEditor = true;
						}
				
					index++;
				}

                if (sch.hasOwnProperty("properties")) {
                	var props = sch['properties']
                	
                }

				if (reloadEditor) {
					editor.setText(JSON.stringify(sch));
				}
			}
		},
		
	    generateSchema: function() {
	    	event.preventDefault();
	    	if(JsonSchema.INPUT_VALUE==undefined){
				$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: ontologyCreateReg.validations.schema}); 
				return;
			}
	    	if (document.getElementById("instancia").value==""){
		        JsonSchema.RESOLVE_REFS = false;
		        JsonSchema.MERGE_EXTS = false;
		        JsonSchema.INCLUDE_DEFS = false;
		        JsonSchema.INPUT_MODE = false;
		        JsonSchema.VERBOSE = false;
		
		        var rootSchema = JsonSchema.GenerateSchema();
		
		        this.rootSchemaPair = new SchemaPair({
		          schema: rootSchema,
		          root: true
		        });
		
		        this.rootSchemaPair.constructId();
		       
		        if (!this.rootSchemaPair) {
		           return;
		        }
		
		        var spStaticV = new SchemaPairStrV({
		           model: this.rootSchemaPair
		        });
		        if(identification.value !== "") {
			        	spStaticV.model.attributes.schema.attributes.schemaid = identification.value;
			     }
		        
		        editor.setText("{}");
		        editor.setMode("tree");
		        editor.setText(spStaticV.render());
		        this.setRequiredFields();
		        $("#jsonschema").val(editor.getText());
		        
		     // CHANGE TO SCHEMA TAB.
				 $('.nav-tabs li a[href="#tab_4"]').tab('show');
	    	} else {
	    		$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Error!"}); 
	    	}
		},
		
		schemaToTableGeometry: function(constr){
			if($("#geometrycheck").is(":checked") || constr == 1){
				logControl ? console.log(LIB_TITLE + ': schemaToTableGeometry()') : '';
				
				$("#fieldsGeometry").removeAttr("disabled");
				
				if(JsonSchema.INPUT_VALUE==undefined){
					$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: ontologyCreateReg.validations.schema}); 
					$("#geometrycheck").removeAttr("checked");
					return;
				}
		    	if (document.getElementById("instancia").value==""){
			        JsonSchema.RESOLVE_REFS = false;
			        JsonSchema.MERGE_EXTS = false;
			        JsonSchema.INCLUDE_DEFS = false;
			        JsonSchema.INPUT_MODE = false;
			        JsonSchema.VERBOSE = false;
			
			        var rootSchema = JsonSchema.GenerateSchema();
			
			        this.rootSchemaPair = new SchemaPair({
			          schema: rootSchema,
			          root: true
			        });
			
			        this.rootSchemaPair.constructId();
			       
			        if (!this.rootSchemaPair) {
			        	$("#geometrycheck").removeAttr("checked");
			           return;
			        }
			
			        var spStaticV = new SchemaPairStrV({
			           model: this.rootSchemaPair
			        });
			        if(identification.value !== "") {
			        	spStaticV.model.attributes.schema.attributes.schemaid = identification.value;
			        }
					
					var data, properties, jsonProperties = '';
						
					// JSON-STRING SCHEMA TO JSON 
					schema = spStaticV.render()
					if 		(typeof schema == 'string'){ data = JSON.parse(schema); }
					else if (typeof schema == 'object'){ data = schema; } else { $.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: ontologyCreateReg.validations.noschemaontologyCreateReg.validations.noschema}); $("#id").removeAttr("checked"); return false; }
					
					// needs Ontology name and description to Run.
					if (($('#identification').val() == '') || ($('#description').val() == '')){
						$.alert({title: ontologyCreateReg.datamodel, theme: 'dark', type: 'orange', content: ontologyCreateReg.dataModelSelection}); $("#geometrycheck").removeAttr("checked"); return false;  
					} 
					else {
						// adding title and description
						// ADD TITLE
						data["title"] = $('#identification').val();
					
						// ADD DESCRIPTION
						if (!data.hasOwnProperty('description')){ data["description"] = $('#description').val(); }
						
						// UDATING SCHEMA STRING
						schema = JSON.stringify(data);
					}
						
					// SCHEMA MODEL ( PROPERTIES / DATOS) 
					if ( data.hasOwnProperty('datos') ){ properties = data.datos; } else { properties = data;  }
					
					// CREATING TABLE FROM DATA.		
					createJsonPropertiesGeometry(properties, constr);
		    	} else {
		    		$("#geometrycheck").removeAttr("checked");
		    		$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Error!"}); 
		    	}
			}else{
				$("#fieldsGeometry").attr('disabled','disabled');
				$("#fieldsGeometry").each(function(){
					$(this).val( '' );
					$(this).selectpicker('deselectAll').selectpicker('refresh');
				});
			}
			
	
		},
		
		schemaToTable: function(constr){
			if($("#id").is(":checked") || constr == 1){
				logControl ? console.log(LIB_TITLE + ': schemaToTable()') : '';
				
				$("#fields").removeAttr("disabled");
				
				if(JsonSchema.INPUT_VALUE==undefined){
					$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: ontologyCreateReg.validations.schema}); 
					$("#id").removeAttr("checked");
					return;
				}
		    	if (document.getElementById("instancia").value==""){
			        JsonSchema.RESOLVE_REFS = false;
			        JsonSchema.MERGE_EXTS = false;
			        JsonSchema.INCLUDE_DEFS = false;
			        JsonSchema.INPUT_MODE = false;
			        JsonSchema.VERBOSE = false;
			
			        var rootSchema = JsonSchema.GenerateSchema();
			
			        this.rootSchemaPair = new SchemaPair({
			          schema: rootSchema,
			          root: true
			        });
			
			        this.rootSchemaPair.constructId();
			       
			        if (!this.rootSchemaPair) {
			        	$("#id").removeAttr("checked");
			           return;
			        }
			
			        var spStaticV = new SchemaPairStrV({
			           model: this.rootSchemaPair
			        });
			        if(identification.value !== "") {
			        	spStaticV.model.attributes.schema.attributes.schemaid = identification.value;
			        }
					
					var data, properties, jsonProperties = '';
						
					// JSON-STRING SCHEMA TO JSON 
					schema = spStaticV.render()
					if 		(typeof schema == 'string'){ data = JSON.parse(schema); }
					else if (typeof schema == 'object'){ data = schema; } else { $.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: ontologyCreateReg.validations.noschemaontologyCreateReg.validations.noschema}); $("#id").removeAttr("checked"); return false; }
					
					// needs Ontology name and description to Run.
					if (($('#identification').val() == '') || ($('#description').val() == '')){
						$.alert({title: ontologyCreateReg.datamodel, theme: 'dark', type: 'orange', content: ontologyCreateReg.dataModelSelection}); $("#id").removeAttr("checked"); return false;  
					} 
					else {
						// adding title and description
						// ADD TITLE
						data["title"] = $('#identification').val();
					
						// ADD DESCRIPTION
						if (!data.hasOwnProperty('description')){ data["description"] = $('#description').val(); }
						
						// UDATING SCHEMA STRING
						schema = JSON.stringify(data);
					}
						
					// SCHEMA MODEL ( PROPERTIES / DATOS) 
					if ( data.hasOwnProperty('datos') ){ properties = data.datos; } else { properties = data;  }
					
					// CREATING TABLE FROM DATA.		
					createJsonProperties(properties, constr);
		    	} else {
		    		$("#id").removeAttr("checked");
		    		$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Error!"}); 
		    	}
			}else{
				$("#fields").attr('disabled','disabled');
				$("#fields").each(function(){
					$(this).val( '' );
					$(this).selectpicker('deselectAll').selectpicker('refresh');
				});
			}
			
	
		},

		getSelectedFieldColumn: function() {
			var nameSelected = $("#fieldName").val();
			var typeSelected = $("#fieldType option:selected").val();
			var notNullSelected = $("#fieldNotNull")[0].checked;
			var fieldAutoIncrSelected = $("#fieldAutoInc")[0].checked;
			var defaultValueSelected = $("#fieldDefaultValue").val();
			var descriptionSelected = $("#fieldDescription").val();
			if (nameSelected === "" || typeSelected=== "") {
				$.alert({
					title : 'ERROR!',
					type : 'red',
					theme : 'light',
					content : 'Fields must be filled correctly'
				});
				return false;
			}
			
			if (typeSelected !== "integer") {
				fieldAutoIncrSelected = false;
			}
			
			if (descriptionSelected === "") {
				descriptionSelected = null;
			}
			
			var column = {
				'name': nameSelected,
				'type': typeSelected,
				'notNull': notNullSelected,
				"autoIncrement": fieldAutoIncrSelected,
				"defautlValue": defaultValueSelected,
				'colComment': descriptionSelected
			};

			return column;
		},

		getSelectedConstraint: function() {
			var nameConstraintSelected = $("#constraintName").val();
			var typeConstraintSelected = $("#constraintType option:selected").val();
			var columnConstraintSelected = $("#constraintCol option:selected").val(); // TODO: Several columns in the constraint
			var referencedTableConstraintSelected = $("#tablesConstraint option:selected").val();
			var referencedColumnConstraintSelected = $("#constraintReferencedColumns option:selected").val();
	
			if (nameConstraintSelected === "" || typeConstraintSelected=== "" || columnConstraintSelected=== ""
				|| (typeConstraintSelected == "FOREIGN_KEY" && (
						referencedTableConstraintSelected === "" || referencedColumnConstraintSelected=== "")
					) 
				) {
				$.alert({
					title : 'ERROR!',
					type : 'red',
					theme : 'light',
					content : 'Constraints must be filled correctly'
				});
				return false;
			}

			if (typeConstraintSelected !== "FOREIGN_KEY") {
				referencedTableConstraintSelected = null;
				referencedColumnConstraintSelected = null;
			}
			
			var constraint = {
				'name': nameConstraintSelected,
				'type': typeConstraintSelected,
				'columns': [columnConstraintSelected],
				"referencedTable": referencedTableConstraintSelected,
				"referencedColumn": referencedColumnConstraintSelected
			};

			return constraint;
		},
		
		clearFieldSelected: function() {
			$('#fieldName').val( '' );
			$('#fieldName').selectpicker('deselectAll').selectpicker('refresh');
			$('#fieldType').val( '' );
			$('#fieldType').selectpicker('deselectAll').selectpicker('refresh');
			$('#fieldNotNull')[0].checked = false;
			$('#fieldAutoInc')[0].checked = false;
			$('#fieldDefaultValue').val( '' );
			$('#fieldDefaultValue').selectpicker('deselectAll').selectpicker('refresh');
			$('#fieldDescription').val( '' );
			$('#fieldDescription').selectpicker('deselectAll').selectpicker('refresh');
			
		},

		clearConstraintSelected: function() {
			$('#constraintName').val( '' );
			$('#constraintName').selectpicker('deselectAll').selectpicker('refresh');
			$('#constraintType').val( '' );
			$('#constraintType').selectpicker('deselectAll').selectpicker('refresh');
			$('#constraintCol').val( '' );
			$('#constraintCol').selectpicker('deselectAll').selectpicker('refresh');
			$('#tablesConstraint').val( '' );
			$('#tablesConstraint').selectpicker('deselectAll').selectpicker('refresh');
			$('#constraintReferencedColumns').val( '' );
			$('#constraintReferencedColumns').selectpicker('deselectAll').selectpicker('refresh');
			
		},
		
		addFieldRow: function() {
		
			if (identification.value===""){
				$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Must set ontology name before"}); 
				return;
			}

			var column = this.getSelectedFieldColumn();
			if (column === false) {
				return;
			}
			
			createColumns.push(column);
			createKeyColumns.push(column['name']);
			var checkUnique = createKeyColumns.unique();
			if (createKeyColumns.length !== checkUnique.length)  {
				createKeyColumns.pop(); 
				createColumns.pop(); 
				$.alert({
					title: 'ERROR!', 
					theme: 'light', 
					type: 'red', 
					content: 'Column name must be unique'
				}
				); return false; 
			}
			
			$('#field_properties > tbody').append(
							'<tr id="field_'+column['name']+' class="tagRow">'
							+'<td>'+ column['name'] + '</td>'
							+'<td>'+ column['type'] + '</td>'
							+'<td>'+ column['notNull'] + '</td>'
							+'<td>'+ column['autoIncrement'] + '</td>'
							+'<td>'+ column['colComment'] + '</td>'
							+ '<td class="text-center"><button type="button" data-property="" class="btn btn-sm btn-circle btn-outline blue" onclick="OntologyCreateController.removeFieldRow(this)" th:text="#{gen.deleteBtn}"><span th:text="#{gen.deleteBtn}"> Delete </span></button></td></tr>'
							);

			$('#constraintCol').append(
				'<option id="'+column['name']+'_colCons" value="'+column['name']+'">'+column['name']+'</option>');
			$('#constraintCol').selectpicker('refresh')
			this.clearFieldSelected();
			console.log('columns -->' + createKeyColumns);	
			this.generateFieldSchema();
		},

		addConstraintRow: function() {
		
			if (identification.value===""){
				$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Must set ontology name before"}); 
				return;
			}

			var constrint = this.getSelectedConstraint();
			if (constrint === false) {
				return;
			}

			constrint["columns"].forEach( function (colName) {
				if (!createKeyColumns.includes(colName)) {
					return;
				}
			});
						
			createConstrains.push(constrint);
			createKeyConstrains.push(constrint['name']);
			var checkUnique = createKeyConstrains.unique();
			if (createKeyConstrains.length !== checkUnique.length)  {
				createKeyConstrains.pop(); 
				createConstrains.pop(); 
				$.alert({
					title: 'ERROR!', 
					theme: 'light', 
					type: 'red', 
					content: 'Constraint name must be unique'
				}
				); return false; 
			}
			
			$('#constraint_properties > tbody').append(
							'<tr id="constr_'+constrint['name']+' class="tagRow">'
							+'<td>'+ constrint['name'] + '</td>'
							+'<td>'+ constrint['type'] + '</td>'
							+'<td>'+ constrint['columns'][0] + '</td>'
							+'<td>'+ constrint['referencedTable'] + '</td>'
							+'<td>'+ constrint['referencedColumn'] + '</td>'
							+ '<td class="text-center"><button type="button" data-property="" class="btn btn-sm btn-circle btn-outline blue" onclick="OntologyCreateController.removeConstraintRow(this)" th:text="#{gen.deleteBtn}"><span th:text="#{gen.deleteBtn}"> Delete </span></button></td></tr>'
							);

			this.clearConstraintSelected();
			console.log('constraints -->' + createKeyConstrains);	
		},

		removeFieldRowFromFieldName: function(fieldSelected) {
			createConstrains.forEach( function(constr) {
				var constrColumns = constr["columns"]
				constrColumns.forEach( function(constrCol) {
					if (constrCol == field) {
						$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Field referenced in constraint"}); 
						return;
					}
				})
			})
			$("#field_"+fieldSelected).remove();
			$("#"+fieldSelected+"_colCons").remove();
			$('#constraintCol').selectpicker('refresh')
			
			var i = createKeyColumns.indexOf(fieldSelected);
			createKeyColumns.splice(i, 1);
			createColumns.splice(i, 1);
			console.log('columns --> ' + createKeyColumns);	
		},

		removeConstraintRowFromConstraintName: function(constraintSelected) {
			$("#constr_"+constraintSelected).remove();
			
			var i = createKeyConstrains.indexOf(constraintSelected);
			createKeyConstrains.splice(i, 1);
			createConstrains.splice(i, 1);
			console.log('constraints --> ' + createKeyConstrains);	
		},

		removeFieldRow: function(field) {
			var fieldSelected = field.parentElement.parentElement.firstElementChild.innerHTML;
			this.removeFieldRowFromFieldName(fieldSelected);
			field.parentElement.parentElement.remove(); 
			this.generateFieldSchema();
		},

		removeConstraintRow: function(constraint) {
			var constraintSelected = constraint.parentElement.parentElement.firstElementChild.innerHTML;
			this.removeConstraintRowFromConstraintName(constraintSelected);
			constraint.parentElement.parentElement.remove();
		},

		clearFieldsRow: function() {
			for (var fieldName in createKeyColumns) {
				this.removeFieldRowFromFieldName(fieldName);
			};
		},

		generateFieldSchema: function() {
			var fieldsSchema = {}
			if (createColumns.length > 0) {
				var index = 0;
				while (index < createColumns.length) {
					var name = createColumns[index]['name'];
					var type = createColumns[index]['type'];
					if (type === "number") {type = 1.1;}
					else if (type === "integer") {type = 1;}

				  	fieldsSchema[name] = type;
					index++;
				}
				
				JsonSchema.INPUT_VALUE = JSON.stringify(fieldsSchema);
				this.generateSchema();
				
			} else {
				editor.setText("{}");
			}
		},

		toggleFieldCreation: function() {
			if(datasources.value===""){
				allowsCreateTable.checked = false;
				$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Must select JDBC connection before"}); 
				return;
			}
			editor.setText("{}");
			if ($("#allowsCreateTable").is(':checked')) {
				$("#datasources").prop('disabled', true);
				$('#fieldCreationForm').show();
				$('#constraintsCreationForm').show();
				$('#selectTable').hide();
				$('#asociateIdTableCheck').hide();
				$('#asociateIdTable').hide();
				$('#asociateGeometryTableCheck').hide();
				$('#asociateGeometryTable').hide();
				$('#fieldsFromExistingTable').hide();
				$('#sqlEditorRow').show();
				creationConfig['allowsCreationTable'] = true;
				identification.readOnly = false;
				identification.value = "";
				this.generateFieldSchema();
				
			} else {
				$("#datasources").prop('disabled', false);
				$('#fieldCreationForm').hide();
				$('#constraintsCreationForm').hide();
				$('#selectTable').show();
				$('#asociateIdTableCheck').show();
				$('#asociateIdTable').show();
				$('#asociateGeometryTableCheck').show();
				$('#asociateGeometryTable').show();
				$('#fieldsFromExistingTable').show();
				$('#sqlEditorRow').hide();
				sqlEditorRow.readOnly = true;
				identification.value = "";
				identification.readOnly = true;
				if (collections.value ==="") {
					identification.value = "";
				} else {
					identification.value = collections.value;
					this.changeCollection();
				}
				creationConfig['allowsCreationTable'] = false;
				
			}
			
		},

		toggleConstraintType: function() {
			if(constraintName.value==="" || constraintType.value===""){
				$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Must set constraint name and type before"}); 
				constraintType.value = "";
				$('#constraintReferencedTableDiv').hide();
				$('#constraintReferencedColumnmDiv').hide();
				return;
			}
			$('#constraintReferencedTableDiv').hide();
			$('#constraintReferencedColumnmDiv').hide();

			if (constraintType.value === "PRIMARY_KEY") {
				$('#constraintReferencedTable').val(undefined);
				$('#constraintReferencedColumn').val(undefined);
				
			} else if(constraintType.value === "FOREIGN_KEY") {
				$('#constraintReferencedTableDiv').show();
				$('#constraintReferencedColumnmDiv').show();

			} else if(constraintType.value === "UNIQUE") {
				$('#constraintReferencedTable').val(undefined);
				$('#constraintReferencedColumn').val(undefined);
			}

			$('#tablesConstraint').selectpicker('refresh');

		},

		changeReferencedTable: function(n) {
	    	
			var referencedTable = $("#constraintReferencedTables").val();

		},

		loadCodeMirror: function() {
			myTextAreaSelector = document.getElementById('querySelector');
			codeMirrorSelector = CodeMirror.fromTextArea(myTextAreaSelector, {
		    	mode: "text/x-mysql",
		    	parserfile: "codemirror/contrib/sql/js/parsesql.js",
				path: "codemirror/js/",
				stylesheet: "css/sqlcolors.css",
		    	autoRefresh: true,
		    	autoCloseBrackets: true,
		        matchBrackets: true,
		        styleActiveLine: true,
		        theme:"material",
		        lineWrapping: true
		    });
			codeMirrorSelector.setSize("100%", 200);
			
			var exampleSelector = "-- Write SQL valid code to save the template \n-- Here is an example: \n\nCREATE TABLE @tablename (\n\tid INT NOT NULL PRIMARY KEY, \n\tfield1 VARCHAR(10)\n\t);";

			var QSId = null;
			
			if(QSId==null)
			{
				codeMirrorSelector.setValue(exampleSelector);
			}
				myTextAreaSelector.value = codeMirrorSelector.getValue();
	
				setTimeout(function() {
					codeMirrorSelector.refresh();
				}, 1);
			
		},
		
		reloadSQLCodeFromServer: function() {
			// execute call to back for SQL code
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");

			if (identification.value === undefined || identification.value === "") {
				$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Must set name before"}); 
				return;
			}

			if (selectedDatasource === undefined) {
				$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Must select datasource before"}); 
				return;
			}
			
			var body = {
				"ontology": $("#identification").val(),
				"columnsRelational": createColumns,
				"columnConstraints": createConstrains

			}
			$.ajax({
				url : "/controlpanel/ontologies/virtual/sql/converter/create/" 
					+ $("#identification").val() + "?datasource=" + selectedDatasource['sgbd'],
				headers: {
					[csrf_header]: csrf_value
				},
				type : 'POST',
				data: JSON.stringify(body),
				dataType: 'json', 
				contentType: 'application/json',
				//mimeType: 'text/plain',
				success : function(data) {
					creationConfig['sqlStatment'] = data["statement"];
					codeMirrorSelector.setValue(data["statement"]);
				},
				error : function(data, status, err) {
					$.alert({title: 'ERROR '+ status + ': '+err+'!', theme: 'dark', type: 'red', content: data.responseText});

				}, 
				complete: function (data, status){
					$('#loading-collection').hide();
				}
			});
		},
		
		saveSQLCodeFromServer: function() {
			sqlStatement.value = codeMirrorSelector.getValue();
			var sch = JSON.parse(editor.getText())
			sch['sqlStatement'] = sqlStatement.value 
			editor.setText(JSON.stringify(sch))
			$("#jsonschema").val(JSON.stringify(sch));
			$.alert({title: 'Saved!', theme: 'light', type: 'green', content: sqlStatement.value}); 
		},
		
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// GLOBAL JSON AND CODE EDITOR INSTANCES
	var editor;
	var aceEditor;
//	var editorInstance;
	var schema = ''; // current schema json string var
	var myTextAreaSelector;
	var codeMirrorSelector;
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	OntologyCreateController.load(ontologyCreateJson);	
	
	var datasources = ontologyCreateReg.datasources;
	
	if(datasources.length > 0 || ontologyCreateReg.authorities == 'ROLE_ADMINISTRATOR'){
		
	}else{
		$.alert({title: 'INFO', theme: 'light', content: ontologyCreateReg.validations.datasource}); 
	}
		
	// AUTO INIT CONTROLLER.
	OntologyCreateController.init();
});