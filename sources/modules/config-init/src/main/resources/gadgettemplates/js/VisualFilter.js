// VISUAL FILTER GADGET
vm.vueconfig = {
    el: document.getElementById(vm.id).querySelector('vuetemplate .gadget-visualFilter'),
    data: {
        ds: [],
        multi: vm.params.parameters.main.multiple,
        theme: vm.params.parameters.main.theme,
        align: vm.params.parameters.main.align, // left,center,right
        position: vm.params.parameters.main.position, // top,middle,bottom -> baseline, center, end
        label: vm.params.parameters.main.title,     
        transparent: vm.params.parameters.main.transparent,
        field: vm.params.parameters.main.field,     
        op: vm.params.parameters.main.op,
        type: vm.params.parameters.main.type,
        selected: [],
        loading: true,
        loadingText: vm.params.parameters.main.loadingtext
    },
    computed: {
        title () {
            return this.label
        },
        setPosition() {
            return { justifyContent: this.align, placeItems: this.position }
        }
    },
    watch: {
        loading: {
            handler(newState,oldState) {                
                setTimeout(() => { this.applyPillSelected() },1000)
            },          
            immediate: false
        }
    },
    methods: {
        drawVueComponent: function (newData, oldData) {
            //This will be call on new data
            this.ds = newData ? newData.length > 0 ? newData.map(x => x[this.field]) : [] : []
            this.loading = false            
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
        selectPill(option, event) {
            
            var alreadySelected = event.target.classList.contains("pill-selected")

            // multiple or single selection
            if (this.multi) {
                alreadySelected ? event.target.classList.remove("pill-selected") : event.target.classList.add("pill-selected")
            } else {
                document.querySelectorAll(".pill-selected").forEach((p) => {
                    p.classList.remove("pill-selected")
                })
                alreadySelected ? event.target.classList.remove("pill-selected") : event.target.classList.add("pill-selected")
            }
            this.applyPillSelected()
        },
        applyPillSelected() {
            var that = this
            const pills = [
                { id: 'light', background: '#37a2da', color: '#fff' },
                { id: 'dark', background: 'darkgoldenrod', color: '#fff' },
                { id: 'vintage', background: '#d87c7c', color: 'white' },
                { id: 'transparent', background: '#37a2da', color: '#fff' },
                { id: 'base', background: '#dddddd', color: '#333' }
            ]
            // apply pill selected
            document.querySelectorAll(".pill-selected").forEach((p) => {
                p.style.backgroundColor = pills.filter(x => x.id === that.theme)[0].background
                p.style.color = pills.filter(x => x.id === that.theme)[0].color
                console.log('ELEMENT selected: ', p.textContent,' or dataset: ', p.dataset.value)
                if (!that.selected.includes(p.textContent)) { that.selected.push(p.textContent) }
            })
            // remove style from not selected
            document.querySelectorAll(".pill:not(.pill-selected)").forEach((p) => {
                p.style.backgroundColor = pills.filter(x => x.id === 'base')[0].background
                p.style.color = pills.filter(x => x.id === 'base')[0].color
                if (that.selected.includes(p.textContent)) { that.selected.splice(that.selected.indexOf(p.textContent), 1) }
            })
            // send filter
            this.sendFilter()
        },
        sendFilter() {
            console.log('sending filter... on ', this.selected)

            var getValue = (selected, op) => {
                var result = undefined
                switch (op) {
                    case '=':
                        result = selected.length > 0 ? selected[0] : null
                        break
                    case 'IN':
                        if (selected.length === 0) {
                            result = null
                        } else {
                            value = this.type === 'string' ? selected.map(x => "'" + x + "'").join(',') : selected.join(',')
                            result = '(' + value + ')'
                        }
                        break
                }
                return result
            }
            var operator = this.multi ? 'IN' : this.op
            var value = getValue(this.selected, operator)
            // sending filter
            console.log('vm.sendFilter(', this.field, ',', value, ',', operator + ')')
            vm.sendFilter(this.field, value, this.op)
        },
        applyTheme(themeColor) {
            console.log('applying theme ', themeColor)
            const light = { background: 'white', color: '#333' }
            const dark = { background: '#100c2a', color: 'white' }
            const vintage = { background: '#fef8ef', color: '#333' }
            const transparent = { background: 'transparent', color: '#333' }

            // ELEMENTS: TOOGLE, GADGET (OPTIONAL HEADER), CONTAINER, GRIDSTER-ITEM
            var gadget = this.$el
            var container = gadget.closest('div.element-container')
            var containerHeader = container.querySelector('div.widget-header')
            var containerTitle = containerHeader ? containerHeader.children[0] : null
            var vuetemplate = gadget.closest('vuetemplate')
            var gridster = gadget.closest('gridster-item')

            switch (themeColor) {
                case "vintage":
                    gadget.style.backgroundColor = vintage.background
                    container.style.backgroundColor = vintage.background
                    if (containerHeader) {
                        containerHeader.style.backgroundColor = vintage.background
                        containerTitle.style.color = vintage.color
                    }
                    vuetemplate.style.backgroundColor = vintage.background
                    gridster.style.backgroundColor = vintage.background
                    break;
                case "dark":
                    gadget.style.backgroundColor = dark.background
                    container.style.backgroundColor = dark.background
                    if (containerHeader) {
                        containerHeader.style.backgroundColor = dark.background
                        containerTitle.style.color = dark.color
                    }
                    vuetemplate.style.backgroundColor = dark.background
                    gridster.style.backgroundColor = dark.background
                    break;
                case "light":
                    gadget.style.backgroundColor = light.background
                    container.style.backgroundColor = light.background
                    if (containerHeader) {
                        containerHeader.style.backgroundColor = light.background
                        containerTitle.style.color = light.color
                    }
                    vuetemplate.style.backgroundColor = light.background
                    gridster.style.backgroundColor = light.background
                    break;
                case "transparent":
                    gadget.style.backgroundColor = transparent.background
                    container.style.backgroundColor = transparent.background
                    if (containerHeader) {
                        containerHeader.style.backgroundColor = transparent.background
                        containerTitle.style.color = transparent.color
                    }
                    vuetemplate.style.backgroundColor = transparent.background
                    gridster.style.backgroundColor = transparent.background
                    break;
                default:
                    console.log('This theme is not available')
            }
        }
    },  
    mounted() {
        console.log('PARAMS: ', vm.params)
        var gadget = this.$el
        var gridster = gadget.closest('gridster-item')
        if (this.transparent) { gridster.classList.toggle('no-shadow') }
        // apply theme and selected filter pills
        this.applyTheme(this.theme)
    }
}

//Init Vue app
vm.vueapp = new Vue(vm.vueconfig);