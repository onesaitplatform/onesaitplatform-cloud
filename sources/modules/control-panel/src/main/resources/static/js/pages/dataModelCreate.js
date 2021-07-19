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
	

	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#datamodel_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');
		
		// set current language
		currentLanguage = currentLanguage || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not(.selectpicker)", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// validation rules
            rules: {
            	identification:	{required: true, minlength: 5},
            	description:	{required: true, minlength: 5},
            	labels:			{required: true},
            	type:			{required: true},
            	jsonSchema:		{required: true}
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
            	if($('#datamodelLabel').val() === ''){
					success1.hide();
					error1.show();
					$('#metainferror').removeClass('hide').addClass('help-block-error font-red'); App.scrollTo(error1, -200);
				} else if($('.CodeMirror')[0].CodeMirror.getValue() === ''){
					success1.hide();
					error1.show();
					$('#jsonerror').removeClass('hide').addClass('help-block-error font-red'); App.scrollTo(error1, -200);
				} else {
					success1.show();
	                error1.hide();
	                form.submit();
				}
            }
        });
    }

	
	$('#resetBtn').on('click',function(){ 
		cleanFields('datamodel_create_form');
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
		
		// INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#datamodelName").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });

		// tagsinput validate fix when handleValidation()
		$('#datamodelLabel').on('itemAdded', function(event) {
			if ($(this).val() !== ''){ $('#metainferror').addClass('hide');}
		});

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
			handleValidation();

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