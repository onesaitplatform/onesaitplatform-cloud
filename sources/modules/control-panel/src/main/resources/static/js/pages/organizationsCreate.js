var organizationCreateController  = function(){
	
	var LIB_TITLE = 'Organization Controller';
	var reader = new FileReader();
	var authorizationsArr 		= []; // add authorizations
	var authorizationsIds 		= []; // get authorizations ids for actions
	var mountableModel2 = $('#organization_authorizations').find('tr.authorization-model')[0].outerHTML;
	logControl = 1;
	
	reader.onload = function (e) {
        $('#showedImg').attr('src', e.target.result);
        if ($('#showedImg').hasClass('hide')){
        	$('#showedImg').removeClass('hide')
        }
    }	
	
	$('#resetBtn').on('click',function(){ 
		cleanFields('organization_create_form');
	});
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});

		$('#showedImg').addClass('hide')
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}

	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#organization_create_form');

		// set current language
		currentLanguage = organizationCreateJson.language || LANGUAGE;

		form1.validate({
					errorElement : 'span',
					errorClass : 'help-block help-block-error',
					focusInvalid : false,
					ignore : ":hidden:not(.selectpicker)",
					lang : currentLanguage,
					rules : {
						organizationTitle : {
							minlength : 5,
							required : true
						}
					},
					invalidHandler : function(event, validator) {
						toastr.error(organizationCreateJson.validform.emptyfields,'');
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
						form.submit();
					}
				});
	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> resetForm') : '';		
				
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('organization_create_form');
		});
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		

		// authorization tab control
