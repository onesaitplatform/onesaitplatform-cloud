var gadgetJSON = [ {

	type : "line",

	icon : "../static/images/dashboards/linechart.jpg",

	attributes : [

	{

		name : "X axis",

		types : [ "date", "number" ]

	},

	{

		name : "Y axis",

		types : [ "number" ]

	}

	],

	series : {

		maxseries : -1,

		options : {

			"title" : "Series options",

			"type" : "object",

			"properties" : {

				"Name" : {

					"type" : "string",

					"description" : "Serie name"

				},

				"Color" : {

					"type" : "string",

					"format" : "color",

					"title" : "Serie color",

					"default" : "#ffa500"

				}

			}

		}

	},

	options : {

		beginToZero : true

	}

},

{
	type : "bar",
	icon : "../static/images/dashboards/barchart.jpg",
	options : ""
},

{
	type : "pie",
	icon : "../static/images/dashboards/piechart.jpg",
	options : ""
},

{
	type : "map",
	icon : "../static/images/dashboards/bordermap.png",
	options : ""
}, {
	type : "wordcloud",
	icon : "../static/images/dashboards/wordchart.jpg",
	options : ""
}

]
