import Component from '../component/Component';

export default class Field extends Component {
  render(element) {
    if (this.noField) {
      return super.render(element);
    }
    else if (this.isAdvancedLabel || this.options.condensedMode) {
      return super.render(this.renderTemplate('field', {
        ...this.getLabelInfo(this.options.condensedMode),
        labelMarkup: this.renderTemplate('label'),
        element: element
      }, 'align'));
    }
    else {
      return super.render(this.renderTemplate('field', {
        labelMarkup: this.renderTemplate('label'),
        element: element,
      }));
    }
  }
}
