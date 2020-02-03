var GadgetsTemplateCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Gadget Template Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	//var myCodeMirror;
	//var myCodeMirrorJS;
	var myVSHTML;
	var myVSHTML_isfullscreen;
	var myVSJS;
	var myVSJS_isfullscreen;
	var timerWrite;
	// CONTROLLER PRIVATE FUNCTIONS	
	
	
	
	var navigateUrl = function(url){ window.location.href = url; }
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
		
        var myTextArea = document.getElementById('templateCode');
        var myTextAreaJS = document.getElementById('templateCodeJS');
        
        var htmlelement = document.getElementById('htmlcode');
        
        if(!$("#id").val()  && ($('#templateCode').text().trim().length == 0 && $('#templateCodeJS').text().trim().length == 0)){
        	myTextArea.value = "<!--Write your HTML <div></div> and CSS <style></style> here -->\n\n<!--Focus here and F1 to show help \n    F11/ESC to enable/disable full screen editor\n    Ctrl + F to find/replace, ... -->\n\n<!--When you are editing this template, in this section, you can drop params in the cursor position. \n    To test this params go to show view or added it to a dashboard-->";
        	myTextAreaJS.value = "//Write your controller (JS code) code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\nvm.initLiveComponent = function(){\n\n};\n\n//This function will be call when data change. On first execution oldData will be null\nvm.drawLiveComponent = function(newData, oldData){\n\n};\n\n//This function will be call on element resize\nvm.resizeEvent = function(){\n\n}\n\n//This function will be call when element is destroyed\nvm.destroyLiveComponent = function(){\n\n};";       
        }
        
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
          	var text = dataFromId(data);
          	var op = {identifier: id, range: range, text: text, forceMoveMarkers: true};
          	vsinstance.executeEdits("my-source", [op]);
      	}
        
        htmlelement.ondragover = function(e){allowDrop(e)};
        htmlelement.ondrop = function(e){dropParam(e,myVSHTML)};
        
        myVSJS = monaco.editor.create(document.getElementById('jscode'), {
    		value: myTextAreaJS.value,
    		language: 'javascript',
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
        	searchPropertiesAndEditForm(myVSHTML.getValue());
        }
        else{
        	searchProperties(myVSHTML.getValue());
        }
        
        myVSHTML.onDidChangeModelContent(function() {
        	if(timerWrite){
        		clearTimeout(timerWrite)
        	}
        	timerWrite = window.setTimeout(
        			function(){
        				$('#templateCode').val(myVSHTML.getValue());
        				$('#templateCodeJS').val(myVSJS.getValue());
        				searchProperties(myVSHTML.getValue());
        				updatePreview();
        			},
        			1000
        	);
    	})

    	myVSJS.onDidChangeModelContent(function() {
    		if(timerWrite){
        		clearTimeout(timerWrite)
        	}
        	timerWrite = window.setTimeout(
        			function(){
        				$('#templateCode').val(myVSHTML.getValue());
        				$('#templateCodeJS').val(myVSJS.getValue());
        				updatePreview();
        			},
        			1000
        	);
    	})
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
	
	
	
	function dataFromId(id){
		var ident = new Uint32Array(1);
		window.crypto.getRandomValues(ident);
		
		switch(id) {
	    case "label_text":	    
	        return '<!--label-osp  name="parameterName-'+ident+'" type="text"-->';
	        break;
	    case "label_number":	    	
	    	  return '<!--label-osp  name="parameterName-'+ident+'" type="number"-->';
	        break;
	    case "label_ds":	    	
	    	  return '<!--label-osp  name="parameterName-'+ident+'" type="ds"-->';
	        break;	  
	    case "label_ds_parameter":	    	
	    	  return '<!--label-osp  name="parameterName-'+ident+'" type="ds_parameter"-->';
	        break;	  
	    case "select_options":	    	
	   	  return '<!--select-osp  name="parameterName-'+ident+'" type="ds" options="a,b,c" -->';	    	        
	      break;
	    default:
	        return "";
	}
		
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
	
	function searchProperties(str){

		const regex =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\>/g;
		const regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;

		let found=[];
		found = searchTag(regex,str);		

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
	
	function searchPropertiesAndEditForm(str){

		const regex =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\>/g;
		const regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;

		let found=[];
		found = searchTag(regex,str);		

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
			updatePreview(mapEntries);
		})
	
	}

	var updatePreview = function (paramMap){
		var iframe = document.getElementById("icontent");
		iframe.src = iframe.src;
		iframe.onload = function() {
			var iframe = document.getElementById("icontent");
			var iframeWindow = (iframe.contentWindow || iframe.contentDocument);
	        var scope = iframeWindow.angular.element(iframeWindow.document.getElementsByTagName('livehtml')[0]).scope();
			var templateCodeHTML = {};
			templateCodeHTML.currentValue = $('#templateCode').val() || "";
			templateCodeHTML.isFirstChange = function(){return false}
			if(paramMap){
				templateCodeHTML.currentValue = parseProperties(templateCodeHTML.currentValue, paramMap);
				if(paramMap["datasource"]){
					scope.$$childHead.vm.$onChanges({datasource: {currentValue: {name: paramMap["datasource"], refresh: 0, type: "query"}, previousValue: ""}});
				}
			}
			var templateCodeJS = {}; 
			templateCodeJS.currentValue = $('#templateCodeJS').val()  || ""
			templateCodeJS.isFirstChange = function(){return false}
			
			scope.$$childHead.vm.livecontent=templateCodeHTML.currentValue;
			scope.$$childHead.vm.livecontentcode=templateCodeJS.currentValue;
			
	        scope.$$childHead.vm.$onChanges({livecontentcode: templateCodeJS,livecontent: templateCodeHTML});
		}
	}
	
	/** this function Replace parameteres for his selected values*/
    function parseProperties(str, paramMap){

      var regexTag =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\-->/g;
      var regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;
      var regexOptions = /options\s*=\s*\"[\s\w\>\=\-\'\:\,\+\_\/]*\s*\"/g;
      var found=[];
      found = searchTag(regexTag,str);	
  
      var parserList=[];
      for (var i = 0; i < found.length; i++) {
        var tag = found[i];			
        var paramValue = paramMap[tag];
        if(tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){                 
          parserList.push({tag:tag,value:paramValue});   
        }else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){
          parserList.push({tag:tag,value:paramValue});   
        }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){                
          parserList.push({tag:tag,value:"{{ds[0]."+paramValue+"}}"});        
        }else if(tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-osp')>=0){                                            
          parserList.push({tag:tag,value:paramValue});        
        }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-osp')>=0){                
          parserList.push({tag:tag,value:paramValue});  
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
			handleVS();
			handleValidation();
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		updatePreview: function(){
			updatePreview();
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
