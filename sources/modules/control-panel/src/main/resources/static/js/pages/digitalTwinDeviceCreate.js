
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
	
	
	$("#createBtn").on('click',function(e){
		var editorLogic = $('.CodeMirror')[0].CodeMirror;
		if($("#identification").val()!='' && $("#identification").val()!=undefined){
			if(pattern.test($("#identification").val())){
				$("#logic").val(JSON.stringify(editorLogic.getValue().trim()));
				$("#typeSelected").val($("#typeDigitalTwin").val());
				$("#createBtn").val('Please wait ...')
			      .attr('disabled','disabled');
				DigitalTwinCreateController.submitform();
			}
		}else{
			$.alert({title: 'ERROR!', theme: 'light',  content: digitalTwinCreateJson.validations.schema});
			return false;
		}
	});
	
	
	
	$("#updateBtn").on('click',function(){
		var editorLogic = $('.CodeMirror')[0].CodeMirror;
		if($("#identification").val()!='' && $("#identification").val()!=undefined){
			if(pattern.test($("#identification").val())){
				$("#logic").val(JSON.stringify(editorLogic.getValue().trim()));
				$("#typeSelected").val($("#typeDigitalTwin").val());
				$("#createBtn").val('Please wait ...')
			      .attr('disabled','disabled');
				DigitalTwinCreateController.submitform();
			}
		}else{
			$.alert({title: 'ERROR!', theme: 'light',  content: digitalTwinCreateJson.validations.schema});
			return false;
		}
		
	});
	
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
		myCodeMirror.setSize("100%", 350);
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
				var types = $("#typesDigitalTwin").val().replace("[","").replace("]","").split(",");
				$.each(types, function(key, object){
					$("#typeDigitalTwin").append("<option id='"+object+"' value='"+object+"'>"+object+"</option>");
				});
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
				
				var editorLogic = $('.CodeMirror')[0].CodeMirror;
				var logica = digitalTwinCreateJson.logic;
					
				if(logica.charAt(0) === '\"'){
					logica = logica.substr(1, logica.length-2);
				}
				editorLogic.setValue(logica)
			}
			
			
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
		submitform: function(){			
			$("#digitaltwindevice_create_form").submit();
		},
		
		// DELETE DIGITAL TWIN DEVICE 
		deleteDigitalTwinDevice: function(digitalTwinDeviceId){
			logControl ? console.log(LIB_TITLE + ': deleteDigitalTwinDevice()') : '';	
			deleteDigitalTwinDeviceConfirmation(digitalTwinDeviceId);			
		},
	}
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	DigitalTwinCreateController.load(digitalTwinCreateJson);
	
	DigitalTwinCreateController.init();
});
