var PrestoDatasourceCreateController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var APPNAME = 'onesait Platform Control Panel';
	var LIB_TITLE = 'Menu Controller';
	var logControl = 1;
	var LANGUAGE = [ 'es' ];
	var currentLanguage = ''; // loaded from template.
	var currentFormat = '' // date format depends on currentLanguage.
	var internalFormat = 'yyyy/mm/dd';
	var internalLanguage = 'en';
	var csrf_value = $("meta[name='_csrf']").attr("content");
	var csrf_header = $("meta[name='_csrf_header']").attr("content");
	var properties = [];
	
	// CONTROLLER PRIVATE FUNCTIONS

	// REDIRECT URL
	var navigateUrl = function(url) {
		window.location.href = url;
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

		// CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');
		
		// CLEAN PROPERTIES
		$("#").val('');
		cleanPropertiesSelector();
		cleanPropertiesTable();
		showHideImagePropertiesTable();
	}
	
	function showHideImagePropertiesTable(){
		if(typeof $('#properties_table > tbody > tr').length =='undefined' || $('#properties_table > tbody > tr').length == 0){
			$('#imageNoElementsOnTable').show();
		}else{
			$('#imageNoElementsOnTable').hide();
		}
		
	}

	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#datasource_create_form');

		// set current language
		currentLanguage = datasourceCreateReg.language || LANGUAGE;

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
						}
					},
					invalidHandler : function(event, validator) { // display error
						toastr.error(datasourceCreateReg.validations.formError,'');						
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
						if (datasourceCreateReg.actionMode === null && !validateIdentificationNotExists()) {
							toastr.error(messagesForms.operations.genOpError,datasourceCreateReg.validations.datasourceExists);						
						} else if (!validateConnectionProperties()) {
							toastr.error(messagesForms.operations.genOpError,datasourceCreateReg.validations.requiredProperties);						
						} else {
							showConfirmDialog('datasource_create_form');
						}
					}
				});
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

		// INPUT MASK FOR datasource identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-z0-9_]*", greedy: false });
		
		// set current language and formats
		currentLanguage = datasourceCreateReg.language || LANGUAGE[0];
		currentFormat = (currentLanguage == 'es') ? 'dd/mm/yyyy' : 'mm/dd/yyyy';

		logControl ? console.log('|---> datepickers currentLanguage: '
				+ currentLanguage) : '';

		// Reset form
		$('#resetBtn').on('click', function() {
			cleanFields('datasource_create_form');
		});
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		

		// INSERT MODE ACTIONS (datasourceCreateReg.actionMode = NULL )
		if (datasourceCreateReg.actionMode === null) {
			logControl ? console.log('action-mode: INSERT') : '';
		}
		// EDIT MODE ACTION
		else {
			logControl ? console.log('action-mode: EDIT') : '';
			getPropertiesByType($("#type").val());
			updateProperties($("#parameter_properties_list").val());
		}

	}

	var addPropertyRow = function() {

		var dsPropertyName = $("#propertyName").val();
		var dsPropertyValue = $("#propertyValue").val();

		if (dsPropertyName === "") {
			toastr.error(messagesForms.operations.genOpError, datasourceCreateReg.validations.propertyNotSelected);
			return false;
		}
		if (dsPropertyValue === "") {
			toastr.error(messagesForms.operations.genOpError, datasourceCreateReg.validations.propertyValueEmpty);
			return false;
		}
		var match = $('#properties_table > tbody > tr[data-property-name="' + dsPropertyName + '"]');
		var matching = match.length==1;
		if(!matching){
			$('#properties_table > tbody')
				.append(
						'<tr data-property-name="'
								+ dsPropertyName
								+ '" data-property-value="'
								+ dsPropertyValue
								+ '" style="max-width:20"><td>'
								+ dsPropertyName
								+ '</td><td >'
								+ dsPropertyValue
								+ '</td><td class="icon" style="white-space: nowrap"><span class="btn btn-xs btn-no-border icon-on-table  tooltips color-red" data-container="body" data-placement="bottom" th:title="#{gen.deleteBtn}" th:data-original-title="#{gen.deleteBtn}" onclick="PrestoDatasourceCreateController.removePropertyRow(this)"><i class="icon-delete"></i></span></td></tr>');
		} else {
			$('#properties_table > tbody > tr[data-property-name="' + dsPropertyName + '"]').find('td:nth-child(2)').text(dsPropertyValue);
			$('#properties_table > tbody > tr[data-property-name="' + dsPropertyName + '"]').attr('data-property-value',dsPropertyValue);
		}
		$("#propertyName").val('');
		$("#propertyValue").val('');
		$('#propertyName').selectpicker('destroy');
		tooltipLoader();
		showHideImagePropertiesTable();
		toastr.success(messagesForms.operations.genOpSuccess, '');
	}
	
	var updateProperties = function(properties) {
		var propertiesList = JSON.parse(properties);
		if (propertiesList !== null && propertiesList.length > 0) {
			for (var i = 0; i < propertiesList.length; i++) {
				var property = propertiesList[i];
				
				$('#properties_table > tbody')
						.append(
								'<tr data-property-name="'
										+ property.name
										+ '" data-property-value="'
										+ property.value
										+ '"><td>'
										+ property.name
										+ '</td><td >'
										+ property.value
										+ '</td><td class="icon" style="white-space: nowrap"><span class="btn btn-xs btn-no-border icon-on-table  tooltips color-red" data-container="body" data-placement="bottom" th:title="#{gen.deleteBtn}" th:data-original-title="#{gen.deleteBtn}" onclick="PrestoDatasourceCreateController.removePropertyRow(this)" th:text="#{gen.remove}"><i class="icon-delete"></i></span></td></tr>');
			}
			$(".onto select option:selected").remove();
			$('.onto').selectpicker('refresh');
			showHideImagePropertiesTable();
		}

	}

	var removePropertyRow = function(row) {	
		row.parentElement.parentElement.remove();
		showHideImagePropertiesTable();
		toastr.success(messagesForms.operations.genOpSuccess, '');
	}

	var validateConnectionProperties = function() {
		var propertiesList = [];
		$("#properties_table tbody tr").each(function(tr) {
			propertiesList.push({
				name : this.getAttribute("data-property-name"),
				value : this.getAttribute("data-property-value")
			});
		});
		$("#parameter_properties_list").val(JSON.stringify(propertiesList));

		return validateRequiredConnectionProperties(propertiesList);
	}
	
	var validateRequiredConnectionProperties = function(propertiesList) {
		for (var i=0; i < properties.length; i++) {
			if(properties[i].required && propertiesList.filter(obj => {return obj.name === properties[i].name}).length == 0)
				return false;
		}
		return true;
	}
	
	var validateIdentificationNotExists = function() {
		return (datasourceCreateJson.catalogList.indexOf($('#identification').val()) === -1);
	}
	
		
	// DELETE datasource
	var deleteDatasourceConfirmation = function(datasourceId) {
		console.log('deleteDatasourceConfirmation() -> formId: '+ datasourceId);
		
		// no Id no fun!
		if ( !datasourceId ) {
			toastr.error(messagesForms.operations.genOpError,'NO DATSOURCE-FORM SELECTED!');
			return false; 
		}
		
		// set action and datasourceId to the form
		$('#delete-datasourceId').attr('datasourceId',datasourceId);
		$('.delete-datasource').attr('action','/controlpanel/presto/datasources/' + datasourceId);
		console.log('deleteDatasourceConfirmation() -> formAction: ' + $('.delete-datasource').attr('action') + ' ID: ' + $('#delete-datasourceId').attr('datasourceId'));
		
		// call delete Confirm at header.
		HeaderController.showConfirmDialogPrestoDatasource('delete_datasource_create_form');	
	}
	
	//get connection properties
	var getPropertiesByType = function(type) {

		cleanPropertiesTable();

		var url = "/controlpanel/presto/datasources/" + type + "/properties/";
		$.ajax({
			url : url,
			headers: {
				[csrf_header]: csrf_value
			},
			type : 'GET',
			dataType: 'text', 
			contentType: 'text/plain',
			mimeType: 'text/plain',
			success : function(data, xhr) {	
				cleanPropertiesSelector();
				properties = JSON.parse(data);				
				var count = properties.length;
				if (count > 0) {
					$.each( properties, function (key, object){
						if (object.required) {
							$('#propertyName').append("<option value='"+ object.name +"' text='" + object.name + "' data-toggle='tooltip' mytooltip='" + object.description + "'>"  + object.name + " *</option>");
						} else {
							$('#propertyName').append("<option value='"+ object.name +"' text='" + object.name + "' data-toggle='tooltip' mytooltip='" + object.description + "'>"  + object.name + "</option>");		
						}		
					});
				} 
				$('#propertyName').selectpicker('destroy');
				tooltipLoader();
			},
			error : function() {
				toastr.error(messagesForms.operations.genOpError, datasourceCreateReg.validations.properties);
				//clean type selector
				$("#type").val('');
				$("#type").selectpicker('refresh');
			}
		});
	}
	
	var tooltipLoader = function() {
		$('#propertyName').selectpicker({
				    liveSearch: true
		  }).on('loaded.bs.select', function(e){
		    // save the element
		    var $el = $(this);
		    // the list items with the options
		    var $lis = $el.data('selectpicker').$lis;
		    $lis.each(function(i) {
		      // get the title from the option
		      var tooltip_title = $el.find('option').eq(i+1).attr('mytooltip');
		      $(this).tooltip({
		         'title': tooltip_title,
		         'placement': 'top'
		      });
		   });
		  });
	}
	
	
	var getPropertyDefaultValue = function(property) {
  	   
  	   var defaultValue = "";
  	   
  	   for (var i = 0; i < properties.length; i++) {
  		   if(properties[i].name == property){
  			   defaultValue = properties[i].defaultValue;
  			   break;
  		   }
  		}
  	   
  	   $("#propertyValue").val(defaultValue);
	}
	
	var cleanPropertiesTable = function() {
		$("#parameter_properties_list").empty();
		$("#properties_table tbody tr").remove();
	} 
	
	var cleanPropertiesSelector = function() {
		properties = [];
		$('#propertyName').empty();
		$('#propertyName').selectpicker('refresh');
	}
	
	var showConfirmDialog = function(formId) {
		var titleText = '';
		if (datasourceCreateReg.actionMode === null) {
			titleText = datasourceCreateReg.createConnectionTitle;
		} else {
			titleText = datasourceCreateReg.updateConnectionTitle;
		}
		
		$.confirm({
			title: titleText,
			theme: 'light',			
			columnClass: 'medium',
			content: datasourceCreateReg.contactMessage,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: datasourceCreateReg.closeBtn,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				save: {
					text: datasourceCreateReg.confirmBtn,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { 
							document.forms[formId].submit(); 
							toastr.success(messagesForms.operations.genOpSuccess,'');						
						} else { 
							$.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); 
						}
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
			return datasourceCreateReg = Data;
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
		cancel: function(id,url){
			logControl ? console.log(LIB_TITLE + ': cancel()') : '';			
			freeResource(id,url);
		},
		// DELETE DATASOURCE
		deleteDatasource : function(id) {
			logControl ? console.log(LIB_TITLE + ': deleteDatasource()') : '';
			deleteDatasourceConfirmation(id);
		},
		addPropertyRow : function() {
			addPropertyRow();
		},
		removePropertyRow : function(row) {
			removePropertyRow(row);
		},
		deleteDatasourceConfirmation : function(id) {
			deleteDatasourceConfirmation(id);
		},
		getPropertiesByType : function(type){
			getPropertiesByType(type);
		},
		getPropertyDefaultValue : function(property) {
			getPropertyDefaultValue(property);
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	PrestoDatasourceCreateController.load(datasourceCreateJson);

	// AUTO INIT CONTROLLER.
	PrestoDatasourceCreateController.init();
});
