(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('rightsidemenu', {
      templateUrl: 'app/components/edit/rightSideMenuComponent/rightsidemenu.html',
      controller: RightSideMenuController,
      controllerAs: 'vm',
      bindings: {

      }
    });

  /** @ngInject */
  function RightSideMenuController($scope, httpService, $window) {
    var vm = this;



    vm.$onInit = function () {

      vm.vue = new Vue({
        el: '#rightsidemenu',

        data: function () {
          return {
            filterText: '',
            filterTextFavorite: '',
            activeName: 'first',
            data: vm.estructure,
            opendelay: 1000,
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

          loadData: function () {},
          handleClick: function (tab, event) {
            console.log(tab, event);
          },          
          showMenurightsidebardashboard: function () {
            $.find(".menurightsidebardashboard")[0].style.width = "400";
            $.find(".dashboardcontent")[0].style.marginRight = "400";
            $("gridster").css("z-index", "1");
            $window.dispatchEvent(new Event("resize"));
          },
          hideMenurightsidebardashboard: function () {
            $.find(".menurightsidebardashboard")[0].style.width = "0";
            $.find(".dashboardcontent")[0].style.marginRight = "0";
            $("gridster").css("z-index", "");
            $window.dispatchEvent(new Event("resize"));
          }

        },
        mounted: function () {

          window.addEventListener('hideMenurightsidebardashboard', function (a) {
            vm.vue.hideMenurightsidebardashboard()
          }, false);
          window.addEventListener('showMenurightsidebardashboard', function (a) {
            vm.vue.showMenurightsidebardashboard()
          }, false);

        }
      })

    };


  }
})();