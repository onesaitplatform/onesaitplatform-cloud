var Vertical = Vertical || {};

Vertical.List = (function() {
	"use-strict";
	
	var updateUrl= "/controlpanel/multitenancy/verticals/update/";
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	headersObj[csrfHeader] = csrfToken;
	var init = function() {
				
	};
	
	var dtRenderOptions = function (data, type, row) {
		return '<div class="grupo-iconos text-center">'
		+ '<a href="'+updateUrl + row.name+'"><span data-id="' + row.name + '" class="btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genUpdate+'"><i class="icon-edit"></i></span></a>'
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
			Vertical.List.initCompleteCallback()
		}, true);
		
		$('.tooltip').tooltip('destroy');
		$('.tooltips').tooltip();
	}
	

	function initTableEvents() {

		$('.upload-btn').each(function() {
			$(this).on('click', function (e) {
				e.preventDefault(); 
				$('#uploadModal').modal('show');
			});
		});
		
		
		$('.tooltips').tooltip();
		
		
	}

	
	return {
		dtRenderOptions: dtRenderOptions,
		init: init,
		initCompleteCallback: initCompleteCallback,
		reloadTable: reloadTable
		
	};
})();


$(document).ready(function() {	
	
	Vertical.List.init();

});
