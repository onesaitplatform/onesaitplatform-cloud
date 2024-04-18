var Rules = Rules || {};

Rules.List = (function() {
	"use-strict";
	var fileLoaded;
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	var jsTreeData;
	headersObj[csrfHeader] = csrfToken;
	var defaultRule= "package com.minsait.onesait.platform.rulesengine;\n"
		+ "import com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper;\n"
		+ "global com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper input;\n"
		+ "global com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper output;\n" + "\n"
		+ "dialect  \"mvel\"\n" + "\n" + "rule \"Assign role\"\n" + "\n" + "    when\n"
		+ "        eval( input.getProperty(\"currentSalary\") < 1000000 && input.getProperty(\"experienceInYears\") > 10 )\n"
		+ "    then\n" + "    	\n" + "        output.setProperty(\"role\", \"Manager\");\n" + "end";
	var init = function() {
		generateJson();
		
		editor = ace.edit("drlCode");
	    editor.setTheme("ace/theme/xcode");
	    editor.session.setMode("ace/mode/drools");
	    
	    
	    var input = document.getElementById('jsonCodeInput');
	    var output = document.getElementById('jsonCodeOutput');
	    myVSJSONInput = monaco.editor.create(input, {
    		value: "{\n}",
    		language: 'json',
    		readOnly: false,
    		scrollBeyondLastLine: false,
    		theme: "vs-dark",
    		automaticLayout: true
    	});
	    
	    myVSJSONOutput = monaco.editor.create(output, {
    		value: "{\n}",
    		language: 'json',
    		readOnly: false,
    		scrollBeyondLastLine: false,
    		theme: "vs-dark",
    		automaticLayout: true
    	});
	    
	    $("#buttonLoadFile").on("change", function(){
	  		var file = $('#buttonLoadFile').val().split('\\').pop();
	  		if(file!=null){
	  			$("#file_name").val(file);
	  		}
	  	});
	   
	    $('#updateBtn').on('click',function(){
	    	var id = $(this).data('id');
	    	var type = $(this).data('type');
	    	if(type == 'drl'){
	    		$.ajax({
		       	 	url : '/controlpanel/rule-domains/rule/' +id +'/drl' ,  
		       	 	headers: headersObj,
		       	 	data: editor.getValue(),
		            type : 'PUT',
		            contentType: 'text/plain'
		        }).done(function(data) {
		        	location.reload();
		        }).fail(function(error) {
		        	console.log(error);
		        	$('#errors').text(error.responseText);
		        	$('.alert-danger').show();
				});
	    	}else if(type == 'table'){
	    		if($("#buttonLoadFile").val() == ""){
	    			console.log("File is empty.");
		        	$('#errors').text("File is empty, please select a file to upload the rule");
		        	$('.alert-danger').show();
	    		}else{
	    			var fd = new FormData();
	    	        var files = $('#buttonLoadFile')[0].files;
	    	        
	    	        // Check file selected or not
	    	        if(files.length > 0 ){
	    	           fd.append('decisionTable',files[0]);
	    	        }
	    	        
	    			$.ajax({
			       	 	url : '/controlpanel/rule-domains/rule/' +id +'/decisionTable' ,  
			       	 	headers: headersObj,
			       	 	data: fd,
			            type : 'POST',
			            contentType: false,
			            processData: false,
			        }).done(function(data) {
			        	$("#file_name").val("");
			        	location.reload();
			        }).fail(function(error) {
			        	console.log(error);
			        	$('#errors').text(error.responseText);
			        	$('.alert-danger').show();
					});
	    		}
	    		
	    	}
	    	
	    })
	    
	    $('#downloadBtn').on('click',function(){
	    	var id = $(this).data('id');
	    	window.location.href = "/controlpanel/rule-domains/rule/" + id + "/downloadTable";
	    	
	    })
	    
	     $('#testBtn').on('click',function(){
	    	var id = $(this).attr('data-id');
	    	$('#testBtnSubmit').attr('data-id', id);
	    	$('#modal-json').modal('show');
	    });
	     $('#testBtnSubmit').on('click',function(){
	    	var id = $(this).attr('data-id')
	    	$.ajax({
	       	 	url : '/controlpanel/rule-domains/rule/' +id +'/test' ,  
	       	 	headers: headersObj,
	       	 	data: myVSJSONInput.getValue(),
	            type : 'POST',
	            contentType: 'text/plain'
	        }).done(function(data) {
	        	myVSJSONOutput.setValue(data)
	        	myVSJSONOutput.getAction('editor.action.formatDocument').run()
	        }).fail(function(error) {
	        	console.log(error);
	        	$.alert({
					title : 'ERROR',
					type : 'red',
					theme : 'light',
					content : error.responseText
				})
				
			});
	    })
	    $('#btn-rule-create').on('click', function(){
	    	navigateUrl('/controlpanel/rule-domains/' + domainId + '/rule');
	    })
	  
		
	};
	
	var loadJsonFromDoc = function(files){
		var reader = new FileReader();
		var size = files[0].size;
		var chunk_size = Math.pow(2, 13);
	    var chunks = [];
	    var offset = 0;
	    var bytes = 0;

		reader.onloadend = function (e) {
			
			if(e.target.readyState == FileReader.DONE){
				var chunk = e.target.result;
				bytes += chunk.length;
				
				chunks.push(chunk);
				
				if(offset < size){
					offset += chunk_size;
					var blob = files[0].slice(offset , offset + chunk_size);
					reader.readAsText(blob);	
					
					if (offset > size){
						var content = chunks.join("");
						
						try {
							var jsonData = JSON.parse(content);	
							fileLoaded = jsonData;
						}
						catch(err) {
							try{
								var firstError = err.message;
								var jsonData = content.replace(/[\r]/g, '');
								var arrayJson = [];
								var dataSplitted = jsonData.split("\n");
								var i;
								for(var i in dataSplitted){
									if(dataSplitted[i] != "") {
										arrayJson.push(JSON.parse(dataSplitted[i]));
									}
								}
								fileLoaded=arrayJson;
							}catch(err){
								
								$('#response').html(firstError+'<br>'+ err.message);
								$('#returnAction').modal("show");
								return;
							}
						}							
					
					printJson();								
					}						
				}
			}
				
		}
		var blob = files[0].slice(offset, offset + chunk_size);
		reader.readAsText(blob);
		$('#progressBarModal').modal("show");
	}
	
	var printJson = function(){
		
		if(fileLoaded.length > 100){
			myVSJSONInput.setValue(JSON.stringify(fileLoaded.slice(0,20)));
		}else{
			myVSJSONInput.setValue(JSON.stringify(fileLoaded));
		}
		beautifyJson();
		
	};
	
	var beautifyJson = function() {
		myVSJSONInput.getAction('editor.action.formatDocument').run()
	};
	
	var generateJson = function(){
		var json = [
			{ "id" : "ONTOLOGY", "parent" : "#", "text" : "Ontology Rules"},
			{ "id" : "REST", "parent" : "#", "text" : "REST Rules" }
		];
		
		var availableOntologies = [];
		
		rules.forEach(function(rule){
			if(rule.type == 'ONTOLOGY'){
				if(availableOntologies.indexOf(rule.inputOntology == -1)){
					json.push({"id" : rule.inputOntology , "parent" : "ONTOLOGY", "text": rule.inputOntology, icon: "flaticon-network"});
					availableOntologies.push(rule.inputOntology);
				}
				var r = {"id" : rule.id ,"identification": rule.identification, "parent" : rule.inputOntology, "text": rule.identification, "icon": "flaticon-interface-5", "drl": rule.drl, "active": rule.active, "decisionTable" : rule.decisionTable};
				json.push(r);
			}else if(rule.type == 'REST'){
				var r = {"id" : rule.id , "identification": rule.identification, "parent" : "REST", "text": rule.identification, "icon": "flaticon-interface-5", "drl": rule.drl, "active": rule.active, "decisionTable" : rule.decisionTable };
				json.push(r);
			}
			
		});
		
		$('#rulesJsTree').jstree({
			'core' : {
			    'data' : json
			},
			"plugins" : [
			    "contextmenu", "state", "changed"
			], 
			'contextmenu': {               
                'items' : function(node) {
                	var id = node.original.identification;
                	var items = {
                		disableItem : {
                			label: node.original.active ? 'Disable' : 'Enable',
                			action: function(t){
                				enableDisableRule(id);
                			},
                			id: id
                		},
                		editItem: {
                			label: "Edit",
                			action: function(){
                				editRule(id);
                			}
                		},
                		removeItem: {
                			label: 'Remove',
                			action: function(){
                				removeRule(id);
                			}
                			
                		}
                	}
					return items;
                }
			}
		})
		.on("select_node.jstree", function (e, data) {
			var rule = data.instance.get_node(data.selected[0]).original;
			var drl = rule.drl;
			if(drl != null && data.selected.length) {
				$("#drlCode").show();
				$("#decisionTable-div").addClass("hidden");
				if(drl != null){
					editor.setValue(drl);
					editor.gotoLine(1);
					$('#updateBtn').prop('disabled', false);
					$('#testBtn').prop('disabled', false);
					$('#updateBtn').attr('data-id', rule.identification);
					$('#testBtn').attr('data-id', rule.identification);
					$('#updateBtn').attr('data-type', 'drl');
					$('#testBtn').attr('data-type', 'drl');
					$("#downloadBtn").prop('disabled', true);
					$("#downloadBtn").hide();
					$("#tableInfo").addClass("hidden");
				}
			}else if(drl==undefined && rule.decisionTable!=undefined){
				
				$("#drlCode").hide();
				$("#decisionTable-div").removeClass("hidden");
				$('#updateBtn').prop('disabled', false);
				$('#testBtn').prop('disabled', false);
				$('#updateBtn').attr('data-id', rule.identification);
				$('#testBtn').attr('data-id', rule.identification);
				$('#updateBtn').attr('data-type', 'table');
				$('#testBtn').attr('data-type', 'table');
				$("#downloadBtn").attr('data-id', rule.identification);
				$("#downloadBtn").prop('disabled', false);
				$("#downloadBtn").show();
				$("#tableInfo").removeClass("hidden");
				
			}
		})
		.on('state_ready.jstree', function () {
			postLoadFontColors();
		})
		.on('refresh.jstree', function () {
			postLoadFontColors();
		})
		.on('changed.jstree', function () {
			postLoadFontColors();
		})
		.on('open_node.jstree', function () {
			postLoadFontColors();
		})
		.on('close_node.jstree', function () {
			postLoadFontColors();
		});
		
		jsTreeData = json;
		
		
	}
	var enableDisableRule = function(id){
		$.ajax({
       	 	url : '/controlpanel/rule-domains/rule/' +id +'/active' ,  
       	 	headers: headersObj,
            type : 'PUT',
            contentType: 'text/plain'
        }).done(function(data) {
        	jsTreeData.forEach(function(rule){
        		if(rule.identification == id){
        			rule.active = !rule.active;
        		}
        	});
        	reloadJsTree();
        }).fail(function(error) {
        	$.alert({
				title : 'ERROR',
				type : 'red',
				theme : 'light',
				content : error.responseText
			})
			
		});
	}
	
	var editRule = function(id){
		navigateUrl('/controlpanel/rule-domains/' + domainId + '/rule/' + id);
	}
	var removeRule = function(id){
		$.ajax({
       	 	url : '/controlpanel/rule-domains/rule/' +id  ,  
       	 	headers: headersObj,
            type : 'DELETE',
        }).done(function(data) {
        	var p;
        	jsTreeData.forEach(function(rule, i){
        		if(rule.identification == id){
        			p= i;
        		}
        	});
        	jsTreeData.splice(p, 1);
        	reloadJsTree();
        }).fail(function(error) {
        	$.alert({
				title : 'ERROR',
				type : 'red',
				theme : 'light',
				content : error.responseText
			})
			
		});
	}
	
	var reloadJsTree = function(){
		$('#rulesJsTree').jstree(true).settings.core.data = jsTreeData;
		$('#rulesJsTree').jstree(true).refresh();
	}
	
	var postLoadFontColors = function(){
		jsTreeData.forEach(function(rule){
			if(rule.active!=null && !rule.active)
				$('#'+rule.id+'_anchor').css('color','grey')
		})
	}
	// Public API
	return {
		init: init,
		loadJsonFromDoc: function(files){
			loadJsonFromDoc(files);
		}
		
	};
	
})();

$(document).ready(function() {	
	
	Rules.List.init();

});
