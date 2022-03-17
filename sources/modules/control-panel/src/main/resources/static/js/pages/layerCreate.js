
var LayerCreateController = function() {
	
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'Onesait Platform Control Panel'; 
	var LIB_TITLE = 'Layer Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var hasId = false; // instance
	var attributesArray = [];
	var fieldsArray = [];
	var operationsArray = [];
	var ontologyFields = [];
	var queryFields = [];
	var mapOperations = [];
	var queryParams=[];
	var isVirtual = false;
	// CONTROLLER PRIVATE FUNCTIONS	--------------------------------------------------------------------------------

	var propertyTypeOntologyIndex=-1;
	
	var cleanFields = function(formId) {
		logControl ? console.log('cleanFields() -> ') : '';
		
		// CLEAR OUT THE VALIDATION ERRORS
		$('.help-block-error').hide();
		$('.has-error').removeClass('has-error');
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
		// UNCHECK PUBLIC OPTION
		$("#public").prop("checked", false);		
		// UNCHECK IS QUERY
		$("#isQuery").prop("checked", false);		
		// UNCHECK HEAT MAP
		$("#isHeatMap").prop("checked", false);		
		// HIDE HEAT MAP DEF
		$("#heatMapDiv").hide();
		$("#tab-symbology").removeClass('disabledTab')
		$("#tab-infobox").removeClass('disabledTab');		
		// HIDE QUERY DEF
		$("#query_def").hide();		
		// HIDE GEOMETRY TYPE
		$("#geometryTypes").css("visibility", "hidden");		
		// UNCHECK GEOMETRY FILTER
		$("#filter").prop("checked", false);		
		// HIDE FILTERS
		$("#withFilter").hide();				
		// CLEAR FITERS ATTRIBUTES
		$("#filtersAttribute").find("tbody").empty();		
		// CLEAR ATTRIBUTES
		$("#attributes").find("tbody").empty();
	}

	
	
	// CHECK IF JSON STRING WHEN JSON PARSE IS OK OR NOT, IF THE JSON IS MALFORMED THE JSON.parse() is broken.
	var IsJsonString = function(str) {
		try {
			JSON.parse(str);
		}
		catch (e) {	return false; }
		
		return true;
	}
	
	$("#isHeatMap").on("click", function(){
		if($("#isHeatMap").is(":checked")){
			$("#tab-symbology").addClass('disabledTab');
			$("#tab-infobox").addClass('disabledTab');
			$("#heatMapDiv").show();
			
				
			$("#weightField").empty();
			var fields = ontologyFields;
			$("#weightField").append('<option id="'+field+'" name="'+type+'" value="select">'+layerCreateJson.layerselect+'</option>');
			for (var key in fields){
		        var field = key;
		        var type = fields[key];
		        
		        $("#weightField").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
		    }
		}else{
			$("#tab-symbology").removeClass('disabledTab')
			$("#tab-infobox").removeClass('disabledTab');
			$("#heatMapDiv").hide();
		}
	});
	
	$("#isQuery").on("click", function(){
		if($("#isQuery").is(":checked")){
			$("#attributes").find("tbody").empty()
			$("#tab-infobox").addClass('disabledTab');
			var ontology = $("#ontology").val()
			$("#query").val("select c from " + ontology + " as c");
			$("#query_def").show();
		}else{
			$("#tab-infobox").removeClass('disabledTab');
			$("#query_def").hide();
		}
	});
	
    function loadParamsCustomValues(){
    	
    	var labels = $('#customsql_paramsquery fieldset label');
    	var params = [];
    	for(var i=0; i<labels.length; i++){
    		var label = labels.get(i);
    		var param = label.attributes.id.value;
    		
    		var type = $('[name= "customsqlParamType_'+param+"\"").val();
    		var value = $('[name= "customsqlParamDefault_'+param+"\"").val();
    		
    		if(!params.includes(param)){
    			params.push(param);
        		queryParams.push({'param':param,'type':type, 'default':value});
    		}
    	}
    }
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// DELETE LAYER
	var deleteLayer = function(layerId){
		console.log('deleteLayerConfirmation() -> formId: '+ layerId);

		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		// no Id no fun!
		if ( !layerId ) {toastr.error(ontologyCreateReg.validations.validform,''); return false; }
		
		$.ajax({
			url : "/controlpanel/layers/isLayerInUse/" + layerId,
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'GET',
			dataType: 'text', 
			contentType: 'text/plain',
			mimeType: 'text/plain',
			async : false,
			success : function(isLayerInUse) {
				
				if(isLayerInUse === "false"){
					logControl ? console.log('deleteLayerConfirmation() -> formAction: ' + $('.delete-layer').attr('action') + ' ID: ' + $('#delete-layerId').attr('layerId')) : '';

					// call ontology Confirm at header.
					HeaderController.showConfirmDialogLayer('delete_layer_form');
				}else{
					
					toastr.error(layerCreateJson.deleteError,'');
				}
				
			},
			error : function(data, status, er) {
				 
				toastr.error(er,'');
			}
		});

		
	}
	
	var uuidv4 = function() {
		  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
		    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
		    return v.toString(16);
		  });
		}
	
	var handleValidation =  function() {
        // for more info visit the official plugin documentation:
        // http://docs.jquery.com/Plugins/Validation

        var form1 = $('#layer_create_form');
         


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
				fields:		{ required: true },
				types:		{ required: true },
				geometryType:	{ required: true },
				geometryTypeVirtual: 	{ required: true }
            },
            invalidHandler: function(event, validator) { // display error alert on form submit
            	toastr.error(messagesForms.validation.genFormError,'');
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
                
                var geometryField = $("#fields").val();
                
                if(geometryField == null || geometryField==undefined || geometryField=="select"){
                	
					toastr.error(layerCreateJson.validations.geometry,'');
                	return;
                }
                
				if($("#filter").is(":checked")){
					var filters=[];
		   			 $.each($("#filtersAttribute tbody tr"), function(k,v){
		   			     var operation=$(v).find('td')[0].innerHTML;
		   				 var color=$(v).find('td')[1].innerHTML;
		   				filters.push({"operation":operation, "color": color});
		   		        
		   		     });
		   			 
		   			var stringFilters = JSON.stringify(filters);
		   			stringFilters = stringFilters.replace(/'/g, '\\\"');
		   			$("<input type='hidden' name='filters' value='"+stringFilters+"' />")
	  		         .appendTo("#layer_create_form");
				}
				
				if($("#isQuery").is(":checked")){
					loadParamsCustomValues();
					 $("<input type='hidden' name='queryParams' value='"+JSON.stringify(queryParams)+"' />")
	   		         .appendTo("#layer_create_form");
				}
                
                
    			var infoBox=[];
	   			 $.each($("#attributes tbody tr"), function(k,v){
	   			     var field=$(v).find('td')[0].innerHTML;
	   				 var attribute=$(v).find('td')[1].innerHTML;
	   				 infoBox.push({"field":field, "attribute": attribute});
	   		        
	   		     });
	   			
	   			 $("<input type='hidden' name='infoBox' value='"+JSON.stringify(infoBox)+"' />")
   		         .appendTo("#layer_create_form");
	   			 
	   			$("<input type='hidden' name='isPublic' value='"+$("#public").is(":checked")+"' />")
  		         .appendTo("#layer_create_form");
	   			
	   			$("<input type='hidden' name='isFilter' value='"+$("#filter").is(":checked")+"' />")
 		         .appendTo("#layer_create_form");
	   			
	   			$("<input type='hidden' name='isHeatMap' value='"+$("#isHeatMap").is(":checked")+"' />")
 		         .appendTo("#layer_create_form");
	   			
	   			$("<input type='hidden' name='isQuery' value='"+$("#isQuery").is(":checked")+"' />")
 		         .appendTo("#layer_create_form");
	   			
	   			if(isVirtual){
	   				$("<input type='hidden' name='geometryType' value='"+$("#geometryTypeVirtual").val()+"' />")
	 		         .appendTo("#layer_create_form");
	   			}else{
	   				$("<input type='hidden' name='geometryType' value='"+$("#geometryType").val()+"' />")
	 		         .appendTo("#layer_create_form");
	   			}
	   			
				// form.submit();
				form1.ajaxSubmit({type: 'post', success : function(data){
					toastr.success(messagesForms.validation.genFormSuccess,'');
					navigateUrl(data.redirect);
					
					}, error: function(data){
						toastr.error(data.responseJSON.cause);
						//HeaderController.showErrorDialog(data.responseJSON.cause)
					}
				})
			}
        });
    }
	
    function loadParamsFromQuery(field, op_name) {
        clearParams();
        if (op_name==null || op_name==""){
            var error = "";
            if (field != null && field !=""){
                error = isValidQuery(field);
            }
            if (error==""){
            	showParams(field);
            	if(field.includes("$")){
            		field=field.replace("{$","").replace("}","");
            	}
            	loadParamsToFilter(field);
            } else {
            	HeaderController.showErrorDialog(error)
            }
        } else {
        	showParams(field);
        }
    }
    
    function loadParamsToFilter(query){
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
    	$.ajax({
			url: '/controlpanel/layers/getQueryFields',
			headers: {
				[csrf_header]: csrf_value
		    },
			type:"POST",
			async: true,
			data: { 'query': query, 'ontology': $("#ontology").val()},
			dataType:"json",
			success: function(response,status){
				queryFields=[];
				queryFields = response;
				
			}
		});
    }
    
    function showParams(query) {
		 var param = "";
		 customsql_queryparam = new Array();
		 while (query.indexOf("{$")>0 && query.indexOf("}")!=-1){
			 var param = query.substring(query.indexOf("{$") + 2, query.indexOf("}", query.indexOf("{$")));
		
			 if (param.indexOf(":")==-1){
				 loadParamQuery(param);
				 query = query.substring(query.indexOf("}", query.indexOf("{$")) + 1);
			 } else {
			    query = query.substring(query.indexOf("{$") + 2, query.length);
			 }
		 }
		 if (customsql_queryparam.length>0){
			$("#customsql_noparam_div").css({ 'display': "none" });
			$("#customsql_params_div").css({ 'display': "block" });
		 } else {
		 	$("#customsql_params_div").css({ 'display': "none" });
		 	$("#customsql_noparam_div").css({ 'display': "block" });
		 }
   }
    
    function loadParamQuery(param) {
    	
        var customsqlParamaDiv=document.getElementById("customsql_paramsquery");

        var newCustomsqlParamDiv = document.createElement('div');
        newCustomsqlParamDiv.id= "customsql_param_" + param;

        var newCustomsqlParamFieldSet = document.createElement('fieldset');
        newCustomsqlParamFieldSet.id = "customsql_param_fieldset" + param;

        newCustomsqlParamFieldSet.style.margin="10px";
        newCustomsqlParamFieldSet.style.marginTop="10px";
        newCustomsqlParamFieldSet.style.padding="10px";
        newCustomsqlParamFieldSet.style.border="1px #d0d2d9 dotted";
        newCustomsqlParamFieldSet.style.display="inline";

        var newLabelCustomsqlParam = document.createElement('label');
        newLabelCustomsqlParam.id = param;
        newLabelCustomsqlParam.className="description";
        newLabelCustomsqlParam.style.marginRight="20px";
        newLabelCustomsqlParam.innerHTML=param;

        newCustomsqlParamFieldSet.appendChild(newLabelCustomsqlParam);

        var newInputCustomsqlParam = document.createElement('select');
        newInputCustomsqlParam.name="customsqlParamType_" + param;
        newInputCustomsqlParam.style.cssFloat="right";

        var optionString = document.createElement( 'option' );
        optionString.value = "STRING"; 
        optionString.text = "STRING";
        newInputCustomsqlParam.add(optionString);
        var optionNumber = document.createElement( 'option' );
        optionNumber.value = "NUMBER"; 
        optionNumber.text = "NUMBER";
        newInputCustomsqlParam.add(optionNumber);
        var optionDate = document.createElement( 'option' );
        optionDate.value = "DATE"; 
        optionDate.text = "DATE";
        newInputCustomsqlParam.add(optionDate);
        var optionBoolean = document.createElement( 'option' );
        optionBoolean.value = "BOOLEAN"; 
        optionBoolean.text = "BOOLEAN";
        newInputCustomsqlParam.add(optionBoolean);

        newCustomsqlParamFieldSet.appendChild(newInputCustomsqlParam);

        customsqlParamaDiv.appendChild(newCustomsqlParamFieldSet);
        
        var newCustomsqlParamDivDefault = document.createElement('div');
        newCustomsqlParamDivDefault.id= "customsql_param_default_" + param;
        
        var newLabelCustomsqlParamDefault = document.createElement('label');
        newLabelCustomsqlParamDefault.id = param;
        newLabelCustomsqlParamDefault.className="description";
        newLabelCustomsqlParamDefault.style.marginRight="20px";
        newLabelCustomsqlParamDefault.innerHTML="Default Value";
        
        newCustomsqlParamDivDefault.appendChild(newLabelCustomsqlParamDefault);
        
        var newInputCustomsqlParamDefault = document.createElement('input');
        newInputCustomsqlParamDefault.name="customsqlParamDefault_" + param;
        newInputCustomsqlParamDefault.className="form-control";
        
        newCustomsqlParamDivDefault.appendChild(newInputCustomsqlParamDefault);
        
        newCustomsqlParamFieldSet.appendChild(newCustomsqlParamDivDefault);

        var parameter = {name: param, condition: "REQUIRED", dataType: $('[name="customsqlParamType_' + param+"\"").val(), description: ""};
        customsql_queryparam.push(parameter);
    }
    
    function isValidQuery(field){
    	if (field != null && field !=""){
			$('#id_customsql_querytype').removeAttr("disabled");
			if (field.toUpperCase().indexOf("SELECT")>=0){
	    			if ($("#ontology").val()!= "" && field.indexOf($("#ontology").val())>=0){
	    				if ((field.split("{$").length - 1)==(field.split("}").length - 1)){
	    					return "";
	    				} else {
	    					return (apiCustomOpsReg.apimanager_customsql_error_query_params);
	    				}
	    			} else {
	    				return (apiCustomOpsReg.apimanager_customsql_error_query_ontology);
	    			}
	    		} else {
	    			return (apiCustomOpsReg.apimanager_customsql_error_query);
	    		}
    		
    	} else {
    		return (apiCustomOpsReg.apimanager_customsql_error_required);
    	}
    }
    
    function clearParams() {
    	$("#customsql_paramsquery").html("");
    	$("#customsql_params_div").css({ 'display': "none" });
    	$("#customsql_noparam_div").css({ 'display': "block" });
    }
	
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return layerCreateJson = Data;
		},
		loadMessages: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return apiCustomOpsReg = Data;
		},
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleValidation();
			
			// INPUT MASK FOR ontology identification allow only letters, numbers and -_
			$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
			
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
			
			$("#addAttribute").on("click", function(){
				LayerCreateController.showAttributeDialog();
			});
			
			$("#addFilter").on("click", function(){
				LayerCreateController.showFilterDialog();
			});
			
			$("#filter").on("click", function(){
				if($("#filter").is(":checked")){
					$("#withFilter").show();
					$("#withoutFilter").show();
				}else{
					$("#withFilter").hide();
					$("#withoutFilter").show();
				}
				
			});
			
			// INSERT MODE ACTIONS  (ontologyCreateReg.actionMode = NULL ) 
			if ( layerCreateJson.actionMode === null){
				logControl ? console.log('|---> Action-mode: INSERT') : '';
				$('.formcolorpicker').each(function () {
				    $(this).colorpicker({
			            color: null
			        });
				});
				
			
			}
			// EDIT MODE ACTION 
			else {
				$.ajax({
					url : "/controlpanel/layers/hasOntRoot", 
					//contentType: "application/json",
					data : {'ontologyID': $("#ontology").val()},
					type : "POST",
					headers: {
						[csrf_header]: csrf_value
				    },
				    success: function(response, status){
				    	if(response=='virtual'){
				    		isVirtual=true;
				    	}
					}
				});
				var csrf_value = $("meta[name='_csrf']").attr("content");
				var csrf_header = $("meta[name='_csrf_header']").attr("content");
				$.ajax({
					url: '/controlpanel/layers/getOntologyFields',
					headers: {
						[csrf_header]: csrf_value
				    },
					type:"POST",
					async: true,
					data: { 'ontologyIdentification': $("#ontology").val()},
					dataType:"json",
					success: function(response,status){
						if(response.FeatureCollection == "FeatureCollection"){
							$("#tab-infobox").addClass('disabledTab');
							$("#tab-query").addClass('disabledTab');
						}else{
							ontologyFields = response;
						}
					}
				});
				
				logControl ? console.log('|---> Action-mode: UPDATE') : '';
				
				if(layerCreateJson.isPublic){
					$("#public").attr("checked", "checked");
				}
				
				if(layerCreateJson.isFilter){
					$("#filter").attr("checked", "checked");
					$("#withFilter").show();
					$("#withoutFilter").show();
					
					var filters = JSON.parse(layerCreateJson.filters);
					
					if(filters!=null){
						$.each(filters, function(k,v){
							var operation = v.operation;
			            	var color = v.color;
			            	
			            	operationsArray.push(operation);
			            	
			            	LayerCreateController.checkOperation(operation, color);
						});
					}
					
				}else{
					$("#withFilter").hide();
					$("#withoutFilter").show();
					
				}
				
				spinnerEachFrom = $("#inner_thinckness").TouchSpin({
					min: 0,
					max: 999.0,
					stepinterval: 0.2,
					maxboostedstep: 999.0,
					verticalbuttons: true,
					postfix: 'px'
				});			
				
				($("#inner_thinckness").val() == "") ? $("#inner_thinckness").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				
				spinnerEachFrom = $("#outer_thinckness").TouchSpin({
					min: 0.0,
					max: 999.0,
					stepinterval: 0.2,
					maxboostedstep: 999,
					verticalbuttons: true,
					postfix: 'px'
				});			
				
				($("#outer_thinckness").val() == "") ? $("#outer_thinckness").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				
				spinnerEachFrom = $("#size").TouchSpin({
					min: 0.0,
					max: 999.0,
					stepinterval: 0.2,
					maxboostedstep: 999,
					verticalbuttons: true,
					postfix: 'px'
				});			
				
				($("#size").val() == "") ? $("#size").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); })
				
				spinnerEachFrom = $("#refresh").TouchSpin({
					min: 0,
					max: 999,
					stepinterval: 1,
					maxboostedstep: 999,
					verticalbuttons: true,
					postfix: 's'
				});			
				
				($("#refresh").val() == "") ? $("#refresh").val(0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				$("#refresh").TouchSpin({
					initval: layerCreateJson.refreshTime
				});
				
			    $("#innerColor").colorpicker({
		            color: layerCreateJson.innerColor
		        });
			    
			    $("#outerColor").colorpicker({
		            color: layerCreateJson.outerColor
		        });
				
				$("#geometryTypes").css('visibility', 'visible');
				$("#identification").attr("disabled", "disabled");
				
				if(layerCreateJson.query!=null){
					
					$("#tab-infobox").addClass('disabledTab');
					$("#query").val(layerCreateJson.query);
					$("#isQuery").attr("checked", "checked");
					$("#query_def").show();
					$("#filter_div").hide();
					
					LayerCreateController.loadParamsQuery(layerCreateJson.query);
					
					var queryParamsAux = JSON.parse(layerCreateJson.queryParams);
					
					$.each(queryParamsAux, function(k,v){
						var param = v.param;
		            	var type = v.type;
		            	var value = v.default;
		            	
		            	$('[name= "customsqlParamType_'+param+"\"").val(type);
		            	$('[name= "customsqlParamDefault_'+param+"\"").val(value);
					});
				}
				
				if(layerCreateJson.isHeatMap){
					$("#tab-symbology").addClass('disabledTab')
					$("#tab-infobox").addClass('disabledTab')
					$("#heatMapDiv").show();
					$("#checkHeatMap").show();
					$("#isHeatMap").attr("checked", "checked");
					spinnerEachFrom = $("#min").TouchSpin({
						min: 0,
						max: 999.0,
						stepinterval: 0.2,
						maxboostedstep: 999.0,
						verticalbuttons: true
					});			
					
					($("#min").val() == "") ? $("#min").val(parseInt(layerCreateJson.heatMapMin)) : null;		
					spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
					
					spinnerEachFrom = $("#max").TouchSpin({
						min: 0.0,
						max: 999.0,
						stepinterval: 0.2,
						maxboostedstep: 999,
						verticalbuttons: true
					});			
					
					($("#max").val() == "") ? $("#max").val(parseInt(layerCreateJson.heatMapMax)) : null;		
					spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
					
					spinnerEachFrom = $("#radius").TouchSpin({
						min: 0.0,
						max: 9999.0,
						stepinterval: 0.2,
						maxboostedstep: 9999,
						verticalbuttons: true,
						postfix: 'px'
					});			
					
					($("#radius").val() == "") ? $("#radius").val(parseInt(layerCreateJson.heatMapRadius)) : null;		
					spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
					
					
					$("#weightField").empty();
					var fields = ontologyFields;
					$("#weightField").append('<option id="'+field+'" name="'+type+'" value="select">'+layerCreateJson.layerselect+'</option>');
					for (var key in fields){
				        var field = key;
				        var type = fields[key];
				        
				        $("#weightField").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
				    }
					$("#weightField").val(layerCreateJson.weightField);
						
					
				}
				
				var csrf_value = $("meta[name='_csrf']").attr("content");
				var csrf_header = $("meta[name='_csrf_header']").attr("content");
				
				$.ajax({
					url: '/controlpanel/layers/getOntologyGeometryFields',
					headers: {
						[csrf_header]: csrf_value
				    },
					type:"POST",
					async: true,
					data: { 'ontologyIdentification': $("#ontology").val()},
					dataType:"json",
					success: function(response,status){
						$("#fields").empty();
						var fields = response;
						$("#fields").append('<option id="'+field+'" name="'+type+'" value="select">'+layerCreateJson.layerselect+'</option>');
						for (var key in fields){
					        var field = key;
					        var type = fields[key];
					        $("#fields").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
					        
					    }
						
						$("#fields").val(layerCreateJson.geometryField);
						LayerCreateController.changeField();
					}
				});
				
				
				$("#types").removeAttr("disabled");
				$("#lon_lat").removeAttr("disabled");
				
				var infoB = JSON.parse(layerCreateJson.infobox);
				
				if(infoB != null){
					$.each(infoB, function(k,v){
						var field = v.field;
		            	var attribute = v.attribute;
		            	
		            	attributesArray.push(attribute);
		            	fieldsArray.push(field);
		            	
		            	LayerCreateController.checkAttribute(attribute, field);
					});
				}
				
			}
			$('#resetBtn').on('click', function() {
				cleanFields('layer_create_form');
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
		deleteAttribute: function(obj){
			
			var field = $(obj).closest('tr').find(".field").text();
			var attribute = $(obj).closest('tr').find(".attribute").text();
			var index = attributesArray.indexOf(attribute);
			if (index > -1) {
				attributesArray.splice(index, 1);
			}
			var index = fieldsArray.indexOf(field);
			if (index > -1) {
				fieldsArray.splice(index, 1);
			}
			$(obj).closest('tr').remove();
		},
		editAttribute: function(obj){
			var field_edit = $(obj).closest('tr').find(".field").text();
			var attribute_edit = $(obj).closest('tr').find(".attribute").text();
			$.confirm({
				async: false,
			    title: layerCreateJson.newattribute,
			    content: '' +
			    '<form action="" class="formName">' +
			    '<div class="form-group col-md-12" id="parameter_info">' +
			    '<label>' + layerCreateJson.field + '</label> <select id="fields_pop" class="form-control" data-width="100%">'+
				'</select>'+
				'<label>'+ layerCreateJson.attribute +'</label> <input type="text" name="field[]" id="attribute" value="" class="form-control"/></div>' +
			    '</form>',
			    buttons: {
			        formSubmit: {
			            text: 'OK',
			            btnClass: 'btn-blue',
			            action: function () {
			            	var field = $("#fields_pop").val();
			            	var attribute = $("#attribute").val();
			            	
			            	if(field_edit!=field){
			            		var index = fieldsArray.indexOf(field_edit);
			            		if (index > -1) {
			            			fieldsArray.splice(index, 1);
			            		}
			            		fieldsArray.push(field);
			            		
			            	}
			            	if(attribute_edit!=attribute){
			            		var index = attributesArray.indexOf(attribute_edit);
			            		if (index > -1) {
			            			attributesArray.splice(index, 1);
			            		}
			            		attributesArray.push(attribute);
			            		
			            	}
			            	
			            	$(obj).closest('tr').remove();
			            	LayerCreateController.checkAttribute(attribute, field);
			            }
			            	
			        },
			        cancel: function () {
			        },
			    },
			    onContentReady: function () {
			    	
			    	$("#fields_pop").empty();
					var fields = ontologyFields;
					$("#fields_pop").append('<option id="'+field+'" name="'+type+'" value="select">'+layerCreateJson.layerselect+'</option>');
					for (var key in fields){
				        var field = key;
				        var type = fields[key];
				        
				        $("#fields_pop").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
				    }
					$("#fields_pop").val(field_edit);
					$("#attribute").val(attribute_edit);
			    }
			});
		},
		deleteFilter: function(obj){
			
			var operation = $(obj).closest('tr').find(".operation").text();
			var color = $(obj).closest('tr').find(".color").text();
			var field;
			var operator;
			var value;
			
			if(operation.includes('==')){
				var split = operation.split('==')
				field = split[0];
				operator = '==';
				value= split[1];
				
			}else if(operation.includes('!=')){
				var split = operation.split('!=')
				field = split[0];
				operator = '!=';
				value= split[1];
				
			}else if(operation.includes('>')){
				var split = operation.split('>')
				field = split[0];
				operator = '>';
				value= split[1];
				
			}else if(operation.includes('<')){
				var split = operation.split('<')
				field = split[0];
				operator = '<';
				value= split[1];
				
			}
			
        	var operation = field + operator + value;
        	
        	var index = operationsArray.indexOf(operation);
    		if (index > -1) {
    			operationsArray.splice(index, 1);
    		}
			
			$(obj).closest('tr').remove();
		},
		editFilter: function(obj){
			var operation = $(obj).closest('tr').find(".operation").text();
			var colorFilter = $(obj).closest('tr').find(".color").text();
			var field;
			var operator;
			var value;
			
			if(operation.includes('==')){
				var split = operation.split('==')
				field = split[0];
				operator = '==';
				value= split[1];
				
			}else if(operation.includes('!=')){
				var split = operation.split('!=')
				field = split[0];
				operator = '!=';
				value= split[1];
				
			}else if(operation.includes('>')){
				var split = operation.split('>')
				field = split[0];
				operator = '>';
				value= split[1];
				
			}else if(operation.includes('<')){
				var split = operation.split('<')
				field = split[0];
				operator = '<';
				value= split[1];
				
			}
			
			if(value.includes("'")){
				value = value.substring(1, value.length-1);
			}
			
			$.confirm({
				async: false,
			    title: layerCreateJson.newattribute,
			    content: '' +
			    '<div class="form-group col-md-12" id="filter_info">' +
			    '<label>' + layerCreateJson.field + '</label> <select id="fields_pop" class="form-control" data-width="100%">'+
				'</select>'+
				'<label>' + layerCreateJson.operator + '</label> <select id="operator" class="form-control" data-width="100%">'+
				'<option value="==">==</option>'+
				'<option value=">">></option>'+
				'<option value="<"><</option>'+
				'<option value="!=">!=</option>'+
				'</select>'+
				'<label>'+ layerCreateJson.value +'</label> <input type="text" id="field_filter_value" name="value[]" id="attribute" value="" class="form-control"/></div>' +
				'<label>Color</label>'+
				'<div class="input-group colorpicker-component formcolorpicker" id="filterColor">'+
					'<label class="" th:text="#{layer.symbology.color} + ":""></label>'+
					'<input type="text" name="filter_color" id="filter_color" value="#000" class="form-control" />' +
				    '<div class="input-group-append" style="display: -webkit-box;">'+
				        '<span class="input-group-text input-group-addon"><i style="height: 15px;"></i></span>'+
				    '</div>'+
				'</div>'+
				'</div>',
			    buttons: {
			        formSubmit: {
			            text: 'OK',
			            btnClass: 'btn-blue',
			            action: function () {
			            	var field = $("#fields_pop").val();
			            	var operator = $("#operator").val();
			            	var value= $("#field_filter_value").val();
			            	
			            	if(isNaN(value)){
			            		value = "'" + value + "'";
			            	}
			            	
			            	var operation = field + operator + value;
			            	
			            	var index = operationsArray.indexOf(operation);
		            		if (index > -1) {
		            			operationsArray.splice(index, 1);
		            		}
		            		operationsArray.push(operation);
			            	
			            	$(obj).closest('tr').remove();
			            	LayerCreateController.checkOperation(operation, $("#filter_color").val());
			            }
			            	
			        },
			        cancel: function () {
			        },
			    },
			    onContentReady: function () {
			    	
			    	$("#filterColor").colorpicker({
			            color: colorFilter
			        });
			    	
			    	$("#fields_pop").empty();
					var fields = ontologyFields;
					$("#fields_pop").append('<option id="'+field+'" name="'+type+'" value="select">'+layerCreateJson.layerselect+'</option>');
					for (var key in fields){
				        var fieldAux = key;
				        var type = fields[key];
				        
				        $("#fields_pop").append('<option id="'+fieldAux+'" name="'+type+'" value="'+ fieldAux +'">' + fieldAux +'</option>');
				    }
					
					$("#fields_pop").val(field);
					$("#operator").val(operator);
					$("#field_filter_value").val(value);
					$("#filterColor").find(".input-group-append").css({"margin-top":-9});
					
			    }
			});
		},
		
		controlOntology: function(){

			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			
			$.ajax({
				url : "/controlpanel/layers/hasOntRoot", 
				//contentType: "application/json",
				data : {'ontologyID': $("#ontology").val()},
				type : "POST",
				headers: {
					[csrf_header]: csrf_value
			    },
			    success: function(response, status){
			    	if(response=='virtual'){
			    		isVirtual=true;
			    	}else{
			    		LayerCreateController.changeOntology();
			    	}
			    
				}
			});
			
		},
		
		
		changeOntology: function(){

			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");	
			
			spinnerEachFrom = $("#inner_thinckness").TouchSpin({
				min: 0,
				max: 999.0,
				stepinterval: 0.2,
				maxboostedstep: 999.0,
				verticalbuttons: true,
				postfix: 'px'
			});			
			
			($("#inner_thinckness").val() == "") ? $("#inner_thinckness").val(0.0) : null;		
			spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
			
			spinnerEachFrom = $("#outer_thinckness").TouchSpin({
				min: 0.0,
				max: 999.0,
				stepinterval: 0.2,
				maxboostedstep: 999,
				verticalbuttons: true,
				postfix: 'px'
			});			
			
			($("#outer_thinckness").val() == "") ? $("#outer_thinckness").val(0.0) : null;		
			spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
			
			spinnerEachFrom = $("#size").TouchSpin({
				min: 0.0,
				max: 999.0,
				stepinterval: 0.2,
				maxboostedstep: 999,
				verticalbuttons: true,
				postfix: 'px'
			});			
			
			($("#size").val() == "") ? $("#size").val(0.0) : null;		
			spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
			
			spinnerEachFrom = $("#refresh").TouchSpin({
				min: 0,
				max: 999,
				stepinterval: 1,
				maxboostedstep: 999,
				verticalbuttons: true,
				postfix: 's'
			});			
			
			($("#refresh").val() == "") ? $("#refresh").val(0) : null;		
			spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
			
			$("#refresh").TouchSpin({
				initval: 0
			});
			
			$.ajax({
				url: '/controlpanel/layers/getOntologyGeometryFields',
				headers: {
					[csrf_header]: csrf_value
			    },
				type:"POST",
				async: true,
				data: { 'ontologyIdentification': $("#ontology").val()},
				dataType:"json",
				success: function(response,status){
					
					if(Object.entries(response).length === 0 && response.constructor === Object){
						
						toastr.error(layerCreateJson.validations.ontology,'');
	                	return;
					}
					$("#fields").empty();
					
					var fields = response;
					$("#fields").append('<option value="select">'+layerCreateJson.layerselect+'</option>');
					for (var key in fields){
				        var field = key;
				        var type = fields[key];
				        
				        if(type=='geometry'){
				        	$("#fields").append('<option name="'+type+'" id="'+field+'" value="'+ field +'">' + field +'</option>');
				        	$("#fields").val(field);
				        }
				        
				        $("#fields").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
				    }
					
					$("#geometryTypes").css('visibility', 'visible');

				}
			});
			
			$.ajax({
				url: '/controlpanel/layers/getOntologyFields',
				headers: {
					[csrf_header]: csrf_value
			    },
				type:"POST",
				async: true,
				data: { 'ontologyIdentification': $("#ontology").val()},
				dataType:"json",
				success: function(response,status){
					if(response.FeatureCollection == "FeatureCollection"){
						$("#tab-infobox").addClass('disabledTab');
						$("#tab-query").addClass('disabledTab');
					}else{
						ontologyFields = response;
					}
				}
			});
		},
		deleteLayer: function(layerId){
			deleteLayer(layerId);
		},
		
		checkAttribute: function (attribute, field){
			areUniqueAttribute = attributesArray.unique();
			areUniqueField = fieldsArray.unique();
			if (attributesArray.length !== areUniqueAttribute.length || fieldsArray.length !== areUniqueField.length)  { 
				var indexAttribute = attributesArray.indexOf(attribute);
	    		if (indexAttribute > -1) {
	    			attributesArray.splice(indexAttribute, 1);
	    		}
	    		var indexField = fieldsArray.indexOf(field);
	    		if (indexField > -1) {
	    			fieldsArray.splice(indexField, 1);
	    		}
				
				toastr.error(layerCreateJson.validations.duplicates,'');
				return;
			}
			var add= "<tr id='"+attribute+"'><td class='text-left no-wrap field'>"+field+"</td><td class='text-left no-wrap attribute'>"+attribute+"</td><td class='icon text-center' style='white-space: nowrap'><div class='grupo-iconos'><span class='btn btn-xs btn-no-border icon-on-table color-blue tooltips' onclick='LayerCreateController.editAttribute(this)' data-container='body' data-placement='bottom' id='edit_"+ attribute +" th:text='#{gen.edit}'><i class='icon-edit'></i></span><span class='btn btn-xs btn-no-border icon-on-table color-red tooltips' onclick='LayerCreateController.deleteAttribute(this)' th:text='#{gen.deleteBtn}'><i class='icon-delete'></i></span></div></div></div></td></tr>";
	    	$("#attributes tbody").append(add);
			return true;
		},
		
		checkOperation: function (operation, color, id){
			areUniqueOperation = operationsArray.unique();
			if (operationsArray.length !== areUniqueOperation.length)  { 
				var index = operationsArray.indexOf(operation);
	    		if (index > -1) {
	    			operationsArray.splice(index, 1);
	    		}
				
				toastr.error(layerCreateJson.validations.duplicates,'');
				
				return;
			}
			var id= uuidv4();
        	mapOperations.push({id : operation});
        	
			var add= "<tr id='"+id+"'><td class='text-left no-wrap operation'>"+operation+"</td><td class='text-left no-wrap color'>"+color+"</td><td class='icon text-center' style='white-space: nowrap'><div class='grupo-iconos'><span class='btn btn-xs btn-no-border icon-on-table color-blue tooltips' onclick='LayerCreateController.editFilter(this)' data-container='body' data-placement='bottom' id='edit_"+ id +" th:text='#{gen.edit}'><i class='icon-edit'></i></span><span class='btn btn-xs btn-no-border icon-on-table color-red tooltips' onclick='LayerCreateController.deleteFilter(this)' th:text='#{gen.deleteBtn}'><i class='icon-delete'></i></span></div></div></div></td></tr>";
	    	$("#filtersAttribute tbody").append(add);
			return true;
		},
		changeField: function (){
			var field = $("#fields").val();
			var type = $("#"+field).attr("name");
			if(type!="null"){
				$("#geometryType").val(type);
				$("#normalType").attr("style","display:block");
				$("#virtualType").attr("style","display:none");
				if(layerCreateJson.actionMode!=null){
					$("#geometryType").val(layerCreateJson.geometryType);
				}
			}else if(type=="null"){
				$("#normalType").attr("style","display:none");
				$("#virtualType").attr("style","display:block");
				if(layerCreateJson.actionMode!=null){
					$("#geometryTypeVirtual").val(layerCreateJson.geometryType);
				}
			}
			
			if(type=="Point"){
				$("#checkHeatMap").show();
				spinnerEachFrom = $("#min").TouchSpin({
					min: 0,
					max: 999.0,
					stepinterval: 0.2,
					maxboostedstep: 999.0,
					verticalbuttons: true
				});			
				
				($("#min").val() == "") ? $("#min").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				
				spinnerEachFrom = $("#max").TouchSpin({
					min: 0.0,
					max: 999.0,
					stepinterval: 0.2,
					maxboostedstep: 999,
					verticalbuttons: true
				});			
				
				($("#max").val() == "") ? $("#max").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				
				spinnerEachFrom = $("#radius").TouchSpin({
					min: 0.0,
					max: 9999.0,
					stepinterval: 0.2,
					maxboostedstep: 9999,
					verticalbuttons: true,
					postfix: 'px'
				});			
				
				($("#radius").val() == "") ? $("#radius").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
			}else if(type=="null"){
				LayerCreateController.changeFieldAux();
			}else{
				$("#checkHeatMap").hide();
			}
		},changeFieldAux: function (){
			var type = $("#geometryTypeVirtual").val();
			if(type=="Point"){
				$("#checkHeatMap").show();
				spinnerEachFrom = $("#min").TouchSpin({
					min: 0,
					max: 999.0,
					stepinterval: 0.2,
					maxboostedstep: 999.0,
					verticalbuttons: true
				});			
				
				($("#min").val() == "") ? $("#min").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				
				spinnerEachFrom = $("#max").TouchSpin({
					min: 0.0,
					max: 999.0,
					stepinterval: 0.2,
					maxboostedstep: 999,
					verticalbuttons: true
				});			
				
				($("#max").val() == "") ? $("#max").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				
				spinnerEachFrom = $("#radius").TouchSpin({
					min: 0.0,
					max: 9999.0,
					stepinterval: 0.2,
					maxboostedstep: 9999,
					verticalbuttons: true,
					postfix: 'px'
				});			
				
				($("#radius").val() == "") ? $("#radius").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
			}else{
				$("#checkHeatMap").hide();
			}
		},
		

		showAttributeDialog: function (editMode){
			$.confirm({
				async: false,
			    title: layerCreateJson.newattribute,
			    content: '' +
			    '<form action="" class="formName">' +
			    '<div class="form-group col-md-12" id="parameter_info">' +
			    '<label>' + layerCreateJson.field + '</label> <select id="fields_pop" class="form-control" data-width="100%">'+
				'</select>'+
				'<label>'+ layerCreateJson.attribute +'</label> <input type="text" name="field[]" id="attribute" value="" class="form-control"/></div>' +
			    '</form>',
			    buttons: {
			        formSubmit: {
			            text: 'OK',
			            btnClass: 'btn-blue',
			            action: function () {
			            	var field = $("#fields_pop").val();
			            	var attribute = $("#attribute").val();
			            	
			            	attributesArray.push(attribute);
			            	fieldsArray.push(field);
			            	
			            	LayerCreateController.checkAttribute(attribute, field);
			            		

			            }
			            	
			        },
			        cancel: function () {
			        },
			    },
			    onContentReady: function () {
			    	$("#fields_pop").empty();
					var fields = ontologyFields;
					$("#fields_pop").append('<option id="'+field+'" name="'+type+'" value="select">'+layerCreateJson.layerselect+'</option>');
					for (var key in fields){
				        var field = key;
				        var type = fields[key];
				        
				        $("#fields_pop").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
				    }
			        
			    }
			});
		},
		
		showFilterDialog: function (editMode){
			$.confirm({
				async: false,
			    title: layerCreateJson.newfilter,
			    content: '' +
			    
			    '<div class="form-group col-md-12" id="filter_info">' +
			    '<label>' + layerCreateJson.field + '</label> <select id="fields_pop" class="form-control" data-width="100%">'+
				'</select>'+
				'<label>' + layerCreateJson.operator + '</label> <select id="operator" class="form-control" data-width="100%">'+
				'<option value="==">==</option>'+
				'<option value=">">></option>'+
				'<option value="<"><</option>'+
				'<option value="!=">!=</option>'+
				'</select>'+
				'<label>'+ layerCreateJson.value +'</label> <input type="text" id="field_filter_value" name="value[]" id="attribute" value="" class="form-control"/></div>' +
				'<label>Color</label>'+
				'<div class="input-group colorpicker-component formcolorpicker" id="filterColor">'+
					'<label class="" th:text="#{layer.symbology.color} + ":""></label>'+
					'<input type="text" name="filter_color" id="filter_color" value="#000" class="form-control" />' +
				    '<div class="input-group-append" style="display: -webkit-box;">'+
				        '<span class="input-group-text input-group-addon"><i style="height: 15px;"></i></span>'+
				    '</div>'+
				'</div>'+
				'</div>',
			    buttons: {
			        formSubmit: {
			            text: 'OK',
			            btnClass: 'btn-blue',
			            action: function () {
			            	var field = $("#fields_pop").val();
			            	var operator = $("#operator").val();
			            	var value= $("#field_filter_value").val();
			            	
			            	var operation = field + operator + value;
			            	
			            	operationsArray.push(operation);
			            	
			            	LayerCreateController.checkOperation(operation, $("#filter_color").val());
			            	
			            }
			            	
			        },
			        cancel: function () {
			        },
			    },
			    onContentReady: function () {
			    	var fields = [];
				    $("#filterColor").colorpicker({
			            color: null
			        });
				    $("#fields_pop").empty();
				    if($("#isQuery").is(":checked")){
				    	fields = queryFields;
				    	 $("#fields_pop").append('<option id="select_field" name="select_field" value="select">'+layerCreateJson.layerselect+'</option>');
							$.each(fields, function (k,v){
								 var field = v;
							     $("#fields_pop").append('<option id="'+field+'" name="'+field+'" value="'+ field +'">' + field +'</option>');
							})
				    }else{
						fields = ontologyFields;
						$("#fields_pop").append('<option id="'+field+'" name="'+type+'" value="select">'+layerCreateJson.layerselect+'</option>');
						for (var key in fields){
					        var field = key;
					        var type = fields[key];
					        
					        $("#fields_pop").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
					    }

				    }
				    $("#filterColor").find(".input-group-append").css({"margin-top":-9});
			    }
			});
		},
		// EXTRACT PARAMS
		loadParamsQuery: function(field, op_name){
			logControl ? console.log(LIB_TITLE + ': loadParamsQuery()') : '';
			loadParamsFromQuery(field, op_name);
		},
	}
}();


// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	LayerCreateController.load(layerCreateJson);
	LayerCreateController.loadMessages(apiCustomOpsJson);
	
	LayerCreateController.init();
});
