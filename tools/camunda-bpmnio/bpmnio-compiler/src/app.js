import $ from "jquery";
import BpmnModeler from "bpmn-js/lib/Modeler";

import { debounce } from "min-dash";
import ZoomScrollModule from "diagram-js/lib/navigation/zoomscroll/ZoomScroll";

import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule,
  //ZeebePropertiesProviderModule,
} from "bpmn-js-properties-panel";
//import ZeebeBpmnModdle from "zeebe-bpmn-moddle/resources/zeebe.json";
// use Camunda BPMN Moddle extension
// import CamundaExtensionModule from "camunda-bpmn-moddle/lib";

// // use Camunda BPMN namespace
// import camundaModdleDescriptors from "camunda-bpmn-moddle/resources/camunda";

import diagramXML from "../resources/newDiagram.bpmn";
import CamundaBpmnModdle from "camunda-bpmn-moddle/resources/camunda.json";
var container = $("#js-drop-zone");

var canvas = $("#js-canvas");

var bpmnModeler = new BpmnModeler({
  container: canvas,
  keyboard: { bindTo: document },
  propertiesPanel: {
    parent: "#js-properties-panel",
  },
  additionalModules: [
    BpmnPropertiesPanelModule,
    BpmnPropertiesProviderModule,
    CamundaPlatformPropertiesProviderModule,
    ZoomScrollModule, // Agrega el módulo de zoom y desplazamiento
    // ZoomScrollPadModule, // Agrega el módulo de zoom y desplazamiento para pad
    // MoveCanvasModule, // Agrega el módulo de movimiento del lienzo
    // MoveCanvasKeyboardModule, // Agrega el módulo de movimiento del lienzo mediante teclado
    //ZeebePropertiesProviderModule,
    // CamundaExtensionModule,
  ],
  moddleExtensions: {
    camunda: CamundaBpmnModdle,
  },
});
window.bpmnModeler = bpmnModeler;
container.removeClass("with-diagram");

function createNewDiagram() {
  createDiagram();
}
async function createDiagram() {
  try {
    bpmnModeler.createDiagram();

    container.removeClass("with-error").addClass("with-diagram");
  } catch (err) {
    container.removeClass("with-diagram").addClass("with-error");

    container.find(".error pre").text(err.message);

    console.error(err);
  }
}
async function openDiagram(xml) {
  try {
    await bpmnModeler.importXML(xml);

    container.removeClass("with-error").addClass("with-diagram");
  } catch (err) {
    container.removeClass("with-diagram").addClass("with-error");

    container.find(".error pre").text(err.message);

    console.error(err);
  }
}

function registerFileDrop(container, callback) {
  function handleFileSelect(e) {
    e.stopPropagation();
    e.preventDefault();

    var files = e.dataTransfer.files;

    var file = files[0];

    var reader = new FileReader();

    reader.onload = function (e) {
      var xml = e.target.result;

      callback(xml);
    };

    reader.readAsText(file);
  }

  function handleDragOver(e) {
    e.stopPropagation();
    e.preventDefault();

    e.dataTransfer.dropEffect = "copy"; // Explicitly show this is a copy.
  }

  container.get(0).addEventListener("dragover", handleDragOver, false);
  container.get(0).addEventListener("drop", handleFileSelect, false);
}

////// file drag / drop ///////////////////////

// check file api availability
if (!window.FileList || !window.FileReader) {
  window.alert(
    "Looks like you use an older browser that does not support drag and drop. " +
      "Try using Chrome, Firefox or the Internet Explorer > 10."
  );
} else {
  registerFileDrop(container, openDiagram);
}

// bootstrap diagram functions

$(function () {
  $("#js-create-diagram").click(function (e) {
    e.stopPropagation();
    e.preventDefault();

    createNewDiagram();
  });

  var downloadLink = $("#js-download-diagram");
  var downloadSvgLink = $("#js-download-svg");

  $(".buttons a").click(function (e) {
    if (!$(this).is(".active")) {
      e.preventDefault();
      e.stopPropagation();
    }
  });

  function setEncoded(link, name, data) {
    var encodedData = encodeURIComponent(data);

    if (data) {
      link.addClass("active").attr({
        href: "data:application/bpmn20-xml;charset=UTF-8," + encodedData,
        download: name,
      });
    } else {
      link.removeClass("active");
    }
  }

  var exportArtifacts = debounce(async function () {
    try {
      const { svg } = await bpmnModeler.saveSVG();

      setEncoded(downloadSvgLink, "diagram.svg", svg);
    } catch (err) {
      console.error("Error happened saving SVG: ", err);

      setEncoded(downloadSvgLink, "diagram.svg", null);
    }

    try {
      const { xml } = await bpmnModeler.saveXML({ format: true });

      setEncoded(downloadLink, "diagram.bpmn", xml);
    } catch (err) {
      console.log("Error happened saving XML: ", err);

      setEncoded(downloadLink, "diagram.bpmn", null);
    }
  }, 500);

  bpmnModeler.on("commandStack.changed", exportArtifacts);
});
