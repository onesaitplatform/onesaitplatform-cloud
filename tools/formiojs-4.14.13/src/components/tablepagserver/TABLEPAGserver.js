import Component from '../_classes/component/Component';
import _ from 'lodash';

export default class TablePagServerComponent extends Component {
  static schema(...extend) {
    return Component.schema({
      label: 'TablePagServer',
      type: 'tablepagserver',
      tag: 'div',
      attrs: [],
      content: '<div></div>',
      datasource: '',
      idPath: '',
      formCode: '',
      input: false,
      persistent: false
    }, ...extend);
  }

  static get builderInfo() {
    return {
      title: 'Paginated Table from Server',
      group: 'data',
      icon: 'list-alt',
      weight: 1,
      documentation: '/userguide/forms/layout-components#html-element',
      schema: TablePagServerComponent.schema()
    };
  }

  get defaultSchema() {
    return TablePagServerComponent.schema();
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
      // this.setContent(this.element, this.renderContent());
      this.renderContentDatasource();
    }
  }

  getDataFromPath(data, path) {
    if (!data || !path || path.length === 0) {
      return null;
    }
    const p = path.split('.');
    let elem = JSON.parse(JSON.stringify(data));
    for (var i = 0; i < p.length; i++) {
      elem = elem[p[i]];
    }
    return elem;
  }

  renderContentDatasource() {
    var that = this;
    var base = window.showformbase;
    var filters = [];
    const numbersTypes = ['number','long','integer'];
    // select datasource
    const datasource = this.component.typedatasource === 'datasource' ? this.component.dsDatasource : this.component.dsEntity;
    const submission = _.get(this.root, 'submission', {});
    // create filters
    if (this.component.filters) {
      for (var i = 0; i < this.component.filters.length; i++) {
        let field = null;
        let value = this.component.filters[i].value ? this.interpolate(this.component.filters[i].value, {
          metadata: submission.metadata || {},
          submission: submission,
          data: this.rootValue,
          row: this.data
        }) : '';
        if (this.component.typedatasource === 'datasource') {
          field = this.component.filters[i].dsParamDatasource.name;
          // cast for field type like add double quotes
          if (this.component.filters[i].dsParamDatasource.type && this.component.filters[i].dsParamDatasource.type.toLowerCase()==='string') {
            value = `'${value}'`;
          }
          else if (this.component.filters[i].dsParamDatasource.type && numbersTypes.includes(this.component.filters[i].dsParamDatasource.type.toLowerCase())) {
            value = Number(value);
          }
        }
        else {
          field = this.component.filters[i].dsParamEntity.name;
          if (this.component.filters[i].dsParamEntity.type && this.component.filters[i].dsParamEntity.type.toLowerCase()==='string') {
            value = `'${value}'`;
          }
          else if (this.component.filters[i].dsParamEntity.type && numbersTypes.includes(this.component.filters[i].dsParamEntity.type.toLowerCase())) {
            value = Number(value);
          }
        }
        const filter = { 'field': field, 'op': this.component.filters[i].filterOperator, 'exp': value };
        filters.push(filter);
      }
    }
    window.from(datasource, this.component.typedatasource).where(filters).skip( that.pagSelected * that.component.elementsPerPage ).limit(that.component.elementsPerPage).exec().then(function(data) {
      that.datasourceData = data;
      that.renderData(that, base);
    });
  }
  emitRedirect(formcod, oid) {
    var message = { formcode: formcod, dataoid: oid };
    this.emit('redirect', message);
  }

  renderData(that, base) {
    var inputSearch = document.getElementById(`inputsearch${this.id}`).value;
    var data;
    if (inputSearch && inputSearch.trim().length > 0) {
      data = that.datasourceData.filter(element => JSON.stringify(element).indexOf(inputSearch) >= 0);
    }
    else {
      data = that.datasourceData;
    }
    that.dataforPagination = data;
    var ths = '';
    for (let i = 0; i < that.component.attrs.length; i++) {
      ths = `${ths}<th data-field="${that.component.attrs[i].attr}">${that.t(that.component.attrs[i].attr)}</th>`;
    }
    ths = `<thead><tr>${ths}<th data-field="options">${that.t('Options')}</th></tr></thead>`;
    var trdata = '';
    that.dataLength = data.length;
    for (let i = 0; i < data.length; i++) {
      let tddata = '';
      let tdoptions = '';

      for (let j = 0; j < that.component.attrs.length; j++) {
        tddata = `${tddata}<td>${that.getDataFromPath(data[i], that.component.attrs[j].value)}</td>`;
      }
      if (that.component.options) {
        for (let j = 0; j < that.component.options.length; j++) {
          const icon = that.component.options[j].icon.trim().length > 0 ? that.component.options[j].icon : 'edit';
          const color = that.component.options[j].iconcolor.trim().length > 0 ? that.component.options[j].iconcolor : '#1168A6';
          var hidedisable = that.showhideicons(that, data, i, j);
          if (that.component.options[j].path && that.component.options[j].path.trim().length > 0) {
            const path = this.component.options[j].path.includes('http') ? this.component.options[j].path : window.appbase + this.component.options[j].path;
            tdoptions = `${tdoptions}<a style="color: ${color};font-size: medium;${hidedisable}" href="${path}"><i style="padding-left: 10px; padding-right: 10px;" class="op-icon-${icon}"></i></a>`;
          }
          else if (that.component.options[j].isdelete && that.component.options[j].idPathButton && that.component.options[j].idPathButton.trim().length > 0) {
            const message = (that.component.options[j].deleteMessage && that.component.options[j].deleteMessage.trim().length > 0) ? that.t(that.component.options[j].deleteMessage) : that.t('Do you want to delete this record?');
            tdoptions = `${tdoptions}<button  style="color: ${color};font-size: medium;border:none;background:none;${hidedisable}" onclick="deleteRecordFromForm('${that.component.options[j].formCodeButton}','${that.getDataFromPath(data[i], that.component.options[j].idPathButton)}','${that.t('Delete')}','${message}');"><i style="padding-left: 10px; padding-right: 10px;" class="op-icon-${icon}"></i></button>`;
          }
          else if (that.component.options[j].isclone && that.component.options[j].cloneIdentification && that.component.options[j].cloneIdentification.trim().length > 0) {
            const message = (that.component.options[j].cloneMessage && that.component.options[j].cloneMessage.trim().length > 0) ? that.t(that.component.options[j].cloneMessage) : that.t('Enter a new value for the parameter');
            const title = that.t('Clone');
            tdoptions = `${tdoptions}<button  style="color: ${color};font-size: medium;border:none;background:none;${hidedisable}" onclick="cloneRecordFromForm('${that.component.options[j].formCodeButton}','${that.getDataFromPath(data[i], that.component.options[j].idPathButton)}','${that.component.options[j].cloneIdentification}','${title}','${message}');"><i style="padding-left: 10px; padding-right: 10px;" class="op-icon-${icon}"></i></button>`;
          }
          else if (that.component.options[j].idPathButton && that.component.options[j].idPathButton.trim().length > 0 && that.component.options[j].formCodeButton && that.component.options[j].formCodeButton.trim().length > 0) {
            var url = `${base}/${that.component.options[j].formCodeButton}`;
            var redi = `${url}/${that.getDataFromPath(data[i], that.component.options[j].idPathButton)}`;
            //redirect by event or by url for href
            if (window.redirectBy && window.redirectBy === 'events') {
              tdoptions = `${tdoptions}<a style="color: ${color};font-size: medium;${hidedisable}" onclick="window.listThat.emitRedirect('${that.component.options[j].formCodeButton}','${that.getDataFromPath(data[i], that.component.options[j].idPathButton)}');"><i style="padding-left: 10px; padding-right: 10px;" class="op-icon-${icon}"></i></a>`;
            }
            else {
              tdoptions = `${tdoptions}<a style="color: ${color};font-size: medium;${hidedisable}" href="${redi}"><i style="padding-left: 10px; padding-right: 10px;" class="op-icon-${icon}"></i></a>`;
            }
          }
          else if (that.component.options[j].onclick && that.component.options[j].onclick.trim().length > 0) {
            tdoptions = `${tdoptions}<button style="color: ${color};font-size: medium;border:none;background:none;${hidedisable}" onclick="${that.component.options[j].onclick}"><i style="padding-left: 10px; padding-right: 10px;" class="op-icon-${icon}"></i></button>`;
          }
          else {
            tdoptions = `${tdoptions}<button style="color: ${color};font-size: medium;border:none;background:none;${hidedisable}" ><i style="padding-left: 10px; padding-right: 10px;" class="op-icon-${icon}"></i></button>`;
          }
        }
      }
      trdata = `${trdata}<tr>${tddata}<td class="table-options">${tdoptions}</td></tr>`;
    }
    let noDataStyle = ' ';
    if (data.length === 0) {
      trdata = '<tr style="height: 50px;"><span style="text-align:center;right:50%;position:absolute;margin-top:58px;z-index:100"> No Data </span></tr>';
      noDataStyle = ' pointer-events: none; opacity: .45; ';
    }
    const tpag = ` <span style="padding: 8px;font-style:italic; font-size: 10px;"> ${that.t('Elements per page') || 'Elements per page'} </span><span  style="padding: 8px;font-size: 11px;border-right:1px solid #d7dadc">${that.component.elementsPerPage}</span>`;
    var result = `<table class="table tablepag">${ths}<tbody>${trdata}</tbody></table><div class="formio-table-footer"  ><div style="display: inherit;">${tpag}<nav aria-label="Page navigation"  style="margin-top:0px;">  <ul class="pagination pagination-sm" style="border-right:1px solid #d7dadc;border-radius: 0;"><li class="page-item">  <a class="page-link" style="color:rgb(255, 255, 255); background-color: rgb(17, 104, 166);border: none; border-top-left-radius: 0;  border-bottom-left-radius: 0;  border-top-right-radius: 0;    border-bottom-right-radius: 0;    padding-top: 8px;    padding-bottom: 8px;font-size:11px;">${window.listThat.pagSelected}</a> </li></ul></nav></div><div style="display: inherit;margin-left:auto;"><button  style="max-height: 32px;border-left:1px solid #d7dadc;border-right:1px solid #d7dadc;${noDataStyle}" class="btn" onclick='window.listThat.selectPag(${window.listThat.pagSelected - 1})'><i  class="op-icon-chevron-left-small" ></i></button></li><button style="max-height: 32px;${noDataStyle}" class="btn" onclick='window.listThat.selectPag(${window.listThat.pagSelected + 1})'><i  class="op-icon-chevron-right-small"></i></button></div></div>`;
    document.getElementById(`listpag${that.id}`).innerHTML = result;
    that.parent.refresh();
  }

  selectPag(selection) {
    window.listThat.pagSelected = selection;
    if (window.listThat.pagSelected  < 1) {
      window.listThat.pagSelected = 1;
    }
    else {
      if (window.listThat.dataLength < window.listThat.component.elementsPerPage) {
        window.listThat.pagSelected = window.listThat.pagSelected-1;
      }
    }
    //window.listThat.renderData(window.listThat, window.showformbase, false);
    window.listThat.renderContentDatasource();
  }

  renderContent() {
    const submission = _.get(this.root, 'submission', {});
    window.listThat = this;
    this.component.content = `
    <div  class="input-group mb-3" style="margin-left: 4px;">
    <!-- <div  style="background-color: white; padding-left: 8px; margin-top: 8px; margin-left: 0px; position: absolute; z-index: 10;">
       <i class="op-icon-search  bg-white"></i>
    </div> -->
      <input oninput="window.listThat.renderData( window.listThat,window.showformbase,true)" id="inputsearch${this.id}" type="search" class="form-control input-sm input-inline" aria-controls="formsTable" placeholder="${this.t('Search') || 'Search'}" style="height: 24px;max-width:260px;${this.component.hideSearch?'display:none;':''}">
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
    this.pagSelected = 1;
    this.renderContentDatasource();
    return super.render(this.renderContent());
  }

  attach(element) {
    this.loadRefs(element, { html: 'single' });
    return super.attach(element);
  }

  showhideicons(that, data, i, j) {
    var hidedisable = '';
    if (that.component.options[j].disablebutton && that.component.options[j].disablebutton.option && that.component.options[j].disablebutton.option !== 'no') {
      if ((String(that.getDataFromPath(data[i], that.component.options[j].disablebutton.when)) === String(that.component.options[j].disablebutton.eq) === that.component.options[j].disablebutton.show === true)) {
        if (that.component.options[j].disablebutton.option === 'disable') {
          hidedisable = ' pointer-events: none; opacity: .45; ';
        }
        else {
          hidedisable = ' display:none; ';
        }
      }
    }
    return hidedisable;
  }
}
