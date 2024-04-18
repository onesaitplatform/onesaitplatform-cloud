angular.module("templates-common", ["common/administration/applicationToken/applicationToken.tpl.html", "common/administration/connectionLost/connectionLost.tpl.html", "common/administration/createDPMUsers/createDPMUsers.tpl.html", "common/administration/disableDPM/disableDPM.tpl.html", "common/administration/dpmInfo/dpmInfo.tpl.html", "common/administration/enableDPM/enableDPM.tpl.html", "common/administration/enableDPM/restartDPM.tpl.html", "common/administration/jvmMetrics/jvmMetrics.tpl.html", "common/administration/jvmMetrics/settings/settingsModal.tpl.html", "common/administration/jvmMetrics/threadDump/threadDumpModal.tpl.html", "common/administration/logs/logConfig/logConfig.tpl.html", "common/administration/logs/logs.tpl.html", "common/administration/restart/restartModal.tpl.html", "common/administration/sdcConfiguration/sdcConfiguration.tpl.html", "common/administration/sdcDirectories/sdcDirectoriesModal.tpl.html", "common/administration/shutdown/shutdownModal.tpl.html", "common/administration/statsOptIn/statsOptIn.tpl.html", "common/administration/update_permissions/updatePermissions.tpl.html", "common/directives/jsonFormatter/json-formatter.tpl.html", "common/directives/loading/loading.tpl.html", "common/directives/pipelineGraph/deleteOrigin.tpl.html", "common/directives/pipelineGraph/pipelineGraph.tpl.html", "common/directives/recordTree/recordTree.tpl.html", "common/directives/swagger-ui/swagger-ui.tpl.html"]);

