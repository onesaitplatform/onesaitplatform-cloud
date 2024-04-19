var RuleDomain = RuleDomain || {};

RuleDomain.List = (function() {
	"use-strict";
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	headersObj[csrfHeader] = csrfToken;
	var init = function() {
		initTableEvents();
		setTimeout(reloadTooltips, 100);
	};
	
	var dtRenderOptions = function (data, type, row) {
		var html ='<div class="grupo-iconos text-center">';
		if(row.active){
			html+= '<span data-id="' + row.id + '" class="btn btn-show btn-xs btn-no-border btn-circle btn-outline blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genView+'"><i class="la la-eye font-hg"></i></span>'
			html+= '<span data-id="' + row.id + '" class="btn btn-stop btn-xs btn-no-border btn-circle btn-outline blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.stop+'"><i class="la la-stop font-hg"></i></span>'
			
		}else{
			html+= '<span data-id="' + row.id + '" class="btn btn-start btn-xs btn-no-border btn-circle btn-outline blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.start+'"><i class="la la-play font-hg"></i></span>'
		}
		html+= '<span data-id="' + row.id + '" class="icon-ruledomain-trash btn btn-xs btn-no-border btn-circle btn-outline blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genDelete+'"><i class="la la-trash font-hg"></i></span>'																											
		html+= '</div>';
		return html;
	};

	var dtRenderState = function(data,type,row){
		var html ='';
		if(row.active)
			html +='<span th:text="1" style="display:none" ></span><i class="la la-check-circle-o text-success font-hg"></i>';
		else
			html+='<span th:text="0" style="display:none" ></span><i class="la la-times-circle-o text-danger font-hg"></i>';
		return html;
	}
	
	
	
	function initCompleteCallback(settings, json) {
		
		initTableEvents();
	
	}
	function reloadTooltips(){
		$('.tooltips').tooltip('destroy');
		$('.tooltips').tooltip();
	}
	
	
	
	function reloadRuleDomainTable() {
		var oTable = $('.datatable').dataTable();
		reloadDataTable(oTable);
	}
	
	function reloadDataTable(oTable) {		
		oTable.fnClearTable();
		
		oTable.DataTable().ajax.reload(function() {
			RuleDomain.List.initCompleteCallback()
		}, true);
		
		$('.tooltips').tooltip('destroy');
		$('.tooltips').tooltip();
	}
	
	function initTableEvents() {		
		
		$('.btn-show').off().on('click', function(){
			var id = $(this).data('id');
			navigateUrl(id+'/rules');
		});
		
		$('.btn-stop , .btn-start').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				var message = $(this).hasClass('btn-start') ? 'started' : 'stopped';
				$.ajax({
		       	 	url : id +'/start-stop' ,  
		       	 	headers: headersObj,
		            type : 'POST'
		        }).done(function(data) {
		        	$.alert({
						title : 'INFO',
						type : 'blue',
						theme : 'light',
						content : 'Domain '+ message
					});
		        	location.reload()
		        }).fail(function(error) {
		        	$.alert({
						title : 'ERROR',
						type : 'red',
						theme : 'light',
						content : error
					})
					
				});
			});
		});
		
		$('.icon-ruledomain-trash').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id'); 
				deleteRuleDomainDialog(id);
			});
		});
		
		
	}
	
	var deleteRuleDomainDialog = function(id) {
		$.confirm({
			icon: 'fa fa-warning',
			title: headerJson.btnEliminar,
			theme: 'light',
			columnClass: 'medium',
			content: constants.deleteContent,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			closeIcon: true,
			buttons: {
				close: {
					text: headerJson.btnClose,
					btnClass: 'btn btn-sm btn-circle btn-outline blue',
					action: function (){} // GENERIC CLOSE.
				},
				Ok: {
					text: headerJson.btnEliminar,
					btnClass: 'btn btn-sm btn-circle btn-outline btn-blue',
					action: function() { 
						$.ajax({ 
						    url : id,
						    headers: headersObj,
						    type : 'DELETE'
						}).done(function( result ) {							
							reloadRuleDomainTable();
						}).fail(function( error ) {
						}).always(function() {
						});
					}											
				}					
			}
		});
	}
	
	
	// Public API
	return {
		init: init,
		dtRenderOptions: dtRenderOptions,
		dtRenderState: dtRenderState,
		initCompleteCallback: initCompleteCallback,
		reloadRuleDomainTable: reloadRuleDomainTable
		
	};
	
})();

$(document).ready(function() {	
	
	RuleDomain.List.init();

});
