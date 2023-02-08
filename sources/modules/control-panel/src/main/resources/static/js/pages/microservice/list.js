var Microservice = Microservice || {};

Microservice.List = (function() {
	"use-strict";
	var mountableModel = $('#table_parameters').find('tr.parameters-model')[0].outerHTML;
	var csrfHeader = headerJson.csrfHeaderName;
	var csrfToken = headerJson.csrfToken;
	var headersObj = {};
	headersObj[csrfHeader] = csrfToken;
	var init = function() {
		 setInterval(reloadMicroserviceTable,30000);
		 setTimeout(reloadTooltips, 100);
		// Create event
		$('#btn-report-create').on('click', function (e) {
			e.preventDefault(); 
			window.location = '/controlpanel/microservices/create';
		})		
	};
	//$('.tooltips').tooltip();
	
	var dtRenderOptions = function (data, type, row) {
		return '<div class="grupo-iconos text-center">'
		+ '<span data-id="' + row.id + '" data-contextPath="' + row.contextPath + '" data-caas="' + row.caas + '" data-deployed="' + row.deployed + '" data-url="' + row.deploymentUrl + '" class="btn-url btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genUrl+'"><i class="la la-eye font-hg"></i></span></a>'
		+ '<span data-id="' + row.id + '" class="icon-microservice-edit btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genUpdate+'"><i class="icon-edit"></i></span>'
		+ '<a target="_blank" href="'+row.deploymentUrl+'"><span data-id="' + row.id + '" class="btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genView+'"><i class="icon-url"></i></span></a>'
		+ '<span data-id="' + row.id + '" class="icon-microservice-trash btn btn-xs btn-no-border icon-on-table color-red tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.genDelete+'"><i class="icon-delete"></i></span>'																											
		+ '</div>';
	};
	
	var dtRenderCICD = function (data, type, row) {
		var div = '<div class="grupo-iconos text-center">'
			+ '<span data-id="' + row.id + '" class="build-btn btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.build+'"><i class="la la-gavel font-hg"></i></span>';																																																			
		if(row.deployed){
			div+='<span data-id="' + row.id + '" data-caas="'+ row.caas +'" class="upgrade-btn btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.upgrade+'"><i class="icon-upload"></i></span>'
				+'<span data-id="' + row.id + '" data-caas="'+ row.caas +'" class="stop-btn btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.stop+'"><i class="icon-stop"></i></span>'
				+ '</div>';
		}else{
			div+= '<span data-id="' + row.id + '" data-caas="'+ row.caas +'" class="deploy-btn btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.deploy+'"><i class="la la-rocket font-hg"></i></span>'																											
			+ '</div>';
		}
		return div;
		
	};
	
	var dtRenderLinks = function (data,type,row){
		if(data == null)
			return '<div class="text-center" ><span th:text="0" style="display:none" ></span><i class="la la-times-circle-o text-danger  font-hg"></i></div>';
		else
			return '<div class="text-center" ><a href="'+data+'" target="_blank"><span class="btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.go+'"><i class="icon-url"></i></span></a></div>';
	}
	
	var dtRenderLinksJenkins = function (data,type,row){
		if(data == null)
			return '<div class="text-center" ><span th:text="0" style="display:none" ></span><i class="la la-times-circle-o text-danger  font-hg"></i></div>';
		else{
			 var html = '<div class="text-center" ><a href="'+data+'" target="_blank"><span class="btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.go+'"><i class="icon-url"></i></span></a>';
			if(row.lastBuild != null)
				html +='<span data-id="' + row.id + '" class="btn-jenkins-building btn btn-xs btn-no-border icon-on-table color-blue tooltips" data-container="body" data-placement="bottom" data-original-title="'+constants.jenkinsbuilding+'"><i class="fa fa-spinner fa-spin font-hg"></i></span>';
			html+='</div>';
			return html;
		}
	}
	
	function initCompleteCallback(settings, json) {
		
		initTableEvents();
	
	}
	
	
	
	function reloadMicroserviceTable() {
		
		reloadDataTable();
	}
	
	function reloadDataTable() {		
		var csrfToken = $("meta[name='_csrf']").attr("content");
		var csrfHeader = $("meta[name='_csrf_header']").attr("content"); 
		var headersObj = {};
		headersObj[csrfHeader] = csrfToken;		
		$.ajax({
       	 	url : '/controlpanel/microservices/data',  
       	 	headers: headersObj,
       	 	contentType:"application/json; charset=utf-8",
       	 	dataType:"json",
            type : 'GET'
        }).done(function(data) {
        	createRows(data);
        	//$('.tooltips').tooltip('destroy');
    		//$('.tooltips').tooltip();
    		//$('#microservices').DataTable();
    		initTableEvents();
    		reloadTooltips();
        });
		
	}
	

	function createRows(data){
		if(data!== null && typeof data!='undefined' && data.length>0){
			$('#microservices tbody').empty();
			for(var i = 0; i<data.length;i++){
				var tr = '<tr>';
				tr  +=createTrContent(data[i]);
				tr +='</tr>';
				$('#microservices tbody').append(tr);
			}
		}
		return null;
	}
	
	function createTrContent(data){		

		var html = '<td class="text-left hide">'+data.id+'</td>';
		html += '<td class="text-left ">'+data.name+'</td>';
		html += '<td class="text-left ">'+data.owner+'</td>';
		html += '<td>'+dtRenderLinks(data.gitlab,null,data)+'</td>';
		html += '<td>'+dtRenderLinksJenkins(data.jenkins,null,data)+'</td>';
		html += '<td>'+dtRenderLinks(data.caasUrl,null,data)+'</td>';
		html += '<td>'+dtRenderOptions(data,null,data)+'</td>';
		html += '<td>'+dtRenderCICD(data,null,data)+'</td>';
		return html;

	}
	

	function reloadTooltips(){
		$('.tooltips').tooltip('destroy');
		$('.tooltips').tooltip();
	}
	
	var buildWithParameters = function(){
		var id = $('#current-microservice').val();
		var elements =  $('#table-body').find('tr');
		var parametersArray = [];
		elements.each(function(){
			var name = $(this).find("input[name='name\\[\\]']").val();
			var value = $(this).find("input[name='value\\[\\]']").val();
			var parameter = {"name":name, "value":value};
			parametersArray.push(parameter);
		});
		$('#pulse').attr('class', 'col-md-12');
		$.ajax({
       	 	url : 'jenkins/build/' +id ,  
       	 	headers: headersObj,
       	 	contentType:"application/json; charset=utf-8",
       	 	dataType:"json",
       	 	data: JSON.stringify(parametersArray),
            type : 'POST'
        }).done(function(data) {
			$('#pulse').attr('class', 'col-md-12 hide');
        	reloadMicroserviceTable();
        	$.alert({
				title : 'Info',
				theme : 'light',
				content : 'Jenkins pipeline was sent to the queue with queue id: '+data
			});
        }).fail(function(error) {
        	
        });
		

	}
	
	var getHosts = function(obj){
		var id = $(obj).data('id');
		var environment = $('#environment').val();
		if(environment != ''){
			$('#pulse').attr('class', 'col-md-12');
			var url_hosts = 'deploy/' +id +'/parameters?hosts=true&environment='+escape(environment);
			$('#wrapper-deployment-fragment').load(url_hosts, function( response, status, xhr ) {
				  if ( status == "error" ) {
					    var msg = "Sorry but there was an error: ";
					    $( "#error" ).html( msg + xhr.status + " " + xhr.statusText );
				  }else{
					  $('.modal-backdrop').remove()
					 $('#wrapper-deployment-fragment').html(response);
					 $('#parametersDeployModal').modal('show');
					 $('#environment').val(environment);
					 
				  }
				
			});
			
		}
	}
	
	var deployWithParameters = function(obj){
		var id = obj.dataset.id;
		var environment = $('#environment').val();
		var worker = $('#worker').val();
		var stack = $('#stack').val();
		var onesaitServerUrl = $('#onesaitServerUrl').val();
		var dockerImageUrl = $('#dockerImageUrl').val();
		var continueDeploy = true;
		
		
		if(environment == ''){
			$('#environment').closest('td').addClass('has-error');
			continueDeploy = false;
		}
		if(worker == ''){
			$('#worker').closest('td').addClass('has-error');
			continueDeploy = false;
		}
		if(dockerImageUrl == ''){
			$('#dockerImageUrl').closest('td').addClass('has-error');
			continueDeploy = false;
		}
		if(continueDeploy){
			$('#environment').closest('td').removeClass('has-error');
			$('#worker').closest('td').removeClass('has-error');
			$('#dockerImageUrl').closest('td').removeClass('has-error');
		}else{
			return;
		}
		var payload = {'environment':environment, 'worker': worker, 'onesaitServerUrl': onesaitServerUrl , 'dockerImageUrl': dockerImageUrl, 'stack':stack};
		$('#pulse').attr('class', 'col-md-12');
		$.ajax({
       	 	url : 'deploy/' +id  ,  
       	 	headers: headersObj,
       	 	data: payload,
            type : 'POST'
        }).done(function(data) {
        	$('.modal-backdrop').remove()
        	$('#parametersDeployModal').modal('hide');
        	
        	reloadMicroserviceTable();	
        	$.alert({
				title : 'Info',
				theme : 'light',
				content : 'Microservice deployed'
			});
        }).fail(function(error) {
        	
        });
		

	}
	
	var deployWithParametersOC = function(obj){
		var id = obj.dataset.id;
		var project = $('#environment').val();
		var onesaitServerUrl = $('#onesaitServerUrl').val();
		var dockerImageUrl = $('#dockerImageUrl').val();
		var worker = $('#worker').val();
		if(typeof(worker) === "undefined")
			worker = "defaultValue";
		var continueDeploy = true;
		
		
		if(project == '' || project == "undefined"){
			$('#project').closest('td').addClass('has-error');
			continueDeploy = false;
		}
		if(dockerImageUrl == ''){
			$('#dockerImageUrl').closest('td').addClass('has-error');
			continueDeploy = false;
		}
		if(onesaitServerUrl == ''){
			$('#onesaitServerUrl').closest('td').addClass('has-error');
			continueDeploy = false;
		}
		if(continueDeploy){
			$('#project').closest('td').removeClass('has-error');
			$('#dockerImageUrl').closest('td').removeClass('has-error');
		}else{
			return;
		}
		var payload = {'environment':project, 'worker': worker, 'onesaitServerUrl': onesaitServerUrl , 'dockerImageUrl': dockerImageUrl, 'stack':""};
		$('#pulse').attr('class', 'col-md-12');
		$.ajax({
       	 	url : 'deploy/' +id  ,  
       	 	headers: headersObj,
       	 	data: payload,
            type : 'POST'
        }).done(function(data) {
        	$('.modal-backdrop').remove()
        	$('#parametersDeployModal').modal('hide');
        	
        	reloadMicroserviceTable();	
        	$.alert({
				title : 'Info',
				theme : 'light',
				content : 'Microservice deployed'
			});
        }).fail(function(error) {
        	
        });
		

	}
	
	var upgrade = function(obj){
		var id = obj.dataset.id;
		var dockerImageUrl = $('#dockerImageUrlUpgrade').val();
		var continueDeploy = true;
		if(dockerImageUrl == ''){
			$('#dockerImageUrlUpgrade').closest('td').addClass('has-error');
			continueDeploy = false;
		}
		if(continueDeploy)
			$('#dockerImageUrlUpgrade').closest('td').removeClass('has-error');
		else
			return;
		
		
		
		
		var env = {};
		var elements = $(".env-tr");
		elements.each(function(){
			var name = $(this).find("input[name='envName\\[\\]']").val();
			var value = $(this).find("input[name='envValue\\[\\]']").val();
			env[name]=value;
		});
		env = JSON.stringify(env);
		var payload = {'dockerImageUrl':dockerImageUrl, 'env':env};
		$('#pulse').attr('class', 'col-md-12');
		$.ajax({
       	 	url : 'upgrade/' +id  ,  
       	 	headers: headersObj,
       	 	data: payload,
            type : 'POST'
        }).done(function(data) {
			$('#pulse').attr('class', 'col-md-12 hide');
        	$('#parametersUpgradeModal').modal('hide');
        	reloadMicroserviceTable();	
        	$.alert({
				title : 'Info',
				theme : 'light',
				content : 'Microservice upgraded'
			});
        }).fail(function(error) {
        	
        });
	}
	
	var removeEnvVar = function(obj){
		$(obj).closest('tr').remove();
	}
	
	var addEnvVar = function(){
		var tr = '<tr class="env-tr">'+
				'<td>'+
					'<input type="text" name="envName[]"   class="form-control" placeholder="ENV VAR Name"/>'+
				'</td>'+
				'<td>'+
					'<input type="text" name="envValue[]"  class="form-control" placeholder="ENV VAR Value"/>'+
				'</td>'+
				'<td class="text-center">'+
					'<div class="btn btn-outline  btn-sm blue tooltips btn-add-env" data-container="body" data-placement="top" data-original-title="Environment Variable" onclick="Microservice.List.removeEnvVar(this)">'+
						'<input type="checkbox" id="groupby_check" style="display:none; margin:0px"/><i class="fa fa-minus"></i>'+
					'</div>'+
				'</td>'+
			'</tr>';
		
		$('#table_deployment_parameters tbody').append(tr);
	}
	
	function initTableEvents() {
		//$('.tooltips').tooltip();
		$('.build-btn').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				 $.ajax({
			       	 	url : 'jenkins/parameters/' +id ,  
			            type : 'GET'
			        }).done(function(data) {
			        	var parameters = data;
			        	if(parameters == null || parameters.length == 0)
			        		return;
			        	else{
			        		if ($('#parameters').attr('data-loaded') === 'true'){
			    				$('#table_parameters > tbody').html("");
			    				$('#table_parameters > tbody').append(mountableModel);
			    			}
			        		
			        		$('#table_parameters').mounTable(parameters,{
			    				model: '.parameters-model',
			    				noDebug: false							
			    			});
			        		$('#parameters').removeClass('hide');
			    			$('#parameters').attr('data-loaded',true);
			    			$('#parametersModal').modal('show');
			    			$('#current-microservice').val(id);
			        		
			        	}
			        		
			        	
			        }).fail(function(error) {
			        	
			        });
				
			});
		})
		
		$('.btn-url').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				var contextpath = $(this).data('contextpath');
				var templatee = $(this).data('template');
				var caas = $(this).data('caas');
				var deployed =  $(this).data('deployed');
				var openshiftUrl = $(this).data('url');
				
				showMicroserviceURLDialog(contextpath, templatee, caas, openshiftUrl, deployed)
				
			});
		})
		
		$('.deploy-btn').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				
					$('#wrapper-deployment-fragment').load('deploy/' +id +'/parameters', function( response, status, xhr ) {
					  if ( status == "error" ) {
						    var msg = "Sorry but there was an error: ";
						    $( "#error" ).html( msg + xhr.status + " " + xhr.statusText );
					  }else
						 $('#parametersDeployModal').modal('show');
					
				});
			});
		});
		
		$('.stop-btn').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				$.ajax({
		       	 	url : 'stop/' +id  ,  
		       	 	headers: headersObj,
		            type : 'POST'
		        }).done(function(data) {
		        	$.alert({
						title : 'Info',
						theme : 'light',
						content : 'Microservice stopped'
					});
		        }).fail(function(error) {
		        	$.alert({
						title : 'Error',
						theme : 'light',
						content : error
					})
					
				});
			});
		});
		
		$('.btn-jenkins-completed').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				$.ajax({
		       	 	url : 'jenkins/completed/' +id  ,  
		       	 	headers: headersObj,
		            type : 'GET'
		        }).done(function(data) {
		        	$.alert({
						title : 'Info',
						theme : 'light',
						content : 'Build Finished!'
					});
		        	reloadMicroserviceTable();
		        }).fail(function(error) {
		        	$.alert({
						title : 'Error',
						theme : 'light',
						content : error
					})
					
				});
			});
		});
		
		$('.upgrade-btn').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				$('#pulse').attr('class', 'col-md-12');
				$('#wrapper-deployment-fragment').load('deploy/' +id +'/parameters?upgrade=true', function( response, status, xhr ) {
				  if ( status == "error" ) {
					    var msg = "Sorry but there was an error: ";
					    $( "#error" ).html( msg + xhr.status + " " + xhr.statusText );
				  }else{
					 $('#parametersUpgradeModal').modal('show');
					 $('#pulse').attr('class', 'col-md-12 hide');
				  }
				
				});
			});
		});
		
		
		$('.icon-microservice-edit').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				window.location = '/controlpanel/microservices/update/' + id;
			});
		});
		

		$('.icon-microservice-trash').each(function() {
			$(this).off().on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id'); 
				deleteMicroserviceDialog(id);
			});
		});
		
		$('.btn-microservice-reload').off().on('click', function (e) {
			e.preventDefault(); 
			Microservice.List.reloadMicroserviceTable();
		});
		
		
		

		
		
	}
	
	var deleteMicroserviceDialog = function(id) {
		$.confirm({
			title: headerJson.microserviceDelete,
			theme: 'light',
			columnClass: 'medium',
			content: constants.deleteContent,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: headerJson.btnClose,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				Ok: {
					text: headerJson.btnEliminar,
					btnClass: 'btn btn-primary',
					action: function() { 
						$.ajax({ 
						    url : id,
						    headers: headersObj,
						    type : 'DELETE'
						}).done(function( result ) {							
							reloadMicroserviceTable();
						}).fail(function( error ) {
							$.alert({
								title : 'Error',
								theme : 'light',
								content : error
							})
						}).always(function() {
						});
					}											
				}					
			}
		});
	}
	
	var copyURL = function(url){
			var $temp = $("<input />");
			$("body").append($temp);
			$temp.val(url).select();
			document.execCommand("copy");
			$temp.remove();
			
	}	
	
	var showMicroserviceURLDialog = function(contextpath, type, caas, openshiftUrl, deployed){
		
		var messageInfo = "URL Information";
		var messageCopied = "copied";
		var messageBase = "BASE URL";
		var messageGetNoteInformation = "GET NOTEBOOK INFO URL";
		var messageRunNotebook = "RUN NOTEBOOK URL";
		var messageRunParagraphSync = "RUN PARAGRAPH SYNC URL";
		var messageRunParagraphAsync = "RUN PARAGRAPH ASYNC URL";
		var messageGetParagrapInformation = "GET PARAGRAPH INFO URL";
		
		var pathGetNoteInformation =  "/api/notebook/0IDSTATIC";
		var pathRunNotebook =  "/api/notebook/job/0IDSTATIC";
		var pathRunParagraphSync =  "/api/notebook/run/0IDSTATIC/<paragraph>";
		var pathRunParagraphAsync =  "/api/notebook/job/0IDSTATIC/<paragraph>";
		var pathGetParagrapInformation =  "/api/notebook/0IDSTATIC/paragraph/<paragraph>";

		var dialogContent="";
		if(caas=="RANCHER" || !deployed)
		{
			dialogContent = `	
			<div class="alert alert-info alert-dismissable">
					<button type="button" class="close" data-dismiss="alert" aria-hidden="true"></button>
				<i class="fa fa-info-circle"></i> 
				<span>${messageInfo}</span>			
			</div>
			<div id="infoCopied" class="alert alert-success display-hide">
				<button class="close" data-close="alert"></button> <span >${messageCopied}</span>
			</div>
			<div><label>${messageBase}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${window.location.origin}${contextpath}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			`
		}
		else {
			dialogContent = `	
			<div class="alert alert-info alert-dismissable">
					<button type="button" class="close" data-dismiss="alert" aria-hidden="true"></button>
				<i class="fa fa-info-circle"></i> 
				<span>${messageInfo}</span>			
			</div>
			<div id="infoCopied" class="alert alert-success display-hide">
				<button class="close" data-close="alert"></button> <span >${messageCopied}</span>
			</div>
			<div><label>${messageBase}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${openshiftUrl}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			`
		}
			
		if (type === "NOTEBOOK_ARCHETYPE") {
		dialogContent += `	
			<div><label>${messageGetNoteInformation}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${window.location.origin}${pathGetNoteInformation}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			<div><label>${messageRunNotebook}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${window.location.origin}${pathRunNotebook}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			<div><label>${messageRunParagraphSync}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${window.location.origin}${pathRunParagraphSync}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			<div><label>${messageRunParagraphAsync}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${window.location.origin}${pathRunParagraphAsync}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			<div><label>${messageGetParagrapInformation}</label>
				<input class="col-md-12 form-control" readonly="readonly" value="${window.location.origin}${pathGetParagrapInformation}" onclick=" this.select();document.execCommand('copy'); $('#infoCopied').show();" type="text"/>	
			</div>
			`
		} 
		
		
		$.confirm({
			title: headerJson.microserviceUrl,
			theme: 'light',
			columnClass: 'medium',
			content:dialogContent,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				Ok: {
					text: headerReg.btnCancelar,
					btnClass: 'btn btn-outline blue dialog',
					action: function(){ 
					}											
				},
				Copy: {
					text: headerReg.btnCopiar,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if(caas=="RANCHER" || !deployed)
						{
							copyURL(window.location.origin + contextpath);
						}
						else {
							copyURL(openshiftUrl);
						}
					}	
				}
			}
		});
	};
	
	
	
	// Public API
	return {
		init: init,
		dtRenderOptions: dtRenderOptions,
		dtRenderCICD: dtRenderCICD,
		initCompleteCallback: initCompleteCallback,
		reloadMicroserviceTable: reloadMicroserviceTable,
		buildWithParameters: buildWithParameters,
		dtRenderLinks: dtRenderLinks,
		dtRenderLinksJenkins: dtRenderLinksJenkins,
		deployWithParameters: deployWithParameters,
		deployWithParametersOC: deployWithParametersOC,
		upgrade: upgrade,
		addEnvVar: addEnvVar,
		removeEnvVar: removeEnvVar,
		getHosts: getHosts
		
	};
	
})();

$(document).ready(function() {	
	
	Microservice.List.init();
	Microservice.List.reloadMicroserviceTable();

});