/*		$(".nav-tabs a[href='#tab_2']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			toastr.info(organizationCreateJson.validform.authinsert,'');
			return false;
		  }
		});
*/		
		/****/
		// general inf tab control
		$(".option a[href='#tab1']").on("click", function(e) {
	        $('.tabContainer').find('.option').removeClass('active');
	        $('#tab-datos').addClass('active');
	    });

		// authorization tab control
		$(".option a[href='#tab2']").on("click", function(e) {
			$('.tabContainer').find('.option').removeClass('active');
			$('#tab-authorizations').addClass('active');
		});
		/***/

		if (organizationCreateJson.actionMode !== null) {
		// if organization has authorizations we load it!.
			var authorizationsJson = organizationCreateJson.authorizations;
			if (authorizationsJson.length > 0 ){

				// MOUNTING AUTHORIZATIONS ARRAY
				var authid_update, role_update , userid_update , authorizationUpdate , authorizationIdUpdate = '';
				$.each( authorizationsJson, function (key, object){

					authid_update 		= object.id;
					role_update 		= object.role;
					userid_update 		= object.userId;

					logControl ? console.log('      |----- authorizations object on Update, ID: ' +  authid_update + ' TYPE: ' +  role_update + ' USER: ' +  userid_update  ) : '';

					// AUTHs-table {"users":user,"roles":role,"id":
					// response.id}
					authorizationUpdate = {"users": userid_update, "roles": role_update, "id": authid_update};
					authorizationsArr.push(authorizationUpdate);

					// AUTH-Ids {[user_id]:auth_id}
					authorizationIdUpdate = {[userid_update]:authid_update};
					authorizationsIds.push(authorizationIdUpdate);

					// disable this users on users select
					$("#users option[value='" + userid_update + "']").prop('disabled', true);
					$("#users").selectpicker('refresh');

				});

				// TO-HTML
				if ($('#authorizations').attr('data-loaded') === 'true'){
					$('#organization_authorizations > tbody').html("");
					$('#organization_authorizations > tbody').append(mountableModel2);
				}
				logControl ? console.log('authorizationsArr on UPDATE: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr)) : '';
				$('#organization_authorizations').mounTable(authorizationsArr,{
					model: '.authorization-model',
					noDebug: false
				});

				// hide info , disable user and show table
				$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));
				$('#authorizations').removeClass('hide');
				$('#authorizations').attr('data-loaded',true);// TO-HTML
				$("#users").selectpicker('deselectAll');

			}
		}
	}	

	// DELETE ORGANIZATION
	var deleteOrganizationConfirmation = function(orgId){
		console.log('deleteOrganizationConfirmation() -> formId: '+ orgId);
		
		// set action and orgId to the form
		$('.delete-organization').attr('id',orgId);
		$('.delete-organization').attr('action','/controlpanel/opendata/organizations/delete/' + orgId);
		console.log('deleteOrganizationConfirmation() -> formAction: ' + $('.delete-organization').attr('action') + ' ID: ' + $('.delete-organization').attr('id'));
		
		// call organization Confirm at header.
		HeaderController.showOrganizationConfirmDialog(orgId);	
	}

	var validateImgSize = function() {
        if ($('#image').prop('files') && $('#image').prop('files')[0].size>60*1024){
			toastr.error(organizationCreateJson.validform.bigimage,'');
        	$('#image').val("");
         } else if ($('#image').prop('files')) {
        	 reader.readAsDataURL($("#image").prop('files')[0]);
         }
    }

	var insertAuthorization = function(){
		// UPDATE MODE ONLY AND VALUES on user
		if ( organizationCreateJson.actionMode !== null){	
			if (($('#users').val() !== '') && ($("#users option:selected").attr('disabled') !== 'disabled') && ($('#roles').val() !== '')){

					// AJAX INSERT (ACTION,ORGID,USER,ROLE) returns
					// object with data.
					authorization('insert',organizationCreateJson.orgId,$('#users').val(),$('#roles').val(),'');

				} else { 
					if ($('#users').val() === '' || $("#users option:selected").attr('disabled') === 'disabled') {
						toastr.error(organizationCreateJson.validform.authuser,'');
					} else {
						toastr.error(organizationCreateJson.validform.authrole,'');
					}
				}
		}
	}

	var updateAuthorization = function(obj){
		logControl ? console.log(LIB_TITLE + ': updateAuthorization()') : '';
		if ( organizationCreateJson.actionMode !== null){

			// AJAX UPDATE (ACTION,ORGID,USER,ROLE) returns
			// object with data.
			var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
			var selRole = $(obj).closest('tr').find("select[name='roles\\[\\]']").val();

			var updateIndex = foundIndex(selUser,'users',authorizationsArr);
			var selAuthorizationId = authorizationsIds[updateIndex][selUser];

			console.log('updateAuthorization:' + selAuthorizationId);

			if (selRole !== authorizationsArr[updateIndex]["roles"]){

				// UPDATING STATUS...
				//$(obj).find("i").removeClass('fa fa-edit').addClass('fa fa-spin fa-refresh');
				$(obj).find("span").text('Updating...');

				authorization('update',organizationCreateJson.orgId, selUser, selRole, selAuthorizationId, obj);
			}
			else { console.log('no hay cambios');}
		}
	}

	var removeAuthorization = function(obj){
		logControl ? console.log(LIB_TITLE + ': removeAuthorization()') : '';
		if ( organizationCreateJson.actionMode !== null){

			// AJAX REMOVE (ACTION,ORGID,USER,ROLE) returns
			// object with data.
			var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
			var selRole = $(obj).closest('tr').find("select[name='roles\\[\\]']").val();

			var removeIndex = foundIndex(selUser,'users',authorizationsArr);
			var selAuthorizationId = authorizationsIds[removeIndex][selUser];

			console.log('removeAuthorization:' + selAuthorizationId);

			authorization('delete',organizationCreateJson.orgId, selUser, selRole, selAuthorizationId, obj );
		}
	}

	// return position to find authId.
	var foundIndex = function(what,item,arr){
		var found = '';
		arr.forEach(function(element, index, array) {
			if ( what === element[item]){ found = index;  console.log("a[" + index + "] = " + element[item] + ' Founded in position: ' + found ); }

		});
		return found;
	}

	var authorization = function(action,orgId,user,role,authorization,btn){
		logControl ? console.log('|---> authorization()') : '';	
		var insertURL = '/controlpanel/opendata/organizations/authorization';
		var updateURL = '/controlpanel/opendata/organizations/authorization/update';
		var deleteURL = '/controlpanel/opendata/organizations/authorization/delete';
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
				data: {"role": role,"orgId": orgId,"user": user},			 
				dataType:"json",
				success: function(response,status){							
					
					var propAuth = {"users":user, "roles": role, "id": response.id};
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
						$('#organization_authorizations > tbody').html("");
						$('#organization_authorizations > tbody').append(mountableModel2);
					}
					console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
					$('#organization_authorizations').mounTable(authorizationsArr,{
						model: '.authorization-model',
						noDebug: false							
					});
					
					// hide info , disable user and show table
					$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));			
					$("#users").selectpicker('deselectAll');
					$("#users option[value='" + $('#users').val() + "']").prop('disabled', true);
					$("#users").selectpicker('refresh');
					$('#authorizations').removeClass('hide');
					$('#authorizations').attr('data-loaded',true);

					toastr.success(messagesForms.operations.genOpSuccess,'');

				},
				error: function(response,status){
					toastr.error(organizationCreateJson.authorizationerror.create,'');
				}
			});	
			
		}
		if (action === 'update'){
			$.ajax({url:updateURL, type:"POST", async: true,
				headers: {
					[csrf_header]: csrf_value
			    },
				data: {"role": role,"orgId": orgId,"user": user},
				dataType:"json",
				success: function(response,status){

					var updateIndex = foundIndex(user,'users',authorizationsArr);
					authorizationsArr[updateIndex]["roles"] = role;
					console.log('ACTUALIZADO: ' + authorizationsArr[updateIndex]["roles"]);

					// UPDATING STATUS...
					//$(btn).find("i").removeClass('fa fa-spin fa-refresh').addClass('fa fa-edit');
					$(btn).find("span").text('Update');

					toastr.success(messagesForms.operations.genOpSuccess,'');
				},
				error: function(response,status){
					toastr.error(organizationCreateJson.authorizationerror.update,'');
				}
			});


		}
		if (action  === 'delete'){
			console.log('    |---> Deleting... ' + user + ' with authId:' + authorization );
			
			$.ajax({url:deleteURL, type:"POST", async: true, 
                headers: {
					[csrf_header]: csrf_value
			    },
				data: {"orgId": orgId,"user": user},			 
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
						$("#users option[value='" + user + "']").prop('disabled', false);
						$("#users").selectpicker('deselectAll');
						$("#users").selectpicker('refresh');
						if (authorizationsArr.length == 0){
							$('#alert-authorizations').toggle(!$('#alert-authorizations').is(':visible'));
							$('#authorizations').addClass('hide');

						}
						
						toastr.success(messagesForms.operations.genOpSuccess,'');
					}
					else{ 
						var errorContent = organizationCreateJson.authorizationerror.delete + ': NO RESPONSE!';
						toastr.error(errorContent,'');
					}
				},
				error: function(response,status){
					var errorContent = organizationCreateJson.authorizationerror.delete + ': ' + response;
					toastr.error(errorContent,'');
				}
			});			
		}	
	};

	
	// CONTROLLER PUBLIC FUNCTIONS
	return{
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
		},
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleValidation();
			initTemplateElements();
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		// DELETE ORGANIZATION
		deleteOrganization: function(orgId){
			logControl ? console.log(LIB_TITLE + ': deleteOrganization()') : '';	
			deleteOrganizationConfirmation(orgId);			
		},

		// VALIDATE IMAGE SIZE
		validateImageSize: function() {
			logControl ? console.log(LIB_TITLE + ': validateImgSize()') : '';
			validateImgSize();
		},

		// INSERT AUTHORIZATION
		insertAuthorization: function() {
			logControl ? console.log(LIB_TITLE + ': insertAuthorization()') : '';
			insertAuthorization();
		},

		// UPDATE AUTHORIZATION
		updateAuthorization : function(obj) {
			logControl ? console.log(LIB_TITLE + ': updateAuthorization()') : '';
			updateAuthorization(obj);
		},

		// REMOVE AUTHORIZATION
		removeAuthorization : function(obj) {
			logControl ? console.log(LIB_TITLE + ': removeAuthorization()') : '';
			removeAuthorization(obj);
		},

		submitform: function(){
			$("#organizationName").val($("#organizationTitle").val().replace(/\s+/g,'-').toLowerCase());
			$('#organization_create_form').submit();		
		}
		
	};
	
}();


//AUTO INIT CONTROLLERS WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	organizationCreateController.load(organizationCreateJson);
	organizationCreateController.init();	
	
});