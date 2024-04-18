var ProjectWizardController = function() {
	var selectedUsers =[];
	var gitlabConfig;
	var newEnvironment=true;
	var selectedModules = {}
	var urlRancherEnvs = '/controlpanel/project-wizard/getrancher';
	var urlOcpEnvs = '/controlpanel/project-wizard/getocp';
	var urlConfigurations ='/controlpanel/project-wizard/configs'
	var urlEnvs = '/controlpanel/project-wizard/environments';

	//Environment retrieving
	var getEnvironments = function(){
		$('#refreshIcon').addClass('la la-spinner la-spin la-2x');
		loadEnvironments();

	}

	var getRancherEnvs = function(){
		jQuery.post(urlRancherEnvs,{"configId" : $('#selectedConfigurations}').val()}, function(environments){
			populateEnvSelect(environments);
		},'json');
	}

	var getOpenshiftEnvs = function(){
		jQuery.post(urlRancherEnvs, function(environments){
			
		})
	}

	var loadEnvironments = function(){
		jQuery.post(urlEnvs, {"caas":$('#selectedCaaS').val(), "configId" : $('#selectedConfigurations').val()}, function(environments){
			populateEnvSelect(environments);
		});
	}

	var populateEnvSelect = function(environments){
		$('#refreshIcon').removeClass('la la-spinner la-spin la-2x');
		$("#selectedEnvironment optgroup option").each(function() {
			    $(this).remove();
		});		
		if(environments.length > 0){

			for (var i = environments.length - 1; i >= 0; i--) {
				$('#selectedEnvironment optgroup').append($('<option>', {
				    value: environments[i],
				    text: environments[i]
				}));
			}
			
			var divListEnvironment = $('#existingEnvironments');
			environmentComboActive(divListEnvironment.is(':visible'));
		}else{
			alert('No environments found');
		}
		
	}//End environment retrieving

	var environmentComboActive = function(boolean){
		var divListEnvironment = $('#existingEnvironments');
		if(boolean){
			divListEnvironment.addClass('hide');
			$('#newEnvironmentName').removeClass('hide');
			$('#newEnvironmentName').addClass('mandatory');
			$('#newEnvironment').val(true);
			newEnvironment=true;

		}else{
			divListEnvironment.removeClass('hide');
			$('#newEnvironmentDiv').addClass('hide');
			$('#newEnvironmentName').removeClass('mandatory');
			$('#newEnvironment').val(false);
			newEnvironment=false;  

		}
	}

	//get Configurations CaaS
	var loadConfigurations = function(){
		var caas = $('#selectedCaaS').val();
		$("#selectedEnvironment optgroup option").each(function() {
			    $(this).remove();
		});	
		if($('#selectedCaaS').val()!= 0){
			//hide combo if visible
			if($('#selectedConfigurations').is(':visible'))
				$('#selectedConfigurations').addClass('hide');
			//remove options
			$("#selectedConfigurations optgroup option").each(function() {
			    $(this).remove();
			});
			
			jQuery.post(urlConfigurations, {"caas":caas}, function(configurations){
				if(configurations.length > 0){
					for (var i = configurations.length - 1; i >= 0; i--) {
						$('#selectedConfigurations optgroup').append($('<option>', {
						    value: configurations[i].id,
						    text: '[' + configurations[i].environment + ']' + configurations[i].description
						}));
					}
					$('#selectedConfigurations').removeClass('hide');
				}else
					alert('No configurations found for ' + $('#selectedCaaS').text());
			},'json');
		}
			
	}
	

	var collectUsers = function(){
		selectedUsers =[];
		$('.selection-users').each(function(i,checkbox){
			if(checkbox.checked)
				selectedUsers.push(checkbox.value);
			});
		console.log("Selected users: ")
		selectedUsers.forEach(function(user){
			console.log(user);
		});
		$('#selectedUsers').val(selectedUsers);
	}
	
	var setGitlabConfig = function(){
		$('.selection-gitlab').each(function(i,checkbox){
			if(checkbox.checked)
				gitlabConfig = checkbox.value;
			});
		console.log("Gitlabconfig id: " + gitlabConfig)
		$('#gitlabConfigId').val(gitlabConfig);
	}

	var setEnvironment = function(){
		if(newEnvironment){
			$('#environment').val($('#newEnvironmentName').val());
		}else{
			$('#environment').val($('#selectedEnvironment').val());
		}

	}

	var setModules = function(){
		$('.selection-modules').each(function(i,checkbox){
			if(checkbox.checked){
				var mod = checkbox.value;
				var count = $('#'+checkbox.value +'Instances').val();
				//moduleObj={mod, count}
				selectedModules[mod] = parseInt(count);
			}
			});
		console.log(selectedModules);
		$('#selectedModules').val(JSON.stringify(selectedModules));
	}
	return{
		addUsersToSelection : function(){
			collectUsers();
		},
		isNewEnv : function(){
			return newEnvironment;
		},
		loadConfigurations : function(){
			return loadConfigurations();
		},
		init : function(){
			$('#newEnvironment').val(true);
			$('.new-environment').on('click', function(){
				var divNewEnvironment = $('#newEnvironmentDiv');
				if(divNewEnvironment.is(':visible')){
					divNewEnvironment.addClass('hide');
					$('#newEnvironmentName').removeClass('mandatory');
					$('#newEnvironment').val(false);
					newEnvironment=false;
				}else{
					divNewEnvironment.removeClass('hide');
					$('#existingEnvironments').addClass('hide');
					$('#newEnvironmentName').addClass('mandatory');
					$('#newEnvironment').val(true);
					newEnvironment=true;
				}
				
			});
			$('.btn-list-environments').on('click', function(){
				getEnvironments();
				//mock-up will be replaced by ajax for retrieving existing ranches/ocp env/projs
				
				
			});
			
			$('.btn-collect-users').on('click', function(){
				collectUsers();
			});

			$('.btn-gitlab-config').on('click', function(){
				setGitlabConfig();
			});

			$('.btn-environments').on('click', function(){
				setEnvironment();
			});
			
			$('.btn-submit').on('click', function(){
				setModules();
				$('#form').submit();
			});
			
			// selection of modules
			$('.selection-modules').on('click',function(){
				if( $(this).is(':checked')){ 
					console.log('modulo chequeado');
					$(this).closest('div.panel-heading').css("border","1px solid #2e6c99");
				}
				else {
					console.log('modulo deschequeado');
					$(this).closest('div.panel-heading').css("border","none");  
				}
			});
			
		}
		
	}
}();

jQuery(document).ready(function() {
	
	ProjectWizardController.init();
});
