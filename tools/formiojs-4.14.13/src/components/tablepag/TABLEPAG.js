import Component from '../_classes/component/Component';
import _ from 'lodash';

export default class TablePagComponent extends Component {
  static schema(...extend) {
    return Component.schema({
      label: 'TablePag',
      type: 'tablepag',
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
      title: 'Table Pag',
      group: 'data',
      icon: 'code',
      weight: 0,
      documentation: '/userguide/forms/layout-components#html-element',
      schema: TablePagComponent.schema()
    };
  }

  get defaultSchema() {
    return TablePagComponent.schema();
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
    that.pagSelected = 1 ;
    var base = window.showformbase;
      window.from(this.component.datasource).exec().then(function( data ) {
        that.datasourceData = data;
        that.renderData(that,base,false);
      });
  }

  renderData(that,base,isSearch) {
    var inputSearch = document.getElementById(`inputsearch${this.id}`).value;
    var data;
    if (inputSearch && inputSearch.trim().length>0) {
       data = that.datasourceData.filter(element => JSON.stringify(element).indexOf(inputSearch) >= 0 );
    }
    else {
       data = that.datasourceData;
    }
    if (isSearch) {
      that.pagSelected = 1 ;
    }
    var ths = '' ;
    for (let i=0;i<that.component.attrs.length;i++) {
      ths= `${ths}<th data-field="${that.component.attrs[i].attr}">${that.component.attrs[i].attr}</th>`;
    }
    ths= `<thead><tr>${ths}<th data-field="options">Options</th></tr></thead>`;
    var trdata = '';
    const start = (that.pagSelected-1)*10;
    const end = that.pagSelected*10>=data.length?data.length:that.pagSelected*10;
    for (let i=start;i < end;i++) {
      let tddata = '';
      let tdoptions = '';

      for (let j=0;j<that.component.attrs.length;j++) {
        tddata = `${tddata}<td>${that.getDataFromPath(data[i],that.component.attrs[j].value)}</td>`;
      }
      if (that.component.options) {
        for (let j=0;j<that.component.options.length;j++) {
          const icon = that.component.options[j].icon.trim().length>0? that.component.options[j].icon : 'edit';
          const color = that.component.options[j].iconcolor.trim().length>0? that.component.options[j].iconcolor : '#1168A6';
          if (that.component.options[j].path && that.component.options[j].path.trim().length>0) {
            const path = this.component.options[j].path.includes('http')?this.component.options[j].path:window.appbase + this.component.options[j].path;
            tdoptions = `${tdoptions}<a style="color: ${color};font-size: medium;" href="${path}"><i style="padding-left: 10px; padding-right: 10px;" class="op-icon-${icon}"></i></a>`;
          }
          else if (that.component.options[j].isdelete && that.component.options[j].idPathButton && that.component.options[j].idPathButton.trim().length>0 ) {
            tdoptions = `${tdoptions}<button style="color: ${color};font-size: medium;border:none;background:none;" onclick="deleteRecordFromForm('${that.getDataFromPath(data[i],that.component.options[j].idPathButton)}');"><i style="padding-left: 10px; padding-right: 10px;" class="op-icon-${icon}"></i></button>`;
          }
          else if (that.component.options[j].idPathButton && that.component.options[j].idPathButton.trim().length>0 && that.component.options[j].formCodeButton && that.component.options[j].formCodeButton.trim().length>0) {
            var url = `${base}/${that.component.options[j].formCodeButton}`;
            var redi = `${url}/${that.getDataFromPath(data[i],that.component.options[j].idPathButton)}`;
            tdoptions = `${tdoptions}<a style="color: ${color};font-size: medium;" href="${redi}"><i style="padding-left: 10px; padding-right: 10px;" class="op-icon-${icon}"></i></a>`;
          }
          else if (that.component.options[j].onclick && that.component.options[j].onclick.trim().length>0) {
            tdoptions = `${tdoptions}<button style="color: ${color};font-size: medium;border:none;background:none;" onclick="${that.component.options[j].onclick}"><i style="padding-left: 10px; padding-right: 10px;" class="op-icon-${icon}"></i></button>`;
          }
          else {
            tdoptions = `${tdoptions}<button style="color: ${color};font-size: medium;border:none;background:none;" ><i style="padding-left: 10px; padding-right: 10px;" class="op-icon-${icon}"></i></button>`;
          }
        }
      }
      trdata = `${trdata}<tr>${tddata}<td>${tdoptions}</td></tr>`;
    }
    let pagination = '';
    const endlimit = Math.ceil(data.length/10);
    const tpag = ` <span style="margin: 15px;font-size: 0.875rem;"> ${that.pagSelected} / ${endlimit} pag. </span>`;
    let finish = endlimit;
    const interval = Math.floor(that.pagSelected/10);
    const init = interval*10;
    if ((interval*10)+10 < endlimit) {
      finish = (interval*10)+10;
    }
    for (let k=init;k < finish;k++) {
        pagination=`${pagination}<li class="page-item"><a class="page-link" style="${k===that.pagSelected-1?'color:#007bff':'color:#343a40'}" onclick='window.listThat.selectPag(${k+1})'>${k+1}</a></li>`;
    }
    pagination=`<li class="page-item"><a class="page-link" style="color:#007bff;    padding: 3px 0px 2px 0px;" onclick='window.listThat.selectPag(${window.listThat.pagSelected-1})'><i  class="op-icon-chevron-left-small" ></i></a></li>${pagination}<li class="page-item"><a class="page-link" style="color:#007bff;padding: 3px 0px 2px 0px;" onclick='window.listThat.selectPag(${window.listThat.pagSelected+1})'><i  class="op-icon-chevron-right-small"></i></a></li>`;
    var result = `<table class="table tablepag">${ths}<tbody>${trdata}</tbody></table><div style="display:flex;"><div style="display: inherit;margin-left:auto;">${tpag}<nav aria-label="Page navigation"  style="margin-top:10px;">  <ul class="pagination pagination-sm">${pagination}</ul></nav></div></div>`;
    document.getElementById(`listpag${that.id}`).innerHTML=result;
    that.parent.refresh();
  }

  selectPag(selection) {
    window.listThat.pagSelected = selection;
    if (selection <1) {
      window.listThat.pagSelected = 1;
    }
    else {
      const endlimit = Math.ceil(window.listThat.datasourceData.length/10) ;
      if (selection > endlimit) {
        window.listThat.pagSelected = endlimit;
      }
    }
    window.listThat.renderData(window.listThat,window.showformbase,false);
  }

  renderContent() {
    const submission = _.get(this.root, 'submission', {});
    window.listThat = this;
    this.component.content =`
    <div  class="input-group mb-3" style="margin-left: 4px;">
    <div  style="background-color: white; padding-left: 4px; margin-top: 4px; margin-left: 0px; position: absolute; z-index: 10;">
       <i class="op-icon-search  bg-white"></i>
    </div>
      <input oninput="window.listThat.renderData( window.listThat,window.showformbase,true)" id="inputsearch${this.id}" type="search" class="form-control input-sm input-inline" placeholder="" aria-controls="formsTable" style="max-width:260px;padding-left: 27px">
    </div>
    <div id="listpag${this.id}"></div>`;
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