angular.module("common/administration/applicationToken/applicationToken.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/applicationToken/applicationToken.tpl.html",
    "<form class=\"application-modal-form\" role=\"form\" ng-submit=\"generateToken()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.applicationToken.headerLabel\">Generate Application Token</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "    <div ng-hide=\"isGeneratingTokenSucceed\">\n" +
    "      <div class=\"alert alert-danger alert-dismissible\" role=\"alert\"\n" +
    "           ng-repeat=\"issue in issues\">\n" +
    "\n" +
    "        <button type=\"button\" class=\"close\" data-dismiss=\"alert\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "          <span class=\"sr-only\">Close</span>\n" +
    "        </button>\n" +
    "        {{issue}}\n" +
    "      </div>\n" +
    "\n" +
    "      <p ng-hide=\"isGeneratingToken\" translate=\"home.applicationToken.confirmationMessage\"></p>\n" +
    "      <p ng-show=\"isGeneratingToken\" translate=\"home.applicationToken.generatingTokenMessage\"></p>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "         ng-show=\"isGeneratingTokenSucceed && !isRestartInProgress\"\n" +
    "         translate=\"home.applicationToken.successMessage\">\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "         ng-show=\"isRestartInProgress\"\n" +
    "         translate=\"home.restart.successMessage\">\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\" ng-hide=\"isRestartInProgress\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\"\n" +
    "            ng-click=\"cancel()\" translate=\"global.form.no\" ng-disabled=\"isGeneratingToken\">No</button>\n" +
    "\n" +
    "    <button type=\"submit\" ng-hide=\"isGeneratingTokenSucceed\"\n" +
    "            class=\"btn btn-primary\" translate=\"global.form.generate\" ng-disabled=\"isGeneratingToken\">Generate</button>\n" +
    "\n" +
    "    <button ng-show=\"isGeneratingTokenSucceed\" ng-click=\"restart()\"\n" +
    "            class=\"btn btn-primary\" translate=\"global.form.restart\">Restart</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("common/administration/connectionLost/connectionLost.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/connectionLost/connectionLost.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <h3 class=\"modal-title\" translate=\"home.connectionLost.headerLabel\">Connection to server lost</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "\n" +
    "  <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "  <p ng-if=\"!isRetryingInProgress\"\n" +
    "     translate=\"home.connectionLost.retryingInSeconds\"\n" +
    "     translate-values=\"{retryCountDown: retryCountDown}\"></p>\n" +
    "\n" +
    "  <p ng-if=\"isRetryingInProgress\">{{'home.connectionLost.retrying' | translate}}</p>\n" +
    "\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"submit\" class=\"btn btn-default\" ng-click=\"refreshBrowser()\" ng-disabled=\"isRetryingInProgress\"\n" +
    "          translate=\"home.connectionLost.refreshBrowser\">Refresh Browser</button>\n" +
    "  <button type=\"submit\" class=\"btn btn-primary\" ng-click=\"retryNow()\" ng-disabled=\"isRetryingInProgress\"\n" +
    "          translate=\"home.connectionLost.retryNow\">Retry Now</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("common/administration/createDPMUsers/createDPMUsers.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/createDPMUsers/createDPMUsers.tpl.html",
    "<form class=\"create-dpm-users-form\" role=\"form\" autocomplete=\"off\" ng-submit=\"onCreateSubjectsSubmit()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.createDPMUsers.title\">Create Control Hub Users</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div ng-if=\"!createResponse\">\n" +
    "\n" +
    "      <div class=\"form-group\">\n" +
    "        <label class=\"control-label\" translate=\"home.enableDPM.dpmBaseURL\">Control Hub URL</label>\n" +
    "        <input type=\"text\"\n" +
    "               autocomplete=\"new-password\"\n" +
    "               ng-disabled=\"isCreateInProgress\"\n" +
    "               name=\"name\"\n" +
    "               class=\"form-control\"\n" +
    "               ng-required\n" +
    "               placeholder=\"{{'home.enableDPM.dpmBaseURLPlaceholder' | translate}}\"\n" +
    "               ng-model=\"dpmInfoModel.baseURL\">\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"form-group\">\n" +
    "        <label class=\"control-label\" translate=\"home.enableDPM.dpmUserName\">Control Hub User Name</label>\n" +
    "        <input type=\"text\"\n" +
    "               autocomplete=\"new-password\"\n" +
    "               ng-disabled=\"isCreateInProgress\"\n" +
    "               autofocus\n" +
    "               name=\"dpmUserID\"\n" +
    "               class=\"form-control\"\n" +
    "               required\n" +
    "               pattern=\"[a-zA-Z_0-9\\.]+@[a-zA-Z_0-9\\.]+$\"\n" +
    "               title=\"User ID must be <ID>@<Organization ID>, IDs must be a alphanumeric characters, underscores and dots\"\n" +
    "               placeholder=\"{{'home.enableDPM.dpmUserNamePlaceholder' | translate}}\"\n" +
    "               ng-model=\"dpmInfoModel.userID\">\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"form-group\">\n" +
    "        <label class=\"control-label\" translate=\"home.enableDPM.dpmUserPassword\">Control Hub User Password</label>\n" +
    "        <input type=\"password\"\n" +
    "               autocomplete=\"new-password\"\n" +
    "               ng-disabled=\"isCreateInProgress\"\n" +
    "               name=\"dpmUserPassword\"\n" +
    "               class=\"form-control\"\n" +
    "               required\n" +
    "               placeholder=\"{{'home.enableDPM.dpmUserPasswordPlaceholder' | translate}}\"\n" +
    "               ng-model=\"dpmInfoModel.userPassword\">\n" +
    "      </div>\n" +
    "\n" +
    "\n" +
    "\n" +
    "      <table class=\"table table-hover\">\n" +
    "        <caption>{{'global.form.groups' | translate}}</caption>\n" +
    "        <thead>\n" +
    "        <tr>\n" +
    "          <th>{{'global.form.groupId' | translate}}</th>\n" +
    "          <th>{{'global.form.userName' | translate}}</th>\n" +
    "          <th></th>\n" +
    "        </tr>\n" +
    "        </thead>\n" +
    "        <tbody ng-hide=\"showLoading\">\n" +
    "        <tr ng-repeat=\"dpmGroup in dpmGroupList track by $index\">\n" +
    "          <td>\n" +
    "            <input type=\"text\"\n" +
    "                   ng-disabled=\"isCreateInProgress\"\n" +
    "                   name=\"dpmGroupID\"\n" +
    "                   class=\"form-control\"\n" +
    "                   ng-required\n" +
    "                   placeholder=\"{{'global.form.groupIdPlaceholder' | translate}}\"\n" +
    "                   ng-model=\"dpmGroup.id\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"text\"\n" +
    "                   ng-disabled=\"isCreateInProgress\"\n" +
    "                   autofocus\n" +
    "                   name=\"dpmGroupName\"\n" +
    "                   class=\"form-control\"\n" +
    "                   required\n" +
    "                   placeholder=\"{{'global.form.userNamePlaceholder' | translate}}\"\n" +
    "                   ng-model=\"dpmGroup.name\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                    ng-click=\"removeFromList(dpmGroupList, $index)\">\n" +
    "              <i class=\"fa fa-minus pointer\"></i>\n" +
    "            </button>\n" +
    "          </td>\n" +
    "        </tr>\n" +
    "        </tbody>\n" +
    "      </table>\n" +
    "\n" +
    "\n" +
    "      <table class=\"table table-hover\">\n" +
    "        <caption>{{'global.form.users' | translate}}</caption>\n" +
    "        <thead>\n" +
    "        <tr>\n" +
    "          <th class=\"subject-col\">{{'global.form.userId' | translate}}</th>\n" +
    "          <th class=\"action-col\">{{'global.form.userName' | translate}}</th>\n" +
    "          <th class=\"action-col\">{{'global.form.email' | translate}}</th>\n" +
    "          <th class=\"action-col\">{{'global.form.groups' | translate}}</th>\n" +
    "          <th class=\"action-menu-col\"></th>\n" +
    "        </tr>\n" +
    "        </thead>\n" +
    "        <tbody ng-hide=\"showLoading\">\n" +
    "        <tr ng-repeat=\"dpmUser in dpmUserList track by $index\">\n" +
    "          <td>\n" +
    "            <input type=\"text\"\n" +
    "                   ng-disabled=\"isCreateInProgress\"\n" +
    "                   name=\"dpmUserID\"\n" +
    "                   class=\"form-control\"\n" +
    "                   ng-required\n" +
    "                   placeholder=\"{{'global.form.userIdPlaceholder' | translate}}\"\n" +
    "                   ng-model=\"dpmUser.id\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"text\"\n" +
    "                   ng-disabled=\"isCreateInProgress\"\n" +
    "                   autofocus\n" +
    "                   name=\"dpmUserName\"\n" +
    "                   class=\"form-control\"\n" +
    "                   required\n" +
    "                   placeholder=\"{{'global.form.userNamePlaceholder' | translate}}\"\n" +
    "                   ng-model=\"dpmUser.name\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <input type=\"text\"\n" +
    "                   ng-disabled=\"isCreateInProgress\"\n" +
    "                   autofocus\n" +
    "                   name=\"dpmUserEmailAddress\"\n" +
    "                   class=\"form-control\"\n" +
    "                   required\n" +
    "                   placeholder=\"{{'global.form.emailPlaceholder' | translate}}\"\n" +
    "                   ng-model=\"dpmUser.email\">\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <ui-select multiple\n" +
    "                       tagging\n" +
    "                       tagging-label=\" - new label\"\n" +
    "                       ng-disabled=\"isEnableInProgress\"\n" +
    "                       tagging-tokens=\",|ENTER\"\n" +
    "                       ng-model=\"dpmUser.groups\">\n" +
    "              <ui-select-match class=\"ui-select-match\">{{$item}}</ui-select-match>\n" +
    "              <ui-select-choices class=\"ui-select-choices\"\n" +
    "                                 repeat=\"listValue in dpmInfoModel.groups | filter:$select.search\">\n" +
    "                <div ng-bind-html=\"listValue | highlight: $select.search\"></div>\n" +
    "              </ui-select-choices>\n" +
    "            </ui-select>\n" +
    "          </td>\n" +
    "\n" +
    "          <td>\n" +
    "            <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                    ng-click=\"removeFromList(dpmUserList, $index)\">\n" +
    "              <i class=\"fa fa-minus pointer\"></i>\n" +
    "            </button>\n" +
    "          </td>\n" +
    "        </tr>\n" +
    "        </tbody>\n" +
    "      </table>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"createResponse && !isRestartInProgress\">\n" +
    "      <div ng-repeat=\"successEntity in createResponse.successEntities\" class=\"alert alert-success alert-dismissible\" role=\"alert\">\n" +
    "        {{successEntity}}\n" +
    "      </div>\n" +
    "\n" +
    "      <div ng-repeat=\"errorMessage in createResponse.errorMessages\" class=\"alert alert-danger alert-dismissible\" role=\"alert\">\n" +
    "        {{errorMessage}}\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "         ng-show=\"isRestartInProgress\"\n" +
    "         translate=\"home.restart.successMessage\">\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\" ng-if=\"!isCreateInProgress\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" ng-if=\"!isCreateInProgress && !createResponse\"\n" +
    "            translate=\"global.form.create\">Create</button>\n" +
    "\n" +
    "    <button class=\"btn btn-primary\" ng-if=\"isCreateInProgress && !createResponse\" disabled\n" +
    "            translate=\"global.form.creating\">Creating...</button>\n" +
    "\n" +
    "    <button ng-if=\"!isCreateInProgress && createResponse && !isRestartInProgress\"\n" +
    "            ng-click=\"restart()\"\n" +
    "            class=\"btn btn-primary\" translate=\"global.form.restart\">Restart</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("common/administration/disableDPM/disableDPM.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/disableDPM/disableDPM.tpl.html",
    "<form class=\"application-modal-form\" role=\"form\" autocomplete=\"off\" ng-submit=\"onDisableDPMSubmit()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.disableDPM.headerLabel\">Enable Control Hub</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div ng-if=\"!dpmDisabled\" translate=\"home.disableDPM.confirmationMessage\">\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"dpmDisabled && !isRestartInProgress\">\n" +
    "      <div class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "         translate=\"home.disableDPM.successMessage\"/>\n" +
    "      <ng-include src=\"'common/administration/enableDPM/restartDPM.tpl.html'\"></ng-include>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "         ng-show=\"isRestartInProgress\"\n" +
    "         translate=\"home.restart.successMessage\">\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\"\n" +
    "            ng-click=\"cancel()\"\n" +
    "            ng-if=\"!isDisableInProgress  && !(dpmDisabled && !isRestartInProgress)\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" ng-if=\"!isDisableInProgress && !dpmDisabled\"\n" +
    "            translate=\"global.form.yes\">Yes</button>\n" +
    "\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" ng-if=\"isDisableInProgress && !dpmDisabled\" disabled\n" +
    "            translate=\"home.disableDPM.isDisableInProgress\">Disabling Control Hub...</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("common/administration/dpmInfo/dpmInfo.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/dpmInfo/dpmInfo.tpl.html",
    "<div class=\"dpm-info\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.dpmInfo.headerLabel\">StreamSets Control Hub</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <p>\n" +
    "      StreamSets Control Hub (SCH &#8482;) helps you view and control\n" +
    "      numerous pipelines from a single point.\n" +
    "    </p>\n" +
    "    <p>\n" +
    "      Control Hub provides release and configuration management for your pipelines and lets you visualize end-to-end aggregated performance metrics from a single dashboard.\n" +
    "    </p>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <a class=\"btn btn-primary\" href=\"https://streamsets.com/dpm-signup\" target=\"_blank\"\n" +
    "       translate=\"home.dpmInfo.learnMore\">Learn More</a>\n" +
    "\n" +
    "    <button class=\"btn btn-primary\" ng-click=\"openEnableDPMModal()\"\n" +
    "            translate=\"home.dpmInfo.enableDPM\">Enable Control Hub</button>\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("common/administration/enableDPM/enableDPM.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/enableDPM/enableDPM.tpl.html",
    "<form class=\"enable-dpm-form\" role=\"form\" autocomplete=\"off\" ng-submit=\"onEnableDPMSubmit()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.enableDPM.headerLabel\">Enable Control Hub</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div ng-if=\"!dpmEnabled\">\n" +
    "      <ul class=\"properties clearfix\">\n" +
    "        <li class=\"pull-right\">\n" +
    "          <span class=\"properties-label\">Don't have an account? <a href=\"https://streamsets.com/dpm-signup\" target=\"_blank\">Click here</a> to learn more. </span>\n" +
    "        </li>\n" +
    "      </ul>\n" +
    "\n" +
    "      <div class=\"form-group\">\n" +
    "        <label class=\"control-label\" translate=\"home.enableDPM.dpmBaseURL\">Control Hub URL</label>\n" +
    "        <input type=\"text\"\n" +
    "               autocomplete=\"new-password\"\n" +
    "               ng-disabled=\"isEnableInProgress\"\n" +
    "               name=\"name\"\n" +
    "               class=\"form-control\"\n" +
    "               ng-required\n" +
    "               placeholder=\"{{'home.enableDPM.dpmBaseURLPlaceholder' | translate}}\"\n" +
    "               ng-model=\"dpmInfoModel.baseURL\">\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"form-group\">\n" +
    "        <label class=\"control-label\" translate=\"home.enableDPM.dpmUserName\">Control Hub User Name</label>\n" +
    "        <input type=\"text\"\n" +
    "               autocomplete=\"new-password\"\n" +
    "               ng-disabled=\"isEnableInProgress\"\n" +
    "               autofocus\n" +
    "               name=\"dpmUserID\"\n" +
    "               class=\"form-control\"\n" +
    "               required\n" +
    "               pattern=\"[a-zA-Z_-0-9\\.]+@[a-zA-Z_-0-9\\.]+$\"\n" +
    "               title=\"User ID must be <ID>@<Organization ID>, IDs must be a alphanumeric characters, underscores, hyphens and dots\"\n" +
    "               placeholder=\"{{'home.enableDPM.dpmUserNamePlaceholder' | translate}}\"\n" +
    "               ng-model=\"dpmInfoModel.userID\">\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"form-group\">\n" +
    "        <label class=\"control-label\" translate=\"home.enableDPM.dpmUserPassword\">Control Hub User Password</label>\n" +
    "        <input type=\"password\"\n" +
    "               autocomplete=\"new-password\"\n" +
    "               ng-disabled=\"isEnableInProgress\"\n" +
    "               name=\"dpmUserPassword\"\n" +
    "               class=\"form-control\"\n" +
    "               required\n" +
    "               placeholder=\"{{'home.enableDPM.dpmUserPasswordPlaceholder' | translate}}\"\n" +
    "               ng-model=\"dpmInfoModel.userPassword\">\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"form-group\">\n" +
    "        <label class=\"control-label\" translate=\"global.form.dataCollectorLabels\">Labels for this Data Collector</label>\n" +
    "\n" +
    "        <ui-select multiple\n" +
    "                   tagging\n" +
    "                   tagging-label=\" - new label\"\n" +
    "                   ng-disabled=\"isEnableInProgress\"\n" +
    "                   tagging-tokens=\",|ENTER\"\n" +
    "                   ng-model=\"dpmInfoModel.labels\">\n" +
    "          <ui-select-match class=\"ui-select-match\">{{$item}}</ui-select-match>\n" +
    "          <ui-select-choices class=\"ui-select-choices\"\n" +
    "                             repeat=\"listValue in dpmInfoModel.labels | filter:$select.search\">\n" +
    "            <div ng-bind-html=\"listValue | highlight: $select.search\"></div>\n" +
    "          </ui-select-choices>\n" +
    "        </ui-select>\n" +
    "\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"(dpmEnabled && !isRestartInProgress)\" >\n" +
    "      <div class=\"alert alert-success alert-dismissible\" role=\"alert\">\n" +
    "        <ul class=\"clearfix\">\n" +
    "          <li>\n" +
    "            <span translate=\"home.enableDPM.successMessage\"></span>\n" +
    "          </li>\n" +
    "          <li>\n" +
    "            <span><a href=\"javascript:;\" ng-click=\"onCreateDPMUsersClick()\">Click here</a>\n" +
    "              to create a Control Hub user account for each Data Collector user account.</span>\n" +
    "          </li>\n" +
    "          <li>\n" +
    "            <span translate=\"home.enableDPM.restartMessage\"></span>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "      <ng-include src=\"'common/administration/enableDPM/restartDPM.tpl.html'\"></ng-include>\n" +
    "    </div>\n" +
    "\n" +
    "    <div ng-if=\"dpmEnabled && !isRestartInProgress && !isStatsLibraryInstalled\"\n" +
    "         class=\"alert alert-info alert-dismissible\"\n" +
    "         role=\"alert\"\n" +
    "         translate=\"home.enableDPM.installStatsLibraryMessage\">\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"alert alert-success alert-dismissible\"\n" +
    "         role=\"alert\"\n" +
    "         ng-show=\"isRestartInProgress\"\n" +
    "         translate=\"home.restart.successMessage\">\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\"\n" +
    "            ng-if=\"!isEnableInProgress && !(dpmEnabled && !isRestartInProgress)\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "\n" +
    "    <button class=\"btn btn-primary\"\n" +
    "            ng-click=\"onInstallStatisticsLibraryClick()\"\n" +
    "            ng-if=\"!isEnableInProgress && dpmEnabled && !isRestartInProgress && !isStatsLibraryInstalled\"\n" +
    "            translate=\"home.enableDPM.installStatistics\">Install Statistics Library</button>\n" +
    "\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\"\n" +
    "            ng-if=\"!isEnableInProgress && !dpmEnabled\"\n" +
    "            translate=\"global.form.enable\">Enable</button>\n" +
    "\n" +
    "    <button class=\"btn btn-primary\"\n" +
    "            ng-if=\"isEnableInProgress && !dpmEnabled\"\n" +
    "            disabled\n" +
    "            translate=\"home.enableDPM.isEnableInProgress\">Enabling Control Hub...</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("common/administration/enableDPM/restartDPM.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/enableDPM/restartDPM.tpl.html",
    "<label>Select how this Data Collector was started to see instructions for restart.</label>\n" +
    "<accordion close-others=\"true\" style=\"min-height:135px;display:block;\">\n" +
    "  <accordion-group is-open=\"status1.open\">\n" +
    "    <accordion-heading>StreamSets Data Collector was started manually\n" +
    "      <i class=\"pull-right glyphicon\"\n" +
    "         ng-class=\"{'glyphicon-chevron-down': status1.open, 'glyphicon-chevron-right': !status1.open}\"></i>\n" +
    "    </accordion-heading>\n" +
    "    <p>Click the button below to restart this Data Collector:</p>\n" +
    "    <button ng-click=\"restart()\"\n" +
    "            class=\"btn btn-primary ng-scope\"\n" +
    "            translate=\"global.form.restart\"></button>\n" +
    "  </accordion-group>\n" +
    "\n" +
    "  <accordion-group is-open=\"status2.open\">\n" +
    "    <accordion-heading>StreamSets Data Collector was started as a service\n" +
    "      <i class=\"pull-right glyphicon\"\n" +
    "         ng-class=\"{'glyphicon-chevron-down': status2.open, 'glyphicon-chevron-right': !status2.open}\"></i>\n" +
    "    </accordion-heading>\n" +
    "    <p>Run this command to restart this Data Collector:</p>\n" +
    "    <p style=\"font-family: monospace;\">\n" +
    "      service sdc restart\n" +
    "    </p>\n" +
    "  </accordion-group>\n" +
    "\n" +
    "  <accordion-group is-open=\"status3.open\">\n" +
    "    <accordion-heading>StreamSets Data Collector was started from Cloudera Manager\n" +
    "      <i class=\"pull-right glyphicon\"\n" +
    "         ng-class=\"{'glyphicon-chevron-down': status3.open, 'glyphicon-chevron-right': !status3.open}\"></i>\n" +
    "    </accordion-heading>\n" +
    "    <p>You must use Cloudera Manager to restart Data Collector. For information about\n" +
    "      how to restart a service through Cloudera Manager, see the Cloudera documentation.</p>\n" +
    "  </accordion-group>\n" +
    "\n" +
    "  <accordion-group is-open=\"status4.open\">\n" +
    "    <accordion-heading>StreamSets Data Collector was started from Docker\n" +
    "      <i class=\"pull-right glyphicon\"\n" +
    "         ng-class=\"{'glyphicon-chevron-down': status4.open, 'glyphicon-chevron-right': !status4.open}\"></i>\n" +
    "    </accordion-heading>\n" +
    "    <p>Run this command to restart this Data Collector:</p>\n" +
    "    <p style=\"font-family: monospace;\">\n" +
    "      docker restart &lt;containerId&gt;\n" +
    "    </p>\n" +
    "  </accordion-group>\n" +
    "</accordion>\n" +
    "");
}]);

