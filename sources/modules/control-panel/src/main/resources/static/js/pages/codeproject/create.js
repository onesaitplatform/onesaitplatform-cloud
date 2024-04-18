var Codeproject = Codeproject || {};



Codeproject.Create = (function(){
	"use-strict";
	
	
	//var mountableModel = exists.codeproject ? '' : $('#table_parameters').find('tr.parameters-model')[0].outerHTML;
	var experimentList = '/controlpanel/modelsmanager/ajax-api/2.0/preview/mlflow/experiments/list';
	var experimentById = '/controlpanel/modelsmanager/ajax-api/2.0/preview/mlflow/experiments/get?experiment_id=';
	var experimentRunsSearch = '/controlpanel/modelsmanager/ajax-api/2.0/preview/mlflow/runs/search';
	var modelsManagerNotEnabled ='Models Manager not enabled';
	
	var init = function() {
		$('#file-zip').hide();
		$('#select-notebooks').hide();
		$('#select-dependency-architecture').hide();
		$('#select-module-architecture').hide();
		$('#project-structure').hide();
		//$('#jenkisjoburl').hide();
		$("#btn-cancel").on('click', function (e) {
			e.preventDefault();
			window.location = '/controlpanel/codeproject/list';
		});
		
		// INPUT MASK FOR microservice identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('form-codeproject');
		});
		
		$("#checkbox-publish-gitlab").on('change', function(){
			if( $(this).is(':checked')){
				$('#project-structure').hide();
				$('.only-new').show();
				$('.only-new-nb').show();
				$('#template').change();
			}else{
				$('#project-structure').show();
				$('.only-new-nb').hide();
				$('.only-new').hide();
				$('#template').val("").change();
			}
			
		});
		
		if($("#templateText").val()==''){
			$('#project-structure').show();
			$('#gitToHide').hide();
			$('#gitToHide2').hide();
			$('#mstemplatetext').hide();
		}
		/*
		if(exists.gitlab){
			$('#gitlab-configuration').hide();
			$("#checkbox-gitlab-default").on('change', function(){
				if( $(this).is(':checked'))
					$("#gitlab-configuration").hide();
				else
					$("#gitlab-configuration").show();
			});
		}
		
		if(exists.codeproject)
		{
			if(currentCaas=="RANCHER")
			{	
				$('#caas-openshift-configuration').hide();
				$('#caas-rancher-configuration').show();
			}	
			else
			{	
				$('#caas-rancher-configuration').hide();
				$('#caas-openshift-configuration').show();
			}
		}
		*/
		$('#template').on('change', function(){
			//var keytemplate = codeprojectCreateJson.optionstemplate[this.value];
			/*
			if(keytemplate != 'IOT_CLIENT_ARCHETYPE')
				$('#select-ontologies').hide();
			else
				$('#select-ontologies').show();
				
			if(keytemplate != 'NOTEBOOK_ARCHETYPE' )
				deactivateNotebookArchetype();
			else
				activateNotebookArchetype();
			if(keytemplate == 'IMPORT_FROM_ZIP')
				$('#file-zip').show();
			else
				$('#file-zip').hide();
			
			if(keytemplate == 'IMPORT_FROM_GIT')
				$('#git-template').show();
			else
				$('#git-template').hide();
			
			if(keytemplate === 'MLFLOW_MODEL' ){
				$('.experiments-divs').removeClass('hide');
				$('.no-experiments-divs').addClass('hide');
				$('#port-div').addClass('hide');
				loadExperiments();
			}else{
				$('.experiments-divs').addClass('hide');
				$('.executions-divs').addClass('hide');
				$('.no-experiments-divs').removeClass('hide');
				$('#port-div').removeClass('hide');
			}
			
			if(keytemplate != 'ARCHITECTURE_ARCHETYPE' )
			{
				$('#select-dependency-architecture').hide();
				$('#select-module-architecture').hide();
			}
			else
			{
				$('#select-dependency-architecture').show();
				$('#select-module-architecture').show();
			}
			*/
		})
		
		$('#btn-codeproject-upload').off().on('click',function(){
			$('#btn-codeproject-file').click();
		})
		$('#experiments').off().on('change', function(){
			if($('#experiments').val() !== ''){
				loadTableModels($('#experiments').val());
			}
		})
		
		handleValidation();
	}
	
	var loadExperiments = () => {
		fetch(experimentList,{
			  credentials: "same-origin"
		})
		.then(r => {
			if(r.ok){
				return r.json()
			}else{
				throw Error();
			}
		})
		.then(r => {
			reloadExperimentsSelect(r);
		})
		.catch(e => {
			toastr.info(messagesForms.operations.genOpError, modelsManagerNotEnabled)
			//DELETE ME
			let mockUp = {
				  "experiments": [
					    {
					      "experiment_id": "0",
					      "name": "Default",
					      "artifact_location": "onesait-platform://ce2f714aff5d4c3ea191eda4cd6ff1cc@moonwalker.onesaitplatform.com/0",
					      "lifecycle_stage": "active"
					    },
					    {
					      "experiment_id": "1",
					      "name": "LR60f971a889bff979d4856426",
					      "artifact_location": "onesait-platform://ce2f714aff5d4c3ea191eda4cd6ff1cc@moonwalker.onesaitplatform.com/1",
					      "lifecycle_stage": "active"
					    }
					  ]
					};
			reloadExperimentsSelect(mockUp);
			//DELETE ME
		})
	}
	var reloadExperimentsSelect = (experiments) =>{
		/*
		$('#experiments option').each(function() {
		   $(this).remove();
		});
		$('#experiments').append(`<option value="">${mlflowSelect}</option>`)
		experiments.experiments.forEach(function(exp){
			$('#experiments').append(`<option value="${exp.experiment_id}">${exp.name}</option>`)
		})
		$('.selectpicker').selectpicker('refresh')
		*/
	} 
	var loadTableModels = (id) =>{
		/*
		$('.executions-divs').removeClass('hide');
		if ($('#parameters').attr('data-loaded') === 'true'){
			$('#table_parameters > tbody').html("");
			$('#table_parameters > tbody').append(mountableModel);
		}
		let idsArr = []
		idsArr.push(id)
		let payload = {"experiment_ids": idsArr ,"filter":"","run_view_type":"ACTIVE_ONLY","max_results":100,"order_by":[],"page_token":null};
		
		fetch(experimentRunsSearch,{
			method: 'POST',
			body: JSON.stringify(payload),
			headers: {
				'Content-type' : 'application/json'
			}
		})
		.then(r => {
			if(r.ok){
				return r.json()
			}else{
				throw Error()
			}
		})
		.then(r => generateRunsTable(r))
		.catch(e =>{
			toastr.info(messagesForms.operations.genOpError, modelsManagerNotEnabled)
			generateRunsTable(mockedRuns);
		})
		*/
	}
	
	var generateRunsTable = (runs) =>{
		/*
		//DELETE ME
		//let parameters = [{'rundate':'2022-02-24 18:38:51','runid':'52e543cea6b4427b805e0163868efe9d','runname':'-','source':'linear_regression_mlflow','models':'ElasticnetWineModel/1','version':'9831b0','user':'notebook'},{'rundate':'2022-02-23 18:00:00','runid':'e0163868e42e543cea6b4427b805fe9d','runname':'-','source':'linear_regression_mlflow','models':'ElasticnetWineModel/1','version':'9831b0','user':'notebook'},{'rundate':'2022-01-29 18:00:00','runid':'cea6b4427be0163868e42e543805fe9d','runname':'-','source':'linear_regression_mlflow','models':'ElasticnetWineModel/1','version':'9831b0','user':'notebook'}]
		//DELETE ME
		let parameters = [];
		runs.runs.forEach(function(run){
			let dateFormatted = new Date(parseFloat(run.info.start_time)).toISOString().replaceAll(/T/g, ' ').replaceAll(/Z/g, '');
			let source = '-';
			let version = '-';
			let runname = '-';
			let gitUrl = '';
			let runid = run.info.run_id;
			let user = run.info.user_id;
			let status = run.info.status;
			run.data.tags.forEach(function(tag){
				if(tag.key === 'mlflow.source.name'){
					source = tag.value.replaceAll(/file:\/\/\/tmp\//g, '');
				}
				if(tag.key == 'mlflow.source.git.commit'){
					version = tag.value.substring(0,6);
				}
				if(tag.key == 'mlflow.source.git.repoURL'){
					gitUrl = tag.value;
				}
			})
			parameters.push({id: runid, 'rundate':dateFormatted, runid , runname, source, status, version, user, gitUrl })
		})
		$('#table_parameters').mounTable(parameters,{
			model: '.parameters-model',
			noDebug: false							
		});
		$('#parameters').removeClass('hide');
		$('#parameters').attr('data-loaded',true);
		addCheckboxListeners();
		*/
	}
	
	var addCheckboxListeners = () =>{
		
		$('.run-checkbox').off().on('change', function(){
			let current = $(this).closest('tr').find("input[name='id\\[\\]']").val();
			$('.run-checkbox').each(function(i, checkbox){
				let next = $(this).closest('tr').find("input[name='id\\[\\]']").val();
				if(checkbox.checked && next !== current){
					checkbox.checked = false;
				}
			})
		})
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
			$(this).val( 'default' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
			$(this).change();
		});
		
		// CLEANING PORT INPUT
		$('#port').val('30000');
		
		
		// CLEANING CHECKS
		$('#checkbox-publish-gitlab').prop('checked', true);
		$('#checkbox-publish-gitlab').change();
	}
	
	
	function activateNotebookArchetype() {
		$('#select-notebooks').show();
		document.getElementById('port').value = 8080;
		document.getElementById('port').readOnly = true;
		document.getElementById('context-path').value = "/" + document.getElementById('identification').value;
		$('#port-div').hide();
	}
	
	$('#caas-select').on('change', function(){
		if(document.getElementById('caas-select').value=="RANCHER")
		{
			document.getElementById("caas-rancher-configuration").style.display = '';
			document.getElementById("caas-openshift-configuration").style.display = 'none';
		}
		else 
		{
			document.getElementById("caas-rancher-configuration").style.display = 'none';
			document.getElementById("caas-openshift-configuration").style.display = '';
		}
	});
	
	function deactivateNotebookArchetype() {
		$('#select-notebooks').hide();
		document.getElementById('port').value = 30000;
		document.getElementById('port').readOnly = false;
		document.getElementById('context-path').value = "";
		$('#port-div').show();
		
	}
	
	function setHiddenInputs(){
		$("#createGitlab").val($("#checkbox-publish-gitlab").is(':checked'));
		if(exists.gitlab)
			$("#defaultGitlab").val($("#checkbox-gitlab-default").is(':checked'));
		if(exists.jenkins)
			$("#defaultJenkins").val($("#checkbox-jenkins-default").is(':checked'));
		if(exists.caas)
			$("#defaultCaaS").val($("#checkbox-caas-default").is(':checked'));
		$('.run-checkbox').each(function(i, checkbox){
			let next = $(this).closest('tr').find("input[name='id\\[\\]']").val();
			let gitNext = $(this).closest('tr').find("input[name='gitUrl\\[\\]']").val();
			if(checkbox.checked){
				$('#modelRunId').val(next);
				$('#gitlab-repository-custom').val(gitNext);
			}
		})
		
	
	}
	
	function submitForm($form, action, method) {
		$form[0].submit();
	}
	
	var handleValidation = function() {
        var $form = $('#form-codeproject');
        var $error = $('.alert-danger');
        var $success = $('.alert-success');
		// set current language
		// TODO: Analizar -> currentLanguage = dashboardCreateReg.language || LANGUAGE;
        
        $form.validate({
            errorElement: 'span', 
            errorClass: 'help-block help-block-error',
            focusInvalid: false, 
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", 
			lang: currentLanguage,
            rules: {				
                identification: { required: true }
            },
            invalidHandler: function(event, validator) {
            	toastr.error(messagesForms.validation.genFormError,'');
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
            submitHandler: function(form) { 
            	setHiddenInputs();
            	toastr.success(messagesForms.validation.genFormSuccess,'');
				submitForm($form, $('#microservice-save-action').val(), $('#microservice-save-method').val());
			}
        });
    }	
	
	
	return {
		init: init
	};
})();


$(document).ready(function() {
	Codeproject.Create.init();
});
