ELEMENT.locale(ELEMENT.lang.en);

Vue.component('option-compositor', {
  props: {
    value: {
      type: "array"
    }
  },
  data() { 
    return {
      localValue: this.value,
      inputValue: '',
      inputText: ''
    }
  },
  watch: {
    localValue (newValue) {
      this.$emit('input', newValue)
    },
    value (newValue) {
      this.localValue = newValue
    }
  },
  methods: {
    remove: function(index){
      this.localValue.splice(index, 1);
    },
    addOption: function(){
      let inputValue = this.inputValue;
      let inputText = this.inputText;
      if (inputValue) {
        this.localValue.push({
          value: inputValue,
          text: inputText
        });
      }
      this.inputValue = '';
      this.inputText = '';
    }
  },
  mounted() {
    if (!this.value) {
      this.localValue = []
    }
  },
  template: `
    <span>
      <el-form-item>
        <el-input placeholder='Option value' v-model="inputValue"></el-input>
      </el-form-item>
      <el-form-item>
        <el-input placeholder='Option text' v-model="inputText"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button class="button-new-tag" @click="addOption">+</el-button>
      </el-form-item>
      <el-form-item>
        <el-tag :key="tag" v-for="(tag, index) in value" closable :disable-transitions="false" @close="remove(index)">
          <span v-if="tag.text && tag.text.length > 0">{{tag.value + ' (' + tag.text + ')'}}</span>
          <span v-else>{{tag.value}}</span>
        </el-tag>
      </el-form-item>
    </span>
  `
});