angular.module("common/administration/jvmMetrics/jvmMetrics.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/jvmMetrics/jvmMetrics.tpl.html",
    "<div class=\"panel panel-default page-panel jvm-metrics-page\">\n" +
    "\n" +
    "  <div class=\"panel-heading clearfix\">\n" +
    "    <div class=\"panel-title size-toolbar\">\n" +
    "      <h3 class=\"pull-left\" translate=\"jvmMetrics.title\">JVM Metrics</h3>\n" +
    "\n" +
    "      <div class=\"btn-group pull-right jvm-metrics-settings-dropdown\">\n" +
    "        <a class=\"btn btn-link dropdown-toggle\" data-toggle=\"dropdown\"\n" +
    "           tooltip-placement=\"top\"\n" +
    "           tooltip=\"{{'home.header.more' | translate}}\"\n" +
    "           tooltip-popup-delay=\"500\">\n" +
    "          <span class=\"fa fa-ellipsis-h fa-14x pointer\"></span>\n" +
    "        </a>\n" +
    "        <ul class=\"dropdown-menu pull-right\" role=\"menu\">\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"isAuthorized(userRoles.admin)\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"launchThreadDump()\">{{'jvmMetrics.threads.threadDump' | translate}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"launchSettings()\">{{'global.form.settings' | translate}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" class=\"divider\" ng-if=\"!$storage.dontShowRESTResponseMenu\"></li>\n" +
    "\n" +
    "          <li role=\"presentation\" class=\"dropdown-header\"\n" +
    "              ng-if=\"!$storage.dontShowRESTResponseMenu\"\n" +
    "              translate=\"home.detailPane.restURL\">REST URL</li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"!$storage.dontShowRESTResponseMenu\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"/rest/v1/system/jmx\"\n" +
    "               target=\"_blank\">{{'jvmMetrics.title' | translate}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"!$storage.dontShowRESTResponseMenu && isAuthorized(userRoles.admin)\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"/rest/v1/system/threads\"\n" +
    "               target=\"_blank\">{{'jvmMetrics.threads.threadDump' | translate}}</a>\n" +
    "          </li>\n" +
    "\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"panel-body\"  ng-style=\"{'height': (windowHeight - 60 - 51) + 'px', 'width': (windowWidth) + 'px'}\" resize>\n" +
    "    <div class=\"row\">\n" +
    "      <div class=\"col-sm-4\" ng-repeat=\"chart in chartList | filter: filterChart\" ng-init=\"chartOptions=getChartOptions(chart)\">\n" +
    "        <div class=\"panel panel-default\">\n" +
    "          <div class=\"panel-heading\">\n" +
    "            <span ng-bind-html=\"chart.label\"></span>\n" +
    "            <span ng-if=\"chart.name === 'threads' && isAuthorized(userRoles.admin)\"\n" +
    "              > ( <a href=\"#\" ng-click=\"launchThreadDump()\">{{'jvmMetrics.threads.viewThreadDump' | translate}} </a> ) </span>\n" +
    "            <button type=\"button\" class=\"close\" aria-label=\"Close\" ng-click=\"removeChart(chart, chartIndex)\">\n" +
    "              <span aria-hidden=\"true\">&times;</span>\n" +
    "            </button>\n" +
    "          </div>\n" +
    "          <div class=\"panel-body\">\n" +
    "\n" +
    "            <nvd3 options=\"chartOptions\" data=\"chart.values\" config=\"{ refreshDataOnly: false }\"></nvd3>\n" +
    "\n" +
    "            <ul class=\"properties\">\n" +
    "              <li ng-repeat=\"value in chart.values\">\n" +
    "                <span class=\"properties-label\"> {{value.key}}:</span>\n" +
    "                <span class=\"properties-value\">{{formatValue(value.values[value.values.length - 1][1], chart)}}</span>\n" +
    "              </li>\n" +
    "\n" +
    "              <li ng-repeat=\"value in chart.displayProperties\">\n" +
    "                <span class=\"properties-label\"> {{value.key}}:</span>\n" +
    "                <span class=\"properties-value\">{{formatValue(value.value, chart)}}</span>\n" +
    "              </li>\n" +
    "            </ul>\n" +
    "\n" +
    "          </div>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "</div>\n" +
    "");
}]);

