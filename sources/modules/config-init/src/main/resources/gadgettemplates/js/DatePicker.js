//Write your Vue with ODS JSON controller code here

//Focus here and F11 or F10 to full screen editor

//This function will be call once to init components

vm.vueconfig = {
	el: document.getElementById(vm.id).querySelector('vuetemplate  .gadget-app'),
	data: {
		ds: [],
		value: "",
		placeholder: vm.tparams.parameters.placeholder,
		type: vm.tparams.parameters.type,
		datepickerOptions: {
			disabledDate: function disabledDate(time) {
				var d = new Date()
				d.setYear(d.getFullYear() - parseInt(vm.tparams.parameters.firstPickDate))
				return (vm.tparams.parameters.lastPickDateToday && time.getTime() > Date.now()) || time.getTime() < d.getTime()
			}
		}
	},
	computed: {
		qdates () {
			if (this.type === 'quarter') {
				var d = new Date()
				d.setYear(d.getFullYear() - parseInt(vm.tparams.parameters.firstPickDate))
				d.setMonth(d.getMonth()+3)
				var now = new Date()
				var startQs = [0,3,6,9]
				var month = d.getMonth()
				var firstQ = startQs.filter(function(q){
					return month >= q
				})
				firstQ =  firstQ[firstQ.length-1]
				var options = []
				for (var year = d.getFullYear();year <= now.getFullYear();year++) {
					for (var quarter = (year === d.getFullYear()?firstQ:0);quarter <= (now.getFullYear()===year?firstQ:11);quarter+=3) {
						var stepDate = new Date()
						stepDate.setYear(year)
						stepDate.setMonth(quarter)
						options.push({
							value: this.formatDate(stepDate, vm.tparams.parameters.filterformat),
							text: "Q" + (startQs.indexOf(quarter)+1) + "-" + year
						})
					}
				}
				return options
			} else {
				return  []
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
		handleChange: function (e) {
			if (vm.tparams.parameters.type == "daterange" || vm.tparams.parameters.filtertype == "daterange") {
				var initdate, enddate;
				if (e == null || e === "") {
					vm.sendFilter("date", null, "between")
				} else {
					switch (vm.tparams.parameters.type) {
						case "daterange":
							initdate = this.formatDate(e[0], vm.tparams.parameters.filterformat)
							enddate = this.formatDate(e[1], vm.tparams.parameters.filterformat)
							break;
						case "year":
							initdate = this.formatDate(e, vm.tparams.parameters.filterformat)
							//end date will be last day of year
							enddate = this.formatDate(new Date(e.getFullYear() + 1, 0, 0), vm.tparams.parameters.filterformat)
							break;
						case "month":
							initdate = this.formatDate(e, vm.tparams.parameters.filterformat)
							enddate = this.formatDate(new Date(e.getFullYear(), e.getMonth() + 1, 0), vm.tparams.parameters.filterformat)
							break;
						case "quarter":
							e = new Date(e)
							initdate = this.formatDate(new Date(e.getFullYear(), e.getMonth() , 1), vm.tparams.parameters.filterformat)
							enddate = this.formatDate(new Date(e.getFullYear(), e.getMonth() + 3, 0), vm.tparams.parameters.filterformat)
							break;
					}
					vm.sendFilter("date", "'" + initdate + "' and '" + enddate + "'", "between")
				}
			} else {
				if (this.type !== 'quarter') {
					if (e == null) {
						vm.sendFilter("date", null)
					} else {
						vm.sendFilter("date", "'" + this.formatDate(e, vm.tparams.parameters.filterformat) + "'")
					}
				} else {
					vm.sendFilter("date", "'" + initdate + "' and '" + enddate + "'", "between")
				}
			}

		},
		formatDate: function (date, format) {
			switch (format) {
				case "YYYY-MM-DDTHH:mm:ss.sssZ":
					return date.toISOString()
				case "YYYY-MM-DD":
					var day = date.getDate();
					var month = date.getMonth() + 1;
					var year = date.getFullYear();
					if (day < 10) {
						day = '0' + day;
					}
					if (month < 10) {
						month = '0' + month;
					}
					return year + "-" + month + "-" + day
				case "YYYY-MM":
					var month = date.getMonth() + 1;
					var year = date.getFullYear();
					if (month < 10) {
						month = '0' + month;
					}
					return year + "-" + month
				case "YYYY":
					var year = date.getFullYear();
					return year + ""
			}
		}
	}
}

//Init Vue app
vm.vueapp = new Vue(vm.vueconfig);
