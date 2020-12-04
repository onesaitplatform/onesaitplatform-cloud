var authorizationsArr = []; // add authorizations
var authorizationUpdateArr = []; // get authorizations of the api
var authorizationsIds = []; // get authorizations ids for actions
var authorizationObj = {}; // object to receive authorizations responses.
var ApiCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Menu Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.
	var currentFormat = '' // date format depends on currentLanguage.
	var internalFormat = 'yyyy/mm/dd';
	var internalLanguage = 'en';
	var reader = new FileReader();
	var mountableModel2 = "";
	if ($('#api_authorizations').find('tr.authorization-model')[0]){
		mountableModel2 = $('#api_authorizations').find('tr.authorization-model')[0].outerHTML;
	}
    
	reader.onload = function (e) {
        $('#showedImg').attr('src', e.target.result);
    }
	

	// CONTROLLER PRIVATE FUNCTIONS	
    var showGenericErrorDialog= function(dialogTitle, dialogContent){		
		logControl ? console.log('showErrorDialog()...') : '';
		var Close = headerReg.btnCancelar;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			icon: 'fa fa-bug',
			title: dialogTitle,
			theme: 'light',
			content: dialogContent,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			closeIcon: true,
			buttons: {				
				close: {
					text: Close,
					btnClass: 'btn btn-sm btn-outline btn-circle blue',
					action: function (){} //GENERIC CLOSE.		
				}
			}
		});			
	}
    
	 function deleteAPIConfirmation (){
			
			var id = apiCreateReg.apiId;
			console.log('deleteAPIConfirmation() -> formId: '+ id);
			
			// no Id no fun!
			if ( !id ) {$.alert({title: 'ERROR!',type: 'red' , theme: 'light', content: 'NO USER-FORM SELECTED!'}); return false; }
			
			// call  Confirm 
			showConfirmDeleteDialog(id);	
		} 
    
	function showConfirmDeleteDialog(id){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Title = headerReg.titleConfirm + ':';

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			icon: 'fa fa-warning',
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: apiCreateReg.apimanager_delete_confirm,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			closeIcon: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-sm btn-circle blue btn-outline',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-sm btn-circle btn-primary btn-outline',
					action: function(){ 
						var csrf_value = $("meta[name='_csrf']").attr("content");
						var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
						console.log(id);
						$.ajax({
						    url: '/controlpanel/apimanager/'+id,
			                headers: {
								[csrf_header]: csrf_value
						    },
						    type: 'DELETE',						  
						    success: function(result) {
						    	console.log(result);
						    	navigateUrl('/controlpanel/apimanager/list');
						    }
						});
					}											
				}
				
			}
		});
	}	

	var calculateVersion = function() {

        var identification = $('#identification').val();
        var apiType = $('#apiType').val();
    	var csrf_value = $("meta[name='_csrf']").attr("content");
    	var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
    	
        if ((identification!=null) && (identification.length>0) && (apiType!='')) {
            $.ajax({
                url: '/controlpanel/apimanager/numVersion',
                headers: {
					[csrf_header]: csrf_value
			    },
                type: 'POST',
                data: JSON.stringify({"identification":identification,"apiType":apiType}),
                dataType: 'text',
                contentType: 'text/plain',
                mimeType: 'text/plain',
                success: function(data) {
                    if(data != null && data != "") {
                        $('#numversion').val(data);
                        createOperationsOntology ();
                        // VISUAL-UPDATE
                        configurarApi();
                    }
                },
                error: function(data,status,er) {
                    $('#dialog-error').val("ERROR");
                }
            });
        } else {
            configurarApi();
        }
    }

	var configurarApi = function () {
        apiType = $('#apiType').val();
        apiName = $('#identification').val();
        apiVersion = $('#numversion').val();
        apiEndPoint = $('#id_endpoint');
        apiSwagger = $('#id_endpoint_swagger');
        
        switch (apiType) {
        	case 'EXTERNAL_FROM_JSON':
            case 'INTERNAL_ONTOLOGY':
            	apiEndPoint.val(endpoint + "server/api/v" + apiVersion + "/" + apiName);
//            	apiSwagger.val(endpoint + "/services/management/api-docs?url=/services/management/swagger" + "/v" + apiVersion + "/" + apiName + "/swagger.json");
            	apiSwagger.val(endpoint + "/services/management/api-docs?url=/services/management/swagger" + "/" + apiName + "/swagger.json");
                break;
                
        }
        // --- configurar panel operaciones
        ontologySelector = $('#ontology');
        ontologyOperations = $('#ontologyOperations');

        cleanOperationsOntology();
        // empieza con la operaciones limpias
        // borrarOperaciones();

        if (apiType && apiType.startsWith('INTERNAL_ONTOLOGY')) {
            // api sobre ontologias
        	ontologySelector.prop('disabled', false);
        	$('#row-json').addClass('hide');
        	$('#row-operations').removeClass('hide');
            createOperationsOntology();
        }
        if(apiType && apiType.startsWith('EXTERNAL_FROM_JSON')) {
        	ontologySelector.prop('disabled', true);
        	$('#row-operations').addClass('hide');
        	$('#row-json').removeClass('hide');
        	myCodeMirror.refresh();
        }
    }
	
	function loadOperations () {
        try {
            if ($('#identification').val()!=null){
                for(var i=0; i<operations.length; i+=1){
                    if (isDefaultOp(operations[i].identification)){
                        var id = operations[i].identification;
                        var nameOp = id.substring(id.lastIndexOf("_") + 1);
                        $('#' + nameOp).addClass('op_button_selected').removeClass('op_button');
                        $('#description_' + nameOp).val(operations[i].description);
                        $('#descOperation' + nameOp).show();
                        $('#description_' + nameOp + '_label').text(operations[i].path);
                        $('#div' + nameOp).addClass('op_div_selected');
                    }
                }
            }
        } catch (err) {
            console.log('Fallo cargando operaciones',err);
            $('.capa-loading').hide();
        }
    }
	
	function isDefaultOp(idOp){
		if (idOp.endsWith("_GETAll") || idOp.endsWith("_GET") || idOp.endsWith("_POST") || idOp.endsWith("_PUT") || idOp.endsWith("_DELETEID")){
			return true;
		} else {
			return false;
		}
	}
	
    function createOperationsOntology () {
    	$('#description_GET_All_label').text("/");
    	$('#description_GET_label').text("/{id}");
        $('#description_POST_label').text("/");
        $('#description_PUT_label').text("/{id}");
        $('#description_DELETEID_label').text("/{id}");
        $('#ontologyOperations input[type="text"]').val('').show();
    }
    
    function cleanOperationsOntology () {
        // desactivar operaciones
        $('#ontologyOperations input.op_button_selected').removeClass('op_button_selected').addClass('op_button');
        // eliminar descripciones y ocultarlas
        $('#ontologyOperations input[type="text"]').val('').css('display',':none');
        // cambia bordes
        $('#ontologyOperations .op_div_selected').removeClass('op_div_selected').addClass('op_div');
        // oculta detalles
        $('#ontologyOperations .op_desc_div').css('display','none');
    }

    var updateCacheTimeout = function () {
        var checkCache= $('#checkboxCache').prop('checked');
        if (checkCache) {
            $('#id_cachetimeout').val("0");
        	$('#id_cachetimeout').prop('disabled', false);
        } else {
        	$('#id_cachetimeout').val("");
        	$('#id_cachetimeout').prop('disabled', true);
        }
    }

    var updateApiLimit = function () {
        var checkCache= $('#checkboxLimit').prop('checked');
        if (checkCache) {
        	$('#id_limit').val("5");
        	$('#id_limit').prop('disabled', false);
        } else {
        	$('#id_limit').val("");
        	$('#id_limit').prop('disabled', true);
        }
    }
	
    function selectOperation(button){
    	if (button.className=='op_button'){
    		button.className='op_button_selected';
    		$('#description_' + button.name).val("");
    		$('#descOperation' + button.name).show();
    		$('#div' + button.name).prop('className', 'op_div_selected');
    	} else if (button.className=='op_button_selected'){
    		button.className='op_button';
    		$('#description_' + button.name).val("");
    		$('#descOperation' + button.name).hide();
    		$('#div' + button.name).prop('className', 'op_div');
    		removeOp(button);
    	}
    } 
    
    function removeOp(button){
    	var op_name = $('#identification').val() + "_" + button.name;
        for(var i=0; i<operations.length; i+=1){
            var operation = operations [i];
            if (operation.identification == op_name){
            	operations.splice(i, 1);
            }
        }
    }  
    
	// REDIRECT URL
	var navigateUrl = function(url){ window.location.href = url; }
	
	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/apimanager/freeResource/" + id).done(
				function(data){
					console.log('freeResource() -> ok');
					navigateUrl(url); 
				}
			).fail(
				function(e){
					console.error("Error freeResource", e);
					navigateUrl(url); 
				}
			)		
	}
		
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		logControl ? console.log('cleanFields() -> ') : '';
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remote" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}
	
	// FORMATDATES: format date to DDBB standard 'yyyy/mm/dd';
	var formatDates = function(dates){
		
		var dateUnformatted = '';
		var dateFormatted	= '';
		
		// no dates no fun!
		if (!dates) { return false;}
		
		// if current language is en , dates are in DDBB format so OK
		if (currentLanguage == 'en') { return true; }
		
		// change all dates to internal format
		logControl ? console.log('formatDates() -> ' + dates + ' with CurrentLanguage: ' + currentLanguage) : '';
		
		
		$(dates).each(function( index, dateInput ) {		  
			if ( $(dateInput).val() ){				
				
				// ES
				if (currentLanguage === 'es'){
					// change date es to en [ dd/mm/yyyy to yyyy/mm/dd ]
					dateUnformatted = $(dateInput).val();
					dateFormatted = dateUnformatted.split("/")[2] + '/' + dateUnformatted.split("/")[1] + '/' + dateUnformatted.split("/")[0];					
					$(dateInput).val(dateFormatted);
					logControl ? console.log('FormatDate -> ' + $(dateInput).attr('id') + ' current:' + dateUnformatted + ' formatted: ' + $(dateInput).val()) : '';					
				}
				// more languages to come...				
			}		  
		});

		// all formatted then true;
		return true;
	}
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#api_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');
		
		// set current language
		currentLanguage = apiCreateReg.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// custom messages
            messages: {
				datedeleted: { checkdates : apiCreateReg.validation_dates }
			},
			// validation rules
            rules: {
            	identification:		{ minlength: 5, maxlength: 50, required: true },
            	categories:			{ required: true },
            	apiType:			{ required: true },
            	ontology:			{ required: true },
            	id_endpoint:		{ required: true },
            	apiDescription:		{ required: true },
            	id_metainf:			{ required: true },
				datecreated:		{ date: true, required: true }
            },
            invalidHandler: function(event, validator) { //display error alert on form submit              
                success1.hide();
                error1.show();
                App.scrollTo(error1, -200);
            },
            errorPlacement: function(error, element) {
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }
				else 								{ error.insertAfter(element); }
            },
            highlight: function(element) { // hightlight error inputs
                $(element).closest('.form-group').addClass('has-error'); 
            },
            unhighlight: function(element) { // revert the change done by hightlight
                $(element).closest('.form-group').removeClass('has-error');
            },
            success: function(label) {
                label.closest('.form-group').removeClass('has-error');
            },
			// ALL OK, THEN SUBMIT.
            submitHandler: function(form) {
                success1.show();
                error1.hide();
				// date conversion to DDBB format.
                var error = "";
                var apiType = $('#apiType').val() ;
                formatData();
				if (!formatDates('#datecreated')){
					error = "";
				} 
				
				if (error == "" && operations.length==0 && apiType=="INTERNAL_ONTOLOGY") {
					error = apiCreateReg.apimanager_noops_error;
				}
				if (error == "" && !validateDescOperations()) {
					error = apiCreateReg.apimanager_ops_description_error;
				}
				if(myCodeMirrorJsExternal.getValue() != ""){
					$('#postProcessFx').val(myCodeMirrorJsExternal.getValue());
				}
				if (error == ""){
					form.submit();
				} else { 
					showGenericErrorDialog('ERROR', error);
				}				
            }
        });
    }
		
	function validateDescOperations(){
		var ontology = $("#ontology option:selected").text();
	    if ((ontology!=null) && (ontology.length!=0)){
            if ((($('#GETAll').attr('class')=='op_button_selected')&&($("#description_GETAll").val()== ""))
            	|| (($('#GET').attr('class')=='op_button_selected')&&($("#description_GET").val()== ""))
        		|| (($('#POST').attr('class')=='op_button_selected')&&($("#description_POST").val()== ""))
        		|| (($('#PUT').attr('class')=='op_button_selected')&&($("#description_PUT").val()== ""))
        		|| (($('#DELETEID').attr('class')=='op_button_selected')&&($("#description_DELETEID").val()== ""))){
            		return false;
            }
	    } else if (operations.length=0) {
	    	return false;
	    }
		return true;
	}
	
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> selectpickers, datepickers, resetForm, today->dateCreated currentLanguage: ' + currentLanguage) : '';
		
		
		// authorization tab control 
		$(".nav-tabs a[href='#tab_2']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			$.alert({title: 'INFO!', theme: 'light', content: 'CREATE API THEN GIVE AUTHORIZATIONS!'});
			return false;
		  }
		});
		
		// set current language and formats
		currentLanguage = apiCreateReg.language || LANGUAGE[0];
		currentFormat = (currentLanguage == 'es') ? 'dd/mm/yyyy' : 'mm/dd/yyyy';		
		
		logControl ? console.log('|---> datepickers currentLanguage: ' + currentLanguage) : '';
		
		// init datepickers dateCreated and dateDeleted		
		$("#datecreated").datepicker({dateFormat: currentFormat, showButtonPanel: true,  orientation: "bottom auto", todayHighlight: true, todayBtn: "linked", clearBtn: true, language: currentLanguage});
		
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('api_create_form');
		});
		
		// INSERT MODE ACTIONS  (apiCreateReg.actionMode = NULL ) 
		if ( apiCreateReg.actionMode === null){
			logControl ? console.log('action-mode: INSERT') : '';
			
			//set TODAY to dateCreated depends on language
			var f = new Date();         
			today = (currentLanguage == 'es') ? ('0' + (f.getDate())).slice(-2) + "/" + ('0' + (f.getMonth()+1)).slice(-2) + "/" + f.getFullYear() : ('0' + (f.getMonth()+1)).slice(-2) + "/" + ('0' + (f.getDate())).slice(-2) + "/" + f.getFullYear();
			$('#datecreated').datepicker('update',today);
			
			// INPUT MASK FOR ontology identification allow only letters, numbers and -_
			$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		}
		// EDIT MODE ACTION 
		else {
			createOperationsOntology();
			loadOperations();
			
			$('#id_endpoint').val($('#id_endpoint_hidden').val());
			
			// set DATE created in EDIT MODE
			logControl ? console.log('action-mode: UPDATE') : '';
			var f = new Date(apiCreateReg.dateCreated);
			regDate = (currentLanguage == 'es') ? ('0' + (f.getDate())).slice(-2) + "/" + ('0' + (f.getMonth()+1)).slice(-2) + "/" + f.getFullYear() : ('0' + (f.getMonth()+1)).slice(-2) + "/" + ('0' + (f.getDate())).slice(-2) + "/" + f.getFullYear();
			$('#datecreated').datepicker('update',regDate);
			
			initAuthorization(apiCreateReg.authorizations);
		}initAuthorization
	}
	
    function replaceOperation(newOp){
        for(var i=0; i<operations.length; i+=1){
            var operation = operations [i];
            if (operation.identification == newOp.identification){
            	operations [i] = newOp;
            }
        }
    }
	
    function existOp(op_name){
        for(var i=0; i<operations.length; i+=1){
            var operation = operations [i];
            if (operation.identification == op_name){
                return true;
            }
        }
        return false;
    }
	
    function formatData(){
    	$('#id_endpoint_hidden').val($('#id_endpoint').val());

        var ontology = $("#ontology option:selected").text();
        if ((ontology!=null) && (ontology.length!=0)){
            var nameApi = $('#identification').val();
            
            var querystringparameter;
            if ($('#GETAll').attr('class')=='op_button_selected'){
            	var querystringsGETAll = new Array();
            	var operationGETAll = {identification: nameApi + "_GETAll", description: $('#description_GETAll').val() , operation:"GET", path: $('#description_GETAll_label').text(), querystrings: querystringsGETAll};
	            //querystringparameter = {name: "id", dataType: "STRING", headerType: "PATH", description: ""};
	           // operationGET.querystrings.push(querystringparameter);
                if (!existOp(operationGETAll.identification)){
                	operations.push(operationGETAll);
                } else {
                    replaceOperation(operationGETAll);
                }
            }
            if ($('#GET').attr('class')=='op_button_selected'){
            	var querystringsGET = new Array();
            	var operationGET = {identification: nameApi + "_GET", description: $('#description_GET').val() , operation:"GET", path: $('#description_GET_label').text(), querystrings: querystringsGET};
	            querystringparameter = {name: "id", dataType: "STRING", headerType: "PATH", description: ""};
	            operationGET.querystrings.push(querystringparameter);
                if (!existOp(operationGET.identification)){
                	operations.push(operationGET);
                } else {
                    replaceOperation(operationGET);
                }
            }
            if ($('#POST').attr('class')=='op_button_selected'){
            	var querystringsPOST = new Array();
            	var operationPOST = {identification: nameApi + "_POST", description: $('#description_POST').val() , operation:"POST", path:$('#description_POST_label').text(), querystrings: querystringsPOST};
	            querystringparameter = {name: "body", dataType: "STRING", headerType: "BODY", description: "", value: ""};
	            operationPOST.querystrings.push(querystringparameter);
                if (!existOp(operationPOST.identification)){
                	operations.push(operationPOST);
                } else {
                    replaceOperation(operationPOST);
                }
            }
            if ($('#PUT').attr('class')=='op_button_selected'){
            	var querystringsPUT = new Array();
            	var operationPUT = {identification: nameApi + "_PUT", description: $('#description_PUT').val() , operation:"PUT", path:$('#description_PUT_label').text(), querystrings: querystringsPUT};
	            querystringparameter = {name: "body", dataType: "STRING", headerType: "BODY", description: "", value: ""};
	            operationPUT.querystrings.push(querystringparameter);
	            querystringparameter = {name: "id", dataType: "STRING", headerType: "PATH", description: ""};
	            operationPUT.querystrings.push(querystringparameter);
                if (!existOp(operationPUT.identification)){
                	operations.push(operationPUT);
                } else {
                    replaceOperation(operationPUT);
                }
            }
            if ($('#DELETEID').attr('class')=='op_button_selected'){
            	var querystringsDELETEID = new Array();
            	var operationDELETEID = {identification: nameApi + "_DELETEID", description: $('#description_DELETEID').val() , operation:"DELETE", path:$('#description_DELETEID_label').text(), querystrings: querystringsDELETEID};
	            querystringparameter = {name: "id", dataType: "STRING", headerType: "PATH", description: ""};
	            operationDELETEID.querystrings.push(querystringparameter);
                if (!existOp(operationDELETEID.identification)){
                	operations.push(operationDELETEID);
                } else {
                    replaceOperation(operationDELETEID);
                }
            }
            
            $("#operationsObject").val(JSON.stringify(operations));
            $("#authenticationObject").val(JSON.stringify(authentication));
        }
    }
    
    function validateImgSize() {
        if ($('#image').prop('files') && $('#image').prop('files')[0].size>60*1024){
        	showGenericErrorDialog('Error', apiCreateReg.apimanager_image_error);
        	$('#image').val("");
         } else if ($('#image').prop('files')) {
        	 reader.readAsDataURL($("#image").prop('files')[0]);
         }
    }
    
    function initAuthorization(authorizationArray){
    	
    	for(var i=0; i<authorizationArray.length; i+=1){
            var authElement = authorizationArray [i];
       
			var propAuth = {"users": authElement.userId, "usersFullName": authElement.userFullName, "id": authElement.id};
			authorizationsArr.push(propAuth);
			console.log('     |---> JSONtoTable: ' + authorizationsArr.length + ' data: ' + JSON.stringify(authorizationsArr));
			// store ids for after actions.	inside callback 				
			var user_id = authElement.userId;
			var auth_id = authElement.id;
			var AuthId = {[user_id]:auth_id};
			authorizationsIds.push(AuthId);
			console.log('     |---> Auths: ' + authorizationsIds.length + ' data: ' + JSON.stringify(authorizationsIds));
								
			// TO-HTML
			if ($('#authorizations').attr('data-loaded') === 'true'){
				$('#api_authorizations > tbody').html("");
				$('#api_authorizations > tbody').append(mountableModel2);
			}
			console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
			$('#api_authorizations').mounTable(authorizationsArr,{
				model: '.authorization-model',
				noDebug: false							
			});
			
			// hide info , disable user and show table
			$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));			
			$("#users").selectpicker('deselectAll');
			$("#users").selectpicker('refresh');
			$('#authorizations').removeClass('hide');
			$('#authorizations').attr('data-loaded',true);
    	}
    }
    
    
    
	var authorization = function(action,api,user,authorization,btn){
		logControl ? console.log('|---> authorization()') : '';	
		var insertURL = apiCreateReg.authorizationsPath + '/authorization';
		var deleteURL = apiCreateReg.authorizationsPath + '/authorization/delete';
		var response = {};
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		if (action === 'insert'){
			console.log('    |---> Inserting... ' + insertURL);
			
			var authorized=false;
			
			for(var i=0; i<authorizationsIds.length; i+=1){
				var authElement = authorizationsIds [i];
				authorized = authElement.hasOwnProperty(user) || authorized;
			}
			
			if (!authorized){
				
				$.ajax({
					url:insertURL,
	                headers: {
						[csrf_header]: csrf_value
				    },
					type:"POST",
					async: true,
					data: {"api": api,"user": user},			 
					dataType:"json",
					success: function(response,status){							
						
						var propAuth = {"users":user, "usersFullName": response.userFullName, "id": response.id};
						authorizationsArr.push(propAuth);
						console.log('     |---> JSONtoTable: ' + authorizationsArr.length + ' data: ' + JSON.stringify(authorizationsArr));
						// store ids for after actions.	inside callback 				
						var user_id = user;
						var auth_id = response.id;
						var AuthId = {[user_id]:auth_id};
						authorizationsIds.push(AuthId);
						console.log('     |---> Auths: ' + authorizationsIds.length + ' data: ' + JSON.stringify(authorizationsIds));
											
						// TO-HTML
						if ($('#authorizations').attr('data-loaded') === 'true'){
							$('#api_authorizations > tbody').html("");
							$('#api_authorizations > tbody').append(mountableModel2);
						}
						console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
						$('#api_authorizations').mounTable(authorizationsArr,{
							model: '.authorization-model',
							noDebug: false							
						});
						
						// hide info , disable user and show table
						$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));			
						$("#users").selectpicker('deselectAll');
						$("#users").selectpicker('refresh');
						$('#authorizations').removeClass('hide');
						$('#authorizations').attr('data-loaded',true);
						
					}
				});	
				
			}	
			
		}
		if (action  === 'delete'){
			console.log('    |---> Deleting... ' + user + ' with authId:' + authorization );
			
			$.ajax({url:deleteURL, type:"POST", async: true, 
                headers: {
					[csrf_header]: csrf_value
			    },
				data: {"id": authorization},			 
				dataType:"json",
				success: function(response,status){									
					
					// remove object
					var removeIndex = authorizationsIds.map(function(item) { return item[user]; }).indexOf(authorization);			
					authorizationsIds.splice(removeIndex, 1);
					authorizationsArr.splice(removeIndex, 1);
					
					console.log('AuthorizationsIDs: ' + JSON.stringify(authorizationsIds));
					// refresh interface. TO-DO: EL this este fallará					
					if ( response  ){ 
						$(btn).closest('tr').remove();
					}
					else{ 
						$.alert({title: 'ALERT!', theme: 'dark', type: 'orange', content: 'VACIO!!'}); 
					}
				}
			});			
		}	
	};
	
	// return position to find authId.
	var foundIndex = function(what,item,arr){
		var found = '';
		arr.forEach(function(element, index, array) {
			if ( what === element[item]){ found = index;  console.log("a[" + index + "] = " + element[item] + ' Founded in position: ' + found ); } 
			
		});		
		return found;
	}

	var handleCodeMirrorJson = function () {
        var myTextArea = document.getElementById('jsonTextArea');
        myCodeMirror = CodeMirror.fromTextArea(myTextArea, {
        	mode: "application/ld+json",
        	autoCloseBrackets: true,
            matchBrackets: true,
            styleActiveLine: true,
            theme:"elegant",
            lineWrapping: true

        });
		myCodeMirror.setSize("100%", 500);
		if(apiCreateReg.actionMode != null && apiCreateReg.apiType == 'EXTERNAL_FROM_JSON'){
			try{
				JSON.parse(myCodeMirror.getValue());
				myCodeMirror.setValue(js_beautify(myCodeMirror.getValue()));
			}catch(error){
				
			}
			
			myCodeMirror.refresh();
		}
    };
    
    
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		// SHOW ERROR DIALOG
		showErrorDialog: function(dialogTitle, dialogContent) {
			logControl ? console.log(LIB_TITLE + ': showErrorDialog(dialogTitle, dialogContent)') : '';
			showGenericErrorDialog(dialogTitle, dialogContent);
		},
		
		// DELETE CONFIRMATION
		deleteConfirmation: function(id) {
			logControl ? console.log(LIB_TITLE + ': deleteConfirmation(id)') : '';
			deleteAPIConfirmation(id);
		},
		
		// VALIDATE IMAGE SIZE
		validateImageSize: function() {
			logControl ? console.log(LIB_TITLE + ': validateImgSize()') : '';
			validateImgSize();
		},
		
		// CHANGE API CACHE TIMEOUT
		changeCacheTimeout: function() {
			logControl ? console.log(LIB_TITLE + ': changeCacheTimeout()') : '';
			updateCacheTimeout();
		},
			
		// CHANGE API LIMIT
		changeApiLimit: function() {
			logControl ? console.log(LIB_TITLE + ': changeApiLimit()') : '';
			updateApiLimit();
		},
		
		// CALCULATE VERSIONS
		calculateNumVersion: function() {
			logControl ? console.log(LIB_TITLE + ': calculateNumVersion()') : '';
			calculateVersion();
		},
		
		// SELECT OPERATIONS
		selectOp: function(button) {
			logControl ? console.log(LIB_TITLE + ': selectOp()') : '';
			selectOperation(button);
		},
		
		// SELECT OPERATIONS
		existOperation: function(name) {
			logControl ? console.log(LIB_TITLE + ': existOperation(name)') : '';
			return existOp(name);
		},
		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return apiCreateReg = Data;
		},	
		
		
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';			
			handleValidation();
			handleCodeMirrorJson();
			initTemplateElements();		
			if(apiCreateReg.ontologyId != null){
				$('#ontology option').each(function(){
					if($(this).text() == apiCreateReg.ontologyId)
						$('#ontology').val($(this).val()).change();
				})
				
			}
		},
		
		// INSERT AUTHORIZATION
		insertAuthorization: function(){
			logControl ? console.log(LIB_TITLE + ': insertAuthorization()') : '';
			if ( apiCreateReg.actionMode !== null){	
				// UPDATE MODE ONLY AND VALUES on user
				if (($('#users').val() !== '') && ($("#users option:selected").attr('disabled') !== 'disabled')){
					
					// AJAX INSERT (ACTION,APIID,USER) returns object with data.
					authorization('insert',apiCreateReg.apiId,$('#users').val(),'');
								
				}	
			}
		},
		
		// REMOVE authorization
		removeAuthorization: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeAuthorization()') : '';
			if ( apiCreateReg.actionMode !== null){
				
				// AJAX REMOVE (ACTION,APIID,USER) returns object with data.
				var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
				
				var removeIndex = foundIndex(selUser,'users',authorizationsArr);				
				var selAuthorizationId = authorizationsIds[removeIndex][selUser];
				
				console.log('removeAuthorization:' + selAuthorizationId);
				
				authorization('delete',apiCreateReg.apiId, selUser, selAuthorizationId, obj);				
			}
		},
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		cancel: function(id,url){
			logControl ? console.log(LIB_TITLE + ': cancel()') : '';
			
			freeResource(id,url);
		},
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	ApiCreateController.load(apiCreateJson);	
		
	// AUTO INIT CONTROLLER.
	ApiCreateController.init();
});
