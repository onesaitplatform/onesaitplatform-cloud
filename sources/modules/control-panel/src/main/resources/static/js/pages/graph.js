var GraphController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var APPNAME = 'Onesait Platform Control Panel', LIB_TITLE = 'Graph Controller', logControl = 0;

	// GRAPH INITIALIZATION VARS AND CONST.
	var links = [], nodes = [], data = [], edges = [], ArraySource = [], jsonProjects = {}, options = {}, jsonIndex = {}, graphData = {
		nodes : [],
		edges : []
	}, containerNetwork = new Object(), network = null, graphHeight = 600;

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
		layout: {
			improvedLayout: false
		},
		/*physics : {
			barnesHut : {
				gravitationalConstant: -8000,
				centralGravity: 0,
				springLength: 150,
				springConstant: 0.1,
				damping: 1,
				avoidOverlap: 1
			},
			stabilization: true,
			minVelocity : 0.5,
			maxVelocity : 50
		},*/
		physics: {
			barnesHut: {
				gravitationalConstant: -25000,
				centralGravity: 2,
				springLength: 125,
				springConstant: 0.03,
				damping: 1,
				avoidOverlap: 1
			},
			maxVelocity: 10,
			minVelocity: 1
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
			  face: 'Arial',
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
			}
			
		},
		edges: {
			smooth: {
				type: "cubicBezier",
				forceDirection:"horizontal",
				roundness: .75,
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
			  to: -4
			},
			hoverWidth: 0,				
			labelHighlightBold: true,
		},
		autoResize : false,
		width : '100%',
		height : graphHeight + 'px',
		groups:{
			resolved :{
				shape: 'image'
			},
			apis:{
				shape: 'image'
			},
			api:{
				shape: 'image'
				
			},
			flows:{
				shape: 'image'
			},
			flow:{
				shape: 'image'				
			},
			webprojects:{
				shape: 'image'
			},
			webproject:{
				shape: 'image'
				
			},
			digitaltwins:{
				shape: 'image'			
				
			},
			digitaltwin:{
				shape: 'image'
			},
			licensing:{
				shape: 'image'
			},
			user:{
				shape: 'image'				
			},
			users:{
				shape: 'image'
			},
			
			visualizations:{
				shape: 'image'
			},
			
			analytics:{
				shape: 'image'
			},
			clientplatform:{
				shape: 'image'
			},		
			deviceandsystems:{
				shape: 'image'
			},			
			notebooks:{
				shape: 'image'
			},
			notebook:{
				shape: 'image'
			},
			dashboards:{
				shape: 'image'				
			},
			script:{
				shape: 'image'	
			},	
			scripts:{
				shape: 'image'	
			},			
			pipelines:{
				shape: 'image'
			},
			pipeline:{
				shape: 'image'
			},
			project:{
				shape: 'image'
			},
			gadget:{
				shape: 'image'
			},
			gadgets:{
				shape: 'image'
			},					
			dashboard:{
				shape: 'image'
			},
			entities:{
				shape: 'image'
			},
			entity:{
				shape: 'image'
			},						
			Clusterdigitaltwins:{				
								
			},
			Clusterwebprojects:{
				
			},
			Clusterdashboards:{
						
			},
			Clusterentities:{
				
			},
			Clusterapis:{
				
			},
			Clusterdeviceandsystems:{
				
			},	
			Clusternotebooks:{
				
			},
			Clusterpipelines:{
				
			},
			Clusterkps:{
				
			},
			Clusterscripts:{
				
			},
			Clustergadgets:{
				
			},			
			Clusterlicensing:{
				
			},
			Clusterflows:{
				
			},
			Clusterresolved :{
				
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
		console.log('ARRAYSOURCE: ' + JSON.stringify(ArraySource));
		console.log('DATA: ' + JSON.stringify(links));
		for (var i = 0; i < ArraySource.length; i++) {
			var idSource = ArraySource[i].id;
			var target = ArraySource[i].target;
			var group = ArraySource[i].group;
			var totalMass = 0;
			
			clusterImage = createSvgCluster(group,idSource);
			
			
			clusterOptionsByData = {

				joinCondition : function(childOptions) {
					return childOptions.category == target;
				},
				processProperties : function(clusterOptions, childNodes,
						childEdges) {
					totalMass = 0;
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
					'label' : '', //'[' + network.getConnectedNodes(idSource,'to').length + '] ' + target,
					shape: 'image',
					image: clusterImage
					
				}
			};
			console.log('CLUSTER DATA: ' + JSON.stringify(clusterOptionsByData));
			network.cluster(clusterOptionsByData);
		}

	};
	
	// create svg for cluster by source.
	var createSvgCluster = function(group,idSource){
		
		var font = '13px Arial';
		var svgCluster = '';
		var groupClusterIcon = {				
			// Clusters , last color 1168A6
		
				"Clusterresolved":'<svg width="32" height="32" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M8.00005 0.800049C11.9765 0.800049 15.2001 4.0236 15.2001 8.00005C15.2001 11.9765 11.9765 15.2001 8.00005 15.2001C4.0236 15.2001 0.800049 11.9765 0.800049 8.00005C0.800049 4.0236 4.0236 0.800049 8.00005 0.800049ZM8 1.6C4.46538 1.6 1.60001 4.46538 1.60001 8C1.60001 11.5346 4.46538 14.4 8 14.4C11.5346 14.4 14.4 11.5346 14.4 8C14.4 4.46538 11.5346 1.6 8 1.6ZM8 5C9.65685 5 11 6.34315 11 8C11 9.65685 9.65685 11 8 11C6.34315 11 5 9.65685 5 8C5 6.34315 6.34315 5 8 5ZM8 5.8C6.78497 5.8 5.8 6.78497 5.8 8C5.8 9.21503 6.78497 10.2 8 10.2C9.21503 10.2 10.2 9.21503 10.2 8C10.2 6.78497 9.21503 5.8 8 5.8Z" fill="#555"/></svg>',
		
			    "Clusterapis":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M2.92011 2.09046C3.5373 0.903569 4.76581 0 6.9 0C10.7692 0 12.3513 2.10639 12.7264 3.50763C13.4395 3.54142 14.252 3.69535 14.9273 4.12504C15.7378 4.64083 16.3 5.52257 16.3 6.9C16.3 9.09103 14.8417 10.3764 14.0789 10.7578L13.9944 10.8H13.9H8.2V11.43C8.71926 11.5354 9.14112 11.9091 9.31465 12.4H15C15.2209 12.4 15.4 12.5791 15.4 12.8C15.4 13.0209 15.2209 13.2 15 13.2H9.37C9.23102 13.8847 8.62569 14.4 7.9 14.4C7.17431 14.4 6.56899 13.8847 6.43001 13.2H0.800001C0.579087 13.2 0.400002 13.0209 0.400002 12.8C0.400002 12.5791 0.579088 12.4 0.800001 12.4H6.48535C6.636 11.9738 6.97379 11.636 7.4 11.4854V10.8H2.4H2.35076L2.30299 10.7881C1.55633 10.6014 0 9.76823 0 7.9C0 6.94115 0.505517 6.25181 1.09228 5.79649C1.53048 5.45644 2.02941 5.23275 2.44727 5.10376C2.33859 4.2361 2.40185 3.08711 2.92011 2.09046ZM3.62989 2.45954C3.1436 3.3947 3.1368 4.54436 3.29223 5.32155L3.37068 5.71379L2.97845 5.79223C2.60911 5.8661 2.04485 6.06991 1.58273 6.42851C1.12782 6.78152 0.800002 7.25885 0.800002 7.9C0.800002 9.1984 1.8554 9.83309 2.45196 10H13.8002C14.396 9.66297 15.5 8.62913 15.5 6.9C15.5 5.77743 15.0622 5.15917 14.4978 4.79996C13.9036 4.42187 13.1181 4.3 12.4 4.3H12.0531L12.004 3.95657C11.8663 2.99241 10.7065 0.8 6.9 0.8C5.03419 0.8 4.09604 1.5631 3.62989 2.45954Z" fill="#555"/></svg>',
				
				"Clusterflows":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M11 0.8H15C15.1105 0.8 15.2 0.889543 15.2 1V3C15.2 3.11046 15.1105 3.2 15 3.2H11C10.8895 3.2 10.8 3.11046 10.8 3V1C10.8 0.889543 10.8895 0.8 11 0.8ZM10 1C10 0.447715 10.4477 0 11 0H15C15.5523 0 16 0.447715 16 1V3C16 3.55228 15.5523 4 15 4H11C10.4477 4 10 3.55228 10 3V2.4H9C8.66863 2.4 8.4 2.66863 8.4 3V7.7H10V7C10 6.44772 10.4477 6 11 6H15C15.5523 6 16 6.44772 16 7V9C16 9.55228 15.5523 10 15 10H11C10.4477 10 10 9.55228 10 9V8.5H8.4V13C8.4 13.3314 8.66863 13.6 9 13.6H10V13C10 12.4477 10.4477 12 11 12H15C15.5523 12 16 12.4477 16 13V15C16 15.5523 15.5523 16 15 16H11C10.4477 16 10 15.5523 10 15V14.4H9C8.2268 14.4 7.6 13.7732 7.6 13V8.5H6V10C6 10.5523 5.55228 11 5 11H1C0.447715 11 0 10.5523 0 10V6C0 5.44772 0.447715 5 1 5H5C5.55228 5 6 5.44772 6 6V7.7H7.6V3C7.6 2.2268 8.2268 1.6 9 1.6H10V1ZM1 5.8H5C5.11046 5.8 5.2 5.88954 5.2 6V10C5.2 10.1105 5.11046 10.2 5 10.2H1C0.889543 10.2 0.8 10.1105 0.8 10V6C0.8 5.88954 0.889543 5.8 1 5.8ZM15 6.8H11C10.8895 6.8 10.8 6.88954 10.8 7V9C10.8 9.11046 10.8895 9.2 11 9.2H15C15.1105 9.2 15.2 9.11046 15.2 9V7C15.2 6.88954 15.1105 6.8 15 6.8ZM11 12.8H15C15.1105 12.8 15.2 12.8895 15.2 13V15C15.2 15.1105 15.1105 15.2 15 15.2H11C10.8895 15.2 10.8 15.1105 10.8 15V13C10.8 12.8895 10.8895 12.8 11 12.8Z" fill="#555"/></svg>',
				
				"Clusterwebprojects":'<svg width="17" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M5.20005 1.20001C6.61065 1.20001 7.77762 2.24312 7.9717 3.60005L12.7888 3.60001C13.8933 3.60001 14.7888 4.49544 14.7888 5.60001C14.7888 5.75137 14.7716 5.90225 14.7375 6.04973L12.9329 13.8698C12.8073 14.4143 12.3224 14.8 11.7636 14.8H2.00005C1.33731 14.8 0.800049 14.2628 0.800049 13.6V2.40001C0.800049 1.73727 1.33731 1.20001 2.00005 1.20001H5.20005ZM10.8002 4.40006H1.60024V13.6001C1.60024 13.821 1.77933 14.0001 2.00024 14.0001H11.6002C11.8212 14.0001 12.0002 13.821 12.0002 13.6001V5.60006C12.0002 4.93732 11.463 4.40006 10.8002 4.40006ZM5.12617 8.40651L5.19807 8.40006C5.39444 8.40006 5.55775 8.54156 5.59162 8.72816L5.59807 8.80006L5.59784 9.99766L6.79589 9.99788C6.99226 9.99788 7.15558 10.1394 7.18945 10.326L7.19589 10.3979C7.19589 10.5943 7.05439 10.7576 6.86779 10.7914L6.79589 10.7979L5.59784 10.7977L5.59807 11.9957C5.59807 12.1921 5.45657 12.3554 5.26997 12.3893L5.19807 12.3957C5.0017 12.3957 4.83838 12.2542 4.80451 12.0676L4.79807 11.9957L4.79784 10.7977L3.60024 10.7979C3.40388 10.7979 3.24056 10.6564 3.20669 10.4698L3.20024 10.3979C3.20024 10.2015 3.34174 10.0382 3.52834 10.0043L3.60024 9.99788L4.79784 9.99766L4.79807 8.80006C4.79807 8.60369 4.93957 8.44037 5.12617 8.40651L5.19807 8.40006L5.12617 8.40651ZM12.7888 4.40006L12.4002 4.4001C12.6513 4.73436 12.8001 5.14983 12.8001 5.60006V10.8857L13.9581 5.86989C13.9785 5.7814 13.9888 5.69088 13.9888 5.60006C13.9888 4.93732 13.4515 4.40006 12.7888 4.40006ZM5.20024 2.00006H2.00024C1.77933 2.00006 1.60024 2.17915 1.60024 2.40006V3.60006H7.16024L7.12958 3.47123C6.8977 2.62328 6.12175 2.00006 5.20024 2.00006Z" fill="#555"/></svg>',
				
				"Clusterdigitaltwins":'<svg width="14" height="13" viewBox="0 0 14 13" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M8.79095 0.485504L6.14418 4.89677C5.74427 5.5633 6.22438 6.41127 7.00168 6.41127H12.2952C13.0725 6.41127 13.5526 5.5633 13.1527 4.89677L10.5059 0.485505C10.1175 -0.161832 9.17935 -0.161838 8.79095 0.485504ZM7.00168 5.61127C6.84622 5.61127 6.75019 5.44168 6.83018 5.30837L7.00168 5.02254L7.12147 4.82288L7.12844 4.81127L9.18196 1.38873L9.18575 1.38242L9.284 1.21866L9.47694 0.897101C9.55462 0.767633 9.74226 0.767633 9.81994 0.897101L10.0129 1.21866L10.1111 1.38242L10.1149 1.38873L12.1684 4.81127L12.1754 4.82288L12.2952 5.02254L12.4667 5.30837C12.5467 5.44168 12.4507 5.61127 12.2952 5.61127H11.9619H11.729H11.7155H7.58139H7.56785H7.33501H7.00168ZM2.79081 6.4855L0.144051 10.8968C-0.255862 11.5633 0.224247 12.4113 1.00154 12.4113H6.29507C7.07236 12.4113 7.55248 11.5633 7.15256 10.8968L4.5058 6.4855C4.1174 5.83817 3.17922 5.83816 2.79081 6.4855ZM1.00154 11.6113C0.846085 11.6113 0.750063 11.4417 0.830046 11.3084L1.00154 11.0225L1.12134 10.8229L1.12831 10.8113L3.18183 7.38873L3.18562 7.38242L3.28387 7.21866L3.47681 6.8971C3.55449 6.76763 3.74212 6.76763 3.8198 6.8971L4.01274 7.21866L4.111 7.38242L4.11478 7.38873L6.16831 10.8113L6.17527 10.8229L6.29507 11.0225L6.46657 11.3084C6.54655 11.4417 6.45053 11.6113 6.29507 11.6113H5.96174H5.72889H5.71535H1.58126H1.56772H1.33488H1.00154Z" fill="#555"/></svg>',
				
				"Clusterlicensing":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M12.349 1.6H3.65121C2.93799 1.6 2.67936 1.67426 2.41862 1.8137C2.15788 1.95315 1.95325 2.15778 1.8138 2.41852C1.67436 2.67926 1.6001 2.93789 1.6001 3.65111V12.3489C1.6001 13.0621 1.67436 13.3207 1.8138 13.5815C1.95325 13.8422 2.15788 14.0468 2.41862 14.1863C2.67936 14.3257 2.93799 14.4 3.65121 14.4H12.349C13.0622 14.4 13.3208 14.3257 13.5816 14.1863C13.8423 14.0468 14.0469 13.8422 14.1864 13.5815C14.3258 13.3207 14.4001 13.0621 14.4001 12.3489V3.65111C14.4001 2.93789 14.3258 2.67926 14.1864 2.41852C14.0469 2.15778 13.8423 1.95315 13.5816 1.8137C13.3208 1.67426 13.0622 1.6 12.349 1.6ZM2.7958 2.51906C2.96038 2.43104 3.12163 2.3999 3.65111 2.3999H12.3489C12.8784 2.3999 13.0396 2.43104 13.2042 2.51906C13.3255 2.58394 13.416 2.67438 13.4808 2.7957C13.5689 2.96029 13.6 3.12153 13.6 3.65101V12.3488C13.6 12.8783 13.5689 13.0395 13.4808 13.2041C13.416 13.3254 13.3255 13.4159 13.2042 13.4807C13.0396 13.5688 12.8784 13.5999 12.3489 13.5999H3.65111C3.12163 13.5999 2.96038 13.5688 2.7958 13.4807C2.67448 13.4159 2.58404 13.3254 2.51916 13.2041C2.43114 13.0395 2.4 12.8783 2.4 12.3488V3.65101C2.4 3.12153 2.43114 2.96029 2.51916 2.7957C2.58404 2.67438 2.67448 2.58394 2.7958 2.51906ZM4 9.9999C4 9.77899 4.17909 9.5999 4.4 9.5999H11.6C11.8209 9.5999 12 9.77899 12 9.9999C12 10.2208 11.8209 10.3999 11.6 10.3999H4.4C4.17909 10.3999 4 10.2208 4 9.9999ZM4.4 5.5999C4.17909 5.5999 4 5.77899 4 5.9999C4 6.22081 4.17909 6.3999 4.4 6.3999H11.6C11.8209 6.3999 12 6.22081 12 5.9999C12 5.77899 11.8209 5.5999 11.6 5.5999H4.4Z" fill="#1168A6"/></svg>',
				
				"Clusterusers":'<svg width="19" height="15" viewBox="0 0 19 15" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M10.8492 5.87909C9.97645 5.3076 9.3999 4.32113 9.3999 3.2C9.3999 1.43269 10.8326 0 12.5999 0C14.3672 0 15.7999 1.43269 15.7999 3.2C15.7999 4.32113 15.2234 5.3076 14.3506 5.87909C16.586 6.61414 18.1999 8.71859 18.1999 11.2C18.1999 12.6027 14.374 13.1856 11.1945 12.9486C11.1981 13.0319 11.2 13.1157 11.2 13.1999C11.2 15.5999 0 15.5999 0 13.1999C0 10.7185 1.61393 8.61404 3.84933 7.87899C2.97655 7.3075 2.4 6.32103 2.4 5.1999C2.4 3.43259 3.83269 1.9999 5.6 1.9999C7.36731 1.9999 8.8 3.43259 8.8 5.1999C8.8 6.32103 8.22345 7.3075 7.35067 7.87899C7.56685 7.95007 7.77722 8.03397 7.98088 8.12977L7.97072 8.04774C8.65849 7.03968 9.66694 6.26785 10.8492 5.87909ZM8.63543 8.49313C9.89403 9.30649 10.8056 10.6109 11.0989 12.1354C11.5704 12.1763 12.0749 12.1999 12.6 12.1999C15.2673 12.1999 17.4 11.5906 17.4 11.1999C17.4 8.54894 15.251 6.3999 12.6 6.3999C10.9531 6.3999 9.49998 7.22928 8.63543 8.49313ZM12.6 5.5999C13.9255 5.5999 15 4.52539 15 3.1999C15 1.87442 13.9255 0.799902 12.6 0.799902C11.2745 0.799902 10.2 1.87442 10.2 3.1999C10.2 4.52539 11.2745 5.5999 12.6 5.5999ZM5.59998 8.39981C8.25094 8.39981 10.4 10.5488 10.4 13.1998C10.4 13.5905 8.26725 14.1998 5.59998 14.1998C2.9327 14.1998 0.799976 13.5905 0.799976 13.1998C0.799976 10.5488 2.94901 8.39981 5.59998 8.39981ZM7.99998 5.1998C7.99998 6.52529 6.92546 7.5998 5.59998 7.5998C4.27449 7.5998 3.19998 6.52529 3.19998 5.1998C3.19998 3.87432 4.27449 2.7998 5.59998 2.7998C6.92546 2.7998 7.99998 3.87432 7.99998 5.1998Z" fill="#1168A6"/></svg>',
				"Clustervisualizations":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M0 0.4C0 0.179086 0.179086 0 0.4 0C0.620914 0 0.8 0.179086 0.8 0.4V3.2002H2.6C2.82091 3.2002 3 3.37928 3 3.6002C3 3.82111 2.82091 4.0002 2.6 4.0002H0.8V7.2002H2.6C2.82091 7.2002 3 7.37928 3 7.6002C3 7.82111 2.82091 8.0002 2.6 8.0002H0.8V11.2002H2.6C2.82091 11.2002 3 11.3793 3 11.6002C3 11.8211 2.82091 12.0002 2.6 12.0002H0.8V15.2002H4V9.2C4 9.08954 4.08954 9 4.2 9H7.8C7.91046 9 8 9.08954 8 9.2V15.2002H10V5.2C10 5.08954 10.0895 5 10.2 5H13.8C13.9105 5 14 5.08954 14 5.2V15.2002H15.6C15.8209 15.2002 16 15.3793 16 15.6002C16 15.8211 15.8209 16.0002 15.6 16.0002H0.4C0.179086 16.0002 0 15.8211 0 15.6002V15.6V11.6002V7.6002V3.6002V0.4ZM4.8 9.8V15.2H7.2V9.8H4.8ZM10.8 15.2V5.8H13.2V15.2H10.8Z" fill="#555"/></svg>',
				"Clusteranalytics":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M0 0.4C0 0.179086 0.179086 0 0.4 0C0.620914 0 0.8 0.179086 0.8 0.4V3.2002H2.6C2.82091 3.2002 3 3.37928 3 3.6002C3 3.82111 2.82091 4.0002 2.6 4.0002H0.8V7.2002H2.6C2.82091 7.2002 3 7.37928 3 7.6002C3 7.82111 2.82091 8.0002 2.6 8.0002H0.8V11.2002H2.6C2.82091 11.2002 3 11.3793 3 11.6002C3 11.8211 2.82091 12.0002 2.6 12.0002H0.8V15.2002H4V9.2C4 9.08954 4.08954 9 4.2 9H7.8C7.91046 9 8 9.08954 8 9.2V15.2002H10V5.2C10 5.08954 10.0895 5 10.2 5H13.8C13.9105 5 14 5.08954 14 5.2V15.2002H15.6C15.8209 15.2002 16 15.3793 16 15.6002C16 15.8211 15.8209 16.0002 15.6 16.0002H0.4C0.179086 16.0002 0 15.8211 0 15.6002V15.6V11.6002V7.6002V3.6002V0.4ZM4.8 9.8V15.2H7.2V9.8H4.8ZM10.8 15.2V5.8H13.2V15.2H10.8Z" fill="#555"/></svg>',
				
				"Clusterdeviceandsystems":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M8.92772 0.930516C8.96519 0.921761 9.07629 0.910693 9.26987 0.919288C9.45393 0.92746 9.68831 0.952193 9.95835 0.999657C10.4988 1.09465 11.1673 1.27807 11.8457 1.58907C13.2022 2.21096 14.5657 3.32499 15.0778 5.23936L15.8506 5.03264C15.2581 2.81761 13.6761 1.54812 12.1791 0.861849C11.4307 0.518778 10.6955 0.316959 10.0968 0.211735C9.79733 0.159092 9.52832 0.129974 9.30536 0.120075C9.09191 0.110599 8.89234 0.117239 8.74572 0.151494L8.92772 0.930516ZM12.9184 5.37745C11.6877 3.46063 10.0576 3.35139 9.47997 3.48634L9.29796 2.70732C10.1931 2.49819 12.1713 2.73304 13.5916 4.94524L12.9184 5.37745ZM1.6 5.28518H10.5504C11.0806 5.28518 11.5104 5.71499 11.5104 6.24518V14.4C11.5104 14.9302 11.0806 15.36 10.5504 15.36H1.6C1.06981 15.36 0.64 14.9302 0.64 14.4V6.24518C0.64 5.71499 1.06981 5.28518 1.6 5.28518ZM0 6.24518C0 5.36153 0.716345 4.64518 1.6 4.64518H10.5504C11.4341 4.64518 12.1504 5.36152 12.1504 6.24518V14.4C12.1504 15.2836 11.4341 16 10.5504 16H1.6C0.716343 16 0 15.2836 0 14.4V6.24518ZM2.72531 12.9032C2.44026 12.9032 2.20918 13.1343 2.20918 13.4194C2.20918 13.7044 2.44026 13.9355 2.72531 13.9355H9.42515C9.7102 13.9355 9.94128 13.7044 9.94128 13.4194C9.94128 13.1343 9.7102 12.9032 9.42515 12.9032H2.72531Z" fill="#555"/></svg>',
				"Clusternotebooks":'<svg width="13" height="17" viewBox="0 0 13 17" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M1.79999 -1.2219e-05H10.8C11.7941 -1.2219e-05 12.6 0.805876 12.6 1.79999V14.8C12.6 15.7941 11.7941 16.6 10.8 16.6H1.79999C0.805875 16.6 -1.2219e-05 15.7941 -1.2219e-05 14.8V1.79999C-1.2219e-05 0.805875 0.805875 -1.2219e-05 1.79999 -1.2219e-05ZM1.79999 0.799988C1.2477 0.799988 0.799988 1.2477 0.799988 1.79999V14.8C0.799988 15.3523 1.2477 15.8 1.79999 15.8H10.8C11.3523 15.8 11.8 15.3523 11.8 14.8V1.79999C11.8 1.2477 11.3523 0.799988 10.8 0.799988H1.79999ZM2.79999 4.19999C2.79999 3.97907 2.97907 3.79999 3.19999 3.79999H6.39999C6.6209 3.79999 6.79999 3.97907 6.79999 4.19999C6.79999 4.4209 6.6209 4.59999 6.39999 4.59999H3.19999C2.97907 4.59999 2.79999 4.4209 2.79999 4.19999ZM3.19999 6.5C2.97907 6.5 2.79999 6.67909 2.79999 6.9C2.79999 7.12091 2.97907 7.3 3.19999 7.3H9.39999C9.6209 7.3 9.79999 7.12091 9.79999 6.9C9.79999 6.67909 9.6209 6.5 9.39999 6.5H3.19999ZM2.79999 9.60001C2.79999 9.3791 2.97907 9.20001 3.19999 9.20001H9.39999C9.6209 9.20001 9.79999 9.3791 9.79999 9.60001C9.79999 9.82093 9.6209 10 9.39999 10H3.19999C2.97907 10 2.79999 9.82093 2.79999 9.60001ZM3.19999 11.9C2.97907 11.9 2.79999 12.0791 2.79999 12.3C2.79999 12.5209 2.97907 12.7 3.19999 12.7H9.39999C9.6209 12.7 9.79999 12.5209 9.79999 12.3C9.79999 12.0791 9.6209 11.9 9.39999 11.9H3.19999Z" fill="#1168A6"/></svg>',
				
				"Clusterdashboards":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M2.05111 0H13.9489C14.6621 0 14.9207 0.0742605 15.1815 0.213706C15.4422 0.353152 15.6468 0.557783 15.7863 0.818523C15.9257 1.07926 16 1.33789 16 2.05111V13.9489C16 14.6621 15.9257 14.9207 15.7863 15.1815C15.6468 15.4422 15.4422 15.6468 15.1815 15.7863C14.9207 15.9257 14.6621 16 13.9489 16H2.05111C1.33789 16 1.07926 15.9257 0.818523 15.7863C0.557783 15.6468 0.353152 15.4422 0.213706 15.1815C0.0742605 14.9207 0 14.6621 0 13.9489V2.05111C0 1.33789 0.0742605 1.07926 0.213706 0.818523C0.353152 0.557783 0.557783 0.353152 0.818523 0.213706C1.07926 0.0742605 1.33789 0 2.05111 0ZM2.05111 0.8C1.52163 0.8 1.36038 0.831137 1.1958 0.919157C1.07448 0.984043 0.984043 1.07448 0.919157 1.1958C0.831137 1.36038 0.8 1.52163 0.8 2.05111V8H7.2V0.8H2.05111ZM8 0.8V7.2H15.2L15.2 2.05111C15.2 1.52163 15.1689 1.36038 15.0808 1.1958C15.016 1.07448 14.9255 0.984043 14.8042 0.919157C14.6396 0.831137 14.4784 0.8 13.9489 0.8H8ZM8 8V15.2L13.9489 15.2C14.4784 15.2 14.6396 15.1689 14.8042 15.0808C14.9255 15.016 15.016 14.9255 15.0808 14.8042C15.1689 14.6396 15.2 14.4784 15.2 13.9489L15.2 8H8ZM7.2 15.2V8.8H0.8V13.9489C0.8 14.4784 0.831137 14.6396 0.919157 14.8042C0.984043 14.9255 1.07448 15.016 1.1958 15.0808C1.36038 15.1689 1.52163 15.2 2.05111 15.2L7.2 15.2Z" fill="#1168A6"/></svg>',
				
				"Clusterscripts":'<svg width="16" height="14" viewBox="0 0 16 14" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M8.68553 3.14948C8.73707 3.02573 8.87917 2.96718 9.00292 3.01872C9.12668 3.07025 9.18522 3.21235 9.13369 3.3361L7.25378 7.85052C7.20225 7.97427 7.06015 8.03282 6.9364 7.98129C6.81264 7.92975 6.7541 7.78765 6.80563 7.6639L8.68553 3.14948ZM5.33558 7.08756C5.43698 7.17525 5.44809 7.32854 5.3604 7.42994C5.27271 7.53133 5.11942 7.54244 5.01803 7.45475L3.08396 5.78212C3.00353 5.71256 2.97991 5.60175 3.01733 5.50829C3.02487 5.45897 3.04764 5.41153 3.08562 5.37355L4.8937 3.56546C4.9885 3.47067 5.14218 3.47067 5.23697 3.56546C5.33177 3.66025 5.33177 3.81394 5.23697 3.90873L3.57808 5.56763L5.33558 7.08756ZM10.1517 3.88093C10.0503 3.79324 10.0392 3.63996 10.1269 3.53856C10.2146 3.43716 10.3678 3.42605 10.4692 3.51374L12.4033 5.18638C12.4837 5.25593 12.5074 5.36675 12.4699 5.46021C12.4624 5.50953 12.4396 5.55697 12.4016 5.59495L10.5936 7.40303C10.4988 7.49782 10.3451 7.49782 10.2503 7.40303C10.1555 7.30824 10.1555 7.15455 10.2503 7.05976L11.9092 5.40087L10.1517 3.88093Z" fill="#555"/><path fill-rule="evenodd" clip-rule="evenodd" d="M1.8 0H13.8C14.7941 0 15.6 0.805887 15.6 1.8V8.8C15.6 9.79411 14.7941 10.6 13.8 10.6H8.8V12.8H10.4C10.6209 12.8 10.8 12.9791 10.8 13.2C10.8 13.4209 10.6209 13.6 10.4 13.6H6.2C5.97909 13.6 5.8 13.4209 5.8 13.2C5.8 12.9791 5.97909 12.8 6.2 12.8H7.8V10.6H1.8C0.805888 10.6 0 9.79411 0 8.8V1.8C0 0.805887 0.805888 0 1.8 0ZM7.8 9.8H8.8H13.8C14.3523 9.8 14.8 9.35229 14.8 8.8V1.8C14.8 1.24772 14.3523 0.8 13.8 0.8H1.8C1.24772 0.8 0.8 1.24772 0.8 1.8V8.8C0.8 9.35229 1.24772 9.8 1.8 9.8H7.8Z" fill="#555"/></svg>',
				
				"Clusterpipelines":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M5.8 0.8V2.2H10.2V0.8H5.8ZM5.5 0C5.22386 0 5 0.223858 5 0.5V2.5C5 2.77614 5.22386 3 5.5 3H6L6 11H3V10.5C3 10.2239 2.77614 10 2.5 10H0.5C0.223857 10 0 10.2239 0 10.5V15.5C0 15.7761 0.223858 16 0.5 16H2.5C2.77614 16 3 15.7761 3 15.5V15H13V15.5C13 15.7761 13.2239 16 13.5 16H15.5C15.7761 16 16 15.7761 16 15.5V10.5C16 10.2239 15.7761 10 15.5 10H13.5C13.2239 10 13 10.2239 13 10.5V11H10L10 3H10.5C10.7761 3 11 2.77614 11 2.5V0.5C11 0.223858 10.7761 0 10.5 0H5.5ZM13.8 15.2H15.2V10.8H13.8V15.2ZM2.2 15.2H0.8L0.8 10.8H2.2L2.2 15.2ZM3.00117 11.8V14.2H12.9723V11.8H3.00117ZM9.2 10.992H6.8V3.0109L9.2 3.0109V10.992Z" fill="#555"/></svg>',
				
				
				"Clustergadgets":'<svg width="15" height="15" viewBox="0 0 15 15" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M5 0.8H2C1.33726 0.8 0.8 1.33726 0.8 2V5C0.8 5.66274 1.33726 6.2 2 6.2H5C5.66274 6.2 6.2 5.66274 6.2 5V2C6.2 1.33726 5.66274 0.8 5 0.8ZM2 0C0.895431 0 0 0.89543 0 2V5C0 6.10457 0.89543 7 2 7H5C6.10457 7 7 6.10457 7 5V2C7 0.895431 6.10457 0 5 0H2ZM13 0.8H10C9.33726 0.8 8.8 1.33726 8.8 2V5C8.8 5.66274 9.33726 6.2 10 6.2H13C13.6627 6.2 14.2 5.66274 14.2 5V2C14.2 1.33726 13.6627 0.8 13 0.8ZM10 0C8.89543 0 8 0.89543 8 2V5C8 6.10457 8.89543 7 10 7H13C14.1046 7 15 6.10457 15 5V2C15 0.895431 14.1046 0 13 0H10ZM2 8.8H5C5.66274 8.8 6.2 9.33726 6.2 10V13C6.2 13.6627 5.66274 14.2 5 14.2H2C1.33726 14.2 0.8 13.6627 0.8 13V10C0.8 9.33726 1.33726 8.8 2 8.8ZM0 10C0 8.89543 0.895431 8 2 8H5C6.10457 8 7 8.89543 7 10V13C7 14.1046 6.10457 15 5 15H2C0.89543 15 0 14.1046 0 13V10ZM11 11V8H12V11H15V12H12V15H11V12H8V11H11Z" fill="#555"/></svg>',
				
				"Clusterentities":'<svg width="16" height="16" viewBox="-2 0 15 15" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M10.2511 1.31952C10.2515 1.31956 10.2493 1.32266 10.2434 1.32868C10.2477 1.32249 10.2507 1.31948 10.2511 1.31952ZM9.17859 1.21115C9.52626 1.30597 9.79082 1.40633 9.97738 1.5C9.79082 1.59367 9.52626 1.69403 9.17859 1.78885C8.26957 2.03676 6.96869 2.2 5.5 2.2C4.03131 2.2 2.73043 2.03676 1.82141 1.78885C1.47374 1.69403 1.20917 1.59367 1.02261 1.5C1.20917 1.40633 1.47374 1.30597 1.82141 1.21115C2.73043 0.963236 4.03131 0.8 5.5 0.8C6.96869 0.8 8.26957 0.963236 9.17859 1.21115ZM0.748928 1.31952C0.749329 1.31948 0.75228 1.32249 0.756579 1.32868C0.750677 1.32266 0.748528 1.31956 0.748928 1.31952ZM10.1 2.32257C9.11706 2.7306 7.42389 3 5.5 3C3.57611 3 1.88294 2.7306 0.9 2.32257V4.93112C0.939871 4.97252 1.01417 5.03217 1.13207 5.10499C1.36574 5.24932 1.7127 5.41015 2.13453 5.5608C2.97884 5.86234 4.05746 6.1 5 6.1C6.37625 6.1 7.71177 5.85699 8.69745 5.55504C9.19163 5.40366 9.58417 5.24162 9.84485 5.09607C9.97374 5.0241 10.0554 4.96439 10.1 4.9224V2.32257ZM10.1 5.86628C9.78853 6.02442 9.38876 6.17996 8.93177 6.31996C7.87719 6.64301 6.4627 6.9 5 6.9C3.94254 6.9 2.77116 6.63766 1.86547 6.3142C1.50184 6.18433 1.17049 6.04089 0.9 5.89432V8.93112C0.939871 8.97253 1.01417 9.03217 1.13207 9.10499C1.36574 9.24932 1.7127 9.41015 2.13453 9.5608C2.97884 9.86234 4.05746 10.1 5 10.1C6.71405 10.1 8.04548 9.85487 8.93601 9.55803C9.38326 9.40894 9.70621 9.25116 9.90847 9.11415C10.0102 9.0452 10.0687 8.99004 10.0976 8.95471L10.1 8.9518V5.86628ZM0.1 5V9V12.5C0.1 12.7498 0.271877 12.9237 0.369411 13.009C0.489093 13.1138 0.64512 13.2108 0.814066 13.2995C1.15568 13.4788 1.61797 13.6591 2.13069 13.8193C3.15406 14.1391 4.44618 14.4 5.5 14.4C7.21106 14.4 8.5104 14.1447 9.396 13.8119C9.83712 13.6462 10.1884 13.4563 10.4378 13.2589C10.5623 13.1603 10.6713 13.0521 10.752 12.9347C10.8309 12.82 10.9 12.6711 10.9 12.5V9V5V1.78621C10.9656 1.69356 11 1.59787 11 1.5C11 0.671573 8.53757 0 5.5 0C2.46243 0 0 0.671573 0 1.5C0 1.59787 0.0343708 1.69356 0.1 1.7862V5ZM0.9 12.4103V9.89432C1.17049 10.0409 1.50184 10.1843 1.86547 10.3142C2.77116 10.6377 3.94254 10.9 5 10.9C6.78595 10.9 8.20452 10.6451 9.18899 10.317C9.54489 10.1983 9.85101 10.0678 10.1 9.93208V12.4704C10.0982 12.4734 10.0958 12.477 10.0929 12.4813C10.0702 12.5143 10.0239 12.5662 9.9412 12.6317C9.77608 12.7624 9.50503 12.9163 9.11458 13.0631C8.33705 13.3553 7.13639 13.6 5.5 13.6C4.55382 13.6 3.34594 13.3609 2.36931 13.0557C1.88203 12.9034 1.46932 12.7399 1.18593 12.5912C1.04579 12.5176 0.952895 12.4556 0.9 12.4103Z" fill="#555"/></svg>'
				
		
		
		};
		
		// calculate with for draw node in image svg mode.
		var calculateClusterTextWidth = function(label,font){
		
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
		
		// Capitalize first letter
		String.prototype.capitalize = function() {  return this.charAt(0).toUpperCase() + this.slice(1); }
		
		// CREATE SVG ELEMENTS		
		let middle = Math.ceil((calculateClusterTextWidth(group,font) / 2) - 30);
		let counter = middle + 20;
		svgCluster += '<svg xmlns="http://www.w3.org/2000/svg" width="'+ calculateClusterTextWidth(group,font) +'" height="60">'+
				'<rect x="0" y="0" width="100%" rx="4" ry="4" height="100%" fill="#FFFFFF"  stroke="#555" style="stroke-width:3" ></rect>'+
				'<foreignObject x="12" y="12" width="100%" height="100%">'+
					'<div xmlns="http://www.w3.org/1999/xhtml">'+						
						'<span style="position: absolute; top:0px; left: '+ middle +'px;">' + groupClusterIcon['Cluster'+group] +'</span>'+
						'<span style="text-align: center; position: absolute; font-family: Soho,Arial; font-size:14px; color:#505D66; top:1px; left: '+ counter +'px;font-weight: bolder">' + parseInt(network.getConnectedNodes(idSource,'to').length) + '</span>'+
						'<span style="position: absolute; text-align: center; font-family: Soho,Arial; font-size:13px; color:#505D66; top:20px;  left: 12px;font-weight: bolder">'+ group.capitalize() +'</span>'+						
					'</div>'+
			    '</foreignObject>'+
			'</svg>';
		var imageUrl = "data:image/svg+xml;charset=utf-8," + encodeURIComponent(svgCluster);		
		
		return imageUrl;
		
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
			context.font = fontText; 
			width = context.measureText(inputText).width; 
			totalWidth = elementsWidth + Math.ceil(width);	
			
			return totalWidth;		  
		};
		
		// create svg with title, font and group
		var createSvg = function(title,font,group){
			
			var svg = '';
			var string = '';
			var hgroup = 64;
			var groupType = '';
			var groupColor = '#000';
			var groupStroke = 2;
			var groupX = 16;
			var groupY = 16;
			var stroke = '#D9E7F1';
			var groupWeight = '400';
			var groupFontSize = '11px';
			var groupIconLeft = '22px';
			
			// icons for groups
			var groupIcon = {
				"apis":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M2.92011 2.09046C3.5373 0.903569 4.76581 0 6.9 0C10.7692 0 12.3513 2.10639 12.7264 3.50763C13.4395 3.54142 14.252 3.69535 14.9273 4.12504C15.7378 4.64083 16.3 5.52257 16.3 6.9C16.3 9.09103 14.8417 10.3764 14.0789 10.7578L13.9944 10.8H13.9H8.2V11.43C8.71926 11.5354 9.14112 11.9091 9.31465 12.4H15C15.2209 12.4 15.4 12.5791 15.4 12.8C15.4 13.0209 15.2209 13.2 15 13.2H9.37C9.23102 13.8847 8.62569 14.4 7.9 14.4C7.17431 14.4 6.56899 13.8847 6.43001 13.2H0.800001C0.579087 13.2 0.400002 13.0209 0.400002 12.8C0.400002 12.5791 0.579088 12.4 0.800001 12.4H6.48535C6.636 11.9738 6.97379 11.636 7.4 11.4854V10.8H2.4H2.35076L2.30299 10.7881C1.55633 10.6014 0 9.76823 0 7.9C0 6.94115 0.505517 6.25181 1.09228 5.79649C1.53048 5.45644 2.02941 5.23275 2.44727 5.10376C2.33859 4.2361 2.40185 3.08711 2.92011 2.09046ZM3.62989 2.45954C3.1436 3.3947 3.1368 4.54436 3.29223 5.32155L3.37068 5.71379L2.97845 5.79223C2.60911 5.8661 2.04485 6.06991 1.58273 6.42851C1.12782 6.78152 0.800002 7.25885 0.800002 7.9C0.800002 9.1984 1.8554 9.83309 2.45196 10H13.8002C14.396 9.66297 15.5 8.62913 15.5 6.9C15.5 5.77743 15.0622 5.15917 14.4978 4.79996C13.9036 4.42187 13.1181 4.3 12.4 4.3H12.0531L12.004 3.95657C11.8663 2.99241 10.7065 0.8 6.9 0.8C5.03419 0.8 4.09604 1.5631 3.62989 2.45954Z" fill="#1B6EAA"/></svg>',
				"api":'<svg width="17" height="15" viewBox="0 0 17 15" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M2.92011 2.09046C3.5373 0.903569 4.76581 0 6.9 0C10.7692 0 12.3513 2.10639 12.7264 3.50763C13.4395 3.54142 14.252 3.69535 14.9273 4.12504C15.7378 4.64083 16.3 5.52257 16.3 6.9C16.3 9.09103 14.8417 10.3764 14.0789 10.7578L13.9944 10.8H13.9H8.2V11.43C8.71926 11.5354 9.14112 11.9091 9.31465 12.4H15C15.2209 12.4 15.4 12.5791 15.4 12.8C15.4 13.0209 15.2209 13.2 15 13.2H9.37C9.23102 13.8847 8.62569 14.4 7.9 14.4C7.17431 14.4 6.56899 13.8847 6.43001 13.2H0.800001C0.579087 13.2 0.400002 13.0209 0.400002 12.8C0.400002 12.5791 0.579088 12.4 0.800001 12.4H6.48535C6.636 11.9738 6.97379 11.636 7.4 11.4854V10.8H2.4H2.35076L2.30299 10.7881C1.55633 10.6014 0 9.76823 0 7.9C0 6.94115 0.505517 6.25181 1.09228 5.79649C1.53048 5.45644 2.02941 5.23275 2.44727 5.10376C2.33859 4.2361 2.40185 3.08711 2.92011 2.09046ZM3.62989 2.45954C3.1436 3.3947 3.1368 4.54436 3.29223 5.32155L3.37068 5.71379L2.97845 5.79223C2.60911 5.8661 2.04485 6.06991 1.58273 6.42851C1.12782 6.78152 0.800002 7.25885 0.800002 7.9C0.800002 9.1984 1.8554 9.83309 2.45196 10H13.8002C14.396 9.66297 15.5 8.62913 15.5 6.9C15.5 5.77743 15.0622 5.15917 14.4978 4.79996C13.9036 4.42187 13.1181 4.3 12.4 4.3H12.0531L12.004 3.95657C11.8663 2.99241 10.7065 0.8 6.9 0.8C5.03419 0.8 4.09604 1.5631 3.62989 2.45954Z" fill="#000"/></svg>',
				"flows":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M11 0.8H15C15.1105 0.8 15.2 0.889543 15.2 1V3C15.2 3.11046 15.1105 3.2 15 3.2H11C10.8895 3.2 10.8 3.11046 10.8 3V1C10.8 0.889543 10.8895 0.8 11 0.8ZM10 1C10 0.447715 10.4477 0 11 0H15C15.5523 0 16 0.447715 16 1V3C16 3.55228 15.5523 4 15 4H11C10.4477 4 10 3.55228 10 3V2.4H9C8.66863 2.4 8.4 2.66863 8.4 3V7.7H10V7C10 6.44772 10.4477 6 11 6H15C15.5523 6 16 6.44772 16 7V9C16 9.55228 15.5523 10 15 10H11C10.4477 10 10 9.55228 10 9V8.5H8.4V13C8.4 13.3314 8.66863 13.6 9 13.6H10V13C10 12.4477 10.4477 12 11 12H15C15.5523 12 16 12.4477 16 13V15C16 15.5523 15.5523 16 15 16H11C10.4477 16 10 15.5523 10 15V14.4H9C8.2268 14.4 7.6 13.7732 7.6 13V8.5H6V10C6 10.5523 5.55228 11 5 11H1C0.447715 11 0 10.5523 0 10V6C0 5.44772 0.447715 5 1 5H5C5.55228 5 6 5.44772 6 6V7.7H7.6V3C7.6 2.2268 8.2268 1.6 9 1.6H10V1ZM1 5.8H5C5.11046 5.8 5.2 5.88954 5.2 6V10C5.2 10.1105 5.11046 10.2 5 10.2H1C0.889543 10.2 0.8 10.1105 0.8 10V6C0.8 5.88954 0.889543 5.8 1 5.8ZM15 6.8H11C10.8895 6.8 10.8 6.88954 10.8 7V9C10.8 9.11046 10.8895 9.2 11 9.2H15C15.1105 9.2 15.2 9.11046 15.2 9V7C15.2 6.88954 15.1105 6.8 15 6.8ZM11 12.8H15C15.1105 12.8 15.2 12.8895 15.2 13V15C15.2 15.1105 15.1105 15.2 15 15.2H11C10.8895 15.2 10.8 15.1105 10.8 15V13C10.8 12.8895 10.8895 12.8 11 12.8Z" fill="#1B6EAA"/></svg>',
				"flow":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M11 0.8H15C15.1105 0.8 15.2 0.889543 15.2 1V3C15.2 3.11046 15.1105 3.2 15 3.2H11C10.8895 3.2 10.8 3.11046 10.8 3V1C10.8 0.889543 10.8895 0.8 11 0.8ZM10 1C10 0.447715 10.4477 0 11 0H15C15.5523 0 16 0.447715 16 1V3C16 3.55228 15.5523 4 15 4H11C10.4477 4 10 3.55228 10 3V2.4H9C8.66863 2.4 8.4 2.66863 8.4 3V7.7H10V7C10 6.44772 10.4477 6 11 6H15C15.5523 6 16 6.44772 16 7V9C16 9.55228 15.5523 10 15 10H11C10.4477 10 10 9.55228 10 9V8.5H8.4V13C8.4 13.3314 8.66863 13.6 9 13.6H10V13C10 12.4477 10.4477 12 11 12H15C15.5523 12 16 12.4477 16 13V15C16 15.5523 15.5523 16 15 16H11C10.4477 16 10 15.5523 10 15V14.4H9C8.2268 14.4 7.6 13.7732 7.6 13V8.5H6V10C6 10.5523 5.55228 11 5 11H1C0.447715 11 0 10.5523 0 10V6C0 5.44772 0.447715 5 1 5H5C5.55228 5 6 5.44772 6 6V7.7H7.6V3C7.6 2.2268 8.2268 1.6 9 1.6H10V1ZM1 5.8H5C5.11046 5.8 5.2 5.88954 5.2 6V10C5.2 10.1105 5.11046 10.2 5 10.2H1C0.889543 10.2 0.8 10.1105 0.8 10V6C0.8 5.88954 0.889543 5.8 1 5.8ZM15 6.8H11C10.8895 6.8 10.8 6.88954 10.8 7V9C10.8 9.11046 10.8895 9.2 11 9.2H15C15.1105 9.2 15.2 9.11046 15.2 9V7C15.2 6.88954 15.1105 6.8 15 6.8ZM11 12.8H15C15.1105 12.8 15.2 12.8895 15.2 13V15C15.2 15.1105 15.1105 15.2 15 15.2H11C10.8895 15.2 10.8 15.1105 10.8 15V13C10.8 12.8895 10.8895 12.8 11 12.8Z" fill="#000"/></svg>',
				"webprojects":'<svg width="17" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M5.20005 1.20001C6.61065 1.20001 7.77762 2.24312 7.9717 3.60005L12.7888 3.60001C13.8933 3.60001 14.7888 4.49544 14.7888 5.60001C14.7888 5.75137 14.7716 5.90225 14.7375 6.04973L12.9329 13.8698C12.8073 14.4143 12.3224 14.8 11.7636 14.8H2.00005C1.33731 14.8 0.800049 14.2628 0.800049 13.6V2.40001C0.800049 1.73727 1.33731 1.20001 2.00005 1.20001H5.20005ZM10.8002 4.40006H1.60024V13.6001C1.60024 13.821 1.77933 14.0001 2.00024 14.0001H11.6002C11.8212 14.0001 12.0002 13.821 12.0002 13.6001V5.60006C12.0002 4.93732 11.463 4.40006 10.8002 4.40006ZM5.12617 8.40651L5.19807 8.40006C5.39444 8.40006 5.55775 8.54156 5.59162 8.72816L5.59807 8.80006L5.59784 9.99766L6.79589 9.99788C6.99226 9.99788 7.15558 10.1394 7.18945 10.326L7.19589 10.3979C7.19589 10.5943 7.05439 10.7576 6.86779 10.7914L6.79589 10.7979L5.59784 10.7977L5.59807 11.9957C5.59807 12.1921 5.45657 12.3554 5.26997 12.3893L5.19807 12.3957C5.0017 12.3957 4.83838 12.2542 4.80451 12.0676L4.79807 11.9957L4.79784 10.7977L3.60024 10.7979C3.40388 10.7979 3.24056 10.6564 3.20669 10.4698L3.20024 10.3979C3.20024 10.2015 3.34174 10.0382 3.52834 10.0043L3.60024 9.99788L4.79784 9.99766L4.79807 8.80006C4.79807 8.60369 4.93957 8.44037 5.12617 8.40651L5.19807 8.40006L5.12617 8.40651ZM12.7888 4.40006L12.4002 4.4001C12.6513 4.73436 12.8001 5.14983 12.8001 5.60006V10.8857L13.9581 5.86989C13.9785 5.7814 13.9888 5.69088 13.9888 5.60006C13.9888 4.93732 13.4515 4.40006 12.7888 4.40006ZM5.20024 2.00006H2.00024C1.77933 2.00006 1.60024 2.17915 1.60024 2.40006V3.60006H7.16024L7.12958 3.47123C6.8977 2.62328 6.12175 2.00006 5.20024 2.00006Z" fill="#1B6EAA"/></svg>',
				"webproject":'<svg width="17" height="16" viewBox="0 0 17 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M0.999976 1.5001C0.999976 1.16873 1.2686 0.900098 1.59998 0.900098H5.09998C6.39989 0.900098 7.47699 1.85406 7.6694 3.1001H0.999976V1.5001ZM8.48692 3.2001C8.33505 1.46284 6.87666 0.100098 5.09998 0.100098H1.59998C0.826777 0.100098 0.199976 0.726899 0.199976 1.5001V3.2001H0.199951V4.0001V14.0001C0.199951 14.9942 1.00584 15.8001 1.99995 15.8001H12L12.023 15.8L12.0228 15.8001H13H13.6191C14.324 15.8001 14.919 15.2759 15.0079 14.5766L16.1106 5.90105C16.2928 4.467 15.1753 3.19844 13.7297 3.19844H12.2004C12.1259 3.19409 12.0503 3.19429 11.9738 3.1993L11.9739 3.2001H8.48692ZM13.5954 3.99844C13.7875 4.28513 13.9 4.63036 13.9 5.00331V14.9084V14.9303C14.0675 14.8414 14.1889 14.6752 14.2143 14.4757L15.3169 5.80018C15.4385 4.84415 14.6934 3.99844 13.7297 3.99844H13.5954ZM1.79995 4.0001H0.999951V4.8001V14.0001C0.999951 14.5524 1.44767 15.0001 1.99995 15.0001H12C12.5522 15.0001 13 14.5524 13 14.0001V5.0001C13 4.44782 12.5522 4.0001 12 4.0001H1.79995Z" fill="#000"/></svg>',
				"digitaltwins":'<svg width="14" height="13" viewBox="0 0 14 13" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M8.79095 0.485504L6.14418 4.89677C5.74427 5.5633 6.22438 6.41127 7.00168 6.41127H12.2952C13.0725 6.41127 13.5526 5.5633 13.1527 4.89677L10.5059 0.485505C10.1175 -0.161832 9.17935 -0.161838 8.79095 0.485504ZM7.00168 5.61127C6.84622 5.61127 6.75019 5.44168 6.83018 5.30837L7.00168 5.02254L7.12147 4.82288L7.12844 4.81127L9.18196 1.38873L9.18575 1.38242L9.284 1.21866L9.47694 0.897101C9.55462 0.767633 9.74226 0.767633 9.81994 0.897101L10.0129 1.21866L10.1111 1.38242L10.1149 1.38873L12.1684 4.81127L12.1754 4.82288L12.2952 5.02254L12.4667 5.30837C12.5467 5.44168 12.4507 5.61127 12.2952 5.61127H11.9619H11.729H11.7155H7.58139H7.56785H7.33501H7.00168ZM2.79081 6.4855L0.144051 10.8968C-0.255862 11.5633 0.224247 12.4113 1.00154 12.4113H6.29507C7.07236 12.4113 7.55248 11.5633 7.15256 10.8968L4.5058 6.4855C4.1174 5.83817 3.17922 5.83816 2.79081 6.4855ZM1.00154 11.6113C0.846085 11.6113 0.750063 11.4417 0.830046 11.3084L1.00154 11.0225L1.12134 10.8229L1.12831 10.8113L3.18183 7.38873L3.18562 7.38242L3.28387 7.21866L3.47681 6.8971C3.55449 6.76763 3.74212 6.76763 3.8198 6.8971L4.01274 7.21866L4.111 7.38242L4.11478 7.38873L6.16831 10.8113L6.17527 10.8229L6.29507 11.0225L6.46657 11.3084C6.54655 11.4417 6.45053 11.6113 6.29507 11.6113H5.96174H5.72889H5.71535H1.58126H1.56772H1.33488H1.00154Z" fill="#1B6EAA"/></svg>',
				"digitaltwin":'<svg width="14" height="13" viewBox="0 0 14 13" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M8.79095 0.485504L6.14418 4.89677C5.74427 5.5633 6.22438 6.41127 7.00168 6.41127H12.2952C13.0725 6.41127 13.5526 5.5633 13.1527 4.89677L10.5059 0.485505C10.1175 -0.161832 9.17935 -0.161838 8.79095 0.485504ZM7.00168 5.61127C6.84622 5.61127 6.75019 5.44168 6.83018 5.30837L7.00168 5.02254L7.12147 4.82288L7.12844 4.81127L9.18196 1.38873L9.18575 1.38242L9.284 1.21866L9.47694 0.897101C9.55462 0.767633 9.74226 0.767633 9.81994 0.897101L10.0129 1.21866L10.1111 1.38242L10.1149 1.38873L12.1684 4.81127L12.1754 4.82288L12.2952 5.02254L12.4667 5.30837C12.5467 5.44168 12.4507 5.61127 12.2952 5.61127H11.9619H11.729H11.7155H7.58139H7.56785H7.33501H7.00168ZM2.79081 6.4855L0.144051 10.8968C-0.255862 11.5633 0.224247 12.4113 1.00154 12.4113H6.29507C7.07236 12.4113 7.55248 11.5633 7.15256 10.8968L4.5058 6.4855C4.1174 5.83817 3.17922 5.83816 2.79081 6.4855ZM1.00154 11.6113C0.846085 11.6113 0.750063 11.4417 0.830046 11.3084L1.00154 11.0225L1.12134 10.8229L1.12831 10.8113L3.18183 7.38873L3.18562 7.38242L3.28387 7.21866L3.47681 6.8971C3.55449 6.76763 3.74212 6.76763 3.8198 6.8971L4.01274 7.21866L4.111 7.38242L4.11478 7.38873L6.16831 10.8113L6.17527 10.8229L6.29507 11.0225L6.46657 11.3084C6.54655 11.4417 6.45053 11.6113 6.29507 11.6113H5.96174H5.72889H5.71535H1.58126H1.56772H1.33488H1.00154Z" fill="#000"/></svg>',
				"licensing":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M12.349 1.6H3.65121C2.93799 1.6 2.67936 1.67426 2.41862 1.8137C2.15788 1.95315 1.95325 2.15778 1.8138 2.41852C1.67436 2.67926 1.6001 2.93789 1.6001 3.65111V12.3489C1.6001 13.0621 1.67436 13.3207 1.8138 13.5815C1.95325 13.8422 2.15788 14.0468 2.41862 14.1863C2.67936 14.3257 2.93799 14.4 3.65121 14.4H12.349C13.0622 14.4 13.3208 14.3257 13.5816 14.1863C13.8423 14.0468 14.0469 13.8422 14.1864 13.5815C14.3258 13.3207 14.4001 13.0621 14.4001 12.3489V3.65111C14.4001 2.93789 14.3258 2.67926 14.1864 2.41852C14.0469 2.15778 13.8423 1.95315 13.5816 1.8137C13.3208 1.67426 13.0622 1.6 12.349 1.6ZM2.7958 2.51906C2.96038 2.43104 3.12163 2.3999 3.65111 2.3999H12.3489C12.8784 2.3999 13.0396 2.43104 13.2042 2.51906C13.3255 2.58394 13.416 2.67438 13.4808 2.7957C13.5689 2.96029 13.6 3.12153 13.6 3.65101V12.3488C13.6 12.8783 13.5689 13.0395 13.4808 13.2041C13.416 13.3254 13.3255 13.4159 13.2042 13.4807C13.0396 13.5688 12.8784 13.5999 12.3489 13.5999H3.65111C3.12163 13.5999 2.96038 13.5688 2.7958 13.4807C2.67448 13.4159 2.58404 13.3254 2.51916 13.2041C2.43114 13.0395 2.4 12.8783 2.4 12.3488V3.65101C2.4 3.12153 2.43114 2.96029 2.51916 2.7957C2.58404 2.67438 2.67448 2.58394 2.7958 2.51906ZM4 9.9999C4 9.77899 4.17909 9.5999 4.4 9.5999H11.6C11.8209 9.5999 12 9.77899 12 9.9999C12 10.2208 11.8209 10.3999 11.6 10.3999H4.4C4.17909 10.3999 4 10.2208 4 9.9999ZM4.4 5.5999C4.17909 5.5999 4 5.77899 4 5.9999C4 6.22081 4.17909 6.3999 4.4 6.3999H11.6C11.8209 6.3999 12 6.22081 12 5.9999C12 5.77899 11.8209 5.5999 11.6 5.5999H4.4Z" fill="#1168A6"/></svg>',
				"user":'<svg width="24" height="24" viewBox="0 0 12 15" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M6.24923 7.47906C5.37645 6.90757 4.7999 5.92111 4.7999 4.79998C4.7999 3.03266 6.23259 1.59998 7.9999 1.59998C9.76721 1.59998 11.1999 3.03266 11.1999 4.79998C11.1999 5.92111 10.6234 6.90757 9.75057 7.47906C11.986 8.21412 13.5999 10.3186 13.5999 12.8C13.5999 15.2 2.3999 15.2 2.3999 12.8C2.3999 10.3186 4.01383 8.21412 6.24923 7.47906ZM12.7999 12.7999C12.7999 10.1489 10.6508 7.99988 7.99988 7.99988C5.34891 7.99988 3.19988 10.1489 3.19988 12.7999C3.19988 13.1905 5.3326 13.7999 7.99988 13.7999C10.6672 13.7999 12.7999 13.1905 12.7999 12.7999ZM7.99988 7.19988C9.32536 7.19988 10.3999 6.12536 10.3999 4.79988C10.3999 3.47439 9.32536 2.39988 7.99988 2.39988C6.67439 2.39988 5.59988 3.47439 5.59988 4.79988C5.59988 6.12536 6.67439 7.19988 7.99988 7.19988Z" fill="#1B6EAA"/></svg>',
				"users":'<svg width="19" height="15" viewBox="0 0 19 15" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M10.8492 5.87909C9.97645 5.3076 9.3999 4.32113 9.3999 3.2C9.3999 1.43269 10.8326 0 12.5999 0C14.3672 0 15.7999 1.43269 15.7999 3.2C15.7999 4.32113 15.2234 5.3076 14.3506 5.87909C16.586 6.61414 18.1999 8.71859 18.1999 11.2C18.1999 12.6027 14.374 13.1856 11.1945 12.9486C11.1981 13.0319 11.2 13.1157 11.2 13.1999C11.2 15.5999 0 15.5999 0 13.1999C0 10.7185 1.61393 8.61404 3.84933 7.87899C2.97655 7.3075 2.4 6.32103 2.4 5.1999C2.4 3.43259 3.83269 1.9999 5.6 1.9999C7.36731 1.9999 8.8 3.43259 8.8 5.1999C8.8 6.32103 8.22345 7.3075 7.35067 7.87899C7.56685 7.95007 7.77722 8.03397 7.98088 8.12977L7.97072 8.04774C8.65849 7.03968 9.66694 6.26785 10.8492 5.87909ZM8.63543 8.49313C9.89403 9.30649 10.8056 10.6109 11.0989 12.1354C11.5704 12.1763 12.0749 12.1999 12.6 12.1999C15.2673 12.1999 17.4 11.5906 17.4 11.1999C17.4 8.54894 15.251 6.3999 12.6 6.3999C10.9531 6.3999 9.49998 7.22928 8.63543 8.49313ZM12.6 5.5999C13.9255 5.5999 15 4.52539 15 3.1999C15 1.87442 13.9255 0.799902 12.6 0.799902C11.2745 0.799902 10.2 1.87442 10.2 3.1999C10.2 4.52539 11.2745 5.5999 12.6 5.5999ZM5.59998 8.39981C8.25094 8.39981 10.4 10.5488 10.4 13.1998C10.4 13.5905 8.26725 14.1998 5.59998 14.1998C2.9327 14.1998 0.799976 13.5905 0.799976 13.1998C0.799976 10.5488 2.94901 8.39981 5.59998 8.39981ZM7.99998 5.1998C7.99998 6.52529 6.92546 7.5998 5.59998 7.5998C4.27449 7.5998 3.19998 6.52529 3.19998 5.1998C3.19998 3.87432 4.27449 2.7998 5.59998 2.7998C6.92546 2.7998 7.99998 3.87432 7.99998 5.1998Z" fill="#1168A6"/></svg>',
				"visualizations":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M0 0.4C0 0.179086 0.179086 0 0.4 0C0.620914 0 0.8 0.179086 0.8 0.4V3.2002H2.6C2.82091 3.2002 3 3.37928 3 3.6002C3 3.82111 2.82091 4.0002 2.6 4.0002H0.8V7.2002H2.6C2.82091 7.2002 3 7.37928 3 7.6002C3 7.82111 2.82091 8.0002 2.6 8.0002H0.8V11.2002H2.6C2.82091 11.2002 3 11.3793 3 11.6002C3 11.8211 2.82091 12.0002 2.6 12.0002H0.8V15.2002H4V9.2C4 9.08954 4.08954 9 4.2 9H7.8C7.91046 9 8 9.08954 8 9.2V15.2002H10V5.2C10 5.08954 10.0895 5 10.2 5H13.8C13.9105 5 14 5.08954 14 5.2V15.2002H15.6C15.8209 15.2002 16 15.3793 16 15.6002C16 15.8211 15.8209 16.0002 15.6 16.0002H0.4C0.179086 16.0002 0 15.8211 0 15.6002V15.6V11.6002V7.6002V3.6002V0.4ZM4.8 9.8V15.2H7.2V9.8H4.8ZM10.8 15.2V5.8H13.2V15.2H10.8Z" fill="#1B6EAA"/></svg>',
				"analytics":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M0 0.4C0 0.179086 0.179086 0 0.4 0C0.620914 0 0.8 0.179086 0.8 0.4V3.2002H2.6C2.82091 3.2002 3 3.37928 3 3.6002C3 3.82111 2.82091 4.0002 2.6 4.0002H0.8V7.2002H2.6C2.82091 7.2002 3 7.37928 3 7.6002C3 7.82111 2.82091 8.0002 2.6 8.0002H0.8V11.2002H2.6C2.82091 11.2002 3 11.3793 3 11.6002C3 11.8211 2.82091 12.0002 2.6 12.0002H0.8V15.2002H4V9.2C4 9.08954 4.08954 9 4.2 9H7.8C7.91046 9 8 9.08954 8 9.2V15.2002H10V5.2C10 5.08954 10.0895 5 10.2 5H13.8C13.9105 5 14 5.08954 14 5.2V15.2002H15.6C15.8209 15.2002 16 15.3793 16 15.6002C16 15.8211 15.8209 16.0002 15.6 16.0002H0.4C0.179086 16.0002 0 15.8211 0 15.6002V15.6V11.6002V7.6002V3.6002V0.4ZM4.8 9.8V15.2H7.2V9.8H4.8ZM10.8 15.2V5.8H13.2V15.2H10.8Z" fill="#1B6EAA"/></svg>',
				"clientplatform":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M8.92772 0.930516C8.96519 0.921761 9.07629 0.910693 9.26987 0.919288C9.45393 0.92746 9.68831 0.952193 9.95835 0.999657C10.4988 1.09465 11.1673 1.27807 11.8457 1.58907C13.2022 2.21096 14.5657 3.32499 15.0778 5.23936L15.8506 5.03264C15.2581 2.81761 13.6761 1.54812 12.1791 0.861849C11.4307 0.518778 10.6955 0.316959 10.0968 0.211735C9.79733 0.159092 9.52832 0.129974 9.30536 0.120075C9.09191 0.110599 8.89234 0.117239 8.74572 0.151494L8.92772 0.930516ZM12.9184 5.37745C11.6877 3.46063 10.0576 3.35139 9.47997 3.48634L9.29796 2.70732C10.1931 2.49819 12.1713 2.73304 13.5916 4.94524L12.9184 5.37745ZM1.6 5.28518H10.5504C11.0806 5.28518 11.5104 5.71499 11.5104 6.24518V14.4C11.5104 14.9302 11.0806 15.36 10.5504 15.36H1.6C1.06981 15.36 0.64 14.9302 0.64 14.4V6.24518C0.64 5.71499 1.06981 5.28518 1.6 5.28518ZM0 6.24518C0 5.36153 0.716345 4.64518 1.6 4.64518H10.5504C11.4341 4.64518 12.1504 5.36152 12.1504 6.24518V14.4C12.1504 15.2836 11.4341 16 10.5504 16H1.6C0.716343 16 0 15.2836 0 14.4V6.24518ZM2.72531 12.9032C2.44026 12.9032 2.20918 13.1343 2.20918 13.4194C2.20918 13.7044 2.44026 13.9355 2.72531 13.9355H9.42515C9.7102 13.9355 9.94128 13.7044 9.94128 13.4194C9.94128 13.1343 9.7102 12.9032 9.42515 12.9032H2.72531Z" fill="#000"/></svg>',
				"deviceandsystems":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M8.92772 0.930516C8.96519 0.921761 9.07629 0.910693 9.26987 0.919288C9.45393 0.92746 9.68831 0.952193 9.95835 0.999657C10.4988 1.09465 11.1673 1.27807 11.8457 1.58907C13.2022 2.21096 14.5657 3.32499 15.0778 5.23936L15.8506 5.03264C15.2581 2.81761 13.6761 1.54812 12.1791 0.861849C11.4307 0.518778 10.6955 0.316959 10.0968 0.211735C9.79733 0.159092 9.52832 0.129974 9.30536 0.120075C9.09191 0.110599 8.89234 0.117239 8.74572 0.151494L8.92772 0.930516ZM12.9184 5.37745C11.6877 3.46063 10.0576 3.35139 9.47997 3.48634L9.29796 2.70732C10.1931 2.49819 12.1713 2.73304 13.5916 4.94524L12.9184 5.37745ZM1.6 5.28518H10.5504C11.0806 5.28518 11.5104 5.71499 11.5104 6.24518V14.4C11.5104 14.9302 11.0806 15.36 10.5504 15.36H1.6C1.06981 15.36 0.64 14.9302 0.64 14.4V6.24518C0.64 5.71499 1.06981 5.28518 1.6 5.28518ZM0 6.24518C0 5.36153 0.716345 4.64518 1.6 4.64518H10.5504C11.4341 4.64518 12.1504 5.36152 12.1504 6.24518V14.4C12.1504 15.2836 11.4341 16 10.5504 16H1.6C0.716343 16 0 15.2836 0 14.4V6.24518ZM2.72531 12.9032C2.44026 12.9032 2.20918 13.1343 2.20918 13.4194C2.20918 13.7044 2.44026 13.9355 2.72531 13.9355H9.42515C9.7102 13.9355 9.94128 13.7044 9.94128 13.4194C9.94128 13.1343 9.7102 12.9032 9.42515 12.9032H2.72531Z" fill="#1B6EAA"/></svg>',
				"notebooks":'<svg width="13" height="17" viewBox="0 0 13 17" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M1.79999 -1.2219e-05H10.8C11.7941 -1.2219e-05 12.6 0.805876 12.6 1.79999V14.8C12.6 15.7941 11.7941 16.6 10.8 16.6H1.79999C0.805875 16.6 -1.2219e-05 15.7941 -1.2219e-05 14.8V1.79999C-1.2219e-05 0.805875 0.805875 -1.2219e-05 1.79999 -1.2219e-05ZM1.79999 0.799988C1.2477 0.799988 0.799988 1.2477 0.799988 1.79999V14.8C0.799988 15.3523 1.2477 15.8 1.79999 15.8H10.8C11.3523 15.8 11.8 15.3523 11.8 14.8V1.79999C11.8 1.2477 11.3523 0.799988 10.8 0.799988H1.79999ZM2.79999 4.19999C2.79999 3.97907 2.97907 3.79999 3.19999 3.79999H6.39999C6.6209 3.79999 6.79999 3.97907 6.79999 4.19999C6.79999 4.4209 6.6209 4.59999 6.39999 4.59999H3.19999C2.97907 4.59999 2.79999 4.4209 2.79999 4.19999ZM3.19999 6.5C2.97907 6.5 2.79999 6.67909 2.79999 6.9C2.79999 7.12091 2.97907 7.3 3.19999 7.3H9.39999C9.6209 7.3 9.79999 7.12091 9.79999 6.9C9.79999 6.67909 9.6209 6.5 9.39999 6.5H3.19999ZM2.79999 9.60001C2.79999 9.3791 2.97907 9.20001 3.19999 9.20001H9.39999C9.6209 9.20001 9.79999 9.3791 9.79999 9.60001C9.79999 9.82093 9.6209 10 9.39999 10H3.19999C2.97907 10 2.79999 9.82093 2.79999 9.60001ZM3.19999 11.9C2.97907 11.9 2.79999 12.0791 2.79999 12.3C2.79999 12.5209 2.97907 12.7 3.19999 12.7H9.39999C9.6209 12.7 9.79999 12.5209 9.79999 12.3C9.79999 12.0791 9.6209 11.9 9.39999 11.9H3.19999Z" fill="#1168A6"/></svg>',
				"notebook":'<svg width="13" height="17" viewBox="0 0 13 17" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M1.79999 -1.2219e-05H10.8C11.7941 -1.2219e-05 12.6 0.805876 12.6 1.79999V14.8C12.6 15.7941 11.7941 16.6 10.8 16.6H1.79999C0.805875 16.6 -1.2219e-05 15.7941 -1.2219e-05 14.8V1.79999C-1.2219e-05 0.805875 0.805875 -1.2219e-05 1.79999 -1.2219e-05ZM1.79999 0.799988C1.2477 0.799988 0.799988 1.2477 0.799988 1.79999V14.8C0.799988 15.3523 1.2477 15.8 1.79999 15.8H10.8C11.3523 15.8 11.8 15.3523 11.8 14.8V1.79999C11.8 1.2477 11.3523 0.799988 10.8 0.799988H1.79999ZM2.79999 4.19999C2.79999 3.97907 2.97907 3.79999 3.19999 3.79999H6.39999C6.6209 3.79999 6.79999 3.97907 6.79999 4.19999C6.79999 4.4209 6.6209 4.59999 6.39999 4.59999H3.19999C2.97907 4.59999 2.79999 4.4209 2.79999 4.19999ZM3.19999 6.5C2.97907 6.5 2.79999 6.67909 2.79999 6.9C2.79999 7.12091 2.97907 7.3 3.19999 7.3H9.39999C9.6209 7.3 9.79999 7.12091 9.79999 6.9C9.79999 6.67909 9.6209 6.5 9.39999 6.5H3.19999ZM2.79999 9.60001C2.79999 9.3791 2.97907 9.20001 3.19999 9.20001H9.39999C9.6209 9.20001 9.79999 9.3791 9.79999 9.60001C9.79999 9.82093 9.6209 10 9.39999 10H3.19999C2.97907 10 2.79999 9.82093 2.79999 9.60001ZM3.19999 11.9C2.97907 11.9 2.79999 12.0791 2.79999 12.3C2.79999 12.5209 2.97907 12.7 3.19999 12.7H9.39999C9.6209 12.7 9.79999 12.5209 9.79999 12.3C9.79999 12.0791 9.6209 11.9 9.39999 11.9H3.19999Z" fill="#000"/></svg>',
				"dashboards":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M2.05111 0H13.9489C14.6621 0 14.9207 0.0742605 15.1815 0.213706C15.4422 0.353152 15.6468 0.557783 15.7863 0.818523C15.9257 1.07926 16 1.33789 16 2.05111V13.9489C16 14.6621 15.9257 14.9207 15.7863 15.1815C15.6468 15.4422 15.4422 15.6468 15.1815 15.7863C14.9207 15.9257 14.6621 16 13.9489 16H2.05111C1.33789 16 1.07926 15.9257 0.818523 15.7863C0.557783 15.6468 0.353152 15.4422 0.213706 15.1815C0.0742605 14.9207 0 14.6621 0 13.9489V2.05111C0 1.33789 0.0742605 1.07926 0.213706 0.818523C0.353152 0.557783 0.557783 0.353152 0.818523 0.213706C1.07926 0.0742605 1.33789 0 2.05111 0ZM2.05111 0.8C1.52163 0.8 1.36038 0.831137 1.1958 0.919157C1.07448 0.984043 0.984043 1.07448 0.919157 1.1958C0.831137 1.36038 0.8 1.52163 0.8 2.05111V8H7.2V0.8H2.05111ZM8 0.8V7.2H15.2L15.2 2.05111C15.2 1.52163 15.1689 1.36038 15.0808 1.1958C15.016 1.07448 14.9255 0.984043 14.8042 0.919157C14.6396 0.831137 14.4784 0.8 13.9489 0.8H8ZM8 8V15.2L13.9489 15.2C14.4784 15.2 14.6396 15.1689 14.8042 15.0808C14.9255 15.016 15.016 14.9255 15.0808 14.8042C15.1689 14.6396 15.2 14.4784 15.2 13.9489L15.2 8H8ZM7.2 15.2V8.8H0.8V13.9489C0.8 14.4784 0.831137 14.6396 0.919157 14.8042C0.984043 14.9255 1.07448 15.016 1.1958 15.0808C1.36038 15.1689 1.52163 15.2 2.05111 15.2L7.2 15.2Z" fill="#1168A6"/></svg>',
				"dashboard":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M2.05111 0H13.9489C14.6621 0 14.9207 0.0742605 15.1815 0.213706C15.4422 0.353152 15.6468 0.557783 15.7863 0.818523C15.9257 1.07926 16 1.33789 16 2.05111V13.9489C16 14.6621 15.9257 14.9207 15.7863 15.1815C15.6468 15.4422 15.4422 15.6468 15.1815 15.7863C14.9207 15.9257 14.6621 16 13.9489 16H2.05111C1.33789 16 1.07926 15.9257 0.818523 15.7863C0.557783 15.6468 0.353152 15.4422 0.213706 15.1815C0.0742605 14.9207 0 14.6621 0 13.9489V2.05111C0 1.33789 0.0742605 1.07926 0.213706 0.818523C0.353152 0.557783 0.557783 0.353152 0.818523 0.213706C1.07926 0.0742605 1.33789 0 2.05111 0ZM2.05111 0.8C1.52163 0.8 1.36038 0.831137 1.1958 0.919157C1.07448 0.984043 0.984043 1.07448 0.919157 1.1958C0.831137 1.36038 0.8 1.52163 0.8 2.05111V8H7.2V0.8H2.05111ZM8 0.8V7.2H15.2L15.2 2.05111C15.2 1.52163 15.1689 1.36038 15.0808 1.1958C15.016 1.07448 14.9255 0.984043 14.8042 0.919157C14.6396 0.831137 14.4784 0.8 13.9489 0.8H8ZM8 8V15.2L13.9489 15.2C14.4784 15.2 14.6396 15.1689 14.8042 15.0808C14.9255 15.016 15.016 14.9255 15.0808 14.8042C15.1689 14.6396 15.2 14.4784 15.2 13.9489L15.2 8H8ZM7.2 15.2V8.8H0.8V13.9489C0.8 14.4784 0.831137 14.6396 0.919157 14.8042C0.984043 14.9255 1.07448 15.016 1.1958 15.0808C1.36038 15.1689 1.52163 15.2 2.05111 15.2L7.2 15.2Z" fill="#000"/></svg>',
				"scripts":'<svg width="16" height="14" viewBox="0 0 16 14" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M8.68553 3.14948C8.73707 3.02573 8.87917 2.96718 9.00292 3.01872C9.12668 3.07025 9.18522 3.21235 9.13369 3.3361L7.25378 7.85052C7.20225 7.97427 7.06015 8.03282 6.9364 7.98129C6.81264 7.92975 6.7541 7.78765 6.80563 7.6639L8.68553 3.14948ZM5.33558 7.08756C5.43698 7.17525 5.44809 7.32854 5.3604 7.42994C5.27271 7.53133 5.11942 7.54244 5.01803 7.45475L3.08396 5.78212C3.00353 5.71256 2.97991 5.60175 3.01733 5.50829C3.02487 5.45897 3.04764 5.41153 3.08562 5.37355L4.8937 3.56546C4.9885 3.47067 5.14218 3.47067 5.23697 3.56546C5.33177 3.66025 5.33177 3.81394 5.23697 3.90873L3.57808 5.56763L5.33558 7.08756ZM10.1517 3.88093C10.0503 3.79324 10.0392 3.63996 10.1269 3.53856C10.2146 3.43716 10.3678 3.42605 10.4692 3.51374L12.4033 5.18638C12.4837 5.25593 12.5074 5.36675 12.4699 5.46021C12.4624 5.50953 12.4396 5.55697 12.4016 5.59495L10.5936 7.40303C10.4988 7.49782 10.3451 7.49782 10.2503 7.40303C10.1555 7.30824 10.1555 7.15455 10.2503 7.05976L11.9092 5.40087L10.1517 3.88093Z" fill="#1B6EAA"/><path fill-rule="evenodd" clip-rule="evenodd" d="M1.8 0H13.8C14.7941 0 15.6 0.805887 15.6 1.8V8.8C15.6 9.79411 14.7941 10.6 13.8 10.6H8.8V12.8H10.4C10.6209 12.8 10.8 12.9791 10.8 13.2C10.8 13.4209 10.6209 13.6 10.4 13.6H6.2C5.97909 13.6 5.8 13.4209 5.8 13.2C5.8 12.9791 5.97909 12.8 6.2 12.8H7.8V10.6H1.8C0.805888 10.6 0 9.79411 0 8.8V1.8C0 0.805887 0.805888 0 1.8 0ZM7.8 9.8H8.8H13.8C14.3523 9.8 14.8 9.35229 14.8 8.8V1.8C14.8 1.24772 14.3523 0.8 13.8 0.8H1.8C1.24772 0.8 0.8 1.24772 0.8 1.8V8.8C0.8 9.35229 1.24772 9.8 1.8 9.8H7.8Z" fill="#1B6EAA"/></svg>',
				"script":'<svg width="16" height="14" viewBox="0 0 16 14" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M8.68553 3.14948C8.73707 3.02573 8.87917 2.96718 9.00292 3.01872C9.12668 3.07025 9.18522 3.21235 9.13369 3.3361L7.25378 7.85052C7.20225 7.97427 7.06015 8.03282 6.9364 7.98129C6.81264 7.92975 6.7541 7.78765 6.80563 7.6639L8.68553 3.14948ZM5.33558 7.08756C5.43698 7.17525 5.44809 7.32854 5.3604 7.42994C5.27271 7.53133 5.11942 7.54244 5.01803 7.45475L3.08396 5.78212C3.00353 5.71256 2.97991 5.60175 3.01733 5.50829C3.02487 5.45897 3.04764 5.41153 3.08562 5.37355L4.8937 3.56546C4.9885 3.47067 5.14218 3.47067 5.23697 3.56546C5.33177 3.66025 5.33177 3.81394 5.23697 3.90873L3.57808 5.56763L5.33558 7.08756ZM10.1517 3.88093C10.0503 3.79324 10.0392 3.63996 10.1269 3.53856C10.2146 3.43716 10.3678 3.42605 10.4692 3.51374L12.4033 5.18638C12.4837 5.25593 12.5074 5.36675 12.4699 5.46021C12.4624 5.50953 12.4396 5.55697 12.4016 5.59495L10.5936 7.40303C10.4988 7.49782 10.3451 7.49782 10.2503 7.40303C10.1555 7.30824 10.1555 7.15455 10.2503 7.05976L11.9092 5.40087L10.1517 3.88093Z" fill="#1B6EAA"/><path fill-rule="evenodd" clip-rule="evenodd" d="M1.8 0H13.8C14.7941 0 15.6 0.805887 15.6 1.8V8.8C15.6 9.79411 14.7941 10.6 13.8 10.6H8.8V12.8H10.4C10.6209 12.8 10.8 12.9791 10.8 13.2C10.8 13.4209 10.6209 13.6 10.4 13.6H6.2C5.97909 13.6 5.8 13.4209 5.8 13.2C5.8 12.9791 5.97909 12.8 6.2 12.8H7.8V10.6H1.8C0.805888 10.6 0 9.79411 0 8.8V1.8C0 0.805887 0.805888 0 1.8 0ZM7.8 9.8H8.8H13.8C14.3523 9.8 14.8 9.35229 14.8 8.8V1.8C14.8 1.24772 14.3523 0.8 13.8 0.8H1.8C1.24772 0.8 0.8 1.24772 0.8 1.8V8.8C0.8 9.35229 1.24772 9.8 1.8 9.8H7.8Z" fill="#000"/></svg>',
				"pipelines":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M5.8 0.8V2.2H10.2V0.8H5.8ZM5.5 0C5.22386 0 5 0.223858 5 0.5V2.5C5 2.77614 5.22386 3 5.5 3H6L6 11H3V10.5C3 10.2239 2.77614 10 2.5 10H0.5C0.223857 10 0 10.2239 0 10.5V15.5C0 15.7761 0.223858 16 0.5 16H2.5C2.77614 16 3 15.7761 3 15.5V15H13V15.5C13 15.7761 13.2239 16 13.5 16H15.5C15.7761 16 16 15.7761 16 15.5V10.5C16 10.2239 15.7761 10 15.5 10H13.5C13.2239 10 13 10.2239 13 10.5V11H10L10 3H10.5C10.7761 3 11 2.77614 11 2.5V0.5C11 0.223858 10.7761 0 10.5 0H5.5ZM13.8 15.2H15.2V10.8H13.8V15.2ZM2.2 15.2H0.8L0.8 10.8H2.2L2.2 15.2ZM3.00117 11.8V14.2H12.9723V11.8H3.00117ZM9.2 10.992H6.8V3.0109L9.2 3.0109V10.992Z" fill="#1B6EAA"/></svg>',
				"pipeline":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M5.8 0.8V2.2H10.2V0.8H5.8ZM5.5 0C5.22386 0 5 0.223858 5 0.5V2.5C5 2.77614 5.22386 3 5.5 3H6L6 11H3V10.5C3 10.2239 2.77614 10 2.5 10H0.5C0.223857 10 0 10.2239 0 10.5V15.5C0 15.7761 0.223858 16 0.5 16H2.5C2.77614 16 3 15.7761 3 15.5V15H13V15.5C13 15.7761 13.2239 16 13.5 16H15.5C15.7761 16 16 15.7761 16 15.5V10.5C16 10.2239 15.7761 10 15.5 10H13.5C13.2239 10 13 10.2239 13 10.5V11H10L10 3H10.5C10.7761 3 11 2.77614 11 2.5V0.5C11 0.223858 10.7761 0 10.5 0H5.5ZM13.8 15.2H15.2V10.8H13.8V15.2ZM2.2 15.2H0.8L0.8 10.8H2.2L2.2 15.2ZM3.00117 11.8V14.2H12.9723V11.8H3.00117ZM9.2 10.992H6.8V3.0109L9.2 3.0109V10.992Z" fill="#000"/></svg>',
				"project":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M6 0.8H10C10.1105 0.8 10.2 0.889543 10.2 1V2C10.2 2.11046 10.1105 2.2 10 2.2H6C5.88954 2.2 5.8 2.11046 5.8 2V1C5.8 0.889543 5.88954 0.8 6 0.8ZM5 1C5 0.447715 5.44772 0 6 0H10C10.5523 0 11 0.447715 11 1V2C11 2.06856 10.9931 2.13551 10.98 2.2002H12H13H14C15.1046 2.2002 16 3.09563 16 4.2002V10.2002C16 11.3048 15.1046 12.2002 14 12.2002H13H12H4H3H2C0.895431 12.2002 0 11.3048 0 10.2002V4.2002C0 3.09563 0.895431 2.2002 2 2.2002H3H4H5.02004C5.0069 2.13551 5 2.06856 5 2V1ZM3 3.0002H2C1.33726 3.0002 0.8 3.53745 0.8 4.2002V10.2002C0.8 10.8629 1.33726 11.4002 2 11.4002H3V3.0002ZM4 11.4002V3.0002H12V11.4002H4ZM14 11.4002H13V3.0002H14C14.6627 3.0002 15.2 3.53745 15.2 4.2002V10.2002C15.2 10.8629 14.6627 11.4002 14 11.4002Z" fill="#1B6EAA"/></svg>',
				"gadgets":'<svg width="15" height="15" viewBox="0 0 15 15" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M5 0.8H2C1.33726 0.8 0.8 1.33726 0.8 2V5C0.8 5.66274 1.33726 6.2 2 6.2H5C5.66274 6.2 6.2 5.66274 6.2 5V2C6.2 1.33726 5.66274 0.8 5 0.8ZM2 0C0.895431 0 0 0.89543 0 2V5C0 6.10457 0.89543 7 2 7H5C6.10457 7 7 6.10457 7 5V2C7 0.895431 6.10457 0 5 0H2ZM13 0.8H10C9.33726 0.8 8.8 1.33726 8.8 2V5C8.8 5.66274 9.33726 6.2 10 6.2H13C13.6627 6.2 14.2 5.66274 14.2 5V2C14.2 1.33726 13.6627 0.8 13 0.8ZM10 0C8.89543 0 8 0.89543 8 2V5C8 6.10457 8.89543 7 10 7H13C14.1046 7 15 6.10457 15 5V2C15 0.895431 14.1046 0 13 0H10ZM2 8.8H5C5.66274 8.8 6.2 9.33726 6.2 10V13C6.2 13.6627 5.66274 14.2 5 14.2H2C1.33726 14.2 0.8 13.6627 0.8 13V10C0.8 9.33726 1.33726 8.8 2 8.8ZM0 10C0 8.89543 0.895431 8 2 8H5C6.10457 8 7 8.89543 7 10V13C7 14.1046 6.10457 15 5 15H2C0.89543 15 0 14.1046 0 13V10ZM11 11V8H12V11H15V12H12V15H11V12H8V11H11Z" fill="#1B6EAA"/></svg>',
				"gadget":'<svg width="15" height="15" viewBox="0 0 15 15" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M5 0.8H2C1.33726 0.8 0.8 1.33726 0.8 2V5C0.8 5.66274 1.33726 6.2 2 6.2H5C5.66274 6.2 6.2 5.66274 6.2 5V2C6.2 1.33726 5.66274 0.8 5 0.8ZM2 0C0.895431 0 0 0.89543 0 2V5C0 6.10457 0.89543 7 2 7H5C6.10457 7 7 6.10457 7 5V2C7 0.895431 6.10457 0 5 0H2ZM13 0.8H10C9.33726 0.8 8.8 1.33726 8.8 2V5C8.8 5.66274 9.33726 6.2 10 6.2H13C13.6627 6.2 14.2 5.66274 14.2 5V2C14.2 1.33726 13.6627 0.8 13 0.8ZM10 0C8.89543 0 8 0.89543 8 2V5C8 6.10457 8.89543 7 10 7H13C14.1046 7 15 6.10457 15 5V2C15 0.895431 14.1046 0 13 0H10ZM2 8.8H5C5.66274 8.8 6.2 9.33726 6.2 10V13C6.2 13.6627 5.66274 14.2 5 14.2H2C1.33726 14.2 0.8 13.6627 0.8 13V10C0.8 9.33726 1.33726 8.8 2 8.8ZM0 10C0 8.89543 0.895431 8 2 8H5C6.10457 8 7 8.89543 7 10V13C7 14.1046 6.10457 15 5 15H2C0.89543 15 0 14.1046 0 13V10ZM11 11V8H12V11H15V12H12V15H11V12H8V11H11Z" fill="#000"/></svg>',
				"entities":'<svg width="16" height="16" viewBox="-2 0 15 15" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M10.2511 1.31952C10.2515 1.31956 10.2493 1.32266 10.2434 1.32868C10.2477 1.32249 10.2507 1.31948 10.2511 1.31952ZM9.17859 1.21115C9.52626 1.30597 9.79082 1.40633 9.97738 1.5C9.79082 1.59367 9.52626 1.69403 9.17859 1.78885C8.26957 2.03676 6.96869 2.2 5.5 2.2C4.03131 2.2 2.73043 2.03676 1.82141 1.78885C1.47374 1.69403 1.20917 1.59367 1.02261 1.5C1.20917 1.40633 1.47374 1.30597 1.82141 1.21115C2.73043 0.963236 4.03131 0.8 5.5 0.8C6.96869 0.8 8.26957 0.963236 9.17859 1.21115ZM0.748928 1.31952C0.749329 1.31948 0.75228 1.32249 0.756579 1.32868C0.750677 1.32266 0.748528 1.31956 0.748928 1.31952ZM10.1 2.32257C9.11706 2.7306 7.42389 3 5.5 3C3.57611 3 1.88294 2.7306 0.9 2.32257V4.93112C0.939871 4.97252 1.01417 5.03217 1.13207 5.10499C1.36574 5.24932 1.7127 5.41015 2.13453 5.5608C2.97884 5.86234 4.05746 6.1 5 6.1C6.37625 6.1 7.71177 5.85699 8.69745 5.55504C9.19163 5.40366 9.58417 5.24162 9.84485 5.09607C9.97374 5.0241 10.0554 4.96439 10.1 4.9224V2.32257ZM10.1 5.86628C9.78853 6.02442 9.38876 6.17996 8.93177 6.31996C7.87719 6.64301 6.4627 6.9 5 6.9C3.94254 6.9 2.77116 6.63766 1.86547 6.3142C1.50184 6.18433 1.17049 6.04089 0.9 5.89432V8.93112C0.939871 8.97253 1.01417 9.03217 1.13207 9.10499C1.36574 9.24932 1.7127 9.41015 2.13453 9.5608C2.97884 9.86234 4.05746 10.1 5 10.1C6.71405 10.1 8.04548 9.85487 8.93601 9.55803C9.38326 9.40894 9.70621 9.25116 9.90847 9.11415C10.0102 9.0452 10.0687 8.99004 10.0976 8.95471L10.1 8.9518V5.86628ZM0.1 5V9V12.5C0.1 12.7498 0.271877 12.9237 0.369411 13.009C0.489093 13.1138 0.64512 13.2108 0.814066 13.2995C1.15568 13.4788 1.61797 13.6591 2.13069 13.8193C3.15406 14.1391 4.44618 14.4 5.5 14.4C7.21106 14.4 8.5104 14.1447 9.396 13.8119C9.83712 13.6462 10.1884 13.4563 10.4378 13.2589C10.5623 13.1603 10.6713 13.0521 10.752 12.9347C10.8309 12.82 10.9 12.6711 10.9 12.5V9V5V1.78621C10.9656 1.69356 11 1.59787 11 1.5C11 0.671573 8.53757 0 5.5 0C2.46243 0 0 0.671573 0 1.5C0 1.59787 0.0343708 1.69356 0.1 1.7862V5ZM0.9 12.4103V9.89432C1.17049 10.0409 1.50184 10.1843 1.86547 10.3142C2.77116 10.6377 3.94254 10.9 5 10.9C6.78595 10.9 8.20452 10.6451 9.18899 10.317C9.54489 10.1983 9.85101 10.0678 10.1 9.93208V12.4704C10.0982 12.4734 10.0958 12.477 10.0929 12.4813C10.0702 12.5143 10.0239 12.5662 9.9412 12.6317C9.77608 12.7624 9.50503 12.9163 9.11458 13.0631C8.33705 13.3553 7.13639 13.6 5.5 13.6C4.55382 13.6 3.34594 13.3609 2.36931 13.0557C1.88203 12.9034 1.46932 12.7399 1.18593 12.5912C1.04579 12.5176 0.952895 12.4556 0.9 12.4103Z" fill="#1B6EAA"/></svg>',
				"entity":'<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M10.2511 1.31952C10.2515 1.31956 10.2493 1.32266 10.2434 1.32868C10.2477 1.32249 10.2507 1.31948 10.2511 1.31952ZM9.17859 1.21115C9.52626 1.30597 9.79082 1.40633 9.97738 1.5C9.79082 1.59367 9.52626 1.69403 9.17859 1.78885C8.26957 2.03676 6.96869 2.2 5.5 2.2C4.03131 2.2 2.73043 2.03676 1.82141 1.78885C1.47374 1.69403 1.20917 1.59367 1.02261 1.5C1.20917 1.40633 1.47374 1.30597 1.82141 1.21115C2.73043 0.963236 4.03131 0.8 5.5 0.8C6.96869 0.8 8.26957 0.963236 9.17859 1.21115ZM0.748928 1.31952C0.749329 1.31948 0.75228 1.32249 0.756579 1.32868C0.750677 1.32266 0.748528 1.31956 0.748928 1.31952ZM10.1 2.32257C9.11706 2.7306 7.42389 3 5.5 3C3.57611 3 1.88294 2.7306 0.9 2.32257V4.93112C0.939871 4.97252 1.01417 5.03217 1.13207 5.10499C1.36574 5.24932 1.7127 5.41015 2.13453 5.5608C2.97884 5.86234 4.05746 6.1 5 6.1C6.37625 6.1 7.71177 5.85699 8.69745 5.55504C9.19163 5.40366 9.58417 5.24162 9.84485 5.09607C9.97374 5.0241 10.0554 4.96439 10.1 4.9224V2.32257ZM10.1 5.86628C9.78853 6.02442 9.38876 6.17996 8.93177 6.31996C7.87719 6.64301 6.4627 6.9 5 6.9C3.94254 6.9 2.77116 6.63766 1.86547 6.3142C1.50184 6.18433 1.17049 6.04089 0.9 5.89432V8.93112C0.939871 8.97253 1.01417 9.03217 1.13207 9.10499C1.36574 9.24932 1.7127 9.41015 2.13453 9.5608C2.97884 9.86234 4.05746 10.1 5 10.1C6.71405 10.1 8.04548 9.85487 8.93601 9.55803C9.38326 9.40894 9.70621 9.25116 9.90847 9.11415C10.0102 9.0452 10.0687 8.99004 10.0976 8.95471L10.1 8.9518V5.86628ZM0.1 5V9V12.5C0.1 12.7498 0.271877 12.9237 0.369411 13.009C0.489093 13.1138 0.64512 13.2108 0.814066 13.2995C1.15568 13.4788 1.61797 13.6591 2.13069 13.8193C3.15406 14.1391 4.44618 14.4 5.5 14.4C7.21106 14.4 8.5104 14.1447 9.396 13.8119C9.83712 13.6462 10.1884 13.4563 10.4378 13.2589C10.5623 13.1603 10.6713 13.0521 10.752 12.9347C10.8309 12.82 10.9 12.6711 10.9 12.5V9V5V1.78621C10.9656 1.69356 11 1.59787 11 1.5C11 0.671573 8.53757 0 5.5 0C2.46243 0 0 0.671573 0 1.5C0 1.59787 0.0343708 1.69356 0.1 1.7862V5ZM0.9 12.4103V9.89432C1.17049 10.0409 1.50184 10.1843 1.86547 10.3142C2.77116 10.6377 3.94254 10.9 5 10.9C6.78595 10.9 8.20452 10.6451 9.18899 10.317C9.54489 10.1983 9.85101 10.0678 10.1 9.93208V12.4704C10.0982 12.4734 10.0958 12.477 10.0929 12.4813C10.0702 12.5143 10.0239 12.5662 9.9412 12.6317C9.77608 12.7624 9.50503 12.9163 9.11458 13.0631C8.33705 13.3553 7.13639 13.6 5.5 13.6C4.55382 13.6 3.34594 13.3609 2.36931 13.0557C1.88203 12.9034 1.46932 12.7399 1.18593 12.5912C1.04579 12.5176 0.952895 12.4556 0.9 12.4103Z" fill="#000"/></svg>'
				
			};
			let str = group;
			// customization
			if ( !str.endsWith("s") ) {
				if ( group === 'user'){
					// special central node
					groupIconLeft = '30px'; groupFontSize = '15px'; stroke = '#D9E7F1'; groupX = 20; groupY = 20; groupStroke = 10; groupColor = '#000;'; hgroup = 74;  groupWeight = 'bolder'; groupType = '<span style="position: absolute; font-family: Soho,Arial; font-size:12px; color:#000; top:25px; left: 5px; ">'+ group +'</span>'; 
					font = '20px Arial';					
				} else {
					//childrens
					groupIconLeft = '20px'; groupFontSize = '11px'; stroke = '#D9E7F1'; groupX = 8; groupY = 8; groupStroke = 2; groupColor = '#000;'; hgroup = 34; groupType = ''; groupWeight = '400' 
					font = '12px Arial'
				}
			}
			else { 
				// common
				groupIconLeft = '25px'; groupFontSize = '13px'; stroke = '#3982D0'; groupX = 20; groupY = 20; groupStroke = 2; groupColor = '#000;'; hgroup = 64; groupWeight = '400'; groupType = '<span style="position: absolute; font-family: Soho,Arial; font-size:10px; color:#000; top:20px; left: 0px; ">'+ group +'</span>'; 
				font = '18px Arial'
			}
			svg += '<svg xmlns="http://www.w3.org/2000/svg" width="'+ calculateTextWidth(title,font) +'" height="'+ hgroup +'">'+
				'<rect x="0" y="0" width="100%" rx="8" ry="8" height="100%" fill="#FFFFFF"  stroke="'+ stroke +'" stroke-width="'+ groupStroke +'" ></rect>'+
				'<foreignObject x="'+ groupX +'" y="'+ groupY +'" width="100%" height="100%">'+
					'<div xmlns="http://www.w3.org/1999/xhtml">'+
						groupIcon[group] +
						'<span title="'+ group +'" style="position: absolute; font-family: Arial; font-size:'+ groupFontSize +'; color:'+ groupColor +'; top:2px; left: '+ groupIconLeft +'; font-weight: '+ groupWeight +'">'+ title +'</span>'+
						 groupType +
					'</div>'+
			    '</foreignObject>'+
			'</svg>';			
			console.log('SVG:',svg)
			return svg;
		};
		

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
						// add image system.
						// ADDING IMAGE GROUP.						
							var font = "12px Arial"; 
							// create svg
							
							let auxTitle = ((Node.linkCreate !== null) || Node.classTarget == 'VISUALIZATIONS' )? Node.target : dataJson.title;
							var svg = createSvg(auxTitle,font,dataJson.group);							
							// encode as image
							var imageUrl  = "data:image/svg+xml;charset=utf-8," + encodeURIComponent(svg);							
							// apply shape and imagen to node
							dataJson.shape = 'image';
							dataJson.label = ''; // label inside, no need
							dataJson.image = imageUrl;
							
							if ( !group.endsWith("s") ) {
								if ( group === 'user'){
									dataJson.size=50;				
								} else {
									//childrens
								dataJson.size=20;	
								}
							}
							else { 
								// common
								dataJson.size=30;	
							}
										
												
						
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
							&& Node.classTarget == "entity") {
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
