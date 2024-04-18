var KafkaClusterConnectionCreateController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var APPNAME = 'onesait Platform Control Panel';
	var LIB_TITLE = 'Menu Controller';
	var logControl = 1;
	var LANGUAGE = [ 'es' ];
	var currentLanguage = ''; // loaded from template.
	var currentFormat = '' // date format depends on currentLanguage.
	var internalFormat = 'yyyy/mm/dd';
	var internalLanguage = 'en';
	
	// CONTROLLER PRIVATE FUNCTIONS

	// REDIRECT URL
	var navigateUrl = function(url) {
		window.location.href = url;
	}
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#kafka_cluster_connection_create_form');
		var error1 = $('.alert-danger');

		// set current language
		currentLanguage = kafkaClusterConnectionReg.language || LANGUAGE;
		
		form1
				.validate({
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
															// but not selectpicker
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

					},
					invalidHandler : function(event, validator) { // display error
																// alert on form submit
						toastr.error(kafkaClusterConnectionReg.messages.validationKO);
						/*if (!valOntologies()) {
							toastr.error(kafkaClusterConnectionReg.ontologyNotSelected);
						}*/
					},
					errorPlacement : function(error, element) {
						if (element.is(':checkbox')) {
							error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline"));
						} else if (element.is(':radio')) {
							error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline"));
						} else {
							error.insertAfter(element);
						}
					},
					highlight : function(element) { // hightlight error inputs
						$(element).closest('.form-group').addClass('has-error');
					},
					unhighlight : function(element) { // revert the change
														// done by hightlight
						$(element).closest('.form-group').removeClass('has-error');
					},
					success : function(label) {
						label.closest('.form-group').removeClass('has-error');
					},
					// ALL OK, THEN SUBMIT.
					submitHandler : function(form) {
						validateConnectionProperties();
						toastr.success(kafkaClusterConnectionReg.messages.validationOK);
						form.submit();
						/*if (valOntologies()) {
							toastr.success(kafkaClusterConnectionReg.messages.validationOK);
							form.submit();
						} else {
							toastr.error(kafkaClusterConnectionReg.ontologyNotSelected);
						}*/
					}
				});
	}
	
	var updateConnectionProperties = function(properties) {
		var selectedProperties = JSON.parse(properties);
		if (selectedProperties !== null && selectedProperties.length > 0) {
			for (var i = 0; i < selectedProperties.length; i++) {
				var property = selectedProperties[i];
				var arr = Object.keys(property)
				var propertyName=arr[0];
				var propertyValue=property[propertyName];
				$('#datamodel_properties > tbody')
						.append(
								"<tr data-property='"
										+ propertyName
										+ "' data-value='"
										+ propertyValue
										+ "'><td>"
										+ propertyName
										+ "</td><td>"
										+ propertyValue
										+ '</td><td class="icon" style="white-space: nowrap"><span class="btn btn-xs btn-no-border icon-on-table  tooltips color-red" data-container="body" data-placement="bottom" th:title="#{gen.deleteBtn}" th:data-original-title="#{gen.deleteBtn}" onclick="KafkaClusterConnectionCreateController.removePropertyRow(this)" th:text="#{device.ontology.remove}"><i class="icon-delete"></i></span></td></tr>');
			}
			$("#connectionProperty option").prop("selected", false);
			$('#connectionProperty').selectpicker('destroy');
			tooltipLoader();
			$("#propertyValue").val("");
			showHideImageTableProperties();
		}

	}
	
	var tooltipLoader = function() {
		$('#connectionProperty').selectpicker({
		    liveSearch: true
		  }).on('loaded.bs.select', function(e){
		    // save the element
		    var $el = $(this);
		    // the list items with the options
		    var $lis = $el.data('selectpicker').$lis;
		    $lis.each(function(i) {
		      // get the title from the option
		      var tooltip_title = $el.find('option').eq(i).attr('mytooltip');
		           $(this).tooltip({
		         'title': tooltip_title,
		         'placement': 'top'
		      });
		   });
		  });
	}
	
	var addPropertyRow = function() {
		var propertySelected = $("#connectionProperty option:selected").text();
		var valueSelected = $("#propertyValue").val();

		if (propertySelected === "") {
			toastr.error(messagesForms.operations.genOpError,kafkaClusterConnectionReg.propertyNotSelected);
			return false;
		}
		var match = $('#datamodel_properties > tbody > tr[data-property="'+propertySelected+'"]');
		var matching = match.length==1;
		if(!matching){
		$('#datamodel_properties > tbody')
				.append(
						"<tr data-property='"
								+ propertySelected
								+ "' data-value='"
								+ valueSelected
								+ "'><td>"
								+ propertySelected
								+ "</td><td>"
								+ valueSelected
								+ '</td><td class="icon" style="white-space: nowrap"><span class="btn btn-xs btn-no-border icon-on-table  tooltips color-red" data-container="body" data-placement="bottom" th:title="#{gen.deleteBtn}" th:data-original-title="#{gen.deleteBtn}" onclick="KafkaClusterConnectionCreateController.removePropertyRow(this)"><i class="icon-delete"></i></span></td></tr>');
		} else {
			//update record
			$('#datamodel_properties > tbody > tr[data-property="'+propertySelected+'"]').find('td:nth-child(2)').text(valueSelected);
			$('#datamodel_properties > tbody > tr[data-property="'+propertySelected+'"]').attr('data-value',valueSelected);
		}
		$("#connectionProperty option").prop("selected", false);
		$('#connectionProperty').selectpicker('destroy');
		tooltipLoader();
		$("#propertyValue").val("");
		showHideImageTableProperties();
		toastr.success(messagesForms.operations.genOpSuccess,kafkaClusterConnectionReg.messages.propertyAdded);
	}
	
	 var removePropertyRow = function(row) {

		row.parentElement.parentElement.remove();
		showHideImageTableProperties();
		toastr.success(messagesForms.operations.genOpSuccess,kafkaClusterConnectionReg.messages.propertyRemoved);
		
	}
	
	var showHideImageTableProperties = function() {
		if(typeof $('#datamodel_properties > tbody > tr').length =='undefined' || $('#datamodel_properties > tbody > tr').length == 0){
			$('#imageNoElementsOnTable').show();
		}else{
			$('#imageNoElementsOnTable').hide();
		}
		
	}  
	
	var getPropertyDefaultValue = function(prop) {
  	   var properties = kafkaClusterConnectionReg.propertiesCatalog;
  	   var defaultValue="";
  	   
  	   for (var i = 0; i < properties.length; i++) {
  		   if(properties[i].id==prop){
  			   defaultValue=properties[i].defaultValue;
  			   break;
  		   }
  		}
  	   
  	   $("#propertyValue").val(defaultValue);
	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function() {
		logControl ? console
				.log('initTemplateElements() -> selectpickers, datepickers, resetForm, today->dateCreated currentLanguage: '
						+ currentLanguage)
				: '';

		
		// INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
		// set current language and formats
		currentLanguage = kafkaClusterConnectionReg.language || LANGUAGE[0];
		currentFormat = (currentLanguage == 'es') ? 'dd/mm/yyyy' : 'mm/dd/yyyy';

		logControl ? console.log('|---> datepickers currentLanguage: '
				+ currentLanguage) : '';

		// Reset form
		$('#resetBtn').on('click', function() {
			cleanFields('kafka_cluster_connection_create_form');
		});
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		

		// INSERT MODE ACTIONS (deviceCreateReg.actionMode = NULL )
		if (kafkaClusterConnectionReg.actionMode === null) {
			logControl ? console.log('action-mode: INSERT') : '';
		}
		// EDIT MODE ACTION
		else {
			logControl ? console.log('action-mode: EDIT') : '';
			$('#identification').prop('readonly', true);
			updateConnectionProperties($("#parameter_connectionProperties").val());
		}

	}
	
	var validateConnectionProperties = function() {
		var listProperties = [];
		
		$("#datamodel_properties tbody tr").each(function(tr) {
			var obj={};
			obj[this.dataset.property]=this.dataset.value;
			listProperties.push(obj);
		});
		$("#parameter_connectionProperties").val(
				JSON.stringify(listProperties));
		return listProperties;
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

		// CLEANING SELECTs
		$(".selectpicker").each(function() {
			$(this).val('');
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});

		// CLEAN ALERT MSG
		$('.alert-danger').hide();

		// CLEAN META INFO
		$('#id_parameter_metaInfo').empty();
		$('#parameter_metaInfo').val('');

		// CLEAN ONTOLOGIES
		
		$("#datamodel_properties tbody tr").each(
			function(tr) {
				this.remove();
		});

		$("#parameter_connectionProperties").val('');
		showHideImageTableProperties();
	}
	
	var deleteClusterConnectionConfirmation = function() {

		var id = kafkaClusterConnectionReg.kafkaClusterConnectionId;

		// no Id no fun!
		if (!id) {
			toastr.error(messagesForms.operations.genOpError,'NO CONNECTION-FORM SELECTED!');
			return false;
		}

		// call Confirm
		showConfirmDeleteDialog(id);
	}

	var showConfirmDeleteDialog = function(id) {

		// i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.kafkaClusterConnectionConfirm;
		var Title = headerReg.kafkaClusterConnectionDelete;

		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title : Title,
			theme : 'light',
			columnClass : 'medium',
			content : Content,
			draggable : true,
			dragWindowGap : 100,
			backgroundDismiss : true,
			buttons : {
				close : {
					text : Close,
					btnClass : 'btn btn-outline blue dialog',
					action : function() {
					} // GENERIC CLOSE.
				},
				remove : {
					text : Remove,
					btnClass : 'btn btn-primary',
					action : function() {
						console.log(id);
						$.ajax({
							url : '/controlpanel/kafka/cluster/' + id,
							headers: {
								[csrf_header]: csrf_value
						    },
							type : 'DELETE',
							success : function(result) {
								console.log(result.responseText);
								navigateUrl('/controlpanel/kafka/cluster/list');
							},
						    error: function(result){
						    	console.log(result.responseText);
						    	toastr.error(messagesForms.operations.genOpError,result.responseText);
						    }
						});
					}
				}
				
			}
		});
	}
	
	
