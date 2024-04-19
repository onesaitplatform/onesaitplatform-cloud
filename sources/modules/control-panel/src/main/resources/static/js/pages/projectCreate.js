var ProjectCreateController = function() {
	var authorizationEndpoint = '/controlpanel/projects/authorizations';
	var parentAuthorization = {};
	var LIB_TITLE = 'Menu Controller';
	var logControl = 1;
	var form1 = $('#project_create_form');
	
	var initTemplateElements = function() {
		var csrf_header = headerReg.csrfHeaderName;
		var csrf_value = headerReg.csrfToken;
		$.ajaxSetup({'headers': {
		       [csrf_header]: csrf_value
		}});
		
		$(".disabled").on("click", function(e) {
			e.preventDefault();
			$.alert({
				title : 'Info',
				theme : 'light',
				content : projectCreateJson.validations.createfirst
			});
			return false;
		});
		
		$('#resource-identification-filter').keypress(function(e) {
		    if(e.which == 13) {
		        getResourcesFiltered();
		    }
		});

		$('#check-realm').on('change', function() {
			var useRealm = $('#check-realm').is(':checked');

			if (!useRealm) {
				$('#platform-users').removeClass('hide');
				$('#realms-select').addClass('hide');
				$('#alert-realm').addClass('hide');
				$('#create-realm').addClass('hide');
			} else {
				$('#platform-users').addClass('hide');
				$('#realms-select').removeClass('hide');
				$('#alert-realm').removeClass('hide');
				$('#create-realm').removeClass('hide');
			}

		});
		
		// INPUT MASK FOR project identification allow only letters, numbers and -_
		$("#project-name").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
		// Reset form
		$('#resetBtn').on('click', function() {
			cleanFields('project_create_form');
		});
		
		// authorization tab control 
		$(".option a[href='#tab_1']").on("click", function(e) {
			$('.tabContainer').find('.option').removeClass('active');
	        $(this).closest("div").addClass('active');
		});
		
		$(".option a[href='#tab_2']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			return false;
		  } else {
	        $('.tabContainer').find('.option').removeClass('active');
	        $(this).closest("div").addClass('active');
		  }
		});
		
		$(".option a[href='#tab_3']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			return false;
		  } else {
	        $('.tabContainer').find('.option').removeClass('active');
	        $(this).closest("div").addClass('active');
		  }
		});
		
		$(".option a[href='#tab_4']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			return false;
		  } else {
	        $('.tabContainer').find('.option').removeClass('active');
	        $(this).closest("div").addClass('active');
		  }
		});
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});
		
		$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.getElementsByTagName('select')[0]){
				$('.form').validate().element('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id'));
			}
		})
	}
	
	var handleValidation = function() {
		
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
		// set current language
		currentLanguage = currentLanguage || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// validation rules
            rules: {
            	identification:	{required: true, minlength: 5},
            	description: {required: true, minlength: 5}
            },
            messages: {
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
            	toastr.success(messagesForms.validation.genFormSuccess,'');
            	form.submit();
            }
        });
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
	
	var addWebProject = function() {
		var webProject = $('#webprojects').val();

		if (webProject != '') {
			$("#webprojects-tab-fragment").load(
					'/controlpanel/projects/addwebproject', {
						'webProject' : webProject,
						'project' : projectCreateJson.projectId
					}, function() {
						refreshSelectpickers();
						toastr.success(messagesForms.operations.genOpSuccess,'');
					});
		} else {
			toastr.error(messagesForms.operations.genOpError,'');
		}

	}

	var removeWebProject = function() {

		$("#webprojects-tab-fragment").load(
				'/controlpanel/projects/removewebproject', {
					'project' : projectCreateJson.projectId
				}, function() {
					refreshSelectpickers();
					toastr.success(messagesForms.operations.genOpSuccess,'');
				});

	}

	var addPlatformUser = function() {
		var user = $('#users').val();
		if (user != '') {
			$("#users-tab-fragment").load('/controlpanel/projects/adduser', {
				'user' : user,
				'project' : projectCreateJson.projectId
			}, function() {
				refreshSelectpickers();
				toastr.success(messagesForms.operations.genOpSuccess,'');
			});
		} else {
			toastr.info(projectCreateJson.validations.selectUser,'');
		}
	}

	var removePlatformUser = function(user) {
		if (user != '') {
			$("#users-tab-fragment").load('/controlpanel/projects/removeuser',
					{
						'user' : user,
						'project' : projectCreateJson.projectId
					}, function() {
						refreshSelectpickers();
						toastr.success(messagesForms.operations.genOpSuccess,'');
					});
		} else {
			toastr.info(projectCreateJson.validations.selectUser,'');
		}
	}

	var unsetRealm = function() {
		var realm = realmLinked;
		if (realm != null && realm != '') {
			$.confirm({
				title : projectCreateJson.confirm.unlinkRealmTitle,
				theme : 'light',
				columnClass : 'medium',
				content : projectCreateJson.confirm.unlinkRealm,
				draggable : true,
				dragWindowGap : 100,
				backgroundDismiss : true,
				buttons : {
					close : {
						text : headerReg.btnCancelar,
						btnClass : 'btn btn-outline blue dialog',
						action : function() {
						} // GENERIC CLOSE.
					},
					remove : {
						text : headerReg.btnEliminar,
						btnClass : 'btn btn-primary',
						action : function() {
							$("#users-tab-fragment").load(
									'/controlpanel/projects/unsetrealm', {
										'realm' : realm,
										'project' : projectCreateJson.projectId
									}, function() {
										refreshSelectpickers();
										refreshResourcesFragment();
										toastr.success(messagesForms.operations.genOpSuccess,'');
									});
						}
					}
				}
			});

		}
	}

	var setRealm = function() {
		var realm = $("#realms").val();
		if (realm != '') {
			$("#users-tab-fragment").load('/controlpanel/projects/setrealm', {
				'realm' : realm,
				'project' : projectCreateJson.projectId
			},function(){
				refreshResourcesFragment();
				toastr.success(messagesForms.operations.genOpSuccess,'');
			});

		} else {
			toastr.info(projectCreateJson.validations.selectRealm,'');
		}
	}

	var refreshSelectpickers = function() {
		$('#realms').selectpicker();
		$('#users').selectpicker();
		$('#webprojects').selectpicker();
		$('.select-modal').selectpicker();
		$('#check-realm').on('change', function() {
			var useRealm = $('#check-realm').is(':checked');

			if (!useRealm) {
				$('#platform-users').removeClass('hide');
				$('#realms-select').addClass('hide');
			} else {
				$('#platform-users').addClass('hide');
				$('#realms-select').removeClass('hide');
			}

		});
		$('.tooltips').tooltip();
	}

	var getResourcesFiltered = function() {
		var identification = $('#resource-identification-filter').val()
		var type = $('#resource-type-filter').val();
		$('#resources-modal-fragment').load(
				'/controlpanel/projects/resources?identification='
						+ identification + '&project='
						+ projectCreateJson.projectId + '&type=' + type,
				function() {
					$('#resources-modal').modal('show');
					refreshSelectpickers();
				});
	}

	var insertAuthorization = function(obj) {
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		var type = $("#resource-type-filter").val();
		var resource = $(obj).closest('tr').find("input[name='ids\\[\\]']")
				.val();
		var accesstype = $(obj).closest('tr').find(
				'select.accesstypes :selected').val();
		var authorizing = $(obj).closest('tr').find(
				'select.authorizing :selected').val();
		if (accesstype == '' || authorizing == '') {
			toastr.info(projectCreateJson.validations.selectAccessAndUser,'');
		} else {
			var authorization = {
				'project' : projectCreateJson.projectId,
				'resource' : resource,
				'authorizing' : authorizing,
				'access' : accesstype,
				'resourceType': type
			}
			parentAuthorization = authorization;
			if (type == 'GADGET' || type == 'DASHBOARD'||
					type == 'GADGETDATASOURCE' || type == 'ONTOLOGY') {
				$('#associated-modal-fragment').load(
						'/controlpanel/projects/associated?resourceId='
								+ resource + '&project='
								+ projectCreateJson.projectId + '&type=' + type,
						function(response) {
							if (associatedElements.length > 0){
								$.confirm({
									title: "Info",
									theme: 'light',
									columnClass: 'medium',
									content: projectCreateJson.otologiesAssociated,
									draggable: true,
									dragWindowGap: 100,
									backgroundDismiss: true,
									buttons: {
										close: {
											text: projectCreateJson.close,
											btnClass: 'btn btn-outline blue dialog',
											action: function (){} //GENERIC CLOSE.		
										},
										Ok: {
											text: headerReg.btnConfirmar,
											btnClass: 'btn btn-primary',
											action: function(){
												$('#associated-modal').modal('show');
											}										
										},
									}
								});	
								
							}
							else {
								handleAuth(authorization, 'POST').done(updateResourcesFragment)
								.fail(showGenericError);
							}
						});
			} else {
				handleAuth(authorization, 'POST').done(updateResourcesFragment)
				.fail(showGenericError);
			}
		}
	}
	
	var showGenericError = function(){
		toastr.error(messagesForms.operations.genOpError,'');
	}
	
	var insertElementsAssociated = function (){
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		var authorizations = [];
		var authorizing = parentAuthorization["authorizing"];
		var project = projectCreateJson.projectId;
		authorizations.push(parentAuthorization);

		$('#tableBody').each(function(){
		    $(this).find('tr').each(function(){
				var authorization = {};
		    	var accesstype = $(this).closest('tr').find('select.accesstypes :selected').val();
				var resource = $(this).closest('tr').find("input[name='ids\\[\\]']").val();
				var project = projectCreateJson.projectId;
				var type = $('#resource-type-filter').val();
				authorization["project"] = project;
				authorization["resource"] = resource;
				authorization["authorizing"] = authorizing;
				authorization["access"] = accesstype;
				authorization["resourceType"] = type;
				authorizations.push(authorization);
		    })
		})
		$.ajax({
			url : '/controlpanel/projects/authorizationsAssociated',
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'POST',
			data : JSON.stringify(authorizations),
			contentType : "application/json",
			dataType : "html",
		}).done(updateResourcesFragment);
		$('#associated-modal').modal('hide');
	}

	var removeAuthorization = function(id) {
		var payload = {
			'id' : id,
			'project' : projectCreateJson.projectId
		};
		handleAuth(payload, 'DELETE').done(updateResourcesFragment).fail();

	}
	var handleAuth = function(payload, methodType) {
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		if (methodType == 'POST') {
			return $.ajax({
				url : authorizationEndpoint,
				headers: {
					[csrf_header]: csrf_value
			    },
				type : methodType,
				data : JSON.stringify(payload),
				contentType : "application/json",
				dataType : "html",
			});
		} else if (methodType == 'DELETE') {
			return $.ajax({
				url : authorizationEndpoint + '?' + $.param(payload),
				headers: {
					[csrf_header]: csrf_value
			    },
				type : methodType,
				data : payload,
				dataType : "html",
			});
		}
	}
	var refreshResourcesFragment = function() {
		toastr.success(messagesForms.operations.genOpSuccess,'');
		$('#resources-tab-fragment').load(
				authorizationEndpoint + '?project='
						+ projectCreateJson.projectId, function() {
					refreshSelectpickers();
				});
	}
	var updateResourcesFragment = function(response) {
		toastr.success(messagesForms.operations.genOpSuccess,'');
		$('#resources-tab-fragment').html(response);
		refreshSelectpickers();
	}

	var sortHTML = function(id, sel, sortvalue, attribute){
		  var a, b, i, ii, y, bytt, v1, v2, cc, j;
		  a = $(id);
		  for (i = 0; i < a.length; i++) {
		    for (j = 0; j < 2; j++) {
		      cc = 0;
		      y = 1;
		      while (y == 1) {
		        y = 0;
		        b = a[i].querySelectorAll(sel);
		        for (ii = 0; ii < (b.length - 1); ii++) {
		          bytt = 0;
		          if (sortvalue) {
		            v1 = b[ii].querySelector(sortvalue).children[0].getAttribute(attribute);
		            v2 = b[ii + 1].querySelector(sortvalue).children[0].getAttribute(attribute);
		          } else {
		            v1 = b[ii].innerText;
		            v2 = b[ii + 1].innerText;
		          }
		          v1 = v1.toLowerCase();
		          v2 = v2.toLowerCase();
		          if ((j == 0 && (v1 > v2)) || (j == 1 && (v1 < v2))) {
		            bytt = 1;
		            break;
		          }
		        }
		        if (bytt == 1) {
		          b[ii].parentNode.insertBefore(b[ii + 1], b[ii]);
		          y = 1;
		          cc++;
		        }
		      }
		      if (cc > 0) {break;}
		    }
		  }
	}
	
	return {
		removeAuthorization : function(id) {
			removeAuthorization(id);
		},
		insertAuthorization : function(obj) {
			insertAuthorization(obj);
		},
		insertElementsAssociated : function () {
			insertElementsAssociated();
		},
		getResourcesFiltered : function() {
			getResourcesFiltered();
		},
		addWebProject : function() {
			addWebProject();
		},
		removeWebProject : function() {
			removeWebProject();
		},
		removePlatformUser : function(user) {
			removePlatformUser(user);
		},
		addPlatformUser : function() {
			addPlatformUser();
		},
		unsetRealm : function() {
			unsetRealm();
		},
		setRealm : function() {
			setRealm();
		},
		sortHTML : function(id, sel, sortvalue, attribute){
			sortHTML(id, sel, sortvalue, attribute);
		},

		init : function() {
			initTemplateElements();
			handleValidation();
		},
		// REDIRECT
		go : function(url) {
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			navigateUrl(url);
		}
	}

}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	// ProjectCreateController.load(appCreateJson);

	// AUTO INIT CONTROLLER.
	ProjectCreateController.init();
});