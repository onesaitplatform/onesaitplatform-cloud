/** GLOBAL VARIABLES */

/** Set the initial extent. Change the values to set your own extent */
var startLongitude = ${longitude}
var startLatitude = ${latitude}
var startHeight = ${height}
var basePath = "${basePath}";

$(document).ready(function() {
	if (viewer != undefined) {
		var options = []
		$.each(dataSourceLayers, function(k, v) {
			options.push('<option value=\'' + v + '\'>' + v + '</option>')
		})
		$('#layers').html(options)
		$('#layers').selectpicker('refresh')
		$('#layers').val(dataSourceLayers)
		$('#layers').selectpicker('refresh')
		showMouseCartographicPosition()
	}
})

var dataSourceLayers = []

$('#layers').on('change', function() {
	var datasources = dataSourceLayers
	var imageryLayers = viewer.imageryLayers._layers
	var dataSourcesViewer = viewer.dataSources._dataSources
	var layersSelected = $('#layers').val()
	var options = []
	var layers = []

	$.each(datasources, function(k, v) {
		var layer = v
		var dataSourceLayer = 'dataSource' + v
		if (layersSelected.includes(layer)) {
			options.push('<option value=\'' + v + '\'>' + v + '</option>')
			layers.push(layer)
			$.each(dataSourcesViewer, function(index, value) {
				if (
					value.name.name == 'dataSource' + layer ||
					value.name == 'dataSource' + layer
				) {
					value.show = true
				}
			})

			$.each(imageryLayers, function(index, value) {
				if (value.imageryProvider.name == 'dataSource' + layer) {
					value.show = true
				}
			})

			$.each(heatMapLayers, function(index, value) {
				if (value.layer == layer) {
					value.heatMap._layer.show = true
				}
			})
		} else {
			$.each(dataSourcesViewer, function(index, value) {
				if (
					value.name.name == 'dataSource' + layer ||
					value.name == 'dataSource' + layer
				) {
					value.show = false
				}
			})

			$.each(imageryLayers, function(index, value) {
				if (value.imageryProvider.name == 'dataSource' + layer) {
					value.show = false
				}
			})

			$.each(heatMapLayers, function(index, value) {
				if (value.layer == layer) {
					value.heatMap._layer.show = false
				}
			})
		}
	})

	$('#layers').val(layers)
	$('#layers').selectpicker('refresh')
})


/** VIEWER */

var viewer = new Cesium.Viewer('cesiumContainer', {
	/** Bottom left widget */
	animation: false,
	/** Home widget button */
	homeButton: false,
	/** Basemap picker widget */
	baseLayerPicker: false,
	/** Full screen widget button */
	fullscreenButton: false,
	/** Geocoger input widget */
	geocoder: false,
	/** infoBox window **/
	infoBox: true,
	/** Useless widget button */
	navigationHelpButton: false,
	/** Force 3D scene always */
	scene3DOnly: true,
	/** Timeline widget */
	timeline: false,
	/** Green selector */
	selectionIndicator: false
})

/** Remove the HDR effect after Cesium 1.52 */
viewer.scene.highDynamicRange = false

/** Change background color to WHITE */
viewer.scene.globe.baseColor = Cesium.Color.WHITE

/** Remove all basemaps */
viewer.scene.imageryLayers.removeAll()

/** LOCAL VARIABLES */

var dataSourceCollection = new Cesium.DataSourceCollection()
var baseMapsLayers = viewer.imageryLayers
var baseMap = null
var heatMapLayers = []

/**
 * FUNCTIONS
 */
 
 /** FILTER DATA */

function filterLayer(data) {
	let layerName
	let layerNameProcessed

	layerName = data.name

	let layerNameSplit = layerName.split(/\s/g)
	let arrayNames = []

	/** Iterate over the names to uppercase its first character */
	layerNameSplit.forEach(function(name) {
		arrayNames.push(name.charAt(0).toUpperCase() + name.slice(1))
	})

	/** Define the layer name from the join */
	layerNameProcessed = arrayNames.join('')

	/** Set the dataSource */
	let dataSource

	/** Define the future name of the dataSource */
	let dataSourceName = 'dataSource' + layerNameProcessed

	/** Get the dataSource */
	if (viewer.dataSources._dataSources.length > 0) {
		viewer.dataSources._dataSources.forEach(viewerDataSource => {
			if (viewerDataSource.name.name === dataSourceName) {
				dataSource = viewerDataSource
			}
		})
	}

	/** Iterate over the filters */
	data.symbology.filters.forEach(filter => {
		/** Set the key and value elements */
		let keyField
		let operator
		let valueField
		let newColor

		/** Get the values */
		if (filter.operation.includes('===')) {
			keyField = filter.operation.split('===')[0]
			operator = '==='
			valueField = filter.operation.split('===')[1]
		} else if (filter.operation.includes('>')) {
			keyField = filter.operation.split('>')[0]
			operator = '>'
			valueField = filter.operation.split('>')[1]
		} else if (filter.operation.includes('<')) {
			keyField = filter.operation.split('<')[0]
			operator = '<'
			valueField = filter.operation.split('<')[1]
		} else if (filter.operation.includes('!=')) {
			keyField = filter.operation.split('!=')[0]
			operator = '!='
			valueField = filter.operation.split('!=')[1]
		}

		/** Set the new color for the entity */
		newColor = filter.colorHEX

		/** Iterate over the entities of the dataSource */
		dataSource.entities.values.forEach(entity => {
			/** Get the value through what will be filtered */
			let entityFilterValue = entity.entityProperties.properties[keyField]

			if (operator === '>') {
				if (entityFilterValue > valueField) {
					/** Change the color of the entity */
					entity.point.color = Cesium.Color.fromCssColorString(newColor)

					/** Update the entity color metadata */
					entity.entitySymbology.color = newColor
				}
			} else if (operator === '<') {
				if (entityFilterValue < valueField) {
					/** Change the color of the entity */
					entity.point.color = Cesium.Color.fromCssColorString(newColor)

					/** Update the entity color metadata */
					entity.entitySymbology.color = newColor
				}
			} else if (operator === '!=') {
				if (valueField != entityFilterValue) {
					/** Change the color of the entity */
					entity.point.color = Cesium.Color.fromCssColorString(newColor)

					/** Update the entity color metadata */
					entity.entitySymbology.color = newColor
				}
			} else if (operator === '===') {
				if (valueField === entityFilterValue) {
					/** Change the color of the entity */
					entity.point.color = Cesium.Color.fromCssColorString(newColor)

					/** Update the entity color metadata */
					entity.entitySymbology.color = newColor
				}
			}
		})
	})
}

/** */

/** END OF FILTER DATA */
 
 /** FEATURE SELECTION */

