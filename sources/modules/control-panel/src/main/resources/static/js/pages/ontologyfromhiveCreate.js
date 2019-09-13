var authorizationsArr 		= []; // add authorizations
var authorizationUpdateArr  = []; // get authorizations of the ontology
var authorizationsIds 		= []; // get authorizations ids for actions
var authorizationObj 		= {}; // object to receive authorizations responses.
	
var OntologyHiveCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Ontology Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var validTypes = ["object","string","number","integer","date","timestamp","array","geometry","file"]; // Valid property types	
	var validJsonSchema = false;
	var validMetaInf = false;
	var hasId = false; // instance

	
	// CONTROLLER PRIVATE FUNCTIONS	--------------------------------------------------------------------------------
	
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
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		//CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
		
		
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
		currentLanguage = ontology.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// custom messages
            messages: {	
				jsonschema: { required:"El esquema no se ha guardado correctamente"},
				datamodelid: { required: "Por favor seleccione una plantilla de ontología, aunque sea la vacia."}
			},
			// validation rules
            rules: {
				ontologyId:		{ minlength: 5, required: true },
                identification:	{ minlength: 5, required: true },						
				datamodelid:	{ required: true},
				jsonschema:		{ required: true},
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
				else if ( element.is(':hidden'))	{ 
					if ($('#datamodelid').val() === '') { $('#datamodelError').removeClass('hide');} 					
				}				
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
					
					// VALIDATE TAGSINPUT
					validMetaInf = validateTagsInput();
					if (validMetaInf) {
						form.submit();					
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
			$.alert({title: 'INFO!', type: 'blue' , theme: 'dark', content: ontology.validations.authinsert});
			return false;
		  }
		});
				
		
		// 	INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_]*", greedy: false });
		
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
		
		
		logControl ? console.log('|---> Action-mode: INSERT') : '';
			
		// Set active 
		$('#active').trigger('click');
			
		// Set Public 
		$('#public').trigger('click');
		
			
	}	
	
	// CREATE EDITOR FOR JSON SCHEMA 
	var createEditor = function(){		
		showLog ? console.log('|--->   createEditor()') : '';
		var container = document.getElementById('jsoneditor');	
		var options = {
			mode: 'view',
			theme: 'bootstrap3',
			required_by_default: true,
			modes: ['view'], // allowed modes
			error: function (err) {
				$.alert({title: 'ERROR!', theme: 'dark', style: 'red', content: err.toString()});
				return false;
			},
			onChange: function(){
				
				console.log('se modifica el editor en modo:' + editor.mode + ' contenido: ' + editor.getText());
			}
		};
		
		editor = new jsoneditor.JSONEditor(container, options, "");			
	}
	
	
	// CHECK IF JSON STRING WHEN JSON PARSE IS OK OR NOT, IF THE JSON IS MALFORMED THE JSON.parse() is broken.
	var IsJsonString = function(str) {
		try {
			JSON.parse(str);
		}
		catch (e) {	return false; }
		
		return true;
	}
	
	// VALIDATE TAGSINPUT
	var validateTagsInput = function(){		
		if ($('#metainf').val() === '') { $('#metainferror').removeClass('hide').addClass('help-block-error font-red'); return false;  } else { return true;} 
	}
	
	
	// GENERATE DUMMY ONTOLOGY INSTANCE FROM JSON SCHEMA
	var generateOntologyInstance = function(){
		logControl ? console.log('|--->   generateOntologyInstance()') : ''; 
		
		var instance 		= "";
		var data 			= "";
		var ontologyJson 	= {};
		hasId = false;
		document.getElementById("ontology_instance").innerHTML = "";
		
		// check if json-string can be parsed
		if(IsJsonString(editor.getText())){

			// get JSON
			var ontologyJson = JSON.parse(editor.getText());
			
			instance = instance + "{"
            // for each property on json.properties
			for ( var property in ontologyJson.properties ){
				
				data = "";
				// check for root node
				if ( ontologyJson.properties[property] && ontologyJson.properties[property].$ref ){
				
					if ( !hasId ){	instance = instance + "\"" + property + "\":"; } else {	instance = instance + "\"" + property + "\":";	}
				
					// access node root reference
					var ref = ontologyJson.properties[property].$ref;
					ref = ref.substring(ref.indexOf("/")+1, ref.length);
					data = ontologyJson[ref].properties;
				
					// Se genera la seccion correspondiente a cada propiedad del elemento de referencia
					instance = instance + "{ ";    
					for( var propertyName in data ) {
						instance = generateProperty(propertyName, data[propertyName], instance);
					}
					instance = instance.substring(0,instance.length-1);
					instance = instance + "}";
				} 
				else {
					// if no root node, get from main properties.
					instance = generateProperty(property, ontologyJson.properties[property], instance);
					instance = instance.substring(0,instance.length-1);
				}
				
				instance = instance + ",";
			}
			
			instance = instance.substring(0,instance.length-1);  
			instance = instance + "}";
			document.getElementById("ontology_instance").innerHTML = "";
			document.getElementById("ontology_instance").innerHTML = instance;
			
			if (ontologyJson.properties == null ){
                	document.getElementById("ontology_instance").innerHTML = "";
			}	
		
		}
		else {
			// no JSON no fun!
			$.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'dark', content: ontology.validations.noschema});
		}		
	}
	
	
	// GENERATE EACH PROPERTY FOR ONTOLOGY INSTANCE.
	var generateProperty = function(propertyName, property, instance){
    	logControl ? console.log('    |--->   generateProperty()') : '';
		var thevalue = "";
		
		// if has internalID (oid) we generate it.
        if ( propertyName == "$oid") {
            hasId = true;
            if ( property.type == "string") { instance = instance + "\"$oid\":\"53b281b1c91cbd35025e3d91\""; }
            instance = instance + ",";        
        }
		else {
			// if not oid, we treat the property        	
            instance = instance + "\"" + propertyName + "\":"; // adding name
            
            var tipo = property.type; // adding type
            if (propertyName == "geometry"){ instance = instance + generateBasicType("geometry", "", "");
            // adding object type
            } else if (tipo.toLowerCase() == "object"){ instance = instance + generateObject(property, "", propertyName);
			// adding array type
            } else if (tipo.toLowerCase() == "array" ){ instance = instance + generateArray(property, "", propertyName);
            // else basic type
            } else {
                thevalue = "";
                // if enum type, get first value of enum.
                if ( property.enum != null ){ thevalue = property.enum[0]; }
                instance = instance + generateBasicType(tipo, "", "", thevalue);
            }
            instance = instance + ",";
        }
        return instance;
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
		else if ( propType == "geometry" || obj =="geometry"){ return "{\"type\":\"Point\", \"coordinates\":[9,19.3]}"; }
		else if ( propType == "number"   || propType == "numeric" ){	return "28.6" } 		
    }	
	
	
	// GENERARATE PROPERTY TYPES [GEOMETRY, OBJECT, ARRAY ]
	var generateObject = function(ontology, instance, parent){
        logControl ? console.log('        |--->   generateObject()') : '';
		
       	instance = "{";       	
       	if ( ontology.properties ){
	        for ( var obj in ontology.properties ){
	            
				var objtype = ontology.properties[obj].type;
	             // if obj <> date or geometry, iterates recursive for treatment.
	             if ((objtype.toLowerCase() == "object") && (obj != "geometry") && ontology.properties[obj].properties && ontology.properties[obj].properties.$date == null ){ 
	             	instance = instance + "\"" +obj+"\":"+ generateObject(ontology.properties[obj], "", obj);
	             
	             }
				 // date obj
				 else if ((ontology.properties && ontology.properties.$date != null) || (ontology.properties && ontology.properties[obj] && ontology.properties[obj].properties && ontology.properties[obj].properties.$date!= null)){
	                 // date root node or date children node
	            	 if (obj == "$date"){  instance = instance + "\"$date\": \"2014-01-30T17:14:00Z\""; } else { instance = instance + "\"" +obj+"\":"+ "{\"$date\": \"2014-01-30T17:14:00Z\"}"; }
				 }
				 // geometry with direct reference to point
				 else if (ontology[obj] && ontology[obj].properties &&  ontology[obj].properties[propertyName].properties && ontology[obj].properties[propertyName].properties.type && ontology.properties[obj].properties.type.enum[0]== "Point"){
	                 instance = instance + "\"" +obj+"\":"+ "{\"type\":\"Point\", \"coordinates\":[9,19.3]}";
	             
	             }
				// array
				 else if (objtype.toLowerCase() == "array"){
	                    instance = instance + "\""+ obj + "\":" + generateArray(ontology.properties[obj], "", obj);	             
	             }
				 // Basic
				 else {
	            	 var valor = "";
	            	 // if enum getr first value
	                 if (ontology.properties[obj].enum != null){
	                	  valor = ontology.properties[obj].enum[0];
	                 }
	            	 instance = instance + "\""+ obj + "\":" + generateBasicType(objtype, obj, parent, valor);
	             }
	             instance = instance + ",";
	        }
	        instance = instance.substring(0,instance.length-1);
	     // if obj is null, generate default
       	} else {
       		instance = instance + "\"object\"";
       	}
        return instance + "}";
    }
		
		
	// GENERARATE PROPERTY TYPES [ ARRAY ]
	var generateArray = function(ontology, instance, parent){
		 logControl ? console.log('        |--->   generateArray()') : '';
        var minItems = 1;
        // Se obtiene el numero minimo de elementos del array
		console.log('ARRAY OBJ: ' + JSON.stringify(ontology));
		
		// void or malformed array
		if (!ontology.hasOwnProperty('items')){
			instance = instance + "[]";  
			return instance;
		}
		
        if (ontology.minItems != null) {
            minItems =  ontology.minItems;
			
        }
        instance = instance + "[";        
        if (ontology.items.type.toLowerCase() == "object"){
            for (i=1;i<=minItems;i++) {
                instance = instance + generateObject(ontology.items, "", parent);
                if (i < minItems){
                    instance = instance + ",";
                }
            }       
        } else {
            for (i=1;i<=minItems;i++) {
                var valor ="";
                if (ontology.items.enum != null){
                    valor = ontology.items.enum[0];
                }
                instance = instance + generateBasicType(ontology.items.type, "", "", valor);
                if (i < minItems){
                    instance = instance + ",";
                }
            }
        }
        return instance + "]";  
    };
    
    var getTableDescription = function (){
		var tablename = $("#identification").val();
		
		$.get( "/controlpanel/hadoop/describe?tablename=" + tablename, function( data ) {				  
			$('.table-viewer-describe').createTable(data, {}); 				
		});
	}
	
	var getSchema = function  () {
		
		var tablename = $("#identification").val();
		
		$.get( "/controlpanel/hadoop/generateschema?tablename=" + tablename, function( response ) {	
			
			if ($('#jsoneditor').attr('data-loaded') == 'false') { 
				createEditor(); 
				$('#jsoneditor').attr('data-loaded', true);	
			}
			//editor.setText(response);
			editor.setText(JSON.stringify(response));
			editor.setMode('view');
		});
		
		if ($('#result-panel').hasClass('hide')){ $('#result-panel').toggleClass('hide'); }
		
	}
	
    var importHiveTable = function () {
    	getSchema ();
		getTableDescription();
		
		if ($('#hivetable-description').hasClass('hide')){ $('#hivetable-description').toggleClass('hide'); }
    }
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{

		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';				
			//handleValidation();
			//createEditor();
			
			
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
		
		// CHECK FOR NON DUPLICATE PROPERTIES
		checkProperty: function(obj){
			logControl ? console.log(LIB_TITLE + ': checkProperty()') : '';
			var allProperties = $("input[name='property\\[\\]']").map(function(){return $(this).val();}).get();		
			areUnique = allProperties.unique();
			if (allProperties.length !== areUnique.length)  { 
				$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: ontology.validations.duplicates});
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
			propType = $.inArray( currentType, validTypes ) > -1 ?  currentType : 'string';
			logControl ? console.log('checkType: ' +propType ) : '';
			$(obj).val(propType);
		},
		
		// CHECK PROPERTIES to be  REQUIRED or NOT 
		checkRequired: function(obj){
			logControl ? console.log(LIB_TITLE + ': checkRequired()') : '';
			var propRequired = '';
			var currentRequiredValue = $(obj).val();
			var currentRequired = currentRequiredValue.toLowerCase();
			// if type is a required field, assign this value , if not, '' by default, (not required).
			propRequired = currentRequired == 'required' ?  currentRequired : '';
			$(obj).val(propRequired);
		},
				
		// JSON SCHEMA VALIDATION
		validateJson: function(){	
			validateJsonSchema();			
		},
		
		// GENERATE DUMMY ONTOLOGY INSTANCES
		generateInstance: function(){
			logControl ? console.log(LIB_TITLE + ': generateInstance()') : '';
			generateOntologyInstance();
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
		importHiveTable: function () {
			importHiveTable();
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// GLOBAL JSON AND CODE EDITOR INSTANCES
	var editor;
	var aceEditor;
	var schema = ''; // current schema json string var
		
	// AUTO INIT CONTROLLER.
	OntologyHiveCreateController.init();
});
