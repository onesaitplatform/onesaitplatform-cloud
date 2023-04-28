//== Set Vars of all the site
var Report = Report || {};

Report.Create = (function() {
	"use-strict";
	

	var $tableParams = $("#table-report-parameters");
	var filesPath = '/controlpanel/files/';
	var fetchResourcesURL = '/controlpanel/reports/resources';
	var reportsPath = '/controlpanel/reports/';
	var addResourceURL = '/controlpanel/reports/report/resources';
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	headersObj[csrfHeader] = csrfToken;
	var csrf_value = $("meta[name='_csrf']").attr("content");
	var csrf_header = $("meta[name='_csrf_header']").attr("content");
	var mountableModel = $('#resources').find('tr.resources-model')[0].outerHTML;
   
	var form1 = $('#form-report');
   
	
	var init = function(data) {

		ontologyCreateReg = data;
		
		// INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
		// -- Events -- //
		
		$('#btn-report-add-resources').off().on('click',function(){
			
			fetchExistingResources();
		})
		
		$('.btn-download-resource').each(function() {
			$(this).on('click', function (e) {
				var id = $(this).data('id');
				window.location = filesPath + id;
			});
		});
		
		$('.btn-update-resource').each(function() {
			$(this).on('click', function (e) {
				var binaryId = $(this).data('id');
				var reportId = $(this).data('report');
				var url =  reportsPath + 'report/' +reportId + '/resource/' + binaryId;
				$('#form_update_resource').attr("action", url);
				$('#updateFile').modal('show');
				$("#form_update_resource").validate().cancelSubmit = true;
			});
		});
		
		$('.btn-delete-resource').each(function() {
			$(this).on('click', function (e) {
				var binaryId = $(this).data('id');
				var reportId = $(this).data('report');
				$.ajax({
		       	 	url : reportsPath + 'report/' +reportId + '/resource/' + binaryId ,  
		       	 	headers: headersObj,
		            type : 'DELETE'
		        }).done(function(data) {
		        	location.reload();
		        }).fail(function(error) {
					toastr.error( error.responseText,'');
				});
				
			});
		});
		
		$("#btn-report-cancel").on('click', function (e) {
			e.preventDefault();
			window.location = '/controlpanel/reports/list';
		});
		
	
		$("#btn-report-upload").on('click', function (e) {
			$("#btn-report-upload-file").click();
		});
		
		$("#btn-report-upload-resources").on('click', function (e) {
			$("#btn-report-upload-file-resources").click();
		});
		
		$("#submitUpdate").on('click', function (e) {
			e.preventDefault();
			submitForm($('#form_update_resource'),$('#form_update_resource').attr('action'), 'post');
		});
		
		$('#form-report').on('submit', function(e){
			e.preventDefault();
			submitForm($('#form-report'),$('#report-save-action').val(), $('#report-save-method').val());
		})

		initTemplateElements();
		handleValidation();
	};	

	function addResource(obj){
		var resourceId = $(obj).closest('tr').find("input[name='ids\\[\\]']").val();
		$.ajax({
			url:addResourceURL, 
			type:"PUT", 
			async: true, 
			headers:headersObj,
			data: {"resourceId":resourceId ,"reportId":reportId},
			success: function(response,status){		
				
				$("#reportResources").load('/controlpanel/reports/edit/' +reportId+'/resources/fragment',headersObj)	
			}
		});	
	}

	function fetchExistingResources(){
		$.ajax({
			url:fetchResourcesURL + "?currentReportId="+reportId,
			headers:headersObj,
			type:"GET",
			async: true,		 
			dataType:"json",
			success: function(response,status){							
				var resources = [];
				response.forEach( function(r){
					resources.push({"ids":r.id,"users":r.userId, "resources":r.fileName});
				})
									
				// TO-HTML
				if ($('#resources-div').attr('data-loaded') === 'true'){
					$('#resources > tbody').html("");
					$('#resources > tbody').append(mountableModel);
				}
			
				$('#resources').mounTable(resources,{
					model: '.resources-model',
					noDebug: false							
				});
				$('#resources-div').removeClass('hide');
				$('#resources-div').attr('data-loaded',true);
				
				$('#modal-add-resources').modal('show');
				
			}
		});
	}
	
	function submitForm($form, action, method) {
		var noerrors = true;
		$('input,textarea,select:visible').filter('[required]').each(function(i, obj) {
			noerrors = noerrors && $('.form').validate().element(obj);
		});
		
		if ($("[name='file']").val()=="" && reportsCreateJson.actionMode == null){
			noerrors = false;
			toastr.error(reportsCreateJson.messages.filerequired);
		}
		
		if ($('#checkboxDataSource')[0] && $('#checkboxDataSource')[0].checked){
			$('#data-source-url').val($('#report-datasource').val());
		} else {
			$('#data-source-url').prop( "disabled", true );
		}
		
		if (noerrors) {
			$form.attr('action', action + "?" + csrfParameter + "=" + csrfValue);
			$form.attr('method', 'post');
			toastr.success(messagesForms.validation.genFormSuccess,'');
			$form[0].submit();
		}
	}
	
	function fileName() {
		$('#fileName').removeClass('description');
		$('#fileName').text($("#btn-report-upload-file").prop('files')[0].name);
	}
	
	function resourceName() {
		$('#resourceName').removeClass('description');
		var files = $("#btn-report-upload-file-resources").prop('files');
		var names = '';
		for(var i=0; i<files.length; i++) {
			if(i==(files.length -1)){
                names += files[i].name;
			}else {
			    names += (files[i].name + ", ");
			}
		}
		$('#resourceName').text(names);
	}
	
	var handleValidation = function() {
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
            	identification:	{required: true, minlength: 5},
            	description:	{required: true, minlength: 5},
            	fileName:	{required: true}
            },
            messages: {
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
		
		$('#fileName').html('');
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		// CLEANING CHECKS
		$('input:checkbox').removeAttr('checked');
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		$('.tagsinput').prev().removeClass('tagsinput-has-error');
		$('.tagsinput').nextAll('span:first').addClass('hide');
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}
	
	//INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(Data){	
		
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('form-report');
		});	
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		
		
		$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.getElementsByTagName('select')[0]){
				$('.form').validate().element('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id'));
			}
		})
			
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

	// Public API
	return {
		init: init,
		addResource:addResource,
		fileName:fileName,
		resourceName:resourceName
	};
})();

$(document).ready(function() {	
	
	Report.Create.init(reportsCreateJson);
});