angular.module("common/administration/jvmMetrics/settings/settingsModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/jvmMetrics/settings/settingsModal.tpl.html",
    "<form class=\"jvm-metrics-settings-modal-form form\" role=\"form\" ng-submit=\"save()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"jvmMetrics.settingsTitle\">JVM Metrics Settings</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
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

angular.module("common/administration/jvmMetrics/threadDump/threadDumpModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/jvmMetrics/threadDump/threadDumpModal.tpl.html",
    "<form class=\"thread-dump-modal-form form\" role=\"form\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"close()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "\n" +
    "\n" +
    "    <input type=\"search\" class=\"form-control search-thread\" placeholder=\"Type to search\"\n" +
    "           ng-model=\"searchInput\"\n" +
    "           ng-change=\"onStageFilterGroupChange()\">\n" +
    "\n" +
    "\n" +
    "    <h3 class=\"modal-title\" translate=\"jvmMetrics.threads.threadDump\">Thread Dump</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <table class=\"table table-hover\">\n" +
    "      <thead>\n" +
    "        <tr>\n" +
    "          <th class=\"expand-col\">\n" +
    "              <span class=\"pointer toggler {{expandAll ? 'open' : ''}}\"\n" +
    "                    ng-click=\"expandAll = !expandAll\">\n" +
    "              </span>\n" +
    "          </th>\n" +
    "          <th class=\"id-col\">\n" +
    "            <a href=\"#\" ng-click=\"sortColumn = 'threadId'; sortReverse = !sortReverse\">\n" +
    "              {{'jvmMetrics.threads.id' | translate}}\n" +
    "              <span ng-show=\"sortColumn == 'threadId' && !sortReverse\" class=\"fa fa-caret-down\"></span>\n" +
    "              <span ng-show=\"sortColumn == 'threadId' && sortReverse\" class=\"fa fa-caret-up\"></span>\n" +
    "            </a>\n" +
    "          </th>\n" +
    "          <th>\n" +
    "            <a href=\"#\" ng-click=\"sortColumn = 'threadName'; sortReverse = !sortReverse\">\n" +
    "              {{'jvmMetrics.threads.threadName' | translate}}\n" +
    "              <span ng-show=\"sortColumn == 'threadName' && !sortReverse\" class=\"fa fa-caret-down\"></span>\n" +
    "              <span ng-show=\"sortColumn == 'threadName' && sortReverse\" class=\"fa fa-caret-up\"></span>\n" +
    "            </a>\n" +
    "          </th>\n" +
    "          <th>\n" +
    "            <a href=\"#\" ng-click=\"sortColumn = 'threadState'; sortReverse = !sortReverse\">\n" +
    "              {{'jvmMetrics.threads.state' | translate}}\n" +
    "              <span ng-show=\"sortColumn == 'threadState' && !sortReverse\" class=\"fa fa-caret-down\"></span>\n" +
    "              <span ng-show=\"sortColumn == 'threadState' && sortReverse\" class=\"fa fa-caret-up\"></span>\n" +
    "            </a>\n" +
    "          </th>\n" +
    "          <th>\n" +
    "            <a href=\"#\" ng-click=\"sortColumn = 'userTimeNanosecs'; sortReverse = !sortReverse\">\n" +
    "              {{'jvmMetrics.threads.userTime' | translate}}\n" +
    "              <span ng-show=\"sortColumn == 'userTimeNanosecs' && !sortReverse\" class=\"fa fa-caret-down\"></span>\n" +
    "              <span ng-show=\"sortColumn == 'userTimeNanosecs' && sortReverse\" class=\"fa fa-caret-up\"></span>\n" +
    "            </a>\n" +
    "          </th>\n" +
    "          <th>\n" +
    "            <a href=\"#\" ng-click=\"sortColumn = 'cpuTimeNanosecs'; sortReverse = !sortReverse\">\n" +
    "              {{'jvmMetrics.threads.cpuTime' | translate}}\n" +
    "              <span ng-show=\"sortColumn == 'cpuTimeNanosecs' && !sortReverse\" class=\"fa fa-caret-down\"></span>\n" +
    "              <span ng-show=\"sortColumn == 'cpuTimeNanosecs' && sortReverse\" class=\"fa fa-caret-up\"></span>\n" +
    "            </a>\n" +
    "          </th>\n" +
    "          <th>\n" +
    "            <a href=\"#\" ng-click=\"sortColumn = 'blockedCount'; sortReverse = !sortReverse\">\n" +
    "              {{'jvmMetrics.threads.blockedCount' | translate}}\n" +
    "              <span ng-show=\"sortColumn == 'blockedCount' && !sortReverse\" class=\"fa fa-caret-down\"></span>\n" +
    "              <span ng-show=\"sortColumn == 'blockedCount' && sortReverse\" class=\"fa fa-caret-up\"></span>\n" +
    "            </a>\n" +
    "          </th>\n" +
    "          <th>\n" +
    "            <a href=\"#\" ng-click=\"sortColumn = 'waitedCount'; sortReverse = !sortReverse\">\n" +
    "              {{'jvmMetrics.threads.waitedCount' | translate}}\n" +
    "              <span ng-show=\"sortColumn == 'waitedCount' && !sortReverse\" class=\"fa fa-caret-down\"></span>\n" +
    "              <span ng-show=\"sortColumn == 'waitedCount' && sortReverse\" class=\"fa fa-caret-up\"></span>\n" +
    "            </a>\n" +
    "          </th>\n" +
    "        </tr>\n" +
    "      </thead>\n" +
    "\n" +
    "      <tbody>\n" +
    "        <tr class=\"pointer\"\n" +
    "            ng-repeat-start=\"thread in threads | orderBy:sortColumn:sortReverse | filter: searchInput\"\n" +
    "            ng-init=\"isOpen=false;\"\n" +
    "            ng-click=\"isOpen = !isOpen\">\n" +
    "          <td>\n" +
    "            <span class=\"pointer toggler {{isOpen || expandAll ? 'open' : ''}}\"\n" +
    "                  ng-click=\"isOpen = !isOpen; $event.stopPropagation()\"></span>\n" +
    "          </td>\n" +
    "          <td>{{thread.threadId}}</td>\n" +
    "          <td>{{thread.threadName}}</td>\n" +
    "          <td>{{thread.threadState}}</td>\n" +
    "\n" +
    "          <td>{{thread.userTimeNanosecs}}</td>\n" +
    "          <td>{{thread.cpuTimeNanosecs}}</td>\n" +
    "          <td>{{thread.blockedCount}}</td>\n" +
    "          <td>{{thread.waitedCount}}</td>\n" +
    "        </tr>\n" +
    "\n" +
    "        <tr ng-repeat-end ng-if=\"isOpen || expandAll\" class=\"expand-row\">\n" +
    "          <td colspan=\"8\" class=\"stack-trace-row\">\n" +
    "            <span class=\"properties-label\">{{'jvmMetrics.threads.stackTrace' | translate}}:</span>\n" +
    "            <ul>\n" +
    "              <li ng-repeat=\"line in thread.threadInfo.stackTrace\">\n" +
    "                <span>at</span>\n" +
    "                <span>{{line.className}}.{{line.methodName}}</span>\n" +
    "                <span>({{line.fileName}}:{{line.lineNumber}})</span>\n" +
    "              </li>\n" +
    "            </ul>\n" +
    "          </td>\n" +
    "        </tr>\n" +
    "      </tbody>\n" +
    "\n" +
    "    </table>\n" +
    "\n" +
    "    <div show-loading=\"showLoading\"></div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\" class=\"btn btn-primary\" ng-click=\"refresh()\" translate=\"global.form.refresh\">\n" +
    "      Refresh\n" +
    "    </button>\n" +
    "    <button type=\"reset\" class=\"btn btn-primary\" ng-click=\"close()\" translate=\"global.form.close\">\n" +
    "      Close\n" +
    "    </button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("common/administration/logs/logConfig/logConfig.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/logs/logConfig/logConfig.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "    <span aria-hidden=\"true\">&times;</span>\n" +
    "    <span class=\"sr-only\">Close</span>\n" +
    "  </button>\n" +
    "  <h3 class=\"modal-title\" translate=\"logs.logConfig.headerLabel\">Log Config</h3>\n" +
    "</div>\n" +
    "<div class=\"log-config-modal-body\">\n" +
    "  <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "  <div show-loading=\"showLoading\"></div>\n" +
    "\n" +
    "  <div class=\"form-group\" ng-hide=\"showLoading\">\n" +
    "    <div ui-codemirror\n" +
    "         class=\"codemirror-editor\"\n" +
    "         ng-model=\"logConfig\"\n" +
    "         ui-refresh=\"refreshCodemirror\"\n" +
    "         ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "              mode: {\n" +
    "                name: 'properties'\n" +
    "              },\n" +
    "              lineNumbers: true\n" +
    "            })\"\n" +
    "         field-paths=\"fieldPaths\">\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"reset\" class=\"btn btn-default pull-left\"\n" +
    "          ng-click=\"reset()\" translate=\"global.form.reset\">Reset</button>\n" +
    "\n" +
    "  <button type=\"reset\" class=\"btn btn-default\"\n" +
    "          ng-click=\"cancel()\" translate=\"global.form.cancel\">Cancel</button>\n" +
    "\n" +
    "  <button ng-click=\"save()\"\n" +
    "          class=\"btn btn-primary\" translate=\"global.form.save\">Save</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("common/administration/logs/logs.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/logs/logs.tpl.html",
    "<div class=\"panel panel-default page-panel logs-page\">\n" +
    "\n" +
    "  <div class=\"panel-heading clearfix\">\n" +
    "    <div class=\"panel-title pull-left\">\n" +
    "      <h3 translate=\"logs.title\">Logs</h3>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-left filter-toolbar\">\n" +
    "\n" +
    "      <span class=\"sort-by-label\">Severity:</span>\n" +
    "      <div class=\"btn-group severity-dropdown\">\n" +
    "\n" +
    "        <button class=\"btn btn-link dropdown-toggle icon-button\" type=\"button\" data-toggle=\"dropdown\">\n" +
    "          <span ng-if=\"!filterSeverity\" translate=\"logs.all\">All</span>\n" +
    "          <span ng-if=\"filterSeverity\">{{filterSeverity}}</span>\n" +
    "        </button>\n" +
    "\n" +
    "        <ul class=\"dropdown-menu\" role=\"menu\" aria-labelledby=\"dropdownMenu1\">\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"severityFilterChanged(undefined);\">\n" +
    "              <span translate=\"logs.all\">All</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"severityFilterChanged('INFO');\">\n" +
    "              <span>INFO</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"severityFilterChanged('DEBUG');\">\n" +
    "              <span>DEBUG</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"severityFilterChanged('WARN');\">\n" +
    "              <span>WARN</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" href=\"#\"\n" +
    "               ng-click=\"severityFilterChanged('ERROR');\">\n" +
    "              <span>ERROR</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "\n" +
    "      <span class=\"sort-by-label\" ng-if=\"pipelines.length\">Pipeline:</span>\n" +
    "      <div class=\"btn-group pipeline-dropdown\" ng-if=\"pipelines.length\">\n" +
    "\n" +
    "        <button class=\"btn btn-link dropdown-toggle icon-button\" type=\"button\" data-toggle=\"dropdown\">\n" +
    "          <span ng-if=\"!filterPipeline\" translate=\"logs.all\">All</span>\n" +
    "          <span ng-if=\"filterPipeline\">{{filterPipelineLabel}}</span>\n" +
    "        </button>\n" +
    "\n" +
    "        <ul class=\"dropdown-menu\" role=\"menu\"\n" +
    "            aria-labelledby=\"dropdownMenu1\">\n" +
    "\n" +
    "          <li role=\"presentation\" ng-if=\"common.isUserAdmin\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" ng-href=\"/collector/logs\">\n" +
    "              <span translate=\"logs.all\">All</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "          <li role=\"presentation\" ng-repeat=\"pipeline in pipelines\">\n" +
    "            <a role=\"menuitem\" tabindex=\"-1\" ng-href=\"/collector/logs/{{pipeline.title}}/{{pipeline.name}}\">\n" +
    "              <span>{{pipeline.title}}/{{pipeline.name}}</span>\n" +
    "            </a>\n" +
    "          </li>\n" +
    "\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "      <a ng-if=\"filterPipeline\" ng-href=\"/collector/pipeline/{{filterPipeline}}\">( view pipeline... )</a>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"pull-right\">\n" +
    "      <button type=\"button\"\n" +
    "              ng-if=\"logEndingOffset !== 0\"\n" +
    "              class=\"btn btn-primary btn-sm\"\n" +
    "              ng-disabled=\"fetchingLog\"\n" +
    "              ng-click=\"loadPreviousLog()\">\n" +
    "        <span ng-hide=\"fetchingLog\">\n" +
    "          {{'logs.loadPreviousLog' | translate}}\n" +
    "        </span>\n" +
    "\n" +
    "        <span ng-show=\"fetchingLog\">\n" +
    "          {{'logs.loadingLog' | translate}}\n" +
    "        </span>\n" +
    "      </button>\n" +
    "\n" +
    "      <div class=\"btn-group download-btn-group\" ng-if=\"logFiles && logFiles.length && common.isUserAdmin\">\n" +
    "        <a href=\"/rest/v1/system/logs/files/{{logFiles[0].file}}?attachment=true\" target=\"_self\"\n" +
    "           class=\"btn btn-primary btn-sm\">{{'logs.download' | translate}}</a>\n" +
    "        <button type=\"button\" class=\"btn btn-primary btn-sm dropdown-toggle\" data-toggle=\"dropdown\" aria-expanded=\"false\">\n" +
    "          <span class=\"caret\"></span>\n" +
    "          <span class=\"sr-only\">Toggle Dropdown</span>\n" +
    "        </button>\n" +
    "        <ul class=\"dropdown-menu pull-right\" role=\"menu\">\n" +
    "          <li ng-repeat=\"logFile in logFiles\">\n" +
    "            <a href=\"/rest/v1/system/logs/files/{{logFile.file}}?attachment=true\" target=\"_self\">{{logFile.file}}</a>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "\n" +
    "      <button type=\"button\" class=\"btn btn-primary btn-sm\"  ng-if=\"common.isUserAdmin\" ng-click=\"onLogConfigClick()\">\n" +
    "        {{'logs.logConfig.headerLabel' | translate}}\n" +
    "      </button>\n" +
    "\n" +
    "      <button type=\"button\" class=\"btn btn-primary btn-sm\"  ng-if=\"common.isUserAdmin\" ng-click=\"toggleAutoFetch()\">\n" +
    "        {{(pauseLogAutoFetch ? 'logs.autoFetchContinue' : 'logs.autoFetchPause') | translate}}\n" +
    "      </button>\n" +
    "\n" +
    "      <button type=\"button\" class=\"btn btn-primary btn-sm\"\n" +
    "              ng-click=\"refreshLogs()\">{{'global.form.refresh' | translate}}</button>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "\n" +
    "  <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "  <ng-include src=\"'app/home/alerts/error/errorModal.tpl.html'\"></ng-include>\n" +
    "\n" +
    "  <div class=\"panel-body\"\n" +
    "       ng-style=\"{'height': (windowHeight - 60 - 51) + 'px', 'width': (windowWidth) + 'px'}\" resize>\n" +
    "\n" +
    "    <table class=\"table log-table\" ng-if=\"!loading\">\n" +
    "      <thead>\n" +
    "      <tr>\n" +
    "        <th>Timestamp</th>\n" +
    "        <th>Pipeline</th>\n" +
    "        <th>Severity</th>\n" +
    "        <th>Message</th>\n" +
    "        <th>Category</th>\n" +
    "        <th>User</th>\n" +
    "        <th>Runner</th>\n" +
    "        <th>Thread</th>\n" +
    "      </tr>\n" +
    "      </thead>\n" +
    "\n" +
    "      <tbody>\n" +
    "\n" +
    "      <tr ng-if=\"logMessages.length && logEndingOffset !== 0\">\n" +
    "        <td colspan=\"7\"> ........................................................................................................................................................................................................................................................................................</td>\n" +
    "      </tr>\n" +
    "\n" +
    "      <tr ng-repeat-start=\"logMessage in logMessages track by $index\" scroll-to-bottom=\"logMessages\">\n" +
    "        <td>{{logMessage.timestamp}}</td>\n" +
    "        <td>{{logMessage['s-entity']}}</td>\n" +
    "        <td>{{logMessage.severity}}</td>\n" +
    "        <td>{{logMessage.message}}</td>\n" +
    "        <td>{{logMessage.category}}</td>\n" +
    "        <td>{{logMessage['s-user']}}</td>\n" +
    "        <td>{{logMessage['s-runner']}}</td>\n" +
    "        <td>{{logMessage.thread}}</td>\n" +
    "      </tr>\n" +
    "\n" +
    "      <tr ng-repeat-end ng-if=\"logMessage.exception\">\n" +
    "        <td colspan=\"7\">\n" +
    "          <div class=\"log-exception\">\n" +
    "            <span>{{logMessage.exception}}</span>\n" +
    "          </div>\n" +
    "        </td>\n" +
    "      </tr>\n" +
    "\n" +
    "      <tr ng-if=\"logMessages.length == 0\">\n" +
    "        <td colspan=\"7\">{{'logs.noLogsMessage' | translate}}</td>\n" +
    "      </tr>\n" +
    "\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "\n" +
    "\n" +
    "    <div class=\"stage-libraries-loading\">\n" +
    "      <div class=\"pipeline-home-loading\" show-loading=\"loading\"></div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("common/administration/restart/restartModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/restart/restartModal.tpl.html",
    "<form class=\"restart-modal-form\" role=\"form\" ng-submit=\"restart()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.restart.headerLabel\">Restart Data Collector</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "    <div ng-hide=\"isRestartSucceed\">\n" +
    "      <div class=\"alert alert-danger alert-dismissible\" role=\"alert\"\n" +
    "           ng-repeat=\"issue in issues\">\n" +
    "\n" +
    "        <button type=\"button\" class=\"close\" data-dismiss=\"alert\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "          <span class=\"sr-only\">Close</span>\n" +
    "        </button>\n" +
    "        {{issue}}\n" +
    "      </div>\n" +
    "\n" +
    "      <p ng-show=\"isRestarting\" translate=\"home.restart.restartingMessage\"></p>\n" +
    "\n" +
    "      <ng-include src=\"'common/administration/enableDPM/restartDPM.tpl.html'\"></ng-include>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "         ng-show=\"isRestartSucceed\"\n" +
    "         translate=\"home.restart.successMessage\">\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("common/administration/sdcConfiguration/sdcConfiguration.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/sdcConfiguration/sdcConfiguration.tpl.html",
    "<div class=\"panel panel-default page-panel sdc-configuration-page\">\n" +
    "\n" +
    "  <div class=\"panel-heading clearfix\">\n" +
    "    <div class=\"panel-title\">\n" +
    "      <h3 translate=\"sdcConfiguration.title\">Data Collector Configuration</h3>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "\n" +
    "  <div class=\"panel-body\"  ng-style=\"{'height': (windowHeight - 60 - 51) + 'px', 'width': (windowWidth) + 'px'}\" resize>\n" +
    "\n" +
    "    <div class=\"alert alert-info\" role=\"alert\">\n" +
    "      <i class=\"fa fa-info-circle\"></i>\n" +
    "      <span translate=\"sdcConfiguration.infoMessage\"></span>\n" +
    "    </div>\n" +
    "\n" +
    "    <table class=\"table table-striped table-hover\">\n" +
    "      <thead>\n" +
    "        <tr>\n" +
    "          <th translate=\"sdcConfiguration.configuration\">Configuration</th>\n" +
    "          <th translate=\"sdcConfiguration.value\">Value</th>\n" +
    "        </tr>\n" +
    "      </thead>\n" +
    "      <tbody>\n" +
    "        <tr ng-repeat=\"configKey in configKeys\">\n" +
    "          <td><label>{{configKey}}</label></td>\n" +
    "          <td>{{sdcConfiguration[configKey]}}</td>\n" +
    "        </tr>\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "\n" +
    "\n" +
    "  </div>\n" +
    "\n" +
    "</div>");
}]);

