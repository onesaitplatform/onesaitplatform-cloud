Report.List = (function () {
  "use-strict";
  var mountableModel = $("#table_parameters").find("tr.parameters-model")[0]
    .outerHTML;
  var csrf = {};
  csrf[headerJson.csrfHeaderName] = headerJson.csrfToken;
  var init = function () {
    $("#btn-report-create").on("click", function (e) {
      e.preventDefault();
      window.location = "/controlpanel/reports/create";
    });
    initTableEvents();
  };

  let isJSON = null;
  let reportType = null;
  let idReport = null;

  var ajaxDownload = function (url, httpMethod, payload, extension) {
    var http = new XMLHttpRequest();

    var params =
      "parameters=" + JSON.stringify(payload) + "&extension=" + extension;
    http.open(httpMethod, url, true);
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.setRequestHeader(headerReg.csrfHeaderName, headerReg.csrfToken);
    http.responseType = "blob";

    http.onload = function () {
      if (http.status === 200) {
        var filename = http
          .getResponseHeader("content-disposition")
          .split("=")[1];
        var contentType = http.getResponseHeader("content-type");
        var blob = new Blob([http.response], {
          type: contentType,
        });
        var link = document.createElement("a");
        link.href = window.URL.createObjectURL(blob);
        link.download = filename;

        document.body.appendChild(link);

        link.click();

        document.body.removeChild(link);
      } else {
        $.alert({
          title: "ERROR!",
          type: "red",
          theme: "light",
          content:
            "Problem executing your Report" +
            http.getResponseHeader("X-Download-Error"),
        });
      }
    };
    http.send(params);
  };

  var runReportWithParameters = function () {
    let parametersArray = [];
    let url = "";

    if (reportType === "WORD") {
      url = "/controlpanel/reports/download/MSTemplate/report/";

      if (isJSON) {
        var editedJson = editor.get();
        parametersArray = {jsonParameters: editedJson};
        //var jsonTextarea = document.getElementById('json-textarea');
        //parametersArray = jsonTextarea.value;
      } else {
        let checkedRows = [];
        let checkboxes = document.querySelectorAll('input[type="checkbox"]');
        checkboxes.forEach(function (checkbox) {
          if (checkbox.checked) {
            let row = checkbox.closest("tr");
            if (row) {
              let nameInput = row.querySelector('input[name="name[]"]');
              let valueInput = row.querySelector('input[name="value[]"]');
              let type = row.dataset.type;
			  let id = row.getAttribute("id");
              if (nameInput && valueInput && type) {
                let name = nameInput.value;
                let value = valueInput.value;

                let rowData = {
                  name: name,
                  value: value,
                  id:id
                };

                let nestedRows = row.querySelectorAll(".nested-row");
                let nestedData = [];

                nestedRows.forEach(function (nestedRow) {
                  let row = checkbox.closest("tr");
                  let nestedNameInput = nestedRow.querySelector(
                    'input[name="name[]"]'
                  );
                  let nestedValueInput = nestedRow.querySelector(
                    'input[name="value[]"]'
                  );
                  let typeChild = nestedRow.dataset.type;

                  if (nestedNameInput && nestedValueInput) {
                    let nestedName = nestedNameInput.value;
                    let nestedValue = nestedValueInput.value;
				  	let idChild = nestedRow.getAttribute("id");
				  
                    let nestedRowData = {
                      name: nestedName,
                      value: nestedValue,
                      id:idChild
                    };

                    nestedData.push(nestedRowData);
                  }
                });

                if (nestedData.length > 0) {
                  rowData.childs = nestedData;

                  // Eliminar los elementos hijos del array principal
                  checkedRows = checkedRows.filter(function (item) {
                    return !nestedData.some(function (nestedItem) {
                      return (
                        item.name === nestedItem.name &&
                        item.value === nestedItem.value
                      );
                    });
                  });
                }

                checkedRows.push(rowData);
              }
            }
          }
        });
        checkedRows.forEach((el) => {
          return el.childs ? (el.value = "") : el.value;
        });

        parametersArray = checkedRows;
        parametersArray = parametersArray.map(item => parseArrayToProps(item));
        
      }
      //LOGICA SI NO ES WORD
    } else {
      var id = $("#current-report").val();
      var elements = $("#table-body").find("tr");
      url = "/controlpanel/reports/download/report/";
      elements.each(function () {
        var check = $(this).find("input[name='checkparameter\\[\\]']");
        if (check.is(":checked")) {
          var name = $(this).find("input[name='name\\[\\]']").val();
          var value = $(this).find("input[name='value\\[\\]']").val();
          var type = $(this).find("input[name='type\\[\\]']").val();
          var parameter = {
            name: name,
            type: type,
            value: value,
            description: "",
          };
          parametersArray.push(parameter);
        }
      });
    }

    var extension = $("#export-type").val();

    if (extension == null || extension == "") extension = PDF;
    ajaxDownload(url + idReport, "POST", parametersArray, extension);
  };
  // Funcion para saber el tipo de report

  function getReportType(type) {
    const fileExtension = type.split(".")[1];

    let newType = "";
    if (fileExtension === "docx") {
      newType = "WORD";
    } else if (fileExtension === "jasper" || fileExtension === "jrxml") {
      newType = "JASPER";
    } else {
      newType = "";
    }
    return newType;
  }

  function parseArrayToProps(arr){
		
  const newElement = {
    name: arr.name,
    value: arr.value
  };

  if (Array.isArray(arr.childs)) {
    const groupedProps = {};
    newElement.value = []
    arr.childs.forEach(child => {
      if (child.id) {
        if (!groupedProps[child.id]) {
          groupedProps[child.id] = [];
        }

        groupedProps[child.id].push({
          name: child.name,
          value: child.value
        });
      }
    });

    for (const id in groupedProps) {
      if (groupedProps.hasOwnProperty(id)) {
        const propsArr = groupedProps[id];
        
        if(Array.isArray(newElement.value))
          newElement.value.push({ props: propsArr });
		}
	}
  }

  return newElement;
}

		
  
	
  let formButton = document.getElementById("form-button");
  let jsonButton = document.getElementById("json-button");
  let formRepresentation = document.getElementById("form-representation");
  let jsonRepresentation = document.getElementById("json-representation");

  var getWordParameters = function (id, item) {
    $.ajax({
      url: "/controlpanel/reports/" + id + "/parameters",
      type: "GET",
    })
      .done(function (data) {
        let parameters = null;
        let tableBody = document.getElementById("table-body");
        parameters = data.jsonParameters;
        isJSON = data.formType === "TEXTAREA";
        if (parameters == null || !parameters.length) {
          $("#table_parameters > tbody").html("");
          $.alert({
            title: "ERROR!",
            type: "red",
            theme: "light",
            content: "Error: Parameters are null or empty",
          });
          item.toggleClass("spinner la la-spinner la-spin icon-play ");
          return;
        } else {
          if ($("#parameters").attr("data-loaded") === "true") {
            $("#table_parameters > tbody").html("");
          }
          if (!isJSON) {
            jsonButton.style.display = "none";
            jsonRepresentation.style.display = "none";
            formButton.classList.add("active");
            formRepresentation.style.display = "block";
            formRepresentation.classList.remove("hide");
          } else {
            formButton.style.display = "none";
            formRepresentation.style.display = "none";
            jsonButton.style.display = "block";
            jsonRepresentation.classList.remove("hide");
            jsonRepresentation.style.display = "block";
          }
        }
        if (!isJSON) {
          parameters.forEach(function (element) {
            var row = createRow(element, false, tableBody, 0);
            tableBody.appendChild(row);
          });
        } else {
          editor = new jsoneditor.JSONEditor(container, options, 
            parameters,
          );
        }
        $("#parameters").removeClass("hide");
        $("#parameters").attr("data-loaded", true);

        $("#parametersModal").modal("show");
        $("#current-report").val(id);
        item.toggleClass("spinner la la-spinner la-spin icon-play ");
      })

      .fail(function (error) {
        item.toggleClass("spinner la la-spinner la-spin icon-play ");

        $.alert({
          title: "ERROR!",
          type: "red",
          theme: "light",
          content: "Could not get report parameters: " + error.responseText,
        });
      });
  };

  var getParameters = function (id, item) {
    $.ajax({
      url: "/controlpanel/reports/" + id + "/parameters",
      type: "GET",
    })
      .done(function (data) {
        let parameters = null;
        document.getElementById("table-body");
        parameters = data;

        if (parameters == null) {
          $("#table_parameters > tbody").html("");
        } else {
          if ($("#parameters").attr("data-loaded") === "true") {
            $("#table_parameters > tbody").html("");
            $("#table_parameters > tbody").append(mountableModel);
          }
          $("#table_parameters").mounTable(parameters, {
            model: ".parameters-model",
            noDebug: false,
          });
          jsonButton.style.display = "none";
          $("#parameters").removeClass("hide");
          $("#parameters").attr("data-loaded", true);
        }
        $("#parametersModal").modal("show");
        $("#current-report").val(id);

        item.toggleClass("spinner la la-spinner la-spin icon-play ");
      })
      .fail(function (error) {
        item.toggleClass("spinner la la-spinner la-spin icon-play ");
        $.alert({
          title: "ERROR!",
          type: "red",
          theme: "light",
          content: "Could not get report parameters: " + error.responseText,
        });
      });
  };

  // Agregar eventos click a los botones

  formButton.addEventListener("click", function () {
    formButton.classList.add("active");
    jsonButton.classList.remove("active");
    formRepresentation.classList.remove("hide");
    jsonRepresentation.classList.add("hide");
  });

  jsonButton.addEventListener("click", function () {
    formButton.classList.remove("active");
    jsonButton.classList.add("active");
    formRepresentation.classList.add("hide");
    jsonRepresentation.classList.remove("hide");
  });

  // funcion para crear elementos dinámicos dentro del modal

  function createRow(element, isChild, parentRow, level) {
    let row = document.createElement("tr");
    row.setAttribute("data-type", element.type);

    let checkboxCell = document.createElement("td");
    checkboxCell.style.width = "40px";
    if (!isChild) {
      let checkbox = document.createElement("input");
      checkbox.setAttribute("type", "checkbox");
      checkbox.style.width = "20px";
      checkbox.style.height = "20px";
      checkbox.checked = true;
      checkboxCell.appendChild(checkbox);
    }

    let nameCell = document.createElement("td");
    let nameInput = document.createElement("input");
    nameInput.setAttribute("type", "text");
    nameInput.setAttribute("name", "name[]");
    nameInput.setAttribute("readonly", "readonly");
    nameInput.classList.add("form-control");
    nameInput.value = element.name;
    nameCell.appendChild(nameInput);

    let valueCell = document.createElement("td");
    if (!element.childs || isChild) {
      let valueInput = document.createElement("input");
      valueInput.setAttribute("type", "text");
      valueInput.setAttribute("name", "value[]");
      valueInput.classList.add("form-control");
      valueInput.value = element.value;
      valueCell.appendChild(valueInput);
    }

    let actionCell = document.createElement("td");
    actionCell.classList.add("action-cell");
    const randomId = function (length = 6) {
      return Math.random()
        .toString(36)
        .substring(2, length + 2);
    };
    if (element.childs) {
      //if (element.type === "LIST") {
      row.style.display = "contents";
      if (!isChild) {
        let addButton = document.createElement("button");
        addButton.innerHTML = '<i class="la la-plus"></i>';
        addButton.classList.add("add-button");

        addButton.addEventListener("click", function () {
          if (element.childs) {
            const id = randomId();
            element.childs.forEach(function (child, index) {
              var childRow = createRow(child, true, row);
              if (parentRow) {
                row.appendChild(childRow);
                childRow.classList.add("nested-row");
                childRow.setAttribute("id", `${id}`);

                if (index === element.childs.length - 1) {
                  deleteTr(childRow, index);
                }
              } else {
                console.error("El elemento padre es nulo:", parentRow);
              }
            });
          } else {
            let childRow = createRow(
              {
                name: element.name,
                value: null,
              },
              true,
              row
            );
            const id = randomId();
            childRow.classList.add("nested-row");
            childRow.setAttribute("id", `${id}`);
            deleteTr(childRow);

            if (parentRow) {
              row.appendChild(childRow);
            } else {
              console.error("El elemento padre es nulo:", parentRow);
            }
          }
        });
        actionCell.appendChild(addButton);
      } else {
        row.classList.add("nested-row");
        checkboxCell.style.width = "40px";
      }
    } else {
      actionCell.innerHTML = "&nbsp;";
      row.classList.add("no-add-child");
    }

    row.appendChild(checkboxCell);
    row.appendChild(nameCell);
    row.appendChild(valueCell);
    row.appendChild(actionCell);

    if (element.childs) {
      //   if (element.type === "LIST" && element.childs) {
      const id = randomId();
      element.childs.forEach(function (child, index) {
        let nestedRow = createRow(child, true, row, level + 1);
        nestedRow.classList.add("nested-row");
        nestedRow.setAttribute("id", `${id}`);
        nestedRow.style.marginLeft = level * 20 + "px";
        if (index === element.childs.length - 1) {
          deleteTr(nestedRow, index);
        }
        row.appendChild(nestedRow, row.nextSibling);
      });
    }

    return row;
  }

  // Borrado de columna

  function deleteTr(childRow, index) {
    var deleteButton = document.createElement("button");
    deleteButton.innerHTML = '<i class="la la-minus"></i>';
    deleteButton.classList.add("delete-button");
    deleteButton.addEventListener("click", function () {
      var id = childRow.getAttribute("id");
      var siblings = Array.from(childRow.parentNode.children);
      var elementsToDelete = siblings.filter(function (sibling) {
        return sibling.getAttribute("id") === id;
      });
      elementsToDelete.forEach(function (element) {
        element.parentNode.removeChild(element);
      });
    });
    var actionCell = childRow.querySelector(".action-cell");
    if (actionCell) {
      actionCell.appendChild(deleteButton);
    }
  }

  function updateOptions(options) {
    var select = $("#export-type");
    select.empty();
    select.prepend(
      $("<option>").val("").text("Select extension for output report...")
    );

    options.forEach(function (option) {
      select.append($("<option>").val(option).text(option));
    });
  }

  function initTableEvents() {
    $(".report-play").each(function () {
      $(this)
        .off()
        .on("click", function (e) {
          e.preventDefault();
          const wordOptions = ["PDF", "DOCX"];

          const button = $(this);
          let icon = button.find("i.icon-play");
          button.prop("disabled", true);
          button.addClass("loading");
          icon.toggleClass("icon-play spinner la la-spinner la-spin");

          const id = $(this).data("id");
          idReport = id;
          reportType = getReportType($(this).data(`type`));
          $("#submit-params").prop("disabled", true);
          if (reportType === "WORD") {
            $("#jsoneditor").html("");
            getWordParameters(id, icon);
            updateOptions(wordOptions);
          } else {
            getParameters(id, icon);
            updateOptions(jasperOptions);
          }
        });
    });

    $("#export-type").on("change", function () {
      var val = $("#export-type").val();
      if (val == "") $("#submit-params").prop("disabled", true);
      else $("#submit-params").removeAttr("disabled");
    });

    $(".report-trash").each(function () {
      $(this)
        .off()
        .on("click", function (e) {
          e.preventDefault();
          var id = $(this).data("id");
          deleteReportDialog(id);
        });
    });

    $(".report-download").each(function () {
      $(this)
        .off()
        .on("click", function (e) {
          e.preventDefault();
          var id = $(this).data("id");
          $.fileDownload("/controlpanel/reports/download/report-design/" + id, {
            httpMethod: "GET",
            successCallback: function (url) {},
            failCallback: function (response, url) {
              $.alert({
                title: "ERROR!",
                type: "red",
                theme: "light",
                content: "Could not download report" + response,
              });
            },
          });
        });
    });

    $(".report-edit").each(function () {
      $(this)
        .off()
        .on("click", function (e) {
          e.preventDefault();
          var id = $(this).data("id");
          window.location = "/controlpanel/reports/edit/" + id;
        });
    });
  }

  var deleteReportDialog = function (id) {
    $.confirm({
      title: headerReg.reportDelete,
      theme: "light",
      columnClass: "medium",
      content: headerReg.reportConfirm,
      draggable: true,
      dragWindowGap: 100,
      backgroundDismiss: true,
      buttons: {
        close: {
          text: headerReg.btnCancelar,
          btnClass: "btn btn-outline blue dialog",
          action: function () {}, //GENERIC CLOSE.
        },
        Ok: {
          text: headerReg.btnEliminar,
          btnClass: "btn btn-primary",
          action: function () {
            $.ajax({
              headers: csrf,
              url: "/controlpanel/reports/delete/" + id,
              type: "DELETE",
            })
              .done(function (result) {
                location.reload();
              })
              .fail(function (error) {
                $.alert({
                  title: "ERROR!",
                  type: "red",
                  theme: "light",
                  content: "Could not delete report" + error.responseText,
                });
              })
              .always(function () {});
          },
        },
      },
    });
  };

  return {
    init: init,
    runReportWithParameters: runReportWithParameters,
    getParameters: getParameters,
  };
})();

$(document).ready(function () {
  Report.List.init();
});
