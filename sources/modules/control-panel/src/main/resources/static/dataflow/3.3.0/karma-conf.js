/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
module.exports = function(config){
  config.set({

    /**
     * From where to look for files, starting with the location of this file.
     */
    basePath: '../../',

    /**
     * This is the list of file patterns to load into the browser during testing.
     */
    files: [
      'target/bower_components/underscore/underscore.js',
        'target/bower_components/d3/d3.js',
        'target/bower_components/jquery/dist/jquery.js',
        'target/bower_components/angular/angular.js',
        'target/bower_components/angular-sanitize/angular-sanitize.js',
        'target/bower_components/angular-route/angular-route.js',
        'target/bower_components/angular-cookies/angular-cookies.js',
        'target/bower_components/angular-translate/angular-translate.js',
        'target/bower_components/angular-translate-storage-cookie/angular-translate-storage-cookie.js',
        'target/bower_components/angular-translate-loader-static-files/angular-translate-loader-static-files.js',
        'target/bower_components/angular-dynamic-locale/src/tmhDynamicLocale.js',
        'target/bower_components/bootstrap/dist/js/bootstrap.js',
        'target/bower_components/angular-bootstrap/ui-bootstrap.js',
        'target/bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
        'target/bower_components/moment/moment.js',
        'target/bower_components/angular-moment/angular-moment.js',
        'target/bower_components/nvd3/build/nv.d3.js',
        'target/bower_components/angular-ui-select/dist/select.js',
        'target/bower_components/ngstorage/ngStorage.js',
        'target/bower_components/angular-bootstrap-datetimepicker/src/js/datetimepicker.js',
        'target/bower_components/angular-bootstrap-datetimepicker/src/js/datetimepicker.templates.js',
        'target/bower_components/codemirror/lib/codemirror.js',
        'target/bower_components/codemirror/mode/clike/clike.js',
        'target/bower_components/codemirror/mode/python/python.js',
        'target/bower_components/codemirror/mode/ruby/ruby.js',
        'target/bower_components/codemirror/mode/groovy/groovy.js',
        'target/bower_components/codemirror/mode/javascript/javascript.js',
        'target/bower_components/codemirror/mode/properties/properties.js',
        'target/bower_components/codemirror/mode/shell/shell.js',
        'target/bower_components/codemirror/mode/sql/sql.js',
        'target/bower_components/codemirror/mode/xml/xml.js',
        'target/bower_components/codemirror/addon/edit/closebrackets.js',
        'target/bower_components/codemirror/addon/edit/matchbrackets.js',
        'target/bower_components/codemirror/addon/hint/show-hint.js',
        'target/bower_components/codemirror/addon/hint/javascript-hint.js',
        'target/bower_components/codemirror/addon/hint/sql-hint.js',
        'target/bower_components/angular-xeditable/dist/js/xeditable.js',
        'target/bower_components/angular-mocks/angular-mocks.js',
        'target/dist/templates-app.js',
        'target/dist/templates-common.js',
        
      'src/main/webapp/app/**/*.js',
      '../common-ui/src/main/webapp/common/**/*.js'
    ],

    autoWatch : false,

    frameworks: ['jasmine'],

    browsers : ['Chrome'],

    plugins : [
            'karma-chrome-launcher',
            'karma-firefox-launcher',
            'karma-jasmine',
            'karma-junit-reporter'
            ],

    junitReporter : {
      outputFile: 'test_out/unit.xml',
      suite: 'unit'
    }

  });
};
