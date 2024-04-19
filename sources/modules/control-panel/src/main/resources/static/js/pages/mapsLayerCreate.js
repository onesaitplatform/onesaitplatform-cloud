var MapsLayerController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS. 

	var LIB_TITLE = 'Maps Layer  Controller';
	var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	

	var emptyLayer = {
		"layerName": "",
		"layerType": "ARCGIS_DYNAMIC_LAYER",
		"opacity": 100,
		"url": "",
		"selectable": true,
		"editable": false,
		"isBaseLayer": false,
		"defaultVisibility": true,
		"source": null,
		"maxZoom": 32,
		"minZoom": 2,
		"maxResolution": 0,
		"minResolution": 0,
		"zIndex": 0,
		"style": null,
		"styleSelect": null
	}




	// CONTROLLER PRIVATE FUNCTIONS	



	var navigateUrl = function(url) { window.location.href = url; }

	var deleteLayer = function(id) {
		console.log('deleteConfirmation() -> formId: ' + id);

		// no Id no fun!
		if (!id) { toastr.error('NO ELEMENT SELECTED!', ''); return false; }


		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");

		HeaderController.showConfirmDialogMapsGeneric('delete_mapslayer_form');

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

	var showHideByLayerType = function() {
		var lT = $("#layerType").val();

		loadLayersStyles();
		
		$('#divSelectable').hide();
		if ((lT === 'WFS_LAYER' || lT === 'WMTS_LAYER' || lT === 'WMS_LAYER' || lT === 'GEOJSON_LAYER') && !getCheck('isBaseLayer')) {
			$('#divSelectable').show();
		}
		$('#divEditable').hide();
		if (lT === 'WFS_LAYER' || lT === 'GEOJSON_LAYER' || lT === 'ARCGIS_FEATURE_LAYER') {
			$('#divEditable').show();
		}
		$('#divFilter').hide();
		if (lT === 'WFS_LAYER' || lT === 'WMTS_LAYER' || lT === 'WMS_LAYER' || lT === 'ARCGIS_FEATURE_LAYER' || lT === 'ARCGIS_TILE_LAYER' || lT === 'ARCGIS_DYNAMIC_LAYER') {
			$('#divFilter').show();
		}
		$('#divStyle').hide();
		if (lT === 'GEOJSON_LAYER') {
			$('#divStyle').show();
		}
		//sources
		$('#rowEntitySource').hide();
		$('#rowWSource').hide();
		if (lT === 'GEOJSON_LAYER') {
			$('#rowEntitySource').show();
		}
		$('#divwsourceparamsversionwfs').hide();
		$('#divwsourceparamsversionwmts').hide();
		$('#divwsourceparamsversionwms').hide();
		if (lT === 'WFS_LAYER' || lT === 'WMTS_LAYER' || lT === 'WMS_LAYER') {
			$('#rowWSource').show();
			if (lT === 'WFS_LAYER') {
				$('#divwsourceparamsversionwfs').show();
				$('#divStyle').show();
			} else if (lT === 'WMTS_LAYER') {
				$('#divwsourceparamsversionwmts').show();
			} else {
				$('#divwsourceparamsversionwms').show();
			}
		}
		$('#rowArcGisSource').hide();
		$('#rowArcGisdSource').hide();
		$('#rowArcGistSource').hide();
		$('#rowArcGisfSource').hide();

		if (lT === 'ARCGIS_FEATURE_LAYER') {
			$('#rowArcGisfSource').show();
			$('#rowArcGisSource').show();
		}
		if (lT === 'ARCGIS_TILE_LAYER') {
			$('#rowArcGistSource').show();
			$('#rowArcGisSource').show();
		}
		if (lT === 'ARCGIS_DYNAMIC_LAYER') {
			$('#rowArcGisdSource').show();
			$('#rowArcGisSource').show();
		}
		$('#divclusterOptions').hide();
		if (lT === 'WFS_LAYER' || lT === 'ARCGIS_FEATURE_LAYER' || lT === 'GEOJSON_LAYER') {
			$('#divclusterOptions').show();
		}
		$('#divFilterbox').hide();
		if ($('#server').val() === 'geoserver') {
			$('#divFilterbox').show();
		}

	}


	//set form fields from param config 
	var initFields = function() {

		if (config == null) {
			config = emptyLayer;
		}
		$("#layerName").val(config.layerName).change();
		$("#headernameupdate").html(config.layerName);
		$( "#layerName" ).on( "change", function() {
			 $("#headernameupdate").html($( "#layerName" ).val());
			} );

		$("#layerType").val(config.layerType).change();
		$("#defaultVisibility").val(config.defaultVisibility).change();
		if (config.defaultVisibility) {
			$("#defaultVisibility").prop('checked', true);
		}
		$("#opacity").val(config.opacity).change();
		$("#isBaseLayer").val(config.isBaseLayer).change();
		if (config.isBaseLayer) {
			$("#isBaseLayer").prop('checked', true);
		}


		var minZoom = config.minZoom == null ? 2 : config.minZoom;
		var maxZoom = config.maxZoom == null ? 32 : config.maxZoom;
		//zoom
		$("#zoomslider").slider({
			range: true,
			min: 2,
			max: 32,
			values: [minZoom, maxZoom],
			slide: function(event, ui) {
				$("#zoom").val(ui.values[0] + " - " + ui.values[1]);
			}
		});
		$("#zoom").val($("#zoomslider").slider("values", 0) +
			" - " + $("#zoomslider").slider("values", 1));

		
		var minResolution = config.minResolution == null ? 2 : config.minResolution;
		var maxResolution = config.maxResolution == null ? 32 : config.maxResolution;
		//resolution
		$("#resolutionslider").slider({
			range: true,
			min: 1,
			max: 200000,
			values: [minResolution, maxResolution],
			slide: function(event, ui) {
				$("#resolution").val(ui.values[0] + " - " + ui.values[1]);
			}
		});
		 
		$('#useResolution').on('change',function () {
			
			if( getCheck('useResolution')){				
				$("#resolutionslider").slider("enable")
			}else{
				$("#resolutionslider").slider("disable")
			}
		})
		
		if(!config.minResolution   && !config.maxResolution){
			$("#useResolution").prop('checked', false);			
			$("#useResolution").val(false).change();
		}else{
			$("#useResolution").prop('checked', true);
			$("#useResolution").val(true).change();
		}
		
		
		$("#resolution").val($("#resolutionslider").slider("values", 0) +
			" - " + $("#resolutionslider").slider("values", 1));

		//mew fields
		$("#styleSelect").val(config.styleSelect).change();
		$("#filterbox").val(config.filter_box).change();
		$("#textVisibility").val(config.textVisibility).change();
		if (config.textVisibility) {
			$("#textVisibility").prop('checked', true);
		}
		$("#server").val(config.server).change();


		if (config.layerType == 'ARCGIS_DYNAMIC_LAYER') {
			$("#filter").val(config.filter).change();
			if (typeof config.source != 'undefined' && config.source != null) {
				$("#arcgisdsourceurl").val(config.source.url).change();
				$("#arcgissourcesrs").val(config.source.srs).change();
				$("#arcgissourceprojection").val(config.source.projection).change();

				if (typeof config.source.params != 'undefined' && config.source.params != null) {
					$("#arcgisdsourceparamslayers").val(config.source.params.layers).change();
				}
			}


		} else if (config.layerType == 'ARCGIS_FEATURE_LAYER') {
			$("#filter").val(config.filter).change();
			if (typeof config.source != 'undefined' && config.source != null) {
				$("#arcgisfsourceurl").val(config.source.url).change();
				$("#arcgisfsourcelayer").val(config.source.layer).change();
				$("#arcgissourcesrs").val(config.source.srs).change();
				$("#arcgissourceprojection").val(config.source.projection).change();
			}

		} else if (config.layerType == 'ARCGIS_TILE_LAYER') {
			$("#filter").val(config.filter).change();
			if (typeof config.source != 'undefined' && config.source != null) {
				$("#arcgistsourceurl").val(config.source.url).change();
				$("#arcgissourcesrs").val(config.source.srs).change();
				$("#arcgissourceprojection").val(config.source.projection).change();
			}

		} else if (config.layerType == 'GEOJSON_LAYER') {
			$("#selectable").val(config.selectable).change();
			if (config.selectable) {
				$("#selectable").prop('checked', true);
			}
			$("#editable").val(config.editable).change();
			if (config.editable) {
				$("#editable").prop('checked', true);
			}
			$("#style").val(config.style).change();
			if (typeof config.source != 'undefined' && config.source != null) {
				$("#entitysourcelayer").val(config.source.layer).change();
				$("#entitysourcedataProjection").val(config.source.dataProjection).change();
				$("#entitysourcesrs").val(config.source.srs).change();
				$("#entitysourceprojection").val(config.source.projection).change();


			}
		} else if (config.layerType == 'WFS_LAYER') {
			$("#filter").val(config.filter).change();
			$("#selectable").val(config.selectable).change();
			if (config.selectable) {
				$("#selectable").prop('checked', true);
			}
			$("#editable").val(config.editable).change();
			if (config.editable) {
				$("#editable").prop('checked', true);
			}

			if (typeof config.source != 'undefined' && config.source != null) {
				$("#wsourceurl").val(config.source.url).change();
				$("#wsourcedataProjection").val(config.source.dataProjection).change();
				$("#wsourcesrs").val(config.source.srs).change();
				$("#wsourceprojection").val(config.source.projection).change();
				//configResponse.source.layer = get('wfsSelectLayer');
					$("#wsourceparamsversionwfs").val(config.source.params.version).change();
					$("#wsourceparamslayers").val(config.source.params.layers).change();

			}

		} else if (config.layerType == 'WMS_LAYER') {
			$("#filter").val(config.filter).change();
			$("#selectable").val(config.selectable).change();
			if (config.selectable) {
				$("#selectable").prop('checked', true);
			}
			if (typeof config.source != 'undefined' && config.source != null) {
				$("#wsourceurl").val(config.source.url).change();
				$("#wsourcedataProjection").val(config.source.dataProjection).change();
				$("#wsourcesrs").val(config.source.srs).change();
				$("#wsourceprojection").val(config.source.projection).change();
				if (typeof config.source.params != 'undefined' && config.source.params != null) {
					$("#wsourceparamsversionwms").val(config.source.params.version).change();
				}
			}

		} else if (config.layerType == 'WMTS_LAYER') {
			$("#filter").val(config.filter).change();
			$("#selectable").val(config.selectable).change();
			if (config.selectable) {
				$("#selectable").prop('checked', true);
			}
			if (typeof config.source != 'undefined' && config.source != null) {
				$("#wsourceurl").val(config.source.url).change();
				$("#wsourcedataProjection").val(config.source.dataProjection).change();
				$("#wsourcesrs").val(config.source.srs).change();
				$("#wsourceprojection").val(config.source.projection).change();
				
				//config.source.layer = get('wfsSelectLayer');
				//config.source.style = get('wfsSelectStyle');
				 
					$("#wsourceparamsversionwmts").val(config.source.params.version).change();
				
			}

		}



		/*else if (config.layerType == 'OSM') {

		}*/

	}

	var toNum = function(val) {
		if (val == null || (typeof val === 'String' && val.trim() == "")) {
			return null;
		} else {
			return Number(val);
		}

	}


	var get = function(id) {
		return $("#" + id).val();
	}
	var getCheck = function(id) {
		return $("#" + id).is(":checked");
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
		configResponse.id = get('identification');
		configResponse.layerName = get('layerName');
		configResponse.layerType = get('layerType');
		configResponse.defaultVisibility = getCheck('defaultVisibility');
		configResponse.opacity = toNum(get('opacity'));
		configResponse.isBaseLayer = getCheck('isBaseLayer');
		configResponse.minZoom = toNum($("#zoomslider").slider("values", 0));
		configResponse.maxZoom = toNum($("#zoomslider").slider("values", 1));
		if( getCheck('useResolution')){		
			configResponse.minResolution = toNum($("#resolutionslider").slider("values", 0));
			configResponse.maxResolution = toNum($("#resolutionslider").slider("values", 1));
		}else{
			configResponse.minResolution = undefined;
			configResponse.maxResolution = undefined;
		}
		configResponse.styleSelect = get('styleSelect');
		configResponse.filter_box = get('filterbox');
		configResponse.textVisibility = getCheck('textVisibility');
		configResponse.private = getCheck('private');
		configResponse.server = get('server');

		if (configResponse.layerType == 'ARCGIS_DYNAMIC_LAYER') {
			configResponse.filter = get('filter');
			if (get('arcgisdsourceurl') != null || get('arcgisdsourceparamslayers') != null) {
				configResponse.source = { 'url': get('arcgisdsourceurl'), 'params': { 'layers': get('arcgisdsourceparamslayers') } };
			}

		} else if (configResponse.layerType == 'ARCGIS_FEATURE_LAYER') {
			configResponse.filter = get('filter');
			if (get('arcgisfsourceurl') != null || get('arcgisfsourcelayer') != null) {
				configResponse.source = { 'url': get('arcgisfsourceurl'), 'layer': get('arcgisfsourcelayer') };
			}
			configResponse.clusterOptions = { 'isCluster': getCheck('clusterOptionsisCluster'), 'distance': get('clusterOptionsdistance') };

		} else if (configResponse.layerType == 'ARCGIS_TILE_LAYER') {
			configResponse.filter = get('filter');
			if (get('arcgistsourceurl') != null) {
				configResponse.source = { 'url': get('arcgistsourceurl') };
			}
		} else if (configResponse.layerType == 'GEOJSON_LAYER') {
			configResponse.selectable = getCheck('selectable');
			configResponse.editable = getCheck('editable');
			configResponse.style = get('style');
			//catch host from window.location.origin for to create the url
			configResponse.source = { 'url': window.location.origin + '/controlpanel/layer/getLayerData?' };
			configResponse.source.layer = get('entitysourcelayer');
			configResponse.source.dataProjection = get('entitysourcedataProjection');
			configResponse.source.srs = get('entitysourcesrs')
			configResponse.source.projection = get('entitysourceprojection');
			configResponse.clusterOptions = { 'isCluster': getCheck('clusterOptionsisCluster'), 'distance': get('clusterOptionsdistance') };

		} else if (configResponse.layerType == 'WFS_LAYER') {
			configResponse.filter = get('filter');
			configResponse.selectable = getCheck('selectable');
			configResponse.editable = getCheck('editable');
			if (get('wsourceurl') != null || get('wsourcedataProjection') != null || get('wsourceparamsversionwfs') != null || get('wfsSelectLayer') != null || get('style') != null) {
				configResponse.source = { 'url': get('wsourceurl'), 'dataProjection': get('wsourcedataProjection') }
				configResponse.source.srs = get('wsourcesrs');
				configResponse.source.layer = get('wfsSelectLayer');
				configResponse.source.version = get('wsourceparamsversionwfs');
				configResponse.source.outputFormat = "json";
				configResponse.style = get('style');
				configResponse.source.projection = get('wsourceprojection');
				 
			}

			configResponse.clusterOptions = { 'isCluster': getCheck('clusterOptionsisCluster'), 'distance': get('clusterOptionsdistance') };


		} else if (configResponse.layerType == 'WMS_LAYER') {
			configResponse.filter = get('filter');
			configResponse.selectable = getCheck('selectable');
			if (get('wsourceurl') != null || get('wsourcedataProjection') != null || get('wsourceparamsversionwms') != null ) {
				configResponse.source = { 'url': get('wsourceurl'), 'dataProjection': get('wsourcedataProjection') }
				configResponse.source.srs = get('wsourcesrs');
				configResponse.source.projection = get('wsourceprojection')
				configResponse.source.params = { 'version': get('wsourceparamsversionwms'), 'layers': loadwmtsSelected(), 'styles': loadwmtsStyleSelected() }
				 
			}

		} else if (configResponse.layerType == 'WMTS_LAYER') {
			configResponse.filter = get('filter');
			configResponse.selectable = getCheck('selectable');
			if (get('wsourceurl') != null || get('wsourcedataProjection') != null || get('wsourceparamsversionwmts') != null || get('wfsSelectLayer') != null || get('wfsSelectStyle') != null) {
				configResponse.source = { 'url': get('wsourceurl'), 'dataProjection': get('wsourcedataProjection') }
				configResponse.source.srs = get('wsourcesrs');
				configResponse.source.layer = get('wfsSelectLayer');
				configResponse.source.style = get('wfsSelectStyle');
				configResponse.source.projection = get('wsourceprojection')
				configResponse.source.version = get('wsourceparamsversionwmts');
				 
			}

		}


		return JSON.stringify(configResponse);
	}



	var createConfig = function() {
		var conf = {
			'id': get('identification'),
			'layerName': get('layerName'),
			'layerType': get('layerType'),
			'defaultVisibility': getCheck('defaultVisibility'),
			'opacity': toNum(get('opacity')),
			'isBaseLayer': getCheck('isBaseLayer'),
			'minZoom': toNum($("#zoomslider").slider("values", 0)),
			'maxZoom': toNum($("#zoomslider").slider("values", 1))
			}
			
		if( getCheck('useResolution')){		
			conf.minResolution = toNum($("#resolutionslider").slider("values", 0));
			conf.maxResolution = toNum($("#resolutionslider").slider("values", 1));
		}else{
			conf.minResolution = undefined;
			conf.maxResolution = undefined;
		}
		
		conf.styleSelect = get('styleSelect');
		conf.filter_box = get('filterbox');
		conf.textVisibility = getCheck('textVisibility');
		conf.private = getCheck('private');
		conf.server = get('server');

		if (conf.layerType == 'ARCGIS_DYNAMIC_LAYER') {
			conf.filter = get('filter');
			if (get('arcgisdsourceurl') != null || get('arcgisdsourceparamslayers') != null) {
				conf.source = { 'url': get('arcgisdsourceurl'), 'params': { 'layers': get('arcgisdsourceparamslayers') } };
			}

		} else if (conf.layerType == 'ARCGIS_FEATURE_LAYER') {
			conf.filter = get('filter');
			if (get('arcgisfsourceurl') != null || get('arcgisfsourcelayer') != null) {
				conf.source = { 'url': get('arcgisfsourceurl'), 'layer': get('arcgisfsourcelayer') };
			}
			conf.clusterOptions = { 'isCluster': getCheck('clusterOptionsisCluster'), 'distance': get('clusterOptionsdistance') };

		} else if (conf.layerType == 'ARCGIS_TILE_LAYER') {
			conf.filter = get('filter');
			if (get('arcgistsourceurl') != null) {
				conf.source = { 'url': get('arcgistsourceurl') };
			}
		} else if (conf.layerType == 'GEOJSON_LAYER') {
			conf.selectable = getCheck('selectable');
			conf.editable = getCheck('editable');
			conf.style = get('style');
			//catch host from window.location.origin for to create the url
			conf.source = { 'url': window.location.origin + '/controlpanel/layer/getLayerData?' };
			conf.source.layer = get('entitysourcelayer');
			conf.source.dataProjection = get('entitysourcedataProjection');

			conf.source.srs = get('entitysourcesrs')
			conf.source.projection = get('entitysourceprojection');

			conf.clusterOptions = { 'isCluster': getCheck('clusterOptionsisCluster'), 'distance': get('clusterOptionsdistance') };

		} else if (conf.layerType == 'WFS_LAYER') {
			conf.filter = get('filter');
			conf.selectable = getCheck('selectable');
			conf.editable = getCheck('editable');
			if (get('wsourceurl') != null || get('wsourcedataProjection') != null || get('wsourceparamsversionwfs') != null || get('wsourceparamslayers') != null || get('wsourceparamsstyles') != null) {
				conf.source = { 'url': get('wsourceurl'), 'dataProjection': get('wsourcedataProjection') }
				conf.source.srs = get('wsourcesrs');
				conf.source.projection = get('wsourceprojection')
				if (get('wsourceparamsversionwfs') != null || get('wsourceparamslayers') != null || get('wsourceparamsstyles') != null) {
					conf.source.params = { 'version': get('wsourceparamsversionwfs'), 'layers': get('wsourceparamslayers'), 'styles': get('wsourceparamsstyles') }
				}
			}
			conf.clusterOptions = { 'isCluster': getCheck('clusterOptionsisCluster'), 'distance': get('clusterOptionsdistance') };

		} else if (conf.layerType == 'WMS_LAYER') {
			conf.filter = get('filter');
			conf.selectable = getCheck('selectable');
			if (get('wsourceurl') != null || get('wsourcedataProjection') != null || get('wsourceparamsversionwms') != null || get('wsourceparamslayers') != null || get('wsourceparamsstyles') != null) {
				conf.source = { 'url': get('wsourceurl'), 'dataProjection': get('wsourcedataProjection') }
				conf.source.srs = get('wsourcesrs');
				conf.source.projection = get('wsourceprojection')
				conf.source.params = { 'version': get('wsourceparamsversionwms'), 'layers': loadwmtsSelected(), 'styles': loadwmtsStyleSelected()  }				 
			}

		} else if (conf.layerType == 'WMTS_LAYER') {
			conf.filter = get('filter');
			conf.selectable = getCheck('selectable');
			if (get('wsourceurl') != null || get('wsourcedataProjection') != null || get('wsourceparamsversionwmts') != null || get('wfsSelectLayer') != null || get('wfsSelectStyle') != null) {
				conf.source = { 'url': get('wsourceurl'), 'dataProjection': get('wsourcedataProjection') }
				conf.source.srs = get('wsourcesrs');
				conf.source.projection = get('wsourceprojection')
				conf.source.layer = get('wfsSelectLayer');
				conf.source.style = get('wfsSelectStyle');
				conf.source.version = get('wsourceparamsversionwmts');
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
		showHideByLayerType();

	}
	var freeResource = function(id, url) {
		console.log('freeResource() -> id: ' + id);
		$.get("/controlpanel/mapslayer/freeResource/" + id).done(
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
		deleteLayer: function(id) {
			deleteLayer(id);
		}, showHideByLayerType: function() {
			showHideByLayerType();
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	MapsLayerController.load(elementJson);

	// AUTO INIT CONTROLLER.
	MapsLayerController.init();
});
