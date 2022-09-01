var fields;


function generateJsonSimulationFields() {
	//return map
	var jsonMap = {};
	//Ontology fields
	var keys = Object.keys(fields);
	//For every field
	for (var i = 0; i < keys.length; i++) {
		var key = keys[i].replace(/\./g,"--");
		var inputs = $('#'+key+'Div input');
		var object = {};
		var functionSelected = $('#'+key).val();
		if(functionSelected == defaultOption) functionSelected = 'NULL';
		object['function'] = functionSelected;
		//For every input get value an add to object
		for (var j = 0; j < inputs.length; j++) {
			object[inputs.get(j).name] = inputs.get(j).value; 
		}

		jsonMap[keys[i]] = object;
	}
	$('#jsonMap').val(JSON.stringify(jsonMap));

}
function getTokensAndOntologies() {
	if($('#clientPlatforms').val()!==''){
		var payload = { 'clientPlatformId': $("#clientPlatforms").val()};
		payload[headerJson.csrfParameterName]=headerJson.csrfToken;
		$("#ontologiesAndTokens").load('/controlpanel/devicesimulation/ontologiesandtokens', payload,getOntologyFields);
		$("#ontologiesAndTokens").show();
		$("#json_check").show();
		$("#interval").show();
		$("#row-panel-info").show();
	}else{
		$("#ontologiesAndTokens").hide();
		$("#json_check").hide();
		$("#tabs").hide();
		$("#ontologyFields").hide();
		$("#row-panel-info").hide();
	}
		
}
	
function getOntologyFields() {
	var payload = { 'ontologyIdentification': $("#ontologies").val()};
	payload[headerJson.csrfParameterName]=headerJson.csrfToken;
	$("#ontologyFields").load('/controlpanel/devicesimulation/ontologyfields',payload, function(data){editFieldParameters()});
	$("#tabs").show();
	$("#ontologyFields").show();
}

function editFieldParameters() {
	
	if(simulationJson != null && simulationJson.fields != null)
	{
		var keys = Object.keys(fields);
		for (var i = 0; i < keys.length; i++) {
			var key = keys[i].replace(/\./g,"--");
			var hiddenDiv= key+ 'Div';
			$('#'+key).val(simulationJson['fields'][keys[i]]['function']);
			var functionSelected = $('#'+key).val();
			$('#'+hiddenDiv).html($('#'+functionSelected).html());;
			var inputs = $('#'+key+'Div input');

			
			
			if(functionSelected.toLowerCase().indexOf("date") != -1) {
				setDateTimePicker(inputs);
				for (var j = 0; j < inputs.length; j++) {
					inputs.get(j).value = simulationJson['fields'][keys[i]][inputs.get(j).name];
				}
			}else {
				for (var j = 0; j < inputs.length; j++) {
					inputs.get(j).value = simulationJson['fields'][keys[i]][inputs.get(j).name];
				}
			}

			$('#'+hiddenDiv).show();
			
		}
	}
}
function setFieldSimulator(field) {
	$("[name="+field+"]").val($("#simulator"+field).val());
}
function navigateUrl(url){  window.location.href = url;	}