function featureSelection(selectionHandler, selectedColor, selectedAlpha) {
  switch (selectionHandler) {
    case 'simple':
      simpleSelection()
      break

    case 'multiSelection':
      multiSelection()
      break
  }

  function simpleSelection() {
    /** Set global variables */
    let presentSelectedEntity
    let presentTypeGeometry
    let previousSelectedEntity
    let previousTypeGeometry

    /** Set an event handler */
    let handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)

    /** Get the pick event */
    handler.setInputAction(click => {
      /** Check if an entity has been selected */
      if (viewer.selectedEntity) {
        /** Check if the entity allows to be selected */
        if (viewer.selectedEntity.entityProperties.allowPicking) {
          /** Set the entity as the selected one */
          let selectedEntity = viewer.selectedEntity

          /** Check if there isn't anything else already selected */
          if (!presentSelectedEntity) {
            /** Get and set the geometry of the selected entity */
            presentTypeGeometry =
              selectedEntity.entityProperties.typeGeometry.type

            /** As there aren't any previous selection, make the previous
             * geometry type the same as actual one */
            previousTypeGeometry = presentTypeGeometry

            /** Set previous and present selection as the same */
            previousSelectedEntity = presentSelectedEntity = selectedEntity

            /** Now check the type of selected entity */
            switch (presentTypeGeometry) {
              case 'Point':
                /** Check the type of symbology */
                if (
                  selectedEntity.entityProperties.typeSymbology === 'billboard'
                ) {
                  /** Launch the new selection point as billboard function */
                  newSelectionPointBillboard(presentSelectedEntity)
                } else {
                  /** Launch the new selection point as color function */
                  newSelectionPointColor(
                    presentSelectedEntity,
                    selectedColor,
                    selectedAlpha
                  )
                }
                break

              case 'LineString':
                /** Launch the new selection line as color function */
                newSelectionLineStringColor(
                  presentSelectedEntity,
                  selectedColor,
                  selectedAlpha
                )
                break

              case 'Polygon':
                /** Launch the new selection polygon as color function */
                newSelectionPolygonColor(
                  presentSelectedEntity,
                  selectedColor,
                  selectedAlpha
                )
                break
            }
          } else if (presentSelectedEntity) {
            /** Get and set the geometry of the selected entity */
            presentTypeGeometry =
              selectedEntity.entityProperties.typeGeometry.type

            /** Set previous and present selection as the same */
            presentSelectedEntity = selectedEntity

            /** Check previous and present geometry, and launch the appropriate
             * function */
            if (
              previousTypeGeometry === 'Point' &&
              presentTypeGeometry === 'Point'
            ) {
              /** Check previous and present symbology type */
              if (
                previousSelectedEntity.entityProperties.typeSymbology ===
                  'color' &&
                presentSelectedEntity.entityProperties.typeSymbology === 'color'
              ) {
                /** Check if the selected entity is the same */
                if (previousSelectedEntity.id === presentSelectedEntity.id) {
                  /** First, reset the previous selected point entity color */
                  previousSelectedEntity.point.color = Cesium.Color.fromCssColorString(
                    previousSelectedEntity.entitySymbology.color
                  ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

                  /** Next, deselect entity and clear variables */
                  viewer.selectedEntity = undefined
                  presentSelectedEntity = undefined
                  presentTypeGeometry = undefined
                  previousSelectedEntity = undefined
                  previousTypeGeometry = undefined
                } else {
                  fromEntityPointColorToEntityPointColor(
                    previousSelectedEntity,
                    presentSelectedEntity,
                    selectedColor,
                    selectedAlpha
                  )
                }
              } else if (
                previousSelectedEntity.entityProperties.typeSymbology ===
                  'color' &&
                presentSelectedEntity.entityProperties.typeSymbology ===
                  'billboard'
              ) {
                fromEntityPointColorToEntityPointBillboard(
                  previousSelectedEntity,
                  presentSelectedEntity
                )
              } else if (
                previousSelectedEntity.entityProperties.typeSymbology ===
                  'billboard' &&
                presentSelectedEntity.entityProperties.typeSymbology === 'color'
              ) {
                fromEntityPointBillboardToEntityPointColor(
                  previousSelectedEntity,
                  presentSelectedEntity,
                  selectedColor,
                  selectedAlpha
                )
              } else if (
                previousSelectedEntity.entityProperties.typeSymbology ===
                  'billboard' &&
                presentSelectedEntity.entityProperties.typeSymbology ===
                  'billboard'
              ) {
                /** Check if the selected entity is the same */
                if (previousSelectedEntity.id === presentSelectedEntity.id) {
                  /** First, reset the previous selected point entity color */
                  previousSelectedEntity.billboard._image._value =
                    previousSelectedEntity.entitySymbology.billboardDefault

                  /** Next, deselect entity and clear variables */
                  viewer.selectedEntity = undefined
                  presentSelectedEntity = undefined
                  presentTypeGeometry = undefined
                  previousSelectedEntity = undefined
                  previousTypeGeometry = undefined
                } else {
                  fromEntityPointBillboardToEntityPointBillboard(
                    previousSelectedEntity,
                    presentSelectedEntity
                  )
                }
              }
            } else if (
              previousTypeGeometry === 'Point' &&
              presentTypeGeometry === 'LineString'
            ) {
              if (
                previousSelectedEntity.entityProperties.typeSymbology ===
                'billboard'
              ) {
                fromEntityPointBillboardToEntityLineStringColor(
                  previousSelectedEntity,
                  presentSelectedEntity,
                  selectedColor,
                  selectedAlpha
                )
              } else {
                fromEntityPointColorToEntityLineStringColor(
                  previousSelectedEntity,
                  presentSelectedEntity,
                  selectedColor,
                  selectedAlpha
                )
              }
            } else if (
              previousTypeGeometry === 'Point' &&
              presentTypeGeometry === 'Polygon'
            ) {
              if (
                previousSelectedEntity.entityProperties.typeSymbology ===
                'billboard'
              ) {
                fromEntityPointBillboardToEntityPolygonColor(
                  previousSelectedEntity,
                  presentSelectedEntity,
                  selectedColor,
                  selectedAlpha
                )
              } else {
                fromEntityPointColorToEntityPolygonColor(
                  previousSelectedEntity,
                  presentSelectedEntity,
                  selectedColor,
                  selectedAlpha
                )
              }
            } else if (
              previousTypeGeometry === 'LineString' &&
              presentTypeGeometry === 'Point'
            ) {
              if (
                presentSelectedEntity.entityProperties.typeSymbology ===
                'billboard'
              ) {
                fromEntityLineStringColorToEntityPointBillboard(
                  previousSelectedEntity,
                  presentSelectedEntity
                )
              } else {
                fromEntityLineStringColorToEntityPointColor(
                  previousSelectedEntity,
                  presentSelectedEntity,
                  selectedColor,
                  selectedAlpha
                )
              }
            } else if (
              previousTypeGeometry === 'LineString' &&
              presentTypeGeometry === 'LineString'
            ) {
              /** Check if the selected line entity is the same */
              if (previousSelectedEntity.id === presentSelectedEntity.id) {
                /** First, reset the previous selected line entity color */
                previousSelectedEntity.polyline.material.color = Cesium.Color.fromCssColorString(
                  previousSelectedEntity.entitySymbology.color
                ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

                /** Next, deselect entity and clear variables */
                viewer.selectedEntity = undefined
                presentSelectedEntity = undefined
                presentTypeGeometry = undefined
                previousSelectedEntity = undefined
                previousTypeGeometry = undefined
              } else {
                fromEntityLineStringColorToEntityLineStringColor(
                  previousSelectedEntity,
                  presentSelectedEntity,
                  selectedColor,
                  selectedAlpha
                )
              }
            } else if (
              previousTypeGeometry === 'LineString' &&
              presentTypeGeometry === 'Polygon'
            ) {
              fromEntityLineStringColorToEntityPolygonColor(
                previousSelectedEntity,
                presentSelectedEntity,
                selectedColor,
                selectedAlpha
              )
            } else if (
              previousTypeGeometry === 'Polygon' &&
              presentTypeGeometry === 'Point'
            ) {
              if (
                presentSelectedEntity.entityProperties.typeSymbology ===
                'billboard'
              ) {
                fromEntityPolygonColorToEntityPointBillboard(
                  previousSelectedEntity,
                  presentSelectedEntity
                )
              } else {
                fromEntityPolygonColorToEntityPointColor(
                  previousSelectedEntity,
                  presentSelectedEntity,
                  selectedColor,
                  selectedAlpha
                )
              }
            } else if (
              previousTypeGeometry === 'Polygon' &&
              presentTypeGeometry === 'LineString'
            ) {
              fromEntityPolygonColorToEntityLineStringColor(
                previousSelectedEntity,
                presentSelectedEntity,
                selectedColor,
                selectedAlpha
              )
            } else if (
              previousTypeGeometry === 'Polygon' &&
              presentTypeGeometry === 'Polygon'
            ) {
              if (previousSelectedEntity.id === presentSelectedEntity.id) {
                /** First, reset the previous selected line entity color */
                previousSelectedEntity.polygon.material.color = Cesium.Color.fromCssColorString(
                  previousSelectedEntity.entitySymbology.color
                ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

                /** Next, deselect entity and clear variables */
                viewer.selectedEntity = undefined
                presentSelectedEntity = undefined
                presentTypeGeometry = undefined
                previousSelectedEntity = undefined
                previousTypeGeometry = undefined
              } else {
                fromEntityPolygonColorToEntityPolygonColor(
                  previousSelectedEntity,
                  presentSelectedEntity,
                  selectedColor,
                  selectedAlpha
                )
              }
            }

            /** After changing colors, set the previous selected entity as the
             * actual one */
            previousSelectedEntity = presentSelectedEntity

            /** Also update the geometry type */
            previousTypeGeometry = presentTypeGeometry
          }
        } else {
          /** If the entity don't allows to be selected, then set the selected
           * entity as undefined */
          viewer.selectedEntity = undefined
        }
      }
      // else if () {
      //   /** Aquí vendran los primitivos y todo eso */
      // }
      else {
        /** Check if there's a entity selected and defined */
        if (presentSelectedEntity) {
          /** RESET THE SYMBOLOGY */

          /** Check the type of entity selected */
          if (presentTypeGeometry === 'Point') {
            /** Check the symbology type */
            if (
              presentSelectedEntity.entityProperties.typeSymbology ===
              'billboard'
            ) {
              /** Reset the default point billboard */
              presentSelectedEntity.billboard._image._value =
                presentSelectedEntity.entitySymbology.billboardSelected
            } else {
              /** Reset the default point color */
              presentSelectedEntity.point.color = Cesium.Color.fromCssColorString(
                presentSelectedEntity.entitySymbology.color
              ).withAlpha(presentSelectedEntity.entitySymbology.colorAlpha)
            }
          } else if (presentTypeGeometry === 'LineString') {
            /** Reset the default line color */
            presentSelectedEntity.polyline.material.color = Cesium.Color.fromCssColorString(
              presentSelectedEntity.entitySymbology.color
            ).withAlpha(presentSelectedEntity.entitySymbology.colorAlpha)
          } else if (presentTypeGeometry === 'Polygon') {
            /** Reset the default polygon color */
            presentSelectedEntity.polygon.material.color = Cesium.Color.fromCssColorString(
              presentSelectedEntity.entitySymbology.color
            ).withAlpha(presentSelectedEntity.entitySymbology.colorAlpha)
          }

          /** Clear the variables */
          presentSelectedEntity = undefined
          presentTypeGeometry = undefined
          previousSelectedEntity = undefined
          previousTypeGeometry = undefined
        }
      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
  }

  function multiSelection() {
    console.log('Nothing to see here... yet')
  }

  /** SUB FUNCTIONS */

  /** NEW SELECTION */

  /** Change the billboard of the first point selected */
  function newSelectionPointBillboard() {
    /** Get parameters through arguments from main function */
    let presentSelectedEntity = arguments[0]

    /** Change the billboard of the point to the selected one */
    presentSelectedEntity.billboard._image._value =
      presentSelectedEntity.entitySymbology.billboardSelected
  }

  /** Change the color of the first point selected */
  function newSelectionPointColor() {
    /** Get parameters through arguments from main function */
    let presentSelectedEntity = arguments[0]
    let selectedColor = arguments[1]
    let selectedAlpha = arguments[2]

    /** Change the color of the point to the selected one */
    presentSelectedEntity.point.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change the color of the first line selected */
  function newSelectionLineStringColor() {
    /** Get parameters through arguments from main function */
    let presentSelectedEntity = arguments[0]
    let selectedColor = arguments[1]
    let selectedAlpha = arguments[2]

    /** Change the color of the line to the selected one */
    presentSelectedEntity.polyline.material.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change the color of the first polygon selected */
  function newSelectionPolygonColor() {
    /** Get parameters through arguments from main function */
    let presentSelectedEntity = arguments[0]
    let selectedColor = arguments[1]
    let selectedAlpha = arguments[2]

    /** Change the color of the polygon to the selected one */
    presentSelectedEntity.polygon.material.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** FROM PREVIOUS SELECTION */

  /** Change color from point to point */
  function fromEntityPointColorToEntityPointColor() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]
    let selectedColor = arguments[2]
    let selectedAlpha = arguments[3]

    /** First, reset the previous selected point entity color */
    previousSelectedEntity.point.color = Cesium.Color.fromCssColorString(
      previousSelectedEntity.entitySymbology.color
    ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

    /** Then, change the new selected point entity color */
    presentSelectedEntity.point.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change billboard point from color point */
  function fromEntityPointColorToEntityPointBillboard() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]

    /** First, reset the previous selected point entity color */
    previousSelectedEntity.point.color = Cesium.Color.fromCssColorString(
      previousSelectedEntity.entitySymbology.color
    ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

    /** Then, change the new selected point entity billboard */
    presentSelectedEntity.billboard._image._value =
      presentSelectedEntity.entitySymbology.billboardSelected
  }

  /** Change color point from billboard point */
  function fromEntityPointBillboardToEntityPointColor() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]
    let selectedColor = arguments[2]
    let selectedAlpha = arguments[3]

    /** First, reset the previous selected point entity billboard */
    previousSelectedEntity.billboard._image._value =
      previousSelectedEntity.entitySymbology.billboardDefault

    /** Then, change the new selected point entity color */
    presentSelectedEntity.point.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change billboard point from billboard point */
  function fromEntityPointBillboardToEntityPointBillboard() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]

    /** First, reset the previous selected point entity color */
    previousSelectedEntity.billboard._image._value =
      previousSelectedEntity.entitySymbology.billboardDefault

    /** Then, change the new selected point entity billboard */
    presentSelectedEntity.billboard._image._value =
      presentSelectedEntity.entitySymbology.billboardSelected
  }

  /** Change color line from billboard point */
  function fromEntityPointBillboardToEntityLineStringColor() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]
    let selectedColor = arguments[2]
    let selectedAlpha = arguments[3]

    /** First, reset the previous selected point entity color */
    previousSelectedEntity.billboard._image._value =
      previousSelectedEntity.entitySymbology.billboardDefault

    /** Then, change the new selected line entity color */
    presentSelectedEntity.polyline.material.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change color line from color point */
  function fromEntityPointColorToEntityLineStringColor() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]
    let selectedColor = arguments[2]
    let selectedAlpha = arguments[3]

    /** First, reset the previous selected point entity color */
    previousSelectedEntity.point.color = Cesium.Color.fromCssColorString(
      previousSelectedEntity.entitySymbology.color
    ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

    /** Then, change the new selected line entity color */
    presentSelectedEntity.polyline.material.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change color polygon from billboard point */
  function fromEntityPointBillboardToEntityPolygonColor() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]
    let selectedColor = arguments[2]
    let selectedAlpha = arguments[3]

    /** First, reset the previous selected point entity color */
    previousSelectedEntity.billboard._image._value =
      previousSelectedEntity.entitySymbology.billboardDefault

    /** Then, change the new selected line entity color */
    presentSelectedEntity.polygon.material.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change color polygon from color point */
  function fromEntityPointColorToEntityPolygonColor() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]
    let selectedColor = arguments[2]
    let selectedAlpha = arguments[3]

    /** First, reset the previous selected point entity color */
    previousSelectedEntity.point.color = Cesium.Color.fromCssColorString(
      previousSelectedEntity.entitySymbology.color
    ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

    /** Then, change the new selected line entity color */
    presentSelectedEntity.polygon.material.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change color polygon from color point */
  function fromEntityLineStringColorToEntityPointBillboard() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]

    /** First, reset the previous selected line entity color */
    previousSelectedEntity.polyline.material.color = Cesium.Color.fromCssColorString(
      previousSelectedEntity.entitySymbology.color
    ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

    /** Then, change the new selected point billboard color */
    presentSelectedEntity.billboard._image._value =
      presentSelectedEntity.entitySymbology.billboardSelected
  }

  /** Change color polygon from color point */
  function fromEntityLineStringColorToEntityPointColor() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]
    let selectedColor = arguments[2]
    let selectedAlpha = arguments[3]

    /** First, reset the previous selected line entity color */
    previousSelectedEntity.polyline.material.color = Cesium.Color.fromCssColorString(
      previousSelectedEntity.entitySymbology.color
    ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

    /** Then, change the new selected line entity color */
    presentSelectedEntity.point.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change color line from color line */
  function fromEntityLineStringColorToEntityLineStringColor() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]
    let selectedColor = arguments[2]
    let selectedAlpha = arguments[3]

    /** First, reset the previous selected line entity color */
    previousSelectedEntity.polyline.material.color = Cesium.Color.fromCssColorString(
      previousSelectedEntity.entitySymbology.color
    ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

    /** Then, change the new selected line entity color */
    presentSelectedEntity.polyline.material.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change color polygon from color line */
  function fromEntityLineStringColorToEntityPolygonColor() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]
    let selectedColor = arguments[2]
    let selectedAlpha = arguments[3]

    /** First, reset the previous selected line entity color */
    previousSelectedEntity.polyline.material.color = Cesium.Color.fromCssColorString(
      previousSelectedEntity.entitySymbology.color
    ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

    /** Then, change the new selected polygon entity color */
    presentSelectedEntity.polygon.material.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change color polygon from color line */
  function fromEntityPolygonColorToEntityPointBillboard() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]

    /** First, reset the previous selected polygon entity color */
    previousSelectedEntity.polygon.material.color = Cesium.Color.fromCssColorString(
      previousSelectedEntity.entitySymbology.color
    ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

    /** Then, change the new selected point billboard color */
    presentSelectedEntity.billboard._image._value =
      presentSelectedEntity.entitySymbology.billboardSelected
  }

  /** Change color polygon from color line */
  function fromEntityPolygonColorToEntityPointColor() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]
    let selectedColor = arguments[2]
    let selectedAlpha = arguments[3]

    /** First, reset the previous selected polygon entity color */
    previousSelectedEntity.polygon.material.color = Cesium.Color.fromCssColorString(
      previousSelectedEntity.entitySymbology.color
    ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

    /** Then, change the new selected line entity color */
    presentSelectedEntity.point.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change color polygon from color line */
  function fromEntityPolygonColorToEntityLineStringColor() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]
    let selectedColor = arguments[2]
    let selectedAlpha = arguments[3]

    /** First, reset the previous selected polygon entity color */
    previousSelectedEntity.polygon.material.color = Cesium.Color.fromCssColorString(
      previousSelectedEntity.entitySymbology.color
    ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

    /** Then, change the new selected polygon entity color */
    presentSelectedEntity.polyline.material.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }

  /** Change color polygon from color line */
  function fromEntityPolygonColorToEntityPolygonColor() {
    /** Get parameters through arguments from main function */
    let previousSelectedEntity = arguments[0]
    let presentSelectedEntity = arguments[1]
    let selectedColor = arguments[2]
    let selectedAlpha = arguments[3]

    /** First, reset the previous selected polygon entity color */
    previousSelectedEntity.polygon.material.color = Cesium.Color.fromCssColorString(
      previousSelectedEntity.entitySymbology.color
    ).withAlpha(previousSelectedEntity.entitySymbology.colorAlpha)

    /** Then, change the new selected polygon entity color */
    presentSelectedEntity.polygon.material.color = Cesium.Color.fromCssColorString(
      selectedColor
    ).withAlpha(selectedAlpha)
  }
}

