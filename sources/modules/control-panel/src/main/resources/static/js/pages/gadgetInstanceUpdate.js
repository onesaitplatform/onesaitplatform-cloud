var GadgetsTemplateCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Gadget Template Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';
	var myVSHTML;
	var myVSHTML_isfullscreen;
	var myVSJS;
	var myVSJS_isfullscreen;
	var myVSHL;
	var timerWrite;
	var languaje= gadgetTemplateInit.type;
	var paramMap;
	var isEdit = window.location.href.indexOf("/gadgettemplates/update/") != -1;
	var currentDatasource;
	var parameters=[];
	// CONTROLLER PRIVATE FUNCTIONS
	
	var navigateUrl = function(url){ window.location.href = url; }
	
	
	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/gadgettemplates/freeResource/" + id).done(
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
	
	
	
	// DELETE TEMPLATE
	var deleteGadgetTemplateConfirmation = function(gadgetTemplateId){
		console.log('deleteGadgetConfirmation() -> formId: '+ gadgetTemplateId);
		
		// no Id no fun!
		if ( !gadgetTemplateId ) {toastr.error('NO GATGET TEMPLATE SELECTED!',''); return false; }
		
		logControl ? console.log('deleteGadgetTemplateConfirmation() -> formAction: ' + $('.delete-gadget').attr('action') + ' ID: ' + $('.delete-gadget').attr('userId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogGadgetTemplate('delete_gadget_template_form');	
	}
	
	// DELETE TEMPLATE
	var deleteInstanceConfirmation = function(gadgetTemplateId){
		console.log('deleteInstanceConfirmation() -> formId: '+ gadgetTemplateId);
		
		// no Id no fun!
		if ( !gadgetTemplateId ) {toastr.error('NO INSTANCE SELECTED!',''); return false; }
		
		logControl ? console.log('deleteInstanceConfirmation() -> formAction: ' + $('.delete-gadget').attr('action') + ' ID: ' + $('.delete-gadget').attr('userId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogInstance('delete_gadget_form');	
	}
	
	deleteInstanceConfirmation
	
	
	
	// INIT CODEMIRROR
	var handleVS = function () {
		logControl ? console.log('handleCodeMirror() on -> templateCode') : '';	

		var myTextAreaHL = document.getElementById('headerlibsCode');
        var myTextArea = document.getElementById('templateCode');
        var myTextAreaJS = document.getElementById('templateCodeJS');

        var hlelement = document.getElementById('hlcode');
        var htmlelement = document.getElementById('htmlcode');
        var jselement = document.getElementById('jscode');
        
        myVSHTML = monaco.editor.create(htmlelement, {
    		value: myTextArea.value,
    		language: 'html',
    		readOnly: myTextArea.disabled,
    		scrollBeyondLastLine: false,
    		theme: "vs-dark",
    		automaticLayout: true
    	});
        
        var allowDrop = function(ev) {
    	  ev.preventDefault();
    	}
      	
      	var dropParam = function(e,vsinstance){
      		e.preventDefault();
      		var data = e.dataTransfer.getData("content");   
          	var line = vsinstance.getPosition();
          	var range = new monaco.Range(line.lineNumber, line.column, line.lineNumber, line.column);
          	var id = { major: 1, minor: 1 };             
          	var text = dataFromId(data,vsinstance._configuration._rawOptions.language);
          	var op = {identifier: id, range: range, text: text, forceMoveMarkers: true};
          	vsinstance.executeEdits("my-source", [op]);
      	}
        
        htmlelement.ondragover = function(e){allowDrop(e)};
        htmlelement.ondrop = function(e){dropParam(e,myVSHTML)};
        jselement.ondragover = function(e){allowDrop(e)};
        jselement.ondrop = function(e){dropParam(e,myVSJS)};
        
        myVSJS = monaco.editor.create(document.getElementById('jscode'), {
    		value: myTextAreaJS.value,
    		language: 'javascript',
    		readOnly: myTextArea.disabled,
    		scrollBeyondLastLine: false,
    		theme: "vs-dark",
    		automaticLayout: true
    	});

    	myVSHL = monaco.editor.create(hlelement, {
            value: myTextAreaHL.value,
            language: 'html',
            readOnly: myTextArea.disabled,
            scrollBeyondLastLine: false,
            theme: "vs-dark",
            automaticLayout: true
        });
        
        myVSHTML.addCommand(monaco.KeyCode.F11, function() {
        	if(!myVSHTML_isfullscreen){
	        	document.getElementById("htmlcode").style.maxWidth = "100%";
	            document.getElementById("htmlcode").style.maxHeight = "100%";
	            document.getElementById("htmlcode").style.height = "100%";
	            document.getElementById("htmlcode").style.left = "0";
	            document.getElementById("htmlcode").style.right = "0";
	            document.getElementById("htmlcode").style.top = "0";
	            document.getElementById("htmlcode").style.bottom = "0";
	            document.getElementById("htmlcode").style.position = "fixed";
	            document.getElementById("htmlcode").style.zIndex = "1000";
	            myVSHTML_isfullscreen=true;
        	}
        	else{
        		document.getElementById("htmlcode").style.maxWidth = "";
	            document.getElementById("htmlcode").style.maxHeight = "";
	            document.getElementById("htmlcode").style.height = "400px";
	            document.getElementById("htmlcode").style.left = "";
	            document.getElementById("htmlcode").style.right = "";
	            document.getElementById("htmlcode").style.top = "";
	            document.getElementById("htmlcode").style.bottom = "";
	            document.getElementById("htmlcode").style.position = "";
	            document.getElementById("htmlcode").style.zIndex = "";
	            myVSHTML_isfullscreen=false;
        	}
        });
        
        myVSHTML.addCommand(monaco.KeyCode.Escape, function() {
        	document.getElementById("htmlcode").style.maxWidth = "";
            document.getElementById("htmlcode").style.maxHeight = "";
            document.getElementById("htmlcode").style.height = "400px";
            document.getElementById("htmlcode").style.left = "";
            document.getElementById("htmlcode").style.right = "";
            document.getElementById("htmlcode").style.top = "";
            document.getElementById("htmlcode").style.bottom = "";
            document.getElementById("htmlcode").style.position = "";
            document.getElementById("htmlcode").style.zIndex = "";
            myVSHTML_isfullscreen=false;
        });
        
        myVSJS.addCommand(monaco.KeyCode.F11, function() {
        	if(!myVSJS_isfullscreen){
	        	document.getElementById("jscode").style.maxWidth = "100%";
	            document.getElementById("jscode").style.maxHeight = "100%";
	            document.getElementById("jscode").style.height = "100%";
	            document.getElementById("jscode").style.left = "0";
	            document.getElementById("jscode").style.right = "0";
	            document.getElementById("jscode").style.top = "0";
	            document.getElementById("jscode").style.bottom = "0";
	            document.getElementById("jscode").style.position = "fixed";
	            document.getElementById("jscode").style.zIndex = "1000";
	            myVSJS_isfullscreen=true;
        	}
        	else{
        		document.getElementById("jscode").style.maxWidth = "";
	            document.getElementById("jscode").style.maxHeight = "";
	            document.getElementById("jscode").style.height = "400px";
	            document.getElementById("jscode").style.left = "";
	            document.getElementById("jscode").style.right = "";
	            document.getElementById("jscode").style.top = "";
	            document.getElementById("jscode").style.bottom = "";
	            document.getElementById("jscode").style.position = "";
	            document.getElementById("jscode").style.zIndex = "";
	            myVSJS_isfullscreen=false;
        	}
        });
        
        myVSJS.addCommand(monaco.KeyCode.Escape, function() {
        	document.getElementById("jscode").style.maxWidth = "";
            document.getElementById("jscode").style.maxHeight = "";
            document.getElementById("jscode").style.height = "400px";
            document.getElementById("jscode").style.left = "";
            document.getElementById("jscode").style.right = "";
            document.getElementById("jscode").style.top = "";
            document.getElementById("jscode").style.bottom = "";
            document.getElementById("jscode").style.position = "";
            document.getElementById("jscode").style.zIndex = "";
            myVSJS_isfullscreen=false;
        });

        function modelChange(){
            if(timerWrite){
                clearTimeout(timerWrite)
            }
            timerWrite = window.setTimeout(
                function(){
                    $('#templateCode').val(myVSHTML.getValue());
                    $('#templateCodeJS').val(myVSJS.getValue());
                    $('#headerlibsCode').val(myVSHL.getValue());
                    searchProperties(myVSHTML.getValue(),myVSJS.getValue());
                    updatePreview();
                },2000
            );
        }

        myVSHTML.onDidChangeModelContent(modelChange);
    	myVSJS.onDidChangeModelContent(modelChange);
    	myVSHL.onDidChangeModelContent(modelChange);

    	if(!$("#id").val()  && ($('#templateCode').text().trim().length == 0 && $('#templateCodeJS').text().trim().length == 0)){
            changeInitCodeView(languaje);
        }
    }
    
	var addConfig = function() {
		
		$("#type").val(gadgetTemplateInit.id);
		$("#instance").val(true);
		$("#config").val(JSON.stringify(vueapp._data.gformvalue));

		var identV = jQuery('#gadget_create_form').validate().element("#identification");
		var descV = jQuery('#gadget_create_form').validate().element("#description");
		 
		if (typeof gadget.id == 'undefined' || gadget.id == null) {
			if (identV) {
				Promise.all([validateGadgetIdentification()]).then((data) => {
					var valid = "false"
					if (data[0].exist === "true") {
						toastr.error(gadgetTemplateCreateJson.existIdent, '');
						valid = false

					} else {
						valid = true
					}

					if (identV && descV && valid) {
						toastr.success(messagesForms.validation.genFormSuccess, '');
						jQuery("#type").val(gadgetTemplateInit.id);
						jQuery("#instance").val(true);
						jQuery("#public").val(false);
						jQuery("#config").val(JSON.stringify(GadgetsTemplateCreateController.getNewConfig()));
						
						jQuery("#gadget_create_form").get(0).submit();
					}
				}).catch((response) => {
				})
			}


		} else {
			if (descV) {
				toastr.success(messagesForms.validation.genFormSuccess, '');
				jQuery("#type").val(gadgetTemplateInit.id);
				jQuery("#instance").val(true);
				jQuery("#public").val(false);
				jQuery("#config").val(JSON.stringify(GadgetsTemplateCreateController.getNewConfig()));
				
				jQuery("#gadget_create_form").get(0).submit();
			}
		}
	}
	
	// FORM VALIDATION
	var handleValidation = function() {
		
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#gadget_create_form');
		

		// set current language
		currentLanguage = templateCreateReg.language || LANGUAGE;
		form1.validate({
					errorElement : 'span', // default input error message
											// container
					errorClass : 'help-block help-block-error', // default input
																// error message
																// class
					focusInvalid : false, // do not focus the last invalid
											// input
					ignore : ":hidden:not(.selectpicker)", // validate all
															// fields including
															// form hidden input
															// but not
															// selectpicker
					lang : currentLanguage,
					// custom messages
					messages : {

					},
					// validation rules
					rules : {
						identification : {
							minlength : 5,
							required : true
						},
						description : {
							minlength : 5,
							required : true
						},

					},
					invalidHandler : function(event, validator) { // display
																	// error
																	// alert on
																	// form
																	// submit
																	
						toastr.error(messagesForms.validation.genFormError,'');
					},
					errorPlacement : function(error, element) {
						if (element.is(':checkbox')) {
							error
									.insertAfter(element
											.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline"));
						} else if (element.is(':radio')) {
							error
									.insertAfter(element
											.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline"));
						} else {
							error.insertAfter(element);
						}
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
							toastr.success(messagesForms.validation.genFormSuccess,'');
							$("#type").val(gadgetTemplateInit.id);		
							$("#instance").val(true);			 
							$("#public").val(false);
							$("#config").val(JSON.stringify(GadgetsTemplateCreateController.getNewConfig()));
						    $("#gadget_create_form").get(0).submit();   	 
					}
				});
		$("#showBtn").click(function(){		
			updatePreview(vueapp._data.gformvalue,);
		})
		updatePreview(vueapp._data.gformvalue,);
	}
	
	
	
	
	
	
	var handleInitEditor = function(){
        $("#type").on("change",changeViewIframe);
	}
	
	function dataFromId(id,type){
		var ident = new Uint32Array(1);
		window.crypto.getRandomValues(ident);
		var initParam, endParam;
		if(type === "html"){
		    initParam = '<!--'
		    endParam = '-->'
		}
		else{
		    initParam = '/*'
            endParam = '*/'
		}
		var data;
		switch(id) {
            case "label_text":
                data = 'label-osp  name="parameterName-'+ident+'" type="text"';
                break;
            case "label_number":
                data = 'label-osp  name="parameterName-'+ident+'" type="number"';
                break;
            case "label_ds":
                data = 'label-osp  name="parameterName-'+ident+'" type="ds"';
                break;
            case "label_ds_parameter":
                data = 'label-osp  name="parameterName-'+ident+'" type="ds_parameter"';
                break;
            case "select_options":
                data = 'select-osp  name="parameterName-'+ident+'" type="ds" options="a,b,c"';
                break;
            default:
                data = "";
            }
        return initParam + data + endParam;
	}

	function searchTag(regex,str){
		let m;
		let found=[];
		while ((m = regex.exec(str)) !== null) {  
		    if (m.index === regex.lastIndex) {
		        regex.lastIndex++;
		    }
		    m.forEach(function(item, index, arr){			
				found.push(arr[0]);			
			});  
		}
		return found;
	}
	
	function searchTagContentName(regex,str){
		let m;
		var content;
		while ((m = regex.exec(str)) !== null) {  
		    if (m.index === regex.lastIndex) {
		        regex.lastIndex++;
		    }
		    m.forEach(function(item, index, arr){			
		    	content = arr[0].match(/"([^"]+)"/)[1];			
			});  
		}
		return content;
	}
	
	
	function searchTagContentDescriptionOrName(regexDescription,regexName, str){
		var tag = searchTagContentName(regexDescription,str);
		if(typeof tag=='undefined' || tag==null || tag.length==0 ){
			tag = searchTagContentName(regexName,str);
		}
		return tag;
	}
	
	
	function searchProperties(strhtml,strjs){

		const regexHTML =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\>/g; //html param tag
		const regexJS =  /\/\*[\-\-\s\w\>\=\"\'\,\:\+\_\/]*\*\//g; //js param tag
		const regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;
		const regexDescription = /description\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;
		const regexOptions = /options\s*=\s*\"[\s\w\>\=\-\'\+\_\/\,]*\s*\"/g;
		let found=[];
		found = searchTag(regexHTML,strhtml).concat(searchTag(regexJS,strjs));

		$('#parameters-form').empty();
		$('#parameters-form').append('<li class="list-group-item bg-blue-hoki font-grey-cararra">'+gadgetTemplateCreateJson.titleParametersSelected+'</li>');

		var plist = [];
		
		for (var i = 0; i < found.length; i++) {			
			var tag = found[i];
			var name = searchTagContentName(regexName,tag)
			var param = {
				name: name
			}
			if(tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
				param.type = "input-text"
			}else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
				param.type = "input-number"
			}else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
				param.type = "ds-field(ds[0].)"
			}else if(tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
				param.type = "ds-field"
			}else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-osp')>=0){
				param.type = "selector";
				var options = searchTagContentName(regexOptions,tag);
				if (options && options.length > 0) {
					param.options = options.split(",").map(
						function(option){
							return {
								value: option
							}
						}
					)
				}
			}
			if (name) {
				plist.push(param);
			}
		}
		return plist;
	}


	function getNewConfig(){
		
			try{
				
					var gadConf={};
					vueapp._data.gformvalue; 
					//paramsValueGadget = paramMap.parameters;
					paramsValueGadget = vueapp._data.gformvalue.parameters;
					if(typeof paramsValueGadget !='undefined' && paramsValueGadget!=null && paramsValueGadget.length>0){
						for(var i = 0 ; i < paramsValueGadget.length;i++){							
							if(paramsValueGadget[i].type =="labelsds"){
								paramsValueGadget[i].value = {field:$('#'+paramsValueGadget[i].label).val(), type:$('#'+paramsValueGadget[i].label).attr('datatype')};
							}else if (paramsValueGadget[i].type =="labelsdspropertie"){
								paramsValueGadget[i].value = {field:$('#'+paramsValueGadget[i].label).val(), type:$('#'+paramsValueGadget[i].label).attr('datatype')}
							}else if (paramsValueGadget[i].type =="selects"){								
								var options = [];
								try{								
								if($('#'+paramsValueGadget[i].label)[0].options!=null && $('#'+paramsValueGadget[i].label)[0].options.length>0){
									for(var j=0;j< $('#'+paramsValueGadget[i].label)[0].options.length;j++){
										options.push($('#'+paramsValueGadget[i].label)[0].options[j].value);
									}
								}
								}catch{								
								}
								paramsValueGadget[i].optionsValue=options;
								paramsValueGadget[i].value= $('#'+paramsValueGadget[i].label).val();
							}else if (paramsValueGadget[i].type =="labelsText"){
								paramsValueGadget[i].value= $('#'+paramsValueGadget[i].label).val();
							}else if (paramsValueGadget[i].type =="labelsNumber"){
								paramsValueGadget[i].value= Number($('#'+paramsValueGadget[i].label).val());
							}						
						}					
					}	
					
					 
					gadConf.parameters=paramsValueGadget;
					// currentDatasource = paramMap.datasource;
						currentDatasource = vueapp._data.gformvalue.datasource;						
					if(typeof currentDatasource!='undefined' && currentDatasource!=null ){
						gadConf.datasource = {
						    name: currentDatasource.name,
						    refresh: currentDatasource.refresh,
						    type: typeof currentDatasource.mode ==='undefined'?'query':currentDatasource.mode,
						    id: currentDatasource.id,
						    query: currentDatasource.query,
						    description: currentDatasource.description
						}
					}				
				}catch(error){}		
				return gadConf;		
		
}


	var updatePreview = function (paramMapAux){
		
		refreshIframe();
		paramMap = paramMapAux;
	}

	var updateComponent = function (){
		
    	var iframe = document.querySelector("#icontent iframe");
        var iframeWindow = (iframe.contentWindow || iframe.contentDocument);
        var scope = iframeWindow.angular.element(iframeWindow.document.getElementsByTagName((languaje.startsWith("angularJS")?'livehtml':(languaje.startsWith("vueJS")?'vuetemplate':'reacttemplate')))[0]).scope();
        scope.$$childHead.vm.id="gapp";

        var templateCodeHTML = {};
        templateCodeHTML.currentValue = $('#templateCode').val() || "";
        templateCodeHTML.isFirstChange = function(){return false}

        var templateCodeJS = {};
        templateCodeJS.currentValue = $('#templateCodeJS').val()  || ""
        templateCodeJS.isFirstChange = function(){return false}

        if(paramMap){
            templateCodeHTML.currentValue = parseProperties(templateCodeHTML.currentValue, paramMap);
            templateCodeJS.currentValue = parseProperties(templateCodeJS.currentValue, paramMap, true);
            if(paramMap["datasource"]){
				scope.$$childHead.vm.init=true
				scope.$$childHead.vm.$onChanges({datasource: {currentValue: {name: paramMap["datasource"].name, refresh: 0, type: "query"}, previousValue: ""}});
			}
     	}
		if(gadget!=null){
			try{
				var gadConf =  JSON.parse(gadget.config);
				if(gadConf.datasource!=null){
					scope.$$childHead.vm.$onChanges({datasource: {currentValue: {name: gadConf.datasource.name, refresh: 0, type: "query"}, previousValue: ""}});
				}	
			}catch(error){}			
		}


        scope.$$childHead.vm.livecontent=templateCodeHTML.currentValue;
        scope.$$childHead.vm.livecontentcode=templateCodeJS.currentValue;
		scope.$$childHead.vm.params=paramMap;
		scope.$$childHead.vm.tparams=paramMap;

        scope.$$childHead.vm.$onChanges({livecontentcode: templateCodeJS,livecontent: templateCodeHTML});
    }
	
	/** this function Replace parameteres for his selected values*/
    function parseProperties(str, paramMap, jsparam){ //the third param build replace with short circuit param: "param ||" for compatibility with default value in js

      var regexTagHTML =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\-->/g;
      var regexTagJS =  /\/\*[\-\-\s\w\>\=\"\'\,\:\+\_\/]*\*\//g;
      var regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;
      var regexOptions = /options\s*=\s*\"[\s\w\>\=\-\'\:\,\+\_\/]*\s*\"/g;
      var found=[];
      found = searchTag(regexTagHTML,str).concat(searchTag(regexTagJS,str));
  
      var parserList=[];
      for (var i = 0; i < found.length; i++) {
        var tag = found[i];
        var paramValue;
		var key = searchTagContentName(regexName,tag);
		function getKeyRec(paramMap, key) {
			for (k in paramMap) {
				if (typeof paramMap[k] === "object") {
					var ret = getKeyRec(paramMap[k], key)
					if (ret) {
						return ret;
					}
				} else if (key in paramMap) {
					return paramMap[key];
				}		
			}
			return null
		}
		var value = getKeyRec(paramMap, key);
		if (value !== null) {
	        if((tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0) ||
	           (tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0) ||
	           (tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-osp')>=0)){
	            if(!jsparam){
	                paramValue = value;
	            }
	            else{
	                paramValue = "'" + value + "' || ";
	            }
	            parserList.push({tag:tag,value:paramValue});
	        }else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
	            if(!jsparam){
	                paramValue = value;
	            }
	            else{
	                paramValue = value + " || ";
	            }
	            parserList.push({tag:tag,value:paramValue});
	        }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
	            if(!jsparam){
	                paramValue = value;
	                parserList.push({tag:tag,value:"{{ds[0]."+paramValue+"}}"});
	            }
	            else{
	                paramValue = "'" + value + "' || ";
	                parserList.push({tag:tag,value:"ds[0]."+paramValue+""});
	            }
	        }
		}
      } 
      //Replace parameteres for his values
      for (var i = 0; i < parserList.length; i++) {
        str = str.replace(parserList[i].tag,parserList[i].value);
      }
      return str;
    }
	
	 var drag = function (ev) {		 
		    ev.dataTransfer.setData("content", ev.target.id);
		}

	var changeInitCodeView = function(languajeC){
        fetch('/controlpanel/gadgettemplates/getTemplateTypeById/' + languajeC)
        .then(function(response) {
            return response.json();
        })
        .then(function(json) {
            myVSJS.setValue(json.templateJS.slice());
            myVSHTML.setValue(json.template.slice());
            myVSHL.setValue(json.headerlibs.slice());
            languaje=languajeC;
        });
	}

	var refreshIframe = function (){
	    var request;
        if(gadgetTemplateInit.type.startsWith("angularJS")){
            request = fetch('/controlpanel/gadgettemplates/gadgetViewer');
        }
        else if(gadgetTemplateInit.type.startsWith("vueJS")){
            request = fetch('/controlpanel/gadgettemplates/gadgetViewerVue');
        }
        else{
            request = fetch('/controlpanel/gadgettemplates/gadgetViewerReact');
        }

        request.then(function(response) {
	
            return response.text();
        })
        .then(function(html) {
	
            var ifrmdiv = document.getElementById('icontent');
            ifrmdiv.innerHTML = "";
            var ifrm = document.createElement('iframe');
            ifrmdiv.appendChild(ifrm);

            ifrm.style.height='400px';
            ifrm.style.width='100%';
            ifrm.style.border='none';

            ifrm = ifrm.contentWindow || ifrm.contentDocument.document || ifrm.contentDocument;
            ifrm.document.open();
            ifrm.document.write(html.replace('<!--headerlibs-->',myVSHL.getValue()));
            ifrm.document.close();
        })
    }

    var changeViewIframe = function(){
        refreshIframe();
        GadgetsTemplateCreateController.changeInitCodeView(gadgetTemplateInit.type);
    }

	var initTemplateElements = function(){
			
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
				$('.form').validate().element('#' + event.target.id);                // checks form for validity
			});			
			
		// Reset form
			$('#resetBtn').on('click',function(){ 
				cleanFields('gadget_create_form');
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
		
		$( "#public" ).prop( "checked", false );

	
		//CLEANING type
		$("#type")[0].selectedIndex = 0;
		changeInitCodeView($("#type option:first").val());
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}
	
	function getSubcategories(){
 		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		$.ajax({
		    url: '/controlpanel/categories/getSubcategories/'+$("#categories_select").val(),
		    headers: {
				[csrf_header]: csrf_value
		    },
		    type: 'GET',		
		    async: false,
		    error: function() {
		    	$("#subcategories").find("option").remove();
				$("#subcategories").attr("disabled","disabled");
				$("#subcategories").selectpicker("refresh");		    
		    },
		    success: function(result) {  
		    	$("#subcategories").removeAttr("disabled");
		    	$("#subcategories").find("option").remove();
		    	$('#subcategories').append($('<option>', { 
	    	        value: '',
	    	        text : '',
	    	        style : 'height:30px;'
	    	    }));
		    	$.each(result, function (i, subcategory) {
		    	    $('#subcategories').append($('<option>', { 
		    	        value: subcategory,
		    	        text : subcategory 
		    	    }));
		    	});
		    	$("#subcategories").selectpicker("refresh");		    	
		    }
		});
	}
	
	function initSubcategories() {
		if(gadgetTemplateCreateJson.actionMode != null){
			if(gadgetTemplateCreateJson.category != null){
			 	getSubcategories();
			 	$('#subcategories').val(gadgetTemplateCreateJson.subcategoryField);
			 	$("#subcategories").selectpicker("refresh"); 
			}
		}
	}
	
	var getConfig = function (){				
		if(gadget!=null){
			try{
				
					var gadConf =  JSON.parse(gadget.config);
					paramsValueGadget = gadConf.parameters;
					if(typeof paramsValueGadget !='undefined' && paramsValueGadget!=null && paramsValueGadget.length>0){
						for(var i = 0 ; i < paramsValueGadget.length;i++){							
							if(paramsValueGadget[i].type =="labelsds"){
								paramsValueGadget[i].value = {field:$('#'+paramsValueGadget[i].label).val(), type:$('#'+paramsValueGadget[i].label).attr('datatype')};
							}else if (paramsValueGadget[i].type =="labelsdspropertie"){
								paramsValueGadget[i].value = {field:$('#'+paramsValueGadget[i].label).val(), type:$('#'+paramsValueGadget[i].label).attr('datatype')}
							}else if (paramsValueGadget[i].type =="selects"){
								paramsValueGadget[i].value= $('#'+paramsValueGadget[i].label).val();
							}else if (paramsValueGadget[i].type =="labelsText"){
								paramsValueGadget[i].value= $('#'+paramsValueGadget[i].label).val();
							}else if (paramsValueGadget[i].type =="labelsNumber"){
								paramsValueGadget[i].value= Number($('#'+paramsValueGadget[i].label).val());
							}						
						}					
					}								
					if(typeof currentDatasource!='undefined' && currentDatasource!=null ){
						gadConf.datasource = {
						    name: currentDatasource.name,
						    refresh: currentDatasource.refresh,
						    type: currentDatasource.mode,
						    id: currentDatasource.id,
						    query: currentDatasource.query,
						    description: currentDatasource.description
						}
					}				
				}catch(error){}		
				return gadConf;		
		}
	}

	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return templateCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			// INPUT MASK FOR ontology identification allow only letters, numbers and -_
			$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
			handleVS();
			handleValidation();
			handleInitEditor();
			initTemplateElements();
			initSubcategories();
		},
		
		// REDIRECT
		go: function(id,url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			if(!isEdit){
				if(id == "null"){
					navigateUrl(url);
				}else{
					  navigateUrl(id);
				}
			}
			else{
			    freeResource(id,url);
			}
		},
		updatePreview: function(){
			updatePreview();
		},

		updateComponent: function(){
		    updateComponent();
		},
	
		// DELETE GADGET TEMPLATE 
		deleteGadgetTemplate: function(gadgetId){
			logControl ? console.log(LIB_TITLE + ': deleteGadget()') : '';	
			deleteGadgetTemplateConfirmation(gadgetId);			
		},
		// DELETE GADGET INSTANCE 
		deleteInstance: function(gadgetId){
			logControl ? console.log(LIB_TITLE + ': deleteInstance()') : '';	
			deleteInstanceConfirmation(gadgetId);			
		},
		drag: function(ev){
			drag(ev);
		},
		
		allowDrop: function(ev){
			allowDrop(ev);
		},
		
		dropParam: function(ev){
			dropParam(ev);
		},
		getConfig:function(){
			return getConfig();
		},
		getNewConfig:function(){
			return getNewConfig();
		},
		changeInitCodeView: function(type){
		    changeInitCodeView(type);
		},
		searchPropertiesInText: function() {
			return searchProperties(document.getElementById('templateCode').value, document.getElementById('templateCodeJS').value);
		},
		getSubcategories: function() {
			getSubcategories();
		},
		addConfig: function() {
			addConfig();
		}
		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	GadgetsTemplateCreateController.load(gadgetTemplateCreateJson);	
		
	// AUTO INIT CONTROLLER.
	GadgetsTemplateCreateController.init();
});
