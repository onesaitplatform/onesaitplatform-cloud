/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.controlpanel.controller.adminHome;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.webproject.WebProjectDTO;
import com.minsait.onesait.platform.config.services.webproject.WebProjectService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@Controller
public class AdminGraphController {

    private static final String GENERIC_USER_NAME = "USER";
    @Autowired
    private AdminGraphUtil graphUtil;

    @Autowired
    private AppWebUtils utils;
    @Autowired
    private WebProjectService webProjectService;
    @Autowired
    private UserService userService;

    @GetMapping("/getadmingraph")
    public @ResponseBody String getAdminGraph(Model model, @RequestParam(value = "all",
        required = false) Boolean all) {
        final List<AdminGraphDTO> arrayLinks = new LinkedList<>();

        final List<WebProjectDTO> webprojects = webProjectService
            .getWebProjectsWithDescriptionAndIdentification(utils.getUserId(), null, null);
        final User user = null == all || all ? null : userService.getUser(utils.getUserId());
        
        arrayLinks.addAll(graphUtil.constructGraphWithUsers(null));
        arrayLinks.addAll(graphUtil.constructGraphWithOntologies(null, user));
        arrayLinks.addAll(graphUtil.constructGraphWithClientPlatforms(null, user));
        arrayLinks.addAll(graphUtil.constructGraphWithVisualization(null, null, user));
        arrayLinks.addAll(graphUtil.constructGraphWithAPIs(null, user));
        arrayLinks.addAll(graphUtil.constructGraphWithDigitalTwins(null, user));
        arrayLinks.addAll(graphUtil.constructGraphWithFlows(null, user));
        arrayLinks.addAll(graphUtil.constructGraphWithWebProjects(webprojects, user));
        arrayLinks.addAll(graphUtil.constructGraphWithNotebooks(null, user));
        arrayLinks.addAll(graphUtil.constructGraphWithDataFlows(null, user));
        
        int total = 0;
        for(AdminGraphDTO agraph : arrayLinks) {
            if(agraph.getClassSource().equals(GENERIC_USER_NAME))
                total += agraph.getTotal();
        }
        arrayLinks.add(AdminGraphDTO.constructSingleNode(GENERIC_USER_NAME, null, GENERIC_USER_NAME, utils.getUserId().toUpperCase() + " ELEMENTS",
            utils.getUserId(), total));
        return arrayLinks.toString();
    }
}
