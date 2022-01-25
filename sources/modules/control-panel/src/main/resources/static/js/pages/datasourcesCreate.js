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
	
	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/datasources/freeResource/" + id).done(
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
	var deleteDatasourceConfirmation = function(datasourceId){
		console.log('deleteGadgetDatasourceConfirmation() -> formId: '+ datasourceId);
		
		// no Id no fun!
		if ( !datasourceId ) {toastr.error('NO USER-FORM SELECTED!',''); return false; }
		
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
						}
					},
					invalidHandler : function(event, validator) { // display
																	// error
																	// alert on
																	// form
																	// submit
																	;
						toastr.error(messagesForms.validation.genFormError,'');			
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
						toastr.success(messagesForms.validation.genFormSuccess,'');
						form.submit();
					}
				});
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
		
		// CLEANING QUERY EDITOR
		codeEditor.getModel().setValue('');
		
		editor.clear();
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
			cleanFields('datasource_create_form');
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

			handleValidation();
			initTemplateElements();
			
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
				setTimeout(function(){ $('#executeQuery').trigger('click'); }, 1000);
				
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
