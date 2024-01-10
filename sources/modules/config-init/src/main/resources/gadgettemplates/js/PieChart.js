//Write your Vue JSON controller code here

//Focus here and F11 to full screen editor

//This function will be call once to init components

function findValues(jsonData, path) {
  path = path.replace(/\[(\w+)\]/g, '.$1');
  path = path.replace(/^\./, ''); // strip a leading dot
  var pathArray = path.split('.');
  for (var i = 0, n = pathArray.length; i < n; ++i) {
    var key = pathArray[i];
    if (key in jsonData) {
      if (jsonData[key] !== null) {
        jsonData = jsonData[key];
      } else {
        return null;
      }
    } else {
      return key;
    }
  }
  return jsonData;
}

//This function claculate and map the series
function calculateSerie(data, labeling) {
  var radius = '0%';
  var rose = '';
  if (vm.tparams.parameters.main.type == 'donut') {
    radius = '40%';
  } else if (vm.tparams.parameters.main.type == 'rose') {
    rose = 'radius';
  }

  return {
    name: "Data",
    type: 'pie',
    avoidLabelOverlap: true,
    roseType: rose,
    radius: [radius, vm.tparams.parameters.main.size + '%'],
    center: ["50%", "60%"],
    data: data.map(inst => {
      return {
        name: findValues(inst, vm.tparams.parameters.data.category),
        value: findValues(inst, vm.tparams.parameters.data.value)
      }
    }),
    emphasis: {
      itemStyle: {
        shadowBlur: 10,
        shadowOffsetX: 0,
        shadowColor: 'rgba(0, 0, 0, 0.5)'
      }
    },
    itemStyle: {
      borderRadius: 0,    
      borderWidth: 1
    },
    label: labeling === 'ON' ? {
      show: true,
      formatter: '{a|{a}}{abg|}\n{hr|}\n  {b|{b}：}{c}  {per|{d}%}  ',
      backgroundColor: '#F6F8FC',
      borderColor: '#8C8D8E',
      borderWidth: 1,
      borderRadius: 4,
      rich: {
        a: {
          color: '#6E7079',
          lineHeight: 22,
          align: 'center'
        },
        hr: {
          borderColor: '#8C8D8E',
          width: '100%',
          borderWidth: 1,
          height: 0
        },
        b: {
          color: '#4C5058',
          fontSize: 12,
          fontWeight: 'bold',
          lineHeight: 33
        },
        per: {
          color: '#fff',
          backgroundColor: '#4C5058',
          padding: [3, 4],
          borderRadius: 4
        }
      }
    } : {}
  }
}

