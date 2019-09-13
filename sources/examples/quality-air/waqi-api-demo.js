
function init( tokenId, inputId, outputId ) {

	token.id = tokenId;

	var input = $(inputId);
	var timer = null;
	var output = $(outputId);

	input.on("keyup",function(){

		/* Debounce */
		if (timer) clearTimeout(timer);
		timer = setTimeout(function(){
			search(input.val(), output);
		},250);

	})

}

function search(keyword, output) {

	var info = token()=="demo"?"(based on demo token)":""
	output.html("<h2>Search results "+info+":</h2>")
	output.append($("<div/>").html("Loading..."))
	output.append($("<div/>").addClass("cp-spinner cp-meter"))

	$.getJSON("//api.waqi.info/search/?token="+token()+"&keyword="+keyword,function(result){

		var info = token()=="demo"?"(based on demo token)":""
		output.html("<h2>Search results "+info+":</h2>")
		if (!result || (result.status!="ok")) {
			output.append("Sorry, something wrong happend: ")
			if (result.data) output.append($("<code>").html(result.data))
			return
		}

		if (result.data.length==0) {
			output.append("Sorry, there is no result for your query!")
			return
		}

		var table = $("<table/>").addClass("result")
		output.append(table)

		output.append($("<div/>").html("Click on any of the station to see the detailled AQI"))

		var stationInfo = $("<div/>")
		output.append(stationInfo)

		result.data.forEach(function(station,i){
			var tr = $("<tr>");
			tr.append($("<td>").html(station.station.name))
			tr.append($("<td>").html(colorize(station.aqi)))
			tr.append($("<td>").html(station.time.stime))
			tr.on("click",function(){
				showStation(station,stationInfo)
			})
			table.append(tr)
			if (i==0) showStation(station,stationInfo)
		})




	});
}

function showStation(station, output) {
	output.html("<h2>Pollutants & Weather conditions:</h2>")
	output.append($("<div/>").html("Loading..."))
	output.append($("<div/>").addClass("cp-spinner cp-meter"))

	$.getJSON("//api.waqi.info/feed/@"+station.uid+"/?token="+token(),function(result){

		output.html("<h2>Pollutants & Weather conditions:</h2>")
		if (!result || (result.status!="ok")) {
			output.append("Sorry, something wrong happend: ")
			if (result.data) output.append($("<code>").html(result.data))
			return
		}

		var names = {
			pm25: "PM<sub>2.5</sub>",
			pm10: "PM<sub>10</sub>",
			o3: "Ozone",
			no2: "Nitrogen Dioxide",
			so2: "Sulphur Dioxide",
			co: "Carbon Monoxyde",
			t: "Temperature",
			w: "Wind",
			r: "Rain (precipitation)",
			h: "Relative Humidity",
			d: "Dew",
			p: "Atmostpheric Pressure"
		}

		output.append($("<div>").html("Station: "+result.data.city.name+" on "+result.data.time.s))

		var table = $("<table/>").addClass("result")
		output.append(table)

		for (var specie in result.data.iaqi) {
			var aqi = result.data.iaqi[specie].v
			var tr = $("<tr>");
			tr.append($("<td>").html(names[specie]||specie))
			tr.append($("<td>").html(colorize(aqi,specie)))
			table.append(tr)
		}
	})
}

function token() {
	return $(token.id).val()||"demo";
}


function colorize(aqi, specie ) {
	specie = specie||"aqi"
	if (["pm25","pm10","no2","so2","co","o3","aqi"].indexOf(specie)<0) return aqi;

	var spectrum = [
		{a:0,  b:"#cccccc",f:"#ffffff"},
		{a:50, b:"#009966",f:"#ffffff"},
		{a:100,b:"#ffde33",f:"#000000"},
		{a:150,b:"#ff9933",f:"#000000"},
		{a:200,b:"#cc0033",f:"#ffffff"},
		{a:300,b:"#660099",f:"#ffffff"},
		{a:500,b:"#7e0023",f:"#ffffff"}
		];


	var i = 0;
	for (i=0;i<spectrum.length-2;i++) {
		if (aqi=="-"||aqi<=spectrum[i].a) break;
	};	
	return $("<div/>")
		.html(aqi)
		.css("font-size","120%")
		.css("min-width","30px")
		.css("text-align","center")
		.css("background-color",spectrum[i].b)
		.css("color",spectrum[i].f)

}