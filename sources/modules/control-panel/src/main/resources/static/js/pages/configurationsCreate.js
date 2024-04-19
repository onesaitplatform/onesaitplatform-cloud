var ConfigurationsCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Configurations Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.
	var currentFormat = '' // date format depends on currentLanguage.
	var editor;
	
	// CONTROLLER PRIVATE FUNCTIONS
	

	
	// REDIRECT URL
	var navigateUrl = function(url){ window.location.href = url; }
	
		
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		logControl ? console.log('cleanFields() -> ') : '';
		
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
		
		editor.setValue("");
	}
	
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#configurations_create_form');
		
		// set current language
		currentLanguage = userCreateReg.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not(.selectpicker)", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,

			// validation rules
            rules: {
				userId:					{ minlength: 5, required: true },             
				configurationTypes:		{ required: true },
				identification:			{ required: true },
				ymlConfig:   			{required:true},
				createdAt:				{ date: true, required: true },

            },
            invalidHandler: function(event, validator) { //display error alert on form submit              
            	toastr.error(messagesForms.validation.genFormError,'');
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
            	toastr.success(messagesForms.validation.genFormSuccess,'');
            	$('#ymlConfig').val(editor.getValue());
				form.submit();
            }
        });
    }
	
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> resetForm') : '';		
				
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('configurations_create_form');
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
		
		$('.editor').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.closest('.CodeMirror').CodeMirror.getValue() !== ''){ 
				$(event.currentTarget.closest('.CodeMirror')).nextAll('span:first').addClass('hide');
				$(event.currentTarget.closest('.CodeMirror')).removeClass('editor-has-error');
			} else {
				$(event.currentTarget.closest('.CodeMirror')).nextAll('span:first').removeClass('hide');
				$(event.currentTarget.closest('.CodeMirror')).addClass('editor-has-error');
			}
		})
	}
	
	// INIT CODEMIRROR
	var handleCodeMirror = function () {
		logControl ? console.log('handleCodeMirror() on -> ymlConfig') : '';	
		
        
        editor = ace.edit("ymlConfigAce");
        editor.setTheme("ace/theme/xcode");
        editor.session.setMode("ace/mode/yaml");
        editor.setValue($('#ymlConfig').val());
        editor.gotoLine(1);
        
		
    }
	
	// DELETE CONFIGURATION
	var deleteConfigurationConfirmation = function(configurationId){
		console.log('deleteConfigurationConfirmation() -> formId: '+ configurationId);
		
		
		// set action and configurationId to the form
		$('.delete-configuration').attr('id',configurationId);
		$('.delete-configuration').attr('action','/controlpanel/configurations/' + configurationId);
		console.log('deleteconfiugrationConfirmation() -> formAction: ' + $('.delete-configuration').attr('action') + ' ID: ' + $('.delete-configuration').attr('userId'));
		
		// call configuration Confirm at header.
		HeaderController.showConfigurationConfirmDialog(configurationId);	
	}

	// CONTROLLER PUBLIC FUNCTIONS 
	return{		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return userCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';			
			handleValidation();
			initTemplateElements();
			handleCodeMirror();
			
		},
		// DELETE CONFIG
		deleteConfiguration: function(configurationId){
			logControl ? console.log(LIB_TITLE + ': deleteConfiguration()') : '';	
			deleteConfigurationConfirmation(configurationId);			
		}
	};

}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	ConfigurationsCreateController.load(configurationsCreateJson);	
		
	// AUTO INIT CONTROLLER.
	ConfigurationsCreateController.init();
	
});

