//Write your Vue JSON controller code here

//Focus here and F11 to full screen editor

//This function will be call once to init components

//This function claculate and map the series
function handleColor(value) {
  if (!vm.tparams.parameters.color.degraded) {
    const porcentaje = (value - 0) / (vm.tparams.parameters.main.maxValue - 0);
    const colorMin = vm.tparams.parameters.color.minColor;
    const colorMax = vm.tparams.parameters.color.maxColor;
    const colorMinArr = colorMin.match(/\d+/g).map(Number);
    const colorMaxArr = colorMax.match(/\d+/g).map(Number);
    const r = Math.round(colorMinArr[0] + (colorMaxArr[0] - colorMinArr[0]) * porcentaje);
    const g = Math.round(colorMinArr[1] + (colorMaxArr[1] - colorMinArr[1]) * porcentaje);
    const b = Math.round(colorMinArr[2] + (colorMaxArr[2] - colorMinArr[2]) * porcentaje);

    return `rgb(${r}, ${g}, ${b})`;

  } else {

    return {
      type: 'linear',
      x: 0.2,
      y: 0,
      x2: 0.8,
      y2: 1,
      colorStops: [{
        offset: 0, color: vm.tparams.parameters.color.maxColor // 0%
      }, {
        offset: 1, color: vm.tparams.parameters.color.minColor // 100%
      }],
      global: false
    }
  }
}

function handleSizes(size, type) {
  var max = vm.isMaximized
  if (type === "text") {
    if (size === "l") {
      return 16
    } else if (size === "m") {
      return 12
    } else if (size === "s") {
      return 10
    }
  } else if (type === "line") {
    if (size === "l") {
      return 10
    } else if (size === "m") {
      return 7
    } else if (size === "s") {
      return 4
    }
  } else if (type === "stepLine") {
    if (size === "l") {
      return 5
    } else if (size === "m") {
      return 3
    } else if (size === "s") {
      return 2
    }
  }
}

function calculateSerie(data) {

  var sizePointer = handleSizes(vm.tparams.parameters.main.size, "text")
  var sizeAxis = handleSizes(vm.tparams.parameters.main.size, "line")
  var sizeStep = handleSizes(vm.tparams.parameters.main.size, "stepLine")
  var si
  return {
    type: 'gauge',
    startAngle: 200,
    endAngle: -20,
    center: ['50%', '70%'],
    redius: '10%',
    splitNumber: vm.tparams.parameters.option.splitNumber,
    max: vm.tparams.parameters.main.maxValue,
    pointer: {
      show: vm.tparams.parameters.option.pointer,
      icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
      length: sizePointer,
      width: sizePointer,
      //must handle offset
      offsetCenter: [0, "-70%"],
      itemStyle: {
        color: "gray"
      }
    },
    progress: {
      show: true,
      overlap: false,
      roundCap: true,
      clip: false
    },
    axisLine: {
      show: true,
      roundCap: true,
      lineStyle: {
        width: sizeAxis
      }
    },
    splitLine: {
      show: vm.tparams.parameters.option.splitLine,
      distance: 0,
      lineStyle: {
        width: sizeStep,
      },
      length: sizeAxis
    },
    axisTick: {
      show: false
    },
    axisLabel: {
      show: false,
      fontSize: sizePointer,
      //handle distance
      distance: -40,
      rotate: 'tangential',
      fontFamily: "Soho, Poppins, Arial",
      fontWeight: 400,
      formatter: function (value) {
        return parseInt(value)
      }
    },
    title: {
      fontSize: sizePointer
    },
    detail: {
      valueAnimation: true,
      width: '60%',
      lineHeight: 40,
      borderRadius: 8,
      offsetCenter: [0, '45%'],
      fontSize: sizePointer,
      fontFamily: "Soho, Poppins, Arial",
      fontWeight: 400,

    },
    data: data.map(inst => {
      return {
        value: vm.utils.findValues(inst, vm.tparams.parameters.data.value),
        name: vm.tparams.parameters.data.name,
        itemStyle: {
          color: handleColor(vm.utils.findValues(inst, vm.tparams.parameters.data.value)),
        },
      }
    })

  }
}

