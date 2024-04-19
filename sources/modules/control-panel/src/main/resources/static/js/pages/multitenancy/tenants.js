var Tenant = Tenant || {};

Tenant.List = (function() {
	"use-strict";
	
	var showUrl= "/controlpanel/multitenancy/tenants/";
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	headersObj[csrfHeader] = csrfToken;
	var init = function() {
				
	};
	
	var dtRenderOptions = function (data, type, row) {
		return '<div class="grupo-iconos text-center">'
		+ '<a href="'+showUrl+ row.name+'/show"><span data-id="' + row.name + '" class="icon-edit btn btn-xs btn-no-border btn-circle btn-outline blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genUpdate+'"><i class="la la-edit font-hg"></i></span></a>'
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
			Tenant.List.initCompleteCallback()
		}, true);
		
		$('.tooltip').tooltip('destroy');
		$('.tooltips').tooltip();
	}
	

	function initTableEvents() {

		
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
	
	Tenant.List.init();

});
