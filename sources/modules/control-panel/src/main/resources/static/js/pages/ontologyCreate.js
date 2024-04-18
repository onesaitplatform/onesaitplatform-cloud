var authorizationsArr 		= []; // add authorizations
var authorizationUpdateArr  = []; // get authorizations of the ontology
var authorizationsIds 		= []; // get authorizations ids for actions
var authorizationObj 		= {}; // object to receive authorizations

// responses.
var referencesArr 			=[]; // references LD array
var referencesIds			=[]; // references ID avoid duplication

var hasDocuments;

var OntologyCreateController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
    var APPNAME = 'onesait Platform Control Panel';
	var LIB_TITLE = 'Ontology Controller';
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.
	var internalLanguage = 'en';
	var validTypes = ["object","string","number","integer","date","timestamp-mongo","timestamp","array","geometry-point","geometry-linestring","geometry-polygon","geometry-multipoint","geometry-multilinestring","geometry-multipolygon","file","boolean"]; // Valid
																																		// property
																																		// types
	var mountableModel = $('#datamodel_properties').find('tr.mountable-model')[0].outerHTML; 
	// save html-model for when select new datamodel, is remove current and
	// create a new one.
	var mountableModel2 = $('#ontology_autthorizations').find('tr.authorization-model')[0].outerHTML;
	var validIdName = false;
	var validJsonSchema = false;
	var validMetaInf = false;
	var hasId = false; // instance
	var mountableModelLD = $('#ontology_references').find('tr.reference-model')[0].outerHTML;
	var mapPropType = {};
	
	var schemaUrl = 'ontologies/schema/';
	// CONTROLLER PRIVATE FUNCTIONS
	// --------------------------------------------------------------------------------


	// AUX. DATAMODEL PROPERTIES OBJECT JSON
	var createJsonProperties = function (jsonData){
		logControl ? console.log('|---  createJsonProperties()') : '';

		var jsonFormatted 	= [];
		var properties 		= [];
		var required 		= [];
		var propObj			= {};
		var propRequired	= '';
		var propDescription	= '';
		var propEncrypted	= 'false';
		var isFile			= false;
		var isGeometryPoint		= false;
		var isGeometryLineString = false;
		var isGeometryPolygon	= false;
		var isGeometryMultiPoint = false;
		var isGeometryMultiLineString	= false;
		var isGeometryMultiPolygon	= false;
		var isDate	= false;
		var isTimestampMongo     = false;
		var objectType		= '';
		var isTimestamp = false;
	

		// Required
		if ( jsonData.hasOwnProperty('datos') ){ required = jsonData.datos.required; } else { required = jsonData.required;  }

		// Properties
		if ( jsonData.hasOwnProperty('datos') ){ properties = jsonData.datos.properties; } else { properties = jsonData.properties;  }

		// KEY and VALUE (value or object, or array...)
		$.each( properties, function (key, object){
			if (object){
				isFile = false;
				isGeometryPoint		= false;
				isGeometryLineString = false;
				isGeometryPolygon	= false;
				isGeometryMultiPoint = false;
				isGeometryMultiLineString	= false;
				isGeometryMultiPolygon	= false;
				isDate	= false;
				isTimestampMongo     = false;
				isTimestamp = false;
				propEncrypted = false;
				console.log('|--- Key: '+ key );
				$.each(object, function (propKey, propValue){
					if ( propKey == 'encrypted'){
						propEncrypted = propValue === true ? 'true' : 'false';
					}

					if ( propKey == 'description'){
						propDescription = propValue !== '' ?  propValue  : '';
					}

					// add property to properties
					if ( propKey == 'type'){
						// check required
						propRequired = $.inArray( key, required ) > -1 ? propRequired = 'required' : propRequired = '';

						// try to find geometry object
						// try to find file object
						if(object.hasOwnProperty('format')){
							if (object['format'] == 'date'){
								isDate	= true;
							} else {
								isTimestamp=true;									
							}						
						}
						if ( object.hasOwnProperty('properties')) { if (object.properties.hasOwnProperty('media')){ isFile = true;  } }
						if ( object.hasOwnProperty('properties')) { 
							if (object.properties.hasOwnProperty('coordinates'))
							{ 
								try {
									
									if (object.properties.type.enum[0]=="Point") isGeometryPoint = true;  
									if (object.properties.type.enum[0]=="LineString") isGeometryLineString = true;  
									if (object.properties.type.enum[0]=="Polygon") isGeometryPolygon = true;  
									if (object.properties.type.enum[0]=="MultiPoint") isGeometryMultiPoint = true;  
									if (object.properties.type.enum[0]=="MultiLineString") isGeometryMultiLineString = true;  
									if (object.properties.type.enum[0]=="MultiPolygon") isGeometryMultiPolygon = true; 
									
								} catch (e){
									console.log("Null value");
									console.log(e.message);
								}
 
							}
						}
						if ( object.hasOwnProperty('properties')) { if (object.properties.hasOwnProperty('$date')){ isTimestampMongo = true;  }}
						if (isFile) { objectType = 'file';  } else if (isGeometryPoint) { objectType = 'geometry-point'; } else if (isGeometryLineString) { objectType = 'geometry-linestring'; } else if (isGeometryPolygon) { objectType = 'geometry-polygon'; }
						else if (isGeometryMultiPoint) { objectType = 'geometry-multipoint'; } else if (isGeometryMultiLineString) { objectType = 'geometry-multilinestring'; } else if (isGeometryMultiPolygon) { objectType = 'geometry-multipolygon'; }
						else if (isTimestampMongo) { objectType = 'timestamp-mongo'; } else if (isTimestamp) { objectType = 'timestamp'; } else if (isDate) { objectType = 'date'; }
						else { objectType = propValue; }

						// adding properties
						propObj = {"property": key, "type": objectType, "required": propRequired, "encrypted": propEncrypted , "descriptions": propDescription};
						jsonFormatted.push(propObj);
					}
				});
			}
		});
		console.log('|--- jsonFormatted: '+ JSON.stringify(jsonFormatted ));
		return jsonFormatted;
	}


	// AUX. getProperties return properties array
	var getProperties = function(json){
		logControl ? console.log('   |---  getProperties()') : '';
		var arrProperties = [];
		
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		
		$.ajax({
			url:"/controlpanel/ontologies/hasDocuments/" + ontologyCreateReg.actionMode,
			headers: {
				[csrf_header]: csrf_value
		    },
			type:"GET",
			async: false,
			success: function(response,status){
				hasDocuments = response;
			}
		});

		// KEYs
		$.each( json, function (key, object){
			//Only required properties if the ontology has documents
			if(object.required == 'required' && hasDocuments){
				arrProperties.push(object.property);
			}
		});
		logControl ? console.log('      |----- getProperties: ' + JSON.stringify(arrProperties)) : '';
		return arrProperties;
	}


	// AUX. getTypes return types array
	var getTypes = function(json){
		logControl ? console.log('   |---  getTypes()') : '';
		var arrTypes = [];

		// KEYs
		$.each( json, function (key, object){
			$.each(object, function (key, value){
				if (value){ if ( key == 'type') {  arrTypes.push(value); } }
			});
		});
		logControl ? console.log('      |----- getTypes: ' + JSON.stringify(arrTypes)) : '';
		return arrTypes;
	}

	
	// AUX. getRequired return required array
	var getRequired = function(json){
		logControl ? console.log('   |---  getReguired()') : '';
		var arrRequired = [];

		// KEYs
		$.each( json, function (key, object){
			$.each(object, function (key, value){
				if (value){ if ( key == 'required') { if (value == '') {value='none'} arrRequired.push(value); } }
			});
		});
		logControl ? console.log('      |----- getRequired: ' + JSON.stringify(arrRequired)) : '';
		return arrRequired;
	}
	// HANDLE LINKED DATA
	var mountReferenceInitialTable = function(){
		if(ontologyCreateReg.actionMode != null){
			var schemaObj = JSON.parse(schema);

			console.log('references:  ' + JSON.stringify(schemaObj));
			if (schemaObj.hasOwnProperty("_references")){
				var references = schemaObj["_references"];
				references.forEach(function(r){
					var srcAttPath = r["self"];
					// if is referencing from array
					if(srcAttPath.endsWith(".items"))
					srcAttPath = srcAttPath.replace(/.items/g, '');
					var srcAtt = srcAttPath.split(".")[ srcAttPath.split(".").length - 1]
					var target = r["target"];
					var validate = r["validate"];
					var dstOnt = target.split(schemaUrl)[1].split("#")[0];
					var dstAttPath = target.split(schemaUrl)[1].split("#")[1];
					if(dstAttPath.endsWith(".items"))
					dstAttPath = dstAttPath.replace(/.items/g, '');

					var dstAtt = dstAttPath.split(".")[ dstAttPath.split(".").length - 1]
					referencesArr.push({ "srcAtt" : srcAtt, "dstOntology" : dstOnt, "dstAtt": dstAtt, "srcAttPath" : srcAttPath, "dstAttPath" : dstAttPath, "validate": validate});
					referencesIds.push(srcAtt + dstOnt + dstAtt);
				});
				mountTableReferences();

			}
		}
			
	}
	
	var createReferences = function(schema){
		
		var refs = [];
		referencesArr.forEach(function(r){
			var self = r.srcAttPath;
			var target = schemaUrl + r.dstOntology + '#' + r.dstAttPath;
			var validate = r.validate;
			refs.push({"self":self, "target": target, "validate":validate});
		});
		schema["_references"] = refs;
	}
	var mountLDModal = function(){
		cleanLDSelects();
		$('#linked-data-modal').modal('show');
		var properties = $("input[name='property\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();
		var propTypes = $("select[name='type\\[\\]']").map(function(){return $(this).val();}).get();
		var schemaObj = {};
		// get current schema
		if (typeof schema == 'string')
			schemaObj = JSON.parse(schema);
		var parentNode = 'properties.';
		if(schemaObj.hasOwnProperty('datos'))
			parentNode = 'datos.' + parentNode;
		properties.forEach(function(property,i){
			$('#self-property').append($('<option>', {value: parentNode + property, text: property}));
			mapPropType[property]=propTypes[i];
		})
		$('#self-property').selectpicker('refresh');
	}
	
	var cleanLDSelects =  function(){
		var selects = ['self-property','target-property'];
		selects.forEach(function(select){
			$('#' + select +' option').each(function(i, option){
				if(option.value != "")
					option.remove();
			});
			$('#' + select).selectpicker('refresh');
		})
		$('#target-ontology').val("");
		
	
	}
	
	var getTargetOntologyProperties = function(){
		$('#target-property option').each(function(i, option){
			if(option.value != "")
				option.remove();
		});
		var dstOnt = $('#target-ontology').val();
		var srcAtt = $('#self-property :selected').text()
		
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		$.ajaxSetup({'headers': {
			[csrf_header]: csrf_value
	    }});

		jQuery.get('/controlpanel/ontologies/' + dstOnt + '/properties/type/' + mapPropType[srcAtt], function(data){
			var properties = data;
			Object.keys(properties).forEach(function(key){
				$('#target-property').append($('<option>', {value: properties[key] , text: key }));
			});
			$('#target-property').selectpicker('refresh');
		});
		
	}
		
	// INSERT RELATION
	var insertRelation = function (srcAtt, dstOnt, dstAtt, srcAttPath, dstAttPath, validate){
		if(referencesIds.indexOf(srcAtt + dstOnt + dstAtt) == -1){
			var relation = { "srcAtt" : srcAtt, "dstOntology" : dstOnt, "dstAtt": dstAtt, "srcAttPath" : srcAttPath, "dstAttPath" : dstAttPath, "validate" : validate};
			referencesArr.push(relation);
			referencesIds.push(srcAtt + dstOnt + dstAtt);
			mountTableReferences();
		}
		
	}
	
	// DELETE RELATION
	var deleteRelation = function(obj){
		
		var srcAtt = $(obj).closest('tr').find("input[name='srcAtt\\[\\]']").val();
		var dstAtt = $(obj).closest('tr').find("input[name='dstAtt\\[\\]']").val();			
		var dstOnt = $(obj).closest('tr').find("input[name='dstOntology\\[\\]']").val();
		if(referencesIds.indexOf(srcAtt + dstOnt + dstAtt) > -1){
			referencesArr.forEach(function(reference, i){
				if(reference["srcAtt"] == srcAtt && reference["dstAtt"] == dstAtt && reference["dstOntology"] == dstOnt)
					referencesArr.splice(i, 1);
			});
			referencesIds.splice(referencesIds.indexOf(srcAtt + dstOnt + dstAtt),1);
		}
		
		mountTableReferences();
		
	}
	
	var mountTableReferences = function(){
		// TO-HTML
		if ($('#references').attr('data-loaded') === 'true'){
			$('#ontology_references > tbody').html("");
			$('#ontology_references > tbody').append(mountableModelLD);
		}
		console.log('referencesArr: ' + referencesArr.length + ' Arr: ' + JSON.stringify(referencesArr));
		$('#ontology_references').mounTable(referencesArr,{
			model: '.reference-model',
			noDebug: false							
		});

		$('#references').removeClass('hide');
		$('#references').attr('data-loaded',true);
	}

	// AUX. UPDATE SCHEMA FROM ADDITIONAL PROPERTIES, SCHEMA IS BASE CURRENT
	// SCHEMA LOADED
	var updateSchemaProperties = function(){
		logControl ? console.log('updateSchemaProperties() -> ') : '';

		// properties, types and required arrays
		var updateProperties = $("input[name='property\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();
		var updateTypes = $("select[name='type\\[\\]']").map(function(){return $(this).val();}).get();
		var updateRequired = $("select[name='required\\[\\]']").map(function(){return $(this).val();}).get();
		var updateDescription = $("input[name='descriptions\\[\\]']").map(function(){return $(this).val();}).get();
		var updateEncrypted = $("select[name='encrypted\\[\\]']").map(function(){return $(this).val();}).get();

		var schemaObj = {};

		logControl ? console.log('|--- CURRENT: ' + updateProperties + ' types: ' + updateTypes + ' required: ' + updateRequired + ' description: ' + updateDescription + ' Encrypted: ' + updateEncrypted ): '';

		checkUnique = updateProperties.unique();
		if (updateProperties.length !== checkUnique.length)  { $.alert({title: 'ERROR!', theme: 'light',  content: ontologyCreateReg.validations.duplicates}); return false; }

		// get current schema
		if (typeof schema == 'string'){
			schemaObj = JSON.parse(schema);

		}else if (typeof schema == 'object') { schemaObj = schema; } else { $.alert({title: 'ERROR!', theme: 'light', content: ontologyCreateReg.validations.tplschema}); return false; }


		// UPDATE SCHEMA , REMOVES REQUIRED, AND PROPERTIES, TO FILL AGAIN ALL
		// FIELDS. (FOR CHANGES)
		var backUpProperties = {};
		var backUpRequired = [];
		if(ontologyCreateReg.actionMode != null){
			if(schemaObj.hasOwnProperty('datos')){
				backUpProperties = schemaObj.datos.properties;
				backUpRequired = schemaObj.datos.required;
			}else{
				backUpProperties = schemaObj.properties;
				backUpRequired = schemaObj.required;
			}
		}
		if ( schemaObj.hasOwnProperty('datos') ){ schemaObj.datos.properties = {}; schemaObj.datos.required = []; } else { schemaObj.properties = {};  schemaObj.required = []; }
		
		schema = JSON.stringify(schemaObj);
		// Show modal if has properties of type array and object, and in create
		// Mode
		var showModalCompleteProperties = false;
		var complexProperties =[];
		// UPDATE ALL PROPERTIES EACH TIME.
		if ( updateProperties.length ){
			$.each(updateProperties, function( index, value ) {
				propIndex = updateProperties.indexOf(value);
				
				// In the meantime we implement generation of objects and
				// arrays, we must notify the user that
				// s/he has to complete this properties by hand
				if(updateTypes[propIndex] == "object" || updateTypes[propIndex] == "array"){
					if(ontologyCreateReg.actionMode != null) {
						// dont update mode= Update
						logControl ? console.log('Not updating index: ' + propIndex + ' | property: ' + updateProperties[propIndex] + ' type: ' + updateTypes[propIndex] + ' required: ' + updateRequired[propIndex] + 'description: ' + updateDescription[propIndex] + ' encrypted: ' + updateEncrypted[propIndex]) : '';
						if ( schemaObj.hasOwnProperty('datos') ){
							schemaObj.datos.properties[value] = backUpProperties[value];
							if(backUpRequired && backUpRequired.indexOf(value) > -1)
								schemaObj.datos.required.push(value);
						}else{
							schemaObj.properties[value] = backUpProperties[value];
							if( backUpRequired && backUpRequired.indexOf(value) > -1)
								schemaObj.required.push(value);
						}
						schema = JSON.stringify(schemaObj);
					}else{
						// update but notify user mode= Create
						updateProperty(updateProperties[propIndex], updateTypes[propIndex], updateRequired[propIndex], updateDescription[propIndex], updateEncrypted[propIndex] , schemaObj );
						logControl ? console.log('index: ' + propIndex + ' | property: ' + updateProperties[propIndex] + ' type: ' + updateTypes[propIndex] + ' required: ' + updateRequired[propIndex] + 'description: ' + updateDescription[propIndex] + ' encrypted: ' + updateEncrypted[propIndex]) : '';
						showModalCompleteProperties = true;
						complexProperties.push(updateProperties[propIndex]);
					}	
				}else{
						logControl ? console.log('index: ' + propIndex + ' | property: ' + updateProperties[propIndex] + ' type: ' + updateTypes[propIndex] + ' required: ' + updateRequired[propIndex] + 'description: ' + updateDescription[propIndex] + ' encrypted: ' + updateEncrypted[propIndex]) : '';
						// update property on Schema /current are stored in
						// schema var. (property,type,required)
						updateProperty(updateProperties[propIndex], updateTypes[propIndex], updateRequired[propIndex], updateDescription[propIndex], updateEncrypted[propIndex], schemaObj  );
					}
			});
		}
		// ADD additionalProperties, because we are adding properties.
		if (!schemaObj.hasOwnProperty('additionalProperties')){
			schemaObj["additionalProperties"] = true;
		}
	
		// remove required if empty
		if(schemaObj.required.length == 0){
			delete schemaObj.required;
			schema = JSON.stringify(schemaObj);
		}

		if (schemaObj.hasOwnProperty('datos') && schemaObj.datos.required.length == 0){
			delete schemaObj.datos.required;
		}
		
		// TO-DO : REVISAR
		/*
		 * if ( schemaObj.hasOwnProperty('datos') ){ if(schemaObj.datos.required !=
		 * undefined && schemaObj.datos.required.length == 0){ //There are not
		 * required fields --> remove required array to avoid errors logControl
		 * ?console.log('updateSchemaProperties() -> remove required array to
		 * avoid errors'): ''; var schemaJson = JSON.parse(schema);
		 * schemaJson.datos.required = []; schema = JSON.stringify(schemaJson);
		 * editor.setMode("text"); editor.setText(''); editor.setText(schema);
		 * editor.setMode("tree"); } }
		 */
		// HANDLE REFERENCES
		if(referencesArr.length > 0)
			createReferences(schemaObj);
		// ADD INFO TO SCHEMA EDITOR
		schema = JSON.stringify(schemaObj);
		editor.setMode("text");
        editor.setText('');
		editor.setText(schema);
		editor.setMode("tree");

			
		
		// UPDATING FORM FIELDS
		$('#jsonschema').val(schema);

		// CHANGE TO SCHEMA TAB.
		 $('.nav-tabs li a[href="#tab_4"]').tab('show');
		 
		 // SHOW THE MODAL FOR COMPLEX OBJECT/ARRAY
		 if(showModalCompleteProperties){
			 $('#complex-properties').text(complexProperties);
			 $('#modal-info-objects').modal("show");
		 }
	}


	// AUX. UPDATE PROPERTY IN SCHEMA FOR EACH NEW PROPERTY ADDED
	var updateProperty = function(prop, type, req, desc, encrypt, schemaObj){
		logControl ? console.log('|---   updateProperty() -> ') : '';

		var properties = [];
		var requires = [];
		var propString = '';
		var updDesc = desc !== '' ? '"description": "'+ desc + '",' : '';
		var updEncryp = encrypt === 'true' ? '"encrypted": '+ encrypt + ',' : '';


// if (typeof schema == 'string'){ data = JSON.parse(schema); }
// else if (typeof schema == 'object'){ data = schema; } else { $.alert({title:
// 'ERROR!', theme: 'dark', type: 'red', content:
// ontologyCreateReg.validations.noschema}); return false; }
		

		// SCHEMA MODEL ( PROPERTIES / DATOS)
		if ( schemaObj.hasOwnProperty('datos') ){ properties = schemaObj.datos.properties; requires = schemaObj.datos.required; } else { properties = schemaObj.properties;  requires = schemaObj.required }

		if(requires == null){
			requires = [];
			schemaObj.required = requires;
		}

		// ADD PROPERTY+TYPE
		if (type == 'timestamp-mongo'){
			propString = '{"type": "object", '+ updDesc +' '+ updEncryp +' "required": ["$date"],"properties": {"$date": {"type": "string","format": "date-time"}}}';
			properties[prop] = JSON.parse(propString);
		} else if(type == 'file') {
			properties[prop] = JSON.parse('{"type": "object", '+ updDesc +' '+ updEncryp +' "required": ["data","media"],"properties": {"data": {"type": "string"},"media": {"type": "object", "required": ["name","storageArea","binaryEncoding","mime"],"properties": {"name":{"type": "string"},"storageArea": {"type": "string","enum": ["SERIALIZED","DATABASE","URL"]},"binaryEncoding": {"type": "string","enum": ["Base64"]},"mime": {"type": "string","enum": ["application/pdf","image/jpeg", "image/png"]}}}},"additionalProperties": false}');
		}else if(type == 'geometry-point' || type == 'geometry'){
			properties[prop] = JSON.parse('{"type":"object",  '+ updDesc +' '+ updEncryp +' "required":["coordinates","type"],"properties":{"coordinates": {"type": "array","items": [{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}],"minItems": 2,"maxItems": 2},"type": {"type": "string","enum": ["Point"]}},"additionalProperties": false}');
		}else if(type == 'geometry-linestring'){
			properties[prop] = JSON.parse('{"type":"object",  '+ updDesc +' '+ updEncryp +' "required":["coordinates","type"],"properties":{"coordinates": {"type": "array", "minItems": 2, "items": [{"type": "array","minItems": 2, "maxItems": 2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]},{"type": "array","minItems": 2, "maxItems": 2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]}]},"type": {"type": "string","enum": ["LineString"]}},"additionalProperties": false}');
		}else if(type == 'geometry-polygon'){
			properties[prop] = JSON.parse('{"type":"object",  '+ updDesc +' '+ updEncryp +' "required":["coordinates","type"],"properties":{"coordinates": {"type": "array","items": [{"type": "array","minItems": 4, "items": [{"type":"array", "minItems": 2, "maxItems":2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]},{"type":"array", "minItems": 2, "maxItems":2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]},{"type":"array", "minItems": 2, "maxItems":2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]},{"type":"array", "minItems": 2, "maxItems":2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]} ]}]},"type": {"type": "string","enum": ["Polygon"]}},"additionalProperties": false}');
		}else if(type == 'geometry-multipoint'){
			properties[prop] = JSON.parse('{"type":"object",  '+ updDesc +' '+ updEncryp +' "required":["coordinates","type"],"properties":{"coordinates": {"type": "array","items": [{"type": "array", "minItems": 2, "maxItems": 2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]}]},"type": {"type": "string","enum": ["MultiPoint"]}},"additionalProperties": false}');
		}else if(type == 'geometry-multilinestring'){
			properties[prop] = JSON.parse('{"type":"object",  '+ updDesc +' '+ updEncryp +' "required":["coordinates","type"],"properties":{"coordinates": {"type": "array","items": [{"type": "array", "minItems": 2, "items": [{"type":"array", "minItems": 2,"maxItems": 2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]},{"type":"array", "minItems": 2,"maxItems": 2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]}]}] },"type": {"type": "string","enum": ["MultiLineString"]}},"additionalProperties": false}');
		}else if(type == 'geometry-multipolygon'){
			properties[prop] = JSON.parse('{"type":"object",  '+ updDesc +' '+ updEncryp +' "required":["coordinates","type"],"properties":{"coordinates": {"type": "array","items": [{"type": "array", "items": [{"type": "array","minItems": 4, "items": [{"type":"array", "minItems": 2, "maxItems":2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]},{"type":"array", "minItems": 2, "maxItems":2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]},{"type":"array", "minItems": 2, "maxItems":2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]},{"type":"array", "minItems": 2, "maxItems":2, "items":[{"type": "number","maximum": 180,"minimum": -180},{"type": "number","maximum": 90,"minimum": -90}]} ]}]}]},"type": {"type": "string","enum": ["MultiPolygon"]}},"additionalProperties": false}');
		}else if(type == 'timestamp'){
			properties[prop] = JSON.parse('{"type": "string","format": "date-time"}');
		}else if(type == 'date'){
			properties[prop] = JSON.parse('{"type": "string","format": "date"}');
		}else {
			propString = '{' + updDesc +' '+ updEncryp +' "type": "' + type + '"}';
			properties[prop] = JSON.parse(propString);

		}

		// ADD REQUIRED
		if (req == 'required') {
			console.log('required object: ' + JSON.stringify(prop));
			
			if (jQuery.inArray(prop, requires) == -1) requires.push(prop);
			
		}else if(requires.includes(prop)){
			var index = requires.indexOf(prop);
			if (index > -1) {
				requires.splice(index, 1);
			}
		}


		
		
		// remove required if empty
// if(schemaObj.required.length == 0)
// delete schemaObj.required;
//		
		
		// ADD INFO TO SCHEMA EDITOR
// editor.setMode("text");
// editor.setText('');
// editor.setText(JSON.stringify(data));
// editor.setMode("tree");

		// UDATING SCHEMA STRING
// schema = JSON.stringify(data);
			
		
		// UPDATING FORM FIELDS
// $('#jsonschema').val(schema);

	}


	// CHECK IF A WRITTEN PROPERTY IS OR NOT FROM THE BASE
	var	noBaseProperty =  function(property){
			logControl ? console.log(LIB_TITLE + ': noBaseProperty()') : '';

			var isNoBaseProperty = false;
			var noBaseJson = createJsonProperties(JSON.parse(staticSchema)); // to
																				// JSON
			var noBaseProperties = getProperties(noBaseJson); // only
																// Properties
																// Arr
			isNoBaseProperty = $.inArray( property, noBaseProperties ) > -1 ? false : true;
			return isNoBaseProperty;
	}

	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url;
	}


	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		logControl ? console.log('cleanFields() -> ') : '';

		// CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm();
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});

		// CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});

		// CLEANING CHECKS
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
            errorElement: 'span', // default input error message container
            errorClass: 'help-block help-block-error', // default input error
														// message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate
																		// all
																		// fields
																		// including
																		// form
																		// hidden
																		// input
																		// but
																		// not
																		// selectpicker
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
            invalidHandler: function(event, validator) { // display error
															// alert on form
															// submit
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
            unhighlight: function(element) { // revert the change done by
												// hightlight
                $(element).closest('.form-group').removeClass('has-error');
            },
            success: function(label) {
                label.closest('.form-group').removeClass('has-error');
            },
			// ALL OK, THEN SUBMIT.
            submitHandler: function(form) {

                error1.hide();
                // VALIDATE JSON SCHEMA
				validIdName = validateIdName();
				if (validIdName){

					// VALIDATE JSON SCHEMA
					validJsonSchema = validateJsonSchema();
					if (validJsonSchema){

						// VALIDATE TAGSINPUT
						validMetaInf = validateTagsInput();
						if (validMetaInf) {
							// form.submit();
							form1.ajaxSubmit({type: 'post', success : function(data){
								if(ontologyCreateReg.actionMode != null)
									navigateUrl(data.redirect);
								else
									$('#modal-created').modal('show');
								}, error: function(data){
									HeaderController.showErrorDialog(data.responseJSON.cause)
								}
							})
						}
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
			$.alert({title: 'INFO!',  theme: 'light', content: ontologyCreateReg.validations.authinsert});
			return false;
		  }
		});
		
		// authorization tab control
		$(".nav-tabs a[href='#tab_kpi']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			$.alert({title: 'INFO!',  theme: 'light', content: ontologyCreateReg.validations.notkpi});
			return false;
		  }
		});


		// INPUT MASK FOR ontology identification allow only letters, numbers
		// and -_
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
		
		$("#rtdbInstance").on('change', function(){
			if(this.value == "KUDU"){
				$("#kuduProps").removeClass("hidden");
			}
			else if(this.value == "COSMOS_DB"){
				$(".cosmosProps").removeClass("hidden");
			}else{
				$(".cosmosProps").addClass("hidden");
				$("#kuduProps").addClass("hidden");
			}
		});
		
		$("#enablePartitionIndexes").on('click', function(){
			if($('#enablePartitionIndexes').is(':checked')){
				$(".kuduAdvancedProps").removeClass("hidden");
			}
			else{
				$(".kuduAdvancedProps").addClass("hidden");
			}
		});
		

		// INSERT MODE ACTIONS (ontologyCreateReg.actionMode = NULL )
		if ( ontologyCreateReg.actionMode === null){
			logControl ? console.log('|---> Action-mode: INSERT') : '';

			// Set active
			$('#active').trigger('click');
			

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

					// AUTHs-table {"users":user,"accesstypes":accesstype,"id":
					// response.id}
					authorizationUpdate = {"users": userid_update, "accesstypes": accesstype_update, "id": authid_update};
					authorizationsArr.push(authorizationUpdate);

					// AUTH-Ids {[user_id]:auth_id}
					authorizationIdUpdate = {[userid_update]:authid_update};
					authorizationsIds.push(authorizationIdUpdate);

					// disable this users on users select
					$("#users option[value='" + userid_update + "']").prop('disabled', true);
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

			// overwrite datamodel schema with loaded ontology schema generated
			// with this datamodel template.
			var theSelectedModel = $("li[data-model='"+ ontologyCreateReg.dataModelEditMode +"']");
			var theSelectedModelType = theSelectedModel.closest('div .panel-collapse').parent().find("a").trigger('click');
			if(schema==null || schema!='{}')
				theSelectedModel.attr('data-schema',schema).trigger('click');
			else
				theSelectedModel.trigger('click');
			
			

		}
	}


	// DATAMODEL TEMPLATE COUNTERS
	var dataModeltemplateCounters = function(){

		var datamodels = $('.datamodel-types');
		datamodels.each(function(ind,elem){
			var templates = $(elem).find('ul.datamodel-template').length;
			var typeHref = $(elem).find('a.list-toggle-container > div.list-toggle');
			$('<span class="pull-right badge badge-success">'+ templates +'</span>').appendTo(typeHref);
			if (templates == 0 ) { $(elem).find('div.list-toggle').removeClass('bg-blue-hoki font-grey-carrara').addClass('bg-grey-steel font-grey-cascade hide');	}
		});
	}


	// DELETE ONTOLOGY
	var deleteOntologyConfirmation = function(ontologyId){
		console.log('deleteOntologyConfirmation() -> formId: '+ ontologyId);

		// no Id no fun!
		if ( !ontologyId ) {$.alert({title: 'ERROR!',  theme: 'light', content: ontologyCreateReg.validations.validform}); return false; }

		logControl ? console.log('deleteOntologyConfirmation() -> formAction: ' + $('.delete-ontology').attr('action') + ' ID: ' + $('#delete-ontologyId').attr('ontologyId')) : '';

		// call ontology Confirm at header.
		HeaderController.showConfirmDialogOntologia('delete_ontology_form');
	}


	// CREATE EDITOR FOR JSON SCHEMA
	var createEditor = function(){
		logControl ? console.log('|--->   createEditor()') : '';
		var container = document.getElementById('jsoneditor');
		var options = {
			mode: 'code',
			theme: 'bootstrap3',
			required_by_default: true,
			modes: ['text', 'tree', 'view'], // allowed modes
			error: function (err) {
				$.alert({title: 'ERROR!', theme: 'light', content: err.toString()});
				return false;
			},
			onChange: function(){

				console.log('se modifica el editor en modo:' + editor.mode + ' contenido: ' + editor.getText());
			}
		};
		editor = new jsoneditor.JSONEditor(container, options, "");
	}


	// CHECK IF JSON STRING WHEN JSON PARSE IS OK OR NOT, IF THE JSON IS
	// MALFORMED THE JSON.parse() is broken.
	var IsJsonString = function(str) {
		try {
			JSON.parse(str);
		}
		catch (e) {	return false; }

		return true;
	}


	// VALIDATE TAGSINPUT
	var validateIdName = function(){
		var error1 = $('.alert-danger');
		error1.show();
		if ($('#identification').val().match(/^[0-9]/)) { $('#identificationerror').removeClass('hide').addClass('help-block-error font-red'); App.scrollTo(error1, -200);return false;  } else { return true;}
	}


	// JSON SCHEMA VALIDATION PROCESS
	var validateJsonSchema = function(){
        logControl ? console.log('|--->   validateJsonSchema()') : '';

		if(IsJsonString(editor.getText())){

			var isValid = true;

			// obtener esquemaOntologiaJson
			var ontologia = JSON.parse(editor.getText());

			if((ontologia.properties == undefined && ontologia.required == undefined)){

				$.alert({title: 'JSON SCHEMA!',  theme: 'light', content: ontologyCreateReg.validations.schemaprop});
				isValid = false;
				return isValid;

			}else if( ontologia.properties == undefined && (ontologia.additionalProperties == null || ontologia.additionalProperties == false)){

				$.alert({title: 'ERROR JSON SCHEMA!',  theme: 'light', content: ontologyCreateReg.validations.schemanoprop});
				isValid = false;
				return isValid;

			}else{

				// Situarse en elemento raiz ontologia.properties (ontologia) o
				// ontologia.datos.properties (datos)
				var nodo;

				if(jQuery.isEmptyObject(ontologia.properties)){
					 // esquema sin raiz
					 nodo=ontologia;
				}else{
					for (var property in ontologia.properties){

						var data = "";
						// Se comprueba si dispone de un elemento raiz
						if (ontologia.properties[property] && ontologia.properties[property].$ref){

							// Se accede al elemento raiz que referencia el obj
							var ref = ontologia.properties[property].$ref;
							ref = ref.substring(ref.indexOf("/")+1, ref.length);
							nodo = ontologia[ref];

						} else {
							// esquema sin raiz
							nodo = ontologia;
						}
					}
				}
				// Plantilla EmptyBase: se permite crear/modificar si se cumple
				// alguna de estas condiciones:
				// a. Hay al menos un campo (requerido o no requerido)
				// b. No hay ningún campo (requerido o no requerido) pero
				// tenemos el AditionalProperties = true
				// Resto de casos: Con que haya al menos un campo (da igual que
				// sea requerido o no requerido) o el AditionalProperties =
				// true, se permite crear/actualizar el esquema de la ontología.

				// Nodo no tiene valor
				if( (nodo == undefined)){

					 $.alert({title: 'JSON SCHEMA!',  theme: 'light', content: 'NO NODE!'});
					  isValid = false;
					  return isValid;

				// Propiedades no definida y additionarProperteis no esta
				// informado a true
				}else  if(  (nodo.properties ==undefined || jQuery.isEmptyObject(nodo.properties))  && (nodo.additionalProperties == null || nodo.additionalProperties == false)){

					$.alert({title: 'JSON SCHEMA!', theme: 'light', content: ontologyCreateReg.validations.noproperties});
					isValid = false;
					return isValid;
				}
				// Validaciones sobre propiedas y requeridos
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
									$.alert({title: 'JSON SCHEMA!',  theme: 'light', content: ontologyCreateReg.validations.schemanoprop});
									isValid = true;
								 }
						}
						else{
							$.alert({title: 'JSON SCHEMA !',  theme: 'light', content: ontologyCreateReg.validations.noproperties});
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
			$.alert({title: 'JSON SCHEMA!',  theme: 'light', content: ontologyCreateReg.validations.noschema});
			return isValid;

		}
		console.log('JSON SCHEMA VALIDATION: ' + isValid);
		return isValid;
	}


	// VALIDATE TAGSINPUT
	var validateTagsInput = function(){
		var error1 = $('.alert-danger');
		// error1.show();
		if ($('#metainf').val() === '') { $('#metainferror').removeClass('hide').addClass('help-block-error font-red'); App.scrollTo(error1, -200);return false;  } else { return true;}

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
		if(IsJsonString(editor.getText()) && editor.getText() != "{}"){

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

					// Se genera la seccion correspondiente a cada propiedad del
					// elemento de referencia
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
			$("#ontology_instance").val("");
			$("#ontology_instance").val(instance);

			if (ontologyJson.properties == null ){
                	document.getElementById("ontology_instance").innerHTML = "";
			}

		}
		else {
			// no JSON no fun!
			$.alert({title: 'JSON SCHEMA!', theme: 'light', content: ontologyCreateReg.validations.noschema});
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
            if (tipo.includes("geometry")){ instance = instance + generateBasicType(tipo, "", "");
            // adding object type
            } else if (tipo.toLowerCase() == "object"){ console.log('INSTANCE (obj): ' + instance); instance = instance + generateObject(property, "", propertyName);
			// adding array type
            } else if (tipo.toLowerCase() == "array" ){ console.log('INSTANCE (arr): ' + instance); instance = instance + generateArray(property, "", propertyName);
            // date
            } else if (tipo.toLowerCase() == "string" && property.format == "date"){
            	instance = instance +"\"2014-01-30\""; 
            // timestamp
            } else if (tipo.toLowerCase() == "string" && property.format != null){
            	instance = instance +"\"2014-01-30T17:14:00Z\""; 
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
		else if ( propType == "geometry-point" || obj =="geometry-point"){ return "{\"type\":\"Point\", \"coordinates\":[9,19.3]}"; }
		else if ( propType == "geometry-linestring" || obj =="geometry-linestring"){ return "{\"type\":\"LineString\", \"coordinates\":[[9,19.3],[19,9.3]]}"; }
		else if ( propType == "geometry-polygon" || obj =="geometry-polygon"){ return "{\"type\":\"Polygon\", \"coordinates\":[ [[9,-19.3],[-19,-9.3],[-9,19.3],[19,-9.3]] ,[[9,19.3],[19,9.3],[-9,19.3],[-19,9.3]] ]}"; }
		else if ( propType == "geometry-multipoint" || obj =="geometry-multipoint"){ return "{\"type\":\"MultiPoint\", \"coordinates\":[[9,19.3],[19,19.3]]}"; }
		else if ( propType == "geometry-multilinestring" || obj =="geometry-multilinestring"){ return "{\"type\":\"MultiLineString\", \"coordinates\":[ [[9,19.3],[19,9.3]], [[9,19.3],[19,9.3]] ]}"; }
		else if ( propType == "geometry-multipolygon" || obj =="geometry-multipolygon"){ return "{\"type\":\"MultiPolygon\", \"coordinates\":[[[[9,2.5],[9,2],[-9,3],[9,-3],[-9,2]]],[[[9,0],[9,-0.5],[9,-1.5],[9,1],[9,0.4]],[[9,0.2],[-9,0.2],[9,0.8],[-9,0.8],[9,3.2]]]]}"; }
		else if ( propType == "number"   || propType == "numeric" ){	return "28.6" }
		else if ( propType == "array") {return ""; }
    }


	// GENERARATE PROPERTY TYPES [GEOMETRY, OBJECT, ARRAY ]
	var generateObject = function(ontology, instance, parent){
        logControl ? console.log('        |--->   generateObject()') : '';

       	instance = "{";
       	if ( ontology.properties ){
	        for ( var obj in ontology.properties ){

				var objtype = ontology.properties[obj].type;
	             // if obj <> date or geometry, iterates recursive for
					// treatment.
	             if ((objtype.toLowerCase() == "object") && (obj != "geometry") && ontology.properties[obj].properties && ontology.properties[obj].properties.$date == null ){
					logControl ? console.log('        |--->   generateObject() --> object: ' + JSON.stringify(ontology.properties[obj])) : '';
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
				 else if (ontology[obj] && ontology[obj].properties &&  ontology[obj].properties[propertyName].properties && ontology[obj].properties[propertyName].properties.type && ontology.properties[obj].properties.type.enum[0]== "LineString"){
	                 instance = instance + "\"" +obj+"\":"+ "{\"type\":\"LineString\", \"coordinates\":[[9,19.3],[19,9.3]]}";

	             }
				 else if (ontology[obj] && ontology[obj].properties &&  ontology[obj].properties[propertyName].properties && ontology[obj].properties[propertyName].properties.type && ontology.properties[obj].properties.type.enum[0]== "Polygon"){
	                 instance = instance + "\"" +obj+"\":"+ "{\"type\":\"Polygon\", \"coordinates\":[ [[9,-19.3],[-19,-9.3],[-9,19.3],[19,-9.3]] ,[[9,19.3],[19,9.3],[-9,19.3],[-19,9.3]] ]}";

	             }
				 else if (ontology[obj] && ontology[obj].properties &&  ontology[obj].properties[propertyName].properties && ontology[obj].properties[propertyName].properties.type && ontology.properties[obj].properties.type.enum[0]== "MultiPoint"){
	                 instance = instance + "\"" +obj+"\":"+ "{\"type\":\"MultiPoint\", \"coordinates\":[[9,19.3],[19,19.3]]}";

	             }
				 else if (ontology[obj] && ontology[obj].properties &&  ontology[obj].properties[propertyName].properties && ontology[obj].properties[propertyName].properties.type && ontology.properties[obj].properties.type.enum[0]== "MultiLineString"){
	                 instance = instance + "\"" +obj+"\":"+ "{\"type\":\"MultiLineString\", \"coordinates\":[ [[9,19.3],[19,9.3]], [[9,19.3],[19,9.3]] ]}";

	             }
				 else if (ontology[obj] && ontology[obj].properties &&  ontology[obj].properties[propertyName].properties && ontology[obj].properties[propertyName].properties.type && ontology.properties[obj].properties.type.enum[0]== "MultiPolygon"){
	                 instance = instance + "\"" +obj+"\":"+ "{\"type\":\"MultiPolygon\", \"coordinates\":[[[[9,2.5],[9,2],[-9,3],[9,-3],[-9,2]]],[[[9,0],[9,-0.5],[9,-1.5],[9,1],[9,0.4]],[[9,0.2],[-9,0.2],[9,0.8],[-9,0.8],[9,3.2]]]]}";

	             }
				// array
				 else if (objtype.toLowerCase() == "array"){
					logControl ? console.log('        |--->   generateObject() --> array: ' + JSON.stringify(ontology.properties[obj])) : '';
					instance = instance + "\""+ obj + "\":" + generateArray(ontology.properties[obj], "", obj);
	             }
				 else if (obj.format != null){
					 instance = instance +"\"" +obj+"\":\"2014-01-30T17:14:00Z\"}"; 
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

		if (!ontology.hasOwnProperty('items')){
			instance = instance + "[]";
			return instance;
		}

        if (ontology.minItems != null) {
            minItems =  ontology.minItems;
        }
        instance = instance + "[";


		// main array iteration
		for (var i=0; i <= minItems-1; i++) {
			// object item
			if (ontology.items[i].type.toLowerCase() == "object"){
				instance = instance + generateObject(ontology.items[i], "", parent);
                if (i < minItems -1){
                    instance = instance + ",";
                }
			}
			else{
			// non object item
				var valor = "";
                if (ontology.items[i].enum != null){
                    valor = ontology.items[i].enum;
                }
                if(ontology.items[i].type!="array")
                	instance = instance + generateBasicType(ontology.items[i].type, "", "", valor);
                else
                	instance = instance + generateArray(ontology.items[i], "", "");
                if (i < minItems -1){
                    instance = instance + ",";
                }

			}
		}

        return instance + "]";
	};


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
					// store ids for after actions. inside callback
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
					$("#users option[value='" + $('#users').val() + "']").prop('disabled', true);
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
						$("#users option[value='" + user + "']").prop('disabled', false);
						$("#users").selectpicker('deselectAll');
						$("#users").selectpicker('refresh');
						if (authorizationsArr.length == 0){
							$('#alert-authorizations').toggle(!$('#alert-authorizations').is(':visible'));
							$('#authorizations').addClass('hide');

						}

					}
					else{
						$.alert({title: 'ALERT!', theme: 'light',  content: 'NO RESPONSE!'});
					}
				}
			});
		}
	};

	var checkRtdbClean = function(){
		if($('#rtdbclean').is(':checked')){
			$('#rtdb2h').removeClass('hide');
			if($('#rtdbCleanLapseOpt').val() == 'NEVER')
				$('#rtdbCleanLapseOpt').val('ONE_MONTH').change();
		}else{
			$('#rtdb2h').addClass('hide');
			if($('#rtdbCleanLapseOpt').val() != 'NEVER')
				$('#rtdbCleanLapseOpt').val('NEVER').change();
		}
	}
	var checkRtdb2H = function(){
		if($('#rtdbToHdb').is(':checked')){
			$('#select-rtdb2h').removeClass('hide');
			$('#rtdbclean').prop('disabled', true);
		}else{
			$('#select-rtdb2h').addClass('hide');
			$('#rtdbclean').prop('disabled', false);
			
		}
	}
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
			dataModeltemplateCounters();
			mountReferenceInitialTable();
			//TAGSINPUT MAX TAGS
			$('#partitionkey').tagsinput({
				  maxTags: 1
			});
			// SET DEFAULT DATAMODEL TO EMPTYBASE
			$('#datamodelid').val($('#EmptyBase li').attr('data-model'));

			// IF rtdbclean is selected, click on RTDB2H with Lapse of one month
			// and viceversa
			$('#rtdbclean').on('click', function(){
				checkRtdbClean();
			});
			// IF RTDB2H is selected, click on rtdbclean + disable and viceversa
			$('#rtdbToHdb').on('click', function(){
				checkRtdb2H();
				
			});
			checkRtdbClean();
			checkRtdb2H();
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

		// REMOVE PROPERTYS (ONLY ADDITIONAL NO BASE)
		removeProperty: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeProperty()') : '';

			var remproperty = $(obj).closest('tr').find("input[name='property\\[\\]']").val();
			
			if (( remproperty == '')||( noBaseProperty(remproperty))){ 
				$(obj).closest('tr').remove();
			}
			else { 
				$.alert({title: 'ALERT!', theme: 'light',  content: ontologyCreateReg.validations.base}); 
			}
		},

		// CHECK FOR NON DUPLICATE PROPERTIES
		checkProperty: function(obj){			
			obj.value = obj.value.replace(/\s|["]|[/]|[\\]|[.]|[*]|[<]|[>]|[:]|[|]|[?]|\$/g,'')
			logControl ? console.log(LIB_TITLE + ': checkProperty()') : '';
			var allProperties = $("input[name='property\\[\\]']").map(function(){return $(this).val();}).get();
			areUnique = allProperties.unique();
			if (allProperties.length !== areUnique.length)  {
				$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: ontologyCreateReg.validations.duplicates});
				$(obj).val(''); return false;
			}
			else {
				$(obj).closest('tr').find('.btn-mountable-remove').attr('data-property', $(obj).val() );
			}
		},

		// UPDATE HIDDEN INPUT--> IF USER WANTS TO CHANGE ONTOLOGY TITLE FOR
		// EXAMPLE
		updateJsonschemaInput: function(){
			$('#jsonschema').val(editor.getText());
		},




		// CHECK PROPERTIES TYPE
		checkType: function(obj){
			logControl ? console.log(LIB_TITLE + ': checkType()') : '';
			var propType = '';
			var currentTypeValue = $(obj).val();
			var currentType = currentTypeValue.toLowerCase();
			// if type is a valid type, assign this value , if not, string by
			// default.
			propType = $.inArray( currentType, validTypes ) > -1 ?  currentType : 'string';
			logControl ? console.log('checkType: ' +propType ) : '';
			$(obj).val(propType);
		},

		// CHECK PROPERTIES to be REQUIRED or NOT
		checkRequired: function(obj){
			logControl ? console.log(LIB_TITLE + ': checkRequired()') : '';
			var propRequired = '';
			var currentRequiredValue = $(obj).val();
			var currentRequired = currentRequiredValue.toLowerCase();
			// if type is a required field, assign this value , if not, '' by
			// default, (not required).
			propRequired = currentRequired == 'required' ?  currentRequired : '';
			$(obj).val(propRequired);
		},

		// DATAMODEL PROPERTIES JSON TO HTML
		schemaToTable: function(objschema,tableId){
			logControl ? console.log(LIB_TITLE + ': schemaToTable()') : '';

			var data, properties, jsonProperties = '';

			// JSON-STRING SCHEMA TO JSON
			schema = $(objschema).attr('data-schema');
			
			// IF EMPTYBASE REPLACE WITH ONTOLOGY IDENTIFICATION
			if(objschema.dataset.schema.indexOf("EmptyBase") > -1)
				schema = schema.replace(/EmptyBase/g,$('#identification').val());
			// base schema with no modifications (used on base properties
			// validation )
			staticSchema = $(objschema).attr('data-schema');
			if 		(typeof schema == 'string'){ data = JSON.parse(schema); }
			else if (typeof schema == 'object'){ data = schema; } else { $.alert({title: 'ERROR!', theme: 'light',  content: ontologyCreateReg.validations.noschemaontologyCreateReg.validations.noschema}); return false; }

			// needs Ontology name and description to Run.
			if (($('#identification').val() == '') || ($('#description').val() == '')){
				$.alert({title: ontologyCreateReg.datamodel, theme: 'light',  content: ontologyCreateReg.dataModelSelection}); return false;
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
			jsonProperties = createJsonProperties(properties);

			// CHECK IF WE HAVE A DATAMODEL LOADED YET... o-DO: make confirm.
			if ( $('#datamodel_properties').attr('data-loaded') == 'true' ){

				$.confirm({ title: ontologyCreateReg.confirmBtn, theme: 'light', content: ontologyCreateReg.validations.datamodelchange,
					buttons: {
						
							cancel: {
							
							btnClass: 'btn-circle btn-outline blue',
							action:function () { return true; }
						},
						confirm: {
							
							btnClass: 'btn-circle btn-outline btn-primary',							
							action: function () {

								$('#datamodel_properties > tbody').html("");
								$('#datamodel_properties > tbody').append(mountableModel);
								editor.setMode("text");
								editor.setText('{}');
								editor.setMode("tree");
	
								// TO-HTML
								$('#'+tableId).mounTable(jsonProperties,{
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
	
								// UPDATING JSON EDITOR
								$('#schema_title').text(data.title + ':');
								editor.setMode("text");
								editor.setText(schema);
								editor.setMode("tree");
	
								// UPDATING FORM FIELDS
								$('#jsonschema').val(schema);
	
								return true;
							}
						}
					}
						
				});
			} else {

				// TO-HTML
				$('#'+tableId).mounTable(jsonProperties,{
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

			}


			// HIGHLIGHT CURRENT DATAMODEL AND SHOW TABLE
			$('li.mt-list-item.datamodel-template').removeClass('bg-success done');
			$(objschema).closest('li').addClass('bg-success done');

			$('#template_schema').removeClass('hide');
			$('#datamodel_properties').attr('data-loaded', true);

			// INIT UPDATE SCHEMA
			$('#btn-updateSchema').on('click',function(){ updateSchemaProperties(); });

			// INIT BTN LINKED DATA
			$('#btn-ld').on('click',function(){ mountLDModal(); });
			
			// UPDATING JSON EDITOR
			$('#schema_title').text(data.title + ':');
			editor.setMode("text");
			editor.setText(schema);
			editor.setMode("tree");

			// UPDATING DATAMODEL ID for ONTOLOGY
			$('#datamodelid').val($(objschema).attr('data-model'));

			// UPDATING FORM FIELDS
			$('#jsonschema').val(schema);

			// HIDE ERROR FOR DATAMODEL NOT SELECTED IF IT WAS VISIBLE
			$('#datamodelError').addClass('hide');

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

		// INSERT AUTHORIZATION
		insertAuthorization: function(){
			logControl ? console.log(LIB_TITLE + ': insertAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){
				// UPDATE MODE ONLY AND VALUES on user and accesstype
				if (($('#users').val() !== '') && ($("#users option:selected").attr('disabled') !== 'disabled') && ($('#accesstypes').val() !== '')){

					// AJAX INSERT (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns
					// object with data.
					authorization('insert',ontologyCreateReg.ontologyId,$('#users').val(),$('#accesstypes').val(),'');

				} else {  $.alert({title: 'ERROR!', theme: 'light',  content: ontologyCreateReg.validations.authuser}); }
			}
		},

		// REMOVE authorization
		removeAuthorization: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){

				// AJAX REMOVE (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns
				// object with data.
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

				// AJAX UPDATE (ACTION,ONTOLOGYID,USER,ACCESSTYPE,ID) returns
				// object with data.
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
		// VALIDATE LAPSE FOR CLEANING INSTANCES
		setRtdbCleanLapse: function(){
			var optSelected = $('#rtdbCleanLapseOpt').val();
			$('#rtdbCleanLapse').val(optSelected);
			if(optSelected != 'Never'){
				$('#rtdbClean').val(true);
			}else{
				$('#rtdbClean').val(false);
			}

		},
		insertRelation : function(){
			var srcProperty = $('#self-property :selected').text();
			var srcPropertyPath = $('#self-property').val();
			var dstOntology = $('#target-ontology').val();
			var dstProperty = $('#target-property :selected').text();
			var dstPropertyPath = $('#target-property').val();
			var validate = $('#validate-property').val();
			if(dstOntology != "" && dstProperty != "")
				insertRelation(srcProperty, dstOntology, dstProperty, srcPropertyPath, dstPropertyPath, validate);
			// TODO: else alert red selections
			
		},
		deleteRelation : function(object){
			deleteRelation(object);
		},
		getTargetOntologyProperties : function(){
			getTargetOntologyProperties();
		},
		enableComboOntologies : function(){
			if($('#self-property').val() != ''){
				$('#target-ontology').prop( "disabled", false);
			}else{
				$('#target-ontology').prop( "disabled", true);
			}
			$('#target-ontology').selectpicker('refresh');

		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// GLOBAL JSON AND CODE EDITOR INSTANCES
	var editor;
	var aceEditor;
	var schema = ''; // current schema json string var
	var staticSchema = ''; // current schema json string but static just for
							// original fields validation.

	// if redirect from error, change editmode for loading the schema
	if(ontologyCreateJson.actionMode == null && headerReg.errores != null && ontologyCreateJson.schemaEditMode != null ){
		ontologyCreateJson.actionMode = 1;
		ontologyCreateJson.authorizations = [];
		console.log(ontologyCreateJson);
	}

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	OntologyCreateController.load(ontologyCreateJson);

	// AUTO INIT CONTROLLER.
	OntologyCreateController.init();
});
