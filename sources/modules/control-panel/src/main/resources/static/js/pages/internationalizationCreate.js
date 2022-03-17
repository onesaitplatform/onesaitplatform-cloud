
var InternationalizationCreateController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var LIB_TITLE = 'Internationalization Controller';
	var logControl = 1;
	var currentLanguage = ''; // loaded from template.
	var reader = new FileReader();
	var myJsonLanguageEditor;
	var myJson;

	reader.onload = function(e) {
		$('#showedImgPreview').attr('src', e.target.result);

	}

	// REDIRECT URL
	var navigateUrl = function(url) {
		window.location.href = url;
	}
	
	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/internationalizations/freeResource/" + id).done(
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
	
	// DELETE INTERNATIONALIZATION
	var deleteInternationalizationConfirmation = function(internationalizationId) {
		console.log('deleteInternationalizationConfirmation() -> formId: ' + internationalizationId);
		// no Id no fun!
		if (!internationalizationId) {
			$.alert({
				title : 'Error',
				theme : 'light',
				content : 'NO INTERNATIONALIZATION-FORM SELECTED!'
			});
			return false;
		}

		logControl ? console
				.log('deleteInternationalizationConfirmation() -> formAction: '
						+ $('.delete-internationalization').attr('action') + ' ID: '
						+ $('.delete-internationalization').attr('userId')) : '';

		// call user Confirm at header.
		HeaderController.showConfirmDialogInternationalization('delete_internationalization_form');
	}



	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';

		var form1 = $('#internationalization_create_form');		

		form1
				.validate({
				  errorElement: 'span', //default input error message container
	            errorClass: 'help-block help-block-error', // default input error message class
	            focusInvalid: false, // do not focus the last invalid input
	            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
				lang: currentLanguage,
					// validation rules
					rules : {
						identification : {
							minlength : 5,							
							required : true
						},
						description : {
							minlength : 5,
							required : true
						}
					},
					invalidHandler : function(event, validator) { // display
						// error alert on form submit
						toastr.error(messagesForms.validation.genFormError,'');
					},
					errorPlacement : function(error, element) {
						if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
						else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }
						else 								{ error.insertAfter(element); }
					},
					highlight : function(element) { // hightlight error inputs
						$(element).closest('.form-group').addClass('has-error');
					},
					unhighlight : function(element) { // revert the change
						// done by hightlight
						$(element).closest('.form-group').removeClass(
								'has-error');
					},
					success : function(label) {
						label.closest('.form-group').removeClass('has-error');
					},
					// ALL OK, THEN SUBMIT.
					submitHandler : function(form) {
						var formAux;
		
						formAux = $('#internationalization_aux_create_form');
						$('#identification_aux').val($('#identification').val());
						$('#description_aux').val($('#description').val());
						$('#checkboxPublic_aux').val($('#checkboxPublic').prop('checked'));
						saveJson();
						$('#jsoni18n_aux').val(JSON.stringify(myJson));							
						
						formAux.attr("action", "?" + csrfParameter + "=" + csrfValue) 
						toastr.success(messagesForms.validation.genFormSuccess,'');
						formAux.submit();
					}
				});
	}

	var saveJson = function(){
		var tab = $("#langTabsUL li.active span").text();
		if(tab != null && tab != ""){
			var textAreaFile= myJsonLanguageEditor.getValue();
			myJson['languages'][tab]=JSON.parse(textAreaFile);
		}
		
		myJson["default"] = $("#languageCode").val();
	}
	
	var openTab = function(evt, tabName){
		saveJson();

		// Put JSON file in monaco editor
		var jsonFile = myJson.languages[tabName];
		if(tabName != "" && jsonFile != null){
			printJson(JSON.stringify(jsonFile));
		}
		
		// Put the language name in text input
		var lang = document.getElementById("languageTabCode");
		lang.value = $("#languageCode option").filter(function(a){return this.value==tabName})[0].text;	
	}

	var printJson = function(text) {
		myJsonLanguageEditor.setValue(text);
		beautifyJson();
	};
	
	var beautifyJson = function() {
		myJsonLanguageEditor.getAction('editor.action.formatDocument').run();
	};

	var downloadJsonFile = function() {
		var tab = $("#langTabsUL li.active span").text();
		if(tab != "" && tab != null) {
			var jsonFile = myJsonLanguageEditor.getValue();
			if(jsonFile != null && jsonFile != ""){
				var element = document.createElement('a');
				element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(jsonFile));
				element.setAttribute('download', tab + "_language.json");
	
				element.style.display = 'none';
				document.body.appendChild(element);
				
				element.click();
				document.body.removeChild(element);
			} else {
				alert("The file is empty.");
			}
		} else {
			alert("Please, select a language!");
		}
	}
	
	var loadJsonFromDoc = function(files) {
		if(files == null){
			alert("Please, select a file!");
		} else {
			var reader = new FileReader();
			var size = files[0].size;
			var chunk_size = Math.pow(2, 13);
			var chunks = [];
			var offset = 0;
			var bytes = 0;
			var selectedTab = $("#langTabsUL li.active span").text();
			if(selectedTab == ""){
				alert("Please, select a language!");
			} else {
				if (files[0].type == "application/json") {
		
					reader.onloadend = function(e) {
		
						if (e.target.readyState == FileReader.DONE) {
							var chunk = e.target.result;
							bytes += chunk.length;
							chunks.push(chunk);
							if (offset < size) {
								offset += chunk_size;
								var blob = files[0].slice(offset, offset + chunk_size);
								reader.readAsText(blob);
							} else {
								var content = chunks.join("");
								var jsonData;
								try {
									jsonData = JSON.parse(content);
									var fileLoaded = jsonData;
								} catch (err) {
									try {
										jsonData = content.replace(/[\r]/g, '');
										var arrayJson = [];
										var dataSplitted = jsonData.split("\n");
										var i;
										for (i in dataSplitted) {
											if (dataSplitted[i] != "") {
												arrayJson.push(JSON
														.parse(dataSplitted[i]));
											}
										}
										fileLoaded = arrayJson;
									} catch (err) {
		
										$('#response').text(err);
										$('#returnAction').modal("show");
										return;
									}
								}
								var myJsonFile = JSON.stringify(fileLoaded);
								printJson(myJsonFile);
							}
							progressBarFileUpload(offset, size);
						}
					}
				}
			
				var blob = files[0].slice(offset, offset + chunk_size);
				reader.readAsText(blob);
				$('#progressBarModal').modal("show");
			}
		}	
	};

	var progressBarFileUpload = function(offset, maxSize) {
		var percent = (offset / maxSize) * 100;
		$('#importProgress').removeClass('progress-bar-success');
		$('#importProgress').removeClass('progress-bar-danger');
		if (offset < maxSize) {
			$('#importProgress').attr('aria-valuenow', percent + '%').css(
					'width', percent + '%');
			$('#importProgress').text(percent.toFixed(2) + '%');
		} else {
			$('#importProgress').attr('aria-valuenow', '100%').css('width',
					'100%');
			$('#importProgress').text('100%');
			$('#importProgress').addClass('progress-bar-success');
			$('#progressBarModal').modal('hide')
		}

	}
	
	
	// Add tabs after select the language
	$(".nav-pills").on("click", "a", function(e){
	      e.preventDefault();
	      $(this).tab('show');
    });
	
    $('.add-language').click(function(e) {
        e.preventDefault();
        showAddLangDialog();
	});
	
    var hideLanguageOptions = function(){
    	var codes = document.getElementById("languageCode");
		for(var i=0; i<codes.options.length; i++){ 
			if(codes.options[i].style.display == "none"){
				codes.options[i].removeAttribute("style");
			} else{
				codes.options[i].setAttribute("style","display: none;")
			}
		}
    }
    
    var showAddLangDialog = function(){
		hideLanguageOptions();
		$.confirm({
			title: addLanguageTitle,
			theme: 'light',
			columnClass: 'medium',
			content: '<select id="popuplangselector" >' + $("#languageCode").html() + '</select>',
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: cancelBtn,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} // GENERIC CLOSE.
				},
				Ok: {
					text: confirmBtn,
					btnClass: 'btn btn-primary',
					action: function() {
						if($("#popuplangselector").val()=="" || !$("#popuplangselector").val()){
							console.log("language empty");
							return;
						}
						var langName = $("#popuplangselector").val();
						var tabs = $("#langTabsUL li span").map(function(){
				    		return this.textContent}).get();
				        for(var i=0;i<tabs.length;i++){
				            if(langName == tabs[i]){
				            	console.log("language exists");
				            	return;
				            }
				        }
						$('.add-language').closest('li').before('<li class="langTabs" id="'+langName+'" onclick="InternationalizationCreateController.openTab(event,\''+langName+'\')" ><a href="#idiomtab_'+langName+'" data-toggle="tab" aria-expanded="true" ><span>'+langName+'</span></a></li>');
						// Add language to JSON
						myJson['languages'][langName]={"_comment": "Example JSON", "_internationalizationTitle":"Temperatura", "_gadgetTitle":"Máximas"};
						beautifyJson();
						if(tabs.length==0){
							$("#languageCode").val(langName);
						}
						hideSelectOptions();
					}											
				}						
			}
		});
	}
    
    var hideSelectOptions = function() {
    	var codes = document.getElementById("languageCode");
		for(var i=0; i<codes.options.length; i++){ 
			codes.options[i].removeAttribute("style");
		}
    	var tabs = $("#langTabsUL li span").map(function(){
    		return this.textContent}).get();
    	
    	var str='[value!=""]';
        for(i=0;i<tabs.length;i++){
            str += '[value!='+tabs[i]+']';
        }
        $('#languageCode option'+ str).hide();
    }
    
    // Monaco Text Area
	var handleLanguageEditor = function() {
			myTextArea = document.getElementById('jsoni18n');
			myJsonLanguageEditor = monaco.editor.create(myTextArea, {
				value : "{/*Example\n \"internationalizationTitle\":\"Temperatura\",\n \"gadgetTitle\":\"Máximas\" */\n}",
				language : 'json',
				readOnly : false,
				scrollBeyondLastLine : false,
				theme : "vs-dark",
				automaticLayout : true
			});
			createTabsWithJson();		
	};
	
    // Add tabs after read JSON file
	var createTabsWithJson = function() {
		var jsonFile = $("#jsoni18n_aux").val();
		if(jsonFile != null && jsonFile != ""){
			myJson = JSON.parse(jsonFile);
			var langCodes = Object.keys(myJson.languages);
			var i;
			
			for(i = 0; i < langCodes.length; i++){
				$('.add-language').closest('li').before('<li class="langTabs" id="'+langCodes[i]+'" onclick="InternationalizationCreateController.openTab(event,\''+langCodes[i]+'\')"><a href="#idiomtab_'+langCodes[i]+'" data-toggle="tab" aria-expanded="false"><span>'+langCodes[i]+'</span></a></li>');
			}
			
			var defaultLang = myJson.default;
			if(defaultLang != "") {
				$("#languageCode").val(defaultLang);
				hideSelectOptions();
				selectDefLanguage();
			} else {
				$("#languageCode").val("");
				hideSelectOptions();
			}
			
		} else {
			var text = '{"languages":{}, "default":null}';
			myJson = JSON.parse(text);
			//
			$("#languageCode").val("");
			//
			hideSelectOptions();
		}
	}
	
	var deleteTab = function() {
		$.confirm({
			title: removeLanguageTitle,
			theme: 'light',
			columnClass: 'medium',
			content: removeLanguageConfirm,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: cancelBtn,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} // GENERIC CLOSE.
				},
				Ok: {
					text: deleteBtn,
					btnClass: 'btn btn-primary',
					action: function() {
						var selectedTab = $("#langTabsUL li.active span").text();
						if(selectedTab == ""){
							alert("Please, select a language!");
						} else{
							delete myJson['languages'][selectedTab];
							$("#langTabsUL li.active").remove();
						}
					}											
				}						
			}
		});		 
	}
	
	var selectDefLanguage = function() {
		$("li#"+ myJson["default"] +".langTabs").click();
		$("li#"+ myJson["default"] +".langTabs").addClass("active");
	}
	
	var codemirrorReadOnly = function() {
		myJsonLanguageEditor.updateOptions({ readOnly: true});
	}
	
	var codemirrorEdit = function() {
		myJsonLanguageEditor.updateOptions({ readOnly: false});
	}


	var initTemplateElements = function(){
			
		$('input').filter('[required]').bind('blur', function (ev) { // fires on every blur				
				$('#internationalization_create_form').validate().element('#' + event.target.id);                // checks form for validity
		});
	}



	// CONTROLLER PUBLIC FUNCTIONS
	return {
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
		},
		getCodeMirror : function() {
			return myCodeMirror;
		},

		// INIT() CONTROLLER INIT CALLS
		init : function() {
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleLanguageEditor();
			
			// INPUT MASK FOR ontology identification allow only letters, numbers and -_
			$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
			
			/* EDITION MODE */
			/* Hide dimensions */

			if (!$("[name='id']").val()) {
				$("#dimensionsPanel").hide();
			}
			handleValidation();
			initTemplateElements();
		},

		// REDIRECT
		go : function(url) {
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			navigateUrl(url);
		},
		cancel: function(id,url){
			logControl ? console.log(LIB_TITLE + ': cancel()') : '';			
			freeResource(id,url);
		},
		loadJsonFromDoc : function(files) {
			loadJsonFromDoc(files);
		},
		printJson : function(){
			printJson();
		},
		beautifyJson : function(){
			beautifyJson();
		},
		openTab : function(evt, tabName){
			openTab(evt, tabName);
		},
		deleteTab : function(){
			deleteTab();
		},
		downloadJsonFile : function(){
			downloadJsonFile();
		},
		codemirrorReadOnly : function(){
			codemirrorReadOnly();
		},
		codemirrorEdit : function(){
			codemirrorEdit();
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
$(window).load(function() {	
	
	// AUTO INIT CONTROLLER.
	InternationalizationCreateController.init();
});
