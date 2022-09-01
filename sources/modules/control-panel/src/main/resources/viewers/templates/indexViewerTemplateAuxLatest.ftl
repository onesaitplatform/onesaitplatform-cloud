<!DOCTYPE html>
<html lang="en">
  <head>
    <title>Onesait Platform Viewer v2</title>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no"/>

    <!-- Load Cesium libraries -->
    <link href="https://cesium.com/downloads/cesiumjs/releases/1.92/Build/Cesium/Widgets/widgets.css" rel="stylesheet"/>
    <!-- Load Onesait Platform Cesium Styles -->
    <link href="${onesaitCesiumPath}/css/styles.css" rel="stylesheet" />
    <link href="${widgetcss}" rel="stylesheet" type="text/css" />
    <!-- Load some icon libraries -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.8.1/font/bootstrap-icons.css"/>

    <!-- scripts -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
	<script src="https://cesium.com/downloads/cesiumjs/releases/1.92/Build/Cesium/Cesium.js"></script>
	<script src="${onesaitCesiumPath}/api/onesaitCesium.js" type="module" defer="defer"></script>
    <!-- Load Turf library to allow some little GPs -->
    <script src="${onesaitCesiumPath}/vendors/turf/turf.min.js"></script>
    <!-- Load Heatmap library -->
   	<script src="${cesiumPath}"></script>	
	<script src="${heatmap}"></script>	


  </head>

  <body>
    <div class="toolbar topRight">
      <div class="toolbarSector">
        <button id="zoomPlus" data-autoSwitch="true" class="button bi-plus-lg" title="Zoom In" ></button>
        <button id="home" data-autoSwitch="true" class="button bi-house-door" title="Home"></button>
        <button id="zoomMinus" data-autoSwitch="true" class="button button bi-dash-lg" title="Zoom Out"></button>
      </div>
      <span class="toolbarSeparator"></span>
      <div class="toolbarSector">
        <button id="measureDistance" data-autoSwitch="false" class="button bi-rulers" title="Measure Distance"></button>
        <button id="measureArea" data-autoSwitch="false" class="button bi-textarea-resize" title="Measure Area"></button>
        <button id="userPosition" data-autoSwitch="true" class="button bi-pin-map" title="Get my position"></button>
        <button id="cursorCoordinates" data-autoSwitch="false" data-standAlone="true" class="button bi-chat-left-dots" title="Show Cursor Coordinates"></button>
        <button id="selectByAttributes" data-autoSwitch="false" data-standAlone="true" class="button bi-binoculars disabled" title="Select Entity by Attribute"></button>
      </div>
    </div>

    <div class="toolbar bottomRight">
      <div class="toolbarSector">
        <button id="showLayers" data-autoSwitch="false" data-standAlone="true" class="button bi-map disabled" title="Map Legend"></button>
      </div>
    </div>

    <div class="geocoder">
      <input placeholder="Enter an address..." list="geocoderCandidates" id="geocoderAddressInput" name="geocoderAddressInput" type="search" size="40"/>
      <datalist id="geocoderCandidates"> </datalist>
    </div>

    <div id="selectByAttributesPanel" class="selectByAttributesPanel">
      <h4>Select by Attribute:</h4>
      <div class="selectByAttributesItem">
        <label>Layer:</label>
        <select name="selectByAttributesLayers" id="selectByAttributesLayers"></select>
      </div>
      <div class="selectByAttributesItem">
        <label style="padding-right: 6px">Field:</label>
        <select name="selectByAttributesField" id="selectByAttributesField" class="selectByAttributesField">
          <option value="empty" disabled selected>Choose a layer first</option>
        </select>
      </div>
      <div class="selectByAttributesItem">
        <label>Value:</label>
        <input style="margin-left: 1px" type="text" id="selectByAttributesValue"/>
        <input type="submit" id="selectByAttributesButton" />
      </div>
      <span id="selectByAttributesResult" style="margin-top: 10px"></span>
    </div>

    <div class="layerListButton">
      <div id="layersDropdown" class="layersDropdown"></div>
    </div>

	<div id="cesiumContainer"></div>
	<div id="statusbar"></div>
	<input id="intervalIds" type="hidden" value=""/>
	
  </body>
</html>
