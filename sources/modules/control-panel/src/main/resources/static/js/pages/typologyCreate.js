var TypologyCreateController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var LIB_TITLE = 'Typology Controller';
	var logControl = 1;


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