angular.module("common/administration/sdcDirectories/sdcDirectoriesModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/sdcDirectories/sdcDirectoriesModal.tpl.html",
    "<form class=\"sdc-directories-modal-form\" role=\"form\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"close()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"sdcDirectories.title\">SDC Directories</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "\n" +
    "    <div class=\"form-group\" ng-repeat=\"(key, value) in sdcDirectories\">\n" +
    "      <label class=\"control-label\">{{('sdcDirectories.' + key) | translate}}</label>\n" +
    "      <div class=\"controls\">\n" +
    "        <textarea readonly type=\"text\" class=\"form-control\" name=\"{{key}}\"\n" +
    "               ng-model=\"value\">\n" +
    "        </textarea>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" translate=\"global.form.close\" ng-click=\"close()\">\n" +
    "      Close\n" +
    "    </button>\n" +
    "  </div>\n" +
    "</form>");
}]);

angular.module("common/administration/shutdown/shutdownModal.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/shutdown/shutdownModal.tpl.html",
    "<form class=\"shutdown-modal-form\" role=\"form\" ng-submit=\"shutdown()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.shutdown.headerLabel\">Shutdown Collector</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "    <div ng-hide=\"isShutdownSucceed\">\n" +
    "      <div class=\"alert alert-danger alert-dismissible\" role=\"alert\"\n" +
    "           ng-repeat=\"issue in issues\">\n" +
    "\n" +
    "        <button type=\"button\" class=\"close\" data-dismiss=\"alert\">\n" +
    "          <span aria-hidden=\"true\">&times;</span>\n" +
    "          <span class=\"sr-only\">Close</span>\n" +
    "        </button>\n" +
    "        {{issue}}\n" +
    "      </div>\n" +
    "\n" +
    "      <p ng-hide=\"isShuttingDown\" translate=\"home.shutdown.confirmationMessage\"></p>\n" +
    "      <p ng-show=\"isShuttingDown\" translate=\"home.shutdown.shuttingDown\"></p>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"alert alert-danger alert-dismissible\" role=\"alert\"\n" +
    "         ng-show=\"isShutdownSucceed\"\n" +
    "         translate=\"home.shutdown.successMessage\">\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\" ng-hide=\"isShutdownSucceed\">\n" +
    "    <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\" translate=\"global.form.no\" ng-disabled=\"isShuttingDown\">\n" +
    "      No\n" +
    "    </button>\n" +
    "    <button type=\"submit\" class=\"btn btn-primary\" translate=\"global.form.yes\" ng-disabled=\"isShuttingDown\">\n" +
    "      Yes\n" +
    "    </button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("common/administration/statsOptIn/statsOptIn.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/statsOptIn/statsOptIn.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "    <span aria-hidden=\"true\">&times;</span>\n" +
    "    <span class=\"sr-only\">Close</span>\n" +
    "  </button>\n" +
    "  <h3 class=\"modal-title\" translate=\"home.statsOptIn.headerTitle\">Improve Data Collector with Usage Statistics</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "  <p> StreamSets would like to collect anonymized usage statistics about features used, system performance, etc.\n" +
    "    This information helps us understand how users actually use the product in order to improve product performance and\n" +
    "    guide decisions regarding future development of the product. As with all development efforts, improvements to\n" +
    "    open source products help the whole community.\n" +
    "  </p>\n" +
    "\n" +
    "  <p>StreamSets is committed to protecting user privacy. Collection of these usage statistics will not include the\n" +
    "    collection of any sensitive user information, such as stage configuration details, pipeline names, credentials, etc.\n" +
    "    No personally identifiable information will be collected. StreamSets will use these anonymized usage statistics for\n" +
    "    its own internal development of the product and support of its users. Information about usage statistics may be\n" +
    "    shared with the community on an anonymized aggregated basis in the form of blog posts and other published\n" +
    "    information about how the product is commonly used.\n" +
    "  </p>\n" +
    "\n" +
    "  <div ng-if=\"!isLoading\">\n" +
    "    <div class=\"checkbox\">\n" +
    "      <label>\n" +
    "        <input type=\"checkbox\" ng-model=\"currentStatus.active\"> {{'home.statsOptIn.optInLabel' | translate}}\n" +
    "      </label>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"reset\" class=\"btn btn-default\" ng-click=\"cancel()\"\n" +
    "          translate=\"global.form.later\">Later</button>\n" +
    "  <button class=\"btn btn-primary\" ng-click=\"save()\"\n" +
    "          translate=\"global.form.save\">Save</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("common/administration/update_permissions/updatePermissions.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/administration/update_permissions/updatePermissions.tpl.html",
    "<form class=\"update-permissions-modal-form\" role=\"form\" ng-submit=\"updatePermissions()\">\n" +
    "  <div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" ng-click=\"cancel()\">\n" +
    "      <span aria-hidden=\"true\">&times;</span>\n" +
    "      <span class=\"sr-only\">Close</span>\n" +
    "    </button>\n" +
    "    <h3 class=\"modal-title\" translate=\"home.library.transferPermissions\">Transfer Permissions</h3>\n" +
    "  </div>\n" +
    "  <div class=\"modal-body\">\n" +
    "    <ng-include src=\"'app/home/alerts/error/errorAlert.tpl.html'\"></ng-include>\n" +
    "\n" +
    "    <div ng-if=\"operationStatus !== 'Completed'\">\n" +
    "      <div class=\"row map-list-type\"\n" +
    "           ng-if=\"!bulkEdit\"\n" +
    "           ng-repeat=\"mapObject in subjectMapping.value track by $index\">\n" +
    "\n" +
    "        <div class=\"col-xs-5\">\n" +
    "          <input type=\"text\" class=\"form-control input-sm\"\n" +
    "                 name=\"mapObject{{$index}}\"\n" +
    "                 placeholder=\"Enter User or Group Name\"\n" +
    "                 ng-required=\"true\"\n" +
    "                 ng-model=\"mapObject.from\"/>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"col-xs-1\">==></div>\n" +
    "\n" +
    "        <div class=\"col-xs-5\">\n" +
    "          <select class=\"form-control input-sm\"\n" +
    "                  name=\"mapObject{{$index}}\"\n" +
    "                  ng-model=\"mapObject.to\">\n" +
    "            <optgroup label=\"{{'global.form.groups' | translate}}\">\n" +
    "              <option ng-repeat=\"listValue in groupList\"\n" +
    "                      value=\"{{listValue}}\"\n" +
    "                      ng-selected=\"listValue === mapObject.to\">{{listValue}}</option>\n" +
    "            </optgroup>\n" +
    "            <optgroup label=\"{{'global.form.users' | translate}}\">\n" +
    "              <option ng-repeat=\"listValue in userList\"\n" +
    "                      value=\"{{listValue}}\"\n" +
    "                      ng-selected=\"listValue === mapObject.to\">{{listValue}}</option>\n" +
    "            </optgroup>\n" +
    "          </select>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"btn-group\" role=\"group\" aria-label=\"Default button group\">\n" +
    "          <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                  ng-click=\"removeFromMap($index)\">\n" +
    "            <i class=\"fa fa-minus pointer\"></i>\n" +
    "          </button>\n" +
    "          <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                  ng-show=\"$last\"\n" +
    "                  ng-click=\"addToMap()\">\n" +
    "            <i class=\"fa fa-plus pointer\"></i>\n" +
    "          </button>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "      <div ui-codemirror\n" +
    "           ng-if=\"bulkEdit\"\n" +
    "           class=\"codemirror-editor\"\n" +
    "           ng-model=\"subjectMapping.value\"\n" +
    "           ui-refresh=\"refreshCodemirror\"\n" +
    "           ui-codemirror-opts=\"getCodeMirrorOptions({\n" +
    "              lineNumbers: true\n" +
    "            })\"\n" +
    "           data-type=\"'LIST'\">\n" +
    "      </div>\n" +
    "\n" +
    "      <div ng-if=\"subjectMapping.value.length === 0 && !bulkEdit\" class=\"row\">\n" +
    "        <div class=\"col-md-2 btn-group\" role=\"group\">\n" +
    "          <button type=\"button\" class=\"btn btn-default btn-sm\"\n" +
    "                  ng-click=\"addToMap()\">\n" +
    "            <i class=\"fa fa-plus pointer\"></i>\n" +
    "          </button>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "      <div class=\"pull-right field-selector-btn\">\n" +
    "        <button type=\"button\" class=\"btn btn-link\"\n" +
    "                ng-click=\"bulkEdit = !bulkEdit\">\n" +
    "          {{bulkEdit ? ('home.detailPane.configurationTab.switchToSimpleMode' | translate) : ('home.detailPane.configurationTab.switchToBulkMode' | translate)}}\n" +
    "        </button>\n" +
    "      </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"alert alert-success alert-dismissible\" role=\"alert\"\n" +
    "         ng-if=\"operationStatus === 'Completed'\"\n" +
    "         translate=\"home.library.updatePermissionsSuccessMessage\">\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "  <div class=\"modal-footer\">\n" +
    "    <button type=\"reset\"  ng-if=\"operationStatus !== 'Completed'\"\n" +
    "            ng-disabled=\"operationStatus === 'Updating'\"\n" +
    "            class=\"btn btn-default\" ng-click=\"cancel()\"\n" +
    "            translate=\"global.form.cancel\">Cancel</button>\n" +
    "\n" +
    "    <button type=\"reset\"  ng-if=\"operationStatus === 'Completed'\"\n" +
    "            class=\"btn btn-default\" ng-click=\"cancel()\"\n" +
    "            translate=\"global.form.close\">Close</button>\n" +
    "\n" +
    "    <button type=\"submit\"  ng-if=\"operationStatus !== 'Completed'\" class=\"btn btn-primary\"\n" +
    "            ng-disabled=\"operationStatus === 'Updating'\"\n" +
    "            translate=\"global.form.update\">Update</button>\n" +
    "  </div>\n" +
    "</form>\n" +
    "");
}]);

