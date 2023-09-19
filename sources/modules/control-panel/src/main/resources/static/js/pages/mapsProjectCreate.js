var MapsProjectController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS. 

	var LIB_TITLE = 'Maps Project  Controller';
	var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	

	var emptyProject = {
		"mapConfig": {
			"mainMapOptions": {
				"mainDivMapId": "mainMapDiv",
				"maxNumberMaps": 1,
				"displayGrid": true,
				"projections": []
			}, "exportConfigOptions": {
				"pdfExportOptions": {
					"orientation": "l",
					"pageSize": 4,
					"imageMargin": [
						10,
						10,
						23,
						10
					],
					"resolution": 150,
					"logoUrl": "data:image/png;base64,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",
					"logoYPosition": 2,
					"logoWidth": 23,
					"logoHeight": 20,
					"imageFormat": "JPEG",
					"titleFont": "Helvetica",
					"titleWeight": "normal",
					"titleFontSize": 22,
					"titleXPosition": 10,
					"titleYPosition": 15,
					"maxTitleLength": 20,
					"footerFont": "Helvetica",
					"footerWeight": "normal",
					"footerFontSize": 12
				},
				"imageExportOptions": {
					"pageSize": 4,
					"imageFormat": "JPEG",
					"resolution": 150
				}
			},
			"loggerOptions": {
				"levelDefault": [
					"ERROR",
					"WARN",
					"INFO",
					"DEBUG"
				],
				"eventOptions": [
					"WARN",
					"INFO"
				],
				"consoleOptions": [
					"DEBUG",
					"WARN",
					"ERROR",
					"INFO"
				],
				"publishedDefault": true
			}, "toolsOptions": {
				"editionOptions": {
					"modifyStyle": {
						"fill": {
							"color": "rgba(255, 255, 153, 0.7)"
						},
						"stroke": {
							"color": "rgba(255, 255, 0, 1)",
							"lineDash": [
								10,
								10
							],
							"width": 3
						}
					},
					"addStyle": {
						"fill": {
							"color": "rgba(0, 255, 0, 0.7)"
						},
						"stroke": {
							"color": "rgba(0, 153, 51, 1)",
							"lineDash": [
								10,
								10
							],
							"width": 3
						}
					},
					"deleteStyle": {
						"fill": {
							"color": "rgba(255, 102, 153, 0.7)"
						},
						"stroke": {
							"color": "rgba(204, 0, 102, 1)",
							"lineDash": [
								10,
								10
							],
							"width": 3
						}
					},
					"inline": false,
					"zoomDuration": 300,
					"tolerance": 10
				},
				"showCoordToolOptions": {
					"style": {
						"circleVertex": false,
						"fill": {
							"color": "rgba(247, 172, 111, 0.7)"
						},
						"stroke": {
							"color": "rgba(26, 59, 71, 1)",
							"width": 2
						},
						"text": {
							"defaultText": "",
							"resolution": 255
						}
					},
					"inline": true
				},
				"measureToolOptions": {
					"style": {
						"labelVisibility": true,
						"circleVertex": true,
						"fill": {
							"color": "rgba(231, 84, 128, 0.7)"
						},
						"stroke": {
							"color": "rgba(231, 84, 128, 1)",
							"lineDash": [
								10,
								10
							],
							"width": 2
						},
						"text": {
							"resolution": 255
						}
					},
					"inline": true,
					"zoomDuration": 300
				},
				"infoToolOptions": {
					"style": {
						"circleVertex": false,
						"radius": 6,
						"fill": {
							"color": "rgba(121,197,180, 0.7)"
						},
						"stroke": {
							"color": "rgba(120,116,180, 1)",
							"width": 3
						}
					},
					"inline": true,
					"zoomDuration": 300,
					"tolerance": 5
				},
				"gotoToolOptions": {
					"style": {
						"circleVertex": false,
						"fill": {
							"color": "rgba(247, 172, 111, 0.7)"
						},
						"stroke": {
							"color": "rgba(26, 59, 71, 1)",
							"width": 2
						},
						"text": {
							"defaultText": "",
							"resolution": 255
						}
					},
					"inline": true,
					"zoomDuration": 300
				},
				"selectByAttrToolOptions": {
					"style": {
						"circleVertex": false,
						"radius": 6,
						"fill": {
							"color": "rgba(247, 172,111, 0.7)"
						},
						"stroke": {
							"color": "rgba(0, 255, 255, 1)",
							"width": 3
						}
					},
					"inline": true,
					"zoomDuration": 300
				},
				"selectByGeomToolOptions": {
					"style": {
						"circleVertex": false,
						"radius": 6,
						"fill": {
							"color": "rgba(255, 234,128, 0.5)"
						},
						"stroke": {
							"color": "rgba(255, 234, 128, 1)",
							"width": 3
						}
					},
					"inline": true,
					"zoomDuration": 300
				},
				"bufferToolOptions": {
					"style": {
						"circleVertex": false,
						"radius": 6,
						"fill": {
							"color": "rgba(232, 138, 162, 0.7)"
						},
						"stroke": {
							"color": "rgba(121, 197, 180, 1)",
							"width": 3
						}
					},
					"zoomDuration": 300,
					"inline": true,
					"processLimitMiliSeconds": 10000
				},
				"intersectToolOptions": {
					"style": {
						"circleVertex": false,
						"radius": 6,
						"fill": {
							"color": "rgba(121,197,180, 0.7)"
						},
						"stroke": {
							"color": "rgba(120,116,180, 1)",
							"width": 3
						}
					},
					"zoomDuration": 300,
					"inline": true,
					"processLimitMiliSeconds": 10000
				},
				"proximityToolOptions": {
					"style": {
						"circleVertex": false,
						"radius": 6,
						"fill": {
							"color": "rgba(121, 197, 180, 0.7)"
						},
						"stroke": {
							"color": "rgba(99, 159, 203, 1)",
							"width": 3
						}
					},
					"zoomDuration": 300,
					"inline": true,
					"processLimitMiliSeconds": 10000
				},
				"geocoderToolOptions": {
					"provider": "esri",					
					"authMode": "apikey",
					"style": null,
					"styleSelect": null,
					"inline": true,
					"zoomDuration": 300,
					"search": {
					  "city": null,
					  "county": null,
					  "neighborhood": null,
					  "postalCode": null,
					  "postalExt": null,
					  "region": null,
					  "state": null,
					  "subregion": null,
					  "country": [],
						},
						"options": {
				          "token": "",
				          "mode": "direct",
				          "structured": "structured",
				          "categories": ["Address", "Postal"],
				          "countryCode": ["es", "pt"],
				          "langCode": "ja",	
				          "location":"",			           
				          "locationType": null,
				          "numberCandidates": 15,				        
				          "searchZoom": 16,
				          "sourceCountry": ["es"],
				          "scoreLimit": 0,
				          "searchExtent": []
				        }
					
					 
					
				},
				"routingToolOptions": {
					"provider": "OpenRoute Service",
					"language": "en",
					"token": "",
					"stopStyle": {
						"circleVertex": false,
						"radius": 15,
						"fill": {
							"color": "rgba(39, 245, 75, 0.9)"
						},
						"stroke": {
							"color": "rgba(26, 149, 46, 0.9)",
							"width": 2
						},
						"text": {
							"defaultText": "",
							"backgroundFill": "rgba(39, 245, 75, 0.9)"
						}
					},
					"blockStyle": {
						"circleVertex": false,
						"radius": 10,
						"fill": {
							"color": "rgba(255, 41, 41, 0.9)"
						},
						"stroke": {
							"color": "rgba(255, 0, 0, 1)",
							"width": 3
						}
					},
					"routeStyle": {
						"stroke": {
							"color": "rgba(106, 90, 205, 0.9)",
							"width": 4
						}
					},
					"style": {
						"circleVertex": false,
						"radius": 15,
						"fill": {
							"color": "rgba(39, 245, 75, 0.9)"
						},
						"stroke": {
							"color": "rgba(26, 149, 46, 0.9)",
							"width": 4
						},
						"text": {
							"defaultText": "",
							"backgroundFill": "rgba(39, 245, 75, 0.9)"
						}
					},
					"inline": true
				},
				"bookmarkToolOptions": {
					"zoomDuration": 200,
					"maxOperations": 10
				},
				"bookmarkToolOptions": {
					"onlyActiveMap": false,
					"maxNumberBookmark": 10,
					"zoomDuration": 200
				}
			},
			"mapOptions": []
		}
	}




	// CONTROLLER PRIVATE FUNCTIONS	



	var navigateUrl = function(url) { window.location.href = url; }

	/*var deleteProject = function(id) {
		console.log('deleteConfirmation() -> formId: ' + id);

		// no Id no fun!
		if (!id) { toastr.error('NO ELEMENT SELECTED!', ''); return false; }


		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");

		HeaderController.showConfirmDialogMapsGeneric('delete_mapsproject_form');

	}*/


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
					$('#public_aux').val(getCheck('checkboxPublic'));

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

		if (c == null) {
			c = emptyProject;
		}
		if(!c.mapConfig){
			return ;
		}
		$("#maxNumberMaps").val(c.mapConfig.mainMapOptions.maxNumberMaps).change();
		$("#displayGrid").val(c.mapConfig.mainMapOptions.displayGrid).change();
		if (c.mapConfig.mainMapOptions.displayGrid) {
			$("#displayGrid").prop('checked', true);
		}
		 
		if (c.mapConfig.mainMapOptions.projections!=null && typeof c.mapConfig.mainMapOptions.projections=='object' && Array.isArray(c.mapConfig.mainMapOptions.projections)) {
		for(var j = 0 ;j< c.mapConfig.mainMapOptions.projections.length;j++){
			addProjectionRow(c.mapConfig.mainMapOptions.projections[j]);
		}
		projectionList=c.mapConfig.mainMapOptions.projections;
		 }
		if (c.mapConfig.mapOptions!=null && typeof c.mapConfig.mapOptions=='object' && Array.isArray(c.mapConfig.mapOptions)) {
		for(var i = 0 ;i< c.mapConfig.mapOptions.length;i++){
			loadMapRow(c.mapConfig.mapOptions[i]);
		}
		mapsList=c.mapConfig.mapOptions;
	 }
		Languages.createOptionsSelectPicker('toolsOptionsroutingToolOptionslanguage');
		
		
		if(c.mapConfig.exportConfigOptions){
			if(c.mapConfig.exportConfigOptions.pdfExportOptions){				
				$("#exportConfigOptionspdfExportOptionsorientation").val(c.mapConfig.exportConfigOptions.pdfExportOptions.orientation).change();
				$("#exportConfigOptionspdfExportOptionspageSize").val(c.mapConfig.exportConfigOptions.pdfExportOptions.pageSize).change();
				if (typeof c.mapConfig.exportConfigOptions.pdfExportOptions.imageMargin != 'undefined' && c.mapConfig.exportConfigOptions.pdfExportOptions.imageMargin != null && c.mapConfig.exportConfigOptions.pdfExportOptions.imageMargin.length > 0) {
					$("#exportConfigOptionspdfExportOptionspageimageMargin0").val(c.mapConfig.exportConfigOptions.pdfExportOptions.imageMargin[0]).change();
					$("#exportConfigOptionspdfExportOptionspageimageMargin1").val(c.mapConfig.exportConfigOptions.pdfExportOptions.imageMargin[1]).change();
					$("#exportConfigOptionspdfExportOptionspageimageMargin2").val(c.mapConfig.exportConfigOptions.pdfExportOptions.imageMargin[2]).change();
					$("#exportConfigOptionspdfExportOptionspageimageMargin3").val(c.mapConfig.exportConfigOptions.pdfExportOptions.imageMargin[3]).change();
				}
				$("#exportConfigOptionspdfExportOptionsresolution").val(c.mapConfig.exportConfigOptions.pdfExportOptions.resolution).change();
				$("#exportConfigOptionspdfExportOptionslogoUrl").val(c.mapConfig.exportConfigOptions.pdfExportOptions.logoUrl).change();
				$("#exportConfigOptionspdfExportOptionslogoYPosition").val(c.mapConfig.exportConfigOptions.pdfExportOptions.logoYPosition).change();
				$("#exportConfigOptionspdfExportOptionslogoWidth").val(c.mapConfig.exportConfigOptions.pdfExportOptions.logoWidth).change();
				$("#exportConfigOptionspdfExportOptionslogoHeight").val(c.mapConfig.exportConfigOptions.pdfExportOptions.logoHeight).change();
				$("#exportConfigOptionspdfExportOptionsimageFormat").val(c.mapConfig.exportConfigOptions.pdfExportOptions.imageFormat).change();
				//$("#exportConfigOptionspdfExportOptionstitleFont").val(c.mapConfig.exportConfigOptions.pdfExportOptions.titleFont).change();
				//$("#exportConfigOptionspdfExportOptionstitleWeight").val(c.mapConfig.exportConfigOptions.pdfExportOptions.titleWeight).change();
				$("#exportConfigOptionspdfExportOptionstitleFontSize").val(c.mapConfig.exportConfigOptions.pdfExportOptions.titleFontSize).change();
				$("#exportConfigOptionspdfExportOptionstitleXPosition").val(c.mapConfig.exportConfigOptions.pdfExportOptions.titleXPosition).change();
				$("#exportConfigOptionspdfExportOptionstitleYPosition").val(c.mapConfig.exportConfigOptions.pdfExportOptions.titleYPosition).change();
				$("#exportConfigOptionspdfExportOptionsmaxTitleLength").val(c.mapConfig.exportConfigOptions.pdfExportOptions.maxTitleLength).change();
				//$("#exportConfigOptionspdfExportOptionsfooterFont").val(c.mapConfig.exportConfigOptions.pdfExportOptions.footerFont).change();
				//$("#exportConfigOptionspdfExportOptionsfooterWeight").val(c.mapConfig.exportConfigOptions.pdfExportOptions.footerWeight).change();
				$("#exportConfigOptionspdfExportOptionsfooterFontSize").val(c.mapConfig.exportConfigOptions.pdfExportOptions.footerFontSize).change();
			}
			if(c.mapConfig.exportConfigOptions.imageExportOptions){
				$("#exportConfigOptionsimageExportOptionspageSize").val(c.mapConfig.exportConfigOptions.imageExportOptions.pageSize).change();
				$("#exportConfigOptionsimageExportOptionsimageFormat").val(c.mapConfig.exportConfigOptions.imageExportOptions.imageFormat).change();
				$("#exportConfigOptionsimageExportOptionsresolution").val(c.mapConfig.exportConfigOptions.imageExportOptions.resolution).change();		
				$("#exportConfigOptionsimageExportOptionsoutputName").val(c.mapConfig.exportConfigOptions.imageExportOptions.outputName).change();	
			}
			}
			//if(c.mapConfig.loggerOptions){}
			if(c.mapConfig.toolsOptions ){
				if(c.mapConfig.toolsOptions.editionOptions){
					$("#toolsOptionseditionOptionsenable").val(c.mapConfig.toolsOptions.editionOptions.enable).change();
					if (c.mapConfig.toolsOptions.editionOptions.enable) {
						$("#toolsOptionseditionOptionsenable").prop('checked', true);
					}
					$("#toolsOptionseditionOptionsmodifyStyle").val(c.mapConfig.toolsOptions.editionOptions.modifyStyle).change();
					$("#toolsOptionseditionOptionsaddStyle").val(c.mapConfig.toolsOptions.editionOptions.addStyle).change();
					$("#toolsOptionseditionOptionsdeleteStyle").val(c.mapConfig.toolsOptions.editionOptions.deleteStyle).change();
					$("#toolsOptionseditionOptionsinline").val(c.mapConfig.toolsOptions.editionOptions.inline).change();
					if (c.mapConfig.toolsOptions.editionOptions.inline) {
						$("#toolsOptionseditionOptionsinline").prop('checked', true);
					}
					$("#toolsOptionseditionOptionszoomDuration").val(c.mapConfig.toolsOptions.editionOptions.zoomDuration).change();
					$("#toolsOptionseditionOptionstolerance").val(c.mapConfig.toolsOptions.editionOptions.tolerance).change();					
				}
				if(c.mapConfig.toolsOptions.showCoordToolOptions){
					$("#toolsOptionsshowCoordToolOptionsenable").val(c.mapConfig.toolsOptions.showCoordToolOptions.enable).change();
					if (c.mapConfig.toolsOptions.showCoordToolOptions.enable) {
						$("#toolsOptionsshowCoordToolOptionsenable").prop('checked', true);
					}					
					$("#toolsOptionsshowCoordToolOptionsstyle").val(c.mapConfig.toolsOptions.showCoordToolOptions.style).change();					
					$("#toolsOptionsshowCoordToolOptionsinline").val(c.mapConfig.toolsOptions.showCoordToolOptions.inline).change();
					if (c.mapConfig.toolsOptions.showCoordToolOptions.inline) {
						$("#toolsOptionsshowCoordToolOptionsinline").prop('checked', true);
					}					 
				}
				if(c.mapConfig.toolsOptions.measureToolOptions){
					$("#toolsOptionsmeasureToolOptionsenable").val(c.mapConfig.toolsOptions.measureToolOptions.enable).change();
					if (c.mapConfig.toolsOptions.measureToolOptions.enable) {
						$("#toolsOptionsmeasureToolOptionsenable").prop('checked', true);
					}					
					$("#toolsOptionsmeasureToolOptionsstyle").val(c.mapConfig.toolsOptions.measureToolOptions.style).change();					
					$("#toolsOptionsmeasureToolOptionsinline").val(c.mapConfig.toolsOptions.measureToolOptions.inline).change();
					if (c.mapConfig.toolsOptions.measureToolOptions.inline) {
						$("#toolsOptionsmeasureToolOptionsinline").prop('checked', true);
					}
					$("#toolsOptionsmeasureToolOptionszoomDuration").val(c.mapConfig.toolsOptions.measureToolOptions.zoomDuration).change();				
				}
				if(c.mapConfig.toolsOptions.infoToolOptions){
					$("#toolsOptionsinfoToolOptionsenable").val(c.mapConfig.toolsOptions.infoToolOptions.enable).change();
					if (c.mapConfig.toolsOptions.infoToolOptions.enable) {
						$("#toolsOptionsinfoToolOptionsenable").prop('checked', true);
					}					
					$("#toolsOptionsinfoToolOptionsstyle").val(c.mapConfig.toolsOptions.infoToolOptions.style).change();					
					$("#toolsOptionsinfoToolOptionsinline").val(c.mapConfig.toolsOptions.infoToolOptions.inline).change();
					if (c.mapConfig.toolsOptions.infoToolOptions.inline) {
						$("#toolsOptionsinfoToolOptionsinline").prop('checked', true);
					}
					$("#toolsOptionsinfoToolOptionszoomDuration").val(c.mapConfig.toolsOptions.infoToolOptions.zoomDuration).change();				
					$("#toolsOptionsinfoToolOptionstolerance").val(c.mapConfig.toolsOptions.infoToolOptions.tolerance).change();
				}
				}
				if(c.mapConfig.toolsOptions.gotoToolOptions){
					$("#toolsOptionsgotoToolOptionsenable").val(c.mapConfig.toolsOptions.gotoToolOptions.enable).change();
					if (c.mapConfig.toolsOptions.gotoToolOptions.enable) {
						$("#toolsOptionsgotoToolOptionsenable").prop('checked', true);
					}					
					$("#toolsOptionsgotoToolOptionsstyle").val(c.mapConfig.toolsOptions.gotoToolOptions.style).change();					
					$("#toolsOptionsgotoToolOptionsinline").val(c.mapConfig.toolsOptions.gotoToolOptions.inline).change();
					if (c.mapConfig.toolsOptions.gotoToolOptions.inline) {
						$("#toolsOptionsgotoToolOptionsinline").prop('checked', true);
					}
					$("#toolsOptionsgotoToolOptionszoomDuration").val(c.mapConfig.toolsOptions.gotoToolOptions.zoomDuration).change();				
				}
				if(c.mapConfig.toolsOptions.selectByAttrToolOptions){
					$("#toolsOptionsselectByAttrToolOptionsenable").val(c.mapConfig.toolsOptions.selectByAttrToolOptions.enable).change();
					if (c.mapConfig.toolsOptions.selectByAttrToolOptions.enable) {
						$("#toolsOptionsselectByAttrToolOptionsenable").prop('checked', true);
					}					
					$("#toolsOptionsselectByAttrToolOptionsstyle").val(c.mapConfig.toolsOptions.selectByAttrToolOptions.style).change();					
					$("#toolsOptionsselectByAttrToolOptionsinline").val(c.mapConfig.toolsOptions.selectByAttrToolOptions.inline).change();
					if (c.mapConfig.toolsOptions.selectByAttrToolOptions.inline) {
						$("#toolsOptionsselectByAttrToolOptionsinline").prop('checked', true);
					}
					$("#toolsOptionsselectByAttrToolOptionszoomDuration").val(c.mapConfig.toolsOptions.selectByAttrToolOptions.zoomDuration).change();				
				}
				if(c.mapConfig.toolsOptions.selectByGeomToolOptions){
					$("#toolsOptionsselectByGeomToolOptionsenable").val(c.mapConfig.toolsOptions.selectByGeomToolOptions.enable).change();
					if (c.mapConfig.toolsOptions.selectByGeomToolOptions.enable) {
						$("#toolsOptionsselectByGeomToolOptionsenable").prop('checked', true);
					}					
					$("#toolsOptionsselectByGeomToolOptionsstyle").val(c.mapConfig.toolsOptions.selectByGeomToolOptions.style).change();					
					$("#toolsOptionsselectByGeomToolOptionsinline").val(c.mapConfig.toolsOptions.selectByGeomToolOptions.inline).change();
					if (c.mapConfig.toolsOptions.selectByGeomToolOptions.inline) {
						$("#toolsOptionsselectByGeomToolOptionsinline").prop('checked', true);
					}
					$("#toolsOptionsselectByGeomToolOptionszoomDuration").val(c.mapConfig.toolsOptions.selectByGeomToolOptions.zoomDuration).change();				
				}
				if(c.mapConfig.toolsOptions.bufferToolOptions){
					$("#toolsOptionsbufferToolOptionsenable").val(c.mapConfig.toolsOptions.bufferToolOptions.enable).change();
					if (c.mapConfig.toolsOptions.bufferToolOptions.enable) {
						$("#toolsOptionsbufferToolOptionsenable").prop('checked', true);
					}					
					$("#toolsOptionsbufferToolOptionsstyle").val(c.mapConfig.toolsOptions.bufferToolOptions.style).change();					
					$("#toolsOptionsbufferToolOptionsinline").val(c.mapConfig.toolsOptions.bufferToolOptions.inline).change();
					if (c.mapConfig.toolsOptions.bufferToolOptions.inline) {
						$("#toolsOptionsbufferToolOptionsinline").prop('checked', true);
					}
					$("#toolsOptionsbufferToolOptionszoomDuration").val(c.mapConfig.toolsOptions.bufferToolOptions.zoomDuration).change();	
					$("#toolsOptionsbufferToolOptionsprocessLimitMiliSeconds").val(c.mapConfig.toolsOptions.bufferToolOptions.processLimitMiliSeconds).change();		
					
				}
				if(c.mapConfig.toolsOptions.intersectToolOptions){
					$("#toolsOptionsintersectToolOptionsenable").val(c.mapConfig.toolsOptions.intersectToolOptions.enable).change();
					if (c.mapConfig.toolsOptions.intersectToolOptions.enable) {
						$("#toolsOptionsintersectToolOptionsenable").prop('checked', true);
					}					
					$("#toolsOptionsintersectToolOptionsstyle").val(c.mapConfig.toolsOptions.intersectToolOptions.style).change();					
					$("#toolsOptionsintersectToolOptionsinline").val(c.mapConfig.toolsOptions.intersectToolOptions.inline).change();
					if (c.mapConfig.toolsOptions.intersectToolOptions.inline) {
						$("#toolsOptionsintersectToolOptionsinline").prop('checked', true);
					}
					$("#toolsOptionsintersectToolOptionszoomDuration").val(c.mapConfig.toolsOptions.intersectToolOptions.zoomDuration).change();	
					$("#toolsOptionsintersectToolOptionsprocessLimitMiliSeconds").val(c.mapConfig.toolsOptions.intersectToolOptions.processLimitMiliSeconds).change();		
					
				}				 
				if(c.mapConfig.toolsOptions.proximityToolOptions){
					$("#toolsOptionsproximityToolOptionsenable").val(c.mapConfig.toolsOptions.proximityToolOptions.enable).change();
					if (c.mapConfig.toolsOptions.proximityToolOptions.enable) {
						$("#toolsOptionsproximityToolOptionsenable").prop('checked', true);
					}					
					$("#toolsOptionsproximityToolOptionsstyle").val(c.mapConfig.toolsOptions.proximityToolOptions.style).change();					
					$("#toolsOptionsproximityToolOptionsinline").val(c.mapConfig.toolsOptions.proximityToolOptions.inline).change();
					if (c.mapConfig.toolsOptions.proximityToolOptions.inline) {
						$("#toolsOptionsproximityToolOptionsinline").prop('checked', true);
					}
					$("#toolsOptionsproximityToolOptionszoomDuration").val(c.mapConfig.toolsOptions.proximityToolOptions.zoomDuration).change();	
					$("#toolsOptionsproximityToolOptionsprocessLimitMiliSeconds").val(c.mapConfig.toolsOptions.proximityToolOptions.processLimitMiliSeconds).change();		
					
				}
				$('#toolsOptionsgeocoderToolOptionsprovider').on('change', function() {
					
					if($('#toolsOptionsgeocoderToolOptionsprovider').val()!=""){
						$('#toolsOptionsgeocoderToolOptionsauthMode').attr('disabled',true).selectpicker('refresh');					 
						if($('#toolsOptionsgeocoderToolOptionsprovider').val()=='nominatim'){
							$('#toolsOptionsgeocoderToolOptionsauthMode').val('none').change();
						}else{
							$('#toolsOptionsgeocoderToolOptionsauthMode').val('apikey').change();
						}
					 }else{
						 $('#toolsOptionsgeocoderToolOptionsauthMode').attr('disabled',false).selectpicker('refresh'); 					 
					}
				});
				
				if(c.mapConfig.toolsOptions.geocoderToolOptions){
					$("#toolsOptionsgeocoderToolOptionsenable").val(c.mapConfig.toolsOptions.geocoderToolOptions.enable).change();
					if (c.mapConfig.toolsOptions.geocoderToolOptions.enable) {
						$("#toolsOptionsgeocoderToolOptionsenable").prop('checked', true);
					}					
					$("#toolsOptionsgeocoderToolOptionsstyle").val(c.mapConfig.toolsOptions.geocoderToolOptions.style).change();					
					$("#toolsOptionsgeocoderToolOptionsinline").val(c.mapConfig.toolsOptions.geocoderToolOptions.inline).change();
					if (c.mapConfig.toolsOptions.geocoderToolOptions.inline) {
						$("#toolsOptionsgeocoderToolOptionsinline").prop('checked', true);
					}
					$("#toolsOptionsgeocoderToolOptionszoomDuration").val(c.mapConfig.toolsOptions.geocoderToolOptions.zoomDuration).change();								
					$("#toolsOptionsgeocoderToolOptionsprovider").val(c.mapConfig.toolsOptions.geocoderToolOptions.provider).change();
					$("#toolsOptionsgeocoderToolOptionsauthMode").val(c.mapConfig.toolsOptions.geocoderToolOptions.authMode).change();
				    $("#toolsOptionsgeocoderToolOptionsstyleSelect").val(c.mapConfig.toolsOptions.geocoderToolOptions.styleSelect).change();	
					if(c.mapConfig.toolsOptions.geocoderToolOptions.search){
						$("#toolsOptionsgeocoderToolOptionssearchcity").val(c.mapConfig.toolsOptions.geocoderToolOptions.search.city).change();	
						$("#toolsOptionsgeocoderToolOptionssearchcounty").val(c.mapConfig.toolsOptions.geocoderToolOptions.search.county).change();	
						$("#toolsOptionsgeocoderToolOptionssearchneighborhood").val(c.mapConfig.toolsOptions.geocoderToolOptions.search.neighborhood).change();	
						$("#toolsOptionsgeocoderToolOptionssearchpostalCode").val(c.mapConfig.toolsOptions.geocoderToolOptions.search.postalCode).change();
						$("#toolsOptionsgeocoderToolOptionssearchpostalExt").val(c.mapConfig.toolsOptions.geocoderToolOptions.search.postalExt).change();
						$("#toolsOptionsgeocoderToolOptionssearchregion").val(c.mapConfig.toolsOptions.geocoderToolOptions.search.region).change();	
					    $("#toolsOptionsgeocoderToolOptionssearchstate").val(c.mapConfig.toolsOptions.geocoderToolOptions.search.state).change();	
					    $("#toolsOptionsgeocoderToolOptionssearchsubregion").val(c.mapConfig.toolsOptions.geocoderToolOptions.search.subregion).change();	
					    $("#toolsOptionsgeocoderToolOptionssearchcountry").val(c.mapConfig.toolsOptions.geocoderToolOptions.search.country).change();	
						
					}
					if(c.mapConfig.toolsOptions.geocoderToolOptions.options){
						
						$("#toolsOptionsgeocoderToolOptionsoptionstoken").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.token).change();	
						$("#toolsOptionsgeocoderToolOptionsoptionsmode").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.mode).change();	
						$("#toolsOptionsgeocoderToolOptionsoptionsstructured").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.structured).change();	
						$("#toolsOptionsgeocoderToolOptionsoptionscategories").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.categories).change();	
						$("#toolsOptionsgeocoderToolOptionsoptionscountryCode").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.countryCode).change();	
						$("#toolsOptionsgeocoderToolOptionsoptionslangCode").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.langCode).change();	
						$("#toolsOptionsgeocoderToolOptionsoptionslocation").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.location).change();	
						$("#toolsOptionsgeocoderToolOptionsoptionslocationType").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.locationType).change();			
						$("#toolsOptionsgeocoderToolOptionsoptionsnumberCandidates").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.numberCandidates).change();	
						$("#toolsOptionsgeocoderToolOptionsoptionsscoreLimit").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.scoreLimit).change();	
						$("#toolsOptionsgeocoderToolOptionsoptionssearchExtent").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.searchExtent).change();	
						$("#toolsOptionsgeocoderToolOptionsoptionssearchZoom").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.searchZoom).change();	
						$("#toolsOptionsgeocoderToolOptionssourceCountry").val(c.mapConfig.toolsOptions.geocoderToolOptions.options.sourceCountry).change();						
					}					 
				}
				
				
				
				
				if(c.mapConfig.toolsOptions.routingToolOptions){
					$("#toolsOptionsroutingToolOptionsenable").val(c.mapConfig.toolsOptions.routingToolOptions.enable).change();
					if (c.mapConfig.toolsOptions.routingToolOptions.enable) {
						$("#toolsOptionsroutingToolOptionsenable").prop('checked', true);
					}					
					$("#toolsOptionsroutingToolOptionsprovider").val(c.mapConfig.toolsOptions.routingToolOptions.provider).change();
					$("#toolsOptionsroutingToolOptionslanguage").val(c.mapConfig.toolsOptions.routingToolOptions.language).change();
					$("#toolsOptionsroutingToolOptionstoken").val(c.mapConfig.toolsOptions.routingToolOptions.token).change();
					$("#toolsOptionsroutingToolOptionsstopStyle").val(c.mapConfig.toolsOptions.routingToolOptions.stopStyle).change();
					$("#toolsOptionsroutingToolOptionsblockStyle").val(c.mapConfig.toolsOptions.routingToolOptions.blockStyle).change();
					$("#toolsOptionsroutingToolOptionsrouteStyle").val(c.mapConfig.toolsOptions.routingToolOptions.routeStyle).change();
					
					
					$("#toolsOptionsroutingToolOptionsstyle").val(c.mapConfig.toolsOptions.routingToolOptions.style).change();					
					$("#toolsOptionsroutingToolOptionsinline").val(c.mapConfig.toolsOptions.routingToolOptions.inline).change();
					if (c.mapConfig.toolsOptions.routingToolOptions.inline) {
						$("#toolsOptionsroutingToolOptionsinline").prop('checked', true);
					}
					 
				}
				if(c.mapConfig.toolsOptions.historyExtentOptions){
					$("#toolsOptionshistoryExtentOptionsenable").val(c.mapConfig.toolsOptions.historyExtentOptions.enable).change();
					if (c.mapConfig.toolsOptions.historyExtentOptions.enable) {
						$("#toolsOptionshistoryExtentOptionsenable").prop('checked', true);
					}					
					 $("#toolsOptionshistoryExtentOptionszoomDuration").val(c.mapConfig.toolsOptions.historyExtentOptions.zoomDuration).change();
					 $("#toolsOptionshistoryExtentOptionsmaxOperations").val(c.mapConfig.toolsOptions.historyExtentOptions.maxOperations).change();
					 
				}
				if(c.mapConfig.toolsOptions.bookmarkToolOptions){
					$("#toolsOptionsbookmarkToolOptionsenable").val(c.mapConfig.toolsOptions.bookmarkToolOptions.enable).change();
					if (c.mapConfig.toolsOptions.bookmarkToolOptions.enable) {
						$("#toolsOptionsbookmarkToolOptionsenable").prop('checked', true);
					}					
					 $("#toolsOptionsbookmarkToolOptionszoomDuration").val(c.mapConfig.toolsOptions.bookmarkToolOptions.zoomDuration).change();
				 $("#toolsOptionsbookmarkToolOptionsmaxNumberBookmark").val(c.mapConfig.toolsOptions.bookmarkToolOptions.maxNumberBookmark).change();
					$("#toolsOptionsbookmarkToolOptionsonlyActiveMap").val(c.mapConfig.toolsOptions.bookmarkToolOptions.onlyActiveMap).change();
					if (c.mapConfig.toolsOptions.bookmarkToolOptions.onlyActiveMap) {
						$("#toolsOptionsbookmarkToolOptionsonlyActiveMap").prop('checked', true);
					}
					 
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

	var createConf = function() {
		//update
		if (cr != null) {
			c=cr;
			return updateConfig();

		} else {
			//create
			return updateConfig();
		}
	}
var updateConfig = function() {

	
	if(!c.mapConfig){
		c.mapConfig = {};
	}
	if(!c.mapConfig.mainMapOptions){
		c.mapConfig.mainMapOption = {};
	}
	c.mapConfig.mainMapOptions.maxNumberMaps=toNum(get('maxNumberMaps'));
	c.mapConfig.mainMapOptions.displayGrid=getCheck('displayGrid'); 
		 
		
		if(!c.mapConfig.exportConfigOptions){
			c.mapConfig.exportConfigOptions={'pdfExportOptions':{},'imageExportOptions':{}}
			}
			if(!c.mapConfig.exportConfigOptions.pdfExportOptions){
				c.mapConfig.exportConfigOptions.pdfExportOptions={};
			}
			if(!c.mapConfig.exportConfigOptions.imageExportOptions){
				c.mapConfig.exportConfigOptions.imageExportOptions={};
			}
			c.mapConfig.exportConfigOptions.pdfExportOptions.orientation=get('exportConfigOptionspdfExportOptionsorientation');
			c.mapConfig.exportConfigOptions.pdfExportOptions.pageSize=toNum(get('exportConfigOptionspdfExportOptionspageSize'));
		    c.mapConfig.exportConfigOptions.pdfExportOptions.imageMargin =valFourValues(get('exportConfigOptionspdfExportOptionspageimageMargin0','exportConfigOptionspdfExportOptionspageimageMargin1','exportConfigOptionspdfExportOptionspageimageMargin2','exportConfigOptionspdfExportOptionspageimageMargin3'))
			c.mapConfig.exportConfigOptions.pdfExportOptions.resolution=toNum(get('exportConfigOptionspdfExportOptionsresolution')); 
			c.mapConfig.exportConfigOptions.pdfExportOptions.logoUrl=get('exportConfigOptionspdfExportOptionslogoUrl'); 
			c.mapConfig.exportConfigOptions.pdfExportOptions.logoYPosition=toNum(get('exportConfigOptionspdfExportOptionslogoYPosition'));	
			c.mapConfig.exportConfigOptions.pdfExportOptions.logoWidth=toNum(get('exportConfigOptionspdfExportOptionslogoWidth'));
			c.mapConfig.exportConfigOptions.pdfExportOptions.logoHeight=toNum(get('exportConfigOptionspdfExportOptionslogoHeight'));
			c.mapConfig.exportConfigOptions.pdfExportOptions.imageFormat=get('exportConfigOptionspdfExportOptionsimageFormat');
			c.mapConfig.exportConfigOptions.pdfExportOptions.titleFontSize=toNum(get('exportConfigOptionspdfExportOptionstitleFontSize'));	
			c.mapConfig.exportConfigOptions.pdfExportOptions.titleYPosition=toNum(get('exportConfigOptionspdfExportOptionstitleYPosition'));	
			c.mapConfig.exportConfigOptions.pdfExportOptions.titleXPosition=toNum(get('exportConfigOptionspdfExportOptionstitleXPosition'));	
			c.mapConfig.exportConfigOptions.pdfExportOptions.maxTitleLength=toNum(get('exportConfigOptionspdfExportOptionsmaxTitleLength'));	
			c.mapConfig.exportConfigOptions.pdfExportOptions.footerFontSize=toNum(get('exportConfigOptionspdfExportOptionsfooterFontSize'));	
			
			c.mapConfig.exportConfigOptions.imageExportOptions.pageSize=toNum(get('exportConfigOptionsimageExportOptionspageSize'));	
			c.mapConfig.exportConfigOptions.imageExportOptions.resolution=toNum(get('exportConfigOptionsimageExportOptionsresolution'));	
			c.mapConfig.exportConfigOptions.imageExportOptions.imageFormat=get('exportConfigOptionsimageExportOptionsimageFormat');
			c.mapConfig.exportConfigOptions.imageExportOptions.outputName=get('exportConfigOptionsimageExportOptionsoutputName');
			
			
			//if(c.mapConfig.loggerOptions){}
			if(!c.mapConfig.toolsOptions ){
					c.mapConfig.toolsOptions = {};
				}
				if(!c.mapConfig.toolsOptions.editionOptions)
				{
					c.mapConfig.toolsOptions.editionOptions = {};
				}
				 
					c.mapConfig.toolsOptions.editionOptions.enable=getCheck('toolsOptionseditionOptionsenable');
					c.mapConfig.toolsOptions.editionOptions.modifyStyle=get('toolsOptionseditionOptionsmodifyStyle');
					c.mapConfig.toolsOptions.editionOptions.addStyle=get('toolsOptionseditionOptionsaddStyle');
					c.mapConfig.toolsOptions.editionOptions.deleteStyle=get('toolsOptionseditionOptionsdeleteStyle')
					c.mapConfig.toolsOptions.editionOptions.inline=getCheck('toolsOptionseditionOptionsinline')
					c.mapConfig.toolsOptions.editionOptions.zoomDuration=toNum(get('toolsOptionseditionOptionszoomDuration'));
					c.mapConfig.toolsOptions.editionOptions.tolerance=toNum(get('toolsOptionseditionOptionstolerance'));
					
				 
				
				if(!c.mapConfig.toolsOptions.showCoordToolOptions)
				{
					c.mapConfig.toolsOptions.showCoordToolOptions = {};
				}
				 
					c.mapConfig.toolsOptions.showCoordToolOptions.enable=getCheck('toolsOptionsshowCoordToolOptionsenable');
					c.mapConfig.toolsOptions.showCoordToolOptions.style=get('toolsOptionsshowCoordToolOptionsstyle');					 
					c.mapConfig.toolsOptions.showCoordToolOptions.inline=getCheck('toolsOptionsshowCoordToolOptionsinline')					
				 
				
				
				if(!c.mapConfig.toolsOptions.measureToolOptions)
				{
					c.mapConfig.toolsOptions.measureToolOptions = {};
				}
				 
					c.mapConfig.toolsOptions.measureToolOptions.enable=getCheck('toolsOptionsmeasureToolOptionsenable');					
					c.mapConfig.toolsOptions.measureToolOptions.style=get('toolsOptionsmeasureToolOptionsstyle');					
					c.mapConfig.toolsOptions.measureToolOptions.inline=getCheck('toolsOptionsmeasureToolOptionsinline')
					c.mapConfig.toolsOptions.measureToolOptions.zoomDuration=toNum(get('toolsOptionsmeasureToolOptionszoomDuration'));
					
					
				 
				
				if(!c.mapConfig.toolsOptions.infoToolOptions)
				{
					c.mapConfig.toolsOptions.infoToolOptions = {};
				}
				 
					c.mapConfig.toolsOptions.infoToolOptions.enable=getCheck('toolsOptionsinfoToolOptionsenable');					
					c.mapConfig.toolsOptions.infoToolOptions.style=get('toolsOptionsinfoToolOptionsstyle');					
					c.mapConfig.toolsOptions.infoToolOptions.inline=getCheck('toolsOptionsinfoToolOptionsinline')
					c.mapConfig.toolsOptions.infoToolOptions.zoomDuration=toNum(get('toolsOptionsinfoToolOptionszoomDuration'));
					c.mapConfig.toolsOptions.infoToolOptions.tolerance=toNum(get('toolsOptionsinfoToolOptionstolerance'));				
				 
				
				if(!c.mapConfig.toolsOptions.gotoToolOptions)
				{
					c.mapConfig.toolsOptions.gotoToolOptions = {};
				}
				 
					c.mapConfig.toolsOptions.gotoToolOptions.enable = getCheck('toolsOptionsgotoToolOptionsenable');					
					c.mapConfig.toolsOptions.gotoToolOptions.style=get('toolsOptionsgotoToolOptionsstyle');					
					c.mapConfig.toolsOptions.gotoToolOptions.inline=getCheck('toolsOptionsgotoToolOptionsinline')
					c.mapConfig.toolsOptions.gotoToolOptions.zoomDuration=toNum(get('toolsOptionsgotoToolOptionszoomDuration'));
									
				 
				
				if(!c.mapConfig.toolsOptions.selectByAttrToolOptions)
				{
					c.mapConfig.toolsOptions.selectByAttrToolOptions = {};
				}
				 
					c.mapConfig.toolsOptions.selectByAttrToolOptions.enable = getCheck('toolsOptionsselectByAttrToolOptionsenable');					
					c.mapConfig.toolsOptions.selectByAttrToolOptions.style=get('toolsOptionsselectByAttrToolOptionsstyle');					
					c.mapConfig.toolsOptions.selectByAttrToolOptions.inline=getCheck('toolsOptionsselectByAttrToolOptionsinline')
					c.mapConfig.toolsOptions.selectByAttrToolOptions.zoomDuration=toNum(get('toolsOptionsselectByAttrToolOptionszoomDuration'));
									
				 
				
				if(!c.mapConfig.toolsOptions.selectByGeomToolOptions)
				{
					c.mapConfig.toolsOptions.selectByGeomToolOptions = {};
				}
			 
					c.mapConfig.toolsOptions.selectByGeomToolOptions.enable = getCheck('toolsOptionsselectByGeomToolOptionsenable');					
					c.mapConfig.toolsOptions.selectByGeomToolOptions.style=get('toolsOptionsselectByGeomToolOptionsstyle');					
					c.mapConfig.toolsOptions.selectByGeomToolOptions.inline=getCheck('toolsOptionsselectByGeomToolOptionsinline')
					c.mapConfig.toolsOptions.selectByGeomToolOptions.zoomDuration=toNum(get('toolsOptionsselectByGeomToolOptionszoomDuration'));
									
				 
				
				if(!c.mapConfig.toolsOptions.bufferToolOptions)
				{
					c.mapConfig.toolsOptions.bufferToolOptions = {};
				}
				 
					c.mapConfig.toolsOptions.bufferToolOptions.enable = getCheck('toolsOptionsbufferToolOptionsenable');					
					c.mapConfig.toolsOptions.bufferToolOptions.style=get('toolsOptionsbufferToolOptionsstyle');					
					c.mapConfig.toolsOptions.bufferToolOptions.inline=getCheck('toolsOptionsbufferToolOptionsinline')
					c.mapConfig.toolsOptions.bufferToolOptions.zoomDuration=toNum(get('toolsOptionsbufferToolOptionszoomDuration'));
					c.mapConfig.toolsOptions.bufferToolOptions.processLimitMiliSeconds=toNum(get('toolsOptionsbufferToolOptionsprocessLimitMiliSeconds'));
									
				 
				if(!c.mapConfig.toolsOptions.intersectToolOptions)
				{
					c.mapConfig.toolsOptions.intersectToolOptions = {};
				}
			 
					c.mapConfig.toolsOptions.intersectToolOptions.enable = getCheck('toolsOptionsintersectToolOptionsenable');					
					c.mapConfig.toolsOptions.intersectToolOptions.style=get('toolsOptionsintersectToolOptionsstyle');					
					c.mapConfig.toolsOptions.intersectToolOptions.inline=getCheck('toolsOptionsintersectToolOptionsinline')
					c.mapConfig.toolsOptions.intersectToolOptions.zoomDuration=toNum(get('toolsOptionsintersectToolOptionszoomDuration'));
					c.mapConfig.toolsOptions.intersectToolOptions.processLimitMiliSeconds=toNum(get('toolsOptionsintersectToolOptionsprocessLimitMiliSeconds'));
									
				 
			 if(!c.mapConfig.toolsOptions.proximityToolOptions)
				{
					c.mapConfig.toolsOptions.proximityToolOptions = {};
				}
				
					c.mapConfig.toolsOptions.proximityToolOptions.enable = getCheck('toolsOptionsproximityToolOptionsenable');					
					c.mapConfig.toolsOptions.proximityToolOptions.style=get('toolsOptionsproximityToolOptionsstyle');					
					c.mapConfig.toolsOptions.proximityToolOptions.inline=getCheck('toolsOptionsproximityToolOptionsinline')
					c.mapConfig.toolsOptions.proximityToolOptions.zoomDuration=toNum(get('toolsOptionsproximityToolOptionszoomDuration'));
					c.mapConfig.toolsOptions.proximityToolOptions.processLimitMiliSeconds=toNum(get('toolsOptionsproximityToolOptionsprocessLimitMiliSeconds'));
									
				 
				 if(!c.mapConfig.toolsOptions.geocoderToolOptions)
				{
					c.mapConfig.toolsOptions.geocoderToolOptions = {};
				}
				
				
				
				
				
				
				
				
				 
					c.mapConfig.toolsOptions.geocoderToolOptions.enable = getCheck('toolsOptionsgeocoderToolOptionsenable');					
					c.mapConfig.toolsOptions.geocoderToolOptions.style=get('toolsOptionsgeocoderToolOptionsstyle');					
					c.mapConfig.toolsOptions.geocoderToolOptions.inline=getCheck('toolsOptionsgeocoderToolOptionsinline');
					c.mapConfig.toolsOptions.geocoderToolOptions.zoomDuration=toNum(get('toolsOptionsgeocoderToolOptionszoomDuration'));
					c.mapConfig.toolsOptions.geocoderToolOptions.provider=get('toolsOptionsgeocoderToolOptionsprovider');
					c.mapConfig.toolsOptions.geocoderToolOptions.styleSelect=get('toolsOptionsgeocoderToolOptionsstyleSelect');					
					c.mapConfig.toolsOptions.geocoderToolOptions.authMode=get('toolsOptionsgeocoderToolOptionsauthMode');
					 if(!c.mapConfig.toolsOptions.geocoderToolOptions.search)
					{
						c.mapConfig.toolsOptions.geocoderToolOptions.search = {};
					}
					
					 
					c.mapConfig.toolsOptions.geocoderToolOptions.search.city=get('toolsOptionsgeocoderToolOptionssearchcity');					
					c.mapConfig.toolsOptions.geocoderToolOptions.search.county=get('toolsOptionsgeocoderToolOptionssearchcounty');
					c.mapConfig.toolsOptions.geocoderToolOptions.search.neighborhood=get('toolsOptionsgeocoderToolOptionssearchneighborhood');
					c.mapConfig.toolsOptions.geocoderToolOptions.search.postalCode=toNum(get('toolsOptionsgeocoderToolOptionssearchpostalCode'));
					c.mapConfig.toolsOptions.geocoderToolOptions.search.postalExt=toNum(get('toolsOptionsgeocoderToolOptionssearchpostalExt'));
					c.mapConfig.toolsOptions.geocoderToolOptions.search.region=get('toolsOptionsgeocoderToolOptionssearchregion');
					c.mapConfig.toolsOptions.geocoderToolOptions.search.state=get('toolsOptionsgeocoderToolOptionssearchstate');
					c.mapConfig.toolsOptions.geocoderToolOptions.search.subregion=get('toolsOptionsgeocoderToolOptionssearchsubregion');
					c.mapConfig.toolsOptions.geocoderToolOptions.search.country=get('toolsOptionsgeocoderToolOptionssearchcountry');
					
					
					 if(!c.mapConfig.toolsOptions.geocoderToolOptions.options)
					{
						c.mapConfig.toolsOptions.geocoderToolOptions.options = {};
					}
					
					
					c.mapConfig.toolsOptions.geocoderToolOptions.options.token=get('toolsOptionsgeocoderToolOptionsoptionstoken');
					c.mapConfig.toolsOptions.geocoderToolOptions.options.mode=get('toolsOptionsgeocoderToolOptionsoptionsmode');
					c.mapConfig.toolsOptions.geocoderToolOptions.options.structured=get('toolsOptionsgeocoderToolOptionsoptionsstructured');
					c.mapConfig.toolsOptions.geocoderToolOptions.options.categories=get('toolsOptionsgeocoderToolOptionsoptionscategories');
					c.mapConfig.toolsOptions.geocoderToolOptions.options.countryCode=get('toolsOptionsgeocoderToolOptionsoptionscountryCode');
					c.mapConfig.toolsOptions.geocoderToolOptions.options.langCode=get('toolsOptionsgeocoderToolOptionsoptionslangCode');
					c.mapConfig.toolsOptions.geocoderToolOptions.options.location=get('toolsOptionsgeocoderToolOptionsoptionslocation');
					c.mapConfig.toolsOptions.geocoderToolOptions.options.locationType=get('toolsOptionsgeocoderToolOptionsoptionslocationType');
					c.mapConfig.toolsOptions.geocoderToolOptions.options.numberCandidates=toNum(get('toolsOptionsgeocoderToolOptionsoptionsnumberCandidates'));
					c.mapConfig.toolsOptions.geocoderToolOptions.options.scoreLimit=toNum(get('toolsOptionsgeocoderToolOptionsoptionsscoreLimit'));
					c.mapConfig.toolsOptions.geocoderToolOptions.options.searchExtent=get('toolsOptionsgeocoderToolOptionsoptionssearchExtent');
					c.mapConfig.toolsOptions.geocoderToolOptions.options.searchZoom=toNum(get('toolsOptionsgeocoderToolOptionsoptionssearchZoom'));
					c.mapConfig.toolsOptions.geocoderToolOptions.options.sourceCountry=get('toolsOptionsgeocoderToolOptionssourceCountry');
			 if(!c.mapConfig.toolsOptions.routingToolOptions)
				{
					c.mapConfig.toolsOptions.routingToolOptions = {};
				}
				 
					c.mapConfig.toolsOptions.routingToolOptions.enable = getCheck('toolsOptionsroutingToolOptionsenable');
					c.mapConfig.toolsOptions.routingToolOptions.style=get('toolsOptionsroutingToolOptionsstyle');						
					c.mapConfig.toolsOptions.routingToolOptions.stopStyle=get('toolsOptionsroutingToolOptionsstopStyle');	
					c.mapConfig.toolsOptions.routingToolOptions.blockStyle=get('toolsOptionsroutingToolOptionsblockStyle');			
					c.mapConfig.toolsOptions.routingToolOptions.routeStyle=get('toolsOptionsroutingToolOptionsrouteStyle');							
					c.mapConfig.toolsOptions.routingToolOptions.inline=getCheck('toolsOptionsroutingToolOptionsinline');
					c.mapConfig.toolsOptions.routingToolOptions.provider=get('toolsOptionsroutingToolOptionsprovider');		
					c.mapConfig.toolsOptions.routingToolOptions.language=get('toolsOptionsroutingToolOptionslanguage');		
					c.mapConfig.toolsOptions.routingToolOptions.token=get('toolsOptionsroutingToolOptionstoken');
				 
				
				 if(!c.mapConfig.toolsOptions.historyExtentOptions)
				{
					c.mapConfig.toolsOptions.historyExtentOptions = {};
				}
			 
					c.mapConfig.toolsOptions.historyExtentOptions.enable = getCheck('toolsOptionshistoryExtentOptionsenable');
					c.mapConfig.toolsOptions.historyExtentOptions.zoomDuration=toNum(get('toolsOptionshistoryExtentOptionszoomDuration'));						
					c.mapConfig.toolsOptions.historyExtentOptions.maxOperations=toNum(get('toolsOptionshistoryExtentOptionsmaxOperations'));
					 
				 
				 if(!c.mapConfig.toolsOptions.bookmarkToolOptions)
				{
					c.mapConfig.toolsOptions.bookmarkToolOptions = {};
				}
				 
					c.mapConfig.toolsOptions.bookmarkToolOptions.enable = getCheck('toolsOptionsbookmarkToolOptionsenable');
					c.mapConfig.toolsOptions.bookmarkToolOptions.zoomDuration=toNum(get('toolsOptionsbookmarkToolOptionszoomDuration'));						
					c.mapConfig.toolsOptions.bookmarkToolOptions.maxNumberBookmark=toNum(get('toolsOptionsbookmarkToolOptionsmaxNumberBookmark'));
					c.mapConfig.toolsOptions.bookmarkToolOptions.onlyActiveMap=getCheck('toolsOptionsbookmarkToolOptionsonlyActiveMap')
					 
				 			 
				
				c.mapConfig.mapOptions = mapsList;
				c.mapConfig.mainMapOptions.projections = projectionList;




		return JSON.stringify(c);





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

	/*	// DELETE INITIAL DASHBOARD CONF
		deleteProject: function(id) {
			deleteProject(id);
		},*/
		 showHideByProjectType: function() {
			showHideByProjectType();
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	MapsProjectController.load(elementJson);

	// AUTO INIT CONTROLLER.
	MapsProjectController.init();
});
