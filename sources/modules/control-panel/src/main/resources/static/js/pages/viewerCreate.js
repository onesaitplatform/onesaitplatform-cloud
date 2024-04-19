
var ViewerCreateController = function() {
	
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'Onesait Platform Control Panel'; 
	var LIB_TITLE = 'Viewer Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var hasId = false; // instance
	var fieldsArray = [];
	var baseLayers = [];
	var htmlEditor;
	var initMap= '';
	var setLayers = [];
	var isBaseLayerLoad= false;
	var base_tpl = null;

	// CONTROLLER PRIVATE FUNCTIONS	--------------------------------------------------------------------------------

	var propertyTypeOntologyIndex=-1;
	
	var cleanFields = function(formId) {
		logControl ? console.log('cleanFields() -> ') : '';
		
		// CLEAR MAP
        $("#container").css('visibility', 'hidden');
		htmlEditor.setValue('');
		ViewerCreateController.run();
		if (isBaseLayerLoad) {
			isBaseLayerLoad = false;			
		}
		
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
		
		// UNCHECK OUBLIC OPTION
		$("#public").prop("checked", false);
	}
	
	
	// CHECK IF JSON STRING WHEN JSON PARSE IS OK OR NOT, IF THE JSON IS MALFORMED THE JSON.parse() is broken.
	var IsJsonString = function(str) {
		try {
			JSON.parse(str);
		}
		catch (e) {	return false; }
		
		return true;
	}
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// DELETE VIEWER
	var deleteViewer = function(viewerId){
		console.log('deleteViewerConfirmation() -> formId: '+ viewerId);

		// no Id no fun!
		if ( !viewerId ) {toastr(ontologyCreateReg.validations.validform,''); return false; }

		logControl ? console.log('deleteViewerConfirmation() -> formAction: ' + $('.delete-viewer').attr('action') + ' ID: ' + $('#delete-viewerId').attr('viewerId')) : '';

		// call ontology Confirm at header.
		HeaderController.showConfirmDialogViewer('delete_viewer_form');
	}
	
		
	var handleValidation =  function() {
        // for more info visit the official plugin documentation:
        // http://docs.jquery.com/Plugins/Validation

        var form1 = $('#viewer_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');


        form1.validate({
            errorElement: 'span', // default input error message container
            errorClass: 'help-block help-block-error', // default input error
														// message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .formcolorpicker, .hidden-validation')", // validate
																		// all
																		// fields
																		// including
																		// form
																		// hidden
																		// input
																		// but
																		// not
																		// selectpicker
			lang: currentLanguage,
			// custom messages
            messages: {
//				jsonschema: { required:"El esquema no se ha guardado correctamente"},
//				datamodelid: { required: "Por favor seleccione una plantilla de ontología, aunque sea la vacia."}
			},
			// validation rules
            rules: {
            	ontology:		{ required: true },
                identification:	{ minlength: 5, required: true },
				description:	{ minlength: 5, required: true },
				technology:	{ required: true },
				baseLayer:		{ required: true }
            },
            invalidHandler: function(event, validator) { // display error
															// alert on form
															// submit
                toastr.error(viewerCreateJson.messages.validationKO);
            },
            errorPlacement: function(error, element) {
            	if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }
				else if ( element.is(':hidden'))	{
					if ($('#datamodelid').val() === '') { $('#datamodelError').removeClass('hide');}
				}
				else { error.insertAfter(element); }
            },
            highlight: function(element) { // hightlight error inputs
                $(element).closest('.form-group').addClass('has-error');
            },
            unhighlight: function(element) { // revert the change done by
												// hightlight
                $(element).closest('.form-group').removeClass('has-error');
            },
            success: function(label) {
                label.closest('.form-group').removeClass('has-error');
            },
			// ALL OK, THEN SUBMIT.
            submitHandler: function(form) {
            	
                var infoBox=[];
    			
    			var js = htmlEditor.getValue();
    			src = base_tpl;
    			
    			// Javascript
    			js = '<script>' + js + '</script>';
    			src = src.replace('</body>', js + '</body>');
    			src = src.replace(/"/g, "&quot;");
    			
    			if($("#baseLayers").val()!="")
    			{
	    			$("<input type='hidden' name='rollback' value='"+$("#rollback").is(':checked')+"' />")
	 		         .appendTo("#viewer_create_form");
	    			
	    			$("<input type='hidden' name='baseLayer' th:field='*{baseLayer}' value='"+$("#baseLayers").val()+"' />")
	  		         .appendTo("#viewer_create_form");
	    			
	    			$('<input type="hidden" name="jsViewer" value="'+src+'" />')
	 		         .appendTo("#viewer_create_form");
	    			
	    			if($("#layersSelect").val() != null){
	    				$('<input type="hidden" name="layersSelectedHidden" value="'+$("#layersSelect").val()+'" />')
	   		         .appendTo("#viewer_create_form");
	    			}
	    			
					form1.ajaxSubmit({type: 'post', success : function(data){
						toastr.success(viewerCreateJson.messages.validationOK);
						navigateUrl(data.redirect);
						
						}, error: function(data){
							toastr.error(data.responseJSON.cause);
							//HeaderController.showErrorDialog(data.responseJSON.cause)
						}
					})
    			}
				else
				{
					toastr.error(viewerCreateJson.messages.validationKO);
				}

			}
        });
    }
	
	
	
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return viewerCreateJson = Data;
		},
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			
			// INPUT MASK FOR ontology identification allow only letters, numbers and -_
			$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
			
			$(".panel-left").resizable({
			   handleSelector: ".splitter",
			   resizeHeight: false,
			   onDragStart: function(){
				 console.log('start...');
				 
			   },
			   onDragEnd: function(){
				 console.log('stop...');
				 
			   }
			 });
	
			handleValidation();
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
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			$.ajax({
				url : "/controlpanel/viewers/getHtmlCode",
				headers: {
					[csrf_header]: csrf_value
			    },
				type : 'GET',
				dataType: 'text', 
				contentType: 'text/plain',
				mimeType: 'text/plain',
				async : false,
				success : function(data) {
					
					base_tpl = data;
					
				},
				error : function(data, status, er) {
					 
					toastr.error(er,'');
				}
			});
			
			// INSERT MODE ACTIONS  (ontologyCreateReg.actionMode = NULL ) 
			if ( viewerCreateJson.actionMode === null){
				logControl ? console.log('|---> Action-mode: INSERT') : '';

				htmlEditor = CodeMirror.fromTextArea(document.getElementById("jsBody"), {
	        	  	mode: "text/javascript",
					lineNumbers: true,
		            foldGutter: true,
		            matchBrackets: true,
		            styleActiveLine: true,
		            theme:"elegant",
		            autoCloseBrackets: true,
		            lineWrapping: true,
		            fullScreen: true
				});
				
			}
			// EDIT MODE ACTION 
			else {	
				logControl ? console.log('|---> Action-mode: UPDATE') : '';
					
				htmlEditor = CodeMirror.fromTextArea(document.getElementById("jsBody"), {
					mode: "text/javascript",
					lineNumbers: true,
		            foldGutter: true,
		            matchBrackets: true,
		            styleActiveLine: true,
		            theme:"elegant",
		            autoCloseBrackets: true,
		            lineWrapping: true,
		            fullScreen: true
				});
				
				if(viewerCreateJson.isPublic){
					$("#public").attr("checked", "checked");
				}
				
				$("#technology").val(viewerCreateJson.tecnology);
				$('#technology').selectpicker('refresh');
				var csrf_value = $("meta[name='_csrf']").attr("content");
				var csrf_header = $("meta[name='_csrf_header']").attr("content");
				$.ajax({
					url : "/controlpanel/viewers/getBaseLayers/" + viewerCreateJson.tecnology,
					headers: {
						[csrf_header]: csrf_value
				    },
					type : 'GET',
					dataType: 'text', 
					contentType: 'text/plain',
					mimeType: 'text/plain',
					async : false,
					success : function(data) {
						var options = [];
						$.each(JSON.parse(data), function(k,v){
							options.push('<option value="'+v.identification+'">'+v.name+'</option>');
							baseLayers.push(v);
						});
						
						$('#baseLayers').html(options);
						$('#baseLayers').removeAttr("disabled");
						$('#baseLayers').selectpicker('refresh');
						
						$("#baseLayers").val(viewerCreateJson.baseLayer);
						$('#baseLayers').selectpicker('refresh');
						
						
						var js = viewerCreateJson.js;
						
						var begin = js.indexOf("<script>");
						var end = js.lastIndexOf("script>");
						
						js = js.substring(end -2,begin + 8);
						
						htmlEditor.setValue(js);
						
						$("#container").css('visibility', 'visible');
						var csrf_value = $("meta[name='_csrf']").attr("content");
						var csrf_header = $("meta[name='_csrf_header']").attr("content");
						$.ajax({
							url : "/controlpanel/viewers/getLayers/",
							headers: {
								[csrf_header]: csrf_value
						    },
							type : 'GET',
							dataType: 'text', 
							contentType: 'text/plain',
							mimeType: 'text/plain',
							async : false,
							success : function(data) {
								
								var options = [];
								
								$.each(JSON.parse(data), function(k,v){
									options.push('<option value="'+v+'">'+v+'</option>');
								});
								
								$('#layersSelect').html(options);
								$('#layersSelect').removeAttr("disabled");
								$('#layersSelect').selectpicker('refresh');
								
								$("#layersSelect").val(viewerCreateJson.layersInUse);
								$('#layersSelect').selectpicker('refresh');
								
							},
							error : function(data, status, er) {
								 
								toastr.error(er,'');
							}
						});
						
						ViewerCreateController.run();
						
					},
					error : function(data, status, er) {
						toastr.error(er,''); 
					}
				});
			}
			
			$('#resetBtn').on('click', function() {
				cleanFields('viewer_create_form');
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
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		deleteViewer: function(viewerId){
			deleteViewer(viewerId);
		},
		rollbackViewer: function(viewerId){
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			$.ajax({
				url : "/controlpanel/viewers/doRollback/" + viewerCreateJson.actionMode,
				headers: {
					[csrf_header]: csrf_value
			    },
				type : 'GET',
				dataType: 'text', 
				contentType: 'text/plain',
				mimeType: 'text/plain',
				success : function(data) {
					navigateUrl(data);
				},
				error : function(data, status, er) {
					toastr.error(er,'');
				}
			});
		},
		run : function() {
			event.preventDefault();
			
			var intervalIds = $("#viewerIframe").contents().find("#intervalIds").val();
			if(intervalIds != undefined && intervalIds != ""){
				$.each(JSON.parse(intervalIds), function(k,v){
					document.getElementById("viewerIframe").contentWindow.clearInterval(v);
				});
			}
			
			
			var iframe = document.querySelector('#viewerIframe');
			iframe_doc = iframe.contentDocument;
			
			iframe_doc.open();
			iframe_doc.write('');
			iframe_doc.close();
			
			var js =htmlEditor.getValue();
			
			var longitude = "var startLongitude = " + $("#longitude").val();
			var latitude = "var startLatitude = " + $("#latitude").val();
			var height = "var startHeight = " + $("#height").val();
			
			var searchString = 'var startLongitude';
			var re = new RegExp('^.*' + searchString + '.*$', 'gm');
			var formatted = js.replace(re, longitude);
			
			searchString = 'var startLatitude';
			re = new RegExp('^.*' + searchString + '.*$', 'gm');
			formatted = formatted.replace(re, latitude);
			
			searchString = 'var startHeight';
			re = new RegExp('^.*' + searchString + '.*$', 'gm');
			formatted = formatted.replace(re, height);
			
			htmlEditor.setValue(formatted);
			
			src = base_tpl;
			
			// Javascript
			formatted = '<script>' + formatted + '<\/script>';
			src = src.replace('</body>', formatted + '</body>');
			
			iframe_doc.open();
			iframe_doc.write(src);
			iframe_doc.close();
			
		},
		changeTechology : function(){
			
			if($("#longitude").val() == "" && $("#latitude").val() == "" && $("#height").val() == "" ){				 
				toastr.error(viewerCreateJson.validations.error,'');
				$("#technology").val("");
				return;
			}
			
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			$.ajax({
				url : "/controlpanel/viewers/getBaseLayers/" + $("#technology").val(),
				headers: {
					[csrf_header]: csrf_value
			    },
				type : 'GET',
				dataType: 'text', 
				contentType: 'text/plain',
				mimeType: 'text/plain',
				success : function(data) {
					var options = [];
					$.each(JSON.parse(data), function(k,v){
						options.push('<option value="'+v.identification+'">'+v.name+'</option>');
						baseLayers.push(v);
					});
					
					$('#baseLayers').html(options);
					$('#baseLayers').removeAttr("disabled");
					$('#baseLayers').selectpicker('refresh');
					
				},
				error : function(data, status, er) {
				 
					toastr.error(er,'');
				}
			});
		},
		changeBaseLayer : function(){
			
			var url='';
			$.each(baseLayers, function(k,v){
				if(v.identification == $("#baseLayers").val()){
					url = v.url;
				}
			});
			
			initMap= "initialBaseMap('"+$("#baseLayers").val()+"','', '"+url+"')\n";
			var layersSelected = $("#layersSelect").val();
			var layersTypes = viewerCreateJson.layersTypes;
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			if(!isBaseLayerLoad){
				$.ajax({
					url : "/controlpanel/viewers/getJSBaseCode/",
					headers: {
						[csrf_header]: csrf_value
				    },
					type : 'POST',
					data : {'latitude': $("#latitude").val(),'longitude': $("#longitude").val(),'height': $("#height").val()},
					async: false,
					success : function(data) {
						
						  data += initMap;
						  htmlEditor.setValue(data);
				          ViewerCreateController.run();
				          isBaseLayerLoad = true;
				          $("#container").css('visibility', 'visible');
				          var csrf_value = $("meta[name='_csrf']").attr("content");
				  		  var csrf_header = $("meta[name='_csrf_header']").attr("content");
							$.ajax({
								url : "/controlpanel/viewers/getLayers/",
								headers: {
									[csrf_header]: csrf_value
							    },
								type : 'GET',
								dataType: 'text', 
								contentType: 'text/plain',
								mimeType: 'text/plain',
								success : function(data) {
									
									var options = [];
									
									$.each(JSON.parse(data), function(k,v){
										options.push('<option value="'+v+'">'+v+'</option>');
									});
									
									$('#layersSelect').html(options);
									$('#layersSelect').removeAttr("disabled");
									$('#layersSelect').selectpicker('refresh');
									
								},
								error : function(data, status, er) {
									 
									toastr.error(er,'');
								}
							});
						
					},
					error : function(data, status, er) {
						toastr.error(er,'');
					}
				});
				

			}else{
				var csrf_value = $("meta[name='_csrf']").attr("content");
				var csrf_header = $("meta[name='_csrf_header']").attr("content");
				$.ajax({
					url : "/controlpanel/viewers/getJSBaseCode/",
					headers: {
						[csrf_header]: csrf_value
				    },
					type : 'POST',
					data : {'latitude': $("#latitude").val(),'longitude': $("#longitude").val(),'height': $("#height").val()},
					async: false,
					success : function(data) {
						
						data += initMap;
						  if(layersSelected!=null){
							  setLayers = [];
							  setHeatLayers = [];
							  $.each(layersSelected, function(k,layerAux){
								  if(layersTypes[layerAux] == 'iot' ){
									    var csrf_value = $("meta[name='_csrf']").attr("content");
										var csrf_header = $("meta[name='_csrf_header']").attr("content");
										$.ajax({
											url : "/controlpanel/viewers/getLayerData/" + layerAux,
											headers: {
												[csrf_header]: csrf_value
										    },
											type : 'GET',
											dataType: 'text', 
											contentType: 'text/plain',
											mimeType: 'text/plain',
											async : false,
											success : function(data) {
												
												setLayers.push(data);
												
											},
											error : function(data, status, er) {
												toastr.error(er,'');
											}
										});
											
								  }else if(layersTypes[layerAux] == 'heat' ){
									    var csrf_value = $("meta[name='_csrf']").attr("content");
										var csrf_header = $("meta[name='_csrf_header']").attr("content");
										$.ajax({
											url : "/controlpanel/viewers/getLayerData/" + layerAux,
											headers: {
												[csrf_header]: csrf_value
										    },
											type : 'GET',
											dataType: 'text', 
											contentType: 'text/plain',
											mimeType: 'text/plain',
											async : false,
											success : function(data) {
												
												setHeatLayers.push(data);
												
											},
											error : function(data, status, er) {
												toastr.error(er,'');
											}
										});
											
								  }else if(layersTypes[layerAux] == 'wms' ){
									  var csrf_value = $("meta[name='_csrf']").attr("content");
									  var csrf_header = $("meta[name='_csrf_header']").attr("content");
									  $.ajax({
											url : "/controlpanel/viewers/getLayerWms/" + layerAux,
											headers: {
												[csrf_header]: csrf_value
										    },
											type : 'GET',
											dataType: 'text', 
											contentType: 'text/plain',
											mimeType: 'text/plain',
											async : false,
											success : function(result) {
												result = JSON.parse(result);
												var urlLayer = result['url'];
												var layerWms = result['layerWms'];
												 data += "loadWms('"+urlLayer+"', '"+layerWms+"', '"+ layerAux +"')\n";
												
											},
											error : function(data, status, er) {
												toastr.error(er,'');
											}
										});
									  
									 
								  }else if(layersTypes[layerAux] == 'kml' ){
									  var csrf_value = $("meta[name='_csrf']").attr("content");
									  var csrf_header = $("meta[name='_csrf_header']").attr("content");
									  $.ajax({
											url : "/controlpanel/viewers/getLayerKml/" + layerAux,
											headers: {
												[csrf_header]: csrf_value
										    },
											type : 'GET',
											dataType: 'text', 
											contentType: 'text/plain',
											mimeType: 'text/plain',
											async : false,
											success : function(result) {
												result = JSON.parse(result);
												var urlLayer = result['url'];
												 data += "loadKml('"+urlLayer+"', '"+ layerAux +"')\n";
												
											},
											error : function(data, status, er) {
												toastr.error(er,'');
											}
										});
									  
									 
								  }
								 
								}) 
								$.each(setLayers, function(k,v){
										data += "createLayer("+v+")\n";
								})
								
								$.each(setHeatLayers, function(k,v){
										data += "heatmapGenerator("+v+")\n";
								})
						  }
						 
						  htmlEditor.setValue(data);
				          ViewerCreateController.run();
						
					},
					error : function(data, status, er) {
						toastr.error(er,'');
					}
				});
				

			}
			
		
			
		},
		changeLayer : function(){
			var layersSelected = $("#layersSelect").val();
			if(layersSelected==null){
				layersSelected=[];
			}
			
			var url='';
			
			$.each(baseLayers, function(k,v){
				if(v.identification == $("#baseLayers").val()){
					url = v.url;
				}
			});
			
			initMap= "initialBaseMap('"+$("#baseLayers").val()+"','', '"+url+"')\n";
			var layersTypes = viewerCreateJson.layersTypes;
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			
			$.ajax({
				url : "/controlpanel/viewers/getJSBaseCode/",
				headers: {
					[csrf_header]: csrf_value
			    },
				type : 'POST',
				data : {'latitude': $("#latitude").val(),'longitude': $("#longitude").val(),'height': $("#height").val()},
				async: false,
				success : function(data) {
					
					 data += initMap;
					  setLayers = [];
					  setHeatLayers = [];
					  $.each(layersSelected, function(index, layerAux){
						  if(layersTypes[layerAux] == 'iot' ){
							  setLayers.push(layerAux);
							
						  }else if(layersTypes[layerAux] == 'heat' ){
							  setHeatLayers.push(layerAux);
			
						  }else if(layersTypes[layerAux] == 'wms' ){
							  var csrf_value = $("meta[name='_csrf']").attr("content");
							  var csrf_header = $("meta[name='_csrf_header']").attr("content");
							  $.ajax({
									url : "/controlpanel/viewers/getLayerWms/" + layerAux,
									headers: {
										[csrf_header]: csrf_value
								    },
									type : 'GET',
									dataType: 'text', 
									contentType: 'text/plain',
									mimeType: 'text/plain',
									async : false,
									success : function(result) {
										result = JSON.parse(result);
										var urlLayer = result['url'];
										var layerWms = result['layerWms'];
										 data += "loadWms('"+urlLayer+"', '"+layerWms+"', '"+ layerAux +"')\n";
										
									},
									error : function(data, status, er) {
										toastr.error(er,'');
									}
								});
							  
							 
						  }else if(layersTypes[layerAux] == 'kml' ){
							  var csrf_value = $("meta[name='_csrf']").attr("content");
							  var csrf_header = $("meta[name='_csrf_header']").attr("content");
							  $.ajax({
									url : "/controlpanel/viewers/getLayerKml/" + layerAux,
									headers: {
										[csrf_header]: csrf_value
								    },
									type : 'GET',
									dataType: 'text', 
									contentType: 'text/plain',
									mimeType: 'text/plain',
									async : false,
									success : function(result) {
										result = JSON.parse(result);
										var urlLayer = result['url'];
										 data += "loadKml('"+urlLayer+"', '"+ layerAux +"')\n";
										
									},
									error : function(data, status, er) {
										toastr.error(er,'');
									}
								});
							  
							 
						  }else if(layersTypes[layerAux] == 'svg_image' ){
							  var csrf_value = $("meta[name='_csrf']").attr("content");
							  var csrf_header = $("meta[name='_csrf_header']").attr("content");
							  $.ajax({
									url : "/controlpanel/viewers/getLayerSvgImage/" + layerAux,
									headers: {
										[csrf_header]: csrf_value
								    },
									type : 'GET',
									dataType: 'text', 
									contentType: 'text/plain',
									mimeType: 'text/plain',
									async : false,
									success : function(result) {
										result = JSON.parse(result);
										var urlLayer = result['url'] + '?disposition=1';
										var west = result['west'];
										var east = result['east'];
										var south = result['south'];
										var north = result['north'];
										 data += "addSvgLayer('"+urlLayer+"',"+west+","+ south+","+ east+","+ north+", '"+ layerAux +"')\n";
										
									},
									error : function(data, status, er) {
										toastr.error(er,'');
									}
								});
							  
							 
						  }
					  })
					  
					  	$.each(setLayers, function(k,v){
					  		var csrf_value = $("meta[name='_csrf']").attr("content");
							var csrf_header = $("meta[name='_csrf_header']").attr("content");
					  		$.ajax({
								url : "/controlpanel/viewers/getQueryParamsAndRefresh/" + v,
								headers: {
									[csrf_header]: csrf_value
							    },
								type : 'GET',
								dataType: 'text', 
								contentType: 'text/plain',
								mimeType: 'text/plain',
								async : false,
								success : function(result) {
									
									var result = JSON.parse(result);
									var params = result["params"];
									var refresh = result["refresh"];
									
									if(refresh>0){
										data += 'getLayerData(\''+v+'\',\''+JSON.stringify(params)+'\');\n';
										data += 'intervalIds.push(setInterval(function() {getLayerData(\''+v+'\',\''+JSON.stringify(params)+'\')}, '+refresh*1000+'));\n';
									}else{
										data += 'getLayerData(\''+v+'\',\''+JSON.stringify(params)+'\');\n';
									}
									
								},
								error : function(data, status, er) {
									toastr.error(er,'');
								}
							});
					  		
					  		
						})
						
						$.each(setHeatLayers, function(k,v){
					  		var csrf_value = $("meta[name='_csrf']").attr("content");
							var csrf_header = $("meta[name='_csrf_header']").attr("content");
					  		$.ajax({
								url : "/controlpanel/viewers/getQueryParamsAndRefresh/" + v,
								headers: {
									[csrf_header]: csrf_value
							    },
								type : 'GET',
								dataType: 'text', 
								contentType: 'text/plain',
								mimeType: 'text/plain',
								async : false,
								success : function(result) {
									
									var result = JSON.parse(result);
									var params = result["params"];
									var refresh = result["refresh"];
									
									if(refresh>0){
										data += 'getLayerData(\''+v+'\',\''+JSON.stringify(params)+'\');\n';
										data += 'intervalIds.push(setInterval(function() {getLayerData(\''+v+'\',\''+JSON.stringify(params)+'\')}, '+refresh*1000+'));\n';
									}else{
										data += 'getLayerData(\''+v+'\',\''+JSON.stringify(params)+'\');\n';
									}
									
								},
								error : function(data, status, er) {
									toastr.error(er,'');
								}
							});
					  		
					  		
						})
						data += '$("#intervalIds").val(JSON.stringify(intervalIds));\n';

						htmlEditor.setValue(data);
						ViewerCreateController.run();
					
				},
				error : function(data, status, er) {
					toastr.error(er,'');
				}
			});

			
			
		}

	}
}();


// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	ViewerCreateController.load(viewerCreateJson);
	
	ViewerCreateController.init();
	
});