Vue.component('nested-draggable', {
  props: {
    elements: {
      required: true,
      type: Array
    }
  },
  data() { 
    return {
		predefineColors: [
	        '#ff4500',
	        '#ff8c00',
	        '#ffd700',
	        '#90ee90',
	        '#00ced1',
	        '#1e90ff',
	        '#c71585',
	        'rgba(255, 69, 0, 0.68)',
	        'rgb(255, 120, 0)',
	        'hsv(51, 100, 98)',
	        'hsva(120, 40, 94, 0.5)',
	        'hsl(181, 100%, 37%)',
	        'hsla(209, 100%, 56%, 0.73)',
	        '#c7158577'
	    ]
	}
  },
  methods: {
    remove: function(list,index){
      list.splice(index, 1);
    },
	defaultToSelectType: function(element) { //for change between select and select multiple
		if (element.multiple && typeof element.default == "string") {
			element.default = [element.default]
		} else if (!element.multiple && typeof Array.isArray(element.default)) {
			if (element.default.length > 0) {
				element.default = element.default[0]; 
			} else {
				element.default = null
			}
		}
	}
  },
  template: `
  <draggable class="dragArea" :list="elements" group="elements">
    <div class="bottom-light-border" v-for="(element,index) in elements" :key="element.type">
      <el-row>
        <el-col :span="23">
          <el-row>
            <el-col :span="3">
              <b class='el-form-item__label'>
                {{ element.type }}
              </b>
            </el-col>
            <el-col :span="21">
              <el-form :inline="true" size="mini">
                <el-form-item label="Name">
                  <el-input placeholder="Name" type="text" v-model='element.name' ></el-input>
                </el-form-item>
                <el-form-item label="Title">
                  <el-input placeholder="Title" type="text" v-model='element.title' ></el-input>
                </el-form-item>
                  
                <span v-if="!element.elements">
                  <span v-if="element.type == 'input-text'"><!-- input text only -->
                    <el-form-item label="Default">
                      <el-input placeholder="Default" type="text" v-model='element.default' type='text' ></el-input>
                    </el-form-item>
                  </span>
                  <span v-if="element.type == 'input-number'"><!-- input number only -->
                    <el-form-item label="Min">
                      <el-input type="number" style="width:100px" placeholder="Min" v-model='element.min'/>
                    </el-form-item>
                    <el-form-item label="Max">
                      <el-input type="number" style="width:100px" placeholder="Max" v-model='element.max'/>
                    </el-form-item>
                    <el-form-item label="Default">
                      <el-input type="number" style="width:100px" placeholder="Default" v-model='element.default' type='number'></el-input>
                    </el-form-item>
                  </span>
                  <span v-if="element.type == 'selector'"><!-- selector only -->
                    <el-form-item label="Multiple">
                      <el-checkbox v-model='element.multiple' @change="defaultToSelectType(element)"></el-checkbox>
                    </el-form-item>
                    <el-form-item label="Default">
                      <el-select placeholder="Default" :multiple="element.multiple" v-model='element.default'>
                        <el-option v-if="!element.options || element.options.length == 0"></el-option>
                        <el-option v-for="item in element.options" :key="item.value" :label="item.text" :value="item.value"></el-option>
                      </el-select>
                    </el-form-item>
                    <el-form-item label="Options">
                      <option-compositor v-model='element.options'/>
                    </el-form-item>
                  </span>
                  <span v-if="element.type == 'checkbox'"><!-- checkbox only -->
                    <el-form-item label="Default">
                      <el-checkbox v-model='element.default'></el-checkbox>
                    </el-form-item>
                  </span>
				  <span v-if="element.type == 'color-picker'"><!-- color-picker only -->
                    <el-form-item label="Default">
					  <el-color-picker v-model='element.default' :predefine="predefineColors" show-alpha/>
                    </el-form-item>
                  </span>
			      <span v-if="element.type == 'autogenerate-id'"><!-- autogenerate-id only -->
                    <el-form-item label="Prefix">
					  <el-input placeholder="Default" type="text" v-model='element.prefix' type='text' ></el-input>
                    </el-form-item>
                  </span>
                  <span v-if="element.type == 'model-selector'"><!-- model-selector only -->
                    <el-form-item label="Path">
					  <el-input placeholder="Default" type="text" v-model='element.path' type='text' ></el-input>
                    </el-form-item>
                  </span>
                  <span v-if="element.type == 'ds-field'"><!-- ds-field only -->
                    <el-form-item label="Multiple">
                      <el-checkbox v-model='element.multiple'></el-checkbox>
                    </el-form-item>
                  </span>
                </span>
              </el-form>
            </el-col>
            <el-col>
              <div v-if="element.elements">
                <el-row>
                  <el-col :offset="1">
                    <nested-draggable style="border-left:1px solid rgb(221, 218, 218); min-height:100px;background-color:rgb(247, 247, 247)" v-if="element.elements" class="dragArea" :elements="element.elements"/>
                  </el-col>
                </el-row>
              </div>
            </el-col>
          </el-row>
        </el-col>
        <el-col :span="1">
          <el-button type="text" @click="remove(elements,index)"><img src="/controlpanel/static/images/dashboards/remove.svg" class="ng-scope"></el-button>
        </el-col>
      </el-row>
    </div>
  </draggable>`
})

