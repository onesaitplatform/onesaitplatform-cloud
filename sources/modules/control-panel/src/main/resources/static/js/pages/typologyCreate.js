var TypologyCreateController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var LIB_TITLE = 'Typology Controller';
	var logControl = 1;


	// Reset form
	$('#resetBtn').on('click',function(){ 
		cleanFields('typology_create_form');
	});

	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		console.log('cleanFields() -> ');

		// CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm();
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
	}
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#typology_create_form');

		// set current language
		currentLanguage = typologyCreateJson.language || LANGUAGE;

		form1.validate({
					errorElement : 'span',
					errorClass : 'help-block help-block-error',
					focusInvalid : false,
					ignore : ":hidden:not(.selectpicker)",
					lang : currentLanguage,
					rules : {
						identification : {
							minlength : 5,
							required : true
						},
						description : {
							minlength : 5,
							required : true
						}
					},
					invalidHandler : function(event, validator) {
						toastr.error(typologyCreateJson.validform.emptyfields,'');
					},
					errorPlacement : function(error, element) {
						if (element.is(':checkbox')) {
							error
									.insertAfter(element
											.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline"));
						} else if (element.is(':radio')) {
							error
									.insertAfter(element
											.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline"));
						} else {
							error.insertAfter(element);
						}
					},
					highlight : function(element) { 
						$(element).closest('.form-group').addClass('has-error');
					},
					unhighlight : function(element) { 
						$(element).closest('.form-group').removeClass(
								'has-error');
					},
					success : function(label) {
						label.closest('.form-group').removeClass('has-error');
					},
					// ALL OK, THEN SUBMIT.
					submitHandler : function(form) {
						form.submit();
					}
				});
	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> resetForm') : '';		

		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		
		
	}

	// REDIRECT URL
	var navigateUrl = function(url) {
		window.location.href = url;
	}
	// DELETE TYPOLOGY
	var deleteTypologyConfirmation = function(typologyId) {
		console.log('deleteTypologyConfirmation() -> formId: ' + typologyId);
		// no Id no fun!
		if (!typologyId) {
			$.alert({
				title : 'ERROR!',
				type : 'red',
				theme : 'dark',
				content : 'NO TYPOLOGY-FORM SELECTED!'
			});
			return false;
		}

		logControl ? console
				.log('deleteTypologyConfirmation() -> formAction: '
						+ $('.delete-typology').attr('action') + ' ID: '
						+ $('.delete-typology').attr('userId')) : '';

		// call user Confirm at header.
		HeaderController.showConfirmDialogTypology('delete_typology_form');
	}

	// CONTROLLER PUBLIC FUNCTIONS
	return {
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
		},
		// INIT() CONTROLLER INIT CALLS
		init : function() {
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleValidation();
			initTemplateElements();
		},

		// REDIRECT
		go : function(url) {
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			navigateUrl(url);
		},
	};
}();

// AUTO INIT CONTROLLER WHEN READY
$(window).load(function() {	
	
	// AUTO INIT CONTROLLER.
	TypologyCreateController.init();
});