vm.vueconfig = {
  el: document.getElementById(vm.id).querySelector('vuetemplate .gadget-app-pieChart'),
  data: {
    ds: [],
    chartLoading: true,
    loadingOptions: {
      text: "Loading Chart...",
      textColor: "#000000",
      color: "#4ea397",
      maskColor: "#ffffffcc"
    },
    isNoData: false,
    noData: 'Data not found.',
    chartShow: true,
    chart: {
      theme: vm.tparams.parameters.main.theme,
      title: vm.tparams.parameters.main.title,
      subTitle: vm.tparams.parameters.main.subTitle,
      options: false,
      labeling: vm.tparams.parameters.main.labeling
    }
  },
  computed: {
    setThemePosition() {
      return this.isGadgetHeaderEnable ? { top: '25px', right: '30px' } : { top: '65px', right: '30px' }
    },
    setColorTheme () {
      return this.chart.theme === 'dark' ? '#eeeeee' : this.chart.theme === 'vintage' ? '#666666' : this.chart.theme === 'light' ? '#333333' : ''
    },
    option() {
      var that = this
      return {      
        title: {
					show: this.chart.title ? true : false,
					text: this.chart.title,
					subtext: this.chart.subTitle,
					left: 12,
					top: 0,
					textStyle: {
						fontSize: 16,
						fontFamily: "Soho, Poppins",
						fontWeight: 'normal',
            color: this.setColorTheme
					},
					subtextStyle: {
						fontSize: 12,
						fontFamily: "Soho, Poppins",
            color: this.setColorTheme
					}
				},
        tooltip: {
          trigger: this.chart.labeling === 'ON' ? 'none' : 'item',
        },
        toolbox: {
          show: vm.tparams.parameters.main.toolbox === "ON",
          orient: "vertical",
          itemSize: 11,
          itemGap: 6,
          top: "15%",
          right: "-6",
          feature: {
            dataView: { show: true, readOnly: false },
            restore: { show: true },
            saveAsImage: { show: true, type: 'png' }
          }
        },
        legend: {
          orient: "horizontal",
          top: 26,
          show: vm.tparams.parameters.main.showLegend === "ON",
          right: this.chart.title ? "12%" : 'auto',
          itemWidth: 3,
          itemHeight: 10,
          itemGap: 10,
          textStyle: {
            fontFamily: "Soho, Poppins",
            fontWeight: 400,
            fontSize: 11,
            color: this.setColorTheme
          }
        },
        series: calculateSerie(this.ds, this.chart.labeling),
      }
    }
  },
  methods: {
    handleRestore: function (e) {
      vm.sendFilter(vm.tparams.parameters.data.value, '', '<>')
    },
    handleClickEvent: function (e) {
      vm.sendFilter(vm.tparams.parameters.data.value, findValues(this.ds[e.dataIndex], vm.tparams.parameters.data.value));
    },
    drawVueComponent: function (newData, oldData) {
      //This will be call on new data
      if (!newData || newData.length === 0) { this.chartShow = false; this.isNoData = true } else { this.chartShow = true; this.isNoData = false }
    },
    resizeEvent: function () {
      //Resize event
    },
    destroyVueComponent: function () {
      vm.vueapp.$destroy();
    },
    receiveValue: function (data) {
      //data received from datalink
    },

    // Change echart Theme and gadget.
    toggleTheme() {
      let newTheme = this.chart.theme === 'vintage' ? 'dark' : this.chart.theme === 'dark' ? 'light' : 'vintage'
      this.chart.theme = newTheme
      this.applyTheme(this.chart.theme)
    },
    // debug Options
    toggleOption(option) {
      var that = this
      if (!option) return false
      const specialOptions = [
        { id: 'theme', options: ['dark', 'light', 'vintage'] },
        { id: 'orientation', options: ['vertical', 'horizontal'] },
        { id: 'position', options: ['left', 'right'] }
      ]
      var setValue = function (option) {
        let value = undefined
        let pos = 0
        pos = (specialOptions.filter(x => x.id === option).map(y => y.options)[0].indexOf(that.chart[option]) + 1) < specialOptions.filter(x => x.id === option).map(y => y.options)[0].length ? (specialOptions.filter(x => x.id === option).map(y => y.options)[0].indexOf(that.chart[option]) + 1) : 0
        return specialOptions.filter(x => x.id === option).map(y => y.options)[0][pos]
      }
      specialOptions.filter(x => x.id === option).length === 0 ? this.$set(this.chart, option, !this.chart[option]) : this.$set(this.chart, option, setValue(option))

    },
    applyTheme(themeColor) {
      const light = { background: 'white', color: '#333' }
      const dark = { background: '#100c2a', color: 'white' }
      const vintage = { background: '#fef8ef', color: '#333' }

      // ELEMENTS: TOOGLE, GADGET (OPTIONAL HEADER), CONTAINER, GRIDSTER-ITEM
      var gadget = this.$el
      // var toolbar = gadget.querySelector('div.toolbar')
      var container = gadget.closest('div.element-container')
      var containerHeader = container.querySelector('div.widget-header')
      var containerTitle = containerHeader ? containerHeader.children[0] : null
      var vuetemplate = gadget.closest('vuetemplate')
      var gridster = gadget.closest('gridster-item')

      switch (themeColor) {
        case "vintage":
          gadget.style.backgroundColor = vintage.background
          // toolbar.style.color = vintage.color
          container.style.backgroundColor = vintage.background
          if (containerHeader) { containerHeader.style.backgroundColor = vintage.background; containerTitle.style.color = vintage.color }
          vuetemplate.style.backgroundColor = vintage.background
          gridster.style.backgroundColor = vintage.background
          break;
        case "dark":
          gadget.style.backgroundColor = dark.background
          // toolbar.style.color = dark.color
          container.style.backgroundColor = dark.background
          if (containerHeader) { containerHeader.style.backgroundColor = dark.background; containerTitle.style.color = dark.color }
          vuetemplate.style.backgroundColor = dark.background
          gridster.style.backgroundColor = dark.background
          break;
        case "light":
          gadget.style.backgroundColor = light.background
          // toolbar.style.color = light.color
          container.style.backgroundColor = light.background
          if (containerHeader) { containerHeader.style.backgroundColor = light.background; containerTitle.style.color = light.color }
          vuetemplate.style.backgroundColor = light.background
          gridster.style.backgroundColor = light.background
          break;
        default:
          console.log('This theme is not available')
      }
    },
    sendValue: vm.sendValue,
    sendFilter: vm.sendFilter
  },
  mounted() {
    this.applyTheme(this.chart.theme)
  },
  components: {
    'v-chart': VueECharts
  }
}
//Init Vue app
vm.vueapp = new Vue(vm.vueconfig);
