var Rules = Rules || {};

Rules.List = (function() {
	"use-strict";
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
	    
	   
	    $('#updateBtn').on('click',function(){
	    	var id = $(this).data('id');
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
				var r = {"id" : rule.id ,"identification": rule.identification, "parent" : rule.inputOntology, "text": rule.identification, "icon": "flaticon-interface-5", "drl": rule.drl, "active": rule.active};
				json.push(r);
			}else if(rule.type == 'REST'){
				var r = {"id" : rule.id , "identification": rule.identification, "parent" : "REST", "text": rule.identification, "icon": "flaticon-interface-5", "drl": rule.drl, "active": rule.active };
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
                	//only drl nodes
                	if(node.original.drl == null)
                		return false;
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
			if(data.selected.length) {
				if(drl != null){
					editor.setValue(drl);
					editor.gotoLine(1);
					$('#updateBtn').prop('disabled', false);
					$('#testBtn').prop('disabled', false);
					$('#updateBtn').attr('data-id', rule.identification);
					$('#testBtn').attr('data-id', rule.identification);
				}
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
       	 	data: editor.getValue(),
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
		init: init
		
	};
	
})();

$(document).ready(function() {	
	
	Rules.List.init();

});
