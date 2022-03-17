Report.List = (function() {
	"use-strict";
	var mountableModel = $('#table_parameters').find('tr.parameters-model')[0].outerHTML;
	var csrf = {}
	csrf[headerJson.csrfHeaderName] = headerJson.csrfToken;
	var init = function() {
		
		$('#btn-report-create').on('click', function (e) {
			e.preventDefault(); 
			window.location = '/controlpanel/reports/create';
		})		
		initTableEvents();
		
	};
	
	



	var ajaxDownload = function(url, httpMethod, payload, extension) {
		var http = new XMLHttpRequest();
		var params = "parameters=" + JSON.stringify(payload) + "&extension="
				+ extension;
		http.open(httpMethod, url, true);
		http.setRequestHeader('Content-type',
				'application/x-www-form-urlencoded');
		http.setRequestHeader(headerReg.csrfHeaderName,
				headerReg.csrfToken);
		http.responseType = 'blob';

		http.onload = function() {
			if (http.status === 200) {
				var filename = http.getResponseHeader('content-disposition')
						.split('=')[1];
				var contentType = http.getResponseHeader('content-type');
				var blob = new Blob([ http.response ], {
					type : contentType
				});
				var link = document.createElement('a');
				link.href = window.URL.createObjectURL(blob);
				link.download = filename;

				document.body.appendChild(link);

				link.click();

				document.body.removeChild(link);
			} else {
				$.alert({
					title : 'ERROR!',
					type : 'red',
					theme : 'light',
					content : 'Problem executing your Report' + http.getResponseHeader('X-Download-Error')
				});
			}
		}
		http.send(params);
	}
	var runReportWithParameters = function(){
		var id = $('#current-report').val();
		var elements =  $('#table-body').find('tr');
		var parametersArray = [];
		elements.each(function(){
			var check = $(this).find("input[name='checkparameter\\[\\]']");
			if(check.is(':checked')){
				var name = $(this).find("input[name='name\\[\\]']").val();
				var value = $(this).find("input[name='value\\[\\]']").val();
				var type = $(this).find("input[name='type\\[\\]']").val();
				var parameter = {"name":name, "type": type, "value":value, "description": ""};
				parametersArray.push(parameter);
			}
			
		});
		var extension = $('#export-type').val();
		if(extension == null || extension == '')
			extension = PDF;
		ajaxDownload('/controlpanel/reports/download/report/'+ id, 'POST', parametersArray, extension);
		

	}
	
	var getParameters= function(id){
		 $.ajax({
	       	 	url : '/controlpanel/reports/' +id +'/parameters',
	            type : 'GET'
	        }).done(function(data) {
	        	var parameters = data;
	        	
	        	if(parameters == null || parameters.length == 0){
	        	
	    				$('#table_parameters > tbody').html("");
	    			
	        	}else{
	        		if ($('#parameters').attr('data-loaded') === 'true'){
	    				$('#table_parameters > tbody').html("");
	    				$('#table_parameters > tbody').append(mountableModel);
	    			}
	        		
	        		$('#table_parameters').mounTable(parameters,{
	    				model: '.parameters-model',
	    				noDebug: false							
	    			});
	        		$('#parameters').removeClass('hide');
	    			$('#parameters').attr('data-loaded',true);
	        	}
	        	$('#parametersModal').modal('show');
 			$('#current-report').val(id);
	        }).fail(function(error) {
	        	$.alert({
					title : 'ERROR!',
					type : 'red',
					theme : 'light',
					content : 'Could not get report parameters: ' + error.responseText
				});
	        });
	}
	
	function initTableEvents() {
		
		$('.report-play').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				getParameters(id);
			});
		})
		
		$('#export-type').on('change', function(){
			var val = $('#export-type').val();
			if(val == '')
				$('#submit-params').prop("disabled", true);
			else
				$('#submit-params').removeAttr("disabled");
				
		});
		
		$('.report-trash').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id'); 
				deleteReportDialog(id);
			});
		});

		$('.report-download').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				$.fileDownload('/controlpanel/reports/download/report-design/'+ id , {
		    		httpMethod: 'GET',
		    		successCallback: function(url) {
		    		},
		    		failCallback: function(response, url) {
		    			$.alert({
							title : 'ERROR!',
							type : 'red',
							theme : 'light',
							content : 'Could not download report' + response
						});
		    		}
		    	});
			
			});
		});
		
		$('.report-edit').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				window.location = '/controlpanel/reports/edit/' + id;
			});
		});
	}
	
	var deleteReportDialog = function(id) {
		$.confirm({
			title: headerReg.reportDelete,
			theme: 'light',
			columnClass: 'medium',
			content: headerReg.reportConfirm,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: headerReg.btnCancelar,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				Ok: {
					text: headerReg.btnEliminar,
					btnClass: 'btn btn-primary',
					action: function() { 
						$.ajax({ 
							headers: csrf,
						    url : '/controlpanel/reports/delete/' + id,
						    type : 'DELETE'
						}).done(function( result ) {							
							location.reload();
						}).fail(function( error ) {
							$.alert({
								title : 'ERROR!',
								type : 'red',
								theme : 'light',
								content : 'Could not delete report' + error.responseText
							});
						}).always(function() {
						});
					}											
				}					
			}
		});
	}
	

	return {
		init: init,
		runReportWithParameters: runReportWithParameters,
		getParameters: getParameters
	};
	
})();

$(document).ready(function() {
	Report.List.init();
});
