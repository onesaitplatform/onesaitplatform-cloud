var UserCreateController = function() {
	
	var csrf = {};
	csrf[headerJson.csrfHeaderName] = headerJson.csrfToken;
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Menu Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.
	var currentFormat = '' // date format depends on currentLanguage.
	var internalFormat = 'yyyy/mm/dd';
	var internalLanguage = 'en';
	
	// CONTROLLER PRIVATE FUNCTIONS	

	
	// REDIRECT URL
	var navigateUrl = function(url){ window.location.href = url; }
	
		
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		logControl ? console.log('cleanFields() -> ') : '';
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remote" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}
	
	// CHECK DATES AND LET THE FORM SUBMMIT
	var checkCreate = function(){
		logControl ? console.log('checkCreate() -> ') : '';
        
		var dateCreated = $("#datecreated").datepicker('getDate');
        var dateDeleted = $("#datedeleted").datepicker('getDate');
		
		var diff = dateDeleted - dateCreated;
		var days = diff / 1000 / 60 / 60 / 24;
				
		logControl ?  console.log('created: ' + dateCreated + '  deleted: ' + dateDeleted): '';		
		
        if (dateDeleted != ""){
            if (dateCreated > dateDeleted){
                $.confirm({icon: 'fa fa-warning', title: 'CONFIRM:', theme: 'dark',
					content: userCreateReg.validation_dates,
					draggable: true,
					dragWindowGap: 100,
					backgroundDismiss: true,
					closeIcon: true,
					buttons: {				
						close: { text: userCreateReg.Close, btnClass: 'btn btn-sm btn-default btn-outline', action: function (){} //GENERIC CLOSE.		
						}
					}
				});
                $("#datedeleted").datepicker('update','');
            }			           
        }
    } 
	
	
	// FORMATDATES: format date to DDBB standard 'yyyy/mm/dd';
	var formatDates = function(dates){
		
		var dateUnformatted = '';
		var dateFormatted	= '';
		
		// no dates no fun!
		if (!dates) { return false;}
		
		// if current language is en , dates are in DDBB format so OK
		if (currentLanguage == 'en') { return true; }
		
		// change all dates to internal format
		logControl ? console.log('formatDates() -> ' + dates + ' with CurrentLanguage: ' + currentLanguage) : '';
		
		
		$(dates).each(function( index, dateInput ) {		  
			if ( $(dateInput).val() ){				
				
				// ES
				if (currentLanguage === 'es'){
					// change date es to en [ dd/mm/yyyy to yyyy/mm/dd ]
					dateUnformatted = $(dateInput).val();
					dateFormatted = dateUnformatted.split("/")[2] + '/' + dateUnformatted.split("/")[1] + '/' + dateUnformatted.split("/")[0];					
					$(dateInput).val(dateFormatted);
					logControl ? console.log('FormatDate -> ' + $(dateInput).attr('id') + ' current:' + dateUnformatted + ' formatted: ' + $(dateInput).val()) : '';					
				}
				// more languages to come...				
			}		  
		});

		// all formatted then true;
		return true;
		
	}
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#user_create_form');
        var error1 = $('.alert-danger');
		
		// set current language
		currentLanguage = userCreateReg.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not(.selectpicker)", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// custom messages
            messages: {
					datedeleted: { checkdates : userCreateReg.validation_dates }
			},
			// validation rules
            rules: {
				userId:				{ required: true, minlength: 4 },
                fullName:			{ required: true},
                email:				{ required: true, email: true },
                newpasswordbox:		{ required: true, minlength: 7, maxlength: 128 },
                repeatpasswordbox:	{ required: true, minlength: 7, maxlength: 128,  equalTo : "#newpasswordbox" }, 
                roles:				{ required: true },
				datecreated:		{ date: true, required: true },
				datedeleted:        { date: true }
            },
            invalidHandler: function(event, validator) { //display error alert on form submit              
            	
                error1.show();
                App.scrollTo(error1, -200);
                
            },
            errorPlacement: function(error, element) {
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }
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
                error1.hide();
                
				// date conversion to DDBB format.
				if ( formatDates('#datecreated,#datedeleted') ) { 
					 //comprobar contraseñas
	                if($('#tab-dataUser').hasClass('active') && userCreateJson.roleType == 'ROLE_ADMINISTRATOR' && userCreateJson.actionMode == null){
	                	$('#tab-password a').click();
	                	if($('#newpasswordbox').val()!="" && $('#repeatpasswordbox').val()!="" && $('#repeatpasswordbox').val() == $('#newpasswordbox').val() ){
	                		$('#createBtn').submit();
	                	}
	                
	                }else{
	                	if ($('#datedeleted').val()=="") {
	                		$('#datedeleted').prop('disabled',true);
	                	}
	                	form.submit();
	                }
					
				} 
				else { 
					error1.show();
					App.scrollTo(error1, -200);
				}				
            }
        });
    }
	
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> selectpickers, datepickers, resetForm, today->dateCreated currentLanguage: ' + currentLanguage) : '';
		
		// selectpicker validate fix when handleValidation()
		$('.selectpicker').on('change', function () {
			$(this).valid();
		});
		
		$('#newpasswordbox, #repeatpasswordbox').on('keyup', function (){
			
			if($ ('#newpasswordbox').val() == $('#repeatpasswordbox').val() ){
				$('#passwordmsg').html(userCreateReg.succespasswordmsg).css('color','green');
			}else{
				$('#passwordmsg').html(userCreateReg.errorpasswordmsg).css('color','red');
			}
		});
		
				
		// set current language and formats
		currentLanguage = userCreateReg.language || LANGUAGE[0];
		currentFormat = (currentLanguage == 'es') ? 'dd/mm/yyyy' : 'mm/dd/yyyy';		
		
		logControl ? console.log('|---> datepickers currentLanguage: ' + currentLanguage) : '';
		
		// init datepickers dateCreated and dateDeleted		
		$("#datecreated").datepicker({dateFormat: currentFormat, showButtonPanel: true,  orientation: "bottom auto", todayHighlight: true, todayBtn: "linked", clearBtn: true, language: currentLanguage});
        
		var dd = $("#datedeleted").datepicker({dateFormat: currentFormat, showButtonPanel: true,  orientation: "bottom auto", todayHighlight: true, todayBtn: "linked", clearBtn: true, language: currentLanguage});
		
		// setting on changeDate to checkDates()
		dd.on('changeDate', function(e){				
			selectedDate = dd.data('datepicker').getFormattedDate(currentFormat);				
			checkCreate();
		});
		
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('user_create_form');
		});
		
		
		// INSERT MODE ACTIONS  (userCreateReg.actionMode = NULL ) 
		if ( userCreateReg.actionMode === null){
			logControl ? console.log('action-mode: INSERT') : '';
			
			//set TODAY to dateCreated depends on language
			var f = new Date();         
			today = (currentLanguage == 'es') ? ('0' + (f.getDate())).slice(-2) + "/" + ('0' + (f.getMonth()+1)).slice(-2) + "/" + f.getFullYear() : ('0' + (f.getMonth()+1)).slice(-2) + "/" + ('0' + (f.getDate())).slice(-2) + "/" + f.getFullYear();
			$('#datecreated').datepicker('update',today);
			
			// Set active 
			//$('#checkboxactive').trigger('click');
			
			// set date deleted to null 
			 $('#datedeleted').datepicker('update',null);
		}
		// EDIT MODE ACTION 
		else {
			// set DATE created in EDIT MODE
			logControl ? console.log('action-mode: UPDATE') : '';
			$('#deleteBtn').hide();
			var f = new Date(userCreateReg.dateCreated);
			regDate = (currentLanguage == 'es') ? ('0' + (f.getDate())).slice(-2) + "/" + ('0' + (f.getMonth()+1)).slice(-2) + "/" + f.getFullYear() : ('0' + (f.getMonth()+1)).slice(-2) + "/" + ('0' + (f.getDate())).slice(-2) + "/" + f.getFullYear();
			$('#datecreated').datepicker('update',regDate);
			
			// set DATE deleted in EDIT MODE if exists
			if ( userCreateReg.dateDeleted !== null ) {
				console.log('entra?');
				var d = new Date(userCreateReg.dateDeleted);
				regDateDel = (currentLanguage == 'es') ? ('0' + (d.getDate())).slice(-2) + "/" + ('0' + (d.getMonth()+1)).slice(-2) + "/" + d.getFullYear() : ('0' + (d.getMonth()+1)).slice(-2) + "/" + ('0' + (d.getDate())).slice(-2) + "/" + d.getFullYear();
				$('#datedeleted').datepicker('update',regDateDel);
			}else { $('#datedeleted').datepicker('update',null); }			
			
			// if user deleted (active=false, and dateDeleted=date) active=true -> set datadeleted to null.
			$('#checkboxactive').on('click', function(){					
					if (( $('#datedeleted').val() != '' )&&( $(this).is(":checked") )) { $('#datedeleted').datepicker('update',null); $('#datedeleted').prop('disabled',true); }
					console.log('checked in update with datedeleted: ' + $('#datedeleted').val());				
			});
			
		}
		
	}	
	
	// DEACTIVATE USER
	var deleteUserConfirmation = function(userId){
		console.log('deleteUserConfirmation() -> formId: '+ userId);
		
		// no Id no fun!
		if ( !userId ) {$.alert({title: 'ERROR!', theme: 'light', content: 'NO USER-FORM SELECTED!'}); return false; }
		
		console.log('deleteUserConfirmation() -> ID: ' + userId);
		
		// i18 labels
		var Close = headerReg.btnCancelar;
		var Title = headerReg.titleConfirm + ':';
		
		// call user Confirm at header.
		$.confirm({
			icon: 'fa fa-warning',
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: userCreateJson.deactivateText,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			closeIcon: true,
			buttons: {
				remove: {
					text: userCreateJson.deactivateTitle,
					btnClass: 'btn btn-sm btn-circle btn-primary btn-outline',
					action: function(){ 
						navigateUrl("/controlpanel/users/forgetDataUser/" +userId+"/true");
					}											
				},
				close: {
					text: Close,
					btnClass: 'btn btn-sm btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				}
			}
		});		
	}
	
	//HARD DELETE USER
	var hardDeleteUserConfirmation = function(userId) {
		
		var Close = headerReg.btnCancelar;
		
		$.confirm({
			icon: 'fa fa-warning',
			title: userCreateJson.deleteTitle,
			theme: 'light',			
			columnClass: 'medium',
			content: userCreateJson.deleteText,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			closeIcon: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: userCreateJson.deleteTitle,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){
						$.ajax({
							url : "/controlpanel/users/hardDelete/"+userId,
							type : "DELETE",
							headers: csrf,
							success : function(response){
								navigateUrl("/controlpanel/users/list");
							},
						    error :  function () {
						    	$.alert({
									title : 'ERROR!',
									type : 'red',
									theme : 'light',
									content :  userCreateJson.deleteError
								});
						    }
						})
					}
				}
			}
		});
	}
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return userCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';			
			handleValidation();
			initTemplateElements();		
			
		},
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		// DEACTIVATE USER 
		deleteUser: function(userId){
			logControl ? console.log(LIB_TITLE + ': deleteUser()') : '';	
			deleteUserConfirmation(userId);			
		},
		// HARD DELETE USER 
		hardDeleteUser: function(userId){
			logControl ? console.log(LIB_TITLE + ': hardDeleteUser()') : '';	
			hardDeleteUserConfirmation(userId);			
		}
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	UserCreateController.load(userCreateJson);	
		
	// AUTO INIT CONTROLLER.
	UserCreateController.init();
});
