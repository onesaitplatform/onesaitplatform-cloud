export default [
  {
    key: 'labelPosition',
    ignore: true
  },
  {
    key: 'placeholder',
    ignore: true
  },
  {
    key: 'description',
    ignore: true
  },
  {
    key: 'tooltip',
    ignore: true
  },
  {
    key: 'hideLabel',
    ignore: true
  },
  {
    key: 'autofocus',
    ignore: true
  },
  {
    key: 'disabled',
    ignore: true
  },
  {
    key: 'tabindex',
    ignore: true
  },
  {
    key: 'tableView',
    ignore: true
  },
  {
    type: 'select',
    input: true,
    label: 'data source type',
    tooltip: 'Select whether you want the data source to be an entity or a datasource',
    key: 'typedatasource',
    defaultValue: 'datasource',
    weight: 10,
    dataSrc: 'values',
    data: {
      values: [
        { label: 'Datasource', value: 'datasource' },
        { label: 'Entity', value: 'entity' }
      ]
    }
  },{
    type: 'select',
    input: true,
    dataSrc: 'url',
    data: {
      url: '../api/gadgetdatasources'
    },
    searchEnabled: false,
    widget: 'choicesjs',
    disableLimit: true,
    template: '<span>{{ item.identification }}</span>',
    valueProperty: 'identification',
    label: 'Data is loaded from the Datasource',
    key: 'dsDatasource',
    weight: 11,
    lazyLoad: false,
    refreshOn: 'typedatasource',
    conditional: {
      json: { '===': [{ var: 'data.typedatasource' }, 'datasource'] },
    },
  },
  {
    type: 'select',
    input: true,
    dataSrc: 'url',
    data: {
      url: '../api/ontologies'
    },
    searchEnabled: false,
    widget: 'choicesjs',
    disableLimit: true,
    template: '<span>{{ item.identification }}</span>',
    valueProperty: 'identification',
    label: 'Data is loaded from the Entity',
    key: 'dsEntity',
    weight: 11,
    lazyLoad: false,
    refreshOn: 'typedatasource',
    conditional: {
      json: { '===': [{ var: 'data.typedatasource' }, 'entity'] },
    },
  },
  {
    type: 'editgrid',
    input: true,
    key: 'filters',
    weight: 71,
    label: 'Filters',
    tooltip: 'Select the field that will be filtered, the operator and the value, this can be a constant or the value of an element of the form or the context, indicating it as {{data.apikey}} where apikey is the api key of another element of the form',
    description: 'You You can filter the table data by adding new filters herecan specify few buttons which will be shown',
    templates: {
      header: '<div class="row">\n  <div class="col-sm-6">Filter</div><div class="col-sm-6">On Click</div>\n</div>',
      row: '<div class="row">\n  <div class="col-sm-6">{{flattenedComponents.dsParamDatasource.getView(row.dsParamDatasource) }}{{flattenedComponents.dsParamEntity.getView(row.dsParamEntity) }}{{flattenedComponents.filterOperator.getView(row.filterOperator) }}{{flattenedComponents.value.getView(row.value) }}\n </div><div class="col-sm-6">\n {% if (!instance.disabled)    { %}\n <div class="col-sm-3">\n <div class="btn-group pull-right">\n <button class="btn btn-default btn-light btn-sm editRow"><i class="{{ iconClass("edit") }}"></i></button>\n {%            if (!instance.hasRemoveButtons || instance.hasRemoveButtons()) { %}\n <button                class="btn btn-danger btn-sm removeRow"><i class="{{ iconClass("trash") }}"></i></button>\n {% } %}\n        </div>\n </div>\n {% } %}\n </div>'
    },
    components: [
      {
        type: 'select',
        input: true,
        dataSrc: 'url',
        data: {
          url: '../api/gadgetdatasources/getFields/{{data.dsDatasource}}'
        },
        searchEnabled: false,
        template: '<span>{{ item.name }}</span>',
        widget: 'choicesjs',
        disableLimit: true,
        label: 'Parameter',
        key: 'dsParamDatasource',
        refreshOn: 'dsDatasource',
        weight: 11,
        lazyLoad: false,
        conditional: {
          json: { '===': [{ var: 'data.typedatasource' }, 'datasource'] },
        },
      },
      {
        type: 'select',
        input: true,
        dataSrc: 'url',
        data: {
          url: '../api/forms/entityparameters/{{data.dsEntity}}'
        },
        searchEnabled: false,
        template: '<span>{{ item.name }}</span>',
        widget: 'choicesjs',
        disableLimit: true,
        label: 'Parameter',
        key: 'dsParamEntity',
        refreshOn: 'dsEntity',
        weight: 11,
        lazyLoad: false,
        conditional: {
          json: { '===': [{ var: 'data.typedatasource' }, 'entity'] },
        },
      },
      {
        type: 'select',
        input: true,
        label: 'Operator',
        key: 'filterOperator',
        defaultValue: '=',
        dataSrc: 'values',
        data: {
          values: [
            { label: '>', value: '>' },
            { label: '&#60', value: '<' },
            { label: '>=', value: '>=' },
            { label: '&#60=', value: '<=' },
            { label: '=', value: '=' },
            { label: '!=', value: '!=' },
          ]
        }
      },
      {
        label: 'Condition',
        tooltip: 'The value can be a constant or the value of a form or context element, indicating it as {{data.apikey}} where apikey is the api key of another form element',
        key: 'value',
        input: true,
        type: 'textfield'
      }
    ],
    defaultValue: []
  },{
    weight: 69,
    label: 'Load Fields',
    action: 'event',
    showValidations: false,
    tableView: false,
    key: 'loadFields',
    type: 'button',
    input: true,
    event: 'updateattributestablepag'
  },
  {
    type: 'datagrid',
    input: true,
    label: 'Fields',
    reorder:'true',
    key: 'attrs',
    tooltip: 'Enter the label and path of each field that corresponds to a column of the table obtained from datasource',
    weight: 70,
    components: [
      {
        label: 'Label',
        key: 'attr',
        input: true,
        type: 'textfield'
      },
      {
        label: 'Path',
        key: 'value',
        input: true,
        type: 'textfield'
      }
    ],
    logic: [
      {
        name: 'loadFieldsTablePag',
        trigger: {
          type: 'event',
          event: 'updateattributestablepag'
        },
        actions: [
          {
            name: 'loadFieldsTablePag',
            type: 'value',
            value: 'const request = new XMLHttpRequest(); request.open("GET", data.typedatasource==="datasource"?"../api/gadgetdatasources/getFields/"+data.dsDatasource: "../api/forms/entityparameters/"+data.dsEntity, false);  request.send(null); if (request.status === 200) {  let resu = JSON.parse(request.responseText); value=[]; if(resu.length && resu.length>0){  for(var i = 0;i<resu.length;i++){value.push({attr:resu[i].name,value:resu[i].name})}};  } else {value=[];};'
          }
        ]
      }
    ],
  },
  {
    type: 'editgrid',
    input: true,
    key: 'options',
    weight: 72,
    label: 'Options',
    description: 'You can specify few buttons which will be shown',
    templates: {
      header: '<div class="row">\n  <div class="col-sm-3">Icon</div>\n<div class="col-sm-3">Icon color</div>\n <div class="col-sm-3">Path</div>\n <div class="col-sm-3">FormCode</div>\n   <div class="col-sm-3">Path ID</div>\n<div class="col-sm-3">ID Path</div>\n<div class="col-sm-3">Is for delete</div>\n<div class="col-sm-6">On Click</div>\n</div>',
      row: '<div class="row">\n <div class="col-sm-3">\n {{ flattenedComponents.icon.getView(row.icon) }}\n </div>\n <div class="col-sm-3">\n {{ flattenedComponents.iconcolor.getView(row.iconcolor) }}\n </div>\n <div class="col-sm-3">\n {{ flattenedComponents.path.getView(row.path) }}\n </div>\n <div class="col-sm-3">\n {{ flattenedComponents.formCodeButton.getView(row.formCodeButton) }}\n </div>\n <div class="col-sm-3">\n {{ flattenedComponents.idPathButton.getView(row.idPathButton) }}\n </div>\n  <div class="col-sm-3">\n {{ flattenedComponents.isdelete.getView(row.isdelete) }}\n<div class="col-sm-6">\n {{ flattenedComponents.onclick.getView(row.onclick) }}\n </div>\n {% if (!instance.disabled)    { %}\n <div class="col-sm-3">\n <div class="btn-group pull-right">\n <button class="btn btn-default btn-light btn-sm editRow"><i class="{{ iconClass("edit") }}"></i></button>\n {%            if (!instance.hasRemoveButtons || instance.hasRemoveButtons()) { %}\n <button                class="btn btn-danger btn-sm removeRow"><i class="{{ iconClass("trash") }}"></i></button>\n {% } %}\n        </div>\n </div>\n {% } %}\n </div>'
    },
    components: [
      {
        label: 'Icon',
        tooltip: 'Enter icons such as pencil, delete, eye...',
        key: 'icon',
        input: true,
        type: 'textfield'
      },{
        label: 'Icon color',
        key: 'iconcolor',
        input: true,
        type: 'textfield'
      },
      {
        label: 'Path',
        key: 'path',
        tooltip: 'https://... type url that will be accessed by clicking on this element',
        input: true,
        type: 'textfield'
      },
      {
        label: 'FormCode',
        key: 'formCodeButton',
        tooltip: 'Enter the code of the form to which you will be redirected after after executing the action.',
        input: true,
        type: 'textfield'
      },
      {
        type: 'textfield',
        input: true,
        key: 'idPathButton',
        weight: 14,
        label: 'Path ID',
        placeholder: 'id',
        tooltip: 'Path to the data id.'
      },
      {
        type: 'checkbox',
        weight: 15,
        label: 'It is a delete Button',
        tooltip: 'If it is checked, you must have selected an entity for the form, and indicate the path id of the entity in this button',
        key: 'isdelete',
        input: true,
        conditional: {
          eq: 'true',
          when: 'isclone',
          show: 'false'
        }
      },
      {
        type: 'textfield',
        input: true,
        key: 'deleteMessage',
        weight: 18,
        label: 'Delete Message',
        placeholder: 'Message',
        tooltip: 'Delete message',
        conditional: {
          eq: 'true',
          when: 'isdelete',
          show: 'true'
        }
      },
      {
        type: 'checkbox',
        weight: 16,
        label: 'It is a clone Button',
        tooltip: 'If it is checked, you must have selected an entity for the form, and indicate the path id of the entity in this button, and the attribute of the entity which is the identifier which will be requested to change it',
        key: 'isclone',
        input: true,
        conditional: {
          eq: 'true',
          when: 'isdelete',
          show: 'false'
        }
      },
      {
        type: 'textfield',
        input: true,
        key: 'cloneIdentification',
        weight: 17,
        label: 'Entity identifier attribute',
        placeholder: 'cloneIdentification',
        tooltip: 'Attribute of the entity that is the identifier which will be requested to change it',
        conditional: {
          eq: 'true',
          when: 'isclone',
          show: 'true'
        }
      },
      {
        type: 'textfield',
        input: true,
        key: 'cloneMessage',
        weight: 18,
        label: 'Clone Message',
        placeholder: 'Message',
        tooltip: 'message that will be displayed when requesting the entity identifier when cloning the element of this',
        conditional: {
          eq: 'true',
          when: 'isclone',
          show: 'true'
        }
      },
      {
        type: 'textarea',
        editor: 'ace',
        rows: 4,
        as: 'html',
        label: 'On Click',
        tooltip: 'In this section you can enter javascript code that will be executed when you press the button',
        defaultValue: '',
        key: 'onclick',
        weight: 30,
        input: true
      },
      {
        type: 'panel',
        title: 'Disable / Hide',
        key: 'disablebutton',
        theme: 'default',
        components: [
          {
            type: 'select',
            input: true,
            label: 'hide or disable the component according to some field in the row:',
            key: 'disablebutton.option',
            dataSrc: 'values',
            data: {
              values: [
                { label: 'No', value: 'no' },
                { label: 'Disable', value: 'disable' },
                { label: 'Hide', value: 'hide' },
              ]
            }
          },
          {
            type: 'select',
            input: true,
            label: 'This button should Display:',
            key: 'disablebutton.show',
            dataSrc: 'values',
            data: {
              values: [
                { label: 'True', value: 'true' },
                { label: 'False', value: 'false' }
              ]
            }
          },
          {
            type: 'textfield',
            input: true,
            label: 'When the row parameter:',
            key: 'disablebutton.when',
          },
          {
            type: 'textfield',
            input: true,
            label: 'Has the value:',
            key: 'disablebutton.eq'
          }
        ]
      },
    ],
    defaultValue: []
  },
  {
    weight: 120,
    type: 'checkbox',
    label: 'Refresh On Change',
    tooltip: 'Rerender the field whenever a value on the form changes.',
    key: 'refreshOnChange',
    input: true
  },
  {
    weight: 121,
    type: 'checkbox',
    label: 'Hide Search',
    tooltip: 'Marked hides the table lookup component.',
    key: 'hideSearch',
    input: true
  },
  {
    label: 'Elements per page',
    mask: false,
    tableView: false,
    weight: 119,
    delimiter: false,
    requireDecimal: false,
    defaultValue: 10,
    inputFormat: 'plain',
    truncateMultipleSpaces: false,
    key: 'elementsPerPage',
    type: 'number',
    input: true,
    decimalLimit: 0,
    validate: {
      min: 1
    }
  }
];
