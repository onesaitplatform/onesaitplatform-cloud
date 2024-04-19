var GadgetsCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Gadget Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	
	
	// REDIRECT URL
	var navigateUrl = function(url) {
		window.location.href = url;
	}
	// CONTROLLER PRIVATE FUNCTIONS	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#gadget_create_form');
		var error1 = $('.alert-danger');
		var success1 = $('.alert-success');

		// set current language
		currentLanguage = gadgetCreateReg.language || LANGUAGE;

		form1.validate({
					errorElement : 'span', // default input error message
											// container
					errorClass : 'help-block help-block-error', // default input
																// error message
																// class
					focusInvalid : false, // do not focus the last invalid
											// input
					ignore : ":hidden:not(.selectpicker)", // validate all
															// fields including
															// form hidden input
															// but not
															// selectpicker
					lang : currentLanguage,
					// custom messages
					messages : {

					},
					// validation rules
					rules : {
						identification : {
							minlength : 5,
							required : true
						},
						description : {
							minlength : 5,
							required : true
						},

					},
					invalidHandler : function(event, validator) { // display
																	// error
																	// alert on
																	// form
																	// submit
						success1.hide();
						error1.show();
						App.scrollTo(error1, -200);
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
					highlight : function(element) { // hightlight error inputs
						$(element).closest('.form-group').addClass('has-error');
					},
					unhighlight : function(element) { // revert the change
														// done by hightlight
						$(element).closest('.form-group').removeClass(
								'has-error');
					},
					success : function(label) {
						label.closest('.form-group').removeClass('has-error');
					},
					// ALL OK, THEN SUBMIT.
					submitHandler : function(form) {

						 success1.show();
			                error1.hide();
			                form.submit();
					}
				});
	}
	
	// DELETE GADGET
	var deleteGadgetConfirmation = function(gadgetId){
		console.log('deleteGadgetConfirmation() -> formId: '+ gadgetId);
		
		// no Id no fun!
		if ( !gadgetId ) {$.alert({title: 'ERROR!',type: 'red' , theme: 'dark', content: 'NO USER-FORM SELECTED!'}); return false; }
		
		logControl ? console.log('deleteGadgetConfirmation() -> formAction: ' + $('.delete-gadget').attr('action') + ' ID: ' + $('.delete-gadget').attr('userId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogGadget('delete_gadget_form');	
	}
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			debugger
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return gadgetCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			
			if(gadgetCreateReg.iframe){
				 $("#toolFullScreen").trigger("click");
			}
			if(gadgetCreateReg.ontologyId != null){
				
					$('#datasources').val(gadgetCreateReg.ontologyId).change();
				}
				

			$( "body" ).removeClass( "wait-interval" )
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		// DELETE GADGET DATASOURCE 
		deleteGadget: function(gadgetId){
			logControl ? console.log(LIB_TITLE + ': deleteGadget()') : '';	
			deleteGadgetConfirmation(gadgetId);			
		}
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	GadgetsCreateController.load(gadgetCreateJson);	
		
	// AUTO INIT CONTROLLER.
	GadgetsCreateController.init();
});
