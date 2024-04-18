var WebProjectCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Web Project Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';
	var stateSwitch = false;
	var webProjectModified = false;
	var webProjectUploaded = false;
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		
        var form1 = $('#webproject_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not(.selectpicker)", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// validation rules
            rules: {
				identification:		{ required: true, minlength: 5 },
				description:		{ required: true, minlength: 5 },
            },
            invalidHandler: function(event, validator) { //display error alert on form submit
            	toastr.error(messagesForms.validation.genFormError);
				
            	if(stateSwitch==false && $("#buttonLoadRootZip").val() == ""){
            		toastr.error(webProjectCreateJson.validform.ziprequired);
            	}
			
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
	
				if(stateSwitch){					
						toastr.success(messagesForms.validation.genFormSuccess);             
	                    form.submit();					
				}else{
	            	if ($("#buttonLoadRootZip").val() != "" || $("#createMode").val() == "Git" ||
	            		(webProjectCreateJson.actionMode != null && !webProjectModified) ||
	            		(webProjectCreateJson.actionMode != null && webProjectModified && webProjectUploaded)){
	            		toastr.success(messagesForms.validation.genFormSuccess);             
	                    form.submit();
	            	} else {	            		 
	            		toastr.error(webProjectCreateJson.validform.ziprequired);
	            	}
				}			
            }
        });
    }
	
		
		
		var showHideUseTemplate = function () {
			$('#div-uploadZIP').toggle('hide');
			$('#div-usePlatformTemplate').toggle('hide');
			stateSwitch= !stateSwitch;			 
			if(stateSwitch){			 
				deployWebFront();
			}
		}
		
		function deployWebFront(){
		
				$('#mainFile').val('index.html');
				WebProjectCreateController.uploadWebTemplate();

		
		
		}
		
	
    var uploadZip = function (){
		var zipNameS = $('#buttonLoadRootZip')[0].files[0].name;
		$('#zipNameS').text(zipNameS); 
		
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
    	$('#updateBtn').attr('disabled','disabled');
    	$('#createBtn').attr('disabled','disabled');    	
    	$('#deleteBtn').attr('disabled','disabled');    	
    	$('#resetBtn').attr('disabled','disabled');
    	webProjectUploaded = false;
    	webProjectModified = true;
		App.blockUI({target:"#createWebprojectDiv",boxed: true, overlayColor:"#5789ad",type:"loader",state:"warning",message:"Uploading Web Project..."});
    	$.ajax({
            type: 'post',
            url: '/controlpanel/webprojects/uploadZip',
            headers: {
				[csrf_header]: csrf_value
		    },
            contentType: false,
            processData: false,
            data: new FormData($('#upload_zip')[0]),
            success: function () {
            	$('#updateBtn').removeAttr('disabled');
            	$('#createBtn').removeAttr('disabled'); 
            	$('#deleteBtn').removeAttr('disabled');    	
            	$('#resetBtn').removeAttr('disabled'); 
            	webProjectUploaded = true;
				toastr.success(webProjectCreateJson.messages.validationZIP,'');	
            },
            error: function(xhr){
            	webProjectUploaded = false;
            	toastr.error(messagesForms.operations.genOpError + ':', xhr.responseText);
    			return false;
            },
			complete:function(){					
				App.unblockUI("#createWebprojectDiv");				
			}
        });
    }

	 var uploadWebTemplate = function (){
		
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
    	$('#updateBtn').attr('disabled','disabled');
    	$('#createBtn').attr('disabled','disabled');
    	$('#deleteBtn').attr('disabled','disabled');
    	$('#resetBtn').attr('disabled','disabled');
   	
		App.blockUI({target:"#createWebprojectDiv",boxed: true, overlayColor:"#5789ad",type:"loader",state:"warning",message:"Uploading Web Project..."});
    	$.ajax({
            type: 'post',
            url: '/controlpanel/webprojects/uploadWebTemplate',
            headers: {
				[csrf_header]: csrf_value
		    },
            contentType: false,
            processData: false,
            success: function () {
            	$('#updateBtn').removeAttr('disabled');
            	$('#createBtn').removeAttr('disabled');
            	$('#deleteBtn').removeAttr('disabled');
            	$('#resetBtn').removeAttr('disabled');
				toastr.success(webProjectCreateJson.messages.validationZIP,'');	
				
            },
            error: function(xhr){
            	toastr.error(messagesForms.operations.genOpError + ':', xhr.responseText);
    			return false;
            },
			complete:function(){					
				App.unblockUI("#createWebprojectDiv");				
			}
        });
    }
    
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/webprojects/freeResource/" + id).done(
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
		
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('webproject_create_form');
			var $el = $('#buttonLoadRootZip');
			$el.wrap('<form>').closest('form').get(0).reset();
			$el.unwrap();
			$('#zipNameS').text("");
		});
		
		// Fields OnBlur validation
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		
		
		$('.tagsinput').filter('[required]').parent().on('blur', 'input', function(event) {
			if ($(event.target).parent().next().val() !== ''){
				$(event.target).parent().next().nextAll('span:first').addClass('hide');
				$(event.target).parent().removeClass('tagsinput-has-error');
			} else {
				$(event.target).parent().next().nextAll('span:first').removeClass('hide');
				$(event.target).parent().addClass('tagsinput-has-error');
			}   
		})
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
			handleValidation();
		},

		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		// uploadZip
		uploadZip: function(url){
			logControl ? console.log(LIB_TITLE + ': uploadZip()') : '';	
			uploadZip(); 
		},// uploadZip
		uploadWebTemplate: function(url){
			logControl ? console.log(LIB_TITLE + ': uploadZip()') : '';	
			uploadWebTemplate(); 
		},
		cancel: function(id,url){
			logControl ? console.log(LIB_TITLE + ': cancel()') : '';
			
			freeResource(id,url);
		},
		submitform: function(){
			$("#webproject_create_form").submit();
		},
		showHideUseTemplate: function(){
			showHideUseTemplate();
		}
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	WebProjectCreateController.load(webProjectCreateJson);	
		
	// AUTO INIT CONTROLLER.
	WebProjectCreateController.init();
});
