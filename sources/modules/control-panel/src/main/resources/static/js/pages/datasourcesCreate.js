var DatasourcesCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Datasource Controller';	
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
	// DELETE DATASOURCE
	var deleteDatasourceConfirmation = function(datasourceId){
		console.log('deleteGadgetDatasourceConfirmation() -> formId: '+ datasourceId);
		
		// no Id no fun!
		if ( !datasourceId ) {$.alert({title: 'ERROR!',type: 'red' , theme: 'dark', content: 'NO USER-FORM SELECTED!'}); return false; }
		
		logControl ? console.log('deleteGadgetDatasourceConfirmation() -> formAction: ' + $('.delete-gadgetDatasource').attr('action') + ' ID: ' + $('.delete-gadgetDatasource').attr('userId')) : '';
		
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		// call user Confirm at header.
		$.ajax({ url: "/controlpanel/datasources/getGadgetsUsingDatasource/" + datasourceId, headers: { [csrf_header]: csrf_value }, type: 'GET', dataType: 'json', contentType: 'application/json', mimeType: 'application/json',
			success: function (gadgetNames) {			 
						HeaderController.showConfirmDialogDatasource('delete_datasource_form', gadgetNames);	
			}
		});	
	}
	

	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#datasource_create_form');
		var error1 = $('.alert-danger');
		var success1 = $('.alert-success');

		// set current language
		currentLanguage = currentLanguage || LANGUAGE;

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
						maxvalues : {							
							required : true
						},
						ontologyIdentification: {							
							required : true
						}

					},
					invalidHandler : function(event, validator) { // display
																	// error
																	// alert on
																	// form
																	// submit
						success1.hide();
						error1.show();						
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
	
	
	
	
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return datasourceCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			/*EDITION MODE*/
			/*Hide dimensions*/
			if(!$("[name='id']").val()){
				$("#dimensionsPanel").hide();
			}
			initDatapicker();
			
			// INPUT MASK FOR ontology identification allow only letters, numbers and -_
			$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
			
			// INSERT MODE ACTIONS (ontologyCreateReg.actionMode = NULL )
			if ( datasourceCreateReg.actionMode !== null){
			logControl ? console.log('|---> Action-mode: UPDATE') : '';
			// Set active query executed
			$('#executeQuery').trigger('click');
		}
			handleValidation();
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		getFieldsFromQueryResult: function (jsonString){
			var fields = [];
			function iterate(obj, stack) {
		        for (var property in obj) {
		            if (obj.hasOwnProperty(property)) {
		                if (typeof obj[property] == "object") {
		                    iterate(obj[property], stack + (stack==""?'':'.') + property);
		                } else {
		                    fields.push(stack + (stack==""?'':'.') + property);
		                }
		            }
		        }
		        
		        return fields;
		    }
	
			return iterate(JSON.parse(jsonString), '');
		},
		
		// DELETE GADGET DATASOURCE 
		deleteGadgetDatasource: function(gadgetDatasourceId){
			logControl ? console.log(LIB_TITLE + ': deleteGadgetDatasource()') : '';	
			deleteDatasourceConfirmation(gadgetDatasourceId);			
		}
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	DatasourcesCreateController.load(datasourceJson);	
		
	// AUTO INIT CONTROLLER.
	DatasourcesCreateController.init();
});
