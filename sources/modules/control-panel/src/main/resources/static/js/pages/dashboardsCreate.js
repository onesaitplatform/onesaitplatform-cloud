var authorizationsArr 		= []; // add authorizations
var authorizationUpdateArr  = []; // get authorizations of the ontology
var authorizationsIds 		= []; // get authorizations ids for actions
var authorizationObj 		= {}; // object to receive authorizations responses.

var DashboardsCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Dashboard Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var mountableModel2 = $('#dashboard_autthorizations').find('tr.authorization-model')[0].outerHTML;
	var reader = new FileReader();
	
	reader.onload = function (e) {
        $('#showedImgPreview').attr('src', e.target.result);
       
    }
	// CONTROLLER PRIVATE FUNCTIONS	
	
	// REDIRECT URL
	var navigateUrl = function(url) {
		window.location.href = url;
	}
	
	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/dashboards/freeResource/" + id).done(
				function(data){
					console.log('freeResource() -> ok');
					navigateUrl(url); 
				}
			).fail(
				function(e){
					console.error("Error freeResource", e);
					navigateUrl(url); 
				}
			)		
	}
	
	$('.selectpicker').selectpicker({iconBase: 'fa', tickIcon: 'fa-check'});
	
	// DELETE DASHBOARD
	var deleteDashboardConfirmation = function(dashboardId){
		console.log('deleteDashoardConfirmation() -> formId: '+ dashboardId);		
		// no Id no fun!
		if ( !dashboardId ) {
			toastr.error('NO DASHBOARD-FORM SELECTED!','');
			}
		
		logControl ? console.log('deleteDashboardConfirmation() -> formAction: ' + $('.delete-dashboard').attr('action') + ' ID: ' + $('.delete-dashboard').attr('userId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogDashboard('delete_dashboard_form');
	}
	
	
	// return position to find authId.
	var foundIndex = function(what,item,arr){
		var found = '';
		arr.forEach(function(element, index, array) {
			if ( what === element[item]){ found = index;  console.log("a[" + index + "] = " + element[item] + ' Founded in position: ' + found ); } 
			
		});		
		return found;
	}
	
	
	
	var authorization = function(action,user,description,accesstype,btn){
		
		logControl ? console.log('|---> authorization()') : '';	
		
		var response = {};
		
		if (action === 'insert'){	
			var propAuth = {"users":user,"description":description,"accesstypes": accesstype};
			
			authorizationsArr.push(propAuth);	
			// TO-HTML
			if ($('#authorizations').attr('data-loaded') === 'true'){
				$('#dashboard_autthorizations > tbody').html("");
				$('#dashboard_autthorizations > tbody').append(mountableModel2);
			}
			console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
			$('#dashboard_autthorizations').mounTable(authorizationsArr,{
				model: '.authorization-model',
				noDebug: false							
			});
			
			// hide info , disable user and show table
			$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));			
			$("#users").selectpicker('deselectAll');
			$("#users option[value='" + user + "']").prop('disabled', true);
			$("#users").selectpicker('refresh');
			$('#authorizations').removeClass('hide');
			$('#authorizations').attr('data-loaded',true);
		}
	
		if (action  === 'delete'){
			
			authorizationsArr.splice(user, 1);
			// refresh interface				
			
				$(btn).closest('tr').remove();
				$("#users option[value='" + user + "']").prop('disabled', false);						
				$("#users").selectpicker('deselectAll');
				$("#users").selectpicker('refresh');
				if (authorizationsArr.length == 0){
					if(!$('#alert-authorizations').is(':visible')){
						$('#alert-authorizations').show();
					}		
					$('#authorizations').addClass('hide');
					
				}	
		}	
	};
	
	var initAccess = function (){
		
		//authorizationsArr
		var authoriz = JSON.parse(dashboardCreateReg.authorizationsSaved);
		var users = dashboardCreateReg.users;
		if(authoriz!=null){
			for (var i = 0; i < authoriz.length; i++) {
				for (var j = 0; j < users.length; j++) {
					if(authoriz[i].users===users[j].userId){
						authoriz[i].description=users[j].userId;
						break;
					}				
				}			
			}
			
			for (var i = 0; i < authoriz.length; i++) {
				authorization('insert',authoriz[i].users,authoriz[i].description,authoriz[i].accesstypes,'');			
			}	
		}
	}
	
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#dashboard_create_form');
       
		
					
		// set current language
		currentLanguage = dashboardCreateReg.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,			
			// validation rules
            rules: {				
                identification:	{ minlength: 5, required: true },
				description:	{ minlength: 5, required: true }
            },
            invalidHandler: function(event, validator) { //display error alert on form submit  

            	toastr.error(messagesForms.validation.genFormError,'');
            },
            errorPlacement: function(error, element) {				
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }
				else if ( element.is(':hidden'))	{ 
					if ($('#datamodelid').val() === '') { $('#datamodelError').removeClass('hide');} 					
				}				
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
	      	
            	if(($('#categories_select').val() != undefined && $('#categories_select').val()!="") &&($('#subcategories').val() == undefined && $('#subcategories').val()=="") ){
            		
					toastr.error(dashboardCreateReg.subcategory,'');
            		return;
            	}else if(($('#categories_select').val() == undefined || $('#categories_select').val()=="") &&($('#subcategories').val() == undefined || $('#subcategories').val()=="")){
            		
            		var formAux = $('#dashboard_aux_create_form');           
            	    $('#category').val(null);
            	    $('#subcategory').val(null);
            	    $('#identification_aux').val($('#identification').val());
            	    $('#dashboardConfId_aux').val($('#dashboardConfId').val());
            	    $('#description_aux').val( $('#description').val());             	    
            	    $('#authorizations_aux').val(JSON.stringify(tableToObj( document.getElementById('dashboard_autthorizations') )));            	
            	    $('#checkboxPublic_aux').val( $('#checkboxPublic').prop('checked'));  
            	    $('#headerlibs_aux').val( myCodeMirror.getValue());
            	    $('#i18n_aux').val($('#i18n').val());
            	   
            	    $( "#image_aux" ).remove();
            	    var x = $("#image"),
            	      y = x.clone();
	            	  x.attr("id", "image_aux");
	            	  x.attr("name", "image");
	            	  x.attr("class", "hide");
	            	  y.insertAfter("#checkboxPublic_aux");
            	    
	            	formAux.attr("action", "?" + csrfParameter + "=" + csrfValue)
            	    toastr.success(messagesForms.validation.genFormSuccess,'');	
				
					formAux.submit();		
            	}else{
            	    var formAux = $('#dashboard_aux_create_form');           
            	    $('#category').val($('#categories_select').val());
            	    $('#subcategory').val($('#subcategories').val());
            	    $('#identification_aux').val($('#identification').val());
            	    $('#dashboardConfId_aux').val($('#dashboardConfId').val());
            	    $('#description_aux').val( $('#description').val());             	    
            	    $('#authorizations_aux').val(JSON.stringify(tableToObj( document.getElementById('dashboard_autthorizations') )));            	
            	    $('#checkboxPublic_aux').val( $('#checkboxPublic').prop('checked')); 
            	    $('#headerlibs_aux').val( myCodeMirror.getValue());
            	    $('#i18n_aux').val($('#i18n').val());
            	    $( "#image_aux" ).remove();
            	    var x = $("#image"),
            	      y = x.clone();
	            	  x.attr("id", "image_aux");
	            	  x.attr("name", "image");
	            	  x.attr("class", "hide");
	            	  y.insertAfter("#checkboxPublic_aux");
            	    
	            	formAux.attr("action", "?" + csrfParameter + "=" + csrfValue)
            	    toastr.success(messagesForms.validation.genFormSuccess,'');	
		
					formAux.submit();	
            	}
			}
        });
    }
	
	var tableToObj = function( table ) {		
	    var trs = table.rows,
	        trl = trs.length,
	        i = 0,
	        j = 0,
	        keys = ["users","description","accesstypes"],
	        obj, ret = [];

	    for (; i < trl; i++) {
	        if (i > 0) {
	        	 if(trs[i].children.length>0){
		            obj = {};
		           
		            for (j = 0; j < trs[i].children.length-1; j++) {
		                obj[keys[j]] = trs[i].children[j].children[0].value;
		            }
		            ret.push(obj);
	        	 }
	        }
	    }
	    //validation
	    
	    for (var k = ret.length-1; k >=0 ; k--) {
	    	if(ret[k].users === null || ret[k].users.length===0){
	    		ret.splice(k, 1);
	    	}
	    }
	    return ret;
	};
	
	
	var initTemplateElements = function(){
		
	$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});			
		
	// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('dashboard_create_form');
		});		
		
	}
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		$('.tagsinput').prev().removeClass('tagsinput-has-error');
		$('.tagsinput').nextAll('span:first').addClass('hide');

		// CLEANING CODEMIRROR
		if ($('.CodeMirror')[0].CodeMirror){
			var editor = $('.CodeMirror')[0].CodeMirror;
			editor.setValue('');
			$('.CodeMirror').removeClass('editor-has-error');
			$('.CodeMirror').nextAll('span:first').addClass('hide');
		}
		
		
		//CLEANING AUTHORIZATION
		$('#dashboard_autthorizations > tbody > tr > td > button').map(function (obj){
				var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();					
				authorization('delete', selUser, '','', obj );
		})
		
		//CLEANING IMAGE
		$('#image').val('');
		$('#showedImgPreview').attr('src', '/controlpanel/img/APPLICATION.png');
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}
	
	 function validateImgSize() {
	        if ($('#image').prop('files') && $('#image').prop('files')[0].size>60*1024){
	        	showGenericErrorDialog('Error', marketAssetCreateReg.marketAssetmanager_image_error);
	        	$("#image").val(null);
	        	$('#showedImg').val("");
	         } else if ($('#image').prop('files')) {
	        	 reader.readAsDataURL($("#image").prop('files')[0]);	        	 
	         }
	    }
	
	// INIT CODEMIRROR
		
			logControl ? console.log('handleCodeMirror() on -> headerlibs') : '';
	        var myTextArea = document.getElementById('headerlibs');
	        myCodeMirror = CodeMirror.fromTextArea(myTextArea, {
	        	mode: "code",
	            lineNumbers: true,
	            foldGutter: true,
	            matchBrackets: true,
	            styleActiveLine: true,
	            theme:"material"
	        })	        
			myCodeMirror.setSize("100%", 200);
	   
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{	
		getCodeMirror:function(){
			return myCodeMirror;
		},
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return dashboardCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			// INPUT MASK FOR ontology identification allow only letters, numbers and -_
			$("#identification").inputmask({ regex: "[a-zA-Z 0-9_-]*", greedy: false });
			
			/*EDITION MODE*/
			/*Hide dimensions*/
			
			if(!$("[name='id']").val()){
				$("#dimensionsPanel").hide();
			}
			handleValidation();
			
		
			initAccess();
			initTemplateElements();
		
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		cancel: function(id,url){
			logControl ? console.log(LIB_TITLE + ': cancel()') : '';	
			freeResource(id,url); 
		},
		
		getFieldsFromQueryResult: function (jsonString){
			var fields = [];
			function iterate(obj, stack) {
		        for (var property in obj) {
		            if (obj.hasOwnProperty(property)) {
		                if (typeof obj[property] == "object") {
		                    iterate(obj[property], stack + (stack==""?'':'.') + property);
		                } else {
		                    fields.push(stack + (stack==""?'':'.') + property);
		                }
		            }
		        }
		        
		        return fields;
		    }
	
			return iterate(JSON.parse(jsonString), '');
		},// INSERT AUTHORIZATION
		insertAuthorization: function(){
			logControl ? console.log(LIB_TITLE + ': insertAuthorization()') : '';
			
				// UPDATE MODE ONLY AND VALUES on user and accesstype
			if (($('#users').val() !== '') && ($("#users option:selected").attr('disabled') !== 'disabled') && ($('#accesstypes').val() !== '')){
					
					// AJAX INSERT (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns object with data.
					authorization('insert',$('#users').val(),$('#users')[0].selectedOptions[0].text,$('#accesstypes').val(),'');
								
				} else { 
					
					toastr.error(dashboardCreateReg.validations.authuser,''); 
				}
			
		},
		
		// REMOVE authorization
		removeAuthorization: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeAuthorization()') : '';
				// AJAX REMOVE (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns object with data.
				var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();					
				authorization('delete', selUser, '','', obj );				
		
		},		
		
		// DELETE GADGET DATASOURCE 
		deleteDashboard: function(dashboardId){
			logControl ? console.log(LIB_TITLE + ': deleteDashboard()') : '';	
			deleteDashboardConfirmation(dashboardId);			
		},
		// VALIDATE IMAGE SIZE
		validateImageSize: function() {
			logControl ? console.log(LIB_TITLE + ': validateImgSize()') : '';
			validateImgSize();
		},
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	DashboardsCreateController.load(dashboardCreateJson);	
		
	// AUTO INIT CONTROLLER.
	DashboardsCreateController.init();
});
