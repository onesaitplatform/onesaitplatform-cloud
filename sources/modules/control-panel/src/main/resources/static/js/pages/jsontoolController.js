var JsonToolController = function(){
	var fileLoaded;
	var parentNode;
	var myCodeMirror;
	var myCodeMirrorSchema;
	var importUrl= '/controlpanel/jsontool/importbulkdata';
	var createUrl= '/controlpanel/jsontool/createontology';
	var csrfParam =  headerJson.csrfParameterName;
	var csrfToken = headerJson.csrfToken;
	var counter= 0;
	var arrayJson;
	var ontologyId;
	var handleCodeMirror = function () {
		
        var myTextArea = document.getElementById('jsonTextArea');
        myCodeMirror = monaco.editor.create(myTextArea, {
    		value: "{\n}",
    		language: 'json',
    		readOnly: false,
    		scrollBeyondLastLine: false,
    		theme: "vs-dark",
    		automaticLayout: true
    	});
        myTextArea = document.getElementById('schemaTextArea');
        myCodeMirrorSchema = monaco.editor.create(myTextArea, {
    		value: "{\n}",
    		language: 'json',
    		readOnly: false,
    		scrollBeyondLastLine: false,
    		theme: "vs-dark",
    		automaticLayout: true
    	});
		
		
    };

    var generateSchema = function (){
    	if(JSON.parse(myCodeMirror.getValue()).length == null){
    		myCodeMirrorSchema.setValue(processJSON(myCodeMirror.getValue()));
    	}else{
    		var json = JSON.parse(myCodeMirror.getValue())[0];
    		myCodeMirrorSchema.setValue(processJSON(JSON.stringify(json)));
    	}
    	
    };

    var beautifyJson = function() {
		myCodeMirror.getAction('editor.action.formatDocument').run()
		//myCodeMirrorJsonImport.setValue(js_beautify(myCodeMirrorJsonImport.getValue()));
	};
	var modalOntology = function() {
		if (myCodeMirrorSchema.getValue()==null || myCodeMirrorSchema.getValue()=="" || myCodeMirrorSchema.getValue()=="{\n}")
			generateSchema();
		$('#ontologyData').modal("show");
	};
	var createOntology = function() {
		myCodeMirrorSchema.setValue(myCodeMirrorSchema.getValue());
		var RegExPattern = /^[a-zA-Z0-9_]*$/;
		if($('#ontologyDescription').val() == "" || $('#ontologyIdentification').val() == "" || $('#datasource').val() == "" )
		{
			$('#form-new-ontology').addClass("has-error");
			$('#returnAction').modal("show");
			$('#response').text(emptyParam);
			return false;
		}
		else if ($('#ontologyIdentification').val().match(RegExPattern))
		{
			$('#form-new-ontology').removeClass("has-error");
		}
		else{
			$('#form-new-ontology').addClass("has-error");
			$('#returnAction').modal("show");
			$('#response').text(invalidOnto);
			return false;
		}
		
		try{
			JSON.parse(myCodeMirrorSchema.getValue());
		}catch(err){
			$('#response').text(err);
			$('#returnAction').modal("show");
		}
		var payload = {'ontologyDescription':$('#ontologyDescription').val(),'ontologyIdentification':$('#ontologyIdentification').val(),'schema': myCodeMirrorSchema.getValue(), 'datasource' : $('#datasource').val()};
		payload[csrfParam] = csrfToken;
		jQuery.post(createUrl,payload, function(data){
			try{
				data = JSON.parse(data);
			}catch(err){
				$('#response').text(err);
				$('#returnAction').modal("show");
			}
			if(data.result=='ok'){
				ontologyId = data.id;
				var importAfterCreation = $('#check-import').is(':checked');
				if(importAfterCreation){
					$('#response').text(ontologyCreated);
					$('#returnAction').modal("show");
					importBulkJson($('#ontologyIdentification').val());
				}else{
					$('#response').text(ontologyCreated);
					$('#returnAction').modal("show");
					navigateUrl('/controlpanel/ontologies/show/' + ontologyId);
				}


			}else{
				//$('#response').text(ontologyExists);
				$('#response').text(data.cause);
				$('#returnAction').modal("show");
			}
			
			
		});
		
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
	var importBulkJson = function(ontology) {
		$('#progressResult').text("");
		if($('#ontology').val() == "" && ontology == null){
			$('#errorSelect').text("Select ontology first");
			$('#ErrorOntSelect').modal("show");
		}else{
			if(ontology == null )
				ontology = $('#ontology').val();
			arrayJson = fileLoaded;
			if(fileLoaded == null) 
				arrayJson = JSON.parse(myCodeMirror.getValue());
			if(parentNode != null){
				//var arrayJson = fileLoaded;
				var newArray = [];
				if(arrayJson.length != null){
					for(var i= arrayJson.length-1; i>=0 ; i--){
						var newObject={};
						if(arrayJson[i][parentNode] == null)
							newObject[parentNode]=arrayJson[i];
						else
							newObject=arrayJson[i];
						newArray.push(newObject);					
					}
					
				}else{
					var newObject={};
					if(arrayJson[parentNode] == null)
						newObject[parentNode]=arrayJson;
					else
						newObject=arrayJson;
					newArray=newObject;
				}
				arrayJson = newArray;
					
				
			}
			if(arrayJson.length != null && arrayJson.length > 200){
			
				counter=0;
				var infLimit=0;
				var supLimit=200;
				var increment =200;
				$('#importProgress').attr('aria-valuenow', '0%').css('width','0%');
				
				$('#importProgress').removeClass('progress-bar-success');
				$('#importProgress').removeClass('progress-bar-danger');
				$('#progressBarModal').modal("show");
				$('#importProgress').text('');
				for(var s=arrayJson.length; s>=0; s--) {
					
					if(infLimit > arrayJson.length){
						break;
					}
					if(supLimit > arrayJson.length){
						supLimit = arrayJson.length;
					}
					var subArray = arrayJson.slice(infLimit,supLimit);
					if(subArray.length != null && subArray.length != 0 ){
//						jQuery.post('/controlpanel/jsontool/importbulkdata', {'data':JSON.stringify(subArray), 'ontologyIdentification': ontology},callbackSlizedImport(data) );
						ajaxImport({'data':JSON.stringify(subArray), 'ontologyIdentification': ontology}).done(callbackSlizedImport).fail(handleError);
					}
					infLimit += increment;
					supLimit += increment;
				}
				
			}else{
				
//				jQuery.post('/controlpanel/jsontool/importbulkdata', {'data':JSON.stringify(arrayJson), 'ontologyIdentification': ontology}, callbackSimpleImport(data));
				ajaxImport({'data':JSON.stringify(arrayJson), 'ontologyIdentification': ontology}).done(callbackSimpleImport).fail(handleError);
			}

		}
		
	}
	
	var ajaxImport = function(payload){
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");

		return $.ajax({
			url : importUrl,
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'POST',
			data : JSON.stringify(payload),
			contentType : "application/json",
			dataType : "text",
		});
	}
	
	var callbackSlizedImport = function(data){
		try {
		    data = JSON.parse(data);
		}
		catch(err) {
			$('#response').text(err);
			$('#returnAction').modal("show");
			return;
		}
		if(data.result != "ERROR"){
			if(data.inserted=="") { data.inserted = 200}
			counter+=Number(data.inserted);
			var percent = (counter/arrayJson.length)*100;
			if(percent > 100) percent = 100;
			
			$('#importProgress').attr('aria-valuenow', percent+'%').css('width',percent+'%');
			$('#importProgress').text(percent.toFixed(2)+'%');
			if(counter >= arrayJson.length){
				//$('#response').text(counter + " ontologies inserted of type " + $('#ontology').val());
				//$('#returnAction').modal("show");
				//$('#importProgress').text('Completed');
				$('#importProgress').addClass('progress-bar-success');
				$('#progressResult').text(arrayJson.length + ' ontologies inserted.');
				if(ontologyId !=null) 
					navigateUrl('/controlpanel/ontologies/show/' + ontologyId)
				else
					navigateUrl('/controlpanel/ontologies/list')
			}
		}else{
			$('#importProgress').removeClass('active');
			$('#importProgress').addClass('progress-bar-danger');
			$('#importProgress').attr('aria-valuenow', '100%').css('width','100%');
			$('#importProgress').text(data.cause);
		}
		
	}
	
	
	
	var callbackSimpleImport = function(data){
		try {
		    data = JSON.parse(data);
		}
		catch(err) {
			$('#response').text(err);
			$('#returnAction').modal("show");
			return;
		}
		if(data.result == "ok"){
			try {
				if(JSON.parse(myCodeMirror.getValue()).length != null || JSON.parse(myCodeMirror.getValue()).length > 1){
					$('#response').text(arrayJson.length + " ontologies inserted of type " + $('#ontology').val());
				}else{
					$('#response').text("Ontology inserted of type " + $('#ontology').val());
				}
				$('#returnAction').modal("show");
				if(ontologyId !=null) 
					navigateUrl('/controlpanel/ontologies/show/' + ontologyId)
				else
					navigateUrl('/controlpanel/ontologies/list')
			}
			catch(err) {
				$('#response').text(err);
				$('#returnAction').modal("show");
				return;
			}
		}else{
			$('#response').text(data.cause);
			$('#returnAction').modal("show");
		}
		
	}
	
	var handleError = function (jqXHR, textStatus, error) {
		console.log(error);
		$.alert({title: 'ERROR!',type: 'red' , theme: 'light', content: error});
    }
	
	var printJson = function(){
	
		if(fileLoaded.length > 100){
			myCodeMirror.setValue(JSON.stringify(fileLoaded.slice(0,20)));
		}else{
			myCodeMirror.setValue(JSON.stringify(fileLoaded));
		}
		beautifyJson();
		
	};
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
									//xml2json parses into object,  need to get array instances of this object
									/*if(fileLoaded.length == null){
										var foundJson = false;
										while(!foundJson){
											var key = Object.keys(fileLoaded);
											fileLoaded = fileLoaded[key];
											if(fileLoaded.length != null)
												foundJson = true;
											
										}
									}*/
									//end
									var tmpContent = findArrayNodeInJson(fileLoaded);
									fileLoaded = tmpContent;
									printJson();
								}
								
							}
							
						}
						progressBarFileUpload(offset,size);
					}	

			}else if (files[0].name.indexOf(".csv")!=-1){
		
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
								var content = chunks.join("");//.replace(/\"/g, '');
								try {
									fileLoaded = csvTojs(content);
								}
								catch(err) {
									$('#response').text(err);
									$('#returnAction').modal("show");
									return;
								}
								printJson();
							}
							
						}
						
						progressBarFileUpload(offset,size);
					}
				
					
				}

			}else if (files[0].type == "application/json"){


			    
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
								}
								catch(err) {
									try{
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
									}catch(err){
										
										$('#response').text(err);
										$('#returnAction').modal("show");
										return;
									}
								}							
							
							printJson();								
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

	var processSubmit = function(){
		var isNewOntology = $('#check-new-ontology').is(':checked');
		var importData = $('#check-import').is(':checked');
		if(isNewOntology){
			modalOntology();
		}else{
			if(importData) 
				importBulkJson();
		}

	}

	return{

		init : function(){
			handleCodeMirror();
			$(function() {
			    $('#ontologyIdentification').on('keypress', function(e) {
			        if (e.which == 32)
			            return false;
			    });
			});
		},
		generateSchema : function(){
			generateSchema();
		},
		beautifyJson : function(){
			beautifyJson();
		},
		modalOntology : function(){
			modalOntology();
		},
		createOntology : function(){
			createOntology();
		},
		getParentNode : function(){
			getParentNode();
		},
		processSubmit : function(){
			processSubmit();
		},
		importBulkJson : function(ontology){
			importBulkJson(ontology);
		},
		loadJsonFromDoc :function(files){
			loadJsonFromDoc(files);
		}


	}

}();

//AUTO INIT CONTROLLER WHEN READY
jQuery(window).on( "load",function() {
	
		
	// AUTO INIT CONTROLLER.
	JsonToolController.init();
	
	
});