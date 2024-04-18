var QueryTemplatesCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Datasource Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';
	
    var form1 = $('#template_create_form');
	
	// CONTROLLER PRIVATE FUNCTIONS	
	// REDIRECT URL
	var navigateUrl = function(url) {
		window.location.href = url;
	}
	// DELETE QUERY TEMPLATE
	var deleteQueryTemplateConfirmation = function(templateId){
		console.log('deleteQueryTemplateConfirmation() -> formId: '+ templateId);
		
		// no Id no fun!
		if ( !templateId ) {$.alert({title: 'ERROR!',type: 'red' , theme: 'dark', content: 'NO USER-FORM SELECTED!'}); return false; }
		
		logControl ? console.log('deleteQueryTemplateConfirmation() -> formAction: ' + $('.delete-template').attr('action') + ' ID: ' + $('.delete-template').attr('userId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogQueryTemplate('delete_template_form');	
	}
	
//	var validateAndSubmit = function (){
//		var valid = true;
//		valid = valid && validate ($('#identification')) && validate ($('#description')) && validate ($('#templates'));
//		alert (valid);
//	}
	
	var validate = function (obj){
		if (obj.val() === '') { 
			obj.closest(".form-group").addClass('has-error');
			$('#' + obj.prop('id') + 'error').removeClass('hide').addClass('help-block-error font-red');
			return false;
		} else { 
			obj.closest(".form-group").removeClass('has-error');
			$('#' + obj.prop('id') + 'error').addClass('hide');
			return true;
		}
	}
	
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
            	identification:	{required: true, minlength: 5},
            	description:	{required: true, minlength: 5}
            },
            messages: {
            	identification: 	{ required: templateJson.validform.emptyfields, minlength: templateJson.validform.minLength},
            	description:		{ required: templateJson.validform.emptyfields, minlength: templateJson.validform.minLength}
            },
            invalidHandler: function(event, validator) { //display error alert on form submit
            	validateSpecialComponentsAndSubmit(null, false);
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
            	validateSpecialComponentsAndSubmit(form, true);
            }
        });
    }
	
	var validateSpecialComponentsAndSubmit = function (form, submit) {
		
		if ($('.CodeMirror')[0].CodeMirror.getValue() !== ''){ 
			$($('.CodeMirror')[0]).nextAll('span:first').addClass('hide');
			$($('.CodeMirror')[0]).removeClass('editor-has-error');
		} else {
			$($('.CodeMirror')[0]).nextAll('span:first').removeClass('hide');
			$($('.CodeMirror')[0]).addClass('editor-has-error');
			submit = false;
		}	

		if ($('.CodeMirror')[1].CodeMirror.getValue() !== ''){ 
			$($('.CodeMirror')[1]).nextAll('span:first').addClass('hide');
			$($('.CodeMirror')[1]).removeClass('editor-has-error');
		} else {
			$($('.CodeMirror')[1]).nextAll('span:first').removeClass('hide');
			$($('.CodeMirror')[1]).addClass('editor-has-error');
			submit = false;
		}

		if (submit){
			
			var url = "/controlpanel/querytemplates/checkQueryTemplateSelector/";
			var requestData = JSON.stringify({"template": $('#identification').val(),"ontology": $('#templates option:selected').text(), "query": $('.CodeMirror')[0].CodeMirror.getValue()});
	    	var csrf_value = $("meta[name='_csrf']").attr("content");
	    	var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
			
			$.ajax({
				url : url,
				headers: {
					[csrf_header]: csrf_value
			    },
				type : 'POST',
				data : requestData,
	            dataType: 'text',
	            contentType: 'text/plain',
	            mimeType: 'text/plain',
				success: function(response,status){
					if (response==null || response==""){
						toastr.success(templateJson.messages.validationOK, templateJson.messages.requestSent);
						form.submit();
					} else {
						toastr.error(templateJson.messages.validationKO, response);
					}
				},
	            error: function(data,status,er) {
	                $('#dialog-error').val("ERROR");
	            }
			});
						

		} else {
			toastr.error(templateJson.messages.validationKO);
		}
	}
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('input,textarea:visible').filter('[required]').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			$(this).val('');
			$(this).closest(".form-group").removeClass('has-error');
			$('#' + $(this).prop('id') + 'error').addClass('hide');
		});
		
		//CLEANING SELECTs
		$('.selectpicker').each(function(){
			$(this).closest(".form-group").removeClass('has-error');
			$('#' + $(this).prop('id') + 'error').addClass('hide');
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});

		
		// CLEANING CODEMIRROR
		if ($('.CodeMirror')[0].CodeMirror){
			var editor = $('.CodeMirror')[0].CodeMirror;
			editor.setValue(exampleSelector);
		}
		if ($('.CodeMirror')[1].CodeMirror){
			var editor = $('.CodeMirror')[1].CodeMirror;
			editor.setValue(exampleGenerator);
		}
		$('.CodeMirror').each(function(){
			$(this).removeClass('editor-has-error');
			$(this).nextAll('span:first').addClass('hide');
		});
	
//		// CLEANING CODEMIRROR
//		if ($('.CodeMirror')[0].CodeMirror){
//			var editor = $('.CodeMirror')[0].CodeMirror;
//			editor.setValue("var digitalTwinApi = Java.type('com.minsait.onesait.platform.digitaltwin.logic.api.DigitalTwinApi').getInstance();\nfunction init(){}\nfunction main(){}");
//		}
//		editor.setText(JSON.stringify({}));
	}	
	

	// CONTROLLER PUBLIC FUNCTIONS 
	return{		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return templateCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleValidation();
			/*EDITION MODE*/
			/*Hide dimensions*/
			/*if(!$("[name='id']").val()){
				$("#dimensionsPanel").hide();
			}*/
			
			// INPUT MASK FOR ontology identification allow only letters, numbers and -_
			$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
			
			// Reset form
			$('#resetBtn').on('click',function(){ 
				cleanFields('digitaltwintype_create_form');
			});	
			
			// On Blur VALIDATIONS
			
			$('input,textarea:visible').filter('[required]').on('blur', function(){
				validate($(this));
			});
			
			$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
				validate($('#templates'));
			});
			
			$('.editor').filter('[required]').parent().on('blur', 'div', function(event) {
				if (event.currentTarget.closest('.CodeMirror').CodeMirror.getValue() !== ''){ 
					$(event.currentTarget.closest('.CodeMirror')).nextAll('span:first').addClass('hide');
					$(event.currentTarget.closest('.CodeMirror')).removeClass('editor-has-error');
				} else {
					$(event.currentTarget.closest('.CodeMirror')).nextAll('span:first').removeClass('hide');
					$(event.currentTarget.closest('.CodeMirror')).addClass('editor-has-error');
				}
			})
					
			// INSERT MODE ACTIONS (templateCreateReg.actionMode = NULL )
			if ( templateCreateReg.actionMode !== null){
			logControl ? console.log('|---> Action-mode: UPDATE') : '';

			// Set active query executed
			//$('#executeQuery').trigger('click');

			}
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		// DELETE QUERY TEMPLATE 
		deleteQueryTemplate: function(queryTemplateId){
			logControl ? console.log(LIB_TITLE + ': deleteQueryTemplate()') : '';	
			deleteQueryTemplateConfirmation(queryTemplateId);			
		}
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	QueryTemplatesCreateController.load(templateJson);	
		
	// AUTO INIT CONTROLLER.
	QueryTemplatesCreateController.init();
});
