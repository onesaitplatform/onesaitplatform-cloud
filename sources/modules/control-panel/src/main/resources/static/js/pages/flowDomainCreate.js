var FlowDomainCreateController = function() {
	
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Flow Domain Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var validMetaInf = false;
	var hasId = false; // instance
	var domainIsAvailable = false;
	var waitingForDomainValidation = false;
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// DELETE DASHBOARD
	var deleteFlowDomainConfirmation = function(dashboardId){
		console.log('deleteFlowDomainConfirmation() -> formId: '+ dashboardId);		
		// no Id no fun!
		if ( !dashboardId ) {$.alert({title: 'ERROR!',type: 'red' , theme: 'dark', content: 'NO FLOW DOMAIN SELECTED!'}); return false; }
		
		logControl ? console.log('deleteFlowDomainConfirmation() -> formAction: ' + $('.delete_flow_domain_form').attr('action') + ' ID: ' + $('.delete_flow_domain_form').attr('userId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogFlowDomain('delete_flow_domain_form');
	}
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		logControl ? console.log('cleanFields() -> ') : '';
		
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
		
		//CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
		
	}
	
	var checkDomainNameAvailable = function (domainId) {
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		
		return $.ajax({ 
			url: '/controlpanel/flows/check/available/'+domainId,
			headers: {
				[csrf_header]: csrf_value
		    },
			type: 'GET',
			async: false,
			dataType: 'text', 
			contentType: 'text/plain',
			mimeType: 'text/plain',
			success: function(exists) { 
			  if(exists=="false") ontologyExist=false;
	      else ontologyExist=true;
			},
			error: function (xhr, textStatus, errorThrown) {
                console.log('a' + textStatus);
                return false;
            }
		});
	}
	
	var checkDomainAmountAvailable = function (domainId) {
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		
		return $.ajax({ 
			url: '/controlpanel/flows/check/amount/'+domainId,
			headers: {
				[csrf_header]: csrf_value
		    },
			type: 'GET',
			async: false,
			dataType: 'text', 
			contentType: 'text/plain',
			mimeType: 'text/plain',
			success: function(available) { 
			  if(available=="false") freeDomains=false;
	      else freeDomains=true;
			},
			error: function (xhr, textStatus, errorThrown) {
                console.log('a' + textStatus);
                return false;
            }
		});
	}
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#domain_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');
		
        // INPUT MASK FOR ontology identification allow only letters, numbers
		// and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
	
		// set current language
		currentLanguage = flowDomainCreateReg.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// validation rules
            rules: {
            	identification:	{ minlength: 5, required: true }
            },
            invalidHandler: function(event, validator) { //display error alert on form submit              
                success1.hide();
                error1.show();
                App.scrollTo(error1, -200);
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
               
            	checkDomainAmountAvailable($('#identification').val());
            	var error1 = $('.alert-danger');
   			 	var success1 = $('.alert-success');
   				if (freeDomains) {
   					checkDomainNameAvailable($('#identification').val());
   	   		     
   	   			 	if (ontologyExist) {
   		   			 	error1.hide();
   		                success1.show();
   		                form.submit();
   	   			 	} else{
   	   			 		//Change style to red
   	   			 		error1.find('span').text(flowDomainCreateReg.dupError);
   	   			 		$('#domainId').closest('.form-group').addClass('has-error'); 
   	   			 		success1.hide();
   	   			 		error1.show();
   	   			 		App.scrollTo(error1, -200);
   	   			 	}
			 	} else{
			 		//Change style to red
			 		error1.find('span').text(flowDomainCreateReg.alreadyOwnsDomains);
			 		$('#domainId').closest('.form-group').addClass('has-error'); 
			 		success1.hide();
			 		error1.show();
			 		App.scrollTo(error1, -200);
			 	}
   			 	
            					
			}
        });
    }
	
	return{		

		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return flowDomainCreateReg = Data;
		},
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';			
			handleValidation();
		},
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		// CHECK FOR NON DUPLICATE PROPERTIES
		checkProperty: function(obj){
			
			checkDomainNameAvailable($('#identification').val());
			 var error1 = $('.alert-danger');
		     var success1 = $('.alert-success');
		     
			if (ontologyExist) {
	             error1.hide();
			} else{
				console.log('Domain Identification is not available.');
				//Change style to red
				error1.find('span').text("Domain must be unique.");
				$('#domainId').closest('.form-group').addClass('has-error'); 
				success1.hide();
	            error1.show();
			}
		},
		deleteFlowDomain: function(flowDomainId){
			logControl ? console.log(LIB_TITLE + ': deleteFlowDomain()') : '';	
			deleteFlowDomainConfirmation(flowDomainId);			
		},
	};
		
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// GLOBAL JSON AND CODE EDITOR INSTANCES
	var editor;
	var aceEditor;
	var schema = ''; // current schema json string var
		
	FlowDomainCreateController.load(flowDomainCreateJson);
	
	// AUTO INIT CONTROLLER.
	FlowDomainCreateController.init();
});

	