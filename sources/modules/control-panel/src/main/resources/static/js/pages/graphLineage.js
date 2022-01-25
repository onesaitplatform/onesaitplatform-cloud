var GraphLineageController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var APPNAME = 'Onesait Platform Control Panel', LIB_TITLE = 'Graph Lineage Controller', logControl = 0;

	// GRAPH INITIALIZATION VARS AND CONST.
	var links = [], nodes = [], data = [], edges = [], ArraySource = [], jsonProjects = {}, options = {}, jsonIndex = {}, graphData = {
		nodes : [],
		edges : []
	}, containerNetwork = new Object(), network = null, graphHeight = 500;
	
	var resourceSelected;
	var typeSelected;

	// GRAPH OPTIONS DEFAULTS
	options = {
		manipulation : false,
		edges: {
			smooth: {
				type: "horizontal",
				forceDirection:"horizontal",
				roundness: .6,
			  },
			font: {
				size: 11,
			},			
			color: {
			  color:'#3982D0',
			  highlight:'#3982D0',
			  hover: '#3982D0',
			  inherit: 'from',
			  opacity:1.0
			},
			arrows: {
				  to: {
					enabled: true,					
					scaleFactor: 0.4,					
					type: "arrow"
				  },				  
				  from: {
					enabled: true,					
					imageHeight: 6,
					imageWidth: 6,
					scaleFactor: 0,
					type: "image",
					src: "/controlpanel/vendor/vis/img/dot.svg",					
				  }
			},
			endPointOffset: {
			  from: -4,
			  to: 0
			},
			hoverWidth: 0,				
			labelHighlightBold: true,
		},
		nodes: {
			shape: "image",				
			shapeProperties: {					
				borderRadius: 2,
			},
			borderWidth: 1,
			borderWidthSelected: 1,				
			chosen: true,			
			font: {
			  color: '#1B6EAA',
			  size: 12, // px
			  face: 'Soho',
			  background: 'none',
			  strokeWidth: 0, // px
			  strokeColor: '#ffffff',
			  align: 'center',
			},
			margin:{
				top: 14,
				right:18,
				bottom:12,
				left:16
			},
			color: {
				border: '#E6E8E9',
				background: '#FFF',
				highlight: {
					border: '#1B6EAA',
					background: '#FFF'
				},
				hover: {
					border: '#1B6EAA',
					background: '#FFF'
				}
			},
		},
		configure: {
        enabled: false,
        showButton: true
		},
		physics: {
			hierarchicalRepulsion: {
			  centralGravity: 0,
			  springLength: 70,
			  nodeDistance: 115,
			  avoidOverlap: 1
			},
			maxVelocity: 39,
			minVelocity: 0.19,
			solver: "hierarchicalRepulsion"
		  },
		/*physics: {
			barnesHut : {
				gravitationalConstant: -4000,
				centralGravity: 0,
				springLength: 175,
				springConstant: 0.04,
				damping: 0.8,
				avoidOverlap: 1
			},			
			stabilization: true,
			minVelocity : 0.5,
			maxVelocity : 25
		},*/
		layout: {
			hierarchical: {
			  enabled: true,
			  levelSeparation: 175,
			  nodeSpacing: 175,
			  treeSpacing: 175,
			  direction: "LR",
			  sortMethod: "directed"
			}
		},
			
		interaction:{
			dragNodes : true,
			dragView : true,
			hover : true,
			navigationButtons : true,
			keyboard : true
		  },
		autoResize : true,
		width : '100%',
		height : graphHeight + 'px',
		groups:{
			resolved :{
				shape: 'icon',
				icon:{ face: 'FontAwesome', code: '\uf2d2', size: 30, color: '#ed6b75' }
			},
			api:{
				shape: 'image',				
				
			},
			licensing:{
				shape: 'image',
				
			},
			dashboard:{
				shape: 'image',
				
			},
			digitalclient:{
				shape: 'image',				
			},
			datasource:{
				shape: 'image',			
			},
			dataflow:{
				shape: 'image',				
			},
			gadget:{
				shape: 'image',				
			},
			ontology:{
				shape: 'image',							
			},
			notebook:{
				shape: 'image',
					
			},
			microservice:{
				shape: 'image',				
			},
			flow:{
				shape: 'image',				
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
	
	var drawPropertiesInfo = function(currentNode) {
		logControl ? console.log('drawPropertiesInfo() -> ') : '';
		var properties = currentNode.properties;

		var strInfo = '';

		// i18 labels
		var propertyCol = graphReg.propertyCol, valueCol = graphReg.valueCol;

		$("#TableInfoNetwork").hide();
		$("#TableInfoNetwork").empty();

		if(properties){
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
				+ '<tbody if="tbody-properties">';
		
			Object.keys(properties).forEach(function(key) {
				strInfo += '<tr>' + '<td class="uppercase  text-center font-grey-mint">'
				+ key + '</td>'
				+ '<td class="long-text  text-center">'
				+ properties[key] + '</td></tr>';
			});
	
			strInfo += '</tbody>';
	
			$("#TableInfoNetwork").empty();
			$("#TableInfoNetwork").html(strInfo);
		}
		$("#relationBtns").show();
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

		drawPropertiesInfo(currentNode);
		
		cleanTable(); // clean graph info table

		// fill node info to table
		$("#TableInfoNetwork").show();
		$("#id_nombre").html(currentNode.title);
		$("#id_source").html(currentNode.nameSource);
		$("#selectedTypeEntity").val(currentNode.group.toUpperCase());
		
		if(currentNode.isExternal){
			$("#id_tr_relation_delete").show();
			if(currentNode.group == "api"){
				$("#id_tr_relation_delete").attr("onclick", "GraphLineageController.showModalDeleteRelation('" + currentNode.title + " - V" + currentNode.properties.Version+"','" + currentNode.group + "');");
			}else{
				$("#id_tr_relation_delete").attr("onclick", "GraphLineageController.showModalDeleteRelation('" + currentNode.title + "','" + currentNode.group + "');");
			}
		}else{
			$("#id_tr_relation_delete").hide();
		}
		
		if(currentNode.group=='api'){
			$("#id_relation_origin").attr("onclick", "GraphLineageController.showModalOrigin('" + currentNode.title + " - V" + currentNode.properties.Version+"');");
			$("#id_relation_target").attr("onclick", "GraphLineageController.showModalTarget('" + currentNode.title + " - V" + currentNode.properties.Version+"');");
		}else{
			$("#id_relation_origin").attr("onclick", "GraphLineageController.showModalOrigin('" + currentNode.title + "');");
			$("#id_relation_target").attr("onclick", "GraphLineageController.showModalTarget('" + currentNode.title + "');");
		}
		
		$("#table-info-container").show();

	}

	// AUX. CLEAN GRAPH INFO TABLE
	var cleanTable = function() {
		$("#id_relation_origin").removeAttr("onclick");
		$("#id_relation_target").removeAttr("onclick");
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
		
		// calculate with for draw node in image svg mode.
		var calculateTextWidth = function(label,font){
		
			const elementsWidth = 60;
			var totalWidth = 0;
			var inputText = label || 'Node';
			var fontText  = font  || '13px Arial';
			
			var label_canvas = document.createElement("canvas"); 
			var context = label_canvas.getContext("2d"); 
			context.font = font; 
			width = context.measureText(inputText).width; 
			totalWidth = elementsWidth + Math.ceil(width);	
			
			return totalWidth;		  
		};
		
		// create svg with title, font and group
		var createSvg = function(title,font,group){
			
			var svg = '';
			
			// icons for groups
			var groupIcon = {
				"ontology": '<svg width="16" height="16" viewBox="-1 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M10.2511 1.31952C10.2515 1.31956 10.2493 1.32266 10.2434 1.32868C10.2477 1.32249 10.2507 1.31948 10.2511 1.31952ZM9.17859 1.21115C9.52626 1.30597 9.79082 1.40633 9.97738 1.5C9.79082 1.59367 9.52626 1.69403 9.17859 1.78885C8.26957 2.03676 6.96869 2.2 5.5 2.2C4.03131 2.2 2.73043 2.03676 1.82141 1.78885C1.47374 1.69403 1.20917 1.59367 1.02261 1.5C1.20917 1.40633 1.47374 1.30597 1.82141 1.21115C2.73043 0.963236 4.03131 0.8 5.5 0.8C6.96869 0.8 8.26957 0.963236 9.17859 1.21115ZM0.748928 1.31952C0.749329 1.31948 0.75228 1.32249 0.756579 1.32868C0.750677 1.32266 0.748528 1.31956 0.748928 1.31952ZM10.1 2.32257C9.11706 2.7306 7.42389 3 5.5 3C3.57611 3 1.88294 2.7306 0.9 2.32257V4.93112C0.939871 4.97252 1.01417 5.03217 1.13207 5.10499C1.36574 5.24932 1.7127 5.41015 2.13453 5.5608C2.97884 5.86234 4.05746 6.1 5 6.1C6.37625 6.1 7.71177 5.85699 8.69745 5.55504C9.19163 5.40366 9.58417 5.24162 9.84485 5.09607C9.97374 5.0241 10.0554 4.96439 10.1 4.9224V2.32257ZM10.1 5.86628C9.78853 6.02442 9.38876 6.17996 8.93177 6.31996C7.87719 6.64301 6.4627 6.9 5 6.9C3.94254 6.9 2.77116 6.63766 1.86547 6.3142C1.50184 6.18433 1.17049 6.04089 0.9 5.89432V8.93112C0.939871 8.97253 1.01417 9.03217 1.13207 9.10499C1.36574 9.24932 1.7127 9.41015 2.13453 9.5608C2.97884 9.86234 4.05746 10.1 5 10.1C6.71405 10.1 8.04548 9.85487 8.93601 9.55803C9.38326 9.40894 9.70621 9.25116 9.90847 9.11415C10.0102 9.0452 10.0687 8.99004 10.0976 8.95471L10.1 8.9518V5.86628ZM0.1 5V9V12.5C0.1 12.7498 0.271877 12.9237 0.369411 13.009C0.489093 13.1138 0.64512 13.2108 0.814066 13.2995C1.15568 13.4788 1.61797 13.6591 2.13069 13.8193C3.15406 14.1391 4.44618 14.4 5.5 14.4C7.21106 14.4 8.5104 14.1447 9.396 13.8119C9.83712 13.6462 10.1884 13.4563 10.4378 13.2589C10.5623 13.1603 10.6713 13.0521 10.752 12.9347C10.8309 12.82 10.9 12.6711 10.9 12.5V9V5V1.78621C10.9656 1.69356 11 1.59787 11 1.5C11 0.671573 8.53757 0 5.5 0C2.46243 0 0 0.671573 0 1.5C0 1.59787 0.0343708 1.69356 0.1 1.7862V5ZM0.9 12.4103V9.89432C1.17049 10.0409 1.50184 10.1843 1.86547 10.3142C2.77116 10.6377 3.94254 10.9 5 10.9C6.78595 10.9 8.20452 10.6451 9.18899 10.317C9.54489 10.1983 9.85101 10.0678 10.1 9.93208V12.4704C10.0982 12.4734 10.0958 12.477 10.0929 12.4813C10.0702 12.5143 10.0239 12.5662 9.9412 12.6317C9.77608 12.7624 9.50503 12.9163 9.11458 13.0631C8.33705 13.3553 7.13639 13.6 5.5 13.6C4.55382 13.6 3.34594 13.3609 2.36931 13.0557C1.88203 12.9034 1.46932 12.7399 1.18593 12.5912C1.04579 12.5176 0.952895 12.4556 0.9 12.4103Z" fill="#1B6EAA"/></svg>',
				"datasource": '<svg width="16" height="16" viewBox="0 0 16 14" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M10 0.8H1C0.889543 0.8 0.8 0.889543 0.8 1V4C0.8 4.11046 0.889543 4.2 1 4.2H10C10.1105 4.2 10.2 4.11046 10.2 4V1C10.2 0.889543 10.1105 0.8 10 0.8ZM1 0C0.447715 0 0 0.447715 0 1V4C0 4.55228 0.447715 5 1 5H10C10.5523 5 11 4.55228 11 4V2.9H11.5C12.3837 2.9 13.1 3.61634 13.1 4.5V5C13.1 5.88366 12.3837 6.6 11.5 6.6H4.5C3.17452 6.6 2.1 7.67452 2.1 9V9.5C2.1 10.8255 3.17452 11.9 4.5 11.9H5V13C5 13.5523 5.44771 14 6 14H15C15.5523 14 16 13.5523 16 13V10C16 9.44772 15.5523 9 15 9H6C5.44772 9 5 9.44771 5 10V11.1H4.5C3.61634 11.1 2.9 10.3837 2.9 9.5V9C2.9 8.11634 3.61634 7.4 4.5 7.4H11.5C12.8255 7.4 13.9 6.32548 13.9 5V4.5C13.9 3.17452 12.8255 2.1 11.5 2.1H11V1C11 0.447715 10.5523 0 10 0H1ZM15.2 10C15.2 9.88954 15.1105 9.8 15 9.8H6C5.88954 9.8 5.8 9.88954 5.8 10V13C5.8 13.1105 5.88954 13.2 6 13.2H15C15.1105 13.2 15.2 13.1105 15.2 13V10Z" fill="#1B6EAA"/></svg>',
				"api": '<svg width="17" height="17" viewBox="0 0 17 15" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M2.92011 2.09046C3.5373 0.903569 4.76581 0 6.9 0C10.7692 0 12.3513 2.10639 12.7264 3.50763C13.4395 3.54142 14.252 3.69535 14.9273 4.12504C15.7378 4.64083 16.3 5.52257 16.3 6.9C16.3 9.09103 14.8417 10.3764 14.0789 10.7578L13.9944 10.8H13.9H8.2V11.43C8.71926 11.5354 9.14112 11.9091 9.31465 12.4H15C15.2209 12.4 15.4 12.5791 15.4 12.8C15.4 13.0209 15.2209 13.2 15 13.2H9.37C9.23102 13.8847 8.62569 14.4 7.9 14.4C7.17431 14.4 6.56899 13.8847 6.43001 13.2H0.800001C0.579087 13.2 0.400002 13.0209 0.400002 12.8C0.400002 12.5791 0.579088 12.4 0.800001 12.4H6.48535C6.636 11.9738 6.97379 11.636 7.4 11.4854V10.8H2.4H2.35076L2.30299 10.7881C1.55633 10.6014 0 9.76823 0 7.9C0 6.94115 0.505517 6.25181 1.09228 5.79649C1.53048 5.45644 2.02941 5.23275 2.44727 5.10376C2.33859 4.2361 2.40185 3.08711 2.92011 2.09046ZM3.62989 2.45954C3.1436 3.3947 3.1368 4.54436 3.29223 5.32155L3.37068 5.71379L2.97845 5.79223C2.60911 5.8661 2.04485 6.06991 1.58273 6.42851C1.12782 6.78152 0.800002 7.25885 0.800002 7.9C0.800002 9.1984 1.8554 9.83309 2.45196 10H13.8002C14.396 9.66297 15.5 8.62913 15.5 6.9C15.5 5.77743 15.0622 5.15917 14.4978 4.79996C13.9036 4.42187 13.1181 4.3 12.4 4.3H12.0531L12.004 3.95657C11.8663 2.99241 10.7065 0.8 6.9 0.8C5.03419 0.8 4.09604 1.5631 3.62989 2.45954Z" fill="#1B6EAA"/></svg>',
				"digitalclient": '<svg width="15" height="16" viewBox="0 0 15 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M8.12127 1.00916C8.12116 1.00918 8.12123 1.00917 8.12149 1.00912C8.12408 1.00862 8.14576 1.00446 8.19286 1.00196C8.2413 0.999375 8.30534 0.999107 8.38448 1.00287C8.54271 1.01038 8.74747 1.03333 8.98571 1.07814C9.4625 1.16782 10.0542 1.34141 10.6548 1.63605C11.852 2.22333 13.0597 3.27576 13.5149 5.09675L14.4851 4.85424C13.9403 2.67487 12.4814 1.41826 11.0952 0.73826C10.4042 0.399262 9.725 0.199654 9.17054 0.0953719C8.89315 0.0432003 8.64219 0.0139821 8.43193 0.00399312C8.23429 -0.00539661 8.03459 5.07777e-05 7.87873 0.0390161L8.12127 1.00916ZM11.5713 5.25731C10.4816 3.44123 9.06907 3.37318 8.62127 3.48513L8.37873 2.51499C9.26426 2.29361 11.1184 2.55889 12.4287 4.74282L11.5713 5.25731ZM2 5.30003H9C9.66274 5.30003 10.2 5.83729 10.2 6.50003V13.5C10.2 14.1628 9.66274 14.7 9 14.7H2C1.33726 14.7 0.8 14.1628 0.8 13.5V6.50003C0.8 5.83729 1.33726 5.30003 2 5.30003ZM0 6.50003C0 5.39547 0.895431 4.50003 2 4.50003H9C10.1046 4.50003 11 5.39547 11 6.50003V13.5C11 14.6046 10.1046 15.5 9 15.5H2C0.895431 15.5 0 14.6046 0 13.5V6.50003ZM2.5 12.5C2.22386 12.5 2 12.7239 2 13C2 13.2762 2.22386 13.5 2.5 13.5H8.5C8.77614 13.5 9 13.2762 9 13C9 12.7239 8.77614 12.5 8.5 12.5H2.5Z" fill="#1B6EAA"/></svg>',
				"dataflow": '<svg width="16" height="16" viewBox="0 0 16 14" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M10 0.8H1C0.889543 0.8 0.8 0.889543 0.8 1V4C0.8 4.11046 0.889543 4.2 1 4.2H10C10.1105 4.2 10.2 4.11046 10.2 4V1C10.2 0.889543 10.1105 0.8 10 0.8ZM1 0C0.447715 0 0 0.447715 0 1V4C0 4.55228 0.447715 5 1 5H10C10.5523 5 11 4.55228 11 4V2.9H11.5C12.3837 2.9 13.1 3.61634 13.1 4.5V5C13.1 5.88366 12.3837 6.6 11.5 6.6H4.5C3.17452 6.6 2.1 7.67452 2.1 9V9.5C2.1 10.8255 3.17452 11.9 4.5 11.9H5V13C5 13.5523 5.44771 14 6 14H15C15.5523 14 16 13.5523 16 13V10C16 9.44772 15.5523 9 15 9H6C5.44772 9 5 9.44771 5 10V11.1H4.5C3.61634 11.1 2.9 10.3837 2.9 9.5V9C2.9 8.11634 3.61634 7.4 4.5 7.4H11.5C12.8255 7.4 13.9 6.32548 13.9 5V4.5C13.9 3.17452 12.8255 2.1 11.5 2.1H11V1C11 0.447715 10.5523 0 10 0H1ZM15.2 10C15.2 9.88954 15.1105 9.8 15 9.8H6C5.88954 9.8 5.8 9.88954 5.8 10V13C5.8 13.1105 5.88954 13.2 6 13.2H15C15.1105 13.2 15.2 13.1105 15.2 13V10Z" fill="#1B6EAA"/></svg>',
				"digitalflow": '<svg width="16" height="14" viewBox="0 0 16 14" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M13.8 0H1.8C0.805888 0 0 0.805887 0 1.8V8.8C0 9.79411 0.805888 10.6 1.8 10.6H7.8V12.8H6.2C5.97909 12.8 5.8 12.9791 5.8 13.2C5.8 13.4209 5.97909 13.6 6.2 13.6H10.4C10.6209 13.6 10.8 13.4209 10.8 13.2C10.8 12.9791 10.6209 12.8 10.4 12.8H8.8V10.6H13.8C14.7941 10.6 15.6 9.79411 15.6 8.8V1.8C15.6 0.805887 14.7941 0 13.8 0ZM8.8 9.8H7.8H1.8C1.24772 9.8 0.8 9.35229 0.8 8.8V1.8C0.8 1.24772 1.24772 0.8 1.8 0.8H13.8C14.3523 0.8 14.8 1.24772 14.8 1.8V8.8C14.8 9.35229 14.3523 9.8 13.8 9.8H8.8ZM3 2.2C3 2.08954 3.08954 2 3.2 2H5.8C5.91046 2 6 2.08954 6 2.2V2.6H8H8.4V3V6.6H10V6.2C10 6.08954 10.0895 6 10.2 6H12.8C12.9105 6 13 6.08954 13 6.2V7.8C13 7.91046 12.9105 8 12.8 8H10.2C10.0895 8 10 7.91046 10 7.8V7.4H8H7.6V7V3.4H6V3.8C6 3.91046 5.91046 4 5.8 4H3.2C3.08954 4 3 3.91046 3 3.8V2.2Z" fill="#1B6EAA"/></svg>',
				"dashboard":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M2.05111 0H13.9489C14.6621 0 14.9207 0.0742605 15.1815 0.213706C15.4422 0.353152 15.6468 0.557783 15.7863 0.818523C15.9257 1.07926 16 1.33789 16 2.05111V13.9489C16 14.6621 15.9257 14.9207 15.7863 15.1815C15.6468 15.4422 15.4422 15.6468 15.1815 15.7863C14.9207 15.9257 14.6621 16 13.9489 16H2.05111C1.33789 16 1.07926 15.9257 0.818523 15.7863C0.557783 15.6468 0.353152 15.4422 0.213706 15.1815C0.0742605 14.9207 0 14.6621 0 13.9489V2.05111C0 1.33789 0.0742605 1.07926 0.213706 0.818523C0.353152 0.557783 0.557783 0.353152 0.818523 0.213706C1.07926 0.0742605 1.33789 0 2.05111 0ZM2.05111 0.8C1.52163 0.8 1.36038 0.831137 1.1958 0.919157C1.07448 0.984043 0.984043 1.07448 0.919157 1.1958C0.831137 1.36038 0.8 1.52163 0.8 2.05111V8H7.2V0.8H2.05111ZM8 0.8V7.2H15.2L15.2 2.05111C15.2 1.52163 15.1689 1.36038 15.0808 1.1958C15.016 1.07448 14.9255 0.984043 14.8042 0.919157C14.6396 0.831137 14.4784 0.8 13.9489 0.8H8ZM8 8V15.2L13.9489 15.2C14.4784 15.2 14.6396 15.1689 14.8042 15.0808C14.9255 15.016 15.016 14.9255 15.0808 14.8042C15.1689 14.6396 15.2 14.4784 15.2 13.9489L15.2 8H8ZM7.2 15.2V8.8H0.8V13.9489C0.8 14.4784 0.831137 14.6396 0.919157 14.8042C0.984043 14.9255 1.07448 15.016 1.1958 15.0808C1.36038 15.1689 1.52163 15.2 2.05111 15.2L7.2 15.2Z" fill="#1168A6"/></svg>',
				"gadget":'<svg width="15" height="15" viewBox="0 0 15 15" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M5 0.8H2C1.33726 0.8 0.8 1.33726 0.8 2V5C0.8 5.66274 1.33726 6.2 2 6.2H5C5.66274 6.2 6.2 5.66274 6.2 5V2C6.2 1.33726 5.66274 0.8 5 0.8ZM2 0C0.895431 0 0 0.89543 0 2V5C0 6.10457 0.89543 7 2 7H5C6.10457 7 7 6.10457 7 5V2C7 0.895431 6.10457 0 5 0H2ZM13 0.8H10C9.33726 0.8 8.8 1.33726 8.8 2V5C8.8 5.66274 9.33726 6.2 10 6.2H13C13.6627 6.2 14.2 5.66274 14.2 5V2C14.2 1.33726 13.6627 0.8 13 0.8ZM10 0C8.89543 0 8 0.89543 8 2V5C8 6.10457 8.89543 7 10 7H13C14.1046 7 15 6.10457 15 5V2C15 0.895431 14.1046 0 13 0H10ZM2 8.8H5C5.66274 8.8 6.2 9.33726 6.2 10V13C6.2 13.6627 5.66274 14.2 5 14.2H2C1.33726 14.2 0.8 13.6627 0.8 13V10C0.8 9.33726 1.33726 8.8 2 8.8ZM0 10C0 8.89543 0.895431 8 2 8H5C6.10457 8 7 8.89543 7 10V13C7 14.1046 6.10457 15 5 15H2C0.89543 15 0 14.1046 0 13V10ZM11 11V8H12V11H15V12H12V15H11V12H8V11H11Z" fill="#1B6EAA"/></svg>',
				"notebook": '<svg width="13" height="17" viewBox="0 0 13 17" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M1.79999 -1.2219e-05H10.8C11.7941 -1.2219e-05 12.6 0.805876 12.6 1.79999V14.8C12.6 15.7941 11.7941 16.6 10.8 16.6H1.79999C0.805875 16.6 -1.2219e-05 15.7941 -1.2219e-05 14.8V1.79999C-1.2219e-05 0.805875 0.805875 -1.2219e-05 1.79999 -1.2219e-05ZM1.79999 0.799988C1.2477 0.799988 0.799988 1.2477 0.799988 1.79999V14.8C0.799988 15.3523 1.2477 15.8 1.79999 15.8H10.8C11.3523 15.8 11.8 15.3523 11.8 14.8V1.79999C11.8 1.2477 11.3523 0.799988 10.8 0.799988H1.79999ZM2.79999 4.19999C2.79999 3.97907 2.97907 3.79999 3.19999 3.79999H6.39999C6.6209 3.79999 6.79999 3.97907 6.79999 4.19999C6.79999 4.4209 6.6209 4.59999 6.39999 4.59999H3.19999C2.97907 4.59999 2.79999 4.4209 2.79999 4.19999ZM3.19999 6.5C2.97907 6.5 2.79999 6.67909 2.79999 6.9C2.79999 7.12091 2.97907 7.3 3.19999 7.3H9.39999C9.6209 7.3 9.79999 7.12091 9.79999 6.9C9.79999 6.67909 9.6209 6.5 9.39999 6.5H3.19999ZM2.79999 9.60001C2.79999 9.3791 2.97907 9.20001 3.19999 9.20001H9.39999C9.6209 9.20001 9.79999 9.3791 9.79999 9.60001C9.79999 9.82093 9.6209 10 9.39999 10H3.19999C2.97907 10 2.79999 9.82093 2.79999 9.60001ZM3.19999 11.9C2.97907 11.9 2.79999 12.0791 2.79999 12.3C2.79999 12.5209 2.97907 12.7 3.19999 12.7H9.39999C9.6209 12.7 9.79999 12.5209 9.79999 12.3C9.79999 12.0791 9.6209 11.9 9.39999 11.9H3.19999Z" fill="#1168A6"/></svg>',
				"microservice": '<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M8.89696 0.899392C9.33309 1.13905 9.45541 1.64919 9.22828 2.02362C9.00114 2.39805 8.47582 2.5523 8.03969 2.31264C7.60356 2.07298 7.48124 1.56285 7.70837 1.18841C7.9355 0.813981 8.46083 0.659733 8.89696 0.899392ZM9.31187 0.215397C10.1188 0.658812 10.3953 1.64087 9.92939 2.40889C9.46351 3.17691 8.4317 3.44005 7.62478 2.99663C6.81785 2.55322 6.54138 1.57116 7.00726 0.803142C7.47313 0.0351247 8.50495 -0.228017 9.31187 0.215397ZM11.5714 2.83501C11.6841 2.64659 11.9277 2.57767 12.1127 2.69053C13.2399 3.37808 14.1581 4.33854 14.7713 5.47628C15.3774 6.60099 15.6618 7.85507 15.5998 9.11286C15.5884 9.34358 15.3764 9.51269 15.1443 9.4894C14.9167 9.46656 14.7554 9.267 14.7652 9.04073C14.8135 7.93329 14.5602 6.83023 14.0265 5.83991C13.49 4.84454 12.6912 4.00168 11.7112 3.39221C11.5219 3.27449 11.4553 3.02884 11.5714 2.83501ZM11.4836 15.2024C11.689 15.1021 11.7555 14.8573 11.6388 14.6674C11.522 14.4775 11.2661 14.4147 11.0599 14.5137C10.0169 15.0148 8.85281 15.2508 7.6812 15.1954C6.50972 15.14 5.37608 14.7953 4.39049 14.1985C4.19533 14.0803 3.93419 14.1184 3.79829 14.2963C3.66233 14.4743 3.70328 14.7247 3.89768 14.8442C5.01548 15.5313 6.30574 15.928 7.63965 15.9911C8.97393 16.0542 10.2995 15.781 11.4836 15.2024ZM14.7413 13.4375C14.3051 13.6772 13.7798 13.5229 13.5527 13.1485C13.3255 12.7741 13.4479 12.2639 13.884 12.0243C14.3201 11.7846 14.8454 11.9389 15.0726 12.3133C15.2997 12.6877 15.1774 13.1979 14.7413 13.4375ZM15.1562 14.1215C14.3492 14.5649 13.3174 14.3018 12.8516 13.5338C12.3857 12.7657 12.6621 11.7837 13.4691 11.3403C14.276 10.8969 15.3078 11.16 15.7737 11.928C16.2396 12.696 15.9631 13.6781 15.1562 14.1215ZM5.34808 3.05454L5.35759 3.04885C5.532 2.94135 5.60651 2.72148 5.51737 2.53076C5.42436 2.33178 5.18791 2.23927 4.99199 2.33416C3.79908 2.91197 2.7856 3.78091 2.0571 4.85498C1.33716 5.91642 0.923617 7.1368 0.854009 8.39358C0.841205 8.62477 1.03625 8.81439 1.2707 8.81311C1.49846 8.81187 1.67811 8.63048 1.69163 8.40597C1.75837 7.29819 2.12545 6.22317 2.76037 5.28709C3.39854 4.3462 4.28251 3.58222 5.32263 3.0682C5.33072 3.0642 5.33863 3.05998 5.34636 3.05554L5.34808 3.05454ZM0.8 11.9854C0.8 11.5772 1.15945 11.1797 1.6871 11.1797C2.21474 11.1797 2.57419 11.5772 2.57419 11.9854C2.57419 12.3937 2.21474 12.7912 1.6871 12.7912C1.15945 12.7912 0.8 12.3937 0.8 11.9854ZM0 11.9854C0 11.0986 0.755339 10.3797 1.6871 10.3797C2.61885 10.3797 3.37419 11.0986 3.37419 11.9854C3.37419 12.8723 2.61885 13.5912 1.6871 13.5912C0.755339 13.5912 0 12.8723 0 11.9854Z" fill="#1B6EAA"/></svg>',
				"flow":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M11 0.8H15C15.1105 0.8 15.2 0.889543 15.2 1V3C15.2 3.11046 15.1105 3.2 15 3.2H11C10.8895 3.2 10.8 3.11046 10.8 3V1C10.8 0.889543 10.8895 0.8 11 0.8ZM10 1C10 0.447715 10.4477 0 11 0H15C15.5523 0 16 0.447715 16 1V3C16 3.55228 15.5523 4 15 4H11C10.4477 4 10 3.55228 10 3V2.4H9C8.66863 2.4 8.4 2.66863 8.4 3V7.7H10V7C10 6.44772 10.4477 6 11 6H15C15.5523 6 16 6.44772 16 7V9C16 9.55228 15.5523 10 15 10H11C10.4477 10 10 9.55228 10 9V8.5H8.4V13C8.4 13.3314 8.66863 13.6 9 13.6H10V13C10 12.4477 10.4477 12 11 12H15C15.5523 12 16 12.4477 16 13V15C16 15.5523 15.5523 16 15 16H11C10.4477 16 10 15.5523 10 15V14.4H9C8.2268 14.4 7.6 13.7732 7.6 13V8.5H6V10C6 10.5523 5.55228 11 5 11H1C0.447715 11 0 10.5523 0 10V6C0 5.44772 0.447715 5 1 5H5C5.55228 5 6 5.44772 6 6V7.7H7.6V3C7.6 2.2268 8.2268 1.6 9 1.6H10V1ZM1 5.8H5C5.11046 5.8 5.2 5.88954 5.2 6V10C5.2 10.1105 5.11046 10.2 5 10.2H1C0.889543 10.2 0.8 10.1105 0.8 10V6C0.8 5.88954 0.889543 5.8 1 5.8ZM15 6.8H11C10.8895 6.8 10.8 6.88954 10.8 7V9C10.8 9.11046 10.8895 9.2 11 9.2H15C15.1105 9.2 15.2 9.11046 15.2 9V7C15.2 6.88954 15.1105 6.8 15 6.8ZM11 12.8H15C15.1105 12.8 15.2 12.8895 15.2 13V15C15.2 15.1105 15.1105 15.2 15 15.2H11C10.8895 15.2 10.8 15.1105 10.8 15V13C10.8 12.8895 10.8895 12.8 11 12.8Z" fill="#1B6EAA"/></svg>'
			};
			
			svg += '<svg xmlns="http://www.w3.org/2000/svg" width="'+ calculateTextWidth(title,font) +'" height="64">'+
				'<rect x="0" y="0" width="100%" rx="4" ry="4" height="100%" fill="#FFFFFF"  stroke="#3982D0" style="stroke-width:2" ></rect>'+
				'<foreignObject x="16" y="16" width="100%" height="100%">'+
					'<div xmlns="http://www.w3.org/1999/xhtml">'+
						groupIcon[group] +
						'<span style="position: absolute; font-family: Soho,Arial; font-size:12px; color:#1B6EAA; top:2px;  left: 22px;font-weight: bolder">'+ title +'</span>'+
						'<span style="position: absolute; font-family: Soho,Arial; font-size:10px; color:#000; top:20px; left: 0px; ">'+ group +'</span>'+
					'</div>'+
			    '</foreignObject>'+
			'</svg>';			
			
			return svg;
		}
		
		
		var graphDataObj = {
			nodes : [],
			edges : []
		}, ArrayNodes = [], ArrayEdges = [], idRef = {}, target = '', nodes = {}, avoidDuplicates = [];

		if ($("#id_panel_botones")) {
			$("#id_panel_botones").hide();
		}

		// handle data
		if (!$.isEmptyObject(links)) {

			if ($("#id_panel_botones")) {
				$("#id_panel_botones").show();
			}
			// main node Loop - create nodes with info.
			console.log('DATA LINKS: ' + JSON.stringify(links));
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
							'nameSource' : Node.nameSource,
							'group' : Node.classTarget,
							'title' : Node.title,
							'isExternal' : Node.isExternal,
							'properties' : Node.properties
						};

					group = dataJson.group.toLowerCase();
					type = dataJson.type.toLowerCase();

					if (options.groups[group] !== undefined) {
						dataJson.group = group;
						
						// ADDING IMAGE GROUP.						
							var font = "12px Soho"; 
							// create svg
							var svg = createSvg(dataJson.title,font,dataJson.group);							
							// encode as image
							var imageUrl  = "data:image/svg+xml;charset=utf-8," + encodeURIComponent(svg);							
							// apply shape and imagen to node
							dataJson.shape = 'image';
							dataJson.label = ''; // label inside, no need
							dataJson.image = imageUrl;				
						
					} 
					else if (options.groups[type] !== undefined) {
						dataJson.group = type;
					} 
					else {
						dataJson.group = 'licensing';
					}

					dataJson.category = Node.source;
				//	if (Node.type == 'suit') {
						dataJson.category = Node.target;
						ArraySource.push({
							'id' : Index,
							'target' : Node.target,
							'group' : dataJson.group
						});
				//	}
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
	//		$("#networkVis").show();
		}
		// RETURNING GRAPH DATA FORMATTED
		return graphDataObj;
	}

	// LOAD GRAPH DATA FROM SERVER
	var loadNetwork = function(type, resource) {
		logControl ? console.log('loadNetwork() -> ') : '';

		destroyNetwork();
		var url = "/controlpanel/lineage/getgraph?type=" + type + "&identification=" + resource;

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

				if(graphData!=null && typeof graphData.nodes!='undefined' && graphData.nodes.length > 100){
					options.physics.stabilization = false;
					delete options.physics.minVelocity;
					delete options.physics.maxVelocity;
				}else{
					options.physics.stabilization = true;
					options.physics.minVelocity = 0.3;
					options.physics.maxVelocity = 75;
				}
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
					if(graphData!=null && typeof graphData.nodes!='undefined' && graphData.nodes.length > 100){
						clusterGraphBySource();
					}
				}
			//	hierarchicalNetwork("RL");
				$("#graphInclude").show();
				// UNBLOCK GRAPH
				App.unblockUI("#graphporlet");
			},
			error : function(data, status, er) {
				$.alert({
					title : 'GRAPH ERROR!',
					theme : 'dark',
					icon : 'fa fa-warning',
					type : 'red',
					content : 'Error loading graph info on graph Controller.'
				});
				// UNBLOCK IF ERROR
				App.unblockUI("#graphporlet");
			}
		});
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
		//LOAD GRAPH LINEAGE
		loadLineage : function(identification, group){
			$('#table-info-container').hide()
			$('#relationBtns').hide();
			
			resourceSelected = identification;
			typeSelected = group;
			
			handleGraphHeight();
			
			handleHierarchical();
			
			handleCluster();
			
			toggleGraphInfoTable();
			
			createRelationsInfoTable();

			// BLOCK UI while loading...
			App.blockUI({target:"#graphporlet",boxed:true,type:"loader",message:"Loading Lineage..."})

			loadNetwork(group, identification);

		},
		// INIT() CONTROLLER INIT CALLS
		init : function() {
			
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			
			var oTable = $('#resources-table').dataTable({
			      "bAutoWidth": false
			      }); 
			
			// SHOW/HIDE DATATABLES COLUMN INIT 	
			//TABLE STYLES
			$('#divcreate').prepend($('#dataTable-vis-toggle'));		
			$('#dataTable-vis-toggle').removeClass('hide');
			$('.dataTables_info').addClass('col-md-6 col-sm-6');
			$('.dataTables_length').addClass('col-md-6 col-sm-6');
			$('#resources-table_wrapper > div:nth-child(3)').addClass('table-scrollable table-scrollable-borderless');
			$('#resources-table_wrapper > div:nth-child(3) > div.col-md-5.col-sm-5').append($('.dataTables_length'));

			$('#search-on-title').append($('.dataTables_filter > label > input'));
			$('#search-on-title > input').removeClass('input-xsmall')
			$('#resources-table_wrapper > div:nth-child(1)').hide();

			$('.dataTables_paginate').attr('style', 'float:right !important');
			$('.download-menu').parent().on('shown.bs.dropdown', function () {	        
				 var $menu = $("ul", this);	        
		         offset = $menu.offset();
		         position = $menu.position();
		         $('body').append($menu);
		         $menu.show();
		         $menu.css('position', 'absolute');
		         $menu.css('top', (offset.top) +'px');
		         $menu.css('left',(offset.left) +'px');
		         $menu.css('min-width', '100px');
		        $(this).data("myDropdownMenu", $menu);
		     });
		     $('.download-menu').parent().on('hide.bs.dropdown', function () {
		         $(this).append($(this).data("myDropdownMenu"));
		         $(this).data("myDropdownMenu").removeAttr('style');
		     });
			
			$("#contenedor-tabla-outside").removeClass("hidden");
			
			$("#type-model").on("change", function(){
				var type = $("#type-model").val();
				if(type == "ONTOLOGY"){
					$("#div-ontology-relation").show();
					$("#div-dashboard-relation ").hide();
					$("#div-pipeline-relation").hide();
					$("#div-api-relation").hide();
					$("#div-notebook-relation").hide();
					$("#div-digitalclient-relation").hide();
					$("#div-gadget-relation").hide();
					$("#div-gadgetdatasource-relation").hide();
					$("#div-microservice-relation").hide();
				}else if(type == "DASHBOARD"){
					$("#div-ontology-relation").hide();
					$("#div-dashboard-relation").show();
					$("#div-pipeline-relation").hide();
					$("#div-api-relation").hide();
					$("#div-notebook-relation").hide();
					$("#div-digitalclient-relation").hide();
					$("#div-gadget-relation").hide();
					$("#div-gadgetdatasource-relation").hide();
					$("#div-microservice-relation").hide();
				}else if(type == "API"){
					$("#div-ontology-relation").hide();
					$("#div-dashboard-relation").hide();
					$("#div-pipeline-relation").hide();
					$("#div-api-relation").show();
					$("#div-notebook-relation").hide();
					$("#div-digitalclient-relation").hide();
					$("#div-gadget-relation").hide();
					$("#div-gadgetdatasource-relation").hide();
					$("#div-microservice-relation").hide();
				}else if(type == "DATAFLOW"){
					$("#div-ontology-relation").hide();
					$("#div-dashboard-relation").hide();
					$("#div-pipeline-relation").show();
					$("#div-api-relation").hide();
					$("#div-notebook-relation").hide();
					$("#div-digitalclient-relation").hide();
					$("#div-gadget-relation").hide();
					$("#div-gadgetdatasource-relation").hide();
					$("#div-microservice-relation").hide();
				}else if(type == "NOTEBOOK"){
					$("#div-ontology-relation").hide();
					$("#div-dashboard-relation").hide();
					$("#div-pipeline-relation").hide();
					$("#div-api-relation").hide();
					$("#div-notebook-relation").show();
					$("#div-digitalclient-relation").hide();
					$("#div-microservice-relation").hide();
				}else if(type == "DIGITALCLIENT"){
					$("#div-ontology-relation").hide();
					$("#div-dashboard-relation").hide();
					$("#div-pipeline-relation").hide();
					$("#div-api-relation").hide();
					$("#div-notebook-relation").hide();
					$("#div-digitalclient-relation").show();
					$("#div-gadget-relation").hide();
					$("#div-gadgetdatasource-relation").hide();
					$("#div-microservice-relation").hide();
				}else if(type == "GADGET"){
					$("#div-ontology-relation").hide();
					$("#div-dashboard-relation").hide();
					$("#div-pipeline-relation").hide();
					$("#div-api-relation").hide();
					$("#div-notebook-relation").hide();
					$("#div-digitalclient-relation").hide();
					$("#div-gadget-relation").show();
					$("#div-gadgetdatasource-relation").hide();
					$("#div-microservice-relation").hide();
				}else if(type == "DATASOURCE"){
					$("#div-ontology-relation").hide();
					$("#div-dashboard-relation").hide();
					$("#div-pipeline-relation").hide();
					$("#div-api-relation").hide();
					$("#div-notebook-relation").hide();
					$("#div-digitalclient-relation").hide();
					$("#div-gadget-relation").hide();
					$("#div-gadgetdatasource-relation").show();
					$("#div-microservice-relation").hide();
				}else if(type == "MICROSERVICE"){
					$("#div-ontology-relation").hide();
					$("#div-dashboard-relation").hide();
					$("#div-pipeline-relation").hide();
					$("#div-api-relation").hide();
					$("#div-notebook-relation").hide();
					$("#div-digitalclient-relation").hide();
					$("#div-gadget-relation").hide();
					$("#div-gadgetdatasource-relation").hide();
					$("#div-microservice-relation").show();
				}
			});
			if($("#loadExample").val() == "true"){
				//init example
				GraphLineageController.loadLineage("airportsdata","ONTOLOGY");
			}
		},
		showModalOrigin : function(resource){
			$('.modal-title').text(graphJson.addOriginFor + ' ' + resource);
			$('#typeRelation').val('origin');
			$("#resource").val(resource);
			$('#modal-add-relation').modal('show');
		},
		showModalTarget : function(resource){
			$('.modal-title').text(graphJson.addTargetFor + ' ' + resource);
			$('#typeRelation').val('target');
			$("#resource").val(resource);
			$('#modal-add-relation').modal('show');
		},
		addRelation : function(){
			var origin = '';
			var target= '';
			var originType= '';
			var targetType= '';
			
			if($('#typeRelation').val() == 'origin'){
				originType = $("#type-model").val();
				origin = $("#resource-relation").val();
				target = $("#resource").val();
				targetType = $("#selectedTypeEntity").val();
			}else if($('#typeRelation').val() == 'target'){
				targetType = $("#type-model").val();
				target = $("#resource-relation").val();
				origin = $("#resource").val();
				originType = $("#selectedTypeEntity").val();
			}
			
			// AJAX CALL - get GRAPH data
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			$.ajax({
				url : "/controlpanel/lineage/addRelation",
				headers: {
					[csrf_header]: csrf_value
			    },
				type : 'POST',
				data: {
					'source' : origin,
					'target' : target,
					'sourceType': originType,
					'targetType': targetType
				},
				success : function(data) {
					
					$("#type-model").val('');
					$("#ontology-relation").val('');
					$("#dashboard-relation").val('');
					$("#pipeline-relation").val('');
					$("#api-relation").val('');
					$("#notebook-relation").val('');
					$("#digitalclient-relation").val('');
					$("#gadget-relation").val('');
					$("#gadgetdatasource-relation").val('');
					$("#div-ontology-relation").hide();
					$("#div-dashboard-relation").hide();
					$("#div-pipeline-relation").hide();
					$("#div-api-relation").hide();
					$("#div-notebook-relation").hide();
					$("#div-digitalclient-relation").hide();
					$("#div-gadget-relation").hide();
					$("#div-gadgetdatasource-relation").hide();
					
					$('#modal-add-relation').modal('hide');
					
					GraphLineageController.loadLineage(resourceSelected, typeSelected);
				},
				error : function(data, status, er) {
					$.alert({
						title : 'ERROR!',
						theme : 'dark',
						icon : 'fa fa-warning',
						type : 'red',
						content : 'Error creating relation.'
					});
				}
			});

		},
		setResourceRelation : function(resource){
			$("#resource-relation").val(resource.value);
		},
		showModalDeleteRelation : function(node, nodeType){
			// i18 labels
			var Close = headerReg.btnCancelar;
			var Remove = headerReg.btnEliminar;
			var Content = graphReg.deleteRelationInfo;
			var Title = headerReg.titleConfirm + ':';

			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
			
			// jquery-confirm DIALOG SYSTEM.
			$.confirm({
				icon : 'fa fa-warning',
				title : Title,
				theme : 'light',
				columnClass : 'medium',
				content : Content,
				draggable : true,
				dragWindowGap : 100,
				backgroundDismiss : true,
				closeIcon : true,
				buttons : {
					close : {
						text : Close,
						btnClass : 'btn btn-sm btn-circle blue btn-outline',
						action : function() {
						} // GENERIC CLOSE.
					},
					remove : {
						text : Remove,
						btnClass : 'btn btn-sm btn-circle btn-primary btn-outline',
						action : function() {
							// AJAX CALL -  Delte Relations of node selected
							var csrf_value = $("meta[name='_csrf']").attr("content");
							var csrf_header = $("meta[name='_csrf_header']").attr("content");
							$.ajax({
								url : "/controlpanel/lineage/deleteRelation",
								headers: {
									[csrf_header]: csrf_value
							    },
								type : 'GET',
								data: {
									'node' : node,
									'nodeType': nodeType,
								},
								success : function(data) {
									
									$('#modal-add-relation').modal('hide');
									GraphLineageController.loadLineage(resourceSelected, typeSelected);
								},
								error : function(data, status, er) {
									$.alert({
										title : 'ERROR!',
										theme : 'dark',
										icon : 'fa fa-warning',
										type : 'red',
										content : 'Error deleting relation.'
									});
								}
							});
						}
					}	
				}
			});
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)	
	GraphLineageController.load(graphJson);	
	// AUTO INIT CONTROLLER.
	GraphLineageController.init();
});
