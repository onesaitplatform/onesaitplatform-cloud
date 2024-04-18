Para integrarlo con plataforma hay varios pasos:

- Primero de todo compilar los cambios o lo que se quiera en este proyecto con npm - npm run start para compilar y ver los cambios ( mirar bien la ruta del index.html del public) - Esta aplicacion esta dividida en miles de modulos que se añaden en el app.js de la aplicacion en el que esta dentro de src, e ir probando ( con la documentacion oficial) - luego coger la carpeta y llevarla a plataforma - Aqui pegar la carpeta según esta en static y luego el index te lo llevas a donde quieras, o llamas a ese index desde otro .html de donde quieras - ESTE ES UN PASO SUPER IMPORTANTE, RUTAS RELATIVAS CON THYMELEAF, DEJO EJEMPLO DE COMO QUEDARIAN
  ////////////////////////////////////////////////////////////////////////////////
  <link rel="stylesheet" type="text/css" media="all"
  		th:href="@{/static/bmpnio/public/vendor/bpmn-js/assets/diagram-js.css}" />
  <link rel="stylesheet" type="text/css" media="all"
  		th:href="@{/static/bmpnio/public/vendor/bpmn-js/assets/bpmn-font/css/bpmn-embedded.css}" />
  <link rel="stylesheet" th:href="@{/static/bmpnio/public/vendor/bpmn-js/assets/bpmn-js.css}" />
  <link rel="stylesheet" type="text/css" media="all"
  		th:href="@{/static/bmpnio/public/vendor/bpmn-js-properties-panel/assets/properties-panel.css}" />

      <link rel="stylesheet" th:href="@{/static/bmpnio/public/app.css}"
      <script th:src="@{/static/bmpnio/public/app.js}">
      //////////////////////////////////////////////////////////////////////////////////////////////////////////

      - Ya se inicializa en el app.js interno la instancia de bpmnio por tanto las funciones estan disponibles, puedes usarlas como se ve en el index.html que hay en static, dejo
      	de ejemplo el index.html aqui para que se copie y pegue y funcione todo perfecto, al menos con un base
      - Cualquier duda preguntar a gcmagana

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////\*
  <head>
  	<meta charset="utf-8">
  	<title>bpmn-js properties panel demo</title>

      <link rel="stylesheet" type="text/css" media="all"
      	th:href="@{/static/bmpnio/public/vendor/bpmn-js/assets/diagram-js.css}" />
      <link rel="stylesheet" type="text/css" media="all"
      	th:href="@{/static/bmpnio/public/vendor/bpmn-js/assets/bpmn-font/css/bpmn-embedded.css}" />
      <link rel="stylesheet" th:href="@{/static/bmpnio/public/vendor/bpmn-js/assets/bpmn-js.css}" />
      <link rel="stylesheet" type="text/css" media="all"
      	th:href="@{/static/bmpnio/public/vendor/bpmn-js-properties-panel/assets/properties-panel.css}" />

      <link rel="stylesheet" th:href="@{/static/bmpnio/public/app.css}" />
      <style>
      	.buttons {
      		position: unset;
      	}
      	.bio-properties-panel-header{
      		border-top: 1px solid hsl(225, 10%, 75%);
      		border-right: 1px solid hsl(225, 10%, 75%);
      	}
      	.bio-properties-panel-scroll-container{
      		border-right: 1px solid hsl(225, 10%, 75%);
      		border-bottom: 1px solid hsl(225, 10%, 75%);
      	}
      </style>

  </head>

<body>
	<div>
		<div style="max-height:calc(100rem - 250px)" class="content with-diagram" id="js-drop-zone">

    		<div class="message intro">
    			<div class="note">
    				Drop BPMN diagram from your desktop or <a id="js-create-diagram" href>create a new diagram</a> to
    				get started.
    			</div>
    		</div>

    		<div class="message error">
    			<div class="note">
    				<p>Ooops, we could not display the BPMN 2.0 diagram.</p>

    				<div class="details">
    					<span>Import Error Details</span>
    					<pre></pre>
    				</div>
    			</div>
    		</div>

    		<div class="canvas" id="js-canvas"></div>
    		<div class="properties-panel-parent" id="js-properties-panel"></div>
    	</div>

    	<ul class="buttons">

    		<li>
    			<a id="js-download-diagram" href title="download BPMN diagram">
    				BPMN diagram
    			</a>
    		</li>
    		<li>
    			<a id="js-download-svg" href title="download as SVG image">
    				SVG image
    			</a>
    		</li>
    		<button id="zoom-in-button">Zoom In</button>
    		<button id="zoom-out-button">Zoom Out</button>
    		<button id="zoom-reset-button">Zoom Reset</button>

    	</ul>
    </div>

</body>
<script th:src="@{/static/bmpnio/public/app.js}">

