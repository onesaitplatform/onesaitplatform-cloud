var ExternalResourcesController = function(){
	var fileLoaded;
	var parentNode;
	var myCodeMirrorSchema;
	var csrfParam =  headerJson.csrfParameterName;
	var csrfToken = headerJson.csrfToken;
	var arrayJson;
	var fileType;
	var fileName;
	var submitFromModal = false;

	var navigateUrl = function (url){
		window.location.href = url;
	};
	
	$('#resetBtn').on('click',function(){ 
		cleanFields('resource_create_form');
	});

	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		$('#public').prop('checked', false);
		
		$('#check-new-ontology').prop('checked', true);
		$('#newOntologyDiv').addClass('hide');
		
		hideResultPanel();
		document.getElementById("createOnt").disabled = true;
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}

	// FORM VALIDATION
	var handleValidation = function() {
		console.log('handleValidation() -> ');
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#resource_create_form');

		// set current language
		currentLanguage = resourceCreateJson.language || LANGUAGE;

		form1.validate({
					errorElement : 'span',
					errorClass : 'help-block help-block-error',
					focusInvalid : false,
					ignore : ":hidden:not(.selectpicker)",
					lang : currentLanguage,
					rules : {
						name : { minlength : 5, required : true },
						resourceDataset : {required : true},
						resourceFormat: {required : true}
					},
					invalidHandler : function(event, validator) {
						toastr.error(resourceCreateJson.validform.emptyfields,'');
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
					highlight : function(element) { 
						$(element).closest('.form-group').addClass('has-error');
					},
					unhighlight : function(element) { 
						$(element).closest('.form-group').removeClass(
								'has-error');
					},
					success : function(label) {
						label.closest('.form-group').removeClass('has-error');
					},
					// ALL OK, THEN SUBMIT.
					submitHandler : function(form) {
						if (submitFromModal) {
							form.submit();
						} else {
							ExternalResourcesController.processSubmit(form);
						}
					}
				});
	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		
		
		$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.getElementsByTagName('select')[0]){
				$('.form').validate().element('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id'));
			}
		});
		
		$('#resourceDataset').parent().focusout (function(){
            $('#resource_create_form').validate().element('#resourceDataset');
		} );
		
		$('#resourceFormat').parent().focusout (function(){
            $('#resource_create_form').validate().element('#resourceFormat');
		} );
		
	}

	var initCodeMirror = function () {
        var myTextArea = document.getElementById('schemaTextArea');
        myCodeMirrorSchema = monaco.editor.create(myTextArea, {
    		value: "{\n}",
    		language: 'json',
    		readOnly: false,
    		scrollBeyondLastLine: false,
    		theme: "vs-dark",
    		automaticLayout: true
    	});		
    };

    var generateSchemaFromFile = function (){
    	if (!myCodeMirrorSchema){
    		initCodeMirror();
    	}   	
    	$('#result-panel').removeClass('hide');
    	var jsonData = JSON.stringify(fileLoaded);
    	var printJson;
    	if (fileLoaded.length == null){
    		printJson = JSON.stringify(fileLoaded);
    	} else {
    		printJson = JSON.stringify(fileLoaded[0]);
    	}
    	$('#resourceData').val(jsonData);
    	resetSchema();
    	myCodeMirrorSchema.setValue(processJSON(printJson));    	
    };

    var generateSchemaFromURL = function(data){
		var schema = processJSON(data);
		$('#result-panel').removeClass('hide');
		myCodeMirrorSchema.setValue(schema);
	};

	var hideResultPanel = function(){
		$('#result-panel').addClass('hide');
		resetSchema();
	};

	var resetSchema = function(){
		if (myCodeMirrorSchema && myCodeMirrorSchema.getValue() != "{\n}"){
    		myCodeMirrorSchema.setValue("{\n}");
    	}
	};

	var modalOntology = function() {
		$('#ontologyData').modal("show");
	};

	var getParentNode = function () {
		var payload = {'id' : $('#ontology').val()};
		payload[csrfParam] = csrfToken;
		jQuery.post('/controlpanel/jsontool/getParentNodeOfSchema',payload, function(data){
			if(data != ""){
				parentNode = data;
				$('#response').text(ontologyHasParentNode);
				$('#returnAction').modal("show");
			}
		});
	};

	var progressBarFileUpload = function(offset, maxSize){
		var percent = (offset/maxSize) *100;
		$('#importProgress').removeClass('progress-bar-success');
		$('#importProgress').removeClass('progress-bar-danger');
		if(offset < maxSize){
			$('#importProgress').attr('aria-valuenow', percent+'%').css('width',percent+'%');
			$('#importProgress').text(percent.toFixed(2)+'%');		
		}else{
			$('#importProgress').attr('aria-valuenow', '100%').css('width','100%');
			$('#importProgress').text('100%');
			$('#importProgress').addClass('progress-bar-success');
			$('#progressBarModal').modal('hide')
		}

	}

	var findArrayNodeInJson = function(object){
		var keys = Object.keys(object);
		var array = [];
		keys.forEach(function(key){
			if(object[key].constructor == Array){
				array = object[key];
				return array;
			}
			else if (object[key].constructor == Object){
				var arr = findArrayNodeInJson(object[key]);
				if (arr.length >0 ){
					array = arr;
					return array;
				}
					
			}
		});
		return array;
		
	};
	var loadJsonFromDoc = function(files){
		var reader = new FileReader();
		var size = files[0].size;
		fileType = files[0].type;
		fileName = files[0].name;
		var chunk_size = Math.pow(2, 13);
	    var chunks = [];
	    var offset = 0;
	    var bytes = 0;
		if(files[0].type == "text/xml"){

			var x2js = new X2JS();
			reader.onloadend = function (e) {
				if(e.target.readyState == FileReader.DONE){
					var chunk = e.target.result;
					bytes += chunk.length;
					
					chunks.push(chunk);
					if(offset < size) {
						offset += chunk_size;
						var blob = files[0].slice(offset , offset + chunk_size);
						reader.readAsText(blob);	

						if (offset > size){
							var content = chunks.join("");
							fileLoaded=x2js.xml_str2json(content);
							var tmpContent = findArrayNodeInJson(fileLoaded);
							fileLoaded = tmpContent;
							generateSchemaFromFile();
						  	document.getElementById("createOnt").disabled = false;
						}
					}
				}
				progressBarFileUpload(offset,size);
			}	

		} else if (files[0].name.indexOf(".csv")!=-1){
	
			reader.onloadend = function (e) {
				if(e.target.readyState == FileReader.DONE){
					var chunk = e.target.result;
					bytes += chunk.length;
					
					chunks.push(chunk);
					if(offset < size){
						offset += chunk_size;
						var blob = files[0].slice(offset , offset + chunk_size);
						reader.readAsText(blob);	
						
						if (offset > size){
							var content = chunks.join("");
							try {
								fileLoaded = csvTojs(content);
							}
							catch(err) {
								$('#response').text(err);
								$('#returnAction').modal("show");
							  	document.getElementById("createOnt").disabled = true;
								return;
							}
							generateSchemaFromFile();
						  	document.getElementById("createOnt").disabled = false;
						}
					}
					progressBarFileUpload(offset,size);
				}
			}

		} else if (files[0].type == "application/json"){   
			reader.onloadend = function (e) {
				
				if(e.target.readyState == FileReader.DONE){
					var chunk = e.target.result;
					bytes += chunk.length;
					
					chunks.push(chunk);
					
					if(offset < size){
						offset += chunk_size;
						var blob = files[0].slice(offset , offset + chunk_size);
						reader.readAsText(blob);	
						
						if (offset > size){
							var content = chunks.join("");
							
							try {
								var jsonData = JSON.parse(content);	
								fileLoaded = jsonData;
							} catch(err) {
								try{
									var firstError = err.message;
									var jsonData = content.replace(/[\r]/g, '');
									var arrayJson = [];
									var dataSplitted = jsonData.split("\n");
									var i;
									for(var i in dataSplitted){
										if(dataSplitted[i] != "") {
											arrayJson.push(JSON.parse(dataSplitted[i]));
										}
									}
									fileLoaded=arrayJson;
								} catch(err){
									
									if (err.message === firstError){
										$('#response').html(firstError);
									} else {
										$('#response').html(firstError+'<br>'+ err.message);
									}
									$('#returnAction').modal("show");
								  	document.getElementById("createOnt").disabled = true;
									return;
								}
							}
							generateSchemaFromFile();
						  	document.getElementById("createOnt").disabled = false;								
						}						
					}
					progressBarFileUpload(offset,size);
				}
			}
		}
		var blob = files[0].slice(offset, offset + chunk_size);
		reader.readAsText(blob);
		$('#progressBarModal').modal("show");
	};

	var checkFileType = function(fileType,fileName){
		var format = $('#resourceFormat').val();
		if (fileType ==  "text/xml" || fileName.indexOf(".xml") != -1){
			return format == "xml";
		} else if (fileType ==  "application/vnd.ms-excel" || fileName.indexOf(".csv") != -1){
			return format == "csv";
		} else if (fileType ==  "application/json" || fileName.indexOf(".json") != -1){
			return format == "json";
		}
		return false;
	};

	var processSubmit = function(form){

		if (!checkFileType(fileType,fileName)) {
			toastr.error(resourceCreateJson.validform.invalidformat,'');
			return false;
	    } 
		
		var isNewOntology = $('#check-new-ontology').is(':checked');
		if(isNewOntology){
			modalOntology();
		}else{
			$('#ontologySchema').val(myCodeMirrorSchema.getValue());
			$('#resourceOntology').val($('#ontology').val());			
			App.blockUI({boxed: true, overlayColor:"#5789ad",type:"loader",state:"warning",message:"Uploading Resource..."});
			form.submit();
		}
	}

	var createResource = function() {
		var RegExPattern = /^[a-zA-Z0-9_]{5,}$/;
		if($('#ontologyDescription').val() == "" || $('#ontologyIdentification').val() == "" || $('#datasource').val() == "" )
		{
			$('#form-new-ontology').addClass("has-error");
//			$('#returnAction').modal("show");
//			$('#response').text(emptyParam);
			toastr.error(messagesForms.operations.genOpError,emptyParam);
			return false;
		}
		else if ($('#ontologyIdentification').val().match(RegExPattern) && $('#ontologyDescription').val().match(RegExPattern))
		{
			$('#form-new-ontology').removeClass("has-error");
		}
		else if (!$('#ontologyIdentification').val().match(RegExPattern)){
			$('#form-new-ontology').addClass("has-error");
//			$('#returnAction').modal("show");
//			$('#response').text(invalidOnto);
			toastr.error(messagesForms.operations.genOpError,invalidOnto);
			return false;
		} else {
			$('#form-new-ontology').addClass("has-error");
			toastr.error(messagesForms.operations.genOpError,invaliddescr);
			return false;
		}
		
		try{
			JSON.parse(myCodeMirrorSchema.getValue());
		}catch(err){
//			$('#response').text(err);
//			$('#returnAction').modal("show");
			toastr.error(messagesForms.operations.genOpError, err);
		}
		$('#ontologySchema').val(myCodeMirrorSchema.getValue());
		$('#resourceOntology').val($('#ontologyIdentification').val());
		App.blockUI({boxed: true, overlayColor:"#5789ad",type:"loader",state:"warning",message:"Uploading Resource..."});
		submitFromModal = true;
		$('#resource_create_form').submit();			
	}

	return{
		init : function(){
			handleValidation();
			initTemplateElements();
			
			// INPUT MASK FOR ontology identification allow only letters, numbers and -_
			$("#ontologyIdentification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
			
			$(function() {
			    $('#ontologyIdentification').on('keypress', function(e) {
			        if (e.which == 32)
			            return false;
			    });
			});
		},
		navigateUrl : function(url){
			navigateUrl(url);
		},
		generateSchemaFromFile : function(){
			generateSchemaFromFile();
		},
		generateSchemaFromURL : function(data){
			generateSchemaFromURL(data);
		},
		hideResultPanel : function(){
			hideResultPanel();
		},
		resetSchema : function(){
			resetSchema();
		},
		modalOntology : function(){
			modalOntology();
		},
		getParentNode : function(){
			getParentNode();
		},
		processSubmit : function(form){
			processSubmit(form);
		},
		createResource : function(){
			createResource();
		},
		loadJsonFromDoc : function(files){
			loadJsonFromDoc(files);
		},
		checkFileType : function(fileType,fileName){
			checkFileType(fileType,fileName);
		}
	}

}();

//AUTO INIT CONTROLLER WHEN READY
jQuery(window).on( "load",function() {		
	// AUTO INIT CONTROLLER.
	ExternalResourcesController.init();	
});