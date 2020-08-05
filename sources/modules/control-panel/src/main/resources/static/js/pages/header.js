var HeaderController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS. 
	var APPNAME = 'Smart4Cities Control Panel'; 
	var LIB_TITLE = 'Header Controller';	
	var logControl = 0;     

	// CONTROLLER PRIVATE FUNCTIONS

	// GENERIC HEADER SEARCH
	var searchDocs = function(){		
		logControl ? console.log('searchDocs() Search --> '+ $("#search-query").val()) : '';

		// NOT-AVAILABLE 
		$.alert({title: 'onesait Platform Search:', theme: 'light' ,content: 'FUNCTIONALITY NOT-AVAILABLE!'}); return false;

		var search = $("#search-query").val();
		var url = "/console/api/rest/searchDocs/"+search;
		var settings = {"async": true, "url": url, "method": "GET", "headers": {"cache-control": "no-cache"} };

		// llamada para la búsqueda
		$.ajax(settings).done(function (response) {
			hideDocPost();
			if( !Array.isArray(response) ){
				showErrorDialog();
				return;
			}

			// total resultados obtenidos.
			$("#docs-count").text(response.length);

			blogResults = response.filter(function(f){ return f.type=="blog";});
			docsResults = response.filter(function(f){ return f.type=="doc";});

			// HTML de salida.
			var html = "";
			// BLOGS
			if( blogResults.length > 0 ){
				$("#blog-content-title").show();
				for ( var i = 0; i < blogResults.length; i++){
					var doc = blogResults[i];
					categorias = doc.categoria.join(" ");
					// TO-DO: ajustar css.
					html += "<div style='padding: 2px;margin-bottom: 10px;padding-bottom: 10px;' class='col-md-4 col-lg-3 "+ categorias.toLowerCase() +"'>"
					+ "<div class='search-card'>"
					+ "<div class='search-card-title'>"
					+ "<a onClick='javascript:showDocPost(\""+ doc.link +"\")'><span class='glyphicon glyphicon-blog'></span>"+ doc.title +"</a>"
					+ "</div>"
					+ "<div class='search-card-body'>"
					if (doc.imageUrl) { html +="<img style='width: 100%;;' src="+ doc.imageUrl +"></img>"}; 

					html += "<p>"+ doc.content +"</p>"
					+ "</div>"
					+ "<div class='search-card-foot'>"
					+ "<span class='glyphicon glyphicon-time'></span>"+new Date(doc.date).toLocaleDateString()+""
					+ "<span class='pull-right glyphicon glyphicon-new-window' onClick='javascript:window.open(\""+ doc.link +"\", \"_blank\")'></span>"
					+ "</div></div>"
					+ "</div>"
				}
			}
			else{
				// NO BLOGS
				$("#blog-content-title").hide();
			}

			// ADD HTML RESULT. 		
			$('#blog-content').html(html);

			// DOCS.
			html = "<ul class='searchdoc'>";
			if( docsResults.length > 0){
				$("#docs-content-title").show();
				for (var i = 0; i < docsResults.length; i++){
					var doc = docsResults[i];
					categorias = doc.categoria.join(" ");
					// TO-DO: ajustar css.
					html += "<li class='"+categorias.toLowerCase()+"'>"
					+ "<a onClick='javascript:showDocPost(\""+ doc.link +"\")'><span class='glyphicon glyphicon-book'></span> "+ doc.title +"</a>"
					+ "<br><span>"+ doc.content +"</span>"
					+ "</li>";
				}
			}
			else{
				// NO DOCS
				$("#docs-content-title").hide();
			}

			// ADD HTML RESULT.
			html += "</ul>";
			$('#docs-content').html(html);
			$('#modalDocs').modal();
			$(".modal-backdrop").hide()
		});
	}

	// SHOW SEARCH DOCS
	var showDocPost = function(url){		
		logControl ? console.log('showDocPost()...') : '';

		$("#result-show-content").html("<iframe id='map-iframe' width='100%' height='100%' frameborder=0 scrolling=no" + "marginheight=0 marginwidth=0 src='" + url +"'></iframe>");
		$("#modalDocs-result-show").show();
		$("#btn-search-back").show();
		$("#modalDocs-content").hide();		
	}

	// HIDE SEARCH DOCS
	var hideDocPost = function(){
		logControl ? console.log('hideDocPost()...') : '';

		$("#modalDocs-result-show").hide();
		$("#btn-search-back").hide();
		$("#modalDocs-content").show();				
	}

	// GENERIC-CONFIRM-DIALOG
	var showConfirmDialog = function(formId){

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var	Content = headerReg.genericConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!',theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});		
	}

	// CONFIG-CONFIRM-DIALOG
	var showConfigurationConfirmDialog = function(formId){

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var	Content = headerReg.configurationConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});		
	}
	
	// DATAMODEL-CONFIRM-DIALOG
	var showDataModelConfirmDialog = function(formId){

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var	Content = headerReg.dataModelConfirm;
		var Title = headerReg.titleConfirm + ':';

		// datamodel-confirm DIALOG SYSTEM.
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},				
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});		
	}

	// CONFIG-CONFIRM-DIALOG
	var showScheduledSearchConfirmDialog = function(formId){

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var	Content = headerReg.scheduledSearchConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light' , content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});		
	}

	// TWITTERLISTENING-CONFIRM-DIALOG
	var showTwitterListeningConfirmDialog = function(formId){		
		logControl ? console.log('showConfirmDialogTwitterlistening()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.twitterListeningConfirm;
		var Title = headerReg.titleConfirm + ':';		

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			icon: 'fa fa-warning',
			title: Title,
			theme: 'light',
			type: 'red',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			closeIcon: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}

	// ONTOLOGY-CONFIRM-DIALOG
	var showConfirmDialogOntologia = function(formId){		
		logControl ? console.log('showConfirmDialogOntologia()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.ontologyConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// LAYER-CONFIRM-DIALOG
	var showConfirmDialogLayer = function(formId){		
		logControl ? console.log('showConfirmDialogLayer()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.layerConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// VIEWER-CONFIRM-DIALOG
	var showConfirmDialogViewer = function(formId){		
		logControl ? console.log('showConfirmDialogViewer()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.viewerConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// SUBSCRIPTION-CONFIRM-DIALOG
	var showConfirmDialogSubscription = function(formId){		
		logControl ? console.log('showConfirmDialogSubscription()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.subscriptionConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// CATEGORY-CONFIRM-DIALOG
	var showConfirmDialogCategory = function(formId){		
		console.log('showConfirmDialogCategory()...');

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.categoryConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	var showConfirmDialogModel = function(formId){		
		console.log('showConfirmDialogModel()...');

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.modelConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	var showConfirmDialogSubcategory = function(formId){		
		console.log('showConfirmDialogSubcategory()...');

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.subcategoryConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// ONTOLOGY-CONFIRM-DIALOG
	var showConfirmDialogVirtualDatasource = function(formId){		
		logControl ? console.log('showConfirmDialogVirtualDatasource()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.virtualDatasourceConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// DIGITALTWINTYPE-CONFIRM-DIALOG
	var showConfirmDialogDigitalTwinType = function(formId){		
		logControl ? console.log('showConfirmDialogDigitalTwinType()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.digitalTwinTypeConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// DIGITALTWINdevice-CONFIRM-DIALOG
	var showConfirmDialogDigitalTwinDevice = function(formId){		
		logControl ? console.log('showConfirmDialogDigitalTwinDevice()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.digitalTwinDeviceConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}

	// USER-CONFIRM-DIALOG
	var showConfirmDialogUsuario = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.userConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!',theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	
	// DATASOURCE-CONFIRM-DIALOG
	var showConfirmDialogDatasource = function(formId, gadgetlist){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.gadgetDatasourceConfirm;
		if (gadgetlist.length>0) {
			Content = Content + '<BR>' + headerReg.gadgetDatasourceGadgetWarningConfirm + '<BR><BR>';
		    for( var i = 0; i < gadgetlist.length; i++ ){
				Content = Content + gadgetlist[i] + "  ";
			}
		}
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}

	// DATASOURCE-NAVIGATION-CONFIRM-DIALOG
	var showNavigationConfirmDialogDatasource = function(url, gadgetlist){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Edit = headerReg.btnEditar;
		var Content = "";
		if (gadgetlist.length>0) {
			Content = Content + '<BR>' + headerReg.gadgetDatasourceGadgetWarningConfirm + '<BR><BR>';
		    for( var i = 0; i < gadgetlist.length; i++ ){
				Content = Content + gadgetlist[i] + "  ";
			}
		}
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				accept: {
					text: Edit,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						window.location.href = url;
					}											
				}
			}
		});
	}
	
	// GADGET-CONFIRM-DIALOG
	var showConfirmDialogGadget = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.gadgetConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	
	// GADGET-CONFIRM-DIALOG
	var showConfirmDialogGadgetTemplate = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.gadgetTemplateConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	
	// DASHBOARD-CONF-CONFIRM-DIALOG
	var showConfirmDialogDashboardConf = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.dashboardConfConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	
	
	
	// DASHBOARDS-CONFIRM-DIALOG
	var showConfirmDialogDashboard = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.dashboardConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}

	// DEVICE-CONFIRM-DIALOG
	var showConfirmDialogDevice = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.deviceConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}	
	
	var showConfirmDialogFlowDomain = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.flowDomainConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	
	// KSQL-FLOW-CONFIRM-DIALOG
	var showConfirmDialogKsqlFlow = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.ksqlFlowConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	
	//KSQL-RELATION-CONFIRM-DIALOG
	var showConfirmDialogKsqlRelation = function(ksqlRelationId, deletionCallback){

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.ksqlRelationConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						deletionCallback(ksqlRelationId);
					}											
				}
			}
		});
	}

	// QUERY TEMPLATE-CONFIRM-DIALOG
	var showConfirmDialogQueryTemplate = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.queryTemplateConfirm;
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
				close: {
					text: Close,
					btnClass: 'btn btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-circle btn-outline btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	
	// SERVER ERRORS-DIALOG
	var errors = function(){		
		var Close = headerReg.btnCancelar;
		if ( headerReg.errores !== null ){	
			var htmlContent= "";
			headerReg.errores.split("\n").forEach(function(error){htmlContent+='<p>'+error+'</p>'});
			// jquery-confirm DIALOG SYSTEM.
			$.confirm({
				icon: 'fa fa-bug',
				title: 'ERROR',
				theme: 'light',
				content: htmlContent ,
				draggable: true,
				dragWindowGap: 100,
				backgroundDismiss: true,
				closeIcon: true,
				buttons: {				
					close: {
						text: Close,
						btnClass: 'btn btn-circle btn-outline btn-primary',
						action: function (){} //GENERIC CLOSE.		
					}
				}
			});			
		} else { logControl ? console.log('|---> errors() -> NO ERRORS FROM SERVER.') : ''; }		
	}

	// SERVER INFORMATION-DIALOG (ERRORS)
	var information = function(){		
		var Close = headerReg.btnCancelar;

		if (headerReg.informacion !== null ){			
			// jquery-confirm DIALOG SYSTEM.
			$.confirm({
				icon: 'fa fa-info-circle',
				title: 'INFO',
				theme: 'light',
				content: headerReg.informacion,
				draggable: true,
				dragWindowGap: 100,
				backgroundDismiss: true,
				closeIcon: true,
				buttons: {				
					close: {
						text: Close,
						btnClass: 'btn btn-circle btn-outline btn-primary',
						action: function (){} //GENERIC CLOSE.		
					}
				}
			});
		}
//		var cookie = $.cookie("RELEASE");
//		if(cookie == null){
//			$('#new-release').modal('show');
//			$.cookie("RELEASE",new Date().getTime(),{expires:30});
//		}
		else { logControl ? console.log('|---> information() -> NO ERROR INFO.') : ''; }		
	}	


	// CONTROLLER PUBLIC FUNCTIONS 
	return{

		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER.
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return headerReg = Data;
		},

		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';

			// CALL ERRORS
			errors();			
			// CALL INFO
			information();
			
			// adjust breadcrum
			$('.page-bar').insertAfter($('.page-logo'));
			$('.page-bar').fadeIn();
			$( "#confluence-query" ).on( "keydown", function(event) {
			      if(event.which == 13)
			    	  gotoSearch();
			    	  
			});

		},

		// SEARCH
		search: function(){
			logControl ? console.log(LIB_TITLE + ': search()') : '';
			searchDocs();			
		},

		// SERVER-ERROR CONTROL-DIALOG
		showErrorDialog: function(message){		
			logControl ? console.log('showErrorDialog()...') : '';
			var Close = headerReg.btnCancelar;

			// jquery-confirm DIALOG SYSTEM.
			$.confirm({
				icon: 'fa fa-bug',
				title: 'ERROR',
				theme: 'light',
				content: message,
				draggable: true,
				dragWindowGap: 100,
				backgroundDismiss: true,
				closeIcon: true,
				buttons: {				
					close: {
						text: Close,
						btnClass: 'btn btn-circle btn-outline btn-primary',
						action: function (){} //GENERIC CLOSE.		
					}
				}
			});			
		},

		// GENERIC-CONFIRM-DIALOG
		showConfirmDialog : function(formId){		
			logControl ? console.log('showConfirmDialog()...') : '';
			showConfirmDialog(formId);
		},

		// ONTOLOGY-CONFIRM-DIALOG
		showConfirmDialogOntologia : function(formId){		
			logControl ? console.log('showConfirmDialogOntologia()...') : '';
			showConfirmDialogOntologia(formId);
		},
		// LAYER-CONFIRM-DIALOG
		showConfirmDialogLayer : function(formId){		
			logControl ? console.log('showConfirmDialogLayer()...') : '';
			showConfirmDialogLayer(formId);
		},
		// LAYER-CONFIRM-DIALOG
		showConfirmDialogViewer : function(formId){		
			logControl ? console.log('showConfirmDialogViewer()...') : '';
			showConfirmDialogViewer(formId);
		},
		// SUBSCRIPTION-CONFIRM-DIALOG
		showConfirmDialogSubscription : function(formId){		
			logControl ? console.log('showConfirmDialogSubscription()...') : '';
			showConfirmDialogSubscription(formId);
		},
		// CATEGORY-CONFIRM-DIALOG
		showConfirmDialogCategory : function(formId){		
			logControl ? console.log('showConfirmDialogCategory()...') : '';
			showConfirmDialogCategory(formId);
		},
		// ONTOLOGY-CONFIRM-DIALOG
		showConfirmDialogModel : function(formId){		
			logControl ? console.log('showConfirmDialogModel()...') : '';
			showConfirmDialogModel(formId);
		},
		// ONTOLOGY-CONFIRM-DIALOG
		showConfirmDialogSubcategory : function(formId){		
			logControl ? console.log('showConfirmDialogSubcategory()...') : '';
			showConfirmDialogSubcategory(formId);
		},
		// DATASOURCE VIRTUAL-CONFIRM-DIALOG
		showConfirmDialogVirtualDatasource : function(formId){		
			logControl ? console.log('showConfirmDialogVirtualDatasource()...') : '';
			showConfirmDialogVirtualDatasource(formId);
		},
		// DIGITALTWINTYPE-CONFIRM-DIALOG
		showConfirmDialogDigitalTwinType : function(formId){		
			logControl ? console.log('showConfirmDialogDigitalTwinType()...') : '';
			showConfirmDialogDigitalTwinType(formId);
		},
		// DIGITALTWINTYPE-CONFIRM-DIALOG
		showConfirmDialogDigitalTwinDevice : function(formId){		
			logControl ? console.log('showConfirmDialogDigitalTwinDevice()...') : '';
			showConfirmDialogDigitalTwinDevice(formId);
		},
		showTwitterListeningConfirmDialog: function(formId){		
			logControl ? console.log('showTwitterListeningConfirmDialog()...') : '';
			showTwitterListeningConfirmDialog(formId);
		},
		// CONFIGURATION-CONFIRM-DIALOG
		showConfigurationConfirmDialog : function(formId){		
			logControl ? console.log('showConfigurationConfirmDialog()...') : '';
			showConfigurationConfirmDialog(formId);
		},
		// DATAMODEL-CONFIRM-DIALOG
		showDataModelConfirmDialog : function(formId){		
			logControl ? console.log('showDataModelConfirmDialog()...') : '';
			showDataModelConfirmDialog(formId);
		},
		// SCHEDULEDSEARCH-CONFIRM-DIALOG
		showScheduledSearchConfirmDialog : function(formId){		
			logControl ? console.log('showScheduledSearchConfirmDialog()...') : '';
			showScheduledSearchConfirmDialog(formId);
		},

		// USER-CONFIRM-DIALOG
		showConfirmDialogUsuario : function(formId){		
			logControl ? console.log('showConfirmDialogUsuario()...') : '';
			showConfirmDialogUsuario(formId);
		},
		
		// DATASOURCE-CONFIRM-DIALOG
		showConfirmDialogDatasource : function(formId, gadgetlist){		
			logControl ? console.log('showConfirmDialogDatasource()...') : '';
			showConfirmDialogDatasource(formId, gadgetlist);
		},
		
		// DATASOURCE-NAVIGATION-CONFIRM-DIALOG
		showNavigationConfirmDialogDatasource : function(url, gadgetlist){		
			logControl ? console.log('showNavigationConfirmDialogDatasource()...') : '';
			showNavigationConfirmDialogDatasource(url, gadgetlist);
		},
		
		// DASHBOARD-CONFIRM-DIALOG
		showConfirmDialogDashboard : function(formId){		
			logControl ? console.log('showConfirmDialogDashboard()...') : '';
			showConfirmDialogDashboard(formId);
		},
		
		// GADGET-CONFIRM-DIALOG
		showConfirmDialogGadget : function(formId){		
			logControl ? console.log('showConfirmDialogDashboard()...') : '';
			showConfirmDialogGadget(formId);
		},
		showConfirmDialogDashboardConf : function(formId){		
			logControl ? console.log('showConfirmDialogDashboardConf()...') : '';
			showConfirmDialogDashboardConf(formId);
		},
			
		showConfirmDialogDevice: function(formId){		
			logControl ? console.log('showConfirmDialogDevice()...') : '';
			showConfirmDialogDevice(formId);
		},// GADGET-CONFIRM-DIALOG
		showConfirmDialogGadgetTemplate : function(formId){		
			logControl ? console.log('showConfirmDialogGadgetTemplate()...') : '';
			showConfirmDialogGadgetTemplate(formId);
		},// FLOW-DOMAIN-CONFIRM-DIALOG
		showConfirmDialogFlowDomain : function(formId){		
			logControl ? console.log('showConfirmDialogKsqlFlow()...') : '';
			showConfirmDialogFlowDomain(formId);
		},// KSQL-FLOW-CONFIRM-DIALOG
		showConfirmDialogKsqlFlow : function(formId){		
			logControl ? console.log('showConfirmDialogKsqlFlow()...') : '';
			showConfirmDialogKsqlFlow(formId);
		},	// KSQL-RELATION-CONFIRM-DIALOG
		showConfirmDialogKsqlRelation : function(ksqlRelationId, deletionCallback){		
			logControl ? console.log('showConfirmDialogKsqlRelation()...') : '';
			showConfirmDialogKsqlRelation(ksqlRelationId, deletionCallback);
		},
		// QUERY TEMPLATE-CONFIRM-DIALOG
		showConfirmDialogQueryTemplate : function(formId){		
			logControl ? console.log('showConfirmDialogQueryTemplate()...') : '';
			showConfirmDialogQueryTemplate(formId);
		},
	};
}();

//AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	var csrf_value = $("meta[name='_csrf']").attr("content");
	var csrf_header = $("meta[name='_csrf_header']").attr("content");
	$.ajax({
		url : "/controlpanel/themes/getThemeJson",
		type : "GET",
		async : true,
		headers: {
			[csrf_header]: csrf_value
	    },
		success : function(response){
			var img64 = response.HEADER_IMAGE;
			if (img64 != null && img64 != ""){
				$('#imagen').append("<img id='headerImg' alt='logo' class='logo-default' src='data:image/jpeg;base64, "+img64+"'/>");
			} else {
				$('#imagen').append("<img id='headerImg' alt='logo' class='logo-default' src='/controlpanel/static/images/platform_logo.png'/>");
			}
		},
	    error :  function () {
	    	$('#imagen').append("<img id='headerImg' alt='logo' class='logo-default' src='/controlpanel/static/images/platform_logo.png'/>");
	    	}
	    
	})

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	HeaderController.load(headerJson);

	// AUTO INIT CONTROLLER.
	HeaderController.init();
});


// CONFLUENCE SEARCH
	function gotoSearch() { 
		var value=document.getElementById("querySearchConfluence").value;
	    var link="https://onesaitplatform.atlassian.net/wiki/dosearchsite.action?cql=siteSearch+~+\""+value+"\"&queryString="+value;
	    var a = document.getElementById("search-confluence");
	    a.href=link;
	    a.target="_blank";
	    a.click();
	}  
	function enterSearch(e) {
	    if (e.keyCode == 13) {
	        gotoSearch();
	    }
	}
	