import Component from '../_classes/component/Component';
import _ from 'lodash';

export default class HTMLTableComponent extends Component {
  static schema(...extend) {
    return Component.schema({
      label: 'List',
      type: 'htmltable',
      tag: 'div',
      attrs: [],
      content: '<div></div>',
      datasource: '',
      idPath: '',
      formCode:'',
      input: false,
      persistent: false
    }, ...extend);
  }

  static get builderInfo() {
    return {
      title: 'List',
      group: 'data',
      icon: 'code',
      weight: 0,
      documentation: '/userguide/forms/layout-components#html-element',
      schema: HTMLTableComponent.schema()
    };
  }

  get defaultSchema() {
    return HTMLTableComponent.schema();
  }

  get content() {
    if (this.builderMode) {
      return this.component.content;
    }

    // i18n returns error exactly with word 'select', spaces will be trimmed
    if (this.component.content.replace(/(<(\/?[^>]+)>)/g, '').trim() === 'select') {
      return ` ${this.component.content} `;
    }

    const submission = _.get(this.root, 'submission', {});
    const content = this.component.content ? this.interpolate(this.component.content, {
      metadata: submission.metadata || {},
      submission: submission,
      data: this.rootValue,
      row: this.data
    }) : '';
    return this.sanitize(content, this.shouldSanitizeValue);
  }

  get singleTags() {
    return ['br', 'img', 'hr'];
  }

  checkRefreshOn(changed) {
    super.checkRefreshOn(changed);
    if (!this.builderMode && this.component.refreshOnChange && this.element &&
      !_.isUndefined(changed) && ((_.isBoolean(changed) && changed) || !_.isEmpty(changed)) &&
      this.conditionallyVisible(this.data, this.row)) {
      this.setContent(this.element, this.renderContent());
    }
  }

  getDataFromPath(data,path) {
    if (!data || !path || path.length === 0) {
      return null;
    }
    const p = path.split('.');
    let elem = JSON.parse(JSON.stringify(data));
    for ( var i=0; i<p.length;i++ ) {
      elem = elem[p[i]];
    }
    return elem;
  }
  renderContentDatasource() {
    var that = this;
    var base = window.showformbase;
      window.from(this.component.datasource).exec().then(function( data ) {
       var ths = '' ;
       var url = `${base}/${that.component.formCode}`;
       for (let i=0;i<that.component.attrs.length;i++) {
         ths= `${ths}<th>${that.component.attrs[i].attr}</th>`;
       }
       ths= `${ths}<th>Options</th>`;
       var trdata = '';
       for (let i=0;i < data.length;i++) {
         var tddata = '';
         for (let j=0;j<that.component.attrs.length;j++) {
           tddata = `${tddata}<td>${that.getDataFromPath(data[i],that.component.attrs[j].value)}</td>`;
         }
         var redi = `${url}/${that.getDataFromPath(data[i],that.component.idPath)}`;
         trdata = `${trdata}<tr>${tddata}<td><a class="btn btn-primary" href="${redi}">Show</a></td></tr>`;
       }
       that.component.content = `<table class="table"><tr>${ths}</tr>${trdata}</table>`;
       //that.setContent(that.element, that.renderContent());
       that.parent.refresh();
      });
  }

  renderContent() {
    const submission = _.get(this.root, 'submission', {});
      return this.renderTemplate('html', {
        component: this.component,
        tag: this.component.tag,
        attrs: (this.component.attrs || []).map((attr) => {
          return {
            attr: attr.attr,
            value: this.interpolate(attr.value, {
              metadata: submission.metadata || {},
              submission: submission,
              data: this.rootValue,
              row: this.data
            })
          };
        }),
        content: this.content,
        singleTags: this.singleTags,
      });
  }

  render() {
      this.renderContentDatasource();
      return super.render(this.renderContent());
  }

  attach(element) {
    this.loadRefs(element, { html: 'single' });
    return super.attach(element);
  }
}
