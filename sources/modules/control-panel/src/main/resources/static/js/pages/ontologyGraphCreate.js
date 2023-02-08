var createKeyColumns 		= []; // object to receive key fields to create table
var createColumns 			= {'vertex':[], 'edge':[]}; // object to receive fields to create table

var form1 = $('#ontology_create_form');

var wizardStep = 1;

var OntologyCreateGraphController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Ontology Controller';	
    var logControl = 1;
	var LANGUAGE = ['es']
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var validTypes = ["string","int","double","float"]; // Valid property types	
	var validIdName = false;
	var validJsonSchema = false;
	var validMetaInf = false;
	var hasId = false; // instance
	var csrf_value = $("meta[name='_csrf']").attr("content");
	var csrf_header = $("meta[name='_csrf_header']").attr("content");

	
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
				
		if ($("#allowsCreateTable").is(':checked')) {
			$("#datasources").prop('disabled', false);
			$('#fieldCreationForm').hide();
			$('#constraintsCreationForm').hide();
			$('#selectTable').show();
			$('#selectTableAssociated').show();
			$('#asociateIdTableCheck').show();
			$('#asociateIdTable').show();
			$('#asociateGeometryTableCheck').show();
			$('#asociateGeometryTable').show();
			$('#fieldsFromExistingTable').show();
			$('#sqlEditorRow').hide();
			$('#saveSqlCode').hide();
			sqlEditorRow.readOnly = true;
			identification.value = "";
			identification.readOnly = true;
			if (collections.value ==="") {
				identification.value = "";
			} else {
				identification.value = collections.value;
			}
			
			createKeyColumns 		= [];
			createColumns 			= [];
			$('#field_properties_vertex > tbody').empty();
			$('#field_properties_edge > tbody').empty();
		}
		
		//CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');

		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		$('.tagsinput').prev().removeClass('tagsinput-has-error');
		$('.tagsinput').nextAll('span:first').addClass('hide');
				
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
		if ($('#identification').val().match(/^[0-9]/)) { 
			$('#identificationerror').removeClass('hide').addClass('help-block-error font-red'); 
			return false; 
		} else {
			return true;
		}
	}

	// VALIDATE TAGSINPUT
	var validateMetaInf = function(){
		if ($('#metainf').val() === ''){
			$('#metainf').prev().addClass('tagsinput-has-error');
			$('#metainf').nextAll('span:first').removeClass('hide');
			return false;
		} else {
			return true;
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
			return (
					$('#identification').val().length >= 5 && 
					$('#description').val().length >= 5 && 
					$('#metainf').val().length >= 5 &&
					($('#partitions').val()==null || ($('#partitions').val() > 0 &&  $('#partitions').val() <= 15)) &&
					(($('#partitions').val()==null && $('#replicas').val()==0) ||  ($('#replicas').val() > 0 && $('#replicas').val() < 2)));
		} else if (wizardStep == 2){
			return ($('#jsonschema').val()!=null && $('#jsonschema').val()!="");
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
			$('#tab-table-schema a').removeClass('disabled');
			$('#tab-table-schema a').click();
			$('#continueBtn').addClass('hide');
			$('#createWizardBtn').removeClass('hide');
			$('#createWizardBtn').prop('disabled', false);			
			wizardStep = 2;
		} 
	}

	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation

					
		// set current language
		currentLanguage = ontologyCreateReg.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// validation rules
            rules: {
				ontologyId:		{ minlength: 5, required: true },
				identification:	{ minlength: 5, required: true },
				description:	{ minlength: 5, required: true }
            },
			// custom messages
            messages: {	
			},
            invalidHandler: function(event, validator) { //display error alert on form submit              
                validateSpecialComponentsAndSubmit(false);
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
            	validateSpecialComponentsAndSubmit(true);
            	return false;
			}
        });
    }
	
	var validateSpecialComponentsAndSubmit = function (submit) {
        // VALIDATE IDENTIFICATION METAINF AND SCHEMA
		let payload = preparePayload();
		let url = ontologyCreateReg.createURL;
		fetch(url,{
			'method': 'POST',
			'body': JSON.stringify(payload),
			'headers': {
				'Content-Type' : 'application/json'
			}
		})
		.then( r => {
			if (r.status !== 200 ) {
				throw Error(response.statusText);
			}
			toastr.info(messagesForms.operations.genOpSuccess,`OK`);
			window.location.href= ontologyCreateReg.ontologies;
		})
		.catch(e => console.log("Waiting for background execution to finish"))
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

		$(".option a[href='#tab_1']").on("click", function(e) {
	        $('.tabContainer').find('.option').removeClass('active');
	        $('#tab-datos').addClass('active');
	    });
		
		// general template/schema tab control
		$(".option a[href='#tab_table_schema']").on("click", function(e) {
	        $('.tabContainer').find('.option').removeClass('active');
	        $('#tab-table-schema').addClass('active');
	    });
		
		
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
		$(".wizard-option a[href='#tab_table_schema']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
				e.preventDefault();
				return false;
		  } else {
	        $('.wizardContainer').find('.wizard-option').removeClass('active');
	        $('#tab-table-schema').addClass('active');
	        
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

		
		// INSERT MODE ACTIONS  (ontologyCreateReg.actionMode = NULL ) 
		logControl ? console.log('|---> Action-mode: INSERT') : '';

					
	}	

	// VALIDATE TAGSINPUT
	var validateTagsInput = function(){		
		if ($('#metainf').val() === '') { $('#metainferror').removeClass('hide').addClass('help-block-error font-red'); return false;  } else { return true;} 
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
	
	var preparePayload = function(){
		let name = $('#identification').val();
		let metainf = $('#metainf').val();
		let description = $('#description').val();
		let partitions = $('#partitions').val();
		let replicas = $('#replicas').val();
		let payload = {
			name,
			metainf,
			description,
			partitions,
			replicas,
			'tags' : [],
			'edges' : []
		}
		let vertexAdded = [];
		let edgeAdded = [];
		createColumns['vertex'].forEach( v => {
			let i = vertexAdded.indexOf(v.entityName);
			let eName = v.entityName;
			let fName = v.fieldName;
			let fType = v.fieldType; 
			let field = {}
			field[fName] = fType
			if(i === -1){
				if(fName === ""){
					field = {};
				}
				payload['tags'].push({'name':eName, 'tagAttributes': field})
				vertexAdded.push(eName);
			}else{
				payload['tags'].forEach( e =>{
					if(e.name === v.entityName && fName !== ""){
						e['tagAttributes'][fName]= fType;
					}
				})
			}
		})
		createColumns['edge'].forEach( v => {
			let i = edgeAdded.indexOf(v.entityName);
			let eName = v.entityName;
			let fName = v.fieldName;
			let fType = v.fieldType;
			let field = {}
			field[fName] = fType
			if(i === -1){
				if(fName === ""){
					field = {};
				}
				payload['edges'].push({'name':eName, 'edgeAttributes': field})
				edgeAdded.push(eName);
			}else{
				payload['edges'].forEach( e =>{
					if(e.name === v.entityName && fName !== ""){
						e['edgeAttributes'][fName]= fType;
					}
				})
			}
		})
		
		console.log('final payload is -->', JSON.stringify(payload));
		return payload;
	}
	
	var preparePayloadUpdate = function(){
		let identification = $('#identification').val();
		let payload = {
			identification,
			'tags' : [],
			'edges' : []
		}
		let vertexAdded = [];
		let edgeAdded = [];
		createColumns['vertex'].forEach( v => {
			let i = vertexAdded.indexOf(v.entityName);
			let eName = v.entityName;
			let fName = v.fieldName;
			let fType = v.fieldType; 
			let field = {}
			field[fName] = fType
			if(i === -1){
				if(fName === ""){
					field = {};
				}
				payload['tags'].push({'name':eName, 'tagAttributes': field})
				vertexAdded.push(eName);
			}else{
				payload['tags'].forEach( e =>{
					if(e.name === v.entityName && fName !== ""){
						e['tagAttributes'][fName]= fType;
					}
				})
			}
		})
		createColumns['edge'].forEach( v => {
			let i = edgeAdded.indexOf(v.entityName);
			let eName = v.entityName;
			let fName = v.fieldName;
			let fType = v.fieldType;
			let field = {}
			field[fName] = fType
			if(i === -1){
				if(fName === ""){
					field = {};
				}
				payload['edges'].push({'name':eName, 'edgeAttributes': field})
				edgeAdded.push(eName);
			}else{
				payload['edges'].forEach( e =>{
					if(e.name === v.entityName && fName !== ""){
						e['edgeAttributes'][fName]= fType;
					}
				})
			}
		})
		
		console.log('final payload is -->', JSON.stringify(payload));
		return payload;
	}
	
	var addColumnToTable = function(column, type){
		createColumns[type].push(column);
		createKeyColumns.push(type+ '_' + column['entityName']+'_'+column['fieldName']);
		var checkUnique = createKeyColumns.unique();
		if (createKeyColumns.length !== checkUnique.length)  {
			createKeyColumns.pop(); 
			createColumns[type].pop(); 
			return false; 
		}
		
		
		$(`#field_properties_${type} > tbody`).append(
			'<tr id="field_'+type+'_'+column['entityName']+"_"+column['fieldName']+' class="tagRow">'
			+`<td class="text-center"><i class="${type==='vertex' ? 'fa flaticon-network' : 'la la-arrow-up'} font-lg"></i> `+ column['entityName'] + '</td>'
			+'<td class="text-center">'+ column['fieldName'] + '</td>'
			+'<td class="text-center">'+ column['fieldType'] + '</td>'
			+ '<td class="text-center"><button type="button" data-property="" class="btn btn-xs btn-no-border icon-on-table color-red tooltips" onclick="OntologyCreateGraphController.removeFieldRow(this,\''+type+'_'+column['entityName']+'_'+column['fieldName']+'\',\''+type+'\')" style="background:white"><i class="icon-delete"></i></button></td></tr>'
			);
		
		console.log('columns -->' + createKeyColumns);	
		console.log('json --> ' + JSON.stringify(createColumns));
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
			initTemplateElements();
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
			
			//Init vertices and edges
			if(ontologyCreateReg.rtdbDatasource === 'NEBULA_GRAPH'){
				ontologyCreateReg.vertices.forEach(function(e){
					let atts = e.tagAttributes;
					let keys = Object.keys(atts);
					if(keys.length > 0){
							keys.forEach(function(key){
							let column = {
								'entityName': e.name,
								'fieldName': key,
								'fieldType': atts[key]
							};
							addColumnToTable(column,'vertex');
						})	
					}else{
						let column = {
								'entityName': e.name,
								'fieldName': "",
								'fieldType': ""
						};
						addColumnToTable(column,'vertex');
					}
				})
				
				ontologyCreateReg.edges.forEach(function(e){
					let atts = e.edgeAttributes;
					let keys = Object.keys(atts);
					if(keys.length > 0){
						keys.forEach(function(key){
							let column = {
								'entityName': e.name,
								'fieldName': key,
								'fieldType': atts[key]
							};
							addColumnToTable(column,'edge');
						})
					}else{
						let column = {
								'entityName': e.name,
								'fieldName': "",
								'fieldType': ""
						};
						addColumnToTable(column,'edge');
					}
				})
			}
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
		

		
		// WIZARD SEQUENCING
		wizardContinue: function(){
			wizardStepContinue();
		},

		
		getSelectedFieldColumn: function(type) {
			var entityName = $(`#${type}Name`).val();
			var nameSelected = $(`#${type}FieldName`).val();
			var typeSelected = $(`#${type}FieldType option:selected`).val();
			
			if (nameSelected !== "" && (typeSelected === "" || entityName === "")) {
				toastr.error(messagesForms.operations.genOpError,'Fields must be filled correctly');
				return false;
			}
			
			if (nameSelected === "" && typeSelected === "" && entityName === "") {
				toastr.error(messagesForms.operations.genOpError,'Fields must be filled correctly');
				return false;
			}
			
			if(nameSelected === ""){
				typeSelected = "";
			}
			
			var column = {
				'entityName': entityName,
				'fieldName': nameSelected,
				'fieldType': typeSelected
			};

			return column;
		},

		
		clearFieldSelected: function(type) {
			$(`#${type}FieldName`).val('');
			$(`#${type}FieldType option:selected`).selectpicker('deselectAll').selectpicker('refresh');
			
		},

		
		addFieldRow: function(type) {
		
			//Allow empty tags/edges
//			if (identification.value===""){
//				toastr.error(messagesForms.operations.genOpError,"Must set ontology name before");
//				return;
//			}

			var column = this.getSelectedFieldColumn(type);
			if (column === false) {
				return;
			}
			
			createColumns[type].push(column);
			createKeyColumns.push(type+ '_' + column['entityName']+'_'+column['fieldName']);
			var checkUnique = createKeyColumns.unique();
			if (createKeyColumns.length !== checkUnique.length)  {
				createKeyColumns.pop(); 
				createColumns[type].pop(); 
				toastr.error(messagesForms.operations.genOpError,'Column name must be unique');
				return false; 
			}
			
			
			$(`#field_properties_${type} > tbody`).append(
				'<tr id="field_'+type+'_'+column['entityName']+"_"+column['fieldName']+' class="tagRow">'
				+`<td class="text-center"><i class="${type==='vertex' ? 'fa flaticon-network' : 'la la-arrow-up'} font-lg"></i> `+ column['entityName'] + '</td>'
				+'<td class="text-center">'+ column['fieldName'] + '</td>'
				+'<td class="text-center">'+ column['fieldType'] + '</td>'
				+ '<td class="text-center"><button type="button" data-property="" class="btn btn-xs btn-no-border icon-on-table color-red tooltips" onclick="OntologyCreateGraphController.removeFieldRow(this,\''+type+'_'+column['entityName']+'_'+column['fieldName']+'\',\''+type+'\')" style="background:white"><i class="icon-delete"></i></button></td></tr>'
				);
			
			

			this.clearFieldSelected(type);
			console.log('columns -->' + createKeyColumns);	
			console.log('json --> ' + JSON.stringify(createColumns));
			toastr.success(messagesForms.operations.genOpSuccess,'');
		},
		
		preparePayload: function(){
			return preparePayload();
		},
		
		preparePayloadUpdate: function(){
			return preparePayloadUpdate();
		},

		removeFieldRowFromFieldName: function(fieldSelected,type) {
			$("#field_"+fieldSelected).remove();
			
			var i = createKeyColumns.indexOf(fieldSelected);
			createKeyColumns.splice(i, 1);
			let j = i;
			createColumns[type].forEach(function(e, pos){
				let name = type + "_" + e.entityName +"_" + e.fieldName;
				if(name === fieldSelected){
					j = pos;
				}
			})
			createColumns[type].splice(j,1);
			console.log('columns --> ' + createKeyColumns);
			console.log('json --> ' + JSON.stringify(createColumns));
			return true;
		},


		removeFieldRow: function(field,name,type) {
			if (this.removeFieldRowFromFieldName(name,type)){
				field.parentElement.parentElement.remove(); 
				toastr.success(messagesForms.operations.genOpSuccess,'');
			}
		},


		clearFieldsRow: function() {
			for (var fieldName in createKeyColumns) {
				this.removeFieldRowFromFieldName(fieldName);
			};
		},

		toggleFieldCreation: function() {
			identification.readOnly = false;
			identification.value = "";
			manageWizardStep();
			
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	OntologyCreateGraphController.load(ontologyCreateJson);	

		
	// AUTO INIT CONTROLLER.
	OntologyCreateGraphController.init();

});