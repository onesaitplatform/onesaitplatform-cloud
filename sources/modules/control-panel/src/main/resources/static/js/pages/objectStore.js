var BinaryFilesController = function() {
	var mainPath = '/controlpanel/files/minio/';
	var publicPath = '/controlpanel/files/minio/public';
	var listPath = '/controlpanel/files/minio';
	var metadataPath = mainPath + 'metadata/';
	var getAuthsPath = mainPath + 'authorization/';
	var maxsizePath = mainPath + 'maxsize';
	var authorizationsArr 		= []; // add authorizations
	var authorizationUpdateArr  = []; // get authorizations of the file
	var authorizationsIds 		= []; // get authorizations ids for actions
	var authorizationObj 		= {}; // object to receive authorizations responses.
	var csrf_value = $("meta[name='_csrf']").attr("content");
	var csrf_header = $("meta[name='_csrf_header']").attr("content");
	$.ajaxSetup({'headers': {
		[csrf_header]: csrf_value
    }});

	var mountableModel2 = $('#file_autthorizations').find('tr.authorization-model')[0].outerHTML;
	
	var getBinaryFile = function(id){
		navigateUrl(mainPath + id );
	}
	var changePublic = function(id){
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		$.ajax({
			url: publicPath, 
			headers: {
				[csrf_header]: csrf_value
		    },
		    type: 'POST',
		    data: {'id' : id},
		    success: function(response){
		    	navigateUrl(listPath);
		    }
		});
	}
	var deleteFile = function(id){
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		$.ajax({
		    url: mainPath + id,
		    contentType: 'application/json; charset=utf-8',
		    dataType: 'json',
			headers: {
				[csrf_header]: csrf_value
		    },
		    type: 'DELETE',
		    success: function(response) {
		    	navigateUrl(listPath);
		    },
			error: function (err) {
				toastr.error(messagesForms.operations.genOpError + ':', xhr.responseText);
			}
		});
	}
	
	var updateModal = function(id){
	    $('#updateFileId').val(id);
		$('#updateFile').modal('show');
	}
	
	var copyUrl = function(url){
		var context = window.location.href.split(window.location.pathname)[0];
		var $temp = $("<input />");
		$("body").append($temp);
		$temp.val(context + url).select();
		document.execCommand("copy");
		$temp.remove();
		
	}
	
	var copyText = function(text){
		var $temp = $("<input />");
		$("body").append($temp);
		$temp.val(text).select();
		document.execCommand("copy");
		$temp.remove();
		
	}

	// AJAX AUTHORIZATION FUNCTIONS
	
	var loadInitialAuths = function(id){
		jQuery.get(getAuthsPath + id, function(authorizations){
			authorizationsArr 		= []; 
			authorizationUpdateArr  = []; 
			authorizationsIds 		= []; 
			authorizationObj 		= {}; 
			$("#users option").each(function(){
				$(this).prop('disabled', false);
			});
			authorizations.forEach(function(authorization){
				
				var propAuth = {"users":authorization.userId,"accesstypes":authorization.typeName,"id": authorization.id};
				authorizationsArr.push(propAuth);
				console.log('     |---> JSONtoTable: ' + authorizationsArr.length + ' data: ' + JSON.stringify(authorizationsArr));
				// store ids for after actions.	inside callback 				
				var user_id = authorization.userId;
				var auth_id = authorization.id;
				var AuthId = {[user_id]:auth_id};
				authorizationsIds.push(AuthId);
				$("#users option[value=" + user_id + "]").prop('disabled', true);

			});
			// TO-HTML
			if ($('#authorizations').attr('data-loaded') === 'true'){
				$('#file_autthorizations > tbody').html("");
				$('#file_autthorizations > tbody').append(mountableModel2);
			}
			console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
			$('#file_autthorizations').mounTable(authorizationsArr,{
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
	var authorization = function(action,file,user,accesstype,authorization,btn){
		
		var insertURL = '/controlpanel/files/minio/authorization';
		var updateURL = '/controlpanel/files/minio/authorization/update';
		var deleteURL = '/controlpanel/files/minio/authorization/delete';
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
				data: {"accesstype": accesstype, "fileId": file,"user": user},			 
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
						$('#file_autthorizations > tbody').html("");
						$('#file_autthorizations > tbody').append(mountableModel2);
					}
					console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
					$('#file_autthorizations').mounTable(authorizationsArr,{
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

					toastr.success(messagesForms.validation.genFormSuccess,'');
				}
			});

	
		}
		if (action === 'update'){
			
			$.ajax({url:updateURL, type:"POST", async: true, 
				headers: {
					[csrf_header]: csrf_value
			    },
				data: {"id": authorization, "accesstype": accesstype},			 
				dataType:"json",
				success: function(response,status){
							
					var updateIndex = foundIndex(user,'users',authorizationsArr);			
					authorizationsArr[updateIndex]["accesstypes"] = accesstype;

					// UPDATING STATUS...
					$(btn).find("i").removeClass('fa fa-spin fa-refresh').addClass('fa fa-edit');
					$(btn).find("span").text('Update');

					toastr.success(messagesForms.validation.genFormSuccess,'');
				}
			});
			
			
		}
		if (action  === 'delete'){
			
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
						if (authorizationsArr.length == 0){
							$('#alert-authorizations').toggle(!$('#alert-authorizations').is(':visible'));					
							$('#authorizations').addClass('hide');						}

						toastr.success(messagesForms.validation.genFormSuccess,'');
					}
					else{ 
						toastr.warning(messagesForms.validation.genFormError,'No response!');
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
	
	
	var getMaxsize = function(){
		jQuery.get(maxsizePath, function(response){
			setEventListeners(response.maxsize);
			
		});
	}
	var setEventListeners = function(maxsize){
		 $('#buttonLoadFile').bind('change', function() {
	           if(this.files[0].size> maxsize){
	        	   $('#modal-error').modal('show');
	        	   return false;
	           }else
	        	   $('#modal-error').modal('hide');
	        	   
	        
	      });
		 $('#buttonLoadUpdateFile').bind('change', function() {
	           if(this.files[0].size > maxsize){
	        	   $('#modal-error').modal('show');
	        	   return false;
	           }else
	        	   $('#modal-error').modal('hide');
	        	   
	        
	      });
	}
	return{
	
		init : function(){
			
			getMaxsize();
		
		},
		getBinaryFile : function(id){
			getBinaryFile(id);
		},
		changePublic : function(id){
			changePublic(id);
		},
		confirmDelete : function(id){
			// i18 labels
			var Remove = headerReg.btnEliminar;
			var Close = headerReg.btnCancelar;
			var	Content = headerReg.binaryFileConfirm;
			var Title = headerReg.binaryFileDelete;

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
							deleteFile(id);
						}
					}					
				}
			});
		},
		deleteFile : function(id){
			deleteFile(id);
		},
		updateModal : function(id){
			updateModal(id);
		},
		copyToClipboard : function (url){
			copyUrl(url);
		},
		copyS3AddressToClipboard : function (text){
			copyText(text);
		},
		// INSERT AUTHORIZATION
		insertAuthorization: function(){
				
				// UPDATE MODE ONLY AND VALUES on user and accesstype
				if (($('#users').val() !== '') && ($("#users option:selected").attr('disabled') !== 'disabled') && ($('#accesstypes').val() !== '')){
					
					// AJAX INSERT (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns object with data.
					authorization('insert',$('#authFileId').val(),$('#users').val(),$('#accesstypes').val(),'');
								
				}
			
		},
		
		// REMOVE authorization
		removeAuthorization: function(obj){
		
				
			// AJAX REMOVE (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns object with data.
			var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
			var selAccessType = $(obj).closest('tr').find("select[name='accesstypes\\[\\]']").val();				
			
			var removeIndex = foundIndex(selUser,'users',authorizationsArr);				
			var selAuthorizationId = authorizationsIds[removeIndex][selUser];
			
			
			authorization('delete',$('#authFileId').val(), selUser, selAccessType, selAuthorizationId, obj );				
		
		},		
		// UPDATE authorization
		updateAuthorization: function(obj){
	
				
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
					
					authorization('update',$('#authFileId').val(), selUser, selAccessType, selAuthorizationId, obj);
				} 

			
		},
		openAuthModal : function (id){
			$('#authFileId').val(id);
			//TODO GET AUTHS AND GENERATE THEM
			loadInitialAuths(id);
		}
	}
}();


jQuery(document).ready(function() {
	
	BinaryFilesController.init();
});
