var EnvironmentController = function(){
	
	// DEFAULT PARAMETERS, VAR, CONSTS
	var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Environment Controller';
	logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
		
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> resetForm') : '';
	}
	
	function initTable () {
		var languageJson = JSON.parse(environmentControllerJson.datatable_lang);
		if ( languageJson ){ $.extend( true, $.fn.dataTable.defaults, { language: languageJson }); }
		
		$('table').unbind('preDraw.dt');
		$('table').on('preDraw.dt', function() {
			$("body").click();
		});
		$('table').unbind('draw.dt');
		$('table').on('draw.dt', function() {
			$('.download-menu').parent().off('shown.bs.dropdown');
			$('.download-menu').parent().off('hide.bs.dropdown');
		
			$('.download-menu').unbind('shown.bs.dropdown');
		    $('.download-menu').parent().on('shown.bs.dropdown', function () {
				var $menu = $("ul", this);
		        offset = $menu.offset();
		        position = $menu.position();
		        $("#tablecontainerpopup").append($menu);
	
		      	$menu.css('position', 'absolute');
		        $menu.css('top', (offset.top-200) +'px');
		        $menu.css('left',(offset.left) +'px');
		        $menu.css('min-width', '100px');
		        $menu.show();
		        $(this).data("myDropdownMenu", $menu);
	       	});
	       	$('.download-menu').unbind('hide.bs.dropdown');
		    $('.download-menu').parent().on('hide.bs.dropdown', function () {
		        $(this).append($(this).data("myDropdownMenu"));
		        $(this).data("myDropdownMenu").removeAttr('style');
		    });
		});
		
		oTable = $('#environments').dataTable({
			"bAutoWidth": false
		});
		
		//SHOW/HIDE DATATABLE COLUMNS HANDLER
		$('input.toggle-vis').on('change', function (e) {
			e.preventDefault();
			// Get the column API object
			var column = $('#environments').DataTable().column($(this).attr('data-column'));
			// Toggle the visibility
			column.visible(!column.visible());
		});
	

		// SHOW/HIDE DATATABLES COLUMN INIT
		$('#divcreate').prepend($('#dataTable-vis-toggle'));
		$('#dataTable-vis-toggle').removeClass('hide');
		$('.dataTables_info').addClass('col-md-6 col-sm-6');
		$('.dataTables_length').addClass('col-md-6 col-sm-6');
		$('#environments_wrapper > div:nth-child(3)').addClass('table-scrollable table-scrollable-borderless');
		$('#environments_wrapper > div:nth-child(3) > div.col-md-5.col-sm-5').append($('.dataTables_length'));
	
		$('#search-on-title').append($('.dataTables_filter > label > input'));
		$('#search-on-title > input').removeClass('input-xsmall')
		$('#environments_wrapper > div:nth-child(1)').hide();
	
		$('.dataTables_paginate').attr('style', 'float:right !important');

		$('#dataTable-vis-toggle').hide();
		
		$("#contenedor-tabla-outside").removeClass("hidden");
	}
	
	function reloadFragment () {
		$('#tablecontainerpopup > ul:first').remove();
		$('#environment-kpis-fragment').load(environmentControllerJson.apiPath + "/getkpis");
		$('#environment-list-fragment').load(environmentControllerJson.apiPath + "/getdeploymentlist",
			function(data) {
				var selectedPage = oTable.fnPagingInfo().iPage;
				var filter = $('#search-on-title').find('input:first').val();
				$('#search-on-title').find('input:first').remove();
				initTable ();
				oTable.filter(filter);
				oTable.fnPageChange(selectedPage);
				$('#search-on-title').find('input:first').val(filter).keyup();
			}
		);
	}
	
	function startEnvironmentConfirm(deploymentName) {
		HeaderController.deleteStandardActionConfirmationDialog(
			"", environmentControllerJson.close, environmentControllerJson.start_module, 
			environmentControllerJson.messages.startConfirm, deploymentName, "",
			function(){
				scaleEnvironment(deploymentName, '1');
			}
		)
	}

	function stopEnvironmentConfirm(deploymentName) {
		HeaderController.deleteStandardActionConfirmationDialog(
			"", environmentControllerJson.close, environmentControllerJson.stop_module, 
			environmentControllerJson.messages.stopConfirm, deploymentName, "",
			function(){
				scaleEnvironment(deploymentName, '0');
			}
		)
	}
	
	function resumeEnvironment(deploymentName) {
		fetch(environmentControllerJson.apiPath + '/resume/' + deploymentName, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json'
			}})
			.then(response => {
				if (!response.ok) {
					toastr.error(messagesForms.operations.genOpError, environmentControllerJson.errors.startFailed);
					console.error(environmentControllerJson.errors.startFailed);
				} else {
					toastr.info(environmentControllerJson.messages.operationInProgress,headerReg.informacion);
				}

			})
			.catch(error => {
				toastr.error(messagesForms.operations.genOpError, environmentControllerJson.errors.startError);
				console.error(environmentControllerJson.errors.startError, error);
			});
	}

	function pauseEnvironment(deploymentName) {
		fetch(environmentControllerJson.apiPath + '/pause/' + deploymentName, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json'
			}})
			.then(response => {
				if (!response.ok) {
					toastr.error(messagesForms.operations.genOpError, environmentControllerJson.errors.stopFailed);
					console.error(environmentControllerJson.errors.stopFailed);
				} else {
					toastr.info(environmentControllerJson.messages.operationInProgress,headerReg.informacion);
				}

			})
			.catch(error => {
				toastr.error(messagesForms.operations.genOpError, environmentControllerJson.errors.stopError);
				console.error(environmentControllerJson.errors.stopError, error);
			});
	}
	
	function restartEnvironmentConfirm(deploymentName) {
		HeaderController.deleteStandardActionConfirmationDialog(
			"", environmentControllerJson.close, environmentControllerJson.restart_module, 
			environmentControllerJson.messages.restartConfirm, deploymentName,	"",
			function(){
				restartEnvironment(deploymentName);
			}
		)
	}

	function restartEnvironment(deploymentName) {
		// Lógica para reiniciar el módulo
		fetch(environmentControllerJson.apiPath + "/restart/" + deploymentName, {
			method: "GET",
			headers: {
				"Content-Type": "application/json"
			}})
			.then(response => {
				if (!response.ok) {
					toastr.error(messagesForms.operations.genOpError, environmentControllerJson.errors.restartFailed);
					console.error(environmentControllerJson.errors.restartFailed + `: ${response.statusText}`);
				} else {
					toastr.info(environmentControllerJson.messages.operationInProgress,headerReg.informacion);
				}
			})
			.catch(error => {
				toastr.error(messagesForms.operations.genOpError, environmentControllerJson.errors.restartError);
				console.error(environmentControllerJson.errors.restartError, error);
			});
	}
	
	function updateEnvironmentConfirm (deploymentName){		
		clearInterval(timeInterval);
		var Content = '<div class="inline font-xs"> ' + environmentControllerJson.messages.updateConfirm + '</div><br><br>' + 
			'<span style="margin-right: 10px;">' + environmentControllerJson.messages.updateConfirmImage + '</span>' +'<input id="id_image" class="form-control" style="text-align: right;" value="" name="id_image">' +
			'<span></span>';

		$.confirm({
			title: deploymentName,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: environmentControllerJson.close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){
						timeInterval = setInterval(EnvironmentController.reloadFragment, 10000);
					} //GENERIC CLOSE.		
				},
				remove: {
					text: environmentControllerJson.update_module,
					btnClass: 'btn btn-primary',
					action: function(){
						if ($('#id_image').val()!=""){
							updateEnvironment(deploymentName, $('#id_image').val());
						} else {
							toastr.error(messagesForms.operations.genOpError, environmentControllerJson.messages.updateConfirmError);
						}
						timeInterval = setInterval(EnvironmentController.reloadFragment, 10000);
					}
				}
			}
		});
	}

	function updateEnvironment(deploymentName, imageId) {
		// Lógica para actualizar el módulo
		fetch(environmentControllerJson.apiPath + "/updateImage/" + deploymentName , {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
				"image": imageId
			},
			data: { 
				image: imageId
			}})	
			.then(response => {
				if (!response.ok) {
					toastr.error(messagesForms.operations.genOpError, environmentControllerJson.errors.updateFailed);
					console.error(environmentControllerJson.errors.updateFailed + `: ${response.statusText}`);
				} else {
					toastr.info(environmentControllerJson.messages.operationInProgress,headerReg.informacion);
				}
			})
			.catch(error => {
				toastr.error(messagesForms.operations.genOpError, environmentControllerJson.errors.updateError);
				console.error(environmentControllerJson.errors.updateError, error);
			});
	}
	
	function scaleEnvironmentConfirm (deploymentName){		
		clearInterval(timeInterval);
		var Content = '<div class="inline font-xs"> ' + environmentControllerJson.messages.scaleConfirm + '</div><br><br>' + 
			'<span style="margin-right: 10px;">' + environmentControllerJson.messages.scaleConfirmPods + '</span>' +'<input id="id_scale" class="form-control input-mini" style="text-align: right;" type="number" min="0" max="10" value="1" name="id_scale">' +
			'<span></span>';

		$.confirm({
			title: deploymentName,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: environmentControllerJson.close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){
						timeInterval = setInterval(EnvironmentController.reloadFragment, 10000);
					} //GENERIC CLOSE.		
				},
				remove: {
					text: environmentControllerJson.scale_module,
					btnClass: 'btn btn-primary',
					action: function(){
						if ($('#id_scale').val()!=""){
							scaleEnvironment(deploymentName, $('#id_scale').val());
						} else {
							toastr.error(messagesForms.operations.genOpError, environmentControllerJson.messages.scaleConfirmError);
						}
						timeInterval = setInterval(EnvironmentController.reloadFragment, 10000);
					}
				}
			}
		});
	}

	function scaleEnvironment(deploymentName, pods) {
		// Lógica para reiniciar el módulo
		fetch(environmentControllerJson.apiPath + "/scale/" + deploymentName + "/" + pods, {
			method: "GET",
			headers: {
				"Content-Type": "application/json"
			}})
			.then(response => {
				if (!response.ok) {
					toastr.error(messagesForms.operations.genOpError, environmentControllerJson.errors.scaleFailed);
					console.error(environmentControllerJson.errors.scaleFailed + `: ${response.statusText}`);
				} else {
					toastr.info(environmentControllerJson.messages.operationInProgress,headerReg.informacion);
				}
			})
			.catch(error => {
				toastr.error(messagesForms.operations.genOpError, environmentControllerJson.errors.scaleError);
				console.error("environmentControllerJson.errors.scaleError", error);
			});
	}
	
	function loadConfigDialog (modulename){
		$('#deployment-detail-fragment').load(environmentControllerJson.apiPath + "/getdeployment/" + modulename,
			function(data) {
				clearInterval(timeInterval);
				$('#dialog-config').modal('toggle');
			}
		);
	}
		
	// CONTROLLER PUBLIC FUNCTIONS
	return{
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
		},
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			initTemplateElements();
		},
		
		initTable: function(){
			logControl ? console.log(LIB_TITLE + ': initTable()') : '';
			initTable();
		},
		
		reloadFragment: function(){
			logControl ? console.log(LIB_TITLE + ': reloadFragment()') : '';
			reloadFragment();
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		startEnvironmentConfirm: function(deploymentName) {
			startEnvironmentConfirm(deploymentName);
		},
		
		stopEnvironmentConfirm: function(deploymentName) {
			stopEnvironmentConfirm(deploymentName);
		},
		
		restartEnvironmentConfirm: function(deploymentName) {
			restartEnvironmentConfirm(deploymentName);
		},
		
		updateEnvironmentConfirm: function(deploymentName) {
			updateEnvironmentConfirm(deploymentName);
		},
		
		scaleEnvironmentConfirm: function(deploymentName) {
			scaleEnvironmentConfirm(deploymentName);
		},
		
		scaleEnvironment: function(deploymentName, pods) {
			scaleEnvironment(deploymentName, pods);
		},
		
		loadConfig: function(modulename) {
			loadConfigDialog(modulename);
		}
	};
	
}();


//AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	EnvironmentController.load(environmentControllerJson);			
	// AUTO INIT CONTROLLER.
	EnvironmentController.init();
	
});