var MicroserviceTemplateController = function(){

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var LIB_TITLE = 'Microservice Template Controller';
	var logControl = 1;
		
	// Reset form
	$('#resetBtn').on('click',function(){ 
		cleanFields('form-mstemplate');
	});
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
	
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file,input:text, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remote" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( 'default' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
			$(this).change();
		});
		
		// CLEANING CHECKS
		$('#checkboxPublish').prop('checked', true);
		$('#checkboxPublish').change();
		$('input:checkbox').not('.no-remove').removeAttr('checked');
	}
	
	var handleValidation = function() {
        var form1 = $('#form-mstemplate');
		// set current language
		// TODO: Analizar -> currentLanguage = dashboardCreateReg.language || LANGUAGE;
        // set current language
		currentLanguage = mstemplateCreateJson.language || LANGUAGE;

        form1.validate({
            errorElement: 'span', 
            errorClass: 'help-block help-block-error',
            focusInvalid: false, 
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", 
			lang: currentLanguage,
            rules: {				
            	identification : {
					minlength : 5,
					required : true
				},
				description : {
					minlength : 5,
					required : true
				}
            },
            invalidHandler: function(event, validator) {
            	toastr.error(messagesForms.validation.genFormError,'');
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
            submitHandler: function(form) { 
            	toastr.success(messagesForms.validation.genFormSuccess,'');
            	form.submit();
            }
        });
    }	
    
    // REDIRECT URL
	var navigateUrl = function(url) {
		window.location.href = url;
	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> resetForm') : '';		

		$("#btn-cancel").on('click', function (e) {
			e.preventDefault();
			window.location = '/controlpanel/mstemplates/list';
		});
		
		// INPUT MASK FOR mstemplate identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
			
		$("#language-select").on('change', function(){
			if(this.value == "Java17"){
				$("#graalvmCheck").removeClass("hide");
				$('#graalvm').removeAttr('checked');
				$('#dockerRelativePath').removeClass('hide');
			}else{
				$("#graalvmCheck").addClass("hide");
				$('#graalvm').removeAttr('checked');
				$('#dockerRelativePath').removeClass('hide');
			}
		});
		
		$('#graalvm').on('click', function(){
			if($('#graalvm').is(':checked')){
				$('#dockerRelativePath').addClass('hide');
			} else {
				$('#dockerRelativePath').removeClass('hide');
			}
		});
		
		if($('#graalvm').is(':checked')){
			$('#dockerRelativePath').addClass('hide');
		} else {
			$('#dockerRelativePath').removeClass('hide');
		}
			
		if($("#language-select").children("option:selected").val() !== "Java17" && typeof($("#language-select").children("option:selected")).val() !== 'undefined' ){
			$("#graalvmCheck").addClass("hide");
		} else if(typeof($("#languageOption").children("option:selected")).val() !== 'undefined' && $("#languageOption")[0].outerText !== "Java17"){
			$("#graalvmCheck").addClass("hide");
		} else if(typeof($("#languageOption")).val() !== 'undefined'  && languageOption.innerHTML !== 'Java17'){
			$("#graalvmCheck").addClass("hide");
		}
		
		if($("#language-select").children("option:selected").val() == "NOTEBOOK_ARCHETYPE" ||
		$("#language-select").children("option:selected").val() == "IOT_CLIENT_ARCHETYPE" ||
		$("#language-select").children("option:selected").val() == "ML_MODEL_ARCHETYPE"){
			$('#language-select').prop('disabled', 'disabled');
		} else {
			$('#language-select').children('option[value="NOTEBOOK_ARCHETYPE"]').remove();
			$('#language-select').children('option[value="ML_MODEL_ARCHETYPE"]').remove();
			$('#language-select').children('option[value="IOT_CLIENT_ARCHETYPE"]').remove();
			$('#language-select').selectpicker('refresh');
		}
	}

	
	// DELETE ONTOLOGY
	var deleteMsTemplateConfirmation = function(mstemplateId){
		console.log('deleteMsTemplateConfirmation() -> formId: '+ mstemplateId);

		// no Id no fun!
		if ( !mstemplateId ) {$.alert({title: 'ERROR!',  theme: 'light', content: mstemplateCreateJson.validations.validform}); return false; }

		logControl ? console.log('deleteMsTemplateConfirmation() -> formAction: ' + $('.delete-mstemplate').attr('action') + ' ID: ' + $('#delete-mstemplateId').attr('mstemplateId')) : '';

		// call ontology Confirm at header.
		$('#delete_mstemplate_form').attr('action', deleteUrl + '/' + mstemplateId);
		$('#delete-id').val(mstemplateId);
		HeaderController.showConfirmDialog('delete_mstemplate_form');		
	}
	
	// CONTROLLER PUBLIC FUNCTIONS
	return {
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
		},
		// INIT() CONTROLLER INIT CALLS
		init : function() {
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleValidation();
			initTemplateElements();
		},

		// REDIRECT
		go : function(url) {
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			navigateUrl(url);
		},
		// DELETE MS TEMPLATE
		deleteMsTemplate: function(mstemplateId){
			logControl ? console.log(LIB_TITLE + ': deleteOntology()') : '';
			deleteMsTemplateConfirmation(mstemplateId);
		}
	};
	
}();

$(window).on("load",function(){
	// AUTO INIT CONTROLLER.
	MicroserviceTemplateController.init();
});
