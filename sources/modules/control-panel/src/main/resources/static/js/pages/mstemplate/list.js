var Mstemplate = Mstemplate || {};

Mstemplate.List = (function() {
	"use-strict";
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	headersObj[csrfHeader] = csrfToken;
	var init = function() {
		// Create event
		$('#btn-report-create').on('click', function (e) {
			e.preventDefault(); 
			window.location = '/controlpanel/mstemplates/create';
		})		
	};
	
	var dtRenderOptions = function (data, type, row) {
		return '<div class="grupo-iconos text-center">'
		+ '<span data-id="' + row.id + '" data-contextPath="' + row.contextPath + '" data-caas="' + row.caas + '" data-deployed="' + row.deployed + '" data-url="' + row.deploymentUrl + '" class="btn-url btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genUrl+'"><i class="la la-eye font-hg"></i></span></a>'
		+ '<span data-id="' + row.id + '" class="icon-mstemplate-edit btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genUpdate+'"><i class="icon-edit"></i></span>'
		+ '<a target="_blank" href="'+row.deploymentUrl+'"><span data-id="' + row.id + '" class="btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genView+'"><i class="icon-url"></i></span></a>'
		+ '<span data-id="' + row.id + '" class="icon-mstemplate-trash btn btn-xs btn-no-border icon-on-table color-red tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genDelete+'"><i class="icon-delete"></i></span>'																											
		+ '</div>';
	};
	
	var dtRenderLinks = function (data,type,row){
		if(data == null)
			return '<div class="text-center" ><span th:text="0" style="display:none" ></span><i class="la la-times-circle-o text-danger  font-hg"></i></div>';
		else
			return '<div class="text-center" ><a href="'+data+'" target="_blank"><span class="btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.go+'"><i class="icon-url"></i></span></a></div>';
	}
	
	
	function initCompleteCallback(settings, json) {
		initTableEvents();
	
	}
	
	var removeEnvVar = function(obj){
		$(obj).closest('tr').remove();
	}
	
	function initTableEvents() {
		//$('.tooltips').tooltip();
		
		$('.btn-url').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				var contextpath = $(this).data('contextpath');
				var templatee = $(this).data('template');
				var caas = $(this).data('caas');
				var deployed =  $(this).data('deployed');
				var openshiftUrl = $(this).data('url');
				
				showMstemplateURLDialog(contextpath, templatee, caas, openshiftUrl, deployed)
				
			});
		})
		

		$('.icon-mstemplate-edit').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				window.location = '/controlpanel/mstemplates/update/' + id;
			});
		});
		

		$('.icon-mstemplate-trash').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id'); 
				deleteMstemplateDialog(id);
			});
		});
		
	}
	
	// Public API
	return {
		init: init,
		dtRenderOptions: dtRenderOptions,
		initCompleteCallback: initCompleteCallback,
		dtRenderLinks: dtRenderLinks,
		removeEnvVar: removeEnvVar
		
	};
	
})();

$(document).ready(function() {	
	
	Mstemplate.List.init();

});
