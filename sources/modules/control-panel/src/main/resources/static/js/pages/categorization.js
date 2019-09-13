var csrf_value = $("meta[name='_csrf']").attr("content");
var csrf_header = $("meta[name='_csrf_header']").attr("content");

var setActiveTree = function (id) {
	
	$.ajax({url : "/controlpanel/categorization/setActive",
		data : {"id" : id},
		type : "POST",
		headers: {
			[csrf_header]: csrf_value
	    }
	}).success(function(response){
		navigateUrl("/controlpanel/categorization/list");}
	).fail(function(response, data){
		$.alert({title: 'ERROR!', theme: 'light', style: 'red', content: errorMsg});
		console.log("Error: ", response);}		
	) 

}

var deleteTree = function (id) {
	$.confirm({
		title: deleteBtn,
		theme: 'light',
		columnClass: 'medium',
		content: deleteDialog,
		draggable: true,
		dragWindowGap: 100,
		backgroundDismiss: true,
		closeIcon: true,
		buttons: {
			close: {
				text: closeBtn,
				btnClass: 'btn btn-sm btn-outline btn-circle blue',
				action: function (){} //GENERIC CLOSE.		
			},
			Ok: {
				text: "Ok",
				btnClass: 'btn btn-sm btn-outline btn-circle btn-primary',
				action: function(){	
					$.ajax({url : "/controlpanel/categorization/delete",
						data : {"id" : id},
						type : "POST",
						headers: {
							[csrf_header]: csrf_value
					    }
					}).success(function(response){
						navigateUrl("/controlpanel/categorization/list");}
					).fail(function(response, data){
						$.alert({title: 'ERROR!', theme: 'light', style: 'red', content: errorMsg});
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
	}).success(function(response){
		console.log("success"+response);
		navigateUrl("/controlpanel/categorization/list");}
	).fail(function(response, data){
		$.alert({title: 'ERROR!', theme: 'light', style: 'red', content: errorMsg});
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
		$.alert({title: 'ERROR!', theme: 'light', style: 'red', content: errorMsg});
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
		$.alert({title: 'ERROR!', theme: 'light', style: 'red', content: errorMsg});
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
		}).success(function(response, data){
			navigateUrl("/controlpanel/categorization/list/");
			}
		).fail(function(response, data){
			$.alert({title: 'ERROR!', theme: 'light', style: 'red', content: errorMsg});
			console.log("Error: ", response);
			}		
		)
	} else {
		$.alert({title: 'ERROR!', theme: 'light', style: 'red', content: noName});
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
	}).success(function(response, data){
		navigateUrl("/controlpanel/categorization/list/");
		}
	).fail(function(response, data){
		$.alert({title: 'ERROR!', theme: 'light', style: 'red', content: errorMsg});
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
	case "flows":
		$.ajax({url : "/controlpanel/categorization/getFlows",
			type : "GET",
			dataType: "text",
			headers: {
				[csrf_header]: csrf_value
		    }
		}).success(function(response){
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
	    }
	}).success(function(response){
		elementos = response;
		var options = [];
		$('#selectElements').empty();
		$.each(elementos, function(k, v){
			options.push('<option value="'+k+'">'+k+'</option>');
			})
		$('#selectElements').html(options);
		$('#selectElements').selectpicker('refresh'); 
		}
	).fail(function(response, data){
		$.alert({title: 'ERROR!', theme: 'light', style: 'red', content: errorMsg});
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
		$("#treeField").jstree(true).set_icon(node, "flaticon-network");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = $("#selectElements").val();
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/flows/show/"+$("#selectElements").val();
		break;
	case "apis":
		$("#treeField").jstree(true).set_icon(node, "flaticon-network");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/apimanager/show/"+elementos[$("#selectElements").val()];
		break;
	case "devices":
		$("#treeField").jstree(true).set_icon(node, "flaticon-share");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/devices/show/"+elementos[$("#selectElements").val()];
		break;
	case "dashboards":
		$("#treeField").jstree(true).set_icon(node, "flaticon-graph");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/dashboards/view/"+elementos[$("#selectElements").val()];
		break;
	case "notebooks":
		$("#treeField").jstree(true).set_icon(node, "flaticon-analytics");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/notebooks/show/"+elementos[$("#selectElements").val()];
		break;
	case "dataflows":
		$("#treeField").jstree(true).set_icon(node, "flaticon-analytics");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/dataflow/show/"+elementos[$("#selectElements").val()];
		break;
	case "viewers":
		$("#treeField").jstree(true).set_icon(node, "flaticon-map-location");
		$("#treeField").jstree(true).get_node(node).a_attr.elementId = elementos[$("#selectElements").val()];
		$("#treeField").jstree(true).get_node(node).a_attr.href = "/controlpanel/viewers/view/"+elementos[$("#selectElements").val()];
		break;
	default:
		break;
	}
	$("#treeField").jstree(true).get_node(node).a_attr.elementType = type;
	$("#treeField").jstree(true).rename_node(node, $("#selectElements").val());
	$('#myModal').modal('toggle');
}