Vue.component('section-array-drawer', {
  props: {
    elements: {
      required: true,
      type: Object
    },
    cdsfields: {
      required: false,
      type: Array
    },
    showDatasource: {
      required: true,
      type: Boolean,
      default: true
    },
    value: {
      type: Object,
	  default: []
    },
	fullmodelin: { // for model access on other elements
	  required: false,
	  type: Object
	}
  },
  data() { 
    return {
      localvalue: [],
    }
  },
  mounted () {
	this.localvalue = this.value;
  },
  watch: {
    localvalue (newValue) {
	  if (JSON.stringify(newValue) !== JSON.stringify(this.value) && (Object.keys(newValue).length == 0 && this.value && Object.keys(this.value).length == 0)) {
	  	this.$emit('input', newValue)
	  }
      this.$forceUpdate()
    },
    value (newValue) {
		this.localvalue = newValue
	},
	fullmodelin (newValue) {
		this.$forceUpdate();
	}
  },
  methods: {
	remove: function(index){
      this.localvalue.splice(index, 1);
      this.$root.$emit('force-render');
	  this.$forceUpdate();
    },
    add: function(){
      this.localvalue.push({});
	  this.$root.$emit('force-render');
      this.$forceUpdate();
    }
  },
  template: `
	<div>
		<draggable class="dragArea" :list="this.localvalue" :group="{ name: 'section-array', pull: false, put: false }">
			<el-row style="border-bottom: 1px solid rgb(236 236 236);" v-for="(arrayitem,index) in value">
	        	<el-col :span="23">
					<gform-drawer :fullmodelin="fullmodelin" :index="index" :showDatasource="false" :cdsfields="cdsfields" :elements="elements" v-model="arrayitem"></gform-drawer>
				</el-col>
				<el-col :span="1">
					<el-button type="text" @click="remove(index)"><img src="/controlpanel/static/images/dashboards/remove.svg" class="ng-scope"></el-button>
				</el-col>
			</el-row>
		</draggable>
		<el-row>
			<el-col :offset="23":span="1">
				<el-button type="text" @click="add()"><i style=" color: #606266;" class="el-icon-plus"></i></el-button>
			</el-col>
		</el-row>
	</div>
  `
});

