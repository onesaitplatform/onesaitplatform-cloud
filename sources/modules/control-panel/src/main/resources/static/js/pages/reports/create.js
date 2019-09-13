//== Set Vars of all the site
var Report = Report || {};

Report.Create = (function() {
	"use-strict";
	

	var $tableParams = $("#table-report-parameters");
	
	var init = function() {

		// -- Events -- //
		$("#btn-report-cancel").on('click', function (e) {
			e.preventDefault();
			window.location = '/controlpanel/reports/list';
		});
		
		/*$("#btn-report-reset").on('click', function (e) {
			e.preventDefault();
		});*/
		
		/*$("#btn-report-save").on('click', function (e) {
			e.preventDefault();
			submitForm($('#report-save-action').val(), $('#report-save-method').val());
		});*/
		
//		maxsize = 60000000; // TODO
//		setEventListeners(maxsize);
//		
		$("#btn-report-upload").on('click', function (e) {
			$("#btn-report-upload-file").click();
		});
		
		
		handleValidation();
	};	

//	var setEventListeners = function (maxsize) {
//		$("#btn-report-upload-file").bind('change', function() {
//			 if(this.files[0].size> maxsize){
//				 $("#modal-error").modal('show');
//				 return false;
//			 } else {
//				 $("#modal-error").modal('hide');
//				 uploadFile($("#report-settings"));
//			 }
//		 });
//	}
	
//	var uploadFile = function ($tabs) {
//		 
//		var formData = new FormData();
//        formData.append('file', $('input[type=file]')[0].files[0]);
//        //console.log("formData " + formData);
//        
//        $.ajax({
//       	 	url : '/controlpanel/reports/info',
//       	 	enctype: 'multipart/form-data',
//       	 	data : formData,
//            processData : false,
//            contentType : false,
//            type : 'POST'
//        }).done(function(data) {
//        	//console.log(JSON.stringify(data));
//        	var parameters = data.parameters;
//        	
//        	//createTableBody(parameters);
//        	
//        	$("#report-datasource").val(data.dataSource);
//        	
//        	// Show params tab if exists
//        	if (parameters.length > 0 || data.datasource) {
//        		$tabs.css({ "visibility" : "visible" });
//        	}
//        	
//        }).fail(function(error) {
//        	alert('Zorro plateado comunica: Ha ocurrido un error ' + error);
//        	$tabs.css({ "visibility" : "hidden" });
//        });
//	}	
//	
//	var createTableBody = function(parameters) {
//		var $tableBodyParams = $tableParams.find('tbody');
//		if ($tableBodyParams == undefined) {
//			$tableParams.append('<body />');
//		} else {
//			$tableBodyParams.html('');
//		}
//		$tableBodyParams = $tableParams.find('tbody');
//		
//		for (let i = 0; i < parameters.length; i++) { 
//    		//console.log(JSON.stringify(parameters[i]));
//    		let row = createRow(parameters[i], i);
//    		$tableBodyParams.append(row);
//    	}
//	}
	
//	var createRow = function (parameter, position) {
//		let row = $('<tr>');
//		row.append($('<td class="text-left" >').html(parameter.name))
//			.append($('<td class="text-left" >')
//					.append(createColumn (parameter, position)) //.append('</td>')					
//					.append(createHiddenType(parameter, position))
//					.append(createHiddenName(parameter, position))
//					.append(createHiddenDescription(parameter, position)))
//					//.append(createHiddenValue(parameter, position))
//			//.append('</td>'))
//			.append($('<td class="text-left" style="word-break:break-all; ">').html(parameter.description));
//		
//		return row;
//	}
//	
//	var createHiddenName = function (parameter, position) {
//		let _id = "parameters" + position + ".name";
//		let _name = "parameters[" + position + "]" + ".name";
//		let _type = "hidden";
//		
//		var $column = $("<input/>")
//			.attr("type", _type)
//			.attr('id', _id)
//			.attr('name', _name)
//			.attr('value', parameter.name);
//		return $column;
//	}
//	
//	var createHiddenDescription = function (parameter, position) {
//		let _id = "parameters" + position + ".description";
//		let _name = "parameters[" + position + "]" + ".description";
//		let _type = "hidden";
//		
//		var $column = $("<input/>")
//			.attr("type", _type)
//			.attr('id', _id)
//			.attr('name', _name)
//			.attr('value', parameter.description);
//		return $column;
//	}
//	
//	var createHiddenType = function (parameter, position) {
//		let _id = "parameters" + position + ".type";
//		let _name = "parameters[" + position + "]" + ".type";
//		let _type = "hidden";
//		
//		var $column = $("<input/>")
//			.attr("type", _type)
//			.attr('id', _id)
//			.attr('name', _name)
//			.attr('value', parameter.type);
//		
//		return $column;
//	}
//	
//	var createHiddenId = function (parameter, position) {
//		let _id = "parameters" + position + ".id";
//		let _name = "parameters[" + position + "]" + ".id";
//		let _type = "hidden";
//		
//		var $column = $("<input/>")
//			.attr("type", _type)
//			.attr('id', _id)
//			.attr('name', _name)
//			.attr('value', parameter.id);
//		
//		return $column;
//	}
//	
//	var createColumn = function (parameter, position) {
//		let _id = "parameters" + position + ".value";
//		let _name = "parameters[" + position + "]" + ".value";
//		let _type = "text";
//		
//		var $column = $("<input/>")
//			.attr("type", _type)
//			.attr('id', _id)
//			.attr('name', _name);
//		
//		return $column; //'<input type="text" value="" size="32" />';
//	}
//	
	
	
	function submitForm($form, action, method) {
		$form.attr('action', action);
		$form.attr('method', 'post');
		$form[0].submit();
	}
	
	/*
	 * For more info visit the official plugin documentation: http://docs.jquery.com/Plugins/Validation
	 */
	var handleValidation = function() {
        var $form = $('#form-report');
        var $error = $('.alert-danger');
        var $success = $('.alert-success');
		// set current language
		// TODO: Analizar -> currentLanguage = dashboardCreateReg.language || LANGUAGE;
        
        $form.validate({

            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
			// validation rules
            rules: {				
				description: { required: true},
				name: { required: true}
				
            },
            invalidHandler: function(event, validator) { //display error alert on form submit  
                $success.hide();
                $error.show();
                App.scrollTo($error, -200);
            },
            errorPlacement: function(error, element) {				
                if (element.is(':checkbox')) { 
					error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); 
				} else if (element.is(':radio')) { 
					error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); 
							
				}else if(element.is(':file')){
					$.alert({
						title : 'ERROR!',
						type : 'red',
						theme : 'light',
						content : validateTemplate
					});
				} else if (element.is(':hidden'))	{ 
					if ($('#datamodelid').val() === '') { 
						$('#datamodelError').removeClass('hide');
					} 		
				
				}else { 
					error.insertAfter(element); 
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
            submitHandler: function(form) { 
            	if($('#checkboxDataSource').is(':checked') && $('#report-datasource').val()!=null)
            		$('#data-source-url').val($('#report-datasource').val());
            	else if(!$('#checkboxDataSource').is(':checked'))
            		$('#data-source-url').val(null);
				$success.show();
				$error.hide();					
				submitForm($form, $('#report-save-action').val(), $('#report-save-method').val());
			}
        });
    }
	
	// Public API
	return {
		init: init
	};
})();

$(document).ready(function() {	
	
	Report.Create.init();
});
