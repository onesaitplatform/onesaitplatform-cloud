var DeviceCreateController = function() {

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

	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/devices/freeResource/" + id).done(
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
					$("#onto").append(
							'<option value="' + this.dataset.ontology + '">'
									+ this.dataset.ontology + '</option>');
					this.remove();

				});
		$('.onto').selectpicker('refresh');

		$("#parameter_clientPlatformOntologies").val('');

	}

	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#device_create_form');
		var error1 = $('.alert-danger');
		var success1 = $('.alert-success');

		// set current language
		currentLanguage = deviceCreateReg.language || LANGUAGE;

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

					},
					invalidHandler : function(event, validator) { // display
																	// error
																	// alert on
																	// form
																	// submit
						success1.hide();
						error1.show();
						App.scrollTo(error1, -200);
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

						if (valOntologies()) {
							success1.show();
							error1.hide();
							form.submit();
							// insert();
						} else {
							success1.hide();
							$.alert({
								title : 'ERROR!',
								type : 'red',
								theme : 'light',
								content : deviceCreateJson.ontologyNotSelected
							});
							return false;
							error1.show();
						}
					}
				});
	}

	var valOntologies = function() {
		return (validateOntologies().length > 0);
	}

	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function() {
		logControl ? console
				.log('initTemplateElements() -> selectpickers, datepickers, resetForm, today->dateCreated currentLanguage: '
						+ currentLanguage)
				: '';

		// selectpicker validate fix when handleValidation()
		$('.selectpicker').on('change', function() {
			$(this).valid();
		});

		// INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
		// set current language and formats
		currentLanguage = deviceCreateReg.language || LANGUAGE[0];
		currentFormat = (currentLanguage == 'es') ? 'dd/mm/yyyy' : 'mm/dd/yyyy';

		logControl ? console.log('|---> datepickers currentLanguage: '
				+ currentLanguage) : '';

		// Reset form
		$('#resetBtn').on('click', function() {
			cleanFields('device_create_form');
		});

		// INSERT MODE ACTIONS (deviceCreateReg.actionMode = NULL )
		if (deviceCreateReg.actionMode === null) {
			logControl ? console.log('action-mode: INSERT') : '';
			updateTokens($("#parameter_clientPlatformTokens").val());
		}
		// EDIT MODE ACTION
		else {
			updateMetainfo($('#parameter_metaInfo').val());
			updateOntologies($("#parameter_clientPlatformOntologies").val());
			refreshTokens($("#identification").val());
		}

	}

	var addMetainfo = function() {

		var nombre = document.getElementById("name_metainfo").value;
		var valor = document.getElementById("value_metainfo").value;
		
		if (nombre === ''){ $.alert({title : 'Information:',theme : 'light',content : deviceCreateJson.invalidMeta}); return false; }
		
		var p = document.createElement('p');
		text = document.createTextNode(nombre + '=' + valor);

		var div = document.createElement('div');
		div.className = "metainfo tag label label-primary";
		p.appendChild(text);

		var span = document.createElement('span');
		span.className = "fa fa-times";
		span.onclick = function() {
			this.parentNode.parentElement.remove()
		};

		div.appendChild(p);
		p.appendChild(span);

		$("#id_parameter_metaInfo").append(div);

		if (document.getElementById("parameter_metaInfo").value != '') {
			document.getElementById("parameter_metaInfo").value = document
					.getElementById("parameter_metaInfo").value
					+ '#' + nombre + '=' + valor;
		} else {
			document.getElementById("parameter_metaInfo").value = nombre + '='
					+ valor;
		}
		
		// remove values of meta info name and value to add new more...
		$('#name_metainfo').val('');
		$('#value_metainfo').val('');

	}

	var updateMetainfo = function(metaInfoValue) {
		if (metaInfoValue !== null && metaInfoValue.length > 0) {
			var metaInfoElements = metaInfoValue.split('#');
			for (var i = 0; i < metaInfoElements.length; i++) {
				var metaVal = metaInfoElements[i];
				var p = document.createElement('p');
				text = document.createTextNode(metaVal);
				var div = document.createElement('div');
				div.className = "metainfo tag label label-primary";
				p.appendChild(text);
				var span = document.createElement('span');
				span.className = "fa fa-times";
				span.onclick = function() {
					this.parentNode.parentElement.remove()
				};
				div.appendChild(p);
				p.appendChild(span);
				$("#id_parameter_metaInfo").append(div);

			}

		}

	}

	var addOntologyRow = function() {

		var ontoSelected = $("#onto option:selected").text();
		var levelSelected = $("#accessLevel option:selected").text();

		if (ontoSelected === "") {
			$.alert({
				title : 'ERROR!',
				type : 'red',
				theme : 'light',
				content : deviceCreateJson.ontologyNotSelected
			});
			return false;
		}
		$('#datamodel_properties > tbody')
				.append(
						'<tr data-ontology="'
								+ ontoSelected
								+ '" data-level="'
								+ levelSelected
								+ '"><td>'
								+ ontoSelected
								+ '</td><td >'
								+ levelSelected
								+ '</td><td class="text-center"><button type="button" data-property="" class="btn btn-sm btn-circle btn-outline blue" onclick="DeviceCreateController.removeOntology(this)" th:text="#{device.ontology.remove}"><span th:text="#{gen.deleteBtn}"> Delete </span></button></td></tr>');
		$(".onto select option:selected").remove();
		$('.onto').selectpicker('refresh');
	}

	
	var updateOntologies = function(ontologies) {
		var selectedOntologies = JSON.parse(ontologies);
		if (selectedOntologies !== null && selectedOntologies.length > 0) {
			for (var i = 0; i < selectedOntologies.length; i++) {
				var onto = selectedOntologies[i];
				$('#datamodel_properties > tbody')
						.append(
								'<tr data-ontology="'
										+ onto.id
										+ '" data-level="'
										+ onto.access
										+ '"><td>'
										+ onto.id
										+ '</td><td >'
										+ onto.access
										+ '</td><td class="text-center"><button type="button" data-property="" class="btn btn-sm btn-circle btn-outline blue" onclick="DeviceCreateController.removeOntology(this)" th:text="#{device.ontology.remove}"><span th:text="#{gen.deleteBtn}"> Delete </span></button></td></tr>');
			}
			$(".onto select option:selected").remove();
			$('.onto').selectpicker('refresh');
		}

	}

	var removeOntology = function(row) {
		var ontoSelected = row.parentElement.parentElement.firstElementChild.innerHTML;
		$("#onto").append(
				'<option value="' + ontoSelected + '">' + ontoSelected
						+ '</option>');
		$('.onto').selectpicker('refresh');
		row.parentElement.parentElement.remove();
	}

	var validateOntologies = function() {
		var listOntology = [];
		$("#datamodel_properties tbody tr").each(function(tr) {
			listOntology.push({
				id : this.dataset.ontology,
				access : this.dataset.level
			});
		});
		$("#parameter_clientPlatformOntologies").val(
				JSON.stringify(listOntology));

		return listOntology;
	}

	var generateToken = function() {
		var selectedDevice = $("#identification").val();
		var request = {
			deviceIdentification : selectedDevice
		};
		requestData = JSON.stringify(request);
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		var url = "/controlpanel/devices/generateToken";
		if(multitenancyEnabled === 'true' && $('#tenants').val()!=null ){
			url+='?tenant='+$('#tenants').val();
		}

		$.ajax({
			url : url,
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'POST',
			dataType : 'json',
			data : requestData,
			contentType : 'application/json',
			mimeType : 'application/json',
			success : function(data) {
				if (data.ok) {
					refreshTokens(selectedDevice);
				} else {
					$.alert({
						title : 'ERROR!',
						type : 'red',
						theme : 'light',
						content : deviceCreateJson.deviceTokenCreateError
					});
					return false;
				}
			},
			error : function(data, status, er) {
				$.alert({
					title : 'ERROR!',
					type : 'red',
					theme : 'light',
					content : deviceCreateJson.deviceTokenCreateError
				});
				return false;
			}
		});
	}

	var updateTokens = function(tokens) {

		var selectedtokens = JSON.parse(tokens);
		$('#datamodel_tokens > tbody').empty();
		if (selectedtokens !== null && selectedtokens.length > 0) {
			for (var i = 0; i < selectedtokens.length; i++) {
				var token = selectedtokens[i];
				var checked = '';
				var disableButton = "";
				if(deviceCreateReg.actionMode === null){
					disableButton = "disabled"; 
				}
				if (token.active) {
					checked = 'checked';
				}
				var html = '<tr data-id="'
					+ token.id
					+ '"><td>'
					+ token.token
					+ '</td>';
					if(multitenancyEnabled === 'true'){
						if(token.tenant !=null)
							html+='<td>'+token.tenant+'</td>';
						else
							html+='<td>'+currentTenant+'</td>';
					}
					html+='<td><input '+disableButton+' class="form-check-input" type="checkbox" onclick="DeviceCreateController.changeEstatusToken(this);" value="'
					+ token.active
					+ '" '
					+ checked
					+ '></td><td class="icon" style="white-space: nowrap"><div class="grupo-iconos"><button   id="deleteBtn" type="button" class="btn btn-circle btn-outline blue" '+disableButton+' name="delete"  value="Remove" onclick="DeviceCreateController.showConfirmDialogDeleteToken(this);" ><span th:text="#{gen.deleteBtn}"> Delete </span></button></div></td></tr>';
				$('#datamodel_tokens > tbody').append(html);
				$('#parameter_clientPlatformTokens').val(tokens)


			}
		}
		
		$("#parameter_clientPlatformTokens").val(tokens);
	}

	var changeEstatusToken = function(check) {
		var $row = check.closest("tr"); // Find the row
		var selectedToken = $row.dataset.id;
		var selectedStatus = check.checked;
		activateDeactivateToken(selectedToken, selectedStatus);

	}

	var activateDeactivateToken = function(selectedToken, selectedStatus) {
		var data = {
			token : selectedToken,
			active : selectedStatus
		};
		requestData = JSON.stringify(data);
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		$.ajax({
			url : "/controlpanel/devices/desactivateToken",
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'POST',
			dataType : 'json',
			data : requestData,
			contentType : 'application/json',
			mimeType : 'application/json',
			success : function(data) {
				if (!data.ok) {
					$.alert({
						title : 'ERROR!',
						type : 'red',
						theme : 'light',
						content : deviceCreateJson.deviceChangeActiveError
					});
					return false;
				}else{
					refreshTokens($("#identification").val());
				}

			},
			error : function(data, status, er) {
				$.alert({
					title : 'ERROR!',
					type : 'red',
					theme : 'light',
					content : deviceCreateJson.deviceChangeActiveError
				});
				return false;
			}
		});
	}

	var deleteToken = function(tokenToDelete) {
		var $row = tokenToDelete.closest("tr"); // Find the row
		var selectedToken = $row.dataset.id;
		var selectedDevice = $("#identification").val();
		var request = {
			token : selectedToken
		};
		requestData = JSON.stringify(request);
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		$.ajax({
			url : "/controlpanel/devices/deleteToken",
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'POST',
			dataType : 'json',
			data : requestData,
			contentType : 'application/json',
			mimeType : 'application/json',
			success : function(data) {
				if (data.ok) {
					refreshTokens(selectedDevice);
				} else {
					$.alert({
						title : 'ERROR!',
						type : 'red',
						theme : 'light',
						content : deviceCreateJson.deviceTokenDeleteError
					});
					return false;
				}
			},
			error : function(data, status, er) {
				$.alert({
					title : 'ERROR!',
					type : 'red',
					theme : 'light',
					content : deviceCreateJson.deviceTokenDeleteError
				});
				return false;
			}
		});
	}

	function refreshTokens(selectedDevice) {
		var request = {
			deviceIdentification : selectedDevice
		};
		requestData = JSON.stringify(request);
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		$.ajax({
			url : "/controlpanel/devices/loadDeviceTokens",
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'POST',
			dataType : 'json',
			data : requestData,
			contentType : 'application/json',
			mimeType : 'application/json',
			success : function(data) {
				updateTokens(JSON.stringify(data));
			},
			error : function(data, status, er) {
			}
		});
	}

	var deleteDeviceConfirmation = function() {

		var id = deviceCreateReg.deviceId;

		// no Id no fun!
		if (!id) {
			$.alert({
				title : 'ERROR!',
				type : 'red',
				theme : 'light',
				content : 'NO DEVICE-FORM SELECTED!'
			});
			return false;
		}

		// call Confirm
		showConfirmDeleteDialog(id);
	}

	var showConfirmDeleteDialog = function(id) {

		// i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = deviceCreateJson.deleteDeviceConfirm;
		var Title = headerReg.titleConfirm + ':';

		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			icon : 'fa fa-warning',
			title : Title,
			theme : 'light',
			columnClass : 'medium',
			content : Content,
			draggable : true,
			dragWindowGap : 100,
			backgroundDismiss : true,
			closeIcon : true,
			buttons : {
				close : {
					text : Close,
					btnClass : 'btn btn-sm btn-circle blue btn-outline',
					action : function() {
					} // GENERIC CLOSE.
				},
				remove : {
					text : Remove,
					btnClass : 'btn btn-sm btn-circle btn-primary btn-outline',
					action : function() {
						console.log(id);
						$.ajax({
							url : '/controlpanel/devices/' + id,
							headers: {
								[csrf_header]: csrf_value
						    },
							type : 'DELETE',
							success : function(result) {
								console.log(result.responseText);
								navigateUrl('/controlpanel/devices/list');
							},
						    error: function(result){
						    	console.log(result.responseText);
						    	$.alert({
									title : 'ERROR!',
									type : 'red',
									theme : 'light',
									content : result.responseText
								});
						    }
						});
					}
				}
				
			}
		});
	}

	// CONFIRM DIALOG DELETE TOKEN
	var showConfirmDialogDeleteToken = function(data) {

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = deviceCreateJson.deleteTokenConfirm;
		var Title = headerReg.titleConfirm + ':';

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			icon : 'fa fa-warning',
			title : Title,
			theme : 'light',
			columnClass : 'medium',
			content : Content,
			draggable : true,
			dragWindowGap : 100,
			backgroundDismiss : true,
			closeIcon : true,
			buttons : {
				close : {
					text : Close,
					btnClass : 'btn btn-sm btn-circle blue btn-outline',
					action : function() {
					} // GENERIC CLOSE.
				},
				remove : {
					text : Remove,
					btnClass : 'btn btn-sm btn-circle btn-primary btn-outline',
					action : function() {
						deleteToken(data);
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
			return deviceCreateReg = Data;
		},

		// INIT() CONTROLLER INIT CALLS
		init : function() {
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleValidation();
			initTemplateElements();
			if(deviceCreateReg.ontologyId != null){
				$('#onto').val(deviceCreateReg.ontologyId).change();
			}

		},
		// REDIRECT
		go : function(url) {
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			navigateUrl(url);
		},
		cancel: function(id,url){
			logControl ? console.log(LIB_TITLE + ': cancel()') : '';			
			freeResource(id,url);
		},
		// DELETE DEVICE
		deleteDevice : function(id) {
			logControl ? console.log(LIB_TITLE + ': deleteDevice()') : '';
			deleteDeviceConfirmation(id);
		},
		// JSON SCHEMA VALIDATION
		validateOntologies : function() {
			validateOntologies();
		},
		addMetainfo : function() {
			addMetainfo();
		},
		addOntologyRow : function() {
			addOntologyRow();
		},
		removeOntology : function(row) {
			removeOntology(row);
		},
		generateToken : function() {
			generateToken();
		},
		changeEstatusToken : function(data) {
			changeEstatusToken(data);
		},
		showConfirmDialogDeleteToken : function(data) {
			showConfirmDialogDeleteToken(data);
		},
		deleteDeviceConfirmation : function(data) {
			deleteDeviceConfirmation(data);
		}

	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	DeviceCreateController.load(deviceCreateJson);

	// AUTO INIT CONTROLLER.
	DeviceCreateController.init();
});