/** END FEATURE SELECTION */
 

/** CAMERA TO POSITION */

/** Move the camera to the selected position */
function cameraToPosition(
	zoomType,
	dataType,
	param1,
	param2,
	param3,
	param4,
	param5,
	param6
) {
	switch (dataType) {
		case 'cartesian':
			cameraToCartesian(zoomType, param1, param2, param3, param4, param5)
			break

		case 'degrees':
			cameraToDegrees(zoomType, param1, param2, param3, param4, param5, param6)
			break
	}
}

/** This function move the user camera to position defined in Cartesian3 */
function cameraToCartesian() {
	/** Get parameters through arguments from main function */
	let zoomType = arguments[0]
	let position = arguments[1]
	let height = arguments[2]
	let heading = arguments[3]
	let pitch = arguments[4]
	let roll = arguments[5]

	/** Set default values. TODO: from function */
	if (height === '') {
		height = 5000
	}
	if (heading === '') {
		heading = 0
	}
	if (pitch === '') {
		pitch = -90
	}
	if (roll === '') {
		roll = 0
	}

	/** Define the longitude value from the cartesian position */
	let longitude = Cesium.Math.toDegrees(
		Cesium.Ellipsoid.WGS84.cartesianToCartographic(position).longitude
	)

	/** Define the latitude value from the cartesian position */
	let latitude = Cesium.Math.toDegrees(
		Cesium.Ellipsoid.WGS84.cartesianToCartographic(position).latitude
	)

	/** Check the type of zoom to do */
	if (zoomType === 'fly') {
		/** Move the camera to the position with the flyTo effect */
		viewer.camera.flyTo({
			destination: Cesium.Cartesian3.fromDegrees(longitude, latitude, height),
			orientation: {
				heading: heading,
				pitch: Cesium.Math.toRadians(pitch),
				roll: roll
			}
		})
	} else {
		/** Move the camera to the position with the zoomTo effect */
		viewer.camera.setView({
			destination: Cesium.Cartesian3.fromDegrees(longitude, latitude, height),
			orientation: {
				heading: heading,
				pitch: Cesium.Math.toRadians(pitch),
				roll: roll
			}
		})
	}
}

/** This function move the user camera to position defined in degrees */
function cameraToDegrees() {
	/** Get parameters through arguments from main function */
	let zoomType = arguments[0]
	let longitude = arguments[1]
	let latitude = arguments[2]
	let height = arguments[3]
	let heading = arguments[4]
	let pitch = arguments[5]
	let roll = arguments[6]

	/** Set default values. TODO: from function */
	if (height === '') {
		height = 5000
	}
	if (heading === '') {
		heading = 0
	}
	if (pitch === '') {
		pitch = -90
	}
	if (roll === '') {
		roll = 0
	}

	/** Check the zoom type */
	if (zoomType === 'fly') {
		/** Move the camera to the position with the flyTo effect */
		viewer.camera.flyTo({
			destination: Cesium.Cartesian3.fromDegrees(longitude, latitude, height),
			orientation: {
				heading: heading,
				pitch: Cesium.Math.toRadians(pitch),
				roll: roll
			}
		})
	} else {
		viewer.camera.setView({
			destination: Cesium.Cartesian3.fromDegrees(longitude, latitude, height),
			orientation: {
				heading: heading,
				pitch: Cesium.Math.toRadians(pitch),
				roll: roll
			}
		})
	}
}

/** END OF CAMERA TO POSITION */

/** */

/** CREATE LAYER */

/** Function management */
function createLayer(
	data,
	allowPicking,
	geometryType,
	geometryClass,
	symbologyType,
	param1 = null,
	param2 = null,
	param3 = null,
	param4 = null,
	param5 = null,
	param6 = null,
	param7 = null,
	param8 = null,
	param9 = null
) {
	/** Check the kind of geometry */
	switch (geometryType) {
		/** Point geometry */
		case 'point':
			/** Check the kind of geometry class */
			switch (geometryClass) {
				/** ENTITIES */
				case 'entity':
					/** Check the kind of symbology to use */
					switch (symbologyType) {
						/** Basic color symbology */
						case 'color':
							pointEntityColorLayer(
								data,
								allowPicking,
								param1,
								param2,
								param3,
								param4,
								param5,
								param6
							)
							break
					}
					break
			}
			break

		/** Polyline geometry */
		case 'lineString':
			/** Check the kind of geometry class */
			switch (geometryClass) {
				/** ENTITIES */
				case 'entity':
					/** Check the kind of symbology to use */
					switch (symbologyType) {
						/** Basic color symbology */
						case 'color':
							lineStringEntityColorLayer(
								data,
								allowPicking,
								param1,
								param2,
								param3
							)
							break
					}
					break
			}
			break

		/** Polygon geometry */
		case 'polygon':
			/** Check the kind of geometry class */
			switch (geometryClass) {
				/** ENTITIES */
				case 'entity':
					/** Check the kind of symbology to use */
					switch (symbologyType) {
						case 'color':
							polygonEntityColorLayer(data, allowPicking, param1, param2)
							break
					}
					break
			}
			break
		/** Raster */
		case 'raster':
			/** Check the kind of geometry class */
			switch (geometryClass) {
				case 'heatmap':
					/** Check the kind of symbology to use */
					switch (symbologyType) {
						case 'color':
							createRasterHeatmapColor(
								data,
								allowPicking,
								param1,
								param2,
								param3,
								param4,
								param5,
								param6,
								param7,
								param8
							)
							break
					}
					break
			}
			break
	}
}

/** GENERIC */

/** Function that gets the value of a property from its key name */
function findValueFromKeyName(data, fieldName) {
	/** Look for the field name through the object keys (recursively) */
	Object.keys(data["properties"]).some(function(keyName) {
		if (keyName === fieldName) {
			fieldValue = data["properties"][keyName]
		}

		/** Check if the key is an object or not; in this case, repeat */
		if (data["properties"][keyName] && typeof data["properties"][keyName] === 'object') {
			/** Return the function, 'cause recursive */
			return findValueFromKeyName(data["properties"][keyName], fieldName)
		}
	})

	/** Return the value of the selected field */
	return fieldValue
}

/** Function that add all properties into an object */
function addProperties(feature) {
	/** Get the content of the object */
	let propertyEntry = Object.entries(feature.properties)

	/** Set an object where load the object properties */
	let propertyObject = {}

	/** Iterate over the content object */
	propertyEntry.forEach(entry => {
		/** Generate the key and value of the object */
		propertyObject[entry[0]] = entry[1]
	})

	/** Return the object */
	return propertyObject
}


/** VECTOR - POINTS - ENTITIES */

/** Function that creates a point layer with basic color points as symbology */
function pointEntityColorLayer() {
	/** Get parameters through arguments from main function */
	let data = arguments[0]
	let allowPicking = arguments[1]
	let pixelSize = arguments[2]
	let color = arguments[3]
	let colorAlpha = arguments[4]
	let outlineWidth = arguments[5]
	let outlineColor = arguments[6]
	let outlineColorAlpha = arguments[7]

	/** Check if data comes in FeatureCollection format. If not launch an error */
	if (data.hasOwnProperty('type') && data.type === 'FeatureCollection') {
		/** Set properties from the data */
		let layerName
		let layerNameProcessed

		/** Check if there's a 'name' property in the incoming data, and is valid */
		if (data.hasOwnProperty('name') && data.name != '') {
			/** Set the layer name for the layer selector */
			layerName = data.name

			/** Set the processed layer name from the 'name' property of the data.
			 * Make the first character as upper case, and join the name if has
			 * several strings */
			let layerNameSplit = layerName.split(/\s/g)
			let arrayNames = []

			/** Iterate over the names to uppercase its first character */
			layerNameSplit.forEach(function(name) {
				arrayNames.push(name.charAt(0).toUpperCase() + name.slice(1))
			})

			/** Define the layer name from the join */
			layerNameProcessed = arrayNames.join('')

			/** Set the dataSource */
			let dataSource

			/** Define the future name of the dataSource */
			let dataSourceName = 'dataSource' + layerNameProcessed

			/** Add the layer to the layer list */
			dataSourceLayers.push(layerNameProcessed)

			/** Check if there's already a dataSource with this name. First, look if
			 * there're existing dataSources and if so, iterate over them and look
			 * for one with the same name */
			if (viewer.dataSources._dataSources.length > 0) {
				viewer.dataSources._dataSources.forEach(function(viewerDataSource) {
					/** Double check 'cause the dataSource setup can be a little tricky */
					if (
						(viewerDataSource.hasOwnProperty('name') &&
							viewerDataSource.name === dataSourceName) ||
						(viewerDataSource.name.hasOwnProperty('name') &&
							viewerDataSource.name.name === dataSourceName)
					) {
						/** If there's a dataSource with that name, reutilize it */
						dataSource = viewerDataSource
					}
				})
			}

			/** If the dataSource has not been found, then create a new one */
			if (!dataSource) {
				/** Create the new custom dataSource */
				dataSource = new Cesium.CustomDataSource({
					name: dataSourceName,
					layerName: layerName
				})

				/** By default, the dataSource will be hide */
				// dataSource.show = false

				/** Add the dataSource to the viewer */
				viewer.dataSources.add(dataSource)
			}
			
			/** Clear the dataSource */
			dataSource.entities.removeAll()

			/** Iterate over all entities in the FeatureCollection */
			data.features.forEach(function(feature) {
				/** Set a default ID and name of each feature */
				let id
				let name

				/** If ID and/or name are declared in the data properties, then
				 * use them */
				if (
					feature.hasOwnProperty('properties') &&
					feature.properties.hasOwnProperty('id')
				) {
					id = feature.properties.id
				}

				if (
					feature.hasOwnProperty('properties') &&
					feature.properties.hasOwnProperty('name')
				) {
					name = feature.properties.name
				}

				/** Check if the feature geometry type is Simple or Multi */
				if (feature.geometry.type === 'Point') {
					/** Set the position of the feature */
					let longitude = feature.geometry.coordinates[0]
					let latitude = feature.geometry.coordinates[1]

					/** Add the feature as a entity to the dataSource */
					dataSource.entities.add({
						description: fakeInfoBox(feature),
						entityProperties: {
							id: id,
							name: name,
							layerName: layerName,
							allowPicking: allowPicking,
							typeGeometry: {
								type: 'Point',
								class: 'Point'
							},
							typeSymbology: 'color',
							parentDataSource: dataSourceName
						},
						entitySymbology: {
							pixelSize: pixelSize,
							color: color,
							colorAlpha: colorAlpha,
							outlineColor: outlineColor,
							outlineColorAlpha: outlineColorAlpha,
							outlineWidth: outlineWidth
						},
						position: Cesium.Cartesian3.fromDegrees(longitude, latitude),
						point: {
							pixelSize: pixelSize,
							color: Cesium.Color.fromCssColorString(color).withAlpha(
								colorAlpha
							),
							outlineWidth: outlineWidth,
							outlineColor: Cesium.Color.fromCssColorString(
								outlineColor
							).withAlpha(outlineColorAlpha)
						}
					})
				} else if (feature.geometry.type === 'MultiPoint') {
					/** Get the array of coordinates from each multipoint points */
					let arrayCoordinates = feature.geometry.coordinates

					/** Iterate over the array of multipoint points */
					arrayCoordinates.forEach(function(coordinates) {
						let longitude = coordinates[0]
						let latitude = coordinates[1]

						/** Add the feature as a entity to the dataSource */
						dataSource.entities.add({
							entityProperties: {
								id: id,
								name: name,
								layerName: layerName,
								allowPicking: allowPicking,
								properties: addProperties(feature),
								typeGeometry: {
									type: 'Point',
									class: 'Multipoint'
								},
								typeSymbology: 'color',
								parentDataSource: dataSourceName
							},
							entitySymbology: {
								pixelSize: pixelSize,
								color: color,
								colorAlpha: colorAlpha,
								outlineColor: outlineColor,
								outlineColorAlpha: outlineColorAlpha,
								outlineWidth: outlineWidth
							},
							position: Cesium.Cartesian3.fromDegrees(longitude, latitude),
							point: {
								pixelSize: pixelSize,
								color: Cesium.Color.fromCssColorString(color).withAlpha(
									colorAlpha
								),
								outlineWidth: outlineWidth,
								outlineColor: Cesium.Color.fromCssColorString(
									outlineColor
								).withAlpha(outlineColorAlpha)
							}
						})
					})
				} else {
					/** Tell the user that the geometry is not recognized. */
					console.error(
						'There geometry of one of the points is not recognized. The ' +
							'point will not be represented. Please make sure that the' +
							'type of geometry is Point or Multipoint.' +
							'Error catched from: ' +
							'%cpointEntityColorLayer.createLayer()',
						'font-weight: bold'
					)
				}
			})
		} else {
			/** Tell the user there is no 'name' property defined in the data */
			console.error(
				"There isnt a name field defined for the data. Please make sure is " +
					'correctly defined in the JSON schema.' +
					'Error catched from: ' +
					'%cpointEntityColorLayer.createLayer()',
				'font-weight: bold'
			)
		}
	} else {
		/** Tell the user there is no 'name' property defined in the data */
		console.error(
			'Data schema differs from a FeatureCollection type schema. Please, ' +
				'check the data incoming service and make sure the XHR response ' +
				'follows a FeatureCollection response.' +
				'A FeatureCollection schema example can be found at: ' +
				'http://geojson.org/schema/FeatureCollection.json' +
				'Error catched from: ' +
				'%cpointEntityColorLayer.createLayer()',
			'font-weight: bold'
		)
	}
}

