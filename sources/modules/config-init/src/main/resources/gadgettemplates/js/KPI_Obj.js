//Write your Vue JSON controller code here

//Focus here and F11 to full screen editor

//This function will be call once to init components

vm.vueconfig = {
	el: document.getElementById(vm.id).querySelector('vuetemplate  .gadget-app'),
	data:{
		ds:[],
		title: vm.tparams.parameters.title,
		objText: vm.tparams.parameters.objText,
		showObj: vm.tparams.parameters.showObj,
		postText: vm.tparams.parameters.postText,
		alignH: vm.tparams.parameters.alignH,
		alignV: vm.tparams.parameters.alignV,
		textAlign: vm.tparams.parameters.textAlign
	},
	computed: {
		real() {
			if (this.ds.length == 0) {
				return "";
			} else {
				return vm.utils.findValues(this.ds[0], vm.tparams.parameters.valueField)
			}
		},
		color() {
			if (this.obj == this.real) {
				return vm.tparams.parameters.colorEqualObj
			} else if ((this.obj > this.real && !vm.tparams.parameters.swapColor) || (this.obj < this.real && vm.tparams.parameters.swapColor)) {
				return vm.tparams.parameters.colorLessObj
			} else {
				return vm.tparams.parameters.colorMoreObj
			}
		},
		obj() {
			if (this.ds.length == 0) {
				return "";
			} else {
				return vm.utils.findValues(this.ds[0], vm.tparams.parameters.objField)
			}
		}
	},
	methods:{
		drawVueComponent: function(newData,oldData){
			//This will be call on new data
		},
		resizeEvent: function(){
			//Resize event
		},
		destroyVueComponent: function(){
			vm.vueapp.$destroy();
		},
		receiveValue: function(data){
			//data received from datalink
		},
		sendValue: vm.sendValue,
		sendFilter: vm.sendFilter
	}
}

//Init Vue app
vm.vueapp = new Vue(vm.vueconfig);
