var authorizationsArr 		= []; // add authorizations
var authorizationsIds 		= []; // get authorizations ids for actions

var associationsArr 		= []; // add associations
var associationsIds 		= []; // get associations ids for actions

var AppCreateController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var APPNAME = 'onesait Platform Control Panel';
	var LIB_TITLE = 'Menu Controller';
	var logControl = 1;
	var LANGUAGE = [ 'es' ];
	var currentLanguage = ''; // loaded from template.
	var currentFormat = '' // date format depends on currentLanguage.
	var internalFormat = 'yyyy/mm/dd';
	var internalLanguage = 'en';
	var mountableModel = $('#app_autthorizations').find('tr.authorization-model')[0].outerHTML;
	var mountableModel2 = $('#app_associations').find('tr.association-model')[0].outerHTML;
	var mountableModelLdapUsers = $('#ldap_authorizations').find('tr.ldap-authorization-model')[0].outerHTML;
	var mountableModelLdapGroups = $('#ldap_groups_authorizations').find('tr.ldap-group-authorization-model')[0].outerHTML;
	var csrf_value = $("meta[name='_csrf']").attr("content");
	var csrf_header = $("meta[name='_csrf_header']").attr("content");
	$.ajaxSetup({'headers': {
		[csrf_header]: csrf_value
    }});
	
	var endpointUsers = '/controlpanel/apps/users';
	var endpointGroups = '/controlpanel/apps/groups';
	var dn;
	
	// CONTROLLER PRIVATE FUNCTIONS

	// REDIRECT URL
	var navigateUrl = function(url) {
		window.location.href = url;
	}

	//GET USERS
	var getGroups = function(){
		dn = $('#baseDnGroups').val();
		var ldapGroups = [];
		jQuery.get(endpointGroups + '?dn='+dn, function(groups){
			if(groups.length > 0){
				groups.forEach(function(group){
					ldapGroups.push({'groups':group});
				});
				
			}
			mountTableLdapGroups(ldapGroups);
			
		});
	}
	
	var getUsers = function(){
		dn = $('#baseDnUsers').val();
		var ldapUsers = [];
		jQuery.get(endpointUsers + '?dn='+dn, function(users){
			if(users.length > 0){
				users.forEach(function(user){
					ldapUsers.push({'users':user});
				});
				
			}
			mountTableLdapUsers(ldapUsers);
			
		});
	}
	//MOUNT TABLE MODAL LDAP USERS AUTHORIZATIONS
	var mountTableLdapGroups = function(groups){
		// TO-HTML
		if ($('#ldap-groups-authorizations').attr('data-loaded') === 'true'){
			$('#ldap_groups_authorizations > tbody').html("");
			$('#ldap_groups_authorizations > tbody').append(mountableModelLdapGroups);
		}
		logControl ? console.log('mounting table with ldap users found in DN: ' + $('#baseDnGroups').val() +' Arr: ' + JSON.stringify(groups)) : '';
		$('#ldap_groups_authorizations').mounTable(groups,{
			model: '.ldap-group-authorization-model',
			noDebug: false							
		});
		
		// hide info , disable user and show table
		$('#ldap-groups-authorizations').attr('data-loaded',true);// TO-HTML
		$('#ldap-groups-modal').modal('show');
		$('.select-modal-group').selectpicker();
		
	}
	
	
	//MOUNT TABLE MODAL LDAP USERS AUTHORIZATIONS
	var mountTableLdapUsers = function(users){
		// TO-HTML
		if ($('#ldap-authorizations').attr('data-loaded') === 'true'){
			$('#ldap_authorizations > tbody').html("");
			$('#ldap_authorizations > tbody').append(mountableModelLdapUsers);
		}
		logControl ? console.log('mounting table with ldap users found in DN: ' + $('#baseDnUsers').val() +' Arr: ' + JSON.stringify(users)) : '';
		$('#ldap_authorizations').mounTable(users,{
			model: '.ldap-authorization-model',
			noDebug: false							
		});
		
		// hide info , disable user and show table
		$('#ldap-authorizations').attr('data-loaded',true);// TO-HTML
		$('#ldap-users-modal').modal('show');
		$('.select-modal').selectpicker();
		
	}
	// CLEAN FIELDS FORM
	var cleanFields = function(formId) {
		logControl ? console.log('cleanFields() -> ') : '';

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

		// CLEANING NUMBER INPUTS
		$(':input[type="number"]').val('');
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();

		// CLEAN ROLES
		$("#datamodel_properties tbody tr").each(
				function(tr) {
					$("#roleName").append(this.dataset.rolename);
					$("#roleDescription").append(this.dataset.roledescription);
					this.remove();

				});

		$("#parameter_roles").val('');
		$('#parameter_users').val('');
		$('#parameter_associations').val('');

	}

	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#app_create_form');
		var error1 = $('.alert-danger');
		var success1 = $('.alert-success');

		// set current language
		currentLanguage = appCreateReg.language || LANGUAGE;

		form1.validate({
					errorElement : 'span', // default input error message
											// container
					errorClass : 'help-block help-block-error', // default input
																// error message
																// class
					focusInvalid : false, // do not focus the last invalid
											// input
					ignore : ":hidden:not(.selectpicker)", // validate all
															// fields including
															// form hidden input
															// but not
															// selectpicker
					lang : currentLanguage,
					// custom messages
					messages : {

					},
					// validation rules
					rules : {
						identification : {
							minlength : 5,
							required : true
						},
						name : {
							minlength : 5,
							required : true
						},
						description : {
							minlength : 5,
							required : true
						}
					},
					invalidHandler : function(event, validator) { // display
																	// error
																	// alert on
																	// form
																	// submit

						toastr.error(messagesForms.validation.genFormError,'');
					},
					errorPlacement : function(error, element) {
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
					highlight : function(element) { // hightlight error inputs
						$(element).closest('.form-group').addClass('has-error');
					},
					unhighlight : function(element) { // revert the change
														// done by hightlight
						$(element).closest('.form-group').removeClass(
								'has-error');
					},
					success : function(label) {
						label.closest('.form-group').removeClass('has-error');
					},
					// ALL OK, THEN SUBMIT.
					submitHandler : function(form) {

						if (valRoles()) {
							toastr.success(messagesForms.validation.genFormSuccess,'');
							form.submit();
							// insert();
						} else {
							toastr.error(messagesForms.validation.genFormError,'');
							return false;
						}
					}
				});
	}

	var valRoles = function() {
		return (validateRoles().length > 0);
	}
	
	var resetAuthorizations = function(newArray){
		// if app has authorizations we load it!.
		authorizationsJson = newArray;			
		if (authorizationsJson.length > 0 ){
			
			// MOUNTING AUTHORIZATIONS ARRAY
			var authid_update, role_update , userid_update , authorizationUpdate , authorizationIdUpdate = '';
			authorizationsArr = []
			$.each( authorizationsJson, function (key, object){			
				
				authid_update 		= object.id; 
				role_update 		= object.roleName; 
				userid_update 		= object.user;					
				
				logControl ? console.log('      |----- authorizations object on Update, ID: ' +  authid_update + ' ROLE: ' +  role_update + ' USER: ' +  userid_update  ) : '';
				
				// AUTHs-table {"users":user,"roles":role,"id": response.id}
				authorizationUpdate = {"users": userid_update, "rolesName": role_update, "id": authid_update};					
				authorizationsArr.push(authorizationUpdate);
				
				// AUTH-Ids {[user_id]:auth_id}
				authorizationIdUpdate = {[userid_update]:authid_update};
				authorizationsIds.push(authorizationIdUpdate);
				
			});

			// TO-HTML
			if ($('#authorizations').attr('data-loaded') === 'true'){
				$('#app_autthorizations > tbody').html("");
				$('#app_autthorizations > tbody').append(mountableModel);
			}
			logControl ? console.log('authorizationsArr on UPDATE: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr)) : '';
			$('#app_autthorizations').mounTable(authorizationsArr,{
				model: '.authorization-model',
				noDebug: false							
			});
			
			// hide info , disable user and show table
			$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));					
			$('#authorizations').removeClass('hide');
			$('#authorizations').attr('data-loaded',true);// TO-HTML
			$("#users").selectpicker('deselectAll');
			$("#roles").selectpicker('deselectAll');

		}
	}

	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function() {
		logControl ? console
				.log('initTemplateElements() -> selectpickers, datepickers, resetForm, today->dateCreated currentLanguage: '
						+ currentLanguage)
				: '';

		// set current language and formats
		currentLanguage = appCreateReg.language || LANGUAGE[0];
		currentFormat = (currentLanguage == 'es') ? 'dd/mm/yyyy' : 'mm/dd/yyyy';

		logControl ? console.log('|---> datepickers currentLanguage: '
				+ currentLanguage) : '';

		// INPUT MASK FOR Realm identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});	
		
		// authorization tab control 
		$(".option a[href='#tab_1']").on("click", function(e) {
			$('.tabContainer').find('.option').removeClass('active');
	        $(this).closest("div").addClass('active');
		});		
		
		// authorization tab control 
		$(".option a[href='#tab_2']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			$.alert({title: 'Info',  theme: 'light', content: appCreateReg.validations.authinsert});
			return false;
		  } else {
	        $('.tabContainer').find('.option').removeClass('active');
	        $(this).closest("div").addClass('active');
		  }
		});

		// association tab control 
		$(".option a[href='#tab_3']").on("click", function(e) {
		  if ($(this).hasClass("disabled1")) {
			e.preventDefault();
			$.alert({title: 'Info',  theme: 'light', content: appCreateReg.validations.associnsert});
			return false;
		  }
		  else if ($(this).hasClass("disabled2")){
		  	e.preventDefault();
			$.alert({title: 'Info',  theme: 'light', content: appCreateReg.validations.assocchildapps});
			return false;
		  } else {
	        $('.tabContainer').find('.option').removeClass('active');
	        $(this).closest("div").addClass('active');
		  }
		  
		});
		$(".option a[href='#tab_4']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			$.alert({title: 'Info',  theme: 'light', content: appCreateReg.validations.createProject});
			return false;
		  } else {
	        $('.tabContainer').find('.option').removeClass('active');
	        $(this).closest("div").addClass('active');
		  }
		});

		// Reset form
		$('#resetBtn').on('click', function() {
			cleanFields('app_create_form');
		});
		$('#check-ldap-list').on('change', function() {
			var ldapUsers = $('#check-ldap-list').is(':checked');

			if(ldapUsers){
				$('.configdb').addClass('hide');
				$('.ldap').removeClass('hide')
			}else{
				$('.ldap').addClass('hide');
				$('.configdb').removeClass('hide')
			}
			
		});
		$('#check-new-project').on('change', function() {
			var newProject = $('#check-new-project').is(':checked');

			if(newProject){
				$('#project-form-data').removeClass('hide');
				$('#project-name').attr('required','required')
				$('#project-description').attr('required','required')
				$('#combo-projects').addClass('hide');
			}else{
				$('#project-form-data').addClass('hide');
				$('#project-name').attr('required',false)
				$('#project-description').attr('required',false)
				$('#combo-projects').removeClass('hide');
			}
			
		});
		

		// INSERT MODE ACTIONS (deviceCreateReg.actionMode = NULL )
		if (appCreateReg.actionMode === null) {
			logControl ? console.log('action-mode: INSERT') : '';

		}

		// EDIT MODE ACTION
		else {
			updateRoles($("#parameter_roles").val());

			// if app has authorizations we load it!.
			authorizationsJson = appCreateReg.authorizations;			
			if (authorizationsJson.length > 0 ){
				
				// MOUNTING AUTHORIZATIONS ARRAY
				var authid_update, role_update , userid_update , authorizationUpdate , authorizationIdUpdate = '';
				$.each( authorizationsJson, function (key, object){			
					
					authid_update 		= object.id; 
					role_update 		= object.roleName; 
					userid_update 		= object.user;					
					
					logControl ? console.log('      |----- authorizations object on Update, ID: ' +  authid_update + ' ROLE: ' +  role_update + ' USER: ' +  userid_update  ) : '';
					
					// AUTHs-table {"users":user,"roles":role,"id": response.id}
					authorizationUpdate = {"users": userid_update, "rolesName": role_update, "id": authid_update};					
					authorizationsArr.push(authorizationUpdate);
					
					// AUTH-Ids {[user_id]:auth_id}
					authorizationIdUpdate = {[userid_update]:authid_update};
					authorizationsIds.push(authorizationIdUpdate);
					
				});

				// TO-HTML
				if ($('#authorizations').attr('data-loaded') === 'true'){
					$('#app_autthorizations > tbody').html("");
					$('#app_autthorizations > tbody').append(mountableModel);
				}
				logControl ? console.log('authorizationsArr on UPDATE: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr)) : '';
				$('#app_autthorizations').mounTable(authorizationsArr,{
					model: '.authorization-model',
					noDebug: false							
				});
				
				// hide info , disable user and show table
				$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));					
				$('#authorizations').removeClass('hide');
				$('#authorizations').attr('data-loaded',true);// TO-HTML
				$("#users").selectpicker('deselectAll');
				$("#roles").selectpicker('deselectAll');

			}

			$('#childApps').change(function(){
				var csrf_value = $("meta[name='_csrf']").attr("content");
				var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
				if ($('#childApps').val() !== ''){
					$.ajax({
						url:'/controlpanel/apps/getRoles',
						headers: {
							[csrf_header]: csrf_value
					    },
						type:"GET",
						async: true,
						data: {"appId": $('#childApps').val()},			 
						dataType:"json",
						success: function(response,status){
							$('#selectRolesChildApp').show();
							$('#rolesChildApp').find('option').remove();
							$('#rolesChildApp').append($('<option>', {value: '', text: 'Select a role...'}));
							$.each(response, function(key,value){
								$('#rolesChildApp').append($('<option>', {value: key, text: value}));
								$('#rolesChildApp').selectpicker('refresh');
	    					});
						}	
					});
				}
				else{
					$('#rolesChildApp').find('option').remove();
					$('#rolesChildApp').append($('<option>', {value: '', text: 'Select a role...'}));
					$('#rolesChildApp').selectpicker('refresh');
				}
			});

			// if app has associations we load it!.
			associationsJson = appCreateReg.associations;
			if (associationsJson.length > 0 ){
				
				// MOUNTING ASSOCIATIONS ARRAY
				var authid_update, fatherAppId_update , fatherRoleName_update ,childAppId_update, childRoleName_update, associationUpdate , associationIdUpdate = '';
				$.each( associationsJson, function (key, object){			
					
					authid_update 			= object.id; 
					fatherAppId_update 		= object.fatherAppId; 
					fatherRoleName_update 	= object.fatherRoleName;
					childAppId_update 		= object.childAppId; 
					childRoleName_update 	= object.childRoleName;					
					
					logControl ? console.log('      |----- Associations object on Update, ID: ' +  authid_update + ' FATHER APP: ' +  fatherAppId_update + ' FATHER ROLE: ' +  fatherRoleName_update  ) : '';
					
					associationUpdate = {"fatherApps": fatherAppId_update, "fatherRoles": fatherRoleName_update, "childApps": childAppId_update, "childRoles": childRoleName_update, "id": authid_update};					
					associationsArr.push(associationUpdate);
					
					// ASSOCs-Ids {[fatherAppId_update]:auth_id}
					associationIdUpdate = {[fatherAppId_update]:authid_update};
					associationsIds.push(associationIdUpdate);
					
				});

				// TO-HTML
				if ($('#associations').attr('data-loaded') === 'true'){
					$('#app_associations > tbody').html("");
					$('#app_associations > tbody').append(mountableModel2);
				}
				logControl ? console.log('associationsArr on UPDATE: ' + associationsArr.length + ' Arr: ' + JSON.stringify(associationsArr)) : '';
				$('#app_associations').mounTable(associationsArr,{
					model: '.association-model',
					noDebug: false							
				});
				
				// hide info , disable user and show table
				$('#alert-associations').toggle($('#alert-associations').hasClass('hide'));					
				$('#associations').removeClass('hide');
				$('#associations').attr('data-loaded',true);// TO-HTML
				$("#rolesFatherApp").selectpicker('deselectAll');
				$("#childApps").selectpicker('deselectAll');
				$("#rolesChildApp").selectpicker('deselectAll');
				
			}
		}
	}

	var addRoleRow = function() {

		var roleName = $("#roleName").val();
		var roleDescription = $("#roleDescription").val();
		var error = false;

		$("#datamodel_properties tbody tr").each(function(tr) {
			if (roleName === this.dataset.rolename){
				toastr.error(messagesForms.operations.genOpError,appCreateReg.validations.existingRole);
				error = true;
				return false;
			}
		});

		if (error){
			return false;
		}

		if (roleName === "" || roleName.length < 2) {
			toastr.error(messagesForms.operations.genOpError,appCreateReg.fieldEmpty);
			return false;
		}
		if(roleName.length > 24) {
			toastr.error(messagesForms.operations.genOpError,appCreateReg.longField);
			return false;
		}
		if (roleDescription === "" || roleDescription.length < 5) {
			toastr.error(messagesForms.operations.genOpError,appCreateReg.fieldEmpty);
			return false;
		}
		$('#datamodel_properties > tbody')
				.append(
						'<tr data-roleName="'
								+ roleName
								+ '" data-roleDescription="'
								+ roleDescription
								+ '" ><td>'
								+ roleName
								+ '</td><td >'
								+ roleDescription
								+ '</td><td><button type="button" data-property="" class="btn btn-xs btn-no-border icon-on-table color-red tooltips" style="background-color:transparent;" onclick="AppCreateController.removeRole(this)"><i class="icon-delete"></i></button></td></tr>');
		$("#roleName").val('');
		$("#roleDescription").val('');
		
		toastr.success(messagesForms.operations.genOpSuccess,'');
	}

	var updateRoles = function(roles) {
		var createdRoles = JSON.parse(roles);
		if (createdRoles !== null && createdRoles.length > 0) {
			for (var i = 0; i < createdRoles.length; i++) {
				var role = createdRoles[i];
				$('#datamodel_properties > tbody')
						.append(
								'<tr data-roleName="'
										+ role.name
										+ '" data-roleDescription="'
										+ role.description
										+ '"><td>'
										+ role.name
										+ '</td><td >'
										+ role.description
										+ '</td><td><button type="button" data-property="" class="btn btn-xs btn-no-border icon-on-table color-red tooltips" style="background-color:transparent;" onclick="AppCreateController.removeRole(this)"><i class="icon-delete"></i></button></td></tr>');
			}
		}

	}

	var removeRole = function(row) {

		// Miramos si el rol es padre de otro rol para eliminar la asociacion:
		var removeIndexFatherRole = associationsArr.map(function(item) { return item['fatherRoles']; }).indexOf(row.parentElement.parentElement.dataset.rolename);
		while (removeIndexFatherRole != -1){
			associationsIds.splice(removeIndexFatherRole,1);
			associationsArr.splice(removeIndexFatherRole,1);
			removeIndexFatherRole = associationsArr.map(function(item) { return item['fatherRoles']; }).indexOf(row.parentElement.parentElement.dataset.rolename);
		}

		// Miramos si el rol es hijo de otro rol para eliminar la asociacion:
		var removeIndexChildRole = associationsArr.map(function(item) { return item['childRoles']; }).indexOf(row.parentElement.parentElement.dataset.rolename);
		while (removeIndexChildRole != -1){
			associationsIds.splice(removeIndexChildRole,1);
			associationsArr.splice(removeIndexChildRole,1);
			removeIndexChildRole = associationsArr.map(function(item) { return item['childRoles']; }).indexOf(row.parentElement.parentElement.dataset.rolename);
		}

		var removeIndex = authorizationsArr.map(function(item) { return item['rolesName']; }).indexOf(row.parentElement.parentElement.dataset.rolename);			
		while (removeIndex != -1) {
			authorizationsIds.splice(removeIndex, 1);
			authorizationsArr.splice(removeIndex, 1);
		  	removeIndex = authorizationsArr.map(function(item) { return item['rolesName']; }).indexOf(row.parentElement.parentElement.dataset.rolename);
		}								

		row.parentElement.parentElement.remove();
		toastr.success(messagesForms.operations.genOpSuccess,'');
	}

	var validateRoles = function() {
		var listRoles = [];
		var listUsers = [];
		var listAssociations = [];
		$("#datamodel_properties tbody tr").each(function(tr) {
			listRoles.push({
				name : this.dataset.rolename,
				description : this.dataset.roledescription
			});
		});

		$.each(authorizationsArr, function(tr){
			listUsers.push({
					user: this.users,
					roleName: this.rolesName
				});
		});

		$.each(associationsArr, function(tr){
			listAssociations.push({
					fatherAppId: this.fatherApps,
					fatherRoleName: this.fatherRoles,
					childAppId: this.childApps,
					childRoleName: this.childRoles
				});
		});

		$('#parameter_roles').val(JSON.stringify(listRoles));
		$('#parameter_users').val(JSON.stringify(listUsers));
		$('#parameter_associations').val(JSON.stringify(listAssociations));

		return listRoles;
	}


	var deleteAppConfirmation = function() {

		var idApp = $("#id").val();

		// no Id no fun!
		if (!idApp) {
			toastr.error(messagesForms.validation.genFormError,'NO APP-FORM SELECTED!');
			return false;
		}

		// call Confirm
		showConfirmDeleteDialog(idApp);
	}

	var showConfirmDeleteDialog = function(idApp) {

		// i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = appCreateReg.deleteAppConfirm;
		var Title = headerReg.appDelete;

		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title : Title,
			theme : 'light',
			columnClass : 'medium',
			content : Content,
			draggable : true,
			dragWindowGap : 100,
			backgroundDismiss : true,
			buttons : {
				close : {
					text : Close,
					btnClass : 'btn btn-outline blue dialog',
					action : function() {
					} // GENERIC CLOSE.
				},
				remove : {
					text : Remove,
					btnClass : 'btn btn-primary',
					action : function() {
						console.log(idApp);
						$.ajax({
							url : '/controlpanel/apps/' + idApp,
							headers: {
								[csrf_header]: csrf_value
						    },
							type : 'DELETE',
							success : function(result) {
								console.log(result);
								navigateUrl(result);
							}
						});
					}
				}
				
			}
		});
	}

	// AJAX AUTHORIZATION FUNCTIONS
	var authorization = function(action,appId,userId,roleId,authorization,btn){
		logControl ? console.log('|---> authorization()') : '';	
		var insertURL = '/controlpanel/apps/authorization';
		var insertURLLDAP = '/controlpanel/apps/authorization/ldap'
		var deleteURL = '/controlpanel/apps/authorization/delete';
		var response = {};
		
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		if (action === 'insert'){
			console.log('    |---> Inserting... ' + insertURL);
						
			$.ajax({
				url:insertURL,
				headers: {
					[csrf_header]: csrf_value
			    },
				type:"POST",
				async: true,
				data: {"roleId": roleId, "appId": appId,"userId": userId},			 
				dataType:"json",
				success: function(response,status){							
					
					var propAuth = {"users":response.user,"rolesName":response.roleName,"id": response.id};
					authorizationsArr.push(propAuth);
					console.log('     |---> JSONtoTable: ' + authorizationsArr.length + ' data: ' + JSON.stringify(authorizationsArr));
					// store ids for after actions.	inside callback 				
					var user_id = response.user;
					var auth_id = response.id;
					var AuthId = {[user_id]:auth_id};
					authorizationsIds.push(AuthId);
					console.log('     |---> Auths: ' + authorizationsIds.length + ' data: ' + JSON.stringify(authorizationsIds));
										
					// TO-HTML
					if ($('#authorizations').attr('data-loaded') === 'true'){
						$('#app_autthorizations > tbody').html("");
						$('#app_autthorizations > tbody').append(mountableModel);
					}
					console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
					$('#app_autthorizations').mounTable(authorizationsArr,{
						model: '.authorization-model',
						noDebug: false							
					});
					
					// hide info , disable user and show table
					$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));			
					$("#users").selectpicker('deselectAll');
					$("#users").selectpicker('refresh');
					$("#roles").selectpicker('deselectAll');
					$("#roles").selectpicker('refresh');
					$('#authorizations').removeClass('hide');
					$('#authorizations').attr('data-loaded',true);
					
					toastr.success(messagesForms.operations.genOpSuccess,'');
				}
			});

	
		}
		if (action === 'insertLDAP'){
			console.log('    |---> Inserting ldap... ' + insertURLLDAP);
						
			$.ajax({
				url:insertURLLDAP,
				headers: {
					[csrf_header]: csrf_value
			    },
				type:"POST",
				async: true,
				data: {"roleId": roleId, "appId": appId,"userId": userId, "dn" : dn},			 
				dataType:"json",
				success: function(response,status){							
					
					var propAuth = {"users":response.user,"rolesName":response.roleName,"id": response.id};
					authorizationsArr.push(propAuth);
					console.log('     |---> JSONtoTable: ' + authorizationsArr.length + ' data: ' + JSON.stringify(authorizationsArr));
					// store ids for after actions.	inside callback 				
					var user_id = response.user;
					var auth_id = response.id;
					var AuthId = {[user_id]:auth_id};
					authorizationsIds.push(AuthId);
					console.log('     |---> Auths: ' + authorizationsIds.length + ' data: ' + JSON.stringify(authorizationsIds));
										
					// TO-HTML
					if ($('#authorizations').attr('data-loaded') === 'true'){
						$('#app_autthorizations > tbody').html("");
						$('#app_autthorizations > tbody').append(mountableModel);
					}
					console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
					$('#app_autthorizations').mounTable(authorizationsArr,{
						model: '.authorization-model',
						noDebug: false							
					});
					
					// hide info , disable user and show table
					$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));			
					$("#users").selectpicker('deselectAll');
					$("#users").selectpicker('refresh');
					$("#roles").selectpicker('deselectAll');
					$("#roles").selectpicker('refresh');
					$('#authorizations').removeClass('hide');
					$('#authorizations').attr('data-loaded',true);
					
					toastr.success(messagesForms.operations.genOpSuccess,'');
				}
			});

	
		}
		
		if (action  === 'delete'){
			console.log('    |---> Deleting... ' + userId + ' with authId:' + authorization );
			
			$.ajax({url:deleteURL, type:"POST", async: true, 
				headers: {
					[csrf_header]: csrf_value
			    },
				data: {"id": authorization},			 
				dataType:"json",
				success: function(response,status){									
					
					// remove object
					var removeIndex = authorizationsIds.map(function(item) { return item[userId]; }).indexOf(authorization);			
					authorizationsIds.splice(removeIndex, 1);
					authorizationsArr.splice(removeIndex, 1);
					
					console.log('AuthorizationsIDs: ' + JSON.stringify(authorizationsIds));
					// refresh interface. TO-DO: EL this este fallará					
					if ( response  ){ 
						$(btn).closest('tr').remove();
						$("#users").selectpicker('deselectAll');
						$("#users").selectpicker('refresh');
						$("#roles").selectpicker('deselectAll');
						$("#roles").selectpicker('refresh');
						if (authorizationsArr.length == 0){
							$('#alert-authorizations').toggle(!$('#alert-authorizations').is(':visible'));					
							$('#authorizations').addClass('hide');
						}
						toastr.success(messagesForms.operations.genOpSuccess,'');
					} else { 
						toastr.error(messagesForms.operations.genOpError,'NO RESPONSE!');
					}
				}
			});			
		}	
	};

	// AJAX ASSOCIATION FUNCTIONS
	var association = function(action,fatherAppId,fatherRoleId,childAppId,childRoleId,association,btn){
		logControl ? console.log('|---> association()') : '';	
		var insertURL = '/controlpanel/apps/association';
		var deleteURL = '/controlpanel/apps/association/delete';
		var response = {};
		
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		if (action === 'insert'){
			console.log('    |---> Inserting... ' + insertURL);
						
			$.ajax({
				url:insertURL,
				headers: {
					[csrf_header]: csrf_value
			    },
				type:"POST",
				async: true,
				data: {"fatherRoleId": fatherRoleId, "childRoleId": childRoleId},			 
				dataType:"json",
				success: function(response,status){							
					
					var propAuth = {"fatherApps":response.fatherAppId,"fatherRoles":response.fatherRoleName, "childApps": response.childAppId,"childRoles": response.childRoleName,"id": response.id};
					associationsArr.push(propAuth);
					console.log('     |---> JSONtoTable: ' + associationsArr.length + ' data: ' + JSON.stringify(associationsArr));
					// store ids for after actions.	inside callback 				
					var fatherApp_id = response.fatherAppId;
					var auth_id = response.id;
					var AuthId = {[fatherApp_id]:auth_id};
					associationsIds.push(AuthId);
					console.log('     |---> Auths: ' + associationsIds.length + ' data: ' + JSON.stringify(associationsIds));
										
					// TO-HTML
					if ($('#associations').attr('data-loaded') === 'true'){
						$('#app_associations > tbody').html("");
						$('#app_associations > tbody').append(mountableModel2);
					}
					console.log('associationsArr: ' + associationsArr.length + ' Arr: ' + JSON.stringify(associationsArr));
					$('#app_associations').mounTable(associationsArr,{
						model: '.association-model',
						noDebug: false							
					});
					
					// hide info and show table
					$('#alert-associations').toggle($('#alert-associations').hasClass('hide'));			
					$("#rolesFatherApp").selectpicker('deselectAll');
					$("#rolesFatherApp").selectpicker('refresh');
					$("#childApps").selectpicker('deselectAll');
					$("#childApps").selectpicker('refresh');
					$("#rolesChildApp").selectpicker('deselectAll');
					$("#rolesChildApp").selectpicker('refresh');
					$('#associations').removeClass('hide');
					$('#associations').attr('data-loaded',true);
					
					toastr.success(messagesForms.operations.genOpSuccess,'');
				}
			});
		}
		
		if (action  === 'delete'){
			console.log('    |---> Deleting... ' + childAppId + ' with authId:' + association );
			
			$.ajax({url:deleteURL, type:"POST", async: true, 
				headers: {
					[csrf_header]: csrf_value
			    },
				data: {"fatherRoleName": fatherRoleId, "childRoleName" : childRoleId, "fatherAppId" : fatherAppId, "childAppId" : childAppId},			 
				dataType:"json",
				success: function(response,status){									
					
					// remove object
					var removeIndex = associationsIds.map(function(item) { return item[fatherAppId]; }).indexOf(association);			
					associationsIds.splice(removeIndex, 1);
					associationsArr.splice(removeIndex, 1);
					
					console.log('AssociationsIDs: ' + JSON.stringify(associationsIds));
					// refresh interface. TO-DO: EL this este fallará					
					if ( response  ){ 
						$(btn).closest('tr').remove();
						$("#rolesFatherApp").selectpicker('deselectAll');
						$("#rolesFatherApp").selectpicker('refresh');
						$("#childApps").selectpicker('deselectAll');
						$("#childApps").selectpicker('refresh');
						$("#rolesChildApp").selectpicker('deselectAll');
						$("#rolesChildApp").selectpicker('refresh');
						if (associationsArr.length == 0){
							$('#alert-associations').toggle(!$('#alert-associations').is(':visible'));					
							$('#associations').addClass('hide');
						}
						toastr.success(messagesForms.operations.genOpSuccess,'');
					}
					else{ 
						toastr.error(messagesForms.operations.genOpError,'NO RESPONSE!');
					}
				}
			});			
		}
	};

	// return position to find authId.
	var foundIndexAuth = function(what,item,what2,item2,arr){
		var found = '';
		arr.forEach(function(element, index, array) {
			if ( what === element[item] && what2 === element[item2]){ found = index;  console.log("a[" + index + "] = " + element[item] + ' Founded in position: ' + found ); } 
			
		});		
		return found;
	}

	// return position to find assocId.
	var foundIndexAssoc = function(fatherApp,field1,id,field2,arr){
		var found = '';
		arr.forEach(function(element, index, array) {
			if ( fatherApp === element[field1] && id === element[field2]){ 
				found = index;  
				console.log("a[" + index + "] = " + element[field1] +':' +element[field2]+ ' Founded in position: ' + found ); 
			} 
			
		});		
		return found;
	}

	// CONTROLLER PUBLIC FUNCTIONS
	return {
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load : function(Data) {
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return appCreateReg = Data;
		},

		// INIT() CONTROLLER INIT CALLS
		init : function() {
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleValidation();
			initTemplateElements();

		},
		// REDIRECT
		go : function(url) {
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			navigateUrl(url);
		},
		// DELETE DEVICE
		deleteApp : function(appId) {
			logControl ? console.log(LIB_TITLE + ': deleteApp()') : '';
			deleteAppConfirmation(appId);
		},
		// JSON SCHEMA VALIDATION
		validateRoles : function() {
			validateRoles();
		},
		addRoleRow : function() {
			addRoleRow();
		},
		removeRole : function(row) {
			removeRole(row);
		},
		deleteAppConfirmation : function(data) {
			deleteAppConfirmation(data);
		},
		
		//INSERT AUTHORIZATION GROUP LDAP
		insertAuthorizationLdapGroups: function(obj){
			var selGroup = $(obj).closest('tr').find("input[name='groups\\[\\]']").val();
			var selRoleText = $(obj).closest('tr').find(':selected').text();
			var selRoleVal = $(obj).closest('tr').find(':selected').val();
			
			logControl ? console.log(LIB_TITLE + ': insertAuthorizationLdapGroups(obj)') : '';
			var found = false;
			if ( appCreateReg.actionMode !== null){	
				// UPDATE MODE ONLY AND VALUES on user and role
				if ((selGroup!== '') && (selRoleVal !== '')){
					jQuery.get(endpointGroups + '/' +selGroup + '?dn=' +$('#baseDnGroups').val(), function(users){
						if(users.length > 0){
							users.forEach(function(user){
								$.each(authorizationsArr, function(i, auth){
									if(auth.users == user && auth.rolesName == selRoleText)
										found=true;
								});
		
								if (found){
									console.log('Not creating auth for user '+user +' and Role ' +selRoleText+' because already exists ');
								}
								else{
									// AJAX INSERT (ACTION,APPID,USER,ROLE) returns object with data.
									authorization('insertLDAP',appCreateReg.appId,user,selRoleVal,'');
								}
							});
						}
					});
				}else {
					toastr.error(messagesForms.operations.genOpError,appCreateReg.validations.authuser);
				}
		}
		},
		//INSERT AUTHORIZATION LDAP
		insertAuthorizationLdapUsers: function(obj){
			var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
			var selRoleText = $(obj).closest('tr').find(':selected').text();
			var selRoleVal = $(obj).closest('tr').find(':selected').val();
			
			logControl ? console.log(LIB_TITLE + ': insertAuthorizationLdap(obj)') : '';
			var found = false;
			if ( appCreateReg.actionMode !== null){	
				// UPDATE MODE ONLY AND VALUES on user and role
				if ((selUser!== '') && (selRoleVal !== '')){
					$.each(authorizationsArr, function(i, auth){
						if(auth.users == selUser && auth.rolesName == selRoleText)
							found=true;
					});

					if (found){
						toastr.error(messagesForms.operations.genOpError,appCreateReg.validations.authUserRole);
					}
					else{
						// AJAX INSERT (ACTION,APPID,USER,ROLE) returns object with data.
						authorization('insertLDAP',appCreateReg.appId,selUser,selRoleVal,'');
					}
				} else {
					toastr.error(messagesForms.operations.genOpError,appCreateReg.validations.authuser);
				}
			}
		},
		
		// INSERT AUTHORIZATION
		insertAuthorization: function(){
			logControl ? console.log(LIB_TITLE + ': insertAuthorization()') : '';
			var found = false;
			if ( appCreateReg.actionMode !== null){	
				// UPDATE MODE ONLY AND VALUES on user and role
				if (($('#users').val() !== '') && ($('#roles').val() !== '')){
					
					$.each(authorizationsArr, function(i, auth){
						if(auth.users == $('#users').val() && auth.rolesName == $('#roles option:selected').text())
							found=true;
					});

					if (found){
						$.alert({title: 'ERROR!', theme: 'light', content: appCreateReg.validations.authUserRole});
					}
					else{
						// AJAX INSERT (ACTION,APPID,USER,ROLE) returns object with data.
						authorization('insert',appCreateReg.appId,$('#users').val(),$('#roles').val(),'');
					}
				} else {
					toastr.error(messagesForms.operations.genOpError,appCreateReg.validations.authuser);
				}
			}
		},

		// REMOVE authorization
		removeAuthorization: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeAuthorization()') : '';
			if ( appCreateReg.actionMode !== null){
				
				// AJAX REMOVE (ACTION,APPID,USER,ROLE) returns object with data.
				var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
				var selRole = $(obj).closest('tr').find("input[name='rolesName\\[\\]']").val();				
				
				var removeIndex = foundIndexAuth(selUser,'users',selRole,'rolesName',authorizationsArr);				
				var selAuthorizationId = authorizationsIds[removeIndex][selUser];
				
				console.log('removeAuthorization:' + selAuthorizationId);
				
				authorization('delete',appCreateReg.appId, selUser, selRole, selAuthorizationId, obj );				
			}
		},
		// INSERT ASSOCIATION
		insertAssociation: function(){
			logControl ? console.log(LIB_TITLE + ': insertAssociation()') : '';
			var found = false;
			if ( appCreateReg.actionMode !== null){	
				// UPDATE MODE ONLY AND VALUES on apps and roles
				if (($('#rolesFatherApp').val() !== '') && ($('#childApps').val() !== '') && ($('#rolesChildApp').val() !== '')){

					var childAppsTable = $('#associations').find("input[name='childApps\\[\\]']");
					var fatherRolesTable = $('#associations').find("input[name='fatherRoles\\[\\]']");
					var childRolesTable = $('#associations').find("input[name='childRoles\\[\\]']");

					$.each(fatherRolesTable,function(indexFatherRole,fatherRole){
						if (fatherRole.value === $('#rolesFatherApp option:selected').text()){
							$.each(childAppsTable,function(indexChildApp,childApp){
									if (childApp.value === $('#childApps').val() && indexChildApp === indexFatherRole){
										$.each(childRolesTable,function(indexChildRole,childRole){
											if (childRole.value === $('#rolesChildApp option:selected').text() && indexChildRole === indexChildApp){
												found = true;
											}
										});
									}
							});
						}
					});

					if (found){
						toastr.error(messagesForms.operations.genOpError,appCreateReg.validations.assocAppRole);
					}
					else{
						// AJAX INSERT (ACTION,APPID,FATHER ROLE, CHILD APP, CHILD ROLE) returns object with data.
						association('insert',appCreateReg.appId,$('#rolesFatherApp').val(),$('#childApps').val(),$('#rolesChildApp').val(),'');
					}
				} else {  
					toastr.error(messagesForms.operations.genOpError,appCreateReg.validations.assocEmpty);
				}
			}
		},
		// REMOVE authorization
		removeAssociation: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeAssociation()') : '';
			if ( appCreateReg.actionMode !== null){
				
				// AJAX REMOVE (ACTION,APPID,FATHER APP, FATHER ROLE, CHILD APP, CHILD ROLE) returns object with data.
				var selFatherApp = $(obj).closest('tr').find("input[name='fatherApps\\[\\]']").val();
				var selChildApp = $(obj).closest('tr').find("input[name='childApps\\[\\]']").val();
				var selFatherRole = $(obj).closest('tr').find("input[name='fatherRoles\\[\\]']").val();
				var selChildRole = $(obj).closest('tr').find("input[name='childRoles\\[\\]']").val();
				var id = selFatherRole+ ':' +selChildRole;					
				
				var removeIndex = foundIndexAssoc(selFatherApp,'fatherApps',id,'id',associationsArr);				
				var selAssociationId = associationsIds[removeIndex][selFatherApp];
				
				console.log('removeAssociation:' + selAssociationId);
				
				association('delete',selFatherApp, selFatherRole, selChildApp, selChildRole, selAssociationId, obj );				
			}
		},
		
		getUsers : function(){
			getUsers();
		},
		
		getGroups : function(){
			getGroups();
		},
		
		resetAuthorizations : function(newArray){
			resetAuthorizations(newArray);
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	AppCreateController.load(appCreateJson);

	// AUTO INIT CONTROLLER.
	AppCreateController.init();
});
