var MapsStyleController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS. 
	var APPNAME = 'onesait Platform Control Panel';
	var LIB_TITLE = 'Maps Style  Controller';
	var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	


	var emptyStyle = {
		'styleType': 'simple',
		'radius': 10,
		'blur': 15,
		//'zIndex': null,
		'circleVertex': false,
		'labelVisibility': false,
		'fill': {
			'color':"#aabbcc"
			},
		'initialRampColor': "#aabbcc",
		'finalRampColor': "#aabbcc",
		'name': null,
		'fieldToClassify': null,
		'stroke': {
			'color': "#aabbcc",
			'width': 2,
			'lineCap': "round",
			'lineJoin': "round",
			'miterLimit': 10,
			'lineDash': null,
			'lineDashOffset': null
		},
		'text': {
			'textVisibility': false,
			'fillColor': "#aabbcc",
			'strokeColor': "#aabbcc",
			'defaultText': null,
			'textField': null,
			'scale': null,
			'font': "Sans Serif + 12px",
			'placement': "point",
			'textAlign': "center",
			'offsetY': 0,
			'offsetX': 0,
			'rotation': 0,
			'textBaseLine': "middle",
			'overflow': false,
			'rotateWithView': false,
			'backgroundFill': "#aabbcc",
			'resolution': 255,
			'padding': [0, 0, 0, 0]
		},
		'image': {
			'src': null,
			'scale': null,
			'position': null,
			'rotateWithView': null,
			'opacity': 100,
			'color': "#aabbcc",
			'displacement': null

		}
	}


	// CONTROLLER PRIVATE FUNCTIONS	


	var getCheck = function(id) {
		return $("#" + id).is(":checked");
	}
	var navigateUrl = function(url) { window.location.href = url; }
	// DELETE DASHBOARDCONF
	/*var deleteDashboardConfConfirmation = function(dashboardconfId){
		console.log('deleteDashboardConfConfirmation() -> formId: '+ dashboardconfId);
		
		// no Id no fun!
		if ( !dashboardconfId ) {toastr('NO INITIAL DASHBOARD CONF SELECTED!',''); return false; }
		
		logControl ? console.log('deleteDashboardConfConfirmation() -> formAction: ' + $('.delete-gadget').attr('action') + ' ID: ' + $('.delete-gadget').attr('userId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogDashboardConf('delete_dashboardconf_form');	
	}
	*/

	var deleteStyle = function(id) {
		console.log('deleteConfirmation() -> formId: ' + id);

		// no Id no fun!
		if (!id) { toastr.error('NO ELEMENT SELECTED!', ''); return false; }


		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");

		HeaderController.showConfirmDialogMapsGeneric('delete_mapsstyle_form');

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
					$('#config_aux').val(createConf());

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

		if (elementID == null || elementID.length == 0) {
			config = emptyStyle;
		}
		if (config != null && Object.keys(config).length > 0) {


			$('#styleType').on('change', function() {
				if (valSelect($("#styleType").val()) !== 'heatmap') {
					$('.noheat').css("display", "block");
				} else {
					$('.noheat').css("display", "none");
				}


			})

			$('#useStrokeLine').on('change', function() {

				if (getCheck('useStrokeLine')) {

					$('#strokelinedash0').attr('disabled', false);
					$('#strokelinedash1').attr('disabled', false);
					$('#strokelineDashOffset0').attr('disabled', false);
				} else {
					$('#strokelinedash0').attr('disabled', true);
					$('#strokelinedash1').attr('disabled', true);
					$('#strokelineDashOffset0').attr('disabled', true);
				}
			})

			$("#useStrokeLine").prop('checked', false);
			$("#useStrokeLine").val(false).change();

			//PROPERTIES
			$("#name").val(config.name).change();
			$("#fieldToClassify").val(config.fieldToClassify).change();
			$("#styleType").val(config.styleType).change();
			$("#radius").val(config.radius).change();
			$("#blur").val(config.blur).change();
			//$("#zIndex").val(config.zIndex).change();

			$("#circleVertex").val(config.circleVertex).change();
			if (config.circleVertex) {
				$("#circleVertex").prop('checked', true);
			}
			$("#labelVisibility").val(config.labelVisibility).change();
			if (config.labelVisibility) {
				$("#labelVisibility").prop('checked', true);
			}
			if (typeof config.fill != 'undefined' && config.fill != null) {
				$("#fill").val(config.fill.color).change();
			}
			$("#initialRampColor").val(config.initialRampColor).change();
			$("#finalRampColor").val(config.finalRampColor).change();

			if (typeof config.stroke != 'undefined' && config.stroke != null) {
				//STROKE
				$("#strokecolor").val(config.stroke.color).change();
				$("#strokewidth").val(config.stroke.width).change();

				if (typeof config.stroke.lineDash != 'undefined' && config.stroke.lineDash != null && config.stroke.lineDash.length > 0) {
					$("#useStrokeLine").prop('checked', true);
					$("#useStrokeLine").val(true).change();
					$("#strokelinedash0").val(config.stroke.lineDash[0]).change();
					$("#strokelinedash1").val(config.stroke.lineDash[1]).change();
				}

				if (typeof config.stroke.lineDashOffset != 'undefined' && config.stroke.lineDashOffset != null) {
					$("#useStrokeLine").prop('checked', true);
					$("#useStrokeLine").val(true).change();
					$("#strokelineDashOffset0").val(config.stroke.lineDashOffset).change();

				}
				$("#strokelineCap").val(config.stroke.lineCap).change();
				$("#strokelineJoin").val(config.stroke.lineJoin).change();
				$("#strokemiterLimit").val(config.stroke.miterLimit).change();
			}
			//TEXT	
			if (typeof config.text != 'undefined' && config.text != null) {
				$("#texttextVisibility").val(config.text.textVisibility).change();
				if (config.text.textVisibility) {
					$("#texttextVisibility").prop('checked', true);
				}
				$("#textfillColor").val(config.text.fillColor).change();
				$("#textstrokeColor").val(config.text.strokeColor).change();
				$("#textdefaultText").val(config.text.defaultText).change();
				$("#texttextField").val(config.text.textField).change();
				$("#textscale").val(config.text.scale).change();
				//$("#textFont").val(config.text.font).change();
				$("#textplacement").val(config.text.placement).change();
				$("#texttextAlign").val(config.text.textAlign).change();
				$("#textoffsetY").val(config.text.offsetY).change();
				$("#textoffsetX").val(config.text.offsetX).change();
				$("#textrotation").val(config.text.rotation).change();
				$("#texttextBaseLine").val(config.text.textBaseLine).change();
				$("#textoverflow").val(config.text.overflow).change();
				if (config.text.overflow) {
					$("#textoverflow").prop('checked', true);
				}
				$("#textrotateWithView").val(config.text.rotateWithView).change();
				if (config.text.rotateWithView) {
					$("#textrotateWithView").prop('checked', true);
				}
				$("#textbackgroundFill").val(config.text.backgroundFill).change();
				if (typeof config.text.padding != 'undefined' && config.text.padding != null && config.text.padding.length > 0) {
					$("#textpadding0").val(config.text.padding[0]).change();
					$("#textpadding1").val(config.text.padding[1]).change();
					$("#textpadding2").val(config.text.padding[2]).change();
					$("#textpadding3").val(config.text.padding[3]).change();
				}
				$("#textresolution").val(config.text.resolution).change();
			}
			//IMAGE
			if (typeof config.image != 'undefined' && config.image != null) {
				$("#imagesrc").val(config.image.src).change();
				$("#imagescale").val(config.image.scale).change();
				$("#imageposition").val(config.image.position).change();

				if (typeof config.image.displacement != 'undefined' && config.image.displacement != null && config.image.displacement.length > 0) {
					$("#imagedisplacement0").val(config.image.displacement[0]).change();
					$("#imagedisplacement1").val(config.image.displacement[1]).change();
				}
				$("#imageopacity").val(config.image.opacity).change();

				$("#imagecolor").val(config.image.color).change();


			}

		}



	}

	var toNum = function(val) {
		if (val == null || val.trim() == "") {
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


	var createConf = function() {
		//update
		if (configResponse != null) {
			return updateConfig();

		} else {
			//create
			return createConfig();
		}
	}

	var updateConfig = function() {
		configResponse.styleType = valSelect($("#styleType").val());
		configResponse.radius = toNum($("#radius").val());
		configResponse.blur = toNum($("#blur").val());
		//configResponse.zIndex = toNum($("#zIndex").val());
		configResponse.circleVertex = $("#circleVertex").is(":checked");
		configResponse.labelVisibility = $("#labelVisibility").is(":checked");
		if (configResponse.fill == null) {
			configResponse.fill = {};
		}
		configResponse.fill.color = $("#fill").val();
		configResponse.initialRampColor = $("#initialRampColor").val();
		configResponse.finalRampColor = $("#finalRampColor").val();
		configResponse.name = $("#name").val();
		configResponse.fieldToClassify = $("#fieldToClassify").val();
		if (configResponse.stroke == null) {
			configResponse.stroke = {};
		}
		if (configResponse.text == null) {
			configResponse.text = {};
		}
		if (configResponse.image == null) {
			configResponse.image = {};
		}

		if (valSelect($("#styleType").val()) === 'heatmap') {
			configResponse.stroke = null;
			configResponse.text = null;
			configResponse.image = null;
		} else {
			configResponse.stroke.color = $("#strokecolor").val();
			configResponse.stroke.width = toNum($("#strokewidth").val());
			configResponse.stroke.lineCap = valSelect($("#strokelineCap").val());
			configResponse.stroke.lineJoin = valSelect($("#strokelineJoin").val());
			configResponse.stroke.miterLimit = toNum($("#strokemiterLimit").val());
			configResponse.stroke.lineDash = valTwoValues($("#strokelinedash0").val(), $("#strokelinedash1").val());
			configResponse.stroke.lineDashOffset = toNum($("#strokelineDashOffset0").val());
			if (!getCheck('useStrokeLine')) {
				configResponse.stroke.lineDash = null;
				configResponse.stroke.lineDashOffset = null;
			}
			configResponse.text.textVisibility = $("#texttextVisibility").is(":checked");
			configResponse.text.fillColor = $("#textfillColor").val();
			configResponse.text.strokeColor = $("#textstrokeColor").val();
			configResponse.text.defaultText = $("#textdefaultText").val();
			configResponse.text.textField = $("#texttextField").val();
			configResponse.text.scale = toNum($("#textscale").val());
			//font= $("#textFont").val();
			configResponse.text.placement = valSelect($("#textplacement").val());
			configResponse.text.textAlign = valSelect($("#texttextAlign").val());
			configResponse.text.offsetY = toNum($("#textoffsetY").val());
			configResponse.text.offsetX = toNum($("#textoffsetX").val());
			configResponse.text.rotation = toNum($("#textrotation").val());
			configResponse.text.textBaseLine = valSelect($("#texttextBaseLine").val());
			configResponse.text.overflow = $("#textoverflow").is(":checked");
			configResponse.text.rotateWithView = $("#textrotateWithView").is(":checked");
			configResponse.text.backgroundFill = $("#textbackgroundFill").val();
			configResponse.text.resolution = toNum($("#textresolution").val());
			configResponse.text.padding = valFourValues($("#textpadding0").val(), $("#textpadding1").val(), $("#textpadding2").val(), $("#textpadding3").val());


			configResponse.image.src = $("#imagesrc").val();
			configResponse.image.scale = toNum($("#imagescale").val());
			configResponse.image.position = valSelect($("#imageposition").val());

			configResponse.image.opacity = toNum($("#imageopacity").val());

			configResponse.image.color = $("#imagecolor").val();

			configResponse.image.displacement = valTwoValues($("#imagedisplacement0").val(), $("#imagedisplacement1").val());

		}



		return JSON.stringify(configResponse);
	}


	var createConfig = function() {
		var conf = {
			'styleType': valSelect($("#styleType").val()),
			'radius': toNum($("#radius").val()),
			'blur': toNum($("#blur").val()),
			//'zIndex': toNum($("#zIndex").val()),
			'circleVertex': $("#circleVertex").is(":checked"),
			'labelVisibility': $("#labelVisibility").is(":checked"),
			'fill':{'color': $("#fill").val()},
			'initialRampColor': $("#initialRampColor").val(),
			'finalRampColor': $("#finalRampColor").val(),
			'name': $("#name").val(),
			'fieldToClassify': $("#fieldToClassify").val(),
			'stroke': {},
			'text': {},
			'image': {}
		}
		 

		conf.stroke = {
			'color': $("#strokecolor").val(),
			'width': toNum($("#strokewidth").val()),
			'lineCap': valSelect($("#strokelineCap").val()),
			'lineJoin': valSelect($("#strokelineJoin").val()),
			'miterLimit': toNum($("#strokemiterLimit").val()),
			'lineDash': valTwoValues($("#strokelinedash0").val(), $("#strokelinedash1").val()),
			'lineDashOffset': toNum($("#strokelineDashOffset0").val())
		}
		if (!getCheck('useStrokeLine')) {
			conf.stroke.lineDash = null;
			conf.stroke.lineDashOffset = null;
		}
		conf.text = {
			'textVisibility': $("#texttextVisibility").is(":checked"),
			'fillColor': $("#textfillColor").val(),
			'strokeColor': $("#textstrokeColor").val(),
			'defaultText': $("#textdefaultText").val(),
			'textField': $("#texttextField").val(),
			'scale': toNum($("#textscale").val()),
			//'font': $("#textFont").val(),
			'placement': valSelect($("#textplacement").val()),
			'textAlign': valSelect($("#texttextAlign").val()),
			'offsetY': toNum($("#textoffsetY").val()),
			'offsetX': toNum($("#textoffsetX").val()),
			'rotation': toNum($("#textrotation").val()),
			'textBaseLine': valSelect($("#texttextBaseLine").val()),
			'overflow': $("#textoverflow").is(":checked"),
			'rotateWithView': $("#textrotateWithView").is(":checked"),
			'backgroundFill': $("#textbackgroundFill").val(),
			'resolution': toNum($("#textresolution").val()),
			'padding': valFourValues($("#textpadding0").val(), $("#textpadding1").val(), $("#textpadding2").val(), $("#textpadding3").val())
		}


		conf.image = {
			'src': $("#imagesrc").val(),
			'scale': toNum($("#imagescale").val()),
			'position': valSelect($("#imageposition").val()),

			'opacity': toNum($("#imageopacity").val()),

			'color': $("#imagecolor").val(),

			'displacement': valTwoValues($("#imagedisplacement0").val(), $("#imagedisplacement1").val())

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
		$.get("/controlpanel/mapsstyle/freeResource/" + id).done(
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

	var showHideStroke = function() {
		var strokelineJoin = $("#strokelineJoin").val();
		$('#divmiterlimit').hide();
		if (strokelineJoin === 'miter') {
			$('#divmiterlimit').show();
		}
	}

	var showHideText = function() {
		var texttextVisibility = $("#texttextVisibility").is(":checked");

		$("#divtextfillColor").hide();
		$("#divtextstrokeColor").hide();
		$("#divtextdefaultText").hide();
		$("#divtexttextField").hide();
		$("#divtexttextAlign").hide();
		$("#divtextoffsetX").hide();
		$("#divtextoffsetY").hide();

		$("#divtextrotation").hide();
		$("#divtexttextBaseLine").hide();
		$("#divtextoverflow").hide();
		$("#divtextrotateWithView").hide();
		$("#divtextbackgroundFill").hide();
		$("#divtextresolution").hide();
		$("#divtextpadding").hide();
		$("#divtextplacement").hide();
		$("#divtextscale").hide();

		if (texttextVisibility) {
			$("#divtextfillColor").show();
			$("#divtextstrokeColor").show();
			$("#divtextdefaultText").show()
			if ($("#textdefaultText").val() == null || $("#textdefaultText").val().trim.length == 0) {
				$("#divtexttextField").show();
			}
			if ($("#textplacement").val() == 'point') {
				$("#divtexttextAlign").show();
				$("#divtextbackgroundFill").show();
			}
			$("#divtextoffsetX").show();
			$("#divtextoffsetY").show();
			$("#divtextrotation").show();
			$("#divtexttextBaseLine").show();
			$("#divtextoverflow").show();
			$("#divtextrotateWithView").show();
			$("#divtextresolution").show();
			$("#divtextpadding").show();
			$("#divtextplacement").show();
			$("#divtextscale").show();
		}
	}


	// CONTROLLER PUBLIC FUNCTIONS 
	return {
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(elementJson) {

			logControl ? console.log(LIB_TITLE + ': load()') : '';

			return templateCreateReg = elementJson;
		},

		// INIT() CONTROLLER INIT CALLS
		init: function() {
			logControl ? console.log(LIB_TITLE + ': init()') : '';

			handleValidation();
			init();
			showHideStroke();
			showHideText();

		},
		showHide: function() {
			showHideStroke();
			showHideText();
		},

		// REDIRECT
		go: function(id, url) {
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			freeResource(id, url);

		},

		navigateUrl: function(url) {
			navigateUrl(url);
		},

		// DELETE INITIAL DASHBOARD CONF
		deleteStyle: function(id) {
			logControl ? console.log(LIB_TITLE + ': deleteStyle()') : '';
			deleteStyle(id);
		},



	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	MapsStyleController.load(elementJson);

	// AUTO INIT CONTROLLER.
	MapsStyleController.init();
});