Vue.component('gform-drawer', {
  props: {
    elements: {
      required: true,
      type: Object
    },
    cdsfields: {
      required: false,
      type: Array
    },
    showDatasource: {
      required: true,
      type: Boolean,
      default: true
    },
    value: {
      type: Object,
	  default: {}
    },
	index: { // for autoincrement
	  required: false,
	  type: Number,
	  default: 0
	},
	fullmodelin: { // for model access on other elements
	  required: false,
	  type: Object
	}
  },
  data() { 
    return {
      localvalue: {},
      localdatasource: {},
      predefineColors: [
        '#ff4500',
        '#ff8c00',
        '#ffd700',
        '#90ee90',
        '#00ced1',
        '#1e90ff',
        '#c71585',
        'rgba(255, 69, 0, 0.68)',
        'rgb(255, 120, 0)',
        'hsv(51, 100, 98)',
        'hsva(120, 40, 94, 0.5)',
        'hsl(181, 100%, 37%)',
        'hsla(209, 100%, 56%, 0.73)',
        '#c7158577'
      ],
      datasources: [],
      datasource: "",
      dsfields: (this.cdsfields ? this.cdsfields : []),
      hasDSField: null,
	  fullmodel: this.fullmodelin ? this.fullmodelin : {}
    }
  },
  computed: {
    
  },
  beforeMount() {
  },
  mounted () {
	var that = this;
	this.$root.$on('force-render', function(){
		that.$forceUpdate()
	})
	if (this.value) {
		this.localdatasource = this.value.datasource;	
	}
	if (this.showDatasource) {
	  this.fullmodel = this.value
	}
    var localvalueAux = this.getLocalValue(this.value);
    if (Array.isArray(localvalueAux)) {
      this.localvalue = this.legacyToNewParams(localvalueAux);
      this.$emit('input', this.getRealValue())
    } else {
      this.localvalue = localvalueAux
    }
    this.hasDSFields();
    this.getDatasources();
    if (localvalueAux && Object.keys(localvalueAux).length > 0) {
      this.localvalue = localvalueAux
      if (this.value && this.value.datasource) {
        this.datasource = this.value.datasource.id
        this.getDatasourceFields()
      } else {
        console.log("No datasource found loading gadget")
      }
    } else {
      this.setDefaults();
    }
    this.$emit('input', this.getRealValue())
  },
  watch: {
    localvalue (newValue) {
	  var localdata = JSON.stringify(this.getRealValue());
      if (localdata !== JSON.stringify(this.value) && (Object.keys(this.getRealValue()).length == 0 && this.value && Object.keys(this.value).length == 0)) {
        this.$emit('input', this.getRealValue())
      }
      this.$forceUpdate()
    },
    value (newValue) {
      this.localdatasource = this.value.datasource;
      newValue = this.getLocalValue(newValue);
      if (Array.isArray(newValue)) {
        this.localvalue = this.legacyToNewParams(newValue);
      } else {
        if (JSON.stringify(this.localvalue) !== JSON.stringify(newValue)) {
          this.localvalue = newValue
        };
      }
	  if (this.showDatasource) {
	    this.fullmodel = newValue
	  }
      this.$forceUpdate()
    },
    cdsfields (newValue) {
      this.dsfields = newValue
    },
    elements (newValue) {
      this.hasDSFields();
      this.setDefaults();
      this.$emit('input', this.getRealValue())
      this.getDatasources();
    },
	fullmodelin (newValue) {
		this.$forceUpdate();
	}
  },
  methods: {
    getLocalValue: function(value) {
      if (this.showDatasource) {
        return value['parameters'] ? value['parameters'] : {};
      } else {
        return value;
      }
    },
    getRealValue: function() {
      if (this.showDatasource) {
        return {
          parameters: this.localvalue,
          datasource: this.localdatasource
        }
      } else {
        return this.localvalue;
      }
    },
    legacyToNewParams: function (legacyParams) {
      var nparams = {}
      for (var i = 0; i < legacyParams.length ; i++) {
        var param = legacyParams[i];
        nparams[param.label] = typeof param.value === 'object' && param.value !== null ? param.value.field : param.value;
      }
      nparams["datasource"] = legacyParams.datasource;
      return nparams;
    },
    getDatasources: function () {
      if (this.showDatasource && this.hasDSField) {
        var that = this;
        fetch('/controlpanel/datasources/getUserGadgetDatasources')
          .then(function(response){return response.json()}).then(function(response) {that.datasources = response})
      }
    },
    getDatasourceFields: function () {
      var that = this;
      if (this.datasource) {
        fetch("/controlpanel/datasources/getFields/" + this.datasource)
          .then(function(response){return response.json()}).then(function(response) {that.dsfields = response.map(function(d){return d.name})})
      }
    },
    hasDSFields: function() {
      function hasDSField(elements) {
        for (element in elements) {
          if (elements[element].elements && elements[element].elements.length > 0) {
            if (hasDSField(elements[element].elements)) {
              return true;
            }
          } else {
            if (elements[element].type === 'ds-field' || elements[element].type === 'ds-field(ds[0].)') {
              return true;
            }
          }
        }
        return false;
      }
      this.hasDSField = hasDSField(this.elements);
    },
    setDefaults: function() {
	  var that = this;
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
			if (elements[element].type == 'autogenerate-id') {
				localvalue[elements[element].name] = that.index >= 0 ? elements[element].prefix + that.index : elements[element].prefix + 0
			} else {
				localvalue[elements[element].name] = JSON.parse(JSON.stringify(elements[element].default == undefined ? null : elements[element].default))
			}
          }
        }
      }
      setDefault(this.elements, this.localvalue);
    },
    remove: function(list,index){
      list.splice(index, 1);
    },
    updateElem: function(key, value) {
      this.localvalue[key]=value;
      this.$emit('input', this.getRealValue())
      this.$forceUpdate()
    },
    updateDSFields() {
      var that = this;
      var dt = this.datasources.filter(function(d){return that.datasource == d.id})[0]
      this.localdatasource = {
        id: dt.id,
        type: "query",
        refresh: dt.refresh,
        name: dt.identification
      }
      this.$emit('input', this.getRealValue())
      this.getDatasourceFields()
    },
	generateValuesFromModelPath(path) {
		var splitedpath = path.split(".");
		var i = 0;
		var auxmodel = this.fullmodel;
		while (i < splitedpath.length) {
			var subpath = splitedpath[i];
			if (subpath == "*") {
				return auxmodel.map(function (data) {
					var auxdata = data;
					for (j = i+1 ; j < splitedpath.length ; j++) {
						auxdata = auxdata[splitedpath[j]];
					}
					return auxdata
				})
			} else {
				auxmodel = auxmodel[subpath];
			}
			i++
		}
		return [auxmodel];
	}
  },
  template: `
  <el-collapse   >
    <el-form :inline="true" size="mini">
     <el-collapse-item title="Select Datasource"  v-if="showDatasource && hasDSField" >
      <el-form-item style="width: 100%;" >
        <el-select v-model="datasource" @change="updateDSFields()"  style="width: 100%;">
          <el-option v-for="item in datasources" :key="item.id" :label="item.identification" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
       </el-collapse-item>
      <span v-for="(element,index) in elements">
        <el-form-item v-if="!(element.elements)" :label="element.title ? element.title : element.name" style="width: 100%;">
		  <el-input v-if="element.type == 'autogenerate-id'" :value="localvalue[element.name]" :disabled="true" style="width: 100%;"/>
          <el-input v-if="element.type == 'input-text'" :value="localvalue[element.name]" @input="updateElem(element.name,$event)" style="width: 100%;"/>
          <el-input type="number" style="width:183px" v-if="element.type == 'input-number'" :min="element.min" :max="element.max" precision="5" :value="localvalue[element.name]" @input="updateElem(element.name,$event)" style="width: 100%;"/>
          <el-checkbox v-if="element.type == 'checkbox'" :value="localvalue[element.name]" @input="updateElem(element.name,$event)"/>
          <el-select v-if="element.type == 'selector'" :multiple="element.multiple" :value="localvalue[element.name]" @input="updateElem(element.name,$event)" style="width: 100%;">
            <el-option v-for="item in element.options" :key="item.value" :label="item.text" :value="item.value"></el-option>
          </el-select>
		  <el-select v-if="element.type == 'model-selector'" :multiple="element.multiple" :value="localvalue[element.name]" @input="updateElem(element.name,$event)" style="width: 100%;">
            <el-option v-for="item in generateValuesFromModelPath(element.path)" :key="item" :label="item" :value="item"></el-option>
          </el-select>
          <el-color-picker :predefine="predefineColors" style="width:183px" v-if="element.type == 'color-picker'" show-alpha :value="localvalue[element.name]" @input="updateElem(element.name,$event)" style="width: 100%;"/>
          <el-select :placeholder="dsfields.length == 0 ? 'Select Datasource First' : 'Select Field'" v-if="element.type == 'ds-field' || element.type == 'ds-field(ds[0].)'" :multiple="element.multiple" :value="localvalue[element.name]" @input="element.type == 'ds-field' ? updateElem(element.name,$event) : updateElem(element.name,'ds[0].' + $event)" style="width: 100%;">
            <el-option key="null" label="No field" :value="null"></el-option>
            <el-option v-for="item in dsfields" :key="item" :label="item" :value="item"></el-option>
          </el-select>
        </el-form-item>
        <span  v-if="element.elements">
         <!-- <h5 v-if="showDatasource" class="section">{{element.title ? element.title : element.name}}</h5>
		  <h6 v-if="!showDatasource" class="sub-section">{{element.title ? element.title : element.name}}</h6>-->
		   <el-collapse-item :title="element.title ? element.title : element.name"   >
		    
		  <div>
          	<gform-drawer :index="index" :fullmodelin="fullmodel" v-if="element.type == 'section'" :showDatasource="false" :cdsfields="dsfields" :elements="element.elements" v-model="localvalue[element.name]"></gform-drawer>
			<section-array-drawer :fullmodelin="fullmodel" v-if="element.type == 'section-array'" :showDatasource="false" :cdsfields="dsfields" :elements="element.elements" v-model="localvalue[element.name]"></gform-drawer>
		  </div>
		  </el-collapse-item>
        </span>
      </span>
    </el-form>
    </el-collapse>
  `
});
