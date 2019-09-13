var WebProjectCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Web Project Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	
	// CONTROLLER PRIVATE FUNCTIONS	

	
	$("#createBtn").on('click',function(){
		event.preventDefault(); 
		if($("#identification").val()!='' && $("#identification").val()!=undefined && $("#description").val()!='' && $("#description").val()!=undefined
				&& $('#buttonLoadRootZip')[0].files[0]!=undefined){
			WebProjectCreateController.submitform();
		}else{
			$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: webProjectCreateJson.validform.emptyfields});
			return false;
		}
		
	});
	
	$("#updateBtn").on('click',function(){
		event.preventDefault(); 
		if($("#identification").val()!='' && $("#identification").val()!=undefined && $("#description").val()!='' && $("#description").val()!=undefined
				&& $('#buttonLoadRootZip')[0].files[0]!=undefined){
			WebProjectCreateController.submitform();
		}else{
			$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: webProjectCreateJson.validform.emptyfields});
			return false;
		}
		
	});
    var uploadZip = function (){
		var zipNameS = $('#buttonLoadRootZip')[0].files[0].name;
		$('#zipNameS').text(zipNameS); 
		
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
    	$('#updateBtn').attr('disabled','disabled');
    	$('#createBtn').attr('disabled','disabled');    	
    	$('#deleteBtn').attr('disabled','disabled');    	
    	$('#resetBtn').attr('disabled','disabled');
		App.blockUI({target:"#createWebprojectPortlet",boxed: true, overlayColor:"#5789ad",type:"loader",state:"warning",message:"Uploading Web Project..."});
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
            },
            error: function(xhr){
            	$.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: xhr.responseText});
    			return false;
            },
			complete:function(){					
				App.unblockUI("#createWebprojectPortlet");				
			}
        });
    }
    
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
		
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('webproject_create_form');
			var $el = $('#buttonLoadRootZip');
			$el.wrap('<form>').closest('form').get(0).reset();
			$el.unwrap();
		});	
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
		
		// uploadZip
		uploadZip: function(url){
			logControl ? console.log(LIB_TITLE + ': uploadZip()') : '';	
			uploadZip(); 
		},
		
		submitform: function(){
		
			$("#webproject_create_form").submit();
		},
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	WebProjectCreateController.load(webProjectCreateJson);	
		
	// AUTO INIT CONTROLLER.
	WebProjectCreateController.init();
});