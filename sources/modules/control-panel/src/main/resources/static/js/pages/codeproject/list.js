var Codeproject = Codeproject || {};

Codeproject.List = (function() {
	"use-strict";
	//var mountableModel = $('#table_parameters').find('tr.parameters-model')[0].outerHTML;
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	headersObj[csrfHeader] = csrfToken;
	var init = function() {
		 setInterval(reloadCodeprojectTable,30000);
		 setTimeout(reloadTooltips, 100);
		// Create event
		$('#btn-codeproject-create').on('click', function (e) {
			e.preventDefault(); 
			window.location = '/controlpanel/codeproject/create';
		})	
	};
	
	
	var dtRenderOptions = function (data, type, row) {
		return '<div class="grupo-iconos text-center">'
		+ '<span data-id="' + row.id + '" class="icon-codeproject-code btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genView+'"><i class="la la-eye font-hg"></i></span>'
		+ '<span data-id="' + row.id + '" class="icon-codeproject-edit btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genUpdate+'"><i class="icon-edit"></i></span>'
		+ '<span data-id="' + row.id + '" class="icon-codeproject-trash btn btn-xs btn-no-border icon-on-table color-red tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genDelete+'"><i class="icon-delete"></i></span>'																											
		+ '</div>';
	};
	
	
	
	
	function initCompleteCallback(settings, json) {
		initTableEvents();
	}
	
	function reloadCodeprojectTable() {
		reloadDataTable();
	}
	
	function reloadDataTable() {		
		var csrfToken = $("meta[name='_csrf']").attr("content");
		var csrfHeader = $("meta[name='_csrf_header']").attr("content"); 
		var headersObj = {};
		var id = $(this).data('find_codeproject');
		headersObj[csrfHeader] = csrfToken;		
		$.ajax({
       	 	url : '/controlpanel/codeproject/data',    
       	 	headers: headersObj,
       	 	contentType:"application/json; charset=utf-8",
       	 	dataType:"json",
            type : 'GET'
        }).done(function(data) {
        	createRows(data);
    		initTableEvents();
    		reloadTooltips();
        });
		
	}
	

	function createRows(data){
		if(data!== null && typeof data!='undefined' && data.length>0){
			var table = $('#codeprojects').DataTable(); 
			table.clear().draw();
			for(var i = 0; i<data.length;i++){
				var tr = '<tr>';
				tr  +=createTrContent(data[i]);
				tr +='</tr>';
				table.row.add($(tr)).draw();
				//$('#codeprojects tbody').append(tr);
			}
  			// HIDE COLUMNS		
		//$.each([ 0 ],function(ind,value){ $("input.toggle-vis[data-column='"+ value +"']").trigger('click'); });
		}
		return null;
	}
	
	function createTrContent(data){
		var html = '<td class="text-left ">'+data.id+'</td>';
		html += '<td class="text-left ">'+data.name+'</td>';
		html += '<td class="text-left ">'+data.owner+'</td>';
		html += '<td class="text-left ">'+data.repo+'</td>';
		html += '<td>'+dtRenderOptions(data,null,data)+'</td>';		
		return html;
	}
	
	function reloadTooltips(){
		$('.tooltips').tooltip('destroy');
		$('.tooltips').tooltip();
	}
	
	function initTableEvents() {
		$('.build-btn').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				 $.ajax({
			       	 	url : 'jenkins/parameters/' +id ,  
			            type : 'GET'
			        }).done(function(data) {
			        	var parameters = data;
			        	if(parameters == null || parameters.length == 0)
			        		return;
			        	else{
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
			    			$('#parametersModal').modal('show');
			    			$('#current-codeproject').val(id);
			        	}
			        }).fail(function(error) {
			        	
			        });
				
			});
		})
		
		$('.icon-codeproject-edit').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				window.location = '/controlpanel/codeproject/update/' + id;
			});
		});
		

		$('.icon-codeproject-trash').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id'); 
				deleteCodeprojectDialog(id);
			});
		});
		
		$('.icon-codeproject-code').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				window.location = '/controlpanel/codeproject/sourcecode/' + id;
			});
		});

		
		
	}
	
	var deleteCodeprojectDialog = function(id) {
		$.confirm({
			title: headerJson.codeprojectDelete,
			theme: 'light',
			columnClass: 'medium',
			content: constants.deleteContent,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: headerJson.btnClose,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				Ok: {
					text: headerJson.btnEliminar,
					btnClass: 'btn btn-primary',
					action: function() { 
						$.ajax({ 
						    url : id,
						    headers: headersObj,
						    type : 'DELETE'
						}).done(function( result ) {							
							reloadCodeprojectTable();
						}).fail(function( error ) {
							$.alert({
								title : 'Error',
								theme : 'light',
								content : error
							})
						}).always(function() {
						});
					}											
				}					
			}
		});
	}
	
	var copyURL = function(url){
			var $temp = $("<input />");
			$("body").append($temp);
			$temp.val(url).select();
			document.execCommand("copy");
			$temp.remove();
			
	}	
	
	var showCodeprojectURLDialog = function(contextpath, type, caas, openshiftUrl, deployed){
		
		var messageInfo = "URL Information";
		var messageCopied = "copied";
		var messageBase = "BASE URL";
		var messageGetNoteInformation = "GET NOTEBOOK INFO URL";
		var messageRunNotebook = "RUN NOTEBOOK URL";
		var messageRunParagraphSync = "RUN PARAGRAPH SYNC URL";
		var messageRunParagraphAsync = "RUN PARAGRAPH ASYNC URL";
		var messageGetParagrapInformation = "GET PARAGRAPH INFO URL";
		
		var pathGetNoteInformation =  "/api/notebook/0IDSTATIC";
		var pathRunNotebook =  "/api/notebook/job/0IDSTATIC";
		var pathRunParagraphSync =  "/api/notebook/run/0IDSTATIC/<paragraph>";
		var pathRunParagraphAsync =  "/api/notebook/job/0IDSTATIC/<paragraph>";
		var pathGetParagrapInformation =  "/api/notebook/0IDSTATIC/paragraph/<paragraph>";

		var dialogContent="";
		if(caas=="RANCHER" || !deployed)
		{
			dialogContent = `	
			<div class="alert alert-info alert-dismissable">
					<button type="button" class="close" data-dismiss="alert" aria-hidden="true"></button>
				<i class="fa fa-info-circle"></i> 
				<span>${messageInfo}</span>			
			</div>
			<div id="infoCopied" class="alert alert-success display-hide">
				<button class="close" data-close="alert"></button> <span >${messageCopied}</span>
			</div>
			<div><label>${messageBase}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${window.location.origin}${contextpath}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			`
		}
		else {
			dialogContent = `	
			<div class="alert alert-info alert-dismissable">
					<button type="button" class="close" data-dismiss="alert" aria-hidden="true"></button>
				<i class="fa fa-info-circle"></i> 
				<span>${messageInfo}</span>			
			</div>
			<div id="infoCopied" class="alert alert-success display-hide">
				<button class="close" data-close="alert"></button> <span >${messageCopied}</span>
			</div>
			<div><label>${messageBase}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${openshiftUrl}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			`
		}
			
		if (type === "NOTEBOOK_ARCHETYPE") {
		dialogContent += `	
			<div><label>${messageGetNoteInformation}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${window.location.origin}${pathGetNoteInformation}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			<div><label>${messageRunNotebook}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${window.location.origin}${pathRunNotebook}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			<div><label>${messageRunParagraphSync}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${window.location.origin}${pathRunParagraphSync}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			<div><label>${messageRunParagraphAsync}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${window.location.origin}${pathRunParagraphAsync}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			<div><label>${messageGetParagrapInformation}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${window.location.origin}${pathGetParagrapInformation}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			`
		} 
		
		
		$.confirm({
			title: headerJson.codeprojectUrl,
			theme: 'light',
			columnClass: 'medium',
			content:dialogContent,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				Ok: {
					text: headerReg.btnCancelar,
					btnClass: 'btn btn-outline blue dialog',
					action: function(){ 
					}											
				},
				Copy: {
					text: headerReg.btnCopiar,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if(caas=="RANCHER" || !deployed)
						{
							copyURL(window.location.origin + contextpath);
						}
						else {
							copyURL(openshiftUrl);
						}
					}	
				}
			}
		});
	};
	
	
	
	// Public API
	return {
		init: init,
		dtRenderOptions: dtRenderOptions,
		initCompleteCallback: initCompleteCallback,
		reloadCodeprojectTable: reloadCodeprojectTable
	};
	
})();

$(document).ready(function() {	
	
	Codeproject.List.init();
	Codeproject.List.reloadCodeprojectTable();

});
