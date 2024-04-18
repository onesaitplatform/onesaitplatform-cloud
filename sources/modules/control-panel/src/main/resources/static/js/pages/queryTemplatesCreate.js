var QueryTemplatesCreateController = function() {
    
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
	// DELETE QUERY TEMPLATE
	var deleteQueryTemplateConfirmation = function(templateId){
		console.log('deleteQueryTemplateConfirmation() -> formId: '+ templateId);
		
		// no Id no fun!
		if ( !templateId ) {$.alert({title: 'ERROR!',type: 'red' , theme: 'dark', content: 'NO USER-FORM SELECTED!'}); return false; }
		
		logControl ? console.log('deleteQueryTemplateConfirmation() -> formAction: ' + $('.delete-template').attr('action') + ' ID: ' + $('.delete-template').attr('userId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogQueryTemplate('delete_template_form');	
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
			/*EDITION MODE*/
			/*Hide dimensions*/
			/*if(!$("[name='id']").val()){
				$("#dimensionsPanel").hide();
			}*/
			initDatapicker();
			
			// INPUT MASK FOR ontology identification allow only letters, numbers and -_
			$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
			
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
