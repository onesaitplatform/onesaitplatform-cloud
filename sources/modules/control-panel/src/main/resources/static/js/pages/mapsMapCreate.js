var MapsMapController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS. 

	var LIB_TITLE = 'Maps Map  Controller';
	var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	

	var emptyMap = {

		"name": "",
		"idDiv": "",
		"providerMap": "ol",
		"zIndex": null,
		"active": true,
		"visible": true,
		"opacity": 1,
		"optionView": {
			"id": null,
			"center": null,
			"zoom": null,
			"projection": "EPSG:4326",
			"extent": null,
			"rotation": 0,
			"maxZoom": 32,
			"minZoom": 2,
			"services": []
		}
	}

var projections =['EPSG:4326','EPSG:3857','EPSG:25828','EPSG:25829','EPSG:25830','EPSG:25831','EPSG:4230','EPSG:4258']

 
	// CONTROLLER PRIVATE FUNCTIONS	



	var navigateUrl = function(url) { window.location.href = url; }

	var deleteMap = function(id) {
		console.log('deleteConfirmation() -> formId: ' + id);

		// no Id no fun!
		if (!id) { toastr.error('NO ELEMENT SELECTED!', ''); return false; }


		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");

		HeaderController.showConfirmDialogMapsGeneric('delete_mapsmap_form');

	}


	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#element_form');

		// set current language
		currentLanguage = templateCreateReg.language || LANGUAGE;

		form1.validate({
			errorElement: 'span', // default input error message
			// container
			errorClass: 'help-block help-block-error', // default input
			// error message
			// class
			focusInvalid: false, // do not focus the last invalid
			// input
			ignore: ":hidden:not(.selectpicker)", // validate all
			// fields including
			// form hidden input
			// but not
			// selectpicker
			lang: currentLanguage,
			// custom messages
			messages: {

			},
			// validation rules
			rules: {
				identification: {
					minlength: 5,
					required: true
				},
				description: {
					minlength: 5,
					required: true
				},

			},
			invalidHandler: function(event, validator) { // display
				// error
				// alert on
				// form
				// submit
				toastr.error(messagesForms.validation.genFormError, '');
			},
			errorPlacement: function(error, element) {
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
			highlight: function(element) { // hightlight error inputs
				$(element).closest('.form-group').addClass('has-error');
			},
			unhighlight: function(element) { // revert the change
				// done by hightlight
				$(element).closest('.form-group').removeClass(
					'has-error');
			},
			success: function(label) {
				label.closest('.form-group').removeClass('has-error');
			},
			// ALL OK, THEN SUBMIT.
			submitHandler: function(form) {


				if (logicValidation()) {
					toastr.success(messagesForms.validation.genFormSuccess, '');
					var auxForm = $('#aux_form');

					$('#identification_aux').val($('#identification').val());
					$('#description_aux').val($('#description').val());
					$('#config_aux').val(createConfig());

					auxForm.attr("action", "?" + csrfParameter + "=" + csrfValue)
					auxForm.submit();
				} else {
					//validations fail 
				}
			}
		});
	}




	//set form fields from param config 
	var initFields = function() {

		if (config == null) {
			config = emptyMap;
		}
		$("#name").val(config.name).change();
		$("#mapname").html(config.name);
		$( "#name" ).on( "change", function() {
  			$("#mapname").html($("#name").val());
		} );

		$("#idDiv").val(config.idDiv).change();
		$("#providerMap").val("ol").change();
		$('#providerMap').attr('disabled',true).selectpicker('refresh');
		$("#zIndex").val(config.zIndex).change();
		$("#opacity").val(typeof config.opacity ==='undefined'?1:config.opacity).change();
		$("#active").val(config.active).change();
		if (config.active) {
			$("#active").prop('checked', true);
		}
		$("#visible").val(config.visible).change();
		if (config.visible) {
			$("#visible").prop('checked', true);
		}
		
		var minZoom = 2;
		var maxZoom = 32;
		if (config.optionView) {
			$("#optionViewId").val(config.optionView.id).change();
			$("#optionViewZoom").val(config.optionView.zoom).change();			
			if(config.optionView.projection && config.optionView.projection.length>0){
				if(!projections.includes(config.optionView.projection)){
					$("#optionViewProjection").append('<option value="'+config.optionView.projection+'">'+config.optionView.projection+'</option>');
				}
			}			
			$("#optionViewProjection").val(config.optionView.projection).change();
			$("#optionViewRotation").val(config.optionView.rotation).change();
			if (typeof config.optionView.center != 'undefined' && config.optionView.center != null && config.optionView.center.length > 0) {
				$("#optionViewCenter0").val(config.optionView.center[0]).change();
				$("#optionViewCenter1").val(config.optionView.center[1]).change();
			}
			if (typeof config.optionView.extent != 'undefined' && config.optionView.extent != null && config.optionView.extent.length > 0) {
				$("#optionViewExtent0").val(config.optionView.extent[0]).change();
				$("#optionViewExtent1").val(config.optionView.extent[1]).change();
				$("#optionViewExtent2").val(config.optionView.extent[2]).change();
				$("#optionViewExtent3").val(config.optionView.extent[3]).change();
			}
		  minZoom = config.optionView.minZoom == null ? 2 : config.optionView.minZoom;
		  maxZoom = config.optionView.maxZoom == null ? 32 : config.optionView.maxZoom;
		}
	   
		//optionView zoom
		$("#optionViewZoomslider").slider({
			range: true,
			min: 2,
			max: 32,
			values: [minZoom, maxZoom],
			slide: function(event, ui) {
				$("#optionViewZooms").val(ui.values[0] + " - " + ui.values[1]);
			}
		});
		$("#optionViewZooms").val($("#optionViewZoomslider").slider("values", 0) +
			" - " + $("#optionViewZoomslider").slider("values", 1));

	if (config.services!=null && typeof config.services=='object' && Array.isArray(config.services)) {
		for(var i = 0 ;i< config.services.length;i++){
			loadLayerRow(config.services[i]);
		}
		layersList=config.services;
	}
	}

	var toNum = function(val) {
		if (val == null || (typeof val === 'String' && val.trim() == "")) {
			return null;
		} else {
			return Number(val);
		}

	}
	var valTwoValues = function(valA, valB) {
		if (valA != null &&
			valB != null &&
			typeof valA != 'undefined' &&
			typeof valB != 'undefined' &&
			valA.trim() != "" &&
			valB.trim() != "") {
			return [toNum(valA), toNum(valB)];
		}
		else {
			return null;
		}

	}
	var valFourValues = function(valA, valB, valC, valD) {
		if (valA != null &&
			valB != null &&
			typeof valA != 'undefined' &&
			typeof valB != 'undefined' &&
			valA.trim() != "" &&
			valB.trim() != ""
			&& valC != null &&
			valD != null &&
			typeof valC != 'undefined' &&
			typeof valD != 'undefined' &&
			valC.trim() != "" &&
			valD.trim() != "") {
			return [toNum(valA), toNum(valB), toNum(valC), toNum(valD)];
		}
		else {
			return null;
		}

	}
	var valSelect = function(val) {
		if (val == "") {
			return null
		} else {
			return val;
		}
	}

	var get = function(id) {
		return $("#" + id).val();
	}
	var getCheck = function(id) {
		return $("#" + id).is(":checked");
	}

	var createConfig = function() {
		var conf = {
			"name": get('name'),
			"idDiv": get('idDiv'),
			"providerMap": valSelect(get('providerMap')),
			"zIndex":  get('zIndex'),
			"active": getCheck('active'),
			"visible": getCheck('visible'),
			"opacity": toNum(get('opacity')),
			"services":layersList,
			"optionView": {
				"id":get('optionViewId'),
				"center":valTwoValues(get('optionViewCenter0'),get('optionViewCenter1')),
				"zoom":toNum(get('optionViewZoom')),
				"projection":valSelect(get('optionViewProjection')),
				"extent":valFourValues(get('optionViewExtent0'),get('optionViewExtent1'),get('optionViewExtent2'),get('optionViewExtent3')),
				"rotation":toNum(get('optionViewRotation')),
				"minZoom": toNum($("#zoomslider").slider("values", 0)),
				"maxZoom": toNum($("#zoomslider").slider("values", 1))
				
			}			
		}

		


		return JSON.stringify(conf);
	}


	var logicValidation = function() {

		return true;
	}


	// CLEAN FIELDS FORM
	var cleanFields = function(formId) {

		//CLEAR OUT THE VALIDATION ERRORS
		$('#' + formId).validate().resetForm();
		$('#' + formId).find('input:text, input:password, input:file, select, textarea').each(function() {
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if (!$(this).hasClass("no-remove")) { $(this).val(''); }
		});


		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}

	// INIT TEMPLATE ELEMENTS
	var init = function() {
		logControl ? console.log('init() -> resetForm') : '';
		// Reset form
		$('#resetBtn').on('click', function() {
			cleanFields('element_form');
		});

		// Fields OnBlur validation		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function(ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});
		initFields();


	}
	var freeResource = function(id, url) {
		console.log('freeResource() -> id: ' + id);
		$.get("/controlpanel/mapsmap/freeResource/" + id).done(
			function(data) {
				console.log('freeResource() -> ok');
				navigateUrl(url);
			}
		).fail(
			function(e) {
				console.error("Error freeResource", e);
				navigateUrl(url);
			}
		)
	}

	// CONTROLLER PUBLIC FUNCTIONS 
	return {
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(elementJson) {
			return templateCreateReg = elementJson;
		},

		// INIT() CONTROLLER INIT CALLS
		init: function() {
			handleValidation();
			init();
		},

		// REDIRECT
		go: function(id, url) {
			freeResource(id, url);

		},
		navigateUrl: function(url) {
			navigateUrl(url);
		},

		// DELETE INITIAL DASHBOARD CONF
		deleteMap: function(id) {
			deleteMap(id);
		}, showHideByMapType: function() {
			showHideByMapType();
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	MapsMapController.load(elementJson);

	// AUTO INIT CONTROLLER.
	MapsMapController.init();
});
