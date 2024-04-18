var TagCreateController = function() {
	var tagsEndpoint = '/controlpanel/tags';
	var parentAuthorization = {};
	var LIB_TITLE = 'Menu Controller';
	var logControl = 1;
	var form1 = $('#project_create_form');
	var oTable;
	
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
				content : tagCreateJson.validations.createfirst
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
				$('#platform-users-header').removeClass('hide');
				$('#realms-select').addClass('hide');
				$('#alert-realm').addClass('hide');
				$('#create-realm').addClass('hide');
				$('#platform-users-table').addClass('col-md-9');
				$('#platform-users-table').removeClass('col-md-12');
			} else {
				$('#platform-users').addClass('hide');
				$('#platform-users-header').addClass('hide');
				$('#realms-select').removeClass('hide');
				$('#alert-realm').removeClass('hide');
				$('#create-realm').removeClass('hide');
				$('#platform-users-table').addClass('col-md-12');
				$('#platform-users-table').removeClass('col-md-9');
			}

		});
		
		// INPUT MASK FOR project identification allow only letters, numbers and -_
		$("#project-name").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
		// Reset form
		$('#resetBtn').on('click', function() {
			cleanFields('project_create_form');
		});

		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});
		
		$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.getElementsByTagName('select')[0]){
				$('.form').validate().element('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id'));
			}
		})
		
		initTable();
		
		calculateTotals();
		
		showAddedResource();
		
	}
	
	var initTable = function(){
		oTable = $('#resource-access-list').DataTable({
			paging: false,
			columnDefs: [
				{
					targets: [0, 1, 2],
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
		if (oTable.settings()[0]){
			initFilter();
		}
	}
	
	var initFilter = function(){
		if (oTable.settings()[0]){
			$('#resource-access-list_wrapper div.dataTables_filter').addClass('hide');
			$('#resource-access-list_wrapper > div.row').addClass('hide');
			
			$('#search-on-title').append($('#resource-access-list_wrapper div.dataTables_filter > label > input'));
			$('#search-on-title > input').removeClass('input-xsmall')
			
			if ($("#search-on-title").children().length>2){
				$("#search-on-title").find('input:first').remove();
			}
			
			// RESET ALL FILTERS BTN
			$('#clearFilters').on('click', function(){			
				yadcf.exResetAllFilters(oTable);		
			});
			
			yadcf.init(oTable, [			
				    {column_number : 3,
				    	filter_type: "select",
				    	filter_container_id:"accessfilter",
				    	filter_default_label: tagCreateJson.accessType,
				    	render : function (data, type, row){
			                  		return "TEXT";
			               		}
				    }
				  ]);
			
			var filtersResets = $('.yadcf-filter-reset-button');
		    filtersResets.html('<i class="icon-delete"></i>');
		    filtersResets.addClass("btn color-blue");
		    filtersResets.on('click',function(e){
		    	$('#accessfilter').toggleClass('hide');
		    });
	
		    turnFirstOptionToGrey();
		    
			$(".yadcf-filter").on("change",function(e){
				refreshFilter();
				if ($(".yadcf-filter option[value='-1']").is(":selected")){
					turnFirstOptionToGrey();
					
				} else {
					$(".yadcf-filter").css('color','black');
				}
			});
		}
	}
	
	var turnFirstOptionToGrey = function (){
		$(".yadcf-filter").css('color','grey');
		$(".yadcf-filter option").css('color','black');
	}
	
	var calculateTotals = function(){
		var total = 0;
		$(".checkbox-filter").each(function() {
			var countclass ="." + $( this ).attr('id').split("_")[1];		
			total = total + $(".resource-row" + countclass).length;
			$('#count_' + $( this ).attr('id').split("_")[1]).text($(".resource-row" + countclass).length);			
		})
		$('#count_ALL').text(total);
	}
	
	var showAddedResource = function(){
		if (tagCreateJson.resourceTypeAdded!=null || document.referrer.indexOf('project')==-1){
			$(".option a[href='#tab_3']").trigger("click");
		}
		if (tagCreateJson.resourceTypeAdded!=null) {
			$("#resource-identification-filter").val(tagCreateJson.resourceIdentificationAdded);
			$("#resource-type-filter").val(tagCreateJson.resourceTypeAdded).change();
			$('#search').trigger("click");
		}
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

	var refreshSelectpickers = function() {
		$('#realms').selectpicker();
		$('#users').selectpicker();
		$('#webprojects').selectpicker();
		$('.select-modal').selectpicker();
		$('#check-realm').on('change', function() {
			var useRealm = $('#check-realm').is(':checked');

			if (!useRealm) {
				$('#platform-users').removeClass('hide');
				$('#platform-users-header').removeClass('hide');
				$('#realms-select').addClass('hide');
				$('#platform-users-table').addClass('col-md-9');
				$('#platform-users-table').removeClass('col-md-12');
			} else {
				$('#platform-users').addClass('hide');
				$('#platform-users-header').addClass('hide');
				$('#realms-select').removeClass('hide');
				$('#platform-users-table').addClass('col-md-12');
				$('#platform-users-table').removeClass('col-md-9');
			}

		});
		$('.tooltips').tooltip();
	}

	var getResourcesFiltered = function() {
		var identification = $('#resource-identification-filter').val();
		var type = $('#resource-type-filter').val();
		if (tagCreateJson.resourceTypeAdded!=null){
			type = tagCreateJson.resourceTypeAdded;
		}
		App.blockUI({boxed: true, overlayColor:"#5789ad",type:"loader",state:"warning",message:"Searching..."});
		$('#resources-modal').modal('hide');
		$('#resources-modal-fragment').load(
				'/controlpanel/tags/resources?identification='
						+ identification + '&type=' + type,
				function() {
					refreshSelectpickers();
					App.unblockUI();
					refreshFilter();
					$('#resource-identification-filter').val(identification);
					$('#resources-modal').modal('show');
					if (tagCreateJson.resourceTypeAdded!=null){
						$("#resource-type-filter").val(tagCreateJson.resourceTypeAdded).change();
					} else {
						$("#resource-type-filter").val(type).change();
					}
					
				});
	}

	var insertAuthorization = function(obj) {
		var resource = $(obj).closest('tr').find("input[name='ids\\[\\]']").val();
		var tagName = $(obj).closest('tr').find('select.tagnames')
		var selectedValues = Array.from(tagName[0].selectedOptions).map(option => option.value);
		var tagRelation = selectedValues.map(el=>{
			return {'name' : el,
					'resourceId' : resource}
		})
		if (tagRelation.length) {
			parentAuthorization = tagRelation;
			handleAuth(tagRelation, 'POST').done(updateResourcesFragment)
				.fail(showGenericError);
		}else{
			toastr.info(tagCreateJson.tagSelectError,'');
		}
	}
	
	var showGenericError = function(){
		toastr.error(messagesForms.operations.genOpError,'');
	}

	var removeResourceTag = function(resourceId, tagId) {
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		App.blockUI({boxed: true, overlayColor:"#5789ad",type:"loader",state:"warning",message:"Removing..."});
		$.ajax({
				url : tagsEndpoint+"/tagId?resourceId="+resourceId+"&tagId="+tagId,
				headers: {
					[csrf_header]: csrf_value
			    },
				type : "DELETE",				
				contentType : "application/json",
				dataType : "html",
		}).done(updateResourcesFragment).fail();;
		App.unblockUI();
	}
	
	var handleAuth = function(payload, methodType) {
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		if (methodType == 'POST') {
			return $.ajax({
				url : tagsEndpoint,
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
				url : tagsEndpoint + '?name=' + payload,
				headers: {
					[csrf_header]: csrf_value
			    },
				type : methodType,
				dataType : "html",
			});
		} else if (methodType == 'DELETE_ALL') {
			return $.ajax({
				url : tagsEndpoint + "/all" + '?' + $.param(payload),
				headers: {
					[csrf_header]: csrf_value
			    },
				type : 'DELETE',
				data : JSON.stringify(payload),
				dataType : "html",
			});
		}
	}
	var refreshResourcesFragment = function() {
		toastr.success(messagesForms.operations.genOpSuccess,'');
		$('#resources-tab-fragment').load(
				tagsEndpoint + '?project='
						+ tagCreateJson.projectId, function() {
					refreshSelectpickers();
					oTable.clear();
					initTable();
					refreshFilter()
					calculateTotals();
				});
	}
	var updateResourcesFragment = function(response) {
		toastr.success(messagesForms.operations.genOpSuccess,'');
		$('#resources-tab-fragment').html(response);
		refreshSelectpickers();
		oTable.clear();
		initTable();
		refreshFilter()
		calculateTotals();
	}

	var sortHTML = function(id, sel, sortvalue){
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
		            v1 = b[ii].querySelector(sortvalue).children[1].value;
		            v2 = b[ii + 1].querySelector(sortvalue).children[1].value;
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
	
	function refreshFilter(){
		if ($( "#checkbox_ALL").prop("checked")){
			$(".resource-row").removeClass("invisible");
			$(".resource-combo").removeClass("invisible");
			$(".resource-combo").closest("li").removeClass("invisible");
		} else {
			$(".checkbox-filter").each(function() {
				if ($( this ).prop("checked")){
					$("." + $( this ).attr('id').split("_")[1]).removeClass("invisible");
					$(".resource-combo." + $( this ).attr('id').split("_")[1]).removeClass("invisible");
					$(".resource-combo." + $( this ).attr('id').split("_")[1]).closest("li").removeClass("invisible");
				} else {
					$("." + $( this ).attr('id').split("_")[1]).addClass("invisible");
					$(".resource-combo." + $( this ).attr('id').split("_")[1]).addClass("invisible");
					$(".resource-combo." + $( this ).attr('id').split("_")[1]).closest("li").addClass("invisible");
				}	
			});
		}
	}
	
	function filterTable(type){
		if (type == "ALL"){
			$('.checkbox-filter').attr('checked', false);
			if ($( "#checkbox_ALL").prop("checked")){
				$(".resource-row").removeClass("invisible");	
				$(".checkbox-filter").each(function() {     
					$("#checkbox_" + $( this ).attr('id').split("_")[1]).prop('checked', true);
				});						
			} else {
				$(".resource-row").addClass("invisible");
				$(".checkbox-filter").each(function() {     
					$("#checkbox_" + $( this ).attr('id').split("_")[1]).prop('checked', false);
				});				
			}
		} else {
			if ($( "#checkbox_ALL").prop("checked")){
				$( "#checkbox_ALL").prop("checked", false);				
				$(".resource-combo").addClass("invisible");
				$(".resource-combo").closest("li").addClass("invisible");
			}
			if ($( "#checkbox_" + type).prop("checked")){
				$("." + type).removeClass("invisible");
			} else {
				$("." + type).addClass("invisible");
				$(".resource-combo." + type).closest("li").addClass("invisible");				
			}	
		}
		$(".resource-combo").removeClass("invisible");
		$(".resource-combo").closest("li").removeClass("invisible");
		
	}

	function createTagName(){
		$.confirm({
			title: tagCreateJson.tagCreateNew,
			theme: 'light',
			content: `<br/><input style="margin-top:10px" id="tag-name-create" type="text" name="tag-name-create" class="form-control" placeholder="${tagCreateJson.placeholder}"/>`,
			draggable: true,
			dragWindowGap: 300,
			backgroundDismiss: true,
			buttons: {	
				close: {
					text: headerJson.btnCancelar,
					btnClass: 'btn btn-outline blue dialog'
				},
				OK: {
					text: tagCreateJson.create,
					btnClass: 'btn btn-primary blue',
					action: function (){
						var csrf_value = $("meta[name='_csrf']").attr("content");
						var csrf_header = $("meta[name='_csrf_header']").attr("content");
						let tagN = $('#tag-name-create').val();
						if(tagN !== ''){
							//TO-DO create tag name and reload fragment
							fetch(tagsEndpoint + '/name?name=' + tagN, {
								method: 'POST',
								headers: {
							      [csrf_header]: csrf_value
							    }
							})
							.then(r => {
								window.location.reload();
							});
							
						}						
						
					} 
				}
			}
		});
	}
	
	function deleteTag(name){
		HeaderController.deleteStandardActionConfirmationDialog(
			name, 
			headerReg.btnCancelar, 
			headerReg.btnEliminar, 
			tagCreateJson.confirm.deleteTagConfirmation, 
			tagCreateJson.confirm.deleteTag,
			"",
			function(){
         		var csrf_value = $("meta[name='_csrf']").attr("content");
				var csrf_header = $("meta[name='_csrf_header']").attr("content");
				fetch(tagsEndpoint + '?name=' + name,{
					headers: {
						[csrf_header]: csrf_value
				    },
				    method: 'DELETE'
				}).then(r => window.location.reload())
			}
		)
	}
	
	function unlinkTag(name){
		HeaderController.deleteStandardActionConfirmationDialog(
			name, 
			headerReg.btnCancelar, 
			headerReg.btnEliminar, 
			tagCreateJson.confirm.unlinkTagConfirmation, 
			tagCreateJson.confirm.unlinkTag,
			"",
			function(){
         		var csrf_value = $("meta[name='_csrf']").attr("content");
				var csrf_header = $("meta[name='_csrf_header']").attr("content");
				fetch(tagsEndpoint+"/unlink?tagId="+name, {
					headers: {
						[csrf_header]: csrf_value
				    },
				    method: 'DELETE'
				}).then(r => window.location.reload())
			}
		)
	}
	
	
	return {
		createTagName : function (){
			createTagName();
		},
		removeResourceTag : function(resourcesId, tagId) {
			removeResourceTag(resourcesId, tagId);
		},
		insertAuthorization : function(obj) {
			App.blockUI({boxed: true, overlayColor:"#5789ad",type:"loader",state:"warning",message:"Adding..."});
			insertAuthorization(obj);
			App.unblockUI();
		},
		insertElementsAssociated : function () {
			insertElementsAssociated();
		},
		getResourcesFiltered : function() {
			getResourcesFiltered();
		},
		sortHTML : function(id, sel, sortvalue){
			sortHTML(id, sel, sortvalue);
		},
		toggleAllVisible: function(){
			toggleAllVisible();
		},
		filterTable : function(id){
			filterTable(id);
		},
		turnFirstOptionToGrey: function(){
			turnFirstOptionToGrey();
		},
		init : function() {
			initTemplateElements();
			handleValidation();
		},
		deleteTag: function(name){
			deleteTag(name);	
		},
		unlinkTag: function(tagId){
			unlinkTag(tagId);	
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
	// TagCreateController.load(appCreateJson);

	// AUTO INIT CONTROLLER.
	TagCreateController.init();
});