vm.vueconfig = {
  el: document.getElementById(vm.id).querySelector('vuetemplate .gadget-app-gauge'),
  data: {
    ds: [],
    chartLoading: false,
    loadingOptions: {
      text: "Loading Chart…",
      color: "#4ea397",
      maskColor: "rgba(255, 255, 255, 0.5)"
    },
    isNoData: false,
    noData: 'Data not found.',
    chartShow: true,
  },
  computed: {
    theme() {
      this.applyTheme(vm.tparams.parameters.main.theme)
      return vm.tparams.parameters.main.theme
    },
    setThemePosition() {
      return this.isGadgetHeaderEnable ? { top: '25px', right: '30px' } : { top: '65px', right: '30px' }
    },
    setColorTheme() {
      return vm.tparams.parameters.main.theme === 'dark' ? '#eeeeee' : vm.tparams.parameters.main.theme === 'vintage' ? '#666666' : vm.tparams.parameters.main.theme === 'light' ? '#333333' : ''
    },
    option() {
      var that = this
      const titleSize = handleSizes(vm.tparams.parameters.main.size, "title")
      var lineSize = handleSizes(vm.tparams.parameters.main.size, "line")
      return {
        title: {
          show: true,
          text: vm.tparams.parameters.main.title,
          subtext: vm.tparams.parameters.main.subTitle,
          left: "center",
          top: 0,
          textStyle: {
            fontSize: titleSize,
            fontFamily: "Soho, 'Soho Gothic Pro',Poppins, Raleway",
            fontWeight: 400
          },
          subtextStyle: {
            fontSize: (titleSize * 0.75),
            fontFamily: "Soho, Poppins, Raleway",
          }
        },
        toolbox: {
          showTitle: false,
          show: vm.tparams.parameters.main.toolbox,
          orient: "vertical",
          itemSize: 7,
          itemGap: 1,
          top: "15%",
          right: "-6",
          feature: {
            dataView: { show: true, readOnly: false },
            restore: { show: true },
            saveAsImage: { show: true, type: 'png' }
          }
        },
        series: calculateSerie(this.ds),

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
      debugger
    },
    destroyVueComponent: function () {
      vm.vueapp.$destroy();
    },
    receiveValue: function (data) {
      //data received from datalink
    },

    // Change echart Theme and gadget.
    toggleTheme() {
      let newTheme = vm.tparams.parameters.main.theme === 'vintage' ? 'dark' : vm.tparams.parameters.main.theme === 'dark' ? 'light' : 'vintage'
      vm.tparams.parameters.main.theme = newTheme
      this.applyTheme(vm.tparams.parameters.main.theme)
    },

    applyTheme(themeColor) {
      const light = { background: 'white', color: '#333' }
      const dark = { background: '#100c2a', color: 'white' }
      const vintage = { background: '#fef8ef', color: '#333' }
      const none = { background: null, color: null }

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
        case "none":
          gadget.style.backgroundColor = none.background
          // toolbar.style.color = none.color
          container.style.backgroundColor = none.background
          if (containerHeader) { containerHeader.style.backgroundColor = none.background; containerTitle.style.color = none.color }
          vuetemplate.style.backgroundColor = none.background
          gridster.style.backgroundColor = none.background
          break;
        default:
          console.log('This theme is not available')
      }
    },
    sendValue: vm.sendValue,
    sendFilter: vm.sendFilter
  },
  mounted() {

    //setTimeout(() => { this.chartLoading = false }, 3000)
    this.applyTheme(vm.tparams.parameters.main.theme)
  },
  components: {
    'v-chart': VueECharts
  }
}
vm.resizeEvent = function () {
 
};
//Init Vue app
vm.vueapp = new Vue(vm.vueconfig);
