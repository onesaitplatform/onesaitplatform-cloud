var csrf_value = $("meta[name='_csrf']").attr("content");
var csrf_header = $("meta[name='_csrf_header']").attr("content");

var setActiveTree = function (id) {
	
	$.ajax({url : "/controlpanel/categorization/setActive",
		data : {"id" : id},
		type : "POST",
		headers: {
			[csrf_header]: csrf_value
	    }
	}).done(function(response){
		navigateUrl("/controlpanel/categorization/list");}
	).fail(function(response, data){
		toastr.error(messagesForms.operations.genOpError,errorMsg);
		console.log("Error: ", response);}		
	) 

}

var deleteTree = function (id) {
	$.confirm({
		title: deleteTitle,
		theme: 'light',
		columnClass: 'medium',
		content: deleteDialog,
		draggable: true,
		dragWindowGap: 100,
		backgroundDismiss: true,
		style: 'red',
		buttons: {
			close: {
				text: closeBtn,
				btnClass: 'btn btn-outline blue dialog',
				action: function (){} //GENERIC CLOSE.		
			},
			Ok: {
				text: deleteBtn,
				btnClass: 'btn btn-primary',
				action: function(){	
					$.ajax({url : "/controlpanel/categorization/delete",
						data : {"id" : id},
						type : "POST",
						headers: {
							[csrf_header]: csrf_value
					    }
					}).done(function(response){
						toastr.success(messagesForms.operations.genOpSuccess,'');
						navigateUrl("/controlpanel/categorization/list");}
					).fail(function(response, data){
						toastr.error(messagesForms.operations.genOpError,errorMsg);
						console.log("Error: ", response);}		
					)
				}											
			},
		}
	});
	
}

var deactivateTree = function (id) {
	
	$.ajax({url : "/controlpanel/categorization/deactivate",
		data : {"id" : id},
		type : "POST",
		headers: {
			[csrf_header]: csrf_value
	    }
	}).done(function(response){
		console.log("success"+response);
		navigateUrl("/controlpanel/categorization/list");}
	).fail(function(response, data){
		toastr.error(messagesForms.operations.genOpError,errorMsg);
		console.log("Error: ", response);}		
	) 

}

var publicTree = function (id) {
	
	$.ajax({url : "/controlpanel/categorization/setPublic",
		data : {"id" : id, "state" : true},
		type : "POST",
		headers: {
			[csrf_header]: csrf_value
	    }
	}).success(function(response){
		console.log("success"+response);
		navigateUrl("/controlpanel/categorization/list");}
	).fail(function(response, data){
		toastr.error(messagesForms.operations.genOpError,errorMsg);
		console.log("Error: ", response);}		
	) 

}

var privateTree = function (id) {
	
	$.ajax({url : "/controlpanel/categorization/setPublic",
		data : {"id" : id, "state" : false},
		type : "POST",
		headers: {
			[csrf_header]: csrf_value
	    }
	}).success(function(response){
		console.log("success"+response);
		navigateUrl("/controlpanel/categorization/list");}
	).fail(function(response, data){
		toastr.error(messagesForms.operations.genOpError,errorMsg);
		console.log("Error: ", response);}		
	) 

}


// ########### CREATE ###########

var create = function() {
	if ($("#identification").val()){
		$("#treeField").jstree("deselect_all");
		$.ajax({url : "/controlpanel/categorization/create",
			type : "POST",
			data : { "name" : $("#identification").val(), "json" : JSON.stringify($("#treeField").jstree(true).get_json('#', {flat:true}))},
			headers: {
				[csrf_header]: csrf_value
		    }
		}).done(function(response, data){
			toastr.success(messagesForms.validation.genFormSuccess,'');
			navigateUrl("/controlpanel/categorization/list/");
			}
		).fail(function(response, data){
			toastr.error(messagesForms.validation.genFormError,response.responseText);
			}		
		)
	} else {
		$('#identification').closest('.form-group').addClass('has-error');
		$('#identificationerror').removeClass('hide');
		$('#identificationerror').addClass('help-block help-block-error');
		toastr.error(messagesForms.validation.genFormError,noName);
	}
}

