//Write your Vue with JSON controller code here

//Focus here and F11 or F10 to full screen editor

function calculateSeries(data) {
    return vm.tparams.parameters.series.map(function (s) {
        var that = this
        var ds = ds;
        var s = {
            type: s.type,
            name: s.label,
            yAxisIndex: s.yAxis,
            data: data.map(inst => vm.utils.findValues(inst, s.field)),
            color: s.color
        }

        if (s.type == 'point') {
            s.type = 'line'
            s.lineStyle = {
                width: 0
            }
        }
        if (s.type == 'dashed') {
            s.type = 'line',
            s.symbolSize = 0,
            s.lineStyle = {
                width: 2,
                type: 'dashed'
            }
        }
        if (s.type == 'dotted') {
            s.type = 'line',
            s.symbolSize = 0,
            s.lineStyle = {
                width: 2,
                type: 'dotted'
            }
        }
        return s;
    })
}

//This function will be call once to init components
vm.vueconfig = {
    el: document.querySelector('#' + vm.id + ' .gadget-app'),
    data: {
        ds: [],
        legendselected: {}
    },
    computed: {
        chartConfig() {
            return {
                legend: {
                    show: vm.tparams.parameters.general.showLegend,
                    selected: this.legendselected
                },
                grid: {
                    left: Math.max(0, ...vm.tparams.parameters.axes.yAxis.filter(axis => axis.position === 'left').map(axis => parseInt(axis.offset?axis.offset:0))) + 60,
                    right: Math.max(0, ...vm.tparams.parameters.axes.yAxis.filter(axis => axis.position === 'right').map(axis => parseInt(axis.offset?axis.offset:0))) + 60
                },
                xAxis: {
                    type: 'category',
                    data: this.ds.map(inst => vm.utils.findValues(inst, vm.tparams.parameters.axes.xAxis.field)),
                    name: vm.tparams.parameters.axes.xAxis.label
                },
                yAxis: vm.tparams.parameters.axes.yAxis.map(function (yAxis) {
                    var yAxisLocal = {
                        id: yAxis.id,
                        position: yAxis.position,
                        type: yAxis.type,
                        name: yAxis.label,
                        offset: parseInt(yAxis.offset?yAxis.offset:0)
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
                tooltip: {
                    show: vm.tparams.parameters.general.showTooltip,
                    axisPointer: {
                        type: 'cross'
                    },
                    trigger: 'axis'
                },
                dataZoom: [
                    {
                        show: vm.tparams.parameters.axes.xAxis.showZoom,
                        realtime: true
                    }
                ]
            }
        }
    },
    methods: {
        drawVueComponent: function (newData, oldData) {
            //This will be call on new data
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
        sendValue: vm.sendValue,
        sendFilter: vm.sendFilter,
        legendselectchanged: function(e) {
            this.legendselected = e.selected;
        }
    },
    components: {
        'v-chart': VueECharts
    }
}

vm.drawLiveComponent = function () { }

//Init Vue app
vm.vueapp = new Vue(vm.vueconfig);
