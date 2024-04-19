
var DigitalTwinCreateController = function() {
	
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'Onesait Platform Control Panel'; 
	var LIB_TITLE = 'Digital Twin Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var hasId = false; // instance
	var AceEditor;
	var pattern= /^[A-Za-z_-]+$/;
	// CONTROLLER PRIVATE FUNCTIONS	--------------------------------------------------------------------------------
	
	
	// CHECK IF JSON STRING WHEN JSON PARSE IS OK OR NOT, IF THE JSON IS MALFORMED THE JSON.parse() is broken.
	var IsJsonString = function(str) {
		try {
			JSON.parse(str);
		}
		catch (e) {	return false; }
		
		return true;
	}
	
	
	// DELETE DIGITAL TWIN TYPE
	var deleteDigitalTwinDeviceConfirmation = function(digitalTwinDeviceId){
		console.log('deleteDigitalTwinDeviceConfirmation() -> formId: '+ digitalTwinDeviceId);
		
		// no Id no fun!
		if ( !digitalTwinDeviceId ) {$.alert({title: 'ERROR!',  theme: 'light', content: digitalTwinCreateJson.validations.validform}); return false; }
		
		logControl ? console.log('deleteDigitalTwinDeviceConfirmation() -> formAction: ' + $('.delete-digital').attr('action') + ' ID: ' + $('#delete-digitaltwindeviceId').attr('digitaltwindeviceId')) : '';
		
		// call digital twin device Confirm at header. 
		HeaderController.showConfirmDialogDigitalTwinDevice('delete_digitaltwindevice_form');	
	}
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/digitaltwindevices/freeResource/" + id).done(
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
	
	$("#button3").on('click', function(){
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		$.ajax({
			url : "/controlpanel/digitaltwindevices/generateToken",
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'GET',
			dataType: 'text', 
			contentType: 'text/plain',
			mimeType: 'text/plain',
			success : function(data) {
				if (data!="" && data != undefined) {
					$("#apiKey").val(data);
					$('.form').validate().element('#apiKey');   
				} else {
					$.alert({title: 'ERROR!', theme: 'light',  content: 'error'}); 
				}
			},
			error : function(data, status, er) {
				$.alert({title: 'ERROR!', theme: 'light',  content: er}); 
			}
		});
	})
	
	$("#typeDigitalTwin").on('change',function(){
		changeDigitalTwinType($("#typeDigitalTwin").val());
	});
	
	var changeDigitalTwinType = function(type){
		var editorLogic = $('.CodeMirror')[0].CodeMirror;
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		$.ajax({
			url : "/controlpanel/digitaltwindevices/getLogicFromType/"+type.trim(),
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'GET',
			dataType: 'text',
			contentType: 'text/plain',
			mimeType: 'text/plain',
			success : function(data) {
				if (data!="" && data != undefined) {
					
					if(data.startsWith("\"") || data.startsWith("'")){
						data.substring(1,data.length-1);
					}
					editorLogic.setValue(data);
				} else {
					$.alert({title: 'ERROR!', theme: 'light',  content: 'error'}); 
				}
			},
			error : function(data, status, er) {
				$.alert({title: 'ERROR!', theme: 'light',  content: er}); 
			}
		});
	}
	
	// INIT CODEMIRROR
	var handleCodeMirror = function () {
		logControl ? console.log('handleCodeMirror() on -> logicEditor') : '';	
		
		// INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
        var myTextArea = document.getElementById('logicEditor');
        var myCodeMirror = CodeMirror.fromTextArea(myTextArea, {
        	mode: "text/javascript",
            lineNumbers: false,
            foldGutter: true,
            matchBrackets: true,
            styleActiveLine: true,
            theme:"elegant",    
            readOnly: true

        });
		myCodeMirror.setSize("100%", 270);
    }


// FORM VALIDATION
		var form1 = $('#digitaltwindevice_create_form');
	var handleValidation = function() {

		form1
				.validate({
				errorElement: 'span', //default input error message container
	            errorClass: 'help-block help-block-error', // default input error message class
	            focusInvalid: false, // do not focus the last invalid input
	            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
				lang: currentLanguage,
					// validation rules
					rules : {
						identification : {
							minlength : 5,							
							required : true
						},
						latitude : {		
							required : true
						},
						longitude : {		
							required : true
						},
						interface : {		
							required : true
						},
						port : {		
							required : true
						},
						contextPath : {		
							required : true
						},
						endpoint : {		
							required : true
						},
						digitalKey : {		
							required : true
						}
					},
					invalidHandler : function(event, validator) { // display
						// error alert on form submit
						toastr.error(messagesForms.validation.genFormError,'');
					},
					errorPlacement : function(error, element) {
						if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
						else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }
						else 								{ error.insertAfter(element); }
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
						var editorLogic = $('.CodeMirror')[0].CodeMirror;		
						$("#logic").val(JSON.stringify(editorLogic.getValue().trim()));
						$("#typeSelected").val($("#typeDigitalTwin").val());
						toastr.success(messagesForms.validation.genFormSuccess,'');
					 	form.submit();
					}
				});
	}
	 var initTemplateElements = function(){
			
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
				$('.form').validate().element('#' + event.target.id);                // checks form for validity
			});			
		
		
		
		}

	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return digitalTwinCreateJson = Data;
		},

		// INIT() CONTROLLER INIT CALLS
		init: function(){
			
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleCodeMirror();
			// PROTOTYPEs
			// ARRAY PROTOTYPE FOR CHECK UNIQUE PROPERTIES.
			Array.prototype.unique = function() {
				return this.filter(function (value, index, self) { 
					return self.indexOf(value) === index;
				});
			};
			
			// ARRAY PROTROTYPE FOR REMOVE ELEMENT (not object) BY VALUE
			Array.prototype.remove = function() {
				var what, a = arguments, L = a.length, ax;				
				while (L && this.length) {
					what = a[--L];				
					while ((ax = this.indexOf(what)) !== -1) {
						console.log('AX: ' + ax);
						this.splice(ax, 1);
					}
				}
				return this;
			};
			
			//LOAD DIGITAL TWIN TYPES 
			logControl ? console.log('|---> Load Digital Twin Types') : '';
			if($("#typesDigitalTwin").val()!="" && $("#typesDigitalTwin").val()!=undefined){
				var types = $("#typesDigitalTwin").val().replace("[","").replace("]","").split(", ");
				$.each(types, function(key, object){
					$("#typeDigitalTwin").append("<option id='"+object+"' value='"+object+"'>"+object+"</option>");
				});
				$("#typeDigitalTwin").selectpicker('refresh');
			}else{
				$.alert({title: 'ERROR!', theme: 'light',  content: digitalTwinCreateJson.validations.types}); 
				return false;
			}
			
			// INSERT MODE ACTIONS  (ontologyCreateReg.actionMode = NULL ) 
			if ( digitalTwinCreateJson.actionMode === null){
				logControl ? console.log('|---> Action-mode: INSERT') : '';
				changeDigitalTwinType($("#typeDigitalTwin").val());
			}
			// EDIT MODE ACTION 
			else {	
				logControl ? console.log('|---> Action-mode: UPDATE') : '';
				var type = $("#typeDigital").val();
				$("#typeDigitalTwin").val(type);
				$("#typeDigitalTwin").selectpicker('render');
				var editorLogic = $('.CodeMirror')[0].CodeMirror;
				var logica = digitalTwinCreateJson.logic;
					
				if(logica.charAt(0) === '\"'){
					logica = logica.substr(1, logica.length-2);
				}
				editorLogic.setValue(logica)
			}
			
			handleValidation();
			initTemplateElements();
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
		
		
		// DELETE DIGITAL TWIN DEVICE 
		deleteDigitalTwinDevice: function(digitalTwinDeviceId){
			logControl ? console.log(LIB_TITLE + ': deleteDigitalTwinDevice()') : '';	
			deleteDigitalTwinDeviceConfirmation(digitalTwinDeviceId);			
		},
	}
}();

// AUTO INIT CONTROLLER WHEN READY
$(window).load(function() {	
	
	DigitalTwinCreateController.load(digitalTwinCreateJson);
	
	DigitalTwinCreateController.init();
});