</script>
<script>
	var container = $("#js-drop-zone");
	// Selecciona los elementos de botón
	const zoomInButton = document.getElementById('zoom-in-button');
	const zoomOutButton = document.getElementById('zoom-out-button');
	const zoomResetButton = document.getElementById('zoom-reset-button');

    // Agrega los controladores de evento para los botones de zoom
    zoomInButton.addEventListener('click', zoomIn);
    zoomOutButton.addEventListener('click', zoomOut);
    zoomResetButton.addEventListener('click', zoomReset);
    var canvas = $("#js-canvas");
    const xmlData = `<?xml version="1.0" encoding="UTF-8"?>

<bpmn2:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" id="sample-diagram" targetNamespace="http://bpmn.io/schema/bpmn" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd">
<bpmn2:process id="Process_1" isExecutable="false">
<bpmn2:startEvent id="StartEvent_1" />
<bpmn2:task id="Activity_1ynwuc6" />
<bpmn2:endEvent id="Event_0s0fuzr" />
<bpmn2:intermediateThrowEvent id="Event_0xfsdat" />
<bpmn2:startEvent id="Event_19n9got" />
<bpmn2:startEvent id="Event_0b152c9" />
<bpmn2:group id="Group_1fgw3vd" />
</bpmn2:process>
<bpmndi:BPMNDiagram id="BPMNDiagram_1">
<bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
<bpmndi:BPMNShape id="\_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
<dc:Bounds x="412" y="240" width="36" height="36" />
</bpmndi:BPMNShape>
<bpmndi:BPMNShape id="Activity_1ynwuc6_di" bpmnElement="Activity_1ynwuc6">
<dc:Bounds x="440" y="410" width="100" height="80" />
</bpmndi:BPMNShape>
<bpmndi:BPMNShape id="Event_0s0fuzr_di" bpmnElement="Event_0s0fuzr">
<dc:Bounds x="792" y="302" width="36" height="36" />
</bpmndi:BPMNShape>
<bpmndi:BPMNShape id="Event_0xfsdat_di" bpmnElement="Event_0xfsdat">
<dc:Bounds x="312" y="240" width="36" height="36" />
</bpmndi:BPMNShape>
<bpmndi:BPMNShape id="Event_19n9got_di" bpmnElement="Event_19n9got">
<dc:Bounds x="212" y="482" width="36" height="36" />
</bpmndi:BPMNShape>
<bpmndi:BPMNShape id="Event_0b152c9_di" bpmnElement="Event_0b152c9">
<dc:Bounds x="502" y="632" width="36" height="36" />
</bpmndi:BPMNShape>
<bpmndi:BPMNShape id="Group_1fgw3vd_di" bpmnElement="Group_1fgw3vd">
<dc:Bounds x="420" y="560" width="300" height="300" />
</bpmndi:BPMNShape>
</bpmndi:BPMNPlane>
</bpmndi:BPMNDiagram>
</bpmn2:definitions>
`
if (!xmlData)
openDiagram(xmlData);

    document.getElementById('data-getter').addEventListener('click', function () {
    	// Verifica si la instancia de bpmnModeler está disponible
    	if (window.bpmnModeler) {
    		getData()

    	} else {
    		console.error('La instancia de bpmnModeler no está disponible');
    	}
    });
    document.getElementById('data-input').addEventListener('click', function () {

    	if (window.bpmnModeler) {
    		openDiagram(xmlData)
    		console.log(xmlData, window.bpmnModeler);
    	} else {
    		console.error('La instancia de bpmnModeler no está disponible');
    	}
    });

    async function openDiagram(xml) {
    	console.log(window.bpmnModeler)
    	try {
    		await bpmnModeler.importXML(xml);
    		container.removeClass("with-error").addClass("with-diagram");

    	} catch (err) {
    		container.removeClass("with-diagram").addClass("with-error");

    		container.find(".error pre").text(err.message);

    		console.error(err);
    	}
    }
    async function getData() {
    	try {
    		const {xml} = await bpmnModeler.saveXML({format: true});
    		console.log(xml)

    	} catch (err) {
    		console.log("Error happened saving XML: ", err);
    	}
    }

    function zoomIn() {
    	const canvas = bpmnModeler.get('canvas');
    	const zoomLevel = canvas.zoom() * 1.1; // Incrementa el zoom en un 10%

    	canvas.zoom(zoomLevel);
    }

    // Zoom out
    function zoomOut() {
    	const canvas = bpmnModeler.get('canvas');
    	const zoomLevel = canvas.zoom() * 0.9; // Disminuye el zoom en un 10%

    	canvas.zoom(zoomLevel);
    }

    // Zoom reset (100%)
    function zoomReset() {
    	const canvas = bpmnModeler.get('canvas');
    	canvas.zoom('fit-viewport');
    }

</script>

</html>
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////*/
