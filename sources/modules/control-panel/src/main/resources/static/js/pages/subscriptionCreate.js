var SubscriptionCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Subscription Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	
	function initDatapicker(){
		var dateCreated = $("#datecreated").datepicker('getDate');
	}
	
	// CONTROLLER PRIVATE FUNCTIONS	
	// REDIRECT URL
	var navigateUrl = function(url) {
		window.location.href = url;
	}
	
	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/subscriptions/freeResource/" + id).done(
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
	// DELETE DATASOURCE
	var deleteSubscriptionConfirmation = function(subscriptionId){
		console.log('deleteSubscriptionConfirmation() -> formId: '+ subscriptionId);
		
		// no Id no fun!
		if ( !subscriptionId ) {$.alert({title: 'ERROR!',type: 'red' , theme: 'dark', content: 'NO USER-FORM SELECTED!'}); return false; }
		
		logControl ? console.log('deleteSubscriptionConfirmation() -> formAction: ' + $('.delete-subscription').attr('action') + ' ID: ' + $('.delete-subscription').attr('userId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogSubscription('delete_subscription_form');	
	}
	
	var handleValidation =  function() {
        // for more info visit the official plugin documentation:
        // http://docs.jquery.com/Plugins/Validation

        var form1 = $('#subscription_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');


        form1.validate({
            errorElement: 'span', // default input error message container
            errorClass: 'help-block help-block-error', // default input error
														// message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .formcolorpicker, .hidden-validation')", // validate
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
//				jsonschema: { required:"El esquema no se ha guardado correctamente"},
//				datamodelid: { required: "Por favor seleccione una plantilla de ontología, aunque sea la vacia."}
			},
			// validation rules
            rules: {
            	ontology:		{ required: true },
                identification:	{ minlength: 5, required: true },
				description:	{ minlength: 5, required: true },
				queryField:		{ required: true },
				queryOperator:		{ required: true },
				projection:		{ required: true },
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
                
				// form.submit();
				form1.ajaxSubmit({type: 'post', success : function(data){
					
					navigateUrl(data.redirect);
					
					}, error: function(data){
						HeaderController.showErrorDialog(data.responseJSON.cause)
					}
				})
				

			}
        });
    }
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return subscriptionCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			
			handleValidation();
			
			// INPUT MASK FOR ontology identification allow only letters, numbers and -_
			$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
			
			// INSERT MODE ACTIONS (ontologyCreateReg.actionMode = NULL )
			if ( subscriptionCreateReg.actionMode !== null){
			logControl ? console.log('|---> Action-mode: UPDATE') : '';
			

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
		
		// DELETE GADGET DATASOURCE 
		deleteSubscription: function(subscriptionId){
			logControl ? console.log(LIB_TITLE + ': deleteSubscription()') : '';	
			deleteSubscriptionConfirmation(subscriptionId);			
		}
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	SubscriptionCreateController.load(subscriptionJson);	
		
	// AUTO INIT CONTROLLER.
	SubscriptionCreateController.init();
});
