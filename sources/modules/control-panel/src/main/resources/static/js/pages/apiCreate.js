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
	var mountableModel;
	var oTable;
	var subscriptions = [];
	
	if ($('#api_authorizations').find('tr.authorization-model')[0]){
		mountableModel2 = $('#api_authorizations').find('tr.authorization-model')[0].outerHTML;
	}
    
	reader.onload = function (e) {
        $('#showedImg').attr('src', e.target.result);
    }
	
	
	function showHideImageTableOntology(){
		if(typeof $('#api_authorizations > tbody > tr').length =='undefined' || $('#api_authorizations > tbody > tr').length == 0 || $('#api_authorizations > tbody > tr > td > input')[0].value==''){
			$('#imageNoElementsOnTable').show();
		}else{
			$('#imageNoElementsOnTable').hide();
		}
	}

	// CONTROLLER PRIVATE FUNCTIONS	
    var showGenericErrorDialog= function(dialogTitle, dialogContent){		
		logControl ? console.log('showErrorDialog()...') : '';
		var Close = headerReg.btnCancelar;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: dialogTitle,
			theme: 'light',
			content: dialogContent,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {				
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				}
			}
		});			
	}
    
	 function deleteAPIConfirmation (){
			
			var id = apiCreateReg.apiId;
			console.log('deleteAPIConfirmation() -> formId: '+ id);
			
			// no Id no fun!
			if ( !id ) {$.alert({title: 'Error',theme: 'light', content: 'NO USER-FORM SELECTED!'}); return false; }
			
			// call  Confirm 
			showConfirmDeleteDialog(id);	
		} 
    
	function showConfirmDeleteDialog(id){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Title = headerReg.apiDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: apiCreateReg.apimanager_delete_confirm,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
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
                        //configurarApi();
                        checkOntologyRTDB();
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
        	ontologySelector.selectpicker('refresh');
        	$('#row-json').addClass('hide');
        	$('.common-ops').removeClass('hide');
        	$('#row-operations').removeClass('hide');
        	$('#divCUSTOMSQL').removeClass('hide');
        	$('#row-panel-info').removeClass('hide');
            createOperationsOntology();
        }
        if(apiType && apiType.startsWith('EXTERNAL_FROM_JSON')) {
        	ontologySelector.val( '' );
        	ontologySelector.prop('disabled', true);
        	ontologySelector.selectpicker('refresh');
        	$('#row-operations').addClass('hide');
        	$('.common-ops').addClass('hide');
        	$('#divCUSTOMSQL').addClass('hide');
        	$('#row-json').removeClass('hide');
        	$('#row-panel-info').removeClass('hide');
        	$('.form-group-ontology').removeClass('has-error');
        	$('span[id^="ontology-error"]').remove();
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
				//NODE-RED disable edit elements 
	 			apiType = $('#apiType').val();
				if (apiType && apiType.startsWith('NODE_RED')) { 
					$('#getAllElements_Eliminar').addClass('hide');
					$('.op_button_div > i').addClass('hide');
					$('.op_button').attr("disabled", true);
				}
            }
            let rtdbSelected = $("#ontology option:selected").data('rtdb');
    		if(rtdbSelected === 'AI_MINDS_DB'){
    			$('#row-operations').addClass('hide');
    			$('#row-operations-nebula').addClass('hide');
    			$('#row-operations-ai').removeClass('hide');
    		}else if(rtdbSelected === 'NEBULA_GRAPH'){
    			$('#row-operations').removeClass('hide');
    			$('.common-ops').addClass('hide');
    			$('#row-operations-ai').addClass('hide');
    		}else{
    			$('.common-ops').removeClass('hide');
    			if(apiCreateReg.apiType === 'IOT' || apiCreateReg.apiType === 'INTERNAL_ONTOLOGY' || apiCreateReg.apiType === 'NODE_RED'){
    				$('#row-operations').removeClass('hide');
    			}
    			$('#row-operations-nebula').addClass('hide');
    			$('#row-operations-ai').addClass('hide');
    		}
        } catch (err) {
            console.log('Fallo cargando operaciones',err);
            $('.capa-loading').hide();
        }
    }
	
	function isDefaultOp(idOp){
		if (idOp.endsWith("_GETAll") || idOp.endsWith("_GET") || idOp.endsWith("_POST") || idOp.endsWith("_PUT") || idOp.endsWith("_DELETEID") || idOp.endsWith("_POSTAI") || idOp.endsWith("_POSTNebula")){
			return true;
		} else {
			return false;
		}
	}
	
    function createOperationsOntology () {
    	$('#description_GETAll_label').text("/");
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
    		$('#buttonOperacion' + button.name).find("i").toggleClass("fa-angle-down fa-angle-up");
    		if(button.name === 'POSTAI'){
    			$('#description_POSTAI').val('Data prediction');
    		}
    		if(button.name === 'POSTNebula'){
    			$('#description_POSTNebula').val('Execute nGQL query');
    		}
    	} else if (button.className=='op_button_selected'){
    		button.className='op_button';
    		$('#description_' + button.name).val("");
    		$('#descOperation' + button.name).hide();
    		$('#div' + button.name).prop('className', 'op_div');
    		$('#buttonOperacion' + button.name).find("i").toggleClass("fa-angle-up fa-angle-down");
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
		$('#'+formId).find('input:text, input:password, input:file,input:text, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remote" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		// CLEANING NUMBER INPUTS
		$(':input[type="number"]').val('');
		
		// CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		$('.tagsinput').prev().removeClass('tagsinput-has-error');
		$('.tagsinput').nextAll('span:first').addClass('hide');
		
		//CLEAN CODEMIRROR
		if (myCodeMirror.getValue() != ""){
			myCodeMirror.setValue('');
		}
		
		operations = [];
		$('#divCUSTOMSQLS').empty();
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
	
	var validateMetaInf = function () {
    	if ($('#id_metainf').val() === '' || $('#id_metainf').val().length < 5 ){
    		$('#id_metainf').prev().addClass('tagsinput-has-error');
    		$('#id_metainf').nextAll('span:first').removeClass('hide');
    		$('#metainferror').addClass('hide');
    		return false;
		} else {
    		$('#id_metainf').prev().removeClass('tagsinput-has-error');
    		$('#metainferror').removeClass('hide');
    		$('#id_metainf').nextAll('span:first').addClass('hide');
    		return true;
		}
	}
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#api_create_form');
	
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
            	id_metainf:			{ required: true },
				datecreated:		{ date: true, required: true }
            },
            invalidHandler: function(event, validator) { //display error alert on form submit  
            
            if ($('#metainf').val() !== ''){
        			$('#metainferror').addClass('hide');
        			$('#id_metainf').closest('.form-group').removeClass('has-error');
        			$('#id_metainf').prev().removeClass('tagsinput-has-error');;
        		} else {
        			$('#metainferror').removeClass('hide');
        			$('#id_metainf').closest('.form-group').addClass('has-error');
        			$('#id_metainf').prev().addClass('tagsinput-has-error');
        		}               
            	toastr.error(messagesForms.validation.genFormError,'');
                validateMetaInf();
                validateDescription();
            },
            errorPlacement: function(error, element) {
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }
				else 								{ error.insertAfter(element); }
            },
            highlight: function(element) { // hightlight error inputs
                $(element).closest('.form-group').addClass('has-error'); 
                $(element).closest('.form-group-ontology').addClass('has-error'); 
            },
            unhighlight: function(element) { // revert the change done by hightlight
                $(element).closest('.form-group').removeClass('has-error');
                $(element).closest('.form-group-ontology').removeClass('has-error');
               
            },
            success: function(label) {
                label.closest('.form-group').removeClass('has-error');
                label.closest('.form-group-ontology').removeClass('has-error');
                
            },
			// ALL OK, THEN SUBMIT.
            submitHandler: function(form) {
				// date conversion to DDBB format.
                var error = "";
                var apiType = $('#apiType').val() ;
                formatData();
				if (!formatDates('#datecreated')){
					error = "";
				} 
				
				if (error == "" && !validateMetaInf()){
					error = apiCreateReg.apimanager_gen_error;
				} 
				if (error == "" && !validateDescription()){
					error = apiCreateReg.apimanager_gen_error;
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
					toastr.success(messagesForms.validation.genFormSuccess,'');
					form.attr("action", "?" + csrfParameter + "=" + csrfValue)
					form.submit();
				} else {
					toastr.error(messagesForms.validation.genFormError,error);
				}				
            }
        });
    }
    
    var validateDescription = function(){		
		var description = $('#apiDescripcion').val();
		var error1 = $('.alert-danger');
		if(typeof description === 'undefined' || description.trim().length < 5 || description == "" ){
			error1.show();
			$('#descriptionerror').removeClass('hide').addClass(' font-red');
			$('#apiDescripcion').closest('.form-group').addClass('has-error')
			return false;
		}else{
			$('#descriptionerror').addClass('hide');
			$('#apiDescripcion').closest('.form-group').removeClass('has-error')
			return true;
		}
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
		
		$('#id_metainf').on('itemAdded', function(event) {
			if ($(this).val() !== ''){ $('#metainferror').addClass('hide');}
		});

		// authorization tab control 
		$(".nav-tabs a[href='#tab_2']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			$.alert({title: 'INFO!', theme: 'light', content: apiCreateJson.validations.authinsert});
			return false;
		  }
		});
		
		$(".nav-tabs a[href='#tab_3']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			$.alert({title: 'INFO!', theme: 'light', content: apiCreateJson.validations.graviteeswagger});
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
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		
		
		$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.getElementsByTagName('select')[0]){
				$('.form').validate().element('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id'));
			}
		})
		$('#apiDescripcion').bind('blur', function (ev) { // fires on every blur
			validateDescription();             // checks form for validity
		})
		
			
		$('.tagsinput').filter('[required]').parent().on('blur', 'input', function(event) {
			if ($(event.target).parent().next().val() == ''){
				$(event.target).parent().next().nextAll('span:first').removeClass('hide');
				$(event.target).parent().next().nextAll('span:last-child').addClass('hide');
				$(event.target).parent().addClass('tagsinput-has-error');
			} else if($(event.target).parent().next().val().length < 5){
				$(event.target).parent().next().nextAll('span:last-child').addClass('font-red');
				$(event.target).parent().next().nextAll('span:last-child').removeClass('hide');
				$(event.target).parent().addClass('tagsinput-has-error');
			} else {
				$(event.target).parent().next().nextAll('span:first').addClass('hide');
				$(event.target).parent().next().nextAll('span:last-child').addClass('hide');
				$(event.target).parent().removeClass('tagsinput-has-error');
			}   
		})
	
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
			if(apiCreateReg.graviteeId != null && apiCreateReg.hasJWTPlan)
				mountableModel = $('#table_subscriptions').find('tr.subscriptions-model')[0].outerHTML;
			
			$('#id_endpoint').val($('#id_endpoint_hidden').val());
			
			// set DATE created in EDIT MODE
			logControl ? console.log('action-mode: UPDATE') : '';
			var f = new Date(apiCreateReg.dateCreated);
			regDate = (currentLanguage == 'es') ? ('0' + (f.getDate())).slice(-2) + "/" + ('0' + (f.getMonth()+1)).slice(-2) + "/" + f.getFullYear() : ('0' + (f.getMonth()+1)).slice(-2) + "/" + ('0' + (f.getDate())).slice(-2) + "/" + f.getFullYear();
			$('#datecreated').datepicker('update',regDate);
			
			initAuthorization(apiCreateReg.authorizations);
			
		    if ($('#checkboxCache').prop('checked')) {
		    	$('#id_cachetimeout').prop('disabled', false);
		    }

		    if ($('#checkboxLimit').prop('checked')) {
		    	$('#id_limit').prop('disabled', false);
		    }
		    
		    if(apiCreateReg.graviteeId && apiCreateReg.hasJWTPlan){
				applications = apiCreateReg.subscriptions;
				mountTableSubscriptions();
			}
		}
	}
	
	function subscribe(){
		let app = $('#apps').val()
		let apiId = apiCreateReg.apiId;
		fetch(`/controlpanel/api/apis/${apiId}/gravitee/subscribe?application=${app}`,
			{
			  method: 'POST'	
			}
		)
		.then(r => r.json())
		.then(data => {
			applications = data;
			mountTableSubscriptions();
		})
	}
	
	function unsubscribe(obj){
		let app = $(obj).closest('tr').find("input[name='applications\\[\\]']").val();
		let apiId = apiCreateReg.apiId;
		fetch(`/controlpanel/api/apis/${apiId}/gravitee/unsubscribe?application=${app}`,
			{
			  method: 'POST'	
			}
		)
		.then(r => r.json())
		.then(data => {
			applications = data;
			mountTableSubscriptions();
		})
	}
	
	function mountTableSubscriptions(){
		let subsArr = []
		$.each( applications, function (key, object){			
			
			subsArr.push({'applications': object, 'clientIds': object})
			
		});

		// TO-HTML
		if ($('#subscriptions').attr('data-loaded') === 'true'){
			$('#table_subscriptions > tbody').html("");
			$('#table_subscriptions > tbody').append(mountableModel);
		}
		$('#table_subscriptions').mounTable(subsArr,{
			model: '.subscriptions-model',
			noDebug: false							
		});
		
		// hide info , disable user and show table
					
		$('#subscriptions').removeClass('hide');
		$('#subscriptions').attr('data-loaded',true);// TO-HTML

	}
	
    function replaceOperation(newOp){
        for(var i=0; i<operations.length; i+=1){
            var operation = operations [i];
            if (operation.identification == newOp.identification){
            	operations [i] = newOp;
            }
        }
    }
	
    function existOp(op_name, method){
        for(var i=0; i<operations.length; i+=1){
            var operation = operations [i];
            if (operation.identification == op_name){
            	if(method !== null && typeof method !== 'undefined'){
            	  return operation.operation === method;
            	}else{
            	  return true;
            	}
            }
        }
        return false;
    }
    
    var initTable = function(){
	    oTable = $('#api_authorizations').DataTable({
			   columnDefs: [
			      {
			         targets: [0, 1],
			         type: 'string',
			         render: function(data, type, full, meta){
			            if (type === 'filter' || type === 'sort') {
			               var api = new $.fn.dataTable.Api(meta.settings);
			               var td = api.cell({row: meta.row, column: meta.col}).node();
			               data = $('select, input[type="text"]', td).val();
			               if (!data){
			            	   if (td.val){
			            		   data = td.val;
			            	   } else {
			            		   data=td.innerHTML;
			            	   }
			               }
			            }
			            return data;
			         }
			      }
			   ]
			});
			
		$('#api_authorizations_wrapper div.dataTables_filter').addClass('hide');
		$('#api_authorizations_wrapper > div.row').addClass('hide');
		
		$('#search-on-title').append($('#api_authorizations_wrapper div.dataTables_filter > label > input'));
		$('#search-on-title > input').css('height', 'auto');
		$('#search-on-title > input').removeClass('input-xsmall')
		
		if ($("#search-on-title").children().length>2){
			$("#search-on-title").find('input:first').remove();
		}
		
	}
    
    var refreshTable = function(){
		oTable.clear();
		oTable.destroy();
		
		// TO-HTML
		$('#api_authorizations > tbody').html("");
		$('#api_authorizations > tbody').append(mountableModel2);
		
		$('#api_authorizations').mounTable(authorizationsArr,{
			model: '.authorization-model',
			noDebug: false							
		});

		// hide info , disable user and show table
		$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));			
		$("#users").selectpicker('deselectAll');
		$("#users").selectpicker('refresh');					
	
		initTable();
		
		$('#authorizations').removeClass('hide');
		$('#authorizations').attr('data-loaded',true);
		showHideImageTableOntology();
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
            if ($('#POSTAI').attr('class')=='op_button_selected'){
            	var querystringsPOST = new Array();
            	var operationPOST = {identification: nameApi + "_POSTAI", description: $('#description_POSTAI').val() , operation:"POST", path:$('#description_POSTAI_label').text(), querystrings: querystringsPOST};
	            querystringparameter = {name: "body", dataType: "STRING", headerType: "BODY", description: "", value: ""};
	            operationPOST.querystrings.push(querystringparameter);
                if (!existOp(operationPOST.identification)){
                	operations.push(operationPOST);
                } else {
                    replaceOperation(operationPOST);
                }
            }
            if ($('#POSTNebula').attr('class')=='op_button_selected'){
            	var querystringsPOST = new Array();
            	var operationPOST = {identification: nameApi + "_POSTNebula", description: $('#description_POSTNebula').val() , operation:"POST", path:$('#description_POSTNebula_label').text(), querystrings: querystringsPOST};
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
        	 $('#imageName').removeClass('description');
        	 $('#imageName').text($("#image").prop('files')[0].name);
        	 
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
    	showHideImageTableOntology();
    }
    
    
    
	var authorization = function(action,api,user,authorization,btn){
		logControl ? console.log('|---> authorization()') : '';	
		var insertURL = apiCreateReg.authorizationsPath + '/authorization';
		var deleteURL = apiCreateReg.authorizationsPath + '/authorization/delete';
		var authorizationOnOntologyURL = apiCreateReg.authorizationsPath + '/authorizationOnOntology';
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
						
						refreshTable();
						
						toastr.success(messagesForms.operations.genOpSuccess,'');
					}
				});	
				// ajax : authorizationOnOntology
				$.ajax({
					url:authorizationOnOntologyURL,
	                headers: {
						[csrf_header]: csrf_value
				    },
					type:"POST",
					async: true,
					data: {"api": api,"user": user},			 
					dataType:"json",
					statusCode: {
	    				403: function() {	 
	    					toastr.warning('You have to give this user permission to the ontology before you can use this API');
	   				
	    				}
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
					
					// refresh interface				
					if ( response  ){ 
						refreshTable();

						toastr.success(messagesForms.operations.genOpSuccess,'');
					}
					else{ 
						toastr.error(messagesForms.operations.genOpError,'Empty Response!');
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
            theme:"material",
            lineWrapping: true

        });
		myCodeMirror.setSize("100%", 300);
		if(apiCreateReg.actionMode != null && apiCreateReg.apiType == 'EXTERNAL_FROM_JSON'){
			try{
				JSON.parse(myCodeMirror.getValue());
				myCodeMirror.setValue(js_beautify(myCodeMirror.getValue()));
			}catch(error){
				
			}
			
			myCodeMirror.refresh();
		}
    };
    
    // Init Code Mirror Gravitee
    var handleCodeMirrorGraviteeSwaggerDoc = function() {
    	if( $('#graviteeDocumentationAce').length ){
	        swaggerEditor = ace.edit("graviteeDocumentationAce");
	        swaggerEditor.setTheme("ace/theme/xcode");
	       	swaggerEditor.session.setMode("ace/mode/yaml");
	    	swaggerEditor.setOptions({showInvisibles:true});
	        swaggerEditor.setValue($('#graviteeDocumentation').val());
	        swaggerEditor.gotoLine(1);
    	}
    }
    
    // Save changes Gravitee Swagger Documentation
	var saveGraviteeSwaggerDocumentation = function(apiId, content) {
		var url =  apiCreateReg.authorizationsPath + '/updateGraviteeSwaggerDoc';
		var response = {};
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		$.ajax({
			url: url,
            headers: {
            	[csrf_header]: csrf_value
		    },
			type:"POST",
			async: true,
			data: {"apiId": apiId,"content": content},			 
			dataType:"json",
			success: function(response,status) {
				toastr.info(messagesForms.operations.genOpSuccess,apiCreateJson.graviteeSwaggerDocSaved);
			},
            error: function(data, status, error) {
            	var errorMessage = error;
				 if(typeof data.responseText !== 'undefined' ){
					 errorMessage = data.responseText;
				 }
				 toastr.error(messagesForms.operations.genOpError,errorMessage);
            }
		});	
						
	};
 
	var checkOntologyRTDB = function(){
		let rtdbSelected = $("#ontology option:selected").data('rtdb');
		if(rtdbSelected === 'AI_MINDS_DB'){
		    $('#row-operations').addClass('hide');
		    $('#row-operations-nebula').addClass('hide');
	        $('#divCUSTOMSQL').addClass('hide');
	        $('#row-panel-info').removeClass('hide');
	        $('#row-operations-ai').removeClass('hide');
	        $('#description_POSTAI_label').text("/predict");
	        $('#ontologyOperationsAI input[type="text"]').val('').show();
	        if(!$('#POSTAI').hasClass('op_button_selected')){
	        	$('#POSTAI').click();
	        }
	        $('#description_POSTAI').val('Data prediction');
		}else if (rtdbSelected === 'NEBULA_GRAPH'){
			$('#row-operations').removeClass('hide');
			$('.common-ops').addClass('hide');
//	        $('#divCUSTOMSQL').addClass('hide');
	        $('#row-panel-info').removeClass('hide');
	        $('#row-operations-ai').addClass('hide');
	        $('#row-operations-nebula').removeClass('hide');
	        $('#description_POSTNebula_label').text("/execute-ngql");
	        $('#ontologyOperationsNebula input[type="text"]').val('').show();
			if(!$('#POSTNebula').hasClass('op_button_selected')){
	        	$('#POSTNebula').click();
	        }
			$('#description_POSTNebula').val('Execute nGQL query');
		}else{
			$('#row-operations-ai').addClass('hide');
			$('#row-operations-nebula').addClass('hide');
			configurarApi();
			$('#description_POSTAI').val('Data prediction');
			$('#description_POSTNebula').val('Execute nGQL query');
		}
	}
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
		
		changeOntology: function() {
			checkOntologyRTDB();
		},
		
		// SELECT OPERATIONS
		selectOp: function(button) {
			logControl ? console.log(LIB_TITLE + ': selectOp()') : '';
			selectOperation(button);
		},
		
		// SELECT OPERATIONS
		existOperation: function(name, method) {
			logControl ? console.log(LIB_TITLE + ': existOperation(name)') : '';
			return existOp(name, method);
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
			initTable();
			handleCodeMirrorGraviteeSwaggerDoc();
		},
		
		// INSERT AUTHORIZATION
		insertAuthorization: function(){
			console.log("##########################INSERT#####################")
			logControl ? console.log(LIB_TITLE + ': insertAuthorization()') : '';
			existe=false;
			if ( apiCreateReg.actionMode !== null){	
				// UPDATE MODE ONLY AND VALUES on user
				if (($('#users').val() !== '') && ($("#users option:selected").attr('disabled') !== 'disabled')){
					
				    if ($("#api_authorizations > tbody > tr").length > 0) {
		                $("#api_authorizations > tbody > tr").each(
			
							function() {
								let fila = $(this).children().eq(0);
								
								if(fila.children().eq(0).val() == $('#users').val()){	
								   existe=true;
								   toastr.warning(messagesForms.validation.genOpexist);
								} 
							}
						);
					}
						
					// AJAX INSERT (ACTION,APIID,USER) returns object with data.
					if(!existe) {
						authorization('insert',apiCreateReg.apiId,$('#users').val(),'');
					}	
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
		
		// UPDATE GRAVITEE SWAGGER DOCUMENTATION
		updateGraviteeSwaggerDocumentation: function(){
			logControl ? console.log(LIB_TITLE + ': updateGraviteeSwaggerDocumentation()') : '';
			if ( apiCreateReg.actionMode !== null){	
				// UPDATE MODE ONLY 
				var content = swaggerEditor.getValue();
				if (content !== '') {
					saveGraviteeSwaggerDocumentation(apiCreateReg.apiId, content);
				}	
			}
		},
		subscribe: function(){
			subscribe();
		},
		unsubscribe: function(obj){
			unsubscribe(obj);
		}

	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	ApiCreateController.load(apiCreateJson);	
		
	// AUTO INIT CONTROLLER.
	ApiCreateController.init();
});
