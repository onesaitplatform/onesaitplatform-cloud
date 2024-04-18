//Write your controller (JS code) code here
//Focus here and F11 or F10 to full screen editor
var map;
ds: [];
selectedFeaturesData: [];

//This function will be call once to init components
vm.initLiveComponent = function () {
    var app = this
    var tile = vm.setLayer(vm.tparams.parameters.layers.layer)

    map = new ol.Map({
        view: new ol.View({
            center: ol.proj.fromLonLat([vm.tparams.parameters.center.center_longitude, vm.tparams.parameters.center.center_latitude]),
            zoom: vm.tparams.parameters.center.center_zoom,
        }),
        layers: [
            tile
        ],
        target: document.getElementById(vm.id).querySelector(' .gadget-app'),
    });

    if ($scope.ds) {
        vm.drawLiveComponent($scope.ds)
    }

    // SELECT INTERACTION
    this.select = new ol.interaction.Select({
        layers: this.selectableLayers,
    })
    map.addInteraction(this.select)

    // POPUP SETUP
    if (vm.tparams.parameters.marker.popup.popupData.length > 0  && !(vm.tparams.parameters.marker.popup.popupData.length == 1 && vm.tparams.parameters.marker.popup.popupData[0].field == null)) {
        const container = document.getElementById('popup')
        const content = document.getElementById('popup-content')
        const closer = document.getElementById('popup-closer')

        const overlay = new ol.Overlay({
            element: container,
            autoPan: {
                animation: {
                    duration: 250,
                },
            },
        })
        map.addOverlay(overlay)

        let hovered = null
        let option = ""
        if (vm.tparams.parameters.marker.popup.showOn == 'click') {
            option = 'singleclick'
        } else if (vm.tparams.parameters.marker.popup.showOn == 'hover') {
            option = 'pointermove'
        }
        map.on(option, function (e) {
            if (hovered !== null) {
                // hovered.setStyle(undefined)
                hovered = null
            }
            this.forEachFeatureAtPixel(e.pixel, function (f) {
                hovered = f
                return true
            })
            if (hovered && hovered.getGeometry().getType() === 'Point') {
                const position = hovered.getGeometry().getCoordinates()

                content.innerHTML = hovered.values_.name
                overlay.setPosition(position)
            } else {
                content.innerHTML = '&nbsp;'
                overlay.setPosition(null)
            }
        })
    }

    //Select on Click
    const selectedFeatures = this.select.getFeatures()
    const selectedName = {}
    this.select.on('select', function (e) {

        // SELECCION DE ESTACIONES                
        if (e.selected.length > 0) {
            this.selectedFeaturesData = []

            selectedFeatures.forEach((feature) => {

                //CHANGE THE MARKER STYLE ON SELECT
                if (vm.tparams.parameters.marker.select.select_change == true) {

                    var icon = vm.tparams.parameters.marker.marker_icon

                    feature.setStyle(new ol.style.Style({
                        text: new ol.style.Text({
                            text: icon.toLowerCase(),
                            font: 'normal 38px \"Material Icons\"',
                            //textBaseline: 'Bottom',
                            fill: new ol.style.Fill({
                                color: vm.tparams.parameters.marker.select.select_color,
                            }),
                            stroke: new ol.style.Stroke({
                                color: '#e3e6e4',
                                width: 8,
                            })
                        })
                    }))
                } else {
                    var icon = vm.tparams.parameters.marker.marker_icon
                    feature.setStyle(new ol.style.Style({
                        text: new ol.style.Text({
                            text: icon.toLowerCase(),
                            font: 'normal 38px \"Material Icons\"',
                            //textBaseline: 'Bottom',
                            fill: new ol.style.Fill({
                                color: vm.tparams.parameters.marker.marker_color,
                            }),
                            stroke: new ol.style.Stroke({
                                color: 'white',
                                width: 4,
                            })
                        })
                    }))
                }
                // console.log('FEATURE: ', feature)
                //this.selectedFeaturesData.push({ buildingName : $scope.ds.Building_Name })

                this.selectedFeaturesData.push({ name: feature.values_.popup })

            })

            if (this.selectedFeaturesData.length > 0) {
                vm.sendFilter("name", this.selectedFeaturesData[0].popup)
            }


        } else if (e.selected.length === 0 && e.deselected.length > 0) {
            // deselect one or multiple but still selected elements.
            // console.log('DESELECT: ', e.deselected[0])
            if (e.deselected.length === 1 && e.deselected[0].values_.estacionId) {
                this.selectedFeaturesData = this.selectedFeaturesData.filter(x => x.estacionId !== e.deselected[0].values_.estacionId)

            } else {
                // DESELECT multiple 
                vm.sendFilter("name", null)
                this.selectedFeaturesData = []
                filterService.cleanAllFilters(vm.id, {})
            }
        } else {
            // DESELECT ALL 
            // console.log('Deselect');
            app.selectedFeaturesData = []
            filterService.cleanAllFilters(vm.id, {})
            vm.sendFilter("name", null)
        }
    })
};