function cancelGo (id,url){
	console.log('freeResource() -> id: '+ id);
	$.get("/controlpanel/devicesimulation/freeResource/" + id).done(
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
	
function handleValidation() {
	
	var form1 = $('#simulation_create_form');
	
	//logControl ? console.log('handleValidation() -> ') : '';
    // for more info visit the official plugin documentation: 
    // http://docs.jquery.com/Plugins/Validation
	
	// set current language
	currentLanguage = currentLanguage || LANGUAGE;
	
    form1.validate({
        errorElement: 'span', //default input error message container
        errorClass: 'help-block help-block-error', // default input error message class
        focusInvalid: false, // do not focus the last invalid input
        ignore: ":hidden:not('  .hidden-validation')", // validate all fields including form hidden input but not selectpicker
		lang: currentLanguage,
		// validation rules
        rules: {
        	identification:	{required: true, minlength: 9},
        	clientPlatform: {required: true}
        },
        messages: {
        	identification: 	{ required: dataModelCreateJson.validform.emptyfields, minlength: dataModelCreateJson.validform.minLength},
        	clientPlatform:		{ required: dataModelCreateJson.validform.emptyfields}
        },
        invalidHandler: function(event, validator) { //display error alert on form submit
        	toastr.error(messagesForms.validation.genFormError,'');
        },
        errorPlacement: function(error, element) {
            if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
			else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list, .radio-inline")); }
			else { error.insertAfter(element); }
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
        	validateSpecialComponentsAndSubmit(form, true);
        }
    });
}

function validateSpecialComponentsAndSubmit (form, submit) {
	
	var csrfParam = headerJson.csrfParameterName;
	var csrfToken = headerJson.csrfToken;	
	
	var form1 = $('#simulation_create_form');
    
	if ($('#interval').val() <= "0"){
		$('#interval').closest('.form-group').addClass('has-error');
		$('#intervalerror').removeClass('hide');
		$('#intervalerror').addClass('help-block help-block-error');
		submit = false;
	}
	if($("#from-json").is(':checked')){
		$('#jsonInstances').val(JSON.stringify(editor.get()));
		var payload = {'ontology': $("#ontologies").val(),'json': JSON.stringify(editor.get())}
		payload[csrfParam] = csrfToken;
		$.ajax({
			  type: 'POST',
			  url: '/controlpanel/devicesimulation/checkjson',
			  data: payload,
			  success: function(response) {
				  if(response != 'ok'){
					toastr.error(messagesForms.operations.genOpError, response);
					submit = false;
				  }
			  },
			  async:false
			});
		$('#jsonMap').val('');
	} else {
		generateJsonSimulationFields();
		$('#jsonInstances').val('');
	}
	if (submit){
		toastr.success(messagesForms.validation.genFormSuccess,'');
		form.submit();
	} else {
		toastr.error(messagesForms.validation.genFormError,'');
	}
}

function generateSimulatorFunctionDiv(field) {

	//Hidden div of the ontology Field
	var hiddenDiv= field+ 'Div';
	//Simulator function selected of the curren ontology field
	var functionSelected = $('#'+field).val();
	//If not function NULL
	if(functionSelected != 'NULL') {
		//html insert Auxiliar Div of the selected simulator function
		$('#'+hiddenDiv).html($('#'+functionSelected).html());
		//Assing unique ID to each input of the hiddenDiv
		var inputs = $('#'+hiddenDiv+' input');
		for (var i = 0; i < inputs.length; i++) {
			inputs.get(i).id = field + inputs.get(i).id;
		}
		//show
		if(functionSelected.toLowerCase().indexOf("date") != -1) {
			setDateTimePicker($('#'+hiddenDiv+' input'));
		}
		$('#'+hiddenDiv).show();
	}else {
		//IF NULL THEN DELETE INNER HTML
		$('#'+hiddenDiv).html('');
	}

}

var cleanFields = function(formId){
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
}

//CLEANING SELECTs
$(".selectpicker").each(function(){
	$(this).val( '' );
	$(this).selectpicker('deselectAll').selectpicker('refresh');
});

var  deleteSimulation= function (id){
		console.log('deleteSimulationConfirmation() -> id: '+ id);
		
		// no Id no fun!
		if ( !id ) {$.alert({title: 'Error', theme: 'light', content: 'NO SIMULATION SELECTED!'}); return false; }
		
		// call  Confirm 
		showConfirmDeleteDialog(id);	
	} 
	
	
	var showConfirmDeleteDialog = function(id){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.deviceSimulationConfirm;
		var Title = headerReg.deviceSimulationDelete;

		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		
		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
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
						console.log(id);
						$.ajax({
						    url: '/controlpanel/devicesimulation/'+id,
						    headers: {
								[csrf_header]: csrf_value
						    },
						    type: 'DELETE',						  
						    success: function(result) {
						    	if(result == 'ok') {navigateUrl('/controlpanel/devicesimulation/list');}
						    }
						});
					}											
				}	
			}
		});
	}	