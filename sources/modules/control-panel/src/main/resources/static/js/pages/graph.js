var GraphController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var APPNAME = 'Onesait Platform Control Panel', LIB_TITLE = 'Graph Controller', logControl = 0;

	// GRAPH INITIALIZATION VARS AND CONST.
	var links = [], nodes = [], data = [], edges = [], ArraySource = [], jsonProjects = {}, options = {}, jsonIndex = {}, graphData = {
		nodes : [],
		edges : []
	}, containerNetwork = new Object(), network = null, graphHeight = 500;

	// GRAPH OPTIONS DEFAULTS
	options = {
		manipulation : false,
		interaction : {
			dragNodes : true,
			dragView : true,
			hover : true,
			navigationButtons : true,
			keyboard : true
		},

		physics : {
			barnesHut : {
				gravitationalConstant : -3200,
				centralGravity : 0.5,
				springConstant : 0.01,
				damping : 0.5
			},
			minVelocity : 0.3,
			maxVelocity : 75
		},
		nodes : {
			shadow : false,
			borderWidth : 4,
			size : 30,
			scaling : {
				label : {
					min : 10,
					max : 20
				}
			},
			color : {
				border : '#222222',
				background : '#666666'
			},
			font : {
				color : '#666'
			}
		},
		edges : {
			arrows : {
				to : {
					enabled : true,
					scaleFactor : 1
				}
			},
			font : {
				align : 'top',
				size : 10
			},
			shadow : false,
			smooth : true,
			labelHighlightBold : true,
			color : '#b5afaf'
		},
		autoResize : true,
		width : '100%',
		height : graphHeight + 'px',
		groups:{
			resolved :{
				shape: 'icon',
				icon:{ face: 'FontAwesome', code: '\uf2d2', size: 30, color: '#ed6b75' }
			},
			apis:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/apis_categoria.svg',
				color: '#0e668c',
				size: 40
			},
			api:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/apis_nodo.svg',
				color: '#0e668c',
				size: 30
				
			},
			flows:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/flows_categoria.svg',
				color: '#0e668c',
				size: 40
			},
			flow:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/flows_nodo.svg',
				color: '#0e668c',
				size: 30				
			},
			webprojects:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/projects_categoria.svg',
				color: '#0e668c',
				size: 40
			},
			webproject:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/projects_nodo.svg',
				color: '#0e668c',
				size: 30
				
			},
			digitaltwins:{				
				shape: 'image',
				image: '/controlpanel/images/graphicons/digital_twin_categoria.svg',
				color: '#0e668c',
				size: 40			
				
			},
			digitaltwin:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/digital_twin_nodo.svg',
				color: '#0e668c',
				size: 30
			},
			licensing:{
				shape: 'icon',
				icon:{ face: 'FontAwesome', code: '\uf2d0', size: 30, color: 'blue' }
			},
			user:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/usuario.svg',
				color: '#0e668c',
				size: 50
			},
			users:{
				shape: 'icon',
				icon:{ face: 'FontAwesome', code: '\uf2be', size: 60, color: '#4B77BE' }
			},
			
			visualizations:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/visualizacion.svg',
				color: '#0e668c',
				size: 40
			},
			
			analytics:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/visualizacion.svg',
				color: '#0e668c',
				size: 40
			},
			clientplatform:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/devices_nodo_normal.svg',
				color: '#0e668c',
				size: 30
			},		
			deviceandsystems:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/devices_categoria.svg',
				color: '#0e668c',
				size: 40
			},			
			notebooks:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/notebook_cat.svg',
				size: 40
			},
			notebook:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/notebook.svg',
				size: 30
			},
			dashboards:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/dashboard_categoria.svg',
				size: 40				
			},
			script:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/script_nodo.svg',
				size: 30	
			},	
			scripts:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/scripts_categoria.svg',
				size: 40	
			},			
			pipelines:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/pipe_cat.svg',
				size: 40
			},
			pipeline:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/pipe.svg',
				size: 30
			},
			project:{
				shape: 'icon',
				icon:{face: 'FontAwesome', code: '\uf114', size: 30, color: '#c49f47' }
			},
			gadget:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/gadgets_nodo.svg',
				color: '#0e668c',
				size: 30
			},
			gadgets:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/gadgets_categoria.svg',
				color: '#0e668c',
				size: 40
			},					
			dashboard:{
					shape: 'image',
				image: '/controlpanel/images/graphicons/dashboard_nodo.svg',
				color: '#0e668c',
				size: 30
			},
			ontologies:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/ontologias_categoria.svg',
				color: '#0e668c',
				size: 40
			},
			ontology:{
					shape: 'image',
				image: '/controlpanel/images/graphicons/ontologias_nodo.svg',
				color: '#0e668c',
				size: 30
			},						
			Clusterdigitaltwins:{				
				shape: 'image',
				image: '/controlpanel/images/graphicons/digital_twin_categoria.svg',
				color: '#555',
				size: 60			
				
			},
			Clusterwebprojects:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/projects_categoria.svg',
				color: '#555',
				size: 60
			},
			Clusterdashboards:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/dashboard_categoria.svg',
				color: '#555',
				size: 60				
			},
			Clusterontologies:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/ontologias_categoria.svg',
				color: '#555',
				size: 60
			},
			Clusterapis:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/apis_categoria.svg',
				color: '#555',
				size: 60
			},
			Clusterdeviceandsystems:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/devices_categoria.svg',
				color: '#555',
				size: 60
			},	
			Clusternotebooks:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/notebook_cat.svg',
				color: '#555',
				size: 60
			},
			Clusterpipelines:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/pipe_cat.svg',
				color: '#555',
				size: 60
			},
			Clusterkps:{
				shape: 'icon',
				icon: { face: 'FontAwesome', code: '\uf2db', size: 45, color: '#5e738b'	}
			},
			Clusterscripts:{
				shape: 'icon',
				icon:{ face: 'FontAwesome',	code: '\uf0b0',	size: 45, color: '#5e738b' }
			},
			Clustergadgets:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/gadgets_categoria.svg',
				color: '#555',
				size: 60
			},			
			Clusterlicensing:{
				shape: 'icon',
				icon:{ face: 'FontAwesome', code: '\uf2d0', size: 60, color: '#c49f47' }
			},
			Clusterflows:{
				shape: 'image',
				image: '/controlpanel/images/graphicons/flows_categoria.svg',
				color: '#555',
				size: 60
			},
			Clusterresolved :{
				shape: 'icon',
				icon:{ face: 'FontAwesome',	code: '\uf2d2',	size: 60, color: '#ed6b75' }
			}
		}
	};

	// CONTROLLER PRIVATE FUNCTIONS
	// CREATE HTML TABLE TO VIEW GRAPH NODE INFO.
	var createRelationsInfoTable = function() {
		logControl ? console.log('createRelationsInfoTable() -> ') : '';

		var strInfo = '';

		// i18 labels
		var propertyCol = graphReg.propertyCol, valueCol = graphReg.valueCol, tableName = graphReg.tableName, tableSource = graphReg.tableSource, tableLinkS = graphReg.tableLinkS, tableLinkC = graphReg.tableLinkC, tableLinkBtn = graphReg.tableLinkBtn

		$("#TableInfoRelations").hide();
		strInfo = '<caption style="text-align:center">Attribute relations</caption><thead></thead>'
				+ '<tbody></tbody>'

		$("#TableInfoRelations").empty();
		$("#TableInfoRelations").html(strInfo);
	}

	// CREATE HTML TABLE TO VIEW GRAPH NODE INFO.
	var createGraphInfoTable = function() {
		logControl ? console.log('createGraphInfoTable() -> ') : '';

		var strInfo = '';

		// i18 labels
		var propertyCol = graphReg.propertyCol, valueCol = graphReg.valueCol, tableName = graphReg.tableName, tableSource = graphReg.tableSource, tableLinkS = graphReg.tableLinkS, tableLinkC = graphReg.tableLinkC, tableLinkBtn = graphReg.tableLinkBtn

		$("#TableInfoNetwork").hide();

		strInfo = '<thead>'
				+ '<tr style="height:40px">'
				+ '<th class="bg-grey-steel text-center font-grey-gallery">'
				+ propertyCol
				+ '</th>'
				+ '<th class="bg-grey-steel  text-center font-grey-gallery">'
				+ valueCol
				+ '</th>'
				+ '</tr>'
				+ '</thead>'
				+ '<tbody>'
				+ '	<tr>'
				+ '		<td class="uppercase  text-center font-grey-mint">'
				+ tableName
				+ '</td>'
				+ '		<td id="id_nombre" class="long-text  text-center "></td>'
				+ '	</tr>'
				+ '	<tr>'
				+ '		<td class="uppercase  text-center font-grey-mint"> '
				+ tableSource
				+ '</td>'
				+ '		<td id="id_source" class="long-text  text-center "> </td>'
				+ '	</tr>'
				+ '	<tr id="id_tr_enlaceS">'
				+ '		<td class="uppercase  text-center font-grey-mint"> '
				+ tableLinkS
				+ '</td>'
				+ '		<td class="long-text  text-center "><a id="id_enlaceS" class="btn btn-circle blue btn-outline"><span> '
				+ tableLinkBtn
				+ '</span> </a></td>'
				+ '	</tr>'
				+ '	<tr id="id_tr_enlaceC">'
				+ '		<td class="uppercase  text-center font-grey-mint">'
				+ tableLinkC
				+ '</td>'
				+ '		<td class="long-text  text-center "><a id="id_enlaceC" class="btn btn-circle blue btn-outline"><span> '
				+ tableLinkBtn + '</span> </a></td>' + '	</tr>';

		strInfo += '</tbody>';

		$("#TableInfoNetwork").empty();
		$("#TableInfoNetwork").html(strInfo);
	}

	// DRAW RELATIONS INFO ON TABLE
	// DRAW NODE INFO ON TABLE
	var drawRelationsInfo = function(currentNode) {
		logControl ? console.log('drawRelationsInfo() -> ') : '';
		$("#TableInfoRelations tbody").html(); // clean graph info table
		var relations = currentNode.relations;
		var avoidRelationDuplicates = []
		var info = '';
		relations.forEach(function(relation) {
			
			if(avoidRelationDuplicates.indexOf(relation.srcOntology + ':'+ relation.dstOntology) == -1){
				info += '<tr>' + '<td class="text-center font-grey-mint"><b>'
				+ relation.srcOntology + '</b></td>'
				+ '<td class="long-text text-center"><b>'
				+ relation.dstOntology + '</b></td></tr>';
				avoidRelationDuplicates.push(relation.srcOntology + ':'
						+ relation.dstOntology)
			}
					
			info += '<tr><td class="text-center font-grey-mint">'
					+ relation.srcAttribute + '</td>'
					+ '<td class="long-text text-center">'
					+ relation.dstAttribute + '</td></tr>';
			
		});

		// fill node info to table
		$("#TableInfoRelations").show();
		$("#TableInfoRelations tbody").html();
		$("#TableInfoRelations tbody").html(info);

	}

	// DRAW NODE INFO ON TABLE
	var drawGraphInfo = function(currentNode) {
		logControl ? console.log('drawGrafoInfo() -> ') : '';

		// null values to ''
		$.each(currentNode, function(key, value) {
			if (value == null) {
				currentNode[key] = "";
			}
		});

		cleanTable(); // clean graph info table

		// fill node info to table
		$("#TableInfoNetwork").show();
		$("#id_nombre").html(currentNode.label);
		$("#id_source").html(currentNode.nameSource);

		if (currentNode.linkTarget) {
			$("#id_tr_enlaceS").show();
			$("#id_enlaceS").attr("href", currentNode.linkTarget);
		}
		if (currentNode.linkCreate) {
			$("#id_tr_enlaceC").show();
			$("#id_enlaceC").attr("href", currentNode.linkCreate);
		}
		// if it has relations create them
		if (currentNode.relations && currentNode.relations.length > 0)
			drawRelationsInfo(currentNode);
		else
			$("#TableInfoRelations").hide();
		$("#table-info-container").show();
	}

	// AUX. CLEAN GRAPH INFO TABLE
	var cleanTable = function() {

		$("#id_nombre").html();
		$("#id_source").html();
		if ($("#id_enlaceS")) {
			$("#id_enlaceS").removeAttr("href");
		}
		if ($("#id_enlaceC")) {
			$("#id_enlaceC").removeAttr("href");
		}
		$("#id_tr_enlaceS,#id_tr_enlaceC").hide();
	}

	// HANDLE HIERARCHICAL OPTION BUTTONS
	var handleHierarchical = function() {
		logControl ? console.log('handleHierarchical() -> ') : '';

		$("#hierarchicalOptions > li > a").each(function() {
			$(this).on('click', function() {
				hierarchicalNetwork(this);
			});
		});
	}

	// HANDLE CLUSTER OPTION BUTTONS
	var handleCluster = function() {
		logControl ? console.log('handleCluster() -> ') : '';

		$("#clusterOptions > li > a").each(function() {
			$(this).on('click', function() {
				clusterGraph(this);
			});
		});
	}

	// HIERARCHICAL GRAPH REDRAW
	var hierarchicalNetwork = function(obj) {
		logControl ? console.log('hierarchicalNetwork() -> ') : '';

		var direction = $(obj).attr("data-action");
		if (direction != "") {

			destroyNetwork();
			if (direction != "default") {
				// setting graph option properties.
				options.layout = {};
				options.layout.hierarchical = {};
				options.layout.hierarchical.direction = direction;
				options.layout.hierarchical.sortMethod = 'directed';
			} else {
				delete options.layout;
			}

			// creating gpraph with this hierarchical configuration data:
			// graphData
			containerNetwork = document.getElementById('networkVis');
			network = new vis.Network(containerNetwork, graphData, options);
			network.on("selectNode", function(params) {

				if (params.nodes.length == 1) {
					if (network.isCluster(params.nodes[0]) == true) {
						network.openCluster(params.nodes[0]);
					}

					// selected node
					var selectedId = network.getSelection().nodes;
					var currentNode = graphData.nodes.get(selectedId);

					// draw node info to graphInfoTable.
					if (currentNode[0] != undefined) {
						drawGraphInfo(currentNode[0]);
					}
				}

			});
		}
	}

	// CLUSTER GRAPH by CONNECTION
	var clusterByConnection = function() {
		logControl ? console.log('clusterByConnection() -> ') : '';
		network.setData(graphData);
		network.clusterByConnection(1)
	}

	// CLUSTER GRAPH REDRAW
	var clusterGraph = function(obj) {
		logControl ? console.log('clusterGraph() -> ') : '';

		var action = $(obj).attr("data-action");
		if (graphData.nodes.length > 0) {
			if (action == "source") {
				clusterGraphBySource();
			}
			if (action == "type") {
				clusterGraphByType();
			}
		}
	}

	// CLUSTER GRAPH by SOURCE
	function clusterGraphBySource() {
		logControl ? console.log('clusterGraphBySource() ->') : '';

		network.setData(graphData);
		var clusterOptionsByData;
		var target = '';

		for (var i = 0; i < ArraySource.length; i++) {
			var idSource = ArraySource[i].id;
			var target = ArraySource[i].target;
			var group = ArraySource[i].group;
			clusterOptionsByData = {

				joinCondition : function(childOptions) {
					return childOptions.category == target;
				},
				processProperties : function(clusterOptions, childNodes,
						childEdges) {
					var totalMass = 0;
					for (var i = 0; i < childNodes.length; i++) {
						totalMass += childNodes[i].mass;
					}
					clusterOptions.mass = totalMass;
					return clusterOptions;
				},
				clusterNodeProperties : {
					id : 'cluster:' + idSource,
					borderWidth : 3,
					group : 'Cluster' + group,
					'label' : target
				}
			};
			network.cluster(clusterOptionsByData);
		}

	}

	// CLUSTER GRAPH by TYPE
	function clusterGraphByType() {
		logControl ? console.log('clusterGraphByType() -> ') : '';

		var ArrayType = [ 'licensing', 'resolved' ];
		network.setData(graphData);
		var clusterOptionsByData;
		for (var i = 0; i < ArrayType.length; i++) {

			var type = ArrayType[i];
			clusterOptionsByData = {
				joinCondition : function(childOptions) {
					return childOptions.type == type;
				},
				processProperties : function(clusterOptions, childNodes,
						childEdges) {
					var totalMass = 0;
					for (var i = 0; i < childNodes.length; i++) {
						totalMass += childNodes[i].mass;
					}
					clusterOptions.mass = totalMass;
					return clusterOptions;
				},
				clusterNodeProperties : {
					id : 'cluster:' + type,
					borderWidth : 3,
					group : 'Cluster' + type,
					label : type
				}
			};
			network.cluster(clusterOptionsByData);
		}
	}

	// CLUSTER GRAPH by PROJECT
	function clusterGrafoByProject() {
		logControl ? console.log('clusterGrafoByProject') : '';

		network.setData(graphData);
		var clusterOptionsByData;
		$.each(jsonProjects, function(project, value) {
			clusterOptionsByData = {
				joinCondition : function(childOptions) {
					return childOptions.project == project;
				},
				processProperties : function(clusterOptions, childNodes,
						childEdges) {
					var totalMass = 0;
					for (var i = 0; i < childNodes.length; i++) {
						totalMass += childNodes[i].mass;
					}
					clusterOptions.mass = totalMass;
					return clusterOptions;
				},
				clusterNodeProperties : {
					id : 'cluster:' + project,
					borderWidth : 3,
					group : 'projectCluster',
					label : project
				}
			};
			network.cluster(clusterOptionsByData);
		});
	}

	// AUX. DESTROY GRAPH
	var destroyNetwork = function() {
		if (network !== null) {
			network.destroy();
			network = null;
		}
	}

	// AUX. ON LOAD GRAPH DATA GET INTO FORMAT.
	var getDataGraph = function() {
		logControl ? console.log('getDataGraph() -> ') : '';

		var graphDataObj = {
			nodes : [],
			edges : []
		}, ArrayNodes = [], ArrayEdges = [], idRef = {}, target = '', nodes = {}, avoidDuplicates = [];

		if ($("#id_panel_botones")) {
			$("#id_panel_botones").hide();
		}
		$("#networkVis").hide();

		// handle data
		if (!$.isEmptyObject(links)) {

			if ($("#id_panel_botones")) {
				$("#id_panel_botones").show();
			}
			// main node Loop - create nodes with info.
			$.each(links, function(Index, Node) {

				if ($.inArray(Node.nameTarget, avoidDuplicates) === -1) {
					if (Node.type === undefined) {
						Node.type = "";
					}
					idRef[Node.target] = Index;

					// node format, adjust type to suit if no-type
					var dataJson = {
						'id' : Index,
						'label' : Node.nameTarget,
						'source' : Node.source,
						'type' : Node.type || 'suit',
						'linkTarget' : Node.linkTarget,
						'nameSource' : Node.nameSource,
						'group' : Node.classTarget,
						'title' : Node.title,
						'linkCreate' : Node.linkCreate,
						'relations' : Node.relations
					};

					group = dataJson.group.toLowerCase();
					type = dataJson.type.toLowerCase();

					if (options.groups[group] !== undefined) {
						dataJson.group = group;
					} else if (options.groups[type] !== undefined) {
						dataJson.group = type;
					} else {
						dataJson.group = 'licensing';
					}

					dataJson.category = Node.source;
					if (Node.type == 'suit') {
						dataJson.category = Node.target;
						ArraySource.push({
							'id' : Index,
							'target' : Node.target,
							'group' : dataJson.group
						});
					}
					ArrayNodes.push(dataJson);
					avoidDuplicates.push(Node.nameTarget);
				}

			});
			var edgesAvoidDuplicates = [];
			// creating relationships between nodes.
			$.each(links, function(Index, Node) {
				if ((idRef[Node.source] !== undefined)
						&& (idRef[Node.target] !== undefined)
						&& (idRef[Node.source] !== idRef[Node.target])) {
					if (Node.classTarget && Node.classSource
							&& Node.classSource == Node.classTarget
							&& Node.classTarget == "ontology") {
						if (edgesAvoidDuplicates.indexOf(idRef[Node.source]
								+ ':' + idRef[Node.target]) == -1) {
							ArrayEdges.push({
								from : idRef[Node.source],
								to : idRef[Node.target],
								dashes : true,
								color : '#5789ad'
							});
							edgesAvoidDuplicates.push(idRef[Node.source] + ':'
									+ idRef[Node.target]);
						}
					} else
						ArrayEdges.push({
							from : idRef[Node.source],
							to : idRef[Node.target]
						});
				} else {
					// console.log("Verificar nodo:"+Index+' -
					// Source:'+Node.source+' Target:'+Node.target);
				}
			});

			// retrieving nodes and relations
			graphDataObj.nodes = new vis.DataSet(ArrayNodes);
			graphDataObj.edges = new vis.DataSet(ArrayEdges);
			$("#networkVis").show();
		}
		// RETURNING GRAPH DATA FORMATTED
		return graphDataObj;
	}

	// LOAD GRAPH DATA FROM SERVER
	var loadNetwork = function(all) {
		logControl ? console.log('loadNetwork() -> ') : '';

		destroyNetwork();
		var url = "/controlpanel/getgraph?all="+all;
		if(graphReg.projectId != null)
			url = "/controlpanel/getgraph/project/" + graphReg.projectId;
		// AJAX CALL - get GRAPH data
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
		$.ajax({
			url : url,
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'GET',
			dataType : 'json',
			contentType : 'text/html',
			success : function(data) {

				links = data;
				graphData = getDataGraph();

				if (graphData.nodes.length > 0) {
					containerNetwork = document.getElementById('networkVis');
					network = new vis.Network(containerNetwork, graphData,
							options);
					network.on("selectNode", function(params) {

						if (params.nodes.length == 1) {
							if (network.isCluster(params.nodes[0]) == true) {
								network.openCluster(params.nodes[0]);
							}

							// Selected Node
							var selectedId = network.getSelection().nodes;
							var currentNode = graphData.nodes.get(selectedId);
							if (currentNode[0] != undefined) {
								drawGraphInfo(currentNode[0]);
							}
						}
					});
				}
			},
			error : function(data, status, er) {
				$.alert({
					title : 'GRAPH ERROR!',
					theme : 'dark',
					icon : 'fa fa-warning',
					type : 'red',
					content : 'Error loading graph info on graph Controller.'
				});
			}
		});

		// ############## MOCKUP ######################
		/*
		 * links = graphJson.data graphData = getDataGraph();
		 * 
		 * if( graphData.nodes.length > 0 ){ containerNetwork =
		 * document.getElementById('networkVis'); network = new
		 * vis.Network(containerNetwork, graphData, options);
		 * network.on("selectNode", function(params){
		 * 
		 * if ( params.nodes.length == 1 ) { if ( network.isCluster(
		 * params.nodes[0] ) == true){ network.openCluster( params.nodes[0] ); } //
		 * Selected Node var selectedId = network.getSelection().nodes; var
		 * currentNode = graphData.nodes.get(selectedId); if( currentNode[0] !=
		 * undefined ){ drawGraphInfo(currentNode[0]); } } }); }
		 */
		// ############## MOCKUP ######################
	}

	// HANDLE GRAPH HEIGHT CONTAINER
	var handleGraphHeight = function() {
		logControl ? console.log('handleGraphHeight() -> ') : '';
		// Add Height
		$('#btn-addH').on('click', function() {
			$('#networkVis').height(function(index, height) {
				return (height + 100);
			});
			graphHeight = graphHeight + 100;
			network.redraw();
		});
		// Remove Height
		$('#btn-remH').on('click', function() {
			$('#networkVis').height(function(index, height) {
				return (height - 100);
			});
			if (parseInt($('#networkVis').css('height')) <= 500) {
				$('#networkVis').css('height', 500);
				graphHeight = 500;
			} else {
				graphHeight = graphHeight - 100;
			}

		});
		// Restore Height
		$('#btn-resH').on('click', function() {
			$('#networkVis').css('height', graphHeight);
		});
	}

	// TOGGLE GRAPH INFO TABLE
	var toggleGraphInfoTable = function() {

		$('#btn-graphInfo').on('click', function() {
			$('#TableInfoNetwork').fadeToggle()
		});

	}

	// CONTROLLER PUBLIC FUNCTIONS
	return {

		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load : function(Data) {
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return graphReg = Data;
		},

		// INIT() CONTROLLER INIT CALLS
		init : function(all) {
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			createGraphInfoTable();
			handleGraphHeight();
			handleHierarchical();
			handleCluster();
			toggleGraphInfoTable();
			createRelationsInfoTable();
			loadNetwork(all);
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	GraphController.load(graphJson);

	// AUTO INIT CONTROLLER.
	GraphController.init(false);
});
