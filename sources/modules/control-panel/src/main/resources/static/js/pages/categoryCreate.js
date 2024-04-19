var categoryCreateController = function(){
	
	// DEFAULT PARAMETERS, VAR, CONSTS
	var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'CATEGORY Controller';
	logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';
	var formId = "tenant_form";
	var subcategories = [];
	
    var form1 = $('#category_create_form');
	
	// CONTROLLER PRIVATE FUNCTIONS
	

	var handleValidation = function() {
		
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
		// set current language
		currentLanguage = currentLanguage || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// validation rules
            rules: {
            	identification:		{required: true, minlength: 5},
            	description:		{required: true, minlength: 5},
            },
            messages: {
            	identification: 	{required: categoryCreateJson.validform.emptyfields, minlength: categoryCreateJson.validform.minLength},
            	description:		{required: categoryCreateJson.validform.emptyfields, minlength: categoryCreateJson.validform.minLength},
            },
            invalidHandler: function(event, validator) { //display error alert on form submit
            	toastr.error(messagesForms.validation.genFormError,'');
            },
            errorPlacement: function(error, element) {
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list, .radio-inline")); }
				else { error.insertAfter(element); }
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
            	toastr.success(messagesForms.validation.genFormSuccess,'');
                form.submit();
            }
        });
    }
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		
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
		logControl ? console.log('initTemplateElements() -> resetForm') : '';

		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('category_create_form');
		});	
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});
		
		$(".option a[href='#tab_1']").on("click", function(e) {
	        $('.tabContainer').find('.option').removeClass('active');
	        $('#tab-general-information').addClass('active');
	    });
	    
		$(".option a[href='#tab_2']").on("click", function(e) {
	        $('.tabContainer').find('.option').removeClass('active');
	        $('#tab-subcategories').addClass('active');
	    });
		
		if(typeof $('#categories > tbody > tr').length =='undefined' || $('#authorizations > tbody > tr').length == 0){
			$('#imageNoElementsOnTable').show();
		}else{
			$('#imageNoElementsOnTable').hide();
		}

	}
	
	var addSubcategory = function(category_id){
 		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		var identification = $('#subcategory-identification').val();
		var description = $('#subcategory-description').val();
		
		if (identification == undefined || identification == null || identification == ''
			|| description == undefined || description == null || description == '') {
		
			toastr.error(messagesForms.validation.genFormError,'');
		
		} else {			
			$.ajax({
			    url: '/controlpanel/categories/addSubcategory/' + category_id,
			    headers: {
					[csrf_header]: csrf_value
			    },
			    type: 'POST',		
			    async: false,
			    data: {'identification': identification, 'description': description, 'id': null},
			    error: function(response) {
					toastr.error(messagesForms.operations.genOpError, response.responseText);
			    },
			    success: function(result) {  
			    	$('#subcategory-identification').val('');
			    	$('#subcategory-description').val('');
			    	
			    	$('#category_subcategories > tbody')
					.append(
							'<tr class="subcategory-model"><td>'
									+ result.identification
									+ '</td><td >'
									+ result.description
									+ '</td><td class="text-center"><button type="button" data-property="" class="btn btn-xs btn-no-border icon-on-table color-red tooltips" style="background-color:transparent;" onclick="categoryCreateController.removeSubcategory(this,\''
									+ result.id 
									+ '\')"><i class="icon-delete"></i></button></td></tr>');			    	
			    	$('#subcategories').removeClass('hide');
					$('#imageNoElementsOnTable').hide();
			    	toastr.success(messagesForms.operations.genOpSuccess,'');
			    	
			    	
			    }
			});
		}
	}
	
	var removeSubcategory = function(element, subcategory_id) {
 		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		$.ajax({
		    url: '/controlpanel/categories/removeSubcategory/' + subcategory_id,
		    headers: {
				[csrf_header]: csrf_value
		    },
		    type: 'POST',		
		    async: false,
		    error: function() {
				toastr.error(messagesForms.operations.genOpError,'');
		    },
		    success: function(result) {  
		    	element.parentElement.parentElement.remove();
		    	
		    	if ($("#category_subcategories > tbody > tr").length == 0) {
		    		$('#subcategories').addClass('hide');
					$('#imageNoElementsOnTable').show();
				}
		    	toastr.success(messagesForms.operations.genOpSuccess,'');	
		    }
		});
 		

	}
	
	
	function initSubcategories(subcategoriesList) {
		if(categoryCreateJson.actionMode != null){
			if(subcategoriesList.length > 0) {
				$.each( subcategoriesList, function (key, object) {
					subcategories.push(object);
				});	
			
				$('#subcategories').removeClass('hide');
				$('#imageNoElementsOnTable').hide();
			}
		}
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
			initSubcategories(categoryCreateJson.subcategories);
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		submitform: function(){
			$("#category_create_form").submit();
		},
		
		addSubcategory: function(category_id) {
			addSubcategory(category_id);
		},
		
		removeSubcategory: function(element, subcategory_id) {
			removeSubcategory(element, subcategory_id);
		}
	};
	
}();


//AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	categoryCreateController.load(categoryCreateJson);	
		
	// AUTO INIT CONTROLLER.
	categoryCreateController.init();
	
});