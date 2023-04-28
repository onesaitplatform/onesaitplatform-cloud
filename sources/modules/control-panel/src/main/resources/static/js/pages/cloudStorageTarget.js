var cloudStorageCreateController  = function(){
	
	// DEFAULT PARAMETERS, VAR, CONSTS
	var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Cloud Storage Controller';
	logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';
	var formId = "cloud_storage_form";
	
    var form1 = $('#cloud_storage_form');
	
	// CONTROLLER PRIVATE FUNCTIONS

	var handleValidation = function() {
		
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
		// set current language
		currentLanguage = currentLanguage || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// validation rules
            rules: {
            	title:	{required: true, minlength: 5},
            	bucket:	{required: true, minlength: 5},
            	regexFilter:	{required: true}
            },
            messages: {
            	title: 	{ required: dataModelCreateJson.validform.emptyfields, minlength: dataModelCreateJson.validform.minLength},
            	bucket:		{ required: dataModelCreateJson.validform.emptyfields, minlength: dataModelCreateJson.validform.minLength},
            	regexFilter: 	{ required: dataModelCreateJson.validform.emptyfields}
            },
            invalidHandler: function(event, validator) { //display error alert on form submit
                
            },
            errorPlacement: function(error, element) {
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list, .radio-inline")); }
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
				var formAux = $('#cloud_storage_form');         
	            formAux.attr("action", "/controlpanel/datalabeling/createcloudstoragetarget")
            	formAux.submit();
            }
        });
    }
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		
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
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		$('.tagsinput').prev().removeClass('tagsinput-has-error');
		$('.tagsinput').nextAll('span:first').addClass('hide');

		// CLEANING CODEMIRROR
		if ($('.CodeMirror')[0].CodeMirror){
			var editor = $('.CodeMirror')[0].CodeMirror;
			editor.setValue('');
			$('.CodeMirror').removeClass('editor-has-error');
			$('.CodeMirror').nextAll('span:first').addClass('hide');
		}
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> resetForm') : '';		
		
		// INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#title").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });

		
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('datamodel_create_form');
		});	
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		
		
		$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.getElementsByTagName('select')[0]){
				$('.form').validate().element('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id'));
			}
		})
			
		$('.tagsinput').filter('[required]').parent().on('blur', 'input', function(event) {
			if ($(event.target).parent().next().val() !== ''){
				$(event.target).parent().next().nextAll('span:first').addClass('hide');
				$(event.target).parent().removeClass('tagsinput-has-error');
			} else {
				$(event.target).parent().next().nextAll('span:first').removeClass('hide');
				$(event.target).parent().addClass('tagsinput-has-error');
			}   
		})
		
		 
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
			$("#cloud_storage_form").submit();
		},
		
	};
	
}();


//AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	cloudStorageCreateController.load(dataModelCreateJson);	
		
	// AUTO INIT CONTROLLER.
	cloudStorageCreateController.init();
	
});