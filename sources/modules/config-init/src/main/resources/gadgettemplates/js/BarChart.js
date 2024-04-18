/*Loading options
vm.tparams.parameters.loading.showloading = true
vm.tparams.parameters.loading.text = "Loading Chart..."
vm.tparams.parameters.loading.loadingtextcolor = "#000000"
vm.tparams.parameters.loading.loadingcolor = "#4ea397"
vm.tparams.parameters.loading.loadingbackground = "#ffffffcc"*/

// GADGET BARCHART CONFIG  
vm.vueconfig = {
	el: document.getElementById(vm.id).querySelector('vuetemplate  .gadget-app_barChart'),
	data: {
		ds: [
		],
		chartLoading: true,
		loadingOptions: {
			text: "Loading Chart...",
			textColor: "#000000",
			color: "#4ea397",
			maskColor: "#ffffffcc"
		},
		width: 0,
		height: 0,
		chartShow: true,
		chart: {
			debug: false, // Beta functionality
			debugOptions: ['legend', 'toolBox', 'excel', 'gradients', 'orientation', 'position'],
			legend: vm.params.parameters.main.legend === 'ON',
			toolBox: vm.params.parameters.main.toolbox === 'ON',
			excel: vm.params.parameters.main.excel === 'ON',
			gradients: false, // Beta functionality vm.params.parameters.main.gradients === 'ON',
			options: false, // Beta functionality vm.params.parameters.main.options === 'ON',
			zoom: vm.params.parameters.zoom.showzoom === 'ON',
			zoomConf: [
				{
					show: vm.params.parameters.zoom.showxzoom === 'ON',
					height: 20,
					type: vm.params.parameters.zoom.xzoomtype,
					xAxisIndex: 0,
					filterMode: 'none'
				},
				{
					show: vm.params.parameters.zoom.showyzoom === 'ON',
					width: 20,
					type: vm.params.parameters.zoom.yzoomtype,
					yAxisIndex: 0,
					filterMode: 'none',
					showDataShadow: false,
					left: '93%',
					bottom: vm.params.parameters.axis.xaxis.xaxisname ? '30%' : 0
				}
			],
			orientation: vm.params.parameters.main.orientation.toLowerCase(),
			theme: vm.params.parameters.main.theme.toLowerCase(),
			title: vm.params.parameters.main.title,
			subTitle: vm.params.parameters.main.subtitle,
			axisDataField: vm.params.parameters.axis.xaxis.axisdatafield,
			xAxisName: vm.params.parameters.axis.xaxis.xaxisname,
			axisData: [], // filled with ds data.
			series: vm.params.parameters.series
		},
		gradients: [
			{ id: 'lightBlue', firstColor: '#46d2fd', secondColor: '#4f79f4' },
			{ id: 'gray', firstColor: '#bdc3c7', secondColor: '#2c3e50' },
			{ id: 'gold', firstColor: '#FDC830', secondColor: '#F37335' },
			{ id: 'coral', firstColor: '#ff9966', secondColor: ' #ff5e62' }
		]

	},
	methods: {
		checkData() {
			// put some delay to allow ds to get data from server
			setTimeout(() => { return this.ds.length > 0 }, 300)
		},
		drawVueComponent: function (newData, oldData) {
			//This will be call on new data
			if (!newData || newData.length === 0) { this.chartShow = false } else { this.chartShow = true }
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
		// find values in data --> vm.utils.findvalues
		findValues(jsonData, path) {
			path = path.replace(/\[(\w+)\]/g, '.$1');
			path = path.replace(/^\./, ''); // strip a leading dot
			var pathArray = path.split('.');
			for (var i = 0, n = pathArray.length; i < n; ++i) {
				var key = pathArray[i];
				if (key in jsonData) {
					if (jsonData[key] !== null) {
						jsonData = jsonData[key];
					} else {
						return null
					}
				} else {
					return key
				}
			}
			return jsonData
		},

		// get gadget dimensions after gadgetLoaded
		getDimensions() {
			this.width = this.$refs.gadget.clientWidth
			this.height = this.$refs.gadget.clientHeight
		},
		// resize chart on window.resize 
		resizeChart() {
			console.log('Resize of chart...')
		},
		// calculate series 
		calculateSeries(data) {
			var that = this
			if (this.chart.series.length === 0) { return [] }

			this.chart.series = this.chart.series.map(function (serie) {
				return {
					...serie,
					data: data.map(inst => that.findValues(inst, serie.yaxis)),
					markPoint: serie.maxmin === 'ON' ? {
						data: [
							{ type: 'max', name: 'Max', label: { fontStyle: "normal", fontWeight: 400, fontFamily: "Soho, Poppins, Arial", fontSize: 11 } },
							{ type: 'min', name: 'Min', label: { fontStyle: "normal", fontWeight: 400, fontFamily: "Soho, Poppins, Arial", fontSize: 11 } }
						]
					} : {},
					markLine: serie.average === 'ON' ? {
						data: [
							{ type: 'average', name: 'Average Value', label: { fontStyle: "normal", fontWeight: 400, fontFamily: "Soho, Poppins, Arial", fontSize: 11 } }
						]
					} : {},
					type: 'bar',
					yAxisIndex: serie.idAxis,
					barWidth: that.chart.orientation === 'horizontal' ? 'auto' : data.map(inst => that.findValues(inst, serie.yaxis)).length > 20 ? 'auto' : 6,
					showBackground: false,
					itemStyle: that.chart.gradients && serie.color ? {
						color: {
							x: 0,
							y: 0,
							x2: 0,
							y2: 1,
							type: 'linear',
							global: false,
							colorStops: [
								{
									offset: 0,
									color: that.gradientColor(serie.color, 0)
								},
								{
									offset: 1,
									color: that.gradientColor(serie.color, 1)
								}
							]
						},
						barBorderRadius: serie.stack ? [0, 0, 0, 0] : that.chart.orientation === 'horizontal' ? [0, 10, 10, 0] : [10, 10, 0, 0]
					} : { color: serie.color }
				}
			})
			return this.chart.series
		},
		// chart X orientation Vertical or Horizontal.		
		orientationX(tipo, data, position) {
			if (tipo == 'vertical') {
				// store axis data for excel
				this.chart.axisData = data.map(inst => this.findValues(inst, this.chart.axisDataField))
				return {
					type: 'category',
					name: this.chart.xAxisName,
					nameTextStyle: {
						fontFamily: "Soho, Poppins, Arial",
						fontWeight: 500,
						fontSize: 11,
					},
					data: this.chart.axisData,
					id: vm.tparams.parameters.axis.yaxis.id,
					position: position,
					axisLabel: {
						show: true,
						inside: false,
						fontFamily: "Soho, Poppins, Arial",
						fontWeight: 400,
						fontSize: 11,
						rotate: this.chart.axisData.length < 15 ? 0 : 30
					},
					axisTick: {
						show: false
					},
					axisLine: {
						show: false
					},
					z: 10

				}
			} else {
				return {
					type: 'value',
					name: this.chart.yAxisName,
					nameTextStyle: {
						fontFamily: "Soho, Poppins, Arial",
						fontWeight: 500,
						fontSize: 11,
					},
					position: position,
					splitNumber: 4,
					axisLine: {
						show: false
					},
					axisTick: {
						show: false
					},
					axisLabel: {
						show: true,
						fontFamily: "Soho,Poppins, Arial",
						fontWeight: 400,
						fontSize: 11
					}
				}
			}
		},
		// chart Y orientation Vertical or Horizontal.
		orientationY(tipo, data) {
			if (tipo == 'vertical') {
				return vm.tparams.parameters.axis.yaxis.map(function (yAxis) {
					var yAxisLocal = {
						type: "value",
						id: yAxis.yaxisid,
						position: yAxis.position,
						name: yAxis.yaxisname,
						nameTextStyle: {
							fontFamily: "Soho, Poppins, Arial",
							fontWeight: 500,
							fontSize: 11,
						},
						axisLabel: {
							show: true,
							inside: false,
							fontFamily: "Soho, Poppins, Arial",
							fontWeight: 400,
							fontSize: 11
						},
						axisTick: {
							show: false
						},
						axisLabel: {
							show: true,
							fontFamily: "Soho,Poppins, Arial",
							fontWeight: 400,
							fontSize: 11
						}
					}
					if (yAxis.min) {
						yAxisLocal['min'] = yAxis.min
					}
					if (yAxis.max) {
						yAxisLocal['max'] = yAxis.max
					}
					return yAxisLocal;
				})
			} else {
				// store axis data for excel
				this.chart.axisData = data.map(inst => this.findValues(inst, this.chart.axisDataField))
				return vm.tparams.parameters.axis.yaxis.map(function (yAxis) {
					var yAxisLocal = {
						type: "category",
						id: yAxis.yaxisid,
						position: yAxis.position,
						name: yAxis.yaxisname,
						nameTextStyle: {
							fontFamily: "Soho, Poppins, Arial",
							fontWeight: 500,
							fontSize: 11,
						},
						axisLabel: {
							show: true,
							inside: false,
							fontFamily: "Soho, Poppins, Arial",
							fontWeight: 400,
							fontSize: 11
						},
						axisTick: {
							show: false
						},
						axisLabel: {
							show: true,
							fontFamily: "Soho,Poppins, Arial",
							fontWeight: 400,
							fontSize: 11
						}
					}
					if (yAxis.min) {
						yAxisLocal['min'] = yAxis.min
					}
					if (yAxis.max) {
						yAxisLocal['max'] = yAxis.max
					}
					return yAxisLocal;
				})
			}
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
		// apply echart theme when selected
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
		// use gradients for selected colors To-do: create gradients for ods colorpicker elements
		gradientColor(id, pos) {
			// backup
			const firstColor = '#bdc3c7'
			const secondColor = '#2c3e50'
			let field = 'firstColor'
			if (pos === undefined) { field = 'firstColor' } else { field = pos === 0 ? 'firstColor' : 'secondColor' }

			if (this.gradients.filter(x => x.id === id).length > 0) {
				return this.gradients.filter(x => x.id === id).map(y => y[field])[0]
			} else {
				return pos === 0 ? firstColor : pos === 1 ? secondColor : firstColor
			}
		},
		// export to excel
		exportExcel(dataForExcel) {
			const data = XLSX.utils.json_to_sheet(dataForExcel)
			const workbook = XLSX.utils.book_new()
			const filename = this.chart.title.length > 31 ? this.chart.title.substr(0, 31) : this.chart.title
			XLSX.utils.book_append_sheet(workbook, data, filename)
			XLSX.writeFile(workbook, `${filename}.xlsx`)
		},
		handleRestore: function (e) {
			vm.sendFilter(vm.params.parameters.axis.xaxis.axisdatafield, '', '<>')
		},
		handleClickEvent: function (e) {
			vm.sendFilter(vm.params.parameters.axis.xaxis.axisdatafield, this.findValues(this.ds[e.dataIndex], vm.params.parameters.axis.xaxis.axisdatafield))
		},
		handleDataZoom: function (e) {
			vm.sendFilter(vm.params.parameters.axis.xaxis.axisdatafield, this.buildBetweenValues([e.batch[0].startValue], [e.batch[0].endValue]), 'between')
		},
		buildBetweenValues: function (v1, v2) {
			return this.findValues(this.ds[v1], vm.params.parameters.axis.xaxis.axisdatafield) + ' and ' + this.findValues(this.ds[v2], vm.params.parameters.axis.xaxis.axisdatafield)
		},
	},
	computed: {
		isGadgetHeaderEnable() {
			return this.$el.closest('div.element-container').querySelector('div.widget-header') || false
		},
		setThemePosition() {
			return this.isGadgetHeaderEnable ? { top: '25px', right: '30px' } : { top: '65px', right: '30px' }
		},
		getBottom() {
			return !this.chart.zoom ? 0 : this.chart.zoomConf.filter(x => (x.type === 'slider' && x.show === true)).length > 0 ? 60 : 0
		},
		// echarts options for v-chart component
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
						fontFamily: "Soho, 'Soho Gothic Pro',Poppins, Raleway",
						fontWeight: 400
					},
					subtextStyle: {
						fontSize: 13,
						fontFamily: "Soho, Poppins, Raleway",
					}
				},
				legend: {
					type: "scroll",
					show: this.chart.legend,
					right: this.chart.title ? "12%" : 'auto',
					top: 26,
					orient: "horizontal",
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
				responsive: true,
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
				grid: {
					top: 75,
					left: 32,
					rigth: 32,
					bottom: this.getBottom,
					borderWidth: 0,
					containLabel: true
				},
				xAxis: this.orientationX(this.chart.orientation, this.ds, this.chart.position),
				yAxis: this.orientationY(this.chart.orientation, this.ds),
				series: this.calculateSeries(this.ds),
				toolbox: this.chart.toolBox ? { show: true, orient: "vertical", itemSize: 11, itemGap: 6, top: "15%", right: "-6", showTitle: true, feature: { magicType: { type: ["line", "bar", "stack"] }, restore: {}, dataZoom: { yAxisIndex: 'none' }, saveAsImage: {} } } : {},
				dataZoom: this.chart.zoom ? this.chart.zoomConf : []
			}
		},
		// export to excel data preparation
		dataForExcel() {
			return this.chart.series.map((serie, i) => {
				const data = serie.data.map((e, i) => ({ [this.chart.axisData[i]]: e, })).reduce((acc, curr, i) => ({ ...acc, [`${Object.keys(curr)}`]: Object.values(curr)[0], }), {})
				return { ['']: serie.name, ...data }
			})
		},
	},
	watch: {
		// whenever ds changes, this function will run
		'chart.series'(series) {
			console.log('CHECK SERIES...')
			this.checkData()
		}
	},
	mounted() {
		// if loading enable, loading chart
		setTimeout(() => { this.chartLoading = false }, 2000)
		this.applyTheme(this.chart.theme)
	},
	components: {
		'v-chart': VueECharts
	}
}

//Init Vue app
vm.vueapp = new Vue(vm.vueconfig);
