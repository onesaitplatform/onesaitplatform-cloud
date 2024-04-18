var MainDashboard = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Dashboard counters fragment Controller';	
    var logControl = 0;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.
	
	// CONTROLLER PRIVATE FUNCTIONS
	
		
	// HANDLE COUNTERUPs
	var handleCounterup = function() {
        if (!$().counterUp) {
            return;
        }

        $("[data-counter='counterup']").counterUp({
            delay: 10,
            time: 1000
        });
    };
	
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return dashboardReg = Data;
		},
		
		lang: function(lang){
			logControl ? console.log(LIB_TITLE + ': lang()') : '';
			logControl ? console.log('|---> lang() -> assign current Language to Dashboard: ' + lang) : '';
			return currentLanguage = lang;
			
		},
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			
			// init counterups
			handleCounterup();		
				
		}		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	MainDashboard.load(dashboardJson);
	// LOADING CURRENT LANGUAGE FROM THE TEMPLATE
	MainDashboard.lang(currentLanguage);	
	// AUTO INIT CONTROLLER.
	MainDashboard.init();
});
