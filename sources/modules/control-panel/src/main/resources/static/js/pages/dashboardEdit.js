var DashboardController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Dashboard Edit Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';
	var myVSJS;
	var myVSJS_isfullscreen;
	var timerWrite;
	// CONTROLLER PRIVATE FUNCTIONS	
	
	
	
	var navigateUrl = function(url){ window.location.href = url; }
	
	
	// INIT CODEMIRROR
	var handleVS = function () {
		logControl ? console.log('handleCodeMirror() on -> templateCode') : '';	
		
       
        var myTextAreaJS = document.getElementById('templateCodeJS');        
     
        
        var valueMyVSJS ="";
        try {
			valueMyVSJS = JSON.stringify(JSON.parse(myTextAreaJS.value), null, 2);
		} catch (e) {
			valueMyVSJS = myTextAreaJS.value;
		}
        
        myVSJS = monaco.editor.create(document.getElementById('jscode'), {
    		value: valueMyVSJS,
    		language: 'json',
    		readOnly: myTextAreaJS.disabled,
    		scrollBeyondLastLine: false,
    		theme: "vs-dark",
    		automaticLayout: true
    	});
     
        myVSJS.addCommand(monaco.KeyCode.F11, function() {
        	if(!myVSJS_isfullscreen){
	        	document.getElementById("jscode").style.maxWidth = "100%";
	            document.getElementById("jscode").style.maxHeight = "100%";
	            document.getElementById("jscode").style.height = "100%";
	            document.getElementById("jscode").style.left = "0";
	            document.getElementById("jscode").style.right = "0";
	            document.getElementById("jscode").style.top = "0";
	            document.getElementById("jscode").style.bottom = "0";
	            document.getElementById("jscode").style.position = "fixed";
	            document.getElementById("jscode").style.zIndex = "1000";
	            myVSJS_isfullscreen=true;
        	}
        	else{
        		document.getElementById("jscode").style.maxWidth = "";
	            document.getElementById("jscode").style.maxHeight = "";
	            document.getElementById("jscode").style.height = "400px";
	            document.getElementById("jscode").style.left = "";
	            document.getElementById("jscode").style.right = "";
	            document.getElementById("jscode").style.top = "";
	            document.getElementById("jscode").style.bottom = "";
	            document.getElementById("jscode").style.position = "";
	            document.getElementById("jscode").style.zIndex = "";
	            myVSJS_isfullscreen=false;
        	}
        });
        
        myVSJS.addCommand(monaco.KeyCode.Escape, function() {
        	document.getElementById("jscode").style.maxWidth = "";
            document.getElementById("jscode").style.maxHeight = "";
            document.getElementById("jscode").style.height = "400px";
            document.getElementById("jscode").style.left = "";
            document.getElementById("jscode").style.right = "";
            document.getElementById("jscode").style.top = "";
            document.getElementById("jscode").style.bottom = "";
            document.getElementById("jscode").style.position = "";
            document.getElementById("jscode").style.zIndex = "";
            myVSJS_isfullscreen=false;
        });
          
        
      
    	myVSJS.onDidChangeModelContent(function() {
    		if(timerWrite){
        		clearTimeout(timerWrite)
        	}
        	timerWrite = window.setTimeout(
        			function(){        				
        				$('#templateCodeJS').val(myVSJS.getValue());
        			},
        			1000
        	);
    	})
    }	
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#dashboard_form');
		var error1 = $('.alert-danger');
		var success1 = $('.alert-success');

		// set current language
		currentLanguage = templateCreateReg.language || LANGUAGE;
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
			return templateCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleVS();
			handleValidation();
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
	
		
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	DashboardController.load(dashboardConfJson);	
		
	// AUTO INIT CONTROLLER.
	DashboardController.init();
});
