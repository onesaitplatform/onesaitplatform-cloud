var gadget= [ {
		type : "line",
		icon : "/controlpanel/images/dashboards/icon_line_chart.svg",

		attributes : [ {
			name : "X axis",
			types : [ "date", "number", "string" ]
		}, {
			name : "Y axis",
			types : [ "number", "string" ]
		} ],
		series : {
			maxseries : -1,
			options : [
				{
					"type" : "color",
					"name" : "Color",
					"default" : "rgba(0,108,168,0.62)",
					"jsonFields" : ["backgroundColor","borderColor","pointBackgroundColor","pointHoverBackgroundColor"]
				},
				{
					"type" : "select",
					"name" : "Y Axis ID",
					"class" : "yaxisselector",
					"ref" : '$(".axis_id").splice(0,$(".axis_id").length).map(function(e){return e.value})',
					"jsonFields": ["yAxisID"]
				},
				{
					"type" : "boolean",
					"name" : "Fill Serie",
					"jsonFields": ["fill"]
				},{
					"type" : "boolean",
					"name" : "SteppedLine",					
					"jsonFields": ["steppedLine"]
				},
				{	"type" : "position",
					"name" : "Point Radius",
					"default" : 3,
					"jsonFields" : ["radius"]
				}			
			]
		},
		options : {
			scales: {
				yAxes: {
					type: "array",
					elems: {
						
					}
				}
			}
		}
	}, {
		type : "bar",
		icon : "/controlpanel/static/images/dashboards/icon_bar_chart.svg",
		attributes : [ {
			name : "X axis",
			types : [ "date", "number", "string" ]
		}, {
			name : "Y axis",
			types : [ "number", "string" ]
		} ],
		series : {
			maxseries : -1,
			options : [
				{
					"type" : "color",
					"name" : "Color",
					"default" : "rgba(0,108,168,0.62)",
					"jsonFields" : ["backgroundColor","borderColor","pointBackgroundColor"]
				},
				{
					"type" : "select",
					"name" : "Y Axis ID",
					"class" : "yaxisselector",
					"ref" : '$(".axis_id").splice(0,$(".axis_id").length).map(function(e){return e.value})',
					"jsonFields": ["yAxisID"]
				}
			]
		},
		options:{}
	},{
		type : "mixed",
		icon : "/controlpanel/static/images/dashboards/icon_mixed_chart.svg",
		attributes : [ {
			name : "X axis",
			types : [ "date", "number", "string" ]
		}, {
			name : "Y axis",
			types : [ "number", "string" ]
		} ],
		series : {
			maxseries : -1,
			options : [
				{
					"type" : "color",
					"name" : "Color",
					"default" : "rgba(0,108,168,0.62)",
					"jsonFields" : ["backgroundColor","borderColor","pointBackgroundColor","pointHoverBackgroundColor"]
				},
				{
					"type" : "select",
					"name" : "Y Axis ID",
					"class" : "yaxisselector",
					"ref" : '$(".axis_id").splice(0,$(".axis_id").length).map(function(e){return e.value})',
					"jsonFields": ["yAxisID"]
				},
				{
					"type" : "select",
					"name" : "Type",
					"class" : "typeselector",
					"ref" : ['bar','line','points'],
					"jsonFields": ["type"]
				},
				{
					"type" : "boolean",
					"name" : "Fill Serie",
					"jsonFields": ["fill"]
				},{
					"type" : "boolean",
					"name" : "SteppedLine",					
					"jsonFields": ["steppedLine"]
				},
				{	"type" : "position",
					"name" : "Point Radius",
					"default" : 3,
					"jsonFields" : ["radius","pointRadius","pointHoverRadius"]
				}
			]
		},
		options:{}
	},


	{
		type : "pie",
		icon : "/controlpanel/static/images/dashboards/icon_piechart.svg",
		attributes : [ {
			name : "X axis",
			types : [ "date", "number", "string" ]
		}, {
			name : "Y axis",
			types : [ "number", "string" ]
		} ],
		series : {
			maxseries : 1,
			options : []
		},
		options:{}
	},

	{
		type : "map",
		icon : "/controlpanel/static/images/dashboards/icon_map.svg",
		attributes : [ {
			name : "Latitude",
			types : [ "number", "string" ]
		}, {
			name : "Longitude",
			types : [ "number", "string" ]
		}, {
			name : "Point ID",
			types : [ "number", "string", "boolean" ]
		}, {
			name : "Popup",
			types : [ "number", "string", "boolean" ],
			multi :true //multivalue
		} ],
		series : {
			maxseries : 1,
			options : {}
				
			
		},
		options:{
			center:{
				lat:30,
				lng:30,
				zoom:2
			},
			markersFilter:"",
			jsonMarkers:""
		}
	}, {
		type : "wordcloud",
		icon : "/controlpanel/static/images/dashboards/icon_wordcloud.svg",
		attributes : [ {
			name : "Words",
			types : [ "number", "string" ]
		}],
		series : {
			maxseries : 1,
			options : {
			}
		},
		options:{
		}
	}, {
		type : "radar",
		icon : "/controlpanel/static/images/dashboards/icon_radar.svg",
		attributes : [ {
			name : "category",
			types : [ "date", "number", "string"  ]
		},{
			name : "value",
			types : [ "number", "string" ]
		} ],
		series : {
			maxseries : -1,
			options : [				
			]
		},
		options:{}
	},{
		type : "table",
		icon : "/controlpanel/static/images/dashboards/icon_table.svg",
		attributes : [ {
			name : "field",
			types : [ "number", "string", "boolean"]
		}],
		series : {
			maxseries : -1,
			options : [
				
			{	"type" : "position",
				"name" : "position",
				"default" : "",
				"jsonFields" : ["position"]
			}
			]
		},
		options:{}
	},{
		type : "datadiscovery",
		disableOnGroup : true, 
		icon : "/controlpanel/static/images/dashboards/widgets.svg",
		attributes : [],
		series : {
			maxseries : 0,
			options : []
		},
		options: [
		]
	}

	];