angular.module("common/directives/jsonFormatter/json-formatter.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/directives/jsonFormatter/json-formatter.tpl.html",
    "<div ng-init=\"isOpen = open && open > 0\" class=\"json-formatter-row\">\n" +
    "  <a ng-click=\"toggleOpen()\">\n" +
    "    <span class=\"toggler {{isOpen ? 'open' : ''}}\" ng-if=\"isObject\"></span>\n" +
    "    <span class=\"key\" ng-if=\"hasKey\">{{key}}:</span>\n" +
    "    <span class=\"value\">\n" +
    "      <span ng-if=\"isObject\">\n" +
    "        <span class=\"constructor-name\">{{getConstructorName(json)}}</span>\n" +
    "        <span ng-if=\"isArray()\"><span class=\"bracket\">[</span><span class=\"number\">{{json.length}}</span><span class=\"bracket\">]</span></span>\n" +
    "      </span>\n" +
    "      <span ng-if=\"!isObject\" ng-click=\"openLink(isUrl)\" class=\"{{type}}\" ng-class=\"{date: isDate, url: isUrl}\">{{parseValue(json)}}</span>\n" +
    "    </span>\n" +
    "  </a>\n" +
    "  <div class=\"children\" ng-if=\"getKeys().length && isOpen\">\n" +
    "    <json-formatter ng-repeat=\"key in getKeys()\" json=\"json[key]\" key=\"key\" open=\"childrenOpen()\"></json-formatter>\n" +
    "  </div>\n" +
    "  <div class=\"children empty object\" ng-if=\"isEmptyObject()\"></div>\n" +
    "  <div class=\"children empty array\" ng-if=\"getKeys() && !getKeys().length && isOpen && isArray()\"></div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("common/directives/loading/loading.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/directives/loading/loading.tpl.html",
    "<div class=\"loading-screen-display-box\" ng-show=\"loading\">\n" +
    "  <i class=\"fa fa-spinner fa-spin fa-2x\"></i>\n" +
    "  <div style=\"margin-top: 10px;\">\n" +
    "	    <span class=\"loading-screen-message\">\n" +
    "	      {{'global.form.loading' | translate}}...\n" +
    "	    </span>\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("common/directives/pipelineGraph/deleteOrigin.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/directives/pipelineGraph/deleteOrigin.tpl.html",
    "<div class=\"modal-header\">\n" +
    "  <button type=\"button\" class=\"close\" ng-click=\"no()\">\n" +
    "    <span aria-hidden=\"true\">&times;</span>\n" +
    "    <span class=\"sr-only\">Close</span>\n" +
    "  </button>\n" +
    "  <h3 class=\"modal-title\" translate=\"global.form.deleteOriginConfirmation\">Delete Origin Confirmation</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "  <p ng-if=\"!isList\"\n" +
    "    translate=\"global.messages.info.deleteOriginConfirmationMsg\" translate-values=\"{name: pipelineInfo.name}\"></p>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button type=\"button\" class=\"btn btn-default\" ng-click=\"no()\" translate=\"global.form.no\">No</button>\n" +
    "  <button type=\"button\" class=\"btn btn-primary\" ng-click=\"yes()\" translate=\"global.form.yes\">Yes</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("common/directives/pipelineGraph/pipelineGraph.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/directives/pipelineGraph/pipelineGraph.tpl.html",
    "<div class=\"graph-container\">\n" +
    "  <svg width=\"100%\" height=\"100%\" tabindex=\"0\">\n" +
    "  </svg>\n" +
    "\n" +
    "\n" +
    "  <div class=\"graph-toolbar\">\n" +
    "\n" +
    "    <div class=\"pan-toolbar\">\n" +
    "      <div>\n" +
    "        <span class=\"glyphicon glyphicon-chevron-up pan-up\" ng-click=\"panUp($event)\">\n" +
    "        </span>\n" +
    "      </div>\n" +
    "      <div>\n" +
    "        <span class=\"glyphicon glyphicon-chevron-right pan-right\" ng-click=\"panRight($event)\"></span>\n" +
    "      </div>\n" +
    "      <div>\n" +
    "        <span class=\"glyphicon glyphicon-home pan-home\" ng-click=\"panHome($event)\"></span>\n" +
    "      </div>\n" +
    "      <div>\n" +
    "        <span class=\"glyphicon glyphicon-chevron-left pan-left\" ng-click=\"panLeft($event)\"></span>\n" +
    "      </div>\n" +
    "      <div>\n" +
    "        <span class=\"glyphicon glyphicon-chevron-down pan-down\" ng-click=\"panDown($event)\"></span>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"zoom-toolbar\">\n" +
    "      <div>\n" +
    "        <span class=\"pointer fa fa-plus\" ng-click=\"zoomIn($event)\"></span>\n" +
    "      </div>\n" +
    "      <div>\n" +
    "        <span class=\"pointer fa fa-minus\" ng-click=\"zoomOut($event)\"></span>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "\n" +
    "  <span class=\"warning-toolbar node-warning fa fa-exclamation-triangle graph-bootstrap-tooltip\"\n" +
    "        ng-click=\"onWarningClick($event)\"\n" +
    "        data-html=\"true\" data-placement=\"right\" style=\"visibility: hidden;\">\n" +
    "  </span>\n" +
    "\n" +
    "</div>");
}]);

