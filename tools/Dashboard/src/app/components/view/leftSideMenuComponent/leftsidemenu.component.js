(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('leftsidemenu', {
      templateUrl: 'app/components/view/leftSideMenuComponent/leftsidemenu.html',
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
      children: [{
        id: 'Parentline',
        drag: false,
        type: 'line',
        image: '/controlpanel/static/images/dashboards/linechart.svg',
        label: 'Line',
        children: [{
          id: 'line',
          type: 'line',
          drag: true,
          new:true,
          image: '/controlpanel/static/images/dashboards/linechart.svg',
          label: 'New Line',
        }]
      }, {
        id: 'ParentBar',
        drag: false,
        image: '/controlpanel/static/images/dashboards/chart-bar.svg',
        label: 'Bar',
        type: 'bar',
        children: [{
          id: 'bar',
          type: 'bar',
          drag: true,
          new:true,
          image: '/controlpanel/static/images/dashboards/chart-bar.svg',
          label: 'New Bar',
        }]
      }, {
        id: 'ParentMixed',
        drag: false,
        image: '/controlpanel/static/images/dashboards/mixedchart.svg',
        label: 'Mixed',
        type: 'mixed',
        children: [{
          id: 'mixed',
          type: 'mixed',
          drag: true,
          image: '/controlpanel/static/images/dashboards/mixedchart.svg',
          new:true,
          label: 'New Mixed',
        }]
      }, {
        id: 'ParentPie',
        drag: false,
        image: '/controlpanel/static/images/dashboards/pie.svg',
        label: 'Pie Chart',
        type: 'pie',
        children: [{
          id: 'pie',
          type: 'pie',
          drag: true,
          image: '/controlpanel/static/images/dashboards/pie.svg',
          new: true,
          label: 'New Pie Chart',
        }]
      }, {
        id: 'ParentWordCloud',
        drag: false,
        image: '/controlpanel/static/images/dashboards/wordcloud.svg',
        label: 'Word Cloud Chart',
        type: 'wordcloud',
        children: [{
          id: 'wordcloud',
          type: 'wordcloud',
          drag: true,
          image: '/controlpanel/static/images/dashboards/wordcloud.svg',
          new:true,
          label: 'New Word Cloud Chart',
        }]
      }, {
        id: 'ParentMap',
        drag: false,
        image: '/controlpanel/static/images/dashboards/map.svg',
        label: 'Map',
        type: 'map',
        children: [{
          id: 'map',
          type: 'map',
          drag: true,
          image: '/controlpanel/static/images/dashboards/map.svg',
          new:true,
          label: 'New Map',
        }]
      }, {
        id: 'ParentRadar',
        drag: false,
        image: '/controlpanel/static/images/dashboards/radar1.svg',
        label: 'Radar',
        type: 'radar',
        children: [{
          id: 'radar',
          type: 'radar',
          drag: true,
          image: '/controlpanel/static/images/dashboards/radar1.svg',
          new:true,
          label: 'New Radar',
        }]
      }, {
        id: 'ParentTable',
        drag: false,
        image: '/controlpanel/static/images/dashboards/table.svg',
        label: 'Table',
        type: 'table',
        children: [{
          id: 'table',
          type: 'table',
          drag: true,
          image: '/controlpanel/static/images/dashboards/table.svg',
          new:true,
          label: 'New Table',
        }]
      }, {
        id: 'ParentDatadiscovery',
        drag: false,
        image: '/controlpanel/static/images/dashboards/datadiscovery.svg',
        label: 'Data Discovery',
        type: 'datadiscovery',
        children: [{
          id: 'datadiscovery',
          type: 'datadiscovery',
          drag: true,
          image: '/controlpanel/static/images/dashboards/datadiscovery.svg',
          new:true,
          label: 'New Data Discovery',
        }]
      }, {
        id: 'ParentFilter',
        drag: false,
        image: '/controlpanel/static/images/dashboards/filter.svg',
        label: 'Filter',
        type: 'gadgetfilter',
        children: [{
          id: 'gadgetfilter',
          type: 'gadgetfilter',
          drag: true,
          image: '/controlpanel/static/images/dashboards/filter.svg',
          new:true,
          label: 'New Filter',
        }]
      }]
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
        label: 'New Code'
      },{
        id: 'html5',
        type: 'html5',
        drag: true,
        image: '/controlpanel/static/images/dashboards/icon_live_html.svg',
        new:true,
        label: 'New HTML 5',
      }]
    }];

    vm.$onInit = function () {
      vm.estructure = JSON.parse(JSON.stringify(vm.initialEstructure));

      vm.vue = new Vue({
        el: '#leftsidemenu',

        data: function () {
          return {
            filterText: '',
            filterTextFavorite: '',
            activeName: 'first',
            data: vm.estructure,
            opendelay:1000,
            dataFavorite: [],
            defaultProps: {
              children: 'children',
              label: 'label'
            }

          }
        },
        watch: {
          filterText: function (val) {
            this.$refs.tree.filter(val);
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
            if (node.data.gid) {
              ev.dataTransfer.setData("gid", node.data.gid);
            }
            if (node.data.inLine) {
              ev.dataTransfer.setData("inLine", node.data.inLine);
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
            httpService.getUserGadgetsAndTemplates().then(
              function (dat) {
                if (typeof dat.data != 'undefined' && dat.data != null && dat.data.length > 0) {
                  //create custom entries
                  for (var i = 0; i < dat.data.length; i++) {
                    if (dat.data[i].isTemplate) {
                      var newEntry = {
                        id: dat.data[i].identification,
                        drag: false,
                        label: dat.data[i].identification,
                        type: dat.data[i].identification,
                        image: '/controlpanel/static/images/dashboards/templates.svg',
                        tooltip: dat.data[i].description,
                        children: [{
                          id: dat.data[i].identification,
                          type: 'customgadget',
                          drag: true,
                          inLine: false,
                          image: '/controlpanel/static/images/dashboards/templates.svg',
                          new:true,
                          label: 'New ' + dat.data[i].identification
                        }, {
                          id: dat.data[i].identification,
                          type: 'customgadget',
                          drag: true,
                          inLine: true,
                          image: '/controlpanel/static/images/dashboards/templates.svg',
                          new:true,
                          label: 'New Inline ' + dat.data[i].identification
                        }]
                      };
                      vm.estructure[1].children.push(newEntry);
                    }
                  }
                  //create instance entries
                  for (var i = 0; i < dat.data.length; i++) {
                    if (!dat.data[i].isTemplate && dat.data[i].typeElem !== 'favorite') {
                      var index = 0;
                      var typeElem = dat.data[i].type;
                      if (dat.data[i].typeElem !== 'predefined') {
                        index = 1;
                        typeElem = 'customgadget';
                      }
                      for (var j = 0; j < vm.estructure[index].children.length; j++) {
                        if (vm.estructure[index].children[j].type === dat.data[i].type) {
                          var newEntry = {
                            id: dat.data[i].identification,
                            gid: dat.data[i].id,
                            type: typeElem,
                            drag: true,
                            label: dat.data[i].identification,
                            image: vm.estructure[index].children[j].image,
                            tooltip: dat.data[i].description
                          }
                          vm.estructure[index].children[j].children.push(newEntry);
                          break;
                        }
                      }
                    }
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
          }

        },
        mounted: function () {

          this.loadData();
          window.addEventListener('newgadgetcreated', function (a) {
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