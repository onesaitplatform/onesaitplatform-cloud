(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('leftsidemenu', {
      templateUrl: 'app/components/edit/leftSideMenuComponent/leftsidemenu.html',
      controller: LeftSideMenuController,
      controllerAs: 'vm',
      bindings: {

        config: "=?"
      }
    });

  /** @ngInject */
  function LeftSideMenuController($scope, httpService) {
    var vm = this;

    vm.initialEstructure = [{
      id: 'Predefined',
      drag: false,
      label: 'Predefined',
      type: 'predefined',
      children: []
    }, {
      id: 'Custom',
      drag: false,
      label: 'Custom',
      type: 'customgadget',
      children: []
    }, {
      id: 'Code',
      drag: false,
      label: 'Code',
      children: [{
        id: 'livehtml',
        type: 'livehtml',
        drag: true,
        image: '/controlpanel/static/images/dashboards/script.svg',
        new:true,
        label: 'New Code',
        desc: 'Create a new gadget with html/css and javascript code'
      },{
        id: 'html5',
        type: 'html5',
        drag: true,
        image: '/controlpanel/static/images/dashboards/icon_live_html.svg',
        new:true,
        label: 'New HTML 5',
        desc: 'Create a new gadget with iframe content code. Everything inside is wrapper in an iframe'
      }]
    }];

    vm.$onInit = function () {
      vm.estructure = JSON.parse(JSON.stringify(vm.initialEstructure));
      vm.estructurePrebuild = JSON.parse(JSON.stringify(vm.initialEstructure));
      vm.estructurePrebuild = vm.estructurePrebuild.filter(
        function(node){
          return node.id !== 'Code'
        }
      );
      for (var icat in vm.estructurePrebuild) {
        for (var itype in vm.estructurePrebuild[icat].children) {
          vm.estructurePrebuild[icat].children[itype].children = []
        }
      }

      vm.vue = new Vue({
        el: '#leftsidemenu',

        data: function () {
          return {
            filterText: '',
            filterTextPrebuild: '',
            filterTextFavorite: '',
            activeName: 'first',
            data: [],
            dataPrebuild: [],
            opendelay:400,
            dataFavorite: [],
            defaultProps: {
              children: 'children',
              label: 'label'
            },
            newgadget: false //control if drag element is Prebuild or no for reload data

          }
        },
        watch: {
          filterText: function (val) {
            this.$refs.tree.filter(val);
          },
          filterTextPrebuild: function (val) {
            this.$refs.treePrebuild.filter(val);
          },
          filterTextFavorite: function (val) {
            this.$refs.treeFavorite.filter(val);
          }
        },
        methods: {
          filterNode: function (value, data) {
            if (!value) return true;
            return data.label.toLowerCase().indexOf(value.toLowerCase()) !== -1;
          },
          handleDragStart: function (node, ev) {

            ev.dataTransfer.setData("type", node.data.type);
            ev.dataTransfer.setData("customType", node.data.id);
            ev.dataTransfer.setData("title", node.data.label);
            if (node.data.gid) {
              ev.dataTransfer.setData("gid", node.data.gid);
            }
            if (node.data.inLine) {
              ev.dataTransfer.setData("inLine", node.data.inLine);
            }
          },
          handleDragStartGrid: function (node, ev) {
            if (node && node.children && node.children.length > 0) {
              node = node.children[0]
            } 

            ev.dataTransfer.setData("type", node.type);
            ev.dataTransfer.setData("customType", node.id);
            ev.dataTransfer.setData("title", node.label);
            if (node.gid) {
              ev.dataTransfer.setData("gid", node.gid);
            }
            if (node.inLine) {
              ev.dataTransfer.setData("inLine", node.inLine);
            }
          },
          allowDrop: function (draggingNode, dropNode, type) {
            return false;
          },
          allowDrag: function (draggingNode) {
            return draggingNode.data.drag;

          },
          loadData: function () {
            
            var that = this;    
                 
            vm.estructure = JSON.parse(JSON.stringify(vm.initialEstructure));
            vm.estructurePrebuild = JSON.parse(JSON.stringify(vm.initialEstructure));
            vm.estructurePrebuild = vm.estructurePrebuild.filter(
              function (node) {
                return node.id !== 'Code'
              }
            )
            for (var icat in vm.estructurePrebuild) {
              for (var itype in vm.estructurePrebuild[icat].children) {
                vm.estructurePrebuild[icat].children[itype].children = []
              }
            }
            httpService.getUserGadgetsAndTemplates().then(
              function (dat) {
                if (typeof dat.data != 'undefined' && dat.data != null && dat.data.length > 0) {
                  //create custom entries
                  for (var i = 0; i < dat.data.length; i++) {
                    if (dat.data[i].isTemplate) {
                      dat.data[i].config = JSON.parse(dat.data[i].config);
                      if (dat.data[i].image) {
                        var raw = window.atob(dat.data[i].image);
                        var prefiximg;
                        if (raw.startsWith("<svg")) {
                          prefiximg = "data:image/svg+xml;base64,"
                        } else if (raw.startsWith("RIFF")) {
                          prefiximg = "data:image/webp;base64,";
                        } else {
                          prefiximg = "data:image/png;base64,";
                        }
                        dat.data[i].image = prefiximg + dat.data[i].image
                      } else {
                        dat.data[i].image = '/controlpanel/static/images/dashboards/templates.svg'
                      }

                      var newEntry = {
                        id: dat.data[i].identification,
                        drag: false,
                        label: that.renameDefaultTemplates(dat.data[i].identification),
                        type: dat.data[i].identification,
                        image: dat.data[i].image,
                        desc: dat.data[i].description,
                        order: dat.data[i].config && dat.data[i].config.metainf && dat.data[i].config.metainf.order?dat.data[i].config.metainf.order:99999,
                        children: [{
                          id: dat.data[i].identification,
                          type: dat.data[i].type==='base'?dat.data[i].id:'customgadget',
                          drag: true,
                          inLine: !(dat.data[i].type==='base'),
                          image: dat.data[i].image,
                          new:true,
                          label: 'New ' + (!(dat.data[i].type==='base')?'inline ':'') + that.renameDefaultTemplates(dat.data[i].identification),
                          desc: dat.data[i].description
                        }]
                      };
                      var index =  (dat.data[i].config &&  dat.data[i].config.metainf && dat.data[i].config.metainf.category ==="Predefined")?0:1;
                      vm.estructure[index].children.push(newEntry);
                      var newEntryPrebuild = JSON.parse(JSON.stringify(newEntry));
                      newEntryPrebuild.children = []
                      vm.estructurePrebuild[index].children.push(newEntryPrebuild);
                    }
                  }
                  //create instance entries
                  for (var i = 0; i < dat.data.length; i++) {
                    if (!dat.data[i].isTemplate && dat.data[i].typeElem !== 'favorite') {
                      var typeElem = dat.data[i].type;
                      if (dat.data[i].typeElem !== 'predefined') {
                        typeElem = 'customgadget';
                      }
                      for (var index = 0; index < vm.estructurePrebuild.length; index++) {
                        for (var j = 0; j < vm.estructurePrebuild[index].children.length; j++) {
                          if (vm.estructurePrebuild[index].children[j].type === dat.data[i].type) {
                            var newEntry = {
                              id: dat.data[i].identification,
                              gid: dat.data[i].id,
                              type: typeElem,
                              drag: true,
                              label: dat.data[i].identification,
                              image: vm.estructure[index].children[j].image,
                              tooltip: dat.data[i].description
                            }
                            vm.estructurePrebuild[index].children[j].children.push(newEntry);
                            break;
                          }
                        }
                      }
                    }
                  }
                  //Clear empty types
                  for (var i = 0; i < vm.estructurePrebuild.length; i++) {
                    vm.estructurePrebuild[i].children = vm.estructurePrebuild[i].children.filter(
                      function(node){
                        return node.children.length > 0
                      }
                    )
                  }
                  //create favorite entries
                  that.dataFavorite = [];
                  for (var i = 0; i < dat.data.length; i++) {
                    if (!dat.data[i].isTemplate && dat.data[i].typeElem === 'favorite') {
                      var newEntry = {
                        id: dat.data[i].identification,
                        gid: dat.data[i].identification,
                        type: 'favoritegadget',
                        drag: true,
                        label: dat.data[i].identification,
                        image: '/controlpanel/static/images/dashboards/icon_star.svg',
                        tooltip: ''
                      }
                      that.dataFavorite.push(newEntry);
                    }
                  }
                }
                that.data = JSON.parse(JSON.stringify(vm.estructure));
                that.dataPrebuild = JSON.parse(JSON.stringify(vm.estructurePrebuild));
                //Sort
                function compare( a, b ) {
                  return a.order - b.order;
                }

                that.data[0].children = that.data[0].children.sort(compare);
                that.data[1].children = that.data[1].children.sort(compare);

                that.dataPrebuild[0].children = that.dataPrebuild[0].children.sort(compare);
                that.dataPrebuild[1].children = that.dataPrebuild[1].children.sort(compare);
              }
            ).catch(function (error) {
              console.error('Can not load gadget: ', error)
            });
          },
          handleClick: function (tab, event) {
            console.log(tab, event);
          },
          hideLeftSideMenu:function(){
            $.find(".menusidebardashboard")[0].style.width = "0";
            $.find(".dashboardcontent")[0].style.marginLeft = "0";
            $.find("gridster")[0].style.zIndex
            $("gridster").css("z-index", "");
          },
          renameDefaultTemplates: function (identification) {
            var trStt = {
              "gadget-crud": "CRUD Entity",
              "ods-gadget-crud": "CRUD Entity (ODS)",
              "gadget-import": "Table from File (ODS)",
              "ods-gadget-import": "Table from File",
              "VueEchartMixed": "Mixed (EChartsJS)",
              "Vue ODS Select": "Dropdown (ODS)",
              "VueEchartLineorBar": "Line/Bar (EChartsJS)",
              "ReactMaterialList": "List (Material React)"
            }
            if (identification in trStt) {
              return trStt[identification];
            } else {
              return identification;
            }
          }

        },
        mounted: function () {

          this.loadData();
          window.addEventListener('newprebuildgadgetcreated', function (a) {
            vm.vue.loadData()
          }, false);
          window.addEventListener('addFavorite', function (a) {
            vm.vue.loadData()
          }, false);
        }
      })

    };


  }
})();