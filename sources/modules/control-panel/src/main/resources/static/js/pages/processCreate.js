
var ProcessCreateController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var APPNAME = 'onesait Platform Control Panel';
	var LIB_TITLE = 'Menu Controller';
	var logControl = 1;
	var LANGUAGE = [ 'es' ];
	var currentLanguage = ''; // loaded from template.
	var currentFormat = '' // date format depends on currentLanguage.
	var internalFormat = 'yyyy/mm/dd';
	var internalLanguage = 'en';
	var jsonOperations = [];
//	var mountableOperationsModel = $('#operations').find('tr.mountable-model')[0].outerHTML; // save html-model for when select new datamodel, is remove current and create a new one.
	var createColumns = []; // object to receive fields to create table
	var createKeyColumns = []; // object to receive key fields to create table
	var positionColumns = []; // object to receive fields to create table
	
	var csrf_value = $("meta[name='_csrf']").attr("content");
	var csrf_header = $("meta[name='_csrf_header']").attr("content");
	$.ajaxSetup({'headers': {
		[csrf_header]: csrf_value
    }});

	// CONTROLLER PRIVATE FUNCTIONS

	// REDIRECT URL
	var navigateUrl = function(url) {
		window.location.href = url;
	}
	
	$("#ordered").on("click", function(){
		if(createKeyColumns.length != 0){
			toastr.error(messagesForms.operations.genOpError,'You have to empty the operations table before you can change this option');
			return false;
		}
		if(!$("#ordered").is(":checked")){
			$("#position").attr("disabled","disabled");
			$("#position").val('');
		}else{
			$("#position").removeAttr("disabled");
			$("#position").val(1);
		}
	});
	
	$("#filtered").on("click", function(){
		if(createKeyColumns.length != 0){
			toastr.error(messagesForms.operations.genOpError,'You have to empty the operations table before you can change this option');
			return false;
		}
		if(!$("#filtered").is(":checked")){
			$("#fieldId").attr("disabled","disabled");
			$("#fieldValue").attr("disabled","disabled");
			$("#fieldValue").val('');
		}else{
			$("#fieldId").removeAttr("disabled");
			$("#fieldValue").removeAttr("disabled");
			$("#fieldValue").val('');
		}
		$('#fieldId').selectpicker('refresh')
	});
	
	function checkIsOrdered(){
		if(!$("#ordered").is(":checked")){
			$("#position").attr("disabled","disabled");
			$("#position").val('');
		}else{
			$("#position").removeAttr("disabled");
			$("#position").val(1);
		}
	}
	
	$("#checkExecutions").on("click", function(){
		if(createKeyColumns.length != 0){
			toastr.error(messagesForms.operations.genOpError,'You have to empty the operations table before you can change this option');
			return false;
		}
		if(!$("#checkExecutions").is(":checked")){
			$("#executionNum").attr("disabled","disabled");
			$("#executionNum").val('');
		}else{
			$("#executionNum").removeAttr("disabled");
			$("#executionNum").val(1);
		}
	});
	
	function checkIsExecutions(){
		if(!$("#checkExecutions").is(":checked")){
			$("#executionNum").attr("disabled","disabled");
			$("#executionNum").val('');
		}else{
			$("#executionNum").removeAttr("disabled");
			$("#executionNum").val(1);
		}
	}
	
	function checkIsFiltered(){
		if(!$("#filtered").is(":checked")){
			$("#fieldId").attr("disabled","disabled");
			$("#fieldValue").attr("disabled","disabled");
			$("#fieldValue").val('');
		}else{
			$("#fieldId").removeAttr("disabled");
			$("#fieldValue").removeAttr("disabled");
			$("#fieldValue").val('');
		}
		$('#fieldId').selectpicker('refresh')
	}
	
