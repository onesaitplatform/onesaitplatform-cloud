(function () {
    'use strict';
    angular.module('dashboardFramework').config(['$translateProvider', configTranslate]);
    function configTranslate($translateProvider) {
        if(__env.i18njson && Object.keys(__env.i18njson).length > 0 && __env.i18njson.constructor === Object){
            var jsonlangs = __env.i18njson.languages;
            var langs = Object.keys(jsonlangs);
            for(var i=0; i<langs.length; i++){
                $translateProvider.translations(langs[i], jsonlangs[langs[i]]);
            }
            
            $translateProvider.preferredLanguage(__env.i18njson.default);
        } else { //Default translate when no language is defined
            $translateProvider.translations('EN', {});
        }
        
    };

})();