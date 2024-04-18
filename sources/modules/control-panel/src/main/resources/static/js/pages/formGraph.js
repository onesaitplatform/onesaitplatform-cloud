const options = {
  manipulation: false,
  interaction: {
    dragNodes: true,
    dragView: true,
    hover: true,
    navigationButtons: true,
    keyboard: true,
  },
  layout: {
    improvedLayout: false,
  },
  physics: {
    enabled: true,
    hierarchicalRepulsion: {
      centralGravity: 0.0,
      springLength: 500,
      springConstant: 0.01,
      nodeDistance: 200,
      damping: 0.09,
    },
    solver: "hierarchicalRepulsion",
    barnesHut: {
      gravitationalConstant: -25000,
      centralGravity: 2,
      springLength: 125,
      springConstant: 0.03,
      damping: 1,
      avoidOverlap: 1,
    },
    maxVelocity: 10,
    minVelocity: 1,
  },
  nodes: {
    shape: "image",
    shapeProperties: {
      borderRadius: 10,
    },
    borderWidth: 1,
    borderWidthSelected: 2,
    chosen: true,

    font: {
      color: "#1B6EAA",
      size: 18, // px
      face: "Arial",
      background: "none",
      strokeWidth: 0, // px
      strokeColor: "#ffffff",
      align: "center",
    },
    margin: {
      top: 20,
      right: 20,
      bottom: 20,
      left: 20,
    },
    color: {
      border: "#E6E8E9",
      background: "#FFF",
      highlight: {
        border: "#1B6EAA",
        background: "#FFF",
      },
      hover: {
        border: "#1B6EAA",
        background: "#FFF",
      },
    },
  },
  edges: {
    widthConstraint: true,
    shadow: true,
    smooth: true,
    physics: true,
    smooth: {
      type: "cubicBezier",
      forceDirection: "horizontal",
      roundness: 1,
    },
    font: {
      size: 14,
    },
    color: {
      color: "#3982D0",
      highlight: "#3982D0",
      hover: "#3982D0",
      inherit: "from",
      opacity: 1.0,
    },
    arrows: {
      from: {
        enabled: true,
        scaleFactor: 0.4,
        type: "arrow",
      },
      to: {
        enabled: true,
        imageHeight: 6,
        imageWidth: 6,
        scaleFactor: 0,
        type: "image",
        src: "/controlpanel/vendor/vis/img/dot.svg",
      },
    },
    endPointOffset: {
      from: -4,
      to: -4,
    },
    hoverWidth: 0,
    labelHighlightBold: true,
  },
  autoResize: true,
  width: "100%",
  height: graphHeight + "px",
};
let dataCopy = null;

$('li[data-lang-tooltip="mnu-only-arr"] a').on("click", function () {
  actualMode = "onlyNodes";
  options.layout = {
    improvedLayout: true,
  };

  fillFormGraph(dataCopy);
});
$('li[data-lang-tooltip="mnu-jer-aba"] a').on("click", function () {
  actualMode = "hier";
  options.layout = {
    hierarchical: {
      nodeSpacing: 100,
      direction: "UD",
    },
  };

  fillFormGraph(dataCopy);
});

$('li[data-lang-tooltip="mnu-graph-arr"] a').on("click", function () {
  actualMode = "graph";
  options.layout = {
    improvedLayout: true,
  };

  fillFormGraph(dataCopy);
});

