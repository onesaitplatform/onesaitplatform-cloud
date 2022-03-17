var ResourceFromPlatformController = function() {
	
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Menu Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.
	var currentFormat = '' // date format depends on currentLanguage.
	var internalFormat = 'yyyy/mm/dd';
	var internalLanguage = 'en';
	var reader = new FileReader();
	
	reader.onload = function (e) {
        $('#showedImg').attr('src', e.target.result);
    }
	
	$('#resetBtn').on('click',function(){ 
		cleanFields('resource_create_form');
	});

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
		
		// CLEANING NUMBER INPUTS
		$(':input[type="number"]').val('');
		
		// CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');
		
		//CLEAN CODEMIRROR
		if (myCodeMirror.getValue() != ""){
			myCodeMirror.setValue('');
		}
		
		//CLEAN POSTPROCESS
		$('#postProcessTool').addClass('hide');
		
		//CLEAN DISPLAY PLATFORM RESOURCE AND NEW API PANELS
		$('#dashboard-div').addClass('hide');
		$('#viewer-div').addClass('hide');
		$('#api-div').addClass('hide');
		$('#prtitle').addClass('hide');
		$('#public-div').addClass('hide');
		$('#api-manager-div').addClass('hide');
		$('#alert-api').addClass('hide');
		$('#new-api-div').addClass('hide');
		$('#gravitee-div').addClass('hide');
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}
	
	var calculateVersion = function() {
		configurarApi();
        var identification = $('#identification').val();
        var apiType = $('#apiType').val();
    	var csrf_value = $("meta[name='_csrf']").attr("content");
    	var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
    	
        if ((identification!=null) && (identification.length>0) && (apiType!='')) {
            $.ajax({
                url: '/controlpanel/apimanager/numVersion',
                headers: {
					[csrf_header]: csrf_value
			    },
                type: 'POST',
                data: JSON.stringify({"identification":identification,"apiType":apiType}),
                dataType: 'text',
                contentType: 'text/plain',
                mimeType: 'text/plain',
                success: function(data) {
                    if(data != null && data != "") {
                        $('#numversion').val(data);
                        // VISUAL-UPDATE
                        configurarApi();
                    }
                },
                error: function(data,status,er) {
                    $('#dialog-error').val("ERROR");
                }
            });
        } else {
            configurarApi();
        }
    }

	var configurarApi = function () {
        apiType = $('#apiType').val();
        apiName = $('#identification').val();
        apiVersion = $('#numversion').val();
        apiEndPoint = $('#id_endpoint');
        apiSwagger = $('#id_endpoint_swagger');
        
        apiEndPoint.val(endpoint + "server/api/v" + apiVersion + "/" + apiName);
        apiSwagger.val(endpoint + "/services/management/api-docs?url=/services/management/swagger" + "/" + apiName + "/swagger.json");
        //myCodeMirror.refresh();
    }
	
    var updateApiLimit = function () {
        var checkCache= $('#checkboxLimit').prop('checked');
        if (checkCache) {
        	$('#id_limit').val("5");
        	$('#id_limit').prop('disabled', false);
        } else {
        	$('#id_limit').val("");
        	$('#id_limit').prop('disabled', true);
        }
    }
    
	var handleCodeMirrorJson = function () {
        var myTextArea = document.getElementById('jsonTextArea');
        myCodeMirror = CodeMirror.fromTextArea(myTextArea, {
        	mode: "application/ld+json",
        	autoCloseBrackets: true,
            matchBrackets: true,
            styleActiveLine: true,
            theme:"material",
            lineWrapping: true

        });
		myCodeMirror.setSize("100%", 300);
		myTextArea = document.getElementById('jsPostProcessExternal');
		myCodeMirrorJsExternal = CodeMirror.fromTextArea(myTextArea, {
	    	mode: "text/javascript",
	    	autoRefresh: {delay: 500},
	    	autoCloseBrackets: true,
	        matchBrackets: true,
	        styleActiveLine: true,
	        theme:"material",
	        lineWrapping: true
	
	    });
		myCodeMirrorJsExternal.setSize("100%",200);
		myCodeMirrorJsExternal.refresh();
    };
    
    var validateFields = function() {
		if(myCodeMirrorJsExternal.getValue() != ""){
			$('#postProcessFx').val(myCodeMirrorJsExternal.getValue());
		}
		
    	if (actionMode === 'create') {
			if(((($("#dashboard-select").val()!='' && $("#dashboard-select").val()!=undefined) || ($("#viewer-select").val()!='' && $("#viewer-select").val()!=undefined) || ($("#api-select").val()!='' && $("#api-select").val()!=undefined))
						|| ($('#check-new-api').prop('checked') == true && $("#identification").val()!='' && $("#identification").val()!=undefined && $("#numversion").val()!='' && $("#numversion").val()!=undefined
								&& $("#categories").val()!='' && $("#categories").val()!=undefined && $("#id_metainf").val()!='' && $("#id_metainf").val()!=undefined && $("#apiDescripcion").val()!='' && $("#apiDescripcion").val()!=undefined))) {
				$('#platformResourcePublic').val($('#check-public').prop('checked'));
				return true;
			}else{
				toastr.error(resourceCreateJson.validform.emptyfields,'');
				return false;
			}	
		} else {
			if ($('#div-resource').is(':visible')){
				if ((($("#dashboard-select").val()!='' && $("#dashboard-select").val()!=undefined) || ($("#viewer-select").val()!='' && $("#viewer-select").val()!=undefined) || ($("#api-select").val()!='' && $("#api-select").val()!=undefined))){
					$('#platformResourcePublic').val($('#check-public').prop('checked'));
					$('#dataset').val($('#resourceDataset').val())
					$('#platformResource').val($('#platformResource-select').val())
					$('#dashboardId').val($('#dashboard-select').val())
					$('#viewerId').val($('#viewer-select').val())
					$('#apiId').val($('#api-select').val())
					return true;
				} else {
					toastr.error(resourceCreateJson.validform.emptyfields,'');
					return false;
				}
			} else {
				$('#dataset').val($('#resourceDataset').val())
				return true;
			}
		}
    }
    
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#resource_create_form');

		// set current language
		currentLanguage = resourceCreateJson.language || LANGUAGE;

		form1.validate({
					errorElement : 'span',
					errorClass : 'help-block help-block-error',
					focusInvalid : false,
					ignore : ":hidden:not(.selectpicker)",
					lang : currentLanguage,
					rules : {
						name : {
							minlength : 5,
							required : true
						},
						resourceDataset: {required: true},
						'platformResource-select': {required: true}
					},
					invalidHandler : function(event, validator) {
						toastr.error(resourceCreateJson.validform.emptyfields,'');
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
						if (validateFields()) {
							form.submit();
						}
					}
				});
	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> resetForm') : '';		

		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		
		
		$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.getElementsByTagName('select')[0]){
				$('.form').validate().element('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id'));
			}
		});
	}
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';			
			handleCodeMirrorJson();
			handleValidation();
			initTemplateElements();
		},
		// CALCULATE VERSIONS
		calculateNumVersion: function() {
			logControl ? console.log(LIB_TITLE + ': calculateNumVersion()') : '';
			calculateVersion();
		},
			
		// CHANGE API LIMIT
		changeApiLimit: function() {
			logControl ? console.log(LIB_TITLE + ': changeApiLimit()') : '';
			updateApiLimit();
		}
		
	};
}();

