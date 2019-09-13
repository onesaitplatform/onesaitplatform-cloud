var dataModelCreateController  = function(){
	
	// DEFAULT PARAMETERS, VAR, CONSTS
	var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Data Model Controller';
	logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';
	var formId = "datamodel_create_form";
	
	// CONTROLLER PRIVATE FUNCTIONS
	
	$("#createBtn").on('click',function(event){
		event.preventDefault(); 
		var editor = $('.CodeMirror')[0].CodeMirror;
		if($("#datamodelName").val()!='' && $("#datamodelName").val()!=undefined && $("#datamodelDescription").val()!='' && $("#datamodelDescription").val()!=undefined && editor.getValue()!='') {
			dataModelCreateController.submitform();
		}else{
			$.alert({title: 'ERROR!', theme: 'light', content: dataModelCreateJson.validform.emptyfields});
			return false;
		}
		
	});
	
	
	$('#resetBtn').on('click',function(){ 
		cleanFields('datamodel_create_form');
	});

	
	$("#updateBtn").on('click',function(event){
		event.preventDefault(); 
		var editor = $('.CodeMirror')[0].CodeMirror;
		if($("#datamodelName").val()!='' && $("#datamodelName").val()!=undefined && $("#datamodelDescription").val()!='' && $("#datamodelDescription").val()!=undefined && editor.getValue()!=''){
			dataModelCreateController.submitform();
		}else{
			$.alert({title: 'ERROR!', theme: 'light',  content: dataModelCreateJson.validform.emptyfields});
			return false;
		}
		
	});
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remote" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();

		// CLEANING CODEMIRROR
		if ($('.CodeMirror')[0].CodeMirror){
			var editor = $('.CodeMirror')[0].CodeMirror;
			editor.setValue('');
			
		}
		
		// CLEANING CODEMIRROR
		if ($('.CodeMirror')[0].CodeMirror){
			var editor = $('.CodeMirror')[0].CodeMirror;
			editor.setValue('');
			
		}
	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> resetForm') : '';		
				
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('datamodel_create_form');
		});	
	}
	
	// INIT CODEMIRROR
	var handleCodeMirror = function () {
		logControl ? console.log('handleCodeMirror() on -> jsonSchemaDatamodel') : '';	
		
        var myTextArea = document.getElementById('jsonSchemaDatamodel');
        var myCodeMirror = CodeMirror.fromTextArea(myTextArea, {
        	mode: "application/ld+json",
        	autoCloseBrackets: true,          
            lineNumbers: false,
            foldGutter: true,
            matchBrackets: true,
            styleActiveLine: true,
            theme:"elegant",
            lineWrapping: true

        });
		myCodeMirror.setSize("100%", 350);
    }
	

	// DELETE DATAMODEL
	var deleteDataModelConfirmation = function(dataModelId){
		console.log('deleteDataModelConfirmation() -> formId: '+ dataModelId);
		
		// set action and dataModelionId to the form
		$('.delete-dataModel').attr('id',dataModelId);
		$('.delete-dataModel').attr('action','/controlpanel/datamodels/delete/' + dataModelId);
		console.log('deleteDataModelConfirmation() -> formAction: ' + $('.delete-dataModel').attr('action') + ' ID: ' + $('.delete-dataModel').attr('id'));
		
		// call dataModel Confirm at header.
		HeaderController.showDataModelConfirmDialog(dataModelId);	
	}

	
	// CONTROLLER PUBLIC FUNCTIONS
	return{
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
		},
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			initTemplateElements();
			handleCodeMirror();
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		// DELETE DATAMODEL
		deleteDataModel: function(dataModelId){
			logControl ? console.log(LIB_TITLE + ': deleteDataModel()') : '';	
			deleteDataModelConfirmation(dataModelId);			
		},
		submitform: function(){
			
			$("#datamodel_create_form").submit();
		},
		
	};
	
}();


//AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	dataModelCreateController.load(dataModelCreateJson);	
		
	// AUTO INIT CONTROLLER.
	dataModelCreateController.init();
	
	
});