function fillFormGraph(data) {
  dataCopy = data;
  var handleGraphHeight = function () {
    $("#btn-addH").on("click", function () {
      $("#networkFormVis").height(function (index, height) {
        return height + 100;
      });
      graphHeight = graphHeight + 100;
      network.redraw();
    });

    $("#btn-remH").on("click", function () {
      $("#networkFormVis").height(function (index, height) {
        return height - 100;
      });
      if (parseInt($("#networkFormVis").css("height")) <= 500) {
        $("#networkFormVis").css("height", 500);
        graphHeight = 500;
      } else {
        graphHeight = graphHeight - 100;
      }
    });

    $("#btn-resH").on("click", function () {
      $("#networkFormVis").css("height", graphHeight);
    });
  };

  var generateNodeInfoHeaderHTML = function (currentNode) {
    if (currentNode.id == "fixedParent") {
      return false;
    }
    var nodeInfoHTML =
    '<div class="network-info-style-main">' +
      '<div class="col-md-6">' +
      '<p style="margin:0;font-weight: 500;" title="' +
      currentNode.realName +
      '">' +
      truncateText(currentNode.realName) +
      "</p>" +
      "</div>" +
      '<div class="col-md-6 text-right">' +
      '<button class="btn btn-primary btn-primary-save" onclick="redirectToForm(\'' +
      currentNode.code +
      "', 'form', 'show')\" title=\"" +
      translations.buttonLabel +
      '">' +
      truncateText(translations.buttonLabel) +
      "</button>" +
      "</div>" +
    "</div>" +
      "</div>" +
       '<div class="col-md-12">'   +
    '<p style="margin: 5px 0;">' +
     (currentNode.options && currentNode.options.fullObject.formItems ? translations.nodeDescription : '') + 
    '</p>' +
      "</div>"

    return nodeInfoHTML;
  };

  var truncateText = function (text) {
    if (text.length > 15) {
      return text.substring(0, 15) + "...";
    }
    return text;
  };

  var generateNodeInfoHTML = function (currentNode, nodes) {
    if (currentNode.options && currentNode.options.fullObject.formItems) {
      var nodeInfoHTML =

        "<table>" +
        "<thead>" +
        '<tr style="height:40px">' +
        '<th class="bg-grey-steel  text-center font-grey-gallery">' +
        translations.type +
        "</th>" +
        '<th class="bg-grey-steel  text-center font-grey-gallery">' +
        translations.name +
        "</th>" +
        '<th class="bg-grey-steel  text-center font-grey-gallery">' +
        translations.address +
        "</th>" +
        '<th class="bg-grey-steel  text-center font-grey-gallery">' +
        translations.actions +
        "</th>" +
        "</tr>" +
        "</thead>" +
        "<tbody>";

      currentNode.options.fullObject?.formItems.forEach(function (
        itemNotParsed
      ) {
        const item = JSON.parse(itemNotParsed);
        const isFormcode = item.formCode ? item.formCode : item.redirect;

    nodeInfoHTML +=
        "<tr>" +
        '<td class="long-text text-center ">' +
        (item.formCode ? "Form" : "Redirect") +
        "</td>" +
        '<td class="long-text text-center ">' +
        truncateText(item.label) +
        "</td>" +
        '<td class="long-text text-center ">' +
        truncateText(isFormcode) +
        "</td>" +
        '<td class="long-text text-center ">';


    nodeInfoHTML +=
        '<span class="btn btn-xs btn-no-border icon-on-table color-blue tooltips" ' +
        'data-container="body" data-placement="bottom" ' +
        'title="' + translations.buttonShow + '" ' +
        'onclick="redirectToForm(\'' +
        (!item?.formCode ? item.redirect : generateDynamicUrl(item.formCode, nodes)) +
        "', '" +
        (!item?.formCode ? "redirect" : "form") +
        "', 'show')\">" +
        '<i class="la la-eye font-hg"></i>' +
        '</span>';

    if (item.formCode) {
        nodeInfoHTML +=
            '<span style="margin-left: 5px;" class="btn btn-xs btn-no-border icon-on-table color-blue tooltips" ' +
            'data-container="body" data-placement="bottom" ' +
            'title="' + translations.buttonUpdate + '" ' +
            'onclick="redirectToForm(\'' +
            generateDynamicUrl(item.formCode, nodes) +
            "', 'form', 'update')\">" +
            '<i class="icon-edit"></i>' +
            '</span>';
    }

    nodeInfoHTML += "</td>" + "</tr>";
      });

      nodeInfoHTML += "</tbody>";
      return nodeInfoHTML;
    } else {
      return false;
    }
  };

  var createGraphInfoTable = function () {
    var strInfo = "";
    var propertyCol = graphReg.propertyCol,
      valueCol = graphReg.valueCol,
      tableName = graphReg.tableName,
      tableSource = graphReg.tableSource,
      tableLinkS = graphReg.tableLinkS,
      tableLinkC = graphReg.tableLinkC,
      tableLinkBtn = graphReg.tableLinkBtn;
    $("#TableFormInfoNetwork").hide();
    $("#TableFormInfoNetworkHeader").hide();

    strInfo =
      "<thead>" +
      '<tr style="height:40px">' +
      '<th class="bg-grey-steel text-center font-grey-gallery" title="' +
      propertyCol +
      '">' +
      truncateText(propertyCol) +
      "</th>" +
      '<th class="bg-grey-steel  text-center font-grey-gallery" title="' +
      valueCol +
      '">' +
      truncateText(valueCol) +
      "</th>" +
      "</tr>" +
      "</thead>" +
      "<tbody>" +
      "	<tr>" +
      '		<td class="uppercase  text-center font-grey-mint" title="' +
      tableName +
      '">' +
      truncateText(tableName) +
      "</td>" +
      '		<td id="id_nombre" class="long-text  text-center "></td>' +
      "	</tr>" +
      "	<tr>" +
      '		<td class="uppercase  text-center font-grey-mint" title="' +
      tableSource +
      '">' +
      truncateText(tableSource) +
      "</td>" +
      '		<td id="id_source" class="long-text  text-center "> </td>' +
      "	</tr>" +
      '	<tr id="id_tr_enlaceS">' +
      '		<td class="uppercase  text-center font-grey-mint" title="' +
      tableLinkS +
      '">' +
      truncateText(tableLinkS) +
      "</td>" +
      '		<td class="long-text  text-center "><a id="id_enlaceS" class="btn btn-circle blue btn-outline" title="' +
      tableLinkBtn +
      '"><span> ' +
      truncateText(tableLinkBtn) +
      "</span> </a></td>" +
      "	</tr>" +
      '	<tr id="id_tr_enlaceC">' +
      '		<td class="uppercase  text-center font-grey-mint" title="' +
      tableLinkC +
      '">' +
      truncateText(tableLinkC) +
      "</td>" +
      '		<td class="long-text  text-center "><a id="id_enlaceC" class="btn btn-circle blue btn-outline" title="' +
      tableLinkBtn +
      '"><span> ' +
      truncateText(tableLinkBtn) +
      "</span> </a></td>" +
      "	</tr>";
    strInfo += "</tbody>";

    $("#TableFormInfoNetwork").empty();
    $("#TableFormInfoNetworkHeader").empty();
    $("#TableFormInfoNetwork").html(strInfo);
  };

  var drawGraphInfo = function (currentNode) {
    $("#TableFormInfoNetwork").show();
    $("#TableFormInfoNetworkHeader").show();
  };

  var cleanTable = function () {
    $("#id_nombre").html();
    $("#id_source").html();
    if ($("#id_enlaceS")) {
      $("#id_enlaceS").removeAttr("href");
    }
    if ($("#id_enlaceC")) {
      $("#id_enlaceC").removeAttr("href");
    }
    $("#id_tr_enlaceS,#id_tr_enlaceC").hide();
  };

  var createRelationsInfoTable = function () {
    logControl ? console.log("createRelationsInfoTable() -> ") : "";

    var strInfo = "";
    var propertyCol = graphReg.propertyCol,
      valueCol = graphReg.valueCol,
      tableName = graphReg.tableName,
      tableSource = graphReg.tableSource,
      tableLinkS = graphReg.tableLinkS,
      tableLinkC = graphReg.tableLinkC,
      tableLinkBtn = graphReg.tableLinkBtn;

    $("#TableInfoRelations").hide();
    strInfo =
      '<caption style="text-align:center">Attribute relations</caption><thead></thead>' +
      "<tbody></tbody>";

    $("#TableInfoRelations").empty();
    $("#TableFormInfoNetworkHeader").empty();
    $("#TableInfoRelations").html(strInfo);
  };

  var toggleGraphInfoTable = function () {
    $("#btn-graphInfo").on("click", function () {
      $("#TableFormInfoNetwork").fadeToggle();
    });
  };

  handleGraphHeight();
  createGraphInfoTable();
  var createSvgCluster = function (group) {
    var font = "13px Arial";
    var svgCluster = "";
    const icon =
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512"><path fill="#47759d" d="M448 75.2v361.7c0 24.3-19 43.2-43.2 43.2H43.2C19.3 480 0 461.4 0 436.8V75.2C0 51.1 18.8 32 43.2 32h361.7c24 0 43.1 18.8 43.1 43.2zm-37.3 361.6V75.2c0-3-2.6-5.8-5.8-5.8h-9.3L285.3 144 224 94.1 162.8 144 52.5 69.3h-9.3c-3.2 0-5.8 2.8-5.8 5.8v361.7c0 3 2.6 5.8 5.8 5.8h361.7c3.2 .1 5.8-2.7 5.8-5.8zM150.2 186v37H76.7v-37h73.5zm0 74.4v37.3H76.7v-37.3h73.5zm11.1-147.3l54-43.7H96.8l64.5 43.7zm210 72.9v37h-196v-37h196zm0 74.4v37.3h-196v-37.3h196zm-84.6-147.3l64.5-43.7H232.8l53.9 43.7zM371.3 335v37.3h-99.4V335h99.4z"/></svg>';

    var calculateClusterTextWidth = function (label, font) {
      const elementsWidth = 60;
      var totalWidth = 0;
      var inputText = label || "Node";
      var fontText = font || "15px Arial";

      var label_canvas = document.createElement("canvas");
      var context = label_canvas.getContext("2d");
      context.font = font;
      width = context.measureText(inputText).width;
      totalWidth = elementsWidth + Math.ceil(width);

      return totalWidth;
    };

    String.prototype.capitalize = function () {
      return this.charAt(0).toUpperCase() + this.slice(1);
    };

    let middle = Math.ceil(calculateClusterTextWidth(group, font) / 2 - 30);
    let counter = middle + 10;

    svgCluster +=
      '<svg xmlns="http://www.w3.org/2000/svg" width="' +
      calculateClusterTextWidth(group, font) +
      '" height="60">' +
      '<rect x="0" y="0" width="100%" rx="4" ry="4" height="100%" fill="#FFFFFF" stroke="#3498db" style="stroke-width:4"></rect>' +
      '<foreignObject x="12" y="12" width="100%" height="100%">' +
      '<div style="display:flex; align-items:center; gap:8px; margin-top:3px; background-color: transparent;" xmlns="http://www.w3.org/1999/xhtml">' +
      '<span style="font-size: 24px; width:24px;heigth:24px">' +
      icon +
      "</span>" + // Ajusta el tamaño del icono
      '<span style="text-align: left; font-family: Soho, Arial; font-size:14px; color:#FFFFF; font-weight: bolder">' +
      group +
      "</span>" +
      "</div>" +
      "</foreignObject>" +
      "</svg>";

    var imageUrl =
      "data:image/svg+xml;charset=utf-8," + encodeURIComponent(svgCluster);

    return imageUrl;
  };
  const nodes = new vis.DataSet();
  const edges = new vis.DataSet();
  const nodesWithFormCode = [];

  if (actualMode !== "onlyNodes") {
    const fixedParentNode = {
      id: "fixedParent",
      label: projectMainId,
      shape: "box",
      borderWidth: 3,
      widthConstraint: {
        minimum: 150,
      },
      heightConstraint: {
        minimum: 40,
      },

      font: {
        color: "#051724",
        size: 21,
      },
    };
    nodes.add(fixedParentNode);
  }

  data.forEach((form) => {
    let schema = null;
    let objectMatches = null;
    let cleanedMatches = null;
    let itemsForLines = null;
    if (form.jsonSchema) {
      try {
        schema = JSON.parse(form.jsonSchema);
        objectMatches = form.jsonSchema.match(
          /({[^}]*"formCode"[^}]*}|{[^}]*"redirect"[^}]*})/g
        );

        schema.componentsArray = Array.isArray(schema.components)
          ? schema.components
          : [];

        if (objectMatches) {
          cleanedMatches = objectMatches.map((match) => {
            const componentsMatch = match.replace(/{"components":\[/, "");
            return componentsMatch;
          });
        }

        if (cleanedMatches)
          itemsForLines = cleanedMatches.map((el) => JSON.parse(el));
      } catch (error) {
        console.error("Error parsing jsonSchema:", error);
      }
    }

    const clusterImage = createSvgCluster(form.name);

    const formNodeId = "form-" + form.id;
    const formNode = {
      id: formNodeId,
      code: form.code,
      realName: form.name,
      shape: "image",
      image: clusterImage,
      options: {
        fullObject: {
          schema: schema,
          formItems: cleanedMatches,
        },
      },
    };

    nodes.add(formNode);

    if (itemsForLines) {
      itemsForLines.forEach((el) => {
        if (el.formCode) {
          nodesWithFormCode.push({ node: formNode, itemsForLines: el });
        }
      });
    }
    if (actualMode !== "onlyNodes") {
      const formEdge = {
        id: formNodeId + "-fixedParent",
        from: formNodeId,
        to: "fixedParent",
      };
      edges.add(formEdge);
    }
  });

  $("#nodeInfoModal").hide();

  const container = document.getElementById("networkFormVis");
  const treeData = { nodes: nodes, edges: edges };
  const tree = new vis.Network(container, treeData, options);

  nodesWithFormCode.forEach((el, index) => {
    var allNodes = nodes.get();

    const targetNodeByFormCode = allNodes.find(
      (n) => n.code === el.itemsForLines.formCode
    );

    if (targetNodeByFormCode) {
      const allEdgesToTargetNode = edges.get({
        filter: (edge) => edge.to === targetNodeByFormCode.id,
      });

      let roundness = 0.2;
      let edgeIdByFormCode = null;

      let edgeStyle;
      const existingEdge = edges
        .get()
        .find(
          (edge) =>
            edge.from === el.node.id &&
            edge.to === targetNodeByFormCode.id &&
            edge.id !== edgeIdByFormCode
        );

      if (existingEdge) {
        edgeIdByFormCode = `${el.node.id}-${targetNodeByFormCode.id}-${index}`;
        roundness = 0.2 + index / 10;
      } else {
        edgeIdByFormCode = `${el.node.id}-${targetNodeByFormCode.id}`;
      }

      edgeStyle = {
        smooth: {
          type: "curvedCW",
          roundness: roundness,
        },
        dashes: el.node.id === targetNodeByFormCode.id,
        label: el.itemsForLines.label,
        arrows: {
          to: {
            enabled: true,
            scaleFactor: 0.4,
            type: "arrow",
          },
          from: {
            enabled: true,
            imageHeight: 6,
            imageWidth: 6,
            scaleFactor: 0,
            type: "image",
            src: "/controlpanel/vendor/vis/img/dot.svg",
          },
          /*middle: {
					        enabled: true,
					        imageHeight: 32,
					        imageWidth: 32,
					        scaleFactor: 1,
					        src: createSvgCluster(el.itemsForLines.label),
					        type: "image"
      },*/
        },
      };

      const edgeByFormCode = {
        id: edgeIdByFormCode,
        from: el.node.id,
        to: targetNodeByFormCode.id,
        ...edgeStyle,
      };
      edges.add(edgeByFormCode);
    }
  });

  // tree.on("selectEdge", function(event) {
  // 	const { edges } = event;
  // 	if (nodes.length === 1) {
  // 		const nodeId = nodes[0];
  // 		const currentNode = treeData.nodes.get(nodeId);
  // 	}
  // });

  tree.on("selectNode", function (event) {
    const { nodes } = event;
    if (nodes.length === 1) {
      const nodeId = nodes[0];
      const currentNode = treeData.nodes.get(nodeId);

      if (currentNode.id == "fixedParent") {
        $("#TableFormInfoNetworkHeader").empty();
        $("#TableFormInfoNetworkHeader").hide();
        $("#TableFormInfoNetwork").empty();
        return false;
      }
      const nodeInfoHTML = generateNodeInfoHTML(
        currentNode,
        treeData.nodes.get()
      );
      const nodeInfoHeaderHTML = generateNodeInfoHeaderHTML(currentNode);

      $("#TableFormInfoNetworkHeader").html(nodeInfoHeaderHTML);
      $("#TableFormInfoNetwork").html(nodeInfoHTML);

      drawGraphInfo(currentNode);
    }
  });

  tree.on("click", function (event) {
    if (!event.nodes.length) {
      $("#TableFormInfoNetworkHeader").empty();
      $("#TableFormInfoNetworkHeader").hide();
      $("#TableFormInfoNetwork").empty();
    }
  });
}

function generateDynamicUrl(nodeId, nodes) {
  let lastId = nodeId;
  if (nodes) {
    const itemFound = nodes.find((el) => el.realName === nodeId);
    if (itemFound) lastId = itemFound.code;
    else return lastId;
  }
  return lastId;
}

function redirectToForm(url, type, mode) {
  let changedUrl = "";

  if (type === "form") {
    changedUrl = "/controlpanel/forms/" + mode + "/" + url;
  } else if (type === "redirect") {
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      changedUrl = "http://" + url;
    } else {
      changedUrl = url;
    }
  } else {
    console.error("Tipo no reconocido:", type);
  }

  window.open(changedUrl, "_blank");
}
