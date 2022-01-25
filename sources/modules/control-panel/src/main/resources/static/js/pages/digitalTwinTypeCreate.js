
var DigitalTwinCreateController = function() {
	
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'Onesait Platform Control Panel'; 
	var LIB_TITLE = 'Digital Twin Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var validTypes = ["string","object","number","date","timestamp","array","binary", "double"]; // Valid property types	
	var validActionsTypes = ["update_shadow","notebook","rule","flow","ping","pipeline","log","register"]; // Valid events types	
	var hasId = false; // instance
	var jsonProperties = [];
	var jsonActions = [];
	var jsonEvents = [];
	var editor;
	var AllActionsLogic = [];
	var mountablePropModel = $('#properties').find('tr.mountable-model')[0].outerHTML; // save html-model for when select new datamodel, is remove current and create a new one.
	var mountableActModel = $('#actions').find('tr.mountable-model')[0].outerHTML; // save html-model for when select new datamodel, is remove current and create a new one.
	var mountableEventModel = $('#events').find('tr.mountable-model')[0].outerHTML; // save html-model for when select new datamodel, is remove current and create a new one. 
	// CONTROLLER PRIVATE FUNCTIONS	--------------------------------------------------------------------------------

	var propertyTypeOntologyIndex=-1;
	
	var generateSchema=false
	$("#createBtn").on('click',function(){
		var validateIdentification = validate($("#identification"));
		var validateDescription = validate($("#description"));
		if (!validateIdentification || !validateDescription){
			toastr.error(digitalTwinCreateJson.validations.fields);
		}
		if (generateSchema) {
			toastr.success(digitalTwinCreateJson.validations.validationOK);
			DigitalTwinCreateController.submitform();
		} else {
			toastr.error(digitalTwinCreateJson.validations.schema);
			return false;
		}		
	});
	
	$("#updateBtn").on('click',function(event){
		event.preventDefault();
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		if(generateSchema){
				$.ajax({
					url : "/controlpanel/digitaltwintypes/getNumOfDevices/" + $("#identification").val(),
					headers: {
						[csrf_header]: csrf_value
				    },
					type : 'GET',
					dataType: 'text', 
					contentType: 'text/plain',
					mimeType: 'text/plain',
					success : function(data) {
						if(data>0){
							toastr.error(messagesForms.operations.genOpError,digitalTwinCreateJson.validations.update);
						}else{
							if($("#identification").val()!='' && $("#identification").val()!=undefined && $("#type").val()!='' && $("#type").val()!=undefined && $("#description").val()!='' && $("#description").val()!=undefined){
								$("#createBtn").val('Please wait ...')
							      .attr('disabled','disabled');
								DigitalTwinCreateController.submitform();
							}
						}
					},
					error : function(data, status, er) { 
						toastr.error(messagesForms.operations.genOpError,er);
					}
				});
			
		}else{
			toastr.error(messagesForms.operations.genOpError,digitalTwinCreateJson.validations.schema);
			return false;
		}
		
	});
	
	// CHECK IF JSON STRING WHEN JSON PARSE IS OK OR NOT, IF THE JSON IS MALFORMED THE JSON.parse() is broken.
	var IsJsonString = function(str) {
		try {
			JSON.parse(str);
		}
		catch (e) {	return false; }
		
		return true;
	}
	
	//INIT UPDATE SCHEMA
	$('#btn-updateSchema').on('click',function(){ 
		if($("#identification").val()!='' && $("#identification").val()!=undefined && $("#type").val()!='' && $("#type").val()!=undefined && $("#description").val()!='' && $("#description").val()!=undefined){
			generateSchema=true;
			updateSchema(); 
		}else{
			toastr.error(messagesForms.operations.genOpError,digitalTwinCreateJson.validations.fields);
			return false;
		}
		
	});
	
	// Reset form
	$('#resetBtn').on('click',function(){ 
		cleanFields('digitaltwintype_create_form');
	});	
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		$('#properties tbody tr').remove();
		props=[];
		$('#actions tbody tr').remove();
		actions=[];
		$('#events tbody tr').remove();
		events=[];

		// CLEANING CODEMIRROR
		if ($('.CodeMirror')[0].CodeMirror){
			var editor = $('.CodeMirror')[0].CodeMirror;
			editor.setValue("var digitalTwinApi = Java.type('com.minsait.onesait.platform.digitaltwin.logic.api.DigitalTwinApi').getInstance();\nfunction init(){}\nfunction main(){}");
		}
		editor.setText(JSON.stringify({}));
	}	
	
	var updateSchema = function(){
		props=[];
		events=[];
		actions=[];
		logControl ? console.log('updateSchema() -> ') : '';
		resetSchemaEditor();
		updateSchemaProperties();
		updateSchemaActions();
		updateSchemaEvents();
		toastr.success(messagesForms.operations.genOpSuccess,'');
	}
	
	var resetSchemaEditor = function(){
		jsonFromEditor = {};
		jsonFromEditor["title"] = $('#identification').val();
		jsonFromEditor["links"] = {};
		jsonFromEditor["description"] = $('#description').val();
		editor.setText(JSON.stringify(jsonFromEditor));
	}
	
	var props=[];
	var updateSchemaProperties = function(){
		logControl ? console.log('updateSchemaProperties() -> ') : '';
		
		var updateProperties = $("input[name='property\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();				
		var updateTypes = $("select[name='typeProp\\[\\]']").map(function(){return $(this).val();}).get();
		var updateUnits = $("input[name='units\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();	
		var updateDirection = $("select[name='direction\\[\\]']").map(function(){return $(this).val();}).get();	
		var updateDescription = $("input[name='descriptionsProps\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();		
		
		var schemaObj = {};
		
		logControl ? console.log('|--- CURRENT: ' + updateProperties + ' types: ' + updateTypes + ' units: ' + updateUnits + ' direction: ' + updateDirection + ' description: ' + updateDescription): '';
		
		checkUnique = updateProperties.unique();
		if (updateProperties.length !== checkUnique.length)  {toastr.error(messagesForms.operations.genOpError,digitaltwintype.validations.duplicates); return false; } 
		
		// UPDATE SCHEMA		
		// UPDATE ALL PROPERTIES EACH TIME.
		if ( updateProperties.length ){	
			$.each(updateProperties, function( index, value ) {
				propIndex = updateProperties.indexOf(value);
				logControl ? console.log('index: ' + propIndex + ' | property: ' + updateProperties[propIndex] + ' type: ' + updateTypes[propIndex] + ' units: ' + updateUnits[propIndex] + ' direction: ' + updateDirection[propIndex]+ ' description: ' + updateDescription[propIndex]) : '';
					// update property on Schema /current are stored in schema var.  (property,type,units,direction,description)
					updateProperty(updateProperties[propIndex], updateTypes[propIndex], updateUnits[propIndex], updateDirection[propIndex], updateDescription[propIndex] );					
			});			
		}
		
		// CHANGE TO SCHEMA TAB.
		 $('.nav-tabs li a[href="#tab_7"]').tab('show');		
	}
	
	// AUX. UPDATE PROPERTY IN SCHEMA FOR EACH NEW PROPERTY ADDED
	var updateProperty = function(name, type, units, direction, description){
		logControl ? console.log('|---   updateProperty() -> ') : '';
		props.push('{"name":"'+name+'","type":"'+type+'","units":"'+units+'", "direction":"'+direction+'","description":"'+description+'"}');
		var prop = {};
		prop['type'] = type;
		if(units!=undefined && units!=''){
			prop['units'] = units;
		}
		prop['direction'] = direction;
		if(description!=undefined && description!=''){
			prop['description'] = description;
		}
		
		jsonFromEditor = editor.get();
		if(jsonFromEditor['properties']!=undefined){
			jsonFromEditor['properties'][name]=prop;
		}else{
			jsonFromEditor['properties']={};
			jsonFromEditor['properties'][name]=prop;
		}
		if(jsonFromEditor['links']==undefined){
			jsonFromEditor['links']={};
		}
		jsonFromEditor['links']['properties']= $("#type").val() + '/' + $("#identification").val() + "/properties"; 
		editor.set(jsonFromEditor);
		
		
	}
	
	//INIT UPDATE SCHEMA
	
	var actions=[];
	var updateSchemaActions = function(){
		logControl ? console.log('updateSchemaActions() -> ') : '';
		
		var updateActions = $("input[name='action\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();				
		var updateDescription = $("input[name='descriptionsActions\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();
		
		var schemaObj = {};
		
		logControl ? console.log('|--- CURRENT: ' + updateActions + ' description: ' + updateDescription): '';
		
		checkUnique = updateActions.unique();
		if (updateActions.length !== checkUnique.length)  { toastr.error(messagesForms.operations.genOpError,digitaltwintype.validations.duplicates); return false; } 
		
		// UPDATE SCHEMA		
		// UPDATE ALL ACTIONS EACH TIME.
		if ( updateActions.length ){	
			$.each(updateActions, function( index, value ) {
				actIndex = updateActions.indexOf(value);
				logControl ? console.log('index: ' + actIndex + ' | property: ' + updateActions[actIndex] + ' description: ' + updateDescription[actIndex]) : '';
					// update action on Schema /current are stored in schema var.  (name,description)
					updateAction(updateActions[actIndex],updateDescription[actIndex]);					
			});			
		}
		
		// CHANGE TO SCHEMA TAB.
		 $('.nav-tabs li a[href="#tab_7"]').tab('show');		
	}
	
	// AUX. UPDATE ACTION IN SCHEMA FOR EACH NEW PROPERTY ADDED
	var updateAction = function(name, description){
		logControl ? console.log('|---   updateAction() -> ') : '';
		actions.push('{"name":"'+name+'","description":"'+description+'"}');
		var act = {};
		if(description!=undefined && description!=''){
			act['description'] = description;
		}
		
		jsonFromEditor = editor.get();
		if(jsonFromEditor['actions']!=undefined){
			jsonFromEditor['actions'][name]=act;
		}else{
			jsonFromEditor['actions']={};
			jsonFromEditor['actions'][name]=act;
		}
		if(jsonFromEditor['links']==undefined){
			jsonFromEditor['links']={};
		}
		jsonFromEditor['links']['actions']= $("#type").val() + '/' + $("#identification").val() + "/actions"; 
		editor.set(jsonFromEditor);
	}
	
	//INIT UPDATE SCHEMA
	var events=[];
	var updateSchemaEvents = function(){
		logControl ? console.log('updateSchemaEvents() -> ') : '';
		
		//Set jsoneditor with ping and register events by default
		updateEvents('ping', 'Ping', true, 'PING');
		updateEvents('register', 'Register', true, 'REGISTER');
		
		var updateEvent = $("input[name='event\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();				
		var updateDescription = $("input[name='descriptionsEvents\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();	
		var updateTypes = $("select[name='typeEvent\\[\\]']").map(function(){return $(this).val();}).get();
		var updateStatus = $("input[name='status\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).is(":checked"); }}).get();
		
		var schemaObj = {};
		
		checkUnique = updateEvent.unique();
		if (updateEvent.length !== checkUnique.length)  { toastr.error(messagesForms.operations.genOpError,digitaltwintype.validations.duplicates); return false; } 
		
		// UPDATE SCHEMA		
		// UPDATE ALL ACTIONS EACH TIME.
		if ( updateEvent.length ){	
			$.each(updateEvent, function( index, value ) {
				actIndex = updateEvent.indexOf(value);
					// update action on Schema /current are stored in schema var.  (name,description)
				updateEvents(updateEvent[actIndex],updateDescription[actIndex],updateStatus[actIndex] , updateTypes[actIndex]);					
			});			
		}
		
		// CHANGE TO SCHEMA TAB.
		 $('.nav-tabs li a[href="#tab_7"]').tab('show');		
	}
	
	// AUX. UPDATE ACTION IN SCHEMA FOR EACH NEW PROPERTY ADDED
	var updateEvents = function(name, description, status, type){
		logControl ? console.log('|---   updateEvents() -> ') : '';
		events.push('{"name":"'+name+'","description":"'+description+'","type":"'+type+'","status":"'+status+'"}');
		var event = {};
		if(description!=undefined && description!=''){
			event['description'] = description;
		}
		
		jsonFromEditor = editor.get();
		if(jsonFromEditor['events']!=undefined){
			jsonFromEditor['events'][name]=event;
		}else{
			jsonFromEditor['events']={};
			jsonFromEditor['events'][name]=event;
		}
		if(jsonFromEditor['links']==undefined){
			jsonFromEditor['links']={};
		}
		jsonFromEditor['links']['events']= $("#type").val() + '/' + $("#identification").val() + "/events"; 
		editor.set(jsonFromEditor);
	}
	
	// CREATE EDITOR FOR JSON SCHEMA 
	var createEditor = function(){		
		logControl ? console.log('|--->   createEditor()') : '';
		var container = document.getElementById('jsoneditor');	
		var options = {
			mode: 'code',
			theme: 'bootstrap3',
			required_by_default: true,
			modes: ['code', 'text', 'tree', 'view'], // allowed modes
			error: function (err) {
				//$.alert({title: 'ERROR!', theme: 'light', style: 'red', content: err.toString()});
				return false;
			},
			onChange: function(){
				
				console.log('se modifica el editor en modo:' + editor.mode + ' contenido: ' + editor.getText());
			}
		};		
		editor = new jsoneditor.JSONEditor(container, options, "{}");			
	}
	
	// DELETE DIGITAL TWIN TYPE
	var deleteDigitalTwinTypeConfirmation = function(digitalTwinTypeId){
		console.log('deleteDigitalTwinTypeConfirmation() -> formId: '+ digitalTwinTypeId);
		
		// no Id no fun!
		if ( !digitalTwinTypeId ) {toastr.error(messagesForms.validation.genFormError,digitalTwinCreateJson.validations.validform); return false; }
		
		logControl ? console.log('deleteDigitalTwinTypeConfirmation() -> formAction: ' + $('.delete-digital').attr('action') + ' ID: ' + $('#delete-digitaltwintypeId').attr('digitaltwintypeId')) : '';
		
		// call ontology Confirm at header. 
		HeaderController.showConfirmDialogDigitalTwinType('delete_digitaltwintype_form');	
	}
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// INIT CODEMIRROR
	var handleCodeMirror = function () {
		logControl ? console.log('handleCodeMirror() on -> logicEditor') : '';	
		
        var myTextArea = document.getElementById('logicEditor');
        var myCodeMirror = CodeMirror.fromTextArea(myTextArea, {
        	mode: "text/javascript",
            lineNumbers: false,
            foldGutter: true,
            matchBrackets: true,
            styleActiveLine: true,
            theme:"elegant",         

        });
		myCodeMirror.setSize("100%", 350);
    }
	
	var validate = function (obj){
		if (obj.val() === '') { 
			obj.parent().addClass('has-error');
			obj.nextAll('span:first').removeClass('hide').addClass('help-block-error font-red');
			return false;
		} else { 
			obj.parent().removeClass('has-error');
			obj.nextAll('span:first').addClass('hide');
			return true;
		}
	}
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return digitalTwinCreateJson = Data;
		},
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleCodeMirror();
			
			// INPUT MASK FOR ontology identification allow only letters, numbers and -_
			$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
			
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
			
			$('#properties').mounTable(jsonProperties,{
				model: '.mountable-model',
				noDebug: false,
				addLine:{				
					button: "#button2",					
					onClick: function (element){
						console.log('Property added!');				
						return true;
					}
				}			
			});
			
			$('#actions').mounTable(jsonActions,{
				model: '.mountable-model',
				noDebug: false,
				addLine:{				
					button: "#button3",					
					onClick: function (element){
						console.log('Action added!');
						return true;
					}
				}			
			});
			
			$('#events').mounTable(jsonEvents,{
				model: '.mountable-model',
				noDebug: false,
				addLine:{				
					button: "#button4",					
					onClick: function (element){
						console.log('Event added!');				
						return true;
					}
				}			
			});
			
			// UPDATE TITLE TYPE AND DESCRIPTION IF CHANGED 
			$('#identification').on('change', function(){
				var jsonFromEditor = {};
				var datamodelLoaded = $('#properties').attr('data-loaded');
				if (datamodelLoaded){			
					if (IsJsonString(editor.getText())){				
						jsonFromEditor = editor.get();
						jsonFromEditor["title"] = $(this).val();
						jsonFromEditor["links"] = {};
						editor.set(jsonFromEditor);
					}			
				}		
			}).on('blur', function(){
				validate($(this));
			});
			
			$('#description').on('change', function(){
				var jsonFromEditor = {};
				var datamodelLoaded = $('#properties').attr('data-loaded');
				if (datamodelLoaded){			
					if (IsJsonString(editor.getText())){				
						jsonFromEditor = editor.get();
						jsonFromEditor["description"] = $(this).val();
						editor.set(jsonFromEditor);
					}			
				}	
			}).on('blur', function(){
				validate($(this));
			});
			
			$('#type').on('change', function(){
				var jsonFromEditor = {};
				var datamodelLoaded = $('#properties').attr('data-loaded');
				if (datamodelLoaded){			
					if (IsJsonString(editor.getText())){				
						jsonFromEditor = editor.get();
						jsonFromEditor["type"] = $(this).val();
						editor.set(jsonFromEditor);
					}			
				}	
			});
			
			createEditor();
			
			// INSERT MODE ACTIONS  (ontologyCreateReg.actionMode = NULL ) 
			if ( digitalTwinCreateJson.actionMode === null){
				logControl ? console.log('|---> Action-mode: INSERT') : '';
			}
			// EDIT MODE ACTION 
			else {	
				logControl ? console.log('|---> Action-mode: UPDATE') : '';
				var propTypeOntology = [];
				// if digitalTwinType has properties  we load it!.
				propertiesJson = digitalTwinCreateJson.properties;			
				if (propertiesJson.length > 0 ){
					
					// MOUNTING PROPERTIES ARRAY
					var name, type , units, direction, description = '';
					$.each( propertiesJson, function (key, object){			
						
						name = object.name; 
						type = object.type; 
						units= object.unit;
						direction = object.direction;
						description = object.description;
						
						if(type=='ontology'){
							propTypeOntology.push(key);
						}
						
						propertyUpdate = {"property": name, "typeProp": type, "units" : units, "direction" : direction, "descriptionsProps": description};					
						jsonProperties.push(propertyUpdate);
						
					});
					
					logControl ? console.log('propertiesArr on UPDATE: ' + jsonProperties.length + ' Arr: ' + JSON.stringify(jsonProperties)) : '';
					
					$('#properties > tbody').html("");
					$('#properties > tbody').append(mountablePropModel);
					
					$('#properties').mounTable(jsonProperties,{
						model: '.mountable-model',
						noDebug: false,
						addLine:{				
							button: "#button2",					
							onClick: function (element){
								console.log('Property added!');				
								return true;
							}
						}			
					});
					
					$.each(propTypeOntology, function(key, object){
						var cell=$('tbody tr:eq('+object+') td:eq(2)', $("#properties"));
						cell.find("input").each(function() {
					        $(this).attr("disabled", "disabled");
					    });
					});
				}
				
				// if digitalTwinType has actions  we load it!.
				actionsJson = digitalTwinCreateJson.actions;			
				if (actionsJson.length > 0 ){
					
					// MOUNTING ACTIONS ARRAY
					var name, description = '';
					$.each( actionsJson, function (key, object){			
						
						name = object.name; 
						description = object.description;
						
						ActionUpdate = {"action": name, "descriptionsActions": description};					
						jsonActions.push(ActionUpdate);
						
					});

					logControl ? console.log('jsonActions on UPDATE: ' + jsonActions.length + ' Arr: ' + JSON.stringify(jsonActions)) : '';
					
					$('#actions > tbody').html("");
					$('#actions > tbody').append(mountableActModel);
					
					$('#actions').mounTable(jsonActions,{
						model: '.mountable-model',
						noDebug: false,
						addLine:{				
							button: "#button3",					
							onClick: function (element){
								console.log('Action added!');				
								return true;
							}
						}			
					});
				}
				
				// if digitalTwinType has actions  we load it!.
				eventsJson = digitalTwinCreateJson.events;			
				if (eventsJson.length > 0 ){
					
					// MOUNTING ACTIONS ARRAY
					var name, description, type, status = '';
					$.each( eventsJson, function (key, object){			
						
						name = object.name; 
						description = object.description;
						type = object.type.toLowerCase();
						status = object.status;
						
						EventUpdate = {"event": name, "status":status, "typeEvent": type,"descriptionsEvents": description};					
						jsonEvents.push(EventUpdate);
						
					});

					logControl ? console.log('jsonEvents on UPDATE: ' + jsonEvents.length + ' Arr: ' + JSON.stringify(jsonEvents)) : '';
					
					$('#events > tbody').html("");
					$('#events > tbody').append(mountableEventModel);
					
					$('#events').mounTable(jsonEvents,{
						model: '.mountable-model',
						noDebug: false,
						addLine:{				
							button: "#button4",					
							onClick: function (element){
								console.log('Event added!');				
								return true;
							}
						}			
					});
				}
				
				editor.setText(digitalTwinCreateJson.json);
				editor.setMode("tree");
				
				
				var logica = digitalTwinCreateJson.logic;
					
				if(logica.charAt(0) === '\"'){
					logica = logica.substr(1, logica.length-2);
				}
				var editorLogic = $('.CodeMirror')[0].CodeMirror;
				editorLogic.setValue(logica)
			}
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		submitform: function(){
			
			 $.each(props, function(k,v){
			       
		         $("<input type='hidden' value='"+v+"' />")
		         .attr("name", "propiedades")
		         .appendTo("#digitaltwintype_create_form");
		        
		     });
			
			 $.each(actions, function(k,v){
			       
		         $("<input type='hidden' value='"+v+"' />")
		         .attr("name", "acciones")
		         .appendTo("#digitaltwintype_create_form");
		        
		     });
			 
			 $.each(events, function(k,v){
			       
		         $("<input type='hidden' value='"+v+"' />")
		         .attr("name", "eventos")
		         .appendTo("#digitaltwintype_create_form");
		        
		     });
			var editorLogic = $('.CodeMirror')[0].CodeMirror;
			
			$("#json").val(JSON.stringify(editor.get()));
			$("#logic").val(JSON.stringify(editorLogic.getValue()));
			$("#digitalType").val($("#type").val());
			
			$("#digitaltwintype_create_form").submit();
			
			$("#createBtn").removeAttr('disabled');
		},
		
		// ADD ACTION LOGIC
		addActionLogic: function(obj){
			logControl ? console.log(LIB_TITLE + ': addActionLogic()') : '';
			var editor = $('.CodeMirror')[0].CodeMirror;
			if(!AllActionsLogic.includes(obj.value)){
				
				var js = editor.getValue();
				js = js + "\nvar onAction"+obj.value.substring(0,1).toUpperCase() + obj.value.substring(1)+"=function(data){ }";
				
				editor.setValue(js);
				
				AllActionsLogic.push(obj.value);
			}

		},
		
		// DELETE DIGITAL TWIN TYPE 
		deleteDigitalTwinType: function(digitalTwinTypeId){
			logControl ? console.log(LIB_TITLE + ': deleteDigitalTwinType()') : '';	
			deleteDigitalTwinTypeConfirmation(digitalTwinTypeId);			
		},
	// CHECK FOR NON DUPLICATE PROPERTIES
		checkProperty: function(obj){
			logControl ? console.log(LIB_TITLE + ': checkProperty()') : '';
			var allProperties = $("input[name='property\\[\\]']").map(function(){return $(this).val();}).get();		
			areUnique = allProperties.unique();
			if (allProperties.length !== areUnique.length)  { 
				toastr.error(messagesForms.operations.genOpError,digitalTwinCreateJson.validations.duplicates);
				$(obj).val(''); return false;
			} 
			else {
				$(obj).closest('tr').find('.btn-mountable-remove').attr('data-property', $(obj).val() );   
			}
		},
		// CHECK FOR NON DUPLICATE ACTIONS	
		checkAction: function(obj){
			logControl ? console.log(LIB_TITLE + ': checkAction()') : '';
			var allActions = $("input[name='action\\[\\]']").map(function(){return $(this).val();}).get();		
			areUnique = allActions.unique();
			if (allActions.length !== areUnique.length)  { 
				toastr.error(messagesForms.operations.genOpError,digitalTwinCreateJson.validations.duplicates);
				$(obj).val(''); return false;
			} 
			else {
				$(obj).closest('tr').find('.btn-mountable-remove').attr('data-property', $(obj).val() );   
			}
		},
		
		// CHECK FOR NON DUPLICATE EVENTS	
		checkEvent: function(obj){
			logControl ? console.log(LIB_TITLE + ': checkEvent()') : '';
			var allEvents = $("input[name='event\\[\\]']").map(function(){return $(this).val();}).get();		
			areUnique = allEvents.unique();
			if (allEvents.length !== areUnique.length)  { 
				toastr.error(messagesForms.operations.genOpError,digitalTwinCreateJson.validations.duplicates);
				$(obj).val(''); return false;
			} 
			else {
				$(obj).closest('tr').find('.btn-mountable-remove').attr('data-property', $(obj).val() );   
			}
		},
		
		// CHECK PROPERTIES TYPE
		checkType: function(obj){	
			logControl ? console.log(LIB_TITLE + ': checkType()') : '';
			var propType = '';
			var currentTypeValue = $(obj).val();
			var currentType = currentTypeValue.toLowerCase();
			// if type is a valid type, assign this value , if not, string by default.
			propType = $.inArray( currentType, validTypes ) > -1 ?  currentType : 'thing';
			$(obj).val(propType);
		},
		// CHECK ACTIONS TYPE
		checkTypeAction: function(obj){	
			logControl ? console.log(LIB_TITLE + ': checkTypeAction()') : '';
			var actType = '';
			var currentTypeValue = $(obj).val();
			var currentType = currentTypeValue.toLowerCase();
			// if type is a valid type, assign this value , if not, string by default.
			actType = $.inArray( currentType, validActionsTypes ) > -1 ?  currentType : 'update_shadow';
			$(obj).val(actType);
		},
		// REMOVE PROPERTYS
		removeProperty: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeProperty()') : '';
			
			var remproperty = $(obj).closest('tr').find("input[name='property\\[\\]']").val();		
			$(obj).closest('tr').remove();
		},
		// REMOVE ACTIONS
		removeAction: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeAction()') : '';
			
			var remproperty = $(obj).closest('tr').find("input[name='action\\[\\]']").val();		
			$(obj).closest('tr').remove();
		},
		// REMOVE EVENTS
		removeEvent: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeEvent()') : '';
			
			var remproperty = $(obj).closest('tr').find("input[name='event\\[\\]']").val();		
			$(obj).closest('tr').remove();
		},
		//Show Select Ontology dialog
		checkShowOntology: function(obj){
			if($(obj).val()==='ontology'){
				propertyTypeOntologyIndex=$(obj).parent().parent().index();			
				$('#dialog-selectOntology').modal('toggle');
			}else{
				var cell=$('tbody tr:eq('+propertyTypeOntologyIndex+') td:eq(2)', $("#properties"));
				cell.find("input").each(function() {
			        this.value="";
			        $(this).removeAttr("disabled");
			    });
			}

		},
		selectOntologyType: function(){
			ontology=$("#id_type_ontology_select").val();
			var cell=$('tbody tr:eq('+propertyTypeOntologyIndex+') td:eq(2)', $("#properties"));
			cell.find("input").each(function() {
		        this.value=ontology;
		        $(this).attr("disabled", "disabled");
		    });
			
			$('#dialog-selectOntology').modal('hide');
		}
	}
}();


// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	DigitalTwinCreateController.load(digitalTwinCreateJson);
	
	$("#logicEditor").val("var digitalTwinApi = Java.type('com.minsait.onesait.platform.digitaltwin.logic.api.DigitalTwinApi').getInstance();\nfunction init(){}\nfunction main(){}");
	
	DigitalTwinCreateController.init();
});