/** VECTOR - POLYLINES - ENTITIES */

/** Function that creates a line layer with basic color as symbology */
function lineStringEntityColorLayer() {
	let data = arguments[0]
	let allowPicking = arguments[1]
	let width = arguments[2]
	let color = arguments[3]
	let colorAlpha = arguments[4]

	/** Check if data comes in FeatureCollection format. If not launch an error */
	if (data.hasOwnProperty('type') && data.type === 'FeatureCollection') {
		/** Set properties from the data */
		let layerName
		let layerNameProcessed

		/** Check if there's a 'name' property in the incoming data, and is valid */
		if (data.hasOwnProperty('name') && data.name != '') {
			/** Set the layer name for the layer selector */
			layerName = data.name

			/** Set the processed layer name from the 'name' property of the data.
			 * Make the first character as upper case, and join the name if has
			 * several strings */
			let layerNameSplit = layerName.split(/\s/g)
			let arrayNames = []

			/** Iterate over the names to uppercase its first character */
			layerNameSplit.forEach(function(name) {
				arrayNames.push(name.charAt(0).toUpperCase() + name.slice(1))
			})

			/** Define the layer name from the join */
			layerNameProcessed = arrayNames.join('')

			/** Set the dataSource */
			let dataSource

			/** Define the future name of the dataSource */
			let dataSourceName = 'dataSource' + layerNameProcessed

			/** Add the layer to the layer list */
			dataSourceLayers.push(layerNameProcessed)

			/** Check if there's already a dataSource with this name. First, look if
			 * there're existing dataSources and if so, iterate over them and look
			 * for one with the same name */
			if (viewer.dataSources._dataSources.length > 0) {
				viewer.dataSources._dataSources.forEach(function(viewerDataSource) {
					/** Double check 'cause the dataSource setup can be a little tricky */
					if (
						viewerDataSource.name === dataSourceName ||
						viewerDataSource.name.name === dataSourceName
					) {
						/** If there's a dataSource with that name, reutilize it */
						dataSource = viewerDataSource
					}
				})
			}

			/** If the dataSource has not been found, then create a new one */
			if (!dataSource) {
				/** Create the new custom dataSource */
				dataSource = new Cesium.CustomDataSource({
					name: dataSourceName,
					layerName: layerName
				})

				/** By default, the dataSource will be hide */
				// dataSource.show = false

				/** Add the dataSource to the viewer */
				viewer.dataSources.add(dataSource)
			}

			/** Iterate over all entities in the FeatureCollection */
			data.features.forEach(function(feature) {
				/** Set a default ID and name of each feature */
				let id
				let name

				/** If ID and/or name are declared in the data properties, then
				 * use them */
				if (
					feature.hasOwnProperty('properties') &&
					feature.properties.hasOwnProperty('id')
				) {
					id = feature.properties.id
				}

				if (
					feature.hasOwnProperty('properties') &&
					feature.properties.hasOwnProperty('name')
				) {
					name = feature.properties.name
				}

				/** Check if the feature geometry type is Simple or Multi */
				if (feature.geometry.type === 'LineString') {
					/** Define an array of coordinates for each feature */
					let arrayList = []

					/** Iterate over all the coordinates elements, and add them to the
					 * array */
					feature.geometry.coordinates.forEach(arrayPolyline => {
						arrayList.push(arrayPolyline[0])
						arrayList.push(arrayPolyline[1])
					})

					/** Add the feature as a entity to the dataSource */
					dataSource.entities.add({
						entityProperties: {
							id: id,
							name: name,
							layerName: layerName,
							allowPicking: allowPicking,
							properties: addProperties(feature),
							typeGeometry: {
								type: 'LineString',
								class: 'LineString'
							},
							parentDataSource: dataSourceName
						},
						entitySymbology: {
							width: width,
							color: color,
							colorAlpha: colorAlpha
						},
						polyline: {
							positions: Cesium.Cartesian3.fromDegreesArray(arrayList),
							width: width,
							material: Cesium.Color.fromCssColorString(color).withAlpha(
								colorAlpha
							)
						}
					})
				} else if (feature.geometry.type === 'MultiLineString') {
					/** Define an array of coordinates for each feature */
					let arrayList = []

					/** Iterate over all the coordinates elements, and add them to the
					 * array */
					feature.geometry.coordinates.forEach(arrayMultiPolyline => {
						arrayMultiPolyline.forEach(arrayPolyline => {
							arrayList.push(arrayPolyline[0])
							arrayList.push(arrayPolyline[1])
						})
					})

					/** Add the feature as a entity to the dataSource */
					dataSource.entities.add({
						entityProperties: {
							id: id,
							name: name,
							layerName: layerName,
							allowPicking: allowPicking,
							properties: addProperties(feature),
							typeGeometry: {
								type: 'LineString',
								class: 'LineString'
							},
							parentDataSource: dataSourceName
						},
						entitySymbology: {
							width: width,
							color: color,
							colorAlpha: colorAlpha
						},
						polyline: {
							positions: Cesium.Cartesian3.fromDegreesArray(arrayList),
							width: width,
							material: Cesium.Color.fromCssColorString(color).withAlpha(
								colorAlpha
							)
						}
					})
				} else {
					/** Tell the user that the geometry is not recognized. */
					console.error(
						'There geometry of one of the lines is not recognized. The ' +
							'point will not be represented. Please make sure that the' +
							'type of geometry is LineString or MultiLineString.' +
							'Error catched from: ' +
							'%cpointEntityColorLayer.createLayer()',
						'font-weight: bold'
					)
				}
			})
		} else {
			/** Tell the user there is no 'name' property defined in the data */
			console.error(
				"There isnt a name field defined for the data. Please make sure is " +
					'correctly defined in the JSON schema.' +
					'Error catched from: ' +
					'%cpointEntityColorLayer.createLayer()',
				'font-weight: bold'
			)
		}
	} else {
		/** Tell the user there is no 'name' property defined in the data */
		console.error(
			'Data schema differs from a FeatureCollection type schema. Please, ' +
				'check the data incoming service and make sure the XHR response ' +
				'follows a FeatureCollection response.' +
				'A FeatureCollection schema example can be found at: ' +
				'http://geojson.org/schema/FeatureCollection.json' +
				'Error catched from: ' +
				'%cpointEntityColorLayer.createLayer()',
			'font-weight: bold'
		)
	}
}

/** VECTOR - POLYGON - ENTITIES */

/** Function that creates a line layer with basic color as symbology */
function polygonEntityColorLayer() {
	let data = arguments[0]
	let allowPicking = arguments[1]
	let color = arguments[2]
	let colorAlpha = arguments[3]

	/** Check if data comes in FeatureCollection format. If not launch an error */
	if (data.hasOwnProperty('type') && data.type === 'FeatureCollection') {
		/** Set properties from the data */
		let layerName
		let layerNameProcessed

		/** Check if there's a 'name' property in the incoming data, and is valid */
		if (data.hasOwnProperty('name') && data.name != '') {
			/** Set the layer name for the layer selector */
			layerName = data.name

			/** Set the processed layer name from the 'name' property of the data.
			 * Make the first character as upper case, and join the name if has
			 * several strings */
			let layerNameSplit = layerName.split(/\s/g)
			let arrayNames = []

			/** Iterate over the names to uppercase its first character */
			layerNameSplit.forEach(name => {
				arrayNames.push(name.charAt(0).toUpperCase() + name.slice(1))
			})

			/** Define the layer name from the join */
			layerNameProcessed = arrayNames.join('')

			/** Set the dataSource */
			let dataSource

			/** Define the future name of the dataSource */
			let dataSourceName = 'dataSource' + layerNameProcessed

			/** Add the layer to the layer list */
			dataSourceLayers.push(layerNameProcessed)

			/** Check if there's already a dataSource with this name. First, look if
			 * there're existing dataSources and if so, iterate over them and look
			 * for one with the same name */
			if (viewer.dataSources._dataSources.length > 0) {
				viewer.dataSources._dataSources.forEach(viewerDataSource => {
					/** Double check 'cause the dataSource setup can be a little tricky */
					if (
						viewerDataSource.name === dataSourceName ||
						viewerDataSource.name.name === dataSourceName
					) {
						/** If there's a dataSource with that name, reutilize it */
						dataSource = viewerDataSource
					}
				})
			}

			/** If the dataSource has not been found, then create a new one */
			if (!dataSource) {
				/** Create the new custom dataSource */
				dataSource = new Cesium.CustomDataSource({
					name: dataSourceName,
					layerName: layerName
				})

				/** By default, the dataSource will be hide */
				// dataSource.show = false

				/** Add the dataSource to the viewer */
				viewer.dataSources.add(dataSource)
			}

			/** Iterate over all entities in the FeatureCollection */
			data.features.forEach(feature => {
				/** Set a default ID and name of each feature */
				let id
				let name

				/** If ID and/or name are declared in the data properties, then
				 * use them */
				if (
					feature.hasOwnProperty('properties') &&
					feature.properties.hasOwnProperty('id')
				) {
					id = feature.properties.id
				}

				if (
					feature.hasOwnProperty('properties') &&
					feature.properties.hasOwnProperty('name')
				) {
					name = feature.properties.name
				}

				/** Check if the feature geometry type is Simple or Multi */
				if (feature.geometry.type === 'Polygon') {
					/** Define an array of coordinates for each feature */
					let arrayList = []

					/** Iterate over every polygon coordinates */
					feature.geometry.coordinates.forEach(arrayPolygon => {
						/** Iterate over every couple of coordinate */
						arrayPolygon.forEach(arraySingle => {
							/** Add the coordinates to the array list */
							arrayList.push(arraySingle[0])
							arrayList.push(arraySingle[1])
						})
					})

					/** Add the feature as a entity to the dataSource */
					dataSource.entities.add({
						entityProperties: {
							id: id,
							name: name,
							layerName: layerName,
							allowPicking: allowPicking,
							properties: addProperties(feature),
							typeGeometry: {
								type: 'Polygon',
								class: 'Polygon'
							},
							parentDataSource: dataSourceName
						},
						entitySymbology: {
							color: color,
							colorAlpha: colorAlpha
						},
						polygon: {
							hierarchy: Cesium.Cartesian3.fromDegreesArray(arrayList),
							material: Cesium.Color.fromCssColorString(color).withAlpha(
								colorAlpha
							)
						}
					})
				} else if (feature.geometry.type === 'MultiPolygon') {
					/** Define an array of coordinates for each feature */
					let arrayList = []

					/** Iterate over every multipolygon coordinates */
					feature.geometry.coordinates.forEach(arrayMultiPolygon => {
						/** Iterate over every polygon coordinates */
						arrayMultiPolygon.forEach(arrayPolygon => {
							/** Iterate over every couple of coordinate */
							arrayPolygon.forEach(arraySingle => {
								/** Add the coordinates to the array list */
								arrayList.push(arraySingle[0])
								arrayList.push(arraySingle[1])
							})
						})
					})

					/** Add the feature as a entity to the dataSource */
					dataSource.entities.add({
						entityProperties: {
							id: id,
							name: name,
							layerName: layerName,
							allowPicking: allowPicking,
							properties: addProperties(feature),
							typeGeometry: {
								type: 'Polygon',
								class: 'MultiPolygon'
							},
							parentDataSource: dataSourceName
						},
						entitySymbology: {
							color: color,
							colorAlpha: colorAlpha
						},
						polygon: {
							hierarchy: Cesium.Cartesian3.fromDegreesArray(arrayList),
							material: Cesium.Color.fromCssColorString(color).withAlpha(
								colorAlpha
							)
						}
					})
				} else {
					console.error('Geometría no reconocida')
				}
			})
		} else {
			/** Tell the user there is no 'name' property defined in the data */
			console.error(
				"There isnt a name field defined for the data. Please make sure is " +
					'correctly defined in the JSON schema.' +
					'Error catched from: ' +
					'%cpointEntityColorLayer.createLayer()',
				'font-weight: bold'
			)
		}
	} else {
		/** Tell the user there is no 'name' property defined in the data */
		console.error(
			'Data schema differs from a FeatureCollection type schema. Please, ' +
				'check the data incoming service and make sure the XHR response ' +
				'follows a FeatureCollection response.' +
				'A FeatureCollection schema example can be found at: ' +
				'http://geojson.org/schema/FeatureCollection.json' +
				'Error catched from: ' +
				'%cpointEntityColorLayer.createLayer()',
			'font-weight: bold'
		)
	}
}

