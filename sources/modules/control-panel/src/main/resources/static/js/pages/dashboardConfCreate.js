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
	
	var globalStylesValue = "";
	var initialStyleValue = '{"header":{"title":"My Dashboard","enable":true,"height":72,"logo":{"height":48},"backgroundColor":"#FFFFFF","textColor":"#060E14","iconColor":"#060E14","pageColor":"#2e6c99"},"navigation":{"showBreadcrumbIcon":true,"showBreadcrumb":true},"pages":[{"title":"New Page","icon":"apps","background":{"file":[]},"layers":[{"gridboard":[{}],"title":"baseLayer","$$hashKey":"object:23"}],"selectedlayer":0,"combinelayers":false,"$$hashKey":"object:4"}],"gridOptions":{"gridType":"fit","compactType":"none","margin":3,"outerMargin":true,"mobileBreakpoint":640,"minCols":20,"maxCols":100,"minRows":20,"maxRows":100,"maxItemCols":5000,"minItemCols":1,"maxItemRows":5000,"minItemRows":1,"maxItemArea":25000,"minItemArea":1,"defaultItemCols":4,"defaultItemRows":4,"fixedColWidth":250,"fixedRowHeight":250,"enableEmptyCellClick":false,"enableEmptyCellContextMenu":false,"enableEmptyCellDrop":true,"enableEmptyCellDrag":false,"emptyCellDragMaxCols":5000,"emptyCellDragMaxRows":5000,"draggable":{"delayStart":100,"enabled":true,"ignoreContent":true,"dragHandleClass":"drag-handler"},"resizable":{"delayStart":0,"enabled":true},"swap":false,"pushItems":true,"disablePushOnDrag":false,"disablePushOnResize":false,"pushDirections":{"north":true,"east":true,"south":true,"west":true},"pushResizeItems":false,"displayGrid":"none","disableWindowResize":false,"disableWarnings":false,"scrollToNewItems":true,"api":{}},"interactionHash":{"1":[]}}';       

	// CONTROLLER PRIVATE FUNCTIONS	
	
	
	
	var navigateUrl = function(url){ window.location.href = url; }
	// DELETE DASHBOARDCONF
	var deleteDashboardConfConfirmation = function(dashboardconfId){
		console.log('deleteDashboardConfConfirmation() -> formId: '+ dashboardconfId);
		
		// no Id no fun!
		if ( !dashboardconfId ) {toastr('NO INITIAL DASHBOARD CONF SELECTED!',''); return false; }
		
		logControl ? console.log('deleteDashboardConfConfirmation() -> formAction: ' + $('.delete-gadget').attr('action') + ' ID: ' + $('.delete-gadget').attr('userId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogDashboardConf('delete_dashboardconf_form');	
	}
	
	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/dashboardconf/freeResource/" + id).done(
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
	
	
	
	// INIT CODEMIRROR
	var handleVS = function () {
		logControl ? console.log('handleCodeMirror() on -> templateCode') : '';	
		
        var myTextArea = document.getElementById('templateCode');
        var myTextAreaJS = document.getElementById('templateCodeJS');
        
        var htmlelement = document.getElementById('htmlcode');
        
        if(!$("#id").val() && ($('#templateCode').text().trim().length == 0 && $('#templateCodeJS').text().trim().length == 0)){
        	
        	myTextArea.value = globalStylesValue;
        	myTextAreaJS.value = initialStyleValue;       
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
        
        myVSHTML.getModel().setValue(globalStylesValue);
        myVSHTML.trigger(globalStylesValue, 'editor.action.formatDocument')
        myVSJS.getModel().setValue(initialStyleValue);
        myVSJS.trigger(initialStyleValue, 'editor.action.formatDocument')
        
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> resetForm') : '';		
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('dashboardconf_form');
		});	
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
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
			initTemplateElements();
		},
		
		// REDIRECT
		go: function(id,url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			freeResource(id,url);
			
		},

		navigateUrl: function(url){
		    navigateUrl(url);
		},
	
		// DELETE INITIAL DASHBOARD CONF
		deleteDashboardConf: function(id){
			logControl ? console.log(LIB_TITLE + ': deleteDashboardConf()') : '';	
			deleteDashboardConfConfirmation(id);			
		},
	
		
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	DashboardConfController.load(dashboardConfJson);	
		
	// AUTO INIT CONTROLLER.
	DashboardConfController.init();
});
