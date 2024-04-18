@echo off
echo Dashboard Build

where npm >nul 2>nul
if errorlevel 1 (
    echo No npm found. Skipping build
    exit /b 0
)
echo npm found

where gulp >nul 2>nul
if errorlevel 1 (
    echo No gulp found. Skipping build dashboard, use 'npm install -g gulp@4.0.2' to install
    exit /b 0
)
echo gulp found

echo Install libs
npm install

if errorlevel 1 (
    echo npm install failed. Exiting script.
    exit /b 1
)

echo Gulp Build
gulp build

if errorlevel 1 (
    echo Gulp build failed. Exiting script.
    exit /b 1
)

echo Copy Resources to controlpanel

copy ".\dist\scripts\app.js" "..\..\sources\modules\control-panel\src\main\resources\static\dashboards\scripts\app.js"
copy ".\dist\scripts\vendor.js" "..\..\sources\modules\control-panel\src\main\resources\static\dashboards\scripts\vendor.js"
copy ".\dist\styles\app.css" "..\..\sources\modules\control-panel\src\main\resources\static\dashboards\styles\app.css"
copy ".\dist\styles\vendor.css" "..\..\sources\modules\control-panel\src\main\resources\static\dashboards\styles\vendor.css"
copy ".\dist\gridster.css" "..\..\sources\modules\control-panel\src\main\resources\static\dashboards\gridster.css"
copy ".\dist\gridster.js" "..\..\sources\modules\control-panel\src\main\resources\static\dashboards\gridster.js"
copy ".\dist\index.html" "..\..\sources\modules\control-panel\src\main\resources\static\dashboards\index.html"

echo End Dashboard Build
