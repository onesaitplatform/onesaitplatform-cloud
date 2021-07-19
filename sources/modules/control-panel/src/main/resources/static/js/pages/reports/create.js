//== Set Vars of all the site
var Report = Report || {};

Report.Create = (function() {
	"use-strict";
	

	var $tableParams = $("#table-report-parameters");
	var filesPath = '/controlpanel/files/';
	var fetchResourcesURL = '/controlpanel/reports/resources';
	var reportsPath = '/controlpanel/reports/';
	var addResourceURL = '/controlpanel/reports/report/resources';
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	headersObj[csrfHeader] = csrfToken;
	var csrf_value = $("meta[name='_csrf']").attr("content");
	var csrf_header = $("meta[name='_csrf_header']").attr("content");
	var mountableModel = $('#resources').find('tr.resources-model')[0].outerHTML;
	var init = function() {

		// INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
		// -- Events -- //
		
		$('#btn-report-add-resources').off().on('click',function(){
			
			fetchExistingResources();
		})
		
		$('.btn-download-resource').each(function() {
			$(this).on('click', function (e) {
				var id = $(this).data('id');
				window.location = filesPath + id;
			});
		});
		
		$('.btn-update-resource').each(function() {
			$(this).on('click', function (e) {
				var binaryId = $(this).data('id');
				var reportId = $(this).data('report');
				var url =  reportsPath + 'report/' +reportId + '/resource/' + binaryId;
				$('#form_update_resource').attr("action", url);
				$('#updateFile').modal('show');
				$("#form_update_resource").validate().cancelSubmit = true;
			});
		});
		
		$('.btn-delete-resource').each(function() {
			$(this).on('click', function (e) {
				var binaryId = $(this).data('id');
				var reportId = $(this).data('report');
				$.ajax({
		       	 	url : reportsPath + 'report/' +reportId + '/resource/' + binaryId ,  
		       	 	headers: headersObj,
		            type : 'DELETE'
		        }).done(function(data) {
		        	location.reload();
		        }).fail(function(error) {
		        	$.alert({
						title : 'ERROR',
						type : 'red',
						theme : 'light',
						content : error.responseText
					})
					
				});
				
			});
		});
		
		$("#btn-report-cancel").on('click', function (e) {
			e.preventDefault();
			window.location = '/controlpanel/reports/list';
		});
		
	
		$("#btn-report-upload").on('click', function (e) {
			$("#btn-report-upload-file").click();
		});
		
		$("#btn-report-upload-resources").on('click', function (e) {
			$("#btn-report-upload-file-resources").click();
		});
		
		$("#submitUpdate").on('click', function (e) {
			e.preventDefault();
			submitForm($('#form_update_resource'),$('#form_update_resource').attr('action'), 'post');
		});
		
		$('#form-report').on('submit', function(e){
			e.preventDefault();
			submitForm($('#form-report'),$('#report-save-action').val(), $('#report-save-method').val());
		})

	};	

	function addResource(obj){
		var resourceId = $(obj).closest('tr').find("input[name='ids\\[\\]']").val();
		$.ajax({
			url:addResourceURL, 
			type:"PUT", 
			async: true, 
			headers:headersObj,
			data: {"resourceId":resourceId ,"reportId":reportId},
			success: function(response,status){		
				
				$("#reportResources").load('/controlpanel/reports/edit/' +reportId+'/resources/fragment',headersObj)	
			}
		});	
	}

	function fetchExistingResources(){
		$.ajax({
			url:fetchResourcesURL + "?currentReportId="+reportId,
			headers:headersObj,
			type:"GET",
			async: true,		 
			dataType:"json",
			success: function(response,status){							
				var resources = [];
				response.forEach( function(r){
					resources.push({"ids":r.id,"users":r.userId, "resources":r.fileName});
				})
									
				// TO-HTML
				if ($('#resources-div').attr('data-loaded') === 'true'){
					$('#resources > tbody').html("");
					$('#resources > tbody').append(mountableModel);
				}
			
				$('#resources').mounTable(resources,{
					model: '.resources-model',
					noDebug: false							
				});
				$('#resources-div').removeClass('hide');
				$('#resources-div').attr('data-loaded',true);
				
				$('#modal-add-resources').modal('show');
				
			}
		});
	}
	
	function submitForm($form, action, method) {
		$form.attr('action', action + "?" + csrfParameter + "=" + csrfValue);
		$form.attr('method', 'post');
		$form[0].submit();
	}
	
	
	
	
	// Public API
	return {
		init: init,
		addResource:addResource
	};
})();

$(document).ready(function() {	
	
	Report.Create.init();
});
