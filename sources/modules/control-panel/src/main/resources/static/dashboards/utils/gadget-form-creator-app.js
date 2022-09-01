var vueapp = new Vue({
  el: '#gform',
  data: {
    list1: [
      { type: "input-text", id: 1, default: "" },
      { type: "input-number", id: 2, default: 1 },
      { type: "selector", id: 3 },
      { type: "checkbox", id: 4, default: false },
      { type: "color-picker", id: 5, default: "rgba(0, 0, 0, 1)" },
      { type: "ds-field", id: 6 },
      { type: "ds-field(ds[0].)", id: 7 },
      { type: "section", id: 8, elements: [] },
      { type: "section-array", id: 9, elements: [] },
      { type: "autogenerate-id", id: 10, "prefix": "#" },
      { type: "model-selector", id: 11 },
    ],
    list2: (window.gformconfig ?  window.gformconfig : []),
    gformvalue: (window.gformvalue ?  window.gformvalue : {})
  },
  methods: {
    cloneAll: function(element){
      var ident = new Uint32Array(1);
      window.crypto.getRandomValues(ident);
      if (element.elements) {
        return {
          id: element.id,
          type: element.type,
          elements: JSON.parse(JSON.stringify(element.elements)),
          name: "parameterName-" + ident
        };
      } else {
      	var clone = {
          id: element.id,
          type: element.type,
          name: "parameterName-" + ident
        }
        if (element.default !== undefined) {
        	clone['default'] = element.default
        }
        if (element.prefix !== undefined) {
        	clone['prefix'] = element.prefix
        } 
        return clone;
      }
      
    },
    remove: function(list,index){
      list.splice(index, 1);
    },
    getDefaultTParams() {
      function setDefault(elements, localvalue) {
        for (element in elements) {
          if (elements[element].elements && elements[element].elements.length > 0) {
			if (elements[element].type == 'section') {
            	localvalue[elements[element].name] = {}
				setDefault(elements[element].elements, localvalue[elements[element].name])
			} else if (elements[element].type == 'section-array') {
				localvalue[elements[element].name] = [{}]
				setDefault(elements[element].elements, localvalue[elements[element].name][0])
			}
          } else {
            localvalue[elements[element].name] = JSON.parse(JSON.stringify(elements[element].default == undefined ? null : elements[element].default))
          }
        }
      }
      var defaultTParams = {}
      setDefault(this.list2, defaultTParams);
      return defaultTParams;
    }
  },
  mounted: function () {
    

  }
});