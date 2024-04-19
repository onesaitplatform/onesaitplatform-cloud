var authorizationsArr 		= []; // add authorizations
var authorizationUpdateArr  = []; // get authorizations of the ontology
var authorizationsIds 		= []; // get authorizations ids for actions
var authorizationObj 		= {}; // object to receive authorizations responses.
var createKeyColumns 		= []; // object to receive key fields to create table
var createColumns 			= []; // object to receive fields to create table
var createKeyConstrains 	= []; // object to receive key table constarins to create table
var createConstrains 		= []; // object to receive table constarins to create table
var creationConfig          = {}; // object to save request params corresponding to OntologyConfiguration class
var selectedDatasource      = "PRESTO"; // object to save selected datasource
var historicalPartitions    = [];

var form1 = $('#ontology_create_form');

var OntologyCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Ontology Controller';	
    var logControl = 1;
	var LANGUAGE = ['es']
	var currentLanguage = ''; // loaded from template.	
	const PRESTO_PREFIX = "presto_";
	var csrf_value = $("meta[name='_csrf']").attr("content");
	var csrf_header = $("meta[name='_csrf_header']").attr("content");
	
	// CONTROLLER PRIVATE FUNCTIONS	--------------------------------------------------------------------------------
	
	var checkConnection = function(catalog) {
		var url = "/controlpanel/presto/datasources/checkConnection/" + catalog;
		$.ajax({
			url : url,
			headers: {
				[csrf_header]: csrf_value
			},
			type : 'GET',
			dataType: 'text', 
			contentType: 'text/plain',
			mimeType: 'text/plain',
			success : function() {				
			},
			error : function() {
				toastr.error(messagesForms.operations.genOpError, ontologyCreateReg.validations.connectionerror);
			}
		});
	}
	
	
	var getSchemas = function(catalog) {
		var urlCheckConnection = "/controlpanel/presto/datasources/checkConnection/" + catalog;
		var url = "/controlpanel/ontologies/getSchemas/PRESTO/" + catalog;

		$('#loading-collection').show();
		$.ajax({
			url : urlCheckConnection,
			headers: {
				[csrf_header]: csrf_value
			},
			type : 'GET',
			dataType: 'text', 
			contentType: 'text/plain',
			mimeType: 'text/plain',
			success : function() {		
				$.ajax({
					url : url,
					headers: {
						[csrf_header]: csrf_value
					},
					type : 'GET',
					dataType: 'text', 
					contentType: 'text/plain',
					mimeType: 'text/plain',
					success : function(data, _textStatus, xhr) {
						$('#schemas').empty();
						$('#datasourceCatalog').val(catalog);
						if (xhr.status === 204) {
							var schemas = [];
						} else {
							var schemas = JSON.parse(data);
						}
		
						var count = schemas.length;
						if (count == 1) {
							$('#schemas').append("<option value='" + schemas[0] + "' text='" + schemas[0] + "' >" + schemas[0] + "</option>");
							$('#schemas').selectpicker('refresh');
							$('#schemas').val(schemas[0]);
							$('#schemas').selectpicker('refresh');
							getTables(catalog, schemas[0]);
						}
						else if (count > 1) {
							$.each( schemas, function (key, object){
								$('#schemas').append("<option value='"+object+"' text='"+object+"' >"+object+"</option>");
								if (!--count) {
									$('#schemas').prop('disabled', false);
									$('#schemas').selectpicker('refresh');
								}
							});
						} else {
							toastr.info(messagesForms.operations.notification,'Datasource has not schemas');
							$('#schemas').prop('disabled', true);
							$('#schemas').selectpicker('refresh');
						}
					},
					error : function(data) {
						$('#schemas').empty();
						toastr.error(messagesForms.operations.genOpError, data.responseText);
					}
				});
			},
			error : function(data) {
				toastr.error(messagesForms.operations.genOpError, ontologyCreateReg.validations.connectionerror);
			}, 
			complete: function (){
				$('#loading-collection').hide();
			}
			
		});
	}
	
	var getTables = function(catalog, schema) {
		url = "/controlpanel/ontologies/getTables/PRESTO/db/" + catalog + "/sc/" + schema

		$('#loading-collection').show();
		$.ajax({
			url : url,
			headers: {
				[csrf_header]: csrf_value
			},
			type : 'GET',
			dataType: 'text', 
			contentType: 'text/plain',
			mimeType: 'text/plain',
			success : function(data, _textStatus, xhr) {
				$('#tables').empty();
				$('#datasourceSchema').val(schema);
				if (xhr.status === 204) {
					var tables = [];
				} else {
					var tables = JSON.parse(data);
				}

				var count = tables.length;
				if(count > 0)
					$.each( tables, function (key, object){
						$('#tables').append("<option value='"+object+"' text='"+object+"' >"+object+"</option>");
						$('#tablesConstraint').append("<option value='"+object+"' text='"+object+"' >"+object+"</option>");
						if (!--count) {
							$('#tables').prop('disabled', false);
							$('#tables').selectpicker('refresh');
						}
					});
				else {
					toastr.info(messagesForms.operations.notification,'Datasource has not tables');
					$('#tables').prop('disabled', true);
					$('#tables').selectpicker('refresh');
				}
			},
			error : function(data, status, err) {
				toastr.error(messagesForms.operations.genOpError,data.responseText);
				$('#tables').empty();
				$('#tables').prop('disabled', true);
				$('#tables').selectpicker('refresh');
			}, 
			complete: function (){
				$('#loading-collection').hide();
			}
		});	
	}

	$("#catalogs").on("change", function () {
		$('#schemas').empty();
		$('#schemas').prop('disabled', true);
		$('#schemas').selectpicker('refresh');	
		$('#tables').empty();
		$('#tables').prop('disabled', true);
		$('#tables').selectpicker('refresh');
		getSchemas($("#catalogs").val());

	})

	$("#schemas").on("change", function () {
		$('#tables').empty();
		$('#tables').selectpicker('refresh');	
	    getTables($("#catalogs").val(), $("#schemas").val());
	})

	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		logControl ? console.log('cleanFields() -> ') : '';
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
						
		//CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');

		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		$('.tagsinput').prev().removeClass('tagsinput-has-error');
		$('.tagsinput').nextAll('span:first').addClass('hide');
				
		// CLEAN ALERT MSG
		$('.alert-danger').hide();	
	}
	
	// VALIDATE IDENTIFICATION
	var validateIdName = function(){
		if ($('#identification').val().match(/^[0-9]/)) { 
			$('#identificationerror').removeClass('hide').addClass('help-block-error font-red'); 
			return false; 
		} else {
			return true;
		}
	}

	// VALIDATE TAGSINPUT
	var validateMetaInf = function(){
		if ($('#metainf').val() === ''){
			$('#metainf').prev().addClass('tagsinput-has-error');
			$('#metainf').nextAll('span:first').removeClass('hide');
			return false;
		} else {
			return true;
		}
	}
	
	var validateCatalog = function() {
		if ($("#catalogs").val() === '') {
			$('#catalogs-error').removeClass('hide');
			return true;
		} else {
			return false;
		}
	}
	
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation

					
		// set current language
		currentLanguage = ontologyCreateReg.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// validation rules
            rules: {
				ontologyId:		{ minlength: 5, required: true },
				identification:	{ minlength: 5, required: true },
				description:	{ minlength: 5, required: true }
            },
			// custom messages
            messages: {	
			},
            invalidHandler: function(event, validator) { //display error alert on form submit              
                validateIdName();
                validateMetaInf();
                toastr.error(messagesForms.validation.genFormError,'');
            },
            errorPlacement: function(error, element) {				
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }				
				else { error.insertAfter(element); }
            },
            highlight: function(element) { // hightlight error inputs
                $(element).closest('.form-group').addClass('has-error');
                $(element).nextAll('span:last-child').addClass('hide');
            },
            unhighlight: function(element) { // revert the change done by hightlight
                $(element).closest('.form-group').removeClass('has-error');
                $(element).nextAll('span:last-child').addClass('hide');
            },
            success: function(label) {
                label.closest('.form-group').removeClass('has-error');
            },
			// ALL OK, THEN SUBMIT.
            submitHandler: function(form) { 		
            	validateSpecialComponentsAndSubmit(true);
			}
        });
    }
	
	var validateSpecialComponentsAndSubmit = function (submit) {
        // VALIDATE IDENTIFICATION METAINF AND SCHEMA
		if (validateIdName() && validateMetaInf()){
			
			form1.ajaxSubmit({type: 'post', success : function(data){
					toastr.success(messagesForms.validation.genFormSuccess,'');
					navigateUrl(data.redirect);
				}, error: function(data){
					toastr.error(messagesForms.operations.genOpError,data.responseJSON.cause);
				}
			})
		} else {
			toastr.error(messagesForms.validation.genFormError,'');										
		}
	}
	
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() ->  resetForm,  currentLanguage: ' + currentLanguage) : '';
		
		// INPUT MASK FOR ontology identification allow only letters, numbers
		// and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_]*", greedy: false });

		
		// tagsinput validate fix when handleValidation()
		$('#metainf').on('itemAdded', function(event) {
			
			if ($(this).val() !== ''){ $('#metainferror').addClass('hide');}
		});

		$(".option a[href='#tab_1']").on("click", function(e) {
	        $('.tabContainer').find('.option').removeClass('active');
	        $('#tab-datos').addClass('active');
	    });
		
		// general template/schema tab control
		$(".option a[href='#tab_table_schema']").on("click", function(e) {
	        $('.tabContainer').find('.option').removeClass('active');
	        $('#tab-table-schema').addClass('active');
	    });
		
		// authorization tab control 
		$(".option a[href='#tab_2']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			toastr.info(messagesForms.operations.notification, ontologyCreateReg.validations.authinsert);
			return false;
		  } else {
			$('.tabContainer').find('.option').removeClass('active');
			$('#tab-authorizations').addClass('active');
		  }
		});	
	
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('ontology_create_form');
		});
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (event) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});
		
		$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.getElementsByTagName('select')[0]){
				$('.form').validate().element('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id'));
			}
		});

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
		}).on('keyup', function(){})
		.on('itemAdded', function(event) {});
	
		// INSERT MODE ACTIONS  (ontologyCreateReg.actionMode = NULL ) 
		if ( ontologyCreateReg.actionMode === null){
			logControl ? console.log('|---> Action-mode: INSERT') : '';
			
			// Set active 
			$('#active').trigger('click');
			
		}
		// EDIT MODE ACTION 
		else {	
			logControl ? console.log('|---> Action-mode: UPDATE') : '';
			
			$("#tables").val(ontologyCreateReg.collection);
			
			if(ontologyCreateReg.objectId != null && ontologyCreateReg.objectId != undefined && ontologyCreateReg.objectId != ""){
				
				$("#id").attr("checked", "checked");
				OntologyCreateController.schemaToTable();
				$("#fields").selectpicker('val', ontologyCreateReg.objectId);
			}
			
			
			
			// if ontology has authorizations we load it!.
			authorizationsJson = ontologyCreateReg.authorizations;			
			if (authorizationsJson.length > 0 ){
				
				// MOUNTING AUTHORIZATIONS ARRAY
				var authid_update, accesstype_update , userid_update , authorizationUpdate , authorizationIdUpdate = '';
				$.each( authorizationsJson, function (key, object){			
					
					authid_update 		= object.id; 
					accesstype_update 	= object.typeName; 
					userid_update 		= object.userId;					
					
					logControl ? console.log('      |----- authorizations object on Update, ID: ' +  authid_update + ' TYPE: ' +  accesstype_update + ' USER: ' +  userid_update  ) : '';
					
					// AUTHs-table {"users":user,"accesstypes":accesstype,"id": response.id}
					authorizationUpdate = {"users": userid_update, "accesstypes": accesstype_update, "id": authid_update};					
					authorizationsArr.push(authorizationUpdate);
					
					// AUTH-Ids {[user_id]:auth_id}
					authorizationIdUpdate = {[userid_update]:authid_update};
					authorizationsIds.push(authorizationIdUpdate);
					
					// disable this users on users select
					$("#users option[value=" + userid_update + "]").prop('disabled', true);
					$("#users").selectpicker('refresh');
					
				});

				// TO-HTML
				if ($('#authorizations').attr('data-loaded') === 'true'){
					$('#ontology_autthorizations > tbody').html("");
					$('#ontology_autthorizations > tbody').append(mountableModel2);
				}
				logControl ? console.log('authorizationsArr on UPDATE: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr)) : '';
				$('#ontology_autthorizations').mounTable(authorizationsArr,{
					model: '.authorization-model',
					noDebug: false							
				});
				
				// hide info , disable user and show table
				$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));					
				$('#authorizations').removeClass('hide');
				$('#authorizations').attr('data-loaded',true);// TO-HTML
				$("#users").selectpicker('deselectAll');
				
				$('#imageNoElementsOnTable').hide();
			}		
			
			// take schema from ontology and load it
			schema = ontologyCreateReg.schemaEditMode;
			editor.setText(schema);
			editor.setMode("view");
		}		
	}	

	
	// DELETE ONTOLOGY
	var deleteOntologyConfirmation = function(ontologyId){
		console.log('deleteOntologyConfirmation() -> formId: '+ ontologyId);
		
		// no Id no fun!
		if ( !ontologyId ) {
			toastr.error(messagesForms.operations.genOpError, ontologyCreateReg.validations.validform);
			return false; 
		}
		
		logControl ? console.log('deleteOntologyConfirmation() -> formAction: ' + $('.delete-ontology').attr('action') + ' ID: ' + $('#delete-ontologyId').attr('ontologyId')) : '';
		
		// call ontology Confirm at header. 
		HeaderController.showConfirmDialogOntologia('delete_ontology_form',ontologyId);	
	}
	
	// CREATE EDITOR FOR JSON SCHEMA 
	var createEditor = function(){		
		logControl ? console.log('|--->   createEditor()') : '';
		var container = document.getElementById('jsoneditor');
		var options = {
			mode: 'text',
			theme: 'bootstrap3',
			required_by_default: true,
			modes: ['tree', 'view'], // allowed modes
			error: function (err) {
				$.alert({title: 'ERROR!', theme: 'light', style: 'red', content: err.toString()});
				return false;
			},
			onChange: function(){
				
				console.log('se modifica el editor en modo:' + editor.mode + ' contenido: ' + editor.getText());
			}
		};		
		editor = new jsoneditor.JSONEditor(container, options, {});	

	}

	function showHideImageTableOntology(){
		if(typeof $('#ontology_autthorizations > tbody > tr').length =='undefined' || $('#ontology_autthorizations > tbody > tr').length == 0 || $('#ontology_autthorizations > tbody > tr > td > input')[0].value==''){
			$('#imageNoElementsOnTable').show();
		} else {
			$('#imageNoElementsOnTable').hide();
		}
	}
	
	// AJAX AUTHORIZATION FUNCTIONS
	var authorization = function(action,ontology,user,accesstype,authorization,btn){
		logControl ? console.log('|---> authorization()') : '';	
		var insertURL = '/controlpanel/ontologies/authorization';
		var updateURL = '/controlpanel/ontologies/authorization/update';
		var deleteURL = '/controlpanel/ontologies/authorization/delete';
		var response = {};
		
		if (action === 'insert'){
			console.log('    |---> Inserting... ' + insertURL);
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");			
			$.ajax({
				url:insertURL,
				headers: {
					[csrf_header]: csrf_value
			    },
				type:"POST",
				async: true,
				data: {"accesstype": accesstype, "ontology": ontology,"user": user},			 
				dataType:"json",
				success: function(response,status){							
					
					var propAuth = {"users":user,"accesstypes":accesstype,"id": response.id};
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
						$('#ontology_autthorizations > tbody').html("");
						$('#ontology_autthorizations > tbody').append(mountableModel2);
					}
					console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
					$('#ontology_autthorizations').mounTable(authorizationsArr,{
						model: '.authorization-model',
						noDebug: false							
					});
					
					// hide info , disable user and show table
					$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));			
					$("#users").selectpicker('deselectAll');
					$("#users option[value=" + $('#users').val() + "]").prop('disabled', true);
					$("#users").selectpicker('refresh');
					$('#authorizations').removeClass('hide');
					$('#authorizations').attr('data-loaded',true);
					
					toastr.success(messagesForms.operations.genOpSuccess,'');
					showHideImageTableOntology();
				}
			});

	
		}
		if (action === 'update'){
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			$.ajax({url:updateURL, type:"POST", async: true, 
				headers: {
					[csrf_header]: csrf_value
			    },
				data: {"id": authorization, "accesstype": accesstype},			 
				dataType:"json",
				success: function(response,status){
							
					var updateIndex = foundIndex(user,'users',authorizationsArr);			
					authorizationsArr[updateIndex]["accesstypes"] = accesstype;
					console.log('ACTUALIZADO: ' + authorizationsArr[updateIndex]["accesstypes"]);
					
					// UPDATING STATUS...
					$(btn).find("i").removeClass('fa fa-spin fa-refresh').addClass('fa fa-edit');
					$(btn).find("span").text('Update');
					
					toastr.success(messagesForms.operations.genOpSuccess,'');
				}
			});
			
			
		}
		if (action  === 'delete'){
			console.log('    |---> Deleting... ' + user + ' with authId:' + authorization );
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
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
						$("#users option[value=" + user + "]").prop('disabled', false);						
						$("#users").selectpicker('deselectAll');
						$("#users").selectpicker('refresh');
						
						toastr.success(messagesForms.operations.genOpSuccess,'');
						showHideImageTableOntology();
					}
					else{ 
						toastr.error(messagesForms.operations.genOpError,'NO RESPONSE!');
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
	
	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/ontologies/freeResource/" + id).done(
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
	
	$("#identification").on("change", function() {
		var sch = JSON.parse($("#jsonSchema").val());
		sch.title = $('#identification').val();		
		$("#jsonSchema").val(JSON.stringify(sch));
		editor.setText($("#jsonSchema").val());
	})
	
	$("#description").on("change", function() {
		var sch = JSON.parse($("#jsonSchema").val());
		sch.description = $('#description').val();		
		$("#jsonSchema").val(JSON.stringify(sch));
		editor.setText($("#jsonSchema").val());
	})
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{

		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return ontologyCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';				
			handleValidation(); 
			createEditor();
			initTemplateElements();
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
		
		// DELETE ONTOLOGY 
		deleteOntology: function(ontologyId){
			logControl ? console.log(LIB_TITLE + ': deleteOntology()') : '';	
			deleteOntologyConfirmation(ontologyId);			
		},		
		
		// INSERT AUTHORIZATION
		insertAuthorization: function(){
			logControl ? console.log(LIB_TITLE + ': insertAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){	
				// UPDATE MODE ONLY AND VALUES on user and accesstype
				if (($('#users').val() !== '') && ($("#users option:selected").attr('disabled') !== 'disabled') && ($('#accesstypes').val() !== '')){
					// AJAX INSERT (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns object with data.
					authorization('insert',ontologyCreateReg.ontologyId,$('#users').val(),$('#accesstypes').val(),'');
								
				} else {  
					toastr.error(messagesForms.operations.genOpError,ontologyCreateReg.validations.authuser);
				}
			}
		},
		
		// REMOVE authorization
		removeAuthorization: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){
				
				// AJAX REMOVE (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns object with data.
				var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
				var selAccessType = $(obj).closest('tr').find("select[name='accesstypes\\[\\]']").val();				
				
				var removeIndex = foundIndex(selUser,'users',authorizationsArr);				
				var selAuthorizationId = authorizationsIds[removeIndex][selUser];
				
				console.log('removeAuthorization:' + selAuthorizationId);
				
				authorization('delete',ontologyCreateReg.ontologyId, selUser, selAccessType, selAuthorizationId, obj );				
			}
		},

		// UPDATE authorization
		updateAuthorization: function(obj){
			logControl ? console.log(LIB_TITLE + ': updateAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){
				
				// AJAX UPDATE (ACTION,ONTOLOGYID,USER,ACCESSTYPE,ID) returns object with data.
				var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
				var selAccessType = $(obj).closest('tr').find("select[name='accesstypes\\[\\]']").val();
								
				var updateIndex = foundIndex(selUser,'users',authorizationsArr);				
				var selAuthorizationId = authorizationsIds[updateIndex][selUser];				
				
				console.log('updateAuthorization:' + selAuthorizationId);
				
				if (selAccessType !== authorizationsArr[updateIndex]["accesstypes"]){
					
					// UPDATING STATUS...
					$(obj).find("i").removeClass('fa fa-edit').addClass('fa fa-spin fa-refresh');
					$(obj).find("span").text('Updating...');
					
					authorization('update',ontologyCreateReg.ontologyId, selUser, selAccessType, selAuthorizationId, obj);
				} 
				else { console.log('no hay cambios');}
			}
		},
		
	    changeCollection: function(n, constr) {
			
			var collection = $("#tables").val();
			
			if (/\s/.test(collection)) {
				toastr.error("Table not valid!","Whitespaces not allowed in ontology name");
	            $("#tables").val(undefined);
	            return;
	        }
			
			$("#identification").val(PRESTO_PREFIX + collection);				       
			$("#identification").text(PRESTO_PREFIX + collection);
			$("#datasourceTableName").val(collection);

			if(n==1)  {
				$("#id").prop('checked',false);
				$("#fields").attr('disabled','disabled');
				$("#fields").empty();
				$("#fields").selectpicker('deselectAll').selectpicker('refresh');
			}
			
			datasource = ontologyCreateJson.datasource;
			collection = ontologyCreateJson.collection ? ontologyCreateJson.collection.toLowerCase() : collection.toLowerCase();

		}
	}
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	OntologyCreateController.load(ontologyCreateJson);	
		
	// AUTO INIT CONTROLLER.
	OntologyCreateController.init();
	
	if (ontologyCreateJson.ontologyId != null){
		$('#identification').val(ontologyCreateJson.ontologyIdentification);
	}
});