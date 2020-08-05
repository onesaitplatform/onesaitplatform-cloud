var ProjectCreateController = function() {
	var authorizationEndpoint = '/controlpanel/projects/authorizations';
	var parentAuthorization = {};
	var initTemplateElements = function() {
		var csrf_header = headerReg.csrfHeaderName;
		var csrf_value = headerReg.csrfToken;
		$.ajaxSetup({'headers': {
		       [csrf_header]: csrf_value
		}});
		
		
		$(".disabled").on("click", function(e) {
			e.preventDefault();
			$.alert({
				title : 'INFO!',
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
			} else {
				$('#platform-users').addClass('hide');
				$('#realms-select').removeClass('hide');
			}

		});
		
		// INPUT MASK FOR project identification allow only letters, numbers and -_
		$("#project-name").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });

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
					});
		}

	}

	var removeWebProject = function() {

		$("#webprojects-tab-fragment").load(
				'/controlpanel/projects/removewebproject', {
					'project' : projectCreateJson.projectId
				}, function() {
					refreshSelectpickers();
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
			});
		} else {
			$.alert({
				title : 'INFO!',
				theme : 'light',
				content : projectCreateJson.validations.selectUser
			});
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
					});
		} else {
			$.alert({
				title : 'INFO!',
				theme : 'light',
				content : projectCreateJson.validations.selectUser
			});
		}
	}

	var unsetRealm = function() {
		var realm = realmLinked;
		if (realm != null && realm != '') {
			$.confirm({
				icon : 'fa fa-warning',
				title : headerReg.titleConfirm + ':',
				theme : 'light',
				type : 'red',
				columnClass : 'medium',
				content : projectCreateJson.confirm.unlinkRealm,
				draggable : true,
				dragWindowGap : 100,
				backgroundDismiss : true,
				closeIcon : true,
				buttons : {
					close : {
						text : headerReg.btnCancelar,
						btnClass : 'btn btn-circle btn-outline blue',
						action : function() {
						} // GENERIC CLOSE.
					},
					remove : {
						text : headerReg.btnEliminar,
						btnClass : 'btn btn-circle btn-outline btn-primary',
						action : function() {
							$("#users-tab-fragment").load(
									'/controlpanel/projects/unsetrealm', {
										'realm' : realm,
										'project' : projectCreateJson.projectId
									}, function() {
										refreshSelectpickers();
										refreshResourcesFragment();
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
			});

		} else {
			$.alert({
				title : 'INFO!',
				theme : 'light',
				content : projectCreateJson.validations.selectRealm
			});
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
			$.alert({
				title : 'INFO!',
				theme : 'light',
				content : projectCreateJson.validations.selectAccessAndUser
			});
		} else {
			var authorization = {
				'project' : projectCreateJson.projectId,
				'resource' : resource,
				'authorizing' : authorizing,
				'access' : accesstype
			}
			parentAuthorization = authorization;
			if (type == 'GADGET' || type == 'DASHBOARD'||
					type == 'GADGETDATASOURCE') {
				$('#associated-modal-fragment').load(
						'/controlpanel/projects/associated?resourceId='
								+ resource + '&project='
								+ projectCreateJson.projectId + '&type=' + type,
						function(response) {
							if (associatedElements.length > 0){
								$.confirm({
									title: "INFO!",
									theme: 'light',
									columnClass: 'medium',
									content: projectCreateJson.otologiesAssociated,
									draggable: true,
									dragWindowGap: 100,
									backgroundDismiss: true,
									closeIcon: true,
									buttons: {
										close: {
											text: projectCreateJson.close,
											btnClass: 'btn btn-sm btn-outline btn-circle blue',
											action: function (){} //GENERIC CLOSE.		
										},
										Ok: {
											text: "Ok",
											btnClass: 'btn btn-sm btn-outline btn-circle btn-primary',
											action: function(){
												$('#associated-modal').modal('show');
											}										
										},
									}
								});	
								
							}
							else {
								handleAuth(authorization, 'POST').done(updateResourcesFragment)
								.fail();
							}
						});
			} else {
				handleAuth(authorization, 'POST').done(updateResourcesFragment)
				.fail();
			}
		}
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
				authorization["project"] = project;
				authorization["resource"] = resource;
				authorization["authorizing"] = authorizing;
				authorization["access"] = accesstype;
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
		$('#resources-tab-fragment').load(
				authorizationEndpoint + '?project='
						+ projectCreateJson.projectId, function() {
					refreshSelectpickers();
				});
	}
	var updateResourcesFragment = function(response) {
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