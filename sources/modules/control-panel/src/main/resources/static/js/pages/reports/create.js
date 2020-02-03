//== Set Vars of all the site
var Report = Report || {};

Report.Create = (function() {
	"use-strict";
	

	var $tableParams = $("#table-report-parameters");
	var filesPath = '/controlpanel/files/';
	var reportsPath = '/controlpanel/reports/'
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	headersObj[csrfHeader] = csrfToken;
	var init = function() {

		// -- Events -- //
		
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


	
	
	function submitForm($form, action, method) {
		$form.attr('action', action);
		$form.attr('method', 'post');
		$form[0].submit();
	}
	
	
	
	
	// Public API
	return {
		init: init
	};
})();

$(document).ready(function() {	
	
	Report.Create.init();
});
