//Write your Vue ODS JSON controller code here

//Focus here and F11 or F10 to full screen editor

//This function will be call once to init components

if (vm.tparams.parameters.groupAndSort) {
  if (vm.tparams.parameters.KeySelect === vm.tparams.parameters.ValueSelect) {
      vm.buildDSTransform()
          .group(vm.tparams.parameters.KeySelect)
          .sort(vm.tparams.parameters.KeySelect)
          .apply()
  } else {
      vm.buildDSTransform()
          .group(vm.tparams.parameters.KeySelect)
          .group(vm.tparams.parameters.ValueSelect)
          .sort(vm.tparams.parameters.ValueSelect)
          .apply()
  }
}

vm.vueconfig = {
  el: document.getElementById(vm.id).querySelector("vuetemplate"),
  data: {
      ds: [{
          value: "Option1",
          label: "Option1"
      }],
      value: "",
      key: vm.tparams.parameters.KeySelect,
      label: vm.tparams.parameters.ValueSelect,
      select: vm.tparams.parameters.PlaceHolder,
      clearable: vm.tparams.parameters.Clearable,
      multiple: vm.tparams.parameters.multiple,
      filterable: vm.tparams.parameters.filterable
  },
  methods: {
      drawVueComponent: function (newData, oldData) {
          this.value = ""
          vm.sendFilter(this.key, null)
      },
      resizeEvent: function () {
          //Resize event
      },
      destroyVueComponent: function () {

      },
      receiveValue: function (data) {
          //data received from datalink
      },
      sendValue: vm.sendValue,
      sendFilter: function (key, value) {
          var op;
          if (vm.tparams.parameters.multiple) {
              op = "in"
              if (!value || value.length == 0) {
                  value = null
              } else {
                  value = "('" + value.join("', '") + "')"
              }
          } else {
              op = "="
              if (value == "") {
                  value = null
              }
          }
          vm.sendFilter(key, value, op)
      },
      findValues: vm.utils.findValues
  }
}

//Init Vue app
vm.vueapp = new Vue(vm.vueconfig);
