/*
 * GLOBAL SCRIPTS ASYNC for NH Hotels
 *  * @author: aponcep
 * @include:
 * 		Utils {}
 */

//== Set Vars of all the site
var Report = Report || {};

var Util = (function() {

	console.log('init Utils')
	
	var formatDateTime = function (date, pattern) {
		return $.formatDateTime(pattern, new Date(date));
	}
			
	var dtRenderCenterColumn = function (data, type, row) {
		return '<div class="text-center">' + data + '</div>';
	}
	
	var dtRenderPublicColumn  = function (data, type, row) {
		return data ? '<div class="text-center"><i class="la la-check-circle-o text-success font-hg"></i></div>' : 
			'<div class="text-center"><i class="la la-times-circle-o text-danger font-hg"></i></div>';
	}	
	
	var dtRenderDateCenterColumn = function (data, type, row) {
		return dtRenderCenterColumn (formatDateTime (data, 'dd/mm/yy'), type, row);
	}
	
	
	
	/*var showConfirmDeleteDialog = function(id){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = 'You are going to delete a datasource, are you sure?';
		var Title = headerReg.titleConfirm + ':';

			// jquery-confirm DIALOG SYSTEM.
			$.confirm({
				icon: 'fa fa-warning',
				title: Title,
				theme: 'light',
				columnClass: 'medium',
				content: Content,
				draggable: true,
				dragWindowGap: 100,
				backgroundDismiss: true,
				closeIcon: true,
				buttons: {
					remove: {
						text: Remove,
						btnClass: 'btn btn-sm btn-danger btn-outline',
						action: function(){ 
							console.log(id);
							$.ajax({
							    url: '/controlpanel/datasources/'+id,
							    type: 'DELETE',						  
							    success: function(result) {
							    	navigateUrl('/controlpanel/datasources/list');
							    }
							});
						}											
					},
					close: {
						text: Close,
						btnClass: 'btn btn-sm btn-default btn-outline',
						action: function (){} //GENERIC CLOSE.		
					}
				}
			});
		}*/

	var blockUI = function (el, centerY) {
		var el = $(el);
		el.block({
			message: '<img src="'+ajaxLoading+'" alt="">',
			centerY: centerY != undefined ? centerY : true,
			css: {
				top: '10%',
				border: 'none',
				padding: '2px',
				backgroundColor: 'none'
			},
			overlayCSS: {
				backgroundColor: '#000',
				opacity: 0.05,
				cursor: 'wait'
			}
		});
	}

	// Wrapper function to unblock elements (finish loading)
	var unblockUI = function (el) {
		$(el).unblock({
			onUnblock: function () {
				$(el).removeAttr("style");
			}
		});
	}
	
	// Public API
	return {
		formatDateTime: formatDateTime,
		dtRenderCenterColumn: dtRenderCenterColumn,
		dtRenderPublicColumn: dtRenderPublicColumn,
		dtRenderDateCenterColumn: dtRenderDateCenterColumn,
		blockUI: blockUI,
		unblockUI: unblockUI
	};
})();