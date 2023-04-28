var resourceCreateController  = function(){
	
	var LIB_TITLE = 'Resource Controller';
	logControl = 1;
	
	// CONTROLLER PRIVATE FUNCTIONS
		
	$('#resetBtn').on('click',function(){ 
		cleanFields('resource_create_form');
	});

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
		
		$('#public').prop('checked', false);
		
		//CLEANING QUERY
		setMonacoValue("");
		$('#sendQueryError').hide()
		
		//CLEANING QUERY RESULT
		$('#result-panel').addClass('hide');
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#resource_create_form');

		// set current language
		currentLanguage = resourceCreateJson.language || LANGUAGE;

		form1.validate({
					errorElement : 'span',
					errorClass : 'help-block help-block-error',
					focusInvalid : false,
					ignore : ":hidden:not(.selectpicker)",
					lang : currentLanguage,
					rules : {
						name : {
							minlength : 5,
							required : true
						},
						selector_ontologias: {required : true},
						resourceDataset: {required: true},
						sendQuery: {required: true}
					},
					invalidHandler : function(event, validator) {
						toastr.error(resourceCreateJson.validform.emptyfields,'');
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
						resourceCreateController.submitform(form);
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
		
		$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.getElementsByTagName('select')[0]){
				$('.form').validate().element('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id'));
			}
		});
	}
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}

	// DELETE RESOURCE
	var deleteResourceConfirmation = function(resourceId){
		console.log('deleteResourceConfirmation() -> formId: '+ resourceId);
		
		// set action and resourceId to the form
		$('.delete-resource').attr('id',resourceId);
		$('.delete-resource').attr('action','/controlpanel/opendata/resources/delete/' + resourceId);
		console.log('deleteResourceConfirmation() -> formAction: ' + $('.delete-resource').attr('action') + ' ID: ' + $('.delete-resource').attr('id'));
		
		// call resource Confirm at header.
		HeaderController.showResourceConfirmDialog(resourceId);	
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
			handleValidation();
			initTemplateElements();
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		// DELETE RESOURCE
		deleteResource: function(resourceId){
			logControl ? console.log(LIB_TITLE + ': deleteResource()') : '';	
			deleteResourceConfirmation(resourceId);			
		},
		submitform: function(form){
			if (resourceCreateJson.actionMode !== null) {
				$("#dataset").val($("#resourceDataset").val());
				$("#ontology").val($("#selector_ontologias").val());
			} else {
				var query = getMonacoValue();
				$("#sendQuery").val(query);
			}
			if ($("#resource_create_form").valid()){
				App.blockUI({boxed: true, overlayColor:"#5789ad",type:"loader",state:"warning",message:"Uploading Resource..."});
			}
			form.submit();
		}
		
	};
	
}();


//AUTO INIT CONTROLLERS WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)		
	resourceCreateController.load(resourceCreateJson);
	resourceCreateController.init();
	
});