angular.module("common/directives/recordTree/recordTree.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/directives/recordTree/recordTree.tpl.html",
    "<ul class=\"record-list\" ng-init=\"isOpen=false;headerExpand=false\">\n" +
    "  <li class=\"record-list-item\"\n" +
    "      ng-class=\"{\n" +
    "        'field-addition' : (diffType === 'output' && (updatedField || updatedValue)),\n" +
    "        'field-deletion': (diffType === 'input' && (updatedField || updatedValue)),\n" +
    "        'is-root': isRoot\n" +
    "      }\">\n" +
    "\n" +
    "    <div>\n" +
    "\n" +
    "      <label class=\"pull-left\"\n" +
    "             ng-class=\"{\n" +
    "              'label label-danger': isError && isRoot,\n" +
    "              'root-label': isRoot\n" +
    "             }\">\n" +
    "\n" +
    "        <span class=\"pointer toggler {{(isOpen || (isRoot && record.expand)) ? 'open' : ''}}\"\n" +
    "            ng-click=\"onClick($event)\"\n" +
    "            ng-if=\"recordValue.type === 'MAP' || recordValue.type === 'LIST' || recordValue.type === 'LIST_MAP'\"></span>\n" +
    "\n" +
    "        <input ng-if=\"selectable === true && !isRoot\" type=\"checkbox\" ng-model=\"selectedPath[recordValue.sqpath]\">\n" +
    "\n" +
    "        <span class=\"field-index\" ng-if=\"fieldIndex !== undefined\" ng-bind-html=\"fieldIndex\"></span>\n" +
    "        <span class=\"field-name\" ng-class=\"{'dirty ': recordValue.dirty || (record.dirty && isRoot)}\" ng-bind-html=\"fieldName\"></span> :\n" +
    "\n" +
    "\n" +
    "        <a class=\"view-stack-trace\"\n" +
    "           ng-if=\"isRoot && record.header.errorStackTrace\"\n" +
    "           ng-click=\"showStackTrace(record.header)\">\n" +
    "          ( <span translate=\"home.graphPane.viewStackTrace\">View Stack Trace</span>... )\n" +
    "        </a>\n" +
    "\n" +
    "        <span class=\"glyphicon glyphicon-asterisk dirty-icon\" ng-if=\"record.dirty && isRoot\"></span>\n" +
    "      </label>\n" +
    "\n" +
    "      <div ng-switch=\"recordValue.type\">\n" +
    "\n" +
    "        <div ng-switch-when=\"MAP\">\n" +
    "          <span class=\"field-type\" ng-show=\"showFieldType\">{{recordValue.type}}</span>\n" +
    "          <record-tree\n" +
    "            ng-if=\"(isOpen || (isRoot && record.expand)) && recordValue.value\"\n" +
    "            ng-repeat=\"_fieldName in mapKeys | limitTo: limit\"\n" +
    "            record=\"record\"\n" +
    "            record-value=\"recordValue.value[_fieldName]\"\n" +
    "            diff-type=\"diffType\"\n" +
    "            diff-record-value=\"diffRecordValue.value[_fieldName]\"\n" +
    "            field-name=\"_fieldName\"\n" +
    "            is-root=\"false\"\n" +
    "            editable=\"editable\"\n" +
    "            selectable=\"selectable\"\n" +
    "            selected-path=\"selectedPath\"\n" +
    "            show-field-type=\"showFieldType\"\n" +
    "            show-header=\"showHeader\">\n" +
    "          </record-tree>\n" +
    "\n" +
    "          <span ng-if=\"recordValue.value == null\" class=\"field-value\">null</span>\n" +
    "        </div>\n" +
    "\n" +
    "        <div ng-switch-when=\"LIST_MAP\">\n" +
    "          <span class=\"field-type\" ng-show=\"showFieldType\">{{recordValue.type}}</span>\n" +
    "          <record-tree\n" +
    "            ng-if=\"(isOpen || (isRoot && record.expand)) && recordValue.value\"\n" +
    "            ng-repeat=\"_fieldName in mapKeys | limitTo: limit\"\n" +
    "            record=\"record\"\n" +
    "            record-value=\"recordValue.value[_fieldName]\"\n" +
    "            diff-type=\"diffType\"\n" +
    "            diff-record-value=\"diffRecordValue.value[_fieldName]\"\n" +
    "            field-name=\"getListMapKey(recordValue.value[_fieldName])\"\n" +
    "            field-index=\"$index\"\n" +
    "            is-root=\"false\"\n" +
    "            editable=\"editable\"\n" +
    "            selectable=\"selectable\"\n" +
    "            selected-path=\"selectedPath\"\n" +
    "            show-field-type=\"showFieldType\"\n" +
    "            show-header=\"showHeader\">\n" +
    "          </record-tree>\n" +
    "\n" +
    "          <span ng-if=\"recordValue.value == null\" class=\"field-value\">null</span>\n" +
    "        </div>\n" +
    "\n" +
    "        <div ng-switch-when=\"LIST\">\n" +
    "          <span class=\"field-type\" ng-show=\"showFieldType\">{{recordValue.type}} [ {{recordValue.value.length}} ]</span>\n" +
    "          <record-tree\n" +
    "            ng-if=\"(isOpen || (isRoot && record.expand)) && recordValue.value\"\n" +
    "            ng-repeat=\"(_fieldName, _recordValue) in recordValue.value | limitTo: limit\"\n" +
    "            record=\"record\"\n" +
    "            record-value=\"_recordValue\"\n" +
    "            diff-type=\"diffType\"\n" +
    "            diff-record-value=\"diffRecordValue.value[_fieldName]\"\n" +
    "            field-name=\"_fieldName\"\n" +
    "            is-root=\"false\"\n" +
    "            editable=\"editable\"\n" +
    "            selectable=\"selectable\"\n" +
    "            selected-path=\"selectedPath\"\n" +
    "            show-field-type=\"showFieldType\"\n" +
    "            show-header=\"showHeader\">\n" +
    "          </record-tree>\n" +
    "\n" +
    "          <span ng-if=\"recordValue.value == null\" class=\"field-value\">null</span>\n" +
    "        </div>\n" +
    "\n" +
    "\n" +
    "        <div class=\"show-more\" ng-if=\"(isOpen || (isRoot && record.expand)) && limit < valueLength\">\n" +
    "          <a class=\"btn btn-link\"\n" +
    "             ng-click=\"onShowMoreClick($event);\">Show More...</a>\n" +
    "\n" +
    "          <span class=\"separator\">|</span>\n" +
    "\n" +
    "          <a class=\"btn btn-link\"\n" +
    "             ng-click=\"onShowAllClick($event);\">Show All ({{valueLength}}) ...</a>\n" +
    "        </div>\n" +
    "\n" +
    "        <div ng-switch-when=\"DATETIME\" class=\"clearfix\">\n" +
    "\n" +
    "          <span class=\"field-type pull-left\" ng-show=\"showFieldType\">{{recordValue.type}}</span>\n" +
    "\n" +
    "          <span class=\"field-value pull-left {{recordValue.type}}\"\n" +
    "                ng-class=\"{'value-addition' : (diffType === 'output' && updatedValue), 'value-deletion' : (diffType === 'input' && updatedValue)}\"\n" +
    "            >{{recordValue.value + '' | date:'medium'}} </span>\n" +
    "\n" +
    "          <div class=\"dropdown pull-left\"\n" +
    "               ng-if=\"editable === true\"\n" +
    "               ng-init=\"dateRecordValue = recordValue.value\">\n" +
    "            <a class=\"record-date-toggle dropdown-toggle\" role=\"button\" data-toggle=\"dropdown\" data-target=\"#\" href=\"\">\n" +
    "              <i class=\"fa fa-calendar\"></i>\n" +
    "            </a>\n" +
    "            <ul class=\"dropdown-menu pull-right\" role=\"menu\" aria-labelledby=\"dLabel\">\n" +
    "              <datetimepicker data-ng-model=\"dateRecordValue\"\n" +
    "                              data-on-set-time=\"recordDateValueUpdated(record, recordValue, dateRecordValue)\"\n" +
    "                              data-datetimepicker-config=\"{ dropdownSelector: '.record-date-toggle' }\"></datetimepicker>\n" +
    "            </ul>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div ng-switch-when=\"DATE\" class=\"clearfix\">\n" +
    "          <span class=\"field-type pull-left\" ng-show=\"showFieldType\">{{recordValue.type}}</span>\n" +
    "\n" +
    "          <span class=\"field-value pull-left {{recordValue.type}}\"\n" +
    "                ng-class=\"{'value-addition' : (diffType === 'output' && updatedValue), 'value-deletion' : (diffType === 'input' && updatedValue)}\"\n" +
    "          >{{recordValue.value + '' | date:'mediumDate'}} </span>\n" +
    "\n" +
    "          <div class=\"dropdown pull-left\"\n" +
    "               ng-if=\"editable === true\"\n" +
    "               ng-init=\"dateRecordValue = recordValue.value\">\n" +
    "            <a class=\"record-date-toggle dropdown-toggle\" role=\"button\" data-toggle=\"dropdown\" data-target=\"#\" href=\"\">\n" +
    "              <i class=\"fa fa-calendar\"></i>\n" +
    "            </a>\n" +
    "            <ul class=\"dropdown-menu pull-right\" role=\"menu\" aria-labelledby=\"dLabel\">\n" +
    "              <datetimepicker data-ng-model=\"dateRecordValue\"\n" +
    "                              data-on-set-time=\"recordDateValueUpdated(record, recordValue, dateRecordValue)\"\n" +
    "                              data-datetimepicker-config=\"{ dropdownSelector: '.record-date-toggle', minView: 'day' }\"></datetimepicker>\n" +
    "            </ul>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div ng-switch-when=\"TIME\" class=\"clearfix\">\n" +
    "          <span class=\"field-type pull-left\" ng-show=\"showFieldType\">{{recordValue.type}}</span>\n" +
    "\n" +
    "          <span class=\"field-value pull-left {{recordValue.type}}\"\n" +
    "                ng-class=\"{'value-addition' : (diffType === 'output' && updatedValue), 'value-deletion' : (diffType === 'input' && updatedValue)}\"\n" +
    "            >{{recordValue.value + '' | date:'mediumTime'}} </span>\n" +
    "\n" +
    "          <div class=\"dropdown pull-left\"\n" +
    "               ng-if=\"editable === true\"\n" +
    "               ng-init=\"dateRecordValue = recordValue.value\">\n" +
    "            <a class=\"record-date-toggle dropdown-toggle\" role=\"button\" data-toggle=\"dropdown\" data-target=\"#\" href=\"\">\n" +
    "              <i class=\"fa fa-calendar\"></i>\n" +
    "            </a>\n" +
    "            <ul class=\"dropdown-menu pull-right\" role=\"menu\" aria-labelledby=\"dLabel\">\n" +
    "              <datetimepicker data-ng-model=\"dateRecordValue\"\n" +
    "                              data-on-set-time=\"recordDateValueUpdated(record, recordValue, dateRecordValue)\"\n" +
    "                              data-datetimepicker-config=\"{ dropdownSelector: '.record-date-toggle', startView: 'hour' }\"></datetimepicker>\n" +
    "            </ul>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div ng-switch-when=\"FILE_REF\" class=\"clearfix\">\n" +
    "          <span class=\"field-type\" ng-show=\"showFieldType\">{{recordValue.type}}</span>\n" +
    "          <span class=\"field-value {{((recordValue.value != null) ? recordValue.type : '')}}\">{{recordValue.value | json}}</span>\n" +
    "        </div>\n" +
    "\n" +
    "        <div ng-switch-default>\n" +
    "\n" +
    "          <span class=\"field-type\" ng-show=\"showFieldType\">{{recordValue.type}}</span>\n" +
    "\n" +
    "          <span ng-if=\"editable !== true\"\n" +
    "                class=\"field-value {{((recordValue.value != null) ? recordValue.type : '')}}\"\n" +
    "                ng-class=\"{'value-addition' : (diffType === 'output' && updatedValue), 'value-deletion' : (diffType === 'input' && updatedValue)}\"\n" +
    "            >{{recordValue.value + ''}}</span>\n" +
    "\n" +
    "          <span class=\"field-value {{recordValue.dirty ? 'dirty ' + ((recordValue.value != null) ? recordValue.type : '') : ((recordValue.value != null) ? recordValue.type : '')}}\"\n" +
    "                ng-if=\"editable === true\"\n" +
    "                ng-class=\"{'value-addition' : (diffType === 'output' && updatedValue), 'value-deletion' : (diffType === 'input' && updatedValue)}\"\n" +
    "                contenteditable=\"true\"\n" +
    "                value-type=\"recordValue.type\"\n" +
    "                ng-change=\"recordValueUpdated(record, recordValue)\"\n" +
    "                ng-model=\"recordValue.value\">\n" +
    "          </span>\n" +
    "        </div>\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </li>\n" +
    "\n" +
    "  <li class=\"record-list-item\" ng-if=\"showHeader && recordValue.attributes && (isOpen || record.expand)\" ng-init=\"fieldHeaderExpand=false\">\n" +
    "    <label>\n" +
    "      <span class=\"pointer toggler {{fieldHeaderExpand ? 'open' : ''}}\"\n" +
    "            ng-click=\"fieldHeaderExpand = !fieldHeaderExpand\"></span>\n" +
    "      <span class=\"field-name\" translate=\"global.form.fieldHeader\">Field Header</span>\n" +
    "    </label>\n" +
    "    <ul class=\"record-list\" ng-if=\"fieldHeaderExpand\">\n" +
    "      <li class=\"record-list-item\"\n" +
    "          ng-repeat=\"(headerName, headerValue) in recordValue.attributes\">\n" +
    "        <label class=\"pull-left\"><span class=\"field-name\">{{headerName}}:</span></label>\n" +
    "        <span class=\"field-value STRING\">{{headerValue + ''}}</span>\n" +
    "      </li>\n" +
    "    </ul>\n" +
    "  </li>\n" +
    "\n" +
    "  <li class=\"record-list-item\" ng-if=\"showHeader && record.header && isRoot && (isOpen || (isRoot && record.expand))\">\n" +
    "      <label>\n" +
    "        <span class=\"pointer toggler {{headerExpand ? 'open' : ''}}\"\n" +
    "              ng-click=\"headerExpand = !headerExpand\"></span>\n" +
    "        <span class=\"field-name\" translate=\"global.form.recordHeader\">Record Header</span>\n" +
    "      </label>\n" +
    "      <ul class=\"record-list\" ng-if=\"headerExpand\">\n" +
    "        <li class=\"record-list-item\"\n" +
    "            ng-repeat=\"(headerName, headerValue) in record.header\"\n" +
    "            ng-if=\"headerValue && headerName !== 'values'\">\n" +
    "          <label class=\"pull-left\"><span class=\"field-name\">{{headerName}}:</span></label>\n" +
    "          <span class=\"field-value STRING\" ng-if=\"headerName !== 'errorTimestamp'\">{{headerValue + ''}}</span>\n" +
    "          <span class=\"field-value DATE\" ng-if=\"headerName === 'errorTimestamp'\">{{headerValue + '' | date:'medium'}}</span>\n" +
    "        </li>\n" +
    "\n" +
    "        <li class=\"record-list-item\">\n" +
    "          <span class=\"field-name\">values:</span>\n" +
    "\n" +
    "          <ul class=\"record-list\">\n" +
    "            <li class=\"record-list-item\"\n" +
    "                ng-repeat=\"(headerName, headerValue) in record.header.values\">\n" +
    "              <label class=\"pull-left\"><span class=\"field-name\">{{headerName}}:</span></label>\n" +
    "              <span class=\"field-value STRING\">{{headerValue + ''}}</span>\n" +
    "            </li>\n" +
    "          </ul>\n" +
    "        </li>\n" +
    "\n" +
    "      </ul>\n" +
    "  </li>\n" +
    "\n" +
    "</ul>\n" +
    "\n" +
    "");
}]);

