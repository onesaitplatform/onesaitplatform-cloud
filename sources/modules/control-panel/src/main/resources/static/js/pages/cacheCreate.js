var CacheCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Cache Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	
	// CONTROLLER PRIVATE FUNCTIONS	

	
	$("#createBtn").on('click',function(){
		event.preventDefault(); 
		if($("#identification").val()!='' && $("#identification").val()!=undefined && $("#size").val()!='' && $("#size").val()!=undefined){
			CacheCreateController.submitform();
		}else{
			$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: cacheCreateJson.validform.emptyfields});
			return false;
		}
		
	});
	
	$("#updateBtn").on('click',function(){
		event.preventDefault(); 
		if($("#identification").val()!='' && $("#identification").val()!=undefined){
			CacheCreateController.submitform();
		}else{
			$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: cacheCreateJson.validform.emptyfields});
			return false;
		}
		
	});
	
    
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
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
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
		
	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() ->  resetForm,  currentLanguage: ' + currentLanguage) : '';
		
		// 	INPUT MASK FOR WEB PROJECT identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });

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
			initTemplateElements();
		},

		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		submitform: function(){
		
			$("#cache_create_form").submit();
		},
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	CacheCreateController.load(cacheCreateJson);	
		
	// AUTO INIT CONTROLLER.
	CacheCreateController.init();
});