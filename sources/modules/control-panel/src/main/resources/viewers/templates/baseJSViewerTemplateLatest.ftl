/** GLOBAL VARIABLES */
/** LATEST **/
var intervalIds = [];
/** Set the initial extent. Change the values to set your own extent */
var startLongitude = ${longitude}
var startLatitude = ${latitude}
var startHeight = ${height}
var basePath = "${basePath}";

import { onesaitCesium } from '${onesaitCesiumPath}/api/onesaitCesium.js'

/** Init the map */
const map = onesaitCesium.map.initMap();

window.onesaitCesium=onesaitCesium;

/** Example of how to set the initial view */
onesaitCesium.camera.setInitZoom(
	{
      coordinates: [startLongitude, startLatitude],
      height: startHeight
    },
    map
)
