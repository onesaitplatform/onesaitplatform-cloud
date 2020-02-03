var viewer = new Cesium.Viewer('cesiumContainer', {
  // Desactiva el widget de la pelota inferior izquierda.
  animation: false,
  // Botón de inicio.
  homeButton: false,
  // Widget del selector de mapas base.
  baseLayerPicker: false,
  // Botón de pantalla completa.
  fullscreenButton: false,
  // Cajetín del geocodificador.
  geocoder: false,
  /** Cajetín del InfoBox */
  infoBox: true,
  // Botón de ayuda a la navegación.
  navigationHelpButton: false,
  /** */
  sceneMode: Cesium.SceneMode.SCENE2D,
  // Botón de selección de modos 2D/2,5D/3D.
  sceneModePicker: false,
  /** Terreno con elevación */
  //terrainProvider : Cesium.createWorldTerrain(),
  // Widget de la barra temporal inferior.
  timeline: false,
  // Recuadro verde que sale al seleccionar entidades.
  selectionIndicator: false,
  // Mapa base por defecto.
  imageryProvider: Cesium.createOpenStreetMapImageryProvider({
    url :'https://a.tile.openstreetmap.org/'
  })
})

viewer.scene.highDynamicRange = false;

/** FUNCIONES */
 
 function defineViewerInitialExtent(longitude,latitude,altitud) {
    viewer.camera.setView({
      destination : Cesium.Cartesian3.fromDegrees(longitude, latitude, altitud)
    })
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
  handler.setInputAction(function(event){

    /** Si no nos estamos moviendo; es decir, seleccionamos una entidad... */
    if(!onMove) {

      /** Nos aseguramos de que existe una entidad seleccionada  */
      if (viewer._selectedEntity) {

        /** Se mete la entidad seleccionad en la variable */
        selectedEntity = viewer._selectedEntity

        /** Se pasa a desplazamiento */
        onMove = true
      }
    }

    /** ... o si ya está seleccionada una entidad que movemos, entonces... */
    else {

      /** Se recoge la posición del canvas sobre la que se pincha */
      let canvasPosition = new Cesium.Cartesian2(event.position.x, event.position.y)

      /** Se define el elipsoide del mapa */
      let ellipsoid = viewer.scene.globe.ellipsoid

      /** Se transforman las coordenadas del canvas al elipsoide */
      let cartesian = viewer.camera.pickEllipsoid(canvasPosition, ellipsoid)

      /** Se definen las coordenadas de la entidad donde se ha pinchado */
      selectedEntity.position.setValue(cartesian)
      let id= selectedEntity.id;
      /** Se elimina la entidad seleccionada, para volver a empezar */
      selectedEntity = undefined
      
      /** Se llama a la función updateAttribute, dándo la posibilidad de editar los campos*/
      let cartographic = ellipsoid.cartesianToCartographic(cartesian)
      let longitude = Cesium.Math.toDegrees(cartographic.longitude).toFixed(4)
      let latitude = Cesium.Math.toDegrees(cartographic.latitude).toFixed(4)
      editAttribute(null,id, longitude, latitude);

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
  handler.setInputAction(function(click) {

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
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK, Cesium.KeyboardEventModifier.CTRL)

}

/** Función que pinta puntos al hacer click en el visor del mapa */
function drawPointClick() {

  /** Variable con la URL de la imagen */
  let imagePoint = '/controlpanel/static/images/viewerIcons/point.png'

  /** El ratón cambia de puntero, para indicar que se puede poner un punto */
  document.body.style.cursor = 'copy'
  
  /** Se define una variable que escuchará los eventos de canvas */
  let handler = new Cesium.ScreenSpaceEventHandler(viewer.canvas)
  
  /** Se producirá este evento cuando se pinche en el visor */
  handler.setInputAction(function(event) {

    /** Se recoge la posición del canvas sobre la que se pincha */
    let canvasPosition = new Cesium.Cartesian2(event.position.x, event.position.y)

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
		      position : Cesium.Cartesian3.fromDegrees(longitude, latitude),
		      billboard: {
		        image: imagePoint,
		        scale: 1.0,
		      }
		    }).id;
    
    addPointInfoBox(longitude, latitude, id);
    
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

/** Función que pinta un punto a partir de unas coordenadas de entrada
 * OJO: esta función es la versión imagen; para punto sencillo es diferente */
function drawPointInput(longitude, latitude) {
  
  /** Se añade un punto con una imagen en el sitio donde se pincha */
  var id = viewer.entities.add({
    position : Cesium.Cartesian3.fromDegrees(longitude, latitude),
    billboard: {
      image: imagePoint,
      scale: 1.0,
    }
  }).id;
  
  addPointInfoBox(longitude, latitude, id);
}

/** Función que pinta n puntos a partir de unas coordenadas de entrada */
function drawPointMultipleInput() {

  /** Variable con la URL de la imagen */
  let imagePoint = '/controlpanel/static/images/viewerIcons/point.png'
  
  /** Se le solicita al usuario que introduzca las coordenadas */
  let coordinatesArray = prompt('Please enter an array of latitude and longitude' +
  'coordinates separated with semi-colon, in the following format: -15.43,28.11;-15.23,28.31','-15.43,28.11;-15.23,28.31')

  /** Se comprueba lo que ha metido el usuario, que tela... */
  if (coordinatesArray == '') {
    text = "Please write a couple of coordinates"
  }

  /** Se rompe la cadena de entrada, definidiendo las coordenadas */
  let coordinates = coordinatesArray.split(';')

  /** Se itera por todas las coordenadas introducidas por el usuario */
  for (coordinate of coordinates) {

    /** Se definen las coordenadas cartográficas */
    let longitude = coordinate.split(',')[0]
    let latitude = coordinate.split(',')[1]

    /** Se añade un punto en el sitio donde se pincha */
    viewer.entities.add({
      position : Cesium.Cartesian3.fromDegrees(longitude, latitude),
      billboard: {
        image: imagePoint,
        scale: 0.05,
      }
    })
  }
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
      position : pointCoordinates,
      point : {
        color : colorPoint,
        pixelSize : 6,
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
    var id = drawShapePolyline(activeShapePoints).id;
    
    addLineInfoBox(id, activeShapePoints);
    
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
        let dynamicPositions = new Cesium.CallbackProperty(function () {
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

/** Función que pinta una línea a partir de unas coordenadas de entrada */
function drawLineInput(coordinatesArray) {
	
	/** Se define el color y grosor de la línea */
	let colorPolyline = Cesium.Color.fromCssColorString('#FF0000').withAlpha(0.6)
	let colorWidth = 5

	/** Se definen las coordenadas como un array */
	let coordinates = coordinatesArray.split(',')

	/** Se crea la entidad de línea a partir del array y las propiedades */
	var id = viewer.entities.add({
		polyline: {
			positions: Cesium.Cartesian3.fromDegreesArray(coordinates),
			width: colorWidth,
			material: colorPolyline
		}
	}).id;
	
	return id;

	/** Se destruye la variable que escucha, para que pinte un único punto */
}

/** Función que pinta n líneas a partir de unas coordenadas de entrada */
function drawLineMultipleInput() {
  return
}

/** Función que pinta líneas al hacer click en el visor del mapa */
function drawLineFreehand() {
  return
}




/** Función que pinta un polígono al hacer click en el visor del mapa */
function drawPolygonClick() {

  /** Se definen las variables a utilizar */
  let colorPoint = Cesium.Color.PINK.withAlpha(0.6)
  let colorPolygon = Cesium.Color.BLUE.withAlpha(0.6)
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
        pixelSize: 6,
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
    var id = drawShapePolygon(activeShapePoints).id;
    
     addLineInfoBox(id, activeShapePoints);

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
        let dynamicPositions = new Cesium.CallbackProperty(function () {
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

/** Función que pinta un polígono a partir de unas coordenadas de entrada */
function drawPolygonInput() {
	/** Se solicita al usuario que introduzca las coordenadas */
	let coordinatesArray = prompt(
		'Please enter latitude and longitude in the following format: ' +
			'0.0,0.0,10.0,0.0,10.0,10.0,0.0,10.0,0.0,0.0',
		'0.0,0.0,10.0,0.0,10.0,10.0,0.0,10.0,0.0,0.0'
	)

	/** Se define el color del polígono */
	let colorPolygon = Cesium.Color.fromCssColorString('#FF0000').withAlpha(0.6)

	/** Se definen las coordenadas como un array */
	let coordinates = coordinatesArray.split(',')

	/** Se crea la entidad de polígono a partir del array y las propiedades */
	viewer.entities.add({
		polygon: {
			hierarchy: Cesium.Cartesian3.fromDegreesArray(coordinates),
			height: 0,
			material: colorPolygon,
			outline: false
		}
	})
}

/** Función que pinta un polígono a partir de unas coordenadas de entrada */
function drawPolygonMultiInput() {
  return
}

/** Función que pinta dibuja polígonos a mano alzada en el visor del mapa */
function drawPolygonFreehand() {

  /** Se definen las variables a utilizar */
  let colorPolygon = Cesium.Color.BLUE.withAlpha(0.6);
  let drawing = false
  let polyline
  let positions = []
  let handler = new Cesium.ScreenSpaceEventHandler(viewer.canvas)

  /** El ratón cambia de puntero, para indicar que se puede poner un punto */
  document.body.style.cursor = 'copy'

    /** Se producirá este evento cuando se pinche en el visor */
    handler.setInputAction(function (click) {

      /** En caso de que no exista ningún polígono (primer uso) */
        if (!drawing) {
          /** Se define la polilínea siguiendo al puntero del ratón */
          polyline = viewer.entities.add({
            polyline : {
              positions : new Cesium.CallbackProperty(function() {
                return positions
              }, false),
            material : colorPolygon,
            }
          })
        
        /** Cuando se haya pintado el polígono que interesa */
        } else {
          /** Se define el polígono a partir de la polilínea */
         var id= viewer.entities.add({
            polygon: {
              hierarchy : {
                positions : positions
              },
            material : colorPolygon,
            outline : true,
            }
          }).id;
          
          addLineInfoBox(id, positions);

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

    },Cesium.ScreenSpaceEventType.LEFT_CLICK)

    /** Por si se usa el botón derecho para cerrar el polígono */
    handler.setInputAction(function (click) {
      viewer.entities.add({
        polygon: {
          hierarchy : {
            positions : positions
          },
        material : colorPolygon,
        outline : true,
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

    },Cesium.ScreenSpaceEventType.RIGHT_CLICK)
    
    /** Se producirá este evento cuando se mueva el puntero por el visor */
    handler.setInputAction(
      function (movement) {

        /** Se define una variable con el punto final del movimiento */
        let surfacePosition = viewer.camera.pickEllipsoid(movement.endPosition)
      
        /** En caso de que exista el dibujo del polígono y su punto final */
        if (drawing && Cesium.defined(surfacePosition)) {

          /** Se añade al listado de posiciones el punto final del movimiento */
          positions.push(surfacePosition)
        }

      },Cesium.ScreenSpaceEventType.MOUSE_MOVE)
}




/** Función que limpia TODO lo que se dibuje en el mapa */
function clearEntities() {
  viewer.entities.removeAll()
}




/** Función que muestra las coordenadas del ratón */
function showMouseCartographicPosition() {

  /** Se genera la escucha del canvas */
  let handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
  
  /** Se genera una entidad 'invisible' que seguirá al ratón */
  let fakeEntity = viewer.entities.add({
    label : {
        show : false,
        showBackground : true,
        font : '14px monospace',
        horizontalOrigin : Cesium.HorizontalOrigin.LEFT,
        verticalOrigin : Cesium.VerticalOrigin.TOP,
        pixelOffset : new Cesium.Cartesian2(15, 0)
    }
  })

  /** Se producirá este evento cuando se mueva el cursor por el visor */
  handler.setInputAction(function(movement) {
    let cartesian = viewer.camera.pickEllipsoid(movement.endPosition, viewer.scene.globe.ellipsoid)
    if (cartesian) {
      let cartographic = Cesium.Cartographic.fromCartesian(cartesian)

      /** Se define la longitud y latitud, limitando los decimales a un par */
      let longitudeString = Cesium.Math.toDegrees(cartographic.longitude).toFixed(2)
      let latitudeString = Cesium.Math.toDegrees(cartographic.latitude).toFixed(2)

      /** Se obtienen las coordenadas de la falsa entidad */
      fakeEntity.position = cartesian
      fakeEntity.label.show = true

      /** Se incluye el texto de la ventanita de las coordenadas. */
      fakeEntity.label.text =
        'Longitude: ' + ('   ' + longitudeString).slice(-7) + '\u00B0' +
        '\n' + 'Latitude: ' + ('   ' + latitudeString).slice(-7) + '\u00B0'
    } else {
        fakeEntity.label.show = false
    }
  }, Cesium.ScreenSpaceEventType.MOUSE_MOVE)

  /** Cuando el usuario quiera terminar, tocará el botón izquierdo */
//  handler.setInputAction(function(click) {
//
//    /** Se destruye la variable que escucha, para matar la ventanita */
//    handler.destroy()
//
//    /** Se elimina la falsa entidad para que desaparezca la ventanita */
//    viewer.entities.remove(fakeEntity)
//
//  }, Cesium.ScreenSpaceEventType.LEFT_CLICK)

  /** Cuando el usuario quiera terminar, tocará el botón derecho */
//  handler.setInputAction(function(click) {
//
//    /** Se destruye la variable que escucha, para matar la ventanita */
//    handler.destroy()
//
//    /** Se elimina la falsa entidad para que desaparezca la ventanita */
//    viewer.entities.remove(fakeEntity)
//
//  }, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
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
    if (entity.name === "measurePolyline") {
      viewer.entities.remove(entity)
    }
  }

  /** Se comprueba si existe un etiquetado previo, y dado el caso lo elimina
   * Esto se mete por separado porque en el mismo FOR peta */
  for (entity of viewer.entities.values) {
    if (entity.name === "measureLabel") {
      viewer.entities.remove(entity)
    }
  }

  /** El ratón cambia de puntero, para indicar que se puede poner un punto */
  document.body.style.cursor = 'copy'
  
  /** Se genera la entidad de punto */
  function createPoint(pointCoordinates) {
    let point = viewer.entities.add({
      name: 'temporalPoint',
      position : pointCoordinates,
      point : {
        color : colorPoint,
        pixelSize : 6,
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
      name: "measurePolyline",
      polyline: {
        positions: positionData,
        material : new Cesium.PolylineDashMaterialProperty({
          color : colorPolyline,
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
    let finalPosition = polyline.polyline.positions.getValue()
    [polyline.polyline.positions.getValue().length - 1]
    
    /** Se genera el elipsoide geodésico a partir de las coordenadas */
    let ellipsoidGeodesic = new Cesium.EllipsoidGeodesic(
      Cesium.Ellipsoid.WGS84.cartesianToCartographic(initialPosition),
      Cesium.Ellipsoid.WGS84.cartesianToCartographic(finalPosition))

    let midPoint = ellipsoidGeodesic.interpolateUsingFraction(0.5)
    
    /** Se define la etiqueta que aparecerá encima de la regla */
    viewer.entities.add({
      name: 'measureLabel',
      position : new Cesium.Cartesian3.fromRadians(
        midPoint.longitude,
        midPoint.latitude,
        midPoint.height),
      label : {
        text: (ellipsoidGeodesic.surfaceDistance * 0.001).toFixed(2) + 'km',
        style: Cesium.LabelStyle.FILL_AND_OUTLINE,
        outlineWidth : 2,
        font : '18px sans-serif',
        pixelOffset : new Cesium.Cartesian2(0.0, -20)
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
        let dynamicPositions = new Cesium.CallbackProperty(function () {
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
      if (counter >1) {

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

    if (counter >1) {
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

/** Función que carga una imagen SVG como capa de imagery en el mapa */
function drawRectangleInput(west, south, east, north) {
	/** Se genera una entidad de tipo rectángulo */
	viewer.entities.add({
		rectangle: {
			coordinates: Cesium.Rectangle.fromDegrees(west, south, east, north),
			material: Cesium.Color.RED.withAlpha(0.6)
		}
	})
}

/** Función que pinta un rectángulo al hacer click en el visor del mapa */
function drawRectangleClick() {
	/** Se define una variable que escucha los eventos de canvas de la escena */
	let handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)

	/** Se definen las variables a utilizar */
	let newRectangle
	let west
	let south
	let east
	let north
	let rectangleSelector = new Cesium.Rectangle()
	let cartesian = new Cesium.Cartesian3()
	let tempCartographic = new Cesium.Cartographic()
	let firstPoint = new Cesium.Cartographic()
	let firstPointSet = false
	let mouseDown = false

	/** Se dibuja el rectángulo cuando se pulsa el botón izquierdo + shift */
	handler.setInputAction(
		function drawSelector(movement) {
			/** Si el botón del ratón no se encuentra pinchado, no se pinta */
			if (!mouseDown) {
				return
			}

			/** Se rellena la variable con la localización de donde se pincha */
			cartesian = viewer.camera.pickEllipsoid(
				movement.endPosition,
				viewer.scene.globe.ellipsoid,
				cartesian
			)

			/** Si se ha rellenado la variable con la localización */
			if (cartesian) {
				/** Se obtiene la localización del puntero del ratón en cada momento */
				tempCartographic = Cesium.Cartographic.fromCartesian(
					cartesian,
					Cesium.Ellipsoid.WGS84,
					tempCartographic
				)

				/** Si no existe un primer punto definido, entonces éste lo será */
				if (!firstPointSet) {
					/** Se clona la localización del punto actual como punto inicial */
					Cesium.Cartographic.clone(tempCartographic, firstPoint)

					/** Se indica que éste es el primer punto */
					firstPointSet = true
				} else {
					/** Se define la coordenada del oeste */
					rectangleSelector.west = Math.min(
						tempCartographic.longitude,
						firstPoint.longitude
					)
					/** Se define la coordenada del sur */
					rectangleSelector.south = Math.min(
						tempCartographic.latitude,
						firstPoint.latitude
					)
					/** Se define la coordenada del este */
					rectangleSelector.east = Math.max(
						tempCartographic.longitude,
						firstPoint.longitude
					)
					/** Se define la coordenada del norte */
					rectangleSelector.north = Math.max(
						tempCartographic.latitude,
						firstPoint.latitude
					)
					/** Se muestra el polígono de creación */
					newRectangle.show = true
				}
			}
		},
		Cesium.ScreenSpaceEventType.MOUSE_MOVE,
		Cesium.KeyboardEventModifier.SHIFT
	)

	/** Se saca las coordenadas del rectángulo */
	let rectangleExtent = new Cesium.CallbackProperty(
		function getSelectorLocation(result) {
			return Cesium.Rectangle.clone(rectangleSelector, result)
		},
		false
	)

	/** Se atiende a cuando se pincha con el ratón y la tecla shift */
	handler.setInputAction(
		function startClickShift() {
			/** Se modifican las propiedades del visor para pintar el rectángulo */
			viewer.scene.screenSpaceCameraController.enableTranslate = false
			viewer.scene.screenSpaceCameraController.enableTilt = false
			viewer.scene.screenSpaceCameraController.enableLook = false
			viewer.scene.screenSpaceCameraController.enableCollisionDetection = false

			/** Se modifica el valor de la variable de control */
			mouseDown = true

			/** Se definen las coordenadas del rectángulo */
			newRectangle.rectangle.coordinates = rectangleExtent
		},
		Cesium.ScreenSpaceEventType.LEFT_DOWN,
		Cesium.KeyboardEventModifier.SHIFT
	)

	/** Se atiende a cuando se termina de pinchar con el ratón y la tecla shift */
	handler.setInputAction(
		function endClickShift() {
			/** Se reinician las propieadades del visor para dejarlo por defecto */
			viewer.scene.screenSpaceCameraController.enableTranslate = true
			viewer.scene.screenSpaceCameraController.enableTilt = true
			viewer.scene.screenSpaceCameraController.enableLook = true
			viewer.scene.screenSpaceCameraController.enableCollisionDetection = true

			/** Se modifica el valor de las variables de control */
			mouseDown = false
			firstPointSet = false

			/** Se definen las coordenadas del rectángulo */
			newRectangle.rectangle.coordinates = rectangleSelector

			/** Se saca cada coordenada del extent del rectángulo */
			east = newRectangle.rectangle.coordinates._value.east
			north = newRectangle.rectangle.coordinates._value.north
			west = newRectangle.rectangle.coordinates._value.west
			south = newRectangle.rectangle.coordinates._value.south
		},
		Cesium.ScreenSpaceEventType.LEFT_UP,
		Cesium.KeyboardEventModifier.SHIFT
	)

	/** Si se pincha con un simple click, se oculta el rectángulo generado */
	handler.setInputAction(function hideSelector() {
		newRectangle.show = false
	}, Cesium.ScreenSpaceEventType.LEFT_CLICK)

	/** Se genera una entidad de tipo rectángulo */
	newRectangle = viewer.entities.add({
		selectable: false,
		show: false,
		rectangle: {
			coordinates: rectangleExtent,
			material: Cesium.Color.RED.withAlpha(0.6)
		}
	})
}

/** Función que cambia el modo del visor en 2D/3D */
function changeSceneMode() {
  console.log(viewer.scene.mode)
  /** Se comprueba si se está en modo 3D */
  if (viewer.scene.mode === 3) {
    /** El visor pasa a 2D */
	$("#3d").text("3D");
    $("#drawPoint").show();
    $("#drawLine").show();
    $("#drawPolygon").show();
    $("#drawPolygonFree").show();
    $("#rule").show();
    viewer.scene.mode = Cesium.SceneMode.SCENE2D
  } else {
    /** El visor pasa a 3D */
	$("#3d").text("2D");
    $("#drawPoint").hide();
    $("#drawLine").hide();
    $("#drawPolygon").hide();
    $("#drawPolygonFree").hide();
    $("#rule").hide();
    viewer.scene.mode = Cesium.SceneMode.SCENE3D
  }
}