var edit = function(id) {
	$("#treeField").jstree("deselect_all");
	$.ajax({url : "/controlpanel/categorization/edit",
		type : "POST",
		data : { "id" : id, "json" : JSON.stringify($("#treeField").jstree(true).get_json('#', {flat:true}))},
		headers: {
			[csrf_header]: csrf_value
	    }
	}).done(function(response, data){
		navigateUrl("/controlpanel/categorization/list/");
		}
	).fail(function(response, data){
		toastr.error(messagesForms.validation.genFormError,errorMsg);
		console.log("Error: ", response);
		}		
	)
}

var getElements = function(type){
	type = type.toLowerCase();
	switch (type){
	case "ontology":
		insertData("/controlpanel/categorization/getOntologies");
		break;
	case "apis":
		insertData("/controlpanel/categorization/getApis");
		break;
	case "devices":
		insertData("/controlpanel/categorization/getDevices");
		break;
	case "dashboards":
		insertData("/controlpanel/categorization/getDashboards");
		break;
	case "notebooks":
		insertData("/controlpanel/categorization/getNotebooks");
		break;
	case "dataflows":
		insertData("/controlpanel/categorization/getDataflows");
		break;
	case "viewers":
		insertData("/controlpanel/categorization/getViewers");
		break;
	case "reports":
		insertData("/controlpanel/categorization/getReports");
		break;
	case "flows":
		$.ajax({url : "/controlpanel/categorization/getFlows",
			type : "GET",
			dataType: "text",
			headers: {
				[csrf_header]: csrf_value
		    }
		}).done(function(response){
			var options = [];
			$('#selectElements').empty();
			options.push('<option value="'+response+'">'+response+'</option>');
			$('#selectElements').html(options);
			$('#selectElements').selectpicker('refresh'); 
			}
		).fail(function(response){
			console.log("Error: ", response);}		
		)
		break;
	default:
		break;
	}
}

var insertData = function(url) {
	$.ajax({url : url,
		type : "GET",
		headers: {
			[csrf_header]: csrf_value
	    },
		success : function(response){
		elementos = response;
		var options = [];
		$('#selectElements').empty();
		$.each(elementos, function(k, v){
			options.push('<option value="'+k+'">'+k+'</option>');
			})
		$('#selectElements').html(options);
		$('#selectElements').selectpicker('refresh'); 
		}
	}).fail(function(response, data){
		toastr.error(messagesForms.operations.genOpError,errorMsg);
		console.log("Error: ", response);}		
	)
}

var addElement = function() {
	type = $("#typeNode").val().toLowerCase();
	switch (type){
	case "ontology":
		$("#treeField").jstree(true).set_icon(node, "flaticon-network");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/ontologies/show/"+elementos[$("#selectElements").val()];
		break;
	case "flows":
		$("#treeField").jstree(true).set_icon(node, "flaticon-analytics");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = $("#selectElements").val();
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/flows/show/"+$("#selectElements").val();
		break;
	case "apis":
		$("#treeField").jstree(true).set_icon(node, "flaticon-multimedia");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/apimanager/show/"+elementos[$("#selectElements").val()];
		break;
	case "devices":
		$("#treeField").jstree(true).set_icon(node, "flaticon-truck");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/devices/show/"+elementos[$("#selectElements").val()];
		break;
	case "dashboards":
		$("#treeField").jstree(true).set_icon(node, "flaticon-graph");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/dashboards/view/"+elementos[$("#selectElements").val()];
		break;
	case "notebooks":
		$("#treeField").jstree(true).set_icon(node, "flaticon-interface-5");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/notebooks/show/"+elementos[$("#selectElements").val()];
		break;
	case "dataflows":
		$("#treeField").jstree(true).set_icon(node, "flaticon-technology");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/dataflow/show/"+elementos[$("#selectElements").val()];
		break;
	case "viewers":
		$("#treeField").jstree(true).set_icon(node, "flaticon-map-location");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/viewers/view/"+elementos[$("#selectElements").val()];
		break;
	case "reports":
		$("#treeField").jstree(true).set_icon(node, "la-folder-open");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/reports/runReport/"+elementos[$("#selectElements").val()];
		break;
	default:
		break;
	}
	$("#treeField").jstree(true).get_node(node).a_attr.elementType = type;
	$("#treeField").jstree(true).rename_node(node, $("#selectElements").val());
	$('#myModal').modal('toggle');
}