/** RASTER - HEATMAPS */

/** Function that create a heatmap, defining gradient color as parameter */
function createRasterHeatmapColor() {
	/** Get parameters through arguments from main function */
	let data = arguments[0]
	let allowPicking = arguments[1]
	let weightField = arguments[2]
	let minIterationValue = arguments[3]
	let maxIterationValue = arguments[4]
	let searchRadius = arguments[5]
	let minOpacity = arguments[6]
	let maxOpacity = arguments[7]
	let blur = arguments[8]
	let gradient = arguments[9]

	/** Set an error controller */
	let error = false

	/** Set a DIV ID storage */
	let heatmapId

	/** Set properties from the data */
	let layerName
	let layerNameProcessed

	/** Set the layer name for the layer selector */
	layerName = data.name

	/** Set the processed layer name from the 'name' property of the data.
	 * Make the first character as upper case, and join the name if has
	 * several strings */
	let layerNameSplit = layerName.split(/\s/g)
	let arrayNames = []

	/** Iterate over the names to uppercase its first character */
	layerNameSplit.forEach(function(name) {
		arrayNames.push(name.charAt(0).toUpperCase() + name.slice(1))
	})

	/** Define the layer name from the join */
	layerNameProcessed = arrayNames.join('')

	/** Add the layer to the layer list */
	dataSourceLayers.push(layerNameProcessed)

	/** Iterates over viewer entities, looking for previous heatmaps */
	viewer.entities.values.forEach(function(entity) {
		/** If the iterated entity has a rectangle property, maybe is a heatmap */
		if (entity.rectangle) {
			/** Check if has the heatmap-canvas tag */
			if (
				entity.rectangle.material.image._value.className === 'heatmap-canvas'
			) {
				/** If so, get the heatmap ID */
				heatmapId = entity.rectangle.material.image._value.id

				/** Remove the entity from viewer */
				viewer.entities.remove(entity)

				/** Get the DIV ID from the heatmap ID */
				let divId = heatmapId.substring(0, 8)

				/** Remove the DIV with this ID */
				document.getElementById(divId).remove()
			}
		}
	})

	/** Function that configure the heatmap properties */
	function setupHeatmap() {
		/** Define the properties of the heatmap */
		let heatmapSetup = {
			//useEntitiesIfAvailable: false,
			radius: searchRadius,
			//scaleRadius: true,
			maxOpacity: maxOpacity,
			minOpacity: minOpacity,
			blur: blur,
			gradient: gradient
		}

		/** Return the configuration */
		return heatmapSetup
	}

	/** Function that get the extent of the data */
	function getExtent() {
		/** Guess that geometry field exist */
		let geometryExist = true

		/** Check if the geometry field is really defined. In this particular case,
		 * if any of the features don't have geometry, will throw an error */
		if ('features' in data) {
			for (let feature of data.features) {
				/** If any feature don't have geometry, change the boolean value */
				if (!feature.hasOwnProperty('geometry')) {
					geometryExist = false
				}
			}
		} else {
			for (let feature of data) {
				/** If any feature don't have geometry, change the boolean value */
				if (!feature.hasOwnProperty('geometry')) {
					geometryExist = false
				}
			}
		}

		/** In case a geometry field is detected, then continue the function */
		if (geometryExist) {
			/** Set the arrays for longitude and latitude values*/
			let longitudeArray = []
			let latitudeArray = []

			/** Set the extreme values for each array */
			let highestLatitude
			let lowestLatitude
			let highestLongitude
			let lowestLongitude

			/** Check if a field called 'features' exist --> FeatureCollection */
			if ('features' in data) {
				try {
					/** Iterate over the data to get the longitude and latitude values */
					for (let feature of data.features) {
						let longitude = feature.geometry.coordinates[0]
						let latitude = feature.geometry.coordinates[1]

						/** Add the values of longitude and latitude to their arrays */
						longitudeArray.push(longitude)
						latitudeArray.push(latitude)
					}
				} catch (error) {
					console.error(
						'There is a problem reading the geometry data of the features: ' +
							error +
							'Error catched from: ' +
							'%cgetExtent().createRasterHeatmapColor()',
						'font-weight: bold'
					)
				}
			} else {
				try {
					/** Iterate over the data to get the longitude and latitude values */
					for (let feature of data) {
						let longitude = feature.geometry.coordinates[0]
						let latitude = feature.geometry.coordinates[1]

						/** Add the values of longitude and latitude to their arrays */
						longitudeArray.push(longitude)
						latitudeArray.push(latitude)
					}
				} catch (error) {
					console.error(
						'There is a problem reading the geometry data: ' +
							error +
							'Error catched from: ' +
							'%cgetExtent().createRasterHeatmapColor()',
						'font-weight: bold'
					)
				}
			}

			/** Get the extreme values of the longitude and latitude arrays */
			highestLongitude = Math.max(...longitudeArray)
			lowestLongitude = Math.min(...longitudeArray)
			highestLatitude = Math.max(...latitudeArray)
			lowestLatitude = Math.min(...latitudeArray)

			/** Set the extent of the data */
			let extent = {
				west: lowestLongitude,
				south: lowestLatitude,
				east: highestLongitude,
				north: highestLatitude
			}

			/** Return the extent */
			return extent
		} else {
			/** Set the error controller as true */
			error = true

			/** Tell the user about the error */
			console.error(
				"There isnt a geometry field defined for the data, or it cant be " +
					'found. Please make sure your data follows the specification of ' +
					'GeoJSON: http://geojson.org/' +
					'Error catched from: ' +
					'%cgetExtent().createRasterHeatmapColor()',
				'font-weight: bold'
			)
		}
	}

	/** Function that get the coordinates and weight of each feature*/
	function getWeights() {
		/** Set a variable to storage the weight value */
		let weight

		/** Set an array that will content the position plus weight */
		let dataArray = []

		/** Check if a field called 'features' exist --> FeatureCollection */
		if ('features' in data) {
			/** Iterate over each entity from the data */
			for (let feature of data.features) {
				/** Se definen los campos de longitud, latitud y peso */
				let longitude = feature.geometry.coordinates[0]
				let latitude = feature.geometry.coordinates[1]

				/** Look for the weight field through the object objects */
				weight = findValueFromKeyName(feature, weightField)

				/** Add the position and weight to the data array */
				dataArray.push({ x: longitude, y: latitude, value: weight })
			}

			return dataArray
		} else {
			/** Iterate over each entity from the data */
			for (let feature of data) {
				/** Se definen los campos de longitud, latitud y peso */
				let longitude = feature.geometry.coordinates[0]
				let latitude = feature.geometry.coordinates[1]

				/** Look for the weight field through the object objects */
				weight = findValueFromKeyName(data, weightField)

				/** Add the position and weight to the data array */
				dataArray.push({ x: longitude, y: latitude, value: weight })
			}

			/** Return the data array */
			return dataArray
		}
	}

	/** Configure the heatmap with the properties defined in the constructor */
	let heatmapSetup = setupHeatmap()

	/** Calculate the extent of the data */
	let extent = getExtent()

	/** If everything is ok, continue and calculate the interpolation weights */
	if (!error) {
		let weights = getWeights()
		/** If still right, creare the heatmap */
		if (!error) {
			/** Define the heatmap */
			let heatMap = CesiumHeatmap.create(viewer, extent, heatmapSetup)

			/** Add the heatmap to the map */
			heatMap.setWGS84Data(minIterationValue, maxIterationValue, weights)

			/** Add the layer to the layer list */
			heatMapLayers.push({ layer: layerNameProcessed, heatMap: heatMap })
		}
	}
}

/** END OF CREATE LAYER */

/** */

/** FEATURE SELECTION */



/** END OF FEATURE SELECTION */

/** */

/** Función que calcula la altura a la que se encuentra la cámara del usuario */
function userCameraHeight() {
	/** Se define la posición de la cámara del usuario */
	let cameraPosition = viewer.scene.camera.positionWC

	/** Se define el elipsoide tridimensional respecto a la posición de cámara */
	let ellipsoid = viewer.scene.globe.ellipsoid.scaleToGeodeticSurface(
		cameraPosition
	)

	/** Se define la altura de la cámara a partir del elipsoide */
	let height = Cesium.Cartesian3.magnitude(
		Cesium.Cartesian3.subtract(
			cameraPosition,
			ellipsoid,
			new Cesium.Cartesian3()
		)
	)

	/** La función devuelve la altura de la cámara  */
	return height
}

/** Función que aumenta el zoom */
function zoomIn() {
	/** Se define una variable con la distancia */
	let height = userCameraHeight()

	/** Altitud mínima a partir de la que no se acerca más */
	if (height <= 501) {
	} else {
		/** El zoom se acerca */
		viewer.camera.zoomIn(500.0)
	}
}

/** Función que disminuye el zoom */
function zoomOut() {
	/** Se define una variable con la distancia */
	userCameraHeight()

	/** El zoom se aleja */
	viewer.camera.zoomOut(500.0)
}

/** Función que carga un WMS en el mapa */

/** Función que carga servicios WMS en el mapa */
function loadWms(urlWmsService, layers = undefined, layerName) {
	dataSourceLayers.push(layerName)
	/** Se define una variable con el nuevo servicio WMS */
	let wmsService = new Cesium.WebMapServiceImageryProvider({
		url: urlWmsService,
		layers: layers,
		proxy: new Cesium.DefaultProxy('/proxy/'),
		parameters: {
			transparent: true,
			format: 'image/png'
		}
	})

	/** Se define el nombre del servicio, para 'reconocerlo' */
	wmsService.name = 'dataSource' + layerName

	/** Se añade el servicio al visor */
	viewer.imageryLayers.addImageryProvider(wmsService)
}

