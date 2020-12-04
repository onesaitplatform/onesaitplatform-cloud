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
	var languaje=$("#type").val();
	var paramMap;
	var isEdit = window.location.href.indexOf("/gadgettemplates/update/") != -1;

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
	
	
	
	// DELETE GADGET
	var deleteGadgetTemplateConfirmation = function(gadgetTemplateId){
		console.log('deleteGadgetConfirmation() -> formId: '+ gadgetTemplateId);
		
		// no Id no fun!
		if ( !gadgetTemplateId ) {$.alert({title: 'ERROR!',type: 'red' , theme: 'dark', content: 'NO GATGET TEMPLATE SELECTED!'}); return false; }
		
		logControl ? console.log('deleteGadgetTemplateConfirmation() -> formAction: ' + $('.delete-gadget').attr('action') + ' ID: ' + $('.delete-gadget').attr('userId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogGadgetTemplate('delete_gadget_template_form');	
	}
	
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
        
        
        if(myTextArea.disabled){
        	searchPropertiesAndEditForm(myVSHTML.getValue(),myVSJS.getValue());
        }
        else{
        	searchProperties(myVSHTML.getValue(),myVSJS.getValue());
        }

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
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#gadget_create_form');
		var error1 = $('.alert-danger');
		var success1 = $('.alert-success');

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
						success1.hide();
						error1.show();
						App.scrollTo(error1, -200);
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

						 success1.show();
			                error1.hide();
							form.submit();
					}
				});
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
	
	function searchProperties(strhtml,strjs){

		const regexHTML =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\>/g; //html param tag
		const regexJS =  /\/\*[\-\-\s\w\>\=\"\'\,\:\+\_\/]*\*\//g; //js param tag
		const regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;

		let found=[];
		found = searchTag(regexHTML,strhtml).concat(searchTag(regexJS,strjs));

		$('#parameters-form').empty();
		$('#parameters-form').append('<li class="list-group-item bg-blue-hoki font-grey-cararra">'+gadgetTemplateCreateJson.titleParametersSelected+'</li>');

		for (var i = 0; i < found.length; i++) {			
			var tag = found[i];
			if(tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
		
				$('#parameters-form').append('<li class="list-group-item"><label class="bold">'+searchTagContentName(regexName,tag)+'&nbsp:&nbsp</label><label>'+gadgetTemplateCreateJson.parameterTextLabel+'</label></li>');
			}else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
			
				$('#parameters-form').append('<li class="list-group-item"><label class="bold">'+searchTagContentName(regexName,tag)+'&nbsp:&nbsp</label><label>'+gadgetTemplateCreateJson.parameterNumberLabel+'</label></li>');
			}else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
				
				$('#parameters-form').append('<li class="list-group-item"><label class="bold">'+searchTagContentName(regexName,tag)+'&nbsp:&nbsp</label><label>'+gadgetTemplateCreateJson.parameterDsLabel+'</label></li>');
			}else if(tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
				
				$('#parameters-form').append('<li class="list-group-item"><label class="bold">'+searchTagContentName(regexName,tag)+'&nbsp:&nbsp</label><label>'+gadgetTemplateCreateJson.parameterDsPropertieLabel+'</label></li>');
			}else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-osp')>=0){
			
				$('#parameters-form').append('<li class="list-group-item"><label class="bold">'+searchTagContentName(regexName,tag)+'&nbsp:&nbsp</label><label>'+gadgetTemplateCreateJson.parameterSelectLabel+'</label></li>');
			}
			
		} 	 
	
	}
	
	function searchPropertiesAndEditForm(strhtml,strjs){

		const regexHTML =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\>/g; //html param tag
        const regexJS =  /\/\*[\-\-\s\w\>\=\"\'\,\:\+\_\/]*\*\//g; //js param tag
		const regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;

		let found=[];
		found = searchTag(regexHTML,strhtml).concat(searchTag(regexJS,strjs));

		$('#parameters-form').empty();
		$('#parameters-form').append('<li class="list-group-item bg-blue-hoki font-grey-cararra">'+gadgetTemplateCreateJson.titleParametersSelected+'</li>');

		var haveDS = false;
		var haveDSParam = false;
		for (var i = 0; i < found.length; i++) {			
			var tag = found[i];
			
			if(tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
		
				$('#parameters-form').append('<li class="list-group-item"><label class="bold">'+searchTagContentName(regexName,tag)+'&nbsp:&nbsp</label><label>'+gadgetTemplateCreateJson.parameterTextLabel+'</label><input style="width:50%" class="pull-right form-control" data=\''+ found[i] +'\'></input></li>');
			}else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
			
				$('#parameters-form').append('<li class="list-group-item"><label class="bold">'+searchTagContentName(regexName,tag)+'&nbsp:&nbsp</label><label>'+gadgetTemplateCreateJson.parameterNumberLabel+'</label><input style="width:50%" class="pull-right form-control" data=\''+ found[i] +'\'></input></li>');
			}else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
				haveDS=true;
				$('#parameters-form').append('<li class="list-group-item"><label class="bold">'+searchTagContentName(regexName,tag)+'&nbsp:&nbsp</label><label>'+gadgetTemplateCreateJson.parameterDsLabel+'</label><input style="width:50%" class="pull-right form-control" data=\''+ found[i] +'\'></input></li>');
			}else if(tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
				haveDSParam=true;
				$('#parameters-form').append('<li class="list-group-item"><label class="bold">'+searchTagContentName(regexName,tag)+'&nbsp:&nbsp</label><label>'+gadgetTemplateCreateJson.parameterDsPropertieLabel+'</label><select style="width:50%" class="param-ds pull-right form-control" data=\''+ found[i] +'\'></select></li>');
			}else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-osp')>=0){
				haveDSParam=true;	
				$('#parameters-form').append('<li class="list-group-item"><label class="bold">'+searchTagContentName(regexName,tag)+'&nbsp:&nbsp</label><label>'+gadgetTemplateCreateJson.parameterSelectLabel+'</label><input style="width:50%" class="pull-right form-control" data=\''+ found[i] +'\'></input></li>');
			}
			
		}
		
		function getJsonFields (obj, stack, fields) {
			for (var property in obj) {
				if (obj.hasOwnProperty(property)) {
					if (typeof obj[property] == "object") {
						getJsonFields(obj[property], stack + (stack==""?'':'.') + property, fields);
					} else {
						fields.push({field:stack + (stack==""?'':'.') + property, type:typeof obj[property]});
					}
				}
			} 
			return fields;
		}
		
		var updateParams = function(){
			var sDatasource = $("#datasourcepicker").val();
			$.get("/controlpanel/datasources/getSampleDatasource/" + sDatasource).done(
				function(data){
					$(".param-ds").empty();
					if(data.length > 0){
						var fields = getJsonFields(data[0],"", []);
						for (i in fields){
							$(".param-ds").append(new Option(fields[i].field, fields[i].field))
						}
						$(".param-ds").val(fields[0].field)
					}
					else{
						$(".param-ds").append(new Option("No data", "No"))
					}
					
				}
			).fail(
				function(e){
					console.error("Error getting Datasource Params", e);
				}
			)
			
		}
		
		if(haveDSParam && !haveDS){
			$('<li class="list-group-item"><label class="bold">Datasource&nbsp:&nbsp</label><select id="datasourcepicker" style="width:50%" class="pull-right form-control" data=\'datasource\'></input></li>').insertAfter($('#parameters-form').children()[0]);
			$.get("/controlpanel/datasources/getUserGadgetDatasources").done(
				function(data){
					if(data.length>0){
						for (i in data){
							$("#datasourcepicker").append(new Option(data[i].identification, data[i].id))
						}
						$("#datasourcepicker").val(data[0].id)
						updateParams();
					}
					else{
						$("#datasourcepicker").append(new Option("No data", "No"))
					}
				}
			).fail(
				function(e){
					console.error("Error getting Datasource", e);
				}
			);
			
			document.getElementById("datasourcepicker").onchange = updateParams;
		}
		
		$("#showBtn").click(function(){
			var mapEntries = {};
			$.each($('#parameters-form').find("li input"), function(){
				mapEntries[this.getAttribute("data")] = this.value;				
			})
			$.each($('#parameters-form').find("li select"), function(){
				mapEntries[this.getAttribute("data")] = $(this).find("option:selected").text();				
			})
			updatePreview(mapEntries,);
		})
	
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
                scope.$$childHead.vm.$onChanges({datasource: {currentValue: {name: paramMap["datasource"], refresh: 0, type: "query"}, previousValue: ""}});
            }
        }

        scope.$$childHead.vm.livecontent=templateCodeHTML.currentValue;
        scope.$$childHead.vm.livecontentcode=templateCodeJS.currentValue;

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
        if((tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0) ||
           (tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0) ||
           (tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-osp')>=0)){
            if(!jsparam){
                paramValue = paramMap[tag];
            }
            else{
                paramValue = "'" + paramMap[tag] + "' || ";
            }
            parserList.push({tag:tag,value:paramValue});
        }else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
            if(!jsparam){
                paramValue = paramMap[tag];
            }
            else{
                paramValue = paramMap[tag] + " || ";
            }
            parserList.push({tag:tag,value:paramValue});
        }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
            if(!jsparam){
                paramValue = paramMap[tag];
                parserList.push({tag:tag,value:"{{ds[0]."+paramValue+"}}"});
            }
            else{
                paramValue = "'" + paramMap[tag] + "' || ";
                parserList.push({tag:tag,value:"ds[0]."+paramValue+""});
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
        if($("#type").val().startsWith("angularJS")){
            request = fetch('/controlpanel/gadgettemplates/gadgetViewer');
        }
        else if($("#type").val().startsWith("vueJS")){
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
        GadgetsTemplateCreateController.changeInitCodeView($("#type").val());
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
		},
		
		// REDIRECT
		go: function(id,url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			if(!isEdit){
			    navigateUrl(id);
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
	
		// DELETE GADGET DATASOURCE 
		deleteGadgetTemplate: function(gadgetId){
			logControl ? console.log(LIB_TITLE + ': deleteGadget()') : '';	
			deleteGadgetTemplateConfirmation(gadgetId);			
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

		changeInitCodeView: function(type){
		    changeInitCodeView(type);
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
