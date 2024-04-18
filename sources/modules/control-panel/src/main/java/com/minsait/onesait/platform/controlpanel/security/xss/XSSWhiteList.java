/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.controlpanel.security.xss;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class XSSWhiteList {

	private static Map<String, HashSet<String>> mWhiteList;
	private static final String HEADERLIBS = "headerlibs";
	private static final String HEADERLIBS_AUX = "headerlibs_aux";
	private static final String POSTPROCESS = "postProcess";
	private static final String MODEL = "model";

	static {
		mWhiteList = new HashMap<>();
		mWhiteList.put("/viewers/create", new HashSet<>(Arrays.asList("jsViewer", "jsBody", "js")));
		mWhiteList.put("/viewers/update", new HashSet<>(Arrays.asList("jsViewer", "jsBody", "js")));
		mWhiteList.put("/dashboards/editfull", new HashSet<>(Arrays.asList(HEADERLIBS, HEADERLIBS_AUX, MODEL, "js")));
		mWhiteList.put("/dashboards/create", new HashSet<>(Arrays.asList(HEADERLIBS, HEADERLIBS_AUX, MODEL, "js")));
		mWhiteList.put("/dashboards/edit", new HashSet<>(Arrays.asList("dashboard", MODEL, "js")));
		mWhiteList.put("/dashboards/dashboardconf", new HashSet<>(Arrays.asList(HEADERLIBS, HEADERLIBS_AUX, "js")));
		mWhiteList.put("/dashboardconf/update", new HashSet<>(Arrays.asList(HEADERLIBS, HEADERLIBS_AUX, MODEL, "js")));
		mWhiteList.put("/dashboardconf/create", new HashSet<>(Arrays.asList(HEADERLIBS, HEADERLIBS_AUX, MODEL, "js")));
		mWhiteList.put("/gadgets/create", new HashSet<>(Arrays.asList("*")));
		mWhiteList.put("/gadgettemplates/create",
				new HashSet<>(Arrays.asList("templateCode", "templateCodeJS", "template", "templateJS", "js")));
		mWhiteList.put("/gadgettemplates/update",
				new HashSet<>(Arrays.asList("templateCode", "templateCodeJS", "template", "templateJS", "js")));
		mWhiteList.put("/querytemplates/create", new HashSet<>(Arrays.asList("querySelector", "queryGenerator", "js")));
		mWhiteList.put("/querytemplates/update", new HashSet<>(Arrays.asList("querySelector", "queryGenerator", "js")));
		mWhiteList.put("/apimanager/create",
				new HashSet<>(Arrays.asList("postProcessFx", "postProcessTool", "operationsObject", "js")));
		mWhiteList.put("/apimanager/update",
				new HashSet<>(Arrays.asList("postProcessFx", "postProcessTool", "operationsObject", "js")));
		mWhiteList.put("/digitaltwintypes/create", new HashSet<>(Arrays.asList("logic", "logicEditor", "js")));
		mWhiteList.put("/digitaltwintypes/update", new HashSet<>(Arrays.asList("logic", "logicEditor", "js")));
		mWhiteList.put("/ontologies/createkpi", new HashSet<>(Arrays.asList(POSTPROCESS, "js")));
		mWhiteList.put("/ontologies/createapirest", new HashSet<>(Arrays.asList(POSTPROCESS, "js")));
		mWhiteList.put("/ontologies/update", new HashSet<>(Arrays.asList(POSTPROCESS, "js")));
		mWhiteList.put("/configurations/create", new HashSet<>(Arrays.asList("ymlConfig")));
		mWhiteList.put("/configurations/update", new HashSet<>(Arrays.asList("ymlConfig")));
		mWhiteList.put("/rule-domains", new HashSet<>(Arrays.asList("DRL")));

	}

	public static boolean isInWhiteList(String path, String field) {
		final HashSet<String> fields = mWhiteList.get(path);
		if (fields != null) {
			return fields.contains(field) || fields.contains("*");
		} else {
			for (Map.Entry<String, HashSet<String>> entry : mWhiteList.entrySet()) {
				String key = entry.getKey();
				if (path.startsWith(key)) {
					return mWhiteList.get(key).contains(field) || mWhiteList.get(key).contains("*");
				}
			}
		}
		return false;

	}
}
