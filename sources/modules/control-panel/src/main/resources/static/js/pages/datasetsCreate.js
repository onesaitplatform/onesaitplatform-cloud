var datasetCreateController  = function(){
	
	var LIB_TITLE = 'Dataset Controller';
	logControl = 1;
	
	// CONTROLLER PRIVATE FUNCTIONS
	
	$('#resetBtn').on('click',function(){ 
		cleanFields('dataset_create_form');
	});
		
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
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		$('#public').prop('checked', false);
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}

	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#dataset_create_form');

		// set current language
		currentLanguage = datasetCreateJson.language || LANGUAGE;

		form1.validate({
					errorElement : 'span',
					errorClass : 'help-block help-block-error',
					focusInvalid : false,
					ignore : ":hidden:not(.selectpicker)",
					lang : currentLanguage,
					rules : {
						datasetTitle : {
							minlength : 5,
							required : true
						}, 
						datasetOrganization : {
							required: true
						},
						datasetLicense: {
							required: true
						}
					},
					invalidHandler : function(event, validator) {
						toastr.error(datasetCreateJson.validform.emptyfields,'');
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
					highlight : function(element) { 
						$(element).closest('.form-group').addClass('has-error');
					},
					unhighlight : function(element) { 
						$(element).closest('.form-group').removeClass(
								'has-error');
					},
					success : function(label) {
						label.closest('.form-group').removeClass('has-error');
					},
					// ALL OK, THEN SUBMIT.
					submitHandler : function(form) {
						$("#datasetName").val($("#datasetTitle").val().replace(/\s+/g,'-').toLowerCase());
						if(datasetCreateJson.actionMode == null){
							$("#dataset_create_form").ajaxSubmit({type: 'post', 
								success : function(data){
									$('#modal-created').modal('show');
								},
								error: function(data){
									HeaderController.showErrorDialog(data.responseText)
								}
							});
						} else {
							form.submit()
						}
					}
				});
	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> resetForm') : '';		
				
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('dataset_create_form');
		});
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		
		
		$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.getElementsByTagName('select')[0]){
				$('.form').validate().element('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id'));
			}
		});
		
		$("#btn-cancel").on('click', function (e) {
			e.preventDefault();
			window.location = '/controlpanel/opendata/datasets/list';
		});
	}	

	// DELETE DATASET
	var deleteDatasetConfirmation = function(datasetId){
		console.log('deleteDatasetConfirmation() -> formId: '+ datasetId);
		
		// set action and datasetId to the form
		$('.delete-dataset').attr('id',datasetId);
		$('.delete-dataset').attr('action','/controlpanel/opendata/datasets/delete/' + datasetId);
		console.log('deleteDatasetConfirmation() -> formAction: ' + $('.delete-dataset').attr('action') + ' ID: ' + $('.delete-dataset').attr('id'));
		
		// call dataset Confirm at header.
		HeaderController.showDatasetConfirmDialog(datasetId);	
	}
	
    var uploadFile = function (){
		var fileNameD = $('#buttonLoadFile')[0].files[0].name;
		$('#fileNameD').text(fileNameD); 
		
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
    	$('#updateBtn').attr('disabled','disabled');
    	$('#createBtn').attr('disabled','disabled');    	
    	$('#deleteBtn').attr('disabled','disabled');    	
    	$('#resetBtn').attr('disabled','disabled');
		App.blockUI({target:"#createDatasetPortlet",boxed: true, overlayColor:"#5789ad",type:"loader",state:"warning",message:"Uploading File..."});
    	$.ajax({
            type: 'post',
            url: '/controlpanel/opendata/datasets/uploadFile',
            headers: {
				[csrf_header]: csrf_value
		    },
            contentType: false,
            processData: false,
            dataType: 'json',
            data: new FormData($('#upload_file')[0]),
            success: function (response) {
            	$('#updateBtn').removeAttr('disabled');
            	$('#createBtn').removeAttr('disabled'); 
            	$('#deleteBtn').removeAttr('disabled');    	
            	$('#resetBtn').removeAttr('disabled');
            	// Append the option to select
          	    $('#datasetFiles').append('<option value="'+response.id+'">'+response.name+'</option>');
          	    // Refresh the selectpicker
          	    $("#datasetFiles").selectpicker("refresh");
          	    
            },
            error: function(xhr){
				toastr.error(xhr.responseText,'');
    			return false;
            },
			complete:function(){					
				App.unblockUI("#createDatasetPortlet");
			}
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
			handleValidation();
			initTemplateElements();
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		// DELETE DATASET
		deleteDataset: function(datasetId){
			logControl ? console.log(LIB_TITLE + ': deleteDataset()') : '';	
			deleteDatasetConfirmation(datasetId);			
		},
		
		// uploadFile
		uploadFile: function(url){
			logControl ? console.log(LIB_TITLE + ': uploadFile()') : '';	
			uploadFile(); 
		},
	};
}();


//AUTO INIT CONTROLLERS WHEN READY
jQuery(document).ready(function() {
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	datasetCreateController.load(datasetCreateJson);
	datasetCreateController.init();	
});