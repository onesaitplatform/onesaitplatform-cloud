var GraphProcessController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var APPNAME = 'Onesait Platform Control Panel', LIB_TITLE = 'Graph Process Controller', logControl = 0;

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
			insert:{
				shape: 'image',				
				
			},
			update:{
				shape: 'image',
				
			},
			delete:{
				shape: 'image',
				
			}
		}
	};

	// CONTROLLER PRIVATE FUNCTIONS

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
		var createSvg = function(title,font,group,status){
			
			var svg = '';
			
			// icons for groups
			var groupIcon = {
				"insert": '<svg width="16" height="16" viewBox="-1 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M10.2511 1.31952C10.2515 1.31956 10.2493 1.32266 10.2434 1.32868C10.2477 1.32249 10.2507 1.31948 10.2511 1.31952ZM9.17859 1.21115C9.52626 1.30597 9.79082 1.40633 9.97738 1.5C9.79082 1.59367 9.52626 1.69403 9.17859 1.78885C8.26957 2.03676 6.96869 2.2 5.5 2.2C4.03131 2.2 2.73043 2.03676 1.82141 1.78885C1.47374 1.69403 1.20917 1.59367 1.02261 1.5C1.20917 1.40633 1.47374 1.30597 1.82141 1.21115C2.73043 0.963236 4.03131 0.8 5.5 0.8C6.96869 0.8 8.26957 0.963236 9.17859 1.21115ZM0.748928 1.31952C0.749329 1.31948 0.75228 1.32249 0.756579 1.32868C0.750677 1.32266 0.748528 1.31956 0.748928 1.31952ZM10.1 2.32257C9.11706 2.7306 7.42389 3 5.5 3C3.57611 3 1.88294 2.7306 0.9 2.32257V4.93112C0.939871 4.97252 1.01417 5.03217 1.13207 5.10499C1.36574 5.24932 1.7127 5.41015 2.13453 5.5608C2.97884 5.86234 4.05746 6.1 5 6.1C6.37625 6.1 7.71177 5.85699 8.69745 5.55504C9.19163 5.40366 9.58417 5.24162 9.84485 5.09607C9.97374 5.0241 10.0554 4.96439 10.1 4.9224V2.32257ZM10.1 5.86628C9.78853 6.02442 9.38876 6.17996 8.93177 6.31996C7.87719 6.64301 6.4627 6.9 5 6.9C3.94254 6.9 2.77116 6.63766 1.86547 6.3142C1.50184 6.18433 1.17049 6.04089 0.9 5.89432V8.93112C0.939871 8.97253 1.01417 9.03217 1.13207 9.10499C1.36574 9.24932 1.7127 9.41015 2.13453 9.5608C2.97884 9.86234 4.05746 10.1 5 10.1C6.71405 10.1 8.04548 9.85487 8.93601 9.55803C9.38326 9.40894 9.70621 9.25116 9.90847 9.11415C10.0102 9.0452 10.0687 8.99004 10.0976 8.95471L10.1 8.9518V5.86628ZM0.1 5V9V12.5C0.1 12.7498 0.271877 12.9237 0.369411 13.009C0.489093 13.1138 0.64512 13.2108 0.814066 13.2995C1.15568 13.4788 1.61797 13.6591 2.13069 13.8193C3.15406 14.1391 4.44618 14.4 5.5 14.4C7.21106 14.4 8.5104 14.1447 9.396 13.8119C9.83712 13.6462 10.1884 13.4563 10.4378 13.2589C10.5623 13.1603 10.6713 13.0521 10.752 12.9347C10.8309 12.82 10.9 12.6711 10.9 12.5V9V5V1.78621C10.9656 1.69356 11 1.59787 11 1.5C11 0.671573 8.53757 0 5.5 0C2.46243 0 0 0.671573 0 1.5C0 1.59787 0.0343708 1.69356 0.1 1.7862V5ZM0.9 12.4103V9.89432C1.17049 10.0409 1.50184 10.1843 1.86547 10.3142C2.77116 10.6377 3.94254 10.9 5 10.9C6.78595 10.9 8.20452 10.6451 9.18899 10.317C9.54489 10.1983 9.85101 10.0678 10.1 9.93208V12.4704C10.0982 12.4734 10.0958 12.477 10.0929 12.4813C10.0702 12.5143 10.0239 12.5662 9.9412 12.6317C9.77608 12.7624 9.50503 12.9163 9.11458 13.0631C8.33705 13.3553 7.13639 13.6 5.5 13.6C4.55382 13.6 3.34594 13.3609 2.36931 13.0557C1.88203 12.9034 1.46932 12.7399 1.18593 12.5912C1.04579 12.5176 0.952895 12.4556 0.9 12.4103Z" fill="#1B6EAA"/></svg>',
				"update": '<svg width="16" height="16" viewBox="-1 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M10.2511 1.31952C10.2515 1.31956 10.2493 1.32266 10.2434 1.32868C10.2477 1.32249 10.2507 1.31948 10.2511 1.31952ZM9.17859 1.21115C9.52626 1.30597 9.79082 1.40633 9.97738 1.5C9.79082 1.59367 9.52626 1.69403 9.17859 1.78885C8.26957 2.03676 6.96869 2.2 5.5 2.2C4.03131 2.2 2.73043 2.03676 1.82141 1.78885C1.47374 1.69403 1.20917 1.59367 1.02261 1.5C1.20917 1.40633 1.47374 1.30597 1.82141 1.21115C2.73043 0.963236 4.03131 0.8 5.5 0.8C6.96869 0.8 8.26957 0.963236 9.17859 1.21115ZM0.748928 1.31952C0.749329 1.31948 0.75228 1.32249 0.756579 1.32868C0.750677 1.32266 0.748528 1.31956 0.748928 1.31952ZM10.1 2.32257C9.11706 2.7306 7.42389 3 5.5 3C3.57611 3 1.88294 2.7306 0.9 2.32257V4.93112C0.939871 4.97252 1.01417 5.03217 1.13207 5.10499C1.36574 5.24932 1.7127 5.41015 2.13453 5.5608C2.97884 5.86234 4.05746 6.1 5 6.1C6.37625 6.1 7.71177 5.85699 8.69745 5.55504C9.19163 5.40366 9.58417 5.24162 9.84485 5.09607C9.97374 5.0241 10.0554 4.96439 10.1 4.9224V2.32257ZM10.1 5.86628C9.78853 6.02442 9.38876 6.17996 8.93177 6.31996C7.87719 6.64301 6.4627 6.9 5 6.9C3.94254 6.9 2.77116 6.63766 1.86547 6.3142C1.50184 6.18433 1.17049 6.04089 0.9 5.89432V8.93112C0.939871 8.97253 1.01417 9.03217 1.13207 9.10499C1.36574 9.24932 1.7127 9.41015 2.13453 9.5608C2.97884 9.86234 4.05746 10.1 5 10.1C6.71405 10.1 8.04548 9.85487 8.93601 9.55803C9.38326 9.40894 9.70621 9.25116 9.90847 9.11415C10.0102 9.0452 10.0687 8.99004 10.0976 8.95471L10.1 8.9518V5.86628ZM0.1 5V9V12.5C0.1 12.7498 0.271877 12.9237 0.369411 13.009C0.489093 13.1138 0.64512 13.2108 0.814066 13.2995C1.15568 13.4788 1.61797 13.6591 2.13069 13.8193C3.15406 14.1391 4.44618 14.4 5.5 14.4C7.21106 14.4 8.5104 14.1447 9.396 13.8119C9.83712 13.6462 10.1884 13.4563 10.4378 13.2589C10.5623 13.1603 10.6713 13.0521 10.752 12.9347C10.8309 12.82 10.9 12.6711 10.9 12.5V9V5V1.78621C10.9656 1.69356 11 1.59787 11 1.5C11 0.671573 8.53757 0 5.5 0C2.46243 0 0 0.671573 0 1.5C0 1.59787 0.0343708 1.69356 0.1 1.7862V5ZM0.9 12.4103V9.89432C1.17049 10.0409 1.50184 10.1843 1.86547 10.3142C2.77116 10.6377 3.94254 10.9 5 10.9C6.78595 10.9 8.20452 10.6451 9.18899 10.317C9.54489 10.1983 9.85101 10.0678 10.1 9.93208V12.4704C10.0982 12.4734 10.0958 12.477 10.0929 12.4813C10.0702 12.5143 10.0239 12.5662 9.9412 12.6317C9.77608 12.7624 9.50503 12.9163 9.11458 13.0631C8.33705 13.3553 7.13639 13.6 5.5 13.6C4.55382 13.6 3.34594 13.3609 2.36931 13.0557C1.88203 12.9034 1.46932 12.7399 1.18593 12.5912C1.04579 12.5176 0.952895 12.4556 0.9 12.4103Z" fill="#1B6EAA"/></svg>',
				"delete": '<svg width="16" height="16" viewBox="-1 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M10.2511 1.31952C10.2515 1.31956 10.2493 1.32266 10.2434 1.32868C10.2477 1.32249 10.2507 1.31948 10.2511 1.31952ZM9.17859 1.21115C9.52626 1.30597 9.79082 1.40633 9.97738 1.5C9.79082 1.59367 9.52626 1.69403 9.17859 1.78885C8.26957 2.03676 6.96869 2.2 5.5 2.2C4.03131 2.2 2.73043 2.03676 1.82141 1.78885C1.47374 1.69403 1.20917 1.59367 1.02261 1.5C1.20917 1.40633 1.47374 1.30597 1.82141 1.21115C2.73043 0.963236 4.03131 0.8 5.5 0.8C6.96869 0.8 8.26957 0.963236 9.17859 1.21115ZM0.748928 1.31952C0.749329 1.31948 0.75228 1.32249 0.756579 1.32868C0.750677 1.32266 0.748528 1.31956 0.748928 1.31952ZM10.1 2.32257C9.11706 2.7306 7.42389 3 5.5 3C3.57611 3 1.88294 2.7306 0.9 2.32257V4.93112C0.939871 4.97252 1.01417 5.03217 1.13207 5.10499C1.36574 5.24932 1.7127 5.41015 2.13453 5.5608C2.97884 5.86234 4.05746 6.1 5 6.1C6.37625 6.1 7.71177 5.85699 8.69745 5.55504C9.19163 5.40366 9.58417 5.24162 9.84485 5.09607C9.97374 5.0241 10.0554 4.96439 10.1 4.9224V2.32257ZM10.1 5.86628C9.78853 6.02442 9.38876 6.17996 8.93177 6.31996C7.87719 6.64301 6.4627 6.9 5 6.9C3.94254 6.9 2.77116 6.63766 1.86547 6.3142C1.50184 6.18433 1.17049 6.04089 0.9 5.89432V8.93112C0.939871 8.97253 1.01417 9.03217 1.13207 9.10499C1.36574 9.24932 1.7127 9.41015 2.13453 9.5608C2.97884 9.86234 4.05746 10.1 5 10.1C6.71405 10.1 8.04548 9.85487 8.93601 9.55803C9.38326 9.40894 9.70621 9.25116 9.90847 9.11415C10.0102 9.0452 10.0687 8.99004 10.0976 8.95471L10.1 8.9518V5.86628ZM0.1 5V9V12.5C0.1 12.7498 0.271877 12.9237 0.369411 13.009C0.489093 13.1138 0.64512 13.2108 0.814066 13.2995C1.15568 13.4788 1.61797 13.6591 2.13069 13.8193C3.15406 14.1391 4.44618 14.4 5.5 14.4C7.21106 14.4 8.5104 14.1447 9.396 13.8119C9.83712 13.6462 10.1884 13.4563 10.4378 13.2589C10.5623 13.1603 10.6713 13.0521 10.752 12.9347C10.8309 12.82 10.9 12.6711 10.9 12.5V9V5V1.78621C10.9656 1.69356 11 1.59787 11 1.5C11 0.671573 8.53757 0 5.5 0C2.46243 0 0 0.671573 0 1.5C0 1.59787 0.0343708 1.69356 0.1 1.7862V5ZM0.9 12.4103V9.89432C1.17049 10.0409 1.50184 10.1843 1.86547 10.3142C2.77116 10.6377 3.94254 10.9 5 10.9C6.78595 10.9 8.20452 10.6451 9.18899 10.317C9.54489 10.1983 9.85101 10.0678 10.1 9.93208V12.4704C10.0982 12.4734 10.0958 12.477 10.0929 12.4813C10.0702 12.5143 10.0239 12.5662 9.9412 12.6317C9.77608 12.7624 9.50503 12.9163 9.11458 13.0631C8.33705 13.3553 7.13639 13.6 5.5 13.6C4.55382 13.6 3.34594 13.3609 2.36931 13.0557C1.88203 12.9034 1.46932 12.7399 1.18593 12.5912C1.04579 12.5176 0.952895 12.4556 0.9 12.4103Z" fill="#1B6EAA"/></svg>'
			};
			var color;
			if(status==undefined){
				color="#FFFFFF";
			}else if(status=="Failed"){
				color="#f29696";
			}else if(status=="Success"){
				color="#61b051";
			}
			svg += '<svg xmlns="http://www.w3.org/2000/svg" width="'+ calculateTextWidth(title,font) +'" height="64">'+
				'<rect x="0" y="0" width="100%" rx="4" ry="4" height="100%" fill="#FFFFFF"  stroke="'+color+'" style="stroke-width:2" ></rect>'+
				'<foreignObject x="16" y="16" width="100%" height="100%">'+
					'<div xmlns="http://www.w3.org/1999/xhtml">'+
						groupIcon[group] +
						'<span style="position: absolute; font-family: Soho,Arial; font-size:12px; color:'+color+'; top:2px;  left: 22px;font-weight: bolder">'+ group +'</span>'+
						'<span style="position: absolute; font-family: Soho,Arial; font-size:10px; color:#000; top:20px; left: 0px; ">'+ title +'</span>'+
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
							'group' : Node.title,
							'title' : Node.classTarget,
							'properties' : Node.properties
						};

					group = dataJson.group.toLowerCase();
					type = dataJson.type.toLowerCase();

					if (options.groups[group] !== undefined) {
						dataJson.group = group;
						
						// ADDING IMAGE GROUP.						
							var font = "12px Soho"; 
							// create svg
							var svg = createSvg(dataJson.title,font,dataJson.group, dataJson.properties.LastStatus);							
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
	var loadNetwork = function(resource) {
		logControl ? console.log('loadNetwork() -> ') : '';

		destroyNetwork();
		var url = "/controlpanel/process/getgraph?identification=" + resource;

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
		loadProcess : function(identification){
			$('#table-info-container').hide()
			
			resourceSelected = identification;
			
			handleGraphHeight();
			
			handleHierarchical();
			
			handleCluster();
			
			toggleGraphInfoTable();
			// BLOCK UI while loading...
			App.blockUI({target:"#graphporlet",boxed:true,type:"loader",message:"Loading Process Graph..."})

			loadNetwork(identification);

		},
		formatDate:function (date) {
			    var d = new Date(date),
		        month = '' + (d.getMonth() + 1),
		        day = '' + d.getDate(),
		        year = d.getFullYear(),
		        hours = '' + d.getHours(),
	            minutes = '' + d.getMinutes(),
	            seconds = '' + d.getSeconds();
			    
	
		    if (month.length < 2) 
		        month = '0' + month;
		    if (day.length < 2) 
		        day = '0' + day;
		    if (hours.length < 2) 
		    	hours = '0' + hours;
		    if (minutes.length < 2) 
		    	minutes = '0' + minutes;
		    if (seconds.length < 2) 
		    	seconds = '0' + seconds;
	
		    return [year, month, day].join('/') + ' - ' + [hours, minutes, seconds].join(':');
		},
		// INIT() CONTROLLER INIT CALLS
		init : function() {
			
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			 
			// SHOW/HIDE DATATABLES COLUMN INIT 	
			//TABLE STYLES
			$('#divcreate').prepend($('#dataTable-vis-toggle'));		
			$('#dataTable-vis-toggle').removeClass('hide');
			$('.dataTables_info').addClass('col-md-6 col-sm-6');
			$('.dataTables_length').addClass('col-md-6 col-sm-6');
			$('#historic-table_wrapper > div:nth-child(3)').addClass('table-scrollable table-scrollable-borderless');
			$('#historic-table_wrapper > div:nth-child(3) > div.col-md-5.col-sm-5').append($('.dataTables_length'));

			$('#search-on-title').append($('.dataTables_filter > label > input'));
			$('#search-on-title > input').removeClass('input-xsmall')
			$('#historic-table_wrapper > div:nth-child(1)').hide();

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
			var opIds = [];
			$("#historic-table").empty();
			
			GraphProcessController.loadProcess(processIdentification);
			var historicTable =  `<thead><tr class="cabecera-tabla">`
				historicTable += `<th class="titulo-columnas text-left" id="table-date" th:text="#{process.execution.date}" >Execution Date</th>
								  <th class="titulo-columnas text-left"> <span th:text="#{process.execution.status}" >Status</span></th>`;
			operations.forEach(function(operation) {
				historicTable += `<th class="titulo-columnas text-left">` + operation.type + ` - ` + operation.ontology + `</th>`;
				opIds.push(operation.id);
			});
			historicTable +=`</tr></thead><tbody th:remove="all-but-first">`;
			for(let i = 0; i < historics.length; i++){
			
				var sDate = GraphProcessController.formatDate(historics[i].date);
				historicTable += `<tr pages:paginate="10">`;
				historicTable += `<td class="text-left"><span>`+sDate+`</span></td>`;
				historicTable += `<td class="text-left">`+historics[i].status+`</td>`;
				opIds.forEach(function(id) {
					historics[i].operationsStatus.forEach(function(opStatus) {
						if(opStatus.operationId == id && opStatus.isOk){
							historicTable += `<td class="text-left"><span th:text="1" style="display:none" ></span><i class="badge badge-empty-success"> </i><spanstyle="color: #2E6B33;"> Success</span></td>`;
						}else if(opStatus.operationId == id && !opStatus.isOk){
							historicTable += `<td class="text-left"><span th:text="0" style="display:none" ></span><i class="badge badge-empty-error"></i><spanstyle="color: #A73535;"> Failed</span><i class="la la-info-circle popovers" data-trigger="hover" data-placement="top" data-container="body" data-content="`+opStatus.message.replaceAll("\"","'")+`" data-title="Error"></i></td>`;
						}
					});
				});
				historicTable += `</tr>`;
			};
			historicTable += `</tbody>`;
			$("#historic-table").empty();
			$("#historic-table").html(historicTable);
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)	
	GraphProcessController.load(graphJson);	
	// AUTO INIT CONTROLLER.
	GraphProcessController.init();
});
