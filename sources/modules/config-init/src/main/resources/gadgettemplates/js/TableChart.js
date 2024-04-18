//Write your Vue JSON controller code here

//Focus here and F11 to full screen editor

//This function will be call once to init components

vm.vueconfig = {
	el: document.getElementById(vm.id).querySelector('vuetemplate  .gadget-app'),
	data: {
		ds: [],
		tableData: [],
		searchString: "",
		showTitle: vm.tparams.parameters.general.showTitle,
		title: vm.tparams.parameters.general.title,
		checkFilter: vm.tparams.parameters.general.filter,
		checkHidden: vm.tparams.parameters.general.hidden,
		checkSelection: vm.tparams.parameters.general.selector,
		checkNumeric: vm.tparams.parameters.general.numeric,
		checkExtra: vm.tparams.parameters.extraInfo.checkInfo,
		stripe: vm.tparams.parameters.general.stripe,
		border: vm.tparams.parameters.general.border,
		fit: vm.tparams.parameters.general.fit,
		hiddenColumns: [],
		columns: vm.tparams.parameters.columns,
		page: 1,
		pageSizes: [
			10,
			20,
			30,
			40,
			50,
			100
		],
		pageSize: 20,
		minimunPageSize: 10,
		currentPage: 1,
		sortField: null,
		sortOrder: null,
	},
	methods: {
		drawVueComponent: function (newData, oldData) {
			if (vm.tparams.parameters.extraDatasource && newData) {
				this.ds = [];
				newData.forEach(function (record) {
					record['hasChildren'] = true
				})
				this.$nextTick(() => {
					this.ds = newData
				})
			}


		},

		handleCell: function (c, scope) {
			var data = vm.utils.findValues(scope.row, c.field)
			if (!(typeof data === 'undefined' || data === null)) {
				return c.prefix + this.transform(data, c.format) + c.postfix;
			} else {
				return c.noValueText;
			}
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
		sendRowFilter: function (e) {
			vm.sendFilter(vm.tparams.parameters.columns[0].field, vm.utils.findValues(e, vm.tparams.parameters.columns[0].field))
			vm.sendValue('value', vm.utils.findValues(e, vm.tparams.parameters.columns[0].field))
		},
		calculateColor(c, scope) {
			if (c.condcolor.op == null) {
				return c.defcolor
			} else {
				var field = c.field
				var arr = field.split(".")
				var calcColor = ""
				if ((c.condcolor.op == "=") && (scope.row[arr[0]][arr[1]] == c.condcolor.value)) {
					calcColor = c.condcolor.color
				} else if ((c.condcolor.op == "<") && (scope.row[arr[0]][arr[1]] < c.condcolor.value)) {
					calcColor = c.condcolor.color
				}else if ((c.condcolor.op == ">") && (scope.row[arr[0]][arr[1]] > c.condcolor.value)) {
					calcColor = c.condcolor.color
				}else if ((c.condcolor.op == "<=") && (scope.row[arr[0]][arr[1]] <= c.condcolor.value)) {
					calcColor = c.condcolor.color
				}else if ((c.condcolor.op == ">=") && (scope.row[arr[0]][arr[1]] >= c.condcolor.value)) {
					calcColor = c.condcolor.color
				}
				return calcColor
			}

		},
		transform: function (value, format) {
			switch (format) {
				case 'HH:MM':
					return Math.floor(value / 3600) + ':' + (Math.floor((value / 60) % 60) + '').padStart(2, '0')
					break;
				default:
					return value;
			}
		},
		handleSizeChange(val) {
			this.pageSize = val
		},
		handleCurrentChange(val) {
			this.page = val
		},
		load(tree, treeNode, resolve) {
			var filterValue = tree[vm.tparams.parameters.extraDatasourceFilterField]
			if (typeof filterValue == 'string') {
				filterValue = "'" + filterValue + "'"
			}
			vm.from(vm.tparams.parameters.extraDatasource)
				.filter(vm.utils.datastatusToFilter(vm.datastatus))
				.filter(vm.tparams.parameters.extraDatasourceFilterField, filterValue)
				.exec()
				.then(resolve)
		},
		handlePageChange(page) {
			this.page = page
		},
		handlePageSizeChange(pageSize) {
			this.pageSize = pageSize
			this.handlePageChange(this.page)
		},
		tableDatafilter: function (element) {
			if (this.searchString == null || this.searchString.trim().length == 0) {
				return true;
			}
			return JSON.stringify(element).toLowerCase().indexOf(this.searchString.toLowerCase()) > -1;
		},
		showSelectionColumn: function () {
			if (vm.tparams.parameters.general.selector) {
				return true;
			}
		},
		handleSelectionChange(val) {
			this.multipleSelection = val;
		},

		//Calculate and map the extraInfo
		handleExtraData: function (scope) {
			var data = []
			for (let i = 0; i < vm.tparams.parameters.extraInfo.extra.length; i++) {
				var field = vm.tparams.parameters.extraInfo.extra[i].extraField;
				var arr = field.split(".")
				var key = vm.tparams.parameters.extraInfo.extra[i].extraKey
				var value = scope.row[arr[0]][arr[1]]
				data.push(key + ": " + value)
			}
			return data
		}
	},
	computed: {
		pageSizes() {
			return [
				this.minimumPageSize,
				this.minimumPageSize * 2,
				this.minimumPageSize * 3,
				this.minimumPageSize * 4,
				this.minimumPageSize * 5,
				this.minimumPageSize * 10
			]
		},
		totalItems() {
			return this.filteredData.length
		},
		filteredData() {
			console.log(this.ds.filter(this.tableDatafilter))
			return this.ds.filter(this.tableDatafilter)
		},
		pageCount() {
			return Math.floor(this.totalItems / this.pageSize)
		},
		paginatedData() {
			return this.filteredData.slice(this.pageSize * this.page - this.pageSize, this.pageSize * this.page)
		},
		totalRecords: function () {
			return this.ds.length
		},
	},
	//For Pagination
	i18n: (window.i18n?window.i18n:(new VueI18n({
		locale: 'EN', fallbackLocale: 'EN',
		messages: { default: "EN", languages: { "EN": {} } }
	})))
	
}

//Init Vue app
vm.vueapp = new Vue(vm.vueconfig);