angular.module('templates-app', ['app/help/about/aboutModal.tpl.html', 'app/help/register/registerModal.tpl.html', 'app/help/restapi/restapi.tpl.html', 'app/help/settings/settingsModal.tpl.html', 'app/help/supportBundle/supportBundleModal.tpl.html', 'app/home/alerts/error/errorAlert.tpl.html', 'app/home/alerts/error/errorModal.tpl.html', 'app/home/alerts/info/infoAlert.tpl.html', 'app/home/alerts/success/successAlert.tpl.html', 'app/home/detail/badRecords/badRecords.tpl.html', 'app/home/detail/badRecords/pipelineBadRecords.tpl.html', 'app/home/detail/badRecords/stageBadRecords.tpl.html', 'app/home/detail/configuration/configuration.tpl.html', 'app/home/detail/configuration/fieldSelector/fieldSelectorModal.tpl.html', 'app/home/detail/configuration/general.tpl.html', 'app/home/detail/configuration/groupConfiguration.tpl.html', 'app/home/detail/configuration/listBeanConfiguration.tpl.html', 'app/home/detail/configuration/typeTemplate.tpl.html', 'app/home/detail/dataSummary/dataSummary.tpl.html', 'app/home/detail/detail.tpl.html', 'app/home/detail/history/clearHistory/clearHistory.tpl.html', 'app/home/detail/history/history.tpl.html', 'app/home/detail/history/summary/summaryModal.tpl.html', 'app/home/detail/info/info.tpl.html', 'app/home/detail/info/linkInformation.tpl.html', 'app/home/detail/info/pipelineInformation.tpl.html', 'app/home/detail/info/stageInformation.tpl.html', 'app/home/detail/rawPreview/rawPreview.tpl.html', 'app/home/detail/rules/dataDriftRules/dataDriftRules.tpl.html', 'app/home/detail/rules/dataDriftRules/editDataDriftRule.tpl.html', 'app/home/detail/rules/dataRules/dataRules.tpl.html', 'app/home/detail/rules/dataRules/editDataRule.tpl.html', 'app/home/detail/rules/metricAlert/editMetricAlertRule.tpl.html', 'app/home/detail/rules/metricAlert/metricAlert.tpl.html', 'app/home/detail/rules/rules.tpl.html', 'app/home/detail/rules/rulesConfiguration/rulesConfiguration.tpl.html', 'app/home/detail/summary/settings/settingsModal.tpl.html', 'app/home/detail/summary/summary.tpl.html', 'app/home/graph/graph.tpl.html', 'app/home/header/addLabel/addLabelConfirmation.tpl.html', 'app/home/header/downloadExecutable/downloadExecutable.tpl.html', 'app/home/header/header.tpl.html', 'app/home/header/start/start.tpl.html', 'app/home/header/stop/stopConfirmation.tpl.html', 'app/home/home.tpl.html', 'app/home/home_empty.tpl.html', 'app/home/home_grid_view.tpl.html', 'app/home/home_header.tpl.html', 'app/home/home_list_view.tpl.html', 'app/home/library/commit_history/commitHistoryModal.tpl.html', 'app/home/library/create/create.tpl.html', 'app/home/library/delete/delete.tpl.html', 'app/home/library/download_remote/downloadRemoteModal.tpl.html', 'app/home/library/duplicate/duplicate.tpl.html', 'app/home/library/import/importModal.tpl.html', 'app/home/library/importFromArchive/importFromArchiveModal.tpl.html', 'app/home/library/library.tpl.html', 'app/home/library/publish/publishModal.tpl.html', 'app/home/library/revert_changes/revertChangesModal.tpl.html', 'app/home/library/share/share.tpl.html', 'app/home/packageManager/customRepoUrl/customRepoUrl.tpl.html', 'app/home/packageManager/delete_extras/deleteExtras.tpl.html', 'app/home/packageManager/install/install.tpl.html', 'app/home/packageManager/package_manager.tpl.html', 'app/home/packageManager/package_manager_extras_view.tpl.html', 'app/home/packageManager/package_manager_header.tpl.html', 'app/home/packageManager/package_manager_list_view.tpl.html', 'app/home/packageManager/package_manager_side_nav.tpl.html', 'app/home/packageManager/uninstall/uninstall.tpl.html', 'app/home/packageManager/upload_extras/uploadExtras.tpl.html', 'app/home/pipelineHome/pipelineHome.tpl.html', 'app/home/preview/common/previewCommon.tpl.html', 'app/home/preview/common/previewCommonListView.tpl.html', 'app/home/preview/common/previewCommonTableView.tpl.html', 'app/home/preview/common/previewMultiStage.tpl.html', 'app/home/preview/common/previewMultiStageListView.tpl.html', 'app/home/preview/common/previewMultiStageTableView.tpl.html', 'app/home/preview/configuration/previewConfig.tpl.html', 'app/home/preview/configuration/previewConfigModal.tpl.html', 'app/home/preview/preview.tpl.html', 'app/home/preview/rawPreviewData/rawPreviewDataModal.tpl.html', 'app/home/resetOffset/resetOffset.tpl.html', 'app/home/snapshot/modal/snapshotModal.tpl.html', 'app/home/snapshot/snapshot.tpl.html', 'app/home/stageLibrary/stageLibrary.tpl.html', 'app/home/usersAndGroups/users_and_groups.tpl.html']);

angular.module("app/help/about/aboutModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/help/about/aboutModal.tpl.html",
    "<script type=\"text/ng-template\" id=\"aboutModalContent.html\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"about.title\"></h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <div>\n" +
    "      <label translate=\"about.version\">Version:</label>\n" +
    "      <div>\n" +
    "        {{'about.productName' | translate}} {{buildInfo.version}} ( {{'about.builtBy' | translate}} {{buildInfo.builtBy}} {{'about.on' | translate}} {{buildInfo.builtDate}} git: <a href=\"https://github.com/streamsets/datacollector/commit/{{buildInfo.builtRepoSha}}\" target=\"_blank\">{{buildInfo.builtRepoSha}}</a>)\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <br>\n" +
    "\n" +
    "    <div>\n" +
    "      <span ng-bind-html=\"'about.copyrightText' | translate\"></span>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-primary\" ng-click=\"cancel()\" translate=\"global.form.close\">\n" +
    "      Close\n" +
    "    </button>\n" +
    "  </div>\n" +
    "</script>\n" +
    "");
}]);

angular.module("app/help/register/registerModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/help/register/registerModal.tpl.html",
    "<form class=\"register-modal-form\" role=\"form\" ng-submit=\"uploadActivationKey()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"register.headerLabel\">Upload Activation key</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div class=\"alert alert-danger alert-dismissible\" role=\"alert\"\n" +
    "         ng-show=\"!activationInfo.info.valid\">Activation key is not valid</div>\n" +
    "\n" +
    "    <table class=\"table\">\n" +
    "      <tbody>\n" +
    "      <tr>\n" +
    "        <td><label>Licensed to</label></td>\n" +
    "        <td>{{activationInfo.info.userInfo}}</td>\n" +
    "      </tr>\n" +
    "      <tr>\n" +
    "        <td><label>Expiration</label></td>\n" +
    "        <td>{{activationInfo.info.expiration | date:'medium'}}</td>\n" +
    "      </tr>\n" +
    "      <tr>\n" +
    "        <td><label>Licensed to SDC IDs</label></td>\n" +
    "        <td>{{activationInfo.info.validSdcIds}}</td>\n" +
    "      </tr>\n" +
    "      <tr>\n" +
    "        <td><label>SDC ID</label></td>\n" +
    "        <td>{{activationInfo.info.sdcId}}</td>\n" +
    "      </tr>\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "\n" +
    "    <div class=\"input-group\" ng-hide=\"operationDone\">\n" +
    "      <span class=\"input-group-btn\">\n" +
    "          <span class=\"btn btn-primary btn-file\">\n" +
    "              {{'home.import.browse' | translate}}&hellip; <input type=\"file\" fileread=\"uploadFile\">\n" +
    "          </span>\n" +
    "      </span>\n" +
    "      <input class=\"form-control\" readonly ng-model=\"uploadFile.name\"\n" +
    "             placeholder=\"{{'home.import.fileUploadPlaceholder' | translate}}\">\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "         ng-show=\"operationDone\"\n" +
    "         translate=\"register.successMessage\">\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\" ng-hide=\"operationDone || operationInProgress\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" ng-disabled=\"!uploadFile.name\"\n" +
    "            ng-hide=\"operationDone\" translate=\"global.form.upload\">Upload</button>\n" +
    "\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\" ng-show=\"operationDone\"\n" +
    "            translate=\"global.form.close\">Close</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/help/restapi/restapi.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/help/restapi/restapi.tpl.html",
    "<div class=\"panel panel-default page-panel sdc-restapi-page\">\n" +
    "\n" +
    "  <div show-loading=\"fetching\"></div>\n" +
    "\n" +
    "  <div class=\"panel-heading clearfix\">\n" +
    "    <div class=\"panel-title size-toolbar\">\n" +
    "      <h3 class=\"pull-left\" translate=\"restapi.title\">Data Collector RESTful API</h3>\n" +
    "\n" +
    "      <div class=\"btn-group pull-right settings-dropdown\">\n" +
    "        <a class=\"btn btn-link dropdown-toggle\" data-toggle=\"dropdown\"\n" +
    "           tooltip-placement=\"top\"\n" +
    "           tooltip=\"{{'home.header.more' | translate}}\"\n" +
    "           tooltip-popup-delay=\"500\">\n" +
    "          <span class=\"fa fa-gear fa-14x pointer\"></span>\n" +
    "        </a>\n" +
    "        <ul class=\"dropdown-menu pull-right\" role=\"menu\">\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"/rest/swagger.json\"\n" +
    "               target=\"_blank\">swagger.json</a>\n" +
    "          </li>\n" +
    "\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"panel-body\" ng-style=\"{'height': (windowHeight - 60 - 51) + 'px', 'width': (windowWidth) + 'px'}\" resize>\n" +
    "    <div swagger-ui url=\"swaggerURL\" api-explorer=\"true\"></div>\n" +
    "\n" +
    "    <div class=\"alert alert-info\" ng-if=\"!fetching\">\n" +
    "      <strong>Note!</strong> Custom HTTP header attribute (X-Requested-By) is required for all POST/PUT/DELETE REST API requests.\n" +
    "      <pre>curl -u username:password -X DELETE https://localhost:18630/rest/v1/pipeline/samplepipeline -H \"X-Requested-By:sdc\"</pre>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"alert alert-info\" ng-if=\"!fetching\">\n" +
    "      Sample script for calling SDC REST API when Control Hub is enabled:\n" +
    "      <pre>\n" +
    "# login to Control Hub security app\n" +
    "curl -X POST -d '{\"userName\":\"DPMUserID\", \"password\": \"DPMUserPassword\"}' https://cloud.streamsets.com/security/public-rest/v1/authentication/login --header \"Content-Type:application/json\" --header \"X-Requested-By:SDC\" -c cookie.txt\n" +
    "\n" +
    "# generate auth token from security app\n" +
    "sessionToken=$(cat cookie.txt | grep SSO | rev | grep -o '^\\S*' | rev)\n" +
    "echo \"Generated session token : $sessionToken\"\n" +
    "\n" +
    "# Call SDC REST APIs using auth token\n" +
    "curl -X GET http://localhost:18630/rest/v1/pipelines --header \"Content-Type:application/json\" --header \"X-Requested-By:SDC\" --header \"X-SS-REST-CALL:true\" --header \"X-SS-User-Auth-Token:$sessionToken\" -i\n" +
    "      </pre>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/help/settings/settingsModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/help/settings/settingsModal.tpl.html",
    "<form class=\"sdc-settings-modal-form\" role=\"form\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"done()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"sdcSettings.title\">Settings</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <div class=\"form-group\">\n" +
    "      <label class=\"control-label\" translate=\"home.header.timezone\">Timezone</label>\n" +
    "      <select class=\"form-control\"\n" +
    "              name=\"density\"\n" +
    "              ng-model=\"$storage.preferredTimezone\">\n" +
    "        <option ng-repeat=\"timezone in timezoneOptions\"\n" +
    "                ng-selected=\"$storage.preferredTimezone === timezone\"\n" +
    "                value=\"{{ timezone }}\">{{ timezone }}</option>\n" +
    "      </select>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\">\n" +
    "      <label class=\"control-label\" translate=\"home.header.displayDensity\">Display Density</label>\n" +
    "      <select class=\"form-control\"\n" +
    "              name=\"density\"\n" +
    "              ng-model=\"$storage.displayDensity\">\n" +
    "        <option value=\"{{pipelineConstant.DENSITY_COMFORTABLE}}\"\n" +
    "                ng-selected=\"$storage.displayDensity === pipelineConstant.DENSITY_COMFORTABLE\">\n" +
    "          {{'home.header.comfortable' | translate}}</option>\n" +
    "        <option value=\"{{pipelineConstant.DENSITY_COZY}}\"\n" +
    "                ng-selected=\"$storage.displayDensity === pipelineConstant.DENSITY_COZY\">\n" +
    "          {{'home.header.cozy' | translate}}</option>\n" +
    "        <option value=\"{{pipelineConstant.DENSITY_COMPACT}}\"\n" +
    "                ng-selected=\"$storage.displayDensity === pipelineConstant.DENSITY_COMPACT\">\n" +
    "          {{'home.header.compact' | translate}}</option>\n" +
    "      </select>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\">\n" +
    "      <label class=\"control-label\" translate=\"global.menu.help.useHelp\">Help</label>\n" +
    "      <select class=\"form-control\"\n" +
    "              name=\"help\"\n" +
    "              ng-model=\"$storage.helpLocation\">\n" +
    "        <option value=\"{{pipelineConstant.HOSTED_HELP}}\"\n" +
    "                ng-selected=\"$storage.helpLocation === pipelineConstant.HOSTED_HELP\">\n" +
    "          {{'global.menu.help.hostedHelp' | translate}}</option>\n" +
    "        <option value=\"{{pipelineConstant.LOCAL_HELP}}\"\n" +
    "                ng-selected=\"$storage.helpLocation === pipelineConstant.LOCAL_HELP\">\n" +
    "          {{'global.menu.help.localHelp' | translate}}</option>\n" +
    "      </select>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"checkbox\">\n" +
    "      <label>\n" +
    "        <input type=\"checkbox\" ng-model=\"$storage.dontShowHelpAlert\"> {{'sdcSettings.helpBar' | translate}}\n" +
    "      </label>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"checkbox\">\n" +
    "      <label>\n" +
    "        <input type=\"checkbox\" ng-model=\"$storage.dontShowRESTResponseMenu\"> {{'sdcSettings.restResponse' | translate}}\n" +
    "      </label>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"checkbox\">\n" +
    "      <label>\n" +
    "        <input type=\"checkbox\" ng-model=\"$storage.runPreviewForFieldPaths\"> {{'sdcSettings.runPreviewForFieldPaths' | translate}}\n" +
    "      </label>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"checkbox\">\n" +
    "      <label>\n" +
    "        <input type=\"checkbox\" ng-model=\"$storage.lineWrapping\"> {{'sdcSettings.lineWrapping' | translate}}\n" +
    "      </label>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"button\" class=\"btn btn-primary\" ng-click=\"done()\" translate=\"global.form.done\">Done</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/help/supportBundle/supportBundleModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/help/supportBundle/supportBundleModal.tpl.html",
    "<!--\n" +
    "  Copyright 2017 StreamSets Inc.\n" +
    "\n" +
    "  Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
    "  you may not use this file except in compliance with the License.\n" +
    "  You may obtain a copy of the License at\n" +
    "\n" +
    "    http://www.apache.org/licenses/LICENSE-2.0\n" +
    "\n" +
    "  Unless required by applicable law or agreed to in writing, software\n" +
    "  distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
    "  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
    "  See the License for the specific language governing permissions and\n" +
    "  limitations under the License. See accompanying LICENSE file.\n" +
    "-->\n" +
    "\n" +
    "<form class=\"sdc-support-bundle-modal-form\" role=\"form\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"done()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"sdcSupportBundle.title\">Support Bundle</h3>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"modal-body text-center\" ng-show=\"showLoading\">\n" +
    "    <i class=\"fa fa-spinner fa-spin fa-2x\"></i>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"modal-body\">\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div\n" +
    "      ng-if=\"message\"\n" +
    "      class=\"alert alert-{{message.type}} alert-dismissible\"\n" +
    "      translate=\"{{message.id}}\"\n" +
    "      role=\"alert\">\n" +
    "    </div>\n" +
    "\n" +
    "    <p translate=\"sdcSupportBundle.infoMessage\"></p>\n" +
    "\n" +
    "    <div class=\"form-horizontal\" style=\"margin-top: 20px\">\n" +
    "      <div class=\"form-group form-group-sm\">\n" +
    "        <label class=\"col-sm-2 control-label\">SDC ID:</label>\n" +
    "        <div class=\"col-sm-10\">\n" +
    "          <input type=\"text\"\n" +
    "                 class=\"form-control\"\n" +
    "                 style=\"font-family: monospace\"\n" +
    "                 value=\"{{sdc_id}}\"\n" +
    "                 readonly=\"readonly\">\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <table class=\"table table-hover\" ng-show=\"!showLoading\">\n" +
    "      <thead>\n" +
    "        <tr>\n" +
    "          <th translate=\"sdcSupportBundle.generator\" colspan=\"2\">Generator</th>\n" +
    "          <th translate=\"sdcSupportBundle.description\">Description</th>\n" +
    "        </tr>\n" +
    "      </thead>\n" +
    "\n" +
    "      <tbody>\n" +
    "        <tr class=\"generator-row\" ng-repeat=\"generator in generators\" ng-click=\"toggleGenerator($event, generator)\">\n" +
    "          <td><input type=\"checkbox\" ng-model=\"generator.checked\"></td>\n" +
    "          <td>{{generator.name}}</td>\n" +
    "          <td>{{generator.description}}</td>\n" +
    "        </tr>\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button\n" +
    "        type=\"reset\"\n" +
    "        class=\"btn btn-default\"\n" +
    "        ng-click=\"done()\"\n" +
    "        ng-disabled=\"showLoading || uploading\"\n" +
    "        translate=\"global.form.cancel\">Cancel\n" +
    "    </button>\n" +
    "    <button\n" +
    "        type=\"button\"\n" +
    "        class=\"btn btn-default\"\n" +
    "        ng-click=\"downloadBundle()\"\n" +
    "        ng-disabled=\"showLoading || uploading || !hasAnyGeneratorSelected()\"\n" +
    "        translate=\"global.form.download\">Download\n" +
    "    </button>\n" +
    "    <button\n" +
    "        type=\"button\"\n" +
    "        class=\"btn btn-primary\"\n" +
    "        ng-if=\"isSupportBundleUplodEnabled\"\n" +
    "        ng-disabled=\"showLoading || uploading || !hasAnyGeneratorSelected()\"\n" +
    "        ng-click=\"uploadBundle()\">\n" +
    "      <span ng-if=\"!uploading\" translate=\"global.form.upload\">Upload</span>\n" +
    "      <i ng-if=\"uploading\" class=\"fa fa-spinner fa-spin\"></i>\n" +
    "      <span ng-if=\"uploading\" translate=\"global.form.uploading\">Uploading...</span>\n" +
    "    </button>\n" +
    "\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/alerts/error/errorAlert.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/alerts/error/errorAlert.tpl.html",
    "<div class=\"alert alert-danger alert-dismissible\" role=\"alert\"\n" +
    "     ng-controller=\"ErrorAlertController\"\n" +
    "     ng-repeat=\"error in common.errors\">\n" +
    "\n" +
    "  <button type=\"button\" class=\"close\" ng-click=\"removeAlert(common.errors, $index)\">\n" +
    "    <span aria-hidden=\"true\">&times;</span>\n" +
    "    <span class=\"sr-only\">Close</span>\n" +
    "  </button>\n" +
    "\n" +
    "  <span ng-if=\"error && error.RemoteException\">\n" +
    "    {{error.RemoteException.localizedMessage}}\n" +
    "  </span>\n" +
    "\n" +
    "  <a href=\"#\"\n" +
    "     ng-if=\"error && error.RemoteException && error.RemoteException.stackTrace\"\n" +
    "     ng-click=\"showStackTrace(error)\">\n" +
    "    ( <span translate=\"home.graphPane.viewStackTrace\">View Stack Trace</span>... )\n" +
    "  </a>\n" +
    "\n" +
    "  <div class=\"alert-validation-errors\" ng-if=\"error && error.pipelineIssues\">\n" +
    "    <span>{{'global.messages.validate.validationFailed' | translate}}: </span>\n" +
    "    <div class=\"btn-group\">\n" +
    "      <button class=\"btn btn-link dropdown-toggle\" type=\"button\"\n" +
    "              data-toggle=\"dropdown\">\n" +
    "        <span ng-bind=\"error.issueCount\"></span>\n" +
    "        <span>{{'global.messages.validate.validationErrors' | translate}}</span>\n" +
    "      </button>\n" +
    "      <ul class=\"dropdown-menu pull-left scrollable-menu\" role=\"menu\" aria-labelledby=\"dropdownMenu1\">\n" +
    "\n" +
    "        <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "            translate=\"home.header.pipelineIssues\"\n" +
    "            ng-repeat-start=\"issue in error.pipelineIssues\">\n" +
    "          Pipeline Issues\n" +
    "        </li>\n" +
    "        <li role=\"presentation\">\n" +
    "          <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "             ng-click=\"onIssueClick(issue)\">{{issue.message}}</a>\n" +
    "        </li>\n" +
    "        <li ng-repeat-end role=\"presentation\" class=\"divider\" ng-if=\"!$last\"></li>\n" +
    "\n" +
    "        <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "            ng-repeat-start=\"(instanceName, issues) in error.stageIssues\"\n" +
    "            ng-bind=\"getStageInstanceLabel(instanceName)\">\n" +
    "        </li>\n" +
    "        <li role=\"presentation\" ng-repeat=\"issue in issues\">\n" +
    "          <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "             ng-click=\"onIssueClick(issue, instanceName)\">\n" +
    "            <span ng-bind=\"getIssuesMessage(instanceName, issue)\"></span>\n" +
    "            <span ng-show=\"issue.count > 1\" ng-bind=\"issue.count\" class=\"badge\"></span>\n" +
    "          </a>\n" +
    "        </li>\n" +
    "        <li ng-repeat-end role=\"presentation\" class=\"divider\" ng-if=\"!$last\"></li>\n" +
    "\n" +
    "      </ul>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <span ng-if=\"error && error.ISSUES && error.ISSUES.length\" ng-bind-html=\"error.ISSUES[0].message\"></span>\n" +
    "\n" +
    "  <span ng-if=\"error && error.EXCEPTION\" ng-bind-html=\"error.EXCEPTION.rawMessage\"></span>\n" +
    "\n" +
    "  <span ng-if=\"error && !error.RemoteException && !error.pipelineIssues && !error.ISSUES && !error.EXCEPTION\" ng-bind-html=\"error\"></span>\n" +
    "\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/alerts/error/errorModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/alerts/error/errorModal.tpl.html",
    "<script type=\"text/ng-template\" id=\"errorModalContent.html\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"close()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.graphPane.errorStackTrace\">Error Stack Trace</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body error-modal-body\">\n" +
    "    <h4 ng-bind=\"errorObject.RemoteException.localizedMessage\"></h4>\n" +
    "    <pre class=\"alert-danger\" ng-bind=\"errorObject.RemoteException.stackTrace\"></pre>\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"close()\" translate=\"global.form.close\">\n" +
    "      Close\n" +
    "    </button>\n" +
    "  </div>\n" +
    "</script>");
}]);

angular.module("app/home/alerts/info/infoAlert.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/alerts/info/infoAlert.tpl.html",
    "<div class=\"alert alert-info alert-dismissible\" role=\"alert\"\n" +
    "     ng-controller=\"InfoAlertController\"\n" +
    "     ng-repeat=\"info in common.infoList\">\n" +
    "\n" +
    "  <button type=\"button\" class=\"close\" ng-click=\"removeAlert(common.infoList, $index)\">\n" +
    "    <span aria-hidden=\"true\">&times;</span>\n" +
    "    <span class=\"sr-only\">Close</span>\n" +
    "  </button>\n" +
    "\n" +
    "  <span ng-bind-html=\"info.message\"></span>\n" +
    "</div>");
}]);

angular.module("app/home/alerts/success/successAlert.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/alerts/success/successAlert.tpl.html",
    "<div class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "     ng-controller=\"SuccessAlertController\"\n" +
    "     ng-repeat=\"success in common.successList\">\n" +
    "\n" +
    "  <button type=\"button\" class=\"close\" ng-click=\"removeAlert(common.successList, $index)\">\n" +
    "    <span aria-hidden=\"true\">&times;</span>\n" +
    "    <span class=\"sr-only\">Close</span>\n" +
    "  </button>\n" +
    "\n" +
    "  <span>\n" +
    "    {{getAlertMessage(success, common.successList, $index)}}\n" +
    "  </span>\n" +
    "</div>");
}]);

angular.module("app/home/detail/badRecords/badRecords.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/badRecords/badRecords.tpl.html",
    "<div ng-controller=\"BadRecordsController\"  class=\"bad-records-tab container-fluid \">\n" +
    "  <div ng-show=\"detailPaneConfig.instanceName\"\n" +
    "       ng-include=\"'app/home/detail/badRecords/stageBadRecords.tpl.html'\"></div>\n" +
    "\n" +
    "  <div ng-hide=\"detailPaneConfig.instanceName\"\n" +
    "       ng-include=\"'app/home/detail/badRecords/pipelineBadRecords.tpl.html'\"></div>\n" +
    "\n" +
    "</div>");
}]);

angular.module("app/home/detail/badRecords/pipelineBadRecords.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/badRecords/pipelineBadRecords.tpl.html",
    "<div class=\"row\">\n" +
    "  <div class=\"col-sm-6\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\" translate=\"home.detailPane.badRecordsTab.badRecordsHistogram\">Records in Error Histogram</div>\n" +
    "      <div class=\"panel-body\">\n" +
    "\n" +
    "        <nvd3 ng-if=\"errorDataLoaded\" options=\"percentilesChartOptions\" data=\"errorRecordsPercentilesData\"\n" +
    "              config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"col-sm-6\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\" translate=\"home.detailPane.badRecordsTab.badRecordsCounts\">Bad Records Count</div>\n" +
    "      <div class=\"panel-body\">\n" +
    "\n" +
    "        <nvd3 ng-if=\"errorDataLoaded\" options=\"barChartOptions\" data=\"badRecordsChartData\"\n" +
    "              config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "</div>\n" +
    "\n" +
    "<div class=\"row\">\n" +
    "\n" +
    "  <div class=\"col-sm-6\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\" translate=\"home.detailPane.badRecordsTab.errorsHistogram\">Errors Histogram</div>\n" +
    "      <div class=\"panel-body\">\n" +
    "\n" +
    "        <nvd3 ng-if=\"errorDataLoaded\" options=\"percentilesChartOptions\" data=\"errorsPercentilesData\"\n" +
    "              config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"col-sm-6\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\" translate=\"home.detailPane.badRecordsTab.errorsCount\">Errors</div>\n" +
    "      <div class=\"panel-body\">\n" +
    "        <nvd3 ng-if=\"errorDataLoaded\" options=\"barChartOptions\" data=\"errorMessagesChartData\"\n" +
    "              config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>");
}]);

angular.module("app/home/detail/badRecords/stageBadRecords.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/badRecords/stageBadRecords.tpl.html",
    "<tabset class=\"tabs-top\">\n" +
    "\n" +
    "  <tab select=\"onTabSelect()\">\n" +
    "    <tab-heading>\n" +
    "      {{'home.detailPane.badRecords' | translate}} <span class=\"badge alert-danger\">{{ errorRecordsCount.count | abbreviateNumber}}</span>\n" +
    "    </tab-heading>\n" +
    "    <div class=\"tabs-content\">\n" +
    "      <div class=\"row\">\n" +
    "        <div class=\"col-sm-8\" ng-if=\"stageBadRecords && stageBadRecords.length > 0\">\n" +
    "\n" +
    "          <div class=\"panel panel-default\">\n" +
    "            <div class=\"panel-heading\">\n" +
    "              <span translate=\"home.detailPane.badRecordsTab.mostRecentBadRecords\">Most Recent Bad Records</span>\n" +
    "              <i class=\"fa fa-refresh pointer icon-button\"\n" +
    "                 ng-click=\"refreshBadRecordsData()\"\n" +
    "                 tooltip-placement=\"right\"\n" +
    "                 tooltip=\"{{'global.form.refresh' | translate}}\"></i>\n" +
    "            </div>\n" +
    "            <div class=\"panel-body\">\n" +
    "              <table class=\"table\">\n" +
    "                <thead>\n" +
    "                <tr>\n" +
    "                  <th class=\"col-md-4\">\n" +
    "                    <!--span class=\"glyphicon glyphicon-expand pointer\"\n" +
    "                      ng-if=\"!expandAllErrorData\"\n" +
    "                      ng-click=\"onExpandAllErrorData()\"></span>\n" +
    "                    <span class=\"glyphicon glyphicon-collapse-down pointer\"\n" +
    "                      ng-if=\"expandAllErrorData\"\n" +
    "                      ng-click=\"onCollapseAllErrorData()\"></span-->\n" +
    "                    <span translate=\"home.detailPane.badRecords\">Bad Records</span>\n" +
    "                  </th>\n" +
    "                  <th class=\"col-md-3\" translate=\"global.form.timestamp\">Timestamp</th>\n" +
    "                  <th class=\"col-md-5\" translate=\"global.form.errorMessage\"> Error Message</th>\n" +
    "                </tr>\n" +
    "                </thead>\n" +
    "                <tbody ng-hide=\"showBadRecordsLoading\">\n" +
    "                <tr ng-repeat=\"errorRecord in stageBadRecords\">\n" +
    "                  <td>\n" +
    "                    <record-tree\n" +
    "                      record=\"errorRecord\"\n" +
    "                      record-value=\"errorRecord.value\"\n" +
    "                      field-name=\"('global.form.record' | translate)+($index+1)\"\n" +
    "                      is-root=\"true\"\n" +
    "                      is-error=\"true\"\n" +
    "                      show-header=\"true\"\n" +
    "                      show-field-type=\"true\">\n" +
    "                      editable=\"false\">\n" +
    "                    </record-tree>\n" +
    "                  </td>\n" +
    "                  <td>{{errorRecord.header.errorTimestamp | date:'medium'}}</td>\n" +
    "                  <td ng-bind=\"errorRecord.header.errorMessage\"></td>\n" +
    "                </tr>\n" +
    "\n" +
    "                </tbody>\n" +
    "\n" +
    "              </table>\n" +
    "              <div show-loading=\"showBadRecordsLoading\"></div>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "\n" +
    "\n" +
    "        <div class=\"col-sm-4\">\n" +
    "          <div class=\"panel panel-default\">\n" +
    "            <div class=\"panel-heading\" translate=\"home.detailPane.badRecordsTab.badRecordsHistogram\">Bad Records Histogram</div>\n" +
    "            <div class=\"panel-body\">\n" +
    "\n" +
    "              <ul class=\"properties\">\n" +
    "                <li>\n" +
    "                  <span class=\"properties-label\" translate=\"home.detailPane.badRecordsTab.totalNumberOfBadRecords\">Total Number of Bad Records</span>\n" +
    "                  <span class=\"properties-value\">{{errorRecordsCount.count}}</span>\n" +
    "                </li>\n" +
    "              </ul>\n" +
    "\n" +
    "              <nvd3 ng-if=\"errorDataLoaded\" options=\"percentilesChartOptions\" data=\"errorRecordsPercentilesData\"\n" +
    "                    config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "            </div>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </tab>\n" +
    "\n" +
    "\n" +
    "  <tab select=\"onTabSelect()\">\n" +
    "    <tab-heading>\n" +
    "      {{'global.form.stageErrors' | translate}} <span class=\"badge alert-danger\">{{errorMessagesCount.count | abbreviateNumber}}</span>\n" +
    "    </tab-heading>\n" +
    "\n" +
    "    <div class=\"tabs-content\">\n" +
    "\n" +
    "      <div class=\"row\">\n" +
    "\n" +
    "        <div class=\"col-sm-8\" ng-if=\"errorMessages && errorMessages.length > 0\">\n" +
    "\n" +
    "          <div class=\"panel panel-default\">\n" +
    "            <div class=\"panel-heading\">\n" +
    "              <span translate=\"home.detailPane.badRecordsTab.mostRecentErrorMessages\">Most Recent Error Messages</span>\n" +
    "              <i class=\"fa fa-refresh pointer icon-button\"\n" +
    "                 ng-click=\"refreshErrorMessagesData()\"\n" +
    "                 tooltip-placement=\"right\"\n" +
    "                 tooltip=\"{{'global.form.refresh' | translate}}\"></i>\n" +
    "            </div>\n" +
    "            <div class=\"panel-body\">\n" +
    "              <table class=\"table table-hover\">\n" +
    "                <thead>\n" +
    "                <tr>\n" +
    "                  <th class=\"col-md-3\" translate=\"global.form.timestamp\">Timestamp</th>\n" +
    "                  <th class=\"col-md-2\" translate=\"global.form.errorCode\">Error Code</th>\n" +
    "                  <th class=\"col-md-7\" translate=\"global.form.errorMessage\"> Error Message</th>\n" +
    "                </tr>\n" +
    "                </thead>\n" +
    "                <tbody ng-hide=\"showErrorMessagesLoading\">\n" +
    "                <tr ng-repeat=\"errorMessage in errorMessages\">\n" +
    "                  <td>{{errorMessage.timestamp | date:'medium'}}</td>\n" +
    "                  <td ng-bind=\"errorMessage.errorCode\"> </td>\n" +
    "                  <td ng-bind=\"errorMessage.localized\"> </td>\n" +
    "                </tr>\n" +
    "                </tbody>\n" +
    "              </table>\n" +
    "\n" +
    "              <div show-loading=\"showErrorMessagesLoading\"></div>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "\n" +
    "        <div class=\"col-sm-4\">\n" +
    "\n" +
    "          <div class=\"panel panel-default\">\n" +
    "            <div class=\"panel-heading\" translate=\"home.detailPane.badRecordsTab.errorsHistogram\">Errors Histogram</div>\n" +
    "            <div class=\"panel-body\">\n" +
    "\n" +
    "              <ul class=\"properties\">\n" +
    "                <li>\n" +
    "                  <span class=\"properties-label\" translate=\"home.detailPane.badRecordsTab.totalNumberOfErrors\">Total Number of Errors</span>\n" +
    "                  <span class=\"properties-value\">{{errorMessagesCount.count}}</span>\n" +
    "                </li>\n" +
    "              </ul>\n" +
    "\n" +
    "              <nvd3 ng-if=\"errorDataLoaded\" options=\"percentilesChartOptions\" data=\"errorsPercentilesData\"\n" +
    "                    config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "            </div>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "  </tab>\n" +
    "\n" +
    "</tabset>\n" +
    "");
}]);

angular.module("app/home/detail/configuration/configuration.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/configuration/configuration.tpl.html",
    "<div ng-controller=\"ConfigurationController\" class=\"configuration-tab\">\n" +
    "  <tabset class=\"tabs-top\">\n" +
    "\n" +
    "    <tab active=\"generalConfigActive\" select=\"onTabSelect({name: 'generalConfig'})\">\n" +
    "      <tab-heading>\n" +
    "        <i class=\"fa fa-exclamation-triangle icon-danger\"\n" +
    "           ng-show=\"showConfigurationWarning(detailPaneConfig, '')\"></i>\n" +
    "        <span translate=\"home.detailPane.configurationTab.general\">General</span>\n" +
    "      </tab-heading>\n" +
    "\n" +
    "      <div ng-if=\"generalConfigActive\" class=\"tabs-content\">\n" +
    "        <ng-include src=\"'app/home/detail/configuration/general.tpl.html'\"></ng-include>\n" +
    "      </div>\n" +
    "    </tab>\n" +
    "\n" +
    "    <tab ng-repeat=\"groupNameToLabelMap in configGroupTabs\"\n" +
    "         disable=\"!isStageGroupVisible(detailPaneConfig, detailPaneConfigDefn, detailPaneServices, groupNameToLabelMap.name)\"\n" +
    "         active=\"groupNameToLabelMap.active\"\n" +
    "         select=\"onTabSelect(groupNameToLabelMap)\">\n" +
    "      <tab-heading>\n" +
    "        <i class=\"fa fa-exclamation-triangle icon-danger\"\n" +
    "           ng-show=\"showConfigurationWarning(detailPaneConfig, groupNameToLabelMap.name)\"></i>\n" +
    "        <span>{{groupNameToLabelMap.label}}</span>\n" +
    "      </tab-heading>\n" +
    "\n" +
    "      <div class=\"tabs-content\" ng-if=\"groupNameToLabelMap.active\">\n" +
    "        <form class=\"form-horizontal\" role=\"form\"\n" +
    "              ng-class=\"{'form-group-sm': ($storage.displayDensity === pipelineConstant.DENSITY_COZY || $storage.displayDensity === pipelineConstant.DENSITY_COMPACT)}\"\n" +
    "              name=\"{{selectedType === pipelineConstant.STAGE_INSTANCE ? 'd' + detailPaneConfig.instanceName : 'pipeline'}}\">\n" +
    "          <div class=\"form-group \"\n" +
    "               ng-class=\"{'has-error': getConfigurationIssues(detailPaneConfig, configDefinition).length > 0}\"\n" +
    "               ng-repeat=\"configDefinition in detailPaneConfigDefn.configDefinitions | filter: {group: groupNameToLabelMap.name} | orderBy: 'displayPosition'\"\n" +
    "               ng-if=\"(verifyDependsOnMap(detailPaneConfig, configDefinition)) && configDefinition.group === groupNameToLabelMap.name\"\n" +
    "               ng-init=\"configIndex = getConfigIndex(detailPaneConfig, configDefinition)\">\n" +
    "\n" +
    "            <ng-include ng-if=\"selectedType !== pipelineConstant.LINK\"\n" +
    "                    src=\"'app/home/detail/configuration/groupConfiguration.tpl.html'\"></ng-include>\n" +
    "          </div>\n" +
    "          <div\n" +
    "              ng-repeat=\"service in detailPaneServices\"\n" +
    "              ng-if=\"detailPaneServices\">\n" +
    "            <div class=\"form-group \"\n" +
    "                 ng-class=\"{'has-error': getConfigurationIssues(service.config, configDefinition).length > 0}\"\n" +
    "                 ng-repeat=\"configDefinition in service.definition.configDefinitions | filter: {group: groupNameToLabelMap.name} | orderBy: 'displayPosition'\"\n" +
    "                 ng-if=\"(verifyDependsOnMap(service.config, configDefinition)) && configDefinition.group === groupNameToLabelMap.name\"\n" +
    "                 ng-init=\"configIndex = getConfigIndex(service.config, configDefinition); detailPaneConfig = service.config\">\n" +
    "\n" +
    "              <ng-include ng-if=\"selectedType !== pipelineConstant.LINK\"\n" +
    "                      src=\"'app/home/detail/configuration/groupConfiguration.tpl.html'\"></ng-include>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "        </form>\n" +
    "      </div>\n" +
    "    </tab>\n" +
    "\n" +
    "    <tab ng-if=\"errorStageConfig\"\n" +
    "         select=\"onTabSelect({name: 'errorStageConfig'})\"\n" +
    "         active=\"errorStageConfigActive\">\n" +
    "      <tab-heading ng-controller=\"ErrorConfigurationController\">\n" +
    "        <i class=\"fa fa-exclamation-triangle icon-danger\"\n" +
    "           ng-show=\"showConfigurationWarning(detailPaneConfig, undefined, true)\"></i>\n" +
    "        {{'home.detailPane.badRecords' | translate}} - {{errorStageConfigDefn.label}}\n" +
    "      </tab-heading>\n" +
    "      <div class=\"tabs-content\" ng-if=\"errorStageConfigActive\" ng-controller=\"ErrorConfigurationController\">\n" +
    "        <form class=\"form-horizontal\" role=\"form\"\n" +
    "              ng-class=\"{'form-group-sm': ($storage.displayDensity === pipelineConstant.DENSITY_COZY || $storage.displayDensity === pipelineConstant.DENSITY_COMPACT)}\"\n" +
    "              name=\"badRecordsForm\">\n" +
    "          <div class=\"form-group\"\n" +
    "               ng-class=\"{'has-error': getConfigurationIssues(detailPaneConfig, configDefinition).length > 0}\"\n" +
    "               ng-repeat=\"configDefinition in errorStageConfigDefn.configDefinitions | orderBy: 'displayPosition'\"\n" +
    "               ng-if=\"(verifyDependsOnMap(errorStageConfig, configDefinition))\"\n" +
    "               ng-init=\"configIndex = getConfigIndex(errorStageConfig, configDefinition);\">\n" +
    "\n" +
    "            <ng-include ng-if=\"selectedType !== pipelineConstant.LINK\"\n" +
    "                        src=\"'app/home/detail/configuration/groupConfiguration.tpl.html'\"></ng-include>\n" +
    "          </div>\n" +
    "        </form>\n" +
    "\n" +
    "        <div class=\"row\" ng-if=\"!errorStageConfigDefn\">\n" +
    "          <div class=\"col-md-12\" >\n" +
    "            <div class=\"alert alert-danger\" role=\"alert\">\n" +
    "              <i class=\"fa fa-exclamation-triangle\"></i>\n" +
    "              <span translate=\"home.detailPane.noStageDefinitionFound\" translate-values=\"{stageName: detailPaneConfig.stageName, library: detailPaneConfig.library}\"></span>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "\n" +
    "          <div class=\"col-md-12 col-md-offset-1\" >\n" +
    "            <button class=\"btn btn-primary\" ng-click=\"onInstallMissingLibraryClick(detailPaneConfig.library)\">Install Missing Stage Library</button>\n" +
    "          </div>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "      </div>\n" +
    "    </tab>\n" +
    "\n" +
    "    <tab ng-if=\"statsAggregatorStageConfig\"\n" +
    "         select=\"onTabSelect({name: 'statsAggregatorStageConfig'})\"\n" +
    "         active=\"statsAggregatorStageConfigActive\">\n" +
    "      <tab-heading ng-controller=\"StatsAggregatorConfigurationController\">\n" +
    "        <i class=\"fa fa-exclamation-triangle icon-danger\"\n" +
    "           ng-show=\"showConfigurationWarning(detailPaneConfig, undefined, true)\"></i>\n" +
    "        {{'home.detailPane.statsAggregator' | translate}} - {{statsAggregatorStageConfigDefn.label}}\n" +
    "      </tab-heading>\n" +
    "      <div class=\"tabs-content\" ng-if=\"statsAggregatorStageConfigActive\" ng-controller=\"StatsAggregatorConfigurationController\">\n" +
    "        <form class=\"form-horizontal\" role=\"form\"\n" +
    "              ng-class=\"{'form-group-sm': ($storage.displayDensity === pipelineConstant.DENSITY_COZY || $storage.displayDensity === pipelineConstant.DENSITY_COMPACT)}\"\n" +
    "              name=\"badRecordsForm\">\n" +
    "          <div class=\"form-group\"\n" +
    "               ng-class=\"{'has-error': getConfigurationIssues(detailPaneConfig, configDefinition).length > 0}\"\n" +
    "               ng-repeat=\"configDefinition in statsAggregatorStageConfigDefn.configDefinitions | orderBy: 'displayPosition'\"\n" +
    "               ng-if=\"(verifyDependsOnMap(statsAggregatorStageConfig, configDefinition))\"\n" +
    "               ng-init=\"configIndex = getConfigIndex(statsAggregatorStageConfig, configDefinition);\">\n" +
    "\n" +
    "            <ng-include ng-if=\"selectedType !== pipelineConstant.LINK\"\n" +
    "                        src=\"'app/home/detail/configuration/groupConfiguration.tpl.html'\"></ng-include>\n" +
    "          </div>\n" +
    "        </form>\n" +
    "\n" +
    "        <div class=\"row\" ng-if=\"!statsAggregatorStageConfigDefn\">\n" +
    "          <div class=\"col-md-12\" >\n" +
    "            <div class=\"alert alert-danger\" role=\"alert\">\n" +
    "              <i class=\"fa fa-exclamation-triangle\"></i>\n" +
    "              <span translate=\"home.detailPane.noStageDefinitionFound\" translate-values=\"{stageName: detailPaneConfig.stageName, library: detailPaneConfig.library}\"></span>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "\n" +
    "          <div class=\"col-md-12 col-md-offset-1\" >\n" +
    "            <button class=\"btn btn-primary\" ng-click=\"onInstallMissingLibraryClick(detailPaneConfig.library)\">Install Missing Stage Library</button>\n" +
    "          </div>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "      </div>\n" +
    "    </tab>\n" +
    "\n" +
    "    <tab ng-if=\"startEventStageConfig\"\n" +
    "         select=\"onTabSelect({name: 'startEventStageConfig'})\"\n" +
    "         active=\"startEventStageConfigActive\">\n" +
    "      <tab-heading ng-controller=\"StartEventConfigurationController\">\n" +
    "        <i class=\"fa fa-exclamation-triangle icon-danger\"\n" +
    "           ng-show=\"showConfigurationWarning(detailPaneConfig, undefined, true)\"></i>\n" +
    "        {{'home.detailPane.startEvent' | translate}} - {{startEventStageConfigDefn.label}}\n" +
    "      </tab-heading>\n" +
    "      <div class=\"tabs-content\" ng-if=\"startEventStageConfigActive\" ng-controller=\"StartEventConfigurationController\">\n" +
    "        <form class=\"form-horizontal\" role=\"form\"\n" +
    "              ng-class=\"{'form-group-sm': ($storage.displayDensity === pipelineConstant.DENSITY_COZY || $storage.displayDensity === pipelineConstant.DENSITY_COMPACT)}\"\n" +
    "              name=\"badRecordsForm\">\n" +
    "\n" +
    "          <div ng-repeat=\"configurationGroup in startEventStageConfigDefnGroups\">\n" +
    "            <h3 ng-if=\"isGroupVisible(detailPaneConfig, configurationGroup.configDefinitions, configurationGroup.group.name)\">{{configurationGroup.group.label}}</h3>\n" +
    "\n" +
    "            <div class=\"form-group\"\n" +
    "                 ng-class=\"{'has-error': getConfigurationIssues(detailPaneConfig, configDefinition).length > 0}\"\n" +
    "                 ng-repeat=\"configDefinition in configurationGroup.configDefinitions | orderBy: 'displayPosition'\"\n" +
    "                 ng-if=\"(verifyDependsOnMap(startEventStageConfig, configDefinition))\"\n" +
    "                 ng-init=\"configIndex = getConfigIndex(startEventStageConfig, configDefinition);\">\n" +
    "\n" +
    "              <ng-include ng-if=\"selectedType !== pipelineConstant.LINK\"\n" +
    "                          src=\"'app/home/detail/configuration/groupConfiguration.tpl.html'\"></ng-include>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "        </form>\n" +
    "\n" +
    "        <div class=\"row\" ng-if=\"!startEventStageConfigDefn\">\n" +
    "          <div class=\"col-md-12\" >\n" +
    "            <div class=\"alert alert-danger\" role=\"alert\">\n" +
    "              <i class=\"fa fa-exclamation-triangle\"></i>\n" +
    "              <span translate=\"home.detailPane.noStageDefinitionFound\" translate-values=\"{stageName: detailPaneConfig.stageName, library: detailPaneConfig.library}\"></span>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "\n" +
    "          <div class=\"col-md-12 col-md-offset-1\" >\n" +
    "            <button class=\"btn btn-primary\" ng-click=\"onInstallMissingLibraryClick(detailPaneConfig.library)\">Install Missing Stage Library</button>\n" +
    "          </div>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "      </div>\n" +
    "    </tab>\n" +
    "\n" +
    "    <tab ng-if=\"stopEventStageConfig\"\n" +
    "         select=\"onTabSelect({name: 'stopEventStageConfig'})\"\n" +
    "         active=\"stopEventStageConfigActive\">\n" +
    "      <tab-heading ng-controller=\"StopEventConfigurationController\">\n" +
    "        <i class=\"fa fa-exclamation-triangle icon-danger\"\n" +
    "           ng-show=\"showConfigurationWarning(detailPaneConfig, undefined, true)\"></i>\n" +
    "        {{'home.detailPane.stopEvent' | translate}} - {{stopEventStageConfigDefn.label}}\n" +
    "      </tab-heading>\n" +
    "      <div class=\"tabs-content\" ng-if=\"stopEventStageConfigActive\" ng-controller=\"StopEventConfigurationController\">\n" +
    "        <form class=\"form-horizontal\" role=\"form\"\n" +
    "              ng-class=\"{'form-group-sm': ($storage.displayDensity === pipelineConstant.DENSITY_COZY || $storage.displayDensity === pipelineConstant.DENSITY_COMPACT)}\"\n" +
    "              name=\"badRecordsForm\">\n" +
    "\n" +
    "          <div ng-repeat=\"configurationGroup in stopEventStageConfigDefnGroups\">\n" +
    "            <h3 ng-if=\"isGroupVisible(detailPaneConfig, configurationGroup.configDefinitions, configurationGroup.group.name)\">{{configurationGroup.group.label}}</h3>\n" +
    "\n" +
    "            <div class=\"form-group\"\n" +
    "                 ng-class=\"{'has-error': getConfigurationIssues(detailPaneConfig, configDefinition).length > 0}\"\n" +
    "                 ng-repeat=\"configDefinition in configurationGroup.configDefinitions | orderBy: 'displayPosition'\"\n" +
    "                 ng-if=\"(verifyDependsOnMap(stopEventStageConfig, configDefinition))\"\n" +
    "                 ng-init=\"configIndex = getConfigIndex(stopEventStageConfig, configDefinition);\">\n" +
    "\n" +
    "              <ng-include ng-if=\"selectedType !== pipelineConstant.LINK\"\n" +
    "                          src=\"'app/home/detail/configuration/groupConfiguration.tpl.html'\"></ng-include>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "        </form>\n" +
    "\n" +
    "        <div class=\"row\" ng-if=\"!stopEventStageConfigDefn\">\n" +
    "          <div class=\"col-md-12\" >\n" +
    "            <div class=\"alert alert-danger\" role=\"alert\">\n" +
    "              <i class=\"fa fa-exclamation-triangle\"></i>\n" +
    "              <span translate=\"home.detailPane.noStageDefinitionFound\" translate-values=\"{stageName: detailPaneConfig.stageName, library: detailPaneConfig.library}\"></span>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "\n" +
    "          <div class=\"col-md-12 col-md-offset-1\" >\n" +
    "            <button class=\"btn btn-primary\" ng-click=\"onInstallMissingLibraryClick(detailPaneConfig.library)\">Install Missing Stage Library</button>\n" +
    "          </div>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "      </div>\n" +
    "    </tab>\n" +
    "\n" +
    "  </tabset>\n" +
    "\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "<ng-include src=\"'app/home/detail/configuration/typeTemplate.tpl.html'\"></ng-include>\n" +
    "");
}]);

angular.module("app/home/detail/configuration/fieldSelector/fieldSelectorModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/configuration/fieldSelector/fieldSelectorModal.tpl.html",
    "<script type=\"text/ng-template\" id=\"fieldSelectorModalContent.html\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"close()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\">Field Selector</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body field-selector-modal-body\">\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div show-loading=\"showLoading\"></div>\n" +
    "\n" +
    "    <div ng-if=\"!showLoading\">\n" +
    "      <record-tree\n" +
    "        record=\"recordObject\"\n" +
    "        record-value=\"recordObject.value\"\n" +
    "        field-name=\"('global.form.record' | translate)\"\n" +
    "        is-root=\"true\"\n" +
    "        editable=\"false\"\n" +
    "        selectable=\"true\"\n" +
    "        selected-path=\"selectedPath\">\n" +
    "      </record-tree>\n" +
    "    </div>\n" +
    "\n" +
    "    <label class=\"label-warning\"\n" +
    "           ng-show=\"noPreviewRecord\"\n" +
    "           translate=\"home.detailPane.configurationTab.noRecord\">No Preview Record</label>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\"\n" +
    "            ng-hide=\"showLoading || noPreviewRecord\" ng-click=\"save()\" translate=\"global.form.save\">Save</button>\n" +
    "    <button type=\"reset\" class=\"btn btn-default\"\n" +
    "            ng-click=\"close()\" translate=\"global.form.close\">Close</button>\n" +
    "  </div>\n" +
    "</script>\n" +
    "");
}]);

angular.module("app/home/detail/configuration/general.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/configuration/general.tpl.html",
    "<form class=\"form-horizontal\"\n" +
    "      ng-class=\"{'form-group-sm': $storage.displayDensity === pipelineConstant.DENSITY_COZY || $storage.displayDensity === pipelineConstant.DENSITY_COMPACT}\"\n" +
    "      role=\"form\" name=\"stageGeneralInfo\">\n" +
    "\n" +
    "  <!-- General Configuration for Pipeline-->\n" +
    "  <div class=\"form-group\" ng-if=\"selectedType === pipelineConstant.PIPELINE\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\" translate=\"global.form.pipelineId\">Pipeline ID</label>\n" +
    "    <div class=\"controls col-lg-7 col-md-8\">\n" +
    "      <input readonly type=\"text\" class=\"form-control\" name=\"name\"\n" +
    "             ng-model=\"pipelineConfig.info.pipelineId\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\" ng-if=\"selectedType === pipelineConstant.PIPELINE\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\" translate=\"global.form.title\">Title</label>\n" +
    "    <div class=\"controls col-lg-7 col-md-8\">\n" +
    "      <input type=\"text\" class=\"form-control\" name=\"title\"\n" +
    "             ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "             ng-model=\"pipelineConfig.title\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\" ng-if=\"selectedType === pipelineConstant.PIPELINE\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\" translate=\"global.form.description\">Description</label>\n" +
    "    <div class=\"controls col-lg-7 col-md-8\">\n" +
    "      <textarea class=\"form-control\" name=\"description\"\n" +
    "                rows=\"1\"\n" +
    "                ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "                ng-model=\"pipelineConfig.description\"></textarea>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\" ng-if=\"selectedType === pipelineConstant.PIPELINE\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\" translate=\"global.form.labels\">Labels</label>\n" +
    "    <div class=\"controls col-lg-7 col-md-8\">\n" +
    "      <ui-select multiple\n" +
    "                 tagging\n" +
    "                 tagging-label=\" - new label\"\n" +
    "                 ng-disabled=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "                 tagging-tokens=\",|ENTER\"\n" +
    "                 ng-model=\"pipelineConfig.metadata.labels\">\n" +
    "        <ui-select-match class=\"ui-select-match\">{{$item}}</ui-select-match>\n" +
    "        <ui-select-choices class=\"ui-select-choices\"\n" +
    "                           repeat=\"listValue in existingPipelineLabels | filter:$select.search\">\n" +
    "          <div ng-bind-html=\"listValue | highlight: $select.search\"></div>\n" +
    "        </ui-select-choices>\n" +
    "      </ui-select>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <!-- General Configuration for Stages-->\n" +
    "  <div class=\"form-group\" ng-if=\"selectedType === pipelineConstant.STAGE_INSTANCE\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\" translate=\"global.form.name\">Name</label>\n" +
    "    <div class=\"controls col-lg-7 col-md-8\">\n" +
    "      <input type=\"text\" class=\"form-control\" name=\"name\"\n" +
    "             ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "             ng-model=\"detailPaneConfig.uiInfo.label\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\" ng-if=\"selectedType === pipelineConstant.STAGE_INSTANCE\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\" translate=\"global.form.description\">Description</label>\n" +
    "    <div class=\"controls col-lg-7 col-md-8\">\n" +
    "      <textarea class=\"form-control\" name=\"description\"\n" +
    "                rows=\"1\"\n" +
    "                ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "                ng-model=\"detailPaneConfig.uiInfo.description\"></textarea>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\" ng-if=\"selectedType === pipelineConstant.STAGE_INSTANCE && stageLibraryList.length > 1\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.configurationTab.stageLibrary\">Stage Library</label>\n" +
    "    <div class=\"controls col-lg-7 col-md-8\">\n" +
    "      <select class=\"form-control\"\n" +
    "              name=\"stageLibrary\"\n" +
    "              ng-disabled=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "              ng-model=\"detailPaneConfig.library\"\n" +
    "              ng-options=\"item.library as item.libraryLabel for item in stageLibraryList\">\n" +
    "      </select>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\" ng-if=\"selectedType === pipelineConstant.STAGE_INSTANCE && detailPaneConfigDefn.producingEvents\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.configurationTab.producingEventsConfig\">Produce Events</label>\n" +
    "    <div class=\"controls col-lg-7 col-md-8\">\n" +
    "      <input type=\"checkbox\" name=\"producingEventsConfig\"\n" +
    "             ng-change=\"producingEventsConfigChange()\"\n" +
    "             ng-disabled=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "             ng-model=\"producingEventsConfig.value\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "</form>\n" +
    "\n" +
    "<form class=\"form-horizontal\" role=\"form\"\n" +
    "      ng-class=\"{'form-group-sm': ($storage.displayDensity === pipelineConstant.DENSITY_COZY || $storage.displayDensity === pipelineConstant.DENSITY_COMPACT)}\"\n" +
    "      name=\"{{selectedType === pipelineConstant.STAGE_INSTANCE ? 'd' + detailPaneConfig.instanceName : 'pipeline'}}\">\n" +
    "  <div class=\"form-group\"\n" +
    "       ng-class=\"{'has-error': getConfigurationIssues(detailPaneConfig, configDefinition).length > 0}\"\n" +
    "       ng-repeat=\"configDefinition in detailPaneConfigDefn.configDefinitions | filter: {group: ''} | orderBy: 'displayPosition'\"\n" +
    "       ng-if=\"(verifyDependsOnMap(detailPaneConfig, configDefinition)) && configDefinition.group === ''\"\n" +
    "       ng-init=\"configIndex = getConfigIndex(detailPaneConfig, configDefinition)\">\n" +
    "\n" +
    "    <ng-include ng-if=\"selectedType !== pipelineConstant.LINK\"\n" +
    "                src=\"'app/home/detail/configuration/groupConfiguration.tpl.html'\"></ng-include>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/detail/configuration/groupConfiguration.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/configuration/groupConfiguration.tpl.html",
    "<div ng-switch=\"configDefinition.type\" class=\"config_{{configDefinition.name}}\">\n" +
    "  <div ng-switch-when=\"BOOLEAN\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'booleanConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"NUMBER\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'elStringConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"STRING\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'elStringConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"CREDENTIAL\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'elCredentialConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"CHARACTER\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'characterConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"EL_STRING\" class=\"expression-language\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'elStringConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"EL_CREDENTIAL\" class=\"expression-language\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'elCredentialConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"EL_NUMBER\" class=\"expression-language\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'elStringConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"EL_DATE\" class=\"expression-language\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'elStringConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"EL_BOOLEAN\" class=\"expression-language\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'elStringConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"EL_OBJECT\" class=\"expression-language\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'elStringConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"LIST\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'listConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"MAP\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'mapConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"TEXT\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "    <ng-include src=\"'textConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"MODEL\"\n" +
    "       ng-switch=\"configDefinition.model.modelType\">\n" +
    "\n" +
    "    <div ng-switch-when=\"VALUE_CHOOSER\">\n" +
    "      <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "      <ng-include src=\"'valueChooserConfigurationTemplate'\"></ng-include>\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-switch-when=\"MULTI_VALUE_CHOOSER\">\n" +
    "      <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "      <ng-include src=\"'multiValueChooserConfigurationTemplate'\"></ng-include>\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-switch-when=\"FIELD_SELECTOR_MULTI_VALUE\"\n" +
    "         ng-click=\"onFieldSelectorFocus(detailPaneConfig)\"\n" +
    "         class=\"clearfix\">\n" +
    "      <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "      <ng-include src=\"'fieldSelectorMultiValuedConfigurationTemplate'\"></ng-include>\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-switch-when=\"FIELD_SELECTOR\"\n" +
    "         ng-click=\"onFieldSelectorFocus(detailPaneConfig)\"\n" +
    "         class=\"clearfix\">\n" +
    "      <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "      <ng-include src=\"'fieldSelectorSingleValuedConfigurationTemplate'\"></ng-include>\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-switch-when=\"PREDICATE\"\n" +
    "         ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "      <ng-include src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "      <ng-include src=\"'lanePredicateMappingConfigurationTemplate'\"></ng-include>\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "    <div ng-switch-when=\"LIST_BEAN\" class=\"clearfix\"\n" +
    "         ng-init=\"bulkEdit=(detailPaneConfig.configuration[configIndex].value.length > 9)\">\n" +
    "      <ng-include ng-if=\"configDefinition.label\" src=\"'labelConfigurationTemplate'\"></ng-include>\n" +
    "\n" +
    "      <div class=\"controls\" ng-class=\"configDefinition.label ? 'col-md-7' : 'col-md-12'\">\n" +
    "\n" +
    "        <table class=\"table\" ng-if=\"_.size(configDefinition.model.configDefinitions) <= 4 && !bulkEdit\">\n" +
    "          <thead>\n" +
    "          <tr>\n" +
    "            <th class=\"custom-field-property\"\n" +
    "                ng-repeat=\"customeFieldConfigDefintion in configDefinition.model.configDefinitions | orderBy: 'displayPosition'\">\n" +
    "              <span>{{customeFieldConfigDefintion.label}}</span>\n" +
    "              <i class=\"fa fa-info-circle help-icon\"\n" +
    "                 ng-if=\"customeFieldConfigDefintion.description\"\n" +
    "                 tooltip-placement=\"right\"\n" +
    "                 tooltip-append-to-body=\"false\"\n" +
    "                 tooltip=\"{{customeFieldConfigDefintion.description}}\"\n" +
    "                 tooltip-trigger=\"mouseenter\"></i>\n" +
    "            </th>\n" +
    "            <th class=\"custom-field-buttons\"></th>\n" +
    "          </tr>\n" +
    "          </thead>\n" +
    "          <tbody>\n" +
    "          <tr ng-repeat=\"customFieldValue in customeFieldValueList = detailPaneConfig.configuration[configIndex].value track by $index\">\n" +
    "\n" +
    "            <td class=\"col-md-5\"\n" +
    "                ng-repeat=\"customFieldConfigDefinition in configDefinition.model.configDefinitions | orderBy: 'displayPosition'\">\n" +
    "\n" +
    "              <div ng-if=\"(verifyCustomFieldDependsOn(detailPaneConfig, customFieldValue, customFieldConfigDefinition))\">\n" +
    "                <ng-include src=\"'app/home/detail/configuration/listBeanConfiguration.tpl.html'\"></ng-include>\n" +
    "              </div>\n" +
    "\n" +
    "            </td>\n" +
    "\n" +
    "            <td>\n" +
    "              <div class=\"btn-group\" role=\"group\" aria-label=\"Default button group\" ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "                <button type=\"button\" class=\"btn btn-default\"\n" +
    "                        ng-click=\"removeFromCustomField(detailPaneConfig, customeFieldValueList, $index)\">\n" +
    "                  <i class=\"fa fa-minus pointer\"></i>\n" +
    "                </button>\n" +
    "                <button type=\"button\" class=\"btn btn-default\"\n" +
    "                        ng-show=\"$last\"\n" +
    "                        ng-click=\"addToCustomField(detailPaneConfig, detailPaneConfig.configuration[configIndex], configDefinition.model.configDefinitions)\">\n" +
    "                  <i class=\"fa fa-plus pointer\"></i>\n" +
    "                </button>\n" +
    "              </div>\n" +
    "            </td>\n" +
    "\n" +
    "          </tr>\n" +
    "          </tbody>\n" +
    "        </table>\n" +
    "\n" +
    "        <table class=\"table table-bordered\" ng-if=\"_.size(configDefinition.model.configDefinitions) > 4 && !bulkEdit\">\n" +
    "          <tbody>\n" +
    "          <tr ng-repeat=\"customFieldValue in customeFieldValueList = detailPaneConfig.configuration[configIndex].value track by $index\"\n" +
    "              ng-init=\"parentConfigIndex=configIndex; configIndex = $index\">\n" +
    "\n" +
    "            <td>\n" +
    "              <div class=\"form-group \"\n" +
    "                   ng-repeat=\"customFieldConfigDefinition in configDefinition.model.configDefinitions | orderBy: 'displayPosition'\"\n" +
    "                   ng-class=\"{'has-error': getConfigurationIssues(detailPaneConfig, customFieldConfigDefinition).length > 0}\"\n" +
    "                   ng-if=\"(verifyCustomFieldDependsOn(detailPaneConfig, customFieldValue, customFieldConfigDefinition))\">\n" +
    "\n" +
    "                <label class=\"col-lg-3 control-label\">{{customFieldConfigDefinition.label}}\n" +
    "                  <i class=\"fa fa-info-circle help-icon\"\n" +
    "                     tooltip-placement=\"right\"\n" +
    "                     tooltip-append-to-body=\"false\"\n" +
    "                     tooltip=\"{{customFieldConfigDefinition.description || customFieldConfigDefinition.label}}\"\n" +
    "                     tooltip-trigger=\"mouseenter\"></i>\n" +
    "                </label>\n" +
    "\n" +
    "                <div class=\"controls col-lg-9\">\n" +
    "                  <ng-include src=\"'app/home/detail/configuration/listBeanConfiguration.tpl.html'\"></ng-include>\n" +
    "                  <ng-include src=\"'complexConfigurationIssuesTemplate'\"></ng-include>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "            </td>\n" +
    "\n" +
    "            <td>\n" +
    "              <div class=\"btn-group\" role=\"group\" aria-label=\"Default button group\" ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "                <button type=\"button\" class=\"btn btn-default\"\n" +
    "                        ng-click=\"removeFromCustomField(detailPaneConfig, customeFieldValueList, $index)\">\n" +
    "                  <i class=\"fa fa-minus pointer\"></i>\n" +
    "                </button>\n" +
    "                <button type=\"button\" class=\"btn btn-default\"\n" +
    "                        ng-show=\"$last\"\n" +
    "                        ng-click=\"addToCustomField(detailPaneConfig, detailPaneConfig.configuration[parentConfigIndex], configDefinition.model.configDefinitions)\">\n" +
    "                  <i class=\"fa fa-plus pointer\"></i>\n" +
    "                </button>\n" +
    "              </div>\n" +
    "            </td>\n" +
    "\n" +
    "          </tr>\n" +
    "\n" +
    "          </tbody>\n" +
    "        </table>\n" +
    "\n" +
    "        <div ui-codemirror\n" +
    "             ng-if=\"bulkEdit\"\n" +
    "             class=\"codemirror-editor\"\n" +
    "             ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "             focus-me=\"{{autoFocusConfigName === configDefinition.name}}\"\n" +
    "             ng-model=\"detailPaneConfig.configuration[configIndex].value\"\n" +
    "             ui-refresh=\"refreshCodemirror\"\n" +
    "             ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                autofocus: autoFocusConfigName === configDefinition.name,\n" +
    "                lineNumbers: true\n" +
    "              }, configDefinition)\"\n" +
    "             codemirror-el\n" +
    "             field-paths=\"fieldPaths\"\n" +
    "             d-field-paths=\"dFieldPaths\"\n" +
    "             data-type=\"'LIST'\">\n" +
    "        </div>\n" +
    "\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-if=\"(!detailPaneConfig.configuration[configIndex].value || detailPaneConfig.configuration[configIndex].value.length === 0) || bulkEdit\"\n" +
    "                ng-hide=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "                ng-click=\"addToCustomField(detailPaneConfig, detailPaneConfig.configuration[configIndex], configDefinition.model.configDefinitions)\">\n" +
    "          <i class=\"fa fa-plus pointer\"></i>\n" +
    "        </button>\n" +
    "\n" +
    "        <div class=\"pull-right field-selector-btn\" ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "          <button type=\"button\" class=\"btn btn-link\"\n" +
    "                  ng-click=\"bulkEdit = !bulkEdit\">\n" +
    "            {{bulkEdit ? ('home.detailPane.configurationTab.switchToSimpleMode' | translate) : ('home.detailPane.configurationTab.switchToBulkMode' | translate)}}\n" +
    "          </button>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"help-block\">\n" +
    "          <div ng-repeat=\"issue in getConfigurationIssues(detailPaneConfig, configDefinition)\">\n" +
    "            <span>{{issue.message}}</span>\n" +
    "            <span ng-show=\"issue.count > 1\" class=\"badge\">{{issue.count}}</span>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "\n" +
    "\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/detail/configuration/listBeanConfiguration.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/configuration/listBeanConfiguration.tpl.html",
    "<div ng-switch=\"customFieldConfigDefinition.type\">\n" +
    "  <div ng-switch-when=\"NUMBER\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\"\n" +
    "       class=\"expression-language\">\n" +
    "    <ng-include src=\"'elStringComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"STRING\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\"\n" +
    "       class=\"expression-language\">\n" +
    "    <ng-include src=\"'elStringComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"CREDENTIAL\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\"\n" +
    "       class=\"expression-language\">\n" +
    "    <ng-include src=\"'elCredentialComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"BOOLEAN\">\n" +
    "    <ng-include src=\"'booleanComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"EL_STRING\" class=\"expression-language\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'elStringComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"EL_CREDENTIAL\" class=\"expression-language\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'elCredentialComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"EL_NUMBER\" class=\"expression-language\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'elStringComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"EL_DATE\" class=\"expression-language\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'elStringComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"EL_BOOLEAN\" class=\"expression-language\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'elStringComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"EL_OBJECT\" class=\"expression-language\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'elStringComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"LIST\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'listComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"MAP\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'mapComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"TEXT\"\n" +
    "       ng-click=\"onFieldSelectorFocus(detailPaneConfig)\">\n" +
    "    <ng-include src=\"'textComplexConfigurationTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-switch-when=\"MODEL\"\n" +
    "       ng-switch=\"customFieldConfigDefinition.model.modelType\">\n" +
    "\n" +
    "    <div ng-switch-when=\"VALUE_CHOOSER\">\n" +
    "      <select class=\"form-control\"\n" +
    "              name=\"{{customFieldConfigDefinition.name + $index}}\"\n" +
    "              ng-disabled=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "              ng-model=\"customFieldValue[customFieldConfigDefinition.name]\">\n" +
    "        <option ng-repeat=\"listValue in customFieldConfigDefinition.model.values\"\n" +
    "                value=\"{{listValue}}\"\n" +
    "                ng-selected=\"listValue === customFieldValue[customFieldConfigDefinition.name]\">\n" +
    "          {{customFieldConfigDefinition.model.labels[$index]}}\n" +
    "        </option>\n" +
    "      </select>\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-switch-when=\"FIELD_SELECTOR_MULTI_VALUE\"\n" +
    "         ng-click=\"onFieldSelectorFocus(detailPaneConfig)\"\n" +
    "         class=\"clearfix\">\n" +
    "      <ui-select multiple\n" +
    "                 tagging=\"tagTransform\"\n" +
    "                 tagging-label=\"\"\n" +
    "                 reset-search-input=\"true\"\n" +
    "                 tagging-tokens=\",|ENTER\"\n" +
    "                 ng-disabled=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "                 ng-model=\"customFieldValue[customFieldConfigDefinition.name]\">\n" +
    "        <ui-select-match class=\"ui-select-match\"\n" +
    "          placeholder=\"{{'home.detailPane.configurationTab.selectFields' | translate}}\">\n" +
    "          {{$item}}\n" +
    "        </ui-select-match>\n" +
    "        <ui-select-choices class=\"ui-select-choices\" repeat=\"fieldPath in fieldPaths | filter:$select.search\">\n" +
    "          {{fieldPath}}\n" +
    "        </ui-select-choices>\n" +
    "      </ui-select>\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-switch-when=\"FIELD_SELECTOR\"\n" +
    "         ng-click=\"onFieldSelectorFocus(detailPaneConfig)\"\n" +
    "         class=\"clearfix input-group\">\n" +
    "      <input type=\"text\" class=\"form-control\" ng-model=\"customFieldValue[customFieldConfigDefinition.name]\">\n" +
    "      <div class=\"input-group-btn\">\n" +
    "        <button type=\"button\" class=\"btn btn-default dropdown-toggle\" data-toggle=\"dropdown\"><span class=\"caret\"></span></button>\n" +
    "        <ul class=\"dropdown-menu dropdown-menu-right\" role=\"menu\">\n" +
    "          <li ng-repeat=\"fieldPath in fieldPaths track by $index\">\n" +
    "            <a href=\"#\" ng-click=\"customFieldValue[customFieldConfigDefinition.name] = fieldPath\">{{fieldPath}}</a>\n" +
    "          </li>\n" +
    "          <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "              ng-if=\"fieldPathsFetchInProgress\"\n" +
    "              translate=\"home.detailPane.configurationTab.loading\">Loading...</li>\n" +
    "          <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "              ng-if=\"!fieldPathsFetchInProgress && fieldPaths.length === 0\"\n" +
    "              translate=\"home.detailPane.configurationTab.noFieldFound\">No Field found.</li>\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "</div>\n" +
    "\n" +
    "");
}]);

angular.module("app/home/detail/configuration/typeTemplate.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/configuration/typeTemplate.tpl.html",
    "<script type=\"text/ng-template\" id=\"labelConfigurationTemplate\">\n" +
    "  <label class=\"col-lg-3 col-md-4 control-label\">{{configDefinition.label}}\n" +
    "    <i class=\"fa fa-info-circle help-icon\"\n" +
    "       ng-class=\"{\n" +
    "       'fa-info-circle': configDefinition.type !== 'CREDENTIAL' && configDefinition.type !== 'EL_CREDENTIAL',\n" +
    "       'fa-key': configDefinition.type === 'CREDENTIAL' || configDefinition.type === 'EL_CREDENTIAL'\n" +
    "       }\"\n" +
    "       tooltip-placement=\"right\"\n" +
    "       tooltip-append-to-body=\"false\"\n" +
    "       tooltip=\"{{configDefinition.description || configDefinition.label}}\"\n" +
    "       tooltip-trigger=\"mouseenter\"></i>\n" +
    "  </label>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"configurationIssuesTemplate\">\n" +
    "  <div class=\"help-block\">\n" +
    "    <div ng-repeat=\"issue in getConfigurationIssues(detailPaneConfig, configDefinition)\">\n" +
    "      <span>{{issue.message}}</span>\n" +
    "      <span ng-show=\"issue.count > 1\" class=\"badge\">{{issue.count}}</span>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"complexConfigurationIssuesTemplate\">\n" +
    "  <div class=\"help-block\">\n" +
    "    <div ng-repeat=\"issue in getConfigurationIssues(detailPaneConfig, customFieldConfigDefinition)\"\n" +
    "         ng-if=\"issue.additionalInfo && issue.additionalInfo.index === configIndex\">\n" +
    "      <span>{{issue.message}}</span>\n" +
    "      <span ng-show=\"issue.count > 1\" class=\"badge\">{{issue.count}}</span>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"stringConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-7 col-md-8\">\n" +
    "\n" +
    "    <input type=\"text\" class=\"form-control\"\n" +
    "           name=\"{{configDefinition.name}}\"\n" +
    "           ng-if=\"configDefinition.lines === 0\"\n" +
    "           focus-me=\"{{autoFocusConfigName === configDefinition.name}}\"\n" +
    "           ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "           ng-model=\"detailPaneConfig.configuration[configIndex].value\"\n" +
    "           ng-required=\"configDefinition.required\">\n" +
    "\n" +
    "    <textarea class=\"form-control\"\n" +
    "              name=\"{{configDefinition.name}}\"\n" +
    "              ng-if=\"configDefinition.lines > 0\"\n" +
    "              focus-me=\"{{autoFocusConfigName === configDefinition.name}}\"\n" +
    "              rows=\"{{configDefinition.lines}}\"\n" +
    "              ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "              ng-model=\"detailPaneConfig.configuration[configIndex].value\"\n" +
    "              ng-required=\"configDefinition.required\"></textarea>\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"elStringConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-7 col-md-8\">\n" +
    "\n" +
    "    <div ui-codemirror\n" +
    "         ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "         focus-me=\"{{autoFocusConfigName === configDefinition.name}}\"\n" +
    "         ng-model=\"detailPaneConfig.configuration[configIndex].value\"\n" +
    "         ui-refresh=\"refreshCodemirror\"\n" +
    "         ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                autofocus: autoFocusConfigName === configDefinition.name,\n" +
    "                extraKeys: {\n" +
    "                  'Tab': false,\n" +
    "                  'Enter': common.ignoreCodeMirrorEnterKey,\n" +
    "                  'Ctrl-Space': 'autocomplete'\n" +
    "                }\n" +
    "              }, configDefinition)\"\n" +
    "         codemirror-el\n" +
    "         field-paths=\"fieldPaths\"\n" +
    "         d-field-paths=\"dFieldPaths\"\n" +
    "         data-type=\"configDefinition.type\">\n" +
    "    </div>\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"elCredentialConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-7 col-md-8\"\n" +
    "       ng-init=\"showValue=(!detailPaneConfig.configuration[configIndex].value || detailPaneConfig.configuration[configIndex].value.indexOf('${') === 0)\">\n" +
    "\n" +
    "    <div ui-codemirror\n" +
    "         ng-if=\"showValue\"\n" +
    "         ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "         focus-me=\"{{autoFocusConfigName === configDefinition.name}}\"\n" +
    "         ng-model=\"detailPaneConfig.configuration[configIndex].value\"\n" +
    "         ui-refresh=\"refreshCodemirror\"\n" +
    "         ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                autofocus: autoFocusConfigName === configDefinition.name,\n" +
    "                extraKeys: {\n" +
    "                  'Tab': false,\n" +
    "                  'Enter': common.ignoreCodeMirrorEnterKey,\n" +
    "                  'Ctrl-Space': 'autocomplete'\n" +
    "                }\n" +
    "              }, configDefinition)\"\n" +
    "         codemirror-el\n" +
    "         field-paths=\"fieldPaths\"\n" +
    "         d-field-paths=\"dFieldPaths\"\n" +
    "         data-type=\"configDefinition.type\">\n" +
    "    </div>\n" +
    "\n" +
    "    <input type=\"password\" class=\"form-control\"\n" +
    "           ng-if=\"!showValue\"\n" +
    "           name=\"{{configDefinition.name}}\"\n" +
    "           focus-me=\"{{autoFocusConfigName === configDefinition.name}}\"\n" +
    "           ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "           ng-model=\"detailPaneConfig.configuration[configIndex].value\"\n" +
    "           ng-required=\"configDefinition.required\">\n" +
    "\n" +
    "\n" +
    "    <div class=\"pull-right field-selector-btn\" ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "      <button type=\"button\" class=\"btn btn-link\"\n" +
    "              ng-click=\"showValue = !showValue\">\n" +
    "        {{showValue ? ('global.form.hideValue' | translate) : ('global.form.showValue' | translate)}}\n" +
    "      </button>\n" +
    "    </div>\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"characterConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-7 col-md-8\" ng-init=\"characterModel = getCharacterValue(detailPaneConfig.configuration[configIndex].value)\">\n" +
    "\n" +
    "    <div class=\"clearfix\">\n" +
    "      <div class=\"btn-group pull-left\">\n" +
    "\n" +
    "        <label class=\"btn btn-default\"\n" +
    "               ng-model=\"characterModel\"\n" +
    "               ng-change=\"detailPaneConfig.configuration[configIndex].value='\\t'\"\n" +
    "               btn-radio=\"'\\t'\">Tab</label>\n" +
    "        <label class=\"btn btn-default\"\n" +
    "               ng-model=\"characterModel\"\n" +
    "               ng-change=\"detailPaneConfig.configuration[configIndex].value=';'\"\n" +
    "               btn-radio=\"';'\">Semicolon</label>\n" +
    "        <label class=\"btn btn-default\"\n" +
    "               ng-model=\"characterModel\"\n" +
    "               ng-change=\"detailPaneConfig.configuration[configIndex].value=','\"\n" +
    "               btn-radio=\"','\">Comma</label>\n" +
    "        <label class=\"btn btn-default\"\n" +
    "               ng-model=\"characterModel\"\n" +
    "               ng-change=\"detailPaneConfig.configuration[configIndex].value=' '\"\n" +
    "               btn-radio=\"' '\">Space</label>\n" +
    "        <label class=\"btn btn-default\"\n" +
    "               ng-model=\"characterModel\"\n" +
    "               ng-change=\"detailPaneConfig.configuration[configIndex].value=null\"\n" +
    "               btn-radio=\"'Other'\">Other</label>\n" +
    "      </div>\n" +
    "\n" +
    "      <input type=\"text\" class=\"form-control character-type pull-left\" name=\"{{configDefinition.name}}\"\n" +
    "             focus-me=\"{{autoFocusConfigName === configDefinition.name}}\"\n" +
    "             ng-if=\"characterModel === 'Other'\"\n" +
    "             ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "             ng-model=\"detailPaneConfig.configuration[configIndex].value\"\n" +
    "             ng-required=\"configDefinition.required\">\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"booleanConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-7 col-md-8\">\n" +
    "    <input type=\"checkbox\" name=\"{{configDefinition.name}}\"\n" +
    "           ng-disabled=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "           ng-model=\"detailPaneConfig.configuration[configIndex].value\">\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"integerConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-7 col-md-8\">\n" +
    "    <input type=\"number\" class=\"form-control\" name=\"{{configDefinition.name}}\" min=\"{{configDefinition.min}}\" max=\"{{configDefinition.max}}\"\n" +
    "           focus-me=\"{{autoFocusConfigName === configDefinition.name}}\"\n" +
    "           ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "           ng-model=\"detailPaneConfig.configuration[configIndex].value\"\n" +
    "           ng-required=\"configDefinition.required\">\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"listConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-7 col-md-8 container-fluid\"\n" +
    "       ng-init=\"bulkEdit=(detailPaneConfig.configuration[configIndex].value.length > 9)\">\n" +
    "\n" +
    "    <div class=\"row map-list-type\"\n" +
    "         ng-if=\"!bulkEdit\"\n" +
    "         ng-repeat=\"val in listValue = detailPaneConfig.configuration[configIndex].value track by $index\">\n" +
    "\n" +
    "      <div class=\"col-xs-1\">\n" +
    "        <label class=\"lane-label\">{{$index + 1}}</label>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"col-xs-7\">\n" +
    "        <div ui-codemirror\n" +
    "             ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "             ng-model=\"listValue[$index]\"\n" +
    "             ui-refresh=\"refreshCodemirror\"\n" +
    "             ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                extraKeys: {\n" +
    "                  'Tab': false,\n" +
    "                  'Ctrl-Space': 'autocomplete'\n" +
    "                }\n" +
    "              }, configDefinition)\"\n" +
    "             codemirror-el\n" +
    "             field-paths=\"fieldPaths\"\n" +
    "             d-field-paths=\"dFieldPaths\"\n" +
    "             data-type=\"'STRING'\">\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"col-xs-4 btn-group\" role=\"group\" aria-label=\"Default button group\"\n" +
    "           ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-click=\"removeFromList(detailPaneConfig, detailPaneConfig.configuration[configIndex].value, $index)\">\n" +
    "          <i class=\"fa fa-minus pointer\"></i>\n" +
    "        </button>\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-show=\"$last\"\n" +
    "                ng-click=\"addToList(detailPaneConfig, detailPaneConfig.configuration[configIndex].value)\">\n" +
    "          <i class=\"fa fa-plus pointer\"></i>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div ui-codemirror\n" +
    "         ng-if=\"bulkEdit\"\n" +
    "         class=\"codemirror-editor\"\n" +
    "         ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "         focus-me=\"{{autoFocusConfigName === configDefinition.name}}\"\n" +
    "         ng-model=\"detailPaneConfig.configuration[configIndex].value\"\n" +
    "         ui-refresh=\"refreshCodemirror\"\n" +
    "         ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                autofocus: autoFocusConfigName === configDefinition.name,\n" +
    "                lineNumbers: true\n" +
    "              }, configDefinition)\"\n" +
    "         codemirror-el\n" +
    "         field-paths=\"fieldPaths\"\n" +
    "         d-field-paths=\"dFieldPaths\"\n" +
    "         data-type=\"'LIST'\">\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"detailPaneConfig.configuration[configIndex].value.length === 0 || bulkEdit\" class=\"row\"\n" +
    "         ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "      <div class=\"col-md-2 btn-group\" role=\"group\">\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-click=\"addToList(detailPaneConfig, detailPaneConfig.configuration[configIndex].value)\">\n" +
    "          <i class=\"fa fa-plus pointer\"></i>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right field-selector-btn\" ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "      <button type=\"button\" class=\"btn btn-link\"\n" +
    "              ng-click=\"bulkEdit = !bulkEdit\">\n" +
    "        {{bulkEdit ? ('home.detailPane.configurationTab.switchToSimpleMode' | translate) : ('home.detailPane.configurationTab.switchToBulkMode' | translate)}}\n" +
    "      </button>\n" +
    "    </div>\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"mapConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-7 col-md-8\"\n" +
    "       ng-init=\"bulkEdit=(detailPaneConfig.configuration[configIndex].value.length > 9)\">\n" +
    "    <div class=\"row map-list-type\"\n" +
    "         ng-if=\"!bulkEdit\"\n" +
    "         ng-repeat=\"mapObject in detailPaneConfig.configuration[configIndex].value track by $index\">\n" +
    "\n" +
    "      <div class=\"col-xs-4\">\n" +
    "        <input type=\"text\" class=\"form-control input-sm\"\n" +
    "               name=\"{{configDefinition.name + 'key' + $index}}\"\n" +
    "               placeholder=\"{{'home.detailPane.configurationTab.enterKey' | translate}}\"\n" +
    "               ng-required=\"true\"\n" +
    "               ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "               ng-model=\"mapObject.key\"/>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"col-xs-1\">:</div>\n" +
    "\n" +
    "      <div class=\"col-xs-5\">\n" +
    "        <div ui-codemirror\n" +
    "             ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "             ng-model=\"mapObject.value\"\n" +
    "             ui-refresh=\"refreshCodemirror\"\n" +
    "             ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                extraKeys: {\n" +
    "                  'Tab': false,\n" +
    "                  'Ctrl-Space': 'autocomplete'\n" +
    "                }\n" +
    "              }, configDefinition)\"\n" +
    "             codemirror-el\n" +
    "             field-paths=\"fieldPaths\"\n" +
    "             d-field-paths=\"dFieldPaths\"\n" +
    "             data-type=\"'STRING'\">\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"btn-group\" role=\"group\" aria-label=\"Default button group\"\n" +
    "           ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-click=\"removeFromMap(detailPaneConfig, detailPaneConfig.configuration[configIndex].value, mapObject, $index)\">\n" +
    "          <i class=\"fa fa-minus pointer\"></i>\n" +
    "        </button>\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-show=\"$last\"\n" +
    "                ng-click=\"addToMap(detailPaneConfig, detailPaneConfig.configuration[configIndex].value)\">\n" +
    "          <i class=\"fa fa-plus pointer\"></i>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div ui-codemirror\n" +
    "         ng-if=\"bulkEdit\"\n" +
    "         class=\"codemirror-editor\"\n" +
    "         ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "         focus-me=\"{{autoFocusConfigName === configDefinition.name}}\"\n" +
    "         ng-model=\"detailPaneConfig.configuration[configIndex].value\"\n" +
    "         ui-refresh=\"refreshCodemirror\"\n" +
    "         ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                autofocus: autoFocusConfigName === configDefinition.name,\n" +
    "                lineNumbers: true\n" +
    "              }, configDefinition)\"\n" +
    "         codemirror-el\n" +
    "         field-paths=\"fieldPaths\"\n" +
    "         d-field-paths=\"dFieldPaths\"\n" +
    "         data-type=\"'LIST'\">\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"detailPaneConfig.configuration[configIndex].value.length === 0 || bulkEdit\" class=\"row\"\n" +
    "         ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "      <div class=\"col-md-2 btn-group\" role=\"group\">\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-click=\"addToMap(detailPaneConfig, detailPaneConfig.configuration[configIndex].value)\">\n" +
    "          <i class=\"fa fa-plus pointer\"></i>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right field-selector-btn\" ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "      <button type=\"button\" class=\"btn btn-link\"\n" +
    "              ng-click=\"bulkEdit = !bulkEdit\">\n" +
    "        {{bulkEdit ? ('home.detailPane.configurationTab.switchToSimpleMode' | translate) : ('home.detailPane.configurationTab.switchToBulkMode' | translate)}}\n" +
    "      </button>\n" +
    "    </div>\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"valueChooserConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-7 col-md-8\">\n" +
    "   <select class=\"form-control\"\n" +
    "            name=\"{{configDefinition.name}}\"\n" +
    "            focus-me=\"{{autoFocusConfigName === configDefinition.name}}\"\n" +
    "            ng-required=\"configDefinition.required\"\n" +
    "            ng-disabled=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "            ng-model=\"detailPaneConfig.configuration[configIndex].value\"\n" +
    "            ng-init=\"entities = getValueChooserOptions(detailPaneConfig, configDefinition)\">\n" +
    "      <option ng-repeat=\"entity in entities\"\n" +
    "              value=\"{{entity.value}}\"\n" +
    "              ng-selected=\"entity.value === detailPaneConfig.configuration[configIndex].value\">{{entity.label}}</option>\n" +
    "    </select>\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"multiValueChooserConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-7 col-md-8\">\n" +
    "    <ui-select multiple\n" +
    "               ng-disabled=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "               ng-model=\"detailPaneConfig.configuration[configIndex].value\">\n" +
    "      <ui-select-match class=\"ui-select-match\">\n" +
    "        {{$item}}\n" +
    "      </ui-select-match>\n" +
    "      <ui-select-choices class=\"ui-select-choices\" repeat=\"listValue in configDefinition.model.values| filter:$select.search\">\n" +
    "        <div ng-bind-html=\"listValue | highlight: $select.search\"></div>\n" +
    "      </ui-select-choices>\n" +
    "    </ui-select>\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"fieldSelectorMultiValuedConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-7 col-md-8\">\n" +
    "    <ui-select multiple\n" +
    "               tagging\n" +
    "               tagging-label=\" - new field path\"\n" +
    "               ng-disabled=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "               tagging-tokens=\",|ENTER\"\n" +
    "               ng-model=\"detailPaneConfig.configuration[configIndex].value\">\n" +
    "      <ui-select-match class=\"ui-select-match\">{{$item}}</ui-select-match>\n" +
    "      <ui-select-choices class=\"ui-select-choices\"\n" +
    "        repeat=\"fieldPath in fieldSelectorPaths.concat(detailPaneConfig.configuration[configIndex].value) | filter:$select.search\">\n" +
    "        <div ng-bind-html=\"fieldPath | highlight: $select.search\"></div>\n" +
    "      </ui-select-choices>\n" +
    "    </ui-select>\n" +
    "\n" +
    "    <div class=\"pull-right field-selector-btn\" ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "      <button type=\"button\" class=\"btn btn-link\"\n" +
    "              translate=\"home.detailPane.configurationTab.requiredFieldsSelectionButton\"\n" +
    "              ng-click=\"showFieldSelectorModal(detailPaneConfig.configuration[configIndex])\">\n" +
    "        Select Field Using Preview Data\n" +
    "      </button>\n" +
    "    </div>\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"fieldSelectorSingleValuedConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-7 col-md-8\">\n" +
    "    <div class=\"input-group\">\n" +
    "      <input type=\"text\" class=\"form-control\" ng-model=\"detailPaneConfig.configuration[configIndex].value\">\n" +
    "      <div class=\"input-group-btn\">\n" +
    "        <button type=\"button\" class=\"btn btn-default dropdown-toggle\" data-toggle=\"dropdown\"><span class=\"caret\"></span></button>\n" +
    "        <ul class=\"dropdown-menu dropdown-menu-right\" role=\"menu\">\n" +
    "          <li ng-repeat=\"fieldPath in fieldSelectorPaths track by $index\">\n" +
    "            <a href=\"#\" ng-click=\"detailPaneConfig.configuration[configIndex].value = fieldPath\">{{fieldPath}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "              ng-if=\"fieldPathsFetchInProgress\"\n" +
    "              translate=\"home.detailPane.configurationTab.loading\">Loading...</li>\n" +
    "\n" +
    "          <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "              ng-if=\"!fieldPathsFetchInProgress && fieldSelectorPaths.length === 0\"\n" +
    "              translate=\"home.detailPane.configurationTab.noFieldFound\">No Field found.</li>\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"lanePredicateMappingConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-9 col-md-8\">\n" +
    "    <div class=\"map-list-type\"\n" +
    "         ng-repeat=\"lanePredicateMapping in detailPaneConfig.configuration[configIndex].value track by $index\">\n" +
    "\n" +
    "      <div class=\"col-xs-1\">\n" +
    "        <!--input type=\"text\" readonly class=\"form-control input-sm\"\n" +
    "               value=\"{{$index + 1}}\"/-->\n" +
    "        <label class=\"lane-label\">{{$index + 1}}</label>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"col-xs-9 expression-language\">\n" +
    "\n" +
    "        <div ui-codemirror\n" +
    "             ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning || lanePredicateMapping.predicate === 'default'}\"\n" +
    "             ng-model=\"lanePredicateMapping.predicate\"\n" +
    "             ui-refresh=\"refreshCodemirror\"\n" +
    "             ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning || lanePredicateMapping.predicate === 'default'),\n" +
    "                extraKeys: {\n" +
    "                  'Tab': false,\n" +
    "                  'Ctrl-Space': 'autocomplete'\n" +
    "                }\n" +
    "              }, configDefinition)\"\n" +
    "             codemirror-el\n" +
    "             field-paths=\"fieldPaths\"\n" +
    "             d-field-paths=\"dFieldPaths\"\n" +
    "             data-type=\"'PREDICATE'\">\n" +
    "        </div>\n" +
    "\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"col-xs-1\" role=\"group\" aria-label=\"Default button group\"\n" +
    "           ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "        <button type=\"button\" class=\"btn btn-default\"\n" +
    "                ng-hide=\"$last\"\n" +
    "                ng-click=\"removeLane(detailPaneConfig, detailPaneConfig.configuration[configIndex].value, lanePredicateMapping, $index)\">\n" +
    "          <i class=\"fa fa-minus pointer\"></i>\n" +
    "        </button>\n" +
    "        <button type=\"button\" class=\"btn btn-default\"\n" +
    "                ng-show=\"$last\"\n" +
    "                ng-click=\"addLane(detailPaneConfig, detailPaneConfig.configuration[configIndex].value, lanePredicateMapping)\">\n" +
    "          <i class=\"fa fa-plus pointer\"></i>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"stringComplexConfigurationTemplate\">\n" +
    "  <input type=\"text\" class=\"form-control\"\n" +
    "         name=\"{{customFieldConfigDefinition.name + $index}}\"\n" +
    "         placeholder=\"{{customFieldConfigDefinition.label}}\"\n" +
    "         ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "         ng-model=\"customFieldValue[customFieldConfigDefinition.name]\"\n" +
    "         ng-required=\"customFieldConfigDefinition.required\">\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"booleanComplexConfigurationTemplate\">\n" +
    "  <div class=\"controls\">\n" +
    "    <input type=\"checkbox\" name=\"{{customFieldConfigDefinition.name}}\"\n" +
    "           ng-disabled=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "           ng-model=\"customFieldValue[customFieldConfigDefinition.name]\">\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"elStringComplexConfigurationTemplate\">\n" +
    "  <div ui-codemirror\n" +
    "       ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "       ng-model=\"customFieldValue[customFieldConfigDefinition.name]\"\n" +
    "       ui-refresh=\"refreshCodemirror\"\n" +
    "       ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                extraKeys: {\n" +
    "                  'Tab': false,\n" +
    "                  'Enter': common.ignoreCodeMirrorEnterKey,\n" +
    "                  'Ctrl-Space': 'autocomplete'\n" +
    "                }\n" +
    "              }, customFieldConfigDefinition)\"\n" +
    "       codemirror-el=\"getCodeMirrorHints(customFieldConfigDefinition)\"\n" +
    "       field-paths=\"fieldPaths\"\n" +
    "       d-field-paths=\"dFieldPaths\"\n" +
    "       data-type=\"customFieldConfigDefinition.type\">\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"elCredentialComplexConfigurationTemplate\">\n" +
    "  <div ng-init=\"showValue=(!customFieldValue[customFieldConfigDefinition.name] || customFieldValue[customFieldConfigDefinition.name].indexOf('${') === 0)\">\n" +
    "    <div ui-codemirror\n" +
    "         ng-if=\"showValue\"\n" +
    "         ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "         ng-model=\"customFieldValue[customFieldConfigDefinition.name]\"\n" +
    "         ui-refresh=\"refreshCodemirror\"\n" +
    "         ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                extraKeys: {\n" +
    "                  'Tab': false,\n" +
    "                  'Enter': common.ignoreCodeMirrorEnterKey,\n" +
    "                  'Ctrl-Space': 'autocomplete'\n" +
    "                }\n" +
    "              }, customFieldConfigDefinition)\"\n" +
    "         codemirror-el=\"getCodeMirrorHints(customFieldConfigDefinition)\"\n" +
    "         field-paths=\"fieldPaths\"\n" +
    "         d-field-paths=\"dFieldPaths\"\n" +
    "         data-type=\"customFieldConfigDefinition.type\">\n" +
    "    </div>\n" +
    "\n" +
    "    <input type=\"password\" class=\"form-control\"\n" +
    "           ng-if=\"!showValue\"\n" +
    "           name=\"{{customFieldConfigDefinition.name}}\"\n" +
    "           focus-me=\"{{autoFocusConfigName === customFieldConfigDefinition.name}}\"\n" +
    "           ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "           ng-model=\"customFieldValue[customFieldConfigDefinition.name]\"\n" +
    "           ng-required=\"customFieldConfigDefinition.required\">\n" +
    "\n" +
    "\n" +
    "    <div class=\"pull-right field-selector-btn\" ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "      <button type=\"button\" class=\"btn btn-link\"\n" +
    "              ng-click=\"showValue = !showValue\">\n" +
    "        {{showValue ? ('global.form.hideValue' | translate) : ('global.form.showValue' | translate)}}\n" +
    "      </button>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"listComplexConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-12 container-fluid\">\n" +
    "    <div class=\"row map-list-type\"\n" +
    "         ng-repeat=\"val in listValue = customFieldValue[customFieldConfigDefinition.name] track by $index\">\n" +
    "\n" +
    "      <div class=\"col-xs-1\">\n" +
    "        <label class=\"lane-label\">{{$index + 1}}</label>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"col-xs-7\">\n" +
    "        <div ui-codemirror\n" +
    "             ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "             ng-model=\"listValue[$index]\"\n" +
    "             ui-refresh=\"refreshCodemirror\"\n" +
    "             ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                extraKeys: {\n" +
    "                  'Tab': false,\n" +
    "                  'Ctrl-Space': 'autocomplete'\n" +
    "                }\n" +
    "              }, configDefinition)\"\n" +
    "             codemirror-el\n" +
    "             field-paths=\"fieldPaths\"\n" +
    "             d-field-paths=\"dFieldPaths\"\n" +
    "             data-type=\"'STRING'\">\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"col-xs-4 btn-group\" role=\"group\" aria-label=\"Default button group\"\n" +
    "           ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-click=\"removeFromList(detailPaneConfig, customFieldValue[customFieldConfigDefinition.name], $index)\">\n" +
    "          <i class=\"fa fa-minus pointer\"></i>\n" +
    "        </button>\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-show=\"$last\"\n" +
    "                ng-click=\"addToList(detailPaneConfig, customFieldValue[customFieldConfigDefinition.name])\">\n" +
    "          <i class=\"fa fa-plus pointer\"></i>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"customFieldValue[customFieldConfigDefinition.name].length === 0\" class=\"row\"\n" +
    "         ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "      <div class=\"col-md-2 btn-group\" role=\"group\">\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-click=\"addToList(detailPaneConfig, customFieldValue[customFieldConfigDefinition.name])\">\n" +
    "          <i class=\"fa fa-plus pointer\"></i>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"mapComplexConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-12\">\n" +
    "    <div class=\"row map-list-type\" ng-repeat=\"mapObject in customFieldValue[customFieldConfigDefinition.name] track by $index\">\n" +
    "\n" +
    "      <div class=\"col-xs-4\">\n" +
    "        <input type=\"text\" class=\"form-control input-sm\"\n" +
    "               name=\"{{configDefinition.name + 'key' + $index}}\"\n" +
    "               placeholder=\"{{'home.detailPane.configurationTab.enterKey' | translate}}\"\n" +
    "               ng-required=\"true\"\n" +
    "               ng-readonly=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "               ng-model=\"mapObject.key\"/>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"col-xs-1\">:</div>\n" +
    "\n" +
    "      <div class=\"col-xs-5\">\n" +
    "        <div ui-codemirror\n" +
    "             ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "             ng-model=\"mapObject.value\"\n" +
    "             ui-refresh=\"refreshCodemirror\"\n" +
    "             ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                extraKeys: {\n" +
    "                  'Tab': false,\n" +
    "                  'Ctrl-Space': 'autocomplete'\n" +
    "                }\n" +
    "              }, configDefinition)\"\n" +
    "             codemirror-el\n" +
    "             field-paths=\"fieldPaths\"\n" +
    "             d-field-paths=\"dFieldPaths\"\n" +
    "             data-type=\"'STRING'\">\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"btn-group\" role=\"group\" aria-label=\"Default button group\"\n" +
    "           ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-click=\"removeFromMap(detailPaneConfig, customFieldValue[customFieldConfigDefinition.name], mapObject, $index)\">\n" +
    "          <i class=\"fa fa-minus pointer\"></i>\n" +
    "        </button>\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-show=\"$last\"\n" +
    "                ng-click=\"addToMap(detailPaneConfig, customFieldValue[customFieldConfigDefinition.name])\">\n" +
    "          <i class=\"fa fa-plus pointer\"></i>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"customFieldValue[customFieldConfigDefinition.name].length === 0\" class=\"row\"\n" +
    "         ng-hide=\"isPipelineReadOnly || isPipelineRunning\">\n" +
    "      <div class=\"col-md-2 btn-group\" role=\"group\">\n" +
    "        <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                ng-click=\"addToMap(detailPaneConfig, customFieldValue[customFieldConfigDefinition.name])\">\n" +
    "          <i class=\"fa fa-plus pointer\"></i>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"textConfigurationTemplate\">\n" +
    "  <div class=\"controls col-lg-9 col-md-8\">\n" +
    "\n" +
    "    <div ui-codemirror\n" +
    "         class=\"codemirror-editor\"\n" +
    "         ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "         focus-me=\"{{autoFocusConfigName === configDefinition.name}}\"\n" +
    "         ng-model=\"detailPaneConfig.configuration[configIndex].value\"\n" +
    "         ui-refresh=\"refreshCodemirror\"\n" +
    "         ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                mode: {\n" +
    "                  name: configDefinition.mode\n" +
    "                },\n" +
    "                lineNumbers: true\n" +
    "              }, configDefinition)\"\n" +
    "         codemirror-el\n" +
    "         field-paths=\"fieldPaths\"\n" +
    "         d-field-paths=\"dFieldPaths\">\n" +
    "    </div>\n" +
    "\n" +
    "    <ng-include src=\"'configurationIssuesTemplate'\"></ng-include>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"textComplexConfigurationTemplate\">\n" +
    "  <div ui-codemirror\n" +
    "       class=\"codemirror-editor\"\n" +
    "       ng-class=\"{'codemirror-read-only': isPipelineReadOnly || isPipelineRunning}\"\n" +
    "       focus-me=\"{{autoFocusConfigName === customFieldConfigDefinition.name}}\"\n" +
    "       ng-model=\"customFieldValue[customFieldConfigDefinition.name]\"\n" +
    "       ui-refresh=\"refreshCodemirror\"\n" +
    "       ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                readOnly: (isPipelineReadOnly || isPipelineRunning),\n" +
    "                mode: {\n" +
    "                  name: customFieldConfigDefinition.mode\n" +
    "                },\n" +
    "                lineNumbers: true\n" +
    "              }, customFieldConfigDefinition)\"\n" +
    "       codemirror-el\n" +
    "       field-paths=\"fieldPaths\"\n" +
    "       d-field-paths=\"dFieldPaths\">\n" +
    "  </div>\n" +
    "</script>\n" +
    "");
}]);

angular.module("app/home/detail/dataSummary/dataSummary.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/dataSummary/dataSummary.tpl.html",
    "<div class=\"container-fluid data-summary-tab\" ng-controller=\"DataSummaryController\">\n" +
    "\n" +
    "  <div class=\"row\"\n" +
    "       ng-repeat=\"dataRuleDefn in dataRuleDefinitions = (allDataRuleDefinitions | filter: {lane: selectedObject.outputLane, enabled: true})\">\n" +
    "\n" +
    "    <div class=\"col-sm-12 col-md-12 col-lg-12\">\n" +
    "      <div class=\"panel panel-default\">\n" +
    "        <div class=\"panel-heading\">\n" +
    "          <span>{{dataRuleDefn.label}}</span>\n" +
    "          <span>(Condition - {{dataRuleDefn.condition}} )</span>\n" +
    "        </div>\n" +
    "        <div class=\"panel-body rule-panel-body\">\n" +
    "\n" +
    "\n" +
    "          <!-- Triggered Alert -->\n" +
    "          <div class=\"col-md-12 col-lg-12 triggered-alerts\"\n" +
    "               ng-repeat=\"triggeredAlert in laneAlerts  = (triggeredAlerts | filter : {ruleDefinition: {id : dataRuleDefn.id}})\">\n" +
    "            <div class=\"alert alert-danger clearfix\" role=\"alert\">\n" +
    "              <i class=\"fa fa-exclamation-triangle\"></i>\n" +
    "\n" +
    "              <span ng-if=\"triggeredAlert.gauge.value.exceptionMessage\"\n" +
    "                     ng-bind=\"triggeredAlert.gauge.value.exceptionMessage\"></span>\n" +
    "\n" +
    "              <span ng-if=\"!triggeredAlert.gauge.value.exceptionMessage && triggeredAlert.ruleDefinition.thresholdType\"\n" +
    "                    ng-bind=\"triggeredAlert.ruleDefinition.alertText\"></span>\n" +
    "\n" +
    "              <span ng-if=\"!triggeredAlert.gauge.value.exceptionMessage && !triggeredAlert.ruleDefinition.thresholdType && triggeredAlert.gauge.value.alertTexts\"\n" +
    "                    class=\"alert-text\" ng-repeat=\"alertText in triggeredAlert.gauge.value.alertTexts track by $index\">{{alertText}}</span>\n" +
    "\n" +
    "              <span ng-if=\"!triggeredAlert.gauge.value.exceptionMessage && !alert.gauge.value.alertTexts\"\n" +
    "                class=\"alert-details\"> ( {{'home.detailPane.summaryTab.currentValue' | translate}}: {{triggeredAlert.gauge.value.currentValue}}, {{'home.detailPane.summaryTab.triggered' | translate}}: {{triggeredAlert.gauge.value.timestamp | amTimeAgo}} )</span>\n" +
    "\n" +
    "              <div class=\"pull-right\">\n" +
    "                <a href=\"#\"\n" +
    "                   ng-click=\"deleteTriggeredAlert(triggeredAlert)\">\n" +
    "                  <span translate=\"global.form.delete\">Delete</span>\n" +
    "                </a>\n" +
    "                &nbsp;|&nbsp;\n" +
    "                <a href=\"#\"\n" +
    "                   ng-click=\"selectRulesTab(triggeredAlert)\">\n" +
    "                  <span translate=\"global.form.changeRule\">Change Rule</span>\n" +
    "                </a>\n" +
    "              </div>\n" +
    "\n" +
    "            </div>\n" +
    "          </div>\n" +
    "\n" +
    "\n" +
    "          <!-- Sampling Records -->\n" +
    "          <div class=\"col-md-6 col-lg-6\" ng-controller=\"DataSummarySamplingController\">\n" +
    "            <table class=\"table fixed-layout\">\n" +
    "              <thead>\n" +
    "              <tr>\n" +
    "                <th class=\"col-md-4\">\n" +
    "                  <div class=\"pull-left\">\n" +
    "                    <span>\n" +
    "                      <span translate=\"global.form.sampleRecords\">Sample Records</span>\n" +
    "                      (<a href=\"#\" ng-click=\"downloadSamplingRecords($event)\">{{samplingRecordsSizeInBytes | friendlyFileSize}}</a>)\n" +
    "                    </span>\n" +
    "                    <i class=\"fa fa-refresh pointer icon-button\"\n" +
    "                       ng-click=\"refreshSamplingRecords()\"\n" +
    "                       tooltip-placement=\"right\"\n" +
    "                       tooltip=\"{{'global.form.refresh' | translate}}\"></i>\n" +
    "                  </div>\n" +
    "\n" +
    "                  <div class=\"pull-right btn-group toggle-toolbar\">\n" +
    "                    <label class=\"btn btn-default btn-sm\" ng-model=\"sampledRecordsType\"\n" +
    "                           tooltip-placement=\"bottom\"\n" +
    "                           tooltip-append-to-body=\"true\"\n" +
    "                           tooltip=\"{{'home.detailPane.dataSummaryTab.allSampledRecordsTooltip' | translate}}\"\n" +
    "                           btn-radio=\"'all'\">{{'home.detailPane.dataSummaryTab.allSampledRecords' | translate}}</label>\n" +
    "                    <label class=\"btn btn-default btn-sm\" ng-model=\"sampledRecordsType\"\n" +
    "                           tooltip-placement=\"bottom\"\n" +
    "                           tooltip-append-to-body=\"true\"\n" +
    "                           tooltip=\"{{'home.detailPane.dataSummaryTab.matchedSampledRecordsTooltip' | translate}}\"\n" +
    "                           btn-radio=\"'matched'\">{{'home.detailPane.dataSummaryTab.matchedSampledRecords' | translate}}</label>\n" +
    "                    <label class=\"btn btn-default btn-sm\" ng-model=\"sampledRecordsType\"\n" +
    "                           tooltip-placement=\"bottom\"\n" +
    "                           tooltip-append-to-body=\"true\"\n" +
    "                           tooltip=\"{{'home.detailPane.dataSummaryTab.notMatchedSampledRecordsTooltip' | translate}}\"\n" +
    "                           btn-radio=\"'notMatched'\">{{'home.detailPane.dataSummaryTab.notMatchedSampledRecords' | translate}}</label>\n" +
    "                  </div>\n" +
    "\n" +
    "                </th>\n" +
    "              </tr>\n" +
    "              </thead>\n" +
    "              <tbody ng-hide=\"showBadRecordsLoading\">\n" +
    "              <tr ng-repeat=\"sampledRecord in filteredSampledRecords = (samplingRecords | filter: filterSampledRecords)\">\n" +
    "                <td>\n" +
    "                  <record-tree\n" +
    "                    record=\"sampledRecord.record\"\n" +
    "                    record-value=\"sampledRecord.record.value\"\n" +
    "                    field-name=\"getRecordHeader(sampledRecord, $index)\"\n" +
    "                    is-root=\"true\"\n" +
    "                    editable=\"false\">\n" +
    "                  </record-tree>\n" +
    "                </td>\n" +
    "              </tr>\n" +
    "\n" +
    "              <tr ng-if=\"filteredSampledRecords.length === 0\">\n" +
    "                <td class=\"no-records text-center\"\n" +
    "                    translate=\"home.previewPane.noRecords\">No Records to view.</td>\n" +
    "              </tr>\n" +
    "\n" +
    "              </tbody>\n" +
    "\n" +
    "            </table>\n" +
    "            <div show-loading=\"showRecordsLoading\"></div>\n" +
    "          </div>\n" +
    "\n" +
    "\n" +
    "          <!-- Meter -->\n" +
    "          <div class=\"col-md-6 col-lg-6\"\n" +
    "               ng-if=\"dataRuleDefn.meterEnabled\"\n" +
    "               ng-controller=\"DataSummaryMeterController\">\n" +
    "\n" +
    "            <ul class=\"properties\">\n" +
    "              <li>\n" +
    "                <span class=\"properties-label\">Count:</span>\n" +
    "                <span class=\"properties-value\">{{count}}</span>\n" +
    "              </li>\n" +
    "            </ul>\n" +
    "\n" +
    "            <nvd3 options=\"chartOptions\" data=\"chartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "          </div>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"row\" ng-if=\"dataRuleDefinitions.length === 0\">\n" +
    "    <div class=\"col-md-12\" >\n" +
    "      <div class=\"alert alert-info\" role=\"alert\">\n" +
    "        <i class=\"fa fa-info-circle\"></i>\n" +
    "        <span>{{'home.detailPane.dataSummaryTab.noRulesMessage' | translate}}</span>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "</div>");
}]);

angular.module("app/home/detail/detail.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/detail.tpl.html",
    "<div class=\"panel panel-default detail-pane\" ng-if=\"(!previewMode && !snapshotMode)\" ng-controller=\"DetailController\">\n" +
    "\n" +
    "  <div class=\"panel-heading clearfix\">\n" +
    "\n" +
    "    <h3 class=\"panel-title pull-left\"\n" +
    "        ng-if=\"isPipelineRunning && selectedType !== pipelineConstant.LINK\">{{'home.detailPane.monitoring' | translate}}:</h3>\n" +
    "\n" +
    "    <h3 class=\"panel-title pull-left\"\n" +
    "        ng-if=\"isPipelineRunning && selectedType === pipelineConstant.LINK\">{{'home.detailPane.inspectingData' | translate}}:</h3>\n" +
    "\n" +
    "    <div class=\"btn-group pull-left detail-name-dropdown\">\n" +
    "      <button class=\"btn btn-link dropdown-toggle\" type=\"button\" data-toggle=\"dropdown\">\n" +
    "        <span>{{getDetailPaneLabel()}}</span>\n" +
    "\n" +
    "        <span class=\"caret\"></span>\n" +
    "      </button>\n" +
    "      <ul class=\"dropdown-menu\" role=\"menu\" aria-labelledby=\"dropdownMenu1\">\n" +
    "\n" +
    "        <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "            translate=\"global.form.pipeline\">Pipeline</li>\n" +
    "\n" +
    "        <li role=\"presentation\">\n" +
    "          <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "             ng-click=\"changeStageSelection({selectedObject: undefined, type: pipelineConstant.PIPELINE})\">{{pipelineConfig.info.title}}</a>\n" +
    "        </li>\n" +
    "\n" +
    "        <li role=\"presentation\" class=\"divider\"></li>\n" +
    "\n" +
    "        <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "            translate=\"global.form.stage\">Stage</li>\n" +
    "\n" +
    "        <li role=\"presentation\"\n" +
    "            ng-repeat=\"stageInstance in pipelineConfig.stages\">\n" +
    "          <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "             ng-click=\"changeStageSelection({selectedObject: stageInstance, type: pipelineConstant.STAGE_INSTANCE})\">{{stageInstance.uiInfo.label}}</a>\n" +
    "        </li>\n" +
    "      </ul>\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "    <div class=\"pull-right size-toolbar\">\n" +
    "\n" +
    "      <div class=\"btn-group pull-left detail-settings-dropdown\"\n" +
    "           ng-if=\"!$storage.dontShowRESTResponseMenu || isPipelineRunning\">\n" +
    "        <a class=\"btn btn-link dropdown-toggle\" data-toggle=\"dropdown\"\n" +
    "           tooltip-placement=\"top\"\n" +
    "           tooltip=\"{{'home.header.more' | translate}}\"\n" +
    "           tooltip-popup-delay=\"500\">\n" +
    "          <span class=\"fa fa-ellipsis-h fa-14x pointer\"></span>\n" +
    "        </a>\n" +
    "        <ul class=\"dropdown-menu pull-right\" role=\"menu\">\n" +
    "\n" +
    "          <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "              ng-if=\"!$storage.dontShowRESTResponseMenu\"\n" +
    "              translate=\"home.detailPane.restURL\">REST URL</li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"isPipelineRunning && !$storage.dontShowRESTResponseMenu\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"/controlpanel/dataflow/app/rest/{{common.apiVersion}}/pipeline/{{pipelineConfig.info.pipelineId}}/metrics?rev=0\"\n" +
    "               target=\"_blank\">{{'home.detailPane.monitoringJSONData' | translate}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"!$storage.dontShowRESTResponseMenu\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"/controlpanel/dataflow/app/rest/{{common.apiVersion}}/pipeline/{{pipelineConfig.info.pipelineId}}\"\n" +
    "               target=\"_blank\">{{'home.detailPane.pipelineConfigJSONData' | translate}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"!$storage.dontShowRESTResponseMenu\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"/controlpanel/dataflow/app/rest/{{common.apiVersion}}/pipeline/{{pipelineConfig.info.pipelineId}}/rules\"\n" +
    "               target=\"_blank\">{{'home.detailPane.pipelineRulesJSONData' | translate}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"!$storage.dontShowRESTResponseMenu\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"/controlpanel/dataflow/app/rest/{{common.apiVersion}}/pipeline/{{pipelineConfig.info.pipelineId}}/status?rev=0\"\n" +
    "               target=\"_blank\">{{'home.detailPane.pipelineStatusJSONData' | translate}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"!$storage.dontShowRESTResponseMenu\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"/controlpanel/dataflow/app/rest/{{common.apiVersion}}/definitions\"\n" +
    "               target=\"_blank\">{{'home.detailPane.definitionsJSONData' | translate}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"!$storage.dontShowRESTResponseMenu\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"/controlpanel/dataflow/app/rest/{{common.apiVersion}}/pipeline/{{pipelineConfig.info.pipelineId}}/committedOffsets\"\n" +
    "               target=\"_blank\">{{'home.detailPane.committedOffsets' | translate}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"isPipelineRunning && activeDetailTab.name == 'summary'\"\n" +
    "              ng-if=\"!$storage.dontShowRESTResponseMenu\"\n" +
    "              class=\"divider\"></li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"isPipelineRunning && activeDetailTab.name == 'summary'\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"launchSettings()\">{{'global.form.settings' | translate}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "      <span class=\"fa fa-question-circle fa-14x pointer\"\n" +
    "            tooltip-placement=\"top\"\n" +
    "            tooltip=\"{{'global.form.help' | translate}}\"\n" +
    "            tooltip-popup-delay=\"500\"\n" +
    "            ng-click=\"launchHelp()\"></span>\n" +
    "\n" +
    "      <span class=\"glyphicon pointer\"\n" +
    "            tooltip-placement=\"top\"\n" +
    "            tooltip=\"{{!$storage.minimizeDetailPane ? 'global.form.minimizePane' : 'global.form.maximizePane' | translate}}\"\n" +
    "            tooltip-popup-delay=\"1000\"\n" +
    "            ng-hide=\"$storage.maximizeDetailPane\"\n" +
    "            ng-class=\"{'glyphicon-minus' : !$storage.minimizeDetailPane , 'glyphicon-resize-full': $storage.minimizeDetailPane}\"\n" +
    "            ng-click=\"onMinimizeDetailPane()\"></span>\n" +
    "\n" +
    "      <span class=\"glyphicon pointer\"\n" +
    "            tooltip-placement=\"top\"\n" +
    "            tooltip=\"{{!$storage.maximizeDetailPane ? 'global.form.maximizePane' : 'global.form.minimizePane' | translate}}\"\n" +
    "            tooltip-popup-delay=\"1000\"\n" +
    "            ng-hide=\"$storage.minimizeDetailPane\"\n" +
    "            ng-class=\"{'glyphicon-fullscreen' : !$storage.maximizeDetailPane , 'glyphicon-resize-small': $storage.maximizeDetailPane}\"\n" +
    "            ng-click=\"onMaximizeDetailPane()\"></span>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"btn-group pull-right detail-time-range-dropdown\"\n" +
    "         ng-if=\"common.isMetricsTimeSeriesEnabled && isPipelineRunning && activeDetailTab.name == 'summary'\">\n" +
    "\n" +
    "      <button class=\"btn btn-link dropdown-toggle\" type=\"button\" data-toggle=\"dropdown\">\n" +
    "        <i class=\"fa fa-clock-o\"></i>\n" +
    "        <span>{{getTimeRangeLabel()}}</span>\n" +
    "        <span class=\"caret\"></span>\n" +
    "      </button>\n" +
    "\n" +
    "      <ul class=\"dropdown-menu pull-left\" role=\"menu\" aria-labelledby=\"dropdownMenu1\">\n" +
    "\n" +
    "        <li role=\"presentation\" ng-repeat=\"timeOption in timeOptions\">\n" +
    "          <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "             ng-click=\"changeTimeRange(timeOption)\">{{'home.detailPane.' + timeOption | translate}}</a>\n" +
    "        </li>\n" +
    "\n" +
    "      </ul>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"panel-body\">\n" +
    "    <tabset class=\"detail-tabs-left tabs-left\" ng-if=\"detailPaneConfigDefn\">\n" +
    "      <tab ng-repeat=\"tab in detailPaneTabs track by $index\" active=\"tab.active\" disable=\"tab.disabled\"\n" +
    "           select=\"onTabSelect(tab)\">\n" +
    "        <tab-heading>  <!--tooltip-placement=\"right\" tooltip=\"{{tab.label}}\" -->\n" +
    "          <i class=\"fa fa-exclamation-triangle icon-danger\"\n" +
    "             ng-show=\"showWarning(tab)\"></i>\n" +
    "\n" +
    "          <i class=\"{{tab.iconClass}}\"\n" +
    "             ng-hide=\"showWarning(tab)\"></i>\n" +
    "\n" +
    "          <span>{{'home.detailPane.' + tab.name | translate}}</span>\n" +
    "        </tab-heading>\n" +
    "        <div ng-if=\"tab.active\" ng-include=\"tab.template\"></div>\n" +
    "      </tab>\n" +
    "    </tabset>\n" +
    "\n" +
    "    <div class=\"row\" ng-if=\"detailPaneConfig && !detailPaneConfigDefn\">\n" +
    "\n" +
    "      <div class=\"row\">\n" +
    "        <div class=\"col-md-12\" >\n" +
    "          <div class=\"alert alert-danger\" role=\"alert\">\n" +
    "            <i class=\"fa fa-exclamation-triangle\"></i>\n" +
    "            <span translate=\"home.detailPane.noStageDefinitionFound\" translate-values=\"{stageName: detailPaneConfig.stageName, library: detailPaneConfig.library}\"></span>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"col-md-2 col-md-offset-1\" >\n" +
    "          <button class=\"btn btn-primary\" ng-click=\"onInstallMissingLibraryClick(detailPaneConfig.library)\">Install Missing Stage Library</button>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"form-group col-md-6\" ng-if=\"selectedType === pipelineConstant.STAGE_INSTANCE && stageLibraryList.length > 0\">\n" +
    "          <p class=\"col-md-1\">OR</p>\n" +
    "          <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.configurationTab.changeStageLibrary\">Change Stage Library</label>\n" +
    "          <div class=\"controls col-lg-7 col-md-8\">\n" +
    "            <select class=\"form-control\"\n" +
    "                    name=\"stageLibrary\"\n" +
    "                    ng-disabled=\"isPipelineReadOnly || isPipelineRunning\"\n" +
    "                    ng-model=\"detailPaneConfig.library\"\n" +
    "                    ng-options=\"item.library as item.libraryLabel for item in stageLibraryList\">\n" +
    "            </select>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "<ng-include src=\"'app/home/detail/configuration/fieldSelector/fieldSelectorModal.tpl.html'\"></ng-include>\n" +
    "");
}]);

angular.module("app/home/detail/history/clearHistory/clearHistory.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/history/clearHistory/clearHistory.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <button type=\"button\" class=\"close\" ng-click=\"no()\">\n" +
    "    <span aria-hidden=\"true\">&times;</span>\n" +
    "    <span class=\"sr-only\">Close</span>\n" +
    "  </button>\n" +
    "  <h3 class=\"modal-title\" translate=\"home.detailPane.historyTab.clearHistoryConfirmationTitle\">Clear History Confirmation</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "  <p translate=\"home.detailPane.historyTab.clearHistoryConfirmationMessage\" translate-values=\"{name: pipelineInfo.pipelineId}\">\n" +
    "  </p>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"button\" class=\"btn btn-default\" ng-click=\"no()\" translate=\"global.form.no\">\n" +
    "    No\n" +
    "  </button>\n" +
    "  <button type=\"button\" class=\"btn btn-primary\" ng-click=\"yes()\" translate=\"global.form.yes\">\n" +
    "    Yes\n" +
    "  </button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/detail/history/history.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/history/history.tpl.html",
    "<div class=\"history-tab\" ng-controller=\"HistoryController\">\n" +
    "\n" +
    "  <button type=\"button\" class=\"btn btn-primary btn-sm add-button pull-right\"\n" +
    "          ng-show=\"isAuthorized([userRoles.admin, userRoles.creator]) && !isPipelineRunning && runHistory.length\"\n" +
    "          ng-click=\"clearHistory()\">\n" +
    "    <i class=\"fa fa-trash-o\"></i> {{'home.detailPane.historyTab.clearHistory' | translate}}\n" +
    "  </button>\n" +
    "\n" +
    "  <table class=\"table\">\n" +
    "    <thead>\n" +
    "    <tr>\n" +
    "      <th class=\"col-md-2\" translate=\"global.form.lastStatusChange\">Last Status Change</th>\n" +
    "      <th class=\"col-md-1\" translate=\"global.form.user\">User</th>\n" +
    "      <th class=\"col-md-1\" translate=\"global.form.status\">Status</th>\n" +
    "      <th class=\"col-md-3\" translate=\"global.form.message\">Message</th>\n" +
    "      <th class=\"col-md-3\" translate=\"global.form.parameters\">Parameters</th>\n" +
    "      <th class=\"col-md-2\" translate=\"home.detailPane.summary\">Summary</th>\n" +
    "    </tr>\n" +
    "    </thead>\n" +
    "    <tbody ng-hide=\"showLoading\">\n" +
    "    <tr ng-repeat=\"history in runHistory track by $index\">\n" +
    "      <td>{{history.timeStamp | date:'medium'}}</td>\n" +
    "      <td>{{history.user}}</td>\n" +
    "      <td ng-bind=\"history.status\"></td>\n" +
    "      <td ng-bind=\"history.message\"></td>\n" +
    "      <td>\n" +
    "        <span ng-if=\"history.attributes && history.attributes['RUNTIME_PARAMETERS']\">{{history.attributes['RUNTIME_PARAMETERS'] | json}}</span>\n" +
    "      </td>\n" +
    "      <td>\n" +
    "        <a href=\"javascript:;\"\n" +
    "           ng-if=\"history.metrics\"\n" +
    "           translate=\"home.detailPane.historyTab.viewSummary\"\n" +
    "           ng-click=\"viewSummary(history, $index)\">View Summary ...</a>\n" +
    "      </td>\n" +
    "    </tr>\n" +
    "\n" +
    "    <tr ng-if=\"runHistory.length === 0\">\n" +
    "      <td colspan=\"3\" class=\"no-status text-center\" translate=\"home.detailPane.historyTab.noStatus\">No Status.</td>\n" +
    "    </tr>\n" +
    "\n" +
    "    </tbody>\n" +
    "  </table>\n" +
    "\n" +
    "  <div show-loading=\"showLoading\"></div>\n" +
    "\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/detail/history/summary/summaryModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/history/summary/summaryModal.tpl.html",
    "<form class=\"history-summary-modal\" role=\"form\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"close()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.detailPane.summary\">Summary</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <div class=\"btn-group\">\n" +
    "      <button class=\"btn btn-default dropdown-toggle\" type=\"button\" data-toggle=\"dropdown\">\n" +
    "        <span>{{getLabel()}}</span>\n" +
    "\n" +
    "        <span class=\"caret\"></span>\n" +
    "      </button>\n" +
    "      <ul class=\"dropdown-menu\" role=\"menu\" aria-labelledby=\"dropdownMenu1\">\n" +
    "\n" +
    "        <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "            translate=\"global.form.pipeline\">Pipeline</li>\n" +
    "\n" +
    "        <li role=\"presentation\">\n" +
    "          <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "             ng-click=\"changeStageSelection({selectedObject: pipelineConfig, type: pipelineConstant.PIPELINE})\">{{pipelineConfig.info.title}}</a>\n" +
    "        </li>\n" +
    "\n" +
    "        <li role=\"presentation\" class=\"divider\"></li>\n" +
    "\n" +
    "        <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "            translate=\"global.form.stage\">Stage</li>\n" +
    "\n" +
    "        <li role=\"presentation\"\n" +
    "            ng-repeat=\"stageInstance in pipelineConfig.stages\">\n" +
    "          <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "             ng-click=\"changeStageSelection({selectedObject: stageInstance, type: pipelineConstant.STAGE_INSTANCE})\">{{stageInstance.uiInfo.label}}</a>\n" +
    "        </li>\n" +
    "      </ul>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"row\">\n" +
    "      <div class=\"col-sm-6\" ng-controller=\"RecordCountBarChartController\">\n" +
    "        <div class=\"panel panel-default\">\n" +
    "          <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.historyTab.summaryModal.recordCountBarChartTitle' | translate}}\"\n" +
    "              translate=\"home.detailPane.historyTab.summaryModal.recordCountBarChartTitle\">Record Count</span>\n" +
    "          </div>\n" +
    "          <div class=\"panel-body\">\n" +
    "            <nvd3 options=\"chartOptions\" data=\"barChartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "\n" +
    "      <div class=\"col-sm-6\"\n" +
    "           ng-if=\"selectedType === pipelineConstant.PIPELINE\">\n" +
    "        <div class=\"panel panel-default\">\n" +
    "          <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.runtimeStatistics' | translate}}\"\n" +
    "              translate=\"home.detailPane.summary\">Summary</span>\n" +
    "          </div>\n" +
    "          <div class=\"panel-body\">\n" +
    "\n" +
    "            <ul class=\"table-properties\">\n" +
    "\n" +
    "              <li>\n" +
    "                <span class=\"properties-label\">{{'home.detailPane.summaryTab.pipelineStartTime' | translate}}:</span>\n" +
    "                <span class=\"properties-value\"> {{pipelineStartTime | date:'medium'}}</span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li>\n" +
    "                <span class=\"properties-label\">{{'home.detailPane.summaryTab.pipelineStopTime' | translate}}:</span>\n" +
    "                <span class=\"properties-value\"> {{pipelineStopTime | date:'medium'}}</span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li>\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.summaryTab.timeOfLastReceivedRecord' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\"\n" +
    "                  ng-if=\"common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.timeOfLastReceivedRecord\">\n" +
    "                  {{common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.timeOfLastReceivedRecord  | date:'medium'}}\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li>\n" +
    "                <span class=\"properties-label\">{{'home.detailPane.summaryTab.batchCount' | translate}}:</span>\n" +
    "                <span class=\"properties-value\">{{common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.batchCount}}</span>\n" +
    "              </li>\n" +
    "\n" +
    "            </ul>\n" +
    "\n" +
    "          </div>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"button\" class=\"btn btn-primary\" ng-click=\"close()\"\n" +
    "            translate=\"global.form.close\">close</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/detail/info/info.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/info/info.tpl.html",
    "<ng-include ng-if=\"selectedType === pipelineConstant.PIPELINE\"\n" +
    "            src=\"'app/home/detail/info/pipelineInformation.tpl.html'\"></ng-include>\n" +
    "\n" +
    "<ng-include ng-if=\"selectedType === pipelineConstant.STAGE_INSTANCE\"\n" +
    "            src=\"'app/home/detail/info/stageInformation.tpl.html'\"></ng-include>\n" +
    "\n" +
    "<ng-include ng-if=\"selectedType === pipelineConstant.LINK\"\n" +
    "            src=\"'app/home/detail/info/linkInformation.tpl.html'\"></ng-include>\n" +
    "");
}]);

angular.module("app/home/detail/info/linkInformation.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/info/linkInformation.tpl.html",
    "<form class=\"form-horizontal info-form\" role=\"form\" name=\"linkGeneralInfo\">\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"global.form.fromStage\">From Stage</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{selectedObject.source.uiInfo.label}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\" ng-if=\"selectedObject.source.outputLanes.length > 1\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"global.form.fromStageLane\">From Stage Lane</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{getLaneIndex(selectedObject)}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "\n" +
    "  <div class=\"form-group\" ng-if=\"selectedObject.source.outputLanes.length > 1\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"global.form.fromStageLanePredicate\">From Stage Lane Predicate</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{getLanePredicate(selectedObject)}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"global.form.toStage\">To Stage</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{selectedObject.target.uiInfo.label}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/detail/info/pipelineInformation.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/info/pipelineInformation.tpl.html",
    "<form class=\"form-horizontal info-form\" role=\"form\" name=\"stageGeneralInfo\">\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"global.form.lastModifiedBy\">Last Modified By</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{pipelineConfig.info.lastModifier}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"global.form.lastModifiedOn\">Last Modified On</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{pipelineConfig.info.lastModified | date:'medium'}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"global.form.createdBy\">Created By</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{pipelineConfig.info.creator}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"global.form.createdOn\">Created On</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{pipelineConfig.info.created  | date:'medium'}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"global.form.schemaVersion\">Schema Version</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{pipelineConfig.schemaVersion}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\" ng-if=\"pipelineConfig.metadata\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"global.form.metadata\">Metadata</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <pre class=\"form-control-static\">{{pipelineConfig.metadata  | json}}</pre>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/detail/info/stageInformation.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/info/stageInformation.tpl.html",
    "<form class=\"form-horizontal info-form\" role=\"form\" name=\"stageGeneralInfo\">\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"home.detailPane.configurationTab.stageName\">Stage Name</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{detailPaneConfigDefn.label}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"home.detailPane.configurationTab.stageInstanceName\">Stage Instance Name</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{detailPaneConfig.instanceName}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\" ng-if=\"detailPaneConfigDefn.description\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"home.detailPane.configurationTab.stageDescription\">Description</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{detailPaneConfigDefn.description}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"home.detailPane.configurationTab.stageType\">Stage Type</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\" ng-if=\"detailPaneConfigDefn.type === pipelineConstant.SOURCE_STAGE_TYPE\"\n" +
    "         translate=\"global.form.source\"></p>\n" +
    "      <p class=\"form-control-static\" ng-if=\"detailPaneConfigDefn.type === pipelineConstant.PROCESSOR_STAGE_TYPE\"\n" +
    "         translate=\"global.form.processor\"></p>\n" +
    "      <p class=\"form-control-static\" ng-if=\"detailPaneConfigDefn.type === pipelineConstant.EXECUTOR_STAGE_TYPE\"\n" +
    "         translate=\"global.form.executor\"></p>\n" +
    "      <p class=\"form-control-static\" ng-if=\"detailPaneConfigDefn.type === pipelineConstant.TARGET_STAGE_TYPE\"\n" +
    "         translate=\"global.form.target\"></p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-md-3 control-label\" translate=\"home.detailPane.configurationTab.stageLibrary\">Stage Library</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <p class=\"form-control-static\">{{getStageLibraryLabel(detailPaneConfig)}}</p>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/detail/rawPreview/rawPreview.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/rawPreview/rawPreview.tpl.html",
    "<div ng-controller=\"RawPreviewController\" class=\"raw-preview-tab\">\n" +
    "  <form class=\"form row form-group-sm\" role=\"form\" ng-submit=\"rawSourcePreview()\">\n" +
    "\n" +
    "    <div class=\"col-md-4 raw-source-config\">\n" +
    "      <div class=\"form-group\"\n" +
    "           ng-switch=\"configDefinition.type\"\n" +
    "           ng-repeat=\"configDefinition in detailPaneConfigDefn.rawSourceDefinition.configDefinitions\">\n" +
    "\n" +
    "        <div ng-switch-when=\"STRING\">\n" +
    "          <label class=\"control-label\">{{configDefinition.label}}\n" +
    "            <i class=\"fa fa-info-circle help-icon\"\n" +
    "               ng-if=\"configDefinition.description\"\n" +
    "               tooltip-placement=\"right\"\n" +
    "               tooltip-append-to-body=\"false\"\n" +
    "               tooltip=\"{{configDefinition.description}}\"\n" +
    "               tooltip-trigger=\"mouseenter\"></i>\n" +
    "          </label>\n" +
    "\n" +
    "          <div class=\"controls\">\n" +
    "            <input type=\"text\" class=\"form-control\" name=\"{{configDefinition.name}}\"\n" +
    "                 ng-model=\"detailPaneConfig.uiInfo.rawSource.configuration[$index].value\"\n" +
    "                 ng-required=\"configDefinition.required\">\n" +
    "          </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div ng-switch-when=\"CREDENTIAL\">\n" +
    "          <label class=\"control-label\">{{configDefinition.label}}\n" +
    "            <i class=\"fa fa-info-circle help-icon\"\n" +
    "               ng-if=\"configDefinition.description\"\n" +
    "               tooltip-placement=\"right\"\n" +
    "               tooltip-append-to-body=\"false\"\n" +
    "               tooltip=\"{{configDefinition.description}}\"\n" +
    "               tooltip-trigger=\"mouseenter\"></i>\n" +
    "          </label>\n" +
    "\n" +
    "          <div class=\"controls\">\n" +
    "            <input type=\"text\" class=\"form-control\" name=\"{{configDefinition.name}}\"\n" +
    "                   ng-model=\"detailPaneConfig.uiInfo.rawSource.configuration[$index].value\"\n" +
    "                   ng-required=\"configDefinition.required\">\n" +
    "          </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"checkbox\" ng-switch-when=\"BOOLEAN\">\n" +
    "          <label class=\"control-label\">{{configDefinition.label}}\n" +
    "            <i class=\"fa fa-info-circle help-icon\"\n" +
    "               ng-if=\"configDefinition.description\"\n" +
    "               tooltip-placement=\"right\"\n" +
    "               tooltip-append-to-body=\"false\"\n" +
    "               tooltip=\"{{configDefinition.description}}\"\n" +
    "               tooltip-trigger=\"mouseenter\"></i>\n" +
    "          </label>\n" +
    "\n" +
    "          <div class=\"controls\">\n" +
    "            <input type=\"checkbox\" name=\"{{configDefinition.name}}\"\n" +
    "                   ng-model=\"detailPaneConfig.uiInfo.rawSource.configuration[$index].value\">\n" +
    "          </div>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "        <div ng-switch-when=\"NUMBER\">\n" +
    "          <label class=\"control-label\">{{configDefinition.label}}\n" +
    "            <i class=\"fa fa-info-circle help-icon\"\n" +
    "               ng-if=\"configDefinition.description\"\n" +
    "               tooltip-placement=\"right\"\n" +
    "               tooltip-append-to-body=\"false\"\n" +
    "               tooltip=\"{{configDefinition.description}}\"\n" +
    "               tooltip-trigger=\"mouseenter\"></i>\n" +
    "          </label>\n" +
    "          <div class=\"controls\">\n" +
    "            <input type=\"number\" class=\"form-control\" name=\"{{configDefinition.name}}\"\n" +
    "                   ng-model=\"detailPaneConfig.uiInfo.rawSource.configuration[$index].value\"\n" +
    "                   ng-required=\"configDefinition.required\">\n" +
    "          </div>\n" +
    "\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "      <button type=\"submit\" class=\"btn btn-primary btn-sm pull-left\" translate=\"home.header.preview\">Preview</button>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"col-md-8 preview-data form-group\" ng-if=\"rawSourcePreviewData\">\n" +
    "      <div ui-codemirror\n" +
    "           class=\"codemirror-editor\"\n" +
    "           ng-model=\"rawSourcePreviewData\"\n" +
    "           ui-refresh=\"refreshCodemirror\"\n" +
    "           ui-codemirror-opts=\"rawDataCodemirrorOptions\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </form>\n" +
    "</div>");
}]);

angular.module("app/home/detail/rules/dataDriftRules/dataDriftRules.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/rules/dataDriftRules/dataDriftRules.tpl.html",
    "<div class=\"data-rules-tab\" ng-controller=\"DataDriftRulesController\">\n" +
    "  <div class=\"panel-heading clearfix\">\n" +
    "    <button type=\"button\" class=\"btn btn-primary btn-sm add-button pull-right\"\n" +
    "            ng-if=\"!isPipelineRulesReadOnly && pipelineConfig.stages.length\"\n" +
    "            ng-click=\"createDataRule()\">\n" +
    "      <i class=\"fa fa-plus\"></i> {{'global.form.add' | translate}}\n" +
    "    </button>\n" +
    "  </div>\n" +
    "  <div class=\"panel-body\">\n" +
    "    <form class=\"form-horizontal\" role=\"form\"\n" +
    "          ng-class=\"{'form-group-sm': ($storage.displayDensity === pipelineConstant.DENSITY_COZY || $storage.displayDensity === pipelineConstant.DENSITY_COMPACT)}\"\n" +
    "          name=\"{{selectedType === pipelineConstant.STAGE_INSTANCE ? 'd' + detailPaneConfig.instanceName : 'pipeline'}}\">\n" +
    "\n" +
    "      <table class=\"table table-hover\" ng-init=\"expandAll=false\">\n" +
    "        <thead>\n" +
    "          <tr>\n" +
    "            <th class=\"expand-col\">\n" +
    "              <span class=\"pointer toggler {{expandAll ? 'open' : ''}}\"\n" +
    "                    ng-click=\"expandAll = !expandAll\">\n" +
    "              </span>\n" +
    "            </th>\n" +
    "            <th class=\"alert-text-col\">{{'home.detailPane.rulesTab.stream' | translate}}</th>\n" +
    "            <th class=\"alert-text-col\">{{'home.detailPane.rulesTab.label' | translate}}</th>\n" +
    "            <th class=\"condition-col\">{{'home.detailPane.rulesTab.condition' | translate}}</th>\n" +
    "            <th class=\"condition-col\">{{'home.detailPane.rulesTab.enableMeter' | translate}}</th>\n" +
    "            <th class=\"condition-col\">{{'home.detailPane.rulesTab.enableAlert' | translate}}</th>\n" +
    "            <th class=\"sendEmail-col\">{{'home.detailPane.rulesTab.sendEmail' | translate}}</th>\n" +
    "            <th class=\"enabled-col\">{{'home.detailPane.rulesTab.enabled' | translate}}</th>\n" +
    "            <th class=\"actions-col\">{{'global.form.actions' | translate}}</th>\n" +
    "          </tr>\n" +
    "        </thead>\n" +
    "        <tbody ng-hide=\"showLoading\">\n" +
    "\n" +
    "        <tr ng-repeat-start=\"dataDriftRuleDefn in dataDriftRuleDefinitions = getFilteredDataDriftRules()\"\n" +
    "            ng-init=\"isOpen=false;\"\n" +
    "            ng-click=\"isOpen = !isOpen\"\n" +
    "            class=\"pointer {{dataDriftRuleDefn.valid ? '' : 'invalid-rule'}}\">\n" +
    "\n" +
    "          <td>\n" +
    "            <span class=\"toggler {{isOpen || expandAll ? 'open' : ''}}\"\n" +
    "                  ng-click=\"isOpen = !isOpen; $event.stopPropagation()\"></span>\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <span>{{streamLabelMap[dataDriftRuleDefn.lane]}}</span>\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <span>{{dataDriftRuleDefn.label}}</span>\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <span>{{dataDriftRuleDefn.condition}}</span>\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"checkbox\"\n" +
    "                   name=\"meterEnabled{{$index}}\"\n" +
    "                   ng-disabled=\"dataDriftRuleDefn.enabled || isPipelineRulesReadOnly\"\n" +
    "                   ng-click=\"$event.stopPropagation()\"\n" +
    "                   ng-model=\"dataDriftRuleDefn.meterEnabled\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"checkbox\"\n" +
    "                   name=\"alertEnabled{{$index}}\"\n" +
    "                   ng-disabled=\"dataDriftRuleDefn.enabled || isPipelineRulesReadOnly\"\n" +
    "                   ng-click=\"$event.stopPropagation()\"\n" +
    "                   ng-model=\"dataDriftRuleDefn.alertEnabled\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"checkbox\"\n" +
    "                   name=\"sendEmail{{$index}}\"\n" +
    "                   ng-if=\"dataDriftRuleDefn.alertEnabled\"\n" +
    "                   ng-disabled=\"dataDriftRuleDefn.enabled || isPipelineRulesReadOnly\"\n" +
    "                   ng-click=\"$event.stopPropagation()\"\n" +
    "                   ng-model=\"dataDriftRuleDefn.sendEmail\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"checkbox\"\n" +
    "                   name=\"enabled{{$index}}\"\n" +
    "                   ng-disabled=\"(!dataDriftRuleDefn.valid && dataDriftRuleDefn.enabled === false) || isPipelineRulesReadOnly\"\n" +
    "                   ng-click=\"$event.stopPropagation()\"\n" +
    "                   ng-model=\"dataDriftRuleDefn.enabled\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <div ng-show=\"!dataDriftRuleDefn.enabled\" ng-if=\"!isPipelineRulesReadOnly\">\n" +
    "              <a href=\"javascript:;\" translate=\"global.form.edit\"\n" +
    "                 ng-click=\"editDataRule(dataDriftRuleDefn, $index, $event)\">Edit</a>\n" +
    "              <span class=\"separator-padding\">|</span>\n" +
    "              <a href=\"javascript:;\"  translate=\"global.form.delete\"\n" +
    "                 ng-click=\"removeRule(pipelineRules.driftRuleDefinitions, dataDriftRuleDefn, $event)\">Delete</a>\n" +
    "            </div>\n" +
    "          </td>\n" +
    "\n" +
    "        </tr>\n" +
    "\n" +
    "        <tr ng-repeat-end ng-if=\"isOpen || expandAll\" class=\"expand-row\" ng-class=\"dataDriftRuleDefn.valid ? '' : 'invalid-rule'\">\n" +
    "          <td colspan=\"9\">\n" +
    "            <ul class=\"table-properties\">\n" +
    "\n" +
    "              <li>\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.samplingPercentage' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  {{dataDriftRuleDefn.samplingPercentage}}\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li>\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.samplingRecords' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  {{dataDriftRuleDefn.samplingRecordsToRetain}}\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li ng-if=\"dataDriftRuleDefn.alertEnabled\">\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.alertText' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  {{dataDriftRuleDefn.alertText}}\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li ng-show=\"ruleIssues.length\">\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.issues' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  <span ng-repeat=\"ruleIssue in ruleIssues = (pipelineRules.ruleIssues | filter: {ruleId: dataDriftRuleDefn.id})\">\n" +
    "                    <span>{{ruleIssue.message}}</span>\n" +
    "                    <span ng-if=\"ruleIssue.additionalInfo.property === 'emailIds'\">{{'home.detailPane.rulesTab.enterEmailIdMsg' | translate}}</span>\n" +
    "                  </span>\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "            </ul>\n" +
    "          </td>\n" +
    "        </tr>\n" +
    "\n" +
    "        <tr ng-if=\"dataDriftRuleDefinitions.length === 0\">\n" +
    "          <td colspan=\"9\" class=\"no-records text-center\"\n" +
    "              translate=\"home.detailPane.rulesTab.noDataDriftRules\">No Data Drift Rules to view.</td>\n" +
    "        </tr>\n" +
    "\n" +
    "        </tbody>\n" +
    "      </table>\n" +
    "\n" +
    "      <div show-loading=\"showLoading\"></div>\n" +
    "\n" +
    "    </form>\n" +
    "\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/detail/rules/dataDriftRules/editDataDriftRule.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/rules/dataDriftRules/editDataDriftRule.tpl.html",
    "<form class=\"create-data-rule-modal-form form-horizontal\" role=\"form\" ng-submit=\"save()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.detailPane.rulesTab.dataDriftRule\">Drift Data Rule</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.stream\">Stream</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <select class=\"form-control\" name=\"thresholdType\"\n" +
    "                required=\"true\"\n" +
    "                ng-model=\"dataDriftRuleDefn.lane\">\n" +
    "\n" +
    "          <option ng-repeat=\"(laneName, laneLabel) in streamLabelMap\"\n" +
    "                  value=\"{{laneName}}\"\n" +
    "                  ng-selected=\"laneName === dataDriftRuleDefn.lane\">\n" +
    "            {{laneLabel}}\n" +
    "          </option>\n" +
    "\n" +
    "        </select>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.label\">Label</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <input type=\"text\"\n" +
    "           autofocus\n" +
    "           name=\"label\"\n" +
    "           class=\"form-control\"\n" +
    "           required=\"true\"\n" +
    "           title=\"{{'home.detailPane.rulesTab.labelPlaceholder' | translate}}\"\n" +
    "           placeholder=\"{{'home.detailPane.rulesTab.labelPlaceholder' | translate}}\"\n" +
    "           ng-model=\"dataDriftRuleDefn.label\"\n" +
    "           focus-me=\"true\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.condition\">Condition</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8 expression-language\"\n" +
    "           ui-codemirror\n" +
    "             ng-model=\"dataDriftRuleDefn.condition\"\n" +
    "             ui-refresh=\"refreshCodemirror\"\n" +
    "             ui-codemirror-opts=\"getCodeMirrorOptions('condition')\"\n" +
    "             codemirror-el\n" +
    "             field-paths=\"fieldPaths\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.samplingPercentage\">Sampling Percentage</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <input type=\"number\"\n" +
    "                  name=\"samplingPercentage\"\n" +
    "                  class=\"form-control\"\n" +
    "                  required=\"true\"\n" +
    "                  title=\"{{'home.detailPane.rulesTab.samplingPercentagePlaceholder' | translate}}\"\n" +
    "                  placeholder=\"{{'home.detailPane.rulesTab.samplingPercentagePlaceholder' | translate}}\"\n" +
    "                  ng-model=\"dataDriftRuleDefn.samplingPercentage\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.samplingRecords\">Sampling Records To Retain</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <input type=\"number\"\n" +
    "               name=\"samplingRecordsToRetain\"\n" +
    "               class=\"form-control\"\n" +
    "               required=\"true\"\n" +
    "               title=\"{{'home.detailPane.rulesTab.samplingRecordsPlaceholder' | translate}}\"\n" +
    "               placeholder=\"{{'home.detailPane.rulesTab.samplingRecordsPlaceholder' | translate}}\"\n" +
    "               ng-model=\"dataDriftRuleDefn.samplingRecordsToRetain\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.enableMeter\">Enable Meter</label>\n" +
    "      <div class=\"controls col-md-1 form-horizontal-checkbox\">\n" +
    "        <input type=\"checkbox\" name=\"meterEnabled\"\n" +
    "               ng-model=\"dataDriftRuleDefn.meterEnabled\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.enableAlert\">Enable Alert</label>\n" +
    "      <div class=\"controls col-md-1 form-horizontal-checkbox\">\n" +
    "        <input type=\"checkbox\" name=\"alertEnabled\"\n" +
    "               ng-model=\"dataDriftRuleDefn.alertEnabled\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" ng-if=\"dataDriftRuleDefn.alertEnabled\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.alertText\">Alert Text</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8 expression-language\"\n" +
    "           ui-codemirror\n" +
    "           ng-model=\"dataDriftRuleDefn.alertText\"\n" +
    "           ui-refresh=\"refreshCodemirror\"\n" +
    "           ui-codemirror-opts=\"getCodeMirrorOptions('alertText')\"\n" +
    "           codemirror-el\n" +
    "           field-paths=\"fieldPaths\">\n" +
    "      </div>\n" +
    "\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" ng-if=\"dataDriftRuleDefn.alertEnabled\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.sendEmail\">Send Email When Alert is triggered</label>\n" +
    "      <div class=\"controls col-md-1 form-horizontal-checkbox\">\n" +
    "        <input type=\"checkbox\" name=\"sendEmail\"\n" +
    "               ng-model=\"dataDriftRuleDefn.sendEmail\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\" translate=\"global.form.cancel\">\n" +
    "      Cancel\n" +
    "    </button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" translate=\"global.form.save\">\n" +
    "      Save\n" +
    "    </button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/detail/rules/dataRules/dataRules.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/rules/dataRules/dataRules.tpl.html",
    "<div class=\"data-rules-tab\" ng-controller=\"DataRulesController\">\n" +
    "  <div class=\"panel-heading clearfix\">\n" +
    "    <button type=\"button\" class=\"btn btn-primary btn-sm add-button pull-right\"\n" +
    "            ng-if=\"!isPipelineRulesReadOnly && pipelineConfig.stages.length\"\n" +
    "            ng-click=\"createDataRule()\">\n" +
    "      <i class=\"fa fa-plus\"></i> {{'global.form.add' | translate}}\n" +
    "    </button>\n" +
    "  </div>\n" +
    "  <div class=\"panel-body\">\n" +
    "    <form class=\"form-horizontal\" role=\"form\"\n" +
    "          ng-class=\"{'form-group-sm': ($storage.displayDensity === pipelineConstant.DENSITY_COZY || $storage.displayDensity === pipelineConstant.DENSITY_COMPACT)}\"\n" +
    "          name=\"{{selectedType === pipelineConstant.STAGE_INSTANCE ? 'd' + detailPaneConfig.instanceName : 'pipeline'}}\">\n" +
    "\n" +
    "      <table class=\"table table-hover\" ng-init=\"expandAll=false\">\n" +
    "        <thead>\n" +
    "          <tr>\n" +
    "            <th class=\"expand-col\">\n" +
    "              <span class=\"pointer toggler {{expandAll ? 'open' : ''}}\"\n" +
    "                    ng-click=\"expandAll = !expandAll\">\n" +
    "              </span>\n" +
    "            </th>\n" +
    "            <th class=\"alert-text-col\">{{'home.detailPane.rulesTab.stream' | translate}}</th>\n" +
    "            <th class=\"alert-text-col\">{{'home.detailPane.rulesTab.label' | translate}}</th>\n" +
    "            <th class=\"condition-col\">{{'home.detailPane.rulesTab.condition' | translate}}</th>\n" +
    "            <th class=\"condition-col\">{{'home.detailPane.rulesTab.enableMeter' | translate}}</th>\n" +
    "            <th class=\"condition-col\">{{'home.detailPane.rulesTab.enableAlert' | translate}}</th>\n" +
    "            <th class=\"sendEmail-col\">{{'home.detailPane.rulesTab.sendEmail' | translate}}</th>\n" +
    "            <th class=\"enabled-col\">{{'home.detailPane.rulesTab.enabled' | translate}}</th>\n" +
    "            <th class=\"actions-col\">{{'global.form.actions' | translate}}</th>\n" +
    "          </tr>\n" +
    "        </thead>\n" +
    "        <tbody ng-hide=\"showLoading\">\n" +
    "\n" +
    "        <tr ng-repeat-start=\"dataRuleDefn in dataRuleDefinitions = getFilteredDataRules()\"\n" +
    "            ng-init=\"isOpen=false;\"\n" +
    "            ng-click=\"isOpen = !isOpen\"\n" +
    "            class=\"pointer {{dataRuleDefn.valid ? '' : 'invalid-rule'}}\">\n" +
    "\n" +
    "          <td>\n" +
    "            <span class=\"toggler {{isOpen || expandAll ? 'open' : ''}}\"\n" +
    "                  ng-click=\"isOpen = !isOpen; $event.stopPropagation()\"></span>\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <span>{{streamLabelMap[dataRuleDefn.lane]}}</span>\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <span>{{dataRuleDefn.label}}</span>\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <span>{{dataRuleDefn.condition}}</span>\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"checkbox\"\n" +
    "                   name=\"meterEnabled{{$index}}\"\n" +
    "                   ng-disabled=\"dataRuleDefn.enabled || isPipelineRulesReadOnly\"\n" +
    "                   ng-click=\"$event.stopPropagation()\"\n" +
    "                   ng-model=\"dataRuleDefn.meterEnabled\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"checkbox\"\n" +
    "                   name=\"alertEnabled{{$index}}\"\n" +
    "                   ng-disabled=\"dataRuleDefn.enabled || isPipelineRulesReadOnly\"\n" +
    "                   ng-click=\"$event.stopPropagation()\"\n" +
    "                   ng-model=\"dataRuleDefn.alertEnabled\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"checkbox\"\n" +
    "                   name=\"sendEmail{{$index}}\"\n" +
    "                   ng-if=\"dataRuleDefn.alertEnabled\"\n" +
    "                   ng-disabled=\"dataRuleDefn.enabled || isPipelineRulesReadOnly\"\n" +
    "                   ng-click=\"$event.stopPropagation()\"\n" +
    "                   ng-model=\"dataRuleDefn.sendEmail\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"checkbox\"\n" +
    "                   name=\"enabled{{$index}}\"\n" +
    "                   ng-disabled=\"(!dataRuleDefn.valid && dataRuleDefn.enabled === false) || isPipelineRulesReadOnly\"\n" +
    "                   ng-click=\"$event.stopPropagation()\"\n" +
    "                   ng-model=\"dataRuleDefn.enabled\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <div ng-show=\"!dataRuleDefn.enabled\" ng-if=\"!isPipelineRulesReadOnly\">\n" +
    "              <a href=\"javascript:;\" translate=\"global.form.edit\"\n" +
    "                 ng-click=\"editDataRule(dataRuleDefn, $index, $event)\">Edit</a>\n" +
    "              <span class=\"separator-padding\">|</span>\n" +
    "              <a href=\"javascript:;\"  translate=\"global.form.delete\"\n" +
    "                 ng-click=\"removeRule(pipelineRules.dataRuleDefinitions, dataRuleDefn, $event)\">Delete</a>\n" +
    "            </div>\n" +
    "          </td>\n" +
    "\n" +
    "        </tr>\n" +
    "\n" +
    "        <tr ng-repeat-end ng-if=\"isOpen || expandAll\" class=\"expand-row\" ng-class=\"dataRuleDefn.valid ? '' : 'invalid-rule'\">\n" +
    "          <td colspan=\"9\">\n" +
    "            <ul class=\"table-properties\">\n" +
    "\n" +
    "              <li>\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.samplingPercentage' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  {{dataRuleDefn.samplingPercentage}}\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li>\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.samplingRecords' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  {{dataRuleDefn.samplingRecordsToRetain}}\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li ng-if=\"dataRuleDefn.alertEnabled\">\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.alertText' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  {{dataRuleDefn.alertText}}\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li ng-if=\"dataRuleDefn.alertEnabled\">\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.thresholdType' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  {{dataRuleDefn.thresholdType}}\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li ng-if=\"dataRuleDefn.alertEnabled\">\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.thresholdValue' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  {{dataRuleDefn.thresholdValue}}\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li ng-if=\"dataRuleDefn.alertEnabled && dataRuleDefn.thresholdType === 'PERCENTAGE'\">\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.minVolumePlaceholder' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  {{dataRuleDefn.minVolume}}\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li ng-show=\"ruleIssues.length\">\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.issues' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  <span ng-repeat=\"ruleIssue in ruleIssues = (pipelineRules.ruleIssues | filter: {ruleId: dataRuleDefn.id})\">\n" +
    "                    <span>{{ruleIssue.message}}</span>\n" +
    "                    <span ng-if=\"ruleIssue.additionalInfo.property === 'emailIds'\">{{'home.detailPane.rulesTab.enterEmailIdMsg' | translate}}</span>\n" +
    "                  </span>\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "            </ul>\n" +
    "          </td>\n" +
    "        </tr>\n" +
    "\n" +
    "        <tr ng-if=\"dataRuleDefinitions.length === 0\">\n" +
    "          <td colspan=\"9\" class=\"no-records text-center\"\n" +
    "              translate=\"home.detailPane.rulesTab.noDataRules\">No Data Rules to view.</td>\n" +
    "        </tr>\n" +
    "\n" +
    "        </tbody>\n" +
    "      </table>\n" +
    "\n" +
    "      <div show-loading=\"showLoading\"></div>\n" +
    "\n" +
    "    </form>\n" +
    "\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/detail/rules/dataRules/editDataRule.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/rules/dataRules/editDataRule.tpl.html",
    "<form class=\"create-data-rule-modal-form form-horizontal\" role=\"form\" ng-submit=\"save()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.detailPane.rulesTab.dataRule\">Data Rule</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.stream\">Stream</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <select class=\"form-control\" name=\"thresholdType\"\n" +
    "                required=\"true\"\n" +
    "                ng-model=\"dataRuleDefn.lane\">\n" +
    "\n" +
    "          <option ng-repeat=\"(laneName, laneLabel) in streamLabelMap\"\n" +
    "                  value=\"{{laneName}}\"\n" +
    "                  ng-selected=\"laneName === dataRuleDefn.lane\">\n" +
    "            {{laneLabel}}\n" +
    "          </option>\n" +
    "\n" +
    "        </select>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.label\">Label</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <input type=\"text\"\n" +
    "           autofocus\n" +
    "           name=\"label\"\n" +
    "           class=\"form-control\"\n" +
    "           required=\"true\"\n" +
    "           title=\"{{'home.detailPane.rulesTab.labelPlaceholder' | translate}}\"\n" +
    "           placeholder=\"{{'home.detailPane.rulesTab.labelPlaceholder' | translate}}\"\n" +
    "           ng-model=\"dataRuleDefn.label\"\n" +
    "           focus-me=\"true\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.condition\">Condition</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8 expression-language\"\n" +
    "           ui-codemirror\n" +
    "             ng-model=\"dataRuleDefn.condition\"\n" +
    "             ui-refresh=\"refreshCodemirror\"\n" +
    "             ui-codemirror-opts=\"getCodeMirrorOptions('condition')\"\n" +
    "             codemirror-el\n" +
    "             field-paths=\"fieldPaths\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.samplingPercentage\">Sampling Percentage</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <input type=\"number\"\n" +
    "                  name=\"samplingPercentage\"\n" +
    "                  class=\"form-control\"\n" +
    "                  required=\"true\"\n" +
    "                  title=\"{{'home.detailPane.rulesTab.samplingPercentagePlaceholder' | translate}}\"\n" +
    "                  placeholder=\"{{'home.detailPane.rulesTab.samplingPercentagePlaceholder' | translate}}\"\n" +
    "                  ng-model=\"dataRuleDefn.samplingPercentage\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.samplingRecords\">Sampling Records To Retain</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <input type=\"number\"\n" +
    "               name=\"samplingRecordsToRetain\"\n" +
    "               class=\"form-control\"\n" +
    "               required=\"true\"\n" +
    "               title=\"{{'home.detailPane.rulesTab.samplingRecordsPlaceholder' | translate}}\"\n" +
    "               placeholder=\"{{'home.detailPane.rulesTab.samplingRecordsPlaceholder' | translate}}\"\n" +
    "               ng-model=\"dataRuleDefn.samplingRecordsToRetain\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.enableMeter\">Enable Meter</label>\n" +
    "      <div class=\"controls col-md-1 form-horizontal-checkbox\">\n" +
    "        <input type=\"checkbox\" name=\"meterEnabled\"\n" +
    "               ng-model=\"dataRuleDefn.meterEnabled\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.enableAlert\">Enable Alert</label>\n" +
    "      <div class=\"controls col-md-1 form-horizontal-checkbox\">\n" +
    "        <input type=\"checkbox\" name=\"alertEnabled\"\n" +
    "               ng-model=\"dataRuleDefn.alertEnabled\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" ng-if=\"dataRuleDefn.alertEnabled\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.alertText\">Alert Text</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8 expression-language\"\n" +
    "           ui-codemirror\n" +
    "           ng-model=\"dataRuleDefn.alertText\"\n" +
    "           ui-refresh=\"refreshCodemirror\"\n" +
    "           ui-codemirror-opts=\"getCodeMirrorOptions('alertText')\"\n" +
    "           codemirror-el\n" +
    "           field-paths=\"fieldPaths\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" ng-if=\"dataRuleDefn.alertEnabled\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.thresholdType\">thresholdType</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <select class=\"form-control\" name=\"thresholdType\"\n" +
    "                ng-model=\"dataRuleDefn.thresholdType\">\n" +
    "          <option value=\"COUNT\">Count</option>\n" +
    "          <option value=\"PERCENTAGE\">Percentage</option>\n" +
    "        </select>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" ng-if=\"dataRuleDefn.alertEnabled\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.thresholdValue\">Threshold Value</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <input type=\"text\"\n" +
    "               name=\"thresholdValue\"\n" +
    "               class=\"form-control\"\n" +
    "               required=\"true\"\n" +
    "               title=\"{{'home.detailPane.rulesTab.thresholdValue' | translate}}\"\n" +
    "               placeholder=\"{{'home.detailPane.rulesTab.thresholdValue' | translate}}\"\n" +
    "               ng-model=\"dataRuleDefn.thresholdValue\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" ng-if=\"dataRuleDefn.alertEnabled && dataRuleDefn.thresholdType === 'PERCENTAGE'\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.minVolumePlaceholder\">Minimum Volume</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <input type=\"number\"\n" +
    "               name=\"minVolume\"\n" +
    "               class=\"form-control\"\n" +
    "               required=\"true\"\n" +
    "               title=\"{{'home.detailPane.rulesTab.minVolumePlaceholder' | translate}}\"\n" +
    "               placeholder=\"{{'home.detailPane.rulesTab.minVolumePlaceholder' | translate}}\"\n" +
    "               ng-model=\"dataRuleDefn.minVolume\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" ng-if=\"dataRuleDefn.alertEnabled\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.sendEmail\">Send Email When Alert is triggered</label>\n" +
    "      <div class=\"controls col-md-1 form-horizontal-checkbox\">\n" +
    "        <input type=\"checkbox\" name=\"sendEmail\"\n" +
    "               ng-model=\"dataRuleDefn.sendEmail\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\" translate=\"global.form.cancel\">\n" +
    "      Cancel\n" +
    "    </button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" translate=\"global.form.save\">\n" +
    "      Save\n" +
    "    </button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/detail/rules/metricAlert/editMetricAlertRule.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/rules/metricAlert/editMetricAlertRule.tpl.html",
    "<form class=\"create-metric-alert-rule-modal-form form-horizontal\" role=\"form\" ng-submit=\"save()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.detailPane.rulesTab.metricAlertRule\">Metric Alert Rule</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.alertText\">Alert Text</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <input type=\"text\"\n" +
    "           autofocus\n" +
    "           name=\"alertText\"\n" +
    "           class=\"form-control\"\n" +
    "           required=\"true\"\n" +
    "           title=\"{{'home.detailPane.rulesTab.alertTextPlaceholder' | translate}}\"\n" +
    "           placeholder=\"{{'home.detailPane.rulesTab.alertTextPlaceholder' | translate}}\"\n" +
    "           ng-model=\"metricAlertRuleDefn.alertText\"\n" +
    "           focus-me=\"true\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.metricType\">Metric Type</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <select class=\"form-control\" name=\"metricType\" required=\"true\"\n" +
    "                ng-model=\"metricAlertRuleDefn.metricType\">\n" +
    "          <option value=\"GAUGE\">{{'home.detailPane.rulesTab.gauge' | translate}}</option>\n" +
    "          <option value=\"COUNTER\">{{'home.detailPane.rulesTab.counter' | translate}}</option>\n" +
    "          <option value=\"HISTOGRAM\">{{'home.detailPane.rulesTab.histogram' | translate}}</option>\n" +
    "          <option value=\"METER\">{{'home.detailPane.rulesTab.meter' | translate}}</option>\n" +
    "          <option value=\"TIMER\">{{'home.detailPane.rulesTab.timer' | translate}}</option>\n" +
    "        </select>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.metricID\">Metric ID</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <select class=\"form-control\" name=\"metricId\" required=\"true\"\n" +
    "                ng-model=\"metricAlertRuleDefn.metricId\"\n" +
    "                ng-options=\"obj.value as obj.label for obj in metricIDList[metricAlertRuleDefn.metricType]\">\n" +
    "        </select>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.metricElement\">Metric Element</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8\">\n" +
    "        <select class=\"form-control\" name=\"metricElement\" required=\"true\"\n" +
    "                ng-model=\"metricAlertRuleDefn.metricElement\"\n" +
    "                ng-options=\"obj.value as obj.label for obj in metricElementList[metricAlertRuleDefn.metricType]\">\n" +
    "        </select>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.condition\">Condition</label>\n" +
    "      <div class=\"controls col-lg-9 col-md-8 expression-language\"\n" +
    "           ui-codemirror\n" +
    "           ng-model=\"metricAlertRuleDefn.condition\"\n" +
    "           ui-refresh=\"refreshCodemirror\"\n" +
    "           ui-codemirror-opts=\"getCodeMirrorOptions()\"\n" +
    "           codemirror-el>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors>\n" +
    "      <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.detailPane.rulesTab.sendEmail\">Send Email When Alert is triggered</label>\n" +
    "      <div class=\"controls col-md-1 form-horizontal-checkbox\">\n" +
    "        <input type=\"checkbox\" name=\"sendEmail\"\n" +
    "               ng-model=\"metricAlertRuleDefn.sendEmail\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\" translate=\"global.form.cancel\">\n" +
    "      Cancel\n" +
    "    </button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" translate=\"global.form.save\">\n" +
    "      Save\n" +
    "    </button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/detail/rules/metricAlert/metricAlert.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/rules/metricAlert/metricAlert.tpl.html",
    "<div class=\"metric-alert-rules-tab\" ng-controller=\"MetricAlertRulesController\">\n" +
    "  <div class=\"panel-heading clearfix\">\n" +
    "\n" +
    "    <button type=\"button\" class=\"btn btn-primary btn-sm pull-right\"\n" +
    "            ng-if=\"!isPipelineRulesReadOnly\"\n" +
    "            ng-click=\"createMetricAlertRule()\">\n" +
    "      <i class=\"fa fa-plus\"></i> {{'global.form.add' | translate}}\n" +
    "    </button>\n" +
    "  </div>\n" +
    "  <div class=\"panel-body\">\n" +
    "    <form class=\"form-horizontal\" role=\"form\"\n" +
    "          ng-class=\"{'form-group-sm': ($storage.displayDensity === pipelineConstant.DENSITY_COZY || $storage.displayDensity === pipelineConstant.DENSITY_COMPACT)}\"\n" +
    "          name=\"{{selectedType === pipelineConstant.STAGE_INSTANCE ? 'd' + detailPaneConfig.instanceName : 'pipeline'}}\">\n" +
    "\n" +
    "      <table class=\"table table-hover\">\n" +
    "        <thead>\n" +
    "          <tr>\n" +
    "            <th class=\"expand-col\">\n" +
    "              <span class=\"pointer toggler {{expandAll ? 'open' : ''}}\"\n" +
    "                      ng-click=\"expandAll = !expandAll\"></span>\n" +
    "            </th>\n" +
    "            <th class=\"alert-text-col\">{{'home.detailPane.rulesTab.alertText' | translate}}</th>\n" +
    "            <th class=\"id-col\">{{'home.detailPane.rulesTab.metricID' | translate}}</th>\n" +
    "            <th class=\"condition-col\">{{'home.detailPane.rulesTab.condition' | translate}}</th>\n" +
    "            <th class=\"condition-col\">{{'home.detailPane.rulesTab.sendEmail' | translate}}</th>\n" +
    "            <th class=\"enabled-col\">{{'home.detailPane.rulesTab.enabled' | translate}}</th>\n" +
    "            <th class=\"actions-col\">{{'global.form.actions' | translate}}</th>\n" +
    "          </tr>\n" +
    "        </thead>\n" +
    "        <tbody ng-hide=\"showLoading\">\n" +
    "\n" +
    "        <tr ng-repeat-start=\"alertRule in metricsRuleDefinitions = (pipelineRules.metricsRuleDefinitions) track by $index\"\n" +
    "            ng-init=\"isOpen=false;\"\n" +
    "            ng-click=\"isOpen = !isOpen\"\n" +
    "            class=\"pointer {{alertRule.valid ? '' : 'invalid-rule'}}\">\n" +
    "\n" +
    "          <td>\n" +
    "            <span class=\"toggler {{isOpen || expandAll ? 'open' : ''}}\"\n" +
    "                  ng-click=\"isOpen = !isOpen; $event.stopPropagation()\"></span>\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            {{alertRule.alertText}}\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            {{getMetricIdLabel(alertRule)}}\n" +
    "          </td>\n" +
    "\n" +
    "          <td class=\"expression-language\">\n" +
    "            {{alertRule.condition}}\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"checkbox\" name=\"sendEmail{{$index}}\"\n" +
    "                   ng-disabled=\"alertRule.enabled || isPipelineRulesReadOnly\"\n" +
    "                   ng-click=\"$event.stopPropagation()\"\n" +
    "                   ng-model=\"alertRule.sendEmail\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"checkbox\" name=\"enabled{{$index}}\"\n" +
    "                   ng-disabled=\"(!alertRule.valid && alertRule.enabled === false) || isPipelineRulesReadOnly\"\n" +
    "                   ng-click=\"$event.stopPropagation()\"\n" +
    "                   ng-model=\"alertRule.enabled\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <div ng-show=\"!alertRule.enabled\" ng-if=\"!isPipelineRulesReadOnly\">\n" +
    "              <a href=\"javascript:;\" translate=\"global.form.edit\"\n" +
    "                 ng-click=\"editMetricAlertDataRule(alertRule, $index); $event.stopPropagation()\">Edit</a>\n" +
    "              <span class=\"separator-padding\">|</span>\n" +
    "              <a href=\"javascript:;\"  translate=\"global.form.delete\"\n" +
    "                 ng-click=\"removeRule(pipelineRules.metricsRuleDefinitions, $index); $event.stopPropagation()\">Delete</a>\n" +
    "            </div>\n" +
    "          </td>\n" +
    "\n" +
    "        </tr>\n" +
    "\n" +
    "\n" +
    "        <tr ng-repeat-end ng-if=\"isOpen || expandAll\" class=\"expand-row\" ng-class=\"alertRule.valid ? '' : 'invalid-rule'\">\n" +
    "          <td colspan=\"8\">\n" +
    "            <ul class=\"table-properties\">\n" +
    "\n" +
    "              <li>\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.metricType' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  {{alertRule.metricType}}\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li>\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.metricElement' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  {{getMetricElementLabel(alertRule)}}\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li ng-show=\"ruleIssues.length\">\n" +
    "                <span class=\"properties-label\">\n" +
    "                  {{'home.detailPane.rulesTab.issues' | translate}}:\n" +
    "                </span>\n" +
    "                <span class=\"properties-value\">\n" +
    "                  <span ng-repeat=\"ruleIssue in ruleIssues = (pipelineRules.ruleIssues | filter: {ruleId: alertRule.id})\">\n" +
    "                    <span>{{ruleIssue.message}}</span>\n" +
    "                    <span ng-if=\"ruleIssue.additionalInfo.property === 'emailIds'\">{{'home.detailPane.rulesTab.enterEmailIdMsg' | translate}}</span>\n" +
    "                  </span>\n" +
    "                </span>\n" +
    "              </li>\n" +
    "\n" +
    "            </ul>\n" +
    "          </td>\n" +
    "        </tr>\n" +
    "\n" +
    "        <tr ng-if=\"metricsRuleDefinitions.length === 0\">\n" +
    "          <td colspan=\"7\" class=\"no-records text-center\"\n" +
    "              translate=\"home.detailPane.rulesTab.noMetricAlertRules\">No Metric Alert Rules to view.</td>\n" +
    "        </tr>\n" +
    "\n" +
    "        </tbody>\n" +
    "      </table>\n" +
    "\n" +
    "      <div show-loading=\"showLoading\"></div>\n" +
    "\n" +
    "    </form>\n" +
    "\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/detail/rules/rules.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/rules/rules.tpl.html",
    "<div ng-controller=\"RulesController\">\n" +
    "  <tabset class=\"tabs-top\">\n" +
    "    <tab>\n" +
    "      <tab-heading>\n" +
    "        {{'home.detailPane.rulesTab.metricAlertRules' | translate}}\n" +
    "      </tab-heading>\n" +
    "      <div class=\"tabs-content\">\n" +
    "        <ng-include src=\"'app/home/detail/rules/metricAlert/metricAlert.tpl.html'\"></ng-include>\n" +
    "      </div>\n" +
    "    </tab>\n" +
    "\n" +
    "    <tab>\n" +
    "      <tab-heading>\n" +
    "        {{'home.detailPane.rulesTab.dataRules' | translate}}\n" +
    "      </tab-heading>\n" +
    "      <div class=\"tabs-content\">\n" +
    "        <ng-include src=\"'app/home/detail/rules/dataRules/dataRules.tpl.html'\"></ng-include>\n" +
    "      </div>\n" +
    "    </tab>\n" +
    "\n" +
    "    <tab>\n" +
    "      <tab-heading>\n" +
    "        {{'home.detailPane.rulesTab.dataDriftRules' | translate}}\n" +
    "      </tab-heading>\n" +
    "      <div class=\"tabs-content\">\n" +
    "        <ng-include src=\"'app/home/detail/rules/dataDriftRules/dataDriftRules.tpl.html'\"></ng-include>\n" +
    "      </div>\n" +
    "    </tab>\n" +
    "\n" +
    "    <tab select=\"onTabSelect()\">\n" +
    "      <tab-heading>\n" +
    "        <i class=\"fa fa-exclamation-triangle icon-danger\"\n" +
    "           ng-show=\"showConfigurationWarning()\"></i>\n" +
    "        {{'home.detailPane.notifications' | translate}}\n" +
    "      </tab-heading>\n" +
    "      <div class=\"tabs-content\">\n" +
    "        <ng-include src=\"'app/home/detail/rules/rulesConfiguration/rulesConfiguration.tpl.html'\"></ng-include>\n" +
    "      </div>\n" +
    "    </tab>\n" +
    "  </tabset>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/detail/rules/rulesConfiguration/rulesConfiguration.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/rules/rulesConfiguration/rulesConfiguration.tpl.html",
    "<div class=\"configuration-tab\">\n" +
    "  <form class=\"form-horizontal email-IDs-tab\"\n" +
    "        ng-controller=\"RulesConfigurationController\"\n" +
    "        ng-class=\"{'form-group-sm': $storage.displayDensity === pipelineConstant.DENSITY_COZY || $storage.displayDensity === pipelineConstant.DENSITY_COMPACT}\"\n" +
    "        role=\"form\" name=\"webhooksForm\">\n" +
    "    <div class=\"form-group \"\n" +
    "         ng-class=\"{'has-error': getConfigurationIssues(detailPaneConfig, configDefinition).length > 0}\"\n" +
    "         ng-repeat=\"configDefinition in pipelineRulesConfigDefinition.configDefinitions | orderBy: 'displayPosition'\"\n" +
    "         ng-if=\"(verifyDependsOnMap(pipelineRules, configDefinition))\"\n" +
    "         ng-init=\"configIndex = getConfigIndex(pipelineRules, configDefinition)\">\n" +
    "\n" +
    "      <ng-include src=\"'app/home/detail/configuration/groupConfiguration.tpl.html'\"></ng-include>\n" +
    "    </div>\n" +
    "  </form>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/detail/summary/settings/settingsModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/summary/settings/settingsModal.tpl.html",
    "<form class=\"summary-settings-modal-form form\" role=\"form\" ng-submit=\"save()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.detailPane.summaryTab.settingsTitle\">Monitoring Settings</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div class=\"form-group\">\n" +
    "      <label class=\"control-label\" translate=\"home.detailPane.summaryTab.chartsLabel\">Charts</label>\n" +
    "      <ui-select multiple\n" +
    "          ng-model=\"selectedCharts.selected\">\n" +
    "        <ui-select-match class=\"ui-select-match\" placeholder=\"{{'home.detailPane.summaryTab.chartsPlaceholder' | translate}}\">{{$item.label | translate}}</ui-select-match>\n" +
    "        <ui-select-choices class=\"ui-select-choices\" repeat=\"chart in availableCharts |  filter: {label: $select.search}\">\n" +
    "          {{chart.label | translate}}\n" +
    "        </ui-select-choices>\n" +
    "      </ui-select>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\" translate=\"global.form.cancel\">\n" +
    "      Cancel\n" +
    "    </button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" translate=\"global.form.save\">\n" +
    "      Save\n" +
    "    </button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/detail/summary/summary.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/detail/summary/summary.tpl.html",
    "<div class=\"container-fluid summary-tab\" ng-controller=\"SummaryController\">\n" +
    "\n" +
    "  <div class=\"row\" ng-if=\"selectedType === pipelineConstant.PIPELINE && triggeredAlerts.length\">\n" +
    "    <div class=\"col-sm-12 col-md-12 col-lg-12 triggered-alerts\"\n" +
    "         ng-repeat=\"triggeredAlert in triggeredAlerts | filter: {type: 'METRIC_ALERT'}\">\n" +
    "      <div class=\"alert alert-danger\" role=\"alert\">\n" +
    "        <i class=\"fa fa-exclamation-triangle\"></i>\n" +
    "        <span>{{triggeredAlert.ruleDefinition.alertText}}</span>\n" +
    "        <span class=\"alert-details\"> ( {{'home.detailPane.summaryTab.currentValue' | translate}}: {{triggeredAlert.gauge.value.currentValue}}, {{'home.detailPane.summaryTab.triggered' | translate}}: {{triggeredAlert.gauge.value.timestamp | amTimeAgo}} )</span>\n" +
    "        <div class=\"pull-right\">\n" +
    "          <a href=\"#\"\n" +
    "             ng-click=\"deleteTriggeredAlert(triggeredAlert)\">\n" +
    "            <span translate=\"global.form.delete\">Delete</span>\n" +
    "          </a>\n" +
    "          &nbsp;|&nbsp;\n" +
    "          <a href=\"#\"\n" +
    "             ng-click=\"selectRulesTab(triggeredAlert)\">\n" +
    "            <span translate=\"global.form.changeRule\">Change Rule</span>\n" +
    "          </a>\n" +
    "        </div>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-if=\"summaryDataLoaded && selectedType === pipelineConstant.STAGE_INSTANCE && customStageMeters\">\n" +
    "    <div class=\"col-sm-4 chartPanels\" ng-repeat=\"customStageMeter in customStageMeters\">\n" +
    "      <div class=\"panel panel-default\" ng-controller=\"CustomMeterController\">\n" +
    "        <div class=\"panel-heading\">\n" +
    "          <span>{{customStageMeter.label}} Meter</span>\n" +
    "        </div>\n" +
    "        <div class=\"panel-body\">\n" +
    "          <ul class=\"properties\">\n" +
    "            <li>\n" +
    "              <span class=\"properties-label\">Count:</span>\n" +
    "              <span class=\"properties-value\">{{count}}</span>\n" +
    "            </li>\n" +
    "          </ul>\n" +
    "          <nvd3 options=\"chartOptions\" data=\"chartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-if=\"summaryDataLoaded && selectedType === pipelineConstant.STAGE_INSTANCE && customStageHistograms\">\n" +
    "    <div class=\"col-sm-4 chartPanels\" ng-repeat=\"customStageHistogram in customStageHistograms\">\n" +
    "      <div class=\"panel panel-default\" ng-controller=\"CustomHistogramController\">\n" +
    "        <div class=\"panel-heading\">\n" +
    "          <span>{{customStageHistogram.label}} Histogram</span>\n" +
    "        </div>\n" +
    "        <div class=\"panel-body\">\n" +
    "          <ul class=\"properties\">\n" +
    "            <li>\n" +
    "              <span class=\"properties-label\">Count:</span>\n" +
    "              <span class=\"properties-value\">{{count}}</span>\n" +
    "            </li>\n" +
    "          </ul>\n" +
    "          <nvd3 options=\"chartOptions\" data=\"chartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "\n" +
    "  <div ng-if=\"summaryDataLoaded && selectedType === pipelineConstant.STAGE_INSTANCE && customStageTimers\">\n" +
    "    <div class=\"col-sm-4 chartPanels\" ng-repeat=\"customStageTimer in customStageTimers\">\n" +
    "      <div class=\"panel panel-default\" ng-controller=\"CustomTimerController\">\n" +
    "        <div class=\"panel-heading\">\n" +
    "          <span>{{customStageTimer.label}} Timer</span>\n" +
    "        </div>\n" +
    "        <div class=\"panel-body\">\n" +
    "          <ul class=\"properties\">\n" +
    "            <li>\n" +
    "              <span class=\"properties-label\">Count:</span>\n" +
    "              <span class=\"properties-value\">{{count}}</span>\n" +
    "            </li>\n" +
    "          </ul>\n" +
    "          <nvd3 options=\"chartOptions\" data=\"chartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-if=\"summaryDataLoaded && selectedType === pipelineConstant.STAGE_INSTANCE && customStageGauges\">\n" +
    "    <div class=\"col-sm-4 chartPanels\" ng-repeat=\"customStageGauge in customStageGauges\" ng-if=\"!customStageGauge.gaugeKey.startsWith('custom.Aggregator')\">\n" +
    "      <div class=\"panel panel-default\">\n" +
    "        <div class=\"panel-heading\">\n" +
    "          <span>{{customStageGauge.label}}</span>\n" +
    "        </div>\n" +
    "        <div class=\"panel-body\">\n" +
    "          <ul class=\"table-properties\">\n" +
    "            <li ng-repeat= \"(propertyKey, propertyValue) in common.pipelineMetrics.gauges[customStageGauge.gaugeKey].value\">\n" +
    "              <span class=\"properties-label\">{{propertyKey}}: </span>\n" +
    "              <span class=\"properties-value\">{{propertyValue}}</span>\n" +
    "            </li>\n" +
    "          </ul>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <!-- Aggregations -->\n" +
    "  <div ng-if=\"summaryDataLoaded && selectedType === pipelineConstant.STAGE_INSTANCE && customStageGauges\">\n" +
    "    <div class=\"col-sm-12 col-md-12 col-lg-12\" ng-repeat=\"customStageGauge in customStageGauges\" ng-if=\"customStageGauge.gaugeKey.startsWith('custom.Aggregator')\">\n" +
    "      <div class=\"panel panel-default\" ng-controller=\"CustomBarChartController\">\n" +
    "        <div class=\"panel-heading\">\n" +
    "          <span>{{chartTitle}}</span>\n" +
    "        </div>\n" +
    "        <div class=\"panel-body\">\n" +
    "          <nvd3 options=\"chartOptions\" data=\"chartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "  <!-- Aggregations -->\n" +
    "\n" +
    "  <div class=\"chartPanels\" ng-if=\"summaryDataLoaded\">\n" +
    "    <ng-include ng-repeat=\"(chartIndex, chart) in $storage.summaryPanelList_v1\" src=\"chart.templateId\">\n" +
    "    </ng-include>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-if=\"summaryDataLoaded && selectedType === pipelineConstant.PIPELINE && runnerGauges\">\n" +
    "    <div class=\"col-sm-4 chartPanels\" ng-repeat=\"runnerGauge in runnerGauges\">\n" +
    "      <div class=\"panel panel-default\">\n" +
    "        <div class=\"panel-heading\">\n" +
    "          <span> Runner {{runnerGauge.runnerId}}</span>\n" +
    "        </div>\n" +
    "        <div class=\"panel-body\">\n" +
    "          <ul class=\"table-properties\" ng-init=\"gaugeData = common.pipelineMetrics.gauges[runnerGauge.gaugeKey].value\">\n" +
    "            <li>\n" +
    "              <span class=\"properties-label\">Current Stage:</span>\n" +
    "              <span class=\"properties-value\">{{stageNameToLabelMap[gaugeData.currentStage]}}</span>\n" +
    "            </li>\n" +
    "            <li>\n" +
    "              <span class=\"properties-label\">Batch Count:</span>\n" +
    "              <span class=\"properties-value\">{{gaugeData.batchCount}}</span>\n" +
    "            </li>\n" +
    "            <li ng-if=\"gaugeData.currentStage !== 'IDLE'\">\n" +
    "              <span class=\"properties-label\">In Stage Since:</span>\n" +
    "              <span class=\"properties-value\">{{gaugeData.stageStartTime | date:'medium' }}</span>\n" +
    "            </li>\n" +
    "            <li ng-if=\"gaugeData.currentStage !== 'IDLE'\">\n" +
    "              <span class=\"properties-label\">Batch Start Time:</span>\n" +
    "              <span class=\"properties-value\">{{gaugeData.batchStartTime | date:'medium' }}</span>\n" +
    "            </li>\n" +
    "            <li ng-if=\"gaugeData.currentStage !== 'IDLE'\">\n" +
    "              <span class=\"properties-label\">Offset Key:</span>\n" +
    "              <span class=\"properties-value\">{{gaugeData.offsetKey}}</span>\n" +
    "            </li>\n" +
    "            <li ng-if=\"gaugeData.currentStage !== 'IDLE'\">\n" +
    "              <span class=\"properties-label\">Offset Value:</span>\n" +
    "              <span class=\"properties-value\">{{gaugeData.offseValue}}</span>\n" +
    "            </li>\n" +
    "          </ul>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "</div>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"summaryRecordPercentagePieChartTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-if=\"selectedType === pipelineConstant.STAGE_INSTANCE && timeRange === 'latest'\"\n" +
    "       ng-controller=\"RecordPercentagePieChartController\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.recordsProcessed' | translate}}\"\n" +
    "          translate=\"home.detailPane.summaryTab.recordsProcessed\">Records Processed</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body\">\n" +
    "\n" +
    "        <nvd3 ng-if=\"timeRange === 'latest' && !allDataZero\"\n" +
    "              options=\"chartOptions\" data=\"pieChartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "        <span class=\"zero-value\" ng-if=\"allDataZero\">0</span>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"summaryRecordCountBarChartTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-controller=\"RecordCountBarChartController\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.recordCountBarChartTitle' | translate}}\"\n" +
    "              translate=\"home.detailPane.summaryTab.recordCountBarChartTitle\">Record Count (since last startup)</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body\">\n" +
    "\n" +
    "        <nvd3 ng-if=\"timeRange === 'latest'\"\n" +
    "              options=\"chartOptions\" data=\"barChartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "\n" +
    "        <nvd3 ng-if=\"timeRange !== 'latest'\"\n" +
    "              options=\"timeSeriesChartOptions\" data=\"timeSeriesChartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"summaryRecordsThroughputMeterBarChartTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-controller=\"MeterBarChartController\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.recordThroughput' | translate}}\"\n" +
    "              translate=\"home.detailPane.summaryTab.recordThroughput\">Record Throughput</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body\">\n" +
    "\n" +
    "        <nvd3 ng-if=\"timeRange === 'latest'\"\n" +
    "              options=\"chartOptions\" data=\"chartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "\n" +
    "        <nvd3 ng-if=\"timeRange !== 'latest'\"\n" +
    "              options=\"timeSeriesChartOptions\" data=\"timeSeriesChartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"summaryBatchThroughputBarChartTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-if=\"selectedType === pipelineConstant.PIPELINE\"\n" +
    "       ng-controller=\"BatchCountBarChartController\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.batchThroughput' | translate}}\"\n" +
    "          translate=\"home.detailPane.summaryTab.batchThroughput\">Batch Throughput</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body\">\n" +
    "\n" +
    "        <nvd3 ng-if=\"timeRange === 'latest'\"\n" +
    "              options=\"chartOptions\" data=\"chartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "\n" +
    "        <nvd3 ng-if=\"timeRange !== 'latest'\"\n" +
    "              options=\"timeSeriesChartOptions\" data=\"timeSeriesChartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"summaryRecordHistogramTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-if=\"activeConfigStatus.executionMode !== pipelineConstant.CLUSTER && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_BATCH &&\n" +
    "       activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_YARN_STREAMING && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_MESOS_STREAMING && timeRange === 'latest'\"\n" +
    "       ng-controller=\"HistogramChartController\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'global.form.histogram' | translate}}\"\n" +
    "          translate=\"global.form.histogram\">Records Per Batch Histogram (5 minutes decay)</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body\">\n" +
    "        <nvd3 ng-if=\"timeRange === 'latest'\"\n" +
    "              options=\"chartOptions\" data=\"timerData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"summaryPipelineRunnersTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-if=\"selectedType === pipelineConstant.PIPELINE && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_BATCH &&\n" +
    "       activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_YARN_STREAMING && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_MESOS_STREAMING && timeRange === 'latest'\"\n" +
    "       ng-controller=\"RunnersHistogramChartController\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.runnersHistogram' | translate}}\"\n" +
    "              translate=\"home.detailPane.summaryTab.runnersHistogram\">Available Pipeline Runners Histogram (5 minutes decay)</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body\">\n" +
    "          <ul class=\"properties\">\n" +
    "            <li>\n" +
    "              <span class=\"properties-label\">Current runners:</span>\n" +
    "              <span class=\"properties-value\">{{common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.availableRunners}}/{{common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.totalRunners}}</span>\n" +
    "            </li>\n" +
    "          </ul>\n" +
    "        <nvd3 options=\"chartOptions\" data=\"timerData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"summaryRecordsProcessedTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-if=\"activeConfigStatus.executionMode !== pipelineConstant.CLUSTER && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_BATCH &&\n" +
    "       activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_YARN_STREAMING && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_MESOS_STREAMING && timeRange === 'latest'\"\n" +
    "       ng-controller=\"BatchTimerChartController\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.batchProcessingTimer' | translate}}\"\n" +
    "          translate=\"home.detailPane.summaryTab.batchProcessingTimer\">Batch Processing Timer (in seconds)</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body\">\n" +
    "        <nvd3 ng-if=\"timeRange === 'latest'\"\n" +
    "              options=\"chartOptions\" data=\"timerData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"memoryConsumedLineChartTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-if=\"activeConfigStatus.executionMode !== pipelineConstant.CLUSTER && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_BATCH &&\n" +
    "       activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_YARN_STREAMING && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_MESOS_STREAMING && monitorMemoryEnabled\"\n" +
    "       ng-controller=\"MemoryConsumedLineChartController\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.memoryConsumed' | translate}}\"\n" +
    "          translate=\"home.detailPane.summaryTab.memoryConsumed\">Memory Consumed</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body\">\n" +
    "        <nvd3 ng-if=\"timeRange === 'latest'\"\n" +
    "              options=\"chartOptions\" data=\"lineChartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "\n" +
    "        <nvd3 ng-if=\"timeRange !== 'latest'\"\n" +
    "              options=\"timeSeriesChartOptions\" data=\"timeSeriesChartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"summaryRuntimeStatisticsTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-if=\"selectedType === pipelineConstant.PIPELINE && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_BATCH &&\n" +
    "       activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_YARN_STREAMING && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_MESOS_STREAMING\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.runtimeStatistics' | translate}}\"\n" +
    "          translate=\"home.detailPane.summaryTab.runtimeStatistics\">Runtime Statistics</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body double-size-panel\">\n" +
    "\n" +
    "        <ul class=\"table-properties\">\n" +
    "          <li>\n" +
    "            <span class=\"properties-label last-batch\">{{'home.detailPane.summaryTab.lastBatchInputRecordsCount' | translate}}:</span>\n" +
    "            <span class=\"properties-value\">{{common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.lastBatchInputRecordsCount}}</span>\n" +
    "          </li>\n" +
    "\n" +
    "          <li>\n" +
    "            <span class=\"properties-label last-batch\">{{'home.detailPane.summaryTab.lastBatchOutputRecordsCount' | translate}}:</span>\n" +
    "            <span class=\"properties-value\">{{common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.lastBatchOutputRecordsCount}}</span>\n" +
    "          </li>\n" +
    "\n" +
    "          <li>\n" +
    "            <span class=\"properties-label last-batch\">{{'home.detailPane.summaryTab.lastBatchErrorRecordsCount' | translate}}:</span>\n" +
    "            <span class=\"properties-value\">{{common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.lastBatchErrorRecordsCount}}</span>\n" +
    "          </li>\n" +
    "\n" +
    "          <li>\n" +
    "            <span class=\"properties-label last-batch\">{{'home.detailPane.summaryTab.lastBatchErrorMessagesCount' | translate}}:</span>\n" +
    "            <span class=\"properties-value\">{{common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.lastBatchErrorMessagesCount}}</span>\n" +
    "          </li>\n" +
    "\n" +
    "          <li>\n" +
    "            <span class=\"properties-label\">{{'home.detailPane.summaryTab.timeOfLastReceivedRecord' | translate}}:</span>\n" +
    "            <span class=\"properties-value\"\n" +
    "                  ng-if=\"common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.timeOfLastReceivedRecord\"\n" +
    "              >{{common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.timeOfLastReceivedRecord  | date:'medium'}}</span>\n" +
    "          </li>\n" +
    "\n" +
    "          <li>\n" +
    "            <span class=\"properties-label\">{{'home.detailPane.summaryTab.batchCount' | translate}}:</span>\n" +
    "            <span class=\"properties-value\">{{common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.batchCount}}</span>\n" +
    "          </li>\n" +
    "\n" +
    "          <li>\n" +
    "            <span class=\"properties-label\">{{'home.detailPane.summaryTab.idleBatchCount' | translate}}:</span>\n" +
    "            <span class=\"properties-value\">{{common.pipelineMetrics.gauges['RuntimeStatsGauge.gauge'].value.idleBatchCount}}</span>\n" +
    "          </li>\n" +
    "\n" +
    "          <li>\n" +
    "            <span class=\"properties-value\">{{'home.detailPane.summaryTab.threadLastRun' | translate}}</span>\n" +
    "          </li>\n" +
    "\n" +
    "          <li>\n" +
    "            <span class=\"properties-label\">{{'home.detailPane.summaryTab.productionPipelineRunnable' | translate}}:</span>\n" +
    "            <span class=\"properties-value\">{{common.pipelineMetrics.gauges['health.ProductionPipelineRunnable.gauge'].value.timestamp  | date:'medium'}}</span>\n" +
    "          </li>\n" +
    "\n" +
    "          <li>\n" +
    "            <span class=\"properties-label\">{{'home.detailPane.summaryTab.rulesConfigLoaderRunnable' | translate}}:</span>\n" +
    "            <span class=\"properties-value\">{{common.pipelineMetrics.gauges['health.RulesConfigLoaderRunnable.gauge'].value.timestamp  | date:'medium'}}</span>\n" +
    "          </li>\n" +
    "\n" +
    "          <li>\n" +
    "            <span class=\"properties-label\">{{'home.detailPane.summaryTab.dataObserverRunnable' | translate}}:</span>\n" +
    "            <span class=\"properties-value\">{{common.pipelineMetrics.gauges['health.DataObserverRunnable.gauge'].value.timestamp  | date:'medium'}}</span>\n" +
    "          </li>\n" +
    "\n" +
    "          <li>\n" +
    "            <span class=\"properties-label\">{{'home.detailPane.summaryTab.metricObserverRunnable' | translate}}:</span>\n" +
    "            <span class=\"properties-value\">{{common.pipelineMetrics.gauges['health.MetricObserverRunnable.gauge'].value.timestamp  | date:'medium'}}</span>\n" +
    "          </li>\n" +
    "\n" +
    "          <li>\n" +
    "            <span class=\"properties-label\">{{'home.detailPane.summaryTab.metricsEventRunnable' | translate}}:</span>\n" +
    "            <span class=\"properties-value\">{{common.pipelineMetrics.gauges['health.MetricsEventRunnable.gauge'].value.timestamp  | date:'medium'}}</span>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"slaveSDCInstancesTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-if=\"selectedType === pipelineConstant.PIPELINE && (activeConfigStatus.executionMode === pipelineConstant.CLUSTER  || activeConfigStatus.executionMode === pipelineConstant.CLUSTER_BATCH ||\n" +
    "       activeConfigStatus.executionMode === pipelineConstant.CLUSTER_YARN_STREAMING || activeConfigStatus.executionMode === pipelineConstant.CLUSTER_MESOS_STREAMING)\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.slaveSDCInstances' | translate}}\"\n" +
    "              translate=\"home.detailPane.summaryTab.slaveSDCInstances\">Slave SDC Instances</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body\">\n" +
    "        <ul class=\"table-properties\">\n" +
    "          <li ng-repeat=\"sdcURL in common.pipelineMetrics.slaves\">\n" +
    "            <a href=\"/controlpanel/dataflow/app/rest/v1/cluster/redirectToSlave?name={{pipelineConfig.info.pipelineId}}&sdcURL={{sdcURL}}\"\n" +
    "               target=\"_blank\">{{sdcURL}}</a>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "\n" +
    "        <span class=\"no-data-available\"\n" +
    "              ng-if=\"!common.pipelineMetrics || !common.pipelineMetrics.slaves || common.pipelineMetrics.slaves.length === 0\"\n" +
    "              translate=\"global.messages.info.noDataAvailable\">No Data Available.</span>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"summaryAllStageBatchTimerBarChartTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-if=\"selectedType === pipelineConstant.PIPELINE && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_BATCH &&\n" +
    " activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_YARN_STREAMING && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_MESOS_STREAMING\"\n" +
    "       ng-controller=\"AllStageBatchTimerChartController\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.stageBatchProcessingTimer' | translate}}\"\n" +
    "              translate=\"home.detailPane.summaryTab.stageBatchProcessingTimer\">Stage Batch Processing Timer (in seconds)</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body double-size-panel\">\n" +
    "\n" +
    "        <nvd3 ng-if=\"timeRange === 'latest' && !allDataZero\"\n" +
    "              options=\"chartOptions\" data=\"chartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "\n" +
    "        <nvd3 ng-if=\"timeRange !== 'latest' && !allDataZero\"\n" +
    "              options=\"timeSeriesChartOptions\" data=\"timeSeriesChartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "\n" +
    "        <span class=\"zero-value\" ng-if=\"allDataZero\">0</span>\n" +
    "\n" +
    "\n" +
    "        <ul class=\"properties\" ng-if=\"timeRange === 'latest'\">\n" +
    "          <li>\n" +
    "            <span class=\"properties-label\"> Total:</span>\n" +
    "            <span class=\"properties-value\">{{totalValue}} seconds</span>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"summaryAllStageMemoryConsumedBarChartTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-if=\"selectedType === pipelineConstant.PIPELINE && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_BATCH &&\n" +
    "activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_YARN_STREAMING && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_MESOS_STREAMING && monitorMemoryEnabled\"\n" +
    "       ng-controller=\"AllStageMemoryConsumedChartController\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.stageMemoryConsumed' | translate}}\"\n" +
    "              translate=\"home.detailPane.summaryTab.stageMemoryConsumed\">Stage Heap Memory Usage</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body double-size-panel\">\n" +
    "\n" +
    "        <nvd3 ng-if=\"timeRange === 'latest' && !allDataZero\"\n" +
    "              options=\"chartOptions\" data=\"chartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "\n" +
    "        <nvd3 ng-if=\"timeRange !== 'latest' && !allDataZero\"\n" +
    "              options=\"timeSeriesChartOptions\" data=\"timeSeriesChartData\" config=\"{ refreshDataOnly: true }\"></nvd3>\n" +
    "\n" +
    "        <span class=\"zero-value\" ng-if=\"allDataZero\">0</span>\n" +
    "\n" +
    "        <ul class=\"properties\" ng-if=\"timeRange === 'latest'\">\n" +
    "          <li>\n" +
    "            <span class=\"properties-label\"> Total:</span>\n" +
    "            <span class=\"properties-value\">{{totalValue}} MB</span>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "\n" +
    "\n" +
    "<script type=\"text/ng-template\" id=\"summaryRuntimeConstantsTemplate\">\n" +
    "  <div class=\"col-sm-4\"\n" +
    "       ng-if=\"selectedType === pipelineConstant.PIPELINE && activeConfigStatus.attributes && activeConfigStatus.attributes['RUNTIME_PARAMETERS']\">\n" +
    "    <div class=\"panel panel-default\">\n" +
    "      <div class=\"panel-heading\">\n" +
    "        <span title=\"{{'home.detailPane.summaryTab.runtimeParameters' | translate}}\"\n" +
    "              translate=\"home.detailPane.summaryTab.runtimeParameters\">Runtime Parameters</span>\n" +
    "        <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <div class=\"panel-body\">\n" +
    "        <ul class=\"table-properties\">\n" +
    "          <li ng-repeat=\"(key, value) in activeConfigStatus.attributes['RUNTIME_PARAMETERS']\">\n" +
    "            <span class=\"properties-label last-batch\">{{key}}:</span>\n" +
    "            <span class=\"properties-value\">{{value}}</span>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</script>\n" +
    "");
}]);

angular.module("app/home/graph/graph.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/graph/graph.tpl.html",
    "<div class=\"panel panel-default graph-pane\"\n" +
    "     ng-controller=\"GraphController\">\n" +
    "\n" +
    "  <div class=\"panel-body\" droppable drop=\"stageDrop\">\n" +
    "\n" +
    "    <div ng-if=\"!sourceExists && pipelineConfig && !isPipelineReadOnly && !$storage.maximizeDetailPane && !$storage.dontShowHelpAlert\"\n" +
    "         class=\"alert alert-dismissible alert-warning alert-select-stage\" role=\"alert\">\n" +
    "\n" +
    "      <button type=\"button\" class=\"close\" data-dismiss=\"alert\">\n" +
    "        <span aria-hidden=\"true\">&times;</span>\n" +
    "        <span class=\"sr-only\">Close</span>\n" +
    "      </button>\n" +
    "\n" +
    "      <span  class=\"validation-message\">{{'home.graphPane.originMissing' | translate}}</span>\n" +
    "\n" +
    "      <select class=\"form-control\" name=\"newPipelineSource\"\n" +
    "          ng-options=\"(stage.label + ' - ' +  stage.libraryLabel) for stage in sources | orderBy: 'label'\"\n" +
    "          ng-model=\"selectedSource.selected\"\n" +
    "          ng-change=\"onSelectSourceChange()\">\n" +
    "        <option value=\"\">\n" +
    "          {{'home.library.sourcePlaceholder' | translate}}\n" +
    "        </option>\n" +
    "      </select>\n" +
    "\n" +
    "      <div class=\"pull-right checkbox dont-show-again\">\n" +
    "        <label>\n" +
    "          <input type=\"checkbox\" name=\"dontShowHelpAgain\"\n" +
    "                 ng-model=\"$storage.dontShowHelpAlert\">  {{'home.graphPane.dontShowAgain' | translate}}\n" +
    "        </label>\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "    <div class=\"alert alert-dismissible alert-warning alert-select-stage\" role=\"alert\"\n" +
    "         ng-if=\"!isPipelineReadOnly && pipelineConfig && firstOpenLane.stageInstance &&\n" +
    "         common.errors.length === 0 && !previewMode && !$storage.maximizeDetailPane\">\n" +
    "\n" +
    "      <button type=\"button\" class=\"close\" ng-click=\"firstOpenLane.stageInstance = undefined\">\n" +
    "        <span aria-hidden=\"true\">&times;</span>\n" +
    "        <span class=\"sr-only\">Close</span>\n" +
    "      </button>\n" +
    "\n" +
    "      <span class=\"validation-message\" ng-if=\"!firstOpenLane.isEventLane\">\n" +
    "        {{firstOpenLane.stageInstance.uiInfo.label}} has open stream {{(firstOpenLane.stageInstance.outputLanes.length > 1) ?\n" +
    "        ': stream ' + (firstOpenLane.laneIndex + 1) : ''}}\n" +
    "      </span>\n" +
    "\n" +
    "\n" +
    "      <span class=\"validation-message\" ng-if=\"firstOpenLane.isEventLane\">\n" +
    "        {{firstOpenLane.stageInstance.uiInfo.label}} has open event stream\n" +
    "      </span>\n" +
    "\n" +
    "      <select class=\"form-control\" name=\"newPipelineProcessor\"\n" +
    "              ng-options=\"(stage.label + ' - ' +  stage.libraryLabel) for stage in processors | orderBy: 'label'\"\n" +
    "              ng-model=\"connectStage.selected\"\n" +
    "              ng-change=\"onConnectStageChange()\">\n" +
    "        <option value=\"\">\n" +
    "          {{'home.graphPane.selectProcessor' | translate}}\n" +
    "        </option>\n" +
    "      </select>\n" +
    "\n" +
    "      <span class=\"pull-left123\"> Or </span>\n" +
    "\n" +
    "      <select class=\"form-control\" name=\"newPipelineTarget\"\n" +
    "              ng-options=\"(stage.label + ' - ' +  stage.libraryLabel) for stage in targets | orderBy: 'label'\"\n" +
    "              ng-model=\"connectStage.selected\"\n" +
    "              ng-change=\"onConnectStageChange()\">\n" +
    "        <option value=\"\">\n" +
    "          {{'home.graphPane.selectTarget' | translate}}\n" +
    "        </option>\n" +
    "      </select>\n" +
    "\n" +
    "      <span ng-if=\"firstOpenLane.isEventLane\" class=\"pull-left123\"> Or </span>\n" +
    "\n" +
    "      <select class=\"form-control\" name=\"newPipelineExecutor\"\n" +
    "              ng-if=\"firstOpenLane.isEventLane\"\n" +
    "              ng-options=\"(stage.label + ' - ' +  stage.libraryLabel) for stage in executors | orderBy: 'label'\"\n" +
    "              ng-model=\"connectStage.selected\"\n" +
    "              ng-change=\"onConnectStageChange()\">\n" +
    "        <option value=\"\">\n" +
    "          {{'home.graphPane.selectExecutor' | translate}}\n" +
    "        </option>\n" +
    "      </select>\n" +
    "\n" +
    "      <div class=\"pull-right checkbox dont-show-again\">\n" +
    "        <label>\n" +
    "          <input type=\"checkbox\" name=\"dontShowHelpAgain\"\n" +
    "                 ng-model=\"$storage.dontShowHelpAlert\">  {{'home.graphPane.dontShowAgain' | translate}}\n" +
    "        </label>\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"alert alert-dismissible alert-info alert-select-stage\" role=\"alert\"\n" +
    "         ng-if=\"!isPipelineRunning && !isPipelineReadOnly && pipelineConfig && selectedType === pipelineConstant.LINK &&\n" +
    "         !previewMode && !$storage.maximizeDetailPane && !$storage.dontShowHelpAlert\">\n" +
    "\n" +
    "      <button type=\"button\" class=\"close\" ng-click=\"$storage.dontShowHelpAlert = !$storage.dontShowHelpAlert\">\n" +
    "        <span aria-hidden=\"true\">&times;</span>\n" +
    "        <span class=\"sr-only\">Close</span>\n" +
    "      </button>\n" +
    "\n" +
    "\n" +
    "      <select class=\"form-control stage-in-between\" name=\"newPipelineProcessor\"\n" +
    "              ng-options=\"(stage.label + ' - ' +  stage.libraryLabel) for stage in processors | orderBy: 'label'\"\n" +
    "              ng-model=\"insertStage.selected\"\n" +
    "              ng-change=\"onInsertStageChange()\">\n" +
    "        <option value=\"\">\n" +
    "          {{'home.graphPane.selectProcessorToAddInBetween' | translate}}\n" +
    "        </option>\n" +
    "      </select>\n" +
    "\n" +
    "\n" +
    "      <div class=\"pull-right checkbox dont-show-again\">\n" +
    "        <label>\n" +
    "          <input type=\"checkbox\" name=\"dontShowHelpAgain\"\n" +
    "                 ng-model=\"$storage.dontShowHelpAlert\">  {{'home.graphPane.dontShowAgain' | translate}}\n" +
    "        </label>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\" ng-if=\"!$storage.maximizeDetailPane\"></ng-include>\n" +
    "    <ng-include src=\"'app/home/alerts/info/infoAlert.tpl.html'\" ng-if=\"!$storage.maximizeDetailPane\"></ng-include>\n" +
    "    <ng-include src=\"'app/home/alerts/success/successAlert.tpl.html'\" ng-if=\"!$storage.maximizeDetailPane\"></ng-include>\n" +
    "\n" +
    "    <pipeline-graph ng-show=\"pipelineConfig\" graph-value=\"pipelineConfig\" ></pipeline-graph>\n" +
    "\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/header/addLabel/addLabelConfirmation.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/header/addLabel/addLabelConfirmation.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "    <span aria-hidden=\"true\">&times;</span>\n" +
    "    <span class=\"sr-only\">Close</span>\n" +
    "  </button>\n" +
    "  <h3 class=\"modal-title\" translate=\"home.header.addLabelsTitle\">Add Labels to Pipelines</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body multiple-pipelines-modal-body\">\n" +
    "  <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "  <p>\n" +
    "    <p translate=\"home.header.addLabelsConfirmationMessage\"></p>\n" +
    "\n" +
    "    <p>\n" +
    "      <div class=\"controls\">\n" +
    "        <ui-select multiple\n" +
    "                   tagging\n" +
    "                   tagging-label=\" - new label\"\n" +
    "                   tagging-tokens=\",|ENTER\"\n" +
    "                   ng-model=\"data.labels\"\n" +
    "                   ng-disabled=\"addingLabels\">\n" +
    "          <ui-select-match class=\"ui-select-match\">{{$item}}</ui-select-match>\n" +
    "          <ui-select-choices class=\"ui-select-choices\" repeat=\"listValue in []\">\n" +
    "            <div ng-bind-html=\"listValue | highlight: $select.search\"></div>\n" +
    "          </ui-select-choices>\n" +
    "        </ui-select>\n" +
    "      </div>\n" +
    "    </p>\n" +
    "\n" +
    "    <p translate=\"home.header.addLabelsSelectedPipelines\"></p>\n" +
    "    <p ng-repeat=\"pipeline in pipelineInfoList\">{{pipeline.title}}</p>\n" +
    "    <p translate=\"home.header.addingLabelsToPipelines\" ng-if=\"addingLabels\"></p>\n" +
    "  </div>\n" +
    "\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"button\"\n" +
    "          class=\"btn btn-default\"\n" +
    "          ng-if=\"!common.errors || common.errors.length == 0\"\n" +
    "          ng-click=\"cancel()\"\n" +
    "          ng-disabled=\"addingLabels\"\n" +
    "          translate=\"global.form.cancel\">Cancel</button>\n" +
    "  <button type=\"button\"\n" +
    "          class=\"btn btn-primary\"\n" +
    "          ng-if=\"!common.errors || common.errors.length == 0\"\n" +
    "          ng-click=\"save()\"\n" +
    "          ng-disabled=\"addingLabels\"\n" +
    "          translate=\"global.form.save\">Save</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/header/downloadExecutable/downloadExecutable.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/header/downloadExecutable/downloadExecutable.tpl.html",
    "<div class=\"download-executable-modal-form\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "\n" +
    "    <h3 class=\"modal-title\" ng-if=\"!downloaded\" translate=\"home.header.downloadExecutable\">Download Edge Executable</h3>\n" +
    "\n" +
    "    <h3 class=\"modal-title\" ng-if=\"downloaded\" translate=\"global.form.quickTips\">Quick Tips</h3>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div ng-if=\"!downloaded\">\n" +
    "      <div class=\"form-group\">\n" +
    "        <label class=\"control-label\" translate=\"global.form.os\">OS</label>\n" +
    "        <select class=\"form-control\"\n" +
    "                name=\"density\"\n" +
    "                ng-model=\"downloadModel.selectedEdgeOs\">\n" +
    "          <option value=\"{{pipelineConstant.DARWIN_OS}}\"\n" +
    "                  ng-selected=\"downloadModel.selectedEdgeOs === pipelineConstant.DARWIN_OS\">Darwin (Mac OS)</option>\n" +
    "          <option value=\"{{pipelineConstant.LINUX_OS}}\"\n" +
    "                  ng-selected=\"downloadModel.selectedEdgeOs === pipelineConstant.LINUX_OS\">Linux</option>\n" +
    "          <option value=\"{{pipelineConstant.WINDOWS_OS}}\"\n" +
    "                  ng-selected=\"downloadModel.selectedEdgeOs === pipelineConstant.WINDOWS_OS\">Windows</option>\n" +
    "        </select>\n" +
    "      </div>\n" +
    "      <div class=\"form-group\">\n" +
    "        <label class=\"control-label\" translate=\"global.form.architecture\">Architecture</label>\n" +
    "        <select class=\"form-control\"\n" +
    "                name=\"density\"\n" +
    "                ng-model=\"downloadModel.selectedEdgeArch\">\n" +
    "          <option value=\"{{pipelineConstant.AMD64_ARCH}}\"\n" +
    "                  ng-selected=\"downloadModel.selectedEdgeArch === pipelineConstant.AMD64_ARCH\">AMD64</option>\n" +
    "          <option value=\"{{pipelineConstant.ARM_ARCH}}\"\n" +
    "                  ng-selected=\"downloadModel.selectedEdgeArch === pipelineConstant.ARM_ARCH\">ARM</option>\n" +
    "        </select>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"downloaded\">\n" +
    "      <div class=\"stepper\">\n" +
    "        <div class=\"step cursor-pointer\">\n" +
    "          <div>\n" +
    "            <div class=\"title\">1. Extract the downloaded tar file</div>\n" +
    "            <div class=\"body\">\n" +
    "              <pre>tar -xvf streamsets-datacollector-edge*.tgz</pre>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "        <div class=\"step cursor-pointer\">\n" +
    "          <div>\n" +
    "            <div class=\"title\">2. Start SDC Edge and an Edge Pipeline</div>\n" +
    "            <div class=\"body\">\n" +
    "              <pre>\n" +
    "cd streamsets-datacollector-edge\n" +
    "bin/edge --start={{pipelineConfig.pipelineId}}\n" +
    "              </pre>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "        <div class=\"step cursor-pointer\">\n" +
    "          <div>\n" +
    "            <div class=\"title\">3. Use REST APIs to manage edge pipelines</div>\n" +
    "            <div class=\"body\">\n" +
    "              <pre>\n" +
    "### List all pipelines\n" +
    "    curl -X GET http://localhost:18633/rest/v1/pipelines\n" +
    "\n" +
    "### Start Pipeline\n" +
    "    curl -X POST http://localhost:18633/rest/v1/pipeline/{{pipelineConfig.pipelineId}}/start\n" +
    "\n" +
    "### Check Pipeline Status\n" +
    "    curl -X GET http://localhost:18633/rest/v1/pipeline/{{pipelineConfig.pipelineId}}/status\n" +
    "\n" +
    "### Check Pipeline Metrics\n" +
    "    curl -X GET http://localhost:18633/rest/v1/pipeline/{{pipelineConfig.pipelineId}}/metrics\n" +
    "\n" +
    "### Stop Pipeline\n" +
    "    curl -X POST http://localhost:18633/rest/v1/pipeline/{{pipelineConfig.pipelineId}}/stop\n" +
    "\n" +
    "### Reset Origin Offset\n" +
    "    curl -X POST http://localhost:18633/rest/v1/pipeline/{{pipelineConfig.pipelineId}}/resetOffset\n" +
    "              </pre>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"button\"\n" +
    "            ng-if=\"!downloaded\"\n" +
    "            class=\"btn btn-default\"\n" +
    "            ng-click=\"cancel()\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "    <button type=\"button\"\n" +
    "            ng-if=\"!downloaded\"\n" +
    "            class=\"btn btn-primary\"\n" +
    "            ng-click=\"download()\"\n" +
    "            translate=\"global.form.download\">Download</button>\n" +
    "    <button type=\"button\"\n" +
    "            ng-if=\"downloaded\"\n" +
    "            class=\"btn btn-primary\"\n" +
    "            ng-click=\"done()\"\n" +
    "            translate=\"global.form.done\">Done</button>\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/header/header.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/header/header.tpl.html",
    "<div class=\"panel panel-default header-pane\"\n" +
    "     ng-controller=\"HeaderController\">\n" +
    "\n" +
    "  <div class=\"panel-heading clearfix\" ng-show=\"pipelineConfig\">\n" +
    "\n" +
    "    <div class=\"pull-right\" ng-if=\"pipelineConfig && activeConfigStatus.status !== 'STOPPING' && activeConfigStatus.status !== 'CONNECTING' && activeConfigStatus.status !== 'DISCONNECTING' && activeConfigStatus.status !== 'STARTING_ERROR' && activeConfigStatus.status !== 'RUNNING_ERROR' && activeConfigStatus.status !== 'STOPPING_ERROR' && activeConfigStatus.status !== 'FINISHING'\">\n" +
    "\n" +
    "      <div class=\"btn-group\" dropdown\n" +
    "           ng-hide=\"previewMode || snapshotMode\">\n" +
    "       <span class=\"btn btn-link dropdown-toggle icon-button\" dropdown-toggle aria-expanded=\"true\"\n" +
    "             tooltip-placement=\"bottom\" tooltip=\"{{'home.header.more' | translate}}\"\n" +
    "             tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "             tooltip-popup-delay=\"500\"\n" +
    "             ng-click=\"$event.stopPropagation();\">\n" +
    "          <i class=\"fa fa-ellipsis-h fa-14x\"></i>\n" +
    "        </span>\n" +
    "        <ul class=\"dropdown-menu pull-right\" role=\"menu\">\n" +
    "          <!--<li role=\"presentation\"\n" +
    "              ng-if=\"isAuthorized([userRoles.admin, userRoles.manager]) && canExecute && !(isPipelineRunning || previewMode || snapshotMode)\"\n" +
    "              ng-class=\"{'disabled': !pipelineConfig.valid}\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"(!pipelineConfig.valid) || startPipelineWithParameters()\">\n" +
    "              <i class=\"glyphicon glyphicon-play\"></i> {{'home.header.startWithParametersTitle' | translate}}\n" +
    "            </a>\n" +
    "          </li>-->\n" +
    "\n" +
    "          <li role=\"presentation\"\n" +
    "              ng-if=\"!isPipelineRunning && isAuthorized([userRoles.admin, userRoles.manager])\"\n" +
    "              ng-class=\"{'disabled': !pipelineConfig.valid || activeConfigStatus.executionMode != pipelineConstant.STANDALONE}\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"(!pipelineConfig.valid || activeConfigStatus.executionMode != pipelineConstant.STANDALONE) || resetOffset()\">\n" +
    "              <i class=\"fa fa-power-off\"></i> {{'home.resetOffset.title' | translate}}\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <!--<li role=\"presentation\"\n" +
    "              ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\"\n" +
    "              ng-hide=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING'\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"deletePipelineConfig(activeConfigInfo, $event)\">\n" +
    "              <i class=\"fa fa-trash-o\"></i>\n" +
    "              <span>{{'global.form.delete' | translate}}</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"duplicatePipelineConfig(activeConfigInfo, $event)\">\n" +
    "              <i class=\"fa fa-files-o\"></i>\n" +
    "              <span>{{'global.form.duplicate' | translate}}</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "          -->\n" +
    "          <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"importPipelineConfig(activeConfigInfo, $event)\">\n" +
    "              <i class=\"glyphicon glyphicon-import\"></i>\n" +
    "              <span>{{'global.form.import' | translate}}</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "        \n" +
    "          <li role=\"presentation\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"exportPipelineConfig(activeConfigInfo, false, $event)\">\n" +
    "              <i class=\"glyphicon glyphicon-export\"></i>\n" +
    "              <span>{{'global.form.export' | translate}}</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "          <!--\n" +
    "          <li role=\"presentation\" ng-if=\"activeConfigInfo.valid\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"exportPipelineConfig(activeConfigInfo, true, $event)\">\n" +
    "              <i class=\"glyphicon glyphicon-export\"></i>\n" +
    "              <span>{{'global.form.exportWithLibraryDefinitions' | translate}}</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\"\n" +
    "              ng-if=\"!isPipelineRunning && isAuthorized([userRoles.admin, userRoles.manager]) && activeConfigStatus.executionMode != pipelineConstant.CLUSTER  && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_BATCH && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_YARN_STREAMING && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_MESOS_STREAMING\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"viewSnapshots()\">\n" +
    "              <i class=\"glyphicon glyphicon-camera\"></i> {{'home.header.snapshots' | translate}}\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"isPipelineRunning && !monitoringPaused\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"pauseMonitoring()\">\n" +
    "              <i class=\"fa fa-pause\"></i> {{'home.header.pauseMonitoring' | translate}}\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"isPipelineRunning && monitoringPaused\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"continueMonitoring()\">\n" +
    "              <i class=\"fa fa-play\"></i> {{'home.header.continueMonitoring' | translate}}\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && canExecute && pipelineConfig.previewable && activeConfigStatus.executionMode != pipelineConstant.SLAVE && !(isPipelineRunning || previewMode || snapshotMode)\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"!pipelineConfig.previewable || previewPipeline(true)\">\n" +
    "              <i class=\"glyphicon glyphicon-eye-open\"></i> {{'home.header.previewConfig' | translate}}\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"fragmentsScope.containsFragments && !fragmentsScope.fragmentsExpanded\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"expandAllFragments()\">\n" +
    "              <i class=\"fa fa-expand\"></i> {{'home.header.expandAllFragments' | translate}}\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"fragmentsScope.containsFragments && fragmentsScope.fragmentsExpanded\">\n" +
    "            <a href=\"#\" dropdown-toggle ng-click=\"collapseAllFragments()\">\n" +
    "              <i class=\"fa fa-compress\"></i> {{'home.header.collapseAllFragments' | translate}}\n" +
    "            </a>\n" +
    "          </li>-->\n" +
    "\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"btn-group issues-dropdown\"\n" +
    "           ng-show=\"pipelineConfig && pipelineConfig.issues.issueCount > 0 && !previewMode && !snapshotMode\">\n" +
    "        <a class=\"btn btn-link dropdown-toggle\" data-toggle=\"dropdown\"\n" +
    "           tooltip-placement=\"top\"\n" +
    "           tooltip=\"{{'home.header.issues' | translate}}\"\n" +
    "           tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "           tooltip-popup-delay=\"500\">\n" +
    "          <span class=\"fa fa-exclamation-triangle fa-12x\"></span>\n" +
    "          <span ng-hide=\"iconOnly\" translate=\"home.header.issues\">Issues</span>\n" +
    "          <span class=\"badge\" ng-bind=\"pipelineConfig.issues.issueCount\"></span>\n" +
    "        </a>\n" +
    "        <ul class=\"dropdown-menu pull-right scrollable-menu\" role=\"menu\" aria-labelledby=\"dropdownMenu1\">\n" +
    "\n" +
    "          <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "              ng-if=\"pipelineConfig.issues.pipelineIssues.length\"\n" +
    "              translate=\"home.header.pipelineIssues\">\n" +
    "            Pipeline Issues\n" +
    "          </li>\n" +
    "          <li role=\"presentation\" ng-repeat=\"issue in pipelineConfig.issues.pipelineIssues\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"onIssueClick(issue)\">{{issue.message}}</a>\n" +
    "          </li>\n" +
    "          <li role=\"presentation\" class=\"divider\" ng-if=\"pipelineConfig.issues.pipelineIssues.length\"></li>\n" +
    "\n" +
    "          <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "              ng-repeat-start=\"(instanceName, issues) in pipelineConfig.issues.stageIssues\"\n" +
    "              ng-bind=\"getStageInstanceLabel(instanceName)\">\n" +
    "          </li>\n" +
    "          <li role=\"presentation\" ng-repeat=\"issue in issues\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"onIssueClick(issue, instanceName)\"\n" +
    "               ng-bind=\"getIssuesMessage(instanceName, issue)\"></a>\n" +
    "          </li>\n" +
    "          <li ng-repeat-end role=\"presentation\" class=\"divider\" ng-if=\"!$last\"></li>\n" +
    "\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button btn-selected\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{'home.header.downloadExecutable' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         ng-if=\"isEdgePipeline()\"\n" +
    "         ng-disabled=\"!pipelineConfig.valid\"\n" +
    "         ng-click=\"pipelineConfig.valid && downloadEdgeExecutable()\">\n" +
    "        <span class=\"fa fa-file-archive-o fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <div class=\"btn-group\" dropdown\n" +
    "           ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && activeConfigStatus.executionMode != pipelineConstant.SLAVE && common.isDPMEnabled\"\n" +
    "           ng-hide=\"isPipelineRunning || previewMode || snapshotMode\">\n" +
    "       <span class=\"btn btn-link dropdown-toggle icon-button\" dropdown-toggle aria-expanded=\"true\"\n" +
    "             tooltip-placement=\"right\" tooltip=\"{{'home.header.remoteOptions' | translate}}\"\n" +
    "             tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "             tooltip-popup-delay=\"500\"\n" +
    "             ng-disabled=\"!pipelineConfig.valid && !isDPMPipelineDirty\"\n" +
    "             ng-click=\"$event.stopPropagation();\">\n" +
    "          <i class=\"fa fa-cloud fa-14x\"></i>\n" +
    "        </span>\n" +
    "        <ul class=\"dropdown-menu pull-right\" role=\"menu\">\n" +
    "          <li role=\"presentation\" ng-class=\"{'disabled': !pipelineConfig.valid}\">\n" +
    "            <a href=\"#\" dropdown-toggle\n" +
    "               ng-click=\"pipelineConfig.valid && publishPipeline(pipelineConfig.info)\"><i class=\"fa fa-cloud-upload\"></i> {{'global.form.publish' | translate}}</a>\n" +
    "          </li>\n" +
    "          <li role=\"presentation\" ng-class=\"{'disabled': !pipelineConfig.metadata }\">\n" +
    "            <a href=\"#\" dropdown-toggle\n" +
    "               ng-click=\"pipelineConfig.metadata && showCommitHistory(pipelineConfig.info, pipelineConfig.metadata)\"><i class=\"fa fa-history\"></i> {{'home.header.commitHistory' | translate}}</a>\n" +
    "          </li>\n" +
    "          <li role=\"presentation\" ng-class=\"{'disabled': !pipelineConfig.metadata || !isDPMPipelineDirty}\">\n" +
    "            <a href=\"#\" dropdown-toggle\n" +
    "               ng-click=\"isDPMPipelineDirty && revertDPMPipelineChanges(pipelineConfig.info, pipelineConfig.metadata)\"><i class=\"fa fa-eraser\"></i> {{'home.header.revertChanges' | translate}}</a>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{'global.form.publishToEdge' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         ng-hide=\"previewMode || snapshotMode\"\n" +
    "         ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && isEdgePipeline()\"\n" +
    "         ng-click=\"publishToEdge()\">\n" +
    "        <span class=\"fa fa-upload fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a target=\"_blank\"\n" +
    "         class=\"btn btn-link icon-button\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{'global.form.viewLogs' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         ng-hide=\"previewMode || snapshotMode\"\n" +
    "         ng-if=\"isAuthorized([userRoles.admin, userRoles.creator, userRoles.manager])\"\n" +
    "         ng-href=\"/controlpanel/dataflow/app/collector/logs/{{pipelineConfig.info.title + '/' + pipelineConfig.info.pipelineId}}\">\n" +
    "        <span class=\"fa fa-file-text fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{'global.form.undo' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         ng-hide=\"isPipelineRunning || previewMode || snapshotMode\"\n" +
    "         ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && activeConfigStatus.executionMode != pipelineConstant.SLAVE\"\n" +
    "         ng-disabled=\"!canUndo()\"\n" +
    "         ng-click=\"!canUndo() || undo()\">\n" +
    "        <span class=\"fa fa-undo fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{'global.form.redo' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         ng-hide=\"isPipelineRunning || previewMode || snapshotMode\"\n" +
    "         ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && activeConfigStatus.executionMode != pipelineConstant.SLAVE\"\n" +
    "         ng-disabled=\"!canRedo()\"\n" +
    "         ng-click=\"!canRedo() || redo()\">\n" +
    "        <span class=\"fa fa-repeat fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{selectedType === pipelineConstant.STAGE_INSTANCE ?\n" +
    "          ('global.form.deleteStage' | translate) + ' ' + selectedObject.uiInfo.label : ('global.form.deleteStream' | translate)}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         ng-hide=\"isPipelineRunning || previewMode || snapshotMode\"\n" +
    "         ng-disabled=\"selectedType === pipelineConstant.PIPELINE\"\n" +
    "         ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && activeConfigStatus.executionMode != pipelineConstant.SLAVE && !isPipelineReadOnly\"\n" +
    "         ng-click=\"(selectedType === pipelineConstant.PIPELINE) || deleteSelection()\">\n" +
    "        <span class=\"fa fa-trash fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{('global.form.duplicateStage' | translate) + ' ' + selectedObject.uiInfo.label}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         ng-hide=\"isPipelineRunning || previewMode || snapshotMode\"\n" +
    "         ng-disabled=\"selectedType !== pipelineConstant.STAGE_INSTANCE || selectedObject.uiInfo.stageType === pipelineConstant.SOURCE_STAGE_TYPE\"\n" +
    "         ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && activeConfigStatus.executionMode != pipelineConstant.SLAVE && !isPipelineReadOnly\"\n" +
    "         ng-click=\"(selectedType !== pipelineConstant.STAGE_INSTANCE || selectedObject.uiInfo.stageType === pipelineConstant.SOURCE_STAGE_TYPE) || duplicateStage()\">\n" +
    "        <span class=\"glyphicon glyphicon-duplicate fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{'global.form.autoArrange' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         ng-hide=\"previewMode || snapshotMode || pipelineConfig.stages.length <= 1\"\n" +
    "         ng-if=\"isAuthorized([userRoles.admin, userRoles.creator, userRoles.manager]) && activeConfigStatus.executionMode != pipelineConstant.SLAVE\"\n" +
    "         ng-click=\"autoArrange()\">\n" +
    "        <span class=\"fa fa-random fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "          tooltip-placement=\"bottom\" tooltip=\"{{'home.header.snapshots' | translate}}\"\n" +
    "          tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "          tooltip-popup-delay=\"500\"\n" +
    "          ng-if=\"isAuthorized([userRoles.admin, userRoles.manager]) && activeConfigStatus.executionMode != pipelineConstant.CLUSTER  && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_BATCH && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_YARN_STREAMING && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_MESOS_STREAMING && isPipelineRunning && !snapshotMode && activeConfigStatus.status != 'CONNECT_ERROR' && activeConfigStatus.status != 'RETRY'\"\n" +
    "          ng-click=\"viewSnapshots()\">\n" +
    "        <span class=\"glyphicon glyphicon-camera fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button-danger\"\n" +
    "          tooltip-placement=\"bottom\" tooltip=\"{{'home.header.closeSnapshot' | translate}}\"\n" +
    "          tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "          tooltip-popup-delay=\"500\"\n" +
    "          ng-if=\"isAuthorized([userRoles.admin, userRoles.manager]) && snapshotMode\"\n" +
    "          ng-click=\"closeSnapshot()\">\n" +
    "        <span class=\"glyphicon glyphicon-camera fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "          tooltip-placement=\"bottom\" tooltip=\"{{'home.header.preview' | translate}}\"\n" +
    "          tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "          tooltip-popup-delay=\"500\"\n" +
    "          ng-if=\"isAuthorized([userRoles.admin, userRoles.creator, userRoles.manager]) && activeConfigStatus.executionMode != pipelineConstant.SLAVE && !(isPipelineRunning || previewMode || snapshotMode) && !isEdgePipeline()\"\n" +
    "          ng-disabled=\"!pipelineConfig.previewable\"\n" +
    "          ng-click=\"!pipelineConfig.previewable || previewPipeline()\">\n" +
    "        <span class=\"glyphicon glyphicon-eye-open fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button-danger\"\n" +
    "          tooltip-placement=\"bottom\" tooltip=\"{{'home.header.closePreview' | translate}}\"\n" +
    "          tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "          tooltip-popup-delay=\"500\"\n" +
    "          ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && previewMode\"\n" +
    "          ng-click=\"closePreview()\">\n" +
    "        <span class=\"glyphicon glyphicon-eye-close fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{'home.header.validate' | translate}}\"\n" +
    "         tooltip-trigger=\"mouseenter\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         ng-if=\"isAuthorized([userRoles.admin, userRoles.creator, userRoles.manager]) && activeConfigStatus.executionMode != pipelineConstant.SLAVE && !isEdgePipeline()\"\n" +
    "         ng-hide=\"isPipelineRunning || previewMode || snapshotMode\"\n" +
    "         ng-disabled=\"!pipelineConfig.valid\"\n" +
    "         ng-click=\"!pipelineConfig.valid || validatePipeline()\">\n" +
    "        <span class=\"glyphicon glyphicon-ok-circle fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "          tooltip-placement=\"bottom\" tooltip=\"{{'home.header.start' | translate}}\"\n" +
    "          tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "          tooltip-popup-delay=\"500\"\n" +
    "          ng-if=\"isAuthorized([userRoles.admin, userRoles.manager]) && canExecute && !(isPipelineRunning || previewMode || snapshotMode)\"\n" +
    "          ng-disabled=\"!pipelineConfig.valid\"\n" +
    "          ng-click=\"!pipelineConfig.valid || startPipeline(); \">\n" +
    "        <span class=\"glyphicon glyphicon-play fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button-danger\"\n" +
    "          tooltip-placement=\"bottom\" tooltip=\"{{'home.header.stop' | translate}}\"\n" +
    "          tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "          tooltip-popup-delay=\"500\"\n" +
    "          ng-if=\"isAuthorized([userRoles.admin, userRoles.manager]) && canExecute && activeConfigStatus.executionMode != pipelineConstant.SLAVE\"\n" +
    "          ng-click=\"stopPipeline(false)\"\n" +
    "          ng-show=\"isPipelineRunning && !snapshotMode && activeConfigStatus.status != 'STARTING'\">\n" +
    "        <span class=\"glyphicon glyphicon-stop fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <!--<a class=\"btn btn-link icon-button\"\n" +
    "         tooltip-placement=\"bottom\" tooltip=\"{{'global.form.share' | translate}}\"\n" +
    "         tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         ng-if=\"(common.authenticationType !== 'none' || common.isDPMEnabled) && !common.isSlaveNode\"\n" +
    "         ng-click=\"sharePipelineConfig(activeConfigInfo, $event)\"\n" +
    "         ng-show=\"!previewMode && !snapshotMode && activeConfigStatus.status != 'STARTING'\">\n" +
    "        <span class=\"fa fa-share-alt fa-14x\"></span>\n" +
    "      </a>-->\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "         tooltip-placement=\"bottom\" tooltip=\"{{'home.header.stageLibrary' | translate}}\"\n" +
    "         tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         ng-if=\"activeConfigStatus.executionMode != pipelineConstant.SLAVE && isAuthorized([userRoles.admin, userRoles.creator, userRoles.manager])\"\n" +
    "         ng-class=\"{'btn-selected': $storage.hideStageLibraryPanel}\"\n" +
    "         ng-hide=\"isPipelineRunning || previewMode || snapshotMode\"\n" +
    "         ng-click=\"$storage.hideStageLibraryPanel = !$storage.hideStageLibraryPanel\">\n" +
    "        <span class=\"fa fa-th fa-14x\"></span>\n" +
    "      </a>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right\" ng-if=\"pipelineConfig && (activeConfigStatus.status === 'STOPPING' || activeConfigStatus.status === 'STARTING_ERROR' || activeConfigStatus.status === 'RUNNING_ERROR' || activeConfigStatus.status === 'STOPPING_ERROR' || activeConnfigStatus.status === 'FINISHING')\">\n" +
    "      <a class=\"btn btn-link icon-button-danger\"\n" +
    "         ng-if=\"isAuthorized([userRoles.admin, userRoles.manager]) && canExecute && activeConfigStatus.executionMode != pipelineConstant.SLAVE && activeConfigStatus.executionMode != pipelineConstant.CLUSTER  && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_BATCH && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_YARN_STREAMING && activeConfigStatus.executionMode !== pipelineConstant.CLUSTER_MESOS_STREAMING\"\n" +
    "         ng-click=\"stopPipeline(true)\">{{'home.header.forceStop' | translate}}</a>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-left\" ng-switch=\"activeConfigStatus.status\">\n" +
    "      <div class=\"panel-title pull-left\" ng-if=\"activeConfigStatus.executionMode === pipelineConstant.CLUSTER || activeConfigStatus.executionMode === pipelineConstant.CLUSTER_BATCH || activeConfigStatus.executionMode === pipelineConstant.CLUSTER_YARN_STREAMING || activeConfigStatus.executionMode === pipelineConstant.CLUSTER_MESOS_STREAMING\">\n" +
    "        <h3 class=\"pull-left\" translate=\"home.header.clusterManagerNode\">Cluster Manager: </h3>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"panel-title pull-left\" ng-if=\"activeConfigStatus.executionMode === pipelineConstant.SLAVE\">\n" +
    "        <h3 class=\"pull-left\" translate=\"home.header.workerNode\">Worker:</h3>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"panel-title pull-left\"\n" +
    "           ng-switch-when=\"STARTING\">\n" +
    "        <h3 class=\"pull-left\" translate=\"home.header.startingPipeline\">Starting Pipeline</h3>\n" +
    "        <span class=\"pipeline-status {{activeConfigStatus.status}}\">{{activeConfigStatus.status}}</span>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"panel-title pull-left\"\n" +
    "           ng-switch-when=\"RUNNING\">\n" +
    "        <!--<h3 class=\"pull-left all-pipelines-link\">\n" +
    "          <a ng-href=\"{{common.baseHref}}\">Pipelines</a>\n" +
    "        </h3>\n" +
    "        <h3 class=\"pull-left all-pipelines-link\"> / </h3>-->\n" +
    "        <h3 class=\"pull-left\">{{pipelineConfig.info.title | limitTo: 40}}: </h3>\n" +
    "        <h3 class=\"pull-left\" translate=\"home.header.uptime\">Uptime</h3>\n" +
    "\n" +
    "        <h3 class=\"pull-left\"\n" +
    "            ng-if=\"!common.pipelineStatusMap[pipelineConfig.info.pipelineId].attributes || !common.pipelineStatusMap[pipelineConfig.info.pipelineId].attributes['cluster.application.startTime']\"\n" +
    "            am-time-ago=\"common.pipelineStatusMap[pipelineConfig.info.pipelineId].timeStamp - common.serverTimeDifference\"></h3>\n" +
    "\n" +
    "        <h3 class=\"pull-left\"\n" +
    "            ng-if=\"common.pipelineStatusMap[pipelineConfig.info.pipelineId].attributes && common.pipelineStatusMap[pipelineConfig.info.pipelineId].attributes['cluster.application.startTime']\"\n" +
    "            am-time-ago=\"common.pipelineStatusMap[pipelineConfig.info.pipelineId].attributes['cluster.application.startTime'] - common.serverTimeDifference\"></h3>\n" +
    "\n" +
    "        <span class=\"pipeline-status {{activeConfigStatus.status}}\">{{activeConfigStatus.status}}</span>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"panel-title pull-left\"\n" +
    "           ng-switch-when=\"STOPPING\">\n" +
    "        <h3 class=\"pull-left\" translate=\"home.header.stoppingPipeline\">Stopping Pipeline</h3>\n" +
    "        <span class=\"pipeline-status {{activeConfigStatus.status}}\">{{activeConfigStatus.status}}</span>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"panel-title pull-left\"\n" +
    "           ng-switch-when=\"FINISHING\">\n" +
    "        <h3 class=\"pull-left\" translate=\"home.header.finishingPipeline\">Finishing Pipeline</h3>\n" +
    "        <span class=\"pipeline-status {{activeConfigStatus.status}}\">{{activeConfigStatus.status}}</span>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"panel-title pull-left\"\n" +
    "           ng-switch-when=\"CONNECTING\">\n" +
    "        <h3 class=\"pull-left\" translate=\"home.header.connectingPipeline\">Connecting ...</h3>\n" +
    "        <span class=\"pipeline-status {{activeConfigStatus.status}}\">{{activeConfigStatus.status}}</span>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"panel-title pull-left\"\n" +
    "           ng-switch-when=\"DISCONNECTING\">\n" +
    "        <h3 class=\"pull-left\" translate=\"home.header.disconnectingPipeline\">Disconnecting ...</h3>\n" +
    "        <span class=\"pipeline-status {{activeConfigStatus.status}}\">{{activeConfigStatus.status}}</span>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"panel-title pull-left\"\n" +
    "           ng-switch-when=\"CONNECT_ERROR\">\n" +
    "        <h3 class=\"pull-left\" translate=\"home.header.connectErrorPipeline\">Failed to Connect ...</h3>\n" +
    "        <span class=\"pipeline-status {{activeConfigStatus.status}}\">{{activeConfigStatus.status}}</span>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"panel-title pull-left\"\n" +
    "           ng-switch-when=\"RETRY\">\n" +
    "        <h3 class=\"pull-left\" translate=\"home.header.retryPipeline\">Retrying to start Pipeline in </h3>\n" +
    "        <h3 class=\"pull-left\">{{retryCountDown | number:0}} seconds ...</h3>\n" +
    "        <span class=\"pipeline-status {{activeConfigStatus.status}}\">{{activeConfigStatus.status}}</span>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"panel-title pull-left\"\n" +
    "           ng-switch-default>\n" +
    "        <!--<h3 class=\"pull-left all-pipelines-link\">\n" +
    "          <a ng-href=\"{{common.baseHref}}\">Pipelines</a>\n" +
    "        </h3>\n" +
    "        <h3 class=\"pull-left all-pipelines-link\"> / </h3>-->\n" +
    "        <!--<h3 class=\"pull-left\" contenteditable=\"true\"\n" +
    "            title=\"Click to edit\"\n" +
    "            ng-model=\"pipelineConfig.title\">{{pipelineConfig.title | limitTo: 40}}</h3> **ORIGINAL NEXT H3 ELEMENT**-->\n" +
    "        <h3 class=\"pull-left\" ng-model=\"pipelineConfig.title\">{{pipelineConfig.title | limitTo: 40}}</h3>\n" +
    "        <h3 class=\"pull-left version-text\"\n" +
    "            ng-if=\"pipelineConfig.metadata && pipelineConfig.metadata['dpm.pipeline.version']\"> (v{{pipelineConfig.metadata['dpm.pipeline.version']}})</h3>\n" +
    "        <h3 class=\"pull-left\" ng-if=\"isDPMPipelineDirty\" title=\"{{'global.messages.info.isDPMPipelineDirty' | translate}}\"><span class=\"glyphicon glyphicon-asterisk dirty-icon\"></span></h3>\n" +
    "\n" +
    "        <span class=\"edge-pipeline\"\n" +
    "              ng-if=\"isEdgePipeline(activeConfigInfo)\">SDC Edge</span>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right\" ng-cloak\n" +
    "         ng-if=\"!isPipelineReadOnly && activeConfigStatus.executionMode != pipelineConstant.SLAVE\"\n" +
    "         ng-hide=\"previewMode || snapshotMode || activeConfigStatus.status === 'STOPPING'\">\n" +
    "      <div class=\"pipelineAgent-status pull-right\" ng-hide=\"common.saveOperationInProgress > 0\">\n" +
    "        <span class=\"glyphicon glyphicon-ok-circle\"></span>\n" +
    "        <span translate=\"global.messages.info.saveOperationInProgress\"></span>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"pipelineAgent-status pull-right\" ng-show=\"common.saveOperationInProgress > 0\"\n" +
    "           translate=\"global.messages.info.savingConfiguration\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"panel-body\"></div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/header/start/start.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/header/start/start.tpl.html",
    "<div class=\"start-pipeline-modal-form\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" ng-if=\"!forceStop\" translate=\"home.header.startWithParametersTitle\">Start Pipeline With Parameters</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div ng-if=\"constantsConfig.value.length\">\n" +
    "      <div class=\"row map-list-type\"\n" +
    "           ng-if=\"!bulkEdit\"\n" +
    "           ng-repeat=\"constant in constantsConfig.value track by $index\">\n" +
    "\n" +
    "        <div class=\"col-xs-4\">\n" +
    "          <input type=\"text\" class=\"form-control input-sm\"\n" +
    "                 name=\"mapObject{{$index}}\"\n" +
    "                 ng-disabled=\"true\"\n" +
    "                 value=\"{{constant.key}}\"/>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"col-xs-1\">:</div>\n" +
    "\n" +
    "        <div class=\"col-xs-5\">\n" +
    "          <input type=\"text\" class=\"form-control input-sm\"\n" +
    "                 name=\"mapObject{{$index}}\"\n" +
    "                 ng-required=\"true\"\n" +
    "                 ng-model=\"parameters.runtimeParameters[constant.key]\"/>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "      <div ui-codemirror\n" +
    "           ng-if=\"bulkEdit\"\n" +
    "           class=\"codemirror-editor\"\n" +
    "           ng-model=\"parameters.runtimeParameters\"\n" +
    "           ui-refresh=\"refreshCodemirror\"\n" +
    "           ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "              lineNumbers: true\n" +
    "            })\"\n" +
    "           data-type=\"'LIST'\">\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"pull-right field-selector-btn\">\n" +
    "        <button type=\"button\" class=\"btn btn-link\"\n" +
    "                ng-click=\"bulkEdit = !bulkEdit\">\n" +
    "          {{bulkEdit ? ('home.detailPane.configurationTab.switchToSimpleMode' | translate) : ('home.detailPane.configurationTab.switchToBulkMode' | translate)}}\n" +
    "        </button>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"alert alert-info alert-dismissible\" role=\"alert\"\n" +
    "         ng-show=\"constantsConfig.value.length === 0\"\n" +
    "         translate=\"global.messages.info.noParametersDefinedMsg\">\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"button\"\n" +
    "            class=\"btn btn-default\"\n" +
    "            ng-click=\"cancel()\"\n" +
    "            translate=\"global.form.cancel\"\n" +
    "            ng-disabled=\"starting\">Cancel</button>\n" +
    "    <button type=\"button\"\n" +
    "            ng-if=\"constantsConfig.value.length\"\n" +
    "            class=\"btn btn-primary\"\n" +
    "            ng-click=\"start()\"\n" +
    "            translate=\"home.header.start\"\n" +
    "            ng-disabled=\"starting\">Start</button>\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/header/stop/stopConfirmation.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/header/stop/stopConfirmation.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <button type=\"button\" class=\"close\" ng-click=\"no()\">\n" +
    "    <span aria-hidden=\"true\">&times;</span>\n" +
    "    <span class=\"sr-only\">Close</span>\n" +
    "  </button>\n" +
    "  <h3 class=\"modal-title\" ng-if=\"!forceStop\" translate=\"home.header.stopConfirmationTitle\">Stop Pipeline Confirmation</h3>\n" +
    "  <h3 class=\"modal-title\" ng-if=\"forceStop\" translate=\"home.header.forceStopConfirmationTitle\">Force Stop Pipeline Confirmation</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body multiple-pipelines-modal-body\">\n" +
    "  <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "  <div ng-if=\"!isList && (!common.errors || common.errors.length == 0)\">\n" +
    "    <p ng-if=\"!stopping && !forceStop\"\n" +
    "       translate=\"home.header.stopConfirmationMessage\" translate-values=\"{name: pipelineInfo.title}\"></p>\n" +
    "\n" +
    "    <p ng-if=\"!stopping && forceStop\"\n" +
    "       translate=\"home.header.forceStopConfirmationMessage\" translate-values=\"{name: pipelineInfo.title}\"></p>\n" +
    "\n" +
    "    <p translate=\"home.header.stoppingPipeline\" ng-if=\"stopping\"></p>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-if=\"isList && (!common.errors || common.errors.length == 0)\">\n" +
    "    <p ng-if=\"!stopping && !forceStop\" translate=\"home.header.stopPipelinesConfirmationMessage\"></p>\n" +
    "    <p ng-if=\"!stopping && forceStop\" translate=\"home.header.forceStopPipelinesConfirmationMessage\"></p>\n" +
    "    <p ng-if=\"!stopping\" ng-repeat=\"pipeline in pipelineInfo\">{{pipeline.title}}</p>\n" +
    "    <p translate=\"home.header.stoppingPipelines\" ng-if=\"stopping\"></p>\n" +
    "  </div>\n" +
    "\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"button\"\n" +
    "          class=\"btn btn-default\"\n" +
    "          ng-if=\"!common.errors || common.errors.length == 0\"\n" +
    "          ng-click=\"no()\"\n" +
    "          translate=\"global.form.no\"\n" +
    "          ng-disabled=\"stopping\">No</button>\n" +
    "  <button type=\"button\"\n" +
    "          class=\"btn btn-primary\"\n" +
    "          ng-if=\"!common.errors || common.errors.length == 0\"\n" +
    "          ng-click=\"yes()\"\n" +
    "          translate=\"global.form.yes\"\n" +
    "          ng-disabled=\"stopping\">Yes</button>\n" +
    "  <button type=\"button\"\n" +
    "          class=\"btn btn-default\"\n" +
    "          ng-if=\"common.errors && common.errors.length > 0\"\n" +
    "          ng-click=\"no()\"\n" +
    "          translate=\"global.form.close\"\n" +
    "          ng-disabled=\"stopping\">Close</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/home.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/home.tpl.html",
    "<ng-include src=\"'app/home/home_empty.tpl.html'\"></ng-include>\n" +
    "\n" +
    "<div show-loading=\"fetching\" ng-if=\"totalPipelinesCount == 0\"></div>\n" +
    "\n" +
    "<div class=\"panel panel-default page-panel home-page\" ng-show=\"totalPipelinesCount > 0\">\n" +
    "  <ng-include src=\"'app/home/home_header.tpl.html'\"></ng-include>\n" +
    "\n" +
    "  <div class=\"home-bg-splitter\" ng-cloak>\n" +
    "    <bg-splitter orientation=\"horizontal\" class=\"library-splitter\"\n" +
    "                 ng-class=\"{'hide-library-panel': hideLibraryPanel}\">\n" +
    "\n" +
    "      <bg-pane min-size=\"0\">\n" +
    "        <ng-include src=\"'app/home/library/library.tpl.html'\"></ng-include>\n" +
    "      </bg-pane>\n" +
    "\n" +
    "      <bg-pane min-size=\"0\">\n" +
    "        <div class=\"home-page-body\">\n" +
    "          <ng-include src=\"'app/home/alerts/error/errorModal.tpl.html'\"></ng-include>\n" +
    "          <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "          <ng-include src=\"'app/home/alerts/success/successAlert.tpl.html'\"></ng-include>\n" +
    "          <ng-include src=\"'app/home/alerts/info/infoAlert.tpl.html'\"></ng-include>\n" +
    "          <ng-include src=\"'app/home/home_grid_view.tpl.html'\"></ng-include>\n" +
    "          <ng-include src=\"'app/home/home_list_view.tpl.html'\"></ng-include>\n" +
    "        </div>\n" +
    "      </bg-pane>\n" +
    "\n" +
    "    </bg-splitter>\n" +
    "  </div>\n" +
    "\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/home_empty.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/home_empty.tpl.html",
    "<div class=\"pipeline-empty-container\" ng-class=\"{'show': loaded}\" ng-show=\"totalPipelinesCount == 0\">\n" +
    "  <div class=\"error-alert-div\">\n" +
    "    <ng-include ng-if=\"common.errors\" src=\"'app/home/alerts/error/errorModal.tpl.html'\"></ng-include>\n" +
    "    <ng-include ng-if=\"common.errors\" src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "    <ng-include src=\"'app/home/alerts/info/infoAlert.tpl.html'\"></ng-include>\n" +
    "  </div>\n" +
    "  <div class=\"create-pipeline-btn-group\" ng-if=\"!common.isSlaveNode\">\n" +
    "    <div class=\"create-pipeline-btn-group-cell\">\n" +
    "      <h2 translate=\"home.getStarted\">Get Started</h2>\n" +
    "\n" +
    "      <div>\n" +
    "        <button class=\"btn btn-primary pull-left create-pipeline-btn\"\n" +
    "                ng-disabled=\"!isAuthorized([userRoles.admin, userRoles.creator]) || common.isSlaveNode\"\n" +
    "                ng-click=\"addPipelineConfig()\">\n" +
    "          <span class=\"fa fa-plus\"></span>\n" +
    "          <span translate=\"home.createPipeline\">Create Pipeline</span>\n" +
    "        </button>\n" +
    "\n" +
    "        <button class=\"btn btn-primary pull-left import-pipeline-btn\"\n" +
    "                ng-disabled=\"!isAuthorized([userRoles.admin, userRoles.creator]) || common.isSlaveNode\"\n" +
    "                ng-click=\"importPipelineConfig()\">\n" +
    "          <span class=\"fa fa-arrow-circle-down\"></span>\n" +
    "          <span translate=\"home.importPipeline\">Import Pipeline</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "\n" +
    "      <div>\n" +
    "        <button class=\"btn btn-primary pull-left download-pipeline-btn\"\n" +
    "                ng-disabled=\"!isAuthorized([userRoles.admin, userRoles.creator]) || common.isSlaveNode\"\n" +
    "                ng-click=\"importPipelinesFromArchive()\">\n" +
    "          <span class=\"fa fa-arrow-circle-down\"></span>\n" +
    "          <span translate=\"home.importPipelinesFromArchive\">Import Pipelines From Archive</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "\n" +
    "      <div ng-if=\"common.isDPMEnabled\">\n" +
    "        <button class=\"btn btn-primary download-pipeline-btn\"\n" +
    "                ng-disabled=\"!isAuthorized([userRoles.admin, userRoles.creator]) || common.isSlaveNode\"\n" +
    "                ng-click=\"downloadRemotePipelineConfig()\">\n" +
    "          <span class=\"fa fa-cloud-download\"></span>\n" +
    "          <span translate=\"home.downloadRemotePipeline\">Download Remote Pipeline</span>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "\n" +
    "      <div>\n" +
    "        <a class=\"btn btn-primary pull-left get-started-with-tutorial\"\n" +
    "           href=\"https://github.com/streamsets/tutorials\" target=\"_blank\">\n" +
    "          <span class=\"fa fa-book\"></span>\n" +
    "          <span translate=\"home.getStartedWithTutorials\">Try a Tutorial</span>\n" +
    "        </a>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/home_grid_view.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/home_grid_view.tpl.html",
    "<section class=\"tile-list\" ng-if=\"header.pipelineGridView\">\n" +
    "\n" +
    "  <section class=\"pipeline-container new-pipeline\" ng-if=\"filteredPipelines.length === 0 && !fetching\">\n" +
    "    <div class=\"centering-container\">\n" +
    "      <span translate=\"home.noPipelines\"></span>\n" +
    "    </div>\n" +
    "  </section>\n" +
    "\n" +
    "  <section class=\"pipeline-container single-pipeline\"\n" +
    "           ng-repeat=\"pipeline in filteredPipelines\">\n" +
    "    <header>\n" +
    "      <a target=\"_blank\" ng-href=\"{{'collector/pipeline/' + pipeline.pipelineId}}\"><h2>{{pipeline.title | limitTo: 45}}</h2></a>\n" +
    "    </header>\n" +
    "\n" +
    "    <section class=\"project-additional-info\"\n" +
    "             ng-hide=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING'\">\n" +
    "      <span class=\"time-update\" translate=\"home.updated\">Updated</span>\n" +
    "      <span class=\"time-update\" am-time-ago=\"pipeline.lastModified\"></span>\n" +
    "      <span class=\"time-update\" translate=\"home.ago\">ago</span>\n" +
    "    </section>\n" +
    "\n" +
    "    <section class=\"project-additional-info\"\n" +
    "             ng-show=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING'\">\n" +
    "      <span class=\"time-update\" translate=\"home.header.uptime\">Uptime</span>\n" +
    "      <span class=\"time-update\" am-time-ago=\"common.pipelineStatusMap[pipeline.pipelineId].timeStamp\"></span>\n" +
    "    </section>\n" +
    "\n" +
    "    <footer >\n" +
    "      <section class=\"running-status\"\n" +
    "               ng-if=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING'\"\n" +
    "               tooltip-placement=\"bottom\"\n" +
    "               tooltip=\"{{'home.runningStatus' | translate}}\">\n" +
    "        <i class=\"fa fa-spinner fa-spin fa-14x\"></i>\n" +
    "      </section>\n" +
    "\n" +
    "      <section class=\"invalid-status\"\n" +
    "               ng-if=\"!pipeline.valid\"\n" +
    "               tooltip-placement=\"bottom\"\n" +
    "               tooltip=\"{{'home.invalidStatus' | translate}}\">\n" +
    "        <i class=\"fa fa-exclamation-triangle fa-12x\"></i>\n" +
    "      </section>\n" +
    "\n" +
    "      <section class=\"invalid-status\"\n" +
    "               ng-if=\"common.pipelineStatusMap[pipeline.pipelineId].status == 'START_ERROR' || common.pipelineStatusMap[pipeline.pipelineId].status == 'STARTING_ERROR' || common.pipelineStatusMap[pipeline.pipelineId].status == 'RUNNING_ERROR' || common.pipelineStatusMap[pipeline.pipelineId].status == 'RUN_ERROR' || common.pipelineStatusMap[pipeline.pipelineId].status == 'CONNECT_ERROR' || common.pipelineStatusMap[pipeline.pipelineId].status == 'STOP_ERROR' || common.pipelineStatusMap[pipeline.pipelineId].status == 'STOPPING_ERROR'\"\n" +
    "               tooltip-placement=\"bottom\"\n" +
    "               tooltip=\"{{'Pipeline Status :' + common.pipelineStatusMap[pipeline.pipelineId].status + ': ' + common.pipelineStatusMap[pipeline.pipelineId].message}}\">\n" +
    "        <i class=\"fa fa-exclamation-triangle fa-12x\"></i>\n" +
    "      </section>\n" +
    "\n" +
    "      <section class=\"triggered-alert-status\"\n" +
    "               ng-if=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING' &&\n" +
    "                   common.alertsMap[pipeline.pipelineId] && common.alertsMap[pipeline.pipelineId].length > 0\"\n" +
    "               tooltip-placement=\"bottom\"\n" +
    "               tooltip-html=\"getPipelineAlerts(common.alertsMap[pipeline.pipelineId])\">\n" +
    "        <i class=\"fa fa-bell fa-12x\"></i>\n" +
    "        <span ng-bind=\"common.alertsMap[pipeline.pipelineId].length\"></span>\n" +
    "      </section>\n" +
    "\n" +
    "      <section class=\"pipeline-menu\" ng-if=\"!common.isSlaveNode && isAuthorized([userRoles.admin, userRoles.creator, userRoles.manager])\">\n" +
    "        <span class=\"split-bar\"></span>\n" +
    "        <div class=\"action-menu\" dropdown>\n" +
    "              <span class=\"dropdown-toggle\" dropdown-toggle\n" +
    "                    ng-click=\"$event.stopPropagation();\">\n" +
    "                <i class=\"fa fa-ellipsis-h\"></i>\n" +
    "              </span>\n" +
    "          <ul class=\"dropdown-menu\" role=\"menu\">\n" +
    "            <!--<li role=\"presentation\"\n" +
    "                ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\"\n" +
    "                ng-hide=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING'\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"deletePipelineConfig(pipeline, $event)\">\n" +
    "                <i class=\"fa fa-trash-o\"></i>\n" +
    "                <span>{{'global.form.delete' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"duplicatePipelineConfig(pipeline, $event)\">\n" +
    "                <i class=\"fa fa-files-o\"></i>\n" +
    "                <span>{{'global.form.duplicate' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"sharePipelineConfig(pipeline, $event)\">\n" +
    "                <i class=\"fa fa-share-alt\"></i>\n" +
    "                <span>{{'global.form.share' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"importPipelineConfig(pipeline, $event)\">\n" +
    "                <i class=\"glyphicon glyphicon-import\"></i>\n" +
    "                <span>{{'global.form.import' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>-->\n" +
    "\n" +
    "            <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin, userRoles.creator, userRoles.manager])\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"exportPipelineConfig(pipeline, false, $event)\">\n" +
    "                <i class=\"glyphicon glyphicon-export\"></i>\n" +
    "                <span>{{'global.form.export' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin, userRoles.creator, userRoles.manager])\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"exportPipelineConfig(pipeline, true, $event)\">\n" +
    "                <i class=\"glyphicon glyphicon-export\"></i>\n" +
    "                <span>{{'global.form.exportWithLibraryDefinitions' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li ng-repeat-end role=\"presentation\" class=\"divider\"\n" +
    "                ng-if=\"isAuthorized([userRoles.admin, userRoles.manager])\"\n" +
    "                ng-show=\"pipeline.valid\"></li>\n" +
    "\n" +
    "            <li role=\"presentation\"\n" +
    "                ng-if=\"isAuthorized([userRoles.admin, userRoles.manager]) && !common.isSlaveNode\"\n" +
    "                ng-hide=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING' || common.pipelineStatusMap[pipeline.pipelineId].status === 'STARTING' || !pipeline.valid\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"startPipeline(pipeline, $event)\">\n" +
    "                <i class=\"glyphicon glyphicon-play\"></i>\n" +
    "                <span>{{'home.header.start' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li role=\"presentation\"\n" +
    "                ng-if=\"isAuthorized([userRoles.admin, userRoles.manager]) && !common.isSlaveNode\"\n" +
    "                ng-show=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING'\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"stopPipeline(pipeline, $event)\">\n" +
    "                <i class=\"glyphicon glyphicon-stop\"></i>\n" +
    "                <span>{{'home.header.stop' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "          </ul>\n" +
    "        </div>\n" +
    "\n" +
    "      </section>\n" +
    "\n" +
    "    </footer>\n" +
    "  </section>\n" +
    "\n" +
    "  <section class=\"pipeline-container new-pipeline\" ng-if=\"showLoadMore || fetching\">\n" +
    "    <div class=\"centering-container\">\n" +
    "      <a class=\"btn btn-link\"\n" +
    "         ng-if=\"!fetching\"\n" +
    "         ng-click=\"onShowMoreClick($event);\">{{'global.form.showMore' | translate}}</a>\n" +
    "\n" +
    "      <div show-loading=\"fetching\"></div>\n" +
    "    </div>\n" +
    "  </section>\n" +
    "\n" +
    "</section>\n" +
    "");
}]);

angular.module("app/home/home_header.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/home_header.tpl.html",
    "<div class=\"panel panel-default page-panel home-page\">\n" +
    "\n" +
    "  <div class=\"panel-heading clearfix\">\n" +
    "\n" +
    "    <div class=\"panel-title \">\n" +
    "      <div class=\"pull-left\">\n" +
    "        <button type=\"button\" class=\"navbar-toggle collapsed\"\n" +
    "                ng-if=\"activeConfigStatus.executionMode != pipelineConstant.SLAVE\"\n" +
    "                ng-class=\"{'selected' : !hideLibraryPanel}\"\n" +
    "                ng-click=\"toggleLibraryPanel()\"\n" +
    "                title=\"{{'home.header.toggleLibraryPane' | translate}}\">\n" +
    "          <i class=\"icon-bar\"></i>\n" +
    "          <i class=\"icon-bar\"></i>\n" +
    "          <i class=\"icon-bar\"></i>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <h3 class=\"pull-left\" translate=\"home.title\">Pipelines</h3>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right btn-group list-grid-toggle-toolbar\">\n" +
    "      <label class=\"btn btn-default btn-sm\"\n" +
    "             tooltip-placement=\"bottom\"\n" +
    "             tooltip=\"{{'global.form.listView' | translate}}\"\n" +
    "             tooltip-popup-delay=\"500\"\n" +
    "             ng-model=\"header.pipelineGridView\"\n" +
    "             btn-radio=\"false\"><span class=\"glyphicon glyphicon-th-list\"></span></label>\n" +
    "      <label class=\"btn btn-default btn-sm\"\n" +
    "             tooltip-placement=\"bottom\"\n" +
    "             tooltip=\"{{'global.form.gridView' | translate}}\"\n" +
    "             tooltip-popup-delay=\"500\"\n" +
    "             ng-model=\"header.pipelineGridView\"\n" +
    "             btn-radio=\"true\"><span class=\"glyphicon glyphicon-th\"></span></label>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right\">\n" +
    "\n" +
    "      <div class=\"btn-group\" dropdown\n" +
    "           ng-if=\"isAuthorized([userRoles.admin, userRoles.creator, userRoles.manager])\"\n" +
    "           ng-hide=\"previewMode || snapshotMode\">\n" +
    "       <span class=\"btn btn-link dropdown-toggle icon-button\" dropdown-toggle aria-expanded=\"true\"\n" +
    "             tooltip-placement=\"bottom\" tooltip=\"{{'home.header.more' | translate}}\"\n" +
    "             tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "             tooltip-popup-delay=\"500\"\n" +
    "             ng-click=\"$event.stopPropagation();\">\n" +
    "          <i class=\"fa fa-ellipsis-h fa-14x\"></i>\n" +
    "        </span>\n" +
    "        <ul class=\"dropdown-menu pull-right\" role=\"menu\">\n" +
    "\n" +
    "          <li role=\"presentation\"\n" +
    "              ng-class=\"{'disabled': selectedPipelineList.length === 0}\"\n" +
    "              ng-if=\"isAuthorized([userRoles.admin, userRoles.manager])\">\n" +
    "            <a href=\"#\" dropdown-toggle\n" +
    "               ng-click=\"selectedPipelineList.length === 0 || resetOffsetForSelectedPipelines()\">\n" +
    "              <i class=\"fa fa-power-off\"></i>\n" +
    "              <span>{{'home.resetOffset.title' | translate}}</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\"\n" +
    "              ng-class=\"{'disabled': selectedPipelineList.length === 0}\"\n" +
    "              ng-if=\"isAuthorized([userRoles.admin, userRoles.creator])\">\n" +
    "            <a href=\"#\" dropdown-toggle\n" +
    "               ng-click=\"selectedPipelineList.length === 0 || addLabelsToSelectedPipelines()\">\n" +
    "              <i class=\"fa fa-tags\"></i>\n" +
    "              <span>{{'home.header.addLabels' | translate}}</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\"\n" +
    "              ng-class=\"{'disabled': selectedPipelineList.length === 0}\"\n" +
    "              ng-if=\"isAuthorized([userRoles.admin, userRoles.creator, userRoles.manager])\">\n" +
    "            <a href=\"#\" dropdown-toggle\n" +
    "               ng-click=\"selectedPipelineList.length === 0 || exportSelectedPipelines(false)\">\n" +
    "              <i class=\"glyphicon glyphicon-export\"></i>\n" +
    "              <span>{{'global.form.export' | translate}}</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\"\n" +
    "              ng-class=\"{'disabled': selectedPipelineList.length === 0}\"\n" +
    "              ng-if=\"isAuthorized([userRoles.admin, userRoles.creator, userRoles.manager])\">\n" +
    "            <a href=\"#\" dropdown-toggle\n" +
    "               ng-click=\"selectedPipelineList.length === 0 || exportSelectedPipelines(true)\">\n" +
    "              <i class=\"glyphicon glyphicon-export\"></i>\n" +
    "              <span>{{'global.form.exportWithLibraryDefinitions' | translate}}</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a href=\"#\" dropdown-toggle\n" +
    "               ng-click=\"onToggleShowNameColumn()\">\n" +
    "              <i class=\"glyphicon glyphicon-record\"></i>\n" +
    "              <span>{{(header.showNameColumn ? 'global.form.hideNameColumn' : 'global.form.showNameColumn') | translate}}</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{'global.form.refresh' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         ng-click=\"refreshPipelines()\">\n" +
    "        <span class=\"fa fa-refresh fa-14x\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <!--<a class=\"btn btn-link icon-button\"\n" +
    "         ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && common.isDPMEnabled\"\n" +
    "         ng-disabled=\"selectedPipelineList.length === 0\"\n" +
    "         ng-click=\"selectedPipelineList.length === 0 || publishSelectedPipelines()\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{('global.form.publish' | translate)  + ' (' + selectedPipelineList.length + ')'}}\"\n" +
    "         tooltip-popup-delay=\"500\">\n" +
    "        <i class=\"fa fa-cloud-upload fa-14x\"></i>\n" +
    "      </a>\n" +
    "\n" +
    "      <a class=\"btn btn-link icon-button\"\n" +
    "         ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\"\n" +
    "         ng-disabled=\"selectedPipelineList.length === 0\"\n" +
    "         ng-click=\"selectedPipelineList.length === 0 || deleteSelectedPipeline()\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{('global.form.delete' | translate) + ' (' + selectedPipelineList.length + ')'}}\"\n" +
    "         tooltip-popup-delay=\"500\">\n" +
    "        <i class=\"fa fa-trash-o fa-14x\"></i>\n" +
    "      </a>\n" +
    "\n" +
    "      <a ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\"\n" +
    "         ng-disabled=\"selectedPipelineList.length !== 1 || common.isSlaveNode\"\n" +
    "         ng-click=\"selectedPipelineList.length !== 1 || duplicatePipelines()\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{'global.form.duplicate' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         class=\"btn btn-link icon-button\">\n" +
    "        <i class=\"fa fa-files-o fa-14x\"></i>\n" +
    "      </a>\n" +
    "\n" +
    "      <a ng-disabled=\"selectedPipelineList.length !== 1 || common.isSlaveNode\"\n" +
    "         ng-if=\"(common.authenticationType !== 'none' || common.isDPMEnabled) && !common.isSlaveNode\"\n" +
    "         ng-click=\"selectedPipelineList.length !== 1 || shareSelectedPipelineConfig()\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{'global.form.share' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         class=\"btn btn-link icon-button\">\n" +
    "        <i class=\"fa fa-share-alt fa-14x\"></i>\n" +
    "      </a>-->\n" +
    "\n" +
    "      <a ng-if=\"isAuthorized([userRoles.admin, userRoles.manager])\"\n" +
    "         ng-disabled=\"selectedPipelineList.length === 0 || common.isSlaveNode\"\n" +
    "         ng-click=\"selectedPipelineList.length === 0 || startSelectedPipelines()\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{('home.header.start' | translate)  + ' (' + selectedPipelineList.length + ')'}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         class=\"btn btn-link icon-button\">\n" +
    "        <i class=\"glyphicon glyphicon-play fa-14x\"></i>\n" +
    "      </a>\n" +
    "\n" +
    "      <a ng-if=\"isAuthorized([userRoles.admin, userRoles.manager])\"\n" +
    "         ng-disabled=\"selectedPipelineList.length === 0 || common.isSlaveNode\"\n" +
    "         ng-click=\"selectedPipelineList.length === 0 || stopSelectedPipelines()\"\n" +
    "         tooltip-placement=\"bottom\"\n" +
    "         tooltip=\"{{('home.header.stop' | translate)  + ' (' + selectedPipelineList.length + ')'}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         class=\"btn btn-link icon-button\">\n" +
    "        <i class=\"glyphicon glyphicon-stop fa-14x\"></i>\n" +
    "      </a>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right\" ng-show=\"header.pipelineGridView\">\n" +
    "\n" +
    "      <span class=\"sort-by-label\">Sort by:</span>\n" +
    "      <div class=\"btn-group pull-right sort-by-dropdown\">\n" +
    "\n" +
    "        <button class=\"btn btn-link dropdown-toggle icon-button\" type=\"button\" data-toggle=\"dropdown\">\n" +
    "          <span>{{'home.sortColumn.' + header.sortColumn | translate}}</span>\n" +
    "          <i ng-show=\"!header.sortReverse\" class=\"fa fa-arrow-down\"></i>\n" +
    "          <i ng-show=\"header.sortReverse\" class=\"fa fa-arrow-up\"></i>\n" +
    "        </button>\n" +
    "\n" +
    "        <ul class=\"dropdown-menu\" role=\"menu\" aria-labelledby=\"dropdownMenu1\">\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"header.sortColumn = 'NAME'; header.sortReverse = false; updateFilteredPipelines(0)\">\n" +
    "              <span translate=\"home.sortColumn.NAME\">Name</span>\n" +
    "              <i class=\"fa fa-arrow-down\"></i>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"header.sortColumn = 'NAME'; header.sortReverse = true; updateFilteredPipelines(0)\">\n" +
    "              <span translate=\"home.sortColumn.NAME\">Name</span>\n" +
    "              <i class=\"fa fa-arrow-up\"></i>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"header.sortColumn = 'CREATED'; header.sortReverse = false; updateFilteredPipelines(0)\">\n" +
    "              <span translate=\"home.sortColumn.CREATED\">Date created</span>\n" +
    "              <i class=\"fa fa-arrow-down\"></i>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"header.sortColumn = 'CREATED'; header.sortReverse = true; updateFilteredPipelines(0)\">\n" +
    "              <span translate=\"home.sortColumn.CREATED\">Date created</span>\n" +
    "              <i class=\"fa fa-arrow-up\"></i>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"header.sortColumn = 'LAST_MODIFIED'; header.sortReverse = false; updateFilteredPipelines(0)\">\n" +
    "              <span translate=\"home.sortColumn.LAST_MODIFIED\">Date updated</span>\n" +
    "              <i class=\"fa fa-arrow-down\"></i>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"header.sortColumn = 'LAST_MODIFIED'; header.sortReverse = true; updateFilteredPipelines(0)\">\n" +
    "              <span translate=\"home.sortColumn.LAST_MODIFIED\">Date updated</span>\n" +
    "              <i class=\"fa fa-arrow-up\"></i>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"header.sortColumn = 'STATUS'; header.sortReverse = false; updateFilteredPipelines(0)\">\n" +
    "              <span translate=\"home.sortColumn.STATUS\">Status</span>\n" +
    "              <i class=\"fa fa-arrow-down\"></i>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"header.sortColumn = 'STATUS'; header.sortReverse = true; updateFilteredPipelines(0)\">\n" +
    "              <span translate=\"home.sortColumn.STATUS\">Status</span>\n" +
    "              <i class=\"fa fa-arrow-up\"></i>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right\">\n" +
    "      <div class=\"btn-group pipeline-search\" >\n" +
    "        <form ng-submit=\"updateFilteredPipelines(0)\">\n" +
    "          <input type=\"search\" class=\"form-control\" placeholder=\"Filter Pipelines\"\n" +
    "                 ng-model=\"header.searchInput\">\n" +
    "        </form>\n" +
    "      <span class=\"glyphicon glyphicon-remove-circle search-clear\"\n" +
    "            ng-click=\"header.searchInput = '';updateFilteredPipelines(0);\"></span>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right pipelines-count\">\n" +
    "      <span ng-if=\"filteredPipelines.length >= totalCount\">{{totalCount}} {{totalCount > 1 ? 'Pipelines' : 'Pipeline'}}</span>\n" +
    "      <span ng-if=\"filteredPipelines.length < totalCount\">{{filteredPipelines.length}} of {{totalCount}} {{totalCount > 1 ? 'Pipelines' : 'Pipeline'}}</span>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/home_list_view.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/home_list_view.tpl.html",
    "<section class=\"pipeline-list-view\" ng-if=\"!header.pipelineGridView\">\n" +
    "  <div class=\"list-content\">\n" +
    "    <ul class=\"list-group checked-list-box\">\n" +
    "\n" +
    "      <li class=\"list-group-item header-list-group-item\">\n" +
    "        <i ng-if=\"allSelected\"\n" +
    "           ng-click=\"unSelectAll()\"\n" +
    "           class=\"glyphicon glyphicon-check fa-12x header-check-box\"></i>\n" +
    "\n" +
    "        <i ng-if=\"!allSelected\"\n" +
    "           ng-click=\"selectAll()\"\n" +
    "           class=\"glyphicon glyphicon-unchecked fa-12x header-check-box\"></i>\n" +
    "\n" +
    "        <div class=\"pipeline-name header\"\n" +
    "             ng-class=\"{'show-name-column': header.showNameColumn}\">\n" +
    "          <a ng-click=\"onSortColumnHeaderClick('TITLE')\">\n" +
    "            <span>{{'global.form.title' | translate}}</span>\n" +
    "            <i ng-if=\"header.sortColumn !== 'TITLE'\" class=\"fa fa-sort\" aria-hidden=\"true\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'TITLE' && !header.sortReverse\" class=\"fa fa-arrow-down\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'TITLE' && header.sortReverse\" class=\"fa fa-arrow-up\"></i>\n" +
    "          </a>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"pipeline-name header\"\n" +
    "             ng-class=\"{'show-name-column': header.showNameColumn}\"\n" +
    "             ng-if=\"header.showNameColumn\">\n" +
    "          <a ng-click=\"onSortColumnHeaderClick('NAME')\">\n" +
    "            <span>{{'global.form.pipelineId' | translate}}</span>\n" +
    "            <i ng-if=\"header.sortColumn !== 'NAME'\" class=\"fa fa-sort\" aria-hidden=\"true\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'NAME' && !header.sortReverse\" class=\"fa fa-arrow-down\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'NAME' && header.sortReverse\" class=\"fa fa-arrow-up\"></i>\n" +
    "          </a>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"pipeline-update-time header\">\n" +
    "          <a ng-click=\"onSortColumnHeaderClick('LAST_MODIFIED')\">\n" +
    "            <span>{{'global.form.lastUpdated' | translate}}</span>\n" +
    "            <i ng-if=\"header.sortColumn !== 'LAST_MODIFIED'\" class=\"fa fa-sort\" aria-hidden=\"true\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'LAST_MODIFIED' && !header.sortReverse\" class=\"fa fa-arrow-down\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'LAST_MODIFIED' && header.sortReverse\" class=\"fa fa-arrow-up\"></i>\n" +
    "          </a>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"pipeline-creator header\">\n" +
    "          <a ng-click=\"onSortColumnHeaderClick('CREATOR')\">\n" +
    "            <span>{{'global.form.createdBy' | translate}}</span>\n" +
    "            <i ng-if=\"header.sortColumn !== 'CREATOR'\" class=\"fa fa-sort\" aria-hidden=\"true\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'CREATOR' && !header.sortReverse\" class=\"fa fa-arrow-down\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'CREATOR' && header.sortReverse\" class=\"fa fa-arrow-up\"></i>\n" +
    "          </a>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"pipeline-status-header header\">\n" +
    "          <a ng-click=\"onSortColumnHeaderClick('STATUS')\">\n" +
    "            <span>{{'global.form.status' | translate}}</span>\n" +
    "            <i ng-if=\"header.sortColumn !== 'STATUS'\" class=\"fa fa-sort\" aria-hidden=\"true\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'STATUS' && !header.sortReverse\" class=\"fa fa-arrow-down\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'STATUS' && header.sortReverse\" class=\"fa fa-arrow-up\"></i>\n" +
    "          </a>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"header pull-right\">\n" +
    "          <a ng-if=\"!showDetails\" ng-click=\"showPipelineDetails()\">\n" +
    "            <span>{{'home.header.showDetails' | translate}}</span>\n" +
    "          </a>\n" +
    "          <a ng-if=\"showDetails\" ng-click=\"hidePipelineDetails()\">\n" +
    "            <span>{{'home.header.hideDetails' | translate}}</span>\n" +
    "          </a>\n" +
    "        </div>\n" +
    "\n" +
    "      </li>\n" +
    "\n" +
    "      <li class=\"list-group-item\"\n" +
    "          ng-class=\"{'active-info': selectedPipelineMap[pipeline.pipelineId]}\"\n" +
    "          ng-repeat=\"pipeline in filteredPipelines\">\n" +
    "\n" +
    "        <i ng-if=\"selectedPipelineMap[pipeline.pipelineId]\"\n" +
    "           ng-click=\"unSelectPipeline(pipeline)\"\n" +
    "           class=\"glyphicon glyphicon-check fa-12x\"></i>\n" +
    "\n" +
    "        <i ng-if=\"!selectedPipelineMap[pipeline.pipelineId]\"\n" +
    "           ng-click=\"selectPipeline(pipeline, $event)\"\n" +
    "           class=\"glyphicon glyphicon-unchecked fa-12x\"></i>\n" +
    "\n" +
    "        <div class=\"pipeline-name\"\n" +
    "             ng-class=\"{'show-name-column': header.showNameColumn}\">\n" +
    "\n" +
    "          <span class=\"system-pipeline-label\"\n" +
    "                ng-if=\"isSystemPipeline(pipeline)\">Control Hub system</span>\n" +
    "\n" +
    "          <span class=\"system-pipeline-label\"\n" +
    "                ng-if=\"isDpmControlledPipeline(pipeline)\">SCH</span>\n" +
    "\n" +
    "          <a target=\"_blank\" ng-href=\"{{'collector/pipeline/' + pipeline.pipelineId}}\" ><span>{{pipeline.title || '--'}}</span></a>\n" +
    "\n" +
    "          <span class=\"edge-pipeline\"\n" +
    "                ng-if=\"isEdgePipeline(pipeline)\">SDC Edge</span>\n" +
    "\n" +
    "          <span class=\"version-text\"\n" +
    "                ng-if=\"pipeline.metadata && pipeline.metadata['dpm.pipeline.version']\"> (v{{pipeline.metadata['dpm.pipeline.version']}}) </span>\n" +
    "\n" +
    "          <span class=\"glyphicon glyphicon-asterisk dirty-icon\" title=\"{{'global.messages.info.isDPMPipelineDirty' | translate}}\"\n" +
    "                ng-if=\"common.isDPMEnabled && pipeline.metadata && pipeline.metadata.lastConfigId && (pipeline.metadata.lastConfigId !== pipeline.uuid) && !isSystemPipeline(pipeline) && !isDpmControlledPipeline(pipeline)\"></span>\n" +
    "\n" +
    "          <span class=\"pipeline-label\"\n" +
    "                ng-if=\"pipeline.metadata && pipeline.metadata.labels && pipeline.metadata.labels.length > 0\"\n" +
    "                ng-repeat=\"label in pipeline.metadata.labels\">{{label}}</span>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"pipeline-name\"\n" +
    "             ng-class=\"{'show-name-column': header.showNameColumn}\"\n" +
    "             ng-if=\"header.showNameColumn\">\n" +
    "          <span>{{pipeline.pipelineId}}</span>\n" +
    "        </div>\n" +
    "\n" +
    "        <span class=\"pipeline-update-time\">\n" +
    "          <span class=\"time-update\" am-time-ago=\"pipeline.lastModified\"></span>\n" +
    "          <span class=\"time-update\" translate=\"home.ago\">ago</span>\n" +
    "        </span>\n" +
    "\n" +
    "        <span class=\"pipeline-creator\">{{pipeline.creator}}</span>\n" +
    "\n" +
    "        <span class=\"pipeline-status {{common.pipelineStatusMap[pipeline.pipelineId].status}}\">{{common.pipelineStatusMap[pipeline.pipelineId].status}}</span>\n" +
    "\n" +
    "        <span class=\"pipeline-uptime\"\n" +
    "              ng-show=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING'\">\n" +
    "          <span class=\"time-update\" translate=\"home.header.uptime\">Uptime</span>\n" +
    "          <span class=\"time-update\" am-time-ago=\"common.pipelineStatusMap[pipeline.pipelineId].timeStamp\"></span>\n" +
    "        </span>\n" +
    "\n" +
    "        <div class=\"action-menu pull-right\" dropdown>\n" +
    "              <span class=\"dropdown-toggle\" dropdown-toggle\n" +
    "                    ng-click=\"$event.stopPropagation();\">\n" +
    "                <i class=\"fa fa-ellipsis-v\"></i>\n" +
    "              </span>\n" +
    "          <ul class=\"dropdown-menu\" role=\"menu\">\n" +
    "            <!--<li role=\"presentation\"\n" +
    "                ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\"\n" +
    "                ng-hide=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING'\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"deletePipelineConfig(pipeline, $event)\">\n" +
    "                <i class=\"fa fa-trash-o\"></i>\n" +
    "                <span>{{'global.form.delete' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"duplicatePipelineConfig(pipeline, $event)\">\n" +
    "                <i class=\"fa fa-files-o\"></i>\n" +
    "                <span>{{'global.form.duplicate' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"sharePipelineConfig(pipeline, $event)\">\n" +
    "                <i class=\"fa fa-share-alt\"></i>\n" +
    "                <span>{{'global.form.share' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin, userRoles.creator]) && !common.isSlaveNode\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"importPipelineConfig(pipeline, $event)\">\n" +
    "                <i class=\"glyphicon glyphicon-import\"></i>\n" +
    "                <span>{{'global.form.import' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>-->\n" +
    "\n" +
    "            <li role=\"presentation\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"exportPipelineConfig(pipeline, false, $event)\">\n" +
    "                <i class=\"glyphicon glyphicon-export\"></i>\n" +
    "                <span>{{'global.form.export' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin, userRoles.creator, userRoles.manager]) && pipeline.valid\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"exportPipelineConfig(pipeline, true, $event)\">\n" +
    "                <i class=\"glyphicon glyphicon-export\"></i>\n" +
    "                <span>{{'global.form.exportWithLibraryDefinitions' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li ng-repeat-end role=\"presentation\" class=\"divider\"\n" +
    "                ng-if=\"isAuthorized([userRoles.admin, userRoles.manager])\"\n" +
    "                ng-show=\"pipeline.valid\"></li>\n" +
    "\n" +
    "            <li role=\"presentation\"\n" +
    "                ng-if=\"isAuthorized([userRoles.admin, userRoles.manager]) && !common.isSlaveNode\"\n" +
    "                ng-hide=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING' ||\n" +
    "                common.pipelineStatusMap[pipeline.pipelineId].status === 'STARTING' ||\n" +
    "                common.pipelineStatusMap[pipeline.pipelineId].status === 'STOPPING' ||\n" +
    "                !pipeline.valid || isEdgePipeline(pipeline)\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"startPipeline(pipeline, $event)\">\n" +
    "                <i class=\"glyphicon glyphicon-play\"></i>\n" +
    "                <span>{{'home.header.start' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li role=\"presentation\"\n" +
    "                ng-if=\"isAuthorized([userRoles.admin, userRoles.manager]) && !common.isSlaveNode\"\n" +
    "                ng-show=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING'\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"stopPipeline(pipeline, false, $event)\">\n" +
    "                <i class=\"glyphicon glyphicon-stop\"></i>\n" +
    "                <span>{{'home.header.stop' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li role=\"presentation\"\n" +
    "                ng-if=\"isAuthorized([userRoles.admin, userRoles.manager]) && !common.isSlaveNode\"\n" +
    "                ng-show=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'STOPPING'\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"stopPipeline(pipeline, true, $event)\">\n" +
    "                <i class=\"glyphicon glyphicon-stop\"></i>\n" +
    "                <span>{{'home.header.forceStop' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "          </ul>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"invalid-status pull-right\"\n" +
    "             ng-if=\"!pipeline.valid\"\n" +
    "             tooltip-placement=\"bottom\"\n" +
    "             tooltip=\"{{'home.invalidStatus' | translate}}\">\n" +
    "          <i class=\"fa fa-exclamation-triangle fa-12x\"></i>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"invalid-status pull-right\"\n" +
    "             ng-if=\"common.pipelineStatusMap[pipeline.pipelineId].status == 'START_ERROR' || common.pipelineStatusMap[pipeline.pipelineId].status == 'STARTING_ERROR' || common.pipelineStatusMap[pipeline.pipelineId].status == 'RUNNING_ERROR' || common.pipelineStatusMap[pipeline.pipelineId].status == 'RUN_ERROR' || common.pipelineStatusMap[pipeline.pipelineId].status == 'CONNECT_ERROR' || common.pipelineStatusMap[pipeline.pipelineId].status == 'STOP_ERROR'|| common.pipelineStatusMap[pipeline.pipelineId].status == 'STOPPING_ERROR'\"\n" +
    "             tooltip-placement=\"bottom\"\n" +
    "             tooltip=\"{{'Pipeline Status :' + common.pipelineStatusMap[pipeline.pipelineId].status + ': ' + common.pipelineStatusMap[pipeline.pipelineId].message}}\">\n" +
    "          <i class=\"fa fa-exclamation-triangle fa-12x\"></i>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"triggered-alert-status pull-right\"\n" +
    "             ng-if=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING' &&\n" +
    "                   common.alertsMap[pipeline.pipelineId] && common.alertsMap[pipeline.pipelineId].length > 0\"\n" +
    "             tooltip-placement=\"bottom\"\n" +
    "             tooltip-html=\"getPipelineAlerts(common.alertsMap[pipeline.pipelineId])\">\n" +
    "          <i class=\"fa fa-bell fa-12x\"></i>\n" +
    "          <span ng-bind=\"common.alertsMap[pipeline.pipelineId].length\"></span>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"clearfix pipeline-details\" ng-if=\"showDetails\">\n" +
    "\n" +
    "          <div class=\"invalid-status pull-right\"\n" +
    "               ng-if=\"!pipeline.valid\">\n" +
    "            <span>{{'home.invalidStatus' | translate}}</span>\n" +
    "          </div>\n" +
    "\n" +
    "          <div class=\"invalid-status pull-right\"\n" +
    "               ng-if=\"common.pipelineStatusMap[pipeline.pipelineId].status == 'STARTING_ERROR' ||\n" +
    "                   common.pipelineStatusMap[pipeline.pipelineId].status == 'START_ERROR' ||\n" +
    "                   common.pipelineStatusMap[pipeline.pipelineId].status == 'RUNNING_ERROR' ||\n" +
    "                   common.pipelineStatusMap[pipeline.pipelineId].status == 'RUN_ERROR' ||\n" +
    "                   common.pipelineStatusMap[pipeline.pipelineId].status == 'STOPPING_ERROR' ||\n" +
    "                   common.pipelineStatusMap[pipeline.pipelineId].status == 'STOP_ERROR' ||\n" +
    "                   common.pipelineStatusMap[pipeline.pipelineId].status == 'CONNECT_ERROR'\">\n" +
    "            <span>{{common.pipelineStatusMap[pipeline.pipelineId].message}}</span>\n" +
    "          </div>\n" +
    "\n" +
    "          <div class=\"triggered-alert-status  pull-right\"\n" +
    "               ng-if=\"common.pipelineStatusMap[pipeline.pipelineId].status === 'RUNNING' &&\n" +
    "                   common.alertsMap[pipeline.pipelineId] && common.alertsMap[pipeline.pipelineId].length > 0\">\n" +
    "            <span ng-bind-html=\"getPipelineAlerts(common.alertsMap[pipeline.pipelineId])\"></span>\n" +
    "          </div>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "      </li>\n" +
    "\n" +
    "      <li class=\"list-group-item center-align\" ng-if=\"filteredPipelines.length === 0 && !fetching\">\n" +
    "        <span translate=\"home.noPipelines\"></span>\n" +
    "      </li>\n" +
    "\n" +
    "      <li class=\"list-group-item center-align\" ng-if=\"showLoadMore && !fetching\">\n" +
    "        <a class=\"btn btn-default\"\n" +
    "           ng-click=\"onShowMoreClick($event);\">{{'global.form.showMore' | translate}}</a>\n" +
    "      </li>\n" +
    "\n" +
    "      <div class=\"pipelines-loading\">\n" +
    "        <div class=\"pipeline-home-loading\" show-loading=\"fetching\"></div>\n" +
    "      </div>\n" +
    "\n" +
    "    </ul>\n" +
    "  </div>\n" +
    "</section>\n" +
    "");
}]);

angular.module("app/home/library/commit_history/commitHistoryModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/library/commit_history/commitHistoryModal.tpl.html",
    "<form class=\"download-remote-modal-form\" role=\"form\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.commitPipelineHistory.headerLabel\">Pipeline Commit History</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <table class=\"table\">\n" +
    "      <caption>{{pipelineInfo.title}} Commits</caption>\n" +
    "      <thead>\n" +
    "      <tr>\n" +
    "        <th translate=\"home.downloadRemote.version\">Version</th>\n" +
    "\n" +
    "        <th translate=\"home.commitPipelineHistory.commitTime\">Commit Time</th>\n" +
    "\n" +
    "        <th translate=\"home.commitPipelineHistory.committer\">Committer</th>\n" +
    "\n" +
    "        <th translate=\"home.downloadRemote.commitMessage\">Commit Message</th>\n" +
    "\n" +
    "        <th translate=\"home.downloadRemote.actions\">Actions</th>\n" +
    "      </tr>\n" +
    "      </thead>\n" +
    "      <tbody>\n" +
    "      <tr ng-repeat=\"remotePipeline in pipelinesCommit\">\n" +
    "\n" +
    "        <td>{{remotePipeline.version}}</td>\n" +
    "\n" +
    "        <td>{{remotePipeline.commitTime | date: 'medium'}}</td>\n" +
    "\n" +
    "        <td>{{remotePipeline.committer}}</td>\n" +
    "\n" +
    "        <td>{{remotePipeline.commitMessage}}</td>\n" +
    "\n" +
    "        <td>\n" +
    "          <a href=\"javascript:;\" translate=\"global.form.getThisVersion\"\n" +
    "             ng-if=\"pipelineVersion !== remotePipeline.version && !downloading\"\n" +
    "             ng-click=\"downloadRemotePipeline(remotePipeline, $index); $event.stopPropagation()\">Get</a>\n" +
    "          <span ng-if=\"pipelineVersion === remotePipeline.version && !downloading\">Current</span>\n" +
    "        </td>\n" +
    "      </tr>\n" +
    "\n" +
    "      <tr ng-if=\"pipelinesCommit.length === 0\">\n" +
    "        <td colspan=\"5\" class=\"no-records text-center\"\n" +
    "            translate=\"home.commitPipelineHistory.noCommits\">No Commits to view.</td>\n" +
    "      </tr>\n" +
    "\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "\n" +
    "    <ul class=\"properties clearfix\">\n" +
    "      <li class=\"pull-right\">\n" +
    "        <span class=\"properties-label\">{{'home.publish.pipelineRepository' | translate}}: </span>\n" +
    "        <span class=\"properties-value\">\n" +
    "          <a href=\"{{remoteBaseUrl}}\" target=\"_blank\">{{remoteBaseUrl}}</a>\n" +
    "        </span>\n" +
    "      </li>\n" +
    "    </ul>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\"\n" +
    "            ng-click=\"close()\">{{((downloading) ? 'global.form.downloading' : 'global.form.close') | translate}}</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/library/create/create.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/library/create/create.tpl.html",
    "<form class=\"create-pipeline-modal-form\" role=\"form\" ng-submit=\"save()\" autocomplete=\"off\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.library.newPipelineDefinition\">New Pipeline</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors=\"{ trigger: 'keypress' }\">\n" +
    "      <label class=\"control-label\" translate=\"global.form.title\">Title</label>\n" +
    "      <input type=\"text\"\n" +
    "             autofocus\n" +
    "             name=\"name\"\n" +
    "             autocomplete=\"off\"\n" +
    "             class=\"form-control\"\n" +
    "             ng-required\n" +
    "             placeholder=\"{{'home.library.namePlaceholder' | translate}}\"\n" +
    "             ng-model=\"newConfig.name\"\n" +
    "             focus-me=\"true\">\n" +
    "      <p class=\"help-block\">{{'home.library.nameValidation' | translate}}</p>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\">\n" +
    "      <label class=\"control-label\" translate=\"global.form.description\">Description</label>\n" +
    "      <textarea class=\"form-control\" rows=\"2\"\n" +
    "                placeholder=\"{{'home.library.descriptionPlaceholder' | translate}}\"\n" +
    "                ng-model=\"newConfig.description\"></textarea>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\">\n" +
    "      <label class=\"control-label\" translate=\"home.library.whereToRun\">Where would you like it to run?</label>\n" +
    "      <div class=\"radio\">\n" +
    "        <label><input type=\"radio\" name=\"executionMode\" ng-model=\"newConfig.executionMode\" value=\"STANDALONE\">Data Collector</label>\n" +
    "      </div>\n" +
    "      <div class=\"radio\">\n" +
    "        <label><input type=\"radio\" name=\"executionMode\" ng-model=\"newConfig.executionMode\" value=\"EDGE\">Data Collector Edge</label>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\"\n" +
    "            translate=\"global.form.save\">Save</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/library/delete/delete.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/library/delete/delete.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <button type=\"button\" class=\"close\" ng-click=\"no()\">\n" +
    "    <span aria-hidden=\"true\">&times;</span>\n" +
    "    <span class=\"sr-only\">Close</span>\n" +
    "  </button>\n" +
    "  <h3 class=\"modal-title\" translate=\"home.library.deleteConfirmationTitle\">Delete Confirmation</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body multiple-pipelines-modal-body\">\n" +
    "  <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "  <p ng-if=\"!isList\"\n" +
    "    translate=\"home.library.deleteConfirmationMessage\" translate-values=\"{name: pipelineInfo.pipelineId}\"></p>\n" +
    "\n" +
    "  <div ng-if=\"isList\">\n" +
    "    <p ng-if=\"isList\"\n" +
    "       translate=\"home.library.deletePipelinesConfirmationMessage\"></p>\n" +
    "    <p ng-repeat=\"pipeline in pipelineInfo\">{{pipeline.title}}</p>\n" +
    "  </div>\n" +
    "\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"button\" class=\"btn btn-default\" ng-click=\"no()\" ng-if=\"!operationInProgress\"\n" +
    "          translate=\"global.form.no\">No</button>\n" +
    "  <button type=\"button\" class=\"btn btn-primary\" ng-click=\"yes()\" ng-if=\"!operationInProgress\"\n" +
    "          translate=\"global.form.yes\">Yes</button>\n" +
    "  <button type=\"submit\" class=\"btn btn-primary\" ng-if=\"operationInProgress\" disabled\n" +
    "          translate=\"global.form.deleting\">Deleting...</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/library/download_remote/downloadRemoteModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/library/download_remote/downloadRemoteModal.tpl.html",
    "<form class=\"download-remote-modal-form\" role=\"form\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"close()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.downloadRemote.headerLabel\">Publish Pipeline</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <table class=\"table\">\n" +
    "      <thead>\n" +
    "      <tr>\n" +
    "        <th translate=\"home.downloadRemote.pipelineName\">Pipeline Name</th>\n" +
    "        <th translate=\"home.downloadRemote.pipelineId\">Pipeline ID</th>\n" +
    "        <th translate=\"home.downloadRemote.version\">Version</th>\n" +
    "        <th translate=\"home.downloadRemote.commitMessage\">Commit Message</th>\n" +
    "        <th translate=\"home.downloadRemote.actions\">Actions</th>\n" +
    "      </tr>\n" +
    "      </thead>\n" +
    "      <tbody>\n" +
    "      <tr ng-repeat=\"remotePipeline in remotePipelines | orderBy:sortColumn:sortReverse\">\n" +
    "        <td>{{remotePipeline.name}}</td>\n" +
    "        <td>{{remotePipeline.pipelineId}}</td>\n" +
    "        <td>{{remotePipeline.version}}</td>\n" +
    "        <td>{{remotePipeline.commitMessage}}</td>\n" +
    "        <td>\n" +
    "          <a href=\"javascript:;\" translate=\"global.form.download\"\n" +
    "             ng-if=\"!downloading[remotePipeline.commitId] && !downloaded[remotePipeline.commitId]\"\n" +
    "             ng-click=\"downloadRemotePipeline(remotePipeline, $index); $event.stopPropagation()\">Download</a>\n" +
    "\n" +
    "          <span ng-if=\"downloaded[remotePipeline.commitId]\">Downloaded</span>\n" +
    "          <span ng-if=\"downloading[remotePipeline.commitId]\">Downloading ....</span>\n" +
    "        </td>\n" +
    "      </tr>\n" +
    "\n" +
    "      <tr ng-if=\"remotePipelines.length === 0\">\n" +
    "        <td colspan=\"5\" class=\"no-records text-center\"\n" +
    "            translate=\"home.downloadRemote.noPipelines\">No Pipelines to view.</td>\n" +
    "      </tr>\n" +
    "\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "\n" +
    "    <ul class=\"properties clearfix\">\n" +
    "      <li class=\"pull-right\">\n" +
    "        <span class=\"properties-label\">{{'home.publish.pipelineRepository' | translate}}: </span>\n" +
    "        <span class=\"properties-value\">\n" +
    "          <a href=\"{{remoteBaseUrl}}\" target=\"_blank\">{{remoteBaseUrl}}</a>\n" +
    "        </span>\n" +
    "      </li>\n" +
    "    </ul>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"close()\" translate=\"global.form.close\">close</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/library/duplicate/duplicate.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/library/duplicate/duplicate.tpl.html",
    "<form class=\"duplicate-modal-form\" role=\"form\" ng-submit=\"save()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.library.duplicatePipelineDefinition\">Duplicate Pipeline Definition</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div class=\"form-group\">\n" +
    "      <label class=\"control-label\" translate=\"global.form.title\">Title</label>\n" +
    "      <input type=\"text\" class=\"form-control\" required\n" +
    "             ng-disabled=\"operationInProgress\"\n" +
    "             placeholder=\"{{'home.library.namePlaceholder' | translate}}\"\n" +
    "             ng-model=\"newConfig.title\"\n" +
    "             focus-me=\"true\">\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\">\n" +
    "      <label class=\"control-label\" translate=\"global.form.description\">Description</label>\n" +
    "      <textarea class=\"form-control\" rows=\"2\"\n" +
    "                ng-disabled=\"operationInProgress\"\n" +
    "                placeholder=\"{{'home.library.descriptionPlaceholder' | translate}}\"\n" +
    "                ng-model=\"newConfig.description\"></textarea>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\">\n" +
    "      <label class=\"control-label\" translate=\"global.form.numberOfCopies\">Number of Copies</label>\n" +
    "      <input type=\"number\" class=\"form-control\"\n" +
    "             ng-disabled=\"operationInProgress\"\n" +
    "             required min=\"1\" max=\"1000\" ng-model=\"newConfig.numberOfCopies\">\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\" ng-if=\"!operationInProgress\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" ng-disabled=\"!newConfig.title\" ng-if=\"!operationInProgress\"\n" +
    "            translate=\"global.form.duplicate\">Duplicate</button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" ng-if=\"operationInProgress\" disabled\n" +
    "            translate=\"global.form.duplicating\">Duplicating...</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/library/import/importModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/library/import/importModal.tpl.html",
    "<form class=\"import-modal-form\" role=\"form\" ng-submit=\"import()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.import.headerLabel\">Import Pipeline Definition</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div class=\"form-group btn-group toggle-toolbar\" ng-show=\"pipelineInfo\">\n" +
    "      <label class=\"btn btn-default btn-sm\" ng-model=\"createNewPipeline\" btn-radio=\"false\">\n" +
    "        {{'home.import.overwritePipeline' | translate}}\n" +
    "      </label>\n" +
    "      <!--<label class=\"btn btn-default btn-sm\" ng-model=\"createNewPipeline\" btn-radio=\"true\">\n" +
    "        {{'home.import.createPipelineAndImport' | translate}}\n" +
    "      </label>-->\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" show-errors=\"{ trigger: 'keypress' }\" ng-if=\"createNewPipeline\">\n" +
    "      <label class=\"control-label\" translate=\"global.form.title\">Title</label>\n" +
    "      <input type=\"text\"\n" +
    "             autofocus\n" +
    "             name=\"name\"\n" +
    "             class=\"form-control\"\n" +
    "             ng-required\n" +
    "             placeholder=\"{{'home.library.namePlaceholder' | translate}}\"\n" +
    "             ng-model=\"newConfig.title\"\n" +
    "             focus-me=\"true\">\n" +
    "      <p class=\"help-block\">{{'home.library.nameValidation' | translate}}</p>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" ng-if=\"createNewPipeline\">\n" +
    "      <label class=\"control-label\" translate=\"global.form.description\">Description</label>\n" +
    "      <textarea class=\"form-control\" rows=\"2\"\n" +
    "                placeholder=\"{{'home.library.descriptionPlaceholder' | translate}}\"\n" +
    "                ng-model=\"newConfig.description\"></textarea>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"input-group\">\n" +
    "      <span class=\"input-group-btn\">\n" +
    "          <span class=\"btn btn-primary btn-file\">\n" +
    "              {{'home.import.browse' | translate}}&hellip; <input type=\"file\" fileread=\"uploadFile\">\n" +
    "          </span>\n" +
    "      </span>\n" +
    "      <input type=\"text\" class=\"form-control\" readonly ng-model=\"uploadFile.name\"\n" +
    "             placeholder=\"{{'home.import.fileUploadPlaceholder' | translate}}\">\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" ng-disabled=\"!uploadFile.name\"\n" +
    "            translate=\"global.form.import\">Import</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/library/importFromArchive/importFromArchiveModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/library/importFromArchive/importFromArchiveModal.tpl.html",
    "<form class=\"import-modal-form\" role=\"form\" ng-submit=\"import()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.header.importPipelinesFromArchive\">Import Pipelines From Archive</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div class=\"input-group\" ng-hide=\"operationDone\">\n" +
    "      <span class=\"input-group-btn\">\n" +
    "          <span class=\"btn btn-primary btn-file\">\n" +
    "              {{'home.import.browse' | translate}}&hellip; <input type=\"file\" ng-disabled=\"operationInProgress\" fileread=\"uploadFile\">\n" +
    "          </span>\n" +
    "      </span>\n" +
    "      <input type=\"text\" class=\"form-control\" readonly ng-model=\"uploadFile.name\"\n" +
    "             placeholder=\"{{'home.import.fileUploadPlaceholder' | translate}}\">\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"input-group\" ng-if=\"operationDone && successEntities && successEntities.length > 0\">\n" +
    "      <p translate=\"home.header.successImportConfirmationMessage\"></p>\n" +
    "      <p ng-repeat=\"pipeline in successEntities\">{{pipeline.title}} ({{pipeline.pipelineId}})</p>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\" ng-hide=\"operationDone || operationInProgress\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" ng-disabled=\"!uploadFile.name || operationInProgress\"\n" +
    "            ng-hide=\"operationDone\">{{(operationInProgress ? 'global.form.importing' : 'global.form.import') | translate}}</button>\n" +
    "\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"close()\" ng-show=\"operationDone\"\n" +
    "            translate=\"global.form.close\">Close</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/library/library.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/library/library.tpl.html",
    "<div class=\"panel panel-default library-pane\" ng-controller=\"LibraryController\">\n" +
    "  <div class=\"panel-body\">\n" +
    "    <!--<div class=\"btn-group create-pipeline-btn-group\" ng-if=\"!common.isSlaveNode\">\n" +
    "      <button type=\"button\" class=\"btn btn-primary btn-sm create-pipeline-btn\"\n" +
    "              ng-disabled=\"!isAuthorized([userRoles.admin, userRoles.creator])\"\n" +
    "              ng-click=\"addPipelineConfig()\">\n" +
    "        <span class=\"glyphicon glyphicon-plus\"></span>\n" +
    "        <span translate=\"home.header.createNewPipeline\">Create</span>\n" +
    "      </button>\n" +
    "      <button type=\"button\" class=\"btn btn-primary btn-sm dropdown-toggle\"\n" +
    "              ng-disabled=\"!isAuthorized([userRoles.admin, userRoles.creator])\"\n" +
    "              data-toggle=\"dropdown\" aria-haspopup=\"true\"\n" +
    "              aria-expanded=\"false\">\n" +
    "        <span class=\"caret\"></span>\n" +
    "        <span class=\"sr-only\">Toggle Dropdown</span>\n" +
    "      </button>\n" +
    "      <ul class=\"dropdown-menu\">\n" +
    "        <li>\n" +
    "          <a href=\"#\" ng-click=\"addPipelineConfig()\" translate=\"home.header.createNewPipeline\">Create New Pipeline</a>\n" +
    "        </li>\n" +
    "        <li>\n" +
    "          <a href=\"#\" ng-click=\"importPipelineConfig()\"translate=\"home.header.importPipeline\">Import Pipeline</a>\n" +
    "        </li>\n" +
    "        <li>\n" +
    "          <a href=\"#\" ng-click=\"importPipelinesFromArchive()\"translate=\"home.header.importPipelinesFromArchive\">Import Pipelines From Archive</a>\n" +
    "        </li>\n" +
    "        <li>\n" +
    "          <a href=\"#\"\n" +
    "             ng-if=\"common.isDPMEnabled\"\n" +
    "             ng-click=\"downloadRemotePipelineConfig()\"translate=\"home.downloadRemotePipeline\">Download Remote Pipeline</a>\n" +
    "        </li>\n" +
    "      </ul>\n" +
    "    </div>-->\n" +
    "\n" +
    "    <div show-loading=\"fetchingSystemLabels\"></div>\n" +
    "\n" +
    "    <ul class=\"list-group predefined-labels\" mutation-callback=\"treeElementChange\">\n" +
    "      <li class=\"hidden\">{{treeElementTimestamp}}</li>\n" +
    "\n" +
    "      <li ng-repeat=\"systemLabel in systemPipelineLabels\"\n" +
    "          class=\"list-group-item pointer clearfix\">\n" +
    "\n" +
    "        <div ng-click=\"onSelectLabel(systemLabel)\"\n" +
    "             ng-class=\"{'active': selectedPipelineLabel === systemLabel}\"\n" +
    "             class=\"list-group-item-wrapper\">{{'home.library.' + systemLabel | translate}}\n" +
    "        </div>\n" +
    "      </li>\n" +
    "\n" +
    "      <li class=\"border-list-group-item\" ng-if=\"hasPipelineLabels()\">\n" +
    "        <span translate=\"global.form.labels\">LABELS</span>\n" +
    "      </li>\n" +
    "\n" +
    "      <li ng-if=\"hasPipelineLabels()\">\n" +
    "        <div class=\"btn-group label-filter-wrapper\">\n" +
    "\n" +
    "          <input ng-model=\"labelFilter.value\"\n" +
    "                 ng-model-options=\"{debounce: getDebounce()}\"\n" +
    "                 ng-class=\"{'loading-labels': getDebounce() > 0 && loadingLabels}\"\n" +
    "                 ng-change=\"filterLabels()\"\n" +
    "                 type=\"search\"\n" +
    "                 class=\"form-control label-filter-input\"\n" +
    "                 placeholder=\"Filter Labels\">\n" +
    "\n" +
    "          <span ng-hide=\"getDebounce() > 0 && loadingLabels\" ng-click=\"clearLabelFilter()\" class=\"glyphicon glyphicon-remove-circle label-filter-icon\"></span>\n" +
    "          <i ng-show=\"getDebounce() > 0 && loadingLabels\" class=\"fa fa-circle-o-notch fa-spin fa-fw label-filter-icon\"></i>\n" +
    "        </div>\n" +
    "      </li>\n" +
    "\n" +
    "      <li ng-repeat=\"node in pipelineLabels track by node.vPath\"\n" +
    "          ng-show=\"node.isVisible\"\n" +
    "          class=\"list-group-item pointer clearfix\">\n" +
    "\n" +
    "        <div ng-click=\"onSelectNode(node)\"\n" +
    "             ng-class=\"{'active': selectedPipelineLabel === node.vPath}\"\n" +
    "             class=\"list-group-item-wrapper\"\n" +
    "             style=\"padding-left: {{::node.level * 10}}px;\">\n" +
    "\n" +
    "          <span class=\"list-group-icon-wrapper\">\n" +
    "            <i ng-show=\"hasChildren(node) && node.isExpanded\" class=\"fa fa-caret-down\"></i>\n" +
    "            <i ng-show=\"hasChildren(node) && !node.isExpanded\" class=\"fa fa-caret-right\"></i>\n" +
    "          </span>\n" +
    "\n" +
    "          <span class=\"label-display\">{{::node.label}}</span>\n" +
    "        </div>\n" +
    "      </li>\n" +
    "\n" +
    "    </ul>\n" +
    "  </div>\n" +
    " </div>\n" +
    "");
}]);

angular.module("app/home/library/publish/publishModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/library/publish/publishModal.tpl.html",
    "<form class=\"publish-modal-form\" role=\"form\" name=\"publishForm\" ng-submit=\"publish()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\"\n" +
    "        ng-if=\"!isList\"\n" +
    "        translate=\"home.publish.headerLabel\">Publish Pipeline</h3>\n" +
    "    <h3 class=\"modal-title\"\n" +
    "        ng-if=\"isList\"\n" +
    "        translate=\"home.publish.listHeaderLabel\">Publish Pipelines</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div ng-if=\"!isList\" class=\"form-group\" show-errors=\"{ trigger: 'keypress' }\">\n" +
    "      <label class=\"control-label\" translate=\"global.form.name\">Name</label>\n" +
    "      <i class=\"fa fa-info-circle help-icon\"\n" +
    "         popover-placement=\"right\"\n" +
    "         popover-append-to-body=\"false\"\n" +
    "         popover=\"{{'home.library.nameValidation' | translate}}\"\n" +
    "         popover-trigger=\"mouseenter\"></i>\n" +
    "      <input type=\"text\"\n" +
    "             disabled\n" +
    "             name=\"name\"\n" +
    "             class=\"form-control\"\n" +
    "             ng-required\n" +
    "             placeholder=\"{{'home.library.namePlaceholder' | translate}}\"\n" +
    "             ng-model=\"commitPipelineModel.name\"\n" +
    "             focus-me=\"true\">\n" +
    "      <p class=\"help-block\">{{'home.library.nameValidation' | translate}}</p>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\">\n" +
    "      <label class=\"control-label\" translate=\"home.library.commitMessage\">Commit Message</label>\n" +
    "      <textarea autofocus class=\"form-control\" rows=\"2\"\n" +
    "                required\n" +
    "                placeholder=\"{{'home.library.commitMessagePlaceholder' | translate}}\"\n" +
    "                ng-model=\"commitPipelineModel.commitMessage\"></textarea>\n" +
    "    </div>\n" +
    "\n" +
    "    <ul class=\"properties clearfix\">\n" +
    "      <li class=\"pull-right\">\n" +
    "        <span class=\"properties-label\">{{'home.publish.pipelineRepository' | translate}}: </span>\n" +
    "        <span class=\"properties-value\">\n" +
    "          <a href=\"{{remoteBaseUrl}}\" target=\"_blank\">{{remoteBaseUrl}}</a>\n" +
    "        </span>\n" +
    "      </li>\n" +
    "    </ul>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-disabled=\"publishing\" ng-click=\"cancel()\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" ng-disabled=\"publishing || publishForm.$invalid\"\n" +
    "            translate=\"global.form.publish\">Publish</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/library/revert_changes/revertChangesModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/library/revert_changes/revertChangesModal.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <button type=\"button\" class=\"close\" ng-click=\"no()\">\n" +
    "    <span aria-hidden=\"true\">&times;</span>\n" +
    "    <span class=\"sr-only\">Close</span>\n" +
    "  </button>\n" +
    "  <h3 class=\"modal-title\" translate=\"home.library.revertChangesConfirmationTitle\">Revert Changes</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body multiple-pipelines-modal-body\">\n" +
    "  <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "  <p translate=\"home.library.revertChangesConfirmationMessage\" translate-values=\"{version: dpmPipelineVersion}\"></p>\n" +
    "\n" +
    "\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"button\" class=\"btn btn-default\" ng-click=\"no()\" ng-if=\"!operationInProgress\"\n" +
    "          translate=\"global.form.no\">No</button>\n" +
    "  <button type=\"button\" class=\"btn btn-primary\" ng-click=\"yes()\" ng-if=\"!operationInProgress\"\n" +
    "          translate=\"global.form.yes\">Yes</button>\n" +
    "  <button type=\"submit\" class=\"btn btn-primary\" ng-if=\"operationInProgress\" disabled\n" +
    "          translate=\"home.library.revertingChanges\">Reverting Changes...</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/library/share/share.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/library/share/share.tpl.html",
    "<form class=\"share-pipeline-modal-form\" role=\"form\" autocomplete=\"off\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.library.sharingSettings\">Sharing Settings</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div class=\"add-users-groups form-horizontal\" ng-if=\"!isACLReadyOnly\">\n" +
    "      <div class=\"form-group\">\n" +
    "        <div class=\"controls col-lg-offset-6 col-lg-5\">\n" +
    "          <ui-select multiple\n" +
    "                     tagging\n" +
    "                     tagging-label=\" \"\n" +
    "                     tagging-tokens=\",|ENTER\"\n" +
    "                     ng-model=\"newSubjectList.value\">\n" +
    "            <ui-select-match placeholder=\"{{'home.library.selectUsersAndGroupsPlaceholder' | translate}}\" class=\"ui-select-match\">{{$item}}</ui-select-match>\n" +
    "            <ui-select-choices class=\"ui-select-choices\"\n" +
    "                               group-by=\"groupSubjectsFn\"\n" +
    "                               repeat=\"listValue in filteredSubjects | filter:$select.search\">\n" +
    "              <div ng-bind-html=\"listValue | highlight: $select.search\"></div>\n" +
    "            </ui-select-choices>\n" +
    "          </ui-select>\n" +
    "        </div>\n" +
    "\n" +
    "        <button type=\"button\" class=\"btn btn-default\" ng-click=\"inviteOthers()\">Add</button>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <table class=\"table table-hover\">\n" +
    "      <thead>\n" +
    "      <tr>\n" +
    "        <th class=\"subject-col\">{{'global.form.subject' | translate}}</th>\n" +
    "        <th class=\"action-col\">{{'global.form.read' | translate}}</th>\n" +
    "        <th class=\"action-col\">{{'global.form.write' | translate}}</th>\n" +
    "        <th class=\"action-col\">{{'global.form.execute' | translate}}</th>\n" +
    "        <th class=\"action-menu-col\"></th>\n" +
    "      </tr>\n" +
    "      </thead>\n" +
    "      <tbody ng-hide=\"showLoading\">\n" +
    "      <tr ng-repeat=\"permission in acl.permissions track by $index\">\n" +
    "        <td>\n" +
    "          <span class=\"fa fa-user user-icon\"\n" +
    "                ng-class=\"{'fa-user': permission.subjectType === 'USER', 'fa-users': permission.subjectType === 'GROUP'}\"></span>\n" +
    "          <span class=\"subject-id\">{{permission.subjectId}}</span>\n" +
    "          <span class=\"is-owner\"\n" +
    "                ng-if=\"permission.subjectId === acl.resourceOwner\"> ({{'global.form.owner' | translate}})</span>\n" +
    "        </td>\n" +
    "\n" +
    "        <td>\n" +
    "          <input type=\"checkbox\" name=\"creatorReadValue\"\n" +
    "                 ng-click=\"onActionToggle(permission, 'READ', $event)\"\n" +
    "                 ng-checked=\"permission.actions.indexOf('READ') !== -1\"\n" +
    "                 disabled>\n" +
    "        </td>\n" +
    "        <td>\n" +
    "          <input type=\"checkbox\" name=\"creatorWritwValue\"\n" +
    "                 ng-click=\"onActionToggle(permission, 'WRITE', $event)\"\n" +
    "                 ng-checked=\"permission.actions.indexOf('WRITE') !== -1\"\n" +
    "                 ng-disabled=\"permission.subjectId === acl.resourceOwner || isACLReadyOnly\">\n" +
    "        </td>\n" +
    "        <td>\n" +
    "          <input type=\"checkbox\" name=\"creatorExecuteValue\"\n" +
    "                 ng-click=\"onActionToggle(permission, 'EXECUTE', $event)\"\n" +
    "                 ng-checked=\"permission.actions.indexOf('EXECUTE') !== -1\"\n" +
    "                 ng-disabled=\"permission.subjectId === acl.resourceOwner || isACLReadyOnly\">\n" +
    "        </td>\n" +
    "        <td class=\"action-menu-col\">\n" +
    "          <div class=\"action-menu pull-right\" dropdown\n" +
    "               ng-if=\"permission.subjectId !== acl.resourceOwner && !isACLReadyOnly\">\n" +
    "              <span class=\"dropdown-toggle\" dropdown-toggle\n" +
    "                    ng-click=\"$event.stopPropagation();\">\n" +
    "                <i class=\"fa fa-ellipsis-v\"></i>\n" +
    "              </span>\n" +
    "            <ul class=\"dropdown-menu\" role=\"menu\">\n" +
    "              <li role=\"presentation\"\n" +
    "                  ng-if=\"permission.subjectId !== acl.resourceOwner && permission.subjectType === 'USER'\">\n" +
    "                <a href=\"#\" dropdown-toggle ng-click=\"changeOwner(permission, $index)\">\n" +
    "                  <span>{{'global.form.isOwner' | translate}}</span>\n" +
    "                </a>\n" +
    "              </li>\n" +
    "              <li role=\"presentation\"\n" +
    "                  ng-if=\"permission.subjectId !== acl.resourceOwner\">\n" +
    "                <a href=\"#\" dropdown-toggle ng-click=\"removePermission(permission, $index)\">\n" +
    "                  <span>{{'global.form.delete' | translate}}</span>\n" +
    "                </a>\n" +
    "              </li>\n" +
    "            </ul>\n" +
    "          </div>\n" +
    "\n" +
    "        </td>\n" +
    "      </tr>\n" +
    "      <tr ng-if=\"!acl || acl.permissions.length === 0\">\n" +
    "        <td colspan=\"5\">{{'global.form.noRecords' | translate}}</td>\n" +
    "      </tr>\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "\n" +
    "    <div ng-if=\"!isACLEnabled\" class=\"alert alert-info\" role=\"alert\">\n" +
    "      Note: Data Collector pipeline sharing (ACLs) are not enabled. Update the Data Collector configuration file to enable pipeline sharing.\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "    <button ng-if=\"!isACLReadyOnly\" ng-click=\"save()\" class=\"btn btn-primary\"\n" +
    "            translate=\"global.form.save\">Save</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/packageManager/customRepoUrl/customRepoUrl.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/packageManager/customRepoUrl/customRepoUrl.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <h3 class=\"modal-title\" translate=\"packageManager.customRepoUrl.title\">Configure Custom Repo URL</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body package-manager-modal-body\">\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"control-label\" translate=\"packageManager.customRepoUrl.repoUrl\">Custom Repo URL</label>\n" +
    "    <i class=\"fa fa-info-circle help-icon\"\n" +
    "       popover-placement=\"right\"\n" +
    "       popover-append-to-body=\"false\"\n" +
    "       popover=\"{{'packageManager.customRepoUrl.urlValidation' | translate}}\"\n" +
    "       popover-trigger=\"mouseenter\"></i>\n" +
    "    <input type=\"text\"\n" +
    "           autofocus\n" +
    "           name=\"name\"\n" +
    "           autocomplete=\"off\"\n" +
    "           class=\"form-control\"\n" +
    "           title=\"{{'packageManager.customRepoUrl.repoUrl' | translate}}\"\n" +
    "           placeholder=\"{{'packageManager.customRepoUrl.repoUrl' | translate}}\"\n" +
    "           ng-model=\"repoUrl\"\n" +
    "           focus-me=\"true\">\n" +
    "  </div>\n" +
    "\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"button\" class=\"btn btn-default\" ng-click=\"cancel()\"\n" +
    "          translate=\"global.form.cancel\">Cancel</button>\n" +
    "\n" +
    "  <button ng-click=\"save()\" class=\"btn btn-primary\"\n" +
    "          translate=\"global.form.save\">Save</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/packageManager/delete_extras/deleteExtras.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/packageManager/delete_extras/deleteExtras.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <h3 class=\"modal-title\" translate=\"packageManager.deleteExtras.title\">Delete Additional Libraries</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body package-manager-modal-body\">\n" +
    "  <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "  <div ng-if=\"!libraryUninstalled\">\n" +
    "    <p translate=\"packageManager.deleteExtras.confirmationMessage\"></p>\n" +
    "    <p class=\"stage-library-label\" ng-repeat=\"library in stageLibrariesExtras\">{{library.id}}</p>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-if=\"libraryUninstalled && !isRestartInProgress\" class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "       translate=\"packageManager.uninstall.successMessage\">\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "       ng-show=\"isRestartInProgress\"\n" +
    "       translate=\"home.restart.successMessage\">\n" +
    "  </div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"button\" class=\"btn btn-default\" ng-click=\"cancel()\" ng-if=\"!operationInProgress\"\n" +
    "          translate=\"global.form.cancel\">Cancel</button>\n" +
    "  <button type=\"button\" class=\"btn btn-primary\" ng-click=\"deleteExtras()\" ng-if=\"!operationInProgress && !libraryUninstalled\"\n" +
    "          translate=\"global.form.delete\">Delete</button>\n" +
    "  <button type=\"submit\" class=\"btn btn-primary\" ng-if=\"operationInProgress\" disabled\n" +
    "          translate=\"global.form.deleting\">Deleting...</button>\n" +
    "  <button ng-if=\"!operationInProgress && libraryUninstalled && !isRestartInProgress\"\n" +
    "          ng-click=\"restart()\"\n" +
    "          class=\"btn btn-primary\" translate=\"global.form.restart\">Restart</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/packageManager/install/install.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/packageManager/install/install.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <h3 class=\"modal-title\" translate=\"packageManager.install.title\">Install Stage Library</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body package-manager-modal-body\">\n" +
    "  <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "  <div ng-if=\"operationStatus === 'complete' && !hasErrors()\"\n" +
    "       class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "       translate=\"packageManager.install.successMessage\">\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-if=\"operationStatus === 'restarting'\"\n" +
    "       class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "       translate=\"home.restart.successMessage\">\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-if=\"operationStatus !== 'complete'\">\n" +
    "    <div ng-if=\"maprStageLib\" class=\"alert alert-info\" role=\"alert\">\n" +
    "      You must perform additional steps to install MapR stage libraries. For more information, see <a href=\"https://streamsets.com/documentation/datacollector/latest/help/#Installation/MapR-Prerequisites.html\" target=\"_blank\">MapR Prerequisites</a>.\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <p translate=\"packageManager.install.confirmationMessage\"></p>\n" +
    "\n" +
    "  <ul class=\"stage-library-labels\">\n" +
    "    <li ng-repeat=\"library in libraryList\">\n" +
    "      <span class=\"icon-wrapper\">\n" +
    "        <i ng-if=\"inStatus(library, 'installing')\" class=\"fa fa-circle-o-notch fa-spin\"></i>\n" +
    "        <i ng-if=\"inStatus(library, 'installed')\" class=\"fa fa-check\"></i>\n" +
    "        <i ng-if=\"inStatus(library, 'failed')\" class=\"fa fa-remove\"></i>\n" +
    "      </span>\n" +
    "      <span>{{library.label}}</span>\n" +
    "      <a href=\"#\" ng-click=\"showError(library)\" ng-show=\"hasError(library)\">show error</a>\n" +
    "    </li>\n" +
    "  </ul>\n" +
    "\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"button\" class=\"btn btn-default\" ng-click=\"cancel()\"\n" +
    "          ng-if=\"operationStatus !== 'installing' && operationStatus !== 'restarting'\"\n" +
    "          translate=\"global.form.cancel\">Cancel</button>\n" +
    "\n" +
    "  <button type=\"button\" class=\"btn btn-primary\" ng-click=\"install()\"\n" +
    "          ng-if=\"operationStatus === 'incomplete'\"\n" +
    "          translate=\"global.form.install\">Install</button>\n" +
    "\n" +
    "  <button type=\"button\" class=\"btn btn-primary\" ng-click=\"retry()\"\n" +
    "          ng-if=\"operationStatus === 'complete' && hasErrors()\"\n" +
    "          translate=\"global.form.retry\">Retry</button>\n" +
    "\n" +
    "  <button type=\"button\" class=\"btn btn-primary\" disabled\n" +
    "          ng-if=\"operationStatus === 'installing'\"\n" +
    "          translate=\"global.form.installing\">Installing...</button>\n" +
    "\n" +
    "  <button ng-click=\"restart()\" class=\"btn btn-primary\"\n" +
    "          ng-if=\"operationStatus === 'complete'\"\n" +
    "          translate=\"global.form.restart\">Restart</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/packageManager/package_manager.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/packageManager/package_manager.tpl.html",
    "<div class=\"panel panel-default page-panel package-manager-page\">\n" +
    "  <ng-include src=\"'app/home/packageManager/package_manager_header.tpl.html'\"></ng-include>\n" +
    "\n" +
    "\n" +
    "  <div class=\"home-bg-splitter\" ng-cloak>\n" +
    "    <bg-splitter orientation=\"horizontal\" class=\"library-splitter\"\n" +
    "                 ng-class=\"{'hide-library-panel': hideLibraryPanel}\">\n" +
    "\n" +
    "      <bg-pane min-size=\"0\">\n" +
    "        <ng-include src=\"'app/home/packageManager/package_manager_side_nav.tpl.html'\"></ng-include>\n" +
    "      </bg-pane>\n" +
    "\n" +
    "      <bg-pane min-size=\"0\">\n" +
    "        <div class=\"home-page-body\">\n" +
    "          <ng-include src=\"'app/home/alerts/error/errorModal.tpl.html'\"></ng-include>\n" +
    "          <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "          <ng-include ng-if=\"selectedNavigationItem !== extrasNavigationItem\"\n" +
    "                      src=\"'app/home/packageManager/package_manager_list_view.tpl.html'\"></ng-include>\n" +
    "\n" +
    "          <ng-include ng-if=\"selectedNavigationItem === extrasNavigationItem\"\n" +
    "                      src=\"'app/home/packageManager/package_manager_extras_view.tpl.html'\"></ng-include>\n" +
    "\n" +
    "          <ul class=\"properties clearfix\" ng-if=\"stageLibraries.length === filteredStageLibraries.length && manifestURL\">\n" +
    "            <li class=\"pull-right\">\n" +
    "              <span class=\"properties-label\">{{'packageManager.manifestURL' | translate}}: </span>\n" +
    "              <span class=\"properties-value\">\n" +
    "                <a href=\"{{manifestURL}}\" target=\"_blank\">{{manifestURL}}</a>\n" +
    "              </span>\n" +
    "            </li>\n" +
    "          </ul>\n" +
    "        </div>\n" +
    "      </bg-pane>\n" +
    "\n" +
    "    </bg-splitter>\n" +
    "  </div>\n" +
    "\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/packageManager/package_manager_extras_view.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/packageManager/package_manager_extras_view.tpl.html",
    "<section class=\"pipeline-list-view\">\n" +
    "\n" +
    "  <div class=\"list-content\">\n" +
    "    <ul class=\"list-group checked-list-box\">\n" +
    "\n" +
    "      <li class=\"list-group-item header-list-group-item\">\n" +
    "        <i ng-if=\"allSelected\"\n" +
    "           ng-click=\"unSelectAll()\"\n" +
    "           class=\"glyphicon glyphicon-check fa-12x header-check-box\"></i>\n" +
    "\n" +
    "        <i ng-if=\"!allSelected\"\n" +
    "           ng-click=\"selectAll()\"\n" +
    "           class=\"glyphicon glyphicon-unchecked fa-12x header-check-box\"></i>\n" +
    "\n" +
    "        <div class=\"stage-library-name header\">\n" +
    "          <a ng-click=\"onSortColumnHeaderClick('label')\">\n" +
    "            <span>File Name</span>\n" +
    "            <i ng-if=\"header.sortColumn !== 'label'\" class=\"fa fa-sort\" aria-hidden=\"true\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'label' && !header.sortReverse\" class=\"fa fa-arrow-down\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'label' && header.sortReverse\" class=\"fa fa-arrow-up\"></i>\n" +
    "          </a>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"stage-library-id header\">\n" +
    "          <a ng-click=\"onSortColumnHeaderClick('libraryId')\">\n" +
    "            <span>Library ID</span>\n" +
    "            <i ng-if=\"header.sortColumn !== 'libraryId'\" class=\"fa fa-sort\" aria-hidden=\"true\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'libraryId' && !header.sortReverse\" class=\"fa fa-arrow-down\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'libraryId' && header.sortReverse\" class=\"fa fa-arrow-up\"></i>\n" +
    "          </a>\n" +
    "        </div>\n" +
    "      </li>\n" +
    "\n" +
    "      <li class=\"list-group-item\"\n" +
    "          ng-class=\"{'active-info': selectedStageLibraryMap[stageLibrary.id]}\"\n" +
    "          ng-repeat=\"stageLibrary in stageLibrariesExtras | orderBy: customStageLibrarySortFunction: header.sortReverse | limitTo: limit\">\n" +
    "\n" +
    "        <i ng-if=\"selectedStageLibraryMap[stageLibrary.id]\"\n" +
    "           ng-click=\"unSelectStageLibrary(stageLibrary)\"\n" +
    "           class=\"glyphicon glyphicon-check fa-12x\"></i>\n" +
    "\n" +
    "        <i ng-if=\"!selectedStageLibraryMap[stageLibrary.id]\"\n" +
    "           ng-click=\"selectStageLibrary(stageLibrary)\"\n" +
    "           class=\"glyphicon glyphicon-unchecked fa-12x\"></i>\n" +
    "\n" +
    "        <div class=\"stage-library-name\">\n" +
    "          <span>{{stageLibrary.fileName}}</span>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"stage-library-id\">\n" +
    "          <span>{{stageLibrary.libraryId}}</span>\n" +
    "        </div>\n" +
    "\n" +
    "      </li>\n" +
    "\n" +
    "      <li class=\"list-group-item center-align\" ng-if=\"stageLibrariesExtras.length === 0 && !fetching\">\n" +
    "        <span translate=\"packageManager.noStageLibrariesExtras\"></span>\n" +
    "      </li>\n" +
    "\n" +
    "      <div class=\"stage-libraries-loading\">\n" +
    "        <div class=\"pipeline-home-loading\" show-loading=\"fetching\"></div>\n" +
    "      </div>\n" +
    "    </ul>\n" +
    "  </div>\n" +
    "</section>\n" +
    "");
}]);

angular.module("app/home/packageManager/package_manager_header.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/packageManager/package_manager_header.tpl.html",
    "<div class=\"panel panel-default page-panel home-page\">\n" +
    "\n" +
    "  <div class=\"panel-heading clearfix\">\n" +
    "\n" +
    "    <div class=\"panel-title \">\n" +
    "      <div class=\"pull-left\">\n" +
    "        <button type=\"button\" class=\"navbar-toggle collapsed\"\n" +
    "                ng-if=\"activeConfigStatus.executionMode != pipelineConstant.SLAVE\"\n" +
    "                ng-class=\"{'selected' : !hideLibraryPanel}\"\n" +
    "                ng-click=\"toggleLibraryPanel()\"\n" +
    "                title=\"{{'home.header.toggleLibraryPane' | translate}}\">\n" +
    "          <i class=\"icon-bar\"></i>\n" +
    "          <i class=\"icon-bar\"></i>\n" +
    "          <i class=\"icon-bar\"></i>\n" +
    "        </button>\n" +
    "      </div>\n" +
    "      <h3 class=\"pull-left\" translate=\"packageManager.title\">Package Manager</h3>\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"selectedNavigationItem !== extrasNavigationItem\">\n" +
    "      <div class=\"pull-right\">\n" +
    "\n" +
    "        <a ng-disabled=\"hasSelectedLibrary(true) || isManagedByClouderaManager\"\n" +
    "           ng-if=\"isAuthorized([userRoles.admin])\"\n" +
    "           ng-click=\"hasSelectedLibrary(true) || onInstallSelectedLibrariesClick()\"\n" +
    "           tooltip-placement=\"bottom\"\n" +
    "           tooltip=\"{{('packageManager.header.install' | translate)  + ' (' + selectedStageLibraryList.length + ')'}}\"\n" +
    "           tooltip-popup-delay=\"500\"\n" +
    "           class=\"btn btn-link icon-button\">\n" +
    "          <i class=\"fa fa-plus-square fa-14x\"></i>\n" +
    "        </a>\n" +
    "\n" +
    "        <a ng-disabled=\"common.isSlaveNode || hasSelectedLibrary(false) || isManagedByClouderaManager\"\n" +
    "           ng-if=\"isAuthorized([userRoles.admin])\"\n" +
    "           ng-click=\"hasSelectedLibrary(false) || onUninstallSelectedLibrariesClick()\"\n" +
    "           tooltip-placement=\"bottom\"\n" +
    "           tooltip=\"{{('packageManager.header.uninstall' | translate)  + ' (' + selectedStageLibraryList.length + ')'}}\"\n" +
    "           tooltip-popup-delay=\"500\"\n" +
    "           class=\"btn btn-link icon-button\">\n" +
    "          <i class=\"fa fa-minus-square fa-14x\"></i>\n" +
    "        </a>\n" +
    "\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"pull-right\">\n" +
    "        <div class=\"btn-group pipeline-search\" >\n" +
    "          <input type=\"search\" class=\"form-control\" placeholder=\"Type to search\"\n" +
    "                 ng-model=\"header.searchInput\"\n" +
    "                 ng-change=\"updateStageLibraryList()\">\n" +
    "          <span class=\"glyphicon glyphicon-remove-circle search-clear\"\n" +
    "                ng-click=\"header.searchInput = '';updateStageLibraryList();\"></span>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"btn-group\" dropdown\n" +
    "             ng-if=\"isAuthorized([userRoles.admin])\"\n" +
    "             ng-hide=\"previewMode || snapshotMode\">\n" +
    "       <span class=\"btn btn-link dropdown-toggle icon-button\" dropdown-toggle aria-expanded=\"true\"\n" +
    "             tooltip-placement=\"bottom\" tooltip=\"{{'home.header.more' | translate}}\"\n" +
    "             tooltip-trigger=\"{{iconOnly ? 'mouseenter' : 'notooltip'}}\"\n" +
    "             tooltip-popup-delay=\"500\"\n" +
    "             ng-click=\"$event.stopPropagation();\">\n" +
    "          <i class=\"fa fa-ellipsis-h fa-14x\"></i>\n" +
    "        </span>\n" +
    "          <ul class=\"dropdown-menu pull-right\" role=\"menu\">\n" +
    "\n" +
    "            <li role=\"presentation\"\n" +
    "                ng-if=\"isAuthorized([userRoles.admin])\">\n" +
    "              <a href=\"#\" dropdown-toggle\n" +
    "                 ng-click=\"onCustomRepoURLClick()\">\n" +
    "                <i class=\"fa fa-globe\"></i>\n" +
    "                <span> {{'packageManager.header.customRepoUrl' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "          </ul>\n" +
    "        </div>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"selectedNavigationItem === extrasNavigationItem\">\n" +
    "      <div class=\"pull-right\">\n" +
    "\n" +
    "        <a ng-if=\"isAuthorized([userRoles.admin])\"\n" +
    "           ng-click=\"onUploadExtrasClick()\"\n" +
    "           tooltip-placement=\"bottom\"\n" +
    "           tooltip=\"{{'packageManager.uploadExtras.title' | translate}}\"\n" +
    "           tooltip-popup-delay=\"500\"\n" +
    "           class=\"btn btn-link icon-button\">\n" +
    "          <i class=\"fa fa-upload fa-14x\"></i>\n" +
    "        </a>\n" +
    "\n" +
    "        <a ng-if=\"isAuthorized([userRoles.admin])\"\n" +
    "           ng-disabled=\"common.isSlaveNode || !selectedStageLibraryList.length\"\n" +
    "           ng-click=\"selectedStageLibraryList.length === 0 || onDeleteExtrasClick()\"\n" +
    "           tooltip-placement=\"bottom\"\n" +
    "           tooltip=\"{{'global.form.delete' | translate}}\"\n" +
    "           tooltip-popup-delay=\"500\"\n" +
    "           class=\"btn btn-link icon-button\">\n" +
    "          <i class=\"fa fa-trash-o fa-14x\"></i>\n" +
    "        </a>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/packageManager/package_manager_list_view.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/packageManager/package_manager_list_view.tpl.html",
    "<section class=\"pipeline-list-view\">\n" +
    "  <div class=\"alert alert-info\"  ng-if=\"header.customRepoUrl\">\n" +
    "    <ul class=\"properties clearfix\">\n" +
    "      <li>\n" +
    "        <span class=\"properties-label\">{{'packageManager.customRepoUrl.repoUrl' | translate}}: </span>\n" +
    "        <span class=\"properties-value\">\n" +
    "                <a href=\"{{header.customRepoUrl}}\" target=\"_blank\">{{header.customRepoUrl}}</a>\n" +
    "              </span>\n" +
    "      </li>\n" +
    "    </ul>\n" +
    "  </div>\n" +
    "  <div class=\"list-content\">\n" +
    "    <ul class=\"list-group checked-list-box\">\n" +
    "\n" +
    "      <li class=\"list-group-item header-list-group-item\">\n" +
    "        <i ng-if=\"allSelected\"\n" +
    "           ng-click=\"unSelectAll()\"\n" +
    "           class=\"glyphicon glyphicon-check fa-12x header-check-box\"></i>\n" +
    "\n" +
    "        <i ng-if=\"!allSelected\"\n" +
    "           ng-click=\"selectAll()\"\n" +
    "           class=\"glyphicon glyphicon-unchecked fa-12x header-check-box\"></i>\n" +
    "\n" +
    "        <div class=\"stage-library-name header\">\n" +
    "          <a ng-click=\"onSortColumnHeaderClick('label')\">\n" +
    "            <span>Library Label</span>\n" +
    "            <i ng-if=\"header.sortColumn !== 'label'\" class=\"fa fa-sort\" aria-hidden=\"true\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'label' && !header.sortReverse\" class=\"fa fa-arrow-down\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'label' && header.sortReverse\" class=\"fa fa-arrow-up\"></i>\n" +
    "          </a>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"stage-library-id header\">\n" +
    "          <a ng-click=\"onSortColumnHeaderClick('id')\">\n" +
    "            <span>Library ID</span>\n" +
    "            <i ng-if=\"header.sortColumn !== 'id'\" class=\"fa fa-sort\" aria-hidden=\"true\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'id' && !header.sortReverse\" class=\"fa fa-arrow-down\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'id' && header.sortReverse\" class=\"fa fa-arrow-up\"></i>\n" +
    "          </a>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"stage-library-installed header\">\n" +
    "          <a ng-click=\"onSortColumnHeaderClick('installed')\">\n" +
    "            <span>Installed</span>\n" +
    "            <i ng-if=\"header.sortColumn !== 'installed'\" class=\"fa fa-sort\" aria-hidden=\"true\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'installed' && !header.sortReverse\" class=\"fa fa-arrow-down\"></i>\n" +
    "            <i ng-if=\"header.sortColumn === 'installed' && header.sortReverse\" class=\"fa fa-arrow-up\"></i>\n" +
    "          </a>\n" +
    "        </div>\n" +
    "\n" +
    "      </li>\n" +
    "\n" +
    "      <li class=\"list-group-item\"\n" +
    "          ng-class=\"{'active-info': selectedStageLibraryMap[stageLibrary.id]}\"\n" +
    "          ng-repeat=\"stageLibrary in filteredStageLibraries | orderBy: customStageLibrarySortFunction: header.sortReverse | limitTo: limit\">\n" +
    "\n" +
    "        <i ng-if=\"selectedStageLibraryMap[stageLibrary.id]\"\n" +
    "           ng-click=\"unSelectStageLibrary(stageLibrary)\"\n" +
    "           class=\"glyphicon glyphicon-check fa-12x\"></i>\n" +
    "\n" +
    "        <i ng-if=\"!selectedStageLibraryMap[stageLibrary.id]\"\n" +
    "           ng-click=\"selectStageLibrary(stageLibrary)\"\n" +
    "           class=\"glyphicon glyphicon-unchecked fa-12x\"></i>\n" +
    "\n" +
    "        <div class=\"stage-library-name\">\n" +
    "          <span>{{stageLibrary.label}}</span>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"stage-library-id\">\n" +
    "          <span>{{stageLibrary.id}}</span>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"stage-library-installed\">\n" +
    "          <i ng-if=\"stageLibrary.installed\"\n" +
    "             class=\"fa fa-check-circle fa-12x\"></i>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"action-menu pull-right\" dropdown>\n" +
    "              <span class=\"dropdown-toggle\" dropdown-toggle\n" +
    "                    ng-click=\"$event.stopPropagation();\">\n" +
    "                <i class=\"fa fa-ellipsis-v\"></i>\n" +
    "              </span>\n" +
    "          <ul class=\"dropdown-menu\" role=\"menu\">\n" +
    "\n" +
    "            <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin]) && !stageLibrary.installed && !isManagedByClouderaManager\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"onInstallLibraryClick(stageLibrary, $event)\">\n" +
    "                <i class=\"fa fa-plus-square\"></i>\n" +
    "                <span>{{'packageManager.header.install' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "            <li role=\"presentation\" ng-if=\"isAuthorized([userRoles.admin]) && stageLibrary.installed && !isManagedByClouderaManager\">\n" +
    "              <a href=\"#\" dropdown-toggle ng-click=\"onUninstallLibraryClick(stageLibrary, $event)\">\n" +
    "                <i class=\"fa fa-minus-square\"></i>\n" +
    "                <span>{{'packageManager.header.uninstall' | translate}}</span>\n" +
    "              </a>\n" +
    "            </li>\n" +
    "\n" +
    "          </ul>\n" +
    "        </div>\n" +
    "\n" +
    "      </li>\n" +
    "\n" +
    "      <li class=\"list-group-item center-align\" ng-if=\"filteredStageLibraries.length === 0 && !fetching\">\n" +
    "        <span translate=\"packageManager.noStageLibrary\"></span>\n" +
    "      </li>\n" +
    "\n" +
    "      <li class=\"list-group-item center-align\" ng-if=\"limit < filteredStageLibraries.length && !fetching\">\n" +
    "        <a class=\"btn btn-default\"\n" +
    "           ng-click=\"onShowMoreClick($event);\">{{'global.form.showMore' | translate}}</a>\n" +
    "      </li>\n" +
    "\n" +
    "\n" +
    "      <div class=\"stage-libraries-loading\">\n" +
    "        <div class=\"pipeline-home-loading\" show-loading=\"fetching\"></div>\n" +
    "      </div>\n" +
    "\n" +
    "\n" +
    "    </ul>\n" +
    "\n" +
    "  </div>\n" +
    "</section>\n" +
    "");
}]);

angular.module("app/home/packageManager/package_manager_side_nav.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/packageManager/package_manager_side_nav.tpl.html",
    "<div class=\"panel panel-default library-pane\">\n" +
    "  <div class=\"panel-body\">\n" +
    "    <ul class=\"list-group predefined-labels\">\n" +
    "\n" +
    "      <li ng-repeat=\"navigationItem in navigationItems\"\n" +
    "          class=\"list-group-item pointer clearfix\">\n" +
    "\n" +
    "        <div ng-click=\"onNavigationItemClick(navigationItem)\"\n" +
    "             ng-class=\"{'active' : selectedNavigationItem === navigationItem}\"\n" +
    "             class=\"pipeline-details-name list-group-item-wrapper\">{{navigationItem}}\n" +
    "        </div>\n" +
    "\n" +
    "      </li>\n" +
    "\n" +
    "      <li class=\"border-list-group-item\"></li>\n" +
    "\n" +
    "      <li class=\"list-group-item pointer clearfix\">\n" +
    "        <div ng-class=\"{'active' : selectedNavigationItem === extrasNavigationItem}\"\n" +
    "             ng-click=\"onNavigationItemClick(extrasNavigationItem)\"\n" +
    "             class=\"pipeline-details-name list-group-item-wrapper\">{{'packageManager.additionalDrivers' | translate}}\n" +
    "        </div>\n" +
    "      </li>\n" +
    "    </ul>\n" +
    "  </div>\n" +
    " </div>\n" +
    "");
}]);

angular.module("app/home/packageManager/uninstall/uninstall.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/packageManager/uninstall/uninstall.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <h3 class=\"modal-title\" translate=\"packageManager.uninstall.title\">Install Stage Library</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body package-manager-modal-body\">\n" +
    "  <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "  <div ng-if=\"!libraryUninstalled\">\n" +
    "    <p translate=\"packageManager.uninstall.confirmationMessage\"></p>\n" +
    "    <p class=\"stage-library-label\" ng-repeat=\"library in libraryList\">{{library.label}}</p>\n" +
    "  </div>\n" +
    "\n" +
    "  <div ng-if=\"libraryUninstalled && !isRestartInProgress\" class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "       translate=\"packageManager.uninstall.successMessage\">\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "       ng-show=\"isRestartInProgress\"\n" +
    "       translate=\"home.restart.successMessage\">\n" +
    "  </div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"button\" class=\"btn btn-default\" ng-click=\"cancel()\" ng-if=\"!operationInProgress\"\n" +
    "          translate=\"global.form.cancel\">Cancel</button>\n" +
    "  <button type=\"button\" class=\"btn btn-primary\" ng-click=\"install()\" ng-if=\"!operationInProgress && !libraryUninstalled\"\n" +
    "          translate=\"global.form.uninstall\">Uninstall</button>\n" +
    "  <button type=\"submit\" class=\"btn btn-primary\" ng-if=\"operationInProgress\" disabled\n" +
    "          translate=\"global.form.uninstalling\">Uninstalling...</button>\n" +
    "  <button ng-if=\"!operationInProgress && libraryUninstalled && !isRestartInProgress\"\n" +
    "          ng-click=\"restart()\"\n" +
    "          class=\"btn btn-primary\" translate=\"global.form.restart\">Restart</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/packageManager/upload_extras/uploadExtras.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/packageManager/upload_extras/uploadExtras.tpl.html",
    "<form class=\"import-modal-form\" role=\"form\" ng-submit=\"uploadExtras()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"packageManager.uploadExtras.title\">Install Additional Drivers</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div ng-if=\"operationStatus === 'incomplete'\">\n" +
    "      <div class=\"form-group\">\n" +
    "        <label class=\"control-label\" translate=\"packageManager.uploadExtras.library\">Library</label>\n" +
    "        <select class=\"form-control\"\n" +
    "                name=\"libraryId\"\n" +
    "                ng-model=\"libraryInfo.library\"\n" +
    "                ng-options=\"stageLibrary.label for stageLibrary in installedLibraries track by stageLibrary.id\">\n" +
    "        </select>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"input-group\">\n" +
    "      <span class=\"input-group-btn\">\n" +
    "          <span class=\"btn btn-primary btn-file\">\n" +
    "              {{'home.import.browse' | translate}}&hellip; <input type=\"file\" fileread=\"libraryInfo.uploadFile\">\n" +
    "          </span>\n" +
    "      </span>\n" +
    "        <input type=\"text\" class=\"form-control\" readonly ng-model=\"libraryInfo.uploadFile.name\"\n" +
    "               placeholder=\"{{'home.import.fileUploadPlaceholder' | translate}}\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"operationStatus === 'complete'\"\n" +
    "         class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "         translate=\"packageManager.uploadExtras.successMessage\">\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"operationStatus === 'restarting'\"\n" +
    "         class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "         translate=\"home.restart.successMessage\">\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\"\n" +
    "            ng-if=\"operationStatus === 'incomplete'\"\n" +
    "            translate=\"global.form.upload\">Upload</button>\n" +
    "\n" +
    "    <button type=\"button\" class=\"btn btn-primary\" disabled\n" +
    "            ng-if=\"operationStatus === 'uploading'\"\n" +
    "            translate=\"global.form.uploading\">Uploading...</button>\n" +
    "\n" +
    "    <button ng-click=\"restart()\" class=\"btn btn-primary\"\n" +
    "            ng-if=\"operationStatus === 'complete'\"\n" +
    "            translate=\"global.form.restart\">Restart</button>\n" +
    "\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/pipelineHome/pipelineHome.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/pipelineHome/pipelineHome.tpl.html",
    "<ng-include src=\"'app/home/alerts/error/errorModal.tpl.html'\"></ng-include>\n" +
    "\n" +
    "<div class=\"pipeline-home-loading\" show-loading=\"showLoading\"></div>\n" +
    "\n" +
    "<ng-include src=\"'app/home/header/header.tpl.html'\"></ng-include>\n" +
    "\n" +
    "<div class=\"home-bg-splitter\" ng-cloak\n" +
    "     ng-class=\"{'comfortable-density': $storage.displayDensity === pipelineConstant.DENSITY_COMFORTABLE,\n" +
    "                   'cozy-density': $storage.displayDensity === pipelineConstant.DENSITY_COZY,\n" +
    "                   'compact-density': $storage.displayDensity === pipelineConstant.DENSITY_COMPACT}\">\n" +
    "  <bg-splitter orientation=\"horizontal\" class=\"library-splitter\" ng-class=\"{'hide-library-panel': true}\">\n" +
    "\n" +
    "    <bg-pane min-size=\"0\">\n" +
    "      <!--ng-include src=\"'app/home/library/library.tpl.html'\"></ng-include-->\n" +
    "    </bg-pane>\n" +
    "\n" +
    "    <bg-pane min-size=\"0\">\n" +
    "\n" +
    "      <bg-splitter orientation=\"horizontal\" class=\"stage-library-splitter\"\n" +
    "                   ng-class=\"{'hide-stage-library-panel': !pipelineConfig || !$storage.hideStageLibraryPanel || $storage.maximizeDetailPane || isPipelineRunning || previewMode || snapshotMode}\"\n" +
    "                   ng-cloak>\n" +
    "\n" +
    "        <bg-pane min-size=\"0\">\n" +
    "          <bg-splitter orientation=\"vertical\"\n" +
    "                       ng-class=\"{'minimize-pane2': $storage.minimizeDetailPane, 'maximize-pane2': $storage.maximizeDetailPane}\">\n" +
    "\n" +
    "            <bg-pane min-size=\"0\">\n" +
    "              <ng-include src=\"'app/home/graph/graph.tpl.html'\"></ng-include>\n" +
    "            </bg-pane>\n" +
    "\n" +
    "            <bg-pane min-size=\"0\">\n" +
    "              <ng-include ng-if=\"pipelineConfig\" src=\"'app/home/detail/detail.tpl.html'\"></ng-include>\n" +
    "              <ng-include ng-if=\"pipelineConfig\" src=\"'app/home/preview/preview.tpl.html'\"></ng-include>\n" +
    "              <ng-include ng-if=\"pipelineConfig\" src=\"'app/home/snapshot/snapshot.tpl.html'\"></ng-include>\n" +
    "            </bg-pane>\n" +
    "          </bg-splitter>\n" +
    "        </bg-pane>\n" +
    "\n" +
    "        <bg-pane min-size=\"0\">\n" +
    "          <ng-include ng-if=\"executionMode\" src=\"'app/home/stageLibrary/stageLibrary.tpl.html'\"></ng-include>\n" +
    "        </bg-pane>\n" +
    "\n" +
    "      </bg-splitter>\n" +
    "\n" +
    "    </bg-pane>\n" +
    "\n" +
    "  </bg-splitter>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/preview/common/previewCommon.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/preview/common/previewCommon.tpl.html",
    "<div class=\"pipeline-preview\" ng-controller=\"PreviewCommonController\">\n" +
    "\n" +
    "  <div show-loading=\"showLoading\"></div>\n" +
    "\n" +
    "  <ng-include ng-if=\"listView\" src=\"'app/home/preview/common/previewCommonListView.tpl.html'\"></ng-include>\n" +
    "  <ng-include ng-if=\"!listView\" src=\"'app/home/preview/common/previewCommonTableView.tpl.html'\"></ng-include>\n" +
    "\n" +
    " </div>\n" +
    "");
}]);

angular.module("app/home/preview/common/previewCommonListView.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/preview/common/previewCommonListView.tpl.html",
    "<table class=\"table preview-table\" ng-hide=\"showLoading\">\n" +
    "  <thead>\n" +
    "    <tr>\n" +
    "      <th class=\"col-md-6\">\n" +
    "        <span class=\"glyphicon glyphicon-expand pointer\"\n" +
    "              ng-if=\"!expandAllInputData\"\n" +
    "              ng-click=\"onExpandAllInputData(stagePreviewData)\"></span>\n" +
    "        <span class=\"glyphicon glyphicon-collapse-down pointer\"\n" +
    "              ng-if=\"expandAllInputData\"\n" +
    "              ng-click=\"onCollapseAllInputData(stagePreviewData)\"></span>\n" +
    "        <span translate=\"home.previewPane.inputData\">Input Data</span>\n" +
    "      </th>\n" +
    "\n" +
    "      <th class=\"col-md-6\">\n" +
    "        <span class=\"glyphicon glyphicon-expand pointer\"\n" +
    "              ng-if=\"!expandAllOutputData\"\n" +
    "              ng-click=\"onExpandAllOutputData(stagePreviewData)\"></span>\n" +
    "        <span class=\"glyphicon glyphicon-collapse-down pointer\"\n" +
    "              ng-if=\"expandAllOutputData\"\n" +
    "              ng-click=\"onCollapseAllOutputData(stagePreviewData)\"></span>\n" +
    "        <span translate=\"home.previewPane.outputData\">Output Data</span>\n" +
    "      </th>\n" +
    "    </tr>\n" +
    "  </thead>\n" +
    "  <tbody ng-switch=\"detailPaneConfig.uiInfo.stageType\">\n" +
    "\n" +
    "    <tr ng-switch-when=\"SOURCE\">\n" +
    "      <td>\n" +
    "        <div ui-codemirror\n" +
    "             class=\"codemirror-editor\"\n" +
    "             ng-if=\"rawDataConfigIndex\"\n" +
    "             ng-model=\"detailPaneConfig.configuration[rawDataConfigIndex].value\"\n" +
    "             ui-refresh=\"refreshCodemirror\"\n" +
    "             ui-codemirror-opts=\"rawDataCodemirrorOptions\">\n" +
    "        </div>\n" +
    "      </td>\n" +
    "\n" +
    "      <td>\n" +
    "        <table class=\"table preview-table\">\n" +
    "          <tbody>\n" +
    "          <tr ng-repeat=\"record in stagePreviewData.output | limitTo: recordPagination.outputRecords\">\n" +
    "            <td>\n" +
    "              <div class=\"pull-right delete-preview-record\"\n" +
    "                   ng-if=\"previewMode\"\n" +
    "                   ng-click=\"removeRecord(detailPaneConfig, stagePreviewData.output, record, $index)\">\n" +
    "                <i class=\"fa fa-trash\"></i>\n" +
    "              </div>\n" +
    "\n" +
    "              <record-tree\n" +
    "                record=\"record\"\n" +
    "                record-value=\"record.value\"\n" +
    "                field-name=\"('global.form.record' | translate)+($index+1)\"\n" +
    "                is-root=\"true\"\n" +
    "                editable=\"previewMode\"\n" +
    "                show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "                show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "              </record-tree>\n" +
    "\n" +
    "              <div class=\"show-more\" ng-if=\"$last && recordPagination.outputRecords < stagePreviewData.output.length\">\n" +
    "                <a class=\"btn btn-link\"\n" +
    "                   ng-click=\"recordPagination.outputRecords = recordPagination.outputRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "                <span class=\"separator\">|</span>\n" +
    "\n" +
    "                <a class=\"btn btn-link\"\n" +
    "                   ng-click=\"recordPagination.outputRecords = stagePreviewData.output.length;\">Show All ({{stagePreviewData.output.length}}) ...</a>\n" +
    "              </div>\n" +
    "            </td>\n" +
    "          </tr>\n" +
    "\n" +
    "          <tr ng-repeat=\"record in stagePreviewData.eventRecords | limitTo: recordPagination.eventRecords\">\n" +
    "            <td>\n" +
    "              <record-tree\n" +
    "                record=\"record\"\n" +
    "                record-value=\"record.value\"\n" +
    "                field-name=\"('global.form.eventRecord' | translate) + ($index+1) + ' (' + record.header.values['sdc.event.type'] + ')'\"\n" +
    "                is-root=\"true\"\n" +
    "                editable=\"previewMode\"\n" +
    "                show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "                show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "              </record-tree>\n" +
    "\n" +
    "              <div class=\"show-more\" ng-if=\"$last && recordPagination.eventRecords < stagePreviewData.eventRecords.length\">\n" +
    "                <a class=\"btn btn-link\"\n" +
    "                   ng-click=\"recordPagination.eventRecords = recordPagination.eventRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "                <span class=\"separator\">|</span>\n" +
    "\n" +
    "                <a class=\"btn btn-link\"\n" +
    "                   ng-click=\"recordPagination.eventRecords = stagePreviewData.eventRecords.length;\">Show All ({{stagePreviewData.eventRecords.length}}) ...</a>\n" +
    "              </div>\n" +
    "            </td>\n" +
    "          </tr>\n" +
    "\n" +
    "          <tr ng-repeat=\"record in stagePreviewData.errorRecords | limitTo: recordPagination.errorRecords\">\n" +
    "            <td>\n" +
    "              <record-tree\n" +
    "                record=\"record\"\n" +
    "                record-value=\"record.value\"\n" +
    "                field-name=\"('global.form.record' | translate)+($parent.$index+1) + '-' + ('global.form.errorRecord' | translate) + ' ' + escapeHtml(record.header.errorMessage)\"\n" +
    "                is-root=\"true\"\n" +
    "                is-error=\"true\"\n" +
    "                editable=\"false\"\n" +
    "                show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "                show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "              </record-tree>\n" +
    "\n" +
    "              <div class=\"show-more\" ng-if=\"$last && recordPagination.errorRecords < stagePreviewData.errorRecords.length\">\n" +
    "                <a class=\"btn btn-link\"\n" +
    "                   ng-click=\"recordPagination.errorRecords = recordPagination.errorRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "                <span class=\"separator\">|</span>\n" +
    "\n" +
    "                <a class=\"btn btn-link\"\n" +
    "                   ng-click=\"recordPagination.errorRecords = stagePreviewData.errorRecords.length;\">Show All ({{stagePreviewData.errorRecords.length}}) ...</a>\n" +
    "              </div>\n" +
    "            </td>\n" +
    "          </tr>\n" +
    "\n" +
    "          </tbody>\n" +
    "        </table>\n" +
    "      </td>\n" +
    "    </tr>\n" +
    "\n" +
    "    <tr ng-switch-when=\"PROCESSOR\"\n" +
    "        ng-repeat=\"inputRecord in stagePreviewData.input | limitTo: recordPagination.inputRecords\"\n" +
    "        ng-init=\"outputRecords = getOutputRecords(stagePreviewData.output, inputRecord)\">\n" +
    "      <td>\n" +
    "        <record-tree\n" +
    "          record=\"inputRecord\"\n" +
    "          record-value=\"inputRecord.value\"\n" +
    "          diff-type=\"'input'\"\n" +
    "          diff-record=\"outputRecords\"\n" +
    "          diff-record-value=\"outputRecords[0].value\"\n" +
    "          field-name=\"('global.form.record' | translate)+($index+1)\"\n" +
    "          is-root=\"true\"\n" +
    "          editable=\"previewMode\"\n" +
    "          show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "          show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "        </record-tree>\n" +
    "\n" +
    "        <div class=\"show-more\" ng-if=\"$last && recordPagination.inputRecords < stagePreviewData.input.length\">\n" +
    "          <a class=\"btn btn-link\"\n" +
    "             ng-click=\"recordPagination.inputRecords = recordPagination.inputRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "          <span class=\"separator\">|</span>\n" +
    "\n" +
    "          <a class=\"btn btn-link\"\n" +
    "             ng-click=\"recordPagination.inputRecords = stagePreviewData.input.length;\">Show All ({{stagePreviewData.input.length}}) ...</a>\n" +
    "        </div>\n" +
    "      </td>\n" +
    "      <td>\n" +
    "        <div ng-repeat=\"record in outputRecords | limitTo: recordPagination.outputRecords\">\n" +
    "          <record-tree\n" +
    "            record=\"record\"\n" +
    "            record-value=\"record.value\"\n" +
    "            diff-type=\"'output'\"\n" +
    "            diff-record=\"inputRecord\"\n" +
    "            diff-record-value=\"inputRecord.value\"\n" +
    "            field-name=\"('global.form.record' | translate)+($parent.$index+1) + '-' + ('global.form.outputRecord' | translate) +  ($index + 1) + getRecordAdditionalInfo(detailPaneConfig, record, 'output')\"\n" +
    "            is-root=\"true\"\n" +
    "            editable=\"false\"\n" +
    "            show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "            show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "          </record-tree>\n" +
    "\n" +
    "          <div class=\"show-more\" ng-if=\"$last && recordPagination.outputRecords < outputRecords.length\">\n" +
    "            <a class=\"btn btn-link\"\n" +
    "               ng-click=\"recordPagination.outputRecords = recordPagination.outputRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "            <span class=\"separator\">|</span>\n" +
    "\n" +
    "            <a class=\"btn btn-link\"\n" +
    "               ng-click=\"recordPagination.outputRecords = outputRecords.length;\">Show All ({{outputRecords.length}}) ...</a>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div ng-repeat=\"record in errorsRecords = getErrorRecords(stagePreviewData.errorRecords, inputRecord)\">\n" +
    "          <record-tree\n" +
    "            record=\"record\"\n" +
    "            record-value=\"record.value\"\n" +
    "            field-name=\"('global.form.record' | translate)+($parent.$index+1) + '-' + ('global.form.errorRecord' | translate) + ' ' + escapeHtml(record.header.errorMessage)\"\n" +
    "            is-root=\"true\"\n" +
    "            is-error=\"true\"\n" +
    "            editable=\"false\"\n" +
    "            show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "            show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "          </record-tree>\n" +
    "        </div>\n" +
    "\n" +
    "        <div ng-if=\"outputRecords.length === 0 && errorsRecords.length === 0\">\n" +
    "          <label class=\"label label-warning\">{{'home.previewPane.noOutputWarning' | translate}}</label>\n" +
    "        </div>\n" +
    "\n" +
    "      </td>\n" +
    "    </tr>\n" +
    "\n" +
    "    <tr ng-if=\"detailPaneConfig.uiInfo.stageType === 'PROCESSOR' && stagePreviewData.newRecords && stagePreviewData.newRecords.length\"\n" +
    "        ng-repeat=\"newRecord in stagePreviewData.newRecords | limitTo: recordPagination.newRecords\">\n" +
    "      <td>\n" +
    "      </td>\n" +
    "      <td>\n" +
    "        <div>\n" +
    "          <record-tree\n" +
    "            record=\"newRecord\"\n" +
    "            record-value=\"newRecord.value\"\n" +
    "            field-name=\"('global.form.newRecord' | translate) +  ($index + 1) + getRecordAdditionalInfo(detailPaneConfig, newRecord, 'output')\"\n" +
    "            is-root=\"true\"\n" +
    "            editable=\"false\"\n" +
    "            show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "            show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "          </record-tree>\n" +
    "\n" +
    "          <div class=\"show-more\" ng-if=\"$last && recordPagination.newRecords < stagePreviewData.newRecords.length\">\n" +
    "            <a class=\"btn btn-link\"\n" +
    "               ng-click=\"recordPagination.newRecords = recordPagination.newRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "            <span class=\"separator\">|</span>\n" +
    "\n" +
    "            <a class=\"btn btn-link\"\n" +
    "               ng-click=\"recordPagination.newRecords = stagePreviewData.newRecords.length;\">Show All ({{stagePreviewData.newRecords.length}}) ...</a>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "      </td>\n" +
    "    </tr>\n" +
    "\n" +
    "    <tr ng-if=\"detailPaneConfig.uiInfo.stageType === pipelineConstant.TARGET_STAGE_TYPE || detailPaneConfig.uiInfo.stageType === pipelineConstant.EXECUTOR_STAGE_TYPE\"\n" +
    "        ng-repeat=\"inputRecord in stagePreviewData.input | limitTo: recordPagination.inputRecords\">\n" +
    "      <td>\n" +
    "        <record-tree\n" +
    "          record=\"inputRecord\"\n" +
    "          record-value=\"inputRecord.value\"\n" +
    "          field-name=\"('global.form.record' | translate)+($index+1)\"\n" +
    "          is-root=\"true\"\n" +
    "          editable=\"false\"\n" +
    "          show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "          show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "        </record-tree>\n" +
    "\n" +
    "        <div class=\"show-more\" ng-if=\"$last && recordPagination.inputRecords < stagePreviewData.input.length\">\n" +
    "          <a class=\"btn btn-link\"\n" +
    "             ng-click=\"recordPagination.inputRecords = recordPagination.inputRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "          <span class=\"separator\">|</span>\n" +
    "\n" +
    "          <a class=\"btn btn-link\"\n" +
    "             ng-click=\"recordPagination.inputRecords = stagePreviewData.input.length;\">Show All ({{stagePreviewData.input.length}}) ...</a>\n" +
    "        </div>\n" +
    "      </td>\n" +
    "      <td>\n" +
    "        <div ng-repeat=\"record in errorsRecords = getErrorRecords(stagePreviewData.errorRecords, inputRecord)\">\n" +
    "          <record-tree\n" +
    "            record=\"record\"\n" +
    "            record-value=\"record.value\"\n" +
    "            field-name=\"('global.form.record' | translate)+($parent.$index+1) + '-' + ('global.form.errorRecord' | translate) + ' ' + escapeHtml(record.header.errorMessage)\"\n" +
    "            is-root=\"true\"\n" +
    "            is-error=\"true\"\n" +
    "            editable=\"false\"\n" +
    "            show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "            show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "          </record-tree>\n" +
    "        </div>\n" +
    "      </td>\n" +
    "    </tr>\n" +
    "\n" +
    "    <tr ng-if=\"detailPaneConfig.uiInfo.stageType === pipelineConstant.TARGET_STAGE_TYPE || detailPaneConfig.uiInfo.stageType === pipelineConstant.EXECUTOR_STAGE_TYPE || detailPaneConfig.uiInfo.stageType === pipelineConstant.PROCESSOR_STAGE_TYPE\"\n" +
    "        ng-repeat=\"record in stagePreviewData.eventRecords | limitTo: recordPagination.eventRecords\">\n" +
    "      <td>\n" +
    "      </td>\n" +
    "      <td>\n" +
    "        <record-tree\n" +
    "          record=\"record\"\n" +
    "          record-value=\"record.value\"\n" +
    "          field-name=\"('global.form.eventRecord' | translate) + ($index+1) + ' (' + record.header.values['sdc.event.type'] + ')'\"\n" +
    "          is-root=\"true\"\n" +
    "          editable=\"previewMode\"\n" +
    "          show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "          show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "        </record-tree>\n" +
    "\n" +
    "        <div class=\"show-more\" ng-if=\"$last && recordPagination.eventRecords < stagePreviewData.eventRecords.length\">\n" +
    "          <a class=\"btn btn-link\"\n" +
    "             ng-click=\"recordPagination.eventRecords = recordPagination.eventRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "          <span class=\"separator\">|</span>\n" +
    "\n" +
    "          <a class=\"btn btn-link\"\n" +
    "             ng-click=\"recordPagination.eventRecords = stagePreviewData.eventRecords.length;\">Show All ({{stagePreviewData.eventRecords.length}}) ...</a>\n" +
    "        </div>\n" +
    "      </td>\n" +
    "    </tr>\n" +
    "\n" +
    "    <tr ng-if=\"stagePreviewData.input.length === 0 &&\n" +
    "                stagePreviewData.output.length === 0 &&\n" +
    "                stagePreviewData.errorRecords.length === 0 &&\n" +
    "                (!stagePreviewData.eventRecords || stagePreviewData.eventRecords.length === 0)\">\n" +
    "      <td colspan=\"2\" class=\"no-records\">\n" +
    "        {{'home.previewPane.noRecords' | translate}}\n" +
    "\n" +
    "        <div ng-if=\"stagePreviewData.stageErrors && stagePreviewData.stageErrors.length\">\n" +
    "          <span translate=\"home.previewPane.stageErrorsWarning\"\n" +
    "                translate-values=\"{stageErrorsCount: stagePreviewData.stageErrors.length}\"></span>\n" +
    "        </div>\n" +
    "\n" +
    "      </td>\n" +
    "    </tr>\n" +
    "  </tbody>\n" +
    "\n" +
    "</table>\n" +
    "");
}]);

angular.module("app/home/preview/common/previewCommonTableView.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/preview/common/previewCommonTableView.tpl.html",
    "<table class=\"table table-bordered table-striped preview-tableview-table\" ng-hide=\"showLoading\" ng-controller=\"PreviewCommonTableViewController\">\n" +
    "  <thead>\n" +
    "  <tr>\n" +
    "    <th class=\"from-stage\" ng-if=\"inputFieldPaths.length || detailPaneConfig.uiInfo.stageType === pipelineConstant.PROCESSOR_STAGE_TYPE\"\n" +
    "        colspan=\"{{inputLimit ? inputLimit : 1}}\">\n" +
    "      <span translate=\"home.previewPane.inputData\">Input Data</span>\n" +
    "    </th>\n" +
    "\n" +
    "    <th class=\"to-stage\" colspan=\"{{outputLimit ? outputLimit : 1}}\">\n" +
    "      <span translate=\"home.previewPane.outputData\">Output Data</span>\n" +
    "    </th>\n" +
    "\n" +
    "  </tr>\n" +
    "\n" +
    "  <tr>\n" +
    "    <th class=\"from-stage\" ng-repeat=\"fieldPath in inputFieldPaths | limitTo: inputLimit\">\n" +
    "      <span>{{fieldPath}}</span>\n" +
    "\n" +
    "      <div class=\"show-more pull-right\" ng-if=\"$last && inputLimit < inputFieldPaths.length\">\n" +
    "        (<a href=\"btn btn-link\" href=\"\"\n" +
    "           ng-click=\"onShowMoreInputClick($event);\">Show More...</a>\n" +
    "\n" +
    "        <span class=\"separator\">|</span>\n" +
    "\n" +
    "        <a href=\"btn btn-link\" href=\"\"\n" +
    "           ng-click=\"onShowAllInputClick($event);\">Show All...</a>)\n" +
    "      </div>\n" +
    "\n" +
    "    </th>\n" +
    "\n" +
    "    <th class=\"to-stage\" ng-repeat=\"fieldPath in outputFieldPaths | limitTo: outputLimit\">\n" +
    "      <span>{{fieldPath}}</span>\n" +
    "\n" +
    "      <div class=\"show-more pull-right\" ng-if=\"$last && outputLimit < outputFieldPaths.length\">\n" +
    "        (<a href=\"btn btn-link\" href=\"\"\n" +
    "           ng-click=\"onShowMoreOutputClick($event);\">Show More...</a>\n" +
    "\n" +
    "        <span class=\"separator\">|</span>\n" +
    "\n" +
    "        <a href=\"btn btn-link\" href=\"\"\n" +
    "           ng-click=\"onShowAllOutputClick($event);\">Show All...</a>)\n" +
    "      </div>\n" +
    "\n" +
    "    </th>\n" +
    "  </tr>\n" +
    "\n" +
    "  </thead>\n" +
    "  <tbody>\n" +
    "\n" +
    "  <tr ng-repeat-start=\"inputRecord in stagePreviewData.input\"\n" +
    "      ng-init=\"\n" +
    "      outputRecords = getOutputRecords(stagePreviewData.output, inputRecord);\n" +
    "      errorsRecords = getErrorRecords(stagePreviewData.errorRecords, inputRecord);\n" +
    "      flattenInputRecord = getFlattenRecord(inputRecord);\n" +
    "      flattenOutputRecord = getFlattenRecord(outputRecords[0])\">\n" +
    "\n" +
    "    <!--Input -->\n" +
    "\n" +
    "    <td ng-repeat=\"fieldPath in inputFieldPaths | limitTo: inputLimit\"\n" +
    "        ng-switch=\"flattenInputRecord[fieldPath].type\"\n" +
    "        ng-class=\"{\n" +
    "          'value-deletion': detailPaneConfig.uiInfo.stageType === pipelineConstant.PROCESSOR_STAGE_TYPE &&\n" +
    "              (flattenInputRecord[fieldPath].value !== flattenOutputRecord[fieldPath].value ||\n" +
    "              flattenInputRecord[fieldPath].type !== flattenOutputRecord[fieldPath].type)\n" +
    "          }\">\n" +
    "\n" +
    "      <span class=\"pull-right glyphicon glyphicon-asterisk dirty-icon\" ng-if=\"flattenInputRecord[fieldPath].dirty\"></span>\n" +
    "\n" +
    "\n" +
    "      <div ng-switch-when=\"DATETIME\" class=\"clearfix\">\n" +
    "\n" +
    "        <span class=\"field-value pull-left {{flattenInputRecord[fieldPath].type}}\"\n" +
    "              ng-class=\"{'value-addition' : (diffType === 'output' && updatedValue), 'value-deletion' : (diffType === 'input' && updatedValue)}\"\n" +
    "          >{{flattenInputRecord[fieldPath].value + '' | date:'medium'}} </span>\n" +
    "\n" +
    "        <div class=\"dropdown pull-left\" ng-show=\"previewMode\"\n" +
    "             ng-init=\"dateRecordValue = flattenInputRecord[fieldPath].value; dropdownSelector = 'dropdownSelector' + $parent.$index + $parent.$parent.$index\">\n" +
    "          <a class=\"record-date-toggle dropdown-toggle {{dropdownSelector}}\" role=\"button\" data-toggle=\"dropdown\" data-target=\"#\" href=\"\">\n" +
    "            <i class=\"fa fa-calendar\"></i>\n" +
    "          </a>\n" +
    "          <ul class=\"dropdown-menu\" role=\"menu\" aria-labelledby=\"dLabel\">\n" +
    "            <datetimepicker data-ng-model=\"dateRecordValue\"\n" +
    "                            data-on-set-time=\"recordDateValueUpdated(inputRecord, flattenInputRecord[fieldPath], dateRecordValue)\"\n" +
    "                            data-datetimepicker-config=\"{ dropdownSelector: '.' + dropdownSelector }\"></datetimepicker>\n" +
    "          </ul>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "      <div ng-switch-when=\"DATE\" class=\"clearfix\">\n" +
    "\n" +
    "        <span class=\"field-value pull-left {{flattenInputRecord[fieldPath].type}}\"\n" +
    "              ng-class=\"{'value-addition' : (diffType === 'output' && updatedValue), 'value-deletion' : (diffType === 'input' && updatedValue)}\"\n" +
    "        >{{flattenInputRecord[fieldPath].value + '' | date:'mediumDate'}} </span>\n" +
    "\n" +
    "        <div class=\"dropdown pull-left\" ng-show=\"previewMode\"\n" +
    "             ng-init=\"dateRecordValue = flattenInputRecord[fieldPath].value; dropdownSelector = 'dropdownSelector' + $parent.$index + $parent.$parent.$index\">\n" +
    "          <a class=\"record-date-toggle dropdown-toggle {{dropdownSelector}}\" role=\"button\" data-toggle=\"dropdown\" data-target=\"#\" href=\"\">\n" +
    "            <i class=\"fa fa-calendar\"></i>\n" +
    "          </a>\n" +
    "          <ul class=\"dropdown-menu\" role=\"menu\" aria-labelledby=\"dLabel\">\n" +
    "            <datetimepicker data-ng-model=\"dateRecordValue\"\n" +
    "                            data-on-set-time=\"recordDateValueUpdated(inputRecord, flattenInputRecord[fieldPath], dateRecordValue)\"\n" +
    "                            data-datetimepicker-config=\"{ dropdownSelector: '.' + dropdownSelector, minView: 'day' }\"></datetimepicker>\n" +
    "          </ul>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "      <div ng-switch-when=\"TIME\" class=\"clearfix\">\n" +
    "\n" +
    "        <span class=\"field-value pull-left {{flattenInputRecord[fieldPath].type}}\"\n" +
    "              ng-class=\"{'value-addition' : (diffType === 'output' && updatedValue), 'value-deletion' : (diffType === 'input' && updatedValue)}\"\n" +
    "        >{{flattenInputRecord[fieldPath].value + '' | date:'mediumTime'}} </span>\n" +
    "\n" +
    "        <div class=\"dropdown pull-left\" ng-show=\"previewMode\"\n" +
    "             ng-init=\"dateRecordValue = flattenInputRecord[fieldPath].value; dropdownSelector = 'dropdownSelector' + $parent.$index + $parent.$parent.$index\">\n" +
    "          <a class=\"record-date-toggle dropdown-toggle {{dropdownSelector}}\" role=\"button\" data-toggle=\"dropdown\" data-target=\"#\" href=\"\">\n" +
    "            <i class=\"fa fa-calendar\"></i>\n" +
    "          </a>\n" +
    "          <ul class=\"dropdown-menu\" role=\"menu\" aria-labelledby=\"dLabel\">\n" +
    "            <datetimepicker data-ng-model=\"dateRecordValue\"\n" +
    "                            data-on-set-time=\"recordDateValueUpdated(inputRecord, flattenInputRecord[fieldPath], dateRecordValue)\"\n" +
    "                            data-datetimepicker-config=\"{ dropdownSelector: '.' + dropdownSelector, startView: 'hour' }\"></datetimepicker>\n" +
    "          </ul>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "      <div ng-switch-default\n" +
    "           contenteditable=\"{{previewMode}}\"\n" +
    "           ng-model=\"flattenInputRecord[fieldPath].value\"\n" +
    "           ng-change=\"recordValueUpdated(inputRecord, flattenInputRecord[fieldPath])\"\n" +
    "           class=\"field-value {{flattenInputRecord[fieldPath].value != null ? flattenInputRecord[fieldPath].type : ''}}\">\n" +
    "      </div>\n" +
    "\n" +
    "    </td>\n" +
    "\n" +
    "\n" +
    "    <!--Output -->\n" +
    "\n" +
    "    <td ng-if=\"flattenOutputRecord\"\n" +
    "        ng-repeat=\"fieldPath in outputFieldPaths | limitTo: outputLimit\"\n" +
    "        ng-switch=\"flattenOutputRecord[fieldPath].type\"\n" +
    "        ng-class=\"{\n" +
    "          'value-addition': flattenInputRecord[fieldPath].value !== flattenOutputRecord[fieldPath].value ||\n" +
    "          flattenInputRecord[fieldPath].type !== flattenOutputRecord[fieldPath].type\n" +
    "          }\">\n" +
    "\n" +
    "      <span class=\"number-circle\" ng-if=\"$index === 0 && laneMap\"\n" +
    "        tooltip=\"{{('home.previewPane.streamCondition' | translate ) + ': ' + laneMap[outputRecords[0].laneName].condition}}\">{{laneMap[outputRecords[0].laneName].index}}</span>\n" +
    "\n" +
    "      <span ng-switch-when=\"DATETIME\"\n" +
    "            class=\"field-value {{flattenOutputRecord[fieldPath].value != null ? flattenOutputRecord[fieldPath].type : ''}}\">{{(flattenOutputRecord[fieldPath].value + '' | date:'medium')}}</span>\n" +
    "      <span ng-switch-when=\"DATE\"\n" +
    "            class=\"field-value {{flattenOutputRecord[fieldPath].value != null ? flattenOutputRecord[fieldPath].type : ''}}\">{{(flattenOutputRecord[fieldPath].value + '' | date:'mediumDate')}}</span>\n" +
    "      <span ng-switch-when=\"TIME\"\n" +
    "            class=\"field-value {{flattenOutputRecord[fieldPath].value != null ? flattenOutputRecord[fieldPath].type : ''}}\">{{(flattenOutputRecord[fieldPath].value + '' | date:'mediumTime')}}</span>\n" +
    "      <span ng-switch-default\n" +
    "            class=\"field-value {{flattenOutputRecord[fieldPath].value != null ? flattenOutputRecord[fieldPath].type : ''}}\">{{(flattenOutputRecord[fieldPath].value + '')}}</span>\n" +
    "\n" +
    "    </td>\n" +
    "\n" +
    "    <td colspan=\"{{outputLimit}}\" ng-if=\"errorsRecords && errorsRecords.length > 0\">\n" +
    "      <label class=\"label label-danger\">\n" +
    "        {{('global.form.record' | translate)+($parent.$index+1) + '-' + ('global.form.errorRecord' | translate) + ' ' +\n" +
    "        escapeHtml(errorsRecords[0].header.errorMessage)}}\n" +
    "      </label>\n" +
    "    </td>\n" +
    "\n" +
    "    <td colspan=\"{{outputLimit}}\"\n" +
    "        ng-if=\"outputRecords.length === 0 && errorsRecords.length === 0 && detailPaneConfig.uiInfo.stageType !== pipelineConstant.TARGET_STAGE_TYPE && detailPaneConfig.uiInfo.stageType !== pipelineConstant.EXECUTOR_STAGE_TYPE\">\n" +
    "      <label class=\"label label-warning\">{{'home.previewPane.noOutputWarning' | translate}}</label>\n" +
    "    </td>\n" +
    "\n" +
    "  </tr>\n" +
    "\n" +
    "  <tr ng-repeat-end\n" +
    "      ng-repeat=\"outputRecord in outputRecords\"\n" +
    "      ng-if=\"$index > 0\"\n" +
    "      ng-init=\"flattenRecord = getFlattenRecord(outputRecord)\">\n" +
    "    <td colspan=\"{{inputLimit}}\"></td>\n" +
    "\n" +
    "    <td ng-repeat=\"fieldPath in outputFieldPaths | limitTo: outputLimit\"\n" +
    "        ng-switch=\"flattenRecord[fieldPath].type\">\n" +
    "      <span ng-switch-when=\"DATETIME\"\n" +
    "        class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\">{{(flattenRecord[fieldPath].value + '' | date:'medium')}}</span>\n" +
    "      <span ng-switch-when=\"DATE\"\n" +
    "            class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\">{{(flattenRecord[fieldPath].value + '' | date:'mediumDate')}}</span>\n" +
    "      <span ng-switch-when=\"TIME\"\n" +
    "            class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\">{{(flattenRecord[fieldPath].value + '' | date:'mediumTime')}}</span>\n" +
    "      <span ng-switch-default\n" +
    "        class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\">{{(flattenRecord[fieldPath].value + '')}}</span>\n" +
    "    </td>\n" +
    "  </tr>\n" +
    "\n" +
    "  <tr ng-if=\"detailPaneConfig.uiInfo.stageType === pipelineConstant.SOURCE_STAGE_TYPE\"\n" +
    "      ng-repeat=\"record in stagePreviewData.output\"\n" +
    "      ng-init=\"flattenRecord = getFlattenRecord(record)\">\n" +
    "    <td ng-repeat=\"fieldPath in inputFieldPaths | limitTo: inputLimit\"\n" +
    "        class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\"\n" +
    "      >{{flattenRecord[fieldPath].value + ''}}</td>\n" +
    "    <td ng-repeat=\"fieldPath in outputFieldPaths | limitTo: outputLimit\"\n" +
    "        ng-switch=\"flattenRecord[fieldPath].type\">\n" +
    "      <span ng-switch-when=\"DATETIME\"\n" +
    "            class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\">{{(flattenRecord[fieldPath].value + '' | date:'medium')}}</span>\n" +
    "      <span ng-switch-when=\"DATE\"\n" +
    "            class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\">{{(flattenRecord[fieldPath].value + '' | date:'mediumDate')}}</span>\n" +
    "      <span ng-switch-when=\"TIME\"\n" +
    "            class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\">{{(flattenRecord[fieldPath].value + '' | date:'mediumTime')}}</span>\n" +
    "      <span ng-switch-default\n" +
    "            class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\">{{(flattenRecord[fieldPath].value + '')}}</span>\n" +
    "    </td>\n" +
    "  </tr>\n" +
    "\n" +
    "\n" +
    "  <tr ng-if=\"detailPaneConfig.uiInfo.stageType === 'PROCESSOR' && stagePreviewData.newRecords && stagePreviewData.newRecords.length\"\n" +
    "      ng-repeat=\"outputRecord in stagePreviewData.newRecords\"\n" +
    "      ng-init=\"flattenRecord = getFlattenRecord(outputRecord)\">\n" +
    "    <td colspan=\"{{inputLimit}}\"></td>\n" +
    "\n" +
    "    <td ng-repeat=\"fieldPath in outputFieldPaths | limitTo: outputLimit\"\n" +
    "        ng-switch=\"flattenRecord[fieldPath].type\">\n" +
    "      <span ng-switch-when=\"DATETIME\"\n" +
    "            class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\">{{(flattenRecord[fieldPath].value + '' | date:'medium')}}</span>\n" +
    "      <span ng-switch-when=\"DATE\"\n" +
    "            class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\">{{(flattenRecord[fieldPath].value + '' | date:'mediumDate')}}</span>\n" +
    "      <span ng-switch-when=\"TIME\"\n" +
    "            class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\">{{(flattenRecord[fieldPath].value + '' | date:'mediumTime')}}</span>\n" +
    "      <span ng-switch-default\n" +
    "            class=\"field-value {{flattenRecord[fieldPath].value != null ? flattenRecord[fieldPath].type : ''}}\">{{(flattenRecord[fieldPath].value + '')}}</span>\n" +
    "    </td>\n" +
    "  </tr>\n" +
    "\n" +
    "\n" +
    "\n" +
    "  <tr ng-if=\"stagePreviewData.input.length === 0 &&\n" +
    "                stagePreviewData.output.length === 0 &&\n" +
    "                stagePreviewData.errorRecords.length === 0\">\n" +
    "    <td colspan=\"2\" class=\"no-records\">\n" +
    "      {{'home.previewPane.noRecords' | translate}}\n" +
    "\n" +
    "      <div ng-if=\"stagePreviewData.stageErrors && stagePreviewData.stageErrors.length\">\n" +
    "          <span translate=\"home.previewPane.stageErrorsWarning\"\n" +
    "                translate-values=\"{stageErrorsCount: stagePreviewData.stageErrors.length}\"></span>\n" +
    "      </div>\n" +
    "\n" +
    "    </td>\n" +
    "  </tr>\n" +
    "  </tbody>\n" +
    "\n" +
    "</table>\n" +
    "");
}]);

angular.module("app/home/preview/common/previewMultiStage.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/preview/common/previewMultiStage.tpl.html",
    "<div class=\"pipeline-preview\" ng-controller=\"PreviewMultiStageController\">\n" +
    "  <div show-loading=\"showLoading\"></div>\n" +
    "  <ng-include ng-if=\"listView\" src=\"'app/home/preview/common/previewMultiStageListView.tpl.html'\"></ng-include>\n" +
    "  <ng-include ng-if=\"!listView\" src=\"'app/home/preview/common/previewMultiStageTableView.tpl.html'\"></ng-include>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/preview/common/previewMultiStageListView.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/preview/common/previewMultiStageListView.tpl.html",
    "<table class=\"table preview-table\" ng-hide=\"showLoading\">\n" +
    "  <thead>\n" +
    "    <tr>\n" +
    "      <th class=\"col-md-6 from-stage\">\n" +
    "        <select class=\"pull-left form-control input-sm\" name=\"previewFromStage\"\n" +
    "                ng-model=\"fromStage.selected\"\n" +
    "                ng-options=\"stageInst.uiInfo.label for stageInst in pipelineConfig.stages | filter : filterSourceAndProcessors\"\n" +
    "                ng-change=\"onFromStageChange()\">\n" +
    "        </select>\n" +
    "      </th>\n" +
    "\n" +
    "      <th class=\"col-md-6 to-stage\">\n" +
    "        <select class=\"pull-left form-control input-sm\" name=\"previewToStage\"\n" +
    "                ng-model=\"toStage.selected\"\n" +
    "                ng-options=\"stageInst.uiInfo.label for stageInst in toStageList\"\n" +
    "                ng-change=\"onToStageChange()\">\n" +
    "        </select>\n" +
    "      </th>\n" +
    "\n" +
    "    </tr>\n" +
    "  </thead>\n" +
    "\n" +
    "  <tbody>\n" +
    "  <tr ng-repeat=\"inputRecord in multiStagePreviewData.output | limitTo: recordPagination.outputRecords\"\n" +
    "      ng-init=\"outputRecords = getInputRecords(multiStagePreviewData.input, inputRecord)\">\n" +
    "    <td>\n" +
    "      <record-tree\n" +
    "        record=\"inputRecord\"\n" +
    "        record-value=\"inputRecord.value\"\n" +
    "        diff-type=\"'input'\"\n" +
    "        diff-record=\"outputRecords\"\n" +
    "        diff-record-value=\"outputRecords[0].value\"\n" +
    "        field-name=\"('global.form.record' | translate)+($index+1) + getRecordAdditionalInfo(fromStage.selected, inputRecord, 'output')\"\n" +
    "        is-root=\"true\"\n" +
    "        editable=\"previewMode\"\n" +
    "        show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "        show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "      </record-tree>\n" +
    "\n" +
    "      <div class=\"show-more\" ng-if=\"$last && recordPagination.outputRecords < multiStagePreviewData.output.length\">\n" +
    "        <a class=\"btn btn-link\"\n" +
    "           ng-click=\"recordPagination.outputRecords = recordPagination.outputRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "        <span class=\"separator\">|</span>\n" +
    "\n" +
    "        <a class=\"btn btn-link\"\n" +
    "           ng-click=\"recordPagination.outputRecords = multiStagePreviewData.output.length;\">Show All ({{multiStagePreviewData.output.length}}) ...</a>\n" +
    "      </div>\n" +
    "    </td>\n" +
    "\n" +
    "    <td>\n" +
    "      <div ng-repeat=\"record in outputRecords | limitTo: recordPagination.inputRecords\">\n" +
    "        <record-tree\n" +
    "          record=\"record\"\n" +
    "          record-value=\"record.value\"\n" +
    "          diff-type=\"'output'\"\n" +
    "          diff-record=\"inputRecord\"\n" +
    "          diff-record-value=\"inputRecord.value\"\n" +
    "          field-name=\"('global.form.record' | translate)+($parent.$index+1)\"\n" +
    "          is-root=\"true\"\n" +
    "          editable=\"false\"\n" +
    "          show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "          show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "        </record-tree>\n" +
    "\n" +
    "        <div class=\"show-more\" ng-if=\"$last && recordPagination.inputRecords < outputRecords.length\">\n" +
    "          <a class=\"btn btn-link\"\n" +
    "             ng-click=\"recordPagination.inputRecords = recordPagination.inputRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "          <span class=\"separator\">|</span>\n" +
    "\n" +
    "          <a class=\"btn btn-link\"\n" +
    "             ng-click=\"recordPagination.inputRecords = outputRecords.length;\">Show All ({{outputRecords.length}}) ...</a>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </td>\n" +
    "  </tr>\n" +
    "\n" +
    "  <tr ng-repeat=\"inputRecord in multiStagePreviewData.eventRecords | limitTo: recordPagination.eventRecords\"\n" +
    "      ng-init=\"outputRecords = getInputRecords(multiStagePreviewData.input, inputRecord)\">\n" +
    "    <td>\n" +
    "      <record-tree\n" +
    "        record=\"inputRecord\"\n" +
    "        record-value=\"inputRecord.value\"\n" +
    "        diff-type=\"'input'\"\n" +
    "        diff-record=\"outputRecords\"\n" +
    "        diff-record-value=\"outputRecords[0].value\"\n" +
    "        field-name=\"('global.form.eventRecord' | translate)+($index+1) + getRecordAdditionalInfo(fromStage.selected, inputRecord, 'output')\"\n" +
    "        is-root=\"true\"\n" +
    "        editable=\"previewMode\"\n" +
    "        show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "        show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "      </record-tree>\n" +
    "\n" +
    "      <div class=\"show-more\" ng-if=\"$last && recordPagination.eventRecords < multiStagePreviewData.eventRecords.length\">\n" +
    "        <a class=\"btn btn-link\"\n" +
    "           ng-click=\"recordPagination.eventRecords = recordPagination.eventRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "        <span class=\"separator\">|</span>\n" +
    "\n" +
    "        <a class=\"btn btn-link\"\n" +
    "           ng-click=\"recordPagination.eventRecords = multiStagePreviewData.eventRecords.length;\">Show All ({{multiStagePreviewData.eventRecords.length}}) ...</a>\n" +
    "      </div>\n" +
    "    </td>\n" +
    "\n" +
    "    <td>\n" +
    "      <div ng-repeat=\"record in outputRecords | limitTo: recordPagination.inputRecords\">\n" +
    "        <record-tree\n" +
    "          record=\"record\"\n" +
    "          record-value=\"record.value\"\n" +
    "          diff-type=\"'output'\"\n" +
    "          diff-record=\"inputRecord\"\n" +
    "          diff-record-value=\"inputRecord.value\"\n" +
    "          field-name=\"('global.form.record' | translate)+($parent.$index+1)\"\n" +
    "          is-root=\"true\"\n" +
    "          editable=\"false\"\n" +
    "          show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "          show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "        </record-tree>\n" +
    "\n" +
    "        <div class=\"show-more\" ng-if=\"$last && recordPagination.inputRecords < outputRecords.length\">\n" +
    "          <a class=\"btn btn-link\"\n" +
    "             ng-click=\"recordPagination.inputRecords = recordPagination.inputRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "          <span class=\"separator\">|</span>\n" +
    "\n" +
    "          <a class=\"btn btn-link\"\n" +
    "             ng-click=\"recordPagination.inputRecords = outputRecords.length;\">Show All ({{outputRecords.length}}) ...</a>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </td>\n" +
    "  </tr>\n" +
    "\n" +
    "  <tr ng-repeat=\"record in multiStagePreviewData.errorRecords | limitTo: recordPagination.errorRecords\">\n" +
    "\n" +
    "    <td>\n" +
    "      <record-tree\n" +
    "        record=\"record\"\n" +
    "        record-value=\"record.value\"\n" +
    "        field-name=\"('global.form.record' | translate)+ '-' + ('global.form.errorRecord' | translate) + ' ' + record.header.errorMessage\"\n" +
    "        is-root=\"true\"\n" +
    "        is-error=\"true\"\n" +
    "        editable=\"false\"\n" +
    "        show-header=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showHeader\"\n" +
    "        show-field-type=\"snapshotMode || pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "      </record-tree>\n" +
    "\n" +
    "      <div class=\"show-more\" ng-if=\"$last && recordPagination.errorRecords < multiStagePreviewData.errorRecords.length\">\n" +
    "        <a class=\"btn btn-link\"\n" +
    "           ng-click=\"recordPagination.errorRecords = recordPagination.errorRecords + recordMaxLimit;\">Show More...</a>\n" +
    "\n" +
    "        <span class=\"separator\">|</span>\n" +
    "\n" +
    "        <a class=\"btn btn-link\"\n" +
    "           ng-click=\"recordPagination.errorRecords = multiStagePreviewData.errorRecords.length;\">Show All ({{multiStagePreviewData.errorRecordss.length}}) ...</a>\n" +
    "      </div>\n" +
    "    </td>\n" +
    "\n" +
    "    <td>\n" +
    "\n" +
    "    </td>\n" +
    "\n" +
    "  </tr>\n" +
    "\n" +
    "  <tr ng-if=\"multiStagePreviewData.input.length === 0 &&\n" +
    "                      multiStagePreviewData.output.length === 0 &&\n" +
    "                      multiStagePreviewData.errorRecords.length === 0\">\n" +
    "    <td colspan=\"2\" class=\"no-records\" translate=\"home.previewPane.noRecords\">No Records to view.</td>\n" +
    "  </tr>\n" +
    "\n" +
    "  </tbody>\n" +
    "\n" +
    "</table>\n" +
    "");
}]);

angular.module("app/home/preview/common/previewMultiStageTableView.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/preview/common/previewMultiStageTableView.tpl.html",
    "<table class=\"table table-bordered table-striped preview-tableview-table\" ng-hide=\"showLoading\" ng-controller=\"PreviewMultiStageTableViewController\">\n" +
    "  <thead>\n" +
    "    <tr>\n" +
    "      <th class=\"from-stage\" colspan=\"{{outputLimit ? outputLimit : 1}}\">\n" +
    "        <select class=\"pull-left form-control input-sm\" name=\"previewFromStage\"\n" +
    "                ng-model=\"fromStage.selected\"\n" +
    "                ng-options=\"stageInst.uiInfo.label for stageInst in pipelineConfig.stages | filter : filterSourceAndProcessors\"\n" +
    "                ng-change=\"onFromStageChange()\">\n" +
    "        </select>\n" +
    "      </th>\n" +
    "\n" +
    "      <th class=\"to-stage\" colspan=\"{{inputLimit ? inputLimit : 1}}\">\n" +
    "        <select class=\"pull-left form-control input-sm\" name=\"previewToStage\"\n" +
    "                ng-model=\"toStage.selected\"\n" +
    "                ng-options=\"stageInst.uiInfo.label for stageInst in toStageList\"\n" +
    "                ng-change=\"onToStageChange()\">\n" +
    "        </select>\n" +
    "      </th>\n" +
    "\n" +
    "    </tr>\n" +
    "\n" +
    "\n" +
    "    <tr>\n" +
    "\n" +
    "      <th class=\"from-stage\" ng-repeat=\"fieldPath in outputFieldPaths | limitTo: outputLimit\">\n" +
    "        <span>{{fieldPath}}</span>\n" +
    "\n" +
    "        <div class=\"show-more pull-right\" ng-if=\"$last && outputLimit < outputFieldPaths.length\">\n" +
    "          (<a href=\"btn btn-link\" href=\"\"\n" +
    "              ng-click=\"onShowMoreOutputClick($event);\">Show More...</a>\n" +
    "\n" +
    "          <span class=\"separator\">|</span>\n" +
    "\n" +
    "          <a href=\"btn btn-link\" href=\"\"\n" +
    "             ng-click=\"onShowAllOutputClick($event);\">Show All...</a>)\n" +
    "        </div>\n" +
    "\n" +
    "      </th>\n" +
    "\n" +
    "\n" +
    "      <th class=\"to-stage\" ng-repeat=\"fieldPath in inputFieldPaths | limitTo: inputLimit\">\n" +
    "        <span>{{fieldPath}}</span>\n" +
    "\n" +
    "        <div class=\"show-more pull-right\" ng-if=\"$last && inputLimit < inputFieldPaths.length\">\n" +
    "          (<a href=\"btn btn-link\" href=\"\"\n" +
    "              ng-click=\"onShowMoreInputClick($event);\">Show More...</a>\n" +
    "\n" +
    "          <span class=\"separator\">|</span>\n" +
    "\n" +
    "          <a href=\"btn btn-link\" href=\"\"\n" +
    "             ng-click=\"onShowAllInputClick($event);\">Show All...</a>)\n" +
    "        </div>\n" +
    "\n" +
    "      </th>\n" +
    "\n" +
    "    </tr>\n" +
    "\n" +
    "\n" +
    "  </thead>\n" +
    "\n" +
    "  <tbody>\n" +
    "\n" +
    "  <tr ng-repeat-start=\"outputRecord in multiStagePreviewData.output\"\n" +
    "      ng-init=\"\n" +
    "      inputRecords = getInputRecords(multiStagePreviewData.input, outputRecord);\n" +
    "      errorsRecords = getErrorRecords(multiStagePreviewData.errorRecords, outputRecord);\n" +
    "      flattenOutputRecord = getFlattenRecord(outputRecord);\n" +
    "      flattenInputRecord = getFlattenRecord(inputRecords[0])\">\n" +
    "\n" +
    "\n" +
    "\n" +
    "    <!--td ng-repeat=\"fieldPath in outputFieldPaths | limitTo: outputLimit\"\n" +
    "        ng-class=\"{'value-deletion': (flattenOutputRecord[fieldPath].value !== flattenInputRecord[fieldPath].value) }\"\n" +
    "        class=\"field-value {{flattenOutputRecord[fieldPath].type}}\">{{flattenOutputRecord[fieldPath].value + ''}}</td-->\n" +
    "\n" +
    "\n" +
    "\n" +
    "    <!--Input -->\n" +
    "\n" +
    "    <td ng-repeat=\"fieldPath in outputFieldPaths | limitTo: outputLimit\"\n" +
    "        ng-class=\"{'value-deletion': (flattenOutputRecord[fieldPath].value !== flattenInputRecord[fieldPath].value) }\">\n" +
    "\n" +
    "      <span class=\"pull-right glyphicon glyphicon-asterisk dirty-icon\" ng-if=\"flattenOutputRecord[fieldPath].dirty\"></span>\n" +
    "\n" +
    "      <div ng-if=\"flattenOutputRecord[fieldPath].type === 'DATETIME' || flattenOutputRecord[fieldPath].type === 'DATE' || flattenInputRecord[fieldPath].type === 'TIME'\" class=\"clearfix\">\n" +
    "\n" +
    "        <span class=\"field-value pull-left {{flattenOutputRecord[fieldPath].type}}\"\n" +
    "              ng-class=\"{'value-deletion': (flattenOutputRecord[fieldPath].value !== flattenInputRecord[fieldPath].value) }\"\n" +
    "          >{{flattenOutputRecord[fieldPath].value + '' | date:'medium'}} </span>\n" +
    "\n" +
    "        <div class=\"dropdown pull-left\" ng-show=\"previewMode\"\n" +
    "             ng-init=\"dateRecordValue = flattenOutputRecord[fieldPath].value; dropdownSelector = 'dropdownSelector' + $parent.$index + $parent.$parent.$index\">\n" +
    "          <a class=\"record-date-toggle dropdown-toggle {{dropdownSelector}}\" role=\"button\" data-toggle=\"dropdown\" data-target=\"#\" href=\"\">\n" +
    "            <i class=\"fa fa-calendar\"></i>\n" +
    "          </a>\n" +
    "          <ul class=\"dropdown-menu\" role=\"menu\" aria-labelledby=\"dLabel\">\n" +
    "            <datetimepicker data-ng-model=\"dateRecordValue\"\n" +
    "                            data-on-set-time=\"recordDateValueUpdated(outputRecord, flattenOutputRecord[fieldPath], dateRecordValue)\"\n" +
    "                            data-datetimepicker-config=\"{ dropdownSelector: '.' + dropdownSelector }\"></datetimepicker>\n" +
    "          </ul>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "\n" +
    "      <div ng-if=\"flattenOutputRecord[fieldPath].type !== 'DATETIME' && flattenOutputRecord[fieldPath].type !== 'DATE' && flattenOutputRecord[fieldPath].type !== 'TIME'\"\n" +
    "           contenteditable=\"{{previewMode}}\"\n" +
    "           ng-model=\"flattenOutputRecord[fieldPath].value\"\n" +
    "           ng-change=\"recordValueUpdated(outputRecord, flattenOutputRecord[fieldPath])\"\n" +
    "           class=\"field-value {{flattenOutputRecord[fieldPath].type}}\">\n" +
    "      </div>\n" +
    "\n" +
    "    </td>\n" +
    "\n" +
    "\n" +
    "\n" +
    "\n" +
    "\n" +
    "\n" +
    "    <!--td ng-if=\"flattenInputRecord\"\n" +
    "        ng-repeat=\"fieldPath in inputFieldPaths | limitTo: inputLimit\"\n" +
    "        ng-class=\"{'value-addition': flattenInputRecord[fieldPath].value !== flattenOutputRecord[fieldPath].value }\"\n" +
    "        class=\"field-value {{flattenInputRecord[fieldPath].type}}\">{{flattenInputRecord[fieldPath].value + ''}}</td-->\n" +
    "\n" +
    "\n" +
    "\n" +
    "\n" +
    "    <!--Output -->\n" +
    "\n" +
    "    <td ng-if=\"flattenInputRecord\"\n" +
    "        ng-repeat=\"fieldPath in inputFieldPaths | limitTo: inputLimit\"\n" +
    "        ng-class=\"{'value-addition': flattenInputRecord[fieldPath].value !== flattenOutputRecord[fieldPath].value }\"\n" +
    "        class=\"field-value {{flattenInputRecord[fieldPath].type}}\"\n" +
    "      >{{(flattenInputRecord[fieldPath].type === 'DATETIME' || flattenInputRecord[fieldPath].type === 'DATE' || flattenInputRecord[fieldPath].type === 'TIME') ? (flattenInputRecord[fieldPath].value + '' | date:'medium') : (flattenInputRecord[fieldPath].value + '')}}</td>\n" +
    "\n" +
    "\n" +
    "\n" +
    "\n" +
    "    <td colspan=\"{{inputLimit ? inputLimit : 1}}\"\n" +
    "        ng-if=\"inputRecords.length === 0\">\n" +
    "      <label class=\"label label-warning\">{{'home.previewPane.noOutputWarning' | translate}}</label>\n" +
    "    </td>\n" +
    "\n" +
    "  </tr>\n" +
    "\n" +
    "  <tr ng-repeat-end\n" +
    "      ng-repeat=\"inputRecord in inputRecords\"\n" +
    "      ng-if=\"$index > 0\"\n" +
    "      ng-init=\"flattenRecord = getFlattenRecord(inputRecord)\">\n" +
    "    <td colspan=\"{{outputLimit ? outputLimit : 1}}\"></td>\n" +
    "\n" +
    "    <td ng-repeat=\"fieldPath in inputFieldPaths | limitTo: inputLimit\"\n" +
    "        class=\"field-value {{flattenRecord[fieldPath].type}}\"\n" +
    "      >{{(flattenRecord[fieldPath].type === 'DATETIME' || flattenRecord[fieldPath].type === 'DATE' || flattenInputRecord[fieldPath].type === 'TIME') ? (flattenRecord[fieldPath].value + '' | date:'medium') : (flattenRecord[fieldPath].value + '')}}</td>\n" +
    "\n" +
    "  </tr>\n" +
    "\n" +
    "\n" +
    "  <tr ng-repeat=\"record in multiStagePreviewData.errorRecords\">\n" +
    "    <td colspan=\"{{outputLimit ? outputLimit : 1}}\">\n" +
    "      <label class=\"label label-danger\">\n" +
    "        {{('global.form.record' | translate)+ '-' + ('global.form.errorRecord' | translate) + ' ' + record.header.errorMessage}}\n" +
    "      </label>\n" +
    "    </td>\n" +
    "\n" +
    "    <td colspan=\"{{inputLimit ? inputLimit : 1}}\"></td>\n" +
    "  </tr>\n" +
    "\n" +
    "\n" +
    "\n" +
    "  <tr ng-if=\"multiStagePreviewData.input.length === 0 &&\n" +
    "                      multiStagePreviewData.output.length === 0 &&\n" +
    "                      multiStagePreviewData.errorRecords.length === 0\">\n" +
    "    <td colspan=\"2\" class=\"no-records\" translate=\"home.previewPane.noRecords\">No Records to view.</td>\n" +
    "  </tr>\n" +
    "\n" +
    "  </tbody>\n" +
    "\n" +
    "</table>\n" +
    "");
}]);

angular.module("app/home/preview/configuration/previewConfig.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/preview/configuration/previewConfig.tpl.html",
    "<form class=\"form-horizontal preview-config-tab\"\n" +
    "      ng-class=\"{'form-group-sm': $storage.displayDensity === pipelineConstant.DENSITY_COZY || $storage.displayDensity === pipelineConstant.DENSITY_COMPACT}\"\n" +
    "      role=\"form\" name=\"stageGeneralInfo\">\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.previewPane.previewSource\">Preview Source</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <select class=\"form-control\"\n" +
    "              name=\"previewSource\"\n" +
    "              ng-model=\"pipelineConfig.uiInfo.previewConfig.previewSource\">\n" +
    "        <option value=\"{{pipelineConstant.CONFIGURED_SOURCE}}\"\n" +
    "                ng-selected=\"pipelineConfig.uiInfo.previewConfig.previewSource === pipelineConstant.CONFIGURED_SOURCE\">\n" +
    "          {{'home.previewPane.configuredSource' | translate}}</option>\n" +
    "        <option value=\"{{pipelineConstant.SNAPSHOT_SOURCE}}\"\n" +
    "                ng-selected=\"pipelineConfig.uiInfo.previewConfig.previewSource === pipelineConstant.SNAPSHOT_SOURCE\">\n" +
    "          {{'home.previewPane.snapshotSource' | translate}}</option>\n" +
    "      </select>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.previewPane.batchSize\">Preview Batch Size</label>\n" +
    "    <div class=\"controls col-md-1\">\n" +
    "      <input type=\"text\" class=\"form-control\" name=\"batchSize\"\n" +
    "             ng-disabled=\"isPipelineRunning\"\n" +
    "             ng-model=\"pipelineConfig.uiInfo.previewConfig.batchSize\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.previewPane.timeout\">Preview Timeout</label>\n" +
    "    <div class=\"controls col-md-1\">\n" +
    "      <input type=\"text\" class=\"form-control\" name=\"timeout\"\n" +
    "             ng-disabled=\"isPipelineRunning\"\n" +
    "             ng-model=\"pipelineConfig.uiInfo.previewConfig.timeout\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\">\n" +
    "      {{'home.previewPane.writeToDestinations' | translate}}\n" +
    "      <i class=\"fa fa-info-circle help-icon\"\n" +
    "         tooltip-placement=\"right\"\n" +
    "         tooltip-append-to-body=\"true\"\n" +
    "         tooltip=\"{{'home.previewPane.writeToDestinationsTooltip' | translate}}\"\n" +
    "         tooltip-trigger=\"mouseenter\"></i>\n" +
    "    </label>\n" +
    "    <div class=\"controls col-md-1\">\n" +
    "      <input type=\"checkbox\" name=\"writeToDestinations\"\n" +
    "             ng-disabled=\"isPipelineRunning\"\n" +
    "             ng-model=\"pipelineConfig.uiInfo.previewConfig.writeToDestinations\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\">\n" +
    "      {{'home.previewPane.executeLifecycleEvents' | translate}}\n" +
    "      <i class=\"fa fa-info-circle help-icon\"\n" +
    "         tooltip-placement=\"right\"\n" +
    "         tooltip-append-to-body=\"true\"\n" +
    "         tooltip=\"{{'home.previewPane.executeLifecycleEventsTooltip' | translate}}\"\n" +
    "         tooltip-trigger=\"mouseenter\"></i>\n" +
    "    </label>\n" +
    "    <div class=\"controls col-md-1\">\n" +
    "      <input type=\"checkbox\" name=\"executeLifecycleEvents\"\n" +
    "             ng-disabled=\"isPipelineRunning\"\n" +
    "             ng-model=\"pipelineConfig.uiInfo.previewConfig.executeLifecycleEvents\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\">\n" +
    "      {{'home.previewPane.showHeader' | translate}}\n" +
    "    </label>\n" +
    "    <div class=\"controls col-md-1\">\n" +
    "      <input type=\"checkbox\" name=\"showHeader\"\n" +
    "             ng-disabled=\"isPipelineRunning\"\n" +
    "             ng-model=\"pipelineConfig.uiInfo.previewConfig.showHeader\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\">\n" +
    "      {{'home.previewPane.showFieldType' | translate}}\n" +
    "    </label>\n" +
    "    <div class=\"controls col-md-1\">\n" +
    "      <input type=\"checkbox\" name=\"showFieldType\"\n" +
    "             ng-disabled=\"isPipelineRunning\"\n" +
    "             ng-model=\"pipelineConfig.uiInfo.previewConfig.showFieldType\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\" ng-if=\"snapshotsInfo && snapshotsInfo.length && pipelineConfig.uiInfo.previewConfig.previewSource === pipelineConstant.SNAPSHOT_SOURCE\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\" translate=\"home.previewPane.snapshotSource\">Snapshot</label>\n" +
    "    <div class=\"controls col-md-7\">\n" +
    "      <select class=\"form-control\"\n" +
    "              name=\"snapshotInfo\"\n" +
    "              ng-model=\"pipelineConfig.uiInfo.previewConfig.snapshotInfo\"\n" +
    "              ng-options=\"snapshotInfo.id group by snapshotInfo.name for snapshotInfo in snapshotsInfo track by snapshotInfo.timeStamp\">\n" +
    "      </select>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"col-lg-3 col-md-4 control-label\">\n" +
    "      {{'home.previewPane.rememberMe' | translate}}\n" +
    "    </label>\n" +
    "    <div class=\"controls col-md-1\">\n" +
    "      <input type=\"checkbox\" name=\"rememberMe\"\n" +
    "             ng-disabled=\"isPipelineRunning\"\n" +
    "             ng-model=\"pipelineConfig.uiInfo.previewConfig.rememberMe\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/preview/configuration/previewConfigModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/preview/configuration/previewConfigModal.tpl.html",
    "<form class=\"preview-config-modal-form\" role=\"form\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.previewPane.configTitle\">Preview Configuration</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <div class=\"form-group\">\n" +
    "      <label class=\"control-label\" translate=\"home.previewPane.previewSource\">Preview Source</label>\n" +
    "      <select class=\"form-control\"\n" +
    "              name=\"previewSource\"\n" +
    "              ng-model=\"previewConfig.previewSource\">\n" +
    "        <option value=\"{{pipelineConstant.CONFIGURED_SOURCE}}\"\n" +
    "                ng-selected=\"previewConfig.previewSource === pipelineConstant.CONFIGURED_SOURCE\">\n" +
    "          {{'home.previewPane.configuredSource' | translate}}</option>\n" +
    "        <option value=\"{{pipelineConstant.SNAPSHOT_SOURCE}}\"\n" +
    "                ng-selected=\"previewConfig.previewSource === pipelineConstant.SNAPSHOT_SOURCE\">\n" +
    "          {{'home.previewPane.snapshotSource' | translate}}</option>\n" +
    "        <!--option value=\"{{pipelineConstant.USER_PROVIDED}}\"\n" +
    "                ng-selected=\"previewConfig.previewSource === pipelineConstant.USER_PROVIDED\">\n" +
    "          {{'home.previewPane.userProvider' | translate}}</option-->\n" +
    "      </select>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" ng-if=\"previewConfig.previewSource !== pipelineConstant.USER_PROVIDED\">\n" +
    "      <label class=\"control-label\" translate=\"home.previewPane.batchSize\">Preview Batch Size</label>\n" +
    "      <input type=\"text\" class=\"form-control\" name=\"batchSize\"\n" +
    "             ng-model=\"previewConfig.batchSize\">\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" ng-if=\"previewConfig.previewSource !== pipelineConstant.USER_PROVIDED\">\n" +
    "      <label class=\"control-label\" translate=\"home.previewPane.timeout\">Preview Timeout</label>\n" +
    "      <input type=\"text\" class=\"form-control\" name=\"timeout\"\n" +
    "             ng-model=\"previewConfig.timeout\">\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"checkbox\">\n" +
    "      <label>\n" +
    "        <input type=\"checkbox\" ng-model=\"previewConfig.writeToDestinations\"> {{'home.previewPane.writeToDestinations' | translate}}\n" +
    "        <i class=\"fa fa-info-circle help-icon\"\n" +
    "           tooltip-placement=\"right\"\n" +
    "           tooltip-append-to-body=\"false\"\n" +
    "           tooltip=\"{{'home.previewPane.writeToDestinationsTooltip' | translate}}\"\n" +
    "           tooltip-trigger=\"mouseenter\"></i>\n" +
    "      </label>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"checkbox\">\n" +
    "      <label>\n" +
    "        <input type=\"checkbox\" ng-model=\"previewConfig.executeLifecycleEvents\"> {{'home.previewPane.executeLifecycleEvents' | translate}}\n" +
    "        <i class=\"fa fa-info-circle help-icon\"\n" +
    "           tooltip-placement=\"right\"\n" +
    "           tooltip-append-to-body=\"false\"\n" +
    "           tooltip=\"{{'home.previewPane.executeLifecycleEventsTooltip' | translate}}\"\n" +
    "           tooltip-trigger=\"mouseenter\"></i>\n" +
    "      </label>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"checkbox\">\n" +
    "      <label>\n" +
    "        <input type=\"checkbox\" ng-model=\"previewConfig.showHeader\"> {{'home.previewPane.showHeader' | translate}}\n" +
    "      </label>\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "    <div class=\"checkbox\">\n" +
    "      <label>\n" +
    "        <input type=\"checkbox\" ng-model=\"previewConfig.showFieldType\"> {{'home.previewPane.showFieldType' | translate}}\n" +
    "      </label>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" ng-if=\"snapshotsInfo && snapshotsInfo.length && previewConfig.previewSource === pipelineConstant.SNAPSHOT_SOURCE\">\n" +
    "      <label class=\"control-label\" translate=\"home.previewPane.snapshotSource\">Snapshot</label>\n" +
    "      <select class=\"form-control\"\n" +
    "              name=\"snapshotInfo\"\n" +
    "              ng-model=\"previewConfig.snapshotInfo\"\n" +
    "              ng-options=\"snapshotInfo.label group by snapshotInfo.name for snapshotInfo in snapshotsInfo track by snapshotInfo.timeStamp\">\n" +
    "\n" +
    "      </select>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-group\" ng-if=\"previewConfig.previewSource === pipelineConstant.USER_PROVIDED\">\n" +
    "      <label class=\"control-label\" translate=\"home.previewPane.inputData\">Input Data</label>\n" +
    "\n" +
    "      <div ui-codemirror\n" +
    "           class=\"codemirror-editor\"\n" +
    "           ng-model=\"previewConfig.userData\"\n" +
    "           ui-refresh=\"refreshCodemirror\"\n" +
    "           ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "                lineNumbers: true\n" +
    "              })\"\n" +
    "           field-paths=\"fieldPaths\">\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "    <div class=\"checkbox\">\n" +
    "      <label>\n" +
    "        <input type=\"checkbox\" ng-model=\"previewConfig.rememberMe\"> {{'home.previewPane.rememberMe' | translate}}\n" +
    "      </label>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"button\" class=\"btn btn-default\" ng-click=\"cancel()\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" ng-click=\"runPreview()\"\n" +
    "            translate=\"home.previewPane.runPreview\">Run Preview</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/preview/preview.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/preview/preview.tpl.html",
    "<div class=\"panel panel-default preview-pane\"\n" +
    "     ng-controller=\"PreviewController\"\n" +
    "     ng-if=\"previewMode\">\n" +
    "\n" +
    "  <div class=\"panel-heading clearfix\">\n" +
    "\n" +
    "    <h3 class=\"panel-title pull-left\">\n" +
    "      <span ng-if=\"!previewMultipleStages\">{{'home.previewPane.previewStage' | translate}}: </span>\n" +
    "      <span ng-if=\"previewMultipleStages\">{{'home.previewPane.previewMultipleStages' | translate}}</span>\n" +
    "      <span ng-if=\"!previewMultipleStages\">{{detailPaneConfig.uiInfo.label}}</span>\n" +
    "    </h3>\n" +
    "\n" +
    "    <div class=\"pull-right size-toolbar\">\n" +
    "\n" +
    "      <div class=\"btn-group pull-left detail-settings-dropdown\" ng-if=\"!$storage.dontShowRESTResponseMenu\">\n" +
    "        <a class=\"btn btn-link dropdown-toggle\" data-toggle=\"dropdown\"\n" +
    "           tooltip-placement=\"top\"\n" +
    "           tooltip=\"{{'home.header.more' | translate}}\"\n" +
    "           tooltip-popup-delay=\"500\">\n" +
    "          <span class=\"fa fa-ellipsis-h fa-14x pointer\"></span>\n" +
    "        </a>\n" +
    "        <ul class=\"dropdown-menu pull-right\" role=\"menu\">\n" +
    "\n" +
    "          <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "              translate=\"home.detailPane.restURL\">REST URL {{currentPreviewerId}}</li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\"\n" +
    "               ng-click=\"viewRawPreviewData()\">{{'home.detailPane.pipelinePreviewJSONData' | translate}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "      <span class=\"fa fa-question-circle fa-14x pointer\"\n" +
    "            tooltip-placement=\"top\"\n" +
    "            tooltip=\"{{'global.form.help' | translate}}\"\n" +
    "            tooltip-popup-delay=\"500\"\n" +
    "            ng-click=\"launchHelp('pipeline-preview')\"></span>\n" +
    "\n" +
    "      <span class=\"glyphicon pointer\"\n" +
    "            tooltip-placement=\"top\"\n" +
    "            tooltip=\"{{!$storage.minimizeDetailPane ? 'global.form.minimizePane' : 'global.form.maximizePane' | translate}}\"\n" +
    "            tooltip-popup-delay=\"1000\"\n" +
    "            ng-hide=\"$storage.maximizeDetailPane\"\n" +
    "            ng-class=\"{'glyphicon-minus' : !$storage.minimizeDetailPane , 'glyphicon-resize-full': $storage.minimizeDetailPane}\"\n" +
    "            ng-click=\"onMinimizeDetailPane()\"></span>\n" +
    "\n" +
    "      <span class=\"glyphicon pointer\"\n" +
    "            tooltip-placement=\"top\"\n" +
    "            tooltip=\"{{!$storage.maximizeDetailPane ? 'global.form.maximizePane' : 'global.form.minimizePane' | translate}}\"\n" +
    "            tooltip-popup-delay=\"1000\"\n" +
    "            ng-hide=\"$storage.minimizeDetailPane\"\n" +
    "            ng-class=\"{'glyphicon-fullscreen' : !$storage.maximizeDetailPane , 'glyphicon-resize-small': $storage.maximizeDetailPane}\"\n" +
    "            ng-click=\"onMaximizeDetailPane()\"></span>\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "    <!-- Buttons for Single Stage Preview -->\n" +
    "\n" +
    "    <div class=\"pull-right preview-toolbar\" ng-if=\"showLoading\">\n" +
    "      <a type=\"button\" class=\"btn btn-danger btn-sm\"\n" +
    "         ng-click=\"cancelPreview()\">{{ 'home.previewPane.cancelPreview' | translate }}</a>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right preview-toolbar btn-group\" ng-if=\"!showLoading\">\n" +
    "\n" +
    "      <div ng-if=\"!previewMultipleStages && previousStageInstances.length > 1\" class=\"dropdown pull-left btn-group\">\n" +
    "        <a type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "           tooltip-placement=\"bottom\" tooltip=\"{{'global.form.previousStage' | translate}}\"\n" +
    "           tooltip-popup-delay=\"500\"\n" +
    "           tooltip-append-to-body=\"true\"\n" +
    "           data-toggle=\"dropdown\">\n" +
    "          <span class=\"glyphicon glyphicon-arrow-left\"></span>\n" +
    "        </a>\n" +
    "        <ul class=\"dropdown-menu pull-right\" role=\"menu\">\n" +
    "          <li ng-repeat=\"previousStageInstance in previousStageInstances\">\n" +
    "            <a href=\"#\" ng-click=\"previousStagePreview(previousStageInstance)\">\n" +
    "              {{previousStageInstance.uiInfo.label}}\n" +
    "            </a>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "      <a role=\"button\" type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "         ng-if=\"!previewMultipleStages && previousStageInstances.length <= 1\"\n" +
    "         tooltip-placement=\"bottom\" tooltip=\"{{'global.form.previousStage' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         tooltip-append-to-body=\"true\"\n" +
    "         ng-click=\"previousStageInstances.length === 0 || previousStagePreview(previousStageInstances[0])\"\n" +
    "         ng-disabled=\"previousStageInstances.length === 0\">\n" +
    "        <span class=\"glyphicon glyphicon-arrow-left\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <div ng-if=\"!previewMultipleStages && nextStageInstances.length > 1\" class=\"dropdown pull-left btn-group\">\n" +
    "        <a type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "           tooltip-placement=\"bottom\" tooltip=\"{{'global.form.nextStage' | translate}}\"\n" +
    "           tooltip-popup-delay=\"500\"\n" +
    "           tooltip-append-to-body=\"true\"\n" +
    "           data-toggle=\"dropdown\">\n" +
    "          <span class=\"glyphicon glyphicon-arrow-right\"></span>\n" +
    "        </a>\n" +
    "        <ul class=\"dropdown-menu pull-right\" role=\"menu\">\n" +
    "          <li ng-repeat=\"nextStageInstance in nextStageInstances\">\n" +
    "            <a href=\"#\" ng-click=\"nextStagePreview(nextStageInstance, stagePreviewData.output)\">\n" +
    "              {{nextStageInstance.uiInfo.label}}\n" +
    "            </a>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "      <a type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "         ng-if=\"!previewMultipleStages && nextStageInstances.length <= 1\"\n" +
    "         tooltip-placement=\"bottom\" tooltip=\"{{'global.form.nextStage' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         tooltip-append-to-body=\"true\"\n" +
    "         ng-disabled=\"nextStageInstances.length === 0\"\n" +
    "         ng-click=\"nextStageInstances.length === 0 || nextStagePreview(nextStageInstances[0], stagePreviewData.output)\">\n" +
    "        <span class=\"glyphicon glyphicon-arrow-right\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "         tooltip-placement=\"bottom\" tooltip=\"{{'home.header.refreshPreview' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         tooltip-append-to-body=\"true\"\n" +
    "         ng-click=\"previewPipeline(true)\">\n" +
    "        <span class=\"fa fa-refresh\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "      <a type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "         tooltip-placement=\"bottom\" tooltip=\"{{'global.form.step' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         tooltip-append-to-body=\"true\"\n" +
    "         ng-disabled=\"!previewDataUpdated && !pipelineConfigUpdated\"\n" +
    "         ng-click=\"(!previewDataUpdated && !pipelineConfigUpdated) || stepPreview(detailPaneConfig, stagePreviewData.input)\">\n" +
    "        <span class=\"fa fa-refresh\"></span> *\n" +
    "      </a>\n" +
    "\n" +
    "      <a type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "         tooltip-placement=\"bottom\" tooltip=\"{{'global.form.revertChanges' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         tooltip-append-to-body=\"true\"\n" +
    "         ng-click=\"!previewDataUpdated || revertChanges()\"\n" +
    "         ng-disabled=\"!previewDataUpdated\">\n" +
    "        <span class=\"fa fa-undo\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right btn-group toggle-toolbar\" ng-show=\"!showLoading\">\n" +
    "      <label class=\"btn btn-default btn-sm\" ng-model=\"listView\" btn-radio=\"true\"\n" +
    "             tooltip-placement=\"bottom\"\n" +
    "             tooltip-append-to-body=\"true\"\n" +
    "             tooltip=\"{{'global.form.listView' | translate}}\">\n" +
    "        <i class=\"fa fa-list fa-12x\"/>\n" +
    "      </label>\n" +
    "      <label class=\"btn btn-default btn-sm\" ng-model=\"listView\" btn-radio=\"false\"\n" +
    "             tooltip-placement=\"bottom\"\n" +
    "             tooltip-append-to-body=\"true\"\n" +
    "             tooltip=\"{{'global.form.tableView' | translate}}\">\n" +
    "        <i class=\"fa fa-table fa-12x\"/>\n" +
    "      </label>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right btn-group toggle-toolbar\" ng-show=\"!showLoading && hasMultipleStages()\">\n" +
    "      <label class=\"btn btn-default btn-sm\" ng-model=\"previewMultipleStages\" btn-radio=\"false\">Single</label>\n" +
    "      <label class=\"btn btn-default btn-sm\" ng-model=\"previewMultipleStages\" btn-radio=\"true\">Multiple</label>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"panel-body\">\n" +
    "    <tabset class=\"preview-tabs-left tabs-left icon-only\">\n" +
    "\n" +
    "      <tab active=\"recordsTabActive\" select=\"onRecordsTabSelect()\">\n" +
    "        <tab-heading tooltip-placement=\"right\" tooltip=\"{{ 'global.form.records' | translate }}\">\n" +
    "          <i class=\"fa fa-list-ul fa-12x\"></i>\n" +
    "        </tab-heading>\n" +
    "        <ng-include ng-if=\"recordsTabActive && !previewMultipleStages\" src=\"'app/home/preview/common/previewCommon.tpl.html'\"></ng-include>\n" +
    "        <ng-include ng-if=\"recordsTabActive && previewMultipleStages\" src=\"'app/home/preview/common/previewMultiStage.tpl.html'\"></ng-include>\n" +
    "      </tab>\n" +
    "\n" +
    "      <tab active=\"configurationTabActive\">\n" +
    "        <tab-heading tooltip-placement=\"right\" tooltip=\"{{ 'home.previewPane.stageConfigTitle' | translate }}\">\n" +
    "          <i class=\"fa fa-gear fa-12x\"></i>\n" +
    "        </tab-heading>\n" +
    "        <ng-include ng-show=\"configurationTabActive\" src=\"'app/home/detail/configuration/configuration.tpl.html'\"></ng-include>\n" +
    "      </tab>\n" +
    "\n" +
    "      <tab active=\"previewConfigTabActive\">\n" +
    "        <tab-heading tooltip-placement=\"right\" tooltip=\"{{ 'home.previewPane.configTitle' | translate }}\">\n" +
    "          <i class=\"fa fa-eye fa-12x\"></i>\n" +
    "        </tab-heading>\n" +
    "        <ng-include ng-if=\"previewConfigTabActive\" src=\"'app/home/preview/configuration/previewConfig.tpl.html'\"></ng-include>\n" +
    "      </tab>\n" +
    "\n" +
    "      <tab  active=\"errorsTabActive\" ng-if=\"stagePreviewData.stageErrors && stagePreviewData.stageErrors.length\">\n" +
    "        <tab-heading tooltip-placement=\"right\" tooltip=\"{{ 'global.form.errors' | translate }}\">\n" +
    "          <i class=\"fa fa-exclamation-triangle icon-danger fa-12x\"></i>\n" +
    "        </tab-heading>\n" +
    "        <div ng-if=\"errorsTabActive\">\n" +
    "          <table class=\"table table-hover\">\n" +
    "            <caption>\n" +
    "              <label translate=\"global.form.errorMessages\">Error Messages</label>\n" +
    "            </caption>\n" +
    "            <thead>\n" +
    "            <tr>\n" +
    "              <th class=\"col-md-3\" translate=\"global.form.timestamp\">Timestamp</th>\n" +
    "              <th class=\"col-md-2\" translate=\"global.form.errorCode\">Error Code</th>\n" +
    "              <th class=\"col-md-7\" translate=\"global.form.errorMessage\"> Error Message</th>\n" +
    "            </tr>\n" +
    "            </thead>\n" +
    "            <tbody>\n" +
    "            <tr ng-repeat=\"errorMessage in stagePreviewData.stageErrors\">\n" +
    "              <td>{{errorMessage.timestamp | date:'medium'}}</td>\n" +
    "              <td ng-bind=\"errorMessage.errorCode\"> </td>\n" +
    "              <td>\n" +
    "                <span ng-bind=\"errorMessage.localized\"></span>\n" +
    "                <a class=\"view-stack-trace\"\n" +
    "                   ng-if=\"errorMessage.errorStackTrace\"\n" +
    "                   ng-click=\"showStackTrace(errorMessage)\">\n" +
    "                  ( <span translate=\"home.graphPane.viewStackTrace\">View Stack Trace</span>... )\n" +
    "                </a>\n" +
    "              </td>\n" +
    "            </tr>\n" +
    "            </tbody>\n" +
    "          </table>\n" +
    "        </div>\n" +
    "      </tab>\n" +
    "\n" +
    "    </tabset>\n" +
    "\n" +
    "\n" +
    "  </div>\n" +
    "\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/preview/rawPreviewData/rawPreviewDataModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/preview/rawPreviewData/rawPreviewDataModal.tpl.html",
    "<form class=\"raw-preview-data-modal-form\" role=\"form\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"close()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.previewPane.rawPreviewData.modalTitle\">Raw Preview Data</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "    <div ui-codemirror\n" +
    "         class=\"codemirror-editor\"\n" +
    "         ng-model=\"previewData\"\n" +
    "         ui-refresh=\"refreshCodemirror\"\n" +
    "         ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "              lineNumbers: true\n" +
    "            })\"\n" +
    "         field-paths=\"fieldPaths\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"button\" class=\"btn btn-default\" ng-click=\"close()\"\n" +
    "            translate=\"global.form.close\">Close</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("app/home/resetOffset/resetOffset.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/resetOffset/resetOffset.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <button type=\"button\" class=\"close\" ng-click=\"no()\">\n" +
    "    <span aria-hidden=\"true\">&times;</span>\n" +
    "    <span class=\"sr-only\">Close</span>\n" +
    "  </button>\n" +
    "  <h3 class=\"modal-title\" translate=\"home.resetOffset.confirmationTitle\">Reset Offset Confirmation</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body reset-offset-modal-body\">\n" +
    "\n" +
    "  <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "  <div class=\"alert alert-success\" role=\"alert\"\n" +
    "       ng-show=\"isOffsetResetSucceed\"\n" +
    "       translate=\"home.resetOffset.successMessage\">\n" +
    "  </div>\n" +
    "\n" +
    "  <p ng-hide=\"isOffsetResetSucceed || showLoading\">\n" +
    "    <div ng-show=\"isList\">\n" +
    "      <div translate=\"home.resetOffset.confirmationMessageBulk\"></div>\n" +
    "      <div ng-repeat=\"pipeline in pipelineInfo\">{{pipeline.title}}</div>\n" +
    "    </div>\n" +
    "    <div ng-hide=\"isList\" translate=\"home.resetOffset.confirmationMessage\"></div>\n" +
    "  </p>\n" +
    "\n" +
    "  <div show-loading=\"showLoading\"></div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\" ng-hide=\"showLoading\">\n" +
    "  <button type=\"button\" class=\"btn btn-default\"\n" +
    "          ng-hide=\"isOffsetResetSucceed || common.errors.length\"\n" +
    "          ng-click=\"no()\"\n" +
    "          translate=\"global.form.no\">No</button>\n" +
    "  <button type=\"button\" class=\"btn btn-primary\"\n" +
    "          ng-hide=\"isOffsetResetSucceed || common.errors.length\"\n" +
    "          ng-click=\"yes()\"\n" +
    "          translate=\"global.form.yes\">Yes</button>\n" +
    "  <button type=\"button\" class=\"btn btn-primary\"\n" +
    "          ng-show=\"isOffsetResetSucceed || common.errors.length\"\n" +
    "          ng-click=\"close()\"\n" +
    "          translate=\"global.form.close\">Close</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/snapshot/modal/snapshotModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/snapshot/modal/snapshotModal.tpl.html",
    "<div class=\"snapshots-modal-form\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"close()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.snapshotsPane.modalTitle\">Snapshots</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <button type=\"button\" class=\"btn btn-primary\"\n" +
    "            ng-if=\"isPipelineRunning\"\n" +
    "            ng-disabled=\"showLoading || snapshotInProgress || !canExecute\"\n" +
    "            ng-click=\"captureSnapshot()\"\n" +
    "            translate=\"home.snapshotsPane.captureSnapshot\">Capture</button>\n" +
    "\n" +
    "    <button type=\"button\" class=\"btn btn-primary\"\n" +
    "            ng-if=\"!isPipelineRunning\"\n" +
    "            ng-disabled=\"showLoading || snapshotInProgress || !canExecute\"\n" +
    "            ng-click=\"captureSnapshot()\"\n" +
    "            translate=\"home.snapshotsPane.startAndCaptureSnapshot\">Start & Capture</button>\n" +
    "\n" +
    "    <table class=\"table table-hover\">\n" +
    "      <thead>\n" +
    "        <tr>\n" +
    "          <th class=\"snapshot-label\">{{'global.form.name' | translate}}</th>\n" +
    "          <th class=\"snapshot-time\">{{'global.form.captured' | translate}}</th>\n" +
    "          <th class=\"snapshot-batch-number\">{{'global.form.batchNumber' | translate}}</th>\n" +
    "          <th class=\"snapshot-action\">{{'global.form.actions' | translate}}</th>\n" +
    "        </tr>\n" +
    "      </thead>\n" +
    "\n" +
    "      <tbody ng-hide=\"showLoading\">\n" +
    "        <tr ng-repeat=\"snapshotInfo in snapshotsInfo\">\n" +
    "          <td>\n" +
    "            <span ng-if=\"snapshotInfo.inProgress\">{{ snapshotInfo.label || snapshotInfo.id }}</span>\n" +
    "            <a href=\"#\"\n" +
    "               ng-if=\"!snapshotInfo.inProgress\"\n" +
    "               editable-text=\"snapshotInfo.label\"\n" +
    "               onaftersave=\"snapshotInfoLabelUpdated(snapshotInfo)\"\n" +
    "               title=\"{{'home.snapshotsPane.labelChangeTitle' | translate}}\"\n" +
    "            >{{ snapshotInfo.label || snapshotInfo.id }}</a>\n" +
    "          </td>\n" +
    "          <td>\n" +
    "            {{!snapshotInfo.inProgress ? (snapshotInfo.timeStamp  | date:'medium') : ('home.snapshotsPane.captureSnapshotInProgress' | translate)}}\n" +
    "          </td>\n" +
    "          <td>\n" +
    "            {{(!snapshotInfo.inProgress && snapshotInfo.batchNumber) ? (snapshotInfo.batchNumber) : '--'}}\n" +
    "          </td>\n" +
    "          <td>\n" +
    "            <a href=\"javascript:;\" translate=\"global.form.view\"\n" +
    "               ng-if=\"!snapshotInfo.inProgress\"\n" +
    "               ng-click=\"viewSnapshot(snapshotInfo)\"></a>\n" +
    "            <span class=\"separator\" ng-if=\"!snapshotInfo.inProgress\">|</span>\n" +
    "            <a href=\"javascript:;\" translate=\"global.form.download\" ng-if=\"!snapshotInfo.inProgress\" ng-click=\"downloadSnapshot(snapshotInfo)\"></a>\n" +
    "            <span class=\"separator\" ng-if=\"!snapshotInfo.inProgress\">|</span>\n" +
    "            <a href=\"javascript:;\" translate=\"global.form.delete\"\n" +
    "               ng-if=\"!snapshotInfo.inProgress\"\n" +
    "               ng-click=\"deleteSnapshot(snapshotInfo.id, $index)\"> Delete </a>\n" +
    "            <a href=\"javascript:;\" translate=\"global.form.cancel\"\n" +
    "               ng-if=\"snapshotInfo.inProgress\"\n" +
    "               ng-click=\"cancelSnapshot(snapshotInfo.id, $index)\"> Cancel </a>\n" +
    "          </td>\n" +
    "        </tr>\n" +
    "\n" +
    "        <tr ng-if=\"snapshotsInfo.length === 0\">\n" +
    "          <td colspan=\"3\" class=\"no-records text-center\"\n" +
    "              translate=\"home.snapshotsPane.noSnapshots\">No Snapshots.</td>\n" +
    "        </tr>\n" +
    "\n" +
    "      </tbody>\n" +
    "\n" +
    "    </table>\n" +
    "\n" +
    "    <div show-loading=\"showLoading\"></div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"button\" class=\"btn btn-primary\" ng-click=\"close()\"\n" +
    "            translate=\"global.form.close\">close</button>\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/snapshot/snapshot.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/snapshot/snapshot.tpl.html",
    "<div class=\"panel panel-default snapshot-pane\"\n" +
    "     ng-controller=\"SnapshotController\"\n" +
    "     ng-if=\"snapshotMode\">\n" +
    "\n" +
    "  <div class=\"panel-heading clearfix\">\n" +
    "\n" +
    "    <div class=\"btn-group pull-left detail-name-dropdown\">\n" +
    "      <button class=\"btn btn-link dropdown-toggle\" type=\"button\" data-toggle=\"dropdown\">\n" +
    "        <span>{{activeSnapshotInfo.label || activeSnapshotInfo.id}}</span>\n" +
    "\n" +
    "        <span class=\"caret\"></span>\n" +
    "      </button>\n" +
    "      <ul class=\"dropdown-menu\" role=\"menu\" aria-labelledby=\"dropdownMenu1\">\n" +
    "        <li role=\"presentation\"\n" +
    "            ng-repeat=\"snapshotInfo in snapshotsInfo\">\n" +
    "          <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "             ng-click=\"viewSnapshot(snapshotInfo)\"\n" +
    "            >{{snapshotInfo.label || snapshotInfo.id}}</a>\n" +
    "        </li>\n" +
    "      </ul>\n" +
    "    </div>\n" +
    "\n" +
    "    <h3 class=\"panel-title pull-left\">\n" +
    "      <span ng-if=\"!previewMultipleStages\">: {{detailPaneConfig.uiInfo.label}}</span>\n" +
    "    </h3>\n" +
    "\n" +
    "    <div class=\"pull-right size-toolbar\">\n" +
    "      <span class=\"fa fa-question-circle fa-14x pointer\"\n" +
    "            tooltip-placement=\"top\"\n" +
    "            tooltip=\"{{'global.form.help' | translate}}\"\n" +
    "            tooltip-popup-delay=\"500\"\n" +
    "            ng-click=\"launchHelp('pipeline-snapshot')\"></span>\n" +
    "\n" +
    "      <span class=\"glyphicon pointer\"\n" +
    "            tooltip-placement=\"top\"\n" +
    "            tooltip=\"{{!$storage.minimizeDetailPane ? 'global.form.minimizePane' : 'global.form.maximizePane' | translate}}\"\n" +
    "            tooltip-popup-delay=\"1000\"\n" +
    "            ng-hide=\"$storage.maximizeDetailPane\"\n" +
    "            ng-class=\"{'glyphicon-minus' : !$storage.minimizeDetailPane , 'glyphicon-resize-full': $storage.minimizeDetailPane}\"\n" +
    "            ng-click=\"onMinimizeDetailPane()\"></span>\n" +
    "\n" +
    "      <span class=\"glyphicon pointer\"\n" +
    "            tooltip-placement=\"top\"\n" +
    "            tooltip=\"{{!$storage.maximizeDetailPane ? 'global.form.maximizePane' : 'global.form.minimizePane' | translate}}\"\n" +
    "            tooltip-popup-delay=\"1000\"\n" +
    "            ng-hide=\"$storage.minimizeDetailPane\"\n" +
    "            ng-class=\"{'glyphicon-fullscreen' : !$storage.maximizeDetailPane , 'glyphicon-resize-small': $storage.maximizeDetailPane}\"\n" +
    "            ng-click=\"onMaximizeDetailPane()\"></span>\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "    <div class=\"pull-right preview-toolbar btn-group\" ng-if=\"!showLoading\">\n" +
    "\n" +
    "      <div ng-if=\"!previewMultipleStages && previousStageInstances.length > 1\" class=\"dropdown pull-left btn-group\">\n" +
    "        <a role=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "           tooltip-placement=\"bottom\" tooltip=\"{{'global.form.previousStage' | translate}}\"\n" +
    "           tooltip-popup-delay=\"500\"\n" +
    "           tooltip-append-to-body=\"true\"\n" +
    "           data-toggle=\"dropdown\">\n" +
    "          <span class=\"glyphicon glyphicon-arrow-left\"></span>\n" +
    "        </a>\n" +
    "        <ul class=\"dropdown-menu\" role=\"menu\" class=\"pull-left\">\n" +
    "          <li ng-repeat=\"previousStageInstance in previousStageInstances\">\n" +
    "            <a href=\"#\" ng-click=\"previousStagePreview(previousStageInstance)\">\n" +
    "              {{previousStageInstance.uiInfo.label}}\n" +
    "            </a>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "      <a role=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "         ng-if=\"!previewMultipleStages && previousStageInstances.length <= 1\"\n" +
    "         tooltip-placement=\"bottom\" tooltip=\"{{'global.form.previousStage' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         tooltip-append-to-body=\"true\"\n" +
    "         ng-click=\"previousStagePreview(previousStageInstances[0])\"\n" +
    "         ng-disabled=\"previousStageInstances.length === 0\">\n" +
    "        <span class=\"glyphicon glyphicon-arrow-left\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "\n" +
    "      <div ng-if=\"!previewMultipleStages && nextStageInstances.length > 1\" class=\"dropdown pull-left btn-group\">\n" +
    "        <a role=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "           tooltip-placement=\"bottom\" tooltip=\"{{'global.form.nextStage' | translate}}\"\n" +
    "           tooltip-popup-delay=\"500\"\n" +
    "           tooltip-append-to-body=\"true\"\n" +
    "           data-toggle=\"dropdown\">\n" +
    "          <span class=\"glyphicon glyphicon-arrow-right\"></span>\n" +
    "        </a>\n" +
    "        <ul class=\"dropdown-menu\" role=\"menu\">\n" +
    "          <li ng-repeat=\"nextStageInstance in nextStageInstances\">\n" +
    "            <a href=\"#\" ng-click=\"nextStagePreview(nextStageInstance, stagePreviewData.output)\">\n" +
    "              {{nextStageInstance.uiInfo.label}}\n" +
    "            </a>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "      <a role=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "         ng-if=\"!previewMultipleStages && nextStageInstances.length <= 1\"\n" +
    "         tooltip-placement=\"bottom\" tooltip=\"{{'global.form.nextStage' | translate}}\"\n" +
    "         tooltip-popup-delay=\"500\"\n" +
    "         tooltip-append-to-body=\"true\"\n" +
    "         ng-disabled=\"nextStageInstances.length === 0\"\n" +
    "         ng-click=\"nextStagePreview(nextStageInstances[0], stagePreviewData.output)\">\n" +
    "        <span class=\"glyphicon glyphicon-arrow-right\"></span>\n" +
    "      </a>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right btn-group toggle-toolbar\" ng-show=\"!showLoading\">\n" +
    "      <label class=\"btn btn-default btn-sm\" ng-model=\"listView\" btn-radio=\"true\"\n" +
    "             tooltip-placement=\"bottom\"\n" +
    "             tooltip-append-to-body=\"true\"\n" +
    "             tooltip=\"{{'global.form.listView' | translate}}\">\n" +
    "        <i class=\"fa fa-list fa-12x\"/>\n" +
    "      </label>\n" +
    "      <label class=\"btn btn-default btn-sm\" ng-model=\"listView\" btn-radio=\"false\"\n" +
    "             tooltip-placement=\"bottom\"\n" +
    "             tooltip-append-to-body=\"true\"\n" +
    "             tooltip=\"{{'global.form.tableView' | translate}}\">\n" +
    "        <i class=\"fa fa-table fa-12x\"/>\n" +
    "      </label>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right btn-group toggle-toolbar\" ng-show=\"!showLoading\">\n" +
    "      <label class=\"btn btn-default btn-sm\" ng-model=\"previewMultipleStages\" btn-radio=\"false\">Single</label>\n" +
    "      <label class=\"btn btn-default btn-sm\" ng-model=\"previewMultipleStages\" btn-radio=\"true\">Multiple</label>\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"panel-body\">\n" +
    "\n" +
    "    <tabset class=\"preview-tabs-left tabs-left icon-only\">\n" +
    "\n" +
    "      <tab active=\"recordsTabActive\">\n" +
    "        <tab-heading tooltip-placement=\"right\" tooltip=\"{{ 'global.form.records' | translate }}\">\n" +
    "          <i class=\"fa fa-list-ul fa-12x\"></i>\n" +
    "        </tab-heading>\n" +
    "        <ng-include ng-if=\"recordsTabActive && !previewMultipleStages\" src=\"'app/home/preview/common/previewCommon.tpl.html'\"></ng-include>\n" +
    "        <ng-include ng-if=\"recordsTabActive && previewMultipleStages\" src=\"'app/home/preview/common/previewMultiStage.tpl.html'\"></ng-include>\n" +
    "      </tab>\n" +
    "\n" +
    "      <tab  active=\"errorsTabActive\" ng-if=\"stagePreviewData.stageErrors && stagePreviewData.stageErrors.length\">\n" +
    "        <tab-heading tooltip-placement=\"right\" tooltip=\"{{ 'global.form.errors' | translate }}\">\n" +
    "          <i class=\"fa fa-exclamation-triangle icon-danger fa-12x\"></i>\n" +
    "        </tab-heading>\n" +
    "        <div ng-if=\"errorsTabActive\">\n" +
    "          <table class=\"table table-hover\">\n" +
    "            <caption>\n" +
    "              <label translate=\"global.form.errorMessages\">Error Messages</label>\n" +
    "            </caption>\n" +
    "            <thead>\n" +
    "            <tr>\n" +
    "              <th class=\"col-md-3\" translate=\"global.form.timestamp\">Timestamp</th>\n" +
    "              <th class=\"col-md-2\" translate=\"global.form.errorCode\">Error Code</th>\n" +
    "              <th class=\"col-md-7\" translate=\"global.form.errorMessage\"> Error Message</th>\n" +
    "            </tr>\n" +
    "            </thead>\n" +
    "            <tbody>\n" +
    "            <tr ng-repeat=\"errorMessage in stagePreviewData.stageErrors\">\n" +
    "              <td>{{errorMessage.timestamp | date:'medium'}}</td>\n" +
    "              <td ng-bind=\"errorMessage.errorCode\"> </td>\n" +
    "              <td ng-bind=\"errorMessage.localized\"> </td>\n" +
    "            </tr>\n" +
    "            </tbody>\n" +
    "          </table>\n" +
    "        </div>\n" +
    "      </tab>\n" +
    "\n" +
    "    </tabset>\n" +
    "\n" +
    "  </div>\n" +
    "\n" +
    "</div>");
}]);

angular.module("app/home/stageLibrary/stageLibrary.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/stageLibrary/stageLibrary.tpl.html",
    "<div class=\"stage-library-pane\" ng-controller=\"StageLibraryController\">\n" +
    "  <form class=\"form-group-sm\">\n" +
    "\n" +
    "    <div class=\"stage-filter\">\n" +
    "      <select class=\"form-control\" name=\"stageLibrary\" ng-model=\"$storage.stageFilterGroup\"\n" +
    "              ng-change=\"onStageFilterGroupChange()\"\n" +
    "              ng-options=\"stageGroup.label group by stageGroup.group for stageGroup in stageGroups track by stageGroup.name\">\n" +
    "        <option value=\"\">{{'global.form.allStages' | translate}}</option>\n" +
    "      </select>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"btn-group stage-search\" >\n" +
    "      <input type=\"search\" class=\"form-control\" placeholder=\"Type to search\"\n" +
    "             ng-model=\"searchInput\"\n" +
    "             ng-change=\"onStageFilterGroupChange()\">\n" +
    "      <span class=\"glyphicon glyphicon-remove-circle search-clear\"\n" +
    "            ng-click=\"searchInput = '';onStageFilterGroupChange();\"></span>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"stage-list-container\">\n" +
    "      <ul class=\"stages-list\">\n" +
    "        <li draggable drag-data=\"stageLibrary\"\n" +
    "            ng-repeat=\"stageLibrary in filteredStageLibraries | orderBy: 'label'\"\n" +
    "            ng-init=\"icon=getStageIconURL(stageLibrary)\"\n" +
    "            tooltip-placement=\"{{$index < filteredStageLibraries.length - 2 ? 'bottom' : 'top'}}\"\n" +
    "            tooltip=\"{{stageLibrary.description}}\"\n" +
    "            tooltip-append-to-body=\"true\"\n" +
    "            tooltip-popup-delay=\"500\">\n" +
    "          <a role=\"menuitem\" tabindex=\"-1\" href=\"/\"\n" +
    "             ng-click=\"addStageInstance({stage: stageLibrary}, $event)\"\n" +
    "             ng-style=\"{'background-image': 'url({{icon}})'}\">\n" +
    "            <span>{{stageLibrary.label}}</span>\n" +
    "            <span\n" +
    "              ng-if=\"$storage.stageFilterGroup.group !== 'Type' && stageLibrary.type == 'SOURCE'\"\n" +
    "              class=\"circle origin\">O</span>\n" +
    "\n" +
    "            <span\n" +
    "              ng-if=\"$storage.stageFilterGroup.group !== 'Type' && stageLibrary.type == 'PROCESSOR'\"\n" +
    "              class=\"circle processor\">P</span>\n" +
    "\n" +
    "            <span\n" +
    "              ng-if=\"$storage.stageFilterGroup.group !== 'Type' && stageLibrary.type == 'TARGET'\"\n" +
    "              class=\"circle destination\">D</span>\n" +
    "\n" +
    "            <span\n" +
    "              ng-if=\"$storage.stageFilterGroup.group !== 'Type' && stageLibrary.type == 'EXECUTOR'\"\n" +
    "              class=\"circle executor\">E</span>\n" +
    "          </a>\n" +
    "        </li>\n" +
    "\n" +
    "        <!--<li ng-if=\"isAuthorized([userRoles.admin])\" tooltip-placement=\"top\"\n" +
    "            tooltip=\"{{'packageManager.title' | translate}}\"\n" +
    "            tooltip-append-to-body=\"true\"\n" +
    "            tooltip-popup-delay=\"500\">\n" +
    "          <a role=\"menuitem\" tabindex=\"-1\" href=\"collector/packageManager\"\n" +
    "             ng-style=\"{'background-image': 'url(assets/add.png)'}\">\n" +
    "            <span>{{'global.form.addAndRemoveStages' | translate}}</span>\n" +
    "          </a>\n" +
    "        </li>-->\n" +
    "\n" +
    "      </ul>\n" +
    "    </div>\n" +
    "\n" +
    "  </form>\n" +
    "</div>\n" +
    "");
}]);

angular.module("app/home/usersAndGroups/users_and_groups.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("app/home/usersAndGroups/users_and_groups.tpl.html",
    "<div class=\"panel panel-default page-panel users-groups-page\">\n" +
    "  <div class=\"panel panel-default page-panel\">\n" +
    "    <div class=\"panel-heading clearfix\">\n" +
    "      <div class=\"panel-title \">\n" +
    "        <h3 class=\"pull-left\" translate=\"usersAndGroups.title\">Users And Groups</h3>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"list-content\">\n" +
    "    <ul class=\"list-group checked-list-box\">\n" +
    "\n" +
    "      <li class=\"list-group-item header-list-group-item\">\n" +
    "        <div class=\"user-name header\">\n" +
    "          <span>User Name</span>\n" +
    "        </div>\n" +
    "        <div class=\"user-roles header\">\n" +
    "          <span>Roles</span>\n" +
    "        </div>\n" +
    "        <div class=\"user-groups header\">\n" +
    "          <span>Groups</span>\n" +
    "        </div>\n" +
    "      </li>\n" +
    "\n" +
    "      <li class=\"list-group-item\" ng-repeat=\"user in userList\">\n" +
    "\n" +
    "        <div class=\"user-name\">\n" +
    "          <span>{{user.name}}</span>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"user-roles\">\n" +
    "          <span>{{user.roles.join(', ')}}</span>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"user-groups\">\n" +
    "          <span>{{user.groups.join(', ')}}</span>\n" +
    "        </div>\n" +
    "      </li>\n" +
    "\n" +
    "      <div class=\"stage-libraries-loading\">\n" +
    "        <div class=\"pipeline-home-loading\" show-loading=\"fetching\"></div>\n" +
    "      </div>\n" +
    "\n" +
    "    </ul>\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);