// CONTROLLER PUBLIC FUNCTIONS
	return {
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load : function(Data) {
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return kafkaClusterConnectionReg = Data;
		},

		// INIT() CONTROLLER INIT CALLS
		init : function() {
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleValidation();
			initTemplateElements();
			tooltipLoader();
		},
		// REDIRECT
		go : function(url) {
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			navigateUrl(url);
		},
		cancel: function(id,url){
			logControl ? console.log(LIB_TITLE + ': cancel()') : '';
			navigateUrl(url); 
		},
		// DELETE DEVICE
		deleteClusterConnection : function(id) {
			logControl ? console.log(LIB_TITLE + ': deleteDevice()') : '';
			deleteClusterConnectionConfirmation(id);
		},
		validateConnectionProperties : function() {
			validateConnectionProperties();
		},
		getPropertyDefaultValue : function(prop){
			getPropertyDefaultValue(prop);
		},
		addPropertyRow : function() {
			addPropertyRow();
		},
		removePropertyRow : function(row) {
			removePropertyRow(row);
		},
		deleteClusterConnectionConfirmation : function(data) {
			deleteClusterConnectionConfirmation(data);
		}

	};
}();
// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	KafkaClusterConnectionCreateController.load(kafkaClusterConnectionReg);

	// AUTO INIT CONTROLLER.
	KafkaClusterConnectionCreateController.init();
});