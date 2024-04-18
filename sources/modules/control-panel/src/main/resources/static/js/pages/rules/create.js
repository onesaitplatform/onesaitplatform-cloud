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
	"import com.minsait.onesait.platform.rulesengine.model.QueryWrapper;\n" + 
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
	"        input: OntologyJsonWrapper()\n" +
	"        eval( input.getProperty(\"currentSalary\") < 1000000 && input.getProperty(\"experienceInYears\") > 10 )\n" + 
	"    then\n" + 
	"    	\n" + 
	"        output.setRootNode(\"roles\")\n" +
	"        output.setProperty(\"role\", \"Manager\");\n" + 
	"end";
	var init = function(Data) {

		editor = ace.edit("drlCode");
	    editor.setTheme("ace/theme/xcode");
	    editor.session.setMode("ace/mode/drools");
	    editor.setValue(DRL==null ? defaultRule : DRL);
	    editor.gotoLine(1);
	  
		$("#btn-save").on('click',function(event){
			event.preventDefault();
			if($("#loadDecisionTable").is(":checked")){
	    		if($("#buttonLoadFile").prop('files')[0]==undefined){
					$('#file_name').parent().addClass('has-error');
					$('#file_nameerror').removeClass('hide').addClass('help-block-error font-red');
		        	toastr.error(rulesCreateJson.messages.filerequired);
	    		}
	    	}else{
    			$('#drl').val(editor.getValue());
	    	}
			if (validForm()){
				var form = $('#form-rule');
				form.attr("action", "?" + csrfParameter + "=" + csrfValue)
				form.submit();
			} else {
				toastr.error(rulesCreateJson.messages.validationKO);
			}

		});
	   
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('form-rule');
		});
		
	    $('#btn-cancel').off().on('click',function(){
	    	window.location = window.location.href+ 's';
	    });
	    
		// INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			if ($('#' + event.target.id).val() === '') { 
				$('#' + event.target.id).parent().addClass('has-error');
				$('#' + event.target.id + 'error').removeClass('hide').addClass('help-block-error font-red');
			} else { 
				$('#' + event.target.id).parent().removeClass('has-error');
				$('#' + event.target.id + 'error').addClass('hide').removeClass('help-block-error font-red');
			}
		});		
		
		$("#loadDecisionTable").on("click", function(){
			if($("#loadDecisionTable").is(":checked")){
				$("#drl-div").addClass("hidden");
				$("#decisionTable-div").removeClass("hidden");
				$("#downloadBtn").show();
			}else{
				$("#drl-div").removeClass("hidden");
				$("#decisionTable-div").addClass("hidden");
				$("#downloadBtn").hide();
			}
		})
		
		$('#downloadBtn').on('click',function(e){
			e.preventDefault();
	    	window.location.href = "/controlpanel/rule-domains/rule/" + ruleIdentification + "/downloadTable";
	    })
	    
	    if(isDecisionTable){
	    	$("#drl-div").addClass("hidden");
			$("#decisionTable-div").removeClass("hidden");
			$("#downloadBtn").show();
		}else{
			$("#drl-div").removeClass("hidden");
			$("#decisionTable-div").addClass("hidden");
			$("#downloadBtn").hide();
		}
		
		$("#buttonLoadFile").on("change", function(){
	  		var file = $('input[type=file]').val().split('\\').pop();
	  		if(file!=null){
	  			$("#file_name").val(file);
	  			$("#file_name").parent().removeClass('has-error');
	 	}
	  	});
		
	};
	
	var validForm = function (){
		if ($('#identification').val() === '') { 
			$('#identification').parent().addClass('has-error');
			$('#identificationerror').removeClass('hide').addClass('help-block-error font-red');
			return false;
		} else { 
			$('#identification').parent().removeClass('has-error');
			$('#identificationerror').addClass('hide').removeClass('help-block-error font-red');
			return true;
		}
		
	}
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file,input:text, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remote" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		// CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');
		
		editor.setValue(defaultRule);
	    editor.gotoLine(1);
	    
	    // CLEANING ERRORS
	    
		$('#identification').parent().removeClass('has-error');
		$('#identificationerror').addClass('hide').removeClass('help-block-error font-red');
		$("#file_name").parent().removeClass('has-error');
		$("#file_nameerror").addClass('hide').removeClass('help-block-error font-red');
		
		$("#drl-div").removeClass("hidden");
		$("#decisionTable-div").addClass("hidden");
		$("#downloadBtn").hide();

	}
	
	var changeRuleType = function(){
		var type = $('#type').val();
		if(type == 'ONTOLOGY')
			$('#ontologies').show();
		else
			$('#ontologies').hide();
		
	}
	
	var loadFromDoc = function(files){
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
					
					
						var content = chunks.join("");//.replace(/\"/g, '');
						try {
							fileLoaded = csvTojs(content);
						}
						catch(err) {
							$('#response').text(err);
							$('#returnAction').modal("show");
							return;
						}
						//printJson();
					
					
				}
				$('#drl').val(JSON.stringify(fileLoaded));
				
			//	progressBarFileUpload(offset,size);
			}
		
			
		}

			
			var blob = files[0].slice(offset, offset + chunk_size);
			reader.readAsText(blob);
		//	$('#progressBarModal').modal("show");
	}
	
	
	// Public API
	return {
		init: init,
		changeRuleType: changeRuleType,
		loadFromDoc : loadFromDoc
		
	};
	
})();

$(document).ready(function() {	

	Rules.Create.init(rulesCreateJson);

});