// CLEAN FIELDS FORM
var cleanFields = function(formId) {
	logControl ? console.log('cleanFields() -> ') : '';

	// CLEAR OUT THE VALIDATION ERRORS
	$('#' + formId).validate().resetForm();
	$('#' + formId).find(
			'input:text, input:password, input:file, select, textarea')
			.each(function() {
				// CLEAN ALL EXCEPTS cssClass "no-remote" persistent fields
				if (!$(this).hasClass("no-remove")) {
					$(this).val('');
				}
			});

	// CLEANING NUMBER INPUTS
	$(':input[type="number"]').val('');
	
	// CLEAN ALERT MSG
	$('.alert-danger').hide();

}

	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#process_create_form');
		var error1 = $('.alert-danger');
		var success1 = $('.alert-success');

		// set current language
		currentLanguage = processCreateReg.language || LANGUAGE;

		form1.validate({
					errorElement : 'span', // default input error message
											// container
					errorClass : 'help-block help-block-error', // default input
																// error message
																// class
					focusInvalid : false, // do not focus the last invalid
											// input
					ignore : ":hidden:not(.selectpicker)", // validate all
															// fields including
															// form hidden input
															// but not
															// selectpicker
					lang : currentLanguage,
					// custom messages
					messages : {

					},
					// validation rules
					rules : {
						identification : {
							minlength : 5,
							required : true
						},
						description : {
							minlength : 5,
							required : true
						},
						periodicity : {
							required : true
						}
					},
					invalidHandler : function(event, validator) { // display
																	// error
																	// alert on
																	// form
																	// submit

						toastr.error(messagesForms.validation.genFormError,'');
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
						logControl ? console.log('submitform() -> ') : '';
						
						var operationInfo = document.getElementsByClassName('operationRow');
						var types=[];
						var entities=[];
						var sources=[];
						var numExecutions=[];
						var positions=[];
						var ids =[];
						var tds=[];
						var fieldIds=[];
						var fieldValues=[];
						if(operationInfo.length==0){
							toastr.error(messagesForms.operations.genOpError,'It is neccesary to add at least one operation to the process');
							return false;
						}
						for(i=0;operationInfo.length>i;i++) {
							tds=[];
							for(j=0;operationInfo[i].childNodes.length>j;j++) {
								if(operationInfo[i].childNodes[j].localName == "td") {
									tds.push(operationInfo[i].childNodes[j].innerText);
								}		
							}
							types.push(tds[0]);
							entities.push(tds[1]);
							fieldIds.push(tds[2]==''?'':tds[2]);
							fieldValues.push(tds[3]==''?'':tds[3]);
							sources.push(tds[4]);
							numExecutions.push(tds[5]==''?'':tds[5]);
							positions.push(tds[6]===''?'':tds[6]);
							ids.push(operationInfo[i].getAttribute("name")==undefined ? null:operationInfo[i].getAttribute("name"));
						}
						
						if ( types.length ){	
							$.each(types, function( index, value ) {
								var position = positions.length == 0 ? 0 : positions[index];
								var numExec = numExecutions.length == 0 ? 0 : numExecutions[index]
								var id = ids.length == 0 ? null : ids[index];
								var fieldId = fieldIds.length == 0 ? null : fieldIds[index];
								var fieldValue = fieldValues.length == 0 ? null : fieldValues[index];
								logControl ? console.log('index: ' + index + ' | type: ' + types[index] + ' entity: ' + entities[index] + ' fieldIds: ' + fieldId +' fieldValues: ' + fieldValue + ' sources: ' + sources[index] + ' execution: ' + numExec+ ' position: ' + position + ' id: ' + id) : '';
								var operation = '{"type":"'+types[index]+'","ontology":"'+entities[index]+'","sources":"'+sources[index]+'","fieldId":"'+fieldId+'","fieldValue":"'+fieldValue+'","numExecutions":"'+numExec+'","position":"'+position+'","id":"'+id+'"}';
								
								$("<input type='hidden' value='"+operation+"' />")
						         .attr("name", "operations")
						         .appendTo("#process_create_form");		
							});			
						}
						
						
						form.submit();
					}
				});
	}

	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function() {
		logControl ? console
				.log('initTemplateElements() -> selectpickers, datepickers, resetForm, today->dateCreated currentLanguage: '
						+ currentLanguage)
				: '';

		// set current language and formats
		currentLanguage = processCreateReg.language || LANGUAGE[0];
		currentFormat = (currentLanguage == 'es') ? 'dd/mm/yyyy' : 'mm/dd/yyyy';

		logControl ? console.log('|---> datepickers currentLanguage: '
				+ currentLanguage) : '';

		// INPUT MASK FOR Realm identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});	
		
		// Reset form
		$('#resetBtn').on('click', function() {
			cleanFields('process_create_form');
		});
		
		// INSERT MODE ACTIONS (deviceCreateReg.actionMode = NULL )
		if (processCreateReg.actionMode === null) {
			logControl ? console.log('action-mode: INSERT') : '';
			$('#active').trigger('click');
			$("#addOperation").click();
		}

		// EDIT MODE ACTION
		else {
			console.log(processCreateReg.periodicity);
			var jsonOperations = [];
			$.each( processCreateReg.operations, function (key, object){			
				
				var column = {
						'type': object.type,
						'entity': object.ontology,
						"fieldId" : object.fieldId,
						"fieldValue" : object.fieldValue,
						'sources': object.sources,
						"execution": object.numExecutions,
						"position": object.position,
						"id": object.id
					};
				
				createColumns.push(column);
				createKeyColumns.push(column['type'] + '_' + column['entity']);
				if($("#ordered").is(":checked"))
					positionColumns.push(column['position']);
				
				var position = $("#ordered").is(":checked") ? column['position'] : 0;
				var numExecutions = $("#checkExecutions").is(":checked") ? column['execution'] : 0;
				var fieldIds = $("#filtered").is(":checked") ? column['fieldId'] : '';
				var fieldValue = $("#filtered").is(":checked") ? column['fieldValue'] : '';
				$('#operations > tbody').append(
						'<tr id="operation_'+column['type']+'_'+column['entity']+'" name="'+ column['id'] +'" class="operationRow">'
						+'<td>'+ column['type'] + '</td>'
						+'<td>'+ column['entity'] + '</td>'
						+'<td>'+ fieldIds + '</td>'
						+'<td>'+ fieldValue + '</td>'
						+'<td>'+ column['sources'] + '</td>'
						+'<td>'+ numExecutions + '</td>'
						+'<td>'+ position + '</td>'
						+ '<td class="text-center"><button type="button" data-property="" class="btn btn-xs btn-no-border icon-on-table color-red tooltips" onclick="ProcessCreateController.removeOperationRow(this)"><i class="icon-delete"></i></button></td></tr>'
						);

				console.log('columns -->' + createKeyColumns);
				
			});
		}

	}

	// CONTROLLER PUBLIC FUNCTIONS
	return {
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load : function(Data) {
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return processCreateReg = Data;
		},

		// INIT() CONTROLLER INIT CALLS
		init : function() {
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleValidation();
			initTemplateElements();
			checkIsOrdered();
			checkIsExecutions();
			checkIsFiltered();
			
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
		},
		// REDIRECT
		go : function(url) {
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			navigateUrl(url);
		},
		getSelectedOperationColumn: function() {
			var typeSelected = $("#type option:selected").val();
			var entitySelected = $("#entity option:selected").text();
			var sourceSelected = $("#source").val();
			var executionSelected = $("#executionNum").val();
			var positionSelected = $("#ordered").is(":checked") ? $("#position").val() : 0;
			var fieldId = $("#filtered").is(":checked") ? $("#fieldId").val() :'';
			var fieldValue = $("#filtered").is(":checked") ? $("#fieldValue").val() : '';
			
			if ((typeSelected === "" || entitySelected=== "" || sourceSelected===null || sourceSelected==="") 
					|| ($("#ordered").is(":checked") && positionSelected==="")
					|| ($("#checkExecutions").is(":checked") && executionSelected==="")
					|| ($("#filtered").is(":checked") && (fieldId === "" || fieldValue === ""))) {
				toastr.error(messagesForms.operations.genOpError,'Fields must be filled correctly');
				return false;
			}
			
			var column = {
				'type': typeSelected,
				'entity': entitySelected,
				'sources': sourceSelected,
				"execution": executionSelected,
				"position": positionSelected,
				"fieldId" : fieldId,
				"fieldValue" : fieldValue
			};

			return column;
		},
		addOperationRow: function() {
			
			var column = this.getSelectedOperationColumn();
			if (column === false) {
				return;
			}
			
			createColumns.push(column);
			createKeyColumns.push(column['type'] + '_' + column['entity']);
			var checkUnique = createKeyColumns.unique();
			if (createKeyColumns.length !== checkUnique.length)  {
				createKeyColumns.pop(); 
				createColumns.pop(); 
				toastr.error(messagesForms.operations.genOpError,'The combination of type and entity must be unique');
				return false; 
			}
			if($("#ordered").is(":checked")){
				positionColumns.push(column['position']);
				var checkUnique = positionColumns.unique();
				if ($("#ordered").is(":checked") && (positionColumns.length !== checkUnique.length))  {
					positionColumns.pop(); 
					createKeyColumns.pop(); 
					createColumns.pop(); 
					toastr.error(messagesForms.operations.genOpError,'The position must be unique');
					return false; 
				}
			}
			
			var position = $("#ordered").is(":checked") ? column['position'] : 0;
			var numExecutions = $("#checkExecutions").is(":checked") ? column['execution'] : 0
			var fieldIds = $("#filtered").is(":checked") ? column['fieldId'] : ''
			var fieldValue = $("#filtered").is(":checked") ? column['fieldValue'] : ''
			$('#operations > tbody').append(
					'<tr id="operation_'+column['type']+'_'+column['entity']+'" class="operationRow">'
					+'<td>'+ column['type'] + '</td>'
					+'<td>'+ column['entity'] + '</td>'
					+'<td>'+ fieldIds + '</td>'
					+'<td>'+ fieldValue + '</td>'
					+'<td>'+ column['sources'] + '</td>'
					+'<td>'+ numExecutions + '</td>'
					+'<td>'+ position + '</td>'
					+ '<td class="text-center"><button type="button" data-property="" class="btn btn-xs btn-no-border icon-on-table color-red tooltips" onclick="ProcessCreateController.removeOperationRow(this)"><i class="icon-delete"></i></button></td></tr>'
					);

			
			this.clearOperationSelected(column['position']);
			console.log('columns -->' + createKeyColumns);
			toastr.success(messagesForms.operations.genOpSuccess,'');
		},
		removeOperationRow: function(operation) {
			var operationSelected = operation.parentElement.parentElement.id;
			if (this.removeOperationRowFromOperationType(operationSelected)){
				operation.parentElement.parentElement.remove(); 
				toastr.success(messagesForms.operations.genOpSuccess,'');
			}
		},
		removeOperationRowFromOperationType: function(operationSelected) {
			var errorRemoving = false
			var p = $("#operation_insert_Ticket").find("td:eq(4)").text();
			$("#"+operationSelected).remove();
			
			var i = createKeyColumns.indexOf(operationSelected.replace('operation_', ''));
			createKeyColumns.splice(i, 1);
			createColumns.splice(i, 1);
			console.log('columns --> ' + createKeyColumns);
			var x = positionColumns.indexOf(p);
			positionColumns.splice(x, 1);
			
			return true;
		},
		clearOperationSelected: function(position) {
			$('#type').selectpicker('deselectAll').selectpicker('refresh');
			$('#entity').selectpicker('deselectAll').selectpicker('refresh');
			$('#fieldId').selectpicker('deselectAll').selectpicker('refresh');
			$('#source').selectpicker('deselectAll').selectpicker('refresh');
			$('#executionNum').val($("#checkExecutions").is(":checked") ? 1 : '');
			$('#position').val($("#ordered").is(":checked") ? parseInt(position) + 1 : '');
			$('#fieldValue').val('');
		},
		changeOntology: function(){
			$.ajax({
				url: '/controlpanel/process/getOntologyFields',
				headers: {
					[csrf_header]: csrf_value
			    },
				type:"POST",
				async: true,
				data: { 'ontologyIdentification': $("#entity option:selected").text()},
				dataType:"json",
				success: function(response,status){
					$("#fieldId").empty();
					var fields = response;
					$("#fieldId").append('<option id="'+field+'" name="'+type+'" value="select">'+processCreateJson.selectField+'</option>');
					for (var key in fields){
				        var field = key;
				        var type = fields[key];
				        $("#fieldId").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
				    }
					$('#fieldId').selectpicker('refresh')
				}
			});
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	ProcessCreateController.load(processCreateJson);

	// AUTO INIT CONTROLLER.
	ProcessCreateController.init();
});
