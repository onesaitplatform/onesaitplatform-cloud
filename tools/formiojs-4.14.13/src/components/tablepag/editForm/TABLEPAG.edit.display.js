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
    type: 'textfield',
    input: true,
    key: 'datasource',
    weight: 12,
    label: 'Datasource',
    placeholder: 'Datasource name',
    tooltip: 'write the name of the datasource',
  },
  {
    type: 'datagrid',
    input: true,
    label: 'Attributes',
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
    ]
  },
  {
    type: 'editgrid',
    input: true,
    key: 'options',
    label: 'Options',
    description: 'You can specify few buttons which will be shown',
    templates: {
      header: '<div class="row">\n  <div class="col-sm-3">Icon</div>\n<div class="col-sm-3">Icon color</div>\n <div class="col-sm-3">Path</div>\n <div class="col-sm-3">FormCode</div>\n   <div class="col-sm-3">Path ID</div>\n<div class="col-sm-3">ID Path</div>\n<div class="col-sm-3">Is for delete</div>\n<div class="col-sm-6">On Click</div>\n</div>',
      row: '<div class="row">\n <div class="col-sm-3">\n {{ flattenedComponents.icon.getView(row.icon) }}\n </div>\n <div class="col-sm-3">\n {{ flattenedComponents.iconcolor.getView(row.iconcolor) }}\n </div>\n <div class="col-sm-3">\n {{ flattenedComponents.path.getView(row.path) }}\n </div>\n <div class="col-sm-3">\n {{ flattenedComponents.formCodeButton.getView(row.formCodeButton) }}\n </div>\n <div class="col-sm-3">\n {{ flattenedComponents.idPathButton.getView(row.idPathButton) }}\n </div>\n  <div class="col-sm-3">\n {{ flattenedComponents.isdelete.getView(row.isdelete) }}\n<div class="col-sm-6">\n {{ flattenedComponents.onclick.getView(row.onclick) }}\n </div>\n {% if (!instance.disabled)    { %}\n <div class="col-sm-3">\n <div class="btn-group pull-right">\n <button class="btn btn-default btn-light btn-sm editRow"><i class="{{ iconClass("edit") }}"></i></button>\n {%            if (!instance.hasRemoveButtons || instance.hasRemoveButtons()) { %}\n <button                class="btn btn-danger btn-sm removeRow"><i class="{{ iconClass("trash") }}"></i></button>\n {% } %}\n        </div>\n </div>\n {% } %}\n </div>'
    },
    components: [
      {
        label: 'Icon',
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
        input: true,
        type: 'textfield'
      },
      {
        label: 'FormCode',
        key: 'formCodeButton',
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
        label: 'It is a delete Button',
        tooltip: 'If it is checked, you must have selected an entity for the form, and indicate the path id of the entity in this button',
        key: 'isdelete',
        input: true
      },
      {
        type: 'textarea',
        editor: 'ace',
        rows: 4,
        as: 'html',
        label: 'On Click',
        tooltip: '',
        defaultValue: '',
        key: 'onclick',
        weight: 30,
        input: true
      }
    ],
    defaultValue: []
  },
  {
    weight: 85,
    type: 'checkbox',
    label: 'Refresh On Change',
    tooltip: 'Rerender the field whenever a value on the form changes.',
    key: 'refreshOnChange',
    input: true
  },
];
