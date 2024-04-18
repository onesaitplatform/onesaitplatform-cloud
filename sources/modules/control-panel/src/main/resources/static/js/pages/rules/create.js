var Rules = Rules || {};

Rules.Create = (function() {
	"use-strict";
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	var jsTreeData;
	headersObj[csrfHeader] = csrfToken;
	var defaultRule= "package com.minsait.onesait.platform.rulesengine;\n" + 
	"import com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper;\n" + 
	"global com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper input;\n" + 
	"global com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper output;\n" + 
	"/*\n" + 
	"Input JSON data is wrapped into an OntologyJsonWrapper\n" + 
	"Here are the methods that you can invoke\n" + 
	"\n" + 
	"input.getProperty(\"anyProperty\") -> gets a property from input JSON\n" + 
	"input.toJson() -> serializes input as a string\n" + 
	"\n" + 
	"Results are extracted from output variable which is also an OntologyJsonWrapper\n" + 
	"Here are the methods that you can invoke\n" + 
	"\n" + 
	"output.setRootNode(\"rootNode\") -> sets Json output root node for ontology validation\n" + 
	"output.setProperty(\"property\", anyValue) -> sets a new Property\n" + 
	"output.copyInputToOuput(input) -> copies al properties from input to output\n" + 
	"output.toJson() -> serializes output as a string\n" + 
	"*/\n" + 
	"dialect  \"mvel\"\n" + 
	"\n" + 
	"rule \"Assign role\"\n" + 
	"\n" + 
	"    when\n" + 
	"        eval( input.getProperty(\"currentSalary\") < 1000000 && input.getProperty(\"experienceInYears\") > 10 )\n" + 
	"    then\n" + 
	"    	\n" + 
	"        output.setProperty(\"role\", \"Manager\");\n" + 
	"end";
	var init = function() {

		
		editor = ace.edit("drlCode");
	    editor.setTheme("ace/theme/xcode");
	    editor.session.setMode("ace/mode/drools");
	    editor.setValue(DRL==null ? defaultRule : DRL);
	    editor.gotoLine(1);
	    
	    $('#form-rule').submit(function(e){
	    	$('#drl').val(editor.getValue());
	    });
	   
		
	};
	var changeRuleType = function(){
		var type = $('#type').val();
		if(type == 'ONTOLOGY')
			$('#ontologies').show();
		else
			$('#ontologies').hide();
		
	}
	
	
	// Public API
	return {
		init: init,
		changeRuleType: changeRuleType
		
	};
	
})();

$(document).ready(function() {	
	
	Rules.Create.init();

});
