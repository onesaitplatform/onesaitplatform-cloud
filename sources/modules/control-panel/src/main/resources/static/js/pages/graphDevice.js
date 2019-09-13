var GraphDeviceController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME  = 'onesait Platform Control Panel'
	, LIB_TITLE  = 'Graph Device Controller'
    , logControl = 0;
	
	// GRAPH INITIALIZATION VARS AND CONST.
	var links 			 = []
	,   updateData       = false
	,	nodes 			 = []
	,	data 			 = []
	,	edges 			 = []
	,	ArraySource 	 = []
	,	jsonProjects	 = {}
	,	options 		 = {}
	,	jsonIndex 		 = {}
	,	graphData		 = { nodes: [], edges: []}
	,	containerNetwork = new Object()
	,	network 		 = null
	,	interval         = null;
	
	
	// GRAPH OPTIONS DEFAULTS
	options = {	
		manipulation: false,		
		interaction: { dragNodes: false, dragView: true, hover:false , navigationButtons: true,keyboard: true},		
		physics: {
			enabled: false
		},
		nodes: {
			shadow: false,		  
			borderWidth:4,
			size:30,
			scaling:{
				label: { min:10, max:20 }
			},
			color:{ border: '#222222', background: '#666666' },
			font: { color:'#666'}
        },
        edges: {
			arrows: {
				to: {enabled: true, scaleFactor: 1 }
			},
			font: { align: 'middle', size: 10 },
			shadow: false,
			smooth: true,
			labelHighlightBold: true,
			color: '#b5afaf'
		},
		autoResize : true,
		width: '100%',
		height : '475px',
		groups:{
			active :{
				shape: 'icon',
				icon:{ face: 'FontAwesome', code: '\uf274', size: 45, color: '#00e01d' }		
			  },
			inactive :{
				shape: 'icon',
				icon:{ face: 'FontAwesome', code: '\uf272', size: 45, color: '#6d6d6d' }
			},
			error :{
				shape: 'icon',
				icon:{ face: 'FontAwesome', code: '\uf273', size: 45, color: '#d80000' }
			},
			resolved :{
				shape: 'icon',
				icon:{ face: 'FontAwesome', code: '\uf013', size: 30, color: '#ed6b75' }
			},
			licensing:{
				shape: 'icon',
				icon:{ face: 'FontAwesome', code: '\uf2d0', size: 40, color: '#c49f47' }
			},
			user:{
				shape: 'icon',
				icon:{ face: 'FontAwesome', code: '\uf2be', size: 60, color: '#4B77BE' }
			}
		},
		layout: {
            hierarchical: {
            	enabled:true,
                levelSeparation: 150,
                nodeSpacing: 150,
                treeSpacing: 200,
                blockShifting: true,
                edgeMinimization: true,
                parentCentralization: true,
                direction: 'UD',        // UD, DU, LR, RL
                sortMethod: 'directed'   // hubsize, directed
            }
        }
		
	};
	
	
	// CONTROLLER PRIVATE FUNCTIONS 	
	
	
	// CREATE HTML TABLE TO VIEW GRAPH NODE INFO.
	var createGraphInfoTable = function(){	
		logControl ? console.log('createGraphInfoTable() -> ') : '';
		
		var strInfo = '';
		
		// i18 labels 
		var propertyCol	= graphReg.propertyCol
		,	valueCol	= graphReg.valueCol
		,	tableName	= graphReg.tableName
		,	tableSource	= graphReg.tableSource
		,	tableLinkS	= graphReg.tableLinkS
		,	tableLinkC	= graphReg.tableLinkC
		,	tableLinkBtn= graphReg.tableLinkBtn
		 
		
		$("#TableInfoNetwork").hide();
		strInfo	=	'<thead>'
			+'<tr>'
				+'<th class="bg-grey-steel font-grey-gallery"><i class="fa fa-briefcase"></i> '+ propertyCol +'</th>'
				+'<th class="bg-grey-steel font-grey-gallery">'+ valueCol +'</th>'															
			+'</tr>'
		+'</thead>'
		+'<tbody>'
		
		
		strInfo	+='</tbody>';
		$("#TableInfoNetwork").empty();
		$("#TableInfoNetwork").html(strInfo);
	}
	
	// DRAW NODE INFO ON TABLE
	var drawGraphInfo = function(currentNode){
		logControl ? console.log('drawGrafoInfo() -> ') : '';
		
		// null values to ''
		$.each( currentNode ,function( key, value ){
			if( value == null ){
				currentNode[key] = "";
			}
		});
		
		cleanTable(); // clean graph info table
		
		var info="";
		
		
		
		
		if(currentNode.category==="DEVICES"){
			 info='	<tr>'
				+'		<td class="uppercase font-grey-mint">NAME</td>'
				+'		<td id="id_nombre" class="long-text"></td>'
				+'	</tr>'
				+'	<tr>'
				+'		<td class="uppercase font-grey-mint"> SOURCE</td>'
				+'		<td id="id_source" class="long-text"> </td>'
				+'	</tr>'
				
				
		}else{			
			 info='	<tr>'
				+'		<td class="uppercase font-grey-mint">NAME</td>'
				+'		<td id="id_nombre" class="long-text"></td>'
				+'	</tr>'
				+'	<tr>'
				+'		<td class="uppercase font-grey-mint"> SOURCE</td>'
				+'		<td id="id_source" class="long-text"> </td>'
				+'	</tr>'
				+'	<tr>'
				+'		<td class="uppercase font-grey-mint">INFO</td>'
				+'		<td id="id_info" class="long-text"> </td>'
				+'	</tr>'
				+'	<tr>'
				+'		<td class="uppercase font-grey-mint">LAST UPDATE</td>'
				+'		<td id="id_lastUpdate" class="long-text"> </td>'
				+'	</tr>'
				+'	<tr>'
				+'		<td class="uppercase font-grey-mint">CONNECTED</td>'
				+'		<td id="id_connected" class="long-text"> </td>'
				+'	</tr>'
				+'	<tr>'
				+'		<td class="uppercase font-grey-mint">STATUS</td>'
				+'		<td id="id_status" class="long-text"> </td>'
				+'	</tr>'
				
		}
		
		$("#TableInfoNetwork tbody").append(info); 
		
		// fill node info to table 
		$("#TableInfoNetwork").show();		
		$("#id_nombre").html(currentNode.label);
		$("#id_source").html(currentNode.nameSource);
		$("#id_info").html(currentNode.linkCreate);
		$("#id_connected").html(currentNode.connected);
		$("#id_status").html(currentNode.status);
		$("#id_lastUpdate").html(currentNode.updateAt);
		
		if ( currentNode.linkTarget ){
			var links = JSON.parse(currentNode.linkTarget);
			for (var i = 0; i < links.length; i++) {
				if(typeof links[i].name !== "undefined" && typeof links[i].url !== "undefined"){
					 
					var trButton = '	<tr>'
								   +'		<td class="uppercase font-grey-mint">EJECUTE ACTION</td>'
					               +'		<td class="long-text"><a id="id_enlaceS" href="'+links[i].url+'" class="btn btn-square btn-sm blue"><i class="fa fa-cube"></i><span> '+ links[i].name +'</span> </a></td>'
					               +'	</tr>'					
					$("#TableInfoNetwork tbody").append(trButton); 
				}
			}			
		}
		if ( currentNode.linkSource ){
			$("#id_tr_enlaceC").show();
			$("#id_enlaceC").attr("href",currentNode.linkSource);
		}
	}
	
	// AUX. CLEAN GRAPH INFO TABLE
	var cleanTable = function(){
		$("#TableInfoNetwork tbody").empty();
	
	}
		
	// HANDLE HIERARCHICAL OPTION BUTTONS
	var handleHierarchical = function(){		
		logControl ? console.log('handleHierarchical() -> ') : '';
		
		$("#hierarchicalOptions > li > a").each(function(){
			$(this).on('click',function(){
				hierarchicalNetwork(this);
			});
		});
	}

	// HANDLE CLUSTER OPTION BUTTONS
	var handleCluster = function(){		
		logControl ? console.log('handleCluster() -> ') : '';		
		
		$("#clusterOptions > li > a").each(function(){
			$(this).on('click',function(){
				clusterGraph(this);
			});
		});
	}
	
	// HIERARCHICAL GRAPH REDRAW
	var hierarchicalNetwork = function(obj){
		logControl ? console.log('hierarchicalNetwork() -> ') : '';
		
	
	
			destroyNetwork();
			
			// setting graph option properties.
			options.layout = {};
			options.layout.hierarchical = {};
			options.layout.hierarchical.direction = 'UD';
			options.layout.hierarchical.sortMethod = 'directed';
			
			// creating gpraph with this hierarchical configuration data: graphData
			containerNetwork = document.getElementById('networkVis');
			network = new vis.Network( containerNetwork, graphData, options );
			network.on("selectNode", function( params ){
		
				if( params.nodes.length == 1){
					if( network.isCluster(params.nodes[0] ) == true ){ network.openCluster( params.nodes[0] ); }

					// selected node
					var selectedId  = network.getSelection().nodes;
					var currentNode = graphData.nodes.get( selectedId );
					
					// draw node info to graphInfoTable.
					if( currentNode[0] != undefined ){ drawGraphInfo( currentNode[0] ); }
				}

			});
		
	}
	
	// CLUSTER GRAPH by CONNECTION
	var clusterByConnection = function(){
		logControl ? console.log('clusterByConnection() -> ') : '';
		network.setData( graphData );
		network.clusterByConnection(1)
	}
	
	// CLUSTER GRAPH REDRAW
	var clusterGraph = function(obj){
		logControl ? console.log('clusterGraph() -> ') : '';
		
		var action = $(obj).attr("data-action");
		if( graphData.nodes.length > 0 ){
			if ( action == "source" ){ clusterGraphBySource();} 
			if ( action == "type" )  { clusterGraphByType();  }
		}
	}
	
	// CLUSTER GRAPH by SOURCE
	function clusterGraphBySource(){
		logControl ? console.log('clusterGraphBySource() ->') : '';
		
		network.setData( graphData );
		var clusterOptionsByData;
		var target = '';
		
		for( var i = 0; i < ArraySource.length; i++){
			var idSource = ArraySource[i].id;
			var target	 = ArraySource[i].target;
			var group	 = ArraySource[i].group;
			clusterOptionsByData = {
				
				joinCondition: 		function (childOptions) { return childOptions.category == target; },
				processProperties:  function (clusterOptions, childNodes, childEdges) {
					var totalMass = 0;
					for (var i = 0; i < childNodes.length; i++) {
						totalMass += childNodes[i].mass;
					}
					clusterOptions.mass = totalMass;
					return clusterOptions;
				},
				clusterNodeProperties: {id:'cluster:'+idSource, borderWidth:3, group:'Cluster'+group, 'label':target }
			};
			network.cluster( clusterOptionsByData );
		}
		
	}
	
	// CLUSTER GRAPH by TYPE
	function clusterGraphByType(){
		logControl ? console.log('clusterGraphByType() -> ') : '';
		
		var ArrayType = ['licensing','resolved'];
		network.setData( graphData );
		var clusterOptionsByData;
		for( var i = 0; i < ArrayType.length; i++){
			
			var type = ArrayType[i];
			clusterOptionsByData = {
				joinCondition: function (childOptions) { return childOptions.type == type; },
				processProperties: function (clusterOptions, childNodes, childEdges) {
					var totalMass = 0;
					for (var i = 0; i < childNodes.length; i++) {
						totalMass += childNodes[i].mass;
					}
					clusterOptions.mass = totalMass;
					return clusterOptions;
				},
				clusterNodeProperties: {id:'cluster:'+type, borderWidth:3, group:'Cluster'+type, label:type}
			};
			network.cluster(clusterOptionsByData);
		}
	}
	
	// CLUSTER GRAPH by PROJECT
	function clusterGrafoByProject(){
		logControl ? console.log('clusterGrafoByProject') : '';
		
		network.setData( graphData );
		var clusterOptionsByData;
		$.each( jsonProjects ,function(project,value){
			clusterOptionsByData = {
				joinCondition: function (childOptions) {
					return childOptions.project == project;
				},
				processProperties: function (clusterOptions, childNodes, childEdges) {
					var totalMass = 0;
					for (var i = 0; i < childNodes.length; i++) {
						totalMass += childNodes[i].mass;
					}
					clusterOptions.mass = totalMass;
					return clusterOptions;
				},
				clusterNodeProperties: {id:'cluster:'+project, borderWidth:3, group:'projectCluster', label:project}
			};
			network.cluster( clusterOptionsByData );
		});
	}
	
	// AUX. DESTROY GRAPH
	var destroyNetwork = function(){ if( network !== null){ network.destroy(); network = null; } }
	
	// AUX. ON LOAD GRAPH DATA GET INTO FORMAT.
	var getDataGraph  = function(){
		logControl ? console.log('getDataGraph() -> ') : '';
		
		var graphDataObj 	= { nodes:[], edges:[] }
		,	ArrayNodes 		= []
		,	ArrayEdges 		= []
		,	idRef 			= {}
		,	target 			= ''
		,	nodes			= {}
		,	avoidDuplicates	= [];
		
		if ($("#id_panel_botones")){ $("#id_panel_botones").hide(); }
		$("#networkVis").hide();
		
		// handle data 
		if ( !$.isEmptyObject(links) ){
			
			if ($("#id_panel_botones")){ $("#id_panel_botones").show(); }
			// main node Loop - create nodes with info.
			$.each( links , function( Index, Node ){
				
				if( $.inArray( Node.target, avoidDuplicates ) === -1 ){
					if( Node.type === undefined ){ Node.type = "";}
					idRef[ Node.target ] = Node.target;				
					// node format, adjust type to suit if no-type
					var dataJson;
					if(Node.image===null){
					
						dataJson = {'id':Node.target, 'label':Node.nameTarget, 'source':Node.source, 'type':Node.type || 'suit', 'linkTarget':Node.linkTarget, 'nameSource':Node.nameSource, 'group':Node.classTarget, 'title':Node.title, 'linkCreate': Node.linkSource, 'status':Node.status,'connected':Node.connected,'sessionKey':Node.sessionKey,'updateAt':new Date(Node.updateAt).toLocaleString() };
					}else{
						dataJson = {'id':Node.target, 'label':Node.nameTarget, 'source':Node.source, 'image': Node.image, shape: 'image', 'type':Node.type || 'suit', 'linkTarget':Node.linkTarget, 'nameSource':Node.nameSource, 'group':Node.classTarget, 'title':Node.title, 'linkCreate': Node.linkSource, 'status':Node.status,'connected':Node.connected,'sessionKey':Node.sessionKey,'updateAt':new Date(Node.updateAt).toLocaleString()  };
					}
					group 	= dataJson.group.toLowerCase();
					type	= dataJson.type.toLowerCase();
				
					if		( options.groups[group] !== undefined ){ dataJson.group = group; }
					else if ( options.groups[type]  !== undefined ){ dataJson.group = type; }
					else{ dataJson.group = 'licensing'; }
				
					dataJson.category = Node.source;
					
					if( Node.type == 'suit'){
						dataJson.category = Node.target;
						ArraySource.push({'id':Node.target,'target':Node.target,'group':dataJson.group});
					}
					ArrayNodes.push(dataJson);
					avoidDuplicates.push(Node.target);
				}
				
			});
			
			// creating relationships between nodes.
			$.each( links , function( Index, Node){
				if( ( idRef[Node.source] !== undefined )&&( idRef[Node.target] !== undefined )&&( idRef[Node.source] !== idRef[Node.target] )){
					ArrayEdges.push({from:idRef[Node.source], to:idRef[Node.target]});
				}else{
					//console.log("Verificar nodo:"+Index+' - Source:'+Node.source+' Target:'+Node.target);
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
	
	
	// AUX. ON LOAD GRAPH DATA GET INTO FORMAT.
	var getDataGraphUpdate  = function(){
		logControl ? console.log('getDataGraph() -> ') : '';
		
		var graphDataObj 	= { nodes:[], edges:[] }
		,	ArrayNodes 		= []
		,	ArrayEdges 		= []
		,	idRef 			= {}
		,	target 			= ''
		,	nodes			= {}
		,	avoidDuplicates	= [];
		
		if ($("#id_panel_botones")){ $("#id_panel_botones").hide(); }
		$("#networkVis").hide();
		
		// handle data 
		if ( !$.isEmptyObject(links) ){
			
			if ($("#id_panel_botones")){ $("#id_panel_botones").show(); }
			// main node Loop - create nodes with info.
			$.each( links , function( Index, Node ){
				
				if( $.inArray( Node.nameTarget, avoidDuplicates ) === -1 ){
					if( Node.type === undefined ){ Node.type = "";}
					idRef[ Node.target ] = Node.target;				
					// node format, adjust type to suit if no-type
					var dataJson;
					if(Node.image===null){
					
						dataJson = {'id':Node.target, 'label':Node.nameTarget, 'source':Node.source, 'type':Node.type || 'suit', 'linkTarget':Node.linkTarget, 'nameSource':Node.nameSource, 'group':Node.classTarget, 'title':Node.title, 'linkCreate': Node.linkSource, 'status':Node.status,'connected':Node.connected,'sessionKey':Node.sessionKey,'updateAt':new Date(Node.updateAt).toLocaleString() };
					}else{
						dataJson = {'id':Node.target, 'label':Node.nameTarget, 'source':Node.source, 'image': Node.image, shape: 'image', 'type':Node.type || 'suit', 'linkTarget':Node.linkTarget, 'nameSource':Node.nameSource, 'group':Node.classTarget, 'title':Node.title, 'linkCreate': Node.linkSource, 'status':Node.status,'connected':Node.connected,'sessionKey':Node.sessionKey,'updateAt':new Date(Node.updateAt).toLocaleString()  };
					}
					group 	= dataJson.group.toLowerCase();
					type	= dataJson.type.toLowerCase();
				
					if		( options.groups[group] !== undefined ){ dataJson.group = group; }
					else if ( options.groups[type]  !== undefined ){ dataJson.group = type; }
					else{ dataJson.group = 'licensing'; }
				
					dataJson.category = Node.source;
					
					if( Node.type == 'suit'){
						dataJson.category = Node.target;
						ArraySource.push({'id':Node.target,'target':Node.target,'group':dataJson.group});
					}
					ArrayNodes.push(dataJson);
					avoidDuplicates.push(Node.nameTarget);
				}
				
			});
			
			
			// creating relationships between nodes.
			$.each( links , function( Index, Node){
				if( ( idRef[Node.source] !== undefined )&&( idRef[Node.target] !== undefined )&&( idRef[Node.source] !== idRef[Node.target] )){
					ArrayEdges.push({from:idRef[Node.source], to:idRef[Node.target]});
				}else{
					//console.log("Verificar nodo:"+Index+' - Source:'+Node.source+' Target:'+Node.target);
				}
			});
			
			
			
			
			// retrieving nodes and relations
			graphDataObj.nodes = ArrayNodes;
			graphDataObj.edges = ArrayEdges;
			$("#networkVis").show();
		}
		// RETURNING GRAPH DATA FORMATTED
		return graphDataObj;
	}
	
	
	// LOAD GRAPH DATA FROM SERVER
	var loadNetwork = function(){
		logControl ? console.log('loadNetwork() -> ') : '';
		
		if(!updateData){
			destroyNetwork();
		}
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content");
	    // AJAX CALL - get GRAPH data
		$.ajax({ url:"/controlpanel/devices/management/getgraph/", type: 'GET', dataType: 'json', contentType: 'text/html',
			headers: {
				[csrf_header]: csrf_value
		    },
			success: function(data) {
				
	        	links 		= data; 
	        	if(!updateData){
	        		graphData	= getDataGraph();
	        		if( graphData.nodes.length > 0 ){
						containerNetwork = document.getElementById('networkVis');
						if(!updateData){
							network = new vis.Network(containerNetwork, graphData, options);
							updateData=true;
						}
						network.on("selectNode", function(params){
					
							if ( params.nodes.length == 1 ) {
								if ( network.isCluster( params.nodes[0] ) == true){
									network.openCluster( params.nodes[0] );
								}
								
								// Selected Node
								var selectedId = network.getSelection().nodes;
								var currentNode = graphData.nodes.get(selectedId);
								if( currentNode[0] != undefined ){
									drawGraphInfo(currentNode[0]);
								}
							}
						});
					}
	        	}else{
	        	var graph = getDataGraphUpdate();
	        	//DELETE DATA 
	        	
	        	if(graph.nodes!=null && graph.nodes.length>0){
	        		var oldIds = graphData.nodes.getIds();
	        		for (var i = 0; i < oldIds.length; i++) {      			
	        		var found=false;	        		
	        			for (var j = 0; j < graph.nodes.length; j++) {
	        				if(graph.nodes[j].id===oldIds[i]){
	        					found=true;
	        					break
	        				}
	        			}
	        			if(!found){
	        				graphData.nodes.remove({id:oldIds[i]});
	        			}
					}
	        		var selectedId = network.getSelection().nodes;
	        		
	        		graphData.nodes.clear();
	        		graphData.edges.clear();	
	        		//UPDATE DATA
	        		graphData.nodes.add(graph.nodes);
	        		graphData.edges.add(graph.edges);
	        		
	        		
	        		// Selected Node
	        		if(selectedId.length>0){
	        			network.selectNodes(selectedId);
	        		}
					var currentNode = graphData.nodes.get(selectedId);
					
					if( currentNode[0] != undefined ){
						drawGraphInfo(currentNode[0]);
					}else{
						$('#TableInfoNetwork').hide() ;
					}
	        		
	        		
	        	}else{
	        		graphData.nodes.clear();
	        		graphData.edges.clear();	
	        	}
	        	
	        		
	        	}
				
			},
			error:function(data, status, er) { 
					$.alert({title: 'GRAPH ERROR!',  theme: 'dark' , icon: 'fa fa-warning', type: 'red', content: 'Error loading graph info on graph Controller.' });   
					clearInterval(interval);
	        }
		}); 
		
	
		
		
		
		
	}
	
	// HANDLE GRAPH HEIGHT CONTAINER
	var handleGraphHeight = function(){
		logControl ? console.log('handleGraphHeight() -> ') : ''; 
		// Add Height
		$('#btn-addH').on('click',function(){
			$('#networkVis').height(function (index, height) { return (height + 100); });
		});		
		// Remove Height
		$('#btn-remH').on('click',function(){
			$('#networkVis').height(function (index, height) { return (height - 100); });
			if ( parseInt($('#networkVis').css('height')) <= 475 ){ $('#networkVis').css('height', options.height); }
						
		});
		// Restore Height
		$('#btn-resH').on('click',function(){
			$('#networkVis').css('height', options.height);					
		});		
	}
	
	var setIntervalRefresh = function(){		
		interval = setInterval(function(){ refresh(); }, 20000);
	}
	
	var refresh =  function (){		
		loadNetwork();
	}
	
	
	// TOGGLE GRAPH INFO TABLE
	var toggleGraphInfoTable = function(){
		
		$('#btn-graphInfo').on('click', function(){ $('#TableInfoNetwork').fadeToggle() });
		
	}
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return graphReg = Data;
		},
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			createGraphInfoTable();
			handleGraphHeight();
			handleHierarchical();
			handleCluster();
			toggleGraphInfoTable();
			loadNetwork();
			setIntervalRefresh();
		}	/*,
		refresh: function(){
			loadNetwork();
		}*/
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	GraphDeviceController.load(graphJson);
	
	// AUTO INIT CONTROLLER.
	GraphDeviceController.init();
});