//This function handle the layers
vm.setLayer = function (layer) {
    var layer;
    if (layer == 'OSM') {
        layer = new ol.layer.Tile({
            source: new ol.source.OSM()
        })
    };
    if (layer == 'Terrain') {
        layer = new ol.layer.Tile({
            source: new ol.source.XYZ({
                url: 'https://services.arcgisonline.com/arcgis/rest/services/World_Shaded_Relief/MapServer/tile/ {z}/{y}/{x}',
                maxZoom: 19
            })
        });
    }
    if (layer == 'Satellite') {
        layer = new ol.layer.Tile({
            source: new ol.source.XYZ({
                url: 'https://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/ {z}/{y}/{x}',
                maxZoom: 19
            })
        });
    }

    return layer;
}

//This function will add the markers to the map
vm.addMarker = function (lon, lat, popup, cond) {
    var layer = new ol.layer.Vector({
        source: new ol.source.Vector({
            features: [
                new ol.Feature({
                    geometry: new ol.geom.Point(ol.proj.fromLonLat([lon, lat])),
                    name: popup.join('<br>'),
                })
            ]
        })
    });

    var color = vm.tparams.parameters.marker.marker_color
    for (let e = 0; e < cond.length; e++) {
        if (cond[e] != vm.tparams.parameters.marker.marker_color) {
            color = cond[e]
        }
    }

    var icon = vm.tparams.parameters.marker.marker_icon
    var color = vm.tparams.parameters.marker.marker_color

    layer.setStyle(new ol.style.Style({
        text: new ol.style.Text({
            text: icon.toLowerCase(),
            font: 'normal 38px \"Material Icons\"',
            //textBaseline: 'Bottom',
            fill: new ol.style.Fill({
                color: cond[cond.length - 1],
            }),
            stroke: new ol.style.Stroke({
                color: 'white',
                width: 4,
            })
        })
    }));


    map.addLayer(layer);
};

//This function will be call when data change. On first execution oldData will be null
vm.drawLiveComponent = function (newData, oldData) {
    this.ds = newData;
    var longitude = this.ds.map(inst => vm.utils.findValues(inst, vm.tparams.parameters.marker.marker_longitude))
    var latitude = this.ds.map(inst => vm.utils.findValues(inst, vm.tparams.parameters.marker.marker_latitude))
    if (vm.tparams.parameters.marker.popup.popupData.length > 0 && !(vm.tparams.parameters.marker.popup.popupData.length == 1 && vm.tparams.parameters.marker.popup.popupData[0].field == null)) {
        var popup = vm.calculatePopup(this.ds)
    } else {
        var popup = {}
    }
    if (vm.tparams.parameters.marker.condcolor.checkConditional) {
        var cond = vm.handleCondColor(this.ds)
    } else {
        var cond = vm.tparams.parameters.marker.marker_color
    }
    var popupMapped = []
    var colorMapped = []
    for (let i = 0; i < $scope.ds.length; i++) {
        for (let e = 0; e < popup.length; e++) {
            popupMapped.push(popup[e].data[i])
        }
        if (vm.tparams.parameters.marker.condcolor.checkConditional) {
            for (let e = 0; e < cond.length; e++) {
                colorMapped.push(cond[e].color[i])
            }
        }
        vm.addMarker(longitude[i], latitude[i], popupMapped, colorMapped)
        popupMapped = []
        colorMapped = []
    }
};

vm.handleCondColor = function (data) {
    var color = [];
    return vm.tparams.parameters.marker.condcolor.colorData.map(function (s) {

        var values = data.map(inst => vm.utils.findValues(inst, s.field))
        for (let i = 0; i < values.length; i++) {
            if ((s.op == "=") && (values[i] == s.value)) {
                color.push(s.color);
            } else if ((s.op == "<") && (values[i] < s.value)) {
                color.push(s.color);
            } else if ((s.op == ">") && (values[i] > s.value)) {
                color.push(s.color);
            } else if ((s.op == "<=") && (values[i] <= s.value)) {
                color.push(s.color);
            } else if ((s.op == ">=") && (values[i] >= s.value)) {
                color.push(s.color);
            } else {
                color.push(vm.tparams.parameters.marker.marker_color)
            }
        }

        return {
            color
        }
    })
}

//This function claculate the popup
vm.calculatePopup = function (data) {
    return vm.tparams.parameters.marker.popup.popupData.map(function (s) {
        return {
            data: data.map(inst => "<b>" + s.key + ": " + "</b>" + s.prefixValue + vm.utils.findValues(inst, s.field) + s.postfixValue),
        }
    })
}

//This function handle the popup
vm.handlePopup = function (data) {

}

//This function will be call on element resize
vm.resizeEvent = function () {

}

//This function will be call when element is destroyed
vm.destroyLiveComponent = function () {
    map.setTarget(null);
    map = null;
};

//This function will be call when receiving a value from vm.sendValue(idGadgetTarget,data)
vm.receiveValue = function (data) {

};