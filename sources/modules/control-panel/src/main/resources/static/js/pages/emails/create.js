//== Set Vars of all the site
var Email = Email || {};

Email.Create = (function() {
	"use-strict";


	var filesPath = '/controlpanel/files/';
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	headersObj[csrfHeader] = csrfToken;
	var csrf_value = $("meta[name='_csrf']").attr("content");
	var csrf_header = $("meta[name='_csrf_header']").attr("content");
	var form1 = $('#form-email');


	var init = function(data) {

		ontologyCreateReg = data;

		// INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });

		// -- Events -- //

		$('#btn-email-add-resources').off().on('click', function() {

			fetchExistingResources();
		})

		$('.btn-download-resource').each(function() {
			$(this).on('click', function(e) {
				var id = $(this).data('id');
				window.location = filesPath + 'gridfs/' + id;
			});
		});

		$('.close-add-resources').each(function() {
			$(this).on('click', function(e) {
				location.reload();
			});
		});

		$('.btn-delete-emails').each(function() {
			$(this).off().on('click', function(e) {
				e.preventDefault();
				var id = data.actionMode;
				deleteemailDialog(id);
			});
		});

		var deleteemailDialog = function(id) {
			$.confirm({
				title: headerReg.emailDelete,
				theme: 'light',
				columnClass: 'medium',
				content: headerReg.emailConfirm,
				draggable: true,
				dragWindowGap: 100,
				backgroundDismiss: true,
				buttons: {
					close: {
						text: headerReg.btnCancelar,
						btnClass: 'btn btn-outline blue dialog',
						action: function() { } //GENERIC CLOSE.		
					},
					Ok: {
						text: headerReg.btnEliminar,
						btnClass: 'btn btn-primary',
						action: function() {
							$.ajax({
								headers: headersObj,
								url: '/controlpanel/emails/delete/' + id,
								type: 'DELETE'
							}).done(function(result) {
								window.location = '/controlpanel/emails/list';
							}).fail(function(error) {
								$.alert({
									title: 'ERROR!',
									type: 'red',
									theme: 'light',
									content: 'Could not delete email' + error.responseText
								});
							}).always(function() {
							});
						}
					}
				}
			});
		}




		/*$('.btn-delete-emails').each(function() {
			$(this).on('click', function (e) {
				 binaryId = data.actionMode;
				 emailId = $(this).data('email');
				$.ajax({
							  url : '/controlpanel/emails/delete/' + binaryId ,  
							  headers: headersObj,
					type : 'DELETE'
				}).done(function(data) {
					window.location = '/controlpanel/emails/list';
				}).fail(function(error) {
					toastr.error( error.responseText,'');
				});
				
			});
		});*/

	

		$("#btn-email-cancel").on('click', function(e) {
			e.preventDefault();
			window.location = '/controlpanel/emails/list';
		});


		$("#btn-email-upload").on('click', function(e) {
			$("#btn-email-upload-file").click();
		});

		$("#submitUpdate").on('click', function(e) {
			e.preventDefault();
			submitForm($('#form_update_resource'), $('#form_update_resource').attr('action'), 'post');
		});

		$('#form-email').on('submit', function(e) {
			e.preventDefault();
			submitForm($('#form-email'), $('#email-save-action').val(), $('#email-save-method').val());
		})

		initTemplateElements();
		handleValidation();
	};


	function fetchExistingResources() {
		$.ajax({
			url: fetchResourcesURL + "?currentemailId=" + emailId,
			headers: headersObj,
			type: "GET",
			async: true,
			dataType: "json",
			success: function(response, status) {
				var resources = [];
				response.forEach(function(r) {
					resources.push({ "ids": r.id, "users": r.userId, "resources": r.fileName });
				})

				// TO-HTML
				if ($('#resources-div').attr('data-loaded') === 'true') {
					$('#resources > tbody').html("");
					$('#resources > tbody').append(mountableModel);
				}

				$('#resources').mounTable(resources, {
					model: '.resources-model',
					noDebug: false
				});
				$('#resources-div').removeClass('hide');
				$('#resources-div').attr('data-loaded', true);

				$('#modal-add-resources').modal('show');

			}
		});
	}

	function submitForm($form, action, method) {
		var noerrors = true;
		$('input,textarea,select:visible').filter('[required]').each(function(i, obj) {
			noerrors = noerrors && $('.form').validate().element(obj);
		});

		if ($("[name='file']").val() == "" && emailsCreateJson.actionMode == null) {
			noerrors = false;
			toastr.error(emailsCreateJson.messages.filerequired);
		}

		if ($('#checkboxDataSource')[0] && $('#checkboxDataSource')[0].checked) {
			$('#data-source-url').val($('#email-datasource').val());
		} else {
			$('#data-source-url').prop("disabled", true);
		}

		if (noerrors) {
			$form.attr('action', action + "?" + csrfParameter + "=" + csrfValue);
			$form.attr('method', method);
			toastr.success(messagesForms.validation.genFormSuccess, '');
			$form[0].submit();
		}
	}

	function fileName() {
		$('#fileName').removeClass('description');
		$('#fileName').text($("#btn-email-upload-file").prop('files')[0].name);
	}

	var handleValidation = function() {
		// for more info visit the official plugin documentation: 
		// http://docs.jquery.com/Plugins/Validation

		// set current language
		currentLanguage = currentLanguage || LANGUAGE;

		form1.validate({
			errorElement: 'span', //default input error message container
			errorClass: 'help-block help-block-error', // default input error message class
			focusInvalid: false, // do not focus the last invalid input
			ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// validation rules
			rules: {
				identification: { required: true, minlength: 5 },
				description: { required: true, minlength: 5 },
				fileName: { required: true }
			},
			messages: {
			},
			invalidHandler: function(event, validator) { //display error alert on form submit
				toastr.error(messagesForms.validation.genFormError, '');
			},
			errorPlacement: function(error, element) {
				if (element.is(':checkbox')) { error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if (element.is(':radio')) { error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list, .radio-inline")); }
				else { error.insertAfter(element); }
			},
			highlight: function(element) { // hightlight error inputs
				$(element).closest('.form-group').addClass('has-error');
			},
			unhighlight: function(element) { // revert the change done by hightlight
				$(element).closest('.form-group').removeClass('has-error');
			},
			success: function(label) {
				label.closest('.form-group').removeClass('has-error');
			},
			// ALL OK, THEN SUBMIT.
			submitHandler: function(form) {

			}
		});
	}

	// CLEAN FIELDS FORM
	var cleanFields = function(formId) {

		//CLEAR OUT THE VALIDATION ERRORS
		$('#' + formId).validate().resetForm();
		$('#' + formId).find('input:text, input:password, input:file, select, textarea').each(function() {
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if (!$(this).hasClass("no-remove")) { $(this).val(''); }
		});

		$('#fileName').html('');

		//CLEANING SELECTs
		$(".selectpicker").each(function() {
			$(this).val('');
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});

		// CLEANING CHECKS
		$('input:checkbox').removeAttr('checked');

		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		$('.tagsinput').prev().removeClass('tagsinput-has-error');
		$('.tagsinput').nextAll('span:first').addClass('hide');

		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}

	//INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(Data) {

		// Reset form
		$('#resetBtn').on('click', function() {
			cleanFields('form-email');
		});

		// Fields OnBlur validation

		$('input,textarea,select:visible').filter('[required]').bind('blur', function(ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});

		$('.selectpicker').filter('[required]').parent().on('blur', 'div', function(event) {
			if (event.currentTarget.getElementsByTagName('select')[0]) {
				$('.form').validate().element('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id'));
			}
		})

		$('.tagsinput').filter('[required]').parent().on('blur', 'input', function(event) {
			if ($(event.target).parent().next().val() !== '') {
				$(event.target).parent().next().nextAll('span:first').addClass('hide');
				$(event.target).parent().removeClass('tagsinput-has-error');
			} else {
				$(event.target).parent().next().nextAll('span:first').removeClass('hide');
				$(event.target).parent().addClass('tagsinput-has-error');
			}
		})

	}

	// Public API
	return {
		init: init,
		fileName: fileName,
	};
})();

$(document).ready(function() {

	Email.Create.init(emailsCreateJson);

});
