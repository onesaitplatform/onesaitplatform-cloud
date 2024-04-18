var authorizationsArr 		= []; // add authorizations
var authorizationUpdateArr  = []; // get authorizations of the ontology
var authorizationsIds 		= []; // get authorizations ids for actions
var authorizationObj 		= {}; // object to receive authorizations responses.
	
var OntologyCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Ontology Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var validTypes = ["object","string","number","integer","date","timestamp","array","geometry","file","boolean"]; // Valid property types	
	var validJsonSchema = false;
	var validMetaInf = false;
	var hasId = false; // instance

	
	// CONTROLLER PRIVATE FUNCTIONS	--------------------------------------------------------------------------------
	
	var createJsonProperties = function (jsonData){
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
		$('#fields').html(options);
		$('#fields').selectpicker('refresh');
		
		$("#fields").show();
		
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
			success : function(data) {
				$('#collections').empty();
				var tables = JSON.parse(data);
				var count = tables.length;
				if(count > 0)
					$.each( tables, function (key, object){
						$('#collections').append("<option value='"+object+"' text='"+object+"' >"+object+"</option>");
						if (!--count) {
							$('#collections').prop('disabled', false);
							$('#collections').selectpicker('refresh');
						}
					});
				else {
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
				// VALIDATE JSON SCHEMA 
				validJsonSchema = validateJsonSchema();				
				if (validJsonSchema){
					if($("#id").is(":checked")){
						$("#objectId").val($("#fields").val());
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
	    changeCollection: function(n) {
	    	
	        var collection = $("#collections").val();
	        $("#identification").val(collection);
	        $("#identification").text(collection);
	        var datasource = $("#datasources").val();
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			
			if(n==1)
			{
				$("#id").prop('checked',false);
				$("#fields").attr('disabled','disabled');
				$("#fields").empty();
				$("#fields").selectpicker('deselectAll').selectpicker('refresh');
			}
//			$("#fields").each(function(){
//				$(this).val( '' );
//				$(this).selectpicker('deselectAll').selectpicker('refresh');
//			});
			
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
//					editorInstance.setMode("text");
//					editorInstance.setText(data);
//					editorInstance.setMode("tree");
					JsonSchema.INPUT_VALUE = JSON.stringify(JSON.parse(data));
					genSchema();
				},
				error : function(data, status, er) {
					$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: er}); 
				}
			});
	        
	       	function genSchema() {
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
			        
			        editor.setText("{}");
			        editor.setMode("view");
			        editor.setText(spStaticV.render());
			        
			        $("#jsonschema").val(spStaticV.render());
			        
			     // CHANGE TO SCHEMA TAB.
//					 $('.nav-tabs li a[href="#tab_4"]').tab('show');
		    	} else {
		    		$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Error!"}); 
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
		        
		        editor.setText("{}");
		        editor.setMode("tree");
		        editor.setText(spStaticV.render());
		        
		        $("#jsonschema").val(spStaticV.render());
		        
		     // CHANGE TO SCHEMA TAB.
				 $('.nav-tabs li a[href="#tab_4"]').tab('show');
	    	} else {
	    		$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: "Error!"}); 
	    	}
	    },
		schemaToTable: function(){
			if($("#id").is(":checked")){
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
					createJsonProperties(properties);
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
			
	
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// GLOBAL JSON AND CODE EDITOR INSTANCES
	var editor;
	var aceEditor;
//	var editorInstance;
	var schema = ''; // current schema json string var
	
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
