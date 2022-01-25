#!/bin/bash

echo "Dashboard Build"

if ! command -v npm &> /dev/null
then
	echo "No npm found skip build"
	exit 0
fi
echo "npm found"

if ! command -v gulp &> /dev/null
then
	echo "No gulp found. Skipping build dashboard, use 'npm install -g gulp@4.0.2' to install"
	exit 0
fi
echo "gulp found"

echo "Install libs"
npm install

echo "Gulp Build"
gulp build

echo "Copy Resources to controlpanel"

cp "./dist/scripts/app.js" "../../sources/modules/control-panel/src/main/resources/static/dashboards/scripts/app.js"
cp "./dist/scripts/vendor.js" "../../sources/modules/control-panel/src/main/resources/static/dashboards/scripts/vendor.js"
cp "./dist/styles/app.css" "../../sources/modules/control-panel/src/main/resources/static/dashboards/styles/app.css"
cp "./dist/styles/vendor.css" "../../sources/modules/control-panel/src/main/resources/static/dashboards/styles/vendor.css"
cp "./dist/gridster.css" "../../sources/modules/control-panel/src/main/resources/static/dashboards/gridster.css"
cp "./dist/gridster.js" "../../sources/modules/control-panel/src/main/resources/static/dashboards/gridster.js"
cp "./dist/index.html" "../../sources/modules/control-panel/src/main/resources/static/dashboards/index.html"

echo "End Dashboard Build"
