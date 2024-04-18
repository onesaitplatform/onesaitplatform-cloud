var authorizationsArr 		= []; // add authorizations
var authorizationUpdateArr  = []; // get authorizations of the ontology
var authorizationsIds 		= []; // get authorizations ids for actions
var authorizationObj 		= {}; // object to receive authorizations responses.

var applicationCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Applications Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	


	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#form-application');
		
		// set current language
		currentLanguage =  LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,			
			// validation rules
            rules: {				
                name:			{ minlength: 5, required: true },
				gitUsername:	{ required: true },
				gitlabSite: 	{ required: true },
				privateToken:	{ required: true },
				gitlabSite: 	{  required: true },
				gitrepository:	{  required: true }
            },
            invalidHandler: function(event, validator) { //display error alert on form submit  

            	toastr.error(messagesForms.validation.genFormError,'');
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
         
        });
    }
	
	
	var initTemplateElements = function(){
		
	$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});	
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		

	}
	
	return{
			// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
			load: function(Data) { 
				logControl ? console.log(LIB_TITLE + ': load()') : '';
				return applicationCreateReg = Data;
			},	
			
			// INIT() CONTROLLER INIT CALLS
			init: function(){
				logControl ? console.log(LIB_TITLE + ': init()') : '';
				// INPUT MASK FOR ontology identification allow only letters, numbers and -_
				$("#name").inputmask({ regex: "[a-zA-Z 0-9_-]*", greedy: false });
				handleValidation();
				initTemplateElements();
				
			}
		}
		
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
		
	// AUTO INIT CONTROLLER.
	applicationCreateController.init();
});