/** Función que carga una imagen SVG como capa de imagery en el mapa */
function addSvgLayer(svgUri, west, south, east, north, layerName) {
	dataSourceLayers.push(layerName)
	baseMapsLayers.addImageryProvider(
		new Cesium.SingleTileImageryProvider({
			url: svgUri,
			rectangle: Cesium.Rectangle.fromDegrees(west, south, east, north)
		})
	)
}

function loadKml(urlKml, layerName) {
	dataSourceLayers.push(layerName)
	/** Se genera un dataSource */
	dataSource = new Cesium.KmlDataSource()

	/** Se carga el KML al dataSource */
	dataSource.load(urlKml, {
		camera: viewer.scene.camera,
		canvas: viewer.scene.canvas
	})

	/** Se fuerza el cambio de nombre del dataSource */
	dataSource.name = 'dataSource' + layerName

	/** Se añade el dataSource al visor */
	viewer.dataSources.add(dataSource)
}

/** Función que atiende a la selección de entidades y escucha clicks */
function entityInteractuation() {
	/** Se define una variable que tendrá el elemento seleccionado */
	let selectedEntity

	/** Otra variable con la posición dinámica del punto al moverse */
	let newPosition

	/** Una variable que controla cuando nos estamos moviendo */
	let onMove = false

	/** Se genera un controlador de eventos del visor */
	let handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)

	/** Esto se ejecutará cuando se haga click con el botón izquierdo */
	handler.setInputAction(function(event) {
		/** Si no nos estamos moviendo; es decir, seleccionamos una entidad... */
		if (!onMove) {
			/** Nos aseguramos de que existe una entidad seleccionada  */
			if (viewer._selectedEntity) {
				/** Se mete la entidad seleccionad en la variable */
				selectedEntity = viewer._selectedEntity

				/** Se pasa a desplazamiento */
				onMove = true
			}
		} else {
			/** ... o si ya está seleccionada una entidad que movemos, entonces... */
			/** Se recoge la posición del canvas sobre la que se pincha */
			let canvasPosition = new Cesium.Cartesian2(
				event.position.x,
				event.position.y
			)

			/** Se define el elipsoide del mapa */
			let ellipsoid = viewer.scene.globe.ellipsoid

			/** Se transforman las coordenadas del canvas al elipsoide */
			let cartesian = viewer.camera.pickEllipsoid(canvasPosition, ellipsoid)

			/** Se definen las coordenadas de la entidad donde se ha pinchado */
			selectedEntity.position.setValue(cartesian)
			let id = selectedEntity.id
			/** Se elimina la entidad seleccionada, para volver a empezar */
			selectedEntity = undefined

			/** Se llama a la función updateAttribute, dándo la posibilidad de editar los campos*/
			let cartographic = ellipsoid.cartesianToCartographic(cartesian)
			let longitude = Cesium.Math.toDegrees(cartographic.longitude).toFixed(4)
			let latitude = Cesium.Math.toDegrees(cartographic.latitude).toFixed(4)
			editAttribute(null, id, longitude, latitude)

			/** Se pasa a estático */
			onMove = false
		}
	}, Cesium.ScreenSpaceEventType.LEFT_CLICK)

	/** Esto se ejecutará cuando se desplaze el cursor del ratón */
	handler.setInputAction(function(event) {
		/** Se comprueba si la entidad seleccionada es correcta */
		if (Cesium.defined(selectedEntity)) {
			/** Se define una nueva posición según donde esté el ratón */
			newPosition = viewer.camera.pickEllipsoid(event.endPosition)

			/** Se comprueba si este nuevo punto es válido */
			if (Cesium.defined(newPosition)) {
				/** Se reasignan valores de coordenadas de la entidad seleccionada*/
				selectedEntity.position.setValue(newPosition)
			}
		}
	}, Cesium.ScreenSpaceEventType.MOUSE_MOVE)

	/** Esto se ejecutará cuando se haga click con el botón izquierdo + CONTROL */
	handler.setInputAction(
		function(click) {
			/** Se define una variable que almacene lo que se pinche */
			let editSelectedEntity = viewer.scene.pick(click.position)

			/** Si se pincha en una entidad */
			if (Cesium.defined(editSelectedEntity)) {
				console.log(editSelectedEntity.primitive)

				/**
				 * AQUÍ ES DONDE PUEDES METER TU CÓDIGO. LA ENTIDAD SELECCIONADA ES
				 * 'editSelectedEntity', LA CUAL TIENE LA INFORMACIÓN QUE NECESITAS
				 * EN LA PROPIEDAD 'primitive'. EL IDENTIFICADOR DE LA ENTIDAD SERÁ
				 * 'editSelectedEntity.primitive.id'.
				 *
				 * SI PREFIERES QUE SEA CON EL BOTÓN DERECHO EN VEZ DE CON CONTROL +
				 * BOTÓN IZQUIERDO, CAMBIA TODA LA LÍNEA DE ABAJO DEL TODO A:
				 * 'Cesium.ScreenSpaceEventType.RIGHT_CLICK'
				 */
			}
		},
		Cesium.ScreenSpaceEventType.LEFT_CLICK,
		Cesium.KeyboardEventModifier.CTRL
	)
}

