import BuilderUtils from '../../../utils/builder';
import _ from 'lodash';

export default [
  {
    key: 'labelPosition',
    ignore: true,
  },
  {
    key: 'placeholder',
    ignore: true,
  },
  {
    key: 'hideLabel',
    ignore: true,
  },
  {
    type: 'select',
    key: 'action',
    label: 'Action',
    input: true,
    dataSrc: 'values',
    weight: 110,
    tooltip: 'This is the action to be performed by this button.',
    data: {
      values: [
        { label: 'Submit', value: 'submit' },
        { label: 'Only Redirect no Submit', value: 'redirect' },
        { label: 'Save in state', value: 'saveState' },
        { label: 'Event', value: 'event' },
        { label: 'Custom', value: 'custom' },
        { label: 'Reset', value: 'reset' },
        { label: 'OAuth', value: 'oauth' },
        { label: 'POST to URL', value: 'url' },
      ],
    },
  },
  {
    type: 'textfield',
    key: 'formCode',
    label: 'Form Code',
    input: true,
    placeholder: 'Enter form code',
    tooltip: 'Enter the code of the form to which you will be redirected after submission',
    weight: 160,
  },
  {
    type: 'textfield',
    key: 'formOid',
    label: 'Form Oid',
    input: true,
    placeholder: 'Enter form oid like {{row.property name}}',
    tooltip: 'Identifier of the record with which the form will be loaded, you can use a property name from another form field',
    weight: 160,
  },
  {
    type: 'textfield',
    key: 'redirect',
    label: 'Redirect',
    input: true,
    placeholder: 'Enter path to redirect',
    tooltip: 'Redirect the browser to a new page you can write host + path like https://onesaitplatform.com/path or only path like /path',
    weight: 160,
  },
  {
    type: 'select',
    key: 'oauthProvider',
    label: 'OAuth Provider',
    input: true,
    dataSrc: 'values',
    weight: 111,
    tooltip: 'The oauth provider to use to log in (8.x server only).',
    data: {
      values: [
        { label: 'OpenID', value: 'openid' },
        { label: 'Github', value: 'github' },
        { label: 'Google', value: 'google' },
      ],
    },
    conditional: {
      json: { '===': [{ var: 'data.action' }, 'oauth'] },
    },
  },
  {
    type: 'textfield',
    label: 'Save in state',
    key: 'state',
    weight: 112,
    tooltip: 'The state you wish to save the submission under when this button is pressed. Example "draft" would save the submission in Draft Mode.',
    placeholder: 'submitted',
    input: true,
    conditional: {
      json: { '===': [{ var: 'data.action' }, 'saveState'] },
    },
  },
  {
    type: 'checkbox',
    input: true,
    inputType: 'checkbox',
    key: 'saveOnEnter',
    label: 'Save On Enter',
    weight: 113,
    tooltip: 'Use the Enter key to submit form.',
    conditional: {
      json: { '===': [{ var: 'data.action' }, 'submit'] },
    },
  },
  {
    type: 'checkbox',
    input: true,
    inputType: 'checkbox',
    key: 'showValidations',
    label: 'Show Validations',
    weight: 115,
    tooltip: 'When the button is pressed, show any validation errors on the form.',
    conditional: {
      json: { '!==': [{ var: 'data.action' }, 'submit'] },
    },
  },
  {
    type: 'textfield',
    label: 'Button Event',
    key: 'event',
    input: true,
    weight: 120,
    tooltip: 'The event to fire when the button is clicked.',
    conditional: {
      json: { '===': [{ var: 'data.action' }, 'event'] },
    },
  },
  {
    type: 'textfield',
    inputType: 'url',
    key: 'url',
    input: true,
    weight: 120,
    label: 'Button URL',
    tooltip: 'The URL where the submission will be sent.',
    placeholder: 'https://example.form.io',
    conditional: {
      json: { '===': [{ var: 'data.action' }, 'url'] },
    },
  },
  {
    type: 'datagrid',
    key: 'headers',
    input: true,
    weight: 130,
    label: 'Headers',
    addAnother: 'Add Header',
    tooltip: 'Headers Properties and Values for your request',
    components: [
      {
        key: 'header',
        label: 'Header',
        input: true,
        type: 'textfield',
      },
      {
        key: 'value',
        label: 'Value',
        input: true,
        type: 'textfield',
      }
    ],
    conditional: {
      json: { '===': [{ var: 'data.action' }, 'url'] },
    },
  },
  {
    type: 'textarea',
    key: 'custom',
    label: 'Button Custom Logic',
    tooltip: 'The custom logic to evaluate when the button is clicked.',
    rows: 5,
    editor: 'ace',
    input: true,
    weight: 120,
    placeholder: "data['mykey'] = data['anotherKey'];",
    conditional: {
      json: { '===': [{ var: 'data.action' }, 'custom'] },
    },
  },
  {
    type: 'select',
    key: 'theme',
    label: 'Theme',
    input: true,
    tooltip: 'The color theme of this button.',
    dataSrc: 'values',
    weight: 140,
    data: {
      values: [
        { label: 'Primary', value: 'primary' },
        { label: 'Secondary', value: 'secondary' },
        { label: 'Info', value: 'info' },
        { label: 'Success', value: 'success' },
        { label: 'Danger', value: 'danger' },
        { label: 'Warning', value: 'warning' },
      ],
    },
  },
  /* {
    type: 'select',
    key: 'size',
    label: 'Size',
    input: true,
    tooltip: 'The size of this button.',
    dataSrc: 'values',
    weight: 150,
    data: {
      values: [
        { label: 'Small', value: 'sm' },
        { label: 'Medium', value: 'md' },
        { label: 'Large', value: 'lg' },
      ],
    },
  },
  {
    type: 'select',
    key: 'float',
    label: 'Float button',
    input: true,
    tooltip: 'These option float this element to the left or right',
    dataSrc: 'values',
    weight: 149,
    data: {
      values: [
        { label: 'none', value: '  ' },
        { label: 'left', value: 'float-start' },
        { label: 'right', value: 'float-end' },
      ],
    },
  },*/
  {
    type: 'textfield',
    key: 'leftIcon',
    label: 'Left Icon',
    input: true,
    placeholder: 'Enter icon classes',
    tooltip: "This is the full icon class string to show the icon. Example: 'fa fa-plus or op-icon-plus'",
    weight: 160,
  },
  {
    type: 'textfield',
    key: 'rightIcon',
    label: 'Right Icon',
    input: true,
    placeholder: 'Enter icon classes',
    tooltip: "This is the full icon class string to show the icon. Example: 'fa fa-plus or op-icon-plus'",
    weight: 170,
  },
  {
    type: 'select',
    input: true,
    weight: 180,
    label: 'Shortcut',
    key: 'shortcut',
    tooltip: 'Shortcut for this component.',
    dataSrc: 'custom',
    valueProperty: 'value',
    customDefaultValue: () => '',
    template: '{{ item.label }}',
    data: {
      custom(context) {
        return BuilderUtils.getAvailableShortcuts(
          _.get(context, 'instance.options.editForm', {}),
          _.get(context, 'instance.options.editComponent', {})
        );
      },
    },
  },
  {
    type: 'checkbox',
    key: 'block',
    label: 'Block Button',
    input: true,
    weight: 155,
    tooltip: 'This control should span the full width of the bounding container.',
  },
  {
    type: 'checkbox',
    key: 'disableOnInvalid',
    label: 'Disable on Form Invalid',
    tooltip: 'This will disable this field if the form is invalid.',
    input: true,
    weight: 620,
  },
  {
    type: 'checkbox',
    input: true,
    inputType: 'checkbox',
    key: 'filtersubmission',
    label: 'filter fields that are sent?',
    weight: 181,
    tooltip: 'The list will include the fields that will be sent, excluding the rest taking into account their API key.',
    conditional: {
      json: {
        or: [
          {
            '===': [
              { var: 'data.action' },
              'submit'
            ],
          },
          {
            '===': [
              { var: 'data.action' },
              'url'
            ],
          }
        ]
      }
    }
  },
  {
    weight:182,
    label: 'Load Fields',
    action: 'event',
    showValidations: false,
    tableView: false,
    key: 'loadFields',
    type: 'button',
    input: true,
    event: 'updatesubmitlist',
    conditional: {
      eq: 'true',
      when: 'filtersubmission',
      show: 'true'
    }
  },
  {
    type: 'datagrid',
    input: true,
    label: 'Submission field list',
    key: 'submissionfieldslist',
    tooltip: 'The list will include the fields that will be sent, excluding the rest taking into account their API key.',
    weight: 183,
    conditional: {
      eq: 'true',
      when: 'filtersubmission',
      show: 'true'
    },
    components: [
      {
        label: 'Field label',
        key: 'label',
        input: true,
        type: 'textfield'
      },
      {
        label: 'Field Api key',
        key: 'key',
        input: true,
        type: 'textfield'
      }
    ],
    logic: [
      {
        name: 'loadsubmissionfieldslist',
        trigger: {
          type: 'event',
          event: 'updatesubmitlist'
        },
        actions: [
          {
            name: 'loadsubmissionfieldslist',
            type: 'value',
            value: 'value = (window.componentsPaths?window.componentsPaths.map((elem)=>({label:elem.label, key:elem.value})):[]);'
          }
        ]
      }
    ],
  },
];