angular.module("common/directives/swagger-ui/swagger-ui.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("common/directives/swagger-ui/swagger-ui.tpl.html",
    "<div class=\"swagger-ui\" aria-live=\"polite\" aria-relevant=\"additions removals\">\n" +
    "  <!--\n" +
    "  <div class=\"api-name\">\n" +
    "    <h3 ng-bind=\"infos.title\"></h3>\n" +
    "  </div>\n" +
    "  <div class=\"api-description\" ng-bind-html=\"infos.description\"></div>\n" +
    "  -->\n" +
    "  <div class=\"api-infos\">\n" +
    "    <div class=\"api-infos-contact\" ng-if=\"infos.contact\">\n" +
    "      <div ng-if=\"infos.contact.name\" class=\"api-infos-contact-name\">created by <span ng-bind=\"infos.contact.name\"></span></div>\n" +
    "      <div ng-if=\"infos.contact.url\" class=\"api-infos-contact-url\">see more at <a href=\"{{infos.contact.url}}\" ng-bind=\"infos.contact.url\"></a></div>\n" +
    "      <a ng-if=\"infos.contact.email\" class=\"api-infos-contact-url\" href=\"mailto:{{infos.contact.email}}?subject={{infos.title}}\">contact the developer</a>\n" +
    "    </div>\n" +
    "    <div class=\"api-infos-license\" ng-if=\"infos.license\">\n" +
    "      <span>license: </span><a href=\"{{infos.license.url}}\" ng-bind=\"infos.license.name\"></a>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "  <ul class=\"list-unstyled endpoints\">\n" +
    "    <li ng-repeat=\"api in resources track by $index\" class=\"endpoint\" ng-class=\"{active:api.open}\">\n" +
    "      <div class=\"clearfix\">\n" +
    "        <ul class=\"list-inline pull-left endpoint-heading\">\n" +
    "          <li>\n" +
    "            <h4>\n" +
    "              <a href=\"javascript:;\" ng-click=\"api.open=!api.open;permalink(api.open?api.name:null)\" ng-bind=\"api.name\"></a>\n" +
    "              <span ng-if=\"api.description\"> : <span ng-bind=\"api.description\"></span></span>\n" +
    "            </h4>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "        <ul class=\"list-inline pull-right endpoint-actions\">\n" +
    "          <li>\n" +
    "            <a href=\"javascript:;\" ng-click=\"api.open=!api.open;permalink(api.open?api.name:null)\">open/hide</a>\n" +
    "          </li>\n" +
    "          <li>\n" +
    "            <a href=\"javascript:;\" ng-click=\"expand(api);permalink(api.name)\">list operations</a>\n" +
    "          </li>\n" +
    "          <li>\n" +
    "            <a href=\"javascript:;\" ng-click=\"expand(api,true);permalink(api.name+'*')\">expand operations</a>\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "      <ul class=\"list-unstyled collapse operations\" ng-class=\"{in:api.open}\">\n" +
    "        <li ng-repeat=\"op in api.operations track by $index\" class=\"operation {{op.httpMethod}}\">\n" +
    "          <div class=\"heading\">\n" +
    "            <a ng-click=\"op.open=!op.open;permalink(op.open?op.operationId:null)\" href=\"javascript:;\">\n" +
    "              <div class=\"clearfix\">\n" +
    "                <span class=\"http-method text-uppercase\" ng-bind=\"op.httpMethod\"></span>\n" +
    "                <span class=\"path\" ng-bind=\"op.path\"></span>\n" +
    "                <span class=\"description pull-right\" ng-bind=\"op.summary\"></span>\n" +
    "              </div>\n" +
    "            </a>\n" +
    "          </div>\n" +
    "          <div class=\"content collapse\" ng-class=\"{in:op.open}\">\n" +
    "            <div ng-if=\"op.description\">\n" +
    "              <h5>implementation notes</h5>\n" +
    "              <p ng-bind-html=\"op.description\"></p>\n" +
    "            </div>\n" +
    "            <form role=\"form\" name=\"explorerForm\" ng-submit=\"explorerForm.$valid&&submitExplorer(op)\">\n" +
    "              <div ng-if=\"op.responseClass\" class=\"response\">\n" +
    "                <h5>response class (status {{op.responseClass.status}})</h5>\n" +
    "                <div ng-if=\"op.responseClass.display!==-1\">\n" +
    "                  <ul class=\"list-inline schema\">\n" +
    "                    <li><a href=\"javascript:;\" ng-click=\"op.responseClass.display=0\" ng-class=\"{active:op.responseClass.display===0}\">model</a></li>\n" +
    "                    <li><a href=\"javascript:;\" ng-click=\"op.responseClass.display=1\" ng-class=\"{active:op.responseClass.display===1}\">model schema</a></li>\n" +
    "                  </ul>\n" +
    "                  <pre class=\"model\" ng-if=\"op.responseClass.display===0\" ng-bind-html=\"op.responseClass.schema.model\"></pre>\n" +
    "                  <pre class=\"model-schema\" ng-if=\"op.responseClass.display===1\" ng-bind=\"op.responseClass.schema.json\"></pre>\n" +
    "                </div>\n" +
    "                <div ng-if=\"op.produces\" class=\"content-type\">\n" +
    "                  <label for=\"responseContentType{{op.id}}\">response content type</label>\n" +
    "                  <select ng-model=\"form[op.id].responseType\" ng-options=\"item for item in op.produces track by item\" id=\"responseContentType{{op.id}}\" name=\"responseContentType{{op.id}}\" required></select>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "              <div ng-if=\"op.parameters&&op.parameters.length>0\" class=\"table-responsive\">\n" +
    "                <h5>parameters</h5>\n" +
    "                <table class=\"table table-condensed parameters\">\n" +
    "                  <thead>\n" +
    "                  <tr>\n" +
    "                    <th class=\"name\">parameter</th>\n" +
    "                    <th class=\"value\">value</th>\n" +
    "                    <th class=\"desc\">description</th>\n" +
    "                    <th class=\"type\">parameter type</th>\n" +
    "                    <th class=\"data\">data type</th>\n" +
    "                  </tr>\n" +
    "                  </thead>\n" +
    "                  <tbody>\n" +
    "                  <tr ng-repeat=\"param in op.parameters track by $index\">\n" +
    "                    <td ng-class=\"{bold:param.required}\">\n" +
    "                      <label for=\"param{{param.id}}\" ng-bind=\"param.name\"></label>\n" +
    "                    </td>\n" +
    "                    <td ng-class=\"{bold:param.required}\">\n" +
    "                      <div ng-if=\"apiExplorer\">\n" +
    "                        <div ng-if=\"param.in!=='body'\" ng-switch=\"param.subtype\">\n" +
    "                          <input ng-switch-when=\"file\" type=\"file\" file-input ng-model=\"form[op.id][param.name]\" id=\"param{{param.id}}\" placeholder=\"{{param.required?'(required)':''}}\" ng-required=\"param.required\">\n" +
    "                          <select ng-switch-when=\"enum\" ng-model=\"form[op.id][param.name]\" id=\"param{{param.id}}\">\n" +
    "                            <option ng-repeat=\"value in param.enum\" value=\"{{value}}\" ng-bind=\"value+(param.default===value?' (default)':'')\" ng-selected=\"param.default===value\"></option>\n" +
    "                          </select>\n" +
    "                          <input ng-switch-default type=\"text\" ng-model=\"form[op.id][param.name]\" id=\"param{{param.id}}\" placeholder=\"{{param.required?'(required)':''}}\" ng-required=\"param.required\">\n" +
    "                        </div>\n" +
    "                        <div ng-if=\"param.in==='body'\">\n" +
    "                          <textarea id=\"param{{param.id}}\" ng-model=\"form[op.id][param.name]\" ng-required=\"param.required\"></textarea>\n" +
    "                          <br>\n" +
    "                          <div ng-if=\"op.consumes\" class=\"content-type\">\n" +
    "                            <label for=\"bodyContentType{{op.id}}\">parameter content type</label>\n" +
    "                            <select ng-model=\"form[op.id].contentType\" id=\"bodyContentType{{op.id}}\" name=\"bodyContentType{{op.id}}\" ng-options=\"item for item in op.consumes track by item\"></select>\n" +
    "                          </div>\n" +
    "                        </div>\n" +
    "                      </div>\n" +
    "                      <div ng-if=\"!apiExplorer\">\n" +
    "                        <div ng-if=\"param.in!=='body'\">\n" +
    "                          <div ng-if=\"param.default\"><span ng-bind=\"param.default\"></span> (default)</div>\n" +
    "                          <div ng-if=\"param.enum\">\n" +
    "                            <span ng-repeat=\"value in param.enum track by $index\">{{value}}<span ng-if=\"!$last\"> or </span></span>\n" +
    "                          </div>\n" +
    "                          <div ng-if=\"param.required\"><strong>(required)</strong></div>\n" +
    "                        </div>\n" +
    "                      </div>\n" +
    "                    </td>\n" +
    "                    <td ng-class=\"{bold:param.required}\" ng-bind-html=\"param.description\"></td>\n" +
    "                    <td ng-bind=\"param.in\"></td>\n" +
    "                    <td ng-if=\"param.type\" ng-switch=\"param.type\">\n" +
    "                      <span ng-switch-when=\"array\" ng-bind=\"'Array['+param.items.type+']'\"></span>\n" +
    "                      <span ng-switch-default ng-bind=\"param.type\"></span>\n" +
    "                    </td>\n" +
    "                    <td ng-if=\"param.schema\">\n" +
    "                      <ul class=\"list-inline schema\">\n" +
    "                        <li><a href=\"javascript:;\" ng-click=\"param.schema.display=0\" ng-class=\"{active:param.schema.display===0}\">model</a></li>\n" +
    "                        <li><a href=\"javascript:;\" ng-click=\"param.schema.display=1\" ng-class=\"{active:param.schema.display===1}\">model schema</a></li>\n" +
    "                      </ul>\n" +
    "                      <pre class=\"model\" ng-if=\"param.schema.display===0&&param.schema.model\" ng-bind-html=\"param.schema.model\"></pre>\n" +
    "                      <div class=\"model-schema\" ng-if=\"param.schema.display===1&&param.schema.json\">\n" +
    "                        <pre ng-bind=\"param.schema.json\" ng-click=\"form[op.id][param.name]=param.schema.json\" aria-described-by=\"help-{{param.id}}\"></pre>\n" +
    "                        <div id=\"help-{{param.id}}\">click to set as parameter value</div>\n" +
    "                      </div>\n" +
    "                    </td>\n" +
    "                  </tr>\n" +
    "                  </tbody>\n" +
    "                </table>\n" +
    "              </div>\n" +
    "              <div class=\"table-responsive\" ng-if=\"op.hasResponses\">\n" +
    "                <h5>response messages</h5>\n" +
    "                <table class=\"table responses\">\n" +
    "                  <thead>\n" +
    "                  <tr>\n" +
    "                    <th class=\"code\">HTTP status code</th>\n" +
    "                    <th>reason</th>\n" +
    "                    <th>response model</th>\n" +
    "                  </tr>\n" +
    "                  </thead>\n" +
    "                  <tbody>\n" +
    "                  <tr ng-repeat=\"(code, resp) in op.responses track by $index\">\n" +
    "                    <td ng-bind=\"code\"></td>\n" +
    "                    <td ng-bind-html=\"resp.description\"></td>\n" +
    "                    <td>\n" +
    "                      <ul ng-if=\"resp.schema&&resp.schema.model&&resp.schema.json\" class=\"list-inline schema\">\n" +
    "                        <li><a href=\"javascript:;\" ng-click=\"resp.display=0\" ng-class=\"{active:resp.display===0}\">model</a></li>\n" +
    "                        <li><a href=\"javascript:;\" ng-click=\"resp.display=1\" ng-class=\"{active:resp.display===1}\">model schema</a></li>\n" +
    "                      </ul>\n" +
    "                      <pre class=\"model\" ng-if=\"resp.display===0&&resp.schema&&resp.schema.model\" ng-bind-html=\"resp.schema.model\"></pre>\n" +
    "                      <pre class=\"model-schema\" ng-if=\"resp.display===1&&resp.schema&&resp.schema.json\" ng-bind=\"resp.schema.json\"></pre>\n" +
    "                    </td>\n" +
    "                  </tr>\n" +
    "                  </tbody>\n" +
    "                </table>\n" +
    "              </div>\n" +
    "              <div ng-if=\"apiExplorer\">\n" +
    "                <button class=\"btn btn-default\" ng-click=\"op.explorerResult=false;op.hideExplorerResult=false\" type=\"submit\" ng-disabled=\"op.loading\" ng-bind=\"op.loading?'loading...':'try it out!'\"></button>\n" +
    "                <a class=\"hide-try-it\" ng-if=\"op.explorerResult&&!op.hideExplorerResult\" ng-click=\"op.hideExplorerResult=true\" href=\"javascript:;\">hide response</a>\n" +
    "              </div>\n" +
    "            </form>\n" +
    "            <div ng-if=\"op.explorerResult\" ng-show=\"!op.hideExplorerResult\">\n" +
    "              <h5>request URL</h5>\n" +
    "              <pre ng-bind=\"op.explorerResult.url\"></pre>\n" +
    "              <h5>response body</h5>\n" +
    "              <pre ng-bind=\"op.explorerResult.response.body\"></pre>\n" +
    "              <h5>response code</h5>\n" +
    "              <pre ng-bind=\"op.explorerResult.response.status\"></pre>\n" +
    "              <h5>response headers</h5>\n" +
    "              <pre ng-bind=\"op.explorerResult.response.headers\"></pre>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "        </li>\n" +
    "      </ul>\n" +
    "    </li>\n" +
    "  </ul>\n" +
    "  <div class=\"api-version clearfix\" ng-if=\"infos\">\n" +
    "    [BASE URL: <span class=\"h4\" ng-bind=\"infos.basePath\"></span>, API VERSION: <span class=\"h4\" ng-bind=\"infos.version\"></span>, HOST: <span class=\"h4\" ng-bind=\"infos.scheme\"></span>://<span class=\"h4\" ng-bind=\"infos.host\"></span>]\n" +
    "  </div>\n" +
    "</div>");
}]);