/** Función que pinta puntos al hacer click en el visor del mapa */
function drawPointClick() {
	/** Variable con la URL de la imagen */
	let imagePoint =
		'http://localhost:18000/controlpanel/static/images/viewerIcons/point.png'

	/** El ratón cambia de puntero, para indicar que se puede poner un punto */
	document.body.style.cursor = 'copy'

	/** Se define una variable que escuchará los eventos de canvas */
	let handler = new Cesium.ScreenSpaceEventHandler(viewer.canvas)

	/** Se producirá este evento cuando se pinche en el visor */
	handler.setInputAction(function(event) {
		/** Se recoge la posición del canvas sobre la que se pincha */
		let canvasPosition = new Cesium.Cartesian2(
			event.position.x,
			event.position.y
		)

		/** Se define el elipsoide del mapa */
		let ellipsoid = viewer.scene.globe.ellipsoid

		/** Se transforman las coordenadas del canvas al elipsoide */
		let cartesian = viewer.camera.pickEllipsoid(canvasPosition, ellipsoid)

		/** Se transforman las coordenadas cartesianas a cartográficas */
		let cartographic = ellipsoid.cartesianToCartographic(cartesian)
		let longitude = Cesium.Math.toDegrees(cartographic.longitude).toFixed(4)
		let latitude = Cesium.Math.toDegrees(cartographic.latitude).toFixed(4)

		/** Se añade un punto en el sitio donde se pincha */
		var id = viewer.entities.add({
			position: Cesium.Cartesian3.fromDegrees(longitude, latitude),
			billboard: {
				image: imagePoint,
				scale: 1.0
			}
		}).id

		/** Se destruye la variable que escucha, para que pinte un único punto */
		handler.destroy()

		/** El ratón regresa al puntero básico */
		document.body.style.cursor = 'auto'
	}, Cesium.ScreenSpaceEventType.LEFT_CLICK)

	/** Se producirá este evento cuando se pinche con el botón derecho */
	handler.setInputAction(function(event) {
		/** Se destruye la variable que escucha, para cancelar el evento */
		handler.destroy()

		/** El ratón regresa al puntero básico */
		document.body.style.cursor = 'auto'
	}, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
}

/** Función que pinta una línea al hacer click en el visor del mapa */
function drawLineClick() {
	/** Se definen las variables a utilizar */
	let colorPoint = Cesium.Color.PINK.withAlpha(0.6)
	let colorPolyline = Cesium.Color.BLUE.withAlpha(0.6)
	let entityCollection = new Cesium.EntityCollection()
	let activeShapePoints = []
	let activeShape
	let floatingPoint
	let handler = new Cesium.ScreenSpaceEventHandler(viewer.canvas)

	/** El ratón cambia de puntero, para indicar que se puede poner un punto */
	document.body.style.cursor = 'copy'

	/** Función que genera la entidad de punto */
	function createPoint(pointCoordinates) {
		let point = viewer.entities.add({
			name: 'temporalPoint',
			position: pointCoordinates,
			point: {
				color: colorPoint,
				pixelSize: 6
			}
		})

		/** Se añade el punto a la colección de puntos, para controlarlo */
		entityCollection.add(point)

		/** Se devuelve el punto, para almacenarlo como punto temporal */
		return point
	}

	/** Función que dibuja la polilínea */
	function drawShapePolyline(positionData) {
		/** Se define una variable con la forma de la entidad */
		let shape

		/** Se genera la entidad de la polilínea */
		shape = viewer.entities.add({
			polyline: {
				positions: positionData,
				material: colorPolyline,
				width: 3
			}
		})

		/** Se devuelve la polilínea */
		return shape
	}

	/** Función que genera la polilínea definitiva, eliminando los temporales */
	function generateFinalPolyline() {
		activeShapePoints.pop()
		var id = drawShapePolyline(activeShapePoints).id

		/** Se eliminan las entidades generadas y se vacían las variables */
		viewer.entities.remove(floatingPoint)
		viewer.entities.remove(activeShape)
		floatingPoint = undefined
		activeShape = undefined
		activeShapePoints = []

		/** Se itera por la colección de puntos guía, y se eliminan */
		for (entity of entityCollection.values) {
			viewer.entities.remove(entity)
		}

		/** Se destruye la variable que escucha, para que pinte un único punto */
		handler.destroy()

		/** El ratón regresa al puntero básico */
		document.body.style.cursor = 'auto'
	}

	/** Se producirá este evento cuando se pinche en el visor */
	handler.setInputAction(function(event) {
		/** Se extraen las coordenadas de donde se pincha con el ratón */
		let position = viewer.camera.pickEllipsoid(event.position)

		/** Se comprueba que donde se pincha está en el mapa */
		if (Cesium.defined(position)) {
			/** Se mira si se han definido puntos activos previamente */
			if (activeShapePoints.length === 0) {
				/** Con cada click, se genera un punto en el mapa */
				floatingPoint = createPoint(position)

				/** Se mete en el listado la posición del punto generado */
				activeShapePoints.push(position)

				/** Se define las posiciones hasta el momento */
				let dynamicPositions = new Cesium.CallbackProperty(function() {
					return activeShapePoints
				}, false)

				/** Se va pintando un polígono temporal con los puntos temporales */
				activeShape = drawShapePolyline(dynamicPositions)
			}
			/** Se van añadiendo al listado las posiciones de los puntos generados */
			activeShapePoints.push(position)

			/** Y se van creando puntos */
			createPoint(position)
		}
	}, Cesium.ScreenSpaceEventType.LEFT_CLICK)

	/** Se producirá este evento cuando se mueva el cursor por el visor */
	handler.setInputAction(function(event) {
		/** Se comprueba si el punto flotante es correcto */
		if (Cesium.defined(floatingPoint)) {
			/** Se define una nueva posición (siguinte punto) */
			let newPosition = viewer.camera.pickEllipsoid(event.endPosition)

			/** Se comprueba si este nuevo punto es válido */
			if (Cesium.defined(newPosition)) {
				/** Se reasignan valores */
				floatingPoint.position.setValue(newPosition)
				activeShapePoints.pop()
				activeShapePoints.push(newPosition)
			}
		}
	}, Cesium.ScreenSpaceEventType.MOUSE_MOVE)

	/** Cuando el usuario quiera terminar, tocará el botón derecho */
	handler.setInputAction(function(event) {
		/** Se genera la polilínea definitiva */
		generateFinalPolyline()
	}, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
}

/** Función que pinta un polígono al hacer click en el visor del mapa */
function drawPolygonClick() {
	/** Se definen las variables a utilizar */
	let colorPoint = Cesium.Color.PINK.withAlpha(0.6)
	let colorPolygon = Cesium.Color.RED.withAlpha(0.6)
	let entityCollection = new Cesium.EntityCollection()
	let activeShapePoints = []
	let activeShape
	let floatingPoint
	let handler = new Cesium.ScreenSpaceEventHandler(viewer.canvas)

	/** El ratón cambia de puntero, para indicar que se puede poner un punto */
	document.body.style.cursor = 'copy'

	/** Función que generará los puntos guía del polígono */
	function createPoint(pointCoordinates) {
		/** Se genera la entidad de punto */
		let point = viewer.entities.add({
			name: 'temporalPoint',
			position: pointCoordinates,
			point: {
				color: colorPoint,
				pixelSize: 6
			}
		})

		/** Se añade el punto a la colección de entidades, para controlarlo */
		entityCollection.add(point)

		/** Se devuelve el punto, para almacenarlo como punto temporal */
		return point
	}

	/** Función que dibuja el polígono */
	function drawShapePolygon(positionData) {
		/** Se define una variable con la forma de la entidad */
		let shape

		/** Se genera la entidad del polígono */
		shape = viewer.entities.add({
			polygon: {
				hierarchy: positionData,
				material: new Cesium.ColorMaterialProperty(colorPolygon)
			}
		})

		/** Se devuelve la forma del polígono */
		return shape
	}

	/** Función que genera el polígono definitivo, eliminando los temporales */
	function generateFinalPolygon() {
		activeShapePoints.pop()
		drawShapePolygon(activeShapePoints)

		/** Se eliminan las entidades generadas y se vacían las variables */
		viewer.entities.remove(floatingPoint)
		viewer.entities.remove(activeShape)
		floatingPoint = undefined
		activeShape = undefined
		activeShapePoints = []

		/** Se itera por la colección de puntos guía, y se eliminan */
		for (entity of entityCollection.values) {
			viewer.entities.remove(entity)
		}

		/** Se destruye la variable que escucha */
		handler.destroy()

		/** El ratón regresa al puntero básico */
		document.body.style.cursor = 'auto'
	}

	/** Se producirá este evento cuando se pinche en el visor */
	handler.setInputAction(function(event) {
		/** Se extraen las coordenadas de donde se pincha con el ratón */
		let position = viewer.camera.pickEllipsoid(event.position)

		/** Se comprueba que donde se pincha está en el mapa */
		if (Cesium.defined(position)) {
			/** Se mira si se han definido puntos activos previamente */
			if (activeShapePoints.length === 0) {
				/** Con cada click, se genera un punto en el mapa */
				floatingPoint = createPoint(position)

				/** Se mete en el listado la posición del punto generado */
				activeShapePoints.push(position)

				/** Se define las posiciones hasta el momento */
				let dynamicPositions = new Cesium.CallbackProperty(function() {
					return activeShapePoints
				}, false)

				/** Se va pintando un polígono temporal con los puntos temporales */
				activeShape = drawShapePolygon(dynamicPositions)
			}

			/** Se van añadiendo al listado las posiciones de los puntos generados */
			activeShapePoints.push(position)

			/** Y se van creando puntos */
			createPoint(position)
		}
	}, Cesium.ScreenSpaceEventType.LEFT_CLICK)

	/** Se producirá este evento cuando se mueva el cursor por el visor */
	handler.setInputAction(function(event) {
		/** Se comprueba si el punto flotante es correcto */
		if (Cesium.defined(floatingPoint)) {
			/** Se define una nueva posición (siguinte punto) */
			let newPosition = viewer.camera.pickEllipsoid(event.endPosition)

			/** Se comprueba si este nuevo punto es válido */
			if (Cesium.defined(newPosition)) {
				/** Se reasignan los valores */
				floatingPoint.position.setValue(newPosition)
				activeShapePoints.pop()
				activeShapePoints.push(newPosition)
			}
		}
	}, Cesium.ScreenSpaceEventType.MOUSE_MOVE)

	/** Cuando el usuario quiera terminar, tocará el botón derecho */
	handler.setInputAction(function(event) {
		/** Se genera la polilínea definitiva */
		generateFinalPolygon()
	}, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
}

/** Función que pinta dibuja polígonos a mano alzada en el visor del mapa */
function drawPolygonFreehand() {
	/** Se definen las variables a utilizar */
	let colorPolygon = Cesium.Color.fromCssColorString('#FF0000').withAlpha(0.6)
	let drawing = false
	let polyline
	let positions = []
	let handler = new Cesium.ScreenSpaceEventHandler(viewer.canvas)

	/** El ratón cambia de puntero, para indicar que se puede poner un punto */
	document.body.style.cursor = 'copy'

	/** Se producirá este evento cuando se pinche en el visor */
	handler.setInputAction(function(click) {
		/** En caso de que no exista ningún polígono (primer uso) */
		if (!drawing) {
			/** Se define la polilínea siguiendo al puntero del ratón */
			polyline = viewer.entities.add({
				polyline: {
					positions: new Cesium.CallbackProperty(function() {
						return positions
					}, false),
					material: colorPolygon
				}
			})

			/** Cuando se haya pintado el polígono que interesa */
		} else {
			/** Se define el polígono a partir de la polilínea */
			viewer.entities.add({
				polygon: {
					hierarchy: {
						positions: positions
					},
					material: colorPolygon,
					outline: true
				}
			})

			/** Se borra la polilínea generada durante el pintado */
			viewer.entities.remove(polyline)

			/** Se limpia el array de coordenadas usado por la polilínea */
			positions = []

			/** Se destruye la variable que escucha, para que pinte un único
			 * polígono */
			handler.destroy()

			/** El ratón regresa al puntero básico */
			document.body.style.cursor = 'auto'
		}

		/** No hay verdad sin falsedad */
		drawing = !drawing
	}, Cesium.ScreenSpaceEventType.LEFT_CLICK)

	/** Por si se usa el botón derecho para cerrar el polígono */
	handler.setInputAction(function(click) {
		viewer.entities.add({
			polygon: {
				hierarchy: {
					positions: positions
				},
				material: colorPolygon,
				outline: true
			}
		})

		/** Se borra la polilínea generada durante el pintado */
		viewer.entities.remove(polyline)

		/** Se limpia el array de coordenadas usado por la polilínea */
		positions = []

		/** Se destruye la variable que escucha, para que pinte un único
		 * polígono */
		handler.destroy()

		/** El ratón regresa al puntero básico */
		document.body.style.cursor = 'auto'
	}, Cesium.ScreenSpaceEventType.RIGHT_CLICK)

	/** Se producirá este evento cuando se mueva el puntero por el visor */
	handler.setInputAction(function(movement) {
		/** Se define una variable con el punto final del movimiento */
		let surfacePosition = viewer.camera.pickEllipsoid(movement.endPosition)

		/** En caso de que exista el dibujo del polígono y su punto final */
		if (drawing && Cesium.defined(surfacePosition)) {
			/** Se añade al listado de posiciones el punto final del movimiento */
			positions.push(surfacePosition)
		}
	}, Cesium.ScreenSpaceEventType.MOUSE_MOVE)
}

/** Función que limpia TODO lo que se dibuje en el mapa */
function clearEntities() {
	viewer.entities.removeAll()
}

/** Función que saca una regla de medida en el visor
 * TODO: hay que hacer la etiqueta de medida dinámica (fjla)
 */
function measureRuler() {
	/** Se definen las variables a utilizar */
	let colorPoint = Cesium.Color.BROWN.withAlpha(0.6)
	let colorPolyline = Cesium.Color.DARKORANGE.withAlpha(0.6)
	let entityCollection = new Cesium.EntityCollection()
	let activeShapePoints = []
	let activeShape
	let floatingPoint
	let handler = new Cesium.ScreenSpaceEventHandler(viewer.canvas)
	let counter = 0

	/** Se comprueba si existe una 'regla' previa, y si es así la elimina */
	for (entity of viewer.entities.values) {
		if (entity.name === 'measurePolyline') {
			viewer.entities.remove(entity)
		}
	}

	/** Se comprueba si existe un etiquetado previo, y dado el caso lo elimina
	 * Esto se mete por separado porque en el mismo FOR peta */
	for (entity of viewer.entities.values) {
		if (entity.name === 'measureLabel') {
			viewer.entities.remove(entity)
		}
	}

	/** El ratón cambia de puntero, para indicar que se puede poner un punto */
	document.body.style.cursor = 'copy'

	/** Se genera la entidad de punto */
	function createPoint(pointCoordinates) {
		let point = viewer.entities.add({
			name: 'temporalPoint',
			position: pointCoordinates,
			point: {
				color: colorPoint,
				pixelSize: 6
			}
		})

		/** Se añade el punto a la colección de puntos, para controlarlo */
		entityCollection.add(point)

		/** Se devuelve el punto, para almacenarlo como punto temporal */
		return point
	}

	/** Función que dibuja la polilínea */
	function drawShapePolyline(positionData) {
		/** Se define una variable con la forma de la entidad */
		let shape

		/** Se genera la entidad de la polilínea */
		shape = viewer.entities.add({
			name: 'measurePolyline',
			polyline: {
				positions: positionData,
				material: new Cesium.PolylineDashMaterialProperty({
					color: colorPolyline,
					dashLength: 12.0
				}),
				width: 3
			}
		})

		/** Se devuelve la polilínea */
		return shape
	}

	/** Función que calcula la longitud de la polilínea */
	function polylineLength(polyline) {
		/** Se definen los puntos iniciales y finales de la polilínea */
		let initialPosition = polyline.polyline.positions.getValue()[0]
		let finalPosition = polyline.polyline.positions.getValue()[
			polyline.polyline.positions.getValue().length - 1
		]

		/** Se genera el elipsoide geodésico a partir de las coordenadas */
		let ellipsoidGeodesic = new Cesium.EllipsoidGeodesic(
			Cesium.Ellipsoid.WGS84.cartesianToCartographic(initialPosition),
			Cesium.Ellipsoid.WGS84.cartesianToCartographic(finalPosition)
		)

		let midPoint = ellipsoidGeodesic.interpolateUsingFraction(0.5)

		/** Se define la etiqueta que aparecerá encima de la regla */
		viewer.entities.add({
			name: 'measureLabel',
			position: new Cesium.Cartesian3.fromRadians(
				midPoint.longitude,
				midPoint.latitude,
				midPoint.height
			),
			label: {
				text: (ellipsoidGeodesic.surfaceDistance * 0.001).toFixed(2) + 'km',
				style: Cesium.LabelStyle.FILL_AND_OUTLINE,
				outlineWidth: 2,
				font: '18px sans-serif',
				pixelOffset: new Cesium.Cartesian2(0.0, -20)
			}
		})

		//entityCollection.add(measureLabel)
	}

	/** Función que elimina los puntos de guiado de la guía */
	function clear() {
		/** Se itera por la colección de puntos guía, y se eliminan */
		for (entity of entityCollection.values) {
			viewer.entities.remove(entity)
		}
	}

	/** Se producirá este evento cuando se pinche en el visor */
	handler.setInputAction(function(event) {
		/** Se saca la posición respecto al elipsoide */
		let position = viewer.camera.pickEllipsoid(event.position)

		/** Se comprueba que donde se pincha está en el mapa */
		if (Cesium.defined(position)) {
			/** Se indica el número de puntos generados hasta el momento */
			counter += 1

			/** Se mira si se han definido puntos activos previamente */
			if (activeShapePoints.length === 0) {
				/** Con cada click, se genera un punto en el mapa */
				floatingPoint = createPoint(position)

				/** Se mete en el listado la posición del punto generado */
				activeShapePoints.push(position)

				/** Se define las posiciones hasta el momento */
				let dynamicPositions = new Cesium.CallbackProperty(function() {
					return activeShapePoints
				}, false)

				/** Se va pintando un polígono temporal con los puntos temporales */
				activeShape = drawShapePolyline(dynamicPositions)
			}

			/** Se van añadiendo al listado las posiciones de los puntos generados */
			activeShapePoints.push(position)

			/** Y se van creando puntos */
			createPoint(position)

			/** Se cuenta si hay al menos dos puntos ya pintados */
			if (counter > 1) {
				/** Entonces se lanza la función que mide entre puntos */
				polylineLength(activeShape)

				/** Se destruye la variable que escucha, para que pinte un único punto */
				handler.destroy()

				/** Se limpia */
				clear()

				/** El ratón regresa al puntero básico */
				document.body.style.cursor = 'auto'
			}
		}
	}, Cesium.ScreenSpaceEventType.LEFT_CLICK)

	/** Se producirá este evento cuando se mueva el cursor por el visor */
	handler.setInputAction(function(event) {
		/** Se comprueba si el punto flotante es correcto */
		if (Cesium.defined(floatingPoint)) {
			/** Se define una nueva posición (siguinte punto) */
			let newPosition = viewer.camera.pickEllipsoid(event.endPosition)

			/** Se comprueba si este nuevo punto es válido */
			if (Cesium.defined(newPosition)) {
				/** Se reasignan los valores */
				floatingPoint.position.setValue(newPosition)
				activeShapePoints.pop()
				activeShapePoints.push(newPosition)
			}
		}
	}, Cesium.ScreenSpaceEventType.MOUSE_MOVE)

	/** Cuando el usuario quiera terminar, tocará el botón derecho */
	handler.setInputAction(function(event) {
		/** Se indica el número de puntos generados hasta el momento */
		counter += 1

		if (counter > 1) {
			/** Se lanza la función que mide entre puntos */
			polylineLength(activeShape)
			clear()
		}

		/** Se destruye la variable que escucha, para que pinte un único punto */
		handler.destroy()

		/** El ratón regresa al puntero básico */
		document.body.style.cursor = 'auto'
	}, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
}

/** Función que define el mapa base de inicio */
function initialBaseMap(nameBaseMap, accessToken = '', urlBase) {
	/** Se clasifica el tipo de mapa base a utilizar */
	baseMapType = nameBaseMap.split('.')[0]
	baseMapName = nameBaseMap.split('.')[1]
	baseMapStyle = nameBaseMap.split('.')[2]

	/** Variable que hará referencia a la variable con la URL del mapa base */
	let urlBaseMap = null

	/** Se comprueba si el mapa base es de CartoDB */
	if (baseMapType === 'cartodb') {
		/** Se comprueba si el mapa será sin etiquetas */
		if (baseMapStyle === 'NoLabels') {
			if (baseMapName === 'Positron') {
				urlBaseMap = urlBase
			} else if (baseMapName === 'DarkMatter') {
				urlBaseMap = urlBase
			} else if (baseMapName === 'Voyager') {
				urlBaseMap = urlBase
			}
		} else if (baseMapStyle === 'Labels') {
			/** Se comprueba si el mapa será con etiquetas */
			if (baseMapName === 'Positron') {
				urlBaseMap = urlBase
			} else if (baseMapName === 'DarkMatter') {
				urlBaseMap = urlBase
			} else if (baseMapName === 'Voyager') {
				urlBaseMap = urlBase
			}
		} else if (baseMapStyle === 'OnlyLabels') {
			/** Se comprueba si el mapa será únicamente de etiquetas */
			if (baseMapName === 'Positron') {
				urlBaseMap = urlBase
			} else if (baseMapName === 'DarkMatter') {
				urlBaseMap = urlBase
			} else if (baseMapName === 'Voyager') {
				urlBaseMap = urlBase
			}
		}

		/** Se añade el mapa base al mapa */
		baseMap = baseMapsLayers.addImageryProvider(
			new Cesium.UrlTemplateImageryProvider({
				url: urlBaseMap
			})
		)
	} else if (baseMapType === 'esri') {
		/** Se comprueba si el mapa base es de CartoDB */
		/** Se comprueba si se está cargando un mapa base sin etiquetado */
		if (baseMapStyle === 'NoLabels') {
			if (baseMapName === 'DarkGray') {
				urlBaseMap = urlBase
			} else if (baseMapName === 'Gray') {
				urlBaseMap = urlBase
			} else if (baseMapName === 'Imagery') {
				urlBaseMap = urlBase
			} else if (baseMapName === 'ShadedRelief') {
				urlBaseMap = urlBase
			}

			/** Se añade el mapa base */
			baseMap = baseMapsLayers.addImageryProvider(
				new Cesium.ArcGisMapServerImageryProvider({
					url: urlBaseMap
				})
			)
		} else if (baseMapStyle === 'Labels') {
			/** Se comprueba si se está cargando un mapa base con etiquetado */
			if (baseMapName === 'DarkGray') {
				baseMap = baseMapsLayers.addImageryProvider(
					new Cesium.ArcGisMapServerImageryProvider({
						url: urlBase
					})
				)

				baseMap = baseMapsLayers.addImageryProvider(
					new Cesium.ArcGisMapServerImageryProvider({
						url: urlBase
					})
				)
			} else if (baseMapName === 'Gray') {
				baseMap = baseMapsLayers.addImageryProvider(
					new Cesium.ArcGisMapServerImageryProvider({
						url: urlBase
					})
				)

				baseMap = baseMapsLayers.addImageryProvider(
					new Cesium.ArcGisMapServerImageryProvider({
						url: urlBase
					})
				)
			} else if (baseMapName === 'Imagery') {
				baseMap = baseMapsLayers.addImageryProvider(
					new Cesium.ArcGisMapServerImageryProvider({
						url: urlBase
					})
				)

				baseMap = baseMapsLayers.addImageryProvider(
					new Cesium.ArcGisMapServerImageryProvider({
						url: urlBase
					})
				)
			} else if (baseMapName === 'ShadedRelief') {
				baseMap = baseMapsLayers.addImageryProvider(
					new Cesium.ArcGisMapServerImageryProvider({
						url: urlBase
					})
				)

				baseMap = baseMapsLayers.addImageryProvider(
					new Cesium.ArcGisMapServerImageryProvider({
						url: urlBase
					})
				)
			} else if (baseMapName === 'Streets') {
				baseMap = baseMapsLayers.addImageryProvider(
					new Cesium.ArcGisMapServerImageryProvider({
						url: urlBase
					})
				)
			} else if (baseMapName === 'Topo') {
				baseMap = baseMapsLayers.addImageryProvider(
					new Cesium.ArcGisMapServerImageryProvider({
						url: urlBase
					})
				)
			}
		} else if (baseMapStyle === 'OnlyLabels') {
			/** Se comprueba si se está cargando un mapa base sólo de etiquetado */
			if (baseMapName === 'DarkGray') {
				urlBaseMap = urlBase
			} else if (baseMapName === 'Gray') {
				urlBaseMap = urlBase
			} else if (baseMapName === 'Reference') {
				urlBaseMap = urlBase
			}

			/** Se añade el mapa base */
			baseMap = baseMapsLayers.addImageryProvider(
				new Cesium.ArcGisMapServerImageryProvider({
					url: urlBaseMap
				})
			)
		}
	} else if (baseMapType === 'osm') {
		/** Se comprueba si el mapa base es de OSM */
		/** Se comprueba si se está cargando un mapa base sin etiquetado */
		if (baseMapStyle === 'NoLabels') {
		} else if (baseMapStyle === 'Labels') {
			/** Se comprueba si se está cargando un mapa base con etiquetado */
			if (baseMapName === 'Mapnik') {
				baseMap = baseMapsLayers.addImageryProvider(
					Cesium.createOpenStreetMapImageryProvider({
						url: urlBase
					})
				)
			} else if (baseMapName === 'BlackAndWhite') {
				baseMap = baseMapsLayers.addImageryProvider(
					new Cesium.UrlTemplateImageryProvider({
						url: urlBase
					})
				)
			}
		} else if (baseMapStyle === 'OnlyLabels') {
			/** Se comprueba si se está cargando un mapa base sólo de etiquetado */
		}
	} else if (baseMapType === 'mapbox') {
		/** Se comprueba si el mapa base es de OSM */
		let mapId = null

		/** Se comprueba si se está cargando un mapa base sin etiquetado */
		if (baseMapStyle === 'NoLabels') {
			if (baseMapName === 'Satellite') {
				mapId = 'mapbox.satellite'
			} else if (baseMapName === 'Outdoors') {
				mapId = 'mapbox.outdoors'
			} else if (baseMapName === 'Pencil') {
				mapId = 'mapbox.pencil'
			}
		} else if (baseMapStyle === 'Labels') {
			/** Se comprueba si se está cargando un mapa base con etiquetado */
			if (baseMapName === 'Streets') {
				mapId = 'mapbox.streets'
			} else if (baseMapName === 'Light') {
				mapId = 'mapbox.light'
			} else if (baseMapName === 'Dark') {
				mapId = 'mapbox.dark'
			} else if (baseMapName === 'Pirates') {
				mapId = 'mapbox.pirates'
			} else if (baseMapName === 'Comic') {
				mapId = 'mapbox.comic'
			} else if (baseMapName === 'Emerald') {
				mapId = 'mapbox.emerald'
			}
		} else if (baseMapStyle === 'OnlyLabels') {
			/** Se comprueba si se está cargando un mapa base sólo de etiquetado */
		}

		baseMap = baseMapsLayers.addImageryProvider(
			new Cesium.MapboxImageryProvider({
				mapId: mapId,
				accessToken: accessToken
			})
		)
	}
}

/** Función que muestra las coordenadas del ratón */
function showMouseCartographicPosition() {
	/** Se genera la escucha del canvas */
	let handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)

	/** Se genera una entidad 'invisible' que seguirá al ratón */
	let fakeEntity = viewer.entities.add({
		label: {
			show: false,
			showBackground: true,
			font: '14px monospace',
			horizontalOrigin: Cesium.HorizontalOrigin.LEFT,
			verticalOrigin: Cesium.VerticalOrigin.TOP,
			pixelOffset: new Cesium.Cartesian2(15, 0)
		}
	})

	/** Se producirá este evento cuando se mueva el cursor por el visor */
	handler.setInputAction(function(movement) {
		let cartesian = viewer.camera.pickEllipsoid(
			movement.endPosition,
			viewer.scene.globe.ellipsoid
		)
		if (cartesian) {
			let cartographic = Cesium.Cartographic.fromCartesian(cartesian)

			/** Se define la longitud y latitud, limitando los decimales a un par */
			let longitudeString = Cesium.Math.toDegrees(
				cartographic.longitude
			).toFixed(2)
			let latitudeString = Cesium.Math.toDegrees(cartographic.latitude).toFixed(
				2
			)

			/** Se obtienen las coordenadas de la falsa entidad */
			fakeEntity.position = cartesian
			fakeEntity.label.show = true

			/** Se incluye el texto de la ventanita de las coordenadas. */
			fakeEntity.label.text =
				'Longitude: ' +
				('   ' + longitudeString).slice(-7) +
				'\u00B0' +
				'Latitude: ' +
				('   ' + latitudeString).slice(-7) +
				'\u00B0'
		} else {
			fakeEntity.label.show = false
		}
	}, Cesium.ScreenSpaceEventType.MOUSE_MOVE)
}

function fakeInfoBox(feature) {
	let propertyEntry = Object.entries(feature.properties)
	let headerBeginning = '<table class=\'cesium-infoBox-defaultTable\'><tbody>'
	let content = ''
	let headerEnding = '</tbody></table>'
	for (let entry of propertyEntry) {
		content += '<tr><th>' + entry[0] + '</th><td>' + entry[1] + '</td></tr>'
	}

	return headerBeginning + content + headerEnding
}

function getLayerData(layer, queryParams){
	var url = basePath + "/layer/getLayerData?layer=" + layer;
	if(queryParams!='null' && queryParams!=undefined){
		queryParams = JSON.parse(queryParams);
		for(var i=0;i<queryParams.length;i++){
			var field = queryParams[i]["param"];
			var value = queryParams[i]["default"];
			url += "&" + field + "=" + value
		}
	}
	
	$.ajax({
		url : url,
		type : "GET",
		dataType : 'json',
		contentType : 'application/json',
		async : false,
		headers : {
			"Content-Type" : "application/json"
		},
		success : function(result) {
			
			var data = result;
			var type = data["typeGeometry"].toLowerCase();
			if(data["heatMap"]["radius"] == undefined || data["heatMap"]["radius"] == null){
				var size = data["symbology"]["pixelSize"];
				var colorIn = data["symbology"]["innerColorHEX"];
				var alphaIn = data["symbology"]["innerColorAlpha"];
				var colorOut = data["symbology"]["outlineColorHEX"];
				var outSize = data["symbology"]["outlineWidth"];
				var alphaOut = data["symbology"]["outerColorAlpha"];
			}else{
				var weithField = "value";
				var min = data["heatMap"]["min"];
				var max = data ["heatMap"]["max"];
				var radius = data ["heatMap"]["radius"];
			}
			
			if(type=="point"){
				createLayer( data,true,type,'entity','color',size,colorIn,alphaIn,outSize,colorOut,alphaOut); 
				filterLayer(data);
			}else if(type=="lineString".toLowerCase() || type=="polyline".toLowerCase()){
				createLayer( data,true,"lineString",'entity','color',outSize,colorIn,alphaIn);
				filterLayer(data); 
			}else if(type=="polygon".toLowerCase()){
				createLayer( data,true,type,'entity','color',colorIn,alphaIn); 
				filterLayer(data);
			}else if(type.toLowerCase()=="raster"){
				createLayer( data,true,type,'heatmap','color',weithField, min, max, radius); 
			}
			
			
			
		},
		error : function(req, status, err) { console.log('something went wrong',req.responseText, status, err); }
	});
}
/** EXECUTIONS */

/** Set initial extent from the global variables */
cameraToPosition('zoom','degrees',startLongitude,startLatitude,startHeight,0,-90,0)
featureSelection('simple', '#55ffff', 0.6, 4) 
