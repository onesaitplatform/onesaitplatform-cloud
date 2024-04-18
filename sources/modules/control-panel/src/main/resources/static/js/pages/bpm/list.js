var Bpm = Bpm || {};

Bpm.List = (function() {
	"use-strict";
	var authsUrl = '/controlpanel/bpm/authorizations';
	var uploadUrl = '/controlpanel/bpm/upload?'+ csrfParameter + "=" + csrfValue;
	var useTenantHelp = "";
	var authorizationsArr 		= []; // add authorizations
	var authorizationObj 		= {}; // object to receive authorizations responses.
	//var mountableModel2 = $('#tenant_authorizations').find('tr.authorization-model')[0].outerHTML;
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	headersObj[csrfHeader] = csrfToken;
	var init = function() {
		initTableEvents();
		useTenantHelp = $('#useTenantHelp').text();
	};
	
	var dtRenderOptions = function (data, type, row) {
		return '<div class="grupo-iconos text-center">'
		+ '<a target="_blank" href="'+constants.camundaWebapp+'"><span data-id="' + row.id + '" class="btn btn-xs btn-no-border color-blue icon-on-table tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genView+'"><i class="la la-eye font-hg"></i></span></a>'
		+ '<span data-id="' + row.name + '" class="btn-auths btn btn-xs btn-no-border color-blue icon-on-table tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.authorizations+'"><i class="icon-lock"></i></span>'
		+ '<span class="dropdown"> <span class="btn btn-xs btn-no-border color-blue icon-on-table" data-container="body" data-placement="bottom" data-toggle="dropdown" data-hover="dropdown" data-close-others="true" ><i class="icon-overflow"></i></span><ul class="dropdown-menu dropdown-menu-table download-menu">'
		+ '<li> <span data-id="' + row.name + '" class="upload-btn btn btn-xs btn-no-border btn-circle btn-outline tooltips" data-container="body" data-placement="bottom" th:data-original-title="'+constants.upload+'"><i class="icon-upload"></i><span class="span-title-three-dots-icons" th:text="'+constants.upload+'">Upload</span></span></li>'
		+ '<li> <span data-id="' + row.name + '" class="btn-delete btn btn-xs btn-no-border btn-circle btn-outline tooltips color-red" data-container="body" data-placement="bottom" th:data-original-title="'+constants.genDelete+'"><i class="icon-delete"></i><span class="span-title-three-dots-icons" th:text="'+constants.genDelete+'">Delete</span></span></li>'
		+ '</ul> </span>'
		+ '</div>';
	};
	
	function initCompleteCallback(settings, json) {
		
		initTableEvents();
	
	}
	
	
	
	function reloadTable() {
		var oTable = $('.datatable').dataTable();
		reloadDataTable(oTable);
	}
	
	function reloadDataTable(oTable) {		
		oTable.fnClearTable();
		
		oTable.DataTable().ajax.reload(function() {
			Bpm.List.initCompleteCallback()
		}, true);
		
		$('.tooltip').tooltip('destroy');
		$('.tooltips').tooltip();
	}
	
	
	
	function insertAuthorization(obj){
//		var id = $(this).data('id'); 
		var id = $('#currentTenat').val();
		// UPDATE MODE ONLY AND VALUES on user and accesstype
		if (($('#users').val() !== '') && ($("#users option:selected").attr('disabled') !== 'disabled')){
			

			authorization('insert',id,$('#users').val(),'');
		}
						
	}
	// REMOVE authorization
	function removeAuthorization(obj){
	
		var id = $('#currentTenat').val();
		// AJAX REMOVE (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns object with data.
		var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
		
		authorization('delete',id, selUser, obj );				
	
	}
	
	var authorization = function(action,tenantId,user,btn){
		
	
		var response = {};
		
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
	
		if (action === 'insert'){
			console.log('    |---> Inserting... ' + authsUrl);
						
			$.ajax({
				url:authsUrl ,
				headers: {
					[csrf_header]: csrf_value,
					"Content-type": "application/json"
			    },
				type:"POST",
				async:true,
				data: JSON.stringify({"tenantId": tenantId,"userId": user})							
			}).done(function(){							
					
					var propAuth = {"users":user,"tenantId": tenantId};
					authorizationsArr.push(propAuth);
					console.log('     |---> JSONtoTable: ' + authorizationsArr.length + ' data: ' + JSON.stringify(authorizationsArr));
					// store ids for after actions.	inside callback 				
					var user_id = user;
					var auth_id = response.tenantId;
					
					// TO-HTML
					if ($('#authorizations').attr('data-loaded') === 'true'){
						$('#tenant_authorizations > tbody').html("");
						$('#tenant_authorizations > tbody').append(mountableModel2);
					}
					console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
					$('#tenant_authorizations').mounTable(authorizationsArr,{
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
					
			}).fail(function(error) {
	        	$.alert({
					title : 'ERROR',
					type : 'red',
					theme : 'light',
					content : error
				})
				
			});

	
		}
		
		if (action  === 'delete'){
		
			
			$.ajax({url:authsUrl +'/' +tenantId+'/'+user, type:"DELETE", async: true, 
				headers: {
					[csrf_header]: csrf_value
			    },
				data: JSON.stringify({"tenantId": tenantId, "userId": user })			
				
			}).done(function(response,status){									
				
				var index = authorizationsArr.findIndex(a => (a.tenantId == tenantId && a.users == user) );
				if(index > -1){
					authorizationsArr.splice(index, 1);
					// refresh interface. TO-DO: EL this este fallará					
					if ( status == "success"  ){ 
						$(btn).closest('tr').remove();
						$("#users option[value=" + user + "]").prop('disabled', false);						
						$("#users").selectpicker('deselectAll');
						$("#users").selectpicker('refresh');
						if (authorizationsArr.length == 0){
							$('#alert-authorizations').toggle(!$('#alert-authorizations').is(':visible'));					
							$('#authorizations').addClass('hide');
							
						}
						
					}else{ 
						$.alert({title: 'ALERT!', theme: 'dark', type: 'red', content: 'NO RESPONSE!'}); 
					}
				}
			}).fail(function(error) {
	        	$.alert({
					title : 'ERROR',
					type : 'red',
					theme : 'light',
					content : error
				})
				
			});		
		}	
	};
	
	
	function upload(){
		  if($('#name').val() == ""){
       	   $('#form_upload').addClass("has-error");
       	   return;
       	}else{
       		$('#form_upload').removeClass("has-error");
       	}
       		
           var formData = new FormData(document.getElementById("form_upload"));
           $.ajax({
               url: uploadUrl,
               type: "POST",
               dataType: "html",
               data: formData,
               cache: false,
               contentType: false,
               processData: false
           }).done(function(res){
           	location.reload();
           }).fail(function(error) {
	        	$.alert({
					title : 'ERROR!',
					type : 'red',
					theme : 'light',
					content : 'Could not deploy resources: ' + error.responseText
				});
	        });
           $('#uploadModal').modal('hide');
	}

	function initTableEvents() {

		$('.upload-btn').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				$('#useTenantHelp').text(useTenantHelp + ': ' +  $(this).data('id') )
				$('#tenantId').val($(this).data('id'));
				$('#uploadModal').modal('show');
			});
		});
		$('.btn-auths').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id'); 
				openAuthModal(id);
			});
		});
		
		$("#data").on("change", function(){
	  		var file = $('input[type=file]').val().split('\\').pop();
	  		if(file!=null){
	  			$("#submitNewFile").removeAttr('disabled');
	  		}
	  	});
		
		
		
		$('.tooltips').tooltip();
		
		
	}
	
	// AJAX AUTHORIZATION FUNCTIONS
	
	var loadInitialAuths = function(id){
		jQuery.get(authsUrl +'/' + id, function(authorizations){
			authorizationsArr 		= []; 
			
			
			authorizationObj 		= {}; 
			$("#users option").each(function(){
				$(this).prop('disabled', false);
			});
			authorizations.forEach(function(authorization){
				
				var propAuth = {"users":authorization.userId,"tenantId": authorization.tenantId};
				authorizationsArr.push(propAuth);
				console.log('     |---> JSONtoTable: ' + authorizationsArr.length + ' data: ' + JSON.stringify(authorizationsArr));
				// store ids for after actions.	inside callback 				
				var user_id = authorization.userId;
				var auth_id = authorization.id;
				
				
				$("#users option").each(function(i, e){
					if (e.value == user_id){
						e.disabled=true;
					}

				});
				
				

			});
			// TO-HTML
			if ($('#authorizations').attr('data-loaded') === 'true'){
				$('#tenant_authorizations > tbody').html("");
				$('#tenant_authorizations > tbody').append(mountableModel2);
			}
			console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
			$('#tenant_authorizations').mounTable(authorizationsArr,{
				model: '.authorization-model',
				noDebug: false							
			});
			
			// hide info , disable user and show table
			$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));			
			$("#users").selectpicker('deselectAll');
			$("#users").selectpicker('refresh');
			$('#authorizations').removeClass('hide');
			$('#authorizations').attr('data-loaded',true);
			$('#authorizationsModal').modal('show');
		});
	}
	function openAuthModal(id){
		//TODO GET AUTHS AND GENERATE THEM
		$('#currentTenat').val(id);
		loadInitialAuths(id);
		
	}
	
	return {
		dtRenderOptions: dtRenderOptions,
		init: init,
		initCompleteCallback: initCompleteCallback,
		reloadTable: reloadTable,
		insertAuthorization: insertAuthorization,
		removeAuthorization: removeAuthorization,
		upload: upload
		
	};
})();


$(document).ready(function() {	
	
	Bpm.List.init();

});
