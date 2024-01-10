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
    type: 'textfield',
    input: true,
    key: 'idPath',
    weight: 14,
    label: 'ID Path',
    placeholder: 'id',
    tooltip: 'Path to the data id.'
  },
  {
    type: 'textfield',
    key: 'formCode',
    label: 'Form Code',
    input: true,
    placeholder: 'Enter form code',
    tooltip: 'Enter the code of the form to which you will be redirected with show buttons',
    weight: 13,
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
    weight: 85,
    type: 'checkbox',
    label: 'Refresh On Change',
    tooltip: 'Rerender the field whenever a value on the form changes.',
    key: 'refreshOnChange',
    input: true
  },
];
