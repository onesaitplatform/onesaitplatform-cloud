//Write your Vue JSON controller code here

//Focus here and F11 to full screen editor
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
function calculateSeries(data) {
    return vm.tparams.parameters.series.map(function (s) {
        var full = null;
        var step = '';
        var smooth = false;
        if (s.steppedLine == true) {
            step = 'start'
        } else {
            smooth = true
        }
        if (s.fullSerie == true) {
            full = {
                color: s.shadowColor
            }
        }
        if (s.markPoint == true) {
            var point = {
                data: [
                    { type: 'max', name: 'Max' }, { type: 'min', name: 'Min' }
                ]
            }
        }
        if (s.markLine == true) {
            var line = {
                data: [
                    { type: 'average', name: 'Avg' }
                ]
            }
        }
        return {
            name: s.serieName,
            type: s.type,
            yAxisIndex: s.idAxis,
            data: data.map(inst => findValues(inst, s.yAxis)),
            color: s.color,
            symbolSize: s.pointRadius,
            areaStyle: full,
            step: step,
            smooth: smooth,
            markPoint: point,
            markLine: line

        }
    })
}
//This function will be call once to init components

vm.vueconfig = {
    el: document.getElementById(vm.id).querySelector('vuetemplate  .gadget-app-lineChart'),
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
            title: vm.tparams.parameters.main.title,
            subtitle: vm.tparams.parameters.main.subtitle,
            theme: vm.tparams.parameters.main.theme,
            zoom: vm.params.parameters.zoom.showzoom === 'on',
            zoomConf: [
                {
                    show: true,
                    height: 20,
                    type: vm.params.parameters.zoom.zoomtype,
                    xAxisIndex: 0,
                    filterMode: 'none'
                },
            ]
        }
    },
    computed: {
        setThemePosition() {
            return this.isGadgetHeaderEnable ? { top: '25px', right: '30px' } : { top: '65px', right: '30px' }
        },
        getBottom() {
            return !this.chart.zoom ? 0 : this.chart.zoomConf.filter(x => (x.type === 'slider' && x.show === true)).length > 0 ? 60 : 0
        },
        option() {
            var that = this
            return {
                textStyle: {
                    fontFamily: "Soho, 'Soho Gothic Pro',Poppins",
                },
                dataZoom: this.chart.zoom ? this.chart.zoomConf : [],
                grid: {
                    top: 75,
                    left: 32,
                    rigth: 32,
                    bottom: this.getBottom,
                    borderWidth: 0,
                    containLabel: true
                },
                title: {
                    text: this.chart.title,
                    subtext: this.chart.subtitle,
                    left: 12,
                    top: 0,
                    bot: 10,
                    textStyle: {
                        fontSize: 16,
                        fontFamily: "Soho, 'Soho Gothic Pro',Poppins",
                        fontWeight: 400
                    },
                    subtextStyle: {
                        fontSize: 13,
                        fontFamily: "Soho, Poppins",
                    }
                },
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow'
                    },
                    borderColor: 'rgba(0, 0, 0, .6)',
                    backgroundColor: 'rgba(255,255,255,0.95)',
                    textStyle: {
                        fontStyle: "normal",
                        fontWeight: 400,
                        fontSize: 11,
                        fontFamily: "Soho,Poppins, Arial",
                        color: '#666'
                    },
                    padding: [12, 12, 12, 12],
                    order: "seriesAsc",
                    confine: true,
                    extraCssText: "box-shadow: 0 0 3px rgba(0, 0, 0, 0.8);"
                },
                legend: {
                    type: "scroll",
                    orient: "horizontal",
                    show: vm.tparams.parameters.main.legendShow === "ON",
                    right: this.chart.title ? "12%" : 'auto',
                    top: 13,
                    z: 9999,
                    itemWidth: 3,
                    itemHeight: 10,
                    itemGap: 10,
                    selectedMode: true,
                    textStyle: {
                        fontFamily: "Soho, Poppins",
                        fontWeight: 400,
                        fontSize: 11
                    }
                },
                toolbox: {
                    show: vm.tparams.parameters.main.toolbox === "ON",
                    orient: "vertical",
                    itemSize: 11,
                    itemGap: 6,
                    top: "15%",
                    right: "-6",
                    feature: {
                        dataZoom: {
                            yAxisIndex: 'none'
                        },
                        dataView: { show: true, readOnly: false },
                        magicType: { show: true, type: ['line', 'bar'] },
                        restore: { show: true },
                        saveAsImage: { show: true }
                    }
                },
                calculable: true,
                xAxis: [
                    {
                        name: vm.tparams.parameters.axis.xAxisConfig.xName,
                        type: 'category',
                        axisLabel: {
                            show: true,
                            inside: false,
                            fontFamily: "Soho, Poppins, Arial",
                            fontWeight: 400,
                            fontSize: 11
                        },
                        nameTextStyle: {
                            fontFamily: "Soho, Poppins, Arial",
                            fontWeight: 500,
                            fontSize: 11,
                        },
                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            show: false
                        },
                        z: 10,
                        // prettier-ignore
                        data: this.ds.map(inst => findValues(inst, vm.tparams.parameters.axis.xAxisConfig.xAxis)),
                    }
                ],
                yAxis: vm.tparams.parameters.axis.yAxisConfig.map(function (yAxis) {
                    var yAxisLocal = {
                        id: yAxis.id,
                        position: yAxis.position,
                        name: yAxis.yName,
                        nameTextStyle: {
                            fontFamily: "Soho, Poppins, Arial",
                            fontWeight: 500,
                            fontSize: 11,
                        },
                        axisLabel: {
                            top: 10,
                            show: true,
                            inside: false,
                            fontFamily: "Soho, Poppins, Arial",
                            fontWeight: 400,
                            fontSize: 11
                        },
                        axisTick: {
                            show: false
                        }
                    }
                    if (yAxis.min) {
                        yAxisLocal['min'] = yAxis.min
                    }
                    if (yAxis.max) {
                        yAxisLocal['max'] = yAxis.max
                    }
                    return yAxisLocal;
                }),
                series: calculateSeries(this.ds),
            }
        }
    },
    methods: {
        handleRestore: function (e) {
            vm.sendFilter(vm.tparams.parameters.axis.xAxisConfig.xAxis, '', '<>')
        },
        handleClickEvent: function (e) {
            vm.sendFilter(vm.tparams.parameters.axis.xAxisConfig.xAxis, findValues(this.ds[e.dataIndex], vm.tparams.parameters.xAxisConfig.xAxis));
        },
        handleDataZoom: function (e) {
            vm.sendFilter(vm.tparams.parameters.axis.xAxisConfig.xAxis, this.buildBetweenValues([e.batch[0].startValue], [e.batch[0].endValue]), 'between')
        },
        buildBetweenValues: function (v1, v2) {
            return findValues(this.ds[v1], vm.tparams.parameters.axis.xAxisConfig.xAxis) +
                ' and ' +
                findValues(this.ds[v2], vm.tparams.parameters.axis.xAxisConfig.xAxis);
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

        // METHODS FOR THEME
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
        this.applyTheme(this.chart.theme)
    },
    components: {
        'v-chart': VueECharts

    }
}

//Init Vue app
vm.vueapp = new Vue(vm.vueconfig);
