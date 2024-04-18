var loadBubbleMap = function(data){
	$('.map').removeClass('hide');
    var H = Highcharts,
        map = H.maps['custom/world'],
        chart;

    chart = Highcharts.mapChart('map', {

        title: {
            text: 'Flights from ' + $('#srcId').val()
        },

        tooltip: {
            pointFormat: '{point.city}({point.IATA}), {point.country}<br>' +
                '# of Arrival: {point.z}'
        },
        
        mapNavigation: {
            enabled: true,
            buttonOptions: {
                verticalAlign: 'bottom'
            }
        },

        series: [{
            name: 'Basemap',
            mapData: map,
            borderColor: '#606060',
            nullColor: 'rgba(200, 200, 200, 0.2)',
            showInLegend: false
        }, {
            type: 'mapbubble',
            dataLabels: {
                enabled: true,
                format: '{point.city}'
            },
            name: 'Cities',
            data: data,
            maxSize: '10%',
            color: H.getOptions().colors[0]
        }]
    });
};