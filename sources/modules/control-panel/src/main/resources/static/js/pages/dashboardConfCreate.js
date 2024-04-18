var DashboardConfController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Dashboard Conf Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	//var myCodeMirror;
	//var myCodeMirrorJS;
	var myVSHTML;
	var myVSHTML_isfullscreen;
	var myVSJS;
	var myVSJS_isfullscreen;
	var timerWrite;
	// CONTROLLER PRIVATE FUNCTIONS	
	
	
	
	var navigateUrl = function(url){ window.location.href = url; }
	// DELETE DASHBOARDCONF
	var deleteDashboardConfConfirmation = function(dashboardconfId){
		console.log('deleteDashboardConfConfirmation() -> formId: '+ dashboardconfId);
		
		// no Id no fun!
		if ( !dashboardconfId ) {$.alert({title: 'ERROR!',type: 'red' , theme: 'dark', content: 'NO INITIAL DASHBOARD CONF SELECTED!'}); return false; }
		
		logControl ? console.log('deleteDashboardConfConfirmation() -> formAction: ' + $('.delete-gadget').attr('action') + ' ID: ' + $('.delete-gadget').attr('userId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogDashboardConf('delete_dashboardconf_form');	
	}
	
	// INIT CODEMIRROR
	var handleVS = function () {
		logControl ? console.log('handleCodeMirror() on -> templateCode') : '';	
		
        var myTextArea = document.getElementById('templateCode');
        var myTextAreaJS = document.getElementById('templateCodeJS');
        
        var htmlelement = document.getElementById('htmlcode');
        
        if(!$("#id").val() && ($('#templateCode').text().trim().length == 0 && $('#templateCodeJS').text().trim().length == 0)){
        	
        	myTextArea.value = "";
        	myTextAreaJS.value = '{"header":{"title":"My Dashboard","enable":true,"height":72,"logo":{"height":48},"backgroundColor":"#FFFFFF","textColor":"#060E14","iconColor":"#060E14","pageColor":"#2e6c99"},"navigation":{"showBreadcrumbIcon":true,"showBreadcrumb":true},"pages":[{"title":"New Page","icon":"apps","background":{"file":[]},"layers":[{"gridboard":[{}],"title":"baseLayer","$$hashKey":"object:23"}],"selectedlayer":0,"combinelayers":false,"$$hashKey":"object:4"}],"gridOptions":{"gridType":"fit","compactType":"none","margin":3,"outerMargin":true,"mobileBreakpoint":640,"minCols":20,"maxCols":100,"minRows":20,"maxRows":100,"maxItemCols":5000,"minItemCols":1,"maxItemRows":5000,"minItemRows":1,"maxItemArea":25000,"minItemArea":1,"defaultItemCols":4,"defaultItemRows":4,"fixedColWidth":250,"fixedRowHeight":250,"enableEmptyCellClick":false,"enableEmptyCellContextMenu":false,"enableEmptyCellDrop":true,"enableEmptyCellDrag":false,"emptyCellDragMaxCols":5000,"emptyCellDragMaxRows":5000,"draggable":{"delayStart":100,"enabled":true,"ignoreContent":true,"dragHandleClass":"drag-handler"},"resizable":{"delayStart":0,"enabled":true},"swap":false,"pushItems":true,"disablePushOnDrag":false,"disablePushOnResize":false,"pushDirections":{"north":true,"east":true,"south":true,"west":true},"pushResizeItems":false,"displayGrid":"none","disableWindowResize":false,"disableWarnings":false,"scrollToNewItems":true,"api":{}},"interactionHash":{"1":[]}}';       
        }
        
        myVSHTML = monaco.editor.create(htmlelement, {
    		value: myTextArea.value,
    		language: 'html',
    		readOnly: myTextArea.disabled,
    		scrollBeyondLastLine: false,
    		theme: "vs-dark",
    		automaticLayout: true
    	});
        
        var valueMyVSJS ="";
        try {
			valueMyVSJS = JSON.stringify(JSON.parse(myTextAreaJS.value), null, 2);
		} catch (e) {
			valueMyVSJS = myTextAreaJS.value;
		}
        
        myVSJS = monaco.editor.create(document.getElementById('jscode'), {
    		value: valueMyVSJS,
    		language: 'json',
    		readOnly: myTextArea.disabled,
    		scrollBeyondLastLine: false,
    		theme: "vs-dark",
    		automaticLayout: true
    	});
     
        
        
        myVSHTML.addCommand(monaco.KeyCode.F11, function() {
        	if(!myVSHTML_isfullscreen){
	        	document.getElementById("htmlcode").style.maxWidth = "100%";
	            document.getElementById("htmlcode").style.maxHeight = "100%";
	            document.getElementById("htmlcode").style.height = "100%";
	            document.getElementById("htmlcode").style.left = "0";
	            document.getElementById("htmlcode").style.right = "0";
	            document.getElementById("htmlcode").style.top = "0";
	            document.getElementById("htmlcode").style.bottom = "0";
	            document.getElementById("htmlcode").style.position = "fixed";
	            document.getElementById("htmlcode").style.zIndex = "1000";
	            myVSHTML_isfullscreen=true;
        	}
        	else{
        		document.getElementById("htmlcode").style.maxWidth = "";
	            document.getElementById("htmlcode").style.maxHeight = "";
	            document.getElementById("htmlcode").style.height = "400px";
	            document.getElementById("htmlcode").style.left = "";
	            document.getElementById("htmlcode").style.right = "";
	            document.getElementById("htmlcode").style.top = "";
	            document.getElementById("htmlcode").style.bottom = "";
	            document.getElementById("htmlcode").style.position = "";
	            document.getElementById("htmlcode").style.zIndex = "";
	            myVSHTML_isfullscreen=false;
        	}
        });
        
        myVSHTML.addCommand(monaco.KeyCode.Escape, function() {
        	document.getElementById("htmlcode").style.maxWidth = "";
            document.getElementById("htmlcode").style.maxHeight = "";
            document.getElementById("htmlcode").style.height = "400px";
            document.getElementById("htmlcode").style.left = "";
            document.getElementById("htmlcode").style.right = "";
            document.getElementById("htmlcode").style.top = "";
            document.getElementById("htmlcode").style.bottom = "";
            document.getElementById("htmlcode").style.position = "";
            document.getElementById("htmlcode").style.zIndex = "";
            myVSHTML_isfullscreen=false;
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
          
        
        myVSHTML.onDidChangeModelContent(function() {
        	if(timerWrite){
        		clearTimeout(timerWrite)
        	}
        	timerWrite = window.setTimeout(
        			function(){
        				$('#templateCode').val(myVSHTML.getValue());
        				$('#templateCodeJS').val(myVSJS.getValue());
        				//updatePreview();
        			},
        			1000
        	);
    	})

    	myVSJS.onDidChangeModelContent(function() {
    		if(timerWrite){
        		clearTimeout(timerWrite)
        	}
        	timerWrite = window.setTimeout(
        			function(){
        				$('#templateCode').val(myVSHTML.getValue());
        				$('#templateCodeJS').val(myVSJS.getValue());
        			//	updatePreview();
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

		var form1 = $('#dashboardconf_form');
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
						identification : {
							minlength : 5,
							required : true
						},
						description : {
							minlength : 5,
							required : true
						},

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
	
		// DELETE INITIAL DASHBOARD CONF
		deleteDashboardConf: function(id){
			logControl ? console.log(LIB_TITLE + ': deleteDashboardConf()') : '';	
			deleteDashboardConfConfirmation(id);			
		}
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	DashboardConfController.load(dashboardConfJson);	
		
	// AUTO INIT CONTROLLER.
	DashboardConfController.init();
});
