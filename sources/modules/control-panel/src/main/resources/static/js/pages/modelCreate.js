
var ModelCreateController = function() {
	
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'Onesait Platform Control Panel'; 
	var LIB_TITLE = 'Model Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var hasId = false; // instance
	var jsonParameters = [];
	var editor;
	var parameters=[];
//	var mountableModel = $('#parameters').find('tr.mountable-model')[0].outerHTML; // save html-model for when select new datamodel, is remove current and create a new one.
	// CONTROLLER PRIVATE FUNCTIONS	--------------------------------------------------------------------------------

	var propertyTypeOntologyIndex=-1;
	
	$("#button2").on("click", function(){

		$.confirm({
		    title: modelCreateJson.newParameter,
		    content: '' +
		    '<form action="" class="formName">' +
		    '<div class="form-group col-md-12" id="parameter_info">' +
		    '<label class="control-label">'+ modelCreateJson.identification +'</label><input type="text" name="parameter[]" id="parameter_name" class="form-control"/>' +
		    '<label>' + modelCreateJson.type + '</label> <select onchange="ModelCreateController.checkType(this);" id="type" class="form-control" data-width="100%">'+
		    '<option value="select" th:text="#{model.parameter.type.select}">' + modelCreateJson.select + '</option>' +
			'<option value="string">string</option>' +																																																			
			'<option value="number">number</option>' +
			'<option value="enumeration">enum</option>' +
			'<option value="timestamp">timestamp</option></select></div>' +
		    '</form>',
		    buttons: {
		        close: {
					text: modelCreateJson.cancel,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
		        formSubmit: {
		            text: modelCreateJson.confirmBtn,
		            btnClass: 'btn btn-primary',
		            action: function () {
		            	if($("#type").val()=='' || $("#type").val()=="select" || $("#parameter_name").val()=='' || $("#parameter_name").val()==undefined){
		            		
							toastr.error(messagesForms.validation.genFormError,'');	
		            		return;
		            	}
		            	
		            	parameters.push($("#parameter_name").val());
		            	var isOk = ModelCreateController.checkParameter($("#parameter_name").val());
		            	if(!isOk){
		            		
		            		var index = parameters.indexOf($("#parameter_name").val());
		            		if (index > -1) {
		            			parameters.splice(index, 1);
		            		}
		            		return;
		            	}
		            	
		            	
		            	$("#parameter").append('<div class="form-group col-md-6 portlet light" id="parameter_'+ $("#parameter_name").val() +'"></div>');
		            	
		            	$("#parameter_" + $("#parameter_name").val()).append('<div class="col-md-12"><p id="identification_'+ $("#parameter_name").val() +'" class="control-label bold">'+ $("#parameter_name").val() +' :</p></div>');
	            		$("#parameter_" + $("#parameter_name").val()).append('<div class="col-md-4"><label class="control-label">'+ modelCreateJson.type +'</label><p class="form-control-static-block" id="type_'+ $("#parameter_name").val() +'"> '+ $("#type").val() +' </p></div>');
	            		
		            	if($("#type").val() == "number"){
		            		$("#parameter_" + $("#parameter_name").val()).append('<div class="col-md-4"><label class="control-label">'+ modelCreateJson.from +'</label><p class="form-control-static-block" id="from_'+ $("#parameter_name").val() +'"> '+ $("#from").val() +' </p></div>');
		            		$("#parameter_" + $("#parameter_name").val()).append('<div class="col-md-4"><label class="control-label">'+ modelCreateJson.to +'</label><p class="form-control-static-block" id="to_'+ $("#parameter_name").val() +'"> '+ $("#to").val() +' </p></div>');
		            	}else if($("#type").val() == "enumeration"){
		            		$("#parameter_" + $("#parameter_name").val()).append('<div class="col-md-4"><label class="control-label">'+ modelCreateJson.enum +'</label><p class="form-control-static-block" id="enum_'+ $("#parameter_name").val() +'"> '+ $("#enum").val() +' </p></div>');
		            	}
		            	
		            	$("#parameter_" + $("#parameter_name").val()).append('<div class="col-md-12"><div class="btn-group pull-right"><span class="btn btn-circle btn-outline blue " onclick="ModelCreateController.editParameter(\''+ $("#parameter_name").val() +'\')" data-container="body" data-placement="bottom" id="edit_'+ $("#parameter_name").val() +'" th:text="#{gen.edit}"><i class="fa fa-edit"></i></span><span class="btn btn-circle btn-outline blue " onclick="ModelCreateController.deleteParameter(\''+ $("#parameter_name").val() +'\')" th:text="#{gen.deleteBtn}"><i class="fa fa-trash"></i></span></div></div>');
		            }
		        }
		    }
		});
	});

	
	$("#categories_select").on("change",function(){
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");

		$.ajax({
		    url: '/controlpanel/categories/getSubcategories/'+$("#categories_select").val(),
		    headers: {
				[csrf_header]: csrf_value
		    },
		    type: 'GET',						  
		    success: function(result) {
	  
		    	$("#subcategories").removeAttr("disabled");
		    	$("#subcategories").empty();
	
		    	$('#subcategories').append($('<option>', { 
	    	        value: '',
	    	        text : '',
	    	        style : 'height:30px;'
	    	    }));
	
		    	$.each(result, function (i, subcategory) {
		    		
		    		$("#subcategories").append($('<option>', { 
		    	        value: subcategory,
		    	        text : subcategory 
		    	    }));
		    	});
	
		    	$("#subcategories").selectpicker("refresh");
		    }
		});
	});
	
	$('input[type=radio][name=output]').change(function() {
	    if (this.value == 'paragraph') {
	    	if($("#notebook").val() == null || $("#notebook").val() == "" || $("#notebook").val() == undefined){
	    		
				toastr.error(modelCreateJson.validations.notebook,'');	
	    		return;
	    	}
	       $("#dashboard").attr("disabled", "disabled");
		   var csrf_value = $("meta[name='_csrf']").attr("content");
		   var csrf_header = $("meta[name='_csrf_header']").attr("content");

	       $.ajax({
			    url: 'getOutputParagraph/'+$("#notebook").val(),
			    headers: {
					[csrf_header]: csrf_value
			    },
			    type: 'GET',						  
			    success: function(result) {
			    	
			    	if(result!="error"){
			    		$("#outputParagraphId").val(result);
			    	}
			    	
			    }
			});
	    }else if (this.value == 'dashboard') {
	    	 $("#dashboard").removeAttr("disabled");
	    	 $("#outputParagraphId").val("");
	    }
	});

	// CHECK IF JSON STRING WHEN JSON PARSE IS OK OR NOT, IF THE JSON IS MALFORMED THE JSON.parse() is broken.
	var IsJsonString = function(str) {
		try {
			JSON.parse(str);
		}
		catch (e) {	return false; }
		
		return true;
	}

	var props=[];
	var updateSchemaProperties = function(){
		logControl ? console.log('updateSchemaProperties() -> ') : '';
		
		var schemaObj = {};
		
		checkUnique = parameters.unique();
		if (parameters.length !== checkUnique.length)  {toastr.error(digitaltwintype.validations.duplicates,'');	 return false; } 
		
		if ( parameters.length ){	
			$.each(parameters, function( index, value ) {
				
				props.push('{"identification":"'+value+'","type":"'+$("#type_" + value).text().trim()+'","from":"'+$("#from_" + value).text().trim()+'", "to":"'+$("#to_" + value).text().trim()+'","enum":"'+$("#enum_" + value).text().trim()+'","timestamp":"' + $("#enum_" + value).text().trim() + '"}');
								
			});			
		}
			
	}
	
	// DELETE MODEL
	var deleteModelConfirmation = function(modelId){
		console.log('deleteModelConfirmation() -> formId: '+ modelId);
		
		// no Id no fun!
		if ( !modelId ) {toastr.error(modelCreateJson.validations.validform,''); return false; }
		
		logControl ? console.log('deleteModelConfirmation() -> formAction: ' + $('.delete-model').attr('action') + ' ID: ' + $('#delete-modelId').attr('modelId')) : '';
		
		// call ontology Confirm at header. 
		HeaderController.showConfirmDialogModel('delete_model_form');	
	}
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// CHECK DATES AND LET THE FORM SUBMMIT
	var checkCreate = function(){
		logControl ? console.log('checkCreate() -> ') : '';
        
		var dateCreated = $("#datecreated").datepicker('getDate');
        var dateDeleted = $("#datedeleted").datepicker('getDate');
		
		var diff = dateDeleted - dateCreated;
		var days = diff / 1000 / 60 / 60 / 24;
				
		logControl ?  console.log('created: ' + dateCreated + '  deleted: ' + dateDeleted): '';		
		
        if (dateDeleted != ""){
            if (dateCreated > dateDeleted){
                $.confirm({title: 'CONFIRM:', theme: 'light',
					content: userCreateReg.validation_dates,
					draggable: true,
					dragWindowGap: 100,
					backgroundDismiss: true,
					buttons: {				
						close: { text: modelCreateJson.cancel, btnClass: 'btn btn-outline blue dialog', action: function (){} //GENERIC CLOSE.		
						}
					}
				});
                $("#datedeleted").datepicker('update','');
            }			           
        }
    } 
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation:
        // http://docs.jquery.com/Plugins/Validation

        var form1 = $('#model_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');


		// set current language
		currentLanguage = modelCreateJson.language || LANGUAGE;

        form1.validate({
            errorElement: 'span', // default input error message container
            errorClass: 'help-block help-block-error', // default input error
														// message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate
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
            	notebook:		{ required: true },
                identification:	{ minlength: 5, required: true },
				description:	{ minlength: 5, required: true }
				
            },
            invalidHandler: function(event, validator) {  					
					
				if($('#categories_select').val()=='') {
            	
					$('#categories_select_div').removeClass('has-error');
					$('#categories_select_div').addClass('has-error');
            		 
            	}else{
					$('#categories_select_div').removeClass('has-error');
				}
															
				if( $('#subcategories').val()=='') {
            	
					$('#subcategories_select_div').removeClass('has-error');
					$('#subcategories_select_div').addClass('has-error');
            		 
            	}else{
					$('#subcategories_select_div').removeClass('has-error');
				}
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
            	
            	if(!$('#dashboard-radio').is(':checked') && !$('#paragraph-radio').is(':checked')) {
            		
					toastr.error(modelCreateJson.validations.outputSource,'');	
            		return;
            	}
				if($('#dashboard-radio').is(':checked') && $('#dashboard').val()==''){
					
					toastr.error(modelCreateJson.validations.outputSource,'');	
            		return;
				}
            	
            	var caterror=false;		
				if($('#categories_select').val()=='') {            	
					$('#categories_select_div').removeClass('has-error');
					$('#categories_select_div').addClass('has-error');
            		 caterror= true;
            	}else{
					$('#categories_select_div').removeClass('has-error');
				}
															
				if( $('#subcategories').val()=='') {
            	
					$('#subcategories_select_div').removeClass('has-error');
					$('#subcategories_select_div').addClass('has-error');
            		  caterror= true;
            	}else{
					$('#subcategories_select_div').removeClass('has-error');
				}
				if( caterror == true){		
					toastr.error(modelCreateJson.validations.category,'');								
					return false;
				}
                error1.hide();
              
                updateSchemaProperties();
    			
	   			 $.each(props, function(k,v){
	   			       
	   		         $("<input type='hidden' value='"+v+"' />")
	   		         .attr("name", "parameters")
	   		         .appendTo("#model_create_form");
	   		        
	   		     });
	   			
					
				// form.submit();
				form1.ajaxSubmit({type: 'post', success : function(data){
					toastr.success(messagesForms.validation.genFormSuccess,'');
					navigateUrl(data.redirect);
					
					}, error: function(data){
						toastr.error(data.responseJSON.cause,'');
						
					}
				})
				

			}
        });
    }
	
	 var initTemplateElements = function(){
		
	$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});			
	
		$('#categories_select').filter('[required]').bind('blur', function (ev) { // fires on every blur
			if($('#categories_select').val()=='') {            	
					$('#categories_select_div').removeClass('has-error');
					$('#categories_select_div').addClass('has-error');            		
            	}else{
					$('#categories_select_div').removeClass('has-error');
				}
		});			
	
	
	$('#subcategories').filter('[required]').bind('blur', function (ev) { // fires on every blur
			if( $('#subcategories').val()=='') {
            	
					$('#subcategories_select_div').removeClass('has-error');
					$('#subcategories_select_div').addClass('has-error');
            		 
            	}else{
					$('#subcategories_select_div').removeClass('has-error');
				}
		});			
		
	// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('model_create_form');
		});		
		
	}

	
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
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		$('.tagsinput').prev().removeClass('tagsinput-has-error');
		$('.tagsinput').nextAll('span:first').addClass('hide');

		$('#subcategories_select_div').removeClass('has-error');
		$('#categories_select_div').removeClass('has-error');
		
		$("#parameter").empty();
		parameters=[];
		
		//CLEANING IMAGE
		$('#image').val('');
		$('#showedImgPreview').attr('src', '/controlpanel/img/APPLICATION.png');
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}
	

	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return modelCreateJson = Data;
		},
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';

			handleValidation();
			initTemplateElements();
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
			
			// INSERT MODE ACTIONS  (ontologyCreateReg.actionMode = NULL ) 
			if ( modelCreateJson.actionMode === null){
				logControl ? console.log('|---> Action-mode: INSERT') : '';
			}
			// EDIT MODE ACTION 
			else {	
				logControl ? console.log('|---> Action-mode: UPDATE') : '';
				var jsonProperties = [];
				// if digitalTwinType has properties  we load it!.
				propertiesJson = modelCreateJson.parameters;	
				
				$("#categories_select").val(modelCreateJson.category);
				$("#subcategories").val(modelCreateJson.subcategory);
				$("#notebook").val(modelCreateJson.notebook);
				
				if(modelCreateJson.dashboard!=null){
					$('#dashboard-radio').click();
					$("#dashboard").val(modelCreateJson.dashboard);
				}else if(modelCreateJson.outputparagraph!=null){
					$('#paragraph-radio').click();
					$("#outputParagraphId").val(modelCreateJson.outputparagraph);
				}
				
				if (propertiesJson.length > 0 ){
					
					// MOUNTING PROPERTIES ARRAY
					var name, type , rank, ontology, url = '';
					$.each( propertiesJson, function (key, object){			
						
						name = object.identification; 
						type = object.type; 
						to = object.to;
						from = object.from;
						timestamp = object.timestamp;
						enume = object.enumerators;
						url = object.url;
						
						propertyUpdate = {"parameter": name, "type": type, "from" : from, "to" : to, "enum": enume, "timestamp": timestamp};					
						jsonProperties.push(propertyUpdate);
						
					});
					
					logControl ? console.log('propertiesArr on UPDATE: ' + jsonProperties.length + ' Arr: ' + JSON.stringify(jsonProperties)) : '';
					
					currentLanguage = modelCreateJson.language || LANGUAGE[0];
					currentFormat =  'MM/DD/YYYY HH:mm:ss';
					
					$.each(jsonProperties, function(key, param){
						
						parameters.push(param.parameter);
						$("#parameter").append('<div class="form-group col-md-6 portlet light" id="parameter_'+ param.parameter +'"></div>');
		            	
		            	element=param.parameter;
		            	
		            	$("#parameter_" + element).append('<div class="col-md-12"><p class="control-label bold">'+ element +' :</p></div>');
	            		$("#parameter_" + element).append('<div class="col-md-4"><label class="control-label">'+ modelCreateJson.type +'</label><p id="type_'+ element +'" class="form-control-static-block"> '+ param.type +' </p></div>');
	            		
		            	if(param.type == "number"){
		            		$("#parameter_" + element).append('<div class="col-md-4"><label class="control-label">'+ modelCreateJson.from +'</label><p id="from_'+ element +'" class="form-control-static-block"> '+ param.from +' </p></div>');
		            		$("#parameter_" + element).append('<div class="col-md-4"><label class="control-label">'+ modelCreateJson.to +'</label><p id="to_'+ element +'" class="form-control-static-block"> '+ param.to +' </p></div>');
		            	}else if(param.type == "enumeration"){
		            		$("#parameter_" + element).append('<div class="col-md-4"><label class="control-label">'+ modelCreateJson.enum +'</label><p id="enum_'+ element +'" class="form-control-static-block"> '+ param.enume +' </p></div>');
		            	}
		            	
		            	$("#parameter_" + element).append('<div class="col-md-12"><div class="btn-group pull-right"><span class="btn btn-circle btn-outline blue " onclick="ModelCreateController.editParameter(\''+ element +'\')" data-container="body" data-placement="bottom" id="edit_'+ element +'" th:text="#{gen.edit}"><i class="fa fa-edit"></i></span><span class="btn btn-circle btn-outline blue " onclick="ModelCreateController.deleteParameter(\''+ element +'\')" th:text="#{gen.deleteBtn}"><i class="fa fa-trash"></i></span></div></div>');
					});
					
				}
				
				var ids= modelCreateJson.ids;
				for(var i=0; i<ids.length; i++){
	    			$("#inputParagraphId").append("<option val='"+ids[i]+"'>"+ids[i]+"</option>");
	    		}
	    		$("#inputParagraphId").val(modelCreateJson.inputParagraphId);
	    		$("#inputParagraphId").removeAttr("disabled");
				
			}
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		// DELETE DIGITAL TWIN TYPE 
		deleteModel: function(modelId){
			logControl ? console.log(LIB_TITLE + ': deleteModel()') : '';	
			deleteModelConfirmation(modelId);			
		},
	// CHECK FOR NON DUPLICATE PROPERTIES
		checkParameter: function(element){
			logControl ? console.log(LIB_TITLE + ': checkParameter()') : '';
			areUnique = parameters.unique();
			if (parameters.length !== areUnique.length)  { 
				
				toastr.error(modelCreateJson.validations.duplicates,'');	
				return false;
			}
			return true;
		},
		
		// REMOVE PROPERTYS
		removeParameter: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeParameter()') : '';
			
			var remproperty = $(obj).closest('tr').find("input[name='parameter\\[\\]']").val();		
			$(obj).closest('tr').remove();
		},
		checkType : function(obj){
			logControl ? console.log(LIB_TITLE + ': checkType()') : '';
			// set current language and formats
			currentLanguage = modelCreateJson.language || LANGUAGE[0];
			currentFormat =  'MM/DD/YYYY HH:mm:ss';
			
			$("#parameter_info").find(".enum").remove();
			$("#parameter_info").find(".to").remove();
			$("#parameter_info").find(".from").remove();
	
			if($(obj).val() == "number"){
				
				$("#parameter_info").append("<div class='col-md-6 from'><label>" + modelCreateJson.from + "</label></div>");
				$("#parameter_info").append("<div class='col-md-6 to'><label>" + modelCreateJson.to + "</label></div>");
				
				$("#parameter_info").find(".from").append('<input class="form-control from-input" id="from" name="from[]"/>');
				$("#parameter_info").find(".to").append('<input class="form-control to-input" id="to" name="to[]"/>');
				
//				SPINNER OFFSET INIT
				spinnerEachFrom = $("#parameter_info").find(".from-input").TouchSpin({
					min: 0,
					max: 999,
					stepinterval: 1,
					maxboostedstep: 999,
					verticalbuttons: true
				});			
				
				($("#parameter_info").find(".from-input").val() == "") ? $("#parameter_info").find(".from-input").val(0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				
//				SPINNER OFFSET INIT
				spinnerEachTo = $("#parameter_info").find(".to-input").TouchSpin({
					min: 0,
					max: 999,
					stepinterval: 1,
					maxboostedstep: 999,
					verticalbuttons: true
				});	
				
				($("#parameter_info").find(".to-input").val() == "") ? $("#parameter_info").find(".to-input").val(0) : null;		
				spinnerEachTo.bind("keydown", function (event) { event.preventDefault(); });
			
				
			}else if($(obj).val() == "enumeration"){
				
				$("#parameter_info").append("<div class='col-md-12 enum'><label>" + modelCreateJson.enum + "</label></div>");
				
				$("#parameter_info").find(".enum").append('<input id="enum" data-msg="Please fill this field" th:tabindex="2" name="enum[]" data-role="tagsinput" class="form-control tagsinput enum-input" type="text" data-minlength="2" pattern=".{2,}" min="2"></input>');
				$("#parameter_info").find(".enum").find("input").tagsinput();
				
				$("#parameter_info").find(".enum").on('beforeItemAdd', function(event) {
					  event.cancel = false;
				});
			}
				
		},
		changeNotebook : function(){
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");

			$.ajax({
			    url: 'getConfigParagraph/'+$("#notebook").val(),
			    headers: {
					[csrf_header]: csrf_value
			    },
			    type: 'GET',						  
			    success: function(result) {
			    	
			    	if(result!=null){
			    		var ids = result;
			    		for(var i=0; i<ids.length; i++){
			    			$("#inputParagraphId").append("<option val='"+ids[i]+"'>"+ids[i]+"</option>");
			    		}
			    		$("#inputParagraphId").val(ids[0]);
			    		$("#inputParagraphId").removeAttr("disabled");
			    	}
			    	
			    }
			});
		},
		editParameter : function(element){
			
				$.confirm({
					async: false,
				    title: modelCreateJson.newParameter,
				    content: '' +
				    '<form action="" class="formName">' +
				    '<div class="form-group col-md-12" id="parameter_info">' +
				    '<label class="control-label">'+ modelCreateJson.identification +'</label><input type="text" name="parameter[]" id="parameter_name" value="'+ element +'" class="form-control"/>' +
				    '<label>' + modelCreateJson.type + '</label> <select onchange="ModelCreateController.checkType(this);" id="type" class="form-control" data-width="100%">'+
				    '<option value="select" th:text="#{model.parameter.type.select}">' + modelCreateJson.select + '</option>' +
					'<option value="string">string</option>' +
					'<option value="number">number</option>' +
					'<option value="enumeration">enum</option>' +
					'<option value="timestamp">timestamp</option>' +
					'</select></div>' +
				    '</form>',
				    buttons: {
				    	close: {
							text: modelCreateJson.cancel,
							btnClass: 'btn btn-outline blue dialog',
							action: function (){} //GENERIC CLOSE.		
						},
				        formSubmit: {
				            text: modelCreateJson.confirmBtn,
				            btnClass: 'btn btn-primary',
				            action: function () {
				            	if($("#type").val()=='' || $("#type").val()=="select" || $("#parameter_name").val()=='' || $("#parameter_name").val()==undefined){
				            		
									toastr.error(messagesForms.validation.genFormError,'');	
				            		return;
				            	}
				            	
				            	if(element != $("#parameter_name").val()){
				            		var index = parameters.indexOf(element);
				            		if (index > -1) {
				            			parameters.splice(index, 1);
				            		}
				            		
				            		parameters.push($("#parameter_name").val());
					            	
					            	var isOk = ModelCreateController.checkParameter($("#parameter_name").val());
					            	if(!isOk){
					            		
					            		var index = parameters.indexOf($("#parameter_name").val());
					            		if (index > -1) {
					            			parameters.splice(index, 1);
					            		}
					            		return;
					            	}
				            	}
				            	
				            	
				            	$("#parameter_" + element).empty();
				            	$("#parameter_" + element).remove();
				            	
				            	$("#parameter").append('<div class="form-group col-md-6 portlet light" id="parameter_'+ $("#parameter_name").val() +'"></div>');
				            	
				            	element=$("#parameter_name").val();
				            	
				            	$("#parameter_" + element).append('<div class="col-md-12"><p class="control-label bold">'+ $("#parameter_name").val() +' :</p></div>');
			            		$("#parameter_" + element).append('<div class="col-md-4"><label class="control-label">'+ modelCreateJson.type +'</label><p id="type_'+ $("#parameter_name").val() +'" class="form-control-static-block"> '+ $("#type").val() +' </p></div>');
			            		
				            	if($("#type").val() == "number"){
				            		$("#parameter_" + element).append('<div class="col-md-4"><label class="control-label">'+ modelCreateJson.from +'</label><p id="from_'+ $("#parameter_name").val() +'" class="form-control-static-block"> '+ $("#from").val() +' </p></div>');
				            		$("#parameter_" + element).append('<div class="col-md-4"><label class="control-label">'+ modelCreateJson.to +'</label><p id="to_'+ $("#parameter_name").val() +'" class="form-control-static-block"> '+ $("#to").val() +' </p></div>');
				            	}else if($("#type").val() == "enumeration"){
				            		$("#parameter_" + element).append('<div class="col-md-4"><label class="control-label">'+ modelCreateJson.enum +'</label><p id="enum_'+ $("#parameter_name").val() +'" class="form-control-static-block"> '+ $("#enum").val() +' </p></div>');
				            	}
				            	
				            	$("#parameter_" + element).append('<div class="col-md-12"><div class="btn-group pull-right"><span class="btn btn-circle btn-outline blue " onclick="ModelCreateController.editParameter(\''+ $("#parameter_name").val() +'\')" data-container="body" data-placement="bottom" id="edit_'+ $("#parameter_name").val() +'" th:text="#{gen.edit}"><i class="fa fa-edit"></i></span><span class="btn btn-circle btn-outline blue " onclick="ModelCreateController.deleteParameter(\''+ $("#parameter_name").val() +'\')" th:text="#{gen.deleteBtn}"><i class="fa fa-trash"></i></span></div></div>');
				            }
				        }
				    },
				    onContentReady: function () {
				        $("#type").val($("#type_" + element).text().trim());
				        
				        if($("#type").val() == "number"){
							
							$("#parameter_info").append("<div class='col-md-6 from'><label>" + modelCreateJson.from + "</label></div>");
							$("#parameter_info").append("<div class='col-md-6 to'><label>" + modelCreateJson.to + "</label></div>");
							
							$("#parameter_info").find(".from").append('<input class="form-control from-input" id="from" name="from[]"/>');
							$("#parameter_info").find(".to").append('<input class="form-control to-input" id="to" name="to[]"/>');
							
//							SPINNER OFFSET INIT
							spinnerEachFrom = $("#parameter_info").find(".from-input").TouchSpin({
								min: 0,
								max: 999,
								stepinterval: 1,
								maxboostedstep: 999,
								verticalbuttons: true
							});			
							
							($("#parameter_info").find(".from-input").val() == "") ? $("#parameter_info").find(".from-input").val(parseInt($("#from_" + element).text().trim())) : null;		
							spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
							
//							SPINNER OFFSET INIT
							spinnerEachTo = $("#parameter_info").find(".to-input").TouchSpin({
								min: 0,
								max: 999,
								stepinterval: 1,
								maxboostedstep: 999,
								verticalbuttons: true
							});	
							
							($("#parameter_info").find(".to-input").val() == "") ? $("#parameter_info").find(".to-input").val(parseInt($("#to_" + element).text().trim())) : null;		
							spinnerEachTo.bind("keydown", function (event) { event.preventDefault(); });
						
							
						}else if($("#type").val() == "enumeration"){
							
							$("#parameter_info").append("<div class='col-md-12 enum'><label>" + modelCreateJson.enum + "</label></div>");
							
							$("#parameter_info").find(".enum").append('<input id="enum" data-msg="Please fill this field" th:tabindex="2" name="enum[]" data-role="tagsinput" class="form-control tagsinput enum-input" type="text" data-minlength="2" pattern=".{2,}" min="2"></input>');
							$("#parameter_info").find(".enum").find("input").tagsinput();
							
							$("#parameter_info").find(".enum").on('beforeItemAdd', function(event) {
								  event.cancel = false;
							});
							
							$("#parameter_info").find(".enum").find("input").val($("#enum_" + element).text().trim());
						}
				        
				    }
				});
				
		},
		deleteParameter : function(element){
			var index = parameters.indexOf(element);
    		if (index > -1) {
    			parameters.splice(index, 1);
    		}
    		
			$("#parameter_" + element).empty();
			$("#parameter_" + element).remove();
		}
	}
}();


// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	ModelCreateController.load(modelCreateJson);
	
	ModelCreateController.init();
});
